package org.dromara.easytl;

import org.dromara.easytl.exception.ArgumentIndexOutOfBoundsException;
import org.dromara.easytl.exception.TemplateRuntimeException;
import org.dromara.easytl.runtime.ExportRegistry;
import org.dromara.easytl.runtime.StandardFunctions;
import org.dromara.easytl.runtime.TemplateFunction;
import org.dromara.easytl.runtime.UrlEncoder;
import org.dromara.easytl.runtime.UrlEncoders;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 模板上下文
 * <p>
 * 用于存储模板渲染时需要的变量和对象，支持嵌套作用域。
 * 支持动态变量（DynamicVariable），当变量值是 DynamicVariable 类型时，
 * 会自动执行其 compute 方法获取实际值。
 * </p>
 * <p>
 * 使用 ConcurrentHashMap 存储变量，保证 put/get/remove 操作的线程安全性。
 * </p>
 *
 * @author gongjun
 * @since 1.0.0
 * @see TemplateVariable
 */
public class Context {

    /**
     * 用于表示 null 值的标记对象（ConcurrentHashMap 不支持 null 值）
     */
    private static final Object NULL_MARKER = new Object();

    /**
     * 当前作用域的变量存储（线程安全）
     */
    private final ConcurrentHashMap<String, Object> variables;

    /**
     * 父级作用域
     */
    private final Context parent;
    
    /**
     * 参数数组，用于通过索引访问参数（0代表第一个参数，1代表第二个，以此类推）
     */
    private Object[] arguments;

    /**
     * Export 注册表（用于导出变量跟踪）
     */
    private ExportRegistry exportRegistry;

    /**
     * 构造函数，创建根作用域
     */
    public Context() {
        this(null);
    }

    /**
     * 构造函数，创建子作用域
     *
     * @param parent 父级作用域
     */
    public Context(final Context parent) {
        this.variables = new ConcurrentHashMap<>();
        this.parent = parent;
    }

    /**
     * 添加变量到上下文
     *
     * @param name 变量名
     * @param value 变量值（可以为 null）
     */
    public void put(final String name, final Object value) {
        if (value == null) {
            variables.put(name, NULL_MARKER);
        } else {
            variables.put(name, value);
        }
    }

    /**
     * 添加动态变量到上下文
     * <p>
     * 此重载方法提供更好的类型明确性，便于使用 Lambda 表达式创建动态变量。
     * </p>
     *
     * @param name 变量名
     * @param value 动态变量（TemplateVariable 实例）
     * @see TemplateVariable
     */
    public void put(final String name, final TemplateVariable value) {
        if (value == null) {
            variables.put(name, NULL_MARKER);
        } else {
            variables.put(name, value);
        }
    }

    /**
     * 导入Java类到上下文，使用类的简单名称作为别名
     *
     * @param clazz 要导入的Java类
     */
    public void importClass(Class<?> clazz) {
        String alias = clazz.getSimpleName();
        variables.put(alias, clazz);
    }

    /**
     * 导入Java类到上下文，使用指定别名
     *
     * @param clazz 要导入的Java类
     * @param alias 别名
     */
    public void importClass(Class<?> clazz, String alias) {
        variables.put(alias, clazz);
    }

    /**
     * 从上下文获取变量
     * <p>
     * 如果当前作用域找不到变量，会向上查找父级作用域。
     * 如果变量值是 {@link TemplateVariable}，会自动执行并返回结果。
     * </p>
     *
     * @param name 变量名
     * @return 变量值，如果不存在则返回 null
     * @throws TemplateRuntimeException 当动态变量执行出错时抛出
     */
    public Object get(final String name) {
        Object value = getRaw(name);
        if (value instanceof TemplateVariable) {
            try {
                return ((TemplateVariable) value).compute(this);
            } catch (Exception e) {
                throw new TemplateRuntimeException(
                    "Error computing dynamic variable '" + name + "'", e);
            }
        }
        return value;
    }

    /**
     * 从上下文获取原始变量值（不执行动态变量）
     * <p>
     * 如果当前作用域找不到变量，会向上查找父级作用域。
     * 与 {@link #get(String)} 不同，此方法不会执行 {@link TemplateVariable}，
     * 而是直接返回 DynamicVariable 实例本身。
     * </p>
     *
     * @param name 变量名
     * @return 变量值（如果是 DynamicVariable 则返回实例本身，不执行）
     */
    public Object getRaw(final String name) {
        if (variables.containsKey(name)) {
            Object value = variables.get(name);
            return value == NULL_MARKER ? null : value;
        }
        if (parent != null) {
            return parent.getRaw(name);
        }
        return null;
    }

    /**
     * 检查变量是否存在
     * <p>
     * 如果当前作用域找不到变量，会向上查找父级作用域。
     * 注意：如果变量值是 {@link TemplateVariable}，此方法会执行它来判断是否存在。
     * </p>
     *
     * @param name 变量名
     * @return 如果变量存在返回 true，否则返回 false
     */
    public boolean contains(final String name) {
        if (variables.containsKey(name)) {
            return true;
        }
        if (parent != null) {
            return parent.contains(name);
        }
        return false;
    }

    /**
     * 检查变量是否存在（包括 DynamicVariable 类型）
     * <p>
     * 与 {@link #contains(String)} 功能相同，此方法只检查变量是否存在于作用域中。
     * 不会执行动态变量。
     * </p>
     *
     * @param name 变量名
     * @return 如果变量存在返回 true，否则返回 false
     */
    public boolean containsRaw(final String name) {
        if (variables.containsKey(name)) {
            return true;
        }
        if (parent != null) {
            return parent.containsRaw(name);
        }
        return false;
    }

    /**
     * 从当前作用域移除变量
     *
     * @param name 变量名
     * @return 被移除的变量值，如果不存在则返回 null
     */
    public Object remove(final String name) {
        Object value = variables.remove(name);
        return value == NULL_MARKER ? null : value;
    }

    /**
     * 创建子作用域
     *
     * @return 新的子作用域
     */
    public Context createChildContext() {
        Context child = new Context(this);
        child.exportRegistry = this.exportRegistry;  // 共享 ExportRegistry
        return child;
    }

    /**
     * 获取 Export 注册表
     *
     * @return Export 注册表，如果未设置则创建新的
     */
    public ExportRegistry getExportRegistry() {
        if (exportRegistry == null) {
            exportRegistry = new ExportRegistry();
        }
        return exportRegistry;
    }

    /**
     * 设置 Export 注册表
     *
     * @param registry Export 注册表
     */
    public void setExportRegistry(ExportRegistry registry) {
        this.exportRegistry = registry;
    }

    /**
     * 获取父级作用域
     * 
     * @return 父级作用域，如果是根作用域则返回 null
     */
    public Context getParent() {
        return parent;
    }

    /**
     * 清空当前作用域的所有变量
     */
    public void clear() {
        variables.clear();
    }

    /**
     * 获取当前作用域的所有变量（不包括父级作用域）
     * 
     * @return 变量映射的副本
     */
    public Map<String, Object> getVariables() {
        return new HashMap<>(variables);
    }
    
    /**
     * 设置参数数组
     * <p>
     * 参数数组用于通过索引访问参数，例如：{0}、${1}、{{2}}
     * </p>
     *
     * @param arguments 参数数组
     */
    public void setArguments(final Object... arguments) {
        this.arguments = arguments;
    }
    
    /**
     * 通过索引获取参数
     * <p>
     * 如果当前作用域没有参数数组，会向上查找父级作用域
     * </p>
     *
     * @param index 参数索引（0代表第一个参数，1代表第二个，以此类推）
     * @return 参数值，如果索引超出范围或参数数组不存在则返回 null
     */
    public Object getArgument(final int index) {
        if (arguments != null && index >= 0 && index < arguments.length) {
            return arguments[index];
        }
        if (parent != null) {
            return parent.getArgument(index);
        }
        return null;
    }
    
    /**
     * 获取参数数组
     * <p>
     * 如果当前作用域没有参数数组，会向上查找父级作用域
     * </p>
     *
     * @return 参数数组，如果不存在则返回 null
     */
    public Object[] getArguments() {
        if (arguments != null) {
            return arguments;
        }
        if (parent != null) {
            return parent.getArguments();
        }
        return null;
    }

    /**
     * 通过索引获取参数（严格模式，越界报错）
     * <p>
     * 用于 {0} 语法，当索引超出范围时抛出异常
     * </p>
     *
     * @param index 参数索引
     * @return 参数值
     * @throws ArgumentIndexOutOfBoundsException 当索引超出范围时
     */
    public Object getArgumentStrict(final int index) {
        Object[] args = getArguments();
        if (args == null || index < 0 || index >= args.length) {
            throw new ArgumentIndexOutOfBoundsException(index, args == null ? null : args.length);
        }
        return args[index];
    }

    /**
     * 通过索引获取参数值（严格模式，越界或 null 报错）
     * <p>
     * 用于 {$0} 语法，当索引超出范围或值为 null 时抛出异常
     * </p>
     *
     * @param index 参数索引
     * @return 参数值（不为 null）
     * @throws ArgumentIndexOutOfBoundsException 当索引超出范围或值为 null 时
     */
    public Object getArgumentVariableStrict(final int index) {
        Object[] args = getArguments();
        if (args == null || index < 0 || index >= args.length) {
            throw new ArgumentIndexOutOfBoundsException(index, args == null ? null : args.length);
        }
        Object value = args[index];
        if (value == null) {
            throw new ArgumentIndexOutOfBoundsException(index);
        }
        return value;
    }

    // ========== 函数扩展（TemplateFunction SPI） ==========

    /**
     * 当前作用域的函数注册表（懒初始化）
     */
    private ConcurrentHashMap<String, TemplateFunction> functions;

    /**
     * 注册模板函数
     * <p>
     * 注册后即可在模板中以 {@code fn(arg1, ...)} 形式调用（含 JSONPath 过滤谓词内）。
     * 同名函数沿作用域链就近覆盖：子作用域优先于父作用域，注册函数优先于内置函数。
     * </p>
     *
     * @param function 函数实现
     * @return this，支持链式调用
     */
    public Context registerFunction(final TemplateFunction function) {
        if (function == null) {
            throw new IllegalArgumentException("function cannot be null");
        }
        if (functions == null) {
            functions = new ConcurrentHashMap<>();
        }
        functions.put(function.getName(), function);
        return this;
    }

    /**
     * 按名字查找模板函数
     * <p>
     * 查找顺序：当前作用域 → 父级作用域链 → 内置函数（{@link StandardFunctions}，
     * 含 RFC 9535 的 {@code length}/{@code count}/{@code match}/{@code search}/{@code value}）。
     * </p>
     *
     * @param name 函数名
     * @return 函数实现，不存在时返回 null
     */
    public TemplateFunction getFunction(final String name) {
        if (functions != null) {
            TemplateFunction function = functions.get(name);
            if (function != null) {
                return function;
            }
        }
        if (parent != null) {
            return parent.getFunction(name);
        }
        return StandardFunctions.get(name);
    }

    // ========== URL 编码器配置 ==========

    public void setUrlSchemeEncoder(UrlEncoder encoder) {
        put("_urlSchemeEncoder", encoder);
    }

    public UrlEncoder getUrlSchemeEncoder() {
        Object encoder = getRaw("_urlSchemeEncoder");
        return encoder instanceof UrlEncoder ? (UrlEncoder) encoder : UrlEncoders.NOOP;
    }

    public void setUrlUserInfoEncoder(UrlEncoder encoder) {
        put("_urlUserInfoEncoder", encoder);
    }

    public UrlEncoder getUrlUserInfoEncoder() {
        Object encoder = getRaw("_urlUserInfoEncoder");
        return encoder instanceof UrlEncoder ? (UrlEncoder) encoder : UrlEncoders.URL;
    }

    public void setUrlHostEncoder(UrlEncoder encoder) {
        put("_urlHostEncoder", encoder);
    }

    public UrlEncoder getUrlHostEncoder() {
        Object encoder = getRaw("_urlHostEncoder");
        return encoder instanceof UrlEncoder ? (UrlEncoder) encoder : UrlEncoders.NOOP;
    }

    public void setUrlPathEncoder(UrlEncoder encoder) {
        put("_urlPathEncoder", encoder);
    }

    public UrlEncoder getUrlPathEncoder() {
        Object encoder = getRaw("_urlPathEncoder");
        return encoder instanceof UrlEncoder ? (UrlEncoder) encoder : UrlEncoders.URL;
    }

    public void setUrlQueryKeyEncoder(UrlEncoder encoder) {
        put("_urlQueryKeyEncoder", encoder);
    }

    public UrlEncoder getUrlQueryKeyEncoder() {
        Object encoder = getRaw("_urlQueryKeyEncoder");
        return encoder instanceof UrlEncoder ? (UrlEncoder) encoder : UrlEncoders.URL;
    }

    public void setUrlQueryValueEncoder(UrlEncoder encoder) {
        put("_urlQueryValueEncoder", encoder);
    }

    public UrlEncoder getUrlQueryValueEncoder() {
        Object encoder = getRaw("_urlQueryValueEncoder");
        return encoder instanceof UrlEncoder ? (UrlEncoder) encoder : UrlEncoders.URL;
    }

    public void setUrlRefEncoder(UrlEncoder encoder) {
        put("_urlRefEncoder", encoder);
    }

    public UrlEncoder getUrlRefEncoder() {
        Object encoder = getRaw("_urlRefEncoder");
        return encoder instanceof UrlEncoder ? (UrlEncoder) encoder : UrlEncoders.URL;
    }
}

