package org.dromara.easytl.ast;

import org.dromara.easytl.parser.Token;

import java.util.List;

/**
 * URL 字面量 AST 节点
 * 表示 url(...) 构造，支持嵌入表达式和语句块
 *
 * @author EasyTL Team
 */
public class UrlLiteral extends ASTNode {

    /** 子节点列表：TextNode、ExpressionNode、StatementBlock */
    private final List<ASTNode> children;

    public UrlLiteral(List<ASTNode> children) {
        this.children = children;
    }

    public UrlLiteral(List<ASTNode> children, Token token) {
        super(positionFromToken(token));
        this.children = children;
    }

    /**
     * 构造 UrlLiteral，直接指定 Position
     *
     * @param children 子节点列表
     * @param position 位置信息
     */
    public UrlLiteral(List<ASTNode> children, Position position) {
        super(position);
        this.children = children;
    }

    public List<ASTNode> getChildren() {
        return children;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitUrlLiteral(this);
    }

    @Override
    public String toString() {
        return "UrlLiteral{" +
                "children=" + children +
                '}';
    }
}