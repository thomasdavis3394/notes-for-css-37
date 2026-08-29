# Increment/Decrement & C-Style For Loop Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add C/C++ style increment/decrement operators (`++`/`--`), compound assignment operators (`+=`/`-=`/etc.), and C-style for loops to EasyTL.

**Architecture:** Orthogonal additions — each new feature adds independent AST nodes, parser paths, and evaluator methods. No existing code is modified, only extended. Follows the existing recursive descent parser pattern with visitor-based evaluation.

**Tech Stack:** Java 8, JUnit 5, no third-party dependencies.

---

## File Structure

### New Files
- `src/main/java/org/dromara/easytl/ast/IncrementExpression.java` — AST node for `++x`/`x++`/`--x`/`x--`
- `src/main/java/org/dromara/easytl/ast/CompoundAssignmentExpression.java` — AST node for `x += expr` etc.
- `src/main/java/org/dromara/easytl/ast/CompoundOperator.java` — Enum for compound assignment operators
- `src/main/java/org/dromara/easytl/ast/ClassicForStatement.java` — AST node for C-style for loop
- `src/test/java/org/dromara/easytl/IncrementDecrementTest.java` — End-to-end tests for all new features

### Modified Files
- `src/main/java/org/dromara/easytl/parser/TokenType.java` — Add 13 new token types
- `src/main/java/org/dromara/easytl/parser/Lexer.java` — Scan new operators in `readOperatorOrDelimiter()`
- `src/main/java/org/dromara/easytl/ast/ASTVisitor.java` — Add 3 new visitor methods
- `src/main/java/org/dromara/easytl/parser/Parser.java` — Add parse methods + modify existing methods
- `src/main/java/org/dromara/easytl/compiler/Evaluator.java` — Add 3 new visit methods + helper
- `src/main/java/org/dromara/easytl/runtime/OperatorEvaluator.java` — Add bitwise operations

---

## Task 1: TokenType — Add New Token Constants

**Files:**
- Modify: `src/main/java/org/dromara/easytl/parser/TokenType.java`

- [ ] **Step 1: Add new token type constants after the ASSIGN line (line 46)**

Insert after `public static final int ASSIGN = 46;` and before the advanced operators section:

```java
    // ==================== 自增/自减运算符 ====================
    public static final int INCREMENT = 47;               // ++
    public static final int DECREMENT = 48;               // --

    // ==================== 复合赋值运算符 ====================
    public static final int PLUS_ASSIGN = 59;             // +=
    public static final int MINUS_ASSIGN = 60;            // -=
    public static final int MULTIPLY_ASSIGN = 61;         // *=
    public static final int DIVIDE_ASSIGN = 62;           // /=
    public static final int MODULO_ASSIGN = 63;           // %=
    public static final int BIT_AND_ASSIGN = 64;          // &=
    public static final int BIT_OR_ASSIGN = 65;           // |=
    public static final int BIT_XOR_ASSIGN = 66;          // ^=
    public static final int LEFT_SHIFT_ASSIGN = 67;       // <<=
    public static final int RIGHT_SHIFT_ASSIGN = 68;      // >>=
    public static final int UNSIGNED_RIGHT_SHIFT_ASSIGN = 69;  // >>>=
```

- [ ] **Step 2: Add symbol mappings in `getSymbol()` method**

Add after the `case ASSIGN: return "=";` line in `getSymbol()`:

```java
            case INCREMENT: return "++";
            case DECREMENT: return "--";
            case PLUS_ASSIGN: return "+=";
            case MINUS_ASSIGN: return "-=";
            case MULTIPLY_ASSIGN: return "*=";
            case DIVIDE_ASSIGN: return "/=";
            case MODULO_ASSIGN: return "%=";
            case BIT_AND_ASSIGN: return "&=";
            case BIT_OR_ASSIGN: return "|=";
            case BIT_XOR_ASSIGN: return "^=";
            case LEFT_SHIFT_ASSIGN: return "<<=";
            case RIGHT_SHIFT_ASSIGN: return ">>=";
            case UNSIGNED_RIGHT_SHIFT_ASSIGN: return ">>>=";
```

- [ ] **Step 3: Add name mappings in `getName()` method**

Add after the `case ASSIGN: return "ASSIGN";` line in `getName()`:

```java
            case INCREMENT: return "INCREMENT";
            case DECREMENT: return "DECREMENT";
            case PLUS_ASSIGN: return "PLUS_ASSIGN";
            case MINUS_ASSIGN: return "MINUS_ASSIGN";
            case MULTIPLY_ASSIGN: return "MULTIPLY_ASSIGN";
            case DIVIDE_ASSIGN: return "DIVIDE_ASSIGN";
            case MODULO_ASSIGN: return "MODULO_ASSIGN";
            case BIT_AND_ASSIGN: return "BIT_AND_ASSIGN";
            case BIT_OR_ASSIGN: return "BIT_OR_ASSIGN";
            case BIT_XOR_ASSIGN: return "BIT_XOR_ASSIGN";
            case LEFT_SHIFT_ASSIGN: return "LEFT_SHIFT_ASSIGN";
            case RIGHT_SHIFT_ASSIGN: return "RIGHT_SHIFT_ASSIGN";
            case UNSIGNED_RIGHT_SHIFT_ASSIGN: return "UNSIGNED_RIGHT_SHIFT_ASSIGN";
```

- [ ] **Step 4: Compile to verify**

Run: `mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 5: Commit**

```bash
git add src/main/java/org/dromara/easytl/parser/TokenType.java
git commit -m "feat(token): add INCREMENT, DECREMENT, and compound assignment token types"
```

---

## Task 2: Lexer — Scan New Operators

**Files:**
- Modify: `src/main/java/org/dromara/easytl/parser/Lexer.java`

- [ ] **Step 1: Update `case '+'` in `readOperatorOrDelimiter()` (line 1576)**

Replace:
```java
            case '+': return new Token(TokenType.PLUS, "+", line, column, source.getEndColumn(), position);
```
With:
```java
            case '+':
                if (source.peek() == '+') {
                    source.read(); // consume second +
                    return new Token(TokenType.INCREMENT, "++", line, column, source.getEndColumn(), position);
                } else if (source.peek() == '=') {
                    source.read(); // consume =
                    return new Token(TokenType.PLUS_ASSIGN, "+=", line, column, source.getEndColumn(), position);
                }
                return new Token(TokenType.PLUS, "+", line, column, source.getEndColumn(), position);
```

- [ ] **Step 2: Update `case '-'` block (line 1577-1583)**

Replace:
```java
            case '-':
                // Check for arrow operator ->
                if (source.peek() == '>') {
                    source.read(); // consume '>'
                    return new Token(TokenType.ARROW, "->", line, column, source.getEndColumn(), position);
                }
                return new Token(TokenType.MINUS, "-", line, column, source.getEndColumn(), position);
```
With:
```java
            case '-':
                if (source.peek() == '-') {
                    source.read(); // consume second -
                    return new Token(TokenType.DECREMENT, "--", line, column, source.getEndColumn(), position);
                } else if (source.peek() == '>') {
                    source.read(); // consume >
                    return new Token(TokenType.ARROW, "->", line, column, source.getEndColumn(), position);
                } else if (source.peek() == '=') {
                    source.read(); // consume =
                    return new Token(TokenType.MINUS_ASSIGN, "-=", line, column, source.getEndColumn(), position);
                }
                return new Token(TokenType.MINUS, "-", line, column, source.getEndColumn(), position);
```

- [ ] **Step 3: Update `case '*'` (line 1584)**

Replace:
```java
            case '*': return new Token(TokenType.MULTIPLY, "*", line, column, source.getEndColumn(), position);
```
With:
```java
            case '*':
                if (source.peek() == '=') {
                    source.read();
                    return new Token(TokenType.MULTIPLY_ASSIGN, "*=", line, column, source.getEndColumn(), position);
                }
                return new Token(TokenType.MULTIPLY, "*", line, column, source.getEndColumn(), position);
```

- [ ] **Step 4: Update `case '/'` (line 1587-1588)**

Replace:
```java
            case '/':
                return new Token(TokenType.DIVIDE, "/", line, column, source.getEndColumn(), position);
```
With:
```java
            case '/':
                if (source.peek() == '=') {
                    source.read();
                    return new Token(TokenType.DIVIDE_ASSIGN, "/=", line, column, source.getEndColumn(), position);
                }
                return new Token(TokenType.DIVIDE, "/", line, column, source.getEndColumn(), position);
```

- [ ] **Step 5: Update `case '%'` (line 1585)**

Replace:
```java
            case '%': return new Token(TokenType.MODULO, "%", line, column, source.getEndColumn(), position);
```
With:
```java
            case '%':
                if (source.peek() == '=') {
                    source.read();
                    return new Token(TokenType.MODULO_ASSIGN, "%=", line, column, source.getEndColumn(), position);
                }
                return new Token(TokenType.MODULO, "%", line, column, source.getEndColumn(), position);
```

- [ ] **Step 6: Update `case '&'` (line 1665-1670)**

Replace:
```java
            case '&':
                if (source.peek() == '&') {
                    source.read();
                    return new Token(TokenType.AND, "&&", line, column, source.getEndColumn(), position);
                }
                throw new TemplateSyntaxException("Unknown character: &", line, column, line, source.getEndColumn(), source.getSourceText());
```
With:
```java
            case '&':
                if (source.peek() == '&') {
                    source.read();
                    return new Token(TokenType.AND, "&&", line, column, source.getEndColumn(), position);
                } else if (source.peek() == '=') {
                    source.read();
                    return new Token(TokenType.BIT_AND_ASSIGN, "&=", line, column, source.getEndColumn(), position);
                }
                throw new TemplateSyntaxException("Unknown character: &", line, column, line, source.getEndColumn(), source.getSourceText());
```

- [ ] **Step 7: Update `case '|'` (line 1672-1677)**

Replace:
```java
            case '|':
                if (source.peek() == '|') {
                    source.read();
                    return new Token(TokenType.OR, "||", line, column, source.getEndColumn(), position);
                }
                throw new TemplateSyntaxException("Unknown character: |", line, column, line, source.getEndColumn(), source.getSourceText());
```
With:
```java
            case '|':
                if (source.peek() == '|') {
                    source.read();
                    return new Token(TokenType.OR, "||", line, column, source.getEndColumn(), position);
                } else if (source.peek() == '=') {
                    source.read();
                    return new Token(TokenType.BIT_OR_ASSIGN, "|=", line, column, source.getEndColumn(), position);
                }
                throw new TemplateSyntaxException("Unknown character: |", line, column, line, source.getEndColumn(), source.getSourceText());
```

- [ ] **Step 8: Add `case '^'` before `case ':'` (line 1689)**

Insert before `case ':':`:

```java
            case '^':
                if (source.peek() == '=') {
                    source.read();
                    return new Token(TokenType.BIT_XOR_ASSIGN, "^=", line, column, source.getEndColumn(), position);
                }
                throw new TemplateSyntaxException("Unknown character: ^", line, column, line, source.getEndColumn(), source.getSourceText());
```

- [ ] **Step 9: Update `case '<'` (line 1623-1628)**

Replace:
```java
            case '<':
                if (source.peek() == '=') {
                    source.read();
                    return new Token(TokenType.LTE, "<=", line, column, source.getEndColumn(), position);
                }
                return new Token(TokenType.LT, "<", line, column, source.getEndColumn(), position);
```
With:
```java
            case '<':
                if (source.peek() == '=') {
                    source.read();
                    return new Token(TokenType.LTE, "<=", line, column, source.getEndColumn(), position);
                } else if (source.peek() == '<') {
                    source.read(); // consume second <
                    if (source.peek() == '=') {
                        source.read();
                        return new Token(TokenType.LEFT_SHIFT_ASSIGN, "<<=", line, column, source.getEndColumn(), position);
                    }
                    throw new TemplateSyntaxException("Unexpected token: <<", line, column, line, source.getEndColumn(), source.getSourceText());
                }
                return new Token(TokenType.LT, "<", line, column, source.getEndColumn(), position);
```

- [ ] **Step 10: Update `case '>'` (line 1630-1650)**

Add `>>=` and `>>>=` scanning. Replace the entire `case '>'` block:

```java
            case '>':
                if (source.peek() == '=') {
                    source.read();
                    return new Token(TokenType.GTE, ">=", line, column, source.getEndColumn(), position);
                } else if (source.peek() == '>') {
                    source.read(); // consume second >
                    if (source.peek() == '>') {
                        source.read(); // consume third >
                        if (source.peek() == '=') {
                            source.read();
                            return new Token(TokenType.UNSIGNED_RIGHT_SHIFT_ASSIGN, ">>>=", line, column, source.getEndColumn(), position);
                        }
                        throw new TemplateSyntaxException("Unexpected token: >>>", line, column, line, source.getEndColumn(), source.getSourceText());
                    } else if (source.peek() == '=') {
                        source.read();
                        return new Token(TokenType.RIGHT_SHIFT_ASSIGN, ">>=", line, column, source.getEndColumn(), position);
                    }
                    throw new TemplateSyntaxException("Unexpected token: >>", line, column, line, source.getEndColumn(), source.getSourceText());
                } else if (source.peek() == '.') {
                    // 区间运算符 >.. 或 >..<
                    source.mark();
                    source.read(); // .
                    if (source.peek() == '.') {
                        source.read(); // .
                        if (source.peek() == '<') {
                            source.read(); // <
                            return new Token(TokenType.RANGE_OPEN, ">..<", line, column, source.getEndColumn(), position);
                        }
                        return new Token(TokenType.RANGE_LEFT_OPEN_RIGHT_CLOSED, ">..", line, column, source.getEndColumn(), position);
                    }
                    source.reset();
                }
                return new Token(TokenType.GT, ">", line, column, source.getEndColumn(), position);
```

- [ ] **Step 11: Compile to verify**

Run: `mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 12: Commit**

```bash
git add src/main/java/org/dromara/easytl/parser/Lexer.java
git commit -m "feat(lexer): scan ++, --, compound assignment, and shift assignment operators"
```

---

## Task 3: AST — New Node Types

**Files:**
- Create: `src/main/java/org/dromara/easytl/ast/IncrementExpression.java`
- Create: `src/main/java/org/dromara/easytl/ast/CompoundOperator.java`
- Create: `src/main/java/org/dromara/easytl/ast/CompoundAssignmentExpression.java`
- Create: `src/main/java/org/dromara/easytl/ast/ClassicForStatement.java`
- Modify: `src/main/java/org/dromara/easytl/ast/ASTVisitor.java`

- [ ] **Step 1: Create IncrementExpression.java**

```java
package org.dromara.easytl.ast;

/**
 * 自增/自减表达式节点
 * 表示 ++x, x++, --x, x-- 表达式
 *
 * @author EasyTL Team
 */
public class IncrementExpression extends ASTNode {

    /** 操作数（必须是可赋值目标：变量、属性访问、索引访问） */
    private final ASTNode operand;

    /** 是否为前缀运算（true: ++x, false: x++） */
    private final boolean isPrefix;

    /** 是否为自增（true: ++, false: --） */
    private final boolean isIncrement;

    public IncrementExpression(ASTNode operand, boolean isPrefix, boolean isIncrement) {
        this.operand = operand;
        this.isPrefix = isPrefix;
        this.isIncrement = isIncrement;
    }

    public IncrementExpression(ASTNode operand, boolean isPrefix, boolean isIncrement, Position position) {
        super(position);
        this.operand = operand;
        this.isPrefix = isPrefix;
        this.isIncrement = isIncrement;
    }

    public ASTNode getOperand() {
        return operand;
    }

    public boolean isPrefix() {
        return isPrefix;
    }

    public boolean isIncrement() {
        return isIncrement;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitIncrementExpression(this);
    }

    @Override
    public String toString() {
        String op = isIncrement ? "++" : "--";
        if (isPrefix) {
            return "IncrementExpression{" + op + operand + '}';
        }
        return "IncrementExpression{" + operand + op + '}';
    }
}
```

- [ ] **Step 2: Create CompoundOperator.java**

```java
package org.dromara.easytl.ast;

/**
 * 复合赋值运算符枚举
 *
 * @author EasyTL Team
 */
public enum CompoundOperator {

    PLUS_ASSIGN("+="),
    MINUS_ASSIGN("-="),
    MULTIPLY_ASSIGN("*="),
    DIVIDE_ASSIGN("/="),
    MODULO_ASSIGN("%="),
    BIT_AND_ASSIGN("&="),
    BIT_OR_ASSIGN("|="),
    BIT_XOR_ASSIGN("^="),
    LEFT_SHIFT_ASSIGN("<<="),
    RIGHT_SHIFT_ASSIGN(">>="),
    UNSIGNED_RIGHT_SHIFT_ASSIGN(">>>=");

    private final String symbol;

    CompoundOperator(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }
}
```

- [ ] **Step 3: Create CompoundAssignmentExpression.java**

```java
package org.dromara.easytl.ast;

/**
 * 复合赋值表达式节点
 * 表示 x += expr, x -= expr 等复合赋值表达式
 *
 * @author EasyTL Team
 */
public class CompoundAssignmentExpression extends ASTNode {

    /** 赋值目标（必须是可赋值表达式） */
    private final ASTNode target;

    /** 复合赋值运算符 */
    private final CompoundOperator operator;

    /** 右侧表达式 */
    private final ASTNode value;

    public CompoundAssignmentExpression(ASTNode target, CompoundOperator operator, ASTNode value) {
        this.target = target;
        this.operator = operator;
        this.value = value;
    }

    public CompoundAssignmentExpression(ASTNode target, CompoundOperator operator, ASTNode value, Position position) {
        super(position);
        this.target = target;
        this.operator = operator;
        this.value = value;
    }

    public ASTNode getTarget() {
        return target;
    }

    public CompoundOperator getOperator() {
        return operator;
    }

    public ASTNode getValue() {
        return value;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitCompoundAssignmentExpression(this);
    }

    @Override
    public String toString() {
        return "CompoundAssignmentExpression{" +
                target + " " + operator.getSymbol() + " " + value +
                '}';
    }
}
```

- [ ] **Step 4: Create ClassicForStatement.java**

```java
package org.dromara.easytl.ast;

import java.util.List;

/**
 * C 风格 for 循环语句节点
 * 格式：{% for let i = 0; condition; update }...{/% for }
 *
 * @author EasyTL Team
 */
public class ClassicForStatement extends StatementBlock {

    /** 初始化语句（通常为 let 声明，可为 null） */
    private final LetStatement initialization;

    /** 循环条件（可为 null，表示无限循环） */
    private final ASTNode condition;

    /** 更新表达式（可为 null） */
    private final ASTNode update;

    /** 循环体 */
    private final List<ASTNode> body;

    public ClassicForStatement(LetStatement initialization, ASTNode condition, ASTNode update, List<ASTNode> body) {
        this.initialization = initialization;
        this.condition = condition;
        this.update = update;
        this.body = body;
    }

    public ClassicForStatement(LetStatement initialization, ASTNode condition, ASTNode update, List<ASTNode> body, Position position) {
        super(position);
        this.initialization = initialization;
        this.condition = condition;
        this.update = update;
        this.body = body;
    }

    public LetStatement getInitialization() {
        return initialization;
    }

    public ASTNode getCondition() {
        return condition;
    }

    public ASTNode getUpdate() {
        return update;
    }

    public List<ASTNode> getBody() {
        return body;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitClassicForStatement(this);
    }

    @Override
    public String toString() {
        return "ClassicForStatement{" +
                "init=" + initialization +
                ", condition=" + condition +
                ", update=" + update +
                ", body=" + body +
                '}';
    }
}
```

- [ ] **Step 5: Add visitor methods to ASTVisitor.java**

Add these three methods to `ASTVisitor.java` (before the closing `}` of the interface):

```java

    // 自增/自减和复合赋值节点访问方法
    T visitIncrementExpression(IncrementExpression node);
    T visitCompoundAssignmentExpression(CompoundAssignmentExpression node);
    T visitClassicForStatement(ClassicForStatement node);
```

- [ ] **Step 6: Compile to verify**

Run: `mvn compile -q`
Expected: Compile will FAIL because Evaluator does not implement the new visitor methods yet. This is expected — we will fix it in Task 5.

If compilation fails with "does not override abstract method" errors, that confirms the AST nodes are correctly wired. Proceed to the next task.

- [ ] **Step 7: Commit**

```bash
git add src/main/java/org/dromara/easytl/ast/IncrementExpression.java \
        src/main/java/org/dromara/easytl/ast/CompoundOperator.java \
        src/main/java/org/dromara/easytl/ast/CompoundAssignmentExpression.java \
        src/main/java/org/dromara/easytl/ast/ClassicForStatement.java \
        src/main/java/org/dromara/easytl/ast/ASTVisitor.java
git commit -m "feat(ast): add IncrementExpression, CompoundAssignmentExpression, ClassicForStatement nodes"
```

---

## Task 4: Parser — Expression & Statement Parsing

**Files:**
- Modify: `src/main/java/org/dromara/easytl/parser/Parser.java`

This task modifies three areas of the parser:
1. `parseExpression()` → add compound assignment layer
2. `parseUnary()` → add prefix `++`/`--`
3. `parsePostfix()` → add postfix `++`/`--`
4. `parseForStatement()` → add C-style for fork

- [ ] **Step 1: Add `parseAssignmentExpression()` method and update `parseExpression()`**

Change `parseExpression()` (line 666-668) from:

```java
    public ASTNode parseExpression() {
        return parseElvis();
    }
```
To:

```java
    public ASTNode parseExpression() {
        return parseAssignmentExpression();
    }

    /**
     * 解析复合赋值表达式 (+=, -=, *=, /=, %=, &=, |=, ^=, <<=, >>=, >>>=)
     * 优先级最低（0），右结合
     */
    private ASTNode parseAssignmentExpression() {
        ASTNode expr = parseElvis();

        if (checkAny(TokenType.PLUS_ASSIGN, TokenType.MINUS_ASSIGN,
                     TokenType.MULTIPLY_ASSIGN, TokenType.DIVIDE_ASSIGN, TokenType.MODULO_ASSIGN,
                     TokenType.BIT_AND_ASSIGN, TokenType.BIT_OR_ASSIGN, TokenType.BIT_XOR_ASSIGN,
                     TokenType.LEFT_SHIFT_ASSIGN, TokenType.RIGHT_SHIFT_ASSIGN,
                     TokenType.UNSIGNED_RIGHT_SHIFT_ASSIGN)) {
            Token opToken = advance();
            ASTNode right = parseAssignmentExpression(); // 右结合递归

            // 验证左侧是可赋值目标
            validateAssignable(expr, opToken);

            CompoundOperator op = getCompoundOperator(opToken.getType());
            Position pos = new Position(expr.getStartLine(), expr.getStartColumn(),
                                       right.getEndLine(), right.getEndColumn());
            return new CompoundAssignmentExpression(expr, op, right, pos);
        }

        return expr;
    }

    /**
     * 获取复合赋值运算符
     */
    private CompoundOperator getCompoundOperator(int tokenType) {
        switch (tokenType) {
            case TokenType.PLUS_ASSIGN: return CompoundOperator.PLUS_ASSIGN;
            case TokenType.MINUS_ASSIGN: return CompoundOperator.MINUS_ASSIGN;
            case TokenType.MULTIPLY_ASSIGN: return CompoundOperator.MULTIPLY_ASSIGN;
            case TokenType.DIVIDE_ASSIGN: return CompoundOperator.DIVIDE_ASSIGN;
            case TokenType.MODULO_ASSIGN: return CompoundOperator.MODULO_ASSIGN;
            case TokenType.BIT_AND_ASSIGN: return CompoundOperator.BIT_AND_ASSIGN;
            case TokenType.BIT_OR_ASSIGN: return CompoundOperator.BIT_OR_ASSIGN;
            case TokenType.BIT_XOR_ASSIGN: return CompoundOperator.BIT_XOR_ASSIGN;
            case TokenType.LEFT_SHIFT_ASSIGN: return CompoundOperator.LEFT_SHIFT_ASSIGN;
            case TokenType.RIGHT_SHIFT_ASSIGN: return CompoundOperator.RIGHT_SHIFT_ASSIGN;
            case TokenType.UNSIGNED_RIGHT_SHIFT_ASSIGN: return CompoundOperator.UNSIGNED_RIGHT_SHIFT_ASSIGN;
            default: throw new TemplateSyntaxException("Unknown compound assignment operator", 0, 0, 0, 0, sourceText);
        }
    }

    /**
     * 验证表达式是否是合法的赋值目标
     */
    private void validateAssignable(ASTNode target, Token opToken) {
        if (!(target instanceof Identifier) &&
            !(target instanceof MemberAccess) &&
            !(target instanceof IndexAccess)) {
            throw new TemplateSyntaxException(
                "Invalid assignment target",
                target.getStartLine(), target.getStartColumn(),
                target.getEndLine(), target.getEndColumn(), sourceText);
        }
    }
```

Add the import at the top of Parser.java:
```java
import org.dromara.easytl.ast.CompoundOperator;
import org.dromara.easytl.ast.CompoundAssignmentExpression;
import org.dromara.easytl.ast.IncrementExpression;
import org.dromara.easytl.ast.ClassicForStatement;
```

- [ ] **Step 2: Update `parseUnary()` to handle prefix `++`/`--`**

In `parseUnary()` (line 836), add INCREMENT/DECREMENT handling before the existing `if (checkAny(TokenType.NOT, ...))` check. Change:

```java
    private ASTNode parseUnary() {
        // 检查是否是类型转换
        if (isCastExpression()) {
            return parseCastExpression();
        }

        if (checkAny(TokenType.NOT, TokenType.MINUS, TokenType.PLUS)) {
```
To:
```java
    private ASTNode parseUnary() {
        // 检查是否是类型转换
        if (isCastExpression()) {
            return parseCastExpression();
        }

        // 前缀自增/自减 ++x, --x
        if (checkAny(TokenType.INCREMENT, TokenType.DECREMENT)) {
            Token opToken = advance();
            ASTNode operand = parseUnary();
            validateAssignable(operand, opToken);
            boolean isIncrement = opToken.getType() == TokenType.INCREMENT;
            Position pos = new Position(opToken.getLine(), opToken.getStartColumn(),
                                       operand.getEndLine(), operand.getEndColumn());
            return new IncrementExpression(operand, true, isIncrement, pos);
        }

        if (checkAny(TokenType.NOT, TokenType.MINUS, TokenType.PLUS)) {
```

- [ ] **Step 3: Update `parsePostfix()` to handle postfix `++`/`--`**

At the end of the `while (true)` loop in `parsePostfix()` (before the final `break;` at line 927), add postfix `++`/`--` handling. Find the `else { break; }` at line 926 and change:

```java
            } else {
                break;
            }
```
To:
```java
            } else if (check(TokenType.INCREMENT)) {
                Token opToken = advance();
                validateAssignable(expr, opToken);
                Position pos = new Position(expr.getStartLine(), expr.getStartColumn(),
                                           opToken.getLine(), opToken.getEndColumn());
                expr = new IncrementExpression(expr, false, true, pos);
            } else if (check(TokenType.DECREMENT)) {
                Token opToken = advance();
                validateAssignable(expr, opToken);
                Position pos = new Position(expr.getStartLine(), expr.getStartColumn(),
                                           opToken.getLine(), opToken.getEndColumn());
                expr = new IncrementExpression(expr, false, false, pos);
            } else {
                break;
            }
```

- [ ] **Step 4: Update `parseForStatement()` to fork between for-in and C-style for**

Replace the entire `parseForStatement()` method (lines 282-308) with:

```java
    private ASTNode parseForStatement() {
        expect(TokenType.FOR);

        // 检查是否是 C 风格 for 循环：{% for let i = 0; cond; update }...{/% for }
        if (check(TokenType.LET)) {
            return parseClassicForStatement();
        }

        // 原有的 for-in 语法：{% for item in items }...{/% for }
        Token first = expect(TokenType.IDENTIFIER);
        String itemVar = first.getValue();
        String indexVar = null;

        if (check(TokenType.COMMA)) {
            advance();
            Token second = expect(TokenType.IDENTIFIER);
            indexVar = itemVar; // 第一个是索引
            itemVar = second.getValue(); // 第二个是值
        }

        expect(TokenType.IN);
        ASTNode collection = parseExpression();
        expectBlockEnd();

        TemplateNode body = parseTemplate();

        expect(TokenType.STMT_CLOSE);
        expect(TokenType.FOR);
        expect(TokenType.RIGHT_BRACE);

        return new ForStatement(indexVar, itemVar, collection, body.getChildren());
    }

    /**
     * 解析 C 风格 for 循环
     * 格式：{% for let i = 0; condition; update }...{/% for }
     */
    private ASTNode parseClassicForStatement() {
        Token letToken = expect(TokenType.LET);
        Token nameToken = expect(TokenType.IDENTIFIER);
        expect(TokenType.ASSIGN);
        ASTNode initValue = parseExpression();

        LetStatement init = new LetStatement(nameToken.getValue(), initValue,
            new Position(letToken.getLine(), letToken.getStartColumn(),
                        nameToken.getLine(), nameToken.getEndColumn()));

        expect(TokenType.SEMICOLON);

        // 条件（可为空，表示无限循环）
        ASTNode condition = null;
        if (!check(TokenType.SEMICOLON)) {
            condition = parseExpression();
        }

        expect(TokenType.SEMICOLON);

        // 更新表达式（可为空）
        ASTNode update = null;
        if (!check(TokenType.RIGHT_BRACE)) {
            update = parseExpression();
        }

        expectBlockEnd();

        TemplateNode body = parseTemplate();

        expect(TokenType.STMT_CLOSE);
        expect(TokenType.FOR);
        expect(TokenType.RIGHT_BRACE);

        return new ClassicForStatement(init, condition, update, body.getChildren());
    }
```

- [ ] **Step 5: Compile to verify**

Run: `mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 6: Commit**

```bash
git add src/main/java/org/dromara/easytl/parser/Parser.java
git commit -m "feat(parser): parse increment/decrement, compound assignment, C-style for loop"
```

---

## Task 5: OperatorEvaluator — Add Bitwise Operations

**Files:**
- Modify: `src/main/java/org/dromara/easytl/runtime/OperatorEvaluator.java`

- [ ] **Step 1: Add bitwise operation methods after `modulo()` (after line 85)**

```java
    // ==================== 位运算 ====================

    /**
     * 按位与运算
     */
    public static Object bitwiseAnd(Object a, Object b) {
        if (a == null || b == null) {
            throw new TemplateRuntimeException("Cannot perform bitwise operation on null");
        }
        return bitwiseOperation(a, b, BitwiseOp.AND);
    }

    /**
     * 按位或运算
     */
    public static Object bitwiseOr(Object a, Object b) {
        if (a == null || b == null) {
            throw new TemplateRuntimeException("Cannot perform bitwise operation on null");
        }
        return bitwiseOperation(a, b, BitwiseOp.OR);
    }

    /**
     * 按位异或运算
     */
    public static Object bitwiseXor(Object a, Object b) {
        if (a == null || b == null) {
            throw new TemplateRuntimeException("Cannot perform bitwise operation on null");
        }
        return bitwiseOperation(a, b, BitwiseOp.XOR);
    }

    /**
     * 左移运算
     */
    public static Object leftShift(Object a, Object b) {
        if (a == null || b == null) {
            throw new TemplateRuntimeException("Cannot perform shift operation on null");
        }
        return bitwiseOperation(a, b, BitwiseOp.LEFT_SHIFT);
    }

    /**
     * 右移运算（带符号）
     */
    public static Object rightShift(Object a, Object b) {
        if (a == null || b == null) {
            throw new TemplateRuntimeException("Cannot perform shift operation on null");
        }
        return bitwiseOperation(a, b, BitwiseOp.RIGHT_SHIFT);
    }

    /**
     * 无符号右移运算
     */
    public static Object unsignedRightShift(Object a, Object b) {
        if (a == null || b == null) {
            throw new TemplateRuntimeException("Cannot perform shift operation on null");
        }
        return bitwiseOperation(a, b, BitwiseOp.UNSIGNED_RIGHT_SHIFT);
    }
```

- [ ] **Step 2: Add BitwiseOp enum and bitwiseOperation helper**

Add after the `ArithmeticOp` enum (line 411-413):

```java
    /**
     * 位运算类型枚举
     */
    private enum BitwiseOp {
        AND, OR, XOR, LEFT_SHIFT, RIGHT_SHIFT, UNSIGNED_RIGHT_SHIFT
    }
```

Add the `bitwiseOperation` helper method:

```java
    /**
     * 执行位运算
     */
    private static Object bitwiseOperation(Object a, Object b, BitwiseOp op) {
        NumberType type = promoteType(a, b);

        // 位运算只支持整数类型
        if (type == NumberType.DOUBLE || type == NumberType.BIG_DECIMAL) {
            throw new TemplateRuntimeException("Cannot perform bitwise operation on floating-point numbers");
        }

        switch (type) {
            case BIG_INTEGER:
                return bitwiseBigInteger(toBigInteger(a), toBigInteger(b), op);
            case LONG:
                return bitwiseLong(toLong(a), toLong(b), op);
            case INT:
                return bitwiseInt(toInt(a), toInt(b), op);
            default:
                return bitwiseLong(toLong(a), toLong(b), op);
        }
    }

    private static int bitwiseInt(int a, int b, BitwiseOp op) {
        switch (op) {
            case AND: return a & b;
            case OR: return a | b;
            case XOR: return a ^ b;
            case LEFT_SHIFT: return a << b;
            case RIGHT_SHIFT: return a >> b;
            case UNSIGNED_RIGHT_SHIFT: return a >>> b;
            default: throw new TemplateRuntimeException("Unknown bitwise operation");
        }
    }

    private static long bitwiseLong(long a, long b, BitwiseOp op) {
        switch (op) {
            case AND: return a & b;
            case OR: return a | b;
            case XOR: return a ^ b;
            case LEFT_SHIFT: return a << b;
            case RIGHT_SHIFT: return a >> b;
            case UNSIGNED_RIGHT_SHIFT: return a >>> b;
            default: throw new TemplateRuntimeException("Unknown bitwise operation");
        }
    }

    private static BigInteger bitwiseBigInteger(BigInteger a, BigInteger b, BitwiseOp op) {
        switch (op) {
            case AND: return a.and(b);
            case OR: return a.or(b);
            case XOR: return a.xor(b);
            case LEFT_SHIFT: return a.shiftLeft(toInt(b));
            case RIGHT_SHIFT: return a.shiftRight(toInt(b));
            default: throw new TemplateRuntimeException("Unsupported bitwise operation for BigInteger: " + op);
        }
    }
```

- [ ] **Step 3: Compile to verify**

Run: `mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add src/main/java/org/dromara/easytl/runtime/OperatorEvaluator.java
git commit -m "feat(runtime): add bitwise and shift operations to OperatorEvaluator"
```

---

## Task 6: Evaluator — Evaluate New AST Nodes

**Files:**
- Modify: `src/main/java/org/dromara/easytl/compiler/Evaluator.java`

- [ ] **Step 1: Add import statements**

Add to the imports section:
```java
import org.dromara.easytl.ast.IncrementExpression;
import org.dromara.easytl.ast.CompoundAssignmentExpression;
import org.dromara.easytl.ast.CompoundOperator;
import org.dromara.easytl.ast.ClassicForStatement;
import org.dromara.easytl.runtime.ReflectionUtils;
```

(Check if `ReflectionUtils` is already imported — if so, skip that import.)

- [ ] **Step 2: Add `visitIncrementExpression()` method**

Add after `visitUnaryExpression()` (after line 583):

```java
    @Override
    public Object visitIncrementExpression(IncrementExpression node) {
        Object oldValue = readAssignableValue(node.getOperand());
        Object newValue;

        if (node.isIncrement()) {
            newValue = OperatorEvaluator.add(oldValue, 1);
        } else {
            newValue = OperatorEvaluator.subtract(oldValue, 1);
        }

        writeAssignableValue(node.getOperand(), newValue);

        return node.isPrefix() ? newValue : oldValue;
    }
```

- [ ] **Step 3: Add `visitCompoundAssignmentExpression()` method**

Add after `visitIncrementExpression()`:

```java
    @Override
    public Object visitCompoundAssignmentExpression(CompoundAssignmentExpression node) {
        Object targetValue = readAssignableValue(node.getTarget());
        Object rightValue = node.getValue().accept(this);
        Object newValue;

        switch (node.getOperator()) {
            case PLUS_ASSIGN:
                newValue = OperatorEvaluator.add(targetValue, rightValue);
                break;
            case MINUS_ASSIGN:
                newValue = OperatorEvaluator.subtract(targetValue, rightValue);
                break;
            case MULTIPLY_ASSIGN:
                newValue = OperatorEvaluator.multiply(targetValue, rightValue);
                break;
            case DIVIDE_ASSIGN:
                newValue = OperatorEvaluator.divide(targetValue, rightValue);
                break;
            case MODULO_ASSIGN:
                newValue = OperatorEvaluator.modulo(targetValue, rightValue);
                break;
            case BIT_AND_ASSIGN:
                newValue = OperatorEvaluator.bitwiseAnd(targetValue, rightValue);
                break;
            case BIT_OR_ASSIGN:
                newValue = OperatorEvaluator.bitwiseOr(targetValue, rightValue);
                break;
            case BIT_XOR_ASSIGN:
                newValue = OperatorEvaluator.bitwiseXor(targetValue, rightValue);
                break;
            case LEFT_SHIFT_ASSIGN:
                newValue = OperatorEvaluator.leftShift(targetValue, rightValue);
                break;
            case RIGHT_SHIFT_ASSIGN:
                newValue = OperatorEvaluator.rightShift(targetValue, rightValue);
                break;
            case UNSIGNED_RIGHT_SHIFT_ASSIGN:
                newValue = OperatorEvaluator.unsignedRightShift(targetValue, rightValue);
                break;
            default:
                throw runtimeError("Unknown compound assignment operator: " + node.getOperator(), node);
        }

        writeAssignableValue(node.getTarget(), newValue);

        return newValue;
    }
```

- [ ] **Step 4: Add `visitClassicForStatement()` method**

Add after `visitForStatement()` (after line 1060):

```java
    @Override
    public Object visitClassicForStatement(ClassicForStatement node) {
        StringBuilder result = new StringBuilder();

        // 创建循环作用域
        Context loopContext = context.createChildContext();

        // 执行初始化
        if (node.getInitialization() != null) {
            // 直接设置变量到循环作用域（不通过 visitor，因为 LetStatement 使用的是当前 context）
            Object initValue = node.getInitialization().getValue().accept(
                new Evaluator(loopContext, templateLoader, sourceText, templateJavaClassImportEnabled, importsMap));
            loopContext.put(node.getInitialization().getVariableName(), initValue);
        }

        while (true) {
            // 检查条件
            if (node.getCondition() != null) {
                Object condValue = node.getCondition().accept(
                    new Evaluator(loopContext, templateLoader, sourceText, templateJavaClassImportEnabled, importsMap));
                if (!OperatorEvaluator.toBoolean(condValue)) {
                    break;
                }
            }

            // 执行循环体
            Evaluator loopEvaluator = new Evaluator(loopContext, templateLoader, sourceText, templateJavaClassImportEnabled, importsMap);
            for (ASTNode child : node.getBody()) {
                Object value = child.accept(loopEvaluator);
                if (value != null) {
                    result.append(value.toString());
                }
            }

            // 执行更新
            if (node.getUpdate() != null) {
                node.getUpdate().accept(
                    new Evaluator(loopContext, templateLoader, sourceText, templateJavaClassImportEnabled, importsMap));
            }
        }

        return result.toString();
    }
```

- [ ] **Step 5: Add helper methods `readAssignableValue()` and `writeAssignableValue()`**

Add to the Evaluator class:

```java
    /**
     * 读取可赋值目标的当前值
     */
    private Object readAssignableValue(ASTNode target) {
        return target.accept(this);
    }

    /**
     * 向可赋值目标写入新值
     */
    private void writeAssignableValue(ASTNode target, Object value) {
        if (target instanceof Identifier) {
            String name = ((Identifier) target).getName();
            context.put(name, value);
        } else if (target instanceof MemberAccess) {
            MemberAccess access = (MemberAccess) target;
            Object obj = access.getObject().accept(this);
            if (obj == null) {
                throw runtimeError("Cannot set property on null", target);
            }
            ReflectionUtils.setProperty(obj, access.getMemberName(), value);
        } else if (target instanceof IndexAccess) {
            IndexAccess access = (IndexAccess) target;
            Object obj = access.getObject().accept(this);
            Object index = access.getIndex().accept(this);
            if (obj == null) {
                throw runtimeError("Cannot set index on null", target);
            }
            ReflectionUtils.setIndexedValue(obj, index, value);
        } else {
            throw runtimeError("Invalid assignment target", target);
        }
    }
```

- [ ] **Step 6: Check if `ReflectionUtils.setIndexedValue()` exists**

Run: `grep -n "setIndexedValue" src/main/java/org/dromara/easytl/runtime/ReflectionUtils.java`

If it does NOT exist, add the following method to ReflectionUtils.java:

```java
    /**
     * 设置索引值（List、数组、Map）
     */
    public static void setIndexedValue(Object obj, Object index, Object value) {
        if (obj == null) {
            throw new TemplateRuntimeException("Cannot set index of null");
        }

        if (obj instanceof List) {
            List<?> list = (List<?>) obj;
            int idx = toInt(index);
            if (idx < 0 || idx >= list.size()) {
                throw new TemplateRuntimeException("Index out of bounds: " + idx);
            }
            @SuppressWarnings("unchecked")
            List<Object> mutableList = (List<Object>) list;
            mutableList.set(idx, value);
        } else if (obj.getClass().isArray()) {
            int idx = toInt(index);
            java.lang.reflect.Array.set(obj, idx, value);
        } else if (obj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<Object, Object> map = (Map<Object, Object>) obj;
            map.put(index, value);
        } else {
            throw new TemplateRuntimeException("Cannot set index on " + obj.getClass().getName());
        }
    }

    private static int toInt(Object value) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        throw new TemplateRuntimeException("Cannot convert " + value.getClass().getName() + " to int for index");
    }
```

- [ ] **Step 7: Compile to verify**

Run: `mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 8: Run all existing tests for regression**

Run: `mvn test -q`
Expected: All existing tests pass (no regressions).

- [ ] **Step 9: Commit**

```bash
git add src/main/java/org/dromara/easytl/compiler/Evaluator.java \
        src/main/java/org/dromara/easytl/runtime/ReflectionUtils.java
git commit -m "feat(evaluator): evaluate IncrementExpression, CompoundAssignmentExpression, ClassicForStatement"
```

---

## Task 7: End-to-End Tests

**Files:**
- Create: `src/test/java/org/dromara/easytl/IncrementDecrementTest.java`

- [ ] **Step 1: Write the test file**

```java
package org.dromara.easytl;

import org.dromara.easytl.exception.TemplateCompileException;
import org.dromara.easytl.exception.TemplateRuntimeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 自增/自减、复合赋值、C 风格 for 循环的端到端测试
 */
class IncrementDecrementTest {

    private TemplateEngine engine;

    @BeforeEach
    void setUp() {
        engine = new TemplateEngine();
    }

    // ==================== 自增/自减 ====================

    @Nested
    class IncrementDecrementTests {

        @Test
        void postfixIncrement_returnsOldValue() {
            Context ctx = new Context();
            ctx.put("i", 5);
            Template t = engine.compile("{i++}");
            assertEquals("5", t.render(ctx));
            assertEquals(6, ctx.get("i"));
        }

        @Test
        void prefixIncrement_returnsNewValue() {
            Context ctx = new Context();
            ctx.put("i", 5);
            Template t = engine.compile("{++i}");
            assertEquals("6", t.render(ctx));
            assertEquals(6, ctx.get("i"));
        }

        @Test
        void postfixDecrement_returnsOldValue() {
            Context ctx = new Context();
            ctx.put("i", 5);
            Template t = engine.compile("{i--}");
            assertEquals("5", t.render(ctx));
            assertEquals(4, ctx.get("i"));
        }

        @Test
        void prefixDecrement_returnsNewValue() {
            Context ctx = new Context();
            ctx.put("i", 5);
            Template t = engine.compile("{--i}");
            assertEquals("4", t.render(ctx));
            assertEquals(4, ctx.get("i"));
        }

        @Test
        void postfixIncrementInExpression() {
            Context ctx = new Context();
            ctx.put("i", 1);
            Template t = engine.compile("{i++ * 2}");
            assertEquals("2", t.render(ctx));
            assertEquals(2, ctx.get("i"));
        }

        @Test
        void prefixIncrementInExpression() {
            Context ctx = new Context();
            ctx.put("i", 1);
            Template t = engine.compile("{++i * 2}");
            assertEquals("4", t.render(ctx));
            assertEquals(2, ctx.get("i"));
        }

        @Test
        void memberAccessPostfixIncrement() {
            Context ctx = new Context();
            Map<String, Object> obj = new HashMap<>();
            obj.put("count", 0);
            ctx.put("obj", obj);
            Template t = engine.compile("{obj.count++}");
            assertEquals("0", t.render(ctx));
            assertEquals(1, obj.get("count"));
        }

        @Test
        void indexAccessPostfixIncrement() {
            Context ctx = new Context();
            List<Integer> arr = new ArrayList<>(Arrays.asList(10, 20, 30));
            ctx.put("arr", arr);
            ctx.put("i", 1);
            Template t = engine.compile("{arr[i]++}");
            assertEquals("20", t.render(ctx));
            assertEquals(21, arr.get(1));
        }

        @Test
        void invalidTarget_throwsSyntaxError() {
            assertThrows(TemplateCompileException.class, () -> engine.compile("{++5}"));
            assertThrows(TemplateCompileException.class, () -> engine.compile("{\"hello\"++}"));
        }
    }

    // ==================== 复合赋值 ====================

    @Nested
    class CompoundAssignmentTests {

        @Test
        void plusAssign() {
            Context ctx = new Context();
            ctx.put("total", 10);
            Template t = engine.compile("{total += 5}");
            assertEquals("15", t.render(ctx));
            assertEquals(15, ctx.get("total"));
        }

        @Test
        void minusAssign() {
            Context ctx = new Context();
            ctx.put("x", 10);
            Template t = engine.compile("{x -= 3}");
            assertEquals("7", t.render(ctx));
            assertEquals(7, ctx.get("x"));
        }

        @Test
        void multiplyAssign() {
            Context ctx = new Context();
            ctx.put("x", 3);
            Template t = engine.compile("{x *= 2}");
            assertEquals("6", t.render(ctx));
            assertEquals(6, ctx.get("x"));
        }

        @Test
        void divideAssign() {
            Context ctx = new Context();
            ctx.put("x", 10);
            Template t = engine.compile("{x /= 2}");
            assertEquals("5", t.render(ctx));
            assertEquals(5, ctx.get("x"));
        }

        @Test
        void moduloAssign() {
            Context ctx = new Context();
            ctx.put("x", 10);
            Template t = engine.compile("{x %= 3}");
            assertEquals("1", t.render(ctx));
            assertEquals(1, ctx.get("x"));
        }

        @Test
        void plusAssignStringConcat() {
            Context ctx = new Context();
            ctx.put("msg", "hello");
            Template t = engine.compile("{msg += ' world'}");
            assertEquals("hello world", t.render(ctx));
            assertEquals("hello world", ctx.get("msg"));
        }

        @Test
        void compoundAssignmentPrecedence() {
            Context ctx = new Context();
            ctx.put("x", 1);
            Template t = engine.compile("{x += 2 + 3}");
            assertEquals("6", t.render(ctx));
            assertEquals(6, ctx.get("x"));
        }

        @Test
        void compoundAssignmentInvalidTarget() {
            assertThrows(TemplateCompileException.class, () -> engine.compile("{5 += 1}"));
        }
    }

    // ==================== C 风格 for 循环 ====================

    @Nested
    class ClassicForLoopTests {

        @Test
        void basicLoop() {
            Template t = engine.compile("{% for let i = 0; i < 5; i++ }{i}{/% for}");
            assertEquals("01234", t.render(new Context()));
        }

        @Test
        void loopWithStep() {
            Template t = engine.compile("{% for let i = 0; i < 10; i += 2 }{i}{/% for}");
            assertEquals("02468", t.render(new Context()));
        }

        @Test
        void nestedLoop() {
            Template t = engine.compile(
                "{% for let i = 0; i < 3; i++ }{% for let j = 0; j < 3; j++ }({i},{j}){/% for}{/% for}");
            assertEquals(
                "(0,0)(0,1)(0,2)(1,0)(1,1)(1,2)(2,0)(2,1)(2,2)",
                t.render(new Context()));
        }

        @Test
        void loopScope_variableNotVisibleAfter() {
            Template t = engine.compile("{% for let i = 0; i < 1; i++ }inner{/% for}");
            String result = t.render(new Context());
            assertTrue(result.contains("inner"));
        }

        @Test
        void infiniteLoopWithBreak() {
            Template t = engine.compile(
                "{% for let i = 0; ; i++ }{i}{% if i >= 3 }{break}{/% if}{/% for}");
            assertEquals("0123", t.render(new Context()));
        }

        @Test
        void decrementLoop() {
            Template t = engine.compile("{% for let i = 5; i > 0; i-- }{i}{/% for}");
            assertEquals("54321", t.render(new Context()));
        }

        @Test
        void loopWithMultiplyStep() {
            Template t = engine.compile("{% for let i = 1; i <= 16; i *= 2 }{i}{/% for}");
            assertEquals("124816", t.render(new Context()));
        }
    }

    // ==================== 回归测试 ====================

    @Nested
    class RegressionTests {

        @Test
        void existingForInLoop_stillWorks() {
            Context ctx = new Context();
            ctx.put("items", Arrays.asList(1, 2, 3));
            Template t = engine.compile("{% for item in items }{item}{/% for}");
            assertEquals("123", t.render(ctx));
        }

        @Test
        void existingForInWithIndex_stillWorks() {
            Context ctx = new Context();
            ctx.put("items", Arrays.asList("a", "b"));
            Template t = engine.compile("{% for idx, item in items }{idx}:{item}{/% for}");
            assertEquals("0:a1:b", t.render(ctx));
        }

        @Test
        void existingLetStatement_stillWorks() {
            Template t = engine.compile("{% let x = 10 %}{x}");
            assertEquals("10", t.render(new Context()));
        }

        @Test
        void existingArithmetic_stillWorks() {
            Context ctx = new Context();
            ctx.put("a", 5);
            ctx.put("b", 3);
            Template t = engine.compile("{a + b}");
            assertEquals("8", t.render(ctx));
        }
    }
}
```

- [ ] **Step 2: Run the tests**

Run: `mvn test -Dtest=IncrementDecrementTest -q`
Expected: All tests pass.

- [ ] **Step 3: Fix any test failures**

Common issues to watch for:
- If `context.get("i")` returns `Long` instead of `Integer` after arithmetic, adjust assertions to use `assertEquals(6L, ctx.get("i"))` or use `assertEquals(6, ((Number) ctx.get("i")).intValue())`.
- If break/continue mechanism differs, check how ForStatement implements it and mirror the pattern.
- If `ctx.put` writes to a child context that is discarded after evaluation (in expression context), the variable may not be visible. Check if expression evaluation uses the original context or a copy.

- [ ] **Step 4: Run full test suite**

Run: `mvn test -q`
Expected: All tests pass (no regressions).

- [ ] **Step 5: Commit**

```bash
git add src/test/java/org/dromara/easytl/IncrementDecrementTest.java
git commit -m "test: add end-to-end tests for increment/decrement, compound assignment, C-style for loop"
```
