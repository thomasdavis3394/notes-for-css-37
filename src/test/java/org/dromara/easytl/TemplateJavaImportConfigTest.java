package org.dromara.easytl;

import org.dromara.easytl.exception.TemplateCompileException;
import org.dromara.easytl.exception.TemplateRuntimeException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TemplateJavaImportConfigTest {

    @Test
    void extendsChildWithImportFailsWhenDisabled(@TempDir Path dir) throws Exception {
        Files.write(dir.resolve("base.etl"), "{% extends \"sub.etl\" %}".getBytes(StandardCharsets.UTF_8));
        Files.write(dir.resolve("sub.etl"), "{% import java.lang.Math %}".getBytes(StandardCharsets.UTF_8));

        TemplateEngine.TemplateEngineConfig cfg = new TemplateEngine.TemplateEngineConfig()
                .setTemplateJavaClassImportEnabled(false)
                .setTemplateBasePath(dir.toString());
        TemplateEngine engine = new TemplateEngine(cfg);
        Template t = engine.compile("{% extends \"base.etl\" %}");

        TemplateRuntimeException ex = assertThrows(TemplateRuntimeException.class, () -> t.render(new Context()));
        // visitExtendsStatement may re-wrap without preserving cause when extends has a position
        assertTrue(ex.getMessage().contains("Template Java class import is disabled"));
    }

    @Test
    void importClassWorksWhenTemplateImportDisabled() {
        TemplateEngine engine = new TemplateEngine(
                new TemplateEngine.TemplateEngineConfig().setTemplateJavaClassImportEnabled(false));
        Template t = engine.compile("{Math.abs(-1)}");
        Context ctx = new Context();
        ctx.importClass(Math.class);
        assertEquals("1", t.render(ctx).trim());
    }

    @Test
    void templateBuilderImplicitEngineDisallowsImport() {
        assertThrows(TemplateCompileException.class,
                () -> new TemplateBuilder("{% import java.lang.Math %}")
                        .templateJavaClassImportEnabled(false)
                        .compile());
    }
}
