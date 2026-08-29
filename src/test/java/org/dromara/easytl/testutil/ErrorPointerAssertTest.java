package org.dromara.easytl.testutil;

import org.dromara.easytl.exception.TemplateRuntimeException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ErrorPointerAssert 工具类测试
 */
class ErrorPointerAssertTest {

    @Test
    void testAssertErrorPointer_Success() {
        // 创建一个带有完整位置信息的异常
        String sourceText = "Hello {user.name}!";
        TemplateRuntimeException ex = new TemplateRuntimeException(
            "Cannot access property 'name' of null",
            1, 13, 1, 17, sourceText
        );

        // 验证成功的情况
        ErrorPointerAssert.assertErrorPointer(ex,
            "Hello {user.name}!",
            "            ^^^^",
            "Cannot access property 'name' of null"
        );
    }

    @Test
    void testAssertErrorPointer_PositionMismatch() {
        String sourceText = "Hello {user.name}!";
        TemplateRuntimeException ex = new TemplateRuntimeException(
            "Cannot access property 'name' of null",
            1, 8, 1, 18, sourceText  // 位置不匹配
        );

        // 应抛出 AssertionError
        AssertionError error = assertThrows(AssertionError.class, () -> {
            ErrorPointerAssert.assertErrorPointer(ex,
                "Hello {user.name}!",
                "            ^^^^",
                "Cannot access property 'name' of null"
            );
        });

        // 验证错误消息包含 Expected 和 Actual
        assertTrue(error.getMessage().contains("Expected"),
            "Error message should contain 'Expected': " + error.getMessage());
        assertTrue(error.getMessage().contains("Actual"),
            "Error message should contain 'Actual': " + error.getMessage());
    }

    @Test
    void testAssertErrorPointer_MissingSourceText() {
        // 创建没有源码的异常
        TemplateRuntimeException ex = new TemplateRuntimeException(
            "Test error", 1, 8, 1, 18
        );

        AssertionError error = assertThrows(AssertionError.class, () -> {
            ErrorPointerAssert.assertErrorPointer(ex,
                "Hello {user.name}!",
                "            ^^^^",
                "Test error"
            );
        });

        assertTrue(error.getMessage().contains("Missing source text"),
            "Error message should contain 'Missing source text': " + error.getMessage());
    }

    @Test
    void testAssertErrorPointer_MissingPosition() {
        // 创建没有位置的异常
        TemplateRuntimeException ex = new TemplateRuntimeException("Test error");

        AssertionError error = assertThrows(AssertionError.class, () -> {
            ErrorPointerAssert.assertErrorPointer(ex,
                "Hello {user.name}!",
                "            ^^^^",
                "Test error"
            );
        });

        assertTrue(error.getMessage().contains("Missing position"),
            "Error message should contain 'Missing position': " + error.getMessage());
    }

    @Test
    void testAssertErrorPointer_MessageMismatch() {
        String sourceText = "Hello {user.name}!";
        TemplateRuntimeException ex = new TemplateRuntimeException(
            "Cannot access property 'name' of null",
            1, 13, 1, 17, sourceText
        );

        AssertionError error = assertThrows(AssertionError.class, () -> {
            ErrorPointerAssert.assertErrorPointer(ex,
                "Hello {user.name}!",
                "            ^^^^",
                "Different error message"
            );
        });

        assertTrue(error.getMessage().contains("Message mismatch"),
            "Error message should contain 'Message mismatch': " + error.getMessage());
    }
}