package org.dromara.easytl;

/**
 * 编译后的模板接口
 * <p>
 * 模板对象是线程安全的，可以在多线程环境下共享使用
 * </p>
 * 
 * @author gongjun
 * @since 1.0.0
 */
public interface Template {

    /**
     * 使用给定的上下文渲染模板
     * 
     * @param context 上下文对象，包含模板所需的变量和数据
     * @return 渲染后的字符串
     */
    String render(Context context);
}

