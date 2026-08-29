package org.dromara.easytl.runtime;

import java.util.List;

/**
 * 模板函数扩展接口（SPI）
 *
 * <p>实现本接口并通过 {@code Context.registerFunction(...)} 注册后，即可在模板中以
 * {@code fn(arg1, arg2, ...)} 形式调用（包括 JSONPath 过滤谓词 {@code [?(fn(@) ...)]} 内）。
 * 引擎内置的 RFC 9535 函数（{@code length}/{@code count}/{@code match}/{@code search}/{@code value}）
 * 同样通过本接口实现（见 {@link StandardFunctions}），语法层面无任何硬编码。</p>
 *
 * <p>调用解析优先级：导入类/上下文变量（Class、Lambda）&gt; 注册函数（含内置函数）。
 * 即上下文中的同名变量会覆盖注册函数。</p>
 *
 * <p>实现要求：</p>
 * <ul>
 * <li>{@link #call(List)} 收到的参数已完成求值；参数个数/类型不满足时抛出
 * {@link org.dromara.easytl.exception.TemplateRuntimeException}（在过滤谓词内按"不匹配"处理，不中断过滤）</li>
 * <li>实现应无状态、线程安全（同一实例可能被并发渲染共享）</li>
 * </ul>
 *
 * @author EasyTL Team
 */
public interface TemplateFunction {

    /**
     * 函数名（模板中以此名字调用，如 {@code length}）
     *
     * @return 函数名
     */
    String getName();

    /**
     * 执行函数
     *
     * @param args 已求值的参数列表（不为 null，可能为空）
     * @return 函数结果（可为 null）
     */
    Object call(List<Object> args);
}
