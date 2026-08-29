package org.dromara.easytl;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 真实场景集成测试（task-real-scenario-testing）
 * <p>
 * 验证模板引擎在邮件、HTML、SQL、日志、配置、报表等实际场景中的可用性。
 * </p>
 *
 * @author EasyTL Team
 * @since 1.0.0
 */
class RealScenarioIntegrationTest {

    // ==================== AC 1: 邮件模板（subtask-real-email）====================

    /** 邮件模板：收件人、主题、正文等动态内容 */
    @Test
    void testEmailTemplateBasic() {
        String template = "To: {to}\nFrom: {from}\nSubject: {subject}\n\n{body}";
        TemplateEngine engine = new TemplateEngine();
        Template t = engine.compile(template);
        Context ctx = new Context();
        ctx.put("to", "user@example.com");
        ctx.put("from", "noreply@example.com");
        ctx.put("subject", "Welcome");
        ctx.put("body", "Hello, thank you for signing up.");
        String result = t.render(ctx);
        assertTrue(result.contains("user@example.com"));
        assertTrue(result.contains("Welcome"));
        assertTrue(result.contains("Hello, thank you for signing up."));
    }

    /** 邮件模板：HTML 正文与附件列表（占位输出） */
    @Test
    void testEmailTemplateWithHtmlAndAttachments() {
        String template = "Subject: {subject}\nContent-Type: text/html\n\n{htmlBody}\nAttachments: {attachments}";
        TemplateEngine engine = new TemplateEngine();
        Template t = engine.compile(template);
        Context ctx = new Context();
        ctx.put("subject", "Report");
        ctx.put("htmlBody", "<p>Hello, Alice</p>");
        ctx.put("attachments", Arrays.asList("file1.pdf", "file2.xlsx"));
        String result = t.render(ctx);
        assertTrue(result.contains("Report"));
        assertTrue(result.contains("<p>Hello, Alice</p>"));
        assertTrue(result.contains("file1.pdf"));
        assertTrue(result.contains("file2.xlsx"));
    }

    // ==================== AC 2: HTML 模板（subtask-real-html）====================

    /** HTML 模板：用户信息与产品列表循环 */
    @Test
    void testHtmlTemplateUserAndProductList() {
        String template = "<html><body><h1>Hello, {user.name}!</h1><ul>{% for item in products }<li>{item.name}: {item.price}</li>{/% for }</ul></body></html>";
        TemplateEngine engine = new TemplateEngine();
        Template t = engine.compile(template);
        Context ctx = new Context();
        ctx.put("user", new SimpleUser("Alice"));
        ctx.put("products", Arrays.asList(
            new SimpleProduct("Book", "29.99"),
            new SimpleProduct("Pen", "2.50")
        ));
        String result = t.render(ctx);
        assertTrue(result.contains("Hello, Alice!"));
        assertTrue(result.contains("Book"));
        assertTrue(result.contains("29.99"));
        assertTrue(result.contains("Pen"));
        assertTrue(result.contains("2.50"));
    }

    /** HTML 模板：条件渲染（登录状态） */
    @Test
    void testHtmlTemplateConditionalLogin() {
        String template = "{% if loggedIn }<span>Welcome, {user.name}</span>{% else }<span>Please login</span>{/% if }";
        TemplateEngine engine = new TemplateEngine();
        Template t = engine.compile(template);
        Context ctx = new Context();
        ctx.put("loggedIn", true);
        ctx.put("user", new SimpleUser("Bob"));
        String result = t.render(ctx);
        assertTrue(result.contains("Welcome, Bob"));
        ctx.put("loggedIn", false);
        result = t.render(ctx);
        assertTrue(result.contains("Please login"));
    }

    // ==================== AC 3: SQL 模板（subtask-real-sql）====================

    /** SQL 模板：动态表名、字段、WHERE、ORDER BY、LIMIT */
    @Test
    void testSqlTemplateDynamicQuery() {
        String template = "SELECT {columns} FROM {tableName} WHERE status = {status} ORDER BY {orderBy} LIMIT {limit}";
        TemplateEngine engine = new TemplateEngine();
        Template t = engine.compile(template);
        Context ctx = new Context();
        ctx.put("columns", "id, name, created_at");
        ctx.put("tableName", "users");
        ctx.put("status", "'active'");
        ctx.put("orderBy", "created_at DESC");
        ctx.put("limit", 10);
        String result = t.render(ctx);
        assertTrue(result.contains("SELECT id, name, created_at FROM users"));
        assertTrue(result.contains("WHERE status = 'active'"));
        assertTrue(result.contains("ORDER BY created_at DESC"));
        assertTrue(result.contains("LIMIT 10"));
    }

    // ==================== AC 4: 日志模板（subtask-real-log）====================

    /** 日志模板：时间戳、级别、消息 */
    @Test
    void testLogTemplateFormat() {
        String template = "[{timestamp}] {level} - {message}";
        TemplateEngine engine = new TemplateEngine();
        Template t = engine.compile(template);
        Context ctx = new Context();
        ctx.put("timestamp", "2026-01-30T10:00:00");
        ctx.put("level", "INFO");
        ctx.put("message", "User login success");
        String result = t.render(ctx);
        assertTrue(result.contains("2026-01-30T10:00:00"));
        assertTrue(result.contains("INFO"));
        assertTrue(result.contains("User login success"));
    }

    /** 日志模板：多行消息与异常信息占位 */
    @Test
    void testLogTemplateMultilineAndException() {
        String template = "{level}: {message}\nException: {exception}";
        TemplateEngine engine = new TemplateEngine();
        Template t = engine.compile(template);
        Context ctx = new Context();
        ctx.put("level", "ERROR");
        ctx.put("message", "Connection failed");
        ctx.put("exception", "java.net.ConnectException: Connection refused");
        String result = t.render(ctx);
        assertTrue(result.contains("ERROR"));
        assertTrue(result.contains("Connection failed"));
        assertTrue(result.contains("ConnectException"));
    }

    // ==================== AC 5: 配置文件模板（subtask-real-config）====================

    /** 配置文件模板：key=value、环境、条件 */
    @Test
    void testConfigTemplateProperties() {
        String template = "app.name={appName}\nenv={env}\n{% if production }debug=false{% else }debug=true{/% if }";
        TemplateEngine engine = new TemplateEngine();
        Template t = engine.compile(template);
        Context ctx = new Context();
        ctx.put("appName", "EasyTL");
        ctx.put("env", "dev");
        ctx.put("production", false);
        String result = t.render(ctx);
        assertTrue(result.contains("app.name=EasyTL"));
        assertTrue(result.contains("env=dev"));
        assertTrue(result.contains("debug=true"));
    }

    /** 配置文件模板：嵌套结构（模拟） */
    @Test
    void testConfigTemplateNested() {
        String template = "server:\n  host: {server.host}\n  port: {server.port}";
        TemplateEngine engine = new TemplateEngine();
        Template t = engine.compile(template);
        Context ctx = new Context();
        Map<String, Object> server = new HashMap<>();
        server.put("host", "localhost");
        server.put("port", 8080);
        ctx.put("server", server);
        String result = t.render(ctx);
        assertTrue(result.contains("localhost"));
        assertTrue(result.contains("8080"));
    }

    // ==================== AC 6: 报表模板（subtask-real-report）====================

    /** 报表模板：表头、数据行循环、合计 */
    @Test
    void testReportTemplateTableWithSum() {
        String template = "<table><tr><th>Name</th><th>Amount</th></tr>{% for row in rows }<tr><td>{row.name}</td><td>{row.amount}</td></tr>{/% for }<tr><td>Total</td><td>{total}</td></tr></table>";
        TemplateEngine engine = new TemplateEngine();
        Template t = engine.compile(template);
        Context ctx = new Context();
        ctx.put("rows", Arrays.asList(
            mapOf("name", "A", "amount", 100),
            mapOf("name", "B", "amount", 200)
        ));
        ctx.put("total", 300);
        String result = t.render(ctx);
        assertTrue(result.contains("<table>"));
        assertTrue(result.contains("A"));
        assertTrue(result.contains("100"));
        assertTrue(result.contains("B"));
        assertTrue(result.contains("200"));
        assertTrue(result.contains("Total"));
        assertTrue(result.contains("300"));
    }

    private static Map<String, Object> mapOf(String k1, Object v1, String k2, Object v2) {
        Map<String, Object> m = new HashMap<>();
        m.put(k1, v1);
        m.put(k2, v2);
        return m;
    }

    /** 简单用户对象（name） */
    public static class SimpleUser {
        private final String name;
        public SimpleUser(String name) { this.name = name; }
        public String getName() { return name; }
    }

    /** 简单产品对象（name, price） */
    public static class SimpleProduct {
        private final String name;
        private final String price;
        public SimpleProduct(String name, String price) { this.name = name; this.price = price; }
        public String getName() { return name; }
        public String getPrice() { return price; }
    }
}
