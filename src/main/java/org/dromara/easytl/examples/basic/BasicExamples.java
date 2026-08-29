package org.dromara.easytl.examples.basic;

import org.dromara.easytl.Context;
import org.dromara.easytl.Template;
import org.dromara.easytl.TemplateEngine;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 基础示例 - 变量替换、表达式计算、条件语句、循环语句
 * <p>
 * 本类演示 EasyTL 模板引擎的基本用法，包括：
 * <ul>
 *   <li>变量替换 - 使用 {variable} 语法</li>
 *   <li>表达式计算 - 算术运算、比较运算、逻辑运算</li>
 *   <li>条件语句 - if/else if/else 语句块</li>
 *   <li>循环语句 - for 循环语句块</li>
 * </ul>
 * </p>
 *
 * @author EasyTL Team
 * @since 1.0.0
 */
public class BasicExamples {

    /**
     * 运行所有基础示例
     */
    public static void runAll() {
        variableSubstitution();
        expressionCalculation();
        conditionalStatements();
        loopStatements();
        switchStatements();
        letStatements();
    }

    // ==================== 变量替换示例 ====================

    /**
     * 示例1：变量替换
     * <p>
     * EasyTL 支持三种表达式语法：
     * <ul>
     *   <li>{expr} - 单花括号语法（默认）</li>
     *   <li>{{expr}} - 双花括号语法</li>
     *   <li>${expr} - 美元符号语法</li>
     * </ul>
     * </p>
     */
    public static void variableSubstitution() {
        System.out.println("=== 1. 变量替换示例 ===");

        TemplateEngine engine = new TemplateEngine();

        // 1.1 基本变量替换
        String template1 = "Hello, {name}! Welcome to {city}.";
        Template compiled1 = engine.compile(template1);

        Context context1 = new Context();
        context1.put("name", "张三");
        context1.put("city", "北京");

        String result1 = compiled1.render(context1);
        System.out.println("基本变量替换: " + result1);

        // 1.2 属性访问
        String template2 = "用户: {user.name}, 年龄: {user.age}, 城市: {user.address.city}";
        Template compiled2 = engine.compile(template2);

        Map<String, Object> address = new HashMap<>();
        address.put("city", "上海");

        Map<String, Object> user = new HashMap<>();
        user.put("name", "李四");
        user.put("age", 28);
        user.put("address", address);

        Context context2 = new Context();
        context2.put("user", user);

        String result2 = compiled2.render(context2);
        System.out.println("属性访问: " + result2);

        // 1.3 不存在的变量返回空字符串
        String template3 = "Name: {name}, Missing: {missing}";
        Template compiled3 = engine.compile(template3);

        Context context3 = new Context();
        context3.put("name", "王五");

        String result3 = compiled3.render(context3);
        System.out.println("不存在变量: " + result3);

        System.out.println();
    }

    // ==================== 表达式计算示例 ====================

    /**
     * 示例2：表达式计算
     * <p>
     * EasyTL 支持多种运算：
     * <ul>
     *   <li>算术运算: +, -, *, /, %</li>
     *   <li>比较运算: ==, !=, <, >, <=, >=</li>
     *   <li>逻辑运算: &&, ||, !</li>
     *   <li>三元运算: condition ? trueValue : falseValue</li>
     *   <li>Elvis 运算: value ?? defaultValue</li>
     * </ul>
     * </p>
     */
    public static void expressionCalculation() {
        System.out.println("=== 2. 表达式计算示例 ===");

        TemplateEngine engine = new TemplateEngine();

        // 2.1 算术运算
        String template1 = "加法: {a + b}, 减法: {a - b}, 乘法: {a * b}, 除法: {a / b}, 取余: {a % b}";
        Template compiled1 = engine.compile(template1);

        Context context1 = new Context();
        context1.put("a", 10);
        context1.put("b", 3);

        String result1 = compiled1.render(context1);
        System.out.println("算术运算: " + result1);

        // 2.2 比较运算
        String template2 = "相等: {a == b}, 不等: {a != b}, 大于: {a > b}, 小于: {a < b}";
        Template compiled2 = engine.compile(template2);

        String result2 = compiled2.render(context1);
        System.out.println("比较运算: " + result2);

        // 2.3 逻辑运算
        String template3 = "与运算: {a > 0 && b > 0}, 或运算: {a > 0 || b < 0}, 非运算: {!(a > b)}";
        Template compiled3 = engine.compile(template3);

        String result3 = compiled3.render(context1);
        System.out.println("逻辑运算: " + result3);

        // 2.4 三元运算符
        String template4 = "成人: {age >= 18 ? '是' : '否'}";
        Template compiled4 = engine.compile(template4);

        Context context4 = new Context();
        context4.put("age", 20);

        String result4 = compiled4.render(context4);
        System.out.println("三元运算: " + result4);

        // 2.5 Elvis 运算符 (空值合并) - 使用 ?? 语法
        String template5 = "名称: {name ?? '匿名'}";
        Template compiled5 = engine.compile(template5);

        Context context5 = new Context();
        // 不设置 name 变量

        String result5 = compiled5.render(context5);
        System.out.println("Elvis运算: " + result5);

        // 2.6 字符串拼接
        String template6 = "全名: {firstName + ' ' + lastName}";
        Template compiled6 = engine.compile(template6);

        Context context6 = new Context();
        context6.put("firstName", "张");
        context6.put("lastName", "三");

        String result6 = compiled6.render(context6);
        System.out.println("字符串拼接: " + result6);

        System.out.println();
    }

    // ==================== 条件语句示例 ====================

    /**
     * 示例3：条件语句
     * <p>
     * EasyTL 支持 if/else if/else 语句块，语法为：
     * <pre>
     * {% if condition }
     *   内容
     * {% else if otherCondition }
     *   其他内容
     * {% else }
     *   默认内容
     * {/% if }
     * </pre>
     * </p>
     */
    public static void conditionalStatements() {
        System.out.println("=== 3. 条件语句示例 ===");

        TemplateEngine engine = new TemplateEngine();

        // 3.1 简单 if 语句
        String template1 = "{% if isLoggedIn }欢迎回来, {username}!{/% if }";
        Template compiled1 = engine.compile(template1);

        Context context1 = new Context();
        context1.put("isLoggedIn", true);
        context1.put("username", "张三");

        String result1 = compiled1.render(context1);
        System.out.println("简单 if (true): " + result1);

        context1.put("isLoggedIn", false);
        String result1False = compiled1.render(context1);
        System.out.println("简单 if (false): " + result1False);

        // 3.2 if-else 语句
        String template2 = "{% if score >= 60 }及格{% else }不及格{/% if }";
        Template compiled2 = engine.compile(template2);

        Context context2 = new Context();
        context2.put("score", 75);

        String result2 = compiled2.render(context2);
        System.out.println("if-else (75分): " + result2);

        context2.put("score", 45);
        String result2Fail = compiled2.render(context2);
        System.out.println("if-else (45分): " + result2Fail);

        // 3.3 if-else if-else 语句
        String template3 = "等级: {% if score >= 90 }优秀{% else if score >= 80 }良好{% else if score >= 60 }及格{% else }不及格{/% if }";
        Template compiled3 = engine.compile(template3);

        Context context3 = new Context();
        context3.put("score", 95);
        System.out.println("if-else if-else (95分): " + compiled3.render(context3));

        context3.put("score", 85);
        System.out.println("if-else if-else (85分): " + compiled3.render(context3));

        context3.put("score", 70);
        System.out.println("if-else if-else (70分): " + compiled3.render(context3));

        context3.put("score", 50);
        System.out.println("if-else if-else (50分): " + compiled3.render(context3));

        System.out.println();
    }

    // ==================== 循环语句示例 ====================

    /**
     * 示例4：循环语句
     * <p>
     * EasyTL 支持 for 循环语句块，语法为：
     * <pre>
     * {% for item in list }
     *   {item}
     * {/% for }
     *
     * // 带索引的循环
     * {% for index, item in list }
     *   {index}: {item}
     * {/% for }
     * </pre>
     * </p>
     */
    public static void loopStatements() {
        System.out.println("=== 4. 循环语句示例 ===");

        TemplateEngine engine = new TemplateEngine();

        // 4.1 基本循环
        String template1 = "列表:\n{% for item in items }- {item}\n{/% for }";
        Template compiled1 = engine.compile(template1);

        Context context1 = new Context();
        context1.put("items", Arrays.asList("苹果", "香蕉", "橙子"));

        String result1 = compiled1.render(context1);
        System.out.println("基本循环:\n" + result1);

        // 4.2 带索引的循环
        String template2 = "编号列表:\n{% for i, item in items }{i}. {item}\n{/% for }";
        Template compiled2 = engine.compile(template2);

        String result2 = compiled2.render(context1);
        System.out.println("带索引循环:\n" + result2);

        // 4.3 循环中访问对象属性
        String template3 = "用户列表:\n{% for user in users }- {user.name} ({user.age}岁)\n{/% for }";
        Template compiled3 = engine.compile(template3);

        Map<String, Object> user1 = new HashMap<>();
        user1.put("name", "张三");
        user1.put("age", 25);

        Map<String, Object> user2 = new HashMap<>();
        user2.put("name", "李四");
        user2.put("age", 30);

        Context context3 = new Context();
        context3.put("users", Arrays.asList(user1, user2));

        String result3 = compiled3.render(context3);
        System.out.println("循环访问属性:\n" + result3);

        // 4.4 嵌套循环
        String template4 = "矩阵:\n{% for row in matrix }行: {% for item in row }{item} {/% for }\n{/% for }";
        Template compiled4 = engine.compile(template4);

        Context context4 = new Context();
        context4.put("matrix", Arrays.asList(
            Arrays.asList(1, 2, 3),
            Arrays.asList(4, 5, 6),
            Arrays.asList(7, 8, 9)
        ));

        String result4 = compiled4.render(context4);
        System.out.println("嵌套循环:\n" + result4);

        System.out.println();
    }

    // ==================== Switch 语句示例 ====================

    /**
     * 示例5：Switch 语句
     * <p>
     * EasyTL 支持 switch-case 语句块，语法为：
     * <pre>
     * {% switch value }
     * {% case 1 }内容1
     * {% case 2 }内容2
     * {% default }默认内容
     * {/% switch }
     * </pre>
     * </p>
     */
    public static void switchStatements() {
        System.out.println("=== 5. Switch 语句示例 ===");

        TemplateEngine engine = new TemplateEngine();

        String template = "状态: {% switch status }\n" +
            "{% case 1 }待处理\n" +
            "{% case 2 }处理中\n" +
            "{% case 3 }已完成\n" +
            "{% default }未知状态\n" +
            "{/% switch }";
        Template compiled = engine.compile(template);

        Context context = new Context();

        context.put("status", 1);
        System.out.println("status=1: " + compiled.render(context).trim());

        context.put("status", 2);
        System.out.println("status=2: " + compiled.render(context).trim());

        context.put("status", 3);
        System.out.println("status=3: " + compiled.render(context).trim());

        context.put("status", 99);
        System.out.println("status=99: " + compiled.render(context).trim());

        System.out.println();
    }

    // ==================== Let 语句示例 ====================

    /**
     * 示例6：Let 语句
     * <p>
     * EasyTL 支持 let 语句来定义局部变量，语法为：
     * <pre>
     * {% let varName = expression %}
     * </pre>
     * </p>
     */
    public static void letStatements() {
        System.out.println("=== 6. Let 语句示例 ===");

        TemplateEngine engine = new TemplateEngine();

        // 6.1 基本变量定义
        String template1 = "{% let greeting = 'Hello' %}Message: {greeting}, {name}!";
        Template compiled1 = engine.compile(template1);

        Context context1 = new Context();
        context1.put("name", "World");

        String result1 = compiled1.render(context1);
        System.out.println("基本变量定义: " + result1);

        // 6.2 表达式结果赋值
        String template2 = "{% let total = a + b %}合计: {total}";
        Template compiled2 = engine.compile(template2);

        Context context2 = new Context();
        context2.put("a", 10);
        context2.put("b", 20);

        String result2 = compiled2.render(context2);
        System.out.println("表达式赋值: " + result2);

        // 6.3 多个变量定义
        String template3 = "{% let x = 1 %}{% let y = 2 %}{% let z = x + y %}结果: x={x}, y={y}, z={z}";
        Template compiled3 = engine.compile(template3);

        String result3 = compiled3.render(new Context());
        System.out.println("多变量定义: " + result3);

        System.out.println();
    }
}