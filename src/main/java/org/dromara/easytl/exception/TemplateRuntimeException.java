package org.dromara.easytl.exception;

import org.dromara.easytl.ast.Position;
import org.dromara.easytl.parser.Source;
import org.dromara.easytl.parser.StringSource;

/**
 * 模板运行时异常
 * <p>
 * 当模板执行过程中发生错误时抛出此异常
 * </p>
 *
 * @author gongjun
 * @since 1.0.0
 */
public class TemplateRuntimeException extends TemplateException {

    private static final long serialVersionUID = 1L;

    /**
     * 构造函数
     *
     * @param message 异常信息
     */
    public TemplateRuntimeException(final String message) {
        super(message, null, null);
    }

    /**
     * 构造函数
     *
     * @param message 异常信息
     * @param cause 原因异常
     */
    public TemplateRuntimeException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * 构造函数
     *
     * @param cause 原因异常
     */
    public TemplateRuntimeException(final Throwable cause) {
        super(cause.toString(), cause);
    }

    /**
     * 构造函数，指定位置信息
     *
     * @param message 异常信息
     * @param startLine 起始行号（从1开始）
     * @param startColumn 起始列号（从1开始）
     * @param endLine 结束行号（从1开始）
     * @param endColumn 结束列号（从1开始）
     */
    public TemplateRuntimeException(final String message, final int startLine, final int startColumn,
                                   final int endLine, final int endColumn) {
        this(message, new Position(startLine, startColumn, endLine, endColumn), (Source) null);
    }

    /**
     * 构造函数，指定位置信息和源代码
     *
     * @param message 异常信息
     * @param startLine 起始行号（从1开始）
     * @param startColumn 起始列号（从1开始）
     * @param endLine 结束行号（从1开始）
     * @param endColumn 结束列号（从1开始）
     * @param sourceText 源代码文本
     */
    public TemplateRuntimeException(final String message, final int startLine, final int startColumn,
                                   final int endLine, final int endColumn, final String sourceText) {
        this(message, new Position(startLine, startColumn, endLine, endColumn),
             sourceText != null ? new StringSource(sourceText) : null);
    }

    /**
     * 构造函数，使用Position对象
     *
     * @param message 异常信息
     * @param position 位置信息
     */
    public TemplateRuntimeException(final String message, final Position position) {
        this(message, position, (Source) null);
    }

    /**
     * 构造函数，使用Position对象和Source对象（新主构造函数）
     *
     * @param message 异常信息（不应预先格式化）
     * @param position 位置信息
     * @param source 源码
     */
    public TemplateRuntimeException(final String message, final Position position, final Source source) {
        // Use default "Template error" from parent, but prepend "Runtime error: " to message
        // This results in "Template error: Runtime error: <message>" format
        super("Runtime error: " + message, position, source);
    }

    /**
     * 构造函数，使用Position对象和sourceText（向后兼容）
     *
     * @param message 异常信息（不应预先格式化）
     * @param position 位置信息
     * @param sourceText 源代码文本
     */
    public TemplateRuntimeException(final String message, final Position position, final String sourceText) {
        // Use default "Template error" from parent, but prepend "Runtime error: " to message
        this(message, position,
             sourceText != null ? new StringSource(sourceText) : null);
    }

    /**
     * 构造函数，带原因异常和位置信息
     *
     * @param message 异常信息（不应预先格式化）
     * @param cause 原因异常
     * @param startLine 起始行号（从1开始）
     * @param startColumn 起始列号（从1开始）
     * @param endLine 结束行号（从1开始）
     * @param endColumn 结束列号（从1开始）
     * @param sourceText 源代码文本
     */
    public TemplateRuntimeException(final String message, final Throwable cause, final int startLine,
                                   final int startColumn, final int endLine, final int endColumn,
                                   final String sourceText) {
        // Use default "Template error" from parent, but prepend "Runtime error: " to message
        super("Runtime error: " + message, cause, new Position(startLine, startColumn, endLine, endColumn),
              sourceText != null ? new StringSource(sourceText) : null);
    }
}
