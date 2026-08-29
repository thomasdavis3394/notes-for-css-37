package org.dromara.easytl.loader;

import org.dromara.easytl.exception.TemplateCompileException;

/**
 * 模板加载器接口
 * <p>
 * 用于从不同来源加载模板文件内容
 * </p>
 * 
 * @author gongjun
 * @since 1.0.0
 */
public interface TemplateLoader {

    /**
     * 加载模板文件内容
     * 
     * @param path 模板文件路径
     * @return 模板文件内容
     * @throws TemplateCompileException 如果模板文件不存在或加载失败
     */
    String load(String path) throws TemplateCompileException;

    /**
     * 检查模板文件是否存在
     * 
     * @param path 模板文件路径
     * @return 如果文件存在返回 true，否则返回 false
     */
    boolean exists(String path);
}
