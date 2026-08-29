package org.dromara.easytl;

import org.junit.jupiter.api.Test;
import java.nio.charset.StandardCharsets;
import java.util.Map;
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

    @Test
    void testImportClassWithSimpleName() {
        TemplateEngine engine = new EngineBuilder()
            .importClass(Math.class)
            .build();

        Map<String, Class<?>> imports = engine.getImportedClasses();
        assertEquals(1, imports.size());
        assertEquals(Math.class, imports.get("Math"));
    }

    @Test
    void testImportClassWithAlias() {
        TemplateEngine engine = new EngineBuilder()
            .importClass(java.util.ArrayList.class, "List")
            .build();

        Map<String, Class<?>> imports = engine.getImportedClasses();
        assertEquals(java.util.ArrayList.class, imports.get("List"));
    }

    @Test
    void testMultipleImportClasses() {
        TemplateEngine engine = new EngineBuilder()
            .importClass(Math.class)
            .importClass(String.class, "Str")
            .importClass(java.util.Date.class)
            .build();

        Map<String, Class<?>> imports = engine.getImportedClasses();
        assertEquals(3, imports.size());
        assertEquals(Math.class, imports.get("Math"));
        assertEquals(String.class, imports.get("Str"));
        assertEquals(java.util.Date.class, imports.get("Date"));
    }

    @Test
    void testImportClassChaining() {
        EngineBuilder builder = new EngineBuilder();
        assertSame(builder, builder.importClass(Math.class));
        assertSame(builder, builder.importClass(String.class, "Str"));
    }

    @Test
    void testImportClassNullClassThrowsException() {
        EngineBuilder builder = new EngineBuilder();
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> builder.importClass(null)
        );
        assertEquals("Class cannot be null", ex.getMessage());
    }

    @Test
    void testImportClassWithAliasNullClassThrowsException() {
        EngineBuilder builder = new EngineBuilder();
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> builder.importClass(null, "Alias")
        );
        assertEquals("Class cannot be null", ex.getMessage());
    }

    @Test
    void testImportClassWithAliasNullAliasThrowsException() {
        EngineBuilder builder = new EngineBuilder();
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> builder.importClass(Math.class, null)
        );
        assertEquals("Alias cannot be null or empty", ex.getMessage());
    }

    @Test
    void testImportClassWithAliasEmptyAliasThrowsException() {
        EngineBuilder builder = new EngineBuilder();
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> builder.importClass(Math.class, "")
        );
        assertEquals("Alias cannot be null or empty", ex.getMessage());
    }
}