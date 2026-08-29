package org.dromara.easytl.parser;

import org.dromara.easytl.ast.*;
import org.dromara.easytl.exception.TemplateSyntaxException;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 语法解析器，将 Token 流解析为抽象语法树 (AST)
 *
 * @author EasyTL Team
 */
public class Parser {

    private final TokenStream tokens;
    private final String sourceText;
    private Token currentToken;

    /**
     * 构造一个 Parser
     *
     * @param tokens Token 流
     */
    public Parser(TokenStream tokens) {
        this(tokens, null);
    }

    /**
     * 构造一个 Parser
     *
     * @param tokens     Token 流
     * @param sourceText 源码文本（用于错误定位，可为 null）
     */
    public Parser(TokenStream tokens, String sourceText) {
        this.tokens = tokens;
        this.sourceText = sourceText;
        // 预读取第一个 Token
        advance();
    }

    /**
     * 解析模板
     *
     * @return AST 根节点
     */
    public TemplateNode parse() {
        return parseTemplate();
    }

    // ==================== 基础辅助方法 ====================

    private Token current() {
        return currentToken;
    }

    private Token peek() {
        return tokens.peek();
    }
    
    private Token peek(int offset) {
        return tokens.peek(offset);
    }

    private Token advance() {
        Token prev = currentToken;
        currentToken = tokens.nextToken();
        return prev;
    }

    private Token expect(int type) {
        if (check(type)) {
            return advance();
        }

        String expected = TokenType.getSymbol(type);
        Token currentToken = current();
        String actual = getActualTokenDescription(currentToken);

        throw new TemplateSyntaxException(
                "Expected " + expected + ", but got " + actual,
                currentToken.getLine(),
                currentToken.getStartColumn(),
                currentToken.getLine(),
                currentToken.getEndColumn(),
                sourceText
        );
    }

    /**
     * 获取实际 Token 的描述信息
     * 对于 number 和 identifier，显示实际值；对于其他类型，显示类型名称
     */
    private String getActualTokenDescription(Token token) {
        int tokenType = token.getType();

        // 对于数字类型，显示实际的数字值
        if (tokenType == TokenType.NUMBER ||
            tokenType == TokenType.LONG ||
            tokenType == TokenType.BIG_INTEGER ||
            tokenType == TokenType.BIG_DECIMAL) {
            return token.getValue();
        }

        // 对于标识符，显示实际的标识符名称
        if (tokenType == TokenType.IDENTIFIER) {
            return token.getValue();
        }

        // 对于其他类型，显示类型符号
        return TokenType.getSymbol(tokenType);
    }

    private boolean check(int type) {
        return current().getType() == type;
    }
    
    private boolean checkAny(int... types) {
        for (int type : types) {
            if (check(type)) {
                return true;
            }
        }
        return false;
    }

    private void expectBlockEnd() {
        if (check(TokenType.STMT_END) || check(TokenType.RIGHT_BRACE)) {
            advance();
        } else {
            expect(TokenType.RIGHT_BRACE);
        }
    }

    // ==================== 模板解析 ====================

    private TemplateNode parseTemplate() {
        List<ASTNode> children = new ArrayList<>();
        
        while (!check(TokenType.EOF) && !check(TokenType.STMT_CLOSE)) {
            
            if (check(TokenType.TEXT)) {
                children.add(new TextNode(advance().getValue()));
            } else if (checkAny(TokenType.SINGLE_BRACE_EXPR_BEGIN, 
                              TokenType.DOUBLE_BRACE_EXPR_BEGIN, 
                              TokenType.DOLLAR_BRACE_EXPR_BEGIN)) {
                children.add(parseExpressionWrapper());
            } else if (check(TokenType.STMT_BEGIN)) {
                // 检查是否是结束当前块的标记 (else, case, default)
                Token next = peek();
                int nextType = next.getType();
                if (nextType == TokenType.ELSE || 
                    nextType == TokenType.CASE || 
                    nextType == TokenType.DEFAULT) {
                    break;
                }
                ASTNode statementBlock = parseStatementBlock();
                if (statementBlock instanceof TemplateNode) {
                    // 多语句自闭合块：展开为多条顶层语句，与相邻的多个单语句块等价
                    children.addAll(((TemplateNode) statementBlock).getChildren());
                } else {
                    children.add(statementBlock);
                }
            } else {
                // 遇到无法识别的非 EOF Token，通常意味着上层调用会处理（如 RIGHT_BRACE）
                break; 
            }
        }
        
        // 处理空白行：移除 StatementBlock 节点所在行的空白字符
        removeBlankLines(children);
        
        return new TemplateNode(children);
    }
    
    private ExpressionNode parseExpressionWrapper() {
        Token startToken = current();
        int type = startToken.getType();
        advance(); // 消耗 BEGIN Token
        
        ASTNode expression = parseExpression();
        
        // 检测纯整数数字表达式，转换为参数访问
        // 只有纯整数（Integer类型，无后缀）才转换为参数访问
        if (expression instanceof NumberLiteral) {
            NumberLiteral numLiteral = (NumberLiteral) expression;
            Number numValue = numLiteral.getValue();
            // 只转换Integer类型的非负整数（不包括Long、BigInteger等）
            if (numValue instanceof Integer) {
                int intValue = numValue.intValue();
                if (intValue >= 0) {
                    // 转换为参数访问节点，继承 NumberLiteral 的位置信息
                    expression = new ArgumentAccess(intValue, numLiteral.getPosition());
                }
            }
        }
        
        if (type == TokenType.SINGLE_BRACE_EXPR_BEGIN) {
            expect(TokenType.SINGLE_BRACE_EXPR_END);
            return new SingleBraceExpressionNode(expression);
        } else if (type == TokenType.DOUBLE_BRACE_EXPR_BEGIN) {
            expect(TokenType.DOUBLE_BRACE_EXPR_END);
            return new DoubleBraceExpressionNode(expression);
        } else {
            expect(TokenType.DOLLAR_BRACE_EXPR_END);
            return new DollarBraceExpressionNode(expression);
        }
    }

    // ==================== 语句块解析 ====================
    
    private ASTNode parseStatementBlock() {
        expect(TokenType.STMT_BEGIN);

        // 标准语句块（带 {/% xxx } 结束标签）：根据关键字分发
        // 注意：if/for 后紧跟 ( ... ) { 形态时是结构化表达式独立语句
        // （如 {% if (c) { ... } %}、{% for (x in list) { ... } %}），不进入标准语句块解析
        if (check(TokenType.IF) && !isStructuredExpressionAhead()) {
            return parseIfStatement();
        } else if (check(TokenType.FOR) && !isStructuredExpressionAhead()) {
            return parseForStatement();
        } else if (check(TokenType.SWITCH)) {
            return parseSwitchStatement();
        }

        // 自闭合语句块：{% <script_content> %}
        return parseScriptContent();
    }

    /**
     * 解析自闭合语句块的脚本内容（script_content）：一条或多条脚本语句，
     * 语句之间以分号或换行分隔（换行在词法层作为空白被跳过，语句自然衔接），
     * 允许末尾多余的分号，以 %} 结束。
     * 单语句时直接返回语句节点，多语句时返回 TemplateNode 包装。
     *
     * @return 语句节点或多语句包装节点
     */
    private ASTNode parseScriptContent() {
        List<ASTNode> statements = new ArrayList<>();
        // 至少一条语句；空块 {% %} 在 parseScriptStatement 处报 "Unexpected token: %}"
        statements.add(parseScriptStatement());
        skipSemicolons();
        while (!check(TokenType.STMT_END)) {
            statements.add(parseScriptStatement());
            skipSemicolons();
        }
        expect(TokenType.STMT_END);

        if (statements.size() == 1) {
            return statements.get(0);
        }
        return new TemplateNode(statements);
    }

    /**
     * 解析单条脚本语句（script_statement）：
     * let 声明、extends、import、export、普通表达式语句或结构化表达式（if/for）独立语句
     *
     * @return 语句节点
     */
    private ASTNode parseScriptStatement() {
        if (check(TokenType.LET)) {
            return parseLetStatement();
        } else if (check(TokenType.EXTENDS)) {
            return parseExtendsStatement();
        } else if (check(TokenType.IMPORT)) {
            return parseImportStatement();
        } else if (check(TokenType.EXPORT)) {
            return parseExportStatement();
        }
        // 普通表达式或结构化表达式（if/for 由 parsePrimary 分发）
        return parseExpression();
    }

    /**
     * 消耗语句之间的分号分隔符（允许连续多个）
     */
    private void skipSemicolons() {
        while (check(TokenType.SEMICOLON)) {
            advance();
        }
    }

    /**
     * 判断当前 if/for 关键字是否为结构化表达式形态（if ( ... ) { 或 for ( ... ) {），
     * 用于区分 {% if (cond) { ... } %}（结构化表达式独立语句）
     * 与 {% if (cond) %} ... {/% if }（标准语句块，条件带括号）。
     * 从关键字后的左括号扫描到匹配的右括号，若其后紧跟 { 则为结构化表达式。
     *
     * @return 如果是结构化表达式形态返回 true
     */
    private boolean isStructuredExpressionAhead() {
        if (peek().getType() != TokenType.LEFT_PAREN) {
            return false;
        }
        int depth = 0;
        int offset = 0;
        while (true) {
            Token token = peek(offset);
            int type = token.getType();
            if (type == TokenType.EOF) {
                // 超出前瞻范围或未闭合，按标准语句块处理（保持原有行为）
                return false;
            }
            if (type == TokenType.LEFT_PAREN) {
                depth++;
            } else if (type == TokenType.RIGHT_PAREN) {
                depth--;
                if (depth == 0) {
                    return peek(offset + 1).getType() == TokenType.LEFT_BRACE;
                }
            }
            offset++;
        }
    }
    
    private ASTNode parseIfStatement() {
        expect(TokenType.IF);
        ASTNode condition = parseExpression();
        expectBlockEnd(); // 标准块开始标记结束
        
        TemplateNode thenBlock = parseTemplate();
        
        List<IfStatement.ElseIfClause> elseIfs = new ArrayList<>();
        TemplateNode elseBlock = null;
        
        while (check(TokenType.STMT_BEGIN)) {
             Token next = peek();
             if (next.getType() == TokenType.ELSE) {
                 advance(); // STMT_BEGIN
                 advance(); // ELSE
                 
                 if (check(TokenType.IF)) {
                     // Else If
                     advance(); // IF
                     ASTNode elseIfCond = parseExpression();
                     expectBlockEnd();
                     TemplateNode elseIfBody = parseTemplate();
                     elseIfs.add(new IfStatement.ElseIfClause(elseIfCond, elseIfBody.getChildren()));
                 } else {
                     // Else
                     expectBlockEnd();
                     elseBlock = parseTemplate();
                     break; // else 后不能再有 else if
                 }
             } else {
                 break;
             }
        }
        
        expect(TokenType.STMT_CLOSE);
        expect(TokenType.IF);
        expect(TokenType.RIGHT_BRACE);
        
        return new IfStatement(condition, thenBlock.getChildren(), elseIfs, elseBlock != null ? elseBlock.getChildren() : null);
    }
    
    private ASTNode parseForStatement() {
        expect(TokenType.FOR);

        // 检查是否是 C 风格 for 循环
        if (check(TokenType.LET)) {
            return parseClassicForStatement();
        }

        // for item in items 或 for index, item in items
        Token first = expect(TokenType.IDENTIFIER);
        String itemVar = first.getValue();
        String indexVar = null;
        
        if (check(TokenType.COMMA)) {
            advance();
            Token second = expect(TokenType.IDENTIFIER);
            indexVar = itemVar; // 第一个是索引
            itemVar = second.getValue(); // 第二个是值
        }
        
        expect(TokenType.IN);
        ASTNode collection = parseExpression();
        expectBlockEnd();
        
        TemplateNode body = parseTemplate();
        
        expect(TokenType.STMT_CLOSE);
        expect(TokenType.FOR);
        expect(TokenType.RIGHT_BRACE);
        
        return new ForStatement(indexVar, itemVar, collection, body.getChildren());
    }

    private ASTNode parseClassicForStatement() {
        Token letToken = expect(TokenType.LET);
        Token nameToken = expect(TokenType.IDENTIFIER);
        expect(TokenType.ASSIGN);
        ASTNode initValue = parseExpression();

        LetStatement init = new LetStatement(nameToken.getValue(), initValue, letToken);

        expect(TokenType.SEMICOLON);

        // 条件（可为空）
        ASTNode condition = null;
        if (!check(TokenType.SEMICOLON)) {
            condition = parseExpression();
        }

        expect(TokenType.SEMICOLON);

        // 更新表达式（可为空）
        ASTNode update = null;
        if (!check(TokenType.RIGHT_BRACE)) {
            update = parseExpression();
        }

        expectBlockEnd();

        TemplateNode body = parseTemplate();

        expect(TokenType.STMT_CLOSE);
        expect(TokenType.FOR);
        expect(TokenType.RIGHT_BRACE);

        return new ClassicForStatement(init, condition, update, body.getChildren());
    }

    private ASTNode parseSwitchStatement() {
        expect(TokenType.SWITCH);
        ASTNode expression = parseExpression();
        expectBlockEnd();
        
        List<CaseClause> cases = new ArrayList<>();
        TemplateNode defaultBlock = null;
        
        while (!check(TokenType.STMT_CLOSE) && !check(TokenType.EOF)) {
             if (check(TokenType.STMT_BEGIN)) {
                 Token next = peek();
                 if (next.getType() == TokenType.CASE) {
                     advance(); // STMT_BEGIN
                     advance(); // CASE
                     ASTNode caseValue = parseExpression();
                     expect(TokenType.RIGHT_BRACE); // case语句必须以 } 结束
                     TemplateNode caseBody = parseTemplate();
                     cases.add(new CaseClause(caseValue, caseBody.getChildren()));
                 } else if (next.getType() == TokenType.DEFAULT) {
                     advance(); // STMT_BEGIN
                     advance(); // DEFAULT
                     expect(TokenType.RIGHT_BRACE); // default语句必须以 } 结束
                     defaultBlock = parseTemplate();
                 } else {
                     // 遇到其他语句块，停止解析，让外层处理
                     break;
                 }
             } else if (check(TokenType.TEXT)) {
                 // 跳过switch内部的空白文本（换行符、空格等）
                 advance();
             } else {
                 // 遇到其他非预期的 token，停止解析
                 break;
             }
        }
        
        expect(TokenType.STMT_CLOSE);
        expect(TokenType.SWITCH);
        expect(TokenType.RIGHT_BRACE);
        
        return new SwitchStatement(expression, cases, defaultBlock != null ? defaultBlock.getChildren() : null);
    }
    
    private ASTNode parseLetStatement() {
        expect(TokenType.LET);
        Token name = expect(TokenType.IDENTIFIER);
        expect(TokenType.ASSIGN);
        ASTNode value = parseExpression();

        // 赋值约束检查：如果 value 是 IfExpression，必须有 else 分支
        if (value instanceof IfExpression) {
            IfExpression ifExpr = (IfExpression) value;
            if (!ifExpr.hasElse()) {
                throw new TemplateSyntaxException("If expression in let assignment must include else branch",
                    value.getStartLine(), value.getStartColumn(),
                    value.getEndLine(), value.getEndColumn(), sourceText);
            }
        }

        return new LetStatement(name.getValue(), value);
    }
    
    private ASTNode parseExtendsStatement() {
        expect(TokenType.EXTENDS);
        Token path = expect(TokenType.STRING);
        return new ExtendsStatement(path.getValue());
    }

    private ASTNode parseImportStatement() {
        Token importToken = current();
        expect(TokenType.IMPORT);

        if (check(TokenType.STRING)) {
            // 模板文件导入：{% import 'path/to/template.etl' as alias %}
            Token pathToken = advance();
            String path = pathToken.getValue();
            expect(TokenType.AS);
            Token aliasToken = expect(TokenType.IDENTIFIER);

            // keywordPosition: 从 import 到 alias 结束
            Position keywordPos = new Position(
                importToken.getLine(), importToken.getStartColumn(),
                aliasToken.getLine(), aliasToken.getEndColumn()
            );

            return new ImportTemplateStatement(path, aliasToken.getValue(), keywordPos);
        } else {
            // Java 类导入：{% import java.util.List %} 或 {% import java.util.List as ArrayList %}
            Token firstIdToken = expect(TokenType.IDENTIFIER);
            StringBuilder className = new StringBuilder();
            className.append(firstIdToken.getValue());

            Token lastToken = firstIdToken;

            while (check(TokenType.DOT)) {
                advance();
                Token idToken = expect(TokenType.IDENTIFIER);
                className.append(".");
                className.append(idToken.getValue());
                lastToken = idToken;
            }

            Token aliasToken = null;
            if (check(TokenType.AS)) {
                advance();
                aliasToken = expect(TokenType.IDENTIFIER);
                lastToken = aliasToken;
            }

            // keywordPosition: 从 import 到类名/别名结束
            Position keywordPos = new Position(
                importToken.getLine(), importToken.getStartColumn(),
                lastToken.getLine(), lastToken.getEndColumn()
            );

            return new ImportStatement(className.toString(), aliasToken != null ? aliasToken.getValue() : null, keywordPos);
        }
    }

    private ASTNode parseExportStatement() {
        Token exportToken = current();
        expect(TokenType.EXPORT);

        if (check(TokenType.LET)) {
            // {% export let x = value %}
            Token letToken = current();
            advance(); // 消耗 LET
            Token nameToken = expect(TokenType.IDENTIFIER);
            expect(TokenType.ASSIGN);
            ASTNode value = parseExpression();

            // keywordPosition: 从 export 到 value 结束（不含 {% 和 %})
            // 使用 value 表达式的结束位置，而不是 STMT_END 的位置
            Position keywordPos = new Position(
                exportToken.getLine(), exportToken.getStartColumn(),
                value.getEndLine(), value.getEndColumn()
            );

            LetStatement letStmt = new LetStatement(nameToken.getValue(), value, letToken);
            return new ExportStatement(nameToken.getValue(), letStmt, keywordPos);
        } else {
            // {% export x %}
            Token nameToken = expect(TokenType.IDENTIFIER);

            // keywordPosition: export x 的位置
            Position keywordPos = new Position(
                exportToken.getLine(), exportToken.getStartColumn(),
                nameToken.getLine(), nameToken.getEndColumn()
            );

            return new ExportStatement(nameToken.getValue(), null, keywordPos);
        }
    }

    // ==================== 结构化表达式解析 (Expression Context) ====================

    private ASTNode parseIfExpression() {
        Token startToken = current(); // capture IF token before advance
        advance(); // IF
        expect(TokenType.LEFT_PAREN);
        ASTNode condition = parseExpression();
        expect(TokenType.RIGHT_PAREN);

        Object[] thenResult = parseBlockStatementWithEndToken();
        @SuppressWarnings("unchecked")
        List<ASTNode> thenBlock = (List<ASTNode>) thenResult[0];
        Token endToken = (Token) thenResult[1];

        List<IfExpression.ElseIfClause> elseIfs = new ArrayList<>();
        List<ASTNode> elseBlock = null;

        while (check(TokenType.ELSE)) {
            advance();
            if (check(TokenType.IF)) {
                advance();
                expect(TokenType.LEFT_PAREN);
                ASTNode elseIfCond = parseExpression();
                expect(TokenType.RIGHT_PAREN);
                Object[] elseIfResult = parseBlockStatementWithEndToken();
                @SuppressWarnings("unchecked")
                List<ASTNode> elseIfBody = (List<ASTNode>) elseIfResult[0];
                endToken = (Token) elseIfResult[1];
                elseIfs.add(new IfExpression.ElseIfClause(elseIfCond, elseIfBody));
            } else {
                Object[] elseResult = parseBlockStatementWithEndToken();
                @SuppressWarnings("unchecked")
                List<ASTNode> elseBody = (List<ASTNode>) elseResult[0];
                endToken = (Token) elseResult[1];
                elseBlock = elseBody;
                break;
            }
        }

        return new IfExpression(condition, thenBlock, elseIfs, elseBlock, startToken, endToken);
    }
    
    private ASTNode parseForExpression() {
        advance(); // FOR
        expect(TokenType.LEFT_PAREN);
        
        Token first = expect(TokenType.IDENTIFIER);
        String itemVar = first.getValue();
        String indexVar = null;
        
        if (check(TokenType.COMMA)) {
            advance();
            Token second = expect(TokenType.IDENTIFIER);
            indexVar = itemVar;
            itemVar = second.getValue();
        }
        
        expect(TokenType.IN);
        ASTNode collection = parseExpression();
        expect(TokenType.RIGHT_PAREN);
        
        List<ASTNode> body = parseBlockStatement();
        
        return new ForExpression(indexVar, itemVar, collection, body);
    }
    
    private List<ASTNode> parseBlockStatement() {
        expect(TokenType.LEFT_BRACE);
        List<ASTNode> stmts = new ArrayList<>();
        while (!check(TokenType.RIGHT_BRACE) && !check(TokenType.EOF)) {
            // Handle return statements (for Lambda blocks)
            if (check(TokenType.RETURN)) {
                stmts.add(parseReturnStatement());
            } else if (check(TokenType.LET)) {
                // Let statement in block
                stmts.add(parseLetStatementInLambdaBlock());
            } else {
                stmts.add(parseExpression());
            }

            if (check(TokenType.SEMICOLON)) {
                advance();
            }
        }
        expect(TokenType.RIGHT_BRACE);
        return stmts;
    }

    /**
     * Parse block statement and return both the statements and the end brace token.
     * Used for capturing position information in expressions like if/for.
     *
     * @return Object[] { List<ASTNode> statements, Token endBrace }
     */
    private Object[] parseBlockStatementWithEndToken() {
        expect(TokenType.LEFT_BRACE);
        List<ASTNode> stmts = new ArrayList<>();
        while (!check(TokenType.RIGHT_BRACE) && !check(TokenType.EOF)) {
            if (check(TokenType.RETURN)) {
                stmts.add(parseReturnStatement());
            } else if (check(TokenType.LET)) {
                stmts.add(parseLetStatementInLambdaBlock());
            } else {
                stmts.add(parseExpression());
            }

            if (check(TokenType.SEMICOLON)) {
                advance();
            }
        }
        Token endBrace = expect(TokenType.RIGHT_BRACE);
        return new Object[] { stmts, endBrace };
    }

    /**
     * Parse source code for Eval.eval() - parses expressions/statements without template wrapper.
     * <p>
     * This method is similar to parseBlockStatement() but does not require
     * enclosing braces. It stops at EOF.
     * </p>
     *
     * @return list of parsed AST nodes (statements/expressions)
     */
    public List<ASTNode> parseEvalSource() {
        List<ASTNode> stmts = new ArrayList<>();
        while (!check(TokenType.EOF)) {
            // Handle return statements
            if (check(TokenType.RETURN)) {
                stmts.add(parseReturnStatement());
            } else if (check(TokenType.LET)) {
                // Handle let statements
                stmts.add(parseLetStatementInLambdaBlock());
            } else {
                stmts.add(parseExpression());
            }

            // Consume optional semicolon
            if (check(TokenType.SEMICOLON)) {
                advance();
            }
        }
        return stmts;
    }

    /**
     * 解析 URL 源码（不需要 url(...) 包装）
     *
     * <p>直接解析 URL 内容，返回 UrlLiteral AST 节点。
     * 此方法用于 Eval.evalAsURL()，使错误位置指向原始源码。
     * </p>
     *
     * @return UrlLiteral AST 节点
     */
    public UrlLiteral parseEvalURLSource() {
        List<ASTNode> children = new ArrayList<>();

        // 记录起始位置（用于 UrlLiteral 的 Position）
        int startLine = current() != null ? current().getLine() : 1;
        int startColumn = current() != null ? current().getStartColumn() : 1;

        while (!check(TokenType.EOF)) {
            if (check(TokenType.SINGLE_BRACE_EXPR_BEGIN)) {
                children.add(parseExpressionWrapper());
            } else if (check(TokenType.DOUBLE_BRACE_EXPR_BEGIN)) {
                children.add(parseExpressionWrapper());
            } else if (check(TokenType.DOLLAR_BRACE_EXPR_BEGIN)) {
                children.add(parseExpressionWrapper());
            } else if (check(TokenType.STMT_BEGIN)) {
                children.add(parseStatementBlock());
            } else if (check(TokenType.TEXT)) {
                children.add(new TextNode(advance().getValue()));
            } else {
                // URL 内容解析结束（EOF）
                break;
            }
        }

        // 计算结束位置
        int endLine = 1;
        int endColumn = 1;
        if (!children.isEmpty()) {
            ASTNode last = children.get(children.size() - 1);
            endLine = last.getEndLine();
            endColumn = last.getEndColumn();
        }

        Position position = new Position(startLine, startColumn, endLine, endColumn);
        return new UrlLiteral(children, position);
    }

    // ==================== 表达式解析 (递归下降) ====================

    /**
     * 解析表达式入口
     */
    public ASTNode parseExpression() {
        return parseAssignmentExpression();
    }

    /**
     * 解析复合赋值表达式 (+=, -=, *=, /=, %=, &=, |=, ^=, <<=, >>=, >>>=)
     * 优先级最低（0），右结合
     */
    private ASTNode parseAssignmentExpression() {
        ASTNode expr = parseElvis();

        if (checkAny(TokenType.PLUS_ASSIGN, TokenType.MINUS_ASSIGN,
                     TokenType.MULTIPLY_ASSIGN, TokenType.DIVIDE_ASSIGN, TokenType.MODULO_ASSIGN,
                     TokenType.BIT_AND_ASSIGN, TokenType.BIT_OR_ASSIGN, TokenType.BIT_XOR_ASSIGN,
                     TokenType.LEFT_SHIFT_ASSIGN, TokenType.RIGHT_SHIFT_ASSIGN,
                     TokenType.UNSIGNED_RIGHT_SHIFT_ASSIGN)) {
            Token opToken = advance();
            ASTNode right = parseAssignmentExpression(); // 右结合递归

            validateAssignable(expr, opToken);

            CompoundOperator op = getCompoundOperator(opToken.getType());
            Position pos = new Position(expr.getStartLine(), expr.getStartColumn(),
                                       right.getEndLine(), right.getEndColumn());
            return new CompoundAssignmentExpression(expr, op, right, pos);
        }

        return expr;
    }

    private CompoundOperator getCompoundOperator(int tokenType) {
        switch (tokenType) {
            case TokenType.PLUS_ASSIGN: return CompoundOperator.PLUS_ASSIGN;
            case TokenType.MINUS_ASSIGN: return CompoundOperator.MINUS_ASSIGN;
            case TokenType.MULTIPLY_ASSIGN: return CompoundOperator.MULTIPLY_ASSIGN;
            case TokenType.DIVIDE_ASSIGN: return CompoundOperator.DIVIDE_ASSIGN;
            case TokenType.MODULO_ASSIGN: return CompoundOperator.MODULO_ASSIGN;
            case TokenType.BIT_AND_ASSIGN: return CompoundOperator.BIT_AND_ASSIGN;
            case TokenType.BIT_OR_ASSIGN: return CompoundOperator.BIT_OR_ASSIGN;
            case TokenType.BIT_XOR_ASSIGN: return CompoundOperator.BIT_XOR_ASSIGN;
            case TokenType.LEFT_SHIFT_ASSIGN: return CompoundOperator.LEFT_SHIFT_ASSIGN;
            case TokenType.RIGHT_SHIFT_ASSIGN: return CompoundOperator.RIGHT_SHIFT_ASSIGN;
            case TokenType.UNSIGNED_RIGHT_SHIFT_ASSIGN: return CompoundOperator.UNSIGNED_RIGHT_SHIFT_ASSIGN;
            default: throw new TemplateSyntaxException("Unknown compound assignment operator", 0, 0, 0, 0, sourceText);
        }
    }

    private void validateAssignable(ASTNode target, Token opToken) {
        if (!(target instanceof Identifier) &&
            !(target instanceof MemberAccess) &&
            !(target instanceof IndexAccess)) {
            throw new TemplateSyntaxException(
                "Invalid assignment target",
                target.getStartLine(), target.getStartColumn(),
                target.getEndLine(), target.getEndColumn(), sourceText);
        }
    }

    /**
     * 解析 Elvis 表达式 (??)
     */
    private ASTNode parseElvis() {
        ASTNode expr = parseTernary();

        while (check(TokenType.ELVIS)) {
            advance(); // 消耗 ??
            ASTNode right = parseTernary();
            Position pos = new Position(expr.getStartLine(), expr.getStartColumn(),
                                       right.getEndLine(), right.getEndColumn());
            expr = new ElvisExpression(expr, right, pos);
        }

        return expr;
    }

    /**
     * 解析三元表达式 (? :)
     */
    private ASTNode parseTernary() {
        ASTNode expr = parseLogicalOr();

        if (check(TokenType.QUESTION)) {
            advance(); // 消耗 ?
            ASTNode trueExpr = parseExpression(); // 递归调用 parseExpression 允许嵌套
            expect(TokenType.COLON);
            ASTNode falseExpr = parseTernary(); // 右结合
            Position pos = new Position(expr.getStartLine(), expr.getStartColumn(),
                                       falseExpr.getEndLine(), falseExpr.getEndColumn());
            expr = new TernaryExpression(expr, trueExpr, falseExpr, pos);
        }

        return expr;
    }

    /**
     * 解析逻辑或 (||)
     */
    private ASTNode parseLogicalOr() {
        ASTNode expr = parseLogicalAnd();

        while (check(TokenType.OR)) {
            advance();
            ASTNode right = parseLogicalAnd();
            Position pos = new Position(expr.getStartLine(), expr.getStartColumn(),
                                       right.getEndLine(), right.getEndColumn());
            expr = new BinaryExpression(expr, BinaryExpression.Operator.OR, right, pos);
        }

        return expr;
    }

    /**
     * 解析逻辑与 (&&)
     */
    private ASTNode parseLogicalAnd() {
        ASTNode expr = parseEquality();

        while (check(TokenType.AND)) {
            advance();
            ASTNode right = parseEquality();
            Position pos = new Position(expr.getStartLine(), expr.getStartColumn(),
                                       right.getEndLine(), right.getEndColumn());
            expr = new BinaryExpression(expr, BinaryExpression.Operator.AND, right, pos);
        }

        return expr;
    }

    /**
     * 解析相等性 (==, !=, =~, !=~, in, !in)
     */
    private ASTNode parseEquality() {
        ASTNode expr = parseRelational();

        while (checkAny(TokenType.EQ, TokenType.NEQ,
                       TokenType.REGEX_MATCH, TokenType.REGEX_NOT_MATCH,
                       TokenType.IN, TokenType.NOT_IN,
                       TokenType.SUBSETOF, TokenType.ANYOF, TokenType.NONEOF)) {
            Token opToken = advance();
            ASTNode right = parseRelational();
            BinaryExpression.Operator op = getBinaryOperator(opToken.getType());
            Position pos = new Position(expr.getStartLine(), expr.getStartColumn(),
                                       right.getEndLine(), right.getEndColumn());
            expr = new BinaryExpression(expr, op, right, pos);
        }

        return expr;
    }

    /**
     * 解析关系运算 (<, <=, >, >=)
     */
    private ASTNode parseRelational() {
        ASTNode expr = parseRange();

        while (checkAny(TokenType.LT, TokenType.LTE, TokenType.GT, TokenType.GTE)) {
            Token opToken = advance();
            ASTNode right = parseRange();
            BinaryExpression.Operator op = getBinaryOperator(opToken.getType());
            Position pos = new Position(expr.getStartLine(), expr.getStartColumn(),
                                       right.getEndLine(), right.getEndColumn());
            expr = new BinaryExpression(expr, op, right, pos);
        }

        return expr;
    }

    /**
     * 解析区间表达式 (.., ..<, >.., >..<)
     */
    private ASTNode parseRange() {
        ASTNode expr = parseAdditive();

        if (checkAny(TokenType.RANGE_CLOSED, TokenType.RANGE_OPEN,
                     TokenType.RANGE_LEFT_CLOSED_RIGHT_OPEN, TokenType.RANGE_LEFT_OPEN_RIGHT_CLOSED)) {
            Token opToken = advance();
            ASTNode right = parseAdditive();
            RangeLiteral.RangeType type = getRangeType(opToken.getType());
            Position pos = new Position(expr.getStartLine(), expr.getStartColumn(),
                                       right.getEndLine(), right.getEndColumn());
            return new RangeLiteral(expr, right, type, pos);
        }

        return expr;
    }

    /**
     * 解析加减法 (+, -)
     */
    private ASTNode parseAdditive() {
        ASTNode expr = parseMultiplicative();

        while (checkAny(TokenType.PLUS, TokenType.MINUS)) {
            Token opToken = advance();
            ASTNode right = parseMultiplicative();
            BinaryExpression.Operator op = getBinaryOperator(opToken.getType());
            Position pos = new Position(expr.getStartLine(), expr.getStartColumn(),
                                       right.getEndLine(), right.getEndColumn());
            expr = new BinaryExpression(expr, op, right, pos);
        }

        return expr;
    }

    /**
     * 解析乘除模 (*, /, %)
     */
    private ASTNode parseMultiplicative() {
        ASTNode expr = parseUnary();

        while (checkAny(TokenType.MULTIPLY, TokenType.DIVIDE, TokenType.MODULO)) {
            Token opToken = advance();
            ASTNode right = parseUnary();
            BinaryExpression.Operator op = getBinaryOperator(opToken.getType());
            Position pos = new Position(expr.getStartLine(), expr.getStartColumn(),
                                       right.getEndLine(), right.getEndColumn());
            expr = new BinaryExpression(expr, op, right, pos);
        }

        return expr;
    }

    /**
     * 解析一元运算 (!, -, +) 和类型转换
     */
    private ASTNode parseUnary() {
        // 检查是否是类型转换
        if (isCastExpression()) {
            return parseCastExpression();
        }

        // 前缀自增/自减 ++x, --x
        if (checkAny(TokenType.INCREMENT, TokenType.DECREMENT)) {
            Token opToken = advance();
            ASTNode operand = parseUnary();
            validateAssignable(operand, opToken);
            boolean isIncrement = opToken.getType() == TokenType.INCREMENT;
            Position pos = new Position(opToken.getLine(), opToken.getStartColumn(),
                                       operand.getEndLine(), operand.getEndColumn());
            return new IncrementExpression(operand, true, isIncrement, pos);
        }

        if (checkAny(TokenType.NOT, TokenType.MINUS, TokenType.PLUS)) {
            Token opToken = advance();
            ASTNode operand = parseUnary();
            UnaryExpression.Operator op = getUnaryOperator(opToken.getType());
            Position pos = new Position(opToken.getLine(), opToken.getStartColumn(),
                                       operand.getEndLine(), operand.getEndColumn());
            return new UnaryExpression(op, operand, pos);
        }

        return parsePostfix();
    }

    /**
     * 解析后缀表达式 (., ?., [], ())
     */
    private ASTNode parsePostfix() {
        ASTNode expr = parsePrimary();

        while (true) {
            if (check(TokenType.DOT)) {
                Token dotToken = advance();  // consume DOT token
                if (check(TokenType.MULTIPLY)) {
                    // JSONPath 点通配 .*（与 [*] 等价）
                    Token star = advance();
                    Position pos = new Position(dotToken.getLine(), dotToken.getStartColumn(),
                                               star.getLine(), star.getEndColumn());
                    expr = new WildcardAccess(expr, pos);
                    continue;
                }
                Token name = expect(TokenType.IDENTIFIER);
                // 整个表达式的位置（使用 Token 的 endColumn，它是 exclusive）
                Position fullPos = new Position(expr.getStartLine(), expr.getStartColumn(),
                                           name.getLine(), name.getEndColumn());
                // 属性名的位置（仅 name 部分）
                Position memberNamePos = new Position(name.getLine(), name.getStartColumn(),
                                           name.getLine(), name.getEndColumn());
                expr = new MemberAccess(expr, name.getValue(), false, fullPos, memberNamePos);
            } else if (check(TokenType.SAFE_NAV)) {
                advance();  // consume SAFE_NAV token
                Token name = expect(TokenType.IDENTIFIER);
                // 整个表达式的位置
                Position fullPos = new Position(expr.getStartLine(), expr.getStartColumn(),
                                           name.getLine(), name.getEndColumn());
                // 属性名的位置
                Position memberNamePos = new Position(name.getLine(), name.getStartColumn(),
                                           name.getLine(), name.getEndColumn());
                expr = new MemberAccess(expr, name.getValue(), true, fullPos, memberNamePos);
            } else if (check(TokenType.LEFT_BRACKET)) {
                Token leftBracket = advance();
                if (check(TokenType.MULTIPLY) && peek().getType() == TokenType.RIGHT_BRACKET) {
                    // JSONPath 通配 [*]（与 .* 等价）
                    advance(); // 消耗 *
                    Token rightBracket = expect(TokenType.RIGHT_BRACKET);
                    // 位置只覆盖 [*] 部分
                    Position pos = new Position(leftBracket.getLine(), leftBracket.getStartColumn(),
                                               rightBracket.getLine(), rightBracket.getStartColumn());
                    expr = new WildcardAccess(expr, pos);
                } else if (check(TokenType.QUESTION) && peek().getType() == TokenType.LEFT_PAREN) {
                    // JSONPath 过滤 [?(expr)]
                    advance(); // 消耗 ?
                    advance(); // 消耗 (
                    ASTNode predicate = parseExpression();
                    expect(TokenType.RIGHT_PAREN);
                    Token rightBracket = expect(TokenType.RIGHT_BRACKET);
                    Position pos = new Position(leftBracket.getLine(), leftBracket.getStartColumn(),
                                               rightBracket.getLine(), rightBracket.getStartColumn());
                    expr = new FilterAccess(expr, predicate, pos);
                } else {
                    JsonPathSelector first = parseBracketSelector();
                    if (check(TokenType.COMMA)) {
                        // JSONPath union 多选 [selector, selector, ...]
                        List<JsonPathSelector> selectors = new ArrayList<>();
                        selectors.add(first);
                        while (check(TokenType.COMMA)) {
                            advance(); // 消耗 ,
                            selectors.add(parseBracketSelector());
                        }
                        Token rightBracket = expect(TokenType.RIGHT_BRACKET);
                        Position pos = new Position(leftBracket.getLine(), leftBracket.getStartColumn(),
                                                   rightBracket.getLine(), rightBracket.getStartColumn());
                        expr = new UnionAccess(expr, selectors, pos);
                    } else {
                        Token rightBracket = expect(TokenType.RIGHT_BRACKET);
                        // 位置只覆盖 [...] 部分
                        Position pos = new Position(leftBracket.getLine(), leftBracket.getStartColumn(),
                                                   rightBracket.getLine(), rightBracket.getStartColumn());
                        expr = toAccessNode(expr, first, pos);
                    }
                }
            } else if (check(TokenType.LEFT_PAREN)) {
                // 方法调用
                if (expr instanceof Identifier) {
                    String methodName = ((Identifier) expr).getName();
                    ArgumentListResult result = parseArgumentListWithPosition();
                    Position pos = new Position(expr.getStartLine(), expr.getStartColumn(),
                                               result.rightParen.getLine(), result.rightParen.getEndColumn());
                    expr = new MethodCall(null, methodName, result.args, false, pos);
                } else if (expr instanceof MemberAccess) {
                    MemberAccess access = (MemberAccess) expr;
                    ArgumentListResult result = parseArgumentListWithPosition();
                    Position pos = new Position(access.getStartLine(), access.getStartColumn(),
                                               result.rightParen.getLine(), result.rightParen.getEndColumn());
                    // 使用成员名位置作为方法名位置
                    Position methodNamePos = access.getMemberNamePosition() != null
                        ? access.getMemberNamePosition()
                        : access.getPosition();
                    expr = new MethodCall(access.getObject(), access.getMemberName(), result.args, access.isSafeNavigation(), pos, methodNamePos);
                } else {
                     throw new TemplateSyntaxException("Invalid method call target", current().getLine(), current().getStartColumn(), current().getLine(), current().getEndColumn(), sourceText);
                }
            } else if (check(TokenType.LEFT_BRACE)) {
                // ObjectConstruction 语法
                // 检查 expr 是否是 Identifier 或 MethodCall
                if (isObjectConstructionTarget(expr)) {
                    ASTNode hashOrBlock = parseHashLiteral();
                    if (hashOrBlock instanceof HashLiteral) {
                        HashLiteral propertiesBlock = (HashLiteral) hashOrBlock;
                        Position pos = new Position(expr.getStartLine(), expr.getStartColumn(),
                                                   propertiesBlock.getEndLine(), propertiesBlock.getEndColumn());
                        expr = new ObjectConstruction(expr, propertiesBlock, pos);
                    } else {
                        break;
                    }
                } else {
                    break;
                }
            } else if (check(TokenType.RANGE_CLOSED) && isRecursiveDescentAhead(expr)) {
                // JSONPath 递归下降（二义消解规则见 isRecursiveDescentAhead；
                // 否则回落给 parseRange 按区间处理，保持 a..b、1..10 语义不变）
                advance(); // 消耗 ..
                if (check(TokenType.IDENTIFIER)) {
                    // ..name
                    Token name = advance(); // 消耗成员名
                    Position pos = new Position(expr.getStartLine(), expr.getStartColumn(),
                                               name.getLine(), name.getEndColumn());
                    expr = new RecursiveDescentAccess(expr, name.getValue(), pos);
                } else if (check(TokenType.MULTIPLY)) {
                    // ..*（全部后代节点）
                    Token star = advance(); // 消耗 *
                    Position pos = new Position(expr.getStartLine(), expr.getStartColumn(),
                                               star.getLine(), star.getEndColumn());
                    List<JsonPathSelector> selectors = new ArrayList<>();
                    selectors.add(new JsonPathSelector.WildcardSelector());
                    expr = new RecursiveDescentAccess(expr, selectors, pos);
                } else {
                    // ..[selector(, selector)*]
                    advance(); // 消耗 [
                    List<JsonPathSelector> selectors = new ArrayList<>();
                    selectors.add(parseBracketSelector());
                    while (check(TokenType.COMMA)) {
                        advance(); // 消耗 ,
                        selectors.add(parseBracketSelector());
                    }
                    Token rightBracket = expect(TokenType.RIGHT_BRACKET);
                    Position pos = new Position(expr.getStartLine(), expr.getStartColumn(),
                                               rightBracket.getLine(), rightBracket.getStartColumn());
                    expr = new RecursiveDescentAccess(expr, selectors, pos);
                }
            } else if (check(TokenType.INCREMENT)) {
                Token opToken = advance();
                validateAssignable(expr, opToken);
                Position pos = new Position(expr.getStartLine(), expr.getStartColumn(),
                                           opToken.getLine(), opToken.getEndColumn());
                expr = new IncrementExpression(expr, false, true, pos);
            } else if (check(TokenType.DECREMENT)) {
                Token opToken = advance();
                validateAssignable(expr, opToken);
                Position pos = new Position(expr.getStartLine(), expr.getStartColumn(),
                                           opToken.getLine(), opToken.getEndColumn());
                expr = new IncrementExpression(expr, false, false, pos);
            } else {
                break;
            }
        }

        return expr;
    }

    /**
     * 检查表达式是否可能是 ObjectConstruction 的目标
     */
    private boolean isObjectConstructionTarget(ASTNode expr) {
        // Identifier (如 Person) 或 MethodCall (如 Person('Peter'))
        return expr instanceof Identifier || expr instanceof MethodCall;
    }

    /**
     * 判断 {@code ..} 是否应按 JSONPath 递归下降解析（调用时当前 token 为 {@code ..}）：
     * <ul>
     * <li>{@code ..name}：仅当左值为路径类节点（保持 {@code a..b}、{@code 1..10} 区间语义）</li>
     * <li>{@code ..*} / {@code ..[...]}：左值为路径类节点或标识符即可
     * （这两种形态本就不构成合法区间，无歧义；标识符左值使 {@code store..*} 可用）</li>
     * </ul>
     *
     * @param expr {@code ..} 左侧已解析的表达式
     * @return 按递归下降解析返回 true，否则回落区间解析
     */
    private boolean isRecursiveDescentAhead(ASTNode expr) {
        int next = peek().getType();
        if (next == TokenType.IDENTIFIER) {
            return isPathLikeNode(expr);
        }
        if (next == TokenType.MULTIPLY || next == TokenType.LEFT_BRACKET) {
            return isPathLikeNode(expr) || expr instanceof Identifier;
        }
        return false;
    }

    /**
     * 判断节点是否为路径类节点（用于 {@code ..} 的递归下降/区间二义消解）
     * 仅当 {@code ..} 左侧为路径类节点时才可能解析为 JSONPath 递归下降
     */
    private boolean isPathLikeNode(ASTNode expr) {
        return expr instanceof MemberAccess
            || expr instanceof IndexAccess
            || expr instanceof MethodCall
            || expr instanceof CurrentNode
            || expr instanceof WildcardAccess
            || expr instanceof SliceAccess
            || expr instanceof FilterAccess
            || expr instanceof RecursiveDescentAccess
            || expr instanceof UnionAccess;
    }

    /**
     * 解析括号内的单个 JSONPath 选择器：通配 {@code *}、过滤 {@code ?(expr)}、
     * 切片 {@code start:end(:step)}（各部分可省略）或下标/键表达式。
     * 调用时当前 token 为选择器的第一个 token。
     *
     * @return 解析得到的选择器
     */
    private JsonPathSelector parseBracketSelector() {
        if (check(TokenType.MULTIPLY)) {
            advance(); // 消耗 *
            return new JsonPathSelector.WildcardSelector();
        }
        if (check(TokenType.QUESTION) && peek().getType() == TokenType.LEFT_PAREN) {
            advance(); // 消耗 ?
            advance(); // 消耗 (
            ASTNode predicate = parseExpression();
            expect(TokenType.RIGHT_PAREN);
            return new JsonPathSelector.FilterSelector(predicate);
        }
        if (check(TokenType.COLON)) {
            // 切片 [:end(:step)]，start 省略
            return parseSliceTail(null);
        }
        ASTNode start = parseExpression();
        if (check(TokenType.COLON)) {
            // 切片 [start:end(:step)]
            return parseSliceTail(start);
        }
        return new JsonPathSelector.IndexSelector(start);
    }

    /**
     * 解析切片的 {@code :end(:step)} 尾部（调用时当前 token 为第一个冒号）
     *
     * @param start 起始下标表达式（可为 null，表示省略）
     * @return 切片选择器
     */
    private JsonPathSelector.SliceSelector parseSliceTail(ASTNode start) {
        advance(); // 消耗第一个 :
        ASTNode end = null;
        ASTNode step = null;
        if (!check(TokenType.RIGHT_BRACKET) && !check(TokenType.COLON) && !check(TokenType.COMMA)) {
            end = parseExpression();
        }
        if (check(TokenType.COLON)) {
            advance(); // 消耗第二个 :
            if (!check(TokenType.RIGHT_BRACKET) && !check(TokenType.COMMA)) {
                step = parseExpression();
            }
        }
        return new JsonPathSelector.SliceSelector(start, end, step);
    }

    /**
     * 将单个括号选择器转换为对应的访问节点（保持既有节点类型与语义不变）
     *
     * @param object 被访问的对象表达式
     * @param selector 选择器
     * @param pos 位置信息（覆盖 [...] 部分）
     * @return 访问节点
     */
    private ASTNode toAccessNode(ASTNode object, JsonPathSelector selector, Position pos) {
        if (selector instanceof JsonPathSelector.SliceSelector) {
            JsonPathSelector.SliceSelector slice = (JsonPathSelector.SliceSelector) selector;
            return new SliceAccess(object, slice.getStart(), slice.getEnd(), slice.getStep(), pos);
        }
        if (selector instanceof JsonPathSelector.WildcardSelector) {
            return new WildcardAccess(object, pos);
        }
        if (selector instanceof JsonPathSelector.FilterSelector) {
            return new FilterAccess(object, ((JsonPathSelector.FilterSelector) selector).getPredicate(), pos);
        }
        return new IndexAccess(object, ((JsonPathSelector.IndexSelector) selector).getExpression(), pos);
    }

    private List<ASTNode> parseArgumentList() {
        advance(); // 消耗 (
        List<ASTNode> args = new ArrayList<>();
        if (!check(TokenType.RIGHT_PAREN)) {
            do {
                args.add(parseExpression());
            } while (check(TokenType.COMMA) && advance() != null);
        }
        expect(TokenType.RIGHT_PAREN);
        return args;
    }

    /**
     * 解析参数列表并返回右括号的位置
     * @return 包含参数列表和右括号位置的对象
     */
    private ArgumentListResult parseArgumentListWithPosition() {
        advance(); // 消耗 (
        List<ASTNode> args = new ArrayList<>();
        if (!check(TokenType.RIGHT_PAREN)) {
            do {
                args.add(parseExpression());
            } while (check(TokenType.COMMA) && advance() != null);
        }
        Token rightParen = expect(TokenType.RIGHT_PAREN);
        return new ArgumentListResult(args, rightParen);
    }

    /**
     * 参数列表解析结果，包含参数和右括号位置
     */
    private static class ArgumentListResult {
        final List<ASTNode> args;
        final Token rightParen;

        ArgumentListResult(List<ASTNode> args, Token rightParen) {
            this.args = args;
            this.rightParen = rightParen;
        }
    }

    // ==================== Lambda Expression Parsing ====================

    /**
     * Check if current position starts a Lambda expression.
     * Uses peek() to look ahead without consuming tokens.
     * Determines if '(' introduces:
     * - (expression) - parenthesized expression
     * - () -> body - no-param Lambda
     * - (params) -> body - Lambda with parameters
     */
    private boolean isLambdaStart() {
        // Must start with '('
        if (!check(TokenType.LEFT_PAREN)) {
            return false;
        }

        // Use peek() to look ahead without consuming
        // peek(0) = next token (the one after current '(')
        Token next1 = peek(0);  // Token after '('

        // Check for empty parameter list: () ->
        if (next1.getType() == TokenType.RIGHT_PAREN) {
            // () case - check if followed by ARROW
            Token next2 = peek(1);  // Token after ')'
            return next2.getType() == TokenType.ARROW;
        }

        // Try to detect parameter list pattern
        // A parameter list consists of identifiers, optionally with types
        // Pattern: (id, id, ...) or (Type id, Type id, ...)
        int offset = 0;

        while (true) {
            Token t = peek(offset);

            // Each parameter starts with an identifier (type or name)
            if (t.getType() != TokenType.IDENTIFIER) {
                return false;  // Not a parameter list
            }

            // Look at next token
            Token t2 = peek(offset + 1);

            if (t2.getType() == TokenType.IDENTIFIER) {
                // Typed parameter: Type name
                // Move past both type and name
                offset += 2;
                t2 = peek(offset);
            } else {
                // Untyped parameter: name
                offset += 1;
            }

            // Check what follows
            if (t2.getType() == TokenType.COMMA) {
                offset += 1;  // Move past comma
                continue;
            } else if (t2.getType() == TokenType.RIGHT_PAREN) {
                // End of parameter list, check for ARROW
                Token afterParen = peek(offset + 1);
                return afterParen.getType() == TokenType.ARROW;
            } else {
                return false;  // Something else - probably an expression
            }
        }
    }

    /**
     * Check if identifier followed by ARROW (single param lambda without parens).
     */
    private boolean isSingleParamLambdaNoParens() {
        if (!check(TokenType.IDENTIFIER)) {
            return false;
        }
        return peek().getType() == TokenType.ARROW;
    }

    /**
     * Parse a Lambda expression.
     * Forms:
     *   () -> expr
     *   () -> { block }
     *   x -> expr
     *   (x) -> expr
     *   (a, b) -> expr
     *   (int a, String b) -> expr
     */
    private ASTNode parseLambdaExpression() {
        Token startToken = current();
        int startLine = startToken.getLine();
        int startColumn = startToken.getStartColumn();

        List<LambdaParameter> parameters = new ArrayList<>();

        if (check(TokenType.LEFT_PAREN)) {
            // () or (params) -> body
            advance(); // consume '('

            if (check(TokenType.RIGHT_PAREN)) {
                // () -> body (no parameters)
                advance(); // consume ')'
            } else {
                // Parse parameter list
                parameters = parseLambdaParameterList();
                expect(TokenType.RIGHT_PAREN);
            }
        } else if (check(TokenType.IDENTIFIER)) {
            // Single parameter without parentheses: x -> body
            Token paramToken = advance();
            Position paramPos = new Position(paramToken.getLine(), paramToken.getStartColumn(),
                                             paramToken.getLine(), paramToken.getEndColumn());
            parameters.add(new LambdaParameter(paramToken.getValue(), null, paramPos));
        }

        expect(TokenType.ARROW);

        // Parse body
        List<ASTNode> body = null;
        ASTNode expressionBody = null;
        boolean isBlockBody = false;
        Token endBrace = null;

        if (check(TokenType.LEFT_BRACE)) {
            // Block body
            isBlockBody = true;
            Object[] blockResult = parseLambdaBlockBody();
            @SuppressWarnings("unchecked")
            List<ASTNode> parsedBody = (List<ASTNode>) blockResult[0];
            body = parsedBody;
            endBrace = (Token) blockResult[1];
        } else {
            // Expression body
            expressionBody = parseExpression();
        }

        // Calculate position
        Position position;
        if (isBlockBody) {
            if (body != null && !body.isEmpty()) {
                ASTNode last = body.get(body.size() - 1);
                position = new Position(startLine, startColumn, last.getEndLine(), last.getEndColumn());
            } else if (endBrace != null) {
                position = new Position(startLine, startColumn, endBrace.getLine(), endBrace.getEndColumn());
            } else {
                position = new Position(startLine, startColumn, startLine, startColumn);
            }
        } else {
            position = new Position(startLine, startColumn,
                expressionBody.getEndLine(), expressionBody.getEndColumn());
        }

        return new LambdaExpression(parameters, body, expressionBody, isBlockBody, position);
    }

    /**
     * Parse Lambda parameter list inside parentheses.
     */
    private List<LambdaParameter> parseLambdaParameterList() {
        List<LambdaParameter> params = new ArrayList<>();
        Set<String> seenNames = new HashSet<>();

        do {
            String type = null;
            String name;
            Token nameToken;

            // Check for type declaration
            // If current is identifier and next is also identifier, first is type
            if (check(TokenType.IDENTIFIER)) {
                if (peek().getType() == TokenType.IDENTIFIER) {
                    type = advance().getValue(); // consume type name
                }
                nameToken = advance();
                name = nameToken.getValue();
            } else {
                throw new TemplateSyntaxException("Expected parameter name",
                    current().getLine(), current().getStartColumn(),
                    current().getLine(), current().getEndColumn(), sourceText);
            }

            // Check for duplicate parameter names
            if (seenNames.contains(name)) {
                throw new TemplateSyntaxException("Duplicate parameter name '" + name + "'",
                    current().getLine(), current().getStartColumn(),
                    current().getLine(), current().getEndColumn(), sourceText);
            }
            seenNames.add(name);

            // Create Position directly from token
            Position paramPos = new Position(nameToken.getLine(), nameToken.getStartColumn(),
                                             nameToken.getLine(), nameToken.getEndColumn());
            params.add(new LambdaParameter(name, type, paramPos));

        } while (check(TokenType.COMMA) && advance() != null);

        return params;
    }

    /**
     * Parse Lambda block body.
     * Returns Object[] { List<ASTNode> statements, Token endBrace }
     */
    private Object[] parseLambdaBlockBody() {
        Token leftBrace = advance(); // consume '{'
        List<ASTNode> statements = new ArrayList<>();

        while (!check(TokenType.RIGHT_BRACE) && !check(TokenType.EOF)) {
            if (check(TokenType.RETURN)) {
                statements.add(parseReturnStatement());
            } else if (check(TokenType.LET)) {
                statements.add(parseLetStatementInLambdaBlock());
            } else {
                statements.add(parseExpression());
            }

            // Optional semicolon
            if (check(TokenType.SEMICOLON)) {
                advance();
            }
        }

        Token endBrace = expect(TokenType.RIGHT_BRACE);
        return new Object[] { statements, endBrace };
    }

    /**
     * Parse return statement inside Lambda block.
     */
    private ASTNode parseReturnStatement() {
        Token returnToken = advance(); // consume 'return'

        ASTNode value = null;
        // Check if there's an expression after return (not followed by } or ;)
        if (!check(TokenType.RIGHT_BRACE) && !check(TokenType.SEMICOLON)) {
            value = parseExpression();
        }

        Position retPos = new Position(returnToken.getLine(), returnToken.getStartColumn(),
                                       returnToken.getLine(), returnToken.getEndColumn());
        return new ReturnStatement(value, retPos);
    }

    /**
     * Parse let statement inside Lambda block.
     */
    private ASTNode parseLetStatementInLambdaBlock() {
        advance(); // consume 'let'
        Token name = expect(TokenType.IDENTIFIER);
        expect(TokenType.ASSIGN);
        ASTNode value = parseExpression();

        return new LetStatement(name.getValue(), value);
    }

    /**
     * 解析基本表达式 (字面量, 标识符, 括号表达式, 结构化表达式)
     */
    private ASTNode parsePrimary() {
        // Check for Lambda expression first (before identifier and left paren)
        // Single parameter lambda without parens: identifier -> body
        if (isSingleParamLambdaNoParens()) {
            return parseLambdaExpression();
        }
        // Lambda with parentheses: () -> body or (params) -> body
        if (isLambdaStart()) {
            return parseLambdaExpression();
        }

        if (check(TokenType.FALSE)) {
            Token token = advance();
            return new BooleanLiteral(false, token);
        }
        if (check(TokenType.TRUE)) {
            Token token = advance();
            return new BooleanLiteral(true, token);
        }
        if (check(TokenType.NULL)) {
            advance();
            return NullLiteral.INSTANCE;
        }
        if (check(TokenType.NUMBER)) {
            return parseNumberLiteral();
        }
        if (check(TokenType.LONG)) {
            return parseNumberLiteral();
        }
        if (check(TokenType.BIG_INTEGER)) {
            return parseNumberLiteral();
        }
        if (check(TokenType.BIG_DECIMAL)) {
            return parseNumberLiteral();
        }
        if (check(TokenType.STRING)) {
            Token token = advance();
            return new StringLiteral(token.getValue(), token);
        }
        if (check(TokenType.TEMPLATE_STRING)) {
            Token token = advance();
            return parseTemplateLiteral(token);
        }
        if (check(TokenType.REGEX)) {
            Token token = advance();
            return parseRegexLiteral(token);
        }
        if (check(TokenType.DOLLAR_NUMBER)) {
            Token token = advance();
            String value = token.getValue();
            // 从 "$0", "$1" 等中提取数字
            int index = Integer.parseInt(value.substring(1));
            return new ArgumentVariable(index, token);
        }
        if (check(TokenType.IDENTIFIER)) {
            Token token = advance();
            return new Identifier(token.getValue(), token);
        }
        if (check(TokenType.AT)) {
            // JSONPath 当前节点 @（仅过滤表达式内有效，后续 .price / [0] 等由 parsePostfix 接续）
            Token token = advance();
            return new CurrentNode(token);
        }
        if (check(TokenType.LEFT_PAREN)) {
            advance();
            ASTNode expr = parseExpression();
            expect(TokenType.RIGHT_PAREN);
            return expr;
        }
        if (check(TokenType.LEFT_BRACKET)) {
            return parseListLiteral();
        }
        if (check(TokenType.LEFT_BRACE)) {
             return parseHashLiteral();
        }
        if (check(TokenType.IF)) {
             return parseIfExpression();
        }
        if (check(TokenType.FOR)) {
             return parseForExpression();
        }
        if (check(TokenType.URL)) {
            return parseUrlLiteral();
        }

        throw new TemplateSyntaxException("Unexpected token: " + TokenType.getSymbol(current().getType()), current().getLine(), current().getStartColumn(), current().getLine(), current().getEndColumn(), sourceText);
    }

    private UrlLiteral parseUrlLiteral() {
        Token beginToken = advance();      // consume URL keyword
        expect(TokenType.LEFT_PAREN);       // consume (

        List<ASTNode> children = new ArrayList<>();

        while (!check(TokenType.RIGHT_PAREN) && !check(TokenType.EOF)) {
            if (check(TokenType.SINGLE_BRACE_EXPR_BEGIN)) {
                children.add(parseExpressionWrapper());
            } else if (check(TokenType.DOUBLE_BRACE_EXPR_BEGIN)) {
                children.add(parseExpressionWrapper());
            } else if (check(TokenType.DOLLAR_BRACE_EXPR_BEGIN)) {
                children.add(parseExpressionWrapper());
            } else if (check(TokenType.STMT_BEGIN)) {
                children.add(parseStatementBlock());
            } else if (check(TokenType.TEXT)) {
                children.add(new TextNode(advance().getValue()));
            } else {
                break;
            }
        }

        expect(TokenType.RIGHT_PAREN);
        return new UrlLiteral(children, beginToken);
    }

    private ASTNode parseNumberLiteral() {
        Token token = advance();
        String val = token.getValue();
        int type = token.getType();
        
        try {
            Number num;
            switch (type) {
                case TokenType.NUMBER:
                    if (val.contains(".") || val.contains("e") || val.contains("E")) {
                        num = Double.valueOf(val);
                    } else {
                        num = Integer.valueOf(val);
                    }
                    break;
                case TokenType.LONG:
                    num = Long.valueOf(val);
                    break;
                case TokenType.BIG_INTEGER:
                     num = new BigInteger(val);
                     break;
                case TokenType.BIG_DECIMAL:
                     num = new BigDecimal(val);
                     break;
                default:
                    throw new TemplateSyntaxException("Invalid number type", token.getLine(), token.getStartColumn(), token.getLine(), token.getEndColumn(), sourceText);
            }
            return new NumberLiteral(num, val, token);
        } catch (NumberFormatException e) {
             throw new TemplateSyntaxException("Invalid number format: " + val, token.getLine(), token.getStartColumn(), token.getLine(), token.getEndColumn(), sourceText);
        }
    }
    
    private ASTNode parseListLiteral() {
        Token startToken = advance(); // [
        List<ASTNode> elements = new ArrayList<>();
        if (!check(TokenType.RIGHT_BRACKET)) {
            do {
                elements.add(parseExpression());
            } while (check(TokenType.COMMA) && advance() != null);
        }
        Token endToken = expect(TokenType.RIGHT_BRACKET);
        return new ListLiteral(elements, startToken, endToken);
    }
    
    private ASTNode parseHashLiteral() {
        Token startToken = advance(); // {
        List<Map.Entry<ASTNode, ASTNode>> entries = new ArrayList<>();
        if (!check(TokenType.RIGHT_BRACE)) {
            do {
                ASTNode key;
                if (check(TokenType.IDENTIFIER)) {
                    Token keyToken = advance();
                    // 标识符键自动转换为字符串（README 3.4.7），不作为变量求值
                    key = new StringLiteral(keyToken.getValue(), keyToken);
                } else if (check(TokenType.STRING)) {
                    Token keyToken = advance();
                    key = new StringLiteral(keyToken.getValue(), keyToken);
                } else if (checkAny(TokenType.NUMBER, TokenType.LONG)) {
                    key = parseNumberLiteral();
                } else {
                     throw new TemplateSyntaxException("Invalid hash key", current().getLine(), current().getStartColumn(), current().getLine(), current().getEndColumn(), sourceText);
                }

                expect(TokenType.COLON);
                ASTNode value = parseExpression();

                entries.add(new java.util.AbstractMap.SimpleEntry<>(key, value));

            } while (check(TokenType.COMMA) && advance() != null);
        }
        Token endToken = expect(TokenType.RIGHT_BRACE);
        return new HashLiteral(entries, startToken, endToken);
    }

    /**
     * 解析字符串模板字面量（反引号包裹），将 {表达式} 拆分为文本部分与表达式部分
     *
     * <p>例如：`Hello {name}!` 拆分为 strings=["Hello ", "!"]、expressions=[name]</p>
     *
     * @param token TEMPLATE_STRING 类型的 Token（value 为反引号内的原始内容）
     * @return TemplateLiteral 节点
     */
    private ASTNode parseTemplateLiteral(Token token) {
        String content = token.getValue();
        List<String> strings = new ArrayList<>();
        List<ASTNode> expressions = new ArrayList<>();
        StringBuilder text = new StringBuilder();

        int i = 0;
        int length = content.length();
        while (i < length) {
            char ch = content.charAt(i);
            if (ch != '{') {
                text.append(ch);
                i++;
                continue;
            }

            // 查找嵌入表达式的结束 }（跟踪嵌套花括号，跳过表达式内的字符串字面量）
            int depth = 1;
            int j = i + 1;
            StringBuilder exprSource = new StringBuilder();
            boolean closed = false;
            while (j < length) {
                char ech = content.charAt(j);
                if (ech == '\'' || ech == '"' || ech == '`') {
                    // 表达式内的字符串/模板字面量整体跳过，其中的花括号不计入深度
                    int end = skipQuoted(content, j);
                    exprSource.append(content, j, end);
                    j = end;
                    continue;
                }
                if (ech == '{') {
                    depth++;
                } else if (ech == '}') {
                    depth--;
                    if (depth == 0) {
                        closed = true;
                        break;
                    }
                }
                exprSource.append(ech);
                j++;
            }

            if (!closed) {
                throw new TemplateSyntaxException("Unclosed embedded expression in template string",
                    token.getLine(), token.getStartColumn(), token.getLine(), token.getEndColumn(), sourceText);
            }

            strings.add(text.toString());
            text.setLength(0);
            expressions.add(parseTemplateEmbeddedExpression(exprSource.toString(), token));
            i = j + 1;
        }

        strings.add(text.toString());
        return new TemplateLiteral(strings, expressions);
    }

    /**
     * 跳过从 start 开始的带引号内容（字符串或模板字面量）
     *
     * @param content 模板内容
     * @param start   开始引号的下标
     * @return 结束引号之后的下标
     */
    private int skipQuoted(String content, int start) {
        char quote = content.charAt(start);
        int i = start + 1;
        while (i < content.length()) {
            char ch = content.charAt(i);
            if (ch == '\\' && i + 1 < content.length()) {
                i += 2;
                continue;
            }
            if (ch == quote) {
                return i + 1;
            }
            i++;
        }
        return content.length();
    }

    /**
     * 解析字符串模板中嵌入的表达式（如 `Hello {name}` 中的 name）
     *
     * @param exprSource    嵌入表达式的源码
     * @param templateToken 字符串模板 Token（用于错误定位）
     * @return 表达式 AST 节点
     */
    private ASTNode parseTemplateEmbeddedExpression(String exprSource, Token templateToken) {
        Lexer lexer = new Lexer(new StringSource(exprSource), true);
        Parser parser = new Parser(new TokenStream(lexer), exprSource);
        List<ASTNode> statements = parser.parseEvalSource();
        if (statements.size() != 1) {
            throw new TemplateSyntaxException("Template string embedded content must be a single expression: {" + exprSource + "}",
                templateToken.getLine(), templateToken.getStartColumn(), templateToken.getLine(), templateToken.getEndColumn(), sourceText);
        }
        return statements.get(0);
    }

    /**
     * 解析正则表达式字面量，从 Token 中拆分模式与标志位
     *
     * <p>Lexer 将正则保存为 pattern:flags 形式的 value，同时在 Token.flags 中保存了
     * 原始标志位；这里依据 flags 从 value 末尾去掉 ":flags" 得到模式，避免模式自身
     * 以 :[gim]+ 结尾时的拆分歧义</p>
     *
     * @param token REGEX 类型的 Token
     * @return RegexLiteral 节点
     */
    private ASTNode parseRegexLiteral(Token token) {
        String value = token.getValue();
        String flags = token.getFlags() != null ? token.getFlags() : "";
        String pattern = value;
        if (!flags.isEmpty() && value.endsWith(":" + flags)) {
            pattern = value.substring(0, value.length() - flags.length() - 1);
        }
        return new RegexLiteral(pattern, flags, token);
    }

    // ==================== 空白行处理 ====================
    
    /**
     * 移除 StatementBlock 节点所在行的空白字符
     * 当 StatementBlock 节点处于单独一行时（前后都是空白字符和换行符），
     * 移除该行前后的空白字符，使该行成为空白行（不渲染）
     *
     * @param children 子节点列表
     */
    private void removeBlankLines(List<ASTNode> children) {
        // 使用循环从后往前处理，这样删除节点时不会影响索引
        for (int i = children.size() - 1; i >= 0; i--) {
            ASTNode node = children.get(i);

            // 处理 StatementBlock 及其子类
            if (node instanceof StatementBlock) {
                StatementBlock stmtBlock = (StatementBlock) node;
                
                // 递归处理 StatementBlock 内部的空白字符
                // 对于 ForStatement，处理其 body
                if (node instanceof ForStatement) {
                    ForStatement forStmt = (ForStatement) node;
                    List<ASTNode> body = forStmt.getBody();
                    if (body != null && !body.isEmpty()) {
                        // 处理 body 开头的换行符
                        removeLeadingNewlineFromFirstTextNode(body);
                        // 处理 body 结尾的换行符
                        removeTrailingNewlineFromLastTextNode(body);
                        // 递归处理 body 内部的空白字符
                        removeBlankLines(body);
                    }
                }
                // 对于 IfStatement，处理其各个分支
                else if (node instanceof IfStatement) {
                    IfStatement ifStmt = (IfStatement) node;
                    // 处理 then body
                    if (ifStmt.getThenBody() != null && !ifStmt.getThenBody().isEmpty()) {
                        removeLeadingNewlineFromFirstTextNode(ifStmt.getThenBody());
                        removeTrailingNewlineFromLastTextNode(ifStmt.getThenBody());
                        removeBlankLines(ifStmt.getThenBody());
                    }
                    // 处理 else if 分支
                    if (ifStmt.getElseIfClauses() != null) {
                        for (IfStatement.ElseIfClause clause : ifStmt.getElseIfClauses()) {
                            if (clause.getBody() != null && !clause.getBody().isEmpty()) {
                                removeLeadingNewlineFromFirstTextNode(clause.getBody());
                                removeTrailingNewlineFromLastTextNode(clause.getBody());
                                removeBlankLines(clause.getBody());
                            }
                        }
                    }
                    // 处理 else body
                    if (ifStmt.getElseBody() != null && !ifStmt.getElseBody().isEmpty()) {
                        removeLeadingNewlineFromFirstTextNode(ifStmt.getElseBody());
                        removeTrailingNewlineFromLastTextNode(ifStmt.getElseBody());
                        removeBlankLines(ifStmt.getElseBody());
                    }
                }
                // 对于 SwitchStatement，处理其各个 case
                else if (node instanceof SwitchStatement) {
                    SwitchStatement switchStmt = (SwitchStatement) node;
                    // 处理 case 分支
                    if (switchStmt.getCases() != null) {
                        for (CaseClause caseClause : switchStmt.getCases()) {
                            if (caseClause.getBody() != null && !caseClause.getBody().isEmpty()) {
                                removeLeadingNewlineFromFirstTextNode(caseClause.getBody());
                                removeTrailingNewlineFromLastTextNode(caseClause.getBody());
                                removeBlankLines(caseClause.getBody());
                            }
                        }
                    }
                    // 处理 default 分支
                    if (switchStmt.getDefaultBody() != null && !switchStmt.getDefaultBody().isEmpty()) {
                        removeLeadingNewlineFromFirstTextNode(switchStmt.getDefaultBody());
                        removeTrailingNewlineFromLastTextNode(switchStmt.getDefaultBody());
                        removeBlankLines(switchStmt.getDefaultBody());
                    }
                }

                boolean removedNext = false;
                boolean removedPrev = false;

                // 检查后一个节点是否是 TextNode，且以空白字符+换行符开头
                if (i + 1 < children.size()) {
                    ASTNode nextNode = children.get(i + 1);
                    if (nextNode instanceof TextNode) {
                        TextNode nextTextNode = (TextNode) nextNode;
                        String nextText = nextTextNode.getText();

                        // 只删除包含换行符的空白文本（StatementBlock 所在行的空白字符）
                        // 不删除只包含空格或制表符的文本（独立的空白内容）
                        if (isWhitespaceOnly(nextText) && nextText.contains("\n")) {
                            // 完全删除这个空白文本节点
                            children.remove(i + 1);
                            removedNext = true;
                        } else if (startsWithWhitespaceAndNewline(nextText)) {
                            // 修改后一个 TextNode：删除开头的空白字符和第一个换行符
                            String newNextText = removeLeadingWhitespaceAndFirstNewline(nextText);
                            if (newNextText.isEmpty()) {
                                children.remove(i + 1);
                            } else {
                                children.set(i + 1, new TextNode(newNextText));
                            }
                            removedNext = true;
                        } else if (nextText.startsWith("\n") && !hasNonWhitespaceBeforeFirstNewline(nextText)) {
                            // 处理只以换行符开头的情况，但需要确保换行符前没有非空白字符（StatementBlock 单独在一行）
                            // 删除第一个换行符
                            String newNextText = nextText.substring(1);
                            if (newNextText.isEmpty()) {
                                children.remove(i + 1);
                            } else {
                                children.set(i + 1, new TextNode(newNextText));
                            }
                            removedNext = true;
                        }
                    }
                }

                // 检查前一个节点是否是 TextNode，且以换行符+空白字符结尾
                if (i > 0) {
                    ASTNode prevNode = children.get(i - 1);
                    if (prevNode instanceof TextNode) {
                        TextNode prevTextNode = (TextNode) prevNode;
                        String prevText = prevTextNode.getText();

                        // 检查是否以换行符+空白字符结尾
                        if (endsWithNewlineAndWhitespace(prevText)) {
                            // 修改前一个 TextNode：删除最后一个换行符后的所有空白字符
                            String newPrevText = removeTrailingWhitespaceAfterLastNewline(prevText);
                            if (newPrevText.isEmpty()) {
                                children.remove(i - 1);
                            } else {
                                children.set(i - 1, new TextNode(newPrevText));
                            }
                            removedPrev = true;
                        } else if (prevText.endsWith("\n") && isOnlyWhitespaceBeforeLastNewline(prevText)) {
                            // 处理只以换行符结尾的情况，但需要确保换行符前只有空白字符（StatementBlock 单独在一行）
                            // 删除最后一个换行符
                            String newPrevText = prevText.substring(0, prevText.length() - 1);
                            if (newPrevText.isEmpty()) {
                                children.remove(i - 1);
                            } else {
                                children.set(i - 1, new TextNode(newPrevText));
                            }
                            removedPrev = true;
                        }
                    }
                }
            }
        }

        // 移除空文本节点
        children.removeIf(node -> node instanceof TextNode && ((TextNode) node).getText().isEmpty());
    }
    
    /**
     * 移除列表中第一个 TextNode 开头的换行符（如果有）
     *
     * @param children 子节点列表
     */
    private void removeLeadingNewlineFromFirstTextNode(List<ASTNode> children) {
        if (children == null || children.isEmpty()) {
            return;
        }
        
        ASTNode firstNode = children.get(0);
        if (firstNode instanceof TextNode) {
            TextNode textNode = (TextNode) firstNode;
            String text = textNode.getText();
            
            // 如果文本以换行符开头（可能有前导空白字符），删除开头的空白字符和第一个换行符
            if (startsWithWhitespaceAndNewline(text)) {
                String newText = removeLeadingWhitespaceAndFirstNewline(text);
                if (newText.isEmpty()) {
                    children.remove(0);
                } else {
                    children.set(0, new TextNode(newText));
                }
            }
        }
    }
    
    /**
     * 移除列表中最后一个 TextNode 结尾的换行符（如果有）
     *
     * @param children 子节点列表
     */
    private void removeTrailingNewlineFromLastTextNode(List<ASTNode> children) {
        if (children == null || children.isEmpty()) {
            return;
        }
        
        ASTNode lastNode = children.get(children.size() - 1);
        if (lastNode instanceof TextNode) {
            TextNode textNode = (TextNode) lastNode;
            String text = textNode.getText();
            
            // 如果文本以换行符+空白字符结尾，删除最后一个换行符后的所有空白字符
            if (endsWithNewlineAndWhitespace(text)) {
                String newText = removeTrailingWhitespaceAfterLastNewline(text);
                if (newText.isEmpty()) {
                    children.remove(children.size() - 1);
                } else {
                    children.set(children.size() - 1, new TextNode(newText));
                }
            }
        }
    }

    /**
     * 检查文本是否只包含空白字符（空格、制表符、换行符）
     *
     * @param text 文本内容
     * @return 如果只包含空白字符返回true
     */
    private boolean isWhitespaceOnly(String text) {
        if (text == null || text.isEmpty()) {
            return true;
        }

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c != ' ' && c != '\t' && c != '\n' && c != '\r') {
                return false;
            }
        }
        return true;
    }
    
    /**
     * 检查文本是否以换行符开头（可能有前导空白字符）
     * 格式：(空白字符) + 换行符 + 其他内容
     *
     * @param text 文本内容
     * @return 如果以换行符开头返回true（可能有前导空白字符）
     */
    private boolean startsWithWhitespaceAndNewline(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }

        // 查找第一个换行符的位置
        int firstNewline = -1;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\n') {
                firstNewline = i;
                break;
            } else if (c != ' ' && c != '\t') {
                // 遇到非空白非换行符，说明不是以换行符开头
                return false;
            }
        }

        // 检查是否找到了换行符
        return firstNewline != -1;
    }
    
    /**
     * 检查文本是否以换行符+空白字符结尾
     * 格式：...内容\n空白字符（空格或制表符）
     * 
     * @param text 文本内容
     * @return 如果以换行符+空白字符结尾返回true
     */
    private boolean endsWithNewlineAndWhitespace(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        
        // 从后往前查找最后一个换行符的位置
        int lastNewline = -1;
        for (int i = text.length() - 1; i >= 0; i--) {
            if (text.charAt(i) == '\n') {
                lastNewline = i;
                break;
            }
        }
        
        // 如果没有换行符，返回false
        if (lastNewline == -1) {
            return false;
        }
        
        // 检查换行符后是否全是空白字符（至少有一个空白字符）
        if (lastNewline + 1 >= text.length()) {
            return false;
        }
        
        String afterNewline = text.substring(lastNewline + 1);
        // 必须至少有一个空白字符，且全是空白字符
        return !afterNewline.isEmpty() && afterNewline.matches("[ \t]+");
    }
    
    /**
     * 删除文本中最后一个换行符后的所有空白字符
     * 
     * @param text 文本内容
     * @return 处理后的文本
     */
    private String removeTrailingWhitespaceAfterLastNewline(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        // 从后往前查找最后一个换行符的位置
        int lastNewline = -1;
        for (int i = text.length() - 1; i >= 0; i--) {
            if (text.charAt(i) == '\n') {
                lastNewline = i;
                break;
            }
        }
        
        // 如果没有换行符，返回原文本
        if (lastNewline == -1) {
            return text;
        }
        
        // 返回换行符之前的内容+换行符
        return text.substring(0, lastNewline + 1);
    }
    
    /**
     * 删除文本开头的空白字符和第一个换行符
     *
     * @param text 文本内容
     * @return 处理后的文本
     */
    private String removeLeadingWhitespaceAndFirstNewline(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        // 查找第一个换行符的位置
        int firstNewline = -1;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\n') {
                firstNewline = i;
                break;
            } else if (c != ' ' && c != '\t') {
                // 遇到非空白非换行符，返回原文本
                return text;
            }
        }

        // 如果没有换行符，返回原文本
        if (firstNewline == -1) {
            return text;
        }

        // 返回第一个换行符之后的内容
        return text.substring(firstNewline + 1);
    }
    
    /**
     * 检查文本在第一个换行符前是否有非空白字符
     * 如果文本以换行符开头，返回 false（表示换行符前没有非空白字符）
     *
     * @param text 文本内容
     * @return 如果第一个换行符前有非空白字符返回 true，否则返回 false
     */
    private boolean hasNonWhitespaceBeforeFirstNewline(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        
        // 如果文本以换行符开头，返回 false
        if (text.startsWith("\n")) {
            return false;
        }
        
        // 查找第一个换行符的位置
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\n') {
                // 找到换行符，检查之前是否有非空白字符
                for (int j = 0; j < i; j++) {
                    char ch = text.charAt(j);
                    if (ch != ' ' && ch != '\t') {
                        return true;
                    }
                }
                return false;
            }
        }
        
        // 没有换行符，检查整个文本是否有非空白字符
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c != ' ' && c != '\t' && c != '\n' && c != '\r') {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 检查文本在最后一个换行符前是否只有空白字符
     * 如果文本以换行符结尾，检查换行符前是否只有空白字符
     *
     * @param text 文本内容
     * @return 如果最后一个换行符前只有空白字符返回 true，否则返回 false
     */
    private boolean isOnlyWhitespaceBeforeLastNewline(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        
        // 从后往前查找最后一个换行符的位置
        int lastNewline = -1;
        for (int i = text.length() - 1; i >= 0; i--) {
            if (text.charAt(i) == '\n') {
                lastNewline = i;
                break;
            }
        }
        
        // 如果没有换行符，返回 false
        if (lastNewline == -1) {
            return false;
        }
        
        // 检查换行符前是否只有空白字符
        for (int i = 0; i < lastNewline; i++) {
            char c = text.charAt(i);
            if (c != ' ' && c != '\t' && c != '\n' && c != '\r') {
                return false;
            }
        }
        return true;
    }

    // ==================== 工具方法 ====================

    private BinaryExpression.Operator getBinaryOperator(int type) {
        switch (type) {
            case TokenType.PLUS: return BinaryExpression.Operator.ADD;
            case TokenType.MINUS: return BinaryExpression.Operator.SUBTRACT;
            case TokenType.MULTIPLY: return BinaryExpression.Operator.MULTIPLY;
            case TokenType.DIVIDE: return BinaryExpression.Operator.DIVIDE;
            case TokenType.MODULO: return BinaryExpression.Operator.MODULO;
            case TokenType.GT: return BinaryExpression.Operator.GT;
            case TokenType.LT: return BinaryExpression.Operator.LT;
            case TokenType.GTE: return BinaryExpression.Operator.GTE;
            case TokenType.LTE: return BinaryExpression.Operator.LTE;
            case TokenType.EQ: return BinaryExpression.Operator.EQ;
            case TokenType.NEQ: return BinaryExpression.Operator.NEQ;
            case TokenType.AND: return BinaryExpression.Operator.AND;
            case TokenType.OR: return BinaryExpression.Operator.OR;
            case TokenType.REGEX_MATCH: return BinaryExpression.Operator.REGEX_MATCH;
            case TokenType.REGEX_NOT_MATCH: return BinaryExpression.Operator.REGEX_NOT_MATCH;
            case TokenType.IN: return BinaryExpression.Operator.IN;
            case TokenType.NOT_IN: return BinaryExpression.Operator.NOT_IN;
            case TokenType.SUBSETOF: return BinaryExpression.Operator.SUBSETOF;
            case TokenType.ANYOF: return BinaryExpression.Operator.ANYOF;
            case TokenType.NONEOF: return BinaryExpression.Operator.NONEOF;
            default: throw new TemplateSyntaxException("Unknown binary operator", current().getLine(), current().getStartColumn(), current().getLine(), current().getEndColumn(), sourceText);
        }
    }
    
    private UnaryExpression.Operator getUnaryOperator(int type) {
        switch (type) {
            case TokenType.NOT: return UnaryExpression.Operator.NOT;
            case TokenType.MINUS: return UnaryExpression.Operator.NEGATE;
            case TokenType.PLUS: return UnaryExpression.Operator.PLUS;
            default: throw new TemplateSyntaxException("Unknown unary operator", current().getLine(), current().getStartColumn(), current().getLine(), current().getEndColumn(), sourceText);
        }
    }

    /**
     * 判断 token 类型是否是表达式开始
     * 用于区分类型转换和括号表达式
     *
     * @param type Token 类型
     * @return 如果是表达式开始返回 true
     */
    private boolean isExpressionStart(int type) {
        return type == TokenType.IDENTIFIER
            || type == TokenType.NUMBER
            || type == TokenType.LONG
            || type == TokenType.BIG_INTEGER
            || type == TokenType.BIG_DECIMAL
            || type == TokenType.STRING
            || type == TokenType.TEMPLATE_STRING
            || type == TokenType.TRUE
            || type == TokenType.FALSE
            || type == TokenType.NULL
            || type == TokenType.DOLLAR_NUMBER
            || type == TokenType.LEFT_PAREN
            || type == TokenType.LEFT_BRACKET
            || type == TokenType.LEFT_BRACE
            || type == TokenType.NOT
            || type == TokenType.MINUS
            || type == TokenType.PLUS
            || type == TokenType.IF
            || type == TokenType.FOR
            || type == TokenType.URL
            || type == TokenType.REGEX;
    }

    /**
     * 判断当前位置是否是类型转换表达式
     * 条件：LEFT_PAREN + IDENTIFIER + RIGHT_PAREN + 表达式开始
     *
     * @return 如果是类型转换返回 true
     */
    private boolean isCastExpression() {
        if (!check(TokenType.LEFT_PAREN)) {
            return false;
        }

        // peek(0) 是 LEFT_PAREN 后的 token
        Token next1 = peek(0);
        if (next1 == null || next1.getType() != TokenType.IDENTIFIER) {
            return false;
        }

        // peek(1) 是 IDENTIFIER 后的 token，必须是 RIGHT_PAREN
        Token next2 = peek(1);
        if (next2 == null || next2.getType() != TokenType.RIGHT_PAREN) {
            return false;
        }

        // peek(2) 是 RIGHT_PAREN 后的 token，必须是表达式开始
        Token next3 = peek(2);
        if (next3 == null) {
            return false;
        }
        return isExpressionStart(next3.getType());
    }

    /**
     * 解析类型转换表达式
     * 语法：(type) expression
     *
     * @return 类型转换表达式 AST 节点
     */
    private ASTNode parseCastExpression() {
        Token leftParen = advance();  // 消耗 (
        Token typeToken = advance();  // 消耗类型名标识符
        advance();  // 消耗 )

        ASTNode operand = parseUnary();  // 递归调用支持链式转换

        Position pos = new Position(leftParen.getLine(), leftParen.getStartColumn(),
                                   operand.getEndLine(), operand.getEndColumn());

        return new CastExpression(typeToken.getValue(), operand, pos);
    }

    private RangeLiteral.RangeType getRangeType(int type) {
        switch (type) {
            case TokenType.RANGE_CLOSED: return RangeLiteral.RangeType.CLOSED;
            case TokenType.RANGE_OPEN: return RangeLiteral.RangeType.OPEN;
            case TokenType.RANGE_LEFT_CLOSED_RIGHT_OPEN: return RangeLiteral.RangeType.LEFT_CLOSED_RIGHT_OPEN;
            case TokenType.RANGE_LEFT_OPEN_RIGHT_CLOSED: return RangeLiteral.RangeType.LEFT_OPEN_RIGHT_CLOSED;
            default: throw new TemplateSyntaxException("Unknown range type", current().getLine(), current().getStartColumn(), current().getLine(), current().getEndColumn(), sourceText);
        }
    }
}
