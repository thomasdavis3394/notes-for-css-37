package org.dromara.easytl.compiler;

import org.dromara.easytl.ast.*;
import org.dromara.easytl.exception.TemplateCompileException;
import org.dromara.easytl.parser.StringSource;

import java.util.List;
import java.util.Map;

/**
 * When Java class import in templates is disabled, rejects any {@link ImportStatement} in the AST.
 */
public final class TemplateJavaImportValidator {

    private TemplateJavaImportValidator() {
    }

    public static void validate(TemplateNode root, boolean templateJavaClassImportEnabled, String sourceText) {
        if (templateJavaClassImportEnabled || root == null) {
            return;
        }
        root.accept(new Checker(sourceText));
    }

    private static final class Checker implements ASTVisitor<Void> {

        private final String sourceText;

        Checker(String sourceText) {
            this.sourceText = sourceText;
        }

        @Override
        public Void visitImportStatement(ImportStatement node) {
            String msg = "Template Java class import is disabled; use Context.importClass(...) instead.";
            if (node.getKeywordPosition() != null && !node.getKeywordPosition().isUnknown()) {
                throw new TemplateCompileException(msg, node.getKeywordPosition(), new StringSource(sourceText));
            }
            throw new TemplateCompileException(msg);
        }

        @Override
        public Void visitImportTemplateStatement(ImportTemplateStatement node) {
            // 模板导入在 ImportTemplateValidator 中验证
            return null;
        }

        @Override
        public Void visitExportStatement(ExportStatement node) {
            // export 在 ExportValidator 中验证
            if (node.getLetStatement() != null) {
                node.getLetStatement().accept(this);
            }
            return null;
        }

        @Override
        public Void visitTemplateNode(TemplateNode node) {
            for (ASTNode child : node.getChildren()) {
                child.accept(this);
            }
            return null;
        }

        @Override
        public Void visitStringLiteral(StringLiteral node) {
            return null;
        }

        @Override
        public Void visitTemplateLiteral(TemplateLiteral node) {
            List<ASTNode> expressions = node.getExpressions();
            if (expressions != null) {
                for (ASTNode expr : expressions) {
                    expr.accept(this);
                }
            }
            return null;
        }

        @Override
        public Void visitNumberLiteral(NumberLiteral node) {
            return null;
        }

        @Override
        public Void visitBooleanLiteral(BooleanLiteral node) {
            return null;
        }

        @Override
        public Void visitNullLiteral(NullLiteral node) {
            return null;
        }

        @Override
        public Void visitListLiteral(ListLiteral node) {
            for (ASTNode element : node.getElements()) {
                element.accept(this);
            }
            return null;
        }

        @Override
        public Void visitHashLiteral(HashLiteral node) {
            for (Map.Entry<ASTNode, ASTNode> entry : node.getEntries()) {
                entry.getKey().accept(this);
                entry.getValue().accept(this);
            }
            return null;
        }

        @Override
        public Void visitRangeLiteral(RangeLiteral node) {
            node.getStart().accept(this);
            node.getEnd().accept(this);
            return null;
        }

        @Override
        public Void visitRegexLiteral(RegexLiteral node) {
            return null;
        }

        @Override
        public Void visitIdentifier(Identifier node) {
            return null;
        }

        @Override
        public Void visitMemberAccess(MemberAccess node) {
            node.getObject().accept(this);
            return null;
        }

        @Override
        public Void visitIndexAccess(IndexAccess node) {
            node.getObject().accept(this);
            node.getIndex().accept(this);
            return null;
        }

        @Override
        public Void visitCurrentNode(CurrentNode node) {
            return null;
        }

        @Override
        public Void visitWildcardAccess(WildcardAccess node) {
            node.getObject().accept(this);
            return null;
        }

        @Override
        public Void visitSliceAccess(SliceAccess node) {
            node.getObject().accept(this);
            if (node.getStart() != null) {
                node.getStart().accept(this);
            }
            if (node.getEnd() != null) {
                node.getEnd().accept(this);
            }
            return null;
        }

        @Override
        public Void visitFilterAccess(FilterAccess node) {
            node.getObject().accept(this);
            node.getPredicate().accept(this);
            return null;
        }

        @Override
        public Void visitRecursiveDescentAccess(RecursiveDescentAccess node) {
            node.getObject().accept(this);
            if (node.getSelectors() != null) {
                for (JsonPathSelector selector : node.getSelectors()) {
                    for (ASTNode child : selector.getChildExpressions()) {
                        child.accept(this);
                    }
                }
            }
            return null;
        }

        @Override
        public Void visitUnionAccess(UnionAccess node) {
            node.getObject().accept(this);
            for (JsonPathSelector selector : node.getSelectors()) {
                for (ASTNode child : selector.getChildExpressions()) {
                    child.accept(this);
                }
            }
            return null;
        }

        @Override
        public Void visitArgumentAccess(ArgumentAccess node) {
            return null;
        }

        @Override
        public Void visitArgumentVariable(ArgumentVariable node) {
            return null;
        }

        @Override
        public Void visitMethodCall(MethodCall node) {
            if (node.getObject() != null) {
                node.getObject().accept(this);
            }
            for (ASTNode arg : node.getArguments()) {
                arg.accept(this);
            }
            return null;
        }

        @Override
        public Void visitBinaryExpression(BinaryExpression node) {
            node.getLeft().accept(this);
            node.getRight().accept(this);
            return null;
        }

        @Override
        public Void visitUnaryExpression(UnaryExpression node) {
            node.getOperand().accept(this);
            return null;
        }

        @Override
        public Void visitTernaryExpression(TernaryExpression node) {
            node.getCondition().accept(this);
            node.getTrueValue().accept(this);
            node.getFalseValue().accept(this);
            return null;
        }

        @Override
        public Void visitElvisExpression(ElvisExpression node) {
            node.getLeft().accept(this);
            node.getRight().accept(this);
            return null;
        }

        @Override
        public Void visitIfExpression(IfExpression node) {
            node.getCondition().accept(this);
            if (node.getThenStatements() != null) {
                for (ASTNode stmt : node.getThenStatements()) {
                    stmt.accept(this);
                }
            }
            if (node.getElseIfClauses() != null) {
                for (IfExpression.ElseIfClause branch : node.getElseIfClauses()) {
                    branch.getCondition().accept(this);
                    for (ASTNode stmt : branch.getStatements()) {
                        stmt.accept(this);
                    }
                }
            }
            if (node.getElseStatements() != null) {
                for (ASTNode stmt : node.getElseStatements()) {
                    stmt.accept(this);
                }
            }
            return null;
        }

        @Override
        public Void visitForExpression(ForExpression node) {
            node.getIterable().accept(this);
            if (node.getBodyStatements() != null) {
                for (ASTNode stmt : node.getBodyStatements()) {
                    stmt.accept(this);
                }
            }
            return null;
        }

        @Override
        public Void visitCastExpression(CastExpression node) {
            node.getOperand().accept(this);
            return null;
        }

        @Override
        public Void visitTextNode(TextNode node) {
            return null;
        }

        @Override
        public Void visitSingleBraceExpressionNode(SingleBraceExpressionNode node) {
            node.getExpression().accept(this);
            return null;
        }

        @Override
        public Void visitDoubleBraceExpressionNode(DoubleBraceExpressionNode node) {
            node.getExpression().accept(this);
            return null;
        }

        @Override
        public Void visitDollarBraceExpressionNode(DollarBraceExpressionNode node) {
            node.getExpression().accept(this);
            return null;
        }

        @Override
        public Void visitIfStatement(IfStatement node) {
            node.getCondition().accept(this);
            if (node.getThenBody() != null) {
                for (ASTNode child : node.getThenBody()) {
                    child.accept(this);
                }
            }
            if (node.getElseIfClauses() != null) {
                for (IfStatement.ElseIfClause branch : node.getElseIfClauses()) {
                    branch.getCondition().accept(this);
                    if (branch.getBody() != null) {
                        for (ASTNode child : branch.getBody()) {
                            child.accept(this);
                        }
                    }
                }
            }
            if (node.getElseBody() != null) {
                for (ASTNode child : node.getElseBody()) {
                    child.accept(this);
                }
            }
            return null;
        }

        @Override
        public Void visitForStatement(ForStatement node) {
            node.getIterable().accept(this);
            if (node.getBody() != null) {
                for (ASTNode child : node.getBody()) {
                    child.accept(this);
                }
            }
            return null;
        }

        @Override
        public Void visitSwitchStatement(SwitchStatement node) {
            node.getCondition().accept(this);
            for (CaseClause caseClause : node.getCases()) {
                caseClause.accept(this);
            }
            if (node.getDefaultBody() != null) {
                for (ASTNode child : node.getDefaultBody()) {
                    child.accept(this);
                }
            }
            return null;
        }

        @Override
        public Void visitCaseClause(CaseClause node) {
            node.getValue().accept(this);
            if (node.getBody() != null) {
                for (ASTNode child : node.getBody()) {
                    child.accept(this);
                }
            }
            return null;
        }

        @Override
        public Void visitLetStatement(LetStatement node) {
            node.getValue().accept(this);
            return null;
        }

        @Override
        public Void visitExtendsStatement(ExtendsStatement node) {
            return null;
        }

        @Override
        public Void visitObjectConstruction(ObjectConstruction node) {
            node.getTargetClass().accept(this);
            HashLiteral properties = node.getPropertiesBlock();
            if (properties != null && !properties.isEmpty()) {
                for (Map.Entry<ASTNode, ASTNode> entry : properties.getEntries()) {
                    entry.getKey().accept(this);
                    entry.getValue().accept(this);
                }
            }
            return null;
        }

        @Override
        public Void visitLambdaExpression(LambdaExpression node) {
            if (node.isBlockBody() && node.getBody() != null) {
                for (ASTNode stmt : node.getBody()) {
                    stmt.accept(this);
                }
            } else if (node.getExpressionBody() != null) {
                node.getExpressionBody().accept(this);
            }
            return null;
        }

        @Override
        public Void visitReturnStatement(ReturnStatement node) {
            if (node.hasValue()) {
                node.getValue().accept(this);
            }
            return null;
        }

        @Override
        public Void visitUrlLiteral(UrlLiteral node) {
            for (ASTNode child : node.getChildren()) {
                child.accept(this);
            }
            return null;
        }

        @Override
        public Void visitIncrementExpression(IncrementExpression node) {
            return null;
        }

        @Override
        public Void visitCompoundAssignmentExpression(CompoundAssignmentExpression node) {
            return null;
        }

        @Override
        public Void visitClassicForStatement(ClassicForStatement node) {
            if (node.getInitialization() != null) {
                node.getInitialization().accept(this);
            }
            if (node.getCondition() != null) {
                node.getCondition().accept(this);
            }
            if (node.getUpdate() != null) {
                node.getUpdate().accept(this);
            }
            for (ASTNode child : node.getBody()) {
                child.accept(this);
            }
            return null;
        }
    }
}
