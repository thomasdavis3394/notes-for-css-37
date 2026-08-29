# Fix Duplicate Location Info Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 修复编译错误信息中位置信息重复显示的问题，确保位置信息块只出现一次。

**Architecture:** 采用延迟格式化方案——异常类存储原始消息，getMessage() 时执行格式化，避免在异常构造链中多次格式化。

**Tech Stack:** Java 8, JUnit 5, Maven

---

## File Structure

| 文件 | 责任 |
|---|---|
| `src/main/java/org/dromara/easytl/exception/TemplateException.java` | 基类：存储 rawMessage/errorType/cachedMessage，实现 getMessage() 延迟格式化，新增 getRawMessage() |
| `src/main/java/org/dromara/easytl/exception/TemplateSyntaxException.java` | 语法异常：修改构造函数传入原始消息，不预格式化 |
| `src/main/java/org/dromara/easytl/exception/TemplateCompileException.java` | 编译异常：确认构造函数传入原始消息 |
| `src/main/java/org/dromara/easytl/TemplateEngine.java` | 异常捕获：使用 getRawMessage() 替代 getMessage() |
| `src/test/java/org/dromara/easytl/testutil/ErrorPointerAssert.java` | 测试工具：添加位置信息块计数验证 |

---

## Task 1: TemplateException 基类修改

**Files:**
- Modify: `src/main/java/org/dromara/easytl/exception/TemplateException.java`

- [ ] **Step 1: 添加新字段**

在 TemplateException 类中添加三个新字段：rawMessage、errorType、cachedMessage。

```java
public class TemplateException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String rawMessage;      // 原始错误消息（不含位置信息）
    private final String errorType;       // 错误类型（如 "Syntax error"）
    private final Position position;
    private final Source source;
    private String cachedMessage;         // 缓存的格式化消息（延迟计算）
```

- [ ] **Step 2: 修改不带位置信息的构造函数**

修改 `TemplateException(String message)` 构造函数，设置 rawMessage 和 cachedMessage。

```java
public TemplateException(final String message) {
    super(message);
    this.rawMessage = message;
    this.errorType = null;
    this.position = null;
    this.source = null;
    this.cachedMessage = message;
}
```

- [ ] **Step 3: 修改带原因异常的构造函数**

修改 `TemplateException(String message, Throwable cause)` 构造函数。

```java
public TemplateException(final String message, final Throwable cause) {
    super(message, cause);
    this.rawMessage = message;
    this.errorType = null;
    this.position = null;
    this.source = null;
    this.cachedMessage = message;
}
```

- [ ] **Step 4: 修改仅带原因异常的构造函数**

修改 `TemplateException(Throwable cause)` 构造函数。

```java
public TemplateException(final Throwable cause) {
    super(cause);
    this.rawMessage = cause != null ? cause.getMessage() : null;
    this.errorType = null;
    this.position = null;
    this.source = null;
    this.cachedMessage = this.rawMessage;
}
```

- [ ] **Step 5: 修改带位置和源码的构造函数**

修改 `TemplateException(String message, Position position, Source source)` 构造函数，使用 errorType 参数版本。

```java
protected TemplateException(final String message, final Position position, final Source source) {
    this(message, "Template error", position, source);
}
```

- [ ] **Step 6: 修改带 errorType 的构造函数（核心构造函数）**

修改带 errorType 参数的构造函数，实现延迟格式化。

```java
protected TemplateException(final String message, final String errorType,
                           final Position position, final Source source) {
    // 不调用 super() 格式化消息，延迟到 getMessage()
    this.rawMessage = message;
    this.errorType = errorType;
    this.position = position;
    this.source = source;
    this.cachedMessage = null;  // 延迟计算
}
```

- [ ] **Step 7: 修改带原因异常和位置信息的构造函数**

修改 `TemplateException(String message, Throwable cause, Position position, Source source)` 构造函数。

```java
protected TemplateException(final String message, final Throwable cause,
                           final Position position, final Source source) {
    this(message, "Template error", cause, position, source);
}
```

- [ ] **Step 8: 修改带 errorType、原因异常和位置信息的构造函数**

修改带完整参数的构造函数。

```java
protected TemplateException(final String message, final String errorType, final Throwable cause,
                           final Position position, final Source source) {
    super(message, cause);  // 仅传递原始消息给 super()
    this.rawMessage = message;
    this.errorType = errorType;
    this.position = position;
    this.source = source;
    this.cachedMessage = null;  // 延迟计算
}
```

- [ ] **Step 9: 添加 formatMessage() 私有方法**

添加私有方法执行延迟格式化。

```java
private String formatMessage() {
    if (position != null && source != null && errorType != null) {
        return ErrorMessageFormatter.formatMessage(
            errorType, rawMessage, position, source);
    }
    return rawMessage;
}
```

- [ ] **Step 10: 重写 getMessage() 方法**

重写 getMessage() 实现延迟格式化。

```java
@Override
public String getMessage() {
    if (cachedMessage == null) {
        cachedMessage = formatMessage();
    }
    return cachedMessage;
}
```

- [ ] **Step 11: 添加 getRawMessage() 方法**

添加 getRawMessage() 方法用于获取原始消息。

```java
/**
 * 获取原始错误消息（不含位置信息）
 *
 * @return 原始错误消息
 */
public String getRawMessage() {
    return rawMessage;
}
```

- [ ] **Step 12: 运行编译验证**

Run: `mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 13: Commit TemplateException 修改**

```bash
git add src/main/java/org/dromara/easytl/exception/TemplateException.java
git commit -m "refactor(exception): implement lazy formatting in TemplateException base class"
```

---

## Task 2: TemplateSyntaxException 修改

**Files:**
- Modify: `src/main/java/org/dromara/easytl/exception/TemplateSyntaxException.java`

- [ ] **Step 1: 添加 SYNTAX_ERROR 常量**

在类顶部添加错误类型常量。

```java
public class TemplateSyntaxException extends TemplateException {

    private static final long serialVersionUID = 1L;
    private static final String SYNTAX_ERROR = "Syntax error";
```

- [ ] **Step 2: 修改无位置信息构造函数**

修改 `TemplateSyntaxException(String message)` 构造函数。

```java
public TemplateSyntaxException(final String message) {
    super(message);  // 调用父类无位置信息构造函数
}
```

- [ ] **Step 3: 修改主构造函数（带 Position 和 Source）**

修改 `TemplateSyntaxException(String message, Position position, Source source)` 构造函数，传入原始消息。

```java
public TemplateSyntaxException(final String message, final Position position, final Source source) {
    super(message, SYNTAX_ERROR, position, source);  // 直接传原始消息，不预格式化
}
```

- [ ] **Step 4: 修改带 Position 构造函数**

修改 `TemplateSyntaxException(String message, Position position)` 构造函数。

```java
public TemplateSyntaxException(final String message, final Position position) {
    this(message, position, null);
}
```

- [ ] **Step 5: 修改带 line/column 构造函数**

修改 `TemplateSyntaxException(String message, int line, int column)` 构造函数。

```java
public TemplateSyntaxException(final String message, final int line, final int column) {
    this(message, new Position(line, column, line, column), null);
}
```

- [ ] **Step 6: 修改带 line/column/sourceText 构造函数**

修改 `TemplateSyntaxException(String message, int line, int column, String sourceText)` 构造函数。

```java
public TemplateSyntaxException(final String message, final int line, final int column, final String sourceText) {
    this(message, new Position(line, column, line, column),
         sourceText != null ? new StringSource(sourceText) : null);
}
```

- [ ] **Step 7: 修改带范围位置构造函数**

修改 `TemplateSyntaxException(String message, int startLine, int startColumn, int endLine, int endColumn, String sourceText)` 构造函数。

```java
public TemplateSyntaxException(final String message, final int startLine, final int startColumn,
                              final int endLine, final int endColumn, final String sourceText) {
    this(message, new Position(startLine, startColumn, endLine, endColumn),
         sourceText != null ? new StringSource(sourceText) : null);
}
```

- [ ] **Step 8: 修改带原因异常构造函数**

修改 `TemplateSyntaxException(String message, Throwable cause)` 构造函数。

```java
public TemplateSyntaxException(final String message, final Throwable cause) {
    super(message, cause);
}
```

- [ ] **Step 9: 运行编译验证**

Run: `mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 10: Commit TemplateSyntaxException 修改**

```bash
git add src/main/java/org/dromara/easytl/exception/TemplateSyntaxException.java
git commit -m "refactor(exception): remove pre-formatting in TemplateSyntaxException constructors"
```

---

## Task 3: TemplateCompileException 确认

**Files:**
- Modify: `src/main/java/org/dromara/easytl/exception/TemplateCompileException.java`

- [ ] **Step 1: 添加 COMPILE_ERROR 常量**

在类顶部添加错误类型常量。

```java
public class TemplateCompileException extends TemplateException {

    private static final long serialVersionUID = 1L;
    private static final String COMPILE_ERROR = "Compile error";
```

- [ ] **Step 2: 确认无位置信息构造函数**

确认 `TemplateCompileException(String message)` 构造函数正确（保持不变）。

```java
public TemplateCompileException(final String message) {
    super(message);
}
```

- [ ] **Step 3: 确认带原因异常构造函数**

确认 `TemplateCompileException(String message, Throwable cause)` 构造函数正确（保持不变）。

```java
public TemplateCompileException(final String message, final Throwable cause) {
    super(message, cause);
}
```

- [ ] **Step 4: 确认仅带原因异常构造函数**

确认 `TemplateCompileException(Throwable cause)` 构造函数正确（保持不变）。

```java
public TemplateCompileException(final Throwable cause) {
    super(cause);
}
```

- [ ] **Step 5: 确认带位置信息构造函数**

确认 `TemplateCompileException(String message, Position position, Source source)` 构造函数正确（传入原始消息）。

```java
public TemplateCompileException(final String message, final Position position, final Source source) {
    super(message, COMPILE_ERROR, position, source);
}
```

- [ ] **Step 6: 运行编译验证**

Run: `mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 7: Commit TemplateCompileException 修改**

```bash
git add src/main/java/org/dromara/easytl/exception/TemplateCompileException.java
git commit -m "refactor(exception): add COMPILE_ERROR constant to TemplateCompileException"
```

---

## Task 4: TemplateEngine.compileInternal 修改

**Files:**
- Modify: `src/main/java/org/dromara/easytl/TemplateEngine.java:318-346`

- [ ] **Step 1: 修改 compileInternal 异常捕获逻辑**

修改 `compileInternal` 方法中的 `catch (TemplateException e)` 分支，使用 `getRawMessage()`。

```java
private Template compileInternal(final String template, Map<String, Class<?>> importsMap) {
    try {
        // 1. 词法分析 (Lexer)
        StringSource source = new StringSource(template);
        Lexer lexer = new Lexer(source);
        TokenStream tokenStream = new TokenStream(lexer);

        // 2. 语法解析 (Parser)
        Parser parser = new Parser(tokenStream, template);
        TemplateNode ast = parser.parse();

        TemplateJavaImportValidator.validate(ast, config.isTemplateJavaClassImportEnabled(), template);

        // 3. 编译为 Template 对象（传入模板加载器以支持 extends 功能，传入源代码以支持错误报告）
        Compiler compiler = new InterpreterCompiler(
            templateLoader,
            template,
            config.isTemplateJavaClassImportEnabled(),
            importsMap);
        return compiler.compile(ast);
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

- [ ] **Step 2: 运行编译验证**

Run: `mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit TemplateEngine 修改**

```bash
git add src/main/java/org/dromara/easytl/TemplateEngine.java
git commit -m "fix(engine): use getRawMessage() to avoid duplicate formatting in compileInternal"
```

---

## Task 5: ErrorPointerAssert 添加位置信息计数验证

**Files:**
- Modify: `src/test/java/org/dromara/easytl/testutil/ErrorPointerAssert.java`

- [ ] **Step 1: 添加 countLocationBlocks 私有方法**

在 ErrorPointerAssert 类末尾添加统计方法。

```java
/**
 * 统计错误消息中位置信息块的数量
 * 指针行特征：以 "  | " 开头，包含 "^"
 *
 * @param message 错误消息
 * @return 位置信息块数量
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

- [ ] **Step 2: 在 assertErrorPointer 中添加验证**

在 `assertErrorPointer` 方法中，第 7 步（验证错误消息包含）之后，添加第 8 步验证位置信息只出现一次。

```java
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

// 8. 验证位置信息只出现一次（新增）
int locationBlockCount = countLocationBlocks(actualMessage);
if (locationBlockCount != 1) {
    throw new AssertionError(
        "Location info should appear exactly once, but found " + locationBlockCount + " times.\n" +
        "Full error message:\n" + actualMessage
    );
}

System.out.println();
System.out.println(exception.getMessage());
```

- [ ] **Step 3: 运行测试验证**

Run: `mvn test -Dtest=CompileErrorTest#testUnexpectedTokenInExpression -q`
Expected: 测试通过，输出显示位置信息只出现一次

- [ ] **Step 4: Commit ErrorPointerAssert 修改**

```bash
git add src/test/java/org/dromara/easytl/testutil/ErrorPointerAssert.java
git commit -m "test(util): add location block count validation in ErrorPointerAssert"
```

---

## Task 6: 运行全部测试确保功能完整

**Files:**
- None (验证任务)

- [ ] **Step 1: 运行 CompileErrorTest 全部测试**

Run: `mvn test -Dtest=CompileErrorTest -q`
Expected: 所有测试通过，每个错误消息位置信息只出现一次

- [ ] **Step 2: 运行 ErrorMessageFormatterTest 测试**

Run: `mvn test -Dtest=ErrorMessageFormatterTest -q`
Expected: 所有测试通过

- [ ] **Step 3: 运行全部测试**

Run: `mvn test -q`
Expected: BUILD SUCCESS, 所有测试通过

---

## Task 7: 最终提交

**Files:**
- None (清理任务)

- [ ] **Step 1: 检查 git 状态**

Run: `git status`
Expected: 所有修改已提交

- [ ] **Step 2: 查看提交历史**

Run: `git log --oneline -5`
Expected: 显示本次修改的提交记录

---

## Spec Coverage Check

| Spec 要求 | Task |
|---|---|
| TemplateException 添加 rawMessage/errorType 字段 | Task 1 Step 1 |
| TemplateException getMessage() 延迟格式化 | Task 1 Step 9-10 |
| TemplateException 新增 getRawMessage() | Task 1 Step 11 |
| TemplateSyntaxException 传入原始消息 | Task 2 Step 3 |
| TemplateCompileException 确认构造函数 | Task 3 |
| TemplateEngine 使用 getRawMessage() | Task 4 Step 1 |
| ErrorPointerAssert 验证位置信息只出现一次 | Task 5 Step 1-2 |

**覆盖完整。**