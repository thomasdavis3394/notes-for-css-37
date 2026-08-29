package org.dromara.easytl.exception;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TypeConversionExceptionTest {

    @Test
    void testBasicMessage() {
        TypeConversionException e = new TypeConversionException("Cannot convert String to Integer");
        assertEquals("Cannot convert String to Integer", e.getMessage());
    }

    @Test
    void testWithSourceAndTargetTypes() {
        TypeConversionException e = new TypeConversionException("String", "Integer", "Incompatible types");
        assertTrue(e.getMessage().contains("String"));
        assertTrue(e.getMessage().contains("Integer"));
        // Verify getters return correct values
        assertEquals("String", e.getSourceType());
        assertEquals("Integer", e.getTargetType());
        // Verify reason is included in the error message
        assertTrue(e.getMessage().contains("Incompatible types"));
    }

    @Test
    void testGettersReturnNullForBasicConstructor() {
        TypeConversionException e = new TypeConversionException("Cannot convert String to Integer");
        assertNull(e.getSourceType());
        assertNull(e.getTargetType());
    }

    @Test
    void testInheritance() {
        TypeConversionException e = new TypeConversionException("test");
        assertTrue(e instanceof TemplateRuntimeException);
    }
}