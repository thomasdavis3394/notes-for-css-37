package org.dromara.easytl.ast;

import java.util.Collections;
import java.util.List;

/**
 * JSONPath 括号选择器
 *
 * <p>Union 多选（{@code [0, 'name', 1:3]}）与递归下降括号形式（{@code ..[...]}）的组成单元。
 * 选择器本身不是独立的 AST 节点（不接受访问者），仅作为 {@link UnionAccess} 与
 * {@link RecursiveDescentAccess} 的组成部分，由求值器在目标对象上逐个应用。</p>
 *
 * @author EasyTL Team
 */
public abstract class JsonPathSelector {

    private JsonPathSelector() {
    }

    /**
     * 返回选择器内的子表达式（用于 AST 校验遍历，如导入校验）
     *
     * @return 子表达式列表（可能为空，不为 null）
     */
    public abstract List<ASTNode> getChildExpressions();

    /**
     * 下标/键选择器，如 {@code [0]}、{@code ['name']}、{@code [i]}
     */
    public static final class IndexSelector extends JsonPathSelector {

        /** 下标/键表达式 */
        private final ASTNode expression;

        public IndexSelector(ASTNode expression) {
            this.expression = expression;
        }

        public ASTNode getExpression() {
            return expression;
        }

        @Override
        public List<ASTNode> getChildExpressions() {
            return Collections.singletonList(expression);
        }

        @Override
        public String toString() {
            return "IndexSelector{" + expression + '}';
        }
    }

    /**
     * 切片选择器，如 {@code [1:4]}、{@code [::-1]}（start/end/step 均可省略，null 表示省略）
     */
    public static final class SliceSelector extends JsonPathSelector {

        /** 起始下标表达式（含），为 null 表示省略 */
        private final ASTNode start;

        /** 结束下标表达式（不含），为 null 表示省略 */
        private final ASTNode end;

        /** 步长表达式，为 null 表示省略（默认 1） */
        private final ASTNode step;

        public SliceSelector(ASTNode start, ASTNode end, ASTNode step) {
            this.start = start;
            this.end = end;
            this.step = step;
        }

        public ASTNode getStart() {
            return start;
        }

        public ASTNode getEnd() {
            return end;
        }

        public ASTNode getStep() {
            return step;
        }

        @Override
        public List<ASTNode> getChildExpressions() {
            List<ASTNode> children = new java.util.ArrayList<>(3);
            if (start != null) {
                children.add(start);
            }
            if (end != null) {
                children.add(end);
            }
            if (step != null) {
                children.add(step);
            }
            return children;
        }

        @Override
        public String toString() {
            return "SliceSelector{start=" + start + ", end=" + end + ", step=" + step + '}';
        }
    }

    /**
     * 过滤选择器，如 {@code [?(@.price < 10)]}
     */
    public static final class FilterSelector extends JsonPathSelector {

        /** 谓词表达式（内可用 {@code @} 引用当前元素） */
        private final ASTNode predicate;

        public FilterSelector(ASTNode predicate) {
            this.predicate = predicate;
        }

        public ASTNode getPredicate() {
            return predicate;
        }

        @Override
        public List<ASTNode> getChildExpressions() {
            return Collections.singletonList(predicate);
        }

        @Override
        public String toString() {
            return "FilterSelector{" + predicate + '}';
        }
    }

    /**
     * 通配选择器 {@code [*]}
     */
    public static final class WildcardSelector extends JsonPathSelector {

        public WildcardSelector() {
        }

        @Override
        public List<ASTNode> getChildExpressions() {
            return Collections.emptyList();
        }

        @Override
        public String toString() {
            return "WildcardSelector{}";
        }
    }
}
