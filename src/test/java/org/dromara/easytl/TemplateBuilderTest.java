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
            .put("computed", ctx -> {
                int a = (int) ctx.get("a");
                int b = (int) ctx.get("b");
                return a + b;
            })
            .render();

        assertEquals("Result: 30", result);
    }
}