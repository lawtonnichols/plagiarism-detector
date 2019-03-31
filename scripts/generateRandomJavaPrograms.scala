import scala.util.Random

object Helpers {
  val startingChars = ('a' to 'z') ++ ('A' to 'Z') :+ '_'
  val nonStartingChars = startingChars ++ ('0' to '9')
  def randomIntInRange(start: Int, end: Int): Int = {
    Random.nextInt(end - start + 1) + start
  }

  def generateValidIdentifier(): String = {
    var length = randomIntInRange(2, 10)
    var s: String = Random.shuffle(startingChars).head.toString
    length = length - 1
    for (i <- 1 to length - 1) {
      s += Random.shuffle(nonStartingChars).head.toString
    }
    s
  }

  def addRandomWhitespace(s: String): String = {
    def randomWhitespace(): String = {
      var res = " " * randomIntInRange(1, 5) +
                "\t" * randomIntInRange(0, 3) +
                "\n" * randomIntInRange(0, 3)
      Random.shuffle(res.toList).mkString("")
    }

    var newString = ""
    for (c <- s) {
      if (c.isWhitespace)
        newString += randomWhitespace()
      else
        newString += c
    }

    newString
  }

  def swapSomeStmts(ss: List[Stmt]): List[Stmt] = {
    if (ss.length == 1) return ss

    // swaps consecutive stmts a few times
    // swap 25% of |ss| statements
    val numberofStmtsToSwap = math.max(ss.length * 25 / 100, 1)

    val a = ss.toArray
    for (i <- 1 to numberofStmtsToSwap) {
      // pick an index i between 0 and a.length - 2, and swap indices i and i+1
      val idx = randomIntInRange(0, a.length - 2)
      val temp = a(idx)
      a(idx) = a(idx+1)
      a(idx+1) = temp
    }

    a.toList
  }

  def insertExtraStmtsRandomly(orig: List[Stmt], newStmts: List[Stmt]): List[Stmt] = {
    newStmts match {
      case Nil => orig
      case x :: xs => 
        val idx = randomIntInRange(0, orig.length)
        val (l: List[Stmt], r: List[Stmt]) = orig.splitAt(idx)
        val newL = l ++ List(x) ++ r
        insertExtraStmtsRandomly(newL, xs)
    }
  }

  def saveStringToFile(s: String, filename: String) {
    import java.io.PrintWriter
    new PrintWriter(filename) { write(s); close }
  }
}

case class Class(name: String, funcs: List[Func]) {
  override def toString = 
    s"public class $name {\n" +
    funcs.map(_.toString).mkString("\n") + 
    "}"

  def plagiarize = 
    Class(Helpers.generateValidIdentifier(), 
      Random.shuffle(funcs.map(_.plagiarize)))
}
case class Argument(typename: String, varname: String) {
  override def toString = 
    s"$typename $varname"

  def plagiarize = 
    Argument(Helpers.generateValidIdentifier(), 
      Helpers.generateValidIdentifier())
}
case class Func(isPublic: Boolean, returnType: String, name: String, args: List[Argument], stmts: List[Stmt]) {
  override def toString = 
    (if (isPublic) "public" else "private") +
    s" $returnType $name(" + 
    args.map(_.toString).mkString(", ") + ") {\n" + 
    stmts.map(_.toString).mkString("\n") + 
    "}"

  def plagiarize = 
    Func(Random.nextBoolean(),
      Helpers.generateValidIdentifier(),
      Helpers.generateValidIdentifier(),
      args.map(_.plagiarize),
      Helpers.swapSomeStmts(stmts.map(_.plagiarize)))
}
trait Stmt {
  def plagiarize: Stmt
}
case class Assign(declare: Boolean, typename: String, varname: String, rhs: Exp) extends Stmt {
  override def toString = (if (declare) s"$typename " else "") + s"$varname = $rhs;"
  def toStringNoSemicolon = toString.init

  def plagiarize = 
    Assign(declare,
      Helpers.generateValidIdentifier(),
      Helpers.generateValidIdentifier(),
      rhs.plagiarize)
}
case class For(init: Assign, test: Exp, update: Assign, body: List[Stmt]) extends Stmt {
  override def toString = 
    // init already will have a ';' at the end of it
    s"for ($init $test; ${update.toStringNoSemicolon}) {\n" + 
    body.map(_.toString).mkString("\n") + 
    "}"

  def plagiarize =
    While(init.plagiarize,
      test.plagiarize,
      update.plagiarize,
      Helpers.swapSomeStmts(body.map(_.plagiarize)))
}
case class While(init: Assign, test: Exp, update: Assign, body: List[Stmt]) extends Stmt {
  override def toString = 
    s"$init\nwhile ($test) {\n" + 
    body.map(_.toString).mkString("\n") + 
    s"$update\n}"

  def plagiarize =
    For(init.plagiarize,
      test.plagiarize,
      update.plagiarize,
      Helpers.swapSomeStmts(body.map(_.plagiarize)))
}
case class IfElse(test: Exp, body: List[Stmt], elsebranch: Option[List[Stmt]]) extends Stmt {
  override def toString = 
    s"if ($test) {\n" +
    body.map(_.toString).mkString("\n") + "}" +
    (if (elsebranch.nonEmpty) "\nelse {\n" + elsebranch.get.map(_.toString).mkString("\n") + "}" else "")

  def plagiarize = {
    val newElse: Option[List[Stmt]] = elsebranch match {
      case None => None
      case Some(e) => Some(Helpers.swapSomeStmts(e.map(_.plagiarize)))
    }
    IfElse(test.plagiarize, Helpers.swapSomeStmts(body.map(_.plagiarize)), newElse)
  }
}

trait Exp {
  def plagiarize: Exp
}
case class VarExp(varname: String) extends Exp {
  override def toString = varname
  def plagiarize = VarExp(Helpers.generateValidIdentifier())
}
case class FunctionCall(funcname: String, args: List[Exp]) extends Exp {
  override def toString = s"$funcname(" + args.map(_.toString).mkString(", ") + ")"
  def plagiarize = FunctionCall(Helpers.generateValidIdentifier, args.map(_.plagiarize))
}
case class Add(e1: Exp, e2: Exp) extends Exp {
  override def toString = 
    s"$e1 + $e2"
  def plagiarize = Add(e2.plagiarize, e1.plagiarize)
}
case class Times(e1: Exp, e2: Exp) extends Exp {
  override def toString = 
    s"$e1 * $e2"
  def plagiarize = Times(e2.plagiarize, e1.plagiarize)
}
case class Lt(e1: Exp, e2: Exp) extends Exp {
  override def toString = 
    s"$e1 < $e2"
  def plagiarize = Gt(e2.plagiarize, e1.plagiarize)
}
case class Gt(e1: Exp, e2: Exp) extends Exp {
  override def toString = 
    s"$e1 > $e2"
  def plagiarize = Lt(e2.plagiarize, e1.plagiarize)
}

object Generator {

  def generateClass(): Class = {
    Class(Helpers.generateValidIdentifier(), (1 to Helpers.randomIntInRange(1, 8)).toList.map(_ => generateFunc()))
  }
  def generateFunc(): Func = {
    Func(Random.nextBoolean(),
      Helpers.generateValidIdentifier(),
      Helpers.generateValidIdentifier(),
      (1 to Helpers.randomIntInRange(0, 8)).toList.map(_ => generateArgument()),
      (1 to Helpers.randomIntInRange(2, 10)).toList.map(_ => generateStmt()))
  }
  def generateArgument(): Argument =
    Argument(Helpers.generateValidIdentifier(), Helpers.generateValidIdentifier())
  def generateAssign(shouldPossiblyDeclare: Boolean = true): Assign = 
    Assign(shouldPossiblyDeclare && Random.nextBoolean(),
      Helpers.generateValidIdentifier(),
      Helpers.generateValidIdentifier(),
      generateExp())
  def generateStmt(depth: Int = 2): Stmt = Helpers.randomIntInRange(1, 4) match {
    case 2 if depth > 0 => // for
      For(generateAssign(),
        generateExp(),
        generateAssign(false),
        (1 to Helpers.randomIntInRange(1, 6)).toList.map(_ => generateStmt(depth - 1)))
    case 3 if depth > 0 => // while
      While(generateAssign(),
        generateExp(),
        generateAssign(false),
        (1 to Helpers.randomIntInRange(1, 6)).toList.map(_ => generateStmt(depth - 1)))
    case 4 if depth > 0 => // ifelse
      val elsebranch = 
        if (Random.nextBoolean()) 
          Some((1 to Helpers.randomIntInRange(1, 6)).toList.map(_ => generateStmt(depth - 1)))
        else
          None
      IfElse(generateExp(),
        (1 to Helpers.randomIntInRange(1, 6)).toList.map(_ => generateStmt(depth - 1)),
        elsebranch)
    case _ => // make assign statement if 1 or depth <= 0
      generateAssign()
  }
  def generateExp(depth: Int = 2): Exp = Helpers.randomIntInRange(1, 6) match {
    case 2 if depth > 0 => 
      Add(generateExp(depth-1), generateExp(depth-1))
    case 3 if depth > 0 => 
      Times(generateExp(depth-1), generateExp(depth-1))
    case 4 if depth > 0 => 
      Lt(generateExp(depth-1), generateExp(depth-1))
    case 5 if depth > 0 => 
      Gt(generateExp(depth-1), generateExp(depth-1))
    case 6 if depth > 0 =>
      FunctionCall(Helpers.generateValidIdentifier(), (1 to Helpers.randomIntInRange(0, 3)).toList.map(_ => generateExp(depth-1)))
    case _ => // varexp if 1 or depth <= 0
      VarExp(Helpers.generateValidIdentifier())
  }

  def addExtraStatements(c: Class): Class = c match {
    case Class(n, funcs) => Class(n, funcs.map(addExtraStatements(_)))
  }
  def addExtraStatements(f: Func): Func = f match {
    case Func(a, b, c, d, stmts) => 
      Func(a, b, c, d, Helpers.insertExtraStmtsRandomly(stmts.map(addExtraStatements(_)), (1 to Helpers.randomIntInRange(0, 2)).toList.map(_ => generateStmt(0))))
  }
  def addExtraStatements(s: Stmt): Stmt = s match {
    case a: Assign => a
    case For(a, b, c, stmts) => 
      For(a, b, c, Helpers.insertExtraStmtsRandomly(stmts.map(addExtraStatements(_)), (1 to Helpers.randomIntInRange(0, 2)).toList.map(_ => generateStmt(0))))
    case While(a, b, c, stmts) => 
      While(a, b, c, Helpers.insertExtraStmtsRandomly(stmts.map(addExtraStatements(_)), (1 to Helpers.randomIntInRange(0, 2)).toList.map(_ => generateStmt(0))))
    case IfElse(test, body, elsecase) =>
      IfElse(test, 
        Helpers.insertExtraStmtsRandomly(body.map(addExtraStatements(_)), (1 to Helpers.randomIntInRange(0, 2)).toList.map(_ => generateStmt(0))),
        elsecase match {
          case Some(elsecase) => Some(Helpers.insertExtraStmtsRandomly(elsecase.map(addExtraStatements(_)), (1 to Helpers.randomIntInRange(0, 2)).toList.map(_ => generateStmt(0))))
          case None => None
        }
      )
  }

  def generateProgramAndPlagiarizedVersion(): (String, String) = {
    val prog = generateClass()
    val plagiarized = Helpers.addRandomWhitespace(addExtraStatements(prog.plagiarize).toString)
    (prog.toString, plagiarized)
  }

  def main(args: Array[String]) {
    val filename = args(0) + ".java"
    val plagiarizedFilename = args(0) + "-p.java"

    var (originalProgram, plagiarizedProgram) = generateProgramAndPlagiarizedVersion()
    while (originalProgram.lines.length > 250) {
      val res = generateProgramAndPlagiarizedVersion()
      originalProgram = res._1
      plagiarizedProgram = res._2
    }

    Helpers.saveStringToFile(originalProgram, filename)
    Helpers.saveStringToFile(plagiarizedProgram, plagiarizedFilename)
  }
}
