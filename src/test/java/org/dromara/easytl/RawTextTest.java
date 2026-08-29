package org.dromara.easytl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RawTextTest - 原始文本功能测试类
 * <p>
 * 测试三种原始文本语法：
 * <ul>
 *   <li>\{ \} 转义括号语法</li>
 *   <li>@raw{...} 块语法</li>
 *   <li>@raw: 冒号语法</li>
 * </ul>
 * </p>
 *
 * @author gongjun
 * @since 1.0.0
 */
class RawTextTest {

    private TemplateEngine engine;

    @BeforeEach
    void setUp() {
        engine = new TemplateEngine();
    }

    // ==================== 转义括号语法测试 ====================

    @Test
    void testEscapeLeftBrace() {
        // 测试 \{ 输出 {
        final String templateStr = "Hello \\{name\\}!";
        final Template template = engine.compile(templateStr);

        final Context context = new Context();
        context.put("name", "World");
        final String result = template.render(context);
        assertEquals("Hello {name}!", result);
    }

    @Test
    void testEscapeRightBrace() {
        // 测试 \} 输出 }
        final String templateStr = "Object: \\{key: value\\}";
        final Template template = engine.compile(templateStr);

        final Context context = new Context();
        final String result = template.render(context);
        assertEquals("Object: {key: value}", result);
    }

    @Test
    void testEscapeBracesMixed() {
        // 测试混合使用转义括号和表达式
        final String templateStr = "Raw: \\{raw\\}, Expr: {name}";
        final Template template = engine.compile(templateStr);

        final Context context = new Context();
        context.put("name", "World");
        final String result = template.render(context);
        assertEquals("Raw: {raw}, Expr: World", result);
    }

    @Test
    void testEscapeBracesOnlyLeft() {
        // 测试只转义左括号
        final String templateStr = "Only left: \\{test}";
        final Template template = engine.compile(templateStr);

        final Context context = new Context();
        final String result = template.render(context);
        assertEquals("Only left: {test}", result);
    }

    @Test
    void testEscapeBracesOnlyRight() {
        // 测试在文本模式下单独转义右括号（实际上不需要转义，因为 } 在文本模式没有特殊含义）
        // 但测试 \} 转义语法仍然正常工作
        final String templateStr = "Only right: test\\}";
        final Template template = engine.compile(templateStr);

        final Context context = new Context();
        final String result = template.render(context);
        assertEquals("Only right: test}", result);
    }

    @Test
    void testBackslashNotEscape() {
        // 测试反斜杠不转义其他字符
        final String templateStr = "Path: C:\\Users\\name";
        final Template template = engine.compile(templateStr);

        final Context context = new Context();
        final String result = template.render(context);
        assertEquals("Path: C:\\Users\\name", result);
    }

    // ==================== @raw{...} 块语法测试 ====================

    @Test
    void testRawBlockSimple() {
        // 测试简单的 @raw{...} 块
        final String templateStr = "Hello @raw{{name}}!";
        final Template template = engine.compile(templateStr);

        final Context context = new Context();
        context.put("name", "World");
        final String result = template.render(context);
        assertEquals("Hello {name}!", result);
    }

    @Test
    void testRawBlockWithExpression() {
        // 测试 @raw{...} 块内包含表达式语法
        final String templateStr = "@raw{RawText: {xxx}}";
        final Template template = engine.compile(templateStr);

        final Context context = new Context();
        context.put("xxx", "value");
        final String result = template.render(context);
        assertEquals("RawText: {xxx}", result);
    }

    @Test
    void testRawBlockNestedBraces() {
        // 测试 @raw{...} 块内的嵌套括号
        final String templateStr = "@raw{a{b{}c}d}";
        final Template template = engine.compile(templateStr);

        final Context context = new Context();
        final String result = template.render(context);
        assertEquals("a{b{}c}d", result);
    }

    @Test
    void testRawBlockMultipleNestedBraces() {
        // 测试多层嵌套括号
        final String templateStr = "@raw{outer{inner1{inner2}inner3}outer}";
        final Template template = engine.compile(templateStr);

        final Context context = new Context();
        final String result = template.render(context);
        assertEquals("outer{inner1{inner2}inner3}outer", result);
    }

    @Test
    void testRawBlockMixedWithExpression() {
        // 测试 @raw{...} 块与表达式混合使用
        final String templateStr = "Name: {name}, Raw: @raw{{expr}}, End";
        final Template template = engine.compile(templateStr);

        final Context context = new Context();
        context.put("name", "Test");
        context.put("expr", "ignored");
        final String result = template.render(context);
        assertEquals("Name: Test, Raw: {expr}, End", result);
    }

    @Test
    void testRawBlockWithStatementSyntax() {
        // 测试 @raw{...} 块内包含语句块语法
        final String templateStr = "@raw{{% if x %}content{/% if %}}";
        final Template template = engine.compile(templateStr);

        final Context context = new Context();
        context.put("x", true);
        final String result = template.render(context);
        assertEquals("{% if x %}content{/% if %}", result);
    }

    @Test
    void testRawBlockEmpty() {
        // 测试空的 @raw{...} 块
        final String templateStr = "Start@raw{}End";
        final Template template = engine.compile(templateStr);

        final Context context = new Context();
        final String result = template.render(context);
        assertEquals("StartEnd", result);
    }

    @Test
    void testRawBlockWithDollarBrace() {
        // 测试 @raw{...} 块内包含 ${} 语法
        final String templateStr = "@raw{Price: ${price}}";
        final Template template = engine.compile(templateStr);

        final Context context = new Context();
        context.put("price", 100);
        final String result = template.render(context);
        assertEquals("Price: ${price}", result);
    }

    // ==================== @raw: 冒号语法测试 ====================

    @Test
    void testRawColonSimple() {
        // 测试简单的 @raw: 语法
        final String templateStr = "@raw: Everything after is raw {name} ${expr}";
        final Template template = engine.compile(templateStr);

        final Context context = new Context();
        context.put("name", "World");
        context.put("expr", "value");
        final String result = template.render(context);
        assertEquals(" Everything after is raw {name} ${expr}", result);
    }

    @Test
    void testRawColonWithExpression() {
        // 测试 @raw: 语法前的表达式
        final String templateStr = "Hello {name}! @raw: Now raw: {xxx}";
        final Template template = engine.compile(templateStr);

        final Context context = new Context();
        context.put("name", "World");
        context.put("xxx", "ignored");
        final String result = template.render(context);
        assertEquals("Hello World!  Now raw: {xxx}", result);
    }

    @Test
    void testRawColonWithStatementSyntax() {
        // 测试 @raw: 后包含语句块语法
        final String templateStr = "@raw: {% if x %} {name} {/% if %}";
        final Template template = engine.compile(templateStr);

        final Context context = new Context();
        context.put("x", true);
        context.put("name", "Test");
        final String result = template.render(context);
        assertEquals(" {% if x %} {name} {/% if %}", result);
    }

    @Test
    void testRawColonAtEnd() {
        // 测试 @raw: 在模板末尾
        final String templateStr = "Start @raw: raw content";
        final Template template = engine.compile(templateStr);

        final Context context = new Context();
        final String result = template.render(context);
        assertEquals("Start  raw content", result);
    }

    @Test
    void testRawColonOnly() {
        // 测试整个模板只有 @raw:
        final String templateStr = "@raw:{name} ${expr} {% if %}";
        final Template template = engine.compile(templateStr);

        final Context context = new Context();
        context.put("name", "Test");
        final String result = template.render(context);
        assertEquals("{name} ${expr} {% if %}", result);
    }

    // ==================== 混合测试 ====================

    @Test
    void testMixedAllRawTextSyntaxes() {
        // 测试混合使用所有原始文本语法
        final String templateStr = "Escaped: \\{x\\}, Block: @raw{{y}}, Colon: @raw: {z}";
        final Template template = engine.compile(templateStr);

        final Context context = new Context();
        context.put("x", "X");
        context.put("y", "Y");
        context.put("z", "Z");
        final String result = template.render(context);
        assertEquals("Escaped: {x}, Block: {y}, Colon:  {z}", result);
    }

    @Test
    void testEscapeInRawBlock() {
        // 测试 @raw{...} 块内的转义字符
        final String templateStr = "@raw{Path: C:\\Users\\{name}}";
        final Template template = engine.compile(templateStr);

        final Context context = new Context();
        context.put("name", "test");
        final String result = template.render(context);
        assertEquals("Path: C:\\Users\\{name}", result);
    }

    // ==================== Context.put(String, TemplateVariable) 测试 ====================

    @Test
    void testContextPutTemplateVariable() {
        // 测试 Context.put(String, TemplateVariable) 重载方法
        final Context context = new Context();

        // 使用 Lambda 创建 TemplateVariable
        context.put("dynamic", ctx -> "Hello, " + ctx.get("name"));
        context.put("name", "World");

        assertEquals("Hello, World", context.get("dynamic"));
    }

    @Test
    void testContextPutTemplateVariableNull() {
        // 测试 Context.put(String, TemplateVariable) 传入 null
        final Context context = new Context();

        context.put("nullVar", (TemplateVariable) null);

        assertNull(context.get("nullVar"));
        assertTrue(context.contains("nullVar"));
    }

    @Test
    void testContextPutTemplateVariableInTemplate() {
        // 测试在模板中使用 TemplateVariable
        final String templateStr = "Result: {dynamic}";
        final Template template = engine.compile(templateStr);

        final Context context = new Context();
        context.put("dynamic", ctx -> {
            int a = (int) ctx.get("a");
            int b = (int) ctx.get("b");
            return a + b;
        });
        context.put("a", 10);
        context.put("b", 20);

        final String result = template.render(context);
        assertEquals("Result: 30", result);
    }
}