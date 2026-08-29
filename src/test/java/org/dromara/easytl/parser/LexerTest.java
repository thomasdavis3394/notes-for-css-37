package org.dromara.easytl.parser;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Lexer 类的测试
 *
 * @author EasyTL Team
 */
public class LexerTest {

    /**
     * 断言下一个 token 的类型
     *
     * @param lexer 词法分析器
     * @param expectedType 期望的 token 类型
     * @return 获取到的 token
     */
    private Token assertNextToken(Lexer lexer, int expectedType) throws IOException {
        Token token = lexer.nextToken();
        assertEquals(expectedType, token.getType());
        return token;
    }

    /**
     * 断言下一个 token 的类型和值
     *
     * @param lexer 词法分析器
     * @param expectedType 期望的 token 类型
     * @param expectedValue 期望的 token 值
     * @return 获取到的 token
     */
    private Token assertNextToken(Lexer lexer, int expectedType, String expectedValue) throws IOException {
        Token token = lexer.nextToken();
        assertEquals(expectedType, token.getType());
        assertEquals(expectedValue, token.getValue());
        return token;
    }

    @Test
    public void testPlainText() throws IOException {
        StringSource source = new StringSource("Hello World");
        Lexer lexer = new Lexer(source);

        assertNextToken(lexer, TokenType.TEXT, "Hello World");
        assertNextToken(lexer, TokenType.EOF);
    }

    @Test
    public void testSingleBraceExpression() throws IOException {
        StringSource source = new StringSource("Hello {name}!");
        Lexer lexer = new Lexer(source);

        assertNextToken(lexer, TokenType.TEXT, "Hello ");
        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_BEGIN);
        assertNextToken(lexer, TokenType.IDENTIFIER, "name");
        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_END);
        assertNextToken(lexer, TokenType.TEXT, "!");
    }

    @Test
    public void testDoubleBraceExpression() throws IOException {
        StringSource source = new StringSource("Hello {{name}}!");
        Lexer lexer = new Lexer(source);

        assertNextToken(lexer, TokenType.TEXT, "Hello ");
        assertNextToken(lexer, TokenType.DOUBLE_BRACE_EXPR_BEGIN, "{{");
        assertNextToken(lexer, TokenType.IDENTIFIER, "name");
        assertNextToken(lexer, TokenType.DOUBLE_BRACE_EXPR_END, "}}");
        assertNextToken(lexer, TokenType.TEXT, "!");
    }

    @Test
    public void testDollarBraceExpression() throws IOException {
        StringSource source = new StringSource("Hello ${name}!");
        Lexer lexer = new Lexer(source);

        assertNextToken(lexer, TokenType.TEXT, "Hello ");
        assertNextToken(lexer, TokenType.DOLLAR_BRACE_EXPR_BEGIN, "${");
        assertNextToken(lexer, TokenType.IDENTIFIER, "name");
        assertNextToken(lexer, TokenType.DOLLAR_BRACE_EXPR_END, "}");
        assertNextToken(lexer, TokenType.TEXT, "!");
    }

    @Test
    public void testDollarBraceExpressionWithComplexExpression() throws IOException {
        StringSource source = new StringSource("Price: ${price * quantity}");
        Lexer lexer = new Lexer(source);

        assertNextToken(lexer, TokenType.TEXT, "Price: ");
        assertNextToken(lexer, TokenType.DOLLAR_BRACE_EXPR_BEGIN, "${");
        assertNextToken(lexer, TokenType.IDENTIFIER, "price");
        assertNextToken(lexer, TokenType.MULTIPLY);
        assertNextToken(lexer, TokenType.IDENTIFIER, "quantity");
        assertNextToken(lexer, TokenType.DOLLAR_BRACE_EXPR_END, "}");
        assertNextToken(lexer, TokenType.EOF);
    }

    @Test
    public void testDollarBraceExpressionWithNestedBraces() throws IOException {
        StringSource source = new StringSource("List: ${list[0]}");
        Lexer lexer = new Lexer(source);

        assertNextToken(lexer, TokenType.TEXT, "List: ");
        assertNextToken(lexer, TokenType.DOLLAR_BRACE_EXPR_BEGIN, "${");
        assertNextToken(lexer, TokenType.IDENTIFIER, "list");
        assertNextToken(lexer, TokenType.LEFT_BRACKET);
        assertNextToken(lexer, TokenType.NUMBER, "0");
        assertNextToken(lexer, TokenType.RIGHT_BRACKET);
        assertNextToken(lexer, TokenType.DOLLAR_BRACE_EXPR_END, "}");
    }

    @Test
    public void testDollarBraceExpressionWithHashLiteral() throws IOException {
        StringSource source = new StringSource("Map: ${map.key}");
        Lexer lexer = new Lexer(source);

        assertNextToken(lexer, TokenType.TEXT, "Map: ");
        assertNextToken(lexer, TokenType.DOLLAR_BRACE_EXPR_BEGIN, "${");
        assertNextToken(lexer, TokenType.IDENTIFIER, "map");
        assertNextToken(lexer, TokenType.DOT);
        assertNextToken(lexer, TokenType.IDENTIFIER, "key");
        assertNextToken(lexer, TokenType.DOLLAR_BRACE_EXPR_END, "}");
    }

    @Test
    public void testDollarBraceExpressionWithStringLiteral() throws IOException {
        StringSource source = new StringSource("Message: ${'Hello World'}");
        Lexer lexer = new Lexer(source);

        assertNextToken(lexer, TokenType.TEXT, "Message: ");
        assertNextToken(lexer, TokenType.DOLLAR_BRACE_EXPR_BEGIN, "${");
        assertNextToken(lexer, TokenType.STRING, "Hello World");
        assertNextToken(lexer, TokenType.DOLLAR_BRACE_EXPR_END, "}");
    }

    @Test
    public void testDollarBraceExpressionMixedWithOtherExpressions() throws IOException {
        StringSource source = new StringSource("Hello {name} and ${user.name}!");
        Lexer lexer = new Lexer(source);

        assertNextToken(lexer, TokenType.TEXT, "Hello ");

        // 单花括号表达式
        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_BEGIN);
        assertNextToken(lexer, TokenType.IDENTIFIER, "name");
        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_END);

        assertNextToken(lexer, TokenType.TEXT, " and ");

        // 美元符号表达式
        assertNextToken(lexer, TokenType.DOLLAR_BRACE_EXPR_BEGIN, "${");
        assertNextToken(lexer, TokenType.IDENTIFIER, "user");
        assertNextToken(lexer, TokenType.DOT);
        assertNextToken(lexer, TokenType.IDENTIFIER, "name");
        assertNextToken(lexer, TokenType.DOLLAR_BRACE_EXPR_END);

        assertNextToken(lexer, TokenType.TEXT, "!");
    }

    @Test
    public void testStringLiteral() throws IOException {
        StringSource source = new StringSource("{\"hello\"}");
        Lexer lexer = new Lexer(source);

        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_BEGIN);
        assertNextToken(lexer, TokenType.STRING, "hello");
        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_END);
    }

    @Test
    public void testStringLiteralWithEscape() throws IOException {
        StringSource source = new StringSource("{\"hello\\nworld\\t!\"}");
        Lexer lexer = new Lexer(source);

        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_BEGIN);
        assertNextToken(lexer, TokenType.STRING, "hello\nworld\t!");
        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_END);
    }

    @Test
    public void testSingleQuoteString() throws IOException {
        StringSource source = new StringSource("{'hello'}");
        Lexer lexer = new Lexer(source);

        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_BEGIN);
        assertNextToken(lexer, TokenType.STRING, "hello");
        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_END);
    }

    @Test
    public void testTemplateString() throws IOException {
        StringSource source = new StringSource("{`hello {name}`}");
        Lexer lexer = new Lexer(source);

        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_BEGIN);
        assertNextToken(lexer, TokenType.TEMPLATE_STRING, "hello {name}");
        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_END);
    }

    @Test
    public void testIntegerLiteral() throws IOException {
        StringSource source = new StringSource("{123}");
        Lexer lexer = new Lexer(source);

        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_BEGIN);
        assertNextToken(lexer, TokenType.NUMBER, "123");
        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_END);
    }

    @Test
    public void testNegativeNumber() throws IOException {
        StringSource source = new StringSource("{-123}");
        Lexer lexer = new Lexer(source);

        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_BEGIN);
        assertNextToken(lexer, TokenType.NUMBER, "-123");
        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_END);
    }

    @Test
    public void testLongLiteral() throws IOException {
        StringSource source = new StringSource("{123456789L}");
        Lexer lexer = new Lexer(source);

        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_BEGIN);
        assertNextToken(lexer, TokenType.LONG, "123456789");
        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_END);
    }

    @Test
    public void testFloatLiteral() throws IOException {
        StringSource source = new StringSource("{3.14}");
        Lexer lexer = new Lexer(source);

        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_BEGIN);
        assertNextToken(lexer, TokenType.NUMBER, "3.14");
        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_END);
    }

    @Test
    public void testScientificNotation() throws IOException {
        StringSource source = new StringSource("{1.23e10}");
        Lexer lexer = new Lexer(source);

        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_BEGIN);
        assertNextToken(lexer, TokenType.NUMBER, "1.23e10");
        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_END);
    }

    @Test
    public void testBigDecimal() throws IOException {
        StringSource source = new StringSource("{123.12345678901234567890}");
        Lexer lexer = new Lexer(source);

        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_BEGIN);
        assertNextToken(lexer, TokenType.BIG_DECIMAL);
        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_END);
    }

    @Test
    public void testKeywords() throws IOException {
        StringSource source = new StringSource("{if true else false null}");
        Lexer lexer = new Lexer(source);

        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_BEGIN);
        assertNextToken(lexer, TokenType.IF);
        assertNextToken(lexer, TokenType.TRUE);
        assertNextToken(lexer, TokenType.ELSE);
        assertNextToken(lexer, TokenType.FALSE);
        assertNextToken(lexer, TokenType.NULL);
        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_END);
    }

    @Test
    public void testArithmeticOperators() throws IOException {
        StringSource source = new StringSource("{a + b - c * d / e % f}");
        Lexer lexer = new Lexer(source);

        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_BEGIN);
        assertNextToken(lexer, TokenType.IDENTIFIER, "a");
        assertNextToken(lexer, TokenType.PLUS);
        assertNextToken(lexer, TokenType.IDENTIFIER, "b");
        assertNextToken(lexer, TokenType.MINUS);
        assertNextToken(lexer, TokenType.IDENTIFIER, "c");
        assertNextToken(lexer, TokenType.MULTIPLY);
        assertNextToken(lexer, TokenType.IDENTIFIER, "d");
        assertNextToken(lexer, TokenType.DIVIDE);
        assertNextToken(lexer, TokenType.IDENTIFIER, "e");
        assertNextToken(lexer, TokenType.MODULO);
        assertNextToken(lexer, TokenType.IDENTIFIER, "f");
        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_END);
    }

    @Test
    public void testComparisonOperators() throws IOException {
        StringSource source = new StringSource("{a > b < c >= d <= e == f != g}");
        Lexer lexer = new Lexer(source);

        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_BEGIN);
        assertNextToken(lexer, TokenType.IDENTIFIER, "a");
        assertNextToken(lexer, TokenType.GT);
        assertNextToken(lexer, TokenType.IDENTIFIER, "b");
        assertNextToken(lexer, TokenType.LT);
        assertNextToken(lexer, TokenType.IDENTIFIER, "c");
        assertNextToken(lexer, TokenType.GTE);
        assertNextToken(lexer, TokenType.IDENTIFIER, "d");
        assertNextToken(lexer, TokenType.LTE);
        assertNextToken(lexer, TokenType.IDENTIFIER, "e");
        assertNextToken(lexer, TokenType.EQ);
        assertNextToken(lexer, TokenType.IDENTIFIER, "f");
        assertNextToken(lexer, TokenType.NEQ);
        assertNextToken(lexer, TokenType.IDENTIFIER, "g");
        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_END);
    }

    @Test
    public void testLogicalOperators() throws IOException {
        StringSource source = new StringSource("{a && b || !c}");
        Lexer lexer = new Lexer(source);

        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_BEGIN);
        assertNextToken(lexer, TokenType.IDENTIFIER, "a");
        assertNextToken(lexer, TokenType.AND);
        assertNextToken(lexer, TokenType.IDENTIFIER, "b");
        assertNextToken(lexer, TokenType.OR);
        assertNextToken(lexer, TokenType.NOT);
        assertNextToken(lexer, TokenType.IDENTIFIER, "c");
        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_END);
    }

    @Test
    public void testSafeNavOperator() throws IOException {
        StringSource source = new StringSource("{user?.name}");
        Lexer lexer = new Lexer(source);

        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_BEGIN);
        assertNextToken(lexer, TokenType.IDENTIFIER, "user");
        assertNextToken(lexer, TokenType.SAFE_NAV, "?.");
        assertNextToken(lexer, TokenType.IDENTIFIER, "name");
        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_END);
    }

    @Test
    public void testElvisOperator() throws IOException {
        StringSource source = new StringSource("{name ?? 'default'}");
        Lexer lexer = new Lexer(source);

        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_BEGIN);
        assertNextToken(lexer, TokenType.IDENTIFIER, "name");
        assertNextToken(lexer, TokenType.ELVIS, "??");
        assertNextToken(lexer, TokenType.STRING, "default");
        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_END);
    }

    @Test
    public void testRegexMatchOperators() throws IOException {
        StringSource source = new StringSource("{str =~ /pattern/ && str2 !=~ /test/}");
        Lexer lexer = new Lexer(source);

        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_BEGIN);
        assertNextToken(lexer, TokenType.IDENTIFIER, "str");
        assertNextToken(lexer, TokenType.REGEX_MATCH);
        assertNextToken(lexer, TokenType.REGEX, "pattern");
        assertNextToken(lexer, TokenType.AND);
        assertNextToken(lexer, TokenType.IDENTIFIER, "str2");
        assertNextToken(lexer, TokenType.REGEX_NOT_MATCH);
        assertNextToken(lexer, TokenType.REGEX, "test");
        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_END);
    }

    @Test
    public void testRangeOperators() throws IOException {
        StringSource source = new StringSource("{1..10}");
        Lexer lexer = new Lexer(source);

        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_BEGIN);
        assertNextToken(lexer, TokenType.NUMBER, "1");
        assertNextToken(lexer, TokenType.RANGE_CLOSED, "..");
        assertNextToken(lexer, TokenType.NUMBER, "10");
        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_END);
    }

    @Test
    public void testRangeOpenOperator() throws IOException {
        StringSource source = new StringSource("{1>..<10}");
        Lexer lexer = new Lexer(source);

        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_BEGIN);
        assertNextToken(lexer, TokenType.NUMBER, "1");
        assertNextToken(lexer, TokenType.RANGE_OPEN, ">..<");
        assertNextToken(lexer, TokenType.NUMBER, "10");
        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_END);
    }

    @Test
    public void testRangeLeftClosedOperator() throws IOException {
        StringSource source = new StringSource("{1..<10}");
        Lexer lexer = new Lexer(source);

        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_BEGIN);
        assertNextToken(lexer, TokenType.NUMBER, "1");
        assertNextToken(lexer, TokenType.RANGE_LEFT_CLOSED_RIGHT_OPEN, "..<");
        assertNextToken(lexer, TokenType.NUMBER, "10");
        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_END);
    }

    @Test
    public void testRangeRightClosedOperator() throws IOException {
        StringSource source = new StringSource("{1>..10}");
        Lexer lexer = new Lexer(source);

        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_BEGIN);
        assertNextToken(lexer, TokenType.NUMBER, "1");
        assertNextToken(lexer, TokenType.RANGE_LEFT_OPEN_RIGHT_CLOSED, ">..");
        assertNextToken(lexer, TokenType.NUMBER, "10");
        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_END);
    }

    @Test
    public void testInOperator() throws IOException {
        StringSource source = new StringSource("{a in list}");
        Lexer lexer = new Lexer(source);

        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_BEGIN);
        assertNextToken(lexer, TokenType.IDENTIFIER, "a");
        assertNextToken(lexer, TokenType.IN);
        assertNextToken(lexer, TokenType.IDENTIFIER, "list");
        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_END);
    }

    @Test
    public void testNotInOperator() throws IOException {
        StringSource source = new StringSource("{a !in list}");
        Lexer lexer = new Lexer(source);

        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_BEGIN);
        assertNextToken(lexer, TokenType.IDENTIFIER, "a");
        assertNextToken(lexer, TokenType.NOT_IN, "!in");
        assertNextToken(lexer, TokenType.IDENTIFIER, "list");
        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_END);
    }

    @Test
    public void testRegexLiteral() throws IOException {
        StringSource source = new StringSource("{/[a-z]+/}");
        Lexer lexer = new Lexer(source);

        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_BEGIN);
        assertNextToken(lexer, TokenType.REGEX, "[a-z]+");
        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_END);
    }

    @Test
    public void testRegexWithFlags() throws IOException {
        StringSource source = new StringSource("{/test/gi}");
        Lexer lexer = new Lexer(source);

        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_BEGIN);
        assertNextToken(lexer, TokenType.REGEX, "test:gi");
        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_END);
    }

    @Test
    public void testListLiteral() throws IOException {
        StringSource source = new StringSource("{[1, 2, 3]}");
        Lexer lexer = new Lexer(source);

        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_BEGIN);
        assertNextToken(lexer, TokenType.LEFT_BRACKET);
        assertNextToken(lexer, TokenType.NUMBER, "1");
        assertNextToken(lexer, TokenType.COMMA);
        assertNextToken(lexer, TokenType.NUMBER, "2");
        assertNextToken(lexer, TokenType.COMMA);
        assertNextToken(lexer, TokenType.NUMBER, "3");
        assertNextToken(lexer, TokenType.RIGHT_BRACKET);
        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_END);
    }

    @Test
    public void testStandardStatementBlock() throws IOException {
        // 标准语句块：开始标签以 } 结尾，结束标签 {/% name }
        StringSource source = new StringSource("{% if true }yes{/% if }");
        Lexer lexer = new Lexer(source);

        assertNextToken(lexer, TokenType.STMT_BEGIN);
        assertNextToken(lexer, TokenType.IF);
        assertNextToken(lexer, TokenType.TRUE);
        assertNextToken(lexer, TokenType.RIGHT_BRACE);
        assertNextToken(lexer, TokenType.TEXT, "yes");
        assertNextToken(lexer, TokenType.STMT_CLOSE);
        assertNextToken(lexer, TokenType.IF); // "if" 被识别为关键字
        assertNextToken(lexer, TokenType.RIGHT_BRACE);
    }

    @Test
    public void testMethodCall() throws IOException {
        StringSource source = new StringSource("{user.getName()}");
        Lexer lexer = new Lexer(source);

        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_BEGIN);
        assertNextToken(lexer, TokenType.IDENTIFIER, "user");
        assertNextToken(lexer, TokenType.DOT);
        assertNextToken(lexer, TokenType.IDENTIFIER, "getName");
        assertNextToken(lexer, TokenType.LEFT_PAREN);
        assertNextToken(lexer, TokenType.RIGHT_PAREN);
        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_END);
    }

    @Test
    public void testArrayAccess() throws IOException {
        StringSource source = new StringSource("{list[0]}");
        Lexer lexer = new Lexer(source);

        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_BEGIN);
        assertNextToken(lexer, TokenType.IDENTIFIER, "list");
        assertNextToken(lexer, TokenType.LEFT_BRACKET);
        assertNextToken(lexer, TokenType.NUMBER, "0");
        assertNextToken(lexer, TokenType.RIGHT_BRACKET);
        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_END);
    }

    @Test
    public void testTernaryOperator() throws IOException {
        StringSource source = new StringSource("{a ? b : c}");
        Lexer lexer = new Lexer(source);

        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_BEGIN);
        assertNextToken(lexer, TokenType.IDENTIFIER, "a");
        assertNextToken(lexer, TokenType.QUESTION);
        assertNextToken(lexer, TokenType.IDENTIFIER, "b");
        assertNextToken(lexer, TokenType.COLON);
        assertNextToken(lexer, TokenType.IDENTIFIER, "c");
        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_END);
    }

    @Test
    public void testComplexExpression() throws IOException {
        StringSource source = new StringSource("{user?.address?.city ?? 'Unknown'}");
        Lexer lexer = new Lexer(source);

        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_BEGIN);
        assertNextToken(lexer, TokenType.IDENTIFIER, "user");
        assertNextToken(lexer, TokenType.SAFE_NAV);
        assertNextToken(lexer, TokenType.IDENTIFIER, "address");
        assertNextToken(lexer, TokenType.SAFE_NAV);
        assertNextToken(lexer, TokenType.IDENTIFIER, "city");
        assertNextToken(lexer, TokenType.ELVIS);
        assertNextToken(lexer, TokenType.STRING, "Unknown");
        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_END);
    }

    // ==================== JSON 文本冲突测试 ====================

    @Test
    public void testJsonAsPlainText() throws IOException {
        // JSON 作为普通文本，不应被识别为表达式
        StringSource source = new StringSource("This is JSON: {\"key\": \"value\"}");
        Lexer lexer = new Lexer(source);

        assertNextToken(lexer, TokenType.TEXT, "This is JSON: {\"key\": \"value\"}");
    }

    @Test
    public void testJsonAsPlainText2() throws IOException {
        // JSON 作为普通文本，不应被识别为表达式
        StringSource source = new StringSource("This is JSON: {key: \"value\"}");
        Lexer lexer = new Lexer(source);

        assertNextToken(lexer, TokenType.TEXT, "This is JSON: {key: \"value\"}");
    }


    @Test
    public void testJsonAsPlainText3() throws IOException {
        // JSON 作为普通文本，不应被识别为表达式
        StringSource source = new StringSource("This is JSON:\n" +
                "{\n" +
                "    \"key1\": \"value1\",\n" +
                "    \"key2\": \"value2\",\n" +
                "}");
        Lexer lexer = new Lexer(source);

        assertNextToken(lexer, TokenType.TEXT, "This is JSON:\n" +
                "{\n" +
                "    \"key1\": \"value1\",\n" +
                "    \"key2\": \"value2\",\n" +
                "}");
    }

    @Test
    public void testJsonAsPlainText4() throws IOException {
        // JSON 作为普通文本，不应被识别为表达式
        StringSource source = new StringSource("This is JSON: {\n\r\tkey: \"value\"}");
        Lexer lexer = new Lexer(source);

        assertNextToken(lexer, TokenType.TEXT, "This is JSON: {\n\r\tkey: \"value\"}");
    }


    @Test
    public void testJsonLikeTextWithSingleBrace() throws IOException {
        // 测试包含类似JSON的文本，但使用单花括号表达式
        StringSource source = new StringSource("Config: {name}, Data: {\"id\": 123}");
        Lexer lexer = new Lexer(source);

        // "Config: "
        assertNextToken(lexer, TokenType.TEXT, "Config: ");

        // {name}
        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_BEGIN);
        assertNextToken(lexer, TokenType.IDENTIFIER, "name");
        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_END);

        // ", Data: "
        assertNextToken(lexer, TokenType.TEXT, ", Data: {\"id\": 123}");
    }

    @Test
    public void testDoubleBraceVsSingleBrace() throws IOException {
        // 测试双花括号和单花括号混合使用
        StringSource source = new StringSource("Single: {x}, Double: {{y}}");
        Lexer lexer = new Lexer(source);

        // "Single: "
        assertNextToken(lexer, TokenType.TEXT);

        // {x}
        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_BEGIN, "{");
        assertNextToken(lexer, TokenType.IDENTIFIER, "x");
        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_END);

        // ", Double: "
        assertNextToken(lexer, TokenType.TEXT);

        // {{y}}
        assertNextToken(lexer, TokenType.DOUBLE_BRACE_EXPR_BEGIN, "{{");
        assertNextToken(lexer, TokenType.IDENTIFIER, "y");
        assertNextToken(lexer, TokenType.DOUBLE_BRACE_EXPR_END, "}}");
    }

    @Test
    public void testNestedBracesInExpression() throws IOException {
        // 测试表达式内的嵌套花括号（如对象字面量）
        StringSource source = new StringSource("{{obj.method()}}");
        Lexer lexer = new Lexer(source);

        assertNextToken(lexer, TokenType.DOUBLE_BRACE_EXPR_BEGIN, "{{");
        assertNextToken(lexer, TokenType.IDENTIFIER, "obj");
        assertNextToken(lexer, TokenType.DOT);
        assertNextToken(lexer, TokenType.IDENTIFIER, "method");
        assertNextToken(lexer, TokenType.LEFT_PAREN);
        assertNextToken(lexer, TokenType.RIGHT_PAREN);
        assertNextToken(lexer, TokenType.DOUBLE_BRACE_EXPR_END, "}}");
    }

    // ==================== 变量声明和赋值测试 ====================

    @Test
    public void testLetVariableDeclaration() throws IOException {
        StringSource source = new StringSource("{% let x = 10 %}");
        Lexer lexer = new Lexer(source);

        assertNextToken(lexer, TokenType.STMT_BEGIN);
        assertNextToken(lexer, TokenType.LET);
        assertNextToken(lexer, TokenType.IDENTIFIER, "x");
        assertNextToken(lexer, TokenType.ASSIGN);
        assertNextToken(lexer, TokenType.NUMBER, "10");
        assertNextToken(lexer, TokenType.STMT_END);
    }

    @Test
    public void testLetVariableWithStringValue() throws IOException {
        StringSource source = new StringSource("{% let name = \"Alice\" %}");
        Lexer lexer = new Lexer(source);

        assertNextToken(lexer, TokenType.STMT_BEGIN);
        assertNextToken(lexer, TokenType.LET);
        assertNextToken(lexer, TokenType.IDENTIFIER, "name");
        assertNextToken(lexer, TokenType.ASSIGN);
        assertNextToken(lexer, TokenType.STRING, "Alice");
        assertNextToken(lexer, TokenType.STMT_END);
    }

    @Test
    public void testLetVariableWithExpression() throws IOException {
        StringSource source = new StringSource("{% let sum = a + b * 2 %}");
        Lexer lexer = new Lexer(source);

        assertNextToken(lexer, TokenType.STMT_BEGIN);
        assertNextToken(lexer, TokenType.LET);
        assertNextToken(lexer, TokenType.IDENTIFIER, "sum");
        assertNextToken(lexer, TokenType.ASSIGN);
        assertNextToken(lexer, TokenType.IDENTIFIER, "a");
        assertNextToken(lexer, TokenType.PLUS);
        assertNextToken(lexer, TokenType.IDENTIFIER, "b");
        assertNextToken(lexer, TokenType.MULTIPLY);
        assertNextToken(lexer, TokenType.NUMBER, "2");
        assertNextToken(lexer, TokenType.STMT_END);
    }

    @Test
    public void testMultipleVariableDeclarations() throws IOException {
        StringSource source = new StringSource("{% let x = 1 %}{% let y = 2 %}");
        Lexer lexer = new Lexer(source);

        // 第一个声明
        assertNextToken(lexer, TokenType.STMT_BEGIN);
        assertNextToken(lexer, TokenType.LET);
        assertNextToken(lexer, TokenType.IDENTIFIER, "x");
        assertNextToken(lexer, TokenType.ASSIGN);
        assertNextToken(lexer, TokenType.NUMBER, "1");
        assertNextToken(lexer, TokenType.STMT_END);

        // 第二个声明
        assertNextToken(lexer, TokenType.STMT_BEGIN);
        assertNextToken(lexer, TokenType.LET);
        assertNextToken(lexer, TokenType.IDENTIFIER, "y");
    }

    // ==================== For 语句块测试 ====================

    @Test
    public void testSimpleForLoop() throws IOException {
        // 标准语句块：开始标签以 } 结尾
        StringSource source = new StringSource("{% for item in items }");
        Lexer lexer = new Lexer(source);

        assertNextToken(lexer, TokenType.STMT_BEGIN);
        assertNextToken(lexer, TokenType.FOR);
        assertNextToken(lexer, TokenType.IDENTIFIER, "item");
        assertNextToken(lexer, TokenType.IN);
        assertNextToken(lexer, TokenType.IDENTIFIER, "items");
        assertNextToken(lexer, TokenType.RIGHT_BRACE);
    }

    @Test
    public void testForLoopWithRange() throws IOException {
        // 标准语句块：开始标签以 } 结尾
        StringSource source = new StringSource("{% for i in 1..10 }");
        Lexer lexer = new Lexer(source);

        assertNextToken(lexer, TokenType.STMT_BEGIN);
        assertNextToken(lexer, TokenType.FOR);
        assertNextToken(lexer, TokenType.IDENTIFIER, "i");
        assertNextToken(lexer, TokenType.IN);
        assertNextToken(lexer, TokenType.NUMBER, "1");
        assertNextToken(lexer, TokenType.RANGE_CLOSED, "..");
        assertNextToken(lexer, TokenType.NUMBER, "10");
        assertNextToken(lexer, TokenType.RIGHT_BRACE);
    }

    @Test
    public void testForLoopComplete() throws IOException {
        // 标准语句块语法
        StringSource source = new StringSource("{% for user in users }{user.name}{/% for }");
        Lexer lexer = new Lexer(source);

        // for 语句开始
        assertNextToken(lexer, TokenType.STMT_BEGIN);
        assertNextToken(lexer, TokenType.FOR);
        assertNextToken(lexer, TokenType.IDENTIFIER, "user");
        assertNextToken(lexer, TokenType.IN);
        assertNextToken(lexer, TokenType.IDENTIFIER, "users");
        assertNextToken(lexer, TokenType.RIGHT_BRACE);

        // 循环体内的表达式
        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_BEGIN);
        assertNextToken(lexer, TokenType.IDENTIFIER, "user");
        assertNextToken(lexer, TokenType.DOT);
        assertNextToken(lexer, TokenType.IDENTIFIER, "name");
        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_END);

        // for 语句结束标记
        assertNextToken(lexer, TokenType.STMT_CLOSE, "{/%");
        assertNextToken(lexer, TokenType.FOR); // "for" 被识别为关键字
        assertNextToken(lexer, TokenType.RIGHT_BRACE);
    }

    @Test
    public void testNestedForLoops() throws IOException {
        // 标准语句块语法
        StringSource source = new StringSource("{% for i in list1 }{% for j in list2 }{/% for }{/% for }");
        Lexer lexer = new Lexer(source);

        // 外层 for
        assertNextToken(lexer, TokenType.STMT_BEGIN);
        assertNextToken(lexer, TokenType.FOR);
        assertNextToken(lexer, TokenType.IDENTIFIER, "i");
        assertNextToken(lexer, TokenType.IN);
        assertNextToken(lexer, TokenType.IDENTIFIER, "list1");
        assertNextToken(lexer, TokenType.RIGHT_BRACE);

        // 内层 for
        assertNextToken(lexer, TokenType.STMT_BEGIN);
        assertNextToken(lexer, TokenType.FOR);
        assertNextToken(lexer, TokenType.IDENTIFIER, "j");
        assertNextToken(lexer, TokenType.IN);
        assertNextToken(lexer, TokenType.IDENTIFIER, "list2");
        assertNextToken(lexer, TokenType.RIGHT_BRACE);

        // 内层 for 结束
        assertNextToken(lexer, TokenType.STMT_CLOSE);
        assertNextToken(lexer, TokenType.FOR);
        assertNextToken(lexer, TokenType.RIGHT_BRACE);

        // 外层 for 结束
        assertNextToken(lexer, TokenType.STMT_CLOSE);
        assertNextToken(lexer, TokenType.FOR);
        assertNextToken(lexer, TokenType.RIGHT_BRACE);
    }

    @Test
    public void testForLoopWithOpenRange() throws IOException {
        // 标准语句块语法
        StringSource source = new StringSource("{% for i in 0..<10 }");
        Lexer lexer = new Lexer(source);

        assertNextToken(lexer, TokenType.STMT_BEGIN);
        assertNextToken(lexer, TokenType.FOR);
        assertNextToken(lexer, TokenType.IDENTIFIER, "i");
        assertNextToken(lexer, TokenType.IN);
        assertNextToken(lexer, TokenType.NUMBER, "0");
        assertNextToken(lexer, TokenType.RANGE_LEFT_CLOSED_RIGHT_OPEN, "..<");
        assertNextToken(lexer, TokenType.NUMBER, "10");
        assertNextToken(lexer, TokenType.RIGHT_BRACE);
    }

    // ==================== Switch 语句块测试 ====================

    @Test
    public void testSwitchStatement() throws IOException {
        // 标准语句块语法
        StringSource source = new StringSource("{% switch value }");
        Lexer lexer = new Lexer(source);

        assertNextToken(lexer, TokenType.STMT_BEGIN);
        assertNextToken(lexer, TokenType.SWITCH);
        assertNextToken(lexer, TokenType.IDENTIFIER, "value");
        assertNextToken(lexer, TokenType.RIGHT_BRACE);
    }

    @Test
    public void testSwitchCaseStatement() throws IOException {
        // case 是自闭合语句块，以 %} 结尾
        StringSource source = new StringSource("{% case 1 %}");
        Lexer lexer = new Lexer(source);

        assertNextToken(lexer, TokenType.STMT_BEGIN);
        assertNextToken(lexer, TokenType.CASE);
        assertNextToken(lexer, TokenType.NUMBER, "1");
        assertNextToken(lexer, TokenType.STMT_END);
    }

    @Test
    public void testSwitchDefaultStatement() throws IOException {
        StringSource source = new StringSource("{% default %}");
        Lexer lexer = new Lexer(source);

        assertNextToken(lexer, TokenType.STMT_BEGIN);
        assertNextToken(lexer, TokenType.DEFAULT);
        assertNextToken(lexer, TokenType.STMT_END);
    }

    @Test
    public void testCompleteSwitchBlock() throws IOException {
        // switch 是标准语句块，case/default 是自闭合语句块
        StringSource source = new StringSource(
                "{% switch status }" +
                "{% case 1 %}Active" +
                "{% case 2 %}Inactive" +
                "{% default %}Unknown" +
                "{/% switch }");
        Lexer lexer = new Lexer(source);

        // switch 开始
        assertNextToken(lexer, TokenType.STMT_BEGIN);
        assertNextToken(lexer, TokenType.SWITCH);
        assertNextToken(lexer, TokenType.IDENTIFIER, "status");
        assertNextToken(lexer, TokenType.RIGHT_BRACE);

        // case 1
        assertNextToken(lexer, TokenType.STMT_BEGIN);
        assertNextToken(lexer, TokenType.CASE);
        assertNextToken(lexer, TokenType.NUMBER, "1");
        assertNextToken(lexer, TokenType.STMT_END);
        assertNextToken(lexer, TokenType.TEXT, "Active");

        // case 2
        assertNextToken(lexer, TokenType.STMT_BEGIN);
        assertNextToken(lexer, TokenType.CASE);
        assertNextToken(lexer, TokenType.NUMBER, "2");
        assertNextToken(lexer, TokenType.STMT_END);
        assertNextToken(lexer, TokenType.TEXT, "Inactive");

        // default
        assertNextToken(lexer, TokenType.STMT_BEGIN);
        assertNextToken(lexer, TokenType.DEFAULT);
        assertNextToken(lexer, TokenType.STMT_END);
        assertNextToken(lexer, TokenType.TEXT, "Unknown");

        // switch 结束标记
        assertNextToken(lexer, TokenType.STMT_CLOSE, "{/%");
        assertNextToken(lexer, TokenType.SWITCH); // "switch" 被识别为关键字
        assertNextToken(lexer, TokenType.RIGHT_BRACE);
    }

    @Test
    public void testSwitchCaseWithStringLiteral() throws IOException {
        // case 是自闭合语句块，以 %} 结尾
        StringSource source = new StringSource("{% case \"active\" %}");
        Lexer lexer = new Lexer(source);

        assertNextToken(lexer, TokenType.STMT_BEGIN);
        assertNextToken(lexer, TokenType.CASE);
        assertNextToken(lexer, TokenType.STRING, "active");
        assertNextToken(lexer, TokenType.STMT_END);
    }

    @Test
    public void testSwitchCaseWithExpression() throws IOException {
        // case 是自闭合语句块，以 %} 结尾
        StringSource source = new StringSource("{% case x + 1 %}");
        Lexer lexer = new Lexer(source);

        assertNextToken(lexer, TokenType.STMT_BEGIN);
        assertNextToken(lexer, TokenType.CASE);
        assertNextToken(lexer, TokenType.IDENTIFIER, "x");
        assertNextToken(lexer, TokenType.PLUS);
        assertNextToken(lexer, TokenType.NUMBER, "1");
        assertNextToken(lexer, TokenType.STMT_END);
    }

    // ==================== 综合场景测试 ====================

    @Test
    public void testMixedStatementsAndExpressions() throws IOException {
        // let 是自闭合语句块（%}），for 是标准语句块（}）
        StringSource source = new StringSource(
                "{% let count = 10 %}" +
                "Count: {count}" +
                "{% for i in 1..count }" +
                "Item {i}" +
                "{/% for }");
        Lexer lexer = new Lexer(source);

        // let 语句（自闭合）
        assertNextToken(lexer, TokenType.STMT_BEGIN);
        assertNextToken(lexer, TokenType.LET);
        assertNextToken(lexer, TokenType.IDENTIFIER, "count");
        assertNextToken(lexer, TokenType.ASSIGN);
        assertNextToken(lexer, TokenType.NUMBER, "10");
        assertNextToken(lexer, TokenType.STMT_END);

        // "Count: "
        assertNextToken(lexer, TokenType.TEXT, "Count: ");

        // {count}
        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_BEGIN);
        assertNextToken(lexer, TokenType.IDENTIFIER, "count");
        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_END);

        // for 循环（标准语句块）
        assertNextToken(lexer, TokenType.STMT_BEGIN);
        assertNextToken(lexer, TokenType.FOR);
    }

    @Test
    public void testComplexNestedStructure() throws IOException {
        // for 和 if 都是标准语句块，以 } 结尾
        StringSource source = new StringSource(
                "{% for user in users }" +
                "{% if user.active }" +
                "{{user.name}}" +
                "{/% if }" +
                "{/% for }");
        Lexer lexer = new Lexer(source);

        // for 开始（标准语句块）
        assertNextToken(lexer, TokenType.STMT_BEGIN, "{%");
        assertNextToken(lexer, TokenType.FOR, "for");
        assertNextToken(lexer, TokenType.IDENTIFIER, "user");
        assertNextToken(lexer, TokenType.IN, "in");
        assertNextToken(lexer, TokenType.IDENTIFIER, "users");
        assertNextToken(lexer, TokenType.RIGHT_BRACE, "}");
        
        // if 开始（标准语句块）
        assertNextToken(lexer, TokenType.STMT_BEGIN, "{%");
        assertNextToken(lexer, TokenType.IF, "if");
        assertNextToken(lexer, TokenType.IDENTIFIER, "user");
        assertNextToken(lexer, TokenType.DOT, ".");
        assertNextToken(lexer, TokenType.IDENTIFIER, "active");
        assertNextToken(lexer, TokenType.RIGHT_BRACE, "}");

        // 表达式 {{user.name}}
        assertNextToken(lexer, TokenType.DOUBLE_BRACE_EXPR_BEGIN, "{{");
        assertNextToken(lexer, TokenType.IDENTIFIER, "user");
        assertNextToken(lexer, TokenType.DOT, ".");
        assertNextToken(lexer, TokenType.IDENTIFIER, "name");
        assertNextToken(lexer, TokenType.DOUBLE_BRACE_EXPR_END, "}}");

        // if 结束（标准语句块）
        assertNextToken(lexer, TokenType.STMT_CLOSE, "{/%");
        assertNextToken(lexer, TokenType.IF, "if");
        assertNextToken(lexer, TokenType.RIGHT_BRACE, "}");

        // for 结束（标准语句块）
        assertNextToken(lexer, TokenType.STMT_CLOSE, "{/%");
        assertNextToken(lexer, TokenType.FOR, "for");
        assertNextToken(lexer, TokenType.RIGHT_BRACE, "}");
    }

    // ==================== 哈希表字面量 Token 测试 ====================
    
    @Test
    public void testEmptyHashLiteral() throws IOException {
        // 在表达式上下文中的空哈希表
        StringSource source = new StringSource("{x = {}}");
        Lexer lexer = new Lexer(source);
        
        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_BEGIN);
        assertNextToken(lexer, TokenType.IDENTIFIER, "x");
        assertNextToken(lexer, TokenType.ASSIGN);
        assertNextToken(lexer, TokenType.LEFT_BRACE);
        assertNextToken(lexer, TokenType.RIGHT_BRACE);
        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_END);
    }
    
    @Test
    public void testHashLiteralWithIdentifierKeys() throws IOException {
        // 混合文本和多种表达式边界情况
        // 测试输入包含普通文本、单花括号表达式和双花括号表达式
        // 示例输入：{ "a": 1, "b": 2 }{{name: "张三", age: 25}}{\tx: 0,\ty: 1}{{"ok"}}{{{"value": "ok"}}}
        StringSource source = new StringSource("{ \"a\": 1, \"b\": 2 }{{name: \"张三\", age: 25}}{\tx: 0,\ty: 1}{{\"ok\"}}{{{\"value\": \"ok\"}}}");
        Lexer lexer = new Lexer(source);

        assertNextToken(lexer, TokenType.TEXT, "{ \"a\": 1, \"b\": 2 }");
        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_BEGIN);
        assertNextToken(lexer, TokenType.LEFT_BRACE);
        assertNextToken(lexer, TokenType.IDENTIFIER, "name");
        assertNextToken(lexer, TokenType.COLON);
        assertNextToken(lexer, TokenType.STRING, "张三");
        assertNextToken(lexer, TokenType.COMMA);
        assertNextToken(lexer, TokenType.IDENTIFIER, "age");
        assertNextToken(lexer, TokenType.COLON);
        assertNextToken(lexer, TokenType.NUMBER, "25");
        assertNextToken(lexer, TokenType.RIGHT_BRACE);
        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_END);
        assertNextToken(lexer, TokenType.TEXT, "{\tx: 0,\ty: 1}");
        assertNextToken(lexer, TokenType.DOUBLE_BRACE_EXPR_BEGIN);
        assertNextToken(lexer, TokenType.STRING, "ok");
        assertNextToken(lexer, TokenType.DOUBLE_BRACE_EXPR_END);
        assertNextToken(lexer, TokenType.DOUBLE_BRACE_EXPR_BEGIN);
        assertNextToken(lexer, TokenType.LEFT_BRACE);
        assertNextToken(lexer, TokenType.STRING, "value");
        assertNextToken(lexer, TokenType.COLON);
        assertNextToken(lexer, TokenType.STRING, "ok");
        assertNextToken(lexer, TokenType.RIGHT_BRACE);
        assertNextToken(lexer, TokenType.DOUBLE_BRACE_EXPR_END);


    }
    
    @Test
    public void testHashLiteralWithStringKeys() throws IOException {
        // 字符串作为键：{"user-name": "李四", "user-id": 10086}
        StringSource source = new StringSource("{config = {\"user-name\": \"李四\", \"user-id\": 10086}}");
        Lexer lexer = new Lexer(source);
        
        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_BEGIN);
        assertNextToken(lexer, TokenType.IDENTIFIER, "config");
        assertNextToken(lexer, TokenType.ASSIGN);
        assertNextToken(lexer, TokenType.LEFT_BRACE);
        assertNextToken(lexer, TokenType.STRING, "user-name");
        assertNextToken(lexer, TokenType.COLON);
        assertNextToken(lexer, TokenType.STRING, "李四");
        assertNextToken(lexer, TokenType.COMMA);
        assertNextToken(lexer, TokenType.STRING, "user-id");
        assertNextToken(lexer, TokenType.COLON);
        assertNextToken(lexer, TokenType.NUMBER, "10086");
        assertNextToken(lexer, TokenType.RIGHT_BRACE);
        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_END);
    }
    
    @Test
    public void testHashLiteralWithNumberKeys() throws IOException {
        // 数字作为键：{0: "待处理", 1: "进行中", 2: "已完成"}
        StringSource source = new StringSource("{status = {0: \"待处理\", 1: \"进行中\", 2: \"已完成\"}}");
        Lexer lexer = new Lexer(source);
        
        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_BEGIN);
        assertNextToken(lexer, TokenType.IDENTIFIER, "status");
        assertNextToken(lexer, TokenType.ASSIGN);
        assertNextToken(lexer, TokenType.LEFT_BRACE);
        assertNextToken(lexer, TokenType.NUMBER, "0");
        assertNextToken(lexer, TokenType.COLON);
        assertNextToken(lexer, TokenType.STRING, "待处理");
        assertNextToken(lexer, TokenType.COMMA);
        assertNextToken(lexer, TokenType.NUMBER, "1");
    }
    
    @Test
    public void testHashLiteralWithMixedKeys() throws IOException {
        // 混合类型键：{name: "test", "string-key": 123, 100: true}
        StringSource source = new StringSource("{mixed = {name: \"test\", \"string-key\": 123, 100: true}}");
        Lexer lexer = new Lexer(source);
        
        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_BEGIN);
        assertNextToken(lexer, TokenType.IDENTIFIER, "mixed");
        assertNextToken(lexer, TokenType.ASSIGN);
        assertNextToken(lexer, TokenType.LEFT_BRACE);
        assertNextToken(lexer, TokenType.IDENTIFIER, "name");
        assertNextToken(lexer, TokenType.COLON);
        assertNextToken(lexer, TokenType.STRING, "test");
        assertNextToken(lexer, TokenType.COMMA);
        assertNextToken(lexer, TokenType.STRING, "string-key");
        assertNextToken(lexer, TokenType.COLON);
        assertNextToken(lexer, TokenType.NUMBER, "123");
        assertNextToken(lexer, TokenType.COMMA);
        assertNextToken(lexer, TokenType.NUMBER, "100");
        assertNextToken(lexer, TokenType.COLON);
        assertNextToken(lexer, TokenType.TRUE);
        assertNextToken(lexer, TokenType.RIGHT_BRACE);
        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_END);
    }
    
    @Test
    public void testNestedHashLiteral() throws IOException {
        // 嵌套哈希表：{user: {name: "张三", age: 25}}
        StringSource source = new StringSource("{data = {user: {name: \"张三\", age: 25}}}");
        Lexer lexer = new Lexer(source);
        
        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_BEGIN);
        assertNextToken(lexer, TokenType.IDENTIFIER, "data");
        assertNextToken(lexer, TokenType.ASSIGN);
        assertNextToken(lexer, TokenType.LEFT_BRACE);
        assertNextToken(lexer, TokenType.IDENTIFIER, "user");
        assertNextToken(lexer, TokenType.COLON);
        assertNextToken(lexer, TokenType.LEFT_BRACE);
        assertNextToken(lexer, TokenType.IDENTIFIER, "name");
        assertNextToken(lexer, TokenType.COLON);
        assertNextToken(lexer, TokenType.STRING, "张三");
        assertNextToken(lexer, TokenType.COMMA);
        assertNextToken(lexer, TokenType.IDENTIFIER, "age");
        assertNextToken(lexer, TokenType.COLON);
        assertNextToken(lexer, TokenType.NUMBER, "25");
        assertNextToken(lexer, TokenType.RIGHT_BRACE);
        assertNextToken(lexer, TokenType.RIGHT_BRACE);
        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_END);
    }
    
    @Test
    public void testHashLiteralWithExpressionValues() throws IOException {
        // 值为表达式：{sum: a + b, product: a * b}
        StringSource source = new StringSource("{calc = {sum: a + b, product: a * b}}");
        Lexer lexer = new Lexer(source);
        
        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_BEGIN);
        assertNextToken(lexer, TokenType.IDENTIFIER, "calc");
        assertNextToken(lexer, TokenType.ASSIGN);
        assertNextToken(lexer, TokenType.LEFT_BRACE);
        assertNextToken(lexer, TokenType.IDENTIFIER, "sum");
        assertNextToken(lexer, TokenType.COLON);
        assertNextToken(lexer, TokenType.IDENTIFIER, "a");
        assertNextToken(lexer, TokenType.PLUS);
        assertNextToken(lexer, TokenType.IDENTIFIER, "b");
        assertNextToken(lexer, TokenType.COMMA);
        assertNextToken(lexer, TokenType.IDENTIFIER, "product");
        assertNextToken(lexer, TokenType.COLON);
        assertNextToken(lexer, TokenType.IDENTIFIER, "a");
        assertNextToken(lexer, TokenType.MULTIPLY);
        assertNextToken(lexer, TokenType.IDENTIFIER, "b");
        assertNextToken(lexer, TokenType.RIGHT_BRACE);
        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_END);
    }

    // ==================== 参数变量引用 ($0, $1 等) 测试 ====================

    @Test
    public void testDollarNumberToken() throws IOException {
        // 测试 $0, $1, $10 等参数变量引用
        StringSource source = new StringSource("{$0}");
        Lexer lexer = new Lexer(source);

        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_BEGIN);
        assertNextToken(lexer, TokenType.DOLLAR_NUMBER, "$0");
        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_END);
    }

    @Test
    public void testDollarNumberWithLargeIndex() throws IOException {
        // 测试两位数的参数索引
        StringSource source = new StringSource("{$10}");
        Lexer lexer = new Lexer(source);

        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_BEGIN);
        assertNextToken(lexer, TokenType.DOLLAR_NUMBER, "$10");
        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_END);
    }

    @Test
    public void testDollarNumberInDollarBrace() throws IOException {
        // 测试在 ${} 中的参数变量引用
        StringSource source = new StringSource("${$1}");
        Lexer lexer = new Lexer(source);

        assertNextToken(lexer, TokenType.DOLLAR_BRACE_EXPR_BEGIN);
        assertNextToken(lexer, TokenType.DOLLAR_NUMBER, "$1");
        assertNextToken(lexer, TokenType.DOLLAR_BRACE_EXPR_END);
    }

    @Test
    public void testDollarNumberInDoubleBrace() throws IOException {
        // 测试在 {{}} 中的参数变量引用
        StringSource source = new StringSource("{{$2}}");
        Lexer lexer = new Lexer(source);

        assertNextToken(lexer, TokenType.DOUBLE_BRACE_EXPR_BEGIN);
        assertNextToken(lexer, TokenType.DOLLAR_NUMBER, "$2");
        assertNextToken(lexer, TokenType.DOUBLE_BRACE_EXPR_END);
    }

    @Test
    public void testDollarNumberWithElvis() throws IOException {
        // 测试参数变量引用与 Elvis 运算符组合
        StringSource source = new StringSource("{$0 ?? 'default'}");
        Lexer lexer = new Lexer(source);

        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_BEGIN);
        assertNextToken(lexer, TokenType.DOLLAR_NUMBER, "$0");
        assertNextToken(lexer, TokenType.ELVIS, "??");
        assertNextToken(lexer, TokenType.STRING, "default");
        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_END);
    }

    @Test
    public void testDollarFollowedByIdentifier() throws IOException {
        // $abc 应该是标识符，不是 DOLLAR_NUMBER
        StringSource source = new StringSource("{$abc}");
        Lexer lexer = new Lexer(source);

        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_BEGIN);
        assertNextToken(lexer, TokenType.IDENTIFIER, "$abc");
        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_END);
    }

    @Test
    public void testMultipleDollarNumbers() throws IOException {
        // 测试多个参数变量引用
        StringSource source = new StringSource("{$0}-{$1}-{$2}");
        Lexer lexer = new Lexer(source);

        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_BEGIN);
        assertNextToken(lexer, TokenType.DOLLAR_NUMBER, "$0");
        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_END);
        assertNextToken(lexer, TokenType.TEXT, "-");
        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_BEGIN);
        assertNextToken(lexer, TokenType.DOLLAR_NUMBER, "$1");
        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_END);
        assertNextToken(lexer, TokenType.TEXT, "-");
        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_BEGIN);
        assertNextToken(lexer, TokenType.DOLLAR_NUMBER, "$2");
        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_END);
    }

    // ==================== Lambda 相关 Token 测试 ====================

    @Test
    public void testArrowToken() throws IOException {
        // 测试箭头运算符 ->
        StringSource source = new StringSource("{->}");
        Lexer lexer = new Lexer(source);

        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_BEGIN);
        assertNextToken(lexer, TokenType.ARROW, "->");
        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_END);
    }

    @Test
    public void testArrowTokenBetweenIdentifiers() throws IOException {
        // 测试箭头运算符在标识符之间：a -> b
        StringSource source = new StringSource("{a -> b}");
        Lexer lexer = new Lexer(source);

        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_BEGIN);
        assertNextToken(lexer, TokenType.IDENTIFIER, "a");
        assertNextToken(lexer, TokenType.ARROW, "->");
        assertNextToken(lexer, TokenType.IDENTIFIER, "b");
        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_END);
    }

    @Test
    public void testReturnKeyword() throws IOException {
        // 测试 return 关键字
        StringSource source = new StringSource("{return 42}");
        Lexer lexer = new Lexer(source);

        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_BEGIN);
        assertNextToken(lexer, TokenType.RETURN, "return");
        assertNextToken(lexer, TokenType.NUMBER, "42");
        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_END);
    }

    @Test
    public void testReturnKeywordInBlock() throws IOException {
        // 测试 return 关键字在代码块中
        StringSource source = new StringSource("{if (x > 0) { return x }}");
        Lexer lexer = new Lexer(source);

        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_BEGIN);
        assertNextToken(lexer, TokenType.IF);
        assertNextToken(lexer, TokenType.LEFT_PAREN);
        assertNextToken(lexer, TokenType.IDENTIFIER, "x");
        assertNextToken(lexer, TokenType.GT);
        assertNextToken(lexer, TokenType.NUMBER, "0");
        assertNextToken(lexer, TokenType.RIGHT_PAREN);
        assertNextToken(lexer, TokenType.LEFT_BRACE);
        assertNextToken(lexer, TokenType.RETURN, "return");
        assertNextToken(lexer, TokenType.IDENTIFIER, "x");
        assertNextToken(lexer, TokenType.RIGHT_BRACE);
        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_END);
    }

    // ==================== 自增/自减和复合赋值运算符测试 ====================

    @Test
    public void testIncrementOperator() throws IOException {
        StringSource source = new StringSource("{i++}");
        Lexer lexer = new Lexer(source);

        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_BEGIN);
        assertNextToken(lexer, TokenType.IDENTIFIER, "i");
        assertNextToken(lexer, TokenType.INCREMENT, "++");
        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_END);
    }

    @Test
    public void testDecrementOperator() throws IOException {
        StringSource source = new StringSource("{i--}");
        Lexer lexer = new Lexer(source);

        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_BEGIN);
        assertNextToken(lexer, TokenType.IDENTIFIER, "i");
        assertNextToken(lexer, TokenType.DECREMENT, "--");
        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_END);
    }

    @Test
    public void testPrefixIncrementOperator() throws IOException {
        StringSource source = new StringSource("{++i}");
        Lexer lexer = new Lexer(source);

        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_BEGIN);
        assertNextToken(lexer, TokenType.INCREMENT, "++");
        assertNextToken(lexer, TokenType.IDENTIFIER, "i");
        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_END);
    }

    @Test
    public void testPlusAssignOperator() throws IOException {
        StringSource source = new StringSource("{x += 5}");
        Lexer lexer = new Lexer(source);

        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_BEGIN);
        assertNextToken(lexer, TokenType.IDENTIFIER, "x");
        assertNextToken(lexer, TokenType.PLUS_ASSIGN, "+=");
        assertNextToken(lexer, TokenType.NUMBER, "5");
        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_END);
    }

    @Test
    public void testMinusAssignOperator() throws IOException {
        StringSource source = new StringSource("{x -= 3}");
        Lexer lexer = new Lexer(source);

        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_BEGIN);
        assertNextToken(lexer, TokenType.IDENTIFIER, "x");
        assertNextToken(lexer, TokenType.MINUS_ASSIGN, "-=");
        assertNextToken(lexer, TokenType.NUMBER, "3");
        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_END);
    }

    @Test
    public void testMultiplyAssignOperator() throws IOException {
        StringSource source = new StringSource("{x *= 2}");
        Lexer lexer = new Lexer(source);

        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_BEGIN);
        assertNextToken(lexer, TokenType.IDENTIFIER, "x");
        assertNextToken(lexer, TokenType.MULTIPLY_ASSIGN, "*=");
        assertNextToken(lexer, TokenType.NUMBER, "2");
        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_END);
    }

    @Test
    public void testDivideAssignOperator() throws IOException {
        StringSource source = new StringSource("{x /= 2}");
        Lexer lexer = new Lexer(source);

        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_BEGIN);
        assertNextToken(lexer, TokenType.IDENTIFIER, "x");
        assertNextToken(lexer, TokenType.DIVIDE_ASSIGN, "/=");
        assertNextToken(lexer, TokenType.NUMBER, "2");
        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_END);
    }

    @Test
    public void testModuloAssignOperator() throws IOException {
        StringSource source = new StringSource("{x %= 3}");
        Lexer lexer = new Lexer(source);

        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_BEGIN);
        assertNextToken(lexer, TokenType.IDENTIFIER, "x");
        assertNextToken(lexer, TokenType.MODULO_ASSIGN, "%=");
        assertNextToken(lexer, TokenType.NUMBER, "3");
        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_END);
    }

    @Test
    public void testBitAndAssignOperator() throws IOException {
        StringSource source = new StringSource("{x &= 1}");
        Lexer lexer = new Lexer(source);

        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_BEGIN);
        assertNextToken(lexer, TokenType.IDENTIFIER, "x");
        assertNextToken(lexer, TokenType.BIT_AND_ASSIGN, "&=");
        assertNextToken(lexer, TokenType.NUMBER, "1");
        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_END);
    }

    @Test
    public void testBitOrAssignOperator() throws IOException {
        StringSource source = new StringSource("{x |= 1}");
        Lexer lexer = new Lexer(source);

        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_BEGIN);
        assertNextToken(lexer, TokenType.IDENTIFIER, "x");
        assertNextToken(lexer, TokenType.BIT_OR_ASSIGN, "|=");
        assertNextToken(lexer, TokenType.NUMBER, "1");
        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_END);
    }

    @Test
    public void testBitXorAssignOperator() throws IOException {
        StringSource source = new StringSource("{x ^= 1}");
        Lexer lexer = new Lexer(source);

        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_BEGIN);
        assertNextToken(lexer, TokenType.IDENTIFIER, "x");
        assertNextToken(lexer, TokenType.BIT_XOR_ASSIGN, "^=");
        assertNextToken(lexer, TokenType.NUMBER, "1");
        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_END);
    }

    @Test
    public void testLeftShiftAssignOperator() throws IOException {
        StringSource source = new StringSource("{x <<= 2}");
        Lexer lexer = new Lexer(source);

        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_BEGIN);
        assertNextToken(lexer, TokenType.IDENTIFIER, "x");
        assertNextToken(lexer, TokenType.LEFT_SHIFT_ASSIGN, "<<=");
        assertNextToken(lexer, TokenType.NUMBER, "2");
        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_END);
    }

    @Test
    public void testRightShiftAssignOperator() throws IOException {
        StringSource source = new StringSource("{x >>= 2}");
        Lexer lexer = new Lexer(source);

        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_BEGIN);
        assertNextToken(lexer, TokenType.IDENTIFIER, "x");
        assertNextToken(lexer, TokenType.RIGHT_SHIFT_ASSIGN, ">>=");
        assertNextToken(lexer, TokenType.NUMBER, "2");
        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_END);
    }

    @Test
    public void testUnsignedRightShiftAssignOperator() throws IOException {
        StringSource source = new StringSource("{x >>>= 2}");
        Lexer lexer = new Lexer(source);

        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_BEGIN);
        assertNextToken(lexer, TokenType.IDENTIFIER, "x");
        assertNextToken(lexer, TokenType.UNSIGNED_RIGHT_SHIFT_ASSIGN, ">>>=");
        assertNextToken(lexer, TokenType.NUMBER, "2");
        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_END);
    }

    @Test
    public void testSemicolonInStatement() throws IOException {
        StringSource source = new StringSource("{% for let i = 0; i < 5; i++ }{i}{/% for }");
        Lexer lexer = new Lexer(source);

        assertNextToken(lexer, TokenType.STMT_BEGIN);
        assertNextToken(lexer, TokenType.FOR);
        assertNextToken(lexer, TokenType.LET);
        assertNextToken(lexer, TokenType.IDENTIFIER, "i");
        assertNextToken(lexer, TokenType.ASSIGN);
        assertNextToken(lexer, TokenType.NUMBER, "0");
        assertNextToken(lexer, TokenType.SEMICOLON, ";");
        assertNextToken(lexer, TokenType.IDENTIFIER, "i");
        assertNextToken(lexer, TokenType.LT);
        assertNextToken(lexer, TokenType.NUMBER, "5");
        assertNextToken(lexer, TokenType.SEMICOLON, ";");
        assertNextToken(lexer, TokenType.IDENTIFIER, "i");
        assertNextToken(lexer, TokenType.INCREMENT, "++");
        assertNextToken(lexer, TokenType.RIGHT_BRACE);
    }

    @Test
    public void testTriplePlusScannedAsIncrementAndPlus() throws IOException {
        // +++ should be scanned as ++ (INCREMENT) then + (PLUS)
        StringSource source = new StringSource("{a +++ b}");
        Lexer lexer = new Lexer(source);

        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_BEGIN);
        assertNextToken(lexer, TokenType.IDENTIFIER, "a");
        assertNextToken(lexer, TokenType.INCREMENT, "++");
        assertNextToken(lexer, TokenType.PLUS, "+");
        assertNextToken(lexer, TokenType.IDENTIFIER, "b");
        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_END);
    }

    @Test
    public void testPlusPlusEqualScannedAsIncrementAndPlusAssign() throws IOException {
        // ++= should be scanned as ++ (INCREMENT) then = ... but actually ++= is ++ followed by = which is ASSIGN
        // Let me think: ++= would be: first + sees next char is +, so returns INCREMENT. Then sees =, peeks... it's end of expression
        // Actually ++= would be: first + sees next char is +, so returns INCREMENT. Then = returns ASSIGN.
        StringSource source = new StringSource("{a ++ = b}");
        Lexer lexer = new Lexer(source);

        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_BEGIN);
        assertNextToken(lexer, TokenType.IDENTIFIER, "a");
        assertNextToken(lexer, TokenType.INCREMENT, "++");
        assertNextToken(lexer, TokenType.ASSIGN, "=");
        assertNextToken(lexer, TokenType.IDENTIFIER, "b");
        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_END);
    }
}

