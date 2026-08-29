package org.dromara.easytl.ast;

import org.dromara.easytl.parser.Token;

/**
 * let变量声明语句节点
 * 表示变量声明和赋值语句
 * 格式：{% let variableName = expression %}
 * 
 * @author EasyTL Team
 */
public class LetStatement extends ASTNode {
    
    /** 变量名 */
    private final String variableName;
    
    /** 赋值表达式 */
    private final ASTNode value;
    
    /**
     * 构造let语句节点
     *
     * @param variableName 变量名
     * @param value 赋值表达式
     */
    public LetStatement(String variableName, ASTNode value) {
        this.variableName = variableName;
        this.value = value;
    }

    /**
     * 构造let语句节点（从 Token 获取位置信息）
     *
     * @param variableName 变量名
     * @param value 赋值表达式
     * @param token Token 对象（用于获取位置信息）
     */
    public LetStatement(String variableName, ASTNode value, Token token) {
        super(positionFromToken(token));
        this.variableName = variableName;
        this.value = value;
    }
    
    /**
     * 获取变量名
     * 
     * @return 变量名
     */
    public String getVariableName() {
        return variableName;
    }
    
    /**
     * 获取赋值表达式
     * 
     * @return 赋值表达式
     */
    public ASTNode getValue() {
        return value;
    }
    
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitLetStatement(this);
    }
    
    @Override
    public String toString() {
        return "LetStatement{" +
                "variableName='" + variableName + '\'' +
                ", value=" + value +
                '}';
    }
}

