package org.dromara.easytl.runtime;

import org.dromara.easytl.exception.TemplateRuntimeException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * OperatorEvaluator 测试类
 * <p>
 * 测试运算符求值器的各种运算功能，包括算术运算、比较运算等
 * </p>
 *
 * @author EasyTL Team
 * @since 1.0.0
 */
class OperatorEvaluatorTest {

    // ==================== 加法运算测试 ====================

    @Test
    void testAdd_IntAndInt() {
        // Given: 两个 int 值
        int a = 10;
        int b = 20;

        // When: 执行加法运算
        Object result = OperatorEvaluator.add(a, b);

        // Then: 应该返回 int 类型的结果
        assertTrue(result instanceof Integer);
        assertEquals(30, (Integer) result);
    }

    @Test
    void testAdd_IntAndLong() {
        // Given: int 和 long 值（类型提升）
        int a = 10;
        long b = 20L;

        // When: 执行加法运算
        Object result = OperatorEvaluator.add(a, b);

        // Then: 应该返回 long 类型的结果（类型提升）
        assertTrue(result instanceof Long);
        assertEquals(30L, (Long) result);
    }

    @Test
    void testAdd_LongAndInt() {
        // Given: long 和 int 值（类型提升）
        long a = 100L;
        int b = 50;

        // When: 执行加法运算
        Object result = OperatorEvaluator.add(a, b);

        // Then: 应该返回 long 类型的结果（类型提升）
        assertTrue(result instanceof Long);
        assertEquals(150L, (Long) result);
    }

    @Test
    void testAdd_LongAndLong() {
        // Given: 两个 long 值
        long a = 1000L;
        long b = 2000L;

        // When: 执行加法运算
        Object result = OperatorEvaluator.add(a, b);

        // Then: 应该返回 long 类型的结果
        assertTrue(result instanceof Long);
        assertEquals(3000L, (Long) result);
    }

    @Test
    void testAdd_IntAndDouble() {
        // Given: int 和 double 值（类型提升）
        int a = 10;
        double b = 20.5;

        // When: 执行加法运算
        Object result = OperatorEvaluator.add(a, b);

        // Then: 应该返回 double 类型的结果（类型提升）
        assertTrue(result instanceof Double);
        assertEquals(30.5, (Double) result, 0.0001);
    }

    @Test
    void testAdd_DoubleAndDouble() {
        // Given: 两个 double 值
        double a = 10.5;
        double b = 20.3;

        // When: 执行加法运算
        Object result = OperatorEvaluator.add(a, b);

        // Then: 应该返回 double 类型的结果
        assertTrue(result instanceof Double);
        assertEquals(30.8, (Double) result, 0.0001);
    }

    @Test
    void testAdd_BigIntegerAndBigInteger() {
        // Given: 两个 BigInteger 值
        BigInteger a = new BigInteger("12345678901234567890");
        BigInteger b = new BigInteger("98765432109876543210");

        // When: 执行加法运算
        Object result = OperatorEvaluator.add(a, b);

        // Then: 应该返回 BigInteger 类型的结果
        assertTrue(result instanceof BigInteger);
        assertEquals(new BigInteger("111111111011111111100"), (BigInteger) result);
    }

    @Test
    void testAdd_BigIntegerAndInt() {
        // Given: BigInteger 和 int 值（类型提升）
        BigInteger a = new BigInteger("12345678901234567890");
        int b = 100;

        // When: 执行加法运算
        Object result = OperatorEvaluator.add(a, b);

        // Then: 应该返回 BigInteger 类型的结果（类型提升）
        assertTrue(result instanceof BigInteger);
        assertEquals(new BigInteger("12345678901234567990"), (BigInteger) result);
    }

    @Test
    void testAdd_BigDecimalAndBigDecimal() {
        // Given: 两个 BigDecimal 值
        BigDecimal a = new BigDecimal("123.456");
        BigDecimal b = new BigDecimal("789.012");

        // When: 执行加法运算
        Object result = OperatorEvaluator.add(a, b);

        // Then: 应该返回 BigDecimal 类型的结果
        assertTrue(result instanceof BigDecimal);
        assertEquals(new BigDecimal("912.468"), (BigDecimal) result);
    }

    @Test
    void testAdd_BigDecimalAndInt() {
        // Given: BigDecimal 和 int 值（类型提升）
        BigDecimal a = new BigDecimal("123.456");
        int b = 100;

        // When: 执行加法运算
        Object result = OperatorEvaluator.add(a, b);

        // Then: 应该返回 BigDecimal 类型的结果（类型提升）
        assertTrue(result instanceof BigDecimal);
        assertEquals(new BigDecimal("223.456"), (BigDecimal) result);
    }

    @Test
    void testAdd_StringAndString() {
        // Given: 两个字符串
        String a = "Hello";
        String b = "World";

        // When: 执行加法运算（字符串拼接）
        Object result = OperatorEvaluator.add(a, b);

        // Then: 应该返回字符串拼接结果
        assertTrue(result instanceof String);
        assertEquals("HelloWorld", result);
    }

    @Test
    void testAdd_StringAndInt() {
        // Given: 字符串和 int 值
        String a = "Number: ";
        int b = 42;

        // When: 执行加法运算（字符串拼接）
        Object result = OperatorEvaluator.add(a, b);

        // Then: 应该返回字符串拼接结果
        assertTrue(result instanceof String);
        assertEquals("Number: 42", result);
    }

    @Test
    void testAdd_IntAndString() {
        // Given: int 值和字符串
        int a = 100;
        String b = " dollars";

        // When: 执行加法运算（字符串拼接）
        Object result = OperatorEvaluator.add(a, b);

        // Then: 应该返回字符串拼接结果
        assertTrue(result instanceof String);
        assertEquals("100 dollars", result);
    }

    @Test
    void testAdd_StringAndBigDecimal() {
        // Given: 字符串和 BigDecimal 值
        String a = "Price: ";
        BigDecimal b = new BigDecimal("99.99");

        // When: 执行加法运算（字符串拼接）
        Object result = OperatorEvaluator.add(a, b);

        // Then: 应该返回字符串拼接结果
        assertTrue(result instanceof String);
        assertEquals("Price: 99.99", result);
    }

    @Test
    void testAdd_StringAndNull() {
        // Given: 字符串和 null（字符串拼接时 null 会被转换为空字符串）
        String a = "Hello";
        Object b = null;

        // When: 执行加法运算（字符串拼接）
        Object result = OperatorEvaluator.add(a, b);

        // Then: 应该返回字符串拼接结果（null 转换为空字符串）
        assertTrue(result instanceof String);
        assertEquals("Hello", result);
    }

    @Test
    void testAdd_NullAndString() {
        // Given: null 和字符串（字符串拼接时 null 会被转换为空字符串）
        Object a = null;
        String b = "World";

        // When: 执行加法运算（字符串拼接）
        Object result = OperatorEvaluator.add(a, b);

        // Then: 应该返回字符串拼接结果（null 转换为空字符串）
        assertTrue(result instanceof String);
        assertEquals("World", result);
    }

    @Test
    void testAdd_EmptyStringAndString() {
        // Given: 空字符串和字符串
        String a = "";
        String b = "World";

        // When: 执行加法运算（字符串拼接）
        Object result = OperatorEvaluator.add(a, b);

        // Then: 应该返回字符串拼接结果
        assertTrue(result instanceof String);
        assertEquals("World", result);
    }

    @Test
    void testAdd_StringAndEmptyString() {
        // Given: 字符串和空字符串
        String a = "Hello";
        String b = "";

        // When: 执行加法运算（字符串拼接）
        Object result = OperatorEvaluator.add(a, b);

        // Then: 应该返回字符串拼接结果
        assertTrue(result instanceof String);
        assertEquals("Hello", result);
    }

    @Test
    void testAdd_StringAndLong() {
        // Given: 字符串和 long 值
        String a = "ID: ";
        long b = 123456789L;

        // When: 执行加法运算（字符串拼接）
        Object result = OperatorEvaluator.add(a, b);

        // Then: 应该返回字符串拼接结果
        assertTrue(result instanceof String);
        assertEquals("ID: 123456789", result);
    }

    @Test
    void testAdd_StringAndDouble() {
        // Given: 字符串和 double 值
        String a = "Value: ";
        double b = 3.14159;

        // When: 执行加法运算（字符串拼接）
        Object result = OperatorEvaluator.add(a, b);

        // Then: 应该返回字符串拼接结果
        assertTrue(result instanceof String);
        assertTrue(result.toString().startsWith("Value: 3.14159"));
    }

    @Test
    void testAdd_StringAndBigInteger() {
        // Given: 字符串和 BigInteger 值
        String a = "Big Number: ";
        BigInteger b = new BigInteger("12345678901234567890");

        // When: 执行加法运算（字符串拼接）
        Object result = OperatorEvaluator.add(a, b);

        // Then: 应该返回字符串拼接结果
        assertTrue(result instanceof String);
        assertEquals("Big Number: 12345678901234567890", result);
    }

    @Test
    void testAdd_StringAndBoolean() {
        // Given: 字符串和 boolean 值
        String a = "Status: ";
        boolean b = true;

        // When: 执行加法运算（字符串拼接）
        Object result = OperatorEvaluator.add(a, b);

        // Then: 应该返回字符串拼接结果
        assertTrue(result instanceof String);
        assertEquals("Status: true", result);
    }

    @Test
    void testAdd_StringAndChar() {
        // Given: 字符串和字符（通过 Character 对象）
        String a = "Char: ";
        Character b = 'A';

        // When: 执行加法运算（字符串拼接）
        Object result = OperatorEvaluator.add(a, b);

        // Then: 应该返回字符串拼接结果
        assertTrue(result instanceof String);
        assertEquals("Char: A", result);
    }

    @Test
    void testAdd_NullOperand() {
        // Given: 一个操作数为 null（且两个都不是字符串）
        int a = 10;

        // When/Then: 应该抛出异常
        assertThrows(TemplateRuntimeException.class, () -> {
            OperatorEvaluator.add(a, null);
        });

        assertThrows(TemplateRuntimeException.class, () -> {
            OperatorEvaluator.add(null, a);
        });
    }

    // ==================== 减法运算测试 ====================

    @Test
    void testSubtract_IntAndInt() {
        // Given: 两个 int 值
        int a = 30;
        int b = 10;

        // When: 执行减法运算
        Object result = OperatorEvaluator.subtract(a, b);

        // Then: 应该返回 int 类型的结果
        assertTrue(result instanceof Integer);
        assertEquals(20, (Integer) result);
    }

    @Test
    void testSubtract_IntAndLong() {
        // Given: int 和 long 值（类型提升）
        int a = 50;
        long b = 20L;

        // When: 执行减法运算
        Object result = OperatorEvaluator.subtract(a, b);

        // Then: 应该返回 long 类型的结果（类型提升）
        assertTrue(result instanceof Long);
        assertEquals(30L, (Long) result);
    }

    @Test
    void testSubtract_LongAndLong() {
        // Given: 两个 long 值
        long a = 5000L;
        long b = 2000L;

        // When: 执行减法运算
        Object result = OperatorEvaluator.subtract(a, b);

        // Then: 应该返回 long 类型的结果
        assertTrue(result instanceof Long);
        assertEquals(3000L, (Long) result);
    }

    @Test
    void testSubtract_DoubleAndDouble() {
        // Given: 两个 double 值
        double a = 30.8;
        double b = 10.3;

        // When: 执行减法运算
        Object result = OperatorEvaluator.subtract(a, b);

        // Then: 应该返回 double 类型的结果
        assertTrue(result instanceof Double);
        assertEquals(20.5, (Double) result, 0.0001);
    }

    @Test
    void testSubtract_BigIntegerAndBigInteger() {
        // Given: 两个 BigInteger 值
        BigInteger a = new BigInteger("111111111011111111100");
        BigInteger b = new BigInteger("12345678901234567890");

        // When: 执行减法运算
        Object result = OperatorEvaluator.subtract(a, b);

        // Then: 应该返回 BigInteger 类型的结果
        assertTrue(result instanceof BigInteger);
        assertEquals(new BigInteger("98765432109876543210"), (BigInteger) result);
    }

    @Test
    void testSubtract_BigDecimalAndBigDecimal() {
        // Given: 两个 BigDecimal 值
        BigDecimal a = new BigDecimal("912.468");
        BigDecimal b = new BigDecimal("123.456");

        // When: 执行减法运算
        Object result = OperatorEvaluator.subtract(a, b);

        // Then: 应该返回 BigDecimal 类型的结果
        assertTrue(result instanceof BigDecimal);
        assertEquals(new BigDecimal("789.012"), (BigDecimal) result);
    }

    @Test
    void testSubtract_NegativeResult() {
        // Given: 被减数小于减数
        int a = 10;
        int b = 20;

        // When: 执行减法运算
        Object result = OperatorEvaluator.subtract(a, b);

        // Then: 应该返回负数结果
        assertTrue(result instanceof Integer);
        assertEquals(-10, (Integer) result);
    }

    @Test
    void testSubtract_NullOperand() {
        // Given: 一个操作数为 null
        int a = 10;

        // When/Then: 应该抛出异常
        assertThrows(TemplateRuntimeException.class, () -> {
            OperatorEvaluator.subtract(a, null);
        });

        assertThrows(TemplateRuntimeException.class, () -> {
            OperatorEvaluator.subtract(null, a);
        });
    }

    // ==================== 乘法运算测试 ====================

    @Test
    void testMultiply_IntAndInt() {
        // Given: 两个 int 值
        int a = 5;
        int b = 6;

        // When: 执行乘法运算
        Object result = OperatorEvaluator.multiply(a, b);

        // Then: 应该返回 int 类型的结果
        assertTrue(result instanceof Integer);
        assertEquals(30, (Integer) result);
    }

    @Test
    void testMultiply_IntAndLong() {
        // Given: int 和 long 值（类型提升）
        int a = 5;
        long b = 100L;

        // When: 执行乘法运算
        Object result = OperatorEvaluator.multiply(a, b);

        // Then: 应该返回 long 类型的结果（类型提升）
        assertTrue(result instanceof Long);
        assertEquals(500L, (Long) result);
    }

    @Test
    void testMultiply_LongAndLong() {
        // Given: 两个 long 值
        long a = 1000L;
        long b = 2000L;

        // When: 执行乘法运算
        Object result = OperatorEvaluator.multiply(a, b);

        // Then: 应该返回 long 类型的结果
        assertTrue(result instanceof Long);
        assertEquals(2000000L, (Long) result);
    }

    @Test
    void testMultiply_DoubleAndDouble() {
        // Given: 两个 double 值
        double a = 2.5;
        double b = 4.0;

        // When: 执行乘法运算
        Object result = OperatorEvaluator.multiply(a, b);

        // Then: 应该返回 double 类型的结果
        assertTrue(result instanceof Double);
        assertEquals(10.0, (Double) result, 0.0001);
    }

    @Test
    void testMultiply_BigIntegerAndBigInteger() {
        // Given: 两个 BigInteger 值
        BigInteger a = new BigInteger("123456789");
        BigInteger b = new BigInteger("987654321");

        // When: 执行乘法运算
        Object result = OperatorEvaluator.multiply(a, b);

        // Then: 应该返回 BigInteger 类型的结果
        assertTrue(result instanceof BigInteger);
        assertEquals(new BigInteger("121932631112635269"), (BigInteger) result);
    }

    @Test
    void testMultiply_BigDecimalAndBigDecimal() {
        // Given: 两个 BigDecimal 值
        BigDecimal a = new BigDecimal("12.5");
        BigDecimal b = new BigDecimal("8.0");

        // When: 执行乘法运算
        Object result = OperatorEvaluator.multiply(a, b);

        // Then: 应该返回 BigDecimal 类型的结果（BigDecimal 乘法会保留小数位数）
        assertTrue(result instanceof BigDecimal);
        // BigDecimal 乘法会保留两个操作数的小数位数之和，所以结果是 100.00
        assertEquals(0, ((BigDecimal) result).compareTo(new BigDecimal("100.00")));
    }

    @Test
    void testMultiply_ByZero() {
        // Given: 一个操作数为 0
        int a = 100;
        int b = 0;

        // When: 执行乘法运算
        Object result = OperatorEvaluator.multiply(a, b);

        // Then: 应该返回 0
        assertTrue(result instanceof Integer);
        assertEquals(0, (Integer) result);
    }

    @Test
    void testMultiply_NullOperand() {
        // Given: 一个操作数为 null
        int a = 10;

        // When/Then: 应该抛出异常
        assertThrows(TemplateRuntimeException.class, () -> {
            OperatorEvaluator.multiply(a, null);
        });

        assertThrows(TemplateRuntimeException.class, () -> {
            OperatorEvaluator.multiply(null, a);
        });
    }

    // ==================== 除法运算测试 ====================

    @Test
    void testDivide_IntAndInt() {
        // Given: 两个 int 值
        int a = 20;
        int b = 4;

        // When: 执行除法运算
        Object result = OperatorEvaluator.divide(a, b);

        // Then: 应该返回 int 类型的结果
        assertTrue(result instanceof Integer);
        assertEquals(5, (Integer) result);
    }

    @Test
    void testDivide_IntAndLong() {
        // Given: int 和 long 值（类型提升）
        int a = 100;
        long b = 5L;

        // When: 执行除法运算
        Object result = OperatorEvaluator.divide(a, b);

        // Then: 应该返回 long 类型的结果（类型提升）
        assertTrue(result instanceof Long);
        assertEquals(20L, (Long) result);
    }

    @Test
    void testDivide_LongAndLong() {
        // Given: 两个 long 值
        long a = 2000L;
        long b = 100L;

        // When: 执行除法运算
        Object result = OperatorEvaluator.divide(a, b);

        // Then: 应该返回 long 类型的结果
        assertTrue(result instanceof Long);
        assertEquals(20L, (Long) result);
    }

    @Test
    void testDivide_DoubleAndDouble() {
        // Given: 两个 double 值
        double a = 15.0;
        double b = 3.0;

        // When: 执行除法运算
        Object result = OperatorEvaluator.divide(a, b);

        // Then: 应该返回 double 类型的结果
        assertTrue(result instanceof Double);
        assertEquals(5.0, (Double) result, 0.0001);
    }

    @Test
    void testDivide_DoubleWithDecimalResult() {
        // Given: 两个 double 值，结果有小数
        double a = 10.0;
        double b = 3.0;

        // When: 执行除法运算
        Object result = OperatorEvaluator.divide(a, b);

        // Then: 应该返回 double 类型的结果
        assertTrue(result instanceof Double);
        assertEquals(3.3333333333333335, (Double) result, 0.0001);
    }

    @Test
    void testDivide_BigIntegerAndBigInteger() {
        // Given: 两个 BigInteger 值
        BigInteger a = new BigInteger("1000000000");
        BigInteger b = new BigInteger("1000");

        // When: 执行除法运算
        Object result = OperatorEvaluator.divide(a, b);

        // Then: 应该返回 BigInteger 类型的结果
        assertTrue(result instanceof BigInteger);
        assertEquals(new BigInteger("1000000"), (BigInteger) result);
    }

    @Test
    void testDivide_BigDecimalAndBigDecimal() {
        // Given: 两个 BigDecimal 值
        BigDecimal a = new BigDecimal("100.0");
        BigDecimal b = new BigDecimal("4.0");

        // When: 执行除法运算
        Object result = OperatorEvaluator.divide(a, b);

        // Then: 应该返回 BigDecimal 类型的结果（四舍五入）
        assertTrue(result instanceof BigDecimal);
        assertEquals(new BigDecimal("25.0"), (BigDecimal) result);
    }

    @Test
    void testDivide_BigDecimalWithPrecision() {
        // Given: 两个 BigDecimal 值，需要精确小数
        BigDecimal a = new BigDecimal("10.0");
        BigDecimal b = new BigDecimal("3.0");

        // When: 执行除法运算
        Object result = OperatorEvaluator.divide(a, b);

        // Then: 应该返回 BigDecimal 类型的结果（使用 ROUND_HALF_UP 四舍五入）
        assertTrue(result instanceof BigDecimal);
        BigDecimal actual = (BigDecimal) result;
        // BigDecimal 除法使用 ROUND_HALF_UP，由于被除数和除数都是 1 位小数，
        // 结果会保留 1 位小数，10.0 / 3.0 = 3.3 (四舍五入)
        // 验证结果在合理范围内（大于等于 3.3，小于 3.4）
        assertTrue(actual.compareTo(new BigDecimal("3.3")) >= 0, 
            "结果应该大于等于 3.3，实际值: " + actual);
        assertTrue(actual.compareTo(new BigDecimal("3.4")) < 0, 
            "结果应该小于 3.4，实际值: " + actual);
    }

    @Test
    void testDivide_ByZero_Int() {
        // Given: 除数为 0（int）
        int a = 10;
        int b = 0;

        // When/Then: 应该抛出 ArithmeticException（Java 整数除零异常）
        assertThrows(ArithmeticException.class, () -> {
            OperatorEvaluator.divide(a, b);
        });
    }

    @Test
    void testDivide_ByZero_Long() {
        // Given: 除数为 0（long）
        long a = 100L;
        long b = 0L;

        // When/Then: 应该抛出 ArithmeticException（Java 整数除零异常）
        assertThrows(ArithmeticException.class, () -> {
            OperatorEvaluator.divide(a, b);
        });
    }

    @Test
    void testDivide_ByZero_Double() {
        // Given: 除数为 0.0（double）
        double a = 10.0;
        double b = 0.0;

        // When: 执行除法运算
        Object result = OperatorEvaluator.divide(a, b);

        // Then: double 除零会返回 Infinity
        assertTrue(result instanceof Double);
        assertTrue(Double.isInfinite((Double) result));
    }

    @Test
    void testDivide_ByZero_BigInteger() {
        // Given: 除数为 0（BigInteger）
        BigInteger a = new BigInteger("100");
        BigInteger b = BigInteger.ZERO;

        // When/Then: 应该抛出 ArithmeticException
        assertThrows(ArithmeticException.class, () -> {
            OperatorEvaluator.divide(a, b);
        });
    }

    @Test
    void testDivide_ByZero_BigDecimal() {
        // Given: 除数为 0（BigDecimal）
        BigDecimal a = new BigDecimal("100.0");
        BigDecimal b = BigDecimal.ZERO;

        // When/Then: 应该抛出 ArithmeticException
        assertThrows(ArithmeticException.class, () -> {
            OperatorEvaluator.divide(a, b);
        });
    }

    @Test
    void testDivide_NullOperand() {
        // Given: 一个操作数为 null
        int a = 10;

        // When/Then: 应该抛出异常
        assertThrows(TemplateRuntimeException.class, () -> {
            OperatorEvaluator.divide(a, null);
        });

        assertThrows(TemplateRuntimeException.class, () -> {
            OperatorEvaluator.divide(null, a);
        });
    }

    // ==================== 取模运算测试 ====================

    @Test
    void testModulo_IntAndInt() {
        // Given: 两个 int 值
        int a = 20;
        int b = 3;

        // When: 执行取模运算
        Object result = OperatorEvaluator.modulo(a, b);

        // Then: 应该返回 int 类型的结果
        assertTrue(result instanceof Integer);
        assertEquals(2, (Integer) result);
    }

    @Test
    void testModulo_IntAndLong() {
        // Given: int 和 long 值（类型提升）
        int a = 100;
        long b = 7L;

        // When: 执行取模运算
        Object result = OperatorEvaluator.modulo(a, b);

        // Then: 应该返回 long 类型的结果（类型提升）
        assertTrue(result instanceof Long);
        assertEquals(2L, (Long) result);
    }

    @Test
    void testModulo_LongAndLong() {
        // Given: 两个 long 值
        long a = 2000L;
        long b = 100L;

        // When: 执行取模运算
        Object result = OperatorEvaluator.modulo(a, b);

        // Then: 应该返回 long 类型的结果
        assertTrue(result instanceof Long);
        assertEquals(0L, (Long) result);
    }

    @Test
    void testModulo_DoubleAndDouble() {
        // Given: 两个 double 值
        double a = 10.5;
        double b = 3.0;

        // When: 执行取模运算
        Object result = OperatorEvaluator.modulo(a, b);

        // Then: 应该返回 double 类型的结果
        assertTrue(result instanceof Double);
        assertEquals(1.5, (Double) result, 0.0001);
    }

    @Test
    void testModulo_BigIntegerAndBigInteger() {
        // Given: 两个 BigInteger 值
        BigInteger a = new BigInteger("1000000000");
        BigInteger b = new BigInteger("7");

        // When: 执行取模运算
        Object result = OperatorEvaluator.modulo(a, b);

        // Then: 应该返回 BigInteger 类型的结果
        assertTrue(result instanceof BigInteger);
        // 1000000000 % 7 = 6 (1000000000 / 7 = 142857142 余 6)
        assertEquals(new BigInteger("6"), (BigInteger) result);
    }

    @Test
    void testModulo_BigDecimalAndBigDecimal() {
        // Given: 两个 BigDecimal 值
        BigDecimal a = new BigDecimal("100.5");
        BigDecimal b = new BigDecimal("7.0");

        // When: 执行取模运算
        Object result = OperatorEvaluator.modulo(a, b);

        // Then: 应该返回 BigDecimal 类型的结果
        assertTrue(result instanceof BigDecimal);
        // 验证余数在合理范围内
        BigDecimal remainder = (BigDecimal) result;
        assertTrue(remainder.compareTo(BigDecimal.ZERO) >= 0);
        assertTrue(remainder.compareTo(b) < 0);
    }

    @Test
    void testModulo_ByZero_Int() {
        // Given: 除数为 0（int）
        int a = 10;
        int b = 0;

        // When/Then: 应该抛出 ArithmeticException
        assertThrows(ArithmeticException.class, () -> {
            OperatorEvaluator.modulo(a, b);
        });
    }

    @Test
    void testModulo_ByZero_Long() {
        // Given: 除数为 0（long）
        long a = 100L;
        long b = 0L;

        // When/Then: 应该抛出 ArithmeticException
        assertThrows(ArithmeticException.class, () -> {
            OperatorEvaluator.modulo(a, b);
        });
    }

    @Test
    void testModulo_ByZero_Double() {
        // Given: 除数为 0.0（double）
        double a = 10.0;
        double b = 0.0;

        // When: 执行取模运算
        Object result = OperatorEvaluator.modulo(a, b);

        // Then: double 取模零会返回 NaN
        assertTrue(result instanceof Double);
        assertTrue(Double.isNaN((Double) result));
    }

    @Test
    void testModulo_ByZero_BigInteger() {
        // Given: 除数为 0（BigInteger）
        BigInteger a = new BigInteger("100");
        BigInteger b = BigInteger.ZERO;

        // When/Then: 应该抛出 ArithmeticException
        assertThrows(ArithmeticException.class, () -> {
            OperatorEvaluator.modulo(a, b);
        });
    }

    @Test
    void testModulo_ByZero_BigDecimal() {
        // Given: 除数为 0（BigDecimal）
        BigDecimal a = new BigDecimal("100.0");
        BigDecimal b = BigDecimal.ZERO;

        // When/Then: 应该抛出 ArithmeticException
        assertThrows(ArithmeticException.class, () -> {
            OperatorEvaluator.modulo(a, b);
        });
    }

    @Test
    void testModulo_NullOperand() {
        // Given: 一个操作数为 null
        int a = 10;

        // When/Then: 应该抛出异常
        assertThrows(TemplateRuntimeException.class, () -> {
            OperatorEvaluator.modulo(a, null);
        });

        assertThrows(TemplateRuntimeException.class, () -> {
            OperatorEvaluator.modulo(null, a);
        });
    }

    // ==================== 比较运算测试 ====================

    @Test
    void testEqual_IntAndInt() {
        // Given: 两个相等的 int 值
        int a = 10;
        int b = 10;

        // When: 执行相等比较
        boolean result = OperatorEvaluator.equal(a, b);

        // Then: 应该返回 true
        assertTrue(result);
    }

    @Test
    void testEqual_IntAndInt_Different() {
        // Given: 两个不同的 int 值
        int a = 10;
        int b = 20;

        // When: 执行相等比较
        boolean result = OperatorEvaluator.equal(a, b);

        // Then: 应该返回 false
        assertFalse(result);
    }

    @Test
    void testEqual_StringAndString() {
        // Given: 两个相等的字符串
        String a = "Hello";
        String b = "Hello";

        // When: 执行相等比较
        boolean result = OperatorEvaluator.equal(a, b);

        // Then: 应该返回 true
        assertTrue(result);
    }

    @Test
    void testEqual_StringAndString_Different() {
        // Given: 两个不同的字符串
        String a = "Hello";
        String b = "World";

        // When: 执行相等比较
        boolean result = OperatorEvaluator.equal(a, b);

        // Then: 应该返回 false
        assertFalse(result);
    }

    @Test
    void testEqual_NullAndNull() {
        // Given: 两个 null 值
        Object a = null;
        Object b = null;

        // When: 执行相等比较
        boolean result = OperatorEvaluator.equal(a, b);

        // Then: 应该返回 true
        assertTrue(result);
    }

    @Test
    void testEqual_NullAndValue() {
        // Given: null 和非 null 值
        Object a = null;
        int b = 10;

        // When: 执行相等比较
        boolean result = OperatorEvaluator.equal(a, b);

        // Then: 应该返回 false
        assertFalse(result);
    }

    @Test
    void testEqual_ValueAndNull() {
        // Given: 非 null 值和 null
        int a = 10;
        Object b = null;

        // When: 执行相等比较
        boolean result = OperatorEvaluator.equal(a, b);

        // Then: 应该返回 false
        assertFalse(result);
    }

    @Test
    void testEqual_BigIntegerAndBigInteger() {
        // Given: 两个相等的 BigInteger 值
        BigInteger a = new BigInteger("123456789");
        BigInteger b = new BigInteger("123456789");

        // When: 执行相等比较
        boolean result = OperatorEvaluator.equal(a, b);

        // Then: 应该返回 true
        assertTrue(result);
    }

    @Test
    void testEqual_BigDecimalAndBigDecimal() {
        // Given: 两个相等的 BigDecimal 值
        BigDecimal a = new BigDecimal("123.456");
        BigDecimal b = new BigDecimal("123.456");

        // When: 执行相等比较
        boolean result = OperatorEvaluator.equal(a, b);

        // Then: 应该返回 true
        assertTrue(result);
    }

    @Test
    void testNotEqual_IntAndInt() {
        // Given: 两个不同的 int 值
        int a = 10;
        int b = 20;

        // When: 执行不等比较
        boolean result = OperatorEvaluator.notEqual(a, b);

        // Then: 应该返回 true
        assertTrue(result);
    }

    @Test
    void testNotEqual_IntAndInt_Same() {
        // Given: 两个相等的 int 值
        int a = 10;
        int b = 10;

        // When: 执行不等比较
        boolean result = OperatorEvaluator.notEqual(a, b);

        // Then: 应该返回 false
        assertFalse(result);
    }

    @Test
    void testNotEqual_NullAndNull() {
        // Given: 两个 null 值
        Object a = null;
        Object b = null;

        // When: 执行不等比较
        boolean result = OperatorEvaluator.notEqual(a, b);

        // Then: 应该返回 false
        assertFalse(result);
    }

    @Test
    void testLessThan_IntAndInt() {
        // Given: 左操作数小于右操作数
        int a = 10;
        int b = 20;

        // When: 执行小于比较
        boolean result = OperatorEvaluator.lessThan(a, b);

        // Then: 应该返回 true
        assertTrue(result);
    }

    @Test
    void testLessThan_IntAndInt_Greater() {
        // Given: 左操作数大于右操作数
        int a = 20;
        int b = 10;

        // When: 执行小于比较
        boolean result = OperatorEvaluator.lessThan(a, b);

        // Then: 应该返回 false
        assertFalse(result);
    }

    @Test
    void testLessThan_IntAndInt_Equal() {
        // Given: 两个相等的 int 值
        int a = 10;
        int b = 10;

        // When: 执行小于比较
        boolean result = OperatorEvaluator.lessThan(a, b);

        // Then: 应该返回 false
        assertFalse(result);
    }

    @Test
    void testLessThan_LongAndInt() {
        // Given: long 和 int 值（类型提升）
        long a = 10L;
        int b = 20;

        // When: 执行小于比较
        boolean result = OperatorEvaluator.lessThan(a, b);

        // Then: 应该返回 true
        assertTrue(result);
    }

    @Test
    void testLessThan_DoubleAndDouble() {
        // Given: 两个 double 值
        double a = 10.5;
        double b = 20.3;

        // When: 执行小于比较
        boolean result = OperatorEvaluator.lessThan(a, b);

        // Then: 应该返回 true
        assertTrue(result);
    }

    @Test
    void testLessThan_BigIntegerAndBigInteger() {
        // Given: 两个 BigInteger 值
        BigInteger a = new BigInteger("100");
        BigInteger b = new BigInteger("200");

        // When: 执行小于比较
        boolean result = OperatorEvaluator.lessThan(a, b);

        // Then: 应该返回 true
        assertTrue(result);
    }

    @Test
    void testLessThan_BigDecimalAndBigDecimal() {
        // Given: 两个 BigDecimal 值
        BigDecimal a = new BigDecimal("100.5");
        BigDecimal b = new BigDecimal("200.3");

        // When: 执行小于比较
        boolean result = OperatorEvaluator.lessThan(a, b);

        // Then: 应该返回 true
        assertTrue(result);
    }

    @Test
    void testLessThan_StringAndString() {
        // Given: 两个字符串（按字典序比较）
        String a = "apple";
        String b = "banana";

        // When: 执行小于比较
        boolean result = OperatorEvaluator.lessThan(a, b);

        // Then: 应该返回 true（字典序）
        assertTrue(result);
    }

    @Test
    void testLessThan_NullAndValue() {
        // Given: null 和非 null 值
        Object a = null;
        int b = 10;

        // When: 执行小于比较
        boolean result = OperatorEvaluator.lessThan(a, b);

        // Then: null 应该被视为小于任何值
        assertTrue(result);
    }

    @Test
    void testLessThan_ValueAndNull() {
        // Given: 非 null 值和 null
        int a = 10;
        Object b = null;

        // When: 执行小于比较
        boolean result = OperatorEvaluator.lessThan(a, b);

        // Then: 任何值应该大于 null
        assertFalse(result);
    }

    @Test
    void testLessThanOrEqual_IntAndInt() {
        // Given: 左操作数小于右操作数
        int a = 10;
        int b = 20;

        // When: 执行小于等于比较
        boolean result = OperatorEvaluator.lessThanOrEqual(a, b);

        // Then: 应该返回 true
        assertTrue(result);
    }

    @Test
    void testLessThanOrEqual_IntAndInt_Equal() {
        // Given: 两个相等的 int 值
        int a = 10;
        int b = 10;

        // When: 执行小于等于比较
        boolean result = OperatorEvaluator.lessThanOrEqual(a, b);

        // Then: 应该返回 true
        assertTrue(result);
    }

    @Test
    void testLessThanOrEqual_IntAndInt_Greater() {
        // Given: 左操作数大于右操作数
        int a = 20;
        int b = 10;

        // When: 执行小于等于比较
        boolean result = OperatorEvaluator.lessThanOrEqual(a, b);

        // Then: 应该返回 false
        assertFalse(result);
    }

    @Test
    void testGreaterThan_IntAndInt() {
        // Given: 左操作数大于右操作数
        int a = 20;
        int b = 10;

        // When: 执行大于比较
        boolean result = OperatorEvaluator.greaterThan(a, b);

        // Then: 应该返回 true
        assertTrue(result);
    }

    @Test
    void testGreaterThan_IntAndInt_Less() {
        // Given: 左操作数小于右操作数
        int a = 10;
        int b = 20;

        // When: 执行大于比较
        boolean result = OperatorEvaluator.greaterThan(a, b);

        // Then: 应该返回 false
        assertFalse(result);
    }

    @Test
    void testGreaterThan_IntAndInt_Equal() {
        // Given: 两个相等的 int 值
        int a = 10;
        int b = 10;

        // When: 执行大于比较
        boolean result = OperatorEvaluator.greaterThan(a, b);

        // Then: 应该返回 false
        assertFalse(result);
    }

    @Test
    void testGreaterThan_LongAndInt() {
        // Given: long 和 int 值（类型提升）
        long a = 20L;
        int b = 10;

        // When: 执行大于比较
        boolean result = OperatorEvaluator.greaterThan(a, b);

        // Then: 应该返回 true
        assertTrue(result);
    }

    @Test
    void testGreaterThan_DoubleAndDouble() {
        // Given: 两个 double 值
        double a = 20.3;
        double b = 10.5;

        // When: 执行大于比较
        boolean result = OperatorEvaluator.greaterThan(a, b);

        // Then: 应该返回 true
        assertTrue(result);
    }

    @Test
    void testGreaterThan_BigIntegerAndBigInteger() {
        // Given: 两个 BigInteger 值
        BigInteger a = new BigInteger("200");
        BigInteger b = new BigInteger("100");

        // When: 执行大于比较
        boolean result = OperatorEvaluator.greaterThan(a, b);

        // Then: 应该返回 true
        assertTrue(result);
    }

    @Test
    void testGreaterThan_BigDecimalAndBigDecimal() {
        // Given: 两个 BigDecimal 值
        BigDecimal a = new BigDecimal("200.3");
        BigDecimal b = new BigDecimal("100.5");

        // When: 执行大于比较
        boolean result = OperatorEvaluator.greaterThan(a, b);

        // Then: 应该返回 true
        assertTrue(result);
    }

    @Test
    void testGreaterThan_StringAndString() {
        // Given: 两个字符串（按字典序比较）
        String a = "banana";
        String b = "apple";

        // When: 执行大于比较
        boolean result = OperatorEvaluator.greaterThan(a, b);

        // Then: 应该返回 true（字典序）
        assertTrue(result);
    }

    @Test
    void testGreaterThan_NullAndValue() {
        // Given: null 和非 null 值
        Object a = null;
        int b = 10;

        // When: 执行大于比较
        boolean result = OperatorEvaluator.greaterThan(a, b);

        // Then: null 应该被视为小于任何值
        assertFalse(result);
    }

    @Test
    void testGreaterThan_ValueAndNull() {
        // Given: 非 null 值和 null
        int a = 10;
        Object b = null;

        // When: 执行大于比较
        boolean result = OperatorEvaluator.greaterThan(a, b);

        // Then: 任何值应该大于 null
        assertTrue(result);
    }

    @Test
    void testGreaterThanOrEqual_IntAndInt() {
        // Given: 左操作数大于右操作数
        int a = 20;
        int b = 10;

        // When: 执行大于等于比较
        boolean result = OperatorEvaluator.greaterThanOrEqual(a, b);

        // Then: 应该返回 true
        assertTrue(result);
    }

    @Test
    void testGreaterThanOrEqual_IntAndInt_Equal() {
        // Given: 两个相等的 int 值
        int a = 10;
        int b = 10;

        // When: 执行大于等于比较
        boolean result = OperatorEvaluator.greaterThanOrEqual(a, b);

        // Then: 应该返回 true
        assertTrue(result);
    }

    @Test
    void testGreaterThanOrEqual_IntAndInt_Less() {
        // Given: 左操作数小于右操作数
        int a = 10;
        int b = 20;

        // When: 执行大于等于比较
        boolean result = OperatorEvaluator.greaterThanOrEqual(a, b);

        // Then: 应该返回 false
        assertFalse(result);
    }

    // ==================== 类型转换比较测试 ====================

    @Test
    void testEqual_IntAndDouble_SameValue() {
        // Given: int 和 double 值（数值相同）
        int a = 10;
        double b = 10.0;

        // When: 执行相等比较
        boolean result = OperatorEvaluator.equal(a, b);

        // Then: Objects.equals 会返回 false（类型不同）
        assertFalse(result);
    }

    @Test
    void testEqual_IntAndLong_SameValue() {
        // Given: int 和 long 值（数值相同）
        int a = 10;
        long b = 10L;

        // When: 执行相等比较
        boolean result = OperatorEvaluator.equal(a, b);

        // Then: Objects.equals 会返回 false（类型不同）
        assertFalse(result);
    }

    @Test
    void testEqual_IntAndBigInteger_SameValue() {
        // Given: int 和 BigInteger 值（数值相同）
        int a = 10;
        BigInteger b = new BigInteger("10");

        // When: 执行相等比较
        boolean result = OperatorEvaluator.equal(a, b);

        // Then: Objects.equals 会返回 false（类型不同）
        assertFalse(result);
    }

    @Test
    void testEqual_IntAndBigDecimal_SameValue() {
        // Given: int 和 BigDecimal 值（数值相同）
        int a = 10;
        BigDecimal b = new BigDecimal("10");

        // When: 执行相等比较
        boolean result = OperatorEvaluator.equal(a, b);

        // Then: Objects.equals 会返回 false（类型不同）
        assertFalse(result);
    }

    @Test
    void testLessThan_IntAndDouble() {
        // Given: int 和 double 值（类型转换）
        int a = 10;
        double b = 20.5;

        // When: 执行小于比较
        boolean result = OperatorEvaluator.lessThan(a, b);

        // Then: 应该返回 true（类型转换后比较）
        assertTrue(result);
    }

    @Test
    void testLessThan_DoubleAndInt() {
        // Given: double 和 int 值（类型转换）
        double a = 10.5;
        int b = 20;

        // When: 执行小于比较
        boolean result = OperatorEvaluator.lessThan(a, b);

        // Then: 应该返回 true（类型转换后比较）
        assertTrue(result);
    }

    @Test
    void testLessThan_IntAndBigDecimal() {
        // Given: int 和 BigDecimal 值（类型转换）
        int a = 10;
        BigDecimal b = new BigDecimal("20.5");

        // When: 执行小于比较
        boolean result = OperatorEvaluator.lessThan(a, b);

        // Then: 应该返回 true（类型转换后比较）
        assertTrue(result);
    }

    @Test
    void testLessThan_BigDecimalAndInt() {
        // Given: BigDecimal 和 int 值（类型转换）
        BigDecimal a = new BigDecimal("10.5");
        int b = 20;

        // When: 执行小于比较
        boolean result = OperatorEvaluator.lessThan(a, b);

        // Then: 应该返回 true（类型转换后比较）
        assertTrue(result);
    }

    @Test
    void testLessThan_IntAndBigInteger() {
        // Given: int 和 BigInteger 值（类型转换）
        int a = 10;
        BigInteger b = new BigInteger("20");

        // When: 执行小于比较
        boolean result = OperatorEvaluator.lessThan(a, b);

        // Then: 应该返回 true（类型转换后比较）
        assertTrue(result);
    }

    @Test
    void testLessThan_BigIntegerAndInt() {
        // Given: BigInteger 和 int 值（类型转换）
        BigInteger a = new BigInteger("10");
        int b = 20;

        // When: 执行小于比较
        boolean result = OperatorEvaluator.lessThan(a, b);

        // Then: 应该返回 true（类型转换后比较）
        assertTrue(result);
    }

    @Test
    void testLessThan_LongAndDouble() {
        // Given: long 和 double 值（类型转换）
        long a = 10L;
        double b = 20.5;

        // When: 执行小于比较
        boolean result = OperatorEvaluator.lessThan(a, b);

        // Then: 应该返回 true（类型转换后比较）
        assertTrue(result);
    }

    @Test
    void testLessThan_LongAndBigDecimal() {
        // Given: long 和 BigDecimal 值（类型转换）
        long a = 10L;
        BigDecimal b = new BigDecimal("20.5");

        // When: 执行小于比较
        boolean result = OperatorEvaluator.lessThan(a, b);

        // Then: 应该返回 true（类型转换后比较）
        assertTrue(result);
    }

    @Test
    void testLessThan_LongAndBigInteger() {
        // Given: long 和 BigInteger 值（类型转换）
        long a = 10L;
        BigInteger b = new BigInteger("20");

        // When: 执行小于比较
        boolean result = OperatorEvaluator.lessThan(a, b);

        // Then: 应该返回 true（类型转换后比较）
        assertTrue(result);
    }

    @Test
    void testLessThan_DoubleAndBigDecimal() {
        // Given: double 和 BigDecimal 值（类型转换）
        double a = 10.5;
        BigDecimal b = new BigDecimal("20.5");

        // When: 执行小于比较
        boolean result = OperatorEvaluator.lessThan(a, b);

        // Then: 应该返回 true（类型转换后比较）
        assertTrue(result);
    }

    @Test
    void testLessThan_DoubleAndBigInteger() {
        // Given: double 和 BigInteger 值（类型转换）
        double a = 10.5;
        BigInteger b = new BigInteger("20");

        // When: 执行小于比较
        boolean result = OperatorEvaluator.lessThan(a, b);

        // Then: 应该返回 true（类型转换后比较）
        assertTrue(result);
    }

    @Test
    void testLessThan_BigDecimalAndBigInteger() {
        // Given: BigDecimal 和 BigInteger 值（类型转换）
        BigDecimal a = new BigDecimal("10.5");
        BigInteger b = new BigInteger("20");

        // When: 执行小于比较
        boolean result = OperatorEvaluator.lessThan(a, b);

        // Then: 应该返回 true（类型转换后比较）
        assertTrue(result);
    }

    @Test
    void testLessThan_FloatAndDouble() {
        // Given: float 和 double 值（类型转换）
        float a = 10.5f;
        double b = 20.5;

        // When: 执行小于比较
        boolean result = OperatorEvaluator.lessThan(a, b);

        // Then: 应该返回 true（类型转换后比较）
        assertTrue(result);
    }

    @Test
    void testLessThanOrEqual_IntAndDouble() {
        // Given: int 和 double 值（类型转换）
        int a = 10;
        double b = 20.5;

        // When: 执行小于等于比较
        boolean result = OperatorEvaluator.lessThanOrEqual(a, b);

        // Then: 应该返回 true
        assertTrue(result);
    }

    @Test
    void testLessThanOrEqual_IntAndDouble_Equal() {
        // Given: int 和 double 值（数值相等，类型转换）
        int a = 10;
        double b = 10.0;

        // When: 执行小于等于比较
        boolean result = OperatorEvaluator.lessThanOrEqual(a, b);

        // Then: 应该返回 true（类型转换后相等）
        assertTrue(result);
    }

    @Test
    void testLessThanOrEqual_IntAndBigDecimal() {
        // Given: int 和 BigDecimal 值（类型转换）
        int a = 10;
        BigDecimal b = new BigDecimal("20.5");

        // When: 执行小于等于比较
        boolean result = OperatorEvaluator.lessThanOrEqual(a, b);

        // Then: 应该返回 true
        assertTrue(result);
    }

    @Test
    void testLessThanOrEqual_NullAndNull() {
        // Given: 两个 null 值
        Object a = null;
        Object b = null;

        // When: 执行小于等于比较
        boolean result = OperatorEvaluator.lessThanOrEqual(a, b);

        // Then: 应该返回 true（null 等于 null）
        assertTrue(result);
    }

    @Test
    void testGreaterThan_IntAndDouble() {
        // Given: int 和 double 值（类型转换）
        int a = 20;
        double b = 10.5;

        // When: 执行大于比较
        boolean result = OperatorEvaluator.greaterThan(a, b);

        // Then: 应该返回 true（类型转换后比较）
        assertTrue(result);
    }

    @Test
    void testGreaterThan_IntAndBigDecimal() {
        // Given: int 和 BigDecimal 值（类型转换）
        int a = 20;
        BigDecimal b = new BigDecimal("10.5");

        // When: 执行大于比较
        boolean result = OperatorEvaluator.greaterThan(a, b);

        // Then: 应该返回 true（类型转换后比较）
        assertTrue(result);
    }

    @Test
    void testGreaterThan_IntAndBigInteger() {
        // Given: int 和 BigInteger 值（类型转换）
        int a = 20;
        BigInteger b = new BigInteger("10");

        // When: 执行大于比较
        boolean result = OperatorEvaluator.greaterThan(a, b);

        // Then: 应该返回 true（类型转换后比较）
        assertTrue(result);
    }

    @Test
    void testGreaterThan_LongAndDouble() {
        // Given: long 和 double 值（类型转换）
        long a = 20L;
        double b = 10.5;

        // When: 执行大于比较
        boolean result = OperatorEvaluator.greaterThan(a, b);

        // Then: 应该返回 true（类型转换后比较）
        assertTrue(result);
    }

    @Test
    void testGreaterThan_LongAndBigDecimal() {
        // Given: long 和 BigDecimal 值（类型转换）
        long a = 20L;
        BigDecimal b = new BigDecimal("10.5");

        // When: 执行大于比较
        boolean result = OperatorEvaluator.greaterThan(a, b);

        // Then: 应该返回 true（类型转换后比较）
        assertTrue(result);
    }

    @Test
    void testGreaterThan_LongAndBigInteger() {
        // Given: long 和 BigInteger 值（类型转换）
        long a = 20L;
        BigInteger b = new BigInteger("10");

        // When: 执行大于比较
        boolean result = OperatorEvaluator.greaterThan(a, b);

        // Then: 应该返回 true（类型转换后比较）
        assertTrue(result);
    }

    @Test
    void testGreaterThan_DoubleAndBigDecimal() {
        // Given: double 和 BigDecimal 值（类型转换）
        double a = 20.5;
        BigDecimal b = new BigDecimal("10.5");

        // When: 执行大于比较
        boolean result = OperatorEvaluator.greaterThan(a, b);

        // Then: 应该返回 true（类型转换后比较）
        assertTrue(result);
    }

    @Test
    void testGreaterThan_DoubleAndBigInteger() {
        // Given: double 和 BigInteger 值（类型转换）
        double a = 20.5;
        BigInteger b = new BigInteger("10");

        // When: 执行大于比较
        boolean result = OperatorEvaluator.greaterThan(a, b);

        // Then: 应该返回 true（类型转换后比较）
        assertTrue(result);
    }

    @Test
    void testGreaterThan_BigDecimalAndBigInteger() {
        // Given: BigDecimal 和 BigInteger 值（类型转换）
        BigDecimal a = new BigDecimal("20.5");
        BigInteger b = new BigInteger("10");

        // When: 执行大于比较
        boolean result = OperatorEvaluator.greaterThan(a, b);

        // Then: 应该返回 true（类型转换后比较）
        assertTrue(result);
    }

    @Test
    void testGreaterThan_FloatAndDouble() {
        // Given: float 和 double 值（类型转换）
        float a = 20.5f;
        double b = 10.5;

        // When: 执行大于比较
        boolean result = OperatorEvaluator.greaterThan(a, b);

        // Then: 应该返回 true（类型转换后比较）
        assertTrue(result);
    }

    @Test
    void testGreaterThanOrEqual_IntAndDouble() {
        // Given: int 和 double 值（类型转换）
        int a = 20;
        double b = 10.5;

        // When: 执行大于等于比较
        boolean result = OperatorEvaluator.greaterThanOrEqual(a, b);

        // Then: 应该返回 true
        assertTrue(result);
    }

    @Test
    void testGreaterThanOrEqual_IntAndDouble_Equal() {
        // Given: int 和 double 值（数值相等，类型转换）
        int a = 10;
        double b = 10.0;

        // When: 执行大于等于比较
        boolean result = OperatorEvaluator.greaterThanOrEqual(a, b);

        // Then: 应该返回 true（类型转换后相等）
        assertTrue(result);
    }

    @Test
    void testGreaterThanOrEqual_IntAndBigDecimal() {
        // Given: int 和 BigDecimal 值（类型转换）
        int a = 20;
        BigDecimal b = new BigDecimal("10.5");

        // When: 执行大于等于比较
        boolean result = OperatorEvaluator.greaterThanOrEqual(a, b);

        // Then: 应该返回 true
        assertTrue(result);
    }

    @Test
    void testGreaterThanOrEqual_NullAndNull() {
        // Given: 两个 null 值
        Object a = null;
        Object b = null;

        // When: 执行大于等于比较
        boolean result = OperatorEvaluator.greaterThanOrEqual(a, b);

        // Then: 应该返回 true（null 等于 null）
        assertTrue(result);
    }

    @Test
    void testNotEqual_IntAndDouble_SameValue() {
        // Given: int 和 double 值（数值相同）
        int a = 10;
        double b = 10.0;

        // When: 执行不等比较
        boolean result = OperatorEvaluator.notEqual(a, b);

        // Then: Objects.equals 会返回 true（类型不同）
        assertTrue(result);
    }

    @Test
    void testNotEqual_IntAndLong_SameValue() {
        // Given: int 和 long 值（数值相同）
        int a = 10;
        long b = 10L;

        // When: 执行不等比较
        boolean result = OperatorEvaluator.notEqual(a, b);

        // Then: Objects.equals 会返回 true（类型不同）
        assertTrue(result);
    }

    @Test
    void testNotEqual_IntAndValue() {
        // Given: null 和非 null 值
        Object a = null;
        int b = 10;

        // When: 执行不等比较
        boolean result = OperatorEvaluator.notEqual(a, b);

        // Then: 应该返回 true
        assertTrue(result);
    }

    @Test
    void testNotEqual_ValueAndNull() {
        // Given: 非 null 值和 null
        int a = 10;
        Object b = null;

        // When: 执行不等比较
        boolean result = OperatorEvaluator.notEqual(a, b);

        // Then: 应该返回 true
        assertTrue(result);
    }

    @Test
    void testLessThan_StringAndString_Equal() {
        // Given: 两个相等的字符串
        String a = "apple";
        String b = "apple";

        // When: 执行小于比较
        boolean result = OperatorEvaluator.lessThan(a, b);

        // Then: 应该返回 false（相等）
        assertFalse(result);
    }

    @Test
    void testLessThan_StringAndString_Greater() {
        // Given: 两个字符串（左大于右）
        String a = "banana";
        String b = "apple";

        // When: 执行小于比较
        boolean result = OperatorEvaluator.lessThan(a, b);

        // Then: 应该返回 false（字典序）
        assertFalse(result);
    }

    @Test
    void testLessThanOrEqual_StringAndString() {
        // Given: 两个字符串（左小于右）
        String a = "apple";
        String b = "banana";

        // When: 执行小于等于比较
        boolean result = OperatorEvaluator.lessThanOrEqual(a, b);

        // Then: 应该返回 true（字典序）
        assertTrue(result);
    }

    @Test
    void testLessThanOrEqual_StringAndString_Equal() {
        // Given: 两个相等的字符串
        String a = "apple";
        String b = "apple";

        // When: 执行小于等于比较
        boolean result = OperatorEvaluator.lessThanOrEqual(a, b);

        // Then: 应该返回 true（相等）
        assertTrue(result);
    }

    @Test
    void testGreaterThan_StringAndString_Equal() {
        // Given: 两个相等的字符串
        String a = "apple";
        String b = "apple";

        // When: 执行大于比较
        boolean result = OperatorEvaluator.greaterThan(a, b);

        // Then: 应该返回 false（相等）
        assertFalse(result);
    }

    @Test
    void testGreaterThan_StringAndString_Less() {
        // Given: 两个字符串（左小于右）
        String a = "apple";
        String b = "banana";

        // When: 执行大于比较
        boolean result = OperatorEvaluator.greaterThan(a, b);

        // Then: 应该返回 false（字典序）
        assertFalse(result);
    }

    @Test
    void testGreaterThanOrEqual_StringAndString() {
        // Given: 两个字符串（左大于右）
        String a = "banana";
        String b = "apple";

        // When: 执行大于等于比较
        boolean result = OperatorEvaluator.greaterThanOrEqual(a, b);

        // Then: 应该返回 true（字典序）
        assertTrue(result);
    }

    @Test
    void testGreaterThanOrEqual_StringAndString_Equal() {
        // Given: 两个相等的字符串
        String a = "apple";
        String b = "apple";

        // When: 执行大于等于比较
        boolean result = OperatorEvaluator.greaterThanOrEqual(a, b);

        // Then: 应该返回 true（相等）
        assertTrue(result);
    }

    // ==================== 正则运算测试 ====================

    @Test
    void testRegexMatch_StringAndPattern() {
        // Given: 文本和正则模式
        String text = "Hello World";
        String pattern = "Hello";

        // When: 执行正则匹配
        boolean result = OperatorEvaluator.regexMatch(text, pattern);

        // Then: 应该返回 true
        assertTrue(result);
    }

    @Test
    void testRegexMatch_StringAndPattern_NoMatch() {
        // Given: 文本和正则模式（不匹配）
        String text = "Hello World";
        String pattern = "Goodbye";

        // When: 执行正则匹配
        boolean result = OperatorEvaluator.regexMatch(text, pattern);

        // Then: 应该返回 false
        assertFalse(result);
    }

    @Test
    void testRegexMatch_StringAndPattern_Complex() {
        // Given: 文本和复杂正则模式
        String text = "Email: test@example.com";
        String pattern = "\\w+@\\w+\\.\\w+";

        // When: 执行正则匹配
        boolean result = OperatorEvaluator.regexMatch(text, pattern);

        // Then: 应该返回 true
        assertTrue(result);
    }

    @Test
    void testRegexMatch_StringAndPatternObject() {
        // Given: 文本和 Pattern 对象
        String text = "Hello World";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("Hello");

        // When: 执行正则匹配
        boolean result = OperatorEvaluator.regexMatch(text, pattern);

        // Then: 应该返回 true
        assertTrue(result);
    }

    @Test
    void testRegexMatch_NullText() {
        // Given: null 文本和正则模式
        String text = null;
        String pattern = "Hello";

        // When: 执行正则匹配
        boolean result = OperatorEvaluator.regexMatch(text, pattern);

        // Then: 应该返回 false
        assertFalse(result);
    }

    @Test
    void testRegexMatch_NullPattern() {
        // Given: 文本和 null 正则模式
        String text = "Hello World";
        String pattern = null;

        // When: 执行正则匹配
        boolean result = OperatorEvaluator.regexMatch(text, pattern);

        // Then: 应该返回 false
        assertFalse(result);
    }

    @Test
    void testRegexMatch_NonStringText() {
        // Given: 非字符串文本（会调用 toString）
        Integer text = 12345;
        String pattern = "123";

        // When: 执行正则匹配
        boolean result = OperatorEvaluator.regexMatch(text, pattern);

        // Then: 应该返回 true（toString 后匹配）
        assertTrue(result);
    }

    @Test
    void testRegexNotMatch_StringAndPattern() {
        // Given: 文本和正则模式（不匹配）
        String text = "Hello World";
        String pattern = "Goodbye";

        // When: 执行正则不匹配
        boolean result = OperatorEvaluator.regexNotMatch(text, pattern);

        // Then: 应该返回 true
        assertTrue(result);
    }

    @Test
    void testRegexNotMatch_StringAndPattern_Match() {
        // Given: 文本和正则模式（匹配）
        String text = "Hello World";
        String pattern = "Hello";

        // When: 执行正则不匹配
        boolean result = OperatorEvaluator.regexNotMatch(text, pattern);

        // Then: 应该返回 false
        assertFalse(result);
    }

    @Test
    void testRegexNotMatch_NullText() {
        // Given: null 文本和正则模式
        String text = null;
        String pattern = "Hello";

        // When: 执行正则不匹配
        boolean result = OperatorEvaluator.regexNotMatch(text, pattern);

        // Then: 应该返回 true（null 不匹配任何模式）
        assertTrue(result);
    }

    @Test
    void testRegexMatch_InvalidPattern() {
        // Given: 文本和无效的正则模式
        String text = "Hello World";
        String pattern = "[invalid";

        // When/Then: 应该抛出异常
        assertThrows(Exception.class, () -> {
            OperatorEvaluator.regexMatch(text, pattern);
        });
    }

    @Test
    void testRegexMatch_EmptyString() {
        // Given: 空字符串和正则模式
        String text = "";
        String pattern = ".*";

        // When: 执行正则匹配
        boolean result = OperatorEvaluator.regexMatch(text, pattern);

        // Then: 应该返回 true（空字符串匹配 .*）
        assertTrue(result);
    }

    @Test
    void testRegexMatch_EmptyPattern() {
        // Given: 文本和空正则模式
        String text = "Hello World";
        String pattern = "";

        // When: 执行正则匹配
        boolean result = OperatorEvaluator.regexMatch(text, pattern);

        // Then: 应该返回 true（空模式匹配任何字符串）
        assertTrue(result);
    }

    @Test
    void testRegexMatch_DigitPattern() {
        // Given: 文本和数字正则模式
        String text = "Phone: 123-456-7890";
        String pattern = "\\d{3}-\\d{3}-\\d{4}";

        // When: 执行正则匹配
        boolean result = OperatorEvaluator.regexMatch(text, pattern);

        // Then: 应该返回 true
        assertTrue(result);
    }

    @Test
    void testRegexMatch_WordBoundary() {
        // Given: 文本和单词边界正则模式
        String text = "Hello World";
        String pattern = "\\bWorld\\b";

        // When: 执行正则匹配
        boolean result = OperatorEvaluator.regexMatch(text, pattern);

        // Then: 应该返回 true
        assertTrue(result);
    }

    @Test
    void testRegexMatch_StartOfString() {
        // Given: 文本和开头匹配正则模式
        String text = "Hello World";
        String pattern = "^Hello";

        // When: 执行正则匹配
        boolean result = OperatorEvaluator.regexMatch(text, pattern);

        // Then: 应该返回 true
        assertTrue(result);
    }

    @Test
    void testRegexMatch_EndOfString() {
        // Given: 文本和结尾匹配正则模式
        String text = "Hello World";
        String pattern = "World$";

        // When: 执行正则匹配
        boolean result = OperatorEvaluator.regexMatch(text, pattern);

        // Then: 应该返回 true
        assertTrue(result);
    }

    @Test
    void testRegexMatch_Quantifier() {
        // Given: 文本和量词正则模式
        String text = "aaaab";
        String pattern = "a+";

        // When: 执行正则匹配
        boolean result = OperatorEvaluator.regexMatch(text, pattern);

        // Then: 应该返回 true
        assertTrue(result);
    }

    @Test
    void testRegexMatch_CharacterClass() {
        // Given: 文本和字符类正则模式
        String text = "Hello123";
        String pattern = "[a-zA-Z]+\\d+";

        // When: 执行正则匹配
        boolean result = OperatorEvaluator.regexMatch(text, pattern);

        // Then: 应该返回 true
        assertTrue(result);
    }

    @Test
    void testRegexMatch_Group() {
        // Given: 文本和分组正则模式
        String text = "abc123";
        String pattern = "(\\w+)(\\d+)";

        // When: 执行正则匹配
        boolean result = OperatorEvaluator.regexMatch(text, pattern);

        // Then: 应该返回 true
        assertTrue(result);
    }

    @Test
    void testRegexMatch_Unicode() {
        // Given: 文本和 Unicode 正则模式
        String text = "你好世界";
        String pattern = "\\u4f60\\u597d";

        // When: 执行正则匹配
        boolean result = OperatorEvaluator.regexMatch(text, pattern);

        // Then: 应该返回 true
        assertTrue(result);
    }

    @Test
    void testRegexMatch_ChineseCharacters() {
        // Given: 文本和中文字符正则模式
        String text = "Hello 世界";
        String pattern = "\\p{IsHan}+";

        // When: 执行正则匹配
        boolean result = OperatorEvaluator.regexMatch(text, pattern);

        // Then: 应该返回 true
        assertTrue(result);
    }

    @Test
    void testRegexMatch_MultipleMatches() {
        // Given: 文本和正则模式（文本中有多个匹配）
        String text = "abc123def456";
        String pattern = "\\d+";

        // When: 执行正则匹配（使用 find，应该找到第一个匹配）
        boolean result = OperatorEvaluator.regexMatch(text, pattern);

        // Then: 应该返回 true（至少找到一个匹配）
        assertTrue(result);
    }

    @Test
    void testRegexMatch_NonStringPattern() {
        // Given: 文本和非字符串模式对象（会抛出异常）
        String text = "Hello World";
        Integer pattern = 123;

        // When/Then: 应该抛出异常
        assertThrows(TemplateRuntimeException.class, () -> {
            OperatorEvaluator.regexMatch(text, pattern);
        });
    }

    @Test
    void testRegexNotMatch_EmptyString() {
        // Given: 空字符串和正则模式（不匹配）
        String text = "";
        String pattern = "Hello";

        // When: 执行正则不匹配
        boolean result = OperatorEvaluator.regexNotMatch(text, pattern);

        // Then: 应该返回 true
        assertTrue(result);
    }

    @Test
    void testRegexNotMatch_NullPattern() {
        // Given: 文本和 null 正则模式
        String text = "Hello World";
        String pattern = null;

        // When: 执行正则不匹配
        boolean result = OperatorEvaluator.regexNotMatch(text, pattern);

        // Then: 应该返回 true（null 不匹配）
        assertTrue(result);
    }

    @Test
    void testRegexNotMatch_ComplexPattern() {
        // Given: 文本和复杂正则模式（不匹配）
        String text = "Invalid email";
        String pattern = "\\w+@\\w+\\.\\w+";

        // When: 执行正则不匹配
        boolean result = OperatorEvaluator.regexNotMatch(text, pattern);

        // Then: 应该返回 true
        assertTrue(result);
    }

    // ==================== 正则替换测试 ====================

    @Test
    void testRegexReplace_SimpleReplacement() {
        // Given: 文本、正则模式和替换字符串
        String text = "Hello World";
        String pattern = "World";
        String replacement = "Java";

        // When: 执行正则替换
        String result = OperatorEvaluator.regexReplace(text, pattern, replacement);

        // Then: 应该返回替换后的字符串
        assertEquals("Hello Java", result);
    }

    @Test
    void testRegexReplace_MultipleMatches() {
        // Given: 文本、正则模式和替换字符串（多个匹配）
        String text = "cat dog cat dog";
        String pattern = "cat";
        String replacement = "bird";

        // When: 执行正则替换（替换所有匹配）
        String result = OperatorEvaluator.regexReplace(text, pattern, replacement);

        // Then: 应该返回替换后的字符串（所有匹配都被替换）
        assertEquals("bird dog bird dog", result);
    }

    @Test
    void testRegexReplace_DigitPattern() {
        // Given: 文本、数字正则模式和替换字符串
        String text = "Phone: 123-456-7890";
        String pattern = "\\d";
        String replacement = "X";

        // When: 执行正则替换
        String result = OperatorEvaluator.regexReplace(text, pattern, replacement);

        // Then: 应该返回替换后的字符串（所有数字都被替换）
        assertEquals("Phone: XXX-XXX-XXXX", result);
    }

    @Test
    void testRegexReplace_Whitespace() {
        // Given: 文本、空白字符正则模式和替换字符串
        String text = "Hello   World";
        String pattern = "\\s+";
        String replacement = " ";

        // When: 执行正则替换
        String result = OperatorEvaluator.regexReplace(text, pattern, replacement);

        // Then: 应该返回替换后的字符串（多个空白字符被替换为单个空格）
        assertEquals("Hello World", result);
    }

    @Test
    void testRegexReplace_EmptyReplacement() {
        // Given: 文本、正则模式和空替换字符串
        String text = "Hello World";
        String pattern = "\\s";
        String replacement = "";

        // When: 执行正则替换
        String result = OperatorEvaluator.regexReplace(text, pattern, replacement);

        // Then: 应该返回替换后的字符串（空白字符被删除）
        assertEquals("HelloWorld", result);
    }

    @Test
    void testRegexReplace_NoMatch() {
        // Given: 文本、正则模式和替换字符串（无匹配）
        String text = "Hello World";
        String pattern = "Goodbye";
        String replacement = "Java";

        // When: 执行正则替换
        String result = OperatorEvaluator.regexReplace(text, pattern, replacement);

        // Then: 应该返回原始字符串（无匹配，不替换）
        assertEquals("Hello World", result);
    }

    @Test
    void testRegexReplace_NullText() {
        // Given: null 文本、正则模式和替换字符串
        String text = null;
        String pattern = "Hello";
        String replacement = "Hi";

        // When: 执行正则替换
        String result = OperatorEvaluator.regexReplace(text, pattern, replacement);

        // Then: 应该返回 null
        assertNull(result);
    }

    @Test
    void testRegexReplace_NullPattern() {
        // Given: 文本、null 正则模式和替换字符串
        String text = "Hello World";
        String pattern = null;
        String replacement = "Hi";

        // When/Then: 应该抛出异常
        assertThrows(TemplateRuntimeException.class, () -> {
            OperatorEvaluator.regexReplace(text, pattern, replacement);
        });
    }

    @Test
    void testRegexReplace_NullReplacement() {
        // Given: 文本、正则模式和 null 替换字符串
        String text = "Hello World";
        String pattern = "Hello";
        String replacement = null;

        // When/Then: 应该抛出异常
        assertThrows(TemplateRuntimeException.class, () -> {
            OperatorEvaluator.regexReplace(text, pattern, replacement);
        });
    }

    @Test
    void testRegexReplace_PatternObject() {
        // Given: 文本、Pattern 对象和替换字符串
        String text = "Hello World";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("World");
        String replacement = "Java";

        // When: 执行正则替换
        String result = OperatorEvaluator.regexReplace(text, pattern, replacement);

        // Then: 应该返回替换后的字符串
        assertEquals("Hello Java", result);
    }

    @Test
    void testRegexReplace_NonStringText() {
        // Given: 非字符串文本、正则模式和替换字符串
        Integer text = 12345;
        String pattern = "3";
        String replacement = "X";

        // When: 执行正则替换（会调用 toString）
        String result = OperatorEvaluator.regexReplace(text, pattern, replacement);

        // Then: 应该返回替换后的字符串
        assertEquals("12X45", result);
    }

    @Test
    void testRegexReplace_NonStringReplacement() {
        // Given: 文本、正则模式和非字符串替换值
        String text = "Hello 123";
        String pattern = "\\d+";
        Integer replacement = 999;

        // When: 执行正则替换（会调用 toString）
        String result = OperatorEvaluator.regexReplace(text, pattern, replacement);

        // Then: 应该返回替换后的字符串
        assertEquals("Hello 999", result);
    }

    @Test
    void testRegexReplace_ComplexPattern() {
        // Given: 文本、复杂正则模式和替换字符串
        String text = "Email: test@example.com";
        String pattern = "\\w+@\\w+\\.\\w+";
        String replacement = "[EMAIL]";

        // When: 执行正则替换
        String result = OperatorEvaluator.regexReplace(text, pattern, replacement);

        // Then: 应该返回替换后的字符串
        assertEquals("Email: [EMAIL]", result);
    }

    @Test
    void testRegexReplace_GroupReference() {
        // Given: 文本、带分组的正则模式和替换字符串（使用 $1 引用分组）
        String text = "John Doe";
        String pattern = "(\\w+) (\\w+)";
        String replacement = "$2, $1";

        // When: 执行正则替换
        String result = OperatorEvaluator.regexReplace(text, pattern, replacement);

        // Then: 应该返回替换后的字符串（交换名字和姓氏）
        assertEquals("Doe, John", result);
    }

    @Test
    void testRegexReplace_Unicode() {
        // Given: 文本、Unicode 正则模式和替换字符串
        String text = "你好世界";
        String pattern = "世界";
        String replacement = "Java";

        // When: 执行正则替换
        String result = OperatorEvaluator.regexReplace(text, pattern, replacement);

        // Then: 应该返回替换后的字符串
        assertEquals("你好Java", result);
    }

    @Test
    void testRegexReplaceFirst_SimpleReplacement() {
        // Given: 文本、正则模式和替换字符串
        String text = "cat dog cat dog";
        String pattern = "cat";
        String replacement = "bird";

        // When: 执行正则替换（仅替换第一个匹配）
        String result = OperatorEvaluator.regexReplaceFirst(text, pattern, replacement);

        // Then: 应该返回替换后的字符串（仅第一个匹配被替换）
        assertEquals("bird dog cat dog", result);
    }

    @Test
    void testRegexReplaceFirst_MultipleMatches() {
        // Given: 文本、正则模式和替换字符串（多个匹配）
        String text = "123 456 789";
        String pattern = "\\d+";
        String replacement = "XXX";

        // When: 执行正则替换（仅替换第一个匹配）
        String result = OperatorEvaluator.regexReplaceFirst(text, pattern, replacement);

        // Then: 应该返回替换后的字符串（仅第一个匹配被替换）
        assertEquals("XXX 456 789", result);
    }

    @Test
    void testRegexReplaceFirst_NoMatch() {
        // Given: 文本、正则模式和替换字符串（无匹配）
        String text = "Hello World";
        String pattern = "Goodbye";
        String replacement = "Hi";

        // When: 执行正则替换（仅替换第一个匹配）
        String result = OperatorEvaluator.regexReplaceFirst(text, pattern, replacement);

        // Then: 应该返回原始字符串（无匹配，不替换）
        assertEquals("Hello World", result);
    }

    @Test
    void testRegexReplaceFirst_NullText() {
        // Given: null 文本、正则模式和替换字符串
        String text = null;
        String pattern = "Hello";
        String replacement = "Hi";

        // When: 执行正则替换（仅替换第一个匹配）
        String result = OperatorEvaluator.regexReplaceFirst(text, pattern, replacement);

        // Then: 应该返回 null
        assertNull(result);
    }

    @Test
    void testRegexReplaceFirst_NullPattern() {
        // Given: 文本、null 正则模式和替换字符串
        String text = "Hello World";
        String pattern = null;
        String replacement = "Hi";

        // When/Then: 应该抛出异常
        assertThrows(TemplateRuntimeException.class, () -> {
            OperatorEvaluator.regexReplaceFirst(text, pattern, replacement);
        });
    }

    @Test
    void testRegexReplaceFirst_NullReplacement() {
        // Given: 文本、正则模式和 null 替换字符串
        String text = "Hello World";
        String pattern = "Hello";
        String replacement = null;

        // When/Then: 应该抛出异常
        assertThrows(TemplateRuntimeException.class, () -> {
            OperatorEvaluator.regexReplaceFirst(text, pattern, replacement);
        });
    }

    @Test
    void testRegexReplaceFirst_EmptyString() {
        // Given: 空字符串、正则模式和替换字符串
        String text = "";
        String pattern = ".*";
        String replacement = "Hello";

        // When: 执行正则替换（仅替换第一个匹配）
        String result = OperatorEvaluator.regexReplaceFirst(text, pattern, replacement);

        // Then: 应该返回替换后的字符串
        assertEquals("Hello", result);
    }

    // ==================== 逻辑运算测试 ====================

    @Test
    void testAnd_BothTrue() {
        // Given: 两个 true
        // When: 执行逻辑与
        boolean result = OperatorEvaluator.and(true, true);
        // Then: 应该返回 true
        assertTrue(result);
    }

    @Test
    void testAnd_LeftTrueRightFalse() {
        // Given: true 和 false
        // When: 执行逻辑与
        boolean result = OperatorEvaluator.and(true, false);
        // Then: 应该返回 false
        assertFalse(result);
    }

    @Test
    void testAnd_LeftFalseRightTrue() {
        // Given: false 和 true
        // When: 执行逻辑与
        boolean result = OperatorEvaluator.and(false, true);
        // Then: 应该返回 false
        assertFalse(result);
    }

    @Test
    void testAnd_BothFalse() {
        // Given: 两个 false
        // When: 执行逻辑与
        boolean result = OperatorEvaluator.and(false, false);
        // Then: 应该返回 false
        assertFalse(result);
    }

    @Test
    void testOr_BothTrue() {
        // Given: 两个 true
        // When: 执行逻辑或
        boolean result = OperatorEvaluator.or(true, true);
        // Then: 应该返回 true
        assertTrue(result);
    }

    @Test
    void testOr_LeftTrueRightFalse() {
        // Given: true 和 false
        // When: 执行逻辑或
        boolean result = OperatorEvaluator.or(true, false);
        // Then: 应该返回 true
        assertTrue(result);
    }

    @Test
    void testOr_LeftFalseRightTrue() {
        // Given: false 和 true
        // When: 执行逻辑或
        boolean result = OperatorEvaluator.or(false, true);
        // Then: 应该返回 true
        assertTrue(result);
    }

    @Test
    void testOr_BothFalse() {
        // Given: 两个 false
        // When: 执行逻辑或
        boolean result = OperatorEvaluator.or(false, false);
        // Then: 应该返回 false
        assertFalse(result);
    }

    @Test
    void testNot_True() {
        // Given: true
        // When: 执行逻辑非
        boolean result = OperatorEvaluator.not(true);
        // Then: 应该返回 false
        assertFalse(result);
    }

    @Test
    void testNot_False() {
        // Given: false
        // When: 执行逻辑非
        boolean result = OperatorEvaluator.not(false);
        // Then: 应该返回 true
        assertTrue(result);
    }

    // ---- 短路求值语义：左操作数即可决定结果时，与/或的结果与右操作数无关 ----

    @Test
    void testAnd_ShortCircuitSemantics_LeftFalse_RightIrrelevant() {
        // Given: 左为 false（短路：右操作数不参与即知结果为 false）
        // When: 执行逻辑与，右为任意值
        assertFalse(OperatorEvaluator.and(false, true));
        assertFalse(OperatorEvaluator.and(false, false));
        assertFalse(OperatorEvaluator.and(false, 1));
        assertFalse(OperatorEvaluator.and(false, "x"));
    }

    @Test
    void testOr_ShortCircuitSemantics_LeftTrue_RightIrrelevant() {
        // Given: 左为 true（短路：右操作数不参与即知结果为 true）
        // When: 执行逻辑或，右为任意值
        assertTrue(OperatorEvaluator.or(true, false));
        assertTrue(OperatorEvaluator.or(true, true));
        assertTrue(OperatorEvaluator.or(true, 0));
        assertTrue(OperatorEvaluator.or(true, ""));
    }

    // ---- 非布尔值逻辑运算（truthy/falsy） ----

    @Test
    void testAnd_TruthyFalsy_NumberZero() {
        // Given: 0 为 falsy
        assertFalse(OperatorEvaluator.and(0, true));
        assertFalse(OperatorEvaluator.and(true, 0));
        assertFalse(OperatorEvaluator.and(0, 1));
        assertFalse(OperatorEvaluator.and(1, 0));
        assertTrue(OperatorEvaluator.and(1, 1));
    }

    @Test
    void testAnd_TruthyFalsy_DoubleZero() {
        assertFalse(OperatorEvaluator.and(0.0, true));
        assertFalse(OperatorEvaluator.and(true, 0.0));
    }

    @Test
    void testOr_TruthyFalsy_NumberZero() {
        assertFalse(OperatorEvaluator.or(0, 0));
        assertTrue(OperatorEvaluator.or(0, 1));
        assertTrue(OperatorEvaluator.or(1, 0));
        assertTrue(OperatorEvaluator.or(1, 1));
    }

    @Test
    void testNot_TruthyFalsy_Number() {
        assertTrue(OperatorEvaluator.not(0));
        assertTrue(OperatorEvaluator.not(0.0));
        assertFalse(OperatorEvaluator.not(1));
        assertFalse(OperatorEvaluator.not(-1));
    }

    @Test
    void testAnd_TruthyFalsy_Null() {
        // Given: null 为 falsy
        assertFalse(OperatorEvaluator.and(null, true));
        assertFalse(OperatorEvaluator.and(true, null));
        assertFalse(OperatorEvaluator.and(null, null));
    }

    @Test
    void testOr_TruthyFalsy_Null() {
        assertFalse(OperatorEvaluator.or(null, null));
        assertTrue(OperatorEvaluator.or(null, true));
        assertTrue(OperatorEvaluator.or(true, null));
    }

    @Test
    void testNot_TruthyFalsy_Null() {
        assertTrue(OperatorEvaluator.not(null));
    }

    @Test
    void testAnd_TruthyFalsy_EmptyString() {
        // Given: 空字符串为 falsy
        assertFalse(OperatorEvaluator.and("", true));
        assertFalse(OperatorEvaluator.and(true, ""));
        assertTrue(OperatorEvaluator.and("a", true));
        assertTrue(OperatorEvaluator.and(true, "a"));
    }

    @Test
    void testOr_TruthyFalsy_EmptyString() {
        assertFalse(OperatorEvaluator.or("", ""));
        assertTrue(OperatorEvaluator.or("", "x"));
        assertTrue(OperatorEvaluator.or("x", ""));
    }

    @Test
    void testNot_TruthyFalsy_String() {
        assertTrue(OperatorEvaluator.not(""));
        assertFalse(OperatorEvaluator.not("hello"));
    }

    @Test
    void testAnd_TruthyFalsy_EmptyCollection() {
        List<Object> empty = Collections.emptyList();
        List<Object> nonEmpty = Collections.singletonList(1);
        assertFalse(OperatorEvaluator.and(empty, true));
        assertFalse(OperatorEvaluator.and(true, empty));
        assertTrue(OperatorEvaluator.and(nonEmpty, true));
        assertTrue(OperatorEvaluator.and(true, nonEmpty));
    }

    @Test
    void testOr_TruthyFalsy_EmptyCollection() {
        List<Object> empty = Collections.emptyList();
        List<Object> nonEmpty = Collections.singletonList(1);
        assertFalse(OperatorEvaluator.or(empty, empty));
        assertTrue(OperatorEvaluator.or(empty, nonEmpty));
        assertTrue(OperatorEvaluator.or(nonEmpty, empty));
    }

    @Test
    void testNot_TruthyFalsy_Collection() {
        assertTrue(OperatorEvaluator.not(Collections.emptyList()));
        assertFalse(OperatorEvaluator.not(Collections.singletonList(1)));
    }

    @Test
    void testAnd_TruthyFalsy_EmptyMap() {
        Map<String, Integer> empty = Collections.emptyMap();
        Map<String, Integer> nonEmpty = Collections.singletonMap("a", 1);
        assertFalse(OperatorEvaluator.and(empty, true));
        assertFalse(OperatorEvaluator.and(true, empty));
        assertTrue(OperatorEvaluator.and(nonEmpty, true));
        assertTrue(OperatorEvaluator.and(true, nonEmpty));
    }

    @Test
    void testOr_TruthyFalsy_EmptyMap() {
        Map<String, Integer> empty = Collections.emptyMap();
        Map<String, Integer> nonEmpty = Collections.singletonMap("a", 1);
        assertFalse(OperatorEvaluator.or(empty, empty));
        assertTrue(OperatorEvaluator.or(empty, nonEmpty));
        assertTrue(OperatorEvaluator.or(nonEmpty, empty));
    }

    @Test
    void testNot_TruthyFalsy_Map() {
        assertTrue(OperatorEvaluator.not(Collections.emptyMap()));
        assertFalse(OperatorEvaluator.not(Collections.singletonMap("a", 1)));
    }

    @Test
    void testToBoolean_PublicApi() {
        // toBoolean 为 public，校验 truthy/falsy 规则一致
        assertFalse(OperatorEvaluator.toBoolean(null));
        assertFalse(OperatorEvaluator.toBoolean(false));
        assertTrue(OperatorEvaluator.toBoolean(true));
        assertFalse(OperatorEvaluator.toBoolean(0));
        assertFalse(OperatorEvaluator.toBoolean(0.0));
        assertTrue(OperatorEvaluator.toBoolean(1));
        assertTrue(OperatorEvaluator.toBoolean(-1));
        assertFalse(OperatorEvaluator.toBoolean(""));
        assertTrue(OperatorEvaluator.toBoolean("x"));
        assertFalse(OperatorEvaluator.toBoolean(Collections.emptyList()));
        assertTrue(OperatorEvaluator.toBoolean(Collections.singletonList(1)));
        assertFalse(OperatorEvaluator.toBoolean(Collections.emptyMap()));
        assertTrue(OperatorEvaluator.toBoolean(Collections.singletonMap("a", 1)));
    }
}
