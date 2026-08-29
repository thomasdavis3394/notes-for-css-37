package org.dromara.easytl.ast;

/**
 * 类型转换表达式节点
 * 表示将值转换为指定类型的表达式，例如：{(int) num}, {(long) 10}, {(String) 123}
 *
 * @author EasyTL Team
 */
public class CastExpression extends ASTNode {

    /** 类型名称（如 "int", "String", "List"） */
    private final String typeName;

    /** 要转换的表达式 */
    private final ASTNode operand;

    /**
     * 构造类型转换表达式节点
     *
     * @param typeName 类型名称
     * @param operand  要转换的表达式
     */
    public CastExpression(String typeName, ASTNode operand) {
        this.typeName = typeName;
        this.operand = operand;
    }

    /**
     * 构造类型转换表达式节点（带位置信息）
     *
     * @param typeName 类型名称
     * @param operand  要转换的表达式
     * @param position 位置信息
     */
    public CastExpression(String typeName, ASTNode operand, Position position) {
        super(position);
        this.typeName = typeName;
        this.operand = operand;
    }

    /**
     * 获取类型名称
     *
     * @return 类型名称
     */
    public String getTypeName() {
        return typeName;
    }

    /**
     * 获取要转换的表达式
     *
     * @return 要转换的表达式
     */
    public ASTNode getOperand() {
        return operand;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitCastExpression(this);
    }

    @Override
    public String toString() {
        return "CastExpression{" + "typeName='" + typeName + '\'' + ", operand=" + operand + '}';
    }
}