package org.dromara.easytl.ast;

import java.util.Collections;
import java.util.List;

/**
 * Lambda expression AST node.
 *
 * <p>Supports two body forms:</p>
 * <ul>
 *   <li>Expression body: <code>() -> 42</code></li>
 *   <li>Block body: <code>() -> { return 42 }</code></li>
 * </ul>
 *
 * <p>Parameter forms:</p>
 * <ul>
 *   <li>No parameters: <code>() -> expr</code></li>
 *   <li>Single untyped: <code>x -> expr</code></li>
 *   <li>Single with parens: <code>(x) -> expr</code></li>
 *   <li>Multiple: <code>(a, b) -> expr</code></li>
 *   <li>Typed: <code>(int a, String b) -> expr</code></li>
 * </ul>
 *
 * @author EasyTL Team
 */
public class LambdaExpression extends ASTNode {

    private final List<LambdaParameter> parameters;
    private final List<ASTNode> body;           // Block body statements (null for expression body)
    private final ASTNode expressionBody;       // Expression body (null for block body)
    private final boolean isBlockBody;

    /**
     * Create a Lambda expression.
     *
     * @param parameters     the parameter list
     * @param body           block body statements (null for expression body)
     * @param expressionBody expression body (null for block body)
     * @param isBlockBody    true if this is a block body Lambda
     * @param position       position in source code
     */
    public LambdaExpression(List<LambdaParameter> parameters,
                           List<ASTNode> body,
                           ASTNode expressionBody,
                           boolean isBlockBody,
                           Position position) {
        super(position);
        this.parameters = parameters != null ? parameters : Collections.emptyList();
        this.body = body;
        this.expressionBody = expressionBody;
        this.isBlockBody = isBlockBody;
    }

    /**
     * Get the parameter list.
     *
     * @return the parameters
     */
    public List<LambdaParameter> getParameters() {
        return parameters;
    }

    /**
     * Get the number of parameters.
     *
     * @return the parameter count
     */
    public int getParameterCount() {
        return parameters.size();
    }

    /**
     * Get the block body statements.
     *
     * @return the statements, or null if this is an expression body Lambda
     */
    public List<ASTNode> getBody() {
        return body;
    }

    /**
     * Get the expression body.
     *
     * @return the expression, or null if this is a block body Lambda
     */
    public ASTNode getExpressionBody() {
        return expressionBody;
    }

    /**
     * Check if this Lambda has a block body.
     *
     * @return true if block body
     */
    public boolean isBlockBody() {
        return isBlockBody;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitLambdaExpression(this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("LambdaExpression{params=[");
        for (int i = 0; i < parameters.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(parameters.get(i));
        }
        sb.append("], body=");
        if (isBlockBody) {
            sb.append(body);
        } else {
            sb.append(expressionBody);
        }
        sb.append("}");
        return sb.toString();
    }
}