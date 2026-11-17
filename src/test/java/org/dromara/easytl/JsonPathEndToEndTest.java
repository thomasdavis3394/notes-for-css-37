package org.dromara.easytl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JSONPath 端到端测试
 * <p>
 * 以经典 bookstore 数据为固定 fixture，通过 {@link EasyTL#render} 与
 * {@link TemplateEngine#compile} 走完整「模板文本 → 词法 → 语法 → 求值 → 渲染输出」链路，
 * 覆盖书店全场景、模板混排以及 {@code {{...}}} / {@code ${...}} 两种包裹语法。
 * </p>
 *
 * @author EasyTL Team
 */
@DisplayName("JSONPath End-to-End Tests")
class JsonPathEndToEndTest {

    @BeforeEach
    void resetDefaultEngine() {
        // 重置默认引擎配置
        EasyTL.defaultEngine().clearCache();
    }

    // ==================== 固定 fixture ====================

    /**
     * 经典 bookstore 数据 Map：store.book[]（category/author/title/isbn/price，
     * 前 2 本无 isbn，前 3 本有 sizes）+ store.bicycle
     */
    private Map<String, Object> bookstoreData() {
        Map<String, Object> data = new HashMap<>();
        data.put("store", bookstoreStore());
        return data;
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

    // ========== 书店全场景（EasyTL.render 便捷链路） ==========

    @Nested
    @DisplayName("Bookstore Scenarios")
    class BookstoreScenarios {

        @Test
        @DisplayName("all authors: store.book[*].author")
        void testAllAuthors() {
            assertEquals("[Nigel Rees, Evelyn Waugh, Herman Melville, J. R. R. Tolkien]",
                EasyTL.render("{store.book[*].author}", bookstoreData()));
        }

        @Test
        @DisplayName("all prices: store.book..price")
        void testAllPrices() {
            assertEquals("[8.95, 12.99, 8.99, 22.99]",
                EasyTL.render("{store.book..price}", bookstoreData()));
        }

        @Test
        @DisplayName("first book and slice: store.book[0] / store.book[1:3]")
        void testFirstBookAndSlice() {
            assertEquals("Sayings of the Century",
                EasyTL.render("{store.book[0].title}", bookstoreData()));
            assertEquals("2", EasyTL.render("{store.book[1:3].size()}", bookstoreData()));
            assertEquals("Moby Dick",
                EasyTL.render("{store.book[1:3][1].title}", bookstoreData()));
        }

        @Test
        @DisplayName("cheap book titles: store.book[?(@.price < 10)].title")
        void testCheapBookTitles() {
            assertEquals("[Sayings of the Century, Moby Dick]",
                EasyTL.render("{store.book[?(@.price < 10)].title}", bookstoreData()));
        }

        @Test
        @DisplayName("books with ISBN: store.book[?(@.isbn)].title")
        void testIsbnBookTitles() {
            assertEquals("[Moby Dick, The Lord of the Rings]",
                EasyTL.render("{store.book[?(@.isbn)].title}", bookstoreData()));
            // 对照：无 isbn 的书
            assertEquals("[Sayings of the Century, Sword of Honour]",
                EasyTL.render("{store.book[?(!@.isbn)].title}", bookstoreData()));
        }

        @Test
        @DisplayName("sizes filter: store.book[?(@.sizes anyof ['M'])].title")
        void testSizesFilter() {
            assertEquals("[Sword of Honour]",
                EasyTL.render("{store.book[?(@.sizes anyof ['M'])].title}", bookstoreData()));
        }

        @Test
        @DisplayName("store children: store.*")
        void testStoreChildren() {
            assertEquals("2", EasyTL.render("{store.*.size()}", bookstoreData()));
            assertEquals("red", EasyTL.render("{store.*[1].color}", bookstoreData()));
        }
    }

    // ========== TemplateEngine.compile().render(context) 完整链路 ==========

    @Nested
    @DisplayName("Compiled Template Scenarios")
    class CompiledTemplateScenarios {

        @Test
        @DisplayName("compile once, render bookstore queries via Context")
        void testCompiledTemplate() {
            TemplateEngine engine = new TemplateEngine();
            Context context = new Context();
            context.put("store", bookstoreStore());

            Template authors = engine.compile("{store.book[*].author}");
            assertEquals("[Nigel Rees, Evelyn Waugh, Herman Melville, J. R. R. Tolkien]",
                authors.render(context));

            Template cheapTitles = engine.compile("{store.book[?(@.price < 10)].title}");
            assertEquals("[Sayings of the Century, Moby Dick]", cheapTitles.render(context));

            Template prices = engine.compile("{store.book..price}");
            assertEquals("[8.95, 12.99, 8.99, 22.99]", prices.render(context));
        }

        @Test
        @DisplayName("compiled template is reusable across contexts")
        void testCompiledTemplateReusable() {
            TemplateEngine engine = new TemplateEngine();
            Template template = engine.compile("{store.book[*].title}");

            Context context = new Context();
            context.put("store", bookstoreStore());
            assertEquals("[Sayings of the Century, Sword of Honour, Moby Dick, The Lord of the Rings]",
                template.render(context));

            // 换一份只有 1 本书的数据再渲染一次
            Map<String, Object> store = bookstoreStore();
            ((List<?>) store.get("book")).remove(3);
            ((List<?>) store.get("book")).remove(2);
            ((List<?>) store.get("book")).remove(1);
            Context context2 = new Context();
            context2.put("store", store);
            assertEquals("[Sayings of the Century]", template.render(context2));
        }
    }

    // ========== 模板混排：纯文本 + for + 三元 + Elvis + JSONPath ==========

    @Nested
    @DisplayName("Mixed Template Rendering")
    class MixedTemplateTests {

        @Test
        @DisplayName("render full document mixing text, for, ternary, Elvis and JSONPath")
        void testMixedDocument() {
            String template =
                "Inventory\n" +
                "{% for b in store.book[?(@.price < 10)] }" +
                "- {b.title} by {b.author}: {b.price < 9 ? 'cheap' : 'mid'} ({b.isbn ?? 'no-isbn'})\n" +
                "{/% for }" +
                "Authors: {store.book[*].author}\n" +
                "Prices: {store.book..price}\n" +
                "Bicycle: {store.bicycle.color} ${store.bicycle.price}";

            String expected =
                "Inventory\n" +
                "- Sayings of the Century by Nigel Rees: cheap (no-isbn)\n" +
                "- Moby Dick by Herman Melville: cheap (0-553-21311-3)\n" +
                "Authors: [Nigel Rees, Evelyn Waugh, Herman Melville, J. R. R. Tolkien]\n" +
                "Prices: [8.95, 12.99, 8.99, 22.99]\n" +
                "Bicycle: red 19.95";

            assertEquals(expected, EasyTL.render(template, bookstoreData()));
        }

        @Test
        @DisplayName("for over wildcard projection with slice inside body")
        void testMixedForWithSlice() {
            String template =
                "{% for t in store.book[:2].title }[{t}]{/% for }" +
                "|{% for b in store.book[?(@.sizes anyof ['M'])] }{b.title}{/% for }";
            assertEquals("[Sayings of the Century][Sword of Honour]|Sword of Honour",
                EasyTL.render(template, bookstoreData()));
        }
    }

    // ========== {{...}} 与 ${...} 包裹语法 ==========

    @Nested
    @DisplayName("Wrapper Syntax Tests")
    class WrapperSyntaxTests {

        @Test
        @DisplayName("{{...}} runs core JSONPath scenarios")
        void testDoubleBraceSyntax() {
            assertEquals("[Nigel Rees, Evelyn Waugh, Herman Melville, J. R. R. Tolkien]",
                EasyTL.render("{{store.book[*].author}}", bookstoreData()));
            assertEquals("[Sayings of the Century, Moby Dick]",
                EasyTL.render("{{store.book[?(@.price < 10)].title}}", bookstoreData()));
            assertEquals("2", EasyTL.render("{{store.book[1:3].size()}}", bookstoreData()));
        }

        @Test
        @DisplayName("${...} runs core JSONPath scenarios")
        void testDollarBraceSyntax() {
            assertEquals("[8.95, 12.99, 8.99, 22.99]",
                EasyTL.render("${store.book..price}", bookstoreData()));
            assertEquals("[Moby Dick, The Lord of the Rings]",
                EasyTL.render("${store.book[?(@.isbn)].title}", bookstoreData()));
            assertEquals("[Sword of Honour]",
                EasyTL.render("${store.book[?(@.sizes anyof ['M'])].title}", bookstoreData()));
        }
    }
}

// 17445c
