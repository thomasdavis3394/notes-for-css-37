package org.dromara.easytl.ast;

import org.dromara.easytl.parser.Token;

import java.util.List;

/**
 * 递归下降访问节点（JSONPath）
 * 表示 {@code ..name} 递归下降访问，在任意深度收集匹配的成员值
 * 例如：store.book..price
 *
 * <p>也支持选择器形式：{@code ..*}（全部后代节点）、{@code ..['name']}（等价 ..name）、
 * {@code ..[0]}（每个列表的首元素）、{@code ..[1:3]}（每个列表的切片）、
 * {@code ..[?(...)]}（对每个列表/Map 应用过滤）及多选 {@code ..[0, 'name']}；
 * 此时 {@link #getMemberName()} 为 null，{@link #getSelectors()} 非 null。</p>
 *
 * @author EasyTL Team
 */
public class RecursiveDescentAccess extends ASTNode {

    /** 被访问的对象表达式 */
    private final ASTNode object;

    /** 要递归收集的成员名称（选择器形式时为 null） */
    private final String memberName;

    /** 递归下降的选择器列表（成员名形式时为 null） */
    private final List<JsonPathSelector> selectors;

    /**
     * 构造递归下降访问节点
     *
     * @param object 被访问的对象表达式
     * @param memberName 要递归收集的成员名称
     */
    public RecursiveDescentAccess(ASTNode object, String memberName) {
        this.object = object;
        this.memberName = memberName;
        this.selectors = null;
    }

    /**
     * 构造递归下降访问节点（从 Token 获取位置信息）
     *
     * @param object 被访问的对象表达式
     * @param memberName 要递归收集的成员名称
     * @param token Token 对象（用于获取位置信息）
     */
    public RecursiveDescentAccess(ASTNode object, String memberName, Token token) {
        super(positionFromToken(token));
        this.object = object;
        this.memberName = memberName;
        this.selectors = null;
    }

    /**
     * 构造递归下降访问节点（从位置信息）
     *
     * @param object 被访问的对象表达式
     * @param memberName 要递归收集的成员名称
     * @param position 位置信息
     */
    public RecursiveDescentAccess(ASTNode object, String memberName, Position position) {
        super(position);
        this.object = object;
        this.memberName = memberName;
        this.selectors = null;
    }

    /**
     * 构造选择器形式的递归下降访问节点（从位置信息）
     *
     * @param object 被访问的对象表达式
     * @param selectors 递归下降的选择器列表
     * @param position 位置信息
     */
    public RecursiveDescentAccess(ASTNode object, List<JsonPathSelector> selectors, Position position) {
        super(position);
        this.object = object;
        this.memberName = null;
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
     * 获取要递归收集的成员名称
     *
     * @return 成员名称；选择器形式时为 null
     */
    public String getMemberName() {
        return memberName;
    }

    /**
     * 获取递归下降的选择器列表
     *
     * @return 选择器列表；成员名形式时为 null
     */
    public List<JsonPathSelector> getSelectors() {
        return selectors;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitRecursiveDescentAccess(this);
    }

    @Override
    public String toString() {
        return "RecursiveDescentAccess{" +
                "object=" + object +
                ", memberName='" + memberName + '\'' +
                ", selectors=" + selectors +
                '}';
    }
}
