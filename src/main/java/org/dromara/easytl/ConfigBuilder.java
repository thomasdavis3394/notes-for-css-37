package org.dromara.easytl;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * 配置构建器，支持链式调用创建 TemplateEngineConfig
 *
 * @author gongjun
 * @since 1.0.0
 */
public class ConfigBuilder {

    private boolean cacheEnabled = true;
    private int maxCacheSize = 100;
    private boolean strictMode = false;
    private boolean templateJavaClassImportEnabled = true;
    private String templateBasePath = "";
    private Charset charset = StandardCharsets.UTF_8;

    /**
     * 设置是否启用缓存
     *
     * @param enabled 是否启用
     * @return this，支持链式调用
     */
    public ConfigBuilder cacheEnabled(boolean enabled) {
        this.cacheEnabled = enabled;
        return this;
    }

    /**
     * 设置缓存最大大小
     *
     * @param size 最大缓存数量
     * @return this，支持链式调用
     */
    public ConfigBuilder maxCacheSize(int size) {
        this.maxCacheSize = size;
        return this;
    }

    /**
     * 设置是否启用严格模式
     *
     * @param enabled 是否启用
     * @return this，支持链式调用
     */
    public ConfigBuilder strictMode(boolean enabled) {
        this.strictMode = enabled;
        return this;
    }

    /**
     * 设置是否允许模板内 Java 类 {@code {% import %}}
     *
     * @param enabled 是否允许
     * @return this，支持链式调用
     */
    public ConfigBuilder templateJavaClassImportEnabled(boolean enabled) {
        this.templateJavaClassImportEnabled = enabled;
        return this;
    }

    /**
     * 设置模板基础路径
     *
     * @param path 模板基础路径
     * @return this，支持链式调用
     */
    public ConfigBuilder templateBasePath(String path) {
        this.templateBasePath = path;
        return this;
    }

    /**
     * 设置字符编码
     *
     * @param charset 字符编码
     * @return this，支持链式调用
     */
    public ConfigBuilder charset(Charset charset) {
        this.charset = charset;
        return this;
    }

    /**
     * 构建并返回配置对象
     *
     * @return 配置对象
     */
    public TemplateEngine.TemplateEngineConfig build() {
        return new TemplateEngine.TemplateEngineConfig()
            .setCacheEnabled(cacheEnabled)
            .setMaxCacheSize(maxCacheSize)
            .setStrictMode(strictMode)
            .setTemplateJavaClassImportEnabled(templateJavaClassImportEnabled)
            .setTemplateBasePath(templateBasePath)
            .setCharset(charset);
    }
}