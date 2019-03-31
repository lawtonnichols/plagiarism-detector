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


object TreeAlign {
  val cutoffSize = 30

  type SimilarityScore = (Double, Double, Map[Symbol, Double]) ⇒ (Symbol, Symbol) ⇒ Double

  def fromOptions(options: Map[String, String]) = {
    TreeAlign(SmithWaterman.fromOptions(options))
  }
}

  

case class TreeAlign(sw: SmithWaterman) extends Scoring {
  def similarity(a: ParseTreeNode, b: ParseTreeNode): Result[LineNo] = {
    val a_tree = a.toTree
    val b_tree = b.toTree
    // println(sw.nodeNameToClassNo)
    Result(Tree.α(a_tree, b_tree), Set())
  }
}
