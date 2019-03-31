package fett.sexp

import scala.language.implicitConversions
import scala.collection.mutable.{Map => MMap, Queue, MutableList}
import fett.util.utils.Implicits._
import fett.util.utils._

// An s-expression that always has a name
case class SExp(name: Symbol, elems: List[SExp], start: Loc = Loc(-1, -1), end: Loc = Loc(-1, -1), isLeaf: Boolean = false) {
  import SExp._

  override def toString(): String = elems match {
    case Nil => if (start.lineno == -1) s"(${name.name})" else s"(${name.name}:${start.lineno})"
    case nonemptylist => 
      val rest = nonemptylist.mkString(" ")
      if (start.lineno == -1) s"($name $rest)" else s"($name:${start.lineno} $rest)"
  }

  def toDotHelper(index: Int): (Int, Seq[((Int, String), (Int, String))]) = {
    val head = (index, name.toString)
    var tails = Seq[(Int, String)]()
    var rest = Seq[((Int, String), (Int, String))]()

    var curIndex = index + 1
    for (elem <- elems) {
      tails = tails :+ (curIndex, elem.name.toString)
      val (i, ss) = elem.toDotHelper(curIndex)
      curIndex = i + 1
      rest = rest ++ ss
    }

    val arrows = tails.map((head, _))
    
    (curIndex, arrows ++ rest)
  }

  def toDot: String = {
    val arrows = this.toDotHelper(0)._2
    val nodes = arrows.flatMap({ case ((i, label1), (j, label2)) => Seq((i, label1), (j, label2)) }).toSet
    val innerNodes = nodes.map({ case (i, label) => val label2 = label.replace("\"","\\\""); s"""node${i} [label="${label2}"];""" }).mkString("\n")
    val innerArrows = arrows.map({ case ((i, label1), (j, label2)) => s"""node${i}  -> node${j};""" }).mkString("\n")
    s"""digraph graphy_mc_graphface {\ngraph [ordering="out"];\n$innerNodes\n$innerArrows\n}"""
  }

  def elemsIndexedSeq: IndexedSeq[SExp] = elems.toIndexedSeq

  def preOrder: IndexedSeq[Symbol] = {
    name +: elemsIndexedSeq.map(_.preOrder).foldLeft(IndexedSeq.empty[Symbol])((acc, x) => acc ++ x)
  }

  def preOrderSExp: IndexedSeq[SExp] = {
    this +: elemsIndexedSeq.map(_.preOrderSExp).foldLeft(IndexedSeq.empty[SExp])((acc, x) => acc ++ x)
  }

  def preOrderLineNumbers: IndexedSeq[Int] = {
    val nameline = start.lineno
    nameline +: elemsIndexedSeq.map(_.preOrderLineNumbers).foldLeft(IndexedSeq.empty[Int])((acc, x) => acc ++ x)
  }

  def postOrder: IndexedSeq[Symbol] = {
    elemsIndexedSeq.map(_.postOrder).flatten :+ name
  }

  def postOrderSExp: IndexedSeq[SExp] = {
    elemsIndexedSeq.map(_.postOrderSExp).flatten :+ this
  }

  def postOrderLineNumbers: IndexedSeq[Int] = {
    val nameline = start.lineno
    elemsIndexedSeq.map(_.postOrderLineNumbers).flatten :+ nameline
  }


  def getFunctionSExps: IndexedSeq[SExp] = {
    val functionNodeLabels = Set(
      Symbol("antlrparsers.cpp14.CPP14Parser$FunctiondefinitionContext"),
      Symbol("antlrparsers.ecmascript.ECMAScriptParser$FunctionDeclarationContext"),
      Symbol("antlrparsers.ecmascript.ECMAScriptParser$FunctionExpressionContext"),
      Symbol("antlrparsers.golang.GolangParser$FunctionContext"),
      Symbol("antlrparsers.java.JavaParser$MethodDeclarationContext"),
      Symbol("antlrparsers.java.JavaParser$GenericMethodDeclarationContext"),
      Symbol("antlrparsers.java.JavaParser$ConstructorDeclarationContext"),
      Symbol("antlrparsers.java.JavaParser$GenericConstructorDeclarationContext")
    )

    val worklist = Queue[SExp](this)
    val res = MutableList[SExp]()

    while (worklist.nonEmpty) {
      val s = worklist.dequeue()

      if (functionNodeLabels contains s.name) {
        // this is a function node; don't search any deeper
        res += s
      } else {
        // otherwise search the children
        worklist ++= s.elems
      }
    }

    // get functions of 15 lines or less -- it makes more sense to just return everything  
    // and create a cutoff value in terms of the size of the pruned preorder list, though
    // res.toList.filter(sexp => math.abs(sexp.end.lineno - sexp.start.lineno) >= 15) 

    res.toIndexedSeq
  }

  // only do a preorder traversal of functions, and separate the results
  lazy val preOrderFunctionsWithLineNumbers: (IndexedSeq[IndexedSeq[Symbol]], IndexedSeq[IndexedSeq[Int]]) = {
    // find all function nodes
    val functionNodes: IndexedSeq[SExp] = getFunctionSExps

    val res: IndexedSeq[(IndexedSeq[Symbol], IndexedSeq[Int])] = functionNodes.map(n => (n.preOrder, n.preOrderLineNumbers))

    // sort by size
    val res2: IndexedSeq[(IndexedSeq[Symbol], IndexedSeq[Int])] = res.map(l => (l._1.size, l)).sortBy(_._1).map(_._2)

    // split preorder and line numbers
    val po = res2.map(_._1)
    val ln = res2.map(_._2)
    (po, ln)
  }

  // only do a preorder traversal of functions, and separate the results
  lazy val preOrderFunctionsWithLineNumbersSExp: (IndexedSeq[IndexedSeq[SExp]], IndexedSeq[IndexedSeq[Int]]) = {
    // find all function nodes
    val functionNodes: IndexedSeq[SExp] = getFunctionSExps

    val res: IndexedSeq[(IndexedSeq[SExp], IndexedSeq[Int])] = functionNodes.map(n => (n.preOrderSExp, n.preOrderLineNumbers))

    // sort by size
    val res2: IndexedSeq[(IndexedSeq[SExp], IndexedSeq[Int])] = res.map(l => (l._1.size, l)).sortBy(_._1).map(_._2)

    // split preorder and line numbers
    val po = res2.map(_._1)
    val ln = res2.map(_._2)
    (po, ln)
  }

  def preOrderFunctions: IndexedSeq[IndexedSeq[Symbol]] = preOrderFunctionsWithLineNumbers._1

  lazy val postOrderFunctionsWithLineNumbers: (IndexedSeq[IndexedSeq[Symbol]], IndexedSeq[IndexedSeq[Int]]) = {
    // find all function nodes
    val functionNodes: IndexedSeq[SExp] = getFunctionSExps

    val res: IndexedSeq[(IndexedSeq[Symbol], IndexedSeq[Int])] = functionNodes.map(n => (n.postOrder, n.postOrderLineNumbers))

    // sort by size
    val res2: IndexedSeq[(IndexedSeq[Symbol], IndexedSeq[Int])] = res.map(l => (l._1.size, l)).sortBy(_._1).map(_._2)

    // split postorder and line numbers
    val po = res2.map(_._1)
    val ln = res2.map(_._2)
    (po, ln)
  }

  // only do a postOrder traversal of functions, and separate the results
  lazy val postOrderFunctionsWithLineNumbersSExp: (IndexedSeq[IndexedSeq[SExp]], IndexedSeq[IndexedSeq[Int]]) = {
    // find all function nodes
    val functionNodes: IndexedSeq[SExp] = getFunctionSExps

    val res: IndexedSeq[(IndexedSeq[SExp], IndexedSeq[Int])] = functionNodes.map(n => (n.postOrderSExp, n.postOrderLineNumbers))

    // sort by size
    val res2: IndexedSeq[(IndexedSeq[SExp], IndexedSeq[Int])] = res.map(l => (l._1.size, l)).sortBy(_._1).map(_._2)

    // split postorder and line numbers
    val po = res2.map(_._1)
    val ln = res2.map(_._2)
    (po, ln)
  }

  /** Converts a tree into lists of each node at a certain "level".
      For example, 

           a
          / \
         /   \
         b   c
         |  / \
         d  e f

      is converted to:
      [
        [a],
        [b, c],
        [d, e, f]
      ]. Also returns the parent's location, if any.
   */
  def levels: (Levels, ParentMap) = {

    def concatenateRespectiveLevels(lvl1: Levels, lvl2: Levels): Levels = {
      (lvl1, lvl2) match {
        case (x :: xs, y :: ys) => x ++ y :: concatenateRespectiveLevels(xs, ys)
        case (Nil, y :: ys) => y :: ys
        case (x :: xs, Nil) => x :: xs
        case (Nil, Nil) => Nil
      }
    }

    def combine(lvl1: Levels, lvl2: Levels, lvl1Parents: ParentMap, lvl2Parents: ParentMap): (Levels, ParentMap) = {
      // the lists at each level are concatenated, and lvl2Parents is updated accordingly
      val combinedLvls = concatenateRespectiveLevels(lvl1, lvl2)
      
      // the column offset for lvl2Parents must increase by the number of elements of lvl1 in each row
      val columnOffsetPerRow = (0 to lvl1.length - 1).zip(lvl1.map(_.length)).toMap // (row, column offset to add)
      val newlvl2Parents = lvl2Parents.toList.map({ 
        case ( (row, col), Some((p_row, p_col)) ) => 
          val newcol = col + columnOffsetPerRow.getOrElse(row, 0)
          val newp_col = p_col + columnOffsetPerRow.getOrElse(p_row, 0)
          (row, newcol) -> Some((p_row, newp_col))
        case ( (row, col), None ) => 
          val newcol = col + columnOffsetPerRow.getOrElse(row, 0)
          (row, newcol) -> None
      }).toMap

      val combinedParents = lvl1Parents ++ newlvl2Parents

      // sanity checks
      assert((lvl1Parents.keySet & newlvl2Parents.keySet).isEmpty)
      assert((lvl1Parents.values.toSet & newlvl2Parents.values.toSet).filter(!_.isEmpty).isEmpty)
      assert(combinedLvls.length == math.max(lvl1.length, lvl2.length))
      assert(combinedParents.size == lvl1Parents.size + lvl2Parents.size)

      (combinedLvls, combinedParents)
    }

    val root = name
    val subtreeLevelsAndParentMaps = elems.map(_.levels)
    val (combinedSubtreeLevels, combinedSubtreeParentMap) = subtreeLevelsAndParentMaps.foldLeft((List[List[Symbol]](), Map[LevelsIndex, Option[LevelsIndex]]()))((acc, x) => {
      (acc, x) match {
        case ( (accLvls, accParentMap), (lvls, parentMap) ) =>
          combine(accLvls, lvls, accParentMap, parentMap)
      }
    })

    // add 1 to every row in the combinedSubtreeParentMap, because we're about to add one row on top for the root
    val updatedCombinedSubtreeParentMap: ParentMap = combinedSubtreeParentMap.map({ 
      case ( (row, col), Some((p_row, p_col)) ) => 
        (row + 1, col) -> Some((p_row + 1, p_col))
      case ( (row, col), None ) =>
        (row + 1, col) -> None
    })

    // add the root at the top level
    val finalLevels: Levels = List(root) :: combinedSubtreeLevels
    // the root has no parent, and everybody in row 1 has the root as its parent
    val finalParentMap: ParentMap = updatedCombinedSubtreeParentMap.map({ 
      case ( (1, col), None ) =>  
        (1, col) -> Some((0, 0))
      case ( (1, col), Some(_) ) => 
        assert(false)
        ???
      case ( (row, col), Some(x) ) =>  
        (row, col) -> Some(x)
      case ( (row, col), None ) => 
        assert(false)
        ???
    }) + ((0, 0) -> None)

    (finalLevels, finalParentMap)
  }

  /** compute 2D "w-h-grams" of an SExp lazily */
  def w_h_grams(width: Int, height: Int): Stream[List[Option[Symbol]]] = {
    val (lvls, parentMap) = levels
    val lvlsRowColMap = levelsToRowColMap(lvls)
    val sortedKeys = lvlsRowColMap.keys.toIndexedSeq.sorted

    def helper(i: Int): Stream[List[Option[Symbol]]] =
      if (i >= sortedKeys.length) { Stream.empty }
      else {
        val idx = sortedKeys(i)
        val window = indexTo2DWindow(height, width, idx, lvlsRowColMap, parentMap)
        window #:: helper(i+1)
      }

    helper(0)
  }
}

object SExp {
  type Levels = List[List[Symbol]]
  type LevelsIndex = (Int, Int)
  type ParentMap = Map[LevelsIndex, Option[LevelsIndex]]

  def apply(name: Symbol): SExp = SExp(name, List())
  def apply(name: Symbol, lineno: Int): SExp = SExp(name, List(), Loc(lineno, -1))
  def apply(name: Symbol, elem: SExp): SExp = SExp(name, List(elem))
  def apply(name: Symbol, elem: SExp, lineno: Int): SExp = SExp(name, List(elem), Loc(lineno, -1))
  implicit def listOfSExpToSexp(sexps: List[SExp]): SExp = {
    SExp('List, sexps)
  }


  // (row, col) -> element at that row & col
  def levelsToRowColMap(lvls: Levels): Map[(Int, Int), Symbol] = {
    val M = MMap[(Int, Int), Symbol]()
    var (i, j) = (0, 0)
    for (lvl <- lvls) {
      for (x <- lvl) {
        M((i, j)) = x
        j += 1
      }
      i += 1
      j = 0
    }

    M.toMap
  }

  // 2D window in row-major order
  // None represents empty space
  def indexTo2DWindow(height: Int, width: Int, index: (Int, Int), lvlsRowColMap: Map[(Int, Int), Symbol], parentMap: ParentMap): List[Option[Symbol]] = {
    // compute all the necessary indices
    var startingRowIndices: List[(Int, Int)] = List(index)
    val (x, y) = index
    var curIndex: (Int, Int) = parentMap.getOrElse(index, Some((-1, -1))).getOrElse((-1, -1))
    for (i <- 1 to height - 1) { // because we've already included index
      startingRowIndices = curIndex :: startingRowIndices
      curIndex = parentMap.getOrElse(curIndex, Some((-1, -1))).getOrElse((-1, -1))
    }
    //startingRowIndices = startingRowIndices.reverse

    val indices: List[List[(Int, Int)]] = startingRowIndices.map({ case (row, col) => 
      (0 to width - 1).map(offset => (row, col + offset)).toList
    })

    val values: List[List[Option[Symbol]]] = indices.map(_.map(lvlsRowColMap.get(_)))
    val inorderValues = (List[Option[Symbol]]() /: values) (_ ++ _)
    inorderValues
  }

  // for testing purposes
  def main(args: Array[String]): Unit = {
    /*
           a     a:(0,0)
          / \
         /   \
         b   c   b:(1, 0), c:(1, 1)
         |  / \ 
         d  e f  d:(2, 0), e:(2, 1), f:(2, 2)
    */
    val tree = 
      SExp('a, List(
        SExp('b, List(SExp('d, List()))),
        SExp('c, List(
          SExp('e, List()),
          SExp('f, List())))
        )
      )

    val (lvls, parentMap) = tree.levels
    println(s"lvls: $lvls")
    println(s"parentMap: $parentMap")

    val lvlsRowColMap = levelsToRowColMap(lvls)
    println(s"lvlsRowColMap: $lvlsRowColMap")

    val twobytwograms = tree.w_h_grams(2, 2).toList
    println(s"twobytwograms:")
    twobytwograms.foreach(w => { print("  "); println(w) })
  }
}
