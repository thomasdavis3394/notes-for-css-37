package org.dromara.easytl.runtime;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Export 变量注册表
 * 用于跟踪模板中已导出的变量名
 *
 * @author EasyTL Team
 */
public class ExportRegistry {

    private final Set<String> exportedNames = new HashSet<>();

    /**
     * 注册导出变量
     *
     * @param name 变量名
     * @throws IllegalStateException 如果变量已被导出
     */
    public void register(String name) {
        if (exportedNames.contains(name)) {
            throw new IllegalStateException("Variable '" + name + "' is already exported");
        }
        exportedNames.add(name);
    }

    /**
     * 获取所有已导出的变量名
     *
     * @return 不可变的变量名集合
     */
    public Set<String> getExportedNames() {
        return Collections.unmodifiableSet(exportedNames);
    }

    /**
     * 检查变量是否已导出
     *
     * @param name 变量名
     * @return 如果已导出返回 true
     */
    public boolean isExported(String name) {
        return exportedNames.contains(name);
    }

    /**
     * 清空注册表
     */
    public void clear() {
        exportedNames.clear();
    }
}