package fett.scoring

import fett.sexp._
import scala.collection.mutable.{Set => MSet, Map => MMap}
import fett.util.logger

// from "JPlag: Finding plagiarisms among a set of programs"
// http://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.34.3508&rep=rep1&type=pdf
// and "An AST-based Code Plagiarism Detection Algorithm"
// http://ieeexplore.ieee.org/document/7424821/
case class GreedyStringTiling(
  isAMatch: (Symbol, Symbol) => Boolean = (a, b) => a == b, // exact match
  minimumMatchLength: Int = 15) extends FlattenedSExpScoring[GreedyStringTiling] {

  def similarityOfSeqs(a: List[Symbol], b: List[Symbol]): Result[Int] = {
    Result(sim(a, b), Set.empty)
  }

  def similarityOfSeqs(a: IndexedSeq[Symbol], b: IndexedSeq[Symbol]): Result[Int] = {
    similarityOfSeqs(a.toList, b.toList)
  }

  type Location = Int
  type Length = Int
  type Matches = MSet[(Location, Location, Length)]

  def greedyStringTiling(a: List[Symbol], b: List[Symbol]): Matches = {
    val A = a.toIndexedSeq
    val B = b.toIndexedSeq
    val tiles = MSet[(Location, Location, Length)]()
    var maxmatch = minimumMatchLength
    val markedA = MSet[Int]()
    val markedB = MSet[Int]()

    def isUnmarkedInA(i: Int): Boolean = !markedA.contains(i)
    def isUnmarkedInB(i: Int): Boolean = !markedB.contains(i)
    def markInA(i: Int) { markedA += i }
    def markInB(i: Int) { markedB += i }
    // TODO: use an interval tree to make this function faster
    def doesntOverlap(a: Location, b: Location, j: Length): Boolean = {
      for (x <- 0 to j-1)
        if (markedA.contains(a+x)) return false
      for (x <- 0 to j-1)
        if (markedB.contains(b+x)) return false
      true
    }

    do {
      maxmatch = minimumMatchLength
      val matches = MSet[(Location, Location, Length)]()

      (0 to A.length - 1).filter(isUnmarkedInA(_)).foreach(a => {
        logger.debug(s"a: $a / ${A.length}")
        logger.debug(s"maxmatch: $maxmatch")
        (0 to B.length - 1).filter(isUnmarkedInB(_)).foreach(b => {
          var j = 0
          while (a+j < A.length && 
                 b+j < B.length && 
                 isAMatch(A(a+j), B(b+j)) &&
                 isUnmarkedInA(a+j) &&
                 isUnmarkedInB(b+j)) 
            j += 1
          if (j == maxmatch && doesntOverlap(a, b, j)) 
            matches += ((a, b, j))
          else if (j > maxmatch) {
            matches.clear()
            matches += ((a, b, j))
            maxmatch = j
          }
        })
      })

      matches.foreach({case (a, b, maxmatch) => {
        (0 to maxmatch - 1).foreach(j => {
          markInA(a+j)
          markInB(b+j)
        })
        tiles += ((a, b, maxmatch))
      }})
    } while (maxmatch > minimumMatchLength)

    tiles
  }

  def coverage(tiles: Matches): Int = {
    tiles.map({case (a, b, length) => length}).sum
  }

  def sim(A: List[Symbol], B: List[Symbol]): Double = {
    val tiles = greedyStringTiling(A, B)
    val res = 2.0 * coverage(tiles)
    logger.debug("A.length + B.length: " + (A.length + B.length))
    logger.debug("Real-valued similarity score: " + (res.toDouble / (A.length + B.length)))
    res / (A.length + B.length)
  }
}
