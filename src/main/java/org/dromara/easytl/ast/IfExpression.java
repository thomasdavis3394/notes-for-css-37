package org.dromara.easytl.ast;

import org.dromara.easytl.parser.Token;

import java.util.List;

/**
 * 条件表达式节点（if表达式）
 * 表示条件表达式：if (条件) {语句块} else if (条件) {语句块} else {语句块}
 * 与IfStatement（条件语句块）不同，IfExpression是一个表达式，可以作为值使用
 * 只能在自闭合语句块中使用
 * 
 * @author EasyTL Team
 */
public class IfExpression extends ASTNode {
    
    /**
     * else if分支
     */
    public static class ElseIfClause {
        private final ASTNode condition;
        private final List<ASTNode> statements;
        
        public ElseIfClause(ASTNode condition, List<ASTNode> statements) {
            this.condition = condition;
            this.statements = statements;
        }
        
        public ASTNode getCondition() {
            return condition;
        }
        
        public List<ASTNode> getStatements() {
            return statements;
        }
        
        @Override
        public String toString() {
            return "ElseIfClause{" +
                    "condition=" + condition +
                    ", statements=" + statements +
                    '}';
        }
    }
    
    /** 条件表达式 */
    private final ASTNode condition;
    
    /** then语句块（语句列表） */
    private final List<ASTNode> thenStatements;
    
    /** else if分支列表（可选） */
    private final List<ElseIfClause> elseIfClauses;
    
    /** else语句块（可选，语句列表） */
    private final List<ASTNode> elseStatements;
    
    /**
     * 构造条件表达式节点（仅if）
     * 
     * @param condition 条件表达式
     * @param thenStatements then语句块
     */
    public IfExpression(ASTNode condition, List<ASTNode> thenStatements) {
        this(condition, thenStatements, null, null);
    }
    
    /**
     * 构造条件表达式节点（if-else）
     * 
     * @param condition 条件表达式
     * @param thenStatements then语句块
     * @param elseStatements else语句块
     */
    public IfExpression(ASTNode condition, List<ASTNode> thenStatements, List<ASTNode> elseStatements) {
        this(condition, thenStatements, null, elseStatements);
    }
    
    /**
     * 构造条件表达式节点（完整形式）
     * 
     * @param condition 条件表达式
     * @param thenStatements then语句块
     * @param elseIfClauses else if分支列表
     * @param elseStatements else语句块
     */
    public IfExpression(ASTNode condition, List<ASTNode> thenStatements, 
                       List<ElseIfClause> elseIfClauses, List<ASTNode> elseStatements) {
        this.condition = condition;
        this.thenStatements = thenStatements;
        this.elseIfClauses = elseIfClauses;
        this.elseStatements = elseStatements;
    }
    
    /**
     * 构造条件表达式节点（带位置信息）
     *
     * @param condition 条件表达式
     * @param thenStatements then语句块
     * @param elseIfClauses else if分支列表
     * @param elseStatements else语句块
     * @param position 位置信息
     */
    public IfExpression(ASTNode condition, List<ASTNode> thenStatements,
                       List<ElseIfClause> elseIfClauses, List<ASTNode> elseStatements,
                       Position position) {
        super(position);
        this.condition = condition;
        this.thenStatements = thenStatements;
        this.elseIfClauses = elseIfClauses;
        this.elseStatements = elseStatements;
    }

    /**
     * 构造条件表达式节点（从 Token 范围获取位置信息）
     *
     * @param condition 条件表达式
     * @param thenStatements then语句块
     * @param elseIfClauses else if分支列表
     * @param elseStatements else语句块
     * @param startToken 起始 Token
     * @param endToken 结束 Token
     */
    public IfExpression(ASTNode condition, List<ASTNode> thenStatements,
                       List<ElseIfClause> elseIfClauses, List<ASTNode> elseStatements,
                       Token startToken, Token endToken) {
        super(positionFromTokens(startToken, endToken));
        this.condition = condition;
        this.thenStatements = thenStatements;
        this.elseIfClauses = elseIfClauses;
        this.elseStatements = elseStatements;
    }
    
    /**
     * 获取条件表达式
     * 
     * @return 条件表达式
     */
    public ASTNode getCondition() {
        return condition;
    }
    
    /**
     * 获取then语句块
     * 
     * @return then语句块（语句列表）
     */
    public List<ASTNode> getThenStatements() {
        return thenStatements;
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
     * 获取else语句块
     * 
     * @return else语句块（语句列表）
     */
    public List<ASTNode> getElseStatements() {
        return elseStatements;
    }
    
    /**
     * 判断是否有else分支
     * 
     * @return 如果有else分支则返回true
     */
    public boolean hasElse() {
        return elseStatements != null && !elseStatements.isEmpty();
    }
    
    /**
     * 判断是否有else if分支
     * 
     * @return 如果有else if分支则返回true
     */
    public boolean hasElseIf() {
        return elseIfClauses != null && !elseIfClauses.isEmpty();
    }
    
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitIfExpression(this);
    }
    
    @Override
    public String toString() {
        return "IfExpression{" +
                "condition=" + condition +
                ", thenStatements=" + thenStatements +
                ", elseIfClauses=" + elseIfClauses +
                ", elseStatements=" + elseStatements +
                '}';
    }
}




