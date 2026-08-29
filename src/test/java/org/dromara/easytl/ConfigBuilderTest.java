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
    void testTemplateJavaClassImportEnabled() {
        TemplateEngine.TemplateEngineConfig config = new ConfigBuilder()
            .templateJavaClassImportEnabled(false)
            .build();
        assertFalse(config.isTemplateJavaClassImportEnabled());
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