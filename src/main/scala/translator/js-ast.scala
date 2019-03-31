package notjs.translator.jsast

import scala.collection.JavaConverters._
import java.util.{List => JList}
import org.mozilla.javascript.ast._
import org.mozilla.javascript.Node
import org.mozilla.javascript.Token
import fett.sexp._
import scala.{Symbol ⇒ ScalaSymbol}

object JSASTTesting {
  import java.io._
  import notjs.translator._

  /** Read AST from given file */
  def readAST(fName: String) = {
    TransformJSAST(RhinoToJSAST(RunTranslator.parseJavaScript(new File(fName))))
  }

  def main(args: Array[String]): Unit = {
    val ast = readAST(args(0))
    println(ast.toSExp)
  }
}

sealed trait JSAST {
  def toSExp: SExp
  var lineno = -1
}
sealed trait JSStmt extends JSAST
sealed trait JSExp extends JSStmt
sealed trait JSLHS extends JSAST

// all the variables are initially set to JSUndef()
case class JSToplevelDecl(vars: Set[JSPVar], rest: JSStmt) extends JSAST {
  def toSExp = rest.toSExp // don't care about the variables
}

// begin expressions
sealed trait JSFunction {
  def name: Option[JSPVar]
  def params: Seq[JSPVar]
  def body: JSStmt
}
case class JSNum(n: Double) extends JSExp {
  def toSExp = SExp('JSNum, SExp(ScalaSymbol(n.toString)), this.lineno)
}
case class JSBool(b: Boolean) extends JSExp
{
  def toSExp = SExp('JSBool, SExp(ScalaSymbol(b.toString)), this.lineno)
}
case class JSStr(s: String) extends JSExp
{
  def toSExp = SExp('JSStr, SExp(ScalaSymbol(s.toString)), this.lineno)
}
case class JSUndef() extends JSExp { // for translator-inserted uses
  def toSExp = SExp('JSUndef, this.lineno)
}
case class JSNull() extends JSExp {
  def toSExp = SExp('JSNull, this.lineno)
}
case class JSEmpty() extends JSExp {
  def toSExp = SExp('JSEmpty, this.lineno)
}

sealed trait JSVar extends JSExp with JSLHS {
  def isScratch(): Boolean
}
case class JSPVar(name: String) extends JSVar { // may include "undefined" from the user
  def isScratch(): Boolean = false
  def toSExp = SExp('JSVar, this.lineno)
}
case class JSScratch(id: Int) extends JSVar {
  def isScratch(): Boolean = true
  def toSExp = SExp('JSVar, this.lineno) // calling them both JSVar
}

case class JSSimpleAssign(x: JSVar, rhs: JSExp) extends JSExp {
  def toSExp = SExp('JSSimpleAssign, rhs.toSExp, this.lineno)
}
case class JSCompoundAssign(x: JSVar, op: AnnotatedBop, rhs: JSExp) extends JSExp {
  def toSExp = SExp('JSCompoundAssign, List(op.toSExp, rhs.toSExp), this.lineno)
}
case class JSSimpleUpdate(lhs: JSAccess, rhs: JSExp) extends JSExp {
  def toSExp = SExp('JSSimpleUpdate, List(lhs.toSExp, rhs.toSExp), this.lineno)
}
case class JSCompoundUpdate(lhs: JSAccess, op: AnnotatedBop, rhs: JSExp) extends JSExp {
  def toSExp = SExp('JSCompoundUpdate, List(lhs.toSExp, op.toSExp, rhs.toSExp), this.lineno)
}

sealed trait RegexFlag {
  def flag(): Char
}
case object GlobalFlag extends RegexFlag {
  def flag(): Char = 'g'
}
case object IgnoreCaseFlag extends RegexFlag {
  def flag(): Char = 'i'
}
case object MultilineFlag extends RegexFlag {
  def flag(): Char = 'm'
}
case class JSRegexp(str: JSStr, flags: Set[RegexFlag]) extends JSExp {
  def toSExp = SExp('JSRegexp, str.toSExp, this.lineno)
}

case class JSTernary(guard: JSExp, ifTrue: JSExp, ifFalse: JSExp) extends JSExp {
  def toSExp = SExp('JSTernary, List(guard.toSExp, ifTrue.toSExp, ifFalse.toSExp) , this.lineno)
}
case class JSAccess(target: JSExp, element: JSExp) extends JSExp with JSLHS {
  def toSExp = SExp('JSAccess, List(target.toSExp, element.toSExp), this.lineno)
}
case class JSDebug(exp: JSExp) extends JSExp { // prints out the value of the given expression when in debugging mode, and returns the value
  def toSExp = SExp('JSDebug, exp.toSExp, this.lineno)
}
case class JSNew(target: JSExp, args: Seq[JSExp]) extends JSExp {
  def toSExp = SExp('JSNew, List[SExp](target.toSExp, args.map(_.toSExp).toList), this.lineno)
}
case class JSCall(target: JSExp, args: Seq[JSExp]) extends JSExp {
  def toSExp = SExp('JSCall, List[SExp](target.toSExp, args.map(_.toSExp).toList), this.lineno)
}
case class JSFunctionExp(name: Option[JSPVar], params: Seq[JSPVar], body: JSStmt) extends JSExp with JSFunction {
  def toSExp = SExp('JSFunctionExp, List[SExp](params.map(_.toSExp).toList, body.toSExp), this.lineno) // skipping `name`
}
case class JSBinop(left: JSExp, op: JSBop, right: JSExp) extends JSExp {
  def toSExp = SExp('JSBinop, List(left.toSExp, op.toSExp, right.toSExp), this.lineno)
}
case class JSUnop(op: JSUop, exp: JSExp) extends JSExp {
  def toSExp = SExp('JSUnop, List(op.toSExp, exp.toSExp), this.lineno)
}
case class JSObject(fields: Seq[(JSStr, JSExp)]) extends JSExp {
  val fields2: List[SExp] = fields.map({case (s, e) => 
    SExp('FieldNameExpPair, List(s.toSExp, e.toSExp))
  }).toList
  
  def toSExp = SExp('JSObject, fields2, this.lineno)
}
case class JSArray(elements: Seq[JSExp]) extends JSExp {
  def toSExp = SExp('JSArray, elements.map(_.toSExp).toList, this.lineno)
}
case class JSThis() extends JSExp {
  def toSExp = SExp('JSThis, this.lineno)
}
case class JSDelete(exp: JSExp) extends JSExp {
  def toSExp = SExp('JSDelete, exp.toSExp, this.lineno)
}
case class JSPrefixInc(lhs: JSLHS) extends JSExp {
  def toSExp = SExp('JSPrefixInc, lhs.toSExp, this.lineno)
}
case class JSPostfixInc(lhs: JSLHS) extends JSExp {
  def toSExp = SExp('JSPostfixInc, lhs.toSExp, this.lineno)
}
case class JSPrefixDec(lhs: JSLHS) extends JSExp {
  def toSExp = SExp('JSPrefixDec, lhs.toSExp, this.lineno)
}
case class JSPostfixDec(lhs: JSLHS) extends JSExp {
  def toSExp = SExp('JSPostfixDec, lhs.toSExp, this.lineno)
}

// artificial.  The semantics are that it performs the bindings and returns retval
case class TransformDecl(bindings: Seq[(JSScratch, JSExp)], retval: JSExp) extends JSExp {
  def toSExp = SExp('TransformDecl, bindings.toList.flatMap(p ⇒ List(p._1, p._2).map(_.toSExp)) :+ retval.toSExp, this.lineno)
}
// end expressions

// begin loops
sealed trait JSLoop extends JSStmt {
  def body: JSStmt
}
case class JSWhile(guard: JSExp, body: JSStmt) extends JSLoop {
  def toSExp = SExp('JSWhile, List(guard.toSExp, body.toSExp), this.lineno)
}
case class JSDoWhile(body: JSStmt, guard: JSExp) extends JSLoop {
  def toSExp = SExp('JSDoWhile, List(body.toSExp, guard.toSExp), this.lineno)
}
case class JSForIn(itervar: JSLHS, iterable: JSExp, body: JSStmt) extends JSLoop {
  def toSExp = SExp('JSForIn, List(itervar.toSExp, iterable.toSExp, body.toSExp), this.lineno)
}
case class JSFor(initializer: JSStmt, guard: JSExp, increment: JSStmt, body: JSStmt) extends JSLoop {
  def toSExp = SExp('JSFor, List(initializer.toSExp, guard.toSExp, increment.toSExp, body.toSExp), this.lineno)
}
// end loops

// begin statements

// may be empty on initial pass
sealed trait JSSeq extends JSStmt {
  def stmts: List[JSStmt]
  def isOrig(): Boolean
}
object JSSeq {
  def apply(stmts: List[JSStmt]): JSInsertedSeq =
    JSInsertedSeq(stmts)

  def unapply(s: JSSeq): Option[List[JSStmt]] =
    Some(s.stmts)
}
case class JSOrigSeq(stmts: List[JSStmt]) extends JSSeq { // in the original program
  def isOrig(): Boolean = true
  def toSExp = SExp('JSOrigSeq, stmts.map(_.toSExp), this.lineno)
}
case class JSInsertedSeq(stmts: List[JSStmt]) extends JSSeq { // inserted during translation
  def isOrig(): Boolean = false
  def toSExp = SExp('JSInsertedSeq, stmts.map(_.toSExp), this.lineno)
}

 // may be empty on initial pass
case class JSDecl(bindings: List[(JSPVar, Option[JSExp])]) extends JSStmt {
  def toSExp = {
    val bindings2 = bindings.map({case (pvar, eo) => eo match {
      case None => SExp('DeclBinding, pvar.toSExp)
      case Some(e) => SExp('DeclBinding, List(pvar.toSExp, e.toSExp))
    }})
    SExp('JSDecl, bindings2, this.lineno)
  }
}

// separated out since we hoist the name differently
case class JSFunctionDecl(fname: JSPVar, params: Seq[JSPVar], body: JSStmt) extends JSStmt with JSFunction {
  def name = Some(fname)
  def toSExp = SExp('JSFunctionDecl, List[SExp](fname.toSExp, params.map(_.toSExp).toList, body.toSExp), this.lineno)
}
case class JSIf(guard: JSExp, ifTrue: JSStmt, ifFalse: Option[JSStmt]) extends JSStmt {
  def toSExp = {
    ifFalse match {
      case None => SExp('JSIf, List(guard.toSExp, ifTrue.toSExp), this.lineno)
      case Some(s) => SExp('JSIf, List(guard.toSExp, ifTrue.toSExp, s.toSExp), this.lineno)
    }
  }
}
case class JSTry(tryBody: JSStmt, catchPart: Option[(JSPVar, JSStmt)], finallyBody: Option[JSStmt]) extends JSStmt {
  def toSExp = {
    val list1: List[SExp] = List(tryBody.toSExp)
    val list2: List[SExp] = catchPart match {
      case None => list1
      case Some((pv, s)) => list1 :+ SExp('CatchPart, List(pv.toSExp, s.toSExp))
    }
    val list3: List[SExp] = finallyBody match {
      case None => list2
      case Some(s) => list2 :+ s.toSExp
    }
    SExp('JSTry, List[SExp](tryBody.toSExp, list3), this.lineno)
  }
}
case class JSThrow(exp: JSExp) extends JSStmt {
  def toSExp = SExp('JSThrow, exp.toSExp, this.lineno)
}
case class JSLabel(name: String) extends JSAST { // hack for a cleaner transform
  def toSExp = SExp('JSLabel, SExp(ScalaSymbol(name)), this.lineno)
}
case class JSLabeledStmt(labels: Seq[JSLabel], body: JSStmt) extends JSStmt { // may be empty on initial pass
  def toSExp = SExp('JSLabeledStmt, List[SExp](labels.map(_.toSExp).toList, body.toSExp), this.lineno)
}
case class JSBreak(label: Option[JSLabel]) extends JSStmt {
  def toSExp = SExp('JSBreak, label.fold(List[SExp]())(l => List(l.toSExp)), this.lineno)
}
case class JSContinue(label: Option[JSLabel]) extends JSStmt {
  def toSExp = SExp('JSContinue, label.fold(List[SExp]())(l => List(l.toSExp)), this.lineno)
}
case class JSWith(exp: JSExp, body: JSStmt) extends JSStmt {
  def toSExp = SExp('JSWith, List(exp.toSExp, body.toSExp), this.lineno)
}
case class JSReturn(exp: Option[JSExp]) extends JSStmt {
  def toSExp = SExp('JSReturn, exp.fold(List[SExp]())(e => List(e.toSExp)), this.lineno)
}
case class JSSwitch(exp: JSExp, cases: Seq[(JSExp, JSStmt)], default: Option[JSStmt]) extends JSStmt {
  def toSExp = {
    val list1 = List(exp.toSExp)
    val cases2: SExp = cases.map({case (e, s) => SExp('SwitchCase, List(e.toSExp, s.toSExp))}).toList
    val list2: List[SExp] = list1 :+ cases2
    val list3: List[SExp] = default match {
      case None => list2
      case Some(s) => list2 :+ s.toSExp
    }
    SExp('JSSwitch, list3, this.lineno)
  }
}
// end statements

// begin binops
sealed trait JSBop {
  def toSExp: SExp
}
sealed trait AnnotatedBop extends JSBop
case object JSMul extends AnnotatedBop {
  def toSExp = SExp('JSMul)
}
case object JSDiv extends AnnotatedBop {
  def toSExp = SExp('JSDiv)
}
case object JSMod extends AnnotatedBop {
  def toSExp = SExp('JSMod)
}
case object JSAdd extends AnnotatedBop {
  def toSExp = SExp('JSAdd)
}
case object JSSub extends AnnotatedBop {
  def toSExp = SExp('JSSub)
}
case object JSShiftLeft extends AnnotatedBop {
  def toSExp = SExp('JSShiftLeft)
}
case object JSShiftRight extends AnnotatedBop {
  def toSExp = SExp('JSShiftRight)
}
case object JSUShiftRight extends AnnotatedBop {
  def toSExp = SExp('JSUShiftRight)
}
case object JSLessThan extends JSBop {
  def toSExp = SExp('JSLessThan)
}
case object JSLessThanOrEqual extends JSBop {
  def toSExp = SExp('JSLessThanOrEqual)
}
case object JSGreaterThan extends JSBop {
  def toSExp = SExp('JSGreaterThan)
}
case object JSGreaterThanOrEqual extends JSBop {
  def toSExp = SExp('JSGreaterThanOrEqual)
}
case object JSEquivalent extends JSBop {
  def toSExp = SExp('JSEquivalent)
}
case object JSNotEquivalent extends JSBop {
  def toSExp = SExp('JSNotEquivalent)
}
case object JSEqual extends JSBop {
  def toSExp = SExp('JSEqual)
}
case object JSNotEqual extends JSBop {
  def toSExp = SExp('JSNotEqual)
}
case object JSBitAnd extends AnnotatedBop {
  def toSExp = SExp('JSBitAnd)
}
case object JSBitOr extends AnnotatedBop {
  def toSExp = SExp('JSBitOr)
}
case object JSBitXOr extends AnnotatedBop {
  def toSExp = SExp('JSBitXOr)
}
case object JSLogAnd extends JSBop {
  def toSExp = SExp('JSLogAnd)
}
case object JSLogOr extends JSBop {
  def toSExp = SExp('JSLogOr)
}
case object JSIn extends JSBop {
  def toSExp = SExp('JSIn)
}
case object JSInstanceOf extends JSBop {
  def toSExp = SExp('JSInstanceOf)
}
case object JSComma extends JSBop {
  def toSExp = SExp('JSComma)
}
// end binops

// begin unops
sealed trait JSUop {
  def toSExp: SExp
}
case object JSVoid extends JSUop {
  def toSExp = SExp('JSVoid)
}
case object JSTypeof extends JSUop {
  def toSExp = SExp('JSTypeof)
}
case object JSPlus extends JSUop {
  def toSExp = SExp('JSPlus)
}
case object JSMinus extends JSUop {
  def toSExp = SExp('JSMinus)
}
case object JSBitNot extends JSUop {
  def toSExp = SExp('JSBitNot)
}
case object JSLogNot extends JSUop {
  def toSExp = SExp('JSLogNot)
}
case object JSToObj extends JSUop { // artificial
  def toSExp = SExp('JSToObj)
}
// end unops

case class BadASTException(message: String) extends Exception(message)
case class UnknownASTException(a: AstNode) extends Exception(
  "Unhandled Rhino AST: " + a.getClass.getName + " at line " + a.getLineno)
case class UnknownTokenException(t: Int) extends Exception("Unhandled token: " + t)

// intermediate for converting switch cases
// not put into the normal JS AST since this would lift a lot of needless complexity there
sealed trait JSSwitchSegment {
  def body: JSStmt
}
case class JSSwitchCase(exp: JSExp, body: JSStmt) extends JSSwitchSegment {
  def toSExp = throw new Exception("no toSExp")
}
case class JSSwitchDefault(body: JSStmt) extends JSSwitchSegment {
  def toSExp = throw new Exception("no toSExp")
}

object RhinoToJSAST {
  val regexFlagMapping =
    Map('g' -> GlobalFlag,
        'i' -> IgnoreCaseFlag,
        'm' -> MultilineFlag)

  def apply(r: AstNode, debug: Boolean = false): JSAST =
    new RhinoToJSAST(debug).apply(r)
}

class RhinoToJSAST(val debugMode: Boolean) {
  def getRegexFlags(str: Option[String]): Set[RegexFlag] =
    str.getOrElse("").map(c =>
      RhinoToJSAST.regexFlagMapping.get(c).getOrElse(
        throw BadASTException("Unknown regex flag: " + c))).toSet

  def makeSwitch(exp: JSExp, segments: Seq[JSSwitchSegment]): JSSwitch = {
    // because we need a handle on the rest of the list, foldLeft isn't appropriate
    import scala.annotation.tailrec
    @tailrec
    def process(segments: List[JSSwitchSegment], cases: List[(JSExp, JSStmt)], default: Option[JSStmt]): (List[(JSExp, JSStmt)], Option[JSStmt]) = {
      segments match {
        case JSSwitchCase(exp, body) :: rest => 
          process(rest, (exp -> body) :: cases, default)
        case JSSwitchDefault(body) :: rest => {
          if (default.isEmpty) {
            val newCases =
              cases match {
                case (exp, stmt) :: restCases =>
                  (exp -> JSSeq(List(stmt, body))) :: restCases // put the body in place
                case _ => cases // default at the top of the switch
              }
            process(rest, newCases,
                    Some(JSSeq(body :: rest.map(_.body))))
          } else {
            throw BadASTException("Multiple default statements in a switch block")
          }
        }
        case Nil => {
          // put a break at the end of the last cases to prevent running
          // through to our artificial default
          val newCases =
            cases match {
              case (exp, stmt) :: restCases =>
                (exp -> JSSeq(List(stmt, JSBreak(None)))) :: restCases
              case _ => cases // switch with only a default
            }
          (newCases.reverse, default)
        }
      }
    }

    val (cases, default) = process(segments.toList, List(), None)
    JSSwitch(exp, cases, default)
  }

  def nodeToAstNode(n: Node): AstNode =
    n match {
      case a: AstNode => a
      case _ => throw new Exception(
	"Node found where Rhino AST node expected: " + n)
    }

  def statementsToBlock(nodes: Seq[AstNode]): Block = {
    val retval = new Block()
    nodes.foreach(n => retval.addStatement(n))
    retval
  }

  def statementsToBlock(nodes: JList[AstNode]): Block =
    statementsToBlock(Option(nodes).map(_.asScala.toSeq).getOrElse(Seq()))

  def optionLabel(name: Name): Option[JSLabel] =
    Option(name).map(n => JSLabel(n.getIdentifier))

  def numString(num: Double): String = {
    val numInt = num.toInt
    if (num == numInt)
      numInt.toString
    else
      num.toString
  }

  def toUnop(op: Int): JSUop =
    op match {
      case Token.VOID => JSVoid
      case Token.TYPEOF => JSTypeof
      case Token.POS => JSPlus
      case Token.NEG => JSMinus 
      case Token.BITNOT => JSBitNot
      case Token.NOT => JSLogNot
      case _ => throw UnknownTokenException(op)
    }

  def toBinop(op: Int): JSBop =
    op match {
      case Token.ADD => JSAdd
      case Token.SUB => JSSub
      case Token.MUL => JSMul
      case Token.DIV => JSDiv
      case Token.MOD => JSMod
      case Token.LSH => JSShiftLeft
      case Token.RSH => JSShiftRight
      case Token.URSH => JSUShiftRight
      case Token.LT => JSLessThan
      case Token.LE => JSLessThanOrEqual
      case Token.GT => JSGreaterThan
      case Token.GE => JSGreaterThanOrEqual
      case Token.EQ => JSEquivalent
      case Token.NE => JSNotEquivalent
      case Token.SHEQ => JSEqual
      case Token.SHNE => JSNotEqual
      case Token.BITAND => JSBitAnd
      case Token.BITOR => JSBitOr
      case Token.BITXOR => JSBitXOr
      case Token.AND => JSLogAnd
      case Token.OR => JSLogOr
      case Token.IN => JSIn
      case Token.INSTANCEOF => JSInstanceOf
      case Token.COMMA => JSComma
      case _ => throw UnknownTokenException(op)
    }

  def toLHS(j: JSAST): JSLHS =
    j match {
      case l: JSLHS => l
      case a => throw BadASTException("Non-LHS value: " + a)
    }

  def applyExp(r: AstNode): JSExp =
    apply(r) match {
      case e: JSExp => e
      case a => throw BadASTException("Non-expression value: " + a)
    }

  def applyStmt(r: AstNode): JSStmt =
    apply(r) match {
      case s: JSStmt => s
      case a => throw BadASTException("Non-statement value: " + a)
    }

  def applyPVar(r: AstNode): JSPVar =
    apply(r) match {
      case v: JSPVar => v
      case a => throw BadASTException("Not a program variable: " + a)
    }

  def applyScratch(r: AstNode): JSScratch =
    apply(r) match {
      case v: JSScratch => v
      case a => throw BadASTException("Not a scratch variable: " + a)
    }

  def apply(r: AstNode): JSAST = {
    val res = r match {
      case b: Block =>
	JSOrigSeq(b.asScala.map(n => applyStmt(nodeToAstNode(n))).toList)

      case w: WhileLoop =>
	JSWhile(applyExp(w.getCondition),
		applyStmt(w.getBody))

      case d: DoLoop =>
	JSDoWhile(applyStmt(d.getBody),
		  applyExp(d.getCondition))

      case f: ForInLoop => {
        val iterator = f.getIterator
	JSForIn(
	  apply(iterator) match {
	    case lhs: JSLHS => lhs
            case JSDecl((x, _) :: Nil) => x
	    case _ => throw UnknownASTException(iterator)
	  },
	  applyExp(f.getIteratedObject),
	  applyStmt(f.getBody))
      }

      case f: ForLoop =>
	JSFor(applyStmt(f.getInitializer),
	      applyExp(f.getCondition),
	      applyStmt(f.getIncrement),
	      applyStmt(f.getBody))

      case t: TryStatement => {
	val clauses = t.getCatchClauses
	val catchPart = 
	  clauses.size match {
	    case 0 => None
	    case 1 => {
	      val clause = clauses.get(0)
	      Some((JSPVar(clause.getVarName.getIdentifier),
		    applyStmt(clause.getBody)))
	    }
	    case _ => throw UnknownASTException(t)
	  }
	  
	JSTry(applyStmt(t.getTryBlock),
	      catchPart,
	      Option(t.getFinallyBlock).map(applyStmt))
      }
		
      case t: ThrowStatement =>
	JSThrow(applyExp(t.getExpression))

      case l: LabeledStatement => 
	JSLabeledStmt(l.getLabels.asScala.map(lbl => JSLabel(lbl.getName)),
		      applyStmt(l.getStatement))

      case b: BreakStatement =>
	JSBreak(optionLabel(b.getBreakLabel))

      case c: ContinueStatement =>
	JSContinue(optionLabel(c.getLabel))

      case r: ReturnStatement =>
	JSReturn(Option(r.getReturnValue).map(applyExp))

      case f: FunctionNode => {
	val params = f.getParams.asScala.map(node => JSPVar(node.getString))
	val body = applyStmt(f.getBody)
        f.getParent.getType match {
          case Token.BLOCK | Token.SCRIPT => {
	    val name = f.getFunctionName
	    assert(name ne null)
	    JSFunctionDecl(JSPVar(name.getIdentifier), params, body)
          }
          case _ => 
            JSFunctionExp(
              Option(f.getFunctionName).map(n => JSPVar(n.getIdentifier)),
              params,
              body)
        }
      }

      case w: WithStatement =>
	JSWith(applyExp(w.getExpression),
	       applyStmt(w.getStatement))

      case s: SwitchStatement => {
        def getBody(c: SwitchCase): Block = {
	  import java.util.ArrayList
          statementsToBlock(
            Option(c.getStatements).getOrElse(new ArrayList()))
        }

        val cases = s.getCases.asScala.map(c => {
          val body = applyStmt(getBody(c))
          if (c.isDefault)
            JSSwitchDefault(body)
          else
            JSSwitchCase(applyExp(c.getExpression), body)
        })

        makeSwitch(applyExp(s.getExpression), cases.toSeq)
      }

      case i: IfStatement =>
	JSIf(applyExp(i.getCondition),
	     applyStmt(i.getThenPart),
	     Option(i.getElsePart).map(applyStmt))

      case n: NumberLiteral =>
	JSNum(n.getNumber)

      case s: StringLiteral =>
	JSStr(s.getValue)

      case k: KeywordLiteral => 
	k.getType match {
	  case Token.TRUE => JSBool(true)
	  case Token.FALSE => JSBool(false)
	  case Token.NULL => JSNull()
	  case Token.THIS => JSThis()
	  case t => throw UnknownTokenException(t)
	}

      case n: Name =>
	JSPVar(n.getIdentifier)

      case a: Assignment => {
	val rhs = applyExp(a.getRight)

	def removeAnnotation(op: Int): Option[AnnotatedBop] =
	  op match {
	    case Token.ASSIGN_ADD => Some(JSAdd)
	    case Token.ASSIGN_SUB => Some(JSSub)
	    case Token.ASSIGN_MUL => Some(JSMul)
	    case Token.ASSIGN_DIV => Some(JSDiv)
	    case Token.ASSIGN_MOD => Some(JSMod)
	    case Token.ASSIGN_LSH => Some(JSShiftLeft)
	    case Token.ASSIGN_RSH => Some(JSShiftRight)
	    case Token.ASSIGN_URSH => Some(JSUShiftRight)
	    case Token.ASSIGN_BITAND => Some(JSBitAnd)
	    case Token.ASSIGN_BITOR => Some(JSBitOr)
	    case Token.ASSIGN_BITXOR => Some(JSBitXOr)
	    case Token.ASSIGN => None
	    case _ => throw UnknownTokenException(op)
	}

	def helper(ifAnnotated: AnnotatedBop => JSStmt, ifNotAnnotated: => JSStmt): JSStmt =
	  removeAnnotation(a.getOperator).map(ifAnnotated).getOrElse(ifNotAnnotated)

	apply(a.getLeft) match {
	  case x: JSPVar =>
	    helper(ann => JSCompoundAssign(x, ann, rhs),
		   JSSimpleAssign(x, rhs))
	  case j: JSAccess =>
	    helper(ann => JSCompoundUpdate(j, ann, rhs),
		   JSSimpleUpdate(j, rhs))
	  case _ => throw UnknownASTException(a)
	}
      }

      case r: RegExpLiteral =>
	JSRegexp(JSStr(r.getValue),
                 getRegexFlags(Option(r.getFlags)))

      case c: ConditionalExpression =>
	JSTernary(applyExp(c.getTestExpression),
		  applyExp(c.getTrueExpression),
		  applyExp(c.getFalseExpression))

      case p: PropertyGet =>
	JSAccess(applyExp(p.getTarget),
		 JSStr(p.getProperty.getIdentifier))

      case e: ElementGet =>
	JSAccess(applyExp(e.getTarget),
		 applyExp(e.getElement))

      // must be before FunctionCall, since it's a subclass of
      // FunctionCall
      case n: NewExpression => 
	JSNew(applyExp(n.getTarget),
	      n.getArguments.asScala.map(applyExp))

      case f: FunctionCall => {
	val target = applyExp(f.getTarget)
	val args = f.getArguments.asScala.map(applyExp)
	lazy val isPrintNode = 
	  target match {
	    case JSPVar("print") => true
	    case _ => false
	  }
	
	if (debugMode && isPrintNode) {
	  if (args.length != 1) {
	    throw BadASTException(
              "Saw a print node with more than one parameter at line: " +
              f.getLineno)
	  } else {
	    JSDebug(args(0))
	  }
	} else {
	  JSCall(target, args)
	}
      }

      case n: InfixExpression => {
	val left = applyExp(n.getLeft)
	val right = applyExp(n.getRight)
	n.getOperator match {
	  case Token.DOT =>
	    JSAccess(left, right)
	  case op =>
	    JSBinop(left, toBinop(op), right)
	}
      }

      case u: UnaryExpression => {
	val exp = applyExp(u.getOperand)
	lazy val asLHS = toLHS(exp)
	u.getOperator match {
	  case Token.DELPROP => 
	    JSDelete(exp)
	  case Token.INC =>
	    if (u.isPrefix) 
	      JSPrefixInc(asLHS)
	    else
	      JSPostfixInc(asLHS)
	  case Token.DEC =>
	    if (u.isPrefix) 
	      JSPrefixDec(asLHS)
	    else
	      JSPostfixDec(asLHS)
	  case op =>
	    JSUnop(toUnop(op), exp)
	}
      }

      case o: ObjectLiteral => {
	def keyToString(key: AstNode): String =
	  key match {
	    case str: StringLiteral => str.getValue
	    case n: Name => n.getIdentifier
	    case n: NumberLiteral => numString(n.getNumber)
	    case _ => throw UnknownASTException(o)
	  }

	JSObject(
	  o.getElements.asScala.map(binding =>
	    (JSStr(keyToString(binding.getLeft)),
	     applyExp(binding.getRight))))
      }

      case a: ArrayLiteral =>
	JSArray(a.getElements.asScala.map(applyExp))

      case p: ParenthesizedExpression =>
	apply(p.getExpression)

      case a: AstRoot =>
	apply(statementsToBlock(a.getStatements))

      case e: ExpressionStatement =>
	apply(e.getExpression)

      case v: VariableDeclaration => 
	JSDecl(v.getVariables.asScala.toList.map(init =>
	  (applyPVar(init.getTarget),
	   Option(init.getInitializer).map(applyExp))))

      case e: EmptyExpression =>
	JSEmpty()

      case s: Scope =>
	apply(statementsToBlock(s.asScala.map(nodeToAstNode).toSeq))
      
      case _ =>
	throw UnknownASTException(r)
    }
    res.lineno = r.getLineno
    res
  }
}
