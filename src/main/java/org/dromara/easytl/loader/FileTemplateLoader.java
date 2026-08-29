package org.dromara.easytl.loader;

import org.dromara.easytl.exception.TemplateCompileException;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 文件系统模板加载器
 * <p>
 * 从文件系统加载模板文件，支持相对路径和绝对路径
 * </p>
 * 
 * @author gongjun
 * @since 1.0.0
 */
public class FileTemplateLoader implements TemplateLoader {

    /**
     * 模板基础路径
     */
    private final String basePath;

    /**
     * 字符编码
     */
    private final Charset charset;

    /**
     * 构造函数，使用默认基础路径和 UTF-8 编码
     */
    public FileTemplateLoader() {
        this("", StandardCharsets.UTF_8);
    }

    /**
     * 构造函数，使用指定的基础路径和默认 UTF-8 编码
     * 
     * @param basePath 模板基础路径
     */
    public FileTemplateLoader(String basePath) {
        this(basePath, StandardCharsets.UTF_8);
    }

    /**
     * 构造函数，使用指定的基础路径和字符编码
     * 
     * @param basePath 模板基础路径
     * @param charset 字符编码
     */
    public FileTemplateLoader(String basePath, Charset charset) {
        this.basePath = basePath != null ? basePath : "";
        this.charset = charset != null ? charset : StandardCharsets.UTF_8;
    }

    @Override
    public String load(String path) throws TemplateCompileException {
        if (path == null || path.isEmpty()) {
            throw new TemplateCompileException("Template path cannot be null or empty");
        }

        File file = resolveFile(path);
        
        if (!file.exists()) {
            throw new TemplateCompileException("Template file does not exist: " + file.getAbsolutePath());
        }

        if (!file.isFile()) {
            throw new TemplateCompileException("Path is not a file: " + file.getAbsolutePath());
        }

        if (!file.canRead()) {
            throw new TemplateCompileException("Template file is not readable: " + file.getAbsolutePath());
        }

        try {
            Path filePath = file.toPath();
            // Java 8 兼容：使用 readAllBytes 代替 readString
            byte[] bytes = Files.readAllBytes(filePath);
            return new String(bytes, charset);
        } catch (IOException e) {
            throw new TemplateCompileException("Failed to read template file: " + file.getAbsolutePath() + " - " + e.getMessage(), e);
        }
    }

    @Override
    public boolean exists(String path) {
        if (path == null || path.isEmpty()) {
            return false;
        }

        try {
            File file = resolveFile(path);
            return file.exists() && file.isFile() && file.canRead();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 解析文件路径
     * <p>
     * 如果路径是绝对路径，直接使用；如果是相对路径，则基于 basePath 解析
     * </p>
     * 
     * @param path 模板文件路径
     * @return 解析后的文件对象
     */
    private File resolveFile(String path) {
        Path pathObj = Paths.get(path);
        
        // 如果是绝对路径，直接使用
        if (pathObj.isAbsolute()) {
            return pathObj.toFile();
        }

        // 如果是相对路径，基于 basePath 解析
        if (basePath != null && !basePath.isEmpty()) {
            Path basePathObj = Paths.get(basePath);
            Path resolvedPath = basePathObj.resolve(path);
            return resolvedPath.toFile();
        }

        // 没有 basePath，使用当前工作目录
        return pathObj.toFile();
    }

    /**
     * 获取模板基础路径
     * 
     * @return 模板基础路径
     */
    public String getBasePath() {
        return basePath;
    }

    /**
     * 获取字符编码
     * 
     * @return 字符编码
     */
    public Charset getCharset() {
        return charset;
    }
}
