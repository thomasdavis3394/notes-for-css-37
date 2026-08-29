# Null Pointer Property Access Error Position Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Fix caret position for null pointer property access errors to point only to the property name, not the entire expression.

**Architecture:** Add `memberNamePosition` field to MemberAccess AST node, set it during parsing, and use it in Evaluator when throwing null pointer errors.

**Tech Stack:** Java 8, Maven, JUnit 5

---

## File Structure

**Modify:**
- `src/main/java/org/dromara/easytl/ast/MemberAccess.java` - Add memberNamePosition field, constructor, getter
- `src/main/java/org/dromara/easytl/parser/Parser.java:694-705` - Pass member name position when creating MemberAccess
- `src/main/java/org/dromara/easytl/compiler/Evaluator.java:193-212` - Use memberNamePosition for null pointer errors

**Test:**
- `src/test/java/org/dromara/easytl/exception/RuntimeExceptionFormattingTest.java` - Add tests to verify caret position

---

### Task 1: Add test for null pointer property access error caret position

**Files:**
- Modify: `src/test/java/org/dromara/easytl/exception/RuntimeExceptionFormattingTest.java`

- [ ] **Step 1: Write the failing test**

Add test method to verify caret points to property name only:

```java
/**
 * Test: Null pointer property access error should point to property name only
 * Template: "Hello {user.name}!"
 * Expected caret position: columns 13-17 (pointing to 'name')
 */
@Test
void testNullPointerPropertyAccess_PointsToPropertyNameOnly() {
    String template = "Hello {user.name}!";
    Template compiled = engine.compile(template);

    TemplateRuntimeException ex = assertThrows(
            TemplateRuntimeException.class,
            () -> compiled.render(new Context())
    );

    String message = ex.getMessage();

    // Verify caret is present
    assertTrue(message.contains("^"),
            "Error message should have caret: " + message);

    // Verify position points to 'name' (columns 13-17)
    assertEquals(13, ex.getStartColumn(),
            "Start column should be 13 (start of 'name'): " + message);
    assertEquals(17, ex.getEndColumn(),
            "End column should be 17 (end of 'name', exclusive): " + message);

    // Verify error line is correct
    assertEquals(1, ex.getStartLine());
    assertEquals(1, ex.getEndLine());
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn test -Dtest=RuntimeExceptionFormattingTest#testNullPointerPropertyAccess_PointsToPropertyNameOnly -q`
Expected: FAIL - current implementation returns startColumn=8 (start of entire expression)

- [ ] **Step 3: Commit test file**

```bash
git add src/test/java/org/dromara/easytl/exception/RuntimeExceptionFormattingTest.java
git commit -m "test: add test for null pointer property access caret position"
```

---

### Task 2: Add memberNamePosition field to MemberAccess

**Files:**
- Modify: `src/main/java/org/dromara/easytl/ast/MemberAccess.java:14-106`

- [ ] **Step 1: Add memberNamePosition field**

Add after line 21 (after `safeNavigation` field):

```java
    /** 属性名位置（仅属性名部分，不包含对象和点号） */
    private final Position memberNamePosition;
```

- [ ] **Step 2: Add new constructor with memberNamePosition**

Add after line 64 (after existing constructors):

```java
    /**
     * 构造成员访问节点（包含属性名位置）
     *
     * @param object 对象表达式
     * @param memberName 成员名称
     * @param safeNavigation 是否为空安全访问
     * @param position 整个表达式的位置
     * @param memberNamePosition 属性名的位置
     */
    public MemberAccess(ASTNode object, String memberName, boolean safeNavigation,
                        Position position, Position memberNamePosition) {
        super(position);
        this.object = object;
        this.memberName = memberName;
        this.safeNavigation = safeNavigation;
        this.memberNamePosition = memberNamePosition;
    }
```

- [ ] **Step 3: Add getter method for memberNamePosition**

Add after line 91 (after `isSafeNavigation()` method):

```java
    /**
     * 获取属性名的位置
     *
     * @return 属性名位置，可能为 null 或 unknown
     */
    public Position getMemberNamePosition() {
        return memberNamePosition;
    }
```

- [ ] **Step 4: Update toString method to include memberNamePosition**

Replace lines 99-105 with:

```java
    @Override
    public String toString() {
        return "MemberAccess{" +
                "object=" + object +
                ", memberName='" + memberName + '\'' +
                ", safeNavigation=" + safeNavigation +
                ", memberNamePosition=" + memberNamePosition +
                '}';
    }
```

- [ ] **Step 5: Compile to verify changes**

Run: `mvn compile -q`
Expected: Success

- [ ] **Step 6: Commit MemberAccess changes**

```bash
git add src/main/java/org/dromara/easytl/ast/MemberAccess.java
git commit -m "feat: add memberNamePosition field to MemberAccess AST node"
```

---

### Task 3: Update Parser to pass member name position

**Files:**
- Modify: `src/main/java/org/dromara/easytl/parser/Parser.java:694-705`

- [ ] **Step 1: Update DOT branch to use Token's endColumn and pass memberNamePosition**

Replace lines 694-699:

```java
            if (check(TokenType.DOT)) {
                advance();
                Token name = expect(TokenType.IDENTIFIER);
                // 整个表达式的位置（使用 Token 的 endColumn，它是 exclusive）
                Position fullPos = new Position(expr.getStartLine(), expr.getStartColumn(),
                                           name.getLine(), name.getEndColumn());
                // 属性名的位置（仅 name 部分）
                Position memberNamePos = new Position(name.getLine(), name.getStartColumn(),
                                           name.getLine(), name.getEndColumn());
                expr = new MemberAccess(expr, name.getValue(), false, fullPos, memberNamePos);
```

- [ ] **Step 2: Update SAFE_NAV branch similarly**

Replace lines 700-705:

```java
            } else if (check(TokenType.SAFE_NAV)) {
                advance();
                Token name = expect(TokenType.IDENTIFIER);
                // 整个表达式的位置
                Position fullPos = new Position(expr.getStartLine(), expr.getStartColumn(),
                                           name.getLine(), name.getEndColumn());
                // 属性名的位置
                Position memberNamePos = new Position(name.getLine(), name.getStartColumn(),
                                           name.getLine(), name.getEndColumn());
                expr = new MemberAccess(expr, name.getValue(), true, fullPos, memberNamePos);
```

- [ ] **Step 3: Compile to verify changes**

Run: `mvn compile -q`
Expected: Success

- [ ] **Step 4: Commit Parser changes**

```bash
git add src/main/java/org/dromara/easytl/parser/Parser.java
git commit -m "feat: pass member name position when creating MemberAccess in Parser"
```

---

### Task 4: Update Evaluator to use memberNamePosition for null pointer errors

**Files:**
- Modify: `src/main/java/org/dromara/easytl/compiler/Evaluator.java:193-212`

- [ ] **Step 1: Update visitMemberAccess to use memberNamePosition**

Replace lines 201-203:

```java
        if (obj == null) {
            // 使用属性名位置而非整个表达式位置
            Position errorPos = node.getMemberNamePosition();
            if (errorPos != null && !errorPos.isUnknown()) {
                throw new TemplateRuntimeException(
                    "Cannot access member '" + node.getMemberName() + "' of null",
                    errorPos, new StringSource(sourceText));
            }
            throw runtimeError("Cannot access member '" + node.getMemberName() + "' of null", node);
        }
```

- [ ] **Step 2: Compile to verify changes**

Run: `mvn compile -q`
Expected: Success

- [ ] **Step 3: Commit Evaluator changes**

```bash
git add src/main/java/org/dromara/easytl/compiler/Evaluator.java
git commit -m "feat: use memberNamePosition for null pointer property access errors"
```

---

### Task 5: Run all tests and verify fix

- [ ] **Step 1: Run the new test**

Run: `mvn test -Dtest=RuntimeExceptionFormattingTest#testNullPointerPropertyAccess_PointsToPropertyNameOnly -q`
Expected: PASS

- [ ] **Step 2: Run all runtime exception formatting tests**

Run: `mvn test -Dtest=RuntimeExceptionFormattingTest -q`
Expected: All tests PASS

- [ ] **Step 3: Run full test suite**

Run: `mvn test -q`
Expected: All tests PASS

- [ ] **Step 4: Run manual verification**

Create and run test program:

```java
package org.dromara.easytl;

import org.dromara.easytl.exception.TemplateRuntimeException;

public class VerifyErrorPosition {
    public static void main(String[] args) {
        TemplateEngine engine = new TemplateEngine();
        String template = "Hello {user.name}!";
        Template compiled = engine.compile(template);
        try {
            compiled.render(new Context());
        } catch (TemplateRuntimeException e) {
            System.out.println(e.getMessage());
        }
    }
}
```

Run: `mvn compile test-compile -q && java -cp target/classes:target/test-classes org.dromara.easytl.VerifyErrorPosition`

Expected output:
```
Runtime error: Cannot access member 'name' of null

  |
1 | Hello {user.name}!
  |             ^^^^ Cannot access member 'name' of null
```

- [ ] **Step 5: Clean up test helper files**

```bash
rm -f src/test/java/org/dromara/easytl/TestErrorOutput.java
rm -f src/test/java/org/dromara/easytl/TestTokenPosition.java
rm -f src/test/java/org/dromara/easytl/TestPositionOutput.java
rm -f src/test/java/org/dromara/easytl/VerifyErrorPosition.java
```

---

### Task 6: Add additional test for chained member access

**Files:**
- Modify: `src/test/java/org/dromara/easytl/exception/RuntimeExceptionFormattingTest.java`

- [ ] **Step 1: Write test for chained member access**

Add test method:

```java
/**
 * Test: Chained member access null pointer error
 * When user.address is null, accessing .city should point to 'city'
 */
@Test
void testChainedMemberAccessNullPointer_PointsToLastProperty() {
    String template = "{user.address.city}";
    Template compiled = engine.compile(template);

    Context context = new Context();
    context.put("user", new TestUser()); // user exists, profile exists, address is null

    TemplateRuntimeException ex = assertThrows(
            TemplateRuntimeException.class,
            () -> compiled.render(context)
    );

    // Verify caret points to 'city' property
    assertTrue(ex.getMessage().contains("^"),
            "Error message should have caret: " + ex.getMessage());

    // 'city' starts at column 15 (after user.address.)
    assertEquals(15, ex.getStartColumn(),
            "Start column should be at 'city': " + ex.getMessage());
}
```

- [ ] **Step 2: Run test to verify it passes**

Run: `mvn test -Dtest=RuntimeExceptionFormattingTest#testChainedMemberAccessNullPointer_PointsToLastProperty -q`
Expected: PASS

- [ ] **Step 3: Commit additional test**

```bash
git add src/test/java/org/dromara/easytl/exception/RuntimeExceptionFormattingTest.java
git commit -m "test: add test for chained member access null pointer error position"
```

---

### Task 7: Final verification and cleanup

- [ ] **Step 1: Run full test suite**

Run: `mvn test -q`
Expected: All tests PASS

- [ ] **Step 2: Review git status**

Run: `git status`
Expected: Clean working tree (all changes committed)

- [ ] **Step 3: Review commit history**

Run: `git log --oneline -5`
Expected: See commits for this feature

---

## Self-Review Checklist

- [x] Spec coverage: All requirements from design spec covered
  - MemberAccess memberNamePosition field: Task 2
  - Parser passes member name position: Task 3
  - Evaluator uses memberNamePosition: Task 4
  - Tests verify caret position: Tasks 1, 6
- [x] Placeholder scan: No TBD, TODO, or placeholder code
- [x] Type consistency: Position class used consistently across all tasks