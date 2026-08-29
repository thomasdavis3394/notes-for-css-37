package org.dromara.easytl.compiler;

import org.dromara.easytl.ast.*;
import org.dromara.easytl.exception.TemplateCompileException;
import org.dromara.easytl.parser.StringSource;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 验证 export 语句的正确性
 * - 是否在顶层
 * - 是否重复 export
 *
 * @author EasyTL Team
 */
public final class ExportValidator {

    private ExportValidator() {
    }

    /**
     * 验证 AST 中的所有 ExportStatement
     *
     * @param root       AST 根节点
     * @param sourceText 源代码文本（用于错误报告）
     */
    public static void validate(TemplateNode root, String sourceText) {
        if (root == null) {
            return;
        }

        Set<String> exportedNames = new HashSet<>();
        List<ASTNode> children = root.getChildren();

        for (ASTNode child : children) {
            if (child instanceof ExportStatement) {
                ExportStatement export = (ExportStatement) child;
                String varName = export.getVariableName();

                if (exportedNames.contains(varName)) {
                    String msg = "Variable '" + varName + "' is already exported";
                    if (export.getKeywordPosition() != null && !export.getKeywordPosition().isUnknown()) {
                        throw new TemplateCompileException(msg, export.getKeywordPosition(), new StringSource(sourceText));
                    }
                    throw new TemplateCompileException(msg);
                }
                exportedNames.add(varName);
            }
        }
    }
}