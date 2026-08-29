package org.dromara.easytl.ast;

import java.util.List;

/**
 * Union 多选访问节点（JSONPath）
 * 表示 {@code [selector, selector, ...]} 多选访问，按声明顺序依次应用各选择器并合并结果
 * 例如：store.book[0, 2]、store.bicycle['color', 'price']、store.book[0:2, -1]
 *
 * <p>求值结果为 {@code NodeList}：下标/键选择器命中单个值，切片/通配/过滤选择器展开多个值；
 * 未命中的选择器（键不存在、下标越界、类型不适用）静默跳过。</p>
 *
 * @author EasyTL Team
 */
public class UnionAccess extends ASTNode {

    /** 被访问的对象表达式 */
    private final ASTNode object;

    /** 选择器列表（按声明顺序应用） */
    private final List<JsonPathSelector> selectors;

    /**
     * 构造 Union 多选访问节点
     *
     * @param object 被访问的对象表达式
     * @param selectors 选择器列表
     */
    public UnionAccess(ASTNode object, List<JsonPathSelector> selectors) {
        this.object = object;
        this.selectors = selectors;
    }

    /**
     * 构造 Union 多选访问节点（从位置信息）
     *
     * @param object 被访问的对象表达式
     * @param selectors 选择器列表
     * @param position 位置信息
     */
    public UnionAccess(ASTNode object, List<JsonPathSelector> selectors, Position position) {
        super(position);
        this.object = object;
        this.selectors = selectors;
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
     * 获取选择器列表
     *
     * @return 选择器列表
     */
    public List<JsonPathSelector> getSelectors() {
        return selectors;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitUnionAccess(this);
    }

    @Override
    public String toString() {
        return "UnionAccess{" +
                "object=" + object +
                ", selectors=" + selectors +
                '}';
    }
}
