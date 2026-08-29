package org.dromara.easytl;

import org.dromara.easytl.exception.AssertionException;
import org.dromara.easytl.exception.TemplateRuntimeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 内置契约断言函数 {@code assert(...)} 测试
 * <p>
 * 覆盖三种调用形式（条件 / 条件+自定义消息 / 条件+lambda 处理器）、
 * 比较/包含/集合/正则运算符的源码结构感知失败消息、
 * 函数调用条件的源码提示，以及参数校验与覆盖规则。
 * 消息约定：{@code ==} 时 Expected 为左值、Actual 为右值；
 * 其他二元运算符 Expected 为左值应满足的约束描述（如 {@code an Integer that was not <2>}）、
 * Actual 为右值。任何生成的消息行都不允许出现 "assert()"。
 * </p>
 *
 * @author EasyTL Team
 */
@DisplayName("assert() Function Tests")
class AssertFunctionTest {

    private TemplateEngine engine;

    @BeforeEach
    void setUp() {
        engine = new TemplateEngine();
        EasyTL.defaultEngine().clearCache();
    }

    private Context ctx(Object... kv) {
        Context context = new Context();
        for (int i = 0; i < kv.length; i += 2) {
            context.put((String) kv[i], kv[i + 1]);
        }
        return context;
    }

    private String render(String template, Context context) {
        return engine.compile(template).render(context);
    }

    /** 生成的消息行（非模板源码回显行）中绝不允许出现 "assert()" 字样 */
    private static void assertNoAssertSignature(String message) {
        assertFalse(message.contains("assert() expects"),
            "message must not contain \"assert() expects\": " + message);
        for (String line : message.split("\n")) {
            // 跳过位置信息中的模板源码回显行（如 "1 | {assert()}"）及其指针行
            if (line.matches("^\\s*\\d+ \\|.*") || line.trim().startsWith("|")) {
                continue;
            }
            assertFalse(line.contains("assert()"),
                "generated message line must not contain \"assert()\": " + line);
        }
    }

    // ========== 基本行为 ==========

    @Nested
    @DisplayName("Basic Behavior")
    class BasicBehavior {

        @Test
        @DisplayName("条件为真时不做任何事，渲染为空串")
        void truthyConditionDoesNothing() {
            assertEquals("", render("{assert(true)}", new Context()));
            assertEquals("ok!", render("ok{assert(1 == 1)}!", new Context()));
            assertEquals("", render("{assert('non-empty')}", new Context()));
        }

        @Test
        @DisplayName("条件为假时抛出 AssertionException")
        void falsyConditionThrows() {
            AssertionException e = assertThrows(AssertionException.class,
                () -> render("{assert(false)}", new Context()));
            assertTrue(e.getMessage().contains("Assertion failed"));
            assertNoAssertSignature(e.getMessage());
        }

        @Test
        @DisplayName("非比较条件的 truthy 风格消息")
        void nonComparisonConditionMessage() {
            AssertionException e = assertThrows(AssertionException.class,
                () -> render("{assert(x)}", ctx("x", 0)));
            assertTrue(e.getMessage().contains("Expected : x to be truthy\nActual   : <0>"),
                "actual: " + e.getMessage());
        }

        @Test
        @DisplayName("逻辑运算条件不做操作数拆解，按整体源码提示")
        void logicalConditionMessage() {
            AssertionException e = assertThrows(AssertionException.class,
                () -> render("{assert(x && y)}", ctx("x", true, "y", false)));
            assertTrue(e.getMessage().contains("Expected : x && y to be truthy\nActual   : <false>"),
                "actual: " + e.getMessage());
        }

        @Test
        @DisplayName("位置指针行只显示消息首行，不重复 Expected/Actual 内容")
        void pointerLineShowsFirstLineOnly() {
            AssertionException e = assertThrows(AssertionException.class,
                () -> render("{assert(a > b)}", ctx("a", 1, "b", 2)));
            String message = e.getMessage();
            // Expected/Actual 仅在头部出现一次；^^^^ 指针行只带消息首行
            assertEquals(1, countOccurrences(message, "Expected :"), "actual: " + message);
            assertEquals(1, countOccurrences(message, "Actual   :"), "actual: " + message);
        }
    }

    private static int countOccurrences(String text, String token) {
        int count = 0;
        for (int i = text.indexOf(token); i >= 0; i = text.indexOf(token, i + token.length())) {
            count++;
        }
        return count;
    }

    // ========== 比较运算符消息 ==========

    @Nested
    @DisplayName("Comparison Operator Messages")
    class ComparisonMessages {

        @Test
        @DisplayName("== 失败消息：Expected 为左值，Actual 为右值")
        void equalMessage() {
            AssertionException e = assertThrows(AssertionException.class,
                () -> render("{assert(a == b)}", ctx("a", 1, "b", 2)));
            assertTrue(e.getMessage().contains(
                "Assertion failed: expected a to be equal to b\nExpected : a = <1>\nActual   : b = <2>"),
                "actual: " + e.getMessage());
        }

        @Test
        @DisplayName("复杂表达式操作数按源码原样展示")
        void complexOperandSourceMessage() {
            AssertionException e = assertThrows(AssertionException.class,
                () -> render("{% let calc = (p) -> p + 12 %}{assert(100 * n == calc(price))}",
                    ctx("n", 3, "price", 300)));
            assertTrue(e.getMessage().contains(
                "Assertion failed: expected 100 * n to be equal to calc(price)"),
                "actual: " + e.getMessage());
            assertTrue(e.getMessage().contains(
                "Expected : 100 * n = <300>\nActual   : calc(price) = <312>"),
                "actual: " + e.getMessage());
        }

        @Test
        @DisplayName("!= 失败消息：Expected 为约束描述")
        void notEqualMessage() {
            AssertionException e = assertThrows(AssertionException.class,
                () -> render("{assert(a != b)}", ctx("a", 2, "b", 2)));
            assertTrue(e.getMessage().contains(
                "Assertion failed: expected a not to be equal to b\n"
              + "Expected : an Integer that was not <2>\nActual   : b = <2>"),
                "actual: " + e.getMessage());
        }

        @Test
        @DisplayName("> 失败消息")
        void greaterThanMessage() {
            AssertionException e = assertThrows(AssertionException.class,
                () -> render("{assert(a > b)}", ctx("a", 1, "b", 2)));
            assertTrue(e.getMessage().contains(
                "Assertion failed: expected a to be greater than b\n"
              + "Expected : an Integer that was greater than <2>\nActual   : b = <2>"),
                "actual: " + e.getMessage());
        }

        @Test
        @DisplayName(">= 失败消息")
        void atLeastMessage() {
            AssertionException e = assertThrows(AssertionException.class,
                () -> render("{assert(user.age >= 18)}", ctx("user", user(16))));
            assertTrue(e.getMessage().contains(
                "Assertion failed: expected user.age to be at least 18\n"
              + "Expected : an Integer that was at least <18>\nActual   : 18 = <18>"),
                "actual: " + e.getMessage());
        }

        @Test
        @DisplayName("< 失败消息")
        void lessThanMessage() {
            AssertionException e = assertThrows(AssertionException.class,
                () -> render("{assert(a < b)}", ctx("a", 3, "b", 2)));
            assertTrue(e.getMessage().contains(
                "Assertion failed: expected a to be less than b\n"
              + "Expected : an Integer that was less than <2>\nActual   : b = <2>"),
                "actual: " + e.getMessage());
        }

        @Test
        @DisplayName("<= 失败消息")
        void atMostMessage() {
            AssertionException e = assertThrows(AssertionException.class,
                () -> render("{assert(a <= b)}", ctx("a", 3, "b", 2)));
            assertTrue(e.getMessage().contains(
                "Assertion failed: expected a to be at most b\n"
              + "Expected : an Integer that was at most <2>\nActual   : b = <2>"),
                "actual: " + e.getMessage());
        }
    }

    // ========== 包含/集合/正则运算符消息 ==========

    @Nested
    @DisplayName("Containment And Regex Operator Messages")
    class ContainmentMessages {

        @Test
        @DisplayName("in 失败消息：Expected 为约束描述，Actual 为左边被检验值")
        void inMessage() {
            AssertionException e = assertThrows(AssertionException.class,
                () -> render("{assert(role in roles)}",
                    ctx("role", "guest", "roles", Arrays.asList("admin", "moderator"))));
            assertTrue(e.getMessage().contains(
                "Assertion failed: expected role to be in roles\n"
              + "Expected : a String in <[admin, moderator]>\nActual   : role = <guest>"),
                "actual: " + e.getMessage());
        }

        @Test
        @DisplayName("!in 失败消息")
        void notInMessage() {
            AssertionException e = assertThrows(AssertionException.class,
                () -> render("{assert(a !in b)}", ctx("a", 1, "b", Arrays.asList(1, 2))));
            assertTrue(e.getMessage().contains(
                "Assertion failed: expected a not to be in b\n"
              + "Expected : an Integer not in <[1, 2]>\nActual   : a = <1>"),
                "actual: " + e.getMessage());
        }

        @Test
        @DisplayName("subsetof 失败消息")
        void subsetofMessage() {
            AssertionException e = assertThrows(AssertionException.class,
                () -> render("{assert(a subsetof b)}",
                    ctx("a", Arrays.asList(1, 4), "b", Arrays.asList(1, 2, 3))));
            assertTrue(e.getMessage().contains(
                "Assertion failed: expected a to be a subset of b\n"
              + "Expected : an ArrayList that is a subset of <[1, 2, 3]>\nActual   : a = <[1, 4]>"),
                "actual: " + e.getMessage());
        }

        @Test
        @DisplayName("anyof 失败消息")
        void anyofMessage() {
            AssertionException e = assertThrows(AssertionException.class,
                () -> render("{assert(a anyof b)}",
                    ctx("a", Arrays.asList(4), "b", Arrays.asList(1, 2, 3))));
            assertTrue(e.getMessage().contains(
                "Assertion failed: expected a to have at least one element in common with b\n"
              + "Expected : an ArrayList that has at least one element in common with <[1, 2, 3]>\nActual   : a = <[4]>"),
                "actual: " + e.getMessage());
        }

        @Test
        @DisplayName("noneof 失败消息")
        void noneofMessage() {
            AssertionException e = assertThrows(AssertionException.class,
                () -> render("{assert(a noneof b)}",
                    ctx("a", Arrays.asList(1), "b", Arrays.asList(1, 2, 3))));
            assertTrue(e.getMessage().contains(
                "Assertion failed: expected a to have no elements in common with b\n"
              + "Expected : an ArrayList that has no elements in common with <[1, 2, 3]>\nActual   : a = <[1]>"),
                "actual: " + e.getMessage());
        }

        @Test
        @DisplayName("=~ 失败消息")
        void regexMatchMessage() {
            AssertionException e = assertThrows(AssertionException.class,
                () -> render("{assert(name =~ /\\d+/)}", ctx("name", "abc")));
            assertTrue(e.getMessage().contains(
                "Assertion failed: expected name to match /\\d+/\n"
              + "Expected : a String that matches <\\d+>\nActual   : name = <abc>"),
                "actual: " + e.getMessage());
        }

        @Test
        @DisplayName("!=~ 失败消息")
        void regexNotMatchMessage() {
            AssertionException e = assertThrows(AssertionException.class,
                () -> render("{assert(name !=~ /\\d+/)}", ctx("name", "123")));
            assertTrue(e.getMessage().contains(
                "Assertion failed: expected name not to match /\\d+/\n"
              + "Expected : a String that does not match <\\d+>\nActual   : name = <123>"),
                "actual: " + e.getMessage());
        }
    }

    // ========== 函数调用条件 ==========

    @Nested
    @DisplayName("Function Call Condition Messages")
    class FunctionCallMessages {

        @Test
        @DisplayName("全局函数调用条件使用调用源码提示")
        void globalFunctionCallMessage() {
            AssertionException e = assertThrows(AssertionException.class,
                () -> render("{assert(match(name, 'x'))}", ctx("name", "abc")));
            assertTrue(e.getMessage().contains(
                "Expected : match(name, 'x') to return true\nActual   : <false>"),
                "actual: " + e.getMessage());
        }

        @Test
        @DisplayName("方法调用条件使用调用源码提示")
        void methodCallMessage() {
            AssertionException e = assertThrows(AssertionException.class,
                () -> render("{assert(list.contains(x))}",
                    ctx("list", new ArrayList<>(Arrays.asList(1, 2)), "x", 9)));
            assertTrue(e.getMessage().contains(
                "Expected : list.contains(x) to return true\nActual   : <false>"),
                "actual: " + e.getMessage());
        }
    }

    // ========== 自定义消息 ==========

    @Nested
    @DisplayName("Custom Message Form")
    class CustomMessageForm {

        @Test
        @DisplayName("自定义消息作为前缀（JUnit 5 风格 msg ==> detail）")
        void customMessagePrefix() {
            AssertionException e = assertThrows(AssertionException.class,
                () -> render("{assert(a > b, 'price check')}", ctx("a", 1, "b", 2)));
            assertTrue(e.getMessage().contains(
                "price check ==> Assertion failed: expected a to be greater than b"),
                "actual: " + e.getMessage());
        }

        @Test
        @DisplayName("自定义消息 + 非比较条件")
        void customMessageWithNonComparison() {
            AssertionException e = assertThrows(AssertionException.class,
                () -> render("{assert(x, 'x must be set')}", ctx("x", null)));
            assertTrue(e.getMessage().contains(
                "x must be set ==> Assertion failed:\nExpected : x to be truthy"),
                "actual: " + e.getMessage());
        }
    }

    // ========== lambda 处理器 ==========

    @Nested
    @DisplayName("Lambda Handler Form")
    class LambdaHandlerForm {

        @Test
        @DisplayName("条件为假时执行 lambda 且不再抛出，渲染继续")
        void lambdaHandlerNotRethrown() {
            List<Object> log = new ArrayList<>();
            Context context = ctx("log", log);
            String result = render("X{assert(1 > 2, e -> log.add(e.getMessage()))}Y", context);
            assertEquals("XY", result);
            assertEquals(1, log.size());
            assertTrue(String.valueOf(log.get(0)).contains("Assertion failed: expected 1 to be greater than 2"),
                "actual: " + log.get(0));
        }

        @Test
        @DisplayName("lambda 入参是 AssertionException")
        void lambdaReceivesExceptionObject() {
            List<Object> log = new ArrayList<>();
            Context context = ctx("log", log);
            render("{assert(a == b, e -> log.add(e))}", ctx("a", 1, "b", 2, "log", log));
            assertEquals(1, log.size());
            assertInstanceOf(AssertionException.class, log.get(0));
        }

        @Test
        @DisplayName("条件为真时 lambda 不执行")
        void lambdaNotInvokedWhenTruthy() {
            List<Object> log = new ArrayList<>();
            Context context = ctx("log", log);
            String result = render("{assert(1 < 2, e -> log.add(1))}", context);
            assertEquals("", result);
            assertTrue(log.isEmpty());
        }
    }

    // ========== 参数校验与覆盖规则 ==========

    @Nested
    @DisplayName("Argument Validation And Override")
    class ValidationAndOverride {

        @Test
        @DisplayName("0 个参数报错")
        void zeroArguments() {
            TemplateRuntimeException e = assertThrows(TemplateRuntimeException.class,
                () -> render("{assert()}", new Context()));
            assertTrue(e.getMessage().contains("Assertion requires 1 or 2 argument(s), but got 0"),
                "actual: " + e.getMessage());
            assertNoAssertSignature(e.getMessage());
        }

        @Test
        @DisplayName("3 个参数报错")
        void threeArguments() {
            TemplateRuntimeException e = assertThrows(TemplateRuntimeException.class,
                () -> render("{assert(a, 'm', 'x')}", ctx("a", true)));
            assertTrue(e.getMessage().contains("Assertion requires 1 or 2 argument(s), but got 3"),
                "actual: " + e.getMessage());
            assertNoAssertSignature(e.getMessage());
        }

        @Test
        @DisplayName("第二参数类型非法时报错")
        void invalidSecondArgumentType() {
            TemplateRuntimeException e = assertThrows(TemplateRuntimeException.class,
                () -> render("{assert(a, 42)}", ctx("a", false)));
            assertTrue(e.getMessage().contains("Assertion requires a string message or a lambda as second argument"),
                "actual: " + e.getMessage());
            assertNoAssertSignature(e.getMessage());
        }

        @Test
        @DisplayName("let 定义的同名 lambda 覆盖内置 assert")
        void letLambdaOverridesBuiltin() {
            Context context = ctx("log", new ArrayList<>());
            String result = render("{% let assert = (c) -> 'called' %}{assert(false)}", context);
            assertEquals("called", result);
        }

        @Test
        @DisplayName("在 {% if %} 语句中使用")
        void assertInsideStatementBlock() {
            AssertionException e = assertThrows(AssertionException.class,
                () -> render("{% if flag }ok{/% if }{assert(score >= 60, 'score too low')}",
                    ctx("flag", true, "score", 42)));
            assertTrue(e.getMessage().contains("score too low ==>"),
                "actual: " + e.getMessage());
        }
    }

    private static Map<String, Object> user(int age) {
        Map<String, Object> user = new HashMap<>();
        user.put("age", age);
        return user;
    }
}
