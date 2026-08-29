package org.dromara.easytl.ast;

import org.dromara.easytl.parser.Token;

import java.util.List;

/**
 * case子句节点
 * 表示switch语句中的一个case分支
 * 格式：{% case value }...
 * 
 * @author EasyTL Team
 */
public class CaseClause extends ASTNode {
    
    /** case匹配值表达式 */
    private final ASTNode value;
    
    /** case分支内容（子节点列表） */
    private final List<ASTNode> body;
    
    /**
     * 构造case子句节点
     *
     * @param value case匹配值表达式
     * @param body case分支内容
     */
    public CaseClause(ASTNode value, List<ASTNode> body) {
        this.value = value;
        this.body = body;
    }

    /**
     * 构造case子句节点（从 Token 范围获取位置信息）
     *
     * @param value case匹配值表达式
     * @param body case分支内容
     * @param startToken 起始 Token
     * @param endToken 结束 Token
     */
    public CaseClause(ASTNode value, List<ASTNode> body, Token startToken, Token endToken) {
        super(positionFromTokens(startToken, endToken));
        this.value = value;
        this.body = body;
    }
    
    /**
     * 获取case匹配值表达式
     * 
     * @return case匹配值表达式
     */
    public ASTNode getValue() {
        return value;
    }
    
    /**
     * 获取case分支内容
     * 
     * @return case分支内容
     */
    public List<ASTNode> getBody() {
        return body;
    }
    
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitCaseClause(this);
    }
    
    @Override
    public String toString() {
        return "CaseClause{" +
                "value=" + value +
                ", body=" + body +
                '}';
    }
}

