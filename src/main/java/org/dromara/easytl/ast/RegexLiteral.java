package org.dromara.easytl.ast;

import org.dromara.easytl.parser.Token;

/**
 * 正则表达式字面量节点
 * 表示斜杠包裹的正则表达式，例如：/pattern/ 或 /pattern/flags
 * 支持标志位：g（全局匹配）、i（忽略大小写）、m（多行模式）
 * 
 * @author EasyTL Team
 */
public class RegexLiteral extends ASTNode {
    
    /** 正则表达式模式字符串 */
    private final String pattern;
    
    /** 标志位字符串（可能包含 g、i、m） */
    private final String flags;
    
    /**
     * 构造正则表达式字面量节点
     *
     * @param pattern 正则表达式模式
     * @param flags 标志位（可为null或空字符串）
     */
    public RegexLiteral(String pattern, String flags) {
        this.pattern = pattern;
        this.flags = flags == null ? "" : flags;
    }

    /**
     * 构造正则表达式字面量节点（从 Token 获取位置信息）
     *
     * @param pattern 正则表达式模式
     * @param flags 标志位
     * @param token Token 对象（用于获取位置信息）
     */
    public RegexLiteral(String pattern, String flags, Token token) {
        super(positionFromToken(token));
        this.pattern = pattern;
        this.flags = flags == null ? "" : flags;
    }
    
    /**
     * 获取正则表达式模式
     * 
     * @return 正则表达式模式
     */
    public String getPattern() {
        return pattern;
    }
    
    /**
     * 获取标志位
     * 
     * @return 标志位字符串
     */
    public String getFlags() {
        return flags;
    }
    
    /**
     * 是否有全局匹配标志
     * 
     * @return 如果包含g标志返回true
     */
    public boolean hasGlobalFlag() {
        return flags.contains("g");
    }
    
    /**
     * 是否有忽略大小写标志
     * 
     * @return 如果包含i标志返回true
     */
    public boolean hasIgnoreCaseFlag() {
        return flags.contains("i");
    }
    
    /**
     * 是否有多行模式标志
     * 
     * @return 如果包含m标志返回true
     */
    public boolean hasMultilineFlag() {
        return flags.contains("m");
    }
    
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitRegexLiteral(this);
    }
    
    @Override
    public String toString() {
        return "RegexLiteral{" +
                "pattern='" + pattern + '\'' +
                ", flags='" + flags + '\'' +
                '}';
    }
}

