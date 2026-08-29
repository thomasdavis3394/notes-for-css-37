# 修复错误位置指针 - 成员访问和索引访问

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 修改 Parser.parsePostfix() 方法，让 MemberAccess 和 IndexAccess 的位置只覆盖访问操作本身，使错误指针精确指向失败的成员访问操作。

**Architecture:** 在 Parser.parsePostfix() 中，修改 Position 构造逻辑。对于 MemberAccess，位置从 DOT token 开始到成员名结束；对于 IndexAccess，位置从 `[` 到 `]`。

**Tech Stack:** Java 8, Maven, JUnit 5

---

## 文件结构

| 文件 | 职责 |
|------|------|
| `src/main/java/org/dromara/easytl/parser/Parser.java:736-782` | 修改 parsePostfix() 方法中的 Position 计算逻辑 |
| `src/test/java/org/dromara/easytl/runtime/DebugErrorPosition.java` | 添加测试验证错误位置 |

---

### Task 1: 编写失败测试 - 验证 MemberAccess 错误位置

**Files:**
- Modify: `src/test/java/org/dromara/easytl/runtime/DebugErrorPosition.java`

- [ ] **Step 1: 更新测试验证错误位置指向成员名**

```java
package org.dromara.easytl.runtime;

import org.dromara.easytl.Context;
import org.dromara.easytl.Eval;
import org.dromara.easytl.exception.TemplateRuntimeException;

public class DebugErrorPosition {
    public static void main(String[] args) {
        testMemberAccessPosition();
        testIndexAccessPosition();
        testChainedMemberAccessPosition();
    }

    private static void testMemberAccessPosition() {
        System.out.println("=== Test: MemberAccess Position ===");
        Context ctx = new Context();
        // scheme is undefined, so scheme.name should fail at .name

        EtlURL url = Eval.evalAsURL("{scheme.name}", ctx);

        try {
            url.getScheme();
            System.err.println("FAIL: Expected TemplateRuntimeException");
        } catch (TemplateRuntimeException ex) {
            System.out.println("Error message:\n" + ex.getMessage());
            System.out.println("Position: line " + ex.getStartLine() + ", col " + ex.getStartColumn() + " - " + ex.getEndColumn());

            // 验证位置：应该指向 .name 而不是 scheme.name
            // 源码: {scheme.name}
            // 位置:       ^^^^  (从第9列开始，指向.name)
            // 列号从1开始: {=1, s=2, c=3, h=4, e=5, m=6, e=7, .=8, n=9, a=10, m=11, e=12, }=13
            // .name 应该是 8-12 (包含点号)
            if (ex.getStartColumn() == 8 && ex.getEndColumn() == 12) {
                System.out.println("PASS: Position points to .name\n");
            } else {
                System.err.println("FAIL: Expected position 8-12, got " + ex.getStartColumn() + "-" + ex.getEndColumn() + "\n");
            }
        }
    }

    private static void testIndexAccessPosition() {
        System.out.println("=== Test: IndexAccess Position ===");
        Context ctx = new Context();
        // arr is undefined, so arr[0] should fail at [0]

        EtlURL url = Eval.evalAsURL("{arr[0]}", ctx);

        try {
            url.getScheme();
            System.err.println("FAIL: Expected TemplateRuntimeException");
        } catch (TemplateRuntimeException ex) {
            System.out.println("Error message:\n" + ex.getMessage());
            System.out.println("Position: line " + ex.getStartLine() + ", col " + ex.getStartColumn() + " - " + ex.getEndColumn());

            // 验证位置：应该指向 [0]
            // 源码: {arr[0]}
            // 位置:     ^^^   (从第5列开始，指向[0])
            // 列号: {=1, a=2, r=3, r=4, [=5, 0=6, ]=7, }=8
            // [0] 应该是 5-7
            if (ex.getStartColumn() == 5 && ex.getEndColumn() == 7) {
                System.out.println("PASS: Position points to [0]\n");
            } else {
                System.err.println("FAIL: Expected position 5-7, got " + ex.getStartColumn() + "-" + ex.getEndColumn() + "\n");
            }
        }
    }

    private static void testChainedMemberAccessPosition() {
        System.out.println("=== Test: Chained MemberAccess Position ===");
        Context ctx = new Context();
        ctx.put("user", new Object()); // user exists but has no 'profile' property

        EtlURL url = Eval.evalAsURL("{user.profile.city}", ctx);

        try {
            url.getScheme();
            System.err.println("FAIL: Expected TemplateRuntimeException");
        } catch (TemplateRuntimeException ex) {
            System.out.println("Error message:\n" + ex.getMessage());
            System.out.println("Position: line " + ex.getStartLine() + ", col " + ex.getStartColumn() + " - " + ex.getEndColumn());

            // 验证位置：应该指向 .profile (第一个失败的访问)
            // 源码: {user.profile.city}
            // 列号: {=1, u=2, s=3, e=4, r=5, .=6, p=7, r=8, o=9, f=10, i=11, l=12, e=13, .=14, c=15...
            // .profile 应该是 6-13
            if (ex.getStartColumn() == 6 && ex.getEndColumn() == 13) {
                System.out.println("PASS: Position points to .profile\n");
            } else {
                System.err.println("FAIL: Expected position 6-13, got " + ex.getStartColumn() + "-" + ex.getEndColumn() + "\n");
            }
        }
    }
}
```

- [ ] **Step 2: 运行测试验证当前行为（应失败）**

Run: `cd C:/Projects/easy-tl && mvn test-compile -q && mvn exec:java -Dexec.mainClass="org.dromara.easytl.runtime.DebugErrorPosition" -Dexec.classpathScope=test -q 2>&1`

Expected: 测试 FAIL，显示当前位置指向整个表达式而不是访问操作

---

### Task 2: 修复 MemberAccess 位置计算

**Files:**
- Modify: `src/main/java/org/dromara/easytl/parser/Parser.java:740-751`

- [ ] **Step 1: 修改 DOT 分支的 Position 计算**

找到 `Parser.java` 中 `parsePostfix()` 方法的以下代码段（约 740-751 行）：

```java
            if (check(TokenType.DOT)) {
                advance();
                Token name = expect(TokenType.IDENTIFIER);
                Position pos = new Position(expr.getStartLine(), expr.getStartColumn(),
                                           name.getLine(), name.getStartColumn() + name.getValue().length() - 1);
                expr = new MemberAccess(expr, name.getValue(), false, pos);
            } else if (check(TokenType.SAFE_NAV)) {
                advance();
                Token name = expect(TokenType.IDENTIFIER);
                Position pos = new Position(expr.getStartLine(), expr.getStartColumn(),
                                           name.getLine(), name.getStartColumn() + name.getValue().length() - 1);
                expr = new MemberAccess(expr, name.getValue(), true, pos);
```

修改为：

```java
            if (check(TokenType.DOT)) {
                Token dot = advance();  // 保存 DOT token
                Token name = expect(TokenType.IDENTIFIER);
                // 位置只覆盖 .memberName 部分
                Position pos = new Position(dot.getLine(), dot.getStartColumn(),
                                           name.getLine(), name.getStartColumn() + name.getValue().length() - 1);
                expr = new MemberAccess(expr, name.getValue(), false, pos);
            } else if (check(TokenType.SAFE_NAV)) {
                Token safeNav = advance();  // 保存 SAFE_NAV token
                Token name = expect(TokenType.IDENTIFIER);
                // 位置只覆盖 ?.memberName 部分
                Position pos = new Position(safeNav.getLine(), safeNav.getStartColumn(),
                                           name.getLine(), name.getStartColumn() + name.getValue().length() - 1);
                expr = new MemberAccess(expr, name.getValue(), true, pos);
```

- [ ] **Step 2: 编译验证语法正确**

Run: `cd C:/Projects/easy-tl && mvn compile -q 2>&1`

Expected: BUILD SUCCESS

---

### Task 3: 修复 IndexAccess 位置计算

**Files:**
- Modify: `src/main/java/org/dromara/easytl/parser/Parser.java:752-758`

- [ ] **Step 1: 修改 LEFT_BRACKET 分支的 Position 计算**

找到以下代码段（约 752-758 行）：

```java
            } else if (check(TokenType.LEFT_BRACKET)) {
                Token leftBracket = advance();
                ASTNode index = parseExpression();
                Token rightBracket = expect(TokenType.RIGHT_BRACKET);
                Position pos = new Position(expr.getStartLine(), expr.getStartColumn(),
                                           rightBracket.getLine(), rightBracket.getStartColumn());
                expr = new IndexAccess(expr, index, pos);
```

修改为：

```java
            } else if (check(TokenType.LEFT_BRACKET)) {
                Token leftBracket = advance();
                ASTNode index = parseExpression();
                Token rightBracket = expect(TokenType.RIGHT_BRACKET);
                // 位置只覆盖 [index] 部分
                Position pos = new Position(leftBracket.getLine(), leftBracket.getStartColumn(),
                                           rightBracket.getLine(), rightBracket.getEndColumn());
                expr = new IndexAccess(expr, index, pos);
```

- [ ] **Step 2: 编译验证语法正确**

Run: `cd C:/Projects/easy-tl && mvn compile -q 2>&1`

Expected: BUILD SUCCESS

---

### Task 4: 运行测试验证修复

**Files:**
- Test: `src/test/java/org/dromara/easytl/runtime/DebugErrorPosition.java`

- [ ] **Step 1: 运行测试验证修复效果**

Run: `cd C:/Projects/easy-tl && mvn test-compile -q && mvn exec:java -Dexec.mainClass="org.dromara.easytl.runtime.DebugErrorPosition" -Dexec.classpathScope=test -q 2>&1`

Expected: 所有测试 PASS，输出显示位置正确指向访问操作

---

### Task 5: 运行完整测试套件

**Files:**
- Test: 所有测试

- [ ] **Step 1: 运行所有测试确保没有回归**

Run: `cd C:/Projects/easy-tl && mvn test -q 2>&1`

Expected: BUILD SUCCESS，所有测试通过

---

### Task 6: 清理临时测试文件并提交

**Files:**
- Delete: `src/test/java/org/dromara/easytl/runtime/DebugErrorPosition.java`

- [ ] **Step 1: 删除临时测试文件**

Run: `rm src/test/java/org/dromara/easytl/runtime/DebugErrorPosition.java`

- [ ] **Step 2: 提交更改**

Run: `cd C:/Projects/easy-tl && git add src/main/java/org/dromara/easytl/parser/Parser.java && git commit -m "$(cat <<'EOF'
fix: error pointer points to access operation instead of full expression

When a runtime error occurs (e.g., accessing property on null), the error
pointer now highlights only the failing access operation (.member or [index])
instead of the entire expression chain.

Example:
  Before: {scheme.name} - pointer at ^^^^^^^^^^ (scheme.name)
  After:  {scheme.name} - pointer at     ^^^^ (.name)

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>
EOF
)"`

Expected: Commit successful

- [ ] **Step 3: 验证提交**

Run: `cd C:/Projects/easy-tl && git log -1 --oneline`

Expected: 显示新提交