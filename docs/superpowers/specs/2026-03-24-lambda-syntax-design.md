# Lambda Syntax Design

**Date:** 2026-03-24
**Status:** Draft
**Author:** Claude

## Overview

This document describes the design for adding Lambda expression support to EasyTL template engine. Lambda expressions enable passing functional code blocks to Java methods that accept functional interfaces, bringing modern functional programming capabilities to the template language.

## Goals

- Support Java-like Lambda syntax with multiple forms
- Enable passing Lambdas to Java methods accepting functional interfaces
- Maintain Java 8 compatibility with no third-party dependencies
- Support capture-by-value semantics (like Java)
- Allow optional type declarations on parameters

## Non-Goals

- Stream API integration (already works via method calls)
- Higher-order functions within templates (Lambdas passed to template-defined functions)
- Compile-time type checking (types are hints only)

## Syntax Specification

### Supported Forms

| Form | Example | Description |
|------|---------|-------------|
| No parameters, expression | `() -> "hello"` | Returns the expression value |
| No parameters, block | `() -> { "hello" }` | Block with implicit return |
| Single param, no parens | `x -> x * 2` | Shorthand for single parameter |
| Single param, with parens | `(x) -> x * 2` | Explicit parentheses |
| Multiple params | `(a, b) -> a + b` | Multiple parameters |
| With type declarations | `(int a, String s) -> s + a` | Optional type hints |
| Block body | `(a, b) -> { let t = a; t + b }` | Multi-statement with implicit return |
| Explicit return | `(x) -> { if (x < 0) return 0; x }` | Early exit with return keyword |

### Not Supported

| Feature | Reason |
|---------|--------|
| Switch expressions | Not supported in expression context (use if-else or ternary) |
| `return` in expression Lambda | Syntax error - must use block body for return |

### Key Semantics

- **Capture**: By value (like Java), captured variables must be effectively final
- **Return**: `return` keyword returns from Lambda only, not enclosing scope
- **Block return**: Last expression is implicit return; explicit `return` allowed for early exit
- **Expression Lambda return**: No `return` keyword allowed; expression value is returned
- **Types**: Optional type declarations, validated at runtime only
- **Recursion**: Lambda can reference itself via the variable name it's assigned to
- **Empty block body**: `() -> {}` returns `null`

### Lambda Capture and Recursion Semantics

#### Capture by Value

When a Lambda is created, it captures the **current values** of all visible variables from the enclosing context. This is "capture by value" semantics, similar to Java:

```easytl
{% let x = 10 %}
{% let fn = (a) -> a + x %}   {# Lambda captures x=10 #}
{% let x = 20 %}              {# Re-assignment creates new variable #}
{fn(5)}                       {# Returns 15, not 25 #}
```

The captured value is "frozen" at Lambda creation time. Later reassignments to the same variable name don't affect the captured value.

#### Recursive Lambda Support

For recursive lambdas like:
```easytl
{% let factorial = (n) -> n <= 1 ? 1 : n * factorial(n - 1) %}
```

At the moment of Lambda creation, `factorial` doesn't exist yet in the context. The Lambda handles this by:

1. **Defining Context Reference**: The LambdaObject holds a reference to the `definingContext` (not just captured values)
2. **Late Binding for Self-Reference**: When `factorial(n - 1)` is evaluated inside the Lambda:
   - First, check captured values (not found)
   - Then, look up in `definingContext` at invocation time (found!)
3. **Invocation-Time Resolution**: By the time the Lambda is invoked, the variable `factorial` has been assigned, so it can be found

This approach allows recursive lambdas while maintaining capture-by-value semantics for other variables.

### Examples

```easytl
{# No parameters #}
let getter = () -> 42

{# Single parameter #}
let doubler = x -> x * 2

{# Multiple parameters #}
let adder = (a, b) -> a + b

{# With type hints #}
let typed = (int a, String s) -> s + a

{# Block body with multiple statements #}
let complex = (a, b) -> {
  let temp = a * 2
  let result = temp + b
  result
}

{# Early return #}
let safeDiv = (a, b) -> {
  if (b == 0) { return 0 }
  a / b
}

{# Pass to Java method #}
{list.stream().filter(x -> x > 5).count()}

{# Recursive Lambda #}
let factorial = (n) -> n <= 1 ? 1 : n * factorial(n - 1)
```

## Architecture

### Component Overview

```
┌─────────────────────────────────────────────────────────────┐
│                        Lexer                                 │
│  - New token: ARROW (->)                                    │
│  - Handles '-' followed by '>' as single token              │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                        Parser                                │
│  - parseLambdaExpression() in parsePrimary()                │
│  - Distinguishes: (expr) vs (param) -> body                 │
│  - Creates LambdaExpression AST node                        │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                     AST Nodes                                │
│  - LambdaParameter (name, optional type, position)          │
│  - LambdaExpression (parameters, body, position)            │
│  - ReturnStatement (value, position)                        │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                      Evaluator                               │
│  - visitLambdaExpression() creates LambdaObject             │
│  - Captures variable values at Lambda creation time         │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                    Runtime Objects                           │
│  - LambdaObject: holds parameters, body, captured values    │
│  - LambdaAdapter: creates dynamic proxy for functional iface│
│  - FunctionalInterfaceUtils: detects functional interfaces  │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                   ReflectionUtils                            │
│  - Modified invokeMethod() to adapt Lambdas                 │
│  - Modified method matching to accept LambdaObject          │
└─────────────────────────────────────────────────────────────┘
```

## Implementation Details

### 1. Lexer Changes

#### New Token Types

**File:** `src/main/java/org/dromara/easytl/parser/TokenType.java`

```java
// Add to advanced operator section (after RANGE operators)
public static final int ARROW = 58;  // ->

// Add to keyword section
public static final int RETURN = 103;  // return
```

Update `getSymbol()` and `getName()` methods in TokenType to handle ARROW and RETURN.

#### Lexer Keyword Addition

**File:** `src/main/java/org/dromara/easytl/parser/Lexer.java`

Add `"return"` to the KEYWORDS map:

```java
static {
    // ... existing keywords ...
    KEYWORDS.put("return", TokenType.RETURN);
}
```

#### Lexer Modification

**File:** `src/main/java/org/dromara/easytl/parser/Lexer.java`

Modify `readOperatorOrDelimiter()` method:

```java
case '-':
    if (source.peek() == '>') {
        source.read(); // consume '>'
        return new Token(TokenType.ARROW, "->", line, column, source.getEndColumn(), position);
    }
    return new Token(TokenType.MINUS, "-", line, column, source.getEndColumn(), position);
```

Update `getSymbol()` and `getName()` methods in TokenType to handle ARROW.

### 2. Parser Changes

**File:** `src/main/java/org/dromara/easytl/parser/Parser.java`

#### TokenStream Changes

**File:** `src/main/java/org/dromara/easytl/parser/TokenStream.java`

The existing TokenStream uses a **ring buffer** implementation with default size 3. Lambda detection requires more lookahead tokens. We need to:

1. **Increase buffer size** to at least 16 for Lambda detection
2. **Add mark/reset capability** for token stream position save/restore

```java
// Change default buffer size for Lambda detection lookahead
private static final int DEFAULT_BUFFER_SIZE = 16;  // Increased from 3

// Add fields for mark/reset support
private int markedHead = -1;
private int markedBufferSize = -1;
private Token[] markedBuffer = null;

/**
 * Mark current position in the token stream.
 * Can be restored later with resetToMark().
 * Only one mark can be active at a time.
 */
public void mark() {
    markedHead = head;
    markedBufferSize = bufferSize;
    // Save buffer contents
    if (markedBuffer == null || markedBuffer.length < buffer.length) {
        markedBuffer = new Token[buffer.length];
    }
    System.arraycopy(buffer, 0, markedBuffer, 0, buffer.length);
}

/**
 * Reset to the previously marked position.
 * Must call mark() before resetToMark().
 */
public void resetToMark() {
    if (markedHead == -1) {
        throw new IllegalStateException("No mark set");
    }
    head = markedHead;
    bufferSize = markedBufferSize;
    System.arraycopy(markedBuffer, 0, buffer, 0, buffer.length);
    markedHead = -1;  // Clear mark
}

/**
 * Clear any active mark.
 */
public void clearMark() {
    markedHead = -1;
}
```

**Why buffer size 16?** Lambda detection for `(Type name, Type2 name2) -> body` requires peeking ahead through:
- `(` (1)
- `Type` (2)
- `name` (3)
- `,` (4)
- `Type2` (5)
- `name2` (6)
- `)` (7)
- `->` (8)

So minimum 8 tokens, plus safety margin for whitespace handling.

#### New Parsing Method

Add `parseLambdaExpression()` method:

```java
/**
 * Parse a Lambda expression.
 * Forms:
 *   () -> expr
 *   () -> { block }
 *   x -> expr
 *   (x) -> expr
 *   (a, b) -> expr
 *   (int a, String b) -> expr
 */
private ASTNode parseLambdaExpression() {
    Token startToken = current();
    int startLine = startToken.getLine();
    int startColumn = startToken.getStartColumn();

    List<LambdaParameter> parameters = new ArrayList<>();

    if (check(TokenType.LEFT_PAREN)) {
        // () or (params) -> body
        advance(); // consume '('

        if (check(TokenType.RIGHT_PAREN)) {
            // () -> body (no parameters)
            advance(); // consume ')'
        } else {
            // Parse parameter list
            parameters = parseLambdaParameterList();
            expect(TokenType.RIGHT_PAREN);
        }
    } else if (check(TokenType.IDENTIFIER)) {
        // Single parameter without parentheses: x -> body
        Token paramToken = advance();
        parameters.add(new LambdaParameter(paramToken.getValue(), null, positionFromToken(paramToken)));
    }

    expect(TokenType.ARROW);

    // Parse body
    List<ASTNode> body = null;
    ASTNode expressionBody = null;
    boolean isBlockBody = false;

    if (check(TokenType.LEFT_BRACE)) {
        // Block body
        isBlockBody = true;
        body = parseLambdaBlockBody();
    } else {
        // Expression body
        expressionBody = parseExpression();
    }

    Position position = new Position(startLine, startColumn,
        isBlockBody ? (body != null && !body.isEmpty() ? body.get(body.size() - 1).getEndLine() : endBrace.getLine()) : expressionBody.getEndLine(),
        isBlockBody ? (body != null && !body.isEmpty() ? body.get(body.size() - 1).getEndColumn() : endBrace.getEndColumn()) : expressionBody.getEndColumn());

    return new LambdaExpression(parameters, body, expressionBody, isBlockBody, position);
}
```

**Note:** For empty block bodies like `() -> {}`, the position uses the right brace token's position.
```

#### Parameter Parsing

```java
private List<LambdaParameter> parseLambdaParameterList() {
    List<LambdaParameter> params = new ArrayList<>();
    Set<String> seenNames = new HashSet<>();

    do {
        String type = null;
        String name;
        Token nameToken;

        // Check for type declaration
        if (check(TokenType.IDENTIFIER)) {
            Token first = current();
            // Peek to see if this is a type (followed by identifier)
            // or just a parameter name (followed by , or ))
            if (peek().getType() == TokenType.IDENTIFIER) {
                type = advance().getValue(); // consume type name
            }
            nameToken = expect(TokenType.IDENTIFIER);
            name = nameToken.getValue();
        } else {
            throw new TemplateSyntaxException("Expected parameter name",
                current().getLine(), current().getStartColumn(),
                current().getLine(), current().getEndColumn(), sourceText);
        }

        // Check for duplicate parameter names
        if (seenNames.contains(name)) {
            throw new TemplateSyntaxException("Duplicate parameter name '" + name + "'",
                current().getLine(), current().getStartColumn(),
                current().getLine(), current().getEndColumn(), sourceText);
        }
        seenNames.add(name);

        // Use nameToken for position (not current() which points to next token)
        params.add(new LambdaParameter(name, type, positionFromToken(nameToken)));

    } while (check(TokenType.COMMA) && advance() != null);

    return params;
}
```

#### Block Body Parsing

```java
private Token endBrace;  // Store the end brace token for position calculation

private List<ASTNode> parseLambdaBlockBody() {
    Token leftBrace = advance(); // consume '{'
    List<ASTNode> statements = new ArrayList<>();

    while (!check(TokenType.RIGHT_BRACE) && !check(TokenType.EOF)) {
        if (check(TokenType.RETURN)) {
            statements.add(parseReturnStatement());
        } else if (check(TokenType.LET)) {
            statements.add(parseLetStatementInBlock());
        } else if (check(TokenType.IF)) {
            statements.add(parseIfExpression());
        } else if (check(TokenType.FOR)) {
            statements.add(parseForExpression());
        } else {
            statements.add(parseExpression());
        }

        // Optional semicolon
        if (check(TokenType.SEMICOLON)) {
            advance();
        }
    }

    endBrace = expect(TokenType.RIGHT_BRACE);
    return statements;
}

/**
 * Parse return statement inside Lambda block.
 * Format: return [expression]
 */
private ASTNode parseReturnStatement() {
    Token returnToken = advance(); // consume 'return'

    ASTNode value = null;
    // Check if there's an expression after return (not followed by } or ;)
    if (!check(TokenType.RIGHT_BRACE) && !check(TokenType.SEMICOLON)) {
        value = parseExpression();
    }

    return new ReturnStatement(value, positionFromToken(returnToken));
}

/**
 * Parse let statement inside Lambda block.
 * Unlike template let statements, block let statements don't use {% %} syntax.
 * Format: let name = value
 */
private ASTNode parseLetStatementInBlock() {
    Token letToken = advance(); // consume 'let'
    Token name = expect(TokenType.IDENTIFIER);
    expect(TokenType.ASSIGN);
    ASTNode value = parseExpression();

    // Validate if expression has else branch
    if (value instanceof IfExpression) {
        IfExpression ifExpr = (IfExpression) value;
        if (!ifExpr.hasElse()) {
            throw new TemplateSyntaxException("If expression in let must include else branch",
                current().getLine(), current().getStartColumn(),
                current().getLine(), current().getEndColumn(), sourceText);
        }
    }

    return new LetStatement(name.getValue(), value);
}
```

**Note on Switch in Lambda Blocks:** The existing `parsePrimary()` only handles IF and FOR in expression context, not SWITCH. Therefore, **switch expressions are not supported inside Lambda blocks**. If switch support is needed, a new `parseSwitchExpression()` method would need to be added to handle expression-style switch syntax. For now, users can use if-else chains or ternary operators as alternatives.

#### Integration in parsePrimary()

```java
private ASTNode parsePrimary() {
    // ... existing cases ...

    // Lambda expression detection
    // Need to look ahead to distinguish from parenthesized expression
    if (check(TokenType.LEFT_PAREN)) {
        if (isLambdaStart()) {
            return parseLambdaExpression();
        }
        // Otherwise, parse as parenthesized expression
        advance();
        ASTNode expr = parseExpression();
        expect(TokenType.RIGHT_PAREN);
        return expr;
    }

    // Single parameter lambda without parens: identifier -> body
    if (check(TokenType.IDENTIFIER) && peek(1).getType() == TokenType.ARROW) {
        return parseLambdaExpression();
    }

    // ... rest of existing code ...
}

/**
 * Check if current position starts a Lambda expression.
 * Uses token-based lookahead to determine if '(' introduces:
 * - (expression) - parenthesized expression
 * - () -> body - no-param Lambda
 * - (params) -> body - Lambda with parameters
 *
 * Lambda detection rules:
 * 1. () -> (empty parens followed by arrow)
 * 2. (identifier) -> (single identifier followed by arrow)
 * 3. (identifier, identifier, ...) -> (parameter list followed by arrow)
 * 4. (Type identifier, ...) -> (typed parameters followed by arrow)
 *
 * Key distinction: In expressions, ')' is followed by operators or end-of-expression.
 * In lambdas, ')' is followed by '->'.
 */
private boolean isLambdaStart() {
    // Save current position in token stream using mark/reset
    tokens.mark();

    try {
        // Must start with '('
        if (!check(TokenType.LEFT_PAREN)) {
            return false;
        }
        advance(); // consume '('

        // Check for empty parameter list: () ->
        if (check(TokenType.RIGHT_PAREN)) {
            advance(); // consume ')'
            boolean isLambda = check(TokenType.ARROW);
            return isLambda;
        }

        // Try to parse as parameter list
        // Parameter patterns:
        // - identifier (untyped param)
        // - identifier identifier (typed param)
        // - identifier , ... (multiple params)

        while (true) {
            // Each parameter starts with an identifier (type or name)
            if (!check(TokenType.IDENTIFIER)) {
                return false;  // Not a valid parameter list
            }
            advance(); // consume identifier

            // Check if followed by another identifier (typed param: Type name)
            if (check(TokenType.IDENTIFIER)) {
                advance(); // consume parameter name
            }

            // Check what follows
            if (check(TokenType.COMMA)) {
                advance(); // consume ',' and continue to next param
                continue;
            } else if (check(TokenType.RIGHT_PAREN)) {
                advance(); // consume ')'
                // If followed by ARROW, it's a lambda
                return check(TokenType.ARROW);
            } else {
                // Something else - probably an expression
                return false;
            }
        }
    } finally {
        // Restore token stream position
        tokens.resetToMark();
    }
}
```

**Note:** This approach requires adding `getCurrentIndex()` and `setCurrentIndex()` methods to `TokenStream` class to enable token stream position save/restore for lookahead.

### 3. AST Nodes

#### LambdaParameter.java

**File:** `src/main/java/org/dromara/easytl/ast/LambdaParameter.java`

```java
package org.dromara.easytl.ast;

/**
 * Represents a single parameter in a Lambda expression.
 */
public class LambdaParameter {

    private final String name;
    private final String type;  // null if untyped
    private final Position position;

    public LambdaParameter(String name, String type, Position position) {
        this.name = name;
        this.type = type;
        this.position = position != null ? position : Position.unknown();
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public boolean hasType() {
        return type != null;
    }

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
```

#### LambdaExpression.java

**File:** `src/main/java/org/dromara/easytl/ast/LambdaExpression.java`

```java
package org.dromara.easytl.ast;

import java.util.Collections;
import java.util.List;

/**
 * Lambda expression AST node.
 *
 * <p>Supports two body forms:</p>
 * <ul>
 *   <li>Expression body: () -> expr</li>
 *   <li>Block body: () -> { statements }</li>
 * </ul>
 */
public class LambdaExpression extends ExpressionNode {

    private final List<LambdaParameter> parameters;
    private final List<ASTNode> body;           // Block body statements (null for expression body)
    private final ASTNode expressionBody;       // Expression body (null for block body)
    private final boolean isBlockBody;

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

    public List<LambdaParameter> getParameters() {
        return parameters;
    }

    public int getParameterCount() {
        return parameters.size();
    }

    public List<ASTNode> getBody() {
        return body;
    }

    public ASTNode getExpressionBody() {
        return expressionBody;
    }

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
```

#### ReturnStatement.java

**File:** `src/main/java/org/dromara/easytl/ast/ReturnStatement.java`

```java
package org.dromara.easytl.ast;

/**
 * Return statement for use inside Lambda block bodies.
 */
public class ReturnStatement extends ASTNode {

    private final ASTNode value;  // Can be null for void returns

    public ReturnStatement(ASTNode value, Position position) {
        super(position);
        this.value = value;
    }

    public ASTNode getValue() {
        return value;
    }

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
```

#### ASTVisitor Update

**File:** `src/main/java/org/dromara/easytl/ast/ASTVisitor.java`

Add new methods:

```java
// Lambda expression
T visitLambdaExpression(LambdaExpression node);

// Return statement
T visitReturnStatement(ReturnStatement node);
```

### 4. Runtime Components

#### LambdaObject.java

**File:** `src/main/java/org/dromara/easytl/runtime/LambdaObject.java`

```java
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
 * <p>Holds:</p>
 * <ul>
 *   <li>Parameter definitions</li>
 *   <li>Body AST (expression or block)</li>
 *   <li>Captured variable values (by value)</li>
 *   <li>Reference to defining context (for self-reference)</li>
 * </ul>
 */
public class LambdaObject {

    private final List<LambdaParameter> parameters;
    private final List<ASTNode> body;
    private final ASTNode expressionBody;
    private final boolean isBlockBody;
    private final Map<String, Object> capturedValues;
    private final Context definingContext;
    private final String sourceText;

    public LambdaObject(List<LambdaParameter> parameters,
                       List<ASTNode> body,
                       ASTNode expressionBody,
                       boolean isBlockBody,
                       Map<String, Object> capturedValues,
                       Context definingContext,
                       String sourceText) {
        this.parameters = parameters;
        this.body = body;
        this.expressionBody = expressionBody;
        this.isBlockBody = isBlockBody;
        this.capturedValues = capturedValues != null ? capturedValues : new HashMap<>();
        this.definingContext = definingContext;
        this.sourceText = sourceText;
    }

    /**
     * Get parameter definitions.
     */
    public List<LambdaParameter> getParameters() {
        return parameters;
    }

    /**
     * Get expected parameter count.
     */
    public int getParameterCount() {
        return parameters.size();
    }

    /**
     * Invoke the Lambda with the given arguments.
     *
     * @param args Argument values
     * @return Result of Lambda execution
     */
    public Object invoke(Object[] args) {
        // Validate argument count
        if (args.length != parameters.size()) {
            throw new RuntimeException("Lambda expects " + parameters.size() +
                " parameters but received " + args.length);
        }

        // Create child context with captured values
        Context lambdaContext = definingContext.createChildContext();

        // Copy captured values into new context
        for (Map.Entry<String, Object> entry : capturedValues.entrySet()) {
            lambdaContext.put(entry.getKey(), entry.getValue());
        }

        // Bind parameters
        for (int i = 0; i < parameters.size(); i++) {
            LambdaParameter param = parameters.get(i);
            Object argValue = args[i];

            // Type validation if parameter has type declaration
            if (param.hasType()) {
                argValue = validateAndConvertType(param, argValue);
            }

            lambdaContext.put(param.getName(), argValue);
        }

        // Create evaluator
        Evaluator evaluator = new Evaluator(lambdaContext, null, sourceText);

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
     * Execute block body statements.
     * Last statement's value is returned (implicit return).
     */
    private Object executeBlock(List<ASTNode> statements, Evaluator evaluator) {
        Object result = null;
        for (ASTNode statement : statements) {
            result = statement.accept(evaluator);
        }
        return result;
    }

    /**
     * Validate and convert argument type if parameter has type declaration.
     */
    private Object validateAndConvertType(LambdaParameter param, Object value) {
        String expectedType = param.getType();

        if (value == null) {
            return null;
        }

        // Type validation/conversion logic
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
        return "LambdaObject{params=" + parameters.size() + "}";
    }
}
```

#### ReturnException.java

**File:** `src/main/java/org/dromara/easytl/runtime/ReturnException.java`

```java
package org.dromara.easytl.runtime;

/**
 * Control flow exception used to implement return statements in Lambda expressions.
 *
 * <p>This exception is thrown when a return statement is executed inside a Lambda
 * and caught by the LambdaObject.invoke() method to return the value.</p>
 */
public class ReturnException extends RuntimeException {

    private final Object value;

    public ReturnException(Object value) {
        super(null, null, false, false);  // No stack trace for control flow
        this.value = value;
    }

    public Object getValue() {
        return value;
    }
}
```

#### FunctionalInterfaceUtils.java

**File:** `src/main/java/org/dromara/easytl/runtime/FunctionalInterfaceUtils.java`

```java
package org.dromara.easytl.runtime;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for detecting and working with functional interfaces.
 */
public class FunctionalInterfaceUtils {

    /**
     * Check if a class is a functional interface.
     *
     * <p>A functional interface has exactly one abstract method.</p>
     *
     * @param clazz Class to check
     * @return true if the class is a functional interface
     */
    public static boolean isFunctionalInterface(Class<?> clazz) {
        if (!clazz.isInterface()) {
            return false;
        }

        return getFunctionalMethod(clazz) != null;
    }

    /**
     * Get the single abstract method from a functional interface.
     *
     * @param clazz Functional interface class
     * @return The single abstract method, or null if not a functional interface
     */
    public static Method getFunctionalMethod(Class<?> clazz) {
        if (!clazz.isInterface()) {
            return null;
        }

        Method singleAbstractMethod = null;

        // Get all methods from the interface and its super-interfaces
        List<Method> abstractMethods = new ArrayList<>();

        for (Method method : clazz.getMethods()) {
            // Check if method is abstract (not default, not static)
            if (Modifier.isAbstract(method.getModifiers())) {
                abstractMethods.add(method);
            }
        }

        // Must have exactly one abstract method
        if (abstractMethods.size() == 1) {
            singleAbstractMethod = abstractMethods.get(0);
        }

        return singleAbstractMethod;
    }

    /**
     * Check if a Lambda's parameter count matches the functional interface method.
     *
     * @param lambda Lambda object
     * @param method Functional interface method
     * @return true if parameter counts match
     */
    public static boolean isParameterCountCompatible(LambdaObject lambda, Method method) {
        int lambdaParamCount = lambda.getParameterCount();
        int methodParamCount = method.getParameterCount();

        return lambdaParamCount == methodParamCount;
    }
}
```

#### LambdaAdapter.java

**File:** `src/main/java/org/dromara/easytl/runtime/LambdaAdapter.java`

```java
package org.dromara.easytl.runtime;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Adapts a LambdaObject to a functional interface using dynamic proxy.
 */
public class LambdaAdapter {

    /**
     * Create a dynamic proxy that implements the target functional interface
     * and delegates to the LambdaObject.
     *
     * @param lambda Lambda object to adapt
     * @param functionalInterface Target functional interface
     * @return Proxy instance implementing the functional interface
     */
    public static Object adapt(LambdaObject lambda, Class<?> functionalInterface) {
        // Validate it's a functional interface
        Method functionalMethod = FunctionalInterfaceUtils.getFunctionalMethod(functionalInterface);
        if (functionalMethod == null) {
            throw new IllegalArgumentException(
                "Class '" + functionalInterface.getName() + "' is not a functional interface");
        }

        // Validate parameter count
        if (!FunctionalInterfaceUtils.isParameterCountCompatible(lambda, functionalMethod)) {
            throw new IllegalArgumentException(
                "Lambda parameter count (" + lambda.getParameterCount() +
                ") does not match functional interface method parameter count (" +
                functionalMethod.getParameterCount() + ")");
        }

        // Create invocation handler
        InvocationHandler handler = new LambdaInvocationHandler(lambda, functionalMethod);

        // Create proxy
        return Proxy.newProxyInstance(
            functionalInterface.getClassLoader(),
            new Class<?>[] { functionalInterface },
            handler
        );
    }

    /**
     * Invocation handler that delegates to the LambdaObject.
     */
    private static class LambdaInvocationHandler implements InvocationHandler {

        private final LambdaObject lambda;
        private final Method functionalMethod;

        LambdaInvocationHandler(LambdaObject lambda, Method functionalMethod) {
            this.lambda = lambda;
            this.functionalMethod = functionalMethod;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            // Handle Object methods
            if (method.getDeclaringClass() == Object.class) {
                return method.invoke(this, args);
            }

            // Delegate to Lambda
            return lambda.invoke(args != null ? args : new Object[0]);
        }
    }
}
```

### 5. Evaluator Changes

**File:** `src/main/java/org/dromara/easytl/compiler/Evaluator.java`

Add new visitor methods:

```java
@Override
public Object visitLambdaExpression(LambdaExpression node) {
    // Capture variable values from the entire scope chain (by value, like Java)
    Map<String, Object> capturedValues = captureAllVisibleVariables(context);

    return new LambdaObject(
        node.getParameters(),
        node.getBody(),
        node.getExpressionBody(),
        node.isBlockBody(),
        capturedValues,
        context,
        sourceText
    );
}

/**
 * Capture all visible variables from the entire scope chain.
 * This ensures variables from parent scopes are also captured.
 *
 * @param ctx The context to capture variables from
 * @return Map of all visible variable names to their values
 */
private Map<String, Object> captureAllVisibleVariables(Context ctx) {
    Map<String, Object> allVariables = new HashMap<>();

    // Walk up the parent chain and capture all variables
    // Child scopes shadow parent scopes, so add parent variables first
    // then child variables will overwrite them
    java.util.List<Context> scopeChain = new java.util.ArrayList<>();
    Context current = ctx;
    while (current != null) {
        scopeChain.add(current);
        current = current.getParent();
    }

    // Add variables from outermost scope first, inner scopes will overwrite
    for (int i = scopeChain.size() - 1; i >= 0; i--) {
        Context scope = scopeChain.get(i);
        Map<String, Object> scopeVars = scope.getVariables();
        for (String varName : scopeVars.keySet()) {
            // Use scope.get() to properly handle null values (NULL_MARKER conversion)
            // and dynamic variables (TemplateVariable execution)
            allVariables.put(varName, scope.get(varName));
        }
    }

    return allVariables;
}

@Override
public Object visitReturnStatement(ReturnStatement node) {
    Object value = null;
    if (node.hasValue()) {
        value = node.getValue().accept(this);
    }
    throw new ReturnException(value);
}
```

### 6. ReflectionUtils Changes

**File:** `src/main/java/org/dromara/easytl/runtime/ReflectionUtils.java`

Modify `invokeMethod()`:

```java
public static Object invokeMethod(Object obj, String methodName, Object[] args) {
    if (obj == null) {
        throw new TemplateRuntimeException("Cannot invoke method '" + methodName + "' on null");
    }

    // ... existing special handling for Collection, List, Map ...

    Class<?> clazz = obj.getClass();

    // NEW: Adapt LambdaObject arguments before finding method
    // We need to find the method first to know parameter types
    Method method = findMethodWithLambdaSupport(clazz, methodName, args);

    if (method == null) {
        throw new TemplateRuntimeException("Method '" + methodName + "' not found in " + clazz.getName());
    }

    // NEW: Adapt LambdaObject arguments to functional interfaces
    Class<?>[] paramTypes = method.getParameterTypes();
    for (int i = 0; i < args.length; i++) {
        if (args[i] instanceof LambdaObject && i < paramTypes.length) {
            Class<?> paramType = paramTypes[i];
            if (FunctionalInterfaceUtils.isFunctionalInterface(paramType)) {
                args[i] = LambdaAdapter.adapt((LambdaObject) args[i], paramType);
            } else {
                throw new TemplateRuntimeException(
                    "Cannot convert Lambda to non-functional interface type '" + paramType.getName() + "'");
            }
        }
    }

    // ... existing invocation code ...
}
```

Add new method matching with Lambda support:

```java
/**
 * Find method with support for LambdaObject arguments.
 */
private static Method findMethodWithLambdaSupport(Class<?> clazz, String methodName, Object[] args) {
    Method[] methods = clazz.getMethods();
    Method bestMatch = null;
    int bestScore = Integer.MAX_VALUE;

    for (Method method : methods) {
        if (!method.getName().equals(methodName)) {
            continue;
        }

        Class<?>[] paramTypes = method.getParameterTypes();

        // Parameter count must match
        if (paramTypes.length != args.length) {
            continue;
        }

        // Calculate match score with Lambda support
        int score = calculateMatchScoreWithLambda(paramTypes, args);
        if (score >= 0 && score < bestScore) {
            bestMatch = method;
            bestScore = score;
        }
    }

    return bestMatch;
}

/**
 * Calculate match score with LambdaObject support.
 */
private static int calculateMatchScoreWithLambda(Class<?>[] paramTypes, Object[] args) {
    int score = 0;

    for (int i = 0; i < paramTypes.length; i++) {
        if (args[i] instanceof LambdaObject) {
            // LambdaObject matches any functional interface
            if (FunctionalInterfaceUtils.isFunctionalInterface(paramTypes[i])) {
                LambdaObject lambda = (LambdaObject) args[i];
                if (FunctionalInterfaceUtils.isParameterCountCompatible(lambda, paramTypes[i])) {
                    score += 0;  // Perfect match
                } else {
                    return -1;  // Parameter count mismatch
                }
            } else {
                return -1;  // Lambda doesn't match non-functional interface
            }
        } else if (args[i] == null) {
            // null can match any reference type
            if (paramTypes[i].isPrimitive()) {
                return -1;
            }
            score += 1;
        } else {
            // Use existing type matching logic
            Class<?> argType = args[i].getClass();
            if (paramTypes[i].equals(argType)) {
                score += 0;
            } else if (paramTypes[i].isAssignableFrom(argType)) {
                score += 1;
            } else if (isCompatiblePrimitive(paramTypes[i], argType)) {
                score += 2;
            } else {
                return -1;
            }
        }
    }

    return score;
}
```

## Error Handling

### Compile-Time Errors

| Error | Example | Message |
|-------|---------|---------|
| Invalid parameter syntax | `(a, ) -> x` | "Expected parameter name or ')' after ','" |
| Missing arrow | `(a) x` | "Expected '->' in Lambda expression" |
| Empty parameter with type | `(int) -> x` | "Expected parameter name after type" |
| Duplicate parameter name | `(a, a) -> x` | "Duplicate parameter name 'a'" |
| return in expression Lambda | `() -> return 5` | "'return' can only be used in Lambda block body" |

### Runtime Errors

| Error | Example | Message |
|-------|---------|---------|
| Wrong argument count | Lambda expects 2, called with 1 | "Lambda expects 2 parameters but received 1" |
| Type mismatch (with types) | `(int a) -> a` called with "string" | "Cannot convert 'string' to int for parameter 'a'" |
| Non-functional interface | Passing Lambda to String param | "Cannot convert Lambda to non-functional interface type 'java.lang.String'" |

## Testing Strategy

### Lexer Tests

```java
@Test
public void testArrowToken() {
    StringSource source = new StringSource("->");
    Lexer lexer = new Lexer(source);
    Token token = lexer.nextToken();
    assertEquals(TokenType.ARROW, token.getType());
}

@Test
public void testArrowNotMinusGt() {
    // With space between - and >, should be separate tokens
    StringSource source = new StringSource("a - > b");
    Lexer lexer = new Lexer(source);
    assertEquals(TokenType.IDENTIFIER, lexer.nextToken().getType());
    assertEquals(TokenType.MINUS, lexer.nextToken().getType());
    assertEquals(TokenType.GT, lexer.nextToken().getType());
}
```

### Parser Tests

```java
@Test
public void testNoParamLambda() {
    Parser parser = parse("() -> 42");
    ASTNode result = parser.parseExpression();
    assertTrue(result instanceof LambdaExpression);
    LambdaExpression lambda = (LambdaExpression) result;
    assertEquals(0, lambda.getParameterCount());
    assertFalse(lambda.isBlockBody());
}

@Test
public void testSingleParamLambdaNoParens() {
    Parser parser = parse("x -> x * 2");
    ASTNode result = parser.parseExpression();
    assertTrue(result instanceof LambdaExpression);
    LambdaExpression lambda = (LambdaExpression) result;
    assertEquals(1, lambda.getParameterCount());
    assertEquals("x", lambda.getParameters().get(0).getName());
}

@Test
public void testMultiParamLambda() {
    Parser parser = parse("(a, b) -> a + b");
    ASTNode result = parser.parseExpression();
    assertTrue(result instanceof LambdaExpression);
    LambdaExpression lambda = (LambdaExpression) result;
    assertEquals(2, lambda.getParameterCount());
}

@Test
public void testTypedParamLambda() {
    Parser parser = parse("(int a, String s) -> s + a");
    ASTNode result = parser.parseExpression();
    assertTrue(result instanceof LambdaExpression);
    LambdaExpression lambda = (LambdaExpression) result;
    assertEquals(2, lambda.getParameterCount());
    assertEquals("int", lambda.getParameters().get(0).getType());
    assertEquals("String", lambda.getParameters().get(1).getType());
}

@Test
public void testBlockLambda() {
    Parser parser = parse("() -> { 1 + 2 }");
    ASTNode result = parser.parseExpression();
    assertTrue(result instanceof LambdaExpression);
    LambdaExpression lambda = (LambdaExpression) result;
    assertTrue(lambda.isBlockBody());
}

@Test
public void testReturnInExpressionLambdaError() {
    assertThrows(TemplateSyntaxException.class, () -> {
        parse("() -> return 5");
    });
}
```

### Integration Tests

```java
@Test
public void testLambdaAsMethodArgument() {
    String template = "{#list.stream().filter(x -> x > 5).count()}";
    List<Integer> list = Arrays.asList(1, 10, 3, 20);
    String result = render(template, Map.of("list", list));
    assertEquals("2", result);
}

@Test
public void testCaptureByValue() {
    String template = "{% let x = 10 %}{% let fn = (a) -> a + x %}{% let x = 20 %}{fn(5)}";
    String result = render(template);
    assertEquals("15", result);  // Uses captured value 10
}

@Test
public void testRecursiveLambda() {
    String template = "{% let factorial = (n) -> n <= 1 ? 1 : n * factorial(n - 1) %}{factorial(5)}";
    String result = render(template);
    assertEquals("120", result);
}

@Test
public void testBlockLambdaWithReturn() {
    String template = "{% let safeDiv = (a, b) -> { if (b == 0) { return 0 } a / b } %}{safeDiv(10, 2)}";
    String result = render(template);
    assertEquals("5", result);
}

@Test
public void testLambdaToNonFunctionalInterface() {
    String template = "{str.length((x) -> x)}";  // length() expects no args, not functional
    assertThrows(TemplateRuntimeException.class, () -> render(template, Map.of("str", "hello")));
}

@Test
public void testLambdaInListLiteral() {
    String template = "{% let lambdas = [(x) -> x * 2, (x) -> x * 3] %}{lambdas.get(0)(5)}";
    String result = render(template);
    assertEquals("10", result);
}

@Test
public void testEmptyBlockLambda() {
    String template = "{% let fn = () -> {} %}{fn()}";
    String result = render(template);
    assertEquals("", result);  // Empty block returns null, which renders as empty string
}
```

## Implementation Order

1. **TokenStream** - Add `mark()`, `resetToMark()`, `clearMark()` methods for token lookahead (ring buffer compatible)
2. **Lexer** - Add ARROW and RETURN tokens
3. **AST Nodes** - Create LambdaParameter, LambdaExpression, ReturnStatement
4. **Parser** - Implement Lambda parsing with mark/reset based lookahead
5. **Runtime** - Create LambdaObject, ReturnException, FunctionalInterfaceUtils, LambdaAdapter
6. **Evaluator** - Add visitor methods with proper scope chain variable capture
7. **ReflectionUtils** - Add Lambda adaptation in method invocation
8. **Tests** - Unit tests and integration tests

## Known Limitations

### Type Resolution for Inner Classes

When using fully qualified type names in Lambda parameters, inner classes require the `$` notation:

```easytl
{# This won't work #}
let fn = (Outer.Inner x) -> x.value

{# Use this instead #}
let fn = (Outer$Inner x) -> x.value
```

This is a limitation of `Class.forName()` which doesn't resolve inner class dot notation.

## Open Questions

None - all questions have been resolved through the design discussion.

## References

- Java Lambda Expressions: https://docs.oracle.com/javase/tutorial/java/javaOO/lambdaexpressions.html
- Functional Interfaces: https://docs.oracle.com/javase/8/docs/api/java/lang/FunctionalInterface.html