package org.dromara.easytl.exception;

import org.dromara.easytl.ast.Position;
import org.dromara.easytl.parser.Source;
import org.dromara.easytl.parser.StringSource;

/**
 * Template Syntax Exception
 * <p>
 * Thrown when template string contains syntax errors
 * </p>
 *
 * @author gongjun
 * @since 1.0.0
 */
public class TemplateSyntaxException extends TemplateException {

    private static final long serialVersionUID = 1L;
    private static final String SYNTAX_ERROR = "Syntax error";

    /**
     * Constructor
     *
     * @param message error message
     */
    public TemplateSyntaxException(final String message) {
        super(message);  // 调用父类无位置信息构造函数
    }

    /**
     * Constructor with Position and Source (new primary constructor)
     *
     * @param message error message
     * @param position position information
     * @param source source code
     */
    public TemplateSyntaxException(final String message, final Position position, final Source source) {
        super(message, SYNTAX_ERROR, position, source);  // 直接传原始消息，不预格式化
    }

    /**
     * Constructor with Position only
     *
     * @param message error message
     * @param position position information
     */
    public TemplateSyntaxException(final String message, final Position position) {
        this(message, position, null);
    }

    /**
     * Constructor with line and column
     *
     * @param message error message
     * @param line error line number (1-based)
     * @param column error column number (1-based)
     */
    public TemplateSyntaxException(final String message, final int line, final int column) {
        this(message, new Position(line, column, line, column), null);
    }

    /**
     * Constructor with line/column and sourceText
     *
     * @param message error message
     * @param line error line number (1-based)
     * @param column error column number (1-based)
     * @param sourceText source code text
     */
    public TemplateSyntaxException(final String message, final int line, final int column, final String sourceText) {
        this(message, new Position(line, column, line, column),
             sourceText != null ? new StringSource(sourceText) : null);
    }

    /**
     * Constructor with position range
     *
     * @param message error message
     * @param startLine start line number (1-based)
     * @param startColumn start column number (1-based)
     * @param endLine end line number (1-based)
     * @param endColumn end column number (1-based)
     * @param sourceText source code text
     */
    public TemplateSyntaxException(final String message, final int startLine, final int startColumn,
                                  final int endLine, final int endColumn, final String sourceText) {
        this(message, new Position(startLine, startColumn, endLine, endColumn),
             sourceText != null ? new StringSource(sourceText) : null);
    }

    /**
     * Constructor with cause
     *
     * @param message error message
     * @param cause cause exception
     */
    public TemplateSyntaxException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Get error line number
     *
     * @return line number (1-based), returns -1 if unknown
     * @deprecated Use {@link #getStartLine()} instead
     */
    @Deprecated
    public int getLine() {
        return getStartLine();
    }

    /**
     * Get error column number
     *
     * @return column number (1-based), returns -1 if unknown
     * @deprecated Use {@link #getStartColumn()} instead
     */
    @Deprecated
    public int getColumn() {
        return getStartColumn();
    }
}
