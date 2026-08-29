package org.dromara.easytl.parser;

/**
 * 基于字符串的 Source 实现
 * 适用于小型模板，直接从字符串读取字符
 *
 * @author EasyTL Team
 */
public class StringSource implements Source {

    private final String content;
    private int position;
    private int line;
    private int column;
    private int endColumn = 1;
    private int markedPosition;
    private int markedLine;
    private int markedColumn;

    /**
     * 构造一个 StringSource
     *
     * @param content 模板内容
     */
    public StringSource(String content) {
        this.content = content != null ? content : "";
        this.position = 0;
        this.line = 1;
        this.column = 1;
        this.markedPosition = 0;
        this.markedLine = 1;
        this.markedColumn = 1;
    }

    @Override
    public int read() {
        if (position >= content.length()) {
            return -1;
        }

        char ch = content.charAt(position++);
        
        // 处理换行符，更新行列信息
        if (ch == '\n') {
            line++;
            column = 1;
        } else if (ch == '\r') {
            line++;
            column = 1;
            // 处理 \r\n 的情况 
            if (position < content.length() && content.charAt(position) == '\n') {
                position++;
            }
        } else {
            column++;
        }

        endColumn = column;
        return ch;
    }

    @Override
    public int peek() {
        return peek(0);
    }

    @Override
    public int peek(int offset) {
        if (offset < 0) {
            return -1;
        }

        int index = position;
        int remaining = offset;

        while (true) {
            if (index >= content.length()) {
                return -1;
            }

            char ch = content.charAt(index);
            if (remaining == 0) {
                return ch;
            }

            index++;
            if (ch == '\r' && index < content.length() && content.charAt(index) == '\n') {
                index++;
            }

            remaining--;
        }
    }

    @Override
    public int getPosition() {
        return position;
    }

    @Override
    public int getLine() {
        return line;
    }

    @Override
    public int getColumn() {
        return column;
    }

    @Override
    public int getEndColumn() {
        return endColumn;
    }

    @Override
    public void mark() {
        markedPosition = position;
        markedLine = line;
        markedColumn = column;
    }

    @Override
    public void reset() {
        position = markedPosition;
        line = markedLine;
        column = markedColumn;
    }

    @Override
    public boolean isEOF() {
        return position >= content.length();
    }

    @Override
    public String getSourceText() {
        return content;
    }
}

