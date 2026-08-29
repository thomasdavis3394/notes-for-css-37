package org.dromara.easytl;

import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class EngineImportClassTest {

    @Test
    void testImportClassWithSimpleName() {
        TemplateEngine engine = new TemplateEngine();
        engine.importClass(Math.class);

        Map<String, Class<?>> imports = engine.getImportedClasses();
        assertEquals(1, imports.size());
        assertEquals(Math.class, imports.get("Math"));
    }

    @Test
    void testImportClassWithAlias() {
        TemplateEngine engine = new TemplateEngine();
        engine.importClass(java.util.ArrayList.class, "List");

        Map<String, Class<?>> imports = engine.getImportedClasses();
        assertEquals(1, imports.size());
        assertEquals(java.util.ArrayList.class, imports.get("List"));
    }

    @Test
    void testImportClassChaining() {
        TemplateEngine engine = new TemplateEngine();
        assertSame(engine, engine.importClass(Math.class));
        assertSame(engine, engine.importClass(String.class, "Str"));
    }

    @Test
    void testGetImportedClassesReturnsCopy() {
        TemplateEngine engine = new TemplateEngine();
        engine.importClass(Math.class);

        Map<String, Class<?>> imports1 = engine.getImportedClasses();
        Map<String, Class<?>> imports2 = engine.getImportedClasses();

        // 返回副本，修改不影响原数据
        imports1.clear();
        assertEquals(1, imports2.size());
    }

    @Test
    void testImportClassNullClassThrowsException() {
        TemplateEngine engine = new TemplateEngine();
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> engine.importClass(null)
        );
        assertEquals("Class cannot be null", ex.getMessage());
    }

    @Test
    void testImportClassWithAliasNullClassThrowsException() {
        TemplateEngine engine = new TemplateEngine();
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> engine.importClass(null, "Alias")
        );
        assertEquals("Class cannot be null", ex.getMessage());
    }

    @Test
    void testImportClassWithAliasNullAliasThrowsException() {
        TemplateEngine engine = new TemplateEngine();
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> engine.importClass(Math.class, null)
        );
        assertEquals("Alias cannot be null or empty", ex.getMessage());
    }

    @Test
    void testImportClassWithAliasEmptyAliasThrowsException() {
        TemplateEngine engine = new TemplateEngine();
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> engine.importClass(Math.class, "")
        );
        assertEquals("Alias cannot be null or empty", ex.getMessage());
    }

    @Test
    void testImportClassOverwriteExistingAlias() {
        TemplateEngine engine = new TemplateEngine();
        engine.importClass(Math.class, "MyClass");
        assertEquals(Math.class, engine.getImportedClasses().get("MyClass"));

        // Overwrite with different class
        engine.importClass(String.class, "MyClass");
        assertEquals(String.class, engine.getImportedClasses().get("MyClass"));
        assertEquals(1, engine.getImportedClasses().size());
    }

    @Test
    void testCompileWithImportsMap() {
        TemplateEngine engine = new TemplateEngine();
        Map<String, Class<?>> imports = new HashMap<>();
        imports.put("Math", Math.class);

        Template template = engine.compile("{Math.PI}", imports);
        assertEquals(String.valueOf(Math.PI), template.render(new Context()));
    }

    @Test
    void testCompileWithImportsMapStaticMethod() {
        TemplateEngine engine = new TemplateEngine();
        Map<String, Class<?>> imports = new HashMap<>();
        imports.put("M", Math.class);

        Template template = engine.compile("{M.sqrt(16)}", imports);
        assertEquals("4.0", template.render(new Context()));
    }
}