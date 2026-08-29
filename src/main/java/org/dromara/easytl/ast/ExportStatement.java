package org.dromara.easytl.ast;

import org.dromara.easytl.parser.Token;

/**
 * 导出变量语句节点
 * 表示导出变量供其他模板使用
 * 格式：{% export let x = value %} 或 {% export x %}
 *
 * @author EasyTL Team
 */
public class ExportStatement extends ASTNode {

    /** 要导出的变量名 */
    private final String variableName;

    /** 声明并导出时的 LetStatement（可为 null） */
    private final LetStatement letStatement;

    /** 关键词位置信息（从 export 到变量名结束，不含 {% 和 %}） */
    private final Position keywordPosition;

    /**
     * 构造导出语句节点
     *
     * @param variableName 要导出的变量名
     * @param letStatement 声明并导出时的 LetStatement（可为 null）
     */
    public ExportStatement(String variableName, LetStatement letStatement) {
        this(variableName, letStatement, (Position) null);
    }

    /**
     * 构造导出语句节点（带关键词位置信息）
     *
     * @param variableName 要导出的变量名
     * @param letStatement 声明并导出时的 LetStatement（可为 null）
     * @param keywordPosition 关键词位置信息（export x 或 export let x = value 的位置）
     */
    public ExportStatement(String variableName, LetStatement letStatement, Position keywordPosition) {
        this.variableName = variableName;
        this.letStatement = letStatement;
        this.keywordPosition = keywordPosition;
    }

    /**
     * 构造导出语句节点（从 Token 获取位置信息）
     *
     * @param variableName 要导出的变量名
     * @param letStatement 声明并导出时的 LetStatement（可为 null）
     * @param token Token 对象（用于获取位置信息）
     * @deprecated Use {@link #ExportStatement(String, LetStatement, Position)} instead
     */
    @Deprecated
    public ExportStatement(String variableName, LetStatement letStatement, Token token) {
        this(variableName, letStatement, positionFromToken(token));
    }

    /**
     * 获取要导出的变量名
     *
     * @return 变量名
     */
    public String getVariableName() {
        return variableName;
    }

    /**
     * 获取声明并导出时的 LetStatement
     *
     * @return LetStatement，如果只是导出已声明变量则返回 null
     */
    public LetStatement getLetStatement() {
        return letStatement;
    }

    /**
     * 获取关键词位置信息
     *
     * @return 关键词位置信息（export x 或 export let x = value 的位置）
     */
    public Position getKeywordPosition() {
        return keywordPosition;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitExportStatement(this);
    }

    @Override
    public String toString() {
        return "ExportStatement{variableName='" + variableName + "', letStatement=" + letStatement + "}";
    }
}