package org.dromara.easytl.compiler;

import org.dromara.easytl.Template;
import org.dromara.easytl.ast.TemplateNode;
import org.dromara.easytl.exception.TemplateCompileException;

/**
 * 编译器接口
 * <p>
 * 将AST编译为可执行的模板对象
 * </p>
 *
 * @author EasyTL Team
 * @since 1.0.0
 */
public interface Compiler {

    /**
     * 编译AST为模板对象
     *
     * @param ast AST根节点
     * @return 编译后的模板对象
     * @throws TemplateCompileException 如果编译失败
     */
    Template compile(TemplateNode ast) throws TemplateCompileException;
}
