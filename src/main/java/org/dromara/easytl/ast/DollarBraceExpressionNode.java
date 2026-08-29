package org.dromara.easytl.ast;

/**
 * 美元符号表达式节点
 * 表示美元符号表达式：${表达式}
 * 
 * @author EasyTL Team
 */
public class DollarBraceExpressionNode extends ExpressionNode {
    
    /**
     * 构造美元符号表达式节点
     * 
     * @param expression 表达式AST
     */
    public DollarBraceExpressionNode(ASTNode expression) {
        super(expression);
    }
    
    /**
     * 构造美元符号表达式节点（带位置信息）
     * 
     * @param expression 表达式AST
     * @param position 位置信息
     */
    public DollarBraceExpressionNode(ASTNode expression, Position position) {
        super(expression, position);
    }
    
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitDollarBraceExpressionNode(this);
    }
    
    @Override
    public String toString() {
        return "DollarBraceExpressionNode{" +
                "expression=" + expression +
                '}';
    }
}



