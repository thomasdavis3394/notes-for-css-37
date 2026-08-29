package org.dromara.easytl;

import org.dromara.easytl.exception.TemplateException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for structured expressions (if/for) used as standalone
 * statements in {% %} blocks, and for let declarations inside if/for
 * expression blocks.
 * <p>
 * BNF: structured expressions may only appear on the right-hand side of a let
 * assignment or as standalone statements; block_statement contains script_content
 * (let declarations allowed).
 * </p>
 *
 * @author EasyTL Team
 */
public class StructuredExpressionStatementTest {

    private final TemplateEngine engine = new TemplateEngine();

    // ==================== if 独立语句 ====================

    @Test
    public void testIfStandaloneStatementWithoutElse() {
        Context context = new Context();
        context.put("debug", true);
        context.put("m", new HashMap<String, Object>());
        Template template = engine.compile("{% if (debug) { m.put(\"k\", \"x\") } %}{m.get(\"k\")}");
        String result = template.render(context);
        assertEquals("x", result);
    }

    @Test
    public void testIfStandaloneStatementWithoutElseNotExecuted() {
        Context context = new Context();
        context.put("debug", false);
        context.put("m", new HashMap<String, Object>());
        Template template = engine.compile("{% if (debug) { m.put(\"k\", \"x\") } %}{m.size()}");
        String result = template.render(context);
        assertEquals("0", result);
    }

    @Test
    public void testIfStandaloneStatementWithElse() {
        Context context = new Context();
        context.put("debug", false);
        context.put("m", new HashMap<String, Object>());
        Template template = engine.compile(
            "{% if (debug) { m.put(\"k\", \"T\") } else { m.put(\"k\", \"F\") } %}{m.get(\"k\")}");
        String result = template.render(context);
        assertEquals("F", result);
    }

    @Test
    public void testIfStandaloneStatementWithElseIf() {
        Context context = new Context();
        context.put("x", 2);
        Template template = engine.compile(
            "{% if (x == 1) { 'one' } else if (x == 2) { 'two' } else { 'other' } %}");
        String result = template.render(context);
        assertEquals("two", result);
    }

    @Test
    public void testIfStandaloneStatementRendersBranchValue() {
        // 与 {% 1 + 1 %} 渲染表达式值一致：分支的值会被渲染
        Template template = engine.compile("{% if (1 == 1) { 1 + 1 } %}");
        String result = template.render(new Context());
        assertEquals("2", result);
    }

    // ==================== for 独立语句 ====================

    @Test
    public void testForStandaloneStatement() {
        Context context = new Context();
        context.put("list", Arrays.asList(1, 2, 3));
        context.put("r", new ArrayList<Object>());
        Template template = engine.compile("{% for (x in list) { r.add(x) } %}{r}");
        String result = template.render(context);
        assertEquals("[1, 2, 3]", result);
    }

    @Test
    public void testForStandaloneStatementWithIndex() {
        Context context = new Context();
        context.put("list", Arrays.asList("a", "b"));
        context.put("r", new ArrayList<Object>());
        Template template = engine.compile("{% for (i, x in list) { r.add(i + \":\" + x) } %}{r}");
        String result = template.render(context);
        assertEquals("[0:a, 1:b]", result);
    }

    @Test
    public void testForStandaloneStatementInMultiStatementBlock() {
        Context context = new Context();
        context.put("list", Arrays.asList(1, 2, 3));
        context.put("r", new ArrayList<Object>());
        Template template = engine.compile(
            "{%\n for (x in list) { r.add(x) }\n let n = r.size()\n %}{n}");
        String result = template.render(context);
        assertEquals("3", result);
    }

    // ==================== if/for 表达式块内 let ====================

    @Test
    public void testLetInsideIfExpressionBlock() {
        Context context = new Context();
        context.put("s", 95);
        Template template = engine.compile(
            "{% let n = if (s >= 90) {\n let level = '优秀'\n 'A'\n } else {\n 'C'\n } %}{n}");
        String result = template.render(context);
        assertEquals("A", result);
    }

    @Test
    public void testLetInsideIfExpressionElseBranch() {
        Context context = new Context();
        context.put("s", 50);
        Template template = engine.compile(
            "{% let n = if (s >= 90) {\n let level = '优秀'\n 'A'\n } else {\n let level = '差'\n 'C'\n } %}{n}");
        String result = template.render(context);
        assertEquals("C", result);
    }

    @Test
    public void testLetInsideIfExpressionBlockInExpressionContext() {
        Template template = engine.compile("{if (1 == 1) { let z = 5\n z * 2 } else { 0 }}");
        String result = template.render(new Context());
        assertEquals("10", result);
    }

    @Test
    public void testLetInsideForExpressionBlock() {
        Context context = new Context();
        context.put("list", Arrays.asList(1, 2, 3));
        context.put("r", new ArrayList<Object>());
        Template template = engine.compile(
            "{% for (x in list) { let y = x * 10\n r.add(y) } %}{r}");
        String result = template.render(context);
        assertEquals("[10, 20, 30]", result);
    }

    @Test
    public void testForExpressionBlockLetDoesNotLeak() {
        // for 表达式循环体在子作用域中执行：块内 let 不泄漏到块外
        Context context = new Context();
        context.put("list", Arrays.asList(1, 2));
        Template template = engine.compile("{for (x in list) { let y = x * 10\n y }}{y}");
        String result = template.render(context);
        assertEquals("", result);
    }

    // ==================== 回归：let 赋值的 else 校验 ====================

    @Test
    public void testLetIfExpressionWithoutElseStillErrors() {
        TemplateException ex = assertThrows(TemplateException.class,
            () -> engine.compile("{% let x = if (true) { 1 } %}"));
        assertTrue(ex.getMessage().contains("If expression in let assignment must include else branch"),
            "let 赋值的 if 表达式无 else 应保持编译报错，实际: " + ex.getMessage());
    }

    @Test
    public void testLetIfExpressionWithElseIfButNoElseStillErrors() {
        TemplateException ex = assertThrows(TemplateException.class,
            () -> engine.compile("{% let x = if (false) { 1 } else if (true) { 2 } %}"));
        assertTrue(ex.getMessage().contains("If expression in let assignment must include else branch"),
            "let 赋值的 if 表达式仅有 else if 应保持编译报错，实际: " + ex.getMessage());
    }

    // ==================== 回归：标准语句块路由不受影响 ====================

    @Test
    public void testStandardIfStatementWithParenConditionRegression() {
        Template template = engine.compile("{% if (1 == 1) %}yes{/% if }");
        String result = template.render(new Context());
        assertEquals("yes", result);
    }

    @Test
    public void testStandardIfStatementWithBraceDelimiterRegression() {
        Template template = engine.compile("{% if (1 == 2) }a{% else }b{/% if }");
        String result = template.render(new Context());
        assertEquals("b", result);
    }

    @Test
    public void testStandardForStatementRegression() {
        Template template = engine.compile("{% for i in 1..2 %}{i}{/% for }");
        String result = template.render(new Context());
        assertEquals("12", result);
    }

    @Test
    public void testIfExpressionInExpressionContextRegression() {
        Template template = engine.compile("{if (1 == 1) {\"A\"} else {\"B\"}}");
        String result = template.render(new Context());
        assertEquals("A", result);
    }
}
