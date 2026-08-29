# Eval 方法添加 Context 参数重载

## 概述

在 `Eval` 和 `EasyTL` 类的 `eval` 系列方法中添加接受 `Context` 参数的重载方法，使表达式求值时能够访问外部变量。

## 动机

当前 `Eval.eval(source)` 系列方法在内部创建空的 `Context`，无法访问外部变量。用户在需要对带有变量引用的表达式进行求值时，无法传入预置的上下文。

例如：
```java
Context ctx = new Context();
ctx.put("name", "张三");
ctx.put("age", 25);

// 当前无法实现这种用法
Integer age = EasyTL.evalAsInteger("age + 1", ctx); // 期望返回 26
```

## 设计

### 原则

- 与 `Template.render(Context)` 方法保持一致：直接使用传入的 Context，不创建子作用域
- 保持向后兼容：现有的无 Context 参数方法保持不变

### 新增方法

#### Eval 类

```java
// 核心方法
public static Object eval(String source, Context ctx)
public static <T> T eval(String source, Context ctx, Class<T> type)

// 便捷方法
public static Integer evalAsInteger(String source, Context ctx)
public static Long evalAsLong(String source, Context ctx)
public static Float evalAsFloat(String source, Context ctx)
public static Double evalAsDouble(String source, Context ctx)
public static BigInteger evalAsBigInteger(String source, Context ctx)
public static BigDecimal evalAsBigDecimal(String source, Context ctx)
public static Boolean evalAsBoolean(String source, Context ctx)
public static String evalAsString(String source, Context ctx)
public static List<?> evalAsList(String source, Context ctx)
public static Map<?, ?> evalAsMap(String source, Context ctx)
public static EtlURL evalAsURL(String source, Context ctx)
```

#### EasyTL 类

同步添加上述 12 个重载方法，委托给 `Eval` 类。

### 实现细节

1. **参数校验**：如果 `ctx` 为 null，抛出 `IllegalArgumentException`

2. **executeStatements 方法修改**：
   - 添加重载版本 `executeStatements(List<ASTNode>, Context, String)`
   - 使用传入的 Context 而非创建新 Context

3. **保持现有方法不变**：无 Context 参数的方法继续在内部创建空 Context

### 使用示例

```java
// 创建上下文并设置变量
Context ctx = new Context();
ctx.put("name", "张三");
ctx.put("age", 25);
ctx.put("price", 99.9);

// 使用 eval 方法
Object result = EasyTL.eval("name", ctx);                    // "张三"
Integer age = EasyTL.evalAsInteger("age + 1", ctx);          // 26
Double total = EasyTL.evalAsDouble("price * 2", ctx);        // 199.8
String greeting = EasyTL.evalAsString("'Hello, ' + name", ctx); // "Hello, 张三"
Boolean isAdult = EasyTL.evalAsBoolean("age >= 18", ctx);    // true

// 使用 Eval 类直接调用
Integer result = Eval.evalAsInteger("age * 2", ctx);         // 50
```

## 影响范围

- `org.dromara.easytl.Eval` - 添加 12 个重载方法
- `org.dromara.easytl.EasyTL` - 添加 12 个重载方法

## 测试计划

1. 测试每个重载方法能正确访问 Context 中的变量
2. 测试 Context 为 null 时抛出 IllegalArgumentException
3. 测试空表达式返回 null
4. 测试类型转换正确性
5. 测试 URL 方法的特殊处理（包装为 url(...) 语法）