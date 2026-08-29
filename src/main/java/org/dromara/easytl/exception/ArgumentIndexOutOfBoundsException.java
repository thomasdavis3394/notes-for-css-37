package org.dromara.easytl.exception;

import org.dromara.easytl.ast.Position;
import org.dromara.easytl.parser.Source;
import org.dromara.easytl.parser.StringSource;

/**
 * Exception thrown when argument index access fails.
 * <p>
 * Thrown in two scenarios:
 * <ul>
 *   <li>Index out of bounds: the specified index is negative or exceeds the arguments array length</li>
 *   <li>Null value: the argument at the specified index is null (for strict variable access)</li>
 * </ul>
 * </p>
 *
 * @author gongjun
 * @since 1.0.0
 */
public class ArgumentIndexOutOfBoundsException extends TemplateRuntimeException {

    private static final long serialVersionUID = 1L;

    private final int index;
    private final Integer arrayLength;

    /**
     * Constructs an exception for index out of bounds case.
     *
     * @param index the invalid index that was accessed
     * @param arrayLength the length of the arguments array, or null if arguments array is null
     */
    public ArgumentIndexOutOfBoundsException(final int index, final Integer arrayLength) {
        super(buildOutOfBoundsMessage(index, arrayLength));
        this.index = index;
        this.arrayLength = arrayLength;
    }

    /**
     * Constructs an exception for null value case.
     *
     * @param index the index where null value was found
     */
    public ArgumentIndexOutOfBoundsException(final int index) {
        super(buildNullValueMessage(index));
        this.index = index;
        this.arrayLength = null;
    }

    /**
     * Constructs an exception with a custom message.
     *
     * @param message the error message
     */
    public ArgumentIndexOutOfBoundsException(final String message) {
        super(message);
        this.index = -1;
        this.arrayLength = null;
    }

    /**
     * Constructs an exception with a custom message and cause.
     *
     * @param message the error message
     * @param cause the cause of this exception
     */
    public ArgumentIndexOutOfBoundsException(final String message, final Throwable cause) {
        super(message, cause);
        this.index = -1;
        this.arrayLength = null;
    }

    /**
     * Constructs an exception with position information.
     *
     * @param index the invalid index that was accessed
     * @param arrayLength the length of the arguments array, or null if arguments array is null
     * @param startLine the start line number (1-based)
     * @param startColumn the start column number (1-based)
     * @param endLine the end line number (1-based)
     * @param endColumn the end column number (1-based)
     * @param sourceText the source code text
     */
    public ArgumentIndexOutOfBoundsException(final int index, final Integer arrayLength,
                                              final int startLine, final int startColumn,
                                              final int endLine, final int endColumn,
                                              final String sourceText) {
        super(buildOutOfBoundsMessage(index, arrayLength),
              new Position(startLine, startColumn, endLine, endColumn),
              sourceText != null ? new StringSource(sourceText) : null);
        this.index = index;
        this.arrayLength = arrayLength;
    }

    /**
     * Constructs an exception with position information for null value case.
     *
     * @param index the index where null value was found
     * @param startLine the start line number (1-based)
     * @param startColumn the start column number (1-based)
     * @param endLine the end line number (1-based)
     * @param endColumn the end column number (1-based)
     * @param sourceText the source code text
     */
    public ArgumentIndexOutOfBoundsException(final int index,
                                              final int startLine, final int startColumn,
                                              final int endLine, final int endColumn,
                                              final String sourceText) {
        super(buildNullValueMessage(index),
              new Position(startLine, startColumn, endLine, endColumn),
              sourceText != null ? new StringSource(sourceText) : null);
        this.index = index;
        this.arrayLength = null;
    }

    /**
     * Constructs an exception using Position and Source objects.
     *
     * @param index the invalid index that was accessed
     * @param arrayLength the length of the arguments array, or null if arguments array is null
     * @param position the position information
     * @param source the source code
     */
    public ArgumentIndexOutOfBoundsException(final int index, final Integer arrayLength,
                                              final Position position, final Source source) {
        super(buildOutOfBoundsMessage(index, arrayLength), position, source);
        this.index = index;
        this.arrayLength = arrayLength;
    }

    /**
     * Constructs an exception using Position object and sourceText (backward compatible).
     *
     * @param index the invalid index that was accessed
     * @param arrayLength the length of the arguments array, or null if arguments array is null
     * @param position the position information
     * @param sourceText the source code text
     */
    public ArgumentIndexOutOfBoundsException(final int index, final Integer arrayLength,
                                              final Position position, final String sourceText) {
        this(index, arrayLength, position,
             sourceText != null ? new StringSource(sourceText) : null);
    }

    /**
     * Gets the index that caused this exception.
     *
     * @return the invalid index, or -1 if not applicable
     */
    public int getIndex() {
        return index;
    }

    /**
     * Gets the length of the arguments array.
     *
     * @return the array length, or null if the arguments array was null
     */
    public Integer getArrayLength() {
        return arrayLength;
    }

    /**
     * Builds error message for index out of bounds case.
     */
    private static String buildOutOfBoundsMessage(final int index, final Integer arrayLength) {
        if (arrayLength == null) {
            return "Argument index " + index + " is out of bounds: arguments array is null";
        }
        return "Argument index " + index + " is out of bounds: arguments array length is " + arrayLength;
    }

    /**
     * Builds error message for null value case.
     */
    private static String buildNullValueMessage(final int index) {
        return "Argument at index " + index + " is null";
    }
}