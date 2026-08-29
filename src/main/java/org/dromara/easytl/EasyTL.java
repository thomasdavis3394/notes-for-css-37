package org.dromara.easytl;

import org.dromara.easytl.ast.TemplateNode;
import org.dromara.easytl.exception.TemplateCompileException;
import org.dromara.easytl.exception.TypeConversionException;
import org.dromara.easytl.parser.Lexer;
import org.dromara.easytl.parser.Parser;
import org.dromara.easytl.parser.StringSource;
import org.dromara.easytl.parser.TokenStream;

import java.util.Map;

/**
 * EasyTL 工具类 - 用户友好的统一入口
 * <p>
 * 提供简洁的 API 进行模板编译、渲染和验证操作。
 * 支持链式调用风格，适合各种使用场景。
 * </p>
 *
 * <h3>使用示例：</h3>
 * <pre>{@code
 * // 快速渲染
 * String result = EasyTL.render("Hello, {name}!", map);
 *
 * // 链式调用
 * String result = EasyTL.template("Hello, {name}!")
 *     .put("name", "World")
 *     .render();
 *
 * // 编译后多次渲染
 * Template template = EasyTL.compile("Hello, {name}!");
 * String r1 = EasyTL.render(template, ctx1);
 * String r2 = EasyTL.render(template, ctx2);
 * }</pre>
 *
 * @author gongjun
 * @since 1.0.0
 */
public final class EasyTL {

    /**
     * 默认模板引擎（单例）
     */
    private static volatile TemplateEngine defaultEngine;

    /**
     * 私有构造函数，防止实例化
     */
    private EasyTL() {
    }

    // ==================== 编译/渲染 ====================

    /**
     * 创建模板构建器（延迟编译）
     * <p>
     * 支持链式配置和渲染，编译在 render() 时执行。
     * </p>
     *
     * <pre>{@code
     * String result = EasyTL.template("Hello, {name}!")
     *     .cacheEnabled(true)
     *     .put("name", "World")
     *     .render();
     * }</pre>
     *
     * @param source 模板源码
     * @return 模板构建器
     */
    public static TemplateBuilder template(String source) {
        return new TemplateBuilder(source);
    }

    /**
     * 立即编译模板（使用默认引擎）
     *
     * @param source 模板源码
     * @return 编译后的模板
     * @throws TemplateCompileException 如果编译失败
     */
    public static Template compile(String source) {
        return defaultEngine().compile(source);
    }

    /**
     * 立即编译模板（使用指定引擎）
     *
     * @param engine 模板引擎
     * @param source 模板源码
     * @return 编译后的模板
     * @throws TemplateCompileException 如果编译失败
     */
    public static Template compile(TemplateEngine engine, String source) {
        return engine.compile(source);
    }

    /**
     * 便捷渲染：编译 + 渲染（使用默认引擎）
     *
     * @param source 模板源码
     * @param context 上下文
     * @return 渲染结果
     */
    public static String render(String source, Context context) {
        return compile(source).render(context);
    }

    /**
     * 便捷渲染：编译 + 渲染（使用默认引擎）
     *
     * @param source 模板源码
     * @param data 数据 Map
     * @return 渲染结果
     */
    public static String render(String source, Map<String, Object> data) {
        Context context = new Context();
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            context.put(entry.getKey(), entry.getValue());
        }
        return compile(source).render(context);
    }

    /**
     * 便捷渲染：使用已编译模板
     *
     * @param template 编译后的模板
     * @param context 上下文
     * @return 渲染结果
     */
    public static String render(Template template, Context context) {
        return template.render(context);
    }

    /**
     * 便捷渲染：使用已编译模板
     *
     * @param template 编译后的模板
     * @param data 数据 Map
     * @return 渲染结果
     */
    public static String render(Template template, Map<String, Object> data) {
        Context context = new Context();
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            context.put(entry.getKey(), entry.getValue());
        }
        return template.render(context);
    }

    // ==================== 引擎/配置 ====================

    /**
     * 创建引擎构建器
     *
     * <pre>{@code
     * TemplateEngine engine = EasyTL.engine()
     *     .cacheEnabled(true)
     *     .maxCacheSize(1024)
     *     .build();
     * }</pre>
     *
     * @return 引擎构建器
     */
    public static EngineBuilder engine() {
        return new EngineBuilder();
    }

    /**
     * 创建配置构建器
     *
     * <pre>{@code
     * TemplateEngineConfig config = EasyTL.config()
     *     .cacheEnabled(true)
     *     .build();
     * }</pre>
     *
     * @return 配置构建器
     */
    public static ConfigBuilder config() {
        return new ConfigBuilder();
    }

    /**
     * 获取默认引擎（单例）
     * <p>
     * 默认引擎配置可通过返回的对象进行修改。
     * </p>
     *
     * @return 默认模板引擎
     */
    public static TemplateEngine defaultEngine() {
        if (defaultEngine == null) {
            synchronized (EasyTL.class) {
                if (defaultEngine == null) {
                    defaultEngine = new TemplateEngine();
                }
            }
        }
        return defaultEngine;
    }

    // ==================== 辅助方法 ====================

    /**
     * 创建上下文构建器
     *
     * <pre>{@code
     * Context ctx = EasyTL.context()
     *     .put("name", "张三")
     *     .put("age", 25)
     *     .build();
     * }</pre>
     *
     * @return 上下文构建器
     */
    public static ContextBuilder context() {
        return new ContextBuilder();
    }

    /**
     * 输出原始文本（不进行模板解析）
     * <p>
     * 在文本前添加 @raw: 标识，使其在模板解析渲染后保持原样输出。
     * </p>
     *
     * <pre>{@code
     * String template = "Hello " + EasyTL.raw("{name}");
     * // template = "Hello @raw:{name}"
     * // 渲染结果: "Hello {name}" (原样输出，不解析为变量)
     * }</pre>
     *
     * @param text 原始文本
     * @return 带 @raw: 前缀的文本
     */
    public static String raw(String text) {
        return "@raw:" + text;
    }

    /**
     * 解析模板返回 AST
     * <p>
     * 用于高级用户需要直接操作 AST 的场景。
     * </p>
     *
     * @param source 模板源码
     * @return AST 根节点
     * @throws TemplateCompileException 如果解析失败
     */
    public static TemplateNode parse(String source) {
        StringSource stringSource = new StringSource(source);
        Lexer lexer = new Lexer(stringSource);
        TokenStream tokenStream = new TokenStream(lexer);
        Parser parser = new Parser(tokenStream);
        return parser.parse();
    }

    /**
     * 验证模板语法
     *
     * @param source 模板源码
     * @return 验证结果
     */
    public static ValidationResult validate(String source) {
        try {
            parse(source);
            return ValidationResult.valid();
        } catch (TemplateCompileException e) {
            return ValidationResult.invalid(e.getMessage());
        } catch (Exception e) {
            return ValidationResult.invalid(e.getMessage());
        }
    }

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
     * 可以省略外层的 url() 包装，直接传入 URL 字符串。
     * </p>
     */
    public static org.dromara.easytl.runtime.EtlURL evalAsURL(String source) {
        return Eval.evalAsURL(source);
    }

    // ==================== 表达式求值（带 Context） ====================

    /**
     * 使用指定上下文执行表达式并返回结果
     *
     * @param source 源码
     * @param context 上下文对象
     * @return 执行结果，可能为 null
     * @throws IllegalArgumentException 如果 source 或 context 为 null
     */
    public static Object eval(String source, Context context) {
        return Eval.eval(source, context);
    }

    /**
     * 使用指定上下文执行表达式并转换为指定类型
     *
     * @param source 源码
     * @param context 上下文对象
     * @param type 目标类型
     * @param <T> 类型泛型
     * @return 转换后的结果
     * @throws IllegalArgumentException 如果 source 或 context 为 null
     * @throws TypeConversionException 如果类型转换失败
     */
    public static <T> T eval(String source, Context context, Class<T> type) {
        return Eval.eval(source, context, type);
    }

    /**
     * 使用指定上下文执行表达式并返回 Integer
     */
    public static Integer evalAsInteger(String source, Context context) {
        return Eval.evalAsInteger(source, context);
    }

    /**
     * 使用指定上下文执行表达式并返回 Long
     */
    public static Long evalAsLong(String source, Context context) {
        return Eval.evalAsLong(source, context);
    }

    /**
     * 使用指定上下文执行表达式并返回 Float
     */
    public static Float evalAsFloat(String source, Context context) {
        return Eval.evalAsFloat(source, context);
    }

    /**
     * 使用指定上下文执行表达式并返回 Double
     */
    public static Double evalAsDouble(String source, Context context) {
        return Eval.evalAsDouble(source, context);
    }

    /**
     * 使用指定上下文执行表达式并返回 BigInteger
     */
    public static java.math.BigInteger evalAsBigInteger(String source, Context context) {
        return Eval.evalAsBigInteger(source, context);
    }

    /**
     * 使用指定上下文执行表达式并返回 BigDecimal
     */
    public static java.math.BigDecimal evalAsBigDecimal(String source, Context context) {
        return Eval.evalAsBigDecimal(source, context);
    }

    /**
     * 使用指定上下文执行表达式并返回 Boolean
     */
    public static Boolean evalAsBoolean(String source, Context context) {
        return Eval.evalAsBoolean(source, context);
    }

    /**
     * 使用指定上下文执行表达式并返回 String
     */
    public static String evalAsString(String source, Context context) {
        return Eval.evalAsString(source, context);
    }

    /**
     * 使用指定上下文执行表达式并返回 List
     */
    public static java.util.List<?> evalAsList(String source, Context context) {
        return Eval.evalAsList(source, context);
    }

    /**
     * 使用指定上下文执行表达式并返回 Map
     */
    public static java.util.Map<?, ?> evalAsMap(String source, Context context) {
        return Eval.evalAsMap(source, context);
    }

    /**
     * 使用指定上下文执行表达式并返回 EtlURL
     */
    public static org.dromara.easytl.runtime.EtlURL evalAsURL(String source, Context context) {
        return Eval.evalAsURL(source, context);
    }
}