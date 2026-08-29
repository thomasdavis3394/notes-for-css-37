package org.dromara.easytl;

import org.dromara.easytl.exception.TypeConversionException;

/**
 * 类型转换工具类
 * <p>
 * 提供类型转换功能，用于 eval 方法的返回值类型转换。
 * 支持以下转换：
 * <ul>
 *   <li>null 值处理：null 到 Object/String 返回 null，null 到基本类型抛出异常</li>
 *   <li>精确匹配：相同类型直接返回</li>
 *   <li>String 特殊处理：任何类型都可以通过 toString() 转换为 String</li>
 *   <li>子类到父类：支持向上转型</li>
 *   <li>数值类型转换：支持所有数值类型之间的转换（包括溢出检查）</li>
 * </ul>
 * </p>
 *
 * @author EasyTL Team
 * @since 1.0.0
 */
public final class TypeConverter {

    /**
     * 私有构造函数，防止实例化
     */
    private TypeConverter() {
        // Utility class
    }

    /**
     * 将值转换为指定类型
     *
     * @param value      要转换的值
     * @param targetType 目标类型
     * @param <T>        目标类型泛型
     * @return 转换后的值
     * @throws TypeConversionException 如果转换失败
     */
    @SuppressWarnings("unchecked")
    public static <T> T convert(Object value, Class<T> targetType) {
        if (targetType == null) {
            throw new TypeConversionException("Target type cannot be null");
        }

        // 1. null 处理
        if (value == null) {
            if (targetType.isPrimitive()) {
                throw new TypeConversionException("null", targetType.getName(), "Cannot convert null to primitive type");
            }
            return null;
        }

        // 2. 精确匹配（包括基本类型与包装类型的对应）
        Class<?> sourceType = value.getClass();
        if (isExactMatch(sourceType, targetType)) {
            return (T) value;
        }

        // 3. String 特殊处理
        if (targetType == String.class) {
            return (T) value.toString();
        }

        // 4. 数值类型转换
        if (isNumericConversion(sourceType, targetType)) {
            return convertNumeric(value, targetType);
        }

        // 5. 子类到父类的转换（向上转型）
        if (targetType.isAssignableFrom(sourceType)) {
            return (T) value;
        }

        // 6. 基本类型与包装类型的转换
        if (isPrimitiveWrapperConversion(sourceType, targetType)) {
            return (T) value;
        }

        // 7. 不支持的转换
        throw new TypeConversionException(
            sourceType.getName(),
            targetType.getName(),
            "Incompatible types"
        );
    }

    /**
     * 判断是否为精确匹配（包括基本类型与包装类型的对应）
     *
     * @param sourceType 源类型
     * @param targetType 目标类型
     * @return 如果是精确匹配返回 true
     */
    private static boolean isExactMatch(Class<?> sourceType, Class<?> targetType) {
        // 完全相同
        if (sourceType.equals(targetType)) {
            return true;
        }

        // 基本类型与包装类型的对应
        return isPrimitiveWrapperMatch(sourceType, targetType);
    }

    /**
     * 判断两个类型是否为基本类型与包装类型的对应关系
     *
     * @param type1 类型1
     * @param type2 类型2
     * @return 如果是对应关系返回 true
     */
    private static boolean isPrimitiveWrapperMatch(Class<?> type1, Class<?> type2) {
        return (type1 == int.class && type2 == Integer.class) ||
               (type1 == Integer.class && type2 == int.class) ||
               (type1 == long.class && type2 == Long.class) ||
               (type1 == Long.class && type2 == long.class) ||
               (type1 == double.class && type2 == Double.class) ||
               (type1 == Double.class && type2 == double.class) ||
               (type1 == float.class && type2 == Float.class) ||
               (type1 == Float.class && type2 == float.class) ||
               (type1 == boolean.class && type2 == Boolean.class) ||
               (type1 == Boolean.class && type2 == boolean.class) ||
               (type1 == byte.class && type2 == Byte.class) ||
               (type1 == Byte.class && type2 == byte.class) ||
               (type1 == short.class && type2 == Short.class) ||
               (type1 == Short.class && type2 == short.class) ||
               (type1 == char.class && type2 == Character.class) ||
               (type1 == Character.class && type2 == char.class);
    }

    /**
     * 判断是否为基本类型到包装类型或包装类型到基本类型的转换
     *
     * @param sourceType 源类型
     * @param targetType 目标类型
     * @return 如果是基本类型与包装类型的转换返回 true
     */
    private static boolean isPrimitiveWrapperConversion(Class<?> sourceType, Class<?> targetType) {
        return isPrimitiveWrapperMatch(sourceType, targetType);
    }

    /**
     * 判断类型是否为数值类型
     *
     * @param type 要检查的类型
     * @return 如果是数值类型返回 true
     */
    public static boolean isNumeric(Class<?> type) {
        return type == int.class || type == Integer.class ||
               type == long.class || type == Long.class ||
               type == double.class || type == Double.class ||
               type == float.class || type == Float.class ||
               type == byte.class || type == Byte.class ||
               type == short.class || type == Short.class;
    }

    /**
     * 判断是否为数值类型之间的转换
     *
     * @param sourceType 源类型
     * @param targetType 目标类型
     * @return 如果是数值类型转换返回 true
     */
    public static boolean isNumericConversion(Class<?> sourceType, Class<?> targetType) {
        return isNumeric(sourceType) && isNumeric(targetType) && !sourceType.equals(targetType);
    }

    /**
     * 执行数值类型转换
     *
     * @param value      要转换的值
     * @param targetType 目标类型
     * @param <T>        目标类型泛型
     * @return 转换后的值
     * @throws TypeConversionException 如果转换失败（如溢出）
     */
    @SuppressWarnings("unchecked")
    public static <T> T convertNumeric(Object value, Class<T> targetType) {
        if (value == null) {
            throw new TypeConversionException("null", targetType.getName(), "Cannot convert null to numeric type");
        }

        if (!isNumeric(value.getClass())) {
            throw new TypeConversionException(value.getClass().getName(), targetType.getName(), "Source is not a numeric type");
        }

        Number number = (Number) value;

        // 转换到 int / Integer
        if (targetType == int.class || targetType == Integer.class) {
            long longValue = number.longValue();
            if (longValue < Integer.MIN_VALUE || longValue > Integer.MAX_VALUE) {
                throw new TypeConversionException(
                    value.getClass().getName(),
                    targetType.getName(),
                    "Value " + longValue + " overflows int range"
                );
            }
            return (T) Integer.valueOf(number.intValue());
        }

        // 转换到 long / Long
        if (targetType == long.class || targetType == Long.class) {
            // double/float 转换可能精度损失，但允许
            return (T) Long.valueOf(number.longValue());
        }

        // 转换到 double / Double
        if (targetType == double.class || targetType == Double.class) {
            return (T) Double.valueOf(number.doubleValue());
        }

        // 转换到 float / Float
        if (targetType == float.class || targetType == Float.class) {
            return (T) Float.valueOf(number.floatValue());
        }

        // 转换到 byte / Byte
        if (targetType == byte.class || targetType == Byte.class) {
            long longValue = number.longValue();
            if (longValue < Byte.MIN_VALUE || longValue > Byte.MAX_VALUE) {
                throw new TypeConversionException(
                    value.getClass().getName(),
                    targetType.getName(),
                    "Value " + longValue + " overflows byte range"
                );
            }
            return (T) Byte.valueOf(number.byteValue());
        }

        // 转换到 short / Short
        if (targetType == short.class || targetType == Short.class) {
            long longValue = number.longValue();
            if (longValue < Short.MIN_VALUE || longValue > Short.MAX_VALUE) {
                throw new TypeConversionException(
                    value.getClass().getName(),
                    targetType.getName(),
                    "Value " + longValue + " overflows short range"
                );
            }
            return (T) Short.valueOf(number.shortValue());
        }

        // 不应该到达这里
        throw new TypeConversionException(
            value.getClass().getName(),
            targetType.getName(),
            "Unsupported numeric conversion"
        );
    }
}