# AST Node Position Information Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Add position information to all AST nodes by accepting Token parameters in constructors, enabling Rust-style error messages with source code location.

**Architecture:** Add static helper methods `positionFromToken()` and `positionFromTokens()` to ASTNode base class. All ASTNode subclasses get new constructors accepting Token (or start/end Token pair) parameters. Parser passes Token references when creating nodes.

**Tech Stack:** Java 8, JUnit 5

---

## Task 1: Add Position Helper Methods to ASTNode

**Files:**
- Modify: `src/main/java/org/dromara/easytl/ast/ASTNode.java`

**Step 1: Read current ASTNode implementation**

Read: `src/main/java/org/dromara/easytl/ast/ASTNode.java`

**Step 2: Add positionFromToken static method**

Add this method to ASTNode class:

```java
/**
 * 从单个 Token 创建 Position 对象
 *
 * @param token Token 对象
 * @return Position 对象，如果 token 为 null 则返回 Position.unknown()
 */
protected static Position positionFromToken(Token token) {
    if (token == null) {
        return Position.unknown();
    }
    int len = token.getValue() != null ? token.getValue().length() : 1;
    return new Position(token.getLine(), token.getColumn(),
                        token.getLine(), token.getColumn() + len - 1);
}
```

**Step 3: Add positionFromTokens static method**

Add this method to ASTNode class:

```java
/**
 * 从两个 Token 创建 Position 对象（表示范围）
 *
 * @param startToken 起始 Token
 * @param endToken 结束 Token
 * @return Position 对象，如果任一 token 为 null 则返回 Position.unknown()
 */
protected static Position positionFromTokens(Token startToken, Token endToken) {
    if (startToken == null || endToken == null) {
        return Position.unknown();
    }
    int endLen = endToken.getValue() != null ? endToken.getValue().length() : 1;
    return new Position(startToken.getLine(), startToken.getColumn(),
                        endToken.getLine(), endToken.getColumn() + endLen - 1);
}
```

**Step 4: Add import for Token**

Add import at the top of the file:

```java
import org.dromara.easytl.parser.Token;
```

**Step 5: Compile to verify**

Run: `mvn compile -q`
Expected: BUILD SUCCESS

**Step 6: Commit**

```bash
git add src/main/java/org/dromara/easytl/ast/ASTNode.java
git commit -m "feat: add positionFromToken helper methods to ASTNode base class"
```

---

## Task 2: Update ArgumentAccess with Token Constructor

**Files:**
- Modify: `src/main/java/org/dromara/easytl/ast/ArgumentAccess.java`

**Step 1: Read current ArgumentAccess implementation**

Read: `src/main/java/org/dromara/easytl/ast/ArgumentAccess.java`

**Step 2: Add import for Token**

```java
import org.dromara.easytl.parser.Token;
```

**Step 3: Modify existing constructor with Position to use helper**

Change the existing Position constructor:

```java
public ArgumentAccess(int index, Position position) {
    super(position);
    this.index = index;
}
```

To:

```java
public ArgumentAccess(int index, Position position) {
    super(position);
    this.index = index;
}

/**
 * 构造参数访问节点（从 Token 获取位置信息）
 *
 * @param index 参数索引
 * @param token Token 对象（用于获取位置信息）
 */
public ArgumentAccess(int index, Token token) {
    super(positionFromToken(token));
    this.index = index;
}
```

**Step 4: Compile to verify**

Run: `mvn compile -q`
Expected: BUILD SUCCESS

**Step 5: Commit**

```bash
git add src/main/java/org/dromara/easytl/ast/ArgumentAccess.java
git commit -m "feat: add Token-based constructor to ArgumentAccess"
```

---

## Task 3: Update ArgumentVariable with Token Constructor

**Files:**
- Modify: `src/main/java/org/dromara/easytl/ast/ArgumentVariable.java`

**Step 1: Read current ArgumentVariable implementation**

Read: `src/main/java/org/dromara/easytl/ast/ArgumentVariable.java`

**Step 2: Add import for Token**

```java
import org.dromara.easytl.parser.Token;
```

**Step 3: Add Token-based constructor**

```java
/**
 * 构造参数变量节点（从 Token 获取位置信息）
 *
 * @param index 参数索引
 * @param token Token 对象（用于获取位置信息）
 */
public ArgumentVariable(int index, Token token) {
    super(positionFromToken(token));
    this.index = index;
}
```

**Step 4: Compile to verify**

Run: `mvn compile -q`
Expected: BUILD SUCCESS

**Step 5: Commit**

```bash
git add src/main/java/org/dromara/easytl/ast/ArgumentVariable.java
git commit -m "feat: add Token-based constructor to ArgumentVariable"
```

---

## Task 4: Update Identifier with Token Constructor

**Files:**
- Modify: `src/main/java/org/dromara/easytl/ast/Identifier.java`

**Step 1: Read current Identifier implementation**

Read: `src/main/java/org/dromara/easytl/ast/Identifier.java`

**Step 2: Add import for Token**

```java
import org.dromara.easytl.parser.Token;
```

**Step 3: Add Token-based constructor**

```java
/**
 * 构造标识符节点（从 Token 获取位置信息）
 *
 * @param name 标识符名称
 * @param token Token 对象（用于获取位置信息）
 */
public Identifier(String name, Token token) {
    super(positionFromToken(token));
    this.name = name;
}
```

**Step 4: Compile to verify**

Run: `mvn compile -q`
Expected: BUILD SUCCESS

**Step 5: Commit**

```bash
git add src/main/java/org/dromara/easytl/ast/Identifier.java
git commit -m "feat: add Token-based constructor to Identifier"
```

---

## Task 5: Update MemberAccess with Token Constructors

**Files:**
- Modify: `src/main/java/org/dromara/easytl/ast/MemberAccess.java`

**Step 1: Read current MemberAccess implementation**

Read: `src/main/java/org/dromara/easytl/ast/MemberAccess.java`

**Step 2: Add import for Token**

```java
import org.dromara.easytl.parser.Token;
```

**Step 3: Add Token-based constructor**

```java
/**
 * 构造成员访问节点（从 Token 获取位置信息）
 *
 * @param object 对象表达式
 * @param memberName 成员名称
 * @param safeNavigation 是否为安全导航
 * @param token Token 对象（用于获取位置信息）
 */
public MemberAccess(ASTNode object, String memberName, boolean safeNavigation, Token token) {
    super(positionFromToken(token));
    this.object = object;
    this.memberName = memberName;
    this.safeNavigation = safeNavigation;
}
```

**Step 4: Compile to verify**

Run: `mvn compile -q`
Expected: BUILD SUCCESS

**Step 5: Commit**

```bash
git add src/main/java/org/dromara/easytl/ast/MemberAccess.java
git commit -m "feat: add Token-based constructor to MemberAccess"
```

---

## Task 6: Update IndexAccess with Token Constructor

**Files:**
- Modify: `src/main/java/org/dromara/easytl/ast/IndexAccess.java`

**Step 1: Read current IndexAccess implementation**

Read: `src/main/java/org/dromara/easytl/ast/IndexAccess.java`

**Step 2: Add import for Token**

```java
import org.dromara.easytl.parser.Token;
```

**Step 3: Add Token-based constructor**

```java
/**
 * 构造索引访问节点（从 Token 获取位置信息）
 *
 * @param object 对象表达式
 * @param index 索引表达式
 * @param token Token 对象（用于获取位置信息）
 */
public IndexAccess(ASTNode object, ASTNode index, Token token) {
    super(positionFromToken(token));
    this.object = object;
    this.index = index;
}
```

**Step 4: Compile to verify**

Run: `mvn compile -q`
Expected: BUILD SUCCESS

**Step 5: Commit**

```bash
git add src/main/java/org/dromara/easytl/ast/IndexAccess.java
git commit -m "feat: add Token-based constructor to IndexAccess"
```

---

## Task 7: Update MethodCall with Token Constructor

**Files:**
- Modify: `src/main/java/org/dromara/easytl/ast/MethodCall.java`

**Step 1: Read current MethodCall implementation**

Read: `src/main/java/org/dromara/easytl/ast/MethodCall.java`

**Step 2: Add import for Token**

```java
import org.dromara.easytl.parser.Token;
```

**Step 3: Add Token-based constructor**

```java
/**
 * 构造方法调用节点（从 Token 获取位置信息）
 *
 * @param object 对象表达式
 * @param methodName 方法名称
 * @param arguments 参数列表
 * @param safeNavigation 是否为安全导航
 * @param token Token 对象（用于获取位置信息）
 */
public MethodCall(ASTNode object, String methodName, List<ASTNode> arguments, boolean safeNavigation, Token token) {
    super(positionFromToken(token));
    this.object = object;
    this.methodName = methodName;
    this.arguments = arguments;
    this.safeNavigation = safeNavigation;
}
```

**Step 4: Compile to verify**

Run: `mvn compile -q`
Expected: BUILD SUCCESS

**Step 5: Commit**

```bash
git add src/main/java/org/dromara/easytl/ast/MethodCall.java
git commit -m "feat: add Token-based constructor to MethodCall"
```

---

## Task 8: Update BinaryExpression with Token Constructor

**Files:**
- Modify: `src/main/java/org/dromara/easytl/ast/BinaryExpression.java`

**Step 1: Read current BinaryExpression implementation**

Read: `src/main/java/org/dromara/easytl/ast/BinaryExpression.java`

**Step 2: Add import for Token**

```java
import org.dromara.easytl.parser.Token;
```

**Step 3: Add Token-based constructor (using start and end tokens)**

```java
/**
 * 构造二元表达式节点（从 Token 范围获取位置信息）
 *
 * @param left 左操作数
 * @param operator 运算符
 * @param right 右操作数
 * @param startToken 起始 Token
 * @param endToken 结束 Token
 */
public BinaryExpression(ASTNode left, Operator operator, ASTNode right, Token startToken, Token endToken) {
    super(positionFromTokens(startToken, endToken));
    this.left = left;
    this.operator = operator;
    this.right = right;
}
```

**Step 4: Compile to verify**

Run: `mvn compile -q`
Expected: BUILD SUCCESS

**Step 5: Commit**

```bash
git add src/main/java/org/dromara/easytl/ast/BinaryExpression.java
git commit -m "feat: add Token-based constructor to BinaryExpression"
```

---

## Task 9: Update UnaryExpression with Token Constructor

**Files:**
- Modify: `src/main/java/org/dromara/easytl/ast/UnaryExpression.java`

**Step 1: Read current UnaryExpression implementation**

Read: `src/main/java/org/dromara/easytl/ast/UnaryExpression.java`

**Step 2: Add import for Token**

```java
import org.dromara.easytl.parser.Token;
```

**Step 3: Add Token-based constructor**

```java
/**
 * 构造一元表达式节点（从 Token 获取位置信息）
 *
 * @param operator 运算符
 * @param operand 操作数
 * @param token Token 对象（用于获取位置信息）
 */
public UnaryExpression(Operator operator, ASTNode operand, Token token) {
    super(positionFromToken(token));
    this.operator = operator;
    this.operand = operand;
}
```

**Step 4: Compile to verify**

Run: `mvn compile -q`
Expected: BUILD SUCCESS

**Step 5: Commit**

```bash
git add src/main/java/org/dromara/easytl/ast/UnaryExpression.java
git commit -m "feat: add Token-based constructor to UnaryExpression"
```

---

## Task 10: Update TernaryExpression with Token Constructor

**Files:**
- Modify: `src/main/java/org/dromara/easytl/ast/TernaryExpression.java`

**Step 1: Read current TernaryExpression implementation**

Read: `src/main/java/org/dromara/easytl/ast/TernaryExpression.java`

**Step 2: Add import for Token**

```java
import org.dromara.easytl.parser.Token;
```

**Step 3: Add Token-based constructor**

```java
/**
 * 构造三元表达式节点（从 Token 范围获取位置信息）
 *
 * @param condition 条件表达式
 * @param trueValue 为真时的值
 * @param falseValue 为假时的值
 * @param startToken 起始 Token
 * @param endToken 结束 Token
 */
public TernaryExpression(ASTNode condition, ASTNode trueValue, ASTNode falseValue, Token startToken, Token endToken) {
    super(positionFromTokens(startToken, endToken));
    this.condition = condition;
    this.trueValue = trueValue;
    this.falseValue = falseValue;
}
```

**Step 4: Compile to verify**

Run: `mvn compile -q`
Expected: BUILD SUCCESS

**Step 5: Commit**

```bash
git add src/main/java/org/dromara/easytl/ast/TernaryExpression.java
git commit -m "feat: add Token-based constructor to TernaryExpression"
```

---

## Task 11: Update ElvisExpression with Token Constructor

**Files:**
- Modify: `src/main/java/org/dromara/easytl/ast/ElvisExpression.java`

**Step 1: Read current ElvisExpression implementation**

Read: `src/main/java/org/dromara/easytl/ast/ElvisExpression.java`

**Step 2: Add import for Token**

```java
import org.dromara.easytl.parser.Token;
```

**Step 3: Add Token-based constructor**

```java
/**
 * 构造 Elvis 表达式节点（从 Token 范围获取位置信息）
 *
 * @param left 左表达式
 * @param right 右表达式
 * @param startToken 起始 Token
 * @param endToken 结束 Token
 */
public ElvisExpression(ASTNode left, ASTNode right, Token startToken, Token endToken) {
    super(positionFromTokens(startToken, endToken));
    this.left = left;
    this.right = right;
}
```

**Step 4: Compile to verify**

Run: `mvn compile -q`
Expected: BUILD SUCCESS

**Step 5: Commit**

```bash
git add src/main/java/org/dromara/easytl/ast/ElvisExpression.java
git commit -m "feat: add Token-based constructor to ElvisExpression"
```

---

## Task 12: Update Remaining Literal Nodes with Token Constructors

**Files:**
- Modify: `src/main/java/org/dromara/easytl/ast/NumberLiteral.java`
- Modify: `src/main/java/org/dromara/easytl/ast/StringLiteral.java`
- Modify: `src/main/java/org/dromara/easytl/ast/BooleanLiteral.java`
- Modify: `src/main/java/org/dromara/easytl/ast/RegexLiteral.java`
- Modify: `src/main/java/org/dromara/easytl/ast/ListLiteral.java`
- Modify: `src/main/java/org/dromara/easytl/ast/HashLiteral.java`
- Modify: `src/main/java/org/dromara/easytl/ast/RangeLiteral.java`

**Step 1: Add Token constructors to each literal node**

For each file, add import and Token-based constructor similar to previous tasks:

**NumberLiteral:**
```java
import org.dromara.easytl.parser.Token;

public NumberLiteral(Number value, String rawValue, Token token) {
    super(positionFromToken(token));
    this.value = value;
    this.rawValue = rawValue;
}
```

**StringLiteral:**
```java
import org.dromara.easytl.parser.Token;

public StringLiteral(String value, Token token) {
    super(positionFromToken(token));
    this.value = value;
}
```

**BooleanLiteral:**
```java
import org.dromara.easytl.parser.Token;

public BooleanLiteral(boolean value, Token token) {
    super(positionFromToken(token));
    this.value = value;
}
```

**RegexLiteral:**
```java
import org.dromara.easytl.parser.Token;

public RegexLiteral(String pattern, String flags, Token token) {
    super(positionFromToken(token));
    this.pattern = pattern;
    this.flags = flags;
}
```

**ListLiteral:**
```java
import org.dromara.easytl.parser.Token;

public ListLiteral(List<ASTNode> elements, Token startToken, Token endToken) {
    super(positionFromTokens(startToken, endToken));
    this.elements = elements;
}
```

**HashLiteral:**
```java
import org.dromara.easytl.parser.Token;

public HashLiteral(List<Map.Entry<ASTNode, ASTNode>> entries, Token startToken, Token endToken) {
    super(positionFromTokens(startToken, endToken));
    this.entries = entries;
}
```

**RangeLiteral:**
```java
import org.dromara.easytl.parser.Token;

public RangeLiteral(ASTNode start, ASTNode end, RangeType type, Token startToken, Token endToken) {
    super(positionFromTokens(startToken, endToken));
    this.start = start;
    this.end = end;
    this.type = type;
}
```

**Step 2: Compile to verify**

Run: `mvn compile -q`
Expected: BUILD SUCCESS

**Step 3: Commit**

```bash
git add src/main/java/org/dromara/easytl/ast/NumberLiteral.java \
        src/main/java/org/dromara/easytl/ast/StringLiteral.java \
        src/main/java/org/dromara/easytl/ast/BooleanLiteral.java \
        src/main/java/org/dromara/easytl/ast/RegexLiteral.java \
        src/main/java/org/dromara/easytl/ast/ListLiteral.java \
        src/main/java/org/dromara/easytl/ast/HashLiteral.java \
        src/main/java/org/dromara/easytl/ast/RangeLiteral.java
git commit -m "feat: add Token-based constructors to literal AST nodes"
```

---

## Task 13: Update Statement Nodes with Token Constructors

**Files:**
- Modify: `src/main/java/org/dromara/easytl/ast/IfStatement.java`
- Modify: `src/main/java/org/dromara/easytl/ast/ForStatement.java`
- Modify: `src/main/java/org/dromara/easytl/ast/SwitchStatement.java`
- Modify: `src/main/java/org/dromara/easytl/ast/CaseClause.java`
- Modify: `src/main/java/org/dromara/easytl/ast/LetStatement.java`
- Modify: `src/main/java/org/dromara/easytl/ast/ExtendsStatement.java`
- Modify: `src/main/java/org/dromara/easytl/ast/ImportStatement.java`

**Step 1: Add Token constructors to each statement node**

For each file, add import and Token-based constructor. Example for IfStatement:

```java
import org.dromara.easytl.parser.Token;

public IfStatement(ASTNode condition, List<ASTNode> thenBody,
                   List<ElseIfClause> elseIfClauses, List<ASTNode> elseBody,
                   Token startToken, Token endToken) {
    super(positionFromTokens(startToken, endToken));
    this.condition = condition;
    this.thenBody = thenBody;
    this.elseIfClauses = elseIfClauses;
    this.elseBody = elseBody;
}
```

Apply similar pattern to other statement nodes.

**Step 2: Compile to verify**

Run: `mvn compile -q`
Expected: BUILD SUCCESS

**Step 3: Commit**

```bash
git add src/main/java/org/dromara/easytl/ast/IfStatement.java \
        src/main/java/org/dromara/easytl/ast/ForStatement.java \
        src/main/java/org/dromara/easytl/ast/SwitchStatement.java \
        src/main/java/org/dromara/easytl/ast/CaseClause.java \
        src/main/java/org/dromara/easytl/ast/LetStatement.java \
        src/main/java/org/dromara/easytl/ast/ExtendsStatement.java \
        src/main/java/org/dromara/easytl/ast/ImportStatement.java
git commit -m "feat: add Token-based constructors to statement AST nodes"
```

---

## Task 14: Update Expression Nodes with Token Constructors

**Files:**
- Modify: `src/main/java/org/dromara/easytl/ast/IfExpression.java`
- Modify: `src/main/java/org/dromara/easytl/ast/ForExpression.java`

**Step 1: Add Token constructors to expression nodes**

Similar to Task 13, add Token-based constructors.

**Step 2: Compile to verify**

Run: `mvn compile -q`
Expected: BUILD SUCCESS

**Step 3: Commit**

```bash
git add src/main/java/org/dromara/easytl/ast/IfExpression.java \
        src/main/java/org/dromara/easytl/ast/ForExpression.java
git commit -m "feat: add Token-based constructors to IfExpression and ForExpression"
```

---

## Task 15: Modify Parser - ArgumentAccess

**Files:**
- Modify: `src/main/java/org/dromara/easytl/parser/Parser.java` (line 187)

**Step 1: Locate the ArgumentAccess creation code**

Find line 187 in Parser.java:
```java
expression = new ArgumentAccess(intValue);
```

**Step 2: Modify to pass Token information**

Save the NumberLiteral's token before the conversion:

```java
if (intValue >= 0) {
    // 转换为参数访问节点，继承 NumberLiteral 的位置信息
    expression = new ArgumentAccess(intValue, numLiteralToken);
}
```

Note: Need to capture the token from NumberLiteral. May need to adjust parseNumberLiteral to return token info, or use the startToken saved earlier.

**Step 3: Compile to verify**

Run: `mvn compile -q`
Expected: BUILD SUCCESS

**Step 4: Commit**

```bash
git add src/main/java/org/dromara/easytl/parser/Parser.java
git commit -m "feat: pass Token to ArgumentAccess constructor in Parser"
```

---

## Task 16: Modify Parser - ArgumentVariable

**Files:**
- Modify: `src/main/java/org/dromara/easytl/parser/Parser.java` (line 737)

**Step 1: Locate the ArgumentVariable creation code**

Find line 737 in Parser.java:
```java
return new ArgumentVariable(index);
```

**Step 2: Modify to pass Token information**

```java
Token token = advance();
String value = token.getValue();
int index = Integer.parseInt(value.substring(1));
return new ArgumentVariable(index, token);
```

**Step 3: Compile to verify**

Run: `mvn compile -q`
Expected: BUILD SUCCESS

**Step 4: Commit**

```bash
git add src/main/java/org/dromara/easytl/parser/Parser.java
git commit -m "feat: pass Token to ArgumentVariable constructor in Parser"
```

---

## Task 17: Modify Parser - Identifier

**Files:**
- Modify: `src/main/java/org/dromara/easytl/parser/Parser.java` (lines 740, 817)

**Step 1: Locate Identifier creation code**

Find lines where Identifier is created:
- Line 740: `return new Identifier(advance().getValue());`
- Line 817: `key = new Identifier(advance().getValue());`

**Step 2: Modify to pass Token information**

```java
// Line 740
Token token = advance();
return new Identifier(token.getValue(), token);

// Line 817
Token token = advance();
key = new Identifier(token.getValue(), token);
```

**Step 3: Compile to verify**

Run: `mvn compile -q`
Expected: BUILD SUCCESS

**Step 4: Commit**

```bash
git add src/main/java/org/dromara/easytl/parser/Parser.java
git commit -m "feat: pass Token to Identifier constructor in Parser"
```

---

## Task 18: Modify Parser - All Expression Nodes

**Files:**
- Modify: `src/main/java/org/dromara/easytl/parser/Parser.java`

**Step 1: Update all expression node creations**

Modify all places where the following nodes are created:
- MemberAccess (lines 652, 656, 667, 671)
- IndexAccess (line 661)
- MethodCall (lines 667, 671)
- BinaryExpression (lines 524, 539, 557, 573, 606, 622)
- UnaryExpression (line 636)
- TernaryExpression (line 509)
- ElvisExpression (line 492)
- RangeLiteral (line 590)

For each, pass appropriate start/end Token information.

**Step 2: Compile to verify**

Run: `mvn compile -q`
Expected: BUILD SUCCESS

**Step 3: Commit**

```bash
git add src/main/java/org/dromara/easytl/parser/Parser.java
git commit -m "feat: pass Token to expression node constructors in Parser"
```

---

## Task 19: Modify Parser - All Literal Nodes

**Files:**
- Modify: `src/main/java/org/dromara/easytl/parser/Parser.java`

**Step 1: Update all literal node creations**

Modify all places where the following nodes are created:
- NumberLiteral (line 792)
- StringLiteral (lines 724, 727, 819)
- BooleanLiteral (lines 701, 705)
- RegexLiteral (line 730)
- ListLiteral (line 807)
- HashLiteral (line 834)

**Step 2: Compile to verify**

Run: `mvn compile -q`
Expected: BUILD SUCCESS

**Step 3: Commit**

```bash
git add src/main/java/org/dromara/easytl/parser/Parser.java
git commit -m "feat: pass Token to literal node constructors in Parser"
```

---

## Task 20: Modify Parser - All Statement Nodes

**Files:**
- Modify: `src/main/java/org/dromara/easytl/parser/Parser.java`

**Step 1: Update all statement node creations**

Modify all places where the following nodes are created:
- IfStatement (line 272)
- ForStatement (line 300)
- SwitchStatement (line 343)
- CaseClause (line 320)
- LetStatement (line 361)
- ExtendsStatement (line 368)
- ImportStatement (line 385)
- IfExpression (line 415)
- ForExpression (line 439)

**Step 2: Compile to verify**

Run: `mvn compile -q`
Expected: BUILD SUCCESS

**Step 3: Commit**

```bash
git add src/main/java/org/dromara/easytl/parser/Parser.java
git commit -m "feat: pass Token to statement node constructors in Parser"
```

---

## Task 21: Run All Tests

**Files:**
- None (verification only)

**Step 1: Run all tests**

Run: `mvn test`
Expected: All tests pass

**Step 2: If any tests fail, investigate and fix**

Review test output and fix any issues.

**Step 3: Commit any fixes**

```bash
git add -A
git commit -m "fix: resolve test failures after position info changes"
```

---

## Task 22: Update testArgumentsOutOfBound to Verify Position Info

**Files:**
- Modify: `src/test/java/org/dromara/easytl/EasyTLTest.java`

**Step 1: Read current test**

Read: `src/test/java/org/dromara/easytl/EasyTLTest.java` (lines 332-340)

**Step 2: Update test to verify position info in error message**

```java
@Test
void testArgumentsOutOfBound() {
    // {2} accessing out-of-bounds argument, should throw exception with position info
    ArgumentIndexOutOfBoundsException ex = assertThrows(ArgumentIndexOutOfBoundsException.class, () -> {
        EasyTL.template("First: {0}, Second: {1}, Third: {2}")
            .arguments("one", "two")
            .render();
    });

    // Verify error message contains position info
    String message = ex.getMessage();
    assertTrue(message.contains("out of bounds"), "Error message should mention out of bounds");

    // Verify Rust-style error format with line/column info
    assertTrue(message.contains("line:1") || message.contains("1 |"),
               "Error message should contain line number indicator");
}
```

**Step 3: Run test to verify**

Run: `mvn test -Dtest=EasyTLTest#testArgumentsOutOfBound`
Expected: Test passes with position info in error message

**Step 4: Commit**

```bash
git add src/test/java/org/dromara/easytl/EasyTLTest.java
git commit -m "test: update testArgumentsOutOfBound to verify position info in error message"
```

---

## Task 23: Clean up and Final Verification

**Files:**
- Remove: `src/test/java/org/dromara/easytl/ErrorMessageTest.java` (temporary test file)

**Step 1: Remove temporary test file**

```bash
rm src/test/java/org/dromara/easytl/ErrorMessageTest.java
```

**Step 2: Run full test suite**

Run: `mvn test`
Expected: All tests pass

**Step 3: Final commit**

```bash
git add -A
git commit -m "chore: clean up temporary test files"
```

---

## Summary

This plan adds position information to all AST nodes by:
1. Adding helper methods to ASTNode base class
2. Adding Token-accepting constructors to all AST node subclasses
3. Modifying Parser to pass Token references when creating nodes
4. Updating tests to verify the position information appears in error messages

The result will be Rust-style error messages with source code location for all runtime errors.