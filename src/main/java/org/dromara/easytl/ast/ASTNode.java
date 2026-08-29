package org.dromara.easytl.ast;

import org.dromara.easytl.parser.Token;

/**
 * 抽象语法树节点基类
 * 所有AST节点都继承自此类
 *
 * @author EasyTL Team
 */
public abstract class ASTNode {
    
    /** 节点在源代码中的位置信息 */
    protected Position position;
    
    /**
     * 默认构造函数，位置信息为未知
     */
    protected ASTNode() {
        this.position = Position.unknown();
    }
    
    /**
     * 带位置信息的构造函数
     * 
     * @param position 位置信息
     */
    protected ASTNode(Position position) {
        this.position = position != null ? position : Position.unknown();
    }
    
    /**
     * 获取节点的位置信息
     * 
     * @return 位置信息
     */
    public Position getPosition() {
        return position;
    }
    
    /**
     * 设置节点的位置信息
     * 
     * @param position 位置信息
     */
    public void setPosition(Position position) {
        this.position = position != null ? position : Position.unknown();
    }
    
    /**
     * 获取起始行号
     * 
     * @return 起始行号（从1开始）
     */
    public int getStartLine() {
        return position.getStartLine();
    }
    
    /**
     * 获取起始列号
     * 
     * @return 起始列号（从1开始）
     */
    public int getStartColumn() {
        return position.getStartColumn();
    }
    
    /**
     * 获取结束行号
     * 
     * @return 结束行号（从1开始）
     */
    public int getEndLine() {
        return position.getEndLine();
    }
    
    /**
     * 获取结束列号
     * 
     * @return 结束列号（从1开始）
     */
    public int getEndColumn() {
        return position.getEndColumn();
    }
    
    /**
     * 接受访问者访问（访问者模式）
     *
     * @param visitor 访问者对象
     * @return 访问结果
     */
    public abstract <T> T accept(ASTVisitor<T> visitor);

    /**
     * 从单个 Token 创建 Position 对象
     *
     * @param token Token 对象
     * @return Position 对象，如果 token 为 null 则返回 Position.unknown()
     */
    protected static Position positionFromToken(Token token) {
        if (token == null) {
            return Position.unknown();
        }
        return new Position(token.getLine(), token.getStartColumn(),
                            token.getLine(), token.getEndColumn());
    }

    /**
     * 从两个 Token 创建 Position 对象（表示范围）
     *
     * @param startToken 起始 Token
     * @param endToken 结束 Token
     * @return Position 对象，如果任一 token 为 null 则返回 Position.unknown()
     */
    protected static Position positionFromTokens(Token startToken, Token endToken) {
        if (startToken == null || endToken == null) {
            return Position.unknown();
        }
        return new Position(startToken.getLine(), startToken.getStartColumn(),
                            endToken.getLine(), endToken.getEndColumn());
    }

    /**
     * 从两个 AST 节点创建 Position 对象（表示范围）
     * 使用左节点的起始位置和右节点的结束位置
     *
     * @param left 左节点（提供起始位置）
     * @param right 右节点（提供结束位置）
     * @return Position 对象，如果任一节点为 null 则返回 Position.unknown()
     */
    protected static Position positionFromNodes(ASTNode left, ASTNode right) {
        if (left == null || right == null) {
            return Position.unknown();
        }
        Position leftPos = left.getPosition();
        Position rightPos = right.getPosition();
        if (leftPos == null || rightPos == null) {
            return Position.unknown();
        }
        return new Position(leftPos.getStartLine(), leftPos.getStartColumn(),
                            rightPos.getEndLine(), rightPos.getEndColumn());
    }
}

