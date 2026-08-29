package org.dromara.easytl.ast;

/**
 * Represents a single parameter in a Lambda expression.
 *
 * <p>Parameters can be typed or untyped:</p>
 * <ul>
 *   <li>Untyped: <code>x -> x * 2</code></li>
 *   <li>Typed: <code>(int a, String b) -> a + b.length()</code></li>
 * </ul>
 *
 * @author EasyTL Team
 */
public class LambdaParameter {

    private final String name;
    private final String type;  // null if untyped
    private final Position position;

    /**
     * Create a Lambda parameter.
     *
     * @param name     the parameter name
     * @param type     the parameter type (null if untyped)
     * @param position the position in source code
     */
    public LambdaParameter(String name, String type, Position position) {
        this.name = name;
        this.type = type;
        this.position = position != null ? position : Position.unknown();
    }

    /**
     * Get the parameter name.
     *
     * @return the parameter name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the parameter type.
     *
     * @return the parameter type, or null if untyped
     */
    public String getType() {
        return type;
    }

    /**
     * Check if this parameter has a type declaration.
     *
     * @return true if the parameter has a type
     */
    public boolean hasType() {
        return type != null;
    }

    /**
     * Get the position in source code.
     *
     * @return the position
     */
    public Position getPosition() {
        return position;
    }

    @Override
    public String toString() {
        if (type != null) {
            return type + " " + name;
        }
        return name;
    }
}