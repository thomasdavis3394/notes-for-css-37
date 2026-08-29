package org.dromara.easytl;

import org.dromara.easytl.exception.TemplateException;
import org.dromara.easytl.loader.ClasspathTemplateLoader;
import org.dromara.easytl.loader.FileTemplateLoader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for multi-statement self-closing statement blocks ({% ... %}).
 * <p>
 * BNF: script_content ::= script_statement ( ( SEMICOLON | NEWLINE ) script_statement )*
 * Statements may be separated by semicolons or newlines.
 * </p>
 *
 * @author EasyTL Team
 */
public class MultiStatementBlockTest {

    private final TemplateEngine engine = new TemplateEngine();

    // ==================== 分号分隔的多语句块 ====================

    @Test
    public void testSemicolonSeparatedMultiStatement() {
        Template template = engine.compile("{% let a = 1; let b = 2; let c = a + b %}{c}");
        String result = template.render(new Context());
        assertEquals("3", result);
    }

    @Test
    public void testSemicolonSeparatedTwoStatements() {
        Template template = engine.compile("{% let a = 1; let b = 2 %}{a}{b}");
        String result = template.render(new Context());
        assertEquals("12", result);
    }

    // ==================== 换行分隔的多语句块 ====================

    @Test
    public void testNewlineSeparatedMultiStatement() {
        Template template = engine.compile("{%\n let a = 1\n let b = 2\n %}{a}{b}");
        String result = template.render(new Context());
        assertEquals("12", result);
    }

    @Test
    public void testNewlineSeparatedWithComputation() {
        Template template = engine.compile("{%\n let a = 1\n let b = 2\n let c = a + b\n %}{c}");
        String result = template.render(new Context());
        assertEquals("3", result);
    }

    // ==================== 混合分隔与末尾分号 ====================

    @Test
    public void testMixedSemicolonAndNewlineSeparators() {
        Template template = engine.compile("{% let a = 1;\n let b = 2; let c = 3\n %}{a}{b}{c}");
        String result = template.render(new Context());
        assertEquals("123", result);
    }

    @Test
    public void testTrailingSemicolon() {
        Template template = engine.compile("{% let a = 1; %}{a}");
        String result = template.render(new Context());
        assertEquals("1", result);
    }

    // ==================== 表达式语句 ====================

    @Test
    public void testExpressionStatementAmongLets() {
        // 表达式语句渲染其值（与 {% 1 + 1 %} 的单语句行为一致）
        Template template = engine.compile("{% let a = 1\n a + 1 %}");
        String result = template.render(new Context());
        assertEquals("2", result);
    }

    // ==================== 单语句块回归 ====================

    @Test
    public void testSingleLetStatementRegression() {
        Template template = engine.compile("{% let x = 42 %}{x}");
        String result = template.render(new Context());
        assertEquals("42", result);
    }

    @Test
    public void testSingleExpressionStatementRegression() {
        Template template = engine.compile("{% 1 + 1 %}");
        String result = template.render(new Context());
        assertEquals("2", result);
    }

    @Test
    public void testSingleExpressionWithSemicolonRegression() {
        Template template = engine.compile("{% 1 + 1; %}");
        String result = template.render(new Context());
        assertEquals("2", result);
    }

    // ==================== 空块与错误回归 ====================

    @Test
    public void testEmptyBlockStillErrors() {
        TemplateException ex = assertThrows(TemplateException.class,
            () -> engine.compile("{% %}"));
        assertTrue(ex.getMessage().contains("Unexpected token: %}"),
            "空语句块应保持编译报错，实际: " + ex.getMessage());
    }

    @Test
    public void testLetWithoutValueStillErrors() {
        TemplateException ex = assertThrows(TemplateException.class,
            () -> engine.compile("{% let x = %}"));
        assertTrue(ex.getMessage().contains("Unexpected token: %}"),
            "let 缺少值应保持编译报错，实际: " + ex.getMessage());
    }

    // ==================== 多语句块内混合 import ====================

    @Test
    public void testImportThenLetNewlineSeparated() {
        Template template = engine.compile("{% import java.lang.Math\n let x = 1 %}{x}");
        String result = template.render(new Context());
        assertEquals("1", result);
    }

    @Test
    public void testLetThenImportNewlineSeparated() {
        Template template = engine.compile("{% let x = 1\n import java.lang.Math %}{x}{Math.abs(-1)}");
        String result = template.render(new Context());
        assertEquals("11", result);
    }

    @Test
    public void testTwoImportsInOneBlock() {
        Template template = engine.compile(
            "{% import java.lang.Math\n import java.util.Objects %}{Math.abs(-2)}");
        String result = template.render(new Context());
        assertEquals("2", result);
    }

    @Test
    public void testImportSemicolonLet() {
        Template template = engine.compile("{% import java.lang.Math; let x = 1 %}{x}{Math.abs(-1)}");
        String result = template.render(new Context());
        assertEquals("11", result);
    }

    @Test
    public void testImportWithAliasThenLet() {
        Template template = engine.compile("{% import java.lang.Math as M; let x = M.abs(-3) %}{x}");
        String result = template.render(new Context());
        assertEquals("3", result);
    }

    @Test
    public void testImportTemplateThenLet() {
        FileTemplateLoader loader = new FileTemplateLoader("src/test/resources/templates/import");
        TemplateEngine engineWithLoader = new TemplateEngine(new TemplateEngine.TemplateEngineConfig(), loader);
        Template template = engineWithLoader.compile(
            "{% import 'utils.etl' as utils\n let x = 1 %}{x}{utils.add(1, 2)}");
        String result = template.render(new Context());
        assertEquals("13", result);
    }

    // ==================== 多语句块内混合 extends ====================

    @Test
    public void testExtendsThenLet() {
        ClasspathTemplateLoader loader = new ClasspathTemplateLoader();
        TemplateEngine engineWithLoader = new TemplateEngine(new TemplateEngine.TemplateEngineConfig(), loader);
        Template template = engineWithLoader.compile("{% extends 'templates/base.etl'\n let x = 1 %}{x}");
        String result = template.render(new Context());
        assertTrue(result.contains("BaseContent"), "应包含外部模板内容，实际: " + result);
        assertTrue(result.contains("1"), "应包含 let 变量渲染结果，实际: " + result);
    }

    // ==================== 多语句块内混合 export ====================

    @Test
    public void testExportLetThenLet() {
        Template template = engine.compile("{% export let a = 1\n let b = 2 %}{a}{b}");
        String result = template.render(new Context());
        assertEquals("12", result);
    }

    @Test
    public void testExportVariableNameThenLet() {
        Template template = engine.compile("{% let a = 1\n export a\n let b = 2 %}{a}{b}");
        String result = template.render(new Context());
        assertEquals("12", result);
    }

    @Test
    public void testDuplicateExportInMultiStatementBlockStillErrors() {
        // 编译期 ExportValidator 校验行为不变：多语句块内重复 export 仍报错
        TemplateException ex = assertThrows(TemplateException.class,
            () -> engine.compile("{% export let a = 1\n export let a = 2 %}"));
        assertTrue(ex.getMessage().contains("already exported"),
            "多语句块内重复 export 应保持编译报错，实际: " + ex.getMessage());
    }

    // ==================== 单语句 import/extends/export 回归 ====================

    @Test
    public void testSingleImportRegression() {
        Template template = engine.compile("{% import java.lang.Math %}{Math.abs(-1)}");
        String result = template.render(new Context());
        assertEquals("1", result);
    }

    @Test
    public void testSingleExportLetRegression() {
        Template template = engine.compile("{% export let a = 1 %}{a}");
        String result = template.render(new Context());
        assertEquals("1", result);
    }

    @Test
    public void testSingleExtendsRegression() {
        ClasspathTemplateLoader loader = new ClasspathTemplateLoader();
        TemplateEngine engineWithLoader = new TemplateEngine(new TemplateEngine.TemplateEngineConfig(), loader);
        Template template = engineWithLoader.compile("{% extends 'templates/base.etl' %}Suffix");
        String result = template.render(new Context());
        assertTrue(result.contains("BaseContent"), "应包含外部模板内容，实际: " + result);
        assertTrue(result.contains("Suffix"), "应包含当前模板内容，实际: " + result);
    }
}
