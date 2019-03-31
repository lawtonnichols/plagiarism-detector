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

import fett.util.trees._


object CombinedMin {
  val cutoffSize = 30

  type SimilarityScore = (Double, Double, Map[Symbol, Double]) ⇒ (Symbol, Symbol) ⇒ Double

  def fromOptions(options: Map[String, String]) = {
    CombinedMin(SmithWaterman.fromOptions(options))
  }
}

  

case class CombinedMin(sw: SmithWaterman) extends Scoring {
  def similarity(a: ParseTreeNode, b: ParseTreeNode): Result[LineNo] = {
    val r1 = sw.similarity(a, b)
    val r2 = Mining.similarity(a, b)
    Result(math.min(r1.score, 1.0 - r2.score), r1.matches ++ r2.matches)
  }
}
