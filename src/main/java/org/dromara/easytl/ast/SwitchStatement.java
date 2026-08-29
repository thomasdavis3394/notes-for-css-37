package org.dromara.easytl.ast;

import org.dromara.easytl.parser.Token;

import java.util.List;

/**
 * switch语句块节点
 * 表示多分支选择语句
 * 格式：{% switch expr }{% case value }...{% default }...{/% switch }
 * 
 * @author EasyTL Team
 */
public class SwitchStatement extends StatementBlock {
    
    /** switch条件表达式 */
    private final ASTNode condition;
    
    /** case分支列表 */
    private final List<CaseClause> cases;
    
    /** default分支内容（可选） */
    private final List<ASTNode> defaultBody;
    
    /**
     * 构造switch语句节点
     *
     * @param condition switch条件表达式
     * @param cases case分支列表
     * @param defaultBody default分支内容
     */
    public SwitchStatement(ASTNode condition, List<CaseClause> cases, List<ASTNode> defaultBody) {
        this.condition = condition;
        this.cases = cases;
        this.defaultBody = defaultBody;
    }

    /**
     * 构造switch语句节点（从 Token 范围获取位置信息）
     *
     * @param condition switch条件表达式
     * @param cases case分支列表
     * @param defaultBody default分支内容
     * @param startToken 起始 Token
     * @param endToken 结束 Token
     */
    public SwitchStatement(ASTNode condition, List<CaseClause> cases, List<ASTNode> defaultBody,
                           Token startToken, Token endToken) {
        super(positionFromTokens(startToken, endToken));
        this.condition = condition;
        this.cases = cases;
        this.defaultBody = defaultBody;
    }
    
    /**
     * 获取switch条件表达式
     * 
     * @return switch条件表达式
     */
    public ASTNode getCondition() {
        return condition;
    }
    
    /**
     * 获取case分支列表
     * 
     * @return case分支列表
     */
    public List<CaseClause> getCases() {
        return cases;
    }
    
    /**
     * 获取default分支内容
     * 
     * @return default分支内容（可能为null）
     */
    public List<ASTNode> getDefaultBody() {
        return defaultBody;
    }
    
    /**
     * 是否有default分支
     * 
     * @return 如果有default分支返回true
     */
    public boolean hasDefaultBody() {
        return defaultBody != null && !defaultBody.isEmpty();
    }
    
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitSwitchStatement(this);
    }
    
    @Override
    public String toString() {
        return "SwitchStatement{" +
                "condition=" + condition +
                ", cases=" + cases +
                ", defaultBody=" + defaultBody +
                '}';
    }
}

