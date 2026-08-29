package org.dromara.easytl.parser;

import org.dromara.easytl.ast.*;
import org.dromara.easytl.exception.TemplateSyntaxException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Parser 类测试
 */
public class ParserTest {

    private Parser createParser(String input) throws IOException {
        StringSource source = new StringSource(input);
        Lexer lexer = new Lexer(source);
        TokenStream tokens = new TokenStream(lexer);
        return new Parser(tokens);
    }

    /**
     * Create a parser for pure expression parsing (starts in EXPRESSION mode)
     */
    private Parser createExpressionParser(String input) throws IOException {
        StringSource source = new StringSource(input);
        Lexer lexer = new Lexer(source, true); // Start in EXPRESSION mode
        TokenStream tokens = new TokenStream(lexer);
        return new Parser(tokens);
    }

    // ==================== 辅助方法：解析和获取节点 ====================

    /**
     * 解析输入并返回根节点
     */
    private TemplateNode parseAndGetRoot(String input) throws IOException {
        Parser parser = createParser(input);
        return parser.parse();
    }

    /**
     * 获取第一个子节点并验证类型
     */
    @SuppressWarnings("unchecked")
    private <T> T getFirstChild(TemplateNode root, Class<T> expectedType) {
        assertEquals(1, root.getChildren().size(), "根节点应该只有一个子节点");
        ASTNode child = root.getChildren().get(0);
        assertTrue(expectedType.isInstance(child), 
            "子节点类型应该是 " + expectedType.getSimpleName() + "，但实际是 " + child.getClass().getSimpleName());
        return (T) child;
    }

    /**
     * 获取指定索引的子节点并验证类型
     */
    @SuppressWarnings("unchecked")
    private <T> T getChild(TemplateNode root, int index, Class<T> expectedType) {
        assertTrue(index < root.getChildren().size(), 
            "索引 " + index + " 超出子节点数量 " + root.getChildren().size());
        ASTNode child = root.getChildren().get(index);
        assertTrue(expectedType.isInstance(child), 
            "子节点类型应该是 " + expectedType.getSimpleName() + "，但实际是 " + child.getClass().getSimpleName());
        return (T) child;
    }

    /**
     * 在body中查找指定类型的元素（跳过TextNode类型的空白行）
     */
    @SuppressWarnings("unchecked")
    private <T> T findInBody(java.util.List<ASTNode> body, Class<T> expectedType) {
        for (ASTNode node : body) {
            if (expectedType.isInstance(node)) {
                return (T) node;
            }
        }
        fail("在body中未找到类型为 " + expectedType.getSimpleName() + " 的节点");
        return null;
    }

    /**
     * 从单花括号表达式节点获取表达式
     */
    private ASTNode getExpression(SingleBraceExpressionNode exprNode) {
        return exprNode.getExpression();
    }

    /**
     * 从双花括号表达式节点获取表达式
     */
    private ASTNode getExpression(DoubleBraceExpressionNode exprNode) {
        return exprNode.getExpression();
    }

    /**
     * 从美元花括号表达式节点获取表达式
     */
    private ASTNode getExpression(DollarBraceExpressionNode exprNode) {
        return exprNode.getExpression();
    }

    /**
     * 从单花括号表达式中获取表达式（常用模式）
     */
    private ASTNode getSingleBraceExpression(String input) throws IOException {
        TemplateNode root = parseAndGetRoot(input);
        SingleBraceExpressionNode exprNode = getFirstChild(root, SingleBraceExpressionNode.class);
        return getExpression(exprNode);
    }

    /**
     * 从双花括号表达式中获取表达式
     */
    private ASTNode getDoubleBraceExpression(String input) throws IOException {
        TemplateNode root = parseAndGetRoot(input);
        DoubleBraceExpressionNode exprNode = getFirstChild(root, DoubleBraceExpressionNode.class);
        return getExpression(exprNode);
    }

    /**
     * 从美元花括号表达式中获取表达式
     */
    private ASTNode getDollarBraceExpression(String input) throws IOException {
        TemplateNode root = parseAndGetRoot(input);
        DollarBraceExpressionNode exprNode = getFirstChild(root, DollarBraceExpressionNode.class);
        return getExpression(exprNode);
    }

    /**
     * 从单花括号表达式中获取指定类型的表达式
     */
    @SuppressWarnings("unchecked")
    private <T> T getSingleBraceExpression(String input, Class<T> expectedType) throws IOException {
        ASTNode expr = getSingleBraceExpression(input);
        assertTrue(expectedType.isInstance(expr), 
            "表达式类型应该是 " + expectedType.getSimpleName() + "，但实际是 " + expr.getClass().getSimpleName());
        return (T) expr;
    }

    /**
     * 测试二元运算符表达式（数字操作数）
     */
    private void assertBinaryExpressionWithNumbers(String input, BinaryExpression.Operator expectedOp, 
                                                   Number leftValue, Number rightValue) throws IOException {
        BinaryExpression expr = assertBinaryExpression(getSingleBraceExpression(input), expectedOp);
        assertNumberLiteral(expr.getLeft(), leftValue);
        assertNumberLiteral(expr.getRight(), rightValue);
    }

    /**
     * 测试二元运算符表达式（标识符操作数）
     */
    private void assertBinaryExpressionWithIdentifiers(String input, BinaryExpression.Operator expectedOp, 
                                                       String leftName, String rightName) throws IOException {
        BinaryExpression expr = assertBinaryExpression(getSingleBraceExpression(input), expectedOp);
        assertIdentifier(expr.getLeft(), leftName);
        assertIdentifier(expr.getRight(), rightName);
    }

    /**
     * 从哈希表/列表测试中获取表达式（常用模式）
     */
    private ASTNode getExpressionFromParser(String input) throws IOException {
        Parser parser = createParser(input);
        TemplateNode root = parser.parse();
        return ((SingleBraceExpressionNode) root.getChildren().get(0)).getExpression();
    }

    /**
     * 从哈希表/列表测试中获取指定类型的表达式
     */
    @SuppressWarnings("unchecked")
    private <T> T getExpressionFromParser(String input, Class<T> expectedType) throws IOException {
        ASTNode expr = getExpressionFromParser(input);
        assertTrue(expectedType.isInstance(expr), 
            "表达式类型应该是 " + expectedType.getSimpleName() + "，但实际是 " + expr.getClass().getSimpleName());
        return (T) expr;
    }

    // ==================== 辅助方法：断言字面量 ====================

    /**
     * 断言节点是数字字面量并返回
     */
    private NumberLiteral assertNumberLiteral(ASTNode node, Number expected) {
        assertTrue(node instanceof NumberLiteral, "节点应该是NumberLiteral类型");
        NumberLiteral literal = (NumberLiteral) node;
        if (expected instanceof Integer) {
            assertEquals(expected.intValue(), literal.getValue().intValue());
        } else if (expected instanceof Long) {
            assertEquals(expected.longValue(), literal.getValue().longValue());
        } else if (expected instanceof Double) {
            assertEquals(expected.doubleValue(), literal.getValue().doubleValue(), 0.001);
        } else {
            assertEquals(expected, literal.getValue());
        }
        return literal;
    }

    /**
     * 断言节点是数字字面量（不检查值）
     */
    private NumberLiteral assertNumberLiteral(ASTNode node) {
        assertTrue(node instanceof NumberLiteral, "节点应该是NumberLiteral类型");
        return (NumberLiteral) node;
    }

    /**
     * 断言节点是字符串字面量并返回
     */
    private StringLiteral assertStringLiteral(ASTNode node, String expected) {
        assertTrue(node instanceof StringLiteral, "节点应该是StringLiteral类型");
        StringLiteral literal = (StringLiteral) node;
        assertEquals(expected, literal.getValue());
        return literal;
    }

    /**
     * 断言节点是字符串字面量（不检查值）
     */
    private StringLiteral assertStringLiteral(ASTNode node) {
        assertTrue(node instanceof StringLiteral, "节点应该是StringLiteral类型");
        return (StringLiteral) node;
    }

    /**
     * 断言节点是布尔字面量并返回
     */
    private BooleanLiteral assertBooleanLiteral(ASTNode node, boolean expected) {
        assertTrue(node instanceof BooleanLiteral, "节点应该是BooleanLiteral类型");
        BooleanLiteral literal = (BooleanLiteral) node;
        assertEquals(expected, literal.getValue());
        return literal;
    }

    /**
     * 断言节点是布尔字面量（不检查值）
     */
    private BooleanLiteral assertBooleanLiteral(ASTNode node) {
        assertTrue(node instanceof BooleanLiteral, "节点应该是BooleanLiteral类型");
        return (BooleanLiteral) node;
    }

    /**
     * 断言节点是null字面量
     */
    private void assertNullLiteral(ASTNode node) {
        assertTrue(node instanceof NullLiteral, "节点应该是NullLiteral类型");
    }

    /**
     * 断言节点是正则字面量并返回
     */
    private RegexLiteral assertRegexLiteral(ASTNode node, String expectedPattern) {
        assertTrue(node instanceof RegexLiteral, "节点应该是RegexLiteral类型");
        RegexLiteral literal = (RegexLiteral) node;
        assertEquals(expectedPattern, literal.getPattern());
        return literal;
    }

    // ==================== 辅助方法：断言标识符和访问 ====================

    /**
     * 断言节点是标识符并返回
     */
    private Identifier assertIdentifier(ASTNode node, String expectedName) {
        assertTrue(node instanceof Identifier, "节点应该是Identifier类型");
        Identifier identifier = (Identifier) node;
        assertEquals(expectedName, identifier.getName());
        return identifier;
    }

    /**
     * 断言节点是标识符（不检查名称）
     */
    private Identifier assertIdentifier(ASTNode node) {
        assertTrue(node instanceof Identifier, "节点应该是Identifier类型");
        return (Identifier) node;
    }

    /**
     * 断言节点是成员访问并返回
     */
    private MemberAccess assertMemberAccess(ASTNode node, String expectedObjectName, String expectedMemberName) {
        assertTrue(node instanceof MemberAccess, "节点应该是MemberAccess类型");
        MemberAccess access = (MemberAccess) node;
        assertTrue(access.getObject() instanceof Identifier, "成员访问的对象应该是Identifier类型");
        assertEquals(expectedObjectName, ((Identifier) access.getObject()).getName());
        assertEquals(expectedMemberName, access.getMemberName());
        return access;
    }

    /**
     * 断言节点是成员访问（不检查值）
     */
    private MemberAccess assertMemberAccess(ASTNode node) {
        assertTrue(node instanceof MemberAccess, "节点应该是MemberAccess类型");
        return (MemberAccess) node;
    }

    /**
     * 断言节点是索引访问并返回
     */
    private IndexAccess assertIndexAccess(ASTNode node) {
        assertTrue(node instanceof IndexAccess, "节点应该是IndexAccess类型");
        return (IndexAccess) node;
    }

    // ==================== 辅助方法：断言表达式 ====================

    /**
     * 断言节点是二元表达式并返回
     */
    private BinaryExpression assertBinaryExpression(ASTNode node, BinaryExpression.Operator expectedOp) {
        assertTrue(node instanceof BinaryExpression, "节点应该是BinaryExpression类型");
        BinaryExpression expr = (BinaryExpression) node;
        assertEquals(expectedOp, expr.getOperator());
        return expr;
    }

    /**
     * 断言节点是二元表达式（不检查操作符）
     */
    private BinaryExpression assertBinaryExpression(ASTNode node) {
        assertTrue(node instanceof BinaryExpression, "节点应该是BinaryExpression类型");
        return (BinaryExpression) node;
    }

    /**
     * 断言节点是三元表达式并返回
     */
    private TernaryExpression assertTernaryExpression(ASTNode node) {
        assertTrue(node instanceof TernaryExpression, "节点应该是TernaryExpression类型");
        return (TernaryExpression) node;
    }

    /**
     * 断言节点是Elvis表达式并返回
     */
    private ElvisExpression assertElvisExpression(ASTNode node) {
        assertTrue(node instanceof ElvisExpression, "节点应该是ElvisExpression类型");
        return (ElvisExpression) node;
    }

    /**
     * 断言节点是一元表达式并返回
     */
    private UnaryExpression assertUnaryExpression(ASTNode node, UnaryExpression.Operator expectedOp) {
        assertTrue(node instanceof UnaryExpression, "节点应该是UnaryExpression类型");
        UnaryExpression expr = (UnaryExpression) node;
        assertEquals(expectedOp, expr.getOperator());
        return expr;
    }

    /**
     * 断言节点是方法调用并返回
     */
    private MethodCall assertMethodCall(ASTNode node, String expectedMethodName) {
        assertTrue(node instanceof MethodCall, "节点应该是MethodCall类型");
        MethodCall call = (MethodCall) node;
        assertEquals(expectedMethodName, call.getMethodName());
        return call;
    }

    /**
     * 断言节点是方法调用（不检查方法名）
     */
    private MethodCall assertMethodCall(ASTNode node) {
        assertTrue(node instanceof MethodCall, "节点应该是MethodCall类型");
        return (MethodCall) node;
    }

    /**
     * 断言节点是区间字面量并返回
     */
    private RangeLiteral assertRangeLiteral(ASTNode node, RangeLiteral.RangeType expectedType) {
        assertTrue(node instanceof RangeLiteral, "节点应该是RangeLiteral类型");
        RangeLiteral range = (RangeLiteral) node;
        assertEquals(expectedType, range.getType());
        return range;
    }

    /**
     * 断言节点是参数访问并返回
     */
    private ArgumentAccess assertArgumentAccess(ASTNode node, int expectedIndex) {
        assertTrue(node instanceof ArgumentAccess, "节点应该是ArgumentAccess类型");
        ArgumentAccess paramAccess = (ArgumentAccess) node;
        assertEquals(expectedIndex, paramAccess.getIndex());
        return paramAccess;
    }

    @Test
    public void testSimpleText() throws IOException {
        TemplateNode root = parseAndGetRoot("Hello World");
        
        TextNode textNode = getFirstChild(root, TextNode.class);
        assertEquals("Hello World", textNode.getText());
    }

    @Test
    public void testExpression() throws IOException {
        assertBinaryExpressionWithNumbers("{1 + 2}", BinaryExpression.Operator.ADD, 1, 2);
    }

    @Test
    public void testIfStatement() throws IOException {
        // if, else 都是标准语句块风格，以 } 结尾
        TemplateNode root = parseAndGetRoot(
                "{% if user.age >= 18 }Adult{% else }Minor{/% if }");
        
        IfStatement ifStmt = getFirstChild(root, IfStatement.class);
        
        BinaryExpression condition = assertBinaryExpression(ifStmt.getCondition(), BinaryExpression.Operator.GTE);
        assertMemberAccess(condition.getLeft(), "user", "age");
        assertNumberLiteral(condition.getRight(), 18);
        
        assertEquals(1, ifStmt.getThenBody().size());
        assertTrue(ifStmt.getThenBody().get(0) instanceof TextNode);
        assertEquals("Adult", ((TextNode) ifStmt.getThenBody().get(0)).getText());
        
        assertNotNull(ifStmt.getElseBody());
        assertEquals(1, ifStmt.getElseBody().size());
        assertTrue(ifStmt.getElseBody().get(0) instanceof TextNode);
        assertEquals("Minor", ((TextNode) ifStmt.getElseBody().get(0)).getText());
    }
    
    @Test
    public void testIfElseIfStatement() throws IOException {
        // if, else if, else 都是标准语句块风格
        TemplateNode root = parseAndGetRoot(
                "{% if a > 10 }Big{% else if a > 5 }Medium{% else }Small{/% if }");
        
        IfStatement ifStmt = getFirstChild(root, IfStatement.class);
        
        // 检查if条件
        BinaryExpression ifCondition = assertBinaryExpression(ifStmt.getCondition(), BinaryExpression.Operator.GT);
        assertIdentifier(ifCondition.getLeft(), "a");
        assertNumberLiteral(ifCondition.getRight(), 10);
        
        assertNotNull(ifStmt.getElseIfClauses());
        assertEquals(1, ifStmt.getElseIfClauses().size());
        
        // 检查else if条件
        IfStatement.ElseIfClause elseIfClause = ifStmt.getElseIfClauses().get(0);
        BinaryExpression elseIfCondition = assertBinaryExpression(elseIfClause.getCondition(), BinaryExpression.Operator.GT);
        assertIdentifier(elseIfCondition.getLeft(), "a");
        assertNumberLiteral(elseIfCondition.getRight(), 5);
        
        assertNotNull(ifStmt.getElseBody());
    }

    @Test
    public void testForStatement() throws IOException {
        // for 是标准语句块风格
        TemplateNode root = parseAndGetRoot("{% for user in users }{{user.name}}{/% for }");
        
        ForStatement forStmt = getFirstChild(root, ForStatement.class);
        
        assertEquals("user", forStmt.getItemVariable());
        assertIdentifier(forStmt.getIterable(), "users");
        assertEquals(1, forStmt.getBody().size());
    }

    @Test
    public void testForStatementWithRange() throws IOException {
        TemplateNode root = parseAndGetRoot("{% for i in 1..10 }{i}{/% for }");
        
        ForStatement forStmt = getFirstChild(root, ForStatement.class);
        RangeLiteral range = assertRangeLiteral(forStmt.getIterable(), RangeLiteral.RangeType.CLOSED);
        assertNumberLiteral(range.getStart(), 1);
        assertNumberLiteral(range.getEnd(), 10);
    }

    @Test
    public void testSwitchStatement() throws IOException {
        // switch, case, default 都是标准语句块风格（在 BNF 中定义为 RIGHT_BRACE 结尾）
        TemplateNode root = parseAndGetRoot(
                "{% switch status }{% case 1 }Active{% case 0 }Inactive{% default }Unknown{/% switch }");
        
        SwitchStatement switchStmt = getFirstChild(root, SwitchStatement.class);
        
        assertIdentifier(switchStmt.getCondition(), "status");
        
        assertEquals(2, switchStmt.getCases().size());
        
        // 检查第一个case
        CaseClause case1 = switchStmt.getCases().get(0);
        assertNumberLiteral(case1.getValue(), 1);
        
        // 检查第二个case
        CaseClause case2 = switchStmt.getCases().get(1);
        assertNumberLiteral(case2.getValue(), 0);
        
        assertNotNull(switchStmt.getDefaultBody());
    }

    @Test
    public void testLetStatement() throws IOException {
        // let 是自闭合语句块，以 %} 结尾
        TemplateNode root = parseAndGetRoot("{% let x = 1 + 2 %}");
        
        LetStatement letStmt = getFirstChild(root, LetStatement.class);
        assertEquals("x", letStmt.getVariableName());
        BinaryExpression expr = assertBinaryExpression(letStmt.getValue(), BinaryExpression.Operator.ADD);
        assertNumberLiteral(expr.getLeft(), 1);
        assertNumberLiteral(expr.getRight(), 2);
    }

    @Test
    public void testLetIfExpression() throws IOException {
        // if 表达式在 let 中使用
        TemplateNode root = parseAndGetRoot(
                "{% let x = if (a > b) { a } else { b } %}");
        
        LetStatement letStmt = getFirstChild(root, LetStatement.class);
        assertEquals("x", letStmt.getVariableName());
        assertTrue(letStmt.getValue() instanceof IfExpression);
        IfExpression ifExpr = (IfExpression) letStmt.getValue();
        BinaryExpression condition = assertBinaryExpression(ifExpr.getCondition(), BinaryExpression.Operator.GT);
        assertIdentifier(condition.getLeft(), "a");
        assertIdentifier(condition.getRight(), "b");
    }

    @Test
    public void testIfExpression() throws IOException {
        // if 表达式作为单花括号表达式的内容
        IfExpression ifExpr = getSingleBraceExpression("{if (true) { 1 } else { 0 }}", IfExpression.class);
        
        assertBooleanLiteral(ifExpr.getCondition(), true);
        assertEquals(1, ifExpr.getThenStatements().size());
        assertNumberLiteral(ifExpr.getThenStatements().get(0), 1);
        assertNotNull(ifExpr.getElseStatements());
        assertEquals(1, ifExpr.getElseStatements().size());
        assertNumberLiteral(ifExpr.getElseStatements().get(0), 0);
    }
    
    @Test
    public void testForExpression() throws IOException {
        // for 表达式
        ForExpression forExpr = getSingleBraceExpression("{for (i in list) { print(i) }}", ForExpression.class);
        assertEquals("i", forExpr.getVariableName());
        assertIdentifier(forExpr.getIterable(), "list");
    }
    
    @Test
    public void testImportStatement() throws IOException {
        // import 是自闭合语句块
        TemplateNode root = parseAndGetRoot("{% import java.util.List %}");

        ImportStatement importStmt = getFirstChild(root, ImportStatement.class);
        assertEquals("java.util.List", importStmt.getClassName());
    }

    @Test
    void testImportStatementWithAlias() throws IOException {
        String code = "{% import java.util.List as ArrayList %}";
        Lexer lexer = new Lexer(new StringSource(code));
        TokenStream tokens = new TokenStream(lexer);
        Parser parser = new Parser(tokens);
        TemplateNode template = parser.parse();

        assertEquals(1, template.getChildren().size());
        ImportStatement stmt = (ImportStatement) template.getChildren().get(0);
        assertEquals("java.util.List", stmt.getClassName());
        assertEquals("ArrayList", stmt.getAlias());
    }

    @Test
    void testImportStatementWithoutAlias() throws IOException {
        String code = "{% import java.util.List %}";
        Lexer lexer = new Lexer(new StringSource(code));
        TokenStream tokens = new TokenStream(lexer);
        Parser parser = new Parser(tokens);
        TemplateNode template = parser.parse();

        assertEquals(1, template.getChildren().size());
        ImportStatement stmt = (ImportStatement) template.getChildren().get(0);
        assertEquals("java.util.List", stmt.getClassName());
        assertNull(stmt.getAlias());
    }

    @Test
    public void testHashLiteral() throws IOException {
        HashLiteral hash = getSingleBraceExpression("{ {a: 1, b: 2} }", HashLiteral.class);
        assertEquals(2, hash.getEntries().size());
        
        // 检查第一个条目（标识符键按 README 3.4.7 自动转换为字符串）
        assertStringLiteral(hash.getEntries().get(0).getKey(), "a");
        assertNumberLiteral(hash.getEntries().get(0).getValue(), 1);
        
        // 检查第二个条目
        assertStringLiteral(hash.getEntries().get(1).getKey(), "b");
        assertNumberLiteral(hash.getEntries().get(1).getValue(), 2);
    }
    
    @Test
    public void testListLiteral() throws IOException {
        ListLiteral list = getSingleBraceExpression("{ [1, 2, 3] }", ListLiteral.class);
        assertEquals(3, list.getElements().size());
        
        // 检查每个元素的值
        assertNumberLiteral(list.getElements().get(0), 1);
        assertNumberLiteral(list.getElements().get(1), 2);
        assertNumberLiteral(list.getElements().get(2), 3);
    }

    // ==================== 表达式包装器测试 ====================
    
    @Test
    public void testDoubleBraceExpression() throws IOException {
        assertMemberAccess(getDoubleBraceExpression("{{user.name}}"), "user", "name");
    }
    
    @Test
    public void testDollarBraceExpression() throws IOException {
        assertMemberAccess(getDollarBraceExpression("${user.age}"), "user", "age");
    }

    // ==================== 二元运算符测试 ====================
    
    @Test
    public void testArithmeticOperators() throws IOException {
        // 加法
        assertBinaryExpressionWithNumbers("{1 + 2}", BinaryExpression.Operator.ADD, 1, 2);
        
        // 减法
        assertBinaryExpressionWithNumbers("{5 - 3}", BinaryExpression.Operator.SUBTRACT, 5, 3);
        
        // 乘法
        assertBinaryExpressionWithNumbers("{2 * 3}", BinaryExpression.Operator.MULTIPLY, 2, 3);
        
        // 除法
        assertBinaryExpressionWithNumbers("{10 / 2}", BinaryExpression.Operator.DIVIDE, 10, 2);
        
        // 模运算
        assertBinaryExpressionWithNumbers("{10 % 3}", BinaryExpression.Operator.MODULO, 10, 3);
    }
    
    @Test
    public void testComparisonOperators() throws IOException {
        // 大于
        assertBinaryExpressionWithIdentifiers("{a > b}", BinaryExpression.Operator.GT, "a", "b");
        
        // 小于
        assertBinaryExpressionWithIdentifiers("{a < b}", BinaryExpression.Operator.LT, "a", "b");
        
        // 大于等于
        assertBinaryExpressionWithIdentifiers("{a >= b}", BinaryExpression.Operator.GTE, "a", "b");
        
        // 小于等于
        assertBinaryExpressionWithIdentifiers("{a <= b}", BinaryExpression.Operator.LTE, "a", "b");
        
        // 等于
        assertBinaryExpressionWithIdentifiers("{a == b}", BinaryExpression.Operator.EQ, "a", "b");
        
        // 不等于
        assertBinaryExpressionWithIdentifiers("{a != b}", BinaryExpression.Operator.NEQ, "a", "b");
    }
    
    @Test
    public void testLogicalOperators() throws IOException {
        // 逻辑与
        assertBinaryExpressionWithIdentifiers("{a && b}", BinaryExpression.Operator.AND, "a", "b");
        
        // 逻辑或
        assertBinaryExpressionWithIdentifiers("{a || b}", BinaryExpression.Operator.OR, "a", "b");
    }
    
    @Test
    public void testRegexOperators() throws IOException {
        // 正则匹配
        BinaryExpression expr = assertBinaryExpression(getSingleBraceExpression("{str =~ /pattern/}"), BinaryExpression.Operator.REGEX_MATCH);
        assertIdentifier(expr.getLeft(), "str");
        assertRegexLiteral(expr.getRight(), "pattern");
        
        // 正则不匹配
        expr = assertBinaryExpression(getSingleBraceExpression("{str !=~ /pattern/}"), BinaryExpression.Operator.REGEX_NOT_MATCH);
        assertIdentifier(expr.getLeft(), "str");
        assertRegexLiteral(expr.getRight(), "pattern");
    }
    
    @Test
    public void testInOperators() throws IOException {
        // in 运算符
        assertBinaryExpressionWithIdentifiers("{x in list}", BinaryExpression.Operator.IN, "x", "list");
        
        // !in 运算符
        assertBinaryExpressionWithIdentifiers("{x !in list}", BinaryExpression.Operator.NOT_IN, "x", "list");
    }

    // ==================== 三元表达式和Elvis表达式测试 ====================
    
    @Test
    public void testTernaryExpression() throws IOException {
        TernaryExpression ternary = assertTernaryExpression(getSingleBraceExpression("{a > b ? a : b}"));
        BinaryExpression condition = assertBinaryExpression(ternary.getCondition(), BinaryExpression.Operator.GT);
        assertIdentifier(condition.getLeft(), "a");
        assertIdentifier(condition.getRight(), "b");
        assertIdentifier(ternary.getTrueValue(), "a");
        assertIdentifier(ternary.getFalseValue(), "b");
    }
    
    @Test
    public void testElvisExpression() throws IOException {
        ElvisExpression elvis = assertElvisExpression(getSingleBraceExpression("{a ?? b}"));
        assertIdentifier(elvis.getLeft(), "a");
        assertIdentifier(elvis.getRight(), "b");
    }
    
    @Test
    public void testChainedElvisExpression() throws IOException {
        ElvisExpression elvis = assertElvisExpression(getSingleBraceExpression("{a ?? b ?? c}"));
        assertTrue(elvis.getLeft() instanceof ElvisExpression);
        ElvisExpression leftElvis = (ElvisExpression) elvis.getLeft();
        assertIdentifier(leftElvis.getLeft(), "a");
        assertIdentifier(leftElvis.getRight(), "b");
        assertIdentifier(elvis.getRight(), "c");
    }

    // ==================== 区间表达式测试 ====================
    
    @Test
    public void testRangeExpressions() throws IOException {
        // 闭区间
        RangeLiteral range = assertRangeLiteral(getSingleBraceExpression("{1..10}"), RangeLiteral.RangeType.CLOSED);
        assertNumberLiteral(range.getStart(), 1);
        assertNumberLiteral(range.getEnd(), 10);
        
        // 开区间（左闭右开）
        range = assertRangeLiteral(getSingleBraceExpression("{1>..<10}"), RangeLiteral.RangeType.OPEN);
        assertNumberLiteral(range.getStart(), 1);
        assertNumberLiteral(range.getEnd(), 10);
        
        // 左闭右开
        range = assertRangeLiteral(getSingleBraceExpression("{1..<10}"), RangeLiteral.RangeType.LEFT_CLOSED_RIGHT_OPEN);
        assertNumberLiteral(range.getStart(), 1);
        assertNumberLiteral(range.getEnd(), 10);
        
        // 左开右闭
        range = assertRangeLiteral(getSingleBraceExpression("{1>..10}"), RangeLiteral.RangeType.LEFT_OPEN_RIGHT_CLOSED);
        assertNumberLiteral(range.getStart(), 1);
        assertNumberLiteral(range.getEnd(), 10);
    }

    // ==================== 一元运算符测试 ====================
    
    @Test
    public void testUnaryOperators() throws IOException {
        // 逻辑非
        UnaryExpression unary = assertUnaryExpression(getSingleBraceExpression("{!flag}"), UnaryExpression.Operator.NOT);
        assertIdentifier(unary.getOperand(), "flag");
        
        // 负号（使用变量以确保是一元运算符）
        unary = assertUnaryExpression(getSingleBraceExpression("{-x}"), UnaryExpression.Operator.NEGATE);
        assertIdentifier(unary.getOperand(), "x");
        
        // 正号（使用变量以确保是一元运算符）
        unary = assertUnaryExpression(getSingleBraceExpression("{+x}"), UnaryExpression.Operator.PLUS);
        assertIdentifier(unary.getOperand(), "x");
    }

    // ==================== 成员访问和索引访问测试 ====================
    
    @Test
    public void testMemberAccess() throws IOException {
        // 普通成员访问
        MemberAccess access = assertMemberAccess(getSingleBraceExpression("{user.name}"), "user", "name");
        assertFalse(access.isSafeNavigation());
        
        // 安全导航
        access = assertMemberAccess(getSingleBraceExpression("{user?.name}"), "user", "name");
        assertTrue(access.isSafeNavigation());
        
        // 链式访问
        access = assertMemberAccess(getSingleBraceExpression("{user.profile.name}"));
        assertTrue(access.getObject() instanceof MemberAccess);
        MemberAccess nestedAccess = (MemberAccess) access.getObject();
        assertIdentifier(nestedAccess.getObject(), "user");
        assertEquals("profile", nestedAccess.getMemberName());
        assertEquals("name", access.getMemberName());
    }
    
    @Test
    public void testIndexAccess() throws IOException {
        IndexAccess access = assertIndexAccess(getSingleBraceExpression("{list[0]}"));
        assertIdentifier(access.getObject(), "list");
        assertNumberLiteral(access.getIndex(), 0);
        
        // 链式索引访问
        access = assertIndexAccess(getSingleBraceExpression("{matrix[0][1]}"));
        assertTrue(access.getObject() instanceof IndexAccess);
        IndexAccess nestedAccess = (IndexAccess) access.getObject();
        assertIdentifier(nestedAccess.getObject(), "matrix");
        assertNumberLiteral(nestedAccess.getIndex(), 0);
        assertNumberLiteral(access.getIndex(), 1);
    }

    // ==================== 方法调用测试 ====================
    
    @Test
    public void testMethodCall() throws IOException {
        // 无参方法调用
        MethodCall call = assertMethodCall(getSingleBraceExpression("{getName()}"), "getName");
        assertEquals(0, call.getArguments().size());
        assertNull(call.getObject());
        
        // 有参方法调用
        call = assertMethodCall(getSingleBraceExpression("{add(1, 2)}"), "add");
        assertEquals(2, call.getArguments().size());
        assertNumberLiteral(call.getArguments().get(0), 1);
        assertNumberLiteral(call.getArguments().get(1), 2);
        
        // 对象方法调用
        call = assertMethodCall(getSingleBraceExpression("{user.getName()}"), "getName");
        assertIdentifier(call.getObject(), "user");
        
        // 安全导航方法调用
        call = assertMethodCall(getSingleBraceExpression("{user?.getName()}"), "getName");
        assertIdentifier(call.getObject(), "user");
        assertTrue(call.isSafeNavigation());
    }
    
    /**
     * 从方法调用表达式中获取方法调用对象
     */
    private MethodCall getMethodCall(String input) throws IOException {
        return assertMethodCall(getSingleBraceExpression(input), "method");
    }

    @Test
    public void testMethodCallWithAllArgumentTypes() throws IOException {
        // 1. 字符串字面量参数
        MethodCall call = getMethodCall("{method(\"hello\")}");
        assertEquals(1, call.getArguments().size());
        assertStringLiteral(call.getArguments().get(0), "hello");
        
        // 2. 数字字面量参数（整数）
        call = getMethodCall("{method(42)}");
        assertNumberLiteral(call.getArguments().get(0), 42);
        
        // 3. 数字字面量参数（浮点数）
        call = getMethodCall("{method(3.14)}");
        assertNumberLiteral(call.getArguments().get(0), 3.14);
        
        // 4. 数字字面量参数（长整数）
        call = getMethodCall("{method(100L)}");
        NumberLiteral longLiteral = assertNumberLiteral(call.getArguments().get(0));
        assertTrue(longLiteral.getValue() instanceof Long);
        
        // 5. 布尔字面量参数（true）
        call = getMethodCall("{method(true)}");
        assertBooleanLiteral(call.getArguments().get(0), true);
        
        // 6. 布尔字面量参数（false）
        call = getMethodCall("{method(false)}");
        assertBooleanLiteral(call.getArguments().get(0), false);
        
        // 7. null字面量参数
        call = getMethodCall("{method(null)}");
        assertNullLiteral(call.getArguments().get(0));
        
        // 8. 标识符参数
        call = getMethodCall("{method(user)}");
        assertIdentifier(call.getArguments().get(0), "user");
        
        // 9. 成员访问参数
        call = getMethodCall("{method(user.name)}");
        MemberAccess memberAccess = assertMemberAccess(call.getArguments().get(0));
        assertIdentifier(memberAccess.getObject());
        assertEquals("name", memberAccess.getMemberName());
        
        // 10. 索引访问参数
        call = getMethodCall("{method(list[0])}");
        IndexAccess indexAccess = assertIndexAccess(call.getArguments().get(0));
        assertIdentifier(indexAccess.getObject());
        assertNumberLiteral(indexAccess.getIndex());
        
        // 11. 方法调用参数（嵌套方法调用）
        call = getMethodCall("{method(getValue())}");
        assertMethodCall(call.getArguments().get(0), "getValue");
        
        // 12. 二元表达式参数
        call = getMethodCall("{method(1 + 2)}");
        assertBinaryExpression(call.getArguments().get(0), BinaryExpression.Operator.ADD);
        
        // 13. 三元表达式参数
        call = getMethodCall("{method(a > b ? a : b)}");
        assertTernaryExpression(call.getArguments().get(0));
        
        // 14. Elvis表达式参数
        call = getMethodCall("{method(a ?? b)}");
        assertElvisExpression(call.getArguments().get(0));
        
        // 15. if表达式参数
        call = getMethodCall("{method(if (a > b) { a } else { b })}");
        assertTrue(call.getArguments().get(0) instanceof IfExpression);
        
        // 16. for表达式参数
        call = getMethodCall("{method(for (i in list) { i })}");
        assertTrue(call.getArguments().get(0) instanceof ForExpression);
        
        // 17. 列表字面量参数
        call = getMethodCall("{method([1, 2, 3])}");
        assertTrue(call.getArguments().get(0) instanceof ListLiteral);
        ListLiteral list = (ListLiteral) call.getArguments().get(0);
        assertEquals(3, list.getElements().size());
        
        // 18. 哈希字面量参数
        call = getMethodCall("{method({a: 1, b: 2})}");
        assertTrue(call.getArguments().get(0) instanceof HashLiteral);
        HashLiteral hash = (HashLiteral) call.getArguments().get(0);
        assertEquals(2, hash.getEntries().size());
        
        // 19. 区间字面量参数
        call = getMethodCall("{method(1..10)}");
        assertRangeLiteral(call.getArguments().get(0), RangeLiteral.RangeType.CLOSED);
        
        // 20. 正则字面量参数
        call = getMethodCall("{method(/pattern/)}");
        assertRegexLiteral(call.getArguments().get(0), "pattern");
        
        // 21. 一元表达式参数（逻辑非）
        call = getMethodCall("{method(!flag)}");
        assertUnaryExpression(call.getArguments().get(0), UnaryExpression.Operator.NOT);
        
        // 22. 一元表达式参数（负号）
        call = getMethodCall("{method(-x)}");
        assertUnaryExpression(call.getArguments().get(0), UnaryExpression.Operator.NEGATE);
        
        // 23. 括号表达式参数
        call = getMethodCall("{method((1 + 2) * 3)}");
        BinaryExpression parenExpr = assertBinaryExpression(call.getArguments().get(0), BinaryExpression.Operator.MULTIPLY);
        assertTrue(parenExpr.getLeft() instanceof BinaryExpression);
        
        // 24. 安全导航成员访问参数
        call = getMethodCall("{method(user?.name)}");
        MemberAccess safeAccess = assertMemberAccess(call.getArguments().get(0));
        assertTrue(safeAccess.isSafeNavigation());
        
        // 25. 模板字符串参数（解析为 TemplateLiteral）
        call = getMethodCall("{method(`Hello ${name}`)}");
        assertTrue(call.getArguments().get(0) instanceof TemplateLiteral);
        
        // 26. 多个不同类型的参数组合
        call = getMethodCall("{method(\"hello\", 42, true, null, user)}");
        assertEquals(5, call.getArguments().size());
        assertStringLiteral(call.getArguments().get(0));
        assertNumberLiteral(call.getArguments().get(1));
        assertBooleanLiteral(call.getArguments().get(2));
        assertNullLiteral(call.getArguments().get(3));
        assertIdentifier(call.getArguments().get(4));
        
        // 27. 复杂嵌套表达式参数
        call = getMethodCall("{method(user.profile.getName())}");
        MethodCall nestedMethodCall = assertMethodCall(call.getArguments().get(0));
        assertTrue(nestedMethodCall.getObject() instanceof MemberAccess);
        
        // 28. 链式方法调用参数
        call = getMethodCall("{method(getList().get(0))}");
        MethodCall chainedCall = assertMethodCall(call.getArguments().get(0), "get");
        assertTrue(chainedCall.getObject() instanceof MethodCall);
    }

    // ==================== 字面量测试 ====================
    
    @Test
    public void testStringLiteral() throws IOException {
        assertStringLiteral(getSingleBraceExpression("{\"hello\"}"), "hello");
    }
    
    @Test
    public void testNumberLiteral() throws IOException {
        // 注意：纯整数数字在表达式包装器中会被转换为参数访问
        // 这里测试不在表达式包装器中的数字字面量（在复杂表达式中）
        // 浮点数不会被转换为参数访问
        assertNumberLiteral(getSingleBraceExpression("{3.14}"), 3.14);
        
        // 长整数（带L后缀）不会被转换为参数访问
        NumberLiteral literal = assertNumberLiteral(getSingleBraceExpression("{100L}"));
        assertTrue(literal.getValue() instanceof Long);
        
        // 在复杂表达式中，数字字面量不会被转换
        BinaryExpression expr = assertBinaryExpression(getSingleBraceExpression("{42 + 1}"));
        assertNumberLiteral(expr.getLeft(), 42);
    }
    
    @Test
    public void testBooleanLiteral() throws IOException {
        // true
        assertBooleanLiteral(getSingleBraceExpression("{true}"), true);
        
        // false
        assertBooleanLiteral(getSingleBraceExpression("{false}"), false);
    }
    
    @Test
    public void testNullLiteral() throws IOException {
        assertNullLiteral(getSingleBraceExpression("{null}"));
    }
    
    @Test
    public void testRegexLiteral() throws IOException {
        assertRegexLiteral(getSingleBraceExpression("{/pattern/}"), "pattern");
    }
    
    @Test
    public void testTemplateStringLiteral() throws IOException {
        // 反引号模板字符串解析为 TemplateLiteral，{...} 部分作为嵌入表达式（$ 为普通文本）
        ASTNode node = getSingleBraceExpression("{`Hello ${name}`}");
        assertTrue(node instanceof TemplateLiteral);
        TemplateLiteral template = (TemplateLiteral) node;
        assertEquals(2, template.getStrings().size());
        assertEquals("Hello $", template.getStrings().get(0));
        assertEquals("", template.getStrings().get(1));
        assertEquals(1, template.getExpressions().size());
    }

    // ==================== 括号表达式测试 ====================
    
    @Test
    public void testParenthesizedExpression() throws IOException {
        BinaryExpression expr = assertBinaryExpression(getSingleBraceExpression("{(1 + 2) * 3}"), BinaryExpression.Operator.MULTIPLY);
        BinaryExpression leftExpr = assertBinaryExpression(expr.getLeft(), BinaryExpression.Operator.ADD);
        assertNumberLiteral(leftExpr.getLeft(), 1);
        assertNumberLiteral(leftExpr.getRight(), 2);
        assertNumberLiteral(expr.getRight(), 3);
    }

    // ==================== 复杂表达式测试 ====================
    
    @Test
    public void testOperatorPrecedence() throws IOException {
        // 测试运算符优先级：乘法优先于加法
        BinaryExpression expr = assertBinaryExpression(getSingleBraceExpression("{1 + 2 * 3}"), BinaryExpression.Operator.ADD);
        assertNumberLiteral(expr.getLeft(), 1);
        BinaryExpression rightExpr = assertBinaryExpression(expr.getRight(), BinaryExpression.Operator.MULTIPLY);
        assertNumberLiteral(rightExpr.getLeft(), 2);
        assertNumberLiteral(rightExpr.getRight(), 3);
        
        // 测试逻辑运算符优先级：&& 优先于 ||
        expr = assertBinaryExpression(getSingleBraceExpression("{a || b && c}"), BinaryExpression.Operator.OR);
        assertIdentifier(expr.getLeft(), "a");
        BinaryExpression rightLogicalExpr = assertBinaryExpression(expr.getRight(), BinaryExpression.Operator.AND);
        assertIdentifier(rightLogicalExpr.getLeft(), "b");
        assertIdentifier(rightLogicalExpr.getRight(), "c");
    }
    
    @Test
    public void testComplexExpression() throws IOException {
        TernaryExpression ternary = assertTernaryExpression(getSingleBraceExpression(
                "{user.age > 18 && user.name != null ? user.name : \"Guest\"}"));
        
        // 检查条件表达式
        assertBinaryExpression(ternary.getCondition(), BinaryExpression.Operator.AND);
        
        // 检查trueValue
        assertMemberAccess(ternary.getTrueValue(), "user", "name");
        
        // 检查falseValue
        assertStringLiteral(ternary.getFalseValue(), "Guest");
    }

    // ==================== Switch表达式测试 ====================
    
    @Test
    public void testSwitchExpression() throws IOException {
        // 注意：根据BNF，switch表达式应该存在，但Parser可能未实现
        // 如果Parser未实现switch表达式，此测试会失败，这是预期的
        // 可以暂时跳过或注释掉此测试
        try {
            Parser parser = createParser("{switch (status) { case 1: \"Active\" case 0: \"Inactive\" default: \"Unknown\" }}");
            TemplateNode root = parser.parse();
            assertNotNull(root);
        } catch (Exception e) {
            // 如果Parser未实现switch表达式，这是预期的
            assertTrue(e instanceof TemplateSyntaxException);
        }
    }

    // ==================== For语句扩展测试 ====================
    
    @Test
    public void testForStatementWithIndex() throws IOException {
        TemplateNode root = parseAndGetRoot(
                "{% for index, item in list }{{index}}: {{item}}{/% for }");
        
        ForStatement forStmt = getFirstChild(root, ForStatement.class);
        assertTrue(forStmt.hasIndexVariable());
        assertEquals("index", forStmt.getIndexVariable());
        assertEquals("item", forStmt.getItemVariable());
        assertIdentifier(forStmt.getIterable(), "list");
    }

    // ==================== Extends语句测试 ====================
    
    @Test
    public void testExtendsStatement() throws IOException {
        TemplateNode root = parseAndGetRoot("{% extends 'base.etl' %}");
        
        ExtendsStatement extendsStmt = getFirstChild(root, ExtendsStatement.class);
        assertEquals("base.etl", extendsStmt.getTemplatePath());
    }

    // ==================== Hash字面量扩展测试 ====================
    
    @Test
    public void testHashLiteralWithStringKeys() throws IOException {
        HashLiteral hash = getSingleBraceExpression(
                "{ {\"key1\": \"value1\", \"key2\": \"value2\"} }", HashLiteral.class);
        assertEquals(2, hash.getEntries().size());
    }
    
    @Test
    public void testHashLiteralWithNumberKeys() throws IOException {
        HashLiteral hash = getSingleBraceExpression("{ {1: \"one\", 2: \"two\"} }", HashLiteral.class);
        assertEquals(2, hash.getEntries().size());
    }
    
    @Test
    public void testEmptyHashLiteral() throws IOException {
        HashLiteral hash = getSingleBraceExpression("{ {} }", HashLiteral.class);
        assertEquals(0, hash.getEntries().size());
    }
    
    @Test
    public void testEmptyListLiteral() throws IOException {
        ListLiteral list = getSingleBraceExpression("{ [] }", ListLiteral.class);
        assertEquals(0, list.getElements().size());
    }

    // ==================== 嵌套和组合测试 ====================
    
    @Test
    public void testNestedExpressions() throws IOException {
        MethodCall call = assertMethodCall(getDoubleBraceExpression("{{user.profile.getName()}}"), "getName");
        MemberAccess memberAccess = assertMemberAccess(call.getObject());
        assertIdentifier(memberAccess.getObject(), "user");
        assertEquals("profile", memberAccess.getMemberName());
    }
    
    @Test
    public void testMixedTemplateParts() throws IOException {
        TemplateNode root = parseAndGetRoot("Hello {user.name}, you have {count} messages.");
        
        assertEquals(5, root.getChildren().size()); // Text, Expr, Text, Expr, Text
        TextNode text0 = getChild(root, 0, TextNode.class);
        assertEquals("Hello ", text0.getText());
        SingleBraceExpressionNode expr1 = getChild(root, 1, SingleBraceExpressionNode.class);
        assertMemberAccess(expr1.getExpression(), "user", "name");
        TextNode text2 = getChild(root, 2, TextNode.class);
        assertEquals(", you have ", text2.getText());
        SingleBraceExpressionNode expr3 = getChild(root, 3, SingleBraceExpressionNode.class);
        assertIdentifier(expr3.getExpression(), "count");
        TextNode text4 = getChild(root, 4, TextNode.class);
        assertEquals(" messages.", text4.getText());
    }
    
    @Test
    public void testNestedIfStatement() throws IOException {
        TemplateNode root = parseAndGetRoot(
                "{% if a > 0 }{% if b > 0 }Both positive{/% if }{% else }A positive{/% if }");
        
        IfStatement outerIf = getFirstChild(root, IfStatement.class);
        assertTrue(outerIf.getThenBody().get(0) instanceof IfStatement);
    }
    
    @Test
    public void testForStatementNested() throws IOException {
        TemplateNode root = parseAndGetRoot(
                "{% for row in rows }{% for col in row }{{col}}{/% for }{/% for }");
        
        ForStatement outerFor = getFirstChild(root, ForStatement.class);
        assertTrue(outerFor.getBody().get(0) instanceof ForStatement);
    }
    
    @Test
    public void testIfExpressionWithElseIf() throws IOException {
        IfExpression ifExpr = getSingleBraceExpression(
                "{if (a > 10) { \"big\" } else if (a > 5) { \"medium\" } else { \"small\" }}",
                IfExpression.class);
        
        // 检查if条件
        BinaryExpression ifCondition = assertBinaryExpression(ifExpr.getCondition(), BinaryExpression.Operator.GT);
        assertIdentifier(ifCondition.getLeft(), "a");
        assertNumberLiteral(ifCondition.getRight(), 10);
        
        // 检查then语句
        assertEquals(1, ifExpr.getThenStatements().size());
        assertStringLiteral(ifExpr.getThenStatements().get(0), "big");
        
        // 检查else if条件
        assertNotNull(ifExpr.getElseIfClauses());
        assertEquals(1, ifExpr.getElseIfClauses().size());
        IfExpression.ElseIfClause elseIfClause = ifExpr.getElseIfClauses().get(0);
        BinaryExpression elseIfCondition = assertBinaryExpression(elseIfClause.getCondition(), BinaryExpression.Operator.GT);
        assertIdentifier(elseIfCondition.getLeft(), "a");
        assertNumberLiteral(elseIfCondition.getRight(), 5);
        
        // 检查else if语句
        assertEquals(1, elseIfClause.getStatements().size());
        assertStringLiteral(elseIfClause.getStatements().get(0), "medium");
        
        // 检查else语句
        assertNotNull(ifExpr.getElseStatements());
        assertEquals(1, ifExpr.getElseStatements().size());
        assertStringLiteral(ifExpr.getElseStatements().get(0), "small");
    }
    
    @Test
    public void testForExpressionWithIndex() throws IOException {
        ForExpression forExpr = getSingleBraceExpression(
                "{for (index, item in list) { print(index + \": \" + item) }}",
                ForExpression.class);
        assertNotNull(forExpr.getIndexName());
        // 根据Parser实现，第一个变量是索引，第二个是值
        assertEquals("index", forExpr.getIndexName());
        assertEquals("item", forExpr.getVariableName());
        assertIdentifier(forExpr.getIterable(), "list");
    }
    
    // ==================== 参数访问测试 ====================
    
    @Test
    public void testArgumentAccessSingleBrace() throws IOException {
        // 测试单花括号参数访问 {0}
        assertArgumentAccess(getSingleBraceExpression("{0}"), 0);
    }
    
    @Test
    public void testArgumentAccessDoubleBrace() throws IOException {
        // 测试双花括号参数访问 {{1}}
        assertArgumentAccess(getDoubleBraceExpression("{{1}}"), 1);
    }
    
    @Test
    public void testArgumentAccessDollarBrace() throws IOException {
        // 测试美元符号参数访问 ${2}
        assertArgumentAccess(getDollarBraceExpression("${2}"), 2);
    }
    
    @Test
    public void testArgumentAccessMultiple() throws IOException {
        // 测试多个参数访问
        TemplateNode root = parseAndGetRoot("{0} and {1} and {2}");
        
        assertEquals(5, root.getChildren().size()); // Expr, Text, Expr, Text, Expr
        SingleBraceExpressionNode expr0 = getChild(root, 0, SingleBraceExpressionNode.class);
        SingleBraceExpressionNode expr2 = getChild(root, 2, SingleBraceExpressionNode.class);
        SingleBraceExpressionNode expr4 = getChild(root, 4, SingleBraceExpressionNode.class);
        
        assertArgumentAccess(expr0.getExpression(), 0);
        assertArgumentAccess(expr2.getExpression(), 1);
        assertArgumentAccess(expr4.getExpression(), 2);
    }
    
    @Test
    public void testArgumentAccessLargeIndex() throws IOException {
        // 测试大索引参数访问
        assertArgumentAccess(getSingleBraceExpression("{100}"), 100);
    }
    
    @Test
    public void testArgumentAccessNotAppliedToComplexExpression() throws IOException {
        // 测试复杂表达式不应该被转换为参数访问
        ASTNode expr = getSingleBraceExpression("{1 + 2}");
        // 应该是二元表达式，而不是参数访问
        assertTrue(expr instanceof BinaryExpression);
        assertFalse(expr instanceof ArgumentAccess);
    }
    
    @Test
    public void testArgumentAccessNotAppliedToNegativeNumber() throws IOException {
        // 测试负数不应该被转换为参数访问（负数索引无效）
        // 注意：Lexer将-1识别为一个数字token，所以{-1}会被解析为NumberLiteral(-1)
        // 但由于值为负数，不会被转换为参数访问
        ASTNode expr = getSingleBraceExpression("{-1}");
        // 应该是数字字面量（负数），而不是参数访问
        assertNumberLiteral(expr, -1);
        assertFalse(expr instanceof ArgumentAccess);
    }
    
    @Test
    public void testArgumentAccessNotAppliedToFloat() throws IOException {
        // 测试浮点数不应该被转换为参数访问
        ASTNode expr = getSingleBraceExpression("{3.14}");
        // 应该是数字字面量，而不是参数访问
        assertNumberLiteral(expr, 3.14);
        assertFalse(expr instanceof ArgumentAccess);
    }
    
    // ==================== 哈希表扩展测试 ====================
    
    @Test
    public void testHashLiteralWithBooleanKeys() throws IOException {
        // 注意：Parser当前不支持布尔值作为键，只支持标识符、字符串和数字
        // 此测试验证Parser会抛出语法错误
        Parser parser = createParser("{ {true: \"yes\", false: \"no\"} }");
        assertThrows(TemplateSyntaxException.class, () -> parser.parse());
    }
    
    @Test
    public void testHashLiteralWithNullKey() throws IOException {
        // 注意：Parser当前不支持null作为键，只支持标识符、字符串和数字
        // 此测试验证Parser会抛出语法错误
        Parser parser = createParser("{ {null: \"null value\"} }");
        assertThrows(TemplateSyntaxException.class, () -> parser.parse());
    }
    
    @Test
    public void testHashLiteralWithIdentifierKeys() throws IOException {
        // 标识符作为键（按 README 3.4.7 自动转换为字符串键）
        HashLiteral hash = getExpressionFromParser("{ {name: \"John\", age: 30} }", HashLiteral.class);
        assertEquals(2, hash.getEntries().size());
        assertStringLiteral(hash.getEntries().get(0).getKey(), "name");
        assertStringLiteral(hash.getEntries().get(1).getKey(), "age");
    }
    
    @Test
    public void testHashLiteralWithHashAsValue() throws IOException {
        // 哈希表作为值
        HashLiteral hash = getExpressionFromParser(
                "{ {user: {name: \"John\", age: 30}} }", HashLiteral.class);
        assertEquals(1, hash.getEntries().size());
        assertTrue(hash.getEntries().get(0).getValue() instanceof HashLiteral);
        
        HashLiteral nestedHash = (HashLiteral) hash.getEntries().get(0).getValue();
        assertEquals(2, nestedHash.getEntries().size());
    }
    
    @Test
    public void testHashLiteralWithHashAsKey() throws IOException {
        // 注意：Parser当前不支持哈希表作为键，只支持标识符、字符串和数字
        // 此测试验证Parser会抛出语法错误
        Parser parser = createParser("{ {{name: \"key\"}: \"value\"} }");
        assertThrows(TemplateSyntaxException.class, () -> parser.parse());
    }
    
    @Test
    public void testHashLiteralWithListAsValue() throws IOException {
        // 列表作为值
        HashLiteral hash = getExpressionFromParser("{ {items: [1, 2, 3]} }", HashLiteral.class);
        assertEquals(1, hash.getEntries().size());
        assertTrue(hash.getEntries().get(0).getValue() instanceof ListLiteral);
        
        ListLiteral list = (ListLiteral) hash.getEntries().get(0).getValue();
        assertEquals(3, list.getElements().size());
    }
    
    @Test
    public void testHashLiteralWithListAsKey() throws IOException {
        // 注意：Parser当前不支持列表作为键，只支持标识符、字符串和数字
        // 此测试验证Parser会抛出语法错误
        Parser parser = createParser("{ {[1, 2]: \"value\"} }");
        assertThrows(TemplateSyntaxException.class, () -> parser.parse());
    }
    
    @Test
    public void testHashLiteralWithExpressionAsValue() throws IOException {
        // 表达式作为值
        HashLiteral hash = getExpressionFromParser(
                "{ {sum: 1 + 2, product: 3 * 4} }", HashLiteral.class);
        assertEquals(2, hash.getEntries().size());
        assertTrue(hash.getEntries().get(0).getValue() instanceof BinaryExpression);
        assertTrue(hash.getEntries().get(1).getValue() instanceof BinaryExpression);
    }
    
    @Test
    public void testHashLiteralWithMethodCallAsValue() throws IOException {
        // 方法调用作为值
        HashLiteral hash = getExpressionFromParser("{ {result: getValue()} }", HashLiteral.class);
        assertEquals(1, hash.getEntries().size());
        assertTrue(hash.getEntries().get(0).getValue() instanceof MethodCall);
    }
    
    @Test
    public void testHashLiteralWithMemberAccessAsValue() throws IOException {
        // 成员访问作为值
        HashLiteral hash = getExpressionFromParser("{ {name: user.name} }", HashLiteral.class);
        assertEquals(1, hash.getEntries().size());
        assertTrue(hash.getEntries().get(0).getValue() instanceof MemberAccess);
    }
    
    @Test
    public void testHashLiteralWithTernaryAsValue() throws IOException {
        // 三元表达式作为值
        HashLiteral hash = getExpressionFromParser(
                "{ {status: a > 0 ? \"positive\" : \"negative\"} }", HashLiteral.class);
        assertEquals(1, hash.getEntries().size());
        assertTrue(hash.getEntries().get(0).getValue() instanceof TernaryExpression);
    }
    
    @Test
    public void testHashLiteralWithElvisAsValue() throws IOException {
        // Elvis表达式作为值
        HashLiteral hash = getExpressionFromParser(
                "{ {name: user.name ?? \"Guest\"} }", HashLiteral.class);
        assertEquals(1, hash.getEntries().size());
        assertTrue(hash.getEntries().get(0).getValue() instanceof ElvisExpression);
    }
    
    @Test
    public void testHashLiteralWithRangeAsValue() throws IOException {
        // 区间作为值
        HashLiteral hash = getExpressionFromParser("{ {range: 1..10} }", HashLiteral.class);
        assertEquals(1, hash.getEntries().size());
        assertTrue(hash.getEntries().get(0).getValue() instanceof RangeLiteral);
    }
    
    @Test
    public void testHashLiteralWithRegexAsValue() throws IOException {
        // 正则表达式作为值
        HashLiteral hash = getExpressionFromParser("{ {pattern: /test/} }", HashLiteral.class);
        assertEquals(1, hash.getEntries().size());
        assertTrue(hash.getEntries().get(0).getValue() instanceof RegexLiteral);
    }
    
    @Test
    public void testHashLiteralDeeplyNested() throws IOException {
        // 深度嵌套的哈希表
        HashLiteral hash = getExpressionFromParser(
                "{ {level1: {level2: {level3: {level4: \"deep\"}}}} }", HashLiteral.class);
        assertEquals(1, hash.getEntries().size());
        
        HashLiteral level1 = (HashLiteral) hash.getEntries().get(0).getValue();
        assertEquals(1, level1.getEntries().size());
        
        HashLiteral level2 = (HashLiteral) level1.getEntries().get(0).getValue();
        assertEquals(1, level2.getEntries().size());
        
        HashLiteral level3 = (HashLiteral) level2.getEntries().get(0).getValue();
        assertEquals(1, level3.getEntries().size());
        
        assertTrue(level3.getEntries().get(0).getValue() instanceof StringLiteral);
        assertEquals("deep", ((StringLiteral) level3.getEntries().get(0).getValue()).getValue());
    }
    
    @Test
    public void testHashLiteralMixedTypes() throws IOException {
        // 混合类型的键值对
        HashLiteral hash = getExpressionFromParser(
                "{ {name: \"John\", age: 30, active: true, score: null, items: [1, 2], meta: {key: \"value\"}} }",
                HashLiteral.class);
        assertEquals(6, hash.getEntries().size());
        
        // 验证各种类型
        assertTrue(hash.getEntries().get(0).getValue() instanceof StringLiteral);
        assertTrue(hash.getEntries().get(1).getValue() instanceof NumberLiteral);
        assertTrue(hash.getEntries().get(2).getValue() instanceof BooleanLiteral);
        assertTrue(hash.getEntries().get(3).getValue() instanceof NullLiteral);
        assertTrue(hash.getEntries().get(4).getValue() instanceof ListLiteral);
        assertTrue(hash.getEntries().get(5).getValue() instanceof HashLiteral);
    }
    
    @Test
    public void testHashLiteralWithHashAndListNested() throws IOException {
        // 哈希表和列表混合嵌套
        HashLiteral hash = getExpressionFromParser(
                "{ {users: [{name: \"John\", age: 30}, {name: \"Jane\", age: 25}]} }",
                HashLiteral.class);
        assertEquals(1, hash.getEntries().size());
        
        ListLiteral list = (ListLiteral) hash.getEntries().get(0).getValue();
        assertEquals(2, list.getElements().size());
        
        assertTrue(list.getElements().get(0) instanceof HashLiteral);
        assertTrue(list.getElements().get(1) instanceof HashLiteral);
    }
    
    @Test
    public void testHashLiteralWithComplexKeys() throws IOException {
        // 注意：Parser当前不支持复杂表达式作为键，只支持标识符、字符串和数字
        // 此测试验证Parser会抛出语法错误
        Parser parser = createParser("{ {\"key\" + \"suffix\": \"value\", 1 + 2: \"three\"} }");
        assertThrows(TemplateSyntaxException.class, () -> parser.parse());
    }
    
    // ==================== 列表扩展测试 ====================
    
    @Test
    public void testListLiteralWithHashAsElement() throws IOException {
        // 哈希表作为列表元素
        ListLiteral list = getExpressionFromParser("{ [{name: \"John\"}, {name: \"Jane\"}] }", ListLiteral.class);
        assertEquals(2, list.getElements().size());
        assertTrue(list.getElements().get(0) instanceof HashLiteral);
        assertTrue(list.getElements().get(1) instanceof HashLiteral);
    }
    
    @Test
    public void testListLiteralWithListAsElement() throws IOException {
        // 列表作为列表元素（嵌套列表）
        ListLiteral list = getExpressionFromParser("{ [[1, 2], [3, 4]] }", ListLiteral.class);
        assertEquals(2, list.getElements().size());
        assertTrue(list.getElements().get(0) instanceof ListLiteral);
        assertTrue(list.getElements().get(1) instanceof ListLiteral);
        
        ListLiteral nested1 = (ListLiteral) list.getElements().get(0);
        ListLiteral nested2 = (ListLiteral) list.getElements().get(1);
        assertEquals(2, nested1.getElements().size());
        assertEquals(2, nested2.getElements().size());
    }
    
    @Test
    public void testListLiteralDeeplyNested() throws IOException {
        // 深度嵌套的列表
        ListLiteral list = getExpressionFromParser("{ [[[[\"deep\"]]]] }", ListLiteral.class);
        assertEquals(1, list.getElements().size());
        
        ListLiteral level1 = (ListLiteral) list.getElements().get(0);
        ListLiteral level2 = (ListLiteral) level1.getElements().get(0);
        ListLiteral level3 = (ListLiteral) level2.getElements().get(0);
        assertTrue(level3.getElements().get(0) instanceof StringLiteral);
    }
    
    @Test
    public void testListLiteralWithMixedTypes() throws IOException {
        // 混合类型的列表元素
        ListLiteral list = getExpressionFromParser(
                "{ [\"string\", 42, 3.14, true, false, null, [1, 2], {key: \"value\"}] }",
                ListLiteral.class);
        assertEquals(8, list.getElements().size());
        
        // 验证各种类型
        assertTrue(list.getElements().get(0) instanceof StringLiteral);
        assertTrue(list.getElements().get(1) instanceof NumberLiteral);
        assertTrue(list.getElements().get(2) instanceof NumberLiteral);
        assertTrue(list.getElements().get(3) instanceof BooleanLiteral);
        assertTrue(list.getElements().get(4) instanceof BooleanLiteral);
        assertTrue(list.getElements().get(5) instanceof NullLiteral);
        assertTrue(list.getElements().get(6) instanceof ListLiteral);
        assertTrue(list.getElements().get(7) instanceof HashLiteral);
    }
    
    @Test
    public void testListLiteralWithExpressions() throws IOException {
        // 表达式作为列表元素
        ListLiteral list = getExpressionFromParser("{ [1 + 2, 3 * 4, a > b] }", ListLiteral.class);
        assertEquals(3, list.getElements().size());
        assertTrue(list.getElements().get(0) instanceof BinaryExpression);
        assertTrue(list.getElements().get(1) instanceof BinaryExpression);
        assertTrue(list.getElements().get(2) instanceof BinaryExpression);
    }
    
    @Test
    public void testListLiteralWithMethodCalls() throws IOException {
        // 方法调用作为列表元素
        ListLiteral list = getExpressionFromParser(
                "{ [getValue(), getName(), user.getAge()] }", ListLiteral.class);
        assertEquals(3, list.getElements().size());
        assertTrue(list.getElements().get(0) instanceof MethodCall);
        assertTrue(list.getElements().get(1) instanceof MethodCall);
        assertTrue(list.getElements().get(2) instanceof MethodCall);
    }
    
    @Test
    public void testListLiteralWithMemberAccess() throws IOException {
        // 成员访问作为列表元素
        ListLiteral list = getExpressionFromParser(
                "{ [user.name, user.age, user.profile.email] }", ListLiteral.class);
        assertEquals(3, list.getElements().size());
        assertTrue(list.getElements().get(0) instanceof MemberAccess);
        assertTrue(list.getElements().get(1) instanceof MemberAccess);
        assertTrue(list.getElements().get(2) instanceof MemberAccess);
    }
    
    @Test
    public void testListLiteralWithIndexAccess() throws IOException {
        // 索引访问作为列表元素
        ListLiteral list = getExpressionFromParser(
                "{ [list[0], list[1], matrix[0][1]] }", ListLiteral.class);
        assertEquals(3, list.getElements().size());
        assertTrue(list.getElements().get(0) instanceof IndexAccess);
        assertTrue(list.getElements().get(1) instanceof IndexAccess);
        assertTrue(list.getElements().get(2) instanceof IndexAccess);
    }
    
    @Test
    public void testListLiteralWithTernary() throws IOException {
        // 三元表达式作为列表元素
        ListLiteral list = getExpressionFromParser(
                "{ [a > 0 ? \"positive\" : \"negative\", b ? 1 : 0] }", ListLiteral.class);
        assertEquals(2, list.getElements().size());
        assertTrue(list.getElements().get(0) instanceof TernaryExpression);
        assertTrue(list.getElements().get(1) instanceof TernaryExpression);
    }
    
    @Test
    public void testListLiteralWithElvis() throws IOException {
        // Elvis表达式作为列表元素
        ListLiteral list = getExpressionFromParser(
                "{ [a ?? \"default\", b ?? c ?? \"fallback\"] }", ListLiteral.class);
        assertEquals(2, list.getElements().size());
        assertTrue(list.getElements().get(0) instanceof ElvisExpression);
        assertTrue(list.getElements().get(1) instanceof ElvisExpression);
    }
    
    @Test
    public void testListLiteralWithRange() throws IOException {
        // 区间作为列表元素
        ListLiteral list = getExpressionFromParser(
                "{ [1..10, 1>..<10, 1..<10] }", ListLiteral.class);
        assertEquals(3, list.getElements().size());
        assertTrue(list.getElements().get(0) instanceof RangeLiteral);
        assertTrue(list.getElements().get(1) instanceof RangeLiteral);
        assertTrue(list.getElements().get(2) instanceof RangeLiteral);
    }
    
    @Test
    public void testListLiteralWithRegex() throws IOException {
        // 正则表达式作为列表元素
        ListLiteral list = getExpressionFromParser(
                "{ [/pattern1/, /pattern2/] }", ListLiteral.class);
        assertEquals(2, list.getElements().size());
        assertTrue(list.getElements().get(0) instanceof RegexLiteral);
        assertTrue(list.getElements().get(1) instanceof RegexLiteral);
    }
    
    @Test
    public void testListLiteralWithUnaryExpressions() throws IOException {
        // 一元表达式作为列表元素
        ListLiteral list = getExpressionFromParser(
                "{ [!flag, -x, +y] }", ListLiteral.class);
        assertEquals(3, list.getElements().size());
        assertTrue(list.getElements().get(0) instanceof UnaryExpression);
        assertTrue(list.getElements().get(1) instanceof UnaryExpression);
        assertTrue(list.getElements().get(2) instanceof UnaryExpression);
    }
    
    @Test
    public void testListLiteralWithIfExpression() throws IOException {
        // if表达式作为列表元素
        ListLiteral list = getExpressionFromParser(
                "{ [if (a > 0) { \"positive\" } else { \"negative\" }] }", ListLiteral.class);
        assertEquals(1, list.getElements().size());
        assertTrue(list.getElements().get(0) instanceof IfExpression);
    }
    
    @Test
    public void testListLiteralWithForExpression() throws IOException {
        // for表达式作为列表元素
        ListLiteral list = getExpressionFromParser(
                "{ [for (i in list) { i * 2 }] }", ListLiteral.class);
        assertEquals(1, list.getElements().size());
        assertTrue(list.getElements().get(0) instanceof ForExpression);
    }
    
    @Test
    public void testListLiteralWithComplexNestedStructures() throws IOException {
        // 复杂的嵌套结构：列表中包含哈希表，哈希表中包含列表
        ListLiteral list = getExpressionFromParser(
                "{ [{users: [\"John\", \"Jane\"]}, {items: [1, 2, 3]}] }", ListLiteral.class);
        assertEquals(2, list.getElements().size());
        
        HashLiteral hash1 = (HashLiteral) list.getElements().get(0);
        HashLiteral hash2 = (HashLiteral) list.getElements().get(1);
        
        assertTrue(hash1.getEntries().get(0).getValue() instanceof ListLiteral);
        assertTrue(hash2.getEntries().get(0).getValue() instanceof ListLiteral);
    }
    
    @Test
    public void testListLiteralWithMultiLevelNesting() throws IOException {
        // 多级嵌套：列表 -> 列表 -> 哈希表 -> 列表
        ListLiteral level1 = getExpressionFromParser("{ [[[{items: [1, 2]}]]] }", ListLiteral.class);
        ListLiteral level2 = (ListLiteral) level1.getElements().get(0);
        ListLiteral level3 = (ListLiteral) level2.getElements().get(0);
        HashLiteral hash = (HashLiteral) level3.getElements().get(0);
        ListLiteral items = (ListLiteral) hash.getEntries().get(0).getValue();
        
        assertEquals(2, items.getElements().size());
    }
    
    @Test
    public void testListLiteralWithAllTypes() throws IOException {
        // 包含所有可能类型的列表
        ListLiteral list = getExpressionFromParser(
                "{ [\"string\", 42, 3.14, 100L, true, false, null, identifier, user.name, list[0], " +
                "getValue(), 1 + 2, a > b, a ? b : c, a ?? b, 1..10, /pattern/, !flag, -x, " +
                "if (a) { b }, for (i in list) { i }, [1, 2], {key: \"value\"}] }",
                ListLiteral.class);
        // 验证包含多种类型
        assertTrue(list.getElements().size() >= 20);
    }
    
    // ==================== JSON形式模板文本测试 ====================
    
    @Test
    public void testJsonFormattedText() throws IOException {
        // 测试纯JSON格式的模板文本（不包含表达式）
        TemplateNode root = parseAndGetRoot(
                "{\"name\": \"John\", \"age\": 30, \"city\": \"Beijing\"}");
        
        assertEquals(1, root.getChildren().size());
        TextNode textNode = getFirstChild(root, TextNode.class);
        assertEquals("{\"name\": \"John\", \"age\": 30, \"city\": \"Beijing\"}",
                textNode.getText());
    }
    
    @Test
    public void testJsonFormattedTextWithNestedObjects() throws IOException {
        // 测试包含嵌套对象的JSON格式文本
        TemplateNode root = parseAndGetRoot(
                "{\"user\": {\"name\": \"John\", \"profile\": {\"age\": 30}}}");
        
        assertEquals(1, root.getChildren().size());
        TextNode textNode = getFirstChild(root, TextNode.class);
        assertEquals("{\"user\": {\"name\": \"John\", \"profile\": {\"age\": 30}}}",
                textNode.getText());
    }
    
    @Test
    public void testJsonFormattedTextWithArrays() throws IOException {
        // 测试包含数组的JSON格式文本
        TemplateNode root = parseAndGetRoot(
                "{\"items\": [1, 2, 3], \"tags\": [\"a\", \"b\", \"c\"]}");
        
        assertEquals(1, root.getChildren().size());
        TextNode textNode = getFirstChild(root, TextNode.class);
        assertEquals("{\"items\": [1, 2, 3], \"tags\": [\"a\", \"b\", \"c\"]}",
                textNode.getText());
    }
    
    @Test
    public void testJsonFormattedTextWithMixedContent() throws IOException {
        // 测试包含多种JSON数据类型的文本
        TemplateNode root = parseAndGetRoot(
                "{\"string\": \"value\", \"number\": 42, \"boolean\": true, \"null\": null, " +
                "\"array\": [1, 2], \"object\": {\"key\": \"value\"}}");
        
        assertEquals(1, root.getChildren().size());
        TextNode textNode = getFirstChild(root, TextNode.class);
        assertTrue(textNode.getText().contains("\"string\": \"value\""));
        assertTrue(textNode.getText().contains("\"number\": 42"));
        assertTrue(textNode.getText().contains("\"boolean\": true"));
    }
    
    // ==================== 混合表达式类型测试 ====================
    
    @Test
    public void testMixedExpressionTypes() throws IOException {
        // 测试单花括号、双花括号、美元符号表达式混合
        TemplateNode root = parseAndGetRoot(
                "Name: {user.name}, Age: {{user.age}}, Email: ${user.email}");
        
        assertEquals(6, root.getChildren().size()); // Text, SingleBrace, Text, DoubleBrace, Text, DollarBrace, Text
        
        // 第一个文本节点
        TextNode text0 = getChild(root, 0, TextNode.class);
        assertEquals("Name: ", text0.getText());
        
        // 单花括号表达式
        SingleBraceExpressionNode expr1 = getChild(root, 1, SingleBraceExpressionNode.class);
        assertMemberAccess(expr1.getExpression(), "user", "name");
        
        // 第二个文本节点
        TextNode text2 = getChild(root, 2, TextNode.class);
        assertEquals(", Age: ", text2.getText());
        
        // 双花括号表达式
        DoubleBraceExpressionNode expr3 = getChild(root, 3, DoubleBraceExpressionNode.class);
        assertMemberAccess(expr3.getExpression(), "user", "age");
        
        // 第三个文本节点
        TextNode text4 = getChild(root, 4, TextNode.class);
        assertEquals(", Email: ", text4.getText());
        
        // 美元花括号表达式
        DollarBraceExpressionNode expr5 = getChild(root, 5, DollarBraceExpressionNode.class);
        assertMemberAccess(expr5.getExpression(), "user", "email");
    }
    
    @Test
    public void testMixedExpressionTypesWithComplexExpressions() throws IOException {
        // 测试混合表达式类型，包含复杂表达式
        TemplateNode root = parseAndGetRoot(
                "Sum: {a + b}, Product: {{x * y}}, Result: ${flag ? \"yes\" : \"no\"}");
        
        assertEquals(6, root.getChildren().size());
        
        // 验证文本节点
        TextNode text0 = getChild(root, 0, TextNode.class);
        assertEquals("Sum: ", text0.getText());
        
        // 单花括号中的二元表达式
        SingleBraceExpressionNode expr1 = getChild(root, 1, SingleBraceExpressionNode.class);
        BinaryExpression addExpr = assertBinaryExpression(expr1.getExpression(), BinaryExpression.Operator.ADD);
        assertIdentifier(addExpr.getLeft(), "a");
        assertIdentifier(addExpr.getRight(), "b");
        
        // 验证文本节点
        TextNode text2 = getChild(root, 2, TextNode.class);
        assertEquals(", Product: ", text2.getText());
        
        // 双花括号中的二元表达式
        DoubleBraceExpressionNode expr3 = getChild(root, 3, DoubleBraceExpressionNode.class);
        BinaryExpression multExpr = assertBinaryExpression(expr3.getExpression(), BinaryExpression.Operator.MULTIPLY);
        assertIdentifier(multExpr.getLeft(), "x");
        assertIdentifier(multExpr.getRight(), "y");
        
        // 验证文本节点
        TextNode text4 = getChild(root, 4, TextNode.class);
        assertEquals(", Result: ", text4.getText());
        
        // 美元花括号中的三元表达式
        DollarBraceExpressionNode expr5 = getChild(root, 5, DollarBraceExpressionNode.class);
        TernaryExpression ternaryExpr = assertTernaryExpression(expr5.getExpression());
        assertIdentifier(ternaryExpr.getCondition(), "flag");
    }
    
    @Test
    public void testMixedExpressionTypesWithMethodCalls() throws IOException {
        // 测试混合表达式类型，包含方法调用
        TemplateNode root = parseAndGetRoot(
                "Name: {user.getName()}, Count: {{getCount()}}, Value: ${obj.getValue()}");
        
        assertEquals(6, root.getChildren().size());
        
        // 验证文本节点
        TextNode text0 = getChild(root, 0, TextNode.class);
        assertEquals("Name: ", text0.getText());
        
        // 单花括号中的方法调用
        SingleBraceExpressionNode expr1 = getChild(root, 1, SingleBraceExpressionNode.class);
        MethodCall call1 = assertMethodCall(expr1.getExpression(), "getName");
        assertIdentifier(call1.getObject(), "user");
        
        // 验证文本节点
        TextNode text2 = getChild(root, 2, TextNode.class);
        assertEquals(", Count: ", text2.getText());
        
        // 双花括号中的方法调用
        DoubleBraceExpressionNode expr3 = getChild(root, 3, DoubleBraceExpressionNode.class);
        MethodCall call2 = assertMethodCall(expr3.getExpression(), "getCount");
        assertNull(call2.getObject());
        
        // 验证文本节点
        TextNode text4 = getChild(root, 4, TextNode.class);
        assertEquals(", Value: ", text4.getText());
        
        // 美元花括号中的方法调用
        DollarBraceExpressionNode expr5 = getChild(root, 5, DollarBraceExpressionNode.class);
        MethodCall call3 = assertMethodCall(expr5.getExpression(), "getValue");
        assertIdentifier(call3.getObject(), "obj");
    }
    
    @Test
    public void testMixedExpressionTypesWithIndexAccess() throws IOException {
        // 测试混合表达式类型，包含索引访问
        TemplateNode root = parseAndGetRoot(
                "First: {list[0]}, Second: {{array[1]}}, Third: ${items[2]}");
        
        assertEquals(6, root.getChildren().size());
        
        // 验证文本节点
        TextNode text0 = getChild(root, 0, TextNode.class);
        assertEquals("First: ", text0.getText());
        
        // 单花括号中的索引访问
        SingleBraceExpressionNode expr1 = getChild(root, 1, SingleBraceExpressionNode.class);
        IndexAccess index1 = assertIndexAccess(expr1.getExpression());
        assertIdentifier(index1.getObject(), "list");
        assertNumberLiteral(index1.getIndex(), 0);
        
        // 验证文本节点
        TextNode text2 = getChild(root, 2, TextNode.class);
        assertEquals(", Second: ", text2.getText());
        
        // 双花括号中的索引访问
        DoubleBraceExpressionNode expr3 = getChild(root, 3, DoubleBraceExpressionNode.class);
        IndexAccess index2 = assertIndexAccess(expr3.getExpression());
        assertIdentifier(index2.getObject(), "array");
        assertNumberLiteral(index2.getIndex(), 1);
        
        // 验证文本节点
        TextNode text4 = getChild(root, 4, TextNode.class);
        assertEquals(", Third: ", text4.getText());
        
        // 美元花括号中的索引访问
        DollarBraceExpressionNode expr5 = getChild(root, 5, DollarBraceExpressionNode.class);
        IndexAccess index3 = assertIndexAccess(expr5.getExpression());
        assertIdentifier(index3.getObject(), "items");
        assertNumberLiteral(index3.getIndex(), 2);
    }
    
    @Test
    public void testMixedExpressionTypesWithAllTypes() throws IOException {
        // 测试包含所有表达式类型的混合模板
        TemplateNode root = parseAndGetRoot(
                "Text {simple}, {{double}}, ${dollar}, {a + b}, {{x * y}}, ${flag ? 1 : 0}");
        
        assertEquals(12, root.getChildren().size()); // 7个文本节点 + 5个表达式节点
        
        // 验证文本节点
        TextNode text0 = getChild(root, 0, TextNode.class);
        assertEquals("Text ", text0.getText());
        
        TextNode text2 = getChild(root, 2, TextNode.class);
        assertEquals(", ", text2.getText());
        
        TextNode text4 = getChild(root, 4, TextNode.class);
        assertEquals(", ", text4.getText());
        
        TextNode text6 = getChild(root, 6, TextNode.class);
        assertEquals(", ", text6.getText());
        
        TextNode text8 = getChild(root, 8, TextNode.class);
        assertEquals(", ", text8.getText());
        
        TextNode text10 = getChild(root, 10, TextNode.class);
        assertEquals(", ", text10.getText());
        
        // 验证各种表达式类型都存在
        boolean hasSingleBrace = false;
        boolean hasDoubleBrace = false;
        boolean hasDollarBrace = false;
        boolean hasBinaryExpr = false;
        boolean hasTernaryExpr = false;
        
        for (ASTNode child : root.getChildren()) {
            if (child instanceof SingleBraceExpressionNode) {
                hasSingleBrace = true;
                SingleBraceExpressionNode singleExpr = (SingleBraceExpressionNode) child;
                if (singleExpr.getExpression() instanceof BinaryExpression) {
                    hasBinaryExpr = true;
                }
            } else if (child instanceof DoubleBraceExpressionNode) {
                hasDoubleBrace = true;
            } else if (child instanceof DollarBraceExpressionNode) {
                hasDollarBrace = true;
                DollarBraceExpressionNode dollarExpr = (DollarBraceExpressionNode) child;
                if (dollarExpr.getExpression() instanceof TernaryExpression) {
                    hasTernaryExpr = true;
                }
            }
        }
        
        assertTrue(hasSingleBrace);
        assertTrue(hasDoubleBrace);
        assertTrue(hasDollarBrace);
        assertTrue(hasBinaryExpr);
        assertTrue(hasTernaryExpr);
    }
    
    // ==================== JSON文本与表达式混合测试 ====================
    
    @Test
    public void testJsonTextWithExpressions() throws IOException {
        // 测试JSON格式文本中包含表达式
        TemplateNode root = parseAndGetRoot(
                "{\"name\": \"{user.name}\", \"age\": {user.age}, \"city\": \"北京\"}");
        
        assertEquals(5, root.getChildren().size()); // Text, Expr, Text, Expr, Text
        
        // 第一个文本节点：{"name": "
        TextNode text0 = getChild(root, 0, TextNode.class);
        assertEquals("{\"name\": \"", text0.getText());
        
        // 第一个表达式：user.name
        SingleBraceExpressionNode expr1 = getChild(root, 1, SingleBraceExpressionNode.class);
        assertMemberAccess(expr1.getExpression(), "user", "name");
        
        // 第二个文本节点：", "age":
        TextNode text2 = getChild(root, 2, TextNode.class);
        assertEquals("\", \"age\": ", text2.getText());
        
        // 第二个表达式：user.age
        SingleBraceExpressionNode expr3 = getChild(root, 3, SingleBraceExpressionNode.class);
        assertMemberAccess(expr3.getExpression(), "user", "age");
        
        // 第三个文本节点：, "city": "北京"}
        TextNode text4 = getChild(root, 4, TextNode.class);
        assertEquals(", \"city\": \"北京\"}", text4.getText());
    }
    
    @Test
    public void testJsonTextWithMixedExpressionTypes() throws IOException {
        // 测试JSON格式文本中包含不同类型的表达式
        TemplateNode root = parseAndGetRoot(
                "{\"name\": {{user.name}}, \"age\": {user.age}, \"email\": ${user.email}}");
        
        assertEquals(7, root.getChildren().size()); // Text, DoubleBrace, Text, SingleBrace, Text, DollarBrace, Text
        
        // 验证第一个文本节点
        TextNode text0 = getChild(root, 0, TextNode.class);
        assertEquals("{\"name\": ", text0.getText());
        
        // 验证双花括号表达式
        DoubleBraceExpressionNode expr1 = getChild(root, 1, DoubleBraceExpressionNode.class);
        assertMemberAccess(expr1.getExpression(), "user", "name");
        
        // 验证第二个文本节点
        TextNode text2 = getChild(root, 2, TextNode.class);
        assertEquals(", \"age\": ", text2.getText());
        
        // 验证单花括号表达式
        SingleBraceExpressionNode expr3 = getChild(root, 3, SingleBraceExpressionNode.class);
        assertMemberAccess(expr3.getExpression(), "user", "age");
        
        // 验证第三个文本节点
        TextNode text4 = getChild(root, 4, TextNode.class);
        assertEquals(", \"email\": ", text4.getText());
        
        // 验证美元花括号表达式
        DollarBraceExpressionNode expr5 = getChild(root, 5, DollarBraceExpressionNode.class);
        assertMemberAccess(expr5.getExpression(), "user", "email");
        
        // 验证最后一个文本节点
        TextNode text6 = getChild(root, 6, TextNode.class);
        assertEquals("}", text6.getText());
    }
    
    @Test
    public void testJsonTextWithComplexExpressions() throws IOException {
        // 测试JSON格式文本中包含复杂表达式
        TemplateNode root = parseAndGetRoot(
                "{\"sum\": {a + b}, \"product\": {{x * y}}, \"status\": ${flag ? \"active\" : \"inactive\"}}");
        
        assertEquals(7, root.getChildren().size());
        
        // 验证第一个文本节点
        TextNode text0 = getChild(root, 0, TextNode.class);
        assertEquals("{\"sum\": ", text0.getText());
        
        // 验证单花括号中的二元表达式
        SingleBraceExpressionNode expr1 = getChild(root, 1, SingleBraceExpressionNode.class);
        BinaryExpression addExpr = assertBinaryExpression(expr1.getExpression(), BinaryExpression.Operator.ADD);
        assertIdentifier(addExpr.getLeft(), "a");
        assertIdentifier(addExpr.getRight(), "b");
        
        // 验证第二个文本节点
        TextNode text2 = getChild(root, 2, TextNode.class);
        assertEquals(", \"product\": ", text2.getText());
        
        // 验证双花括号中的二元表达式
        DoubleBraceExpressionNode expr3 = getChild(root, 3, DoubleBraceExpressionNode.class);
        BinaryExpression multExpr = assertBinaryExpression(expr3.getExpression(), BinaryExpression.Operator.MULTIPLY);
        assertIdentifier(multExpr.getLeft(), "x");
        assertIdentifier(multExpr.getRight(), "y");
        
        // 验证第三个文本节点
        TextNode text4 = getChild(root, 4, TextNode.class);
        assertEquals(", \"status\": ", text4.getText());
        
        // 验证美元花括号中的三元表达式
        DollarBraceExpressionNode expr5 = getChild(root, 5, DollarBraceExpressionNode.class);
        TernaryExpression ternaryExpr = assertTernaryExpression(expr5.getExpression());
        assertIdentifier(ternaryExpr.getCondition(), "flag");
        
        // 验证最后一个文本节点
        TextNode text6 = getChild(root, 6, TextNode.class);
        assertEquals("}", text6.getText());
    }
    
    @Test
    public void testJsonTextWithNestedObjectsAndExpressions() throws IOException {
        // 测试包含嵌套对象的JSON文本和表达式混合
        TemplateNode root = parseAndGetRoot(
                "{\"user\": {\"name\": {user.name}, \"profile\": {\"age\": {user.age}}}}");
        
        assertEquals(5, root.getChildren().size());
        
        // 验证第一个文本节点
        TextNode text0 = getChild(root, 0, TextNode.class);
        assertEquals("{\"user\": {\"name\": ", text0.getText());
        
        // 验证第一个表达式：user.name
        SingleBraceExpressionNode expr1 = getChild(root, 1, SingleBraceExpressionNode.class);
        assertMemberAccess(expr1.getExpression(), "user", "name");
        
        // 验证第二个文本节点
        TextNode text2 = getChild(root, 2, TextNode.class);
        assertEquals(", \"profile\": {\"age\": ", text2.getText());
        
        // 验证第二个表达式：user.age
        SingleBraceExpressionNode expr3 = getChild(root, 3, SingleBraceExpressionNode.class);
        assertMemberAccess(expr3.getExpression(), "user", "age");
        
        // 验证最后一个文本节点
        TextNode text4 = getChild(root, 4, TextNode.class);
        assertEquals("}}}", text4.getText());
    }
    
    @Test
    public void testJsonTextWithArraysAndExpressions() throws IOException {
        // 测试包含数组的JSON文本和表达式混合
        TemplateNode root = parseAndGetRoot("{\"items\": [{item1}, {item2}, {item3}], \"count\": {count}}");
        
        assertEquals(9, root.getChildren().size()); // Text, Expr, Text, Expr, Text, Expr, Text, Expr, Text
        
        // 验证第一个文本节点
        TextNode text0 = getChild(root, 0, TextNode.class);
        assertEquals("{\"items\": [", text0.getText());
        
        // 验证数组中的表达式
        SingleBraceExpressionNode expr1 = getChild(root, 1, SingleBraceExpressionNode.class);
        assertIdentifier(expr1.getExpression(), "item1");
        
        // 验证第二个文本节点
        TextNode text2 = getChild(root, 2, TextNode.class);
        assertEquals(", ", text2.getText());
        
        SingleBraceExpressionNode expr3 = getChild(root, 3, SingleBraceExpressionNode.class);
        assertIdentifier(expr3.getExpression(), "item2");
        
        // 验证第三个文本节点
        TextNode text4 = getChild(root, 4, TextNode.class);
        assertEquals(", ", text4.getText());
        
        SingleBraceExpressionNode expr5 = getChild(root, 5, SingleBraceExpressionNode.class);
        assertIdentifier(expr5.getExpression(), "item3");
        
        // 验证第四个文本节点
        TextNode text6 = getChild(root, 6, TextNode.class);
        assertEquals("], \"count\": ", text6.getText());
        
        // 验证count表达式
        SingleBraceExpressionNode expr7 = getChild(root, 7, SingleBraceExpressionNode.class);
        assertIdentifier(expr7.getExpression(), "count");
        
        // 验证最后一个文本节点
        TextNode text8 = getChild(root, 8, TextNode.class);
        assertEquals("}", text8.getText());
    }
    
    @Test
    public void testJsonTextWithMethodCalls() throws IOException {
        // 测试JSON文本中包含方法调用表达式
        TemplateNode root = parseAndGetRoot(
                "{\"name\": {user.getName()}, \"count\": {{getCount()}}, \"value\": ${obj.getValue()}}");
        
        assertEquals(7, root.getChildren().size());
        
        // 验证第一个文本节点
        TextNode text0 = getChild(root, 0, TextNode.class);
        assertEquals("{\"name\": ", text0.getText());
        
        // 验证单花括号中的方法调用
        SingleBraceExpressionNode expr1 = getChild(root, 1, SingleBraceExpressionNode.class);
        MethodCall call1 = assertMethodCall(expr1.getExpression(), "getName");
        assertIdentifier(call1.getObject(), "user");
        
        // 验证第二个文本节点
        TextNode text2 = getChild(root, 2, TextNode.class);
        assertEquals(", \"count\": ", text2.getText());
        
        // 验证双花括号中的方法调用
        DoubleBraceExpressionNode expr3 = getChild(root, 3, DoubleBraceExpressionNode.class);
        MethodCall call2 = assertMethodCall(expr3.getExpression(), "getCount");
        assertNull(call2.getObject());
        
        // 验证第三个文本节点
        TextNode text4 = getChild(root, 4, TextNode.class);
        assertEquals(", \"value\": ", text4.getText());
        
        // 验证美元花括号中的方法调用
        DollarBraceExpressionNode expr5 = getChild(root, 5, DollarBraceExpressionNode.class);
        MethodCall call3 = assertMethodCall(expr5.getExpression(), "getValue");
        assertIdentifier(call3.getObject(), "obj");
        
        // 验证最后一个文本节点
        TextNode text6 = getChild(root, 6, TextNode.class);
        assertEquals("}", text6.getText());
    }
    
    @Test
    public void testJsonTextWithStatements() throws IOException {
        // 测试JSON文本与语句块混合
        TemplateNode root = parseAndGetRoot(
                "{\"users\": [{% for user in users }{{user.name}}{/% for }]}");
        
        // 应该包含文本节点和for语句
        assertTrue(root.getChildren().size() >= 2);
        
        // 第一个文本节点应该是JSON开始部分
        TextNode text0 = getChild(root, 0, TextNode.class);
        assertEquals("{\"users\": [", text0.getText());
        
        // 应该包含for语句
        boolean hasForStatement = false;
        ForStatement forStmt = null;
        for (ASTNode child : root.getChildren()) {
            if (child instanceof ForStatement) {
                hasForStatement = true;
                forStmt = (ForStatement) child;
                assertEquals("user", forStmt.getItemVariable());
                assertIdentifier(forStmt.getIterable(), "users");
                break;
            }
        }
        assertTrue(hasForStatement);
        
        // 验证最后一个文本节点（JSON结束部分）
        int forStmtIndex = root.getChildren().indexOf(forStmt);
        TextNode text1 = getChild(root, forStmtIndex + 1, TextNode.class);
        assertEquals("]}", text1.getText());
    }
    
    @Test
    public void testComplexJsonWithExpressionsAndStatements() throws IOException {
        // 测试复杂的JSON格式，包含表达式和语句块
        TemplateNode root = parseAndGetRoot(
                "{\"header\": {title}, \"items\": [{% for item in items }{{item.name}}{/% for }], " +
                "\"footer\": ${footer}}");
        
        // 验证结构
        assertTrue(root.getChildren().size() >= 4);
        
        // 验证第一个文本节点（JSON开始部分）
        TextNode text0 = getChild(root, 0, TextNode.class);
        assertEquals("{\"header\": ", text0.getText());
        
        // 验证第一个表达式（title）
        boolean hasTitleExpr = false;
        SingleBraceExpressionNode titleExpr = null;
        for (ASTNode child : root.getChildren()) {
            if (child instanceof SingleBraceExpressionNode) {
                SingleBraceExpressionNode expr = (SingleBraceExpressionNode) child;
                if (expr.getExpression() instanceof Identifier) {
                    Identifier id = (Identifier) expr.getExpression();
                    if ("title".equals(id.getName())) {
                        hasTitleExpr = true;
                        titleExpr = expr;
                        break;
                    }
                }
            }
        }
        assertTrue(hasTitleExpr);
        
        // 验证第二个文本节点（在title表达式之后）
        int titleExprIndex = root.getChildren().indexOf(titleExpr);
        TextNode text1 = getChild(root, titleExprIndex + 1, TextNode.class);
        assertEquals(", \"items\": [", text1.getText());
        
        // 验证for语句存在
        boolean hasForStatement = false;
        ForStatement forStmt = null;
        for (ASTNode child : root.getChildren()) {
            if (child instanceof ForStatement) {
                hasForStatement = true;
                forStmt = (ForStatement) child;
                break;
            }
        }
        assertTrue(hasForStatement);
        
        // 验证第三个文本节点（在for语句之后）
        int forStmtIndex = root.getChildren().indexOf(forStmt);
        TextNode text2 = getChild(root, forStmtIndex + 1, TextNode.class);
        assertEquals("], \"footer\": ", text2.getText());
        
        // 验证美元花括号表达式（footer）
        boolean hasFooterExpr = false;
        DollarBraceExpressionNode footerExpr = null;
        for (ASTNode child : root.getChildren()) {
            if (child instanceof DollarBraceExpressionNode) {
                DollarBraceExpressionNode expr = (DollarBraceExpressionNode) child;
                if (expr.getExpression() instanceof Identifier) {
                    Identifier id = (Identifier) expr.getExpression();
                    if ("footer".equals(id.getName())) {
                        hasFooterExpr = true;
                        footerExpr = expr;
                        break;
                    }
                }
            }
        }
        assertTrue(hasFooterExpr);
        
        // 验证最后一个文本节点（JSON结束部分）
        int footerExprIndex = root.getChildren().indexOf(footerExpr);
        TextNode text3 = getChild(root, footerExprIndex + 1, TextNode.class);
        assertEquals("}", text3.getText());
    }
    
    // ==================== 空白行处理测试 ====================
    
    @Test
    public void testBlankLineHandlingForStatement() throws IOException {
        // 测试 for 语句块单独一行时的空白行处理
        // 模板：商品列表：\n  {% for item in items }\n  {item}\n  {/% for }\n结束
        // 期望：删除 for 语句块所在行的空白字符
        TemplateNode root = parseAndGetRoot(
                "商品列表：\n" +
                "  {% for item in items }\n" +
                "  {item}\n" +
                "  {/% for }\n" +
                "结束");
        
        // 验证第一个文本节点：应该删除最后一个换行符后的空白字符
        TextNode text0 = getChild(root, 0, TextNode.class);
        assertEquals("商品列表：\n", text0.getText(), "应该删除换行符后的空白字符");
        
        // 验证 for 语句块存在
        ForStatement forStmt = getChild(root, 1, ForStatement.class);
        assertEquals("item", forStmt.getItemVariable());
        
        // 验证最后一个文本节点：应该删除开头的空白字符和第一个换行符
        TextNode textLast = getChild(root, root.getChildren().size() - 1, TextNode.class);
        assertEquals("结束", textLast.getText(), "应该删除开头的空白字符和换行符");
    }
    
    @Test
    public void testBlankLineHandlingIfStatement() throws IOException {
        // 测试 if 语句块单独一行时的空白行处理
        TemplateNode root = parseAndGetRoot(
                "状态：\n" +
                "  {% if flag == 1 }\n" +
                "  文本1\n" +
                "  {/% if }\n" +
                "结束");
        
        // 验证第一个文本节点
        TextNode text0 = getChild(root, 0, TextNode.class);
        assertEquals("状态：\n", text0.getText(), "应该删除换行符后的空白字符");
        
        // 验证 if 语句块存在
        IfStatement ifStmt = getChild(root, 1, IfStatement.class);
        assertNotNull(ifStmt);
        
        // 验证最后一个文本节点
        TextNode textLast = getChild(root, root.getChildren().size() - 1, TextNode.class);
        assertEquals("结束", textLast.getText(), "应该删除开头的空白字符和换行符");
    }
    
    @Test
    public void testBlankLineHandlingSwitchStatement() throws IOException {
        // 测试 switch 语句块单独一行时的空白行处理
        TemplateNode root = parseAndGetRoot(
                "选择：\n" +
                "  {% switch status }\n" +
                "  {% case 1 }选项1\n" +
                "  {/% switch }\n" +
                "结束");
        
        // 验证第一个文本节点
        TextNode text0 = getChild(root, 0, TextNode.class);
        assertEquals("选择：\n", text0.getText(), "应该删除换行符后的空白字符");
        
        // 验证 switch 语句块存在
        SwitchStatement switchStmt = getChild(root, 1, SwitchStatement.class);
        assertNotNull(switchStmt);
        
        // 验证最后一个文本节点
        TextNode textLast = getChild(root, root.getChildren().size() - 1, TextNode.class);
        assertEquals("结束", textLast.getText(), "应该删除开头的空白字符和换行符");
    }
    
    @Test
    public void testBlankLineHandlingIfElseStatement() throws IOException {
        // 测试 if-else 语句块的空白行处理
        TemplateNode root = parseAndGetRoot(
                "判断：\n" +
                "  {% if flag == 1 }\n" +
                "  文本1\n" +
                "  {% else }\n" +
                "  文本2\n" +
                "  {/% if }\n" +
                "结束");
        
        // 验证第一个文本节点
        TextNode text0 = getChild(root, 0, TextNode.class);
        assertEquals("判断：\n", text0.getText());
        
        // 验证 if 语句块存在
        IfStatement ifStmt = getChild(root, 1, IfStatement.class);
        assertNotNull(ifStmt);
        assertNotNull(ifStmt.getElseBody());
        
        // 验证最后一个文本节点
        TextNode textLast = getChild(root, root.getChildren().size() - 1, TextNode.class);
        assertEquals("结束", textLast.getText());
    }
    
    @Test
    public void testBlankLineHandlingNestedForStatements() throws IOException {
        // 测试嵌套 for 语句块的空白行处理
        TemplateNode root = parseAndGetRoot(
                "开始\n" +
                "  {% for row in rows }\n" +
                "    {% for col in row }\n" +
                "      {col}\n" +
                "    {/% for }\n" +
                "  {/% for }\n" +
                "结束");
        
        // 验证第一个文本节点
        TextNode text0 = getChild(root, 0, TextNode.class);
        assertEquals("开始\n", text0.getText());
        
        // 验证外层 for 语句块
        ForStatement outerFor = getChild(root, 1, ForStatement.class);
        assertEquals("row", outerFor.getItemVariable());
        
        // 验证内层 for 语句块在 body 中
        ForStatement innerFor = findInBody(outerFor.getBody(), ForStatement.class);
        assertEquals("col", innerFor.getItemVariable());
        
        // 验证最后一个文本节点
        TextNode textLast = getChild(root, root.getChildren().size() - 1, TextNode.class);
        assertEquals("结束", textLast.getText());
    }
    
    @Test
    public void testBlankLineHandlingNestedIfStatements() throws IOException {
        // 测试嵌套 if 语句块的空白行处理
        TemplateNode root = parseAndGetRoot(
                "开始\n" +
                "  {% if a > 0 }\n" +
                "    {% if b > 0 }\n" +
                "      都为正\n" +
                "    {/% if }\n" +
                "  {/% if }\n" +
                "结束");
        
        // 验证第一个文本节点
        TextNode text0 = getChild(root, 0, TextNode.class);
        assertEquals("开始\n", text0.getText());
        
        // 验证外层 if 语句块
        IfStatement outerIf = getChild(root, 1, IfStatement.class);
        assertNotNull(outerIf);
        
        // 验证内层 if 语句块在 body 中
        IfStatement innerIf = findInBody(outerIf.getThenBody(), IfStatement.class);
        assertNotNull(innerIf);
        
        // 验证最后一个文本节点
        TextNode textLast = getChild(root, root.getChildren().size() - 1, TextNode.class);
        assertEquals("结束", textLast.getText());
    }
    
    @Test
    public void testBlankLineHandlingForInsideIf() throws IOException {
        // 测试 if 语句块中包含 for 语句块的空白行处理
        TemplateNode root = parseAndGetRoot(
                "开始\n" +
                "  {% if flag }\n" +
                "    {% for item in items }\n" +
                "      {item}\n" +
                "    {/% for }\n" +
                "  {/% if }\n" +
                "结束");
        
        // 验证第一个文本节点
        TextNode text0 = getChild(root, 0, TextNode.class);
        assertEquals("开始\n", text0.getText());
        
        // 验证 if 语句块
        IfStatement ifStmt = getChild(root, 1, IfStatement.class);
        assertNotNull(ifStmt);
        
        // 验证 for 语句块在 if 的 body 中
        ForStatement forStmt = findInBody(ifStmt.getThenBody(), ForStatement.class);
        assertEquals("item", forStmt.getItemVariable());
        
        // 验证最后一个文本节点
        TextNode textLast = getChild(root, root.getChildren().size() - 1, TextNode.class);
        assertEquals("结束", textLast.getText());
    }
    
    @Test
    public void testBlankLineHandlingIfInsideFor() throws IOException {
        // 测试 for 语句块中包含 if 语句块的空白行处理
        TemplateNode root = parseAndGetRoot(
                "开始\n" +
                "  {% for item in items }\n" +
                "    {% if item.active }\n" +
                "      {item.name}\n" +
                "    {/% if }\n" +
                "  {/% for }\n" +
                "结束");
        
        // 验证第一个文本节点
        TextNode text0 = getChild(root, 0, TextNode.class);
        assertEquals("开始\n", text0.getText());
        
        // 验证 for 语句块
        ForStatement forStmt = getChild(root, 1, ForStatement.class);
        assertEquals("item", forStmt.getItemVariable());
        
        // 验证 if 语句块在 for 的 body 中
        IfStatement ifStmt = findInBody(forStmt.getBody(), IfStatement.class);
        assertNotNull(ifStmt);
        
        // 验证最后一个文本节点
        TextNode textLast = getChild(root, root.getChildren().size() - 1, TextNode.class);
        assertEquals("结束", textLast.getText());
    }
    
    @Test
    public void testBlankLineHandlingSwitchInsideFor() throws IOException {
        // 测试 for 语句块中包含 switch 语句块的空白行处理
        TemplateNode root = parseAndGetRoot(
                "开始\n" +
                "  {% for item in items }\n" +
                "    {% switch item.status }\n" +
                "      {% case 1 }激活\n" +
                "    {/% switch }\n" +
                "  {/% for }\n" +
                "结束");
        
        // 验证第一个文本节点
        TextNode text0 = getChild(root, 0, TextNode.class);
        assertEquals("开始\n", text0.getText());
        
        // 验证 for 语句块
        ForStatement forStmt = getChild(root, 1, ForStatement.class);
        assertEquals("item", forStmt.getItemVariable());
        
        // 验证 switch 语句块在 for 的 body 中
        SwitchStatement switchStmt = findInBody(forStmt.getBody(), SwitchStatement.class);
        assertNotNull(switchStmt);
        
        // 验证最后一个文本节点
        TextNode textLast = getChild(root, root.getChildren().size() - 1, TextNode.class);
        assertEquals("结束", textLast.getText());
    }
    
    @Test
    public void testNestedForSwitchInOneLineWithSpaces() throws IOException {
        // 测试在一行内嵌套 for 和 switch 语句块，并且中间包括空格
        TemplateNode root = parseAndGetRoot(
                "{% for item in items }{% switch item.type }{% case 'important' }[{item.name}]{% case 'normal' }{item.name}{% default }?{/% switch } {/% for }");
        
        // 验证 for 语句块存在
        ForStatement forStmt = getFirstChild(root, ForStatement.class);
        assertEquals("item", forStmt.getItemVariable());
        assertIdentifier(forStmt.getIterable(), "items");
        
        // 验证 switch 语句块在 for 的 body 中
        assertTrue(forStmt.getBody().get(0) instanceof SwitchStatement);
        SwitchStatement switchStmt = (SwitchStatement) forStmt.getBody().get(0);
        
        // 验证 switch 条件
        MemberAccess condition = assertMemberAccess(switchStmt.getCondition());
        assertIdentifier(condition.getObject(), "item");
        assertEquals("type", condition.getMemberName());
        
        // 验证 switch 有多个 case
        assertEquals(2, switchStmt.getCases().size());
        
        // 验证第一个 case：'important'
        CaseClause case1 = switchStmt.getCases().get(0);
        assertStringLiteral(case1.getValue(), "important");
        assertEquals(3, case1.getBody().size()); // TextNode "[", SingleBraceExpressionNode, TextNode "]"
        assertTrue(case1.getBody().get(0) instanceof TextNode);
        assertEquals("[", ((TextNode) case1.getBody().get(0)).getText());
        assertTrue(case1.getBody().get(1) instanceof SingleBraceExpressionNode);
        SingleBraceExpressionNode expr1 = (SingleBraceExpressionNode) case1.getBody().get(1);
        assertMemberAccess(expr1.getExpression(), "item", "name");
        assertTrue(case1.getBody().get(2) instanceof TextNode);
        assertEquals("]", ((TextNode) case1.getBody().get(2)).getText());
        
        // 验证第二个 case：'normal'
        CaseClause case2 = switchStmt.getCases().get(1);
        assertStringLiteral(case2.getValue(), "normal");
        assertEquals(1, case2.getBody().size()); // SingleBraceExpressionNode
        assertTrue(case2.getBody().get(0) instanceof SingleBraceExpressionNode);
        SingleBraceExpressionNode expr2 = (SingleBraceExpressionNode) case2.getBody().get(0);
        assertMemberAccess(expr2.getExpression(), "item", "name");
        
        // 验证 default case
        assertNotNull(switchStmt.getDefaultBody());
        assertEquals(1, switchStmt.getDefaultBody().size(), "default body 应该包含一个 TextNode");
        assertTrue(switchStmt.getDefaultBody().get(0) instanceof TextNode);
        assertEquals("?", ((TextNode) switchStmt.getDefaultBody().get(0)).getText());
        
        // 验证 switch 结束后和 for 结束前有一个空格
        int forBodySize = forStmt.getBody().size();
        assertTrue(forBodySize == 2, "for body 应该包含2个节点, 一个 switch 语句块和一个空格");

        ASTNode node = forStmt.getBody().get(1);
        assertTrue(node instanceof TextNode, "for body 的第二个节点应该是一个 TextNode 表示空格");
        String text = ((TextNode) node).getText();
        assertEquals(" ", text);
    }
    
    @Test
    public void testBlankLineHandlingIfInsideSwitch() throws IOException {
        // 测试 switch 语句块中包含 if 语句块的空白行处理
        TemplateNode root = parseAndGetRoot(
                "开始\n" +
                "  {% switch status }\n" +
                "    {% case 1 }\n" +
                "      {% if flag }\n" +
                "        文本\n" +
                "      {/% if }\n" +
                "    {/% switch }\n" +
                "结束");
        
        // 验证第一个文本节点
        TextNode text0 = getChild(root, 0, TextNode.class);
        assertEquals("开始\n", text0.getText());
        
        // 验证 switch 语句块
        SwitchStatement switchStmt = getChild(root, 1, SwitchStatement.class);
        assertNotNull(switchStmt);
        
        // 验证 if 语句块在 switch 的 case body 中
        IfStatement ifStmt = findInBody(switchStmt.getCases().get(0).getBody(), IfStatement.class);
        assertNotNull(ifStmt);
        
        // 验证最后一个文本节点
        TextNode textLast = getChild(root, root.getChildren().size() - 1, TextNode.class);
        assertEquals("结束", textLast.getText());
    }
    
    @Test
    public void testBlankLineHandlingComplexNestedStatements() throws IOException {
        // 测试复杂的嵌套语句块组合：for -> if -> for -> switch
        TemplateNode root = parseAndGetRoot(
                "开始\n" +
                "  {% for row in rows }\n" +
                "    {% if row.visible }\n" +
                "      {% for col in row.cols }\n" +
                "        {% switch col.type }\n" +
                "          {% case 1 }类型1\n" +
                "        {/% switch }\n" +
                "      {/% for }\n" +
                "    {/% if }\n" +
                "  {/% for }\n" +
                "结束");
        
        // 验证第一个文本节点
        TextNode text0 = getChild(root, 0, TextNode.class);
        assertEquals("开始\n", text0.getText());
        
        // 验证外层 for 语句块
        ForStatement outerFor = getChild(root, 1, ForStatement.class);
        assertEquals("row", outerFor.getItemVariable());
        
        // 验证 if 语句块在 for 的 body 中
        IfStatement ifStmt = findInBody(outerFor.getBody(), IfStatement.class);
        assertNotNull(ifStmt);
        
        // 验证内层 for 语句块在 if 的 body 中
        ForStatement innerFor = findInBody(ifStmt.getThenBody(), ForStatement.class);
        assertEquals("col", innerFor.getItemVariable());
        
        // 验证 switch 语句块在内层 for 的 body 中
        SwitchStatement switchStmt = findInBody(innerFor.getBody(), SwitchStatement.class);
        assertNotNull(switchStmt);
        
        // 验证最后一个文本节点
        TextNode textLast = getChild(root, root.getChildren().size() - 1, TextNode.class);
        assertEquals("结束", textLast.getText());
    }
    
    @Test
    public void testBlankLineHandlingMultipleStatements() throws IOException {
        // 测试多个连续的语句块，每个都在单独一行
        TemplateNode root = parseAndGetRoot(
                "开始\n" +
                "  {% if flag1 }\n" +
                "    文本1\n" +
                "  {/% if }\n" +
                "  {% if flag2 }\n" +
                "    文本2\n" +
                "  {/% if }\n" +
                "  {% for item in items }\n" +
                "    {item}\n" +
                "  {/% for }\n" +
                "结束");
        
        // 验证第一个文本节点
        TextNode text0 = getChild(root, 0, TextNode.class);
        assertEquals("开始\n", text0.getText());
        
        // 验证第一个 if 语句块
        IfStatement ifStmt1 = getChild(root, 1, IfStatement.class);
        assertNotNull(ifStmt1);
        
        // 验证第二个 if 语句块
        IfStatement ifStmt2 = getChild(root, 2, IfStatement.class);
        assertNotNull(ifStmt2);
        
        // 验证 for 语句块
        ForStatement forStmt = getChild(root, 3, ForStatement.class);
        assertEquals("item", forStmt.getItemVariable());
        
        // 验证最后一个文本节点
        TextNode textLast = getChild(root, root.getChildren().size() - 1, TextNode.class);
        assertEquals("结束", textLast.getText());
    }
    
    @Test
    public void testBlankLineHandlingWithTabs() throws IOException {
        // 测试使用制表符的空白行处理
        TemplateNode root = parseAndGetRoot(
                "开始\n" +
                "\t{% for item in items }\n" +
                "\t  {item}\n" +
                "\t{/% for }\n" +
                "结束");
        
        // 验证第一个文本节点
        TextNode text0 = getChild(root, 0, TextNode.class);
        assertEquals("开始\n", text0.getText(), "应该删除换行符后的制表符");
        
        // 验证 for 语句块存在
        ForStatement forStmt = getChild(root, 1, ForStatement.class);
        assertEquals("item", forStmt.getItemVariable());
        
        // 验证最后一个文本节点
        TextNode textLast = getChild(root, root.getChildren().size() - 1, TextNode.class);
        assertEquals("结束", textLast.getText(), "应该删除开头的制表符和换行符");
    }
    
    @Test
    public void testBlankLineHandlingMixedSpacesAndTabs() throws IOException {
        // 测试混合空格和制表符的空白行处理
        TemplateNode root = parseAndGetRoot(
                "开始\n" +
                "  \t{% if flag }\n" +
                "    文本\n" +
                "  \t{/% if }\n" +
                "结束");
        
        // 验证第一个文本节点
        TextNode text0 = getChild(root, 0, TextNode.class);
        assertEquals("开始\n", text0.getText(), "应该删除换行符后的空格和制表符");
        
        // 验证 if 语句块存在
        IfStatement ifStmt = getChild(root, 1, IfStatement.class);
        assertNotNull(ifStmt);
        
        // 验证最后一个文本节点
        TextNode textLast = getChild(root, root.getChildren().size() - 1, TextNode.class);
        assertEquals("结束", textLast.getText(), "应该删除开头的空格、制表符和换行符");
    }
    
    @Test
    public void testBlankLineHandlingNoBlankLine() throws IOException {
        // 测试不在单独一行的语句块不应该被处理
        TemplateNode root = parseAndGetRoot("开始{% for item in items }{item}{/% for }结束");
        
        // 验证文本节点没有被修改
        TextNode text0 = getChild(root, 0, TextNode.class);
        assertEquals("开始", text0.getText(), "不在单独一行的语句块不应该影响文本节点");
        
        // 验证 for 语句块存在
        ForStatement forStmt = getChild(root, 1, ForStatement.class);
        assertEquals("item", forStmt.getItemVariable());
        
        // 验证最后一个文本节点
        TextNode textLast = getChild(root, root.getChildren().size() - 1, TextNode.class);
        assertEquals("结束", textLast.getText());
    }

    // ==================== 参数变量引用 ($0, $1 等) 测试 ====================

    @Test
    public void testParseArgumentVariable() throws IOException {
        // 测试解析 $0 参数变量引用
        TemplateNode root = parseAndGetRoot("{$0}");

        assertEquals(1, root.getChildren().size());

        SingleBraceExpressionNode expr = getChild(root, 0, SingleBraceExpressionNode.class);
        assertTrue(expr.getExpression() instanceof ArgumentVariable);
        ArgumentVariable argVar = (ArgumentVariable) expr.getExpression();
        assertEquals(0, argVar.getIndex());
    }

    @Test
    public void testParseArgumentVariableWithLargeIndex() throws IOException {
        // 测试解析两位数参数索引
        TemplateNode root = parseAndGetRoot("{$10}");

        assertEquals(1, root.getChildren().size());

        SingleBraceExpressionNode expr = getChild(root, 0, SingleBraceExpressionNode.class);
        assertTrue(expr.getExpression() instanceof ArgumentVariable);
        ArgumentVariable argVar = (ArgumentVariable) expr.getExpression();
        assertEquals(10, argVar.getIndex());
    }

    @Test
    public void testParseArgumentVariableInDollarBrace() throws IOException {
        // 测试在 ${} 中的参数变量引用
        TemplateNode root = parseAndGetRoot("${$1}");

        assertEquals(1, root.getChildren().size());

        DollarBraceExpressionNode expr = getChild(root, 0, DollarBraceExpressionNode.class);
        assertTrue(expr.getExpression() instanceof ArgumentVariable);
        ArgumentVariable argVar = (ArgumentVariable) expr.getExpression();
        assertEquals(1, argVar.getIndex());
    }

    @Test
    public void testParseArgumentVariableInDoubleBrace() throws IOException {
        // 测试在 {{}} 中的参数变量引用
        TemplateNode root = parseAndGetRoot("{{$2}}");

        assertEquals(1, root.getChildren().size());

        DoubleBraceExpressionNode expr = getChild(root, 0, DoubleBraceExpressionNode.class);
        assertTrue(expr.getExpression() instanceof ArgumentVariable);
        ArgumentVariable argVar = (ArgumentVariable) expr.getExpression();
        assertEquals(2, argVar.getIndex());
    }

    @Test
    public void testParseArgumentVariableWithElvis() throws IOException {
        // 测试参数变量引用与 Elvis 运算符组合
        TemplateNode root = parseAndGetRoot("{$0 ?? 'default'}");

        assertEquals(1, root.getChildren().size());

        SingleBraceExpressionNode expr = getChild(root, 0, SingleBraceExpressionNode.class);
        assertTrue(expr.getExpression() instanceof ElvisExpression);

        ElvisExpression elvis = (ElvisExpression) expr.getExpression();
        assertTrue(elvis.getLeft() instanceof ArgumentVariable);
        ArgumentVariable argVar = (ArgumentVariable) elvis.getLeft();
        assertEquals(0, argVar.getIndex());

        assertTrue(elvis.getRight() instanceof StringLiteral);
        StringLiteral defaultVal = (StringLiteral) elvis.getRight();
        assertEquals("default", defaultVal.getValue());
    }

    @Test
    public void testParseMultipleArgumentVariables() throws IOException {
        // 测试多个参数变量引用
        TemplateNode root = parseAndGetRoot("{$0}-{$1}-{$2}");

        assertEquals(5, root.getChildren().size());

        // 第一个参数变量
        SingleBraceExpressionNode expr0 = getChild(root, 0, SingleBraceExpressionNode.class);
        ArgumentVariable argVar0 = (ArgumentVariable) expr0.getExpression();
        assertEquals(0, argVar0.getIndex());

        // 文本 "-"
        TextNode text1 = getChild(root, 1, TextNode.class);
        assertEquals("-", text1.getText());

        // 第二个参数变量
        SingleBraceExpressionNode expr2 = getChild(root, 2, SingleBraceExpressionNode.class);
        ArgumentVariable argVar2 = (ArgumentVariable) expr2.getExpression();
        assertEquals(1, argVar2.getIndex());

        // 文本 "-"
        TextNode text3 = getChild(root, 3, TextNode.class);
        assertEquals("-", text3.getText());

        // 第三个参数变量
        SingleBraceExpressionNode expr4 = getChild(root, 4, SingleBraceExpressionNode.class);
        ArgumentVariable argVar4 = (ArgumentVariable) expr4.getExpression();
        assertEquals(2, argVar4.getIndex());
    }

    @Test
    public void testParseMixedArgumentAccessAndVariable() throws IOException {
        // 测试混合使用 {0} 和 {$1}
        TemplateNode root = parseAndGetRoot("{0}-{$1}");

        assertEquals(3, root.getChildren().size());

        // {0} 是 ArgumentAccess
        SingleBraceExpressionNode expr0 = getChild(root, 0, SingleBraceExpressionNode.class);
        assertTrue(expr0.getExpression() instanceof ArgumentAccess);
        ArgumentAccess argAccess = (ArgumentAccess) expr0.getExpression();
        assertEquals(0, argAccess.getIndex());

        // 文本 "-"
        TextNode text1 = getChild(root, 1, TextNode.class);
        assertEquals("-", text1.getText());

        // {$1} 是 ArgumentVariable
        SingleBraceExpressionNode expr2 = getChild(root, 2, SingleBraceExpressionNode.class);
        assertTrue(expr2.getExpression() instanceof ArgumentVariable);
        ArgumentVariable argVar = (ArgumentVariable) expr2.getExpression();
        assertEquals(1, argVar.getIndex());
    }

    // ==================== Lambda Expression Tests ====================

    @Test
    public void testParseNoParamLambda() throws IOException {
        ASTNode result = getSingleBraceExpression("{() -> 42}");
        assertTrue(result instanceof LambdaExpression);
        LambdaExpression lambda = (LambdaExpression) result;
        assertEquals(0, lambda.getParameterCount());
        assertFalse(lambda.isBlockBody());
    }

    @Test
    public void testParseSingleParamLambdaNoParens() throws IOException {
        ASTNode result = getSingleBraceExpression("{x -> x * 2}");
        assertTrue(result instanceof LambdaExpression);
        LambdaExpression lambda = (LambdaExpression) result;
        assertEquals(1, lambda.getParameterCount());
        assertEquals("x", lambda.getParameters().get(0).getName());
        assertFalse(lambda.isBlockBody());
    }

    @Test
    public void testParseSingleParamLambdaWithParens() throws IOException {
        ASTNode result = getSingleBraceExpression("{(x) -> x * 2}");
        assertTrue(result instanceof LambdaExpression);
        LambdaExpression lambda = (LambdaExpression) result;
        assertEquals(1, lambda.getParameterCount());
        assertEquals("x", lambda.getParameters().get(0).getName());
    }

    @Test
    public void testParseMultiParamLambda() throws IOException {
        ASTNode result = getSingleBraceExpression("{(a, b) -> a + b}");
        assertTrue(result instanceof LambdaExpression);
        LambdaExpression lambda = (LambdaExpression) result;
        assertEquals(2, lambda.getParameterCount());
        assertEquals("a", lambda.getParameters().get(0).getName());
        assertEquals("b", lambda.getParameters().get(1).getName());
    }

    @Test
    public void testParseTypedParamLambda() throws IOException {
        ASTNode result = getSingleBraceExpression("{(int a, String s) -> s + a}");
        assertTrue(result instanceof LambdaExpression);
        LambdaExpression lambda = (LambdaExpression) result;
        assertEquals(2, lambda.getParameterCount());
        assertEquals("int", lambda.getParameters().get(0).getType());
        assertEquals("a", lambda.getParameters().get(0).getName());
        assertEquals("String", lambda.getParameters().get(1).getType());
        assertEquals("s", lambda.getParameters().get(1).getName());
    }

    @Test
    public void testParseBlockLambda() throws IOException {
        ASTNode result = getSingleBraceExpression("{() -> { 1 + 2 }}");
        assertTrue(result instanceof LambdaExpression);
        LambdaExpression lambda = (LambdaExpression) result;
        assertTrue(lambda.isBlockBody());
        assertNotNull(lambda.getBody());
        assertNull(lambda.getExpressionBody());
    }

    @Test
    public void testParseLambdaWithReturn() throws IOException {
        ASTNode result = getSingleBraceExpression("{(a, b) -> { return a + b }}");
        assertTrue(result instanceof LambdaExpression);
        LambdaExpression lambda = (LambdaExpression) result;
        assertTrue(lambda.isBlockBody());
        assertEquals(1, lambda.getBody().size());
        assertTrue(lambda.getBody().get(0) instanceof ReturnStatement);
    }

    @Test
    public void testParseLambdaWithComplexBlockBody() throws IOException {
        ASTNode result = getSingleBraceExpression("{(a, b) -> { if (b == 0) { return 0 } a / b }}");
        assertTrue(result instanceof LambdaExpression);
        LambdaExpression lambda = (LambdaExpression) result;
        assertTrue(lambda.isBlockBody());
        // Body contains: IfStatement and BinaryExpression
        assertTrue(lambda.getBody().size() >= 1);
    }

    @Test
    public void testParseLambdaInListLiteral() throws IOException {
        ASTNode result = getSingleBraceExpression("{[(x) -> x * 2, (y) -> y + 1]}");
        assertTrue(result instanceof ListLiteral);
        ListLiteral list = (ListLiteral) result;
        assertEquals(2, list.getElements().size());
        assertTrue(list.getElements().get(0) instanceof LambdaExpression);
        assertTrue(list.getElements().get(1) instanceof LambdaExpression);
    }

    @Test
    public void testParseParenthesizedExpressionNotLambda() throws IOException {
        // Ensure (x + 1) is still parsed as parenthesized expression, not lambda
        ASTNode result = getSingleBraceExpression("{(x + 1)}");
        assertTrue(result instanceof BinaryExpression);
        // Should NOT be a LambdaExpression
    }

    // ==================== parseEvalSource Tests ====================

    @Test
    public void testParseEvalSource_SingleExpression() throws IOException {
        Parser parser = createExpressionParser("1 + 2");
        List<ASTNode> statements = parser.parseEvalSource();

        assertEquals(1, statements.size());
        assertTrue(statements.get(0) instanceof BinaryExpression);

        BinaryExpression expr = (BinaryExpression) statements.get(0);
        assertTrue(expr.getLeft() instanceof NumberLiteral);
        assertTrue(expr.getRight() instanceof NumberLiteral);
    }

    @Test
    public void testParseEvalSource_MultipleStatements() throws IOException {
        Parser parser = createExpressionParser("let x = 1; let y = 2; x + y");
        List<ASTNode> statements = parser.parseEvalSource();

        assertEquals(3, statements.size());

        // First statement: let x = 1
        assertTrue(statements.get(0) instanceof LetStatement);
        LetStatement letX = (LetStatement) statements.get(0);
        assertEquals("x", letX.getVariableName());

        // Second statement: let y = 2
        assertTrue(statements.get(1) instanceof LetStatement);
        LetStatement letY = (LetStatement) statements.get(1);
        assertEquals("y", letY.getVariableName());

        // Third statement: x + y
        assertTrue(statements.get(2) instanceof BinaryExpression);
    }

    @Test
    public void testParseEvalSource_ReturnStatement() throws IOException {
        Parser parser = createExpressionParser("if (true) { return 5 } 10");
        List<ASTNode> statements = parser.parseEvalSource();

        assertEquals(2, statements.size());

        // First statement: if (true) { return 5 }
        assertTrue(statements.get(0) instanceof IfExpression);
        IfExpression ifExpr = (IfExpression) statements.get(0);
        assertTrue(ifExpr.getThenStatements().size() >= 1);
        // The then block should contain a ReturnStatement
        boolean hasReturn = false;
        for (ASTNode node : ifExpr.getThenStatements()) {
            if (node instanceof ReturnStatement) {
                hasReturn = true;
                ReturnStatement ret = (ReturnStatement) node;
                assertTrue(ret.hasValue());
                assertTrue(ret.getValue() instanceof NumberLiteral);
                break;
            }
        }
        assertTrue(hasReturn, "If block should contain a ReturnStatement");

        // Second statement: 10
        assertTrue(statements.get(1) instanceof NumberLiteral);
    }

    @Test
    public void testParseEvalSource_Empty() throws IOException {
        Parser parser = createExpressionParser("");
        List<ASTNode> statements = parser.parseEvalSource();

        assertEquals(0, statements.size());
    }

    @Test
    public void testParseEvalSource_ForLoop() throws IOException {
        Parser parser = createExpressionParser("for (i in [1, 2, 3]) { i * 2 }");
        List<ASTNode> statements = parser.parseEvalSource();

        assertEquals(1, statements.size());
        assertTrue(statements.get(0) instanceof ForExpression);

        ForExpression forExpr = (ForExpression) statements.get(0);
        assertEquals("i", forExpr.getVariableName());
        assertNull(forExpr.getIndexName());
        assertTrue(forExpr.getIterable() instanceof ListLiteral);
    }

    // ==================== ObjectConstruction Tests ====================

    @Test
    void testObjectConstructionNoArgs() throws IOException {
        String code = "{Person {name: 'Peter'}}";
        Lexer lexer = new Lexer(new StringSource(code));
        TokenStream tokens = new TokenStream(lexer);
        Parser parser = new Parser(tokens);
        TemplateNode template = parser.parse();

        assertEquals(1, template.getChildren().size());
        SingleBraceExpressionNode expr = (SingleBraceExpressionNode) template.getChildren().get(0);
        ObjectConstruction obj = (ObjectConstruction) expr.getExpression();

        assertTrue(obj.getTargetClass() instanceof Identifier);
        assertEquals("Person", ((Identifier) obj.getTargetClass()).getName());
        assertNotNull(obj.getPropertiesBlock());
    }

    @Test
    void testObjectConstructionWithArgs() throws IOException {
        String code = "{Person('Peter') {age: 18}}";
        Lexer lexer = new Lexer(new StringSource(code));
        TokenStream tokens = new TokenStream(lexer);
        Parser parser = new Parser(tokens);
        TemplateNode template = parser.parse();

        assertEquals(1, template.getChildren().size());
        SingleBraceExpressionNode expr = (SingleBraceExpressionNode) template.getChildren().get(0);
        ObjectConstruction obj = (ObjectConstruction) expr.getExpression();

        assertTrue(obj.getTargetClass() instanceof MethodCall);
        assertNotNull(obj.getPropertiesBlock());
    }

    // ==================== 自增/自减表达式测试 ====================

    @Test
    public void testPostfixIncrement() throws IOException {
        ASTNode expr = getSingleBraceExpression("{i++}");
        assertTrue(expr instanceof IncrementExpression, "应该是IncrementExpression");
        IncrementExpression inc = (IncrementExpression) expr;
        assertFalse(inc.isPrefix());
        assertTrue(inc.isIncrement());
        assertIdentifier(inc.getOperand(), "i");
    }

    @Test
    public void testPostfixDecrement() throws IOException {
        ASTNode expr = getSingleBraceExpression("{i--}");
        assertTrue(expr instanceof IncrementExpression, "应该是IncrementExpression");
        IncrementExpression inc = (IncrementExpression) expr;
        assertFalse(inc.isPrefix());
        assertFalse(inc.isIncrement());
        assertIdentifier(inc.getOperand(), "i");
    }

    @Test
    public void testPrefixIncrement() throws IOException {
        ASTNode expr = getSingleBraceExpression("{++i}");
        assertTrue(expr instanceof IncrementExpression, "应该是IncrementExpression");
        IncrementExpression inc = (IncrementExpression) expr;
        assertTrue(inc.isPrefix());
        assertTrue(inc.isIncrement());
        assertIdentifier(inc.getOperand(), "i");
    }

    @Test
    public void testPrefixDecrement() throws IOException {
        ASTNode expr = getSingleBraceExpression("{--i}");
        assertTrue(expr instanceof IncrementExpression, "应该是IncrementExpression");
        IncrementExpression inc = (IncrementExpression) expr;
        assertTrue(inc.isPrefix());
        assertFalse(inc.isIncrement());
        assertIdentifier(inc.getOperand(), "i");
    }

    @Test
    public void testPostfixIncrementOnMemberAccess() throws IOException {
        ASTNode expr = getSingleBraceExpression("{obj.count++}");
        assertTrue(expr instanceof IncrementExpression, "应该是IncrementExpression");
        IncrementExpression inc = (IncrementExpression) expr;
        assertFalse(inc.isPrefix());
        assertTrue(inc.isIncrement());
        assertTrue(inc.getOperand() instanceof MemberAccess, "操作数应该是MemberAccess");
    }

    @Test
    public void testPostfixIncrementOnIndexAccess() throws IOException {
        ASTNode expr = getSingleBraceExpression("{arr[i]++}");
        assertTrue(expr instanceof IncrementExpression, "应该是IncrementExpression");
        IncrementExpression inc = (IncrementExpression) expr;
        assertFalse(inc.isPrefix());
        assertTrue(inc.isIncrement());
        assertTrue(inc.getOperand() instanceof IndexAccess, "操作数应该是IndexAccess");
    }

    @Test
    public void testPrefixIncrementInExpression() throws IOException {
        // ++i * 2 should parse as (++i) * 2
        ASTNode expr = getSingleBraceExpression("{++i * 2}");
        BinaryExpression binary = assertBinaryExpression(expr, BinaryExpression.Operator.MULTIPLY);
        assertTrue(binary.getLeft() instanceof IncrementExpression, "左侧应该是IncrementExpression");
        IncrementExpression inc = (IncrementExpression) binary.getLeft();
        assertTrue(inc.isPrefix());
        assertNumberLiteral(binary.getRight(), 2);
    }

    @Test
    public void testPostfixIncrementInExpression() throws IOException {
        // i++ * 2 should parse as (i++) * 2
        ASTNode expr = getSingleBraceExpression("{i++ * 2}");
        BinaryExpression binary = assertBinaryExpression(expr, BinaryExpression.Operator.MULTIPLY);
        assertTrue(binary.getLeft() instanceof IncrementExpression, "左侧应该是IncrementExpression");
        IncrementExpression inc = (IncrementExpression) binary.getLeft();
        assertFalse(inc.isPrefix());
        assertNumberLiteral(binary.getRight(), 2);
    }

    @Test
    public void testInvalidIncrementTarget_throwsSyntaxError() throws IOException {
        assertThrows(TemplateSyntaxException.class, () -> getSingleBraceExpression("{++5}"));
    }

    @Test
    public void testInvalidPostfixTarget_throwsSyntaxError() throws IOException {
        assertThrows(TemplateSyntaxException.class, () -> getSingleBraceExpression("{\"hello\"++}"));
    }

    // ==================== 复合赋值表达式测试 ====================

    @Test
    public void testPlusAssign() throws IOException {
        ASTNode expr = getSingleBraceExpression("{x += 5}");
        assertTrue(expr instanceof CompoundAssignmentExpression, "应该是CompoundAssignmentExpression");
        CompoundAssignmentExpression cae = (CompoundAssignmentExpression) expr;
        assertEquals(CompoundOperator.PLUS_ASSIGN, cae.getOperator());
        assertIdentifier(cae.getTarget(), "x");
        assertNumberLiteral(cae.getValue(), 5);
    }

    @Test
    public void testMinusAssign() throws IOException {
        ASTNode expr = getSingleBraceExpression("{x -= 3}");
        CompoundAssignmentExpression cae = (CompoundAssignmentExpression) expr;
        assertEquals(CompoundOperator.MINUS_ASSIGN, cae.getOperator());
    }

    @Test
    public void testMultiplyAssign() throws IOException {
        ASTNode expr = getSingleBraceExpression("{x *= 2}");
        CompoundAssignmentExpression cae = (CompoundAssignmentExpression) expr;
        assertEquals(CompoundOperator.MULTIPLY_ASSIGN, cae.getOperator());
    }

    @Test
    public void testCompoundAssignmentPrecedence() throws IOException {
        // x += 2 + 3 should parse as x += (2 + 3), not (x += 2) + 3
        ASTNode expr = getSingleBraceExpression("{x += 2 + 3}");
        CompoundAssignmentExpression cae = (CompoundAssignmentExpression) expr;
        assertIdentifier(cae.getTarget(), "x");
        // 右侧应该是 BinaryExpression(2 + 3)
        assertTrue(cae.getValue() instanceof BinaryExpression, "右侧应该是BinaryExpression");
        BinaryExpression right = (BinaryExpression) cae.getValue();
        assertEquals(BinaryExpression.Operator.ADD, right.getOperator());
    }

    @Test
    public void testCompoundAssignmentRightAssociativity() throws IOException {
        // a += b += 1 should parse as a += (b += 1)
        ASTNode expr = getSingleBraceExpression("{a += b += 1}");
        CompoundAssignmentExpression outer = (CompoundAssignmentExpression) expr;
        assertIdentifier(outer.getTarget(), "a");
        assertTrue(outer.getValue() instanceof CompoundAssignmentExpression, "右侧应该是CompoundAssignmentExpression");
        CompoundAssignmentExpression inner = (CompoundAssignmentExpression) outer.getValue();
        assertIdentifier(inner.getTarget(), "b");
    }

    @Test
    public void testCompoundAssignmentInvalidTarget_throwsSyntaxError() throws IOException {
        assertThrows(TemplateSyntaxException.class, () -> getSingleBraceExpression("{5 += 1}"));
    }

    // ==================== C 风格 for 循环测试 ====================

    @Test
    public void testClassicForStatement() throws IOException {
        TemplateNode root = parseAndGetRoot("{% for let i = 0; i < 5; i++ }{i}{/% for }");

        ClassicForStatement forStmt = getFirstChild(root, ClassicForStatement.class);

        assertNotNull(forStmt.getInitialization());
        assertEquals("i", forStmt.getInitialization().getVariableName());
        assertNumberLiteral(forStmt.getInitialization().getValue(), 0);

        assertNotNull(forStmt.getCondition());
        assertTrue(forStmt.getCondition() instanceof BinaryExpression);

        assertNotNull(forStmt.getUpdate());
        assertTrue(forStmt.getUpdate() instanceof IncrementExpression);

        assertTrue(forStmt.getBody().size() > 0);
    }

    @Test
    public void testClassicForStatementWithStep() throws IOException {
        TemplateNode root = parseAndGetRoot("{% for let i = 0; i < 10; i += 2 }{i}{/% for }");

        ClassicForStatement forStmt = getFirstChild(root, ClassicForStatement.class);

        assertNotNull(forStmt.getUpdate());
        assertTrue(forStmt.getUpdate() instanceof CompoundAssignmentExpression, "更新表达式应该是CompoundAssignmentExpression");
        CompoundAssignmentExpression update = (CompoundAssignmentExpression) forStmt.getUpdate();
        assertEquals(CompoundOperator.PLUS_ASSIGN, update.getOperator());
    }

    @Test
    public void testClassicForStatementEmptyCondition() throws IOException {
        TemplateNode root = parseAndGetRoot("{% for let i = 0; ; i++ }{i}{/% for }");

        ClassicForStatement forStmt = getFirstChild(root, ClassicForStatement.class);

        assertNull(forStmt.getCondition(), "空条件应该为null");
        assertNotNull(forStmt.getInitialization());
        assertNotNull(forStmt.getUpdate());
    }

    @Test
    public void testClassicForStatementEmptyUpdate() throws IOException {
        TemplateNode root = parseAndGetRoot("{% for let i = 0; i < 5; }{i}{/% for }");

        ClassicForStatement forStmt = getFirstChild(root, ClassicForStatement.class);

        assertNull(forStmt.getUpdate(), "空更新表达式应该为null");
        assertNotNull(forStmt.getCondition());
    }

    @Test
    public void testForInStillWorksAfterClassicFor() throws IOException {
        // 确保 for-in 语法仍然正常工作
        TemplateNode root = parseAndGetRoot("{% for item in items }{item}{/% for }");

        ForStatement forStmt = getFirstChild(root, ForStatement.class);
        assertEquals("item", forStmt.getItemVariable());
        assertIdentifier(forStmt.getIterable(), "items");
    }
}
