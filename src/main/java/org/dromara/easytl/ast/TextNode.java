package org.dromara.easytl.ast;

/**
 * 文本节点
 * 表示模板中的纯文本内容
 * 
 * @author EasyTL Team
 */
public class TextNode extends ASTNode {
    
    /** 文本内容 */
    private final String text;
    
    /**
     * 构造文本节点
     * 
     * @param text 文本内容
     */
    public TextNode(String text) {
        this.text = text;
    }
    
    /**
     * 获取文本内容
     * 
     * @return 文本内容
     */
    public String getText() {
        return text;
    }
    
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitTextNode(this);
    }
    
    @Override
    public String toString() {
        return "TextNode{" + "text='" + text + '\'' + '}';
    }
}

