package org.dromara.easytl.ast;

/**
 * 复合赋值表达式节点
 * 表示 x += expr, x -= expr 等复合赋值表达式
 *
 * @author EasyTL Team
 */
public class CompoundAssignmentExpression extends ASTNode {

    private final ASTNode target;
    private final CompoundOperator operator;
    private final ASTNode value;

    public CompoundAssignmentExpression(ASTNode target, CompoundOperator operator, ASTNode value) {
        this.target = target;
        this.operator = operator;
        this.value = value;
    }

    public CompoundAssignmentExpression(ASTNode target, CompoundOperator operator, ASTNode value, Position position) {
        super(position);
        this.target = target;
        this.operator = operator;
        this.value = value;
    }

    public ASTNode getTarget() { return target; }
    public CompoundOperator getOperator() { return operator; }
    public ASTNode getValue() { return value; }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitCompoundAssignmentExpression(this);
    }

    @Override
    public String toString() {
        return "CompoundAssignmentExpression{" + target + " " + operator.getSymbol() + " " + value + '}';
    }
}