package org.dromara.easytl.examples;

import org.dromara.easytl.examples.basic.BasicExamples;
import org.dromara.easytl.examples.advanced.AdvancedExamples;
import org.dromara.easytl.examples.performance.PerformanceExamples;
import org.dromara.easytl.examples.errorhandling.ErrorHandlingExamples;

/**
 * EasyTL 模板引擎示例代码入口
 * <p>
 * 本示例代码库包含四类示例：
 * <ul>
 *   <li>{@link BasicExamples} - 基础示例：变量替换、表达式计算、条件语句、循环语句等</li>
 *   <li>{@link AdvancedExamples} - 进阶示例：复杂表达式、嵌套语句块、模板扩展等</li>
 *   <li>{@link PerformanceExamples} - 性能优化示例：缓存使用、对象复用等</li>
 *   <li>{@link ErrorHandlingExamples} - 错误处理示例：语法错误处理、运行时错误处理等</li>
 * </ul>
 * </p>
 *
 * @author EasyTL Team
 * @since 1.0.0
 */
public class ExamplesRunner {

    /**
     * 运行所有示例
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("EasyTL Template Engine Examples");
        System.out.println("========================================\n");

        // 运行基础示例
        System.out.println("--- Running Basic Examples ---\n");
        BasicExamples.runAll();

        // 运行进阶示例
        System.out.println("\n--- Running Advanced Examples ---\n");
        AdvancedExamples.runAll();

        // 运行性能优化示例
        System.out.println("\n--- Running Performance Examples ---\n");
        PerformanceExamples.runAll();

        // 运行错误处理示例
        System.out.println("\n--- Running Error Handling Examples ---\n");
        ErrorHandlingExamples.runAll();

        System.out.println("\n========================================");
        System.out.println("All examples completed!");
        System.out.println("========================================");
    }
}