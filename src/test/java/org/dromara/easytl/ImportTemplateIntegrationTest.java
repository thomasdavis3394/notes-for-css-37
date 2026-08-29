package org.dromara.easytl;

import org.dromara.easytl.loader.FileTemplateLoader;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Import Template 集成测试
 */
class ImportTemplateIntegrationTest {

    private static TemplateEngine engine;

    @BeforeAll
    static void setup() {
        FileTemplateLoader loader = new FileTemplateLoader("src/test/resources/templates/import");
        TemplateEngine.TemplateEngineConfig config = new TemplateEngine.TemplateEngineConfig();
        engine = new TemplateEngine(config, loader);
    }

    @Test
    void testBasicImportAndFunctionCall() {
        Context ctx = new Context();
        Template template = engine.compile("{% import 'utils.etl' as utils %}{utils.add(1, 2)}");
        String result = template.render(ctx);
        assertEquals("3", result);
    }

    @Test
    void testMultipleFunctionCalls() {
        Context ctx = new Context();
        Template template = engine.compile(
            "{% import 'utils.etl' as utils %}{utils.add(1, 2)} {utils.multiply(3, 4)}");
        String result = template.render(ctx);
        assertEquals("3 12", result);
    }

    @Test
    void testAccessExportedVariable() {
        Context ctx = new Context();
        Template template = engine.compile(
            "{% import 'utils.etl' as utils %}{utils.config.name}");
        String result = template.render(ctx);
        assertEquals("utils", result);
    }

    @Test
    void testExportLetSyntax() {
        TemplateEngine localEngine = new TemplateEngine();
        String templateSource = "{% export let greet = (name) -> 'Hello, ' + name %}{greet('World')}";
        Template template = localEngine.compile(templateSource);
        String result = template.render(new Context());
        assertEquals("Hello, World", result);
    }

    @Test
    void testExportExistingVariableSyntax() {
        TemplateEngine localEngine = new TemplateEngine();
        String templateSource = "{% let x = 100 %}{% export x %}{x}";
        Template template = localEngine.compile(templateSource);
        String result = template.render(new Context());
        assertEquals("100", result);
    }

    @Test
    void testDuplicateExportThrowsException() {
        TemplateEngine localEngine = new TemplateEngine();
        String templateSource = "{% let x = 1 %}{% export x %}{% export x %}";
        assertThrows(Exception.class, () -> {
            localEngine.compile(templateSource);
        });
    }

    @Test
    void testAccessNonExportedMemberThrowsException() {
        Context ctx = new Context();
        Template template = engine.compile(
            "{% import 'utils.etl' as utils %}{utils.nonExistent}");
        assertThrows(Exception.class, () -> {
            template.render(ctx);
        });
    }
}