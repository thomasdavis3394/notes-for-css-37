package org.dromara.easytl.ast;

import org.dromara.easytl.parser.Token;

import java.util.List;

/**
 * 列表字面量节点
 * 表示方括号包裹的列表，例如：[1, 2, 3] 或 ['a', 'b', 'c']
 * 支持混合类型和嵌套列表
 * 
 * @author EasyTL Team
 */
public class ListLiteral extends ASTNode {
    
    /** 列表元素表达式列表 */
    private final List<ASTNode> elements;
    
    /**
     * 构造列表字面量节点
     *
     * @param elements 元素表达式列表
     */
    public ListLiteral(List<ASTNode> elements) {
        this.elements = elements;
    }

    /**
     * 构造列表字面量节点（从 Token 范围获取位置信息）
     *
     * @param elements 元素列表
     * @param startToken 起始 Token
     * @param endToken 结束 Token
     */
    public ListLiteral(List<ASTNode> elements, Token startToken, Token endToken) {
        super(positionFromTokens(startToken, endToken));
        this.elements = elements;
    }
    
    /**
     * 获取元素表达式列表
     * 
     * @return 元素表达式列表
     */
    public List<ASTNode> getElements() {
        return elements;
    }
    
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitListLiteral(this);
    }
    
    @Override
    public String toString() {
        return "ListLiteral{" + "elements=" + elements + '}';
    }
}

