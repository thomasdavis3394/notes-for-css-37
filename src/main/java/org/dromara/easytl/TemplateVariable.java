package org.dromara.easytl;

/**
 * 动态变量接口
 * <p>
 * 用于实现延迟计算的变量值。当变量被访问时，会自动调用 compute 方法计算实际值。
 * 支持与 Java 8 Lambda 表达式配合使用。
 * </p>
 *
 * <h3>使用示例：</h3>
 * <pre>{@code
 * Context context = new Context();
 * context.put("dynamicName", (ctx) -> "Hello, " + ctx.get("name"));
 * context.put("name", "World");
 *
 * // 模板中使用 {dynamicName}，渲染时输出 "Hello, World"
 * }</pre>
 *
 * <h3>特性：</h3>
 * <ul>
 *   <li>每次访问都会重新执行 compute 方法</li>
 *   <li>可以访问 Context 中的其他变量</li>
 *   <li>支持抛出异常，异常会被包装为 TemplateRuntimeException</li>
 *   <li>线程安全由实现者自行保证</li>
 * </ul>
 *
 * @author gongjun
 * @since 1.0.0
 */
@FunctionalInterface
public interface TemplateVariable {

    /**
     * 计算动态变量的值
     *
     * @param context 当前模板上下文，可用于访问其他变量
     * @return 计算后的变量值
     * @throws Exception 计算过程中可能抛出的异常
     */
    Object compute(Context context) throws Exception;
}