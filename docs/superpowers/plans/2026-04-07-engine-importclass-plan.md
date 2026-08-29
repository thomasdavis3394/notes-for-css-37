# Engine/Builder 层 importClass 功能实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在 TemplateEngine、EngineBuilder、TemplateBuilder 层添加 importClass 方法，实现编译期别名注入

**Architecture:** 三层 API（Engine → EngineBuilder → TemplateBuilder），别名在编译时替换为完整类名，编译后的 Template 不依赖 Context 类绑定

**Tech Stack:** Java 8, ConcurrentHashMap（线程安全），JUnit 5

---

## 文件结构

| 文件 | 责责 |
|------|------|
| `TemplateEngine.java` | 添加 `importedClasses` 字段、`importClass()` 方法、`compile(source, importsMap)` 方法 |
| `EngineBuilder.java` | 添加 `importedClasses` 字段、`importClass()` 方法，修改 `build()` |
| `TemplateBuilder.java` | 添加 `importedClasses` 字段、`importClass()` 方法，修改 `compile()` 冲突检测 |
| `InterpreterCompiler.java` | 修改构造函数和 `compile()` 方法，接收 `importsMap` 参数 |
| `InterpreterTemplate.java` | 添加 `importsMap` 字段，传递给 Evaluator |
| `Evaluator.java` | 添加 `importsMap` 字段，修改 `visitIdentifier()` 先查 importsMap |
| `EngineImportClassTest.java` | 测试 Engine.importClass 功能 |
| `TemplateBuilderImportClassTest.java` | 测试 TemplateBuilder.importClass 功能 |

---

### Task 1: TemplateEngine.importClass 和 getImportedClasses

**Files:**
- Modify: `src/main/java/org/dromara/easytl/TemplateEngine.java:27-108`
- Test: `src/test/java/org/dromara/easytl/EngineImportClassTest.java`

- [ ] **Step 1: Write the failing test**

```java
package org.dromara.easytl;

import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class EngineImportClassTest {

    @Test
    void testImportClassWithSimpleName() {
        TemplateEngine engine = new TemplateEngine();
        engine.importClass(Math.class);

        Map<String, Class<?>> imports = engine.getImportedClasses();
        assertEquals(1, imports.size());
        assertEquals(Math.class, imports.get("Math"));
    }

    @Test
    void testImportClassWithAlias() {
        TemplateEngine engine = new TemplateEngine();
        engine.importClass(java.util.ArrayList.class, "List");

        Map<String, Class<?>> imports = engine.getImportedClasses();
        assertEquals(1, imports.size());
        assertEquals(java.util.ArrayList.class, imports.get("List"));
    }

    @Test
    void testImportClassChaining() {
        TemplateEngine engine = new TemplateEngine();
        assertSame(engine, engine.importClass(Math.class));
        assertSame(engine, engine.importClass(String.class, "Str"));
    }

    @Test
    void testGetImportedClassesReturnsCopy() {
        TemplateEngine engine = new TemplateEngine();
        engine.importClass(Math.class);

        Map<String, Class<?>> imports1 = engine.getImportedClasses();
        Map<String, Class<?>> imports2 = engine.getImportedClasses();

        // 返回副本，修改不影响原数据
        imports1.clear();
        assertEquals(1, imports2.size());
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn test -Dtest=EngineImportClassTest -q`
Expected: FAIL with "cannot find symbol" or "method importClass not found"

- [ ] **Step 3: Write minimal implementation**

在 `TemplateEngine.java` 添加：

```java
// 在类开头添加字段（约第40行附近，在其他字段之后）
import java.util.concurrent.ConcurrentHashMap;

// 新增字段
private final ConcurrentHashMap<String, Class<?>> importedClasses = new ConcurrentHashMap<>();

// 新增方法（在构造函数之后）
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

- [ ] **Step 4: Run test to verify it passes**

Run: `mvn test -Dtest=EngineImportClassTest -q`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/java/org/dromara/easytl/TemplateEngine.java src/test/java/org/dromara/easytl/EngineImportClassTest.java
git commit -m "feat: add importClass and getImportedClasses to TemplateEngine"
```

---

### Task 2: EngineBuilder.importClass

**Files:**
- Modify: `src/main/java/org/dromara/easytl/EngineBuilder.java`
- Test: `src/test/java/org/dromara/easytl/EngineBuilderTest.java`

- [ ] **Step 1: Write the failing test**

在 `EngineBuilderTest.java` 添加：

```java
@Test
void testImportClassWithSimpleName() {
    TemplateEngine engine = new EngineBuilder()
        .importClass(Math.class)
        .build();

    Map<String, Class<?>> imports = engine.getImportedClasses();
    assertEquals(1, imports.size());
    assertEquals(Math.class, imports.get("Math"));
}

@Test
void testImportClassWithAlias() {
    TemplateEngine engine = new EngineBuilder()
        .importClass(java.util.ArrayList.class, "List")
        .build();

    Map<String, Class<?>> imports = engine.getImportedClasses();
    assertEquals(java.util.ArrayList.class, imports.get("List"));
}

@Test
void testMultipleImportClasses() {
    TemplateEngine engine = new EngineBuilder()
        .importClass(Math.class)
        .importClass(String.class, "Str")
        .importClass(java.util.Date.class)
        .build();

    Map<String, Class<?>> imports = engine.getImportedClasses();
    assertEquals(3, imports.size());
    assertEquals(Math.class, imports.get("Math"));
    assertEquals(String.class, imports.get("Str"));
    assertEquals(java.util.Date.class, imports.get("Date"));
}

@Test
void testImportClassChaining() {
    EngineBuilder builder = new EngineBuilder();
    assertSame(builder, builder.importClass(Math.class));
    assertSame(builder, builder.importClass(String.class, "Str"));
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn test -Dtest=EngineBuilderTest -q`
Expected: FAIL with "cannot find symbol method importClass"

- [ ] **Step 3: Write minimal implementation**

在 `EngineBuilder.java` 添加：

```java
// 添加 import
import java.util.Map;
import java.util.HashMap;

// 新增字段（在其他字段之后）
private Map<String, Class<?>> importedClasses = new HashMap<>();

// 新增方法
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

// 修改 build() 方法
public TemplateEngine build() {
    TemplateEngine.TemplateEngineConfig config = new TemplateEngine.TemplateEngineConfig()
        .setCacheEnabled(cacheEnabled)
        .setMaxCacheSize(maxCacheSize)
        .setStrictMode(strictMode)
        .setTemplateJavaClassImportEnabled(templateJavaClassImportEnabled)
        .setTemplateBasePath(templateBasePath)
        .setCharset(charset);

    TemplateEngine engine = new TemplateEngine(config);
    // 初始化导入的类
    for (Map.Entry<String, Class<?>> entry : importedClasses.entrySet()) {
        engine.importClass(entry.getValue(), entry.getKey());
    }
    return engine;
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `mvn test -Dtest=EngineBuilderTest -q`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/java/org/dromara/easytl/EngineBuilder.java src/test/java/org/dromara/easytl/EngineBuilderTest.java
git commit -m "feat: add importClass to EngineBuilder and initialize in build()"
```

---

### Task 3: TemplateBuilder.importClass

**Files:**
- Modify: `src/main/java/org/dromara/easytl/TemplateBuilder.java`
- Test: `src/test/java/org/dromara/easytl/TemplateBuilderImportClassTest.java`

- [ ] **Step 1: Write the failing test**

```java
package org.dromara.easytl;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TemplateBuilderImportClassTest {

    @Test
    void testImportClassWithSimpleName() {
        TemplateBuilder builder = new TemplateBuilder("test");
        assertSame(builder, builder.importClass(Math.class));
    }

    @Test
    void testImportClassWithAlias() {
        TemplateBuilder builder = new TemplateBuilder("test");
        assertSame(builder, builder.importClass(java.util.ArrayList.class, "List"));
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn test -Dtest=TemplateBuilderImportClassTest -q`
Expected: FAIL with "cannot find symbol method importClass"

- [ ] **Step 3: Write minimal implementation**

在 `TemplateBuilder.java` 添加：

```java
// 添加 import
import java.util.Map;
import java.util.HashMap;

// 新增字段（在其他 private 字段之后）
private Map<String, Class<?>> importedClasses;

// 新增私有辅助方法
private void ensureImportedClasses() {
    if (importedClasses == null) {
        importedClasses = new HashMap<>();
    }
}

// 新增方法（在其他链式方法之后）
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
```

- [ ] **Step 4: Run test to verify it passes**

Run: `mvn test -Dtest=TemplateBuilderImportClassTest -q`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/java/org/dromara/easytl/TemplateBuilder.java src/test/java/org/dromara/easytl/TemplateBuilderImportClassTest.java
git commit -m "feat: add importClass to TemplateBuilder"
```

---

### Task 4: TemplateEngine.compile(source, importsMap)

**Files:**
- Modify: `src/main/java/org/dromara/easytl/TemplateEngine.java`
- Test: `src/test/java/org/dromara/easytl/EngineImportClassTest.java`

- [ ] **Step 1: Write the failing test**

在 `EngineImportClassTest.java` 添加：

```java
@Test
void testCompileWithImportsMap() {
    TemplateEngine engine = new TemplateEngine();
    Map<String, Class<?>> imports = new HashMap<>();
    imports.put("Math", Math.class);

    Template template = engine.compile("{Math.PI}", imports);
    assertEquals(String.valueOf(Math.PI), template.render(new Context()));
}

@Test
void testCompileWithImportsMapStaticMethod() {
    TemplateEngine engine = new TemplateEngine();
    Map<String, Class<?>> imports = new HashMap<>();
    imports.put("M", Math.class);

    Template template = engine.compile("{M.sqrt(16)}", imports);
    assertEquals("4.0", template.render(new Context()));
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn test -Dtest=EngineImportClassTest -q`
Expected: FAIL - 需要实现 compile(source, importsMap) 方法

- [ ] **Step 3: Write minimal implementation**

在 `TemplateEngine.java` 添加新编译方法：

```java
// 添加 import
import java.util.Map;
import java.util.Collections;

// 新增公共编译方法（在现有 compile(String) 方法附近）
/**
 * 编译模板字符串，使用额外的导入映射
 *
 * @param template 模板字符串
 * @param importsMap 别名到类的映射
 * @return 编译后的模板对象
 * @throws TemplateCompileException 如果编译失败
 */
public Template compile(final String template, Map<String, Class<?>> importsMap) {
    if (template == null) {
        throw new TemplateCompileException("Template string cannot be null");
    }

    // 缓存逻辑：缓存键不考虑 importsMap，保持原有行为
    if (config.isCacheEnabled() && templateCache != null && template.length() <= MAX_CACHE_KEY_LENGTH) {
        final String cacheKey = template;

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

// 修改私有方法 compileInternal，添加 importsMap 参数
private Template compileInternal(final String template, Map<String, Class<?>> importsMap) {
    try {
        StringSource source = new StringSource(template);
        Lexer lexer = new Lexer(source);
        TokenStream tokenStream = new TokenStream(lexer);

        Parser parser = new Parser(tokenStream);
        TemplateNode ast = parser.parse();

        TemplateJavaImportValidator.validate(ast, config.isTemplateJavaClassImportEnabled(), template);

        Compiler compiler = new InterpreterCompiler(
            templateLoader,
            template,
            config.isTemplateJavaClassImportEnabled(),
            importsMap != null ? importsMap : Collections.emptyMap());
        return compiler.compile(ast);
    } catch (TemplateCompileException e) {
        throw e;
    } catch (Exception e) {
        throw new TemplateCompileException("Failed to compile template: " + e.getMessage(), e);
    }
}

// 修改原有 compile(String) 方法调用 compileInternal
public Template compile(final String template) {
    return compile(template, null);
}
```

- [ ] **Step 4: Run test to verify it fails at InterpreterCompiler**

Run: `mvn test -Dtest=EngineImportClassTest -q`
Expected: FAIL - InterpreterCompiler 构造函数需要接收 importsMap 参数

- [ ] **Step 5: Commit partial progress**

```bash
git add src/main/java/org/dromara/easytl/TemplateEngine.java src/test/java/org/dromara/easytl/EngineImportClassTest.java
git commit -m "feat: add compile(source, importsMap) to TemplateEngine (pending compiler changes)"
```

---

### Task 5: InterpreterCompiler 接收 importsMap

**Files:**
- Modify: `src/main/java/org/dromara/easytl/compiler/InterpreterCompiler.java`
- Modify: `src/main/java/org/dromara/easytl/compiler/InterpreterTemplate.java`

- [ ] **Step 1: Add importsMap to InterpreterCompiler**

```java
// 在 InterpreterCompiler.java 添加
import java.util.Map;
import java.util.Collections;

// 新增字段
private final Map<String, Class<?>> importsMap;

// 新增构造函数
/**
 * 构造函数
 *
 * @param templateLoader 模板加载器
 * @param sourceText 源代码文本
 * @param templateJavaClassImportEnabled 是否允许模板内 Java 类 import
 * @param importsMap 别名到类的映射
 */
public InterpreterCompiler(TemplateLoader templateLoader, String sourceText,
        boolean templateJavaClassImportEnabled, Map<String, Class<?>> importsMap) {
    this.templateLoader = templateLoader;
    this.sourceText = sourceText;
    this.templateJavaClassImportEnabled = templateJavaClassImportEnabled;
    this.importsMap = importsMap != null ? importsMap : Collections.emptyMap();
}

// 修改 compile 方法
@Override
public Template compile(TemplateNode ast) throws TemplateCompileException {
    if (ast == null) {
        throw new TemplateCompileException("AST cannot be null");
    }

    try {
        return new InterpreterTemplate(ast, templateLoader, sourceText, 
            templateJavaClassImportEnabled, importsMap);
    } catch (Exception e) {
        throw new TemplateCompileException("Failed to compile template: " + e.getMessage(), e);
    }
}
```

- [ ] **Step 2: Add importsMap to InterpreterTemplate**

```java
// 在 InterpreterTemplate.java 添加
import java.util.Map;
import java.util.Collections;

// 新增字段
private final Map<String, Class<?>> importsMap;

// 新增构造函数
public InterpreterTemplate(TemplateNode ast, TemplateLoader templateLoader, String sourceText,
        boolean templateJavaClassImportEnabled, Map<String, Class<?>> importsMap) {
    if (ast == null) {
        throw new IllegalArgumentException("AST cannot be null");
    }
    this.ast = ast;
    this.templateLoader = templateLoader;
    this.sourceText = sourceText;
    this.templateJavaClassImportEnabled = templateJavaClassImportEnabled;
    this.importsMap = importsMap != null ? importsMap : Collections.emptyMap();
}

// 修改 render 方法，传递 importsMap 给 Evaluator
@Override
public String render(Context context) {
    if (context == null) {
        throw new IllegalArgumentException("Context cannot be null");
    }

    try {
        Evaluator evaluator = new Evaluator(context, templateLoader, sourceText, 
            templateJavaClassImportEnabled, importsMap);
        Object result = ast.accept(evaluator);
        return result != null ? result.toString() : "";
    } catch (TemplateException e) {
        throw e;
    } catch (Exception e) {
        throw new TemplateRuntimeException("Failed to render template: " + e.getMessage(), e);
    }
}
```

- [ ] **Step 3: Run compile test**

Run: `mvn test -Dtest=EngineImportClassTest -q`
Expected: FAIL - Evaluator 构造函数需要接收 importsMap

- [ ] **Step 4: Commit partial progress**

```bash
git add src/main/java/org/dromara/easytl/compiler/InterpreterCompiler.java src/main/java/org/dromara/easytl/compiler/InterpreterTemplate.java
git commit -m "feat: add importsMap to InterpreterCompiler and InterpreterTemplate"
```

---

### Task 6: Evaluator 接收 importsMap 并处理 visitIdentifier

**Files:**
- Modify: `src/main/java/org/dromara/easytl/compiler/Evaluator.java`

- [ ] **Step 1: Add importsMap to Evaluator**

```java
// 在 Evaluator.java 添加
import java.util.Map;
import java.util.Collections;

// 新增字段
private final Map<String, Class<?>> importsMap;

// 新增构造函数
public Evaluator(Context context, TemplateLoader templateLoader, String sourceText,
        boolean templateJavaClassImportEnabled, Map<String, Class<?>> importsMap) {
    this.context = context;
    this.templateLoader = templateLoader;
    this.sourceText = sourceText;
    this.templateJavaClassImportEnabled = templateJavaClassImportEnabled;
    this.importsMap = importsMap != null ? importsMap : Collections.emptyMap();
}

// 修改 visitIdentifier 方法
@Override
public Object visitIdentifier(Identifier node) {
    String name = node.getName();
    
    // 先查 importsMap
    Class<?> importedClass = importsMap.get(name);
    if (importedClass != null) {
        return importedClass;
    }
    
    // 再查 Context（原有逻辑）
    return context.get(name);
}
```

- [ ] **Step 2: Update all Evaluator instantiation calls**

在 Evaluator.java 中搜索所有创建新 Evaluator 的地方，添加 importsMap 参数：

```java
// visitForExpression 中（约第623行）
Evaluator loopEvaluator = new Evaluator(loopContext, templateLoader, sourceText, 
    templateJavaClassImportEnabled, importsMap);

// visitForStatement 中（约第756行）
Evaluator loopEvaluator = new Evaluator(loopContext, templateLoader, sourceText, 
    templateJavaClassImportEnabled, importsMap);

// visitExtendsStatement 中（约第843行）
Object result = externalAst.accept(new Evaluator(context, templateLoader, sourceText, 
    templateJavaClassImportEnabled, importsMap));
```

- [ ] **Step 3: Run compile test**

Run: `mvn test -Dtest=EngineImportClassTest -q`
Expected: PASS

- [ ] **Step 4: Commit**

```bash
git add src/main/java/org/dromara/easytl/compiler/Evaluator.java
git commit -m "feat: add importsMap to Evaluator and handle in visitIdentifier"
```

---

### Task 7: TemplateBuilder.compile() 合并 importsMap 和冲突检测

**Files:**
- Modify: `src/main/java/org/dromara/easytl/TemplateBuilder.java`
- Test: `src/test/java/org/dromara/easytl/TemplateBuilderImportClassTest.java`

- [ ] **Step 1: Write the failing test**

在 `TemplateBuilderImportClassTest.java` 添加：

```java
import org.dromara.easytl.exception.TemplateCompileException;
import java.util.HashMap;
import java.util.Map;

@Test
void testCompileWithImportClass() {
    String result = new TemplateBuilder("{Math.PI}")
        .importClass(Math.class)
        .render();
    assertEquals(String.valueOf(Math.PI), result);
}

@Test
void testCompileWithImportClassAlias() {
    String result = new TemplateBuilder("{M.sqrt(16)}")
        .importClass(Math.class, "M")
        .render();
    assertEquals("4.0", result);
}

@Test
void testEngineImportClassUsedByTemplateBuilder() {
    TemplateEngine engine = new EngineBuilder()
        .importClass(Math.class)
        .build();

    String result = new TemplateBuilder("{Math.PI}")
        .engine(engine)
        .render();
    assertEquals(String.valueOf(Math.PI), result);
}

@Test
void testTemplateBuilderMergedWithEngineImports() {
    TemplateEngine engine = new EngineBuilder()
        .importClass(Math.class)
        .build();

    String result = new TemplateBuilder("{Math.PI} {ArrayList().size()}")
        .engine(engine)
        .importClass(java.util.ArrayList.class)
        .render();
    assertEquals(String.valueOf(Math.PI) + " 0", result);
}

@Test
void testAliasConflictThrowsException() {
    TemplateEngine engine = new EngineBuilder()
        .importClass(java.util.ArrayList.class, "List")
        .build();

    TemplateCompileException ex = assertThrows(TemplateCompileException.class, () -> {
        new TemplateBuilder("test")
            .engine(engine)
            .importClass(java.util.LinkedList.class, "List")
            .compile();
    });
    assertTrue(ex.getMessage().contains("alias conflict"));
}

@Test
void testSameAliasSameClassAllowed() {
    TemplateEngine engine = new EngineBuilder()
        .importClass(java.util.ArrayList.class, "List")
        .build();

    // 同名同类，允许覆盖
    Template template = new TemplateBuilder("{List().size()}")
        .engine(engine)
        .importClass(java.util.ArrayList.class, "List")
        .compile();
    assertEquals("0", template.render(new Context()));
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn test -Dtest=TemplateBuilderImportClassTest -q`
Expected: FAIL - compile() 方法需要合并 importsMap

- [ ] **Step 3: Write implementation**

修改 `TemplateBuilder.java` 的 `compile()` 方法：

```java
import org.dromara.easytl.exception.TemplateCompileException;

public Template compile() {
    TemplateEngine engine = getOrCreateEngine();

    // 获取 Engine 的导入
    Map<String, Class<?>> engineImports = engine.getImportedClasses();

    // 检测冲突
    if (importedClasses != null) {
        for (Map.Entry<String, Class<?>> entry : importedClasses.entrySet()) {
            String alias = entry.getKey();
            Class<?> builderClass = entry.getValue();
            Class<?> engineClass = engineImports.get(alias);

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

// 修改 render() 方法
public String render() {
    Template template = compile();
    return template.render(contextBuilder.build());
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `mvn test -Dtest=TemplateBuilderImportClassTest -q`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/java/org/dromara/easytl/TemplateBuilder.java src/test/java/org/dromara/easytl/TemplateBuilderImportClassTest.java
git commit -m "feat: TemplateBuilder.compile merges importsMap with conflict detection"
```

---

### Task 8: 集成测试

**Files:**
- Test: `src/test/java/org/dromara/easytl/ImportClassIntegrationTest.java`

- [ ] **Step 1: Add integration tests**

在 `ImportClassIntegrationTest.java` 添加：

```java
@Test
void testEngineImportClassWithStaticMethod() {
    TemplateEngine engine = new EngineBuilder()
        .importClass(Math.class)
        .build();

    Template template = engine.compile("{Math.sqrt(16)}");
    assertEquals("4.0", template.render(new Context()));
}

@Test
void testEngineImportClassWithConstructor() {
    TemplateEngine engine = new EngineBuilder()
        .importClass(String.class)
        .build();

    Template template = engine.compile("{String('hello')}");
    assertEquals("hello", template.render(new Context()));
}

@Test
void testEngineDynamicImportClass() {
    TemplateEngine engine = new TemplateEngine();
    
    // 运行时动态添加
    engine.importClass(Math.class);
    
    Template template = engine.compile("{Math.PI}");
    assertEquals(String.valueOf(Math.PI), template.render(new Context()));
}

@Test
void testTemplateBuilderImportClassWithObjectConstruction() {
    String result = new TemplateBuilder("{TestPerson {name: 'Peter'}.getName()}")
        .importClass(TestPerson.class)
        .render();
    assertEquals("Peter", result);
}
```

- [ ] **Step 2: Run all import class tests**

Run: `mvn test -Dtest=ImportClassIntegrationTest -q`
Expected: PASS

- [ ] **Step 3: Run full test suite**

Run: `mvn test -q`
Expected: PASS (all tests)

- [ ] **Step 4: Commit**

```bash
git add src/test/java/org/dromara/easytl/ImportClassIntegrationTest.java
git commit -m "test: add integration tests for Engine/Builder importClass"
```

---

### Task 9: LambdaObject 和 EtlURL 传递 importsMap

**Files:**
- Modify: `src/main/java/org/dromara/easytl/runtime/LambdaObject.java`
- Modify: `src/main/java/org/dromara/easytl/runtime/EtlURL.java`

- [ ] **Step 1: Check LambdaObject.java for Evaluator creation**

```bash
grep -n "new Evaluator" src/main/java/org/dromara/easytl/runtime/LambdaObject.java
```

如果找到，需要添加 importsMap 字段并传递给 Evaluator。

- [ ] **Step 2: Check EtlURL.java for Evaluator creation**

```bash
grep -n "new Evaluator" src/main/java/org/dromara/easytl/runtime/EtlURL.java
```

如果找到，需要添加 importsMap 字段并传递给 Evaluator。

- [ ] **Step 3: Update LambdaObject if needed**

如果 LambdaObject 创建 Evaluator，添加 importsMap 字段：

```java
private final Map<String, Class<?>> importsMap;

// 构造函数添加 importsMap 参数
// invoke 方法中传递 importsMap 给新 Evaluator
```

- [ ] **Step 4: Update EtlURL if needed**

如果 EtlURL 创建 Evaluator，添加 importsMap 字段：

```java
private final Map<String, Class<?>> importsMap;

// 构造函数添加 importsMap 参数
// eval 方法中传递 importsMap 给新 Evaluator
```

- [ ] **Step 5: Update Evaluator.visitLambdaExpression**

确保 LambdaObject 接收 importsMap：

```java
@Override
public Object visitLambdaExpression(LambdaExpression node) {
    Map<String, Object> capturedValues = captureAllVisibleVariables(context);

    return new LambdaObject(
        node.getParameters(),
        node.getBody(),
        node.getExpressionBody(),
        node.isBlockBody(),
        capturedValues,
        context,
        sourceText,
        templateJavaClassImportEnabled,
        importsMap  // 新增
    );
}
```

- [ ] **Step 6: Update Evaluator.visitUrlLiteral**

确保 EtlURL 接收 importsMap：

```java
@Override
public Object visitUrlLiteral(UrlLiteral node) {
    return new EtlURL(
        node.getChildren(),
        context,
        templateLoader,
        sourceText,
        templateJavaClassImportEnabled,
        importsMap  // 新增
    );
}
```

- [ ] **Step 7: Run full test suite**

Run: `mvn test -q`
Expected: PASS

- [ ] **Step 8: Commit**

```bash
git add src/main/java/org/dromara/easytl/runtime/LambdaObject.java src/main/java/org/dromara/easytl/runtime/EtlURL.java src/main/java/org/dromara/easytl/compiler/Evaluator.java
git commit -m "feat: propagate importsMap through LambdaObject and EtlURL"
```

---

### Task 10: 最终验证

- [ ] **Step 1: Run full test suite**

Run: `mvn test -q`
Expected: PASS (all tests)

- [ ] **Step 2: Run compile**

Run: `mvn compile -q`
Expected: SUCCESS

- [ ] **Step 3: Create final commit**

```bash
git add -A
git commit -m "feat: complete Engine/Builder importClass implementation"
```

---

## Spec Coverage Check

| Spec 要求 | 实现位置 |
|-----------|----------|
| TemplateEngine.importClass | Task 1 |
| TemplateEngine.getImportedClasses | Task 1 |
| EngineBuilder.importClass | Task 2 |
| TemplateBuilder.importClass | Task 3 |
| compile(source, importsMap) | Task 4 |
| InterpreterCompiler 接收 importsMap | Task 5 |
| InterpreterTemplate 接收 importsMap | Task 5 |
| Evaluator.visitIdentifier 先查 importsMap | Task 6 |
| TemplateBuilder.compile 冲突检测 | Task 7 |
| TemplateBuilder 覆盖 Engine 同名同类 | Task 7 |
| 缓存键不考虑 importsMap | Task 4 |
| LambdaObject 传递 importsMap | Task 9 |
| EtlURL 传递 importsMap | Task 9 |