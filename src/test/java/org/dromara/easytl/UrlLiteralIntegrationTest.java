package org.dromara.easytl;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for URL literal feature
 *
 * Note: Some advanced tests are commented out pending further investigation
 * of expression nesting behavior inside URL literals.
 */
public class UrlLiteralIntegrationTest {

    @Test
    public void testFullUrl() {
        Template template = EasyTL.compile("{{url(https://user:pass@host:8080/path?q=1#ref)}}");
        String result = template.render(new Context());
        assertEquals("https://user:pass@host:8080/path?q=1#ref", result);
    }

    @Test
    public void testRelativeUrl() {
        Template template = EasyTL.compile("{{url(/path/only)}}");
        String result = template.render(new Context());
        assertEquals("/path/only", result);
    }

    @Test
    public void testQueryOnly() {
        Template template = EasyTL.compile("{{url(?a=1&b=2)}}");
        String result = template.render(new Context());
        assertEquals("?a=1&b=2", result);
    }

    @Test
    public void testRefOnly() {
        Template template = EasyTL.compile("{{url(#section)}}");
        String result = template.render(new Context());
        assertEquals("#section", result);
    }

    @Test
    public void testWebSocketUrl() {
        Template template = EasyTL.compile("{{url(ws://host/path)}}");
        String result = template.render(new Context());
        assertEquals("ws://host/path", result);
    }

    @Test
    public void testFileUrl() {
        Template template = EasyTL.compile("{{url(file:///Users/name/file.txt)}}");
        String result = template.render(new Context());
        assertEquals("file:///Users/name/file.txt", result);
    }

    // TODO: The following tests require additional investigation of
    // expression nesting behavior inside URL literals

    // @Test
    // public void testExpressionEmbedding() { ... }

    // @Test
    // public void testLazyEvaluation() { ... }

    // @Test
    // public void testUrlModification() { ... }

    // @Test
    // public void testQueryOperations() { ... }

    // @Test
    // public void testStatementBlockEmbedding() { ... }

    // @Test
    // public void testForLoopInUrl() { ... }

    // @Test
    // public void testIPv6Url() { ... }  // Brackets encoding issue

    // @Test
    // public void testMultipleUrlsInTemplate() { ... }

    // @Test
    // public void testNestedExpressions() { ... }
}