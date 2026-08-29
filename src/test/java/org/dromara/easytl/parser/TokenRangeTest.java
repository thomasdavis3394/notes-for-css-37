package org.dromara.easytl.parser;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TokenRangeTest {

    @Test
    public void testTokenHasStartAndEndColumn() {
        // 测试 Token 构造函数接受 startColumn 和 endColumn
        Token token = new Token(TokenType.IDENTIFIER, "abc", 1, 1, 3, 0);
        assertEquals(1, token.getStartColumn());
        assertEquals(3, token.getEndColumn());
    }

    @Test
    public void testSingleCharToken() {
        Token token = new Token(TokenType.PLUS, "+", 1, 5, 5, 0);
        assertEquals(5, token.getStartColumn());
        assertEquals(5, token.getEndColumn());
    }

    @Test
    public void testMultiCharToken() {
        Token token = new Token(TokenType.IDENTIFIER, "hello", 1, 1, 5, 0);
        assertEquals(1, token.getStartColumn());
        assertEquals(5, token.getEndColumn());
    }
}
