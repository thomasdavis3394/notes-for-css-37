package org.dromara.easytl.ast;

import org.dromara.easytl.parser.Token;

/**
 * 当前节点（JSONPath）
 * 表示过滤表达式中的当前元素引用 {@code @}，例如：store.book[?(@.price < 10)]
 *
 * @author EasyTL Team
 */
public class CurrentNode extends ASTNode {

    /**
     * 构造当前节点
     */
    public CurrentNode() {
    }

    /**
     * 构造当前节点（从 Token 获取位置信息）
     *
     * @param token Token 对象（用于获取位置信息）
     */
    public CurrentNode(Token token) {
        super(positionFromToken(token));
    }

    /**
     * 构造当前节点（从位置信息）
     *
     * @param position 位置信息
     */
    public CurrentNode(Position position) {
        super(position);
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitCurrentNode(this);
    }

    @Override
    public String toString() {
        return "CurrentNode{}";
    }
}
