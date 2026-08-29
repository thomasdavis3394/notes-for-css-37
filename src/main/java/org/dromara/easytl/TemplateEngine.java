package org.dromara.easytl;

import org.dromara.easytl.ast.TemplateNode;
import org.dromara.easytl.compiler.Compiler;
import org.dromara.easytl.compiler.InterpreterCompiler;
import org.dromara.easytl.compiler.TemplateJavaImportValidator;
import org.dromara.easytl.exception.TemplateCompileException;
import org.dromara.easytl.exception.TemplateException;
import org.dromara.easytl.loader.FileTemplateLoader;
import org.dromara.easytl.loader.TemplateLoader;
import org.dromara.easytl.parser.Lexer;
import org.dromara.easytl.parser.Parser;
import org.dromara.easytl.parser.StringSource;
import org.dromara.easytl.parser.TokenStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 模板引擎主类
 * <p>
 * 负责模板的编译和管理，支持配置选项
 * </p>
 * 
 * @author gongjun
 * @since 1.0.0
 */
public class TemplateEngine {

    /**
     * 配置选项
     */
    private final TemplateEngineConfig config;

    /**
     * 模板加载器（用于加载 extends 的外部模板）
     */
    private final TemplateLoader templateLoader;

    /**
     * 模板缓存（LRU 缓存）
     */
    private final Map<String, Template> templateCache;

    /**
     * 缓存键的最大长度限制（避免大源码作为 key 造成空间浪费）
     * 仅在源码长度小于此限制时才进行缓存
     */
    private static final int MAX_CACHE_KEY_LENGTH = 1024;

    /**
     * 缓存读写锁，保证线程安全
     */
    private final ReadWriteLock cacheLock = new ReentrantReadWriteLock();

    /**
     * 导入的Java类映射（别名 -> 类）
     */
    private final ConcurrentHashMap<String, Class<?>> importedClasses = new ConcurrentHashMap<>();

    /**
     * 构造函数，使用默认配置
     */
    public TemplateEngine() {
        this(new TemplateEngineConfig(), null);
    }

    /**
     * 构造函数，使用自定义配置
     * 
     * @param config 配置选项
     */
    public TemplateEngine(final TemplateEngineConfig config) {
        this(config, null);
    }

    /**
     * 构造函数，使用自定义配置和模板加载器
     * 
     * @param config 配置选项
     * @param templateLoader 模板加载器，如果为 null 且配置了 templateBasePath，则自动创建 FileTemplateLoader
     */
    public TemplateEngine(final TemplateEngineConfig config, final TemplateLoader templateLoader) {
        this.config = config;
        // 初始化模板加载器
        if (templateLoader != null) {
            this.templateLoader = templateLoader;
        } else if (config.getTemplateBasePath() != null && !config.getTemplateBasePath().isEmpty()) {
            // 如果配置了 templateBasePath，自动创建 FileTemplateLoader
            this.templateLoader = new FileTemplateLoader(config.getTemplateBasePath(), config.getCharset());
        } else {
            this.templateLoader = null;
        }
        // 如果启用缓存，初始化缓存
        if (config.isCacheEnabled()) {
            final int maxCacheSize = config.getMaxCacheSize();
            this.templateCache = new LinkedHashMap<String, Template>(16, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, Template> eldest) {
                    return size() > maxCacheSize;
                }
            };
        } else {
            this.templateCache = null;
        }
    }

    /**
     * 导入Java类，使用简单名称作为别名
     *
     * @param clazz 要导入的Java类
     * @return this，支持链式调用
     * @throws IllegalArgumentException 如果 clazz 为 null
     */
    public TemplateEngine importClass(Class<?> clazz) {
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
    public TemplateEngine importClass(Class<?> clazz, String alias) {
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
     * 获取当前导入的类映射（不可修改副本）
     *
     * @return 别名到类的映射副本
     */
    public Map<String, Class<?>> getImportedClasses() {
        return new HashMap<>(importedClasses);
    }

    /**
     * 编译模板字符串
     *
     * @param template 模板字符串
     * @return 编译后的模板对象
     * @throws TemplateCompileException 如果编译失败
     */
    public Template compile(final String template) {
        return compile(template, null);
    }

    /**
     * 编译模板字符串，使用额外的导入映射
     *
     * @param template 模板字符串
     * @param importsMap 别名到类的映射
     * @return 编译后的模板对象
     * @throws TemplateCompileException 如果编译失败
     */
    public Template compile(final String template, Map<String, Class<?>> importsMap) {
        if (template == null) {
            throw new TemplateCompileException("Template string cannot be null");
        }

        // 合并导入映射：以 Engine 的导入为基础，importsMap 可以覆盖
        Map<String, Class<?>> mergedImports = new HashMap<>(importedClasses);
        if (importsMap != null) {
            mergedImports.putAll(importsMap);
        }

        // 缓存逻辑：缓存键不考虑 importsMap，保持原有行为
        if (config.isCacheEnabled() && templateCache != null && template.length() <= MAX_CACHE_KEY_LENGTH) {
            final String cacheKey = generateCacheKey(template);

            // 尝试从缓存读取
            cacheLock.readLock().lock();
            try {
                final Template cached = templateCache.get(cacheKey);
                if (cached != null) {
                    return cached;
                }
            } finally {
                cacheLock.readLock().unlock();
            }

            // 缓存未命中，编译模板
            final Template compiled = compileInternal(template, mergedImports);

            // 存入缓存
            cacheLock.writeLock().lock();
            try {
                templateCache.put(cacheKey, compiled);
            } catch (Exception e) {
                // 缓存异常不影响正常编译流程
                // 忽略缓存错误，继续返回编译结果
            } finally {
                cacheLock.writeLock().unlock();
            }

            return compiled;
        } else {
            // 缓存未启用或模板太大，直接编译
            return compileInternal(template, mergedImports);
        }
    }

    /**
     * 编译模板输入流
     *
     * @param input 模板输入流
     * @return 编译后的模板对象
     * @throws TemplateCompileException 如果编译失败或 IO 错误
     */
    public Template compile(final InputStream input) {
        if (input == null) {
            throw new TemplateCompileException("InputStream cannot be null");
        }

        try {
            final String template = readInputStream(input, config.getCharset());
            return compile(template);
        } catch (TemplateCompileException e) {
            throw e;
        } catch (IOException e) {
            throw new TemplateCompileException("Failed to read template from InputStream: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new TemplateCompileException("Failed to compile template from InputStream: " + e.getMessage(), e);
        }
    }

    /**
     * 编译模板文件
     *
     * @param file 模板文件
     * @return 编译后的模板对象
     * @throws TemplateCompileException 如果编译失败、文件不存在或 IO 错误
     */
    public Template compile(final File file) {
        if (file == null) {
            throw new TemplateCompileException("File cannot be null");
        }

        // 检查文件是否存在
        if (!file.exists()) {
            throw new TemplateCompileException("Template file does not exist: " + file.getAbsolutePath());
        }

        // 检查是否为文件（不是目录）
        if (!file.isFile()) {
            throw new TemplateCompileException("Path is not a file: " + file.getAbsolutePath());
        }

        // 检查是否可读
        if (!file.canRead()) {
            throw new TemplateCompileException("Template file is not readable: " + file.getAbsolutePath());
        }

        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            return compile(fileInputStream);
        } catch (TemplateCompileException e) {
            throw e;
        } catch (IOException e) {
            throw new TemplateCompileException("Failed to read template file: " + file.getAbsolutePath() + " - " + e.getMessage(), e);
        } catch (Exception e) {
            throw new TemplateCompileException("Failed to compile template file: " + file.getAbsolutePath() + " - " + e.getMessage(), e);
        }
    }

    /**
     * 从输入流读取模板内容
     *
     * @param input 输入流
     * @param charset 字符编码
     * @return 模板字符串
     * @throws IOException 如果读取失败
     */
    private String readInputStream(final InputStream input, final Charset charset) throws IOException {
        try (java.io.InputStreamReader reader = new java.io.InputStreamReader(input, charset)) {
            final StringBuilder sb = new StringBuilder();
            final char[] buffer = new char[8192];
            int bytesRead;
            while ((bytesRead = reader.read(buffer)) != -1) {
                sb.append(buffer, 0, bytesRead);
            }
            return sb.toString();
        }
    }

    /**
     * 内部编译方法，执行实际的编译逻辑
     *
     * @param template 模板字符串
     * @param importsMap 别名到类的映射
     * @return 编译后的模板对象
     * @throws TemplateCompileException 如果编译失败
     */
    private Template compileInternal(final String template, Map<String, Class<?>> importsMap) {
        try {
            // 1. 词法分析 (Lexer)
            StringSource source = new StringSource(template);
            Lexer lexer = new Lexer(source);
            TokenStream tokenStream = new TokenStream(lexer);

            // 2. 语法解析 (Parser)
            Parser parser = new Parser(tokenStream, template);
            TemplateNode ast = parser.parse();

            TemplateJavaImportValidator.validate(ast, config.isTemplateJavaClassImportEnabled(), template);

            // 3. 编译为 Template 对象（传入模板加载器以支持 extends 功能，传入源代码以支持错误报告）
            Compiler compiler = new InterpreterCompiler(
                templateLoader,
                template,
                config.isTemplateJavaClassImportEnabled(),
                importsMap);
            return compiler.compile(ast);
        } catch (TemplateCompileException e) {
            throw e;
        } catch (TemplateException e) {
            // 使用 getRawMessage() 获取原始消息，避免重复格式化
            throw new TemplateCompileException(e.getRawMessage(), e.getPosition(), e.getSource());
        } catch (Exception e) {
            throw new TemplateCompileException("Failed to compile template: " + e.getMessage(), e);
        }
    }

    /**
     * 生成缓存键（使用模板源码内容本身，不使用 MD5 或 hash，避免碰撞风险）
     * <p>
     * 注意：此方法仅在模板长度不超过 MAX_CACHE_KEY_LENGTH 时才会被调用
     * </p>
     *
     * @param template 模板字符串
     * @return 缓存键（模板源码内容本身）
     */
    private String generateCacheKey(final String template) {
        // 直接返回模板源码内容本身，不使用 MD5 或 hash，避免碰撞风险
        return template;
    }

    /**
     * 清除模板缓存
     * <p>
     * 如果缓存未启用，此方法不执行任何操作
     * </p>
     */
    public void clearCache() {
        if (config.isCacheEnabled() && templateCache != null) {
            cacheLock.writeLock().lock();
            try {
                templateCache.clear();
            } finally {
                cacheLock.writeLock().unlock();
            }
        }
    }

    /**
     * 获取当前缓存大小
     *
     * @return 缓存中模板的数量，如果缓存未启用返回 0
     */
    public int getCacheSize() {
        if (config.isCacheEnabled() && templateCache != null) {
            cacheLock.readLock().lock();
            try {
                return templateCache.size();
            } finally {
                cacheLock.readLock().unlock();
            }
        }
        return 0;
    }

    /**
     * 获取配置选项
     * 
     * @return 配置选项
     */
    public TemplateEngineConfig getConfig() {
        return config;
    }

    /**
     * 获取模板加载器
     * 
     * @return 模板加载器，如果未配置则返回 null
     */
    public TemplateLoader getTemplateLoader() {
        return templateLoader;
    }

    /**
     * 模板引擎配置类
     */
    public static class TemplateEngineConfig {

        /**
         * 表达式语法类型
         */
        public enum BraceSyntax {
            /**
             * 单花括号语法：{expression}
             */
            SINGLE,

            /**
             * 双花括号语法：{{expression}}
             */
            DOUBLE,

            /**
             * 同时支持单花括号和双花括号
             */
            BOTH
        }

        /**
         * 花括号语法类型，默认为单花括号
         */
        private BraceSyntax braceSyntax = BraceSyntax.SINGLE;

        /**
         * 是否启用严格模式，默认为 false
         * <p>
         * 严格模式下，访问不存在的变量会抛出异常
         * </p>
         */
        private boolean strictMode = false;

        /**
         * 是否允许模板内 Java 类 import（{% import %}），默认为 true
         */
        private boolean templateJavaClassImportEnabled = true;

        /**
         * 是否启用缓存，默认为 true
         */
        private boolean cacheEnabled = true;

        /**
         * 缓存最大大小，默认为 100
         */
        private int maxCacheSize = 100;

        /**
         * 模板基础路径，用于 extends 功能
         */
        private String templateBasePath = "";

        /**
         * 字符编码，默认为 UTF-8
         */
        private Charset charset = StandardCharsets.UTF_8;

        /**
         * 获取花括号语法类型
         * 
         * @return 花括号语法类型
         */
        public BraceSyntax getBraceSyntax() {
            return braceSyntax;
        }

        /**
         * 设置花括号语法类型
         * 
         * @param braceSyntax 花括号语法类型
         * @return 配置对象本身，支持链式调用
         */
        public TemplateEngineConfig setBraceSyntax(final BraceSyntax braceSyntax) {
            this.braceSyntax = braceSyntax;
            return this;
        }

        /**
         * 是否启用严格模式
         * 
         * @return 如果启用严格模式返回 true，否则返回 false
         */
        public boolean isStrictMode() {
            return strictMode;
        }

        /**
         * 设置是否启用严格模式
         * 
         * @param strictMode 是否启用严格模式
         * @return 配置对象本身，支持链式调用
         */
        public TemplateEngineConfig setStrictMode(final boolean strictMode) {
            this.strictMode = strictMode;
            return this;
        }

        /**
         * 是否允许模板内 Java 类 import（{% import %}）
         *
         * @return 如果允许返回 true，否则返回 false
         */
        public boolean isTemplateJavaClassImportEnabled() {
            return templateJavaClassImportEnabled;
        }

        /**
         * 设置是否允许模板内 Java 类 import（{% import %}）
         *
         * @param templateJavaClassImportEnabled 是否允许
         * @return 配置对象本身，支持链式调用
         */
        public TemplateEngineConfig setTemplateJavaClassImportEnabled(final boolean templateJavaClassImportEnabled) {
            this.templateJavaClassImportEnabled = templateJavaClassImportEnabled;
            return this;
        }

        /**
         * 是否启用缓存
         * 
         * @return 如果启用缓存返回 true，否则返回 false
         */
        public boolean isCacheEnabled() {
            return cacheEnabled;
        }

        /**
         * 设置是否启用缓存
         * 
         * @param cacheEnabled 是否启用缓存
         * @return 配置对象本身，支持链式调用
         */
        public TemplateEngineConfig setCacheEnabled(final boolean cacheEnabled) {
            this.cacheEnabled = cacheEnabled;
            return this;
        }

        /**
         * 获取缓存最大大小
         * 
         * @return 缓存最大大小
         */
        public int getMaxCacheSize() {
            return maxCacheSize;
        }

        /**
         * 设置缓存最大大小
         * 
         * @param maxCacheSize 缓存最大大小，必须大于 0
         * @return 配置对象本身，支持链式调用
         * @throws IllegalArgumentException 如果 maxCacheSize 小于等于 0
         */
        public TemplateEngineConfig setMaxCacheSize(final int maxCacheSize) {
            if (maxCacheSize <= 0) {
                throw new IllegalArgumentException("Max cache size must be greater than 0");
            }
            this.maxCacheSize = maxCacheSize;
            return this;
        }

        /**
         * 获取模板基础路径
         * 
         * @return 模板基础路径
         */
        public String getTemplateBasePath() {
            return templateBasePath;
        }

        /**
         * 设置模板基础路径
         * 
         * @param templateBasePath 模板基础路径
         * @return 配置对象本身，支持链式调用
         */
        public TemplateEngineConfig setTemplateBasePath(final String templateBasePath) {
            this.templateBasePath = templateBasePath;
            return this;
        }

        /**
         * 获取字符编码
         * 
         * @return 字符编码
         */
        public Charset getCharset() {
            return charset;
        }

        /**
         * 设置字符编码
         * 
         * @param charset 字符编码，不能为 null
         * @return 配置对象本身，支持链式调用
         * @throws IllegalArgumentException 如果 charset 为 null
         */
        public TemplateEngineConfig setCharset(final Charset charset) {
            if (charset == null) {
                throw new IllegalArgumentException("Charset cannot be null");
            }
            this.charset = charset;
            return this;
        }
    }
}

