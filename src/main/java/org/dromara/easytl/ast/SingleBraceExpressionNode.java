package org.dromara.easytl.ast;

/**
 * 单花括号表达式节点
 * 表示单花括号表达式：{表达式}
 * 
 * @author EasyTL Team
 */
public class SingleBraceExpressionNode extends ExpressionNode {
    
    /**
     * 构造单花括号表达式节点
     * 
     * @param expression 表达式AST
     */
    public SingleBraceExpressionNode(ASTNode expression) {
        super(expression);
    }
    
    /**
     * 构造单花括号表达式节点（带位置信息）
     * 
     * @param expression 表达式AST
     * @param position 位置信息
     */
    public SingleBraceExpressionNode(ASTNode expression, Position position) {
        super(expression, position);
    }
    
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitSingleBraceExpressionNode(this);
    }
    
    @Override
    public String toString() {
        return "SingleBraceExpressionNode{" +
                "expression=" + expression +
                '}';
    }
}



