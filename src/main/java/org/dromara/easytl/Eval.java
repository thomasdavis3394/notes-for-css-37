package org.dromara.easytl;

import org.dromara.easytl.ast.ASTNode;
import org.dromara.easytl.ast.Position;
import org.dromara.easytl.ast.UrlLiteral;
import org.dromara.easytl.compiler.Evaluator;
import org.dromara.easytl.exception.TemplateRuntimeException;
import org.dromara.easytl.exception.TypeConversionException;
import org.dromara.easytl.parser.Lexer;
import org.dromara.easytl.parser.Parser;
import org.dromara.easytl.parser.Source;
import org.dromara.easytl.parser.StringSource;
import org.dromara.easytl.parser.TokenStream;
import org.dromara.easytl.runtime.EtlURL;
import org.dromara.easytl.runtime.ReturnException;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

/**
 * Eval 工具类
 * <p>
 * 提供表达式求值的静态方法，用于直接执行 EasyTL 表达式并返回结果。
 * </p>
 *
 * <h3>核心方法：</h3>
 * <ul>
 *   <li>{@link #eval(String)} - 执行表达式并返回 Object</li>
 *   <li>{@link #eval(String, Class)} - 执行表达式并转换为指定类型</li>
 * </ul>
 *
 * <h3>便捷方法：</h3>
 * <ul>
 *   <li>{@link #evalAsInteger(String)} - 返回 Integer</li>
 *   <li>{@link #evalAsLong(String)} - 返回 Long</li>
 *   <li>{@link #evalAsFloat(String)} - 返回 Float</li>
 *   <li>{@link #evalAsDouble(String)} - 返回 Double</li>
 *   <li>{@link #evalAsBigInteger(String)} - 返回 BigInteger</li>
 *   <li>{@link #evalAsBigDecimal(String)} - 返回 BigDecimal</li>
 *   <li>{@link #evalAsBoolean(String)} - 返回 Boolean</li>
 *   <li>{@link #evalAsString(String)} - 返回 String</li>
 *   <li>{@link #evalAsList(String)} - 返回 List</li>
 *   <li>{@link #evalAsMap(String)} - 返回 Map</li>
 *   <li>{@link #evalAsURL(String)} - 返回 EtlURL</li>
 * </ul>
 *
 * @author EasyTL Team
 * @since 1.0.0
 */
public final class Eval {

    /**
     * 私有构造函数，防止实例化
     */
    private Eval() {
        // Utility class
    }

    // ==================== Core Methods ====================

    /**
     * 执行表达式并返回结果
     *
     * @param source 表达式源码
     * @return 表达式执行结果
     * @throws IllegalArgumentException 如果 source 为 null
     * @throws TemplateRuntimeException 如果表达式执行出错
     */
    public static Object eval(String source) {
        return eval(source, Object.class);
    }

    /**
     * 执行表达式并将结果转换为指定类型
     *
     * @param source 表达式源码
     * @param type 目标类型
     * @param <T> 目标类型泛型
     * @return 转换后的结果
     * @throws IllegalArgumentException 如果 source 为 null
     * @throws TypeConversionException 如果类型转换失败
     * @throws TemplateRuntimeException 如果表达式执行出错
     */
    public static <T> T eval(String source, Class<T> type) {
        if (source == null) {
            throw new IllegalArgumentException("Source cannot be null");
        }

        if (type == null) {
            throw new IllegalArgumentException("Type cannot be null");
        }

        // 空白源码返回 null
        if (source.trim().isEmpty()) {
            return null;
        }

        // 解析并执行
        List<ASTNode> statements = parseAsStatements(source);
        Object result = executeStatements(statements, source);

        // 类型转换
        return convertResult(result, type);
    }

    /**
     * 使用指定上下文执行表达式并返回结果
     *
     * @param source 表达式源码
     * @param context 上下文对象，包含变量
     * @return 表达式执行结果
     * @throws IllegalArgumentException 如果 source 或 context 为 null
     * @throws TemplateRuntimeException 如果表达式执行出错
     */
    public static Object eval(String source, Context context) {
        return eval(source, context, Object.class);
    }

    /**
     * 使用指定上下文执行表达式并将结果转换为指定类型
     *
     * @param source 表达式源码
     * @param context 上下文对象，包含变量
     * @param type 目标类型
     * @param <T> 目标类型泛型
     * @return 转换后的结果
     * @throws IllegalArgumentException 如果 source 或 context 为 null
     * @throws TypeConversionException 如果类型转换失败
     * @throws TemplateRuntimeException 如果表达式执行出错
     */
    public static <T> T eval(String source, Context context, Class<T> type) {
        if (source == null) {
            throw new IllegalArgumentException("Source cannot be null");
        }

        if (context == null) {
            throw new IllegalArgumentException("Context cannot be null");
        }

        if (type == null) {
            throw new IllegalArgumentException("Type cannot be null");
        }

        // 空白源码返回 null
        if (source.trim().isEmpty()) {
            return null;
        }

        // 解析并执行（使用传入的 context）
        List<ASTNode> statements = parseAsStatements(source);
        Object result = executeStatements(statements, context, source);

        // 类型转换
        return convertResult(result, type);
    }

    // ==================== Convenience Methods ====================

    /**
     * 执行表达式并返回 Integer
     *
     * @param source 表达式源码
     * @return Integer 结果
     */
    public static Integer evalAsInteger(String source) {
        return eval(source, Integer.class);
    }

    /**
     * 执行表达式并返回 Long
     *
     * @param source 表达式源码
     * @return Long 结果
     */
    public static Long evalAsLong(String source) {
        return eval(source, Long.class);
    }

    /**
     * 执行表达式并返回 Float
     *
     * @param source 表达式源码
     * @return Float 结果
     */
    public static Float evalAsFloat(String source) {
        return eval(source, Float.class);
    }

    /**
     * 执行表达式并返回 Double
     *
     * @param source 表达式源码
     * @return Double 结果
     */
    public static Double evalAsDouble(String source) {
        return eval(source, Double.class);
    }

    /**
     * 执行表达式并返回 BigInteger
     *
     * @param source 表达式源码
     * @return BigInteger 结果
     */
    public static BigInteger evalAsBigInteger(String source) {
        Object result = eval(source);
        if (result == null) {
            return null;
        }
        return toBigInteger(result);
    }

    /**
     * 执行表达式并返回 BigDecimal
     *
     * @param source 表达式源码
     * @return BigDecimal 结果
     */
    public static BigDecimal evalAsBigDecimal(String source) {
        Object result = eval(source);
        if (result == null) {
            return null;
        }
        return toBigDecimal(result);
    }

    /**
     * 执行表达式并返回 Boolean
     *
     * @param source 表达式源码
     * @return Boolean 结果
     */
    public static Boolean evalAsBoolean(String source) {
        return eval(source, Boolean.class);
    }

    /**
     * 执行表达式并返回 String
     *
     * @param source 表达式源码
     * @return String 结果
     */
    public static String evalAsString(String source) {
        return eval(source, String.class);
    }

    /**
     * 执行表达式并返回 List
     *
     * @param source 表达式源码
     * @return List 结果
     */
    @SuppressWarnings("unchecked")
    public static List<?> evalAsList(String source) {
        return eval(source, List.class);
    }

    /**
     * 执行表达式并返回 Map
     *
     * @param source 表达式源码
     * @return Map 结果
     */
    @SuppressWarnings("unchecked")
    public static Map<?, ?> evalAsMap(String source) {
        return eval(source, Map.class);
    }

    /**
     * 执行表达式并返回 EtlURL
     * <p>
     * 此方法支持两种输入格式：
     * <ul>
     *   <li>直接 URL 内容：如 "http://{host}/path"</li>
     *   <li>url(...) 包装：如 "url(http://{host}/path)"</li>
     * </ul>
     * 推荐使用第一种格式，错误位置将指向原始源码。
     * </p>
     *
     * @param source URL 源码
     * @return EtlURL 结果
     */
    public static EtlURL evalAsURL(String source) {
        if (source == null) {
            throw new IllegalArgumentException("Source cannot be null");
        }

        if (source.trim().isEmpty()) {
            return null;
        }

        // 检测是否包含 url(...) 包装
        boolean isWrapped = source.startsWith("url(") && source.endsWith(")");

        if (isWrapped) {
            // 用户显式使用了 url(...) 包装，走现有流程
            Object result = eval(source, EtlURL.class);
            return (EtlURL) result;
        } else {
            // 用户没有使用 url(...) 包装，直接解析 URL 内容
            StringSource stringSource = new StringSource(source);
            Lexer lexer = new Lexer(stringSource, false, true); // evalMode=false, urlMode=true
            TokenStream tokenStream = new TokenStream(lexer);
            Parser parser = new Parser(tokenStream, source);
            UrlLiteral urlLiteral = parser.parseEvalURLSource();

            // 执行 AST，传入原始源码
            Context context = new Context();
            Evaluator evaluator = new Evaluator(context, null, source);
            return (EtlURL) urlLiteral.accept(evaluator);
        }
    }

    // ==================== Convenience Methods with Context ====================

    /**
     * 使用指定上下文执行表达式并返回 Integer
     *
     * @param source 表达式源码
     * @param context 上下文对象
     * @return Integer 结果
     */
    public static Integer evalAsInteger(String source, Context context) {
        return eval(source, context, Integer.class);
    }

    /**
     * 使用指定上下文执行表达式并返回 Long
     *
     * @param source 表达式源码
     * @param context 上下文对象
     * @return Long 结果
     */
    public static Long evalAsLong(String source, Context context) {
        return eval(source, context, Long.class);
    }

    /**
     * 使用指定上下文执行表达式并返回 Float
     *
     * @param source 表达式源码
     * @param context 上下文对象
     * @return Float 结果
     */
    public static Float evalAsFloat(String source, Context context) {
        return eval(source, context, Float.class);
    }

    /**
     * 使用指定上下文执行表达式并返回 Double
     *
     * @param source 表达式源码
     * @param context 上下文对象
     * @return Double 结果
     */
    public static Double evalAsDouble(String source, Context context) {
        return eval(source, context, Double.class);
    }

    /**
     * 使用指定上下文执行表达式并返回 BigInteger
     *
     * @param source 表达式源码
     * @param context 上下文对象
     * @return BigInteger 结果
     */
    public static BigInteger evalAsBigInteger(String source, Context context) {
        Object result = eval(source, context);
        if (result == null) {
            return null;
        }
        return toBigInteger(result);
    }

    /**
     * 使用指定上下文执行表达式并返回 BigDecimal
     *
     * @param source 表达式源码
     * @param context 上下文对象
     * @return BigDecimal 结果
     */
    public static BigDecimal evalAsBigDecimal(String source, Context context) {
        Object result = eval(source, context);
        if (result == null) {
            return null;
        }
        return toBigDecimal(result);
    }

    /**
     * 使用指定上下文执行表达式并返回 Boolean
     *
     * @param source 表达式源码
     * @param context 上下文对象
     * @return Boolean 结果
     */
    public static Boolean evalAsBoolean(String source, Context context) {
        return eval(source, context, Boolean.class);
    }

    /**
     * 使用指定上下文执行表达式并返回 String
     *
     * @param source 表达式源码
     * @param context 上下文对象
     * @return String 结果
     */
    public static String evalAsString(String source, Context context) {
        return eval(source, context, String.class);
    }

    /**
     * 使用指定上下文执行表达式并返回 List
     *
     * @param source 表达式源码
     * @param context 上下文对象
     * @return List 结果
     */
    @SuppressWarnings("unchecked")
    public static List<?> evalAsList(String source, Context context) {
        return eval(source, context, List.class);
    }

    /**
     * 使用指定上下文执行表达式并返回 Map
     *
     * @param source 表达式源码
     * @param context 上下文对象
     * @return Map 结果
     */
    @SuppressWarnings("unchecked")
    public static Map<?, ?> evalAsMap(String source, Context context) {
        return eval(source, context, Map.class);
    }

    /**
     * 使用指定上下文执行表达式并返回 EtlURL
     * <p>
     * 此方法支持两种输入格式：
     * <ul>
     *   <li>直接 URL 内容：如 "http://{host}/path"</li>
     *   <li>url(...) 包装：如 "url(http://{host}/path)"</li>
     * </ul>
     * 推荐使用第一种格式，错误位置将指向原始源码。
     * </p>
     *
     * @param source URL 源码
     * @param context 上下文对象
     * @return EtlURL 结果
     */
    public static EtlURL evalAsURL(String source, Context context) {
        if (source == null) {
            throw new IllegalArgumentException("Source cannot be null");
        }

        if (context == null) {
            throw new IllegalArgumentException("Context cannot be null");
        }

        if (source.trim().isEmpty()) {
            return null;
        }

        // 检测是否包含 url(...) 包装
        boolean isWrapped = source.startsWith("url(") && source.endsWith(")");

        if (isWrapped) {
            // 用户显式使用了 url(...) 包装，走现有流程
            Object result = eval(source, context, EtlURL.class);
            return (EtlURL) result;
        } else {
            // 用户没有使用 url(...) 包装，直接解析 URL 内容
            StringSource stringSource = new StringSource(source);
            Lexer lexer = new Lexer(stringSource, false, true); // evalMode=false, urlMode=true
            TokenStream tokenStream = new TokenStream(lexer);
            Parser parser = new Parser(tokenStream, source);
            UrlLiteral urlLiteral = parser.parseEvalURLSource();

            // 执行 AST，传入原始源码
            Evaluator evaluator = new Evaluator(context, null, source);
            return (EtlURL) urlLiteral.accept(evaluator);
        }
    }

    // ==================== Internal Methods ====================

    /**
     * 解析源码为语句列表
     *
     * @param source 源码
     * @return AST 节点列表
     */
    static List<ASTNode> parseAsStatements(String source) {
        StringSource stringSource = new StringSource(source);
        Lexer lexer = new Lexer(stringSource, true); // evalMode = true for pure expression parsing
        TokenStream tokenStream = new TokenStream(lexer);
        Parser parser = new Parser(tokenStream);
        return parser.parseEvalSource();
    }

    /**
     * 执行语句列表并返回结果
     * <p>
     * 执行所有语句，捕获 ReturnException 获取返回值，
     * 如果没有 ReturnException，返回最后一个表达式的值。
     * </p>
     *
     * @param statements 语句列表
     * @param sourceText 源码文本（用于错误报告）
     * @return 执行结果
     */
    static Object executeStatements(List<ASTNode> statements, String sourceText) {
        if (statements.isEmpty()) {
            return null;
        }

        Context context = new Context();
        Evaluator evaluator = new Evaluator(context, null, sourceText);

        Object result = null;
        try {
            for (ASTNode statement : statements) {
                result = statement.accept(evaluator);
            }
        } catch (ReturnException e) {
            return e.getValue();
        }

        return result;
    }

    /**
     * 执行语句列表并返回结果（使用指定上下文）
     * <p>
     * 使用传入的 Context 执行语句，捕获 ReturnException 获取返回值，
     * 如果没有 ReturnException，返回最后一个表达式的值。
     * </p>
     *
     * @param statements 语句列表
     * @param context 上下文对象
     * @param sourceText 源码文本（用于错误报告）
     * @return 执行结果
     */
    static Object executeStatements(List<ASTNode> statements, Context context, String sourceText) {
        if (statements.isEmpty()) {
            return null;
        }

        Evaluator evaluator = new Evaluator(context, null, sourceText);

        Object result = null;
        try {
            for (ASTNode statement : statements) {
                result = statement.accept(evaluator);
            }
        } catch (ReturnException e) {
            return e.getValue();
        }

        return result;
    }

    /**
     * 转换结果为指定类型
     *
     * @param result 原始结果
     * @param type 目标类型
     * @param <T> 目标类型泛型
     * @return 转换后的结果
     */
    @SuppressWarnings("unchecked")
    private static <T> T convertResult(Object result, Class<T> type) {
        if (type == Object.class) {
            return (T) result;
        }

        // 特殊处理 BigInteger 和 BigDecimal
        if (type == BigInteger.class) {
            return (T) toBigInteger(result);
        }

        if (type == BigDecimal.class) {
            return (T) toBigDecimal(result);
        }

        // 使用 TypeConverter 进行转换
        return TypeConverter.convert(result, type);
    }

    /**
     * 将值转换为 BigInteger
     *
     * @param value 原始值
     * @return BigInteger
     */
    private static BigInteger toBigInteger(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof BigInteger) {
            return (BigInteger) value;
        }

        if (value instanceof BigDecimal) {
            return ((BigDecimal) value).toBigInteger();
        }

        if (value instanceof Number) {
            return BigInteger.valueOf(((Number) value).longValue());
        }

        if (value instanceof String) {
            try {
                return new BigInteger((String) value);
            } catch (NumberFormatException e) {
                throw new TypeConversionException(
                    value.getClass().getName(),
                    BigInteger.class.getName(),
                    "Cannot convert string to BigInteger: " + value
                );
            }
        }

        throw new TypeConversionException(
            value.getClass().getName(),
            BigInteger.class.getName(),
            "Incompatible types"
        );
    }

    /**
     * 将值转换为 BigDecimal
     *
     * @param value 原始值
     * @return BigDecimal
     */
    private static BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }

        if (value instanceof BigInteger) {
            return new BigDecimal((BigInteger) value);
        }

        if (value instanceof Number) {
            if (value instanceof Double || value instanceof Float) {
                return BigDecimal.valueOf(((Number) value).doubleValue());
            }
            return BigDecimal.valueOf(((Number) value).longValue());
        }

        if (value instanceof String) {
            try {
                return new BigDecimal((String) value);
            } catch (NumberFormatException e) {
                throw new TypeConversionException(
                    value.getClass().getName(),
                    BigDecimal.class.getName(),
                    "Cannot convert string to BigDecimal: " + value
                );
            }
        }

        throw new TypeConversionException(
            value.getClass().getName(),
            BigDecimal.class.getName(),
            "Incompatible types"
        );
    }
}