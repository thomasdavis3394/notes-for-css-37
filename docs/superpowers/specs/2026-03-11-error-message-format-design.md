# Error Message Format Modification Design

**Date:** 2026-03-11
**Status:** Approved
**Topic:** 错误消息格式修改

## Overview

Modify the error message format for TemplateException and its subclasses:
1. Display error reason first
2. Add an empty line
3. Display source code position information
4. Keep error reason on the right side of the `^^^` marker
5. Ensure `^^^` markers exactly match the error position in source code

## Target Format

Before:
```
  |
1 | Hello {user.name}!
  |        ^^^^^^^^^^^^ Runtime error: Cannot access property 'name' of null
```

After:
```
Runtime error: Cannot access property 'name' of null

  |
1 | Hello {user.name}!
  |             ^^^^ Runtime error: Cannot access property 'name' of null
```

## Design

### Section 1: ErrorMessageFormatter.formatRustStyleMessage

Modify the `formatRustStyleMessage` method in `ErrorMessageFormatter.java`:

1. **Add error reason at the beginning**:
   ```java
   sb.append(errorType).append(": ").append(message).append("\n\n");
   ```

2. **Fix error marker length calculation**:
   ```java
   // Before (incorrect)
   errorLength = endColumn - startColumn + 1;

   // After (correct)
   errorLength = endColumn - startColumn;
   ```

### Section 2: Test Updates

Update test cases in `TemplateExceptionTest.java` to verify the new format.

## Implementation Steps

1. Modify `ErrorMessageFormatter.formatRustStyleMessage` method
2. Update test cases in `TemplateExceptionTest`
3. Run `mvn test` to verify all tests pass
