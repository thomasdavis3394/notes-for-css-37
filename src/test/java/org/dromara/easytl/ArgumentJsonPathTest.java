package org.dromara.easytl;

import org.dromara.easytl.exception.ArgumentIndexOutOfBoundsException;
import org.dromara.easytl.exception.TemplateRuntimeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * $ 参数（$0/$1 及裸 $）与 JSONPath 语法组合测试
 * <p>
 * 覆盖参数作为 JSONPath 根节点（通配符/切片/过滤/递归下降）、裸 $ 的 JSONPath 访问、
 * 参数参与过滤谓词、集合操作符与参数的组合、与 for/Elvis/安全导航/三元的组合
 * 以及参数越界、对标量参数应用 JSONPath 等错误场景。
 * </p>
 *
 * @author EasyTL Team
 */
@DisplayName("Argument JSONPath Combination Tests")
class ArgumentJsonPathTest {

    private TemplateEngine engine;

    @BeforeEach
    void setUp() {
        engine = new TemplateEngine();
        EasyTL.defaultEngine().clearCache();
    }

    // ==================== 测试数据 ====================

    /**
     * 经典 bookstore（与 JsonPathSyntaxTest 相同结构），通过参数而非上下文变量传入：
     * 4 本书（前 2 本无 isbn，前 3 本有 sizes）+ bicycle
     */
    private Context storeArgumentContext(Object... extraArgs) {
        Context context = new Context();
        Object[] args = new Object[extraArgs.length + 1];
        args[0] = bookstoreStore();
        System.arraycopy(extraArgs, 0, args, 1, extraArgs.length);
        context.setArguments(args);
        return context;
    }

    /** store 放上下文变量、额外数据走参数，用于“参数参与过滤谓词”场景 */
    private Context storeContextWithArguments(Object... args) {
        Context context = new Context();
        context.put("store", bookstoreStore());
        context.setArguments(args);
        return context;
    }

    private Map<String, Object> bookstoreStore() {
        List<Object> books = new ArrayList<>();
        books.add(book("reference", "Nigel Rees", "Sayings of the Century", 8.95, null, Arrays.asList("S")));
        books.add(book("fiction", "Evelyn Waugh", "Sword of Honour", 12.99, null, Arrays.asList("S", "M")));
        books.add(book("fiction", "Herman Melville", "Moby Dick", 8.99, "0-553-21311-3", Arrays.asList("L")));
        books.add(book("fiction", "J. R. R. Tolkien", "The Lord of the Rings", 22.99, "0-395-19395-8", null));

        Map<String, Object> bicycle = new LinkedHashMap<>();
        bicycle.put("color", "red");
        bicycle.put("price", 19.95);

        // LinkedHashMap 保证 store.* 的子节点顺序确定
        Map<String, Object> store = new LinkedHashMap<>();
        store.put("book", books);
        store.put("bicycle", bicycle);
        return store;
    }

    private Map<String, Object> book(String category, String author, String title,
                                     double price, String isbn, List<String> sizes) {
        Map<String, Object> book = new LinkedHashMap<>();
        book.put("category", category);
        book.put("author", author);
        book.put("title", title);
        if (isbn != null) {
            book.put("isbn", isbn);
        }
        book.put("price", price);
        if (sizes != null) {
            book.put("sizes", sizes);
        }
        return book;
    }

    // ========== 参数作为 JSONPath 根节点 ==========

    @Nested
    @DisplayName("Argument As JSONPath Root Tests")
    class ArgumentAsJsonPathRootTests {

        @Test
        @DisplayName("$0.book[*].author projects authors of all books")
        void testWildcardProjectionOnArgument() {
            assertEquals("[Nigel Rees, Evelyn Waugh, Herman Melville, J. R. R. Tolkien]",
                engine.compile("{$0.book[*].author}").render(storeArgumentContext()));
        }

        @Test
        @DisplayName("$0.*[1].color dot wildcard on argument")
        void testDotWildcardOnArgument() {
            assertEquals("red",
                engine.compile("{$0.*[1].color}").render(storeArgumentContext()));
        }

        @Test
        @DisplayName("$0.book[1:4] slice on argument")
        void testSliceOnArgument() {
            assertEquals("3",
                engine.compile("{$0.book[1:4].size()}").render(storeArgumentContext()));
            assertEquals("Moby Dick",
                engine.compile("{$0.book[-2:][0].title}").render(storeArgumentContext()));
        }

        @Test
        @DisplayName("$0.book[?(@.price < 10)].title filter on argument")
        void testFilterOnArgument() {
            assertEquals("[Sayings of the Century, Moby Dick]",
                engine.compile("{$0.book[?(@.price < 10)].title}").render(storeArgumentContext()));
        }

        @Test
        @DisplayName("$0.book[?(@.isbn)].title existence filter on argument")
        void testExistenceFilterOnArgument() {
            assertEquals("[Moby Dick, The Lord of the Rings]",
                engine.compile("{$0.book[?(@.isbn)].title}").render(storeArgumentContext()));
        }

        @Test
        @DisplayName("$0.book..price recursive descent on argument")
        void testRecursiveDescentOnArgument() {
            assertEquals("[8.95, 12.99, 8.99, 22.99]",
                engine.compile("{$0.book..price}").render(storeArgumentContext()));
        }

        @Test
        @DisplayName("$1 as JSONPath root (second argument)")
        void testSecondArgumentAsRoot() {
            assertEquals("[Sayings of the Century, Sword of Honour, Moby Dick, The Lord of the Rings]",
                engine.compile("{$1.book[*].title}").render(storeContextWithArguments(10, bookstoreStore())));
        }

        @Test
        @DisplayName("$0.book[$1].title argument as dynamic index")
        void testArgumentAsDynamicIndex() {
            assertEquals("Moby Dick",
                engine.compile("{$0.book[$1].title}").render(storeArgumentContext(2)));
        }

        @Test
        @DisplayName("$0.book[$1:] argument as slice bound")
        void testArgumentAsSliceBound() {
            assertEquals("2",
                engine.compile("{$0.book[$1:].size()}").render(storeArgumentContext(2)));
        }
    }

    // ========== 裸 $ 与 JSONPath 组合 ==========

    @Nested
    @DisplayName("Bare Dollar JSONPath Tests")
    class BareDollarJsonPathTests {

        @Test
        @DisplayName("$.book[*].title bare $ as JSONPath root")
        void testBareDollarWildcard() {
            assertEquals("[Sayings of the Century, Sword of Honour, Moby Dick, The Lord of the Rings]",
                engine.compile("{$.book[*].title}").render(storeArgumentContext()));
        }

        @Test
        @DisplayName("$.book[?(@.price > 20)].title bare $ with filter")
        void testBareDollarFilter() {
            assertEquals("[The Lord of the Rings]",
                engine.compile("{$.book[?(@.price > 20)].title}").render(storeArgumentContext()));
        }

        @Test
        @DisplayName("$.book[:2] bare $ with slice")
        void testBareDollarSlice() {
            assertEquals("2",
                engine.compile("{$.book[:2].size()}").render(storeArgumentContext()));
        }

        @Test
        @DisplayName("bare $ overridden by context variable still supports JSONPath")
        void testBareDollarOverriddenByVariable() {
            Map<String, Object> smallStore = new LinkedHashMap<>();
            smallStore.put("book", Arrays.asList(book("fiction", "A", "Only Book", 1.0, null, null)));

            Context context = new Context();
            context.setArguments(bookstoreStore());
            context.put("$", smallStore);

            assertEquals("Only Book",
                engine.compile("{$.book[0].title}").render(context));
        }

        @Test
        @DisplayName("${$.book[*].author} bare $ JSONPath in ${} wrapper")
        void testBareDollarInDollarBraceWrapper() {
            assertEquals("[Nigel Rees, Evelyn Waugh, Herman Melville, J. R. R. Tolkien]",
                engine.compile("${$.book[*].author}").render(storeArgumentContext()));
        }

        @Test
        @DisplayName("{{$0.book..price}} argument JSONPath in {{}} wrapper")
        void testArgumentInDoubleBraceWrapper() {
            assertEquals("[8.95, 12.99, 8.99, 22.99]",
                engine.compile("{{$0.book..price}}").render(storeArgumentContext()));
        }
    }

    // ========== 参数参与过滤谓词 ==========

    @Nested
    @DisplayName("Argument In Filter Predicate Tests")
    class ArgumentInFilterPredicateTests {

        @Test
        @DisplayName("[?(@.price < $0)] argument as comparison threshold")
        void testThresholdArgument() {
            assertEquals("[Sayings of the Century, Moby Dick]",
                engine.compile("{store.book[?(@.price < $0)].title}").render(storeContextWithArguments(10)));
        }

        @Test
        @DisplayName("[?(@.category == $1)] argument as equality operand")
        void testEqualityArgument() {
            assertEquals("[Sayings of the Century]",
                engine.compile("{store.book[?(@.category == $1)].title}")
                    .render(storeContextWithArguments(10, "reference")));
        }

        @Test
        @DisplayName("[?(@.sizes anyof $0)] argument as set operand in filter")
        void testAnyofArgumentInFilter() {
            assertEquals("[Sword of Honour]",
                engine.compile("{store.book[?(@.sizes anyof $0)].title}")
                    .render(storeContextWithArguments(Arrays.asList("M"))));
        }

        @Test
        @DisplayName("[?(@.price < $.max)] bare $ member access in predicate")
        void testBareDollarMemberInPredicate() {
            Map<String, Object> limit = new LinkedHashMap<>();
            limit.put("max", 10);
            assertEquals("[Sayings of the Century, Moby Dick]",
                engine.compile("{store.book[?(@.price < $.max)].title}")
                    .render(storeContextWithArguments(limit)));
        }

        @Test
        @DisplayName("[?(@[$0] < 10)] argument as dynamic key of current node")
        void testArgumentAsCurrentNodeKey() {
            assertEquals("[Sayings of the Century, Moby Dick]",
                engine.compile("{store.book[?(@[$0] < 10)].title}")
                    .render(storeContextWithArguments("price")));
        }
    }

    // ========== 集合操作符与参数组合 ==========

    @Nested
    @DisplayName("Set Operator With Argument Tests")
    class SetOperatorWithArgumentTests {

        @Test
        @DisplayName("$0 subsetof [...] argument as left operand")
        void testArgumentAsLeftOperand() {
            assertEquals("true",
                engine.compile("{$0 subsetof ['S','M','L']}")
                    .render(storeContextWithArguments(Arrays.asList("S", "M"))));
            assertEquals("false",
                engine.compile("{$0 subsetof ['S','M','L']}")
                    .render(storeContextWithArguments(Arrays.asList("S", "X"))));
        }

        @Test
        @DisplayName("store.book[0].sizes subsetof $0 argument as right operand")
        void testArgumentAsRightOperand() {
            assertEquals("true",
                engine.compile("{store.book[0].sizes subsetof $0}")
                    .render(storeContextWithArguments(Arrays.asList("S", "M", "L"))));
        }

        @Test
        @DisplayName("$1 anyof $0 argument to argument")
        void testArgumentToArgument() {
            assertEquals("true",
                engine.compile("{$1 anyof $0}")
                    .render(storeContextWithArguments(Arrays.asList("M", "L"), Arrays.asList("M"))));
            assertEquals("false",
                engine.compile("{$1 anyof $0}")
                    .render(storeContextWithArguments(Arrays.asList("M", "L"), Arrays.asList("X"))));
        }

        @Test
        @DisplayName("scalar argument is treated as single-element set")
        void testScalarArgumentLeftOperand() {
            assertEquals("true",
                engine.compile("{$0 anyof ['M','L']}").render(storeContextWithArguments("M")));
            assertEquals("false",
                engine.compile("{$0 subsetof ['M','L']}").render(storeContextWithArguments("X")));
        }
    }

    // ========== 与 for / Elvis / 安全导航 / 三元组合 ==========

    @Nested
    @DisplayName("Control Flow Combination Tests")
    class ControlFlowCombinationTests {

        @Test
        @DisplayName("for iterates over filter result of bare $ argument")
        void testForOverArgumentFilter() {
            // 注意：{% %} 语句模式的词法不产出 DOLLAR_NUMBER，$0 会被读成普通标识符，
            // 语句内引用第一个参数需用裸 $（求值时回退为 $0）
            String template = "{% for b in $.book[?(@.price < 10)] }{b.title};{/% for }";
            assertEquals("Sayings of the Century;Moby Dick;",
                engine.compile(template).render(storeArgumentContext()));
        }

        @Test
        @DisplayName("Elvis applies to member of filtered argument element")
        void testElvisOnArgumentJsonPath() {
            assertEquals("full",
                engine.compile("{$0.book[?(@.isbn)][0].discount ?? 'full'}").render(storeArgumentContext()));
        }

        @Test
        @DisplayName("safe navigation on argument JSONPath results")
        void testSafeNavigationOnArgument() {
            assertEquals("[Sayings of the Century, Sword of Honour, Moby Dick, The Lord of the Rings]",
                engine.compile("{$0.book[*]?.title}").render(storeArgumentContext()));
        }

        @Test
        @DisplayName("ternary over argument filter result size")
        void testTernaryOnArgumentFilterSize() {
            assertEquals("many",
                engine.compile("{$0.book[?(@.price < 10)].size() > 1 ? 'many' : 'few'}")
                    .render(storeArgumentContext()));
        }
    }

    // ========== 错误场景 ==========

    @Nested
    @DisplayName("Error Case Tests")
    class ErrorCaseTests {

        @Test
        @DisplayName("$5.book[*] with insufficient arguments throws out of bounds")
        void testOutOfBoundsArgumentWithJsonPath() {
            ArgumentIndexOutOfBoundsException ex = assertThrows(ArgumentIndexOutOfBoundsException.class,
                () -> engine.compile("{$5.book[*].author}").render(storeArgumentContext()));
            assertTrue(ex.getMessage().contains("out of bounds"));
        }

        @Test
        @DisplayName("[*] on scalar argument throws runtime exception")
        void testWildcardOnScalarArgument() {
            TemplateRuntimeException ex = assertThrows(TemplateRuntimeException.class,
                () -> engine.compile("{$0[*]}").render(storeContextWithArguments("scalar")));
            assertTrue(ex.getMessage().contains("Cannot apply wildcard"));
        }

        @Test
        @DisplayName("[?(expr)] on scalar argument throws runtime exception")
        void testFilterOnScalarArgument() {
            TemplateRuntimeException ex = assertThrows(TemplateRuntimeException.class,
                () -> engine.compile("{$0[?(@ > 1)]}").render(storeContextWithArguments("scalar")));
            assertTrue(ex.getMessage().contains("Cannot apply filter"));
        }

        @Test
        @DisplayName("[start:end] on scalar argument throws runtime exception")
        void testSliceOnScalarArgument() {
            TemplateRuntimeException ex = assertThrows(TemplateRuntimeException.class,
                () -> engine.compile("{$0[1:2]}").render(storeContextWithArguments("scalar")));
            assertTrue(ex.getMessage().contains("Cannot apply slice"));
        }
    }
}
