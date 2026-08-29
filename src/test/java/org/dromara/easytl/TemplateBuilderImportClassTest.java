package org.dromara.easytl;

import org.dromara.easytl.exception.TemplateCompileException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class TemplateBuilderImportClassTest {

    @Test
    void testImportClassWithSimpleName() {
        TemplateBuilder builder = new TemplateBuilder("test");
        assertSame(builder, builder.importClass(Math.class));
    }

    @Test
    void testImportClassWithAlias() {
        TemplateBuilder builder = new TemplateBuilder("test");
        assertSame(builder, builder.importClass(java.util.ArrayList.class, "List"));
    }

    @Test
    void testImportClassWithNullClass() {
        TemplateBuilder builder = new TemplateBuilder("test");
        assertThrows(IllegalArgumentException.class, () -> builder.importClass(null));
    }

    @Test
    void testImportClassWithAliasNullClass() {
        TemplateBuilder builder = new TemplateBuilder("test");
        assertThrows(IllegalArgumentException.class, () -> builder.importClass(null, "List"));
    }

    @Test
    void testImportClassWithNullAlias() {
        TemplateBuilder builder = new TemplateBuilder("test");
        assertThrows(IllegalArgumentException.class, () -> builder.importClass(Math.class, null));
    }

    @Test
    void testImportClassWithEmptyAlias() {
        TemplateBuilder builder = new TemplateBuilder("test");
        assertThrows(IllegalArgumentException.class, () -> builder.importClass(Math.class, ""));
    }

    @Test
    void testCompileWithImportClass() {
        String result = new TemplateBuilder("{Math.PI}")
            .importClass(Math.class)
            .render();
        assertEquals(String.valueOf(Math.PI), result);
    }

    @Test
    void testCompileWithImportClassAlias() {
        String result = new TemplateBuilder("{M.sqrt(16)}")
            .importClass(Math.class, "M")
            .render();
        assertEquals("4.0", result);
    }

    @Test
    void testEngineImportClassUsedByTemplateBuilder() {
        TemplateEngine engine = new EngineBuilder()
            .importClass(Math.class)
            .build();

        String result = new TemplateBuilder("{Math.PI}")
            .engine(engine)
            .render();
        assertEquals(String.valueOf(Math.PI), result);
    }

    @Test
    void testTemplateBuilderMergedWithEngineImports() {
        TemplateEngine engine = new EngineBuilder()
            .importClass(Math.class)
            .build();

        String result = new TemplateBuilder("{Math.PI} {ArrayList().size()}")
            .engine(engine)
            .importClass(java.util.ArrayList.class)
            .render();
        assertEquals(String.valueOf(Math.PI) + " 0", result);
    }

    @Test
    void testAliasConflictThrowsException() {
        TemplateEngine engine = new EngineBuilder()
            .importClass(java.util.ArrayList.class, "List")
            .build();

        TemplateCompileException ex = assertThrows(TemplateCompileException.class, () -> {
            new TemplateBuilder("test")
                .engine(engine)
                .importClass(java.util.LinkedList.class, "List")
                .compile();
        });
        assertTrue(ex.getMessage().contains("alias conflict"));
    }

    @Test
    void testSameAliasSameClassAllowed() {
        TemplateEngine engine = new EngineBuilder()
            .importClass(java.util.ArrayList.class, "List")
            .build();

        // 同名同类，允许覆盖
        Template template = new TemplateBuilder("{List().size()}")
            .engine(engine)
            .importClass(java.util.ArrayList.class, "List")
            .compile();
        assertEquals("0", template.render(new Context()));
    }
}