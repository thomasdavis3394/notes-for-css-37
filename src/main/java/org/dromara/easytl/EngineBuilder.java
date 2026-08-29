package org.dromara.easytl;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.HashMap;

/**
 * 引擎构建器，支持链式调用创建 TemplateEngine
 *
 * @author gongjun
 * @since 1.0.0
 */
public class EngineBuilder {

    private boolean cacheEnabled = true;
    private int maxCacheSize = 100;
    private boolean strictMode = false;
    private boolean templateJavaClassImportEnabled = true;
    private String templateBasePath = "";
    private Charset charset = StandardCharsets.UTF_8;
    private Map<String, Class<?>> importedClasses = new HashMap<>();

    /**
     * 设置是否启用缓存
     *
     * @param enabled 是否启用
     * @return this，支持链式调用
     */
    public EngineBuilder cacheEnabled(boolean enabled) {
        this.cacheEnabled = enabled;
        return this;
    }

    /**
     * 设置缓存最大大小
     *
     * @param size 最大缓存数量
     * @return this，支持链式调用
     */
    public EngineBuilder maxCacheSize(int size) {
        this.maxCacheSize = size;
        return this;
    }

    /**
     * 设置是否启用严格模式
     *
     * @param enabled 是否启用
     * @return this，支持链式调用
     */
    public EngineBuilder strictMode(boolean enabled) {
        this.strictMode = enabled;
        return this;
    }

    /**
     * 设置是否允许模板内 Java 类 {@code {% import %}}
     *
     * @param enabled 是否允许
     * @return this，支持链式调用
     */
    public EngineBuilder templateJavaClassImportEnabled(boolean enabled) {
        this.templateJavaClassImportEnabled = enabled;
        return this;
    }

    /**
     * 设置模板基础路径
     *
     * @param path 模板基础路径
     * @return this，支持链式调用
     */
    public EngineBuilder templateBasePath(String path) {
        this.templateBasePath = path;
        return this;
    }

    /**
     * 设置字符编码
     *
     * @param charset 字符编码
     * @return this，支持链式调用
     */
    public EngineBuilder charset(Charset charset) {
        this.charset = charset;
        return this;
    }

    /**
     * 导入Java类，使用简单名称作为别名
     *
     * @param clazz 要导入的Java类
     * @return this，支持链式调用
     * @throws IllegalArgumentException 如果 clazz 为 null
     */
    public EngineBuilder importClass(Class<?> clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("Class cannot be null");
        }
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
    public EngineBuilder importClass(Class<?> clazz, String alias) {
        if (clazz == null) {
            throw new IllegalArgumentException("Class cannot be null");
        }
        if (alias == null || alias.isEmpty()) {
            throw new IllegalArgumentException("Alias cannot be null or empty");
        }
        importedClasses.put(alias, clazz);
        return this;
    }

    /**
     * 构建并返回模板引擎
     *
     * @return 模板引擎实例
     */
    public TemplateEngine build() {
        TemplateEngine.TemplateEngineConfig config = new TemplateEngine.TemplateEngineConfig()
            .setCacheEnabled(cacheEnabled)
            .setMaxCacheSize(maxCacheSize)
            .setStrictMode(strictMode)
            .setTemplateJavaClassImportEnabled(templateJavaClassImportEnabled)
            .setTemplateBasePath(templateBasePath)
            .setCharset(charset);

        TemplateEngine engine = new TemplateEngine(config);
        // 初始化导入的类
        for (Map.Entry<String, Class<?>> entry : importedClasses.entrySet()) {
            engine.importClass(entry.getValue(), entry.getKey());
        }
        return engine;
    }
}