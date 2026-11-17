package org.dromara.easytl;

import org.dromara.easytl.runtime.UrlEncoders;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UrlEncoderContextTest {

    @Test
    public void testDefaultEncoders() {
        Context ctx = new Context();

        // Default encoders should be NOOP or URL
        assertEquals(UrlEncoders.NOOP, ctx.getUrlSchemeEncoder());
        assertEquals(UrlEncoders.URL, ctx.getUrlUserInfoEncoder());
        assertEquals(UrlEncoders.NOOP, ctx.getUrlHostEncoder());
        assertEquals(UrlEncoders.URL, ctx.getUrlPathEncoder());
        assertEquals(UrlEncoders.URL, ctx.getUrlQueryKeyEncoder());
        assertEquals(UrlEncoders.URL, ctx.getUrlQueryValueEncoder());
        assertEquals(UrlEncoders.URL, ctx.getUrlRefEncoder());
    }

    @Test
    public void testSetCustomEncoder() {
        Context ctx = new Context();
        ctx.setUrlQueryValueEncoder(UrlEncoders.BASE64);

        assertEquals(UrlEncoders.BASE64, ctx.getUrlQueryValueEncoder());
    }
}
// 5f473c
