package org.dromara.easytl.compiler;

import org.dromara.easytl.Template;
import org.dromara.easytl.ast.TemplateNode;
import org.dromara.easytl.exception.TemplateCompileException;
import org.dromara.easytl.loader.TemplateLoader;

import java.util.Collections;
import java.util.Map;

/**
 * 解释器编译器实现
 * <p>
 * 将AST编译为解释执行的模板对象
 * </p>
 *
 * @author EasyTL Team
 * @since 1.0.0
 */
public class InterpreterCompiler implements Compiler {

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
     * 构造函数，不使用模板加载器
     */
    public InterpreterCompiler() {
        this(null, null, true, null);
    }

    /**
     * 构造函数
     *
     * @param templateLoader 模板加载器
     */
    public InterpreterCompiler(TemplateLoader templateLoader) {
        this(templateLoader, null, true, null);
    }

    /**
     * 构造函数
     *
     * @param templateLoader 模板加载器
     * @param sourceText 源代码文本（用于错误报告）
     */
    public InterpreterCompiler(TemplateLoader templateLoader, String sourceText) {
        this(templateLoader, sourceText, true, null);
    }

    /**
     * 构造函数
     *
     * @param templateLoader 模板加载器
     * @param sourceText 源代码文本（用于错误报告）
     * @param templateJavaClassImportEnabled 是否允许模板中的 Java 类 import
     */
    public InterpreterCompiler(TemplateLoader templateLoader, String sourceText,
            boolean templateJavaClassImportEnabled) {
        this(templateLoader, sourceText, templateJavaClassImportEnabled, null);
    }

    /**
     * 构造函数
     *
     * @param templateLoader 模板加载器
     * @param sourceText 源代码文本（用于错误报告）
     * @param templateJavaClassImportEnabled 是否允许模板中的 Java 类 import
     * @param importsMap 别名到类的映射
     */
    public InterpreterCompiler(TemplateLoader templateLoader, String sourceText,
            boolean templateJavaClassImportEnabled, Map<String, Class<?>> importsMap) {
        this.templateLoader = templateLoader;
        this.sourceText = sourceText;
        this.templateJavaClassImportEnabled = templateJavaClassImportEnabled;
        this.importsMap = importsMap != null ? importsMap : Collections.emptyMap();
    }

    /**
     * 编译AST为模板对象
     *
     * @param ast AST根节点
     * @return 编译后的模板对象
     * @throws TemplateCompileException 如果编译失败
     */
    @Override
    public Template compile(TemplateNode ast) throws TemplateCompileException {
        if (ast == null) {
            throw new TemplateCompileException("AST cannot be null");
        }

        try {
            return new InterpreterTemplate(ast, templateLoader, sourceText,
                templateJavaClassImportEnabled, importsMap);
        } catch (TemplateCompileException e) {
            // Re-throw TemplateCompileException directly to preserve position info
            throw e;
        } catch (Exception e) {
            throw new TemplateCompileException("Failed to compile template: " + e.getMessage(), e);
        }
    }
}
