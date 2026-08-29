package org.dromara.easytl.ast;

import org.dromara.easytl.parser.Token;

/**
 * 区间字面量节点
 * 表示数值区间，支持四种类型：
 * - 全闭区间 [a, b]：a..b
 * - 全开区间 (a, b)：a>..<b
 * - 前闭后开 [a, b)：a..<b
 * - 前开后闭 (a, b]：a>..b
 * 
 * @author EasyTL Team
 */
public class RangeLiteral extends ASTNode {
    
    /**
     * 区间类型枚举
     */
    public enum RangeType {
        /** 全闭区间 [a, b] */
        CLOSED,
        /** 全开区间 (a, b) */
        OPEN,
        /** 前闭后开 [a, b) */
        LEFT_CLOSED_RIGHT_OPEN,
        /** 前开后闭 (a, b] */
        LEFT_OPEN_RIGHT_CLOSED
    }
    
    /** 起始值表达式 */
    private final ASTNode start;
    
    /** 结束值表达式 */
    private final ASTNode end;
    
    /** 区间类型 */
    private final RangeType type;
    
    /**
     * 构造区间字面量节点
     *
     * @param start 起始值表达式
     * @param end 结束值表达式
     * @param type 区间类型
     */
    public RangeLiteral(ASTNode start, ASTNode end, RangeType type) {
        this.start = start;
        this.end = end;
        this.type = type;
    }

    /**
     * 构造区间字面量节点（从 Token 范围获取位置信息）
     *
     * @param start 起始值表达式
     * @param end 结束值表达式
     * @param type 区间类型
     * @param startToken 起始 Token
     * @param endToken 结束 Token
     */
    public RangeLiteral(ASTNode start, ASTNode end, RangeType type, Token startToken, Token endToken) {
        super(positionFromTokens(startToken, endToken));
        this.start = start;
        this.end = end;
        this.type = type;
    }

    /**
     * 构造区间字面量节点（从子节点推断位置信息）
     *
     * @param start 起始值表达式
     * @param end 结束值表达式
     * @param type 区间类型
     * @param position 位置信息
     */
    public RangeLiteral(ASTNode start, ASTNode end, RangeType type, Position position) {
        super(position);
        this.start = start;
        this.end = end;
        this.type = type;
    }
    
    /**
     * 获取起始值表达式
     * 
     * @return 起始值表达式
     */
    public ASTNode getStart() {
        return start;
    }
    
    /**
     * 获取结束值表达式
     * 
     * @return 结束值表达式
     */
    public ASTNode getEnd() {
        return end;
    }
    
    /**
     * 获取区间类型
     * 
     * @return 区间类型
     */
    public RangeType getType() {
        return type;
    }
    
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitRangeLiteral(this);
    }
    
    @Override
    public String toString() {
        return "RangeLiteral{" +
                "start=" + start +
                ", end=" + end +
                ", type=" + type +
                '}';
    }
}

