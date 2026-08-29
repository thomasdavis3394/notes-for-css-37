package org.dromara.easytl;

import org.dromara.easytl.exception.TemplateRuntimeException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for type cast expression functionality.
 *
 * @author EasyTL Team
 */
public class CastExpressionTest {

    private final TemplateEngine engine = new TemplateEngine();

    // ========== int/Integer tests ==========

    @Test
    public void testCastToIntFromDouble() {
        Template template = engine.compile("{(int) 3.14}");
        assertEquals("3", template.render(new Context()));
    }

    @Test
    public void testCastToIntFromString() {
        Template template = engine.compile("{(int) '123'}");
        assertEquals("123", template.render(new Context()));
    }

    @Test
    public void testCastToIntegerFromDouble() {
        Template template = engine.compile("{(Integer) 3.99}");
        assertEquals("3", template.render(new Context()));
    }

    @Test
    public void testCastToIntNegative() {
        Template template = engine.compile("{(int) -3.9}");
        assertEquals("-3", template.render(new Context()));
    }

    // ========== long/Long tests ==========

    @Test
    public void testCastToLongFromInt() {
        Template template = engine.compile("{(long) 100}");
        assertEquals("100", template.render(new Context()));
    }

    @Test
    public void testCastToLongFromString() {
        Template template = engine.compile("{(long) '9999999999'}");
        assertEquals("9999999999", template.render(new Context()));
    }

    @Test
    public void testCastToLongFromDouble() {
        Template template = engine.compile("{(long) 123.456}");
        assertEquals("123", template.render(new Context()));
    }

    // ========== double/Double tests ==========

    @Test
    public void testCastToDoubleFromInt() {
        Template template = engine.compile("{(double) 3}");
        assertEquals("3.0", template.render(new Context()));
    }

    @Test
    public void testCastToDoubleFromString() {
        Template template = engine.compile("{(double) '3.14'}");
        assertEquals("3.14", template.render(new Context()));
    }

    @Test
    public void testCastToDoubleFromLong() {
        Context ctx = new Context();
        ctx.put("val", 100L);
        Template template = engine.compile("{(double) val}");
        assertEquals("100.0", template.render(ctx));
    }

    // ========== float/Float tests ==========

    @Test
    public void testCastToFloatFromInt() {
        Template template = engine.compile("{(float) 3}");
        assertEquals("3.0", template.render(new Context()));
    }

    @Test
    public void testCastToFloatFromString() {
        Template template = engine.compile("{(float) '2.5'}");
        assertEquals("2.5", template.render(new Context()));
    }

    // ========== short/Short tests ==========

    @Test
    public void testCastToShortFromInt() {
        Template template = engine.compile("{(short) 1000}");
        assertEquals("1000", template.render(new Context()));
    }

    @Test
    public void testCastToShortFromString() {
        Template template = engine.compile("{(short) '500'}");
        assertEquals("500", template.render(new Context()));
    }

    // ========== byte/Byte tests ==========

    @Test
    public void testCastToByteFromInt() {
        Template template = engine.compile("{(byte) 127}");
        assertEquals("127", template.render(new Context()));
    }

    @Test
    public void testCastToByteFromString() {
        Template template = engine.compile("{(byte) '50'}");
        assertEquals("50", template.render(new Context()));
    }

    // ========== char/Character tests ==========

    @Test
    public void testCastToCharFromString() {
        Template template = engine.compile("{(char) 'A'}");
        assertEquals("A", template.render(new Context()));
    }

    @Test
    public void testCastToCharFromInt() {
        Template template = engine.compile("{(char) 65}");
        assertEquals("A", template.render(new Context()));
    }

    @Test
    public void testCastToCharacterFromChar() {
        Context ctx = new Context();
        ctx.put("ch", 'B');
        Template template = engine.compile("{(Character) ch}");
        assertEquals("B", template.render(ctx));
    }

    // ========== boolean/Boolean tests ==========

    @Test
    public void testCastToBooleanFromStringTrue() {
        Template template = engine.compile("{(boolean) 'true'}");
        assertEquals("true", template.render(new Context()));
    }

    @Test
    public void testCastToBooleanFromStringFalse() {
        Template template = engine.compile("{(boolean) 'false'}");
        assertEquals("false", template.render(new Context()));
    }

    @Test
    public void testCastToBooleanFromInt() {
        Template template = engine.compile("{(boolean) 1}");
        assertEquals("true", template.render(new Context()));
    }

    @Test
    public void testCastToBooleanFromZero() {
        Template template = engine.compile("{(boolean) 0}");
        assertEquals("false", template.render(new Context()));
    }

    // ========== String tests ==========

    @Test
    public void testCastToStringFromInt() {
        Template template = engine.compile("{(String) 123}");
        assertEquals("123", template.render(new Context()));
    }

    @Test
    public void testCastToStringFromDouble() {
        Template template = engine.compile("{(String) 3.14}");
        assertEquals("3.14", template.render(new Context()));
    }

    @Test
    public void testCastToStringFromBoolean() {
        Template template = engine.compile("{(String) true}");
        assertEquals("true", template.render(new Context()));
    }

    // ========== BigDecimal tests ==========

    @Test
    public void testCastToBigDecimalFromString() {
        Template template = engine.compile("{(BigDecimal) '3.14159'}");
        String result = template.render(new Context());
        assertTrue(result.startsWith("3.14159"));
    }

    @Test
    public void testCastToBigDecimalFromInt() {
        Template template = engine.compile("{(BigDecimal) 100}");
        assertEquals("100", template.render(new Context()));
    }

    // ========== BigInteger tests ==========

    @Test
    public void testCastToBigIntegerFromString() {
        Template template = engine.compile("{(BigInteger) '9999999999999999999'}");
        String result = template.render(new Context());
        assertTrue(result.contains("9999999999999999999"));
    }

    @Test
    public void testCastToBigIntegerFromLong() {
        Context ctx = new Context();
        ctx.put("val", 100L);
        Template template = engine.compile("{(BigInteger) val}");
        assertEquals("100", template.render(ctx));
    }

    // ========== null handling tests ==========

    @Test
    public void testCastIntNull() {
        Context ctx = new Context();
        ctx.put("val", null);
        Template template = engine.compile("{(int) val}");
        assertEquals("", template.render(ctx));
    }

    @Test
    public void testCastStringNull() {
        Context ctx = new Context();
        ctx.put("val", null);
        Template template = engine.compile("{(String) val}");
        assertEquals("", template.render(ctx));
    }

    @Test
    public void testCastIntegerNull() {
        Context ctx = new Context();
        ctx.put("val", null);
        Template template = engine.compile("{(Integer) val}");
        assertEquals("", template.render(ctx));
    }

    @Test
    public void testCastBigDecimalNull() {
        Context ctx = new Context();
        ctx.put("val", null);
        Template template = engine.compile("{(BigDecimal) val}");
        assertEquals("", template.render(ctx));
    }

    // ========== chained cast tests ==========

    @Test
    public void testChainedCastIntDouble() {
        Template template = engine.compile("{(int)(double) '3.14'}");
        assertEquals("3", template.render(new Context()));
    }

    @Test
    public void testChainedCastStringInt() {
        Template template = engine.compile("{(String)(int) 3.99}");
        assertEquals("3", template.render(new Context()));
    }

    @Test
    public void testChainedCastThreeLevels() {
        Template template = engine.compile("{(long)(int)(double) '3.99'}");
        assertEquals("3", template.render(new Context()));
    }

    @Test
    public void testChainedCastStringDoubleInt() {
        Template template = engine.compile("{(String)(double)(int) 5}");
        assertEquals("5.0", template.render(new Context()));
    }

    // ========== precedence tests ==========

    @Test
    public void testCastWithUnaryMinus() {
        Context ctx = new Context();
        ctx.put("x", 5);
        Template template = engine.compile("{(int) -x}");
        assertEquals("-5", template.render(ctx));
    }

    @Test
    public void testCastWithUnaryPlus() {
        Context ctx = new Context();
        ctx.put("x", 5);
        Template template = engine.compile("{(int) +x}");
        assertEquals("5", template.render(ctx));
    }

    @Test
    public void testCastWithBinaryAdd() {
        Context ctx = new Context();
        ctx.put("x", 5);
        Template template = engine.compile("{(int) x + 1}");
        assertEquals("6", template.render(ctx));
    }

    @Test
    public void testCastWithBinaryMultiply() {
        Context ctx = new Context();
        ctx.put("x", 5);
        Template template = engine.compile("{(int) x * 2}");
        assertEquals("10", template.render(ctx));
    }

    @Test
    public void testUnaryMinusBeforeCast() {
        Context ctx = new Context();
        ctx.put("x", 5.9);
        Template template = engine.compile("{-(int) x}");
        assertEquals("-5", template.render(ctx));
    }

    @Test
    public void testCastWithTernary() {
        Context ctx = new Context();
        ctx.put("x", 6.5);
        Template template = engine.compile("{(int) x > 5 ? 1 : 0}");
        assertEquals("1", template.render(ctx));  // (int) 6.5 = 6, 6 > 5 is true, result is 1
    }

    @Test
    public void testCastResultInExpression() {
        Context ctx = new Context();
        ctx.put("x", 5.9);
        Template template = engine.compile("{(int) x * 2 + 1}");
        assertEquals("11", template.render(ctx));  // ((int) 5.9) * 2 + 1 = 5 * 2 + 1 = 11
    }

    // ========== error case tests ==========

    @Test
    public void testCastInvalidStringToInt() {
        Template template = engine.compile("{(int) 'abc'}");
        assertThrows(TemplateRuntimeException.class, () -> template.render(new Context()));
    }

    @Test
    public void testCastInvalidStringToLong() {
        Template template = engine.compile("{(long) 'not_a_number'}");
        assertThrows(TemplateRuntimeException.class, () -> template.render(new Context()));
    }

    @Test
    public void testCastInvalidStringToDouble() {
        Template template = engine.compile("{(double) 'abc'}");
        assertThrows(TemplateRuntimeException.class, () -> template.render(new Context()));
    }

    @Test
    public void testCastStringToCharTooLong() {
        Template template = engine.compile("{(char) 'hello'}");
        assertThrows(TemplateRuntimeException.class, () -> template.render(new Context()));
    }

    @Test
    public void testCastUnknownType() {
        Context ctx = new Context();
        ctx.put("x", 5);
        Template template = engine.compile("{(UnknownType) x}");
        assertThrows(TemplateRuntimeException.class, () -> template.render(ctx));
    }

    @Test
    public void testCastToUnimportedClass() {
        Context ctx = new Context();
        ctx.put("x", "test");
        Template template = engine.compile("{(ArrayList) x}");
        assertThrows(TemplateRuntimeException.class, () -> template.render(ctx));
    }

    // ========== imported type tests ==========

    @Test
    public void testCastToImportedArrayList() {
        TemplateEngine engineWithImport = new TemplateEngine().importClass(java.util.ArrayList.class, "AL");
        Template template = engineWithImport.compile("{(AL) ['a', 'b']}");
        String result = template.render(new Context());
        assertTrue(result.contains("a"));
        assertTrue(result.contains("b"));
    }

    @Test
    public void testCastToImportedTypeFromContext() {
        TemplateEngine engineWithImport = new TemplateEngine().importClass(java.util.ArrayList.class);
        Context ctx = new Context();
        ctx.put("list", java.util.Arrays.asList("x", "y"));

        Template template = engineWithImport.compile("{(ArrayList) list}");
        String result = template.render(ctx);
        assertTrue(result.contains("x"));
    }

    @Test
    public void testCastToSameImportedType() {
        TemplateEngine engineWithImport = new TemplateEngine().importClass(java.util.ArrayList.class);
        Context ctx = new Context();
        java.util.ArrayList<String> original = new java.util.ArrayList<>();
        original.add("test");
        ctx.put("list", original);

        Template template = engineWithImport.compile("{(ArrayList) list}");
        String result = template.render(ctx);
        assertTrue(result.contains("test"));
    }
}