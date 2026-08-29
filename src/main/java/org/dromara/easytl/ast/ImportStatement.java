package org.dromara.easytl.ast;

import org.dromara.easytl.parser.Token;

/**
 * import类导入语句节点
 * 表示导入Java类，以便在模板中使用其静态方法
 * 格式：{% import java.util.List %} 或 {% import java.util.List as ArrayList %}
 *
 * @author EasyTL Team
 */
public class ImportStatement extends ASTNode {

    /** Java类全限定名 */
    private final String className;

    /** 别名（可为null） */
    private final String alias;

    /** 关键词位置信息（从 import 到类名/别名结束，不含 {% 和 %}） */
    private final Position keywordPosition;

    /**
     * 构造import语句节点
     *
     * @param className Java类全限定名
     */
    public ImportStatement(String className) {
        this(className, null);
    }

    /**
     * 构造import语句节点
     *
     * @param className Java类全限定名
     * @param alias 别名（可为null）
     */
    public ImportStatement(String className, String alias) {
        this(className, alias, (Position) null);
    }

    /**
     * 构造import语句节点（带关键词位置信息）
     *
     * @param className Java类全限定名
     * @param alias 别名（可为null）
     * @param keywordPosition 关键词位置信息
     */
    public ImportStatement(String className, String alias, Position keywordPosition) {
        this.className = className;
        this.alias = alias;
        this.keywordPosition = keywordPosition;
    }

    /**
     * 构造import语句节点（从 Token 获取位置信息）
     *
     * @param className Java类全限定名
     * @param alias 别名（可为null）
     * @param token Token 对象（用于获取位置信息）
     * @deprecated Use {@link #ImportStatement(String, String, Position)} instead
     */
    @Deprecated
    public ImportStatement(String className, String alias, Token token) {
        this(className, alias, positionFromToken(token));
    }

    /**
     * 获取Java类全限定名
     *
     * @return Java类全限定名
     */
    public String getClassName() {
        return className;
    }

    /**
     * 获取别名
     *
     * @return 别名，如果没有指定则返回null
     */
    public String getAlias() {
        return alias;
    }

    /**
     * 获取关键词位置信息
     *
     * @return 关键词位置信息（import className 或 import className as alias 的位置）
     */
    public Position getKeywordPosition() {
        return keywordPosition;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitImportStatement(this);
    }

    @Override
    public String toString() {
        return "ImportStatement{className='" + className + "', alias='" + alias + "'}";
    }
}