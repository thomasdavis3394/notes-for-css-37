# Compile Error Location Tests Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Consolidate compile-time error location tests into `CompilerValidatorLocationTest` and refactor `SyntaxErrorTest` to use `assertErrorPointer` for all tests.

**Architecture:** Rename and extend the existing `ExportValidatorLocationTest` to cover all validators (Export, ImportTemplate, TemplateJavaImport). Refactor `SyntaxErrorTest` by replacing `assertSyntaxError` with `assertSyntaxErrorWithPointer` for each test case.

**Tech Stack:** Java 8, JUnit 5, Maven

---

## File Structure

| Action | File |
|--------|------|
| Create | `src/test/resources/templates/empty.etl` |
| Create | `src/test/resources/templates/broken.etl` |
| Rename | `ExportValidatorLocationTest.java` → `CompilerValidatorLocationTest.java` |
| Modify | `CompilerValidatorLocationTest.java` (add ImportTemplateValidator and TemplateJavaImportValidator tests) |
| Delete | `TemplateJavaImportValidatorTest.java` |
| Modify | `SyntaxErrorTest.java` (refactor all tests to use assertSyntaxErrorWithPointer) |

---

### Task 1: Create Test Template Files

**Files:**
- Create: `src/test/resources/templates/empty.etl`
- Create: `src/test/resources/templates/broken.etl`

- [ ] **Step 1: Create empty.etl template file**

```bash
mkdir -p src/test/resources/templates
```

Create file `src/test/resources/templates/empty.etl`:
```
{# Empty template for testing #}
```

- [ ] **Step 2: Create broken.etl template file**

Create file `src/test/resources/templates/broken.etl`:
```
{x + }
```

This template has a syntax error (missing right operand).

- [ ] **Step 3: Commit**

```bash
git add src/test/resources/templates/
git commit -m "test: add template files for ImportTemplateValidator tests"
```

---

### Task 2: Rename ExportValidatorLocationTest to CompilerValidatorLocationTest

**Files:**
- Rename: `src/test/java/org/dromara/easytl/compiler/ExportValidatorLocationTest.java` → `src/test/java/org/dromara/easytl/compiler/CompilerValidatorLocationTest.java`

- [ ] **Step 1: Rename the file**

```bash
git mv src/test/java/org/dromara/easytl/compiler/ExportValidatorLocationTest.java src/test/java/org/dromara/easytl/compiler/CompilerValidatorLocationTest.java
```

- [ ] **Step 2: Update class name and Javadoc**

Edit `src/test/java/org/dromara/easytl/compiler/CompilerValidatorLocationTest.java`:

```java
package org.dromara.easytl.compiler;

import org.dromara.easytl.EasyTL;
import org.dromara.easytl.TemplateEngine;
import org.dromara.easytl.exception.TemplateCompileException;
import org.dromara.easytl.testutil.ErrorPointerAssert;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for compile-time validator error location accuracy
 */
class CompilerValidatorLocationTest {

    private final TemplateEngine engine = EasyTL.defaultEngine();

    @Test
    void duplicateExport_simple() {
        String template = "{% export x %} {% export x %}";

        TemplateCompileException ex = assertThrows(
            TemplateCompileException.class,
            () -> engine.compile(template)
        );

        ErrorPointerAssert.assertErrorPointer(ex,
            "{% export x %} {% export x %}",
            "                  ^^^^^^^^",
            "Variable 'x' is already exported"
        );
    }

    @Test
    void duplicateExport_withLet() {
        String template = "{% export let x = 1 %}\n{% export let x = 2 %}";

        TemplateCompileException ex = assertThrows(
            TemplateCompileException.class,
            () -> engine.compile(template)
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
            () -> engine.compile(template)
        );

        ErrorPointerAssert.assertErrorPointer(ex,
            "World {% export name %}",
            "         ^^^^^^^^^^^",
            "Variable 'name' is already exported"
        );
    }
}
```

- [ ] **Step 3: Compile and run tests to verify**

```bash
mvn test -Dtest=CompilerValidatorLocationTest -q
```

Expected: All 3 tests pass.

- [ ] **Step 4: Commit**

```bash
git add src/test/java/org/dromara/easytl/compiler/CompilerValidatorLocationTest.java
git commit -m "refactor: rename ExportValidatorLocationTest to CompilerValidatorLocationTest"
```

---

### Task 3: Add TemplateJavaImportValidator Location Test

**Files:**
- Modify: `src/test/java/org/dromara/easytl/compiler/CompilerValidatorLocationTest.java`

- [ ] **Step 1: Add TemplateJavaImportValidator test method**

Add to `CompilerValidatorLocationTest.java` after the ExportValidator tests:

```java
    // ==================== TemplateJavaImportValidator Tests ====================

    @Test
    void javaImport_disabled() {
        String template = "{% import java.util.List %}";

        // Create engine with Java import disabled
        TemplateEngine engineNoJavaImport = EasyTL.builder()
            .templateJavaClassImportEnabled(false)
            .build();

        TemplateCompileException ex = assertThrows(
            TemplateCompileException.class,
            () -> engineNoJavaImport.compile(template)
        );

        // keywordPosition: "import java.util.List" (columns 4-22, length 19)
        ErrorPointerAssert.assertErrorPointer(ex,
            "{% import java.util.List %}",
            "   ^^^^^^^^^^^^^^^^^^^^",
            "Template Java class import is disabled"
        );
    }
```

- [ ] **Step 2: Compile and run test to verify**

```bash
mvn test -Dtest=CompilerValidatorLocationTest#javaImport_disabled -q
```

Expected: Test passes.

- [ ] **Step 3: Commit**

```bash
git add src/test/java/org/dromara/easytl/compiler/CompilerValidatorLocationTest.java
git commit -m "test: add TemplateJavaImportValidator location test"
```

---

### Task 4: Add ImportTemplateValidator Location Tests

**Files:**
- Modify: `src/test/java/org/dromara/easytl/compiler/CompilerValidatorLocationTest.java`

- [ ] **Step 1: Add imports for ImportTemplateValidator tests**

Add imports at the top of `CompilerValidatorLocationTest.java`:

```java
import org.dromara.easytl.loader.ClasspathTemplateLoader;
import org.dromara.easytl.loader.TemplateLoader;
```

- [ ] **Step 2: Add ImportTemplateValidator test methods**

Add to `CompilerValidatorLocationTest.java` after the TemplateJavaImportValidator tests:

```java
    // ==================== ImportTemplateValidator Tests ====================

    @Test
    void importTemplate_aliasConflictsWithVariable() {
        String template = "{% let foo = 1 %}\n{% import 'templates/empty.etl' as foo %}";

        TemplateLoader loader = new ClasspathTemplateLoader();
        TemplateEngine engineWithLoader = EasyTL.builder()
            .templateLoader(loader)
            .build();

        TemplateCompileException ex = assertThrows(
            TemplateCompileException.class,
            () -> engineWithLoader.compile(template)
        );

        // keywordPosition points to the alias "foo" at the end
        ErrorPointerAssert.assertErrorPointer(ex,
            "{% import 'templates/empty.etl' as foo %}",
            "                                    ^^^",
            "Alias 'foo' conflicts with an existing variable"
        );
    }

    @Test
    void importTemplate_aliasAlreadyUsed() {
        String template = "{% import 'templates/empty.etl' as lib %}\n{% import 'templates/empty.etl' as lib %}";

        TemplateLoader loader = new ClasspathTemplateLoader();
        TemplateEngine engineWithLoader = EasyTL.builder()
            .templateLoader(loader)
            .build();

        TemplateCompileException ex = assertThrows(
            TemplateCompileException.class,
            () -> engineWithLoader.compile(template)
        );

        // keywordPosition points to the alias "lib" at the end
        ErrorPointerAssert.assertErrorPointer(ex,
            "{% import 'templates/empty.etl' as lib %}",
            "                                    ^^^",
            "Alias 'lib' is already used for another import"
        );
    }

    @Test
    void importTemplate_templateNotFound() {
        String template = "{% import 'nonexistent.etl' as lib %}";

        TemplateLoader loader = new ClasspathTemplateLoader();
        TemplateEngine engineWithLoader = EasyTL.builder()
            .templateLoader(loader)
            .build();

        TemplateCompileException ex = assertThrows(
            TemplateCompileException.class,
            () -> engineWithLoader.compile(template)
        );

        // keywordPosition points to "import 'nonexistent.etl' as lib"
        ErrorPointerAssert.assertErrorPointer(ex,
            "{% import 'nonexistent.etl' as lib %}",
            "   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^",
            "Cannot import template 'nonexistent.etl'"
        );
    }

    @Test
    void importTemplate_parseFailure() {
        String template = "{% import 'templates/broken.etl' as lib %}";

        TemplateLoader loader = new ClasspathTemplateLoader();
        TemplateEngine engineWithLoader = EasyTL.builder()
            .templateLoader(loader)
            .build();

        TemplateCompileException ex = assertThrows(
            TemplateCompileException.class,
            () -> engineWithLoader.compile(template)
        );

        // keywordPosition points to "import 'templates/broken.etl' as lib"
        ErrorPointerAssert.assertErrorPointer(ex,
            "{% import 'templates/broken.etl' as lib %}",
            "   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^",
            "Cannot import template 'templates/broken.etl'"
        );
    }
```

- [ ] **Step 3: Compile and run tests to verify**

```bash
mvn test -Dtest=CompilerValidatorLocationTest -q
```

Expected: All tests pass. If pointer positions are incorrect, adjust them based on actual output.

- [ ] **Step 4: Commit**

```bash
git add src/test/java/org/dromara/easytl/compiler/CompilerValidatorLocationTest.java
git commit -m "test: add ImportTemplateValidator location tests"
```

---

### Task 5: Delete TemplateJavaImportValidatorTest

**Files:**
- Delete: `src/test/java/org/dromara/easytl/compiler/TemplateJavaImportValidatorTest.java`

- [ ] **Step 1: Delete the file**

```bash
git rm src/test/java/org/dromara/easytl/compiler/TemplateJavaImportValidatorTest.java
```

- [ ] **Step 2: Run all compiler tests to verify**

```bash
mvn test -Dtest="*Validator*Test" -q
```

Expected: All tests pass.

- [ ] **Step 3: Commit**

```bash
git commit -m "refactor: remove TemplateJavaImportValidatorTest (merged into CompilerValidatorLocationTest)"
```

---

### Task 6: Refactor SyntaxErrorTest - Part 1: Expression Wrapper Errors

**Files:**
- Modify: `src/test/java/org/dromara/easytl/parser/SyntaxErrorTest.java`

- [ ] **Step 1: Refactor testSingleBraceExprMissingEnd**

Replace:
```java
    @Test
    public void testSingleBraceExprMissingEnd() throws IOException {
        assertSyntaxError("{x", "Unclosed expression");
    }
```

With:
```java
    @Test
    public void testSingleBraceExprMissingEnd() throws IOException {
        assertSyntaxErrorWithPointer("{x",
            "{x",
            " ^^",
            "Unclosed expression"
        );
    }
```

- [ ] **Step 2: Refactor testDoubleBraceExprMissingEnd**

Replace:
```java
    @Test
    public void testDoubleBraceExprMissingEnd() throws IOException {
        assertSyntaxError("{{x", "Unclosed expression");
    }
```

With:
```java
    @Test
    public void testDoubleBraceExprMissingEnd() throws IOException {
        assertSyntaxErrorWithPointer("{{x",
            "{{x",
            " ^^^",
            "Unclosed expression"
        );
    }
```

- [ ] **Step 3: Refactor testDollarBraceExprMissingEnd**

Replace:
```java
    @Test
    public void testDollarBraceExprMissingEnd() throws IOException {
        assertSyntaxError("${x", "Unclosed expression");
    }
```

With:
```java
    @Test
    public void testDollarBraceExprMissingEnd() throws IOException {
        assertSyntaxErrorWithPointer("${x",
            "${x",
            " ^^^",
            "Unclosed expression"
        );
    }
```

- [ ] **Step 4: Run tests to verify**

```bash
mvn test -Dtest=SyntaxErrorTest#testSingleBraceExprMissingEnd,SyntaxErrorTest#testDoubleBraceExprMissingEnd,SyntaxErrorTest#testDollarBraceExprMissingEnd -q
```

Expected: All 3 tests pass. If pointer positions are incorrect, adjust based on actual output.

- [ ] **Step 5: Commit**

```bash
git add src/test/java/org/dromara/easytl/parser/SyntaxErrorTest.java
git commit -m "test: add location verification for expression wrapper errors"
```

---

### Task 7: Refactor SyntaxErrorTest - Part 2: If Statement Errors

**Files:**
- Modify: `src/test/java/org/dromara/easytl/parser/SyntaxErrorTest.java`

- [ ] **Step 1: Refactor testIfStatementMissingRightBrace**

Replace:
```java
    @Test
    public void testIfStatementMissingRightBrace() throws IOException {
        assertSyntaxError("{% if true %}", "Expected");
    }
```

With:
```java
    @Test
    public void testIfStatementMissingRightBrace() throws IOException {
        assertSyntaxErrorWithPointer("{% if true %}",
            "{% if true %}",
            "            ^",
            "Expected"
        );
    }
```

- [ ] **Step 2: Refactor testIfStatementMissingEndTag**

Replace:
```java
    @Test
    public void testIfStatementMissingEndTag() throws IOException {
        assertSyntaxError("{% if true %}content", "Expected");
    }
```

With:
```java
    @Test
    public void testIfStatementMissingEndTag() throws IOException {
        assertSyntaxErrorWithPointer("{% if true %}content",
            "{% if true %}content",
            "                   ^",
            "Expected"
        );
    }
```

- [ ] **Step 3: Refactor testIfStatementWrongEndTag**

Replace:
```java
    @Test
    public void testIfStatementWrongEndTag() throws IOException {
        assertSyntaxError("{% if true %}content{% endfor %}", "Expected");
    }
```

With:
```java
    @Test
    public void testIfStatementWrongEndTag() throws IOException {
        assertSyntaxErrorWithPointer("{% if true %}content{% endfor %}",
            "{% if true %}content{% endfor %}",
            "                     ^^^^^^",
            "Expected"
        );
    }
```

- [ ] **Step 4: Refactor testElseIfMissingCondition**

Replace:
```java
    @Test
    public void testElseIfMissingCondition() throws IOException {
        assertSyntaxError("{% if false %}a{% else if %}b{% endif %}", "Unexpected token");
    }
```

With:
```java
    @Test
    public void testElseIfMissingCondition() throws IOException {
        assertSyntaxErrorWithPointer("{% if false %}a{% else if %}b{% endif %}",
            "{% else if %}",
            "           ^",
            "Unexpected token"
        );
    }
```

- [ ] **Step 5: Refactor testElseIfMissingRightBrace**

Replace:
```java
    @Test
    public void testElseIfMissingRightBrace() throws IOException {
        assertSyntaxError("{% if false %}a{% else if true %}b{% endif %}", "Expected");
    }
```

With:
```java
    @Test
    public void testElseIfMissingRightBrace() throws IOException {
        assertSyntaxErrorWithPointer("{% if false %}a{% else if true %}b{% endif %}",
            "{% else if true %}",
            "                 ^",
            "Expected"
        );
    }
```

- [ ] **Step 6: Refactor testElseMissingRightBrace**

Replace:
```java
    @Test
    public void testElseMissingRightBrace() throws IOException {
        assertSyntaxError("{% if false %}a{% else %}b{% endif %}", "Expected");
    }
```

With:
```java
    @Test
    public void testElseMissingRightBrace() throws IOException {
        assertSyntaxErrorWithPointer("{% if false %}a{% else %}b{% endif %}",
            "{% else %}",
            "        ^",
            "Expected"
        );
    }
```

- [ ] **Step 7: Run tests to verify**

```bash
mvn test -Dtest=SyntaxErrorTest -q
```

Expected: All tests pass. Adjust pointer positions if needed based on actual output.

- [ ] **Step 8: Commit**

```bash
git add src/test/java/org/dromara/easytl/parser/SyntaxErrorTest.java
git commit -m "test: add location verification for if statement errors"
```

---

### Task 8: Refactor SyntaxErrorTest - Part 3: For Statement Errors

**Files:**
- Modify: `src/test/java/org/dromara/easytl/parser/SyntaxErrorTest.java`

- [ ] **Step 1: Refactor testForStatementMissingIn**

Replace:
```java
    @Test
    public void testForStatementMissingIn() throws IOException {
        assertSyntaxError("{% for item list %}", "Expected");
    }
```

With:
```java
    @Test
    public void testForStatementMissingIn() throws IOException {
        assertSyntaxErrorWithPointer("{% for item list %}",
            "{% for item list %}",
            "           ^^^^",
            "Expected"
        );
    }
```

- [ ] **Step 2: Refactor testForStatementMissingCollection**

Replace:
```java
    @Test
    public void testForStatementMissingCollection() throws IOException {
        assertSyntaxError("{% for item in %}", "Unexpected token");
    }
```

With:
```java
    @Test
    public void testForStatementMissingCollection() throws IOException {
        assertSyntaxErrorWithPointer("{% for item in %}",
            "{% for item in %}",
            "              ^",
            "Unexpected token"
        );
    }
```

- [ ] **Step 3: Refactor testForStatementMissingRightBrace**

Replace:
```java
    @Test
    public void testForStatementMissingRightBrace() throws IOException {
        assertSyntaxError("{% for item in list %}", "Expected");
    }
```

With:
```java
    @Test
    public void testForStatementMissingRightBrace() throws IOException {
        assertSyntaxErrorWithPointer("{% for item in list %}",
            "{% for item in list %}",
            "                    ^",
            "Expected"
        );
    }
```

- [ ] **Step 4: Refactor testForStatementMissingEndTag**

Replace:
```java
    @Test
    public void testForStatementMissingEndTag() throws IOException {
        assertSyntaxError("{% for item in list %}content", "Expected");
    }
```

With:
```java
    @Test
    public void testForStatementMissingEndTag() throws IOException {
        assertSyntaxErrorWithPointer("{% for item in list %}content",
            "{% for item in list %}content",
            "                             ^",
            "Expected"
        );
    }
```

- [ ] **Step 5: Refactor testForStatementWrongEndTag**

Replace:
```java
    @Test
    public void testForStatementWrongEndTag() throws IOException {
        assertSyntaxError("{% for item in list %}content{% endif %}", "Expected");
    }
```

With:
```java
    @Test
    public void testForStatementWrongEndTag() throws IOException {
        assertSyntaxErrorWithPointer("{% for item in list %}content{% endif %}",
            "{% for item in list %}content{% endif %}",
            "                          ^^^^^^",
            "Expected"
        );
    }
```

- [ ] **Step 6: Refactor testForStatementInvalidComma**

Replace:
```java
    @Test
    public void testForStatementInvalidComma() throws IOException {
        assertSyntaxError("{% for item, in list %}", "Expected");
    }
```

With:
```java
    @Test
    public void testForStatementInvalidComma() throws IOException {
        assertSyntaxErrorWithPointer("{% for item, in list %}",
            "{% for item, in list %}",
            "           ^",
            "Expected"
        );
    }
```

- [ ] **Step 7: Refactor testForStatementMissingSecondIdentifier**

Replace:
```java
    @Test
    public void testForStatementMissingSecondIdentifier() throws IOException {
        assertSyntaxError("{% for index, in list %}", "Expected");
    }
```

With:
```java
    @Test
    public void testForStatementMissingSecondIdentifier() throws IOException {
        assertSyntaxErrorWithPointer("{% for index, in list %}",
            "{% for index, in list %}",
            "             ^^",
            "Expected"
        );
    }
```

- [ ] **Step 8: Run tests to verify**

```bash
mvn test -Dtest=SyntaxErrorTest -q
```

Expected: All tests pass. Adjust pointer positions if needed.

- [ ] **Step 9: Commit**

```bash
git add src/test/java/org/dromara/easytl/parser/SyntaxErrorTest.java
git commit -m "test: add location verification for for statement errors"
```

---

### Task 9: Refactor SyntaxErrorTest - Part 4: Switch Statement Errors

**Files:**
- Modify: `src/test/java/org/dromara/easytl/parser/SyntaxErrorTest.java`

- [ ] **Step 1: Refactor testSwitchStatementMissingRightBrace**

Replace:
```java
    @Test
    public void testSwitchStatementMissingRightBrace() throws IOException {
        assertSyntaxError("{% switch x %}", "Expected");
    }
```

With:
```java
    @Test
    public void testSwitchStatementMissingRightBrace() throws IOException {
        assertSyntaxErrorWithPointer("{% switch x %}",
            "{% switch x %}",
            "             ^",
            "Expected"
        );
    }
```

- [ ] **Step 2: Refactor testSwitchStatementMissingEndTag**

Replace:
```java
    @Test
    public void testSwitchStatementMissingEndTag() throws IOException {
        assertSyntaxError("{% switch x %}content", "Expected");
    }
```

With:
```java
    @Test
    public void testSwitchStatementMissingEndTag() throws IOException {
        assertSyntaxErrorWithPointer("{% switch x %}content",
            "{% switch x %}content",
            "                     ^",
            "Expected"
        );
    }
```

- [ ] **Step 3: Refactor testSwitchCaseMissingExpression**

Replace:
```java
    @Test
    public void testSwitchCaseMissingExpression() throws IOException {
        assertSyntaxError("{% switch x %}{% case %}{% endswitch %}", "Unexpected token");
    }
```

With:
```java
    @Test
    public void testSwitchCaseMissingExpression() throws IOException {
        assertSyntaxErrorWithPointer("{% switch x %}{% case %}{% endswitch %}",
            "{% case %}",
            "       ^",
            "Unexpected token"
        );
    }
```

- [ ] **Step 4: Refactor testSwitchCaseMissingRightBrace**

Replace:
```java
    @Test
    public void testSwitchCaseMissingRightBrace() throws IOException {
        assertSyntaxError("{% switch x %}{% case 1 %}content{% endswitch %}", "Expected");
    }
```

With:
```java
    @Test
    public void testSwitchCaseMissingRightBrace() throws IOException {
        assertSyntaxErrorWithPointer("{% switch x %}{% case 1 %}content{% endswitch %}",
            "{% case 1 %}",
            "          ^",
            "Expected"
        );
    }
```

- [ ] **Step 5: Refactor testSwitchDefaultMissingRightBrace**

Replace:
```java
    @Test
    public void testSwitchDefaultMissingRightBrace() throws IOException {
        assertSyntaxError("{% switch x %}{% default %}content{% endswitch %}", "Expected");
    }
```

With:
```java
    @Test
    public void testSwitchDefaultMissingRightBrace() throws IOException {
        assertSyntaxErrorWithPointer("{% switch x %}{% default %}content{% endswitch %}",
            "{% default %}",
            "           ^",
            "Expected"
        );
    }
```

- [ ] **Step 6: Run tests to verify**

```bash
mvn test -Dtest=SyntaxErrorTest -q
```

Expected: All tests pass. Adjust pointer positions if needed.

- [ ] **Step 7: Commit**

```bash
git add src/test/java/org/dromara/easytl/parser/SyntaxErrorTest.java
git commit -m "test: add location verification for switch statement errors"
```

---

### Task 10: Refactor SyntaxErrorTest - Part 5: Let Statement Errors

**Files:**
- Modify: `src/test/java/org/dromara/easytl/parser/SyntaxErrorTest.java`

- [ ] **Step 1: Refactor testLetStatementMissingAssign**

Replace:
```java
    @Test
    public void testLetStatementMissingAssign() throws IOException {
        assertSyntaxError("{% let x 5 %}", "Expected");
    }
```

With:
```java
    @Test
    public void testLetStatementMissingAssign() throws IOException {
        assertSyntaxErrorWithPointer("{% let x 5 %}",
            "{% let x 5 %}",
            "        ^",
            "Expected"
        );
    }
```

- [ ] **Step 2: Refactor testLetStatementMissingValue**

Replace:
```java
    @Test
    public void testLetStatementMissingValue() throws IOException {
        assertSyntaxError("{% let x = %}", "Unexpected token");
    }
```

With:
```java
    @Test
    public void testLetStatementMissingValue() throws IOException {
        assertSyntaxErrorWithPointer("{% let x = %}",
            "{% let x = %}",
            "          ^",
            "Unexpected token"
        );
    }
```

- [ ] **Step 3: Refactor testLetStatementIfExpressionWithoutElse**

Replace:
```java
    @Test
    public void testLetStatementIfExpressionWithoutElse() throws IOException {
        assertSyntaxError(
            "{% let x = if (true) { 1 } %}",
            "If expression in let assignment must include else branch"
        );
    }
```

With:
```java
    @Test
    public void testLetStatementIfExpressionWithoutElse() throws IOException {
        assertSyntaxErrorWithPointer("{% let x = if (true) { 1 } %}",
            "{ if (true) { 1 }",
            "^^^^^^^^^^^^^^^^^",
            "If expression in let assignment must include else branch"
        );
    }
```

- [ ] **Step 4: Refactor testLetStatementIfExpressionWithElseIfButNoElse**

Replace:
```java
    @Test
    public void testLetStatementIfExpressionWithElseIfButNoElse() throws IOException {
        assertSyntaxError(
            "{% let x = if (false) { 1 } else if (true) { 2 } %}",
            "If expression in let assignment must include else branch"
        );
    }
```

With:
```java
    @Test
    public void testLetStatementIfExpressionWithElseIfButNoElse() throws IOException {
        assertSyntaxErrorWithPointer("{% let x = if (false) { 1 } else if (true) { 2 } %}",
            "{ if (false) { 1 } else if (true) { 2 }",
            "^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^",
            "If expression in let assignment must include else branch"
        );
    }
```

- [ ] **Step 5: Run tests to verify**

```bash
mvn test -Dtest=SyntaxErrorTest -q
```

Expected: All tests pass. Adjust pointer positions if needed.

- [ ] **Step 6: Commit**

```bash
git add src/test/java/org/dromara/easytl/parser/SyntaxErrorTest.java
git commit -m "test: add location verification for let statement errors"
```

---

### Task 11: Refactor SyntaxErrorTest - Part 6: Import/Extends Statement Errors

**Files:**
- Modify: `src/test/java/org/dromara/easytl/parser/SyntaxErrorTest.java`

- [ ] **Step 1: Refactor testExtendsStatementMissingString**

Replace:
```java
    @Test
    public void testExtendsStatementMissingString() throws IOException {
        assertSyntaxError("{% extends %}", "Expected");
    }
```

With:
```java
    @Test
    public void testExtendsStatementMissingString() throws IOException {
        assertSyntaxErrorWithPointer("{% extends %}",
            "{% extends %}",
            "          ^",
            "Expected"
        );
    }
```

- [ ] **Step 2: Refactor testImportStatementMissingIdentifier**

Replace:
```java
    @Test
    public void testImportStatementMissingIdentifier() throws IOException {
        assertSyntaxError("{% import %}", "Expected");
    }
```

With:
```java
    @Test
    public void testImportStatementMissingIdentifier() throws IOException {
        assertSyntaxErrorWithPointer("{% import %}",
            "{% import %}",
            "         ^",
            "Expected"
        );
    }
```

- [ ] **Step 3: Refactor testImportStatementIncompleteDot**

Replace:
```java
    @Test
    public void testImportStatementIncompleteDot() throws IOException {
        assertSyntaxError("{% import java. %}", "Expected");
    }
```

With:
```java
    @Test
    public void testImportStatementIncompleteDot() throws IOException {
        assertSyntaxErrorWithPointer("{% import java. %}",
            "{% import java. %}",
            "             ^",
            "Expected"
        );
    }
```

- [ ] **Step 4: Run tests to verify**

```bash
mvn test -Dtest=SyntaxErrorTest -q
```

Expected: All tests pass. Adjust pointer positions if needed.

- [ ] **Step 5: Commit**

```bash
git add src/test/java/org/dromara/easytl/parser/SyntaxErrorTest.java
git commit -m "test: add location verification for import/extends statement errors"
```

---

### Task 12: Refactor SyntaxErrorTest - Part 7: Expression Errors

**Files:**
- Modify: `src/test/java/org/dromara/easytl/parser/SyntaxErrorTest.java`

- [ ] **Step 1: Refactor testTernaryExpressionMissingTrueExpr**

Replace:
```java
    @Test
    public void testTernaryExpressionMissingTrueExpr() throws IOException {
        assertSyntaxError("{x ? : y}", "Unexpected token");
    }
```

With:
```java
    @Test
    public void testTernaryExpressionMissingTrueExpr() throws IOException {
        assertSyntaxErrorWithPointer("{x ? : y}",
            "{x ? : y}",
            "     ^",
            "Unexpected token"
        );
    }
```

- [ ] **Step 2: Refactor testTernaryExpressionMissingFalseExpr**

Replace:
```java
    @Test
    public void testTernaryExpressionMissingFalseExpr() throws IOException {
        assertSyntaxError("{x ? y : }", "Unexpected token");
    }
```

With:
```java
    @Test
    public void testTernaryExpressionMissingFalseExpr() throws IOException {
        assertSyntaxErrorWithPointer("{x ? y : }",
            "{x ? y : }",
            "         ^",
            "Unexpected token"
        );
    }
```

- [ ] **Step 3: Refactor testElvisExpressionMissingRightOperand**

Replace:
```java
    @Test
    public void testElvisExpressionMissingRightOperand() throws IOException {
        assertSyntaxError("{x ?? }", "Unexpected token");
    }
```

With:
```java
    @Test
    public void testElvisExpressionMissingRightOperand() throws IOException {
        assertSyntaxErrorWithPointer("{x ?? }",
            "{x ?? }",
            "     ^",
            "Unexpected token"
        );
    }
```

- [ ] **Step 4: Refactor testMethodCallMissingComma**

Replace:
```java
    @Test
    public void testMethodCallMissingComma() throws IOException {
        assertSyntaxError("{func(x y)}", "Expected");
    }
```

With:
```java
    @Test
    public void testMethodCallMissingComma() throws IOException {
        assertSyntaxErrorWithPointer("{func(x y)}",
            "{func(x y)}",
            "       ^",
            "Expected"
        );
    }
```

- [ ] **Step 5: Refactor testMethodCallInvalidTarget**

Replace:
```java
    @Test
    public void testMethodCallInvalidTarget() throws IOException {
        assertSyntaxError("{5()}", "Invalid method call target");
    }
```

With:
```java
    @Test
    public void testMethodCallInvalidTarget() throws IOException {
        assertSyntaxErrorWithPointer("{5()}",
            "{5()}",
            "  ^",
            "Invalid method call target"
        );
    }
```

- [ ] **Step 6: Refactor testSafeNavMissingIdentifier**

Replace:
```java
    @Test
    public void testSafeNavMissingIdentifier() throws IOException {
        assertSyntaxError("{obj?.}", "Expected");
    }
```

With:
```java
    @Test
    public void testSafeNavMissingIdentifier() throws IOException {
        assertSyntaxErrorWithPointer("{obj?.}",
            "{obj?.}",
            "      ^",
            "Expected"
        );
    }
```

- [ ] **Step 7: Refactor testIndexAccessMissingIndex**

Replace:
```java
    @Test
    public void testIndexAccessMissingIndex() throws IOException {
        assertSyntaxError("{list[]}", "Unexpected token");
    }
```

With:
```java
    @Test
    public void testIndexAccessMissingIndex() throws IOException {
        assertSyntaxErrorWithPointer("{list[]}",
            "{list[]}",
            "      ^",
            "Unexpected token"
        );
    }
```

- [ ] **Step 8: Refactor testParenthesizedExpressionMissingRightParen**

Replace:
```java
    @Test
    public void testParenthesizedExpressionMissingRightParen() throws IOException {
        assertSyntaxError("{(x}", "Expected");
    }
```

With:
```java
    @Test
    public void testParenthesizedExpressionMissingRightParen() throws IOException {
        assertSyntaxErrorWithPointer("{(x}",
            "{(x}",
            "   ^",
            "Expected"
        );
    }
```

- [ ] **Step 9: Refactor testParenthesizedExpressionEmpty**

Replace:
```java
    @Test
    public void testParenthesizedExpressionEmpty() throws IOException {
        assertSyntaxError("{()}", "Unexpected token");
    }
```

With:
```java
    @Test
    public void testParenthesizedExpressionEmpty() throws IOException {
        assertSyntaxErrorWithPointer("{()}",
            "{()}",
            "  ^",
            "Unexpected token"
        );
    }
```

- [ ] **Step 10: Run tests to verify**

```bash
mvn test -Dtest=SyntaxErrorTest -q
```

Expected: All tests pass. Adjust pointer positions if needed.

- [ ] **Step 11: Commit**

```bash
git add src/test/java/org/dromara/easytl/parser/SyntaxErrorTest.java
git commit -m "test: add location verification for expression errors"
```

---

### Task 13: Refactor SyntaxErrorTest - Part 8: Literal Errors

**Files:**
- Modify: `src/test/java/org/dromara/easytl/parser/SyntaxErrorTest.java`

- [ ] **Step 1: Refactor testListLiteralMissingRightBracket**

Replace:
```java
    @Test
    public void testListLiteralMissingRightBracket() throws IOException {
        assertSyntaxError("{[1, 2}", "Expected");
    }
```

With:
```java
    @Test
    public void testListLiteralMissingRightBracket() throws IOException {
        assertSyntaxErrorWithPointer("{[1, 2}",
            "{[1, 2}",
            "      ^",
            "Expected"
        );
    }
```

- [ ] **Step 2: Refactor testListLiteralMissingComma**

Replace:
```java
    @Test
    public void testListLiteralMissingComma() throws IOException {
        assertSyntaxError("{[1 2]}", "Expected");
    }
```

With:
```java
    @Test
    public void testListLiteralMissingComma() throws IOException {
        assertSyntaxErrorWithPointer("{[1 2]}",
            "{[1 2]}",
            "    ^",
            "Expected"
        );
    }
```

- [ ] **Step 3: Refactor testHashLiteralMissingRightBrace**

Replace:
```java
    @Test
    public void testHashLiteralMissingRightBrace() throws IOException {
        assertSyntaxError("{{key: value}", "Unclosed expression");
    }
```

With:
```java
    @Test
    public void testHashLiteralMissingRightBrace() throws IOException {
        assertSyntaxErrorWithPointer("{{key: value}",
            "{{key: value}",
            "            ^",
            "Unclosed expression"
        );
    }
```

- [ ] **Step 4: Refactor testHashLiteralMissingColon**

Replace:
```java
    @Test
    public void testHashLiteralMissingColon() throws IOException {
        assertSyntaxError("{{key value}}", "Expected");
    }
```

With:
```java
    @Test
    public void testHashLiteralMissingColon() throws IOException {
        assertSyntaxErrorWithPointer("{{key value}}",
            "{{key value}}",
            "     ^^^^^",
            "Expected"
        );
    }
```

- [ ] **Step 5: Refactor testHashLiteralInvalidKey**

Replace:
```java
    @Test
    public void testHashLiteralInvalidKey() throws IOException {
        assertSyntaxError("{{true: value}}", "Invalid hash key");
    }
```

With:
```java
    @Test
    public void testHashLiteralInvalidKey() throws IOException {
        assertSyntaxErrorWithPointer("{{true: value}}",
            "{{true: value}}",
            "  ^^^^",
            "Invalid hash key"
        );
    }
```

- [ ] **Step 6: Refactor testHashLiteralMissingValue**

Replace:
```java
    @Test
    public void testHashLiteralMissingValue() throws IOException {
        assertSyntaxError("{{key: }}", "Unexpected token");
    }
```

With:
```java
    @Test
    public void testHashLiteralMissingValue() throws IOException {
        assertSyntaxErrorWithPointer("{{key: }}",
            "{{key: }}",
            "       ^",
            "Unexpected token"
        );
    }
```

- [ ] **Step 7: Refactor testHashLiteralMissingComma**

Replace:
```java
    @Test
    public void testHashLiteralMissingComma() throws IOException {
        assertSyntaxError("{{key1: value1 key2: value2}}", "Expected");
    }
```

With:
```java
    @Test
    public void testHashLiteralMissingComma() throws IOException {
        assertSyntaxErrorWithPointer("{{key1: value1 key2: value2}}",
            "{{key1: value1 key2: value2}}",
            "             ^^^^",
            "Expected"
        );
    }
```

- [ ] **Step 8: Run tests to verify**

```bash
mvn test -Dtest=SyntaxErrorTest -q
```

Expected: All tests pass. Adjust pointer positions if needed.

- [ ] **Step 9: Commit**

```bash
git add src/test/java/org/dromara/easytl/parser/SyntaxErrorTest.java
git commit -m "test: add location verification for literal errors"
```

---

### Task 14: Refactor SyntaxErrorTest - Part 9: Structured Expression Errors

**Files:**
- Modify: `src/test/java/org/dromara/easytl/parser/SyntaxErrorTest.java`

- [ ] **Step 1: Refactor testIfExpressionMissingLeftParen**

Replace:
```java
    @Test
    public void testIfExpressionMissingLeftParen() throws IOException {
        assertSyntaxError("{if true { 1 } else { 2 }}", "Expected");
    }
```

With:
```java
    @Test
    public void testIfExpressionMissingLeftParen() throws IOException {
        assertSyntaxErrorWithPointer("{if true { 1 } else { 2 }}",
            "{if true { 1 } else { 2 }}",
            "    ^^^^",
            "Expected"
        );
    }
```

- [ ] **Step 2: Refactor testIfExpressionMissingRightParen**

Replace:
```java
    @Test
    public void testIfExpressionMissingRightParen() throws IOException {
        assertSyntaxError("{if (true { 1 } else { 2 }}", "Expected");
    }
```

With:
```java
    @Test
    public void testIfExpressionMissingRightParen() throws IOException {
        assertSyntaxErrorWithPointer("{if (true { 1 } else { 2 }}",
            "{if (true { 1 } else { 2 }}",
            "        ^",
            "Expected"
        );
    }
```

- [ ] **Step 3: Refactor testIfExpressionMissingLeftBrace**

Replace:
```java
    @Test
    public void testIfExpressionMissingLeftBrace() throws IOException {
        assertSyntaxError("{if (true) 1 else { 2 }}", "Expected");
    }
```

With:
```java
    @Test
    public void testIfExpressionMissingLeftBrace() throws IOException {
        assertSyntaxErrorWithPointer("{if (true) 1 else { 2 }}",
            "{if (true) 1 else { 2 }}",
            "           ^",
            "Expected"
        );
    }
```

- [ ] **Step 4: Refactor testIfExpressionMissingRightBrace**

Replace:
```java
    @Test
    public void testIfExpressionMissingRightBrace() throws IOException {
        assertSyntaxError("{if (true) { 1 else { 2 }}", "Unexpected token");
    }
```

With:
```java
    @Test
    public void testIfExpressionMissingRightBrace() throws IOException {
        assertSyntaxErrorWithPointer("{if (true) { 1 else { 2 }}",
            "{if (true) { 1 else { 2 }}",
            "              ^^^^",
            "Unexpected token"
        );
    }
```

- [ ] **Step 5: Refactor testIfExpressionElseIfMissingLeftParen**

Replace:
```java
    @Test
    public void testIfExpressionElseIfMissingLeftParen() throws IOException {
        assertSyntaxError("{if (false) { 1 } else if true { 2 } else { 3 }}", "Expected");
    }
```

With:
```java
    @Test
    public void testIfExpressionElseIfMissingLeftParen() throws IOException {
        assertSyntaxErrorWithPointer("{if (false) { 1 } else if true { 2 } else { 3 }}",
            "{if (false) { 1 } else if true { 2 } else { 3 }}",
            "                          ^^^^",
            "Expected"
        );
    }
```

- [ ] **Step 6: Refactor testIfExpressionElseIfMissingRightParen**

Replace:
```java
    @Test
    public void testIfExpressionElseIfMissingRightParen() throws IOException {
        assertSyntaxError("{if (false) { 1 } else if (true { 2 } else { 3 }}", "Expected");
    }
```

With:
```java
    @Test
    public void testIfExpressionElseIfMissingRightParen() throws IOException {
        assertSyntaxErrorWithPointer("{if (false) { 1 } else if (true { 2 } else { 3 }}",
            "{if (false) { 1 } else if (true { 2 } else { 3 }}",
            "                               ^",
            "Expected"
        );
    }
```

- [ ] **Step 7: Refactor testForExpressionMissingLeftParen**

Replace:
```java
    @Test
    public void testForExpressionMissingLeftParen() throws IOException {
        assertSyntaxError("{for item in list { x } }", "Expected");
    }
```

With:
```java
    @Test
    public void testForExpressionMissingLeftParen() throws IOException {
        assertSyntaxErrorWithPointer("{for item in list { x } }",
            "{for item in list { x } }",
            "     ^^^^",
            "Expected"
        );
    }
```

- [ ] **Step 8: Refactor testForExpressionMissingRightParen**

Replace:
```java
    @Test
    public void testForExpressionMissingRightParen() throws IOException {
        assertSyntaxError("{for (item in list { x } }", "Expected");
    }
```

With:
```java
    @Test
    public void testForExpressionMissingRightParen() throws IOException {
        assertSyntaxErrorWithPointer("{for (item in list { x } }",
            "{for (item in list { x } }",
            "                  ^",
            "Expected"
        );
    }
```

- [ ] **Step 9: Refactor testForExpressionMissingLeftBrace**

Replace:
```java
    @Test
    public void testForExpressionMissingLeftBrace() throws IOException {
        assertSyntaxError("{for (item in list) x }", "Expected");
    }
```

With:
```java
    @Test
    public void testForExpressionMissingLeftBrace() throws IOException {
        assertSyntaxErrorWithPointer("{for (item in list) x }",
            "{for (item in list) x }",
            "                    ^",
            "Expected"
        );
    }
```

- [ ] **Step 10: Refactor testForExpressionMissingRightBrace**

Replace:
```java
    @Test
    public void testForExpressionMissingRightBrace() throws IOException {
        assertSyntaxError("{for (item in list) { x }", "Unclosed expression");
    }
```

With:
```java
    @Test
    public void testForExpressionMissingRightBrace() throws IOException {
        assertSyntaxErrorWithPointer("{for (item in list) { x }",
            "{for (item in list) { x }",
            "                        ^",
            "Unclosed expression"
        );
    }
```

- [ ] **Step 11: Run tests to verify**

```bash
mvn test -Dtest=SyntaxErrorTest -q
```

Expected: All tests pass. Adjust pointer positions if needed.

- [ ] **Step 12: Commit**

```bash
git add src/test/java/org/dromara/easytl/parser/SyntaxErrorTest.java
git commit -m "test: add location verification for structured expression errors"
```

---

### Task 15: Refactor SyntaxErrorTest - Part 10: Remaining Errors

**Files:**
- Modify: `src/test/java/org/dromara/easytl/parser/SyntaxErrorTest.java`

- [ ] **Step 1: Refactor testSwitchExpressionMissingLeftParen**

Replace:
```java
    @Test
    public void testSwitchExpressionMissingLeftParen() throws IOException {
        assertSyntaxError("{switch x { case 1: { 1 } }}", "Unexpected token");
    }
```

With:
```java
    @Test
    public void testSwitchExpressionMissingLeftParen() throws IOException {
        assertSyntaxErrorWithPointer("{switch x { case 1: { 1 } }}",
            "{switch x { case 1: { 1 } }}",
            "        ^",
            "Unexpected token"
        );
    }
```

- [ ] **Step 2: Refactor testSwitchExpressionMissingRightParen**

Replace:
```java
    @Test
    public void testSwitchExpressionMissingRightParen() throws IOException {
        assertSyntaxError("{switch (x { case 1: { 1 } }}", "Unexpected token");
    }
```

With:
```java
    @Test
    public void testSwitchExpressionMissingRightParen() throws IOException {
        assertSyntaxErrorWithPointer("{switch (x { case 1: { 1 } }}",
            "{switch (x { case 1: { 1 } }}",
            "          ^",
            "Unexpected token"
        );
    }
```

- [ ] **Step 3: Refactor testSwitchExpressionMissingLeftBrace**

Replace:
```java
    @Test
    public void testSwitchExpressionMissingLeftBrace() throws IOException {
        assertSyntaxError("{switch (x) case 1: { 1 } }", "Unexpected token");
    }
```

With:
```java
    @Test
    public void testSwitchExpressionMissingLeftBrace() throws IOException {
        assertSyntaxErrorWithPointer("{switch (x) case 1: { 1 } }",
            "{switch (x) case 1: { 1 } }",
            "            ^^^^",
            "Unexpected token"
        );
    }
```

- [ ] **Step 4: Refactor testSwitchExpressionMissingRightBrace**

Replace:
```java
    @Test
    public void testSwitchExpressionMissingRightBrace() throws IOException {
        assertSyntaxError("{switch (x) { case 1: { 1 } }", "Unexpected token");
    }
```

With:
```java
    @Test
    public void testSwitchExpressionMissingRightBrace() throws IOException {
        assertSyntaxErrorWithPointer("{switch (x) { case 1: { 1 } }",
            "{switch (x) { case 1: { 1 } }",
            "                             ^",
            "Unexpected token"
        );
    }
```

- [ ] **Step 5: Refactor testSwitchExpressionCaseMissingExpression**

Replace:
```java
    @Test
    public void testSwitchExpressionCaseMissingExpression() throws IOException {
        assertSyntaxError("{switch (x) { case : { 1 } }}", "Unexpected token");
    }
```

With:
```java
    @Test
    public void testSwitchExpressionCaseMissingExpression() throws IOException {
        assertSyntaxErrorWithPointer("{switch (x) { case : { 1 } }}",
            "{switch (x) { case : { 1 } }}",
            "                  ^",
            "Unexpected token"
        );
    }
```

- [ ] **Step 6: Refactor testSwitchExpressionCaseMissingColon**

Replace:
```java
    @Test
    public void testSwitchExpressionCaseMissingColon() throws IOException {
        assertSyntaxError("{switch (x) { case 1 { 1 } }}", "Unexpected token");
    }
```

With:
```java
    @Test
    public void testSwitchExpressionCaseMissingColon() throws IOException {
        assertSyntaxErrorWithPointer("{switch (x) { case 1 { 1 } }}",
            "{switch (x) { case 1 { 1 } }}",
            "                   ^",
            "Unexpected token"
        );
    }
```

- [ ] **Step 7: Refactor testRangeExpressionMissingRightOperand**

Replace:
```java
    @Test
    public void testRangeExpressionMissingRightOperand() throws IOException {
        assertSyntaxError("{1..}", "Unexpected token");
    }
```

With:
```java
    @Test
    public void testRangeExpressionMissingRightOperand() throws IOException {
        assertSyntaxErrorWithPointer("{1..}",
            "{1..}",
            "   ^",
            "Unexpected token"
        );
    }
```

- [ ] **Step 8: Refactor testUnexpectedTokenInExpression**

Replace:
```java
    @Test
    public void testUnexpectedTokenInExpression() throws IOException {
        assertSyntaxError("{% %}", "Unexpected token");
    }
```

With:
```java
    @Test
    public void testUnexpectedTokenInExpression() throws IOException {
        assertSyntaxErrorWithPointer("{% %}",
            "{% %}",
            "   ^",
            "Unexpected token"
        );
    }
```

- [ ] **Step 9: Refactor testUnexpectedTokenAfterExpression**

Replace:
```java
    @Test
    public void testUnexpectedTokenAfterExpression() throws IOException {
        assertSyntaxError("{x %}", "Unexpected token");
    }
```

With:
```java
    @Test
    public void testUnexpectedTokenAfterExpression() throws IOException {
        assertSyntaxErrorWithPointer("{x %}",
            "{x %}",
            "   ^",
            "Unexpected token"
        );
    }
```

- [ ] **Step 10: Refactor nested error tests**

Replace:
```java
    @Test
    public void testNestedIfMissingEndTag() throws IOException {
        assertSyntaxError("{% if true %}{% if false %}content{% endif %}", "Expected");
    }
```

With:
```java
    @Test
    public void testNestedIfMissingEndTag() throws IOException {
        assertSyntaxErrorWithPointer("{% if true %}{% if false %}content{% endif %}",
            "{% if true %}{% if false %}content{% endif %}",
            "                                           ^",
            "Expected"
        );
    }
```

Replace:
```java
    @Test
    public void testNestedForMissingEndTag() throws IOException {
        assertSyntaxError("{% for x in list %}{% for y in list %}content{% endfor %}", "Expected");
    }
```

With:
```java
    @Test
    public void testNestedForMissingEndTag() throws IOException {
        assertSyntaxErrorWithPointer("{% for x in list %}{% for y in list %}content{% endfor %}",
            "{% for x in list %}{% for y in list %}content{% endfor %}",
            "                                                      ^",
            "Expected"
        );
    }
```

Replace:
```java
    @Test
    public void testMixedNestedStatementsMissingEndTag() throws IOException {
        assertSyntaxError("{% if true %}{% for x in list %}content{% endfor %}", "Expected");
    }
```

With:
```java
    @Test
    public void testMixedNestedStatementsMissingEndTag() throws IOException {
        assertSyntaxErrorWithPointer("{% if true %}{% for x in list %}content{% endfor %}",
            "{% if true %}{% for x in list %}content{% endfor %}",
            "                                                  ^",
            "Expected"
        );
    }
```

- [ ] **Step 11: Run tests to verify**

```bash
mvn test -Dtest=SyntaxErrorTest -q
```

Expected: All tests pass. Adjust pointer positions if needed.

- [ ] **Step 12: Commit**

```bash
git add src/test/java/org/dromara/easytl/parser/SyntaxErrorTest.java
git commit -m "test: add location verification for remaining errors"
```

---

### Task 16: Remove Unused assertSyntaxError Method

**Files:**
- Modify: `src/test/java/org/dromara/easytl/parser/SyntaxErrorTest.java`

- [ ] **Step 1: Remove unused assertSyntaxError methods**

Remove the following methods from `SyntaxErrorTest.java`:

```java
    /**
     * 辅助方法：验证解析会抛出语法错误
     */
    private void assertSyntaxError(String input, String expectedMessageContains) throws IOException {
        Parser parser = createParser(input);
        TemplateSyntaxException exception = assertThrows(
            TemplateSyntaxException.class,
            () -> parser.parse(),
            "应该抛出 TemplateSyntaxException"
        );
        
        if (expectedMessageContains != null) {
            assertTrue(
                exception.getMessage().contains(expectedMessageContains),
                "异常消息应该包含: " + expectedMessageContains + "，实际: " + exception.getMessage()
            );
            System.out.println(exception.getMessage());
        }
    }

    /**
     * 辅助方法：验证解析会抛出语法错误（不检查消息内容）
     */
    @SuppressWarnings("unused")
    private void assertSyntaxError(String input) throws IOException {
        assertSyntaxError(input, null);
    }
```

- [ ] **Step 2: Run all tests to verify**

```bash
mvn test -Dtest=SyntaxErrorTest -q
```

Expected: All tests pass.

- [ ] **Step 3: Commit**

```bash
git add src/test/java/org/dromara/easytl/parser/SyntaxErrorTest.java
git commit -m "refactor: remove unused assertSyntaxError methods from SyntaxErrorTest"
```

---

### Task 17: Final Verification

**Files:**
- None (verification only)

- [ ] **Step 1: Run all tests**

```bash
mvn test -q
```

Expected: All tests pass (1882+ tests).

- [ ] **Step 2: Run specific test classes**

```bash
mvn test -Dtest=CompilerValidatorLocationTest,SyntaxErrorTest -q
```

Expected: All tests pass.

- [ ] **Step 3: Final commit (if any remaining changes)**

```bash
git status
```

If there are uncommitted changes:
```bash
git add -A
git commit -m "chore: final cleanup for compile error location tests"
```

---

## Summary

| Task | Description |
|------|-------------|
| 1 | Create test template files |
| 2 | Rename ExportValidatorLocationTest to CompilerValidatorLocationTest |
| 3 | Add TemplateJavaImportValidator location test |
| 4 | Add ImportTemplateValidator location tests |
| 5 | Delete TemplateJavaImportValidatorTest |
| 6-16 | Refactor SyntaxErrorTest (parts 1-11) |
| 17 | Final verification |

Total: 17 tasks with bite-sized steps following TDD principles.