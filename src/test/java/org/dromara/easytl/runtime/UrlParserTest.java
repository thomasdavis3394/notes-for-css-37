package org.dromara.easytl.runtime;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UrlParserTest {

    @Test
    public void testParseFullUrl() {
        UrlComponents c = UrlParser.parse("https://user:pass@host:8080/path?q=1#ref");
        assertEquals("https", c.scheme);
        assertEquals("user:pass", c.userInfo);
        assertEquals("host", c.host);
        assertEquals(8080, c.port);
        assertEquals("/path", c.path);
        assertEquals("q=1", c.query);
        assertEquals("ref", c.ref);
    }

    @Test
    public void testParseSimpleUrl() {
        UrlComponents c = UrlParser.parse("http://localhost/path");
        assertEquals("http", c.scheme);
        assertNull(c.userInfo);
        assertEquals("localhost", c.host);
        assertNull(c.port);
        assertEquals("/path", c.path);
        assertNull(c.query);
        assertNull(c.ref);
    }

    @Test
    public void testParseRelativePath() {
        UrlComponents c = UrlParser.parse("/path/only");
        assertNull(c.scheme);
        assertNull(c.host);
        assertEquals("/path/only", c.path);
    }

    @Test
    public void testParseQueryOnly() {
        UrlComponents c = UrlParser.parse("?a=1&b=2");
        assertNull(c.scheme);
        assertNull(c.host);
        assertEquals("a=1&b=2", c.query);
    }

    @Test
    public void testParseRefOnly() {
        UrlComponents c = UrlParser.parse("#section");
        assertNull(c.scheme);
        assertNull(c.host);
        assertEquals("section", c.ref);
    }

    @Test
    public void testParseIPv6() {
        UrlComponents c = UrlParser.parse("http://[::1]:8080/path");
        assertEquals("http", c.scheme);
        assertEquals("::1", c.host);
        assertEquals(8080, c.port);
        assertEquals("/path", c.path);
    }

    @Test
    public void testParseIPv6NoPort() {
        UrlComponents c = UrlParser.parse("http://[2001:db8::1]/path");
        assertEquals("http", c.scheme);
        assertEquals("2001:db8::1", c.host);
        assertNull(c.port);
    }

    @Test
    public void testParseFileUrl() {
        UrlComponents c = UrlParser.parse("file:///Users/name/file.txt");
        assertEquals("file", c.scheme);
        assertEquals("/Users/name/file.txt", c.path);
    }

    @Test
    public void testParseWebSocketUrl() {
        UrlComponents c = UrlParser.parse("ws://host/path");
        assertEquals("ws", c.scheme);
        assertEquals("host", c.host);
        assertEquals("/path", c.path);
    }

    @Test
    public void testParseCustomScheme() {
        UrlComponents c = UrlParser.parse("myapp://deep/link?id=123");
        assertEquals("myapp", c.scheme);
        assertEquals("/link", c.path);
        assertEquals("id=123", c.query);
    }

    @Test
    public void testParseEmpty() {
        UrlComponents c = UrlParser.parse("");
        assertNull(c.scheme);
        assertNull(c.host);
        assertNull(c.path);
    }
}