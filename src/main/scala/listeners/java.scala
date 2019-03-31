package fett.listeners

import antlrparsers.java._
import scala.collection.mutable.{MutableList => MList}
import fett.util.utils._

case class JavaTraversalListener() extends JavaBaseListener {
  private val preorder = MList[PreOrderNode]()
  private val postorder = MList[PostOrderNode]()

  private val preorder_functions = MList[List[PreOrderNode]]()
  private var inside_function = false
  private var node_were_waiting_to_exit = PreOrderNode(Symbol(""), Loc(-1, -1))
  private var start_index = -1

  def getPreOrder: List[PreOrderNode] = preorder.toList
  def getPostOrder: List[PostOrderNode] = postorder.toList
  def getPreOrderFunctions: List[List[PreOrderNode]] = preorder_functions.toList

  override def enterMethodDeclaration(ctx: JavaParser.MethodDeclarationContext) { 
    if (!inside_function) {
      inside_function = true
      node_were_waiting_to_exit = PreOrderNode(Symbol("MethodDeclaration"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine()))
      start_index = preorder.size
    }

    preorder += PreOrderNode(Symbol("MethodDeclaration"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) 
  }
  override def exitMethodDeclaration(ctx: JavaParser.MethodDeclarationContext) { 
    if (inside_function && node_were_waiting_to_exit == PreOrderNode(Symbol("MethodDeclaration"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine()))) {
      inside_function = false
      preorder_functions += preorder.drop(start_index).toList
    }

    postorder += PostOrderNode(Symbol("MethodDeclaration"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) 
  }
  override def enterGenericMethodDeclaration(ctx: JavaParser.GenericMethodDeclarationContext) { 
    if (!inside_function) {
      inside_function = true
      node_were_waiting_to_exit = PreOrderNode(Symbol("GenericMethodDeclaration"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine()))
      start_index = preorder.size
    }

    preorder += PreOrderNode(Symbol("GenericMethodDeclaration"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) 
  }
  override def exitGenericMethodDeclaration(ctx: JavaParser.GenericMethodDeclarationContext) { 
    if (inside_function && node_were_waiting_to_exit == PreOrderNode(Symbol("GenericMethodDeclaration"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine()))) {
      inside_function = false
      preorder_functions += preorder.drop(start_index).toList
    }

    postorder += PostOrderNode(Symbol("GenericMethodDeclaration"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) 
  }
  override def enterConstructorDeclaration(ctx: JavaParser.ConstructorDeclarationContext) { 
    if (!inside_function) {
      inside_function = true
      node_were_waiting_to_exit = PreOrderNode(Symbol("ConstructorDeclaration"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine()))
      start_index = preorder.size
    }

    preorder += PreOrderNode(Symbol("ConstructorDeclaration"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) 
  }
  override def exitConstructorDeclaration(ctx: JavaParser.ConstructorDeclarationContext) { 
    if (inside_function && node_were_waiting_to_exit == PreOrderNode(Symbol("ConstructorDeclaration"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine()))) {
      inside_function = false
      preorder_functions += preorder.drop(start_index).toList
    }

    postorder += PostOrderNode(Symbol("ConstructorDeclaration"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) 
  }
  override def enterGenericConstructorDeclaration(ctx: JavaParser.GenericConstructorDeclarationContext) { 
    if (!inside_function) {
      inside_function = true
      node_were_waiting_to_exit = PreOrderNode(Symbol("GenericConstructorDeclaration"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine()))
      start_index = preorder.size
    }

    preorder += PreOrderNode(Symbol("GenericConstructorDeclaration"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) 
  }
  override def exitGenericConstructorDeclaration(ctx: JavaParser.GenericConstructorDeclarationContext) { 
    if (inside_function && node_were_waiting_to_exit == PreOrderNode(Symbol("GenericConstructorDeclaration"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine()))) {
      inside_function = false
      preorder_functions += preorder.drop(start_index).toList
    }

    postorder += PostOrderNode(Symbol("GenericConstructorDeclaration"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) 
  }

  import org.antlr.v4.runtime._
  import org.antlr.v4.runtime.tree._
  override def enterEveryRule(ctx: ParserRuleContext) { }
  override def exitEveryRule(ctx: ParserRuleContext) { }
  override def visitTerminal(node: TerminalNode) { }
  override def visitErrorNode(node: ErrorNode) { }

    override def enterCompilationUnit(ctx: JavaParser.CompilationUnitContext) { preorder += PreOrderNode(Symbol("CompilationUnit"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def exitCompilationUnit(ctx: JavaParser.CompilationUnitContext) { postorder += PostOrderNode(Symbol("CompilationUnit"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def enterPackageDeclaration(ctx: JavaParser.PackageDeclarationContext) { preorder += PreOrderNode(Symbol("PackageDeclaration"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def exitPackageDeclaration(ctx: JavaParser.PackageDeclarationContext) { postorder += PostOrderNode(Symbol("PackageDeclaration"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def enterImportDeclaration(ctx: JavaParser.ImportDeclarationContext) { preorder += PreOrderNode(Symbol("ImportDeclaration"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def exitImportDeclaration(ctx: JavaParser.ImportDeclarationContext) { postorder += PostOrderNode(Symbol("ImportDeclaration"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def enterTypeDeclaration(ctx: JavaParser.TypeDeclarationContext) { preorder += PreOrderNode(Symbol("TypeDeclaration"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def exitTypeDeclaration(ctx: JavaParser.TypeDeclarationContext) { postorder += PostOrderNode(Symbol("TypeDeclaration"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def enterModifier(ctx: JavaParser.ModifierContext) { preorder += PreOrderNode(Symbol("Modifier"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def exitModifier(ctx: JavaParser.ModifierContext) { postorder += PostOrderNode(Symbol("Modifier"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def enterClassOrInterfaceModifier(ctx: JavaParser.ClassOrInterfaceModifierContext) { preorder += PreOrderNode(Symbol("ClassOrInterfaceModifier"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def exitClassOrInterfaceModifier(ctx: JavaParser.ClassOrInterfaceModifierContext) { postorder += PostOrderNode(Symbol("ClassOrInterfaceModifier"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def enterVariableModifier(ctx: JavaParser.VariableModifierContext) { preorder += PreOrderNode(Symbol("VariableModifier"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def exitVariableModifier(ctx: JavaParser.VariableModifierContext) { postorder += PostOrderNode(Symbol("VariableModifier"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def enterClassDeclaration(ctx: JavaParser.ClassDeclarationContext) { preorder += PreOrderNode(Symbol("ClassDeclaration"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def exitClassDeclaration(ctx: JavaParser.ClassDeclarationContext) { postorder += PostOrderNode(Symbol("ClassDeclaration"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def enterTypeParameters(ctx: JavaParser.TypeParametersContext) { preorder += PreOrderNode(Symbol("TypeParameters"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def exitTypeParameters(ctx: JavaParser.TypeParametersContext) { postorder += PostOrderNode(Symbol("TypeParameters"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def enterTypeParameter(ctx: JavaParser.TypeParameterContext) { preorder += PreOrderNode(Symbol("TypeParameter"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def exitTypeParameter(ctx: JavaParser.TypeParameterContext) { postorder += PostOrderNode(Symbol("TypeParameter"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def enterTypeBound(ctx: JavaParser.TypeBoundContext) { preorder += PreOrderNode(Symbol("TypeBound"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def exitTypeBound(ctx: JavaParser.TypeBoundContext) { postorder += PostOrderNode(Symbol("TypeBound"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def enterEnumDeclaration(ctx: JavaParser.EnumDeclarationContext) { preorder += PreOrderNode(Symbol("EnumDeclaration"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def exitEnumDeclaration(ctx: JavaParser.EnumDeclarationContext) { postorder += PostOrderNode(Symbol("EnumDeclaration"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def enterEnumConstants(ctx: JavaParser.EnumConstantsContext) { preorder += PreOrderNode(Symbol("EnumConstants"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def exitEnumConstants(ctx: JavaParser.EnumConstantsContext) { postorder += PostOrderNode(Symbol("EnumConstants"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def enterEnumConstant(ctx: JavaParser.EnumConstantContext) { preorder += PreOrderNode(Symbol("EnumConstant"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def exitEnumConstant(ctx: JavaParser.EnumConstantContext) { postorder += PostOrderNode(Symbol("EnumConstant"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def enterEnumBodyDeclarations(ctx: JavaParser.EnumBodyDeclarationsContext) { preorder += PreOrderNode(Symbol("EnumBodyDeclarations"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def exitEnumBodyDeclarations(ctx: JavaParser.EnumBodyDeclarationsContext) { postorder += PostOrderNode(Symbol("EnumBodyDeclarations"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def enterInterfaceDeclaration(ctx: JavaParser.InterfaceDeclarationContext) { preorder += PreOrderNode(Symbol("InterfaceDeclaration"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def exitInterfaceDeclaration(ctx: JavaParser.InterfaceDeclarationContext) { postorder += PostOrderNode(Symbol("InterfaceDeclaration"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def enterTypeList(ctx: JavaParser.TypeListContext) { preorder += PreOrderNode(Symbol("TypeList"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def exitTypeList(ctx: JavaParser.TypeListContext) { postorder += PostOrderNode(Symbol("TypeList"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def enterClassBody(ctx: JavaParser.ClassBodyContext) { preorder += PreOrderNode(Symbol("ClassBody"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def exitClassBody(ctx: JavaParser.ClassBodyContext) { postorder += PostOrderNode(Symbol("ClassBody"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def enterInterfaceBody(ctx: JavaParser.InterfaceBodyContext) { preorder += PreOrderNode(Symbol("InterfaceBody"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def exitInterfaceBody(ctx: JavaParser.InterfaceBodyContext) { postorder += PostOrderNode(Symbol("InterfaceBody"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def enterClassBodyDeclaration(ctx: JavaParser.ClassBodyDeclarationContext) { preorder += PreOrderNode(Symbol("ClassBodyDeclaration"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def exitClassBodyDeclaration(ctx: JavaParser.ClassBodyDeclarationContext) { postorder += PostOrderNode(Symbol("ClassBodyDeclaration"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def enterMemberDeclaration(ctx: JavaParser.MemberDeclarationContext) { preorder += PreOrderNode(Symbol("MemberDeclaration"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def exitMemberDeclaration(ctx: JavaParser.MemberDeclarationContext) { postorder += PostOrderNode(Symbol("MemberDeclaration"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def enterFieldDeclaration(ctx: JavaParser.FieldDeclarationContext) { preorder += PreOrderNode(Symbol("FieldDeclaration"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def exitFieldDeclaration(ctx: JavaParser.FieldDeclarationContext) { postorder += PostOrderNode(Symbol("FieldDeclaration"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def enterInterfaceBodyDeclaration(ctx: JavaParser.InterfaceBodyDeclarationContext) { preorder += PreOrderNode(Symbol("InterfaceBodyDeclaration"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def exitInterfaceBodyDeclaration(ctx: JavaParser.InterfaceBodyDeclarationContext) { postorder += PostOrderNode(Symbol("InterfaceBodyDeclaration"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def enterInterfaceMemberDeclaration(ctx: JavaParser.InterfaceMemberDeclarationContext) { preorder += PreOrderNode(Symbol("InterfaceMemberDeclaration"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def exitInterfaceMemberDeclaration(ctx: JavaParser.InterfaceMemberDeclarationContext) { postorder += PostOrderNode(Symbol("InterfaceMemberDeclaration"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def enterConstDeclaration(ctx: JavaParser.ConstDeclarationContext) { preorder += PreOrderNode(Symbol("ConstDeclaration"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def exitConstDeclaration(ctx: JavaParser.ConstDeclarationContext) { postorder += PostOrderNode(Symbol("ConstDeclaration"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def enterConstantDeclarator(ctx: JavaParser.ConstantDeclaratorContext) { preorder += PreOrderNode(Symbol("ConstantDeclarator"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def exitConstantDeclarator(ctx: JavaParser.ConstantDeclaratorContext) { postorder += PostOrderNode(Symbol("ConstantDeclarator"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def enterInterfaceMethodDeclaration(ctx: JavaParser.InterfaceMethodDeclarationContext) { preorder += PreOrderNode(Symbol("InterfaceMethodDeclaration"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def exitInterfaceMethodDeclaration(ctx: JavaParser.InterfaceMethodDeclarationContext) { postorder += PostOrderNode(Symbol("InterfaceMethodDeclaration"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def enterGenericInterfaceMethodDeclaration(ctx: JavaParser.GenericInterfaceMethodDeclarationContext) { preorder += PreOrderNode(Symbol("GenericInterfaceMethodDeclaration"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def exitGenericInterfaceMethodDeclaration(ctx: JavaParser.GenericInterfaceMethodDeclarationContext) { postorder += PostOrderNode(Symbol("GenericInterfaceMethodDeclaration"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def enterVariableDeclarators(ctx: JavaParser.VariableDeclaratorsContext) { preorder += PreOrderNode(Symbol("VariableDeclarators"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def exitVariableDeclarators(ctx: JavaParser.VariableDeclaratorsContext) { postorder += PostOrderNode(Symbol("VariableDeclarators"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def enterVariableDeclarator(ctx: JavaParser.VariableDeclaratorContext) { preorder += PreOrderNode(Symbol("VariableDeclarator"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def exitVariableDeclarator(ctx: JavaParser.VariableDeclaratorContext) { postorder += PostOrderNode(Symbol("VariableDeclarator"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def enterVariableDeclaratorId(ctx: JavaParser.VariableDeclaratorIdContext) { preorder += PreOrderNode(Symbol("VariableDeclaratorId"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def exitVariableDeclaratorId(ctx: JavaParser.VariableDeclaratorIdContext) { postorder += PostOrderNode(Symbol("VariableDeclaratorId"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def enterVariableInitializer(ctx: JavaParser.VariableInitializerContext) { preorder += PreOrderNode(Symbol("VariableInitializer"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def exitVariableInitializer(ctx: JavaParser.VariableInitializerContext) { postorder += PostOrderNode(Symbol("VariableInitializer"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def enterArrayInitializer(ctx: JavaParser.ArrayInitializerContext) { preorder += PreOrderNode(Symbol("ArrayInitializer"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def exitArrayInitializer(ctx: JavaParser.ArrayInitializerContext) { postorder += PostOrderNode(Symbol("ArrayInitializer"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def enterEnumConstantName(ctx: JavaParser.EnumConstantNameContext) { preorder += PreOrderNode(Symbol("EnumConstantName"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def exitEnumConstantName(ctx: JavaParser.EnumConstantNameContext) { postorder += PostOrderNode(Symbol("EnumConstantName"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def enterTypeType(ctx: JavaParser.TypeTypeContext) { preorder += PreOrderNode(Symbol("TypeType"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def exitTypeType(ctx: JavaParser.TypeTypeContext) { postorder += PostOrderNode(Symbol("TypeType"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def enterClassOrInterfaceType(ctx: JavaParser.ClassOrInterfaceTypeContext) { preorder += PreOrderNode(Symbol("ClassOrInterfaceType"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def exitClassOrInterfaceType(ctx: JavaParser.ClassOrInterfaceTypeContext) { postorder += PostOrderNode(Symbol("ClassOrInterfaceType"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def enterPrimitiveType(ctx: JavaParser.PrimitiveTypeContext) { preorder += PreOrderNode(Symbol("PrimitiveType"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def exitPrimitiveType(ctx: JavaParser.PrimitiveTypeContext) { postorder += PostOrderNode(Symbol("PrimitiveType"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def enterTypeArguments(ctx: JavaParser.TypeArgumentsContext) { preorder += PreOrderNode(Symbol("TypeArguments"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def exitTypeArguments(ctx: JavaParser.TypeArgumentsContext) { postorder += PostOrderNode(Symbol("TypeArguments"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def enterTypeArgument(ctx: JavaParser.TypeArgumentContext) { preorder += PreOrderNode(Symbol("TypeArgument"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def exitTypeArgument(ctx: JavaParser.TypeArgumentContext) { postorder += PostOrderNode(Symbol("TypeArgument"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def enterQualifiedNameList(ctx: JavaParser.QualifiedNameListContext) { preorder += PreOrderNode(Symbol("QualifiedNameList"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def exitQualifiedNameList(ctx: JavaParser.QualifiedNameListContext) { postorder += PostOrderNode(Symbol("QualifiedNameList"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def enterFormalParameters(ctx: JavaParser.FormalParametersContext) { preorder += PreOrderNode(Symbol("FormalParameters"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def exitFormalParameters(ctx: JavaParser.FormalParametersContext) { postorder += PostOrderNode(Symbol("FormalParameters"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def enterFormalParameterList(ctx: JavaParser.FormalParameterListContext) { preorder += PreOrderNode(Symbol("FormalParameterList"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def exitFormalParameterList(ctx: JavaParser.FormalParameterListContext) { postorder += PostOrderNode(Symbol("FormalParameterList"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def enterFormalParameter(ctx: JavaParser.FormalParameterContext) { preorder += PreOrderNode(Symbol("FormalParameter"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def exitFormalParameter(ctx: JavaParser.FormalParameterContext) { postorder += PostOrderNode(Symbol("FormalParameter"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def enterLastFormalParameter(ctx: JavaParser.LastFormalParameterContext) { preorder += PreOrderNode(Symbol("LastFormalParameter"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def exitLastFormalParameter(ctx: JavaParser.LastFormalParameterContext) { postorder += PostOrderNode(Symbol("LastFormalParameter"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def enterMethodBody(ctx: JavaParser.MethodBodyContext) { preorder += PreOrderNode(Symbol("MethodBody"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def exitMethodBody(ctx: JavaParser.MethodBodyContext) { postorder += PostOrderNode(Symbol("MethodBody"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def enterConstructorBody(ctx: JavaParser.ConstructorBodyContext) { preorder += PreOrderNode(Symbol("ConstructorBody"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def exitConstructorBody(ctx: JavaParser.ConstructorBodyContext) { postorder += PostOrderNode(Symbol("ConstructorBody"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def enterQualifiedName(ctx: JavaParser.QualifiedNameContext) { preorder += PreOrderNode(Symbol("QualifiedName"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def exitQualifiedName(ctx: JavaParser.QualifiedNameContext) { postorder += PostOrderNode(Symbol("QualifiedName"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def enterLiteral(ctx: JavaParser.LiteralContext) { preorder += PreOrderNode(Symbol("Literal"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def exitLiteral(ctx: JavaParser.LiteralContext) { postorder += PostOrderNode(Symbol("Literal"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def enterAnnotation(ctx: JavaParser.AnnotationContext) { preorder += PreOrderNode(Symbol("Annotation"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def exitAnnotation(ctx: JavaParser.AnnotationContext) { postorder += PostOrderNode(Symbol("Annotation"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def enterAnnotationName(ctx: JavaParser.AnnotationNameContext) { preorder += PreOrderNode(Symbol("AnnotationName"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def exitAnnotationName(ctx: JavaParser.AnnotationNameContext) { postorder += PostOrderNode(Symbol("AnnotationName"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def enterElementValuePairs(ctx: JavaParser.ElementValuePairsContext) { preorder += PreOrderNode(Symbol("ElementValuePairs"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def exitElementValuePairs(ctx: JavaParser.ElementValuePairsContext) { postorder += PostOrderNode(Symbol("ElementValuePairs"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def enterElementValuePair(ctx: JavaParser.ElementValuePairContext) { preorder += PreOrderNode(Symbol("ElementValuePair"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def exitElementValuePair(ctx: JavaParser.ElementValuePairContext) { postorder += PostOrderNode(Symbol("ElementValuePair"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def enterElementValue(ctx: JavaParser.ElementValueContext) { preorder += PreOrderNode(Symbol("ElementValue"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def exitElementValue(ctx: JavaParser.ElementValueContext) { postorder += PostOrderNode(Symbol("ElementValue"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def enterElementValueArrayInitializer(ctx: JavaParser.ElementValueArrayInitializerContext) { preorder += PreOrderNode(Symbol("ElementValueArrayInitializer"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def exitElementValueArrayInitializer(ctx: JavaParser.ElementValueArrayInitializerContext) { postorder += PostOrderNode(Symbol("ElementValueArrayInitializer"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def enterAnnotationTypeDeclaration(ctx: JavaParser.AnnotationTypeDeclarationContext) { preorder += PreOrderNode(Symbol("AnnotationTypeDeclaration"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def exitAnnotationTypeDeclaration(ctx: JavaParser.AnnotationTypeDeclarationContext) { postorder += PostOrderNode(Symbol("AnnotationTypeDeclaration"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def enterAnnotationTypeBody(ctx: JavaParser.AnnotationTypeBodyContext) { preorder += PreOrderNode(Symbol("AnnotationTypeBody"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def exitAnnotationTypeBody(ctx: JavaParser.AnnotationTypeBodyContext) { postorder += PostOrderNode(Symbol("AnnotationTypeBody"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def enterAnnotationTypeElementDeclaration(ctx: JavaParser.AnnotationTypeElementDeclarationContext) { preorder += PreOrderNode(Symbol("AnnotationTypeElementDeclaration"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def exitAnnotationTypeElementDeclaration(ctx: JavaParser.AnnotationTypeElementDeclarationContext) { postorder += PostOrderNode(Symbol("AnnotationTypeElementDeclaration"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def enterAnnotationTypeElementRest(ctx: JavaParser.AnnotationTypeElementRestContext) { preorder += PreOrderNode(Symbol("AnnotationTypeElementRest"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def exitAnnotationTypeElementRest(ctx: JavaParser.AnnotationTypeElementRestContext) { postorder += PostOrderNode(Symbol("AnnotationTypeElementRest"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def enterAnnotationMethodOrConstantRest(ctx: JavaParser.AnnotationMethodOrConstantRestContext) { preorder += PreOrderNode(Symbol("AnnotationMethodOrConstantRest"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def exitAnnotationMethodOrConstantRest(ctx: JavaParser.AnnotationMethodOrConstantRestContext) { postorder += PostOrderNode(Symbol("AnnotationMethodOrConstantRest"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def enterAnnotationMethodRest(ctx: JavaParser.AnnotationMethodRestContext) { preorder += PreOrderNode(Symbol("AnnotationMethodRest"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def exitAnnotationMethodRest(ctx: JavaParser.AnnotationMethodRestContext) { postorder += PostOrderNode(Symbol("AnnotationMethodRest"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def enterAnnotationConstantRest(ctx: JavaParser.AnnotationConstantRestContext) { preorder += PreOrderNode(Symbol("AnnotationConstantRest"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def exitAnnotationConstantRest(ctx: JavaParser.AnnotationConstantRestContext) { postorder += PostOrderNode(Symbol("AnnotationConstantRest"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def enterDefaultValue(ctx: JavaParser.DefaultValueContext) { preorder += PreOrderNode(Symbol("DefaultValue"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def exitDefaultValue(ctx: JavaParser.DefaultValueContext) { postorder += PostOrderNode(Symbol("DefaultValue"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def enterBlock(ctx: JavaParser.BlockContext) { preorder += PreOrderNode(Symbol("Block"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def exitBlock(ctx: JavaParser.BlockContext) { postorder += PostOrderNode(Symbol("Block"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def enterBlockStatement(ctx: JavaParser.BlockStatementContext) { preorder += PreOrderNode(Symbol("BlockStatement"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def exitBlockStatement(ctx: JavaParser.BlockStatementContext) { postorder += PostOrderNode(Symbol("BlockStatement"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def enterLocalVariableDeclarationStatement(ctx: JavaParser.LocalVariableDeclarationStatementContext) { preorder += PreOrderNode(Symbol("LocalVariableDeclarationStatement"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def exitLocalVariableDeclarationStatement(ctx: JavaParser.LocalVariableDeclarationStatementContext) { postorder += PostOrderNode(Symbol("LocalVariableDeclarationStatement"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def enterLocalVariableDeclaration(ctx: JavaParser.LocalVariableDeclarationContext) { preorder += PreOrderNode(Symbol("LocalVariableDeclaration"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def exitLocalVariableDeclaration(ctx: JavaParser.LocalVariableDeclarationContext) { postorder += PostOrderNode(Symbol("LocalVariableDeclaration"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def enterAssertStatement(ctx: JavaParser.AssertStatementContext) { preorder += PreOrderNode(Symbol("AssertStatement"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def exitAssertStatement(ctx: JavaParser.AssertStatementContext) { postorder += PostOrderNode(Symbol("AssertStatement"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def enterIfStatement(ctx: JavaParser.IfStatementContext) { preorder += PreOrderNode(Symbol("IfStatement"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def exitIfStatement(ctx: JavaParser.IfStatementContext) { postorder += PostOrderNode(Symbol("IfStatement"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def enterForLoop(ctx: JavaParser.ForLoopContext) { preorder += PreOrderNode(Symbol("ForLoop"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def exitForLoop(ctx: JavaParser.ForLoopContext) { postorder += PostOrderNode(Symbol("ForLoop"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def enterWhileLoop(ctx: JavaParser.WhileLoopContext) { preorder += PreOrderNode(Symbol("WhileLoop"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def exitWhileLoop(ctx: JavaParser.WhileLoopContext) { postorder += PostOrderNode(Symbol("WhileLoop"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def enterDoWhileLoop(ctx: JavaParser.DoWhileLoopContext) { preorder += PreOrderNode(Symbol("DoWhileLoop"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def exitDoWhileLoop(ctx: JavaParser.DoWhileLoopContext) { postorder += PostOrderNode(Symbol("DoWhileLoop"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def enterTry(ctx: JavaParser.TryContext) { preorder += PreOrderNode(Symbol("Try"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def exitTry(ctx: JavaParser.TryContext) { postorder += PostOrderNode(Symbol("Try"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def enterSwitch(ctx: JavaParser.SwitchContext) { preorder += PreOrderNode(Symbol("Switch"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def exitSwitch(ctx: JavaParser.SwitchContext) { postorder += PostOrderNode(Symbol("Switch"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def enterSynchronized(ctx: JavaParser.SynchronizedContext) { preorder += PreOrderNode(Symbol("Synchronized"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def exitSynchronized(ctx: JavaParser.SynchronizedContext) { postorder += PostOrderNode(Symbol("Synchronized"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def enterReturnStatement(ctx: JavaParser.ReturnStatementContext) { preorder += PreOrderNode(Symbol("ReturnStatement"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def exitReturnStatement(ctx: JavaParser.ReturnStatementContext) { postorder += PostOrderNode(Symbol("ReturnStatement"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def enterThrowStatement(ctx: JavaParser.ThrowStatementContext) { preorder += PreOrderNode(Symbol("ThrowStatement"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def exitThrowStatement(ctx: JavaParser.ThrowStatementContext) { postorder += PostOrderNode(Symbol("ThrowStatement"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def enterBreakStatement(ctx: JavaParser.BreakStatementContext) { preorder += PreOrderNode(Symbol("BreakStatement"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def exitBreakStatement(ctx: JavaParser.BreakStatementContext) { postorder += PostOrderNode(Symbol("BreakStatement"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def enterContinueStatement(ctx: JavaParser.ContinueStatementContext) { preorder += PreOrderNode(Symbol("ContinueStatement"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def exitContinueStatement(ctx: JavaParser.ContinueStatementContext) { postorder += PostOrderNode(Symbol("ContinueStatement"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def enterCatchClause(ctx: JavaParser.CatchClauseContext) { preorder += PreOrderNode(Symbol("CatchClause"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def exitCatchClause(ctx: JavaParser.CatchClauseContext) { postorder += PostOrderNode(Symbol("CatchClause"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def enterCatchType(ctx: JavaParser.CatchTypeContext) { preorder += PreOrderNode(Symbol("CatchType"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def exitCatchType(ctx: JavaParser.CatchTypeContext) { postorder += PostOrderNode(Symbol("CatchType"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def enterFinallyBlock(ctx: JavaParser.FinallyBlockContext) { preorder += PreOrderNode(Symbol("FinallyBlock"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def exitFinallyBlock(ctx: JavaParser.FinallyBlockContext) { postorder += PostOrderNode(Symbol("FinallyBlock"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def enterResourceSpecification(ctx: JavaParser.ResourceSpecificationContext) { preorder += PreOrderNode(Symbol("ResourceSpecification"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def exitResourceSpecification(ctx: JavaParser.ResourceSpecificationContext) { postorder += PostOrderNode(Symbol("ResourceSpecification"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def enterResources(ctx: JavaParser.ResourcesContext) { preorder += PreOrderNode(Symbol("Resources"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def exitResources(ctx: JavaParser.ResourcesContext) { postorder += PostOrderNode(Symbol("Resources"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def enterResource(ctx: JavaParser.ResourceContext) { preorder += PreOrderNode(Symbol("Resource"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def exitResource(ctx: JavaParser.ResourceContext) { postorder += PostOrderNode(Symbol("Resource"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def enterSwitchBlockStatementGroup(ctx: JavaParser.SwitchBlockStatementGroupContext) { preorder += PreOrderNode(Symbol("SwitchBlockStatementGroup"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def exitSwitchBlockStatementGroup(ctx: JavaParser.SwitchBlockStatementGroupContext) { postorder += PostOrderNode(Symbol("SwitchBlockStatementGroup"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def enterSwitchLabel(ctx: JavaParser.SwitchLabelContext) { preorder += PreOrderNode(Symbol("SwitchLabel"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def exitSwitchLabel(ctx: JavaParser.SwitchLabelContext) { postorder += PostOrderNode(Symbol("SwitchLabel"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def enterForControl(ctx: JavaParser.ForControlContext) { preorder += PreOrderNode(Symbol("ForControl"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def exitForControl(ctx: JavaParser.ForControlContext) { postorder += PostOrderNode(Symbol("ForControl"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def enterForInit(ctx: JavaParser.ForInitContext) { preorder += PreOrderNode(Symbol("ForInit"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def exitForInit(ctx: JavaParser.ForInitContext) { postorder += PostOrderNode(Symbol("ForInit"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def enterEnhancedForControl(ctx: JavaParser.EnhancedForControlContext) { preorder += PreOrderNode(Symbol("EnhancedForControl"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def exitEnhancedForControl(ctx: JavaParser.EnhancedForControlContext) { postorder += PostOrderNode(Symbol("EnhancedForControl"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def enterForUpdate(ctx: JavaParser.ForUpdateContext) { preorder += PreOrderNode(Symbol("ForUpdate"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def exitForUpdate(ctx: JavaParser.ForUpdateContext) { postorder += PostOrderNode(Symbol("ForUpdate"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def enterParExpression(ctx: JavaParser.ParExpressionContext) { preorder += PreOrderNode(Symbol("ParExpression"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def exitParExpression(ctx: JavaParser.ParExpressionContext) { postorder += PostOrderNode(Symbol("ParExpression"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def enterExpressionList(ctx: JavaParser.ExpressionListContext) { preorder += PreOrderNode(Symbol("ExpressionList"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def exitExpressionList(ctx: JavaParser.ExpressionListContext) { postorder += PostOrderNode(Symbol("ExpressionList"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def enterStatementExpression(ctx: JavaParser.StatementExpressionContext) { preorder += PreOrderNode(Symbol("StatementExpression"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def exitStatementExpression(ctx: JavaParser.StatementExpressionContext) { postorder += PostOrderNode(Symbol("StatementExpression"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def enterConstantExpression(ctx: JavaParser.ConstantExpressionContext) { preorder += PreOrderNode(Symbol("ConstantExpression"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def exitConstantExpression(ctx: JavaParser.ConstantExpressionContext) { postorder += PostOrderNode(Symbol("ConstantExpression"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def enterAdditiveExpression(ctx: JavaParser.AdditiveExpressionContext) { preorder += PreOrderNode(Symbol("AdditiveExpression"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def exitAdditiveExpression(ctx: JavaParser.AdditiveExpressionContext) { postorder += PostOrderNode(Symbol("AdditiveExpression"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def enterRelationalExpression(ctx: JavaParser.RelationalExpressionContext) { preorder += PreOrderNode(Symbol("RelationalExpression"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def exitRelationalExpression(ctx: JavaParser.RelationalExpressionContext) { postorder += PostOrderNode(Symbol("RelationalExpression"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def enterTernaryExpression(ctx: JavaParser.TernaryExpressionContext) { preorder += PreOrderNode(Symbol("TernaryExpression"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def exitTernaryExpression(ctx: JavaParser.TernaryExpressionContext) { postorder += PostOrderNode(Symbol("TernaryExpression"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def enterArrayAccess(ctx: JavaParser.ArrayAccessContext) { preorder += PreOrderNode(Symbol("ArrayAccess"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def exitArrayAccess(ctx: JavaParser.ArrayAccessContext) { postorder += PostOrderNode(Symbol("ArrayAccess"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def enterDontKnowWhatToPutHere(ctx: JavaParser.DontKnowWhatToPutHereContext) { preorder += PreOrderNode(Symbol("DontKnowWhatToPutHere"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def exitDontKnowWhatToPutHere(ctx: JavaParser.DontKnowWhatToPutHereContext) { postorder += PostOrderNode(Symbol("DontKnowWhatToPutHere"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def enterShiftExpression(ctx: JavaParser.ShiftExpressionContext) { preorder += PreOrderNode(Symbol("ShiftExpression"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def exitShiftExpression(ctx: JavaParser.ShiftExpressionContext) { postorder += PostOrderNode(Symbol("ShiftExpression"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def enterMultiplicativeExpression(ctx: JavaParser.MultiplicativeExpressionContext) { preorder += PreOrderNode(Symbol("MultiplicativeExpression"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def exitMultiplicativeExpression(ctx: JavaParser.MultiplicativeExpressionContext) { postorder += PostOrderNode(Symbol("MultiplicativeExpression"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def enterPrimary(ctx: JavaParser.PrimaryContext) { preorder += PreOrderNode(Symbol("Primary"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def exitPrimary(ctx: JavaParser.PrimaryContext) { postorder += PostOrderNode(Symbol("Primary"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def enterCreator(ctx: JavaParser.CreatorContext) { preorder += PreOrderNode(Symbol("Creator"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def exitCreator(ctx: JavaParser.CreatorContext) { postorder += PostOrderNode(Symbol("Creator"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def enterCreatedName(ctx: JavaParser.CreatedNameContext) { preorder += PreOrderNode(Symbol("CreatedName"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def exitCreatedName(ctx: JavaParser.CreatedNameContext) { postorder += PostOrderNode(Symbol("CreatedName"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def enterInnerCreator(ctx: JavaParser.InnerCreatorContext) { preorder += PreOrderNode(Symbol("InnerCreator"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def exitInnerCreator(ctx: JavaParser.InnerCreatorContext) { postorder += PostOrderNode(Symbol("InnerCreator"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def enterArrayCreatorRest(ctx: JavaParser.ArrayCreatorRestContext) { preorder += PreOrderNode(Symbol("ArrayCreatorRest"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def exitArrayCreatorRest(ctx: JavaParser.ArrayCreatorRestContext) { postorder += PostOrderNode(Symbol("ArrayCreatorRest"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def enterClassCreatorRest(ctx: JavaParser.ClassCreatorRestContext) { preorder += PreOrderNode(Symbol("ClassCreatorRest"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def exitClassCreatorRest(ctx: JavaParser.ClassCreatorRestContext) { postorder += PostOrderNode(Symbol("ClassCreatorRest"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def enterExplicitGenericInvocation(ctx: JavaParser.ExplicitGenericInvocationContext) { preorder += PreOrderNode(Symbol("ExplicitGenericInvocation"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def exitExplicitGenericInvocation(ctx: JavaParser.ExplicitGenericInvocationContext) { postorder += PostOrderNode(Symbol("ExplicitGenericInvocation"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def enterNonWildcardTypeArguments(ctx: JavaParser.NonWildcardTypeArgumentsContext) { preorder += PreOrderNode(Symbol("NonWildcardTypeArguments"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def exitNonWildcardTypeArguments(ctx: JavaParser.NonWildcardTypeArgumentsContext) { postorder += PostOrderNode(Symbol("NonWildcardTypeArguments"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def enterTypeArgumentsOrDiamond(ctx: JavaParser.TypeArgumentsOrDiamondContext) { preorder += PreOrderNode(Symbol("TypeArgumentsOrDiamond"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def exitTypeArgumentsOrDiamond(ctx: JavaParser.TypeArgumentsOrDiamondContext) { postorder += PostOrderNode(Symbol("TypeArgumentsOrDiamond"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def enterNonWildcardTypeArgumentsOrDiamond(ctx: JavaParser.NonWildcardTypeArgumentsOrDiamondContext) { preorder += PreOrderNode(Symbol("NonWildcardTypeArgumentsOrDiamond"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def exitNonWildcardTypeArgumentsOrDiamond(ctx: JavaParser.NonWildcardTypeArgumentsOrDiamondContext) { postorder += PostOrderNode(Symbol("NonWildcardTypeArgumentsOrDiamond"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def enterSuperSuffix(ctx: JavaParser.SuperSuffixContext) { preorder += PreOrderNode(Symbol("SuperSuffix"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def exitSuperSuffix(ctx: JavaParser.SuperSuffixContext) { postorder += PostOrderNode(Symbol("SuperSuffix"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def enterExplicitGenericInvocationSuffix(ctx: JavaParser.ExplicitGenericInvocationSuffixContext) { preorder += PreOrderNode(Symbol("ExplicitGenericInvocationSuffix"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def exitExplicitGenericInvocationSuffix(ctx: JavaParser.ExplicitGenericInvocationSuffixContext) { postorder += PostOrderNode(Symbol("ExplicitGenericInvocationSuffix"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def enterArguments(ctx: JavaParser.ArgumentsContext) { preorder += PreOrderNode(Symbol("Arguments"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
    override def exitArguments(ctx: JavaParser.ArgumentsContext) { postorder += PostOrderNode(Symbol("Arguments"), Loc(ctx.start.getLine(), ctx.start.getCharPositionInLine())) }
}
