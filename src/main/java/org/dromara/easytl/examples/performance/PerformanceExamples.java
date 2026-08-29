package org.dromara.easytl.examples.performance;

import org.dromara.easytl.Context;
import org.dromara.easytl.Template;
import org.dromara.easytl.TemplateEngine;

/**
 * 性能优化示例 - 缓存使用、对象复用
 * <p>
 * 本类演示 EasyTL 模板引擎的性能优化技巧：
 * <ul>
 *   <li>缓存使用 - 模板缓存机制</li>
 *   <li>对象复用 - Template 对象复用、Context 对象复用</li>
 *   <li>配置优化 - 根据场景选择最佳配置</li>
 * </ul>
 * </p>
 *
 * @author EasyTL Team
 * @since 1.0.0
 */
public class PerformanceExamples {

    /**
     * 运行所有性能优化示例
     */
    public static void runAll() {
        cacheUsage();
        templateReuse();
        contextReuse();
        configurationOptimization();
        benchmarkComparison();
    }

    // ==================== 缓存使用示例 ====================

    /**
     * 示例1：缓存使用
     * <p>
     * EasyTL 内置模板缓存机制，默认启用 LRU 缓存。
     * <ul>
     *   <li>缓存默认启用，最多缓存 100 个模板</li>
     *   <li>可通过配置调整缓存大小或禁用缓存</li>
     *   <li>相同模板源码只编译一次，后续从缓存获取</li>
     * </ul>
     * </p>
     */
    public static void cacheUsage() {
        System.out.println("=== 1. 缓存使用示例 ===");

        // 1.1 默认缓存配置
        TemplateEngine engine = new TemplateEngine();
        System.out.println("默认缓存大小: " + engine.getConfig().getMaxCacheSize());
        System.out.println("默认缓存启用: " + engine.getConfig().isCacheEnabled());

        // 1.2 演示缓存命中
        String template = "Hello, {name}!";

        // 第一次编译
        long start1 = System.nanoTime();
        Template compiled1 = engine.compile(template);
        long end1 = System.nanoTime();
        System.out.println("第一次编译耗时: " + (end1 - start1) / 1000 + " μs");
        System.out.println("缓存大小: " + engine.getCacheSize());

        // 第二次编译（从缓存获取）
        long start2 = System.nanoTime();
        Template compiled2 = engine.compile(template);
        long end2 = System.nanoTime();
        System.out.println("第二次编译耗时: " + (end2 - start2) / 1000 + " μs (缓存命中)");

        // 验证是否是同一个对象
        System.out.println("是否同一对象: " + (compiled1 == compiled2));

        // 1.3 清除缓存
        engine.clearCache();
        System.out.println("清除后缓存大小: " + engine.getCacheSize());

        System.out.println();
    }

    // ==================== 模板对象复用示例 ====================

    /**
     * 示例2：模板对象复用
     * <p>
     * Template 对象是线程安全的，可以在多线程环境下共享使用。
     * 编译一次，多次渲染，是最佳实践。
     * </p>
     */
    public static void templateReuse() {
        System.out.println("=== 2. 模板对象复用示例 ===");

        TemplateEngine engine = new TemplateEngine();
        String template = "用户: {user}, 时间: {time}, 操作: {action}";

        // 编译一次
        Template compiled = engine.compile(template);

        // 多次渲染（不同上下文）
        long startTime = System.nanoTime();

        for (int i = 0; i < 5; i++) {
            Context context = new Context();
            context.put("user", "User" + i);
            context.put("time", System.currentTimeMillis());
            context.put("action", "Action" + i);

            String result = compiled.render(context);
            System.out.println("渲染结果 " + i + ": " + result);
        }

        long endTime = System.nanoTime();
        System.out.println("总渲染耗时: " + (endTime - startTime) / 1000 + " μs");

        System.out.println();
    }

    // ==================== Context 对象复用示例 ====================

    /**
     * 示例3：Context 对象复用
     * <p>
     * 当渲染多个相似模板时，可以复用 Context 对象。
     * 注意：复用前需要清除或更新相关变量。
     * </p>
     */
    public static void contextReuse() {
        System.out.println("=== 3. Context 对象复用示例 ===");

        TemplateEngine engine = new TemplateEngine();
        Template template = engine.compile("Name: {name}, Age: {age}");

        // 复用 Context 对象
        Context context = new Context();

        // 第一次渲染
        context.put("name", "张三");
        context.put("age", 25);
        System.out.println("第一次: " + template.render(context));

        // 清除并复用
        context.clear();
        context.put("name", "李四");
        context.put("age", 30);
        System.out.println("第二次: " + template.render(context));

        // 再次复用
        context.clear();
        context.put("name", "王五");
        context.put("age", 35);
        System.out.println("第三次: " + template.render(context));

        System.out.println();
    }

    // ==================== 配置优化示例 ====================

    /**
     * 示例4：配置优化
     * <p>
     * 根据不同场景选择最佳配置：
     * <ul>
     *   <li>开发环境：禁用缓存，方便调试</li>
     *   <li>生产环境：启用缓存，提高性能</li>
     *   <li>大模板场景：调整缓存大小</li>
     * </ul>
     * </p>
     */
    public static void configurationOptimization() {
        System.out.println("=== 4. 配置优化示例 ===");

        // 4.1 开发环境配置（禁用缓存）
        TemplateEngine.TemplateEngineConfig devConfig = new TemplateEngine.TemplateEngineConfig()
            .setCacheEnabled(false);

        TemplateEngine devEngine = new TemplateEngine(devConfig);
        System.out.println("开发环境 - 缓存启用: " + devEngine.getConfig().isCacheEnabled());

        // 4.2 生产环境配置（启用缓存，调整大小）
        TemplateEngine.TemplateEngineConfig prodConfig = new TemplateEngine.TemplateEngineConfig()
            .setCacheEnabled(true)
            .setMaxCacheSize(500);

        TemplateEngine prodEngine = new TemplateEngine(prodConfig);
        System.out.println("生产环境 - 缓存启用: " + prodEngine.getConfig().isCacheEnabled());
        System.out.println("生产环境 - 缓存大小: " + prodEngine.getConfig().getMaxCacheSize());

        // 4.3 演示性能差异
        String template = "计算结果: {a + b * c}";

        // 开发环境（无缓存）
        long devStart = System.nanoTime();
        for (int i = 0; i < 100; i++) {
            devEngine.compile(template);
        }
        long devEnd = System.nanoTime();
        System.out.println("开发环境 100 次编译耗时: " + (devEnd - devStart) / 1000000 + " ms");

        // 生产环境（有缓存）
        long prodStart = System.nanoTime();
        for (int i = 0; i < 100; i++) {
            prodEngine.compile(template);
        }
        long prodEnd = System.nanoTime();
        System.out.println("生产环境 100 次编译耗时: " + (prodEnd - prodStart) / 1000000 + " ms");

        System.out.println();
    }

    // ==================== 性能对比示例 ====================

    /**
     * 示例5：性能对比
     * <p>
     * 对比不同使用方式的性能差异
     * </p>
     */
    public static void benchmarkComparison() {
        System.out.println("=== 5. 性能对比示例 ===");

        String template = "Hello, {name}! Your score is {score}. Status: {status}";
        int iterations = 1000;

        // 5.1 不使用缓存 vs 使用缓存
        TemplateEngine noCacheEngine = new TemplateEngine(
            new TemplateEngine.TemplateEngineConfig().setCacheEnabled(false)
        );

        TemplateEngine cachedEngine = new TemplateEngine(
            new TemplateEngine.TemplateEngineConfig().setCacheEnabled(true)
        );

        // 无缓存编译
        long noCacheStart = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            noCacheEngine.compile(template);
        }
        long noCacheEnd = System.nanoTime();

        // 有缓存编译
        long cachedStart = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            cachedEngine.compile(template);
        }
        long cachedEnd = System.nanoTime();

        System.out.println("编译 " + iterations + " 次:");
        System.out.println("  无缓存: " + (noCacheEnd - noCacheStart) / 1000000 + " ms");
        System.out.println("  有缓存: " + (cachedEnd - cachedStart) / 1000000 + " ms");

        // 5.2 渲染性能测试
        Template compiledTemplate = cachedEngine.compile(template);
        Context context = new Context();
        context.put("name", "测试用户");
        context.put("score", 95);
        context.put("status", "优秀");

        long renderStart = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            compiledTemplate.render(context);
        }
        long renderEnd = System.nanoTime();

        System.out.println("渲染 " + iterations + " 次: " + (renderEnd - renderStart) / 1000000 + " ms");

        System.out.println();
    }
}