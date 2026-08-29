package org.dromara.easytl.runtime;

import org.junit.jupiter.api.Test;

import java.io.UnsupportedEncodingException;

import static org.junit.jupiter.api.Assertions.*;

public class UrlEncodersTest {

    @Test
    public void testNoopEncoder() {
        UrlEncoder encoder = UrlEncoders.NOOP;
        assertEquals("hello world", encoder.encode("hello world"));
        assertEquals("test", encoder.decode("test"));
    }

    @Test
    public void testUrlEncoder() throws UnsupportedEncodingException {
        UrlEncoder encoder = UrlEncoders.URL;
        assertEquals("hello+world", encoder.encode("hello world"));
        assertEquals("hello world", encoder.decode("hello+world"));
        assertEquals("%E4%B8%AD%E6%96%87", encoder.encode("中文"));
        assertEquals("中文", encoder.decode("%E4%B8%AD%E6%96%87"));
    }

    @Test
    public void testBase64Encoder() {
        UrlEncoder encoder = UrlEncoders.BASE64;
        assertEquals("aGVsbG8=", encoder.encode("hello"));
        assertEquals("hello", encoder.decode("aGVsbG8="));
    }

    @Test
    public void testBase64UrlSafeEncoder() {
        UrlEncoder encoder = UrlEncoders.BASE64_URL_SAFE;
        assertEquals("aGVsbG8=", encoder.encode("hello"));
        assertEquals("hello", encoder.decode("aGVsbG8="));
        String encoded = encoder.encode("data?");
        assertNotNull(encoded);
        assertEquals("data?", encoder.decode(encoded));
    }
}