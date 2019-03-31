package fett.util.trees

import fett.sexp._
import scala.collection.mutable.{ArraySeq, Queue, MutableList, Set => MSet, Map => MMap}
import fett.util.utils._
import fett.util.hashConsing._
import scala.math.min

case class ParseTree(var root: ParseTreeNode) {

  def toSExp: SExp = root.toSExp

  /**
    * Collapse this tree by merging each node with a single child with its child.
    */
  def collapse(importantNodeLabels: Set[String]): ParseTree = {
    ParseTree(root.collapse(importantNodeLabels))
  }

  def collapse: ParseTree = collapse(Set())

  // remove terminal leaf nodes
  def chop(): ParseTree = {
    assert(root.label != "org.antlr.v4.runtime.tree.TerminalNodeImpl")
    ParseTree(root.chop)
  }

  def leaves = root.leaves

  def collectNodesWithLabel(label: String) = {
    root.collectNodesWithLabel(label)
  }

  def functionNodes = root.functionNodes

  def toTree: TreeNode = root.toTree

}

/*abstract class ParseTreeNode
case object EmptyParseTreeNode extends ParseTreeNode*/
case class ParseTreeNode(val label: String, val children: IndexedSeq[ParseTreeNode] = IndexedSeq.empty, val start: Loc = Loc(-1, -1), val end: Loc = Loc(-1, -1)) extends SmartHash {

  def leaves: Seq[String] = if (children.isEmpty) {
    Seq(label)
  } else {
    children.flatMap(_.leaves)
  }

  lazy val toSExp: SExp = {
    SExp(Symbol(label), children.toList.map(_.toSExp), start, end, children.isEmpty)
  }

  def isLeaf: Boolean = children.isEmpty

  def chop: ParseTreeNode = {
    // remove any children that are terminal nodes
    copy(children=children.filter(_.label != "org.antlr.v4.runtime.tree.TerminalNodeImpl").map(_.chop))
  }

  /**
    * Collapse the subtree with this node as its root by merging each node with a single child with its child.
    *
    * Caution: this mutates the tree. Also, if the returned node is not same as this, then this node is invalid.
    *
    * @returns the new root of the subtree
    */
  def collapse(importantNodeLabels: Set[String]): ParseTreeNode = {
    // collapse all children
    val c = children.map(_.collapse(importantNodeLabels))
    // println(s"label: $label")
    // println(s"importantNodeLabels: $importantNodeLabels")
    val isANodeWeCareAbout = importantNodeLabels contains label
    if (c.filter(n => !n.isLeaf && n.label != "org.antlr.v4.runtime.tree.TerminalNodeImpl").size == 1 && !isANodeWeCareAbout) {
      val newroot = c.filter(n => !n.isLeaf && n.label != "org.antlr.v4.runtime.tree.TerminalNodeImpl").head
      val terminalsToTheLeft = c.takeWhile(_ != newroot)
      val terminalsToTheRight = c.dropWhile(_ != newroot).tail
      newroot.copy(children=terminalsToTheLeft ++ newroot.children ++ terminalsToTheRight)
    } else {
      copy(children=c)
    }
  }

  def collapse: ParseTreeNode = collapse(Set())

  def size: Int = children.map(_.size).sum + 1

  /** Get the parent, the parent's parent, the parent's parent's parent, and so
    * on until the root of the tree.
    */
  def getAllAncestors(parentMap: Map[ParseTreeNode, Option[ParseTreeNode]]): List[ParseTreeNode] = {
    if (ParseTreeNode.ancestorCache.contains(this)) return ParseTreeNode.ancestorCache(this)

    val res = parentMap(this) match {
      case Some(p) => p :: p.getAllAncestors(parentMap)
      case None    => Nil
    }
    ParseTreeNode.ancestorCache(this) = res
    res
  }

  def collectNodesWithLabel(label: String): List[ParseTreeNode] = {
    if (this.label == label) {
      List(this)
    } else {
      children.toList.flatMap(_.collectNodesWithLabel(label))
    }
  }

  def generateParentMap(r: ParseTreeNode): Map[ParseTreeNode, Option[ParseTreeNode]] = {
    def helper(curNode: ParseTreeNode, lastParent: Option[ParseTreeNode]): Map[ParseTreeNode, Option[ParseTreeNode]] = {
      Map() + (curNode -> lastParent) ++ curNode.children.map(helper(_, Some(curNode))).flatten
    }

    helper(r, None)
  }

  def functionNodes: List[ParseTreeNode] = {
    // invalidate parent node cache
    ParseTreeNode.ancestorCache.clear()
    val parentMap = generateParentMap(this)

    val functionNodeLabels = Set(
      "antlrparsers.cpp14.CPP14Parser$FunctiondefinitionContext",
      "antlrparsers.ecmascript.ECMAScriptParser$FunctionDeclarationContext",
      "antlrparsers.ecmascript.ECMAScriptParser$FunctionExpressionContext",
      "antlrparsers.golang.GolangParser$FunctionContext",
      "antlrparsers.java.JavaParser$MethodDeclarationContext",
      "antlrparsers.java.JavaParser$GenericMethodDeclarationContext",
      "antlrparsers.java.JavaParser$ConstructorDeclarationContext",
      "antlrparsers.java.JavaParser$GenericConstructorDeclarationContext"
    )

    val worklist = Queue[ParseTreeNode](this)
    val res = MSet[ParseTreeNode]()

    while (worklist.nonEmpty) {
      val s = worklist.dequeue()

      // tentatively add this node; we may remove it later if one of the children ends up also being a function node
      if (functionNodeLabels contains s.label) {
        res += s

        // because s is a function node, and we're looking for the bottom-most function nodes, remove all of its parents from the result set
        val parentsOfS = s.getAllAncestors(parentMap)
        res --= parentsOfS
      }

      // search the children
      worklist ++= s.children
    }

    // get functions of 15 lines or less -- it makes more sense to just return everything
    // and create a cutoff value in terms of the size of the pruned preorder list, though
    // res.toList.filter(sexp => math.abs(sexp.end.lineno - sexp.start.lineno) >= 15)

    res.toList
  }

  def toTree: TreeNode = Tree(label, if (children.isEmpty) IndexedSeq(Empty) else children.map(_.toTree))
}

object ParseTreeNode {
  val ancestorCache = MMap[ParseTreeNode, List[ParseTreeNode]]()
}


//case class Tree(root: TreeNode, list1: IndexedSeq[Int] = IndexedSeq.empty, list2: IndexedSeq[Int] = IndexedSeq.empty, Lemma8: IndexedSeq[Int] = IndexedSeq.empty) 

trait TreeNodeId
case class TreeId(val id: Int) extends TreeNodeId with SmartHash
case class ForestId(val startId: TreeNodeId, val endId: TreeNodeId) extends TreeNodeId
case object EmptyId extends TreeNodeId

abstract class TreeNode extends SmartHash {
  var id: TreeNodeId = null // Empty will have id of -1; Trees will have this set
  var fromT1: Boolean = true

  def toForest: Forest = this match {
    case Empty => Forest(IndexedSeq())
    case Tree(label, children) => Forest(children)
    case forest@Forest(_) => forest
  }

  def toLabel: String = this match {
    case Empty => ("")
    case Tree(label, children) => label
    case f@Forest(_) => ("")
  }

  lazy val getAllNodes: IndexedSeq[TreeNode] = this match {
    case Empty => IndexedSeq(this)
    case Tree(label, children) => this +: children.map(_.getAllNodes).flatten
    case Forest(children) => children.map(_.getAllNodes).flatten
  }
}

case object Empty extends TreeNode
case class Tree(label: String, children: IndexedSeq[TreeNode]) extends TreeNode
case class Forest(children: IndexedSeq[TreeNode]) extends TreeNode

object Tree {
  def γ(l1: String, l2: String): Int = 
    if(l1 == l2) 0
    else 1

  // TODO: fix for multithreading
  var t1: TreeNode = null
  var t2: TreeNode = null
  var idToNode = MMap[TreeNodeId, TreeNode]()
  val memo: MMap[(TreeNodeId, TreeNodeId), Int] = MMap()

  implicit def treeNodeToTreeNodeId(tn: TreeNode): TreeNodeId = {
    tn match {
      case Empty => EmptyId
      case t: Tree => t.id
      case Forest(IndexedSeq()) =>
        val id = ForestId(TreeId(Int.MaxValue), TreeId(Int.MaxValue))
        if (!idToNode.contains(id)) {
          idToNode(id) = tn
        }
        id
      case Forest(children) => 
        val id = ForestId(children.head.id, children.last.id)
        if (!idToNode.contains(id)) {
          idToNode(id) = tn
        }
        id
    }
  }

  def α(t1: TreeNode, t2: TreeNode): Int = {
    Tree.t1 = t1
    Tree.t2 = t2

    idToNode.clear()
    memo.clear()

    // create numbering
    for ((t, i) <- t1.getAllNodes.zipWithIndex) {
      if (t != Empty) {
        idToNode(TreeId(i + 1)) = t // + 1 up here and - 1 down there because +0/-0 would clash
        t.id = TreeId(i + 1)
      } else {
        idToNode(EmptyId) = t
        t.id = EmptyId
      }
    }

    for ((t, i) <- t2.getAllNodes.zipWithIndex) {
      if (t != Empty) {
        idToNode(TreeId(-i - 1)) = t
        t.id = TreeId(-i - 1)
      } else {
        idToNode(EmptyId) = t
        t.id = EmptyId
      }
    }

    α_helper(t1, t2)
  }

  def α_helper(t1_id: TreeNodeId, t2_id: TreeNodeId): Int = {
    // println(s"α:\n\t$t1\n\t$t2")

    if (memo.contains(t1_id, t2_id)) {
      // println("returning " + memo((t1, t2)))
      // println("hit the memo")
      return memo((t1_id, t2_id))
    }

    val t1: TreeNode = idToNode(t1_id)
    val t2: TreeNode = idToNode(t2_id)

    val res = (t1, t2) match {
      // Lemma 7
      case (Empty, Empty) => 0
      case (Forest(IndexedSeq()), Forest(IndexedSeq())) => 0
      case (t@Tree(label, _), Empty) => α_helper(t.toForest, Forest(IndexedSeq())) + γ(t.label, "")
      case (Empty, t@Tree(label, _)) => α_helper(Forest(IndexedSeq()), t.toForest) + γ("", t.label)
      case (Forest(children), Forest(IndexedSeq())) => children.map { child => α_helper(child, Empty) }.sum
      case (Forest(IndexedSeq()), Forest(children)) => children.map { child => α_helper(Empty, child) }.sum

      // Lemma 8
      case (t1@Tree(l1, c1), t2@Tree(l2, c2)) =>
        val m1 = α_helper(t1.toForest, t2.toForest) + γ(l1, l2)
        val m2 = α_helper(Empty, t2) + c2.map { child => α_helper(t1, child) - α_helper(Empty, child) }.min
        val m3 = α_helper(t1, Empty) + c1.map { child => α_helper(child, t2) - α_helper(child, Empty) }.min
        return Math.min(m1, Math.min(m2, m3))

      // Lemma 9
      case f@(Forest(c1), Forest(c2)) =>
        val n1 = α_helper(Forest(c1.init), Forest(c2.init)) + α_helper(c1.last,c2.last)
        val n2 = α_helper(Forest(c1.init),Forest(c2)) + α_helper(c1.last,Empty)
        val n3 = α_helper(Forest(c1),Forest(c2.init)) + α_helper(Empty,c2.last)
        var n4 = Int.MaxValue
        var n5 = Int.MaxValue
        if (c1.length >= 2) {
          n4 = γ("", c2.last.toLabel) + {
            for {
              k <- 1 to c1.length - 1
            } yield {
              val (v1ThroughvKMinus1, vKThroughvS) = c1.splitAt(k)
              α_helper(Forest(v1ThroughvKMinus1), Forest(c2.init)) + α_helper(Forest(vKThroughvS), c2.last.toForest)
            }
          }.min
        }
        if (c2.length >= 2){
          n5 = γ(c1.last.toLabel, "") + {
            for {
              k <- 1 to c2.length - 1
            } yield {
              val (w1ThroughwKMinus1, wKThroughwT) = c2.splitAt(k)
              α_helper(Forest(c1.init), Forest(w1ThroughwKMinus1)) + α_helper(c2.last.toForest, Forest(wKThroughwT))
            }
          }.min
        }
        
        return Math.min(n1, Math.min(n2, Math.min(n3, Math.min(n4, n5))))
    }

    memo((t1_id, t2_id)) = res
    // println(s"adding $t1;;; $t2 to the memo")

    res
  }
}

// object TreeOld {
//   ////////////// Tree Alignment ////////////////


//   def minimum(v:Int, w:Int, x:Int, y:Int, z:Int):Int = {
//     val seq = IndexedSeq(v, w, x, y, z)
//     return seq.min
//   }


//   def Cost(Node1:TreeNode, Node2:TreeNode):Int = {
//     (Node1, Node2) match {
//       case (_:NonEmptyTreeNode, EmptyTreeNode) => return 1

//       case (EmptyTreeNode, _:NonEmptyTreeNode) => return 1

//         case (x:NonEmptyTreeNode, y:NonEmptyTreeNode) => if(x.label == y.label) {
//           return 0
//         } else {
//           return 1
//         }

//       }
//     }

    
//   def AlphaForest(F1:IndexedSeq[TreeNode], F2:IndexedSeq[TreeNode]):Int = { //Lemma 9
//     val a = AlphaForest(F1.init, F2.init) + AlphaTree(Tree(F1.last), Tree(F2.last))
//     val b = AlphaForest(F1.init, F2) + AlphaTree(Tree(F1.last), Tree(EmptyTreeNode))
//     val c = AlphaForest(F1, F2.init) + AlphaTree(Tree(EmptyTreeNode), Tree(F2.last))
//     for (i <- 1 to F1.size) {
//       var getNode = F2(i) match {//get TreeNode
//         case (x:NonEmptyTreeNode) => list1 :+ AlphaForest(F1.take(i-1), F2.init) + AlphaForest(F1.drop(i-1), x.children)
//         }
//       }
//       val d = Cost(EmptyTreeNode,F2.last) + list1.min
//       for (i <- 1 to F2.size) {
//         var getNode = F1.last match {//get TreeNode
//           case (y:NonEmptyTreeNode) => list2 :+ AlphaForest(F1.init, F2.take(i-1)) + AlphaForest(y.children, F2.drop(i-1))
//           }
//         }
//         val e = Cost(F1.last, EmptyTreeNode) + list2.min

//         return minimum(a,b,c,d,e)
//       }


//     def AlphaTree(T1:Tree, T2:Tree):Int = {
//       var v = T1.root
//       var w = T2.root
//       var l = EmptyTreeNode
//       var sum = 0
//       (v, w) match {
//         // T1 and T2 are empty
//         case (EmptyTreeNode, EmptyTreeNode) => return 0
//         // T2 is empty
//         case (x:NonEmptyTreeNode,EmptyTreeNode) => for(a <- x.children) {
//           sum = sum + AlphaTree(Tree(a), T2)
//         }
//         return sum + Cost(x, l)
//         //T1 is empty
//         case (EmptyTreeNode, y:NonEmptyTreeNode) => for(a <- y.children){
//           sum = sum + AlphaTree(T1, Tree(a))
//         }
//         return sum + Cost(l, y)
//         //Lemma8
//         case (x:NonEmptyTreeNode, y:NonEmptyTreeNode) =>
//         var T2min = AlphaTree(T1, Tree(y.children(1))) - AlphaTree(Tree(l), Tree(y.children(1)))
//         for(a <- y.children){
//           if(T2min > AlphaTree(T1, Tree(a)) - AlphaTree(Tree(l), Tree(a)))
//             T2min = AlphaTree(T1, Tree(a)) - AlphaTree(Tree(l), Tree(a))
//         }
//         Lemma8 :+ (AlphaTree(Tree(l), T2) + T2min)

//         var T1min = AlphaTree(Tree(x.children(1)), T2) - AlphaTree(Tree(x.children(1)), Tree(l))
//         for(a <- x.children){
//           if(T1min > AlphaTree(Tree(a), T2) - AlphaTree(Tree(a), Tree(l))){
//             T1min = AlphaTree(Tree(a), T2) - AlphaTree(Tree(a), Tree(l))
//           }
//         }
//         Lemma8 :+ (AlphaTree(T1, Tree(l)) + T1min)

//         Lemma8 :+ (AlphaForest(x.children, y.children) + Cost(x, y))

//         return Lemma8.min
//       }
//     }


//   //////////////////////////////////////////////
// }

