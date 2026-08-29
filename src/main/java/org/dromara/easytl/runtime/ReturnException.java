package org.dromara.easytl.runtime;

/**
 * Control flow exception used to implement return statements in Lambda expressions.
 *
 * <p>This exception is thrown when a return statement is executed inside a Lambda
 * and caught by the LambdaObject.invoke() method to return the value.</p>
 *
 * <p>Uses no stack trace filling for performance since this is a control flow mechanism.</p>
 *
 * @author EasyTL Team
 */
public class ReturnException extends RuntimeException {

    private final Object value;

    /**
     * Create a return exception with the given value.
     *
     * @param value the value to return (may be null for void returns)
     */
    public ReturnException(Object value) {
        super(null, null, false, false);  // No stack trace for control flow
        this.value = value;
    }

    /**
     * Get the return value.
     *
     * @return the value to return, or null for void return
     */
    public Object getValue() {
        return value;
    }
}