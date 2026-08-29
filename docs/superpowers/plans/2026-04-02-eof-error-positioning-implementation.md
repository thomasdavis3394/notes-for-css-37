# EOF Error Positioning Fix Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Fix EOF error positioning so that the `^` marker is displayed at the position after the last character when a template ends unexpectedly.

**Architecture:** Single-line fix in `ErrorMessageFormatter.formatRustStyleMessage()` to ensure at least one `^` marker is drawn for zero-width positions.

**Tech Stack:** Java 8, JUnit 5, Maven

---

## File Structure

| File | Action | Purpose |
|------|--------|---------|
| `src/test/java/org/dromara/easytl/exception/ErrorMessageFormatterTest.java` | Create | New test class for EOF error positioning |
| `src/main/java/org/dromara/easytl/exception/ErrorMessageFormatter.java` | Modify | Fix the error marker length calculation |

---

### Task 1: Write Failing Test for EOF Error Positioning

**Files:**
- Create: `src/test/java/org/dromara/easytl/exception/ErrorMessageFormatterTest.java`

- [ ] **Step 1: Write the failing test**

```java
package org.dromara.easytl.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ErrorMessageFormatter, specifically EOF error positioning.
 */
class ErrorMessageFormatterTest {

    /**
     * Test that EOF errors show a ^ marker at the position after the last character.
     * 
     * When a template ends unexpectedly (e.g., "{% for item in list %}content"),
     * the error marker should point to the end-of-file position, not be missing.
     */
    @Test
    void testEofErrorShowsCaretMarker() {
        // Source: "{% for item in list %}content" (30 characters)
        // EOF position: after the last character 't', at column 31
        String sourceText = "{% for item in list %}content";
        int eofLine = 1;
        int eofColumn = 31; // Position after last character (1-based)
        
        String message = ErrorMessageFormatter.formatMessage(
            ErrorMessageFormatter.SYNTAX_ERROR,
            "Expected {/%, but got <end of file>",
            eofLine, eofColumn, eofLine, eofColumn,
            sourceText
        );
        
        // The message should contain the source line
        assertTrue(message.contains(sourceText), 
            "Error message should contain source text");
        
        // The message should contain a ^ marker
        assertTrue(message.contains("^"), 
            "Error message should contain ^ marker for EOF position");
        
        // Verify the ^ appears at the correct position (after last character)
        // The error line format is: "1 | <source>\n  | <spaces>^"
        // For column 31, we expect 30 spaces before the ^
        String[] lines = message.split("\n");
        String caretLine = null;
        for (String line : lines) {
            if (line.contains(" | ") && line.contains("^")) {
                caretLine = line;
                break;
            }
        }
        
        assertNotNull(caretLine, "Should find a line containing the ^ marker");
        
        // The caret should be at position after the last character
        // Line format: "  |                                ^ Expected..."
        // The ^ should be at column 31 (after 30 characters of padding)
        int caretIndex = caretLine.indexOf('^');
        // After "  | " prefix (4 chars), there should be padding of (eofColumn - 1) spaces
        // So caretIndex should be 4 + (eofColumn - 1) = 4 + 30 = 34
        assertEquals(34, caretIndex, 
            "Caret should be at position after the last character (column 31)");
    }

    /**
     * Test that zero-width positions show at least one ^ marker.
     * 
     * This is a more general test for any zero-width error position,
     * not just EOF scenarios.
     */
    @Test
    void testZeroWidthErrorShowsCaretMarker() {
        String sourceText = "hello";
        // Zero-width position at column 3 (startColumn == endColumn)
        int line = 1;
        int column = 3;
        
        String message = ErrorMessageFormatter.formatMessage(
            ErrorMessageFormatter.SYNTAX_ERROR,
            "Test error",
            line, column, line, column,
            sourceText
        );
        
        // Should show at least one ^
        assertTrue(message.contains("^"), 
            "Zero-width error should show at least one ^ marker");
    }

    /**
     * Test that normal (non-zero-width) errors still work correctly.
     */
    @Test
    void testNormalErrorShowsCorrectCaretLength() {
        String sourceText = "hello world";
        // Error spanning from column 7 to column 11 (inclusive) -> length 5
        int line = 1;
        int startColumn = 7;
        int endColumn = 12; // Points to position after 'world' (endColumn is exclusive)
        
        String message = ErrorMessageFormatter.formatMessage(
            ErrorMessageFormatter.SYNTAX_ERROR,
            "Test error",
            line, startColumn, line, endColumn,
            sourceText
        );
        
        // Should contain 5 ^ characters (endColumn - startColumn = 5)
        String[] lines = message.split("\n");
        String caretLine = null;
        for (String lineStr : lines) {
            if (lineStr.contains(" | ") && lineStr.contains("^")) {
                caretLine = lineStr;
                break;
            }
        }
        
        assertNotNull(caretLine, "Should find a line containing the ^ marker");
        
        // Count consecutive ^ characters
        int caretCount = 0;
        boolean inCaretRun = false;
        for (char c : caretLine.toCharArray()) {
            if (c == '^') {
                if (!inCaretRun) {
                    inCaretRun = true;
                }
                caretCount++;
            } else if (inCaretRun) {
                break;
            }
        }
        
        assertEquals(5, caretCount, 
            "Should have 5 ^ characters for error spanning columns 7-11");
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn test -Dtest=ErrorMessageFormatterTest -q`

Expected: FAIL - test expects `^` marker but it's not present for zero-width positions

Output should show:
```
ErrorMessageFormatterTest > testEofErrorShowsCaretMarker FAILED
    java.lang.AssertionError: Error message should contain ^ marker for EOF position
```

---

### Task 2: Implement the Fix

**Files:**
- Modify: `src/main/java/org/dromara/easytl/exception/ErrorMessageFormatter.java:135-142`

- [ ] **Step 1: Implement the minimal fix**

In `ErrorMessageFormatter.java`, locate the `formatRustStyleMessage` method and modify lines ~135-142:

**Before (line ~136):**
```java
// Calculate error marker length
// Note: endColumn in Token points to the position AFTER the last character,
// so we use endColumn - startColumn (not +1)
int errorLength;
if (startLine == endLine && endColumn >= startColumn) {
    // Single line error
    errorLength = endColumn - startColumn;
} else {
    // Multi-line error, mark from start to end of line
    errorLength = Math.max(1, errorLine.length() - startColumn + 1);
}
```

**After:**
```java
// Calculate error marker length
// Note: endColumn in Token points to the position AFTER the last character,
// so we use endColumn - startColumn (not +1)
// Use Math.max(1, ...) to ensure at least one ^ for zero-width positions (e.g., EOF)
int errorLength;
if (startLine == endLine && endColumn >= startColumn) {
    // Single line error
    errorLength = Math.max(1, endColumn - startColumn);
} else {
    // Multi-line error, mark from start to end of line
    errorLength = Math.max(1, errorLine.length() - startColumn + 1);
}
```

The key change is line 141:
- Before: `errorLength = endColumn - startColumn;`
- After: `errorLength = Math.max(1, endColumn - startColumn);`

- [ ] **Step 2: Run tests to verify they pass**

Run: `mvn test -Dtest=ErrorMessageFormatterTest -q`

Expected: PASS - all 3 tests pass

- [ ] **Step 3: Run existing tests to verify no regression**

Run: `mvn test -Dtest=SyntaxErrorTest -q`

Expected: PASS - all existing syntax error tests still pass

- [ ] **Step 4: Run full test suite**

Run: `mvn test -q`

Expected: PASS - all tests pass

- [ ] **Step 5: Commit**

```bash
git add src/test/java/org/dromara/easytl/exception/ErrorMessageFormatterTest.java src/main/java/org/dromara/easytl/exception/ErrorMessageFormatter.java
git commit -m "fix: ensure ^ marker is shown for EOF error positioning

When a template ends unexpectedly, the error marker ^ was not displayed
because the EOF token has zero width (startColumn == endColumn), resulting
in errorLength = 0 and no caret characters being drawn.

This fix ensures at least one ^ marker is drawn for zero-width positions
by using Math.max(1, endColumn - startColumn) in the single-line error case.

Fixes: EOF error positioning issue
Test: Added ErrorMessageFormatterTest with 3 test cases

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

## Self-Review Checklist

- [x] **Spec coverage:** All requirements from spec have corresponding tasks
- [x] **Placeholder scan:** No TBD/TODO/placeholders - all code is complete
- [x] **Type consistency:** Method signatures and types match across tasks

## Verification Commands

After implementation, verify the fix manually:

```bash
# Run a test that triggers an EOF error and check the output
mvn test -Dtest=SyntaxErrorTest#testForStatementMissingEndTag -q
```

Expected output should now show:
```
  |
1 | {% for item in list %}content
  |                                ^ Expected {/%, but got <end of file>
```

(With the `^` marker visible after the last character)