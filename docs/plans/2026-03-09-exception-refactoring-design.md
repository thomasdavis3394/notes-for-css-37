# Exception Refactoring Design: Position + Source

**Date:** 2026-03-09
**Status:** Approved

## Overview

Refactor the exception hierarchy to:
1. Reduce code duplication by moving common fields to the base class
2. Use `Position` object instead of individual line/column fields
3. Use `Source` reference instead of storing full `String sourceText` (lazy extraction)

## Design

### Section 1: TemplateException (Base Class)

```java
package org.dromara.easytl.exception;

import org.dromara.easytl.ast.Position;
import org.dromara.easytl.parser.Source;

public class TemplateException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private final Position position;
    private final Source source;

    // Protected constructor for subclasses
    protected TemplateException(String message, Position position, Source source) {
        super(formatMessage(message, position, source));
        this.position = position;
        this.source = source;
    }

    // Simple constructors (no position info)
    public TemplateException(String message) {
        this(message, null, null);
    }

    public TemplateException(String message, Throwable cause) {
        super(message, cause);
        this.position = null;
        this.source = null;
    }

    public TemplateException(Throwable cause) {
        super(cause);
        this.position = null;
        this.source = null;
    }

    // Getters
    public Position getPosition() { return position; }
    public Source getSource() { return source; }

    // Convenience getters for backward compatibility
    public int getStartLine() { return position != null ? position.getStartLine() : -1; }
    public int getStartColumn() { return position != null ? position.getStartColumn() : -1; }
    public int getEndLine() { return position != null ? position.getEndLine() : -1; }
    public int getEndColumn() { return position != null ? position.getEndColumn() : -1; }
    public String getSourceText() { return source != null ? source.getSourceText() : null; }

    private static String formatMessage(String message, Position position, Source source) {
        // Delegate to ErrorMessageFormatter
    }
}
```

### Section 2: TemplateSyntaxException (Simplified)

```java
package org.dromara.easytl.exception;

import org.dromara.easytl.ast.Position;
import org.dromara.easytl.parser.Source;
import org.dromara.easytl.parser.StringSource;

public class TemplateSyntaxException extends TemplateException {
    private static final long serialVersionUID = 1L;

    // Simple constructor
    public TemplateSyntaxException(String message) {
        super(message);
    }

    // With Position and Source
    public TemplateSyntaxException(String message, Position position, Source source) {
        super(ErrorMessageFormatter.formatSyntaxError(message, position, source),
              position, source);
    }

    // With Position only
    public TemplateSyntaxException(String message, Position position) {
        this(message, position, null);
    }

    // Backward-compatible: with individual position fields + String sourceText
    public TemplateSyntaxException(String message, int startLine, int startColumn,
                                  int endLine, int endColumn, String sourceText) {
        this(message,
             new Position(startLine, startColumn, endLine, endColumn),
             sourceText != null ? new StringSource(sourceText) : null);
    }

    // With cause
    public TemplateSyntaxException(String message, Throwable cause) {
        super(message, cause);
    }

    // Deprecated getters (now inherited from base class)
    @Deprecated
    public int getLine() { return getStartLine(); }

    @Deprecated
    public int getColumn() { return getStartColumn(); }
}
```

### Section 3: TemplateRuntimeException (Simplified)

```java
package org.dromara.easytl.exception;

import org.dromara.easytl.ast.Position;
import org.dromara.easytl.parser.Source;
import org.dromara.easytl.parser.StringSource;

public class TemplateRuntimeException extends TemplateException {
    private static final long serialVersionUID = 1L;

    // Simple constructor
    public TemplateRuntimeException(String message) {
        super(message);
    }

    // With Position and Source
    public TemplateRuntimeException(String message, Position position, Source source) {
        super(ErrorMessageFormatter.formatRuntimeError(message, position, source),
              position, source);
    }

    // With Position only
    public TemplateRuntimeException(String message, Position position) {
        this(message, position, null);
    }

    // Backward-compatible: with individual position fields + String sourceText
    public TemplateRuntimeException(String message, int startLine, int startColumn,
                                  int endLine, int endColumn, String sourceText) {
        this(message,
             new Position(startLine, startColumn, endLine, endColumn),
             sourceText != null ? new StringSource(sourceText) : null);
    }

    // With cause
    public TemplateRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    // With cause + position info
    public TemplateRuntimeException(String message, Throwable cause, int startLine,
                                   int startColumn, int endLine, int endColumn,
                                   String sourceText) {
        this(message,
             new Position(startLine, startColumn, endLine, endColumn),
             sourceText != null ? new StringSource(sourceText) : null);
    }
}
```

### Section 4: ErrorMessageFormatter Changes

Add overloaded methods:

```java
public static String formatMessage(String errorType, String message,
                                   Position position, Source source) {
    // Extract position info and delegate to existing method
}

public static String formatSyntaxError(String message, Position position, Source source) {
    return formatMessage(SYNTAX_ERROR, message, position, source);
}

public static String formatRuntimeError(String message, Position position, Source source) {
    return formatMessage(RUNTIME_ERROR, message, position, source);
}
```

### Section 5: Backward Compatibility

- Keep all existing constructors with individual parameters for binary compatibility
- Keep `@Deprecated getLine()` and `getColumn()` methods
- Exception message format unchanged

## Implementation Steps

1. Modify TemplateException - add Position and Source fields
2. Simplify TemplateSyntaxException - remove duplicate fields
3. Simplify TemplateRuntimeException - remove duplicate fields
4. Update ErrorMessageFormatter - add overloaded methods
5. Run tests - verify no behavioral changes
