package org.dromara.easytl;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ValidationResultTest {

    @Test
    void testValidResult() {
        ValidationResult result = ValidationResult.valid();

        assertTrue(result.isValid());
        assertNull(result.getErrorMessage());
        assertNull(result.getErrorPosition());
    }

    @Test
    void testInvalidResult() {
        ValidationResult result = ValidationResult.invalid("Syntax error", 10);

        assertFalse(result.isValid());
        assertEquals("Syntax error", result.getErrorMessage());
        assertEquals(10, result.getErrorPosition());
    }

    @Test
    void testInvalidWithoutPosition() {
        ValidationResult result = ValidationResult.invalid("Error");

        assertFalse(result.isValid());
        assertEquals("Error", result.getErrorMessage());
        assertNull(result.getErrorPosition());
    }
}