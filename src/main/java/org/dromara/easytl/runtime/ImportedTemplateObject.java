package org.dromara.easytl.runtime;

import org.dromara.easytl.Context;
import org.dromara.easytl.ast.TemplateNode;
import org.dromara.easytl.compiler.Evaluator;
import org.dromara.easytl.exception.TemplateRuntimeException;
import org.dromara.easytl.loader.TemplateLoader;

import java.util.HashMap;
import java.util.Map;

/**
 * 导入的模板对象
 * 封装被导入模板的 AST，提供懒加载、缓存、成员访问和渲染功能
 *
 * @author EasyTL Team
 */
public class ImportedTemplateObject {

    private final TemplateNode templateAst;
    private final TemplateLoader templateLoader;
    private final Context definingContext;
    private final String sourceText;
    private final String templatePath;

    private volatile Map<String, Object> exportedValues;
    private volatile boolean executed;

    /**
     * 构造导入的模板对象
     *
     * @param templateAst    编译后的模板 AST
     * @param templateLoader 模板加载器
     * @param definingContext import 语句所在模板的上下文
     * @param sourceText     源文本（用于错误报告）
     * @param templatePath   模板路径（用于循环导入检测）
     */
    public ImportedTemplateObject(TemplateNode templateAst,
                                  TemplateLoader templateLoader,
                                  Context definingContext,
                                  String sourceText,
                                  String templatePath) {
        this.templateAst = templateAst;
        this.templateLoader = templateLoader;
        this.definingContext = definingContext;
        this.sourceText = sourceText;
        this.templatePath = templatePath;
        this.exportedValues = null;
        this.executed = false;
    }

    /**
     * 获取导出的成员
     *
     * @param name 成员名
     * @return 成员值
     * @throws TemplateRuntimeException 如果成员不存在
     */
    public Object getMember(String name) {
        ensureExecuted();

        Object value = exportedValues.get(name);
        if (value == null && !exportedValues.containsKey(name)) {
            throw new TemplateRuntimeException(
                "Imported template '" + templatePath + "' does not export '" + name + "'");
        }
        return value;
    }

    /**
     * 调用导出的方法
     *
     * @param name 方法名
     * @param args 参数
     * @return 方法返回值
     */
    public Object invokeMethod(String name, Object[] args) {
        // 特殊方法：render
        if ("render".equals(name)) {
            Context ctx = args.length > 0 && args[0] instanceof Context
                ? (Context) args[0]
                : definingContext;
            return render(ctx);
        }

        ensureExecuted();
        Object value = exportedValues.get(name);

        if (value == null && !exportedValues.containsKey(name)) {
            throw new TemplateRuntimeException(
                "Imported template '" + templatePath + "' does not export '" + name + "'");
        }

        if (value instanceof LambdaObject) {
            return ((LambdaObject) value).invoke(args);
        }

        // 不是 Lambda，尝试调用该对象的同名方法
        return ReflectionUtils.invokeMethod(value, name, args);
    }

    /**
     * 渲染模板文本
     *
     * @param ctx 上下文（可为 null，使用 definingContext）
     * @return 渲染结果
     */
    public String render(Context ctx) {
        Context renderContext = (ctx != null ? ctx : definingContext).createChildContext();
        renderContext.setExportRegistry(new ExportRegistry());

        Evaluator evaluator = new Evaluator(renderContext, templateLoader, sourceText, true);
        Object result = templateAst.accept(evaluator);
        return result != null ? result.toString() : "";
    }

    /**
     * 确保模板已执行（懒加载）
     */
    private void ensureExecuted() {
        if (executed) {
            return;
        }

        synchronized (this) {
            if (executed) {
                return;
            }

            // 循环导入检测
            if (ExecutionTracker.isExecuting(templatePath)) {
                // 循环导入，跳过，返回空缓存
                exportedValues = new HashMap<>();
                executed = true;
                return;
            }

            ExecutionTracker.beginExecution(templatePath);
            try {
                Context execContext = definingContext.createChildContext();
                ExportRegistry registry = new ExportRegistry();
                execContext.setExportRegistry(registry);

                Evaluator evaluator = new Evaluator(execContext, templateLoader, sourceText, true);
                templateAst.accept(evaluator);

                exportedValues = new HashMap<>();
                for (String name : registry.getExportedNames()) {
                    exportedValues.put(name, execContext.get(name));
                }

                executed = true;
            } finally {
                ExecutionTracker.endExecution(templatePath);
            }
        }
    }

    /**
     * 获取模板路径
     *
     * @return 模板路径
     */
    public String getTemplatePath() {
        return templatePath;
    }

    /**
     * 检查是否已执行
     *
     * @return 如果已执行返回 true
     */
    public boolean isExecuted() {
        return executed;
    }
}