package org.dromara.easytl.runtime;

import org.dromara.easytl.exception.TemplateRuntimeException;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * 内置模板函数（RFC 9535 函数扩展）
 *
 * <p>通过 {@link TemplateFunction} 扩展接口实现，默认全局可用，无需注册；
 * 可被 {@code Context.registerFunction(...)} 注册的同名函数或上下文变量覆盖。</p>
 *
 * <p>包含函数：</p>
 * <ul>
 * <li>{@code length(x)} — 字符串长度 / 集合、Map、数组的元素个数</li>
 * <li>{@code count(x)} — 节点/元素计数：null → 0，标量 → 1，集合/Map/数组 → 元素个数</li>
 * <li>{@code match(str, regex)} — 全串正则匹配（regex 可为字符串或正则字面量）</li>
 * <li>{@code search(str, regex)} — 子串正则搜索（regex 可为字符串或正则字面量）</li>
 * <li>{@code value(x)} — 单元素集合取其唯一元素；空或多元素 → null；标量原样返回</li>
 * <li>{@code assert(cond[, message|lambda])} — 契约断言：cond 为假时抛出
 * {@code AssertionException}（可识别比较/包含/正则运算给出友好消息），
 * 或交给 lambda 失败处理器</li>
 * </ul>
 *
 * @author EasyTL Team
 */
public final class StandardFunctions {

    private static final Map<String, TemplateFunction> BUILTINS;

    static {
        Map<String, TemplateFunction> map = new HashMap<>();
        register(map, new LengthFunction());
        register(map, new CountFunction());
        register(map, new MatchFunction());
        register(map, new SearchFunction());
        register(map, new ValueFunction());
        register(map, new AssertFunction());
        BUILTINS = Collections.unmodifiableMap(map);
    }

    private StandardFunctions() {
        // 工具类，禁止实例化
    }

    private static void register(Map<String, TemplateFunction> map, TemplateFunction function) {
        map.put(function.getName(), function);
    }

    /**
     * 按名字查找内置函数
     *
     * @param name 函数名
     * @return 内置函数，不存在时返回 null
     */
    public static TemplateFunction get(String name) {
        return BUILTINS.get(name);
    }

    /**
     * 获取全部内置函数（只读视图）
     *
     * @return 函数名 → 内置函数
     */
    public static Map<String, TemplateFunction> builtins() {
        return BUILTINS;
    }

    private static void checkArgCount(String name, List<Object> args, int expected) {
        if (args.size() != expected) {
            throw new TemplateRuntimeException(
                name + "() expects " + expected + " argument(s), but got " + args.size());
        }
    }

    private static String typeName(Object value) {
        return value == null ? "null" : value.getClass().getName();
    }

    /**
     * RFC 9535 {@code length()}：字符串长度 / 集合、Map、数组的元素个数
     */
    static final class LengthFunction implements TemplateFunction {
        @Override
        public String getName() {
            return "length";
        }

        @Override
        public Object call(List<Object> args) {
            checkArgCount(getName(), args, 1);
            Object value = args.get(0);
            if (value instanceof String) {
                return ((String) value).length();
            }
            if (value instanceof Collection) {
                return ((Collection<?>) value).size();
            }
            if (value instanceof Map) {
                return ((Map<?, ?>) value).size();
            }
            if (value != null && value.getClass().isArray()) {
                return Array.getLength(value);
            }
            throw new TemplateRuntimeException(
                "length() expects a string, collection, map or array, but got " + typeName(value));
        }
    }

    /**
     * RFC 9535 {@code count()}：节点计数；null → 0，标量 → 1，集合/Map/数组 → 元素个数
     */
    static final class CountFunction implements TemplateFunction {
        @Override
        public String getName() {
            return "count";
        }

        @Override
        public Object call(List<Object> args) {
            checkArgCount(getName(), args, 1);
            Object value = args.get(0);
            if (value == null) {
                return 0;
            }
            if (value instanceof Collection) {
                return ((Collection<?>) value).size();
            }
            if (value instanceof Map) {
                return ((Map<?, ?>) value).size();
            }
            if (value.getClass().isArray()) {
                return Array.getLength(value);
            }
            return 1;
        }
    }

    /**
     * RFC 9535 {@code match()}：全串正则匹配
     */
    static final class MatchFunction implements TemplateFunction {
        @Override
        public String getName() {
            return "match";
        }

        @Override
        public Object call(List<Object> args) {
            checkArgCount(getName(), args, 2);
            return toPattern(getName(), args.get(1)).matcher(toStringArg(getName(), args.get(0))).matches();
        }
    }

    /**
     * RFC 9535 {@code search()}：子串正则搜索
     */
    static final class SearchFunction implements TemplateFunction {
        @Override
        public String getName() {
            return "search";
        }

        @Override
        public Object call(List<Object> args) {
            checkArgCount(getName(), args, 2);
            return toPattern(getName(), args.get(1)).matcher(toStringArg(getName(), args.get(0))).find();
        }
    }

    /**
     * RFC 9535 {@code value()}：单元素集合取唯一元素；空或多元素 → null；标量原样返回
     */
    static final class ValueFunction implements TemplateFunction {
        @Override
        public String getName() {
            return "value";
        }

        @Override
        public Object call(List<Object> args) {
            checkArgCount(getName(), args, 1);
            Object value = args.get(0);
            if (value == null) {
                return null;
            }
            if (value instanceof List) {
                List<?> list = (List<?>) value;
                return list.size() == 1 ? list.get(0) : null;
            }
            if (value instanceof Collection) {
                Collection<?> collection = (Collection<?>) value;
                return collection.size() == 1 ? collection.iterator().next() : null;
            }
            if (value.getClass().isArray()) {
                return Array.getLength(value) == 1 ? Array.get(value, 0) : null;
            }
            if (value instanceof Map) {
                throw new TemplateRuntimeException("value() does not support map, but got " + typeName(value));
            }
            return value;
        }
    }

    private static String toStringArg(String name, Object value) {
        if (value instanceof String) {
            return (String) value;
        }
        throw new TemplateRuntimeException(
            name + "() expects a string as first argument, but got " + typeName(value));
    }

    private static Pattern toPattern(String name, Object value) {
        if (value instanceof Pattern) {
            return (Pattern) value;
        }
        if (value instanceof String) {
            try {
                return Pattern.compile((String) value);
            } catch (PatternSyntaxException e) {
                throw new TemplateRuntimeException(name + "() got an invalid regex: " + e.getMessage());
            }
        }
        throw new TemplateRuntimeException(
            name + "() expects a string or regex as second argument, but got " + typeName(value));
    }
}
