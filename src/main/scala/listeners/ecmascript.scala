package fett.listeners

import antlrparsers.ecmascript._
import scala.collection.mutable.{MutableList => MList}
import fett.util.utils._

case class ECMAScriptTraversalListener() extends ECMAScriptBaseListener {
  private val preorder = MList[PreOrderNode]()
  private val postorder = MList[PostOrderNode]()

  private val preorder_functions = MList[List[PreOrderNode]]()
  private var inside_function = false
  private var node_were_waiting_to_exit = PreOrderNode(Symbol(""), Loc(-1, -1))
  private var start_index = -1

  def getPreOrder: List[PreOrderNode] = preorder.toList
  def getPostOrder: List[PostOrderNode] = postorder.toList
  def getPreOrderFunctions: List[List[PreOrderNode]] = preorder_functions.toList

  override def enterProgram(ctx: ECMAScriptParser.ProgramContext) { preorder += PreOrderNode(Symbol("Program"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def exitProgram(ctx: ECMAScriptParser.ProgramContext) { postorder += PostOrderNode(Symbol("Program"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def enterSourceElements(ctx: ECMAScriptParser.SourceElementsContext) { preorder += PreOrderNode(Symbol("SourceElements"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def exitSourceElements(ctx: ECMAScriptParser.SourceElementsContext) { postorder += PostOrderNode(Symbol("SourceElements"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def enterSourceElement(ctx: ECMAScriptParser.SourceElementContext) { preorder += PreOrderNode(Symbol("SourceElement"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def exitSourceElement(ctx: ECMAScriptParser.SourceElementContext) { postorder += PostOrderNode(Symbol("SourceElement"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def enterStatement(ctx: ECMAScriptParser.StatementContext) { preorder += PreOrderNode(Symbol("Statement"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def exitStatement(ctx: ECMAScriptParser.StatementContext) { postorder += PostOrderNode(Symbol("Statement"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def enterBlock(ctx: ECMAScriptParser.BlockContext) { preorder += PreOrderNode(Symbol("Block"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def exitBlock(ctx: ECMAScriptParser.BlockContext) { postorder += PostOrderNode(Symbol("Block"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def enterStatementList(ctx: ECMAScriptParser.StatementListContext) { preorder += PreOrderNode(Symbol("StatementList"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def exitStatementList(ctx: ECMAScriptParser.StatementListContext) { postorder += PostOrderNode(Symbol("StatementList"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def enterVariableStatement(ctx: ECMAScriptParser.VariableStatementContext) { preorder += PreOrderNode(Symbol("VariableStatement"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def exitVariableStatement(ctx: ECMAScriptParser.VariableStatementContext) { postorder += PostOrderNode(Symbol("VariableStatement"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def enterVariableDeclarationList(ctx: ECMAScriptParser.VariableDeclarationListContext) { preorder += PreOrderNode(Symbol("VariableDeclarationList"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def exitVariableDeclarationList(ctx: ECMAScriptParser.VariableDeclarationListContext) { postorder += PostOrderNode(Symbol("VariableDeclarationList"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def enterVariableDeclaration(ctx: ECMAScriptParser.VariableDeclarationContext) { preorder += PreOrderNode(Symbol("VariableDeclaration"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def exitVariableDeclaration(ctx: ECMAScriptParser.VariableDeclarationContext) { postorder += PostOrderNode(Symbol("VariableDeclaration"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def enterInitialiser(ctx: ECMAScriptParser.InitialiserContext) { preorder += PreOrderNode(Symbol("Initialiser"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def exitInitialiser(ctx: ECMAScriptParser.InitialiserContext) { postorder += PostOrderNode(Symbol("Initialiser"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def enterEmptyStatement(ctx: ECMAScriptParser.EmptyStatementContext) { preorder += PreOrderNode(Symbol("EmptyStatement"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def exitEmptyStatement(ctx: ECMAScriptParser.EmptyStatementContext) { postorder += PostOrderNode(Symbol("EmptyStatement"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def enterExpressionStatement(ctx: ECMAScriptParser.ExpressionStatementContext) { preorder += PreOrderNode(Symbol("ExpressionStatement"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def exitExpressionStatement(ctx: ECMAScriptParser.ExpressionStatementContext) { postorder += PostOrderNode(Symbol("ExpressionStatement"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def enterIfStatement(ctx: ECMAScriptParser.IfStatementContext) { preorder += PreOrderNode(Symbol("IfStatement"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def exitIfStatement(ctx: ECMAScriptParser.IfStatementContext) { postorder += PostOrderNode(Symbol("IfStatement"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def enterDoStatement(ctx: ECMAScriptParser.DoStatementContext) { preorder += PreOrderNode(Symbol("DoStatement"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def exitDoStatement(ctx: ECMAScriptParser.DoStatementContext) { postorder += PostOrderNode(Symbol("DoStatement"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def enterWhileStatement(ctx: ECMAScriptParser.WhileStatementContext) { preorder += PreOrderNode(Symbol("WhileStatement"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def exitWhileStatement(ctx: ECMAScriptParser.WhileStatementContext) { postorder += PostOrderNode(Symbol("WhileStatement"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def enterForStatement(ctx: ECMAScriptParser.ForStatementContext) { preorder += PreOrderNode(Symbol("ForStatement"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def exitForStatement(ctx: ECMAScriptParser.ForStatementContext) { postorder += PostOrderNode(Symbol("ForStatement"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def enterForVarStatement(ctx: ECMAScriptParser.ForVarStatementContext) { preorder += PreOrderNode(Symbol("ForVarStatement"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def exitForVarStatement(ctx: ECMAScriptParser.ForVarStatementContext) { postorder += PostOrderNode(Symbol("ForVarStatement"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def enterForInStatement(ctx: ECMAScriptParser.ForInStatementContext) { preorder += PreOrderNode(Symbol("ForInStatement"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def exitForInStatement(ctx: ECMAScriptParser.ForInStatementContext) { postorder += PostOrderNode(Symbol("ForInStatement"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def enterForVarInStatement(ctx: ECMAScriptParser.ForVarInStatementContext) { preorder += PreOrderNode(Symbol("ForVarInStatement"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def exitForVarInStatement(ctx: ECMAScriptParser.ForVarInStatementContext) { postorder += PostOrderNode(Symbol("ForVarInStatement"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def enterContinueStatement(ctx: ECMAScriptParser.ContinueStatementContext) { preorder += PreOrderNode(Symbol("ContinueStatement"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def exitContinueStatement(ctx: ECMAScriptParser.ContinueStatementContext) { postorder += PostOrderNode(Symbol("ContinueStatement"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def enterBreakStatement(ctx: ECMAScriptParser.BreakStatementContext) { preorder += PreOrderNode(Symbol("BreakStatement"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def exitBreakStatement(ctx: ECMAScriptParser.BreakStatementContext) { postorder += PostOrderNode(Symbol("BreakStatement"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def enterReturnStatement(ctx: ECMAScriptParser.ReturnStatementContext) { preorder += PreOrderNode(Symbol("ReturnStatement"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def exitReturnStatement(ctx: ECMAScriptParser.ReturnStatementContext) { postorder += PostOrderNode(Symbol("ReturnStatement"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def enterWithStatement(ctx: ECMAScriptParser.WithStatementContext) { preorder += PreOrderNode(Symbol("WithStatement"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def exitWithStatement(ctx: ECMAScriptParser.WithStatementContext) { postorder += PostOrderNode(Symbol("WithStatement"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def enterSwitchStatement(ctx: ECMAScriptParser.SwitchStatementContext) { preorder += PreOrderNode(Symbol("SwitchStatement"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def exitSwitchStatement(ctx: ECMAScriptParser.SwitchStatementContext) { postorder += PostOrderNode(Symbol("SwitchStatement"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def enterCaseBlock(ctx: ECMAScriptParser.CaseBlockContext) { preorder += PreOrderNode(Symbol("CaseBlock"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def exitCaseBlock(ctx: ECMAScriptParser.CaseBlockContext) { postorder += PostOrderNode(Symbol("CaseBlock"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def enterCaseClauses(ctx: ECMAScriptParser.CaseClausesContext) { preorder += PreOrderNode(Symbol("CaseClauses"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def exitCaseClauses(ctx: ECMAScriptParser.CaseClausesContext) { postorder += PostOrderNode(Symbol("CaseClauses"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def enterCaseClause(ctx: ECMAScriptParser.CaseClauseContext) { preorder += PreOrderNode(Symbol("CaseClause"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def exitCaseClause(ctx: ECMAScriptParser.CaseClauseContext) { postorder += PostOrderNode(Symbol("CaseClause"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def enterDefaultClause(ctx: ECMAScriptParser.DefaultClauseContext) { preorder += PreOrderNode(Symbol("DefaultClause"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def exitDefaultClause(ctx: ECMAScriptParser.DefaultClauseContext) { postorder += PostOrderNode(Symbol("DefaultClause"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def enterLabelledStatement(ctx: ECMAScriptParser.LabelledStatementContext) { preorder += PreOrderNode(Symbol("LabelledStatement"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def exitLabelledStatement(ctx: ECMAScriptParser.LabelledStatementContext) { postorder += PostOrderNode(Symbol("LabelledStatement"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def enterThrowStatement(ctx: ECMAScriptParser.ThrowStatementContext) { preorder += PreOrderNode(Symbol("ThrowStatement"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def exitThrowStatement(ctx: ECMAScriptParser.ThrowStatementContext) { postorder += PostOrderNode(Symbol("ThrowStatement"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def enterTryStatement(ctx: ECMAScriptParser.TryStatementContext) { preorder += PreOrderNode(Symbol("TryStatement"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def exitTryStatement(ctx: ECMAScriptParser.TryStatementContext) { postorder += PostOrderNode(Symbol("TryStatement"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def enterCatchProduction(ctx: ECMAScriptParser.CatchProductionContext) { preorder += PreOrderNode(Symbol("CatchProduction"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def exitCatchProduction(ctx: ECMAScriptParser.CatchProductionContext) { postorder += PostOrderNode(Symbol("CatchProduction"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def enterFinallyProduction(ctx: ECMAScriptParser.FinallyProductionContext) { preorder += PreOrderNode(Symbol("FinallyProduction"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def exitFinallyProduction(ctx: ECMAScriptParser.FinallyProductionContext) { postorder += PostOrderNode(Symbol("FinallyProduction"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def enterDebuggerStatement(ctx: ECMAScriptParser.DebuggerStatementContext) { preorder += PreOrderNode(Symbol("DebuggerStatement"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def exitDebuggerStatement(ctx: ECMAScriptParser.DebuggerStatementContext) { postorder += PostOrderNode(Symbol("DebuggerStatement"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def enterFunctionDeclaration(ctx: ECMAScriptParser.FunctionDeclarationContext) { 
		if (!inside_function) {
			inside_function = true
			node_were_waiting_to_exit = PreOrderNode(Symbol("FunctionDeclaration"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine()))
			start_index = preorder.size
		}

		preorder += PreOrderNode(Symbol("FunctionDeclaration"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine()))
	}
	override def exitFunctionDeclaration(ctx: ECMAScriptParser.FunctionDeclarationContext) { 
		if (inside_function && node_were_waiting_to_exit == PreOrderNode(Symbol("FunctionDeclaration"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine()))) {
			inside_function = false
			preorder_functions += preorder.drop(start_index).toList
		}

		postorder += PostOrderNode(Symbol("FunctionDeclaration"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) 
	}
	override def enterFormalParameterList(ctx: ECMAScriptParser.FormalParameterListContext) { preorder += PreOrderNode(Symbol("FormalParameterList"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def exitFormalParameterList(ctx: ECMAScriptParser.FormalParameterListContext) { postorder += PostOrderNode(Symbol("FormalParameterList"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def enterFunctionBody(ctx: ECMAScriptParser.FunctionBodyContext) { preorder += PreOrderNode(Symbol("FunctionBody"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def exitFunctionBody(ctx: ECMAScriptParser.FunctionBodyContext) { postorder += PostOrderNode(Symbol("FunctionBody"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def enterArrayLiteral(ctx: ECMAScriptParser.ArrayLiteralContext) { preorder += PreOrderNode(Symbol("ArrayLiteral"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def exitArrayLiteral(ctx: ECMAScriptParser.ArrayLiteralContext) { postorder += PostOrderNode(Symbol("ArrayLiteral"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def enterElementList(ctx: ECMAScriptParser.ElementListContext) { preorder += PreOrderNode(Symbol("ElementList"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def exitElementList(ctx: ECMAScriptParser.ElementListContext) { postorder += PostOrderNode(Symbol("ElementList"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def enterElision(ctx: ECMAScriptParser.ElisionContext) { preorder += PreOrderNode(Symbol("Elision"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def exitElision(ctx: ECMAScriptParser.ElisionContext) { postorder += PostOrderNode(Symbol("Elision"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def enterObjectLiteral(ctx: ECMAScriptParser.ObjectLiteralContext) { preorder += PreOrderNode(Symbol("ObjectLiteral"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def exitObjectLiteral(ctx: ECMAScriptParser.ObjectLiteralContext) { postorder += PostOrderNode(Symbol("ObjectLiteral"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def enterPropertyNameAndValueList(ctx: ECMAScriptParser.PropertyNameAndValueListContext) { preorder += PreOrderNode(Symbol("PropertyNameAndValueList"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def exitPropertyNameAndValueList(ctx: ECMAScriptParser.PropertyNameAndValueListContext) { postorder += PostOrderNode(Symbol("PropertyNameAndValueList"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def enterPropertyExpressionAssignment(ctx: ECMAScriptParser.PropertyExpressionAssignmentContext) { preorder += PreOrderNode(Symbol("PropertyExpressionAssignment"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def exitPropertyExpressionAssignment(ctx: ECMAScriptParser.PropertyExpressionAssignmentContext) { postorder += PostOrderNode(Symbol("PropertyExpressionAssignment"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def enterPropertyGetter(ctx: ECMAScriptParser.PropertyGetterContext) { preorder += PreOrderNode(Symbol("PropertyGetter"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def exitPropertyGetter(ctx: ECMAScriptParser.PropertyGetterContext) { postorder += PostOrderNode(Symbol("PropertyGetter"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def enterPropertySetter(ctx: ECMAScriptParser.PropertySetterContext) { preorder += PreOrderNode(Symbol("PropertySetter"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def exitPropertySetter(ctx: ECMAScriptParser.PropertySetterContext) { postorder += PostOrderNode(Symbol("PropertySetter"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def enterPropertyName(ctx: ECMAScriptParser.PropertyNameContext) { preorder += PreOrderNode(Symbol("PropertyName"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def exitPropertyName(ctx: ECMAScriptParser.PropertyNameContext) { postorder += PostOrderNode(Symbol("PropertyName"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def enterPropertySetParameterList(ctx: ECMAScriptParser.PropertySetParameterListContext) { preorder += PreOrderNode(Symbol("PropertySetParameterList"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def exitPropertySetParameterList(ctx: ECMAScriptParser.PropertySetParameterListContext) { postorder += PostOrderNode(Symbol("PropertySetParameterList"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def enterArguments(ctx: ECMAScriptParser.ArgumentsContext) { preorder += PreOrderNode(Symbol("Arguments"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def exitArguments(ctx: ECMAScriptParser.ArgumentsContext) { postorder += PostOrderNode(Symbol("Arguments"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def enterArgumentList(ctx: ECMAScriptParser.ArgumentListContext) { preorder += PreOrderNode(Symbol("ArgumentList"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def exitArgumentList(ctx: ECMAScriptParser.ArgumentListContext) { postorder += PostOrderNode(Symbol("ArgumentList"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def enterExpressionSequence(ctx: ECMAScriptParser.ExpressionSequenceContext) { preorder += PreOrderNode(Symbol("ExpressionSequence"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def exitExpressionSequence(ctx: ECMAScriptParser.ExpressionSequenceContext) { postorder += PostOrderNode(Symbol("ExpressionSequence"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def enterTernaryExpression(ctx: ECMAScriptParser.TernaryExpressionContext) { preorder += PreOrderNode(Symbol("TernaryExpression"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def exitTernaryExpression(ctx: ECMAScriptParser.TernaryExpressionContext) { postorder += PostOrderNode(Symbol("TernaryExpression"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def enterLogicalAndExpression(ctx: ECMAScriptParser.LogicalAndExpressionContext) { preorder += PreOrderNode(Symbol("LogicalAndExpression"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def exitLogicalAndExpression(ctx: ECMAScriptParser.LogicalAndExpressionContext) { postorder += PostOrderNode(Symbol("LogicalAndExpression"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def enterPreIncrementExpression(ctx: ECMAScriptParser.PreIncrementExpressionContext) { preorder += PreOrderNode(Symbol("PreIncrementExpression"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def exitPreIncrementExpression(ctx: ECMAScriptParser.PreIncrementExpressionContext) { postorder += PostOrderNode(Symbol("PreIncrementExpression"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def enterObjectLiteralExpression(ctx: ECMAScriptParser.ObjectLiteralExpressionContext) { preorder += PreOrderNode(Symbol("ObjectLiteralExpression"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def exitObjectLiteralExpression(ctx: ECMAScriptParser.ObjectLiteralExpressionContext) { postorder += PostOrderNode(Symbol("ObjectLiteralExpression"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def enterInExpression(ctx: ECMAScriptParser.InExpressionContext) { preorder += PreOrderNode(Symbol("InExpression"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def exitInExpression(ctx: ECMAScriptParser.InExpressionContext) { postorder += PostOrderNode(Symbol("InExpression"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def enterLogicalOrExpression(ctx: ECMAScriptParser.LogicalOrExpressionContext) { preorder += PreOrderNode(Symbol("LogicalOrExpression"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def exitLogicalOrExpression(ctx: ECMAScriptParser.LogicalOrExpressionContext) { postorder += PostOrderNode(Symbol("LogicalOrExpression"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def enterNotExpression(ctx: ECMAScriptParser.NotExpressionContext) { preorder += PreOrderNode(Symbol("NotExpression"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def exitNotExpression(ctx: ECMAScriptParser.NotExpressionContext) { postorder += PostOrderNode(Symbol("NotExpression"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def enterPreDecreaseExpression(ctx: ECMAScriptParser.PreDecreaseExpressionContext) { preorder += PreOrderNode(Symbol("PreDecreaseExpression"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def exitPreDecreaseExpression(ctx: ECMAScriptParser.PreDecreaseExpressionContext) { postorder += PostOrderNode(Symbol("PreDecreaseExpression"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def enterArgumentsExpression(ctx: ECMAScriptParser.ArgumentsExpressionContext) { preorder += PreOrderNode(Symbol("ArgumentsExpression"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def exitArgumentsExpression(ctx: ECMAScriptParser.ArgumentsExpressionContext) { postorder += PostOrderNode(Symbol("ArgumentsExpression"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def enterThisExpression(ctx: ECMAScriptParser.ThisExpressionContext) { preorder += PreOrderNode(Symbol("ThisExpression"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def exitThisExpression(ctx: ECMAScriptParser.ThisExpressionContext) { postorder += PostOrderNode(Symbol("ThisExpression"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def enterFunctionExpression(ctx: ECMAScriptParser.FunctionExpressionContext) { 
		if (!inside_function) {
			inside_function = true
			node_were_waiting_to_exit = PreOrderNode(Symbol("FunctionExpression"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine()))
			start_index = preorder.size
		}

		preorder += PreOrderNode(Symbol("FunctionExpression"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) 
	}
	override def exitFunctionExpression(ctx: ECMAScriptParser.FunctionExpressionContext) { 
		if (inside_function && node_were_waiting_to_exit == PreOrderNode(Symbol("FunctionExpression"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine()))) {
			inside_function = false
			preorder_functions += preorder.drop(start_index).toList
		}

		postorder += PostOrderNode(Symbol("FunctionExpression"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) 
	}
	override def enterUnaryMinusExpression(ctx: ECMAScriptParser.UnaryMinusExpressionContext) { preorder += PreOrderNode(Symbol("UnaryMinusExpression"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def exitUnaryMinusExpression(ctx: ECMAScriptParser.UnaryMinusExpressionContext) { postorder += PostOrderNode(Symbol("UnaryMinusExpression"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def enterAssignmentExpression(ctx: ECMAScriptParser.AssignmentExpressionContext) { preorder += PreOrderNode(Symbol("AssignmentExpression"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def exitAssignmentExpression(ctx: ECMAScriptParser.AssignmentExpressionContext) { postorder += PostOrderNode(Symbol("AssignmentExpression"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def enterPostDecreaseExpression(ctx: ECMAScriptParser.PostDecreaseExpressionContext) { preorder += PreOrderNode(Symbol("PostDecreaseExpression"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def exitPostDecreaseExpression(ctx: ECMAScriptParser.PostDecreaseExpressionContext) { postorder += PostOrderNode(Symbol("PostDecreaseExpression"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def enterTypeofExpression(ctx: ECMAScriptParser.TypeofExpressionContext) { preorder += PreOrderNode(Symbol("TypeofExpression"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def exitTypeofExpression(ctx: ECMAScriptParser.TypeofExpressionContext) { postorder += PostOrderNode(Symbol("TypeofExpression"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def enterInstanceofExpression(ctx: ECMAScriptParser.InstanceofExpressionContext) { preorder += PreOrderNode(Symbol("InstanceofExpression"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def exitInstanceofExpression(ctx: ECMAScriptParser.InstanceofExpressionContext) { postorder += PostOrderNode(Symbol("InstanceofExpression"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def enterUnaryPlusExpression(ctx: ECMAScriptParser.UnaryPlusExpressionContext) { preorder += PreOrderNode(Symbol("UnaryPlusExpression"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def exitUnaryPlusExpression(ctx: ECMAScriptParser.UnaryPlusExpressionContext) { postorder += PostOrderNode(Symbol("UnaryPlusExpression"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def enterDeleteExpression(ctx: ECMAScriptParser.DeleteExpressionContext) { preorder += PreOrderNode(Symbol("DeleteExpression"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def exitDeleteExpression(ctx: ECMAScriptParser.DeleteExpressionContext) { postorder += PostOrderNode(Symbol("DeleteExpression"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def enterEqualityExpression(ctx: ECMAScriptParser.EqualityExpressionContext) { preorder += PreOrderNode(Symbol("EqualityExpression"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def exitEqualityExpression(ctx: ECMAScriptParser.EqualityExpressionContext) { postorder += PostOrderNode(Symbol("EqualityExpression"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def enterBitXOrExpression(ctx: ECMAScriptParser.BitXOrExpressionContext) { preorder += PreOrderNode(Symbol("BitXOrExpression"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def exitBitXOrExpression(ctx: ECMAScriptParser.BitXOrExpressionContext) { postorder += PostOrderNode(Symbol("BitXOrExpression"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def enterMultiplicativeExpression(ctx: ECMAScriptParser.MultiplicativeExpressionContext) { preorder += PreOrderNode(Symbol("MultiplicativeExpression"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def exitMultiplicativeExpression(ctx: ECMAScriptParser.MultiplicativeExpressionContext) { postorder += PostOrderNode(Symbol("MultiplicativeExpression"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def enterBitShiftExpression(ctx: ECMAScriptParser.BitShiftExpressionContext) { preorder += PreOrderNode(Symbol("BitShiftExpression"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def exitBitShiftExpression(ctx: ECMAScriptParser.BitShiftExpressionContext) { postorder += PostOrderNode(Symbol("BitShiftExpression"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def enterParenthesizedExpression(ctx: ECMAScriptParser.ParenthesizedExpressionContext) { preorder += PreOrderNode(Symbol("ParenthesizedExpression"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def exitParenthesizedExpression(ctx: ECMAScriptParser.ParenthesizedExpressionContext) { postorder += PostOrderNode(Symbol("ParenthesizedExpression"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def enterAdditiveExpression(ctx: ECMAScriptParser.AdditiveExpressionContext) { preorder += PreOrderNode(Symbol("AdditiveExpression"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def exitAdditiveExpression(ctx: ECMAScriptParser.AdditiveExpressionContext) { postorder += PostOrderNode(Symbol("AdditiveExpression"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def enterRelationalExpression(ctx: ECMAScriptParser.RelationalExpressionContext) { preorder += PreOrderNode(Symbol("RelationalExpression"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def exitRelationalExpression(ctx: ECMAScriptParser.RelationalExpressionContext) { postorder += PostOrderNode(Symbol("RelationalExpression"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def enterPostIncrementExpression(ctx: ECMAScriptParser.PostIncrementExpressionContext) { preorder += PreOrderNode(Symbol("PostIncrementExpression"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def exitPostIncrementExpression(ctx: ECMAScriptParser.PostIncrementExpressionContext) { postorder += PostOrderNode(Symbol("PostIncrementExpression"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def enterBitNotExpression(ctx: ECMAScriptParser.BitNotExpressionContext) { preorder += PreOrderNode(Symbol("BitNotExpression"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def exitBitNotExpression(ctx: ECMAScriptParser.BitNotExpressionContext) { postorder += PostOrderNode(Symbol("BitNotExpression"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def enterNewExpression(ctx: ECMAScriptParser.NewExpressionContext) { preorder += PreOrderNode(Symbol("NewExpression"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def exitNewExpression(ctx: ECMAScriptParser.NewExpressionContext) { postorder += PostOrderNode(Symbol("NewExpression"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def enterLiteralExpression(ctx: ECMAScriptParser.LiteralExpressionContext) { preorder += PreOrderNode(Symbol("LiteralExpression"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def exitLiteralExpression(ctx: ECMAScriptParser.LiteralExpressionContext) { postorder += PostOrderNode(Symbol("LiteralExpression"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def enterArrayLiteralExpression(ctx: ECMAScriptParser.ArrayLiteralExpressionContext) { preorder += PreOrderNode(Symbol("ArrayLiteralExpression"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def exitArrayLiteralExpression(ctx: ECMAScriptParser.ArrayLiteralExpressionContext) { postorder += PostOrderNode(Symbol("ArrayLiteralExpression"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def enterMemberDotExpression(ctx: ECMAScriptParser.MemberDotExpressionContext) { preorder += PreOrderNode(Symbol("MemberDotExpression"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def exitMemberDotExpression(ctx: ECMAScriptParser.MemberDotExpressionContext) { postorder += PostOrderNode(Symbol("MemberDotExpression"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def enterMemberIndexExpression(ctx: ECMAScriptParser.MemberIndexExpressionContext) { preorder += PreOrderNode(Symbol("MemberIndexExpression"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def exitMemberIndexExpression(ctx: ECMAScriptParser.MemberIndexExpressionContext) { postorder += PostOrderNode(Symbol("MemberIndexExpression"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def enterIdentifierExpression(ctx: ECMAScriptParser.IdentifierExpressionContext) { preorder += PreOrderNode(Symbol("IdentifierExpression"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def exitIdentifierExpression(ctx: ECMAScriptParser.IdentifierExpressionContext) { postorder += PostOrderNode(Symbol("IdentifierExpression"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def enterBitAndExpression(ctx: ECMAScriptParser.BitAndExpressionContext) { preorder += PreOrderNode(Symbol("BitAndExpression"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def exitBitAndExpression(ctx: ECMAScriptParser.BitAndExpressionContext) { postorder += PostOrderNode(Symbol("BitAndExpression"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def enterBitOrExpression(ctx: ECMAScriptParser.BitOrExpressionContext) { preorder += PreOrderNode(Symbol("BitOrExpression"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def exitBitOrExpression(ctx: ECMAScriptParser.BitOrExpressionContext) { postorder += PostOrderNode(Symbol("BitOrExpression"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def enterAssignmentOperatorExpression(ctx: ECMAScriptParser.AssignmentOperatorExpressionContext) { preorder += PreOrderNode(Symbol("AssignmentOperatorExpression"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def exitAssignmentOperatorExpression(ctx: ECMAScriptParser.AssignmentOperatorExpressionContext) { postorder += PostOrderNode(Symbol("AssignmentOperatorExpression"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def enterVoidExpression(ctx: ECMAScriptParser.VoidExpressionContext) { preorder += PreOrderNode(Symbol("VoidExpression"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def exitVoidExpression(ctx: ECMAScriptParser.VoidExpressionContext) { postorder += PostOrderNode(Symbol("VoidExpression"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def enterAssignmentOperator(ctx: ECMAScriptParser.AssignmentOperatorContext) { preorder += PreOrderNode(Symbol("AssignmentOperator"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def exitAssignmentOperator(ctx: ECMAScriptParser.AssignmentOperatorContext) { postorder += PostOrderNode(Symbol("AssignmentOperator"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def enterLiteral(ctx: ECMAScriptParser.LiteralContext) { preorder += PreOrderNode(Symbol("Literal"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def exitLiteral(ctx: ECMAScriptParser.LiteralContext) { postorder += PostOrderNode(Symbol("Literal"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def enterNumericLiteral(ctx: ECMAScriptParser.NumericLiteralContext) { preorder += PreOrderNode(Symbol("NumericLiteral"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def exitNumericLiteral(ctx: ECMAScriptParser.NumericLiteralContext) { postorder += PostOrderNode(Symbol("NumericLiteral"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def enterIdentifierName(ctx: ECMAScriptParser.IdentifierNameContext) { preorder += PreOrderNode(Symbol("IdentifierName"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def exitIdentifierName(ctx: ECMAScriptParser.IdentifierNameContext) { postorder += PostOrderNode(Symbol("IdentifierName"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def enterReservedWord(ctx: ECMAScriptParser.ReservedWordContext) { preorder += PreOrderNode(Symbol("ReservedWord"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def exitReservedWord(ctx: ECMAScriptParser.ReservedWordContext) { postorder += PostOrderNode(Symbol("ReservedWord"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def enterKeyword(ctx: ECMAScriptParser.KeywordContext) { preorder += PreOrderNode(Symbol("Keyword"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def exitKeyword(ctx: ECMAScriptParser.KeywordContext) { postorder += PostOrderNode(Symbol("Keyword"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def enterFutureReservedWord(ctx: ECMAScriptParser.FutureReservedWordContext) { preorder += PreOrderNode(Symbol("FutureReservedWord"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def exitFutureReservedWord(ctx: ECMAScriptParser.FutureReservedWordContext) { postorder += PostOrderNode(Symbol("FutureReservedWord"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def enterGetter(ctx: ECMAScriptParser.GetterContext) { preorder += PreOrderNode(Symbol("Getter"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def exitGetter(ctx: ECMAScriptParser.GetterContext) { postorder += PostOrderNode(Symbol("Getter"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def enterSetter(ctx: ECMAScriptParser.SetterContext) { preorder += PreOrderNode(Symbol("Setter"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def exitSetter(ctx: ECMAScriptParser.SetterContext) { postorder += PostOrderNode(Symbol("Setter"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def enterEos(ctx: ECMAScriptParser.EosContext) { preorder += PreOrderNode(Symbol("Eos"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def exitEos(ctx: ECMAScriptParser.EosContext) { postorder += PostOrderNode(Symbol("Eos"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def enterEof(ctx: ECMAScriptParser.EofContext) { preorder += PreOrderNode(Symbol("Eof"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
	override def exitEof(ctx: ECMAScriptParser.EofContext) { postorder += PostOrderNode(Symbol("Eof"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }

  import org.antlr.v4.runtime._
  import org.antlr.v4.runtime.tree._
	override def enterEveryRule(ctx: ParserRuleContext) { }
	override def exitEveryRule(ctx: ParserRuleContext) { }
	override def visitTerminal(node: TerminalNode) { }
	override def visitErrorNode(node: ErrorNode) { }
}
