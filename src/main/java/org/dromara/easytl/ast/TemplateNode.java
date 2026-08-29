package org.dromara.easytl.ast;

import java.util.List;

/**
 * 模板根节点
 * 表示整个模板的AST根节点，包含所有子节点
 * 
 * @author EasyTL Team
 */
public class TemplateNode extends ASTNode {
    
    /** 子节点列表 */
    private final List<ASTNode> children;
    
    /**
     * 构造模板根节点
     * 
     * @param children 子节点列表
     */
    public TemplateNode(List<ASTNode> children) {
        this.children = children;
    }
    
    /**
     * 获取子节点列表
     * 
     * @return 子节点列表
     */
    public List<ASTNode> getChildren() {
        return children;
    }
    
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitTemplateNode(this);
    }
    
    @Override
    public String toString() {
        return "TemplateNode{" + "children=" + children + '}';
    }
}

