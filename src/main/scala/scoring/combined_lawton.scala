package fett.scoring

import fett.sexp._
import fett.util.logger
import fett.util.utils.Implicits._
import fett.util.hashConsing._
import fett.util.trees._

import scala.collection.mutable.{MutableList => MList, Set => MSet, Map => MMap}
import scala.collection.concurrent.TrieMap

import scala.language.postfixOps

import scala.util.parsing.json._
import java.nio.file.{Files, Paths}
import java.nio.charset.Charset

// https://rosettacode.org/wiki/Levenshtein_distance#Scala
import scala.math._

object CombinedLawton {
  val cutoffSize = 30

  type SimilarityScore = (Double, Double, Map[Symbol, Double]) ⇒ (Symbol, Symbol) ⇒ Double

  def fromOptions(options: Map[String, String]) = {
    val cutoffEnabled = options.get("cutoff").fold(true)(
      (s:String) ⇒ s.toLowerCase match {
        case "true" ⇒ true
        case "false" ⇒ false
        case _ ⇒ sys.error("Expected a Boolean for cutoff, got " + s)
      })
    val combineConsecutiveTerminals = options.get("combineConsecutiveTerminals").fold(true)(
      (s:String) ⇒ s.toLowerCase match {
        case "true" ⇒ true
        case "false" ⇒ false
        case _ ⇒ sys.error("Expected a Boolean for combineConsecutiveTerminals, got " + s)
      })
    val matchScore = options.get("matchScore").fold[Double](2)(_.toDouble)
    val miningMultiplier = options.get("miningMultiplier").fold[Double](2.0)(_.toDouble)
    val mismatchScore = options.get("mismatchScore").fold[Double](-1)(_.toDouble)
    val gapScore = options.get("gapScore").fold[Double](-1)(_.toDouble)
    val similarity = options.getOrElse("similarity", "naive")
    val similarityFn = similarity match {
      case "tokenName" | "naive" ⇒ naiveNodeNameSimilarity
      case "classBased" ⇒ classBasedNodeNameSimilarity
      case "levenshtein" ⇒ levenshteinBasedNodeNameSimilarity
      case _ ⇒ sys.error(s"Unknown similarity metric name: '$similarity'")
    }
    val eqClasses = options.get("classFile") map {
      f ⇒
      val contents = new String(Files.readAllBytes(Paths.get(f)), Charset.forName("UTF-8")).replaceAll("//.*", "")
      // we assume that the JSON file is structured as follows:
      // {
      //   useless: ["token1", "token2", ...],
      //   class1: ["token1'", "token2'", ...],
      //   class2: ["token1''", "token2''", ...]
      // }
      //
      try {
        // if the condition above is not satisfied, we will get a casting error at some point
        val classes = JSON.parseFull(contents).get.asInstanceOf[Map[String, Seq[String]]]
        val eqClasses = for ((name, tokens) ← (classes - "weights")) yield {
          val toks = tokens.map { t ⇒ Symbol("antlrparsers." + t + "Context") }
          name → toks.toSet
        }
        val eqClassMap = eqClasses.toMap
        val useless = eqClassMap.getOrElse("useless", Set.empty)
        val _weights = if (classes contains "weights") classes("weights").asInstanceOf[Map[String, Double]] else Map()
        val weights = _weights.map({ case (k, v) => eqClassMap(k) -> v })
        ((eqClassMap - "useless" - "weights").values.toSet, useless, weights)
      } catch {
        case e: ClassCastException ⇒
          sys.error(s"JSON file '$f' describing the equivalence sets is ill-formatted")
      }
    }
    val classes = eqClasses.map(_._1)
    val useless = eqClasses.map(_._2)
    val weights = eqClasses.map(_._3)
    CombinedLawton(matchScore, mismatchScore, gapScore, similarityFn, classes, useless, weights, cutoffEnabled, combineConsecutiveTerminals, miningMultiplier)
  }

  //—————————————————————————————————————————————————————
  // scoring functions for similarity between nodes
  // actual implementations are private to allow equality checks
  // on scoring functions in Smith-Waterman class

  // normalized levenshtein distances
  val levenshteinBasedNodeNameSimilarity = levenshteinSimilarityImpl _
  @inline private[this] def levenshteinSimilarityImpl(matchScore: Double, mismatchScore: Double, classNoToWeights: Map[Symbol, Double])(na: Symbol, nb: Symbol): Double = {
    val a = na.name
    val b = nb.name
    val normalizedEditDistance = Levenshtein.distance(a, b).doubleValue / math.max(a.length, b.length)
    // somewhere between mismatchScore and matchScore
    val comparisonScore = (matchScore - mismatchScore).doubleValue * (1.0 - normalizedEditDistance) + mismatchScore
    comparisonScore
  }

  // in class-based case, symbols are class-representatives
  val classBasedNodeNameSimilarity = classBasedSimilarityImpl _
  @inline private[this] def classBasedSimilarityImpl(matchScore: Double, mismatchScore: Double, classNoToWeights: Map[Symbol, Double])(a: Symbol, b: Symbol): Double = {
    if (a == b) {
      // println(s"a: $a")
      // println(s"b: $b")
      // println(classNoToWeights)
      classNoToWeights(a) * matchScore
    } else {
      math.max(classNoToWeights(a), classNoToWeights(b)) * mismatchScore
    }
  }

  // naive case
  val naiveNodeNameSimilarity = naiveSimilarityImpl _
  @inline def naiveSimilarityImpl(matchScore: Double, mismatchScore: Double, classNoToWeights: Map[Symbol, Double])(a: Symbol, b: Symbol): Double = {
    if (a == b) {
      matchScore
    } else {
      mismatchScore
    }
  }

}

case class CombinedLawton(matchScore: Double = 2,
                         mismatchScore: Double = -1,
                         gapScore: Double = -1,
                         similarityFn: CombinedLawton.SimilarityScore = CombinedLawton.naiveNodeNameSimilarity,
                         eqClasses: Option[Set[Set[Symbol]]] = None,
                         uselessNodeSet: Option[Set[Symbol]] = None,
                         weights: Option[Map[Set[Symbol], Double]] = None,
                         cutoffEnabled: Boolean = true,
                         combineConsecutiveTerminals: Boolean = true,
                         miningMultiplier: Double = 1.0
                         ) extends Scoring with SmartHash {

  val useClassBasedSimilarity = similarityFn == CombinedLawton.classBasedNodeNameSimilarity

  val eqClasses_ = if (useClassBasedSimilarity) {
    eqClasses.getOrElse(sys.error("Tried to use class-based similarity without similarity classes"))
  } else {
    Set.empty[Set[Symbol]]
  }

  val uselessNodeSet_ = if (useClassBasedSimilarity) {
    uselessNodeSet.getOrElse(sys.error("Tried to use class-based similarity without a useless node set")) + 'useless
  } else {
    Set.empty
  }

  // equivalence classes are disjoint so instead of taking intersection, we can just check for equality
  // for faster equality check, use automatically generated symbols
  val uselessNodeClassNo = Symbol("-1")
  // create a representative for each node class
  val nodeClasses = eqClasses_.zipWithIndex.toMap.mapValues((x:Int) ⇒ Symbol(x.toString)).withDefaultValue(uselessNodeClassNo)

  val nodeNameToClassNo: Map[Symbol, Symbol] = (for {
    c ← eqClasses_
    sym ← c
  } yield {
    sym → nodeClasses(c)
  }).toMap.withDefaultValue(uselessNodeClassNo)
  // println(nodeNameToClassNo)

  val usefulNodeSet = eqClasses_.flatten

  override def importantNodeLabels: Set[String] = Set("antlrparsers.java.JavaParser$ReturnStatementContext", "antlrparsers.cpp14.CPP14Parser$ReturnContext")

  val classNoToWeights: Map[Symbol, Double] = (for {
      (set, weight) <- weights.getOrElse(Map()).toList
      nodeName <- set
      classNo = nodeNameToClassNo(nodeName)
    } yield (classNo -> weight)
  ).toMap.withDefaultValue(1.0)
  // println(classNoToWeights)

  val nodeSimilarity = similarityFn(matchScore, mismatchScore, classNoToWeights)

  def removeConsecutiveDuplicates[A](l: List[A], ifTheyreInThisSet: Set[A]): List[A] = l match {
    case Nil => l
    case x :: Nil => l
    case x :: y :: ys if x == y && ifTheyreInThisSet.contains(x) => removeConsecutiveDuplicates(y :: ys, ifTheyreInThisSet)
  }

  // stupid naive
  def findAndReplace[A](l: IndexedSeq[A], patternsAndReplacements: Seq[(List[A], List[A])]): IndexedSeq[A] = l match {
    case IndexedSeq() => l
    case x +: xs => 
      for ((pat, repl) <- patternsAndReplacements) {
        if (l.take(pat.length) == pat)
          return repl.toIndexedSeq ++ findAndReplace(l.drop(pat.length), patternsAndReplacements)
      }

      return x +: findAndReplace(xs, patternsAndReplacements)
  }

  def preprocess(l: IndexedSeq[SymbolOrIndexedSeqOfConsecutiveTerminals]): IndexedSeq[SymbolOrIndexedSeqOfConsecutiveTerminals] = {
    if (useClassBasedSimilarity) {
      // println(l.filter(usefulNodeSet.contains(_)))
      if (combineConsecutiveTerminals) {
        val res = l.toIndexedSeq collect {
          case Left(sym) if usefulNodeSet.contains(sym) ⇒
            Left(nodeNameToClassNo(sym))
          case Right(s) if Mining.characteristicVectorFromSymbols(s).nonEmpty =>
            // only take the stuff that has a nonempty characteristic vector
            Right(s)
        }
        // !! TODO: either just return res like you used to or do this
        // combineTerminals(res.toList).toIndexedSeq
        res
      }
      else {
        l.toIndexedSeq collect {
          case Left(sym) if usefulNodeSet.contains(sym) ⇒
            List(Left(nodeNameToClassNo(sym)))
          case Right(l) =>
            // separate the terminals into words, and compare those inside smith waterman
            l.map(symb => Mining.splitToken(symb.toString.tail)).flatten.map(x => Left(Symbol(x)))
        } flatten
      }
    }
    else {
      l.toIndexedSeq
    }
  }

  def filterSExp(sexp: SExp): Boolean = {
    sexp.preOrder.length >= CombinedLawton.cutoffSize
  }

  override def filterParseTreeNode(n: ParseTreeNode) = filterSExp(n.toSExp)

  /*def selfSimilarityOfSeq(a: List[Symbol]) = {
    preprocess(a).map(classNoToWeights(_) * matchScore).sum
  }*/

  type SymbolOrIndexedSeqOfConsecutiveTerminals = Either[Symbol, IndexedSeq[Symbol]]

  def combineTerminals(l: List[SymbolOrIndexedSeqOfConsecutiveTerminals]): List[SymbolOrIndexedSeqOfConsecutiveTerminals] = 
    l.foldLeft(List[SymbolOrIndexedSeqOfConsecutiveTerminals]())((acc, x) => {
      acc match {
        case Nil => acc :+ x
        case _ => acc.last match {
          case Left(_) => acc :+ x
          case Right(s1) => x match {
            case Right(s2) => acc.init :+ Right(s1 ++ s2)
            case Left(_) => acc :+ x
          }
        }
      }
    })

  def listOfPreorderSExpsToListOfSymbolOrIndexedSeqOfConsecutiveTerminals(l: IndexedSeq[SExp]): IndexedSeq[SymbolOrIndexedSeqOfConsecutiveTerminals] = {
    // separate terminals from nonterminals
    l.foldLeft(IndexedSeq[SymbolOrIndexedSeqOfConsecutiveTerminals]())((acc, x) => {
      acc match {
        case IndexedSeq() => if (x.isLeaf) acc :+ Right(IndexedSeq(x.name)) else acc :+ Left(x.name)
        case _ => acc.last match {
          case Left(_) => acc :+ {if (x.isLeaf) Right(IndexedSeq(x.name)) else Left(x.name)}
          case Right(s) => if (x.isLeaf) acc.init :+ Right(s :+ x.name)
                           else acc :+ Left(x.name)
        }
      }
    })
  }

  // https://en.wikipedia.org/wiki/Smith%E2%80%93Waterman_algorithm
  def similarityOfSeqs(a: IndexedSeq[SExp], b: IndexedSeq[SExp]): Result[Int] = {
    if (cutoffEnabled) {
      if (a.length < CombinedLawton.cutoffSize || b.length < CombinedLawton.cutoffSize) {
        return Result(0, Set.empty)
      }
    }

    val a1: IndexedSeq[SymbolOrIndexedSeqOfConsecutiveTerminals] = preprocess(listOfPreorderSExpsToListOfSymbolOrIndexedSeqOfConsecutiveTerminals(a))
    val b1: IndexedSeq[SymbolOrIndexedSeqOfConsecutiveTerminals] = preprocess(listOfPreorderSExpsToListOfSymbolOrIndexedSeqOfConsecutiveTerminals(b))

    def lefts(x: IndexedSeq[SymbolOrIndexedSeqOfConsecutiveTerminals]) = x.collect({case Left(x) => x})
    def rights(x: IndexedSeq[SymbolOrIndexedSeqOfConsecutiveTerminals]) = x.collect({case Right(x) => x})
    //println(s"a1 lefts: ${lefts(a1)}\n\nb1 lefts: ${lefts(b1)}\n\na1 rights: ${rights(a1)}\n\nb1 rights: ${rights(b1)}")
    /*println("a: " + listOfPreorderSExpsToListOfSymbolOrIndexedSeqOfConsecutiveTerminals(a).filter(x => x match {
        case Left(s) => usefulNodeSet.contains(s)
        case Right(_) => true
      }).take(35).map(x => x match {
        case Left(s) => s.toString.tail
        case Right(l) => "{" + l.map("\"" + _.toString.tail + "\"").mkString(", ") + "}"
      }))
    println("b: " + listOfPreorderSExpsToListOfSymbolOrIndexedSeqOfConsecutiveTerminals(b).filter(x => x match {
        case Left(s) => usefulNodeSet.contains(s)
        case Right(_) => true
      }).take(35).map(x => x match {
        case Left(s) => s.toString.tail
        case Right(l) => "{" + l.map("\"" + _.toString.tail + "\"").mkString(", ") + "}"
      }))

    println("a1: " + (a1).take(35).map(x => x match {
        case Left(s) => s.toString.tail
        case Right(l) => "{" + l.map(s => Mining.splitToken(s.toString.tail)).flatten.toSet.map((s: String) => "\"" + s + "\"").mkString(", ") + "}"
      }))
    println("b1: " + (b1).take(35).map(x => x match {
        case Left(s) => s.toString.tail
        case Right(l) => "{" + l.map(s => Mining.splitToken(s.toString.tail)).flatten.toSet.map((s: String) => "\"" + s + "\"").mkString(", ") + "}"
      }))*/

    // println(nodeNameToClassNo)


    if (a1.length == 0 || b1.length == 0) return Result(0, Set.empty)

    val (m, n) = (a1.length-1, b1.length-1)

    var c1 = Array.ofDim[Double](n+1)
    var c2 = Array.ofDim[Double](n+1)
    var score: Double = 0

    for (j ← 0 to n)
      c1(j) = 0
    c2(0) = 0

    for (i <- 1 to m) {
      for (j <- 1 to n) {
        val comparisonScore = (a1(i), b1(j)) match {
          case (Left(x), Left(y)) => nodeSimilarity(x, y)
          case (Right(s1), Right(s2)) => 
            val cv1 = Mining.characteristicVectorFromSymbols(s1)
            val cv2 = Mining.characteristicVectorFromSymbols(s2)
            (1.0 - Mining.distance(cv1, cv2)) * matchScore * miningMultiplier
          case _ => mismatchScore * miningMultiplier
        }
        val matchOrMismatch = c1(j-1) + comparisonScore

        // var deletion = -1
        // for (k <- 1 to i)
        //   deletion = math.max(deletion, H(i-k)(j) + gapScore)
        // var insertion = -1
        // for (l <- 1 to j)
        //   insertion = math.max(insertion, H(i)(j-l) + gapScore)

        // optimization -- when gapScore is a constant, you don't have to loop
        // moreover, we can do the computation using only two arrays
        val deletion = c1(j) + gapScore
        val insertion = c2(j-1) + gapScore

        c2(j) = matchOrMismatch max deletion max insertion max 0
        score = score max c2(j)
      }
      // swap the vectors
      val tmp = c1
      c1 = c2
      c2 = tmp
    }

    val aScore1 = a1.collect({case Left(symb) => symb}).map(classNoToWeights(_) * matchScore).sum
    val aScore2 = a1.collect({case Right(s) => s}).map(_ => matchScore*miningMultiplier/*/2.0*/).sum
    val aScore = aScore1 + aScore2

    val bScore1 = b1.collect({case Left(symb) => symb}).map(classNoToWeights(_) * matchScore).sum
    val bScore2 = b1.collect({case Right(s) => s}).map(_ => matchScore*miningMultiplier/*/2.0*/).sum
    val bScore = bScore1 + bScore2

    val minScore = aScore min bScore
    val maxScore = aScore max bScore
    // println(s"score: $score, maxScore: $maxScore")
    // Thread.sleep(10 * 1000)

    score /= maxScore

    Result[Int](score, Set.empty)
  }

  def resolveLineNos(lineNos: IndexedSeq[LineNo])(i: Interval[Int]): Interval[LineNo] = {
    val slice = lineNos.slice(i.begin, i.end).filterNot(-1 == _)
    if (slice.isEmpty) {
      Interval(-1, -1)
    } else {
      Interval(slice.min, slice.max)
    }
  }

  override val collapseTrees = true

  def similarity(a: SExp, b: SExp) = {
    val (preOrderA, _lineNosA) = a.preOrderFunctionsWithLineNumbersSExp
    val (preOrderB, _lineNosB) = b.preOrderFunctionsWithLineNumbersSExp

    val result = similarityOfSeqs(preOrderA.flatten, preOrderB.flatten)
    val lineNosA = _lineNosA.flatten.toIndexedSeq
    val lineNosB = _lineNosB.flatten.toIndexedSeq
    result.convert(resolveLineNos(lineNosA), resolveLineNos(lineNosB))
  }

  def similarity(a: ParseTreeNode, b: ParseTreeNode): Result[Int] = {
    similarity(a.toSExp, b.toSExp)
  }
}
