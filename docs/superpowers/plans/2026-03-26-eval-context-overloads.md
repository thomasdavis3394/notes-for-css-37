# Eval Context 参数重载实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 为 Eval 和 EasyTL 类添加接受 Context 参数的 eval 系列重载方法

**Architecture:** 在 Eval 类内部添加 executeStatements 重载方法接受 Context 参数，然后添加 12 个公开的重载方法，最后在 EasyTL 类添加对应的委托方法

**Tech Stack:** Java 8, JUnit 5

---

## 文件结构

| 文件 | 操作 | 说明 |
|------|------|------|
| `src/main/java/org/dromara/easytl/Eval.java` | 修改 | 添加 executeStatements 重载 + 12 个公开方法 |
| `src/main/java/org/dromara/easytl/EasyTL.java` | 修改 | 添加 12 个委托方法 |
| `src/test/java/org/dromara/easytl/EvalTest.java` | 修改 | 添加 Context 参数方法的测试 |
| `src/test/java/org/dromara/easytl/EasyTLTest.java` | 修改 | 添加 EasyTL.evalWithContext 方法的测试 |

---

### Task 1: Eval 类 - 添加 executeStatements 重载方法

**Files:**
- Modify: `src/main/java/org/dromara/easytl/Eval.java:282-300`

- [ ] **Step 1: 编写 executeStatements 重载方法**

在现有 `executeStatements(List<ASTNode>, String)` 方法后添加：

```java
/**
 * 执行语句列表并返回结果（使用指定上下文）
 * <p>
 * 使用传入的 Context 执行语句，捕获 ReturnException 获取返回值，
 * 如果没有 ReturnException，返回最后一个表达式的值。
 * </p>
 *
 * @param statements 语句列表
 * @param context 上下文对象
 * @param sourceText 源码文本（用于错误报告）
 * @return 执行结果
 */
static Object executeStatements(List<ASTNode> statements, Context context, String sourceText) {
    if (statements.isEmpty()) {
        return null;
    }

    Evaluator evaluator = new Evaluator(context, null, sourceText);

    Object result = null;
    try {
        for (ASTNode statement : statements) {
            result = statement.accept(evaluator);
        }
    } catch (ReturnException e) {
        return e.getValue();
    }

    return result;
}
```

- [ ] **Step 2: 验证编译**

Run: `mvn compile -q`
Expected: BUILD SUCCESS

---

### Task 2: Eval 类 - 添加核心 eval 重载方法

**Files:**
- Modify: `src/main/java/org/dromara/easytl/Eval.java:76-111`

- [ ] **Step 1: 添加 eval(String, Context) 方法**

在现有 `eval(String)` 方法后添加：

```java
/**
 * 使用指定上下文执行表达式并返回结果
 *
 * @param source 表达式源码
 * @param context 上下文对象，包含变量
 * @return 表达式执行结果
 * @throws IllegalArgumentException 如果 source 或 context 为 null
 * @throws TemplateRuntimeException 如果表达式执行出错
 */
public static Object eval(String source, Context context) {
    return eval(source, context, Object.class);
}
```

- [ ] **Step 2: 添加 eval(String, Context, Class) 方法**

在刚刚添加的方法后添加：

```java
/**
 * 使用指定上下文执行表达式并将结果转换为指定类型
 *
 * @param source 表达式源码
 * @param context 上下文对象，包含变量
 * @param type 目标类型
 * @param <T> 目标类型泛型
 * @return 转换后的结果
 * @throws IllegalArgumentException 如果 source 或 context 为 null
 * @throws TypeConversionException 如果类型转换失败
 * @throws TemplateRuntimeException 如果表达式执行出错
 */
public static <T> T eval(String source, Context context, Class<T> type) {
    if (source == null) {
        throw new IllegalArgumentException("Source cannot be null");
    }

    if (context == null) {
        throw new IllegalArgumentException("Context cannot be null");
    }

    if (type == null) {
        throw new IllegalArgumentException("Type cannot be null");
    }

    // 空白源码返回 null
    if (source.trim().isEmpty()) {
        return null;
    }

    // 解析并执行（使用传入的 context）
    List<ASTNode> statements = parseAsStatements(source);
    Object result = executeStatements(statements, context, source);

    // 类型转换
    return convertResult(result, type);
}
```

- [ ] **Step 3: 验证编译**

Run: `mvn compile -q`
Expected: BUILD SUCCESS

---

### Task 3: Eval 类 - 添加便捷方法重载

**Files:**
- Modify: `src/main/java/org/dromara/easytl/Eval.java:113-253`

- [ ] **Step 1: 添加数值类型便捷方法**

在现有便捷方法区域末尾添加：

```java
// ==================== Convenience Methods with Context ====================

/**
 * 使用指定上下文执行表达式并返回 Integer
 *
 * @param source 表达式源码
 * @param context 上下文对象
 * @return Integer 结果
 */
public static Integer evalAsInteger(String source, Context context) {
    return eval(source, context, Integer.class);
}

/**
 * 使用指定上下文执行表达式并返回 Long
 *
 * @param source 表达式源码
 * @param context 上下文对象
 * @return Long 结果
 */
public static Long evalAsLong(String source, Context context) {
    return eval(source, context, Long.class);
}

/**
 * 使用指定上下文执行表达式并返回 Float
 *
 * @param source 表达式源码
 * @param context 上下文对象
 * @return Float 结果
 */
public static Float evalAsFloat(String source, Context context) {
    return eval(source, context, Float.class);
}

/**
 * 使用指定上下文执行表达式并返回 Double
 *
 * @param source 表达式源码
 * @param context 上下文对象
 * @return Double 结果
 */
public static Double evalAsDouble(String source, Context context) {
    return eval(source, context, Double.class);
}

/**
 * 使用指定上下文执行表达式并返回 BigInteger
 *
 * @param source 表达式源码
 * @param context 上下文对象
 * @return BigInteger 结果
 */
public static BigInteger evalAsBigInteger(String source, Context context) {
    Object result = eval(source, context);
    if (result == null) {
        return null;
    }
    return toBigInteger(result);
}

/**
 * 使用指定上下文执行表达式并返回 BigDecimal
 *
 * @param source 表达式源码
 * @param context 上下文对象
 * @return BigDecimal 结果
 */
public static BigDecimal evalAsBigDecimal(String source, Context context) {
    Object result = eval(source, context);
    if (result == null) {
        return null;
    }
    return toBigDecimal(result);
}

/**
 * 使用指定上下文执行表达式并返回 Boolean
 *
 * @param source 表达式源码
 * @param context 上下文对象
 * @return Boolean 结果
 */
public static Boolean evalAsBoolean(String source, Context context) {
    return eval(source, context, Boolean.class);
}

/**
 * 使用指定上下文执行表达式并返回 String
 *
 * @param source 表达式源码
 * @param context 上下文对象
 * @return String 结果
 */
public static String evalAsString(String source, Context context) {
    return eval(source, context, String.class);
}

/**
 * 使用指定上下文执行表达式并返回 List
 *
 * @param source 表达式源码
 * @param context 上下文对象
 * @return List 结果
 */
@SuppressWarnings("unchecked")
public static List<?> evalAsList(String source, Context context) {
    return eval(source, context, List.class);
}

/**
 * 使用指定上下文执行表达式并返回 Map
 *
 * @param source 表达式源码
 * @param context 上下文对象
 * @return Map 结果
 */
@SuppressWarnings("unchecked")
public static Map<?, ?> evalAsMap(String source, Context context) {
    return eval(source, context, Map.class);
}

/**
 * 使用指定上下文执行表达式并返回 EtlURL
 * <p>
 * 此方法会将源码包装为 url(...) 语法，并调整错误位置。
 * </p>
 *
 * @param source URL 源码
 * @param context 上下文对象
 * @return EtlURL 结果
 */
public static EtlURL evalAsURL(String source, Context context) {
    if (source == null) {
        throw new IllegalArgumentException("Source cannot be null");
    }

    if (context == null) {
        throw new IllegalArgumentException("Context cannot be null");
    }

    if (source.trim().isEmpty()) {
        return null;
    }

    // 包装为 url(...) 语法
    String wrappedSource = "url(" + source + ")";

    try {
        Object result = eval(wrappedSource, context, EtlURL.class);
        return (EtlURL) result;
    } catch (TemplateRuntimeException e) {
        // 调整错误位置
        throw adjustErrorPosition(e, URL_PREFIX_LENGTH);
    }
}
```

- [ ] **Step 2: 验证编译**

Run: `mvn compile -q`
Expected: BUILD SUCCESS

---

### Task 4: EasyTL 类 - 添加 eval 重载方法

**Files:**
- Modify: `src/main/java/org/dromara/easytl/EasyTL.java:272-377`

- [ ] **Step 1: 添加 eval 重载方法**

在现有 eval 方法区域末尾添加：

```java
// ==================== 表达式求值（带 Context） ====================

/**
 * 使用指定上下文执行表达式并返回结果
 *
 * @param source 源码
 * @param context 上下文对象
 * @return 执行结果，可能为 null
 * @throws IllegalArgumentException 如果 source 或 context 为 null
 */
public static Object eval(String source, Context context) {
    return Eval.eval(source, context);
}

/**
 * 使用指定上下文执行表达式并转换为指定类型
 *
 * @param source 源码
 * @param context 上下文对象
 * @param type 目标类型
 * @param <T> 类型泛型
 * @return 转换后的结果
 * @throws IllegalArgumentException 如果 source 或 context 为 null
 * @throws TypeConversionException 如果类型转换失败
 */
public static <T> T eval(String source, Context context, Class<T> type) {
    return Eval.eval(source, context, type);
}

/**
 * 使用指定上下文执行表达式并返回 Integer
 */
public static Integer evalAsInteger(String source, Context context) {
    return Eval.evalAsInteger(source, context);
}

/**
 * 使用指定上下文执行表达式并返回 Long
 */
public static Long evalAsLong(String source, Context context) {
    return Eval.evalAsLong(source, context);
}

/**
 * 使用指定上下文执行表达式并返回 Float
 */
public static Float evalAsFloat(String source, Context context) {
    return Eval.evalAsFloat(source, context);
}

/**
 * 使用指定上下文执行表达式并返回 Double
 */
public static Double evalAsDouble(String source, Context context) {
    return Eval.evalAsDouble(source, context);
}

/**
 * 使用指定上下文执行表达式并返回 BigInteger
 */
public static java.math.BigInteger evalAsBigInteger(String source, Context context) {
    return Eval.evalAsBigInteger(source, context);
}

/**
 * 使用指定上下文执行表达式并返回 BigDecimal
 */
public static java.math.BigDecimal evalAsBigDecimal(String source, Context context) {
    return Eval.evalAsBigDecimal(source, context);
}

/**
 * 使用指定上下文执行表达式并返回 Boolean
 */
public static Boolean evalAsBoolean(String source, Context context) {
    return Eval.evalAsBoolean(source, context);
}

/**
 * 使用指定上下文执行表达式并返回 String
 */
public static String evalAsString(String source, Context context) {
    return Eval.evalAsString(source, context);
}

/**
 * 使用指定上下文执行表达式并返回 List
 */
public static java.util.List<?> evalAsList(String source, Context context) {
    return Eval.evalAsList(source, context);
}

/**
 * 使用指定上下文执行表达式并返回 Map
 */
public static java.util.Map<?, ?> evalAsMap(String source, Context context) {
    return Eval.evalAsMap(source, context);
}

/**
 * 使用指定上下文执行表达式并返回 EtlURL
 */
public static org.dromara.easytl.runtime.EtlURL evalAsURL(String source, Context context) {
    return Eval.evalAsURL(source, context);
}
```

- [ ] **Step 2: 验证编译**

Run: `mvn compile -q`
Expected: BUILD SUCCESS

---

### Task 5: 添加 EvalTest 测试

**Files:**
- Modify: `src/test/java/org/dromara/easytl/EvalTest.java`

- [ ] **Step 1: 添加 Context 参数方法的测试**

在文件末尾 `}` 之前添加：

```java
// ==================== Context Parameter Tests ====================

@Test
void testEvalWithContext_IntegerVariable() {
    Context ctx = new Context();
    ctx.put("age", 25);
    Integer result = Eval.eval("age + 1", ctx, Integer.class);
    assertEquals(Integer.valueOf(26), result);
}

@Test
void testEvalWithContext_StringVariable() {
    Context ctx = new Context();
    ctx.put("name", "张三");
    String result = Eval.eval("name", ctx, String.class);
    assertEquals("张三", result);
}

@Test
void testEvalWithContext_MultipleVariables() {
    Context ctx = new Context();
    ctx.put("a", 10);
    ctx.put("b", 20);
    Integer result = Eval.eval("a + b", ctx, Integer.class);
    assertEquals(Integer.valueOf(30), result);
}

@Test
void testEvalWithContext_NullContext_ThrowsException() {
    assertThrows(IllegalArgumentException.class, () -> {
        Eval.eval("1 + 1", (Context) null);
    });
}

@Test
void testEvalWithContext_NullContextWithType_ThrowsException() {
    assertThrows(IllegalArgumentException.class, () -> {
        Eval.eval("1 + 1", (Context) null, Integer.class);
    });
}

@Test
void testEvalWithContext_EmptySource_ReturnsNull() {
    Context ctx = new Context();
    Object result = Eval.eval("", ctx);
    assertNull(result);
}

@Test
void testEvalAsIntegerWithContext() {
    Context ctx = new Context();
    ctx.put("x", 5);
    Integer result = Eval.evalAsInteger("x * 2", ctx);
    assertEquals(Integer.valueOf(10), result);
}

@Test
void testEvalAsLongWithContext() {
    Context ctx = new Context();
    ctx.put("value", 100L);
    Long result = Eval.evalAsLong("value + 50", ctx);
    assertEquals(Long.valueOf(150L), result);
}

@Test
void testEvalAsFloatWithContext() {
    Context ctx = new Context();
    ctx.put("price", 1.5f);
    Float result = Eval.evalAsFloat("price * 2", ctx);
    assertEquals(Float.valueOf(3.0f), result, 0.001f);
}

@Test
void testEvalAsDoubleWithContext() {
    Context ctx = new Context();
    ctx.put("rate", 1.5);
    Double result = Eval.evalAsDouble("rate * 2", ctx);
    assertEquals(Double.valueOf(3.0), result);
}

@Test
void testEvalAsBigIntegerWithContext() {
    Context ctx = new Context();
    ctx.put("big", BigInteger.valueOf(1000));
    BigInteger result = Eval.evalAsBigInteger("big + 1", ctx);
    assertEquals(BigInteger.valueOf(1001), result);
}

@Test
void testEvalAsBigDecimalWithContext() {
    Context ctx = new Context();
    ctx.put("amount", new BigDecimal("99.99"));
    BigDecimal result = Eval.evalAsBigDecimal("amount * 2", ctx);
    assertEquals(new BigDecimal("199.98"), result);
}

@Test
void testEvalAsBooleanWithContext() {
    Context ctx = new Context();
    ctx.put("age", 25);
    Boolean result = Eval.evalAsBoolean("age >= 18", ctx);
    assertTrue(result);
}

@Test
void testEvalAsStringWithContext() {
    Context ctx = new Context();
    ctx.put("name", "World");
    String result = Eval.evalAsString("'Hello, ' + name", ctx);
    assertEquals("Hello, World", result);
}

@Test
void testEvalAsListWithContext() {
    Context ctx = new Context();
    ctx.put("items", java.util.Arrays.asList(1, 2, 3));
    List<?> result = Eval.evalAsList("items", ctx);
    assertEquals(3, result.size());
    assertEquals(1, result.get(0));
}

@Test
void testEvalAsMapWithContext() {
    Context ctx = new Context();
    Map<String, Integer> data = new java.util.HashMap<>();
    data.put("a", 1);
    data.put("b", 2);
    ctx.put("data", data);
    Map<?, ?> result = Eval.evalAsMap("data", ctx);
    assertEquals(1, result.get("a"));
    assertEquals(2, result.get("b"));
}

@Test
void testEvalAsURLWithContext() {
    Context ctx = new Context();
    ctx.put("host", "example.com");
    EtlURL url = Eval.evalAsURL("http://${host}/path", ctx);
    assertEquals("http://example.com/path", url.toString());
}

@Test
void testEvalAsURLWithContext_NullContext_ThrowsException() {
    assertThrows(IllegalArgumentException.class, () -> {
        Eval.evalAsURL("http://example.com", (Context) null);
    });
}
```

- [ ] **Step 2: 运行测试验证**

Run: `mvn test -Dtest=EvalTest -q`
Expected: Tests run: X, Failures: 0, Errors: 0

---

### Task 6: 添加 EasyTLTest 测试

**Files:**
- Modify: `src/test/java/org/dromara/easytl/EasyTLTest.java`

- [ ] **Step 1: 添加 EasyTL.evalWithContext 方法的测试**

在文件末尾 `}` 之前添加：

```java
// ==================== eval with Context 测试 ====================

@Test
void testEvalWithContext_IntegerResult() {
    Context ctx = new Context();
    ctx.put("x", 10);
    Integer result = EasyTL.eval("x + 5", ctx, Integer.class);
    assertEquals(Integer.valueOf(15), result);
}

@Test
void testEvalWithContext_ObjectResult() {
    Context ctx = new Context();
    ctx.put("name", "张三");
    Object result = EasyTL.eval("name", ctx);
    assertEquals("张三", result);
}

@Test
void testEvalWithContext_NullContext_ThrowsException() {
    assertThrows(IllegalArgumentException.class, () -> {
        EasyTL.eval("1 + 1", (Context) null);
    });
}

@Test
void testEvalAsIntegerWithContext() {
    Context ctx = new Context();
    ctx.put("count", 5);
    Integer result = EasyTL.evalAsInteger("count * 2", ctx);
    assertEquals(Integer.valueOf(10), result);
}

@Test
void testEvalAsLongWithContext() {
    Context ctx = new Context();
    ctx.put("value", 100L);
    Long result = EasyTL.evalAsLong("value + 50", ctx);
    assertEquals(Long.valueOf(150L), result);
}

@Test
void testEvalAsFloatWithContext() {
    Context ctx = new Context();
    ctx.put("price", 1.5f);
    Float result = EasyTL.evalAsFloat("price * 2", ctx);
    assertEquals(Float.valueOf(3.0f), result, 0.001f);
}

@Test
void testEvalAsDoubleWithContext() {
    Context ctx = new Context();
    ctx.put("rate", 1.5);
    Double result = EasyTL.evalAsDouble("rate * 2", ctx);
    assertEquals(Double.valueOf(3.0), result);
}

@Test
void testEvalAsBooleanWithContext() {
    Context ctx = new Context();
    ctx.put("age", 25);
    Boolean result = EasyTL.evalAsBoolean("age >= 18", ctx);
    assertTrue(result);
}

@Test
void testEvalAsStringWithContext() {
    Context ctx = new Context();
    ctx.put("name", "World");
    String result = EasyTL.evalAsString("'Hello, ' + name", ctx);
    assertEquals("Hello, World", result);
}

@Test
void testEvalAsListWithContext() {
    Context ctx = new Context();
    ctx.put("items", java.util.Arrays.asList(1, 2, 3));
    java.util.List<?> result = EasyTL.evalAsList("items", ctx);
    assertEquals(3, result.size());
}

@Test
void testEvalAsMapWithContext() {
    Context ctx = new Context();
    Map<String, Object> data = new HashMap<>();
    data.put("key", "value");
    ctx.put("data", data);
    Map<?, ?> result = EasyTL.evalAsMap("data", ctx);
    assertEquals("value", result.get("key"));
}

@Test
void testEvalAsURLWithContext() {
    Context ctx = new Context();
    ctx.put("host", "example.com");
    org.dromara.easytl.runtime.EtlURL url = EasyTL.evalAsURL("http://${host}", ctx);
    assertEquals("http://example.com", url.toString());
}
```

- [ ] **Step 2: 运行测试验证**

Run: `mvn test -Dtest=EasyTLTest -q`
Expected: Tests run: X, Failures: 0, Errors: 0

---

### Task 7: 运行完整测试并提交

**Files:**
- All modified files

- [ ] **Step 1: 运行全部测试**

Run: `mvn test -q`
Expected: Tests run: X, Failures: 0, Errors: 0

- [ ] **Step 2: 提交代码**

```bash
git add src/main/java/org/dromara/easytl/Eval.java \
        src/main/java/org/dromara/easytl/EasyTL.java \
        src/test/java/org/dromara/easytl/EvalTest.java \
        src/test/java/org/dromara/easytl/EasyTLTest.java

git commit -m "$(cat <<'EOF'
feat: add Context parameter overloads to eval methods

- Add executeStatements overload that accepts Context parameter
- Add 12 eval method overloads in Eval class
- Add 12 delegate methods in EasyTL class
- Add comprehensive tests for all new methods
EOF
)"
```

---

## 验收标准

- [ ] 所有新增方法编译通过
- [ ] 所有测试通过
- [ ] 代码已提交