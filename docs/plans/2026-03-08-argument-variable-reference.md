# Argument Variable Reference Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Enhance argument index reference syntax with strict error handling and new variable-style argument reference syntax.

**Architecture:**
1. Add error handling for existing `{0}` syntax - throw errors when index out of bounds or value is null
2. Add new `$0`, `$1` syntax that works as variable-style argument reference, supporting `??` operator
3. Create new AST node `ArgumentVariable` to distinguish from `ArgumentAccess`

**Tech Stack:** Java 8, Maven, JUnit 5

---

## Summary of Changes

| Syntax | Behavior | Out of Bounds | Null Value | Supports `??` |
|--------|----------|---------------|------------|---------------|
| `{0}` | Simple interpolation | Error | Error | No |
| `{$0}` | Variable-style reference | Error | Error | Yes |

## Files to Modify/Create

### New Files
- `src/main/java/org/dromara/easytl/ast/ArgumentVariable.java` - New AST node

### Modified Files
- `src/main/java/org/dromara/easytl/parser/Lexer.java` - Recognize `$` + number pattern
- `src/main/java/org/dromara/easytl/parser/TokenType.java` - Add new token type
- `src/main/java/org/dromara/easytl/parser/Parser.java` - Parse `$0` as `ArgumentVariable`
- `src/main/java/org/dromara/easytl/compiler/Evaluator.java` - Handle both nodes with different error behaviors
- `src/main/java/org/dromara/easytl/Context.java` - Add methods with error throwing
- `src/main/java/org/dromara/easytl/ast/ASTVisitor.java` - Add visit method for `ArgumentVariable`

### Test Files
- `src/test/java/org/dromara/easytl/ArgumentVariableTest.java` - Tests for new syntax
- `src/test/java/org/dromara/easytl/ArgumentErrorTest.java` - Tests for error handling

---

## Task 1: Add New Token Type for Dollar-Number

**Files:**
- Modify: `src/main/java/org/dromara/easytl/parser/TokenType.java`

**Step 1: Write the failing test**

Not applicable - this is adding a constant, no test needed.

**Step 2: Add token type constant**

Add to `TokenType.java`:

```java
// In the literal/identifier section (around line 70)
public static final int DOLLAR_NUMBER = 81;    // $0, $1, $2, ... (argument variable reference)
```

Update `getSymbol` method:

```java
// Add in getSymbol method
case DOLLAR_NUMBER: return "$<number>";
```

Update `getName` method:

```java
// Add in getName method
case DOLLAR_NUMBER: return "DOLLAR_NUMBER";
```

**Step 3: Commit**

```bash
git add src/main/java/org/dromara/easytl/parser/TokenType.java
git commit -m "feat: add DOLLAR_NUMBER token type for argument variable reference"
```

---

## Task 2: Create ArgumentVariable AST Node

**Files:**
- Create: `src/main/java/org/dromara/easytl/ast/ArgumentVariable.java`
- Modify: `src/main/java/org/dromara/easytl/ast/ASTVisitor.java`

**Step 1: Write the failing test**

Create `src/test/java/org/dromara/easytl/ast/ArgumentVariableTest.java`:

```java
package org.dromara.easytl.ast;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ArgumentVariableTest {

    @Test
    void testArgumentVariableCreation() {
        ArgumentVariable var = new ArgumentVariable(0);
        assertEquals(0, var.getIndex());
    }

    @Test
    void testArgumentVariableWithPosition() {
        Position pos = new Position(1, 5, 1, 7);
        ArgumentVariable var = new ArgumentVariable(1, pos);
        assertEquals(1, var.getIndex());
        assertEquals(pos, var.getPosition());
    }

    @Test
    void testAccept() {
        ArgumentVariable var = new ArgumentVariable(2);
        ASTVisitor<String> visitor = new ASTVisitor<String>() {
            @Override
            public String visitArgumentVariable(ArgumentVariable node) {
                return "arg:" + node.getIndex();
            }
        };
        assertEquals("arg:2", var.accept(visitor));
    }
}
```

**Step 2: Run test to verify it fails**

Run: `mvn test -Dtest=ArgumentVariableTest`
Expected: FAIL - class not found

**Step 3: Create ArgumentVariable class**

Create `src/main/java/org/dromara/easytl/ast/ArgumentVariable.java`:

```java
package org.dromara.easytl.ast;

/**
 * 参数变量节点
 * 表示通过 $index 语法访问外部传入的参数，例如：{$0}、${$1}、{{$2}}
 * 与 ArgumentAccess 不同，此节点支持与其他运算符组合使用（如 ??）
 *
 * @author EasyTL Team
 */
public class ArgumentVariable extends ASTNode {

    /** 参数索引（0代表第一个参数，1代表第二个，以此类推） */
    private final int index;

    /**
     * 构造参数变量节点
     *
     * @param index 参数索引
     */
    public ArgumentVariable(int index) {
        this.index = index;
    }

    /**
     * 构造参数变量节点（带位置信息）
     *
     * @param index 参数索引
     * @param position 位置信息
     */
    public ArgumentVariable(int index, Position position) {
        super(position);
        this.index = index;
    }

    /**
     * 获取参数索引
     *
     * @return 参数索引
     */
    public int getIndex() {
        return index;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitArgumentVariable(this);
    }

    @Override
    public String toString() {
        return "ArgumentVariable{" + "index=" + index + '}';
    }
}
```

**Step 4: Add visit method to ASTVisitor**

Add to `src/main/java/org/dromara/easytl/ast/ASTVisitor.java`:

```java
/**
 * 访问参数变量节点
 *
 * @param node 参数变量节点
 * @return 访问结果
 */
T visitArgumentVariable(ArgumentVariable node);
```

**Step 5: Run test to verify it passes**

Run: `mvn test -Dtest=ArgumentVariableTest`
Expected: PASS

**Step 6: Commit**

```bash
git add src/main/java/org/dromara/easytl/ast/ArgumentVariable.java
git add src/main/java/org/dromara/easytl/ast/ASTVisitor.java
git add src/test/java/org/dromara/easytl/ast/ArgumentVariableTest.java
git commit -m "feat: add ArgumentVariable AST node for $index syntax"
```

---

## Task 3: Update Lexer to Recognize Dollar-Number Pattern

**Files:**
- Modify: `src/main/java/org/dromara/easytl/parser/Lexer.java`

**Step 1: Write the failing test**

Add to `src/test/java/org/dromara/easytl/parser/LexerTest.java`:

```java
@Test
void testDollarNumberToken() {
    // Test $0, $1, $10 etc.
    Lexer lexer = new Lexer("{$0}");
    TokenStream stream = lexer.tokenize();

    // Skip SINGLE_BRACE_EXPR_BEGIN
    stream.next();

    Token token = stream.next();
    assertEquals(TokenType.DOLLAR_NUMBER, token.getType());
    assertEquals("$0", token.getValue());

    // Test larger number
    lexer = new Lexer("{$10}");
    stream = lexer.tokenize();
    stream.next(); // Skip begin
    token = stream.next();
    assertEquals(TokenType.DOLLAR_NUMBER, token.getType());
    assertEquals("$10", token.getValue());
}

@Test
void testDollarNumberNotFollowedByLetter() {
    // $abc should not be DOLLAR_NUMBER
    Lexer lexer = new Lexer("{$abc}");
    TokenStream stream = lexer.tokenize();
    stream.next(); // Skip begin
    Token token = stream.next();
    assertEquals(TokenType.IDENTIFIER, token.getType());
    assertEquals("$abc", token.getValue());
}
```

**Step 2: Run test to verify it fails**

Run: `mvn test -Dtest=LexerTest#testDollarNumberToken`
Expected: FAIL

**Step 3: Implement Lexer change**

In `Lexer.java`, find the expression mode parsing section (around where identifiers are parsed), add logic to recognize `$` followed by digits:

```java
// In scanExpression() method, after checking for '$' that starts ${...}
// Add check for $ followed by digit
if (ch == '$') {
    int nextChar = source.peek();
    if (nextChar >= '0' && nextChar <= '9') {
        // This is $0, $1, etc.
        int startLine = line;
        int startColumn = column;
        int startPosition = position;

        StringBuilder sb = new StringBuilder();
        sb.append((char) source.read()); // consume '$'
        column++;
        position++;

        // Read digits
        while (source.peek() >= '0' && source.peek() <= '9') {
            sb.append((char) source.read());
            column++;
            position++;
        }

        return new Token(TokenType.DOLLAR_NUMBER, sb.toString(), startLine, startColumn, startPosition);
    }
    // Otherwise, fall through to identifier handling
}
```

**Step 4: Run test to verify it passes**

Run: `mvn test -Dtest=LexerTest#testDollarNumberToken`
Expected: PASS

**Step 5: Commit**

```bash
git add src/main/java/org/dromara/easytl/parser/Lexer.java
git add src/test/java/org/dromara/easytl/parser/LexerTest.java
git commit -m "feat: lexer recognize $number pattern for argument variable"
```

---

## Task 4: Update Parser to Parse $0 as ArgumentVariable

**Files:**
- Modify: `src/main/java/org/dromara/easytl/parser/Parser.java`

**Step 1: Write the failing test**

Add to `src/test/java/org/dromara/easytl/parser/ParserTest.java`:

```java
@Test
void testParseArgumentVariable() {
    Parser parser = new Parser("{$0}");
    ASTNode node = parser.parse();

    assertTrue(node instanceof TemplateNode);
    TemplateNode template = (TemplateNode) node;
    assertEquals(1, template.getChildren().size());

    SingleBraceExpressionNode expr = (SingleBraceExpressionNode) template.getChildren().get(0);
    assertTrue(expr.getExpression() instanceof ArgumentVariable);
    ArgumentVariable argVar = (ArgumentVariable) expr.getExpression();
    assertEquals(0, argVar.getIndex());
}

@Test
void testParseArgumentVariableWithElvis() {
    Parser parser = new Parser("{$0 ?? 'default'}");
    ASTNode node = parser.parse();

    assertTrue(node instanceof TemplateNode);
    TemplateNode template = (TemplateNode) node;
    SingleBraceExpressionNode expr = (SingleBraceExpressionNode) template.getChildren().get(0);

    assertTrue(expr.getExpression() instanceof ElvisExpression);
    ElvisExpression elvis = (ElvisExpression) expr.getExpression();
    assertTrue(elvis.getLeft() instanceof ArgumentVariable);
}
```

**Step 2: Run test to verify it fails**

Run: `mvn test -Dtest=ParserTest#testParseArgumentVariable`
Expected: FAIL

**Step 3: Implement Parser change**

In `Parser.java`, add handling for `DOLLAR_NUMBER` token in `parsePrimary()` method:

```java
// In parsePrimary() method, add case for DOLLAR_NUMBER
if (check(TokenType.DOLLAR_NUMBER)) {
    Token token = advance();
    String value = token.getValue();
    // Extract number from "$0", "$1", etc.
    int index = Integer.parseInt(value.substring(1));
    return new ArgumentVariable(index, token.getPosition());
}
```

**Step 4: Run test to verify it passes**

Run: `mvn test -Dtest=ParserTest#testParseArgumentVariable`
Expected: PASS

**Step 5: Commit**

```bash
git add src/main/java/org/dromara/easytl/parser/Parser.java
git add src/test/java/org/dromara/easytl/parser/ParserTest.java
git commit -m "feat: parser recognize $number as ArgumentVariable"
```

---

## Task 5: Add Context Methods with Error Throwing

**Files:**
- Modify: `src/main/java/org/dromara/easytl/Context.java`
- Modify: `src/main/java/org/dromara/easytl/exception/TemplateRuntimeException.java` (if needed)

**Step 1: Write the failing test**

Add to `src/test/java/org/dromara/easytl/ContextTest.java`:

```java
import org.dromara.easytl.exception.TemplateRuntimeException;

@Test
void testGetArgumentStrictOutOfBounds() {
    Context context = new Context();
    context.setArguments("a", "b");

    assertThrows(TemplateRuntimeException.class, () -> {
        context.getArgumentStrict(5);
    });
}

@Test
void testGetArgumentStrictNull() {
    Context context = new Context();
    context.setArguments("a", null, "c");

    assertThrows(TemplateRuntimeException.class, () -> {
        context.getArgumentStrict(1);
    });
}

@Test
void testGetArgumentStrictSuccess() {
    Context context = new Context();
    context.setArguments("a", "b", "c");

    assertEquals("a", context.getArgumentStrict(0));
    assertEquals("b", context.getArgumentStrict(1));
    assertEquals("c", context.getArgumentStrict(2));
}

@Test
void testGetArgumentVariableStrictOutOfBounds() {
    Context context = new Context();
    context.setArguments("a");

    assertThrows(TemplateRuntimeException.class, () -> {
        context.getArgumentVariableStrict(5);
    });
}

@Test
void testGetArgumentVariableStrictNull() {
    Context context = new Context();
    context.setArguments("a", null);

    assertThrows(TemplateRuntimeException.class, () -> {
        context.getArgumentVariableStrict(1);
    });
}
```

**Step 2: Run test to verify it fails**

Run: `mvn test -Dtest=ContextTest#testGetArgumentStrictOutOfBounds`
Expected: FAIL - method not found

**Step 3: Implement Context methods**

Add to `Context.java`:

```java
/**
 * 通过索引获取参数（严格模式，越界报错）
 * <p>
 * 用于 {0} 语法，当索引超出范围时抛出异常
 * </p>
 *
 * @param index 参数索引
 * @return 参数值
 * @throws TemplateRuntimeException 当索引超出范围时
 */
public Object getArgumentStrict(int index) {
    Object[] args = getArguments();
    if (args == null || index < 0 || index >= args.length) {
        throw new TemplateRuntimeException(
            "参数索引 " + index + " 超出范围" +
            (args == null ? "" : "，arguments 数组长度为 " + args.length));
    }
    return args[index];
}

/**
 * 通过索引获取参数值（严格模式，越界或 null 报错）
 * <p>
 * 用于 {$0} 语法，当索引超出范围或值为 null 时抛出异常
 * </p>
 *
 * @param index 参数索引
 * @return 参数值（不为 null）
 * @throws TemplateRuntimeException 当索引超出范围或值为 null 时
 */
public Object getArgumentVariableStrict(int index) {
    Object[] args = getArguments();
    if (args == null || index < 0 || index >= args.length) {
        throw new TemplateRuntimeException(
            "参数索引 " + index + " 超出范围" +
            (args == null ? "" : "，arguments 数组长度为 " + args.length));
    }
    Object value = args[index];
    if (value == null) {
        throw new TemplateRuntimeException(
            "参数索引 " + index + " 的值为 null");
    }
    return value;
}
```

**Step 4: Run test to verify it passes**

Run: `mvn test -Dtest=ContextTest`
Expected: PASS

**Step 5: Commit**

```bash
git add src/main/java/org/dromara/easytl/Context.java
git add src/test/java/org/dromara/easytl/ContextTest.java
git commit -m "feat: add strict argument access methods with error throwing"
```

---

## Task 6: Update Evaluator for ArgumentAccess Error Handling

**Files:**
- Modify: `src/main/java/org/dromara/easytl/compiler/Evaluator.java`

**Step 1: Write the failing test**

Add to `src/test/java/org/dromara/easytl/EasyTLTest.java`:

```java
import org.dromara.easytl.exception.TemplateRuntimeException;

@Test
void testArgumentAccessOutOfBounds() {
    Template template = TemplateEngine.compile("{0}");
    Context context = new Context();
    context.setArguments("only_one");

    assertThrows(TemplateRuntimeException.class, () -> {
        template.render(context);
    });
}

@Test
void testArgumentAccessNull() {
    Template template = TemplateEngine.compile("{0}");
    Context context = new Context();
    context.setArguments((Object) null);

    assertThrows(TemplateRuntimeException.class, () -> {
        template.render(context);
    });
}

@Test
void testArgumentAccessNoArguments() {
    Template template = TemplateEngine.compile("{0}");
    Context context = new Context();
    // No arguments set

    assertThrows(TemplateRuntimeException.class, () -> {
        template.render(context);
    });
}
```

**Step 2: Run test to verify it fails**

Run: `mvn test -Dtest=EasyTLTest#testArgumentAccessOutOfBounds`
Expected: FAIL - currently returns null instead of throwing

**Step 3: Update Evaluator**

In `Evaluator.java`, modify `visitArgumentAccess`:

```java
@Override
public Object visitArgumentAccess(ArgumentAccess node) {
    int index = node.getIndex();
    return context.getArgumentStrict(index);
}
```

**Step 4: Run test to verify it passes**

Run: `mvn test -Dtest=EasyTLTest#testArgumentAccessOutOfBounds`
Expected: PASS

**Step 5: Commit**

```bash
git add src/main/java/org/dromara/easytl/compiler/Evaluator.java
git add src/test/java/org/dromara/easytl/EasyTLTest.java
git commit -m "feat: ArgumentAccess throws error on out-of-bounds or null"
```

---

## Task 7: Update Evaluator for ArgumentVariable

**Files:**
- Modify: `src/main/java/org/dromara/easytl/compiler/Evaluator.java`

**Step 1: Write the failing test**

Add to `src/test/java/org/dromara/easytl/ArgumentVariableTest.java`:

```java
import org.dromara.easytl.exception.TemplateRuntimeException;

@Test
void testArgumentVariableRender() {
    Template template = TemplateEngine.compile("{$0}");
    Context context = new Context();
    context.setArguments("hello");
    assertEquals("hello", template.render(context));
}

@Test
void testArgumentVariableOutOfBounds() {
    Template template = TemplateEngine.compile("{$5}");
    Context context = new Context();
    context.setArguments("only_one");

    assertThrows(TemplateRuntimeException.class, () -> {
        template.render(context);
    });
}

@Test
void testArgumentVariableNull() {
    Template template = TemplateEngine.compile("{$0}");
    Context context = new Context();
    context.setArguments((Object) null);

    assertThrows(TemplateRuntimeException.class, () -> {
        template.render(context);
    });
}

@Test
void testArgumentVariableWithElvis() {
    Template template = TemplateEngine.compile("{$0 ?? 'default'}");
    Context context = new Context();
    context.setArguments((Object) null);
    assertEquals("default", template.render(context));
}

@Test
void testArgumentVariableWithElvisNotTriggered() {
    Template template = TemplateEngine.compile("{$0 ?? 'default'}");
    Context context = new Context();
    context.setArguments("actual");
    assertEquals("actual", template.render(context));
}

@Test
void testArgumentVariableOutOfBoundsWithElvis() {
    // Elvis should NOT catch out-of-bounds error
    Template template = TemplateEngine.compile("{$5 ?? 'default'}");
    Context context = new Context();
    context.setArguments("only_one");

    assertThrows(TemplateRuntimeException.class, () -> {
        template.render(context);
    });
}
```

**Step 2: Run test to verify it fails**

Run: `mvn test -Dtest=ArgumentVariableTest#testArgumentVariableRender`
Expected: FAIL - visitArgumentVariable not implemented

**Step 3: Add visitArgumentVariable to Evaluator**

Add to `Evaluator.java`:

```java
@Override
public Object visitArgumentVariable(ArgumentVariable node) {
    int index = node.getIndex();
    return context.getArgumentVariableStrict(index);
}
```

**Step 4: Update ElvisExpression evaluation**

**Important:** The `??` operator needs special handling. When the left side is `ArgumentVariable`, we need to:
1. Try to evaluate it
2. If it throws "value is null" error, return right side
3. If it throws "out of bounds" error, still throw

Modify `visitElvisExpression` in `Evaluator.java`:

```java
@Override
public Object visitElvisExpression(ElvisExpression node) {
    // Special handling for ArgumentVariable with null coalescing
    if (node.getLeft() instanceof ArgumentVariable) {
        try {
            Object left = node.getLeft().accept(this);
            if (left != null) {
                return left;
            }
            return node.getRight().accept(this);
        } catch (TemplateRuntimeException e) {
            // Only catch null value error, not out-of-bounds
            if (e.getMessage() != null && e.getMessage().contains("的值为 null")) {
                return node.getRight().accept(this);
            }
            throw e;
        }
    }

    // Standard Elvis behavior
    Object left = node.getLeft().accept(this);
    if (left != null) {
        return left;
    }
    return node.getRight().accept(this);
}
```

**Step 5: Run test to verify it passes**

Run: `mvn test -Dtest=ArgumentVariableTest`
Expected: PASS

**Step 6: Commit**

```bash
git add src/main/java/org/dromara/easytl/compiler/Evaluator.java
git add src/test/java/org/dromara/easytl/ArgumentVariableTest.java
git commit -m "feat: implement ArgumentVariable evaluation with ?? support"
```

---

## Task 8: Update Existing Tests

**Files:**
- Modify: `src/test/java/org/dromara/easytl/ArgumentIntegrationTest.java`

**Step 1: Update tests that expect null return**

Some existing tests may expect `context.getArgument()` to return null for out-of-bounds. Update these tests to use the new strict methods or adjust expectations.

**Step 2: Run all tests**

Run: `mvn test`
Expected: All tests pass

**Step 3: Commit**

```bash
git add src/test/java/org/dromara/easytl/ArgumentIntegrationTest.java
git commit -m "test: update existing tests for strict argument access"
```

---

## Task 9: Integration Tests

**Files:**
- Create: `src/test/java/org/dromara/easytl/ArgumentVariableIntegrationTest.java`

**Step 1: Write integration tests**

```java
package org.dromara.easytl;

import org.dromara.easytl.exception.TemplateRuntimeException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ArgumentVariableIntegrationTest {

    @Test
    void testAllExpressionSyntaxes() {
        Context context = new Context();
        context.setArguments("hello", "world");

        // Single brace
        assertEquals("hello", TemplateEngine.compile("{$0}").render(context));
        // Double brace
        assertEquals("hello", TemplateEngine.compile({{$0}}").render(context));
        // Dollar brace
        assertEquals("hello", TemplateEngine.compile("${$0}").render(context));
    }

    @Test
    void testComplexExpression() {
        Context context = new Context();
        context.setArguments("test");

        // With property access
        // Note: depends on whether your template supports this
        // assertEquals("test", TemplateEngine.compile("{$0.name}").render(context));
    }

    @Test
    void testMixedArgumentAccess() {
        Context context = new Context();
        context.setArguments("a", "b", "c");

        // {0} still works
        assertEquals("a", TemplateEngine.compile("{0}").render(context));
        // {$1} also works
        assertEquals("b", TemplateEngine.compile("{$1}").render(context));
        // Mixed
        assertEquals("a-b", TemplateEngine.compile("{0}-{$1}").render(context));
    }

    @Test
    void testErrorMessageChinese() {
        Context context = new Context();
        context.setArguments("only_one");

        try {
            TemplateEngine.compile("{$5}").render(context);
            fail("Should throw exception");
        } catch (TemplateRuntimeException e) {
            assertTrue(e.getMessage().contains("超出范围"));
        }

        context.setArguments((Object) null);
        try {
            TemplateEngine.compile("{$0}").render(context);
            fail("Should throw exception");
        } catch (TemplateRuntimeException e) {
            assertTrue(e.getMessage().contains("null"));
        }
    }
}
```

**Step 2: Run tests**

Run: `mvn test -Dtest=ArgumentVariableIntegrationTest`
Expected: PASS

**Step 3: Commit**

```bash
git add src/test/java/org/dromara/easytl/ArgumentVariableIntegrationTest.java
git commit -m "test: add integration tests for argument variable syntax"
```

---

## Task 10: Final Verification and Documentation

**Step 1: Run all tests**

Run: `mvn test`
Expected: All tests pass

**Step 2: Update BNF_GRAMMAR.md if exists**

Add documentation for new syntax:

```markdown
### Argument Reference

| Syntax | Description | Error Behavior |
|--------|-------------|----------------|
| `{n}` | Simple argument interpolation (n is integer) | Error on out-of-bounds or null |
| `{$n}` | Variable-style argument reference | Error on out-of-bounds or null, supports `??` |

Examples:
- `{0}` - Access first argument, throws error if missing
- `{$0}` - Access first argument as variable
- `{$0 ?? 'default'}` - Access first argument, use 'default' if null
```

**Step 3: Final commit**

```bash
git add .
git commit -m "docs: update grammar documentation for argument variable syntax"
```

---

## Summary

This plan implements:

1. **Strict error handling** for existing `{0}` syntax
2. **New `$0` syntax** that works as variable-style argument reference
3. **`??` operator support** for `$0` to handle null values gracefully
4. **Clear error messages** in Chinese for both out-of-bounds and null cases
5. **New AST node** `ArgumentVariable` to distinguish behaviors

Total estimated tasks: 10
Recommended execution: Subagent-Driven for fast iteration with reviews