package org.dromara.easytl.runtime;

import org.dromara.easytl.Context;
import org.dromara.easytl.ast.ASTNode;
import org.dromara.easytl.ast.TextNode;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class EtlURLTest {

    private EtlURL createUrl(String urlStr) {
        List<ASTNode> children = Collections.singletonList(new TextNode(urlStr));
        return new EtlURL(children, new Context(), null, null);
    }

    @Test
    public void testBasicUrl() {
        EtlURL url = createUrl("http://localhost/path");
        assertEquals("http", url.getScheme());
        assertEquals("localhost", url.getHost());
        assertEquals("/path", url.getPath());
    }

    @Test
    public void testFullUrl() {
        EtlURL url = createUrl("https://user:pass@host:8080/path?q=1#ref");
        assertEquals("https", url.getScheme());
        assertEquals("user:pass", url.getUserInfo());
        assertEquals("host", url.getHost());
        assertEquals(8080, url.getPort());
        assertEquals("/path", url.getPath());
        assertEquals("q=1", url.getQuery());
        assertEquals("ref", url.getRef());
    }

    @Test
    public void testRelativeUrl() {
        EtlURL url = createUrl("/path/only");
        assertNull(url.getScheme());
        assertNull(url.getHost());
        assertEquals("/path/only", url.getPath());
    }

    @Test
    public void testQueryOnly() {
        EtlURL url = createUrl("?a=1&b=2");
        assertEquals("a=1&b=2", url.getQuery());
    }

    @Test
    public void testRefOnly() {
        EtlURL url = createUrl("#section");
        assertEquals("section", url.getRef());
    }

    @Test
    public void testModifyScheme() {
        EtlURL url = createUrl("http://host/path");
        url.setScheme("https");
        assertEquals("https", url.getScheme());
        assertEquals("https://host/path", url.toString());
    }

    @Test
    public void testModifyHost() {
        EtlURL url = createUrl("http://host/path");
        url.setHost("newhost");
        assertEquals("newhost", url.getHost());
        assertEquals("http://newhost/path", url.toString());
    }

    @Test
    public void testModifyPort() {
        EtlURL url = createUrl("http://host:80/path");
        url.setPort(443);
        assertEquals(443, url.getPort());
        assertEquals("http://host:443/path", url.toString());
    }

    @Test
    public void testModifyPath() {
        EtlURL url = createUrl("http://host/old");
        url.setPath("/new");
        assertEquals("/new", url.getPath());
        assertEquals("http://host/new", url.toString());
    }

    @Test
    public void testModifyQuery() {
        EtlURL url = createUrl("http://host?a=1");
        url.setQuery("b=2");
        assertEquals("b=2", url.getQuery());
        assertEquals("http://host?b=2", url.toString());
    }

    @Test
    public void testModifyRef() {
        EtlURL url = createUrl("http://host/path#old");
        url.setRef("new");
        assertEquals("new", url.getRef());
    }

    @Test
    public void testToString() {
        EtlURL url = createUrl("http://localhost/path");
        assertEquals("http://localhost/path", url.toString());
    }

    @Test
    public void testToStringWithAllComponents() {
        EtlURL url = createUrl("https://user:pass@host:8080/path?q=1#ref");
        assertEquals("https://user:pass@host:8080/path?q=1#ref", url.toString());
    }

    @Test
    public void testGetQuerySingle() {
        EtlURL url = createUrl("http://host/path?a=1&b=2");
        assertEquals("1", url.getQuery("a"));
        assertEquals("2", url.getQuery("b"));
        assertNull(url.getQuery("c"));
    }

    @Test
    public void testGetQueryValues() {
        EtlURL url = createUrl("http://host/path?a=1&a=2");
        List<String> values = url.getQueryValues("a");
        assertEquals(2, values.size());
        assertTrue(values.contains("1"));
        assertTrue(values.contains("2"));
    }

    @Test
    public void testAddQuery() {
        EtlURL url = createUrl("http://host/path?a=1");
        url.addQuery("b", "2");
        assertTrue(url.toString().contains("a=1"));
        assertTrue(url.toString().contains("b=2"));
    }

    @Test
    public void testSetQuery() {
        EtlURL url = createUrl("http://host/path?a=1");
        url.setQuery("a", "2");
        assertEquals("2", url.getQuery("a"));
    }

    @Test
    public void testRemoveQuery() {
        EtlURL url = createUrl("http://host/path?a=1&b=2");
        url.removeQuery("a");
        assertNull(url.getQuery("a"));
        assertEquals("2", url.getQuery("b"));
    }

    @Test
    public void testDefaultEncoding() {
        EtlURL url = createUrl("http://host/path?key=hello world");
        // Default encoder is URL encoder, spaces become +
        String result = url.toString();
        assertTrue(result.contains("key=hello+world"));
    }

    @Test
    public void testEncodingChineseCharacters() {
        EtlURL url = createUrl("http://host/path?q=中文");
        String result = url.toString();
        // Chinese characters should be percent-encoded
        assertTrue(result.contains("%"));
    }

    @Test
    public void testNoopEncoding() {
        Context ctx = new Context();
        // Use put directly since setUrlQueryValueEncoder doesn't exist yet
        ctx.put("_urlQueryValueEncoder", UrlEncoders.NOOP);
        EtlURL url = new EtlURL(
            Collections.singletonList(new TextNode("http://host/path?key=hello world")),
            ctx, null, null
        );
        String result = url.toString();
        // With NOOP encoder, spaces remain as-is
        assertTrue(result.contains("key=hello world"));
    }
}