package org.dromara.easytl.runtime;

import org.dromara.easytl.ast.MethodCall;
import org.dromara.easytl.compiler.Evaluator;

import java.util.List;

/**
 * AST 感知的模板函数扩展接口（SPI）
 *
 * <p>普通 {@link TemplateFunction} 在调用点只能拿到<strong>已求值</strong>的参数列表，
 * 无法感知参数的源码结构。实现本接口的函数会由 Evaluator 在函数调用点直接分派，
 * 拿到未求值的参数 AST（{@link MethodCall} 调用节点），可自行决定求值时机与方式，
 * 从而支持依赖源码结构的能力（如 {@code assert(a == b)} 识别左右操作数给出友好提示）。</p>
 *
 * <p>求值方式：对参数节点调用 {@code argNode.accept(evaluator)} 即按正常语义求值。
 * 实现仍需遵守 {@link TemplateFunction} 的无状态、线程安全要求。</p>
 *
 * @author EasyTL Team
 */
public interface AstAwareTemplateFunction extends TemplateFunction {

    /**
     * AST 感知调用
     *
     * @param evaluator 当前求值器（用于对参数节点求值，以及获取模板源码）
     * @param node      本次函数调用的 {@link MethodCall} 节点（{@code node.getArguments()} 为未求值的参数 AST）
     * @return 函数结果（可为 null）
     */
    Object callWithAst(Evaluator evaluator, MethodCall node);

    /**
     * 兜底调用（参数已求值，无 AST 上下文，如 JSONPath 过滤谓词路径）
     *
     * @param args 已求值的参数列表
     * @return 函数结果（可为 null）
     */
    @Override
    Object call(List<Object> args);
}
