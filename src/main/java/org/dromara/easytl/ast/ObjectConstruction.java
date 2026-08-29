package org.dromara.easytl.ast;

import org.dromara.easytl.parser.Token;

/**
 * 对象构造节点
 * 表示使用构造函数创建对象并设置属性
 * 格式：Person {name: 'Peter'} 或 Person('Peter') {age: 18}
 *
 * @author EasyTL Team
 */
public class ObjectConstruction extends ASTNode {

    /** 目标类（Identifier 或 MethodCall） */
    private final ASTNode targetClass;

    /** 属性设置块 */
    private final HashLiteral propertiesBlock;

    /**
     * 构造对象构造节点
     *
     * @param targetClass 目标类表达式
     * @param propertiesBlock 属性设置块
     */
    public ObjectConstruction(ASTNode targetClass, HashLiteral propertiesBlock) {
        this.targetClass = targetClass;
        this.propertiesBlock = propertiesBlock;
    }

    /**
     * 构造对象构造节点（带位置信息）
     *
     * @param targetClass 目标类表达式
     * @param propertiesBlock 属性设置块
     * @param position 位置信息
     */
    public ObjectConstruction(ASTNode targetClass, HashLiteral propertiesBlock, Position position) {
        super(position);
        this.targetClass = targetClass;
        this.propertiesBlock = propertiesBlock;
    }

    /**
     * 获取目标类表达式
     *
     * @return 目标类表达式
     */
    public ASTNode getTargetClass() {
        return targetClass;
    }

    /**
     * 获取属性设置块
     *
     * @return 属性设置块
     */
    public HashLiteral getPropertiesBlock() {
        return propertiesBlock;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitObjectConstruction(this);
    }

    @Override
    public String toString() {
        return "ObjectConstruction{targetClass=" + targetClass + ", propertiesBlock=" + propertiesBlock + '}';
    }
}