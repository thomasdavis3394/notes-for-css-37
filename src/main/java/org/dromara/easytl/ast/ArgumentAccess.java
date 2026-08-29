package org.dromara.easytl.ast;

import org.dromara.easytl.parser.Token;

/**
 * 参数访问节点
 * 表示通过索引访问外部传入的参数，例如：{0}、${1}、{{2}}
 *
 * @author EasyTL Team
 */
public class ArgumentAccess extends ASTNode {

    /** 参数索引（0代表第一个参数，1代表第二个，以此类推） */
    private final int index;

    /**
     * 构造参数访问节点
     *
     * @param index 参数索引
     */
    public ArgumentAccess(int index) {
        this.index = index;
    }

    /**
     * 构造参数访问节点（带位置信息）
     *
     * @param index 参数索引
     * @param position 位置信息
     */
    public ArgumentAccess(int index, Position position) {
        super(position);
        this.index = index;
    }

    /**
     * 构造参数访问节点（从 Token 获取位置信息）
     *
     * @param index 参数索引
     * @param token Token 对象（用于获取位置信息）
     */
    public ArgumentAccess(int index, Token token) {
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
        return visitor.visitArgumentAccess(this);
    }

    @Override
    public String toString() {
        return "ArgumentAccess{" + "index=" + index + '}';
    }
}