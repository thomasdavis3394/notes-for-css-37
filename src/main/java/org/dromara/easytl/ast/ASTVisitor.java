package org.dromara.easytl.ast;

/**
 * AST访问者接口
 * 使用访问者模式遍历和处理AST节点
 * 
 * @param <T> 访问结果类型
 * @author EasyTL Team
 */
public interface ASTVisitor<T> {
    
    // 字面量节点访问方法
    T visitStringLiteral(StringLiteral node);
    T visitTemplateLiteral(TemplateLiteral node);
    T visitNumberLiteral(NumberLiteral node);
    T visitBooleanLiteral(BooleanLiteral node);
    T visitNullLiteral(NullLiteral node);
    T visitListLiteral(ListLiteral node);
    T visitHashLiteral(HashLiteral node);
    T visitRangeLiteral(RangeLiteral node);
    T visitRegexLiteral(RegexLiteral node);
    
    // 变量和访问节点访问方法
    T visitIdentifier(Identifier node);
    T visitMemberAccess(MemberAccess node);
    T visitIndexAccess(IndexAccess node);
    T visitArgumentAccess(ArgumentAccess node);
    T visitArgumentVariable(ArgumentVariable node);

    // JSONPath 访问节点访问方法
    T visitCurrentNode(CurrentNode node);
    T visitWildcardAccess(WildcardAccess node);
    T visitSliceAccess(SliceAccess node);
    T visitFilterAccess(FilterAccess node);
    T visitRecursiveDescentAccess(RecursiveDescentAccess node);
    T visitUnionAccess(UnionAccess node);
    
    // 调用节点访问方法
    T visitMethodCall(MethodCall node);
    
    // 运算符节点访问方法
    T visitBinaryExpression(BinaryExpression node);
    T visitUnaryExpression(UnaryExpression node);
    T visitTernaryExpression(TernaryExpression node);
    T visitElvisExpression(ElvisExpression node);
    T visitIfExpression(IfExpression node);
    T visitForExpression(ForExpression node);
    T visitCastExpression(CastExpression node);
    
    // 模板节点访问方法
    T visitTemplateNode(TemplateNode node);
    T visitTextNode(TextNode node);
    
    // 表达式节点访问方法（三种表达式类型）
    T visitSingleBraceExpressionNode(SingleBraceExpressionNode node);
    T visitDoubleBraceExpressionNode(DoubleBraceExpressionNode node);
    T visitDollarBraceExpressionNode(DollarBraceExpressionNode node);
    
    // 语句块节点访问方法
    T visitIfStatement(IfStatement node);
    T visitForStatement(ForStatement node);
    T visitSwitchStatement(SwitchStatement node);
    T visitCaseClause(CaseClause node);
    
    // 嵌入式代码块节点访问方法
    T visitLetStatement(LetStatement node);
    T visitExtendsStatement(ExtendsStatement node);
    T visitImportStatement(ImportStatement node);

    // 模板导入节点访问方法
    T visitImportTemplateStatement(ImportTemplateStatement node);

    // 导出变量节点访问方法
    T visitExportStatement(ExportStatement node);

    // 对象构造节点访问方法
    T visitObjectConstruction(ObjectConstruction node);

    // Lambda 表达式节点访问方法
    T visitLambdaExpression(LambdaExpression node);
    T visitReturnStatement(ReturnStatement node);

    // URL 字面量节点访问方法
    T visitUrlLiteral(UrlLiteral node);

    // 自增/自减和复合赋值节点访问方法
    T visitIncrementExpression(IncrementExpression node);
    T visitCompoundAssignmentExpression(CompoundAssignmentExpression node);
    T visitClassicForStatement(ClassicForStatement node);
}

