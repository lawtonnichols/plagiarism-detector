package fett.util.matching

import fett.scoring._

/**
  * A matching heuristic that matches only the longest common substrings.
  */
object LongestCommonSubstring extends SequenceMatcher {
  /**
    * Search the longest common substring. Return nothing if
    * longest common substring is shorter than `minLength`.
    */
  def search[A <: Ordered[A]](a: IndexedSeq[A],
    b: IndexedSeq[A],
    fpLen: Int,
    minLength: Int
  )(implicit ev: Ordering[IndexedSeq[A]]): Set[Match[Int]] = {
    if (a.isEmpty || b.isEmpty) {
      return Set.empty
    }
    val l = Array.ofDim[Int](a.length, b.length)
    var longestMatch = Option.empty[Match[Int]]
    var maxLen = 0
    for (
      i ← 0 to (a.length - 1);
      j ← 0 to (b.length - 1)
    ) {
      if (a(i) == b(j)) {
        if (i == 0 || j == 0) {
          l(i)(j) = 1
        } else {
          l(i)(j) = l(i-1)(j-1) + 1
        }
        if (l(i)(j) > maxLen) {
          maxLen = l(i)(j)
          longestMatch = Some(Match(Interval(i - maxLen, i), Interval(j - maxLen, j)))
        }
      } else {
        l(i)(j) = 0
      }
    }

    if (maxLen >= minLength) {
      longestMatch.toSet
    } else {
      Set.empty
    }
  }
}
