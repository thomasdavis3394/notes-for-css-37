package org.dromara.easytl.parser;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class UrlLexerTest {

    private Token assertNextToken(Lexer lexer, int expectedType) throws IOException {
        Token token = lexer.nextToken();
        assertEquals(expectedType, token.getType());
        return token;
    }

    private Token assertNextToken(Lexer lexer, int expectedType, String expectedValue) throws IOException {
        Token token = lexer.nextToken();
        assertEquals(expectedType, token.getType());
        if (expectedValue != null) {
            assertEquals(expectedValue, token.getValue());
        }
        return token;
    }

    @Test
    public void testUrlKeyword() throws IOException {
        StringSource source = new StringSource("{url()}");
        Lexer lexer = new Lexer(source);

        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_BEGIN);
        assertNextToken(lexer, TokenType.URL, "url");
        assertNextToken(lexer, TokenType.LEFT_PAREN);
        assertNextToken(lexer, TokenType.RIGHT_PAREN);
        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_END);
        assertNextToken(lexer, TokenType.EOF);
    }

    @Test
    public void testUrlWithText() throws IOException {
        StringSource source = new StringSource("{url(http://localhost)}");
        Lexer lexer = new Lexer(source);

        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_BEGIN);
        assertNextToken(lexer, TokenType.URL, "url");
        assertNextToken(lexer, TokenType.LEFT_PAREN);
        assertNextToken(lexer, TokenType.TEXT, "http://localhost");
        assertNextToken(lexer, TokenType.RIGHT_PAREN);
        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_END);
        assertNextToken(lexer, TokenType.EOF);
    }

    @Test
    public void testUrlWithExpression() throws IOException {
        StringSource source = new StringSource("{url(http://{host}/path)}");
        Lexer lexer = new Lexer(source);

        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_BEGIN);
        assertNextToken(lexer, TokenType.URL, "url");
        assertNextToken(lexer, TokenType.LEFT_PAREN);
        assertNextToken(lexer, TokenType.TEXT, "http://");
        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_BEGIN);
        assertNextToken(lexer, TokenType.IDENTIFIER, "host");
        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_END);
        assertNextToken(lexer, TokenType.TEXT, "/path");
        assertNextToken(lexer, TokenType.RIGHT_PAREN);
        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_END);
        assertNextToken(lexer, TokenType.EOF);
    }
}