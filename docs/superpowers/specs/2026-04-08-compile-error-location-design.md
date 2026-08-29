---
name: compile-error-location
description: Add source location information to compile-time errors
type: project
---

# Compile Error Location Design

## 背景

目前在 render 方法和 eval 方法执行过程中报的错误，都会包含代码定位信息，即 `^` 符号指向某行代码中某些列表示错误发生的源头。但在 compile 过程中报的错误还没有定位信息，需要补上。

## 目标

为 compile-time 错误添加与 syntax error 和 runtime error 相同的 Rust 风格位置信息显示。

## 最终效果示例

### Export 错误

**场景 1：重复导出变量**

模板代码：
```
{% export x %} {% export x %}
```

错误输出：
```
Compile error: Variable 'x' is already exported

     |
   1 | {% export x %} {% export x %}
     |                   ^^^^^^^^
```

**场景 2：重复导出变量（带 let）**

模板代码：
```
{% export let x = 1 %}
{% export let x = 2 %}
```

错误输出：
```
Compile error: Variable 'x' is already exported

     |
   2 | {% export let x = 2 %}
     |    ^^^^^^^^^^^^^^^^
```

**场景 3：多行模板中的重复导出**

模板代码：
```
Hello {% export name %}
World {% export name %}
```

错误输出：
```
Compile error: Variable 'name' is already exported

     |
   2 | World {% export name %}
     |          ^^^^^^^^^^^
```

### Import 错误

**场景 1：导入别名冲突**

模板代码：
```
{% let foo = 1 %}
{% import 'lib.etl' as foo %}
```

错误输出：
```
Compile error: Alias 'foo' conflicts with an existing variable

     |
   2 | {% import 'lib.etl' as foo %}
     |                        ^^^
```

**场景 2：导入模板解析失败**

模板代码：
```
{% import 'broken.etl' as lib %}
```

错误输出：
```
Compile error: Cannot import template 'broken.etl': Unclosed expression

     |
   1 | {% import 'broken.etl' as lib %}
     |    ^^^^^^^^^^^^^^^^^^^^^^^^
```

**场景 3：禁用 Java 类导入**

模板代码：
```
{% import java.util.List %}
```

错误输出：
```
Compile error: Template Java class import is disabled; use Context.importClass(...) instead.

     |
   1 | {% import java.util.List %}
     |    ^^^^^^^^^^^^^^^^^^^^^
```

## 设计方案

### 方案选择

选择**最小增强方案**，利用现有基础设施，改动最小。

### 实现细节

#### 1. ErrorMessageFormatter 增强

在 `ErrorMessageFormatter` 中添加 `COMPILE_ERROR` 常量和 `formatCompileError()` 方法：

```java
/** Error type constant for compile errors */
public static final String COMPILE_ERROR = "Compile error";

/**
 * Format a compile error message with Position and Source objects.
 *
 * @param message the error message
 * @param position the position information, may be null
 * @param source the source code, may be null
 * @return the formatted error message
 */
public static String formatCompileError(final String message, final Position position, final Source source) {
    return formatMessage(COMPILE_ERROR, message, position, source);
}
```

#### 2. TemplateCompileException 增强

更新构造函数使用 `formatCompileError()`：

```java
/**
 * 构造函数，带位置与源码（用于编译期错误定位）
 *
 * @param message  异常信息
 * @param position 位置信息
 * @param source   源码
 */
public TemplateCompileException(final String message, final Position position, final Source source) {
    super(ErrorMessageFormatter.formatCompileError(message, position, source), position, source);
}
```

#### 3. AST 节点位置信息增强

为相关 AST 节点添加 `keywordPosition` 字段，存储核心关键词的位置（不含 `{%` `%}`）。

**ExportStatement：**

```java
public class ExportStatement extends ASTNode {
    private final String variableName;
    private final LetStatement letStatement;
    private final Position keywordPosition; // 新增

    public Position getKeywordPosition() {
        return keywordPosition;
    }
}
```

**ImportTemplateStatement：**

```java
public class ImportTemplateStatement extends ASTNode {
    private final String templatePath;
    private final String alias;
    private final Position keywordPosition; // 新增

    public Position getKeywordPosition() {
        return keywordPosition;
    }
}
```

**ImportStatement：**

```java
public class ImportStatement extends ASTNode {
    private final String className;
    private final String alias;
    private final Position keywordPosition; // 新增

    public Position getKeywordPosition() {
        return keywordPosition;
    }
}
```

#### 4. Parser 修改

Parser 在解析语句时需要保存更多 Token 位置信息，构建 `keywordPosition`。

**parseExportStatement()：**

```java
private ASTNode parseExportStatement() {
    Token exportToken = current();
    expect(TokenType.EXPORT);

    if (check(TokenType.LET)) {
        Token letToken = current();
        advance();
        Token nameToken = expect(TokenType.IDENTIFIER);
        expect(TokenType.ASSIGN);
        ASTNode value = parseExpression();
        Token stmtEndToken = current();
        expect(TokenType.STMT_END);

        // keywordPosition: 从 export 到 value 结束
        Position keywordPos = new Position(
            exportToken.getLine(), exportToken.getStartColumn(),
            stmtEndToken.getLine(), stmtEndToken.getEndColumn()
        );

        LetStatement letStmt = new LetStatement(nameToken.getValue(), value, letToken);
        return new ExportStatement(nameToken.getValue(), letStmt, keywordPos);
    } else {
        Token nameToken = expect(TokenType.IDENTIFIER);
        expect(TokenType.STMT_END);

        // keywordPosition: export x 的位置
        Position keywordPos = new Position(
            exportToken.getLine(), exportToken.getStartColumn(),
            nameToken.getLine(), nameToken.getEndColumn()
        );

        return new ExportStatement(nameToken.getValue(), null, keywordPos);
    }
}
```

类似的修改适用于 `parseImportTemplateStatement()` 和 `parseImportStatement()`。

#### 5. Validator 修改

Validator 在抛出错误时，使用节点的 `keywordPosition` 替代 `getPosition()`。

**ExportValidator：**

```java
// 使用 keywordPosition
if (export.getKeywordPosition() != null && !export.getKeywordPosition().isUnknown()) {
    throw new TemplateCompileException(msg, export.getKeywordPosition(), new StringSource(sourceText));
}
```

**ImportTemplateValidator：**

```java
// 使用 keywordPosition
if (importStmt.getKeywordPosition() != null && !importStmt.getKeywordPosition().isUnknown()) {
    throw new TemplateCompileException(msg, importStmt.getKeywordPosition(), new StringSource(sourceText));
}
```

**TemplateJavaImportValidator：**

```java
// 使用 keywordPosition
if (node.getKeywordPosition() != null && !node.getKeywordPosition().isUnknown()) {
    throw new TemplateCompileException(msg, node.getKeywordPosition(), new StringSource(sourceText));
}
```

## 修改文件清单

| 文件 | 修改内容 |
|------|----------|
| `ErrorMessageFormatter.java` | 添加 `COMPILE_ERROR` 常量和 `formatCompileError()` 方法 |
| `TemplateCompileException.java` | 更新构造函数使用 `formatCompileError()` |
| `ExportStatement.java` | 添加 `keywordPosition` 字段和 getter，更新构造函数 |
| `ImportTemplateStatement.java` | 添加 `keywordPosition` 字段和 getter，更新构造函数 |
| `ImportStatement.java` | 添加 `keywordPosition` 字段和 getter，更新构造函数 |
| `Parser.java` | `parseExportStatement()`, `parseImportTemplateStatement()`, `parseImportStatement()` 计算并传递 `keywordPosition` |
| `ExportValidator.java` | 使用 `keywordPosition` 抛出错误 |
| `ImportTemplateValidator.java` | 使用 `keywordPosition` 抛出错误 |
| `TemplateJavaImportValidator.java` | 使用 `keywordPosition` 抛出错误 |

## 测试计划

1. 为每个错误场景编写测试用例
2. 使用 `ErrorPointerAssert` 验证指针位置正确
3. 测试多行模板中的错误定位
4. 测试无位置信息时的 fallback 行为