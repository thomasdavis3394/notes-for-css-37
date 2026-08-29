package org.dromara.easytl;

import org.dromara.easytl.exception.TypeConversionException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TypeConverter 测试类
 * <p>
 * 测试类型转换工具类的基本功能
 * </p>
 *
 * @author EasyTL Team
 * @since 1.0.0
 */
class TypeConverterTest {

    // ==================== Null Handling Tests ====================

    @Test
    void testNullToObject() {
        Object result = TypeConverter.convert(null, Object.class);
        assertNull(result);
    }

    @Test
    void testNullToString() {
        String result = TypeConverter.convert(null, String.class);
        assertNull(result);
    }

    @Test
    void testNullToPrimitive_throws() {
        // null to int should throw
        assertThrows(TypeConversionException.class, () -> {
            TypeConverter.convert(null, int.class);
        });

        // null to long should throw
        assertThrows(TypeConversionException.class, () -> {
            TypeConverter.convert(null, long.class);
        });

        // null to double should throw
        assertThrows(TypeConversionException.class, () -> {
            TypeConverter.convert(null, double.class);
        });

        // null to boolean should throw
        assertThrows(TypeConversionException.class, () -> {
            TypeConverter.convert(null, boolean.class);
        });
    }

    @Test
    void testNullToPrimitiveWrapper_returnsNull() {
        // null to Integer should return null (not throw)
        Integer intResult = TypeConverter.convert(null, Integer.class);
        assertNull(intResult);

        // null to Long should return null
        Long longResult = TypeConverter.convert(null, Long.class);
        assertNull(longResult);

        // null to Boolean should return null
        Boolean boolResult = TypeConverter.convert(null, Boolean.class);
        assertNull(boolResult);
    }

    // ==================== Exact Match Tests ====================

    @Test
    void testExactMatch_Integer() {
        Integer value = 42;
        Integer result = TypeConverter.convert(value, Integer.class);
        assertSame(value, result);
    }

    @Test
    void testExactMatch_String() {
        String value = "hello";
        String result = TypeConverter.convert(value, String.class);
        assertSame(value, result);
    }

    @Test
    void testExactMatch_Boolean() {
        Boolean value = true;
        Boolean result = TypeConverter.convert(value, Boolean.class);
        assertSame(value, result);
    }

    @Test
    void testExactMatch_PrimitiveInt() {
        int value = 42;
        Integer result = TypeConverter.convert(value, Integer.class);
        assertEquals(Integer.valueOf(42), result);
    }

    @Test
    void testExactMatch_PrimitiveBoolean() {
        boolean value = true;
        Boolean result = TypeConverter.convert(value, Boolean.class);
        assertEquals(Boolean.TRUE, result);
    }

    // ==================== Any to String Tests ====================

    @Test
    void testAnyToString_Integer() {
        Integer value = 42;
        String result = TypeConverter.convert(value, String.class);
        assertEquals("42", result);
    }

    @Test
    void testAnyToString_Boolean() {
        Boolean value = true;
        String result = TypeConverter.convert(value, String.class);
        assertEquals("true", result);
    }

    @Test
    void testAnyToString_List() {
        List<String> list = new ArrayList<>();
        list.add("a");
        list.add("b");
        String result = TypeConverter.convert(list, String.class);
        assertEquals(list.toString(), result);
    }

    @Test
    void testAnyToString_CustomObject() {
        Object obj = new Object() {
            @Override
            public String toString() {
                return "CustomObject";
            }
        };
        String result = TypeConverter.convert(obj, String.class);
        assertEquals("CustomObject", result);
    }

    // ==================== Subclass to Parent Tests ====================

    @Test
    void testSubclassToParent_ListToCollection() {
        List<String> list = new ArrayList<>();
        list.add("test");
        Collection<String> result = TypeConverter.convert(list, Collection.class);
        assertSame(list, result);
    }

    @Test
    void testSubclassToParent_ArrayListToList() {
        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add("test");
        List<String> result = TypeConverter.convert(arrayList, List.class);
        assertSame(arrayList, result);
    }

    @Test
    void testSubclassToParent_LinkedListToList() {
        LinkedList<String> linkedList = new LinkedList<>();
        linkedList.add("test");
        List<String> result = TypeConverter.convert(linkedList, List.class);
        assertSame(linkedList, result);
    }

    @Test
    void testSubclassToParent_StringToCharSequence() {
        String str = "hello";
        CharSequence result = TypeConverter.convert(str, CharSequence.class);
        assertSame(str, result);
    }

    // ==================== Incompatible Type Tests ====================

    @Test
    void testIncompatible_StringToInteger() {
        String value = "not a number";
        assertThrows(TypeConversionException.class, () -> {
            TypeConverter.convert(value, Integer.class);
        });
    }

    @Test
    void testIncompatible_StringToBoolean() {
        String value = "not a boolean";
        assertThrows(TypeConversionException.class, () -> {
            TypeConverter.convert(value, Boolean.class);
        });
    }

    @Test
    void testIncompatible_IntegerToStringType() {
        // Integer to String should succeed (toString special case)
        Integer value = 42;
        String result = TypeConverter.convert(value, String.class);
        assertEquals("42", result);
    }

    @Test
    void testIncompatible_ListToString() {
        // List to String should succeed (toString special case)
        List<Integer> list = new ArrayList<>();
        list.add(1);
        list.add(2);
        String result = TypeConverter.convert(list, String.class);
        assertEquals(list.toString(), result);
    }

    @Test
    void testIncompatible_IntegerToList() {
        Integer value = 42;
        assertThrows(TypeConversionException.class, () -> {
            TypeConverter.convert(value, List.class);
        });
    }

    // ==================== Numeric Conversion Tests ====================

    @Test
    void testNumericConversion_IntegerToLong() {
        Integer value = 42;
        Long result = TypeConverter.convert(value, Long.class);
        assertEquals(Long.valueOf(42L), result);
    }

    @Test
    void testNumericConversion_IntegerToDouble() {
        Integer value = 42;
        Double result = TypeConverter.convert(value, Double.class);
        assertEquals(Double.valueOf(42.0), result);
    }

    @Test
    void testNumericConversion_LongToInteger() {
        Long value = 42L;
        Integer result = TypeConverter.convert(value, Integer.class);
        assertEquals(Integer.valueOf(42), result);
    }

    @Test
    void testNumericConversion_DoubleToInteger() {
        Double value = 42.5;
        Integer result = TypeConverter.convert(value, Integer.class);
        assertEquals(Integer.valueOf(42), result);
    }

    @Test
    void testNumericConversion_DoubleToInteger_Truncates() {
        Double value = 42.9;
        Integer result = TypeConverter.convert(value, Integer.class);
        assertEquals(Integer.valueOf(42), result); // truncates towards zero
    }

    @Test
    void testNumericConversion_DoubleToInteger_Negative() {
        Double value = -42.9;
        Integer result = TypeConverter.convert(value, Integer.class);
        assertEquals(Integer.valueOf(-42), result); // truncates towards zero
    }

    // ==================== Overflow Check Tests ====================

    @Test
    void testOverflow_LongToInteger() {
        Long value = (long) Integer.MAX_VALUE + 1;
        assertThrows(TypeConversionException.class, () -> {
            TypeConverter.convert(value, Integer.class);
        });
    }

    @Test
    void testOverflow_LongToInteger_Negative() {
        Long value = (long) Integer.MIN_VALUE - 1;
        assertThrows(TypeConversionException.class, () -> {
            TypeConverter.convert(value, Integer.class);
        });
    }

    @Test
    void testOverflow_DoubleToInteger() {
        Double value = (double) Integer.MAX_VALUE + 1;
        assertThrows(TypeConversionException.class, () -> {
            TypeConverter.convert(value, Integer.class);
        });
    }

    @Test
    void testOverflow_IntegerToByte() {
        Integer value = 128; // Byte.MAX_VALUE + 1
        assertThrows(TypeConversionException.class, () -> {
            TypeConverter.convert(value, Byte.class);
        });
    }

    @Test
    void testOverflow_IntegerToShort() {
        Integer value = Short.MAX_VALUE + 1;
        assertThrows(TypeConversionException.class, () -> {
            TypeConverter.convert(value, Short.class);
        });
    }

    // ==================== Primitive Type Tests ====================

    @Test
    void testPrimitive_IntegerToInt() {
        Integer value = 42;
        int result = TypeConverter.convert(value, int.class);
        assertEquals(42, result);
    }

    @Test
    void testPrimitive_IntToInteger() {
        int value = 42;
        Integer result = TypeConverter.convert(value, Integer.class);
        assertEquals(Integer.valueOf(42), result);
    }

    @Test
    void testPrimitive_IntToLong() {
        int value = 42;
        long result = TypeConverter.convert(value, long.class);
        assertEquals(42L, result);
    }

    @Test
    void testPrimitive_IntToDouble() {
        int value = 42;
        double result = TypeConverter.convert(value, double.class);
        assertEquals(42.0, result);
    }

    @Test
    void testPrimitive_BooleanToboolean() {
        Boolean value = true;
        boolean result = TypeConverter.convert(value, boolean.class);
        assertTrue(result);
    }

    @Test
    void testPrimitive_booleanToBoolean() {
        boolean value = false;
        Boolean result = TypeConverter.convert(value, Boolean.class);
        assertFalse(result);
    }

    // ==================== Helper Method Tests ====================

    @Test
    void testIsNumeric_NumericTypes() {
        assertTrue(TypeConverter.isNumeric(int.class));
        assertTrue(TypeConverter.isNumeric(long.class));
        assertTrue(TypeConverter.isNumeric(double.class));
        assertTrue(TypeConverter.isNumeric(float.class));
        assertTrue(TypeConverter.isNumeric(byte.class));
        assertTrue(TypeConverter.isNumeric(short.class));
        assertTrue(TypeConverter.isNumeric(Integer.class));
        assertTrue(TypeConverter.isNumeric(Long.class));
        assertTrue(TypeConverter.isNumeric(Double.class));
        assertTrue(TypeConverter.isNumeric(Float.class));
        assertTrue(TypeConverter.isNumeric(Byte.class));
        assertTrue(TypeConverter.isNumeric(Short.class));
    }

    @Test
    void testIsNumeric_NonNumericTypes() {
        assertFalse(TypeConverter.isNumeric(String.class));
        assertFalse(TypeConverter.isNumeric(Boolean.class));
        assertFalse(TypeConverter.isNumeric(boolean.class));
        assertFalse(TypeConverter.isNumeric(List.class));
        assertFalse(TypeConverter.isNumeric(Object.class));
    }

    @Test
    void testIsNumericConversion() {
        assertTrue(TypeConverter.isNumericConversion(Integer.class, Long.class));
        assertTrue(TypeConverter.isNumericConversion(int.class, long.class));
        assertTrue(TypeConverter.isNumericConversion(Integer.class, double.class));
        assertTrue(TypeConverter.isNumericConversion(Double.class, Integer.class));
        assertFalse(TypeConverter.isNumericConversion(String.class, Integer.class));
        assertFalse(TypeConverter.isNumericConversion(Integer.class, String.class));
        assertFalse(TypeConverter.isNumericConversion(String.class, String.class));
    }

    // ==================== Edge Cases ====================

    @Test
    void testConvert_SameType_ReturnsSameInstance() {
        String value = "test";
        String result = TypeConverter.convert(value, String.class);
        assertSame(value, result);
    }

    @Test
    void testConvert_ObjectClass_AcceptsAny() {
        String str = "test";
        Object result = TypeConverter.convert(str, Object.class);
        assertSame(str, result);

        Integer num = 42;
        result = TypeConverter.convert(num, Object.class);
        assertSame(num, result);
    }

    @Test
    void testConvert_FloatToDouble() {
        Float value = 3.14f;
        Double result = TypeConverter.convert(value, Double.class);
        assertEquals(value.doubleValue(), result, 0.0001);
    }

    @Test
    void testConvert_DoubleToFloat() {
        Double value = 3.14;
        Float result = TypeConverter.convert(value, Float.class);
        assertEquals(value.floatValue(), result, 0.0001);
    }

    @Test
    void testConvert_ByteToInt() {
        Byte value = (byte) 42;
        Integer result = TypeConverter.convert(value, Integer.class);
        assertEquals(Integer.valueOf(42), result);
    }

    @Test
    void testConvert_ShortToLong() {
        Short value = (short) 42;
        Long result = TypeConverter.convert(value, Long.class);
        assertEquals(Long.valueOf(42L), result);
    }

    @Test
    void testConvert_Character() {
        Character value = 'A';
        Character result = TypeConverter.convert(value, Character.class);
        assertEquals(Character.valueOf('A'), result);
    }

    @Test
    void testCannotConvert_CharacterToInteger() {
        Character value = 'A';
        assertThrows(TypeConversionException.class, () -> {
            TypeConverter.convert(value, Integer.class);
        });
    }

    // ==================== Utility Class Tests ====================

    @Test
    void testPrivateConstructor() throws Exception {
        // Verify TypeConverter is a utility class with private constructor
        java.lang.reflect.Constructor<TypeConverter> constructor =
            TypeConverter.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        constructor.newInstance(); // Should not throw
    }
}