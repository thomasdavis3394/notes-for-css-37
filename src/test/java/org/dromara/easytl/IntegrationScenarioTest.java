package org.dromara.easytl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 集成场景测试
 * <p>
 * 测试复杂场景下的模板渲染，包括嵌套结构、特性组合、数据结构场景和实际应用模式
 * </p>
 *
 * @author EasyTL Team
 */
@DisplayName("Integration Scenario Tests")
class IntegrationScenarioTest {

    private TemplateEngine engine;

    @BeforeEach
    void setUp() {
        engine = new TemplateEngine();
    }

    // ========== 嵌套结构测试 ==========

    @Nested
    @DisplayName("Nested Structure Tests")
    class NestedStructureTests {

        @Test
        @DisplayName("Nested if in for loop")
        void testNestedIfInForLoop() {
            String template = "{% for i in 1..5 }{% if i > 2 }{i} {/% if }{/% for }";
            assertEquals("3 4 5 ", engine.compile(template).render(new Context()));
        }

        @Test
        @DisplayName("Nested for loops with condition")
        void testNestedForLoopsWithCondition() {
            String template = "{% for i in 1..3 }{% for j in 1..3 }{% if i == j }*{% else }-{/% if }{/% for }\n{/% for }";
            String result = engine.compile(template).render(new Context());
            assertTrue(result.contains("*"));
            assertTrue(result.contains("-"));
        }

        @Test
        @DisplayName("Nested switch in for loop")
        void testNestedSwitchInForLoop() {
            String template = "{% for i in 1..3 }{% switch i }{% case 1 }One{% case 2 }Two{% case 3 }Three{/% switch }\n{/% for }";
            String result = engine.compile(template).render(new Context());
            assertTrue(result.contains("One"));
            assertTrue(result.contains("Two"));
            assertTrue(result.contains("Three"));
        }

        @Test
        @DisplayName("Deeply nested expressions")
        void testDeeplyNestedExpressions() {
            String template = "{(a + b) * (c - d)}";
            Context context = new Context();
            context.put("a", 10);
            context.put("b", 5);
            context.put("c", 8);
            context.put("d", 3);
            assertEquals("75", engine.compile(template).render(context));
        }

        @Test
        @DisplayName("Nested method calls")
        void testNestedMethodCalls() {
            String template = "{text.toUpperCase().substring(0, 5)}";
            Context context = new Context();
            context.put("text", "hello world");
            assertEquals("HELLO", engine.compile(template).render(context));
        }

        @Test
        @DisplayName("Nested member access")
        void testNestedMemberAccess() {
            String template = "{user.profile.name}";
            Context context = new Context();
            Map<String, Object> profile = new HashMap<>();
            profile.put("name", "John");
            Map<String, Object> user = new HashMap<>();
            user.put("profile", profile);
            context.put("user", user);
            assertEquals("John", engine.compile(template).render(context));
        }

        @Test
        @DisplayName("Nested list access")
        void testNestedListAccess() {
            String template = "{users[0].name}";
            Context context = new Context();
            List<Map<String, Object>> users = new ArrayList<>();
            Map<String, Object> user = new HashMap<>();
            user.put("name", "Alice");
            users.add(user);
            context.put("users", users);
            assertEquals("Alice", engine.compile(template).render(context));
        }

        @Test
        @DisplayName("Let with nested structure")
        void testLetWithNestedStructure() {
            String template = "{% let data = {\"name\": \"Test\", \"items\": [1, 2, 3]} %}{data[\"name\"]}";
            assertEquals("Test", engine.compile(template).render(new Context()));
        }

        @Test
        @DisplayName("Lambda in nested context")
        void testLambdaInNestedContext() {
            String template = "{% let multiplier = x -> x * 2 %}{% for i in 1..3 }{multiplier(i)} {/% for }";
            assertEquals("2 4 6 ", engine.compile(template).render(new Context()));
        }

        @Test
        @DisplayName("Complex nested template")
        void testComplexNestedTemplate() {
            String template = "{% for user in users }" +
                    "{user.name}: {% if user.active }Active{% else }Inactive{/% if }\n" +
                    "{% for role in user.roles }  - {role}\n{/% for }" +
                    "{/% for }";
            Context context = new Context();
            List<Map<String, Object>> users = new ArrayList<>();
            Map<String, Object> user = new HashMap<>();
            user.put("name", "Alice");
            user.put("active", true);
            user.put("roles", Arrays.asList("admin", "user"));
            users.add(user);
            context.put("users", users);
            String result = engine.compile(template).render(context);
            assertTrue(result.contains("Alice"));
            assertTrue(result.contains("Active"));
            assertTrue(result.contains("admin"));
        }

        @Test
        @DisplayName("Nested ternary expressions")
        void testNestedTernaryExpressions() {
            String template = "{a > 0 ? (b > 0 ? \"both positive\" : \"a positive\") : (b > 0 ? \"b positive\" : \"both negative\")}";
            Context context = new Context();
            context.put("a", 1);
            context.put("b", 1);
            assertEquals("both positive", engine.compile(template).render(context));
        }

        @Test
        @DisplayName("Nested elvis operators")
        void testNestedElvisOperators() {
            String template = "{a ?? b ?? c ?? \"none\"}";
            Context context = new Context();
            context.put("a", null);
            context.put("b", null);
            context.put("c", "found");
            assertEquals("found", engine.compile(template).render(context));
        }
    }

    // ========== 特性组合测试 ==========

    @Nested
    @DisplayName("Feature Combination Tests")
    class FeatureCombinationTests {

        @Test
        @DisplayName("For loop with let and expression")
        void testForLoopWithLetAndExpression() {
            String template = "{% for i in 1..3 }{% let doubled = i * 2 %}{doubled} {/% for }";
            assertEquals("2 4 6 ", engine.compile(template).render(new Context()));
        }

        @Test
        @DisplayName("If with elvis and ternary")
        void testIfWithElvisAndTernary() {
            String template = "{% if (value ?? 0) > 5 }Large{% else }Small{/% if }";
            Context context = new Context();
            context.put("value", 10);
            assertEquals("Large", engine.compile(template).render(context));
        }

        @Test
        @DisplayName("Switch with method calls")
        void testSwitchWithMethodCalls() {
            String template = "{% switch text.length() }{% case 5 }Five{% case 10 }Ten{% default }Other{/% switch }";
            Context context = new Context();
            context.put("text", "Hello");
            assertEquals("Five", engine.compile(template).render(context));
        }

        @Test
        @DisplayName("Lambda with closure")
        void testLambdaWithClosure() {
            String template = "{% let base = 10 %}{% let add = x -> x + base %}{add(5)}";
            assertEquals("15", engine.compile(template).render(new Context()));
        }

        @Test
        @DisplayName("For with range and condition")
        void testForWithRangeAndCondition() {
            String template = "{% for i in 1..10 }{% if i % 2 == 0 }{i} {/% if }{/% for }";
            String result = engine.compile(template).render(new Context());
            assertTrue(result.contains("2"));
            assertTrue(result.contains("4"));
            assertTrue(result.contains("6"));
        }

        @Test
        @DisplayName("Map iteration with expression")
        void testMapIterationWithExpression() {
            String template = "{% for key in map.keySet() }{key}: {map[key]}\n{/% for }";
            Context context = new Context();
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("a", 1);
            map.put("b", 2);
            context.put("map", map);
            String result = engine.compile(template).render(context);
            assertTrue(result.contains("a: 1"));
            assertTrue(result.contains("b: 2"));
        }

        @Test
        @DisplayName("Nested data structure access")
        void testNestedDataStructureAccess() {
            String template = "{data.items[0].value}";
            Context context = new Context();
            Map<String, Object> item = new HashMap<>();
            item.put("value", 100);
            Map<String, Object> data = new HashMap<>();
            data.put("items", Arrays.asList(item));
            context.put("data", data);
            assertEquals("100", engine.compile(template).render(context));
        }

        @Test
        @DisplayName("Multiple let statements")
        void testMultipleLetStatements() {
            String template = "{% let a = 1 %}{% let b = a + 1 %}{% let c = b + 1 %}{c}";
            assertEquals("3", engine.compile(template).render(new Context()));
        }

        @Test
        @DisplayName("Safe navigation chain")
        void testSafeNavigationChain() {
            String template = "{user?.profile?.name ?? \"Unknown\"}";
            Context context = new Context();
            context.put("user", null);
            assertEquals("Unknown", engine.compile(template).render(context));
        }

        @Test
        @DisplayName("Regex with ternary")
        void testRegexWithTernary() {
            String template = "{email =~ /.*@.*/ ? \"valid\" : \"invalid\"}";
            Context context = new Context();
            context.put("email", "test@example.com");
            assertEquals("valid", engine.compile(template).render(context));
        }

        @Test
        @DisplayName("List operations with for")
        void testListOperationsWithFor() {
            String template = "{% for item in list }{item.toString().toUpperCase()} {/% for }";
            Context context = new Context();
            context.put("list", Arrays.asList("a", "b", "c"));
            assertEquals("A B C ", engine.compile(template).render(context));
        }

        @Test
        @DisplayName("Argument access with elvis")
        void testArgumentAccessWithElvis() {
            String template = "{$0 ?? \"default\"}";
            Context context = new Context();
            context.setArguments((Object) null);
            assertEquals("default", engine.compile(template).render(context));
        }

        @Test
        @DisplayName("Complex expression in condition")
        void testComplexExpressionInCondition() {
            String template = "{% if a > 0 && b > 0 && c > 0 }All positive{/% if }";
            Context context = new Context();
            context.put("a", 1);
            context.put("b", 2);
            context.put("c", 3);
            assertEquals("All positive", engine.compile(template).render(context));
        }

        @Test
        @DisplayName("Dynamic variable in loop")
        void testDynamicVariableInLoop() {
            String template = "{% for key in keys }{data[key]} {/% for }";
            Context context = new Context();
            Map<String, Object> data = new HashMap<>();
            data.put("a", "A");
            data.put("b", "B");
            context.put("data", data);
            context.put("keys", Arrays.asList("a", "b"));
            assertEquals("A B ", engine.compile(template).render(context));
        }
    }

    // ========== 数据结构场景 ==========

    @Nested
    @DisplayName("Data Structure Scenarios")
    class DataStructureScenarios {

        @Test
        @DisplayName("List of maps iteration")
        void testListOfMapsIteration() {
            String template = "{% for user in users }{user.name}: {user.age}\n{/% for }";
            Context context = new Context();
            List<Map<String, Object>> users = new ArrayList<>();
            Map<String, Object> user1 = new HashMap<>();
            user1.put("name", "Alice");
            user1.put("age", 30);
            users.add(user1);
            Map<String, Object> user2 = new HashMap<>();
            user2.put("name", "Bob");
            user2.put("age", 25);
            users.add(user2);
            context.put("users", users);
            String result = engine.compile(template).render(context);
            assertTrue(result.contains("Alice: 30"));
            assertTrue(result.contains("Bob: 25"));
        }

        @Test
        @DisplayName("Nested map access")
        void testNestedMapAccess() {
            String template = "{config.database.host}";
            Context context = new Context();
            Map<String, Object> database = new HashMap<>();
            database.put("host", "localhost");
            Map<String, Object> config = new HashMap<>();
            config.put("database", database);
            context.put("config", config);
            assertEquals("localhost", engine.compile(template).render(context));
        }

        @Test
        @DisplayName("List filtering simulation")
        void testListFilteringSimulation() {
            String template = "{% for item in items }{% if item.active }{item.name} {/% if }{/% for }";
            Context context = new Context();
            List<Map<String, Object>> items = new ArrayList<>();
            Map<String, Object> item1 = new HashMap<>();
            item1.put("name", "Active1");
            item1.put("active", true);
            items.add(item1);
            Map<String, Object> item2 = new HashMap<>();
            item2.put("name", "Inactive");
            item2.put("active", false);
            items.add(item2);
            Map<String, Object> item3 = new HashMap<>();
            item3.put("name", "Active2");
            item3.put("active", true);
            items.add(item3);
            context.put("items", items);
            assertEquals("Active1 Active2 ", engine.compile(template).render(context));
        }

        @Test
        @DisplayName("Map to list conversion")
        void testMapToListConversion() {
            String template = "{% for key in map.keySet() }{key}={map[key]} {/% for }";
            Context context = new Context();
            Map<String, String> map = new LinkedHashMap<>();
            map.put("name", "John");
            map.put("city", "NYC");
            context.put("map", map);
            assertEquals("name=John city=NYC ", engine.compile(template).render(context));
        }

        @Test
        @DisplayName("Deep object graph navigation")
        void testDeepObjectGraphNavigation() {
            String template = "{a.b.c.d.e}";
            Context context = new Context();
            Map<String, Object> d = new HashMap<>();
            d.put("e", "deep_value");
            Map<String, Object> c = new HashMap<>();
            c.put("d", d);
            Map<String, Object> b = new HashMap<>();
            b.put("c", c);
            Map<String, Object> a = new HashMap<>();
            a.put("b", b);
            context.put("a", a);
            assertEquals("deep_value", engine.compile(template).render(context));
        }

        @Test
        @DisplayName("Array access in list")
        void testArrayAccessInList() {
            String template = "{matrix[0][1]}";
            Context context = new Context();
            List<List<Integer>> matrix = new ArrayList<>();
            matrix.add(Arrays.asList(1, 2, 3));
            matrix.add(Arrays.asList(4, 5, 6));
            context.put("matrix", matrix);
            assertEquals("2", engine.compile(template).render(context));
        }

        @Test
        @DisplayName("Mixed type list handling")
        void testMixedTypeListHandling() {
            String template = "{% for item in items }{item} {/% for }";
            Context context = new Context();
            List<Object> items = new ArrayList<>();
            items.add("string");
            items.add(123);
            items.add(true);
            items.add(Arrays.asList("nested"));
            context.put("items", items);
            String result = engine.compile(template).render(context);
            assertTrue(result.contains("string"));
            assertTrue(result.contains("123"));
            assertTrue(result.contains("true"));
        }

        @Test
        @DisplayName("Empty collection handling")
        void testEmptyCollectionHandling() {
            String template = "{% for item in items }{item}{/% for }{items.size()}";
            Context context = new Context();
            context.put("items", new ArrayList<>());
            assertEquals("0", engine.compile(template).render(context));
        }

        @Test
        @DisplayName("Map entry iteration")
        void testMapEntryIteration() {
            String template = "{% for key in map.keySet() }{key}: {map[key].name}\n{/% for }";
            Context context = new Context();
            Map<String, Map<String, Object>> map = new LinkedHashMap<>();
            Map<String, Object> user1 = new HashMap<>();
            user1.put("name", "Alice");
            map.put("user1", user1);
            context.put("map", map);
            assertTrue(engine.compile(template).render(context).contains("user1: Alice"));
        }

        @Test
        @DisplayName("Conditional map access")
        void testConditionalMapAccess() {
            String template = "{map[condition ? \"a\" : \"b\"]}";
            Context context = new Context();
            Map<String, String> map = new HashMap<>();
            map.put("a", "value_a");
            map.put("b", "value_b");
            context.put("map", map);
            context.put("condition", true);
            assertEquals("value_a", engine.compile(template).render(context));
        }
    }

    // ========== 实际应用模式 ==========

    @Nested
    @DisplayName("Real-World Application Patterns")
    class RealWorldApplicationPatterns {

        @Test
        @DisplayName("HTML template rendering")
        void testHtmlTemplateRendering() {
            String template = "<div class=\"user\">\n" +
                    "  <h1>{user.name}</h1>\n" +
                    "  {% if user.admin }<span class=\"badge\">Admin</span>{/% if }\n" +
                    "  <ul>\n" +
                    "  {% for item in user.items }\n" +
                    "    <li>{item}</li>\n" +
                    "  {/% for }\n" +
                    "  </ul>\n" +
                    "</div>";
            Context context = new Context();
            Map<String, Object> user = new HashMap<>();
            user.put("name", "John");
            user.put("admin", true);
            user.put("items", Arrays.asList("Item1", "Item2"));
            context.put("user", user);
            String result = engine.compile(template).render(context);
            assertTrue(result.contains("<h1>John</h1>"));
            assertTrue(result.contains("Admin"));
            assertTrue(result.contains("<li>Item1</li>"));
        }

        @Test
        @DisplayName("JSON template generation")
        void testJsonTemplateGeneration() {
            String template = "{\n" +
                    "  \"name\": \"{user.name}\",\n" +
                    "  \"age\": {user.age},\n" +
                    "  \"active\": {user.active}\n" +
                    "}";
            Context context = new Context();
            Map<String, Object> user = new HashMap<>();
            user.put("name", "John");
            user.put("age", 30);
            user.put("active", true);
            context.put("user", user);
            String result = engine.compile(template).render(context);
            assertTrue(result.contains("\"name\": \"John\""));
            assertTrue(result.contains("\"age\": 30"));
            assertTrue(result.contains("\"active\": true"));
        }

        @Test
        @DisplayName("Configuration file template")
        void testConfigurationFileTemplate() {
            String template = "server:\n" +
                    "  host: {config.host}\n" +
                    "  port: {config.port}\n" +
                    "  {% if config.debug }debug: true{% else }debug: false{/% if }";
            Context context = new Context();
            Map<String, Object> config = new HashMap<>();
            config.put("host", "localhost");
            config.put("port", 8080);
            config.put("debug", true);
            context.put("config", config);
            String result = engine.compile(template).render(context);
            assertTrue(result.contains("host: localhost"));
            assertTrue(result.contains("port: 8080"));
            assertTrue(result.contains("debug: true"));
        }

        @Test
        @DisplayName("Email template")
        void testEmailTemplate() {
            String template = "Dear {user.name},\n\n" +
                    "Thank you for your order.\n\n" +
                    "Order Details:\n" +
                    "{% for item in order.items }- {item.name}: ${item.price}\n{/% for }\n\n" +
                    "Total: ${order.total}\n\n" +
                    "Best regards,\nCompany";
            Context context = new Context();
            Map<String, Object> user = new HashMap<>();
            user.put("name", "Alice");
            List<Map<String, Object>> items = new ArrayList<>();
            Map<String, Object> item = new HashMap<>();
            item.put("name", "Product A");
            item.put("price", 29.99);
            items.add(item);
            Map<String, Object> order = new HashMap<>();
            order.put("items", items);
            order.put("total", 29.99);
            context.put("user", user);
            context.put("order", order);
            String result = engine.compile(template).render(context);
            assertTrue(result.contains("Dear Alice"));
            assertTrue(result.contains("Product A"));
            assertTrue(result.contains("29.99"));
        }

        @Test
        @DisplayName("Report generation")
        void testReportGeneration() {
            String template = "=== Sales Report ===\n\n" +
                    "{% for region in regions }Region: {region.name}\n" +
                    "  Sales: {region.sales}\n" +
                    "  {% if region.growth > 0 }Growth: +{region.growth}{% else }Growth: {region.growth}{/% if }\n\n" +
                    "{/% for }";
            Context context = new Context();
            List<Map<String, Object>> regions = new ArrayList<>();
            Map<String, Object> region = new HashMap<>();
            region.put("name", "North");
            region.put("sales", 10000);
            region.put("growth", 15);
            regions.add(region);
            context.put("regions", regions);
            String result = engine.compile(template).render(context);
            assertTrue(result.contains("Region: North"));
            assertTrue(result.contains("Sales: 10000"));
            assertTrue(result.contains("Growth: +15"));
        }

        @Test
        @DisplayName("Table generation")
        void testTableGeneration() {
            String template = "| Name | Status |\n|------|--------|\n" +
                    "{% for row in rows }| {row.name} | {row.status} |\n{/% for }";
            Context context = new Context();
            List<Map<String, Object>> rows = new ArrayList<>();
            Map<String, Object> row1 = new HashMap<>();
            row1.put("name", "Task1");
            row1.put("status", "Done");
            rows.add(row1);
            Map<String, Object> row2 = new HashMap<>();
            row2.put("name", "Task2");
            row2.put("status", "Pending");
            rows.add(row2);
            context.put("rows", rows);
            String result = engine.compile(template).render(context);
            assertTrue(result.contains("| Task1 | Done |"));
            assertTrue(result.contains("| Task2 | Pending |"));
        }

        @Test
        @DisplayName("Internationalization template")
        void testInternationalizationTemplate() {
            String template = "{greeting}, {name}!\n" +
                    "{% if count == 1 }You have 1 message.{% else }You have {count} messages.{/% if }";
            Context context = new Context();
            context.put("greeting", "Hello");
            context.put("name", "World");
            context.put("count", 5);
            String result = engine.compile(template).render(context);
            assertTrue(result.contains("Hello, World!"));
            assertTrue(result.contains("You have 5 messages."));
        }

        @Test
        @DisplayName("Conditional class names")
        void testConditionalClassNames() {
            String template = "<div class=\"item {% if active }active{% else }inactive{/% if }\">{content}</div>";
            Context context = new Context();
            context.put("active", true);
            context.put("content", "Test");
            String result = engine.compile(template).render(context);
            assertTrue(result.contains("class=\"item active\""));
            assertTrue(result.contains("Test"));
        }

        @Test
        @DisplayName("Pagination template")
        void testPaginationTemplate() {
            String template = "Page {currentPage} of {totalPages}\n" +
                    "{% for i in 1..5 }{% if i == currentPage }[{i}]{% else }{i}{/% if } {/% for }";
            Context context = new Context();
            context.put("currentPage", 3);
            context.put("totalPages", 10);
            String result = engine.compile(template).render(context);
            assertTrue(result.contains("Page 3 of 10"));
            assertTrue(result.contains("[3]"));
        }

        @Test
        @DisplayName("Error message template")
        void testErrorMessageTemplate() {
            String template = "Error: {error.code} - {error.message}\n" +
                    "{% if error.details }Details: {error.details}{/% if }";
            Context context = new Context();
            Map<String, Object> error = new HashMap<>();
            error.put("code", 404);
            error.put("message", "Not Found");
            error.put("details", "The requested resource does not exist.");
            context.put("error", error);
            String result = engine.compile(template).render(context);
            assertTrue(result.contains("Error: 404 - Not Found"));
            assertTrue(result.contains("Details: The requested resource does not exist."));
        }

        @Test
        @DisplayName("Menu generation")
        void testMenuGeneration() {
            String template = "<nav>\n" +
                    "{% for item in menu }\n" +
                    "  <a href=\"{item.url}\"{% if item.active } class=\"active\"{/% if }>{item.label}</a>\n" +
                    "{/% for }\n" +
                    "</nav>";
            Context context = new Context();
            List<Map<String, Object>> menu = new ArrayList<>();
            Map<String, Object> item1 = new HashMap<>();
            item1.put("url", "/home");
            item1.put("label", "Home");
            item1.put("active", true);
            menu.add(item1);
            Map<String, Object> item2 = new HashMap<>();
            item2.put("url", "/about");
            item2.put("label", "About");
            item2.put("active", false);
            menu.add(item2);
            context.put("menu", menu);
            String result = engine.compile(template).render(context);
            assertTrue(result.contains("<a href=\"/home\" class=\"active\">Home</a>"));
            assertTrue(result.contains("<a href=\"/about\">About</a>"));
        }
    }
}