package fett.scoring

import fett.sexp._
import fett.util.logger
import fett.util.trees._
import fett.util.utils._
import fett.util.utils.Implicits._
import fett.util.hashConsing._

import scala.collection.mutable.{Map => MMap}
import scala.collection.concurrent.TrieMap

/**
  * A left-closed, right-open interval.
  */
case class Interval[A](val begin: A, val end: A)(implicit num: Numeric[A]) {
  assert(num.gteq(end, begin))
  def length: A = num.minus(end, begin)
  def overlaps(that: Interval[A]): Boolean = num.lteq(begin, that.end) || num.lteq(that.begin, end)
}

// invariant: `is` contains only non-overlapping intervals
case class MultiInterval[A](is: Set[Interval[A]] = Set[Interval[A]]())(implicit num: Numeric[A]) {
  import MultiInterval._

  def join(mi: MultiInterval[A]): MultiInterval[A] = {
    def adjoin(mi: MultiInterval[A], j: Interval[A]): MultiInterval[A] = {
      // go through `is` to find all the overlapping intervals
      val overlappingIntervals = is.filter(_ overlaps j)
      // combine the lads
      val intervalsToCombine = overlappingIntervals + j
      val (newBegin, newEnd) = ((j.begin, j.end) /: intervalsToCombine) ((acc, i) => {
          acc match {
            case (accMin, accMax) => (num.min(accMin, i.begin), num.max(accMax, i.end))
          }
        })
      // remove the old overlapping stuff, add in the new interval
      MultiInterval(is -- overlappingIntervals + Interval(newBegin, newEnd))
    }

    (this /: mi.is)(adjoin(_, _))
  }

  def length: A = is.map(_.length).sum
}

object MultiInterval {
  implicit def IntervalToMultiInterval[A](i: Interval[A])(implicit num: Numeric[A]): MultiInterval[A] = MultiInterval(Set(i))

  def make[A](is: collection.GenTraversableOnce[Interval[A]])(implicit num: Numeric[A]): MultiInterval[A] = {
    (MultiInterval() /: is)(_ join _)
  }
}

/**
  * A match between two strings/documents/etc.
  */
case class Match[A](val left: Interval[A], val right: Interval[A]) {
  def flip = Match(right, left)
}

case class Result[A](val score: Double, val matches: Set[Match[A]]) {
  def convert[B](fLeft: Interval[A] ⇒ Interval[B], fRight: Interval[A] ⇒ Interval[B]): Result[B] = {
    copy(matches=matches map {
      case Match(left, right) ⇒ Match(fLeft(left), fRight(right))
    })
  }
}

object Scoring {
  type LineNo = Int
}

trait Scoring {
  type LineNo = Scoring.LineNo
  def similarity(a: ParseTreeNode, b: ParseTreeNode): Result[LineNo]

  val collapseTrees = false

    /** Prune away any nodess that a scorer thinks will be too noisy */
  def filterParseTreeNode(n: ParseTreeNode): Boolean = true

  def importantNodeLabels: Set[String] = Set()

  def selfSimilarity(a: ParseTreeNode): Double = ???
}

trait SExpScoring extends Scoring {
  def similarity(a: ParseTreeNode, b: ParseTreeNode): Result[LineNo] = {
    similarity(a.toSExp, b.toSExp)
  }

  override val collapseTrees = true
  val usePreorder: Boolean = true

  def similarity(a: SExp, b: SExp): Result[LineNo]

  override def filterParseTreeNode(n: ParseTreeNode) = this match {
    case _: Zhang13SmithWaterman => filterSExp(n.toSExp)
    case otherwise => filterSExp(n.chop.toSExp)
  }

  /** Prune away any SExps that a scorer thinks will be too noisy */
  def filterSExp(s: SExp): Boolean = true

  override def selfSimilarity(a: ParseTreeNode): Double = selfSimilarity(a.toSExp)

  def selfSimilarity(a: SExp): Double
}

trait FlattenedSExpScoring[Scorer] extends SExpScoring {
  // use of F-bounded quantification to enforce correct types
  // since Map is invariant on its keys and we want to split
  // the hash tables to keep the collision rate small. Also,
  // this gives more precise type checking
  val memoTable: Option[TrieMap[(FlattenedSExpScoring[Scorer], HashedIndexedSeq[Symbol], HashedIndexedSeq[Symbol]), Result[Int]]] = None

  final def similarity(a: SExp, b: SExp) = 
    if (usePreorder) similarityPreorder(a, b) else similarityPostorder(a, b)

  def similarityPreorder(a: SExp, b: SExp) = {
    val (preOrderA, _lineNosA) = a.preOrderFunctionsWithLineNumbers
    val (preOrderB, _lineNosB) = b.preOrderFunctionsWithLineNumbers

    val result = similarityOfSeqs(preOrderA.flatten, preOrderB.flatten)
    val lineNosA = _lineNosA.flatten.toIndexedSeq
    val lineNosB = _lineNosB.flatten.toIndexedSeq
    result.convert(resolveLineNos(lineNosA), resolveLineNos(lineNosB))
  }

  def similarityPostorder(a: SExp, b: SExp) = {
    val (postOrderA, _lineNosA) = a.postOrderFunctionsWithLineNumbers
    val (postOrderB, _lineNosB) = b.postOrderFunctionsWithLineNumbers

    // println("================")
    // println(postOrderA.flatten)
    // println("================")
    // println(postOrderB.flatten)
    // println("================")

    val result = similarityOfSeqs(postOrderA.flatten, postOrderB.flatten)
    val lineNosA = _lineNosA.flatten.toIndexedSeq
    val lineNosB = _lineNosB.flatten.toIndexedSeq
    result.convert(resolveLineNos(lineNosA), resolveLineNos(lineNosB))
  }

  /**
    * Convert an interval of flattened sexp positions to an interval of line numbers.
    */
  private def resolveLineNos(lineNos: IndexedSeq[LineNo])(i: Interval[Int]): Interval[LineNo] = {
    val slice = lineNos.slice(i.begin, i.end).filterNot(-1 == _)
    if (slice.isEmpty) {
      Interval(-1, -1)
    } else {
      Interval(slice.min, slice.max)
    }
  }

  /** Compute similarity on flattened SExps */
  def similarityOfSeqs(a: IndexedSeq[Symbol], b: IndexedSeq[Symbol]): Result[Int]

  /** Compute similarity on flattened SExps */
  def similarityOfSeqs(a: HashedIndexedSeq[Symbol], b: HashedIndexedSeq[Symbol]): Result[Int] = memoTable match {
    case None ⇒
      similarityOfSeqs(a.seq, b.seq)
    case Some(memo) ⇒ {
      memo.getOrElseUpdate((this, a, b), similarityOfSeqs(a.seq, b.seq))
    }
  }

  def selfSimilarity(a: SExp): Double = selfSimilarityOfSeq(a.preOrderFunctionsWithLineNumbers._1.flatten)
  def selfSimilarityOfSeq(a: IndexedSeq[Symbol]): Double = ???
}
