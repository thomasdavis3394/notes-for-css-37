package org.dromara.easytl.ast;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AST节点测试类
 * 覆盖所有AST节点的接口和功能
 * 
 * @author EasyTL Team
 */
class ASTNodeTest {
    
    // ========== Position 测试 ==========
    
    @Test
    void testPositionConstruction() {
        Position pos = new Position(1, 5, 2, 10);
        assertEquals(1, pos.getStartLine());
        assertEquals(5, pos.getStartColumn());
        assertEquals(2, pos.getEndLine());
        assertEquals(10, pos.getEndColumn());
        assertFalse(pos.isUnknown());
    }
    
    @Test
    void testPositionUnknown() {
        Position pos = Position.unknown();
        assertEquals(0, pos.getStartLine());
        assertEquals(0, pos.getStartColumn());
        assertEquals(0, pos.getEndLine());
        assertEquals(0, pos.getEndColumn());
        assertTrue(pos.isUnknown());
    }
    
    @Test
    void testPositionMerge() {
        Position pos1 = new Position(1, 5, 2, 10);
        Position pos2 = new Position(2, 8, 3, 15);
        Position merged = pos1.merge(pos2);
        
        assertEquals(1, merged.getStartLine());
        assertEquals(5, merged.getStartColumn());
        assertEquals(3, merged.getEndLine());
        assertEquals(15, merged.getEndColumn());
    }
    
    @Test
    void testPositionMergeWithNull() {
        Position pos = new Position(1, 5, 2, 10);
        Position merged = pos.merge(null);
        assertSame(pos, merged);
    }
    
    @Test
    void testPositionEquals() {
        Position pos1 = new Position(1, 5, 2, 10);
        Position pos2 = new Position(1, 5, 2, 10);
        Position pos3 = new Position(1, 5, 2, 11);
        
        assertEquals(pos1, pos2);
        assertNotEquals(pos1, pos3);
        assertEquals(pos1.hashCode(), pos2.hashCode());
    }
    
    @Test
    void testPositionToString() {
        Position pos1 = new Position(1, 5, 1, 10);
        assertTrue(pos1.toString().contains("1:5-10"));
        
        Position pos2 = new Position(1, 5, 2, 10);
        assertTrue(pos2.toString().contains("1:5 - 2:10"));
        
        Position unknown = Position.unknown();
        assertTrue(unknown.toString().contains("unknown"));
    }
    
    // ========== 字面量节点测试 ==========
    
    @Test
    void testStringLiteral() {
        StringLiteral node = new StringLiteral("Hello World");
        assertEquals("Hello World", node.getValue());
        assertNotNull(node.getPosition());
        assertTrue(node.toString().contains("Hello World"));
        
        // 测试访问者模式
        String result = node.accept(new TestVisitor());
        assertEquals("StringLiteral", result);
    }
    
    @Test
    void testNumberLiteralInteger() {
        NumberLiteral node = new NumberLiteral(42, "42");
        assertEquals(42, node.getValue());
        assertEquals("42", node.getRaw());
        assertTrue(node.getValue() instanceof Integer);
        assertTrue(node.toString().contains("42"));
        
        String result = node.accept(new TestVisitor());
        assertEquals("NumberLiteral", result);
    }
    
    @Test
    void testNumberLiteralLong() {
        NumberLiteral node = new NumberLiteral(1234567890123L, "1234567890123L");
        assertEquals(1234567890123L, node.getValue());
        assertEquals("1234567890123L", node.getRaw());
        assertTrue(node.getValue() instanceof Long);
    }
    
    @Test
    void testNumberLiteralBigInteger() {
        BigInteger bigInt = new BigInteger("123456789012345678901234567890");
        NumberLiteral node = new NumberLiteral(bigInt, "123456789012345678901234567890");
        assertEquals(bigInt, node.getValue());
        assertTrue(node.getValue() instanceof BigInteger);
    }
    
    @Test
    void testNumberLiteralDouble() {
        NumberLiteral node = new NumberLiteral(3.14159, "3.14159");
        assertEquals(3.14159, node.getValue());
        assertTrue(node.getValue() instanceof Double);
    }
    
    @Test
    void testNumberLiteralBigDecimal() {
        BigDecimal bigDec = new BigDecimal("99.999999999999");
        NumberLiteral node = new NumberLiteral(bigDec, "99.999999999999");
        assertEquals(bigDec, node.getValue());
        assertTrue(node.getValue() instanceof BigDecimal);
    }
    
    @Test
    void testBooleanLiteral() {
        BooleanLiteral trueNode = new BooleanLiteral(true);
        assertTrue(trueNode.getValue());
        assertTrue(trueNode.toString().contains("true"));
        
        BooleanLiteral falseNode = new BooleanLiteral(false);
        assertFalse(falseNode.getValue());
        assertTrue(falseNode.toString().contains("false"));
        
        String result = trueNode.accept(new TestVisitor());
        assertEquals("BooleanLiteral", result);
    }
    
    @Test
    void testNullLiteral() {
        NullLiteral node = NullLiteral.INSTANCE;
        assertNotNull(node);
        assertTrue(node.toString().contains("NullLiteral"));
        
        // 测试单例
        assertSame(node, NullLiteral.INSTANCE);
        
        String result = node.accept(new TestVisitor());
        assertEquals("NullLiteral", result);
    }
    
    @Test
    void testTemplateLiteral() {
        List<String> strings = Arrays.asList("Hello ", " World", "!");
        List<ASTNode> expressions = Arrays.asList(
            new Identifier("name"),
            new NumberLiteral(123, "123")
        );
        
        TemplateLiteral node = new TemplateLiteral(strings, expressions);
        assertEquals(strings, node.getStrings());
        assertEquals(expressions, node.getExpressions());
        assertEquals(3, node.getStrings().size());
        assertEquals(2, node.getExpressions().size());
        
        String result = node.accept(new TestVisitor());
        assertEquals("TemplateLiteral", result);
    }
    
    @Test
    void testListLiteralEmpty() {
        ListLiteral node = new ListLiteral(Collections.emptyList());
        assertTrue(node.getElements().isEmpty());
        
        String result = node.accept(new TestVisitor());
        assertEquals("ListLiteral", result);
    }
    
    @Test
    void testListLiteralWithElements() {
        List<ASTNode> elements = Arrays.asList(
            new NumberLiteral(1, "1"),
            new NumberLiteral(2, "2"),
            new NumberLiteral(3, "3")
        );
        
        ListLiteral node = new ListLiteral(elements);
        assertEquals(3, node.getElements().size());
        assertEquals(elements, node.getElements());
    }
    
    @Test
    void testListLiteralMixedTypes() {
        List<ASTNode> elements = Arrays.asList(
            new NumberLiteral(1, "1"),
            new StringLiteral("hello"),
            new BooleanLiteral(true),
            NullLiteral.INSTANCE
        );
        
        ListLiteral node = new ListLiteral(elements);
        assertEquals(4, node.getElements().size());
    }
    
    @Test
    void testListLiteralNested() {
        ListLiteral innerList = new ListLiteral(Arrays.asList(
            new NumberLiteral(1, "1"),
            new NumberLiteral(2, "2")
        ));
        
        ListLiteral outerList = new ListLiteral(Arrays.asList(
            innerList,
            new NumberLiteral(3, "3")
        ));
        
        assertEquals(2, outerList.getElements().size());
        assertTrue(outerList.getElements().get(0) instanceof ListLiteral);
    }
    
    @Test
    void testHashLiteralEmpty() {
        HashLiteral node = new HashLiteral(Collections.emptyList());
        assertTrue(node.isEmpty());
        assertEquals(0, node.getEntries().size());
        assertTrue(node.toString().contains("HashLiteral"));
        
        String result = node.accept(new TestVisitor());
        assertEquals("HashLiteral", result);
    }
    
    @Test
    void testHashLiteralIdentifierKeys() {
        List<java.util.Map.Entry<ASTNode, ASTNode>> entries = Arrays.asList(
            HashLiteral.entry(new Identifier("name"), new StringLiteral("张三")),
            HashLiteral.entry(new Identifier("age"), new NumberLiteral(25, "25")),
            HashLiteral.entry(new Identifier("city"), new StringLiteral("北京"))
        );
        
        HashLiteral node = new HashLiteral(entries);
        assertFalse(node.isEmpty());
        assertEquals(3, node.getEntries().size());
        
        // 验证第一个键值对
        java.util.Map.Entry<ASTNode, ASTNode> first = node.getEntries().get(0);
        assertTrue(first.getKey() instanceof Identifier);
        assertEquals("name", ((Identifier) first.getKey()).getName());
        assertTrue(first.getValue() instanceof StringLiteral);
        assertEquals("张三", ((StringLiteral) first.getValue()).getValue());
    }
    
    @Test
    void testHashLiteralStringKeys() {
        List<java.util.Map.Entry<ASTNode, ASTNode>> entries = Arrays.asList(
            HashLiteral.entry(new StringLiteral("user-name"), new StringLiteral("李四")),
            HashLiteral.entry(new StringLiteral("user-id"), new NumberLiteral(10086, "10086"))
        );
        
        HashLiteral node = new HashLiteral(entries);
        assertEquals(2, node.getEntries().size());
    }
    
    @Test
    void testHashLiteralNumberKeys() {
        List<java.util.Map.Entry<ASTNode, ASTNode>> entries = Arrays.asList(
            HashLiteral.entry(new NumberLiteral(0, "0"), new StringLiteral("待处理")),
            HashLiteral.entry(new NumberLiteral(1, "1"), new StringLiteral("进行中")),
            HashLiteral.entry(new NumberLiteral(2, "2"), new StringLiteral("已完成"))
        );
        
        HashLiteral node = new HashLiteral(entries);
        assertEquals(3, node.getEntries().size());
    }
    
    @Test
    void testHashLiteralMixedKeys() {
        List<java.util.Map.Entry<ASTNode, ASTNode>> entries = Arrays.asList(
            HashLiteral.entry(new Identifier("name"), new StringLiteral("混合示例")),
            HashLiteral.entry(new StringLiteral("string-key"), new StringLiteral("字符串键")),
            HashLiteral.entry(new NumberLiteral(100, "100"), new StringLiteral("数字键"))
        );
        
        HashLiteral node = new HashLiteral(entries);
        assertEquals(3, node.getEntries().size());
    }
    
    @Test
    void testHashLiteralNestedHash() {
        HashLiteral innerHash = new HashLiteral(Arrays.asList(
            HashLiteral.entry(new Identifier("city"), new StringLiteral("北京")),
            HashLiteral.entry(new Identifier("street"), new StringLiteral("长安街"))
        ));
        
        List<java.util.Map.Entry<ASTNode, ASTNode>> entries = Arrays.asList(
            HashLiteral.entry(new Identifier("user"), new StringLiteral("张三")),
            HashLiteral.entry(new Identifier("address"), innerHash)
        );
        
        HashLiteral node = new HashLiteral(entries);
        assertEquals(2, node.getEntries().size());
        
        // 验证嵌套哈希表
        java.util.Map.Entry<ASTNode, ASTNode> addressEntry = node.getEntries().get(1);
        assertTrue(addressEntry.getValue() instanceof HashLiteral);
    }
    
    @Test
    void testHashLiteralWithPosition() {
        Position pos = new Position(1, 5, 1, 25);
        HashLiteral node = new HashLiteral(Collections.emptyList(), pos);
        
        assertEquals(pos, node.getPosition());
        assertEquals(1, node.getStartLine());
        assertEquals(5, node.getStartColumn());
    }
    
    @Test
    void testRangeLiteralClosed() {
        RangeLiteral node = new RangeLiteral(
            new NumberLiteral(0, "0"),
            new NumberLiteral(10, "10"),
            RangeLiteral.RangeType.CLOSED
        );
        
        assertEquals(RangeLiteral.RangeType.CLOSED, node.getType());
        assertNotNull(node.getStart());
        assertNotNull(node.getEnd());
        assertTrue(node.toString().contains("CLOSED"));
        
        String result = node.accept(new TestVisitor());
        assertEquals("RangeLiteral", result);
    }
    
    @Test
    void testRangeLiteralOpen() {
        RangeLiteral node = new RangeLiteral(
            new NumberLiteral(0, "0"),
            new NumberLiteral(10, "10"),
            RangeLiteral.RangeType.OPEN
        );
        
        assertEquals(RangeLiteral.RangeType.OPEN, node.getType());
    }
    
    @Test
    void testRangeLiteralLeftClosedRightOpen() {
        RangeLiteral node = new RangeLiteral(
            new NumberLiteral(0, "0"),
            new NumberLiteral(10, "10"),
            RangeLiteral.RangeType.LEFT_CLOSED_RIGHT_OPEN
        );
        
        assertEquals(RangeLiteral.RangeType.LEFT_CLOSED_RIGHT_OPEN, node.getType());
    }
    
    @Test
    void testRangeLiteralLeftOpenRightClosed() {
        RangeLiteral node = new RangeLiteral(
            new NumberLiteral(0, "0"),
            new NumberLiteral(10, "10"),
            RangeLiteral.RangeType.LEFT_OPEN_RIGHT_CLOSED
        );
        
        assertEquals(RangeLiteral.RangeType.LEFT_OPEN_RIGHT_CLOSED, node.getType());
    }
    
    @Test
    void testRegexLiteralBasic() {
        RegexLiteral node = new RegexLiteral("[a-zA-Z]+", null);
        assertEquals("[a-zA-Z]+", node.getPattern());
        assertEquals("", node.getFlags());
        assertFalse(node.hasGlobalFlag());
        assertFalse(node.hasIgnoreCaseFlag());
        assertFalse(node.hasMultilineFlag());
        
        String result = node.accept(new TestVisitor());
        assertEquals("RegexLiteral", result);
    }
    
    @Test
    void testRegexLiteralWithFlags() {
        RegexLiteral node = new RegexLiteral("\\d+", "gi");
        assertEquals("\\d+", node.getPattern());
        assertEquals("gi", node.getFlags());
        assertTrue(node.hasGlobalFlag());
        assertTrue(node.hasIgnoreCaseFlag());
        assertFalse(node.hasMultilineFlag());
    }
    
    @Test
    void testRegexLiteralAllFlags() {
        RegexLiteral node = new RegexLiteral("pattern", "gim");
        assertTrue(node.hasGlobalFlag());
        assertTrue(node.hasIgnoreCaseFlag());
        assertTrue(node.hasMultilineFlag());
    }
    
    // ========== 变量和访问节点测试 ==========
    
    @Test
    void testIdentifier() {
        Identifier node = new Identifier("userName");
        assertEquals("userName", node.getName());
        assertTrue(node.toString().contains("userName"));
        
        String result = node.accept(new TestVisitor());
        assertEquals("Identifier", result);
    }
    
    @Test
    void testMemberAccessNormal() {
        MemberAccess node = new MemberAccess(
            new Identifier("user"),
            "name",
            false
        );
        
        assertNotNull(node.getObject());
        assertEquals("name", node.getMemberName());
        assertFalse(node.isSafeNavigation());
        assertTrue(node.toString().contains("name"));
        
        String result = node.accept(new TestVisitor());
        assertEquals("MemberAccess", result);
    }
    
    @Test
    void testMemberAccessSafe() {
        MemberAccess node = new MemberAccess(
            new Identifier("user"),
            "address",
            true
        );
        
        assertTrue(node.isSafeNavigation());
        assertEquals("address", node.getMemberName());
    }
    
    @Test
    void testMemberAccessChained() {
        MemberAccess inner = new MemberAccess(
            new Identifier("user"),
            "address",
            false
        );
        
        MemberAccess outer = new MemberAccess(
            inner,
            "city",
            false
        );
        
        assertTrue(outer.getObject() instanceof MemberAccess);
        assertEquals("city", outer.getMemberName());
    }
    
    @Test
    void testIndexAccess() {
        IndexAccess node = new IndexAccess(
            new Identifier("list"),
            new NumberLiteral(0, "0")
        );
        
        assertNotNull(node.getObject());
        assertNotNull(node.getIndex());
        assertTrue(node.getObject() instanceof Identifier);
        assertTrue(node.getIndex() instanceof NumberLiteral);
        
        String result = node.accept(new TestVisitor());
        assertEquals("IndexAccess", result);
    }
    
    @Test
    void testIndexAccessWithStringKey() {
        IndexAccess node = new IndexAccess(
            new Identifier("map"),
            new StringLiteral("key")
        );
        
        assertTrue(node.getIndex() instanceof StringLiteral);
    }
    
    @Test
    void testIndexAccessChained() {
        IndexAccess inner = new IndexAccess(
            new Identifier("matrix"),
            new NumberLiteral(0, "0")
        );
        
        IndexAccess outer = new IndexAccess(
            inner,
            new NumberLiteral(1, "1")
        );
        
        assertTrue(outer.getObject() instanceof IndexAccess);
    }
    
    // ========== 调用节点测试 ==========
    
    @Test
    void testMethodCallNoArgs() {
        MethodCall node = new MethodCall(
            new Identifier("user"),
            "getName",
            Collections.emptyList(),
            false
        );
        
        assertNotNull(node.getObject());
        assertEquals("getName", node.getMethodName());
        assertTrue(node.getArguments().isEmpty());
        assertFalse(node.isSafeNavigation());
        
        String result = node.accept(new TestVisitor());
        assertEquals("MethodCall", result);
    }
    
    @Test
    void testMethodCallWithArgs() {
        List<ASTNode> args = Arrays.asList(
            new StringLiteral("John"),
            new NumberLiteral(25, "25")
        );
        
        MethodCall node = new MethodCall(
            new Identifier("user"),
            "setInfo",
            args,
            false
        );
        
        assertEquals(2, node.getArguments().size());
        assertEquals("setInfo", node.getMethodName());
    }
    
    @Test
    void testMethodCallSafe() {
        MethodCall node = new MethodCall(
            new Identifier("user"),
            "getName",
            Collections.emptyList(),
            true
        );
        
        assertTrue(node.isSafeNavigation());
    }
    
    @Test
    void testMethodCallChained() {
        MethodCall inner = new MethodCall(
            new Identifier("user"),
            "getName",
            Collections.emptyList(),
            false
        );
        
        MethodCall outer = new MethodCall(
            inner,
            "toUpperCase",
            Collections.emptyList(),
            false
        );
        
        assertTrue(outer.getObject() instanceof MethodCall);
    }
    
    @Test
    void testMethodCallNullObject() {
        MethodCall node = new MethodCall(
            null,
            "staticMethod",
            Collections.emptyList(),
            false
        );
        
        assertNull(node.getObject());
        assertEquals("staticMethod", node.getMethodName());
    }
    
    // ========== 运算符节点测试 ==========
    
    @Test
    void testBinaryExpressionArithmetic() {
        BinaryExpression add = new BinaryExpression(
            new NumberLiteral(1, "1"),
            BinaryExpression.Operator.ADD,
            new NumberLiteral(2, "2")
        );
        
        assertEquals(BinaryExpression.Operator.ADD, add.getOperator());
        assertNotNull(add.getLeft());
        assertNotNull(add.getRight());
        assertEquals("+", add.getOperator().getSymbol());
        
        String result = add.accept(new TestVisitor());
        assertEquals("BinaryExpression", result);
    }
    
    @Test
    void testBinaryExpressionAllArithmeticOperators() {
        BinaryExpression.Operator[] operators = {
            BinaryExpression.Operator.ADD,
            BinaryExpression.Operator.SUBTRACT,
            BinaryExpression.Operator.MULTIPLY,
            BinaryExpression.Operator.DIVIDE,
            BinaryExpression.Operator.MODULO
        };
        
        for (BinaryExpression.Operator op : operators) {
            BinaryExpression expr = new BinaryExpression(
                new NumberLiteral(10, "10"),
                op,
                new NumberLiteral(5, "5")
            );
            assertEquals(op, expr.getOperator());
        }
    }
    
    @Test
    void testBinaryExpressionComparison() {
        BinaryExpression.Operator[] operators = {
            BinaryExpression.Operator.GT,
            BinaryExpression.Operator.LT,
            BinaryExpression.Operator.GTE,
            BinaryExpression.Operator.LTE,
            BinaryExpression.Operator.EQ,
            BinaryExpression.Operator.NEQ
        };
        
        for (BinaryExpression.Operator op : operators) {
            BinaryExpression expr = new BinaryExpression(
                new NumberLiteral(10, "10"),
                op,
                new NumberLiteral(5, "5")
            );
            assertEquals(op, expr.getOperator());
        }
    }
    
    @Test
    void testBinaryExpressionLogical() {
        BinaryExpression and = new BinaryExpression(
            new BooleanLiteral(true),
            BinaryExpression.Operator.AND,
            new BooleanLiteral(false)
        );
        assertEquals(BinaryExpression.Operator.AND, and.getOperator());
        
        BinaryExpression or = new BinaryExpression(
            new BooleanLiteral(true),
            BinaryExpression.Operator.OR,
            new BooleanLiteral(false)
        );
        assertEquals(BinaryExpression.Operator.OR, or.getOperator());
    }
    
    @Test
    void testBinaryExpressionRegex() {
        BinaryExpression match = new BinaryExpression(
            new StringLiteral("test"),
            BinaryExpression.Operator.REGEX_MATCH,
            new RegexLiteral("\\w+", null)
        );
        assertEquals(BinaryExpression.Operator.REGEX_MATCH, match.getOperator());
        
        BinaryExpression notMatch = new BinaryExpression(
            new StringLiteral("test"),
            BinaryExpression.Operator.REGEX_NOT_MATCH,
            new RegexLiteral("\\d+", null)
        );
        assertEquals(BinaryExpression.Operator.REGEX_NOT_MATCH, notMatch.getOperator());
    }
    
    @Test
    void testBinaryExpressionIn() {
        BinaryExpression in = new BinaryExpression(
            new NumberLiteral(5, "5"),
            BinaryExpression.Operator.IN,
            new ListLiteral(Arrays.asList(
                new NumberLiteral(1, "1"),
                new NumberLiteral(5, "5"),
                new NumberLiteral(10, "10")
            ))
        );
        assertEquals(BinaryExpression.Operator.IN, in.getOperator());
        
        BinaryExpression notIn = new BinaryExpression(
            new NumberLiteral(3, "3"),
            BinaryExpression.Operator.NOT_IN,
            new ListLiteral(Collections.emptyList())
        );
        assertEquals(BinaryExpression.Operator.NOT_IN, notIn.getOperator());
    }
    
    @Test
    void testUnaryExpressionNot() {
        UnaryExpression node = new UnaryExpression(
            UnaryExpression.Operator.NOT,
            new BooleanLiteral(true)
        );
        
        assertEquals(UnaryExpression.Operator.NOT, node.getOperator());
        assertNotNull(node.getOperand());
        assertEquals("!", node.getOperator().getSymbol());
        
        String result = node.accept(new TestVisitor());
        assertEquals("UnaryExpression", result);
    }
    
    @Test
    void testUnaryExpressionNegate() {
        UnaryExpression node = new UnaryExpression(
            UnaryExpression.Operator.NEGATE,
            new NumberLiteral(42, "42")
        );
        
        assertEquals(UnaryExpression.Operator.NEGATE, node.getOperator());
        assertEquals("-", node.getOperator().getSymbol());
    }
    
    @Test
    void testUnaryExpressionPlus() {
        UnaryExpression node = new UnaryExpression(
            UnaryExpression.Operator.PLUS,
            new NumberLiteral(42, "42")
        );
        
        assertEquals(UnaryExpression.Operator.PLUS, node.getOperator());
        assertEquals("+", node.getOperator().getSymbol());
    }
    
    @Test
    void testTernaryExpression() {
        TernaryExpression node = new TernaryExpression(
            new BooleanLiteral(true),
            new StringLiteral("yes"),
            new StringLiteral("no")
        );
        
        assertNotNull(node.getCondition());
        assertNotNull(node.getTrueValue());
        assertNotNull(node.getFalseValue());
        
        String result = node.accept(new TestVisitor());
        assertEquals("TernaryExpression", result);
    }
    
    @Test
    void testTernaryExpressionWithComplexCondition() {
        TernaryExpression node = new TernaryExpression(
            new BinaryExpression(
                new NumberLiteral(10, "10"),
                BinaryExpression.Operator.GT,
                new NumberLiteral(5, "5")
            ),
            new StringLiteral("greater"),
            new StringLiteral("not greater")
        );
        
        assertTrue(node.getCondition() instanceof BinaryExpression);
    }
    
    @Test
    void testElvisExpression() {
        ElvisExpression node = new ElvisExpression(
            new Identifier("name"),
            new StringLiteral("Anonymous")
        );
        
        assertNotNull(node.getLeft());
        assertNotNull(node.getRight());
        assertTrue(node.getLeft() instanceof Identifier);
        assertTrue(node.getRight() instanceof StringLiteral);
        
        String result = node.accept(new TestVisitor());
        assertEquals("ElvisExpression", result);
    }
    
    @Test
    void testElvisExpressionChained() {
        ElvisExpression inner = new ElvisExpression(
            new Identifier("firstName"),
            new Identifier("lastName")
        );
        
        ElvisExpression outer = new ElvisExpression(
            inner,
            new StringLiteral("Unknown")
        );
        
        assertTrue(outer.getLeft() instanceof ElvisExpression);
    }
    
    @Test
    void testIfExpressionSimple() {
        IfExpression node = new IfExpression(
            new BinaryExpression(
                new Identifier("age"),
                BinaryExpression.Operator.GTE,
                new NumberLiteral(18, "18")
            ),
            Arrays.asList(new StringLiteral("成年"))
        );
        
        assertNotNull(node.getCondition());
        assertNotNull(node.getThenStatements());
        assertEquals(1, node.getThenStatements().size());
        assertFalse(node.hasElse());
        assertFalse(node.hasElseIf());
        
        String result = node.accept(new TestVisitor());
        assertEquals("IfExpression", result);
    }
    
    @Test
    void testIfExpressionWithElse() {
        IfExpression node = new IfExpression(
            new BinaryExpression(
                new Identifier("age"),
                BinaryExpression.Operator.GTE,
                new NumberLiteral(18, "18")
            ),
            Arrays.asList(new StringLiteral("成年")),
            Arrays.asList(new StringLiteral("未成年"))
        );
        
        assertTrue(node.hasElse());
        assertNotNull(node.getElseStatements());
        assertEquals(1, node.getElseStatements().size());
    }
    
    @Test
    void testIfExpressionWithElseIf() {
        List<IfExpression.ElseIfClause> elseIfClauses = Arrays.asList(
            new IfExpression.ElseIfClause(
                new BinaryExpression(
                    new Identifier("score"),
                    BinaryExpression.Operator.GTE,
                    new NumberLiteral(60, "60")
                ),
                Arrays.asList(new StringLiteral("及格"))
            )
        );
        
        IfExpression node = new IfExpression(
            new BinaryExpression(
                new Identifier("score"),
                BinaryExpression.Operator.GTE,
                new NumberLiteral(90, "90")
            ),
            Arrays.asList(new StringLiteral("优秀")),
            elseIfClauses,
            Arrays.asList(new StringLiteral("不及格"))
        );
        
        assertTrue(node.hasElseIf());
        assertEquals(1, node.getElseIfClauses().size());
        assertTrue(node.hasElse());
    }
    
    @Test
    void testIfExpressionMultipleStatements() {
        List<ASTNode> thenStatements = Arrays.asList(
            new LetStatement("level", new StringLiteral("优秀")),
            new LetStatement("color", new StringLiteral("green")),
            new StringLiteral("A")
        );
        
        IfExpression node = new IfExpression(
            new BinaryExpression(
                new Identifier("score"),
                BinaryExpression.Operator.GTE,
                new NumberLiteral(90, "90")
            ),
            thenStatements,
            Arrays.asList(new StringLiteral("B"))
        );
        
        assertEquals(3, node.getThenStatements().size());
    }
    
    @Test
    void testIfExpressionWithPosition() {
        Position pos = new Position(1, 5, 3, 10);
        IfExpression node = new IfExpression(
            new BooleanLiteral(true),
            Arrays.asList(new StringLiteral("yes")),
            null,
            Arrays.asList(new StringLiteral("no")),
            pos
        );
        
        assertEquals(pos, node.getPosition());
        assertEquals(1, node.getStartLine());
        assertEquals(5, node.getStartColumn());
    }
    
    @Test
    void testForExpressionSimple() {
        ForExpression node = new ForExpression(
            "user",
            new Identifier("userList"),
            Collections.singletonList(
                new MethodCall(
                    new Identifier("names"),
                    "add",
                    Collections.singletonList(
                        new MemberAccess(new Identifier("user"), "name", false)
                    ),
                    false
                )
            )
        );
        
        assertEquals("user", node.getVariableName());
        assertNull(node.getIndexName());
        assertFalse(node.hasIndex());
        assertNotNull(node.getIterable());
        assertNotNull(node.getBodyStatements());
        assertEquals(1, node.getBodyStatements().size());
        
        String result = node.accept(new TestVisitor());
        assertEquals("ForExpression", result);
    }
    
    @Test
    void testForExpressionWithIndex() {
        ForExpression node = new ForExpression(
            "i",
            "user",
            new Identifier("userList"),
            Collections.singletonList(
                new MethodCall(
                    new Identifier("user"),
                    "setIndex",
                    Collections.singletonList(new Identifier("i")),
                    false
                )
            )
        );
        
        assertEquals("i", node.getIndexName());
        assertEquals("user", node.getVariableName());
        assertTrue(node.hasIndex());
    }
    
    @Test
    void testForExpressionWithRange() {
        ForExpression node = new ForExpression(
            "i",
            new RangeLiteral(
                new NumberLiteral(1, "1"),
                new NumberLiteral(10, "10"),
                RangeLiteral.RangeType.CLOSED
            ),
            Collections.singletonList(
                new BinaryExpression(
                    new Identifier("sum"),
                    BinaryExpression.Operator.ADD,
                    new Identifier("i")
                )
            )
        );
        
        assertTrue(node.getIterable() instanceof RangeLiteral);
    }
    
    @Test
    void testForExpressionMultipleStatements() {
        List<ASTNode> bodyStatements = Arrays.asList(
            new LetStatement("temp", new Identifier("item")),
            new MethodCall(
                new Identifier("result"),
                "add",
                Collections.singletonList(new Identifier("temp")),
                false
            )
        );
        
        ForExpression node = new ForExpression(
            "item",
            new Identifier("items"),
            bodyStatements
        );
        
        assertEquals(2, node.getBodyStatements().size());
    }
    
    @Test
    void testForExpressionWithPosition() {
        Position pos = new Position(1, 5, 3, 10);
        ForExpression node = new ForExpression(
            null,
            "item",
            new Identifier("items"),
            Arrays.asList(new StringLiteral("test")),
            pos
        );
        
        assertEquals(pos, node.getPosition());
        assertEquals(1, node.getStartLine());
        assertEquals(5, node.getStartColumn());
    }
    
    @Test
    void testForExpressionToString() {
        ForExpression nodeWithIndex = new ForExpression(
            "i",
            "item",
            new Identifier("items"),
            Collections.emptyList()
        );
        assertTrue(nodeWithIndex.toString().contains("i, item"));
        
        ForExpression nodeWithoutIndex = new ForExpression(
            "item",
            new Identifier("items"),
            Collections.emptyList()
        );
        String str = nodeWithoutIndex.toString();
        assertTrue(str.contains("loopVar='item'"));
        assertTrue(str.contains("ForExpression"));
    }
    
    // ========== 模板节点测试 ==========
    
    @Test
    void testTemplateNodeEmpty() {
        TemplateNode node = new TemplateNode(Collections.emptyList());
        assertTrue(node.getChildren().isEmpty());
        
        String result = node.accept(new TestVisitor());
        assertEquals("TemplateNode", result);
    }
    
    @Test
    void testTemplateNodeWithChildren() {
        List<ASTNode> children = Arrays.asList(
            new TextNode("Hello "),
            new SingleBraceExpressionNode(new Identifier("name")),
            new TextNode("!")
        );
        
        TemplateNode node = new TemplateNode(children);
        assertEquals(3, node.getChildren().size());
    }
    
    @Test
    void testTextNode() {
        TextNode node = new TextNode("This is plain text.");
        assertEquals("This is plain text.", node.getText());
        assertTrue(node.toString().contains("This is plain text."));
        
        String result = node.accept(new TestVisitor());
        assertEquals("TextNode", result);
    }
    
    @Test
    void testTextNodeEmpty() {
        TextNode node = new TextNode("");
        assertEquals("", node.getText());
    }
    
    @Test
    void testSingleBraceExpressionNode() {
        SingleBraceExpressionNode node = new SingleBraceExpressionNode(new Identifier("user"));
        assertNotNull(node.getExpression());
        assertTrue(node.getExpression() instanceof Identifier);
        
        String result = node.accept(new TestVisitor());
        assertEquals("SingleBraceExpressionNode", result);
    }
    
    @Test
    void testDoubleBraceExpressionNode() {
        DoubleBraceExpressionNode node = new DoubleBraceExpressionNode(new Identifier("user"));
        assertNotNull(node.getExpression());
        assertTrue(node.getExpression() instanceof Identifier);
        
        String result = node.accept(new TestVisitor());
        assertEquals("DoubleBraceExpressionNode", result);
    }
    
    @Test
    void testDollarBraceExpressionNode() {
        DollarBraceExpressionNode node = new DollarBraceExpressionNode(new Identifier("user"));
        assertNotNull(node.getExpression());
        assertTrue(node.getExpression() instanceof Identifier);
        
        String result = node.accept(new TestVisitor());
        assertEquals("DollarBraceExpressionNode", result);
    }
    
    @Test
    void testExpressionNodeComplex() {
        SingleBraceExpressionNode node = new SingleBraceExpressionNode(
            new BinaryExpression(
                new NumberLiteral(10, "10"),
                BinaryExpression.Operator.ADD,
                new NumberLiteral(20, "20")
            )
        );
        
        assertTrue(node.getExpression() instanceof BinaryExpression);
    }
    
    @Test
    void testExpressionNodeWithPosition() {
        Position pos = new Position(1, 5, 1, 15);
        SingleBraceExpressionNode node = new SingleBraceExpressionNode(
            new Identifier("user"),
            pos
        );
        
        assertEquals(pos, node.getPosition());
        assertEquals(1, node.getStartLine());
        assertEquals(5, node.getStartColumn());
    }
    
    // ========== 语句块节点测试 ==========
    
    @Test
    void testStatementBlockInheritance() {
        // 测试 StatementBlock 作为基类，子类应该都是 StatementBlock 的实例
        IfStatement ifStmt = new IfStatement(
            new BooleanLiteral(true),
            Arrays.asList(new TextNode("Then")),
            null,
            null
        );
        
        ForStatement forStmt = new ForStatement(
            "item",
            new Identifier("list"),
            Arrays.asList(new TextNode("Body"))
        );
        
        SwitchStatement switchStmt = new SwitchStatement(
            new Identifier("value"),
            Arrays.asList(),
            null
        );
        
        // 验证所有子类都是 StatementBlock 的实例
        assertTrue(ifStmt instanceof StatementBlock);
        assertTrue(forStmt instanceof StatementBlock);
        assertTrue(switchStmt instanceof StatementBlock);
        
        // 验证它们也是 ASTNode 的实例
        assertTrue(ifStmt instanceof ASTNode);
        assertTrue(forStmt instanceof ASTNode);
        assertTrue(switchStmt instanceof ASTNode);
    }
    
    @Test
    void testStatementBlockPosition() {
        // 测试 StatementBlock 的位置信息功能
        Position pos = new Position(1, 1, 1, 20);
        
        // 由于 StatementBlock 是抽象类，我们通过子类测试
        IfStatement ifStmt = new IfStatement(
            new BooleanLiteral(true),
            Arrays.asList(new TextNode("Then")),
            null,
            null
        );
        
        // 设置位置信息
        ifStmt.setPosition(pos);
        
        assertEquals(pos, ifStmt.getPosition());
        assertEquals(1, ifStmt.getStartLine());
        assertEquals(1, ifStmt.getStartColumn());
        assertEquals(1, ifStmt.getEndLine());
        assertEquals(20, ifStmt.getEndColumn());
    }
    
    @Test
    void testIfStatementSimple() {
        IfStatement node = new IfStatement(
            new BooleanLiteral(true),
            Arrays.asList(new TextNode("Then branch")),
            null,
            null
        );
        
        assertNotNull(node.getCondition());
        assertNotNull(node.getThenBody());
        assertNull(node.getElseIfClauses());
        assertNull(node.getElseBody());
        
        String result = node.accept(new TestVisitor());
        assertEquals("IfStatement", result);
    }
    
    @Test
    void testIfStatementWithElse() {
        IfStatement node = new IfStatement(
            new BooleanLiteral(true),
            Arrays.asList(new TextNode("Then")),
            null,
            Arrays.asList(new TextNode("Else"))
        );
        
        assertNotNull(node.getElseBody());
        assertEquals(1, node.getElseBody().size());
    }
    
    @Test
    void testIfStatementWithElseIf() {
        List<IfStatement.ElseIfClause> elseIfClauses = Arrays.asList(
            new IfStatement.ElseIfClause(
                new BooleanLiteral(false),
                Arrays.asList(new TextNode("ElseIf 1"))
            ),
            new IfStatement.ElseIfClause(
                new BooleanLiteral(true),
                Arrays.asList(new TextNode("ElseIf 2"))
            )
        );
        
        IfStatement node = new IfStatement(
            new BooleanLiteral(false),
            Arrays.asList(new TextNode("Then")),
            elseIfClauses,
            Arrays.asList(new TextNode("Else"))
        );
        
        assertNotNull(node.getElseIfClauses());
        assertEquals(2, node.getElseIfClauses().size());
        
        IfStatement.ElseIfClause clause = node.getElseIfClauses().get(0);
        assertNotNull(clause.getCondition());
        assertNotNull(clause.getBody());
        assertTrue(clause.toString().contains("ElseIfClause"));
    }
    
    @Test
    void testForStatementSimple() {
        ForStatement node = new ForStatement(
            "item",
            new Identifier("list"),
            Arrays.asList(new TextNode("Body"))
        );
        
        assertEquals("item", node.getItemVariable());
        assertNull(node.getIndexVariable());
        assertFalse(node.hasIndexVariable());
        assertNotNull(node.getIterable());
        assertNotNull(node.getBody());
        assertEquals(1, node.getBody().size());
        
        String result = node.accept(new TestVisitor());
        assertEquals("ForStatement", result);
    }
    
    @Test
    void testForStatementWithIndex() {
        ForStatement node = new ForStatement(
            "i",
            "item",
            new Identifier("list"),
            Arrays.asList(new TextNode("Body"))
        );
        
        assertEquals("item", node.getItemVariable());
        assertEquals("i", node.getIndexVariable());
        assertTrue(node.hasIndexVariable());
    }
    
    @Test
    void testForStatementWithRange() {
        RangeLiteral range = new RangeLiteral(
            new NumberLiteral(0, "0"),
            new NumberLiteral(10, "10"),
            RangeLiteral.RangeType.CLOSED
        );
        
        ForStatement node = new ForStatement(
            "i",
            range,
            Arrays.asList(new TextNode("Body"))
        );
        
        assertTrue(node.getIterable() instanceof RangeLiteral);
    }
    
    @Test
    void testSwitchStatementSimple() {
        List<CaseClause> cases = Arrays.asList(
            new CaseClause(
                new NumberLiteral(1, "1"),
                Arrays.asList(new TextNode("Case 1"))
            ),
            new CaseClause(
                new NumberLiteral(2, "2"),
                Arrays.asList(new TextNode("Case 2"))
            )
        );
        
        SwitchStatement node = new SwitchStatement(
            new Identifier("value"),
            cases,
            null
        );
        
        assertNotNull(node.getCondition());
        assertNotNull(node.getCases());
        assertEquals(2, node.getCases().size());
        assertNull(node.getDefaultBody());
        assertFalse(node.hasDefaultBody());
        
        String result = node.accept(new TestVisitor());
        assertEquals("SwitchStatement", result);
    }
    
    @Test
    void testSwitchStatementWithDefault() {
        List<CaseClause> cases = Arrays.asList(
            new CaseClause(
                new NumberLiteral(1, "1"),
                Arrays.asList(new TextNode("Case 1"))
            )
        );
        
        SwitchStatement node = new SwitchStatement(
            new Identifier("value"),
            cases,
            Arrays.asList(new TextNode("Default"))
        );
        
        assertNotNull(node.getDefaultBody());
        assertTrue(node.hasDefaultBody());
        assertEquals(1, node.getDefaultBody().size());
    }
    
    @Test
    void testSwitchStatementEmptyDefault() {
        SwitchStatement node = new SwitchStatement(
            new Identifier("value"),
            Collections.emptyList(),
            Collections.emptyList()
        );
        
        assertFalse(node.hasDefaultBody());
    }
    
    @Test
    void testCaseClause() {
        CaseClause node = new CaseClause(
            new StringLiteral("admin"),
            Arrays.asList(new TextNode("Admin case"))
        );
        
        assertNotNull(node.getValue());
        assertNotNull(node.getBody());
        assertTrue(node.getValue() instanceof StringLiteral);
        assertEquals(1, node.getBody().size());
        
        String result = node.accept(new TestVisitor());
        assertEquals("CaseClause", result);
    }
    
    // ========== 嵌入式代码块节点测试 ==========
    
    @Test
    void testLetStatement() {
        LetStatement node = new LetStatement(
            "userName",
            new StringLiteral("John")
        );
        
        assertEquals("userName", node.getVariableName());
        assertNotNull(node.getValue());
        assertTrue(node.getValue() instanceof StringLiteral);
        
        String result = node.accept(new TestVisitor());
        assertEquals("LetStatement", result);
    }
    
    @Test
    void testLetStatementWithExpression() {
        LetStatement node = new LetStatement(
            "sum",
            new BinaryExpression(
                new NumberLiteral(10, "10"),
                BinaryExpression.Operator.ADD,
                new NumberLiteral(20, "20")
            )
        );
        
        assertTrue(node.getValue() instanceof BinaryExpression);
    }
    
    @Test
    void testExtendsStatement() {
        ExtendsStatement node = new ExtendsStatement("layout.etl");
        assertEquals("layout.etl", node.getTemplatePath());
        assertTrue(node.toString().contains("layout.etl"));
        
        String result = node.accept(new TestVisitor());
        assertEquals("ExtendsStatement", result);
    }
    
    @Test
    void testExtendsStatementWithPath() {
        ExtendsStatement node = new ExtendsStatement("templates/base.etl");
        assertEquals("templates/base.etl", node.getTemplatePath());
    }
    
    @Test
    void testImportStatement() {
        ImportStatement node = new ImportStatement("java.util.List");
        assertEquals("java.util.List", node.getClassName());
        assertTrue(node.toString().contains("java.util.List"));
        
        String result = node.accept(new TestVisitor());
        assertEquals("ImportStatement", result);
    }
    
    @Test
    void testImportStatementCustomClass() {
        ImportStatement node = new ImportStatement("com.example.utils.StringUtils");
        assertEquals("com.example.utils.StringUtils", node.getClassName());
    }

    @Test
    void testImportStatementWithAlias() {
        ImportStatement stmt = new ImportStatement("java.util.List", "ArrayList");
        assertEquals("java.util.List", stmt.getClassName());
        assertEquals("ArrayList", stmt.getAlias());
    }

    @Test
    void testImportStatementWithoutAlias() {
        ImportStatement stmt = new ImportStatement("java.util.List");
        assertEquals("java.util.List", stmt.getClassName());
        assertNull(stmt.getAlias());
    }

    // ========== ArgumentAccess 测试 ==========

    @Test
    void testArgumentAccessBasic() {
        ArgumentAccess node = new ArgumentAccess(0);
        assertEquals(0, node.getIndex());
        assertTrue(node.toString().contains("index=0"));

        String result = node.accept(new TestVisitor());
        assertEquals("ArgumentAccess", result);
    }

    @Test
    void testArgumentAccessWithDifferentIndices() {
        ArgumentAccess node0 = new ArgumentAccess(0);
        ArgumentAccess node1 = new ArgumentAccess(1);
        ArgumentAccess node2 = new ArgumentAccess(2);

        assertEquals(0, node0.getIndex());
        assertEquals(1, node1.getIndex());
        assertEquals(2, node2.getIndex());
    }

    @Test
    void testArgumentAccessWithPosition() {
        Position pos = new Position(1, 5, 1, 8);
        ArgumentAccess node = new ArgumentAccess(0, pos);

        assertEquals(0, node.getIndex());
        assertEquals(pos, node.getPosition());
        assertEquals(1, node.getStartLine());
        assertEquals(5, node.getStartColumn());
    }

    // ========== ASTNode基类测试 ==========
    
    @Test
    void testASTNodePositionDefault() {
        StringLiteral node = new StringLiteral("test");
        assertNotNull(node.getPosition());
        assertTrue(node.getPosition().isUnknown());
    }
    
    @Test
    void testASTNodeSetPosition() {
        StringLiteral node = new StringLiteral("test");
        Position pos = new Position(1, 5, 1, 10);
        node.setPosition(pos);
        
        assertEquals(pos, node.getPosition());
        assertEquals(1, node.getStartLine());
        assertEquals(5, node.getStartColumn());
        assertEquals(1, node.getEndLine());
        assertEquals(10, node.getEndColumn());
    }
    
    @Test
    void testASTNodeSetPositionNull() {
        StringLiteral node = new StringLiteral("test");
        node.setPosition(null);
        
        assertNotNull(node.getPosition());
        assertTrue(node.getPosition().isUnknown());
    }
    
    // ========== 对象构造节点测试 ==========

    @Test
    void testObjectConstruction() {
        Identifier targetClass = new Identifier("Person");
        List<Map.Entry<ASTNode, ASTNode>> entries = new ArrayList<>();
        entries.add(new AbstractMap.SimpleEntry<>(
            new Identifier("name"), new StringLiteral("Peter")));
        HashLiteral properties = new HashLiteral(entries);

        ObjectConstruction obj = new ObjectConstruction(targetClass, properties);

        assertEquals(targetClass, obj.getTargetClass());
        assertEquals(properties, obj.getPropertiesBlock());

        // Test visitor pattern
        String result = obj.accept(new TestVisitor());
        assertEquals("ObjectConstruction", result);
    }

    @Test
    void testObjectConstructionWithPosition() {
        Identifier targetClass = new Identifier("Person");
        List<Map.Entry<ASTNode, ASTNode>> entries = new ArrayList<>();
        entries.add(new AbstractMap.SimpleEntry<>(
            new Identifier("age"), new NumberLiteral(25, "25")));
        HashLiteral properties = new HashLiteral(entries);
        Position pos = new Position(1, 1, 1, 20);

        ObjectConstruction obj = new ObjectConstruction(targetClass, properties, pos);

        assertEquals(targetClass, obj.getTargetClass());
        assertEquals(properties, obj.getPropertiesBlock());
        assertEquals(pos, obj.getPosition());
    }

    @Test
    void testObjectConstructionToString() {
        Identifier targetClass = new Identifier("Person");
        List<Map.Entry<ASTNode, ASTNode>> entries = new ArrayList<>();
        entries.add(new AbstractMap.SimpleEntry<>(
            new Identifier("name"), new StringLiteral("Peter")));
        HashLiteral properties = new HashLiteral(entries);

        ObjectConstruction obj = new ObjectConstruction(targetClass, properties);

        assertTrue(obj.toString().contains("ObjectConstruction"));
        assertTrue(obj.toString().contains("targetClass"));
        assertTrue(obj.toString().contains("propertiesBlock"));
    }

    // ========== 测试访问者实现 ==========

    /**
     * 简单的测试访问者实现，用于测试访问者模式
     */
    private static class TestVisitor implements ASTVisitor<String> {
        @Override
        public String visitStringLiteral(StringLiteral node) {
            return "StringLiteral";
        }
        
        @Override
        public String visitTemplateLiteral(TemplateLiteral node) {
            return "TemplateLiteral";
        }
        
        @Override
        public String visitNumberLiteral(NumberLiteral node) {
            return "NumberLiteral";
        }
        
        @Override
        public String visitBooleanLiteral(BooleanLiteral node) {
            return "BooleanLiteral";
        }
        
        @Override
        public String visitNullLiteral(NullLiteral node) {
            return "NullLiteral";
        }
        
        @Override
        public String visitListLiteral(ListLiteral node) {
            return "ListLiteral";
        }
        
        @Override
        public String visitHashLiteral(HashLiteral node) {
            return "HashLiteral";
        }
        
        @Override
        public String visitRangeLiteral(RangeLiteral node) {
            return "RangeLiteral";
        }
        
        @Override
        public String visitRegexLiteral(RegexLiteral node) {
            return "RegexLiteral";
        }
        
        @Override
        public String visitIdentifier(Identifier node) {
            return "Identifier";
        }
        
        @Override
        public String visitMemberAccess(MemberAccess node) {
            return "MemberAccess";
        }
        
        @Override
        public String visitIndexAccess(IndexAccess node) {
            return "IndexAccess";
        }

        @Override
        public String visitCurrentNode(CurrentNode node) {
            return "CurrentNode";
        }

        @Override
        public String visitWildcardAccess(WildcardAccess node) {
            return "WildcardAccess";
        }

        @Override
        public String visitSliceAccess(SliceAccess node) {
            return "SliceAccess";
        }

        @Override
        public String visitFilterAccess(FilterAccess node) {
            return "FilterAccess";
        }

        @Override
        public String visitRecursiveDescentAccess(RecursiveDescentAccess node) {
            return "RecursiveDescentAccess";
        }

        @Override
        public String visitUnionAccess(UnionAccess node) {
            return "UnionAccess";
        }
        
        @Override
        public String visitArgumentAccess(ArgumentAccess node) {
            return "ArgumentAccess";
        }

        @Override
        public String visitArgumentVariable(ArgumentVariable node) {
            return "ArgumentVariable";
        }

        @Override
        public String visitMethodCall(MethodCall node) {
            return "MethodCall";
        }
        
        @Override
        public String visitBinaryExpression(BinaryExpression node) {
            return "BinaryExpression";
        }
        
        @Override
        public String visitUnaryExpression(UnaryExpression node) {
            return "UnaryExpression";
        }
        
        @Override
        public String visitTernaryExpression(TernaryExpression node) {
            return "TernaryExpression";
        }
        
        @Override
        public String visitElvisExpression(ElvisExpression node) {
            return "ElvisExpression";
        }
        
        @Override
        public String visitIfExpression(IfExpression node) {
            return "IfExpression";
        }
        
        @Override
        public String visitForExpression(ForExpression node) {
            return "ForExpression";
        }
        
        @Override
        public String visitTemplateNode(TemplateNode node) {
            return "TemplateNode";
        }
        
        @Override
        public String visitTextNode(TextNode node) {
            return "TextNode";
        }
        
        @Override
        public String visitSingleBraceExpressionNode(SingleBraceExpressionNode node) {
            return "SingleBraceExpressionNode";
        }
        
        @Override
        public String visitDoubleBraceExpressionNode(DoubleBraceExpressionNode node) {
            return "DoubleBraceExpressionNode";
        }
        
        @Override
        public String visitDollarBraceExpressionNode(DollarBraceExpressionNode node) {
            return "DollarBraceExpressionNode";
        }
        
        @Override
        public String visitIfStatement(IfStatement node) {
            return "IfStatement";
        }
        
        @Override
        public String visitForStatement(ForStatement node) {
            return "ForStatement";
        }
        
        @Override
        public String visitSwitchStatement(SwitchStatement node) {
            return "SwitchStatement";
        }
        
        @Override
        public String visitCaseClause(CaseClause node) {
            return "CaseClause";
        }
        
        @Override
        public String visitLetStatement(LetStatement node) {
            return "LetStatement";
        }
        
        @Override
        public String visitExtendsStatement(ExtendsStatement node) {
            return "ExtendsStatement";
        }
        
        @Override
        public String visitImportStatement(ImportStatement node) {
            return "ImportStatement";
        }

        @Override
        public String visitImportTemplateStatement(ImportTemplateStatement node) {
            return "ImportTemplateStatement";
        }

        @Override
        public String visitExportStatement(ExportStatement node) {
            return "ExportStatement";
        }

        @Override
        public String visitLambdaExpression(LambdaExpression node) {
            return "LambdaExpression";
        }

        @Override
        public String visitReturnStatement(ReturnStatement node) {
            return "ReturnStatement";
        }

        @Override
        public String visitUrlLiteral(UrlLiteral node) {
            return "UrlLiteral";
        }

        @Override
        public String visitObjectConstruction(ObjectConstruction node) {
            return "ObjectConstruction";
        }

        @Override
        public String visitCastExpression(CastExpression node) {
            return "CastExpression";
        }

        @Override
        public String visitIncrementExpression(IncrementExpression node) {
            return "IncrementExpression";
        }

        @Override
        public String visitCompoundAssignmentExpression(CompoundAssignmentExpression node) {
            return "CompoundAssignmentExpression";
        }

        @Override
        public String visitClassicForStatement(ClassicForStatement node) {
            return "ClassicForStatement";
        }
    }
}

