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

object Zhang13SmithWaterman {
  type SimilarityScore = (Double, Double, Map[Symbol, Double]) ⇒ (Symbol, Symbol) ⇒ Double

  def fromOptions(options: Map[String, String]) = {

    val matchScore = options.get("matchScore").fold[Double](2)(_.toDouble)
    val mismatchScore = options.get("mismatchScore").fold[Double](-1)(_.toDouble)
    val gapScore = options.get("gapScore").fold[Double](-1)(_.toDouble)

    Zhang13SmithWaterman(matchScore, mismatchScore, gapScore)
  }

}

case class Zhang13SmithWaterman(matchScore: Double = 1,
                         mismatchScore: Double = -1,
                         gapScore: Double = -1
                         ) extends FlattenedSExpScoring[Zhang13SmithWaterman] with SmartHash {

  override val collapseTrees = false
  override val usePreorder = true


  // https://en.wikipedia.org/wiki/Smith%E2%80%93Waterman_algorithm
  def similarityOfSeqs(a: IndexedSeq[Symbol], b: IndexedSeq[Symbol]): Result[Int] = {
    // the paper explicitly says they compare "characters", so a and b need
    // to be converted to strings
    // val a1: String = a.map(_.toString.tail).foldLeft("")(_ + _)
    // val b1: String = b.map(_.toString.tail).foldLeft("")(_ + _)

    val a1 = a
    val b1 = b

    // println(s"a1: ${preprocess2(a)}\nb1: ${preprocess2(b)}")

    if (a1.length == 0 || b1.length == 0) return Result(0, Set.empty)

    val (m, n) = (a1.length, b1.length)

    var c1 = Array.ofDim[Double](n+1)
    var c2 = Array.ofDim[Double](n+1)
    var lengthOfMatch1 = Array.ofDim[Int](n+1)
    var lengthOfMatch2 = Array.ofDim[Int](n+1)
    var score: Double = 0
    var lengthOfBestMatch = 0

    for (j ← 0 to n) {
      c1(j) = 0
      lengthOfMatch1(j) = 0
    }
    c2(0) = 0
    lengthOfMatch2(0) = 0

    for (i <- 1 to m) {
      for (j <- 1 to n) {
        val comparisonScore = if (a1(i-1) == b1(j-1)) 1 else -1
        val matchOrMismatch = c1(j-1) + comparisonScore
        val deletion = c1(j) + gapScore
        val insertion = c2(j-1) + gapScore

        // c2(j) = matchOrMismatch max deletion max insertion max 0
        // score = score max c2(j)

        var lengthOfThisMatch = 0
        c2(j) = 0
        if (matchOrMismatch > c2(j)) {
          c2(j) = matchOrMismatch
          lengthOfThisMatch = lengthOfMatch1(j-1) + (if (a1(i-1) == b1(j-1)) 1 else 0)
        }
        if (deletion > c2(j)) {
          c2(j) = deletion
          lengthOfThisMatch = lengthOfMatch1(j)
        }
        if (insertion > c2(j)) {
          c2(j) = insertion
          lengthOfThisMatch = lengthOfMatch2(j-1)
        }
        lengthOfMatch2(j) = lengthOfThisMatch

        if (c2(j) > score) {
          score = c2(j)
          lengthOfBestMatch = lengthOfThisMatch
        }



        // if (c2(j) > lengthOfMatch2(j)) {
        //   println("c1(j-1): " + (c1(j-1)))
        //   println("c1(j): " + (c1(j)))
        //   println("c2(j-1): " + (c2(j-1)))
        //   println("lengthOfMatch1(j-1): " + (lengthOfMatch1(j-1)))
        //   println("lengthOfMatch1(j): " + (lengthOfMatch1(j)))
        //   println("lengthOfMatch2(j-1): " + (lengthOfMatch2(j-1)))
        //   println("a1(i-1) == b1(j-1): " + (a1(i-1) == b1(j-1)))

        //   sys.exit(1)
        // }



      }
      // swap the vectors
      val tmp = c1
      c1 = c2
      c2 = tmp
      val tmp2 = lengthOfMatch1
      lengthOfMatch1 = lengthOfMatch2
      lengthOfMatch2 = tmp2
    }

    // println("score: " + score)
    // println("length of match: " + lengthOfBestMatch)

    val denominator = a1.length + b1.length

    score /= denominator

    Result[Int](2.0*lengthOfBestMatch / denominator, Set.empty)
  }
}
