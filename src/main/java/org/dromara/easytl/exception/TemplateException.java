package org.dromara.easytl.exception;

import org.dromara.easytl.ast.Position;
import org.dromara.easytl.parser.Source;

/**
 * 模板引擎基础异常类
 *
 * @author gongjun
 * @since 1.0.0
 */
public class TemplateException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String rawMessage;      // 原始错误消息（不含位置信息）
    private final String errorType;        // 错误类型（如 "Syntax error"）
    private final Position position;
    private final Source source;
    private String cachedMessage;          // 缓存的格式化消息（延迟计算）

    /**
     * 构造函数
     *
     * @param message 异常信息
     */
    public TemplateException(final String message) {
        super(message);
        this.rawMessage = message;
        this.errorType = null;
        this.position = null;
        this.source = null;
        this.cachedMessage = message;
    }

    /**
     * 构造函数
     *
     * @param message 异常信息
     * @param cause 原因异常
     */
    public TemplateException(final String message, final Throwable cause) {
        super(message, cause);
        this.rawMessage = message;
        this.errorType = null;
        this.position = null;
        this.source = null;
        this.cachedMessage = message;
    }

    /**
     * 构造函数
     *
     * @param cause 原因异常
     */
    public TemplateException(final Throwable cause) {
        super(cause);
        this.rawMessage = cause != null ? cause.getMessage() : null;
        this.errorType = null;
        this.position = null;
        this.source = null;
        this.cachedMessage = this.rawMessage;
    }

    /**
     * 构造函数
     *
     * @param message 异常信息
     * @param position 位置信息
     * @param source 源码
     */
    protected TemplateException(final String message, final Position position, final Source source) {
        this(message, "Template error", position, source);
    }

    /**
     * 构造函数，允许子类指定错误类型
     *
     * @param message 异常信息
     * @param errorType 错误类型（如 "Template error" 或 "Runtime error"）
     * @param position 位置信息
     * @param source 源码
     */
    protected TemplateException(final String message, final String errorType,
                               final Position position, final Source source) {
        // 不调用 super() 格式化消息，延迟到 getMessage()
        this.rawMessage = message;
        this.errorType = errorType;
        this.position = position;
        this.source = source;
        this.cachedMessage = null;  // 延迟计算
    }

    /**
     * 构造函数
     *
     * @param message 异常信息
     * @param cause 原因异常
     * @param position 位置信息
     * @param source 源码
     */
    protected TemplateException(final String message, final Throwable cause,
                               final Position position, final Source source) {
        this(message, "Template error", cause, position, source);
    }

    /**
     * 构造函数，允许子类指定错误类型和原因异常
     *
     * @param message 异常信息
     * @param errorType 错误类型（如 "Template error" 或 "Runtime error"）
     * @param cause 原因异常
     * @param position 位置信息
     * @param source 源码
     */
    protected TemplateException(final String message, final String errorType, final Throwable cause,
                               final Position position, final Source source) {
        super(message, cause);  // 仅传递原始消息给 super()
        this.rawMessage = message;
        this.errorType = errorType;
        this.position = position;
        this.source = source;
        this.cachedMessage = null;  // 延迟计算
    }

    /**
     * 格式化错误消息
     *
     * @return 格式化后的错误消息
     */
    private String formatMessage() {
        if (position != null && errorType != null) {
            return ErrorMessageFormatter.formatMessage(
                errorType, rawMessage, position, source);
        }
        return rawMessage;
    }

    /**
     * 获取错误消息（延迟格式化）
     *
     * @return 错误消息
     */
    @Override
    public String getMessage() {
        if (cachedMessage == null) {
            cachedMessage = formatMessage();
        }
        return cachedMessage;
    }

    /**
     * 获取原始错误消息（不含位置信息）
     *
     * @return 原始错误消息
     */
    public String getRawMessage() {
        return rawMessage;
    }

    /**
     * 获取位置信息
     *
     * @return 位置信息
     */
    public Position getPosition() {
        return position;
    }

    /**
     * 获取源码
     *
     * @return 源码
     */
    public Source getSource() {
        return source;
    }

    /**
     * 获取起始行号
     *
     * @return 起始行号
     */
    public int getStartLine() {
        return position != null ? position.getStartLine() : -1;
    }

    /**
     * 获取起始列号
     *
     * @return 起始列号
     */
    public int getStartColumn() {
        return position != null ? position.getStartColumn() : -1;
    }

    /**
     * 获取结束行号
     *
     * @return 结束行号
     */
    public int getEndLine() {
        return position != null ? position.getEndLine() : -1;
    }

    /**
     * 获取结束列号
     *
     * @return 结束列号
     */
    public int getEndColumn() {
        return position != null ? position.getEndColumn() : -1;
    }

    /**
     * 获取源码文本
     *
     * @return 源码文本
     */
    public String getSourceText() {
        return source != null ? source.getSourceText() : null;
    }
}