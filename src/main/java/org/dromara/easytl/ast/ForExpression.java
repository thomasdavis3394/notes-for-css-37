package org.dromara.easytl.ast;

import org.dromara.easytl.parser.Token;

import java.util.List;

/**
 * for循环语句节点（for表达式）
 * 表示for循环语句：for (变量 in 表达式) {语句块}
 * 与ForStatement（for循环语句块）不同，ForExpression是一个表达式，用于执行副作用操作
 * 只能在自闭合语句块中使用
 * 
 * @author EasyTL Team
 */
public class ForExpression extends ASTNode {
    
    /** 循环变量名 */
    private final String variableName;
    
    /** 索引变量名（可选，用于带索引的循环） */
    private final String indexName;
    
    /** 集合表达式或区间 */
    private final ASTNode iterable;
    
    /** 循环体语句块（语句列表） */
    private final List<ASTNode> bodyStatements;
    
    /**
     * 构造for循环语句节点（单变量形式）
     * 
     * @param variableName 循环变量名
     * @param iterable 集合表达式或区间
     * @param bodyStatements 循环体语句块
     */
    public ForExpression(String variableName, ASTNode iterable, List<ASTNode> bodyStatements) {
        this(null, variableName, iterable, bodyStatements);
    }
    
    /**
     * 构造for循环语句节点（带索引形式）
     * 
     * @param indexName 索引变量名（可选）
     * @param variableName 循环变量名
     * @param iterable 集合表达式或区间
     * @param bodyStatements 循环体语句块
     */
    public ForExpression(String indexName, String variableName, ASTNode iterable, List<ASTNode> bodyStatements) {
        this.indexName = indexName;
        this.variableName = variableName;
        this.iterable = iterable;
        this.bodyStatements = bodyStatements;
    }
    
    /**
     * 构造for循环语句节点（带位置信息）
     *
     * @param indexName 索引变量名（可选）
     * @param variableName 循环变量名
     * @param iterable 集合表达式或区间
     * @param bodyStatements 循环体语句块
     * @param position 位置信息
     */
    public ForExpression(String indexName, String variableName, ASTNode iterable,
                        List<ASTNode> bodyStatements, Position position) {
        super(position);
        this.indexName = indexName;
        this.variableName = variableName;
        this.iterable = iterable;
        this.bodyStatements = bodyStatements;
    }

    /**
     * 构造for循环语句节点（从 Token 范围获取位置信息）
     *
     * @param indexName 索引变量名（可选）
     * @param variableName 循环变量名
     * @param iterable 集合表达式或区间
     * @param bodyStatements 循环体语句块
     * @param startToken 起始 Token
     * @param endToken 结束 Token
     */
    public ForExpression(String indexName, String variableName, ASTNode iterable,
                        List<ASTNode> bodyStatements, Token startToken, Token endToken) {
        super(positionFromTokens(startToken, endToken));
        this.indexName = indexName;
        this.variableName = variableName;
        this.iterable = iterable;
        this.bodyStatements = bodyStatements;
    }
    
    /**
     * 获取循环变量名
     * 
     * @return 循环变量名
     */
    public String getVariableName() {
        return variableName;
    }
    
    /**
     * 获取索引变量名
     * 
     * @return 索引变量名（可能为null）
     */
    public String getIndexName() {
        return indexName;
    }
    
    /**
     * 判断是否有索引变量
     * 
     * @return 如果有索引变量则返回true
     */
    public boolean hasIndex() {
        return indexName != null && !indexName.isEmpty();
    }
    
    /**
     * 获取集合表达式或区间
     * 
     * @return 集合表达式或区间
     */
    public ASTNode getIterable() {
        return iterable;
    }
    
    /**
     * 获取循环体语句块
     * 
     * @return 循环体语句块（语句列表）
     */
    public List<ASTNode> getBodyStatements() {
        return bodyStatements;
    }
    
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitForExpression(this);
    }
    
    @Override
    public String toString() {
        String loopVar = hasIndex() ? indexName + ", " + variableName : variableName;
        return "ForExpression{" +
                "loopVar='" + loopVar + '\'' +
                ", iterable=" + iterable +
                ", bodyStatements=" + bodyStatements +
                '}';
    }
}




