package fett.scoring

import fett.sexp._
import fett.util.logger

case class NeedlemanWunsch(
  matchScore: (Symbol, Symbol) => Int = (a, b) => if (a == b) 1 else -1, // functional "similarity matrix"
  gapPenalty: Int = -1) extends FlattenedSExpScoring[NeedlemanWunsch] {

  // https://en.wikipedia.org/wiki/Needleman%E2%80%93Wunsch_algorithm#Advanced_presentation_of_algorithm
  def similarityOfSeqs(a: List[Symbol], b: List[Symbol]): Result[Int] = {
    val a1 = a.toIndexedSeq
    val b1 = b.toIndexedSeq
    val (m, n) = (a1.length-1, b1.length-1)
    val F = Array.ofDim[Int](m+1, n+1)
    for (i <- 0 to m)
      F(i)(0) = gapPenalty * i
    for (j <- 0 to n)
      F(0)(j) = gapPenalty * j
    for (i <- 1 to m) {
      logger.debug(s"$i/$m")
      for (j <- 1 to n) {
        val matchOrMismatch = F(i-1)(j-1) + matchScore(a1(i), b1(j))
        val deletion = F(i-1)(j) + gapPenalty
        val insertion = F(i)(j-1) + gapPenalty
        F(i)(j) = List(matchOrMismatch, deletion, insertion).max
      }
    }

    val score = F(m)(n).doubleValue / math.min(m, n)

    Result(score, Set.empty)
  }

  def similarityOfSeqs(a: IndexedSeq[Symbol], b: IndexedSeq[Symbol]): Result[Int] = {
    similarityOfSeqs(a.toList, b.toList)
  }
}
