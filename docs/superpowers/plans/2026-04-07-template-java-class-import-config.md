# Template Java Class Import Config Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add `templateJavaClassImportEnabled` (default `true`) on engine/builder config; when `false`, reject Java class `{% import %}` (`ImportStatement`) at compile time for the root template and at parse time for `{% extends %}` child templates, while keeping `Context.importClass` unrestricted.

**Architecture:** A dedicated `TemplateJavaImportValidator` walks the full AST and throws `TemplateCompileException` when disabled and an `ImportStatement` is found. `TemplateEngine.compileInternal` runs it after parse. The same flag is stored on `InterpreterTemplate` and passed through every `Evaluator` (and nested `Evaluator`, `LambdaObject`, `EtlURL`) so `visitExtendsStatement` can validate the child AST before execution. Only `ImportStatement` is checked so future template-file import nodes stay allowed.

**Tech stack:** Java 8, Maven, JUnit 5 (`mvn test`).

**Spec:** `docs/superpowers/specs/2026-04-07-template-java-class-import-config-design.md`

---

## File map

| File | Role |
|------|------|
| `src/main/java/org/dromara/easytl/TemplateEngine.java` | Config field + getter/setter; `compileInternal` calls validator; pass flag into `InterpreterCompiler` |
| `src/main/java/org/dromara/easytl/EngineBuilder.java` | Chain setter → config |
| `src/main/java/org/dromara/easytl/ConfigBuilder.java` | Chain setter → config |
| `src/main/java/org/dromara/easytl/TemplateBuilder.java` | Chain setter → `EngineBuilder` in `getOrCreateEngine()`; Javadoc when `engine(...)` is set |
| `src/main/java/org/dromara/easytl/compiler/InterpreterCompiler.java` | Accept flag; pass to `InterpreterTemplate` |
| `src/main/java/org/dromara/easytl/compiler/InterpreterTemplate.java` | Store flag; pass to `Evaluator` in `render` |
| `src/main/java/org/dromara/easytl/compiler/Evaluator.java` | Field + constructor overload; propagate flag to all `new Evaluator(...)`; validate in `visitExtendsStatement`; pass to `LambdaObject` / `EtlURL` |
| `src/main/java/org/dromara/easytl/compiler/TemplateJavaImportValidator.java` | **New** — static `validate(TemplateNode, boolean, String)` |
| `src/main/java/org/dromara/easytl/runtime/LambdaObject.java` | Store flag; `invoke` uses `new Evaluator(..., flag)` |
| `src/main/java/org/dromara/easytl/runtime/EtlURL.java` | Store flag; `buildUrlString` passes to `Evaluator` |
| `src/test/java/org/dromara/easytl/TemplateEngineTest.java` | Default + chaining assertions |
| `src/test/java/org/dromara/easytl/ConfigBuilderTest.java` | Builder wires flag |
| `src/test/java/org/dromara/easytl/TemplateJavaImportConfigTest.java` | **New** — compile/extends/importClass behavior |

**Out of scope:** `Eval.java` static helpers keep using `new Evaluator(context, null, source)` (3-arg) so default remains `true` unless extended later.

---

### Task 1: Config API on `TemplateEngineConfig` (TDD)

**Files:**
- Modify: `src/main/java/org/dromara/easytl/TemplateEngine.java` (`TemplateEngineConfig` inner class)
- Test: `src/test/java/org/dromara/easytl/TemplateEngineTest.java`

- [ ] **Step 1: Write failing tests**

Add to `TemplateEngineTest`:

```java
@Test
void testTemplateJavaClassImportEnabledDefaultTrue() {
    TemplateEngine.TemplateEngineConfig config = engine.getConfig();
    assertTrue(config.isTemplateJavaClassImportEnabled());
}

@Test
void testTemplateJavaClassImportEnabledChaining() {
    TemplateEngine.TemplateEngineConfig config = new TemplateEngine.TemplateEngineConfig()
            .setTemplateJavaClassImportEnabled(false);
    assertFalse(config.isTemplateJavaClassImportEnabled());
    assertSame(config, config.setTemplateJavaClassImportEnabled(true));
    assertTrue(config.isTemplateJavaClassImportEnabled());
}
```

- [ ] **Step 2: Run tests — expect failure**

Run: `mvn test -Dtest=TemplateEngineTest#testTemplateJavaClassImportEnabledDefaultTrue -q`  
Expected: compilation failure or test failure (method missing).

- [ ] **Step 3: Implement on `TemplateEngineConfig`**

In `TemplateEngine.java` inner class, mirror `strictMode`:

- Private field `private boolean templateJavaClassImportEnabled = true;`
- `public boolean isTemplateJavaClassImportEnabled() { return templateJavaClassImportEnabled; }`
- `public TemplateEngineConfig setTemplateJavaClassImportEnabled(boolean templateJavaClassImportEnabled) { this.templateJavaClassImportEnabled = templateJavaClassImportEnabled; return this; }`

Update `testConfigDefaultValues` in the same test class to assert `assertTrue(config.isTemplateJavaClassImportEnabled());`.

- [ ] **Step 4: Run tests — expect pass**

Run: `mvn test -Dtest=TemplateEngineTest -q`  
Expected: BUILD SUCCESS.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/org/dromara/easytl/TemplateEngine.java src/test/java/org/dromara/easytl/TemplateEngineTest.java
git commit -m "feat(config): add templateJavaClassImportEnabled on TemplateEngineConfig"
```

---

### Task 2: `ConfigBuilder` and `EngineBuilder`

**Files:**
- Modify: `src/main/java/org/dromara/easytl/ConfigBuilder.java`
- Modify: `src/main/java/org/dromara/easytl/EngineBuilder.java`
- Test: `src/test/java/org/dromara/easytl/ConfigBuilderTest.java`

- [ ] **Step 1: Failing test in `ConfigBuilderTest`**

```java
@Test
void testTemplateJavaClassImportEnabled() {
    TemplateEngine.TemplateEngineConfig config = new ConfigBuilder()
            .templateJavaClassImportEnabled(false)
            .build();
    assertFalse(config.isTemplateJavaClassImportEnabled());
}
```

- [ ] **Step 2: Run test — fail**

Run: `mvn test -Dtest=ConfigBuilderTest#testTemplateJavaClassImportEnabled -q`

- [ ] **Step 3: Implement**

`ConfigBuilder`: private field default `true`, method `public ConfigBuilder templateJavaClassImportEnabled(boolean v) { ... return this; }`, and in `build()` chain `.setTemplateJavaClassImportEnabled(...)`.

`EngineBuilder`: same pattern in `build()` when constructing `TemplateEngineConfig`.

- [ ] **Step 4: Run `mvn test -Dtest=ConfigBuilderTest -q`**

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/org/dromara/easytl/ConfigBuilder.java src/main/java/org/dromara/easytl/EngineBuilder.java src/test/java/org/dromara/easytl/ConfigBuilderTest.java
git commit -m "feat(config): wire templateJavaClassImportEnabled in ConfigBuilder and EngineBuilder"
```

---

### Task 3: `TemplateBuilder`

**Files:**
- Modify: `src/main/java/org/dromara/easytl/TemplateBuilder.java`

- [ ] **Step 1:** Add `private Boolean templateJavaClassImportEnabled;` and `public TemplateBuilder templateJavaClassImportEnabled(boolean v) { this.templateJavaClassImportEnabled = v; return this; }`. In `getOrCreateEngine()`:

```java
if (templateJavaClassImportEnabled != null) {
    builder.templateJavaClassImportEnabled(templateJavaClassImportEnabled);
}
```

- [ ] **Step 2:** Javadoc on `engine(TemplateEngine engine)`: when an explicit engine is supplied, compile uses that engine’s `TemplateEngineConfig` only; this builder’s `templateJavaClassImportEnabled(...)` does not modify the supplied engine.

- [ ] **Step 3:** `mvn compile -q`

- [ ] **Step 4:** Commit (integration test for implicit engine is in Task 6, `templateBuilderImplicitEngineDisallowsImport`).

```bash
git add src/main/java/org/dromara/easytl/TemplateBuilder.java
git commit -m "feat: TemplateBuilder supports templateJavaClassImportEnabled for implicit EngineBuilder"
```

---

### Task 4: `TemplateJavaImportValidator`

**Files:**
- Create: `src/main/java/org/dromara/easytl/compiler/TemplateJavaImportValidator.java`
- Test: `src/test/java/org/dromara/easytl/compiler/TemplateJavaImportValidatorTest.java` (new)

- [ ] **Step 1: Unit test**

```java
package org.dromara.easytl.compiler;

import org.dromara.easytl.ast.TemplateNode;
import org.dromara.easytl.exception.TemplateCompileException;
import org.dromara.easytl.parser.Lexer;
import org.dromara.easytl.parser.Parser;
import org.dromara.easytl.parser.StringSource;
import org.dromara.easytl.parser.TokenStream;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TemplateJavaImportValidatorTest {

    private static TemplateNode parse(String src) throws Exception {
        StringSource source = new StringSource(src);
        Lexer lexer = new Lexer(source);
        return new Parser(new TokenStream(lexer)).parse();
    }

    @Test
    void allowed_doesNotThrow() throws Exception {
        String src = "{% import java.lang.Math %}";
        TemplateNode root = parse(src);
        TemplateJavaImportValidator.validate(root, true, src);
    }

    @Test
    void disallowed_throws() throws Exception {
        String src = "{% import java.lang.Math %}";
        TemplateNode root = parse(src);
        assertThrows(TemplateCompileException.class,
            () -> TemplateJavaImportValidator.validate(root, false, src));
    }
}
```

- [ ] **Step 2: Run test — fail**

`mvn test -Dtest=TemplateJavaImportValidatorTest -q`

- [ ] **Step 3: Implement validator**

```java
package org.dromara.easytl.compiler;

import org.dromara.easytl.ast.*;
import org.dromara.easytl.exception.TemplateCompileException;
import org.dromara.easytl.parser.StringSource;

import java.util.List;

/**
 * When Java class import in templates is disabled, rejects any {@link ImportStatement} in the AST.
 */
public final class TemplateJavaImportValidator {

    private TemplateJavaImportValidator() {}

    public static void validate(TemplateNode root, boolean templateJavaClassImportEnabled, String sourceText) {
        if (templateJavaClassImportEnabled || root == null) {
            return;
        }
        root.accept(new Checker(sourceText));
    }

    private static final class Checker implements ASTVisitor<Void> {

        private final String sourceText;

        Checker(String sourceText) {
            this.sourceText = sourceText;
        }

        @Override
        public Void visitImportStatement(ImportStatement node) {
            String msg = "Template Java class import is disabled; use Context.importClass(...) instead.";
            if (node.getPosition() != null && !node.getPosition().isUnknown()) {
                throw new TemplateCompileException(msg, node.getPosition(), new StringSource(sourceText));
            }
            throw new TemplateCompileException(msg);
        }

        @Override
        public Void visitTemplateNode(TemplateNode node) {
            for (ASTNode child : node.getChildren()) {
                child.accept(this);
            }
            return null;
        }

        // Implement ALL remaining ASTVisitor methods:
        // - For each node type that contains nested ASTNode / List<ASTNode>, recurse with child.accept(this)
        // - For leaf expression nodes (literals, Identifier, etc.), return null without recursion
        //
        // Use org.dromara.easytl.compiler.Evaluator as a reference for which getters hold child nodes
        // (e.g. IfStatement getThenBody / getElseBody / ElseIfClause.getBody / getCondition, etc.).
    }
}
```

**Requirement:** The `Checker` class must implement **every** method in `ASTVisitor<Void>` (return `null`). Any omission causes a compile error.

- [ ] **Step 4:** `mvn test -Dtest=TemplateJavaImportValidatorTest -q` — PASS.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/org/dromara/easytl/compiler/TemplateJavaImportValidator.java src/test/java/org/dromara/easytl/compiler/TemplateJavaImportValidatorTest.java
git commit -m "feat(compiler): add TemplateJavaImportValidator for disabled Java class import"
```

---

### Task 5: Wire compile path and `InterpreterTemplate`

**Files:**
- Modify: `src/main/java/org/dromara/easytl/TemplateEngine.java` (`compileInternal`)
- Modify: `src/main/java/org/dromara/easytl/compiler/InterpreterCompiler.java`
- Modify: `src/main/java/org/dromara/easytl/compiler/InterpreterTemplate.java`

- [ ] **Step 1: `compileInternal`**

After `TemplateNode ast = parser.parse();` and **before** `new InterpreterCompiler(...)`, add:

```java
TemplateJavaImportValidator.validate(ast, config.isTemplateJavaClassImportEnabled(), template);
```

Construct compiler with flag:

```java
Compiler compiler = new InterpreterCompiler(
    templateLoader,
    template,
    config.isTemplateJavaClassImportEnabled());
```

- [ ] **Step 2: `InterpreterCompiler`**

Add field `private final boolean templateJavaClassImportEnabled;`, update constructors: no-arg and 1-arg can delegate to `(null, null, true)`. The 2-arg `(loader, text)` becomes `(loader, text, true)` for backward compatibility. Add explicit 3-arg constructor; in `compile`, pass flag into `InterpreterTemplate`.

- [ ] **Step 3: `InterpreterTemplate`**

Add `private final boolean templateJavaClassImportEnabled;`  
Extend constructors: overloads without flag default to `true`.  
In `render`:

```java
Evaluator evaluator = new Evaluator(context, templateLoader, sourceText, templateJavaClassImportEnabled);
```

- [ ] **Step 4: `Evaluator` constructors (minimal for compile)**

Add field:

```java
private final boolean templateJavaClassImportEnabled;
```

Chain existing constructors to `this(..., true)` and add:

```java
public Evaluator(Context context, TemplateLoader templateLoader, String sourceText,
                 boolean templateJavaClassImportEnabled) {
    this.context = context;
    this.templateLoader = templateLoader;
    this.sourceText = sourceText;
    this.templateJavaClassImportEnabled = templateJavaClassImportEnabled;
}
```

Temporarily use `this.templateJavaClassImportEnabled` only in `render` path until Task 6 completes propagation.

- [ ] **Step 5: Integration test**

Add to `TemplateEngineTest` or `TemplateJavaImportConfigTest`:

```java
@Test
void compileFailsWhenJavaClassImportDisabled() {
    TemplateEngine engine = new TemplateEngine(
        new TemplateEngine.TemplateEngineConfig().setTemplateJavaClassImportEnabled(false));
    assertThrows(TemplateCompileException.class,
        () -> engine.compile("{% import java.lang.Math %}"));
}

@Test
void compileSucceedsWhenImportDisabledButNoImportInTemplate() {
    TemplateEngine engine = new TemplateEngine(
        new TemplateEngine.TemplateEngineConfig().setTemplateJavaClassImportEnabled(false));
    Template t = engine.compile("{x}");
    assertNotNull(t);
}
```

- [ ] **Step 6:** `mvn test -q` — fix any constructor breakage in tests that construct `InterpreterCompiler` / `InterpreterTemplate` manually (grep codebase).

- [ ] **Step 7: Commit**

```bash
git add src/main/java/org/dromara/easytl/TemplateEngine.java src/main/java/org/dromara/easytl/compiler/InterpreterCompiler.java src/main/java/org/dromara/easytl/compiler/InterpreterTemplate.java src/main/java/org/dromara/easytl/compiler/Evaluator.java src/test/java/org/dromara/easytl/TemplateEngineTest.java
git commit -m "feat: validate template Java import at compile and pass flag to InterpreterTemplate"
```

---

### Task 6: Full `Evaluator` propagation + extends + Lambda + EtlURL

**Files:**
- Modify: `src/main/java/org/dromara/easytl/compiler/Evaluator.java`
- Modify: `src/main/java/org/dromara/easytl/runtime/LambdaObject.java`
- Modify: `src/main/java/org/dromara/easytl/runtime/EtlURL.java`
- Test: `src/test/java/org/dromara/easytl/TemplateJavaImportConfigTest.java`

- [ ] **Step 1: Replace every internal `new Evaluator(loopContext, templateLoader, sourceText)`** with the 4-arg form passing `this.templateJavaClassImportEnabled` (lines ~604, ~737).

- [ ] **Step 2: `visitExtendsStatement`**

After `TemplateNode externalAst = parser.parse();`, **before** `externalAst.accept(...)`, add:

```java
TemplateJavaImportValidator.validate(externalAst, templateJavaClassImportEnabled,
    templateContent);
```

Use `templateContent` as source text for positions (the loaded file body).

Replace `new Evaluator(context, templateLoader, sourceText)` with 4-arg including `templateJavaClassImportEnabled`.

- [ ] **Step 3: `visitLambdaExpression`**

Add constructor parameter to `LambdaObject` for `templateJavaClassImportEnabled` (see Step 4).

- [ ] **Step 4: `LambdaObject`**

Add field + constructor param (append after `sourceText`). In `invoke`:

```java
Evaluator evaluator = new Evaluator(lambdaContext, null, sourceText, templateJavaClassImportEnabled);
```

Update `Evaluator.visitLambdaExpression` to pass `templateJavaClassImportEnabled`.

- [ ] **Step 5: `EtlURL`**

Add field `private final boolean templateJavaClassImportEnabled;`  
Constructor: add overload or parameter with default `true` for test convenience:

```java
public EtlURL(List<ASTNode> children, Context context,
              TemplateLoader templateLoader, String sourceText) {
    this(children, context, templateLoader, sourceText, true);
}

public EtlURL(List<ASTNode> children, Context context,
              TemplateLoader templateLoader, String sourceText,
              boolean templateJavaClassImportEnabled) {
    ...
}
```

In `buildUrlString`: `new Evaluator(context, templateLoader, sourceText, templateJavaClassImportEnabled)`.

Update `Evaluator.visitUrlLiteral` to pass `templateJavaClassImportEnabled`.

- [ ] **Step 6: Defense in depth — `visitImportStatement`**

If `!templateJavaClassImportEnabled`, throw `TemplateRuntimeException` (or `TemplateCompileException` if you prefer strict symmetry) with the same message — should be unreachable if validator is complete.

- [ ] **Step 7: Extends integration test**

Use `FileTemplateLoader` + temp dir (see `CompilerIntegrationTest` / `TemplateLoaderTest` patterns): parent `{% extends "child.etl" %}`, child file contains `{% import java.lang.Math %}`, engine with `setTemplateJavaClassImportEnabled(false)` and `setTemplateBasePath(tempDir)`. Assert `compile(parent)` succeeds but `render` throws (wrapped) compile failure, **or** if base path resolution requires compile of child only at render — match actual engine behavior.

**Concrete pattern:** Put in `TemplateJavaImportConfigTest` using `@TempDir`:

```java
@Test
void extendsChildWithImportFailsWhenDisabled(@TempDir Path dir) throws Exception {
    Files.write(dir.resolve("base.etl"), "{% extends \"sub.etl\" %}".getBytes(StandardCharsets.UTF_8));
    Files.write(dir.resolve("sub.etl"), "{% import java.lang.Math %}".getBytes(StandardCharsets.UTF_8));
    TemplateEngine.TemplateEngineConfig cfg = new TemplateEngine.TemplateEngineConfig()
            .setTemplateJavaClassImportEnabled(false)
            .setTemplateBasePath(dir.toString());
    TemplateEngine engine = new TemplateEngine(cfg);
    Template t = engine.compile("{% extends \"base.etl\" %}");
    assertThrows(TemplateRuntimeException.class, () -> t.render(new Context()));
}
```

Adjust exception type/message to match actual `visitExtendsStatement` wrapping (`TemplateRuntimeException` with cause `TemplateCompileException` is acceptable; assert `getCause() instanceof TemplateCompileException` if needed).

- [ ] **Step 8: `Context.importClass` still works**

```java
@Test
void importClassWorksWhenTemplateImportDisabled() {
    TemplateEngine engine = new TemplateEngine(
        new TemplateEngine.TemplateEngineConfig().setTemplateJavaClassImportEnabled(false));
    Template t = engine.compile("{Math.abs(-1)}");
    Context ctx = new Context();
    ctx.importClass(Math.class);
    assertEquals("1", t.render(ctx).trim());
}
```

- [ ] **Step 9: `TemplateBuilder` implicit engine test** (from Task 3)

```java
@Test
void templateBuilderImplicitEngineDisallowsImport() {
    assertThrows(TemplateCompileException.class,
        () -> new TemplateBuilder("{% import java.lang.Math %}")
            .templateJavaClassImportEnabled(false)
            .compile());
}
```

- [ ] **Step 10:** `mvn test -q`

- [ ] **Step 11: Commit**

```bash
git add src/main/java/org/dromara/easytl/compiler/Evaluator.java src/main/java/org/dromara/easytl/runtime/LambdaObject.java src/main/java/org/dromara/easytl/runtime/EtlURL.java src/test/java/org/dromara/easytl/TemplateJavaImportConfigTest.java src/test/java/org/dromara/easytl/runtime/EtlURLTest.java
git commit -m "feat: propagate java import flag through Evaluator, extends, lambda, EtlURL"
```

(Update `EtlURLTest` only if constructors changed and tests break.)

---

### Task 7: Final verification

- [ ] **Step 1:** `mvn clean test -q` — full suite green.

- [ ] **Step 2:** Grep for `new Evaluator(` and `new InterpreterCompiler(` ensuring no 3-arg Evaluator used where flag should flow from template (production code paths).

- [ ] **Step 3: Commit** (only if fixes needed)

```bash
git add -A
git commit -m "fix: align tests with template Java import propagation"
```

---

## Self-review (spec coverage)

| Spec item | Task |
|-----------|------|
| `TemplateEngineConfig` + default true | Task 1 |
| `EngineBuilder` / `ConfigBuilder` | Task 2 |
| `TemplateBuilder` + explicit engine Javadoc | Task 3, 6 |
| Compile-time validation root AST | Task 4–5 |
| extends child validation + same flag | Task 6 |
| `Context.importClass` unaffected | Task 6 test |
| Cache unchanged | N/A (no code) |
| Only `ImportStatement` | Task 4 validator |
| Exception type + message hint | Task 4 `TemplateCompileException` |

Placeholder scan: none intentional.

---

## Execution handoff

Plan complete and saved to `docs/superpowers/plans/2026-04-07-template-java-class-import-config.md`. Two execution options:

**1. Subagent-Driven (recommended)** — dispatch a fresh subagent per task, review between tasks, fast iteration.

**2. Inline Execution** — execute tasks in this session using executing-plans, batch execution with checkpoints.

Which approach do you want?
