# Token 范围改进设计

## 概述

将 Token 从单点位置（line, column）改为范围位置（startColumn, endColumn），并为语法/词法错误提供更精确的位置范围信息。

## 目标

1. Token 类使用 startColumn 和 endColumn 代替 column
2. 语法错误使用范围：起始 token 的 line/startColumn 到结尾 token 的 line/endColumn
3. 单 token 语法错误：token 的 line/startColumn 到 line/endColumn
4. 词法错误尽量记录范围，而非单点

---

## 1. Token 类修改

**文件**: `src/main/java/org/dromara/easytl/parser/Token.java`

```java
// 当前
private final int column;

// 修改后
private final int startColumn;
private final int endColumn;
```

- 保留 `line` 字段（对于单行的 token，startLine == endLine）
- 将 `column` 替换为 `startColumn` 和 `endColumn`
- 新增构造函数接受 startColumn 和 endColumn
- 移除旧的 getColumn() 方法

---

## 2. Source 接口和实现修改

**文件**: `src/main/java/org/dromara/easytl/parser/Source.java`

```java
// 新增方法
int getEndColumn();  // 获取结束列号（读取完当前字符后的列号）
```

**文件**: `src/main/java/org/dromara/easytl/parser/StringSource.java`

- 需要追踪 `endColumn`（当前字符结束后的列号）
- 在 `read()` 方法中，读取字符后更新 `endColumn`
- 需要在读取前 `mark()` 当前位置跟踪起点

---

## 3. Lexer 修改

**文件**: `src/main/java/org/dromara/easytl/parser/Lexer.java`

所有创建 Token 的地方改为：
```java
// 当前
new Token(type, value, line, column, position);

// 修改后
new Token(type, value, line, startColumn, endColumn, position);
```

实现方式：
- 在解析每个 token 前调用 `source.mark()` 记录起始位置
- 解析完成后调用 `source.getColumn()` 获取结束列号
- 由于 read() 在读取字符后 column 已经 +1，所以 column 就是 endColumn

---

## 4. Parser 修改

**文件**: `src/main/java/org/dromara/easytl/parser/Parser.java`

所有创建 `TemplateSyntaxException` 的地方改为使用范围：

```java
// 单 token 错误
Token token = current();
throw new TemplateSyntaxException("message",
    token.getLine(), token.getStartColumn(),
    token.getLine(), token.getEndColumn(),
    sourceText);

// 多 token 错误（范围从起始到结尾 token）
Token startToken = ...;
Token endToken = ...;
throw new TemplateSyntaxException("message",
    startToken.getLine(), startToken.getStartColumn(),
    endToken.getLine(), endToken.getEndColumn(),
    sourceText);
```

---

## 5. 词法错误处理

对于未闭合的字符串等词法错误：

```java
// 在 Lexer 中
source.mark();
int startColumn = source.getColumn();
// ... 解析失败，到达错误点
int endColumn = source.getColumn();
throw new TemplateSyntaxException("Unclosed string",
    line, startColumn, line, endColumn, source.getSourceText());
```

---

## 6. 测试更新

- 更新 `SyntaxErrorTest.java` 验证错误范围正确
- 更新 `LexerTest.java` 验证 Token 的 startColumn/endColumn 正确

---

## 变更清单

| 文件 | 变更 |
|------|------|
| Token.java | column → startColumn + endColumn，移除 getColumn() |
| Source.java | 新增 getEndColumn() |
| StringSource.java | 实现 getEndColumn()，追踪结束列号 |
| FileSource.java | 实现 getEndColumn() |
| StreamSource.java | 实现 getEndColumn() |
| Lexer.java | 所有 Token 创建处更新为使用范围 |
| Parser.java | 所有 TemplateSyntaxException 创建处更新为使用范围 |

---

## 兼容性

此为破坏性变更：
- Token 的 `getColumn()` 方法被移除
- 调用方需要改为使用 `getStartColumn()` 或 `getEndColumn()`
