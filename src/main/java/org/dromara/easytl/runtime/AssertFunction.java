package org.dromara.easytl.runtime;

import org.dromara.easytl.ast.ASTNode;
import org.dromara.easytl.ast.BinaryExpression;
import org.dromara.easytl.ast.MethodCall;
import org.dromara.easytl.ast.Position;
import org.dromara.easytl.compiler.Evaluator;
import org.dromara.easytl.exception.AssertionException;
import org.dromara.easytl.exception.TemplateRuntimeException;
import org.dromara.easytl.parser.StringSource;

import java.util.List;

/**
 * 内置契约断言函数 {@code assert(...)}
 *
 * <p>支持三种调用形式：</p>
 * <ul>
 * <li>{@code assert(cond)} — cond 为假时抛出 {@link AssertionException}；为真时不做任何事（返回 null）</li>
 * <li>{@code assert(cond, message)} — cond 为假时抛出异常，自定义消息按 JUnit 5 约定作为前缀：
 * {@code message ==> Assertion failed: ...}</li>
 * <li>{@code assert(cond, lambda)} — cond 为假时执行 lambda（入参为 {@link AssertionException} 对象），
 * 执行后不再抛出，渲染继续</li>
 * </ul>
 *
 * <p>本函数实现 {@link AstAwareTemplateFunction}，可识别条件表达式的源码结构：
 * 比较运算符（{@code == != > >= < <=}）、包含/集合运算符（{@code in !in subsetof anyof noneof}）、
 * 正则运算符（{@code =~ !=~}）会给出包含左右操作数源码与实际值的友好消息，例如
 * {@code Assertion failed: expected a to be greater than b\nExpected : a = <1>\nActual   : b = <2>}；
 * 函数/方法调用条件直接使用调用源码提示。</p>
 *
 * <p>注意：断言失败时，为生成消息，二元条件的左右操作数会被二次求值（第一次是整个条件求值时）。
 * 对纯读取表达式无影响；含副作用的调用会执行两次。</p>
 *
 * @author EasyTL Team
 */
public final class AssertFunction implements AstAwareTemplateFunction {

    private static final String NAME = "assert";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Object callWithAst(Evaluator evaluator, MethodCall node) {
        List<ASTNode> argNodes = node.getArguments();
        checkArgCount(argNodes.size());

        ASTNode condNode = argNodes.get(0);
        Object condValue = condNode.accept(evaluator);
        if (OperatorEvaluator.toBoolean(condValue)) {
            return null;
        }

        // 求值第二参数（若存在）并校验类型
        Object second = null;
        if (argNodes.size() == 2) {
            second = argNodes.get(1).accept(evaluator);
            if (!(second instanceof String) && !(second instanceof LambdaObject)) {
                throw new TemplateRuntimeException(
                    "Assertion requires a string message or a lambda as second argument, but got " + typeName(second));
            }
        }

        String detail = buildFailureMessage(condNode, condValue, evaluator);

        if (second instanceof LambdaObject) {
            // handler 语义：执行 lambda，不再抛出
            AssertionException exception = createException(detail, node, evaluator);
            ((LambdaObject) second).invoke(new Object[]{exception});
            return null;
        }

        String message = second != null ? (second + " ==> " + detail) : detail;
        throw createException(message, node, evaluator);
    }

    /**
     * 兜底调用（参数已求值，无 AST 上下文，如 JSONPath 过滤谓词路径）
     */
    @Override
    public Object call(List<Object> args) {
        checkArgCount(args.size());
        if (OperatorEvaluator.toBoolean(args.get(0))) {
            return null;
        }
        Object second = args.size() == 2 ? args.get(1) : null;
        if (second instanceof LambdaObject) {
            ((LambdaObject) second).invoke(new Object[]{new AssertionException("Assertion failed")});
            return null;
        }
        if (second != null && !(second instanceof String)) {
            throw new TemplateRuntimeException(
                "Assertion requires a string message or a lambda as second argument, but got " + typeName(second));
        }
        throw new AssertionException(second != null ? (String) second : "Assertion failed");
    }

    private static void checkArgCount(int actual) {
        if (actual < 1 || actual > 2) {
            throw new TemplateRuntimeException(
                "Assertion requires 1 or 2 argument(s), but got " + actual);
        }
    }

    private static String typeName(Object value) {
        return value == null ? "null" : value.getClass().getName();
    }

    private static AssertionException createException(String message, MethodCall node, Evaluator evaluator) {
        Position position = node.getPosition();
        String sourceText = evaluator.getSourceText();
        if (position != null && !position.isUnknown() && sourceText != null) {
            return new AssertionException(message, position, new StringSource(sourceText));
        }
        return new AssertionException(message);
    }

    // ==================== 失败消息构建 ====================

    /**
     * 按条件表达式的源码结构构建友好的断言失败消息
     */
    private static String buildFailureMessage(ASTNode condNode, Object condValue, Evaluator evaluator) {
        String sourceText = evaluator.getSourceText();

        if (condNode instanceof BinaryExpression) {
            BinaryExpression binary = (BinaryExpression) condNode;
            String operatorMessage = buildBinaryMessage(binary, sourceText, evaluator);
            if (operatorMessage != null) {
                return operatorMessage;
            }
        }

        String condSrc = extractSource(sourceText, condNode.getPosition());
        if (condNode instanceof MethodCall) {
            if (condSrc != null) {
                return assertionMessage(condSrc + " to return true", formatValue(condValue));
            }
            return assertionMessage("<true>", formatValue(condValue));
        }
        if (condSrc != null) {
            return assertionMessage(condSrc + " to be truthy", formatValue(condValue));
        }
        return assertionMessage("<true>", formatValue(condValue));
    }

    /**
     * 组装多行断言消息：{@code Assertion failed:\nExpected : xxx\nActual   : yyy}
     */
    private static String assertionMessage(String expected, String actual) {
        return "Assertion failed:\nExpected : " + expected + "\nActual   : " + actual;
    }

    /**
     * 为二元条件构建消息；运算符无需操作数详情（如 &amp;&amp;、||）时返回 null
     *
     * <p>三类形式：</p>
     * <ul>
     * <li>{@code ==}：{@code Expected} 为左值，{@code Actual} 为右值</li>
     * <li>属于/匹配类（{@code in !in subsetof anyof noneof =~ !=~}）：
     * {@code Expected} 为左值应满足的约束描述（如 {@code an Integer in <[1, 2, 3]>}），
     * {@code Actual} 为左值（被检验项）</li>
     * <li>其余（{@code != > >= < <=}）：{@code Expected} 为左值应满足的约束描述
     * （Hamcrest 风格，如 {@code an Integer that was not <2>}），{@code Actual} 为右值</li>
     * </ul>
     */
    private static String buildBinaryMessage(BinaryExpression binary, String sourceText, Evaluator evaluator) {
        BinaryExpression.Operator op = binary.getOperator();
        String expectation;
        switch (op) {
            case EQ:             expectation = "to be equal to"; break;
            case NEQ:            expectation = "not to be equal to"; break;
            case GT:             expectation = "to be greater than"; break;
            case GTE:            expectation = "to be at least"; break;
            case LT:             expectation = "to be less than"; break;
            case LTE:            expectation = "to be at most"; break;
            case IN:             expectation = "to be in"; break;
            case NOT_IN:         expectation = "not to be in"; break;
            case SUBSETOF:       expectation = "to be a subset of"; break;
            case ANYOF:          expectation = "to have at least one element in common with"; break;
            case NONEOF:         expectation = "to have no elements in common with"; break;
            case REGEX_MATCH:    expectation = "to match"; break;
            case REGEX_NOT_MATCH: expectation = "not to match"; break;
            default:
                return null;
        }

        // 为生成消息二次求值左右操作数（纯读取表达式无副作用）
        Object leftValue = binary.getLeft().accept(evaluator);
        Object rightValue = binary.getRight().accept(evaluator);
        String leftSrc = operandLabel(sourceText, binary.getLeft(), "left");
        String rightSrc = operandLabel(sourceText, binary.getRight(), "right");

        String headline = "Assertion failed: expected " + leftSrc + ' ' + expectation + ' ' + rightSrc;
        if (op == BinaryExpression.Operator.EQ) {
            return headline
                 + "\nExpected : " + leftSrc + " = " + formatValue(leftValue)
                 + "\nActual   : " + rightSrc + " = " + formatValue(rightValue);
        }
        if (isMembershipOperator(op)) {
            // 属于/匹配类（左边是否属于右边、是否匹配右边）：Expected 为左值应满足的约束
            // （如 an Integer in <[1, 2, 3]>），Actual 为左边被检验的值
            return headline
                 + "\nExpected : " + constraintDescription(op, leftValue, rightValue)
                 + "\nActual   : " + leftSrc + " = " + formatValue(leftValue);
        }
        return headline
             + "\nExpected : " + constraintDescription(op, leftValue, rightValue)
             + "\nActual   : " + rightSrc + " = " + formatValue(rightValue);
    }

    /**
     * 是否属于/匹配类运算符：左值是否属于右值、是否是右值的一部分、是否匹配右值
     * （{@code in !in subsetof anyof noneof =~ !=~}）
     */
    private static boolean isMembershipOperator(BinaryExpression.Operator op) {
        switch (op) {
            case IN:
            case NOT_IN:
            case SUBSETOF:
            case ANYOF:
            case NONEOF:
            case REGEX_MATCH:
            case REGEX_NOT_MATCH:
                return true;
            default:
                return false;
        }
    }

    /**
     * 非相等运算符的约束描述（Hamcrest 风格）：描述左值应满足的条件。
     * 关系/不等类：{@code an Integer that was not <2>}、{@code an Integer that was greater than <2>}；
     * 属于/匹配类：{@code an Integer in <[1, 2, 3]>}、{@code a String that matches <\d+>}
     */
    private static String constraintDescription(BinaryExpression.Operator op, Object leftValue, Object rightValue) {
        String constraint;
        switch (op) {
            case NEQ:            constraint = "that was not"; break;
            case GT:             constraint = "that was greater than"; break;
            case GTE:            constraint = "that was at least"; break;
            case LT:             constraint = "that was less than"; break;
            case LTE:            constraint = "that was at most"; break;
            case IN:             constraint = "in"; break;
            case NOT_IN:         constraint = "not in"; break;
            case SUBSETOF:       constraint = "that is a subset of"; break;
            case ANYOF:          constraint = "that has at least one element in common with"; break;
            case NONEOF:         constraint = "that has no elements in common with"; break;
            case REGEX_MATCH:    constraint = "that matches"; break;
            case REGEX_NOT_MATCH: constraint = "that does not match"; break;
            default:             constraint = "that was"; break;
        }
        return typePhrase(leftValue != null ? leftValue : rightValue)
             + ' ' + constraint + ' ' + formatValue(rightValue);
    }

    /**
     * 值类型的冠词短语：{@code an Integer}、{@code a String}；null 或匿名类退化为 {@code a value}
     */
    private static String typePhrase(Object value) {
        if (value == null) {
            return "a value";
        }
        String simpleName = value.getClass().getSimpleName();
        if (simpleName.isEmpty()) {
            return "a value";
        }
        char first = Character.toUpperCase(simpleName.charAt(0));
        boolean vowel = first == 'A' || first == 'E' || first == 'I' || first == 'O' || first == 'U';
        return (vowel ? "an " : "a ") + simpleName;
    }

    /**
     * 操作数标签：截取到的源码文本，截取失败时使用兜底名
     */
    private static String operandLabel(String sourceText, ASTNode node, String fallback) {
        String src = extractSource(sourceText, node != null ? node.getPosition() : null);
        return src != null ? src : fallback;
    }

    /**
     * 将值渲染为 JUnit 风格的 {@code <value>} 形式（字符串不加引号，null → {@code <null>}）
     */
    private static String formatValue(Object value) {
        return "<" + String.valueOf(value) + ">";
    }

    /**
     * 按 Position（行/列，1 起始，endColumn 为开区间）从源码中截取文本；
     * 源码缺失、位置未知或越界时返回 null
     */
    private static String extractSource(String sourceText, Position position) {
        if (sourceText == null || position == null || position.isUnknown()) {
            return null;
        }
        try {
            String[] lines = sourceText.split("\n", -1);
            int startLine = position.getStartLine();
            int endLine = position.getEndLine();
            if (startLine < 1 || endLine < startLine || endLine > lines.length) {
                return null;
            }
            StringBuilder sb = new StringBuilder();
            for (int i = startLine; i <= endLine; i++) {
                String line = stripCarriageReturn(lines[i - 1]);
                int from = (i == startLine) ? position.getStartColumn() - 1 : 0;
                int to = (i == endLine) ? position.getEndColumn() - 1 : line.length();
                from = Math.max(0, Math.min(from, line.length()));
                to = Math.max(from, Math.min(to, line.length()));
                if (sb.length() > 0) {
                    sb.append(' ');
                }
                sb.append(line, from, to);
            }
            String result = sb.toString().trim();
            return result.isEmpty() ? null : result;
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static String stripCarriageReturn(String line) {
        return line.endsWith("\r") ? line.substring(0, line.length() - 1) : line;
    }
}
