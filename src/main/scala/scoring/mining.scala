package fett.scoring

import fett.parsing._
import fett.util.trees._
import fett.util

import scala.collection.mutable.{Map ⇒ MMap}
import scala.collection.concurrent.TrieMap
import scala.language.postfixOps
import math.abs

// Use bag-of-words based on identifiers from:
//
// Xiao Cheng et al., Mining Revision Histories to Detect Cross-Language Clones without Intermediates
object Mining extends Scoring {
  val vecCache = TrieMap.empty[ParseTreeNode, CharVec[String]]
  val vecCache2 = TrieMap.empty[IndexedSeq[Symbol], CharVec[String]]

  type CharVec[A] = MMap[A, Double]

  def similarity(a: ParseTreeNode, b: ParseTreeNode): Result[LineNo] = {
    val p = characteristicVector(a)
    val q = characteristicVector(b)
    Result(distance(p, q), Set.empty)
  }

  /** distance between two charactristic vectors as defined by the paper:
    * D(v, w) = norm(v - w, 1) / norm(v + w, 1)
    */
  def distance[A](v: CharVec[A], w: CharVec[A]) = {
    val sum: Double = v.values.sum + w.values.sum
    val v1 = v.withDefaultValue(0)
    val w1 = w.withDefaultValue(0)

    var diff: Double = 0.0
    for (i ← (v.keySet ++ w.keySet)) {
      diff += abs(v(i) - w(i))
    }

    diff / sum
  }

  def characteristicVector(tree: ParseTreeNode): CharVec[String] = vecCache.getOrElseUpdate(tree, {
    val tokens = tree.leaves.filter((tok:String) ⇒ tok.exists(_.isLetter))
    val dist = MMap.empty[String, Double].withDefaultValue(0)
    for {
      rawTok ← tokens
      tok ← splitToken(rawTok)
    } {
      dist(tok) = dist(tok) + 1
    }

    dist
  })

  def characteristicVectorFromSymbols(tree: IndexedSeq[Symbol]): CharVec[String] = vecCache2.getOrElseUpdate(tree, {
    val stringlist = tree.map(_.toString.tail)
    val tokens = stringlist.filter((tok:String) ⇒ tok.exists(_.isLetter))
    val dist = MMap.empty[String, Double].withDefaultValue(0)
    for {
      rawTok ← tokens
      tok ← splitToken(rawTok)
    } {
      dist(tok) = dist(tok) + 1
    }

    dist
  })

  def splitToken(tok: String): List[String] = {
    split(tok, (c:Char) ⇒ !c.isLetter).flatMap(splitCamelCase).filter(_.nonEmpty)
  }

  def split(s: String, isSeparator: Char ⇒ Boolean): List[String] = {
    var t = s.dropWhile(isSeparator)
    var segments = List.empty[String]
    while (t.nonEmpty) {
      val segment = t.takeWhile(! isSeparator(_))
      t = t.drop(segment.length).dropWhile(isSeparator)
      segments = segment :: segments
    }
    segments.reverse
  }

  def splitCamelCase(s: String): List[String] = {
    val head = s.takeWhile((x:Char) ⇒ !(x.isUpper)) // don't split on numbers
    if (head.length == s.length) {
      return List(s)
    }
    val tail = s(head.length).toLower +: s.drop(head.length+1)
    head :: splitCamelCase(tail)
  }

  def collectIdentifiers(tree: ParseTreeNode): List[ParseTreeNode] = {
    // written in for-style to make it more readable
    for {
      name ← util.identifierNodeNames.toList
      node ← tree.collectNodesWithLabel(name) if node.children.forall(_.label contains "TerminalNode")
    } yield {
      node
    }
  }
}
