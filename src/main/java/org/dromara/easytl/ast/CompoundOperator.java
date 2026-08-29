package org.dromara.easytl.ast;

/**
 * 复合赋值运算符枚举
 *
 * @author EasyTL Team
 */
public enum CompoundOperator {

    PLUS_ASSIGN("+="),
    MINUS_ASSIGN("-="),
    MULTIPLY_ASSIGN("*="),
    DIVIDE_ASSIGN("/="),
    MODULO_ASSIGN("%="),
    BIT_AND_ASSIGN("&="),
    BIT_OR_ASSIGN("|="),
    BIT_XOR_ASSIGN("^="),
    LEFT_SHIFT_ASSIGN("<<="),
    RIGHT_SHIFT_ASSIGN(">>="),
    UNSIGNED_RIGHT_SHIFT_ASSIGN(">>>=");

    private final String symbol;

    CompoundOperator(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }
}