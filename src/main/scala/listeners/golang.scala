package fett.listeners

import antlrparsers.golang._
import scala.collection.mutable.{MutableList => MList}
import fett.util.utils._

case class GolangTraversalListener() extends GolangBaseListener {
  private val preorder = MList[PreOrderNode]()
  private val postorder = MList[PostOrderNode]()

  private val preorder_functions = MList[List[PreOrderNode]]()
  private var inside_function = false
  private var node_were_waiting_to_exit = PreOrderNode(Symbol(""), Loc(-1, -1))
  private var start_index = -1

  def getPreOrder: List[PreOrderNode] = preorder.toList
  def getPostOrder: List[PostOrderNode] = postorder.toList
  def getPreOrderFunctions: List[List[PreOrderNode]] = preorder_functions.toList

  override def enterFunctionDecl(ctx: GolangParser.FunctionDeclContext) { 
    if (!inside_function) {
      inside_function = true
      node_were_waiting_to_exit = PreOrderNode(Symbol("FunctionDecl"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine()))
      start_index = preorder.size
    }

    preorder += PreOrderNode(Symbol("FunctionDecl"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) 
  }
  override def exitFunctionDecl(ctx: GolangParser.FunctionDeclContext) { 
    if (inside_function && node_were_waiting_to_exit == PreOrderNode(Symbol("FunctionDecl"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine()))) {
      inside_function = false
      preorder_functions += preorder.drop(start_index).toList
    }

    postorder += PostOrderNode(Symbol("FunctionDecl"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) 
  }
  override def enterMethodDecl(ctx: GolangParser.MethodDeclContext) { preorder += PreOrderNode(Symbol("MethodDecl"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) 
    if (!inside_function) {
      inside_function = true
      node_were_waiting_to_exit = PreOrderNode(Symbol("MethodDecl"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine()))
      start_index = preorder.size
    }

    preorder += PreOrderNode(Symbol("MethodDecl"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) 
  }
  override def exitMethodDecl(ctx: GolangParser.MethodDeclContext) {
    if (inside_function && node_were_waiting_to_exit == PreOrderNode(Symbol("MethodDecl"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine()))) {
      inside_function = false
      preorder_functions += preorder.drop(start_index).toList
    }

    postorder += PostOrderNode(Symbol("MethodDecl"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) 
  }
  
  import org.antlr.v4.runtime._
  import org.antlr.v4.runtime.tree._
  override def enterEveryRule(ctx: ParserRuleContext) { }
  override def exitEveryRule(ctx: ParserRuleContext) { }
  override def visitTerminal(node: TerminalNode) { }
  override def visitErrorNode(node: ErrorNode) { }

  override def enterSourceFile(ctx: GolangParser.SourceFileContext) { preorder += PreOrderNode(Symbol("SourceFile"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def exitSourceFile(ctx: GolangParser.SourceFileContext) { postorder += PostOrderNode(Symbol("SourceFile"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def enterPackageClause(ctx: GolangParser.PackageClauseContext) { preorder += PreOrderNode(Symbol("PackageClause"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def exitPackageClause(ctx: GolangParser.PackageClauseContext) { postorder += PostOrderNode(Symbol("PackageClause"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def enterImportDecl(ctx: GolangParser.ImportDeclContext) { preorder += PreOrderNode(Symbol("ImportDecl"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def exitImportDecl(ctx: GolangParser.ImportDeclContext) { postorder += PostOrderNode(Symbol("ImportDecl"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def enterImportSpec(ctx: GolangParser.ImportSpecContext) { preorder += PreOrderNode(Symbol("ImportSpec"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def exitImportSpec(ctx: GolangParser.ImportSpecContext) { postorder += PostOrderNode(Symbol("ImportSpec"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def enterImportPath(ctx: GolangParser.ImportPathContext) { preorder += PreOrderNode(Symbol("ImportPath"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def exitImportPath(ctx: GolangParser.ImportPathContext) { postorder += PostOrderNode(Symbol("ImportPath"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def enterTopLevelDecl(ctx: GolangParser.TopLevelDeclContext) { preorder += PreOrderNode(Symbol("TopLevelDecl"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def exitTopLevelDecl(ctx: GolangParser.TopLevelDeclContext) { postorder += PostOrderNode(Symbol("TopLevelDecl"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def enterDeclaration(ctx: GolangParser.DeclarationContext) { preorder += PreOrderNode(Symbol("Declaration"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def exitDeclaration(ctx: GolangParser.DeclarationContext) { postorder += PostOrderNode(Symbol("Declaration"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def enterConstDecl(ctx: GolangParser.ConstDeclContext) { preorder += PreOrderNode(Symbol("ConstDecl"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def exitConstDecl(ctx: GolangParser.ConstDeclContext) { postorder += PostOrderNode(Symbol("ConstDecl"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def enterConstSpec(ctx: GolangParser.ConstSpecContext) { preorder += PreOrderNode(Symbol("ConstSpec"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def exitConstSpec(ctx: GolangParser.ConstSpecContext) { postorder += PostOrderNode(Symbol("ConstSpec"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def enterIdentifierList(ctx: GolangParser.IdentifierListContext) { preorder += PreOrderNode(Symbol("IdentifierList"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def exitIdentifierList(ctx: GolangParser.IdentifierListContext) { postorder += PostOrderNode(Symbol("IdentifierList"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def enterExpressionList(ctx: GolangParser.ExpressionListContext) { preorder += PreOrderNode(Symbol("ExpressionList"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def exitExpressionList(ctx: GolangParser.ExpressionListContext) { postorder += PostOrderNode(Symbol("ExpressionList"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def enterTypeDecl(ctx: GolangParser.TypeDeclContext) { preorder += PreOrderNode(Symbol("TypeDecl"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def exitTypeDecl(ctx: GolangParser.TypeDeclContext) { postorder += PostOrderNode(Symbol("TypeDecl"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def enterTypeSpec(ctx: GolangParser.TypeSpecContext) { preorder += PreOrderNode(Symbol("TypeSpec"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def exitTypeSpec(ctx: GolangParser.TypeSpecContext) { postorder += PostOrderNode(Symbol("TypeSpec"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def enterFunction(ctx: GolangParser.FunctionContext) { preorder += PreOrderNode(Symbol("Function"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def exitFunction(ctx: GolangParser.FunctionContext) { postorder += PostOrderNode(Symbol("Function"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def enterReceiver(ctx: GolangParser.ReceiverContext) { preorder += PreOrderNode(Symbol("Receiver"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def exitReceiver(ctx: GolangParser.ReceiverContext) { postorder += PostOrderNode(Symbol("Receiver"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def enterVarDecl(ctx: GolangParser.VarDeclContext) { preorder += PreOrderNode(Symbol("VarDecl"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def exitVarDecl(ctx: GolangParser.VarDeclContext) { postorder += PostOrderNode(Symbol("VarDecl"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def enterVarSpec(ctx: GolangParser.VarSpecContext) { preorder += PreOrderNode(Symbol("VarSpec"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def exitVarSpec(ctx: GolangParser.VarSpecContext) { postorder += PostOrderNode(Symbol("VarSpec"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def enterBlock(ctx: GolangParser.BlockContext) { preorder += PreOrderNode(Symbol("Block"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def exitBlock(ctx: GolangParser.BlockContext) { postorder += PostOrderNode(Symbol("Block"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def enterStatementList(ctx: GolangParser.StatementListContext) { preorder += PreOrderNode(Symbol("StatementList"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def exitStatementList(ctx: GolangParser.StatementListContext) { postorder += PostOrderNode(Symbol("StatementList"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def enterStatement(ctx: GolangParser.StatementContext) { preorder += PreOrderNode(Symbol("Statement"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def exitStatement(ctx: GolangParser.StatementContext) { postorder += PostOrderNode(Symbol("Statement"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def enterSimpleStmt(ctx: GolangParser.SimpleStmtContext) { preorder += PreOrderNode(Symbol("SimpleStmt"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def exitSimpleStmt(ctx: GolangParser.SimpleStmtContext) { postorder += PostOrderNode(Symbol("SimpleStmt"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def enterExpressionStmt(ctx: GolangParser.ExpressionStmtContext) { preorder += PreOrderNode(Symbol("ExpressionStmt"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def exitExpressionStmt(ctx: GolangParser.ExpressionStmtContext) { postorder += PostOrderNode(Symbol("ExpressionStmt"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def enterSendStmt(ctx: GolangParser.SendStmtContext) { preorder += PreOrderNode(Symbol("SendStmt"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def exitSendStmt(ctx: GolangParser.SendStmtContext) { postorder += PostOrderNode(Symbol("SendStmt"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def enterIncDecStmt(ctx: GolangParser.IncDecStmtContext) { preorder += PreOrderNode(Symbol("IncDecStmt"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def exitIncDecStmt(ctx: GolangParser.IncDecStmtContext) { postorder += PostOrderNode(Symbol("IncDecStmt"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def enterAssignment(ctx: GolangParser.AssignmentContext) { preorder += PreOrderNode(Symbol("Assignment"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def exitAssignment(ctx: GolangParser.AssignmentContext) { postorder += PostOrderNode(Symbol("Assignment"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def enterAssign_op(ctx: GolangParser.Assign_opContext) { preorder += PreOrderNode(Symbol("Assign_op"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def exitAssign_op(ctx: GolangParser.Assign_opContext) { postorder += PostOrderNode(Symbol("Assign_op"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def enterShortVarDecl(ctx: GolangParser.ShortVarDeclContext) { preorder += PreOrderNode(Symbol("ShortVarDecl"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def exitShortVarDecl(ctx: GolangParser.ShortVarDeclContext) { postorder += PostOrderNode(Symbol("ShortVarDecl"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def enterLabeledStmt(ctx: GolangParser.LabeledStmtContext) { preorder += PreOrderNode(Symbol("LabeledStmt"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def exitLabeledStmt(ctx: GolangParser.LabeledStmtContext) { postorder += PostOrderNode(Symbol("LabeledStmt"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def enterReturnStmt(ctx: GolangParser.ReturnStmtContext) { preorder += PreOrderNode(Symbol("ReturnStmt"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def exitReturnStmt(ctx: GolangParser.ReturnStmtContext) { postorder += PostOrderNode(Symbol("ReturnStmt"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def enterBreakStmt(ctx: GolangParser.BreakStmtContext) { preorder += PreOrderNode(Symbol("BreakStmt"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def exitBreakStmt(ctx: GolangParser.BreakStmtContext) { postorder += PostOrderNode(Symbol("BreakStmt"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def enterContinueStmt(ctx: GolangParser.ContinueStmtContext) { preorder += PreOrderNode(Symbol("ContinueStmt"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def exitContinueStmt(ctx: GolangParser.ContinueStmtContext) { postorder += PostOrderNode(Symbol("ContinueStmt"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def enterGotoStmt(ctx: GolangParser.GotoStmtContext) { preorder += PreOrderNode(Symbol("GotoStmt"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def exitGotoStmt(ctx: GolangParser.GotoStmtContext) { postorder += PostOrderNode(Symbol("GotoStmt"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def enterFallthroughStmt(ctx: GolangParser.FallthroughStmtContext) { preorder += PreOrderNode(Symbol("FallthroughStmt"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def exitFallthroughStmt(ctx: GolangParser.FallthroughStmtContext) { postorder += PostOrderNode(Symbol("FallthroughStmt"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def enterDeferStmt(ctx: GolangParser.DeferStmtContext) { preorder += PreOrderNode(Symbol("DeferStmt"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def exitDeferStmt(ctx: GolangParser.DeferStmtContext) { postorder += PostOrderNode(Symbol("DeferStmt"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def enterIfStmt(ctx: GolangParser.IfStmtContext) { preorder += PreOrderNode(Symbol("IfStmt"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def exitIfStmt(ctx: GolangParser.IfStmtContext) { postorder += PostOrderNode(Symbol("IfStmt"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def enterSwitchStmt(ctx: GolangParser.SwitchStmtContext) { preorder += PreOrderNode(Symbol("SwitchStmt"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def exitSwitchStmt(ctx: GolangParser.SwitchStmtContext) { postorder += PostOrderNode(Symbol("SwitchStmt"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def enterExprSwitchStmt(ctx: GolangParser.ExprSwitchStmtContext) { preorder += PreOrderNode(Symbol("ExprSwitchStmt"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def exitExprSwitchStmt(ctx: GolangParser.ExprSwitchStmtContext) { postorder += PostOrderNode(Symbol("ExprSwitchStmt"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def enterExprCaseClause(ctx: GolangParser.ExprCaseClauseContext) { preorder += PreOrderNode(Symbol("ExprCaseClause"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def exitExprCaseClause(ctx: GolangParser.ExprCaseClauseContext) { postorder += PostOrderNode(Symbol("ExprCaseClause"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def enterExprSwitchCase(ctx: GolangParser.ExprSwitchCaseContext) { preorder += PreOrderNode(Symbol("ExprSwitchCase"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def exitExprSwitchCase(ctx: GolangParser.ExprSwitchCaseContext) { postorder += PostOrderNode(Symbol("ExprSwitchCase"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def enterTypeSwitchStmt(ctx: GolangParser.TypeSwitchStmtContext) { preorder += PreOrderNode(Symbol("TypeSwitchStmt"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def exitTypeSwitchStmt(ctx: GolangParser.TypeSwitchStmtContext) { postorder += PostOrderNode(Symbol("TypeSwitchStmt"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def enterTypeSwitchGuard(ctx: GolangParser.TypeSwitchGuardContext) { preorder += PreOrderNode(Symbol("TypeSwitchGuard"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def exitTypeSwitchGuard(ctx: GolangParser.TypeSwitchGuardContext) { postorder += PostOrderNode(Symbol("TypeSwitchGuard"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def enterTypeCaseClause(ctx: GolangParser.TypeCaseClauseContext) { preorder += PreOrderNode(Symbol("TypeCaseClause"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def exitTypeCaseClause(ctx: GolangParser.TypeCaseClauseContext) { postorder += PostOrderNode(Symbol("TypeCaseClause"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def enterTypeSwitchCase(ctx: GolangParser.TypeSwitchCaseContext) { preorder += PreOrderNode(Symbol("TypeSwitchCase"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def exitTypeSwitchCase(ctx: GolangParser.TypeSwitchCaseContext) { postorder += PostOrderNode(Symbol("TypeSwitchCase"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def enterTypeList(ctx: GolangParser.TypeListContext) { preorder += PreOrderNode(Symbol("TypeList"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def exitTypeList(ctx: GolangParser.TypeListContext) { postorder += PostOrderNode(Symbol("TypeList"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def enterSelectStmt(ctx: GolangParser.SelectStmtContext) { preorder += PreOrderNode(Symbol("SelectStmt"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def exitSelectStmt(ctx: GolangParser.SelectStmtContext) { postorder += PostOrderNode(Symbol("SelectStmt"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def enterCommClause(ctx: GolangParser.CommClauseContext) { preorder += PreOrderNode(Symbol("CommClause"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def exitCommClause(ctx: GolangParser.CommClauseContext) { postorder += PostOrderNode(Symbol("CommClause"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def enterCommCase(ctx: GolangParser.CommCaseContext) { preorder += PreOrderNode(Symbol("CommCase"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def exitCommCase(ctx: GolangParser.CommCaseContext) { postorder += PostOrderNode(Symbol("CommCase"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def enterRecvStmt(ctx: GolangParser.RecvStmtContext) { preorder += PreOrderNode(Symbol("RecvStmt"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def exitRecvStmt(ctx: GolangParser.RecvStmtContext) { postorder += PostOrderNode(Symbol("RecvStmt"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def enterForStmt(ctx: GolangParser.ForStmtContext) { preorder += PreOrderNode(Symbol("ForStmt"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def exitForStmt(ctx: GolangParser.ForStmtContext) { postorder += PostOrderNode(Symbol("ForStmt"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def enterForClause(ctx: GolangParser.ForClauseContext) { preorder += PreOrderNode(Symbol("ForClause"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def exitForClause(ctx: GolangParser.ForClauseContext) { postorder += PostOrderNode(Symbol("ForClause"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def enterRangeClause(ctx: GolangParser.RangeClauseContext) { preorder += PreOrderNode(Symbol("RangeClause"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def exitRangeClause(ctx: GolangParser.RangeClauseContext) { postorder += PostOrderNode(Symbol("RangeClause"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def enterGoStmt(ctx: GolangParser.GoStmtContext) { preorder += PreOrderNode(Symbol("GoStmt"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def exitGoStmt(ctx: GolangParser.GoStmtContext) { postorder += PostOrderNode(Symbol("GoStmt"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def enterType(ctx: GolangParser.TypeContext) { preorder += PreOrderNode(Symbol("Type"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def exitType(ctx: GolangParser.TypeContext) { postorder += PostOrderNode(Symbol("Type"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def enterTypeName(ctx: GolangParser.TypeNameContext) { preorder += PreOrderNode(Symbol("TypeName"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def exitTypeName(ctx: GolangParser.TypeNameContext) { postorder += PostOrderNode(Symbol("TypeName"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def enterTypeLit(ctx: GolangParser.TypeLitContext) { preorder += PreOrderNode(Symbol("TypeLit"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def exitTypeLit(ctx: GolangParser.TypeLitContext) { postorder += PostOrderNode(Symbol("TypeLit"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def enterArrayType(ctx: GolangParser.ArrayTypeContext) { preorder += PreOrderNode(Symbol("ArrayType"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def exitArrayType(ctx: GolangParser.ArrayTypeContext) { postorder += PostOrderNode(Symbol("ArrayType"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def enterArrayLength(ctx: GolangParser.ArrayLengthContext) { preorder += PreOrderNode(Symbol("ArrayLength"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def exitArrayLength(ctx: GolangParser.ArrayLengthContext) { postorder += PostOrderNode(Symbol("ArrayLength"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def enterElementType(ctx: GolangParser.ElementTypeContext) { preorder += PreOrderNode(Symbol("ElementType"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def exitElementType(ctx: GolangParser.ElementTypeContext) { postorder += PostOrderNode(Symbol("ElementType"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def enterPointerType(ctx: GolangParser.PointerTypeContext) { preorder += PreOrderNode(Symbol("PointerType"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def exitPointerType(ctx: GolangParser.PointerTypeContext) { postorder += PostOrderNode(Symbol("PointerType"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def enterInterfaceType(ctx: GolangParser.InterfaceTypeContext) { preorder += PreOrderNode(Symbol("InterfaceType"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def exitInterfaceType(ctx: GolangParser.InterfaceTypeContext) { postorder += PostOrderNode(Symbol("InterfaceType"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def enterSliceType(ctx: GolangParser.SliceTypeContext) { preorder += PreOrderNode(Symbol("SliceType"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def exitSliceType(ctx: GolangParser.SliceTypeContext) { postorder += PostOrderNode(Symbol("SliceType"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def enterMapType(ctx: GolangParser.MapTypeContext) { preorder += PreOrderNode(Symbol("MapType"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def exitMapType(ctx: GolangParser.MapTypeContext) { postorder += PostOrderNode(Symbol("MapType"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def enterChannelType(ctx: GolangParser.ChannelTypeContext) { preorder += PreOrderNode(Symbol("ChannelType"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def exitChannelType(ctx: GolangParser.ChannelTypeContext) { postorder += PostOrderNode(Symbol("ChannelType"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def enterMethodSpec(ctx: GolangParser.MethodSpecContext) { preorder += PreOrderNode(Symbol("MethodSpec"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def exitMethodSpec(ctx: GolangParser.MethodSpecContext) { postorder += PostOrderNode(Symbol("MethodSpec"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def enterFunctionType(ctx: GolangParser.FunctionTypeContext) { preorder += PreOrderNode(Symbol("FunctionType"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def exitFunctionType(ctx: GolangParser.FunctionTypeContext) { postorder += PostOrderNode(Symbol("FunctionType"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def enterSignature(ctx: GolangParser.SignatureContext) { preorder += PreOrderNode(Symbol("Signature"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def exitSignature(ctx: GolangParser.SignatureContext) { postorder += PostOrderNode(Symbol("Signature"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def enterResult(ctx: GolangParser.ResultContext) { preorder += PreOrderNode(Symbol("Result"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def exitResult(ctx: GolangParser.ResultContext) { postorder += PostOrderNode(Symbol("Result"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def enterParameters(ctx: GolangParser.ParametersContext) { preorder += PreOrderNode(Symbol("Parameters"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def exitParameters(ctx: GolangParser.ParametersContext) { postorder += PostOrderNode(Symbol("Parameters"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def enterParameterList(ctx: GolangParser.ParameterListContext) { preorder += PreOrderNode(Symbol("ParameterList"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def exitParameterList(ctx: GolangParser.ParameterListContext) { postorder += PostOrderNode(Symbol("ParameterList"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def enterParameterDecl(ctx: GolangParser.ParameterDeclContext) { preorder += PreOrderNode(Symbol("ParameterDecl"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def exitParameterDecl(ctx: GolangParser.ParameterDeclContext) { postorder += PostOrderNode(Symbol("ParameterDecl"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def enterOperand(ctx: GolangParser.OperandContext) { preorder += PreOrderNode(Symbol("Operand"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def exitOperand(ctx: GolangParser.OperandContext) { postorder += PostOrderNode(Symbol("Operand"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def enterLiteral(ctx: GolangParser.LiteralContext) { preorder += PreOrderNode(Symbol("Literal"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def exitLiteral(ctx: GolangParser.LiteralContext) { postorder += PostOrderNode(Symbol("Literal"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def enterBasicLit(ctx: GolangParser.BasicLitContext) { preorder += PreOrderNode(Symbol("BasicLit"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def exitBasicLit(ctx: GolangParser.BasicLitContext) { postorder += PostOrderNode(Symbol("BasicLit"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def enterOperandName(ctx: GolangParser.OperandNameContext) { preorder += PreOrderNode(Symbol("OperandName"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def exitOperandName(ctx: GolangParser.OperandNameContext) { postorder += PostOrderNode(Symbol("OperandName"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def enterQualifiedIdent(ctx: GolangParser.QualifiedIdentContext) { preorder += PreOrderNode(Symbol("QualifiedIdent"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def exitQualifiedIdent(ctx: GolangParser.QualifiedIdentContext) { postorder += PostOrderNode(Symbol("QualifiedIdent"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def enterCompositeLit(ctx: GolangParser.CompositeLitContext) { preorder += PreOrderNode(Symbol("CompositeLit"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def exitCompositeLit(ctx: GolangParser.CompositeLitContext) { postorder += PostOrderNode(Symbol("CompositeLit"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def enterLiteralType(ctx: GolangParser.LiteralTypeContext) { preorder += PreOrderNode(Symbol("LiteralType"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def exitLiteralType(ctx: GolangParser.LiteralTypeContext) { postorder += PostOrderNode(Symbol("LiteralType"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def enterLiteralValue(ctx: GolangParser.LiteralValueContext) { preorder += PreOrderNode(Symbol("LiteralValue"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def exitLiteralValue(ctx: GolangParser.LiteralValueContext) { postorder += PostOrderNode(Symbol("LiteralValue"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def enterElementList(ctx: GolangParser.ElementListContext) { preorder += PreOrderNode(Symbol("ElementList"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def exitElementList(ctx: GolangParser.ElementListContext) { postorder += PostOrderNode(Symbol("ElementList"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def enterKeyedElement(ctx: GolangParser.KeyedElementContext) { preorder += PreOrderNode(Symbol("KeyedElement"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def exitKeyedElement(ctx: GolangParser.KeyedElementContext) { postorder += PostOrderNode(Symbol("KeyedElement"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def enterKey(ctx: GolangParser.KeyContext) { preorder += PreOrderNode(Symbol("Key"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def exitKey(ctx: GolangParser.KeyContext) { postorder += PostOrderNode(Symbol("Key"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def enterElement(ctx: GolangParser.ElementContext) { preorder += PreOrderNode(Symbol("Element"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def exitElement(ctx: GolangParser.ElementContext) { postorder += PostOrderNode(Symbol("Element"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def enterStructType(ctx: GolangParser.StructTypeContext) { preorder += PreOrderNode(Symbol("StructType"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def exitStructType(ctx: GolangParser.StructTypeContext) { postorder += PostOrderNode(Symbol("StructType"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def enterFieldDecl(ctx: GolangParser.FieldDeclContext) { preorder += PreOrderNode(Symbol("FieldDecl"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def exitFieldDecl(ctx: GolangParser.FieldDeclContext) { postorder += PostOrderNode(Symbol("FieldDecl"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def enterAnonymousField(ctx: GolangParser.AnonymousFieldContext) { preorder += PreOrderNode(Symbol("AnonymousField"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def exitAnonymousField(ctx: GolangParser.AnonymousFieldContext) { postorder += PostOrderNode(Symbol("AnonymousField"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def enterFunctionLit(ctx: GolangParser.FunctionLitContext) { preorder += PreOrderNode(Symbol("FunctionLit"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def exitFunctionLit(ctx: GolangParser.FunctionLitContext) { postorder += PostOrderNode(Symbol("FunctionLit"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def enterSelector(ctx: GolangParser.SelectorContext) { preorder += PreOrderNode(Symbol("Selector"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def exitSelector(ctx: GolangParser.SelectorContext) { postorder += PostOrderNode(Symbol("Selector"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def enterIndex(ctx: GolangParser.IndexContext) { preorder += PreOrderNode(Symbol("Index"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def exitIndex(ctx: GolangParser.IndexContext) { postorder += PostOrderNode(Symbol("Index"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def enterSlice(ctx: GolangParser.SliceContext) { preorder += PreOrderNode(Symbol("Slice"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def exitSlice(ctx: GolangParser.SliceContext) { postorder += PostOrderNode(Symbol("Slice"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def enterTypeAssertion(ctx: GolangParser.TypeAssertionContext) { preorder += PreOrderNode(Symbol("TypeAssertion"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def exitTypeAssertion(ctx: GolangParser.TypeAssertionContext) { postorder += PostOrderNode(Symbol("TypeAssertion"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def enterArguments(ctx: GolangParser.ArgumentsContext) { preorder += PreOrderNode(Symbol("Arguments"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def exitArguments(ctx: GolangParser.ArgumentsContext) { postorder += PostOrderNode(Symbol("Arguments"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def enterMethodExpr(ctx: GolangParser.MethodExprContext) { preorder += PreOrderNode(Symbol("MethodExpr"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def exitMethodExpr(ctx: GolangParser.MethodExprContext) { postorder += PostOrderNode(Symbol("MethodExpr"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def enterReceiverType(ctx: GolangParser.ReceiverTypeContext) { preorder += PreOrderNode(Symbol("ReceiverType"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def exitReceiverType(ctx: GolangParser.ReceiverTypeContext) { postorder += PostOrderNode(Symbol("ReceiverType"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def enterAdditiveExpression(ctx: GolangParser.AdditiveExpressionContext) { preorder += PreOrderNode(Symbol("AdditiveExpression"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def exitAdditiveExpression(ctx: GolangParser.AdditiveExpressionContext) { postorder += PostOrderNode(Symbol("AdditiveExpression"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def enterRelationalExpression(ctx: GolangParser.RelationalExpressionContext) { preorder += PreOrderNode(Symbol("RelationalExpression"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def exitRelationalExpression(ctx: GolangParser.RelationalExpressionContext) { postorder += PostOrderNode(Symbol("RelationalExpression"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def enterDontKnowWhatToPutHere(ctx: GolangParser.DontKnowWhatToPutHereContext) { preorder += PreOrderNode(Symbol("DontKnowWhatToPutHere"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def exitDontKnowWhatToPutHere(ctx: GolangParser.DontKnowWhatToPutHereContext) { postorder += PostOrderNode(Symbol("DontKnowWhatToPutHere"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def enterMultiplicativeExpression(ctx: GolangParser.MultiplicativeExpressionContext) { preorder += PreOrderNode(Symbol("MultiplicativeExpression"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def exitMultiplicativeExpression(ctx: GolangParser.MultiplicativeExpressionContext) { postorder += PostOrderNode(Symbol("MultiplicativeExpression"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def enterUnaryExpr(ctx: GolangParser.UnaryExprContext) { preorder += PreOrderNode(Symbol("UnaryExpr"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def exitUnaryExpr(ctx: GolangParser.UnaryExprContext) { postorder += PostOrderNode(Symbol("UnaryExpr"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def enterConversion(ctx: GolangParser.ConversionContext) { preorder += PreOrderNode(Symbol("Conversion"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def exitConversion(ctx: GolangParser.ConversionContext) { postorder += PostOrderNode(Symbol("Conversion"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def enterEos(ctx: GolangParser.EosContext) { preorder += PreOrderNode(Symbol("Eos"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
  override def exitEos(ctx: GolangParser.EosContext) { postorder += PostOrderNode(Symbol("Eos"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
}