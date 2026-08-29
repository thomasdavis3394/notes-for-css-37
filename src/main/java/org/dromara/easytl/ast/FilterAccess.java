package org.dromara.easytl.ast;

import org.dromara.easytl.parser.Token;

/**
 * 过滤访问节点（JSONPath）
 * 表示 {@code [?(expr)]} 过滤访问，对列表/数组逐元素按谓词筛选
 * 例如：store.book[?(@.price < 10)]
 *
 * @author EasyTL Team
 */
public class FilterAccess extends ASTNode {

    /** 被访问的对象表达式 */
    private final ASTNode object;

    /** 过滤谓词表达式（内可使用 {@code @} 引用当前元素） */
    private final ASTNode predicate;

    /**
     * 构造过滤访问节点
     *
     * @param object 被访问的对象表达式
     * @param predicate 过滤谓词表达式
     */
    public FilterAccess(ASTNode object, ASTNode predicate) {
        this.object = object;
        this.predicate = predicate;
    }

    /**
     * 构造过滤访问节点（从 Token 获取位置信息）
     *
     * @param object 被访问的对象表达式
     * @param predicate 过滤谓词表达式
     * @param token Token 对象（用于获取位置信息）
     */
    public FilterAccess(ASTNode object, ASTNode predicate, Token token) {
        super(positionFromToken(token));
        this.object = object;
        this.predicate = predicate;
    }

    /**
     * 构造过滤访问节点（从位置信息）
     *
     * @param object 被访问的对象表达式
     * @param predicate 过滤谓词表达式
     * @param position 位置信息
     */
    public FilterAccess(ASTNode object, ASTNode predicate, Position position) {
        super(position);
        this.object = object;
        this.predicate = predicate;
    }

    /**
     * 获取被访问的对象表达式
     *
     * @return 对象表达式
     */
    public ASTNode getObject() {
        return object;
    }

    /**
     * 获取过滤谓词表达式
     *
     * @return 过滤谓词表达式
     */
    public ASTNode getPredicate() {
        return predicate;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitFilterAccess(this);
    }

    @Override
    public String toString() {
        return "FilterAccess{" +
                "object=" + object +
                ", predicate=" + predicate +
                '}';
    }
}
