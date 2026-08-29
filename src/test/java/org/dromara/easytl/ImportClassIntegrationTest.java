package org.dromara.easytl;

import org.dromara.easytl.exception.TemplateRuntimeException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ImportClassIntegrationTest {

    private final TemplateEngine engine = new TemplateEngine();

    @Test
    void testImportStatementWithAlias() {
        String template = "{% import java.lang.Math as M %}{M.PI}";
        assertEquals(String.valueOf(Math.PI), engine.compile(template).render(new Context()));
    }

    @Test
    void testImportStatementWithoutAlias() {
        String template = "{% import java.lang.Math %}{Math.PI}";
        assertEquals(String.valueOf(Math.PI), engine.compile(template).render(new Context()));
    }

    @Test
    void testImportClassNotFound() {
        String template = "{% import non.existent.Class %}";
        TemplateRuntimeException ex = assertThrows(TemplateRuntimeException.class, () -> {
            engine.compile(template).render(new Context());
        });
        assertTrue(ex.getMessage().contains("class not found"));
    }

    @Test
    void testConstructorInvocationNoArgs() {
        Context ctx = new Context();
        ctx.importClass(java.util.ArrayList.class);

        String template = "{ArrayList().size()}";
        assertEquals("0", engine.compile(template).render(ctx));
    }

    @Test
    void testConstructorInvocationWithArgs() {
        Context ctx = new Context();
        ctx.importClass(String.class);

        String template = "{String('hello')}";
        assertEquals("hello", engine.compile(template).render(ctx));
    }

    @Test
    void testStaticMethodCall() {
        Context ctx = new Context();
        ctx.importClass(Math.class);

        String template = "{Math.sqrt(16)}";
        assertEquals("4.0", engine.compile(template).render(ctx));
    }

    // Create a simple test class for object construction
    public static class TestPerson {
        private String name;
        private int age;

        public TestPerson() {}

        public TestPerson(String name) {
            this.name = name;
        }

        public void setName(String name) { this.name = name; }
        public void setAge(int age) { this.age = age; }
        public String getName() { return name; }
        public int getAge() { return age; }
    }

    @Test
    void testObjectConstructionWithPropertyBlock() {
        Context ctx = new Context();
        ctx.importClass(TestPerson.class);

        String template = "{TestPerson {name: 'Peter'}.getName()}";
        assertEquals("Peter", engine.compile(template).render(ctx));
    }

    @Test
    void testObjectConstructionWithArgsAndPropertyBlock() {
        Context ctx = new Context();
        ctx.importClass(TestPerson.class);

        String template = "{TestPerson('John') {age: 25}.getAge()}";
        assertEquals("25", engine.compile(template).render(ctx));
    }

    @Test
    void testObjectConstructionWithHashMap() {
        Context ctx = new Context();
        ctx.importClass(java.util.HashMap.class);

        String template = "{HashMap {name: 'Peter', age: 18}.get('name')}";
        assertEquals("Peter", engine.compile(template).render(ctx));
    }

    // ============================================================
    // Integration tests for Engine/Builder importClass
    // ============================================================

    @Test
    void testEngineImportClassWithStaticMethod() {
        TemplateEngine engine = new EngineBuilder()
            .importClass(Math.class)
            .build();

        Template template = engine.compile("{Math.sqrt(16)}");
        assertEquals("4.0", template.render(new Context()));
    }

    @Test
    void testEngineImportClassWithConstructor() {
        TemplateEngine engine = new EngineBuilder()
            .importClass(String.class)
            .build();

        Template template = engine.compile("{String('hello')}");
        assertEquals("hello", template.render(new Context()));
    }

    @Test
    void testEngineDynamicImportClass() {
        TemplateEngine engine = new TemplateEngine();

        // 运行时动态添加
        engine.importClass(Math.class);

        Template template = engine.compile("{Math.PI}");
        assertEquals(String.valueOf(Math.PI), template.render(new Context()));
    }

    @Test
    void testTemplateBuilderImportClassWithObjectConstruction() {
        String result = new TemplateBuilder("{TestPerson {name: 'Peter'}.getName()}")
            .importClass(TestPerson.class)
            .render();
        assertEquals("Peter", result);
    }
}