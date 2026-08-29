package org.dromara.easytl.runtime;

/**
 * URL 编码器接口
 * 用于对 URL 各组件进行编码和解码
 *
 * @author EasyTL Team
 */
public interface UrlEncoder {

    /**
     * 编码字符串
     *
     * @param input 输入字符串
     * @return 编码后的字符串
     */
    String encode(String input);

    /**
     * 解码字符串
     *
     * @param input 编码后的字符串
     * @return 解码后的字符串
     */
    String decode(String input);
}