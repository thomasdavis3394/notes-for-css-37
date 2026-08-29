package org.dromara.easytl.ast;

import org.dromara.easytl.parser.Token;

/**
 * extends模板扩展语句节点
 * 表示扩展/包含另一个模板文件
 * 格式：{% extends 'templatePath' %}
 * 
 * @author EasyTL Team
 */
public class ExtendsStatement extends ASTNode {
    
    /** 模板文件路径 */
    private final String templatePath;
    
    /**
     * 构造extends语句节点
     *
     * @param templatePath 模板文件路径
     */
    public ExtendsStatement(String templatePath) {
        this.templatePath = templatePath;
    }

    /**
     * 构造extends语句节点（从 Token 获取位置信息）
     *
     * @param templatePath 模板文件路径
     * @param token Token 对象（用于获取位置信息）
     */
    public ExtendsStatement(String templatePath, Token token) {
        super(positionFromToken(token));
        this.templatePath = templatePath;
    }
    
    /**
     * 获取模板文件路径
     * 
     * @return 模板文件路径
     */
    public String getTemplatePath() {
        return templatePath;
    }
    
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitExtendsStatement(this);
    }
    
    @Override
    public String toString() {
        return "ExtendsStatement{" + "templatePath='" + templatePath + '\'' + '}';
    }
}

