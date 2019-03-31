package fett.util.matching

import fett.scoring.Match

trait SequenceMatcher {
  def search[A <: Ordered[A]](a: IndexedSeq[A],
    b: IndexedSeq[A],
    fpLen: Int,
    minLength: Int
  )(implicit ev: Ordering[IndexedSeq[A]]): Set[Match[Int]]
}
