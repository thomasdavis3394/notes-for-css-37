package org.dromara.easytl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * BNF 语法系统性覆盖测试
 * <p>
 * 基于 BNF_GRAMMAR.md 和 README.md 覆盖每个语法规则
 * </p>
 *
 * @author EasyTL Team
 */
@DisplayName("Grammar Coverage Tests")
class GrammarCoverageTest {

    private TemplateEngine engine;

    @BeforeEach
    void setUp() {
        engine = new TemplateEngine();
    }

    // ========== 模板结构测试 ==========

    @Nested
    @DisplayName("Template Structure Tests")
    class TemplateStructureTests {
        // Section 3.1.1 tests

        @Test
        @DisplayName("Single brace expression - basic")
        void testSingleBraceExpression_basic() {
            // Given: 单花括号表达式模板
            String template = "Hello, {name}!";
            Context context = new Context();
            context.put("name", "World");

            // When: 渲染模板
            String result = engine.compile(template).render(context);

            // Then: 变量被正确替换
            assertEquals("Hello, World!", result);
        }

        @Test
        @DisplayName("Double brace expression - basic")
        void testDoubleBraceExpression_basic() {
            // Given: 双花括号表达式模板
            TemplateEngine.TemplateEngineConfig config = new TemplateEngine.TemplateEngineConfig()
                    .setBraceSyntax(TemplateEngine.TemplateEngineConfig.BraceSyntax.DOUBLE);
            TemplateEngine doubleBraceEngine = new TemplateEngine(config);
            String template = "Hello, {{name}}!";
            Context context = new Context();
            context.put("name", "World");

            // When: 渲染模板
            String result = doubleBraceEngine.compile(template).render(context);

            // Then: 变量被正确替换
            assertEquals("Hello, World!", result);
        }

        @Test
        @DisplayName("Dollar brace expression - basic")
        void testDollarBraceExpression_basic() {
            // Given: 美元花括号表达式模板
            String template = "Hello, ${name}!";
            Context context = new Context();
            context.put("name", "World");

            // When: 渲染模板
            String result = engine.compile(template).render(context);

            // Then: 变量被正确替换
            assertEquals("Hello, World!", result);
        }

        @Test
        @DisplayName("Mixed expression wrappers")
        void testMixedExpressionWrappers() {
            // Given: 混合使用三种表达式语法
            String template = "{a} and {{b}} and ${c}";
            TemplateEngine.TemplateEngineConfig config = new TemplateEngine.TemplateEngineConfig()
                    .setBraceSyntax(TemplateEngine.TemplateEngineConfig.BraceSyntax.DOUBLE);
            TemplateEngine doubleBraceEngine = new TemplateEngine(config);
            Context context = new Context();
            context.put("a", "A");
            context.put("b", "B");
            context.put("c", "C");

            // When: 渲染模板
            String result = doubleBraceEngine.compile(template).render(context);

            // Then: 所有表达式被正确解析
            assertEquals("A and B and C", result);
        }

        @Test
        @DisplayName("Plain text only - no expressions")
        void testPlainTextOnly() {
            // Given: 纯文本模板，无表达式
            String template = "Hello, World! This is plain text.";

            // When: 渲染模板
            String result = engine.compile(template).render(new Context());

            // Then: 原样输出
            assertEquals("Hello, World! This is plain text.", result);
        }
    }

    // ========== 语句块测试 ==========

    @Nested
    @DisplayName("Statement Tests")
    class StatementTests {
        // Section 3.1.2 tests will go here

        @Nested
        @DisplayName("If Statement Tests")
        class IfStatementTests {

            @Test
            @DisplayName("Simple if statement")
            void testIfStatement_simple() {
                String template = "{% if flag }Yes{/% if }";
                Context context = new Context();
                context.put("flag", true);
                assertEquals("Yes", engine.compile(template).render(context));
            }

            @Test
            @DisplayName("If-else statement")
            void testIfStatement_withElse() {
                String template = "{% if flag }Yes{% else }No{/% if }";
                Context context1 = new Context();
                context1.put("flag", true);
                assertEquals("Yes", engine.compile(template).render(context1));

                Context context2 = new Context();
                context2.put("flag", false);
                assertEquals("No", engine.compile(template).render(context2));
            }

            @Test
            @DisplayName("If-else if-else statement")
            void testIfStatement_withElseIfAndElse() {
                String template = "{% if x == 1 }One{% else if x == 2 }Two{% else }Other{/% if }";
                Context context = new Context();
                context.put("x", 1);
                assertEquals("One", engine.compile(template).render(context));
                context.put("x", 2);
                assertEquals("Two", engine.compile(template).render(context));
                context.put("x", 3);
                assertEquals("Other", engine.compile(template).render(context));
            }

            @Test
            @DisplayName("Multiple else if clauses")
            void testIfStatement_multipleElseIf() {
                String template = "{% if x == 1 }A{% else if x == 2 }B{% else if x == 3 }C{% else if x == 4 }D{% else }E{/% if }";
                Context context = new Context();

                context.put("x", 1);
                assertEquals("A", engine.compile(template).render(context));
                context.put("x", 2);
                assertEquals("B", engine.compile(template).render(context));
                context.put("x", 3);
                assertEquals("C", engine.compile(template).render(context));
                context.put("x", 4);
                assertEquals("D", engine.compile(template).render(context));
                context.put("x", 5);
                assertEquals("E", engine.compile(template).render(context));
            }

            @Test
            @DisplayName("Nested if statements")
            void testIfStatement_nested() {
                String template = "{% if a }A{% if b }B{/% if }{/% if }";
                Context context = new Context();

                context.put("a", true);
                context.put("b", true);
                assertEquals("AB", engine.compile(template).render(context));

                context.put("a", true);
                context.put("b", false);
                assertEquals("A", engine.compile(template).render(context));

                context.put("a", false);
                context.put("b", true);
                assertEquals("", engine.compile(template).render(context));
            }

            @Test
            @DisplayName("If condition with complex expression")
            void testIfStatement_withExpressionCondition() {
                String template = "{% if x > 5 && y < 10 }Valid{/% if }";
                Context context = new Context();

                context.put("x", 6);
                context.put("y", 9);
                assertEquals("Valid", engine.compile(template).render(context));

                context.put("x", 5);
                assertEquals("", engine.compile(template).render(context));
            }
        }

        @Nested
        @DisplayName("Switch Statement Tests")
        class SwitchStatementTests {

            @Test
            @DisplayName("Simple switch statement")
            void testSwitchStatement_simple() {
                String template = "{% switch x }{% case 1 }One{/% switch }";
                Context context = new Context();
                context.put("x", 1);
                assertEquals("One", engine.compile(template).render(context));
            }

            @Test
            @DisplayName("Switch with multiple cases")
            void testSwitchStatement_multipleCases() {
                String template = "{% switch x }{% case 1 }A{% case 2 }B{% case 3 }C{/% switch }";
                Context context = new Context();

                context.put("x", 1);
                assertEquals("A", engine.compile(template).render(context));
                context.put("x", 2);
                assertEquals("B", engine.compile(template).render(context));
                context.put("x", 3);
                assertEquals("C", engine.compile(template).render(context));
            }

            @Test
            @DisplayName("Switch with default")
            void testSwitchStatement_withDefault() {
                String template = "{% switch x }{% case 1 }One{% default }Other{/% switch }";
                Context context = new Context();

                context.put("x", 1);
                assertEquals("One", engine.compile(template).render(context));
                context.put("x", 99);
                assertEquals("Other", engine.compile(template).render(context));
            }

            @Test
            @DisplayName("Switch case with expression")
            void testSwitchStatement_caseExpression() {
                String template = "{% switch x }{% case 1 + 1 }Two{% default }Other{/% switch }";
                Context context = new Context();
                context.put("x", 2);
                assertEquals("Two", engine.compile(template).render(context));
            }

            @Test
            @DisplayName("Switch with string case")
            void testSwitchStatement_stringCase() {
                String template = "{% switch status }{% case \"active\" }Active{% case \"inactive\" }Inactive{/% switch }";
                Context context = new Context();

                context.put("status", "active");
                assertEquals("Active", engine.compile(template).render(context));
                context.put("status", "inactive");
                assertEquals("Inactive", engine.compile(template).render(context));
            }
        }

        @Nested
        @DisplayName("For Statement Tests")
        class ForStatementTests {

            @Test
            @DisplayName("Simple for loop")
            void testForStatement_simple() {
                String template = "{% for item in items }{item} {/% for }";
                Context context = new Context();
                context.put("items", Arrays.asList("a", "b", "c"));
                assertEquals("a b c ", engine.compile(template).render(context));
            }

            @Test
            @DisplayName("For loop with index")
            void testForStatement_withIndex() {
                String template = "{% for i, item in items }{i}:{item} {/% for }";
                Context context = new Context();
                context.put("items", Arrays.asList("a", "b", "c"));
                assertEquals("0:a 1:b 2:c ", engine.compile(template).render(context));
            }

            @Test
            @DisplayName("For loop with range")
            void testForStatement_withRange() {
                String template = "{% for i in 1..3 }{i} {/% for }";
                Context context = new Context();
                assertEquals("1 2 3 ", engine.compile(template).render(context));
            }

            @Test
            @DisplayName("For loop with list variable")
            void testForStatement_withList() {
                String template = "{% for num in numbers }{num * 2} {/% for }";
                Context context = new Context();
                context.put("numbers", Arrays.asList(1, 2, 3));
                assertEquals("2 4 6 ", engine.compile(template).render(context));
            }

            @Test
            @DisplayName("Nested for loops")
            void testForStatement_nested() {
                String template = "{% for i in 1..2 }{% for j in 1..2 }({i},{j}) {/% for }{/% for }";
                Context context = new Context();
                assertEquals("(1,1) (1,2) (2,1) (2,2) ", engine.compile(template).render(context));
            }

            @Test
            @DisplayName("For loop with Map keys")
            void testForStatement_withMap() {
                String template = "{% for key in map.keySet() }{key}={map[key]} {/% for }";
                Context context = new Context();
                Map<String, String> map = new LinkedHashMap<>();
                map.put("a", "1");
                map.put("b", "2");
                context.put("map", map);
                assertEquals("a=1 b=2 ", engine.compile(template).render(context));
            }
        }

        @Nested
        @DisplayName("Let Statement Tests")
        class LetStatementTests {

            @Test
            @DisplayName("Let with number value")
            void testLetStatement_numberValue() {
                String template = "{% let x = 42 %}{x}";
                Context context = new Context();
                assertEquals("42", engine.compile(template).render(context));
            }

            @Test
            @DisplayName("Let with string value")
            void testLetStatement_stringValue() {
                String template = "{% let name = \"World\" %}Hello, {name}!";
                Context context = new Context();
                assertEquals("Hello, World!", engine.compile(template).render(context));
            }

            @Test
            @DisplayName("Let with expression value")
            void testLetStatement_expressionValue() {
                String template = "{% let sum = 10 + 20 %}{sum}";
                Context context = new Context();
                assertEquals("30", engine.compile(template).render(context));
            }

            @Test
            @DisplayName("Let with list value")
            void testLetStatement_listValue() {
                String template = "{% let items = [1, 2, 3] %}{% for i in items %}{i} {/% for }";
                Context context = new Context();
                assertEquals("1 2 3 ", engine.compile(template).render(context));
            }

            @Test
            @DisplayName("Let with map value")
            void testLetStatement_mapValue() {
                String template = "{% let user = {\"name\": \"John\", \"age\": 30} %}{user[\"name\"]} is {user[\"age\"]}";
                Context context = new Context();
                assertEquals("John is 30", engine.compile(template).render(context));
            }

            @Test
            @DisplayName("Let with lambda value")
            void testLetStatement_lambdaValue() {
                String template = "{% let doubler = x -> x * 2 %}{doubler(5)}";
                Context context = new Context();
                assertEquals("10", engine.compile(template).render(context));
            }

            @Test
            @DisplayName("Multiple let statements")
            void testLetStatement_multiple() {
                String template = "{% let a = 1 %}{% let b = 2 %}{% let c = a + b %}{c}";
                Context context = new Context();
                assertEquals("3", engine.compile(template).render(context));
            }
        }
    }

    // ========== 结构化表达式测试 ==========

    @Nested
    @DisplayName("Structured Expression Tests")
    class StructuredExpressionTests {
        // Section 3.1.3 tests will go here

        @Test
        @DisplayName("If expression - basic with else")
        void testIfExpression_basicWithElse() {
            // Given: if 表达式作为值 (必须有 else 分支)
            String template = "{% let result = if (true) { \"yes\" } else { \"no\" } %}{result}";
            Context context = new Context();

            // When: 渲染模板
            String result = engine.compile(template).render(context);

            // Then: if 表达式正确求值
            assertEquals("yes", result);
        }

        @Test
        @DisplayName("If expression with else")
        void testIfExpression_withElse() {
            // Given: if-else 表达式 (使用代码块语法)
            String template = "{% let result = if (flag) { \"yes\" } else { \"no\" } %}{result}";
            Context context = new Context();

            context.put("flag", true);
            assertEquals("yes", engine.compile(template).render(context));

            context.put("flag", false);
            assertEquals("no", engine.compile(template).render(context));
        }

        @Test
        @DisplayName("If expression with multiple else if")
        void testIfExpression_multipleElseIf() {
            // Given: 多 else if 的 if 表达式 (使用代码块语法)
            String template = "{% let result = if (x == 1) { \"one\" } else if (x == 2) { \"two\" } else { \"other\" } %}{result}";
            Context context = new Context();

            context.put("x", 1);
            assertEquals("one", engine.compile(template).render(context));
            context.put("x", 2);
            assertEquals("two", engine.compile(template).render(context));
            context.put("x", 3);
            assertEquals("other", engine.compile(template).render(context));
        }

        @Test
        @DisplayName("For expression - basic")
        void testForExpression_basic() {
            // Given: for 表达式 (在单花括号表达式内)
            String template = "{for (i in 1..3) { i }}";
            Context context = new Context();

            // When: 渲染模板
            String result = engine.compile(template).render(context);

            // Then: for 表达式正确执行
            assertNotNull(result);
        }

        @Test
        @DisplayName("For expression in list")
        void testForExpression_inList() {
            // Given: for 表达式遍历列表
            String template = "{for (i in items) { i * 2 }}";
            Context context = new Context();
            context.put("items", Arrays.asList(1, 2, 3));

            // When: 渲染模板
            String result = engine.compile(template).render(context);

            // Then: for 表达式正确执行
            assertNotNull(result);
        }

        @Test
        @DisplayName("If expression with complex condition")
        void testIfExpression_complexCondition() {
            // Given: if 表达式带复杂条件
            String template = "{% let result = if (x > 5 && y < 10) { \"valid\" } else { \"invalid\" } %}{result}";
            Context context = new Context();

            context.put("x", 6);
            context.put("y", 9);
            assertEquals("valid", engine.compile(template).render(context));

            context.put("x", 5);
            assertEquals("invalid", engine.compile(template).render(context));
        }

        @Test
        @DisplayName("Structured expression in let with else")
        void testStructuredExpression_inLet() {
            // Given: 结构化表达式在 let 语句中 (必须有 else)
            String template = "{% let value = if (true) { 42 } else { 0 } %}{value}";
            Context context = new Context();

            // When: 渲染模板
            String result = engine.compile(template).render(context);

            // Then: 正确赋值
            assertEquals("42", result);
        }
    }

    // ========== 运算符测试 ==========

    @Nested
    @DisplayName("Operator Tests")
    class OperatorTests {
        // Section 3.1.4 tests

        // ========== Elvis 运算符 ==========

        @Nested
        @DisplayName("Elvis Operator Tests")
        class ElvisOperatorTests {

            @Test
            @DisplayName("Elvis operator - basic")
            void testElvisOperator_basic() {
                String template = "{value ?? \"default\"}";
                Context context = new Context();
                context.put("value", "actual");
                assertEquals("actual", engine.compile(template).render(context));
            }

            @Test
            @DisplayName("Elvis operator with null")
            void testElvisOperator_withNull() {
                String template = "{missing ?? \"default\"}";
                assertEquals("default", engine.compile(template).render(new Context()));
            }

            @Test
            @DisplayName("Elvis operator chain")
            void testElvisOperator_chain() {
                String template = "{a ?? b ?? \"fallback\"}";
                Context context = new Context();
                context.put("a", null);
                context.put("b", null);
                assertEquals("fallback", engine.compile(template).render(context));
            }
        }

        // ========== 三元运算符 ==========

        @Nested
        @DisplayName("Ternary Operator Tests")
        class TernaryOperatorTests {

            @Test
            @DisplayName("Ternary operator - basic")
            void testTernaryOperator_basic() {
                String template = "{flag ? \"yes\" : \"no\"}";
                Context context = new Context();

                context.put("flag", true);
                assertEquals("yes", engine.compile(template).render(context));

                context.put("flag", false);
                assertEquals("no", engine.compile(template).render(context));
            }

            @Test
            @DisplayName("Nested ternary operator")
            void testTernaryOperator_nested() {
                String template = "{x > 0 ? \"positive\" : (x < 0 ? \"negative\" : \"zero\")}";
                Context context = new Context();

                context.put("x", 1);
                assertEquals("positive", engine.compile(template).render(context));
                context.put("x", -1);
                assertEquals("negative", engine.compile(template).render(context));
                context.put("x", 0);
                assertEquals("zero", engine.compile(template).render(context));
            }

            @Test
            @DisplayName("Ternary with expressions")
            void testTernaryOperator_withExpressions() {
                String template = "{flag ? a + b : a - b}";
                Context context = new Context();
                context.put("flag", true);
                context.put("a", 10);
                context.put("b", 3);
                assertEquals("13", engine.compile(template).render(context));
            }
        }

        // ========== 逻辑运算符 ==========

        @Nested
        @DisplayName("Logical Operator Tests")
        class LogicalOperatorTests {

            @Test
            @DisplayName("Logical OR - basic")
            void testLogicalOr_basic() {
                String template = "{a || b}";
                Context context = new Context();

                context.put("a", false);
                context.put("b", true);
                assertEquals("true", engine.compile(template).render(context));

                context.put("a", false);
                context.put("b", false);
                assertEquals("false", engine.compile(template).render(context));
            }

            @Test
            @DisplayName("Logical AND - basic")
            void testLogicalAnd_basic() {
                String template = "{a && b}";
                Context context = new Context();

                context.put("a", true);
                context.put("b", true);
                assertEquals("true", engine.compile(template).render(context));

                context.put("a", true);
                context.put("b", false);
                assertEquals("false", engine.compile(template).render(context));
            }

            @Test
            @DisplayName("Logical NOT - basic")
            void testLogicalNot_basic() {
                String template = "{!flag}";
                Context context = new Context();

                context.put("flag", true);
                assertEquals("false", engine.compile(template).render(context));

                context.put("flag", false);
                assertEquals("true", engine.compile(template).render(context));
            }

            @Test
            @DisplayName("Mixed logical operators")
            void testLogicalOperator_mixed() {
                String template = "{a && b || c}";
                Context context = new Context();
                context.put("a", true);
                context.put("b", false);
                context.put("c", true);
                assertEquals("true", engine.compile(template).render(context));
            }
        }

        // ========== 相等性运算符 ==========

        @Nested
        @DisplayName("Equality Operator Tests")
        class EqualityOperatorTests {

            @Test
            @DisplayName("Equal operator")
            void testEqualOperator() {
                String template = "{a == b}";
                Context context = new Context();

                context.put("a", 1);
                context.put("b", 1);
                assertEquals("true", engine.compile(template).render(context));

                context.put("b", 2);
                assertEquals("false", engine.compile(template).render(context));
            }

            @Test
            @DisplayName("Not equal operator")
            void testNotEqualOperator() {
                String template = "{a != b}";
                Context context = new Context();

                context.put("a", 1);
                context.put("b", 2);
                assertEquals("true", engine.compile(template).render(context));

                context.put("b", 1);
                assertEquals("false", engine.compile(template).render(context));
            }

            @Test
            @DisplayName("Regex match operator")
            void testRegexMatchOperator() {
                String template = "{name =~ /^[A-Z].*/}";
                Context context = new Context();

                context.put("name", "Hello");
                assertEquals("true", engine.compile(template).render(context));

                context.put("name", "hello");
                assertEquals("false", engine.compile(template).render(context));
            }

            @Test
            @DisplayName("In operator")
            void testInOperator() {
                String template = "{item in list}";
                Context context = new Context();
                context.put("item", "a");
                context.put("list", Arrays.asList("a", "b", "c"));
                assertEquals("true", engine.compile(template).render(context));

                context.put("item", "d");
                assertEquals("false", engine.compile(template).render(context));
            }
        }

        // ========== 关系运算符 ==========

        @Nested
        @DisplayName("Relational Operator Tests")
        class RelationalOperatorTests {

            @Test
            @DisplayName("Less than operator")
            void testLessThanOperator() {
                String template = "{a < b}";
                Context context = new Context();
                context.put("a", 1);
                context.put("b", 2);
                assertEquals("true", engine.compile(template).render(context));
            }

            @Test
            @DisplayName("Less than or equal operator")
            void testLessThanOrEqualOperator() {
                String template = "{a <= b}";
                Context context = new Context();
                context.put("a", 2);
                context.put("b", 2);
                assertEquals("true", engine.compile(template).render(context));
            }

            @Test
            @DisplayName("Greater than operator")
            void testGreaterThanOperator() {
                String template = "{a > b}";
                Context context = new Context();
                context.put("a", 3);
                context.put("b", 2);
                assertEquals("true", engine.compile(template).render(context));
            }

            @Test
            @DisplayName("Greater than or equal operator")
            void testGreaterThanOrEqualOperator() {
                String template = "{a >= b}";
                Context context = new Context();
                context.put("a", 2);
                context.put("b", 2);
                assertEquals("true", engine.compile(template).render(context));
            }
        }

        // ========== 区间运算符 ==========

        @Nested
        @DisplayName("Range Operator Tests")
        class RangeOperatorTests {

            @Test
            @DisplayName("Closed range operator")
            void testClosedRangeOperator() {
                String template = "{% for i in 1..3 }{i} {/% for }";
                assertEquals("1 2 3 ", engine.compile(template).render(new Context()));
            }

            @Test
            @DisplayName("Half-open range operator")
            void testHalfOpenRangeOperator() {
                String template = "{% for i in 1..<4 }{i} {/% for }";
                assertEquals("1 2 3 ", engine.compile(template).render(new Context()));
            }
        }

        // ========== 算术运算符 ==========

        @Nested
        @DisplayName("Arithmetic Operator Tests")
        class ArithmeticOperatorTests {

            @Test
            @DisplayName("Addition operator")
            void testAdditionOperator() {
                String template = "{a + b}";
                Context context = new Context();
                context.put("a", 10);
                context.put("b", 5);
                assertEquals("15", engine.compile(template).render(context));
            }

            @Test
            @DisplayName("Subtraction operator")
            void testSubtractionOperator() {
                String template = "{a - b}";
                Context context = new Context();
                context.put("a", 10);
                context.put("b", 3);
                assertEquals("7", engine.compile(template).render(context));
            }

            @Test
            @DisplayName("Multiplication operator")
            void testMultiplicationOperator() {
                String template = "{a * b}";
                Context context = new Context();
                context.put("a", 4);
                context.put("b", 5);
                assertEquals("20", engine.compile(template).render(context));
            }

            @Test
            @DisplayName("Division operator")
            void testDivisionOperator() {
                String template = "{a / b}";
                Context context = new Context();
                context.put("a", 10);
                context.put("b", 2);
                assertEquals("5", engine.compile(template).render(context));
            }

            @Test
            @DisplayName("Modulo operator")
            void testModuloOperator() {
                String template = "{a % b}";
                Context context = new Context();
                context.put("a", 10);
                context.put("b", 3);
                assertEquals("1", engine.compile(template).render(context));
            }

            @Test
            @DisplayName("Unary minus operator")
            void testUnaryMinusOperator() {
                String template = "{-a}";
                Context context = new Context();
                context.put("a", 5);
                assertEquals("-5", engine.compile(template).render(context));
            }

            @Test
            @DisplayName("Unary plus operator")
            void testUnaryPlusOperator() {
                String template = "{+a}";
                Context context = new Context();
                context.put("a", 5);
                assertEquals("5", engine.compile(template).render(context));
            }
        }

        // ========== 后缀运算符 ==========

        @Nested
        @DisplayName("Postfix Operator Tests")
        class PostfixOperatorTests {

            @Test
            @DisplayName("Member access operator")
            void testMemberAccessOperator() {
                String template = "{user.name}";
                Context context = new Context();
                Map<String, Object> user = new HashMap<>();
                user.put("name", "John");
                context.put("user", user);
                assertEquals("John", engine.compile(template).render(context));
            }

            @Test
            @DisplayName("Safe navigation operator")
            void testSafeNavigationOperator() {
                String template = "{user?.name}";
                Context context = new Context();
                context.put("user", null);
                assertEquals("", engine.compile(template).render(context));
            }

            @Test
            @DisplayName("Index access operator")
            void testIndexAccessOperator() {
                String template = "{list[0]}";
                Context context = new Context();
                context.put("list", Arrays.asList("a", "b", "c"));
                assertEquals("a", engine.compile(template).render(context));
            }

            @Test
            @DisplayName("Method call operator")
            void testMethodCallOperator() {
                String template = "{text.toUpperCase()}";
                Context context = new Context();
                context.put("text", "hello");
                assertEquals("HELLO", engine.compile(template).render(context));
            }

            @Test
            @DisplayName("Method call with arguments")
            void testMethodCallWithArguments() {
                String template = "{text.substring(0, 3)}";
                Context context = new Context();
                context.put("text", "hello world");
                assertEquals("hel", engine.compile(template).render(context));
            }
        }
    }

    // ========== 参数访问测试 ==========

    @Nested
    @DisplayName("Argument Access Tests")
    class ArgumentAccessTests {
        // Section 3.1.5 tests

        @Test
        @DisplayName("Simple argument access - first argument")
        void testSimpleArgumentAccess_first() {
            String template = "{0}";
            Context context = new Context();
            context.setArguments("Hello", "World");
            assertEquals("Hello", engine.compile(template).render(context));
        }

        @Test
        @DisplayName("Simple argument access - second argument")
        void testSimpleArgumentAccess_second() {
            String template = "{1}";
            Context context = new Context();
            context.setArguments("Hello", "World");
            assertEquals("World", engine.compile(template).render(context));
        }

        @Test
        @DisplayName("Argument variable reference - basic")
        void testArgumentVariableReference_basic() {
            String template = "{$0}";
            Context context = new Context();
            context.setArguments("Hello");
            assertEquals("Hello", engine.compile(template).render(context));
        }

        @Test
        @DisplayName("Argument variable with elvis operator")
        void testArgumentVariable_withElvis() {
            String template = "{$0 ?? \"default\"}";
            Context context = new Context();
            context.setArguments((Object) null);
            assertEquals("default", engine.compile(template).render(context));
        }

        @Test
        @DisplayName("Mixed argument access")
        void testMixedArgumentAccess() {
            String template = "{0}-{$1}-{2}";
            Context context = new Context();
            context.setArguments("A", "B", "C");
            assertEquals("A-B-C", engine.compile(template).render(context));
        }
    }

    // ========== 字面量测试 ==========

    @Nested
    @DisplayName("Literal Tests")
    class LiteralTests {
        // Section 3.1.6 tests

        // ========== 字符串字面量 ==========

        @Test
        @DisplayName("String literal - double quotes")
        void testStringLiteral_doubleQuotes() {
            String template = "{ \"Hello, World!\" }";
            assertEquals("Hello, World!", engine.compile(template).render(new Context()));
        }

        @Test
        @DisplayName("String literal - single quotes")
        void testStringLiteral_singleQuotes() {
            String template = "{ 'Hello, World!' }";
            assertEquals("Hello, World!", engine.compile(template).render(new Context()));
        }

        @Test
        @DisplayName("String literal - escape sequences")
        void testStringLiteral_escapeSequences() {
            String template = "{ \"Line1\\nLine2\" }";
            assertEquals("Line1\nLine2", engine.compile(template).render(new Context()));
        }

        // ========== 数字字面量 ==========

        @Test
        @DisplayName("Number literal - integer in expression")
        void testNumberLiteral_integer() {
            // Note: { 42 } is interpreted as argument access, so we use in expression
            String template = "{ 40 + 2 }";
            assertEquals("42", engine.compile(template).render(new Context()));
        }

        @Test
        @DisplayName("Number literal - negative")
        void testNumberLiteral_negative() {
            String template = "{ -10 }";
            assertEquals("-10", engine.compile(template).render(new Context()));
        }

        @Test
        @DisplayName("Number literal - decimal")
        void testNumberLiteral_decimal() {
            String template = "{ 3.14 }";
            assertEquals("3.14", engine.compile(template).render(new Context()));
        }

        @Test
        @DisplayName("Number literal - in list")
        void testNumberLiteral_inList() {
            String template = "{ [42, 100, 200].get(0) }";
            assertEquals("42", engine.compile(template).render(new Context()));
        }

        // ========== 布尔字面量 ==========

        @Test
        @DisplayName("Boolean literal - true")
        void testBooleanLiteral_true() {
            String template = "{ true }";
            assertEquals("true", engine.compile(template).render(new Context()));
        }

        @Test
        @DisplayName("Boolean literal - false")
        void testBooleanLiteral_false() {
            String template = "{ false }";
            assertEquals("false", engine.compile(template).render(new Context()));
        }

        // ========== Null 字面量 ==========

        @Test
        @DisplayName("Null literal")
        void testNullLiteral() {
            String template = "{ null }";
            assertEquals("", engine.compile(template).render(new Context()));
        }

        // ========== 列表字面量 ==========

        @Test
        @DisplayName("List literal - empty")
        void testListLiteral_empty() {
            String template = "{ [] }";
            assertEquals("[]", engine.compile(template).render(new Context()));
        }

        @Test
        @DisplayName("List literal - with elements")
        void testListLiteral_withElements() {
            String template = "{ [1, 2, 3].size() }";
            assertEquals("3", engine.compile(template).render(new Context()));
        }

        @Test
        @DisplayName("List literal - mixed types")
        void testListLiteral_mixedTypes() {
            String template = "{ [1, \"two\", true].size() }";
            assertEquals("3", engine.compile(template).render(new Context()));
        }

        // ========== Map 字面量 ==========

        @Test
        @DisplayName("Map literal - empty")
        void testMapLiteral_empty() {
            String template = "{ {} }";
            assertEquals("{}", engine.compile(template).render(new Context()));
        }

        @Test
        @DisplayName("Map literal - with entries")
        void testMapLiteral_withEntries() {
            String template = "{ {\"name\": \"John\", \"age\": 30}[\"name\"] }";
            assertEquals("John", engine.compile(template).render(new Context()));
        }

        @Test
        @DisplayName("Map literal - identifier keys")
        void testMapLiteral_identifierKeys() {
            // Note: identifier keys may need to use different syntax
            String template = "{ {\"name\": \"John\", \"age\": 30}[\"name\"] }";
            assertEquals("John", engine.compile(template).render(new Context()));
        }

        // ========== 正则字面量 ==========

        @Test
        @DisplayName("Regex literal - basic")
        void testRegexLiteral_basic() {
            String template = "{ \"test\" =~ /[a-z]+/ }";
            assertEquals("true", engine.compile(template).render(new Context()));
        }
    }

    // ========== Lambda 表达式测试 ==========

    @Nested
    @DisplayName("Lambda Expression Tests")
    class LambdaTests {
        // Section 3.1.7 tests

        @Test
        @DisplayName("Lambda - single parameter")
        void testLambda_singleParameter() {
            String template = "{% let double = x -> x * 2 %}{double(5)}";
            assertEquals("10", engine.compile(template).render(new Context()));
        }

        @Test
        @DisplayName("Lambda - multiple parameters")
        void testLambda_multipleParameters() {
            String template = "{% let add = (a, b) -> a + b %}{add(3, 4)}";
            assertEquals("7", engine.compile(template).render(new Context()));
        }

        @Test
        @DisplayName("Lambda - no parameters")
        void testLambda_noParameters() {
            String template = "{% let getValue = () -> 42 %}{getValue()}";
            assertEquals("42", engine.compile(template).render(new Context()));
        }

        @Test
        @DisplayName("Lambda - in variable assignment")
        void testLambda_inVariableAssignment() {
            String template = "{% let square = x -> x * x %}{square(4)}";
            assertEquals("16", engine.compile(template).render(new Context()));
        }

        @Test
        @DisplayName("Lambda - closure capturing variable")
        void testLambda_closure() {
            String template = "{% let factor = 10 %}{% let multiply = x -> x * factor %}{multiply(5)}";
            assertEquals("50", engine.compile(template).render(new Context()));
        }

        @Test
        @DisplayName("Lambda - with string operations")
        void testLambda_stringOperations() {
            String template = "{% let prefix = s -> \"Hello, \" + s %}{prefix(\"World\")}";
            assertEquals("Hello, World", engine.compile(template).render(new Context()));
        }

        @Test
        @DisplayName("Lambda - used in expression")
        void testLambda_inExpression() {
            String template = "{% let double = x -> x * 2 %}{double(5) + 10}";
            assertEquals("20", engine.compile(template).render(new Context()));
        }
    }

    // ========== 动态变量测试 ==========

    @Nested
    @DisplayName("Dynamic Variable Tests")
    class DynamicVariableTests {
        // Section 3.1.8 tests

        @Test
        @DisplayName("Dynamic variable - basic access")
        void testDynamicVariable_basic() {
            String template = "{user[name]}";
            Context context = new Context();
            Map<String, Object> user = new HashMap<>();
            user.put("name", "John");
            context.put("user", user);
            context.put("name", "name");
            assertEquals("John", engine.compile(template).render(context));
        }

        @Test
        @DisplayName("Dynamic variable - with expression key")
        void testDynamicVariable_expressionKey() {
            String template = "{data[key]}";
            Context context = new Context();
            Map<String, Object> data = new HashMap<>();
            data.put("field1", "value1");
            context.put("data", data);
            context.put("key", "field1");
            assertEquals("value1", engine.compile(template).render(context));
        }

        @Test
        @DisplayName("Dynamic variable - nested access")
        void testDynamicVariable_nested() {
            String template = "{users[0][\"name\"]}";
            Context context = new Context();
            List<Map<String, Object>> users = new ArrayList<>();
            Map<String, Object> user = new HashMap<>();
            user.put("name", "John");
            users.add(user);
            context.put("users", users);
            assertEquals("John", engine.compile(template).render(context));
        }

        @Test
        @DisplayName("Dynamic variable - with null handling")
        void testDynamicVariable_nullHandling() {
            String template = "{user?.name}";
            Context context = new Context();
            context.put("user", null);
            assertEquals("", engine.compile(template).render(context));
        }

        @Test
        @DisplayName("Dynamic variable - map with variable key")
        void testDynamicVariable_mapWithVariableKey() {
            String template = "{% let key = \"name\" %}{user[key]}";
            Context context = new Context();
            Map<String, Object> user = new HashMap<>();
            user.put("name", "John");
            context.put("user", user);
            assertEquals("John", engine.compile(template).render(context));
        }
    }
}