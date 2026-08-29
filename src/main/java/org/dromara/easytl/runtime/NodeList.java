package org.dromara.easytl.runtime;

import java.util.ArrayList;

/**
 * JSONPath 查询结果列表（标记类型）
 *
 * <p>仅作为标记类型使用：通配（{@code [*]}/{@code .*}）、切片（{@code [start:end(:step)]}）、
 * 过滤（{@code [?(expr)]}）、递归下降（{@code ..name}/{@code ..*}/{@code ..[...]}）、
 * Union 多选（{@code [sel, sel, ...]}）等 JSONPath 访问的结果均为本类型。
 * 由于继承自 {@link ArrayList}，可直接用于 {@code {% for %}}、{@code .size()}、Elvis 等现有能力，
 * 渲染输出与普通 List 一致。</p>
 *
 * <p>{@code MemberAccess}/{@code IndexAccess}（字符串 key）作用于 {@code NodeList} 时按
 * JSONPath 投影语义逐元素取值（元素为 null 或无该属性时跳过），结果仍为 {@code NodeList}；
 * 数值索引作用于 {@code NodeList} 时按普通列表取元素（保证 {@code result[0]} 的直觉行为）。</p>
 *
 * @author EasyTL Team
 */
public class NodeList extends ArrayList<Object> {

    private static final long serialVersionUID = 1L;
}
