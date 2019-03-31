package fett.scoring
import fett.util.trees._

object DummyScoring extends Scoring {
  override val collapseTrees = true
  def similarity(a: ParseTreeNode, b: ParseTreeNode): Result[LineNo] = Result(0, Set.empty)
  override def filterParseTreeNode(n: ParseTreeNode) = n.chop.toSExp.preOrder.length >= SmithWaterman.cutoffSize
}
