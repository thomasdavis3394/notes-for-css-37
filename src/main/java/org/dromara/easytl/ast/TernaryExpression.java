package org.dromara.easytl.ast;

import org.dromara.easytl.parser.Token;

/**
 * 三元表达式节点
 * 表示条件运算符表达式：condition ? trueValue : falseValue
 * 
 * @author EasyTL Team
 */
public class TernaryExpression extends ASTNode {
    
    /** 条件表达式 */
    private final ASTNode condition;
    
    /** 条件为真时的值 */
    private final ASTNode trueValue;
    
    /** 条件为假时的值 */
    private final ASTNode falseValue;
    
    /**
     * 构造三元表达式节点
     *
     * @param condition 条件表达式
     * @param trueValue 条件为真时的值
     * @param falseValue 条件为假时的值
     */
    public TernaryExpression(ASTNode condition, ASTNode trueValue, ASTNode falseValue) {
        this.condition = condition;
        this.trueValue = trueValue;
        this.falseValue = falseValue;
    }

    /**
     * 构造三元表达式节点（从 Token 范围获取位置信息）
     *
     * @param condition 条件表达式
     * @param trueValue 为真时的值
     * @param falseValue 为假时的值
     * @param startToken 起始 Token
     * @param endToken 结束 Token
     */
    public TernaryExpression(ASTNode condition, ASTNode trueValue, ASTNode falseValue, Token startToken, Token endToken) {
        super(positionFromTokens(startToken, endToken));
        this.condition = condition;
        this.trueValue = trueValue;
        this.falseValue = falseValue;
    }

    /**
     * 构造三元表达式节点（从子节点推断位置信息）
     *
     * @param condition 条件表达式
     * @param trueValue 为真时的值
     * @param falseValue 为假时的值
     * @param position 位置信息
     */
    public TernaryExpression(ASTNode condition, ASTNode trueValue, ASTNode falseValue, Position position) {
        super(position);
        this.condition = condition;
        this.trueValue = trueValue;
        this.falseValue = falseValue;
    }
    
    /**
     * 获取条件表达式
     * 
     * @return 条件表达式
     */
    public ASTNode getCondition() {
        return condition;
    }
    
    /**
     * 获取条件为真时的值
     * 
     * @return 条件为真时的值
     */
    public ASTNode getTrueValue() {
        return trueValue;
    }
    
    /**
     * 获取条件为假时的值
     * 
     * @return 条件为假时的值
     */
    public ASTNode getFalseValue() {
        return falseValue;
    }
    
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitTernaryExpression(this);
    }
    
    @Override
    public String toString() {
        return "TernaryExpression{" +
                "condition=" + condition +
                ", trueValue=" + trueValue +
                ", falseValue=" + falseValue +
                '}';
    }
}

