package fett.scoring

import fett.parsing._
import fett.util.trees._
import fett.util

import scala.collection.mutable.{Map ⇒ MMap}
import scala.collection.concurrent.TrieMap
import scala.language.postfixOps
import math.log

// Use bag-of-words based on identifiers from:
//
// Xiao Cheng, Lingxiao Jiang, Hao Zhong, Haibo Yu, and Jianjun
// Zhao. 2016. On the feasibility of detecting cross-platform code clones
// via identifier similarity. In Proceedings of the 5th International
// Workshop on Software Mining (SoftwareMining 2016). ACM, New York, NY,
// USA, 39-42. DOI: http://dx.doi.org/10.1145/2975961.2975967
object IdSimilarity extends Scoring {
  val distCache = TrieMap.empty[ParseTreeNode, Dist[String]]

  type Dist[A] = MMap[A, Double]

  def similarity(a: ParseTreeNode, b: ParseTreeNode): Result[LineNo] = {
    val p = tokenDistribution(a)
    val q = tokenDistribution(b)
    Result(distance(p, q), Set.empty)
  }

  val eps = math.ulp(0.0) // smallest positive double precision floating point number

  def distance[A](p: Dist[A], q: Dist[A]): Double = kl(p, q) + kl(q, p)

  @inline def log2(x: Double) = log(x) / log(2)

  def kl[A](p: Dist[A], q: Dist[A]) = {
    val q1 = q.withDefaultValue(eps)
    var sum: Double = 0
    for (i ← p.keys) {
      sum += p(i) * (log2(p(i)) - log2(q1(i)))
    }
    sum
  }

  // mean of two probability distributions
  def meanDist[A](p: Dist[A], q: Dist[A]): Dist[A] = {
    val m = MMap.empty[A, Double]
    val q1 = q.withDefaultValue(0)
    val p1 = p.withDefaultValue(0)
    for (k ← p.keys) {
      m(k) = (p(k) + q1(k)) / 2
    }
    for (k ← q.keys) {
      m(k) = (p1(k) + q(k)) / 2
    }
    m
  }

  // Jensen-Shannon divergence
  def js[A](p: Dist[A], q: Dist[A]) = {
    val m = meanDist(p, q)
    (kl(p, m) + kl(q, m)) / 2
  }

  def tokenDistribution(tree: ParseTreeNode): Dist[String] = distCache.getOrElseUpdate(tree, {
    val tokens = collectIdentifiers(tree).flatMap(_.leaves).filter((tok:String) ⇒ tok.exists(_.isLetter))
    val dist = MMap.empty[String, Double].withDefaultValue(0)
    var nToken = 0
    for {
      rawTok ← tokens
      tok ← splitToken(rawTok)
    } {
      dist(tok) = dist(tok) + 1
      nToken += 1
    }

    for (t ← dist.keys) {
      dist(t) /= nToken
    }

    dist
  })

  def splitToken(tok: String): List[String] = {
    split(tok, (c:Char) ⇒ !c.isLetterOrDigit).flatMap(splitCamelCase).filter(_.nonEmpty)
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
