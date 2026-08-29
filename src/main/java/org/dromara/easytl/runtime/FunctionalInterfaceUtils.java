package org.dromara.easytl.runtime;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for detecting and working with functional interfaces.
 *
 * @author EasyTL Team
 */
public class FunctionalInterfaceUtils {

    /**
     * Check if a class is a functional interface.
     * A functional interface has exactly one abstract method.
     *
     * @param clazz the class to check
     * @return true if the class is a functional interface
     */
    public static boolean isFunctionalInterface(Class<?> clazz) {
        if (!clazz.isInterface()) {
            return false;
        }
        return getFunctionalMethod(clazz) != null;
    }

    /**
     * Get the single abstract method from a functional interface.
     *
     * @param clazz the interface class
     * @return the single abstract method, or null if not a functional interface
     */
    public static Method getFunctionalMethod(Class<?> clazz) {
        if (!clazz.isInterface()) {
            return null;
        }

        List<Method> abstractMethods = new ArrayList<>();

        for (Method method : clazz.getMethods()) {
            // Check if the method is abstract
            if (Modifier.isAbstract(method.getModifiers())) {
                // Exclude methods from Object class (like hashCode, equals, toString)
                // These are not counted for functional interface determination
                abstractMethods.add(method);
            }
        }

        // A functional interface has exactly one abstract method
        if (abstractMethods.size() == 1) {
            return abstractMethods.get(0);
        }

        return null;
    }

    /**
     * Check if a Lambda's parameter count matches the functional interface method.
     *
     * @param lambda  the Lambda object
     * @param method  the functional interface method
     * @return true if parameter counts match
     */
    public static boolean isParameterCountCompatible(LambdaObject lambda, Method method) {
        return lambda.getParameterCount() == method.getParameterCount();
    }
}