package org.dromara.easytl.ast;

/**
 * Return statement for use inside Lambda block bodies.
 *
 * <p>Examples:</p>
 * <ul>
 *   <li>Return with value: <code>return 42</code></li>
 *   <li>Return without value: <code>return</code></li>
 * </ul>
 *
 * @author EasyTL Team
 */
public class ReturnStatement extends ASTNode {

    private final ASTNode value;  // Can be null for void returns

    /**
     * Create a return statement.
     *
     * @param value    the return value expression (null for void return)
     * @param position position in source code
     */
    public ReturnStatement(ASTNode value, Position position) {
        super(position);
        this.value = value;
    }

    /**
     * Get the return value expression.
     *
     * @return the value expression, or null if void return
     */
    public ASTNode getValue() {
        return value;
    }

    /**
     * Check if this return statement has a value.
     *
     * @return true if there is a return value
     */
    public boolean hasValue() {
        return value != null;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitReturnStatement(this);
    }

    @Override
    public String toString() {
        return "ReturnStatement{value=" + value + "}";
    }
}