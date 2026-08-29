package org.dromara.easytl.parser;

/**
 * Token 类型定义（使用 int 常量）
 * 
 * <p>使用 int 魔数表示 Token 类型，避免枚举的性能开销</p>
 *
 * @author EasyTL Team
 */
public final class TokenType {

    // ==================== 特殊 ====================
    public static final int EOF = 0;                // 文件结束
    public static final int TEXT = 1;               // 普通文本内容

    // ==================== 分隔符 ====================
    public static final int LEFT_BRACE = 10;        // {
    public static final int RIGHT_BRACE = 11;       // }
    public static final int LEFT_PAREN = 12;        // (
    public static final int RIGHT_PAREN = 13;       // )
    public static final int LEFT_BRACKET = 14;      // [
    public static final int RIGHT_BRACKET = 15;     // ]
    public static final int COMMA = 16;             // ,
    public static final int DOT = 17;               // .
    public static final int SEMICOLON = 18;         // ;
    public static final int BACKTICK = 19;          // `（字符串模板）
    public static final int SLASH = 20;             // /（正则表达式和除法共用）

    // ==================== 基本运算符 ====================
    public static final int PLUS = 30;              // +
    public static final int MINUS = 31;             // -
    public static final int MULTIPLY = 32;          // *
    public static final int DIVIDE = 33;            // /
    public static final int MODULO = 34;            // %
    public static final int GT = 35;                // >
    public static final int LT = 36;                // <
    public static final int GTE = 37;               // >=
    public static final int LTE = 38;               // <=
    public static final int EQ = 39;                // ==
    public static final int NEQ = 40;               // !=
    public static final int AND = 41;               // &&
    public static final int OR = 42;                // ||
    public static final int NOT = 43;               // !
    public static final int QUESTION = 44;          // ?
    public static final int COLON = 45;             // :
    public static final int ASSIGN = 46;            // =

    // ==================== 自增/自减运算符 ====================
    public static final int INCREMENT = 47;               // ++
    public static final int DECREMENT = 48;               // --

    // ==================== 复合赋值运算符 ====================
    public static final int PLUS_ASSIGN = 59;             // +=
    public static final int MINUS_ASSIGN = 60;            // -=
    public static final int MULTIPLY_ASSIGN = 61;         // *=
    public static final int DIVIDE_ASSIGN = 62;           // /=
    public static final int MODULO_ASSIGN = 63;           // %=
    public static final int BIT_AND_ASSIGN = 64;          // &=
    public static final int BIT_OR_ASSIGN = 65;           // |=
    public static final int BIT_XOR_ASSIGN = 66;          // ^=
    public static final int LEFT_SHIFT_ASSIGN = 67;       // <<=
    public static final int RIGHT_SHIFT_ASSIGN = 68;      // >>=
    public static final int UNSIGNED_RIGHT_SHIFT_ASSIGN = 69;  // >>>=

    // ==================== 高级运算符 ====================
    public static final int SAFE_NAV = 50;                      // ?. 空安全操作符
    public static final int ELVIS = 51;                         // ?? Elvis 运算符
    public static final int REGEX_MATCH = 52;                   // =~ 正则匹配
    public static final int REGEX_NOT_MATCH = 53;               // !=~ 正则不匹配
    
    // 区间运算符（四种类型）
    public static final int RANGE_CLOSED = 54;                  // .. 全闭区间 [a, b]
    public static final int RANGE_OPEN = 55;                    // >..< 全开区间 (a, b)
    public static final int RANGE_LEFT_CLOSED_RIGHT_OPEN = 56;  // ..< 前闭后开 [a, b)
    public static final int RANGE_LEFT_OPEN_RIGHT_CLOSED = 57;  // >.. 前开后闭 (a, b]

    // Lambda 运算符
    public static final int ARROW = 58;                         // -> Lambda 箭头

    // ==================== 字面量 ====================
    public static final int IDENTIFIER = 70;        // 标识符
    public static final int STRING = 71;            // 字符串字面量（单引号/双引号）
    public static final int TEMPLATE_STRING = 72;   // 字符串模板字面量（反引号）
    public static final int NUMBER = 73;            // 数值字面量 (int/double)
    public static final int LONG = 74;              // 长整数字面量（L 结尾）
    public static final int BIG_INTEGER = 75;       // 大整数字面量（超出 long 范围）
    public static final int BIG_DECIMAL = 76;       // 高精度小数字面量（BigDecimal）
    public static final int REGEX = 77;             // 正则表达式字面量（/pattern/flags）
    public static final int TRUE = 78;              // true
    public static final int FALSE = 79;             // false
    public static final int NULL = 80;              // null
    public static final int DOLLAR_NUMBER = 81;     // $0, $1, $2, ... (argument variable reference)
    public static final int AT = 82;                // @（JSONPath 当前节点，仅过滤表达式内）

    // ==================== URL 字面量 ====================
    public static final int URL = 132;              // url 关键字

    // ==================== 关键字 ====================
    public static final int IF = 90;                // if
    public static final int ELSE = 91;              // else
    public static final int FOR = 92;               // for
    public static final int IN = 93;                // in（范围检查、循环）
    public static final int NOT_IN = 94;            // !in（不包含判断）
    public static final int SWITCH = 95;            // switch
    public static final int CASE = 96;              // case
    public static final int DEFAULT = 97;           // default
    public static final int LET = 98;               // let（变量声明）
    public static final int EXTENDS = 99;           // extends（模板扩展）
    public static final int IMPORT = 100;           // import（模板文件导入）
    public static final int AS = 101;               // as（别名）
    public static final int RETURN = 102;          // return（Lambda 返回语句）
    public static final int EXPORT = 103;          // export（导出变量）
    public static final int SUBSETOF = 104;        // subsetof（JSONPath 集合操作符：左是右的子集）
    public static final int ANYOF = 105;           // anyof（JSONPath 集合操作符：左与右有交集）
    public static final int NONEOF = 106;          // noneof（JSONPath 集合操作符：左与右无交集）

    // ==================== 语句块标记 ====================
    /**
     * 语句块开始标记 {%
     * <p>用于标准语句块和自闭合语句块的开始</p>
     * <ul>
     *   <li>标准语句块：{% 语句名称 参数 } ... {/% 语句名称 }</li>
     *   <li>自闭合语句块：{% 语句 %}</li>
     * </ul>
     */
    public static final int STMT_BEGIN = 110;       // {%
    
    /**
     * 自闭合语句块结束标记 %}
     * <p>用于自闭合语句块的结束</p>
     */
    public static final int STMT_END = 111;         // %}
    
    /**
     * 标准语句块结束标签开始 {/%
     * <p>用于标准语句块的结束标签开始，后面跟语句名称和 }</p>
     */
    public static final int STMT_CLOSE = 112;       // {/%

    // ==================== 表达式标记 ====================
    /**
     * 单花括号表达式开始标记
     * <p>格式：{表达式}</p>
     */
    public static final int SINGLE_BRACE_EXPR_BEGIN = 120;  // {
    
    /**
     * 单花括号表达式结束标记
     * <p>格式：{表达式}</p>
     */
    public static final int SINGLE_BRACE_EXPR_END = 121;    // }
    
    /**
     * 双花括号表达式开始标记
     * <p>格式：{{表达式}}</p>
     */
    public static final int DOUBLE_BRACE_EXPR_BEGIN = 122;  // {{
    
    /**
     * 双花括号表达式结束标记
     * <p>格式：{{表达式}}</p>
     */
    public static final int DOUBLE_BRACE_EXPR_END = 123;    // }}
    
    /**
     * 美元符号表达式开始标记
     * <p>格式：${表达式}</p>
     */
    public static final int DOLLAR_BRACE_EXPR_BEGIN = 124;  // ${
    
    /**
     * 美元符号表达式结束标记
     * <p>格式：${表达式}</p>
     */
    public static final int DOLLAR_BRACE_EXPR_END = 125;    // }

    private TokenType() {
        // 工具类，禁止实例化
    }

    /**
     * 获取 Token 类型对应的符号（用于错误消息）
     *
     * @param type Token 类型常量
     * @return Token 类型对应的符号字符串，如果没有对应的符号则返回类型名称
     */
    public static String getSymbol(int type) {
        switch (type) {
            // 分隔符
            case LEFT_BRACE: return "{";
            case RIGHT_BRACE: return "}";
            case LEFT_PAREN: return "(";
            case RIGHT_PAREN: return ")";
            case LEFT_BRACKET: return "[";
            case RIGHT_BRACKET: return "]";
            case COMMA: return ",";
            case DOT: return ".";
            case SEMICOLON: return ";";
            case BACKTICK: return "`";
            case SLASH: return "/";

            // 基本运算符
            case PLUS: return "+";
            case MINUS: return "-";
            case MULTIPLY: return "*";
            case DIVIDE: return "/";
            case MODULO: return "%";
            case GT: return ">";
            case LT: return "<";
            case GTE: return ">=";
            case LTE: return "<=";
            case EQ: return "==";
            case NEQ: return "!=";
            case AND: return "&&";
            case OR: return "||";
            case NOT: return "!";
            case QUESTION: return "?";
            case COLON: return ":";
            case ASSIGN: return "=";
            case INCREMENT: return "++";
            case DECREMENT: return "--";
            case PLUS_ASSIGN: return "+=";
            case MINUS_ASSIGN: return "-=";
            case MULTIPLY_ASSIGN: return "*=";
            case DIVIDE_ASSIGN: return "/=";
            case MODULO_ASSIGN: return "%=";
            case BIT_AND_ASSIGN: return "&=";
            case BIT_OR_ASSIGN: return "|=";
            case BIT_XOR_ASSIGN: return "^=";
            case LEFT_SHIFT_ASSIGN: return "<<=";
            case RIGHT_SHIFT_ASSIGN: return ">>=";
            case UNSIGNED_RIGHT_SHIFT_ASSIGN: return ">>>=";

            // 高级运算符
            case SAFE_NAV: return "?.";
            case ELVIS: return "??";
            case REGEX_MATCH: return "=~";
            case REGEX_NOT_MATCH: return "!=~";
            case RANGE_CLOSED: return "..";
            case RANGE_OPEN: return ">..<";
            case RANGE_LEFT_CLOSED_RIGHT_OPEN: return "..<";
            case RANGE_LEFT_OPEN_RIGHT_CLOSED: return ">..";

            // Lambda 运算符
            case ARROW: return "->";

            // 关键字
            case IF: return "if";
            case ELSE: return "else";
            case FOR: return "for";
            case IN: return "in";
            case NOT_IN: return "!in";
            case SWITCH: return "switch";
            case CASE: return "case";
            case DEFAULT: return "default";
            case LET: return "let";
            case EXTENDS: return "extends";
            case IMPORT: return "import";
            case AS: return "as";
            case RETURN: return "return";
            case EXPORT: return "export";
            case SUBSETOF: return "subsetof";
            case ANYOF: return "anyof";
            case NONEOF: return "noneof";

            // 语句块标记
            case STMT_BEGIN: return "{%";
            case STMT_END: return "%}";
            case STMT_CLOSE: return "{/%";

            // 表达式标记
            case SINGLE_BRACE_EXPR_BEGIN: return "{";
            case SINGLE_BRACE_EXPR_END: return "}";
            case DOUBLE_BRACE_EXPR_BEGIN: return "{{";
            case DOUBLE_BRACE_EXPR_END: return "}}";
            case DOLLAR_BRACE_EXPR_BEGIN: return "${";
            case DOLLAR_BRACE_EXPR_END: return "}";

            // 字面量和特殊
            case TRUE: return "true";
            case FALSE: return "false";
            case NULL: return "null";
            case DOLLAR_NUMBER: return "$<number>";
            case AT: return "@";
            case EOF: return "<end of file>";

            // 字面量类型（使用小写描述）
            case IDENTIFIER: return "identifier";
            case STRING: return "string";
            case TEMPLATE_STRING: return "template string";
            case NUMBER: return "number";
            case LONG: return "long";
            case BIG_INTEGER: return "big integer";
            case BIG_DECIMAL: return "big decimal";
            case REGEX: return "regex";
            case TEXT: return "text";

            // URL literal
            case URL: return "url";

            // 其他类型返回名称
            default: return getName(type);
        }
    }

    /**
     * 获取 Token 类型的名称（用于调试和日志输出）
     *
     * @param type Token 类型常量
     * @return Token 类型名称字符串
     */
    public static String getName(int type) {
        switch (type) {
            // 特殊
            case EOF: return "EOF";
            case TEXT: return "TEXT";
            
            // 分隔符
            case LEFT_BRACE: return "LEFT_BRACE";
            case RIGHT_BRACE: return "RIGHT_BRACE";
            case LEFT_PAREN: return "LEFT_PAREN";
            case RIGHT_PAREN: return "RIGHT_PAREN";
            case LEFT_BRACKET: return "LEFT_BRACKET";
            case RIGHT_BRACKET: return "RIGHT_BRACKET";
            case COMMA: return "COMMA";
            case DOT: return "DOT";
            case SEMICOLON: return "SEMICOLON";
            case BACKTICK: return "BACKTICK";
            case SLASH: return "SLASH";
            
            // 基本运算符
            case PLUS: return "PLUS";
            case MINUS: return "MINUS";
            case MULTIPLY: return "MULTIPLY";
            case DIVIDE: return "DIVIDE";
            case MODULO: return "MODULO";
            case GT: return "GT";
            case LT: return "LT";
            case GTE: return "GTE";
            case LTE: return "LTE";
            case EQ: return "EQ";
            case NEQ: return "NEQ";
            case AND: return "AND";
            case OR: return "OR";
            case NOT: return "NOT";
            case QUESTION: return "QUESTION";
            case COLON: return "COLON";
            case ASSIGN: return "ASSIGN";
            case INCREMENT: return "INCREMENT";
            case DECREMENT: return "DECREMENT";
            case PLUS_ASSIGN: return "PLUS_ASSIGN";
            case MINUS_ASSIGN: return "MINUS_ASSIGN";
            case MULTIPLY_ASSIGN: return "MULTIPLY_ASSIGN";
            case DIVIDE_ASSIGN: return "DIVIDE_ASSIGN";
            case MODULO_ASSIGN: return "MODULO_ASSIGN";
            case BIT_AND_ASSIGN: return "BIT_AND_ASSIGN";
            case BIT_OR_ASSIGN: return "BIT_OR_ASSIGN";
            case BIT_XOR_ASSIGN: return "BIT_XOR_ASSIGN";
            case LEFT_SHIFT_ASSIGN: return "LEFT_SHIFT_ASSIGN";
            case RIGHT_SHIFT_ASSIGN: return "RIGHT_SHIFT_ASSIGN";
            case UNSIGNED_RIGHT_SHIFT_ASSIGN: return "UNSIGNED_RIGHT_SHIFT_ASSIGN";
            
            // 高级运算符
            case SAFE_NAV: return "SAFE_NAV";
            case ELVIS: return "ELVIS";
            case REGEX_MATCH: return "REGEX_MATCH";
            case REGEX_NOT_MATCH: return "REGEX_NOT_MATCH";
            case RANGE_CLOSED: return "RANGE_CLOSED";
            case RANGE_OPEN: return "RANGE_OPEN";
            case RANGE_LEFT_CLOSED_RIGHT_OPEN: return "RANGE_LEFT_CLOSED_RIGHT_OPEN";
            case RANGE_LEFT_OPEN_RIGHT_CLOSED: return "RANGE_LEFT_OPEN_RIGHT_CLOSED";

            // Lambda 运算符
            case ARROW: return "ARROW";

            // 字面量
            case IDENTIFIER: return "IDENTIFIER";
            case STRING: return "STRING";
            case TEMPLATE_STRING: return "TEMPLATE_STRING";
            case NUMBER: return "NUMBER";
            case LONG: return "LONG";
            case BIG_INTEGER: return "BIG_INTEGER";
            case BIG_DECIMAL: return "BIG_DECIMAL";
            case REGEX: return "REGEX";
            case TRUE: return "TRUE";
            case FALSE: return "FALSE";
            case NULL: return "NULL";
            case DOLLAR_NUMBER: return "DOLLAR_NUMBER";
            case AT: return "AT";

            // URL literal
            case URL: return "URL";

            // 关键字
            case IF: return "IF";
            case ELSE: return "ELSE";
            case FOR: return "FOR";
            case IN: return "IN";
            case NOT_IN: return "NOT_IN";
            case SWITCH: return "SWITCH";
            case CASE: return "CASE";
            case DEFAULT: return "DEFAULT";
            case LET: return "LET";
            case EXTENDS: return "EXTENDS";
            case IMPORT: return "IMPORT";
            case AS: return "AS";
            case RETURN: return "RETURN";
            case EXPORT: return "EXPORT";
            case SUBSETOF: return "SUBSETOF";
            case ANYOF: return "ANYOF";
            case NONEOF: return "NONEOF";

            // 语句块标记
            case STMT_BEGIN: return "STMT_BEGIN";
            case STMT_END: return "STMT_END";
            case STMT_CLOSE: return "STMT_CLOSE";
            
            // 表达式标记
            case SINGLE_BRACE_EXPR_BEGIN: return "SINGLE_BRACE_EXPR_BEGIN";
            case SINGLE_BRACE_EXPR_END: return "SINGLE_BRACE_EXPR_END";
            case DOUBLE_BRACE_EXPR_BEGIN: return "DOUBLE_BRACE_EXPR_BEGIN";
            case DOUBLE_BRACE_EXPR_END: return "DOUBLE_BRACE_EXPR_END";
            case DOLLAR_BRACE_EXPR_BEGIN: return "DOLLAR_BRACE_EXPR_BEGIN";
            case DOLLAR_BRACE_EXPR_END: return "DOLLAR_BRACE_EXPR_END";
            
            default: return "UNKNOWN(" + type + ")";
        }
    }
}

