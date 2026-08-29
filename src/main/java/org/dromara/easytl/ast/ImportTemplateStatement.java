package org.dromara.easytl.ast;

import org.dromara.easytl.parser.Token;

/**
 * 模板导入语句节点
 * 表示导入其他模板文件
 * 格式：{% import 'path/to/template.etl' as alias %}
 *
 * @author EasyTL Team
 */
public class ImportTemplateStatement extends ASTNode {

    /** 模板文件路径 */
    private final String templatePath;

    /** 别名（必须指定） */
    private final String alias;

    /** 关键词位置信息（从 import 到 alias 结束，不含 {% 和 %}） */
    private final Position keywordPosition;

    /**
     * 构造模板导入语句节点
     *
     * @param templatePath 模板文件路径
     * @param alias 别名
     */
    public ImportTemplateStatement(String templatePath, String alias) {
        this(templatePath, alias, (Position) null);
    }

    /**
     * 构造模板导入语句节点（带关键词位置信息）
     *
     * @param templatePath 模板文件路径
     * @param alias 别名
     * @param keywordPosition 关键词位置信息
     */
    public ImportTemplateStatement(String templatePath, String alias, Position keywordPosition) {
        this.templatePath = templatePath;
        this.alias = alias;
        this.keywordPosition = keywordPosition;
    }

    /**
     * 构造模板导入语句节点（从 Token 获取位置信息）
     *
     * @param templatePath 模板文件路径
     * @param alias 别名
     * @param token Token 对象（用于获取位置信息）
     * @deprecated Use {@link #ImportTemplateStatement(String, String, Position)} instead
     */
    @Deprecated
    public ImportTemplateStatement(String templatePath, String alias, Token token) {
        this(templatePath, alias, positionFromToken(token));
    }

    /**
     * 获取模板文件路径
     *
     * @return 模板文件路径
     */
    public String getTemplatePath() {
        return templatePath;
    }

    /**
     * 获取别名
     *
     * @return 别名
     */
    public String getAlias() {
        return alias;
    }

    /**
     * 获取关键词位置信息
     *
     * @return 关键词位置信息（import 'path' as alias 的位置）
     */
    public Position getKeywordPosition() {
        return keywordPosition;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitImportTemplateStatement(this);
    }

    @Override
    public String toString() {
        return "ImportTemplateStatement{templatePath='" + templatePath + "', alias='" + alias + "'}";
    }
}