package org.dromara.easytl.ast;

import org.dromara.easytl.parser.Token;

/**
 * 字符串字面量节点
 * 表示单引号或双引号包裹的字符串
 * 
 * @author EasyTL Team
 */
public class StringLiteral extends ASTNode {
    
    /** 字符串值 */
    private final String value;
    
    /**
     * 构造字符串字面量节点
     *
     * @param value 字符串值
     */
    public StringLiteral(String value) {
        this.value = value;
    }

    /**
     * 构造字符串字面量节点（从 Token 获取位置信息）
     *
     * @param value 字符串值
     * @param token Token 对象（用于获取位置信息）
     */
    public StringLiteral(String value, Token token) {
        super(positionFromToken(token));
        this.value = value;
    }
    
    /**
     * 获取字符串值
     * 
     * @return 字符串值
     */
    public String getValue() {
        return value;
    }
    
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitStringLiteral(this);
    }
    
    @Override
    public String toString() {
        return "StringLiteral{" + "value='" + value + '\'' + '}';
    }
}

