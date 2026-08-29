package org.dromara.easytl.examples.errorhandling;

import org.dromara.easytl.Context;
import org.dromara.easytl.Template;
import org.dromara.easytl.TemplateEngine;
import org.dromara.easytl.exception.TemplateCompileException;
import org.dromara.easytl.exception.TemplateRuntimeException;

/**
 * 错误处理示例 - 语法错误处理、运行时错误处理
 * <p>
 * 本类演示 EasyTL 模板引擎的错误处理机制：
 * <ul>
 *   <li>编译时错误 - 模板语法错误</li>
 *   <li>运行时错误 - 模板执行错误</li>
 *   <li>错误捕获与处理 - 如何优雅地处理错误</li>
 * </ul>
 * </p>
 *
 * @author EasyTL Team
 * @since 1.0.0
 */
public class ErrorHandlingExamples {

    /**
     * 运行所有错误处理示例
     */
    public static void runAll() {
        syntaxErrors();
        runtimeErrors();
        errorHandlingBestPractices();
        nullHandling();
    }

    // ==================== 语法错误示例 ====================

    /**
     * 示例1：语法错误处理
     * <p>
     * 编译时错误由 {@link TemplateCompileException} 表示，
     * 通常是由于模板语法不正确导致。
     * <p>
     * 常见语法错误：
     * <ul>
     *   <li>未闭合的表达式: {name</li>
     *   <li>未闭合的语句块: {% if true %}... (缺少 {/% if })</li>
     *   <li>无效的表达式: {x + }</li>
     *   <li>错误的语句语法: {% for in list %}</li>
     * </ul>
     * </p>
     */
    public static void syntaxErrors() {
        System.out.println("=== 1. 语法错误示例 ===");

        TemplateEngine engine = new TemplateEngine();

        // 1.1 未闭合的表达式
        System.out.println("\n--- 未闭合表达式 ---");
        try {
            engine.compile("{name");
        } catch (TemplateCompileException e) {
            System.out.println("错误类型: 编译时错误");
            System.out.println("错误信息: " + e.getMessage());
        }

        // 1.2 未闭合的语句块
        System.out.println("\n--- 未闭合语句块 ---");
        try {
            engine.compile("{% if true %}content");
        } catch (TemplateCompileException e) {
            System.out.println("错误类型: 编译时错误");
            System.out.println("错误信息: " + e.getMessage());
        }

        // 1.3 无效的表达式语法
        System.out.println("\n--- 无效表达式 ---");
        try {
            engine.compile("{x + }");
        } catch (TemplateCompileException e) {
            System.out.println("错误类型: 编译时错误");
            System.out.println("错误信息: " + e.getMessage());
        }

        // 1.4 错误的 for 语句语法
        System.out.println("\n--- 错误的 for 语句 ---");
        try {
            engine.compile("{% for in list %}{/% for }");
        } catch (TemplateCompileException e) {
            System.out.println("错误类型: 编译时错误");
            System.out.println("错误信息: " + e.getMessage());
        }

        // 1.5 错误的 if 语句语法
        System.out.println("\n--- 错误的 if 语句 ---");
        try {
            engine.compile("{% if }content{/% if }");
        } catch (TemplateCompileException e) {
            System.out.println("错误类型: 编译时错误");
            System.out.println("错误信息: " + e.getMessage());
        }

        // 1.6 未闭合的三元表达式
        System.out.println("\n--- 未闭合三元表达式 ---");
        try {
            engine.compile("{x ? y}");
        } catch (TemplateCompileException e) {
            System.out.println("错误类型: 编译时错误");
            System.out.println("错误信息: " + e.getMessage());
        }

        System.out.println();
    }

    // ==================== 运行时错误示例 ====================

    /**
     * 示例2：运行时错误处理
     * <p>
     * 运行时错误由 {@link TemplateRuntimeException} 表示，
     * 在模板执行过程中发生。
     * <p>
     * 常见运行时错误：
     * <ul>
     *   <li>方法调用失败: 调用不存在的方法</li>
     *   <li>类型转换错误: 对非数字进行算术运算</li>
     *   <li>空指针异常: 访问 null 对象的属性</li>
     * </ul>
     * </p>
     */
    public static void runtimeErrors() {
        System.out.println("=== 2. 运行时错误示例 ===");

        TemplateEngine engine = new TemplateEngine();

        // 2.1 方法调用失败
        System.out.println("\n--- 方法调用失败 ---");
        try {
            Template template = engine.compile("{obj.nonExistentMethod()}");
            Context context = new Context();
            context.put("obj", "test string");
            template.render(context);
        } catch (TemplateRuntimeException e) {
            System.out.println("错误类型: 运行时错误");
            System.out.println("错误信息: " + e.getMessage());
        }

        // 2.2 类型转换错误
        System.out.println("\n--- 类型转换错误 ---");
        try {
            Template template = engine.compile("{text + 1}");
            Context context = new Context();
            context.put("text", "hello");
            template.render(context);
        } catch (TemplateRuntimeException e) {
            System.out.println("错误类型: 运行时错误");
            System.out.println("错误信息: " + e.getMessage());
        }

        // 2.3 除零错误
        System.out.println("\n--- 除零错误 ---");
        try {
            Template template = engine.compile("{a / b}");
            Context context = new Context();
            context.put("a", 10);
            context.put("b", 0);
            template.render(context);
        } catch (TemplateRuntimeException e) {
            System.out.println("错误类型: 运行时错误");
            System.out.println("错误信息: " + e.getMessage());
        }

        System.out.println();
    }

    // ==================== 错误处理最佳实践 ====================

    /**
     * 示例3：错误处理最佳实践
     * <p>
     * 演示如何优雅地处理模板引擎中的错误
     * </p>
     */
    public static void errorHandlingBestPractices() {
        System.out.println("=== 3. 错误处理最佳实践 ===");

        TemplateEngine engine = new TemplateEngine();

        // 3.1 安全的模板编译
        System.out.println("\n--- 安全的模板编译 ---");
        String template = "Hello, {name}!";
        Template compiled = safeCompile(engine, template);

        if (compiled != null) {
            Context context = new Context();
            context.put("name", "World");
            String result = safeRender(compiled, context);
            System.out.println("渲染结果: " + result);
        }

        // 3.2 安全的模板编译（错误情况）
        System.out.println("\n--- 安全的模板编译（错误情况） ---");
        String badTemplate = "{name";
        Template badCompiled = safeCompile(engine, badTemplate);
        System.out.println("错误模板编译结果: " + (badCompiled != null ? "成功" : "失败"));

        // 3.3 使用默认值避免错误 - 使用 ?? 语法和 ?. 安全导航
        System.out.println("\n--- 使用默认值避免错误 ---");
        String templateWithDefault = "名称: {name ?? '未知'}, 年龄: {user?.age ?? 0}";
        Template compiledWithDefault = engine.compile(templateWithDefault);

        Context context = new Context();
        // 不设置任何变量
        String result = compiledWithDefault.render(context);
        System.out.println("使用默认值结果: " + result);

        // 3.4 条件检查避免错误
        System.out.println("\n--- 条件检查避免错误 ---");
        String templateWithCheck = "{% if user != null && user.name != null %}{user.name}{% else %}未知用户{/% if }";
        Template compiledWithCheck = engine.compile(templateWithCheck);

        Context contextWithNull = new Context();
        contextWithNull.put("user", null);
        String resultWithCheck = compiledWithCheck.render(contextWithNull);
        System.out.println("条件检查结果: " + resultWithCheck);

        System.out.println();
    }

    // ==================== 空值处理示例 ====================

    /**
     * 示例4：空值处理
     * <p>
     * 演示如何正确处理 null 值
     * </p>
     */
    public static void nullHandling() {
        System.out.println("=== 4. 空值处理示例 ===");

        TemplateEngine engine = new TemplateEngine();

        // 4.1 null 变量渲染为空字符串
        System.out.println("\n--- null 变量渲染 ---");
        String template1 = "值: [{value}]";
        Template compiled1 = engine.compile(template1);

        Context context1 = new Context();
        context1.put("value", null);
        System.out.println("null 值渲染结果: " + compiled1.render(context1));

        // 4.2 不存在的变量渲染为空字符串
        System.out.println("\n--- 不存在的变量 ---");
        Context context2 = new Context();
        // 不设置 anyVariable
        String template2 = "不存在的变量: [{anyVariable}]";
        Template compiled2 = engine.compile(template2);
        System.out.println("不存在变量渲染结果: " + compiled2.render(context2));

        // 4.3 Elvis 运算符处理 null - 使用 ?? 语法
        System.out.println("\n--- Elvis 运算符 ---");
        String template3 = "名称: {name ?? '默认名称'}";
        Template compiled3 = engine.compile(template3);

        Context context3 = new Context();
        context3.put("name", null);
        System.out.println("Elvis null 处理: " + compiled3.render(context3));

        // 4.4 安全导航运算符 (?.) 和 Elvis 运算符组合
        System.out.println("\n--- 安全导航运算符 ---");
        String template4 = "城市: {user?.address?.city ?? '未知'}";
        Template compiled4 = engine.compile(template4);

        Context context4 = new Context();
        context4.put("user", null);
        System.out.println("安全导航 null 对象: " + compiled4.render(context4));

        Context context5 = new Context();
        java.util.Map<String, Object> user = new java.util.HashMap<>();
        java.util.Map<String, Object> address = new java.util.HashMap<>();
        address.put("city", "北京");
        user.put("address", address);
        context5.put("user", user);
        System.out.println("安全导航有效对象: " + compiled4.render(context5));

        System.out.println();
    }

    /**
     * 安全编译模板
     *
     * @param engine   模板引擎
     * @param template 模板字符串
     * @return 编译后的模板，如果失败返回 null
     */
    private static Template safeCompile(TemplateEngine engine, String template) {
        try {
            return engine.compile(template);
        } catch (TemplateCompileException e) {
            System.err.println("编译错误: " + e.getMessage());
            return null;
        }
    }

    /**
     * 安全渲染模板
     *
     * @param template 模板对象
     * @param context  上下文
     * @return 渲染结果，如果失败返回错误信息
     */
    private static String safeRender(Template template, Context context) {
        try {
            return template.render(context);
        } catch (TemplateRuntimeException e) {
            return "渲染错误: " + e.getMessage();
        }
    }
}