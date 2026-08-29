# EOF Error Positioning Fix Design

**Date:** 2026-04-02
**Status:** Draft
**Author:** Claude

## Problem Statement

When a template ends unexpectedly (EOF error), the error marker `^` is not displayed in the error message. This occurs because the EOF token has zero width (`startColumn == endColumn`), resulting in `errorLength = 0` and no caret characters being drawn.

### Current Behavior

For input `{% for item in list %}content`, the error shows:

```
  |
1 | {% for item in list %}content
  |                               Expected {/%, but got <end of file>
```

The `^` marker is missing.

### Expected Behavior

The `^` marker should point to the position after the last character:

```
  |
1 | {% for item in list %}content
  |                                ^ Expected {/%, but got <end of file>
```

## Root Cause Analysis

The issue is in `ErrorMessageFormatter.formatRustStyleMessage()`:

```java
int errorLength;
if (startLine == endLine && endColumn >= startColumn) {
    // Single line error
    errorLength = endColumn - startColumn;  // <-- Can be 0 for EOF
} else {
    // Multi-line error, mark from start to end of line
    errorLength = Math.max(1, errorLine.length() - startColumn + 1);
}

// Draw error marker
for (int i = 0; i < errorLength; i++) {  // <-- Loop doesn't run if errorLength is 0
    sb.append('^');
}
```

For EOF tokens:
- `startColumn = endColumn` (both at position after last character)
- `errorLength = endColumn - startColumn = 0`
- No `^` characters are drawn

## Solution

Modify `formatRustStyleMessage()` to ensure at least one `^` marker is drawn for zero-width positions.

### Code Change

**File:** `src/main/java/org/dromara/easytl/exception/ErrorMessageFormatter.java`

**Location:** Line ~136 in `formatRustStyleMessage()` method

**Before:**
```java
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
int errorLength;
if (startLine == endLine && endColumn >= startColumn) {
    // Single line error
    // Use Math.max(1, ...) to ensure at least one ^ for zero-width positions (e.g., EOF)
    errorLength = Math.max(1, endColumn - startColumn);
} else {
    // Multi-line error, mark from start to end of line
    errorLength = Math.max(1, errorLine.length() - startColumn + 1);
}
```

## Impact Analysis

### Affected Code

- `ErrorMessageFormatter.formatRustStyleMessage()` - Single location change

### Test Coverage

- Existing tests in `SyntaxErrorTest.java` should continue to pass
- May want to add a specific test for EOF error positioning

## Test Cases

| Test Case | Input | Expected Output |
|-----------|-------|-----------------|
| EOF at statement end | `{% for item in list %}content` | `^` at column 31 |
| EOF at expression end | `{x` | `^` at column 2 |
| EOF in block statement | `{% if true %}content` | `^` at end |
| Normal error (non-EOF) | `{x + }` | Existing behavior unchanged |

## Migration Notes

No migration needed - this is a display-only fix that improves error messages.

## Conclusion

A minimal one-line change that ensures the `^` marker is always visible for error positions, including EOF scenarios.