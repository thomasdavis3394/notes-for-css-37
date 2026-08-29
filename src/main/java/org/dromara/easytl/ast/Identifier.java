package org.dromara.easytl.ast;

import org.dromara.easytl.parser.Token;

/**
 * 标识符节点
 * 表示变量名或对象名
 * 
 * @author EasyTL Team
 */
public class Identifier extends ASTNode {
    
    /** 标识符名称 */
    private final String name;
    
    /**
     * 构造标识符节点
     *
     * @param name 标识符名称
     */
    public Identifier(String name) {
        this.name = name;
    }

    /**
     * 构造标识符节点（从 Token 获取位置信息）
     *
     * @param name 标识符名称
     * @param token Token 对象（用于获取位置信息）
     */
    public Identifier(String name, Token token) {
        super(positionFromToken(token));
        this.name = name;
    }
    
    /**
     * 获取标识符名称
     * 
     * @return 标识符名称
     */
    public String getName() {
        return name;
    }
    
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitIdentifier(this);
    }
    
    @Override
    public String toString() {
        return "Identifier{" + "name='" + name + '\'' + '}';
    }
}

