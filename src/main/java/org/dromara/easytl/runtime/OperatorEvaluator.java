package org.dromara.easytl.runtime;

import org.dromara.easytl.exception.TemplateRuntimeException;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 运算符求值器
 * <p>
 * 提供各种运算符的求值实现，支持类型提升和大数运算
 * </p>
 *
 * @author EasyTL Team
 * @since 1.0.0
 */
public class OperatorEvaluator {

    // ==================== 算术运算 ====================

    /**
     * 加法运算
     *
     * @param a 左操作数
     * @param b 右操作数
     * @return 运算结果
     */
    public static Object add(Object a, Object b) {
        // 字符串拼接
        if (a instanceof String || b instanceof String) {
            return toString(a) + toString(b);
        }

        // 数值运算
        return arithmeticOperation(a, b, ArithmeticOp.ADD);
    }

    /**
     * 减法运算
     *
     * @param a 左操作数
     * @param b 右操作数
     * @return 运算结果
     */
    public static Object subtract(Object a, Object b) {
        return arithmeticOperation(a, b, ArithmeticOp.SUBTRACT);
    }

    /**
     * 乘法运算
     *
     * @param a 左操作数
     * @param b 右操作数
     * @return 运算结果
     */
    public static Object multiply(Object a, Object b) {
        return arithmeticOperation(a, b, ArithmeticOp.MULTIPLY);
    }

    /**
     * 除法运算
     *
     * @param a 左操作数
     * @param b 右操作数
     * @return 运算结果
     */
    public static Object divide(Object a, Object b) {
        return arithmeticOperation(a, b, ArithmeticOp.DIVIDE);
    }

    /**
     * 取模运算
     *
     * @param a 左操作数
     * @param b 右操作数
     * @return 运算结果
     */
    public static Object modulo(Object a, Object b) {
        return arithmeticOperation(a, b, ArithmeticOp.MODULO);
    }

    // ==================== 位运算 ====================

    /**
     * 按位与运算
     */
    public static Object bitwiseAnd(Object a, Object b) {
        if (a == null || b == null) {
            throw new TemplateRuntimeException("Cannot perform bitwise operation on null");
        }
        return bitwiseOperation(a, b, BitwiseOp.AND);
    }

    /**
     * 按位或运算
     */
    public static Object bitwiseOr(Object a, Object b) {
        if (a == null || b == null) {
            throw new TemplateRuntimeException("Cannot perform bitwise operation on null");
        }
        return bitwiseOperation(a, b, BitwiseOp.OR);
    }

    /**
     * 按位异或运算
     */
    public static Object bitwiseXor(Object a, Object b) {
        if (a == null || b == null) {
            throw new TemplateRuntimeException("Cannot perform bitwise operation on null");
        }
        return bitwiseOperation(a, b, BitwiseOp.XOR);
    }

    /**
     * 左移运算
     */
    public static Object leftShift(Object a, Object b) {
        if (a == null || b == null) {
            throw new TemplateRuntimeException("Cannot perform shift operation on null");
        }
        return bitwiseOperation(a, b, BitwiseOp.LEFT_SHIFT);
    }

    /**
     * 右移运算（带符号）
     */
    public static Object rightShift(Object a, Object b) {
        if (a == null || b == null) {
            throw new TemplateRuntimeException("Cannot perform shift operation on null");
        }
        return bitwiseOperation(a, b, BitwiseOp.RIGHT_SHIFT);
    }

    /**
     * 无符号右移运算
     */
    public static Object unsignedRightShift(Object a, Object b) {
        if (a == null || b == null) {
            throw new TemplateRuntimeException("Cannot perform shift operation on null");
        }
        return bitwiseOperation(a, b, BitwiseOp.UNSIGNED_RIGHT_SHIFT);
    }

    /**
     * 取负运算
     *
     * @param operand 操作数
     * @return 运算结果
     */
    public static Object negate(Object operand) {
        if (operand == null) {
            throw new TemplateRuntimeException("Cannot negate null");
        }

        if (operand instanceof BigDecimal) {
            return ((BigDecimal) operand).negate();
        }
        if (operand instanceof BigInteger) {
            return ((BigInteger) operand).negate();
        }
        if (operand instanceof Double) {
            return -((Double) operand);
        }
        if (operand instanceof Float) {
            return -((Float) operand);
        }
        if (operand instanceof Long) {
            return -((Long) operand);
        }
        if (operand instanceof Integer) {
            return -((Integer) operand);
        }
        if (operand instanceof Number) {
            return -((Number) operand).doubleValue();
        }

        throw new TemplateRuntimeException("Cannot negate " + operand.getClass().getName());
    }

    // ==================== 比较运算 ====================

    /**
     * 大于比较
     *
     * @param a 左操作数
     * @param b 右操作数
     * @return 比较结果
     */
    public static boolean greaterThan(Object a, Object b) {
        return compareNumbers(a, b) > 0;
    }

    /**
     * 小于比较
     *
     * @param a 左操作数
     * @param b 右操作数
     * @return 比较结果
     */
    public static boolean lessThan(Object a, Object b) {
        return compareNumbers(a, b) < 0;
    }

    /**
     * 大于等于比较
     *
     * @param a 左操作数
     * @param b 右操作数
     * @return 比较结果
     */
    public static boolean greaterThanOrEqual(Object a, Object b) {
        return compareNumbers(a, b) >= 0;
    }

    /**
     * 小于等于比较
     *
     * @param a 左操作数
     * @param b 右操作数
     * @return 比较结果
     */
    public static boolean lessThanOrEqual(Object a, Object b) {
        return compareNumbers(a, b) <= 0;
    }

    /**
     * 相等比较
     *
     * @param a 左操作数
     * @param b 右操作数
     * @return 比较结果
     */
    public static boolean equal(Object a, Object b) {
        return Objects.equals(a, b);
    }

    /**
     * 不等比较
     *
     * @param a 左操作数
     * @param b 右操作数
     * @return 比较结果
     */
    public static boolean notEqual(Object a, Object b) {
        return !Objects.equals(a, b);
    }

    // ==================== 正则运算 ====================

    /**
     * 正则匹配
     *
     * @param text 文本
     * @param pattern 正则模式
     * @return 是否匹配
     */
    public static boolean regexMatch(Object text, Object pattern) {
        if (text == null || pattern == null) {
            return false;
        }

        String textStr = text.toString();
        Pattern p = toPattern(pattern);
        Matcher matcher = p.matcher(textStr);
        return matcher.find();
    }

    /**
     * 正则不匹配
     *
     * @param text 文本
     * @param pattern 正则模式
     * @return 是否不匹配
     */
    public static boolean regexNotMatch(Object text, Object pattern) {
        return !regexMatch(text, pattern);
    }

    /**
     * 正则替换（替换所有匹配）
     *
     * @param text 文本
     * @param pattern 正则模式
     * @param replacement 替换字符串
     * @return 替换后的字符串
     */
    public static String regexReplace(Object text, Object pattern, Object replacement) {
        if (text == null) {
            return null;
        }
        if (pattern == null) {
            throw new TemplateRuntimeException("Pattern cannot be null");
        }
        if (replacement == null) {
            throw new TemplateRuntimeException("Replacement cannot be null");
        }

        String textStr = text.toString();
        Pattern p = toPattern(pattern);
        String replacementStr = replacement.toString();
        return p.matcher(textStr).replaceAll(replacementStr);
    }

    /**
     * 正则替换（仅替换第一个匹配）
     *
     * @param text 文本
     * @param pattern 正则模式
     * @param replacement 替换字符串
     * @return 替换后的字符串
     */
    public static String regexReplaceFirst(Object text, Object pattern, Object replacement) {
        if (text == null) {
            return null;
        }
        if (pattern == null) {
            throw new TemplateRuntimeException("Pattern cannot be null");
        }
        if (replacement == null) {
            throw new TemplateRuntimeException("Replacement cannot be null");
        }

        String textStr = text.toString();
        Pattern p = toPattern(pattern);
        String replacementStr = replacement.toString();
        return p.matcher(textStr).replaceFirst(replacementStr);
    }

    // ==================== 范围运算 ====================

    /**
     * 包含判断
     *
     * @param value 值
     * @param container 容器
     * @return 是否包含
     */
    public static boolean in(Object value, Object container) {
        if (container == null) {
            return false;
        }

        // Collection
        if (container instanceof Collection) {
            return ((Collection<?>) container).contains(value);
        }

        // Map
        if (container instanceof Map) {
            return ((Map<?, ?>) container).containsKey(value);
        }

        // Range
        if (container instanceof Range) {
            return ((Range) container).contains(value);
        }

        // String
        if (container instanceof String && value instanceof String) {
            return ((String) container).contains((String) value);
        }

        // Array
        if (container.getClass().isArray()) {
            Object[] array = (Object[]) container;
            for (Object item : array) {
                if (Objects.equals(value, item)) {
                    return true;
                }
            }
            return false;
        }

        throw new TemplateRuntimeException("Cannot use 'in' operator with " + container.getClass().getName());
    }

    /**
     * 不包含判断
     *
     * @param value 值
     * @param container 容器
     * @return 是否不包含
     */
    public static boolean notIn(Object value, Object container) {
        return !in(value, container);
    }

    // ==================== 集合运算（JSONPath） ====================

    /**
     * 子集判断（subsetof）
     *
     * <p>左集合的每个元素都包含于右集合时返回 true；左为空集合时返回 true。
     * 元素相等性判定与 {@code in} 操作符一致（{@link Collection#contains}，即 equals 语义）。</p>
     *
     * @param left 左操作数
     * @param right 右操作数
     * @return 左是否为右的子集
     */
    public static boolean subsetof(Object left, Object right) {
        Collection<?> leftCol = toCollection(left);
        Collection<?> rightCol = toCollection(right);
        for (Object item : leftCol) {
            if (!rightCol.contains(item)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 交集判断（anyof）
     *
     * <p>左集合任一元素包含于右集合时返回 true；左为空集合时返回 false。</p>
     *
     * @param left 左操作数
     * @param right 右操作数
     * @return 左与右是否有交集
     */
    public static boolean anyof(Object left, Object right) {
        Collection<?> leftCol = toCollection(left);
        Collection<?> rightCol = toCollection(right);
        for (Object item : leftCol) {
            if (rightCol.contains(item)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 无交集判断（noneof）
     *
     * <p>左集合没有任何元素包含于右集合时返回 true，等价于 {@code !(anyof)}；
     * 左为空集合时返回 true。</p>
     *
     * @param left 左操作数
     * @param right 右操作数
     * @return 左与右是否无交集
     */
    public static boolean noneof(Object left, Object right) {
        return !anyof(left, right);
    }

    /**
     * 集合操作数归一化：null → 空集合；Collection → 本身；数组（含基本类型数组）→ List；
     * 其他标量 → 单元素集合
     *
     * @param value 操作数
     * @return 归一化后的集合
     */
    private static Collection<?> toCollection(Object value) {
        if (value == null) {
            return Collections.emptyList();
        }
        if (value instanceof Collection) {
            return (Collection<?>) value;
        }
        if (value instanceof Object[]) {
            return Arrays.asList((Object[]) value);
        }
        if (value.getClass().isArray()) {
            // 基本类型数组逐项装箱
            int length = Array.getLength(value);
            List<Object> result = new ArrayList<>(length);
            for (int i = 0; i < length; i++) {
                result.add(Array.get(value, i));
            }
            return result;
        }
        return Collections.singletonList(value);
    }

    // ==================== 逻辑运算 ====================

    /**
     * 逻辑与运算
     * <p>
     * 支持布尔值及 truthy/falsy 语义：null、false、0、空字符串、空集合、空 Map 为 falsy，其余为 truthy。
     * 短路求值由调用方（如 Evaluator）在求值前保证，此处仅对已求值操作数做逻辑与。
     * </p>
     *
     * @param a 左操作数
     * @param b 右操作数
     * @return 逻辑与结果
     */
    public static boolean and(Object a, Object b) {
        return toBoolean(a) && toBoolean(b);
    }

    /**
     * 逻辑或运算
     * <p>
     * 支持布尔值及 truthy/falsy 语义，短路求值由调用方保证。
     * </p>
     *
     * @param a 左操作数
     * @param b 右操作数
     * @return 逻辑或结果
     */
    public static boolean or(Object a, Object b) {
        return toBoolean(a) || toBoolean(b);
    }

    /**
     * 逻辑非运算
     * <p>
     * 支持布尔值及 truthy/falsy 语义。
     * </p>
     *
     * @param operand 操作数
     * @return 逻辑非结果
     */
    public static boolean not(Object operand) {
        return !toBoolean(operand);
    }

    /**
     * 将对象转换为布尔值（truthy/falsy）
     * <p>
     * falsy: null、false、0、0.0、空字符串、空 Collection、空 Map；其余为 truthy。
     * </p>
     *
     * @param value 值
     * @return 布尔值
     */
    public static boolean toBoolean(Object value) {
        if (value == null) {
            return false;
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue() != 0;
        }
        if (value instanceof String) {
            return !((String) value).isEmpty();
        }
        if (value instanceof Collection) {
            return !((Collection<?>) value).isEmpty();
        }
        if (value instanceof Map) {
            return !((Map<?, ?>) value).isEmpty();
        }
        return true;
    }

    // ==================== 辅助方法 ====================

    /**
     * 算术运算枚举
     */
    private enum ArithmeticOp {
        ADD, SUBTRACT, MULTIPLY, DIVIDE, MODULO
    }

    /**
     * 位运算类型枚举
     */
    private enum BitwiseOp {
        AND, OR, XOR, LEFT_SHIFT, RIGHT_SHIFT, UNSIGNED_RIGHT_SHIFT
    }

    /**
     * 执行算术运算
     *
     * @param a 左操作数
     * @param b 右操作数
     * @param op 运算类型
     * @return 运算结果
     */
    private static Object arithmeticOperation(Object a, Object b, ArithmeticOp op) {
        if (a == null || b == null) {
            throw new TemplateRuntimeException("Cannot perform arithmetic operation on null");
        }

        // 类型提升
        NumberType type = promoteType(a, b);

        switch (type) {
            case BIG_DECIMAL:
                return arithmeticBigDecimal(toBigDecimal(a), toBigDecimal(b), op);
            case BIG_INTEGER:
                return arithmeticBigInteger(toBigInteger(a), toBigInteger(b), op);
            case DOUBLE:
                return arithmeticDouble(toDouble(a), toDouble(b), op);
            case LONG:
                return arithmeticLong(toLong(a), toLong(b), op);
            case INT:
                return arithmeticInt(toInt(a), toInt(b), op);
            default:
                throw new TemplateRuntimeException("Unsupported number type");
        }
    }

    /**
     * BigDecimal算术运算
     */
    private static BigDecimal arithmeticBigDecimal(BigDecimal a, BigDecimal b, ArithmeticOp op) {
        switch (op) {
            case ADD:
                return a.add(b);
            case SUBTRACT:
                return a.subtract(b);
            case MULTIPLY:
                return a.multiply(b);
            case DIVIDE:
                return a.divide(b, BigDecimal.ROUND_HALF_UP);
            case MODULO:
                return a.remainder(b);
            default:
                throw new TemplateRuntimeException("Unknown arithmetic operation");
        }
    }

    /**
     * BigInteger算术运算
     */
    private static BigInteger arithmeticBigInteger(BigInteger a, BigInteger b, ArithmeticOp op) {
        switch (op) {
            case ADD:
                return a.add(b);
            case SUBTRACT:
                return a.subtract(b);
            case MULTIPLY:
                return a.multiply(b);
            case DIVIDE:
                return a.divide(b);
            case MODULO:
                return a.mod(b);
            default:
                throw new TemplateRuntimeException("Unknown arithmetic operation");
        }
    }

    /**
     * Double算术运算
     */
    private static double arithmeticDouble(double a, double b, ArithmeticOp op) {
        switch (op) {
            case ADD:
                return a + b;
            case SUBTRACT:
                return a - b;
            case MULTIPLY:
                return a * b;
            case DIVIDE:
                return a / b;
            case MODULO:
                return a % b;
            default:
                throw new TemplateRuntimeException("Unknown arithmetic operation");
        }
    }

    /**
     * Long算术运算
     */
    private static long arithmeticLong(long a, long b, ArithmeticOp op) {
        switch (op) {
            case ADD:
                return a + b;
            case SUBTRACT:
                return a - b;
            case MULTIPLY:
                return a * b;
            case DIVIDE:
                return a / b;
            case MODULO:
                return a % b;
            default:
                throw new TemplateRuntimeException("Unknown arithmetic operation");
        }
    }

    /**
     * Int算术运算
     */
    private static int arithmeticInt(int a, int b, ArithmeticOp op) {
        switch (op) {
            case ADD:
                return a + b;
            case SUBTRACT:
                return a - b;
            case MULTIPLY:
                return a * b;
            case DIVIDE:
                return a / b;
            case MODULO:
                return a % b;
            default:
                throw new TemplateRuntimeException("Unknown arithmetic operation");
        }
    }

    /**
     * 执行位运算
     */
    private static Object bitwiseOperation(Object a, Object b, BitwiseOp op) {
        NumberType type = promoteType(a, b);

        // 位运算只支持整数类型
        if (type == NumberType.DOUBLE || type == NumberType.BIG_DECIMAL) {
            throw new TemplateRuntimeException("Cannot perform bitwise operation on floating-point numbers");
        }

        switch (type) {
            case BIG_INTEGER:
                return bitwiseBigInteger(toBigInteger(a), toBigInteger(b), op);
            case LONG:
                return bitwiseLong(toLong(a), toLong(b), op);
            case INT:
                return bitwiseInt(toInt(a), toInt(b), op);
            default:
                return bitwiseLong(toLong(a), toLong(b), op);
        }
    }

    private static int bitwiseInt(int a, int b, BitwiseOp op) {
        switch (op) {
            case AND: return a & b;
            case OR: return a | b;
            case XOR: return a ^ b;
            case LEFT_SHIFT: return a << b;
            case RIGHT_SHIFT: return a >> b;
            case UNSIGNED_RIGHT_SHIFT: return a >>> b;
            default: throw new TemplateRuntimeException("Unknown bitwise operation");
        }
    }

    private static long bitwiseLong(long a, long b, BitwiseOp op) {
        switch (op) {
            case AND: return a & b;
            case OR: return a | b;
            case XOR: return a ^ b;
            case LEFT_SHIFT: return a << b;
            case RIGHT_SHIFT: return a >> b;
            case UNSIGNED_RIGHT_SHIFT: return a >>> b;
            default: throw new TemplateRuntimeException("Unknown bitwise operation");
        }
    }

    private static BigInteger bitwiseBigInteger(BigInteger a, BigInteger b, BitwiseOp op) {
        switch (op) {
            case AND: return a.and(b);
            case OR: return a.or(b);
            case XOR: return a.xor(b);
            case LEFT_SHIFT: return a.shiftLeft(toInt(b));
            case RIGHT_SHIFT: return a.shiftRight(toInt(b));
            default: throw new TemplateRuntimeException("Unsupported bitwise operation for BigInteger: " + op);
        }
    }

    /**
     * 数字类型枚举
     */
    private enum NumberType {
        INT, LONG, DOUBLE, BIG_INTEGER, BIG_DECIMAL
    }

    /**
     * 类型提升
     *
     * @param a 左操作数
     * @param b 右操作数
     * @return 提升后的类型
     */
    private static NumberType promoteType(Object a, Object b) {
        NumberType typeA = getNumberType(a);
        NumberType typeB = getNumberType(b);

        // 类型提升规则：int < long < double < BigInteger < BigDecimal
        if (typeA == NumberType.BIG_DECIMAL || typeB == NumberType.BIG_DECIMAL) {
            return NumberType.BIG_DECIMAL;
        }
        if (typeA == NumberType.BIG_INTEGER || typeB == NumberType.BIG_INTEGER) {
            return NumberType.BIG_INTEGER;
        }
        if (typeA == NumberType.DOUBLE || typeB == NumberType.DOUBLE) {
            return NumberType.DOUBLE;
        }
        if (typeA == NumberType.LONG || typeB == NumberType.LONG) {
            return NumberType.LONG;
        }
        return NumberType.INT;
    }

    /**
     * 获取数字类型
     *
     * @param value 值
     * @return 数字类型
     */
    private static NumberType getNumberType(Object value) {
        if (value instanceof BigDecimal) {
            return NumberType.BIG_DECIMAL;
        }
        if (value instanceof BigInteger) {
            return NumberType.BIG_INTEGER;
        }
        if (value instanceof Double || value instanceof Float) {
            return NumberType.DOUBLE;
        }
        if (value instanceof Long) {
            return NumberType.LONG;
        }
        return NumberType.INT;
    }

    /**
     * 比较两个数字
     *
     * @param a 左操作数
     * @param b 右操作数
     * @return 比较结果
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static int compareNumbers(Object a, Object b) {
        if (a == null && b == null) {
            return 0;
        }
        if (a == null) {
            return -1;
        }
        if (b == null) {
            return 1;
        }

        if (a instanceof Comparable && b instanceof Comparable) {
            try {
                return ((Comparable) a).compareTo(b);
            } catch (ClassCastException e) {
                // 类型不匹配，尝试转换为数字比较
            }
        }

        // 转换为数字比较
        NumberType type = promoteType(a, b);
        switch (type) {
            case BIG_DECIMAL:
                return toBigDecimal(a).compareTo(toBigDecimal(b));
            case BIG_INTEGER:
                return toBigInteger(a).compareTo(toBigInteger(b));
            case DOUBLE:
                return Double.compare(toDouble(a), toDouble(b));
            case LONG:
                return Long.compare(toLong(a), toLong(b));
            case INT:
                return Integer.compare(toInt(a), toInt(b));
            default:
                throw new TemplateRuntimeException("Cannot compare " + a.getClass().getName() + " and " + b.getClass().getName());
        }
    }

    /**
     * 转换为Pattern对象
     *
     * @param pattern 正则模式
     * @return Pattern对象
     */
    private static Pattern toPattern(Object pattern) {
        if (pattern instanceof Pattern) {
            return (Pattern) pattern;
        }
        if (pattern instanceof String) {
            return Pattern.compile((String) pattern);
        }
        throw new TemplateRuntimeException("Cannot convert " + pattern.getClass().getName() + " to Pattern");
    }

    /**
     * 转换为字符串
     *
     * @param value 值
     * @return 字符串
     */
    private static String toString(Object value) {
        return value != null ? value.toString() : "";
    }

    /**
     * 转换为int
     *
     * @param value 值
     * @return int值
     */
    private static int toInt(Object value) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        throw new TemplateRuntimeException("Cannot convert " + value.getClass().getName() + " to int");
    }

    /**
     * 转换为long
     *
     * @param value 值
     * @return long值
     */
    private static long toLong(Object value) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        throw new TemplateRuntimeException("Cannot convert " + value.getClass().getName() + " to long");
    }

    /**
     * 转换为double
     *
     * @param value 值
     * @return double值
     */
    private static double toDouble(Object value) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        throw new TemplateRuntimeException("Cannot convert " + value.getClass().getName() + " to double");
    }

    /**
     * 转换为BigInteger
     *
     * @param value 值
     * @return BigInteger值
     */
    private static BigInteger toBigInteger(Object value) {
        if (value instanceof BigInteger) {
            return (BigInteger) value;
        }
        if (value instanceof BigDecimal) {
            return ((BigDecimal) value).toBigInteger();
        }
        if (value instanceof Number) {
            return BigInteger.valueOf(((Number) value).longValue());
        }
        throw new TemplateRuntimeException("Cannot convert " + value.getClass().getName() + " to BigInteger");
    }

    /**
     * 转换为BigDecimal
     *
     * @param value 值
     * @return BigDecimal值
     */
    private static BigDecimal toBigDecimal(Object value) {
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        if (value instanceof BigInteger) {
            return new BigDecimal((BigInteger) value);
        }
        if (value instanceof Double || value instanceof Float) {
            return BigDecimal.valueOf(((Number) value).doubleValue());
        }
        if (value instanceof Number) {
            return BigDecimal.valueOf(((Number) value).longValue());
        }
        throw new TemplateRuntimeException("Cannot convert " + value.getClass().getName() + " to BigDecimal");
    }
}
