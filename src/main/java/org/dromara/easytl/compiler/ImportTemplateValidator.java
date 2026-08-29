package org.dromara.easytl.compiler;

import org.dromara.easytl.ast.*;
import org.dromara.easytl.exception.TemplateCompileException;
import org.dromara.easytl.loader.TemplateLoader;
import org.dromara.easytl.parser.Lexer;
import org.dromara.easytl.parser.Parser;
import org.dromara.easytl.parser.StringSource;
import org.dromara.easytl.parser.TokenStream;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 验证 import template 语句
 * - 模板文件是否存在
 * - 别名是否冲突
 * - 循环导入检测
 *
 * @author EasyTL Team
 */
public final class ImportTemplateValidator {

    private ImportTemplateValidator() {
    }

    /**
     * 验证 AST 中的所有 ImportTemplateStatement
     *
     * @param root               AST 根节点
     * @param loader             模板加载器
     * @param currentTemplatePath 当前模板路径（用于相对路径解析和循环导入检测）
     * @param definedVariables   已定义的变量名集合
     * @param visitedTemplates   已访问的模板路径集合（用于循环导入检测）
     * @param sourceText         源代码文本（用于错误报告）
     */
    public static void validate(TemplateNode root,
                                TemplateLoader loader,
                                String currentTemplatePath,
                                Set<String> definedVariables,
                                Set<String> visitedTemplates,
                                String sourceText) {
        if (root == null || loader == null) {
            return;
        }

        Set<String> importedAliases = new HashSet<>();
        collectImports(root, loader, currentTemplatePath, definedVariables, importedAliases,
                       visitedTemplates != null ? visitedTemplates : new HashSet<>(), sourceText);
    }

    private static void collectImports(TemplateNode root,
                                       TemplateLoader loader,
                                       String currentTemplatePath,
                                       Set<String> definedVariables,
                                       Set<String> importedAliases,
                                       Set<String> visitedTemplates,
                                       String sourceText) {
        // 检测循环导入
        if (currentTemplatePath != null && visitedTemplates.contains(currentTemplatePath)) {
            return; // 已经在访问路径中，跳过
        }
        if (currentTemplatePath != null) {
            visitedTemplates.add(currentTemplatePath);
        }

        List<ASTNode> children = root.getChildren();
        Set<String> localDefinedVariables = new HashSet<>();
        if (definedVariables != null) {
            localDefinedVariables.addAll(definedVariables);
        }

        for (ASTNode child : children) {
            if (child instanceof ImportTemplateStatement) {
                ImportTemplateStatement importStmt = (ImportTemplateStatement) child;
                String alias = importStmt.getAlias();
                String templatePath = importStmt.getTemplatePath();

                // 检查别名冲突
                if (localDefinedVariables.contains(alias)) {
                    String msg = "Alias '" + alias + "' conflicts with an existing variable";
                    if (importStmt.getKeywordPosition() != null && !importStmt.getKeywordPosition().isUnknown()) {
                        throw new TemplateCompileException(msg, importStmt.getKeywordPosition(), new StringSource(sourceText));
                    }
                    throw new TemplateCompileException(msg);
                }

                if (importedAliases.contains(alias)) {
                    String msg = "Alias '" + alias + "' is already used for another import";
                    if (importStmt.getKeywordPosition() != null && !importStmt.getKeywordPosition().isUnknown()) {
                        throw new TemplateCompileException(msg, importStmt.getKeywordPosition(), new StringSource(sourceText));
                    }
                    throw new TemplateCompileException(msg);
                }

                // 检查模板文件是否存在
                try {
                    String content = loader.load(templatePath);
                    importedAliases.add(alias);
                    localDefinedVariables.add(alias);

                    // 递归检查被导入模板中的 import 语句
                    Lexer lexer = new Lexer(new StringSource(content));
                    TokenStream tokens = new TokenStream(lexer);
                    Parser parser = new Parser(tokens);
                    TemplateNode importedAst = parser.parse();
                    collectImports(importedAst, loader, templatePath, null, new HashSet<>(),
                                   new HashSet<>(visitedTemplates), content);

                } catch (TemplateCompileException e) {
                    String msg = "Cannot import template '" + templatePath + "': " + e.getMessage();
                    if (importStmt.getKeywordPosition() != null && !importStmt.getKeywordPosition().isUnknown()) {
                        throw new TemplateCompileException(msg, importStmt.getKeywordPosition(), new StringSource(sourceText));
                    }
                    throw new TemplateCompileException(msg);
                } catch (Exception e) {
                    String msg = "Failed to parse imported template '" + templatePath + "': " + e.getMessage();
                    if (importStmt.getKeywordPosition() != null && !importStmt.getKeywordPosition().isUnknown()) {
                        throw new TemplateCompileException(msg, importStmt.getKeywordPosition(), new StringSource(sourceText));
                    }
                    throw new TemplateCompileException(msg);
                }

            } else if (child instanceof LetStatement) {
                LetStatement letStmt = (LetStatement) child;
                localDefinedVariables.add(letStmt.getVariableName());
            }
        }
    }
}