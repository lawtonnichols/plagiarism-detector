package fett.parsing

import antlrparsers.cpp14._
import antlrparsers.ecmascript._
import antlrparsers.java._
import antlrparsers.golang._
import antlrparsers.c._
import fett.util.utils._
import fett.util.trees._
import fett.listeners._
import org.antlr.v4.runtime._
import org.antlr.v4.runtime.tree.{ParseTree => AntlrParseTree, _}
import scala.collection.JavaConverters._
import scala.collection.mutable.ArraySeq

object ExceptionThrowingErrorListener extends BaseErrorListener {
    override
    def syntaxError(recognizer: Recognizer[_, _], offendingSymbol: Object,
                            line: Int, charPositionInLine: Int,
                            msg: String, e: RecognitionException)
    {
      throw new Exception("syntax error!")
    }
}

object ParsingHelpers {
  def toParseTree(t: ParserRuleContext): ParseTree = {
    def helper(t: AntlrParseTree): ParseTreeNode = {
      val start = Some(t).collect({
        case t: ParserRuleContext ⇒
          t.start
        case t: TerminalNode ⇒
          t.getSymbol
      }).map(token ⇒ {
        Loc(token.getLine, token.getCharPositionInLine)
      }).getOrElse(Loc(-1, -1))
      val end = Some(t).collect({
        case t: ParserRuleContext ⇒
          t.stop
        case t: TerminalNode ⇒
          t.getSymbol
      }).map(token ⇒ {
        Loc(token.getLine, token.getCharPositionInLine + token.getText.length)
      }).getOrElse(Loc(-1, -1))
      t match {
        case t:TerminalNode ⇒ {
          // attach token strings to the terminal nodes
          val label = t.getClass.getName
          val symbol = t.getSymbol.getText
          ParseTreeNode(label, IndexedSeq(ParseTreeNode(symbol, start=start, end=end)), start, end)
        }
        case _ ⇒ {
          val label = t.getClass.getName
          val children = (0 to t.getChildCount - 1).map((x:Int) ⇒ helper(t.getChild(x))).toIndexedSeq
          ParseTreeNode(label, children, start=start, end=end)
        }
      }
    }
    ParseTree(helper(t))
  }
}

abstract class ParserWrapper[ParserT <: Parser](protected val parserCtor: TokenStream ⇒ ParserT) {
  val resetInterval = 20
  var resetCounter = 1

  protected var lastParser = Option.empty[ParserT]
  protected def newParser(tokStream: TokenStream) = {
    lastParser = Some(parserCtor(tokStream))
    lastParser.get
  }

  /** Clear DFA once on every `resetInterval` calls */
  def periodicCleaning(): Unit = {
    resetCounter = resetCounter % resetInterval
    if (resetCounter == 0) {
      clearDFA()
    }
    resetCounter += 1
  }

  // we only need to clear cache of last parser since ANTLR
  // relies on caching DFA among parsers for performance
  def clearDFA(): Unit = {
    lastParser.foreach(_.getInterpreter.clearDFA)
    lastParser = None
  }
}

object ParseJS extends ParserWrapper(new ECMAScriptParser(_)) {
  def parse(fileName: String) = {
    periodicCleaning()
    // val fileText = Source.fromFile(fileName).getLines.mkString
    // val fileStream = new ANTLRInputStream(fileText)
    val fileStream = new ANTLRFileStream(fileName)
    val lexer = new ECMAScriptLexer(fileStream)
    lexer.addErrorListener(ExceptionThrowingErrorListener)
    val tokenStream = new CommonTokenStream(lexer)
    val parser = newParser(tokenStream)
    parser.addErrorListener(ExceptionThrowingErrorListener)
    val context = parser.program()

    context
  }

  def preOrder(parseContext: ParserRuleContext): IndexedSeq[IndexedSeq[PreOrderNode]] = {
    val walker = new IterativeParseTreeWalker()
    val listener = ECMAScriptTraversalListener()
    walker.walk(listener, parseContext)

    listener.getPreOrderFunctions.map(_.toIndexedSeq).toIndexedSeq
  }

  def main(args: Array[String]): Unit = {
    val fileName = args(0)
    val fileToSave = args(1)

    val dot = ParsingHelpers.toParseTree(parse(fileName)).toSExp.toDot
    println(dot)
    new java.io.PrintWriter(fileToSave) { write(dot); close }
    // println(preOrder(parse(fileName)).length)
  }
}

object ParseCPP extends ParserWrapper(new CPP14Parser(_)) {
  def parse(fileName: String) = {
    periodicCleaning()
    // val fileText = Source.fromFile(fileName).getLines.mkString
    // val fileStream = new ANTLRInputStream(fileText)
    val fileStream = new ANTLRFileStream(fileName)
    val lexer = new CPP14Lexer(fileStream)
    lexer.addErrorListener(ExceptionThrowingErrorListener)
    val tokenStream = new CommonTokenStream(lexer)
    val parser = newParser(tokenStream)
    parser.addErrorListener(ExceptionThrowingErrorListener)
    val context = parser.translationunit()

    context
  }

  def preOrder(parseContext: ParserRuleContext): List[List[PreOrderNode]] = {
    val walker = new IterativeParseTreeWalker()
    val listener = CPP14TraversalListener()
    walker.walk(listener, parseContext)

    listener.getPreOrderFunctions
  }

  def main(args: Array[String]): Unit = {
    val fileName = args(0)
    val fileToSave = args(1)

    val dot = ParsingHelpers.toParseTree(parse(fileName))/*.collapse*/.functionNodes(0).toSExp.toDot
    // val dot = ParsingHelpers.toParseTree(parse(fileName)).functionNodes(0).collapse.toSExp.toDot
    println(dot)
    println(ParsingHelpers.toParseTree(parse(fileName)).collapse.functionNodes(0).toSExp.preOrder)
    new java.io.PrintWriter(fileToSave) { write(dot); close }
    // println(preOrder(parse(fileName)).length)
  }
}

object ParseJava extends ParserWrapper(new JavaParser(_)) {
  def parse(fileName: String) = {
    periodicCleaning()
    // val fileText = Source.fromFile(fileName).getLines.mkString
    // val fileStream = new ANTLRInputStream(fileText)
    val fileStream = new ANTLRFileStream(fileName)
    val lexer = new JavaLexer(fileStream)
    lexer.addErrorListener(ExceptionThrowingErrorListener)
    val tokenStream = new CommonTokenStream(lexer)
    val parser = newParser(tokenStream)
    parser.addErrorListener(ExceptionThrowingErrorListener)
    val context = parser.compilationUnit()

    context
  }

  def preOrder(parseContext: ParserRuleContext): List[List[PreOrderNode]] = {
    val walker = new IterativeParseTreeWalker()
    val listener = JavaTraversalListener()
    walker.walk(listener, parseContext)

    listener.getPreOrderFunctions
  }

  def main(args: Array[String]): Unit = {
    val fileName = args(0)
    val fileToSave = args(1)

    val dot = ParsingHelpers.toParseTree(parse(fileName)).functionNodes(0).toSExp.toDot
    println(dot)
    // println(ParsingHelpers.toParseTree(parse(fileName)).collapse.functionNodes(0).toSExp.preOrder)
    new java.io.PrintWriter(fileToSave) { write(dot); close }
    // println(preOrder(parse(fileName)).length)
  }
}

object ParseGo extends ParserWrapper(new GolangParser(_)) {
  def parse(fileName: String) = {
    periodicCleaning()
    // val fileText = Source.fromFile(fileName).getLines.mkString
    // val fileStream = new ANTLRInputStream(fileText)
    val fileStream = new ANTLRFileStream(fileName)
    val lexer = new GolangLexer(fileStream)
    lexer.addErrorListener(ExceptionThrowingErrorListener)
    val tokenStream = new CommonTokenStream(lexer)
    val parser = newParser(tokenStream)
    lastParser = Some(parser)
    parser.addErrorListener(ExceptionThrowingErrorListener)
    val context = parser.sourceFile()
    parser.getInterpreter.clearDFA()
    context
  }

  def preOrder(parseContext: ParserRuleContext): List[List[PreOrderNode]] = {
    val walker = new IterativeParseTreeWalker()
    val listener = GolangTraversalListener()
    walker.walk(listener, parseContext)

    listener.getPreOrderFunctions
  }

  def main(args: Array[String]): Unit = {
    val fileName = args(0)
    val fileToSave = args(1)

    val dot = ParsingHelpers.toParseTree(parse(fileName)).toSExp.toDot
    println(dot)
    new java.io.PrintWriter(fileToSave) { write(dot); close }
    // println(preOrder(parse(fileName)).length)
  }
}

object ParseC extends ParserWrapper(new CParser(_)) {
  def parse(fileName: String) = {
    periodicCleaning()
    // val fileText = Source.fromFile(fileName).getLines.mkString
    // val fileStream = new ANTLRInputStream(fileText)
    val fileStream = new ANTLRFileStream(fileName)
    val lexer = new CLexer(fileStream)
    lexer.addErrorListener(ExceptionThrowingErrorListener)
    val tokenStream = new CommonTokenStream(lexer)
    val parser = newParser(tokenStream)
    lastParser = Some(parser)
    parser.addErrorListener(ExceptionThrowingErrorListener)
    val context = parser.compilationUnit()
    parser.getInterpreter.clearDFA()
    context
  }

  def main(args: Array[String]): Unit = {
    val fileName = args(0)
    val fileToSave = args(1)

    val dot = ParsingHelpers.toParseTree(parse(fileName)).toSExp.toDot
    println(dot)
    new java.io.PrintWriter(fileToSave) { write(dot); close }
    // println(preOrder(parse(fileName)).length)
  }
}
