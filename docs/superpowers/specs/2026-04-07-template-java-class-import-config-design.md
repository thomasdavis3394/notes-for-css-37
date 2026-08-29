# 模板 Java 类 import 开关设计

## 概述

在 `TemplateEngineConfig` 及现有构建器链上增加配置项 **`templateJavaClassImportEnabled`**（默认 `true`）。当为 `false` 时，**禁止在模板源码中使用针对 Java 类的 `{% import %}` 语句**（当前语法与 AST：`ImportStatement`），在编译期（及 `{% extends %}` 所加载子模板的等价校验点）报错。  
**不限制**将来支持的 **模板文件 import**（例如 `{% import '../scripts/xxx.etl' as lib %}`）。  
**不限制** Java 代码中的 **`Context.importClass(...)`**。

**说明**：当前实现仅存在 Java 类 import；模板文件 import 尚未实现。本 spec 约定未来引入模板 import 时须使用 **与 `ImportStatement` 可区分的语法与 AST**（推荐独立节点如 `TemplateImportStatement`），校验器仅拦截 Java 类 import 节点。

## 功能需求

### 1. 配置项

| 位置 | 行为 |
|------|------|
| `TemplateEngine.TemplateEngineConfig` | 字段 + getter + `setTemplateJavaClassImportEnabled(boolean)`（链式），默认 `true` |
| `EngineBuilder` | 链式 `templateJavaClassImportEnabled(boolean)`，写入上述 config |
| `ConfigBuilder` | 同上，与 `strictMode` 等并列 |
| `TemplateBuilder` | 链式 `templateJavaClassImportEnabled(boolean)`；在 `getOrCreateEngine()` 经 `EngineBuilder` 传入。若用户已调用 `engine(TemplateEngine)`，**以该引擎内已有 `TemplateEngineConfig` 为准**，`TemplateBuilder` 上的本项**不覆盖**外部引擎（须在 Javadoc 写明） |

### 2. 校验规则

- **`templateJavaClassImportEnabled == true`**：行为与现状一致（允许模板内 Java 类 `{% import %}`）。
- **`templateJavaClassImportEnabled == false`**：
  - 在 AST 中每出现一个 **Java 类 import** 节点即失败。
  - 异常类型：**`TemplateCompileException`**，信息应提示可使用 **`Context.importClass(...)`** 作为替代。

### 3. 校验调用点（方案 B）

抽取专用校验逻辑（例如 `TemplateJavaImportValidator` 或 `compiler` 包内 package-private 工具类），避免重复实现：

1. **`TemplateEngine.compileInternal`**：`Parser.parse()` 得到根 `TemplateNode` 之后、`InterpreterCompiler.compile` 之前调用。
2. **`Evaluator.visitExtendsStatement`**：加载子模板字符串并 `parse()` 得到 `externalAst` 之后、**`externalAst.accept(evaluator)` 之前**调用，使用**与编译根模板时相同的** `templateJavaClassImportEnabled`（该标志需从 `TemplateEngine` 配置传递到 `InterpreterTemplate`，再在 `render` 创建 `Evaluator` 时传入）。

**当前代码库**：仅 `ImportStatement` 表示 Java 类 import，校验器对该节点类型在 `false` 时报错即可。  
**未来**：模板文件 import 使用独立 AST 节点时，校验器**不得**对其报错。

### 4. 与 `Context.importClass` 的关系

- `Context.importClass(Class)` / `importClass(Class, String)` **始终允许**，与 `templateJavaClassImportEnabled` 无关。

### 5. 模板缓存

- 标志为 `TemplateEngine` 实例配置的一部分；缓存仍以源码等为键（与 `strictMode` 等同引擎内固定配置一致），**无需**将该布尔值单独拼入缓存 key。

## 非目标

- 不在本 spec 中实现 **模板文件 import** 的解析与执行；仅约定其 **不受** `templateJavaClassImportEnabled` 约束。
- 不在 `Context` 上增加本开关（已否决）。

## 测试要点

- `templateJavaClassImportEnabled == false`：根模板含 `{% import java.lang.Math %}`（或带 `as`）→ `compile` 抛 `TemplateCompileException`。
- 同上为 false：父模板无 import，**extends 的子模板**含 Java 类 import → 首次执行 extends 路径时失败（`TemplateCompileException` 经现有 extends 错误包装链体现）。
- `true`：现有 import 相关测试行为不变。
- `false`：`Context.importClass` + 模板使用已导入类 → 仍成功（需用例覆盖）。

## 命名与默认值

- 配置名：**`templateJavaClassImportEnabled`**，默认 **`true`**（向后兼容）。
