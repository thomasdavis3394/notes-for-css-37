---
name: fix-duplicate-location-info-design
description: 设计方案：修复编译错误信息中位置信息重复的问题
type: project
---

# 修复编译错误信息中位置信息重复问题

## 问题描述

当前编译错误信息中存在位置信息重复显示的问题。一个错误消息中位置信息（行号、列号、源码显示）出现多次，导致错误信息冗长且难以阅读。

**问题：** 一个错误信息中位置信息出现了多次，应该只出现 1 次。

## 根本原因

错误信息在异常构造链中被多次格式化：

```
TemplateSyntaxException 构造
  → formatSyntaxError() 添加位置信息块
    → TemplateException 构造  
      → formatMessage() 再次添加位置信息块
        → TemplateEngine.compileInternal 捕获异常
          → 用 e.getMessage() 创建新异常（已双重格式化的消息）
            → TemplateCompileException 构造
              → 又一次添加位置信息块
```

## 设计方案：延迟格式化

**核心原则：** 异常类在构造时存储原始数据（消息、位置、源码），不预格式化消息。格式化仅在 `getMessage()` 时执行一次。

### 设计要点

1. **原始数据存储**
   - 异常类存储原始错误消息（不含位置信息的纯文本）
   - 存储 Position 对象
   - 存储 Source 对象
   - 存储错误类型（如 "Syntax error", "Compile error"）

2. **延迟格式化**
   - `getMessage()` 方法执行格式化
   - 格式化结果缓存，避免重复计算
   - 使用错误类型参数决定格式化前缀

3. **避免双重格式化**
   - 构造函数接收原始消息，不接收已格式化消息
   - 如果需要包装异常，使用 `getRawMessage()` 提取原始消息

### 修改范围

| 类 | 修改内容 |
|---|---|
| `TemplateException` | 添加 rawMessage/errorType 字段；getMessage() 延迟格式化；新增 getRawMessage() |
| `TemplateSyntaxException` | 修改构造函数，传入原始消息和错误类型，不预格式化 |
| `TemplateCompileException` | 构造函数保持不变，父类已处理延迟格式化 |
| `TemplateEngine` | 捕获异常时使用 getRawMessage() 获取原始消息 |
| `ErrorPointerAssert` | 添加验证位置信息只出现1次的检查 |

---

## 详细设计

### 1. TemplateException 基类修改

**新增字段：**
- `rawMessage`: 原始错误消息（不含位置信息）
- `errorType`: 错误类型（如 "Syntax error", "Compile error"）
- `cachedMessage`: 缓存的格式化消息（延迟计算）

**修改构造函数：**

```java
// 带位置和错误类型的构造函数（子类使用）
protected TemplateException(String rawMessage, String errorType,
                            Position position, Source source) {
    this.rawMessage = rawMessage;
    this.errorType = errorType;
    this.position = position;
    this.source = source;
    this.cachedMessage = null;  // 延迟计算
}
```

**新增 getMessage() 延迟格式化：**

```java
@Override
public String getMessage() {
    if (cachedMessage == null) {
        cachedMessage = formatMessage();
    }
    return cachedMessage;
}

private String formatMessage() {
    if (position != null && source != null && errorType != null) {
        return ErrorMessageFormatter.formatMessage(
            errorType, rawMessage, position, source);
    }
    return rawMessage;
}
```

**新增 getRawMessage() 方法：**

```java
public String getRawMessage() {
    return rawMessage;
}
```

**注意：** 不带位置信息的构造函数保持不变，直接设置 `cachedMessage = message`。

---

### 2. TemplateSyntaxException 修改

**主构造函数修改：**

```java
private static final String SYNTAX_ERROR = "Syntax error";

public TemplateSyntaxException(final String message, final Position position, final Source source) {
    super(message, SYNTAX_ERROR, position, source);  // 直接传原始消息
}
```

**其他便捷构造函数保持兼容，调整实现：**
- `TemplateSyntaxException(String message)` - 无位置信息
- `TemplateSyntaxException(String message, Position position)` - 无源码
- `TemplateSyntaxException(String message, int line, int column)` - 单点位置
- `TemplateSyntaxException(String message, int line, int column, String sourceText)` - 单点位置带源码
- `TemplateSyntaxException(String message, int startLine, int startColumn, int endLine, int endColumn, String sourceText)` - 范围位置
- `TemplateSyntaxException(String message, Throwable cause)` - 带原因异常

---

### 3. TemplateCompileException 修改

当前构造函数已正确设计，只需确认传入原始消息：

```java
private static final String COMPILE_ERROR = "Compile error";

public TemplateCompileException(final String message, final Position position, final Source source) {
    super(message, COMPILE_ERROR, position, source);
}
```

其他构造函数（不带位置信息）保持不变。

---

### 4. TemplateEngine.compileInternal 修改

**修改异常捕获逻辑：**

```java
private Template compileInternal(final String template, Map<String, Class<?>> importsMap) {
    try {
        // ... 编译逻辑保持不变
    } catch (TemplateCompileException e) {
        // TemplateCompileException 直接抛出，不重新包装
        throw e;
    } catch (TemplateException e) {
        // 使用 getRawMessage() 获取原始消息，避免重复格式化
        throw new TemplateCompileException(e.getRawMessage(), e.getPosition(), e.getSource());
    } catch (Exception e) {
        throw new TemplateCompileException("Failed to compile template: " + e.getMessage(), e);
    }
}
```

---

### 5. ErrorPointerAssert 测试验证

**新增验证位置信息只出现一次：**

```java
public static void assertErrorPointer(
    TemplateException exception,
    String expectedSourceLine,
    String expectedPointerLine,
    String expectedErrorMessage
) {
    // ... 现有验证逻辑保持不变 ...

    // 8. 验证位置信息只出现一次（新增）
    String actualMessage = exception.getMessage();
    int locationBlockCount = countLocationBlocks(actualMessage);
    if (locationBlockCount != 1) {
        throw new AssertionError(
            "Location info should appear exactly once, but found " + locationBlockCount + " times.\n" +
            "Full error message:\n" + actualMessage
        );
    }

    System.out.println();
    System.out.println(exception.getMessage());
}

/**
 * 统计错误消息中位置信息块的数量
 * 指针行特征：以 "  | " 开头，包含 "^"
 */
private static int countLocationBlocks(String message) {
    String[] lines = message.split("\n");
    int count = 0;
    for (String line : lines) {
        if (line.startsWith("  | ") && line.contains("^")) {
            count++;
        }
    }
    return count;
}
```

---

## 预期结果

修复后的错误消息格式：

```
Syntax error: Unexpected token: %}

  |
1 | {% %}
  |    ^^ Unexpected token: %}
```

位置信息块只出现 1 次，简洁清晰。

---

## Why

用户反馈错误信息中位置信息重复，影响阅读体验。根本原因是异常类构造函数链中多次调用格式化方法，每层都添加新的位置信息块。

---

## How to apply

1. 修改 `TemplateException` 基类，实现延迟格式化
2. 修改 `TemplateSyntaxException` 构造函数，传入原始消息
3. 修改 `TemplateEngine.compileInternal`，使用 `getRawMessage()`
4. 修改 `ErrorPointerAssert`，添加位置信息计数验证
5. 运行所有测试确保不破坏现有功能