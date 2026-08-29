package org.dromara.easytl.compiler;

import org.dromara.easytl.Context;
import org.dromara.easytl.Template;
import org.dromara.easytl.ast.TemplateNode;
import org.dromara.easytl.exception.TemplateException;
import org.dromara.easytl.exception.TemplateRuntimeException;
import org.dromara.easytl.loader.TemplateLoader;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;

/**
 * 解释器模板实现
 * <p>
 * 直接解释执行AST，无需额外编译步骤
 * </p>
 *
 * @author EasyTL Team
 * @since 1.0.0
 */
public class InterpreterTemplate implements Template {

    /**
     * AST根节点
     */
    private final TemplateNode ast;

    /**
     * 模板加载器（用于加载 extends 的外部模板）
     */
    private final TemplateLoader templateLoader;

    /**
     * 源代码文本（用于错误报告）
     */
    private final String sourceText;

    /**
     * 是否允许模板中的 Java 类 {@code {% import %}} 语句
     */
    private final boolean templateJavaClassImportEnabled;

    /**
     * 别名到类的映射（用于外部传入的 Java 类导入）
     */
    private final Map<String, Class<?>> importsMap;

    /**
     * 构造函数
     *
     * @param ast AST根节点
     */
    public InterpreterTemplate(TemplateNode ast) {
        this(ast, null, null, true, null);
    }

    /**
     * 构造函数
     *
     * @param ast AST根节点
     * @param templateLoader 模板加载器
     */
    public InterpreterTemplate(TemplateNode ast, TemplateLoader templateLoader) {
        this(ast, templateLoader, null, true, null);
    }

    /**
     * 构造函数
     *
     * @param ast AST根节点
     * @param templateLoader 模板加载器
     * @param sourceText 源代码文本（用于错误报告）
     */
    public InterpreterTemplate(TemplateNode ast, TemplateLoader templateLoader, String sourceText) {
        this(ast, templateLoader, sourceText, true, null);
    }

    /**
     * 构造函数
     *
     * @param ast AST根节点
     * @param templateLoader 模板加载器
     * @param sourceText 源代码文本（用于错误报告）
     * @param templateJavaClassImportEnabled 是否允许模板中的 Java 类 import
     */
    public InterpreterTemplate(TemplateNode ast, TemplateLoader templateLoader, String sourceText,
            boolean templateJavaClassImportEnabled) {
        this(ast, templateLoader, sourceText, templateJavaClassImportEnabled, null);
    }

    /**
     * 构造函数
     *
     * @param ast AST根节点
     * @param templateLoader 模板加载器
     * @param sourceText 源代码文本（用于错误报告）
     * @param templateJavaClassImportEnabled 是否允许模板中的 Java 类 import
     * @param importsMap 别名到类的映射
     */
    public InterpreterTemplate(TemplateNode ast, TemplateLoader templateLoader, String sourceText,
            boolean templateJavaClassImportEnabled, Map<String, Class<?>> importsMap) {
        if (ast == null) {
            throw new IllegalArgumentException("AST cannot be null");
        }
        this.ast = ast;
        this.templateLoader = templateLoader;
        this.sourceText = sourceText;
        this.templateJavaClassImportEnabled = templateJavaClassImportEnabled;
        this.importsMap = importsMap != null ? importsMap : Collections.emptyMap();

        // 运行验证
        TemplateJavaImportValidator.validate(ast, templateJavaClassImportEnabled, sourceText);
        ExportValidator.validate(ast, sourceText);
        // ImportTemplateValidator 需要在有 TemplateLoader 时才验证
        if (templateLoader != null && sourceText != null) {
            ImportTemplateValidator.validate(ast, templateLoader, null, new HashSet<String>(), new HashSet<String>(), sourceText);
        }
    }

    /**
     * 使用给定的上下文渲染模板
     *
     * @param context 上下文对象，包含模板所需的变量和数据
     * @return 渲染后的字符串
     */
    @Override
    public String render(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("Context cannot be null");
        }

        try {
            // 创建求值器（传入模板加载器以支持 extends 功能，传入源代码以支持错误报告）
            Evaluator evaluator = new Evaluator(context, templateLoader, sourceText,
                templateJavaClassImportEnabled, importsMap);

            // 执行AST并获取结果
            Object result = ast.accept(evaluator);

            // 转换为字符串
            return result != null ? result.toString() : "";
        } catch (TemplateException e) {
            // Re-throw TemplateException and its subclasses directly
            throw e;
        } catch (Exception e) {
            throw new TemplateRuntimeException("Failed to render template: " + e.getMessage(), e);
        }
    }

    /**
     * 获取AST根节点
     *
     * @return AST根节点
     */
    public TemplateNode getAst() {
        return ast;
    }

    /**
     * 获取源代码文本
     *
     * @return 源代码文本，如果未提供则返回 null
     */
    public String getSourceText() {
        return sourceText;
    }
}