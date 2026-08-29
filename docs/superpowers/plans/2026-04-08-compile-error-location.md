# Compile Error Location Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add source location information to compile-time errors with Rust-style pointer display.

**Architecture:** Extend AST nodes with keywordPosition field, update Parser to calculate position, modify Validators to use keywordPosition for error reporting.

**Tech Stack:** Java 8, JUnit 5, no external dependencies

---

## Task 1: Add COMPILE_ERROR to ErrorMessageFormatter

**Files:**
- Modify: `src/main/java/org/dromara/easytl/exception/ErrorMessageFormatter.java`

- [ ] **Step 1: Add COMPILE_ERROR constant and formatCompileError method**

Add after line 22 (after `RUNTIME_ERROR` constant):

```java
    /** Error type constant for compile errors */
    public static final String COMPILE_ERROR = "Compile error";
```

Add after line 229 (after `formatRuntimeError` method):

```java
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

- [ ] **Step 2: Run tests to verify no regression**

Run: `mvn test -Dtest=ErrorMessageFormatterTest -q`
Expected: PASS

- [ ] **Step 3: Commit**

```bash
git add src/main/java/org/dromara/easytl/exception/ErrorMessageFormatter.java
git commit -m "feat: add COMPILE_ERROR constant and formatCompileError method"
```

---

## Task 2: Update TemplateCompileException constructor

**Files:**
- Modify: `src/main/java/org/dromara/easytl/exception/TemplateCompileException.java`

- [ ] **Step 1: Update constructor to use formatCompileError**

Replace the existing constructor (lines 48-56):

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

- [ ] **Step 2: Run tests to verify no regression**

Run: `mvn test -Dtest=TemplateCompileExceptionTest -q`
Expected: PASS

- [ ] **Step 3: Commit**

```bash
git add src/main/java/org/dromara/easytl/exception/TemplateCompileException.java
git commit -m "feat: update TemplateCompileException to use formatCompileError"
```

---

## Task 3: Add keywordPosition to ExportStatement

**Files:**
- Modify: `src/main/java/org/dromara/easytl/ast/ExportStatement.java`

- [ ] **Step 1: Add keywordPosition field and getter**

Add field after `letStatement` field (around line 18):

```java
    /** 关键词位置信息（从 export 到变量名结束，不含 {% 和 %}） */
    private final Position keywordPosition;
```

Add getter after `getLetStatement()` method (around line 60):

```java
    /**
     * 获取关键词位置信息
     *
     * @return 关键词位置信息（export x 或 export let x = value 的位置）
     */
    public Position getKeywordPosition() {
        return keywordPosition;
    }
```

- [ ] **Step 2: Update constructors**

Replace all constructors with:

```java
    /**
     * 构造导出语句节点
     *
     * @param variableName 要导出的变量名
     * @param letStatement 声明并导出时的 LetStatement（可为 null）
     */
    public ExportStatement(String variableName, LetStatement letStatement) {
        this(variableName, letStatement, null);
    }

    /**
     * 构造导出语句节点（带关键词位置信息）
     *
     * @param variableName 要导出的变量名
     * @param letStatement 声明并导出时的 LetStatement（可为 null）
     * @param keywordPosition 关键词位置信息（export x 或 export let x = value 的位置）
     */
    public ExportStatement(String variableName, LetStatement letStatement, Position keywordPosition) {
        this.variableName = variableName;
        this.letStatement = letStatement;
        this.keywordPosition = keywordPosition;
    }

    /**
     * 构造导出语句节点（从 Token 获取位置信息）
     *
     * @param variableName 要导出的变量名
     * @param letStatement 声明并导出时的 LetStatement（可为 null）
     * @param token Token 对象（用于获取位置信息）
     * @deprecated Use {@link #ExportStatement(String, LetStatement, Position)} instead
     */
    @Deprecated
    public ExportStatement(String variableName, LetStatement letStatement, Token token) {
        this(variableName, letStatement, positionFromToken(token));
    }
```

- [ ] **Step 3: Compile to verify**

Run: `mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add src/main/java/org/dromara/easytl/ast/ExportStatement.java
git commit -m "feat: add keywordPosition to ExportStatement"
```

---

## Task 4: Add keywordPosition to ImportTemplateStatement

**Files:**
- Modify: `src/main/java/org/dromara/easytl/ast/ImportTemplateStatement.java`

- [ ] **Step 1: Add keywordPosition field and getter**

Add field after `alias` field (around line 19):

```java
    /** 关键词位置信息（从 import 到 alias 结束，不含 {% 和 %}） */
    private final Position keywordPosition;
```

Add getter after `getAlias()` method (around line 59):

```java
    /**
     * 获取关键词位置信息
     *
     * @return 关键词位置信息（import 'path' as alias 的位置）
     */
    public Position getKeywordPosition() {
        return keywordPosition;
    }
```

- [ ] **Step 2: Update constructors**

Replace all constructors with:

```java
    /**
     * 构造模板导入语句节点
     *
     * @param templatePath 模板文件路径
     * @param alias 别名
     */
    public ImportTemplateStatement(String templatePath, String alias) {
        this(templatePath, alias, null);
    }

    /**
     * 构造模板导入语句节点（带关键词位置信息）
     *
     * @param templatePath 模板文件路径
     * @param alias 别名
     * @param keywordPosition 关键词位置信息
     */
    public ImportTemplateStatement(String templatePath, String alias, Position keywordPosition) {
        this.templatePath = templatePath;
        this.alias = alias;
        this.keywordPosition = keywordPosition;
    }

    /**
     * 构造模板导入语句节点（从 Token 获取位置信息）
     *
     * @param templatePath 模板文件路径
     * @param alias 别名
     * @param token Token 对象（用于获取位置信息）
     * @deprecated Use {@link #ImportTemplateStatement(String, String, Position)} instead
     */
    @Deprecated
    public ImportTemplateStatement(String templatePath, String alias, Token token) {
        this(templatePath, alias, positionFromToken(token));
    }
```

- [ ] **Step 3: Compile to verify**

Run: `mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add src/main/java/org/dromara/easytl/ast/ImportTemplateStatement.java
git commit -m "feat: add keywordPosition to ImportTemplateStatement"
```

---

## Task 5: Add keywordPosition to ImportStatement

**Files:**
- Modify: `src/main/java/org/dromara/easytl/ast/ImportStatement.java`

- [ ] **Step 1: Add keywordPosition field and getter**

Add field after `alias` field (around line 18):

```java
    /** 关键词位置信息（从 import 到类名/别名结束，不含 {% 和 %}） */
    private final Position keywordPosition;
```

Add getter after `getAlias()` method (around line 68):

```java
    /**
     * 获取关键词位置信息
     *
     * @return 关键词位置信息（import className 或 import className as alias 的位置）
     */
    public Position getKeywordPosition() {
        return keywordPosition;
    }
```

- [ ] **Step 2: Update constructors**

Replace all constructors with:

```java
    /**
     * 构造import语句节点
     *
     * @param className Java类全限定名
     */
    public ImportStatement(String className) {
        this(className, null);
    }

    /**
     * 构造import语句节点
     *
     * @param className Java类全限定名
     * @param alias 别名（可为null）
     */
    public ImportStatement(String className, String alias) {
        this(className, alias, null);
    }

    /**
     * 构造import语句节点（带关键词位置信息）
     *
     * @param className Java类全限定名
     * @param alias 别名（可为null）
     * @param keywordPosition 关键词位置信息
     */
    public ImportStatement(String className, String alias, Position keywordPosition) {
        this.className = className;
        this.alias = alias;
        this.keywordPosition = keywordPosition;
    }

    /**
     * 构造import语句节点（从 Token 获取位置信息）
     *
     * @param className Java类全限定名
     * @param alias 别名（可为null）
     * @param token Token 对象（用于获取位置信息）
     * @deprecated Use {@link #ImportStatement(String, String, Position)} instead
     */
    @Deprecated
    public ImportStatement(String className, String alias, Token token) {
        this(className, alias, positionFromToken(token));
    }
```

- [ ] **Step 3: Compile to verify**

Run: `mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add src/main/java/org/dromara/easytl/ast/ImportStatement.java
git commit -m "feat: add keywordPosition to ImportStatement"
```

---

## Task 6: Update Parser.parseExportStatement to calculate keywordPosition

**Files:**
- Modify: `src/main/java/org/dromara/easytl/parser/Parser.java`

- [ ] **Step 1: Update parseExportStatement method**

Replace the entire `parseExportStatement` method (lines 413-433):

```java
    private ASTNode parseExportStatement() {
        Token exportToken = current();
        expect(TokenType.EXPORT);

        if (check(TokenType.LET)) {
            // {% export let x = value %}
            Token letToken = current();
            advance(); // 消耗 LET
            Token nameToken = expect(TokenType.IDENTIFIER);
            expect(TokenType.ASSIGN);
            ASTNode value = parseExpression();
            Token stmtEndToken = current();
            expect(TokenType.STMT_END);

            // keywordPosition: 从 export 到 value 结束（不含 {% 和 %})
            Position keywordPos = new Position(
                exportToken.getLine(), exportToken.getStartColumn(),
                stmtEndToken.getLine(), stmtEndToken.getEndColumn()
            );

            LetStatement letStmt = new LetStatement(nameToken.getValue(), value, letToken);
            return new ExportStatement(nameToken.getValue(), letStmt, keywordPos);
        } else {
            // {% export x %}
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

- [ ] **Step 2: Add Position import if needed**

Check if `Position` is already imported. If not, add:
```java
import org.dromara.easytl.ast.Position;
```

- [ ] **Step 3: Compile to verify**

Run: `mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add src/main/java/org/dromara/easytl/parser/Parser.java
git commit -m "feat: update parseExportStatement to calculate keywordPosition"
```

---

## Task 7: Update Parser.parseImportStatement to calculate keywordPosition

**Files:**
- Modify: `src/main/java/org/dromara/easytl/parser/Parser.java`

- [ ] **Step 1: Update parseImportStatement method**

Replace the entire `parseImportStatement` method (lines 380-411):

```java
    private ASTNode parseImportStatement() {
        Token importToken = current();
        expect(TokenType.IMPORT);

        if (check(TokenType.STRING)) {
            // 模板文件导入：{% import 'path/to/template.etl' as alias %}
            Token pathToken = advance();
            String path = pathToken.getValue();
            expect(TokenType.AS);
            Token aliasToken = expect(TokenType.IDENTIFIER);
            expect(TokenType.STMT_END);

            // keywordPosition: 从 import 到 alias 结束
            Position keywordPos = new Position(
                importToken.getLine(), importToken.getStartColumn(),
                aliasToken.getLine(), aliasToken.getEndColumn()
            );

            return new ImportTemplateStatement(path, aliasToken.getValue(), keywordPos);
        } else {
            // Java 类导入：{% import java.util.List %} 或 {% import java.util.List as ArrayList %}
            Token firstIdToken = expect(TokenType.IDENTIFIER);
            StringBuilder className = new StringBuilder();
            className.append(firstIdToken.getValue());

            Token lastToken = firstIdToken;

            while (check(TokenType.DOT)) {
                advance();
                Token idToken = expect(TokenType.IDENTIFIER);
                className.append(".");
                className.append(idToken.getValue());
                lastToken = idToken;
            }

            Token aliasToken = null;
            if (check(TokenType.AS)) {
                advance();
                aliasToken = expect(TokenType.IDENTIFIER);
                lastToken = aliasToken;
            }

            expect(TokenType.STMT_END);

            // keywordPosition: 从 import 到类名/别名结束
            Position keywordPos = new Position(
                importToken.getLine(), importToken.getStartColumn(),
                lastToken.getLine(), lastToken.getEndColumn()
            );

            return new ImportStatement(className.toString(), aliasToken != null ? aliasToken.getValue() : null, keywordPos);
        }
    }
```

- [ ] **Step 2: Compile to verify**

Run: `mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add src/main/java/org/dromara/easytl/parser/Parser.java
git commit -m "feat: update parseImportStatement to calculate keywordPosition"
```

---

## Task 8: Update ExportValidator to use keywordPosition

**Files:**
- Modify: `src/main/java/org/dromara/easytl/compiler/ExportValidator.java`

- [ ] **Step 1: Update error throwing to use keywordPosition**

Replace lines 42-48:

```java
                if (exportedNames.contains(varName)) {
                    String msg = "Variable '" + varName + "' is already exported";
                    if (export.getKeywordPosition() != null && !export.getKeywordPosition().isUnknown()) {
                        throw new TemplateCompileException(msg, export.getKeywordPosition(), new StringSource(sourceText));
                    }
                    throw new TemplateCompileException(msg);
                }
```

- [ ] **Step 2: Compile to verify**

Run: `mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add src/main/java/org/dromara/easytl/compiler/ExportValidator.java
git commit -m "feat: update ExportValidator to use keywordPosition"
```

---

## Task 9: Update ImportTemplateValidator to use keywordPosition

**Files:**
- Modify: `src/main/java/org/dromara/easytl/compiler/ImportTemplateValidator.java`

- [ ] **Step 1: Update all error throwing to use keywordPosition**

Replace lines 80-87:

```java
                // 检查别名冲突
                if (localDefinedVariables.contains(alias)) {
                    String msg = "Alias '" + alias + "' conflicts with an existing variable";
                    if (importStmt.getKeywordPosition() != null && !importStmt.getKeywordPosition().isUnknown()) {
                        throw new TemplateCompileException(msg, importStmt.getKeywordPosition(), new StringSource(sourceText));
                    }
                    throw new TemplateCompileException(msg);
                }

                if (importedAliases.contains(alias)) {
                    String msg = "Alias '" + alias + "' is already used for another import";
                    if (importStmt.getKeywordPosition() != null && !importStmt.getKeywordPosition().isUnknown()) {
                        throw new TemplateCompileException(msg, importStmt.getKeywordPosition(), new StringSource(sourceText));
                    }
                    throw new TemplateCompileException(msg);
                }
```

Replace lines 111-123:

```java
                } catch (TemplateCompileException e) {
                    String msg = "Cannot import template '" + templatePath + "': " + e.getMessage();
                    if (importStmt.getKeywordPosition() != null && !importStmt.getKeywordPosition().isUnknown()) {
                        throw new TemplateCompileException(msg, importStmt.getKeywordPosition(), new StringSource(sourceText));
                    }
                    throw new TemplateCompileException(msg);
                } catch (Exception e) {
                    String msg = "Failed to parse imported template '" + templatePath + "': " + e.getMessage();
                    if (importStmt.getKeywordPosition() != null && !importStmt.getKeywordPosition().isUnknown()) {
                        throw new TemplateCompileException(msg, importStmt.getKeywordPosition(), new StringSource(sourceText));
                    }
                    throw new TemplateCompileException(msg);
                }
```

- [ ] **Step 2: Compile to verify**

Run: `mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add src/main/java/org/dromara/easytl/compiler/ImportTemplateValidator.java
git commit -m "feat: update ImportTemplateValidator to use keywordPosition"
```

---

## Task 10: Update TemplateJavaImportValidator to use keywordPosition

**Files:**
- Modify: `src/main/java/org/dromara/easytl/compiler/TemplateJavaImportValidator.java`

- [ ] **Step 1: Update visitImportStatement to use keywordPosition**

Replace lines 34-40:

```java
        @Override
        public Void visitImportStatement(ImportStatement node) {
            String msg = "Template Java class import is disabled; use Context.importClass(...) instead.";
            if (node.getKeywordPosition() != null && !node.getKeywordPosition().isUnknown()) {
                throw new TemplateCompileException(msg, node.getKeywordPosition(), new StringSource(sourceText));
            }
            throw new TemplateCompileException(msg);
        }
```

- [ ] **Step 2: Compile to verify**

Run: `mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add src/main/java/org/dromara/easytl/compiler/TemplateJavaImportValidator.java
git commit -m "feat: update TemplateJavaImportValidator to use keywordPosition"
```

---

## Task 11: Write tests for ExportStatement compile error location

**Files:**
- Create: `src/test/java/org/dromara/easytl/compiler/ExportValidatorLocationTest.java`

- [ ] **Step 1: Create test file**

```java
package org.dromara.easytl.compiler;

import org.dromara.easytl.TemplateEngine;
import org.dromara.easytl.exception.TemplateCompileException;
import org.dromara.easytl.testutil.ErrorPointerAssert;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ExportValidator compile error location
 */
class ExportValidatorLocationTest {

    @Test
    void duplicateExport_simple() {
        String template = "{% export x %} {% export x %}";

        TemplateCompileException ex = assertThrows(
            TemplateCompileException.class,
            () -> TemplateEngine.compile(template)
        );

        ErrorPointerAssert.assertErrorPointer(ex,
            "{% export x %} {% export x %}",
            "                   ^^^^^^^^",
            "Variable 'x' is already exported"
        );
    }

    @Test
    void duplicateExport_withLet() {
        String template = "{% export let x = 1 %}\n{% export let x = 2 %}";

        TemplateCompileException ex = assertThrows(
            TemplateCompileException.class,
            () -> TemplateEngine.compile(template)
        );

        ErrorPointerAssert.assertErrorPointer(ex,
            "{% export let x = 2 %}",
            "   ^^^^^^^^^^^^^^^^",
            "Variable 'x' is already exported"
        );
    }

    @Test
    void duplicateExport_multiline() {
        String template = "Hello {% export name %}\nWorld {% export name %}";

        TemplateCompileException ex = assertThrows(
            TemplateCompileException.class,
            () -> TemplateEngine.compile(template)
        );

        ErrorPointerAssert.assertErrorPointer(ex,
            "World {% export name %}",
            "         ^^^^^^^^^^^",
            "Variable 'name' is already exported"
        );
    }
}
```

- [ ] **Step 2: Run tests to verify they pass**

Run: `mvn test -Dtest=ExportValidatorLocationTest -q`
Expected: All tests PASS

- [ ] **Step 3: Commit**

```bash
git add src/test/java/org/dromara/easytl/compiler/ExportValidatorLocationTest.java
git commit -m "test: add ExportValidator compile error location tests"
```

---

## Task 12: Write tests for ImportStatement compile error location

**Files:**
- Modify: `src/test/java/org/dromara/easytl/compiler/TemplateJavaImportValidatorTest.java`

- [ ] **Step 1: Add location test to existing test file**

Add after line 34:

```java
    @Test
    void disallowed_hasCorrectLocation() throws Exception {
        String src = "{% import java.util.List %}";
        TemplateNode root = parse(src);

        TemplateCompileException ex = assertThrows(
            TemplateCompileException.class,
            () -> TemplateJavaImportValidator.validate(root, false, src)
        );

        // Verify position information
        assertNotNull(ex.getPosition());
        assertFalse(ex.getPosition().isUnknown());
        assertEquals(1, ex.getStartLine());
        // keywordPosition should point to "import java.util.List" (columns 4-23)
        assertEquals(4, ex.getStartColumn());
        assertEquals(23, ex.getEndColumn());
        assertTrue(ex.getMessage().contains("Java class import is disabled"));
    }
```

- [ ] **Step 2: Run tests to verify they pass**

Run: `mvn test -Dtest=TemplateJavaImportValidatorTest -q`
Expected: All tests PASS

- [ ] **Step 3: Commit**

```bash
git add src/test/java/org/dromara/easytl/compiler/TemplateJavaImportValidatorTest.java
git commit -m "test: add TemplateJavaImportValidator error location test"
```

---

## Task 13: Run full test suite

- [ ] **Step 1: Run all tests**

Run: `mvn test -q`
Expected: All tests PASS

- [ ] **Step 2: Fix any failures if needed**

If any tests fail, investigate and fix.

---

## Task 14: Final commit and verification

- [ ] **Step 1: Verify all changes compile**

Run: `mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 2: Run all tests one more time**

Run: `mvn test -q`
Expected: All tests PASS

- [ ] **Step 3: Push changes**

```bash
git push origin develop
```