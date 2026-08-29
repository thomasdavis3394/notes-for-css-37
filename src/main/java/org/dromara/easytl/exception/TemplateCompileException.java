package org.dromara.easytl.exception;

import org.dromara.easytl.ast.Position;
import org.dromara.easytl.parser.Source;

/**
 * 模板编译异常
 * <p>
 * 当模板编译过程中发生错误时抛出此异常
 * </p>
 * 
 * @author gongjun
 * @since 1.0.0
 */
public class TemplateCompileException extends TemplateException {

    private static final long serialVersionUID = 1L;
    private static final String COMPILE_ERROR = "Compile error";

    /**
     * 构造函数
     * 
     * @param message 异常信息
     */
    public TemplateCompileException(final String message) {
        super(message);
    }

    /**
     * 构造函数
     * 
     * @param message 异常信息
     * @param cause 原因异常
     */
    public TemplateCompileException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * 构造函数
     * 
     * @param cause 原因异常
     */
    public TemplateCompileException(final Throwable cause) {
        super(cause);
    }

    /**
     * 构造函数，带位置与源码（用于编译期错误定位）
     *
     * @param message  异常信息
     * @param position 位置信息
     * @param source   源码
     */
    public TemplateCompileException(final String message, final Position position, final Source source) {
        super(message, COMPILE_ERROR, position, source);
    }
}

