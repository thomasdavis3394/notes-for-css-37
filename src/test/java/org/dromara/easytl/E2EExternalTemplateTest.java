package org.dromara.easytl;

import org.dromara.easytl.exception.AssertionException;
import org.dromara.easytl.loader.FileTemplateLoader;
import org.dromara.easytl.runtime.TemplateFunction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 外部模板 E2E 测试
 * <p>
 * 所有测试用例均编写在外部 .etl 模板文件中（src/test/resources/templates/e2e/），
 * 模板内部通过内置 {@code assert(...)} 函数逐项断言语法/功能特性；
 * 本类负责加载外部模板、准备上下文数据并验证渲染结果：
 * <ul>
 *   <li>渲染不抛出异常 —— 证明模板内全部 assert 断言通过</li>
 *   <li>渲染输出包含各特性分组的 OK 标记与预期文本</li>
 *   <li>失败场景（断言失败/严格模式）在 Java 侧验证抛出的异常</li>
 * </ul>
 * </p>
 *
 * @author EasyTL Team
 */
@DisplayName("E2E External Template Tests")
class E2EExternalTemplateTest {

    private static final String E2E = "templates/e2e/";

    /** 测试 Bean：验证对象构造表达式、属性访问与方法调用 */
    public static class TestPerson {
        private String name;
        private int age;

        public TestPerson() {
        }

        public TestPerson(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }
    }

    // ==================== 工具方法 ====================

    private File getResourceFile(String path) {
        URL resource = getClass().getClassLoader().getResource(path);
        assertNotNull(resource, "测试资源不存在: " + path);
        try {
            return new File(resource.toURI());
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    /** 默认引擎编译外部模板并渲染 */
    private String render(String resourcePath, Context context) {
        Template template = new TemplateEngine().compile(getResourceFile(resourcePath));
        assertNotNull(template);
        return template.render(context);
    }

    private String render(String resourcePath) {
        return render(resourcePath, new Context());
    }

    /** 断言渲染结果包含全部分组 OK 标记与最终 PASS 标记 */
    private void assertPassMarkers(String result, String passMarker, String... okMarkers) {
        for (String marker : okMarkers) {
            assertTrue(result.contains(marker),
                "渲染结果应包含标记 [" + marker + "]，实际输出:\n" + result);
        }
        assertTrue(result.contains(passMarker),
            "渲染结果应包含最终标记 [" + passMarker + "]，实际输出:\n" + result);
    }

    // ==================== 字面量 ====================

    @Nested
    @DisplayName("literals.etl - 字面量")
    class LiteralsTest {

        @Test
        @DisplayName("字符串/模板字符串/数字/布尔/null/列表/Map/区间/正则字面量")
        void testLiterals() {
            String result = render(E2E + "literals.etl");
            assertPassMarkers(result, "LITERALS ALL PASS",
                "strings OK", "escapes OK", "template-string OK", "numbers OK", "boolean-null OK",
                "list OK", "map OK", "range OK", "regex OK");
            assertTrue(result.contains("string-render: hello"), "实际输出:\n" + result);
            assertTrue(result.contains("list-render: [1, 2, 3]"), "实际输出:\n" + result);
        }
    }

    // ==================== 表达式 ====================

    @Nested
    @DisplayName("expressions.etl - 表达式与访问")
    class ExpressionsTest {

        @Test
        @DisplayName("三种包裹语法/属性/方法链/下标/安全导航/动态键")
        void testExpressions() {
            TemplateEngine.TemplateEngineConfig config = new TemplateEngine.TemplateEngineConfig()
                .setBraceSyntax(TemplateEngine.TemplateEngineConfig.BraceSyntax.BOTH);
            TemplateEngine engine = new TemplateEngine(config);

            TestPerson user = new TestPerson("John");
            user.setAge(30);
            Map<String, Object> data = new HashMap<>();
            data.put("name", "John");
            data.put("field1", "value1");
            Map<String, Object> first = new HashMap<>();
            first.put("name", "John");
            List<Map<String, Object>> users = new ArrayList<>();
            users.add(first);

            Context context = new Context();
            context.put("user", user);
            context.put("items", Arrays.asList("a", "b", "c"));
            context.put("data", data);
            context.put("key", "field1");
            context.put("users", users);
            context.put("nullUser", null);

            String result = engine.compile(getResourceFile(E2E + "expressions.etl")).render(context);
            assertPassMarkers(result, "EXPRESSIONS ALL PASS",
                "wrappers OK", "method-call OK", "index-access OK", "safe-nav OK", "dynamic-key OK",
                "nested-chain OK", "parens OK", "literal-render OK");
            assertTrue(result.contains("wrappers: John | John | John"), "实际输出:\n" + result);
            assertTrue(result.contains("mixed: 31/a"), "实际输出:\n" + result);
            assertTrue(result.contains("bool: true"), "实际输出:\n" + result);
            assertTrue(result.contains("nothing: []"), "实际输出:\n" + result);
        }
    }

    // ==================== 位置参数 ====================

    @Nested
    @DisplayName("positional-args.etl - 位置参数")
    class PositionalArgsTest {

        @Test
        @DisplayName("{0}/{$0}/{$} 参数访问")
        void testPositionalArgs() {
            Context context = new Context();
            context.setArguments("A", "B", "C");
            String result = render(E2E + "positional-args.etl", context);
            assertPassMarkers(result, "POSITIONAL-ARGS ALL PASS",
                "basic OK", "lambda-arg OK", "override OK");
            assertTrue(result.contains("simple: A / B / C"), "实际输出:\n" + result);
            assertTrue(result.contains("vars: A / B / C"), "实际输出:\n" + result);
            assertTrue(result.contains("default: A"), "实际输出:\n" + result);
            assertTrue(result.contains("lambda-arg: x"), "实际输出:\n" + result);
            assertTrue(result.contains("override: own"), "实际输出:\n" + result);
        }

        @Test
        @DisplayName("{$0 ?? 'fallback'} 处理 null 参数")
        void testPositionalArgsNull() {
            Context context = new Context();
            context.setArguments((Object) null);
            String result = render(E2E + "positional-args-null.etl", context);
            assertPassMarkers(result, "POSITIONAL-ARGS-NULL ALL PASS");
            assertTrue(result.contains("fallback: fallback"), "实际输出:\n" + result);
        }
    }

    // ==================== 运算符 ====================

    @Nested
    @DisplayName("operators.etl - 运算符")
    class OperatorsTest {

        @Test
        @DisplayName("算术/一元/关系/相等/逻辑/Elvis/三元/正则/包含/集合/复合赋值/自增自减/类型转换")
        void testOperators() {
            String result = render(E2E + "operators.etl");
            assertPassMarkers(result, "OPERATORS ALL PASS",
                "arithmetic OK", "unary OK", "relational OK", "equality OK", "logical OK",
                "elvis OK", "ternary OK", "regex-ops OK", "membership OK", "set-ops OK",
                "compound-assign OK", "inc-dec OK", "cast OK");
        }
    }

    // ==================== 控制流语句块 ====================

    @Nested
    @DisplayName("control-flow.etl - 控制流语句块")
    class ControlFlowTest {

        @Test
        @DisplayName("if/else-if/else、嵌套 if、switch、for(列表/索引/区间/Map/嵌套/经典)、let 多语句")
        void testControlFlow() {
            String result = render(E2E + "control-flow.etl");
            assertPassMarkers(result, "CONTROL-FLOW ALL PASS",
                "if-elseif OK", "nested-if OK", "truthy OK", "switch OK", "switch-string OK",
                "switch-no-default OK", "switch-nested OK",
                "for-list OK", "for-empty OK", "for-index OK", "for-range OK", "for-map OK",
                "for-nested OK", "for-classic OK", "let-multi OK");
            assertTrue(result.contains("items: a b c"), "实际输出:\n" + result);
        }
    }

    // ==================== 结构化表达式 ====================

    @Nested
    @DisplayName("structured-expressions.etl - 结构化表达式")
    class StructuredExpressionsTest {

        @Test
        @DisplayName("if 表达式/for 表达式/Lambda(各种形态/闭包/高阶)")
        void testStructuredExpressions() {
            String result = render(E2E + "structured-expressions.etl");
            assertPassMarkers(result, "STRUCTURED ALL PASS",
                "if-expr OK", "block-value OK", "inline-if-expr OK", "standalone-if-expr OK",
                "for-expr OK", "for-expr-index OK", "for-expr-range OK", "lambda OK");
            assertTrue(result.contains("inline: big"), "实际输出:\n" + result);
        }
    }

    // ==================== Java 互操作 ====================

    @Nested
    @DisplayName("java-interop.etl - Java 类导入与对象构造")
    class JavaInteropTest {

        @Test
        @DisplayName("{% import %}/静态访问/对象构造/构造+属性块")
        void testJavaInterop() {
            Context context = new Context();
            context.importClass(TestPerson.class);
            context.importClass(TestPerson.class, "Person");
            String result = render(E2E + "java-interop.etl", context);
            assertPassMarkers(result, "JAVA-INTEROP ALL PASS",
                "construct OK", "static-alias OK", "map-construct OK",
                "bean-construct OK", "bean-ctor-args OK", "bean-alias OK");
            assertTrue(result.contains("person: Peter(26)"), "实际输出:\n" + result);
        }
    }

    // ==================== JSONPath ====================

    @Nested
    @DisplayName("jsonpath.etl - JSONPath 查询")
    class JsonPathTest {

        @Test
        @DisplayName("下标/负数下标/通配/切片/过滤/递归下降/Union/内置函数组合")
        void testJsonPath() {
            String result = render(E2E + "jsonpath.etl");
            assertPassMarkers(result, "JSONPATH ALL PASS",
                "data OK", "index OK", "wildcard OK", "slice OK", "filter OK",
                "recursive OK", "union OK", "functions-in-path OK");
            assertTrue(result.contains(
                "titles: [Sayings of the Century, Sword of Honour, Moby Dick, The Lord of the Rings]"),
                "实际输出:\n" + result);
        }
    }

    // ==================== 内置函数与自定义函数 ====================

    @Nested
    @DisplayName("functions.etl - 内置/自定义函数")
    class FunctionsTest {

        @Test
        @DisplayName("length/count/match/search/value/自定义函数/内置函数遮蔽")
        void testFunctions() {
            Context context = new Context();
            context.registerFunction(new TemplateFunction() {
                @Override
                public String getName() {
                    return "double";
                }

                @Override
                public Object call(List<Object> args) {
                    return ((Number) args.get(0)).doubleValue() * 2;
                }
            });
            context.registerFunction(new TemplateFunction() {
                @Override
                public String getName() {
                    return "concat";
                }

                @Override
                public Object call(List<Object> args) {
                    return String.valueOf(args.get(0)) + args.get(1);
                }
            });
            String result = render(E2E + "functions.etl", context);
            assertPassMarkers(result, "FUNCTIONS ALL PASS",
                "length OK", "count OK", "match OK", "search OK", "value OK",
                "custom-function OK", "shadowing OK");
        }
    }

    // ==================== assert 函数 ====================

    @Nested
    @DisplayName("assert-forms.etl - 内置 assert 函数")
    class AssertFormsTest {

        @Test
        @DisplayName("条件形式/消息形式/lambda 处理器形式/同名遮蔽")
        void testAssertForms() {
            Context context = new Context();
            List<Object> log = new ArrayList<>();
            context.put("log", log);

            String result = render(E2E + "assert-forms.etl", context);
            assertPassMarkers(result, "ASSERT-FORMS ALL PASS",
                "cond-form OK", "truthy OK", "message-form OK", "lambda-form OK", "shadow OK");

            // lambda 处理器形式：断言失败时不抛出，异常对象交给 lambda
            assertEquals(3, log.size(), "log 应包含 lambda 捕获的消息、异常非空标记与遮蔽验证记录");
            assertTrue(String.valueOf(log.get(0)).contains("Assertion failed"),
                "lambda 应收到 AssertionException 消息，实际: " + log.get(0));
            assertEquals("true", String.valueOf(log.get(1)),
                "lambda 入参应为非空的 AssertionException 对象");
            assertEquals("shadowed", String.valueOf(log.get(2)));
        }
    }

    @Nested
    @DisplayName("assert-failure.etl - assert 失败行为")
    class AssertFailureTest {

        @Test
        @DisplayName("断言失败抛出 AssertionException 并携带自定义消息")
        void testAssertFailure() {
            Template template = new TemplateEngine().compile(getResourceFile(E2E + "assert-failure.etl"));
            AssertionException e = assertThrows(AssertionException.class,
                () -> template.render(new Context()));
            assertTrue(e.getMessage().contains("math still works"),
                "异常消息应包含自定义消息，实际: " + e.getMessage());
            assertTrue(e.getMessage().contains("Assertion failed"),
                "异常消息应包含断言失败描述，实际: " + e.getMessage());
        }
    }

    @Nested
    @DisplayName("assert-failure-plain.etl - assert 失败（无自定义消息）")
    class AssertFailurePlainTest {

        @Test
        @DisplayName("断言失败抛出 AssertionException 并携带源码结构感知消息")
        void testAssertFailurePlain() {
            Template template = new TemplateEngine().compile(getResourceFile(E2E + "assert-failure-plain.etl"));
            AssertionException e = assertThrows(AssertionException.class,
                () -> template.render(new Context()));
            assertTrue(e.getMessage().contains("Assertion failed"),
                "异常消息应包含断言失败描述，实际: " + e.getMessage());
            assertTrue(e.getMessage().contains("Expected") && e.getMessage().contains("Actual"),
                "异常消息应包含 Expected/Actual 明细，实际: " + e.getMessage());
        }
    }

    // ==================== 转义与原文输出 ====================

    @Nested
    @DisplayName("escaping-raw.etl - 转义与 @raw")
    class EscapingRawTest {

        @Test
        @DisplayName("\\{\\} 转义、@raw{...} 块（空块/嵌套/多行/特殊字符）、@raw: 冒号语法")
        void testEscapingRaw() {
            String result = render(E2E + "escaping-raw.etl");
            assertPassMarkers(result, "before-raw-colon OK",
                "escape OK", "raw-block OK", "raw-nested OK", "raw-multiline OK");
            assertTrue(result.contains("literal-braces: {name} and {%"), "实际输出:\n" + result);
            assertTrue(result.contains("single-open: {"), "实际输出:\n" + result);
            assertTrue(result.contains("single-close: }"), "实际输出:\n" + result);
            assertTrue(result.contains("block: {name} and ${x} and {% if }"), "实际输出:\n" + result);
            assertTrue(result.contains("empty-block: !"), "实际输出:\n" + result);
            assertTrue(result.contains("special: 100% {complete} $done"), "实际输出:\n" + result);
            assertTrue(result.contains("nested: a{b{}c}d"), "实际输出:\n" + result);
            assertTrue(result.contains("line1 {x}"), "实际输出:\n" + result);
            assertTrue(result.contains("line2 {y}"), "实际输出:\n" + result);
            assertTrue(result.contains(" tail {name} ${expr} {% if x } all raw"), "实际输出:\n" + result);
        }
    }

    // ==================== 空白行处理 ====================

    @Nested
    @DisplayName("whitespace.etl - 空白行处理")
    class WhitespaceTest {

        @Test
        @DisplayName("控制流标签独占行被抑制，自闭合块换行保留")
        void testWhitespace() {
            String result = render(E2E + "whitespace.etl").replace("\r\n", "\n");
            String expected = "商品列表:\n"
                + "- a\n"
                + "- b\n"
                + "done\n"
                + "\n"
                + "\n"
                + "after-let\n";
            assertEquals(expected, result);
        }
    }

    @Nested
    @DisplayName("whitespace-blocks.etl - if/switch 块空白行处理")
    class WhitespaceBlocksTest {

        @Test
        @DisplayName("if/switch 标签独占行被抑制，case 体内容正常渲染")
        void testWhitespaceBlocks() {
            String result = render(E2E + "whitespace-blocks.etl").replace("\r\n", "\n");
            assertFalse(result.contains("unreachable"), "不满足条件的分支不应渲染");
            assertFalse(result.contains("case-default"), "default 分支不应渲染");
            String expected = "if-body\n"
                + "case-one\n"
                + "end\n"
                + "\n";
            assertEquals(expected, result);
        }
    }

    // ==================== URL 字面量 ====================

    @Nested
    @DisplayName("url.etl - URL 字面量")
    class UrlTest {

        @Test
        @DisplayName("url(...) 各种格式/组件读取/各位置内嵌表达式/编码与转义")
        void testUrl() {
            String result = render(E2E + "url.etl");
            assertPassMarkers(result, "URL ALL PASS",
                "formats OK", "components OK", "embed-positions OK",
                "embed-multi OK", "missing OK", "encoding OK");
            assertTrue(result.contains("full: https://user:pass@host:8080/path?q=1#ref"),
                "实际输出:\n" + result);
            assertTrue(result.contains("relative-path: /path/only"), "实际输出:\n" + result);
            assertTrue(result.contains("query-only: ?a=1&b=2"), "实际输出:\n" + result);
            assertTrue(result.contains("ref-only: #section"), "实际输出:\n" + result);
            assertTrue(result.contains("ws: ws://host/path"), "实际输出:\n" + result);
            assertTrue(result.contains("empty: []"), "实际输出:\n" + result);
            assertTrue(result.contains("embed-userinfo: http://admin@example.com/p"), "实际输出:\n" + result);
            assertTrue(result.contains("embed-host: http://example.com/path"), "实际输出:\n" + result);
            assertTrue(result.contains("embed-port: http://example.com:8080/p"), "实际输出:\n" + result);
            assertTrue(result.contains("embed-path: http://example.com/api/v1/x"), "实际输出:\n" + result);
            assertTrue(result.contains("embed-query-key: http://h/p?q=1"), "实际输出:\n" + result);
            assertTrue(result.contains("embed-query-value: http://example.com/path?a=x+y"),
                "query 值应被 form 编码（空格 → +），实际输出:\n" + result);
            assertTrue(result.contains("embed-ref: http://example.com/p#top"), "实际输出:\n" + result);
            assertTrue(result.contains("embed-multi: http://admin@example.com:8080/api/v1?q=x+y#top"),
                "实际输出:\n" + result);
        }
    }

    // ==================== 模板复用（import/export/extends） ====================

    @Nested
    @DisplayName("reuse/main.etl - 模板导入与扩展")
    class ReuseTest {

        @Test
        @DisplayName("{% import 'utils.etl' as utils %} 与 {% extends 'layout.etl' %}")
        void testReuse() {
            File baseDir = getResourceFile(E2E + "reuse");
            TemplateEngine engine = new TemplateEngine(
                new TemplateEngine.TemplateEngineConfig(),
                new FileTemplateLoader(baseDir.getAbsolutePath()));

            Context context = new Context();
            context.put("title", "Welcome");
            String result = engine.compile(getResourceFile(E2E + "reuse/main.etl")).render(context);
            assertPassMarkers(result, "REUSE ALL PASS", "import OK", "extends OK");
            assertTrue(result.contains("[HEADER] Welcome"), "实际输出:\n" + result);
        }
    }
}
