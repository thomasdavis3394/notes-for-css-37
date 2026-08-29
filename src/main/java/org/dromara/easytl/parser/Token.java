package org.dromara.easytl.parser;

/**
 * Token 表示词法分析的最小单元
 *
 * @author EasyTL Team
 */
public class Token {

    private final int type;
    private final String value;
    private final int line;
    private final int startColumn;
    private final int endColumn;
    private final int position;
    private final String flags;

    /**
     * 构造一个 Token
     *
     * @param type        Token 类型
     * @param value       Token 值
     * @param line        行号
     * @param startColumn 起始列号
     * @param endColumn   结束列号
     * @param position    位置
     */
    public Token(int type, String value, int line, int startColumn, int endColumn, int position) {
        this(type, value, line, startColumn, endColumn, position, null);
    }

    /**
     * 构造一个无值的 Token
     *
     * @param type        Token 类型
     * @param line        行号
     * @param startColumn 起始列号
     * @param endColumn   结束列号
     * @param position    位置
     */
    public Token(int type, int line, int startColumn, int endColumn, int position) {
        this(type, null, line, startColumn, endColumn, position, null);
    }

    /**
     * 构造一个带标志位的 Token（目前仅 REGEX 类型使用，保存正则标志位）
     *
     * @param type        Token 类型
     * @param value       Token 值
     * @param line        行号
     * @param startColumn 起始列号
     * @param endColumn   结束列号
     * @param position    位置
     * @param flags       标志位（可为 null，如正则的 g/i/m）
     */
    public Token(int type, String value, int line, int startColumn, int endColumn, int position, String flags) {
        this.type = type;
        this.value = value;
        this.line = line;
        this.startColumn = startColumn;
        this.endColumn = endColumn;
        this.position = position;
        this.flags = flags;
    }

    /**
     * 获取 Token 类型
     *
     * @return Token 类型
     */
    public int getType() {
        return type;
    }

    /**
     * 获取 Token 值
     *
     * @return Token 值，如果没有值则返回 null
     */
    public String getValue() {
        return value;
    }

    /**
     * 获取 Token 标志位
     *
     * <p>目前仅 REGEX 类型的 Token 会携带标志位（g/i/m 的子集），
     * 用于区分正则模式与标志位，避免从 value 中拆分时的歧义</p>
     *
     * @return 标志位字符串，如果没有标志位则返回 null
     */
    public String getFlags() {
        return flags;
    }

    /**
     * 获取 Token 所在行号
     *
     * @return 行号
     */
    public int getLine() {
        return line;
    }

    /**
     * 获取 Token 起始列号
     *
     * @return 起始列号
     */
    public int getStartColumn() {
        return startColumn;
    }

    /**
     * 获取 Token 结束列号
     *
     * @return 结束列号
     */
    public int getEndColumn() {
        return endColumn;
    }

    /**
     * 获取 Token 在源码中的位置
     *
     * @return 位置
     */
    public int getPosition() {
        return position;
    }

    @Override
    public String toString() {
        if (value != null) {
            return String.format("Token{type=%s, value='%s', line=%d, startColumn=%d, endColumn=%d}",
                    TokenType.getName(type), value, line, startColumn, endColumn);
        } else {
            return String.format("Token{type=%s, line=%d, startColumn=%d, endColumn=%d}",
                    TokenType.getName(type), line, startColumn, endColumn);
        }
    }
}
