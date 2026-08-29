# Token 范围改进实现计划

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**目标：** 将 Token 从单点位置改为范围位置，使语法错误能显示精确的代码范围

**架构：** 修改 Token 类使用 startColumn/endColumn，更新 Source 接口追踪结束列号，更新 Lexer 和 Parser 使用范围

**技术栈：** Java 8, Maven, JUnit 5

---

## Task 1: 修改 Token 类 - 添加 startColumn 和 endColumn

**Files:**
- Modify: `src/main/java/org/dromara/easytl/parser/Token.java`

**Step 1: 编写失败的测试**

创建测试文件 `src/test/java/org/dromara/easytl/parser/TokenRangeTest.java`:

```java
package org.dromara.easytl.parser;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TokenRangeTest {

    @Test
    public void testTokenHasStartAndEndColumn() {
        // 测试 Token 构造函数接受 startColumn 和 endColumn
        Token token = new Token(TokenType.IDENTIFIER, "abc", 1, 1, 3, 0);
        assertEquals(1, token.getStartColumn());
        assertEquals(3, token.getEndColumn());
    }

    @Test
    public void testSingleCharToken() {
        Token token = new Token(TokenType.PLUS, "+", 1, 5, 5, 0);
        assertEquals(5, token.getStartColumn());
        assertEquals(5, token.getEndColumn());
    }

    @Test
    public void testMultiCharToken() {
        Token token = new Token(TokenType.IDENTIFIER, "hello", 1, 1, 5, 0);
        assertEquals(1, token.getStartColumn());
        assertEquals(5, token.getEndColumn());
    }
}
```

**Step 2: 运行测试验证失败**

Run: `mvn test -Dtest=TokenRangeTest`
Expected: FAIL - 编译错误（构造函数不存在）

**Step 3: 修改 Token.java**

将 `column` 改为 `startColumn` 和 `endColumn`:

```java
public class Token {

    private final int type;
    private final String value;
    private final int line;
    private final int startColumn;
    private final int endColumn;
    private final int position;

    /**
     * 构造一个 Token
     */
    public Token(int type, String value, int line, int startColumn, int endColumn, int position) {
        this.type = type;
        this.value = value;
        this.line = line;
        this.startColumn = startColumn;
        this.endColumn = endColumn;
        this.position = position;
    }

    /**
     * 构造一个无值的 Token
     */
    public Token(int type, int line, int startColumn, int endColumn, int position) {
        this(type, null, line, startColumn, endColumn, position);
    }

    public int getLine() {
        return line;
    }

    public int getStartColumn() {
        return startColumn;
    }

    public int getEndColumn() {
        return endColumn;
    }

    // 移除 getColumn() 方法

    @Override
    public String toString() {
        if (value != null) {
            return String.format("Token{type=%s, value='%s', line=%d, startColumn=%d, endColumn=%d}",
                    TokenType.getName(type), value, line, startColumn, endColumn);
        } else {
            return String.format("Token{type=%s, line=%d, startColumn=%d, endColumn=%d}",
                    TokenType.getName(type), line, startColumn, endColumn);
        }
    }
}
```

**Step 4: 运行测试验证通过**

Run: `mvn test -Dtest=TokenRangeTest`
Expected: PASS

**Step 5: Commit**

```bash
git add src/main/java/org/dromara/easytl/parser/Token.java src/test/java/org/dromara/easytl/parser/TokenRangeTest.java
git commit -m "feat: add startColumn and endColumn to Token class"
```

---

## Task 2: 修改 Source 接口 - 添加 getEndColumn()

**Files:**
- Modify: `src/main/java/org/dromara/easytl/parser/Source.java`

**Step 1: 编写失败的测试**

在 `SourceTest.java` 中添加测试（已有此文件）:

```java
@Test
public void testEndColumnTracking() {
    StringSource source = new StringSource("ab");
    source.read(); // 读取 'a'
    assertEquals(2, source.getColumn()); // column 是下一个字符位置
    // 需要验证 endColumn 的概念 - 读取完字符后的位置
}
```

**Step 2: 运行测试验证失败**

Run: `mvn test -Dtest=SourceTest`
Expected: FAIL - getEndColumn() 方法不存在

**Step 3: 修改 Source.java**

添加接口方法:

```java
/**
 * 获取当前字符结束后的列号
 * 即读取完当前字符后，当前位置的列号
 */
int getEndColumn();
```

**Step 4: 运行测试验证通过**

Run: `mvn test -Dtest=SourceTest`
Expected: PASS

**Step 5: Commit**

```bash
git add src/main/java/org/dromara/easytl/parser/Source.java
git commit -m "feat: add getEndColumn() to Source interface"
```

---

## Task 3: 修改 StringSource - 实现 getEndColumn()

**Files:**
- Modify: `src/main/java/org/dromara/easytl/parser/StringSource.java`

**Step 1: 编写失败的测试**

在 `SourceTest.java` 中添加测试:

```java
@Test
public void testEndColumnAfterRead() {
    StringSource source = new StringSource("abc");
    // 初始状态
    assertEquals(1, source.getColumn());
    assertEquals(1, source.getEndColumn());

    source.read(); // 读取 'a'，column 变为 2
    assertEquals(2, source.getColumn());
    // endColumn 应该是读取完 'a' 后的位置，即 2
    assertEquals(2, source.getEndColumn());

    source.read(); // 读取 'b'，column 变为 3
    assertEquals(3, source.getColumn());
    assertEquals(3, source.getEndColumn());
}
```

**Step 2: 运行测试验证失败**

Run: `mvn test -Dtest=SourceTest#testEndColumnAfterRead`
Expected: FAIL

**Step 3: 修改 StringSource.java**

添加 endColumn 字段和实现:

```java
private int column;
private int endColumn = 1; // 新增

@Override
public int getEndColumn() {
    return endColumn;
}

@Override
public int read() {
    if (position >= content.length()) {
        return -1;
    }

    char ch = content.charAt(position++);

    if (ch == '\n') {
        line++;
        column = 1;
    } else if (ch == '\r') {
        line++;
        column = 1;
        if (position < content.length() && content.charAt(position) == '\n') {
            position++;
        }
    } else {
        column++;
    }

    endColumn = column; // 更新 endColumn
    return ch;
}
```

**Step 4: 运行测试验证通过**

Run: `mvn test -Dtest=SourceTest#testEndColumnAfterRead`
Expected: PASS

**Step 5: Commit**

```bash
git add src/main/java/org/dromara/easytl/parser/StringSource.java
git commit -m "feat: implement getEndColumn() in StringSource"
```

---

## Task 4: 修改 FileSource 和 StreamSource - 实现 getEndColumn()

**Files:**
- Modify: `src/main/java/org/dromara/easytl/parser/FileSource.java`
- Modify: `src/main/java/org/dromara/easytl/parser/StreamSource.java`

**Step 1: 修改 FileSource.java**

添加字段和方法:

```java
private int column;
private int endColumn = 1;

@Override
public int getEndColumn() {
    return endColumn;
}

// 在 read() 方法中添加: endColumn = column;
```

**Step 2: 修改 StreamSource.java**

同样添加 endColumn 字段和 getEndColumn() 方法。

**Step 3: 编译验证**

Run: `mvn compile`
Expected: 编译成功

**Step 4: Commit**

```bash
git add src/main/java/org/dromara/easytl/parser/FileSource.java src/main/java/org/dromara/easytl/parser/StreamSource.java
git commit -m "feat: implement getEndColumn() in FileSource and StreamSource"
```

---

## Task 5: 修改 Lexer - 使用 startColumn 和 endColumn

**Files:**
- Modify: `src/main/java/org/dromara/easytl/parser/Lexer.java`

**Step 1: 编写失败的测试**

在 `LexerTest.java` 中添加测试:

```java
@Test
public void testTokenRange() {
    StringSource source = new StringSource("abc");
    Lexer lexer = new Lexer(source);
    Token token = lexer.next();

    assertNotNull(token);
    assertEquals("abc", token.getValue());
    assertEquals(1, token.getStartColumn());
    assertEquals(3, token.getEndColumn());
}

@Test
public void testSingleCharTokenRange() {
    StringSource source = new StringSource("+ -");
    Lexer lexer = new Lexer(source);
    Token plus = lexer.next();
    Token minus = lexer.next();

    assertEquals(1, plus.getStartColumn());
    assertEquals(1, plus.getEndColumn());

    assertEquals(3, minus.getStartColumn());
    assertEquals(3, minus.getEndColumn());
}
```

**Step 2: 运行测试验证失败**

Run: `mvn test -Dtest=LexerTest#testTokenRange`
Expected: FAIL - Token 构造函数参数不匹配

**Step 3: 修改 Lexer.java**

找到所有创建 Token 的地方（约 50+ 处），改为:

```java
// 原来的调用
new Token(type, value, line, column, position);

// 改为
new Token(type, value, line, startColumn, endColumn, position);
```

关键修改点:
- 在每个 token 解析前调用 `source.mark()` 记录起始位置
- 解析完成后获取 `source.getEndColumn()` 作为结束列号

示例修改:
```java
int line = source.getLine();
source.mark();
int startColumn = source.getColumn();
// ... 解析 token
int endColumn = source.getEndColumn();
return new Token(type, value, line, startColumn, endColumn, position);
```

**Step 4: 运行测试验证通过**

Run: `mvn test -Dtest=LexerTest#testTokenRange,LexerTest#testSingleCharTokenRange`
Expected: PASS

**Step 5: 运行所有 Lexer 测试**

Run: `mvn test -Dtest=LexerTest`
Expected: 所有测试通过

**Step 6: Commit**

```bash
git add src/main/java/org/dromara/easytl/parser/Lexer.java
git commit -m "feat: update Lexer to use startColumn and endColumn"
```

---

## Task 6: 修改 Parser - 使用范围创建语法错误

**Files:**
- Modify: `src/main/java/org/dromara/easytl/parser/Parser.java`

**Step 1: 运行现有测试**

Run: `mvn test -Dtest=SyntaxErrorTest`
Expected: 部分测试失败（因为 Token.getColumn() 被移除）

**Step 2: 找到所有使用 current().getColumn() 的地方**

```bash
grep -n "getColumn()" src/main/java/org/dromara/easytl/parser/Parser.java
```

**Step 3: 修改 Parser.java**

将所有:
```java
throw new TemplateSyntaxException("message", current().getLine(), current().getColumn(), sourceText);
```

改为:
```java
Token token = current();
throw new TemplateSyntaxException("message",
    token.getLine(), token.getStartColumn(),
    token.getLine(), token.getEndColumn(),
    sourceText);
```

对于需要多 token 范围的错误:
```java
Token startToken = ...;
Token endToken = ...;
throw new TemplateSyntaxException("message",
    startToken.getLine(), startToken.getStartColumn(),
    endToken.getLine(), endToken.getEndColumn(),
    sourceText);
```

**Step 4: 运行测试验证**

Run: `mvn test -Dtest=SyntaxErrorTest`
Expected: 所有测试通过

**Step 5: Commit**

```bash
git add src/main/java/org/dromara/easytl/parser/Parser.java
git commit -m "feat: update Parser to use range for syntax errors"
```

---

## Task 7: 更新词法错误 - 使用范围

**Files:**
- Modify: `src/main/java/org/dromara/easytl/parser/Lexer.java`

**Step 1: 找到词法错误抛出位置**

```bash
grep -n "throw new TemplateSyntaxException" src/main/java/org/dromara/easytl/parser/Lexer.java
```

**Step 2: 修改词法错误使用范围**

对于未闭合的字符串等错误:
```java
// 修改前
throw new TemplateSyntaxException("Unclosed string", line, column, source.getSourceText());

// 修改后
throw new TemplateSyntaxException("Unclosed string",
    line, startColumn, line, endColumn, source.getSourceText());
```

**Step 3: 运行测试验证**

Run: `mvn test -Dtest=LexerTest`
Expected: 所有测试通过

**Step 4: Commit**

```bash
git add src/main/java/org/dromara/easytl/parser/Lexer.java
git commit -m "feat: use range for lexical errors"
```

---

## Task 8: 验证所有测试通过

**Step 1: 运行所有测试**

Run: `mvn test`
Expected: 所有测试通过

**Step 2: 验证错误消息格式**

运行一个语法错误测试，检查错误消息中的范围是否正确:
```bash
mvn test -Dtest=SyntaxErrorTest#testSyntaxErrorLineAndColumn -DtrimStackTrace=false
```

**Step 3: Commit**

```bash
git add . && git commit -m "test: verify all tests pass"
```

---

## Task 9: 清理和最终提交

**Step 1: 检查是否有遗留问题**

```bash
grep -rn "getColumn()" src/main/java/
```

确保没有遗留的 getColumn() 调用。

**Step 2: 最终测试**

Run: `mvn test`
Expected: BUILD SUCCESS

**Step 3: Commit**

```bash
git add . && git commit -m "feat: complete token range implementation"
```
