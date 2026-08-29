package org.dromara.easytl.ast;

/**
 * 表达式节点抽象基类
 * 表示模板中嵌入的表达式，例如：{user.name}、{{user.age}} 或 ${user.name}
 * 
 * <p>有三种具体的表达式节点类型：</p>
 * <ul>
 *   <li>{@link SingleBraceExpressionNode} - 单花括号表达式 {表达式}</li>
 *   <li>{@link DoubleBraceExpressionNode} - 双花括号表达式 {{表达式}}</li>
 *   <li>{@link DollarBraceExpressionNode} - 美元符号表达式 ${表达式}</li>
 * </ul>
 * 
 * <p>三种语法功能等价，但节点类型不同，便于调试和分析</p>
 * 
 * @author EasyTL Team
 */
public abstract class ExpressionNode extends ASTNode {
    
    /** 表达式AST */
    protected final ASTNode expression;
    
    /**
     * 构造表达式节点
     * 
     * @param expression 表达式AST
     */
    protected ExpressionNode(ASTNode expression) {
        this.expression = expression;
    }
    
    /**
     * 构造表达式节点（带位置信息）
     * 
     * @param expression 表达式AST
     * @param position 位置信息
     */
    protected ExpressionNode(ASTNode expression, Position position) {
        super(position);
        this.expression = expression;
    }
    
    /**
     * 获取表达式AST
     * 
     * @return 表达式AST
     */
    public ASTNode getExpression() {
        return expression;
    }
}

