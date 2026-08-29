package org.dromara.easytl.ast;

import org.dromara.easytl.parser.Token;

/**
 * 通配符访问节点（JSONPath）
 * 表示对对象全部子节点的访问，{@code [*]} 与 {@code .*} 两种写法共用本节点
 * 例如：store.book[*]、store.*
 *
 * @author EasyTL Team
 */
public class WildcardAccess extends ASTNode {

    /** 被访问的对象表达式 */
    private final ASTNode object;

    /**
     * 构造通配符访问节点
     *
     * @param object 被访问的对象表达式
     */
    public WildcardAccess(ASTNode object) {
        this.object = object;
    }

    /**
     * 构造通配符访问节点（从 Token 获取位置信息）
     *
     * @param object 被访问的对象表达式
     * @param token Token 对象（用于获取位置信息）
     */
    public WildcardAccess(ASTNode object, Token token) {
        super(positionFromToken(token));
        this.object = object;
    }

    /**
     * 构造通配符访问节点（从位置信息）
     *
     * @param object 被访问的对象表达式
     * @param position 位置信息
     */
    public WildcardAccess(ASTNode object, Position position) {
        super(position);
        this.object = object;
    }

    /**
     * 获取被访问的对象表达式
     *
     * @return 对象表达式
     */
    public ASTNode getObject() {
        return object;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitWildcardAccess(this);
    }

    @Override
    public String toString() {
        return "WildcardAccess{" +
                "object=" + object +
                '}';
    }
}
