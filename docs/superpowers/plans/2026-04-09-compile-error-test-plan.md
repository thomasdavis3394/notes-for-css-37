# CompileErrorTest Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Create a comprehensive test class that tests all compile-time errors through `engine.compile(...)` API with error location verification.

**Architecture:** Single test class with nested test classes organized by error category (SyntaxErrors, ValidatorErrors, EngineErrors, NestedErrors). Uses `ErrorPointerAssert.assertErrorPointer` for location verification.

**Tech Stack:** Java 8, JUnit 5, existing EasyTL codebase

---

## File Structure

| File | Action | Purpose |
|------|--------|---------|
| `src/test/java/org/dromara/easytl/CompileErrorTest.java` | Create | Main test class with all nested test classes |

**Existing files used:**
- `src/test/java/org/dromara/easytl/testutil/ErrorPointerAssert.java` — Helper for verifying error pointers
- `src/test/resources/templates/empty.etl` — Empty template for import tests
- `src/test/resources/templates/broken.etl` — Broken template for import failure tests

---

### Task 1: Create Test Class Skeleton

**Files:**
- Create: `src/test/java/org/dromara/easytl/CompileErrorTest.java`

- [ ] **Step 1: Create test class with imports, setup, and helper method**

```java
package org.dromara.easytl;

import org.dromara.easytl.exception.TemplateCompileException;
import org.dromara.easytl.exception.TemplateException;
import org.dromara.easytl.loader.ClasspathTemplateLoader;
import org.dromara.easytl.testutil.ErrorPointerAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests all compile-time errors via engine.compile(...) API.
 * Verifies error location information (line, column, pointer) accuracy.
 */
@DisplayName("Compile Error Tests")
class CompileErrorTest {

    private TemplateEngine engine;

    @BeforeEach
    void setUp() {
        engine = EasyTL.defaultEngine();
    }

    /**
     * Helper method: Verify engine.compile throws exception with correct error pointer.
     */
    private void assertCompileError(
        String template,
        String expectedSourceLine,
        String expectedPointerLine,
        String expectedErrorMessage
    ) {
        TemplateException ex = assertThrows(
            TemplateException.class,
            () -> engine.compile(template),
            "Should throw TemplateException for: " + template
        );
        ErrorPointerAssert.assertErrorPointer(ex,
            expectedSourceLine, expectedPointerLine, expectedErrorMessage);
    }

    // Nested test classes will be added in subsequent tasks
}
```

- [ ] **Step 2: Run test to verify class compiles**

Run: `mvn test-compile -Dtest=CompileErrorTest`
Expected: BUILD SUCCESS (no tests run yet, class compiles)

- [ ] **Step 3: Commit**

```bash
git add src/test/java/org/dromara/easytl/CompileErrorTest.java
git commit -m "test: add CompileErrorTest skeleton

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

### Task 2: Add SyntaxErrors - Expression Wrapper Tests (3 tests)

**Files:**
- Modify: `src/test/java/org/dromara/easytl/CompileErrorTest.java`

- [ ] **Step 1: Add SyntaxErrors nested class with expression wrapper tests**

Add inside `CompileErrorTest` class, after the helper method:

```java
    @Nested
    @DisplayName("Syntax Errors")
    class SyntaxErrors {

        // ========== Expression Wrapper Errors ==========

        @Test
        @DisplayName("Unclosed single brace expression")
        void testUnclosedSingleBrace() {
            assertCompileError("{x",
                "{x",
                "  ^",
                "Unclosed expression"
            );
        }

        @Test
        @DisplayName("Unclosed double brace expression")
        void testUnclosedDoubleBrace() {
            assertCompileError("{{x",
                "{{x",
                "   ^",
                "Unclosed expression"
            );
        }

        @Test
        @DisplayName("Unclosed dollar brace expression")
        void testUnclosedDollarBrace() {
            assertCompileError("${x",
                "${x",
                "   ^",
                "Unclosed expression"
            );
        }
    }
```

- [ ] **Step 2: Run tests to verify they pass**

Run: `mvn test -Dtest=CompileErrorTest#testUnclosedSingleBrace,CompileErrorTest#testUnclosedDoubleBrace,CompileErrorTest#testUnclosedDollarBrace`
Expected: Tests PASS

- [ ] **Step 3: Commit**

```bash
git add src/test/java/org/dromara/easytl/CompileErrorTest.java
git commit -m "test: add expression wrapper error tests

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

### Task 3: Add SyntaxErrors - If Statement Tests (7 tests)

**Files:**
- Modify: `src/test/java/org/dromara/easytl/CompileErrorTest.java`

- [ ] **Step 1: Add if statement error tests inside SyntaxErrors class**

Add after expression wrapper tests in `SyntaxErrors` class:

```java
        // ========== If Statement Errors ==========

        @Test
        @DisplayName("If statement missing condition")
        void testIfMissingCondition() {
            assertCompileError("{% if } %}",
                "{% if } %}",
                "      ^",
                "Unexpected token"
            );
        }

        @Test
        @DisplayName("If statement missing right brace")
        void testIfMissingRightBrace() {
            assertCompileError("{% if true %}",
                "{% if true %}",
                "             ^",
                "Expected {/%"
            );
        }

        @Test
        @DisplayName("If statement missing end tag")
        void testIfMissingEndTag() {
            assertCompileError("{% if true %}content",
                "{% if true %}content",
                "                    ^",
                "Expected {/%"
            );
        }

        @Test
        @DisplayName("If statement wrong end tag")
        void testIfWrongEndTag() {
            assertCompileError("{% if true %}content{% endfor %}",
                "{% if true %}content{% endfor %}",
                "                                ^",
                "Expected {/%"
            );
        }

        @Test
        @DisplayName("Else if missing condition")
        void testElseIfMissingCondition() {
            assertCompileError("{% if false %}a{% else if %}b{% endif %}",
                "{% if false %}a{% else if %}b{% endif %}",
                "                          ^^",
                "Unexpected token"
            );
        }

        @Test
        @DisplayName("Else if missing right brace")
        void testElseIfMissingRightBrace() {
            assertCompileError("{% if false %}a{% else if true %}b{% endif %}",
                "{% if false %}a{% else if true %}b{% endif %}",
                "                                             ^",
                "Expected {/%"
            );
        }

        @Test
        @DisplayName("Else missing right brace")
        void testElseMissingRightBrace() {
            assertCompileError("{% if false %}a{% else %}b{% endif %}",
                "{% if false %}a{% else %}b{% endif %}",
                "                                     ^",
                "Expected {/%"
            );
        }
```

- [ ] **Step 2: Run tests to verify they pass**

Run: `mvn test -Dtest=CompileErrorTest\$SyntaxErrors`
Expected: All 10 tests PASS (3 wrapper + 7 if)

- [ ] **Step 3: Commit**

```bash
git add src/test/java/org/dromara/easytl/CompileErrorTest.java
git commit -m "test: add if statement error tests

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

### Task 4: Add SyntaxErrors - For Statement Tests (8 tests)

**Files:**
- Modify: `src/test/java/org/dromara/easytl/CompileErrorTest.java`

- [ ] **Step 1: Add for statement error tests inside SyntaxErrors class**

Add after if statement tests:

```java
        // ========== For Statement Errors ==========

        @Test
        @DisplayName("For statement missing identifier")
        void testForMissingIdentifier() {
            assertCompileError("{% for in list %}",
                "{% for in list %}",
                "       ^^",
                "Expected identifier"
            );
        }

        @Test
        @DisplayName("For statement missing 'in' keyword")
        void testForMissingIn() {
            assertCompileError("{% for item list %}",
                "{% for item list %}",
                "            ^^^^",
                "Expected in"
            );
        }

        @Test
        @DisplayName("For statement missing collection")
        void testForMissingCollection() {
            assertCompileError("{% for item in %}",
                "{% for item in %}",
                "               ^^",
                "Unexpected token"
            );
        }

        @Test
        @DisplayName("For statement missing right brace")
        void testForMissingRightBrace() {
            assertCompileError("{% for item in list %}",
                "{% for item in list %}",
                "                      ^",
                "Expected {/%"
            );
        }

        @Test
        @DisplayName("For statement missing end tag")
        void testForMissingEndTag() {
            assertCompileError("{% for item in list %}content",
                "{% for item in list %}content",
                "                             ^",
                "Expected {/%"
            );
        }

        @Test
        @DisplayName("For statement wrong end tag")
        void testForWrongEndTag() {
            assertCompileError("{% for item in list %}content{% endif %}",
                "{% for item in list %}content{% endif %}",
                "                                        ^",
                "Expected {/%"
            );
        }

        @Test
        @DisplayName("For statement invalid comma")
        void testForInvalidComma() {
            assertCompileError("{% for item, in list %}",
                "{% for item, in list %}",
                "             ^^",
                "Expected identifier"
            );
        }

        @Test
        @DisplayName("For statement missing second identifier after comma")
        void testForMissingSecondIdentifier() {
            assertCompileError("{% for index, in list %}",
                "{% for index, in list %}",
                "              ^^",
                "Expected identifier"
            );
        }
```

- [ ] **Step 2: Run tests to verify they pass**

Run: `mvn test -Dtest=CompileErrorTest\$SyntaxErrors`
Expected: All 18 tests PASS

- [ ] **Step 3: Commit**

```bash
git add src/test/java/org/dromara/easytl/CompileErrorTest.java
git commit -m "test: add for statement error tests

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

### Task 5: Add SyntaxErrors - Switch Statement Tests (6 tests)

**Files:**
- Modify: `src/test/java/org/dromara/easytl/CompileErrorTest.java`

- [ ] **Step 1: Add switch statement error tests inside SyntaxErrors class**

Add after for statement tests:

```java
        // ========== Switch Statement Errors ==========

        @Test
        @DisplayName("Switch statement missing expression")
        void testSwitchMissingExpression() {
            assertCompileError("{% switch %}",
                "{% switch %}",
                "          ^^",
                "Unexpected token"
            );
        }

        @Test
        @DisplayName("Switch statement missing right brace")
        void testSwitchMissingRightBrace() {
            assertCompileError("{% switch x %}",
                "{% switch x %}",
                "              ^",
                "Expected {/%"
            );
        }

        @Test
        @DisplayName("Switch statement missing end tag")
        void testSwitchMissingEndTag() {
            assertCompileError("{% switch x %}content",
                "{% switch x %}content",
                "                     ^",
                "Expected {/%"
            );
        }

        @Test
        @DisplayName("Switch case missing expression")
        void testSwitchCaseMissingExpression() {
            assertCompileError("{% switch x %}{% case %}{% endswitch %}",
                "{% switch x %}{% case %}{% endswitch %}",
                "                      ^^",
                "Unexpected token"
            );
        }

        @Test
        @DisplayName("Switch case missing right brace")
        void testSwitchCaseMissingRightBrace() {
            assertCompileError("{% switch x %}{% case 1 %}content{% endswitch %}",
                "{% switch x %}{% case 1 %}content{% endswitch %}",
                "                        ^^",
                "Expected }"
            );
        }

        @Test
        @DisplayName("Switch default missing right brace")
        void testSwitchDefaultMissingRightBrace() {
            assertCompileError("{% switch x %}{% default %}content{% endswitch %}",
                "{% switch x %}{% default %}content{% endswitch %}",
                "                         ^^",
                "Expected }"
            );
        }
```

- [ ] **Step 2: Run tests to verify they pass**

Run: `mvn test -Dtest=CompileErrorTest\$SyntaxErrors`
Expected: All 24 tests PASS

- [ ] **Step 3: Commit**

```bash
git add src/test/java/org/dromara/easytl/CompileErrorTest.java
git commit -m "test: add switch statement error tests

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

### Task 6: Add SyntaxErrors - Let Statement Tests (5 tests)

**Files:**
- Modify: `src/test/java/org/dromara/easytl/CompileErrorTest.java`

- [ ] **Step 1: Add let statement error tests inside SyntaxErrors class**

Add after switch statement tests:

```java
        // ========== Let Statement Errors ==========

        @Test
        @DisplayName("Let statement missing identifier")
        void testLetMissingIdentifier() {
            assertCompileError("{% let = 5 %}",
                "{% let = 5 %}",
                "       ^",
                "Expected identifier"
            );
        }

        @Test
        @DisplayName("Let statement missing assignment operator")
        void testLetMissingAssign() {
            assertCompileError("{% let x 5 %}",
                "{% let x 5 %}",
                "         ^",
                "Expected ="
            );
        }

        @Test
        @DisplayName("Let statement missing value")
        void testLetMissingValue() {
            assertCompileError("{% let x = %}",
                "{% let x = %}",
                "           ^^",
                "Unexpected token"
            );
        }

        @Test
        @DisplayName("Let statement if expression without else")
        void testLetIfExprWithoutElse() {
            assertCompileError("{% let x = if (true) { 1 } %}",
                "{% let x = if (true) { 1 } %}",
                "           ^^^^^^^^^^^^^^^",
                "If expression in let assignment must include else branch"
            );
        }

        @Test
        @DisplayName("Let statement if expression with else if but no else")
        void testLetIfExprWithElseIfNoElse() {
            assertCompileError("{% let x = if (false) { 1 } else if (true) { 2 } %}",
                "{% let x = if (false) { 1 } else if (true) { 2 } %}",
                "           ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^",
                "If expression in let assignment must include else branch"
            );
        }
```

- [ ] **Step 2: Run tests to verify they pass**

Run: `mvn test -Dtest=CompileErrorTest\$SyntaxErrors`
Expected: All 29 tests PASS

- [ ] **Step 3: Commit**

```bash
git add src/test/java/org/dromara/easytl/CompileErrorTest.java
git commit -m "test: add let statement error tests

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

### Task 7: Add SyntaxErrors - Extends/Import Statement Tests (3 tests)

**Files:**
- Modify: `src/test/java/org/dromara/easytl/CompileErrorTest.java`

- [ ] **Step 1: Add extends/import statement error tests inside SyntaxErrors class**

Add after let statement tests:

```java
        // ========== Extends/Import Statement Errors ==========

        @Test
        @DisplayName("Extends statement missing string")
        void testExtendsMissingString() {
            assertCompileError("{% extends %}",
                "{% extends %}",
                "           ^^",
                "Expected string"
            );
        }

        @Test
        @DisplayName("Import statement missing identifier")
        void testImportMissingIdentifier() {
            assertCompileError("{% import %}",
                "{% import %}",
                "          ^^",
                "Expected identifier"
            );
        }

        @Test
        @DisplayName("Import statement incomplete dot notation")
        void testImportIncompleteDot() {
            assertCompileError("{% import java. %}",
                "{% import java. %}",
                "                ^^",
                "Expected identifier"
            );
        }
```

- [ ] **Step 2: Run tests to verify they pass**

Run: `mvn test -Dtest=CompileErrorTest\$SyntaxErrors`
Expected: All 32 tests PASS

- [ ] **Step 3: Commit**

```bash
git add src/test/java/org/dromara/easytl/CompileErrorTest.java
git commit -m "test: add extends/import statement error tests

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

### Task 8: Add SyntaxErrors - Expression Tests (15 tests)

**Files:**
- Modify: `src/test/java/org/dromara/easytl/CompileErrorTest.java`

- [ ] **Step 1: Add expression error tests inside SyntaxErrors class**

Add after extends/import tests:

```java
        // ========== Expression Errors ==========

        @Test
        @DisplayName("Binary expression missing right operand")
        void testBinaryMissingRightOperand() {
            assertCompileError("{x + }",
                "{x + }",
                "     ^",
                "Unexpected token"
            );
        }

        @Test
        @DisplayName("Ternary expression missing colon")
        void testTernaryMissingColon() {
            assertCompileError("{x ? y }",
                "{x ? y }",
                "       ^",
                "Expected :"
            );
        }

        @Test
        @DisplayName("Ternary expression missing true expression")
        void testTernaryMissingTrueExpr() {
            assertCompileError("{x ? : y}",
                "{x ? : y}",
                "     ^",
                "Unexpected token"
            );
        }

        @Test
        @DisplayName("Ternary expression missing false expression")
        void testTernaryMissingFalseExpr() {
            assertCompileError("{x ? y : }",
                "{x ? y : }",
                "         ^",
                "Unexpected token"
            );
        }

        @Test
        @DisplayName("Elvis expression missing right operand")
        void testElvisMissingRightOperand() {
            assertCompileError("{x ?? }",
                "{x ?? }",
                "      ^",
                "Unexpected token"
            );
        }

        @Test
        @DisplayName("Method call missing right parenthesis")
        void testMethodCallMissingRightParen() {
            assertCompileError("{func(}",
                "{func(}",
                "      ^",
                "Unexpected token"
            );
        }

        @Test
        @DisplayName("Method call missing comma between arguments")
        void testMethodCallMissingComma() {
            assertCompileError("{func(x y)}",
                "{func(x y)}",
                "        ^",
                "Expected )"
            );
        }

        @Test
        @DisplayName("Method call with invalid target")
        void testMethodCallInvalidTarget() {
            assertCompileError("{5()}",
                "{5()}",
                "  ^",
                "Invalid method call target"
            );
        }

        @Test
        @DisplayName("Member access missing identifier")
        void testMemberAccessMissingIdentifier() {
            assertCompileError("{obj.}",
                "{obj.}",
                "     ^",
                "Expected identifier"
            );
        }

        @Test
        @DisplayName("Safe navigation missing identifier")
        void testSafeNavMissingIdentifier() {
            assertCompileError("{obj?.}",
                "{obj?.}",
                "      ^",
                "Expected identifier"
            );
        }

        @Test
        @DisplayName("Index access missing right bracket")
        void testIndexAccessMissingRightBracket() {
            assertCompileError("{list[}",
                "{list[}",
                "      ^",
                "Unexpected token"
            );
        }

        @Test
        @DisplayName("Index access missing index expression")
        void testIndexAccessMissingIndex() {
            assertCompileError("{list[]}",
                "{list[]}",
                "      ^",
                "Unexpected token"
            );
        }

        @Test
        @DisplayName("Parenthesized expression missing right paren")
        void testParenMissingRightParen() {
            assertCompileError("{(x}",
                "{(x}",
                "   ^",
                "Expected )"
            );
        }

        @Test
        @DisplayName("Empty parenthesized expression")
        void testParenEmpty() {
            assertCompileError("{()}",
                "{()}",
                "  ^",
                "Unexpected token"
            );
        }

        @Test
        @DisplayName("Unexpected token in expression context")
        void testUnexpectedTokenInExpression() {
            assertCompileError("{% %}",
                "{% %}",
                "   ^^",
                "Unexpected token"
            );
        }
```

- [ ] **Step 2: Run tests to verify they pass**

Run: `mvn test -Dtest=CompileErrorTest\$SyntaxErrors`
Expected: All 47 tests PASS

- [ ] **Step 3: Commit**

```bash
git add src/test/java/org/dromara/easytl/CompileErrorTest.java
git commit -m "test: add expression error tests

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

### Task 9: Add SyntaxErrors - Literal Tests (6 tests)

**Files:**
- Modify: `src/test/java/org/dromara/easytl/CompileErrorTest.java`

- [ ] **Step 1: Add literal error tests inside SyntaxErrors class**

Add after expression tests:

```java
        // ========== Literal Errors ==========

        @Test
        @DisplayName("List literal missing right bracket")
        void testListMissingRightBracket() {
            assertCompileError("{[1, 2}",
                "{[1, 2}",
                "      ^",
                "Expected ]"
            );
        }

        @Test
        @DisplayName("List literal missing comma")
        void testListMissingComma() {
            assertCompileError("{[1 2]}",
                "{[1 2]}",
                "    ^",
                "Expected ]"
            );
        }

        @Test
        @DisplayName("Hash literal missing right brace")
        void testHashMissingRightBrace() {
            assertCompileError("{{key: value}",
                "{{key: value}",
                "             ^",
                "Unclosed expression"
            );
        }

        @Test
        @DisplayName("Hash literal missing colon")
        void testHashMissingColon() {
            assertCompileError("{{key value}}",
                "{{key value}}",
                "      ^^^^^",
                "Expected }}"
            );
        }

        @Test
        @DisplayName("Hash literal invalid key type")
        void testHashInvalidKey() {
            assertCompileError("{{true: value}}",
                "{{true: value}}",
                "  ^^^^",
                "Invalid hash key"
            );
        }

        @Test
        @DisplayName("Hash literal missing value")
        void testHashMissingValue() {
            assertCompileError("{{key: }}",
                "{{key: }}",
                "       ^",
                "Unexpected token"
            );
        }
```

- [ ] **Step 2: Run tests to verify they pass**

Run: `mvn test -Dtest=CompileErrorTest\$SyntaxErrors`
Expected: All 53 tests PASS

- [ ] **Step 3: Commit**

```bash
git add src/test/java/org/dromara/easytl/CompileErrorTest.java
git commit -m "test: add literal error tests

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

### Task 10: Add SyntaxErrors - Structured Expression Tests (9 tests)

**Files:**
- Modify: `src/test/java/org/dromara/easytl/CompileErrorTest.java`

- [ ] **Step 1: Add structured expression error tests inside SyntaxErrors class**

Add after literal tests:

```java
        // ========== Structured Expression Errors ==========

        @Test
        @DisplayName("If expression missing left paren")
        void testIfExprMissingLeftParen() {
            assertCompileError("{if true { 1 } else { 2 }}",
                "{if true { 1 } else { 2 }}",
                "    ^^^^",
                "Expected ("
            );
        }

        @Test
        @DisplayName("If expression missing right paren")
        void testIfExprMissingRightParen() {
            assertCompileError("{if (true { 1 } else { 2 }}",
                "{if (true { 1 } else { 2 }}",
                "          ^",
                "Expected )"
            );
        }

        @Test
        @DisplayName("If expression missing left brace")
        void testIfExprMissingLeftBrace() {
            assertCompileError("{if (true) 1 else { 2 }}",
                "{if (true) 1 else { 2 }}",
                "           ^",
                "Expected {"
            );
        }

        @Test
        @DisplayName("If expression missing right brace")
        void testIfExprMissingRightBrace() {
            assertCompileError("{if (true) { 1 else { 2 }}",
                "{if (true) { 1 else { 2 }}",
                "               ^^^^",
                "Unexpected token"
            );
        }

        @Test
        @DisplayName("For expression missing left paren")
        void testForExprMissingLeftParen() {
            assertCompileError("{for item in list { x } }",
                "{for item in list { x } }",
                "     ^^^^",
                "Expected ("
            );
        }

        @Test
        @DisplayName("For expression missing right paren")
        void testForExprMissingRightParen() {
            assertCompileError("{for (item in list { x } }",
                "{for (item in list { x } }",
                "                       ^",
                "Expected :"
            );
        }

        @Test
        @DisplayName("For expression missing left brace")
        void testForExprMissingLeftBrace() {
            assertCompileError("{for (item in list) x }",
                "{for (item in list) x }",
                "                    ^",
                "Expected {"
            );
        }

        @Test
        @DisplayName("For expression missing right brace")
        void testForExprMissingRightBrace() {
            assertCompileError("{for (item in list) { x }",
                "{for (item in list) { x }",
                "                         ^",
                "Unclosed expression"
            );
        }

        @Test
        @DisplayName("Range expression missing right operand")
        void testRangeMissingRightOperand() {
            assertCompileError("{1..}",
                "{1..}",
                "    ^",
                "Unexpected token"
            );
        }
```

- [ ] **Step 2: Run tests to verify they pass**

Run: `mvn test -Dtest=CompileErrorTest\$SyntaxErrors`
Expected: All 62 tests PASS

- [ ] **Step 3: Commit**

```bash
git add src/test/java/org/dromara/easytl/CompileErrorTest.java
git commit -m "test: add structured expression error tests

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

### Task 11: Add ValidatorErrors Nested Class (7 tests)

**Files:**
- Modify: `src/test/java/org/dromara/easytl/CompileErrorTest.java`

- [ ] **Step 1: Add ValidatorErrors nested class after SyntaxErrors class**

Add after the closing brace of `SyntaxErrors` class:

```java
    @Nested
    @DisplayName("Validator Errors")
    class ValidatorErrors {

        // ========== ExportValidator Errors ==========

        @Test
        @DisplayName("Duplicate export")
        void testDuplicateExport() {
            assertCompileError("{% export x %} {% export x %}",
                "{% export x %} {% export x %}",
                "                  ^^^^^^^^",
                "Variable 'x' is already exported"
            );
        }

        @Test
        @DisplayName("Duplicate export with let")
        void testDuplicateExportWithLet() {
            assertCompileError("{% export let x = 1 %}\n{% export let x = 2 %}",
                "{% export let x = 2 %}",
                "   ^^^^^^^^^^^^^^^^",
                "Variable 'x' is already exported"
            );
        }

        // ========== ImportTemplateValidator Errors ==========

        @Test
        @DisplayName("Import template alias conflicts with variable")
        void testImportTemplateAliasConflicts() {
            TemplateEngine engineWithLoader = new TemplateEngine(
                new TemplateEngine.TemplateEngineConfig(),
                new ClasspathTemplateLoader()
            );

            TemplateException ex = assertThrows(
                TemplateException.class,
                () -> engineWithLoader.compile("{% let foo = 1 %}\n{% import 'templates/empty.etl' as foo %}")
            );
            ErrorPointerAssert.assertErrorPointer(ex,
                "{% import 'templates/empty.etl' as foo %}",
                "   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^",
                "Alias 'foo' conflicts"
            );
        }

        @Test
        @DisplayName("Import template alias already used")
        void testImportTemplateAliasAlreadyUsed() {
            TemplateEngine engineWithLoader = new TemplateEngine(
                new TemplateEngine.TemplateEngineConfig(),
                new ClasspathTemplateLoader()
            );

            TemplateException ex = assertThrows(
                TemplateException.class,
                () -> engineWithLoader.compile("{% import 'templates/empty.etl' as lib %}\n{% import 'templates/empty.etl' as lib %}")
            );
            ErrorPointerAssert.assertErrorPointer(ex,
                "{% import 'templates/empty.etl' as lib %}",
                "   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^",
                "Alias 'lib' conflicts"
            );
        }

        @Test
        @DisplayName("Import template not found")
        void testImportTemplateNotFound() {
            TemplateEngine engineWithLoader = new TemplateEngine(
                new TemplateEngine.TemplateEngineConfig(),
                new ClasspathTemplateLoader()
            );

            TemplateException ex = assertThrows(
                TemplateException.class,
                () -> engineWithLoader.compile("{% import 'nonexistent.etl' as lib %}")
            );
            ErrorPointerAssert.assertErrorPointer(ex,
                "{% import 'nonexistent.etl' as lib %}",
                "   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^",
                "Cannot import template"
            );
        }

        @Test
        @DisplayName("Import template parse failure")
        void testImportTemplateParseFailure() {
            TemplateEngine engineWithLoader = new TemplateEngine(
                new TemplateEngine.TemplateEngineConfig(),
                new ClasspathTemplateLoader()
            );

            TemplateException ex = assertThrows(
                TemplateException.class,
                () -> engineWithLoader.compile("{% import 'templates/broken.etl' as lib %}")
            );
            ErrorPointerAssert.assertErrorPointer(ex,
                "{% import 'templates/broken.etl' as lib %}",
                "   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^",
                "Failed to parse imported template"
            );
        }

        // ========== TemplateJavaImportValidator Errors ==========

        @Test
        @DisplayName("Java import disabled")
        void testJavaImportDisabled() {
            TemplateEngine engineNoJavaImport = EasyTL.engine()
                .templateJavaClassImportEnabled(false)
                .build();

            TemplateException ex = assertThrows(
                TemplateException.class,
                () -> engineNoJavaImport.compile("{% import java.util.List %}")
            );
            ErrorPointerAssert.assertErrorPointer(ex,
                "{% import java.util.List %}",
                "   ^^^^^^^^^^^^^^^^^^^^^",
                "Template Java class import is disabled"
            );
        }
    }
```

- [ ] **Step 2: Run tests to verify they pass**

Run: `mvn test -Dtest=CompileErrorTest\$ValidatorErrors`
Expected: All 7 tests PASS

- [ ] **Step 3: Commit**

```bash
git add src/test/java/org/dromara/easytl/CompileErrorTest.java
git commit -m "test: add validator error tests

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

### Task 12: Add EngineErrors Nested Class (5 tests)

**Files:**
- Modify: `src/test/java/org/dromara/easytl/CompileErrorTest.java`

- [ ] **Step 1: Add EngineErrors nested class after ValidatorErrors class**

Add after the closing brace of `ValidatorErrors` class:

```java
    @Nested
    @DisplayName("Engine Errors")
    class EngineErrors {

        @Test
        @DisplayName("Null template string")
        void testNullTemplateString() {
            TemplateCompileException ex = assertThrows(
                TemplateCompileException.class,
                () -> engine.compile((String) null)
            );
            assertTrue(ex.getMessage().contains("Template string cannot be null"));
        }

        @Test
        @DisplayName("Null input stream")
        void testNullInputStream() {
            TemplateCompileException ex = assertThrows(
                TemplateCompileException.class,
                () -> engine.compile((java.io.InputStream) null)
            );
            assertTrue(ex.getMessage().contains("InputStream cannot be null"));
        }

        @Test
        @DisplayName("Null file")
        void testNullFile() {
            TemplateCompileException ex = assertThrows(
                TemplateCompileException.class,
                () -> engine.compile((File) null)
            );
            assertTrue(ex.getMessage().contains("File cannot be null"));
        }

        @Test
        @DisplayName("File not found")
        void testFileNotFound() {
            TemplateCompileException ex = assertThrows(
                TemplateCompileException.class,
                () -> engine.compile(new File("nonexistent-template-file.etl"))
            );
            assertTrue(ex.getMessage().contains("Template file does not exist"));
        }

        @Test
        @DisplayName("Path is directory")
        void testPathIsDirectory() {
            TemplateCompileException ex = assertThrows(
                TemplateCompileException.class,
                () -> engine.compile(new File("src/test/resources"))
            );
            assertTrue(ex.getMessage().contains("Path is not a file"));
        }
    }
```

- [ ] **Step 2: Run tests to verify they pass**

Run: `mvn test -Dtest=CompileErrorTest\$EngineErrors`
Expected: All 5 tests PASS

- [ ] **Step 3: Commit**

```bash
git add src/test/java/org/dromara/easytl/CompileErrorTest.java
git commit -m "test: add engine error tests

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

### Task 13: Add NestedErrors Nested Class (5 tests)

**Files:**
- Modify: `src/test/java/org/dromara/easytl/CompileErrorTest.java`

- [ ] **Step 1: Add NestedErrors nested class after EngineErrors class**

Add after the closing brace of `EngineErrors` class:

```java
    @Nested
    @DisplayName("Nested Statement Errors")
    class NestedErrors {

        @Test
        @DisplayName("Nested if missing end tag")
        void testNestedIfMissingEnd() {
            assertCompileError("{% if true %}{% if false %}content{% endif %}",
                "{% if true %}{% if false %}content{% endif %}",
                "                                             ^",
                "Expected {/%"
            );
        }

        @Test
        @DisplayName("Nested for missing end tag")
        void testNestedForMissingEnd() {
            assertCompileError("{% for x in list %}{% for y in list %}content{% endfor %}",
                "{% for x in list %}{% for y in list %}content{% endfor %}",
                "                                                         ^",
                "Expected {/%"
            );
        }

        @Test
        @DisplayName("Mixed nested statements missing end tag")
        void testMixedNestedMissingEnd() {
            assertCompileError("{% if true %}{% for x in list %}content{% endfor %}",
                "{% if true %}{% for x in list %}content{% endfor %}",
                "                                                   ^",
                "Expected {/%"
            );
        }

        @Test
        @DisplayName("Multiline error - error on line 3")
        void testMultilineErrorLine3() {
            assertCompileError("line1\nline2\n{x + }",
                "{x + }",
                "     ^",
                "Unexpected token"
            );
        }

        @Test
        @DisplayName("Multiline error - statement missing end")
        void testMultilineErrorLocation() {
            assertCompileError("Hello\n{% if true %}\ncontent",
                "content",
                "       ^",
                "Expected {/%"
            );
        }
    }
```

- [ ] **Step 2: Run tests to verify they pass**

Run: `mvn test -Dtest=CompileErrorTest\$NestedErrors`
Expected: All 5 tests PASS

- [ ] **Step 3: Commit**

```bash
git add src/test/java/org/dromara/easytl/CompileErrorTest.java
git commit -m "test: add nested statement error tests

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

### Task 14: Final Verification

- [ ] **Step 1: Run all CompileErrorTest tests**

Run: `mvn test -Dtest=CompileErrorTest`
Expected: All tests PASS (79 total: 62 SyntaxErrors + 7 ValidatorErrors + 5 EngineErrors + 5 NestedErrors)

- [ ] **Step 2: Run all tests to ensure no regressions**

Run: `mvn test`
Expected: All tests PASS

- [ ] **Step 3: Verify test count matches spec**

The final test class should have:
- SyntaxErrors: 62 tests
- ValidatorErrors: 7 tests
- EngineErrors: 5 tests
- NestedErrors: 5 tests
- **Total: 79 tests**

---

## Self-Review Checklist

- [x] **Spec coverage:** All test cases from spec are covered
- [x] **Placeholder scan:** No TBD, TODO, or placeholder text
- [x] **Type consistency:** All method names and parameters are consistent across tasks
- [x] **File paths:** All paths are exact and correct
- [x] **Code completeness:** All code blocks contain full implementation