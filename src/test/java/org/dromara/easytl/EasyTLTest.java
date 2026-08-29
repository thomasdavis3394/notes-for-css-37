package org.dromara.easytl;

import org.dromara.easytl.ast.TemplateNode;
import org.dromara.easytl.exception.ArgumentIndexOutOfBoundsException;
import org.junit.jupiter.api.BeforeEach;
import org.dromara.easytl.exception.TemplateRuntimeException;
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
        // raw() 方法在文本前添加 @raw: 标识，使其在模板渲染时保持原样输出
        String raw = EasyTL.raw("{name}");
        assertEquals("@raw:{name}", raw);
    }

    @Test
    void testRawWithBraces() {
        // raw() 方法在文本前添加 @raw: 标识
        String raw = EasyTL.raw("Object: {key: value}");
        assertEquals("@raw:Object: {key: value}", raw);
    }

    @Test
    void testRawWithEscape() {
        // raw() 方法在文本前添加 @raw: 标识
        String raw = EasyTL.raw("\\{escaped\\}");
        assertEquals("@raw:\\{escaped\\}", raw);
    }

    @Test
    void testRawInTemplate() {
        // 测试 raw() 在模板渲染中的实际效果：@raw:{name} 应该原样输出 {name}
        String result = EasyTL.template("Hello " + EasyTL.raw("{name}") + "!")
            .put("name", "World")
            .render();
        assertEquals("Hello {name}!", result);
    }

    // ==================== parse() 测试 ====================

    @Test
    void testParseSimple() {
        TemplateNode ast = EasyTL.parse("Hello, {name}!");
        assertNotNull(ast);
    }

    @Test
    void testParseComplex() {
        TemplateNode ast = EasyTL.parse("Hello, {name}! Count: {count}");
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

    // ==================== arguments() 测试 ====================

    @Test
    void testTemplateWithArguments() {
        String result = EasyTL.template("Args: {0}, {1}")
            .arguments("a", "b")
            .render();
        assertEquals("Args: a, b", result);
    }

    @Test
    void testTemplateWithArgumentsSingle() {
        String result = EasyTL.template("Hello, {0}!")
            .arguments("World")
            .render();
        assertEquals("Hello, World!", result);
    }

    @Test
    void testTemplateWithArgumentsDifferentTypes() {
        String result = EasyTL.template("Name: {0}, Age: {1}, Score: {2}")
            .arguments("Alice", 25, 98.5)
            .render();
        assertEquals("Name: Alice, Age: 25, Score: 98.5", result);
    }

    @Test
    void testContextBuilderWithArguments() {
        Context ctx = EasyTL.context()
            .arguments("arg0", "arg1", 123)
            .build();

        assertEquals("arg0", ctx.getArgument(0));
        assertEquals("arg1", ctx.getArgument(1));
        assertEquals(123, ctx.getArgument(2));
    }

    @Test
    void testTemplateWithContextArguments() {
        Context ctx = EasyTL.context()
            .arguments("Alice", 25)
            .build();

        String result = EasyTL.template("Name: {0}, Age: {1}")
            .context(ctx)
            .render();
        assertEquals("Name: Alice, Age: 25", result);
    }

    @Test
    void testArgumentsWithNamedVariables() {
        String result = EasyTL.template("Hello {name}, your ID is {0}")
            .put("name", "Bob")
            .arguments(12345)
            .render();
        assertEquals("Hello Bob, your ID is 12345", result);
    }

    @Test
    void testArgumentsOutOfBound() {
        // {2} accessing out-of-bounds argument, should throw exception now
        ArgumentIndexOutOfBoundsException ex = assertThrows(ArgumentIndexOutOfBoundsException.class, () -> {
            EasyTL.template("First: {0}, Second: {1}, Third: {2}")
                .arguments("one", "two")
                .render();
        });
        assertTrue(ex.getMessage().contains("out of bounds"));
    }

    @Test
    void testArgumentsWithDollarBrace() {
        String result = EasyTL.template("Args: ${0}, ${1}")
            .arguments("x", "y")
            .render();
        assertEquals("Args: x, y", result);
    }

    @Test
    void testArgumentsWithDoubleBrace() {
        String result = EasyTL.template("Args: {{0}}, {{1}}")
            .arguments("p", "q")
            .render();
        assertEquals("Args: p, q", result);
    }

    // ==================== eval() 测试 ====================

    @Test
    void testEval_IntegerResult() {
        Integer result = EasyTL.eval("1 + 2", Integer.class);
        assertEquals(Integer.valueOf(3), result);
    }

    @Test
    void testEval_StringResult() {
        String result = EasyTL.eval("'hello'", String.class);
        assertEquals("hello", result);
    }

    @Test
    void testEval_WithoutType() {
        Object result = EasyTL.eval("1 + 2");
        assertEquals(Integer.valueOf(3), result);
    }

    @Test
    void testEval_LetStatement() {
        Integer result = EasyTL.eval("let x = 1; let y = 2; x + y", Integer.class);
        assertEquals(Integer.valueOf(3), result);
    }

    @Test
    void testEval_ReturnStatement() {
        Integer result = EasyTL.eval("return 42", Integer.class);
        assertEquals(Integer.valueOf(42), result);
    }

    // ==================== evalAsXxx 便捷方法测试 ====================

    @Test
    void testEvalAsInteger() {
        assertEquals(Integer.valueOf(2), EasyTL.evalAsInteger("4 / 2"));
    }

    @Test
    void testEvalAsLong() {
        assertEquals(Long.valueOf(42), EasyTL.evalAsLong("42"));
    }

    @Test
    void testEvalAsFloat() {
        assertEquals(Float.valueOf(1.5f), EasyTL.evalAsFloat("1.5"));
    }

    @Test
    void testEvalAsDouble() {
        assertEquals(Double.valueOf(3.0), EasyTL.evalAsDouble("1.5 + 1.5"));
    }

    @Test
    void testEvalAsBoolean() {
        assertTrue(EasyTL.evalAsBoolean("1 < 2"));
        assertFalse(EasyTL.evalAsBoolean("1 > 2"));
    }

    @Test
    void testEvalAsString() {
        assertEquals("hello", EasyTL.evalAsString("'hello'"));
        assertEquals("3", EasyTL.evalAsString("1 + 2"));
    }

    @Test
    void testEvalAsList() {
        assertEquals(java.util.Arrays.asList(1, 2, 3), EasyTL.evalAsList("[1, 2, 3]"));
    }

    @Test
    void testEvalAsURL() {
        org.dromara.easytl.runtime.EtlURL url = EasyTL.evalAsURL("http://example.com");
        assertEquals("http", url.getScheme());
        assertEquals("example.com", url.getHost());
    }

    // ==================== eval with Context 测试 ====================

    @Test
    void testEvalWithContext_IntegerResult() {
        Context ctx = new Context();
        ctx.put("x", 10);
        Integer result = EasyTL.eval("x + 5", ctx, Integer.class);
        assertEquals(Integer.valueOf(15), result);
    }

    @Test
    void testEvalWithContext_ObjectResult() {
        Context ctx = new Context();
        ctx.put("name", "张三");
        Object result = EasyTL.eval("name", ctx);
        assertEquals("张三", result);
    }

    @Test
    void testEvalWithContext_NullContext_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            EasyTL.eval("1 + 1", (Context) null);
        });
    }

    @Test
    void testEvalAsIntegerWithContext() {
        Context ctx = new Context();
        ctx.put("count", 5);
        Integer result = EasyTL.evalAsInteger("count * 2", ctx);
        assertEquals(Integer.valueOf(10), result);
    }

    @Test
    void testEvalAsLongWithContext() {
        Context ctx = new Context();
        ctx.put("value", 100L);
        Long result = EasyTL.evalAsLong("value + 50", ctx);
        assertEquals(Long.valueOf(150L), result);
    }

    @Test
    void testEvalAsFloatWithContext() {
        Context ctx = new Context();
        ctx.put("price", 1.5f);
        Float result = EasyTL.evalAsFloat("price * 2", ctx);
        assertEquals(Float.valueOf(3.0f), result, 0.001f);
    }

    @Test
    void testEvalAsDoubleWithContext() {
        Context ctx = new Context();
        ctx.put("rate", 1.5);
        Double result = EasyTL.evalAsDouble("rate * 2", ctx);
        assertEquals(Double.valueOf(3.0), result);
    }

    @Test
    void testEvalAsBooleanWithContext() {
        Context ctx = new Context();
        ctx.put("age", 25);
        Boolean result = EasyTL.evalAsBoolean("age >= 18", ctx);
        assertTrue(result);
    }

    @Test
    void testEvalAsStringWithContext() {
        Context ctx = new Context();
        ctx.put("name", "World");
        String result = EasyTL.evalAsString("'Hello, ' + name", ctx);
        assertEquals("Hello, World", result);
    }

    @Test
    void testEvalAsListWithContext() {
        Context ctx = new Context();
        ctx.put("items", java.util.Arrays.asList(1, 2, 3));
        java.util.List<?> result = EasyTL.evalAsList("items", ctx);
        assertEquals(3, result.size());
    }

    @Test
    void testEvalAsMapWithContext() {
        Context ctx = new Context();
        Map<String, Object> data = new HashMap<>();
        data.put("key", "value");
        ctx.put("data", data);
        Map<?, ?> result = EasyTL.evalAsMap("data", ctx);
        assertEquals("value", result.get("key"));
    }

    @Test
    void testEvalAsURLWithContext() {
        Context ctx = new Context();
        ctx.put("host", "example.com");
        org.dromara.easytl.runtime.EtlURL url = EasyTL.evalAsURL("http://${host}", ctx);
        assertEquals("http://example.com", url.toString());
    }
}