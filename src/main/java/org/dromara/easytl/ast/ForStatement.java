package org.dromara.easytl.ast;

import org.dromara.easytl.parser.Token;

import java.util.List;

/**
 * for循环语句块节点
 * 表示循环遍历语句，支持遍历集合和区间
 * 格式：{% for item in collection }...{/% for }
 * 或：{% for index, item in collection }...{/% for }
 * 
 * @author EasyTL Team
 */
public class ForStatement extends StatementBlock {
    
    /** 循环变量名（存储元素值） */
    private final String itemVariable;
    
    /** 索引变量名（可选，存储索引） */
    private final String indexVariable;
    
    /** 集合表达式（可能是数组、List、区间等） */
    private final ASTNode iterable;
    
    /** 循环体内容（子节点列表） */
    private final List<ASTNode> body;
    
    /**
     * 构造for循环节点（不带索引）
     * 
     * @param itemVariable 循环变量名
     * @param iterable 集合表达式
     * @param body 循环体内容
     */
    public ForStatement(String itemVariable, ASTNode iterable, List<ASTNode> body) {
        this(null, itemVariable, iterable, body);
    }
    
    /**
     * 构造for循环节点（带索引）
     *
     * @param indexVariable 索引变量名
     * @param itemVariable 循环变量名
     * @param iterable 集合表达式
     * @param body 循环体内容
     */
    public ForStatement(String indexVariable, String itemVariable,
                        ASTNode iterable, List<ASTNode> body) {
        this.indexVariable = indexVariable;
        this.itemVariable = itemVariable;
        this.iterable = iterable;
        this.body = body;
    }

    /**
     * 构造for循环节点（从 Token 范围获取位置信息）
     *
     * @param indexVariable 索引变量名
     * @param itemVariable 循环变量名
     * @param iterable 集合表达式
     * @param body 循环体内容
     * @param startToken 起始 Token
     * @param endToken 结束 Token
     */
    public ForStatement(String indexVariable, String itemVariable,
                        ASTNode iterable, List<ASTNode> body,
                        Token startToken, Token endToken) {
        super(positionFromTokens(startToken, endToken));
        this.indexVariable = indexVariable;
        this.itemVariable = itemVariable;
        this.iterable = iterable;
        this.body = body;
    }
    
    /**
     * 获取循环变量名
     * 
     * @return 循环变量名
     */
    public String getItemVariable() {
        return itemVariable;
    }
    
    /**
     * 获取索引变量名
     * 
     * @return 索引变量名（可能为null）
     */
    public String getIndexVariable() {
        return indexVariable;
    }
    
    /**
     * 是否有索引变量
     * 
     * @return 如果有索引变量返回true
     */
    public boolean hasIndexVariable() {
        return indexVariable != null;
    }
    
    /**
     * 获取集合表达式
     * 
     * @return 集合表达式
     */
    public ASTNode getIterable() {
        return iterable;
    }
    
    /**
     * 获取循环体内容
     * 
     * @return 循环体内容
     */
    public List<ASTNode> getBody() {
        return body;
    }
    
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitForStatement(this);
    }
    
    @Override
    public String toString() {
        return "ForStatement{" +
                "indexVariable='" + indexVariable + '\'' +
                ", itemVariable='" + itemVariable + '\'' +
                ", iterable=" + iterable +
                ", body=" + body +
                '}';
    }
}

