package org.dromara.easytl.ast;

/**
 * 双花括号表达式节点
 * 表示双花括号表达式：{{表达式}}
 * 
 * @author EasyTL Team
 */
public class DoubleBraceExpressionNode extends ExpressionNode {
    
    /**
     * 构造双花括号表达式节点
     * 
     * @param expression 表达式AST
     */
    public DoubleBraceExpressionNode(ASTNode expression) {
        super(expression);
    }
    
    /**
     * 构造双花括号表达式节点（带位置信息）
     * 
     * @param expression 表达式AST
     * @param position 位置信息
     */
    public DoubleBraceExpressionNode(ASTNode expression, Position position) {
        super(expression, position);
    }
    
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitDoubleBraceExpressionNode(this);
    }
    
    @Override
    public String toString() {
        return "DoubleBraceExpressionNode{" +
                "expression=" + expression +
                '}';
    }
}



