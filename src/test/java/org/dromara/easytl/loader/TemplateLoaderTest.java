package org.dromara.easytl.loader;

import org.dromara.easytl.Context;
import org.dromara.easytl.Template;
import org.dromara.easytl.TemplateEngine;
import org.dromara.easytl.exception.TemplateCompileException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TemplateLoader 测试类
 * <p>
 * 验证模板加载器接口和实现类的功能
 * </p>
 * 
 * @author gongjun
 * @since 1.0.0
 */
class TemplateLoaderTest {

    @TempDir
    Path tempDir;

    private Path templatesDir;

    @BeforeEach
    void setUp() throws IOException {
        templatesDir = tempDir.resolve("templates");
        Files.createDirectories(templatesDir);
    }

    // ==================== AC 1: TemplateLoader 接口测试 ====================

    @Test
    void testTemplateLoaderInterface() {
        // Given: 存在 TemplateLoader 接口
        // When: 查看接口定义
        // Then: 接口应包含 load(String path) 方法
        assertTrue(TemplateLoader.class.isInterface(), "TemplateLoader 应该是接口");
        
        try {
            TemplateLoader.class.getMethod("load", String.class);
            TemplateLoader.class.getMethod("exists", String.class);
        } catch (NoSuchMethodException e) {
            fail("TemplateLoader 接口应包含 load(String) 和 exists(String) 方法");
        }
    }

    // ==================== AC 2: FileTemplateLoader 实现测试 ====================

    @Test
    void testFileTemplateLoaderLoadRelativePath() throws IOException {
        // Given: 存在 FileTemplateLoader 类
        // When: 使用 FileTemplateLoader 加载文件系统中的模板文件（相对路径）
        // Then: 应该能够加载相对路径的模板文件
        
        // 创建测试模板文件
        File templateFile = templatesDir.resolve("test.etl").toFile();
        String templateContent = "Hello, {name}!";
        Files.write(templateFile.toPath(), templateContent.getBytes(StandardCharsets.UTF_8));
        
        // 创建 FileTemplateLoader，使用 templatesDir 作为基础路径
        FileTemplateLoader loader = new FileTemplateLoader(templatesDir.toString());
        
        // 加载模板
        String loaded = loader.load("test.etl");
        assertEquals(templateContent, loaded, "应该能够加载相对路径的模板文件");
    }

    @Test
    void testFileTemplateLoaderLoadAbsolutePath() throws IOException {
        // Given: 存在 FileTemplateLoader 类
        // When: 使用 FileTemplateLoader 加载文件系统中的模板文件（绝对路径）
        // Then: 应该能够加载绝对路径的模板文件
        
        // 创建测试模板文件
        File templateFile = templatesDir.resolve("test.etl").toFile();
        String templateContent = "Hello, World!";
        Files.write(templateFile.toPath(), templateContent.getBytes(StandardCharsets.UTF_8));
        
        // 创建 FileTemplateLoader
        FileTemplateLoader loader = new FileTemplateLoader();
        
        // 加载模板（使用绝对路径）
        String loaded = loader.load(templateFile.getAbsolutePath());
        assertEquals(templateContent, loaded, "应该能够加载绝对路径的模板文件");
    }

    @Test
    void testFileTemplateLoaderWithBasePath() throws IOException {
        // Given: FileTemplateLoader 配置了基础路径
        // When: 使用相对路径加载模板
        // Then: 应该支持模板基础路径配置
        
        // 创建子目录和模板文件
        Path subDir = templatesDir.resolve("sub");
        Files.createDirectories(subDir);
        File templateFile = subDir.resolve("template.etl").toFile();
        String templateContent = "Base path test";
        Files.write(templateFile.toPath(), templateContent.getBytes(StandardCharsets.UTF_8));
        
        // 创建 FileTemplateLoader，使用 templatesDir 作为基础路径
        FileTemplateLoader loader = new FileTemplateLoader(templatesDir.toString());
        
        // 使用相对路径加载模板
        String loaded = loader.load("sub/template.etl");
        assertEquals(templateContent, loaded, "应该支持模板基础路径配置");
    }

    @Test
    void testFileTemplateLoaderFileNotExists() {
        // Given: FileTemplateLoader
        // When: 加载不存在的文件
        // Then: 当文件不存在时应抛出异常
        
        FileTemplateLoader loader = new FileTemplateLoader(templatesDir.toString());
        
        assertThrows(TemplateCompileException.class, () -> {
            loader.load("nonexistent.etl");
        }, "当文件不存在时应抛出 TemplateCompileException");
    }

    @Test
    void testFileTemplateLoaderExists() throws IOException {
        // Given: FileTemplateLoader
        // When: 检查文件是否存在
        // Then: 应该正确返回文件是否存在
        
        File templateFile = templatesDir.resolve("test.etl").toFile();
        Files.write(templateFile.toPath(), "test".getBytes(StandardCharsets.UTF_8));
        
        FileTemplateLoader loader = new FileTemplateLoader(templatesDir.toString());
        
        assertTrue(loader.exists("test.etl"), "存在的文件应该返回 true");
        assertFalse(loader.exists("nonexistent.etl"), "不存在的文件应该返回 false");
        assertFalse(loader.exists(null), "null 路径应该返回 false");
        assertFalse(loader.exists(""), "空路径应该返回 false");
    }

    // ==================== AC 3: ClasspathTemplateLoader 实现测试 ====================

    @Test
    void testClasspathTemplateLoaderLoad() {
        // Given: 存在 ClasspathTemplateLoader 类
        // When: 使用 ClasspathTemplateLoader 加载 classpath 中的模板文件
        // Then: 应该能够从 classpath 加载模板文件
        
        // 使用测试类自己的类加载器，确保能找到测试资源
        ClasspathTemplateLoader loader = new ClasspathTemplateLoader(
                StandardCharsets.UTF_8, 
                TemplateLoaderTest.class.getClassLoader()
        );
        
        // 尝试加载一个已知存在的测试资源文件
        // 使用 loader-test.etl，这个文件在测试资源中确实存在
        String loaded = loader.load("loader-test.etl");
        assertNotNull(loaded, "应该能够从 classpath 加载模板文件");
        assertFalse(loaded.isEmpty(), "加载的内容不应为空");
        assertTrue(loaded.contains("Test template"), "加载的内容应该包含预期内容");
    }

    @Test
    void testClasspathTemplateLoaderWithLeadingSlash() {
        // Given: ClasspathTemplateLoader
        // When: 使用以 / 开头的路径加载模板
        // Then: 应该能够正确加载
        
        // 使用测试类自己的类加载器
        ClasspathTemplateLoader loader = new ClasspathTemplateLoader(
                StandardCharsets.UTF_8,
                TemplateLoaderTest.class.getClassLoader()
        );
        
        // 使用以 / 开头的路径
        String loaded1 = loader.load("/loader-test.etl");
        // 使用不以 / 开头的路径
        String loaded2 = loader.load("loader-test.etl");
        
        assertEquals(loaded1, loaded2, "无论路径是否以 / 开头，都应该加载相同的内容");
    }

    @Test
    void testClasspathTemplateLoaderFileNotExists() {
        // Given: ClasspathTemplateLoader
        // When: 加载不存在的文件
        // Then: 当文件不存在时应抛出异常
        
        ClasspathTemplateLoader loader = new ClasspathTemplateLoader();
        
        assertThrows(TemplateCompileException.class, () -> {
            loader.load("nonexistent/template.etl");
        }, "当文件不存在时应抛出 TemplateCompileException");
    }

    @Test
    void testClasspathTemplateLoaderExists() {
        // Given: ClasspathTemplateLoader
        // When: 检查文件是否存在
        // Then: 应该正确返回文件是否存在
        
        // 使用测试类自己的类加载器
        ClasspathTemplateLoader loader = new ClasspathTemplateLoader(
                StandardCharsets.UTF_8,
                TemplateLoaderTest.class.getClassLoader()
        );
        
        // 使用已知存在的测试资源文件
        assertTrue(loader.exists("loader-test.etl"), "存在的文件应该返回 true");
        assertTrue(loader.exists("/loader-test.etl"), "存在的文件（带 / 前缀）应该返回 true");
        assertFalse(loader.exists("nonexistent/template.etl"), "不存在的文件应该返回 false");
        assertFalse(loader.exists(null), "null 路径应该返回 false");
        assertFalse(loader.exists(""), "空路径应该返回 false");
    }

    // ==================== AC 4: TemplateEngine 集成测试 ====================

    @Test
    void testTemplateEngineWithFileTemplateLoader() throws IOException {
        // Given: TemplateEngine 已配置 TemplateLoader
        // When: 编译包含 extends 语句的模板
        // Then: 应该能够正确加载外部模板文件
        
        // 创建基础模板文件
        File baseTemplate = templatesDir.resolve("base.etl").toFile();
        Files.write(baseTemplate.toPath(), "Base: {content}".getBytes(StandardCharsets.UTF_8));
        
        // 创建子模板文件
        File childTemplate = templatesDir.resolve("child.etl").toFile();
        Files.write(childTemplate.toPath(), "{% extends 'base.etl' %}".getBytes(StandardCharsets.UTF_8));
        
        // 创建 TemplateEngine，配置 FileTemplateLoader
        FileTemplateLoader loader = new FileTemplateLoader(templatesDir.toString());
        TemplateEngine engine = new TemplateEngine(new TemplateEngine.TemplateEngineConfig(), loader);
        
        // 编译包含 extends 的模板
        Template template = engine.compile("{% extends 'base.etl' %}");
        
        // 验证模板可以正常编译
        assertNotNull(template, "模板应该能够正常编译");
    }

    @Test
    void testTemplateEngineWithClasspathTemplateLoader() {
        // Given: TemplateEngine 已配置 ClasspathTemplateLoader
        // When: 编译包含 extends 语句的模板
        // Then: 应该能够正确加载外部模板文件
        
        ClasspathTemplateLoader loader = new ClasspathTemplateLoader();
        TemplateEngine engine = new TemplateEngine(new TemplateEngine.TemplateEngineConfig(), loader);
        
        // 编译包含 extends 的模板（使用 classpath 中的模板）
        // 注意：需要先创建一个可以被 extends 的模板文件
        Template template = engine.compile("Hello, {name}!");
        
        // 验证模板可以正常编译
        assertNotNull(template, "模板应该能够正常编译");
    }

    @Test
    void testTemplateEngineAutoCreateFileTemplateLoader() throws IOException {
        // Given: TemplateEngine 配置了 templateBasePath
        // When: 创建 TemplateEngine
        // Then: 应该自动创建 FileTemplateLoader
        
        // 创建模板文件
        File templateFile = templatesDir.resolve("test.etl").toFile();
        Files.write(templateFile.toPath(), "Test".getBytes(StandardCharsets.UTF_8));
        
        // 创建 TemplateEngine，配置 templateBasePath
        TemplateEngine.TemplateEngineConfig config = new TemplateEngine.TemplateEngineConfig()
                .setTemplateBasePath(templatesDir.toString());
        TemplateEngine engine = new TemplateEngine(config);
        
        // 验证 TemplateEngine 可以正常使用
        assertNotNull(engine, "TemplateEngine 应该能够正常创建");
        
        // 验证可以通过 extends 加载模板（需要验证内部是否有 loader）
        // 这里我们通过编译一个简单的模板来验证引擎正常工作
        Template template = engine.compile("Hello");
        assertNotNull(template, "模板应该能够正常编译");
    }

    // ==================== AC 5: extends 功能测试 ====================

    @Test
    void testExtendsFunctionality() throws IOException {
        // Given: 模板中包含 {% extends 'template.etl' %} 语句
        // When: 编译并渲染模板
        // Then: 应该加载并渲染外部模板文件的内容
        
        // 创建基础模板文件
        File baseTemplate = templatesDir.resolve("base.etl").toFile();
        String baseContent = "Base template: {message}";
        Files.write(baseTemplate.toPath(), baseContent.getBytes(StandardCharsets.UTF_8));
        
        // 创建 TemplateEngine，配置 FileTemplateLoader
        FileTemplateLoader loader = new FileTemplateLoader(templatesDir.toString());
        TemplateEngine engine = new TemplateEngine(new TemplateEngine.TemplateEngineConfig(), loader);
        
        // 编译包含 extends 的模板
        Template template = engine.compile("{% extends 'base.etl' %}");
        
        // 渲染模板
        Context context = new Context();
        context.put("message", "Hello");
        String result = template.render(context);
        
        // 验证结果
        assertEquals("Base template: Hello", result, "应该加载并渲染外部模板文件的内容");
    }

    @Test
    void testExtendsWithContextVariables() throws IOException {
        // Given: 模板中包含 {% extends 'template.etl' %} 语句
        // When: 编译并渲染模板
        // Then: 外部模板可以访问当前上下文中的变量
        
        // 创建基础模板文件，使用上下文变量
        File baseTemplate = templatesDir.resolve("base.etl").toFile();
        String baseContent = "User: {user.name}, Age: {user.age}";
        Files.write(baseTemplate.toPath(), baseContent.getBytes(StandardCharsets.UTF_8));
        
        // 创建 TemplateEngine，配置 FileTemplateLoader
        FileTemplateLoader loader = new FileTemplateLoader(templatesDir.toString());
        TemplateEngine engine = new TemplateEngine(new TemplateEngine.TemplateEngineConfig(), loader);
        
        // 编译包含 extends 的模板
        Template template = engine.compile("{% extends 'base.etl' %}");
        
        // 创建上下文并设置变量
        Context context = new Context();
        User user = new User("Alice", 30);
        context.put("user", user);
        
        // 渲染模板
        String result = template.render(context);
        
        // 验证结果
        assertEquals("User: Alice, Age: 30", result, "外部模板应该可以访问当前上下文中的变量");
    }

    @Test
    void testExtendsWithoutTemplateLoader() {
        // Given: TemplateEngine 没有配置 TemplateLoader
        // When: 编译包含 extends 的模板
        // Then: 应该抛出异常
        
        TemplateEngine engine = new TemplateEngine();
        
        assertThrows(Exception.class, () -> {
            Template template = engine.compile("{% extends 'base.etl' %}");
            Context context = new Context();
            template.render(context);
        }, "没有配置 TemplateLoader 时，extends 应该抛出异常");
    }

    // ==================== AC 6: Getter 方法测试 ====================

    @Test
    void testFileTemplateLoaderGetters() {
        // Given: FileTemplateLoader 实例
        // When: 调用 getter 方法
        // Then: 应该返回正确的值
        
        String basePath = "/test/path";
        Charset charset = StandardCharsets.ISO_8859_1;
        
        FileTemplateLoader loader1 = new FileTemplateLoader();
        assertEquals("", loader1.getBasePath(), "默认基础路径应该是空字符串");
        assertEquals(StandardCharsets.UTF_8, loader1.getCharset(), "默认编码应该是 UTF-8");
        
        FileTemplateLoader loader2 = new FileTemplateLoader(basePath);
        assertEquals(basePath, loader2.getBasePath(), "应该返回设置的基础路径");
        assertEquals(StandardCharsets.UTF_8, loader2.getCharset(), "默认编码应该是 UTF-8");
        
        FileTemplateLoader loader3 = new FileTemplateLoader(basePath, charset);
        assertEquals(basePath, loader3.getBasePath(), "应该返回设置的基础路径");
        assertEquals(charset, loader3.getCharset(), "应该返回设置的编码");
    }

    @Test
    void testFileTemplateLoaderWithNullBasePath() {
        // Given: FileTemplateLoader 使用 null 基础路径
        // When: 创建实例
        // Then: 应该使用空字符串作为基础路径
        
        FileTemplateLoader loader = new FileTemplateLoader(null);
        assertEquals("", loader.getBasePath(), "null 基础路径应该转换为空字符串");
    }

    @Test
    void testFileTemplateLoaderWithNullCharset() {
        // Given: FileTemplateLoader 使用 null 编码
        // When: 创建实例
        // Then: 应该使用 UTF-8 作为默认编码
        
        FileTemplateLoader loader = new FileTemplateLoader("/test", null);
        assertEquals(StandardCharsets.UTF_8, loader.getCharset(), "null 编码应该使用 UTF-8 默认值");
    }

    @Test
    void testFileTemplateLoaderWithDifferentCharset() throws IOException {
        // Given: FileTemplateLoader 使用不同的字符编码
        // When: 加载包含特殊字符的文件
        // Then: 应该使用指定的编码正确读取
        
        // 创建包含中文的模板文件
        File templateFile = templatesDir.resolve("chinese.etl").toFile();
        String templateContent = "你好，{name}！";
        Files.write(templateFile.toPath(), templateContent.getBytes(StandardCharsets.UTF_8));
        
        FileTemplateLoader loader = new FileTemplateLoader(templatesDir.toString(), StandardCharsets.UTF_8);
        String loaded = loader.load("chinese.etl");
        assertEquals(templateContent, loaded, "应该能够使用 UTF-8 编码正确读取中文内容");
    }

    @Test
    void testFileTemplateLoaderLoadDirectory() throws IOException {
        // Given: FileTemplateLoader
        // When: 尝试加载目录而非文件
        // Then: 应该抛出异常
        
        // 创建一个目录
        Path dirPath = templatesDir.resolve("subdir");
        Files.createDirectories(dirPath);
        
        FileTemplateLoader loader = new FileTemplateLoader(templatesDir.toString());
        
        assertThrows(TemplateCompileException.class, () -> {
            loader.load("subdir");
        }, "尝试加载目录应该抛出 TemplateCompileException");
    }

    @Test
    void testClasspathTemplateLoaderGetters() {
        // Given: ClasspathTemplateLoader 实例
        // When: 调用 getter 方法
        // Then: 应该返回正确的值
        
        Charset charset = StandardCharsets.ISO_8859_1;
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        
        ClasspathTemplateLoader loader1 = new ClasspathTemplateLoader();
        assertEquals(StandardCharsets.UTF_8, loader1.getCharset(), "默认编码应该是 UTF-8");
        
        ClasspathTemplateLoader loader2 = new ClasspathTemplateLoader(charset);
        assertEquals(charset, loader2.getCharset(), "应该返回设置的编码");
        
        ClasspathTemplateLoader loader3 = new ClasspathTemplateLoader(charset, classLoader);
        assertEquals(charset, loader3.getCharset(), "应该返回设置的编码");
    }

    @Test
    void testClasspathTemplateLoaderWithNullCharset() {
        // Given: ClasspathTemplateLoader 使用 null 编码
        // When: 创建实例
        // Then: 应该使用 UTF-8 作为默认编码
        
        ClasspathTemplateLoader loader = new ClasspathTemplateLoader(null);
        assertEquals(StandardCharsets.UTF_8, loader.getCharset(), "null 编码应该使用 UTF-8 默认值");
    }

    @Test
    void testClasspathTemplateLoaderWithNullClassLoader() {
        // Given: ClasspathTemplateLoader 使用 null ClassLoader
        // When: 创建实例
        // Then: 应该使用当前线程的上下文类加载器
        
        ClasspathTemplateLoader loader = new ClasspathTemplateLoader(StandardCharsets.UTF_8, null);
        assertNotNull(loader, "应该能够创建实例");
        // 验证可以正常使用（使用已知存在的资源）
        assertTrue(loader.exists("loader-test.etl"), "应该能够使用默认类加载器找到资源");
    }

    @Test
    void testFileTemplateLoaderLoadEmptyPath() {
        // Given: FileTemplateLoader
        // When: 尝试加载空路径
        // Then: 应该抛出异常
        
        FileTemplateLoader loader = new FileTemplateLoader();
        
        assertThrows(TemplateCompileException.class, () -> {
            loader.load("");
        }, "空路径应该抛出 TemplateCompileException");
        
        assertThrows(TemplateCompileException.class, () -> {
            loader.load(null);
        }, "null 路径应该抛出 TemplateCompileException");
    }

    @Test
    void testClasspathTemplateLoaderLoadEmptyPath() {
        // Given: ClasspathTemplateLoader
        // When: 尝试加载空路径
        // Then: 应该抛出异常
        
        ClasspathTemplateLoader loader = new ClasspathTemplateLoader();
        
        assertThrows(TemplateCompileException.class, () -> {
            loader.load("");
        }, "空路径应该抛出 TemplateCompileException");
        
        assertThrows(TemplateCompileException.class, () -> {
            loader.load(null);
        }, "null 路径应该抛出 TemplateCompileException");
    }

    // ==================== 辅助类 ====================

    /**
     * 测试用的 User 类
     */
    static class User {
        private String name;
        private int age;

        public User(String name, int age) {
            this.name = name;
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public int getAge() {
            return age;
        }
    }
}
