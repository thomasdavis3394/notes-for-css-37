package org.dromara.easytl.exception;

/**
 * URL 编码异常
 * 当 URL 编码或解码失败时抛出此异常
 *
 * @author EasyTL Team
 */
public class UrlEncodingException extends TemplateRuntimeException {

    private static final long serialVersionUID = 1L;

    private final String component;
    private final String value;

    public UrlEncodingException(String component, String value, Throwable cause) {
        super("Failed to encode " + component + ": " + value, cause);
        this.component = component;
        this.value = value;
    }

    public String getComponent() {
        return component;
    }

    public String getValue() {
        return value;
    }
}