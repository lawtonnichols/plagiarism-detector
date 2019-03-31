package fett.scoring

import fett.sexp._
import fett.util.logger

case class Winnowing2(
  k: Int = 5,
  w: Int = 4
) extends FlattenedSExpScoring[Winnowing2] { 

  val t = k + w - 1

  def kgrams[A](k: Int, s: Seq[A]): Stream[List[A]] = {
    val (hd, tl) = s.splitAt(k)
    if (tl.isEmpty) {
      Stream.empty
    } else {
      hd.toList #:: kgrams(k, s.tail)
    }
  }

  def rightmostMinimumAndPosition(l: List[Int]): (Int, Int) = {
    var min = l.head
    var pos = 0
    for ((x, idx) <- l.zipWithIndex.tail) {
      if (x <= min) { min = x; pos = idx } 
    }
    (min, pos)
  }

  def winnow(kgs_hashed: Seq[Int]): List[Int] = {
    if (kgs_hashed.length == 0) return List()

    val windows: Stream[List[Int]] = kgrams(w, kgs_hashed)
    val windows_with_mins_and_posns: List[(Int, Int)] = windows.toList map rightmostMinimumAndPosition

    import scala.collection.mutable.ListBuffer
    val winnowed_window = ListBuffer[Int]()
    // the head goes in no matter what
    winnowed_window += windows_with_mins_and_posns.head._1
    var lastMinPos = windows_with_mins_and_posns.head
    // for the rest of the elements, use fancy logic to decide whether to add it
    for ((min, pos) <- windows_with_mins_and_posns.tail) {
      val (lastMin, lastPos) = lastMinPos
      // this element gets added if the minimum is new
      var shouldAddThisElement = min < lastMin
      // or the minimum is the same but in a higher (more rightmost) position
      shouldAddThisElement ||= min == lastMin && pos > lastPos
      // or the old minimum has run off the edge (is negative)
      shouldAddThisElement ||= lastPos < 0

      if (shouldAddThisElement) {
        winnowed_window += min
        lastMinPos = (min, pos)
      } else {
        // keep the same min, but since the window moves right decrease the lastPos
        lastMinPos = (lastMin, lastPos - 1)
      }
    }

    winnowed_window.toList
  }

  def getFingerprints[A](s: List[A]): List[Int] = {
    val kgs: Stream[List[A]] = kgrams(k, s)
    val kgs_hashed: Stream[Int] = kgs map {_.hashCode}
    winnow(kgs_hashed)
  }

  def largestCommonSubstringsTotalLength(a: List[Int], b: List[Int]): Int = {
    val (matchScore, mismatchScore, gapScore) = (1, 0, 0)

    if (a.length == 0 || b.length == 0) return 0

    val a1 = a.toIndexedSeq
    val b1 = b.toIndexedSeq
    val (m, n) = (a1.length-1, b1.length-1)
    val H = Array.ofDim[Int](m+1, n+1)
    for (i <- 0 to m)
      H(i)(0) = 0
    for (j <- 0 to n)
      H(0)(j) = 0
    for (i <- 1 to m) {
      logger.debug(s"$i/$m")
      for (j <- 1 to n) {
        val matchOrMismatch = H(i-1)(j-1) + (if (a1(i) == b1(j)) matchScore else mismatchScore)
        
        val deletion = H(i-1)(j) + gapScore
        val insertion = H(i)(j-1) + gapScore

        H(i)(j) = List(0, matchOrMismatch, deletion, insertion).max
      }
    }

    H(m)(n)
  }


  def similarityOfSeqs(a: List[Symbol], b: List[Symbol]): Result[Int] = {
    val afs = getFingerprints(a)
    val bfs = getFingerprints(b)

    val numMatches = largestCommonSubstringsTotalLength(afs, bfs)
    val score = numMatches.doubleValue / math.max(math.min(afs.length, bfs.length), 1)
    Result(score, Set.empty)
  }

  def similarityOfSeqs(a: IndexedSeq[Symbol], b: IndexedSeq[Symbol]): Result[Int] = {
    similarityOfSeqs(a.toList, b.toList)
  }

}

object Winnowing2 {
  def test() {
    val testSeq = List(77, 74, 42, 17, 98, 50, 17, 98, 8, 88, 67, 39, 77, 74, 42, 17, 98)
    val w = Winnowing2()
    println(w.winnow(testSeq))
  }
}
