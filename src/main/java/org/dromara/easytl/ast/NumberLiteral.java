package org.dromara.easytl.ast;

import org.dromara.easytl.parser.Token;

/**
 * 数值字面量节点
 * 支持多种数值类型：int、long、BigInteger、double、BigDecimal
 * 
 * @author EasyTL Team
 */
public class NumberLiteral extends ASTNode {
    
    /** 数值对象（可能是 Integer、Long、BigInteger、Double、BigDecimal） */
    private final Number value;
    
    /** 原始字符串表示 */
    private final String raw;
    
    /**
     * 构造数值字面量节点
     *
     * @param value 数值对象
     * @param raw 原始字符串
     */
    public NumberLiteral(Number value, String raw) {
        this.value = value;
        this.raw = raw;
    }

    /**
     * 构造数值字面量节点（从 Token 获取位置信息）
     *
     * @param value 数值对象
     * @param raw 原始字符串
     * @param token Token 对象（用于获取位置信息）
     */
    public NumberLiteral(Number value, String raw, Token token) {
        super(positionFromToken(token));
        this.value = value;
        this.raw = raw;
    }
    
    /**
     * 获取数值对象
     * 
     * @return 数值对象
     */
    public Number getValue() {
        return value;
    }
    
    /**
     * 获取原始字符串表示
     * 
     * @return 原始字符串
     */
    public String getRaw() {
        return raw;
    }
    
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitNumberLiteral(this);
    }
    
    @Override
    public String toString() {
        return "NumberLiteral{" +
                "value=" + value +
                ", type=" + value.getClass().getSimpleName() +
                ", raw='" + raw + '\'' +
                '}';
    }
}

