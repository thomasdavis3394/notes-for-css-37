package org.dromara.easytl.ast;

import java.util.List;

/**
 * 字符串模板字面量节点
 * 表示反引号包裹的字符串模板，支持嵌入表达式
 * 例如：`Hello {name}!`
 * 
 * @author EasyTL Team
 */
public class TemplateLiteral extends ASTNode {
    
    /** 字符串部分列表（奇数索引位置是文本，偶数索引位置是表达式） */
    private final List<String> strings;
    
    /** 嵌入的表达式列表 */
    private final List<ASTNode> expressions;
    
    /**
     * 构造字符串模板字面量节点
     * 
     * @param strings 字符串部分列表
     * @param expressions 嵌入的表达式列表
     */
    public TemplateLiteral(List<String> strings, List<ASTNode> expressions) {
        this.strings = strings;
        this.expressions = expressions;
    }
    
    /**
     * 获取字符串部分列表
     * 
     * @return 字符串部分列表
     */
    public List<String> getStrings() {
        return strings;
    }
    
    /**
     * 获取嵌入的表达式列表
     * 
     * @return 表达式列表
     */
    public List<ASTNode> getExpressions() {
        return expressions;
    }
    
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitTemplateLiteral(this);
    }
    
    @Override
    public String toString() {
        return "TemplateLiteral{" +
                "strings=" + strings +
                ", expressions=" + expressions +
                '}';
    }
}

