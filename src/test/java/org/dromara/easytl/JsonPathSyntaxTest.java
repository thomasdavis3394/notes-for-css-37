package org.dromara.easytl;

import org.dromara.easytl.exception.TemplateRuntimeException;
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
 * JSONPath 语法特性测试
 * <p>
 * 覆盖通配符（[*]/.*）、切片（[start:end(:step)]）、过滤（[?(expr)]，含 Map 按 value 过滤）、
 * 存在性过滤、递归下降（..name / ..* / ..[...]）、负数下标、Union 多选（[sel, sel, ...]）、
 * .. 二义性回归、集合操作符（subsetof/anyof/noneof）
 * 以及与 for/Elvis/安全导航的组合。
 * </p>
 *
 * @author EasyTL Team
 */
@DisplayName("JSONPath Syntax Tests")
class JsonPathSyntaxTest {

    private TemplateEngine engine;

    @BeforeEach
    void setUp() {
        engine = new TemplateEngine();
        EasyTL.defaultEngine().clearCache();
    }

    // ==================== 测试数据 ====================

    /**
     * 经典 bookstore 上下文：4 本书（前 2 本无 isbn，前 3 本有 sizes）+ bicycle
     */
    private Context bookstoreContext() {
        Context context = new Context();
        context.put("store", bookstoreStore());
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

    /** 带两个 getter 的 Bean（通配符枚举属性值用） */
    public static class BeanPoint {
        public int getX() {
            return 1;
        }

        public int getY() {
            return 2;
        }
    }

    /** 带 isbn 的 Bean（存在性过滤用） */
    public static class BeanBook {
        private final String title;
        private final String isbn;

        public BeanBook(String title, String isbn) {
            this.title = title;
            this.isbn = isbn;
        }

        public String getTitle() {
            return title;
        }

        public String getIsbn() {
            return isbn;
        }
    }

    /** 无 isbn getter 的 Bean（存在性过滤用） */
    public static class BeanMagazine {
        private final String title;

        public BeanMagazine(String title) {
            this.title = title;
        }

        public String getTitle() {
            return title;
        }
    }

    // ========== 通配符 [*] / .* ==========

    @Nested
    @DisplayName("Wildcard Tests")
    class WildcardTests {

        @Test
        @DisplayName("store.book[*].author projects authors of all books")
        void testWildcardMemberProjection() {
            String result = engine.compile("{store.book[*].author}").render(bookstoreContext());
            assertEquals("[Nigel Rees, Evelyn Waugh, Herman Melville, J. R. R. Tolkien]", result);
        }

        @Test
        @DisplayName("store.* collects all children of store")
        void testDotWildcardChildren() {
            assertEquals("2", engine.compile("{store.*.size()}").render(bookstoreContext()));
            assertEquals("red", engine.compile("{store.*[1].color}").render(bookstoreContext()));
        }

        @Test
        @DisplayName("store.*.price chains projection and skips nodes without price")
        void testDotWildcardChainedProjection() {
            // book 列表本身没有 price 属性（跳过），bicycle 有 price
            assertEquals("[19.95]", engine.compile("{store.*.price}").render(bookstoreContext()));
        }

        @Test
        @DisplayName("store.book.* is equivalent to store.book[*]")
        void testDotWildcardEquivalence() {
            String bracket = engine.compile("{store.book[*].title}").render(bookstoreContext());
            String dot = engine.compile("{store.book.*.title}").render(bookstoreContext());
            assertEquals(bracket, dot);
            assertEquals("[Sayings of the Century, Sword of Honour, Moby Dick, The Lord of the Rings]", dot);
        }

        @Test
        @DisplayName("[*] works on Java arrays")
        void testWildcardOnArray() {
            Context context = new Context();
            context.put("arr", new String[]{"a", "b", "c"});
            assertEquals("[a, b, c]", engine.compile("{arr[*]}").render(context));
        }

        @Test
        @DisplayName("[*] collects Map values")
        void testWildcardOnMap() {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("k1", "v1");
            map.put("k2", "v2");
            Context context = new Context();
            context.put("map", map);
            assertEquals("[v1, v2]", engine.compile("{map[*]}").render(context));
        }

        @Test
        @DisplayName("[*] collects Bean property values")
        void testWildcardOnBean() {
            Context context = new Context();
            context.put("point", new BeanPoint());
            assertEquals("2", engine.compile("{point[*].size()}").render(context));
            // 属性枚举顺序不做假设，用 in 判断内容
            assertEquals("true", engine.compile("{1 in point[*] && 2 in point[*]}").render(context));
        }

        @Test
        @DisplayName("[*] on scalar throws runtime exception")
        void testWildcardOnScalarThrows() {
            TemplateRuntimeException ex = assertThrows(TemplateRuntimeException.class,
                () -> engine.compile("{store.bicycle.color[*]}").render(bookstoreContext()));
            assertTrue(ex.getMessage().contains("Cannot apply wildcard to java.lang.String"));
        }

        @Test
        @DisplayName("[*] on null throws runtime exception")
        void testWildcardOnNullThrows() {
            TemplateRuntimeException ex = assertThrows(TemplateRuntimeException.class,
                () -> engine.compile("{missing[*]}").render(new Context()));
            assertTrue(ex.getMessage().contains("Cannot apply wildcard to null"));
        }

        @Test
        @DisplayName("[*] followed by numeric index takes list element")
        void testWildcardThenNumericIndex() {
            assertEquals("Sayings of the Century",
                engine.compile("{store.book[*][0].title}").render(bookstoreContext()));
        }
    }

    // ========== 切片 [start:end] ==========

    @Nested
    @DisplayName("Slice Tests")
    class SliceTests {

        @Test
        @DisplayName("[1:4] takes elements 1..3")
        void testSliceBasic() {
            assertEquals("3", engine.compile("{store.book[1:4].size()}").render(bookstoreContext()));
            assertEquals("Sword of Honour",
                engine.compile("{store.book[1:4][0].title}").render(bookstoreContext()));
        }

        @Test
        @DisplayName("[:2] omits start")
        void testSliceOmitStart() {
            assertEquals("2", engine.compile("{store.book[:2].size()}").render(bookstoreContext()));
            assertEquals("Sword of Honour",
                engine.compile("{store.book[:2][1].title}").render(bookstoreContext()));
        }

        @Test
        @DisplayName("[2:] omits end")
        void testSliceOmitEnd() {
            assertEquals("2", engine.compile("{store.book[2:].size()}").render(bookstoreContext()));
            assertEquals("Moby Dick",
                engine.compile("{store.book[2:][0].title}").render(bookstoreContext()));
        }

        @Test
        @DisplayName("[:] takes the whole list")
        void testSliceFull() {
            assertEquals("4", engine.compile("{store.book[:].size()}").render(bookstoreContext()));
        }

        @Test
        @DisplayName("[-2:] counts from the end")
        void testSliceNegative() {
            assertEquals("Moby Dick",
                engine.compile("{store.book[-2:][0].title}").render(bookstoreContext()));
            assertEquals("2", engine.compile("{store.book[-2:].size()}").render(bookstoreContext()));
        }

        @Test
        @DisplayName("out-of-range bounds are clamped")
        void testSliceClamping() {
            assertEquals("4", engine.compile("{store.book[0:99].size()}").render(bookstoreContext()));
            assertEquals("2", engine.compile("{store.book[-99:2].size()}").render(bookstoreContext()));
        }

        @Test
        @DisplayName("[2:1] yields empty result")
        void testSliceEmpty() {
            assertEquals("[]", engine.compile("{store.book[2:1]}").render(bookstoreContext()));
        }

        @Test
        @DisplayName("slice works on Java arrays")
        void testSliceOnArray() {
            Context context = new Context();
            context.put("arr", new String[]{"a", "b", "c", "d"});
            assertEquals("[b, c]", engine.compile("{arr[1:3]}").render(context));
        }

        @Test
        @DisplayName("slice on non-list throws runtime exception")
        void testSliceOnMapThrows() {
            TemplateRuntimeException ex = assertThrows(TemplateRuntimeException.class,
                () -> engine.compile("{store.bicycle[1:2]}").render(bookstoreContext()));
            assertTrue(ex.getMessage().contains("Cannot apply slice to"));
        }

        @Test
        @DisplayName("slice on scalar throws runtime exception")
        void testSliceOnScalarThrows() {
            TemplateRuntimeException ex = assertThrows(TemplateRuntimeException.class,
                () -> engine.compile("{store.bicycle.color[1:2]}").render(bookstoreContext()));
            assertTrue(ex.getMessage().contains("Cannot apply slice to java.lang.String"));
        }

        @Test
        @DisplayName("ternary subscript a[b ? c : d] is not a slice")
        void testTernarySubscriptRegression() {
            assertEquals("Sayings of the Century",
                engine.compile("{store.book[1 > 0 ? 0 : 1].title}").render(bookstoreContext()));
        }
    }

    // ========== 过滤 [?(expr)] ==========

    @Nested
    @DisplayName("Filter Tests")
    class FilterTests {

        @Test
        @DisplayName("[?(@.price < 10)] filters books by predicate")
        void testFilterByPrice() {
            assertEquals("[Sayings of the Century, Moby Dick]",
                engine.compile("{store.book[?(@.price < 10)].title}").render(bookstoreContext()));
        }

        @Test
        @DisplayName("filter result chains into member projection")
        void testFilterChainedProjection() {
            assertEquals("[The Lord of the Rings]",
                engine.compile("{store.book[?(@.price > 20)].title}").render(bookstoreContext()));
        }

        @Test
        @DisplayName("@['key'] accesses current element by key")
        void testFilterIndexKey() {
            assertEquals("[Sayings of the Century, Moby Dick]",
                engine.compile("{store.book[?(@['price'] < 10)].title}").render(bookstoreContext()));
        }

        @Test
        @DisplayName("nested filter inside predicate")
        void testNestedFilter() {
            // sizes 中含 'M' 的书：内层过滤结果非空即为真
            assertEquals("[Sword of Honour]",
                engine.compile("{store.book[?(@.sizes[?(@ == 'M')])].title}").render(bookstoreContext()));
        }

        @Test
        @DisplayName("cascaded [*][?(...)] filtering")
        void testCascadedWildcardFilter() {
            assertEquals("[Sword of Honour, The Lord of the Rings]",
                engine.compile("{store.book[*][?(@.price > 12)].title}").render(bookstoreContext()));
        }

        @Test
        @DisplayName("filter result followed by [0] takes first match")
        void testFilterThenNumericIndex() {
            assertEquals("Sayings of the Century",
                engine.compile("{store.book[?(@.price < 10)][0].title}").render(bookstoreContext()));
        }

        @Test
        @DisplayName("@ outside filter throws runtime exception")
        void testCurrentNodeOutsideFilterThrows() {
            TemplateRuntimeException ex = assertThrows(TemplateRuntimeException.class,
                () -> engine.compile("{@.price}").render(bookstoreContext()));
            assertTrue(ex.getMessage().contains("'@' can only be used inside filter expressions"));
        }

        @Test
        @DisplayName("@ inside plain index (not filter) throws runtime exception")
        void testCurrentNodeInIndexThrows() {
            TemplateRuntimeException ex = assertThrows(TemplateRuntimeException.class,
                () -> engine.compile("{store.book[@.price]}").render(bookstoreContext()));
            assertTrue(ex.getMessage().contains("'@' can only be used inside filter expressions"));
        }

        @Test
        @DisplayName("filter on Bean throws runtime exception")
        void testFilterOnBeanThrows() {
            Context context = new Context();
            context.put("point", new BeanPoint());
            TemplateRuntimeException ex = assertThrows(TemplateRuntimeException.class,
                () -> engine.compile("{point[?(@.x)]}").render(context));
            assertTrue(ex.getMessage().contains("Cannot apply filter to"));
        }
    }

    // ========== 存在性过滤 ==========

    @Nested
    @DisplayName("Existence Filter Tests")
    class ExistenceFilterTests {

        @Test
        @DisplayName("[?(@.isbn)] keeps only elements having the key (Map)")
        void testExistenceFilterMap() {
            assertEquals("[Moby Dick, The Lord of the Rings]",
                engine.compile("{store.book[?(@.isbn)].title}").render(bookstoreContext()));
        }

        @Test
        @DisplayName("[?(!@.isbn)] keeps only elements missing the key")
        void testExistenceFilterNegated() {
            assertEquals("[Sayings of the Century, Sword of Honour]",
                engine.compile("{store.book[?(!@.isbn)].title}").render(bookstoreContext()));
        }

        @Test
        @DisplayName("[?(@.isbn)] keeps only beans having the getter (Bean)")
        void testExistenceFilterBean() {
            List<Object> items = new ArrayList<>();
            items.add(new BeanBook("B1", "isbn-1"));
            items.add(new BeanMagazine("M1"));
            items.add(new BeanBook("B2", null));
            Context context = new Context();
            context.put("items", items);
            // BeanMagazine 无 isbn getter、B2 的 isbn 为 null，均被排除
            assertEquals("[B1]", engine.compile("{items[?(@.isbn)].title}").render(context));
        }

        @Test
        @DisplayName("missing property in comparison skips element instead of failing")
        void testMissingPropertyInComparison() {
            List<Object> items = new ArrayList<>();
            Map<String, Object> a = new HashMap<>();
            a.put("name", "a");
            a.put("price", 5);
            Map<String, Object> b = new HashMap<>();
            b.put("name", "b");
            Map<String, Object> c = new HashMap<>();
            c.put("name", "c");
            c.put("price", 15);
            items.add(a);
            items.add(b);
            items.add(c);
            Context context = new Context();
            context.put("items", items);
            assertEquals("[a]", engine.compile("{items[?(@.price < 10)].name}").render(context));
        }

        @Test
        @DisplayName("deep path with null intermediate yields no match")
        void testDeepPathNullIntermediate() {
            List<Object> items = new ArrayList<>();
            Map<String, Object> withMeta = new HashMap<>();
            withMeta.put("title", "with-meta");
            Map<String, Object> publisher = new HashMap<>();
            publisher.put("name", "ACME");
            Map<String, Object> meta = new HashMap<>();
            meta.put("publisher", publisher);
            withMeta.put("meta", meta);
            Map<String, Object> noMeta = new HashMap<>();
            noMeta.put("title", "no-meta");
            items.add(withMeta);
            items.add(noMeta);
            Context context = new Context();
            context.put("items", items);
            // noMeta 的 @.meta.publisher.name 中间节点为 null，按不匹配处理
            assertEquals("[with-meta]", engine.compile("{items[?(@.meta.publisher.name)].title}").render(context));
        }

        @Test
        @DisplayName("falsy property values (\"\" / false) are excluded by truthiness")
        void testExistenceFilterFalsyValues() {
            List<Object> items = new ArrayList<>();
            Map<String, Object> empty = new HashMap<>();
            empty.put("t", "a");
            empty.put("isbn", "");
            Map<String, Object> fals = new HashMap<>();
            fals.put("t", "b");
            fals.put("isbn", false);
            Map<String, Object> valid = new HashMap<>();
            valid.put("t", "c");
            valid.put("isbn", "x");
            items.add(empty);
            items.add(fals);
            items.add(valid);
            Context context = new Context();
            context.put("items", items);
            assertEquals("[c]", engine.compile("{items[?(@.isbn)].t}").render(context));
        }
    }

    // ========== 递归下降 ..name ==========

    @Nested
    @DisplayName("Recursive Descent Tests")
    class RecursiveDescentTests {

        @Test
        @DisplayName("store.book..price collects prices at any depth")
        void testRecursiveDescentPrices() {
            assertEquals("[8.95, 12.99, 8.99, 22.99]",
                engine.compile("{store.book..price}").render(bookstoreContext()));
        }

        @Test
        @DisplayName("recursive descent over nested Map/List mixture (pre-order)")
        void testRecursiveDescentNested() {
            Map<String, Object> b = new HashMap<>();
            b.put("price", 1);
            Map<String, Object> item2 = new HashMap<>();
            item2.put("price", 2);
            Map<String, Object> item3 = new HashMap<>();
            item3.put("price", 3);
            List<Object> inner = new ArrayList<>();
            inner.add(item3);
            List<Object> list = new ArrayList<>();
            list.add(item2);
            list.add(inner);
            // LinkedHashMap 保证先序结果顺序确定
            Map<String, Object> a = new LinkedHashMap<>();
            a.put("b", b);
            a.put("list", list);
            Map<String, Object> root = new HashMap<>();
            root.put("a", a);
            Context context = new Context();
            context.put("root", root);
            assertEquals("[1, 2, 3]", engine.compile("{root.a..price}").render(context));
        }

        @Test
        @DisplayName("cyclic references do not overflow the stack")
        void testRecursiveDescentCycle() {
            Map<String, Object> cyclic = new HashMap<>();
            cyclic.put("price", 7);
            cyclic.put("self", cyclic);
            Map<String, Object> holder = new HashMap<>();
            holder.put("node", cyclic);
            Context context = new Context();
            context.put("holder", holder);
            assertEquals("[7]", engine.compile("{holder.node..price}").render(context));
        }

        @Test
        @DisplayName("recursive descent result is iterable in for")
        void testRecursiveDescentInFor() {
            String template = "{% for p in store.book..price }{p};{/% for }";
            assertEquals("8.95;12.99;8.99;22.99;", engine.compile(template).render(bookstoreContext()));
        }
    }

    // ========== .. 二义性回归 ==========

    @Nested
    @DisplayName("Range Ambiguity Regression Tests")
    class RangeAmbiguityTests {

        @Test
        @DisplayName("{1..10} remains a range literal")
        void testNumberRangeUnchanged() {
            assertEquals("[1, 10]", engine.compile("{1..10}").render(new Context()));
        }

        @Test
        @DisplayName("{a..b} with identifiers remains a range")
        void testIdentifierRangeUnchanged() {
            Context context = new Context();
            context.put("a", 1);
            context.put("b", 5);
            assertEquals("[1, 5]", engine.compile("{a..b}").render(context));
        }

        @Test
        @DisplayName("for i in 1..10 remains a range iteration")
        void testForRangeUnchanged() {
            assertEquals("12345", engine.compile("{% for i in 1..5 }{i}{/% for }").render(new Context()));
        }

        @Test
        @DisplayName("in operator with range still works")
        void testInRangeUnchanged() {
            assertEquals("true", engine.compile("{3 in 1..10}").render(new Context()));
        }

        @Test
        @DisplayName("..* collects all descendant values")
        void testRecursiveWildcard() {
            assertEquals("33", engine.compile("{store..*.size()}").render(bookstoreContext()));
            assertEquals("true",
                engine.compile("{'Moby Dick' in store..* && 'red' in store..*}").render(bookstoreContext()));
        }
    }

    // ========== 集合操作符 subsetof / anyof / noneof ==========

    @Nested
    @DisplayName("Set Operator Tests")
    class SetOperatorTests {

        @Test
        @DisplayName("subsetof: left is subset of right")
        void testSubsetof() {
            assertEquals("true", engine.compile("{['S','M'] subsetof ['S','M','L']}").render(new Context()));
            assertEquals("false", engine.compile("{['S','X'] subsetof ['S','M','L']}").render(new Context()));
        }

        @Test
        @DisplayName("subsetof: empty left is always true")
        void testSubsetofEmptyLeft() {
            assertEquals("true", engine.compile("{[] subsetof ['S']}").render(new Context()));
        }

        @Test
        @DisplayName("anyof: intersection exists / not exists")
        void testAnyof() {
            assertEquals("true", engine.compile("{['M','L'] anyof ['M']}").render(new Context()));
            assertEquals("false", engine.compile("{['X'] anyof ['M','L']}").render(new Context()));
        }

        @Test
        @DisplayName("anyof: empty left is always false")
        void testAnyofEmptyLeft() {
            assertEquals("false", engine.compile("{[] anyof ['M']}").render(new Context()));
        }

        @Test
        @DisplayName("noneof: equivalent to !(anyof)")
        void testNoneof() {
            assertEquals("true", engine.compile("{['X'] noneof ['M','L']}").render(new Context()));
            assertEquals("false", engine.compile("{['M'] noneof ['M','L']}").render(new Context()));
        }

        @Test
        @DisplayName("noneof: empty left is always true")
        void testNoneofEmptyLeft() {
            assertEquals("true", engine.compile("{[] noneof ['M']}").render(new Context()));
        }

        @Test
        @DisplayName("scalar left operand is treated as single-element set")
        void testScalarLeftOperand() {
            assertEquals("true", engine.compile("{'M' anyof ['M','L']}").render(new Context()));
            assertEquals("false", engine.compile("{'X' subsetof ['M','L']}").render(new Context()));
            assertEquals("true", engine.compile("{'M' subsetof ['M','L']}").render(new Context()));
        }

        @Test
        @DisplayName("null left operand normalizes to empty set")
        void testNullLeftOperand() {
            assertEquals("true", engine.compile("{missing subsetof ['M']}").render(new Context()));
            assertEquals("false", engine.compile("{missing anyof ['M']}").render(new Context()));
            assertEquals("true", engine.compile("{missing noneof ['M']}").render(new Context()));
        }

        @Test
        @DisplayName("Java array (non-List) operand is normalized")
        void testArrayOperand() {
            Context context = new Context();
            context.put("sizes", new String[]{"S", "M"});
            assertEquals("true", engine.compile("{sizes subsetof ['S','M','L']}").render(context));
            assertEquals("true", engine.compile("{sizes anyof ['M']}").render(context));
        }

        @Test
        @DisplayName("duplicate elements do not affect result")
        void testDuplicateElements() {
            assertEquals("true", engine.compile("{['M','M'] subsetof ['M']}").render(new Context()));
            assertEquals("true", engine.compile("{['M','M'] anyof ['M']}").render(new Context()));
        }

        @Test
        @DisplayName("set operators share precedence/associativity with == and in")
        void testPrecedenceWithEqualityAndIn() {
            assertEquals("true",
                engine.compile("{'M' anyof ['M','L'] == true}").render(new Context()));
            assertEquals("true",
                engine.compile("{'M' in ['M','L'] && ['S','M'] subsetof ['S','M','L']}").render(new Context()));
        }

        @Test
        @DisplayName("set operators work inside filter predicates")
        void testSetOperatorInFilter() {
            assertEquals("[Sword of Honour]",
                engine.compile("{store.book[?(@.sizes anyof ['M'])].title}").render(bookstoreContext()));
        }
    }

    // ========== 与 for / Elvis / 安全导航组合 ==========

    @Nested
    @DisplayName("Combination Tests")
    class CombinationTests {

        @Test
        @DisplayName("for iterates over filter result")
        void testForOverFilterResult() {
            String template = "{% for b in store.book[?(@.price < 10)] }{b.title};{/% for }";
            assertEquals("Sayings of the Century;Moby Dick;",
                engine.compile(template).render(bookstoreContext()));
        }

        @Test
        @DisplayName("Elvis applies to member of filtered element")
        void testElvisCombination() {
            // discount 不存在 → null → Elvis 取右值
            assertEquals("full",
                engine.compile("{store.book[?(@.isbn)][0].discount ?? 'full'}").render(bookstoreContext()));
        }

        @Test
        @DisplayName("safe navigation on JSONPath results")
        void testSafeNavigationCombination() {
            assertEquals("[Sayings of the Century, Sword of Honour, Moby Dick, The Lord of the Rings]",
                engine.compile("{store.book[*]?.title}").render(bookstoreContext()));
            assertEquals("Moby Dick",
                engine.compile("{store.book[?(@.isbn)][0]?.title}").render(bookstoreContext()));
        }

        @Test
        @DisplayName("size() on wildcard projection")
        void testSizeOnProjection() {
            assertEquals("4",
                engine.compile("{store.book[*].author.size()}").render(bookstoreContext()));
        }

        @Test
        @DisplayName("ternary over filter result size")
        void testTernaryCombination() {
            assertEquals("many",
                engine.compile("{store.book[?(@.price < 10)].size() > 1 ? 'many' : 'few'}")
                    .render(bookstoreContext()));
        }
    }

    // ========== 负数下标 [-1] ==========

    @Nested
    @DisplayName("Negative Index Tests")
    class NegativeIndexTests {

        @Test
        @DisplayName("[-1] takes the last element")
        void testNegativeIndexLast() {
            assertEquals("The Lord of the Rings",
                engine.compile("{store.book[-1].title}").render(bookstoreContext()));
            assertEquals("Sayings of the Century",
                engine.compile("{store.book[-4].title}").render(bookstoreContext()));
        }

        @Test
        @DisplayName("negative index works on Java arrays")
        void testNegativeIndexOnArray() {
            Context context = new Context();
            context.put("arr", new String[]{"a", "b", "c"});
            assertEquals("c", engine.compile("{arr[-1]}").render(context));
        }

        @Test
        @DisplayName("negative index works on NodeList (wildcard result)")
        void testNegativeIndexOnNodeList() {
            assertEquals("The Lord of the Rings",
                engine.compile("{store.book[*][-1].title}").render(bookstoreContext()));
        }

        @Test
        @DisplayName("out-of-range negative index still throws")
        void testNegativeIndexOutOfBounds() {
            TemplateRuntimeException ex = assertThrows(TemplateRuntimeException.class,
                () -> engine.compile("{store.book[-5]}").render(bookstoreContext()));
            assertTrue(ex.getMessage().contains("out of bounds"));
        }

        @Test
        @DisplayName("negative index compound assignment writes from the end")
        void testNegativeIndexAssignment() {
            Context context = new Context();
            List<Object> nums = new ArrayList<>(Arrays.asList(1, 2, 3, 4));
            context.put("nums", nums);
            engine.compile("{% nums[-1] += 5 %}").render(context);
            assertEquals(9, nums.get(3));
        }
    }

    // ========== 切片步长 [start:end:step] ==========

    @Nested
    @DisplayName("Slice Step Tests")
    class SliceStepTests {

        @Test
        @DisplayName("[0:4:2] takes every second element")
        void testSliceStepBasic() {
            assertEquals("2", engine.compile("{store.book[0:4:2].size()}").render(bookstoreContext()));
            assertEquals("Moby Dick",
                engine.compile("{store.book[0:4:2][1].title}").render(bookstoreContext()));
        }

        @Test
        @DisplayName("[::2] omits start and end")
        void testSliceStepOmitted() {
            assertEquals("2", engine.compile("{store.book[::2].size()}").render(bookstoreContext()));
            assertEquals("2", engine.compile("{store.book[1:4:2].size()}").render(bookstoreContext()));
        }

        @Test
        @DisplayName("[::-1] reverses the list")
        void testSliceNegativeStepReverse() {
            assertEquals("The Lord of the Rings",
                engine.compile("{store.book[::-1][0].title}").render(bookstoreContext()));
        }

        @Test
        @DisplayName("[3:0:-1] walks backwards from 3 down to 1")
        void testSliceNegativeStepRange() {
            assertEquals("3", engine.compile("{store.book[3:0:-1].size()}").render(bookstoreContext()));
            assertEquals("Sword of Honour",
                engine.compile("{store.book[3:0:-1][2].title}").render(bookstoreContext()));
        }

        @Test
        @DisplayName("step 0 yields empty result")
        void testSliceZeroStep() {
            assertEquals("0", engine.compile("{store.book[::0].size()}").render(bookstoreContext()));
        }

        @Test
        @DisplayName("slice without step is unchanged")
        void testSliceNoStepRegression() {
            assertEquals("2", engine.compile("{store.book[1:3].size()}").render(bookstoreContext()));
        }

        @Test
        @DisplayName("step works on Java arrays and can be an expression")
        void testSliceStepOnArrayAndExpression() {
            Context context = new Context();
            context.put("arr", new String[]{"a", "b", "c"});
            assertEquals("[a, c]", engine.compile("{arr[::2]}").render(context));
            assertEquals("2", engine.compile("{store.book[0:4:1+1].size()}").render(bookstoreContext()));
        }
    }

    // ========== Union 多选 [sel, sel, ...] ==========

    @Nested
    @DisplayName("Union Tests")
    class UnionTests {

        @Test
        @DisplayName("[0, 2] selects multiple indices in order")
        void testUnionIndices() {
            assertEquals("[Sayings of the Century, Moby Dick]",
                engine.compile("{store.book[0, 2].title}").render(bookstoreContext()));
        }

        @Test
        @DisplayName("['color', 'price'] selects multiple Map keys")
        void testUnionKeys() {
            assertEquals("[red, 19.95]",
                engine.compile("{store.bicycle['color', 'price']}").render(bookstoreContext()));
        }

        @Test
        @DisplayName("slice and index can mix in a union")
        void testUnionMixedSliceAndIndex() {
            assertEquals("3", engine.compile("{store.book[0:2, 3].size()}").render(bookstoreContext()));
            assertEquals("3", engine.compile("{store.book[0:4:2, 1].size()}").render(bookstoreContext()));
        }

        @Test
        @DisplayName("negative index works inside union")
        void testUnionNegativeIndex() {
            assertEquals("The Lord of the Rings",
                engine.compile("{store.book[-1, 0][0].title}").render(bookstoreContext()));
        }

        @Test
        @DisplayName("missing key / out-of-bounds index / mismatched type are skipped")
        void testUnionSkipsMisses() {
            assertEquals("[red]",
                engine.compile("{store.bicycle['color', 'missing']}").render(bookstoreContext()));
            assertEquals("1", engine.compile("{store.book[0, 99].size()}").render(bookstoreContext()));
            assertEquals("1", engine.compile("{store.book[0, 'title'].size()}").render(bookstoreContext()));
        }

        @Test
        @DisplayName("wildcard can appear in a union")
        void testUnionWildcard() {
            // 'color' → red；* → [red, 19.95]
            assertEquals("3", engine.compile("{store.bicycle['color', *].size()}").render(bookstoreContext()));
        }

        @Test
        @DisplayName("union on null throws runtime exception")
        void testUnionOnNullThrows() {
            TemplateRuntimeException ex = assertThrows(TemplateRuntimeException.class,
                () -> engine.compile("{missing[0, 1]}").render(new Context()));
            assertTrue(ex.getMessage().contains("Cannot apply union to null"));
        }

        @Test
        @DisplayName("union result supports further projection and filter")
        void testUnionChaining() {
            assertEquals("[Sayings of the Century, Moby Dick]",
                engine.compile("{store.book[0, 1, 2, 3][?(@.price < 10)].title}").render(bookstoreContext()));
        }
    }

    // ========== 递归下降选择器 ..* / ..[...] ==========

    @Nested
    @DisplayName("Recursive Descent Selector Tests")
    class RecursiveDescentSelectorTests {

        @Test
        @DisplayName("..['name'] is equivalent to ..name")
        void testRecursiveBracketName() {
            assertEquals("5", engine.compile("{store..['price'].size()}").render(bookstoreContext()));
            assertEquals("true",
                engine.compile("{store.book..price == store.book..['price']}").render(bookstoreContext()));
        }

        @Test
        @DisplayName("..[*] is equivalent to ..*")
        void testRecursiveBracketWildcard() {
            assertEquals("true",
                engine.compile("{store..[*].size() == store..*.size()}").render(bookstoreContext()));
        }

        @Test
        @DisplayName("..[0] takes the first element of every list in the tree")
        void testRecursiveBracketIndex() {
            assertEquals("4", engine.compile("{store..[0].size()}").render(bookstoreContext()));
            assertEquals("S", engine.compile("{store..[0][1]}").render(bookstoreContext()));
        }

        @Test
        @DisplayName("..[0:2] slices every list in the tree")
        void testRecursiveBracketSlice() {
            assertEquals("6", engine.compile("{store..[0:2].size()}").render(bookstoreContext()));
        }

        @Test
        @DisplayName("..[?(...)] filters every list/Map in the tree")
        void testRecursiveBracketFilter() {
            assertEquals("[Moby Dick, The Lord of the Rings]",
                engine.compile("{store..[?(@.isbn)].title}").render(bookstoreContext()));
        }

        @Test
        @DisplayName("..[0, 'price'] applies multiple selectors per node")
        void testRecursiveBracketUnion() {
            assertEquals("9", engine.compile("{store..[0, 'price'].size()}").render(bookstoreContext()));
        }
    }

    // ========== Map 过滤 [?(...)] ==========

    @Nested
    @DisplayName("Map Filter Tests")
    class MapFilterTests {

        @Test
        @DisplayName("filter applies to Map values")
        void testMapFilterValues() {
            assertEquals("[19.95]",
                engine.compile("{store.bicycle[?(@ > 10)]}").render(bookstoreContext()));
            assertEquals("[red]",
                engine.compile("{store.bicycle[?(@ == 'red')]}").render(bookstoreContext()));
        }

        @Test
        @DisplayName("existence filter over Map values")
        void testMapFilterExistence() {
            // store 的 value 中只有 bicycle 有 price
            assertEquals("red",
                engine.compile("{store[?(@.price)][0].color}").render(bookstoreContext()));
        }

        @Test
        @DisplayName("elements failing predicate evaluation are skipped")
        void testMapFilterSkipsFailures() {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("a", 1);
            map.put("b", "x");
            map.put("c", 3);
            Context context = new Context();
            context.put("m", map);
            assertEquals("[3]", engine.compile("{m[?(@ > 1)]}").render(context));
        }
    }
}
