package org.dromara.easytl.exception;

/**
 * URL 语法异常
 * 当 URL 解析失败时抛出此异常
 *
 * @author EasyTL Team
 */
public class UrlSyntaxException extends TemplateRuntimeException {

    private static final long serialVersionUID = 1L;

    private final String url;
    private final int errorIndex;

    public UrlSyntaxException(String message, String url, int errorIndex) {
        super(message);
        this.url = url;
        this.errorIndex = errorIndex;
    }

    public UrlSyntaxException(String message, String url) {
        this(message, url, -1);
    }

    public String getUrl() {
        return url;
    }

    /**
     * 获取 URL 字符串中发生错误的位置索引
     *
     * @return 错误位置索引，-1 表示未知位置
     */
    public int getErrorIndex() {
        return errorIndex;
    }
}