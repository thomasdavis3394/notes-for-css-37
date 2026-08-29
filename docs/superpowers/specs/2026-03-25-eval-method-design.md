# EasyTL eval 方法设计

## 概述

为 EasyTL 添加 `eval` 方法族，支持即时编译和执行表达式/语句，返回指定类型的值。

## API 规格

### EasyTL 类新增静态方法

```java
// 通用 eval
public static Object eval(String source);
public static <T> T eval(String source, Class<T> type);

// 类型便捷方法
public static Integer evalAsInteger(String source);
public static Long evalAsLong(String source);
public static Float evalAsFloat(String source);
public static Double evalAsDouble(String source);
public static BigInteger evalAsBigInteger(String source);
public static Boolean evalAsBoolean(String source);
public static String evalAsString(String source);
public static List<?> evalAsList(String source);
public static Map<?, ?> evalAsMap(String source);
public static EtlURL evalAsURL(String source);
```

### 新建 Eval 类

提供与 EasyTL 相同的方法签名，作为完整功能的独立入口。

```java
public final class Eval {
    public static Object eval(String source);
    public static <T> T eval(String source, Class<T> type);
    public static Integer evalAsInteger(String source);
    // ... 其他便捷方法
}
```

## 语法范围

支持语句块内的完整语法，类似 `{% ... %}` 中包含的表达式语句：

- **基本表达式** — 算术运算、比较运算、逻辑运算
- **字面量** — 数字、字符串、布尔、null、列表、Map、范围、正则
- **变量声明** — `let x = value`
- **流程控制** — `if/else`、`for`、`switch`
- **Lambda 表达式** — `x => x + 1`、`(x, y) => x + y`
- **return 语句** — 显式返回值

## 返回值规则

支持两种返回方式：

1. **最后表达式值** — 最后一个表达式的值作为返回值
2. **显式 return** — 通过 `return` 语句显式返回值

优先级：如果执行过程中遇到 `return`，使用其值；否则使用最后表达式值。

示例：

```java
eval("let x = 1; let y = 2; x + y", Integer.class);  // → 3（最后表达式）
eval("for (i in [1,2,3]) { if (i == 2) return i }", Integer.class); // → 2（显式 return）
eval("if (false) { 10 } else { 20 }", Integer.class); // → 20（条件表达式）
```

## 类型转换规则

### 基本规则

| 规则 | 描述 | 示例 |
|------|------|------|
| 精确匹配 | 类型完全一致 | `eval("1", Integer.class)` → Integer 1 |
| 向上转换 | 小类型到大类型，安全 | `eval("1", Long.class)` → Long 1L |
| 向下转换 | 大类型到小类型，可能失败 | `eval("1L", Integer.class)` → Integer 1 或溢出异常 |
| 子类→父类 | 实现类到接口/父类 | `eval("[1,2]", Collection.class)` → List 作为 Collection |
| 父类→子类 | 不支持，抛异常 | `eval("...", ArrayList.class)` → 异常 |
| 任意→String | 特殊处理，调用 toString() | `eval("1 + 2", String.class)` → "3" |
| 任意→Object | 接受任意返回值 | `eval("...", Object.class)` → 原值 |
| 类型不兼容 | 无法转换，抛异常 | `eval("'abc'", Integer.class)` → 异常 |

### 数值类型转换链

**整数链：** `Byte → Short → Integer → Long → BigInteger`

**浮点链：** `Float → Double → BigDecimal`

**整数到浮点：** `Integer → Float`、`Long → Double`、`BigInteger → BigDecimal`

**完整转换矩阵：**

| 源类型 → 目标类型 | Byte | Short | Integer | Long | BigInteger | Float | Double | BigDecimal |
|------------------|------|-------|---------|------|------------|-------|--------|------------|
| Byte             | ✓    | ✓     | ✓       | ✓    | ✓          | ✓     | ✓      | ✓          |
| Short            | ?    | ✓     | ✓       | ✓    | ✓          | ✓     | ✓      | ✓          |
| Integer          | ?    | ?     | ✓       | ✓    | ✓          | ✓     | ✓      | ✓          |
| Long             | ?    | ?     | ?       | ✓    | ✓          | -     | ✓      | ✓          |
| BigInteger       | ?    | ?     | ?       | ?    | ✓          | -     | -      | ✓          |
| Float            | ?    | ?     | ?       | ?    | -          | ✓     | ✓      | ✓          |
| Double           | ?    | ?     | ?       | ?    | -          | ?     | ✓      | ✓          |
| BigDecimal       | ?    | ?     | ?       | ?    | ?          | ?     | ?      | ✓          |

图例：`✓` 安全转换，`?` 可能溢出，`-` 不支持（精度损失）

示例：

```java
eval("1", Long.class);           // Integer 1 → Long 1L ✓
eval("1", BigInteger.class);     // Integer 1 → BigInteger ✓
eval("1.5", Double.class);       // Float 1.5 → Double ✓
eval("1", Float.class);          // Integer 1 → Float 1.0f ✓
eval("1L", Integer.class);       // Long → Integer，值范围内则成功，溢出则异常
eval("123456789012345", Integer.class); // 超出 int 范围，抛异常
```

### 类型转换异常

当类型转换失败时，抛出 `TypeConversionException`（或类似异常），包含：

- 源类型
- 目标类型
- 原始值（可选）
- 失败原因

## evalAsURL 特殊处理

### 透明包装

`evalAsURL` 内部将 source 包装为 `url(...)` 语法，但对用户透明：

```java
evalAsURL("http://baidu.com");   // 内部: eval("url(http://baidu.com)", EtlURL.class)
evalAsURL("/xxx/yyy");           // 内部: eval("url(/xxx/yyy)", EtlURL.class)
```

### 错误位置显示

错误信息显示原始位置，不暴露 `url()` 包装：

```java
evalAsURL("aaa{x}");
// 错误显示:
// aaa{x}
//     ^ 未定义变量 x

// 而不是:
// url(aaa{x})
//        ^ 未定义变量 x
```

实现方式：在包装时记录偏移量，错误报告时调整位置。

## 实现方案

### 架构

```
source → Parser → AST → Evaluator → Object → TypeConverter → T
```

### Parser 集成

#### 问题：如何解析语句块

现有 Parser 的 `parseStatementBlock()` 方法只能解析单个语句（`{% let x = 1 %}`），无法处理多语句如 `let x = 1; let y = 2; x + y`。

现有的 `parseBlockStatement()` 方法（用于 Lambda 块体）支持 `{ }` 包围的多语句，用分号分隔。

#### 解决方案：在 Parser 中新增公开方法

在 `Parser` 中添加 `parseEvalSource()` 方法，复用 `parseBlockStatement()` 的逻辑：

```java
// Parser.java 新增方法
public List<ASTNode> parseEvalSource() {
    List<ASTNode> stmts = new ArrayList<>();
    while (!check(TokenType.EOF)) {
        if (check(TokenType.RETURN)) {
            stmts.add(parseReturnStatement());
        } else {
            stmts.add(parseExpression());
        }
        if (check(TokenType.SEMICOLON)) {
            advance();
        }
    }
    return stmts;
}
```

`Eval.eval()` 实现：

```java
private static List<ASTNode> parseAsStatements(String source) {
    if (source == null || source.trim().isEmpty()) {
        return Collections.emptyList();
    }

    StringSource stringSource = new StringSource(source);
    Lexer lexer = new Lexer(stringSource);
    TokenStream tokenStream = new TokenStream(lexer);
    Parser parser = new Parser(tokenStream, source);

    return parser.parseEvalSource();
}
```

#### 无需位置偏移调整

由于直接使用原始 source，无需包装，错误位置自然正确。

### 新增类

| 类名 | 职责 |
|------|------|
| `Eval` | 独立的表达式求值入口类 |
| `TypeConverter` | 类型转换工具类 |
| `TypeConversionException` | 类型转换异常，继承 `TemplateRuntimeException` |

### 修改类

| 类名 | 变更 |
|------|------|
| `EasyTL` | 新增 eval 方法族，委托给 Eval 类实现 |

### 异常层次结构

`TypeConversionException` 继承 `TemplateRuntimeException`，保持与现有异常体系一致：

```
RuntimeException
  └── TemplateRuntimeException
        ├── TypeConversionException (新增)
        └── ... (其他现有异常)
```

### 实现细节

#### Eval.eval 实现

```java
public static Object eval(String source) {
    // 1. 参数校验
    if (source == null) {
        throw new IllegalArgumentException("Source cannot be null");
    }

    // 2. 空字符串处理
    String trimmed = source.trim();
    if (trimmed.isEmpty()) {
        return null;
    }

    // 3. 解析为语句列表
    List<ASTNode> statements = parseAsStatements(source);

    // 4. 创建空 Context 执行
    Context context = new Context();
    Evaluator evaluator = new Evaluator(context, null, source);

    // 5. 执行并获取返回值（捕获 ReturnException）
    Object result = null;
    try {
        for (ASTNode stmt : statements) {
            result = stmt.accept(evaluator);
        }
    } catch (ReturnException e) {
        result = e.getValue();
    }

    return result;
}

public static <T> T eval(String source, Class<T> type) {
    Object result = eval(source);
    return TypeConverter.convert(result, type);
}
```

#### ReturnException 处理

`return` 语句在 Evaluator 中抛出 `ReturnException`。`Eval.eval()` 需要捕获并提取返回值，行为与 `LambdaObject.invoke()` 一致：

```java
// LambdaObject.invoke() 中的处理方式（现有代码）
try {
    result = body.accept(evaluator);
} catch (ReturnException e) {
    result = e.getValue();
}
```

`Eval.eval()` 采用相同策略：
- 捕获 `ReturnException`
- 提取其值作为返回值
- 如果没有 `return`，使用最后表达式的值
```

#### TypeConverter.convert 实现

```java
public static <T> T convert(Object value, Class<T> targetType) {
    if (value == null) {
        if (targetType.isPrimitive()) {
            throw new TypeConversionException("Cannot convert null to primitive type " + targetType);
        }
        return null;
    }

    Class<?> sourceType = value.getClass();

    // 精确匹配
    if (targetType.isAssignableFrom(sourceType)) {
        return targetType.cast(value);
    }

    // String 特殊处理
    if (targetType == String.class) {
        return targetType.cast(value.toString());
    }

    // 数值类型转换
    if (isNumericConversion(sourceType, targetType)) {
        return convertNumeric(value, targetType);
    }

    // 不支持的转换
    throw new TypeConversionException(
        "Cannot convert " + sourceType.getName() + " to " + targetType.getName()
    );
}

private static <T> T convertNumeric(Object value, Class<T> targetType) {
    Number num = (Number) value;

    // 向上转换（安全）
    if (targetType == Long.class || targetType == long.class) {
        return targetType.cast(num.longValue());
    }
    if (targetType == Float.class || targetType == float.class) {
        return targetType.cast(num.floatValue());
    }
    if (targetType == Double.class || targetType == double.class) {
        return targetType.cast(num.doubleValue());
    }
    if (targetType == BigInteger.class) {
        if (value instanceof BigInteger) {
            return targetType.cast(value);
        }
        if (value instanceof BigDecimal) {
            return targetType.cast(((BigDecimal) value).toBigInteger());
        }
        return targetType.cast(BigInteger.valueOf(num.longValue()));
    }
    if (targetType == BigDecimal.class) {
        if (value instanceof BigDecimal) {
            return targetType.cast(value);
        }
        if (value instanceof BigInteger) {
            return targetType.cast(new BigDecimal((BigInteger) value));
        }
        // 使用 toString 避免精度损失
        if (value instanceof Double || value instanceof Float) {
            return targetType.cast(BigDecimal.valueOf(num.doubleValue()));
        }
        return targetType.cast(BigDecimal.valueOf(num.longValue()));
    }

    // 向下转换（可能失败）
    if (targetType == Integer.class || targetType == int.class) {
        long longVal = num.longValue();
        if (longVal < Integer.MIN_VALUE || longVal > Integer.MAX_VALUE) {
            throw new TypeConversionException(
                "Value " + longVal + " out of range for Integer"
            );
        }
        return targetType.cast((int) longVal);
    }
    if (targetType == Short.class || targetType == short.class) {
        long longVal = num.longValue();
        if (longVal < Short.MIN_VALUE || longVal > Short.MAX_VALUE) {
            throw new TypeConversionException(
                "Value " + longVal + " out of range for Short"
            );
        }
        return targetType.cast((short) longVal);
    }
    if (targetType == Byte.class || targetType == byte.class) {
        long longVal = num.longValue();
        if (longVal < Byte.MIN_VALUE || longVal > Byte.MAX_VALUE) {
            throw new TypeConversionException(
                "Value " + longVal + " out of range for Byte"
            );
        }
        return targetType.cast((byte) longVal);
    }

    throw new TypeConversionException(
        "Unsupported numeric conversion to " + targetType.getName()
    );
}
```

#### evalAsURL 实现

```java
private static final int URL_WRAPPER_OFFSET = 4; // "url(".length()

public static EtlURL evalAsURL(String source) {
    if (source == null) {
        throw new IllegalArgumentException("Source cannot be null");
    }

    // 包装为 url() 语法
    String wrappedSource = "url(" + source + ")";

    try {
        return eval(wrappedSource, EtlURL.class);
    } catch (TemplateRuntimeException e) {
        // 调整错误位置（减去 "url(" 的偏移量）
        throw adjustErrorPosition(e, -URL_WRAPPER_OFFSET);
    }
}

/**
 * 调整异常中的位置信息
 */
private static TemplateRuntimeException adjustErrorPosition(
        TemplateRuntimeException e, int columnOffset) {
    Position pos = e.getPosition();
    if (pos == null || pos.isUnknown()) {
        return e;
    }

    // Position 使用 startLine/startColumn/endLine/endColumn
    int newStartColumn = Math.max(1, pos.getStartColumn() + columnOffset);
    int newEndColumn = Math.max(1, pos.getEndColumn() + columnOffset);

    // 创建新位置
    Position newPos = new Position(
        pos.getStartLine(), newStartColumn,
        pos.getEndLine(), newEndColumn
    );

    // 创建新异常
    return new TemplateRuntimeException(e.getMessage(), newPos, e.getSource());
}
```

### 错误处理

#### 参数校验

| 场景 | 异常类型 | 消息 |
|------|----------|------|
| source 为 null | `IllegalArgumentException` | "Source cannot be null" |
| source 为空字符串 | 正常处理 | 返回 null 或空值 |

#### 语法错误

使用现有 `TemplateCompileException`，位置信息已通过包装偏移量调整。

#### 运行时错误

使用现有 `TemplateRuntimeException`，包括：
- 未定义变量错误
- 类型错误
- 空指针错误

#### 类型转换错误

使用新建 `TypeConversionException`（继承 `TemplateRuntimeException`）：

| 场景 | 消息示例 |
|------|----------|
| 类型不兼容 | "Cannot convert String to Integer" |
| 数值溢出 | "Value 123456789012345 out of range for Integer" |
| null 转基本类型 | "Cannot convert null to primitive type int" |
```

## 测试用例

### 基本表达式

```java
assertEquals(3, EasyTL.eval("1 + 2", Integer.class));
assertEquals(3.0, EasyTL.eval("1.5 + 1.5", Double.class));
assertEquals("hello", EasyTL.eval("'hello'", String.class));
assertEquals(true, EasyTL.eval("1 < 2", Boolean.class));
```

### 变量和语句

```java
assertEquals(3, EasyTL.eval("let x = 1; let y = 2; x + y", Integer.class));
assertEquals(6, EasyTL.eval("let sum = 0; for (i in [1,2,3]) { sum = sum + i } sum", Integer.class));
```

### 流程控制

```java
assertEquals(10, EasyTL.eval("if (true) { 10 } else { 20 }", Integer.class));
assertEquals(20, EasyTL.eval("if (false) { 10 } else { 20 }", Integer.class));
```

### return 语句

```java
assertEquals(2, EasyTL.eval("for (i in [1,2,3]) { if (i == 2) return i }", Integer.class));
assertEquals(5, EasyTL.eval("if (true) { return 5 } 10", Integer.class));
```

### 类型转换

```java
assertEquals(Long.valueOf(1), EasyTL.eval("1", Long.class));
assertEquals(Double.valueOf(1.5), EasyTL.eval("1.5", Double.class));
assertEquals("3", EasyTL.eval("1 + 2", String.class));
assertThrows(TypeConversionException.class, () -> EasyTL.eval("'abc'", Integer.class));
```

### 数值类型转换边界

```java
// 向上转换（安全）
assertEquals(Short.valueOf((short)1), EasyTL.eval("(byte)1", Short.class));
assertEquals(Long.valueOf(1), EasyTL.eval("1", Long.class));
assertEquals(BigInteger.ONE, EasyTL.eval("1", BigInteger.class));

// 向下转换（值范围内）
assertEquals(Integer.valueOf(1), EasyTL.eval("1L", Integer.class));
assertEquals(Short.valueOf((short)1), EasyTL.eval("1", Short.class));

// 向下转换（溢出）
assertThrows(TypeConversionException.class,
    () -> EasyTL.eval("123456789012345", Integer.class));
assertThrows(TypeConversionException.class,
    () -> EasyTL.eval("99999", Byte.class));
```

### 便捷方法

```java
assertEquals(Integer.valueOf(2), EasyTL.evalAsInteger("4 / 2"));
assertEquals(Long.valueOf(1), EasyTL.evalAsLong("1"));
assertEquals(Float.valueOf(1.5f), EasyTL.evalAsFloat("1.5"));
assertEquals("abc", EasyTL.evalAsString("`abc`"));
assertEquals(Arrays.asList(1, 2, 3), EasyTL.evalAsList("[1, 2, 3]"));
```

### evalAsURL

```java
EtlURL url = EasyTL.evalAsURL("http://baidu.com");
assertEquals("http", url.getScheme());
assertEquals("baidu.com", url.getHost());
```

### 错误位置

```java
try {
    EasyTL.evalAsURL("aaa{x}");
} catch (TemplateRuntimeException e) {
    // 验证错误位置指向 {x} 而不是 url(aaa{x})
    assertTrue(e.getMessage().contains("aaa{x}"));
    assertFalse(e.getMessage().contains("url("));
}
```

### Lambda 表达式

```java
assertEquals(3, EasyTL.eval("((x) => x + 1)(2)", Integer.class));
assertEquals(6, EasyTL.eval("let f = (x, y) => x * y; f(2, 3)", Integer.class));
```

### 参数校验

```java
assertThrows(IllegalArgumentException.class, () -> EasyTL.eval(null));
assertThrows(IllegalArgumentException.class, () -> EasyTL.eval(null, Integer.class));
assertThrows(IllegalArgumentException.class, () -> EasyTL.evalAsURL(null));
```

### 空字符串处理

```java
assertNull(EasyTL.eval(""));
assertNull(EasyTL.eval("", Object.class));
```

### 未定义变量

```java
assertThrows(TemplateRuntimeException.class, () -> EasyTL.eval("undefinedVar", Object.class));
```

### 语法错误

```java
assertThrows(TemplateCompileException.class, () -> EasyTL.eval("let x = ", Object.class));
```
```

## 文件变更清单

| 文件 | 操作 | 描述 |
|------|------|------|
| `src/main/java/org/dromara/easytl/Eval.java` | 新建 | 表达式求值入口类 |
| `src/main/java/org/dromara/easytl/TypeConverter.java` | 新建 | 类型转换工具类 |
| `src/main/java/org/dromara/easytl/exception/TypeConversionException.java` | 新建 | 类型转换异常类，继承 TemplateRuntimeException |
| `src/main/java/org/dromara/easytl/EasyTL.java` | 修改 | 新增 eval 方法族，委托给 Eval |
| `src/main/java/org/dromara/easytl/parser/Parser.java` | 修改 | 新增 parseEvalSource() 方法 |
| `src/test/java/org/dromara/easytl/EvalTest.java` | 新建 | 单元测试 |

## 依赖关系

```
EasyTL.eval() → Eval.eval() → Parser → AST → Evaluator → TypeConverter
                     ↓
              ReturnException (catch)
```

## 风险与缓解

| 风险 | 影响 | 缓解措施 |
|------|------|----------|
| Parser 包装方案可能影响复杂语法 | 错误位置偏移不准确 | 充分测试多行、嵌套场景 |
| ReturnException 未被捕获 | return 语句失效 | 确保 catch 在正确位置 |
| 数值溢出转换 | 数据丢失 | 明确抛异常，不静默截断 |