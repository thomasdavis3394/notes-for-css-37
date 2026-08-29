# Exception Refactoring Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Refactor exception hierarchy to use Position + Source instead of individual line/column fields and String sourceText.

**Architecture:** Move common fields (position, source) to base TemplateException class. Subclasses delegate to base class constructors. Use lazy extraction for source text via Source interface.

**Tech Stack:** Java 8, JUnit 5, Maven

---

### Task 1: Update ErrorMessageFormatter with new overloaded methods

**Files:**
- Modify: `src/main/java/org/dromara/easytl/exception/ErrorMessageFormatter.java`

**Step 1: Add new overloaded methods**

Add these methods to ErrorMessageFormatter (keep existing methods for backward compatibility):

```java
/**
 * Format an error message with Position and Source objects.
 */
public static String formatMessage(final String errorType, final String message,
                                   final Position position, final Source source) {
    if (position != null && source != null) {
        int startLine = position.getStartLine();
        int startColumn = position.getStartColumn();
        int endLine = position.getEndLine();
        int endColumn = position.getEndColumn();
        String sourceText = source.getSourceText();
        return formatMessage(errorType, message, startLine, startColumn, endLine, endColumn, sourceText);
    } else if (position != null) {
        int startLine = position.getStartLine();
        int startColumn = position.getStartColumn();
        int endLine = position.getEndLine();
        int endColumn = position.getEndColumn();
        return formatMessage(errorType, message, startLine, startColumn, endLine, endColumn, null);
    }
    return message;
}

public static String formatSyntaxError(final String message, final Position position, final Source source) {
    return formatMessage(SYNTAX_ERROR, message, position, source);
}

public static String formatRuntimeError(final String message, final Position position, final Source source) {
    return formatMessage(RUNTIME_ERROR, message, position, source);
}
```

**Step 2: Add imports**

Add these imports at the top of the file:
```java
import org.dromara.easytl.ast.Position;
import org.dromara.easytl.parser.Source;
```

**Step 3: Run tests to verify**

Run: `mvn test -Dtest=TemplateExceptionTest`
Expected: PASS (existing tests still work)

**Step 4: Commit**

```bash
git add src/main/java/org/dromara/easytl/exception/ErrorMessageFormatter.java
git commit -m "feat(exception): add Position+Source overloaded methods to ErrorMessageFormatter"
```

---

### Task 2: Refactor TemplateException base class

**Files:**
- Modify: `src/main/java/org/dromara/easytl/exception/TemplateException.java`

**Step 1: Add imports**

```java
import org.dromara.easytl.ast.Position;
import org.dromara.easytl.parser.Source;
```

**Step 2: Add fields**

```java
private final Position position;
private final Source source;
```

**Step 3: Add protected constructor**

```java
protected TemplateException(final String message, final Position position, final Source source) {
    super(ErrorMessageFormatter.formatMessage(
            "Template error", message, position, source));
    this.position = position;
    this.source = source;
}
```

**Step 4: Add getters**

```java
public Position getPosition() {
    return position;
}

public Source getSource() {
    return source;
}

public int getStartLine() {
    return position != null ? position.getStartLine() : -1;
}

public int getStartColumn() {
    return position != null ? position.getStartColumn() : -1;
}

public int getEndLine() {
    return position != null ? position.getEndLine() : -1;
}

public int getEndColumn() {
    return position != null ? position.getEndColumn() : -1;
}

public String getSourceText() {
    return source != null ? source.getSourceText() : null;
}
```

**Step 5: Run tests**

Run: `mvn test -Dtest=TemplateExceptionTest`
Expected: PASS

**Step 6: Commit**

```bash
git add src/main/java/org/dromara/easytl/exception/TemplateException.java
git commit -m "refactor(exception): add Position and Source fields to TemplateException"
```

---

### Task 3: Simplify TemplateSyntaxException

**Files:**
- Modify: `src/main/java/org/dromara/easytl/exception/TemplateSyntaxException.java`

**Step 1: Add imports**

```java
import org.dromara.easytl.ast.Position;
import org.dromara.easytl.parser.Source;
import org.dromara.easytl.parser.StringSource;
```

**Step 2: Remove fields** (delete these 5 fields):
- startLine
- startColumn
- endLine
- endColumn
- sourceText

**Step 3: Update constructors**

Replace all constructors with:

```java
// Simple constructor
public TemplateSyntaxException(final String message) {
    super(message, null, null);
}

// With Position and Source (new primary constructor)
public TemplateSyntaxException(final String message, final Position position, final Source source) {
    super(ErrorMessageFormatter.formatSyntaxError(message, position, source), position, source);
}

// With Position only
public TemplateSyntaxException(final String message, final Position position) {
    this(message, position, null);
}

// Backward-compatible: with line/column
public TemplateSyntaxException(final String message, final int line, final int column) {
    this(message, new Position(line, column, line, column), null);
}

// Backward-compatible: with line/column and sourceText
public TemplateSyntaxException(final String message, final int line, final int column, final String sourceText) {
    this(message, new Position(line, column, line, column),
         sourceText != null ? new StringSource(sourceText) : null);
}

// Backward-compatible: with full position range
public TemplateSyntaxException(final String message, final int startLine, final int startColumn,
                               final int endLine, final int endColumn, final String sourceText) {
    super(ErrorMessageFormatter.formatSyntaxError(message,
            new Position(startLine, startColumn, endLine, endColumn),
            sourceText != null ? new StringSource(sourceText) : null));
    // Position and source are passed to parent but we don't store them here
}

// Backward-compatible: with cause
public TemplateSyntaxException(final String message, final Throwable cause) {
    super(message, cause);
}
```

**Step 4: Update getters**

Remove the field-based getters and keep deprecated ones that delegate to parent:

```java
@Deprecated
public int getLine() {
    return getStartLine();
}

@Deprecated
public int getColumn() {
    return getStartColumn();
}
```

**Step 5: Run tests**

Run: `mvn test -Dtest=TemplateExceptionTest`
Expected: PASS

**Step 6: Commit**

```bash
git add src/main/java/org/dromara/easytl/exception/TemplateSyntaxException.java
git commit -m "refactor(exception): simplify TemplateSyntaxException using Position+Source"
```

---

### Task 4: Simplify TemplateRuntimeException

**Files:**
- Modify: `src/main/java/org/dromara/easytl/exception/TemplateRuntimeException.java`

**Step 1: Add imports**

```java
import org.dromara.easytl.ast.Position;
import org.dromara.easytl.parser.Source;
import org.dromara.easytl.parser.StringSource;
```

**Step 2: Remove fields** (delete these 5 fields):
- startLine
- startColumn
- endLine
- endColumn
- sourceText

**Step 3: Update constructors**

Replace all constructors with:

```java
// Simple constructor
public TemplateRuntimeException(final String message) {
    super(message, null, null);
}

// With cause
public TemplateRuntimeException(final String message, final Throwable cause) {
    super(message, cause);
}

// With cause (no position)
public TemplateRuntimeException(final Throwable cause) {
    super(cause.toString(), null, null);
}

// With position range only
public TemplateRuntimeException(final String message, final int startLine, final int startColumn,
                                final int endLine, final int endColumn) {
    this(message, new Position(startLine, startColumn, endLine, endColumn), null);
}

// Backward-compatible: with position range and sourceText
public TemplateRuntimeException(final String message, final int startLine, final int startColumn,
                                final int endLine, final int endColumn, final String sourceText) {
    this(message, new Position(startLine, startColumn, endLine, endColumn),
         sourceText != null ? new StringSource(sourceText) : null);
}

// With Position object
public TemplateRuntimeException(final String message, final Position position) {
    this(message, position, null);
}

// With Position and Source (new primary constructor)
public TemplateRuntimeException(final String message, final Position position, final Source source) {
    super(ErrorMessageFormatter.formatRuntimeError(message, position, source), position, source);
}

// With cause + position
public TemplateRuntimeException(final String message, final Throwable cause, final int startLine,
                                final int startColumn, final int endLine, final int endColumn,
                                final String sourceText) {
    super(ErrorMessageFormatter.formatRuntimeError(message,
            new Position(startLine, startColumn, endLine, endColumn),
            sourceText != null ? new StringSource(sourceText) : null),
          new Position(startLine, startColumn, endLine, endColumn),
          sourceText != null ? new StringSource(sourceText) : null);
}
```

**Step 4: Remove getters** (they're now inherited from TemplateException)

**Step 5: Run tests**

Run: `mvn test -Dtest=TemplateExceptionTest`
Expected: PASS

**Step 6: Commit**

```bash
git add src/main/java/org/dromara/easytl/exception/TemplateRuntimeException.java
git commit -m "refactor(exception): simplify TemplateRuntimeException using Position+Source"
```

---

### Task 5: Update ArgumentIndexOutOfBoundsException

**Files:**
- Modify: `src/main/java/org/dromara/easytl/exception/ArgumentIndexOutOfBoundsException.java`

Note: This class extends TemplateRuntimeException, so it inherits Position and Source. Just need to ensure constructors work correctly.

**Step 1: Update constructors to use Position**

Change constructor at line 82-89 to use Position:

```java
public ArgumentIndexOutOfBoundsException(final int index, final Integer arrayLength,
                                          final int startLine, final int startColumn,
                                          final int endLine, final int endColumn,
                                          final String sourceText) {
    super(buildOutOfBoundsMessage(index, arrayLength),
          new Position(startLine, startColumn, endLine, endColumn),
          sourceText != null ? new StringSource(sourceText) : null);
    this.index = index;
    this.arrayLength = arrayLength;
}
```

Add import:
```java
import org.dromara.easytl.parser.StringSource;
```

**Step 2: Run tests**

Run: `mvn test -Dtest=TemplateExceptionTest`
Expected: PASS

**Step 3: Commit**

```bash
git add src/main/java/org/dromara/easytl/exception/ArgumentIndexOutOfBoundsException.java
git commit -m "refactor(exception): update ArgumentIndexOutOfBoundsException constructors"
```

---

### Task 6: Run all tests

**Step 1: Run full test suite**

Run: `mvn test`
Expected: All tests PASS

**Step 2: Commit**

```bash
git commit -m "test: verify all exception refactoring tests pass"
```

---

### Task 7: Final verification

**Step 1: Run tests with coverage**

Run: `mvn test jacoco:report`
Expected: Code coverage maintained or improved

**Step 2: Commit final changes**

```bash
git add -A
git commit -m "refactor(exception): complete Position+Source refactoring

- Move Position and Source fields to TemplateException base class
- Simplify TemplateSyntaxException and TemplateRuntimeException
- Add lazy extraction for source text via Source interface
- Maintain backward compatibility with existing constructors"
```
