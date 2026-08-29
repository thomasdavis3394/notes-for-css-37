package org.dromara.easytl;

import org.dromara.easytl.exception.TemplateCompileException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-end tests for increment/decrement, compound assignment, C-style for loop
 */
class IncrementDecrementTest {

    /**
     * Simple bean with a public count field for testing member access increment/decrement.
     */
    public static class Counter {
        public int count;

        public Counter(int count) {
            this.count = count;
        }
    }

    private TemplateEngine engine;

    @BeforeEach
    void setUp() {
        engine = new TemplateEngine();
    }

    @Nested
    class IncrementDecrementTests {

        @Test
        void postfixIncrement_returnsOldValue() {
            Context ctx = new Context();
            ctx.put("i", 5);
            Template t = engine.compile("{i++}");
            assertEquals("5", t.render(ctx));
        }

        @Test
        void prefixIncrement_returnsNewValue() {
            Context ctx = new Context();
            ctx.put("i", 5);
            Template t = engine.compile("{++i}");
            assertEquals("6", t.render(ctx));
        }

        @Test
        void postfixDecrement_returnsOldValue() {
            Context ctx = new Context();
            ctx.put("i", 5);
            Template t = engine.compile("{i--}");
            assertEquals("5", t.render(ctx));
        }

        @Test
        void prefixDecrement_returnsNewValue() {
            Context ctx = new Context();
            ctx.put("i", 5);
            Template t = engine.compile("{--i}");
            assertEquals("4", t.render(ctx));
        }

        @Test
        void postfixIncrementInExpression() {
            Context ctx = new Context();
            ctx.put("i", 1);
            Template t = engine.compile("{i++ * 2}");
            assertEquals("2", t.render(ctx));
        }

        @Test
        void prefixIncrementInExpression() {
            Context ctx = new Context();
            ctx.put("i", 1);
            Template t = engine.compile("{++i * 2}");
            assertEquals("4", t.render(ctx));
        }

        @Test
        void memberAccessPostfixIncrement() {
            Context ctx = new Context();
            ctx.put("obj", new Counter(0));
            Template t = engine.compile("{obj.count++}");
            assertEquals("0", t.render(ctx));
        }

        @Test
        void memberAccessPrefixIncrement() {
            Context ctx = new Context();
            ctx.put("obj", new Counter(0));
            Template t = engine.compile("{++obj.count}");
            assertEquals("1", t.render(ctx));
        }

        @Test
        void indexAccessPostfixIncrement() {
            Context ctx = new Context();
            List<Integer> arr = new ArrayList<>(Arrays.asList(10, 20, 30));
            ctx.put("arr", arr);
            ctx.put("i", 1);
            Template t = engine.compile("{arr[i]++}");
            assertEquals("20", t.render(ctx));
        }

        @Test
        void indexAccessPrefixIncrement() {
            Context ctx = new Context();
            List<Integer> arr = new ArrayList<>(Arrays.asList(10, 20, 30));
            ctx.put("arr", arr);
            ctx.put("i", 1);
            Template t = engine.compile("{++arr[i]}");
            assertEquals("21", t.render(ctx));
        }

        @Test
        void invalidTarget_throwsCompileError() {
            assertThrows(TemplateCompileException.class, () -> engine.compile("{++5}"));
            assertThrows(TemplateCompileException.class, () -> engine.compile("{\"hello\"++}"));
        }

        @Test
        void prefixIncrementMutatesVariable() {
            Context ctx = new Context();
            ctx.put("i", 5);
            Template t = engine.compile("{++i}");
            t.render(ctx);
            // Verify the variable was actually incremented in the context
            assertEquals(6, ((Number) ctx.get("i")).intValue());
        }

        @Test
        void postfixIncrementMutatesVariable() {
            Context ctx = new Context();
            ctx.put("i", 5);
            Template t = engine.compile("{i++}");
            t.render(ctx);
            assertEquals(6, ((Number) ctx.get("i")).intValue());
        }
    }

    @Nested
    class CompoundAssignmentTests {

        @Test
        void plusAssign() {
            Context ctx = new Context();
            ctx.put("total", 10);
            Template t = engine.compile("{total += 5}");
            assertEquals("15", t.render(ctx));
        }

        @Test
        void minusAssign() {
            Context ctx = new Context();
            ctx.put("x", 10);
            Template t = engine.compile("{x -= 3}");
            assertEquals("7", t.render(ctx));
        }

        @Test
        void multiplyAssign() {
            Context ctx = new Context();
            ctx.put("x", 3);
            Template t = engine.compile("{x *= 2}");
            assertEquals("6", t.render(ctx));
        }

        @Test
        void divideAssign() {
            Context ctx = new Context();
            ctx.put("x", 10);
            Template t = engine.compile("{x /= 2}");
            assertEquals("5", t.render(ctx));
        }

        @Test
        void moduloAssign() {
            Context ctx = new Context();
            ctx.put("x", 10);
            Template t = engine.compile("{x %= 3}");
            assertEquals("1", t.render(ctx));
        }

        @Test
        void plusAssignStringConcat() {
            Context ctx = new Context();
            ctx.put("msg", "hello");
            Template t = engine.compile("{msg += ' world'}");
            assertEquals("hello world", t.render(ctx));
        }

        @Test
        void compoundAssignmentPrecedence() {
            Context ctx = new Context();
            ctx.put("x", 1);
            Template t = engine.compile("{x += 2 + 3}");
            assertEquals("6", t.render(ctx));
        }

        @Test
        void compoundAssignmentInvalidTarget() {
            assertThrows(TemplateCompileException.class, () -> engine.compile("{5 += 1}"));
        }

        @Test
        void bitAndAssign() {
            Context ctx = new Context();
            ctx.put("x", 15);
            Template t = engine.compile("{x &= 6}");
            assertEquals("6", t.render(ctx));
        }

        @Test
        void bitOrAssign() {
            Context ctx = new Context();
            ctx.put("x", 5);
            Template t = engine.compile("{x |= 2}");
            assertEquals("7", t.render(ctx));
        }

        @Test
        void bitXorAssign() {
            Context ctx = new Context();
            ctx.put("x", 5);
            Template t = engine.compile("{x ^= 3}");
            assertEquals("6", t.render(ctx));
        }

        @Test
        void leftShiftAssign() {
            Context ctx = new Context();
            ctx.put("x", 1);
            Template t = engine.compile("{x <<= 4}");
            assertEquals("16", t.render(ctx));
        }

        @Test
        void rightShiftAssign() {
            Context ctx = new Context();
            ctx.put("x", 16);
            Template t = engine.compile("{x >>= 2}");
            assertEquals("4", t.render(ctx));
        }
    }

    @Nested
    class ClassicForLoopTests {

        @Test
        void basicLoop() {
            Template t = engine.compile("{% for let i = 0; i < 5; i++ }{i}{/% for}");
            assertEquals("01234", t.render(new Context()));
        }

        @Test
        void loopWithStep() {
            Template t = engine.compile("{% for let i = 0; i < 10; i += 2 }{i}{/% for}");
            assertEquals("02468", t.render(new Context()));
        }

        @Test
        void nestedLoop() {
            Template t = engine.compile(
                "{% for let i = 0; i < 3; i++ }{% for let j = 0; j < 3; j++ }({i},{j}){/% for}{/% for}");
            assertEquals(
                "(0,0)(0,1)(0,2)(1,0)(1,1)(1,2)(2,0)(2,1)(2,2)",
                t.render(new Context()));
        }

        @Test
        void loopScope_variableWorksInsideLoop() {
            Template t = engine.compile("{% for let i = 0; i < 1; i++ }inner{/% for}");
            String result = t.render(new Context());
            assertTrue(result.contains("inner"));
        }

        @Test
        void decrementLoop() {
            Template t = engine.compile("{% for let i = 5; i > 0; i-- }{i}{/% for}");
            assertEquals("54321", t.render(new Context()));
        }

        @Test
        void loopWithMultiplyStep() {
            Template t = engine.compile("{% for let i = 1; i <= 16; i *= 2 }{i}{/% for}");
            assertEquals("124816", t.render(new Context()));
        }

        @Test
        void loopWithBodyExpressions() {
            Template t = engine.compile(
                "{% for let i = 1; i <= 3; i++ }[{i}]{/% for}");
            assertEquals("[1][2][3]", t.render(new Context()));
        }

        @Test
        void loopScope_variableNotVisibleAfterLoop() {
            // After the loop, the variable i should not be accessible
            // The loop creates a child context, so i is not in the parent
            Template t = engine.compile("{% for let i = 0; i < 3; i++ }{i}{/% for}");
            Context ctx = new Context();
            String result = t.render(ctx);
            assertEquals("012", result);
            // i should not exist in the original context
            assertNull(ctx.get("i"));
        }

        @Test
        void loopBodyWithExpressions() {
            Context ctx = new Context();
            ctx.put("items", java.util.Arrays.asList("a", "b", "c"));
            Template t = engine.compile("{% for let i = 0; i < 3; i++ }{items[i]}{/% for}");
            assertEquals("abc", t.render(ctx));
        }
    }

    @Nested
    class RegressionTests {

        @Test
        void existingForInLoop_stillWorks() {
            Context ctx = new Context();
            ctx.put("items", Arrays.asList(1, 2, 3));
            Template t = engine.compile("{% for item in items }{item}{/% for}");
            assertEquals("123", t.render(ctx));
        }

        @Test
        void existingForInWithIndex_stillWorks() {
            Context ctx = new Context();
            ctx.put("items", Arrays.asList("a", "b"));
            Template t = engine.compile("{% for idx, item in items }{idx}:{item}{/% for}");
            assertEquals("0:a1:b", t.render(ctx));
        }

        @Test
        void existingLetStatement_stillWorks() {
            Template t = engine.compile("{% let x = 10 %}{x}");
            assertEquals("10", t.render(new Context()));
        }

        @Test
        void existingArithmetic_stillWorks() {
            Context ctx = new Context();
            ctx.put("a", 5);
            ctx.put("b", 3);
            Template t = engine.compile("{a + b}");
            assertEquals("8", t.render(ctx));
        }
    }
}
