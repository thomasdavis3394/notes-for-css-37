# 错误指针位置严格验证设计

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**需求：** 在所有校验错误的测试用例中，严格检测校验错误的代码定位信息，对错误定位符 `^` 的位置和长度做严格匹配和校验。

**当前状态：** 测试只检查 `^` 是否存在和位置范围，未严格验证指针长度和格式。

---

## 解决方案

### 1. 创建 ErrorPointerAssert 工具类

**文件位置：** `src/test/java/org/dromara/easytl/testutil/ErrorPointerAssert.java`

**核心方法：**

```java
/**
 * 验证错误指针位置和消息
 *
 * 示例：
 * assertErrorPointer(exception,
 *     "Hello {user.name}!",    // 源码行
 *     "            ^^^^",      // 指针标记
 *     "Cannot access property 'name' of null"  // 错误消息
 * );
 */
public static void assertErrorPointer(
    TemplateException exception,
    String expectedSourceLine,
    String expectedPointerLine,
    String expectedErrorMessage
);
```

### 2. 验证逻辑

1. 检查异常有 `sourceText`，否则报错 "缺少源码信息"
2. 检查异常有有效 Position（非 unknown），否则报错 "缺少位置信息"
3. 解析 `expectedPointerLine`，提取 `^` 的起始位置和长度
4. 验证源码行与 `expectedSourceLine` 匹配
5. 验证指针起始位置 = `Position.startColumn`
6. 验证指针长度 = `Position.endColumn - Position.startColumn`
7. 验证错误消息中的实际指针行与 `expectedPointerLine` 匹配
8. 验证错误消息包含 `expectedErrorMessage`

### 3. 辅助方法

```java
/** 从指针行提取位置信息 */
private static PointerInfo extractPointerInfo(String pointerLine);

/** 从源码提取指定行 */
private static String extractLine(String sourceText, int lineNumber);

/** 从错误消息提取实际指针行 */
private static String extractActualPointerLine(String errorMessage, int lineNumber);

/** 指针信息数据类 */
private static class PointerInfo {
    final int startColumn;  // 指针起始列号（从1开始）
    final int length;       // 指针长度（^ 数量）
}
```

### 4. 断言失败时的详细输出

当断言失败时，除了基本的错误信息，还要打印期望的和实际的完整错误定位信息：

**输出格式示例：**

```
AssertionFailedError: Pointer position mismatch

Expected:

  |
1 | Hello {user.name}!
  |             ^^^^ Runtime error: Cannot access property 'name' of null

Actual:

  |
1 | Hello {user.name}!
  |        ^^^^^^^^^ Runtime error: Cannot access property 'name' of null
```

**实现要点：**
- 使用 `AssertionError` 抛出失败信息
- 直接使用 Rust 风格错误定位格式输出期望和实际值
- 便于直观对比指针位置差异

---

## 指针定位规则

错误指针应精确指向最小错误元素：

| 错误类型 | 指针指向 | 示例 |
|----------|----------|------|
| 属性访问 | 属性名（不含点号） | `user.name` → `name` |
| 方法调用 | 方法名（不含括号） | `obj.toString()` → `toString` |
| 索引越界 | 索引值（不含方括号） | `arr[10]` → `10` |
| 语法错误 | 具体错误元素 | `{x + }` → `}` 或空位置 |

---

## 测试文件更新范围

| 文件 | 修改内容 |
|------|----------|
| `SyntaxErrorTest.java` | 改用 `assertErrorPointer` |
| `RuntimeExceptionFormattingTest.java` | 改用 `assertErrorPointer` |
| `EtlURLLazyEvaluationTest.java` | 改用 `assertErrorPointer` |
| `TemplateExceptionTest.java` | 部分改用 `assertErrorPointer` |

---

## 测试修改示例

### SyntaxErrorTest

从：
```java
assertSyntaxError("{x + }", "Unexpected token");
```

改为：
```java
TemplateSyntaxException ex = assertThrows(
    TemplateSyntaxException.class,
    () -> createParser("{x + }").parse()
);

ErrorPointerAssert.assertErrorPointer(ex,
    "{x + }",
    "     ^",
    "Unexpected token"
);
```

### RuntimeExceptionFormattingTest

从：
```java
assertTrue(ex.getMessage().contains("^"));
assertTrue(ex.getStartColumn() >= 9);
```

改为：
```java
ErrorPointerAssert.assertErrorPointer(ex,
    "Value: {a.xxx}",
    "          ^^^",
    "xxx"
);
```

### EtlURLLazyEvaluationTest

从：
```java
assertTrue(ex.getStartLine() >= 1);
assertTrue(ex.getMessage().contains("null"));
```

改为：
```java
ErrorPointerAssert.assertErrorPointer(ex,
    "http://{user.name}/path",
    "            ^^^^",
    "Cannot access property 'name' of null"
);
```

---

## 验收标准

- [ ] ErrorPointerAssert 工具类创建完成
- [ ] 所有辅助方法实现完成
- [ ] SyntaxErrorTest 所有测试改用新验证方法
- [ ] RuntimeExceptionFormattingTest 所有测试改用新验证方法
- [ ] EtlURLLazyEvaluationTest 所有测试改用新验证方法
- [ ] TemplateExceptionTest 相关测试改用新验证方法
- [ ] 所有测试通过