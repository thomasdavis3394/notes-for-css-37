---
name: compile-error-location-tests
description: Consolidate and enhance compile-time error location tests
type: project
---

# 编译期错误位置测试重构设计

## 背景

当前编译期错误位置测试分散在多个测试类中，且部分测试未使用 `ErrorPointerAssert.assertErrorPointer` 验证错误定位信息的准确性。

## 目标

1. 将 `ExportValidatorLocationTest` 重命名为 `CompilerValidatorLocationTest`
2. 添加 ImportTemplateValidator 和 TemplateJavaImportValidator 的位置测试
3. 重构 `SyntaxErrorTest`，使所有测试用例都使用 `assertErrorPointer` 验证位置信息

## 文件变更

### 重命名

| 原文件 | 新文件 |
|--------|--------|
| `ExportValidatorLocationTest.java` | `CompilerValidatorLocationTest.java` |

### 删除

| 文件 | 原因 |
|------|------|
| `TemplateJavaImportValidatorTest.java` | 合并到 CompilerValidatorLocationTest |

### 重构

| 文件 | 变更内容 |
|------|----------|
| `SyntaxErrorTest.java` | 所有测试改为使用 `assertSyntaxErrorWithPointer` |

## 测试用例清单

### CompilerValidatorLocationTest

#### ExportValidator（3个，从原文件迁移）

| 测试方法 | 模板 | 指针 | 错误消息 |
|----------|------|------|----------|
| `duplicateExport_simple` | `{% export x %} {% export x %}` | `^^^^^^^^` (export x) | Variable 'x' is already exported |
| `duplicateExport_withLet` | `{% export let x = 1 %}\n{% export let x = 2 %}` | `^^^^^^^^^^^^^^^^^` (export let x = 2) | Variable 'x' is already exported |
| `duplicateExport_multiline` | `Hello {% export name %}\nWorld {% export name %}` | `^^^^^^^^^^^` (export name) | Variable 'name' is already exported |

#### ImportTemplateValidator（4个，新增）

| 测试方法 | 模板 | 指针 | 错误消息 |
|----------|------|------|----------|
| `importTemplate_aliasConflictsWithVariable` | `{% let foo = 1 %}\n{% import 'lib.etl' as foo %}` | `^^^` (foo 别名) | Alias 'foo' conflicts with an existing variable |
| `importTemplate_aliasAlreadyUsed` | `{% import 'a.etl' as lib %}\n{% import 'b.etl' as lib %}` | `^^^` (lib 别名) | Alias 'lib' is already used for another import |
| `importTemplate_templateNotFound` | `{% import 'nonexistent.etl' as lib %}` | `^^^^^^^^^^^^^^^^^^` (import 'nonexistent.etl' as lib) | Cannot import template 'nonexistent.etl' |
| `importTemplate_parseFailure` | `{% import 'broken.etl' as lib %}` | `^^^^^^^^^^^^^^^^^^^^` (import 'broken.etl' as lib) | Cannot import template 'broken.etl' |

**注意**: ImportTemplateValidator 测试需要模板加载器支持。

**测试模板文件创建方式：**

在 `src/test/resources/templates/` 目录下创建测试模板：
- `broken.etl` — 包含语法错误的模板（如 `{x + }`）
- 用于 `importTemplate_parseFailure` 测试

对于 `importTemplate_aliasConflictsWithVariable` 和 `importTemplate_aliasAlreadyUsed` 测试，需要一个有效的模板文件（可以是空模板）。

对于 `importTemplate_templateNotFound` 测试，直接使用不存在的路径即可。

#### TemplateJavaImportValidator（1个，迁移并完善）

| 测试方法 | 模板 | 指针 | 错误消息 |
|----------|------|------|----------|
| `javaImport_disabled` | `{% import java.util.List %}` | `^^^^^^^^^^^^^^^^^^^^` (import java.util.List) | Template Java class import is disabled |

### SyntaxErrorTest

重构原则：
- 删除 `assertSyntaxError(String input, String expectedMessageContains)` 辅助方法
- 保留 `assertSyntaxErrorWithPointer` 辅助方法
- 所有测试方法改为使用 `assertSyntaxErrorWithPointer`

#### 测试方法分类

**已有位置验证的测试（保持不变）：**
- `testIfStatementMissingCondition`
- `testForStatementMissingIdentifier`
- `testSwitchStatementMissingExpression`
- `testLetStatementMissingIdentifier`
- `testBinaryExpressionMissingRightOperand`
- `testTernaryExpressionMissingColon`
- `testMethodCallMissingRightParen`
- `testMemberAccessMissingIdentifier`
- `testIndexAccessMissingRightBracket`

**需要添加位置验证的测试：**
- 表达式包装器错误（3个）：`testSingleBraceExprMissingEnd`, `testDoubleBraceExprMissingEnd`, `testDollarBraceExprMissingEnd`
- If 语句块错误（7个）：`testIfStatementMissingRightBrace`, `testIfStatementMissingEndTag`, `testIfStatementWrongEndTag`, `testElseIfMissingCondition`, `testElseIfMissingRightBrace`, `testElseMissingRightBrace`
- For 语句块错误（8个）
- Switch 语句块错误（6个）
- Let 语句错误（5个，不含已有位置验证的）
- Import 语句错误（3个）
- 表达式错误（约15个）
- 字面量错误（6个）
- 结构化表达式错误（约15个）
- 其他错误（约10个）

**无需改造的测试：**
- `testEmptyTemplate` — 合法模板，不抛异常
- `testOnlyText` — 合法模板，不抛异常
- `testSyntaxErrorLineAndColumn` — 已有位置验证逻辑

## 实现步骤

### 第一步：重命名测试类

1. 将 `ExportValidatorLocationTest.java` 重命名为 `CompilerValidatorLocationTest.java`
2. 更新类名和导入

### 第二步：重构 SyntaxErrorTest

1. 删除 `assertSyntaxError(String input, String expectedMessageContains)` 方法
2. 逐个测试方法改为使用 `assertSyntaxErrorWithPointer`
3. 对于每个错误场景，确定正确的指针位置

### 第三步：添加 ImportTemplateValidator 测试

1. 在 `CompilerValidatorLocationTest` 中添加 ImportTemplateValidator 测试方法
2. 创建测试用的模板文件（如 `broken.etl`）或使用 Mock

### 第四步：迁移 TemplateJavaImportValidator 测试

1. 将 `TemplateJavaImportValidatorTest` 中的测试迁移到 `CompilerValidatorLocationTest`
2. 使用 `assertErrorPointer` 验证位置信息
3. 删除原 `TemplateJavaImportValidatorTest.java`

### 第五步：验证

1. 运行 `mvn test` 确保所有测试通过
2. 验证每个测试的错误指针位置正确

## 注意事项

1. ImportTemplateValidator 测试需要模板加载器支持，可使用内存中的模板加载器或测试资源文件
2. 部分语法错误的位置信息可能需要调整 Parser/Lexer 代码才能准确
3. 保持测试类之间的职责分离：SyntaxErrorTest 测试 Parser/Lexer 错误，CompilerValidatorLocationTest 测试 Validator 错误