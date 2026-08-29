package org.dromara.easytl.ast;

import org.dromara.easytl.parser.Token;

/**
 * Elvis表达式节点
 * 表示空值合并运算符：value ?: defaultValue
 * 当左侧值为null或空时，返回右侧的默认值
 * 
 * @author EasyTL Team
 */
public class ElvisExpression extends ASTNode {
    
    /** 左侧表达式 */
    private final ASTNode left;
    
    /** 右侧默认值表达式 */
    private final ASTNode right;
    
    /**
     * 构造Elvis表达式节点
     *
     * @param left 左侧表达式
     * @param right 右侧默认值表达式
     */
    public ElvisExpression(ASTNode left, ASTNode right) {
        this.left = left;
        this.right = right;
    }

    /**
     * 构造 Elvis 表达式节点（从 Token 范围获取位置信息）
     *
     * @param left 左表达式
     * @param right 右表达式
     * @param startToken 起始 Token
     * @param endToken 结束 Token
     */
    public ElvisExpression(ASTNode left, ASTNode right, Token startToken, Token endToken) {
        super(positionFromTokens(startToken, endToken));
        this.left = left;
        this.right = right;
    }

    /**
     * 构造 Elvis 表达式节点（从子节点推断位置信息）
     *
     * @param left 左表达式
     * @param right 右表达式
     * @param position 位置信息
     */
    public ElvisExpression(ASTNode left, ASTNode right, Position position) {
        super(position);
        this.left = left;
        this.right = right;
    }
    
    /**
     * 获取左侧表达式
     * 
     * @return 左侧表达式
     */
    public ASTNode getLeft() {
        return left;
    }
    
    /**
     * 获取右侧默认值表达式
     * 
     * @return 右侧默认值表达式
     */
    public ASTNode getRight() {
        return right;
    }
    
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitElvisExpression(this);
    }
    
    @Override
    public String toString() {
        return "ElvisExpression{" +
                "left=" + left +
                ", right=" + right +
                '}';
    }
}

