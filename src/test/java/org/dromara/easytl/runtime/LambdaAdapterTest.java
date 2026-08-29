package org.dromara.easytl.runtime;

import org.dromara.easytl.Context;
import org.dromara.easytl.ast.*;
import org.dromara.easytl.parser.*;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.*;
import java.util.function.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for LambdaAdapter and FunctionalInterfaceUtils.
 *
 * @author EasyTL Team
 */
public class LambdaAdapterTest {

    private ASTNode parseExpression(String input) throws Exception {
        StringSource source = new StringSource("{" + input + "}");
        Lexer lexer = new Lexer(source);
        TokenStream tokens = new TokenStream(lexer);
        Parser parser = new Parser(tokens);
        TemplateNode template = parser.parse();
        ASTNode child = template.getChildren().get(0);
        if (child instanceof SingleBraceExpressionNode) {
            return ((SingleBraceExpressionNode) child).getExpression();
        }
        throw new RuntimeException("Expected SingleBraceExpressionNode");
    }

    private LambdaObject createLambda(String expr) throws Exception {
        LambdaExpression lambdaAst = (LambdaExpression) parseExpression(expr);
        return new LambdaObject(
            lambdaAst.getParameters(),
            lambdaAst.getBody(),
            lambdaAst.getExpressionBody(),
            lambdaAst.isBlockBody(),
            new HashMap<>(),
            new Context(),
            expr,
            true,
            null
        );
    }

    @Test
    public void testIsFunctionalInterface() {
        assertTrue(FunctionalInterfaceUtils.isFunctionalInterface(Function.class));
        assertTrue(FunctionalInterfaceUtils.isFunctionalInterface(Predicate.class));
        assertTrue(FunctionalInterfaceUtils.isFunctionalInterface(Consumer.class));
        assertTrue(FunctionalInterfaceUtils.isFunctionalInterface(BiFunction.class));
        assertTrue(FunctionalInterfaceUtils.isFunctionalInterface(Runnable.class));

        // Not a functional interface
        assertFalse(FunctionalInterfaceUtils.isFunctionalInterface(String.class));
        assertFalse(FunctionalInterfaceUtils.isFunctionalInterface(List.class));
        assertFalse(FunctionalInterfaceUtils.isFunctionalInterface(Object.class));
    }

    @Test
    public void testGetFunctionalMethod() {
        Method functionMethod = FunctionalInterfaceUtils.getFunctionalMethod(Function.class);
        assertNotNull(functionMethod);
        assertEquals("apply", functionMethod.getName());

        Method predicateMethod = FunctionalInterfaceUtils.getFunctionalMethod(Predicate.class);
        assertNotNull(predicateMethod);
        assertEquals("test", predicateMethod.getName());

        Method biFunctionMethod = FunctionalInterfaceUtils.getFunctionalMethod(BiFunction.class);
        assertNotNull(biFunctionMethod);
        assertEquals(2, biFunctionMethod.getParameterCount());
    }

    @Test
    public void testAdaptToFunction() throws Exception {
        LambdaObject lambda = createLambda("x -> x * 2");

        Object adapted = LambdaAdapter.adapt(lambda, Function.class);
        assertTrue(adapted instanceof Function);

        @SuppressWarnings("unchecked")
        Function<Integer, Integer> func = (Function<Integer, Integer>) adapted;
        assertEquals(10, func.apply(5).intValue());
        assertEquals(20, func.apply(10).intValue());
    }

    @Test
    public void testAdaptToPredicate() throws Exception {
        LambdaObject lambda = createLambda("x -> x > 5");

        Object adapted = LambdaAdapter.adapt(lambda, Predicate.class);
        assertTrue(adapted instanceof Predicate);

        @SuppressWarnings("unchecked")
        Predicate<Integer> pred = (Predicate<Integer>) adapted;
        assertTrue(pred.test(10));
        assertFalse(pred.test(3));
    }

    @Test
    public void testAdaptToBiFunction() throws Exception {
        LambdaObject lambda = createLambda("(a, b) -> a + b");

        Object adapted = LambdaAdapter.adapt(lambda, BiFunction.class);
        assertTrue(adapted instanceof BiFunction);

        @SuppressWarnings("unchecked")
        BiFunction<Integer, Integer, Integer> bifunc = (BiFunction<Integer, Integer, Integer>) adapted;
        assertEquals(7, bifunc.apply(3, 4).intValue());
        assertEquals(15, bifunc.apply(10, 5).intValue());
    }

    @Test
    public void testAdaptToConsumer() throws Exception {
        LambdaObject lambda = createLambda("x -> x + 1");

        Object adapted = LambdaAdapter.adapt(lambda, Consumer.class);
        assertTrue(adapted instanceof Consumer);
    }

    @Test
    public void testAdaptToSupplier() throws Exception {
        LambdaObject lambda = createLambda("() -> 42");

        Object adapted = LambdaAdapter.adapt(lambda, Supplier.class);
        assertTrue(adapted instanceof Supplier);

        @SuppressWarnings("unchecked")
        Supplier<Integer> supplier = (Supplier<Integer>) adapted;
        assertEquals(42, supplier.get().intValue());
    }

    @Test
    public void testNonFunctionalInterfaceRejected() throws Exception {
        LambdaObject lambda = createLambda("x -> x");

        try {
            LambdaAdapter.adapt(lambda, String.class);
            fail("Should reject non-functional interface");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("not a functional interface"));
        }
    }

    @Test
    public void testParameterCountMismatch() throws Exception {
        LambdaObject lambda = createLambda("x -> x");

        try {
            LambdaAdapter.adapt(lambda, BiFunction.class);
            fail("Should reject parameter count mismatch");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("parameter count"));
        }
    }

    @Test
    public void testToString() throws Exception {
        LambdaObject lambda = createLambda("x -> x");
        Object adapted = LambdaAdapter.adapt(lambda, Function.class);

        assertEquals("LambdaObject{params=1}", adapted.toString());
    }
}