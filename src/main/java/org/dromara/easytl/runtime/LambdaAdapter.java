package org.dromara.easytl.runtime;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Adapts a LambdaObject to a functional interface using dynamic proxy.
 *
 * <p>This class allows EasyTL Lambda expressions to be used with Java's
 * functional interfaces like Function, Predicate, Consumer, etc.</p>
 *
 * @author EasyTL Team
 */
public class LambdaAdapter {

    /**
     * Create a dynamic proxy that implements the target functional interface.
     *
     * @param lambda              the Lambda object to adapt
     * @param functionalInterface the functional interface to implement
     * @return a proxy instance implementing the functional interface
     * @throws IllegalArgumentException if the class is not a functional interface
     *                                  or if parameter counts don't match
     */
    public static Object adapt(LambdaObject lambda, Class<?> functionalInterface) {
        Method functionalMethod = FunctionalInterfaceUtils.getFunctionalMethod(functionalInterface);
        if (functionalMethod == null) {
            throw new IllegalArgumentException(
                "Class '" + functionalInterface.getName() + "' is not a functional interface");
        }

        if (!FunctionalInterfaceUtils.isParameterCountCompatible(lambda, functionalMethod)) {
            throw new IllegalArgumentException(
                "Lambda parameter count (" + lambda.getParameterCount() +
                ") does not match functional interface method parameter count (" +
                functionalMethod.getParameterCount() + ")");
        }

        InvocationHandler handler = new LambdaInvocationHandler(lambda, functionalMethod);

        return Proxy.newProxyInstance(
            functionalInterface.getClassLoader(),
            new Class<?>[] { functionalInterface },
            handler
        );
    }

    /**
     * InvocationHandler that delegates calls to a LambdaObject.
     */
    private static class LambdaInvocationHandler implements InvocationHandler {

        private final LambdaObject lambda;
        private final Method functionalMethod;

        LambdaInvocationHandler(LambdaObject lambda, Method functionalMethod) {
            this.lambda = lambda;
            this.functionalMethod = functionalMethod;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            // Handle Object methods (toString, hashCode, equals)
            if (method.getDeclaringClass() == Object.class) {
                String methodName = method.getName();
                if ("toString".equals(methodName)) {
                    return lambda.toString();
                } else if ("hashCode".equals(methodName)) {
                    return System.identityHashCode(lambda);
                } else if ("equals".equals(methodName)) {
                    return proxy == args[0];
                }
            }

            // Delegate to Lambda
            return lambda.invoke(args != null ? args : new Object[0]);
        }
    }
}