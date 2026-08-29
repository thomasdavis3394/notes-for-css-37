package org.dromara.easytl;

import org.dromara.easytl.exception.TemplateRuntimeException;
import org.dromara.easytl.runtime.TemplateFunction;
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
 * RFC 9535 函数扩展与 TemplateFunction SPI 测试
 * <p>
 * 覆盖内置函数 length/count/match/search/value 的基础行为、
 * 在 JSONPath 过滤谓词内的组合使用，以及通过 Context/TemplateBuilder
 * 注册自定义函数、覆盖内置函数、作用域继承与遮蔽规则。
 * </p>
 *
 * @author EasyTL Team
 */
@DisplayName("JSONPath Function Extension Tests")
class JsonPathFunctionsTest {

    private TemplateEngine engine;

    @BeforeEach
    void setUp() {
        engine = new TemplateEngine();
        EasyTL.defaultEngine().clearCache();
    }

    // ==================== 测试数据 ====================

    private Context bookstoreContext() {
        List<Object> books = new ArrayList<>();
        books.add(book("reference", "Nigel Rees", "Sayings of the Century", 8.95, null, Arrays.asList("S")));
        books.add(book("fiction", "Evelyn Waugh", "Sword of Honour", 12.99, null, Arrays.asList("S", "M")));
        books.add(book("fiction", "Herman Melville", "Moby Dick", 8.99, "0-553-21311-3", Arrays.asList("L")));
        books.add(book("fiction", "J. R. R. Tolkien", "The Lord of the Rings", 22.99, "0-395-19395-8", null));

        Map<String, Object> bicycle = new LinkedHashMap<>();
        bicycle.put("color", "red");
        bicycle.put("price", 19.95);

        Map<String, Object> store = new LinkedHashMap<>();
        store.put("book", books);
        store.put("bicycle", bicycle);

        Context context = new Context();
        context.put("store", store);
        return context;
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

    // ========== length() ==========

    @Nested
    @DisplayName("length() Tests")
    class LengthTests {

        @Test
        @DisplayName("length of string / list / map / array")
        void testLengthBasics() {
            assertEquals("5", engine.compile("{length('hello')}").render(new Context()));
            assertEquals("4", engine.compile("{length(store.book)}").render(bookstoreContext()));
            assertEquals("2", engine.compile("{length(store)}").render(bookstoreContext()));
            Context context = new Context();
            context.put("arr", new int[]{1, 2, 3});
            assertEquals("3", engine.compile("{length(arr)}").render(context));
        }

        @Test
        @DisplayName("length of number or null throws runtime exception")
        void testLengthInvalidType() {
            Context context = new Context();
            context.put("num", 42);
            TemplateRuntimeException ex = assertThrows(TemplateRuntimeException.class,
                () -> engine.compile("{length(num)}").render(context));
            assertTrue(ex.getMessage().contains("length() expects"));
            assertThrows(TemplateRuntimeException.class,
                () -> engine.compile("{length(missing)}").render(new Context()));
        }

        @Test
        @DisplayName("length inside filter predicate")
        void testLengthInFilter() {
            assertEquals("[Sayings of the Century, The Lord of the Rings]",
                engine.compile("{store.book[?(length(@.title) > 15)].title}").render(bookstoreContext()));
        }
    }

    // ========== count() ==========

    @Nested
    @DisplayName("count() Tests")
    class CountTests {

        @Test
        @DisplayName("count of list / null / scalar / query result")
        void testCountBasics() {
            Context context = bookstoreContext();
            assertEquals("4", engine.compile("{count(store.book)}").render(context));
            assertEquals("0", engine.compile("{count(missing)}").render(new Context()));
            assertEquals("1", engine.compile("{count('x')}").render(new Context()));
            assertEquals("2", engine.compile("{count(store.book[?(@.price < 10)])}").render(bookstoreContext()));
        }

        @Test
        @DisplayName("count inside filter predicate")
        void testCountInFilter() {
            assertEquals("[Sword of Honour]",
                engine.compile("{store.book[?(count(@.sizes) > 1)].title}").render(bookstoreContext()));
        }
    }

    // ========== match() / search() ==========

    @Nested
    @DisplayName("match() / search() Tests")
    class RegexTests {

        @Test
        @DisplayName("match requires full-string match")
        void testMatchFull() {
            assertEquals("true", engine.compile("{match('abc123', '[a-z]+[0-9]+')}").render(new Context()));
            assertEquals("false", engine.compile("{match('abc123', '[a-z]+')}").render(new Context()));
        }

        @Test
        @DisplayName("match accepts regex literal with flags")
        void testMatchRegexLiteral() {
            assertEquals("true", engine.compile("{match('ABC', /^[a-z]+$/i)}").render(new Context()));
        }

        @Test
        @DisplayName("search finds substring match")
        void testSearch() {
            assertEquals("true", engine.compile("{search('abc123', '[0-9]+')}").render(new Context()));
            assertEquals("false", engine.compile("{search('abc', '[0-9]+')}").render(new Context()));
        }

        @Test
        @DisplayName("match / search inside filter predicates")
        void testRegexInFilter() {
            assertEquals("[Sayings of the Century]",
                engine.compile("{store.book[?(match(@.author, '.*Rees.*'))].title}").render(bookstoreContext()));
            assertEquals("[The Lord of the Rings]",
                engine.compile("{store.book[?(search(@.title, 'Lord'))].title}").render(bookstoreContext()));
        }

        @Test
        @DisplayName("match on non-string first argument throws runtime exception")
        void testMatchInvalidType() {
            Context context = new Context();
            context.put("num", 42);
            TemplateRuntimeException ex = assertThrows(TemplateRuntimeException.class,
                () -> engine.compile("{match(num, '[0-9]+')}").render(context));
            assertTrue(ex.getMessage().contains("match() expects"));
        }
    }

    // ========== value() ==========

    @Nested
    @DisplayName("value() Tests")
    class ValueTests {

        @Test
        @DisplayName("value of single-element query result")
        void testValueSingle() {
            assertEquals("Moby Dick",
                engine.compile("{value(store.book[?(@.isbn == '0-553-21311-3')]).title}")
                    .render(bookstoreContext()));
        }

        @Test
        @DisplayName("value of empty or multi-element result is null")
        void testValueEmptyOrMulti() {
            assertEquals("", engine.compile("{value(store.book[?(@.price > 100)])}")
                .render(bookstoreContext()));
            assertEquals("none", engine.compile("{value(store.book[*]) ?? 'none'}")
                .render(bookstoreContext()));
        }

        @Test
        @DisplayName("value combined with recursive descent inside filter")
        void testValueInFilter() {
            assertEquals("[The Lord of the Rings]",
                engine.compile("{store.book[?(value(@..isbn) == '0-395-19395-8')].title}")
                    .render(bookstoreContext()));
        }
    }

    // ========== TemplateFunction SPI ==========

    @Nested
    @DisplayName("Custom Function SPI Tests")
    class CustomFunctionTests {

        private TemplateFunction doubleFunction() {
            return new TemplateFunction() {
                @Override
                public String getName() {
                    return "double";
                }

                @Override
                public Object call(List<Object> args) {
                    return ((Number) args.get(0)).doubleValue() * 2;
                }
            };
        }

        @Test
        @DisplayName("custom function registered on Context")
        void testCustomFunction() {
            Context context = new Context();
            context.registerFunction(doubleFunction());
            assertEquals("42.0", engine.compile("{double(21)}").render(context));
        }

        @Test
        @DisplayName("custom function usable inside filter predicate")
        void testCustomFunctionInFilter() {
            Context context = bookstoreContext();
            context.registerFunction(new TemplateFunction() {
                @Override
                public String getName() {
                    return "isBig";
                }

                @Override
                public Object call(List<Object> args) {
                    return args.get(0) instanceof Number && ((Number) args.get(0)).doubleValue() > 10;
                }
            });
            assertEquals("[Sword of Honour, The Lord of the Rings]",
                engine.compile("{store.book[?(isBig(@.price))].title}").render(context));
        }

        @Test
        @DisplayName("registered function overrides builtin with the same name")
        void testOverrideBuiltin() {
            Context context = new Context();
            context.registerFunction(new TemplateFunction() {
                @Override
                public String getName() {
                    return "length";
                }

                @Override
                public Object call(List<Object> args) {
                    return 99;
                }
            });
            assertEquals("99", engine.compile("{length('hello')}").render(context));
        }

        @Test
        @DisplayName("context variable (lambda) shadows registered function")
        void testLambdaShadowsFunction() {
            assertEquals("custom",
                engine.compile("{% let length = (x) -> 'custom' %}{length('hello')}").render(new Context()));
        }

        @Test
        @DisplayName("functions are visible in for-loop child scopes")
        void testFunctionInChildScope() {
            Context context = bookstoreContext();
            context.registerFunction(doubleFunction());
            assertEquals("22;15;9;21;",
                engine.compile("{% for b in store.book %}{length(b.title)};{/% for }").render(context));
        }

        @Test
        @DisplayName("custom function via TemplateBuilder chain")
        void testTemplateBuilderFunction() {
            String result = EasyTL.template("{double(21)}")
                .function(doubleFunction())
                .render();
            assertEquals("42.0", result);
        }

        @Test
        @DisplayName("unknown function still throws the original error")
        void testUnknownFunction() {
            TemplateRuntimeException ex = assertThrows(TemplateRuntimeException.class,
                () -> engine.compile("{nosuchfn(1)}").render(new Context()));
            assertTrue(ex.getMessage().contains("Cannot call 'nosuchfn' as a function"));
        }
    }

    // ========== 边界与组合 ==========

    @Nested
    @DisplayName("Edge Case and Combination Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("length of empty string / wildcard NodeList result")
        void testLengthEdgeCases() {
            assertEquals("0", engine.compile("{length('')}").render(new Context()));
            assertEquals("4", engine.compile("{length(store.book[*].author)}").render(bookstoreContext()));
        }

        @Test
        @DisplayName("count of map / array / union result")
        void testCountEdgeCases() {
            Context context = bookstoreContext();
            assertEquals("2", engine.compile("{count(store.bicycle)}").render(context));
            assertEquals("2", engine.compile("{count(store.book[0, 2])}").render(context));
            Context arrContext = new Context();
            arrContext.put("arr", new String[]{"a", "b", "c"});
            assertEquals("3", engine.compile("{count(arr)}").render(arrContext));
        }

        @Test
        @DisplayName("match with invalid regex string throws runtime exception")
        void testMatchInvalidRegex() {
            TemplateRuntimeException ex = assertThrows(TemplateRuntimeException.class,
                () -> engine.compile("{match('abc', '[')}").render(new Context()));
            assertTrue(ex.getMessage().contains("match() got an invalid regex"));
        }

        @Test
        @DisplayName("match with non-string regex argument throws runtime exception")
        void testMatchNonStringRegex() {
            TemplateRuntimeException ex = assertThrows(TemplateRuntimeException.class,
                () -> engine.compile("{match('abc', 123)}").render(new Context()));
            assertTrue(ex.getMessage().contains("match() expects a string or regex"));
        }

        @Test
        @DisplayName("search accepts regex literal")
        void testSearchRegexLiteral() {
            assertEquals("true", engine.compile("{search('abc123', /[0-9]+/)}").render(new Context()));
            assertEquals("false", engine.compile("{search('abc', /[0-9]+/)}").render(new Context()));
        }

        @Test
        @DisplayName("value passes scalar through and rejects map")
        void testValueEdgeCases() {
            assertEquals("x", engine.compile("{value('x')}").render(new Context()));
            Context context = new Context();
            context.put("arr", new String[]{"only"});
            assertEquals("only", engine.compile("{value(arr)}").render(context));
            TemplateRuntimeException ex = assertThrows(TemplateRuntimeException.class,
                () -> engine.compile("{value(store)}").render(bookstoreContext()));
            assertTrue(ex.getMessage().contains("value() does not support map"));
        }

        @Test
        @DisplayName("argument count mismatch throws runtime exception")
        void testArgCountMismatch() {
            TemplateRuntimeException ex1 = assertThrows(TemplateRuntimeException.class,
                () -> engine.compile("{length('a', 'b')}").render(new Context()));
            assertTrue(ex1.getMessage().contains("length() expects 1 argument(s), but got 2"));
            TemplateRuntimeException ex2 = assertThrows(TemplateRuntimeException.class,
                () -> engine.compile("{match('abc')}").render(new Context()));
            assertTrue(ex2.getMessage().contains("match() expects 2 argument(s), but got 1"));
        }

        @Test
        @DisplayName("functions are callable inside lambda bodies")
        void testFunctionInLambdaBody() {
            assertEquals("3",
                engine.compile("{% let f = (x) -> length(x) %}{f('abc')}").render(new Context()));
        }

        @Test
        @DisplayName("functions work in {{ }} and ${ } expression wrappers")
        void testFunctionInOtherWrappers() {
            assertEquals("5", engine.compile("{{length('hello')}}").render(new Context()));
            assertEquals("4", engine.compile("${count(store.book)}").render(bookstoreContext()));
        }

        @Test
        @DisplayName("nested function calls")
        void testNestedFunctionCalls() {
            // value 取出唯一匹配的书（The Lord of the Rings）→ .title → length
            assertEquals("21",
                engine.compile("{length(value(store.book[?(@.isbn == '0-395-19395-8')]).title)}")
                    .render(bookstoreContext()));
        }

        @Test
        @DisplayName("function result composes with elvis and ternary")
        void testFunctionWithOperators() {
            assertEquals("0", engine.compile("{count(missing)}").render(new Context()));
            assertEquals("big",
                engine.compile("{count(store.book) > 2 ? 'big' : 'small'}").render(bookstoreContext()));
        }
    }
}
