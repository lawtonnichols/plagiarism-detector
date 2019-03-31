package fett.scoring

import fett.sexp._
import fett.util.logger
import scala.annotation.tailrec
import scala.language.postfixOps
import fett.util.matching._
import fett.util.utils._

/**
  * Fingerprints for winnowing.
  * 
  * Fingerprints are not case classes so that they can be compared
  * like hashes and are position independent when being compared.
  * 
  * @param hash hash value of a fingerprint.
  * @param pos position of fingerprint in the document
  */
final class Fingerprint(val hash: Int, val pos: Int) extends Ordered[Fingerprint] {
  @inline def compare(that: Fingerprint): Int = {
    // do actual comparison instead of `this.hash - that.hash` since that causes
    // integer overflow
    if (this.hash > that.hash) {
      1
    } else if (this.hash == that.hash) {
      0
    } else {
      -1
    }
  }

  override def hashCode = hash

  @inline override def equals(arg0: Any) = arg0 match {
    case that:Fingerprint ⇒ this.hash == that.hash
    case _ ⇒ false
  }

  override def toString: String = s"Fingerprint($hash, $pos)"
}

object Fingerprint {
  def apply(hash: Int, pos: Int): Fingerprint = new Fingerprint(hash, pos)
  def unapply(arg0: Any): Option[(Int, Int)] = arg0 match {
    case f:Fingerprint ⇒ Some(f.hash → f.pos)
    case _ ⇒ None
  }

  implicit object FingerprintListOrdering extends Ordering[IndexedSeq[Fingerprint]] {
    @tailrec def compare(a: IndexedSeq[Fingerprint], b: IndexedSeq[Fingerprint]): Int = {
      if (a.isEmpty) {
        if (b.isEmpty) {
          return 0
        }
        return -1
      }
      if (b.isEmpty) {
        return 1
      }
      a.head compare b.head match {
        case 0 ⇒ compare(a.tail, b.tail)
        case x ⇒ x
      }
    }
  }
}

import Fingerprint.FingerprintListOrdering

/**
  * Winnowing algorithm to measure similarity by comparing document
  * fingerprints.
  * 
  * From: "Winnowing: Local Algorithms for Document Fingerprinting"
  * by Schleimer et al.
  * 
  * @param k noise threshold. Algorithm doesn't detect matches shorter
  * than `k`.
  * @param w window size. Algorithm is guaranteed to detect matches
  * longer than `k+w`.
  */
case class Winnowing(
  k: Int,
  w: Int,
  matcher: SequenceMatcher
) extends FlattenedSExpScoring[Winnowing] {

  /** Guarantee threshold. Algorithm guarantees matching substrings
    * at least as long as this threshold.
    */
  val t = k + w - 1

  def similarityOfSeqs(a: List[Symbol], b: List[Symbol]): Result[Int] = {
    logger.debug("A:")
    val fPrintsA = winnow(a, (x:List[Symbol]) ⇒ x.hashCode)
    logger.debug("B:")
    val fPrintsB = winnow(b, (x:List[Symbol]) ⇒ x.hashCode)

    val result = similarityScore(a, b, fPrintsA, fPrintsB)
    logger.debug(s"result: $result")
    result
  }

  def similarityOfSeqs(a: IndexedSeq[Symbol], b: IndexedSeq[Symbol]): Result[Int] = {
    similarityOfSeqs(a.toList, b.toList)
  }

  def similarityScore(
    origA: List[Symbol], origB: List[Symbol],
    a: IndexedSeq[Fingerprint], b: IndexedSeq[Fingerprint]): Result[Int] = {
    // length of a chunk that fingerprint represents
    val fpLen = w + k - 1
    val matchesA = matcher.search(a, b, fpLen, 1)
    // val matchLenA = (0 /: matchesA) { (acc, m) ⇒ acc + m.right.length }
    val matchLenA = MultiInterval.make(matchesA.map(_.right)).length
    val matchesB = matcher.search(b, a, fpLen, 1)
    // val matchLenB = (0 /: matchesB) { (acc, m) ⇒ acc + m.right.length }
    val matchLenB = MultiInterval.make(matchesB.map(_.right)).length

    val score = (matchLenA max matchLenB).doubleValue / math.min(origA.length, origB.length)
    Result(score, matchesA ++ matchesB.map(_.flip))
  }

  /** compute k-grams of a list lazily */
  def kgrams[A](s: List[A]): Stream[List[A]] = {
    val (hd, tl) = s.splitAt(k)
    if (tl.isEmpty) {
      Stream.empty
    } else {
      hd #:: kgrams(s.tail)
    }
  }

  /** global position of a minimum k-gram in the file */
  @inline private def globalPos(min: Int, r: Int, w: Int, pos: Int) = {
    (min - r + w - 1) % w + pos
  }

  /** Winnow a string with given hash function for k-grams */
  def winnow[A](s: List[A], hash: List[A] ⇒ Int): IndexedSeq[Fingerprint] = {
    // circular hash buffer to prevent copying for each window
    val h = Array.fill(w)(Int.MaxValue)

    var r = w - 1 // right end of window
    var min = w - 1 // index of minimum hash
    var pos = -(w - 1) // position of left end of window in document
    var fPrints = List.empty[Fingerprint]

    // process the up to first window without recording to fill the buffer
    val (hd, tl) = kgrams(s).splitAt(w-1)
    for (x ← hd) {
      r = (r + 1) % w
      h(r) = hash(x)
      logger.debug(s"${pos+w-1} $x ${h(r)} ${globalPos(r, r, w, pos)}")
      if (h(r) <= h(min)) {
        min = r
      }
      pos += 1
    }

    // start processing all the windows with recording
    for (x ← tl) {
      r = (r + 1) % w // shift window by one
      h(r) = hash(x)
      logger.debug(s"${pos+w-1} $x ${h(r)} ${globalPos(r, r, w, pos)}")
      if (min == r) {
        // if old minimum is no longer in the window, scan h to find a new rightmost minimum
        var i = (r + w - 1) % w
        while (i != r) {
          if (h(i) < h(min))
            min = i
          i = (i - 1 + w) % w
        }
        logger.debug(s"r: $r min: $min")
        logger.debug(s"minimum: ${h(min)} at ${globalPos(min, r, w, pos)} after throwing old minimum")
        fPrints +:= Fingerprint(h(min), globalPos(min, r, w, pos))
      } else {
        // check if the new hash is the new rightmost minimum, if so record the new minimum
        if (h(r) <= h(min)) {
          min = r
          logger.debug(s"minimum: ${h(min)} at ${globalPos(min, r, w, pos)}")
          fPrints +:= Fingerprint(h(min), globalPos(min, r, w, pos))
        }
      }
      pos += 1
    }
    fPrints.reverse.toIndexedSeq
  }
}

object Winnowing {
  def fromOptions(options: Map[String, String]): Winnowing = {
    val w = options.get("w").fold(10)(_.toInt)
    val k = options.get("k").fold(3)(_.toInt)
    val matcher = options.get("matcher").fold[SequenceMatcher](BinarySubstringSearch)({
      case "BinarySubstringSearch" ⇒ BinarySubstringSearch
      case "LongestCommonSubstring" ⇒ LongestCommonSubstring
      case matcher ⇒ sys.error(s"Unknown matching algorithm: $matcher")
    })
    Winnowing(w, k, matcher)
  }
}
