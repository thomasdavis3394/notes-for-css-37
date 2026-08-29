package org.dromara.easytl.loader;

import org.dromara.easytl.exception.TemplateCompileException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Classpath 模板加载器
 * <p>
 * 从 classpath 加载模板文件
 * </p>
 * 
 * @author gongjun
 * @since 1.0.0
 */
public class ClasspathTemplateLoader implements TemplateLoader {

    /**
     * 字符编码
     */
    private final Charset charset;

    /**
     * 类加载器
     */
    private final ClassLoader classLoader;

    /**
     * 构造函数，使用默认 UTF-8 编码和当前线程的类加载器
     */
    public ClasspathTemplateLoader() {
        this(StandardCharsets.UTF_8, Thread.currentThread().getContextClassLoader());
    }

    /**
     * 构造函数，使用指定的字符编码和当前线程的类加载器
     * 
     * @param charset 字符编码
     */
    public ClasspathTemplateLoader(Charset charset) {
        this(charset, Thread.currentThread().getContextClassLoader());
    }

    /**
     * 构造函数，使用指定的字符编码和类加载器
     * 
     * @param charset 字符编码
     * @param classLoader 类加载器
     */
    public ClasspathTemplateLoader(Charset charset, ClassLoader classLoader) {
        this.charset = charset != null ? charset : StandardCharsets.UTF_8;
        this.classLoader = classLoader != null ? classLoader : Thread.currentThread().getContextClassLoader();
    }

    @Override
    public String load(String path) throws TemplateCompileException {
        if (path == null || path.isEmpty()) {
            throw new TemplateCompileException("Template path cannot be null or empty");
        }

        // ClassLoader.getResourceAsStream() 对于以 / 开头的路径会去掉开头的 /
        // 所以我们需要确保路径不以 / 开头，或者直接使用去掉 / 的路径
        String resourcePath = path.startsWith("/") ? path.substring(1) : path;

        try (InputStream inputStream = classLoader.getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new TemplateCompileException("Template file not found in classpath: " + path);
            }

            // Java 8 兼容：使用 BufferedReader 读取
            java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(inputStream, charset));
            StringBuilder sb = new StringBuilder();
            char[] buffer = new char[8192];
            int bytesRead;
            while ((bytesRead = reader.read(buffer)) != -1) {
                sb.append(buffer, 0, bytesRead);
            }
            return sb.toString();
        } catch (IOException e) {
            throw new TemplateCompileException("Failed to read template file from classpath: " + path + " - " + e.getMessage(), e);
        }
    }

    @Override
    public boolean exists(String path) {
        if (path == null || path.isEmpty()) {
            return false;
        }

        try {
            // ClassLoader.getResourceAsStream() 对于以 / 开头的路径会去掉开头的 /
            // 所以我们需要确保路径不以 / 开头，或者直接使用去掉 / 的路径
            String resourcePath = path.startsWith("/") ? path.substring(1) : path;
            InputStream inputStream = classLoader.getResourceAsStream(resourcePath);
            if (inputStream != null) {
                inputStream.close();
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
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
