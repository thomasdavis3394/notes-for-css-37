package org.dromara.easytl.parser;

import org.dromara.easytl.exception.TemplateSyntaxException;

import java.io.IOException;

/**
 * Token 流，提供流式的 Token 读取和预览功能
 *
 * @author EasyTL Team
 */
public class TokenStream {

    private static final int DEFAULT_BUFFER_SIZE = 16;

    private final Lexer lexer;
    private final Token[] buffer;
    private int bufferSize;
    private int head;
    private boolean eof;

    // Mark/reset support for lookahead
    private int markedHead = -1;
    private int markedBufferSize = -1;
    private boolean markedEof = false;
    private Token[] markedBuffer = null;

    /**
     * 构造一个 Token 流
     *
     * @param lexer 词法分析器
     */
    public TokenStream(Lexer lexer) {
        this(lexer, DEFAULT_BUFFER_SIZE);
    }

    /**
     * 构造一个 Token 流
     *
     * @param lexer      词法分析器
     * @param bufferSize 缓冲区大小
     */
    public TokenStream(Lexer lexer, int bufferSize) {
        this.lexer = lexer;
        this.buffer = new Token[bufferSize];
        this.bufferSize = 0;
        this.head = 0;
        this.eof = false;
    }

    /**
     * 读取下一个 Token（消费）
     *
     * @return 下一个 Token，如果到达末尾返回 EOF Token
     */
    public Token nextToken() {
        try {
            ensureBuffer(1);
            if (bufferSize == 0) {
                return createEOFToken();
            }
            Token token = buffer[head];
            head = (head + 1) % buffer.length;
            bufferSize--;
            return token;
        } catch (IOException e) {
            throw new TemplateSyntaxException("Error reading token", e);
        }
    }

    /**
     * 预览下一个 Token（不消费）
     *
     * @return 下一个 Token，如果到达末尾返回 EOF Token
     */
    public Token peek() {
        return peek(0);
    }

    /**
     * 预览指定偏移位置的 Token（不消费）
     *
     * @param offset 偏移量（0 表示下一个 Token）
     * @return 指定位置的 Token，如果超出范围返回 EOF Token
     */
    public Token peek(int offset) {
        try {
            ensureBuffer(offset + 1);
            if (offset >= bufferSize) {
                return createEOFToken();
            }
            int index = (head + offset) % buffer.length;
            return buffer[index];
        } catch (IOException e) {
            throw new TemplateSyntaxException("Error previewing token", e);
        }
    }

    /**
     * 判断是否还有更多 Token
     *
     * @return 如果还有 Token 返回 true
     */
    public boolean hasNext() {
        try {
            ensureBuffer(1);
            return bufferSize > 0;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * 获取当前 Token 位置
     *
     * @return 当前位置
     */
    public int getPosition() {
        Token token = peek();
        return token.getPosition();
    }

    /**
     * Mark current position in the token stream.
     * Can be restored later with resetToMark().
     * Only one mark can be active at a time.
     */
    public void mark() {
        // When marking, ensure the buffer contains tokens at the current position
        // This is crucial for the reset operation to work correctly
        if (bufferSize == 0) {
            // No tokens in buffer, we need to read ahead to capture the current position
            try {
                ensureBuffer(1);
            } catch (IOException e) {
                // Ignore - will be handled on next read
            }
        }
        markedHead = head;
        markedBufferSize = bufferSize;
        markedEof = eof;
        // Save buffer contents
        if (markedBuffer == null || markedBuffer.length < buffer.length) {
            markedBuffer = new Token[buffer.length];
        }
        // Copy the entire buffer (tokens are at positions head to head+bufferSize-1 modulo buffer.length)
        System.arraycopy(buffer, 0, markedBuffer, 0, buffer.length);
    }

    /**
     * Reset to the previously marked position.
     * Must call mark() before resetToMark().
     */
    public void resetToMark() {
        if (markedHead == -1) {
            throw new IllegalStateException("No mark set");
        }
        head = markedHead;
        bufferSize = markedBufferSize;
        eof = markedEof;
        System.arraycopy(markedBuffer, 0, buffer, 0, buffer.length);
        markedHead = -1;  // Clear mark
    }

    /**
     * Clear any active mark.
     */
    public void clearMark() {
        markedHead = -1;
    }

    /**
     * 确保缓冲区中有足够的 Token
     *
     * @param required 需要的 Token 数量
     * @throws IOException 读取错误
     */
    private void ensureBuffer(int required) throws IOException {
        if (eof || bufferSize >= required) {
            return;
        }

        while (bufferSize < required && bufferSize < buffer.length && !eof) {
            Token token = lexer.nextToken();
            if (token.getType() == TokenType.EOF) {
                eof = true;
                break;
            }
            int tail = (head + bufferSize) % buffer.length;
            buffer[tail] = token;
            bufferSize++;
        }
    }

    /**
     * 创建 EOF Token
     */
    private Token createEOFToken() {
        int column = lexer.getColumn();
        return new Token(TokenType.EOF, lexer.getLine(), column, column, lexer.getPosition());
    }
}

