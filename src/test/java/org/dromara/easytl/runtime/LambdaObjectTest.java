package org.dromara.easytl.runtime;

import org.dromara.easytl.Context;
import org.dromara.easytl.ast.*;
import org.dromara.easytl.parser.*;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for LambdaObject runtime behavior.
 *
 * @author EasyTL Team
 */
public class LambdaObjectTest {

    /**
     * Parse an expression from a string.
     * Wraps the input in braces so the lexer enters expression mode.
     */
    private ASTNode parseExpression(String input) throws Exception {
        StringSource source = new StringSource("{" + input + "}");
        Lexer lexer = new Lexer(source);
        TokenStream tokens = new TokenStream(lexer);
        Parser parser = new Parser(tokens);
        TemplateNode template = parser.parse();
        // Get the expression from the first child (should be SingleBraceExpressionNode)
        ASTNode child = template.getChildren().get(0);
        if (child instanceof SingleBraceExpressionNode) {
            return ((SingleBraceExpressionNode) child).getExpression();
        }
        throw new RuntimeException("Expected SingleBraceExpressionNode but got " + child.getClass().getSimpleName());
    }

    @Test
    public void testSimpleLambdaInvoke() throws Exception {
        // Parse: x -> x * 2
        LambdaExpression lambdaAst = (LambdaExpression) parseExpression("x -> x * 2");

        Context context = new Context();
        Map<String, Object> captured = new HashMap<>();

        LambdaObject lambda = new LambdaObject(
            lambdaAst.getParameters(),
            lambdaAst.getBody(),
            lambdaAst.getExpressionBody(),
            lambdaAst.isBlockBody(),
            captured,
            context,
            "x -> x * 2",
            true,
            null
        );

        Object result = lambda.invoke(new Object[]{5});
        assertEquals(10, result);
    }

    @Test
    public void testMultiParamLambda() throws Exception {
        // Parse: (a, b) -> a + b
        LambdaExpression lambdaAst = (LambdaExpression) parseExpression("(a, b) -> a + b");

        Context context = new Context();
        LambdaObject lambda = new LambdaObject(
            lambdaAst.getParameters(),
            lambdaAst.getBody(),
            lambdaAst.getExpressionBody(),
            lambdaAst.isBlockBody(),
            new HashMap<>(),
            context,
            "(a, b) -> a + b",
            true,
            null
        );

        Object result = lambda.invoke(new Object[]{3, 4});
        assertEquals(7, result);
    }

    @Test
    public void testNoParamLambda() throws Exception {
        // Parse: () -> 42
        LambdaExpression lambdaAst = (LambdaExpression) parseExpression("() -> 42");

        Context context = new Context();
        LambdaObject lambda = new LambdaObject(
            lambdaAst.getParameters(),
            lambdaAst.getBody(),
            lambdaAst.getExpressionBody(),
            lambdaAst.isBlockBody(),
            new HashMap<>(),
            context,
            "() -> 42",
            true,
            null
        );

        Object result = lambda.invoke(new Object[]{});
        assertEquals(42, result);
    }

    @Test
    public void testWrongArgumentCount() throws Exception {
        LambdaExpression lambdaAst = (LambdaExpression) parseExpression("(a, b) -> a + b");

        LambdaObject lambda = new LambdaObject(
            lambdaAst.getParameters(),
            lambdaAst.getBody(),
            lambdaAst.getExpressionBody(),
            lambdaAst.isBlockBody(),
            new HashMap<>(),
            new Context(),
            "(a, b) -> a + b",
            true,
            null
        );

        try {
            lambda.invoke(new Object[]{1});
            fail("Should throw for wrong argument count");
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains("expects 2"));
        }
    }

    @Test
    public void testCapturedVariable() throws Exception {
        // Parse: x -> x + offset
        LambdaExpression lambdaAst = (LambdaExpression) parseExpression("x -> x + offset");

        Context context = new Context();
        Map<String, Object> captured = new HashMap<>();
        captured.put("offset", 10);

        LambdaObject lambda = new LambdaObject(
            lambdaAst.getParameters(),
            lambdaAst.getBody(),
            lambdaAst.getExpressionBody(),
            lambdaAst.isBlockBody(),
            captured,
            context,
            "x -> x + offset",
            true,
            null
        );

        Object result = lambda.invoke(new Object[]{5});
        assertEquals(15, result);
    }

    @Test
    public void testBlockBodyLambda() throws Exception {
        // Parse: (a, b) -> { a + b }
        LambdaExpression lambdaAst = (LambdaExpression) parseExpression("(a, b) -> { a + b }");

        Context context = new Context();
        LambdaObject lambda = new LambdaObject(
            lambdaAst.getParameters(),
            lambdaAst.getBody(),
            lambdaAst.getExpressionBody(),
            lambdaAst.isBlockBody(),
            new HashMap<>(),
            context,
            "(a, b) -> { a + b }",
            true,
            null
        );

        Object result = lambda.invoke(new Object[]{3, 4});
        assertEquals(7, result);
    }

    @Test
    public void testLambdaWithReturn() throws Exception {
        // Parse: (a, b) -> { return a * b }
        LambdaExpression lambdaAst = (LambdaExpression) parseExpression("(a, b) -> { return a * b }");

        Context context = new Context();
        LambdaObject lambda = new LambdaObject(
            lambdaAst.getParameters(),
            lambdaAst.getBody(),
            lambdaAst.getExpressionBody(),
            lambdaAst.isBlockBody(),
            new HashMap<>(),
            context,
            "(a, b) -> { return a * b }",
            true,
            null
        );

        Object result = lambda.invoke(new Object[]{3, 4});
        assertEquals(12, result);
    }

    @Test
    public void testLambdaToString() throws Exception {
        LambdaExpression lambdaAst = (LambdaExpression) parseExpression("(a, b) -> a + b");

        LambdaObject lambda = new LambdaObject(
            lambdaAst.getParameters(),
            lambdaAst.getBody(),
            lambdaAst.getExpressionBody(),
            lambdaAst.isBlockBody(),
            new HashMap<>(),
            new Context(),
            "(a, b) -> a + b",
            true,
            null
        );

        assertEquals("LambdaObject{params=2}", lambda.toString());
    }
}