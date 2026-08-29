package org.dromara.easytl.examples.advanced;

import org.dromara.easytl.Context;
import org.dromara.easytl.Template;
import org.dromara.easytl.TemplateEngine;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 进阶示例 - 复杂表达式、嵌套语句块、模板扩展
 * <p>
 * 本类演示 EasyTL 模板引擎的进阶用法，包括：
 * <ul>
 *   <li>复杂表达式 - 方法调用、链式调用、字面量</li>
 *   <li>嵌套语句块 - 嵌套 if/for/switch</li>
 *   <li>模板扩展 - extends 语句</li>
 *   <li>模板导入 - import 语句</li>
 * </ul>
 * </p>
 *
 * @author EasyTL Team
 * @since 1.0.0
 */
public class AdvancedExamples {

    /**
     * 运行所有进阶示例
     */
    public static void runAll() {
        complexExpressions();
        methodCalls();
        nestedStatements();
        literalsAndCollections();
        expressionInStatements();
    }

    // ==================== 复杂表达式示例 ====================

    /**
     * 示例1：复杂表达式
     * <p>
     * 演示复杂的表达式组合，包括嵌套的三元运算、复合逻辑判断等
     * </p>
     */
    public static void complexExpressions() {
        System.out.println("=== 1. 复杂表达式示例 ===");

        TemplateEngine engine = new TemplateEngine();

        // 1.1 嵌套三元运算
        String template1 = "等级: {score >= 90 ? '优秀' : (score >= 60 ? '及格' : '不及格')}";
        Template compiled1 = engine.compile(template1);

        Context context1 = new Context();
        context1.put("score", 95);
        System.out.println("嵌套三元 (95分): " + compiled1.render(context1));

        context1.put("score", 70);
        System.out.println("嵌套三元 (70分): " + compiled1.render(context1));

        context1.put("score", 40);
        System.out.println("嵌套三元 (40分): " + compiled1.render(context1));

        // 1.2 复合逻辑判断
        String template2 = "结果: {(age >= 18 && age <= 65) ? '工作年龄' : '非工作年龄'}";
        Template compiled2 = engine.compile(template2);

        Context context2 = new Context();
        context2.put("age", 25);
        System.out.println("复合逻辑 (25岁): " + compiled2.render(context2));

        context2.put("age", 70);
        System.out.println("复合逻辑 (70岁): " + compiled2.render(context2));

        // 1.3 数学表达式组合
        String template3 = "计算: {(a + b) * c - d / 2}";
        Template compiled3 = engine.compile(template3);

        Context context3 = new Context();
        context3.put("a", 10);
        context3.put("b", 5);
        context3.put("c", 2);
        context3.put("d", 8);

        System.out.println("数学表达式: " + compiled3.render(context3));

        System.out.println();
    }

    // ==================== 方法调用示例 ====================

    /**
     * 示例2：方法调用
     * <p>
     * EasyTL 支持在表达式中调用对象的方法：
     * <pre>
     * {object.method()}
     * {object.method(arg1, arg2)}
     * {object.property.method()}
     * </pre>
     * </p>
     */
    public static void methodCalls() {
        System.out.println("=== 2. 方法调用示例 ===");

        TemplateEngine engine = new TemplateEngine();

        // 2.1 字符串方法调用
        String template1 = "大写: {name.toUpperCase()}, 长度: {name.length()}";
        Template compiled1 = engine.compile(template1);

        Context context1 = new Context();
        context1.put("name", "hello");

        String result1 = compiled1.render(context1);
        System.out.println("字符串方法: " + result1);

        // 2.2 列表方法调用
        String template2 = "列表大小: {items.size()}, 第一个: {items.get(0)}";
        Template compiled2 = engine.compile(template2);

        Context context2 = new Context();
        context2.put("items", Arrays.asList("a", "b", "c"));

        String result2 = compiled2.render(context2);
        System.out.println("列表方法: " + result2);

        // 2.3 自定义对象方法调用
        String template3 = "用户: {user.getName()}, 年龄: {user.getAge()}";
        Template compiled3 = engine.compile(template3);

        Context context3 = new Context();
        context3.put("user", new User("张三", 28));

        String result3 = compiled3.render(context3);
        System.out.println("自定义对象方法: " + result3);

        // 2.4 链式方法调用
        String template4 = "链式调用: {user.getName().toUpperCase()}";
        Template compiled4 = engine.compile(template4);

        String result4 = compiled4.render(context3);
        System.out.println("链式调用: " + result4);

        System.out.println();
    }

    // ==================== 嵌套语句块示例 ====================

    /**
     * 示例3：嵌套语句块
     * <p>
     * 演示 if/for/switch 语句块的嵌套使用
     * </p>
     */
    public static void nestedStatements() {
        System.out.println("=== 3. 嵌套语句块示例 ===");

        TemplateEngine engine = new TemplateEngine();

        // 3.1 if 嵌套
        String template1 = "{% if isLoggedIn %}{% if isAdmin %}管理员面板{% else %}用户面板{/% if }{/% if }";
        Template compiled1 = engine.compile(template1);

        Context context1 = new Context();
        context1.put("isLoggedIn", true);
        context1.put("isAdmin", true);
        System.out.println("if嵌套 (管理员): " + compiled1.render(context1));

        context1.put("isAdmin", false);
        System.out.println("if嵌套 (普通用户): " + compiled1.render(context1));

        // 3.2 for 嵌套 if
        String template2 = "商品列表:\n{% for item in items %}{% if item.available %}- {item.name}: {item.price}元{/% if }{/% for }";
        Template compiled2 = engine.compile(template2);

        Map<String, Object> item1 = new HashMap<>();
        item1.put("name", "苹果");
        item1.put("price", 5);
        item1.put("available", true);

        Map<String, Object> item2 = new HashMap<>();
        item2.put("name", "香蕉");
        item2.put("price", 3);
        item2.put("available", false);

        Map<String, Object> item3 = new HashMap<>();
        item3.put("name", "橙子");
        item3.put("price", 4);
        item3.put("available", true);

        Context context2 = new Context();
        context2.put("items", Arrays.asList(item1, item2, item3));

        System.out.println("for嵌套if:\n" + compiled2.render(context2));

        // 3.3 多层嵌套 for 循环
        String template3 = "二维数组:\n{% for row in matrix %}行: {% for val in row %}{val} {/% for }\n{/% for }";
        Template compiled3 = engine.compile(template3);

        Context context3 = new Context();
        context3.put("matrix", Arrays.asList(
            Arrays.asList(1, 2, 3),
            Arrays.asList(4, 5, 6),
            Arrays.asList(7, 8, 9)
        ));

        System.out.println("多层嵌套for:\n" + compiled3.render(context3));

        System.out.println();
    }

    // ==================== 字面量与集合示例 ====================

    /**
     * 示例4：字面量与集合
     * <p>
     * EasyTL 支持多种字面量：
     * <ul>
     *   <li>数字: 123, 3.14</li>
     *   <li>字符串: 'hello', "world"</li>
     *   <li>布尔: true, false</li>
     *   <li>null: null</li>
     *   <li>列表: [1, 2, 3]</li>
     *   <li>Map: {key: value}</li>
     *   <li>区间: 1..10</li>
     * </ul>
     * </p>
     */
    public static void literalsAndCollections() {
        System.out.println("=== 4. 字面量与集合示例 ===");

        TemplateEngine engine = new TemplateEngine();

        // 4.1 数字字面量
        String template1 = "整数: {123}, 浮点数: {3.14}";
        Template compiled1 = engine.compile(template1);
        System.out.println("数字字面量: " + compiled1.render(new Context()));

        // 4.2 字符串字面量
        String template2 = "单引号: {'Hello'}, 双引号: {\"World\"}";
        Template compiled2 = engine.compile(template2);
        System.out.println("字符串字面量: " + compiled2.render(new Context()));

        // 4.3 布尔和 null
        String template3 = "真: {true}, 假: {false}, 空: {null}";
        Template compiled3 = engine.compile(template3);
        System.out.println("布尔和null: " + compiled3.render(new Context()));

        // 4.4 列表字面量
        String template4 = "列表: {[1, 2, 3]}, 第一个: {[1, 2, 3][0]}";
        Template compiled4 = engine.compile(template4);
        System.out.println("列表字面量: " + compiled4.render(new Context()));

        // 4.5 Map 字面量
        String template5 = "Map: {{'name': '张三', 'age': 25}}, 名字: {{'name': '李四'}['name']}";
        Template compiled5 = engine.compile(template5);
        System.out.println("Map字面量: " + compiled5.render(new Context()));

        // 4.6 区间
        String template6 = "区间循环:\n{% for i in 1..5 %}{i} {/% for }";
        Template compiled6 = engine.compile(template6);
        System.out.println("区间循环:\n" + compiled6.render(new Context()));

        System.out.println();
    }

    // ==================== 语句中的表达式示例 ====================

    /**
     * 示例5：语句中的表达式
     * <p>
     * 演示在 if/for 等语句中使用复杂表达式
     * </p>
     */
    public static void expressionInStatements() {
        System.out.println("=== 5. 语句中的表达式示例 ===");

        TemplateEngine engine = new TemplateEngine();

        // 5.1 if 中使用方法调用
        String template1 = "{% if name.length() > 5 %}名字太长{% else %}名字合适{/% if }";
        Template compiled1 = engine.compile(template1);

        Context context1 = new Context();
        context1.put("name", "Alexander");
        System.out.println("if中使用方法 (长名字): " + compiled1.render(context1));

        context1.put("name", "Tom");
        System.out.println("if中使用方法 (短名字): " + compiled1.render(context1));

        // 5.2 for 中使用列表表达式
        String template2 = "反向遍历:\n{% for item in [3, 2, 1] %}{item} {/% for }";
        Template compiled2 = engine.compile(template2);
        System.out.println("for中使用列表表达式:\n" + compiled2.render(new Context()));

        // 5.3 复杂条件组合
        String template3 = "{% if (age >= 18 && age <= 60) && (hasLicense || isExempt) %}允许驾驶{% else %}不允许驾驶{/% if }";
        Template compiled3 = engine.compile(template3);

        Context context3 = new Context();
        context3.put("age", 25);
        context3.put("hasLicense", true);
        context3.put("isExempt", false);
        System.out.println("复杂条件组合 (允许): " + compiled3.render(context3));

        context3.put("hasLicense", false);
        System.out.println("复杂条件组合 (不允许): " + compiled3.render(context3));

        System.out.println();
    }

    /**
     * 用户测试类
     */
    public static class User {
        private final String name;
        private final int age;

        public User(String name, int age) {
            this.name = name;
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public int getAge() {
            return age;
        }
    }
}