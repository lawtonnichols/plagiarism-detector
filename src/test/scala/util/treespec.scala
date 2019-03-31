import org.scalatest._
import fett.util.trees._
import scala.collection.mutable.ArraySeq

object ParseTreeData {
  // Construct data call-by-name so that it will be same each time it is invoked
  // and data source won't be mutated during tests
  val parseTreeNodes: List[() ⇒ ParseTreeNode] = List(
    () ⇒ ParseTreeNode("1", ArraySeq(ParseTreeNode("2"), ParseTreeNode("3", ArraySeq(ParseTreeNode("4", ArraySeq(ParseTreeNode("5"), ParseTreeNode("6"))))))),
    () ⇒ ParseTreeNode("1", ArraySeq(ParseTreeNode("2"), ParseTreeNode("3", ArraySeq(ParseTreeNode("4", ArraySeq(ParseTreeNode("5", ArraySeq(ParseTreeNode("6"))))))))),
    () ⇒ ParseTreeNode("1", ArraySeq(ParseTreeNode("2", ArraySeq(ParseTreeNode("3", ArraySeq(ParseTreeNode("4", ArraySeq(ParseTreeNode("5", ArraySeq(ParseTreeNode("6")))))))))))
  )

  val collapsedNodes: List[() ⇒ ParseTreeNode] = List(
    () ⇒ ParseTreeNode("1", ArraySeq(ParseTreeNode("2"), ParseTreeNode("4", ArraySeq(ParseTreeNode("5"), ParseTreeNode("6"))))),
    () ⇒ ParseTreeNode("1", ArraySeq(ParseTreeNode("2"), ParseTreeNode("6"))),
    () ⇒ ParseTreeNode("6")
  )

  val parseTrees: List[() ⇒ ParseTree] = parseTreeNodes map { n ⇒ () ⇒ ParseTree(n()) }
  val collapsedTrees: List[() ⇒ ParseTree] = collapsedNodes map { n ⇒ () ⇒ ParseTree(n()) }
}

class ParseTreeNodeSuite extends FunSuite {
  import ParseTreeData._

  /*test("Collapsing a ParseTreeNode should produce expected values") {
    for ((node, result) ← parseTreeNodes zip collapsedNodes) {
      assert(node().collapse == result())
    }
  }*/

  test("Collapsing a ParseTreeNode should be idempotent") {
    for (node ← parseTreeNodes) {
      assert(node().collapse.copy() == node().collapse.collapse.copy())
    }
  }
}

class ParseTreeSuite extends FunSuite {
  import ParseTreeData._

  test("Collapsing a ParseTree should be idempotent") {
    for (tree ← parseTrees) {
      val t1 = tree()
      val t2 = tree()
      assert(t1.collapse == t2.collapse.collapse)
    }
  }

  test("Collapsing shouldn't change a ParseTree") {
    for (tree ← parseTrees) {
      val t1 = tree()
      val t2 = tree()
      t2.collapse
      assert(t1 == t2)
    }
  }
}
