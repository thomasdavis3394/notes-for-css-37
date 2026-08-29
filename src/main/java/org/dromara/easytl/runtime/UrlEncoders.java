package org.dromara.easytl.runtime;

import org.dromara.easytl.exception.UrlEncodingException;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Base64;

/**
 * 内置 URL 编码器集合
 *
 * @author EasyTL Team
 */
public final class UrlEncoders {

    private UrlEncoders() {
        // 工具类，禁止实例化
    }

    /**
     * 无编码器（原样返回）
     */
    public static final UrlEncoder NOOP = new UrlEncoder() {
        @Override
        public String encode(String input) {
            return input;
        }

        @Override
        public String decode(String input) {
            return input;
        }
    };

    /**
     * 标准 URL 编码器 (application/x-www-form-urlencoded)
     */
    public static final UrlEncoder URL = new UrlEncoder() {
        @Override
        public String encode(String input) {
            try {
                return URLEncoder.encode(input, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new UrlEncodingException("url", input, e);
            }
        }

        @Override
        public String decode(String input) {
            try {
                return URLDecoder.decode(input, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new UrlEncodingException("url", input, e);
            }
        }
    };

    /**
     * Base64 编码器
     */
    public static final UrlEncoder BASE64 = new UrlEncoder() {
        @Override
        public String encode(String input) {
            try {
                return Base64.getEncoder().encodeToString(input.getBytes("UTF-8"));
            } catch (UnsupportedEncodingException e) {
                throw new UrlEncodingException("base64", input, e);
            }
        }

        @Override
        public String decode(String input) {
            try {
                return new String(Base64.getDecoder().decode(input), "UTF-8");
            } catch (Exception e) {
                throw new UrlEncodingException("base64", input, e);
            }
        }
    };

    /**
     * URL 安全 Base64 编码器
     */
    public static final UrlEncoder BASE64_URL_SAFE = new UrlEncoder() {
        @Override
        public String encode(String input) {
            try {
                return Base64.getUrlEncoder().encodeToString(input.getBytes("UTF-8"));
            } catch (UnsupportedEncodingException e) {
                throw new UrlEncodingException("base64url", input, e);
            }
        }

        @Override
        public String decode(String input) {
            try {
                return new String(Base64.getUrlDecoder().decode(input), "UTF-8");
            } catch (Exception e) {
                throw new UrlEncodingException("base64url", input, e);
            }
        }
    };
}