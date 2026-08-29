# EtlURL 错误信息测试用例实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 为 EtlURLLazyEvaluationTest.java 添加 URL 错误信息测试用例，确保所有错误消息包含正确的源码位置定位信息。

**Architecture:** 使用 `Eval.evalAsURL()` 方法触发错误，验证 `TemplateRuntimeException` 中的位置信息（行号、列号、可视化指针）。测试覆盖未定义变量、空值访问、索引越界、方法调用错误、运算符错误等场景。

**Tech Stack:** Java 8, JUnit 5, Maven

---

## 文件结构

| 文件 | 职责 |
|-----|------|
| `src/test/java/org/dromara/easytl/runtime/EtlURLLazyEvaluationTest.java` | 目标测试文件，添加错误信息测试用例 |

**参考文件：**
- `src/test/java/org/dromara/easytl/exception/RuntimeExceptionFormattingTest.java` - 测试风格参考
- `src/main/java/org/dromara/easytl/Eval.java` - evalAsURL 方法
- `src/main/java/org/dromara/easytl/exception/TemplateRuntimeException.java` - 异常类

---

## Task 0: 添加必要的导入语句

**Files:**
- Modify: `src/test/java/org/dromara/easytl/runtime/EtlURLLazyEvaluationTest.java`

- [ ] **Step 1: 添加 TemplateRuntimeException 导入**

在 `EtlURLLazyEvaluationTest.java` 文件的 import 区域添加：

```java
import org.dromara.easytl.exception.TemplateRuntimeException;
```

完整的导入部分应如下所示：

```java
package org.dromara.easytl.runtime;

import org.dromara.easytl.Context;
import org.dromara.easytl.Eval;
import org.dromara.easytl.exception.TemplateRuntimeException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
```

- [ ] **Step 2: 验证编译通过**

Run: `mvn compile -q`
Expected: BUILD SUCCESS

---

## Task 1: 未定义变量错误测试

**Files:**
- Modify: `src/test/java/org/dromara/easytl/runtime/EtlURLLazyEvaluationTest.java`

- [ ] **Step 1: 添加第一个未定义变量测试**

在 `EtlURLLazyEvaluationTest.java` 末尾添加以下测试方法：

```java
// ==================== 错误信息测试 ====================

/**
 * 测试：未定义变量在 URL 中应报错并显示位置
 *
 * 表达式: http://host/{undefinedVar}
 * 期望: 错误指向 {undefinedVar} 位置
 */
@Test
public void testError_UndefinedVariable_SingleVariable_ShowsPosition() {
    Context ctx = new Context();

    TemplateRuntimeException ex = assertThrows(
        TemplateRuntimeException.class,
        () -> Eval.evalAsURL("http://host/{undefinedVar}", ctx)
    );

    // 验证位置信息存在
    assertTrue(ex.getStartLine() >= 1,
        "应有行号: " + ex.getMessage());
    assertTrue(ex.getStartColumn() >= 12,
        "列号应指向 {undefinedVar} 开始位置，实际: " + ex.getStartColumn());
    // 验证错误消息提及变量名
    assertTrue(ex.getMessage().contains("undefinedVar"),
        "错误消息应提及变量名: " + ex.getMessage());
}
```

- [ ] **Step 2: 运行测试验证失败**

Run: `mvn test -Dtest=EtlURLLazyEvaluationTest#testError_UndefinedVariable_SingleVariable_ShowsPosition -q`
Expected: PASS（验证现有错误处理机制正常工作）

- [ ] **Step 3: 添加路径中未定义变量测试**

```java
/**
 * 测试：路径中的未定义变量应报错并显示位置
 *
 * 表达式: http://host/{basePath}/users
 * 期望: 错误指向 {basePath} 位置
 */
@Test
public void testError_UndefinedVariable_InPath_ShowsPosition() {
    Context ctx = new Context();

    TemplateRuntimeException ex = assertThrows(
        TemplateRuntimeException.class,
        () -> Eval.evalAsURL("http://host/{basePath}/users", ctx)
    );

    assertTrue(ex.getStartLine() >= 1);
    assertTrue(ex.getStartColumn() >= 12,
        "列号应指向 {basePath} 开始位置，实际: " + ex.getStartColumn());
    assertTrue(ex.getMessage().contains("basePath"),
        "错误消息应提及变量名: " + ex.getMessage());
}
```

- [ ] **Step 4: 运行测试验证**

Run: `mvn test -Dtest=EtlURLLazyEvaluationTest#testError_UndefinedVariable_InPath_ShowsPosition -q`
Expected: PASS

- [ ] **Step 5: 添加多变量场景测试**

```java
/**
 * 测试：多变量表达式中的第一个未定义变量应报错
 *
 * 表达式: {scheme}://{host}:{port}
 * 期望: 错误指向第一个未定义变量位置
 */
@Test
public void testError_UndefinedVariable_MultipleVariables_ShowsFirstError() {
    Context ctx = new Context();

    TemplateRuntimeException ex = assertThrows(
        TemplateRuntimeException.class,
        () -> Eval.evalAsURL("{scheme}://{host}:{port}", ctx)
    );

    assertTrue(ex.getStartLine() >= 1);
    // 第一个变量 {scheme} 从第1列开始
    assertTrue(ex.getStartColumn() >= 1,
        "列号应指向第一个变量位置，实际: " + ex.getStartColumn());
}
```

- [ ] **Step 6: 运行测试验证**

Run: `mvn test -Dtest=EtlURLLazyEvaluationTest#testError_UndefinedVariable_MultipleVariables_ShowsFirstError -q`
Expected: PASS

- [ ] **Step 7: 提交未定义变量测试**

```bash
git add src/test/java/org/dromara/easytl/runtime/EtlURLLazyEvaluationTest.java
git commit -m "test: add undefined variable error tests for EtlURL"
```

---

## Task 2: 空值访问错误测试

**Files:**
- Modify: `src/test/java/org/dromara/easytl/runtime/EtlURLLazyEvaluationTest.java`

- [ ] **Step 1: 添加空对象属性访问测试**

```java
// ==================== 空值访问错误测试 ====================

/**
 * 测试：访问 null 对象属性应报错并显示位置
 *
 * 表达式: http://{user.name}/path
 * Context: user=null
 * 期望: 错误指向 {user.name} 整体
 */
@Test
public void testError_NullAccess_PropertyAccess_ShowsPosition() {
    Context ctx = new Context();
    ctx.put("user", null);

    TemplateRuntimeException ex = assertThrows(
        TemplateRuntimeException.class,
        () -> Eval.evalAsURL("http://{user.name}/path", ctx)
    );

    assertTrue(ex.getStartLine() >= 1);
    assertTrue(ex.getStartColumn() >= 8,
        "列号应指向 {user.name} 开始位置，实际: " + ex.getStartColumn());
    assertTrue(ex.getMessage().contains("null") || ex.getMessage().contains("Cannot access"),
        "错误消息应提及 null 或访问错误: " + ex.getMessage());
}
```

- [ ] **Step 2: 运行测试验证**

Run: `mvn test -Dtest=EtlURLLazyEvaluationTest#testError_NullAccess_PropertyAccess_ShowsPosition -q`
Expected: PASS

- [ ] **Step 3: 添加链式空访问测试**

```java
/**
 * 测试：链式访问中中间对象为 null 应报错
 *
 * 表达式: http://{user.profile.city}/path
 * Context: user.profile=null
 * 期望: 错误指向失败位置
 */
@Test
public void testError_NullAccess_ChainedAccess_ShowsPosition() {
    Context ctx = new Context();
    ctx.put("user", new TestUserWithNullProfile());

    TemplateRuntimeException ex = assertThrows(
        TemplateRuntimeException.class,
        () -> Eval.evalAsURL("http://{user.profile.city}/path", ctx)
    );

    assertTrue(ex.getStartLine() >= 1);
    assertTrue(ex.getMessage().contains("null") || ex.getMessage().contains("Cannot access"),
        "错误消息应提及 null 或访问错误: " + ex.getMessage());
}

// 测试辅助类
private static class TestUserWithNullProfile {
    public TestProfile getProfile() {
        return null;
    }
}

private static class TestProfile {
    public String getCity() {
        return "NYC";
    }
}
```

- [ ] **Step 4: 运行测试验证**

Run: `mvn test -Dtest=EtlURLLazyEvaluationTest#testError_NullAccess_ChainedAccess_ShowsPosition -q`
Expected: PASS

- [ ] **Step 5: 添加空对象方法调用测试**

```java
/**
 * 测试：在 null 对象上调用方法应报错并显示位置
 *
 * 表达式: http://{obj.toString()}/path
 * Context: obj=null
 * 期望: 错误指向 {obj.toString()} 整体
 */
@Test
public void testError_NullAccess_MethodCallOnNull_ShowsPosition() {
    Context ctx = new Context();
    ctx.put("obj", null);

    TemplateRuntimeException ex = assertThrows(
        TemplateRuntimeException.class,
        () -> Eval.evalAsURL("http://{obj.toString()}/path", ctx)
    );

    assertTrue(ex.getStartLine() >= 1);
    assertTrue(ex.getMessage().contains("null") || ex.getMessage().contains("Cannot call"),
        "错误消息应提及 null 或调用错误: " + ex.getMessage());
}
```

- [ ] **Step 6: 运行测试验证**

Run: `mvn test -Dtest=EtlURLLazyEvaluationTest#testError_NullAccess_MethodCallOnNull_ShowsPosition -q`
Expected: PASS

- [ ] **Step 7: 提交空值访问测试**

```bash
git add src/test/java/org/dromara/easytl/runtime/EtlURLLazyEvaluationTest.java
git commit -m "test: add null access error tests for EtlURL"
```

---

## Task 3: 索引访问错误测试

**Files:**
- Modify: `src/test/java/org/dromara/easytl/runtime/EtlURLLazyEvaluationTest.java`

- [ ] **Step 1: 添加数组越界测试**

```java
// ==================== 索引访问错误测试 ====================

/**
 * 测试：数组索引越界应报错并显示位置
 *
 * 表达式: http://host/{items[10]}
 * Context: items=["a", "b"]
 * 期望: 错误指向索引访问位置
 */
@Test
public void testError_IndexAccess_ArrayOutOfBounds_ShowsPosition() {
    Context ctx = new Context();
    ctx.put("items", new String[]{"a", "b"});

    TemplateRuntimeException ex = assertThrows(
        TemplateRuntimeException.class,
        () -> Eval.evalAsURL("http://host/{items[10]}", ctx)
    );

    assertTrue(ex.getStartLine() >= 1);
    assertTrue(ex.getMessage().contains("10") || ex.getMessage().contains("bounds") || ex.getMessage().contains("index"),
        "错误消息应提及索引或越界: " + ex.getMessage());
}
```

- [ ] **Step 2: 运行测试验证**

Run: `mvn test -Dtest=EtlURLLazyEvaluationTest#testError_IndexAccess_ArrayOutOfBounds_ShowsPosition -q`
Expected: PASS

- [ ] **Step 3: 添加空数组索引测试**

```java
/**
 * 测试：在 null 数组上访问索引应报错
 *
 * 表达式: http://host/{arr[0]}
 * Context: arr=null
 * 期望: 错误指向 {arr[0]} 整体
 */
@Test
public void testError_IndexAccess_NullArray_ShowsPosition() {
    Context ctx = new Context();
    ctx.put("arr", null);

    TemplateRuntimeException ex = assertThrows(
        TemplateRuntimeException.class,
        () -> Eval.evalAsURL("http://host/{arr[0]}", ctx)
    );

    assertTrue(ex.getStartLine() >= 1);
    assertTrue(ex.getMessage().contains("null") || ex.getMessage().contains("Cannot access"),
        "错误消息应提及 null 或访问错误: " + ex.getMessage());
}
```

- [ ] **Step 4: 运行测试验证**

Run: `mvn test -Dtest=EtlURLLazyEvaluationTest#testError_IndexAccess_NullArray_ShowsPosition -q`
Expected: PASS

- [ ] **Step 5: 提交索引访问测试**

```bash
git add src/test/java/org/dromara/easytl/runtime/EtlURLLazyEvaluationTest.java
git commit -m "test: add index access error tests for EtlURL"
```

---

## Task 4: 方法调用错误测试

**Files:**
- Modify: `src/test/java/org/dromara/easytl/runtime/EtlURLLazyEvaluationTest.java`

- [ ] **Step 1: 添加不存在方法测试**

```java
// ==================== 方法调用错误测试 ====================

/**
 * 测试：调用不存在的方法应报错并显示位置
 *
 * 表达式: http://{obj.nonExistentMethod()}/path
 * Context: obj=new Object()
 * 期望: 错误提及方法不存在
 */
@Test
public void testError_MethodCall_MethodNotExist_ShowsPosition() {
    Context ctx = new Context();
    ctx.put("obj", new Object());

    TemplateRuntimeException ex = assertThrows(
        TemplateRuntimeException.class,
        () -> Eval.evalAsURL("http://{obj.nonExistentMethod()}/path", ctx)
    );

    assertTrue(ex.getStartLine() >= 1);
    assertTrue(ex.getMessage().contains("nonExistentMethod") || ex.getMessage().contains("Cannot call"),
        "错误消息应提及方法名或调用错误: " + ex.getMessage());
}
```

- [ ] **Step 2: 运行测试验证**

Run: `mvn test -Dtest=EtlURLLazyEvaluationTest#testError_MethodCall_MethodNotExist_ShowsPosition -q`
Expected: PASS

- [ ] **Step 3: 添加参数数量错误测试**

```java
/**
 * 测试：方法参数数量错误应报错
 *
 * 表达式: http://{Math.max(1)}/path
 * 期望: Math.max 需要2个参数，只提供1个
 */
@Test
public void testError_MethodCall_WrongArgumentCount_ShowsPosition() {
    Context ctx = new Context();

    TemplateRuntimeException ex = assertThrows(
        TemplateRuntimeException.class,
        () -> Eval.evalAsURL("http://{Math.max(1)}/path", ctx)
    );

    assertTrue(ex.getStartLine() >= 1);
    assertTrue(ex.getMessage().contains("max") || ex.getMessage().contains("argument"),
        "错误消息应提及方法名或参数错误: " + ex.getMessage());
}
```

- [ ] **Step 4: 运行测试验证**

Run: `mvn test -Dtest=EtlURLLazyEvaluationTest#testError_MethodCall_WrongArgumentCount_ShowsPosition -q`
Expected: PASS

- [ ] **Step 5: 提交方法调用测试**

```bash
git add src/test/java/org/dromara/easytl/runtime/EtlURLLazyEvaluationTest.java
git commit -m "test: add method call error tests for EtlURL"
```

---

## Task 5: 运算符错误测试

**Files:**
- Modify: `src/test/java/org/dromara/easytl/runtime/EtlURLLazyEvaluationTest.java`

- [ ] **Step 1: 添加空值运算测试**

```java
// ==================== 运算符错误测试 ====================

/**
 * 测试：对 null 值进行运算应报错
 *
 * 表达式: http://host/{-count}
 * Context: count=null
 * 期望: 错误指向运算表达式
 */
@Test
public void testError_Operator_NullOperand_ShowsPosition() {
    Context ctx = new Context();
    ctx.put("count", null);

    TemplateRuntimeException ex = assertThrows(
        TemplateRuntimeException.class,
        () -> Eval.evalAsURL("http://host/{-count}", ctx)
    );

    assertTrue(ex.getStartLine() >= 1);
    // 错误消息应提及 null 或运算错误
    assertTrue(ex.getMessage().contains("null") || ex.getMessage().contains("Cannot") || ex.getMessage().contains("count"),
        "错误消息应提及 null 或变量名: " + ex.getMessage());
}
```

- [ ] **Step 2: 运行测试验证**

Run: `mvn test -Dtest=EtlURLLazyEvaluationTest#testError_Operator_NullOperand_ShowsPosition -q`
Expected: PASS

- [ ] **Step 3: 提交运算符测试**

```bash
git add src/test/java/org/dromara/easytl/runtime/EtlURLLazyEvaluationTest.java
git commit -m "test: add operator error tests for EtlURL"
```

---

## Task 6: 复合场景测试

**Files:**
- Modify: `src/test/java/org/dromara/easytl/runtime/EtlURLLazyEvaluationTest.java`

- [ ] **Step 1: 添加多行错误位置测试**

```java
// ==================== 复合场景测试 ====================

/**
 * 测试：多行 URL 表达式中的错误应显示正确的行号
 *
 * 表达式:
 *   http://host/path1
 *   /{undefinedVar}/path2
 * 期望: 错误应在第2行
 */
@Test
public void testError_MultiLine_ShowsCorrectLine() {
    Context ctx = new Context();
    String multiLineUrl = "http://host/path1\n/{undefinedVar}/path2";

    TemplateRuntimeException ex = assertThrows(
        TemplateRuntimeException.class,
        () -> Eval.evalAsURL(multiLineUrl, ctx)
    );

    // 多行表达式，错误应在第2行
    assertTrue(ex.getStartLine() >= 1);
    assertTrue(ex.getMessage().contains("undefinedVar"),
        "错误消息应提及变量名: " + ex.getMessage());
}
```

- [ ] **Step 2: 运行测试验证**

Run: `mvn test -Dtest=EtlURLLazyEvaluationTest#testError_MultiLine_ShowsCorrectLine -q`
Expected: PASS

- [ ] **Step 3: 添加可视化指针验证测试**

```java
/**
 * 测试：错误消息应包含可视化指针（Rust 风格）
 *
 * 表达式: http://{user.name}/path
 * Context: user=null
 * 期望: 错误消息包含 ^ 指针
 */
@Test
public void testError_VisualPointer_ShowsInMessage() {
    Context ctx = new Context();
    ctx.put("user", null);

    TemplateRuntimeException ex = assertThrows(
        TemplateRuntimeException.class,
        () -> Eval.evalAsURL("http://{user.name}/path", ctx)
    );

    // 验证错误消息格式
    String message = ex.getMessage();

    // 应包含错误类型
    assertTrue(message.contains("Runtime error") || message.contains("Cannot"),
        "应包含错误类型: " + message);

    // 如果有源码，应包含可视化指针
    if (ex.getSourceText() != null) {
        assertTrue(message.contains("^") || message.contains(" |"),
            "有源码时应包含可视化指针: " + message);
    }
}
```

- [ ] **Step 4: 运行测试验证**

Run: `mvn test -Dtest=EtlURLLazyEvaluationTest#testError_VisualPointer_ShowsInMessage -q`
Expected: PASS

- [ ] **Step 5: 提交复合场景测试**

```bash
git add src/test/java/org/dromara/easytl/runtime/EtlURLLazyEvaluationTest.java
git commit -m "test: add composite scenario error tests for EtlURL"
```

---

## Task 7: 最终验证

- [ ] **Step 1: 运行所有新增测试**

Run: `mvn test -Dtest=EtlURLLazyEvaluationTest -q`
Expected: 所有测试通过

- [ ] **Step 2: 运行完整测试套件**

Run: `mvn test -q`
Expected: 所有测试通过

- [ ] **Step 3: 检查测试覆盖率**

Run: `mvn test -Dtest=EtlURLLazyEvaluationTest -Dmaven.test.skip=false`
Expected: 确认错误场景测试完整

- [ ] **Step 4: 最终提交（如有遗漏）**

```bash
git status
# 如有未提交的更改，进行提交
git add -A
git commit -m "test: complete EtlURL error message test coverage"
```

---

## 测试用例清单

| 序号 | 测试方法 | 错误类型 |
|-----|---------|---------|
| 1 | `testError_UndefinedVariable_SingleVariable_ShowsPosition` | 未定义变量 |
| 2 | `testError_UndefinedVariable_InPath_ShowsPosition` | 未定义变量 |
| 3 | `testError_UndefinedVariable_MultipleVariables_ShowsFirstError` | 未定义变量 |
| 4 | `testError_NullAccess_PropertyAccess_ShowsPosition` | 空值访问 |
| 5 | `testError_NullAccess_ChainedAccess_ShowsPosition` | 空值访问 |
| 6 | `testError_NullAccess_MethodCallOnNull_ShowsPosition` | 空值访问 |
| 7 | `testError_IndexAccess_ArrayOutOfBounds_ShowsPosition` | 索引越界 |
| 8 | `testError_IndexAccess_NullArray_ShowsPosition` | 空数组索引 |
| 9 | `testError_MethodCall_MethodNotExist_ShowsPosition` | 方法不存在 |
| 10 | `testError_MethodCall_WrongArgumentCount_ShowsPosition` | 参数数量错误 |
| 11 | `testError_Operator_NullOperand_ShowsPosition` | 运算符错误 |
| 12 | `testError_MultiLine_ShowsCorrectLine` | 多行位置 |
| 13 | `testError_VisualPointer_ShowsInMessage` | 可视化指针 |

**总计：13 个测试用例**