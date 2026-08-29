package org.dromara.easytl.exception;

import org.dromara.easytl.ast.Position;
import org.dromara.easytl.parser.Source;

/**
 * Exception thrown when a template {@code assert(...)} assertion fails.
 * <p>
 * Raised by the built-in {@code assert} function when the condition expression
 * evaluates to a falsy value. The message describes the failed expectation,
 * including the source text and actual values of the operands when available,
 * e.g. {@code Assertion failed: expected a to be greater than b\nExpected : an Integer that was greater than <2>\nActual   : b = <2>}.
 * </p>
 * <p>
 * In the {@code assert(condition, lambda)} form this exception is not thrown;
 * instead an instance is passed to the lambda as the failure handler argument.
 * </p>
 *
 * @author EasyTL Team
 * @since 1.0.0
 */
public class AssertionException extends TemplateRuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs an exception with the given detail message.
     *
     * @param message the assertion failure message
     */
    public AssertionException(final String message) {
        super(message);
    }

    /**
     * Constructs an exception with the given detail message and position information.
     *
     * @param message  the assertion failure message
     * @param position the position of the assert call in the template source
     * @param source   the template source
     */
    public AssertionException(final String message, final Position position, final Source source) {
        super(message, position, source);
    }
}
