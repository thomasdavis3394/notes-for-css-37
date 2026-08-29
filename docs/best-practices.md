# EasyTL 最佳实践

## 目录

1. [性能优化建议](#性能优化建议)
2. [常见错误避免](#常见错误避免)
3. [最佳实践建议](#最佳实践建议)

---

## 性能优化建议

### 1. 启用模板缓存

对于重复使用的模板，启用缓存可以显著提升性能：

```java
TemplateEngine.TemplateEngineConfig config = new TemplateEngine.TemplateEngineConfig();
config.setCacheEnabled(true);
config.setMaxCacheSize(100);

TemplateEngine engine = new TemplateEngine(config);

// 编译后的模板会被缓存
Template template = engine.compile("Hello, {name}!");
// 再次编译相同模板会直接返回缓存结果
Template cached = engine.compile("Hello, {name}!");
```

**适用场景**：静态模板、配置文件模板、邮件模板等重复使用场景。

### 2. 复用 Template 对象

模板对象是线程安全的，可以在多线程环境下共享：

```java
Template template = engine.compile("Hello, {name}!");

// 多个线程可以同时使用同一个模板对象
ExecutorService executor = Executors.newFixedThreadPool(10);
for (int i = 0; i < 100; i++) {
    final int index = i;
    executor.submit(() -> {
        Context ctx = new Context();
        ctx.put("name", "User" + index);
        String result = template.render(ctx);
    });
}
```

### 3. 复用 Context 对象

如果模板变量变化不大，可以复用 Context 对象：

```java
// 创建基础上下文
Context baseContext = new Context();
baseContext.put("appName", "MyApp");
baseContext.put("version", "1.0");

// 为每个请求创建子上下文
Context requestContext = baseContext.createChildContext();
requestContext.put("user", currentUser);
requestContext.put("data", requestData);
```

### 4. 避免大模板缓存

过大的模板源码会占用大量缓存空间：

```java
TemplateEngine.TemplateEngineConfig config = new TemplateEngine.TemplateEngineConfig();
config.setCacheEnabled(true);

// 默认只缓存小于 1024 字符的模板
// 大模板不会被缓存，每次都会重新编译
```

### 5. 合理使用对象属性访问

频繁的属性访问会有一定开销，考虑使用展开的对象：

```java
// 多次属性访问
context.put("user", user);
engine.compile("{user.name}{user.age}{user.address}");

// 更好的方式：将需要的属性直接放入上下文
context.put("name", user.getName());
context.put("age", user.getAge());
context.put("address", user.getAddress());
engine.compile("{name}{age}{address}");
```

---

## 常见错误避免

### 1. 空指针异常

**错误示例**：
```java
// user 为 null 时会抛出异常
engine.compile("{user.name}");
```

**正确做法**：
```java
// 使用 Elvis 运算符提供默认值
engine.compile("{user?.name ?? '未知'}");

// 提前在代码中处理
if (user != null) {
    context.put("user", user);
}
```

### 2. 模板语法错误

**常见错误**：
```java
// 缺少结束标记
engine.compile("{% if condition }Yes");

// 缺少 end for
engine.compile("{% for item in items }- {item}");

// 括号不匹配
engine.compile("{func(a, b}");
```

**正确做法**：
```java
// 确保所有块都有结束标记
engine.compile("{% if condition }Yes{% else }No{/% if }");
engine.compile("{% for item in items }- {item}{/% for }");
engine.compile("{func(a, b)}");
```

### 3. 变量未定义

**错误示例**：
```java
engine.compile("{undefined_var}");  // 在非严格模式下返回 null
```

**正确做法**：
```java
// 严格模式会抛出异常，便于调试
TemplateEngine.TemplateEngineConfig config = new TemplateEngine.TemplateEngineConfig();
config.setStrictMode(true);
TemplateEngine engine = new TemplateEngine(config);

// 或提供默认值
engine.compile("{undefined_var ?? 'default'}");
```

### 4. 类型转换错误

**错误示例**：
```java
context.put("count", "10");  // 字符串
engine.compile("{count + 1}");  // 尝试字符串加数字
```

**正确做法**：
```java
context.put("count", 10);  // 数字
engine.compile("{count + 1}");  // 11
```

### 5. 模板缓存键冲突

**问题**：相同源码的模板会共享缓存：

```java
// 这两个模板会返回相同的缓存对象
Template t1 = engine.compile("Hello {name}");
Template t2 = engine.compile("Hello {name}");  // t1 == t2
```

**如果需要不同行为**，可以禁用缓存或添加唯一标识：

```java
// 禁用缓存
TemplateEngine.TemplateEngineConfig config = new TemplateEngine.TemplateEngineConfig();
config.setCacheEnabled(false);
```

---

## 最佳实践建议

### 1. 项目结构建议

```
src/
├── main/
│   ├── java/
│   │   └── com/example/
│   │       └── MyApplication.java
│   └── resources/
│       └── templates/
│           ├── layout.html
│           ├── components/
│           │   └── header.html
│           └── pages/
│               └── home.html
```

### 2. 模板编写建议

#### 2.1 使用有意义的变量名

```java
// 不推荐
context.put("k", "value");
engine.compile("{k}");

// 推荐
context.put("userName", "value");
engine.compile("{userName}");
```

#### 2.2 保持模板简洁

```java
// 不推荐：逻辑过于复杂
engine.compile("{% if (a > 0 && b < 100) or (c == 'active' && !d) }复杂{/% if }");

// 推荐：使用子表达式
engine.compile("{isValid}");  // 在 Java 中计算好
```

### 3. 错误处理建议

```java
try {
    Template template = engine.compile(templateSource);
    String result = template.render(context);
} catch (TemplateCompileException e) {
    // 模板语法错误
    System.err.println("编译错误: " + e.getMessage());
    System.err.println("位置: 第 " + e.getLine() + " 行");
} catch (TemplateRuntimeException e) {
    // 运行时错误
    System.err.println("运行时错误: " + e.getMessage());
}
```

### 4. 测试建议

```java
@Test
public void testTemplateRendering() {
    TemplateEngine engine = new TemplateEngine();
    Template template = engine.compile("Hello, {name}!");

    Context context = new Context();
    context.put("name", "World");

    String result = template.render(context);
    Assert.assertEquals("Hello, World!", result);
}
```

### 5. 国际化建议

```java
// 按语言创建不同模板
context.put("_lang", "zh");
engine.compile("{% if _lang == 'zh' }你好{% else }Hello{endif} {name}");

// 或使用 Map 存储翻译
Map<String, String> i18n = new HashMap<>();
i18n.put("zh", "你好");
i18n.put("en", "Hello");
context.put("i18n", i18n);
context.put("lang", "zh");
engine.compile("{i18n[lang]} {name}");
```

### 6. 安全建议

```java
// 模板引擎不自动转义 HTML
// 如需转义，在 Java 代码中处理或创建工具方法

// 避免模板注入：不要将用户输入直接作为模板源码
String userInput = getTemplateUserInput();
 template = engine.compile(userInput);  // 危险！
```

---

## 总结

| 场景 | 建议 |
|------|------|
| 重复使用的模板 | 启用缓存 |
| 高并发环境 | 复用 Template 对象 |
| 复杂业务逻辑 | 在 Java 中计算，模板只做展示 |
| 调试困难 | 启用严格模式 |
| 大文本模板 | 禁用缓存 |
| 用户输入 | 避免模板注入 |
