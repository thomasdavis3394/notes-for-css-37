# evalAsURL 错误位置优化设计

## 概述

优化 `Eval.evalAsURL()` 方法的错误位置显示，使错误信息中的源码位置指向用户提供的原始 URL 内容，而非包装后的 `url(...)` 语法。

## 问题背景

当前 `Eval.evalAsURL()` 的实现会将用户提供的 URL 源码包装为 `url(...)` 语法：

```java
String wrappedSource = "url(" + source + ")";
```

这导致运行时错误报告中出现以下问题：

**当前行为（有问题）：**
```
用户输入: EasyTL.evalAsURL("http://{urlPath}")

错误显示:
1 | url(http://{urlPath})
          ^^^^^^^ Runtime error: Cannot access member 'urlPath' of null
```

**期望行为：**
```
用户输入: EasyTL.evalAsURL("http://{urlPath}")

错误显示:
1 | http://{urlPath}
        ^^^^^^^ Runtime error: Cannot access member 'urlPath' of null
```

问题在于：
1. 错误消息显示的是包装后的源码 `url(http://{urlPath})`，而非用户提供的原始源码
2. 位置信息（列号）偏移了 4 列（对应 `"url("` 前缀）
3. `adjustErrorPosition()` 方法虽有设计意图但未真正实现位置调整

## 设计方案

采用在 Parser 层解决问题的方案，新增 `parseEvalURLSource` 方法直接解析 URL 内容，无需包装源码。

### 1. Lexer 新增 URL 模式启动方式

为 Lexer 添加构造函数参数，允许直接以 URL 模式启动：

```java
/**
 * 构造一个 Lexer，以 URL 模式开始
 *
 * @param source 源码
 * @param urlMode 是否以 URL 模式开始
 */
public Lexer(Source source, boolean evalMode, boolean urlMode) {
    this.source = source;
    if (urlMode) {
        this.mode = Mode.URL;
        this.urlParenDepth = 0; // 不跟踪括号深度，因为没有外层 url(...)
    } else if (evalMode) {
        this.mode = Mode.EVAL;
    } else {
        this.mode = Mode.TEXT;
    }
    // ... 其他初始化
}
```

**关键点：**
- URL 模式下 `urlParenDepth = 0`，遇到 `)` 时不会退出 URL 模式（因为没有外层包装）
- 解析直到 EOF 结束

### 2. Parser 新增 `parseEvalURLSource` 方法

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

### 3. Eval.evalAsURL() 修改

```java
public static EtlURL evalAsURL(String source, Context context) {
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
        // 提取括号内的内容作为源码
        String innerSource = source.substring(4, source.length() - 1);
        Object result = eval(source, context, EtlURL.class);
        // 注意：这里错误位置可能仍然需要调整
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

### 4. UrlLiteral 支持自定义 Position

修改 `UrlLiteral` 类，支持通过 Position 对象构造：

```java
public class UrlLiteral extends ASTNode {
    private final List<ASTNode> children;

    // 现有构造函数（从 Token 获取位置）
    public UrlLiteral(List<ASTNode> children, Token beginToken) {
        super(new Position(beginToken.getLine(), beginToken.getStartColumn(),
                          beginToken.getLine(), beginToken.getEndColumn()));
        this.children = children;
    }

    // 新增构造函数（直接指定 Position）
    public UrlLiteral(List<ASTNode> children, Position position) {
        super(position);
        this.children = children;
    }
}
```

### 5. 移除位置调整逻辑

由于源码不再被包装，`adjustErrorPosition` 方法不再需要，可以删除或保留为空实现。

## 多行 URL 支持

此方案天然支持多行 URL 模板：

```
http://host/path1
/{undefinedVar}/path2
```

错误将正确指向第二行的 `{undefinedVar}`，无需特殊的位置调整逻辑。

## 实现步骤

1. **Lexer 修改**
   - 新增 `urlMode` 构造参数
   - URL 模式下 `urlParenDepth = 0`
   - URL 模式解析直到 EOF

2. **UrlLiteral 修改**
   - 新增 Position 参数构造函数

3. **Parser 修改**
   - 新增 `parseEvalURLSource` 方法

4. **Eval 修改**
   - 检测 `url(...)` 包装
   - 非包装时调用新方法
   - 删除或保留 `adjustErrorPosition`

5. **测试验证**
   - 运行现有测试确保功能不变
   - 新增错误位置验证测试

## 测试用例

### 错误位置验证

```java
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
    assertEquals("http://{user.name}/path", ex.getSourceText());

    // 验证位置指向 {user.name}
    assertTrue(ex.getStartColumn() >= 8,
        "列号应指向 {user.name} 开始位置: " + ex.getStartColumn());
}

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
```

### 包装与非包装兼容性

```java
@Test
public void testEvalAsURL_WithWrapper_WorksAsExpected() {
    Context ctx = new Context();
    ctx.put("host", "example.com");

    // 显式使用 url(...) 包装
    EtlURL url1 = Eval.evalAsURL("url(http://{host}/path)", ctx);
    // 不使用包装
    EtlURL url2 = Eval.evalAsURL("http://{host}/path", ctx);

    // 两者结果应相同
    assertEquals(url1.toString(), url2.toString());
}
```

## 相关文件

| 文件 | 修改内容 |
|------|---------|
| `src/main/java/org/dromara/easytl/parser/Lexer.java` | 新增 urlMode 参数 |
| `src/main/java/org/dromara/easytl/parser/Parser.java` | 新增 parseEvalURLSource 方法 |
| `src/main/java/org/dromara/easytl/ast/UrlLiteral.java` | 新增 Position 构造函数 |
| `src/main/java/org/dromara/easytl/Eval.java` | 修改 evalAsURL，删除 adjustErrorPosition |
| `src/test/java/org/dromara/easytl/runtime/EtlURLLazyEvaluationTest.java` | 验证错误位置 |

## 风险评估

- **低风险**：改动范围明确，不改变现有功能行为
- **向后兼容**：显式使用 `url(...)` 包装的场景保持原有行为
- **测试覆盖**：现有测试 + 新增错误位置测试确保正确性