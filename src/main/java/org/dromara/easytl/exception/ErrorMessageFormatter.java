package org.dromara.easytl.exception;

import org.dromara.easytl.ast.Position;
import org.dromara.easytl.parser.Source;

/**
 * Utility class for formatting error messages with source code position information.
 * <p>
 * Provides Rust-style error message formatting with visual indicators showing
 * the exact location of errors in template source code.
 * </p>
 *
 * @author gongjun
 * @since 1.0.0
 */
public final class ErrorMessageFormatter {

    /** Error type constant for syntax errors */
    public static final String SYNTAX_ERROR = "Syntax error";

    /** Error type constant for runtime errors */
    public static final String RUNTIME_ERROR = "Runtime error";

    /** Error type constant for compile errors */
    public static final String COMPILE_ERROR = "Compile error";

    private ErrorMessageFormatter() {
        // Utility class, prevent instantiation
    }

    /**
     * Format an error message with position information.
     *
     * @param errorType the type of error (e.g., "Syntax error", "Runtime error")
     * @param message the error message
     * @param startLine the start line number (1-based), -1 for unknown
     * @param startColumn the start column number (1-based), -1 for unknown
     * @param endLine the end line number (1-based), -1 for unknown
     * @param endColumn the end column number (1-based), -1 for unknown
     * @param sourceText the source code text, may be null
     * @return the formatted error message
     */
    public static String formatMessage(final String errorType, final String message,
                                        final int startLine, final int startColumn,
                                        final int endLine, final int endColumn,
                                        final String sourceText) {
        if (startLine >= 1 && startColumn >= 1 && sourceText != null) {
            return formatRustStyleMessage(errorType, message, startLine, startColumn, endLine, endColumn, sourceText);
        } else if (startLine >= 1 && startColumn >= 1 && endLine >= 1 && endColumn >= 1) {
            return formatPositionRange(errorType, message, startLine, startColumn, endLine, endColumn);
        } else if (startLine >= 1 && startColumn >= 1) {
            return String.format("%s [line:%d, column:%d]: %s", errorType, startLine, startColumn, message);
        } else if (startLine >= 1) {
            return String.format("%s [line:%d]: %s", errorType, startLine, message);
        } else {
            return message;
        }
    }

    /**
     * Format an error message with position range (without source code display).
     *
     * @param errorType the type of error
     * @param message the error message
     * @param startLine the start line number (1-based)
     * @param startColumn the start column number (1-based)
     * @param endLine the end line number (1-based)
     * @param endColumn the end column number (1-based)
     * @return the formatted error message
     */
    private static String formatPositionRange(final String errorType, final String message,
                                               final int startLine, final int startColumn,
                                               final int endLine, final int endColumn) {
        if (startLine == endLine && startColumn == endColumn) {
            return String.format("%s [line:%d, column:%d]: %s", errorType, startLine, startColumn, message);
        } else if (startLine == endLine) {
            return String.format("%s [line:%d, column:%d-%d]: %s",
                    errorType, startLine, startColumn, endColumn, message);
        } else {
            return String.format("%s [line:%d:%d - %d:%d]: %s",
                    errorType, startLine, startColumn, endLine, endColumn, message);
        }
    }

    /**
     * Format a Rust-style error message with visual source code display.
     * <p>
     * Example output:
     * <pre>
     *
     *  |
     * 1 | Hello {user.name}!
     *  |        ^^^^^^^^^^^^ Runtime error: Cannot access property 'name' of null
     * </pre>
     * </p>
     *
     * @param errorType the type of error (e.g., "Syntax error", "Runtime error")
     * @param message the error message
     * @param startLine the start line number (1-based)
     * @param startColumn the start column number (1-based)
     * @param endLine the end line number (1-based)
     * @param endColumn the end column number (1-based)
     * @param sourceText the source code text
     * @return the formatted error message
     */
    private static String formatRustStyleMessage(final String errorType, final String message,
                                                  final int startLine, final int startColumn,
                                                  final int endLine, final int endColumn,
                                                  final String sourceText) {
        StringBuilder sb = new StringBuilder();

        // Add error reason at the beginning
        sb.append(errorType).append(": ").append(message).append("\n\n");

        // Split source code into lines
        String[] lines = sourceText.split("\r?\n");

        if (startLine < 1 || startLine > lines.length) {
            // Invalid line number, fall back to simple format
            return formatPositionRange(errorType, message, startLine, startColumn, endLine, endColumn);
        }

        String errorLine = lines[startLine - 1];
        int lineNumberWidth = String.valueOf(startLine).length();

        // Add empty line with separator
        sb.append(padding(lineNumberWidth)).append(" |\n");

        // Display the error line
        sb.append(String.format("%" + lineNumberWidth + "d", startLine))
          .append(" | ")
          .append(errorLine)
          .append("\n");

        // Calculate error marker length
        // Note: endColumn in Token points to the position AFTER the last character,
        // so we use endColumn - startColumn (not +1)
        int errorLength;
        if (startLine == endLine && endColumn >= startColumn) {
            // Single line error
            // Use Math.max(1, ...) to ensure at least one ^ for zero-width positions (e.g., EOF)
            errorLength = Math.max(1, endColumn - startColumn);
        } else {
            // Multi-line error, mark from start to end of line
            errorLength = Math.max(1, errorLine.length() - startColumn + 1);
        }

        // Display error position indicator and error message
        sb.append(padding(lineNumberWidth))
          .append(" | ")
          .append(padding(Math.max(0, startColumn - 1)));

        // Draw error marker using ^ characters
        for (int i = 0; i < errorLength; i++) {
            sb.append('^');
        }

        // For the pointer line, only show the actual error message (without error type prefix)
        // to avoid redundancy. The first line already shows the full error type + message.
        // Multi-line messages (e.g. assertion failures with Expected/Actual lines) are
        // truncated to their first line here; the full message is shown in the header above.
        sb.append(" ").append(firstLine(message));

        // 指针行之后空一行，与后续内容（如堆栈）分隔
        sb.append("\n");

        return sb.toString();
    }

    /**
     * Extract the first line of a (possibly multi-line) message.
     *
     * @param message the message
     * @return the first line, without the line terminator
     */
    private static String firstLine(final String message) {
        if (message == null) {
            return "";
        }
        int newline = message.indexOf('\n');
        String first = newline >= 0 ? message.substring(0, newline) : message;
        return first.endsWith("\r") ? first.substring(0, first.length() - 1) : first;
    }

    /**
     * Generate a padding string of spaces with the specified length.
     *
     * @param length the number of spaces
     * @return a string containing the specified number of spaces
     */
    public static String padding(final int length) {
        if (length <= 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(' ');
        }
        return sb.toString();
    }

    /**
     * Format an error message with Position and Source objects.
     *
     * @param errorType the type of error (e.g., "Syntax error", "Runtime error")
     * @param message the error message
     * @param position the position information, may be null
     * @param source the source code, may be null
     * @return the formatted error message
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

    /**
     * Format a syntax error message with Position and Source objects.
     *
     * @param message the error message
     * @param position the position information, may be null
     * @param source the source code, may be null
     * @return the formatted error message
     */
    public static String formatSyntaxError(final String message, final Position position, final Source source) {
        return formatMessage(SYNTAX_ERROR, message, position, source);
    }

    /**
     * Format a runtime error message with Position and Source objects.
     *
     * @param message the error message
     * @param position the position information, may be null
     * @param source the source code, may be null
     * @return the formatted error message
     */
    public static String formatRuntimeError(final String message, final Position position, final Source source) {
        return formatMessage(RUNTIME_ERROR, message, position, source);
    }

    /**
     * Format a compile error message with Position and Source objects.
     *
     * @param message the error message
     * @param position the position information, may be null
     * @param source the source code, may be null
     * @return the formatted error message
     */
    public static String formatCompileError(final String message, final Position position, final Source source) {
        return formatMessage(COMPILE_ERROR, message, position, source);
    }
}