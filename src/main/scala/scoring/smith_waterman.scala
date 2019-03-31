package fett.scoring

import fett.sexp._
import fett.util.logger
import fett.util.utils.Implicits._
import fett.util.hashConsing._

import scala.collection.mutable.{MutableList => MList, Set => MSet, Map => MMap}
import scala.collection.concurrent.TrieMap

import scala.language.postfixOps

import scala.util.parsing.json._
import java.nio.file.{Files, Paths}
import java.nio.charset.Charset

// https://rosettacode.org/wiki/Levenshtein_distance#Scala
import scala.math._
object Levenshtein {
  val memo = TrieMap[(String, String), Int]()

   def minimum(i1: Int, i2: Int, i3: Int)=min(min(i1, i2), i3)
   def distance(s1:String, s2:String): Int = {
     if (memo.contains((s1, s2))) return memo((s1, s2))

     val dist=Array.tabulate(s2.length+1, s1.length+1){(j,i)=>if(j==0) i else if (i==0) j else 0}
     
     for(j<-1 to s2.length; i<-1 to s1.length)
       dist(j)(i)=if(s2(j-1)==s1(i-1)) dist(j-1)(i-1)
       else minimum(dist(j-1)(i)+1, dist(j)(i-1)+1, dist(j-1)(i-1)+1)
     
     val d = dist(s2.length)(s1.length)
     memo((s1, s2)) = d
     memo((s2, s1)) = d
     d
   }
 
   def main(args: Array[String]): Unit = {
      printDistance("kitten", "sitting")
      printDistance("rosettacode", "raisethysword")
   }
 
   def printDistance(s1:String, s2:String)=println("%s -> %s : %d".format(s1, s2, distance(s1, s2)))
}

object SmithWaterman {
  val cutoffSize = 30

  type SimilarityScore = (Double, Double, Map[Symbol, Double]) ⇒ (Symbol, Symbol) ⇒ Double

  def fromOptions(options: Map[String, String]) = {
    val cutoffEnabled = options.get("cutoff").fold(true)(
      (s:String) ⇒ s.toLowerCase match {
        case "true" ⇒ true
        case "false" ⇒ false
        case _ ⇒ sys.error("Expected a Boolean for cutoff, got " + s)
      })
    val matchScore = options.get("matchScore").fold[Double](2)(_.toDouble)
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
    SmithWaterman(matchScore, mismatchScore, gapScore, similarityFn, classes, useless, weights, cutoffEnabled)
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
      // matchScore
      classNoToWeights.getOrElse(a, 1.0) * matchScore
    } else {
      // mismatchScore
      math.max(classNoToWeights.getOrElse(a, 1.0), classNoToWeights.getOrElse(b, 1.0)) * mismatchScore
    }
  }

}

case class SmithWaterman(matchScore: Double = 2,
                         mismatchScore: Double = -1,
                         gapScore: Double = -1,
                         similarityFn: SmithWaterman.SimilarityScore = SmithWaterman.naiveNodeNameSimilarity,
                         eqClasses: Option[Set[Set[Symbol]]] = None,
                         uselessNodeSet: Option[Set[Symbol]] = None,
                         weights: Option[Map[Set[Symbol], Double]] = None,
                         cutoffEnabled: Boolean = true
                         ) extends FlattenedSExpScoring[SmithWaterman] with SmartHash {

  val useClassBasedSimilarity = similarityFn == SmithWaterman.classBasedNodeNameSimilarity

  override val usePreorder = false

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

  override val collapseTrees = true

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

  def preprocess(l: IndexedSeq[Symbol]) = {
    if (useClassBasedSimilarity) {
      // println(l.filter(usefulNodeSet.contains(_)))
      l.toIndexedSeq collect {
        case sym if usefulNodeSet.contains(sym) ⇒
          nodeNameToClassNo(sym)
      }
    }
    else
      l.toIndexedSeq
  }

  def preprocess2(l: IndexedSeq[Symbol]) = {
    if (useClassBasedSimilarity) {
      // println(l.filter(usefulNodeSet.contains(_)))
      l.toIndexedSeq collect {
        case sym if usefulNodeSet.contains(sym) ⇒
          sym
      }
    }
    else
      l.toIndexedSeq
  }

  override def filterSExp(sexp: SExp): Boolean = {
    sexp.preOrder.length >= SmithWaterman.cutoffSize
  }

  override def selfSimilarityOfSeq(a: IndexedSeq[Symbol]) = {
    preprocess(a).map(classNoToWeights(_) * matchScore).sum
  }

  // https://en.wikipedia.org/wiki/Smith%E2%80%93Waterman_algorithm
  def similarityOfSeqs(a: IndexedSeq[Symbol], b: IndexedSeq[Symbol]): Result[Int] = {
    if (cutoffEnabled) {
      if (a.length < SmithWaterman.cutoffSize || b.length < SmithWaterman.cutoffSize) {
        return Result(0, Set.empty)
      }
    }

    val a1 = preprocess(a).toIndexedSeq
    val b1 = preprocess(b).toIndexedSeq

    // println("==================")
    // println(preprocess2(a))
    // println("==================")
    // println(preprocess2(b))
    // println("==================")

    // println(s"a1: ${preprocess2(a)}\nb1: ${preprocess2(b)}")

    if (a1.length == 0 || b1.length == 0) return Result(0, Set.empty)

    val (m, n) = (a1.length, b1.length)

    var c1 = Array.ofDim[Double](n+1)
    var c2 = Array.ofDim[Double](n+1)
    var score: Double = 0

    for (j ← 0 to n)
      c1(j) = 0
    c2(0) = 0

    for (i <- 1 to m) {
      for (j <- 1 to n) {
        val comparisonScore = nodeSimilarity(a1(i-1), b1(j-1))
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

    val aScore = a1.map(classNoToWeights(_) * matchScore).sum
    val bScore = b1.map(classNoToWeights(_) * matchScore).sum
    val minScore = aScore min bScore
    val maxScore = aScore max bScore

    score /= maxScore

    Result[Int](score, Set.empty)
  }
}
