package org.dromara.easytl.ast;

/**
 * 自增/自减表达式节点
 * 表示 ++x, x++, --x, x-- 表达式
 *
 * @author EasyTL Team
 */
public class IncrementExpression extends ASTNode {

    private final ASTNode operand;
    private final boolean isPrefix;
    private final boolean isIncrement;

    public IncrementExpression(ASTNode operand, boolean isPrefix, boolean isIncrement) {
        this.operand = operand;
        this.isPrefix = isPrefix;
        this.isIncrement = isIncrement;
    }

    public IncrementExpression(ASTNode operand, boolean isPrefix, boolean isIncrement, Position position) {
        super(position);
        this.operand = operand;
        this.isPrefix = isPrefix;
        this.isIncrement = isIncrement;
    }

    public ASTNode getOperand() { return operand; }
    public boolean isPrefix() { return isPrefix; }
    public boolean isIncrement() { return isIncrement; }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitIncrementExpression(this);
    }

    @Override
    public String toString() {
        String op = isIncrement ? "++" : "--";
        if (isPrefix) {
            return "IncrementExpression{" + op + operand + '}';
        }
        return "IncrementExpression{" + operand + op + '}';
    }
}