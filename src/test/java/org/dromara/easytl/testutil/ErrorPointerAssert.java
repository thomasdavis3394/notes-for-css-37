package org.dromara.easytl.testutil;

import org.dromara.easytl.ast.Position;
import org.dromara.easytl.exception.TemplateException;

/**
 * 错误指针位置验证工具类
 */
public class ErrorPointerAssert {

    /**
     * 指针位置信息
     */
    private static class PointerInfo {
        final int startColumn;  // 指针起始列号（从1开始）
        final int length;       // 指针长度（^ 数量）

        PointerInfo(int startColumn, int length) {
            this.startColumn = startColumn;
            this.length = length;
        }
    }

    /**
     * 从指针行提取指针位置信息
     *
     * @param pointerLine 指针行字符串（如 "            ^^^^"）
     * @return PointerInfo 包含起始列号和长度
     */
    private static PointerInfo extractPointerInfo(String pointerLine) {
        int startColumn = -1;
        int length = 0;

        for (int i = 0; i < pointerLine.length(); i++) {
            char c = pointerLine.charAt(i);
            if (c == '^') {
                if (startColumn == -1) {
                    startColumn = i + 1;  // 列号从1开始
                }
                length++;
            }
        }

        if (startColumn == -1) {
            throw new IllegalArgumentException("Pointer line must contain at least one '^' character");
        }

        return new PointerInfo(startColumn, length);
    }

    /**
     * 从源码文本提取指定行内容
     *
     * @param sourceText 源码文本
     * @param lineNumber 行号（从1开始）
     * @return 该行内容
     */
    private static String extractLine(String sourceText, int lineNumber) {
        if (sourceText == null) {
            return null;
        }

        String[] lines = sourceText.split("\r?\n");
        if (lineNumber < 1 || lineNumber > lines.length) {
            return null;
        }

        return lines[lineNumber - 1];
    }

    /**
     * 格式化指针显示（Rust 风格）
     *
     * @param sourceLine 源码行
     * @param pointerLine 指针行
     * @param errorMessage 错误消息
     * @param lineNumber 行号
     * @return 格式化的字符串
     */
    private static String formatPointerDisplay(
        String sourceLine,
        String pointerLine,
        String errorMessage,
        int lineNumber
    ) {
        StringBuilder sb = new StringBuilder();
        sb.append("  |\n");
        sb.append(lineNumber).append(" | ").append(sourceLine).append("\n");
        sb.append("  | ").append(pointerLine).append(" ").append(errorMessage);
        return sb.toString();
    }

    /**
     * 构建指针行字符串
     *
     * @param startColumn 起始列号
     * @param length 指针长度
     * @return 指针行字符串
     */
    private static String buildPointerLine(int startColumn, int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < startColumn; i++) {
            sb.append(' ');
        }
        for (int i = 0; i < length; i++) {
            sb.append('^');
        }
        return sb.toString();
    }

    /**
     * 构建不匹配时的错误消息
     */
    private static String buildMismatchMessage(
        String errorTitle,
        String expectedSourceLine,
        String expectedPointerLine,
        String expectedErrorMessage,
        String actualSourceLine,
        int actualLine,
        int actualStartColumn,
        int actualPointerLength,
        String actualFullMessage
    ) {
        StringBuilder sb = new StringBuilder();
        sb.append(errorTitle).append("\n\n");
        sb.append("Expected:\n\n");
        sb.append(formatPointerDisplay(
            expectedSourceLine, expectedPointerLine, expectedErrorMessage, 1
        ));
        sb.append("\n\nActual:\n\n");

        // 构建实际的指针行
        String actualPointerLine = buildPointerLine(actualStartColumn, actualPointerLength);
        sb.append(formatPointerDisplay(
            actualSourceLine, actualPointerLine, expectedErrorMessage, actualLine
        ));
        sb.append("\n\nFull error message:\n").append(actualFullMessage);

        return sb.toString();
    }

    /**
     * 验证错误指针位置和消息
     *
     * 示例：
     * assertErrorPointer(exception,
     *     "Hello {user.name}!",    // 源码行
     *     "            ^^^^",      // 指针标记
     *     "Cannot access property 'name' of null"  // 错误消息
     * );
     *
     * @param exception 异常对象
     * @param expectedSourceLine 期望的源码行
     * @param expectedPointerLine 期望的指针行
     * @param expectedErrorMessage 期望的错误消息（包含检查）
     */
    public static void assertErrorPointer(
        TemplateException exception,
        String expectedSourceLine,
        String expectedPointerLine,
        String expectedErrorMessage
    ) {
        // Check for null exception
        if (exception == null) {
            throw new AssertionError("Exception cannot be null");
        }

        // 1. 检查位置信息
        Position position = exception.getPosition();
        if (position == null || position.isUnknown()) {
            throw new AssertionError("Missing position information in exception");
        }

        // 2. 检查源码信息
        String actualSourceText = exception.getSourceText();
        if (actualSourceText == null) {
            throw new AssertionError("Missing source text in exception");
        }

        int actualLine = exception.getStartLine();
        int actualStartColumn = exception.getStartColumn();
        int actualEndColumn = exception.getEndColumn();
        // Use Math.max(1, ...) to match the message formatter behavior for zero-width positions (e.g., EOF)
        int actualPointerLength = Math.max(1, actualEndColumn - actualStartColumn);

        // 3. 解析期望的指针信息
        PointerInfo expectedPointerInfo = extractPointerInfo(expectedPointerLine);

        // 4. 提取实际的源码行
        String actualSourceLine = extractLine(actualSourceText, actualLine);

        // 5. 验证源码行匹配
        if (!expectedSourceLine.equals(actualSourceLine)) {
            throw new AssertionError(buildMismatchMessage(
                "Source line mismatch",
                expectedSourceLine, expectedPointerLine, expectedErrorMessage,
                actualSourceLine, actualLine, actualStartColumn, actualPointerLength,
                exception.getMessage()
            ));
        }

        // 6. 验证指针位置匹配
        if (expectedPointerInfo.startColumn != actualStartColumn ||
            expectedPointerInfo.length != actualPointerLength) {
            throw new AssertionError(buildMismatchMessage(
                "Pointer position mismatch",
                expectedSourceLine, expectedPointerLine, expectedErrorMessage,
                actualSourceLine, actualLine, actualStartColumn, actualPointerLength,
                exception.getMessage()
            ));
        }

        // 7. 验证错误消息包含
        String actualMessage = exception.getMessage();
        if (!actualMessage.contains(expectedErrorMessage)) {
            throw new AssertionError(buildMismatchMessage(
                "Message mismatch",
                expectedSourceLine, expectedPointerLine, expectedErrorMessage,
                actualSourceLine, actualLine, actualStartColumn, actualPointerLength,
                actualMessage
            ));
        }

        // 8. 验证位置信息只出现一次
        int locationBlockCount = countLocationBlocks(actualMessage);
        if (locationBlockCount != 1) {
            throw new AssertionError(
                "Location info should appear exactly once, but found " + locationBlockCount + " times.\n" +
                "Full error message:\n" + actualMessage
            );
        }

        System.out.println();
        System.out.println(exception.getMessage());
    }

    /**
     * 统计错误消息中位置信息块的数量
     * 指针行特征：以 "  | " 开头，包含 "^"
     *
     * @param message 错误消息
     * @return 位置信息块数量
     */
    private static int countLocationBlocks(String message) {
        String[] lines = message.split("\r?\n");
        int count = 0;
        for (String line : lines) {
            if (line.startsWith("  | ") && line.contains("^")) {
                count++;
            }
        }
        return count;
    }
}