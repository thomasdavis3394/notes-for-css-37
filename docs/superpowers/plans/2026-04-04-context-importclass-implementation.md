# Context importClass Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add importClass methods to Context and support template syntax {% import className as alias %} for importing Java classes, enabling static field access, static method calls, and instance creation.

**Architecture:** Store Class<?> objects directly in Context's variables map. Extend ReflectionUtils to handle Class<?> targets for static property access and constructor invocation. Add ObjectConstruction AST node for constructor + property block syntax.

**Tech Stack:** Java 8, JUnit 5, no third-party dependencies

---

## File Structure

### Modified Files
- `src/main/java/org/dromara/easytl/Context.java` - Add importClass methods
- `src/main/java/org/dromara/easytl/ast/ImportStatement.java` - Add alias field
- `src/main/java/org/dromara/easytl/ast/ASTVisitor.java` - Add visitObjectConstruction method
- `src/main/java/org/dromara/easytl/parser/Parser.java` - Parse `as alias` and ObjectConstruction
- `src/main/java/org/dromara/easytl/compiler/Evaluator.java` - Implement visitImportStatement, modify visitMethodCall, add visitObjectConstruction
- `src/main/java/org/dromara/easytl/runtime/ReflectionUtils.java` - Add static property/method access, createInstance, setProperty

### New Files
- `src/main/java/org/dromara/easytl/ast/ObjectConstruction.java` - AST node for constructor + property block
- `src/test/java/org/dromara/easytl/ImportClassIntegrationTest.java` - Integration tests

---

## Task 1: Context.importClass Methods

**Files:**
- Modify: `src/main/java/org/dromara/easytl/Context.java`
- Test: `src/test/java/org/dromara/easytl/ContextTest.java`

- [ ] **Step 1: Write the failing test for importClass with simple name alias**

```java
// In ContextTest.java, add:

@Test
void testImportClassWithSimpleName() {
    context.importClass(Math.class);

    assertTrue(context.contains("Math"));
    assertEquals(Math.class, context.get("Math"));
}

@Test
void testImportClassWithAlias() {
    context.importClass(ArrayList.class, "List");

    assertTrue(context.contains("List"));
    assertEquals(ArrayList.class, context.get("List"));
}

@Test
void testImportClassOverwrite() {
    context.importClass(Math.class);
    context.importClass(java.lang.System.class, "Math");

    assertEquals(java.lang.System.class, context.get("Math"));
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn test -Dtest=ContextTest#testImportClassWithSimpleName -q`
Expected: FAIL with "cannot find symbol method importClass"

- [ ] **Step 3: Write minimal implementation**

```java
// In Context.java, add after line 96 (after the put(TemplateVariable) method):

/**
 * 导入Java类到上下文，使用类的简单名称作为别名
 *
 * @param clazz 要导入的Java类
 */
public void importClass(Class<?> clazz) {
    String alias = clazz.getSimpleName();
    variables.put(alias, clazz);
}

/**
 * 导入Java类到上下文，使用指定别名
 *
 * @param clazz 要导入的Java类
 * @param alias 别名
 */
public void importClass(Class<?> clazz, String alias) {
    variables.put(alias, clazz);
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `mvn test -Dtest=ContextTest#testImportClass* -q`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/java/org/dromara/easytl/Context.java src/test/java/org/dromara/easytl/ContextTest.java
git commit -m "feat: add importClass methods to Context"
```

---

## Task 2: ImportStatement Alias Field

**Files:**
- Modify: `src/main/java/org/dromara/easytl/ast/ImportStatement.java`
- Test: `src/test/java/org/dromara/easytl/ast/ASTNodeTest.java`

- [ ] **Step 1: Write the failing test**

```java
// In ASTNodeTest.java, find testImportStatement and modify/add:

@Test
void testImportStatementWithAlias() {
    ImportStatement stmt = new ImportStatement("java.util.List", "ArrayList");
    assertEquals("java.util.List", stmt.getClassName());
    assertEquals("ArrayList", stmt.getAlias());
}

@Test
void testImportStatementWithoutAlias() {
    ImportStatement stmt = new ImportStatement("java.util.List");
    assertEquals("java.util.List", stmt.getClassName());
    assertNull(stmt.getAlias());
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn test -Dtest=ASTNodeTest#testImportStatementWithAlias -q`
Expected: FAIL

- [ ] **Step 3: Write minimal implementation**

```java
// In ImportStatement.java, modify the class:

package org.dromara.easytl.ast;

import org.dromara.easytl.parser.Token;

/**
 * import类导入语句节点
 * 表示导入Java类，以便在模板中使用其静态方法
 * 格式：{% import java.util.List %} 或 {% import java.util.List as ArrayList %}
 *
 * @author EasyTL Team
 */
public class ImportStatement extends ASTNode {

    /** Java类全限定名 */
    private final String className;

    /** 别名（可为null） */
    private final String alias;

    /**
     * 构造import语句节点
     *
     * @param className Java类全限定名
     */
    public ImportStatement(String className) {
        this(className, null);
    }

    /**
     * 构造import语句节点
     *
     * @param className Java类全限定名
     * @param alias 别名（可为null）
     */
    public ImportStatement(String className, String alias) {
        this.className = className;
        this.alias = alias;
    }

    /**
     * 构造import语句节点（从 Token 获取位置信息）
     *
     * @param className Java类全限定名
     * @param alias 别名（可为null）
     * @param token Token 对象（用于获取位置信息）
     */
    public ImportStatement(String className, String alias, Token token) {
        super(positionFromToken(token));
        this.className = className;
        this.alias = alias;
    }

    /**
     * 获取Java类全限定名
     *
     * @return Java类全限定名
     */
    public String getClassName() {
        return className;
    }

    /**
     * 获取别名
     *
     * @return 别名，如果没有指定则返回null
     */
    public String getAlias() {
        return alias;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitImportStatement(this);
    }

    @Override
    public String toString() {
        return "ImportStatement{className='" + className + "', alias='" + alias + "'}";
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `mvn test -Dtest=ASTNodeTest#testImportStatement* -q`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/java/org/dromara/easytl/ast/ImportStatement.java src/test/java/org/dromara/easytl/ast/ASTNodeTest.java
git commit -m "feat: add alias field to ImportStatement"
```

---

## Task 3: Parser Support for "as alias" Syntax

**Files:**
- Modify: `src/main/java/org/dromara/easytl/parser/Parser.java`
- Test: `src/test/java/org/dromara/easytl/parser/ParserTest.java`

- [ ] **Step 1: Write the failing test**

```java
// In ParserTest.java, add:

@Test
void testImportStatementWithAlias() {
    String code = "{% import java.util.List as ArrayList %}";
    Lexer lexer = new Lexer(new StringSource(code));
    TokenStream tokens = new TokenStream(lexer);
    Parser parser = new Parser(tokens);
    TemplateNode template = parser.parse();

    assertEquals(1, template.getChildren().size());
    ImportStatement stmt = (ImportStatement) template.getChildren().get(0);
    assertEquals("java.util.List", stmt.getClassName());
    assertEquals("ArrayList", stmt.getAlias());
}

@Test
void testImportStatementWithoutAlias() {
    String code = "{% import java.util.List %}";
    Lexer lexer = new Lexer(new StringSource(code));
    TokenStream tokens = new TokenStream(lexer);
    Parser parser = new Parser(tokens);
    TemplateNode template = parser.parse();

    assertEquals(1, template.getChildren().size());
    ImportStatement stmt = (ImportStatement) template.getChildren().get(0);
    assertEquals("java.util.List", stmt.getClassName());
    assertNull(stmt.getAlias());
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn test -Dtest=ParserTest#testImportStatementWithAlias -q`
Expected: FAIL

- [ ] **Step 3: Write minimal implementation**

```java
// In Parser.java, modify parseImportStatement() method (around line 376):

private ASTNode parseImportStatement() {
    expect(TokenType.IMPORT);
    // 支持 java.util.List 形式
    // Token流可能识别为 IDENTIFIER DOT IDENTIFIER DOT ...
    StringBuilder className = new StringBuilder();
    className.append(expect(TokenType.IDENTIFIER).getValue());

    while (check(TokenType.DOT)) {
        advance();
        className.append(".");
        className.append(expect(TokenType.IDENTIFIER).getValue());
    }

    // 新增：解析可选的 as alias
    String alias = null;
    if (check(TokenType.AS)) {
        advance();
        alias = expect(TokenType.IDENTIFIER).getValue();
    }

    expect(TokenType.STMT_END);
    return new ImportStatement(className.toString(), alias);
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `mvn test -Dtest=ParserTest#testImportStatement* -q`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/java/org/dromara/easytl/parser/Parser.java src/test/java/org/dromara/easytl/parser/ParserTest.java
git commit -m "feat: parser support for import as alias syntax"
```

---

## Task 4: ReflectionUtils Static Property Access

**Files:**
- Modify: `src/main/java/org/dromara/easytl/runtime/ReflectionUtils.java`
- Test: `src/test/java/org/dromara/easytl/runtime/ReflectionUtilsTest.java`

- [ ] **Step 1: Write the failing test**

```java
// In ReflectionUtilsTest.java, add:

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

@Test
void testGetStaticProperty() {
    // Math.PI is a public static field
    Object value = ReflectionUtils.getProperty(Math.class, "PI");
    assertEquals(Math.PI, value);
}

@Test
void testGetStaticFieldNotFound() {
    TemplateRuntimeException ex = assertThrows(TemplateRuntimeException.class, () -> {
        ReflectionUtils.getProperty(Math.class, "NON_EXISTENT_FIELD");
    });
    assertTrue(ex.getMessage().contains("not found"));
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn test -Dtest=ReflectionUtilsTest#testGetStaticProperty -q`
Expected: FAIL

- [ ] **Step 3: Write minimal implementation**

```java
// In ReflectionUtils.java, modify getProperty() method to handle Class<?>:

public static Object getProperty(Object obj, String propertyName) {
    if (obj == null) {
        throw new TemplateRuntimeException("Cannot get property '" + propertyName + "' of null");
    }

    // 新增：如果 obj 是 Class<?>，尝试获取静态属性
    if (obj instanceof Class) {
        Class<?> clazz = (Class<?>) obj;
        return getStaticProperty(clazz, propertyName);
    }

    // 如果是Map，直接使用get方法
    if (obj instanceof Map) {
        return ((Map<?, ?>) obj).get(propertyName);
    }

    // ... 其余原有代码保持不变
}

// Add new method after getProperty():

/**
 * 获取类的静态属性值
 *
 * @param clazz 类
 * @param propertyName 属性名
 * @return 属性值
 */
private static Object getStaticProperty(Class<?> clazz, String propertyName) {
    // 尝试静态 getter 方法
    Method getter = findStaticGetter(clazz, propertyName);
    if (getter != null) {
        try {
            if (!getter.isAccessible()) {
                getter.setAccessible(true);
            }
            return getter.invoke(null);
        } catch (Exception e) {
            throw new TemplateRuntimeException(
                "Failed to invoke static getter '" + getter.getName() + "': " + e.getMessage(), e);
        }
    }

    // 尝试静态字段
    try {
        Field field = findStaticField(clazz, propertyName);
        if (field != null) {
            if (!field.isAccessible()) {
                field.setAccessible(true);
            }
            return field.get(null);
        }
    } catch (Exception e) {
        throw new TemplateRuntimeException(
            "Failed to access static field '" + propertyName + "': " + e.getMessage(), e);
    }

    throw new TemplateRuntimeException(
        "Static property '" + propertyName + "' not found in " + clazz.getName());
}

/**
 * 查找静态 getter 方法
 */
private static Method findStaticGetter(Class<?> clazz, String propertyName) {
    // 尝试 getXxx 形式
    String getterName = "get" + capitalize(propertyName);
    try {
        Method method = clazz.getMethod(getterName);
        if (Modifier.isStatic(method.getModifiers())) {
            return method;
        }
    } catch (NoSuchMethodException e) {
        // 继续尝试其他形式
    }

    // 尝试 isXxx 形式
    String isGetterName = "is" + capitalize(propertyName);
    try {
        Method method = clazz.getMethod(isGetterName);
        if (Modifier.isStatic(method.getModifiers()) &&
            (method.getReturnType() == boolean.class || method.getReturnType() == Boolean.class)) {
            return method;
        }
    } catch (NoSuchMethodException e) {
        // 忽略
    }

    return null;
}

/**
 * 查找静态字段
 */
private static Field findStaticField(Class<?> clazz, String fieldName) {
    try {
        Field field = clazz.getField(fieldName);
        if (Modifier.isStatic(field.getModifiers())) {
            return field;
        }
    } catch (NoSuchFieldException e) {
        // 尝试查找 declared field
        try {
            Field field = clazz.getDeclaredField(fieldName);
            if (Modifier.isStatic(field.getModifiers())) {
                return field;
            }
        } catch (NoSuchFieldException ex) {
            // 忽略
        }
    }
    return null;
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `mvn test -Dtest=ReflectionUtilsTest#testGetStatic* -q`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/java/org/dromara/easytl/runtime/ReflectionUtils.java src/test/java/org/dromara/easytl/runtime/ReflectionUtilsTest.java
git commit -m "feat: add static property access to ReflectionUtils"
```

---

## Task 5: ReflectionUtils Static Method Invocation

**Files:**
- Modify: `src/main/java/org/dromara/easytl/runtime/ReflectionUtils.java`
- Test: `src/test/java/org/dromara/easytl/runtime/ReflectionUtilsTest.java`

- [ ] **Step 1: Write the failing test**

```java
// In ReflectionUtilsTest.java, add:

@Test
void testInvokeStaticMethod() {
    Object result = ReflectionUtils.invokeMethod(Math.class, "sqrt", new Object[]{16.0});
    assertEquals(4.0, result);
}

@Test
void testInvokeStaticMethodNoArgs() {
    Object result = ReflectionUtils.invokeMethod(System.class, "currentTimeMillis", new Object[0]);
    assertNotNull(result);
    assertTrue((Long) result > 0);
}

@Test
void testInvokeStaticMethodNotFound() {
    TemplateRuntimeException ex = assertThrows(TemplateRuntimeException.class, () -> {
        ReflectionUtils.invokeMethod(Math.class, "nonExistentMethod", new Object[0]);
    });
    assertTrue(ex.getMessage().contains("not found"));
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn test -Dtest=ReflectionUtilsTest#testInvokeStaticMethod -q`
Expected: FAIL

- [ ] **Step 3: Write minimal implementation**

```java
// In ReflectionUtils.java, modify invokeMethod() to handle Class<?>:

public static Object invokeMethod(Object obj, String methodName, Object[] args) {
    if (obj == null) {
        throw new TemplateRuntimeException("Cannot invoke method '" + methodName + "' on null");
    }

    // 新增：如果 obj 是 Class<?>，尝试调用静态方法
    if (obj instanceof Class) {
        Class<?> clazz = (Class<?>) obj;
        return invokeStaticMethod(clazz, methodName, args);
    }

    // Special handling for Collection methods to avoid module access issues
    // ... 原有代码保持不变
}

// Add new method after invokeMethod():

/**
 * 调用静态方法
 *
 * @param clazz 类
 * @param methodName 方法名
 * @param args 参数数组
 * @return 方法返回值
 */
private static Object invokeStaticMethod(Class<?> clazz, String methodName, Object[] args) {
    Method method = findStaticMethodCached(clazz, methodName, args);

    if (method == null) {
        throw new TemplateRuntimeException(
            "Static method '" + methodName + "' not found in " + clazz.getName());
    }

    Object[] adaptedArgs = adaptLambdaArguments(method, args);

    try {
        if (!method.isAccessible()) {
            method.setAccessible(true);
        }
        return method.invoke(null, adaptedArgs);
    } catch (Exception e) {
        throw new TemplateRuntimeException(
            "Failed to invoke static method '" + methodName + "': " + e.getMessage(), e);
    }
}

/**
 * 查找静态方法（带缓存）
 */
private static Method findStaticMethodCached(Class<?> clazz, String methodName, Object[] args) {
    StringBuilder cacheKeyBuilder = new StringBuilder();
    cacheKeyBuilder.append("static:").append(clazz.getName()).append("::").append(methodName).append("::");
    for (int i = 0; i < args.length; i++) {
        if (i > 0) {
            cacheKeyBuilder.append(",");
        }
        if (args[i] != null) {
            cacheKeyBuilder.append(args[i].getClass().getName());
        } else {
            cacheKeyBuilder.append("null");
        }
    }
    String cacheKey = cacheKeyBuilder.toString();

    Method cached = methodCache.get(cacheKey);
    if (cached != null) {
        return cached;
    }

    Method method = findStaticMethod(clazz, methodName, args);
    if (method != null) {
        methodCache.put(cacheKey, method);
    }
    return method;
}

/**
 * 查找静态方法
 */
private static Method findStaticMethod(Class<?> clazz, String methodName, Object[] args) {
    Method[] methods = clazz.getMethods();
    Method bestMatch = null;
    int bestScore = Integer.MAX_VALUE;

    for (Method method : methods) {
        if (!method.getName().equals(methodName)) {
            continue;
        }

        if (!Modifier.isStatic(method.getModifiers())) {
            continue;
        }

        Class<?>[] paramTypes = method.getParameterTypes();

        if (paramTypes.length != args.length) {
            if (method.isVarArgs() && args.length >= paramTypes.length - 1) {
                if (bestMatch == null) {
                    bestMatch = method;
                }
            }
            continue;
        }

        int score = calculateMatchScore(paramTypes, args);
        if (score >= 0 && score < bestScore) {
            bestMatch = method;
            bestScore = score;
        }
    }

    return bestMatch;
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `mvn test -Dtest=ReflectionUtilsTest#testInvokeStatic* -q`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/java/org/dromara/easytl/runtime/ReflectionUtils.java src/test/java/org/dromara/easytl/runtime/ReflectionUtilsTest.java
git commit -m "feat: add static method invocation to ReflectionUtils"
```

---

## Task 6: ReflectionUtils createInstance and setProperty

**Files:**
- Modify: `src/main/java/org/dromara/easytl/runtime/ReflectionUtils.java`
- Test: `src/test/java/org/dromara/easytl/runtime/ReflectionUtilsTest.java`

- [ ] **Step 1: Write the failing test**

```java
// In ReflectionUtilsTest.java, add:

@Test
void testCreateInstanceNoArgs() {
    Object instance = ReflectionUtils.createInstance(ArrayList.class, new Object[0]);
    assertNotNull(instance);
    assertTrue(instance instanceof ArrayList);
}

@Test
void testCreateInstanceWithArgs() {
    Object instance = ReflectionUtils.createInstance(String.class, new Object[]{"hello"});
    assertNotNull(instance);
    assertEquals("hello", instance);
}

@Test
void testCreateInstanceNoSuitableConstructor() {
    TemplateRuntimeException ex = assertThrows(TemplateRuntimeException.class, () -> {
        ReflectionUtils.createInstance(Math.class, new Object[0]);
    });
    assertTrue(ex.getMessage().contains("No suitable constructor"));
}

@Test
void testSetPropertyViaSetter() {
    // Use a simple test class
    java.util.List<Object> list = new java.util.ArrayList<>();
    ReflectionUtils.setProperty(list, "size", null); // This won't work, but test setProperty exists
    // Better test with a custom object
}

@Test
void testSetPropertyOnMap() {
    Map<String, Object> map = new java.util.HashMap<>();
    ReflectionUtils.setProperty(map, "key", "value");
    // Maps handle property setting differently
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn test -Dtest=ReflectionUtilsTest#testCreateInstance -q`
Expected: FAIL

- [ ] **Step 3: Write minimal implementation**

```java
// In ReflectionUtils.java, add after the existing methods:

/**
 * 创建类的实例
 *
 * @param clazz 类
 * @param args 构造函数参数
 * @return 新创建的实例
 */
public static Object createInstance(Class<?> clazz, Object[] args) {
    Constructor<?> constructor = findConstructorCached(clazz, args);

    if (constructor == null) {
        throw new TemplateRuntimeException(
            "No suitable constructor found in " + clazz.getName() + " for given arguments");
    }

    try {
        if (!constructor.isAccessible()) {
            constructor.setAccessible(true);
        }
        return constructor.newInstance(args);
    } catch (Exception e) {
        throw new TemplateRuntimeException(
            "Failed to create instance of " + clazz.getName() + ": " + e.getMessage(), e);
    }
}

/**
 * 查找构造函数（带缓存）
 */
private static Constructor<?> findConstructorCached(Class<?> clazz, Object[] args) {
    StringBuilder cacheKeyBuilder = new StringBuilder();
    cacheKeyBuilder.append(clazz.getName()).append("::constructor::");
    for (int i = 0; i < args.length; i++) {
        if (i > 0) {
            cacheKeyBuilder.append(",");
        }
        if (args[i] != null) {
            cacheKeyBuilder.append(args[i].getClass().getName());
        } else {
            cacheKeyBuilder.append("null");
        }
    }
    String cacheKey = cacheKeyBuilder.toString();

    // Use a separate cache for constructors (we can reuse the methodCache pattern)
    Method cached = methodCache.get(cacheKey);
    // For constructors, we'll just search directly without caching for simplicity
    return findConstructor(clazz, args);
}

/**
 * 查找构造函数
 */
private static Constructor<?> findConstructor(Class<?> clazz, Object[] args) {
    Constructor<?>[] constructors = clazz.getConstructors();
    Constructor<?> bestMatch = null;
    int bestScore = Integer.MAX_VALUE;

    for (Constructor<?> constructor : constructors) {
        Class<?>[] paramTypes = constructor.getParameterTypes();

        if (paramTypes.length != args.length) {
            continue;
        }

        int score = calculateMatchScore(paramTypes, args);
        if (score >= 0 && score < bestScore) {
            bestMatch = constructor;
            bestScore = score;
        }
    }

    return bestMatch;
}

/**
 * 设置对象的属性值
 *
 * @param obj 对象
 * @param propertyName 属性名
 * @param value 属性值
 */
public static void setProperty(Object obj, String propertyName, Object value) {
    if (obj == null) {
        throw new TemplateRuntimeException("Cannot set property '" + propertyName + "' of null");
    }

    // 尝试 setter 方法
    Method setter = findSetterCached(obj.getClass(), propertyName, value);
    if (setter != null) {
        try {
            if (!setter.isAccessible()) {
                setter.setAccessible(true);
            }
            setter.invoke(obj, value);
            return;
        } catch (Exception e) {
            throw new TemplateRuntimeException(
                "Failed to invoke setter '" + setter.getName() + "': " + e.getMessage(), e);
        }
    }

    // 尝试直接设置字段
    try {
        Field field = findFieldCached(obj.getClass(), propertyName);
        if (!field.isAccessible()) {
            field.setAccessible(true);
        }
        field.set(obj, value);
    } catch (Exception e) {
        throw new TemplateRuntimeException(
            "Failed to set property '" + propertyName + "' on " + obj.getClass().getName() + ": " + e.getMessage(), e);
    }
}

/**
 * 查找 setter 方法（带缓存）
 */
private static Method findSetterCached(Class<?> clazz, String propertyName, Object value) {
    String cacheKey = clazz.getName() + "::setter::" + propertyName;
    Method cached = getterCache.get(cacheKey); // Reuse getterCache for setters
    if (cached != null) {
        return cached;
    }

    Method setter = findSetter(clazz, propertyName, value);
    if (setter != null) {
        getterCache.put(cacheKey, setter);
    }
    return setter;
}

/**
 * 查找 setter 方法
 */
private static Method findSetter(Class<?> clazz, String propertyName, Object value) {
    String setterName = "set" + capitalize(propertyName);

    // If value is null, try to find any setter with that name
    if (value == null) {
        for (Method method : clazz.getMethods()) {
            if (method.getName().equals(setterName) &&
                method.getParameterCount() == 1) {
                return method;
            }
        }
        return null;
    }

    // Try to find setter with matching parameter type
    Class<?> valueType = value.getClass();
    try {
        return clazz.getMethod(setterName, valueType);
    } catch (NoSuchMethodException e) {
        // Try with supertypes
        for (Method method : clazz.getMethods()) {
            if (method.getName().equals(setterName) &&
                method.getParameterCount() == 1 &&
                method.getParameterTypes()[0].isAssignableFrom(valueType)) {
                return method;
            }
        }
    }

    return null;
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `mvn test -Dtest=ReflectionUtilsTest#testCreateInstance*,testSetProperty* -q`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/java/org/dromara/easytl/runtime/ReflectionUtils.java src/test/java/org/dromara/easytl/runtime/ReflectionUtilsTest.java
git commit -m "feat: add createInstance and setProperty to ReflectionUtils"
```

---

## Task 7: Evaluator visitImportStatement Implementation

**Files:**
- Modify: `src/main/java/org/dromara/easytl/compiler/Evaluator.java`
- Test: `src/test/java/org/dromara/easytl/ImportClassIntegrationTest.java`

- [ ] **Step 1: Write the failing test**

```java
// Create new file: src/test/java/org/dromara/easytl/ImportClassIntegrationTest.java

package org.dromara.easytl;

import org.dromara.easytl.exception.TemplateRuntimeException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ImportClassIntegrationTest {

    private final TemplateEngine engine = new TemplateEngine();

    @Test
    void testImportStatementWithAlias() {
        String template = "{% import java.lang.Math as M %}{M.PI}";
        assertEquals(String.valueOf(Math.PI), engine.compile(template).render(new Context()));
    }

    @Test
    void testImportStatementWithoutAlias() {
        String template = "{% import java.lang.Math %}{Math.PI}";
        assertEquals(String.valueOf(Math.PI), engine.compile(template).render(new Context()));
    }

    @Test
    void testImportClassNotFound() {
        String template = "{% import non.existent.Class %}";
        TemplateRuntimeException ex = assertThrows(TemplateRuntimeException.class, () -> {
            engine.compile(template).render(new Context());
        });
        assertTrue(ex.getMessage().contains("class not found"));
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn test -Dtest=ImportClassIntegrationTest -q`
Expected: FAIL with UnsupportedOperationException

- [ ] **Step 3: Write minimal implementation**

```java
// In Evaluator.java, modify visitImportStatement():

@Override
public Object visitImportStatement(ImportStatement node) {
    String className = node.getClassName();
    String alias = node.getAlias();

    try {
        Class<?> clazz = Class.forName(className);

        // 如果没有指定别名，使用类的简单名称
        if (alias == null) {
            alias = clazz.getSimpleName();
        }

        // 将 Class<?> 存入 Context
        context.put(alias, clazz);

        return null;  // import 语句不返回值
    } catch (ClassNotFoundException e) {
        throw runtimeError("Cannot import class '" + className + "': class not found", node);
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `mvn test -Dtest=ImportClassIntegrationTest#testImportStatement* -q`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/java/org/dromara/easytl/compiler/Evaluator.java src/test/java/org/dromara/easytl/ImportClassIntegrationTest.java
git commit -m "feat: implement visitImportStatement in Evaluator"
```

---

## Task 8: Evaluator visitMethodCall for Constructor Invocation

**Files:**
- Modify: `src/main/java/org/dromara/easytl/compiler/Evaluator.java`
- Test: `src/test/java/org/dromara/easytl/ImportClassIntegrationTest.java`

- [ ] **Step 1: Write the failing test**

```java
// In ImportClassIntegrationTest.java, add:

@Test
void testConstructorInvocationNoArgs() {
    Context ctx = new Context();
    ctx.importClass(java.util.ArrayList.class);

    String template = "{ArrayList().size()}";
    assertEquals("0", engine.compile(template).render(ctx));
}

@Test
void testConstructorInvocationWithArgs() {
    Context ctx = new Context();
    ctx.importClass(String.class);

    String template = "{String('hello')}";
    assertEquals("hello", engine.compile(template).render(ctx));
}

@Test
void testStaticMethodCall() {
    Context ctx = new Context();
    ctx.importClass(Math.class);

    String template = "{Math.sqrt(16)}";
    assertEquals("4.0", engine.compile(template).render(ctx));
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn test -Dtest=ImportClassIntegrationTest#testConstructor* -q`
Expected: FAIL

- [ ] **Step 3: Write minimal implementation**

```java
// In Evaluator.java, modify visitMethodCall():

@Override
public Object visitMethodCall(MethodCall node) {
    // Handle the case where object is null (function-style call like fn())
    if (node.getObject() == null) {
        String varName = node.getMethodName();
        Object varValue = context.get(varName);

        // 新增：如果变量是 Class<?>，调用构造函数
        if (varValue instanceof Class) {
            List<ASTNode> argNodes = node.getArguments();
            Object[] args = new Object[argNodes.size()];
            for (int i = 0; i < argNodes.size(); i++) {
                args[i] = argNodes.get(i).accept(this);
            }
            return ReflectionUtils.createInstance((Class<?>) varValue, args);
        }

        if (varValue instanceof LambdaObject) {
            // Invoke the Lambda directly
            LambdaObject lambda = (LambdaObject) varValue;
            List<ASTNode> argNodes = node.getArguments();
            Object[] args = new Object[argNodes.size()];
            for (int i = 0; i < argNodes.size(); i++) {
                args[i] = argNodes.get(i).accept(this);
            }
            return lambda.invoke(args);
        }

        // Not a LambdaObject, throw error
        throw runtimeError("Cannot call '" + varName + "' as a function", node);
    }

    Object obj = node.getObject().accept(this);

    if (obj == null) {
        throw runtimeError("Cannot call method '" + node.getMethodName() + "' on null", node);
    }

    // 新增：如果 obj 是 Class<?>，调用静态方法
    if (obj instanceof Class) {
        Class<?> clazz = (Class<?>) obj;
        String methodName = node.getMethodName();
        List<ASTNode> argNodes = node.getArguments();
        Object[] args = new Object[argNodes.size()];

        for (int i = 0; i < argNodes.size(); i++) {
            args[i] = argNodes.get(i).accept(this);
        }

        try {
            return ReflectionUtils.invokeMethod(clazz, methodName, args);
        } catch (TemplateRuntimeException e) {
            throw enrichRuntimeException(e, node);
        }
    }

    // Handle LambdaObject as the callee
    if (obj instanceof LambdaObject) {
        LambdaObject lambda = (LambdaObject) obj;
        List<ASTNode> argNodes = node.getArguments();
        Object[] args = new Object[argNodes.size()];
        for (int i = 0; i < argNodes.size(); i++) {
            args[i] = argNodes.get(i).accept(this);
        }
        return lambda.invoke(args);
    }

    String methodName = node.getMethodName();
    List<ASTNode> argNodes = node.getArguments();
    Object[] args = new Object[argNodes.size()];

    for (int i = 0; i < argNodes.size(); i++) {
        args[i] = argNodes.get(i).accept(this);
    }

    try {
        return ReflectionUtils.invokeMethod(obj, methodName, args);
    } catch (TemplateRuntimeException e) {
        throw enrichRuntimeException(e, node);
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `mvn test -Dtest=ImportClassIntegrationTest -q`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/java/org/dromara/easytl/compiler/Evaluator.java src/test/java/org/dromara/easytl/ImportClassIntegrationTest.java
git commit -m "feat: handle Class<?> in visitMethodCall for constructor and static method invocation"
```

---

## Task 9: ObjectConstruction AST Node

**Files:**
- Create: `src/main/java/org/dromara/easytl/ast/ObjectConstruction.java`
- Modify: `src/main/java/org/dromara/easytl/ast/ASTVisitor.java`
- Test: `src/test/java/org/dromara/easytl/ast/ASTNodeTest.java`

- [ ] **Step 1: Write the failing test**

```java
// In ASTNodeTest.java, add:

@Test
void testObjectConstruction() {
    Identifier targetClass = new Identifier("Person");
    List<Map.Entry<ASTNode, ASTNode>> entries = new ArrayList<>();
    entries.add(new AbstractMap.SimpleEntry<>(
        new Identifier("name"), new StringLiteral("Peter")));
    HashLiteral properties = new HashLiteral(entries);

    ObjectConstruction obj = new ObjectConstruction(targetClass, properties);

    assertEquals(targetClass, obj.getTargetClass());
    assertEquals(properties, obj.getPropertiesBlock());
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn test -Dtest=ASTNodeTest#testObjectConstruction -q`
Expected: FAIL

- [ ] **Step 3: Create ObjectConstruction.java**

```java
// Create file: src/main/java/org/dromara/easytl/ast/ObjectConstruction.java

package org.dromara.easytl.ast;

import org.dromara.easytl.parser.Token;

/**
 * 对象构造节点
 * 表示使用构造函数创建对象并设置属性
 * 格式：Person {name: 'Peter'} 或 Person('Peter') {age: 18}
 *
 * @author EasyTL Team
 */
public class ObjectConstruction extends ASTNode {

    /** 目标类（Identifier 或 MethodCall） */
    private final ASTNode targetClass;

    /** 属性设置块 */
    private final HashLiteral propertiesBlock;

    /**
     * 构造对象构造节点
     *
     * @param targetClass 目标类表达式
     * @param propertiesBlock 属性设置块
     */
    public ObjectConstruction(ASTNode targetClass, HashLiteral propertiesBlock) {
        this.targetClass = targetClass;
        this.propertiesBlock = propertiesBlock;
    }

    /**
     * 构造对象构造节点（带位置信息）
     *
     * @param targetClass 目标类表达式
     * @param propertiesBlock 属性设置块
     * @param position 位置信息
     */
    public ObjectConstruction(ASTNode targetClass, HashLiteral propertiesBlock, Position position) {
        super(position);
        this.targetClass = targetClass;
        this.propertiesBlock = propertiesBlock;
    }

    /**
     * 获取目标类表达式
     *
     * @return 目标类表达式
     */
    public ASTNode getTargetClass() {
        return targetClass;
    }

    /**
     * 获取属性设置块
     *
     * @return 属性设置块
     */
    public HashLiteral getPropertiesBlock() {
        return propertiesBlock;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitObjectConstruction(this);
    }

    @Override
    public String toString() {
        return "ObjectConstruction{targetClass=" + targetClass + ", propertiesBlock=" + propertiesBlock + '}';
    }
}
```

- [ ] **Step 4: Add visitObjectConstruction to ASTVisitor.java**

```java
// In ASTVisitor.java, add after visitImportStatement():

T visitObjectConstruction(ObjectConstruction node);
```

- [ ] **Step 5: Run test to verify it passes**

Run: `mvn test -Dtest=ASTNodeTest#testObjectConstruction -q`
Expected: PASS

- [ ] **Step 6: Commit**

```bash
git add src/main/java/org/dromara/easytl/ast/ObjectConstruction.java src/main/java/org/dromara/easytl/ast/ASTVisitor.java src/test/java/org/dromara/easytl/ast/ASTNodeTest.java
git commit -m "feat: add ObjectConstruction AST node"
```

---

## Task 10: Update AST Visitor Implementations

**Files:**
- Modify: `src/test/java/org/dromara/easytl/ast/ASTNodeTest.java` (TestVisitor class)
- Modify: `src/test/java/org/dromara/easytl/ast/LambdaASTTest.java`

- [ ] **Step 1: Add visitObjectConstruction to test visitors**

```java
// In ASTNodeTest.java, find TestVisitor class and add:

@Override
public String visitObjectConstruction(ObjectConstruction node) {
    return "ObjectConstruction";
}

// In LambdaASTTest.java, find the anonymous visitor classes and add to each:

@Override
public String visitObjectConstruction(ObjectConstruction node) {
    return null;
}
```

- [ ] **Step 2: Run tests to verify compilation**

Run: `mvn test -Dtest=ASTNodeTest,LambdaASTTest -q`
Expected: PASS

- [ ] **Step 3: Commit**

```bash
git add src/test/java/org/dromara/easytl/ast/ASTNodeTest.java src/test/java/org/dromara/easytl/ast/LambdaASTTest.java
git commit -m "test: add visitObjectConstruction to test visitors"
```

---

## Task 11: Parser ObjectConstruction Syntax

**Files:**
- Modify: `src/main/java/org/dromara/easytl/parser/Parser.java`
- Test: `src/test/java/org/dromara/easytl/parser/ParserTest.java`

- [ ] **Step 1: Write the failing test**

```java
// In ParserTest.java, add:

@Test
void testObjectConstructionNoArgs() {
    String code = "{Person {name: 'Peter'}}";
    Lexer lexer = new Lexer(new StringSource(code));
    TokenStream tokens = new TokenStream(lexer);
    Parser parser = new Parser(tokens);
    TemplateNode template = parser.parse();

    assertEquals(1, template.getChildren().size());
    SingleBraceExpressionNode expr = (SingleBraceExpressionNode) template.getChildren().get(0);
    ObjectConstruction obj = (ObjectConstruction) expr.getExpression();

    assertTrue(obj.getTargetClass() instanceof Identifier);
    assertEquals("Person", ((Identifier) obj.getTargetClass()).getName());
    assertNotNull(obj.getPropertiesBlock());
}

@Test
void testObjectConstructionWithArgs() {
    String code = "{Person('Peter') {age: 18}}";
    Lexer lexer = new Lexer(new StringSource(code));
    TokenStream tokens = new TokenStream(lexer);
    Parser parser = new Parser(tokens);
    TemplateNode template = parser.parse();

    assertEquals(1, template.getChildren().size());
    SingleBraceExpressionNode expr = (SingleBraceExpressionNode) template.getChildren().get(0);
    ObjectConstruction obj = (ObjectConstruction) expr.getExpression();

    assertTrue(obj.getTargetClass() instanceof MethodCall);
    assertNotNull(obj.getPropertiesBlock());
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn test -Dtest=ParserTest#testObjectConstruction -q`
Expected: FAIL

- [ ] **Step 3: Write minimal implementation**

```java
// In Parser.java, modify parsePostfix() to handle LEFT_BRACE after Identifier/MethodCall:

private ASTNode parsePostfix() {
    ASTNode expr = parsePrimary();

    while (true) {
        if (check(TokenType.DOT)) {
            advance();
            Token name = expect(TokenType.IDENTIFIER);
            Position pos = new Position(expr.getStartLine(), expr.getStartColumn(),
                                       name.getLine(), name.getStartColumn() + name.getValue().length() - 1);
            expr = new MemberAccess(expr, name.getValue(), false, pos);
        } else if (check(TokenType.SAFE_NAV)) {
            advance();
            Token name = expect(TokenType.IDENTIFIER);
            Position pos = new Position(expr.getStartLine(), expr.getStartColumn(),
                                       name.getLine(), name.getStartColumn() + name.getValue().length() - 1);
            expr = new MemberAccess(expr, name.getValue(), true, pos);
        } else if (check(TokenType.LEFT_BRACKET)) {
            Token leftBracket = advance();
            ASTNode index = parseExpression();
            Token rightBracket = expect(TokenType.RIGHT_BRACKET);
            Position pos = new Position(expr.getStartLine(), expr.getStartColumn(),
                                       rightBracket.getLine(), rightBracket.getStartColumn());
            expr = new IndexAccess(expr, index, pos);
        } else if (check(TokenType.LEFT_PAREN)) {
            // 方法调用
            if (expr instanceof Identifier) {
                String methodName = ((Identifier) expr).getName();
                List<ASTNode> args = parseArgumentList();
                Position pos = expr.getPosition();
                expr = new MethodCall(null, methodName, args, false, pos);
            } else if (expr instanceof MemberAccess) {
                MemberAccess access = (MemberAccess) expr;
                List<ASTNode> args = parseArgumentList();
                Position pos = access.getPosition();
                expr = new MethodCall(access.getObject(), access.getMemberName(), args, access.isSafeNavigation(), pos);
            } else {
                 throw new TemplateSyntaxException("Invalid method call target", current().getLine(), current().getStartColumn(), current().getLine(), current().getEndColumn(), sourceText);
            }
        } else if (check(TokenType.LEFT_BRACE)) {
            // 新增：ObjectConstruction 语法
            // 检查 expr 是否是 Identifier 或 MethodCall
            if (isObjectConstructionTarget(expr)) {
                HashLiteral propertiesBlock = parseHashLiteralForObjectConstruction();
                Position pos = new Position(expr.getStartLine(), expr.getStartColumn(),
                                           propertiesBlock.getEndLine(), propertiesBlock.getEndColumn());
                expr = new ObjectConstruction(expr, propertiesBlock, pos);
            } else {
                // 不是 ObjectConstruction，直接返回（让 parsePrimary 处理 HashLiteral）
                break;
            }
        } else {
            break;
        }
    }

    return expr;
}

/**
 * 检查表达式是否可能是 ObjectConstruction 的目标
 */
private boolean isObjectConstructionTarget(ASTNode expr) {
    // Identifier (如 Person) 或 MethodCall (如 Person('Peter'))
    return expr instanceof Identifier || expr instanceof MethodCall;
}

/**
 * 解析 ObjectConstruction 的属性块
 * 注意：这个方法只应该在确定是 ObjectConstruction 时调用
 */
private HashLiteral parseHashLiteralForObjectConstruction() {
    // 复用现有的 parseHashLiteral 逻辑
    return (HashLiteral) parseHashLiteral();
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `mvn test -Dtest=ParserTest#testObjectConstruction -q`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/java/org/dromara/easytl/parser/Parser.java src/test/java/org/dromara/easytl/parser/ParserTest.java
git commit -m "feat: add ObjectConstruction parsing to Parser"
```

---

## Task 12: Evaluator visitObjectConstruction Implementation

**Files:**
- Modify: `src/main/java/org/dromara/easytl/compiler/Evaluator.java`
- Test: `src/test/java/org/dromara/easytl/ImportClassIntegrationTest.java`

- [ ] **Step 1: Write the failing test**

```java
// In ImportClassIntegrationTest.java, add:

// Create a simple test class for object construction
public static class TestPerson {
    private String name;
    private int age;

    public TestPerson() {}

    public TestPerson(String name) {
        this.name = name;
    }

    public void setName(String name) { this.name = name; }
    public void setAge(int age) { this.age = age; }
    public String getName() { return name; }
    public int getAge() { return age; }
}

@Test
void testObjectConstructionWithPropertyBlock() {
    Context ctx = new Context();
    ctx.importClass(TestPerson.class);

    String template = "{TestPerson {name: 'Peter'}.getName()}";
    assertEquals("Peter", engine.compile(template).render(ctx));
}

@Test
void testObjectConstructionWithArgsAndPropertyBlock() {
    Context ctx = new Context();
    ctx.importClass(TestPerson.class);

    String template = "{TestPerson('John') {age: 25}.getAge()}";
    assertEquals("25", engine.compile(template).render(ctx));
}

@Test
void testObjectConstructionWithHashMap() {
    Context ctx = new Context();
    ctx.importClass(java.util.HashMap.class);

    String template = "{HashMap {name: 'Peter', age: 18}.get('name')}";
    assertEquals("Peter", engine.compile(template).render(ctx));
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn test -Dtest=ImportClassIntegrationTest#testObjectConstruction* -q`
Expected: FAIL

- [ ] **Step 3: Write minimal implementation**

```java
// In Evaluator.java, add visitObjectConstruction:

@Override
public Object visitObjectConstruction(ObjectConstruction node) {
    ASTNode targetNode = node.getTargetClass();

    // 获取或创建实例
    Object target;
    if (targetNode instanceof MethodCall) {
        // 有参构造：Person("Peter") { ... }
        target = visitMethodCall((MethodCall) targetNode);
    } else {
        // 无参构造或类引用：Person { ... }
        Object classOrInstance = targetNode.accept(this);
        if (classOrInstance instanceof Class) {
            target = ReflectionUtils.createInstance((Class<?>) classOrInstance, new Object[0]);
        } else {
            target = classOrInstance;  // 可能已经是实例
        }
    }

    if (target == null) {
        throw runtimeError("Cannot create object from null", node);
    }

    // 设置属性
    HashLiteral properties = node.getPropertiesBlock();
    if (properties != null && !properties.isEmpty()) {
        for (Map.Entry<ASTNode, ASTNode> entry : properties.getEntries()) {
            String propName = getPropertyName(entry.getKey());
            Object propValue = entry.getValue().accept(this);

            // 特殊处理：如果目标是 Map，直接 put
            if (target instanceof Map) {
                ((Map) target).put(propName, propValue);
            } else {
                // 普通对象：调用 setter 方法
                try {
                    ReflectionUtils.setProperty(target, propName, propValue);
                } catch (TemplateRuntimeException e) {
                    throw enrichRuntimeException(e, node);
                }
            }
        }
    }

    return target;
}

/**
 * 从 AST 节点获取属性名
 */
private String getPropertyName(ASTNode keyNode) {
    if (keyNode instanceof Identifier) {
        return ((Identifier) keyNode).getName();
    } else if (keyNode instanceof StringLiteral) {
        return ((StringLiteral) keyNode).getValue();
    } else {
        // 对于其他类型，求值后转字符串
        Object key = keyNode.accept(this);
        return key != null ? key.toString() : null;
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `mvn test -Dtest=ImportClassIntegrationTest -q`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/java/org/dromara/easytl/compiler/Evaluator.java src/test/java/org/dromara/easytl/ImportClassIntegrationTest.java
git commit -m "feat: implement visitObjectConstruction in Evaluator"
```

---

## Task 13: Run All Tests

- [ ] **Step 1: Run full test suite**

Run: `mvn test -q`
Expected: All tests PASS

- [ ] **Step 2: Fix any failing tests**

If any tests fail, investigate and fix them.

- [ ] **Step 3: Final commit**

```bash
git add -A
git commit -m "feat: complete importClass implementation"
```

---

## Summary

This plan implements the importClass feature in 13 tasks:

1. Context.importClass methods
2. ImportStatement alias field
3. Parser `as alias` syntax
4. ReflectionUtils static property access
5. ReflectionUtils static method invocation
6. ReflectionUtils createInstance and setProperty
7. Evaluator visitImportStatement
8. Evaluator visitMethodCall for constructor invocation
9. ObjectConstruction AST node
10. Update AST visitor implementations
11. Parser ObjectConstruction syntax
12. Evaluator visitObjectConstruction
13. Full test suite verification