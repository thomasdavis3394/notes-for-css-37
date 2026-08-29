package org.dromara.easytl.exception;

import org.dromara.easytl.ast.Position;
import org.dromara.easytl.testutil.ErrorPointerAssert;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 异常类测试
 *
 * @author gongjun
 * @since 1.0.0
 */
class TemplateExceptionTest {

    @Test
    void testTemplateException() {
        final TemplateException ex1 = new TemplateException("测试异常");
        assertEquals("测试异常", ex1.getMessage());

        final Exception cause = new RuntimeException("原因");
        final TemplateException ex2 = new TemplateException("测试异常", cause);
        assertEquals("测试异常", ex2.getMessage());
        assertEquals(cause, ex2.getCause());

        final TemplateException ex3 = new TemplateException(cause);
        assertEquals(cause, ex3.getCause());
    }

    @Test
    void testTemplateSyntaxException() {
        final TemplateSyntaxException ex1 = new TemplateSyntaxException("语法错误");
        assertTrue(ex1.getMessage().contains("语法错误"));
        assertEquals(-1, ex1.getLine());
        assertEquals(-1, ex1.getColumn());

        final TemplateSyntaxException ex2 = new TemplateSyntaxException("缺少右括号", 10, 25);
        assertTrue(ex2.getMessage().contains("Syntax error"));
        assertTrue(ex2.getMessage().contains("line:10"));
        assertTrue(ex2.getMessage().contains("column:25"));
        assertEquals(10, ex2.getLine());
        assertEquals(25, ex2.getColumn());
    }

    @Test
    void testTemplateRuntimeException() {
        final TemplateRuntimeException ex1 = new TemplateRuntimeException("运行时错误");
        assertEquals("运行时错误", ex1.getMessage());

        final Exception cause = new NullPointerException();
        final TemplateRuntimeException ex2 = new TemplateRuntimeException("运行时错误", cause);
        assertEquals("运行时错误", ex2.getMessage());
        assertEquals(cause, ex2.getCause());

        final TemplateRuntimeException ex3 = new TemplateRuntimeException(cause);
        assertEquals(cause, ex3.getCause());
    }

    @Test
    void testTemplateRuntimeExceptionWithPosition() {
        // Test with position only (no source text)
        final TemplateRuntimeException ex1 = new TemplateRuntimeException("运行时错误", 5, 10, 5, 20);
        assertTrue(ex1.getMessage().contains("Runtime error"));
        assertTrue(ex1.getMessage().contains("line:5"));
        assertTrue(ex1.getMessage().contains("column:10"));
        assertEquals(5, ex1.getStartLine());
        assertEquals(10, ex1.getStartColumn());
        assertEquals(5, ex1.getEndLine());
        assertEquals(20, ex1.getEndColumn());
        assertNull(ex1.getSourceText());

        // Test with position and source text (Rust-style formatting)
        final String sourceText = "Hello {user.name}!\nWorld {item}";
        final TemplateRuntimeException ex2 = new TemplateRuntimeException(
                "Cannot access property 'name' of null", 1, 8, 1, 18, sourceText);

        // Use ErrorPointerAssert for strict pointer validation
        // Line 1: "Hello {user.name}!"
        // Columns 8-17 (startColumn=8, endColumn=18, length=10) point to "user.name"
        ErrorPointerAssert.assertErrorPointer(ex2,
                "Hello {user.name}!",
                "       ^^^^^^^^^^",
                "Cannot access property 'name' of null"
        );
        assertEquals(1, ex2.getStartLine());
        assertEquals(8, ex2.getStartColumn());
        assertEquals(1, ex2.getEndLine());
        assertEquals(18, ex2.getEndColumn());
        assertEquals(sourceText, ex2.getSourceText());

        // Test with Position object
        final Position position = new Position(3, 5, 3, 15);
        final TemplateRuntimeException ex3 = new TemplateRuntimeException("Test error", position);
        assertTrue(ex3.getMessage().contains("Runtime error"));
        assertTrue(ex3.getMessage().contains("line:3"));
        assertEquals(3, ex3.getStartLine());
        assertEquals(5, ex3.getStartColumn());
        assertEquals(3, ex3.getEndLine());
        assertEquals(15, ex3.getEndColumn());
    }

    @Test
    void testTemplateRuntimeExceptionRustStyle() {
        // Test Rust-style error message formatting
        final String sourceText = "line1\nline2\nHello {user.name}\nline4";
        final TemplateRuntimeException ex = new TemplateRuntimeException(
                "Cannot access property 'name' of null", 3, 8, 3, 18, sourceText);

        // Use ErrorPointerAssert for strict pointer validation
        // Line 3: "Hello {user.name}"
        // Columns 8-17 (startColumn=8, endColumn=18, length=10) point to "user.name"
        ErrorPointerAssert.assertErrorPointer(ex,
                "Hello {user.name}",
                "       ^^^^^^^^^^",
                "Cannot access property 'name' of null"
        );
    }

    @Test
    void testTemplateRuntimeExceptionWithCause() {
        final Exception cause = new NullPointerException("null value");
        final TemplateRuntimeException ex = new TemplateRuntimeException(
                "Test error", cause, 5, 10, 5, 20, "source code");

        assertTrue(ex.getMessage().contains("Runtime error"));
        assertEquals(cause, ex.getCause());
        assertEquals(5, ex.getStartLine());
        assertEquals(10, ex.getStartColumn());
    }

    @Test
    void testTemplateCompileException() {
        final TemplateCompileException ex1 = new TemplateCompileException("编译错误");
        assertEquals("编译错误", ex1.getMessage());

        final Exception cause = new IllegalStateException();
        final TemplateCompileException ex2 = new TemplateCompileException("编译错误", cause);
        assertEquals("编译错误", ex2.getMessage());
        assertEquals(cause, ex2.getCause());

        final TemplateCompileException ex3 = new TemplateCompileException(cause);
        assertEquals(cause, ex3.getCause());
    }

    @Test
    void testArgumentIndexOutOfBoundsExceptionWithPosition() {
        // Test with position only
        final ArgumentIndexOutOfBoundsException ex1 = new ArgumentIndexOutOfBoundsException(
                5, 3, 2, 10, 5, 15, null);
        assertTrue(ex1.getMessage().contains("Argument index 5"));
        assertTrue(ex1.getMessage().contains("out of bounds"));
        assertEquals(5, ex1.getIndex());
        assertEquals(3, ex1.getArrayLength().intValue());
        assertEquals(2, ex1.getStartLine());

        // Test with position and source text
        final String sourceText = "Hello {$5}!";
        final ArgumentIndexOutOfBoundsException ex2 = new ArgumentIndexOutOfBoundsException(
                5, 2, 1, 8, 1, 10, sourceText);

        // Use ErrorPointerAssert for strict pointer validation
        // Line 1: "Hello {$5}!"
        // Columns 8-9 (startColumn=8, endColumn=10, length=2) point to "$5"
        ErrorPointerAssert.assertErrorPointer(ex2,
                "Hello {$5}!",
                "       ^^",
                "Argument index 5 is out of bounds"
        );
        assertEquals(5, ex2.getIndex());
        assertEquals(2, ex2.getArrayLength().intValue());
        assertEquals(sourceText, ex2.getSourceText());

        // Test with Position object
        final Position position = new Position(1, 8, 1, 10);
        final ArgumentIndexOutOfBoundsException ex3 = new ArgumentIndexOutOfBoundsException(
                5, null, position, sourceText);
        assertTrue(ex3.getMessage().contains("arguments array is null"));
        assertEquals(5, ex3.getIndex());
        assertNull(ex3.getArrayLength());
    }
}

