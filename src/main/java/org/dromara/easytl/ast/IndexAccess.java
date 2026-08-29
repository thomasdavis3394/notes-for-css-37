package org.dromara.easytl.ast;

import org.dromara.easytl.parser.Token;

/**
 * 索引访问节点
 * 表示数组或列表的索引访问，例如：list[0] 或 map['key']
 * 
 * @author EasyTL Team
 */
public class IndexAccess extends ASTNode {
    
    /** 被访问的对象表达式 */
    private final ASTNode object;
    
    /** 索引表达式 */
    private final ASTNode index;
    
    /**
     * 构造索引访问节点
     *
     * @param object 被访问的对象表达式
     * @param index 索引表达式
     */
    public IndexAccess(ASTNode object, ASTNode index) {
        this.object = object;
        this.index = index;
    }

    /**
     * 构造索引访问节点（从 Token 获取位置信息）
     *
     * @param object 对象表达式
     * @param index 索引表达式
     * @param token Token 对象（用于获取位置信息）
     */
    public IndexAccess(ASTNode object, ASTNode index, Token token) {
        super(positionFromToken(token));
        this.object = object;
        this.index = index;
    }

    /**
     * 构造索引访问节点（从位置信息）
     *
     * @param object 对象表达式
     * @param index 索引表达式
     * @param position 位置信息
     */
    public IndexAccess(ASTNode object, ASTNode index, Position position) {
        super(position);
        this.object = object;
        this.index = index;
    }
    
    /**
     * 获取被访问的对象表达式
     * 
     * @return 对象表达式
     */
    public ASTNode getObject() {
        return object;
    }
    
    /**
     * 获取索引表达式
     * 
     * @return 索引表达式
     */
    public ASTNode getIndex() {
        return index;
    }
    
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitIndexAccess(this);
    }
    
    @Override
    public String toString() {
        return "IndexAccess{" +
                "object=" + object +
                ", index=" + index +
                '}';
    }
}

