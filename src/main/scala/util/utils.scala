package fett.util.utils

case class Loc(lineno: Int, colno: Int) {
  override def toString(): String = s":$lineno:$colno" 
}
case class PreOrderNode(name: Symbol, loc: Loc)
case class PostOrderNode(name: Symbol, loc: Loc)

// ctx.start.getLine()
// ctx.start.getCharPositionInLine()
// preorder += PreOrderNode(Symbol("enterProgram"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine()))

object Implicits {
  implicit def symbolToString(s: Symbol): String = s.toString.tail
}
