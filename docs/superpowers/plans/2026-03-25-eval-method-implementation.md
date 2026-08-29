# Eval Method Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add eval method family to EasyTL for immediate expression evaluation with type conversion support.

**Architecture:** Add `Eval` class as the core evaluator with `parseEvalSource()` in Parser for statement parsing, `TypeConverter` for type conversions, and `TypeConversionException` for conversion errors. EasyTL delegates to Eval.

**Tech Stack:** Java 8, JUnit 5, no third-party dependencies

---

## File Structure

| File | Purpose |
|------|---------|
| `src/main/java/org/dromara/easytl/exception/TypeConversionException.java` | Type conversion error handling |
| `src/main/java/org/dromara/easytl/TypeConverter.java` | Type conversion utility |
| `src/main/java/org/dromara/easytl/Eval.java` | Core eval implementation |
| `src/main/java/org/dromara/easytl/EasyTL.java` | Add eval facade methods |
| `src/main/java/org/dromara/easytl/parser/Parser.java` | Add parseEvalSource() |
| `src/test/java/org/dromara/easytl/EvalTest.java` | Unit tests |

---

### Task 1: TypeConversionException

**Files:**
- Create: `src/main/java/org/dromara/easytl/exception/TypeConversionException.java`
- Test: `src/test/java/org/dromara/easytl/exception/TypeConversionExceptionTest.java`

- [ ] **Step 1: Write the failing test**

```java
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
    }

    @Test
    void testInheritance() {
        TypeConversionException e = new TypeConversionException("test");
        assertTrue(e instanceof TemplateRuntimeException);
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn test -Dtest=TypeConversionExceptionTest -q`
Expected: FAIL - class not found

- [ ] **Step 3: Write minimal implementation**

```java
package org.dromara.easytl.exception;

/**
 * 类型转换异常
 * <p>
 * 当 eval 方法的返回值类型转换失败时抛出此异常
 * </p>
 *
 * @author EasyTL Team
 * @since 1.0.0
 */
public class TypeConversionException extends TemplateRuntimeException {

    private static final long serialVersionUID = 1L;

    private final String sourceType;
    private final String targetType;

    /**
     * 构造函数
     *
     * @param message 异常信息
     */
    public TypeConversionException(String message) {
        super(message);
        this.sourceType = null;
        this.targetType = null;
    }

    /**
     * 构造函数，指定源类型和目标类型
     *
     * @param sourceType 源类型名称
     * @param targetType 目标类型名称
     * @param reason 失败原因
     */
    public TypeConversionException(String sourceType, String targetType, String reason) {
        super(String.format("Cannot convert %s to %s: %s", sourceType, targetType, reason));
        this.sourceType = sourceType;
        this.targetType = targetType;
    }

    /**
     * 获取源类型名称
     *
     * @return 源类型名称，可能为 null
     */
    public String getSourceType() {
        return sourceType;
    }

    /**
     * 获取目标类型名称
     *
     * @return 目标类型名称，可能为 null
     */
    public String getTargetType() {
        return targetType;
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `mvn test -Dtest=TypeConversionExceptionTest -q`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/java/org/dromara/easytl/exception/TypeConversionException.java src/test/java/org/dromara/easytl/exception/TypeConversionExceptionTest.java
git commit -m "feat: add TypeConversionException for eval type conversion errors"
```

---

### Task 2: TypeConverter - Basic Conversions

**Files:**
- Create: `src/main/java/org/dromara/easytl/TypeConverter.java`
- Test: `src/test/java/org/dromara/easytl/TypeConverterTest.java`

- [ ] **Step 1: Write the failing tests for basic conversions**

```java
package org.dromara.easytl;

import org.dromara.easytl.exception.TypeConversionException;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class TypeConverterTest {

    // ========== Null handling ==========

    @Test
    void testNullToObject() {
        assertNull(TypeConverter.convert(null, Object.class));
    }

    @Test
    void testNullToString() {
        assertNull(TypeConverter.convert(null, String.class));
    }

    @Test
    void testNullToPrimitive_throws() {
        assertThrows(TypeConversionException.class, () -> TypeConverter.convert(null, int.class));
    }

    // ========== Exact match ==========

    @Test
    void testExactMatch_Integer() {
        Integer result = TypeConverter.convert(42, Integer.class);
        assertEquals(Integer.valueOf(42), result);
    }

    @Test
    void testExactMatch_String() {
        String result = TypeConverter.convert("hello", String.class);
        assertEquals("hello", result);
    }

    @Test
    void testExactMatch_Boolean() {
        Boolean result = TypeConverter.convert(true, Boolean.class);
        assertEquals(Boolean.TRUE, result);
    }

    // ========== String special case ==========

    @Test
    void testAnyToString_Integer() {
        String result = TypeConverter.convert(42, String.class);
        assertEquals("42", result);
    }

    @Test
    void testAnyToString_Boolean() {
        String result = TypeConverter.convert(true, String.class);
        assertEquals("true", result);
    }

    @Test
    void testAnyToString_List() {
        String result = TypeConverter.convert(Arrays.asList(1, 2, 3), String.class);
        assertEquals("[1, 2, 3]", result);
    }

    // ========== Subclass to parent ==========

    @Test
    void testSubclassToParent_ListToCollection() {
        Collection<?> result = TypeConverter.convert(Arrays.asList(1, 2, 3), Collection.class);
        assertEquals(3, result.size());
    }

    @Test
    void testSubclassToParent_ArrayListToList() {
        List<Integer> list = new ArrayList<>(Arrays.asList(1, 2, 3));
        List<?> result = TypeConverter.convert(list, List.class);
        assertSame(list, result);
    }

    // ========== Incompatible types ==========

    @Test
    void testIncompatible_StringToInteger() {
        assertThrows(TypeConversionException.class, () -> TypeConverter.convert("abc", Integer.class));
    }

    @Test
    void testIncompatible_StringToBoolean() {
        assertThrows(TypeConversionException.class, () -> TypeConverter.convert("hello", Boolean.class));
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn test -Dtest=TypeConverterTest -q`
Expected: FAIL - class not found

- [ ] **Step 3: Write minimal implementation**

```java
package org.dromara.easytl;

import org.dromara.easytl.exception.TypeConversionException;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * 类型转换工具类
 * <p>
 * 用于 eval 方法的返回值类型转换
 * </p>
 *
 * @author EasyTL Team
 * @since 1.0.0
 */
public final class TypeConverter {

    private TypeConverter() {
        // 工具类，禁止实例化
    }

    /**
     * 将值转换为目标类型
     *
     * @param value 源值
     * @param targetType 目标类型
     * @param <T> 目标类型泛型
     * @return 转换后的值
     * @throws TypeConversionException 如果转换失败
     */
    public static <T> T convert(Object value, Class<T> targetType) {
        if (value == null) {
            if (targetType.isPrimitive()) {
                throw new TypeConversionException(
                    "null", targetType.getName(), "Cannot convert null to primitive type");
            }
            return null;
        }

        Class<?> sourceType = value.getClass();

        // 精确匹配（包括子类到父类）
        if (targetType.isAssignableFrom(sourceType)) {
            return targetType.cast(value);
        }

        // String 特殊处理：任意类型可转 String
        if (targetType == String.class) {
            return targetType.cast(value.toString());
        }

        // 数值类型转换
        if (isNumericConversion(sourceType, targetType)) {
            return convertNumeric(value, targetType);
        }

        // 不支持的转换
        throw new TypeConversionException(
            sourceType.getName(), targetType.getName(), "Incompatible types");
    }

    /**
     * 检查是否为数值类型转换
     */
    static boolean isNumericConversion(Class<?> sourceType, Class<?> targetType) {
        return isNumeric(sourceType) && isNumeric(targetType);
    }

    /**
     * 检查类型是否为数值类型
     */
    static boolean isNumeric(Class<?> type) {
        return type == Byte.class || type == byte.class ||
               type == Short.class || type == short.class ||
               type == Integer.class || type == int.class ||
               type == Long.class || type == long.class ||
               type == Float.class || type == float.class ||
               type == Double.class || type == double.class ||
               type == BigInteger.class || type == BigDecimal.class;
    }

    /**
     * 数值类型转换
     */
    @SuppressWarnings("unchecked")
    static <T> T convertNumeric(Object value, Class<T> targetType) {
        Number num = (Number) value;

        // 向上转换（安全）
        if (targetType == Long.class || targetType == long.class) {
            return (T) Long.valueOf(num.longValue());
        }
        if (targetType == Float.class || targetType == float.class) {
            return (T) Float.valueOf(num.floatValue());
        }
        if (targetType == Double.class || targetType == double.class) {
            return (T) Double.valueOf(num.doubleValue());
        }
        if (targetType == BigInteger.class) {
            if (value instanceof BigInteger) {
                return (T) value;
            }
            if (value instanceof BigDecimal) {
                return (T) ((BigDecimal) value).toBigInteger();
            }
            return (T) BigInteger.valueOf(num.longValue());
        }
        if (targetType == BigDecimal.class) {
            if (value instanceof BigDecimal) {
                return (T) value;
            }
            if (value instanceof BigInteger) {
                return (T) new BigDecimal((BigInteger) value);
            }
            if (value instanceof Double || value instanceof Float) {
                return (T) BigDecimal.valueOf(num.doubleValue());
            }
            return (T) BigDecimal.valueOf(num.longValue());
        }

        // 向下转换（可能溢出）
        if (targetType == Integer.class || targetType == int.class) {
            long longVal = num.longValue();
            if (longVal < Integer.MIN_VALUE || longVal > Integer.MAX_VALUE) {
                throw new TypeConversionException(
                    "Long", "Integer", "Value " + longVal + " out of range");
            }
            return (T) Integer.valueOf((int) longVal);
        }
        if (targetType == Short.class || targetType == short.class) {
            long longVal = num.longValue();
            if (longVal < Short.MIN_VALUE || longVal > Short.MAX_VALUE) {
                throw new TypeConversionException(
                    "Long", "Short", "Value " + longVal + " out of range");
            }
            return (T) Short.valueOf((short) longVal);
        }
        if (targetType == Byte.class || targetType == byte.class) {
            long longVal = num.longValue();
            if (longVal < Byte.MIN_VALUE || longVal > Byte.MAX_VALUE) {
                throw new TypeConversionException(
                    "Long", "Byte", "Value " + longVal + " out of range");
            }
            return (T) Byte.valueOf((byte) longVal);
        }

        throw new TypeConversionException(
            value.getClass().getName(), targetType.getName(), "Unsupported numeric conversion");
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `mvn test -Dtest=TypeConverterTest -q`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/java/org/dromara/easytl/TypeConverter.java src/test/java/org/dromara/easytl/TypeConverterTest.java
git commit -m "feat: add TypeConverter for eval type conversions"
```

---

### Task 3: TypeConverter - Numeric Conversions

**Files:**
- Modify: `src/test/java/org/dromara/easytl/TypeConverterTest.java`

- [ ] **Step 1: Add tests for numeric conversions**

```java
// Add to TypeConverterTest.java

    // ========== Numeric widening (safe) ==========

    @Test
    void testIntegerToLong() {
        Long result = TypeConverter.convert(42, Long.class);
        assertEquals(Long.valueOf(42), result);
    }

    @Test
    void testIntegerToDouble() {
        Double result = TypeConverter.convert(42, Double.class);
        assertEquals(Double.valueOf(42.0), result);
    }

    @Test
    void testIntegerToBigInteger() {
        BigInteger result = TypeConverter.convert(42, BigInteger.class);
        assertEquals(BigInteger.valueOf(42), result);
    }

    @Test
    void testIntegerToBigDecimal() {
        BigDecimal result = TypeConverter.convert(42, BigDecimal.class);
        assertEquals(BigDecimal.valueOf(42), result);
    }

    @Test
    void testFloatToDouble() {
        Double result = TypeConverter.convert(1.5f, Double.class);
        assertEquals(Double.valueOf(1.5), result, 0.0001);
    }

    @Test
    void testLongToBigInteger() {
        BigInteger result = TypeConverter.convert(1234567890123L, BigInteger.class);
        assertEquals(BigInteger.valueOf(1234567890123L), result);
    }

    // ========== Numeric narrowing (may overflow) ==========

    @Test
    void testLongToInteger_inRange() {
        Integer result = TypeConverter.convert(42L, Integer.class);
        assertEquals(Integer.valueOf(42), result);
    }

    @Test
    void testLongToInteger_outOfRange() {
        assertThrows(TypeConversionException.class,
            () -> TypeConverter.convert(Long.MAX_VALUE, Integer.class));
    }

    @Test
    void testBigIntegerToInteger_inRange() {
        Integer result = TypeConverter.convert(BigInteger.valueOf(42), Integer.class);
        assertEquals(Integer.valueOf(42), result);
    }

    @Test
    void testBigIntegerToInteger_outOfRange() {
        assertThrows(TypeConversionException.class,
            () -> TypeConverter.convert(BigInteger.valueOf(Long.MAX_VALUE).multiply(BigInteger.TWO), Integer.class));
    }

    @Test
    void testIntegerToShort_inRange() {
        Short result = TypeConverter.convert(42, Short.class);
        assertEquals(Short.valueOf((short) 42), result);
    }

    @Test
    void testIntegerToShort_outOfRange() {
        assertThrows(TypeConversionException.class,
            () -> TypeConverter.convert(100000, Short.class));
    }

    @Test
    void testIntegerToByte_inRange() {
        Byte result = TypeConverter.convert(42, Byte.class);
        assertEquals(Byte.valueOf((byte) 42), result);
    }

    @Test
    void testIntegerToByte_outOfRange() {
        assertThrows(TypeConversionException.class,
            () -> TypeConverter.convert(1000, Byte.class));
    }

    // ========== BigDecimal conversions ==========

    @Test
    void testLongToBigDecimal() {
        BigDecimal result = TypeConverter.convert(1234567890123L, BigDecimal.class);
        assertEquals(BigDecimal.valueOf(1234567890123L), result);
    }

    @Test
    void testDoubleToBigDecimal() {
        BigDecimal result = TypeConverter.convert(1.5, BigDecimal.class);
        assertEquals(BigDecimal.valueOf(1.5), result);
    }

    @Test
    void testBigIntegerToBigDecimal() {
        BigDecimal result = TypeConverter.convert(BigInteger.valueOf(42), BigDecimal.class);
        assertEquals(BigDecimal.valueOf(42), result);
    }
```

- [ ] **Step 2: Run test to verify it passes**

Run: `mvn test -Dtest=TypeConverterTest -q`
Expected: PASS (implementation already supports these)

- [ ] **Step 3: Commit**

```bash
git add src/test/java/org/dromara/easytl/TypeConverterTest.java
git commit -m "test: add numeric conversion tests for TypeConverter"
```

---

### Task 4: Parser - Add parseEvalSource Method

**Files:**
- Modify: `src/main/java/org/dromara/easytl/parser/Parser.java`
- Test: `src/test/java/org/dromara/easytl/parser/ParserTest.java`

- [ ] **Step 1: Write the failing tests**

```java
// Add to ParserTest.java

    // ==================== parseEvalSource Tests ====================

    @Test
    void testParseEvalSource_SingleExpression() {
        String source = "1 + 2";
        StringSource stringSource = new StringSource(source);
        Lexer lexer = new Lexer(stringSource);
        TokenStream tokenStream = new TokenStream(lexer);
        Parser parser = new Parser(tokenStream, source);

        List<ASTNode> statements = parser.parseEvalSource();

        assertEquals(1, statements.size());
        assertTrue(statements.get(0) instanceof BinaryExpression);
    }

    @Test
    void testParseEvalSource_MultipleStatements() {
        String source = "let x = 1; let y = 2; x + y";
        StringSource stringSource = new StringSource(source);
        Lexer lexer = new Lexer(stringSource);
        TokenStream tokenStream = new TokenStream(lexer);
        Parser parser = new Parser(tokenStream, source);

        List<ASTNode> statements = parser.parseEvalSource();

        assertEquals(3, statements.size());
    }

    @Test
    void testParseEvalSource_ReturnStatement() {
        String source = "if (true) { return 5 } 10";
        StringSource stringSource = new StringSource(source);
        Lexer lexer = new Lexer(stringSource);
        TokenStream tokenStream = new TokenStream(lexer);
        Parser parser = new Parser(tokenStream, source);

        List<ASTNode> statements = parser.parseEvalSource();

        assertEquals(2, statements.size());
        assertTrue(statements.get(0) instanceof IfExpression);
    }

    @Test
    void testParseEvalSource_Empty() {
        String source = "";
        StringSource stringSource = new StringSource(source);
        Lexer lexer = new Lexer(stringSource);
        TokenStream tokenStream = new TokenStream(lexer);
        Parser parser = new Parser(tokenStream, source);

        List<ASTNode> statements = parser.parseEvalSource();

        assertEquals(0, statements.size());
    }

    @Test
    void testParseEvalSource_ForLoop() {
        String source = "for (i in [1, 2, 3]) { i * 2 }";
        StringSource stringSource = new StringSource(source);
        Lexer lexer = new Lexer(stringSource);
        TokenStream tokenStream = new TokenStream(lexer);
        Parser parser = new Parser(tokenStream, source);

        List<ASTNode> statements = parser.parseEvalSource();

        assertEquals(1, statements.size());
        assertTrue(statements.get(0) instanceof ForExpression);
    }
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn test -Dtest=ParserTest#testParseEvalSource_SingleExpression -q`
Expected: FAIL - method not found

- [ ] **Step 3: Add parseEvalSource method to Parser**

```java
// Add to Parser.java after parseBlockStatement() method

    /**
     * 解析 eval 源码（无模板包装的语句/表达式列表）
     * <p>
     * 用于 Eval.eval() 方法，直接解析表达式和语句，
     * 不需要 {% %} 或 { } 包装
     * </p>
     *
     * @return 语句/表达式列表
     */
    public List<ASTNode> parseEvalSource() {
        List<ASTNode> stmts = new ArrayList<>();
        while (!check(TokenType.EOF)) {
            if (check(TokenType.RETURN)) {
                stmts.add(parseReturnStatement());
            } else {
                stmts.add(parseExpression());
            }
            if (check(TokenType.SEMICOLON)) {
                advance();
            }
        }
        return stmts;
    }
```

- [ ] **Step 4: Run test to verify it passes**

Run: `mvn test -Dtest=ParserTest#testParseEvalSource* -q`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/java/org/dromara/easytl/parser/Parser.java src/test/java/org/dromara/easytl/parser/ParserTest.java
git commit -m "feat: add parseEvalSource method to Parser for eval support"
```

---

### Task 5: Eval Class - Core Implementation

**Files:**
- Create: `src/main/java/org/dromara/easytl/Eval.java`
- Test: `src/test/java/org/dromara/easytl/EvalTest.java`

- [ ] **Step 1: Write the failing tests for basic eval**

```java
package org.dromara.easytl;

import org.dromara.easytl.exception.TemplateCompileException;
import org.dromara.easytl.exception.TemplateRuntimeException;
import org.dromara.easytl.exception.TypeConversionException;
import org.dromara.easytl.runtime.EtlURL;
import org.junit.jupiter.api.Test;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class EvalTest {

    // ========== Basic expressions ==========

    @Test
    void testEval_IntegerArithmetic() {
        Integer result = Eval.eval("1 + 2", Integer.class);
        assertEquals(Integer.valueOf(3), result);
    }

    @Test
    void testEval_FloatArithmetic() {
        Double result = Eval.eval("1.5 + 1.5", Double.class);
        assertEquals(Double.valueOf(3.0), result);
    }

    @Test
    void testEval_StringLiteral() {
        String result = Eval.eval("'hello'", String.class);
        assertEquals("hello", result);
    }

    @Test
    void testEval_BooleanExpression() {
        Boolean result = Eval.eval("1 < 2", Boolean.class);
        assertTrue(result);
    }

    @Test
    void testEval_WithoutType() {
        Object result = Eval.eval("1 + 2");
        assertEquals(Integer.valueOf(3), result);
    }

    // ========== Null handling ==========

    @Test
    void testEval_NullSource() {
        assertThrows(IllegalArgumentException.class, () -> Eval.eval(null));
    }

    @Test
    void testEval_EmptySource() {
        assertNull(Eval.eval(""));
    }

    @Test
    void testEval_WhitespaceOnly() {
        assertNull(Eval.eval("   "));
    }

    // ========== Type conversion ==========

    @Test
    void testEval_IntegerToString() {
        String result = Eval.eval("1 + 2", String.class);
        assertEquals("3", result);
    }

    @Test
    void testEval_IntegerToLong() {
        Long result = Eval.eval("42", Long.class);
        assertEquals(Long.valueOf(42), result);
    }

    @Test
    void testEval_IntegerToBigInteger() {
        BigInteger result = Eval.eval("42", BigInteger.class);
        assertEquals(BigInteger.valueOf(42), result);
    }

    @Test
    void testEval_StringToInteger_throws() {
        assertThrows(TypeConversionException.class, () -> Eval.eval("'abc'", Integer.class));
    }

    // ========== Variables and let ==========

    @Test
    void testEval_LetStatement() {
        Integer result = Eval.eval("let x = 1; let y = 2; x + y", Integer.class);
        assertEquals(Integer.valueOf(3), result);
    }

    @Test
    void testEval_LastExpression() {
        Object result = Eval.eval("let x = 5; x * 2");
        assertEquals(Integer.valueOf(10), result);
    }

    // ========== Errors ==========

    @Test
    void testEval_UndefinedVariable() {
        assertThrows(TemplateRuntimeException.class, () -> Eval.eval("undefinedVar", Object.class));
    }

    @Test
    void testEval_SyntaxError() {
        assertThrows(TemplateCompileException.class, () -> Eval.eval("let x = ", Object.class));
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn test -Dtest=EvalTest -q`
Expected: FAIL - class not found

- [ ] **Step 3: Write Eval class implementation**

```java
package org.dromara.easytl;

import org.dromara.easytl.ast.ASTNode;
import org.dromara.easytl.ast.Position;
import org.dromara.easytl.compiler.Evaluator;
import org.dromara.easytl.exception.TemplateRuntimeException;
import org.dromara.easytl.parser.Lexer;
import org.dromara.easytl.parser.Parser;
import org.dromara.easytl.parser.StringSource;
import org.dromara.easytl.parser.TokenStream;
import org.dromara.easytl.runtime.EtlURL;
import org.dromara.easytl.runtime.ReturnException;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 表达式求值工具类
 * <p>
 * 提供即时编译和执行表达式/语句的能力，返回指定类型的值。
 * </p>
 *
 * <h3>使用示例：</h3>
 * <pre>{@code
 * // 基本表达式
 * Integer result = Eval.eval("1 + 2", Integer.class);  // 3
 *
 * // 变量和语句
 * Integer result = Eval.eval("let x = 1; let y = 2; x + y", Integer.class);  // 3
 *
 * // 无类型参数
 * Object result = Eval.eval("1 + 2");  // Integer 3
 * }</pre>
 *
 * @author EasyTL Team
 * @since 1.0.0
 */
public final class Eval {

    private static final int URL_WRAPPER_OFFSET = 4; // "url(".length()

    private Eval() {
        // 工具类，禁止实例化
    }

    // ==================== 核心 eval 方法 ====================

    /**
     * 执行表达式/语句并返回结果
     *
     * @param source 源码
     * @return 执行结果，可能为 null
     * @throws IllegalArgumentException 如果 source 为 null
     */
    public static Object eval(String source) {
        if (source == null) {
            throw new IllegalArgumentException("Source cannot be null");
        }

        String trimmed = source.trim();
        if (trimmed.isEmpty()) {
            return null;
        }

        List<ASTNode> statements = parseAsStatements(source);
        if (statements.isEmpty()) {
            return null;
        }

        Context context = new Context();
        Evaluator evaluator = new Evaluator(context, null, source);

        Object result = null;
        try {
            for (ASTNode stmt : statements) {
                result = stmt.accept(evaluator);
            }
        } catch (ReturnException e) {
            result = e.getValue();
        }

        return result;
    }

    /**
     * 执行表达式/语句并转换为指定类型
     *
     * @param source 源码
     * @param type 目标类型
     * @param <T> 类型泛型
     * @return 转换后的结果
     * @throws IllegalArgumentException 如果 source 为 null
     * @throws TypeConversionException 如果类型转换失败
     */
    public static <T> T eval(String source, Class<T> type) {
        Object result = eval(source);
        return TypeConverter.convert(result, type);
    }

    // ==================== 便捷方法 ====================

    public static Integer evalAsInteger(String source) {
        return eval(source, Integer.class);
    }

    public static Long evalAsLong(String source) {
        return eval(source, Long.class);
    }

    public static Float evalAsFloat(String source) {
        return eval(source, Float.class);
    }

    public static Double evalAsDouble(String source) {
        return eval(source, Double.class);
    }

    public static BigInteger evalAsBigInteger(String source) {
        return eval(source, BigInteger.class);
    }

    public static BigDecimal evalAsBigDecimal(String source) {
        return eval(source, BigDecimal.class);
    }

    public static Boolean evalAsBoolean(String source) {
        return eval(source, Boolean.class);
    }

    public static String evalAsString(String source) {
        return eval(source, String.class);
    }

    @SuppressWarnings("unchecked")
    public static List<?> evalAsList(String source) {
        return eval(source, List.class);
    }

    @SuppressWarnings("unchecked")
    public static Map<?, ?> evalAsMap(String source) {
        return eval(source, Map.class);
    }

    public static EtlURL evalAsURL(String source) {
        if (source == null) {
            throw new IllegalArgumentException("Source cannot be null");
        }

        String wrappedSource = "url(" + source + ")";

        try {
            return eval(wrappedSource, EtlURL.class);
        } catch (TemplateRuntimeException e) {
            throw adjustErrorPosition(e, -URL_WRAPPER_OFFSET);
        }
    }

    // ==================== 内部方法 ====================

    private static List<ASTNode> parseAsStatements(String source) {
        if (source == null || source.trim().isEmpty()) {
            return Collections.emptyList();
        }

        StringSource stringSource = new StringSource(source);
        Lexer lexer = new Lexer(stringSource);
        TokenStream tokenStream = new TokenStream(lexer);
        Parser parser = new Parser(tokenStream, source);

        return parser.parseEvalSource();
    }

    private static TemplateRuntimeException adjustErrorPosition(
            TemplateRuntimeException e, int columnOffset) {
        Position pos = e.getPosition();
        if (pos == null || pos.isUnknown()) {
            return e;
        }

        int newStartColumn = Math.max(1, pos.getStartColumn() + columnOffset);
        int newEndColumn = Math.max(1, pos.getEndColumn() + columnOffset);

        Position newPos = new Position(
            pos.getStartLine(), newStartColumn,
            pos.getEndLine(), newEndColumn
        );

        return new TemplateRuntimeException(e.getMessage(), newPos, e.getSource());
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `mvn test -Dtest=EvalTest -q`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/java/org/dromara/easytl/Eval.java src/test/java/org/dromara/easytl/EvalTest.java
git commit -m "feat: add Eval class with core eval functionality"
```

---

### Task 6: Eval Class - Control Flow and Return

**Files:**
- Modify: `src/test/java/org/dromara/easytl/EvalTest.java`

- [ ] **Step 1: Add tests for control flow and return**

```java
// Add to EvalTest.java

    // ========== Control flow ==========

    @Test
    void testEval_IfExpression() {
        Integer result = Eval.eval("if (true) { 10 } else { 20 }", Integer.class);
        assertEquals(Integer.valueOf(10), result);
    }

    @Test
    void testEval_IfExpression_FalseCondition() {
        Integer result = Eval.eval("if (false) { 10 } else { 20 }", Integer.class);
        assertEquals(Integer.valueOf(20), result);
    }

    @Test
    void testEval_ForExpression() {
        Object result = Eval.eval("let sum = 0; for (i in [1,2,3]) { sum = sum + i } sum");
        assertEquals(Integer.valueOf(6), result);
    }

    // ========== Return statement ==========

    @Test
    void testEval_ReturnInFor() {
        Integer result = Eval.eval("for (i in [1,2,3]) { if (i == 2) return i }", Integer.class);
        assertEquals(Integer.valueOf(2), result);
    }

    @Test
    void testEval_ReturnInIf() {
        Integer result = Eval.eval("if (true) { return 5 } 10", Integer.class);
        assertEquals(Integer.valueOf(5), result);
    }

    @Test
    void testEval_ReturnValue() {
        Integer result = Eval.eval("return 42", Integer.class);
        assertEquals(Integer.valueOf(42), result);
    }

    // ========== Lambda expressions ==========

    @Test
    void testEval_LambdaCall() {
        Integer result = Eval.eval("((x) => x + 1)(2)", Integer.class);
        assertEquals(Integer.valueOf(3), result);
    }

    @Test
    void testEval_LambdaWithMultipleParams() {
        Integer result = Eval.eval("let f = (x, y) => x * y; f(2, 3)", Integer.class);
        assertEquals(Integer.valueOf(6), result);
    }

    // ========== Complex expressions ==========

    @Test
    void testEval_Ternary() {
        Integer result = Eval.eval("true ? 1 : 2", Integer.class);
        assertEquals(Integer.valueOf(1), result);
    }

    @Test
    void testEval_Elvis() {
        Object result = Eval.eval("null ?? 5");
        assertEquals(Integer.valueOf(5), result);
    }

    @Test
    void testEval_ListLiteral() {
        Object result = Eval.eval("[1, 2, 3]");
        assertTrue(result instanceof List);
        assertEquals(Arrays.asList(1, 2, 3), result);
    }

    @Test
    void testEval_MapLiteral() {
        @SuppressWarnings("unchecked")
        Map<String, Integer> result = (Map<String, Integer>) Eval.eval("{'a': 1, 'b': 2}");
        assertEquals(Integer.valueOf(1), result.get("a"));
        assertEquals(Integer.valueOf(2), result.get("b"));
    }

    // ========== Convenience methods ==========

    @Test
    void testEvalAsBigDecimal() {
        BigDecimal result = Eval.evalAsBigDecimal("1.5 + 2.5");
        assertEquals(BigDecimal.valueOf(4.0), result);
    }

    @Test
    void testEvalAsBigInteger() {
        BigInteger result = Eval.evalAsBigInteger("1000000 * 1000000");
        assertEquals(BigInteger.valueOf(1000000000000L), result);
    }
```

- [ ] **Step 2: Run test to verify it passes**

Run: `mvn test -Dtest=EvalTest -q`
Expected: PASS

- [ ] **Step 3: Commit**

```bash
git add src/test/java/org/dromara/easytl/EvalTest.java
git commit -m "test: add control flow and return tests for Eval"
```

---

### Task 7: Eval Class - evalAsURL Tests

**Files:**
- Modify: `src/test/java/org/dromara/easytl/EvalTest.java`

- [ ] **Step 1: Add tests for evalAsURL**

```java
// Add to EvalTest.java

    // ========== evalAsURL ==========

    @Test
    void testEvalAsURL_BasicURL() {
        EtlURL url = Eval.evalAsURL("http://baidu.com");
        assertEquals("http", url.getScheme());
        assertEquals("baidu.com", url.getHost());
    }

    @Test
    void testEvalAsURL_Path() {
        EtlURL url = Eval.evalAsURL("/xxx/yyy");
        assertEquals("/xxx/yyy", url.getPath());
    }

    @Test
    void testEvalAsURL_NullSource() {
        assertThrows(IllegalArgumentException.class, () -> Eval.evalAsURL(null));
    }

    @Test
    void testEvalAsURL_ErrorPosition() {
        try {
            Eval.evalAsURL("aaa{x}");
            fail("Should throw TemplateRuntimeException");
        } catch (TemplateRuntimeException e) {
            // 错误位置应指向原始位置，不是 url(aaa{x})
            String message = e.getMessage();
            // 验证消息中包含原始代码
            if (message != null) {
                // 不应该显示 url( 前缀
                assertFalse(message.contains("url(aaa"),
                    "Error message should not show url() wrapper: " + message);
            }
        }
    }
```

- [ ] **Step 2: Run test to verify it passes**

Run: `mvn test -Dtest=EvalTest -q`
Expected: PASS

- [ ] **Step 3: Commit**

```bash
git add src/test/java/org/dromara/easytl/EvalTest.java
git commit -m "test: add evalAsURL tests for Eval"
```

---

### Task 8: EasyTL - Add Eval Methods

**Files:**
- Modify: `src/main/java/org/dromara/easytl/EasyTL.java`
- Modify: `src/test/java/org/dromara/easytl/EasyTLTest.java`

- [ ] **Step 1: Add tests for EasyTL eval methods**

```java
// Add to EasyTLTest.java
// Add imports at the top:
import org.dromara.easytl.runtime.EtlURL;
import java.util.Arrays;

    // ==================== eval() 测试 ====================

    @Test
    void testEval_IntegerResult() {
        Integer result = EasyTL.eval("1 + 2", Integer.class);
        assertEquals(Integer.valueOf(3), result);
    }

    @Test
    void testEval_StringResult() {
        String result = EasyTL.eval("'hello'", String.class);
        assertEquals("hello", result);
    }

    @Test
    void testEval_WithoutType() {
        Object result = EasyTL.eval("1 + 2");
        assertEquals(Integer.valueOf(3), result);
    }

    @Test
    void testEval_LetStatement() {
        Integer result = EasyTL.eval("let x = 1; let y = 2; x + y", Integer.class);
        assertEquals(Integer.valueOf(3), result);
    }

    @Test
    void testEval_ReturnStatement() {
        Integer result = EasyTL.eval("return 42", Integer.class);
        assertEquals(Integer.valueOf(42), result);
    }

    // ==================== evalAsXxx 便捷方法测试 ====================

    @Test
    void testEvalAsInteger() {
        assertEquals(Integer.valueOf(2), EasyTL.evalAsInteger("4 / 2"));
    }

    @Test
    void testEvalAsLong() {
        assertEquals(Long.valueOf(42), EasyTL.evalAsLong("42"));
    }

    @Test
    void testEvalAsFloat() {
        assertEquals(Float.valueOf(1.5f), EasyTL.evalAsFloat("1.5"));
    }

    @Test
    void testEvalAsDouble() {
        assertEquals(Double.valueOf(3.0), EasyTL.evalAsDouble("1.5 + 1.5"));
    }

    @Test
    void testEvalAsBoolean() {
        assertTrue(EasyTL.evalAsBoolean("1 < 2"));
        assertFalse(EasyTL.evalAsBoolean("1 > 2"));
    }

    @Test
    void testEvalAsString() {
        assertEquals("hello", EasyTL.evalAsString("'hello'"));
        assertEquals("3", EasyTL.evalAsString("1 + 2"));
    }

    @Test
    void testEvalAsList() {
        assertEquals(Arrays.asList(1, 2, 3), EasyTL.evalAsList("[1, 2, 3]"));
    }

    @Test
    void testEvalAsURL() {
        EtlURL url = EasyTL.evalAsURL("http://example.com");
        assertEquals("http", url.getScheme());
        assertEquals("example.com", url.getHost());
    }
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn test -Dtest=EasyTLTest#testEval_IntegerResult -q`
Expected: FAIL - method not found

- [ ] **Step 3: Add eval methods to EasyTL**

```java
// Add to EasyTL.java after the validate method

    // ==================== 表达式求值 ====================

    /**
     * 执行表达式/语句并返回结果
     *
     * @param source 源码
     * @return 执行结果，可能为 null
     * @throws IllegalArgumentException 如果 source 为 null
     */
    public static Object eval(String source) {
        return Eval.eval(source);
    }

    /**
     * 执行表达式/语句并转换为指定类型
     *
     * @param source 源码
     * @param type 目标类型
     * @param <T> 类型泛型
     * @return 转换后的结果
     * @throws IllegalArgumentException 如果 source 为 null
     * @throws TypeConversionException 如果类型转换失败
     */
    public static <T> T eval(String source, Class<T> type) {
        return Eval.eval(source, type);
    }

    /**
     * 执行表达式并返回 Integer
     */
    public static Integer evalAsInteger(String source) {
        return Eval.evalAsInteger(source);
    }

    /**
     * 执行表达式并返回 Long
     */
    public static Long evalAsLong(String source) {
        return Eval.evalAsLong(source);
    }

    /**
     * 执行表达式并返回 Float
     */
    public static Float evalAsFloat(String source) {
        return Eval.evalAsFloat(source);
    }

    /**
     * 执行表达式并返回 Double
     */
    public static Double evalAsDouble(String source) {
        return Eval.evalAsDouble(source);
    }

    /**
     * 执行表达式并返回 BigInteger
     */
    public static java.math.BigInteger evalAsBigInteger(String source) {
        return Eval.evalAsBigInteger(source);
    }

    /**
     * 执行表达式并返回 BigDecimal
     */
    public static java.math.BigDecimal evalAsBigDecimal(String source) {
        return Eval.evalAsBigDecimal(source);
    }

    /**
     * 执行表达式并返回 Boolean
     */
    public static Boolean evalAsBoolean(String source) {
        return Eval.evalAsBoolean(source);
    }

    /**
     * 执行表达式并返回 String
     */
    public static String evalAsString(String source) {
        return Eval.evalAsString(source);
    }

    /**
     * 执行表达式并返回 List
     */
    public static java.util.List<?> evalAsList(String source) {
        return Eval.evalAsList(source);
    }

    /**
     * 执行表达式并返回 Map
     */
    public static java.util.Map<?, ?> evalAsMap(String source) {
        return Eval.evalAsMap(source);
    }

    /**
     * 执行表达式并返回 EtlURL
     * <p>
     * 可省略外层的 url() 包装，直接传入 URL 字符串
     * </p>
     */
    public static EtlURL evalAsURL(String source) {
        return Eval.evalAsURL(source);
    }
```

Also add imports at the top:

```java
import org.dromara.easytl.exception.TypeConversionException;
import org.dromara.easytl.runtime.EtlURL;
```

- [ ] **Step 4: Run test to verify it passes**

Run: `mvn test -Dtest=EasyTLTest -q`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/java/org/dromara/easytl/EasyTL.java src/test/java/org/dromara/easytl/EasyTLTest.java
git commit -m "feat: add eval method family to EasyTL facade"
```

---

### Task 9: Run Full Test Suite

- [ ] **Step 1: Run all tests**

Run: `mvn test -q`
Expected: All tests PASS

- [ ] **Step 2: Fix any failures**

If tests fail, investigate and fix.

- [ ] **Step 3: Final commit (if needed)**

```bash
git add -A
git commit -m "fix: resolve test failures for eval implementation"
```

---

### Task 10: Documentation

**Files:**
- Modify: `README.md` or documentation files (optional)

- [ ] **Step 1: Add usage examples to documentation**

Add eval method usage examples to project documentation.

- [ ] **Step 2: Commit**

```bash
git add README.md
git commit -m "docs: add eval method usage examples"
```

---

## Summary

| Task | Description | Files Changed |
|------|-------------|---------------|
| 1 | TypeConversionException | 2 new |
| 2 | TypeConverter basic | 2 new |
| 3 | TypeConverter numeric | 1 modified |
| 4 | Parser.parseEvalSource | 2 modified |
| 5 | Eval class core | 2 new |
| 6 | Eval control flow | 1 modified |
| 7 | Eval evalAsURL | 1 modified |
| 8 | EasyTL eval methods | 2 modified |
| 9 | Full test suite | verification |
| 10 | Documentation | optional |

**Total new files:** 6
**Total modified files:** 6