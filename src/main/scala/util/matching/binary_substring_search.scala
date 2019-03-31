package fett.util.matching

import fett.scoring._

/**
  * A matching heuristic that matches two fingerprint lists
  * by covering the first list using substrings of the second. 
  * The covering algorithm greedily matches longest substrings
  * from current index then switches to next substring.
  */
object BinarySubstringSearch extends SequenceMatcher {
  /**
    * Find leftmost item on given slice in given sorted array of sequences that has `x` at `index`.
    * 
    * This method finds leftmost element of slice of `suffixesOfA` represented by
    * `low` and `high` that has `x` at `index`, i.e. if this method returns
    * `Some(i)` then `suffixesOfA(i)(index) == x`, `low <= i`, `high >= i` all hold.
    * Moreover, for all `low <= j < i`, `suffixesOfA(j)(index) != x`.
    * 
    * @param x value being searched
    * @param low lower bound of array slice
    * @param high upper bound of array slice
    * @param index index of the element of each sequence that search will go through
    * @param suffixesOfA the array which is being searched
    * 
    * @returns index of leftmost satisfying element. `None` if no satisfying element is found in the slice.
    */
  def findLeft[A <: Ordered[A]](x: A, low: Int, high: Int, index: Int, suffixesOfA: Array[IndexedSeq[A]]): Option[Int] = {
    val cursor = (low + high) / 2
    if (suffixesOfA(cursor).length <= index) {
      return None
    }
    val y = suffixesOfA(cursor)(index)
    if (low == high) {
      if (x == y) {
        return Some(cursor)
      }
      else {
        return None
      }
    }
    if (x == y) {
      if (low == cursor) {
        Some(cursor)
      } else {
        findLeft(x, low, cursor, index, suffixesOfA) orElse Some(cursor)
      }
    } else if (x < y) {
      findLeft(x, low, cursor, index, suffixesOfA)
    } else {
      findLeft(x, cursor+1, high, index, suffixesOfA)
    }
  }

  /**
    * Find rightmost item on given slice in given sorted array of sequences that has `x` at `index`.
    * 
    * This method finds rightmost element of slice of `suffixesOfA` represented by
    * `low` and `high` that has `x` at `index`, i.e. if this method returns
    * `Some(i)` then `suffixesOfA(i)(index) == x`, `low <= i`, `high >= i` all hold.
    * Moreover, for all `i < j <= high`, `suffixesOfA(j)(index) != x`.
    * 
    * @param x value being searched
    * @param low lower bound of array slice
    * @param high upper bound of array slice
    * @param index index of the element of each sequence that search will go through
    * @param suffixesOfA the array which is being searched
    * 
    * @returns index of rightmost satisfying element. `None` if no satisfying element is found in the slice.
    */
  def findRight[A <: Ordered[A]](x: A, low: Int, high: Int, index: Int, suffixesOfA: Array[IndexedSeq[A]]): Option[Int] = {
    val cursor = (low + high) / 2
    if (suffixesOfA(cursor).length <= index) {
      return None
    }
    val y = suffixesOfA(cursor)(index)
    if (low == high) {
      if (x == y) {
        return Some(cursor)
      }
      else {
        return None
      }
    }
    if (x == y) {
      if (high == cursor) {
        Some(cursor)
      } else {
        findRight(x, cursor+1, high, index, suffixesOfA) orElse Some(cursor)
      }
    } else if (x < y) {
      findRight(x, low, cursor, index, suffixesOfA)
    } else {
      findRight(x, cursor+1, high, index, suffixesOfA)
    }
  }

  /** Construct a suffix array and an array of positions of each suffix. */
  def suffixArray[A <: Ordered[A]](a: IndexedSeq[A])(implicit ev: Ordering[IndexedSeq[A]]) = {
    val suffixes = Array.fill[(IndexedSeq[A], Int)](a.length)(null)
    var current = a
    var i = 0
    while (current.nonEmpty) {
      suffixes(i) = (current, i)
      current = current.tail
      i += 1
    }
    // TODO: change this with a suffix sort algorithm or a suffix tree construction for performance
    scala.util.Sorting.quickSort(suffixes)
    suffixes.unzip
  }

  /**
    * Compute similarity of two fingerprint sequences by constructing
    * a suffix array for a then binary-searching for substrings of b in it.
    * 
    * current method of doing so has complexity O(m*(m+n)*log(m)+n*(m+n)*log(n)).
    * it can be improved by:
    *  - constructing suffix array by using a suffix sort or constructing a suffix tree
    *  - using a KMP-style jump table
    */
  def search[A <: Ordered[A]](a: IndexedSeq[A],
    b: IndexedSeq[A],
    fpLen: Int,
    minLength: Int
  )(implicit ev: Ordering[IndexedSeq[A]]): Set[Match[Int]] = {
    if (a.isEmpty) {
      return Set.empty
    }

    val (suffixesOfA, posA) = suffixArray(a)

    var pos = 0
    var substrings = List.empty
    var length = 0
    var left = 0
    var right = suffixesOfA.length - 1
    var matches = List.empty[Match[Int]]
    while (pos + length < b.length) {
      val current = b(pos + length)
      findLeft(current, left, right, length, suffixesOfA) match {
        case None ⇒ {
          // current substring is found, add it and increment position
          if (length >= minLength) {
            // compute length of original string from fingerprint length
            val l = length + fpLen - 1
            matches +:= Match(Interval(posA(left), posA(left) + l), Interval(pos, pos + l))
          }
          pos += 1
          length = 0
        }
        case Some(l) ⇒ {
          left = l
          right = findRight(current, left, right, length, suffixesOfA).get
          length += 1
        }
      }
    }
    matches.toSet
  }
}
