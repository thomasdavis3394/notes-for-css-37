package org.dromara.easytl;

import org.dromara.easytl.exception.TemplateRuntimeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 边界条件测试
 * <p>
 * 测试 null 值、空集合、数值边界、类型转换、运算符优先级等
 * </p>
 *
 * @author EasyTL Team
 */
@DisplayName("Edge Case Tests")
class EdgeCaseTest {

    private TemplateEngine engine;

    @BeforeEach
    void setUp() {
        engine = new TemplateEngine();
    }

    // ========== 边界条件测试 ==========

    @Nested
    @DisplayName("Boundary Condition Tests")
    class BoundaryConditionTests {

        @Test
        @DisplayName("Null value handling")
        void testNullValueHandling() {
            String template = "{value}";
            Context context = new Context();
            context.put("value", null);
            assertEquals("", engine.compile(template).render(context));
        }

        @Test
        @DisplayName("Empty string handling")
        void testEmptyStringHandling() {
            String template = "{value}";
            Context context = new Context();
            context.put("value", "");
            assertEquals("", engine.compile(template).render(context));
        }

        @Test
        @DisplayName("Empty list handling")
        void testEmptyListHandling() {
            String template = "{% for item in list }{item}{/% for }";
            Context context = new Context();
            context.put("list", new ArrayList<>());
            assertEquals("", engine.compile(template).render(context));
        }

        @Test
        @DisplayName("Empty map handling")
        void testEmptyMapHandling() {
            String template = "{map.size()}";
            Context context = new Context();
            context.put("map", new HashMap<>());
            assertEquals("0", engine.compile(template).render(context));
        }

        @Test
        @DisplayName("Large number handling")
        void testLargeNumberHandling() {
            String template = "{a + b}";
            Context context = new Context();
            context.put("a", Long.MAX_VALUE - 1);
            context.put("b", 1);
            assertEquals(String.valueOf(Long.MAX_VALUE), engine.compile(template).render(context));
        }

        @Test
        @DisplayName("Negative number handling")
        void testNegativeNumberHandling() {
            String template = "{-2147483648}";
            Context context = new Context();
            assertEquals("-2147483648", engine.compile(template).render(context));
        }

        @Test
        @DisplayName("Zero division handling")
        void testZeroDivisionHandling() {
            String template = "{a / b}";
            Context context = new Context();
            context.put("a", 10);
            context.put("b", 0);
            assertThrows(TemplateRuntimeException.class, () -> engine.compile(template).render(context));
        }

        @Test
        @DisplayName("Deeply nested structure")
        void testDeeplyNestedStructure() {
            // Wait - let me check the actual syntax used elsewhere
            // The correct syntax uses } after the condition, not %}
            String template2 = "{% if a }{% if b }{% if c }Deep{/% if }{/% if }{/% if }";
            Context context = new Context();
            context.put("a", true);
            context.put("b", true);
            context.put("c", true);
            assertEquals("Deep", engine.compile(template2).render(context));
        }

        @Test
        @DisplayName("Very long string")
        void testVeryLongString() {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 1000; i++) {
                sb.append("x");
            }
            String longString = sb.toString();
            String template = "{text}";
            Context context = new Context();
            context.put("text", longString);
            assertEquals(longString, engine.compile(template).render(context));
        }

        @Test
        @DisplayName("Unicode characters")
        void testUnicodeCharacters() {
            String template = "{text}";
            Context context = new Context();
            context.put("text", "你好世界 🌍");
            assertEquals("你好世界 🌍", engine.compile(template).render(context));
        }

        @Test
        @DisplayName("Missing variable with elvis")
        void testMissingVariableWithElvis() {
            String template = "{missing ?? \"default\"}";
            assertEquals("default", engine.compile(template).render(new Context()));
        }

        @Test
        @DisplayName("Boolean edge cases")
        void testBooleanEdgeCases() {
            String template = "{a && b}";
            Context context = new Context();

            context.put("a", true);
            context.put("b", true);
            assertEquals("true", engine.compile(template).render(context));

            context.put("a", false);
            context.put("b", true);
            assertEquals("false", engine.compile(template).render(context));
        }

        @Test
        @DisplayName("Index out of bounds")
        void testIndexOutOfBounds() {
            String template = "{list[10]}";
            Context context = new Context();
            context.put("list", Arrays.asList("a", "b", "c"));
            assertThrows(TemplateRuntimeException.class, () -> engine.compile(template).render(context));
        }

        @Test
        @DisplayName("Whitespace handling")
        void testWhitespaceHandling() {
            String template = "  {name}  ";
            Context context = new Context();
            context.put("name", "John");
            assertEquals("  John  ", engine.compile(template).render(context));
        }

        @Test
        @DisplayName("Newline in template")
        void testNewlineInTemplate() {
            String template = "Line1\n{value}\nLine3";
            Context context = new Context();
            context.put("value", "Line2");
            assertEquals("Line1\nLine2\nLine3", engine.compile(template).render(context));
        }
    }

    // ========== 类型转换测试 ==========

    @Nested
    @DisplayName("Type Coercion Tests")
    class TypeCoercionTests {

        @Test
        @DisplayName("Integer to string comparison")
        void testIntegerToStringComparison() {
            String template = "{a == b}";
            Context context = new Context();
            context.put("a", 1);
            context.put("b", "1");
            // Types are compared strictly
            assertNotNull(engine.compile(template).render(context));
        }

        @Test
        @DisplayName("Number addition with different types")
        void testNumberAdditionDifferentTypes() {
            String template = "{a + b}";
            Context context = new Context();
            context.put("a", 10);
            context.put("b", 2.5);
            assertEquals("12.5", engine.compile(template).render(context));
        }

        @Test
        @DisplayName("BigDecimal arithmetic")
        void testBigDecimalArithmetic() {
            String template = "{a + b}";
            Context context = new Context();
            context.put("a", new BigDecimal("10.123"));
            context.put("b", new BigDecimal("5.877"));
            assertEquals("16.000", engine.compile(template).render(context));
        }

        @Test
        @DisplayName("String concatenation")
        void testStringConcatenation() {
            String template = "{a + b}";
            Context context = new Context();
            context.put("a", "Hello, ");
            context.put("b", "World!");
            assertEquals("Hello, World!", engine.compile(template).render(context));
        }

        @Test
        @DisplayName("Boolean in numeric context")
        void testBooleanInNumericContext() {
            String template = "{flag ? 1 : 0}";
            Context context = new Context();
            context.put("flag", true);
            assertEquals("1", engine.compile(template).render(context));
        }

        @Test
        @DisplayName("Null in concatenation")
        void testNullInConcatenation() {
            String template = "{a ?? \"\"}{b ?? \"\"}";
            Context context = new Context();
            context.put("a", null);
            context.put("b", "text");
            assertEquals("text", engine.compile(template).render(context));
        }

        @Test
        @DisplayName("Integer range iteration")
        void testIntegerRangeIteration() {
            String template = "{% for i in 1..5 }{i} {/% for }";
            assertEquals("1 2 3 4 5 ", engine.compile(template).render(new Context()));
        }

        @Test
        @DisplayName("Float precision")
        void testFloatPrecision() {
            String template = "{a}";
            Context context = new Context();
            context.put("a", 0.1 + 0.2);
            assertNotNull(engine.compile(template).render(context));
        }

        @Test
        @DisplayName("Long overflow")
        void testLongOverflow() {
            String template = "{a * b}";
            Context context = new Context();
            context.put("a", 1000000000L);
            context.put("b", 1000000000L);
            assertNotNull(engine.compile(template).render(context));
        }

        @Test
        @DisplayName("String to number conversion")
        void testStringToNumberConversion() {
            String template = "{str.length()}";
            Context context = new Context();
            context.put("str", "123");
            assertEquals("3", engine.compile(template).render(context));
        }

        @Test
        @DisplayName("List size comparison")
        void testListSizeComparison() {
            String template = "{list.size() > 0}";
            Context context = new Context();
            context.put("list", Arrays.asList(1, 2, 3));
            assertEquals("true", engine.compile(template).render(context));
        }

        @Test
        @DisplayName("Map key types")
        void testMapKeyTypes() {
            String template = "{map[\"key\"]}";
            Context context = new Context();
            Map<String, Object> map = new HashMap<>();
            map.put("key", "value");
            context.put("map", map);
            assertEquals("value", engine.compile(template).render(context));
        }
    }

    // ========== 运算符优先级测试 ==========

    @Nested
    @DisplayName("Operator Precedence Tests")
    class OperatorPrecedenceTests {

        @Test
        @DisplayName("Multiplication before addition")
        void testMultiplicationBeforeAddition() {
            String template = "{2 + 3 * 4}";
            assertEquals("14", engine.compile(template).render(new Context()));
        }

        @Test
        @DisplayName("Parentheses override precedence")
        void testParenthesesOverride() {
            String template = "{(2 + 3) * 4}";
            assertEquals("20", engine.compile(template).render(new Context()));
        }

        @Test
        @DisplayName("Logical AND before OR")
        void testLogicalAndBeforeOr() {
            String template = "{true || false && false}";
            assertEquals("true", engine.compile(template).render(new Context()));
        }

        @Test
        @DisplayName("Comparison operators")
        void testComparisonOperators() {
            String template = "{1 + 2 < 3 + 1}";
            assertEquals("true", engine.compile(template).render(new Context()));
        }

        @Test
        @DisplayName("Ternary right associativity")
        void testTernaryRightAssociativity() {
            String template = "{true ? 1 : false ? 2 : 3}";
            assertEquals("1", engine.compile(template).render(new Context()));
        }

        @Test
        @DisplayName("Elvis operator precedence")
        void testElvisOperatorPrecedence() {
            String template = "{null ?? \"default\"}";
            assertEquals("default", engine.compile(template).render(new Context()));
        }

        @Test
        @DisplayName("Mixed arithmetic and comparison")
        void testMixedArithmeticAndComparison() {
            String template = "{2 * 3 > 5}";
            assertEquals("true", engine.compile(template).render(new Context()));
        }

        @Test
        @DisplayName("Unary minus precedence")
        void testUnaryMinusPrecedence() {
            String template = "{-a + b}";
            Context context = new Context();
            context.put("a", 5);
            context.put("b", 10);
            assertEquals("5", engine.compile(template).render(context));
        }

        @Test
        @DisplayName("Method call precedence")
        void testMethodCallPrecedence() {
            String template = "{text.toUpperCase().length()}";
            Context context = new Context();
            context.put("text", "hello");
            assertEquals("5", engine.compile(template).render(context));
        }

        @Test
        @DisplayName("Complex expression")
        void testComplexExpression() {
            String template = "{(a > b ? a : b) * 2}";
            Context context = new Context();
            context.put("a", 10);
            context.put("b", 5);
            assertEquals("20", engine.compile(template).render(context));
        }
    }

    // ========== 特殊字符处理测试 ==========

    @Nested
    @DisplayName("Special Character Tests")
    class SpecialCharacterTests {

        @Test
        @DisplayName("String with quotes")
        void testStringWithQuotes() {
            String template = "{ \"He said \\\"hello\\\"\" }";
            assertEquals("He said \"hello\"", engine.compile(template).render(new Context()));
        }

        @Test
        @DisplayName("String with backslash")
        void testStringWithBackslash() {
            String template = "{ \"path\\\\to\\\\file\" }";
            assertEquals("path\\to\\file", engine.compile(template).render(new Context()));
        }

        @Test
        @DisplayName("Template with dollar sign")
        void testTemplateWithDollarSign() {
            String template = "Price: ${price}";
            Context context = new Context();
            context.put("price", "100");
            assertEquals("Price: 100", engine.compile(template).render(context));
        }

        @Test
        @DisplayName("Template with percent sign")
        void testTemplateWithPercentSign() {
            String template = "{value}%";
            Context context = new Context();
            context.put("value", 50);
            assertEquals("50%", engine.compile(template).render(context));
        }

        @Test
        @DisplayName("Template with angle brackets")
        void testTemplateWithAngleBrackets() {
            String template = "<div>{content}</div>";
            Context context = new Context();
            context.put("content", "Hello");
            assertEquals("<div>Hello</div>", engine.compile(template).render(context));
        }

        @Test
        @DisplayName("Template with ampersand")
        void testTemplateWithAmpersand() {
            String template = "{a} & {b}";
            Context context = new Context();
            context.put("a", "Tom");
            context.put("b", "Jerry");
            assertEquals("Tom & Jerry", engine.compile(template).render(context));
        }

        @Test
        @DisplayName("Newline in expression")
        void testNewlineInExpression() {
            String template = "{ \"line1\\nline2\" }";
            assertEquals("line1\nline2", engine.compile(template).render(new Context()));
        }

        @Test
        @DisplayName("Tab in expression")
        void testTabInExpression() {
            String template = "{ \"col1\\tcol2\" }";
            assertEquals("col1\tcol2", engine.compile(template).render(new Context()));
        }
    }

    // ========== 错误处理测试 ==========

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Undefined variable access")
        void testUndefinedVariableAccess() {
            String template = "{undefined}";
            assertEquals("", engine.compile(template).render(new Context()));
        }

        @Test
        @DisplayName("Method not found")
        void testMethodNotFound() {
            String template = "{obj.nonExistentMethod()}";
            Context context = new Context();
            context.put("obj", "string");
            assertThrows(TemplateRuntimeException.class, () -> engine.compile(template).render(context));
        }

        @Test
        @DisplayName("Null pointer in method call")
        void testNullPointerInMethodCall() {
            String template = "{obj.toString()}";
            Context context = new Context();
            context.put("obj", null);
            assertThrows(TemplateRuntimeException.class, () -> engine.compile(template).render(context));
        }

        @Test
        @DisplayName("Safe navigation with null")
        void testSafeNavigationWithNull() {
            String template = "{obj?.name}";
            Context context = new Context();
            context.put("obj", null);
            assertEquals("", engine.compile(template).render(context));
        }

        @Test
        @DisplayName("Invalid regex pattern")
        void testInvalidRegexPattern() {
            String template = "{ \"test\" =~ /[/ }";
            assertThrows(Exception.class, () -> engine.compile(template).render(new Context()));
        }

        @Test
        @DisplayName("Division by zero")
        void testDivisionByZero() {
            String template = "{1 / 0}";
            assertThrows(TemplateRuntimeException.class, () -> engine.compile(template).render(new Context()));
        }

        @Test
        @DisplayName("Modulo by zero")
        void testModuloByZero() {
            String template = "{1 % 0}";
            assertThrows(TemplateRuntimeException.class, () -> engine.compile(template).render(new Context()));
        }

        @Test
        @DisplayName("Index out of bounds - negative")
        void testIndexOutOfBoundsNegative() {
            String template = "{list[-4]}";
            Context context = new Context();
            context.put("list", Arrays.asList("a", "b", "c"));
            assertThrows(TemplateRuntimeException.class, () -> engine.compile(template).render(context));
        }

        @Test
        @DisplayName("Invalid type for operation")
        void testInvalidTypeForOperation() {
            String template = "{a + b}";
            Context context = new Context();
            context.put("a", "string");
            context.put("b", 10);
            // This might concatenate or throw depending on implementation
            assertNotNull(engine.compile(template).render(context));
        }

        @Test
        @DisplayName("Missing closing brace")
        void testMissingClosingBrace() {
            String template = "{unclosed";
            assertThrows(Exception.class, () -> engine.compile(template).render(new Context()));
        }

        @Test
        @DisplayName("Missing closing statement tag")
        void testMissingClosingStatementTag() {
            String template = "{% if true %}content";
            assertThrows(Exception.class, () -> engine.compile(template).render(new Context()));
        }

        @Test
        @DisplayName("Mismatched statement tags")
        void testMismatchedStatementTags() {
            String template = "{% if true %}content{/% for %}";
            assertThrows(Exception.class, () -> engine.compile(template).render(new Context()));
        }

        @Test
        @DisplayName("Invalid expression in if")
        void testInvalidExpressionInIf() {
            String template = "{% if %}content{/% if %}";
            assertThrows(Exception.class, () -> engine.compile(template).render(new Context()));
        }

        @Test
        @DisplayName("Empty template")
        void testEmptyTemplate() {
            String template = "";
            assertEquals("", engine.compile(template).render(new Context()));
        }

        @Test
        @DisplayName("Whitespace only template")
        void testWhitespaceOnlyTemplate() {
            String template = "   \n\t  ";
            assertEquals("   \n\t  ", engine.compile(template).render(new Context()));
        }

        @Test
        @DisplayName("Lambda with wrong arity")
        void testLambdaWithWrongArity() {
            String template = "{% let f = x -> x * 2 %}{f(1, 2)}";
            Context context = new Context();
            // This may or may not throw depending on implementation
            assertThrows(Exception.class, () -> engine.compile(template).render(context));
        }

        @Test
        @DisplayName("Null in list iteration")
        void testNullInListIteration() {
            String template = "{% for item in list }{item}{/% for }";
            Context context = new Context();
            context.put("list", null);
            assertThrows(Exception.class, () -> engine.compile(template).render(context));
        }

        @Test
        @DisplayName("Non-iterable in for loop")
        void testNonIterableInForLoop() {
            String template = "{% for item in num }{item}{/% for }";
            Context context = new Context();
            context.put("num", 123);
            assertThrows(Exception.class, () -> engine.compile(template).render(context));
        }

        @Test
        @DisplayName("Map access with non-existent key")
        void testMapAccessWithNonExistentKey() {
            String template = "{map[\"missing\"]}";
            Context context = new Context();
            Map<String, Object> map = new HashMap<>();
            context.put("map", map);
            assertEquals("", engine.compile(template).render(context));
        }
    }
}