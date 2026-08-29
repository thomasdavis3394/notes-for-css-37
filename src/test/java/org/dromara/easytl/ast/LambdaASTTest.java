package org.dromara.easytl.ast;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Lambda AST node tests.
 *
 * @author EasyTL Team
 */
public class LambdaASTTest {

    @Test
    public void testLambdaParameterNoType() {
        LambdaParameter param = new LambdaParameter("x", null, Position.unknown());
        assertEquals("x", param.getName());
        assertNull(param.getType());
        assertFalse(param.hasType());
        assertEquals("x", param.toString());
    }

    @Test
    public void testLambdaParameterWithType() {
        LambdaParameter param = new LambdaParameter("x", "int", Position.unknown());
        assertEquals("x", param.getName());
        assertEquals("int", param.getType());
        assertTrue(param.hasType());
        assertEquals("int x", param.toString());
    }

    @Test
    public void testLambdaExpressionStructure() {
        LambdaParameter param = new LambdaParameter("x", null, Position.unknown());
        NumberLiteral body = new NumberLiteral(42, "42");

        LambdaExpression lambda = new LambdaExpression(
            Arrays.asList(param), null, body, false, Position.unknown());

        assertEquals(1, lambda.getParameterCount());
        assertEquals("x", lambda.getParameters().get(0).getName());
        assertFalse(lambda.isBlockBody());
        assertNotNull(lambda.getExpressionBody());
        assertNull(lambda.getBody());
    }

    @Test
    public void testLambdaExpressionNoParams() {
        NumberLiteral body = new NumberLiteral(42, "42");

        LambdaExpression lambda = new LambdaExpression(
            null, null, body, false, Position.unknown());

        assertEquals(0, lambda.getParameterCount());
        assertTrue(lambda.getParameters().isEmpty());
    }

    @Test
    public void testLambdaExpressionBlockBody() {
        LambdaParameter param = new LambdaParameter("x", null, Position.unknown());
        NumberLiteral expr = new NumberLiteral(1, "1");

        LambdaExpression lambda = new LambdaExpression(
            Arrays.asList(param), Arrays.asList(expr), null, true, Position.unknown());

        assertTrue(lambda.isBlockBody());
        assertNotNull(lambda.getBody());
        assertNull(lambda.getExpressionBody());
        assertEquals(1, lambda.getBody().size());
    }

    @Test
    public void testReturnStatement() {
        NumberLiteral value = new NumberLiteral(10, "10");
        ReturnStatement ret = new ReturnStatement(value, Position.unknown());

        assertTrue(ret.hasValue());
        assertEquals(10, ((NumberLiteral) ret.getValue()).getValue());
        assertEquals("ReturnStatement{value=NumberLiteral{value=10, type=Integer, raw='10'}}", ret.toString());
    }

    @Test
    public void testReturnStatementNoValue() {
        ReturnStatement ret = new ReturnStatement(null, Position.unknown());

        assertFalse(ret.hasValue());
        assertNull(ret.getValue());
        assertEquals("ReturnStatement{value=null}", ret.toString());
    }

    @Test
    public void testLambdaExpressionAccept() {
        LambdaParameter param = new LambdaParameter("x", null, Position.unknown());
        NumberLiteral body = new NumberLiteral(42, "42");

        LambdaExpression lambda = new LambdaExpression(
            Arrays.asList(param), null, body, false, Position.unknown());

        // Test accept method with a simple visitor
        ASTVisitor<String> visitor = new ASTVisitor<String>() {
            @Override
            public String visitLambdaExpression(LambdaExpression node) {
                return "lambda:" + node.getParameterCount();
            }
            // Stub implementations for other methods
            @Override public String visitStringLiteral(StringLiteral node) { return null; }
            @Override public String visitTemplateLiteral(TemplateLiteral node) { return null; }
            @Override public String visitNumberLiteral(NumberLiteral node) { return null; }
            @Override public String visitBooleanLiteral(BooleanLiteral node) { return null; }
            @Override public String visitNullLiteral(NullLiteral node) { return null; }
            @Override public String visitListLiteral(ListLiteral node) { return null; }
            @Override public String visitHashLiteral(HashLiteral node) { return null; }
            @Override public String visitRangeLiteral(RangeLiteral node) { return null; }
            @Override public String visitRegexLiteral(RegexLiteral node) { return null; }
            @Override public String visitIdentifier(Identifier node) { return null; }
            @Override public String visitMemberAccess(MemberAccess node) { return null; }
            @Override public String visitIndexAccess(IndexAccess node) { return null; }
            @Override public String visitCurrentNode(CurrentNode node) { return null; }
            @Override public String visitWildcardAccess(WildcardAccess node) { return null; }
            @Override public String visitSliceAccess(SliceAccess node) { return null; }
            @Override public String visitFilterAccess(FilterAccess node) { return null; }
            @Override public String visitRecursiveDescentAccess(RecursiveDescentAccess node) { return null; }
            @Override public String visitUnionAccess(UnionAccess node) { return null; }
            @Override public String visitArgumentAccess(ArgumentAccess node) { return null; }
            @Override public String visitArgumentVariable(ArgumentVariable node) { return null; }
            @Override public String visitMethodCall(MethodCall node) { return null; }
            @Override public String visitBinaryExpression(BinaryExpression node) { return null; }
            @Override public String visitUnaryExpression(UnaryExpression node) { return null; }
            @Override public String visitTernaryExpression(TernaryExpression node) { return null; }
            @Override public String visitElvisExpression(ElvisExpression node) { return null; }
            @Override public String visitIfExpression(IfExpression node) { return null; }
            @Override public String visitForExpression(ForExpression node) { return null; }
            @Override public String visitTemplateNode(TemplateNode node) { return null; }
            @Override public String visitTextNode(TextNode node) { return null; }
            @Override public String visitSingleBraceExpressionNode(SingleBraceExpressionNode node) { return null; }
            @Override public String visitDoubleBraceExpressionNode(DoubleBraceExpressionNode node) { return null; }
            @Override public String visitDollarBraceExpressionNode(DollarBraceExpressionNode node) { return null; }
            @Override public String visitIfStatement(IfStatement node) { return null; }
            @Override public String visitForStatement(ForStatement node) { return null; }
            @Override public String visitSwitchStatement(SwitchStatement node) { return null; }
            @Override public String visitCaseClause(CaseClause node) { return null; }
            @Override public String visitLetStatement(LetStatement node) { return null; }
            @Override public String visitExtendsStatement(ExtendsStatement node) { return null; }
            @Override public String visitImportStatement(ImportStatement node) { return null; }
            @Override public String visitImportTemplateStatement(ImportTemplateStatement node) { return null; }
            @Override public String visitExportStatement(ExportStatement node) { return null; }
            @Override public String visitReturnStatement(ReturnStatement node) { return null; }
            @Override public String visitUrlLiteral(UrlLiteral node) { return null; }
            @Override public String visitObjectConstruction(ObjectConstruction node) { return null; }
            @Override public String visitCastExpression(CastExpression node) { return null; }
            @Override public String visitIncrementExpression(IncrementExpression node) { return null; }
            @Override public String visitCompoundAssignmentExpression(CompoundAssignmentExpression node) { return null; }
            @Override public String visitClassicForStatement(ClassicForStatement node) { return null; }
        };

        assertEquals("lambda:1", lambda.accept(visitor));
    }

    @Test
    public void testReturnStatementAccept() {
        NumberLiteral value = new NumberLiteral(10, "10");
        ReturnStatement ret = new ReturnStatement(value, Position.unknown());

        // Test accept method
        ASTVisitor<String> visitor = new ASTVisitor<String>() {
            @Override
            public String visitReturnStatement(ReturnStatement node) {
                return "return:" + (node.hasValue() ? "hasValue" : "noValue");
            }
            // Stub implementations for other methods
            @Override public String visitLambdaExpression(LambdaExpression node) { return null; }
            @Override public String visitStringLiteral(StringLiteral node) { return null; }
            @Override public String visitTemplateLiteral(TemplateLiteral node) { return null; }
            @Override public String visitNumberLiteral(NumberLiteral node) { return null; }
            @Override public String visitBooleanLiteral(BooleanLiteral node) { return null; }
            @Override public String visitNullLiteral(NullLiteral node) { return null; }
            @Override public String visitListLiteral(ListLiteral node) { return null; }
            @Override public String visitHashLiteral(HashLiteral node) { return null; }
            @Override public String visitRangeLiteral(RangeLiteral node) { return null; }
            @Override public String visitRegexLiteral(RegexLiteral node) { return null; }
            @Override public String visitIdentifier(Identifier node) { return null; }
            @Override public String visitMemberAccess(MemberAccess node) { return null; }
            @Override public String visitIndexAccess(IndexAccess node) { return null; }
            @Override public String visitCurrentNode(CurrentNode node) { return null; }
            @Override public String visitWildcardAccess(WildcardAccess node) { return null; }
            @Override public String visitSliceAccess(SliceAccess node) { return null; }
            @Override public String visitFilterAccess(FilterAccess node) { return null; }
            @Override public String visitRecursiveDescentAccess(RecursiveDescentAccess node) { return null; }
            @Override public String visitUnionAccess(UnionAccess node) { return null; }
            @Override public String visitArgumentAccess(ArgumentAccess node) { return null; }
            @Override public String visitArgumentVariable(ArgumentVariable node) { return null; }
            @Override public String visitMethodCall(MethodCall node) { return null; }
            @Override public String visitBinaryExpression(BinaryExpression node) { return null; }
            @Override public String visitUnaryExpression(UnaryExpression node) { return null; }
            @Override public String visitTernaryExpression(TernaryExpression node) { return null; }
            @Override public String visitElvisExpression(ElvisExpression node) { return null; }
            @Override public String visitIfExpression(IfExpression node) { return null; }
            @Override public String visitForExpression(ForExpression node) { return null; }
            @Override public String visitTemplateNode(TemplateNode node) { return null; }
            @Override public String visitTextNode(TextNode node) { return null; }
            @Override public String visitSingleBraceExpressionNode(SingleBraceExpressionNode node) { return null; }
            @Override public String visitDoubleBraceExpressionNode(DoubleBraceExpressionNode node) { return null; }
            @Override public String visitDollarBraceExpressionNode(DollarBraceExpressionNode node) { return null; }
            @Override public String visitIfStatement(IfStatement node) { return null; }
            @Override public String visitForStatement(ForStatement node) { return null; }
            @Override public String visitSwitchStatement(SwitchStatement node) { return null; }
            @Override public String visitCaseClause(CaseClause node) { return null; }
            @Override public String visitLetStatement(LetStatement node) { return null; }
            @Override public String visitExtendsStatement(ExtendsStatement node) { return null; }
            @Override public String visitImportStatement(ImportStatement node) { return null; }
            @Override public String visitImportTemplateStatement(ImportTemplateStatement node) { return null; }
            @Override public String visitExportStatement(ExportStatement node) { return null; }
            @Override public String visitUrlLiteral(UrlLiteral node) { return null; }
            @Override public String visitObjectConstruction(ObjectConstruction node) { return null; }
            @Override public String visitCastExpression(CastExpression node) { return null; }
            @Override public String visitIncrementExpression(IncrementExpression node) { return null; }
            @Override public String visitCompoundAssignmentExpression(CompoundAssignmentExpression node) { return null; }
            @Override public String visitClassicForStatement(ClassicForStatement node) { return null; }
        };

        assertEquals("return:hasValue", ret.accept(visitor));
    }
}