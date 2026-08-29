package org.dromara.easytl;

import org.dromara.easytl.exception.TemplateCompileException;
import org.dromara.easytl.runtime.TemplateFunction;

import java.util.HashMap;
import java.util.Map;

/**
 * 模板构建器，支持延迟编译和链式调用
 *
 * @author gongjun
 * @since 1.0.0
 */
public class TemplateBuilder {

    private final String source;
    private final ContextBuilder contextBuilder;
    private TemplateEngine engine;
    private Boolean cacheEnabled;
    private Boolean strictMode;
    private Boolean templateJavaClassImportEnabled;
    private Map<String, Class<?>> importedClasses;

    /**
     * 构造函数
     *
     * @param source 模板源码
     */
    public TemplateBuilder(String source) {
        this.source = source;
        this.contextBuilder = new ContextBuilder();
    }

    /**
     * 设置使用的模板引擎
     * <p>
     * When an explicit engine is supplied, compilation uses that engine's
     * {@link TemplateEngine#getConfig() TemplateEngineConfig} only; this builder's
     * {@link #templateJavaClassImportEnabled(boolean)} does not modify the supplied engine.
     * </p>
     *
     * @param engine 模板引擎
     * @return this，支持链式调用
     */
    public TemplateBuilder engine(TemplateEngine engine) {
        this.engine = engine;
        return this;
    }

    /**
     * 设置是否启用缓存
     *
     * @param enabled 是否启用
     * @return this，支持链式调用
     */
    public TemplateBuilder cacheEnabled(boolean enabled) {
        this.cacheEnabled = enabled;
        return this;
    }

    /**
     * 设置是否启用严格模式
     *
     * @param enabled 是否启用
     * @return this，支持链式调用
     */
    public TemplateBuilder strictMode(boolean enabled) {
        this.strictMode = enabled;
        return this;
    }

    /**
     * 设置是否允许模板中的 Java 类 import 语句（仅在使用隐式 {@link EngineBuilder} 时生效）
     *
     * @param enabled 是否允许
     * @return this，支持链式调用
     */
    public TemplateBuilder templateJavaClassImportEnabled(boolean enabled) {
        this.templateJavaClassImportEnabled = enabled;
        return this;
    }

    /**
     * 添加变量
     *
     * @param name 变量名
     * @param value 变量值
     * @return this，支持链式调用
     */
    public TemplateBuilder put(String name, Object value) {
        contextBuilder.put(name, value);
        return this;
    }

    /**
     * 添加动态变量
     *
     * @param name 变量名
     * @param value 动态变量
     * @return this，支持链式调用
     */
    public TemplateBuilder put(String name, TemplateVariable value) {
        contextBuilder.put(name, value);
        return this;
    }

    /**
     * 注册模板函数（TemplateFunction SPI），模板内以 {@code fn(arg1, ...)} 调用
     *
     * @param function 函数实现
     * @return this，支持链式调用
     */
    public TemplateBuilder function(TemplateFunction function) {
        contextBuilder.function(function);
        return this;
    }

    /**
     * 设置上下文（会覆盖之前 put 的变量）
     *
     * @param context 上下文对象
     * @return this，支持链式调用
     */
    public TemplateBuilder context(Context context) {
        // 合并上下文变量
        Map<String, Object> vars = context.getVariables();
        for (Map.Entry<String, Object> entry : vars.entrySet()) {
            contextBuilder.put(entry.getKey(), entry.getValue());
        }
        // 复制参数数组
        Object[] args = context.getArguments();
        if (args != null) {
            contextBuilder.arguments(args);
        }
        return this;
    }

    /**
     * 设置数据 Map（会覆盖之前 put 的变量）
     *
     * @param data 数据 Map
     * @return this，支持链式调用
     */
    public TemplateBuilder data(Map<String, Object> data) {
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            contextBuilder.put(entry.getKey(), entry.getValue());
        }
        return this;
    }

    /**
     * 设置参数数组
     * <p>
     * 参数数组用于通过索引访问参数，例如：{0}、${1}、{{2}}
     * </p>
     *
     * @param arguments 参数数组
     * @return this，支持链式调用
     */
    public TemplateBuilder arguments(Object... arguments) {
        contextBuilder.arguments(arguments);
        return this;
    }

    /**
     * 确保导入类映射已初始化
     */
    private void ensureImportedClasses() {
        if (importedClasses == null) {
            importedClasses = new HashMap<>();
        }
    }

    /**
     * 导入Java类，使用简单名称作为别名
     *
     * @param clazz 要导入的Java类
     * @return this，支持链式调用
     * @throws IllegalArgumentException 如果 clazz 为 null
     */
    public TemplateBuilder importClass(Class<?> clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("Class cannot be null");
        }
        ensureImportedClasses();
        importedClasses.put(clazz.getSimpleName(), clazz);
        return this;
    }

    /**
     * 导入Java类，使用指定别名
     *
     * @param clazz 要导入的Java类
     * @param alias 别名
     * @return this，支持链式调用
     * @throws IllegalArgumentException 如果 clazz 为 null 或 alias 为 null/空字符串
     */
    public TemplateBuilder importClass(Class<?> clazz, String alias) {
        if (clazz == null) {
            throw new IllegalArgumentException("Class cannot be null");
        }
        if (alias == null || alias.isEmpty()) {
            throw new IllegalArgumentException("Alias cannot be null or empty");
        }
        ensureImportedClasses();
        importedClasses.put(alias, clazz);
        return this;
    }

    /**
     * 编译模板（不渲染）
     *
     * @return 编译后的模板
     * @throws TemplateCompileException 如果导入别名冲突
     */
    public Template compile() {
        TemplateEngine engine = getOrCreateEngine();

        // 获取 Engine 的导入
        Map<String, Class<?>> engineImports = engine.getImportedClasses();

        // 检测冲突
        if (importedClasses != null) {
            for (Map.Entry<String, Class<?>> entry : importedClasses.entrySet()) {
                String alias = entry.getKey();
                Class<?> builderClass = entry.getValue();
                Class<?> engineClass = engineImports.get(alias);

                if (engineClass != null && !engineClass.equals(builderClass)) {
                    throw new TemplateCompileException(
                        "Import alias conflict: '" + alias + "' already defined as " +
                        engineClass.getName() + " in engine, cannot redefine as " +
                        builderClass.getName());
                }
            }
        }

        // 合并映射（TemplateBuilder 覆盖 Engine 同名同类）
        Map<String, Class<?>> mergedImports = new HashMap<>(engineImports);
        if (importedClasses != null) {
            mergedImports.putAll(importedClasses);
        }

        return engine.compile(source, mergedImports);
    }

    /**
     * 编译并渲染模板
     *
     * @return 渲染结果
     */
    public String render() {
        Template template = compile();
        return template.render(contextBuilder.build());
    }

    /**
     * 获取或创建引擎
     */
    private TemplateEngine getOrCreateEngine() {
        if (engine != null) {
            return engine;
        }

        EngineBuilder builder = new EngineBuilder();
        if (cacheEnabled != null) {
            builder.cacheEnabled(cacheEnabled);
        }
        if (strictMode != null) {
            builder.strictMode(strictMode);
        }
        if (templateJavaClassImportEnabled != null) {
            builder.templateJavaClassImportEnabled(templateJavaClassImportEnabled);
        }
        return builder.build();
    }
}