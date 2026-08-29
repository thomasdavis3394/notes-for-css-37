package org.dromara.easytl.ast;

import org.dromara.easytl.parser.Token;

/**
 * 一元表达式节点
 * 表示包含一个操作数和一个运算符的表达式
 * 例如：!a, -b, +c
 * 
 * @author EasyTL Team
 */
public class UnaryExpression extends ASTNode {
    
    /**
     * 一元运算符类型
     */
    public enum Operator {
        /** 逻辑非 */
        NOT("!"),
        
        /** 负号 */
        NEGATE("-"),
        
        /** 正号 */
        PLUS("+");
        
        private final String symbol;
        
        Operator(String symbol) {
            this.symbol = symbol;
        }
        
        public String getSymbol() {
            return symbol;
        }
    }
    
    /** 运算符 */
    private final Operator operator;
    
    /** 操作数 */
    private final ASTNode operand;
    
    /**
     * 构造一元表达式节点
     *
     * @param operator 运算符
     * @param operand 操作数
     */
    public UnaryExpression(Operator operator, ASTNode operand) {
        this.operator = operator;
        this.operand = operand;
    }

    /**
     * 构造一元表达式节点（从 Token 获取位置信息）
     *
     * @param operator 运算符
     * @param operand 操作数
     * @param token Token 对象（用于获取位置信息）
     */
    public UnaryExpression(Operator operator, ASTNode operand, Token token) {
        super(positionFromToken(token));
        this.operator = operator;
        this.operand = operand;
    }

    /**
     * 构造一元表达式节点（从子节点推断位置信息）
     *
     * @param operator 运算符
     * @param operand 操作数
     * @param position 位置信息
     */
    public UnaryExpression(Operator operator, ASTNode operand, Position position) {
        super(position);
        this.operator = operator;
        this.operand = operand;
    }
    
    /**
     * 获取运算符
     * 
     * @return 运算符
     */
    public Operator getOperator() {
        return operator;
    }
    
    /**
     * 获取操作数
     * 
     * @return 操作数
     */
    public ASTNode getOperand() {
        return operand;
    }
    
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitUnaryExpression(this);
    }
    
    @Override
    public String toString() {
        return "UnaryExpression{" +
                "operator=" + operator +
                ", operand=" + operand +
                '}';
    }
}

