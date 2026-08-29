package org.dromara.easytl.exception;

import org.dromara.easytl.Context;
import org.dromara.easytl.Template;
import org.dromara.easytl.TemplateEngine;
import org.dromara.easytl.testutil.ErrorPointerAssert;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-end tests for TemplateRuntimeException error formatting
 *
 * @author gongjun
 * @since 1.0.0
 */
class RuntimeExceptionFormattingTest {

    private final TemplateEngine engine = new TemplateEngine();

    // ==================== Null Access Errors ====================

    @Test
    void testMemberAccessOfNull_ShowsPosition() {
        String template = "Hello {user.name}!";
        Template compiled = engine.compile(template);

        TemplateRuntimeException ex = assertThrows(
                TemplateRuntimeException.class,
                () -> compiled.render(new Context())
        );

        ErrorPointerAssert.assertErrorPointer(ex,
                "Hello {user.name}!",
                "            ^^^^",
                "Cannot access member 'name' of null"
        );
    }

    @Test
    void testIndexAccessOfNull_ShowsPosition() {
        String template = "Value: {items[0]}";
        Template compiled = engine.compile(template);

        TemplateRuntimeException ex = assertThrows(
                TemplateRuntimeException.class,
                () -> compiled.render(new Context())
        );

        ErrorPointerAssert.assertErrorPointer(ex,
                "Value: {items[0]}",
                "        ^^^^^",
                "Cannot access index of null"
        );
    }

    @Test
    void testMethodCallOnNull_ShowsPosition() {
        String template = "Result: {obj.toString()}";
        Template compiled = engine.compile(template);

        TemplateRuntimeException ex = assertThrows(
                TemplateRuntimeException.class,
                () -> compiled.render(new Context())
        );

        ErrorPointerAssert.assertErrorPointer(ex,
                "Result: {obj.toString()}",
                "             ^^^^^^^^",
                "Cannot call method 'toString' on null"
        );
    }

    // ==================== Multi-line Template Errors ====================

    @Test
    void testErrorOnSecondLine_ShowsCorrectLine() {
        String template = "Line 1\nLine 2: {user.name}\nLine 3";
        Template compiled = engine.compile(template);

        TemplateRuntimeException ex = assertThrows(
                TemplateRuntimeException.class,
                () -> compiled.render(new Context())
        );

        ErrorPointerAssert.assertErrorPointer(ex,
                "Line 2: {user.name}",
                "              ^^^^",
                "Cannot access member 'name' of null"
        );
    }

    @Test
    void testErrorOnThirdLine_ShowsCorrectLine() {
        String template = "Line 1\nLine 2\nLine 3: {obj.method()}";
        Template compiled = engine.compile(template);

        TemplateRuntimeException ex = assertThrows(
                TemplateRuntimeException.class,
                () -> compiled.render(new Context())
        );

        ErrorPointerAssert.assertErrorPointer(ex,
                "Line 3: {obj.method()}",
                "             ^^^^^^",
                "Cannot call method 'method' on null"
        );
    }

    // ==================== For Loop Errors ====================

    @Test
    void testForLoopOverNull_ShowsPosition() {
        String template = "{% for item in items }{item}{/% for }";
        Template compiled = engine.compile(template);

        TemplateRuntimeException ex = assertThrows(
                TemplateRuntimeException.class,
                () -> compiled.render(new Context())
        );

        assertTrue(ex.getMessage().contains("Cannot iterate") || ex.getMessage().contains("null"),
                "Error message should describe iteration error: " + ex.getMessage());
    }

    @Test
    void testForLoopOverInvalidType_ShowsPosition() {
        String template = "{% for item in number }{/% for }";
        Template compiled = engine.compile(template);

        Context context = new Context();
        context.put("number", 42);

        TemplateRuntimeException ex = assertThrows(
                TemplateRuntimeException.class,
                () -> compiled.render(context)
        );

        assertTrue(ex.getMessage().contains("Cannot iterate") || ex.getMessage().contains("Integer"),
                "Error message should describe type error: " + ex.getMessage());
    }

    // ==================== Argument Access Errors ====================

    @Test
    void testArgumentIndexOutOfBounds_ShowsPosition() {
        // Test with {2} syntax (single brace) like in EasyTLTest
        String template = "First: {0}, Second: {1}, Third: {2}";
        Template compiled = engine.compile(template);

        Context context = new Context();
        context.setArguments("one", "two");

        ArgumentIndexOutOfBoundsException ex = assertThrows(
                ArgumentIndexOutOfBoundsException.class,
                () -> compiled.render(context)
        );

        assertTrue(ex.getMessage().contains("index") || ex.getMessage().contains("bounds"),
                "Error message should describe bounds error: " + ex.getMessage());
        assertEquals(2, ex.getIndex());
    }

    @Test
    void testArgumentNullValue_ShowsPosition() {
        String template = "Value: {$0}";
        Template compiled = engine.compile(template);

        Context context = new Context();
        context.setArguments(new Object[]{null});

        ArgumentIndexOutOfBoundsException ex = assertThrows(
                ArgumentIndexOutOfBoundsException.class,
                () -> compiled.render(context)
        );

        assertTrue(ex.getMessage().contains("null"),
                "Error message should describe null value: " + ex.getMessage());
        assertEquals(0, ex.getIndex());
    }

    // ==================== Statement Errors ====================

    @Test
    void testForStatementOverNull_ShowsPosition() {
        String template = "Start{% for item in items }{item}{/% for }";
        Template compiled = engine.compile(template);

        TemplateRuntimeException ex = assertThrows(
                TemplateRuntimeException.class,
                () -> compiled.render(new Context())
        );

        assertTrue(ex.getMessage().contains("Cannot iterate"),
                "Error message should describe iteration error: " + ex.getMessage());
    }

    // ==================== Complex Expression Errors ====================

    @Test
    void testChainedMemberAccessError_ShowsPosition() {
        String template = "{user.address.city.name}";
        Template compiled = engine.compile(template);

        Context context = new Context();
        context.put("user", new Object()); // user exists but has no address property

        TemplateRuntimeException ex = assertThrows(
                TemplateRuntimeException.class,
                () -> compiled.render(context));

        ErrorPointerAssert.assertErrorPointer(ex,
                "{user.address.city.name}",
                "      ^^^^^^^",
                "address"
        );
    }

    @Test
    void testErrorWithSafeNavigation_NoError() {
        String template = "Hello {user?.name}!";
        Template compiled = engine.compile(template);

        // Safe navigation should not throw error when user is null
        String result = compiled.render(new Context());
        assertEquals("Hello !", result);
    }

    // ==================== Error Message Format Tests ====================

    @Test
    void testErrorMessageFormat_ContainsAllParts() {
        String template = "Hello {user.name}!";
        Template compiled = engine.compile(template);

        TemplateRuntimeException ex = assertThrows(
                TemplateRuntimeException.class,
                () -> compiled.render(new Context())
        );

        String message = ex.getMessage();

        // Should contain error type and pointer
        assertTrue(message.contains("Runtime error"), "Should indicate Runtime error: " + message);
        assertTrue(message.contains("^"), "Should have pointer: " + message);
    }

    @Test
    void testErrorMessage_WithSimplePosition() {
        // Test error without source text (using older constructor)
        TemplateRuntimeException ex = new TemplateRuntimeException("Test error", 5, 10, 5, 20);

        assertTrue(ex.getMessage().contains("line:5"));
        assertTrue(ex.getMessage().contains("column:10"));
        assertEquals(5, ex.getStartLine());
        assertEquals(10, ex.getStartColumn());
        assertEquals(5, ex.getEndLine());
        assertEquals(20, ex.getEndColumn());
    }

    // ==================== Edge Cases ====================

    @Test
    void testErrorAtStartOfLine() {
        String template = "{user.name}";
        Template compiled = engine.compile(template);

        TemplateRuntimeException ex = assertThrows(
                TemplateRuntimeException.class,
                () -> compiled.render(new Context())
        );

        ErrorPointerAssert.assertErrorPointer(ex,
                "{user.name}",
                "      ^^^^",
                "Cannot access member 'name' of null"
        );
    }

    @Test
    void testErrorAtEndOfLine() {
        String template = "Hello {user.name}";
        Template compiled = engine.compile(template);

        TemplateRuntimeException ex = assertThrows(
                TemplateRuntimeException.class,
                () -> compiled.render(new Context())
        );

        ErrorPointerAssert.assertErrorPointer(ex,
                "Hello {user.name}",
                "            ^^^^",
                "Cannot access member 'name' of null"
        );
    }

    @Test
    void testErrorInNestedExpression() {
        String template = "{items[0].name}";
        Template compiled = engine.compile(template);

        TemplateRuntimeException ex = assertThrows(
                TemplateRuntimeException.class,
                () -> compiled.render(new Context())
        );

        // Pointer should point to 'items' (object), not the index expression
        ErrorPointerAssert.assertErrorPointer(ex,
                "{items[0].name}",
                " ^^^^^",
                "Cannot access index of null"
        );
    }

    // ==================== Position Range Tests ====================

    @Test
    void testPositionRange_WithSourceText() {
        String sourceText = "Hello {user.name}!";
        TemplateRuntimeException ex = new TemplateRuntimeException(
                "Test error", 1, 8, 1, 18, sourceText);

        assertEquals(1, ex.getStartLine());
        assertEquals(8, ex.getStartColumn());
        assertEquals(1, ex.getEndLine());
        assertEquals(18, ex.getEndColumn());
        assertEquals(sourceText, ex.getSourceText());

        // Should have Rust-style formatting
        String message = ex.getMessage();
        assertTrue(message.contains(" |"));
        assertTrue(message.contains("^"));
    }

    @Test
    void testPositionRange_WithPosition() {
        org.dromara.easytl.ast.Position position = new org.dromara.easytl.ast.Position(2, 5, 2, 15);
        TemplateRuntimeException ex = new TemplateRuntimeException("Test error", position, "source code");

        assertEquals(2, ex.getStartLine());
        assertEquals(5, ex.getStartColumn());
        assertEquals(2, ex.getEndLine());
        assertEquals(15, ex.getEndColumn());
    }

    // ==================== Null Pointer Property Access Caret Position ====================

    /**
     * Test: Null pointer property access error should point to property name only
     * Template: "Hello {user.name}!"
     * Expected caret position: columns 13-17 (pointing to 'name')
     */
    @Test
    void testNullPointerPropertyAccess_PointsToPropertyNameOnly() {
        String template = "Hello {user.name}!";
        Template compiled = engine.compile(template);

        TemplateRuntimeException ex = assertThrows(
                TemplateRuntimeException.class,
                () -> compiled.render(new Context())
        );

        String message = ex.getMessage();

        // Verify caret is present
        assertTrue(message.contains("^"),
                "Error message should have caret: " + message);

        // Verify position points to 'name' (columns 13-17)
        assertEquals(13, ex.getStartColumn(),
                "Start column should be 13 (start of 'name'): " + message);
        assertEquals(17, ex.getEndColumn(),
                "End column should be 17 (end of 'name', exclusive): " + message);

        // Verify error line is correct
        assertEquals(1, ex.getStartLine());
        assertEquals(1, ex.getEndLine());
    }

    // ==================== Pointer Position Tests ====================

    /**
     * Test: {a.xxx} where a exists but property xxx doesn't exist
     * Pointer should point to 'xxx'
     */
    @Test
    void testMemberAccessPropertyNotExist_PointsToProperty() {
        String template = "Value: {a.xxx}";
        Template compiled = engine.compile(template);

        Context context = new Context();
        context.put("a", new Object()); // a exists but has no 'xxx' property

        TemplateRuntimeException ex = assertThrows(
                TemplateRuntimeException.class,
                () -> compiled.render(context)
        );

        ErrorPointerAssert.assertErrorPointer(ex,
                "Value: {a.xxx}",
                "          ^^^",
                "xxx"
        );
    }

    /**
     * Test: Calling a non-existent method on an object
     * Pointer should point to the method name
     */
    @Test
    void testMethodCallNotExist_PointsToMethodName() {
        String template = "Value: {obj.nonExistentMethod()}";
        Template compiled = engine.compile(template);

        Context context = new Context();
        context.put("obj", new Object());

        TemplateRuntimeException ex = assertThrows(
                TemplateRuntimeException.class,
                () -> compiled.render(context)
        );

        ErrorPointerAssert.assertErrorPointer(ex,
                "Value: {obj.nonExistentMethod()}",
                "            ^^^^^^^^^^^^^^^^^",
                "nonExistentMethod"
        );
    }

    /**
     * Test: Method call with arguments where one argument doesn't exist
     * Pointer should point to the method call
     */
    @Test
    void testMethodCallWithMissingArgument_PointsToArgument() {
        String template = "Value: {obj.method(missingArg)}";
        Template compiled = engine.compile(template);

        Context context = new Context();
        context.put("obj", new Object());

        TemplateRuntimeException ex = assertThrows(
                TemplateRuntimeException.class,
                () -> compiled.render(context)
        );

        ErrorPointerAssert.assertErrorPointer(ex,
                "Value: {obj.method(missingArg)}",
                "            ^^^^^^",
                "method"
        );
    }

    /**
     * Test: Array index access on a valid array with out-of-bounds index
     * Pointer should point to the array access expression
     */
    @Test
    void testArrayIndexOutOfBounds_PointsToIndex() {
        String template = "Value: {arr[10]}";
        Template compiled = engine.compile(template);

        Context context = new Context();
        context.put("arr", new String[]{"a", "b"}); // only 2 elements

        TemplateRuntimeException ex = assertThrows(
                TemplateRuntimeException.class,
                () -> compiled.render(context)
        );

ErrorPointerAssert.assertErrorPointer(ex,
                "Value: {arr[10]}",
                "            ^^",
                "out of bounds"
        );
    }

    /**
     * Test: Empty list index access - pointer should point to the index
     */
    @Test
    void testEmptyListIndexAccess_PointsToIndex() {
        String template = "Value: {array[0]}";
        Template compiled = engine.compile(template);

        Context context = new Context();
        context.put("array", new java.util.ArrayList<>()); // empty list

        TemplateRuntimeException ex = assertThrows(
                TemplateRuntimeException.class,
                () -> compiled.render(context)
        );

        ErrorPointerAssert.assertErrorPointer(ex,
                "Value: {array[0]}",
                "              ^",
                "out of bounds"
        );
    }

    /**
     * Test: Non-indexable type - pointer should point to the object
     */
    @Test
    void testNonIndexableType_PointsToObject() {
        String template = "Value: {num[0]}";
        Template compiled = engine.compile(template);

        Context context = new Context();
        context.put("num", 42); // Integer is not indexable

        TemplateRuntimeException ex = assertThrows(
                TemplateRuntimeException.class,
                () -> compiled.render(context)
        );

        ErrorPointerAssert.assertErrorPointer(ex,
                "Value: {num[0]}",
                "        ^^^",
                "Cannot access index"
        );
    }

    /**
     * Test: Null-safe access where inner expression is null
     * Should NOT throw error with safe navigation
     */
    @Test
    void testSafeNavigationOnNull_NoError() {
        String template = "Value: {user?.name}";
        Template compiled = engine.compile(template);

        Context context = new Context();
        // user is null, but safe navigation should prevent error

        String result = compiled.render(context);
        assertEquals("Value: ", result);
    }

    /**
     * Test: Unary operator on null
     * Pointer should point to the member access expression
     */
    @Test
    void testUnaryOperatorOnNull_PointsToExpression() {
        String template = "Value: {-user.name}";
        Template compiled = engine.compile(template);

        Context context = new Context();
        // user is null, accessing .name will fail

        TemplateRuntimeException ex = assertThrows(
                TemplateRuntimeException.class,
                () -> compiled.render(context)
        );

        ErrorPointerAssert.assertErrorPointer(ex,
                "Value: {-user.name}",
                "              ^^^^",
                "Cannot access member 'name' of null"
        );
    }

    /**
     * Test: Ternary operator with error in condition
     * Pointer should point to the condition
     */
    @Test
    void testTernaryConditionError_PointsToCondition() {
        // Test ternary where true branch has error
        String template = "Value: {true ? obj.name : 'default'}";
        Template compiled = engine.compile(template);

        Context context = new Context();
        // obj is null, accessing .name on null should throw

        TemplateRuntimeException ex = assertThrows(
                TemplateRuntimeException.class,
                () -> compiled.render(context)
        );

        ErrorPointerAssert.assertErrorPointer(ex,
                "Value: {true ? obj.name : 'default'}",
                "                   ^^^^",
                "Cannot access member 'name' of null"
        );
    }

    /**
     * Test: Chained member access - error at end of chain
     * Pointer should point to the last member accessed
     */
    @Test
    void testChainedMemberError_PointsToLastMember() {
        String template = "Value: {user.profile.address.city}";
        Template compiled = engine.compile(template);

        Context context = new Context();
        // user exists, profile exists, but address is null
        context.put("user", new TestUser());

        TemplateRuntimeException ex = assertThrows(
                TemplateRuntimeException.class,
                () -> compiled.render(context)
        );

        ErrorPointerAssert.assertErrorPointer(ex,
                "Value: {user.profile.address.city}",
                "                             ^^^^",
                "Cannot access member 'city' of null"
        );
    }

    /**
     * Test: Index access with invalid index (negative out of range)
     * Pointer should point to the array access expression
     */
    @Test
    void testNegativeIndex_PointsToIndex() {
        String template = "Value: {arr[-3]}";
        Template compiled = engine.compile(template);

        Context context = new Context();
        context.put("arr", new String[]{"a", "b"});

        TemplateRuntimeException ex = assertThrows(
                TemplateRuntimeException.class,
                () -> compiled.render(context)
        );

        ErrorPointerAssert.assertErrorPointer(ex,
                "Value: {arr[-3]}",
                "            ^^",
                "out of bounds"
        );
    }

    /**
     * Test: Method call with wrong number of arguments
     * Note: Math is not defined in default context, so error is "Cannot call method on null"
     */
    @Test
    void testMethodWrongArgCount_PointsToMethod() {
        String template = "Value: {Math.max(1)}";
        Template compiled = engine.compile(template);

        TemplateRuntimeException ex = assertThrows(
                TemplateRuntimeException.class,
                () -> compiled.render(new Context())
        );

        ErrorPointerAssert.assertErrorPointer(ex,
                "Value: {Math.max(1)}",
                "             ^^^",
                "Cannot call method 'max' on null"
        );
    }

    /**
     * Test: Access property on wrong type (e.g., calling method as property)
     * Pointer should point to the property name
     */
    @Test
    void testAccessMethodAsProperty_PointsToMethod() {
        String template = "Value: {obj.toString}";
        Template compiled = engine.compile(template);

        Context context = new Context();
        context.put("obj", new Object());

        TemplateRuntimeException ex = assertThrows(
                TemplateRuntimeException.class,
                () -> compiled.render(context)
        );

        ErrorPointerAssert.assertErrorPointer(ex,
                "Value: {obj.toString}",
                "            ^^^^^^^^",
                "toString"
        );
    }

    /**
     * Test: Chained member access null pointer error
     * When user.address is null, accessing .city should point to 'city'
     */
    @Test
    void testChainedMemberAccessNullPointer_PointsToLastProperty() {
        String template = "{user.address.city}";
        Template compiled = engine.compile(template);

        Context context = new Context();
        context.put("user", new TestUser()); // user exists, address is null

        TemplateRuntimeException ex = assertThrows(
                TemplateRuntimeException.class,
                () -> compiled.render(context)
        );

        // Verify caret points to 'city' property
        assertTrue(ex.getMessage().contains("^"),
                "Error message should have caret: " + ex.getMessage());

        // 'city' starts at column 15 (after user.address.)
        assertEquals(15, ex.getStartColumn(),
                "Start column should be at 'city': " + ex.getMessage());
    }

    /**
     * Helper class for testing
     */
    private static class TestUser {
        public TestProfile getProfile() {
            return new TestProfile();
        }

        public TestAddress getAddress() {
            return null; // returns null to trigger error on .city
        }
    }

    private static class TestProfile {
        public TestAddress getAddress() {
            return null; // returns null to trigger error on .city
        }
    }

    private static class TestAddress {
        public String getCity() {
            return "NYC";
        }
    }
}