package org.dromara.easytl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TemplateVariable 集成测试
 * <p>
 * 测试 TemplateVariable 与 TemplateEngine 的集成使用
 * </p>
 */
@DisplayName("TemplateVariable Integration Tests")
class TemplateVariableIntegrationTest {

    private final TemplateEngine engine = new TemplateEngine();

    @Nested
    @DisplayName("Template Rendering Tests")
    class TemplateRenderingTests {

        @Test
        @DisplayName("TemplateVariable in single brace expression")
        void testInSingleBraceExpression() {
            Context context = new Context();
            context.put("name", "World");
            context.put("greeting", ctx -> "Hello, " + ctx.get("name"));

            Template template = engine.compile("Message: {greeting}");
            String result = template.render(context);

            assertEquals("Message: Hello, World", result);
        }

        @Test
        @DisplayName("TemplateVariable in double brace expression")
        void testInDoubleBraceExpression() {
            Context context = new Context();
            context.put("user", "张三");
            context.put("welcome", ctx -> "欢迎, " + ctx.get("user") + "!");

            Template template = engine.compile("{{welcome}}");
            String result = template.render(context);

            assertEquals("欢迎, 张三!", result);
        }

        @Test
        @DisplayName("TemplateVariable in dollar brace expression")
        void testInDollarBraceExpression() {
            Context context = new Context();
            context.put("item", "商品A");
            context.put("price", ctx -> "价格: 100元");

            Template template = engine.compile("商品: ${item}, ${price}");
            String result = template.render(context);

            assertEquals("商品: 商品A, 价格: 100元", result);
        }
    }

    @Nested
    @DisplayName("Complex Expression Tests")
    class ComplexExpressionTests {

        @Test
        @DisplayName("TemplateVariable in arithmetic expression")
        void testInArithmeticExpression() {
            Context context = new Context();
            context.put("base", 10);
            context.put("bonus", ctx -> (Integer) ctx.get("base") * 2);

            Template template = engine.compile("Total: {base + bonus}");
            String result = template.render(context);

            assertEquals("Total: 30", result);
        }

        @Test
        @DisplayName("TemplateVariable as method parameter")
        void testTemplateVariableAsMethodParameter() {
            Context context = new Context();
            context.put("text", "hello world");
            context.put("upperText", ctx -> ((String) ctx.get("text")).toUpperCase());

            Template template = engine.compile("Result: {upperText}");
            String result = template.render(context);

            assertEquals("Result: HELLO WORLD", result);
        }

        @Test
        @DisplayName("TemplateVariable with conditional expression")
        void testWithConditionalExpression() {
            Context context = new Context();
            context.put("age", 20);
            context.put("status", ctx -> {
                int age = (Integer) ctx.get("age");
                return age >= 18 ? "成年" : "未成年";
            });

            Template template = engine.compile("年龄: {age}, 状态: {status}");
            String result = template.render(context);

            assertEquals("年龄: 20, 状态: 成年", result);
        }

        @Test
        @DisplayName("TemplateVariable in ternary expression")
        void testInTernaryExpression() {
            Context context = new Context();
            context.put("score", 85);
            context.put("grade", ctx -> {
                int s = (Integer) ctx.get("score");
                return s >= 90 ? "A" : s >= 80 ? "B" : s >= 70 ? "C" : "D";
            });

            Template template = engine.compile("分数: {score}, 等级: {grade}");
            String result = template.render(context);

            assertEquals("分数: 85, 等级: B", result);
        }
    }

    @Nested
    @DisplayName("Real World Scenario Tests")
    class RealWorldScenarioTests {

        @Test
        @DisplayName("Dynamic user display name generation")
        void testDynamicUserDisplayNameGeneration() {
            Context context = new Context();

            // 模拟用户对象
            User user = new User("张", "三", "zhangsan@example.com");
            context.put("user", user);

            // 动态变量：生成用户显示名称
            context.put("displayName", ctx -> {
                User u = (User) ctx.get("user");
                return u.getFirstName() + " " + u.getLastName();
            });

            // 动态变量：生成邮箱掩码
            context.put("maskedEmail", ctx -> {
                User u = (User) ctx.get("user");
                String email = u.getEmail();
                int atIndex = email.indexOf('@');
                return email.substring(0, Math.min(3, atIndex)) + "***" + email.substring(atIndex);
            });

            Template template = engine.compile("用户: {displayName}, 邮箱: {maskedEmail}");
            String result = template.render(context);

            assertEquals("用户: 张 三, 邮箱: zha***@example.com", result);
        }

        @Test
        @DisplayName("Dynamic order total calculation")
        void testDynamicOrderTotalCalculation() {
            Context context = new Context();

            // 模拟订单项
            context.put("items", java.util.Arrays.asList(
                new OrderItem("商品A", 100, 2),
                new OrderItem("商品B", 200, 1)
            ));

            // 动态变量：计算订单总价
            context.put("orderTotal", ctx -> {
                @SuppressWarnings("unchecked")
                java.util.List<OrderItem> items = (java.util.List<OrderItem>) ctx.get("items");
                int total = 0;
                for (OrderItem item : items) {
                    total += item.getPrice() * item.getQuantity();
                }
                return total;
            });

            Template template = engine.compile("订单总价: {orderTotal}元");
            String result = template.render(context);

            assertEquals("订单总价: 400元", result);
        }

        @Test
        @DisplayName("Dynamic date formatting")
        void testDynamicDateFormatting() {
            Context context = new Context();

            java.util.Date now = new java.util.Date();
            context.put("timestamp", now);

            // 动态变量：格式化日期
            context.put("formattedDate", ctx -> {
                java.util.Date date = (java.util.Date) ctx.get("timestamp");
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
                return sdf.format(date);
            });

            Template template = engine.compile("日期: {formattedDate}");
            String result = template.render(context);

            assertTrue(result.startsWith("日期: "));
            assertTrue(result.matches("日期: \\d{4}-\\d{2}-\\d{2}"));
        }

        @Test
        @DisplayName("Template variable with for loop")
        void testTemplateVariableWithForLoop() {
            Context context = new Context();

            context.put("items", java.util.Arrays.asList("苹果", "香蕉", "橙子"));
            context.put("itemCount", ctx -> {
                @SuppressWarnings("unchecked")
                java.util.List<?> items = (java.util.List<?>) ctx.get("items");
                return items.size();
            });

            Template template = engine.compile("共有 {itemCount} 种水果: {% for item in items }{item} {/% for }");
            String result = template.render(context);

            assertEquals("共有 3 种水果: 苹果 香蕉 橙子 ", result);
        }

        @Test
        @DisplayName("Template variable with if statement")
        void testTemplateVariableWithIfStatement() {
            Context context = new Context();

            context.put("user", new User("张", "三", "admin@example.com"));
            context.put("isAdmin", ctx -> {
                User u = (User) ctx.get("user");
                return u.getEmail().startsWith("admin");
            });

            Template template = engine.compile("{% if isAdmin }管理员{/% if }");
            String result = template.render(context);

            assertEquals("管理员", result);
        }
    }

    @Nested
    @DisplayName("Multiple Access Tests")
    class MultipleAccessTests {

        @Test
        @DisplayName("Multiple accesses in same template execute each time")
        void testMultipleAccessesInSameTemplate() {
            java.util.concurrent.atomic.AtomicInteger counter = new java.util.concurrent.atomic.AtomicInteger(0);

            Context context = new Context();
            context.put("counter", ctx -> counter.incrementAndGet());

            Template template = engine.compile("第1次: {counter}, 第2次: {counter}, 第3次: {counter}");
            String result = template.render(context);

            assertEquals("第1次: 1, 第2次: 2, 第3次: 3", result);
        }

        @Test
        @DisplayName("Multiple renders execute TemplateVariable each time")
        void testMultipleRendersExecuteEachTime() {
            java.util.concurrent.atomic.AtomicInteger counter = new java.util.concurrent.atomic.AtomicInteger(0);

            Context context = new Context();
            context.put("counter", ctx -> counter.incrementAndGet());

            Template template = engine.compile("计数: {counter}");

            assertEquals("计数: 1", template.render(context));
            assertEquals("计数: 2", template.render(context));
            assertEquals("计数: 3", template.render(context));
        }
    }

    // Test helper classes

    static class User {
        private final String firstName;
        private final String lastName;
        private final String email;

        User(String firstName, String lastName, String email) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.email = email;
        }

        public String getFirstName() {
            return firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public String getEmail() {
            return email;
        }
    }

    static class OrderItem {
        private final String name;
        private final int price;
        private final int quantity;

        OrderItem(String name, int price, int quantity) {
            this.name = name;
            this.price = price;
            this.quantity = quantity;
        }

        public String getName() {
            return name;
        }

        public int getPrice() {
            return price;
        }

        public int getQuantity() {
            return quantity;
        }
    }
}