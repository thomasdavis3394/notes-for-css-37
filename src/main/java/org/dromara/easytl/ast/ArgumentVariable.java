package org.dromara.easytl.ast;

import org.dromara.easytl.parser.Token;

/**
 * 参数变量节点
 * 表示通过 $index 语法访问外部传入的参数，例如：{$0}、${$1}、{{$2}}
 * 与 ArgumentAccess 不同，此节点支持与其他运算符组合使用（如 ??）
 *
 * @author EasyTL Team
 */
public class ArgumentVariable extends ASTNode {

    /** 参数索引（0代表第一个参数，1代表第二个，以此类推） */
    private final int index;

    /**
     * 构造参数变量节点
     *
     * @param index 参数索引
     */
    public ArgumentVariable(int index) {
        this.index = index;
    }

    /**
     * 构造参数变量节点（带位置信息）
     *
     * @param index 参数索引
     * @param position 位置信息
     */
    public ArgumentVariable(int index, Position position) {
        super(position);
        this.index = index;
    }

    /**
     * 构造参数变量节点（从 Token 获取位置信息）
     *
     * @param index 参数索引
     * @param token Token 对象（用于获取位置信息）
     */
    public ArgumentVariable(int index, Token token) {
        super(positionFromToken(token));
        this.index = index;
    }

    /**
     * 获取参数索引
     *
     * @return 参数索引
     */
    public int getIndex() {
        return index;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitArgumentVariable(this);
    }

    @Override
    public String toString() {
        return "ArgumentVariable{" + "index=" + index + '}';
    }
}