package org.dromara.easytl.ast;

import org.dromara.easytl.parser.Token;

/**
 * 布尔字面量节点
 * 表示 true 或 false
 * 
 * @author EasyTL Team
 */
public class BooleanLiteral extends ASTNode {
    
    /** 布尔值 */
    private final boolean value;
    
    /**
     * 构造布尔字面量节点
     *
     * @param value 布尔值
     */
    public BooleanLiteral(boolean value) {
        this.value = value;
    }

    /**
     * 构造布尔字面量节点（从 Token 获取位置信息）
     *
     * @param value 布尔值
     * @param token Token 对象（用于获取位置信息）
     */
    public BooleanLiteral(boolean value, Token token) {
        super(positionFromToken(token));
        this.value = value;
    }
    
    /**
     * 获取布尔值
     * 
     * @return 布尔值
     */
    public boolean getValue() {
        return value;
    }
    
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitBooleanLiteral(this);
    }
    
    @Override
    public String toString() {
        return "BooleanLiteral{" + "value=" + value + '}';
    }
}

