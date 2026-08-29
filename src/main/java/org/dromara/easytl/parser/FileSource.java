package org.dromara.easytl.parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * 基于文件的 Source 实现
 * 内部使用 StreamSource 实现
 *
 * @author EasyTL Team
 */
public class FileSource implements Source {

    private final StreamSource delegate;

    /**
     * 构造一个 FileSource
     *
     * @param file 模板文件
     * @throws IOException 读取文件错误
     */
    public FileSource(File file) throws IOException {
        this(file, StandardCharsets.UTF_8);
    }

    /**
     * 构造一个 FileSource
     *
     * @param file    模板文件
     * @param charset 字符编码
     * @throws IOException 读取文件错误
     */
    public FileSource(File file, Charset charset) throws IOException {
        this.delegate = new StreamSource(new FileInputStream(file), charset);
    }

    /**
     * 构造一个 FileSource
     *
     * @param filePath 模板文件路径
     * @throws IOException 读取文件错误
     */
    public FileSource(String filePath) throws IOException {
        this(new File(filePath));
    }

    /**
     * 构造一个 FileSource
     *
     * @param filePath 模板文件路径
     * @param charset  字符编码
     * @throws IOException 读取文件错误
     */
    public FileSource(String filePath, Charset charset) throws IOException {
        this(new File(filePath), charset);
    }

    @Override
    public int read() throws IOException {
        return delegate.read();
    }

    @Override
    public int peek() throws IOException {
        return delegate.peek();
    }

    @Override
    public int peek(int offset) throws IOException {
        return delegate.peek(offset);
    }

    @Override
    public int getPosition() {
        return delegate.getPosition();
    }

    @Override
    public int getLine() {
        return delegate.getLine();
    }

    @Override
    public int getColumn() {
        return delegate.getColumn();
    }

    @Override
    public int getEndColumn() {
        return delegate.getEndColumn();
    }

    @Override
    public void mark() {
        delegate.mark();
    }

    @Override
    public void reset() {
        delegate.reset();
    }

    @Override
    public boolean isEOF() {
        return delegate.isEOF();
    }

    @Override
    public String getSourceText() {
        return delegate.getSourceText();
    }
}

