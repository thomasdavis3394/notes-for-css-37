# EasyTL 工具类实现计划

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 创建用户友好的 EasyTL 工具类作为项目对外统一接口，支持链式调用风格。

**Architecture:** 采用静态工具类 + 多个 Builder 类的设计。EasyTL 作为唯一入口，返回不同类型的 Builder 对象支持链式调用。TemplateBuilder 支持延迟编译，在 render() 时才执行实际编译。

**Tech Stack:** Java 8, 无第三方依赖, Maven

---

## Task 1: 创建 ContextBuilder 类

**Files:**
- Create: `src/main/java/org/dromara/easytl/ContextBuilder.java`
- Test: `src/test/java/org/dromara/easytl/ContextBuilderTest.java`

**Step 1: Write the failing test**

```java
package org.dromara.easytl;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ContextBuilderTest {

    @Test
    void testPutAndBuild() {
        Context ctx = new ContextBuilder()
            .put("name", "张三")
            .put("age", 25)
            .build();

        assertEquals("张三", ctx.get("name"));
        assertEquals(25, ctx.get("age"));
    }

    @Test
    void testPutNull() {
        Context ctx = new ContextBuilder()
            .put("nullVar", null)
            .build();

        assertNull(ctx.get("nullVar"));
        assertTrue(ctx.contains("nullVar"));
    }

    @Test
    void testPutTemplateVariable() {
        Context ctx = new ContextBuilder()
            .put("name", "World")
            .put("greeting", (TemplateVariable) c -> "Hello, " + c.get("name"))
            .build();

        assertEquals("Hello, World", ctx.get("greeting"));
    }

    @Test
    void testEmptyBuilder() {
        Context ctx = new ContextBuilder().build();
        assertNull(ctx.get("any"));
        assertFalse(ctx.contains("any"));
    }

    @Test
    void testChaining() {
        ContextBuilder builder = new ContextBuilder();
        assertSame(builder, builder.put("a", 1));
        assertSame(builder, builder.put("b", 2));
    }
}
```

**Step 2: Run test to verify it fails**

Run: `mvn test -Dtest=ContextBuilderTest -q`
Expected: FAIL with "Cannot resolve symbol ContextBuilder"

**Step 3: Write minimal implementation**

```java
package org.dromara.easytl;

/**
 * 上下文构建器，支持链式调用创建 Context
 *
 * @author gongjun
 * @since 1.0.0
 */
public class ContextBuilder {

    private final Context context;

    /**
     * 构造函数
     */
    public ContextBuilder() {
        this.context = new Context();
    }

    /**
     * 添加变量
     *
     * @param name 变量名
     * @param value 变量值
     * @return this，支持链式调用
     */
    public ContextBuilder put(String name, Object value) {
        context.put(name, value);
        return this;
    }

    /**
     * 添加动态变量
     *
     * @param name 变量名
     * @param value 动态变量
     * @return this，支持链式调用
     */
    public ContextBuilder put(String name, TemplateVariable value) {
        context.put(name, value);
        return this;
    }

    /**
     * 构建并返回 Context
     *
     * @return 构建好的 Context 对象
     */
    public Context build() {
        return context;
    }
}
```

**Step 4: Run test to verify it passes**

Run: `mvn test -Dtest=ContextBuilderTest -q`
Expected: PASS

**Step 5: Commit**

```bash
git add src/main/java/org/dromara/easytl/ContextBuilder.java src/test/java/org/dromara/easytl/ContextBuilderTest.java
git commit -m "feat: add ContextBuilder for fluent context creation"
```

---

## Task 2: 创建 ConfigBuilder 类

**Files:**
- Create: `src/main/java/org/dromara/easytl/ConfigBuilder.java`
- Test: `src/test/java/org/dromara/easytl/ConfigBuilderTest.java`

**Step 1: Write the failing test**

```java
package org.dromara.easytl;

import org.junit.jupiter.api.Test;
import java.nio.charset.StandardCharsets;
import static org.junit.jupiter.api.Assertions.*;

class ConfigBuilderTest {

    @Test
    void testDefaultConfig() {
        TemplateEngine.TemplateEngineConfig config = new ConfigBuilder().build();

        assertTrue(config.isCacheEnabled());
        assertEquals(100, config.getMaxCacheSize());
        assertFalse(config.isStrictMode());
        assertEquals(StandardCharsets.UTF_8, config.getCharset());
    }

    @Test
    void testCacheEnabled() {
        TemplateEngine.TemplateEngineConfig config = new ConfigBuilder()
            .cacheEnabled(false)
            .build();

        assertFalse(config.isCacheEnabled());
    }

    @Test
    void testMaxCacheSize() {
        TemplateEngine.TemplateEngineConfig config = new ConfigBuilder()
            .maxCacheSize(500)
            .build();

        assertEquals(500, config.getMaxCacheSize());
    }

    @Test
    void testStrictMode() {
        TemplateEngine.TemplateEngineConfig config = new ConfigBuilder()
            .strictMode(true)
            .build();

        assertTrue(config.isStrictMode());
    }

    @Test
    void testTemplateBasePath() {
        TemplateEngine.TemplateEngineConfig config = new ConfigBuilder()
            .templateBasePath("/templates")
            .build();

        assertEquals("/templates", config.getTemplateBasePath());
    }

    @Test
    void testCharset() {
        TemplateEngine.TemplateEngineConfig config = new ConfigBuilder()
            .charset(StandardCharsets.ISO_8859_1)
            .build();

        assertEquals(StandardCharsets.ISO_8859_1, config.getCharset());
    }

    @Test
    void testChaining() {
        ConfigBuilder builder = new ConfigBuilder();
        assertSame(builder, builder.cacheEnabled(true));
        assertSame(builder, builder.maxCacheSize(200));
        assertSame(builder, builder.strictMode(true));
    }

    @Test
    void testAllOptions() {
        TemplateEngine.TemplateEngineConfig config = new ConfigBuilder()
            .cacheEnabled(false)
            .maxCacheSize(1000)
            .strictMode(true)
            .templateBasePath("/my/templates")
            .charset(StandardCharsets.UTF_8)
            .build();

        assertFalse(config.isCacheEnabled());
        assertEquals(1000, config.getMaxCacheSize());
        assertTrue(config.isStrictMode());
        assertEquals("/my/templates", config.getTemplateBasePath());
        assertEquals(StandardCharsets.UTF_8, config.getCharset());
    }
}
```

**Step 2: Run test to verify it fails**

Run: `mvn test -Dtest=ConfigBuilderTest -q`
Expected: FAIL with "Cannot resolve symbol ConfigBuilder"

**Step 3: Write minimal implementation**

```java
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
            .setTemplateBasePath(templateBasePath)
            .setCharset(charset);
    }
}
```

**Step 4: Run test to verify it passes**

Run: `mvn test -Dtest=ConfigBuilderTest -q`
Expected: PASS

**Step 5: Commit**

```bash
git add src/main/java/org/dromara/easytl/ConfigBuilder.java src/test/java/org/dromara/easytl/ConfigBuilderTest.java
git commit -m "feat: add ConfigBuilder for fluent config creation"
```

---

## Task 3: 创建 EngineBuilder 类

**Files:**
- Create: `src/main/java/org/dromara/easytl/EngineBuilder.java`
- Test: `src/test/java/org/dromara/easytl/EngineBuilderTest.java`

**Step 1: Write the failing test**

```java
package org.dromara.easytl;

import org.junit.jupiter.api.Test;
import java.nio.charset.StandardCharsets;
import static org.junit.jupiter.api.Assertions.*;

class EngineBuilderTest {

    @Test
    void testDefaultEngine() {
        TemplateEngine engine = new EngineBuilder().build();

        assertNotNull(engine);
        assertTrue(engine.getConfig().isCacheEnabled());
        assertEquals(100, engine.getConfig().getMaxCacheSize());
    }

    @Test
    void testCacheEnabled() {
        TemplateEngine engine = new EngineBuilder()
            .cacheEnabled(false)
            .build();

        assertFalse(engine.getConfig().isCacheEnabled());
    }

    @Test
    void testMaxCacheSize() {
        TemplateEngine engine = new EngineBuilder()
            .maxCacheSize(500)
            .build();

        assertEquals(500, engine.getConfig().getMaxCacheSize());
    }

    @Test
    void testStrictMode() {
        TemplateEngine engine = new EngineBuilder()
            .strictMode(true)
            .build();

        assertTrue(engine.getConfig().isStrictMode());
    }

    @Test
    void testTemplateBasePath() {
        TemplateEngine engine = new EngineBuilder()
            .templateBasePath("/templates")
            .build();

        assertEquals("/templates", engine.getConfig().getTemplateBasePath());
        assertNotNull(engine.getTemplateLoader());
    }

    @Test
    void testCharset() {
        TemplateEngine engine = new EngineBuilder()
            .charset(StandardCharsets.ISO_8859_1)
            .build();

        assertEquals(StandardCharsets.ISO_8859_1, engine.getConfig().getCharset());
    }

    @Test
    void testChaining() {
        EngineBuilder builder = new EngineBuilder();
        assertSame(builder, builder.cacheEnabled(true));
        assertSame(builder, builder.maxCacheSize(200));
        assertSame(builder, builder.strictMode(true));
    }

    @Test
    void testCompileWithBuiltEngine() {
        TemplateEngine engine = new EngineBuilder()
            .cacheEnabled(true)
            .build();

        Template template = engine.compile("Hello, {name}!");
        Context ctx = new Context();
        ctx.put("name", "World");

        assertEquals("Hello, World!", template.render(ctx));
    }
}
```

**Step 2: Run test to verify it fails**

Run: `mvn test -Dtest=EngineBuilderTest -q`
Expected: FAIL with "Cannot resolve symbol EngineBuilder"

**Step 3: Write minimal implementation**

```java
package org.dromara.easytl;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

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
    private String templateBasePath = "";
    private Charset charset = StandardCharsets.UTF_8;

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
     * 构建并返回模板引擎
     *
     * @return 模板引擎实例
     */
    public TemplateEngine build() {
        TemplateEngine.TemplateEngineConfig config = new TemplateEngine.TemplateEngineConfig()
            .setCacheEnabled(cacheEnabled)
            .setMaxCacheSize(maxCacheSize)
            .setStrictMode(strictMode)
            .setTemplateBasePath(templateBasePath)
            .setCharset(charset);

        return new TemplateEngine(config);
    }
}
```

**Step 4: Run test to verify it passes**

Run: `mvn test -Dtest=EngineBuilderTest -q`
Expected: PASS

**Step 5: Commit**

```bash
git add src/main/java/org/dromara/easytl/EngineBuilder.java src/test/java/org/dromara/easytl/EngineBuilderTest.java
git commit -m "feat: add EngineBuilder for fluent engine creation"
```

---

## Task 4: 创建 TemplateBuilder 类（延迟编译）

**Files:**
- Create: `src/main/java/org/dromara/easytl/TemplateBuilder.java`
- Test: `src/test/java/org/dromara/easytl/TemplateBuilderTest.java`

**Step 1: Write the failing test**

```java
package org.dromara.easytl;

import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class TemplateBuilderTest {

    @Test
    void testSimpleRender() {
        String result = new TemplateBuilder("Hello, {name}!")
            .put("name", "World")
            .render();

        assertEquals("Hello, World!", result);
    }

    @Test
    void testRenderWithContext() {
        Context ctx = new Context();
        ctx.put("name", "张三");
        ctx.put("city", "北京");

        String result = new TemplateBuilder("{name} 来自 {city}")
            .context(ctx)
            .render();

        assertEquals("张三 来自 北京", result);
    }

    @Test
    void testRenderWithMap() {
        Map<String, Object> data = new HashMap<>();
        data.put("name", "李四");

        String result = new TemplateBuilder("Hello, {name}!")
            .data(data)
            .render();

        assertEquals("Hello, 李四!", result);
    }

    @Test
    void testCacheEnabled() {
        String result = new TemplateBuilder("Hello, {name}!")
            .cacheEnabled(false)
            .put("name", "World")
            .render();

        assertEquals("Hello, World!", result);
    }

    @Test
    void testStrictMode() {
        String result = new TemplateBuilder("Hello, {name}!")
            .strictMode(true)
            .put("name", "World")
            .render();

        assertEquals("Hello, World!", result);
    }

    @Test
    void testWithEngine() {
        TemplateEngine engine = new EngineBuilder()
            .cacheEnabled(false)
            .build();

        String result = new TemplateBuilder("Hello, {name}!")
            .engine(engine)
            .put("name", "World")
            .render();

        assertEquals("Hello, World!", result);
    }

    @Test
    void testCompile() {
        Template template = new TemplateBuilder("Hello, {name}!")
            .cacheEnabled(true)
            .compile();

        assertNotNull(template);

        Context ctx = new Context();
        ctx.put("name", "World");
        assertEquals("Hello, World!", template.render(ctx));
    }

    @Test
    void testCompileThenRender() {
        TemplateBuilder builder = new TemplateBuilder("Hello, {name}!")
            .put("name", "World");

        Template template = builder.compile();
        String result = template.render(new Context());

        // compile() 不包含 put 的变量
        assertNotEquals("Hello, World!", result);
    }

    @Test
    void testMultipleRenders() {
        TemplateBuilder builder = new TemplateBuilder("Hello, {name}!");

        String r1 = builder.put("name", "张三").render();
        String r2 = builder.put("name", "李四").render();

        assertEquals("Hello, 张三!", r1);
        assertEquals("Hello, 李四!", r2);
    }

    @Test
    void testChaining() {
        TemplateBuilder builder = new TemplateBuilder("test");
        assertSame(builder, builder.cacheEnabled(true));
        assertSame(builder, builder.strictMode(true));
        assertSame(builder, builder.put("key", "value"));
    }

    @Test
    void testTemplateVariable() {
        String result = new TemplateBuilder("Result: {computed}")
            .put("a", 10)
            .put("b", 20)
            .put("computed", (TemplateVariable) ctx -> {
                int a = (int) ctx.get("a");
                int b = (int) ctx.get("b");
                return a + b;
            })
            .render();

        assertEquals("Result: 30", result);
    }
}
```

**Step 2: Run test to verify it fails**

Run: `mvn test -Dtest=TemplateBuilderTest -q`
Expected: FAIL with "Cannot resolve symbol TemplateBuilder"

**Step 3: Write minimal implementation**

```java
package org.dromara.easytl;

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
     * 设置上下文（会覆盖之前 put 的变量）
     *
     * @param context 上下文对象
     * @return this，支持链式调用
     */
    public TemplateBuilder context(Context context) {
        // 合并上下文
        Map<String, Object> vars = context.getVariables();
        for (Map.Entry<String, Object> entry : vars.entrySet()) {
            contextBuilder.put(entry.getKey(), entry.getValue());
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
     * 编译模板（不渲染）
     *
     * @return 编译后的模板
     */
    public Template compile() {
        return getOrCreateEngine().compile(source);
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
        return builder.build();
    }
}
```

**Step 4: Run test to verify it passes**

Run: `mvn test -Dtest=TemplateBuilderTest -q`
Expected: PASS

**Step 5: Commit**

```bash
git add src/main/java/org/dromara/easytl/TemplateBuilder.java src/test/java/org/dromara/easytl/TemplateBuilderTest.java
git commit -m "feat: add TemplateBuilder for lazy compilation and fluent API"
```

---

## Task 5: 创建 ValidationResult 类

**Files:**
- Create: `src/main/java/org/dromara/easytl/ValidationResult.java`
- Test: `src/test/java/org/dromara/easytl/ValidationResultTest.java`

**Step 1: Write the failing test**

```java
package org.dromara.easytl;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ValidationResultTest {

    @Test
    void testValidResult() {
        ValidationResult result = ValidationResult.valid();

        assertTrue(result.isValid());
        assertNull(result.getErrorMessage());
        assertNull(result.getErrorPosition());
    }

    @Test
    void testInvalidResult() {
        ValidationResult result = ValidationResult.invalid("Syntax error", 10);

        assertFalse(result.isValid());
        assertEquals("Syntax error", result.getErrorMessage());
        assertEquals(10, result.getErrorPosition());
    }

    @Test
    void testInvalidWithoutPosition() {
        ValidationResult result = ValidationResult.invalid("Error");

        assertFalse(result.isValid());
        assertEquals("Error", result.getErrorMessage());
        assertNull(result.getErrorPosition());
    }
}
```

**Step 2: Run test to verify it fails**

Run: `mvn test -Dtest=ValidationResultTest -q`
Expected: FAIL with "Cannot resolve symbol ValidationResult"

**Step 3: Write minimal implementation**

```java
package org.dromara.easytl;

/**
 * 模板验证结果
 *
 * @author gongjun
 * @since 1.0.0
 */
public class ValidationResult {

    private final boolean valid;
    private final String errorMessage;
    private final Integer errorPosition;

    private ValidationResult(boolean valid, String errorMessage, Integer errorPosition) {
        this.valid = valid;
        this.errorMessage = errorMessage;
        this.errorPosition = errorPosition;
    }

    /**
     * 创建验证通过的结果
     *
     * @return 验证通过的结果
     */
    public static ValidationResult valid() {
        return new ValidationResult(true, null, null);
    }

    /**
     * 创建验证失败的结果
     *
     * @param errorMessage 错误信息
     * @return 验证失败的结果
     */
    public static ValidationResult invalid(String errorMessage) {
        return new ValidationResult(false, errorMessage, null);
    }

    /**
     * 创建验证失败的结果（带位置信息）
     *
     * @param errorMessage 错误信息
     * @param errorPosition 错误位置
     * @return 验证失败的结果
     */
    public static ValidationResult invalid(String errorMessage, int errorPosition) {
        return new ValidationResult(false, errorMessage, errorPosition);
    }

    /**
     * 是否验证通过
     *
     * @return 如果验证通过返回 true
     */
    public boolean isValid() {
        return valid;
    }

    /**
     * 获取错误信息
     *
     * @return 错误信息，如果验证通过则返回 null
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * 获取错误位置
     *
     * @return 错误位置（字符索引），如果没有位置信息则返回 null
     */
    public Integer getErrorPosition() {
        return errorPosition;
    }
}
```

**Step 4: Run test to verify it passes**

Run: `mvn test -Dtest=ValidationResultTest -q`
Expected: PASS

**Step 5: Commit**

```bash
git add src/main/java/org/dromara/easytl/ValidationResult.java src/test/java/org/dromara/easytl/ValidationResultTest.java
git commit -m "feat: add ValidationResult for template validation"
```

---

## Task 6: 创建 EasyTL 主类

**Files:**
- Create: `src/main/java/org/dromara/easytl/EasyTL.java`
- Test: `src/test/java/org/dromara/easytl/EasyTLTest.java`

**Step 1: Write the failing test**

```java
package org.dromara.easytl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class EasyTLTest {

    @BeforeEach
    void resetDefaultEngine() {
        // 重置默认引擎配置
        EasyTL.defaultEngine().clearCache();
    }

    // ==================== template() 测试 ====================

    @Test
    void testTemplateSimple() {
        String result = EasyTL.template("Hello, {name}!")
            .put("name", "World")
            .render();

        assertEquals("Hello, World!", result);
    }

    @Test
    void testTemplateWithConfig() {
        String result = EasyTL.template("Hello, {name}!")
            .cacheEnabled(false)
            .strictMode(true)
            .put("name", "张三")
            .render();

        assertEquals("Hello, 张三!", result);
    }

    // ==================== compile() 测试 ====================

    @Test
    void testCompileSimple() {
        Template template = EasyTL.compile("Hello, {name}!");
        assertNotNull(template);

        Context ctx = new Context();
        ctx.put("name", "World");
        assertEquals("Hello, World!", template.render(ctx));
    }

    @Test
    void testCompileWithEngine() {
        TemplateEngine engine = EasyTL.engine()
            .cacheEnabled(false)
            .build();

        Template template = EasyTL.compile(engine, "Hello, {name}!");
        assertNotNull(template);

        Context ctx = new Context();
        ctx.put("name", "World");
        assertEquals("Hello, World!", template.render(ctx));
    }

    // ==================== render() 测试 ====================

    @Test
    void testRenderStringContext() {
        Context ctx = new Context();
        ctx.put("name", "World");

        String result = EasyTL.render("Hello, {name}!", ctx);
        assertEquals("Hello, World!", result);
    }

    @Test
    void testRenderStringMap() {
        Map<String, Object> data = new HashMap<>();
        data.put("name", "World");

        String result = EasyTL.render("Hello, {name}!", data);
        assertEquals("Hello, World!", result);
    }

    @Test
    void testRenderTemplateContext() {
        Template template = EasyTL.compile("Hello, {name}!");

        Context ctx = new Context();
        ctx.put("name", "World");

        String result = EasyTL.render(template, ctx);
        assertEquals("Hello, World!", result);
    }

    @Test
    void testRenderTemplateMap() {
        Template template = EasyTL.compile("Hello, {name}!");

        Map<String, Object> data = new HashMap<>();
        data.put("name", "World");

        String result = EasyTL.render(template, data);
        assertEquals("Hello, World!", result);
    }

    // ==================== engine() 测试 ====================

    @Test
    void testEngineBuilder() {
        TemplateEngine engine = EasyTL.engine()
            .cacheEnabled(false)
            .maxCacheSize(500)
            .strictMode(true)
            .build();

        assertNotNull(engine);
        assertFalse(engine.getConfig().isCacheEnabled());
        assertEquals(500, engine.getConfig().getMaxCacheSize());
        assertTrue(engine.getConfig().isStrictMode());
    }

    // ==================== config() 测试 ====================

    @Test
    void testConfigBuilder() {
        TemplateEngine.TemplateEngineConfig config = EasyTL.config()
            .cacheEnabled(false)
            .maxCacheSize(500)
            .strictMode(true)
            .build();

        assertFalse(config.isCacheEnabled());
        assertEquals(500, config.getMaxCacheSize());
        assertTrue(config.isStrictMode());
    }

    // ==================== defaultEngine() 测试 ====================

    @Test
    void testDefaultEngine() {
        TemplateEngine engine1 = EasyTL.defaultEngine();
        TemplateEngine engine2 = EasyTL.defaultEngine();

        assertSame(engine1, engine2);
    }

    @Test
    void testDefaultEngineConfiguration() {
        // 配置默认引擎
        EasyTL.defaultEngine()
            .getConfig()
            .setCacheEnabled(false);

        // 使用默认引擎编译
        Template template = EasyTL.compile("Hello, {name}!");
        assertNotNull(template);
    }

    // ==================== context() 测试 ====================

    @Test
    void testContextBuilder() {
        Context ctx = EasyTL.context()
            .put("name", "张三")
            .put("age", 25)
            .build();

        assertEquals("张三", ctx.get("name"));
        assertEquals(25, ctx.get("age"));
    }

    // ==================== raw() 测试 ====================

    @Test
    void testRawSimple() {
        String raw = EasyTL.raw("{name}");
        assertEquals("{name}", raw);
    }

    @Test
    void testRawWithBraces() {
        String raw = EasyTL.raw("Object: {key: value}");
        assertEquals("Object: {key: value}", raw);
    }

    @Test
    void testRawWithEscape() {
        String raw = EasyTL.raw("\\{escaped\\}");
        assertEquals("\\{escaped\\}", raw);
    }

    // ==================== parse() 测试 ====================

    @Test
    void testParseSimple() {
        TemplateNode ast = EasyTL.parse("Hello, {name}!");
        assertNotNull(ast);
    }

    @Test
    void testParseComplex() {
        TemplateNode ast = EasyTL.parse("{% if x %}content{/% if %}");
        assertNotNull(ast);
    }

    // ==================== validate() 测试 ====================

    @Test
    void testValidateValid() {
        ValidationResult result = EasyTL.validate("Hello, {name}!");
        assertTrue(result.isValid());
    }

    @Test
    void testValidateInvalid() {
        // 不闭合的表达式
        ValidationResult result = EasyTL.validate("Hello, {name");
        assertFalse(result.isValid());
    }

    // ==================== 综合测试 ====================

    @Test
    void testComplexWorkflow() {
        // 创建引擎
        TemplateEngine engine = EasyTL.engine()
            .cacheEnabled(true)
            .maxCacheSize(1024)
            .build();

        // 编译模板
        Template template = EasyTL.compile(engine, "用户: {user.name}, 年龄: {user.age}");

        // 准备数据
        Map<String, Object> user = new HashMap<>();
        user.put("name", "张三");
        user.put("age", 25);

        Map<String, Object> data = new HashMap<>();
        data.put("user", user);

        // 渲染
        String result = EasyTL.render(template, data);
        assertEquals("用户: 张三, 年龄: 25", result);
    }

    @Test
    void testQuickRender() {
        Map<String, Object> data = new HashMap<>();
        data.put("name", "World");

        String result = EasyTL.render("Hello, {name}!", data);
        assertEquals("Hello, World!", result);
    }
}
```

**Step 2: Run test to verify it fails**

Run: `mvn test -Dtest=EasyTLTest -q`
Expected: FAIL with "Cannot resolve symbol EasyTL"

**Step 3: Write minimal implementation**

```java
package org.dromara.easytl;

import org.dromara.easytl.ast.TemplateNode;
import org.dromara.easytl.exception.TemplateCompileException;
import org.dromara.easytl.parser.Lexer;
import org.dromara.easytl.parser.Parser;
import org.dromara.easytl.parser.StringSource;
import org.dromara.easytl.parser.TokenStream;

import java.util.Map;

/**
 * EasyTL 工具类 - 用户友好的统一入口
 * <p>
 * 提供简洁的 API 进行模板编译、渲染和验证操作。
 * 支持链式调用风格，适合各种使用场景。
 * </p>
 *
 * <h3>使用示例：</h3>
 * <pre>{@code
 * // 快速渲染
 * String result = EasyTL.render("Hello, {name}!", map);
 *
 * // 链式调用
 * String result = EasyTL.template("Hello, {name}!")
 *     .put("name", "World")
 *     .render();
 *
 * // 编译后多次渲染
 * Template template = EasyTL.compile("Hello, {name}!");
 * String r1 = EasyTL.render(template, ctx1);
 * String r2 = EasyTL.render(template, ctx2);
 * }</pre>
 *
 * @author gongjun
 * @since 1.0.0
 */
public final class EasyTL {

    /**
     * 默认模板引擎（单例）
     */
    private static volatile TemplateEngine defaultEngine;

    /**
     * 私有构造函数，防止实例化
     */
    private EasyTL() {
    }

    // ==================== 编译/渲染 ====================

    /**
     * 创建模板构建器（延迟编译）
     * <p>
     * 支持链式配置和渲染，编译在 render() 时执行。
     * </p>
     *
     * <pre>{@code
     * String result = EasyTL.template("Hello, {name}!")
     *     .cacheEnabled(true)
     *     .put("name", "World")
     *     .render();
     * }</pre>
     *
     * @param source 模板源码
     * @return 模板构建器
     */
    public static TemplateBuilder template(String source) {
        return new TemplateBuilder(source);
    }

    /**
     * 立即编译模板（使用默认引擎）
     *
     * @param source 模板源码
     * @return 编译后的模板
     * @throws TemplateCompileException 如果编译失败
     */
    public static Template compile(String source) {
        return defaultEngine().compile(source);
    }

    /**
     * 立即编译模板（使用指定引擎）
     *
     * @param engine 模板引擎
     * @param source 模板源码
     * @return 编译后的模板
     * @throws TemplateCompileException 如果编译失败
     */
    public static Template compile(TemplateEngine engine, String source) {
        return engine.compile(source);
    }

    /**
     * 便捷渲染：编译 + 渲染（使用默认引擎）
     *
     * @param source 模板源码
     * @param context 上下文
     * @return 渲染结果
     */
    public static String render(String source, Context context) {
        return compile(source).render(context);
    }

    /**
     * 便捷渲染：编译 + 渲染（使用默认引擎）
     *
     * @param source 模板源码
     * @param data 数据 Map
     * @return 渲染结果
     */
    public static String render(String source, Map<String, Object> data) {
        Context context = new Context();
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            context.put(entry.getKey(), entry.getValue());
        }
        return compile(source).render(context);
    }

    /**
     * 便捷渲染：使用已编译模板
     *
     * @param template 编译后的模板
     * @param context 上下文
     * @return 渲染结果
     */
    public static String render(Template template, Context context) {
        return template.render(context);
    }

    /**
     * 便捷渲染：使用已编译模板
     *
     * @param template 编译后的模板
     * @param data 数据 Map
     * @return 渲染结果
     */
    public static String render(Template template, Map<String, Object> data) {
        Context context = new Context();
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            context.put(entry.getKey(), entry.getValue());
        }
        return template.render(context);
    }

    // ==================== 引擎/配置 ====================

    /**
     * 创建引擎构建器
     *
     * <pre>{@code
     * TemplateEngine engine = EasyTL.engine()
     *     .cacheEnabled(true)
     *     .maxCacheSize(1024)
     *     .build();
     * }</pre>
     *
     * @return 引擎构建器
     */
    public static EngineBuilder engine() {
        return new EngineBuilder();
    }

    /**
     * 创建配置构建器
     *
     * <pre>{@code
     * TemplateEngineConfig config = EasyTL.config()
     *     .cacheEnabled(true)
     *     .build();
     * }</pre>
     *
     * @return 配置构建器
     */
    public static ConfigBuilder config() {
        return new ConfigBuilder();
    }

    /**
     * 获取默认引擎（单例）
     * <p>
     * 默认引擎配置可通过返回的对象进行修改。
     * </p>
     *
     * @return 默认模板引擎
     */
    public static TemplateEngine defaultEngine() {
        if (defaultEngine == null) {
            synchronized (EasyTL.class) {
                if (defaultEngine == null) {
                    defaultEngine = new TemplateEngine();
                }
            }
        }
        return defaultEngine;
    }

    // ==================== 辅助方法 ====================

    /**
     * 创建上下文构建器
     *
     * <pre>{@code
     * Context ctx = EasyTL.context()
     *     .put("name", "张三")
     *     .put("age", 25)
     *     .build();
     * }</pre>
     *
     * @return 上下文构建器
     */
    public static ContextBuilder context() {
        return new ContextBuilder();
    }

    /**
     * 输出原始文本（不进行模板解析）
     * <p>
     * 返回输入文本的原始内容，用于需要输出模板语法本身的场景。
     * </p>
     *
     * @param text 原始文本
     * @return 原始文本（原样返回）
     */
    public static String raw(String text) {
        return text;
    }

    /**
     * 解析模板返回 AST
     * <p>
     * 用于高级用户需要直接操作 AST 的场景。
     * </p>
     *
     * @param source 模板源码
     * @return AST 根节点
     * @throws TemplateCompileException 如果解析失败
     */
    public static TemplateNode parse(String source) {
        StringSource stringSource = new StringSource(source);
        Lexer lexer = new Lexer(stringSource);
        TokenStream tokenStream = new TokenStream(lexer);
        Parser parser = new Parser(tokenStream);
        return parser.parse();
    }

    /**
     * 验证模板语法
     *
     * @param source 模板源码
     * @return 验证结果
     */
    public static ValidationResult validate(String source) {
        try {
            parse(source);
            return ValidationResult.valid();
        } catch (TemplateCompileException e) {
            return ValidationResult.invalid(e.getMessage());
        } catch (Exception e) {
            return ValidationResult.invalid(e.getMessage());
        }
    }
}
```

**Step 4: Run test to verify it passes**

Run: `mvn test -Dtest=EasyTLTest -q`
Expected: PASS

**Step 5: Commit**

```bash
git add src/main/java/org/dromara/easytl/EasyTL.java src/test/java/org/dromara/easytl/EasyTLTest.java
git commit -m "feat: add EasyTL as unified entry point with fluent API"
```

---

## Task 7: 运行全部测试

**Step 1: Run all tests**

Run: `mvn test -q`
Expected: All tests PASS

**Step 2: Verify no regression**

Run: `mvn test -q 2>&1 | grep -E "(Tests run:|BUILD)"`
Expected: BUILD SUCCESS

---

## Task 8: 更新 README 文档

**Files:**
- Modify: `README.md`

**Step 1: Add EasyTL usage section to README**

在 README.md 的"快速开始"部分后添加 EasyTL 工具类的使用说明：

```markdown
### 3. 使用 EasyTL 工具类（推荐）

EasyTL 提供了更简洁的 API，支持链式调用：

```java
import org.dromara.easytl.EasyTL;

// 快速渲染
Map<String, Object> data = new HashMap<>();
data.put("name", "张三");
String result = EasyTL.render("你好，{name}！", data);
// 输出：你好，张三！

// 链式调用
String result = EasyTL.template("你好，{name}！")
    .put("name", "张三")
    .render();

// 编译后多次渲染
Template template = EasyTL.compile("你好，{name}！");
Context ctx = new Context();
ctx.put("name", "张三");
String r1 = EasyTL.render(template, ctx);

// 创建自定义引擎
TemplateEngine engine = EasyTL.engine()
    .cacheEnabled(true)
    .maxCacheSize(1024)
    .strictMode(true)
    .build();

// 验证模板语法
ValidationResult result = EasyTL.validate("你好，{name}！");
if (result.isValid()) {
    // 模板语法正确
}
```
```

**Step 2: Commit**

```bash
git add README.md
git commit -m "docs: add EasyTL usage examples to README"
```

---

## Summary

完成上述任务后，EasyTL 工具类将提供以下功能：

| 方法 | 功能 |
|------|------|
| `template(source)` | 创建模板构建器，支持链式配置和延迟编译 |
| `compile(source)` | 立即编译模板 |
| `compile(engine, source)` | 使用指定引擎编译模板 |
| `render(source, ctx)` | 编译并渲染 |
| `render(source, map)` | 编译并渲染 |
| `render(template, ctx)` | 渲染已编译模板 |
| `render(template, map)` | 渲染已编译模板 |
| `engine()` | 创建引擎构建器 |
| `config()` | 创建配置构建器 |
| `defaultEngine()` | 获取默认引擎 |
| `context()` | 创建上下文构建器 |
| `raw(text)` | 输出原始文本 |
| `parse(source)` | 解析返回 AST |
| `validate(source)` | 验证模板语法 |