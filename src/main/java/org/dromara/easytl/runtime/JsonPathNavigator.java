package org.dromara.easytl.runtime;

import org.dromara.easytl.exception.TemplateRuntimeException;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * JSONPath 导航工具
 *
 * <p>提供子节点枚举与递归下降收集能力，供通配符（{@code [*]}/{@code .*}）与
 * 递归下降（{@code ..name}）求值使用。子节点定义：Map → 全部 value；
 * List/数组 → 全部元素；Bean → 全部属性值（getter/public 字段，排除 {@code class}）。</p>
 *
 * @author EasyTL Team
 */
public final class JsonPathNavigator {

    private JsonPathNavigator() {
        // 工具类，禁止实例化
    }

    /**
     * 枚举对象的直接子节点
     *
     * @param obj 对象（可为 null）
     * @return 子节点列表；标量及 null 返回空列表
     */
    public static List<Object> childrenOf(Object obj) {
        if (obj == null) {
            return Collections.emptyList();
        }
        if (obj instanceof Map) {
            return new ArrayList<>(((Map<?, ?>) obj).values());
        }
        if (obj instanceof List) {
            return new ArrayList<>((List<?>) obj);
        }
        if (obj.getClass().isArray()) {
            int length = Array.getLength(obj);
            List<Object> children = new ArrayList<>(length);
            for (int i = 0; i < length; i++) {
                children.add(Array.get(obj, i));
            }
            return children;
        }
        if (isScalar(obj)) {
            return Collections.emptyList();
        }
        return beanValues(obj);
    }

    /**
     * 递归下降收集匹配成员的值（先序 DFS）
     *
     * <p>从对象自身开始（Map 含该 key / Bean 有该属性即收集），再递归子节点；
     * 使用 {@link IdentityHashMap} 防止循环引用导致的无限递归。</p>
     *
     * @param root 根对象（可为 null）
     * @param member 要收集的成员名称
     * @return 收集结果（先序顺序）
     */
    public static NodeList descendantValues(Object root, String member) {
        NodeList result = new NodeList();
        collect(root, member, new IdentityHashMap<>(), result);
        return result;
    }

    /**
     * 先序 DFS 收集：先检查节点自身是否含有该成员，再递归子节点
     */
    private static void collect(Object node, String member,
                                IdentityHashMap<Object, Boolean> visited, NodeList result) {
        if (node == null || isScalar(node)) {
            return;
        }
        if (visited.containsKey(node)) {
            return;
        }
        visited.put(node, Boolean.TRUE);

        // 节点自身含有该成员则收集（Map 含 key / Bean 有该属性）
        if (node instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) node;
            if (map.containsKey(member)) {
                result.add(map.get(member));
            }
        } else if (!(node instanceof List) && !node.getClass().isArray()) {
            try {
                result.add(ReflectionUtils.getProperty(node, member));
            } catch (TemplateRuntimeException e) {
                // 无该属性，忽略
            }
        }

        // 递归子节点
        for (Object child : childrenOf(node)) {
            collect(child, member, visited, result);
        }
    }

    /**
     * 收集后代树中的所有容器节点（先序 DFS，含根节点自身）
     *
     * <p>供 {@code ..*} / {@code ..[...]} 选择器形式的递归下降使用；
     * 标量（无子节点的叶子）不收录；使用 {@link IdentityHashMap} 防止循环引用导致的无限递归。</p>
     *
     * @param root 根对象（可为 null）
     * @return 容器节点列表（先序顺序，含 root 自身）
     */
    public static List<Object> descendantNodes(Object root) {
        List<Object> result = new ArrayList<>();
        collectNodes(root, new IdentityHashMap<>(), result);
        return result;
    }

    /**
     * 先序 DFS 收集容器节点：先收录节点自身，再递归子节点
     */
    private static void collectNodes(Object node, IdentityHashMap<Object, Boolean> visited, List<Object> result) {
        if (node == null || isScalar(node)) {
            return;
        }
        if (visited.containsKey(node)) {
            return;
        }
        visited.put(node, Boolean.TRUE);
        result.add(node);
        for (Object child : childrenOf(node)) {
            collectNodes(child, visited, result);
        }
    }

    /**
     * 枚举 Bean 的全部属性值（getter + public 字段，排除 class）
     */
    private static List<Object> beanValues(Object bean) {
        List<Object> values = new ArrayList<>();
        List<String> propertyNames = new ArrayList<>();
        Class<?> clazz = bean.getClass();
        try {
            // stopClass 指定为 Object.class，排除 getClass 对应的 class 属性
            BeanInfo beanInfo = Introspector.getBeanInfo(clazz, Object.class);
            for (PropertyDescriptor pd : beanInfo.getPropertyDescriptors()) {
                Method readMethod = pd.getReadMethod();
                if (readMethod == null) {
                    continue;
                }
                propertyNames.add(pd.getName());
                try {
                    if (!readMethod.isAccessible()) {
                        readMethod.setAccessible(true);
                    }
                    values.add(readMethod.invoke(bean));
                } catch (Exception e) {
                    // 单个属性读取失败不影响其他属性
                }
            }
        } catch (Exception e) {
            // 内省失败时退化为仅枚举 public 字段
        }
        for (Field field : clazz.getFields()) {
            if (propertyNames.contains(field.getName())) {
                continue;
            }
            try {
                values.add(field.get(bean));
            } catch (Exception e) {
                // 单个字段读取失败不影响其他字段
            }
        }
        return values;
    }

    /**
     * 判断是否为标量（无子节点的叶子类型）
     */
    private static boolean isScalar(Object obj) {
        return obj instanceof String
            || obj instanceof Number
            || obj instanceof Boolean
            || obj instanceof Character
            || obj.getClass().isEnum();
    }
}
