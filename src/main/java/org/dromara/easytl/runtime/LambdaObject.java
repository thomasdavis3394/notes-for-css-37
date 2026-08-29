package org.dromara.easytl.runtime;

import org.dromara.easytl.Context;
import org.dromara.easytl.ast.ASTNode;
import org.dromara.easytl.ast.LambdaParameter;
import org.dromara.easytl.compiler.Evaluator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Runtime representation of a Lambda expression.
 *
 * <p>Encapsulates the Lambda's parameters, body, and captured variables.
 * When invoked, creates a new scope with parameter bindings and evaluates the body.</p>
 *
 * @author EasyTL Team
 */
public class LambdaObject {

    private final List<LambdaParameter> parameters;
    private final List<ASTNode> body;           // Block body statements (null for expression body)
    private final ASTNode expressionBody;       // Expression body (null for block body)
    private final boolean isBlockBody;
    private final Map<String, Object> capturedValues;
    private final Context definingContext;
    private final String sourceText;
    private final boolean templateJavaClassImportEnabled;
    private final Map<String, Class<?>> importsMap;

    /**
     * Create a Lambda object.
     *
     * @param parameters      the Lambda parameters
     * @param body            block body statements (null for expression body)
     * @param expressionBody  expression body (null for block body)
     * @param isBlockBody     true if this Lambda has a block body
     * @param capturedValues  captured variable values from the defining scope
     * @param definingContext the context where the Lambda was defined
     * @param sourceText      source text for error messages
     * @param templateJavaClassImportEnabled whether template Java class import is allowed in nested evaluation
     * @param importsMap      the imported class map (alias -> Class<?>)
     */
    public LambdaObject(List<LambdaParameter> parameters,
                       List<ASTNode> body,
                       ASTNode expressionBody,
                       boolean isBlockBody,
                       Map<String, Object> capturedValues,
                       Context definingContext,
                       String sourceText,
                       boolean templateJavaClassImportEnabled,
                       Map<String, Class<?>> importsMap) {
        this.parameters = parameters;
        this.body = body;
        this.expressionBody = expressionBody;
        this.isBlockBody = isBlockBody;
        this.capturedValues = capturedValues != null ? capturedValues : new HashMap<>();
        this.definingContext = definingContext;
        this.sourceText = sourceText;
        this.templateJavaClassImportEnabled = templateJavaClassImportEnabled;
        this.importsMap = importsMap != null ? importsMap : new HashMap<>();
    }

    /**
     * Get the Lambda parameters.
     *
     * @return the parameter list
     */
    public List<LambdaParameter> getParameters() {
        return parameters;
    }

    /**
     * Get the parameter count.
     *
     * @return number of parameters
     */
    public int getParameterCount() {
        return parameters != null ? parameters.size() : 0;
    }

    /**
     * Get the block body statements.
     *
     * @return the statements, or null for expression body
     */
    public List<ASTNode> getBody() {
        return body;
    }

    /**
     * Get the expression body.
     *
     * @return the expression, or null for block body
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

    /**
     * Invoke the Lambda with the given arguments.
     *
     * @param args the argument values
     * @return the result of invoking the Lambda
     * @throws RuntimeException if argument count doesn't match parameter count
     */
    public Object invoke(Object[] args) {
        // Validate argument count
        int paramCount = getParameterCount();
        if (args == null) {
            args = new Object[0];
        }
        if (args.length != paramCount) {
            throw new RuntimeException("Lambda expects " + paramCount +
                " parameters but received " + args.length);
        }

        // Create child context with captured values
        Context lambdaContext = definingContext.createChildContext();

        // Copy captured values into new context
        for (Map.Entry<String, Object> entry : capturedValues.entrySet()) {
            lambdaContext.put(entry.getKey(), entry.getValue());
        }

        // Bind parameters
        for (int i = 0; i < paramCount; i++) {
            LambdaParameter param = parameters.get(i);
            Object argValue = args[i];

            // Type validation if parameter has type declaration
            if (param.hasType()) {
                argValue = validateAndConvertType(param, argValue);
            }

            lambdaContext.put(param.getName(), argValue);
        }

        // 建立 Lambda 自身参数作用域：Lambda 体内 $/$0/{0} 引用 Lambda 自身入参
        lambdaContext.setArguments(args);

        // Create evaluator
        Evaluator evaluator = new Evaluator(lambdaContext, null, sourceText, templateJavaClassImportEnabled, importsMap);

        try {
            // Execute body
            if (isBlockBody) {
                return executeBlock(body, evaluator);
            } else {
                return expressionBody.accept(evaluator);
            }
        } catch (ReturnException e) {
            return e.getValue();
        }
    }

    /**
     * Execute a block of statements.
     *
     * @param statements the statements to execute
     * @param evaluator  the evaluator to use
     * @return the result of the last statement
     */
    private Object executeBlock(List<ASTNode> statements, Evaluator evaluator) {
        Object result = null;
        for (ASTNode statement : statements) {
            result = statement.accept(evaluator);
        }
        return result;
    }

    /**
     * Validate and convert the argument type if needed.
     *
     * @param param the parameter
     * @param value the argument value
     * @return the validated/converted value
     * @throws RuntimeException if type mismatch
     */
    private Object validateAndConvertType(LambdaParameter param, Object value) {
        if (value == null) {
            return null;
        }

        String expectedType = param.getType();
        Class<?> expectedClass = resolveType(expectedType);

        if (expectedClass != null && !expectedClass.isInstance(value)) {
            // Try primitive type matching
            if (!isAssignableWithPrimitive(expectedClass, value.getClass())) {
                throw new RuntimeException("Cannot convert '" + value + "' to " +
                    expectedType + " for parameter '" + param.getName() + "'");
            }
        }

        return value;
    }

    /**
     * Resolve a type name to a Class object.
     *
     * @param typeName the type name
     * @return the Class, or null if not found
     */
    private Class<?> resolveType(String typeName) {
        switch (typeName) {
            case "int": return Integer.class;
            case "long": return Long.class;
            case "double": return Double.class;
            case "float": return Float.class;
            case "boolean": return Boolean.class;
            case "String": return String.class;
            case "Object": return Object.class;
            default:
                try {
                    return Class.forName(typeName);
                } catch (ClassNotFoundException e) {
                    return null;
                }
        }
    }

    /**
     * Check if a primitive type assignment is compatible.
     *
     * @param expected the expected (wrapper) type
     * @param actual   the actual type
     * @return true if compatible
     */
    private boolean isAssignableWithPrimitive(Class<?> expected, Class<?> actual) {
        if (expected == Integer.class && (actual == Integer.class || actual == int.class)) return true;
        if (expected == Long.class && (actual == Long.class || actual == long.class)) return true;
        if (expected == Double.class && (actual == Double.class || actual == double.class)) return true;
        if (expected == Float.class && (actual == Float.class || actual == float.class)) return true;
        if (expected == Boolean.class && (actual == Boolean.class || actual == boolean.class)) return true;
        return false;
    }

    @Override
    public String toString() {
        return "LambdaObject{params=" + getParameterCount() + "}";
    }
}