package org.dromara.easytl.parser;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TokenStream 类的测试
 *
 * @author EasyTL Team
 */
public class TokenStreamTest {

    @Test
    public void testNextToken() throws IOException {
        StringSource source = new StringSource("abc def");
        Lexer lexer = new Lexer(source);
        TokenStream stream = new TokenStream(lexer);

        Token token1 = stream.nextToken();
        assertEquals(TokenType.TEXT, token1.getType());
        assertEquals("abc def", token1.getValue());

        Token token2 = stream.nextToken();
        assertEquals(TokenType.EOF, token2.getType());
    }

    @Test
    public void testPeek() throws IOException {
        StringSource source = new StringSource("{a}");
        Lexer lexer = new Lexer(source);
        TokenStream stream = new TokenStream(lexer);

        Token peek1 = stream.peek();
        Token peek2 = stream.peek();
        assertSame(peek1.getType(), peek2.getType());

        Token next = stream.nextToken();
        assertEquals(peek1.getType(), next.getType());
    }

    @Test
    public void testPeekOffset() throws IOException {
        StringSource source = new StringSource("{a + b}");
        Lexer lexer = new Lexer(source);
        TokenStream stream = new TokenStream(lexer);

        assertEquals(TokenType.SINGLE_BRACE_EXPR_BEGIN, stream.peek(0).getType());
        assertEquals(TokenType.IDENTIFIER, stream.peek(1).getType());
        assertEquals(TokenType.PLUS, stream.peek(2).getType());

        stream.nextToken(); // consume {
        assertEquals(TokenType.IDENTIFIER, stream.peek(0).getType());
    }

    @Test
    public void testHasNext() throws IOException {
        StringSource source = new StringSource("{a}");
        Lexer lexer = new Lexer(source);
        TokenStream stream = new TokenStream(lexer);

        assertTrue(stream.hasNext());
        stream.nextToken(); // {
        assertTrue(stream.hasNext());
        stream.nextToken(); // a
        assertTrue(stream.hasNext());
        stream.nextToken(); // }
        assertFalse(stream.hasNext());
    }

    @Test
    public void testReadTemplateFile() throws IOException {
        // 构建测试文件路径
        String testFilePath = "src/test/resources/token_test.etl";
        File testFile = new File(testFilePath);

        // 验证文件存在
        assertTrue(testFile.exists(), "测试文件不存在: " + testFilePath);
        assertTrue(testFile.isFile(), "测试路径不是文件: " + testFilePath);

        // 使用 FileSource 读取文件
        FileSource source = new FileSource(testFile);
        Lexer lexer = new Lexer(source);
        TokenStream stream = new TokenStream(lexer);

        // 测试能否成功读取第一个 token
        assertTrue(stream.hasNext(), "TokenStream 应该有至少一个 token");
        Token firstToken = stream.nextToken();
        assertNotNull(firstToken, "第一个 token 不应为 null");

        // 验证文件开头的内容（注释和文本）
        assertEquals(TokenType.TEXT, firstToken.getType(), "第一个 token 应该是 TEXT 类型");
        String firstTokenValue = firstToken.getValue();
        assertNotNull(firstTokenValue, "第一个 token 的值不应为 null");
        assertTrue(firstTokenValue.contains("EasyTL 超大测试文件"),
                "第一个 token 应包含文件标题");

        // 继续读取几个 token，验证能够正常解析
        int tokenCount = 1; // 已经读取了第一个 token
        int maxTokensToRead = 100; // 读取前 100 个 token 进行验证

        while (stream.hasNext() && tokenCount < maxTokensToRead) {
            Token token = stream.nextToken();
            assertNotNull(token, "Token 不应为 null");
            assertNotNull(token.getType(), "Token 类型不应为 null");
            tokenCount++;
        }

        assertTrue(tokenCount > 10, "应该至少能读取 10 个 token");
        System.out.println("成功读取了前 " + tokenCount + " 个 token");

        // 验证 TokenStream 能够继续读取更多内容
        assertTrue(stream.hasNext(), "超大文件应该还有更多 token 可读取");
    }

    @Test
    public void testTemplateFileTokenTypes() throws IOException {
        // 使用 FileSource 读取模板文件
        String testFilePath = "src/test/resources/token_test.etl";
        FileSource source = new FileSource(testFilePath);
        Lexer lexer = new Lexer(source);
        TokenStream stream = new TokenStream(lexer);

        int textTokenCount = 0;
        int exprBeginCount = 0;
        int identifierCount = 0;
        int stmtBeginCount = 0;
        int tokenCount = 0;
        int maxTokensToCheck = 1000; // 检查前 1000 个 token

        // 统计前 1000 个 token 的类型分布
        while (stream.hasNext() && tokenCount < maxTokensToCheck) {
            Token token = stream.nextToken();

            switch (token.getType()) {
                case TokenType.TEXT:
                    textTokenCount++;
                    break;
                case TokenType.SINGLE_BRACE_EXPR_BEGIN:
                case TokenType.DOUBLE_BRACE_EXPR_BEGIN:
                    exprBeginCount++;
                    break;
                case TokenType.IDENTIFIER:
                    identifierCount++;
                    break;
                case TokenType.STMT_BEGIN:
                    stmtBeginCount++;
                    break;
                default:
                    break;
            }

            tokenCount++;
        }

        // 验证各种 token 类型都有出现
        assertTrue(textTokenCount > 0, "应该有 TEXT 类型的 token");
        assertTrue(exprBeginCount > 0, "应该有 EXPR_BEGIN 类型的 token（表达式）");
        assertTrue(identifierCount > 0, "应该有 IDENTIFIER 类型的 token");

        System.out.println("Token 统计信息（前 " + tokenCount + " 个）：");
        System.out.println("  TEXT tokens: " + textTokenCount);
        System.out.println("  EXPR_BEGIN tokens: " + exprBeginCount);
        System.out.println("  IDENTIFIER tokens: " + identifierCount);
        System.out.println("  STMT_BEGIN tokens: " + stmtBeginCount);
    }

    /**
     * 测试从 token_test.etl 文件中读取 Token，并抽样检查特定位置的 Token 类型和内容
     * 验证词法分析器能够正确解析文件中的各种模板语法
     */
    @Test
    public void testTokenSamplingFromFile() throws IOException {
        // 读取模板文件
        String testFilePath = "src/test/resources/token_test.etl";
        FileSource source = new FileSource(testFilePath);
        Lexer lexer = new Lexer(source);
        TokenStream stream = new TokenStream(lexer);

        // 收集前 1000 个 token 用于抽样检查（增加数量以确保能扫描到商品列表部分）
        java.util.List<Token> tokens = new java.util.ArrayList<>();
        int maxTokens = 1000;

        while (stream.hasNext() && tokens.size() < maxTokens) {
            tokens.add(stream.nextToken());
        }

        assertTrue(tokens.size() > 0, "应该能够读取到 token");
        System.out.println("\n========== Token 抽样检查报告 ==========");
        System.out.println("总共收集了 " + tokens.size() + " 个 token 进行检查\n");

        // 抽样 1：检查文件开头的 TEXT token（第1-2行的注释）
        System.out.println("【抽样 1】检查文件开头的注释内容：");
        Token firstToken = tokens.get(0);
        assertEquals(TokenType.TEXT, firstToken.getType(), "第一个 token 应该是 TEXT 类型");
        assertNotNull(firstToken.getValue(), "第一个 token 应该有值");
        assertTrue(firstToken.getValue().contains("EasyTL 超大测试文件"),
                "第一个 token 应包含文件标题");
        assertTrue(firstToken.getValue().contains("token_test.etl"),
                "第一个 token 应包含文件名");
        System.out.println("  ✓ Token 类型: " + TokenType.getName(firstToken.getType()));
        System.out.println("  ✓ Token 内容包含预期的文件标题和说明\n");

        // 抽样 2：查找并检查第一个双花括号表达式 "{{user1.id}}"
        System.out.println("【抽样 2】检查双花括号表达式 {{user1.id}}：");
        boolean foundUserIdExpr = false;
        for (int i = 0; i < tokens.size() - 5; i++) {
            Token t = tokens.get(i);
            // 查找包含 "用户ID:" 的 TEXT token
            if (t.getType() == TokenType.TEXT && t.getValue() != null
                    && t.getValue().contains("用户ID:")) {
                // 下一个应该是 DOUBLE_BRACE_EXPR_BEGIN ({{user1.id}})
                Token exprBegin = tokens.get(i + 1);
                assertEquals(TokenType.DOUBLE_BRACE_EXPR_BEGIN, exprBegin.getType(),
                        "用户ID: 后应该是双花括号表达式开始标记");

                // 接着应该是 IDENTIFIER "user1"
                Token identifier1 = tokens.get(i + 2);
                assertEquals(TokenType.IDENTIFIER, identifier1.getType(),
                        "应该是标识符 user1");
                assertEquals("user1", identifier1.getValue(),
                        "标识符值应该是 user1");

                // 然后是 DOT
                Token dot = tokens.get(i + 3);
                assertEquals(TokenType.DOT, dot.getType(), "应该是点操作符");

                // 再是 IDENTIFIER "id"
                Token identifier2 = tokens.get(i + 4);
                assertEquals(TokenType.IDENTIFIER, identifier2.getType(),
                        "应该是标识符 id");
                assertEquals("id", identifier2.getValue(),
                        "标识符值应该是 id");

                // 最后是 DOUBLE_BRACE_EXPR_END
                Token exprEnd = tokens.get(i + 5);
                assertEquals(TokenType.DOUBLE_BRACE_EXPR_END, exprEnd.getType(),
                        "应该是双花括号表达式结束标记");

                foundUserIdExpr = true;
                System.out.println("  ✓ 找到表达式序列: TEXT(用户ID:) → DOUBLE_BRACE_EXPR_BEGIN → IDENTIFIER(user1) → DOT → IDENTIFIER(id) → DOUBLE_BRACE_EXPR_END");
                System.out.println("  ✓ Token 类型和值完全匹配预期\n");
                break;
            }
        }
        assertTrue(foundUserIdExpr, "应该能找到 {{user1.id}} 表达式");

        // 抽样 3：查找并检查 switch 语句块 "{% switch order1.status %}"
        System.out.println("【抽样 3】检查 switch 语句块：");
        boolean foundSwitchStmt = false;
        for (int i = 0; i < tokens.size() - 6; i++) {
            Token t = tokens.get(i);
            // 查找 STMT_BEGIN
            if (t.getType() == TokenType.STMT_BEGIN) {
                // 下一个应该是 SWITCH 关键字
                Token switchToken = tokens.get(i + 1);
                if (switchToken.getType() == TokenType.SWITCH) {
                    // 接着应该是 IDENTIFIER "order1"
                    Token identifier1 = tokens.get(i + 2);
                    assertEquals(TokenType.IDENTIFIER, identifier1.getType(),
                            "应该是标识符 order1");

                    // 如果是 order1，继续检查
                    if ("order1".equals(identifier1.getValue())) {
                        // 然后是 DOT
                        Token dot = tokens.get(i + 3);
                        assertEquals(TokenType.DOT, dot.getType(), "应该是点操作符");

                        // 再是 IDENTIFIER "status"
                        Token identifier2 = tokens.get(i + 4);
                        assertEquals(TokenType.IDENTIFIER, identifier2.getType(),
                                "应该是标识符 status");
                        assertEquals("status", identifier2.getValue(),
                                "标识符值应该是 status");

                        // 最后是 RIGHT_BRACE（标准语句块开始标签以 } 结尾）
                        Token rightBrace = tokens.get(i + 5);
                        assertEquals(TokenType.RIGHT_BRACE, rightBrace.getType(),
                                "应该是右花括号（标准语句块开始标签以 } 结尾）");

                        foundSwitchStmt = true;
                        System.out.println("  ✓ 找到语句序列: STMT_BEGIN → SWITCH → IDENTIFIER(order1) → DOT → IDENTIFIER(status) → RIGHT_BRACE");
                        System.out.println("  ✓ Token 类型和值完全匹配预期\n");
                        break;
                    }
                }
            }
        }
        assertTrue(foundSwitchStmt, "应该能找到 {% switch order1.status %} 语句");

        // 抽样 4：查找并检查 case 语句 "{% case 1 }"（在标准语句块内部，以 } 结尾）
        System.out.println("【抽样 4】检查 case 语句：");
        boolean foundCaseStmt = false;
        for (int i = 0; i < tokens.size() - 3; i++) {
            Token t = tokens.get(i);
            // 查找 STMT_BEGIN 后跟 CASE
            if (t.getType() == TokenType.STMT_BEGIN) {
                Token caseToken = tokens.get(i + 1);
                if (caseToken.getType() == TokenType.CASE) {
                    // 接着应该是 NUMBER
                    Token number = tokens.get(i + 2);
                    if (number.getType() == TokenType.NUMBER) {
                        // 最后是 RIGHT_BRACE（case 在标准语句块内部，以 } 结尾）
                        Token rightBrace = tokens.get(i + 3);
                        assertEquals(TokenType.RIGHT_BRACE, rightBrace.getType(),
                                "应该是右花括号（case 语句以 } 结尾）");

                        foundCaseStmt = true;
                        System.out.println("  ✓ 找到语句序列: STMT_BEGIN → CASE → NUMBER(" + number.getValue() + ") → RIGHT_BRACE");
                        System.out.println("  ✓ Token 类型和值完全匹配预期\n");
                        break;
                    }
                }
            }
        }
        assertTrue(foundCaseStmt, "应该能找到 case 语句");

        // 抽样 5：查找并检查单花括号表达式 "{product1_1.name}"
        System.out.println("【抽样 5】检查单花括号表达式 {product1_1.name}：");
        boolean foundProductExpr = false;
        for (int i = 0; i < tokens.size() - 5; i++) {
            Token t = tokens.get(i);
            // 查找 SINGLE_BRACE_EXPR_BEGIN
            if (t.getType() == TokenType.SINGLE_BRACE_EXPR_BEGIN) {
                Token identifier1 = tokens.get(i + 1);
                // 查找以 product 开头的标识符
                if (identifier1.getType() == TokenType.IDENTIFIER
                        && identifier1.getValue() != null
                        && identifier1.getValue().startsWith("product")) {

                    // 然后是 DOT
                    Token dot = tokens.get(i + 2);
                    if (dot.getType() == TokenType.DOT) {
                        // 再是 IDENTIFIER "name"
                        Token identifier2 = tokens.get(i + 3);
                        if (identifier2.getType() == TokenType.IDENTIFIER
                                && "name".equals(identifier2.getValue())) {

                            // 最后是 SINGLE_BRACE_EXPR_END
                            Token exprEnd = tokens.get(i + 4);
                            assertEquals(TokenType.SINGLE_BRACE_EXPR_END, exprEnd.getType(),
                                    "应该是单花括号表达式结束标记");

                            foundProductExpr = true;
                            System.out.println("  ✓ 找到表达式序列: SINGLE_BRACE_EXPR_BEGIN → IDENTIFIER(" + identifier1.getValue()
                                    + ") → DOT → IDENTIFIER(name) → SINGLE_BRACE_EXPR_END");
                            System.out.println("  ✓ Token 类型和值完全匹配预期\n");
                            break;
                        }
                    }
                }
            }
        }
        assertTrue(foundProductExpr, "应该能找到 {product.name} 形式的表达式");

        // 抽样 6：统计和验证 Token 类型分布
        System.out.println("【抽样 6】Token 类型分布统计：");
        int textCount = 0, exprBeginCount = 0, identifierCount = 0;
        int dotCount = 0, stmtBeginCount = 0, switchCount = 0, caseCount = 0;

        for (Token token : tokens) {
            switch (token.getType()) {
                case TokenType.TEXT:
                    textCount++;
                    break;
                case TokenType.SINGLE_BRACE_EXPR_BEGIN:
                case TokenType.DOUBLE_BRACE_EXPR_BEGIN:
                    exprBeginCount++;
                    break;
                case TokenType.IDENTIFIER:
                    identifierCount++;
                    break;
                case TokenType.DOT:
                    dotCount++;
                    break;
                case TokenType.STMT_BEGIN:
                    stmtBeginCount++;
                    break;
                case TokenType.SWITCH:
                    switchCount++;
                    break;
                case TokenType.CASE:
                    caseCount++;
                    break;
            }
        }

        System.out.println("  TEXT tokens: " + textCount);
        System.out.println("  EXPR_BEGIN tokens: " + exprBeginCount);
        System.out.println("  IDENTIFIER tokens: " + identifierCount);
        System.out.println("  DOT tokens: " + dotCount);
        System.out.println("  STMT_BEGIN tokens: " + stmtBeginCount);
        System.out.println("  SWITCH tokens: " + switchCount);
        System.out.println("  CASE tokens: " + caseCount);

        // 验证基本的 token 类型都存在
        assertTrue(textCount > 0, "应该有 TEXT 类型的 token");
        assertTrue(exprBeginCount > 0, "应该有 EXPR_BEGIN 类型的 token");
        assertTrue(identifierCount > 0, "应该有 IDENTIFIER 类型的 token");
        assertTrue(dotCount > 0, "应该有 DOT 类型的 token");

        System.out.println("\n========== Token 抽样检查完成 ==========");
        System.out.println("✓ 所有抽样检查通过，Token 类型和内容与文件内容一致");
    }

    /**
     * 测试从 token_test_simple.etl 文件中读取所有 Token，并验证每个 Token 是否符合预期
     * 该文件包含了所有模板语法的示例，是一个全面的词法分析测试
     */
    @Test
    public void testReadAllTokensFromSimpleFile() throws IOException {
        // 读取简化测试文件
        String testFilePath = "src/test/resources/token_test_simple.etl";
        FileSource source = new FileSource(testFilePath);
        Lexer lexer = new Lexer(source);
        TokenStream stream = new TokenStream(lexer);

        // 读取所有 Token 到 List 中
        java.util.List<Token> allTokens = new java.util.ArrayList<>();
        while (stream.hasNext()) {
            Token token = stream.nextToken();
            allTokens.add(token);
            // 打印每个 token 用于调试（可选）
            // if (allTokens.size() <= 50) {
            //     System.out.println(String.format("[%d] %s: %s",
            //         allTokens.size(), TokenType.getName(token.getType()),
            //         token.getValue() != null && token.getValue().length() > 50 ?
            //             token.getValue().substring(0, 50) + "..." : token.getValue()));
            // }
        }

        // 验证读取到了 Token
        assertTrue(allTokens.size() > 0, "应该能读取到 Token");
        System.out.println("\n========== token_test_simple.etl Token 验证报告 ==========");
        System.out.println("总共读取了 " + allTokens.size() + " 个 Token\n");

        int tokenIndex = 0;

        // ===== 验证部分 1：文件头部的纯文本 =====
        System.out.println("【验证 1】文件头部纯文本：");
        Token headerToken = allTokens.get(tokenIndex++);
        assertEquals(TokenType.TEXT, headerToken.getType(), "文件头应该是 TEXT 类型");
        assertTrue(headerToken.getValue().contains("EasyTL 简化测试文件"),
                "文件头应包含标题");
        System.out.println("  ✓ 文件头部纯文本验证通过\n");

        // ===== 验证部分 2：单花括号表达式 {user.name} =====
        System.out.println("【验证 2】单花括号表达式 {user.name}：");
        // 从第一个TEXT token开始，它包含了整个文件头（包括"单花括号: Hello "）
        // 第二个token就是单花括号表达式的开始
        // 基于调试输出：[1] TEXT → [2] SINGLE_BRACE_EXPR_BEGIN → [3] IDENTIFIER(user) → [4] DOT → [5] IDENTIFIER(name) → [6] SINGLE_BRACE_EXPR_END
        assertEquals(TokenType.SINGLE_BRACE_EXPR_BEGIN, allTokens.get(1).getType(), "Token[1] 应该是 SINGLE_BRACE_EXPR_BEGIN");
        assertEquals("{", allTokens.get(1).getValue(), "单花括号表达式开始");
        assertEquals(TokenType.IDENTIFIER, allTokens.get(2).getType());
        assertEquals("user", allTokens.get(2).getValue());
        assertEquals(TokenType.DOT, allTokens.get(3).getType());
        assertEquals(TokenType.IDENTIFIER, allTokens.get(4).getType());
        assertEquals("name", allTokens.get(4).getValue());
        assertEquals(TokenType.SINGLE_BRACE_EXPR_END, allTokens.get(5).getType(),
                "单花括号表达式应该以 SINGLE_BRACE_EXPR_END 结束");
        System.out.println("  ✓ 单花括号表达式验证通过\n");

        // ===== 验证部分 3：双花括号表达式 {{user.name}} =====
        System.out.println("【验证 3】双花括号表达式 {{user.name}}：");
        // 基于调试输出：[7] TEXT（包含"双花括号: Hello "） → [8] DOUBLE_BRACE_EXPR_BEGIN → [9] IDENTIFIER(user) → [10] DOT → [11] IDENTIFIER(name) → [12] DOUBLE_BRACE_EXPR_END
        assertEquals(TokenType.DOUBLE_BRACE_EXPR_BEGIN, allTokens.get(7).getType(), "Token[7] 应该是 DOUBLE_BRACE_EXPR_BEGIN");
        assertEquals("{{", allTokens.get(7).getValue(), "双花括号表达式开始");
        assertEquals(TokenType.IDENTIFIER, allTokens.get(8).getType());
        assertEquals("user", allTokens.get(8).getValue());
        assertEquals(TokenType.DOT, allTokens.get(9).getType());
        assertEquals(TokenType.IDENTIFIER, allTokens.get(10).getType());
        assertEquals("name", allTokens.get(10).getValue());
        assertEquals(TokenType.DOUBLE_BRACE_EXPR_END, allTokens.get(11).getType(),
                "双花括号表达式应该以 DOUBLE_BRACE_EXPR_END 结束");
        assertEquals("}}", allTokens.get(11).getValue());
        System.out.println("  ✓ 双花括号表达式验证通过\n");

        // ===== 验证部分 4：整数字面量 =====
        System.out.println("【验证 4】整数字面量：");
        // 查找包含值 "25" 和 "-42" 的 NUMBER token
        boolean found25 = false;
        boolean foundMinus42 = false;
        for (Token t : allTokens) {
            if (t.getType() == TokenType.NUMBER) {
                if ("25".equals(t.getValue())) found25 = true;
                if ("-42".equals(t.getValue())) foundMinus42 = true;
            }
        }
        assertTrue(found25, "应该找到数字 25");
        assertTrue(foundMinus42, "应该找到数字 -42");
        System.out.println("  ✓ 整数字面量验证通过：25, -42\n");

        // ===== 验证部分 5：字符串字面量 =====
        System.out.println("【验证 5】字符串字面量：");
        // 查找包含 "Hello World" 的 STRING token
        int helloWorldCount = 0;
        for (Token t : allTokens) {
            if (t.getType() == TokenType.STRING && "Hello World".equals(t.getValue())) {
                helloWorldCount++;
            }
        }
        assertTrue(helloWorldCount >= 2, "应该找到至少2个 'Hello World' 字符串（单引号和双引号）");
        System.out.println("  ✓ 字符串字面量验证通过：找到 " + helloWorldCount + " 个 'Hello World'\n");

        // ===== 验证部分 6：布尔值和空值 =====
        System.out.println("【验证 6】布尔值和空值：");
        int trueCount = 0, falseCount = 0, nullCount = 0;
        for (Token t : allTokens) {
            if (t.getType() == TokenType.TRUE) trueCount++;
            if (t.getType() == TokenType.FALSE) falseCount++;
            if (t.getType() == TokenType.NULL) nullCount++;
        }
        assertTrue(trueCount > 0, "应该有 true 关键字");
        assertTrue(falseCount > 0, "应该有 false 关键字");
        assertTrue(nullCount > 0, "应该有 null 关键字");
        System.out.println("  ✓ 找到 true: " + trueCount + " 次");
        System.out.println("  ✓ 找到 false: " + falseCount + " 次");
        System.out.println("  ✓ 找到 null: " + nullCount + " 次\n");

        // ===== 验证部分 7：算术运算符 =====
        System.out.println("【验证 7】算术运算符：");
        int plusCount = 0, minusCount = 0, multiplyCount = 0, divideCount = 0, moduloCount = 0;
        for (Token t : allTokens) {
            switch (t.getType()) {
                case TokenType.PLUS: plusCount++; break;
                case TokenType.MINUS: minusCount++; break;
                case TokenType.MULTIPLY: multiplyCount++; break;
                case TokenType.DIVIDE: divideCount++; break;
                case TokenType.MODULO: moduloCount++; break;
            }
        }
        assertTrue(plusCount > 0, "应该有 + 运算符");
        assertTrue(minusCount > 0, "应该有 - 运算符");
        assertTrue(multiplyCount > 0, "应该有 * 运算符");
        assertTrue(divideCount > 0, "应该有 / 运算符");
        assertTrue(moduloCount > 0, "应该有 % 运算符");
        System.out.println("  ✓ 所有算术运算符验证通过\n");

        // ===== 验证部分 8：比较运算符 =====
        System.out.println("【验证 8】比较运算符：");
        int gtCount = 0, ltCount = 0, gteCount = 0, lteCount = 0, eqCount = 0, neqCount = 0;
        for (Token t : allTokens) {
            switch (t.getType()) {
                case TokenType.GT: gtCount++; break;
                case TokenType.LT: ltCount++; break;
                case TokenType.GTE: gteCount++; break;
                case TokenType.LTE: lteCount++; break;
                case TokenType.EQ: eqCount++; break;
                case TokenType.NEQ: neqCount++; break;
            }
        }
        assertTrue(gtCount > 0, "应该有 > 运算符");
        assertTrue(ltCount > 0, "应该有 < 运算符");
        assertTrue(gteCount > 0, "应该有 >= 运算符");
        assertTrue(lteCount > 0, "应该有 <= 运算符");
        assertTrue(eqCount > 0, "应该有 == 运算符");
        assertTrue(neqCount > 0, "应该有 != 运算符");
        System.out.println("  ✓ 所有比较运算符验证通过\n");

        // ===== 验证部分 9：逻辑运算符 =====
        System.out.println("【验证 9】逻辑运算符：");
        int andCount = 0, orCount = 0, notCount = 0;
        for (Token t : allTokens) {
            switch (t.getType()) {
                case TokenType.AND: andCount++; break;
                case TokenType.OR: orCount++; break;
                case TokenType.NOT: notCount++; break;
            }
        }
        assertTrue(andCount > 0, "应该有 && 运算符");
        assertTrue(orCount > 0, "应该有 || 运算符");
        assertTrue(notCount > 0, "应该有 ! 运算符");
        System.out.println("  ✓ 所有逻辑运算符验证通过\n");

        // ===== 验证部分 10：if 语句 =====
        System.out.println("【验证 10】if 条件语句：");
        int ifCount = 0, elseCount = 0;
        for (Token t : allTokens) {
            if (t.getType() == TokenType.IF) ifCount++;
            if (t.getType() == TokenType.ELSE) elseCount++;
        }
        assertTrue(ifCount > 0, "应该找到 if 关键字");
        assertTrue(elseCount > 0, "应该找到 else 关键字");
        System.out.println("  ✓ 找到 if 语句：" + ifCount + " 次, else：" + elseCount + " 次\n");

        // ===== 验证部分 11：for 循环语句 =====
        System.out.println("【验证 11】for 循环语句：");
        int forCount = 0, inCount = 0;
        for (Token t : allTokens) {
            if (t.getType() == TokenType.FOR) forCount++;
            if (t.getType() == TokenType.IN) inCount++;
        }
        assertTrue(forCount > 0, "应该找到 for 关键字");
        assertTrue(inCount > 0, "应该找到 in 关键字");
        System.out.println("  ✓ 找到 for 循环：" + forCount + " 次, in：" + inCount + " 次\n");

        // ===== 验证部分 12：switch 语句 =====
        System.out.println("【验证 12】switch 语句：");
        boolean foundSwitchStmt = false, foundCaseStmt = false, foundDefaultStmt = false;
        for (Token t : allTokens) {
            if (t.getType() == TokenType.SWITCH) foundSwitchStmt = true;
            if (t.getType() == TokenType.CASE) foundCaseStmt = true;
            if (t.getType() == TokenType.DEFAULT) foundDefaultStmt = true;
        }
        assertTrue(foundSwitchStmt, "应该找到 switch 关键字");
        assertTrue(foundCaseStmt, "应该找到 case 关键字");
        assertTrue(foundDefaultStmt, "应该找到 default 关键字");
        System.out.println("  ✓ switch 语句所有关键字验证通过\n");

        // ===== 验证部分 13：let 语句 =====
        System.out.println("【验证 13】let 变量声明：");
        int letCount = 0, assignCount = 0;
        for (Token t : allTokens) {
            if (t.getType() == TokenType.LET) letCount++;
            if (t.getType() == TokenType.ASSIGN) assignCount++;
        }
        assertTrue(letCount > 0, "应该找到 let 关键字");
        assertTrue(assignCount > 0, "应该找到 = 赋值运算符");
        System.out.println("  ✓ 找到 let 语句：" + letCount + " 次, 赋值运算符：" + assignCount + " 次\n");

        // ===== 验证部分 14：import 语句 =====
        System.out.println("【验证 14】import 语句：");
        int importCount = 0;
        for (Token t : allTokens) {
            if (t.getType() == TokenType.IMPORT) {
                importCount++;
            }
        }
        assertTrue(importCount > 0, "应该找到 import 关键字");
        System.out.println("  ✓ 找到 " + importCount + " 个 import 语句\n");

        // ===== 统计报告 =====
        System.out.println("【统计报告】所有 Token 类型分布：");
        java.util.Map<Integer, Integer> tokenTypeCounts = new java.util.HashMap<>();
        for (Token t : allTokens) {
            tokenTypeCounts.put(t.getType(), tokenTypeCounts.getOrDefault(t.getType(), 0) + 1);
        }

        System.out.println("  TEXT: " + tokenTypeCounts.getOrDefault(TokenType.TEXT, 0));
        System.out.println("  SINGLE_BRACE_EXPR_BEGIN: " + tokenTypeCounts.getOrDefault(TokenType.SINGLE_BRACE_EXPR_BEGIN, 0));
        System.out.println("  SINGLE_BRACE_EXPR_END: " + tokenTypeCounts.getOrDefault(TokenType.SINGLE_BRACE_EXPR_END, 0));
        System.out.println("  DOUBLE_BRACE_EXPR_BEGIN: " + tokenTypeCounts.getOrDefault(TokenType.DOUBLE_BRACE_EXPR_BEGIN, 0));
        System.out.println("  DOUBLE_BRACE_EXPR_END: " + tokenTypeCounts.getOrDefault(TokenType.DOUBLE_BRACE_EXPR_END, 0));
        System.out.println("  STMT_BEGIN: " + tokenTypeCounts.getOrDefault(TokenType.STMT_BEGIN, 0));
        System.out.println("  STMT_END: " + tokenTypeCounts.getOrDefault(TokenType.STMT_END, 0));
        System.out.println("  IDENTIFIER: " + tokenTypeCounts.getOrDefault(TokenType.IDENTIFIER, 0));
        System.out.println("  NUMBER: " + tokenTypeCounts.getOrDefault(TokenType.NUMBER, 0));
        System.out.println("  STRING: " + tokenTypeCounts.getOrDefault(TokenType.STRING, 0));
        System.out.println("  关键字总计: " +
                (tokenTypeCounts.getOrDefault(TokenType.IF, 0) +
                        tokenTypeCounts.getOrDefault(TokenType.FOR, 0) +
                        tokenTypeCounts.getOrDefault(TokenType.SWITCH, 0) +
                        tokenTypeCounts.getOrDefault(TokenType.LET, 0) +
                        tokenTypeCounts.getOrDefault(TokenType.IMPORT, 0)));

        System.out.println("\n========== 验证完成 ==========");
        System.out.println("✓ 所有验证项通过，token_test_simple.etl 词法分析正确！");
        System.out.println("✓ 总共验证了 " + allTokens.size() + " 个 Token");
    }

    // ==================== Mark/Reset 测试 ====================

    @Test
    public void testMarkAndResetToMark() throws IOException {
        // 测试 mark/reset 功能，用于 Lambda 解析时的前瞻
        StringSource source = new StringSource("{a b c}");
        Lexer lexer = new Lexer(source);
        TokenStream tokens = new TokenStream(lexer);

        // 读取第一个 token（表达式开始）
        Token first = tokens.nextToken();
        assertEquals(TokenType.SINGLE_BRACE_EXPR_BEGIN, first.getType());

        // 标记当前位置
        tokens.mark();

        // 读取后续 tokens
        Token second = tokens.nextToken();
        Token third = tokens.nextToken();
        assertEquals(TokenType.IDENTIFIER, second.getType());
        assertEquals("a", second.getValue());
        assertEquals(TokenType.IDENTIFIER, third.getType());
        assertEquals("b", third.getValue());

        // 重置到标记位置
        tokens.resetToMark();

        // 应该回到 'a'
        Token afterReset = tokens.nextToken();
        assertEquals(TokenType.IDENTIFIER, afterReset.getType());
        assertEquals("a", afterReset.getValue());
    }

    @Test
    public void testMarkResetWithPeek() throws IOException {
        // 测试 mark/reset 与 peek 的配合使用
        StringSource source = new StringSource("{( a, b )}");
        Lexer lexer = new Lexer(source);
        TokenStream tokens = new TokenStream(lexer);

        // 读取表达式开始
        tokens.nextToken(); // {

        // 标记位置，然后使用 peek 查看
        tokens.mark();
        Token peek1 = tokens.peek();
        assertEquals(TokenType.LEFT_PAREN, peek1.getType());

        // 读取几个 token
        tokens.nextToken(); // (
        tokens.nextToken(); // a
        tokens.nextToken(); // ,

        // 重置
        tokens.resetToMark();

        // 应该回到 (
        Token afterReset = tokens.peek();
        assertEquals(TokenType.LEFT_PAREN, afterReset.getType());
    }

    @Test
    public void testMarkResetMultipleTimes() throws IOException {
        // 测试多次 mark/reset（只保留最后一个 mark）
        StringSource source = new StringSource("{a b c d}");
        Lexer lexer = new Lexer(source);
        TokenStream tokens = new TokenStream(lexer);

        tokens.nextToken(); // {

        tokens.mark();
        tokens.nextToken(); // a
        tokens.nextToken(); // b

        tokens.mark();  // 新的标记位置
        tokens.nextToken(); // c
        tokens.nextToken(); // d

        tokens.resetToMark();

        // 应该回到 c
        Token afterReset = tokens.nextToken();
        assertEquals("c", afterReset.getValue());
    }

    @Test
    public void testClearMark() throws IOException {
        // 测试 clearMark 功能
        StringSource source = new StringSource("{a b}");
        Lexer lexer = new Lexer(source);
        TokenStream tokens = new TokenStream(lexer);

        tokens.nextToken(); // {

        tokens.mark();
        tokens.clearMark();

        // 清除 mark 后调用 resetToMark 应该抛出异常
        try {
            tokens.resetToMark();
            fail("应该抛出 IllegalStateException");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("No mark set"));
        }
    }

}
