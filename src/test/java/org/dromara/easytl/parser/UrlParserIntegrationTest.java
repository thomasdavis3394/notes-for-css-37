package org.dromara.easytl.parser;

import org.dromara.easytl.ast.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UrlParserIntegrationTest {

    private UrlLiteral parseUrl(String source) {
        Lexer lexer = new Lexer(new StringSource(source));
        TokenStream tokens = new TokenStream(lexer);
        Parser parser = new Parser(tokens);
        TemplateNode template = parser.parse();

        // Extract URL literal from template
        for (ASTNode child : template.getChildren()) {
            if (child instanceof SingleBraceExpressionNode) {
                ASTNode expr = ((SingleBraceExpressionNode) child).getExpression();
                if (expr instanceof UrlLiteral) {
                    return (UrlLiteral) expr;
                }
            }
        }
        return null;
    }

    @Test
    public void testParseEmptyUrl() {
        UrlLiteral url = parseUrl("{url()}");
        assertNotNull(url);
        assertTrue(url.getChildren().isEmpty());
    }

    @Test
    public void testParseSimpleUrl() {
        UrlLiteral url = parseUrl("{url(http://localhost)}");
        assertNotNull(url);
        assertEquals(1, url.getChildren().size());
        assertTrue(url.getChildren().get(0) instanceof TextNode);
        assertEquals("http://localhost", ((TextNode) url.getChildren().get(0)).getText());
    }

    @Test
    public void testParseUrlWithExpression() {
        UrlLiteral url = parseUrl("{url(http://{host}/path)}");
        assertNotNull(url);
        assertEquals(3, url.getChildren().size());
        assertTrue(url.getChildren().get(0) instanceof TextNode);
        assertTrue(url.getChildren().get(1) instanceof SingleBraceExpressionNode);
        assertTrue(url.getChildren().get(2) instanceof TextNode);
    }
}