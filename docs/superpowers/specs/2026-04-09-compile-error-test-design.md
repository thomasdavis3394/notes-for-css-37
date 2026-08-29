---
name: compile-error-test-design
description: Design for CompileErrorTest class to test all compile-time errors via engine.compile(...)
type: project
---

# CompileErrorTest 设计文档

## 背景

当前编译期错误测试分散在多个测试类中：

| 测试类 | 覆盖范围 | 测试方式 |
|--------|----------|----------|
| `SyntaxErrorTest.java` | 语法错误 (TemplateSyntaxException) | 使用 Parser 直接解析，绕过 engine.compile |
| `CompilerValidatorLocationTest.java` | 验证器错误 (TemplateCompileException) | 使用 engine.compile ✓ |
| `EdgeCaseTest.java` | 边界条件 | 部分使用 engine.compile，主要是运行时错误 |

**问题**: SyntaxErrorTest 虽然全面覆盖语法错误，但未通过 `engine.compile(...)` 测试，无法验证完整编译管道中的错误处理。

## 目标

创建 `CompileErrorTest.java`，通过 `engine.compile(...)` 测试所有编译期错误，确保：

1. 所有编译期异常通过 TemplateEngine.compile() API 正确抛出
2. 错误位置信息（行号、列号、指针）准确
3. 错误消息包含关键信息片段

## 设计方案

### 测试类结构

采用类别嵌套类组织方式：

```
CompileErrorTest
├── SyntaxErrors (nested class)
│   ├── Expression wrapper errors
│   ├── Statement errors (if/for/switch/let/extends/import)
│   ├── Expression errors (binary/ternary/elvis/method/member/index/paren)
│   ├── Literal errors (list/hash/range)
│   └── Structured expression errors (if/for/switch expressions)
│
├── ValidatorErrors (nested class)
│   ├── ExportValidator errors
│   ├── ImportTemplateValidator errors
│   ├── TemplateJavaImportValidator errors
│
├── EngineErrors (nested class)
│   ├── Null input errors
│   ├── File errors
│   └── Input stream errors
│
└── NestedErrors (nested class)
    ├── Nested statement errors
    ├── Multiline error location
```

### 辅助方法

```java
/**
 * 验证 engine.compile 抛出异常并校验错误指针
 */
private void assertCompileError(
    String template,
    String expectedSourceLine,
    String expectedPointerLine,
    String expectedErrorMessage
) {
    TemplateException ex = assertThrows(
        TemplateException.class,
        () -> engine.compile(template)
    );
    ErrorPointerAssert.assertErrorPointer(ex,
        expectedSourceLine, expectedPointerLine, expectedErrorMessage);
}
```

### 测试配置

```java
@BeforeEach
void setUp() {
    engine = EasyTL.defaultEngine();
}

// ImportTemplateValidator 测试引擎
private final TemplateEngine engineWithLoader = new TemplateEngine(
    new TemplateEngine.TemplateEngineConfig(),
    new ClasspathTemplateLoader()
);

// Java import 禁用测试引擎
private TemplateEngine createEngineNoJavaImport() {
    return EasyTL.engine()
        .templateJavaClassImportEnabled(false)
        .build();
}
```

## 测试用例清单

### SyntaxErrors (~50 个测试)

#### 表达式包装器错误 (3个)

| 测试方法 | 模板 | 指针 | 错误消息 |
|----------|------|------|----------|
| `testUnclosedSingleBrace` | `{x` | `  ^` | Unclosed expression |
| `testUnclosedDoubleBrace` | `{{x` | `   ^` | Unclosed expression |
| `testUnclosedDollarBrace` | `${x` | `   ^` | Unclosed expression |

#### If 语句错误 (8个)

| 测试方法 | 模板 | 指针 | 错误消息 |
|----------|------|------|----------|
| `testIfMissingCondition` | `{% if } %}` | `      ^` | Unexpected token |
| `testIfMissingRightBrace` | `{% if true %}` | `             ^` | Expected {/% |
| `testIfMissingEndTag` | `{% if true %}content` | `                    ^` | Expected {/% |
| `testIfWrongEndTag` | `{% if true %}content{% endfor %}` | `                                ^` | Expected {/% |
| `testElseIfMissingCondition` | `{% if false %}a{% else if %}b{% endif %}` | `                          ^^` | Unexpected token |
| `testElseIfMissingRightBrace` | `{% if false %}a{% else if true %}b{% endif %}` | `                                             ^` | Expected {/% |
| `testElseMissingRightBrace` | `{% if false %}a{% else %}b{% endif %}` | `                                     ^` | Expected {/% |

#### For 语句错误 (8个)

| 测试方法 | 模板 | 指针 | 错误消息 |
|----------|------|------|----------|
| `testForMissingIdentifier` | `{% for in list %}` | `       ^^` | Expected identifier |
| `testForMissingIn` | `{% for item list %}` | `            ^^^^` | Expected in |
| `testForMissingCollection` | `{% for item in %}` | `               ^^` | Unexpected token |
| `testForMissingRightBrace` | `{% for item in list %}` | `                      ^` | Expected {/% |
| `testForMissingEndTag` | `{% for item in list %}content` | `                             ^` | Expected {/% |
| `testForWrongEndTag` | `{% for item in list %}content{% endif %}` | `                                        ^` | Expected {/% |
| `testForInvalidComma` | `{% for item, in list %}` | `             ^^` | Expected identifier |
| `testForMissingSecondIdentifier` | `{% for index, in list %}` | `              ^^` | Expected identifier |

#### Switch 语句错误 (6个)

| 测试方法 | 模板 | 指针 | 错误消息 |
|----------|------|------|----------|
| `testSwitchMissingExpression` | `{% switch %}` | `          ^^` | Unexpected token |
| `testSwitchMissingRightBrace` | `{% switch x %}` | `              ^` | Expected {/% |
| `testSwitchMissingEndTag` | `{% switch x %}content` | `                     ^` | Expected {/% |
| `testSwitchCaseMissingExpression` | `{% switch x %}{% case %}{% endswitch %}` | `                      ^^` | Unexpected token |
| `testSwitchCaseMissingRightBrace` | `{% switch x %}{% case 1 %}content{% endswitch %}` | `                        ^^` | Expected } |
| `testSwitchDefaultMissingRightBrace` | `{% switch x %}{% default %}content{% endswitch %}` | `                         ^^` | Expected } |

#### Let 语句错误 (5个)

| 测试方法 | 模板 | 指针 | 错误消息 |
|----------|------|------|----------|
| `testLetMissingIdentifier` | `{% let = 5 %}` | `       ^` | Expected identifier |
| `testLetMissingAssign` | `{% let x 5 %}` | `         ^` | Expected = |
| `testLetMissingValue` | `{% let x = %}` | `           ^^` | Unexpected token |
| `testLetIfExprWithoutElse` | `{% let x = if (true) { 1 } %}` | `           ^^^^^^^^^^^^^^^` | If expression must include else |
| `testLetIfExprWithElseIfNoElse` | `{% let x = if (false) { 1 } else if (true) { 2 } %}` | `           ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^` | If expression must include else |

#### Extends/Import 语句错误 (3个)

| 测试方法 | 模板 | 指针 | 错误消息 |
|----------|------|------|----------|
| `testExtendsMissingString` | `{% extends %}` | `           ^^` | Expected string |
| `testImportMissingIdentifier` | `{% import %}` | `          ^^` | Expected identifier |
| `testImportIncompleteDot` | `{% import java. %}` | `                ^^` | Expected identifier |

#### 表达式错误 (15个)

| 测试方法 | 模板 | 指针 | 错误消息 |
|----------|------|------|----------|
| `testBinaryMissingRightOperand` | `{x + }` | `     ^` | Unexpected token |
| `testTernaryMissingColon` | `{x ? y }` | `       ^` | Expected : |
| `testTernaryMissingTrueExpr` | `{x ? : y}` | `     ^` | Unexpected token |
| `testTernaryMissingFalseExpr` | `{x ? y : }` | `         ^` | Unexpected token |
| `testElvisMissingRightOperand` | `{x ?? }` | `      ^` | Unexpected token |
| `testMethodCallMissingRightParen` | `{func(}` | `      ^` | Unexpected token |
| `testMethodCallMissingComma` | `{func(x y)}` | `        ^` | Expected ) |
| `testMethodCallInvalidTarget` | `{5()}` | `  ^` | Invalid method call target |
| `testMemberAccessMissingIdentifier` | `{obj.}` | `     ^` | Expected identifier |
| `testSafeNavMissingIdentifier` | `{obj?.}` | `      ^` | Expected identifier |
| `testIndexAccessMissingRightBracket` | `{list[}` | `      ^` | Unexpected token |
| `testIndexAccessMissingIndex` | `{list[]}` | `      ^` | Unexpected token |
| `testParenMissingRightParen` | `{(x}` | `   ^` | Expected ) |
| `testParenEmpty` | `{()}` | `  ^` | Unexpected token |
| `testUnexpectedTokenInExpression` | `{% %}` | `   ^^` | Unexpected token |

#### 字面量错误 (6个)

| 测试方法 | 模板 | 指针 | 错误消息 |
|----------|------|------|----------|
| `testListMissingRightBracket` | `{[1, 2}` | `      ^` | Expected ] |
| `testListMissingComma` | `{[1 2]}` | `    ^` | Expected ] |
| `testHashMissingRightBrace` | `{{key: value}` | `             ^` | Unclosed expression |
| `testHashMissingColon` | `{{key value}}` | `      ^^^^^` | Expected } |
| `testHashInvalidKey` | `{{true: value}}` | `  ^^^^` | Invalid hash key |
| `testHashMissingValue` | `{{key: }}` | `       ^` | Unexpected token |

#### 结构化表达式错误 (9个)

| 测试方法 | 模板 | 指针 | 错误消息 |
|----------|------|------|----------|
| `testIfExprMissingLeftParen` | `{if true { 1 } else { 2 }}` | `    ^^^^` | Expected ( |
| `testIfExprMissingRightParen` | `{if (true { 1 } else { 2 }}` | `          ^` | Expected ) |
| `testIfExprMissingLeftBrace` | `{if (true) 1 else { 2 }}` | `           ^` | Expected { |
| `testIfExprMissingRightBrace` | `{if (true) { 1 else { 2 }}` | `               ^^^^` | Unexpected token |
| `testForExprMissingLeftParen` | `{for item in list { x } }` | `     ^^^^` | Expected ( |
| `testForExprMissingRightParen` | `{for (item in list { x } }` | `                       ^` | Expected : |
| `testForExprMissingLeftBrace` | `{for (item in list) x }` | `                    ^` | Expected { |
| `testForExprMissingRightBrace` | `{for (item in list) { x }` | `                         ^` | Unclosed expression |
| `testRangeMissingRightOperand` | `{1..}` | `    ^` | Unexpected token |

### ValidatorErrors (~8个测试)

#### ExportValidator (2个)

| 测试方法 | 模板 | 指针 | 错误消息 |
|----------|------|------|----------|
| `testDuplicateExport` | `{% export x %} {% export x %}` | `                  ^^^^^^^^` | Variable 'x' is already exported |
| `testDuplicateExportWithLet` | `{% export let x = 1 %}\n{% export let x = 2 %}` | `   ^^^^^^^^^^^^^^^^` | Variable 'x' is already exported |

#### ImportTemplateValidator (4个)

| 测试方法 | 模板 | 指针 | 错误消息 |
|----------|------|------|----------|
| `testImportTemplateAliasConflicts` | `{% let foo = 1 %}\n{% import 'empty.etl' as foo %}` | `   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^` | Alias 'foo' conflicts |
| `testImportTemplateAliasAlreadyUsed` | `{% import 'empty.etl' as lib %}\n{% import 'empty.etl' as lib %}` | `   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^` | Alias 'lib' conflicts |
| `testImportTemplateNotFound` | `{% import 'nonexistent.etl' as lib %}` | `   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^` | Cannot import template |
| `testImportTemplateParseFailure` | `{% import 'broken.etl' as lib %}` | `   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^` | Failed to parse imported template |

#### TemplateJavaImportValidator (1个)

| 测试方法 | 模板 | 指针 | 错误消息 |
|----------|------|------|----------|
| `testJavaImportDisabled` | `{% import java.util.List %}` | `   ^^^^^^^^^^^^^^^^^^^^^` | Template Java class import is disabled |

### EngineErrors (~5个测试)

| 测试方法 | 输入 | 预期异常 | 错误消息 |
|----------|------|----------|----------|
| `testNullTemplateString` | `null` | TemplateCompileException | Template string cannot be null |
| `testNullInputStream` | `null` | TemplateCompileException | InputStream cannot be null |
| `testNullFile` | `null` | TemplateCompileException | File cannot be null |
| `testFileNotFound` | `new File("nonexistent.etl")` | TemplateCompileException | Template file does not exist |
| `testPathIsDirectory` | `new File("src/test/resources")` | TemplateCompileException | Path is not a file |

**注意**: `testFileNotReadable` 测试因平台依赖性（Windows 文件权限差异）被移除。

### NestedErrors (~5个测试)

| 测试方法 | 模板 | 指针 | 错误消息 |
|----------|------|------|----------|
| `testNestedIfMissingEnd` | `{% if true %}{% if false %}content{% endif %}` | `                                             ^` | Expected {/% |
| `testNestedForMissingEnd` | `{% for x in list %}{% for y in list %}content{% endfor %}` | `                                                         ^` | Expected {/% |
| `testMixedNestedMissingEnd` | `{% if true %}{% for x in list %}content{% endfor %}` | `                                                   ^` | Expected {/% |
| `testMultilineErrorLine1` | `line1\nline2\n{x + }` | `      ^` (line 3) | Unexpected token |
| `testMultilineErrorLocation` | `Hello\n{% if true %}\ncontent` | `                    ^` (line 3) | Expected {/% |

## 文件位置

```
src/test/java/org/dromara/easytl/CompileErrorTest.java
```

## 测试资源

ImportTemplateValidator 测试需要以下资源文件（已存在于 CompilerValidatorLocationTest 中）：

- `src/test/resources/templates/empty.etl` — 空模板
- `src/test/resources/templates/broken.etl` — 包含语法错误的模板

## 实现步骤

1. 创建 `CompileErrorTest.java` 文件
2. 添加 `@BeforeEach` 初始化方法
3. 添加辅助方法 `assertCompileError`
4. 按类别添加嵌套测试类和测试方法
5. 运行 `mvn test -Dtest=CompileErrorTest` 验证所有测试通过

## 异常类型映射

| 错误类别 | 异常类型 | 来源 |
|----------|----------|------|
| 语法错误 | `TemplateSyntaxException` | Lexer/Parser |
| 验证器错误 | `TemplateCompileException` | Validators |
| 引擎错误 | `TemplateCompileException` | TemplateEngine |

## 测试数量估算

- SyntaxErrors: ~48 个测试 (表达式包装器 3 + if 7 + for 8 + switch 6 + let 5 + extends/import 3 + 表达式 15 + 字面量 6 + 结构化表达式 9)
- ValidatorErrors: ~7 个测试 (export 2 + import template 4 + java import 1)
- EngineErrors: ~5 个测试
- NestedErrors: ~5 个测试

**总计: ~65 个测试**

## 注意事项

1. 所有测试通过 `engine.compile(...)` API，验证完整编译管道
2. 使用 `ErrorPointerAssert.assertErrorPointer` 校验错误位置
3. Validator 测试需要配置特定的 TemplateEngine（带 loader 或禁用 Java import）
4. Engine 错误测试不验证位置信息（这些错误无源码位置）
5. 测试类独立于 SyntaxErrorTest，不修改现有测试类