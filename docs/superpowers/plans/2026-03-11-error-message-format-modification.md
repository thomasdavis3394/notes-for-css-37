# Error Message Format Modification Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Modify error message format to display error reason first, then empty line, then source code position, with precise `^^^` marker alignment

**Architecture:** Modify ErrorMessageFormatter.formatRustStyleMessage to:
1. Add error reason at the beginning of the message
2. Fix error marker length calculation to match exact column positions

**Tech Stack:** Java 8, JUnit 5

---

## Implementation Steps

### Task 1: Modify ErrorMessageFormatter.formatRustStyleMessage

**Files:**
- Modify: `src/main/java/org/dromara/easytl/exception/ErrorMessageFormatter.java:103-152`

- [ ] **Step 1: Add error reason at the beginning**

In the `formatRustStyleMessage` method, after `StringBuilder sb = new StringBuilder();` (around line 107), add:

```java
// Add error reason at the beginning
sb.append(errorType).append(": ").append(message).append("\n\n");
```

- [ ] **Step 2: Fix error marker length calculation**

Find the errorLength calculation (around lines 130-137):

```java
// Current code (incorrect)
int errorLength;
if (startLine == endLine && endColumn >= startColumn) {
    // Single line error
    errorLength = endColumn - startColumn + 1;
} else {
    // Multi-line error, mark from start to end of line
    errorLength = Math.max(1, errorLine.length() - startColumn + 1);
}
```

Replace with:

```java
// Fixed: exact column-to-column marker length (inclusive)
int errorLength;
if (startLine == endLine && endColumn >= startColumn) {
    // Single line error: endColumn - startColumn + 1 gives exact character count
    errorLength = endColumn - startColumn + 1;
} else {
    // Multi-line error, mark from start to end of line
    errorLength = Math.max(1, errorLine.length() - startColumn + 1);
}
```

Note: For single-line errors, we use `endColumn - startColumn + 1` (inclusive) because column positions are 1-based and the marker should span exactly from startColumn to endColumn (both inclusive).

- [ ] **Step 3: Run tests to verify changes**

Run: `mvn test -Dtest=TemplateExceptionTest`
Expected: Existing tests should fail due to changed format

- [ ] **Step 4: Commit**

```bash
git add src/main/java/org/dromara/easytl/exception/ErrorMessageFormatter.java
git commit -m "fix: modify error message format - show error reason first, then position

- Add error reason at beginning of Rust-style error message
- Fix ^^^ marker length calculation for exact column alignment

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

### Task 2: Update Test Cases

**Files:**
- Modify: `src/test/java/org/dromara/easytl/exception/TemplateExceptionTest.java`

- [ ] **Step 1: Update testTemplateRuntimeExceptionRustStyle test**

Find the test method and update the assertions to match new format:

```java
@Test
void testTemplateRuntimeExceptionRustStyle() {
    // Test Rust-style error message formatting
    final String sourceText = "line1\nline2\nHello {user.name}\nline4";
    final TemplateRuntimeException ex = new TemplateRuntimeException(
            "Cannot access property 'name' of null", 3, 8, 3, 18, sourceText);

    final String message = ex.getMessage();
    // Should contain error reason at the beginning
    assertTrue(message.startsWith("Runtime error: Cannot access property 'name' of null\n\n"));
    // Should contain the error line
    assertTrue(message.contains("Hello {user.name}"));
    // Should contain the caret pointer with exact length
    assertTrue(message.contains("^^^^^^^^^^"));
    // Should contain error reason at the end too
    assertTrue(message.contains("Runtime error: Cannot access property"));
}
```

- [ ] **Step 2: Run tests to verify**

Run: `mvn test -Dtest=TemplateExceptionTest`
Expected: All tests pass

- [ ] **Step 3: Commit**

```bash
git add src/test/java/org/dromara/easytl/exception/TemplateExceptionTest.java
git commit -m "test: update TemplateExceptionTest for new error message format

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

### Task 3: Verify All Tests Pass

- [ ] **Step 1: Run full test suite**

Run: `mvn test`
Expected: All tests pass

- [ ] **Step 2: Final commit if needed**

If any additional changes are needed, commit them:

```bash
git add -A
git commit -m "fix: complete error message format modification

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```
