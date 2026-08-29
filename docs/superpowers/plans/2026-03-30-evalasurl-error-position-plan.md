# evalAsURL 错误位置优化实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 优化 `Eval.evalAsURL()` 的错误位置显示，使错误信息指向原始 URL 内容而非 `url(...)` 包装后的源码。

**Architecture:** 在 Parser 层新增 `parseEvalURLSource` 方法直接解析 URL 内容，Lexer 新增 URL 模式启动参数，无需包装源码即可正确解析 URL 模板。

**Tech Stack:** Java 8, Maven, JUnit 5

---

## Files to Create/Modify

| File | Action | Purpose |
|------|--------|---------|
| `src/main/java/org/dromara/easytl/parser/Lexer.java` | Modify | 新增 urlMode 构造参数 |
| `src/main/java/org/dromara/easytl/ast/UrlLiteral.java` | Modify | 新增 Position 构造函数 |
| `src/main/java/org/dromara/easytl/parser/Parser.java` | Modify | 新增 parseEvalURLSource 方法 |
| `src/main/java/org/dromara/easytl/Eval.java` | Modify | 修改 evalAsURL 检测包装并调用新方法 |
| `src/test/java/org/dromara/easytl/runtime/EvalAsURLTest.java` | Create | 新增测试文件验证错误位置 |

---

### Task 1: Lexer 新增 URL 模式构造函数

**Files:**
- Modify: `src/main/java/org/dromara/easytl/parser/Lexer.java:85-110`

- [ ] **Step 1: 新增三参数构造函数**

在 `Lexer.java` 现有构造函数后添加：

```java
/**
 * 构造一个 Lexer，可选择模式
 *
 * @param source 模板源码
 * @param evalMode 是否以Eval模式开始（用于 Eval.eval() 等纯表达式解析场景）
 * @param urlMode 是否以URL模式开始（用于 Eval.evalAsURL() 场景）
 */
public Lexer(Source source, boolean evalMode, boolean urlMode) {
    this.source = source;
    if (urlMode) {
        this.mode = Mode.URL;
        this.urlParenDepth = 0; // 不跟踪括号深度，因为没有外层 url(...)
        this.inUrlContext = true;
    } else if (evalMode) {
        this.mode = Mode.EVAL;
    } else {
        this.mode = Mode.TEXT;
    }
    this.braceDepth = 0;
    this.doubleBrace = false;
    this.dollarBrace = false;
    this.pendingLeftBrace = false;
    this.lastExpressionTokenType = null;
    this.previousMode = null;
    this.savedBraceDepth = 0;
}
```

- [ ] **Step 2: 编译验证**

Run: `mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add src/main/java/org/dromara/easytl/parser/Lexer.java
git commit -m "feat: add urlMode parameter to Lexer constructor"
```

---

### Task 2: UrlLiteral 新增 Position 构造函数

**Files:**
- Modify: `src/main/java/org/dromara/easytl/ast/UrlLiteral.java`

- [ ] **Step 1: 新增 Position 构造函数**

修改 `UrlLiteral.java`：

```java
package org.dromara.easytl.ast;

import org.dromara.easytl.parser.Token;

import java.util.List;

/**
 * URL 字面量 AST 节点
 * 表示 url(...) 构造，支持嵌入表达式和语句块
 *
 * @author EasyTL Team
 */
public class UrlLiteral extends ASTNode {

    /** 子节点列表：TextNode、ExpressionNode、StatementBlock */
    private final List<ASTNode> children;

    public UrlLiteral(List<ASTNode> children) {
        this.children = children;
    }

    public UrlLiteral(List<ASTNode> children, Token token) {
        super(positionFromToken(token));
        this.children = children;
    }

    /**
     * 构造 UrlLiteral，直接指定 Position
     *
     * @param children 子节点列表
     * @param position 位置信息
     */
    public UrlLiteral(List<ASTNode> children, Position position) {
        super(position);
        this.children = children;
    }

    public List<ASTNode> getChildren() {
        return children;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitUrlLiteral(this);
    }

    @Override
    public String toString() {
        return "UrlLiteral{" +
                "children=" + children +
                '}';
    }
}
```

- [ ] **Step 2: 编译验证**

Run: `mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add src/main/java/org/dromara/easytl/ast/UrlLiteral.java
git commit -m "feat: add Position constructor to UrlLiteral"
```

---

### Task 3: Parser 新增 parseEvalURLSource 方法

**Files:**
- Modify: `src/main/java/org/dromara/easytl/parser/Parser.java`

- [ ] **Step 1: 新增 parseEvalURLSource 方法**

在 `Parser.java` 的 `parseEvalSource()` 方法后添加：

```java
/**
 * 解析 URL 源码（不需要 url(...) 包装）
 *
 * <p>直接解析 URL 内容，返回 UrlLiteral AST 节点。
 * 此方法用于 Eval.evalAsURL()，使错误位置指向原始源码。
 * </p>
 *
 * @return UrlLiteral AST 节点
 */
public UrlLiteral parseEvalURLSource() {
    List<ASTNode> children = new ArrayList<>();

    // 记录起始位置（用于 UrlLiteral 的 Position）
    int startLine = current() != null ? current().getLine() : 1;
    int startColumn = current() != null ? current().getStartColumn() : 1;

    while (!check(TokenType.EOF)) {
        if (check(TokenType.SINGLE_BRACE_EXPR_BEGIN)) {
            children.add(parseExpressionWrapper());
        } else if (check(TokenType.DOUBLE_BRACE_EXPR_BEGIN)) {
            children.add(parseExpressionWrapper());
        } else if (check(TokenType.DOLLAR_BRACE_EXPR_BEGIN)) {
            children.add(parseExpressionWrapper());
        } else if (check(TokenType.STMT_BEGIN)) {
            children.add(parseStatementBlock());
        } else if (check(TokenType.TEXT)) {
            children.add(new TextNode(advance().getValue()));
        } else {
            // URL 内容解析结束（EOF）
            break;
        }
    }

    // 计算结束位置
    int endLine = 1;
    int endColumn = 1;
    if (!children.isEmpty()) {
        ASTNode last = children.get(children.size() - 1);
        endLine = last.getEndLine();
        endColumn = last.getEndColumn();
    }

    Position position = new Position(startLine, startColumn, endLine, endColumn);
    return new UrlLiteral(children, position);
}
```

- [ ] **Step 2: 编译验证**

Run: `mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add src/main/java/org/dromara/easytl/parser/Parser.java
git commit -m "feat: add parseEvalURLSource method to Parser"
```

---

### Task 4: 创建错误位置测试

**Files:**
- Create: `src/test/java/org/dromara/easytl/runtime/EvalAsURLTest.java`

- [ ] **Step 1: 创建测试文件**

```java
package org.dromara.easytl.runtime;

import org.dromara.easytl.Context;
import org.dromara.easytl.Eval;
import org.dromara.easytl.exception.TemplateRuntimeException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Eval.evalAsURL 错误位置测试
 */
public class EvalAsURLTest {

    // ==================== 错误位置验证测试 ====================

    /**
     * 测试：错误消息应显示原始源码（不包含 url(...) 包装）
     */
    @Test
    public void testErrorPosition_NoWrapper_ShowsOriginalSource() {
        Context ctx = new Context();
        ctx.put("user", null);

        EtlURL url = Eval.evalAsURL("http://{user.name}/path", ctx);

        TemplateRuntimeException ex = assertThrows(
            TemplateRuntimeException.class,
            () -> url.getHost()
        );

        // 验证源码是原始的（不包含 url(...) 包装）
        assertEquals("http://{user.name}/path", ex.getSourceText(),
            "源码应为原始内容，不包含 url(...) 包装");

        // 验证位置指向 {user.name}
        assertTrue(ex.getStartColumn() >= 8,
            "列号应指向 {user.name} 开始位置: " + ex.getStartColumn());
    }

    /**
     * 测试：多行 URL 错误应指向正确的行
     */
    @Test
    public void testErrorPosition_MultiLine_ShowsCorrectLine() {
        Context ctx = new Context();
        String multiLineUrl = "http://host/path1\n/{undefinedVar}/path2";

        EtlURL url = Eval.evalAsURL(multiLineUrl, ctx);

        TemplateRuntimeException ex = assertThrows(
            TemplateRuntimeException.class,
            () -> url.getPath()
        );

        // 验证错误在第2行
        assertEquals(2, ex.getStartLine(),
            "错误应在第2行: " + ex.getMessage());

        // 验证源码是原始的
        assertTrue(ex.getSourceText().contains("http://host/path1"),
            "源码应包含第一行内容");
    }

    /**
     * 测试：错误消息中的源码显示应正确格式化
     */
    @Test
    public void testErrorPosition_VisualFormat_ShowsCorrectPosition() {
        Context ctx = new Context();
        ctx.put("user", null);

        EtlURL url = Eval.evalAsURL("http://{user.name}/path", ctx);

        TemplateRuntimeException ex = assertThrows(
            TemplateRuntimeException.class,
            () -> url.getHost()
        );

        String message = ex.getMessage();

        // 验证错误消息包含源码行
        assertTrue(message.contains("http://{user.name}/path") || ex.getSourceText() != null,
            "错误消息应包含原始源码");

        // 验证不包含 url(...) 包装
        assertFalse(message.contains("url(http://") || message.contains("url (http://"),
            "错误消息不应包含 url(...) 包装: " + message);
    }

    // ==================== 包装与非包装兼容性测试 ====================

    /**
     * 测试：使用 url(...) 包装和非包装结果应相同
     */
    @Test
    public void testEvalAsURL_WithWrapper_WorksAsExpected() {
        Context ctx = new Context();
        ctx.put("host", "example.com");

        // 显式使用 url(...) 包装
        EtlURL url1 = Eval.evalAsURL("url(http://{host}/path)", ctx);
        // 不使用包装
        EtlURL url2 = Eval.evalAsURL("http://{host}/path", ctx);

        // 两者结果应相同
        assertEquals(url1.toString(), url2.toString(),
            "包装和非包装结果应相同");
    }

    /**
     * 测试：不带包装的简单 URL
     */
    @Test
    public void testEvalAsURL_NoWrapper_SimpleUrl() {
        Context ctx = new Context();
        ctx.put("host", "api.example.com");
        ctx.put("path", "/v1/users");

        EtlURL url = Eval.evalAsURL("http://{host}{path}", ctx);

        assertEquals("http", url.getScheme());
        assertEquals("api.example.com", url.getHost());
        assertEquals("/v1/users", url.getPath());
        assertEquals("http://api.example.com/v1/users", url.toString());
    }

    // ==================== 测试辅助类 ====================

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
}
```

- [ ] **Step 2: 运行测试验证失败**

Run: `mvn test -Dtest=EvalAsURLTest -q`
Expected: 测试失败（功能尚未实现）

- [ ] **Step 3: Commit**

```bash
git add src/test/java/org/dromara/easytl/runtime/EvalAsURLTest.java
git commit -m "test: add error position tests for evalAsURL"
```

---

### Task 5: 修改 Eval.evalAsURL 方法

**Files:**
- Modify: `src/main/java/org/dromara/easytl/Eval.java`

- [ ] **Step 1: 修改 evalAsURL(Context) 方法**

找到 `evalAsURL(String source, Context context)` 方法（约第438-461行），修改为：

```java
/**
 * 使用指定上下文执行表达式并返回 EtlURL
 * <p>
 * 此方法支持两种输入格式：
 * <ul>
 *   <li>直接 URL 内容：如 "http://{host}/path"</li>
 *   <li>url(...) 包装：如 "url(http://{host}/path)"</li>
 * </ul>
 * 推荐使用第一种格式，错误位置将指向原始源码。
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

    // 检测是否包含 url(...) 包装
    boolean isWrapped = source.startsWith("url(") && source.endsWith(")");

    if (isWrapped) {
        // 用户显式使用了 url(...) 包装，走现有流程
        Object result = eval(source, context, EtlURL.class);
        return (EtlURL) result;
    } else {
        // 用户没有使用 url(...) 包装，直接解析 URL 内容
        StringSource stringSource = new StringSource(source);
        Lexer lexer = new Lexer(stringSource, false, true); // evalMode=false, urlMode=true
        TokenStream tokenStream = new TokenStream(lexer);
        Parser parser = new Parser(tokenStream, source);
        UrlLiteral urlLiteral = parser.parseEvalURLSource();

        // 执行 AST，传入原始源码
        Evaluator evaluator = new Evaluator(context, null, source);
        return (EtlURL) urlLiteral.accept(evaluator);
    }
}
```

- [ ] **Step 2: 添加必要的 import**

确保 `Eval.java` 顶部有以下 import：

```java
import org.dromara.easytl.ast.UrlLiteral;
import org.dromara.easytl.parser.Lexer;
import org.dromara.easytl.parser.Parser;
import org.dromara.easytl.parser.TokenStream;
```

- [ ] **Step 3: 修改 evalAsURL(无 Context) 方法**

找到 `evalAsURL(String source)` 方法（约第285-304行），修改为：

```java
/**
 * 执行表达式并返回 EtlURL
 * <p>
 * 此方法支持两种输入格式：
 * <ul>
 *   <li>直接 URL 内容：如 "http://{host}/path"</li>
 *   <li>url(...) 包装：如 "url(http://{host}/path)"</li>
 * </ul>
 * 推荐使用第一种格式，错误位置将指向原始源码。
 * </p>
 *
 * @param source URL 源码
 * @return EtlURL 结果
 */
public static EtlURL evalAsURL(String source) {
    if (source == null) {
        throw new IllegalArgumentException("Source cannot be null");
    }

    if (source.trim().isEmpty()) {
        return null;
    }

    // 检测是否包含 url(...) 包装
    boolean isWrapped = source.startsWith("url(") && source.endsWith(")");

    if (isWrapped) {
        // 用户显式使用了 url(...) 包装，走现有流程
        Object result = eval(source, EtlURL.class);
        return (EtlURL) result;
    } else {
        // 用户没有使用 url(...) 包装，直接解析 URL 内容
        StringSource stringSource = new StringSource(source);
        Lexer lexer = new Lexer(stringSource, false, true); // evalMode=false, urlMode=true
        TokenStream tokenStream = new TokenStream(lexer);
        Parser parser = new Parser(tokenStream, source);
        UrlLiteral urlLiteral = parser.parseEvalURLSource();

        // 执行 AST，传入原始源码
        Context context = new Context();
        Evaluator evaluator = new Evaluator(context, null, source);
        return (EtlURL) urlLiteral.accept(evaluator);
    }
}
```

- [ ] **Step 4: 删除 adjustErrorPosition 方法**

删除 `adjustErrorPosition` 方法（约第552-565行），因为不再需要位置调整：

```java
// 删除此方法 - 不再需要
```

同时删除 `URL_PREFIX_LENGTH` 常量：

```java
// 删除此常量 - 不再需要
private static final int URL_PREFIX_LENGTH = 4; // "url(".length()
```

- [ ] **Step 5: 编译验证**

Run: `mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 6: Commit**

```bash
git add src/main/java/org/dromara/easytl/Eval.java
git commit -m "feat: modify evalAsURL to parse URL content directly without wrapper"
```

---

### Task 6: 运行测试验证

**Files:**
- Test: `src/test/java/org/dromara/easytl/runtime/EvalAsURLTest.java`
- Test: `src/test/java/org/dromara/easytl/runtime/EtlURLLazyEvaluationTest.java`

- [ ] **Step 1: 运行新测试**

Run: `mvn test -Dtest=EvalAsURLTest -q`
Expected: 所有测试通过

- [ ] **Step 2: 运行现有测试确保不破坏功能**

Run: `mvn test -Dtest=EtlURLLazyEvaluationTest -q`
Expected: 所有测试通过

- [ ] **Step 3: 运行全部测试**

Run: `mvn test -q`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add -A
git commit -m "test: verify evalAsURL error position optimization"
```

---

### Task 7: 更新 EtlURLLazyEvaluationTest 验证源码

**Files:**
- Modify: `src/test/java/org/dromara/easytl/runtime/EtlURLLazyEvaluationTest.java`

- [ ] **Step 1: 更新测试验证源码不包含 url(...) 包装**

在 `testError_VisualPointer_ShowsInMessage` 方法中添加验证：

```java
/**
 * 测试：错误消息应包含可视化指针（Rust 风格）
 *
 * 表达式: http://{user.name}/path
 * Context: user=null
 * 期望: 错误消息包含 ^ 指针，且源码不包含 url(...) 包装
 */
@Test
public void testError_VisualPointer_ShowsInMessage() {
    Context ctx = new Context();
    ctx.put("user", null);

    EtlURL url = Eval.evalAsURL("http://{user.name}/path", ctx);

    TemplateRuntimeException ex = assertThrows(
        TemplateRuntimeException.class,
        () -> url.getHost()
    );

    // 验证错误消息格式
    String message = ex.getMessage();

    // 应包含错误类型
    assertTrue(message.contains("Runtime error") || message.contains("Cannot"),
        "应包含错误类型: " + message);

    // 验证源码不包含 url(...) 包装
    if (ex.getSourceText() != null) {
        assertEquals("http://{user.name}/path", ex.getSourceText(),
            "源码应为原始内容，不包含 url(...) 包装");
    }

    // 如果有源码，应包含可视化指针
    if (ex.getSourceText() != null) {
        assertTrue(message.contains("^") || message.contains(" |"),
            "有源码时应包含可视化指针: " + message);
    }
}
```

- [ ] **Step 2: 运行测试验证**

Run: `mvn test -Dtest=EtlURLLazyEvaluationTest -q`
Expected: 所有测试通过

- [ ] **Step 3: Commit**

```bash
git add src/test/java/org/dromara/easytl/runtime/EtlURLLazyEvaluationTest.java
git commit -m "test: update visual pointer test to verify original source"
```

---

## Self-Review

**1. Spec coverage:**
- ✅ Lexer 新增 urlMode 参数 - Task 1
- ✅ UrlLiteral 新增 Position 构造函数 - Task 2
- ✅ Parser 新增 parseEvalURLSource 方法 - Task 3
- ✅ Eval.evalAsURL 检测包装并调用新方法 - Task 5
- ✅ 测试验证错误位置 - Task 4, 6, 7

**2. Placeholder scan:**
- 无 "TBD", "TODO", "implement later" 等
- 所有代码步骤都包含完整代码
- 所有测试步骤都有具体断言

**3. Type consistency:**
- `Lexer(Source, boolean, boolean)` 构造函数在 Task 1 定义，Task 5 使用
- `parseEvalURLSource()` 方法返回 `UrlLiteral` 在 Task 3 定义，Task 5 使用
- `UrlLiteral(List<ASTNode>, Position)` 构造函数在 Task 2 定义，Task 3 使用

---

**Plan complete and saved to `docs/superpowers/plans/2026-03-30-evalasurl-error-position-plan.md`.**