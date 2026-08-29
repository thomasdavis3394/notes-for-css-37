package org.dromara.easytl.ast;

import org.dromara.easytl.parser.Token;

/**
 * 二元表达式节点
 * 表示包含两个操作数和一个运算符的表达式
 * 例如：a + b, a > b, a && b
 * 
 * @author EasyTL Team
 */
public class BinaryExpression extends ASTNode {
    
    /**
     * 二元运算符类型
     */
    public enum Operator {
        // 算术运算符
        ADD("+"),           // 加
        SUBTRACT("-"),      // 减
        MULTIPLY("*"),      // 乘
        DIVIDE("/"),        // 除
        MODULO("%"),        // 模
        
        // 比较运算符
        GT(">"),            // 大于
        LT("<"),            // 小于
        GTE(">="),          // 大于等于
        LTE("<="),          // 小于等于
        EQ("=="),           // 等于
        NEQ("!="),          // 不等于
        
        // 逻辑运算符
        AND("&&"),          // 逻辑与
        OR("||"),           // 逻辑或
        
        // 正则运算符
        REGEX_MATCH("=~"),      // 正则匹配
        REGEX_NOT_MATCH("!=~"), // 正则不匹配
        
        // 范围运算符
        IN("in"),           // 包含
        NOT_IN("!in"),      // 不包含

        // 集合运算符（JSONPath）
        SUBSETOF("subsetof"),   // 左是右的子集
        ANYOF("anyof"),         // 左与右有交集
        NONEOF("noneof");       // 左与右无交集
        
        private final String symbol;
        
        Operator(String symbol) {
            this.symbol = symbol;
        }
        
        public String getSymbol() {
            return symbol;
        }
    }
    
    /** 左操作数 */
    private final ASTNode left;
    
    /** 运算符 */
    private final Operator operator;
    
    /** 右操作数 */
    private final ASTNode right;
    
    /**
     * 构造二元表达式节点
     *
     * @param left 左操作数
     * @param operator 运算符
     * @param right 右操作数
     */
    public BinaryExpression(ASTNode left, Operator operator, ASTNode right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    /**
     * 构造二元表达式节点（从 Token 范围获取位置信息）
     *
     * @param left 左操作数
     * @param operator 运算符
     * @param right 右操作数
     * @param startToken 起始 Token
     * @param endToken 结束 Token
     */
    public BinaryExpression(ASTNode left, Operator operator, ASTNode right, Token startToken, Token endToken) {
        super(positionFromTokens(startToken, endToken));
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    /**
     * 构造二元表达式节点（从子节点推断位置信息）
     *
     * @param left 左操作数
     * @param operator 运算符
     * @param right 右操作数
     * @param position 位置信息
     */
    public BinaryExpression(ASTNode left, Operator operator, ASTNode right, Position position) {
        super(position);
        this.left = left;
        this.operator = operator;
        this.right = right;
    }
    
    /**
     * 获取左操作数
     * 
     * @return 左操作数
     */
    public ASTNode getLeft() {
        return left;
    }
    
    /**
     * 获取运算符
     * 
     * @return 运算符
     */
    public Operator getOperator() {
        return operator;
    }
    
    /**
     * 获取右操作数
     * 
     * @return 右操作数
     */
    public ASTNode getRight() {
        return right;
    }
    
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitBinaryExpression(this);
    }
    
    @Override
    public String toString() {
        return "BinaryExpression{" +
                "left=" + left +
                ", operator=" + operator +
                ", right=" + right +
                '}';
    }
}

