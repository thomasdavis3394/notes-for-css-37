package org.dromara.easytl.parser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * 基于输入流的 Source 实现
 * 使用滑动窗口缓冲区管理，适用于大型模板文件
 *
 * @author EasyTL Team
 */
public class StreamSource implements Source {

    private static final int DEFAULT_BUFFER_SIZE = 8192;
    private static final int DEFAULT_LOOKAHEAD_SIZE = 1024;

    private final Reader reader;
    private final char[] buffer;
    private final int lookaheadSize;
    private final StringBuilder sourceTextBuilder;  // 用于存储完整源代码

    private int bufferEnd;        // 缓冲区中有效数据的结束位置
    private int position;         // 当前读取位置（相对于缓冲区）
    private int absolutePosition; // 绝对位置
    private int line;
    private int column;
    private int endColumn = 1;
    private int markedPosition;
    private int markedAbsolutePosition;
    private int markedLine;
    private int markedColumn;
    private boolean eof;

    /**
     * 构造一个 StreamSource
     *
     * @param inputStream 输入流
     */
    public StreamSource(InputStream inputStream) {
        this(inputStream, StandardCharsets.UTF_8);
    }

    /**
     * 构造一个 StreamSource
     *
     * @param inputStream 输入流
     * @param charset     字符编码
     */
    public StreamSource(InputStream inputStream, Charset charset) {
        this(inputStream, charset, DEFAULT_BUFFER_SIZE);
    }

    /**
     * 构造一个 StreamSource
     *
     * @param inputStream 输入流
     * @param charset     字符编码
     * @param bufferSize  缓冲区大小
     */
    public StreamSource(InputStream inputStream, Charset charset, int bufferSize) {
        this.reader = new InputStreamReader(inputStream, charset);
        this.buffer = new char[bufferSize];
        this.lookaheadSize = Math.min(DEFAULT_LOOKAHEAD_SIZE, bufferSize / 8);
        this.sourceTextBuilder = new StringBuilder();
        this.bufferEnd = 0;
        this.position = 0;
        this.absolutePosition = 0;
        this.line = 1;
        this.column = 1;
        this.markedPosition = 0;
        this.markedAbsolutePosition = 0;
        this.markedLine = 1;
        this.markedColumn = 1;
        this.eof = false;
    }

    @Override
    public int read() throws IOException {
        ensureBuffer(1);

        if (position >= bufferEnd) {
            return -1;
        }

        char ch = buffer[position++];
        absolutePosition++;

        // 将读取的字符添加到源代码缓存中
        sourceTextBuilder.append(ch);

        // 处理换行符
        if (ch == '\n') {
            line++;
            column = 1;
        } else if (ch == '\r') {
            line++;
            column = 1;
            // 处理 \r\n
            ensureBuffer(1);
            if (position < bufferEnd && buffer[position] == '\n') {
                char nextCh = buffer[position++];
                absolutePosition++;
                sourceTextBuilder.append(nextCh);
            }
        } else {
            column++;
        }

        endColumn = column;
        return ch;
    }

    @Override
    public int peek() throws IOException {
        return peek(0);
    }

    @Override
    public int peek(int offset) throws IOException {
        if (offset < 0) {
            return -1;
        }

        int index = position;
        int remaining = offset;

        while (true) {
            int relativeIndex = index - position;
            ensureBuffer(relativeIndex + 1);
            index = position + relativeIndex;

            if (index < 0 || index >= bufferEnd) {
                return -1;
            }

            char ch = buffer[index];
            if (remaining == 0) {
                return ch;
            }

            index++;
            if (ch == '\r') {
                relativeIndex = index - position;
                ensureBuffer(relativeIndex + 1);
                index = position + relativeIndex;
                if (index < bufferEnd && buffer[index] == '\n') {
                    index++;
                }
            }

            remaining--;
        }
    }

    @Override
    public int getPosition() {
        return absolutePosition;
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
        markedAbsolutePosition = absolutePosition;
        markedLine = line;
        markedColumn = column;
    }

    @Override
    public void reset() {
        position = markedPosition;
        absolutePosition = markedAbsolutePosition;
        line = markedLine;
        column = markedColumn;
    }

    @Override
    public boolean isEOF() {
        try {
            ensureBuffer(1);
            return position >= bufferEnd;
        } catch (IOException e) {
            return true;
        }
    }

    /**
     * 确保缓冲区中有足够的数据供预览
     *
     * @param requiredSize 需要的字符数
     * @throws IOException 读取错误
     */
    private void ensureBuffer(int requiredSize) throws IOException {
        if (eof) {
            return;
        }

        int available = bufferEnd - position;
        
        // 如果缓冲区中的数据足够，直接返回
        if (available >= requiredSize) {
            return;
        }

        // 如果缓冲区空间不足，需要移动数据
        int preserveStart = Math.max(0, position - lookaheadSize);
        if (preserveStart > 0) {
            int preserveLength = bufferEnd - preserveStart;
            System.arraycopy(buffer, preserveStart, buffer, 0, preserveLength);
            position -= preserveStart;
            markedPosition -= preserveStart;
            bufferEnd = preserveLength;
        }

        // 读取更多数据
        while (bufferEnd < buffer.length && !eof) {
            int bytesRead = reader.read(buffer, bufferEnd, buffer.length - bufferEnd);
            if (bytesRead == -1) {
                eof = true;
                break;
            }
            bufferEnd += bytesRead;

            // 如果已经满足需求，可以提前返回
            if (bufferEnd - position >= requiredSize) {
                break;
            }
        }
    }

    @Override
    public String getSourceText() {
        return sourceTextBuilder.toString();
    }
}

