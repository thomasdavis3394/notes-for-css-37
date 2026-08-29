# Lambda Syntax Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add Lambda expression support to EasyTL template engine with Java-like syntax, enabling functional programming patterns and Java functional interface compatibility.

**Architecture:** Extend the existing compiler pipeline (Lexer → Parser → AST → Evaluator) to recognize and process Lambda syntax. Add runtime components (LambdaObject, LambdaAdapter) to capture variables and adapt to Java functional interfaces via dynamic proxy.

**Tech Stack:** Java 8, no third-party dependencies, dynamic proxy for functional interface adaptation

---

## File Structure

### New Files
| File | Responsibility |
|------|----------------|
| `src/main/java/org/dromara/easytl/ast/LambdaParameter.java` | Represents a Lambda parameter with optional type |
| `src/main/java/org/dromara/easytl/ast/LambdaExpression.java` | Lambda expression AST node |
| `src/main/java/org/dromara/easytl/ast/ReturnStatement.java` | Return statement for Lambda block bodies |
| `src/main/java/org/dromara/easytl/runtime/LambdaObject.java` | Runtime representation of a Lambda |
| `src/main/java/org/dromara/easytl/runtime/ReturnException.java` | Control flow exception for return statements |
| `src/main/java/org/dromara/easytl/runtime/FunctionalInterfaceUtils.java` | Detects functional interfaces |
| `src/main/java/org/dromara/easytl/runtime/LambdaAdapter.java` | Adapts Lambda to functional interface via dynamic proxy |
| `src/test/java/org/dromara/easytl/LambdaTest.java` | Integration tests for Lambda functionality |

### Modified Files
| File | Changes |
|------|---------|
| `src/main/java/org/dromara/easytl/parser/TokenType.java` | Add ARROW and RETURN token types |
| `src/main/java/org/dromara/easytl/parser/Lexer.java` | Tokenize `->` and `return` keyword |
| `src/main/java/org/dromara/easytl/parser/TokenStream.java` | Add mark/reset for lookahead, increase buffer size |
| `src/main/java/org/dromara/easytl/parser/Parser.java` | Parse Lambda expressions |
| `src/main/java/org/dromara/easytl/ast/ASTVisitor.java` | Add visitor methods for Lambda nodes |
| `src/main/java/org/dromara/easytl/compiler/Evaluator.java` | Evaluate Lambda expressions |
| `src/main/java/org/dromara/easytl/runtime/ReflectionUtils.java` | Adapt Lambda to functional interface |

---

## Task 1: Add ARROW and RETURN Token Types

**Files:**
- Modify: `src/main/java/org/dromara/easytl/parser/TokenType.java`
- Test: `src/test/java/org/dromara/easytl/parser/LexerTest.java`

- [ ] **Step 1: Write the failing test for ARROW token**

```java
// In LexerTest.java
@Test
public void testArrowToken() {
    StringSource source = new StringSource("->");
    Lexer lexer = new Lexer(source, LexerMode.EXPRESSION);
    Token token = lexer.nextToken();
    assertEquals(TokenType.ARROW, token.getType());
    assertEquals("->", token.getValue());
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn test -Dtest=LexerTest#testArrowToken -q`
Expected: FAIL with compilation error (ARROW not found) or test failure

- [ ] **Step 3: Add ARROW constant to TokenType**

```java
// In TokenType.java, after RANGE_LEFT_OPEN_RIGHT_CLOSED (line 58)
public static final int ARROW = 58;  // ->
```

- [ ] **Step 4: Add ARROW to getSymbol() method**

```java
// In getSymbol(), after RANGE_LEFT_OPEN_RIGHT_CLOSED case
case ARROW: return "->";
```

- [ ] **Step 5: Add ARROW to getName() method**

```java
// In getName(), after RANGE_LEFT_OPEN_RIGHT_CLOSED case
case ARROW: return "ARROW";
```

- [ ] **Step 6: Run test to verify ARROW token constant works**

Run: `mvn test -Dtest=LexerTest#testArrowToken -q`
Expected: FAIL - Lexer doesn't produce ARROW token yet

- [ ] **Step 7: Write test for RETURN keyword**

```java
@Test
public void testReturnKeyword() {
    StringSource source = new StringSource("return");
    Lexer lexer = new Lexer(source, LexerMode.EXPRESSION);
    Token token = lexer.nextToken();
    assertEquals(TokenType.RETURN, token.getType());
}
```

- [ ] **Step 8: Add RETURN constant to TokenType**

```java
// In TokenType.java, in keyword section after AS (line 101)
public static final int RETURN = 102;  // return
```

- [ ] **Step 9: Add RETURN to getSymbol() and getName() methods**

```java
// In getSymbol()
case RETURN: return "return";

// In getName()
case RETURN: return "RETURN";
```

- [ ] **Step 10: Commit TokenType changes**

```bash
git add src/main/java/org/dromara/easytl/parser/TokenType.java
git commit -m "feat(tokens): add ARROW and RETURN token types for Lambda support"
```

---

## Task 2: Lexer - Tokenize Arrow Operator

**Files:**
- Modify: `src/main/java/org/dromara/easytl/parser/Lexer.java`
- Test: `src/test/java/org/dromara/easytl/parser/LexerTest.java`

- [ ] **Step 1: Write test for arrow tokenization**

```java
@Test
public void testArrowTokenization() {
    StringSource source = new StringSource("a -> b");
    Lexer lexer = new Lexer(source, LexerMode.EXPRESSION);
    assertEquals(TokenType.IDENTIFIER, lexer.nextToken().getType());
    Token arrow = lexer.nextToken();
    assertEquals(TokenType.ARROW, arrow.getType());
    assertEquals("->", arrow.getValue());
    assertEquals(TokenType.IDENTIFIER, lexer.nextToken().getType());
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn test -Dtest=LexerTest#testArrowTokenization -q`
Expected: FAIL - MINUS and GT tokens instead of ARROW

- [ ] **Step 3: Modify Lexer to handle `->` operator**

Find the `readOperatorOrDelimiter()` method in Lexer.java and modify the `case '-'` section:

```java
case '-':
    if (source.peek() == '>') {
        source.read(); // consume '>'
        return new Token(TokenType.ARROW, "->", line, column, source.getEndColumn(), position);
    }
    return new Token(TokenType.MINUS, "-", line, column, source.getEndColumn(), position);
```

- [ ] **Step 4: Run test to verify it passes**

Run: `mvn test -Dtest=LexerTest#testArrowTokenization -q`
Expected: PASS

- [ ] **Step 5: Write test for return keyword**

```java
@Test
public void testReturnKeywordInExpression() {
    StringSource source = new StringSource("return 42");
    Lexer lexer = new Lexer(source, LexerMode.EXPRESSION);
    Token ret = lexer.nextToken();
    assertEquals(TokenType.RETURN, ret.getType());
    assertEquals("return", ret.getValue());
}
```

- [ ] **Step 6: Add return to Lexer KEYWORDS map**

Find the KEYWORDS static block in Lexer.java and add:

```java
KEYWORDS.put("return", TokenType.RETURN);
```

- [ ] **Step 7: Run all Lexer tests to verify no regressions**

Run: `mvn test -Dtest=LexerTest -q`
Expected: All tests PASS

- [ ] **Step 8: Commit Lexer changes**

```bash
git add src/main/java/org/dromara/easytl/parser/Lexer.java
git commit -m "feat(lexer): tokenize arrow operator (->) and return keyword"
```

---

## Task 3: TokenStream - Add Mark/Reset for Lookahead

**Files:**
- Modify: `src/main/java/org/dromara/easytl/parser/TokenStream.java`
- Test: `src/test/java/org/dromara/easytl/parser/TokenStreamTest.java`

- [ ] **Step 1: Write test for mark/reset functionality**

```java
// In TokenStreamTest.java
@Test
public void testMarkAndResetToMark() throws Exception {
    StringSource source = new StringSource("a b c");
    Lexer lexer = new Lexer(source, LexerMode.EXPRESSION);
    TokenStream tokens = new TokenStream(lexer);

    // Read first token
    Token first = tokens.nextToken();
    assertEquals("a", first.getValue());

    // Mark position
    tokens.mark();

    // Read next tokens
    Token second = tokens.nextToken();
    Token third = tokens.nextToken();
    assertEquals("b", second.getValue());
    assertEquals("c", third.getValue());

    // Reset to mark
    tokens.resetToMark();

    // Should be back at 'b'
    Token afterReset = tokens.nextToken();
    assertEquals("b", afterReset.getValue());
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn test -Dtest=TokenStreamTest#testMarkAndResetToMark -q`
Expected: FAIL - methods not found

- [ ] **Step 3: Increase buffer size in TokenStream**

Change the default buffer size from 3 to 16:

```java
private static final int DEFAULT_BUFFER_SIZE = 16;
```

- [ ] **Step 4: Add mark/reset fields and methods to TokenStream**

Add fields after the existing field declarations:

```java
// Mark/reset support for lookahead
private int markedHead = -1;
private int markedBufferSize = -1;
private Token[] markedBuffer = null;
```

Add methods after the `getPosition()` method:

```java
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

- [ ] **Step 5: Run test to verify it passes**

Run: `mvn test -Dtest=TokenStreamTest#testMarkAndResetToMark -q`
Expected: PASS

- [ ] **Step 6: Run all TokenStream tests**

Run: `mvn test -Dtest=TokenStreamTest -q`
Expected: All tests PASS

- [ ] **Step 7: Commit TokenStream changes**

```bash
git add src/main/java/org/dromara/easytl/parser/TokenStream.java
git commit -m "feat(tokens): add mark/reset for lookahead, increase buffer size to 16"
```

---

## Task 4: Create AST Nodes for Lambda

**Files:**
- Create: `src/main/java/org/dromara/easytl/ast/LambdaParameter.java`
- Create: `src/main/java/org/dromara/easytl/ast/LambdaExpression.java`
- Create: `src/main/java/org/dromara/easytl/ast/ReturnStatement.java`
- Modify: `src/main/java/org/dromara/easytl/ast/ASTVisitor.java`
- Test: `src/test/java/org/dromara/easytl/ast/LambdaASTTest.java`

- [ ] **Step 1: Create LambdaParameter class**

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

- [ ] **Step 2: Create LambdaExpression class**

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
public class LambdaExpression extends ASTNode {

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

- [ ] **Step 3: Create ReturnStatement class**

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

- [ ] **Step 4: Add visitor methods to ASTVisitor interface**

Add to ASTVisitor.java after the last method:

```java
// Lambda expression
T visitLambdaExpression(LambdaExpression node);

// Return statement
T visitReturnStatement(ReturnStatement node);
```

- [ ] **Step 5: Write test for Lambda AST structure**

```java
package org.dromara.easytl.ast;

import org.dromara.easytl.parser.*;
import org.junit.Test;
import java.util.Arrays;
import static org.junit.Assert.*;

public class LambdaASTTest {

    @Test
    public void testLambdaParameterNoType() {
        LambdaParameter param = new LambdaParameter("x", null, Position.unknown());
        assertEquals("x", param.getName());
        assertNull(param.getType());
        assertFalse(param.hasType());
    }

    @Test
    public void testLambdaParameterWithType() {
        LambdaParameter param = new LambdaParameter("x", "int", Position.unknown());
        assertEquals("x", param.getName());
        assertEquals("int", param.getType());
        assertTrue(param.hasType());
    }

    @Test
    public void testLambdaExpressionStructure() {
        LambdaParameter param = new LambdaParameter("x", null, Position.unknown());
        NumberLiteral body = new NumberLiteral(42, Position.unknown());

        LambdaExpression lambda = new LambdaExpression(
            Arrays.asList(param), null, body, false, Position.unknown());

        assertEquals(1, lambda.getParameterCount());
        assertEquals("x", lambda.getParameters().get(0).getName());
        assertFalse(lambda.isBlockBody());
        assertNotNull(lambda.getExpressionBody());
    }

    @Test
    public void testReturnStatement() {
        NumberLiteral value = new NumberLiteral(10, Position.unknown());
        ReturnStatement ret = new ReturnStatement(value, Position.unknown());

        assertTrue(ret.hasValue());
        assertEquals(10, ((NumberLiteral) ret.getValue()).getValue());
    }
}
```

- [ ] **Step 6: Run tests to verify AST nodes**

Run: `mvn test -Dtest=LambdaASTTest -q`
Expected: All tests PASS

- [ ] **Step 7: Commit AST node changes**

```bash
git add src/main/java/org/dromara/easytl/ast/LambdaParameter.java \
        src/main/java/org/dromara/easytl/ast/LambdaExpression.java \
        src/main/java/org/dromara/easytl/ast/ReturnStatement.java \
        src/main/java/org/dromara/easytl/ast/ASTVisitor.java \
        src/test/java/org/dromara/easytl/ast/LambdaASTTest.java
git commit -m "feat(ast): add LambdaExpression, LambdaParameter, ReturnStatement nodes"
```

---

## Task 5: Parser - Parse Lambda Expressions

**Files:**
- Modify: `src/main/java/org/dromara/easytl/parser/Parser.java`
- Test: `src/test/java/org/dromara/easytl/parser/ParserTest.java`

- [ ] **Step 1: Write test for no-parameter Lambda**

```java
@Test
public void testParseNoParamLambda() {
    Parser parser = createParser("() -> 42");
    ASTNode result = parser.parseExpression();
    assertTrue(result instanceof LambdaExpression);
    LambdaExpression lambda = (LambdaExpression) result;
    assertEquals(0, lambda.getParameterCount());
    assertFalse(lambda.isBlockBody());
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn test -Dtest=ParserTest#testParseNoParamLambda -q`
Expected: FAIL - Lambda parsing not implemented

- [ ] **Step 3: Add isLambdaStart() method to Parser**

Add after the existing helper methods in Parser.java:

```java
/**
 * Check if current position starts a Lambda expression.
 * Uses token-based lookahead with mark/reset to determine if '(' introduces:
 * - (expression) - parenthesized expression
 * - () -> body - no-param Lambda
 * - (params) -> body - Lambda with parameters
 */
private boolean isLambdaStart() {
    // Must start with '('
    if (!check(TokenType.LEFT_PAREN)) {
        return false;
    }

    // Save current position
    tokens.mark();

    try {
        advance(); // consume '('

        // Check for empty parameter list: () ->
        if (check(TokenType.RIGHT_PAREN)) {
            advance(); // consume ')'
            boolean isLambda = check(TokenType.ARROW);
            return isLambda;
        }

        // Try to parse as parameter list
        while (true) {
            // Each parameter starts with an identifier (type or name)
            if (!check(TokenType.IDENTIFIER)) {
                return false;
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
        tokens.resetToMark();
    }
}
```

- [ ] **Step 4: Add parseLambdaExpression() method to Parser**

Add after the `isLambdaStart()` method:

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
        Position paramPos = new Position(paramToken.getLine(), paramToken.getStartColumn(),
                                         paramToken.getLine(), paramToken.getEndColumn());
        parameters.add(new LambdaParameter(paramToken.getValue(), null, paramPos));
    }

    expect(TokenType.ARROW);

    // Parse body
    List<ASTNode> body = null;
    ASTNode expressionBody = null;
    boolean isBlockBody = false;
    Token endBrace = null;

    if (check(TokenType.LEFT_BRACE)) {
        // Block body
        isBlockBody = true;
        Object[] blockResult = parseLambdaBlockBody();
        body = (List<ASTNode>) blockResult[0];
        endBrace = (Token) blockResult[1];
    } else {
        // Expression body
        expressionBody = parseExpression();
    }

    // Calculate position
    Position position;
    if (isBlockBody) {
        if (body != null && !body.isEmpty()) {
            ASTNode last = body.get(body.size() - 1);
            position = new Position(startLine, startColumn, last.getEndLine(), last.getEndColumn());
        } else if (endBrace != null) {
            position = new Position(startLine, startColumn, endBrace.getLine(), endBrace.getEndColumn());
        } else {
            position = new Position(startLine, startColumn, startLine, startColumn);
        }
    } else {
        position = new Position(startLine, startColumn,
            expressionBody.getEndLine(), expressionBody.getEndColumn());
    }

    return new LambdaExpression(parameters, body, expressionBody, isBlockBody, position);
}

/**
 * Parse Lambda parameter list inside parentheses.
 */
private List<LambdaParameter> parseLambdaParameterList() {
    List<LambdaParameter> params = new ArrayList<>();
    Set<String> seenNames = new HashSet<>();

    do {
        String type = null;
        String name;
        Token nameToken;

        // Check for type declaration using peek()
        // If current is identifier and next is also identifier, first is type
        if (check(TokenType.IDENTIFIER)) {
            if (peek().getType() == TokenType.IDENTIFIER) {
                type = advance().getValue(); // consume type name
            }
            nameToken = advance();
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

        // Create Position directly from token
        Position paramPos = new Position(nameToken.getLine(), nameToken.getStartColumn(),
                                         nameToken.getLine(), nameToken.getEndColumn());
        params.add(new LambdaParameter(name, type, paramPos));

    } while (check(TokenType.COMMA) && advance() != null);

    return params;
}

/**
 * Parse Lambda block body.
 * Returns Object[] { List<ASTNode> statements, Token endBrace }
 */
private Object[] parseLambdaBlockBody() {
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

    Token endBrace = expect(TokenType.RIGHT_BRACE);
    return new Object[] { statements, endBrace };
}

/**
 * Parse return statement inside Lambda block.
 */
private ASTNode parseReturnStatement() {
    Token returnToken = advance(); // consume 'return'

    ASTNode value = null;
    // Check if there's an expression after return (not followed by } or ;)
    if (!check(TokenType.RIGHT_BRACE) && !check(TokenType.SEMICOLON)) {
        value = parseExpression();
    }

    Position retPos = new Position(returnToken.getLine(), returnToken.getStartColumn(),
                                   returnToken.getLine(), returnToken.getEndColumn());
    return new ReturnStatement(value, retPos);
}

/**
 * Parse let statement inside Lambda block.
 */
private ASTNode parseLetStatementInBlock() {
    Token letToken = advance(); // consume 'let'
    Token name = expect(TokenType.IDENTIFIER);
    expect(TokenType.ASSIGN);
    ASTNode value = parseExpression();

    return new LetStatement(name.getValue(), value);
}
```

- [ ] **Step 5: Modify parsePrimary() to detect Lambda**

Find the `parsePrimary()` method and add Lambda detection. Find the section that handles `LEFT_PAREN`:

```java
case LEFT_PAREN:
    // Check for Lambda expression: () -> or (params) ->
    if (isLambdaStart()) {
        return parseLambdaExpression();
    }
    // Otherwise, parse as parenthesized expression
    advance();
    ASTNode expr = parseExpression();
    expect(TokenType.RIGHT_PAREN);
    return expr;
```

Also add handling for single-param Lambda without parens before the default case:

```java
// Single parameter lambda without parens: identifier -> body
if (check(TokenType.IDENTIFIER) && peek().getType() == TokenType.ARROW) {
    return parseLambdaExpression();
}
```

- [ ] **Step 6: Run test to verify basic Lambda parsing**

Run: `mvn test -Dtest=ParserTest#testParseNoParamLambda -q`
Expected: PASS

- [ ] **Step 7: Write test for single-parameter Lambda**

```java
@Test
public void testParseSingleParamLambdaNoParens() {
    Parser parser = createParser("x -> x * 2");
    ASTNode result = parser.parseExpression();
    assertTrue(result instanceof LambdaExpression);
    LambdaExpression lambda = (LambdaExpression) result;
    assertEquals(1, lambda.getParameterCount());
    assertEquals("x", lambda.getParameters().get(0).getName());
    assertFalse(lambda.isBlockBody());
}
```

- [ ] **Step 8: Write test for multi-parameter Lambda**

```java
@Test
public void testParseMultiParamLambda() {
    Parser parser = createParser("(a, b) -> a + b");
    ASTNode result = parser.parseExpression();
    assertTrue(result instanceof LambdaExpression);
    LambdaExpression lambda = (LambdaExpression) result;
    assertEquals(2, lambda.getParameterCount());
}
```

- [ ] **Step 9: Write test for typed parameters**

```java
@Test
public void testParseTypedParamLambda() {
    Parser parser = createParser("(int a, String s) -> s + a");
    ASTNode result = parser.parseExpression();
    assertTrue(result instanceof LambdaExpression);
    LambdaExpression lambda = (LambdaExpression) result;
    assertEquals(2, lambda.getParameterCount());
    assertEquals("int", lambda.getParameters().get(0).getType());
    assertEquals("String", lambda.getParameters().get(1).getType());
}
```

- [ ] **Step 10: Write test for block body Lambda**

```java
@Test
public void testParseBlockLambda() {
    Parser parser = createParser("() -> { 1 + 2 }");
    ASTNode result = parser.parseExpression();
    assertTrue(result instanceof LambdaExpression);
    LambdaExpression lambda = (LambdaExpression) result;
    assertTrue(lambda.isBlockBody());
    assertNotNull(lambda.getBody());
}
```

- [ ] **Step 11: Write test for Lambda with return statement**

```java
@Test
public void testParseLambdaWithReturn() {
    Parser parser = createParser("(a, b) -> { if (b == 0) { return 0 } a / b }");
    ASTNode result = parser.parseExpression();
    assertTrue(result instanceof LambdaExpression);
    LambdaExpression lambda = (LambdaExpression) result;
    assertTrue(lambda.isBlockBody());
}
```

- [ ] **Step 12: Run all new Parser tests**

Run: `mvn test -Dtest=ParserTest -q`
Expected: All tests PASS

- [ ] **Step 13: Commit Parser changes**

```bash
git add src/main/java/org/dromara/easytl/parser/Parser.java
git commit -m "feat(parser): parse Lambda expressions with multiple forms"
```

---

## Task 6: Runtime - Create LambdaObject and ReturnException

**Files:**
- Create: `src/main/java/org/dromara/easytl/runtime/ReturnException.java`
- Create: `src/main/java/org/dromara/easytl/runtime/LambdaObject.java`
- Test: `src/test/java/org/dromara/easytl/runtime/LambdaObjectTest.java`

- [ ] **Step 1: Create ReturnException class**

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

- [ ] **Step 2: Create LambdaObject class (basic version)**

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

    public List<LambdaParameter> getParameters() {
        return parameters;
    }

    public int getParameterCount() {
        return parameters.size();
    }

    /**
     * Invoke the Lambda with the given arguments.
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

    private Object executeBlock(List<ASTNode> statements, Evaluator evaluator) {
        Object result = null;
        for (ASTNode statement : statements) {
            result = statement.accept(evaluator);
        }
        return result;
    }

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

- [ ] **Step 3: Write test for LambdaObject invoke**

```java
package org.dromara.easytl.runtime;

import org.dromara.easytl.Context;
import org.dromara.easytl.ast.*;
import org.dromara.easytl.parser.*;
import org.junit.Test;
import java.util.*;

import static org.junit.Assert.*;

public class LambdaObjectTest {

    @Test
    public void testSimpleLambdaInvoke() throws Exception {
        // Parse: x -> x * 2
        StringSource source = new StringSource("x -> x * 2");
        Lexer lexer = new Lexer(source, LexerMode.EXPRESSION);
        TokenStream tokens = new TokenStream(lexer);
        Parser parser = new Parser(tokens);
        LambdaExpression lambdaAst = (LambdaExpression) parser.parseExpression();

        Context context = new Context();
        Map<String, Object> captured = new HashMap<>();

        LambdaObject lambda = new LambdaObject(
            lambdaAst.getParameters(),
            lambdaAst.getBody(),
            lambdaAst.getExpressionBody(),
            lambdaAst.isBlockBody(),
            captured,
            context,
            "x -> x * 2"
        );

        Object result = lambda.invoke(new Object[]{5});
        assertEquals(10, result);
    }

    @Test
    public void testMultiParamLambda() throws Exception {
        // Parse: (a, b) -> a + b
        StringSource source = new StringSource("(a, b) -> a + b");
        Lexer lexer = new Lexer(source, LexerMode.EXPRESSION);
        TokenStream tokens = new TokenStream(lexer);
        Parser parser = new Parser(tokens);
        LambdaExpression lambdaAst = (LambdaExpression) parser.parseExpression();

        Context context = new Context();
        LambdaObject lambda = new LambdaObject(
            lambdaAst.getParameters(),
            lambdaAst.getBody(),
            lambdaAst.getExpressionBody(),
            lambdaAst.isBlockBody(),
            new HashMap<>(),
            context,
            "(a, b) -> a + b"
        );

        Object result = lambda.invoke(new Object[]{3, 4});
        assertEquals(7, result);
    }

    @Test
    public void testWrongArgumentCount() throws Exception {
        StringSource source = new StringSource("(a, b) -> a + b");
        Lexer lexer = new Lexer(source, LexerMode.EXPRESSION);
        TokenStream tokens = new TokenStream(lexer);
        Parser parser = new Parser(tokens);
        LambdaExpression lambdaAst = (LambdaExpression) parser.parseExpression();

        LambdaObject lambda = new LambdaObject(
            lambdaAst.getParameters(),
            lambdaAst.getBody(),
            lambdaAst.getExpressionBody(),
            lambdaAst.isBlockBody(),
            new HashMap<>(),
            new Context(),
            "(a, b) -> a + b"
        );

        try {
            lambda.invoke(new Object[]{1});
            fail("Should throw for wrong argument count");
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains("expects 2"));
        }
    }
}
```

- [ ] **Step 4: Run tests to verify LambdaObject works**

Run: `mvn test -Dtest=LambdaObjectTest -q`
Expected: All tests PASS

- [ ] **Step 5: Commit runtime classes**

```bash
git add src/main/java/org/dromara/easytl/runtime/ReturnException.java \
        src/main/java/org/dromara/easytl/runtime/LambdaObject.java \
        src/test/java/org/dromara/easytl/runtime/LambdaObjectTest.java
git commit -m "feat(runtime): add LambdaObject and ReturnException for Lambda execution"
```

---

## Task 7: Runtime - Create FunctionalInterfaceUtils and LambdaAdapter

**Files:**
- Create: `src/main/java/org/dromara/easytl/runtime/FunctionalInterfaceUtils.java`
- Create: `src/main/java/org/dromara/easytl/runtime/LambdaAdapter.java`
- Test: `src/test/java/org/dromara/easytl/runtime/LambdaAdapterTest.java`

- [ ] **Step 1: Create FunctionalInterfaceUtils class**

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
     */
    public static boolean isFunctionalInterface(Class<?> clazz) {
        if (!clazz.isInterface()) {
            return false;
        }
        return getFunctionalMethod(clazz) != null;
    }

    /**
     * Get the single abstract method from a functional interface.
     */
    public static Method getFunctionalMethod(Class<?> clazz) {
        if (!clazz.isInterface()) {
            return null;
        }

        List<Method> abstractMethods = new ArrayList<>();

        for (Method method : clazz.getMethods()) {
            if (Modifier.isAbstract(method.getModifiers())) {
                abstractMethods.add(method);
            }
        }

        if (abstractMethods.size() == 1) {
            return abstractMethods.get(0);
        }

        return null;
    }

    /**
     * Check if a Lambda's parameter count matches the functional interface method.
     */
    public static boolean isParameterCountCompatible(LambdaObject lambda, Method method) {
        return lambda.getParameterCount() == method.getParameterCount();
    }
}
```

- [ ] **Step 2: Create LambdaAdapter class**

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
     * Create a dynamic proxy that implements the target functional interface.
     */
    public static Object adapt(LambdaObject lambda, Class<?> functionalInterface) {
        Method functionalMethod = FunctionalInterfaceUtils.getFunctionalMethod(functionalInterface);
        if (functionalMethod == null) {
            throw new IllegalArgumentException(
                "Class '" + functionalInterface.getName() + "' is not a functional interface");
        }

        if (!FunctionalInterfaceUtils.isParameterCountCompatible(lambda, functionalMethod)) {
            throw new IllegalArgumentException(
                "Lambda parameter count (" + lambda.getParameterCount() +
                ") does not match functional interface method parameter count (" +
                functionalMethod.getParameterCount() + ")");
        }

        InvocationHandler handler = new LambdaInvocationHandler(lambda, functionalMethod);

        return Proxy.newProxyInstance(
            functionalInterface.getClassLoader(),
            new Class<?>[] { functionalInterface },
            handler
        );
    }

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

- [ ] **Step 3: Write test for LambdaAdapter**

```java
package org.dromara.easytl.runtime;

import org.junit.Test;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.BiFunction;

import static org.junit.Assert.*;

public class LambdaAdapterTest {

    @Test
    public void testAdaptToFunction() {
        // Create a simple LambdaObject
        LambdaObject lambda = createTestLambda("(x) -> x * 2", 1);

        // Adapt to Function
        Object adapted = LambdaAdapter.adapt(lambda, Function.class);
        assertTrue(adapted instanceof Function);

        Function<Integer, Integer> func = (Function<Integer, Integer>) adapted;
        assertEquals(10, func.apply(5).intValue());
    }

    @Test
    public void testAdaptToPredicate() {
        LambdaObject lambda = createTestLambda("(x) -> x > 5", 1);

        Object adapted = LambdaAdapter.adapt(lambda, Predicate.class);
        assertTrue(adapted instanceof Predicate);

        Predicate<Integer> pred = (Predicate<Integer>) adapted;
        assertTrue(pred.test(10));
        assertFalse(pred.test(3));
    }

    @Test
    public void testAdaptToBiFunction() {
        LambdaObject lambda = createTestLambda("(a, b) -> a + b", 2);

        Object adapted = LambdaAdapter.adapt(lambda, BiFunction.class);
        assertTrue(adapted instanceof BiFunction);

        BiFunction<Integer, Integer, Integer> bifunc = (BiFunction<Integer, Integer, Integer>) adapted;
        assertEquals(7, bifunc.apply(3, 4).intValue());
    }

    @Test
    public void testNonFunctionalInterfaceRejected() {
        LambdaObject lambda = createTestLambda("(x) -> x", 1);

        try {
            LambdaAdapter.adapt(lambda, String.class);
            fail("Should reject non-functional interface");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("not a functional interface"));
        }
    }

    private LambdaObject createTestLambda(String expr, int paramCount) {
        // This is a mock - actual parsing happens in integration tests
        return new LambdaObject(
            java.util.Collections.nCopies(paramCount,
                new org.dromara.easytl.ast.LambdaParameter("p", null, null)),
            null,
            null,
            false,
            new java.util.HashMap<>(),
            new org.dromara.easytl.Context(),
            expr
        ) {
            // Override invoke for testing without full parser
            private final String expression;

            {
                this.expression = expr;
            }

            @Override
            public Object invoke(Object[] args) {
                // Simple mock behavior based on expression
                if (expr.contains("* 2")) {
                    return ((Number) args[0]).intValue() * 2;
                } else if (expr.contains("> 5")) {
                    return ((Number) args[0]).intValue() > 5;
                } else if (expr.contains("a + b")) {
                    return ((Number) args[0]).intValue() + ((Number) args[1]).intValue();
                }
                return args[0];
            }
        };
    }
}
```

- [ ] **Step 4: Run tests to verify adapter works**

Run: `mvn test -Dtest=LambdaAdapterTest -q`
Expected: All tests PASS

- [ ] **Step 5: Commit adapter classes**

```bash
git add src/main/java/org/dromara/easytl/runtime/FunctionalInterfaceUtils.java \
        src/main/java/org/dromara/easytl/runtime/LambdaAdapter.java \
        src/test/java/org/dromara/easytl/runtime/LambdaAdapterTest.java
git commit -m "feat(runtime): add FunctionalInterfaceUtils and LambdaAdapter for Java integration"
```

---

## Task 8: Evaluator - Evaluate Lambda Expressions

**Files:**
- Modify: `src/main/java/org/dromara/easytl/compiler/Evaluator.java`

- [ ] **Step 1: Add import for Lambda classes**

Add at the top of Evaluator.java imports:

```java
import org.dromara.easytl.runtime.LambdaObject;
import org.dromara.easytl.runtime.ReturnException;
```

- [ ] **Step 2: Add visitLambdaExpression method**

Add after the existing visitor methods in Evaluator.java:

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
 */
private Map<String, Object> captureAllVisibleVariables(Context ctx) {
    Map<String, Object> allVariables = new HashMap<>();

    // Walk up the parent chain and capture all variables
    List<Context> scopeChain = new ArrayList<>();
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
            allVariables.put(varName, scope.get(varName));
        }
    }

    return allVariables;
}
```

- [ ] **Step 3: Add visitReturnStatement method**

Add after the visitLambdaExpression method:

```java
@Override
public Object visitReturnStatement(ReturnStatement node) {
    Object value = null;
    if (node.hasValue()) {
        value = node.getValue().accept(this);
    }
    throw new ReturnException(value);
}
```

- [ ] **Step 4: Write integration test for Lambda evaluation**

```java
// In LambdaTest.java
@Test
public void testLambdaEvaluation() {
    Context context = new Context();
    Evaluator evaluator = new Evaluator(context, null, "x -> x * 2");

    // Parse Lambda
    StringSource source = new StringSource("x -> x * 2");
    Lexer lexer = new Lexer(source, LexerMode.EXPRESSION);
    TokenStream tokens = new TokenStream(lexer);
    Parser parser = new Parser(tokens);
    LambdaExpression lambdaAst = (LambdaExpression) parser.parseExpression();

    // Evaluate
    Object result = lambdaAst.accept(evaluator);
    assertTrue(result instanceof LambdaObject);

    LambdaObject lambda = (LambdaObject) result;
    assertEquals(10, lambda.invoke(new Object[]{5}));
}
```

- [ ] **Step 5: Run test to verify evaluation works**

Run: `mvn test -Dtest=LambdaTest#testLambdaEvaluation -q`
Expected: PASS

- [ ] **Step 6: Commit Evaluator changes**

```bash
git add src/main/java/org/dromara/easytl/compiler/Evaluator.java
git commit -m "feat(evaluator): evaluate Lambda expressions with variable capture"
```

---

## Task 9: ReflectionUtils - Adapt Lambda for Method Calls

**Files:**
- Modify: `src/main/java/org/dromara/easytl/runtime/ReflectionUtils.java`

- [ ] **Step 1: Add imports**

Add at the top of ReflectionUtils.java:

```java
import org.dromara.easytl.runtime.LambdaObject;
import org.dromara.easytl.runtime.LambdaAdapter;
import org.dromara.easytl.runtime.FunctionalInterfaceUtils;
```

- [ ] **Step 2: Modify invokeMethod to adapt Lambdas**

Find the `invokeMethod` method and modify it. Replace the method finding and invocation section with:

```java
public static Object invokeMethod(Object obj, String methodName, Object[] args) {
    if (obj == null) {
        throw new TemplateRuntimeException("Cannot invoke method '" + methodName + "' on null");
    }

    // ... existing special handling for Collection, List, Map ...

    Class<?> clazz = obj.getClass();

    // Find method with Lambda support
    Method method = findMethodWithLambdaSupport(clazz, methodName, args);

    if (method == null) {
        throw new TemplateRuntimeException("Method '" + methodName + "' not found in " + clazz.getName());
    }

    // Adapt LambdaObject arguments to functional interfaces
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
    try {
        return method.invoke(obj, args);
    } catch (IllegalAccessException e) {
        try {
            if (!method.isAccessible()) {
                method.setAccessible(true);
            }
            return method.invoke(obj, args);
        } catch (Exception ex) {
            throw new TemplateRuntimeException("Failed to invoke method '" + methodName + "': " + ex.getMessage(), ex);
        }
    } catch (Exception e) {
        throw new TemplateRuntimeException("Failed to invoke method '" + methodName + "': " + e.getMessage(), e);
    }
}
```

- [ ] **Step 3: Add findMethodWithLambdaSupport method**

Add after the existing `findMethodCached` method:

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

        if (paramTypes.length != args.length) {
            // Check varargs
            if (method.isVarArgs() && args.length >= paramTypes.length - 1) {
                if (bestMatch == null) {
                    bestMatch = method;
                }
            }
            continue;
        }

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
                    return -1;
                }
            } else {
                return -1;
            }
        } else if (args[i] == null) {
            if (paramTypes[i].isPrimitive()) {
                return -1;
            }
            score += 1;
        } else {
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

- [ ] **Step 4: Write test for Lambda method call adaptation**

```java
@Test
public void testLambdaPassedToJavaMethod() throws Exception {
    // Create context with a list
    Context context = new Context();
    List<Integer> list = Arrays.asList(1, 10, 3, 20);
    context.put("list", list);

    // Template: {list.stream().filter(x -> x > 5).count()}
    // First evaluate the Lambda
    StringSource source = new StringSource("x -> x > 5");
    Lexer lexer = new Lexer(source, LexerMode.EXPRESSION);
    TokenStream tokens = new TokenStream(lexer);
    Parser parser = new Parser(tokens);
    LambdaExpression lambdaAst = (LambdaExpression) parser.parseExpression();

    Evaluator evaluator = new Evaluator(context, null, "x -> x > 5");
    LambdaObject lambda = (LambdaObject) lambdaAst.accept(evaluator);

    // Test passing to filter
    Object result = ReflectionUtils.invokeMethod(
        list.stream(),
        "filter",
        new Object[]{lambda}
    );

    assertNotNull(result);
    assertTrue(result instanceof java.util.stream.Stream);
}
```

- [ ] **Step 5: Run test to verify adaptation works**

Run: `mvn test -Dtest=LambdaTest#testLambdaPassedToJavaMethod -q`
Expected: PASS

- [ ] **Step 6: Commit ReflectionUtils changes**

```bash
git add src/main/java/org/dromara/easytl/runtime/ReflectionUtils.java
git commit -m "feat(reflection): adapt Lambda to functional interface in method calls"
```

---

## Task 10: Integration Tests

**Files:**
- Create: `src/test/java/org/dromara/easytl/LambdaIntegrationTest.java`

- [ ] **Step 1: Create comprehensive integration test class**

```java
package org.dromara.easytl;

import org.dromara.easytl.runtime.LambdaObject;
import org.junit.Test;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class LambdaIntegrationTest {

    @Test
    public void testNoParamLambda() {
        Template template = TemplateEngine.compile("{% let fn = () -> 42 %}{fn()}");
        String result = template.render(new Context());
        assertEquals("42", result);
    }

    @Test
    public void testSingleParamLambda() {
        Template template = TemplateEngine.compile("{% let doubler = x -> x * 2 %}{doubler(5)}");
        String result = template.render(new Context());
        assertEquals("10", result);
    }

    @Test
    public void testMultiParamLambda() {
        Template template = TemplateEngine.compile("{% let adder = (a, b) -> a + b %}{adder(3, 4)}");
        String result = template.render(new Context());
        assertEquals("7", result);
    }

    @Test
    public void testCaptureByValue() {
        Context context = new Context();
        Template template = TemplateEngine.compile(
            "{% let x = 10 %}" +
            "{% let fn = (a) -> a + x %}" +
            "{% let x = 20 %}" +
            "{fn(5)}"
        );
        String result = template.render(context);
        assertEquals("15", result);  // Uses captured value 10
    }

    @Test
    public void testBlockLambda() {
        Template template = TemplateEngine.compile(
            "{% let fn = (a, b) -> { " +
            "  let temp = a * 2 " +
            "  temp + b " +
            "} %}" +
            "{fn(3, 4)}"
        );
        String result = template.render(new Context());
        assertEquals("10", result);  // 3 * 2 + 4 = 10
    }

    @Test
    public void testReturnStatement() {
        Template template = TemplateEngine.compile(
            "{% let safeDiv = (a, b) -> { " +
            "  if (b == 0) { return 0 } " +
            "  a / b " +
            "} %}" +
            "{safeDiv(10, 2)} {safeDiv(10, 0)}"
        );
        String result = template.render(new Context());
        assertEquals("5 0", result);
    }

    @Test
    public void testLambdaToStreamFilter() {
        Context context = new Context();
        List<Integer> list = Arrays.asList(1, 10, 3, 20, 5, 30);
        context.put("list", list);

        Template template = TemplateEngine.compile(
            "{list.stream().filter(x -> x > 5).collect(Collectors.toList())}"
        );
        context.put("Collectors", Collectors.class);

        String result = template.render(context);
        assertEquals("[10, 20, 30]", result);
    }

    @Test
    public void testLambdaToStreamMap() {
        Context context = new Context();
        List<Integer> list = Arrays.asList(1, 2, 3);
        context.put("list", list);
        context.put("Collectors", Collectors.class);

        Template template = TemplateEngine.compile(
            "{list.stream().map(x -> x * 2).collect(Collectors.toList())}"
        );
        String result = template.render(context);
        assertEquals("[2, 4, 6]", result);
    }

    @Test
    public void testRecursiveLambda() {
        Template template = TemplateEngine.compile(
            "{% let factorial = (n) -> n <= 1 ? 1 : n * factorial(n - 1) %}" +
            "{factorial(5)}"
        );
        String result = template.render(new Context());
        assertEquals("120", result);
    }

    @Test
    public void testLambdaInListLiteral() {
        Template template = TemplateEngine.compile(
            "{% let fns = [(x) -> x * 2, (x) -> x * 3] %}" +
            "{fns.get(0)(5)} {fns.get(1)(5)}"
        );
        String result = template.render(new Context());
        assertEquals("10 15", result);
    }

    @Test
    public void testTypedParameters() {
        Template template = TemplateEngine.compile(
            "{% let fn = (int a, String s) -> s + a %}" +
            "{fn(42, \"value\")}"
        );
        String result = template.render(new Context());
        assertEquals("value42", result);
    }

    @Test
    public void testEmptyBlockLambda() {
        Template template = TemplateEngine.compile(
            "{% let fn = () -> {} %}" +
            "{fn()}"
        );
        String result = template.render(new Context());
        assertEquals("", result);  // Empty block returns null
    }
}
```

- [ ] **Step 2: Run all integration tests**

Run: `mvn test -Dtest=LambdaIntegrationTest -q`
Expected: All tests PASS

- [ ] **Step 3: Run full test suite**

Run: `mvn test -q`
Expected: All tests PASS

- [ ] **Step 4: Commit integration tests**

```bash
git add src/test/java/org/dromara/easytl/LambdaIntegrationTest.java
git commit -m "test: add comprehensive Lambda integration tests"
```

---

## Final Step: Merge and Document

- [ ] **Step 1: Run full test suite one more time**

Run: `mvn test`
Expected: All tests PASS

- [ ] **Step 2: Create final commit**

```bash
git add -A
git commit -m "feat: complete Lambda expression support

- Add ARROW (->) and RETURN tokens
- Parse Lambda expressions with multiple forms
- Support capture-by-value semantics
- Adapt Lambda to Java functional interfaces
- Support recursive Lambda via definingContext reference
- Handle return statements in block bodies"
```

---

## Known Limitations

1. **Switch expressions not supported in Lambda body** - Use if-else or ternary instead
2. **Inner class types require $ notation** - Use `Outer$Inner` instead of `Outer.Inner`
3. **Type checking is runtime only** - Type declarations are hints, validated at invocation time