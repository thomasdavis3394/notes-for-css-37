# 错误指针位置严格验证实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-step. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 创建 ErrorPointerAssert 工具类并更新所有错误测试用例，实现严格的指针位置和长度验证。

**Architecture:** 创建独立的测试工具类 ErrorPointerAssert，提供 assertErrorPointer 方法验证指针位置。先写工具类测试（TDD），再实现工具类，最后更新现有测试文件。

**Tech Stack:** Java 8, JUnit 5

---

## 文件结构

| 文件 | 操作 | 说明 |
|------|------|------|
| `src/test/java/org/dromara/easytl/testutil/ErrorPointerAssert.java` | 创建 | 错误指针验证工具类 |
| `src/test/java/org/dromara/easytl/testutil/ErrorPointerAssertTest.java` | 创建 | 工具类测试 |
| `src/test/java/org/dromara/easytl/parser/SyntaxErrorTest.java` | 修改 | 改用 assertErrorPointer |
| `src/test/java/org/dromara/easytl/exception/RuntimeExceptionFormattingTest.java` | 修改 | 改用 assertErrorPointer |
| `src/test/java/org/dromara/easytl/runtime/EtlURLLazyEvaluationTest.java` | 修改 | 改用 assertErrorPointer |
| `src/test/java/org/dromara/easytl/exception/TemplateExceptionTest.java` | 修改 | 部分改用 assertErrorPointer |

---

### Task 1: 创建 ErrorPointerAssert 测试类

**Files:**
- Create: `src/test/java/org/dromara/easytl/testutil/ErrorPointerAssertTest.java`

- [ ] **Step 1: 创建测试类框架**

```java
package org.dromara.easytl.testutil;

import org.dromara.easytl.exception.TemplateRuntimeException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ErrorPointerAssert 工具类测试
 */
class ErrorPointerAssertTest {

    // 测试将在此添加
}
```

- [ ] **Step 2: 编写成功验证测试**

```java
@Test
void testAssertErrorPointer_Success() {
    // 创建一个带有完整位置信息的异常
    String sourceText = "Hello {user.name}!";
    TemplateRuntimeException ex = new TemplateRuntimeException(
        "Cannot access property 'name' of null",
        1, 13, 1, 17, sourceText
    );

    // 验证成功的情况
    ErrorPointerAssert.assertErrorPointer(ex,
        "Hello {user.name}!",
        "            ^^^^",
        "Cannot access property 'name' of null"
    );
}
```

- [ ] **Step 3: 编写指针位置不匹配测试**

```java
@Test
void testAssertErrorPointer_PositionMismatch() {
    String sourceText = "Hello {user.name}!";
    TemplateRuntimeException ex = new TemplateRuntimeException(
        "Cannot access property 'name' of null",
        1, 8, 1, 18, sourceText  // 位置不匹配
    );

    // 应抛出 AssertionError
    AssertionError error = assertThrows(AssertionError.class, () -> {
        ErrorPointerAssert.assertErrorPointer(ex,
            "Hello {user.name}!",
            "            ^^^^",
            "Cannot access property 'name' of null"
        );
    });

    // 验证错误消息包含 Expected 和 Actual
    assertTrue(error.getMessage().contains("Expected"));
    assertTrue(error.getMessage().contains("Actual"));
}
```

- [ ] **Step 4: 编写缺少源码信息测试**

```java
@Test
void testAssertErrorPointer_MissingSourceText() {
    // 创建没有源码的异常
    TemplateRuntimeException ex = new TemplateRuntimeException(
        "Test error", 1, 8, 1, 18
    );

    AssertionError error = assertThrows(AssertionError.class, () -> {
        ErrorPointerAssert.assertErrorPointer(ex,
            "Hello {user.name}!",
            "            ^^^^",
            "Test error"
        );
    });

    assertTrue(error.getMessage().contains("Missing source text"));
}
```

- [ ] **Step 5: 编写缺少位置信息测试**

```java
@Test
void testAssertErrorPointer_MissingPosition() {
    // 创建没有位置的异常
    TemplateRuntimeException ex = new TemplateRuntimeException("Test error");

    AssertionError error = assertThrows(AssertionError.class, () -> {
        ErrorPointerAssert.assertErrorPointer(ex,
            "Hello {user.name}!",
            "            ^^^^",
            "Test error"
        );
    });

    assertTrue(error.getMessage().contains("Missing position"));
}
```

- [ ] **Step 6: 编写错误消息不匹配测试**

```java
@Test
void testAssertErrorPointer_MessageMismatch() {
    String sourceText = "Hello {user.name}!";
    TemplateRuntimeException ex = new TemplateRuntimeException(
        "Cannot access property 'name' of null",
        1, 13, 1, 17, sourceText
    );

    AssertionError error = assertThrows(AssertionError.class, () -> {
        ErrorPointerAssert.assertErrorPointer(ex,
            "Hello {user.name}!",
            "            ^^^^",
            "Different error message"
        );
    });

    assertTrue(error.getMessage().contains("Message mismatch"));
}
```

- [ ] **Step 7: 运行测试验证失败**

Run: `mvn test -Dtest=ErrorPointerAssertTest -q`

Expected: 所有测试失败，因为 ErrorPointerAssert 类尚未创建

---

### Task 2: 实现 ErrorPointerAssert 工具类

**Files:**
- Create: `src/test/java/org/dromara/easytl/testutil/ErrorPointerAssert.java`

- [ ] **Step 1: 创建工具类框架和 PointerInfo 内部类**

```java
package org.dromara.easytl.testutil;

import org.dromara.easytl.ast.Position;
import org.dromara.easytl.exception.TemplateException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 错误指针位置验证工具类
 */
public class ErrorPointerAssert {

    /**
     * 指针位置信息
     */
    private static class PointerInfo {
        final int startColumn;  // 指针起始列号（从1开始）
        final int length;       // 指针长度（^ 数量）

        PointerInfo(int startColumn, int length) {
            this.startColumn = startColumn;
            this.length = length;
        }
    }

    // 方法将在后续步骤添加
}
```

- [ ] **Step 2: 实现 extractPointerInfo 方法**

```java
/**
 * 从指针行提取指针位置信息
 *
 * @param pointerLine 指针行字符串（如 "            ^^^^"）
 * @return PointerInfo 包含起始列号和长度
 */
private static PointerInfo extractPointerInfo(String pointerLine) {
    int startColumn = -1;
    int length = 0;

    for (int i = 0; i < pointerLine.length(); i++) {
        char c = pointerLine.charAt(i);
        if (c == '^') {
            if (startColumn == -1) {
                startColumn = i + 1;  // 列号从1开始
            }
            length++;
        }
    }

    if (startColumn == -1) {
        throw new IllegalArgumentException("Pointer line must contain at least one '^' character");
    }

    return new PointerInfo(startColumn, length);
}
```

- [ ] **Step 3: 实现 extractLine 方法**

```java
/**
 * 从源码文本提取指定行内容
 *
 * @param sourceText 源码文本
 * @param lineNumber 行号（从1开始）
 * @return 该行内容
 */
private static String extractLine(String sourceText, int lineNumber) {
    if (sourceText == null) {
        return null;
    }

    String[] lines = sourceText.split("\r?\n");
    if (lineNumber < 1 || lineNumber > lines.length) {
        return null;
    }

    return lines[lineNumber - 1];
}
```

- [ ] **Step 4: 实现 formatPointerDisplay 方法（用于失败输出）**

```java
/**
 * 格式化指针显示（Rust 风格）
 *
 * @param sourceLine 源码行
 * @param pointerLine 指针行
 * @param errorMessage 错误消息
 * @param lineNumber 行号
 * @return 格式化的字符串
 */
private static String formatPointerDisplay(
    String sourceLine,
    String pointerLine,
    String errorMessage,
    int lineNumber
) {
    StringBuilder sb = new StringBuilder();
    sb.append("  |\n");
    sb.append(lineNumber).append(" | ").append(sourceLine).append("\n");
    sb.append("  | ").append(pointerLine).append(" ").append(errorMessage);
    return sb.toString();
}
```

- [ ] **Step 5: 实现 assertErrorPointer 主方法**

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
 *
 * @param exception 异常对象
 * @param expectedSourceLine 期望的源码行
 * @param expectedPointerLine 期望的指针行
 * @param expectedErrorMessage 期望的错误消息（包含检查）
 */
public static void assertErrorPointer(
    TemplateException exception,
    String expectedSourceLine,
    String expectedPointerLine,
    String expectedErrorMessage
) {
    // 1. 检查源码信息
    String actualSourceText = exception.getSourceText();
    if (actualSourceText == null) {
        throw new AssertionError("Missing source text in exception");
    }

    // 2. 检查位置信息
    Position position = exception.getPosition();
    if (position == null || position.isUnknown()) {
        throw new AssertionError("Missing position information in exception");
    }

    int actualLine = exception.getStartLine();
    int actualStartColumn = exception.getStartColumn();
    int actualEndColumn = exception.getEndColumn();
    int actualPointerLength = actualEndColumn - actualStartColumn;

    // 3. 解析期望的指针信息
    PointerInfo expectedPointerInfo = extractPointerInfo(expectedPointerLine);

    // 4. 提取实际的源码行
    String actualSourceLine = extractLine(actualSourceText, actualLine);

    // 5. 验证源码行匹配
    if (!expectedSourceLine.equals(actualSourceLine)) {
        throw new AssertionError(buildMismatchMessage(
            "Source line mismatch",
            expectedSourceLine, expectedPointerLine, expectedErrorMessage,
            actualSourceLine, actualLine, actualStartColumn, actualPointerLength,
            exception.getMessage()
        ));
    }

    // 6. 验证指针位置匹配
    if (expectedPointerInfo.startColumn != actualStartColumn ||
        expectedPointerInfo.length != actualPointerLength) {
        throw new AssertionError(buildMismatchMessage(
            "Pointer position mismatch",
            expectedSourceLine, expectedPointerLine, expectedErrorMessage,
            actualSourceLine, actualLine, actualStartColumn, actualPointerLength,
            exception.getMessage()
        ));
    }

    // 7. 验证错误消息包含
    String actualMessage = exception.getMessage();
    if (!actualMessage.contains(expectedErrorMessage)) {
        throw new AssertionError(buildMismatchMessage(
            "Message mismatch",
            expectedSourceLine, expectedPointerLine, expectedErrorMessage,
            actualSourceLine, actualLine, actualStartColumn, actualPointerLength,
            actualMessage
        ));
    }
}
```

- [ ] **Step 6: 实现 buildMismatchMessage 方法**

```java
/**
 * 构建不匹配时的错误消息
 */
private static String buildMismatchMessage(
    String errorTitle,
    String expectedSourceLine,
    String expectedPointerLine,
    String expectedErrorMessage,
    String actualSourceLine,
    int actualLine,
    int actualStartColumn,
    int actualPointerLength,
    String actualFullMessage
) {
    StringBuilder sb = new StringBuilder();
    sb.append(errorTitle).append("\n\n");
    sb.append("Expected:\n\n");
    sb.append(formatPointerDisplay(
        expectedSourceLine, expectedPointerLine, expectedErrorMessage, 1
    ));
    sb.append("\n\nActual:\n\n");

    // 构建实际的指针行
    String actualPointerLine = buildPointerLine(actualStartColumn, actualPointerLength);
    sb.append(formatPointerDisplay(
        actualSourceLine, actualPointerLine, expectedErrorMessage, actualLine
    ));
    sb.append("\n\nFull error message:\n").append(actualFullMessage);

    return sb.toString();
}

/**
 * 构建指针行字符串
 *
 * @param startColumn 起始列号
 * @param length 指针长度
 * @return 指针行字符串
 */
private static String buildPointerLine(int startColumn, int length) {
    StringBuilder sb = new StringBuilder();
    for (int i = 1; i < startColumn; i++) {
        sb.append(' ');
    }
    for (int i = 0; i < length; i++) {
        sb.append('^');
    }
    return sb.toString();
}
```

- [ ] **Step 7: 运行测试验证通过**

Run: `mvn test -Dtest=ErrorPointerAssertTest -q`

Expected: 所有测试通过

- [ ] **Step 8: 提交工具类**

```bash
git add src/test/java/org/dromara/easytl/testutil/ErrorPointerAssert.java \
        src/test/java/org/dromara/easytl/testutil/ErrorPointerAssertTest.java
git commit -m "feat: add ErrorPointerAssert utility for strict pointer validation

- Add assertErrorPointer method to validate pointer position and length
- Add PointerInfo inner class to extract pointer information
- Add comprehensive tests for success and failure cases
- Add detailed mismatch output in Rust-style format"
```

---

### Task 3: 更新 SyntaxErrorTest

**Files:**
- Modify: `src/test/java/org/dromara/easytl/parser/SyntaxErrorTest.java`

- [ ] **Step 1: 添加 ErrorPointerAssert 导入**

在文件顶部添加导入：

```java
import org.dromara.easytl.testutil.ErrorPointerAssert;
```

- [ ] **Step 2: 创建新的辅助方法替代 assertSyntaxError**

将原有的 `assertSyntaxError` 方法保留，但添加新的 `assertSyntaxErrorWithPointer` 方法：

```java
/**
 * 辅助方法：验证解析会抛出语法错误，并严格验证指针位置
 */
private void assertSyntaxErrorWithPointer(
    String input,
    String expectedSourceLine,
    String expectedPointerLine,
    String expectedErrorMessage
) throws IOException {
    Parser parser = createParser(input);
    TemplateSyntaxException ex = assertThrows(
        TemplateSyntaxException.class,
        () -> parser.parse(),
        "应该抛出 TemplateSyntaxException"
    );

    ErrorPointerAssert.assertErrorPointer(ex,
        expectedSourceLine,
        expectedPointerLine,
        expectedErrorMessage
    );
}
```

- [ ] **Step 3: 更新 testBinaryExpressionMissingRightOperand 测试**

将原测试改为使用新方法。先查看原测试确定正确的指针位置。

原代码：`{x + }`，错误在 `}` 位置（第5列）

```java
@Test
public void testBinaryExpressionMissingRightOperand() throws IOException {
    assertSyntaxErrorWithPointer("{x + }",
        "{x + }",
        "     ^",
        "Unexpected token"
    );
}
```

- [ ] **Step 4: 运行测试验证**

Run: `mvn test -Dtest=SyntaxErrorTest#testBinaryExpressionMissingRightOperand -q`

Expected: 测试通过

- [ ] **Step 5: 更新 testMemberAccessMissingIdentifier 测试**

原代码：`{obj.}`，错误在 `}` 或 `.}` 位置

需要先运行测试查看实际错误位置：

Run: `mvn test -Dtest=SyntaxErrorTest#testMemberAccessMissingIdentifier -q`

根据实际错误位置更新测试。

- [ ] **Step 6: 批量更新其他语法错误测试**

逐一更新以下测试方法，根据实际错误指针位置调整：

- `testIfStatementMissingCondition`
- `testForStatementMissingIdentifier`
- `testSwitchStatementMissingExpression`
- `testLetStatementMissingIdentifier`
- `testTernaryExpressionMissingColon`
- `testMethodCallMissingRightParen`
- `testIndexAccessMissingRightBracket`

每个测试改为：

```java
@Test
public void testXxx() throws IOException {
    assertSyntaxErrorWithPointer("input",
        "input",
        "pointer line",
        "error message"
    );
}
```

- [ ] **Step 7: 运行全部 SyntaxErrorTest 测试**

Run: `mvn test -Dtest=SyntaxErrorTest -q`

Expected: 所有测试通过

- [ ] **Step 8: 提交 SyntaxErrorTest 更新**

```bash
git add src/test/java/org/dromara/easytl/parser/SyntaxErrorTest.java
git commit -m "test: update SyntaxErrorTest to use ErrorPointerAssert

- Replace loose pointer checks with strict validation
- Add assertSyntaxErrorWithPointer helper method
- Validate exact pointer position for all syntax errors"
```

---

### Task 4: 更新 RuntimeExceptionFormattingTest

**Files:**
- Modify: `src/test/java/org/dromara/easytl/exception/RuntimeExceptionFormattingTest.java`

- [ ] **Step 1: 添加 ErrorPointerAssert 导入**

```java
import org.dromara.easytl.testutil.ErrorPointerAssert;
```

- [ ] **Step 2: 更新 testMemberAccessOfNull_ShowsPosition 测试**

原模板：`Hello {user.name}!`
需要先运行确认实际指针位置（指向 `name`）

Run: `mvn test -Dtest=RuntimeExceptionFormattingTest#testMemberAccessOfNull_ShowsPosition -q`

查看实际错误位置后更新：

```java
@Test
void testMemberAccessOfNull_ShowsPosition() {
    String template = "Hello {user.name}!";
    Template compiled = engine.compile(template);

    TemplateRuntimeException ex = assertThrows(
        TemplateRuntimeException.class,
        () -> compiled.render(new Context())
    );

    ErrorPointerAssert.assertErrorPointer(ex,
        "Hello {user.name}!",
        "            ^^^^",
        "Cannot access property 'name' of null"
    );
}
```

- [ ] **Step 3: 更新 testMemberAccessPropertyNotExist_PointsToProperty 测试**

```java
@Test
void testMemberAccessPropertyNotExist_PointsToProperty() {
    String template = "Value: {a.xxx}";
    Template compiled = engine.compile(template);

    Context context = new Context();
    context.put("a", new Object());

    TemplateRuntimeException ex = assertThrows(
        TemplateRuntimeException.class,
        () -> compiled.render(context)
    );

    ErrorPointerAssert.assertErrorPointer(ex,
        "Value: {a.xxx}",
        "          ^^^",
        "xxx"
    );
}
```

- [ ] **Step 4: 更新其他指针位置测试**

逐一更新以下测试：
- `testIndexAccessOfNull_ShowsPosition`
- `testMethodCallOnNull_ShowsPosition`
- `testErrorOnSecondLine_ShowsCorrectLine`
- `testErrorOnThirdLine_ShowsCorrectLine`
- `testForLoopOverNull_ShowsPosition`
- `testArrayIndexOutOfBounds_PointsToIndex`
- `testNegativeIndex_PointsToIndex`
- `testMethodWrongArgCount_PointsToMethod`

每个测试运行确认实际指针位置后更新。

- [ ] **Step 5: 运行全部 RuntimeExceptionFormattingTest 测试**

Run: `mvn test -Dtest=RuntimeExceptionFormattingTest -q`

Expected: 所有测试通过

- [ ] **Step 6: 提交 RuntimeExceptionFormattingTest 更新**

```bash
git add src/test/java/org/dromara/easytl/exception/RuntimeExceptionFormattingTest.java
git commit -m "test: update RuntimeExceptionFormattingTest to use ErrorPointerAssert

- Replace loose pointer checks with strict validation
- Validate exact pointer position for all runtime errors"
```

---

### Task 5: 更新 EtlURLLazyEvaluationTest

**Files:**
- Modify: `src/test/java/org/dromara/easytl/runtime/EtlURLLazyEvaluationTest.java`

- [ ] **Step 1: 添加 ErrorPointerAssert 导入**

```java
import org.dromara.easytl.testutil.ErrorPointerAssert;
```

- [ ] **Step 2: 更新 testError_NullAccess_PropertyAccess_ShowsPosition 测试**

```java
@Test
public void testError_NullAccess_PropertyAccess_ShowsPosition() {
    Context ctx = new Context();
    ctx.put("user", null);

    EtlURL url = Eval.evalAsURL("http://{user.name}/path", ctx);

    TemplateRuntimeException ex = assertThrows(
        TemplateRuntimeException.class,
        () -> url.getHost()
    );

    ErrorPointerAssert.assertErrorPointer(ex,
        "http://{user.name}/path",
        "            ^^^^",
        "Cannot access property 'name' of null"
    );
}
```

- [ ] **Step 3: 更新其他错误位置测试**

逐一更新以下测试：
- `testError_UndefinedVariable_PropertyAccess_ShowsPosition`
- `testError_UndefinedVariable_InPath_ShowsPosition`
- `testError_NullAccess_ChainedAccess_ShowsPosition`
- `testError_NullAccess_MethodCallOnNull_ShowsPosition`
- `testError_IndexAccess_ArrayOutOfBounds_ShowsPosition`
- `testError_IndexAccess_NullArray_ShowsPosition`
- `testError_MethodCall_MethodNotExist_ShowsPosition`
- `testError_MultiLine_ShowsCorrectLine`

- [ ] **Step 4: 运行全部 EtlURLLazyEvaluationTest 测试**

Run: `mvn test -Dtest=EtlURLLazyEvaluationTest -q`

Expected: 所有测试通过

- [ ] **Step 5: 提交 EtlURLLazyEvaluationTest 更新**

```bash
git add src/test/java/org/dromara/easytl/runtime/EtlURLLazyEvaluationTest.java
git commit -m "test: update EtlURLLazyEvaluationTest to use ErrorPointerAssert

- Replace loose pointer checks with strict validation
- Validate exact pointer position for URL expression errors"
```

---

### Task 6: 更新 TemplateExceptionTest

**Files:**
- Modify: `src/test/java/org/dromara/easytl/exception/TemplateExceptionTest.java`

- [ ] **Step 1: 添加 ErrorPointerAssert 导入**

```java
import org.dromara.easytl.testutil.ErrorPointerAssert;
```

- [ ] **Step 2: 更新 testTemplateRuntimeExceptionWithPosition 测试**

对于带有源码的位置测试，添加严格验证：

```java
@Test
void testTemplateRuntimeExceptionRustStyle() {
    String sourceText = "line1\nline2\nHello {user.name}\nline4";
    TemplateRuntimeException ex = new TemplateRuntimeException(
        "Cannot access property 'name' of null", 3, 14, 3, 18, sourceText);

    ErrorPointerAssert.assertErrorPointer(ex,
        "Hello {user.name}",
        "            ^^^^",
        "Cannot access property 'name' of null"
    );
}
```

- [ ] **Step 3: 运行 TemplateExceptionTest 测试**

Run: `mvn test -Dtest=TemplateExceptionTest -q`

Expected: 所有测试通过

- [ ] **Step 4: 提交 TemplateExceptionTest 更新**

```bash
git add src/test/java/org/dromara/easytl/exception/TemplateExceptionTest.java
git commit -m "test: update TemplateExceptionTest to use ErrorPointerAssert

- Add strict pointer validation for Rust-style error tests"
```

---

### Task 7: 运行全部测试并提交

- [ ] **Step 1: 运行全部测试**

Run: `mvn test -q`

Expected: 所有测试通过

- [ ] **Step 2: 最终提交（如有遗漏）**

检查是否有未提交的更改：

Run: `git status`

如有未提交文件，补充提交。

---

## 验收标准

- [ ] ErrorPointerAssert 工具类创建完成，包含完整测试
- [ ] assertErrorPointer 方法实现，验证源码、位置、长度、消息
- [ ] 断言失败输出 Rust 风格的 Expected/Actual 对比
- [ ] SyntaxErrorTest 所有测试改用新验证方法
- [ ] RuntimeExceptionFormattingTest 所有测试改用新验证方法
- [ ] EtlURLLazyEvaluationTest 所有测试改用新验证方法
- [ ] TemplateExceptionTest 相关测试改用新验证方法
- [ ] 所有测试通过