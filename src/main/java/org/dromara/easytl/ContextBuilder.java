package org.dromara.easytl;

import org.dromara.easytl.runtime.TemplateFunction;

/**
 * 上下文构建器，支持链式调用创建 Context
 *
 * @author gongjun
 * @since 1.0.0
 */
public class ContextBuilder {

    private final Context context;

    /**
     * 构造函数
     */
    public ContextBuilder() {
        this.context = new Context();
    }

    /**
     * 添加变量
     *
     * @param name 变量名
     * @param value 变量值
     * @return this，支持链式调用
     */
    public ContextBuilder put(String name, Object value) {
        context.put(name, value);
        return this;
    }

    /**
     * 添加动态变量
     *
     * @param name 变量名
     * @param value 动态变量
     * @return this，支持链式调用
     */
    public ContextBuilder put(String name, TemplateVariable value) {
        context.put(name, value);
        return this;
    }

    /**
     * 设置参数数组
     * <p>
     * 参数数组用于通过索引访问参数，例如：{0}、${1}、{{2}}
     * </p>
     *
     * @param arguments 参数数组
     * @return this，支持链式调用
     */
    public ContextBuilder arguments(Object... arguments) {
        context.setArguments(arguments);
        return this;
    }

    /**
     * 注册模板函数（TemplateFunction SPI）
     * <p>
     * 注册后即可在模板中以 {@code fn(arg1, ...)} 形式调用（含 JSONPath 过滤谓词内）。
     * </p>
     *
     * @param function 函数实现
     * @return this，支持链式调用
     */
    public ContextBuilder function(TemplateFunction function) {
        context.registerFunction(function);
        return this;
    }

    /**
     * 构建并返回 Context
     *
     * @return 构建好的 Context 对象
     */
    public Context build() {
        return context;
    }
}