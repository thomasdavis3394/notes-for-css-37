# Engine/Builder 层 importClass 功能设计

## 概述

在 TemplateEngine、EngineBuilder、TemplateBuilder 层添加 importClass 方法，实现编译期别名注入。别名在编译时替换为完整类名，编译后的 Template 不依赖 Context 中的类绑定。

## 功能需求

### 1. 三层 importClass API

```java
// Engine 层（运行时可动态添加）
TemplateEngine engine = new EngineBuilder()
    .importClass(Math.class)
    .importClass(ArrayList.class, "List")
    .build();

engine.importClass(Date.class);  // 后续动态添加

// TemplateBuilder 层（编译时配置）
Template t = new TemplateBuilder("source")
    .importClass(HashMap.class, "Map")
    .compile();
```

### 2. 别名替换机制

模板 `{Math.abs(-1)}` 编译后，`Math` 被替换为 `java.lang.Math`，生成的 Template 不依赖 Context 中的类绑定，保持线程安全。

### 3. 层级覆盖与冲突检测

- TemplateBuilder.importClass 覆盖 Engine.importClass 的同名别名（指向相同类时允许）
- 别名冲突（指向不同类）时报错

## 设计细节

### 第一部分：TemplateEngine 层

**新增字段：**

```java
// 线程安全的别名映射
private final ConcurrentHashMap<String, Class<?>> importedClasses = new ConcurrentHashMap<>();
```

**新增方法：**

```java
/**
 * 导入Java类，使用简单名称作为别名
 *
 * @param clazz 要导入的Java类
 * @return this，支持链式调用
 */
public TemplateEngine importClass(Class<?> clazz) {
    importedClasses.put(clazz.getSimpleName(), clazz);
    return this;
}

/**
 * 导入Java类，使用指定别名
 *
 * @param clazz 要导入的Java类
 * @param alias 别名
 * @return this，支持链式调用
 */
public TemplateEngine importClass(Class<?> clazz, String alias) {
    importedClasses.put(alias, clazz);
    return this;
}

/**
 * 获取当前导入的类映射（不可修改副本）
 *
 * @return 别名到类的映射副本
 */
public Map<String, Class<?>> getImportedClasses() {
    return new HashMap<>(importedClasses);
}
```

**新增编译方法：**

```java
/**
 * 编译模板字符串，使用额外的导入映射
 *
 * @param template 模板字符串
 * @param importsMap 别名到类的映射（可与 TemplateBuilder.importClass 配合使用）
 * @return 编译后的模板对象
 */
public Template compile(String template, Map<String, Class<?>> importsMap) {
    // 内部编译时传递 importsMap
}
```

### 第二部分：EngineBuilder 层

**新增字段：**

```java
private Map<String, Class<?>> importedClasses = new HashMap<>();
```

**新增方法：**

```java
/**
 * 导入Java类，使用简单名称作为别名
 *
 * @param clazz 要导入的Java类
 * @return this，支持链式调用
 */
public EngineBuilder importClass(Class<?> clazz) {
    importedClasses.put(clazz.getSimpleName(), clazz);
    return this;
}

/**
 * 导入Java类，使用指定别名
 *
 * @param clazz 要导入的Java类
 * @param alias 别名
 * @return this，支持链式调用
 */
public EngineBuilder importClass(Class<?> clazz, String alias) {
    importedClasses.put(alias, clazz);
    return this;
}
```

**修改 build() 方法：**

```java
public TemplateEngine build() {
    TemplateEngine engine = new TemplateEngine(config);
    // 初始化导入的类
    for (Map.Entry<String, Class<?>> entry : importedClasses.entrySet()) {
        engine.importClass(entry.getValue(), entry.getKey());
    }
    return engine;
}
```

### 第三部分：TemplateBuilder 层

**新增字段：**

```java
private Map<String, Class<?>> importedClasses;
```

**新增方法：**

```java
/**
 * 导入Java类，使用简单名称作为别名
 *
 * @param clazz 要导入的Java类
 * @return this，支持链式调用
 */
public TemplateBuilder importClass(Class<?> clazz) {
    ensureImportedClasses();
    importedClasses.put(clazz.getSimpleName(), clazz);
    return this;
}

/**
 * 导入Java类，使用指定别名
 *
 * @param clazz 要导入的Java类
 * @param alias 别名
 * @return this，支持链式调用
 */
public TemplateBuilder importClass(Class<?> clazz, String alias) {
    ensureImportedClasses();
    importedClasses.put(alias, clazz);
    return this;
}

private void ensureImportedClasses() {
    if (importedClasses == null) {
        importedClasses = new HashMap<>();
    }
}
```

**修改 compile() 方法（别名冲突检测）：**

```java
public Template compile() {
    TemplateEngine engine = getOrCreateEngine();

    // 合并前检测冲突
    Map<String, Class<?>> engineImports = engine.getImportedClasses();
    if (importedClasses != null) {
        for (String alias : importedClasses.keySet()) {
            Class<?> engineClass = engineImports.get(alias);
            Class<?> builderClass = importedClasses.get(alias);
            if (engineClass != null && !engineClass.equals(builderClass)) {
                throw new TemplateCompileException(
                    "Import alias conflict: '" + alias + "' already defined as " +
                    engineClass.getName() + " in engine, cannot redefine as " +
                    builderClass.getName());
            }
        }
    }

    // 合并映射（TemplateBuilder 覆盖 Engine 同名同类）
    Map<String, Class<?>> mergedImports = new HashMap<>(engineImports);
    if (importedClasses != null) {
        mergedImports.putAll(importedClasses);
    }

    return engine.compile(source, mergedImports);
}
```

### 第四部分：Compiler 层扩展

**TemplateEngine.compile(source, importsMap)：**

```java
public Template compile(final String template, Map<String, Class<?>> importsMap) {
    if (template == null) {
        throw new TemplateCompileException("Template string cannot be null");
    }

    // 缓存逻辑（缓存键不考虑 importsMap）
    if (config.isCacheEnabled() && templateCache != null && template.length() <= MAX_CACHE_KEY_LENGTH) {
        final String cacheKey = template;  // 保持原有行为

        cacheLock.readLock().lock();
        try {
            final Template cached = templateCache.get(cacheKey);
            if (cached != null) {
                return cached;
            }
        } finally {
            cacheLock.readLock().unlock();
        }

        final Template compiled = compileInternal(template, importsMap);

        cacheLock.writeLock().lock();
        try {
            templateCache.put(cacheKey, compiled);
        } finally {
            cacheLock.writeLock().unlock();
        }

        return compiled;
    } else {
        return compileInternal(template, importsMap);
    }
}

private Template compileInternal(final String template, Map<String, Class<?>> importsMap) {
    // ... 词法分析、语法解析 ...

    Compiler compiler = new InterpreterCompiler(
        templateLoader,
        template,
        config.isTemplateJavaClassImportEnabled(),
        importsMap);  // 新增参数
    return compiler.compile(ast);
}
```

**InterpreterTemplate 新增字段：**

```java
private final Map<String, Class<?>> importsMap;

public InterpreterTemplate(..., Map<String, Class<?>> importsMap) {
    // ...
    this.importsMap = importsMap != null ? importsMap : Collections.emptyMap();
}
```

**Evaluator 接收 importsMap：**

```java
private final Map<String, Class<?>> importsMap;

public Evaluator(Context context, TemplateLoader loader, String source,
                 boolean templateJavaClassImportEnabled,
                 Map<String, Class<?>> importsMap) {
    this.context = context;
    this.loader = loader;
    this.source = source;
    this.templateJavaClassImportEnabled = templateJavaClassImportEnabled;
    this.importsMap = importsMap != null ? importsMap : Collections.emptyMap();
}
```

**Evaluator.visitIdentifier() 处理别名：**

```java
@Override
public Object visitIdentifier(Identifier node) {
    String name = node.getName();

    // 先查 importsMap
    Class<?> importedClass = importsMap.get(name);
    if (importedClass != null) {
        return importedClass;  // 返回 Class 对象
    }

    // 再查 Context（原有逻辑）
    return context.get(name);
}
```

### 第五部分：缓存键策略

**保持原有行为，不考虑 importsMap：**

- 缓存键仍为模板源码内容本身
- importsMap 只在编译时生效
- 相同源码命中缓存后，importsMap 配置差异不影响

**说明：** 这意味着 TemplateBuilder.importClass 配置的类只在首次编译时生效；缓存命中后返回已编译的 Template（其 importsMap 为编译时的值）。

### 第六部分：层级覆盖规则

**优先级：**

1. TemplateBuilder.importClass > Engine.importClass（同名同类时覆盖）
2. 别名冲突（同名不同类）时报错

**覆盖规则：**

```
Engine.importClass(Math.class)           → {"Math": java.lang.Math}
TemplateBuilder.importClass(Math.class)  → 覆盖，仍为 java.lang.Math（同类允许）
TemplateBuilder.importClass(MyMath.class, "Math") → 报错，别名冲突
```

## 改动文件列表

| 文件 | 改动类型 | 说明 |
|------|----------|------|
| TemplateEngine.java | 新增字段/方法 | importedClasses、importClass()、getImportedClasses()、compile(source, importsMap) |
| EngineBuilder.java | 新增字段/方法 | importedClasses、importClass()、build() 初始化导入 |
| TemplateBuilder.java | 新增字段/方法 | importedClasses、importClass()、compile() 冲突检测 |
| InterpreterCompiler.java | 新增参数 | compile() 接收 importsMap |
| InterpreterTemplate.java | 新增字段 | importsMap 字段及构造函数 |
| Evaluator.java | 新增参数/逻辑 | 构造函数接收 importsMap，visitIdentifier() 先查 importsMap |

## 使用示例

### Java API 使用

```java
// Engine 层预配置
TemplateEngine engine = new EngineBuilder()
    .importClass(Math.class)
    .importClass(ArrayList.class, "List")
    .build();

// 动态添加
engine.importClass(Date.class);

// TemplateBuilder 层额外导入
Template t = new TemplateBuilder("{Math.sqrt(16)}")
    .engine(engine)
    .importClass(HashMap.class, "Map")
    .compile();

String result = t.render(new Context());
// 输出: 4.0
```

### 别名覆盖（同类）

```java
TemplateEngine engine = new EngineBuilder()
    .importClass(ArrayList.class, "List")
    .build();

// 同名同类，允许覆盖
Template t = new TemplateBuilder("source")
    .engine(engine)
    .importClass(ArrayList.class, "List")
    .compile();  // 正常编译
```

### 别名冲突（不同类）

```java
TemplateEngine engine = new EngineBuilder()
    .importClass(ArrayList.class, "List")
    .build();

// 同名不同类，报错
Template t = new TemplateBuilder("source")
    .engine(engine)
    .importClass(LinkedList.class, "List")
    .compile();  // 抛出 TemplateCompileException
```

## 测试要点

1. **TemplateEngine.importClass 测试**
   - 使用简单名称作为别名
   - 使用指定别名
   - 动态添加后编译生效
   - getImportedClasses() 返回不可修改副本

2. **EngineBuilder.importClass 测试**
   - 链式调用配置多个导入
   - build() 后 Engine.getImportedClasses() 包含配置的类

3. **TemplateBuilder.importClass 测试**
   - 链式调用配置导入
   - 与 Engine 导入合并生效
   - 同名同类覆盖成功
   - 同名不同类报错

4. **编译效果测试**
   - 模板 `{Math.PI}` 使用导入的别名
   - 模板 `{List.add("item")}` 创建实例并调用方法
   - 未导入的别名仍从 Context 查找

5. **缓存行为测试**
   - 相同源码命中缓存，importsMap 不影响
   - 首次编译使用 importsMap，缓存命中后保持编译时的值

6. **线程安全测试**
   - Engine.importClass() 多线程调用不冲突
   - 编译后的 Template 可多线程渲染