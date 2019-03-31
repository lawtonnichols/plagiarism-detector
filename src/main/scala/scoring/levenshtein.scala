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

object LevenshteinScoring {

  type SimilarityScore = (Int, Int, Map[Symbol, Double]) ⇒ (Symbol, Symbol) ⇒ Double

  def fromOptions(options: Map[String, String]) = {
    val eqClasses = options.get("classFile") map {
      f ⇒
      val contents = new String(Files.readAllBytes(Paths.get(f)), Charset.forName("UTF-8"))
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
    LevenshteinScoring(classes, useless, weights)
  }
}

/** Scoring based on Levenshtein distance between two sequences abstracted by equivalence classes. */
case class LevenshteinScoring(
  eqClasses: Option[Set[Set[Symbol]]] = None,
  uselessNodeSet: Option[Set[Symbol]] = None,
  weights: Option[Map[Set[Symbol], Double]] = None) extends FlattenedSExpScoring[SmithWaterman] with SmartHash {
  val cutoffSize = 40

  val useClassBasedSimilarity = List[Option[_]](eqClasses, uselessNodeSet, weights).forall(_.nonEmpty)

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

  val usefulNodeSet = eqClasses_.flatten

  val classNoToWeights: Map[Symbol, Double] = (for {
      (set, weight) <- weights.getOrElse(Map()).toList
      nodeName <- set
      classNo = nodeNameToClassNo(nodeName)
    } yield (classNo -> weight)
  ).toMap.withDefaultValue(1.0)

  // def removeConsecutiveDuplicates[A](l: List[A], ifTheyreInThisSet: Set[A]): List[A] = l match {
  //   case Nil => l
  //   case x :: Nil => l
  //   case x :: y :: ys if x == y && ifTheyreInThisSet.contains(x) => removeConsecutiveDuplicates(y :: ys, ifTheyreInThisSet)
  // }

  def preprocess(l: IndexedSeq[Symbol]) = {
    if (useClassBasedSimilarity)
      l.toIndexedSeq collect {
        case sym if usefulNodeSet.contains(sym) ⇒
          Symbol(nodeNameToClassNo(sym).toString)
      }
    else
      l.toIndexedSeq
  }

  override def filterSExp(sexp: SExp): Boolean = {
    preprocess(sexp.preOrder).length >= cutoffSize
  }

  // https://en.wikipedia.org/wiki/Smith%E2%80%93Waterman_algorithm
  def similarityOfSeqs(a: IndexedSeq[Symbol], b: IndexedSeq[Symbol]): Result[Int] = {
    val a1 = preprocess(a).toIndexedSeq
    val b1 = preprocess(b).toIndexedSeq
    val m = a1.size
    val n = b1.size

    val smallestLength = math.max(math.min(m, n), 1)
    val largestLength = math.max(math.max(m, n), 1)
    
    val score = distance(a1, b1).doubleValue / largestLength

    Result[Int](score, Set())
  }

  def minimum(i1: Int, i2: Int, i3: Int)=min(min(i1, i2), i3)

  def distance[A](s1:IndexedSeq[A], s2:IndexedSeq[A]): Int = {
    val dist=Array.tabulate(s2.size+1, s1.size+1){(j,i)=>if(j==0) i else if (i==0) j else 0}
    
    for(j<-1 to s2.length; i<-1 to s1.length)
      dist(j)(i)=if(s2(j-1)==s1(i-1)) dist(j-1)(i-1)
      else minimum(dist(j-1)(i)+1, dist(j)(i-1)+1, dist(j-1)(i-1)+1)
    
    dist(s2.length)(s1.length)
  }
}
