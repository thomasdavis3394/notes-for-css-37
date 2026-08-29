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
            .put("greeting", c -> "Hello, " + c.get("name"))
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