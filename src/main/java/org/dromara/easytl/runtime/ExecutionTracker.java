package org.dromara.easytl.runtime;

import java.util.HashSet;
import java.util.Set;

/**
 * 模板执行追踪器
 * 用于检测循环导入
 *
 * @author EasyTL Team
 */
public class ExecutionTracker {

    private static final ThreadLocal<Set<String>> executingTemplates = ThreadLocal.withInitial(HashSet::new);

    /**
     * 检查模板是否正在执行
     *
     * @param templateKey 模板唯一标识（通常是路径）
     * @return 如果正在执行返回 true
     */
    public static boolean isExecuting(String templateKey) {
        return executingTemplates.get().contains(templateKey);
    }

    /**
     * 开始执行模板
     *
     * @param templateKey 模板唯一标识
     */
    public static void beginExecution(String templateKey) {
        executingTemplates.get().add(templateKey);
    }

    /**
     * 结束执行模板
     *
     * @param templateKey 模板唯一标识
     */
    public static void endExecution(String templateKey) {
        executingTemplates.get().remove(templateKey);
    }

    /**
     * 清空当前线程的执行追踪
     */
    public static void clear() {
        executingTemplates.get().clear();
    }

    private ExecutionTracker() {
        // 工具类，禁止实例化
    }
}