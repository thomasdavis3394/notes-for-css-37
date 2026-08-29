package org.dromara.easytl.parser;

import org.dromara.easytl.exception.TemplateSyntaxException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 词法分析器（Lexer）
 * 
 * <p>负责将模板源代码转换为 Token 流，支持三种模式：</p>
 * <ul>
 *   <li>文本模式：解析普通文本内容，识别表达式和语句块的开始</li>
 *   <li>表达式模式：解析 {} 或 {{}} 中的表达式</li>
 *   <li>语句块模式：解析 {% %} 中的语句</li>
 * </ul>
 * 
 * <p>支持的语法特性：</p>
 * <ul>
 *   <li>字符串字面量（单引号、双引号、字符串模板）</li>
 *   <li>数字字面量（整数、长整数、大整数、浮点数、科学计数法、高精度小数）</li>
 *   <li>正则表达式字面量（/pattern/flags）</li>
 *   <li>运算符（算术、比较、逻辑、空安全、Elvis、正则匹配、区间）</li>
 *   <li>关键字（if、for、switch、let、extends、import 等）</li>
 *   <li>语句块标记（{%、%}、{/%）</li>
 * </ul>
 *
 * @author EasyTL Team
 */
public class Lexer {

    /**
     * 词法分析器的工作模式
     */
    private enum Mode {
        /** 文本模式：解析普通文本 */
        TEXT,
        /** 表达式模式：解析 {} 或 {{}} 中的表达式 */
        EXPRESSION,
        /** 语句块模式：解析 {% %} 中的语句 */
        STATEMENT,
        /** URL模式：解析 url(...) 内部内容 */
        URL,
        /** Eval模式：解析纯表达式/语句，直到EOF（用于Eval.eval()） */
        EVAL
    }

    // 关键字映射
    private static final Map<String, Integer> KEYWORDS = new HashMap<>();
    static {
        KEYWORDS.put("if", TokenType.IF);
        KEYWORDS.put("else", TokenType.ELSE);
        KEYWORDS.put("for", TokenType.FOR);
        KEYWORDS.put("in", TokenType.IN);
        KEYWORDS.put("switch", TokenType.SWITCH);
        KEYWORDS.put("case", TokenType.CASE);
        KEYWORDS.put("default", TokenType.DEFAULT);
        KEYWORDS.put("let", TokenType.LET);
        KEYWORDS.put("extends", TokenType.EXTENDS);
        KEYWORDS.put("import", TokenType.IMPORT);
        KEYWORDS.put("true", TokenType.TRUE);
        KEYWORDS.put("false", TokenType.FALSE);
        KEYWORDS.put("null", TokenType.NULL);
        KEYWORDS.put("return", TokenType.RETURN);
        KEYWORDS.put("as", TokenType.AS);
        KEYWORDS.put("export", TokenType.EXPORT);
        KEYWORDS.put("subsetof", TokenType.SUBSETOF);
        KEYWORDS.put("anyof", TokenType.ANYOF);
        KEYWORDS.put("noneof", TokenType.NONEOF);
    }

    private final Source source;
    private Mode mode;
    private int braceDepth;  // 花括号深度（用于跟踪嵌套）
    private boolean doubleBrace; // 是否使用双花括号
    private boolean dollarBrace; // 是否使用美元符号表达式
    private boolean pendingLeftBrace; // 是否有待输出的 LEFT_BRACE token
    private Integer lastExpressionTokenType; // 表达式模式下上一个 Token 类型
    private Mode previousMode; // 进入URL模式前的模式（用于退出URL模式时恢复）
    private int urlParenDepth; // URL模式下的括号深度（用于处理嵌套的括号）
    private boolean inUrlContext; // 是否在URL上下文中（用于从嵌套表达式/语句返回URL模式）
    private int savedBraceDepth; // 进入URL模式前保存的braceDepth（用于退出URL模式时恢复）

    /**
     * 构造一个 Lexer
     *
     * @param source 模板源码
     */
    public Lexer(Source source) {
        this(source, false);
    }

    /**
     * 构造一个 Lexer，可选择是否以表达式模式开始
     *
     * @param source 模板源码
     * @param evalMode 是否以Eval模式开始（用于 Eval.eval() 等纯表达式解析场景）
     */
    public Lexer(Source source, boolean evalMode) {
        this(source, evalMode, false);
    }

    /**
     * 构造一个 Lexer，可选择模式
     *
     * @param source 模板源码
     * @param evalMode 是否以Eval模式开始（用于 Eval.eval() 等纯表达式解析场景）
     * @param urlMode 是否以URL模式开始（用于 Eval.evalAsURL() 场景）
     */
    public Lexer(Source source, boolean evalMode, boolean urlMode) {
        this.source = source;
        if (urlMode) {
            this.mode = Mode.URL;
            this.urlParenDepth = 0; // 不跟踪括号深度，因为没有外层 url(...)
            this.inUrlContext = true;
        } else if (evalMode) {
            this.mode = Mode.EVAL;
            this.urlParenDepth = 0;
            this.inUrlContext = false;
        } else {
            this.mode = Mode.TEXT;
            this.urlParenDepth = 0;
            this.inUrlContext = false;
        }
        this.braceDepth = 0;
        this.doubleBrace = false;
        this.dollarBrace = false;
        this.pendingLeftBrace = false;
        this.lastExpressionTokenType = null;
        this.previousMode = null;
        this.savedBraceDepth = 0;
    }

    /**
     * 读取下一个 Token
     *
     * <p>根据当前模式（TEXT/EXPRESSION/STATEMENT/EVAL）调用相应的 Token 读取方法</p>
     *
     * @return 下一个 Token
     * @throws IOException 如果读取源代码时发生 I/O 错误
     */
    public Token nextToken() throws IOException {
        if (mode == Mode.TEXT) {
            return nextTextToken();
        } else if (mode == Mode.EXPRESSION) {
            return nextExpressionToken();
        } else if (mode == Mode.STATEMENT) {
            return nextStatementToken();
        } else if (mode == Mode.EVAL) {
            return nextEvalToken();
        } else {
            return nextUrlToken();
        }
    }

    /**
     * 根据表达式上下文更新最近一次返回的 Token 类型
     */
    private Token wrapExpressionToken(Token token) {
        int type = token.getType();

        // 检查是否需要进入URL模式：上一个token是URL，当前token是LEFT_PAREN
        if (type == TokenType.LEFT_PAREN && lastExpressionTokenType != null
            && lastExpressionTokenType == TokenType.URL) {
            previousMode = mode;
            savedBraceDepth = braceDepth; // 保存当前braceDepth
            mode = Mode.URL;
            urlParenDepth = 1;
            inUrlContext = true;
        }

        if (type == TokenType.SINGLE_BRACE_EXPR_END ||
            type == TokenType.DOUBLE_BRACE_EXPR_END ||
            type == TokenType.DOLLAR_BRACE_EXPR_END) {
            lastExpressionTokenType = null;
        } else {
            lastExpressionTokenType = type;
        }
        return token;
    }

    /**
     * 判断当前是否应将 / 解析为正则表达式起始
     */
    private boolean isRegexContext() {
        if (lastExpressionTokenType == null) {
            return true;
        }
        return !isValueLikeToken(lastExpressionTokenType);
    }

    private boolean isValueLikeToken(int type) {
        switch (type) {
            case TokenType.IDENTIFIER:
            case TokenType.STRING:
            case TokenType.TEMPLATE_STRING:
            case TokenType.NUMBER:
            case TokenType.LONG:
            case TokenType.BIG_INTEGER:
            case TokenType.BIG_DECIMAL:
            case TokenType.REGEX:
            case TokenType.TRUE:
            case TokenType.FALSE:
            case TokenType.NULL:
            case TokenType.RIGHT_PAREN:
            case TokenType.RIGHT_BRACKET:
            case TokenType.RIGHT_BRACE:
            case TokenType.SINGLE_BRACE_EXPR_END:
            case TokenType.DOUBLE_BRACE_EXPR_END:
            case TokenType.DOLLAR_BRACE_EXPR_END:
                return true;
            default:
                return false;
        }
    }

    /**
     * 获取当前行号
     * 
     * @return 行号（从 1 开始）
     */
    public int getLine() {
        return source.getLine();
    }

    /**
     * 获取当前列号
     * 
     * @return 列号（从 1 开始）
     */
    public int getColumn() {
        return source.getColumn();
    }

    /**
     * 获取当前位置
     * 
     * @return 字符位置（从 0 开始）
     */
    public int getPosition() {
        return source.getPosition();
    }

    /**
     * 读取文本模式的 Token
     *
     * <p>在文本模式下，识别以下内容：</p>
     * <ul>
     *   <li>普通文本内容 → TEXT</li>
     *   <li>表达式开始标记 { 或 {{ → EXPR_BEGIN，切换到表达式模式</li>
     *   <li>语句块开始标记 {% → STMT_BEGIN，切换到语句块模式</li>
     *   <li>语句块结束标记 {/% → STMT_CLOSE</li>
     *   <li>转义括号 \{ 或 \} → 输出对应的括号字符</li>
     *   <li>原始文本块 @raw{...} → 内容原样输出，支持嵌套括号</li>
     *   <li>原始文本行 @raw: → 冒号后的所有内容原样输出</li>
     * </ul>
     *
     * @return 文本 Token 或控制标记 Token
     * @throws IOException 如果读取源代码时发生 I/O 错误
     */
    private Token nextTextToken() throws IOException {
        StringBuilder text = new StringBuilder();
        int startLine = source.getLine();
        int startColumn = source.getColumn();
        int startPosition = source.getPosition();

        while (true) {
            int ch = source.peek();
            if (ch == -1) {
                break;
            }

            // 处理转义括号 \{ 和 \}
            if (ch == '\\') {
                int next = source.peek(1);
                if (next == '{' || next == '}') {
                    source.read(); // 消费 \
                    text.append((char) source.read()); // 添加转义后的字符
                    continue;
                }
            }

            // 处理 @raw{...} 和 @raw: 原始文本语法
            if (ch == '@') {
                // 检查是否是 @raw{ 或 @raw:
                if (isRawBlockStart()) {
                    // @raw{...} 语法
                    if (text.length() > 0) {
                        return new Token(TokenType.TEXT, text.toString(), startLine, startColumn, source.getEndColumn(), startPosition);
                    }
                    // 消费 @raw{
                    source.read(); // @
                    source.read(); // r
                    source.read(); // a
                    source.read(); // w
                    source.read(); // {
                    String rawContent = readRawBlockContent();
                    return new Token(TokenType.TEXT, rawContent, startLine, startColumn, source.getEndColumn(), startPosition);
                } else if (isRawColonStart()) {
                    // @raw: 语法
                    if (text.length() > 0) {
                        return new Token(TokenType.TEXT, text.toString(), startLine, startColumn, source.getEndColumn(), startPosition);
                    }
                    // 消费 @raw:
                    source.read(); // @
                    source.read(); // r
                    source.read(); // a
                    source.read(); // w
                    source.read(); // :
                    String rawContent = readRawColonContent();
                    return new Token(TokenType.TEXT, rawContent, startLine, startColumn, source.getEndColumn(), startPosition);
                }
            }

            // 检查是否是表达式开始 {、{{ 或 ${
            if (ch == '{') {
                int next = source.peek(1);
                if (next == '%') {
                    // 语句块开始 {%
                    if (text.length() > 0) {
                        return new Token(TokenType.TEXT, text.toString(), startLine, startColumn, source.getEndColumn(), startPosition);
                    }
                    source.read(); // 消费 {
                    source.read(); // 消费 %
                    mode = Mode.STATEMENT;
                    return new Token(TokenType.STMT_BEGIN, "{%", startLine, startColumn, source.getEndColumn(), startPosition);
                } else if (next == '/' && source.peek(2) == '%') {
                    // 语句块关闭 {/%
                    // 标准语句块结束标签格式：{/% 语句名称 }
                    if (text.length() > 0) {
                        return new Token(TokenType.TEXT, text.toString(), startLine, startColumn, source.getEndColumn(), startPosition);
                    }
                    source.read(); // {
                    source.read(); // /
                    source.read(); // %
                    mode = Mode.STATEMENT; // 切换到语句块模式，以便解析后续的语句名称和 }
                    return new Token(TokenType.STMT_CLOSE, "{/%", startLine, startColumn, source.getEndColumn(), startPosition);
                } else if (next == '{') {
                    // 双花括号 {{
                    if (text.length() > 0) {
                        return new Token(TokenType.TEXT, text.toString(), startLine, startColumn, source.getEndColumn(), startPosition);
                    }
                    source.read(); // 消费第一个 {
                    source.read(); // 消费第二个 {
                    mode = Mode.EXPRESSION;
                    doubleBrace = true;
                    dollarBrace = false;

                    // 检查是否是哈希表字面量模式 {{key: value}}
                    // 如果 {{ 后面跟着 identifier:、string: 或 number:，则认为是哈希表字面量
                    // 此时需要将 {{ 拆分为 SINGLE_BRACE_EXPR_BEGIN({) 和 LEFT_BRACE({)
                    if (looksLikeHashLiteral()) {
                        braceDepth = 0;
                        pendingLeftBrace = true;
                        lastExpressionTokenType = null;
                        return new Token(TokenType.SINGLE_BRACE_EXPR_BEGIN, "{", startLine, startColumn, source.getEndColumn(), startPosition);
                    } else {
                        braceDepth = 0;
                        lastExpressionTokenType = null;
                        return new Token(TokenType.DOUBLE_BRACE_EXPR_BEGIN, "{{", startLine, startColumn, source.getEndColumn(), startPosition);
                    }
                } else if (isExpressionStart(next)) {
                    // 单花括号表达式 {
                    if (text.length() > 0) {
                        return new Token(TokenType.TEXT, text.toString(), startLine, startColumn, source.getEndColumn(), startPosition);
                    }
                    source.read(); // 消费 {
                    mode = Mode.EXPRESSION;
                    doubleBrace = false;
                    dollarBrace = false;
                    braceDepth = 1;
                    lastExpressionTokenType = null;
                    return new Token(TokenType.SINGLE_BRACE_EXPR_BEGIN, "{", startLine, startColumn, source.getEndColumn(), startPosition);
                }
            } else if (ch == '$') {
                // 检查是否是美元符号表达式 ${
                int next = source.peek(1);
                if (next == '{') {
                    // 美元符号表达式 ${
                    if (text.length() > 0) {
                        return new Token(TokenType.TEXT, text.toString(), startLine, startColumn, source.getEndColumn(), startPosition);
                    }
                    source.read(); // 消费 $
                    source.read(); // 消费 {
                    mode = Mode.EXPRESSION;
                    doubleBrace = false;
                    dollarBrace = true;
                    braceDepth = 1;
                    lastExpressionTokenType = null;
                    return new Token(TokenType.DOLLAR_BRACE_EXPR_BEGIN, "${", startLine, startColumn, source.getEndColumn(), startPosition);
                }
            }

            // 普通文本
            text.append((char) source.read());
        }

        if (text.length() > 0) {
            return new Token(TokenType.TEXT, text.toString(), startLine, startColumn, source.getEndColumn(), startPosition);
        }

        return new Token(TokenType.EOF, startLine, startColumn, source.getEndColumn(), startPosition);
    }

    /**
     * 检查当前位置是否是 @raw{ 的开始
     *
     * @return 如果是 @raw{ 则返回 true
     * @throws IOException 如果读取源代码时发生 I/O 错误
     */
    private boolean isRawBlockStart() throws IOException {
        // 检查当前位置是否是 @raw{
        // 需要 peek(1) = 'r', peek(2) = 'a', peek(3) = 'w', peek(4) = '{'
        return source.peek() == '@' &&
               source.peek(1) == 'r' &&
               source.peek(2) == 'a' &&
               source.peek(3) == 'w' &&
               source.peek(4) == '{';
    }

    /**
     * 检查当前位置是否是 @raw: 的开始
     *
     * @return 如果是 @raw: 则返回 true
     * @throws IOException 如果读取源代码时发生 I/O 错误
     */
    private boolean isRawColonStart() throws IOException {
        // 检查当前位置是否是 @raw:
        // 需要 peek(1) = 'r', peek(2) = 'a', peek(3) = 'w', peek(4) = ':'
        return source.peek() == '@' &&
               source.peek(1) == 'r' &&
               source.peek(2) == 'a' &&
               source.peek(3) == 'w' &&
               source.peek(4) == ':';
    }

    /**
     * 读取 @raw{...} 块的内容
     * <p>
     * 支持嵌套括号，需要找到匹配的外层 } 才结束。
     * 例如：@raw{a{b{}c}d} 输出 a{b{}c}d
     * </p>
     *
     * @return 原始文本内容
     * @throws IOException 如果读取源代码时发生 I/O 错误
     * @throws TemplateSyntaxException 如果括号不匹配
     */
    private String readRawBlockContent() throws IOException {
        StringBuilder content = new StringBuilder();
        int braceDepth = 1; // 已经消费了 @raw{，所以深度为 1

        while (true) {
            int ch = source.peek();
            if (ch == -1) {
                throw new TemplateSyntaxException("Unclosed @raw block, missing matching '}'",
                    source.getLine(), source.getColumn(), source.getLine(), source.getEndColumn(), source.getSourceText());
            }

            if (ch == '{') {
                braceDepth++;
                content.append((char) source.read());
            } else if (ch == '}') {
                braceDepth--;
                if (braceDepth == 0) {
                    source.read(); // 消费匹配的 }
                    break;
                }
                content.append((char) source.read());
            } else {
                content.append((char) source.read());
            }
        }

        return content.toString();
    }

    /**
     * 读取 @raw: 后的内容
     * <p>
     * 冒号后的所有内容都是原始文本，直到文件结束。
     * </p>
     *
     * @return 原始文本内容
     * @throws IOException 如果读取源代码时发生 I/O 错误
     */
    private String readRawColonContent() throws IOException {
        StringBuilder content = new StringBuilder();

        while (true) {
            int ch = source.peek();
            if (ch == -1) {
                break;
            }
            content.append((char) source.read());
        }

        return content.toString();
    }

    /**
     * 判断字符是否可以作为表达式的开始
     * 
     * <p>用于区分单花括号 { 后面是否是表达式，避免与 JSON 等格式中的 { 混淆</p>
     * 
     * @param ch 待判断的字符
     * @return 如果字符可以作为表达式的开始则返回 true
     */
    private boolean isExpressionStart(int ch) {
        // 如果是引号、空格或标识符开始，需要进一步检查是否是 JSON 格式
        if (ch == '"' || ch == '\'' || Character.isWhitespace(ch) || Character.isJavaIdentifierStart(ch)) {
            return !looksLikeJson();
        }
        
        return Character.isDigit(ch) ||
               ch == '`' ||
               ch == '[' || ch == '(' ||
               ch == '-' || ch == '+' || ch == '!' ||
               ch == '@' || // JSONPath 当前节点（过滤表达式外使用会在求值期报错）
               ch == '/'; // 正则表达式
    }
    
    /**
     * 检查当前位置（{{ 之后）是否看起来像哈希表字面量
     * 
     * <p>哈希表字面量的特征：</p>
     * <ul>
     *   <li>{} - 空哈希表</li>
     *   <li>{key: value} - 标识符键</li>
     *   <li>{"key": value} - 字符串键</li> 
     *   <li>{123: value} - 数字键</li>
     * </ul>
     * <p>注意：调用此方法时，source 的当前位置在 {{ 之后</p>
     * 
     * @return 如果看起来像哈希表字面量则返回 true
     */
    private boolean looksLikeHashLiteral() {
        try {
            source.mark();
            
            // 跳过空白字符
            while (true) {
                int ch = source.peek();
                if (ch == -1 || !Character.isWhitespace(ch)) {
                    break;
                }
                source.read();
            }
            
            int ch = source.peek();
            
            // 空哈希表 {}
            if (ch == '}') {
                source.reset();
                return true;
            }
            
            // 检查键：可以是字符串、标识符或数字
            boolean isKey = false;
            
            if (ch == '"' || ch == '\'') {
                // 字符串键
                int quoteChar = ch;
                source.read(); // 消费开始引号
                
                // 扫描到键字符串结束
                while (true) {
                    ch = source.peek();
                    if (ch == -1) {
                        source.reset();
                        return false;
                    }
                    
                    if (ch == quoteChar) {
                        source.read(); // 消费结束引号
                        isKey = true;
                        break;
                    }
                    
                    if (ch == '\\') {
                        source.read(); // 消费反斜杠
                        int next = source.peek();
                        if (next != -1) {
                            source.read(); // 消费转义字符
                        }
                    } else {
                        source.read();
                    }
                }
            } else if (Character.isJavaIdentifierStart(ch)) {
                // 标识符键
                source.read(); // 消费第一个字符
                while (true) {
                    ch = source.peek();
                    if (ch == -1 || !Character.isJavaIdentifierPart(ch)) {
                        break;
                    }
                    source.read();
                }
                isKey = true;
            } else if (Character.isDigit(ch) || ch == '-') {
                // 数字键
                source.read(); // 消费第一个字符
                while (true) {
                    ch = source.peek();
                    if (ch == -1 || !(Character.isDigit(ch) || ch == '.' || ch == 'e' || ch == 'E')) {
                        break;
                    }
                    source.read();
                }
                isKey = true;
            }
            
            if (!isKey) {
                source.reset();
                return false;
            }
            
            // 跳过空白字符
            while (true) {
                ch = source.peek();
                if (ch == -1 || !Character.isWhitespace(ch)) {
                    break;
                }
                source.read();
            }
            
            // 检查是否有冒号
            if (ch != ':') {
                source.reset();
                return false;
            }
            
            // 有 key: 格式，认为是哈希表字面量
            source.reset();
            return true;
            
        } catch (Exception e) {
            try {
                source.reset();
            } catch (Exception ignored) {
            }
            return false;
        }
    }

    /**
     * 检查 { 后面的内容是否看起来像 JSON 格式
     * 
     * <p>JSON 文本的特征：</p>
     * <ul>
     *   <li>{"key": value} - 字符串键且有冒号</li>
     *   <li>{key: value} - 标识符键且有冒号（JavaScript 对象字面量）</li>
     * </ul>
     * <p>只要检测到键后跟冒号的格式，就认为是 JSON/对象字面量文本</p>
     * <p>注意：调用此方法时，source 的当前位置在 { 字符前面</p>
     * 
     * @return 如果看起来像 JSON 文本则返回 true
     */
    private boolean looksLikeJson() {
        try {
            source.mark();
            
            // 跳过 { 字符
            int ch = source.read();
            if (ch != '{') {
                source.reset();
                return false;
            }
            
            // 跳过空白字符
            while (true) {
                ch = source.peek();
                if (ch == -1 || !Character.isWhitespace(ch)) {
                    break;
                }
                source.read();
            }
            
            // 检查键：可以是字符串（引号）或标识符
            boolean isStringKey = (ch == '"' || ch == '\'');
            
            if (isStringKey) {
                // 字符串键
                int quoteChar = ch;
                source.read(); // 消费开始引号
                
                // 扫描到键字符串结束
                while (true) {
                    ch = source.peek();
                    if (ch == -1) {
                        source.reset();
                        return false;
                    }
                    
                    if (ch == quoteChar) {
                        source.read(); // 消费结束引号
                        break;
                    }
                    
                    if (ch == '\\') {
                        source.read(); // 消费反斜杠
                        int next = source.peek();
                        if (next != -1) {
                            source.read(); // 消费转义字符
                        }
                    } else {
                        source.read();
                    }
                }
            } else if (Character.isJavaIdentifierStart(ch)) {
                // 标识符键
                source.read(); // 消费第一个字符
                while (true) {
                    ch = source.peek();
                    if (ch == -1 || !Character.isJavaIdentifierPart(ch)) {
                        break;
                    }
                    source.read();
                }
            } else {
                // 既不是字符串也不是标识符，不是 JSON
                source.reset();
                return false;
            }
            
            // 跳过空白字符
            while (true) {
                ch = source.peek();
                if (ch == -1 || !Character.isWhitespace(ch)) {
                    break;
                }
                source.read();
            }
            
            // 检查是否有冒号
            if (ch != ':') {
                source.reset();
                return false;
            }
            
            // 有 key: 格式，认为是 JSON/对象字面量文本
            source.reset();
            return true;
            
        } catch (Exception e) {
            try {
                source.reset();
            } catch (Exception ignored) {
            }
            return false;
        }
    }

    /**
     * 读取表达式模式的 Token
     * 
     * <p>在表达式模式下（{} 或 {{}} 内部），识别以下内容：</p>
     * <ul>
     *   <li>字符串字面量（单引号、双引号）</li>
     *   <li>字符串模板字面量（反引号）</li>
     *   <li>数字字面量（整数、长整数、大整数、浮点数、科学计数法）</li>
     *   <li>正则表达式字面量（/pattern/flags）</li>
     *   <li>标识符和关键字</li>
     *   <li>运算符和分隔符</li>
     *   <li>表达式结束标记 } 或 }} → EXPR_END 或 RIGHT_BRACE，切换回文本模式</li>
     * </ul>
     * 
     * @return 表达式内的 Token
     * @throws IOException 如果读取源代码时发生 I/O 错误
     */
    private Token nextExpressionToken() throws IOException {
        // 如果有待输出的 LEFT_BRACE，先输出它
        if (pendingLeftBrace) {
            pendingLeftBrace = false;
            int line = source.getLine();
            int column = source.getColumn();
            int position = source.getPosition();
            braceDepth++;
            return wrapExpressionToken(new Token(TokenType.LEFT_BRACE, "{", line, column, source.getEndColumn(), position));
        }
        
        skipWhitespace();

        int ch = source.peek();
        if (ch == -1) {
            throw new TemplateSyntaxException("Unclosed expression", source.getLine(), source.getColumn(), source.getLine(), source.getEndColumn(), source.getSourceText());
        }

        int line = source.getLine();
        int column = source.getColumn();
        int position = source.getPosition();

        // 检查表达式结束
        if (ch == '}') {
            if (dollarBrace) {
                // 美元符号表达式模式
                source.read(); // 消费 }
                braceDepth--;
                if (braceDepth == 0) {
                    mode = inUrlContext ? Mode.URL : Mode.TEXT;
                    dollarBrace = false;
                    return wrapExpressionToken(new Token(TokenType.DOLLAR_BRACE_EXPR_END, "}", line, column, source.getEndColumn(), position));
                }
                return wrapExpressionToken(new Token(TokenType.RIGHT_BRACE, "}", line, column, source.getEndColumn(), position));
            } else if (doubleBrace) {
                // 双花括号模式
                if (braceDepth == 0) {
                    // braceDepth == 0，表示没有嵌套花括号，检查是否是 }} 表达式结束
                    int next = source.peek(1);
                    if (next == '}') {
                        // }} 作为整体的 DOUBLE_BRACE_EXPR_END
                        source.read(); // 消费第一个 }
                        source.read(); // 消费第二个 }
                        mode = inUrlContext ? Mode.URL : Mode.TEXT;
                        doubleBrace = false;
                        return wrapExpressionToken(new Token(TokenType.DOUBLE_BRACE_EXPR_END, "}}", line, column, source.getEndColumn(), position));
                    } else {
                        // 只有一个 }，是单独的 DOUBLE_BRACE_EXPR_END
                        source.read(); // 消费 }
                        mode = inUrlContext ? Mode.URL : Mode.TEXT;
                        doubleBrace = false;
                        return wrapExpressionToken(new Token(TokenType.SINGLE_BRACE_EXPR_END, "}", line, column, source.getEndColumn(), position));
                    }
                } else {
                    // braceDepth > 0，说明有嵌套的花括号（如哈希表字面量）
                    // 此时 } 是 RIGHT_BRACE
                    source.read(); // 消费 }
                    braceDepth--;
                    return wrapExpressionToken(new Token(TokenType.RIGHT_BRACE, "}", line, column, source.getEndColumn(), position));
                }
            } else {
                // 单花括号模式
                source.read(); // 消费 }
                braceDepth--;
                if (braceDepth == 0) {
                    mode = inUrlContext ? Mode.URL : Mode.TEXT;
                    return wrapExpressionToken(new Token(TokenType.SINGLE_BRACE_EXPR_END, "}", line, column, source.getEndColumn(), position));
                }
                return wrapExpressionToken(new Token(TokenType.RIGHT_BRACE, "}", line, column, source.getEndColumn(), position));
            }
        }

        // 字符串字面量
        if (ch == '"' || ch == '\'') {
            return wrapExpressionToken(readStringLiteral());
        }

        // 字符串模板字面量
        if (ch == '`') {
            return wrapExpressionToken(readTemplateLiteral());
        }

        // 数字字面量
        if (Character.isDigit(ch) || (ch == '-' && Character.isDigit(source.peek(1)))) {
            return wrapExpressionToken(readNumberLiteral());
        }

        // 正则表达式（需要在特定上下文中识别）
        if (ch == '/' && isRegexContext()) {
            Token regex = tryReadRegex();
            if (regex != null) {
                return wrapExpressionToken(regex);
            }
        }

        // 标识符或关键字
        if (Character.isJavaIdentifierStart(ch)) {
            // 特殊处理：$后跟数字是参数变量引用 $0, $1 等
            if (ch == '$') {
                int next = source.peek(1);
                if (next >= '0' && next <= '9') {
                    return wrapExpressionToken(readDollarNumber());
                }
            }
            return wrapExpressionToken(readIdentifierOrKeyword());
        }

        // 运算符和分隔符
        return wrapExpressionToken(readOperatorOrDelimiter());
    }

    /**
     * 读取Eval模式的 Token
     *
     * <p>Eval模式用于解析纯表达式/语句（用于 Eval.eval()），与表达式模式类似但不要求闭合的 }。</p>
     * <p>在Eval模式下，识别以下内容：</p>
     * <ul>
     *   <li>标识符和关键字（if、for、let、return 等）</li>
     *   <li>字符串字面量</li>
     *   <li>数字字面量</li>
     *   <li>运算符和分隔符</li>
     *   <li>左花括号 { → 用于代码块</li>
     *   <li>右花括号 } → 用于代码块结束</li>
     *   <li>EOF → 解析结束</li>
     * </ul>
     *
     * @return Eval模式下的 Token
     * @throws IOException 如果读取源代码时发生 I/O 错误
     */
    private Token nextEvalToken() throws IOException {
        // 如果有待输出的 LEFT_BRACE，先输出它
        if (pendingLeftBrace) {
            pendingLeftBrace = false;
            int line = source.getLine();
            int column = source.getColumn();
            int position = source.getPosition();
            braceDepth++;
            return wrapExpressionToken(new Token(TokenType.LEFT_BRACE, "{", line, column, source.getEndColumn(), position));
        }

        skipWhitespace();

        int ch = source.peek();
        if (ch == -1) {
            // Eval模式下，EOF是正常的结束
            return new Token(TokenType.EOF, source.getLine(), source.getColumn(), source.getEndColumn(), source.getPosition());
        }

        int line = source.getLine();
        int column = source.getColumn();
        int position = source.getPosition();

        // 处理 } 作为普通的花括号（用于代码块）
        if (ch == '}') {
            source.read(); // 消费 }
            if (braceDepth > 0) {
                braceDepth--;
            }
            return wrapExpressionToken(new Token(TokenType.RIGHT_BRACE, "}", line, column, source.getEndColumn(), position));
        }

        // 字符串字面量
        if (ch == '"' || ch == '\'') {
            return wrapExpressionToken(readStringLiteral());
        }

        // 字符串模板字面量
        if (ch == '`') {
            return wrapExpressionToken(readTemplateLiteral());
        }

        // 数字字面量
        if (Character.isDigit(ch) || (ch == '-' && Character.isDigit(source.peek(1)))) {
            return wrapExpressionToken(readNumberLiteral());
        }

        // 正则表达式（需要在特定上下文中识别）
        if (ch == '/' && isRegexContext()) {
            Token regex = tryReadRegex();
            if (regex != null) {
                return wrapExpressionToken(regex);
            }
        }

        // 标识符或关键字
        if (Character.isJavaIdentifierStart(ch)) {
            // 特殊处理：$后跟数字是参数变量引用 $0, $1 等
            if (ch == '$') {
                int next = source.peek(1);
                if (next >= '0' && next <= '9') {
                    source.read(); // 消费 $
                    StringBuilder num = new StringBuilder();
                    while (source.peek() >= '0' && source.peek() <= '9') {
                        num.append((char) source.read());
                    }
                    return wrapExpressionToken(new Token(TokenType.NUMBER, num.toString(), line, column, source.getEndColumn(), position));
                }
            }

            return wrapExpressionToken(readIdentifierOrKeyword());
        }

        // 运算符和分隔符
        return wrapExpressionToken(readOperatorOrDelimiter());
    }

    /**
     * 读取语句块模式的 Token
     * 
     * <p>在语句块模式下（{% %} 内部），识别以下内容：</p>
     * <ul>
     *   <li>标识符和关键字（if、for、let、extends、import 等）</li>
     *   <li>字符串字面量（用于 extends 路径、import 类名等）</li>
     *   <li>数字字面量</li>
     *   <li>运算符和分隔符</li>
     *   <li>语句块结束标记 %} → STMT_END，切换回文本模式</li>
     *   <li>标准语句块开始标签结束 } → RIGHT_BRACE</li>
     * </ul>
     * 
     * @return 语句块内的 Token
     * @throws IOException 如果读取源代码时发生 I/O 错误
     */
    private Token nextStatementToken() throws IOException {
        skipWhitespace();

        int ch = source.peek();
        if (ch == -1) {
            throw new TemplateSyntaxException("Unclosed statement block", source.getLine(), source.getColumn(), source.getLine(), source.getEndColumn(), source.getSourceText());
        }

        int line = source.getLine();
        int column = source.getColumn();
        int position = source.getPosition();

        // 检查自闭合语句块结束 %}
        if (ch == '%') {
            int next = source.peek(1);
            if (next == '}') {
                source.read(); // 消费 %
                source.read(); // 消费 }
                mode = inUrlContext ? Mode.URL : Mode.TEXT;
                return new Token(TokenType.STMT_END, "%}", line, column, source.getEndColumn(), position);
            }
        }

        // 检查标准语句块开始标签结束 }
        // 注意：标准语句块格式为 {% 语句名称 参数 }，最后的 } 表示开始标签结束
        if (ch == '}') {
            if (braceDepth > 0) {
                // 如果在花括号内部（例如哈希表字面量或代码块），} 只是普通的右花括号
                source.read(); // 消费 }
                braceDepth--;
                return new Token(TokenType.RIGHT_BRACE, "}", line, column, source.getEndColumn(), position);
            }
            source.read(); // 消费 }
            mode = inUrlContext ? Mode.URL : Mode.TEXT;
            return new Token(TokenType.RIGHT_BRACE, "}", line, column, source.getEndColumn(), position);
        }

        // 标识符或关键字
        if (Character.isJavaIdentifierStart(ch)) {
            return readIdentifierOrKeyword();
        }

        // 字符串字面量
        if (ch == '"' || ch == '\'') {
            return readStringLiteral();
        }

        // 字符串模板字面量
        if (ch == '`') {
            return readTemplateLiteral();
        }

        // 数字字面量
        if (Character.isDigit(ch)) {
            return readNumberLiteral();
        }

        // 运算符和分隔符
        return readOperatorOrDelimiter();
    }

    /**
     * 读取URL模式的Token
     *
     * <p>在URL模式下，识别以下内容：</p>
     * <ul>
     *   <li>普通文本内容 → TEXT</li>
     *   <li>表达式开始标记 { 或 {{ 或 ${ → 进入表达式模式</li>
     *   <li>语句块开始标记 {% → 进入语句块模式</li>
     *   <li>右括号 ) → 退出URL模式</li>
     * </ul>
     *
     * @return URL内的Token
     * @throws IOException 如果读取源代码时发生I/O错误
     */
    private Token nextUrlToken() throws IOException {
        StringBuilder text = new StringBuilder();
        int startLine = source.getLine();
        int startColumn = source.getColumn();
        int startPosition = source.getPosition();

        while (true) {
            int ch = source.peek();
            if (ch == -1) {
                break;
            }

            // 处理转义括号 \{ 和 \}
            if (ch == '\\') {
                int next = source.peek(1);
                if (next == '{' || next == '}') {
                    source.read(); // 消费 \
                    text.append((char) source.read()); // 添加转义后的字符
                    continue;
                }
            }

            // 检查右括号 )
            if (ch == ')') {
                // 先输出已累积的文本
                if (text.length() > 0) {
                    return new Token(TokenType.TEXT, text.toString(), startLine, startColumn, source.getEndColumn(), startPosition);
                }
                source.read(); // 消费 )
                urlParenDepth--;
                if (urlParenDepth == 0) {
                    // 退出URL模式，返回之前的模式
                    mode = previousMode != null ? previousMode : Mode.EXPRESSION;
                    previousMode = null;
                    inUrlContext = false;
                    braceDepth = savedBraceDepth; // 恢复之前保存的braceDepth
                }
                return new Token(TokenType.RIGHT_PAREN, ")", startLine, startColumn, source.getEndColumn(), startPosition);
            }

            // 检查左括号 ( (嵌套)
            if (ch == '(') {
                // 先输出已累积的文本
                if (text.length() > 0) {
                    return new Token(TokenType.TEXT, text.toString(), startLine, startColumn, source.getEndColumn(), startPosition);
                }
                source.read(); // 消费 (
                urlParenDepth++;
                return new Token(TokenType.LEFT_PAREN, "(", startLine, startColumn, source.getEndColumn(), startPosition);
            }

            // 检查是否是表达式开始 {、{{ 或 ${
            if (ch == '{') {
                int next = source.peek(1);
                if (next == '%') {
                    // 语句块开始 {%
                    if (text.length() > 0) {
                        return new Token(TokenType.TEXT, text.toString(), startLine, startColumn, source.getEndColumn(), startPosition);
                    }
                    source.read(); // 消费 {
                    source.read(); // 消费 %
                    mode = Mode.STATEMENT;
                    return new Token(TokenType.STMT_BEGIN, "{%", startLine, startColumn, source.getEndColumn(), startPosition);
                } else if (next == '/' && source.peek(2) == '%') {
                    // 语句块关闭 {/%
                    if (text.length() > 0) {
                        return new Token(TokenType.TEXT, text.toString(), startLine, startColumn, source.getEndColumn(), startPosition);
                    }
                    source.read(); // {
                    source.read(); // /
                    source.read(); // %
                    mode = Mode.STATEMENT;
                    return new Token(TokenType.STMT_CLOSE, "{/%", startLine, startColumn, source.getEndColumn(), startPosition);
                } else if (next == '{') {
                    // 双花括号 {{
                    if (text.length() > 0) {
                        return new Token(TokenType.TEXT, text.toString(), startLine, startColumn, source.getEndColumn(), startPosition);
                    }
                    source.read(); // 消费第一个 {
                    source.read(); // 消费第二个 {
                    mode = Mode.EXPRESSION;
                    doubleBrace = true;
                    dollarBrace = false;
                    braceDepth = 0;
                    lastExpressionTokenType = null;
                    return new Token(TokenType.DOUBLE_BRACE_EXPR_BEGIN, "{{", startLine, startColumn, source.getEndColumn(), startPosition);
                } else if (isExpressionStart(next)) {
                    // 单花括号表达式 {
                    if (text.length() > 0) {
                        return new Token(TokenType.TEXT, text.toString(), startLine, startColumn, source.getEndColumn(), startPosition);
                    }
                    source.read(); // 消费 {
                    mode = Mode.EXPRESSION;
                    doubleBrace = false;
                    dollarBrace = false;
                    braceDepth = 1;
                    lastExpressionTokenType = null;
                    return new Token(TokenType.SINGLE_BRACE_EXPR_BEGIN, "{", startLine, startColumn, source.getEndColumn(), startPosition);
                }
            } else if (ch == '$') {
                // 检查是否是美元符号表达式 ${
                int next = source.peek(1);
                if (next == '{') {
                    if (text.length() > 0) {
                        return new Token(TokenType.TEXT, text.toString(), startLine, startColumn, source.getEndColumn(), startPosition);
                    }
                    source.read(); // 消费 $
                    source.read(); // 消费 {
                    mode = Mode.EXPRESSION;
                    doubleBrace = false;
                    dollarBrace = true;
                    braceDepth = 1;
                    lastExpressionTokenType = null;
                    return new Token(TokenType.DOLLAR_BRACE_EXPR_BEGIN, "${", startLine, startColumn, source.getEndColumn(), startPosition);
                }
            }

            // 普通文本
            text.append((char) source.read());
        }

        if (text.length() > 0) {
            return new Token(TokenType.TEXT, text.toString(), startLine, startColumn, source.getEndColumn(), startPosition);
        }

        return new Token(TokenType.EOF, startLine, startColumn, source.getEndColumn(), startPosition);
    }

    /**
     * 跳过空白字符
     * 
     * <p>在表达式和语句块模式下，空白字符不参与词法分析，需要跳过</p>
     * 
     * @throws IOException 如果读取源代码时发生 I/O 错误
     */
    private void skipWhitespace() throws IOException {
        while (true) {
            int ch = source.peek();
            if (ch == -1 || !Character.isWhitespace(ch)) {
                break;
            }
            source.read();
        }
    }

    /**
     * 读取字符串字面量（单引号或双引号）
     * 
     * <p>支持的转义字符：\n、\t、\r、\\、\'、\"</p>
     * 
     * @return STRING 类型的 Token
     * @throws IOException 如果读取源代码时发生 I/O 错误
     * @throws TemplateSyntaxException 如果字符串未闭合
     */
    private Token readStringLiteral() throws IOException {
        int line = source.getLine();
        int column = source.getColumn();
        int position = source.getPosition();

        int quote = source.read(); // 消费引号
        StringBuilder value = new StringBuilder();

        while (true) {
            int ch = source.peek();
            if (ch == -1) {
                throw new TemplateSyntaxException("Unclosed string", line, column, line, source.getEndColumn(), source.getSourceText());
            }

            if (ch == quote) {
                source.read(); // 消费结束引号
                break;
            }

            if (ch == '\\') {
                source.read(); // 消费 \
                int next = source.peek();
                if (next == -1) {
                    throw new TemplateSyntaxException("Unclosed string", line, column, line, source.getEndColumn(), source.getSourceText());
                }
                source.read();
                // 处理转义字符
                switch (next) {
                    case 'n': value.append('\n'); break;
                    case 't': value.append('\t'); break;
                    case 'r': value.append('\r'); break;
                    case '\\': value.append('\\'); break;
                    case '\'': value.append('\''); break;
                    case '"': value.append('"'); break;
                    default: value.append((char) next); break;
                }
            } else {
                value.append((char) source.read());
            }
        }

        return new Token(TokenType.STRING, value.toString(), line, column, source.getEndColumn(), position);
    }

    /**
     * 读取字符串模板字面量（反引号包裹）
     * 
     * <p>字符串模板可以包含嵌入的表达式 {...}，例如：`Hello {name}!`</p>
     * 
     * @return TEMPLATE_STRING 类型的 Token
     * @throws IOException 如果读取源代码时发生 I/O 错误
     * @throws TemplateSyntaxException 如果字符串模板未闭合
     */
    private Token readTemplateLiteral() throws IOException {
        int line = source.getLine();
        int column = source.getColumn();
        int position = source.getPosition();

        source.read(); // 消费 `
        StringBuilder value = new StringBuilder();

        while (true) {
            int ch = source.peek();
            if (ch == -1) {
                throw new TemplateSyntaxException("Unclosed template string", line, column, line, source.getEndColumn(), source.getSourceText());
            }

            if (ch == '`') {
                source.read(); // 消费结束 `
                break;
            }

            value.append((char) source.read());
        }

        return new Token(TokenType.TEMPLATE_STRING, value.toString(), line, column, source.getEndColumn(), position);
    }

    /**
     * 读取数字字面量
     * 
     * <p>支持的数字类型：</p>
     * <ul>
     *   <li>整数：123、-456</li>
     *   <li>长整数：123L、-456L</li>
     *   <li>大整数：超出 long 范围的整数自动使用 BigInteger</li>
     *   <li>浮点数：3.14、-0.5</li>
     *   <li>科学计数法：1.23e10、1.5E-8</li>
     *   <li>高精度小数：小数位数超过 10 位时自动使用 BigDecimal</li>
     * </ul>
     * 
     * @return NUMBER、LONG、BIG_INTEGER 或 BIG_DECIMAL 类型的 Token
     * @throws IOException 如果读取源代码时发生 I/O 错误
     */
    private Token readNumberLiteral() throws IOException {
        int line = source.getLine();
        int column = source.getColumn();
        int position = source.getPosition();

        StringBuilder value = new StringBuilder();
        boolean hasDecimalPoint = false;
        boolean hasExponent = false;

        // 处理负号
        if (source.peek() == '-') {
            value.append((char) source.read());
        }

        // 读取数字
        while (true) {
            int ch = source.peek();
            if (ch == -1) {
                break;
            }

            if (Character.isDigit(ch)) {
                value.append((char) source.read());
            } else if (ch == '.' && !hasDecimalPoint && !hasExponent) {
                int next = source.peek(1);
                if (next == '.' || next == '<') {
                    // 这是区间运算符，不是小数点
                    break;
                }
                hasDecimalPoint = true;
                value.append((char) source.read());
            } else if ((ch == 'e' || ch == 'E') && !hasExponent) {
                hasExponent = true;
                hasDecimalPoint = true; // 科学计数法也算浮点数
                value.append((char) source.read());
                int next = source.peek();
                if (next == '+' || next == '-') {
                    value.append((char) source.read());
                }
            } else if (ch == 'L' && !hasDecimalPoint && !hasExponent) {
                source.read(); // 消费 L
                try {
                    Long.parseLong(value.toString());
                    return new Token(TokenType.LONG, value.toString(), line, column, source.getEndColumn(), position);
                } catch (NumberFormatException e) {
                    // 超出 long 范围，使用 BigInteger
                    return new Token(TokenType.BIG_INTEGER, value.toString(), line, column, source.getEndColumn(), position);
                }
            } else {
                break;
            }
        }

        String numStr = value.toString();

        // 判断数字类型
        if (hasDecimalPoint || hasExponent) {
            // 浮点数
            try {
                Double.parseDouble(numStr);
                // 检查精度，如果精度要求高则使用 BigDecimal
                if (numStr.contains(".") && numStr.substring(numStr.indexOf(".") + 1).length() > 10) {
                    return new Token(TokenType.BIG_DECIMAL, numStr, line, column, source.getEndColumn(), position);
                }
                return new Token(TokenType.NUMBER, numStr, line, column, source.getEndColumn(), position);
            } catch (NumberFormatException e) {
                return new Token(TokenType.BIG_DECIMAL, numStr, line, column, source.getEndColumn(), position);
            }
        } else {
            // 整数
            try {
                Integer.parseInt(numStr);
                return new Token(TokenType.NUMBER, numStr, line, column, source.getEndColumn(), position);
            } catch (NumberFormatException e) {
                try {
                    Long.parseLong(numStr);
                    return new Token(TokenType.LONG, numStr, line, column, source.getEndColumn(), position);
                } catch (NumberFormatException e2) {
                    return new Token(TokenType.BIG_INTEGER, numStr, line, column, source.getEndColumn(), position);
                }
            }
        }
    }

    /**
     * 尝试读取正则表达式字面量
     * 
     * <p>格式：/pattern/ 或 /pattern/flags</p>
     * <p>支持的标志位：g（全局）、i（忽略大小写）、m（多行）</p>
     * <p>注意：需要区分正则表达式和除法运算符 /，通过上下文判断</p>
     * 
     * @return REGEX 类型的 Token，如果不是正则表达式则返回 null
     * @throws IOException 如果读取源代码时发生 I/O 错误
     */
    private Token tryReadRegex() throws IOException {
        source.mark();
        int line = source.getLine();
        int column = source.getColumn();
        int position = source.getPosition();

        try {
            source.read(); // 消费 /
            StringBuilder pattern = new StringBuilder();

            while (true) {
                int ch = source.peek();
                if (ch == -1 || ch == '\n') {
                    source.reset();
                    return null;
                }

                if (ch == '/') {
                    source.read(); // 消费结束 /
                    // 读取标志位
                    StringBuilder flags = new StringBuilder();
                    while (true) {
                        int flag = source.peek();
                        if (flag == 'g' || flag == 'i' || flag == 'm') {
                            flags.append((char) source.read());
                        } else {
                            break;
                        }
                    }
                    // 标志位后紧跟其他字母属于非法标志位（如 /abc/x），给出明确的编译错误
                    int next = source.peek();
                    if (next != -1 && Character.isLetter(next)) {
                        throw new TemplateSyntaxException("Invalid regex flag: '" + (char) next + "' (supported flags: g, i, m)",
                            source.getLine(), source.getColumn(), source.getLine(), source.getEndColumn(), source.getSourceText());
                    }
                    String value = pattern.toString() + (flags.length() > 0 ? ":" + flags : "");
                    return new Token(TokenType.REGEX, value, line, column, source.getEndColumn(), position, flags.toString());
                }

                if (ch == '\\') {
                    pattern.append((char) source.read());
                    int next = source.peek();
                    if (next != -1) {
                        pattern.append((char) source.read());
                    }
                } else {
                    pattern.append((char) source.read());
                }
            }
        } catch (TemplateSyntaxException e) {
            // 非法标志位等语法错误直接抛出，不回退为除法解析
            throw e;
        } catch (Exception e) {
            source.reset();
            return null;
        }
    }

    /**
     * 读取标识符或关键字
     *
     * <p>标识符遵循 Java 命名规则，关键字包括：</p>
     * <ul>
     *   <li>控制流：if、else、for、in、switch、case、default</li>
     *   <li>字面量：true、false、null</li>
     *   <li>嵌入式代码：let、extends、import</li>
     *   <li>运算符：!in</li>
     * </ul>
     * 
     * @return IDENTIFIER 或对应的关键字 Token
     * @throws IOException 如果读取源代码时发生 I/O 错误
     */
    private Token readIdentifierOrKeyword() throws IOException {
        int line = source.getLine();
        int column = source.getColumn();
        int position = source.getPosition();

        StringBuilder value = new StringBuilder();

        // 第一个字符
        value.append((char) source.read());

        // 后续字符
        while (true) {
            int ch = source.peek();
            if (ch == -1 || !Character.isJavaIdentifierPart(ch)) {
                break;
            }
            value.append((char) source.read());
        }

        String identifier = value.toString();

        // 检查是否是关键字
        Integer keywordType = KEYWORDS.get(identifier);
        if (keywordType != null) {
            return new Token(keywordType, identifier, line, column, source.getEndColumn(), position);
        }

        // 特殊处理：如果标识符是 "url" 且后面跟着 "("，则返回 URL token
        if ("url".equals(identifier) && source.peek() == '(') {
            return new Token(TokenType.URL, identifier, line, column, source.getEndColumn(), position);
        }

        return new Token(TokenType.IDENTIFIER, identifier, line, column, source.getEndColumn(), position);
    }

    /**
     * 读取参数变量引用（$0、$1 等）
     *
     * <p>参数变量引用格式为 $ 后跟一个或多个数字，例如：</p>
     * <ul>
     *   <li>$0 - 引用第一个参数</li>
     *   <li>$1 - 引用第二个参数</li>
     *   <li>$10 - 引用第十一个参数</li>
     * </ul>
     *
     * @return DOLLAR_NUMBER 类型的 Token
     * @throws IOException 如果读取源代码时发生 I/O 错误
     */
    private Token readDollarNumber() throws IOException {
        int line = source.getLine();
        int column = source.getColumn();
        int position = source.getPosition();

        StringBuilder value = new StringBuilder();

        // 消费 $
        value.append((char) source.read());

        // 读取后续数字
        while (true) {
            int ch = source.peek();
            if (ch < '0' || ch > '9') {
                break;
            }
            value.append((char) source.read());
        }

        return new Token(TokenType.DOLLAR_NUMBER, value.toString(), line, column, source.getEndColumn(), position);
    }

    /**
     * 读取运算符或分隔符
     * 
     * <p>支持的运算符和分隔符：</p>
     * <ul>
     *   <li>分隔符：()、[]、{}、,、;、.</li>
     *   <li>算术运算符：+、-、*、/、%</li>
     *   <li>比较运算符：>、<、>=、<=、==、!=</li>
     *   <li>逻辑运算符：&&、||、!</li>
     *   <li>三元运算符：?、:</li>
     *   <li>赋值运算符：=</li>
     *   <li>空安全操作符：?.</li>
     *   <li>Elvis 运算符：?:</li>
     *   <li>正则匹配：=~、!=~</li>
     *   <li>区间运算符：..、>..<、..<、>..</li>
     *   <li>不包含运算符：!in</li>
     * </ul>
     * 
     * @return 对应的运算符或分隔符 Token
     * @throws IOException 如果读取源代码时发生 I/O 错误
     * @throws TemplateSyntaxException 如果遇到未知字符
     */
    private Token readOperatorOrDelimiter() throws IOException {
        int line = source.getLine();
        int column = source.getColumn();
        int position = source.getPosition();

        int ch = source.read();

        switch (ch) {
            case '(': return new Token(TokenType.LEFT_PAREN, "(", line, column, source.getEndColumn(), position);
            case ')': return new Token(TokenType.RIGHT_PAREN, ")", line, column, source.getEndColumn(), position);
            case '[': return new Token(TokenType.LEFT_BRACKET, "[", line, column, source.getEndColumn(), position);
            case ']': return new Token(TokenType.RIGHT_BRACKET, "]", line, column, source.getEndColumn(), position);
            case '{': 
                braceDepth++;
                return new Token(TokenType.LEFT_BRACE, "{", line, column, source.getEndColumn(), position);
            case ',': return new Token(TokenType.COMMA, ",", line, column, source.getEndColumn(), position);
            case ';': return new Token(TokenType.SEMICOLON, ";", line, column, source.getEndColumn(), position);
            case '@': return new Token(TokenType.AT, "@", line, column, source.getEndColumn(), position);
            case '`': return new Token(TokenType.BACKTICK, "`", line, column, source.getEndColumn(), position);

            case '+':
                if (source.peek() == '+') {
                    source.read();
                    return new Token(TokenType.INCREMENT, "++", line, column, source.getEndColumn(), position);
                } else if (source.peek() == '=') {
                    source.read();
                    return new Token(TokenType.PLUS_ASSIGN, "+=", line, column, source.getEndColumn(), position);
                }
                return new Token(TokenType.PLUS, "+", line, column, source.getEndColumn(), position);
            case '-':
                if (source.peek() == '-') {
                    source.read();
                    return new Token(TokenType.DECREMENT, "--", line, column, source.getEndColumn(), position);
                } else if (source.peek() == '>') {
                    source.read();
                    return new Token(TokenType.ARROW, "->", line, column, source.getEndColumn(), position);
                } else if (source.peek() == '=') {
                    source.read();
                    return new Token(TokenType.MINUS_ASSIGN, "-=", line, column, source.getEndColumn(), position);
                }
                return new Token(TokenType.MINUS, "-", line, column, source.getEndColumn(), position);
            case '*':
                if (source.peek() == '=') {
                    source.read();
                    return new Token(TokenType.MULTIPLY_ASSIGN, "*=", line, column, source.getEndColumn(), position);
                }
                return new Token(TokenType.MULTIPLY, "*", line, column, source.getEndColumn(), position);
            case '%':
                if (source.peek() == '=') {
                    source.read();
                    return new Token(TokenType.MODULO_ASSIGN, "%=", line, column, source.getEndColumn(), position);
                }
                return new Token(TokenType.MODULO, "%", line, column, source.getEndColumn(), position);

            case '/':
                if (source.peek() == '=') {
                    source.read();
                    return new Token(TokenType.DIVIDE_ASSIGN, "/=", line, column, source.getEndColumn(), position);
                }
                return new Token(TokenType.DIVIDE, "/", line, column, source.getEndColumn(), position);

            case '=':
                if (source.peek() == '=') {
                    source.read();
                    return new Token(TokenType.EQ, "==", line, column, source.getEndColumn(), position);
                } else if (source.peek() == '~') {
                    source.read();
                    return new Token(TokenType.REGEX_MATCH, "=~", line, column, source.getEndColumn(), position);
                }
                return new Token(TokenType.ASSIGN, "=", line, column, source.getEndColumn(), position);

            case '!':
                if (source.peek() == '=') {
                    source.read();
                    if (source.peek() == '~') {
                        source.read();
                        return new Token(TokenType.REGEX_NOT_MATCH, "!=~", line, column, source.getEndColumn(), position);
                    }
                    return new Token(TokenType.NEQ, "!=", line, column, source.getEndColumn(), position);
                } else if (source.peek() == 'i') {
                    // 检查 !in
                    source.mark();
                    source.read(); // i
                    if (source.peek() == 'n') {
                        source.read(); // n
                        int next = source.peek();
                        if (!Character.isJavaIdentifierPart(next)) {
                            return new Token(TokenType.NOT_IN, "!in", line, column, source.getEndColumn(), position);
                        }
                    }
                    source.reset();
                }
                return new Token(TokenType.NOT, "!", line, column, source.getEndColumn(), position);

            case '<':
                if (source.peek() == '=') {
                    source.read();
                    return new Token(TokenType.LTE, "<=", line, column, source.getEndColumn(), position);
                } else if (source.peek() == '<') {
                    source.read();
                    if (source.peek() == '=') {
                        source.read();
                        return new Token(TokenType.LEFT_SHIFT_ASSIGN, "<<=", line, column, source.getEndColumn(), position);
                    }
                    throw new TemplateSyntaxException("Unexpected token: <<", line, column, line, source.getEndColumn(), source.getSourceText());
                }
                return new Token(TokenType.LT, "<", line, column, source.getEndColumn(), position);

            case '>':
                if (source.peek() == '=') {
                    source.read();
                    return new Token(TokenType.GTE, ">=", line, column, source.getEndColumn(), position);
                } else if (source.peek() == '>') {
                    source.read();
                    if (source.peek() == '>') {
                        source.read();
                        if (source.peek() == '=') {
                            source.read();
                            return new Token(TokenType.UNSIGNED_RIGHT_SHIFT_ASSIGN, ">>>=", line, column, source.getEndColumn(), position);
                        }
                        throw new TemplateSyntaxException("Unexpected token: >>>", line, column, line, source.getEndColumn(), source.getSourceText());
                    } else if (source.peek() == '=') {
                        source.read();
                        return new Token(TokenType.RIGHT_SHIFT_ASSIGN, ">>=", line, column, source.getEndColumn(), position);
                    }
                    throw new TemplateSyntaxException("Unexpected token: >>", line, column, line, source.getEndColumn(), source.getSourceText());
                } else if (source.peek() == '.') {
                    source.mark();
                    source.read();
                    if (source.peek() == '.') {
                        source.read();
                        if (source.peek() == '<') {
                            source.read();
                            return new Token(TokenType.RANGE_OPEN, ">..<", line, column, source.getEndColumn(), position);
                        }
                        return new Token(TokenType.RANGE_LEFT_OPEN_RIGHT_CLOSED, ">..", line, column, source.getEndColumn(), position);
                    }
                    source.reset();
                }
                return new Token(TokenType.GT, ">", line, column, source.getEndColumn(), position);

            case '.':
                if (source.peek() == '.') {
                    source.read();
                    if (source.peek() == '<') {
                        // ..< 前闭后开区间 [a, b)
                        source.read();
                        return new Token(TokenType.RANGE_LEFT_CLOSED_RIGHT_OPEN, "..<", line, column, source.getEndColumn(), position);
                    }
                    // .. 全闭区间 [a, b]
                    return new Token(TokenType.RANGE_CLOSED, "..", line, column, source.getEndColumn(), position);
                }
                return new Token(TokenType.DOT, ".", line, column, source.getEndColumn(), position);

            case '&':
                if (source.peek() == '&') {
                    source.read();
                    return new Token(TokenType.AND, "&&", line, column, source.getEndColumn(), position);
                } else if (source.peek() == '=') {
                    source.read();
                    return new Token(TokenType.BIT_AND_ASSIGN, "&=", line, column, source.getEndColumn(), position);
                }
                throw new TemplateSyntaxException("Unknown character: &", line, column, line, source.getEndColumn(), source.getSourceText());

            case '|':
                if (source.peek() == '|') {
                    source.read();
                    return new Token(TokenType.OR, "||", line, column, source.getEndColumn(), position);
                } else if (source.peek() == '=') {
                    source.read();
                    return new Token(TokenType.BIT_OR_ASSIGN, "|=", line, column, source.getEndColumn(), position);
                }
                throw new TemplateSyntaxException("Unknown character: |", line, column, line, source.getEndColumn(), source.getSourceText());

            case '^':
                if (source.peek() == '=') {
                    source.read();
                    return new Token(TokenType.BIT_XOR_ASSIGN, "^=", line, column, source.getEndColumn(), position);
                }
                throw new TemplateSyntaxException("Unknown character: ^", line, column, line, source.getEndColumn(), source.getSourceText());

            case '?':
                if (source.peek() == '.') {
                    source.read();
                    return new Token(TokenType.SAFE_NAV, "?.", line, column, source.getEndColumn(), position);
                } else if (source.peek() == '?') {
                    source.read();
                    return new Token(TokenType.ELVIS, "??", line, column, source.getEndColumn(), position);
                }
                return new Token(TokenType.QUESTION, "?", line, column, source.getEndColumn(), position);

            case ':':
                return new Token(TokenType.COLON, ":", line, column, source.getEndColumn(), position);

            default:
                throw new TemplateSyntaxException("Unknown character: " + (char) ch, line, column, line, source.getEndColumn(), source.getSourceText());
        }
    }
}

