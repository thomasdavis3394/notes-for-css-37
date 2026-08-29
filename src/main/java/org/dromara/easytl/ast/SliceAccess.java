package org.dromara.easytl.ast;

import org.dromara.easytl.parser.Token;

/**
 * 数组切片访问节点（JSONPath）
 * 表示 {@code [start:end(:step)]} 切片访问，start 含、end 不含，均可省略（null 表示省略），支持负数；
 * step 为可选步长（null 表示省略，默认 1），负数表示反向取值，0 表示空结果
 * 例如：store.book[1:4]、store.book[:2]、store.book[-2:]、store.book[0:4:2]、store.book[::-1]
 *
 * @author EasyTL Team
 */
public class SliceAccess extends ASTNode {

    /** 被访问的对象表达式 */
    private final ASTNode object;

    /** 起始下标表达式（含），为 null 表示省略 */
    private final ASTNode start;

    /** 结束下标表达式（不含），为 null 表示省略 */
    private final ASTNode end;

    /** 步长表达式，为 null 表示省略（默认 1） */
    private final ASTNode step;

    /**
     * 构造切片访问节点（无步长）
     *
     * @param object 被访问的对象表达式
     * @param start 起始下标表达式（含），可为 null
     * @param end 结束下标表达式（不含），可为 null
     */
    public SliceAccess(ASTNode object, ASTNode start, ASTNode end) {
        this(object, start, end, null, (Position) null);
    }

    /**
     * 构造切片访问节点（从 Token 获取位置信息，无步长）
     *
     * @param object 被访问的对象表达式
     * @param start 起始下标表达式（含），可为 null
     * @param end 结束下标表达式（不含），可为 null
     * @param token Token 对象（用于获取位置信息）
     */
    public SliceAccess(ASTNode object, ASTNode start, ASTNode end, Token token) {
        this(object, start, end, null, positionFromToken(token));
    }

    /**
     * 构造切片访问节点（从位置信息，无步长）
     *
     * @param object 被访问的对象表达式
     * @param start 起始下标表达式（含），可为 null
     * @param end 结束下标表达式（不含），可为 null
     * @param position 位置信息
     */
    public SliceAccess(ASTNode object, ASTNode start, ASTNode end, Position position) {
        this(object, start, end, null, position);
    }

    /**
     * 构造切片访问节点（从位置信息，含步长）
     *
     * @param object 被访问的对象表达式
     * @param start 起始下标表达式（含），可为 null
     * @param end 结束下标表达式（不含），可为 null
     * @param step 步长表达式，可为 null
     * @param position 位置信息
     */
    public SliceAccess(ASTNode object, ASTNode start, ASTNode end, ASTNode step, Position position) {
        super(position);
        this.object = object;
        this.start = start;
        this.end = end;
        this.step = step;
    }

    /**
     * 获取被访问的对象表达式
     *
     * @return 对象表达式
     */
    public ASTNode getObject() {
        return object;
    }

    /**
     * 获取起始下标表达式（含）
     *
     * @return 起始下标表达式，省略时为 null
     */
    public ASTNode getStart() {
        return start;
    }

    /**
     * 获取结束下标表达式（不含）
     *
     * @return 结束下标表达式，省略时为 null
     */
    public ASTNode getEnd() {
        return end;
    }

    /**
     * 获取步长表达式
     *
     * @return 步长表达式，省略时为 null
     */
    public ASTNode getStep() {
        return step;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitSliceAccess(this);
    }

    @Override
    public String toString() {
        return "SliceAccess{" +
                "object=" + object +
                ", start=" + start +
                ", end=" + end +
                ", step=" + step +
                '}';
    }
}
