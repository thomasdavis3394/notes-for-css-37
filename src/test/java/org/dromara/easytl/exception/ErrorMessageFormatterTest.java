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