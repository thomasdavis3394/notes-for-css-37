package org.dromara.easytl.ast;

import org.dromara.easytl.parser.Token;

import java.util.List;

/**
 * if条件语句块节点
 * 表示条件分支语句，支持 if、else if、else
 * 格式：{% if condition }...{% else if condition }...{% else }...{/% if }
 * 
 * @author EasyTL Team
 */
public class IfStatement extends StatementBlock {
    
    /**
     * else if分支
     */
    public static class ElseIfClause {
        /** 条件表达式 */
        private final ASTNode condition;
        
        /** 分支内容（子节点列表） */
        private final List<ASTNode> body;
        
        public ElseIfClause(ASTNode condition, List<ASTNode> body) {
            this.condition = condition;
            this.body = body;
        }
        
        public ASTNode getCondition() {
            return condition;
        }
        
        public List<ASTNode> getBody() {
            return body;
        }
        
        @Override
        public String toString() {
            return "ElseIfClause{" +
                    "condition=" + condition +
                    ", body=" + body +
                    '}';
        }
    }
    
    /** if条件表达式 */
    private final ASTNode condition;
    
    /** if分支内容（子节点列表） */
    private final List<ASTNode> thenBody;
    
    /** else if分支列表（可选） */
    private final List<ElseIfClause> elseIfClauses;
    
    /** else分支内容（可选） */
    private final List<ASTNode> elseBody;
    
    /**
     * 构造if语句节点
     *
     * @param condition if条件表达式
     * @param thenBody if分支内容
     * @param elseIfClauses else if分支列表
     * @param elseBody else分支内容
     */
    public IfStatement(ASTNode condition, List<ASTNode> thenBody,
                       List<ElseIfClause> elseIfClauses, List<ASTNode> elseBody) {
        this.condition = condition;
        this.thenBody = thenBody;
        this.elseIfClauses = elseIfClauses;
        this.elseBody = elseBody;
    }

    /**
     * 构造if语句节点（从 Token 范围获取位置信息）
     *
     * @param condition 条件表达式
     * @param thenBody then分支内容
     * @param elseIfClauses else if分支列表
     * @param elseBody else分支内容
     * @param startToken 起始 Token
     * @param endToken 结束 Token
     */
    public IfStatement(ASTNode condition, List<ASTNode> thenBody,
                       List<ElseIfClause> elseIfClauses, List<ASTNode> elseBody,
                       Token startToken, Token endToken) {
        super(positionFromTokens(startToken, endToken));
        this.condition = condition;
        this.thenBody = thenBody;
        this.elseIfClauses = elseIfClauses;
        this.elseBody = elseBody;
    }
    
    /**
     * 获取if条件表达式
     * 
     * @return if条件表达式
     */
    public ASTNode getCondition() {
        return condition;
    }
    
    /**
     * 获取if分支内容
     * 
     * @return if分支内容
     */
    public List<ASTNode> getThenBody() {
        return thenBody;
    }
    
    /**
     * 获取else if分支列表
     * 
     * @return else if分支列表
     */
    public List<ElseIfClause> getElseIfClauses() {
        return elseIfClauses;
    }
    
    /**
     * 获取else分支内容
     * 
     * @return else分支内容
     */
    public List<ASTNode> getElseBody() {
        return elseBody;
    }
    
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitIfStatement(this);
    }
    
    @Override
    public String toString() {
        return "IfStatement{" +
                "condition=" + condition +
                ", thenBody=" + thenBody +
                ", elseIfClauses=" + elseIfClauses +
                ", elseBody=" + elseBody +
                '}';
    }
}

