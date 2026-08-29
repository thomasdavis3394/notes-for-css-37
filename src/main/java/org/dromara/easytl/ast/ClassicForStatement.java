package org.dromara.easytl.ast;

import java.util.List;

/**
 * C 风格 for 循环语句节点
 * 格式：{% for let i = 0; condition; update }...{/% for }
 *
 * @author EasyTL Team
 */
public class ClassicForStatement extends StatementBlock {

    private final LetStatement initialization;
    private final ASTNode condition;
    private final ASTNode update;
    private final List<ASTNode> body;

    public ClassicForStatement(LetStatement initialization, ASTNode condition, ASTNode update, List<ASTNode> body) {
        this.initialization = initialization;
        this.condition = condition;
        this.update = update;
        this.body = body;
    }

    public ClassicForStatement(LetStatement initialization, ASTNode condition, ASTNode update, List<ASTNode> body, Position position) {
        super(position);
        this.initialization = initialization;
        this.condition = condition;
        this.update = update;
        this.body = body;
    }

    public LetStatement getInitialization() { return initialization; }
    public ASTNode getCondition() { return condition; }
    public ASTNode getUpdate() { return update; }
    public List<ASTNode> getBody() { return body; }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitClassicForStatement(this);
    }

    @Override
    public String toString() {
        return "ClassicForStatement{init=" + initialization + ", condition=" + condition + ", update=" + update + ", body=" + body + '}';
    }
}