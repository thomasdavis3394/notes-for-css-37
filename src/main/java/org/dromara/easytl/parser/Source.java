package org.dromara.easytl.parser;

import java.io.IOException;

/**
 * 模板源文件的抽象接口，提供字符流读取功能
 *
 * @author EasyTL Team
 */
public interface Source {

    /**
     * 读取下一个字符并前进位置
     *
     * @return 下一个字符，如果到达文件末尾返回 -1
     * @throws IOException 读取错误
     */
    int read() throws IOException;

    /**
     * 预览下一个字符但不前进位置
     *
     * @return 下一个字符，如果到达文件末尾返回 -1
     * @throws IOException 读取错误
     */
    int peek() throws IOException;

    /**
     * 预览指定偏移位置的字符但不前进位置
     *
     * @param offset 偏移量（0表示当前位置）
     * @return 指定位置的字符，如果超出范围返回 -1
     * @throws IOException 读取错误
     */
    int peek(int offset) throws IOException;

    /**
     * 获取当前读取位置（字符的绝对位置）
     *
     * @return 当前位置
     */
    int getPosition();

    /**
     * 获取当前行号（从 1 开始）
     *
     * @return 当前行号
     */
    int getLine();

    /**
     * 获取当前列号（从 1 开始）
     *
     * @return 当前列号
     */
    int getColumn();

    /**
     * 获取当前字符结束后的列号
     * 即读取完当前字符后，当前位置的列号
     *
     * @return 结束列号
     */
    int getEndColumn();

    /**
     * 标记当前位置
     */
    void mark();

    /**
     * 重置到标记位置
     */
    void reset();

    /**
     * 判断是否到达文件末尾
     *
     * @return 如果到达末尾返回 true
     */
    boolean isEOF();

    /**
     * 获取完整的源代码文本
     *
     * @return 源代码文本
     */
    String getSourceText();
}

