package org.dromara.easytl.compiler;

import org.dromara.easytl.Context;
import org.dromara.easytl.ast.ASTNode;
import org.dromara.easytl.ast.TextNode;
import org.dromara.easytl.ast.UrlLiteral;
import org.dromara.easytl.runtime.EtlURL;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for URL literal evaluation
 * Note: Full URL literal syntax tests will be in UrlLiteralIntegrationTest
 * after Lexer and Parser support is added
 */
public class UrlEvaluatorTest {

    private EtlURL evaluateUrlLiteral(String urlText) {
        List<ASTNode> children = Collections.singletonList(new TextNode(urlText));
        UrlLiteral urlLiteral = new UrlLiteral(children);
        Evaluator evaluator = new Evaluator(new Context());
        return (EtlURL) urlLiteral.accept(evaluator);
    }

    @Test
    public void testEvaluateSimpleUrl() {
        EtlURL url = evaluateUrlLiteral("http://localhost/path");
        assertEquals("http://localhost/path", url.toString());
    }

    @Test
    public void testEvaluateFullUrl() {
        EtlURL url = evaluateUrlLiteral("https://user:pass@host:443/path?q=1#ref");
        assertEquals("https://user:pass@host:443/path?q=1#ref", url.toString());
    }

    @Test
    public void testEvaluateRelativeUrl() {
        EtlURL url = evaluateUrlLiteral("/path/only");
        assertEquals("/path/only", url.toString());
    }

    @Test
    public void testEvaluateQueryOnlyUrl() {
        EtlURL url = evaluateUrlLiteral("?a=1&b=2");
        assertEquals("?a=1&b=2", url.toString());
    }

    @Test
    public void testEvaluateRefOnlyUrl() {
        EtlURL url = evaluateUrlLiteral("#section");
        assertEquals("#section", url.toString());
    }

    @Test
    public void testEvaluateReturnsEtlURL() {
        EtlURL url = evaluateUrlLiteral("http://localhost/path");
        assertNotNull(url);
        assertTrue(url instanceof EtlURL);
    }
}