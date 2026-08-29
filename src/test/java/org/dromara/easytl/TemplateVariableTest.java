package org.dromara.easytl;

import org.dromara.easytl.exception.TemplateRuntimeException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TemplateVariable 单元测试
 */
@DisplayName("TemplateVariable Tests")
class TemplateVariableTest {

    @Nested
    @DisplayName("Basic Functionality Tests")
    class BasicFunctionalityTests {

        @Test
        @DisplayName("Simple TemplateVariable returns fixed value")
        void testSimpleTemplateVariable() {
            Context context = new Context();
            context.put("dynamicVar", ctx -> "Hello, World!");

            assertEquals("Hello, World!", context.get("dynamicVar"));
        }

        @Test
        @DisplayName("TemplateVariable accesses other variables in Context")
        void testAccessOtherVariables() {
            Context context = new Context();
            context.put("name", "张三");
            context.put("greeting", ctx -> "Hello, " + ctx.get("name"));

            assertEquals("Hello, 张三", context.get("greeting"));
        }

        @Test
        @DisplayName("TemplateVariable returns complex object")
        void testReturnComplexObject() {
            Context context = new Context();
            context.put("user", ctx -> {
                User user = new User();
                user.setName("李四");
                user.setAge(25);
                return user;
            });

            Object result = context.get("user");
            assertNotNull(result);
            assertTrue(result instanceof User);
            assertEquals("李四", ((User) result).getName());
            assertEquals(25, ((User) result).getAge());
        }

        @Test
        @DisplayName("TemplateVariable returns null")
        void testReturnNull() {
            Context context = new Context();
            context.put("nullVar", ctx -> null);

            assertNull(context.get("nullVar"));
        }
    }

    @Nested
    @DisplayName("Scope Tests")
    class ScopeTests {

        @Test
        @DisplayName("Child scope accesses parent's TemplateVariable")
        void testChildScopeAccessParentTemplateVariable() {
            Context parentContext = new Context();
            parentContext.put("name", "World");
            parentContext.put("greeting", ctx -> "Hello, " + ctx.get("name"));

            Context childContext = parentContext.createChildContext();

            assertEquals("Hello, World", childContext.get("greeting"));
        }

        @Test
        @DisplayName("Child scope overrides TemplateVariable")
        void testOverrideInChildScope() {
            Context parentContext = new Context();
            parentContext.put("name", "Parent");
            parentContext.put("greeting", ctx -> "Hello from " + ctx.get("name"));

            Context childContext = parentContext.createChildContext();
            childContext.put("greeting", "Static greeting");

            assertEquals("Static greeting", childContext.get("greeting"));
            assertEquals("Hello from Parent", parentContext.get("greeting"));
        }

        @Test
        @DisplayName("Child scope can add its own TemplateVariable")
        void testChildScopeAddOwnTemplateVariable() {
            Context parentContext = new Context();
            parentContext.put("name", "Parent");

            Context childContext = parentContext.createChildContext();
            childContext.put("childName", "Child");
            childContext.put("childGreeting", ctx ->
                "Hello from " + ctx.get("childName") + ", parent is " + ctx.get("name"));

            assertEquals("Hello from Child, parent is Parent", childContext.get("childGreeting"));
        }
    }

    @Nested
    @DisplayName("Exception Handling Tests")
    class ExceptionHandlingTests {

        @Test
        @DisplayName("TemplateVariable throws exception is wrapped in TemplateRuntimeException")
        void testExceptionHandling() {
            Context context = new Context();
            context.put("errorVar", ctx -> {
                throw new RuntimeException("Intentional error");
            });

            TemplateRuntimeException exception = assertThrows(
                TemplateRuntimeException.class,
                () -> context.get("errorVar")
            );

            assertTrue(exception.getMessage().contains("errorVar"));
            assertTrue(exception.getCause() instanceof RuntimeException);
            assertEquals("Intentional error", exception.getCause().getMessage());
        }

        @Test
        @DisplayName("Exception message includes variable name")
        void testExceptionMessageIncludesVariableName() {
            Context context = new Context();
            context.put("myDynamicVar", ctx -> {
                throw new IllegalStateException("Test error");
            });

            TemplateRuntimeException exception = assertThrows(
                TemplateRuntimeException.class,
                () -> context.get("myDynamicVar")
            );

            assertTrue(exception.getMessage().contains("myDynamicVar"));
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("getRaw returns TemplateVariable instance without executing")
        void testGetRawReturnsTemplateVariableInstance() {
            Context context = new Context();
            TemplateVariable dynamicVar = ctx -> "Dynamic Value";
            context.put("dynamicVar", dynamicVar);

            Object rawValue = context.getRaw("dynamicVar");
            assertSame(dynamicVar, rawValue);
        }

        @Test
        @DisplayName("Multiple accesses to same TemplateVariable execute each time")
        void testMultipleAccessExecuteEachTime() {
            AtomicInteger counter = new AtomicInteger(0);
            Context context = new Context();
            context.put("counter", ctx -> counter.incrementAndGet());

            assertEquals(1, context.get("counter"));
            assertEquals(2, context.get("counter"));
            assertEquals(3, context.get("counter"));
        }

        @Test
        @DisplayName("Nested TemplateVariable calls")
        void testNestedTemplateVariableCalls() {
            Context context = new Context();
            context.put("inner", ctx -> "Inner Value");
            context.put("outer", ctx -> "Outer: " + ctx.get("inner"));

            assertEquals("Outer: Inner Value", context.get("outer"));
        }

        @Test
        @DisplayName("containsRaw returns true for TemplateVariable without executing")
        void testContainsRawForTemplateVariable() {
            Context context = new Context();
            context.put("dynamicVar", ctx -> {
                fail("TemplateVariable should not be executed by containsRaw");
                return "Value";
            });

            assertTrue(context.containsRaw("dynamicVar"));
            assertTrue(context.contains("dynamicVar"));
        }

        @Test
        @DisplayName("Non-existent variable returns null")
        void testNonExistentVariableReturnsNull() {
            Context context = new Context();

            assertNull(context.get("nonExistent"));
            assertNull(context.getRaw("nonExistent"));
            assertFalse(context.contains("nonExistent"));
            assertFalse(context.containsRaw("nonExistent"));
        }
    }

    @Nested
    @DisplayName("Thread Safety Tests")
    class ThreadSafetyTests {

        @Test
        @DisplayName("Concurrent access to Context with TemplateVariable")
        void testConcurrentAccess() throws InterruptedException {
            Context context = new Context();
            AtomicInteger executionCount = new AtomicInteger(0);

            context.put("dynamicVar", ctx -> {
                executionCount.incrementAndGet();
                return Thread.currentThread().getName();
            });

            int threadCount = 10;
            Thread[] threads = new Thread[threadCount];

            for (int i = 0; i < threadCount; i++) {
                threads[i] = new Thread(() -> {
                    for (int j = 0; j < 100; j++) {
                        Object value = context.get("dynamicVar");
                        assertNotNull(value);
                    }
                });
            }

            for (Thread thread : threads) {
                thread.start();
            }

            for (Thread thread : threads) {
                thread.join();
            }

            // Each access should have executed the TemplateVariable
            assertEquals(threadCount * 100, executionCount.get());
        }

        @Test
        @DisplayName("Concurrent put and get operations")
        void testConcurrentPutAndGet() throws InterruptedException {
            Context context = new Context();
            int threadCount = 10;
            Thread[] threads = new Thread[threadCount];

            for (int i = 0; i < threadCount; i++) {
                final int index = i;
                threads[i] = new Thread(() -> {
                    for (int j = 0; j < 100; j++) {
                        String key = "key-" + index + "-" + j;
                        context.put(key, "value-" + j);
                        assertEquals("value-" + j, context.get(key));
                    }
                });
            }

            for (Thread thread : threads) {
                thread.start();
            }

            for (Thread thread : threads) {
                thread.join();
            }
        }
    }

    /**
     * Test helper class
     */
    static class User {
        private String name;
        private int age;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }
    }
}