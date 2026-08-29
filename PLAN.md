# EasyTL 开发计划

## 项目概述

EasyTL 是一个功能丰富的轻量级字符串模板引擎，支持类 JavaScript 的表达式语法、控制流语句（if/for/switch）、高级特性（空安全操作符、Elvis 表达式、正则匹配、区间字面量、哈希表字面量等）。

## 📊 项目进度总览（更新时间：2026年1月23日）

### 整体进度
- **已完成阶段**：7 / 10（70%）
- **总测试数**：300+ 个（持续增加中）
- **测试通过率**：100%

### 各阶段状态
| 阶段 | 名称 | 状态 | 测试数 | 完成时间 |
|------|------|------|--------|----------|
| 第一阶段 | 基础框架搭建 | ✅ 已完成 | 18 | 2025-11-09 |
| 第二阶段 | 词法分析器 | ✅ 已完成 | 92 | 2025-11-10 |
| 第三阶段 | 抽象语法树（AST） | ✅ 已完成 | 94 | 2025-11-10 |
| 第四阶段 | BNF 文法构建 | ✅ 已完成 | 0 | 2025-11-10 |
| 第五阶段 | 语法解析器 | ✅ 已完成 | 100+ | 2025-11-XX |
| 第六阶段 | 编译器 | ✅ 已完成 | 50+ | 2025-11-XX |
| 第七阶段 | 运行时执行 | ✅ 已完成 | 部分 | 2025-11-XX |
| 第八阶段 | 集成与优化 | ⏳ 进行中 | - | - |
| 第九阶段 | 测试 | ⏳ 进行中 | - | - |
| 第十阶段 | 文档与示例 | ⏳ 待开始 | 0 | - |

### 代码统计
- **源代码文件**：60+ 个
  - 核心类：TemplateEngine, Template, Context
  - 异常类：5 个
  - Parser 类：9 个（Lexer, Token, TokenType, Source, Parser等）
  - AST 类：34 个（包括 HashLiteral、三种表达式节点类型）
  - 编译器类：4 个（Compiler, Evaluator, InterpreterCompiler, InterpreterTemplate）
  - 运行时类：3 个（OperatorEvaluator, Range, ReflectionUtils）
- **测试文件**：11 个
  - ParserTest（语法解析器测试，约 2600+ 行）
  - CompilerIntegrationTest（编译器集成测试）
  - ASTNodeTest（AST 节点测试）
  - LexerTest, SourceTest, TokenStreamTest 等
- **文档文件**：8 个（README, PLAN, PHASE1_SUMMARY, PHASE2_SUMMARY, AST_TEST_REPORT, AST_TEST_SUMMARY, REFACTOR_SUMMARY, HASH_LITERAL_COMPLETION_SUMMARY）

### 下一步工作
🎯 **当前任务**：完善集成与测试
- **集成与优化（第八阶段）**：
  - 完善 TemplateEngine 集成（Lexer → Parser → Compiler → Template）
  - 性能优化（反射缓存、对象池等）
  - 错误处理增强（详细错误信息、调试模式）
- **测试完善（第九阶段）**：
  - 补充运行时执行测试（OperatorEvaluator, Range, ReflectionUtils）
  - 端到端集成测试
  - 真实场景测试（邮件模板、HTML模板、SQL模板等）
- **文档与示例（第十阶段）**：
  - API 文档完善（JavaDoc）
  - 使用指南和最佳实践
  - 示例代码库

## 开发周期

预计开发周期：**29-36 天**

由于功能需求较为丰富，相比初始规划增加了以下功能：
- 字符串模板字面量
- 列表字面量（支持混合类型、嵌套列表）
- 哈希表字面量（支持标识符键、字符串键、数字键、嵌套哈希表）
- 区间字面量（四种类型）
- 大数运算支持（BigInteger、BigDecimal）
- 空安全操作符和 Elvis 表达式
- 正则表达式支持（字面量和匹配运算符）
- 控制流语句（if/else、for、switch）
- 条件表达式（if 表达式，可作为值使用）
- for 循环语句（for 表达式，用于执行副作用操作）
- 自定义命令语句块
- 嵌入式代码块（let、extends、import）
- !in 运算符（不包含判断）

## 语法设计说明

### 语句块类型

模板引擎支持两种类型的语句块：

#### 1. 标准语句块（Standard Statement Block）

**特点：**
- 有开始标签和结束标签
- 开始标签以 `{%` 开始，以 `}` 结尾（不是 `%}` 结尾）
- 结束标签以 `{/%` 开始，以 `}` 结尾（不是 `%}` 结尾）
- 开始和结束之间可以插入普通文本和其他语句块
- 用于流程控制和内容包裹

**语法格式：**
```
{% 语句名称 语句参数 }
    文本内容和其他语句块
{/% 语句名称 }
```

**应用场景：**
- 条件语句：`{% if ... }...{/% if }`
- 循环语句：`{% for ... }...{/% for }`
- Switch 语句：`{% switch ... }...{/% switch }`

**示例：**
```
{% if user.age >= 18 }
成年人
{% else }
未成年人
{/% if }
```

#### 2. 自闭合语句块（Self-Closing Statement Block）

**特点：**
- 没有结束标签
- 不能在中间插入文本和其他语句块
- 用于执行单条或多条脚本语句

**语法格式：**

单条语句：
```
{% 语句 %}
```

多条语句（换行或分号分隔）：
```
{%
    语句1
    语句2
    语句3
%}
```

或：
```
{% 语句1; 语句2; 语句3 %}
```

**应用场景：**
- 变量声明：`{% let name = 'value' %}`
- 模板扩展：`{% extends 'template.etl' %}`
- 类导入：`{% import java.util.List %}`
- case 分支：`{% case 1 }`（在 switch 内部）
- else 分支：`{% else }`（在 if 内部）
- else if 分支：`{% else if condition }`（在 if 内部）
- default 分支：`{% default }`（在 switch 内部）

**示例：**
```
{% let a = 1 %}
{%
    let b = 2
    let c = a + b
%}
{% import java.util.ArrayList %}
```

### 语法区分要点

1. **标准语句块：**
   - 开始标签：以 `{%` 开始，以 `}` 结尾（不是 `%}` 结尾）
   - 结束标签：以 `{/%` 开始，以 `}` 结尾
   - 格式：`{% 语句名称 参数 }...{/% 语句名称 }`
   - 适用于：if、for、switch 等流程控制语句

2. **自闭合语句块：**
   - 以 `{%` 开始，以 `%}` 结尾
   - 格式：`{% 语句 %}` 或 `{% 语句1; 语句2; ... %}`
   - 适用于：let、extends、import 等单行或多行脚本语句

3. **判断标准还是自闭合的依据：**
   - 如果 `{%` 后跟语句，最后以 `}` 结尾（非 `%}`），说明是标准语句块的开始标签
   - 如果 `{%` 后跟语句，最后以 `%}` 结尾，说明是自闭合语句块
   - 如果看到 `{/%`，说明是标准语句块的结束标签

4. **特殊的自闭合语句（在标准语句块内部使用）：**
   - `{% else }`：在 if 语句块内部，连接 if 和 else 分支
   - `{% else if 条件 }`：在 if 语句块内部，添加额外的条件分支
   - `{% case 值 }`：在 switch 语句块内部，定义匹配分支
   - `{% default }`：在 switch 语句块内部，定义默认分支

## 功能特性清单

基于 README.md 需求文档，1.0 版本需要实现以下功能特性：

### 核心特性
- [ ] 纯文本支持
- [ ] 表达式嵌入（单花括号 `{}`、双花括号 `{{}}`、美元符号 `${}`）
- [ ] 类 JavaScript 表达式语法

### 字面量
- [ ] 整数字面量（int、long、BigInteger）
- [ ] 浮点数字面量（double、科学计数法、BigDecimal）
- [ ] 字符串字面量（单引号、双引号、转义字符）
- [ ] 字符串模板字面量（反引号，支持嵌入表达式）
- [ ] 区间字面量（四种类型：全开、全闭、前开后闭、前闭后开）
- [ ] 列表字面量（`[1, 2, 3]`，支持混合类型、嵌套列表）
- [ ] 哈希表字面量（`{key: value}`，支持标识符键、字符串键、数字键、嵌套哈希表）
- [ ] 正则表达式字面量（`/pattern/flags`）
- [ ] 布尔字面量（true、false）
- [ ] 空值字面量（null）

### 表达式语法支持
- [x] 三种表达式语法格式：
  - [x] 单花括号语法 `{表达式}` → 生成 `SINGLE_BRACE_EXPR_BEGIN/END` Token 和 `SingleBraceExpressionNode`
  - [x] 双花括号语法 `{{表达式}}` → 生成 `DOUBLE_BRACE_EXPR_BEGIN/END` Token 和 `DoubleBraceExpressionNode`
  - [x] 美元符号语法 `${表达式}` → 生成 `DOLLAR_BRACE_EXPR_BEGIN/END` Token 和 `DollarBraceExpressionNode`
- [x] 三种语法功能等价，但Token类型和ASTNode类型不同，便于调试和分析

### 运算符
- [ ] 算术运算（+、-、*、/、%）
- [ ] 比较运算（>、<、>=、<=、==、!=）
- [ ] 逻辑运算（&&、||、!）
- [ ] 三元运算符（? :）
- [ ] 空安全操作符（?.）
- [ ] Elvis 运算符（?:）
- [ ] 正则匹配运算符（=~）
- [ ] 正则不匹配运算符（!=~）
- [ ] 范围检查运算符（in、!in）

### 条件表达式（if 表达式）
- [ ] 基本 if 表达式（`if (条件) {语句块}`）
- [ ] if-else 表达式（`if (条件) {语句块1} else {语句块2}`）
- [ ] if-else if-else 表达式（支持多个 else if 分支）
- [ ] 语句块支持（花括号包裹，分号或换行符分隔）
- [ ] 语句块返回值（最后一条语句的返回值作为整个语句块的返回值）
- [ ] 在自闭合语句块中使用（如 `{% let name = if (a == 1) {"A"} else {"B"} %}`）
- [ ] 条件表达式可用于赋值、作为参数等场景
- [ ] **赋值约束**：当 if 表达式用于赋值（= 右边）时，必须包含 else 语句块，否则编译报错

### for 循环语句
- [ ] 基本 for 循环语句（`for (变量 in 表达式) {语句块}`）
- [ ] 带索引的 for 循环语句（`for (索引变量, 值变量 in 表达式) {语句块}`）
- [ ] 语句块支持（花括号包裹，分号或换行符分隔）
- [ ] 在自闭合语句块中使用（如 `{% for (user in userList) { names.add(user.name) } %}`）
- [ ] 循环变量作用域管理（仅限循环体内）
- [ ] 支持遍历集合（实现 Iterable 接口）
- [ ] 支持遍历区间字面量
- [ ] 支持嵌套 for 循环语句
- [ ] for 循环语句返回 null

### 表达式功能
- [ ] 变量访问
- [ ] 对象属性访问（user.name）
- [ ] 数组/列表索引访问（list[0]、map['key']）
- [ ] 方法调用
- [ ] 链式调用
- [ ] 空安全链式调用

### 控制流语句
- [ ] if 语句
- [ ] if-else 语句
- [ ] if-else if-else 语句
- [ ] for 循环（遍历集合）
- [ ] for 循环（遍历区间）
- [ ] for 循环（带索引）
- [ ] switch 语句
- [ ] switch-case-default 语句
- [ ] 嵌套语句块

### 扩展功能
- [ ] 嵌入式代码块（`{% ... %}`）
  - 变量声明和赋值（`let`）
  - 模板扩展（`extends`）
  - Java 类导入（`import`）
  - 多行代码块支持

## 开发阶段

### 第一阶段：基础框架搭建（第 1-2 天）✅ 已完成

#### 1.1 项目初始化

- [x] 创建 Maven 项目结构
- [x] 配置 `pom.xml`
  - Java 8 编译配置
  - JUnit 5 测试依赖
  - Maven Compiler Plugin
  - Maven Surefire Plugin
- [x] 创建包结构
  - `org.dromara.easytl` - 主包
  - `org.dromara.easytl.exception` - 异常类
  - `org.dromara.easytl.parser` - 解析器
  - `org.dromara.easytl.ast` - 抽象语法树
  - `org.dromara.easytl.compiler` - 编译器
  - `org.dromara.easytl.runtime` - 运行时

#### 1.2 核心接口设计

- [x] 创建 `TemplateEngine` 类
  - `compile(String template)` 方法
  - 配置选项（单/双花括号支持）
- [x] 创建 `Template` 接口
  - `render(Context context)` 方法
- [x] 创建 `Context` 类
  - `put(String name, Object value)` 方法
  - `get(String name)` 方法
  - 支持嵌套作用域

#### 1.3 异常体系

- [x] `TemplateException` - 基础异常类
- [x] `TemplateSyntaxException` - 语法错误异常
- [x] `TemplateRuntimeException` - 运行时异常
- [x] `TemplateCompileException` - 编译异常

---

### 第二阶段：词法分析器（第 3-6 天）✅ 已完成

#### 2.1 Source 类实现

创建 `Source` 类，作为模板源文件的抽象和输入流管理：

- [x] `Source` 接口定义
  - `read()` 方法 - 读取下一个字符
  - `peek()` 方法 - 预览下一个字符
  - `peek(int offset)` 方法 - 预览指定偏移位置的字符
  - `getPosition()` 方法 - 获取当前读取位置
  - `getLine()` 方法 - 获取当前行号
  - `getColumn()` 方法 - 获取当前列号
  - `mark()` 方法 - 标记当前位置
  - `reset()` 方法 - 重置到标记位置
- [x] `StringSource` 类
  - 接受字符串作为输入
  - 直接从字符串读取字符
  - 适用于小型模板
- [x] `StreamSource` 类
  - 接受 InputStream 作为输入
  - 使用缓冲区读取流内容
  - 缓冲区大小可配置（默认 8KB）
  - 实现滑动窗口机制
- [x] `FileSource` 类
  - 接受 File 对象作为输入
  - 内部使用 FileInputStream
  - 支持字符编码指定
- [x] 缓冲区管理策略
  - 环形缓冲区设计
  - 缓冲区阈值配置（默认 16KB）
  - 自动清理已读取的旧内容
  - 保留足够的回溯空间（用于 peek 和 reset）
- [x] 位置信息跟踪
  - 字符位置（绝对位置）
  - 行号（从 1 开始）
  - 列号（从 1 开始）
  - 换行符处理（\n、\r\n、\r）
- [x] 异常处理
  - IO 异常封装
  - 编码错误处理

#### 2.2 Token 定义

创建 `Token` 类（使用 int 类型魔数表示 TokenType）：

- [x] **文本类型**
  - `TEXT` - 普通文本内容
- [x] **分隔符**
  - `LEFT_BRACE` - `{`
  - `RIGHT_BRACE` - `}`
  - `LEFT_PAREN` - `(`
  - `RIGHT_PAREN` - `)`
  - `LEFT_BRACKET` - `[`
  - `RIGHT_BRACKET` - `]`
  - `COMMA` - `,`
  - `DOT` - `.`
  - `SEMICOLON` - `;`
  - `BACKTICK` - `` ` `` - 反引号（字符串模板）
  - `SLASH` - `/` - 斜杠（正则表达式）
  - `DOLLAR_SIGN` - `$` - 美元符号（用于 ${} 表达式）
- [x] **表达式标记**
  - `SINGLE_BRACE_TOKEN` - 单花括号表达式 `{表达式}`
  - `DOUBLE_BRACE_TOKEN` - 双花括号表达式 `{{表达式}}`
  - `DOLLAR_BRACE_TOKEN` - 美元符号表达式 `${表达式}`
- [x] **运算符**
  - `PLUS` - `+`
  - `MINUS` - `-`
  - `MULTIPLY` - `*`
  - `DIVIDE` - `/`
  - `MODULO` - `%`
  - `GT` - `>`
  - `LT` - `<`
  - `GTE` - `>=`
  - `LTE` - `<=`
  - `EQ` - `==`
  - `NEQ` - `!=`
  - `AND` - `&&`
  - `OR` - `||`
  - `NOT` - `!`
  - `QUESTION` - `?`
  - `COLON` - `:`
  - `SAFE_NAV` - `?.` - 空安全操作符
  - `ELVIS` - `?:` - Elvis 运算符
  - `REGEX_MATCH` - `=~` - 正则匹配
  - `REGEX_NOT_MATCH` - `!=~` - 正则不匹配
  - `RANGE_OPEN` - `..` - 全开区间
  - `RANGE_CLOSED` - `>..<` - 全闭区间
  - `RANGE_LEFT_OPEN` - `..<` - 前开后闭
  - `RANGE_RIGHT_OPEN` - `>..` - 前闭后开
- [x] **字面量**
  - `IDENTIFIER` - 标识符
  - `STRING` - 字符串字面量（单引号、双引号）
  - `TEMPLATE_STRING` - 字符串模板字面量（反引号）
  - `NUMBER` - 数值字面量
  - `LONG` - 长整数字面量（L 结尾）
  - `BIG_INTEGER` - 大整数字面量
  - `BIG_DECIMAL` - 高精度小数字面量
  - `REGEX` - 正则表达式字面量
  - `TRUE` - `true`
  - `FALSE` - `false`
  - `NULL` - `null`
- [x] **关键字**
  - `IF` - `if`
  - `ELSE` - `else`
  - `FOR` - `for`
  - `IN` - `in`
  - `NOT_IN` - `!in` - 不包含运算符
  - `SWITCH` - `switch`
  - `CASE` - `case`
  - `DEFAULT` - `default`
  - `LET` - `let` - 变量声明
  - `EXTENDS` - `extends` - 模板扩展
  - `IMPORT` - `import` - Java 类导入
- [x] **语句块标记**
  - `STMT_BEGIN` - `{%` - 语句块开始（标准语句块和自闭合语句块共用）
  - `STMT_END` - `%}` - 自闭合语句块结束
  - `STMT_CLOSE` - `{/%` - 标准语句块结束标签开始
  - 注：标准语句块开始标签以 `}` 结尾（使用 `RIGHT_BRACE` token）
  - 注：标准语句块结束标签格式为 `{/% 语句名称 }`，最后的 `}` 也使用 `RIGHT_BRACE` token
- [x] **特殊**
  - `EOF` - 文件结束

#### 2.3 TokenStream 类实现

创建 `TokenStream` 类，作为 Token 流的抽象：

- [x] `TokenStream` 类定义
  - `nextToken()` 方法 - 读取下一个 Token
  - `peek()` 方法 - 预览下一个 Token（不消费）
  - `peek(int offset)` 方法 - 预览指定偏移位置的 Token
  - `hasNext()` 方法 - 判断是否还有更多 Token
  - `getPosition()` 方法 - 获取当前 Token 位置
- [x] 流式设计目的
  - 避免将所有 Token 都加载到内存中
  - 支持大型模板文件的词法分析
  - 按需生成 Token，减少内存占用
  - 支持 Token 预览和回溯
- [x] 内部缓冲区管理
  - 维护小型 Token 缓冲区（默认缓冲 3 个 Token）
  - 支持有限的回溯能力
  - 按需从 Lexer 读取新 Token

#### 2.4 词法分析器实现

创建 `Lexer` 类：

- [x] 构造函数接收 `Source` 对象（而不是字符串）
- [x] `getTokenStream()` 方法 - 返回 `TokenStream` 对象
- [x] 内部 `nextToken()` 方法 - 供 TokenStream 调用，返回下一个 Token
- [x] 状态管理
  - 文本模式
  - 表达式模式（进入 `{`、`{{` 或 `${` 后）
  - 语句块模式（进入 `{%` 后）
- [x] 表达式语法识别
  - 单花括号表达式 `{表达式}`
  - 双花括号表达式 `{{表达式}}`
  - 美元符号表达式 `${表达式}`
- [x] 字符串解析
  - 单引号字符串
  - 双引号字符串
  - 支持转义字符（`\n`、`\t`、`\\`、`\'`、`\"`）
- [x] 字符串模板解析（反引号）
  - 识别嵌入的表达式 `{...}`
  - 保持模板结构用于后续解析
- [x] 数字解析
  - 整数（int）
  - 长整数（L 结尾）
  - 大整数（超出 long 范围）
  - 浮点数（double）
  - 科学计数法（e/E）
  - 高精度小数（BigDecimal）
- [x] 区间字面量解析
  - 全开区间 `..`
  - 全闭区间 `>..<`
  - 前开后闭 `..<`
  - 前闭后开 `>..`
- [x] 正则表达式解析
  - `/pattern/` 基本格式
  - `/pattern/flags` 带标志位（g、i、m）
  - 处理转义字符
- [x] 列表字面量解析
  - 识别方括号 `[` 和 `]`
  - 解析列表元素（逗号分隔）
  - 支持嵌套列表
  - 支持空列表 `[]`
- [x] 哈希表字面量 Token 支持（词法层面）
  - 识别花括号 `{` 和 `}`（在表达式上下文中）
  - 识别冒号 `:` 用于键值对分隔
  - 识别逗号 `,` 用于键值对分隔
  - 支持标识符、字符串、数字作为键的 Token
  - 注：哈希表字面量的实际解析在语法分析阶段（第四阶段）完成
- [x] 标识符和关键字解析
  - 识别关键字（if、else、for、in、switch、case、default、let、extends、import）
  - 识别 `!in` 运算符
  - 普通标识符
- [x] 运算符识别
  - 单字符运算符
  - 双字符运算符（`==`、`!=`、`>=`、`<=`、`&&`、`||`、`?.`、`?:`、`=~`、`..`）
  - 三字符运算符（`!=~`、`..<`、`>..`）
  - 四字符运算符（`>..<`）
- [x] 语句块标记识别
  - `{%` 语句块开始（标准语句块和自闭合语句块共用）
  - `%}` 自闭合语句块结束
  - `{/%` 标准语句块结束标签开始
  - 标准语句块开始标签格式：`{% 语句名称 参数 }`（以 `}` 结尾，不是 `%}`）
  - 标准语句块结束标签格式：`{/% 语句名称 }`（以 `}` 结尾）
  - 需要根据 `}` 或 `%}` 来区分标准语句块和自闭合语句块
- [x] 空白字符处理
- [x] 从 `Source` 对象读取字符
  - 使用 `Source.read()` 读取字符
  - 使用 `Source.peek()` 预览字符
  - 利用 `Source` 的位置信息进行错误报告

#### 2.5 词法分析器测试

- [x] 测试纯文本解析
- [x] 测试单花括号表达式
- [x] 测试双花括号表达式
- [x] 测试美元符号表达式 `${表达式}`
- [x] 测试字符串字面量
  - 单引号字符串
  - 双引号字符串
  - 转义字符

- [x] 测试 TokenStream 类
  - nextToken() 方法测试
  - peek() 方法测试
  - hasNext() 方法测试
  - Token 缓冲区管理测试
  - 回溯功能测试

- [x] 测试 Source 类
  - StringSource 读取测试
  - StreamSource 缓冲区管理测试
  - FileSource 文件读取测试
  - 位置信息跟踪测试
  - 缓冲区阈值和清理测试
  - 换行符处理测试
- [x] 测试单花括号表达式
- [x] 测试双花括号表达式
- [x] 测试字符串字面量
  - 单引号字符串
  - 双引号字符串
  - 转义字符
- [x] 测试字符串模板字面量（反引号）
- [x] 测试数值字面量
  - 整数
  - 长整数（L 结尾）
  - 大整数
  - 浮点数
  - 科学计数法
  - 高精度小数
- [x] 测试区间字面量
  - 全开区间 `..`
  - 全闭区间 `>..<`
  - 前开后闭 `..<`
  - 前闭后开 `>..`
- [x] 测试正则表达式字面量
  - 基本正则
  - 带标志位的正则
- [x] 测试列表字面量
  - 整数列表
  - 字符串列表
  - 混合类型列表
  - 嵌套列表
  - 空列表
- [x] 测试哈希表字面量 Token 序列
  - 标识符作为键
  - 字符串作为键
  - 数字作为键
  - 混合类型的键
  - 值为表达式
  - 嵌套哈希表
  - 空哈希表
- [x] 测试运算符
  - 基本运算符
  - 空安全操作符 `?.`
  - Elvis 运算符 `?:`
  - 正则匹配 `=~`
  - 正则不匹配 `!=~`
  - in 和 !in 运算符
- [x] 测试关键字识别
  - 控制流关键字（if、else、for、switch等）
  - 嵌入式代码块关键字（let、extends、import）
- [x] 测试语句块标记
- [x] 测试边界情况

---

### 第三阶段：抽象语法树（AST）（第 7-9 天）✅ 已完成

#### 3.1 AST 节点基类

- [x] `ASTNode` - 抽象基类
  - `accept(Visitor visitor)` 方法（访问者模式）

#### 3.2 字面量节点

- [x] `StringLiteral` - 字符串字面量
- [x] `TemplateLiteral` - 字符串模板字面量
  - 包含字符串部分和嵌入表达式的列表
- [x] `NumberLiteral` - 数值字面量
  - 支持 int、long、BigInteger、double、BigDecimal
- [x] `BooleanLiteral` - 布尔字面量
- [x] `NullLiteral` - null 字面量
- [x] `ListLiteral` - 列表字面量
  - 元素表达式列表
  - 支持混合类型
  - 支持嵌套列表
- [x] `HashLiteral` - 哈希表字面量
  - 键值对列表（Map.Entry<ASTNode, ASTNode>）
  - 键支持标识符、字符串、数字
  - 值支持任意表达式
  - 支持嵌套哈希表
  - 空哈希表 `{}`
- [x] `RangeLiteral` - 区间字面量
  - 起始值
  - 结束值
  - 区间类型（全开、全闭、前开后闭、前闭后开）
- [x] `RegexLiteral` - 正则表达式字面量
  - pattern 字符串
  - flags 标志位（g、i、m）

#### 3.3 变量和访问节点

- [x] `Identifier` - 标识符
- [x] `MemberAccess` - 成员访问（如 `user.name`）
  - 支持普通访问 `.`
  - 支持空安全访问 `?.`
- [x] `IndexAccess` - 索引访问（如 `list[0]`）

#### 3.4 调用节点

- [x] `MethodCall` - 方法调用
  - 方法名
  - 参数列表

#### 3.5 运算符节点

- [x] `BinaryExpression` - 二元表达式
  - 左操作数
  - 运算符（算术、比较、逻辑、正则匹配等）
  - 右操作数
- [x] `UnaryExpression` - 一元表达式
  - 运算符
  - 操作数
- [x] `TernaryExpression` - 三元表达式（`?:`）
  - 条件
  - true 分支
  - false 分支
- [x] `ElvisExpression` - Elvis 表达式（`?:`）
  - 左操作数
  - 右操作数（默认值）
- [x] `IfExpression` - 条件表达式（if 表达式）
  - 条件表达式
  - then 语句块（语句列表）
  - else if 分支列表（可选，每个分支包含条件和语句块）
  - else 语句块（可选，语句列表）
  - 返回值为被执行的语句块中最后一条语句的值
- [x] `ForExpression` - for 循环语句
  - 循环变量名
  - 索引变量名（可选）
  - 集合表达式
  - 循环体语句块（语句列表）
  - 返回值为 null

#### 3.6 模板节点

- [x] `TemplateNode` - 模板根节点
  - 子节点列表
- [x] `TextNode` - 文本节点
- [x] `ExpressionNode` - 表达式节点抽象基类
  - 支持三种表达式类型：
    - [x] `SingleBraceExpressionNode` - 单花括号表达式 `{表达式}`
    - [x] `DoubleBraceExpressionNode` - 双花括号表达式 `{{表达式}}`
    - [x] `DollarBraceExpressionNode` - 美元符号表达式 `${表达式}`

#### 3.7 语句块节点

- [x] `IfStatement` - 条件语句块
  - 条件表达式
  - then 分支（子节点列表）
  - else if 分支列表（可选）
  - else 分支（可选）
- [x] `ForStatement` - 循环语句块
  - 循环变量名
  - 索引变量名（可选）
  - 集合表达式
  - 循环体（子节点列表）
- [x] `SwitchStatement` - Switch 语句块
  - 条件表达式
  - case 分支列表
  - default 分支（可选）
- [x] `CaseClause` - Case 子句
  - case 值表达式
  - 子节点列表

#### 3.8 嵌入式代码块节点

- [x] `LetStatement` - 变量声明语句
  - 变量名
  - 赋值表达式
- [x] `ExtendsStatement` - 模板扩展语句
  - 模板文件路径
- [x] `ImportStatement` - Java 类导入语句
  - 类全限定名

---

### 第四阶段：BNF 文法构建（第 10 天）✅ 已完成

#### 4.1 BNF 文法文档编写

在开始语法解析器开发之前，需要先构建完整的 BNF（巴科斯-瑙尔范式）文法文档，明确语法规则和解析优先级。

- [x] 分析 README.md 中的语法规则
- [x] 结合 TokenType 和 Lexer 实现，梳理所有语法结构
- [x] 编写 BNF 文法文档（`BNF_GRAMMAR.md`）
  - 模板结构定义
  - 表达式语法定义（按优先级从低到高）
  - 字面量语法定义
  - 语句块语法定义（标准语句块和自闭合语句块）
  - 运算符优先级和结合性定义
- [x] 验证 BNF 文法的完整性和一致性
- [x] 标注语法歧义点和处理方式
- [x] 为后续 Parser 实现提供参考文档

**BNF 文法文档内容：**
- 模板根规则
- 表达式规则（按优先级分层）
- 字面量规则（字符串、数字、列表、哈希表、区间、正则表达式等）
- 语句块规则（if、for、switch、let、extends、import）
- 运算符规则（算术、比较、逻辑、空安全、Elvis、正则匹配、范围检查等）
- 注释说明和示例

---

### 第五阶段：语法解析器（第 11-16 天）✅ 已完成

#### 5.1 Parser 基础结构

创建 `Parser` 类：

- [x] 构造函数接收 `TokenStream` 对象（而不是 Token 列表）
- [x] `parse()` 方法 - 返回 AST 根节点
- [x] `current()` - 获取当前 Token（从 TokenStream 读取）
- [x] `peek()` - 预览下一个 Token（调用 TokenStream.peek()）
- [x] `peek(int offset)` - 预览指定偏移的 Token
- [x] `advance()` - 前进到下一个 Token（调用 TokenStream.nextToken()）
- [x] `expect(TokenType type)` - 期望特定类型的 Token
- [x] 参考 BNF_GRAMMAR.md 中的文法规则实现解析逻辑
- [x] 流式解析特点
  - 按需从 TokenStream 获取 Token
  - 无需预先加载所有 Token
  - 支持大型模板文件的解析

#### 5.2 表达式解析（递归下降）

按优先级从低到高实现：

- [x] `parseExpression()` - 解析表达式入口
- [x] `parseIfExpression()` - 条件表达式 `if (条件) {语句块} else {语句块}`
  - 识别 `if` 关键字
  - 解析条件表达式（括号包裹）
  - 解析语句块（花括号包裹）
  - 支持 else 分支
  - 支持 else if 分支（可多个）
  - 返回 IfExpression 节点
  - 在 IfExpression 节点中标记是否包含 else 分支（用于后续的赋值约束检查）
- [x] `parseForExpression()` - for 循环语句 `for (变量 in 表达式) {语句块}`
  - 识别 `for` 关键字
  - 解析左括号 `(`
  - 解析循环变量（支持单变量或索引+值变量）
  - 解析 `in` 关键字
  - 解析集合表达式或区间
  - 解析右括号 `)`
  - 解析循环体语句块（花括号包裹）
  - 返回 ForExpression 节点
- [x] `parseStatementBlock()` - 解析语句块（用于 if 表达式和 for 循环语句）
  - 解析花括号内的语句
  - 支持分号或换行符分隔
  - 返回语句列表
  - 最后一条语句的值作为语句块的返回值
- [x] `parseElvis()` - Elvis 表达式 `?:`（最低优先级）
- [x] `parseTernary()` - 三元运算符 `? :`
- [x] `parseLogicalOr()` - 逻辑或 `||`
- [x] `parseLogicalAnd()` - 逻辑与 `&&`
- [x] `parseEquality()` - 相等性 `==`, `!=`
- [x] `parseRegex()` - 正则匹配 `=~`, `!=~`
- [x] `parseRange()` - 范围检查 `in`
- [x] `parseComparison()` - 比较 `>`, `<`, `>=`, `<=`
- [x] `parseRangeLiteral()` - 区间字面量 `..`, `>..<`, `..<`, `>..`
- [x] `parseHashLiteral()` - 哈希表字面量
  - 解析花括号 `{}`
  - 解析键值对（支持标识符、字符串、数字作为键）
  - 解析冒号 `:`
  - 解析值表达式
  - 支持空哈希表 `{}`
  - 支持嵌套哈希表
  - 需要与表达式花括号和语句块花括号区分
- [x] `parseAdditive()` - 加减 `+`, `-`
- [x] `parseMultiplicative()` - 乘除模 `*`, `/`, `%`
- [x] `parseUnary()` - 一元运算 `!`, `-`, `+`
- [x] `parsePostfix()` - 后缀表达式
  - 成员访问 `.`
  - 空安全访问 `?.`
  - 索引访问 `[]`
  - 方法调用 `()`
- [x] `parsePrimary()` - 基本表达式
  - 字面量（数字、字符串、布尔、null）
  - 字符串模板（反引号）
  - 列表字面量（方括号）
  - 哈希表字面量（花括号，需要与表达式花括号区分）
  - 正则表达式（斜杠）
  - 标识符
  - 括号表达式
  - 条件表达式（if 表达式，在这里调用 parseIfExpression）
  - for 循环语句（在这里调用 parseForExpression）

#### 5.3 模板解析

- [x] `parseTemplate()` - 解析整个模板
  - 识别文本部分
  - 识别表达式部分（`{...}`、`{{...}}` 或 `${...}`）
  - 识别语句块部分（`{% ... %}`）
  - 构建 TemplateNode
  - 支持三种表达式语法格式的区分和解析

#### 5.4 语句块解析

语句块分为两种：
1. **标准语句块**：有结束标签，格式为 `{% 语句名称 参数 }...{/% 语句名称 }`
2. **自闭合语句块**：无结束标签，格式为 `{% 语句 %}` 或 `{% 语句1; 语句2 %}`

- [x] `parseStatementBlock()` - 解析语句块
  - 识别 `{%` 开始标记
  - 判断语句块类型（标准或自闭合）
  - 路由到具体的解析方法
  
##### 标准语句块解析（有结束标签）

- [x] `parseIfStatement()` - 解析 if 语句
  - 格式：`{% if 条件 }...{% else }...{% else if 条件 }...{/% if }`
  - 解析 if 条件和内容
  - 解析 else if 分支（可选，可多个，格式 `{% else if 条件 }`）
  - 解析 else 分支（可选，格式 `{% else }`）
  - 匹配结束标签 `{/% if }`
- [x] `parseForStatement()` - 解析 for 循环
  - 格式：`{% for 变量 in 集合 }...{/% for }`
  - 解析循环变量（单个或索引+值）
  - 解析 in 关键字
  - 解析集合表达式或区间
  - 解析循环体
  - 匹配结束标签 `{/% for }`
- [x] `parseSwitchStatement()` - 解析 switch 语句
  - 格式：`{% switch 表达式 }{% case 值 }...{% default }...{/% switch }`
  - 解析 switch 表达式
  - 解析多个 case 分支（格式 `{% case 值 }`，自闭合）
  - 解析 default 分支（格式 `{% default }`，自闭合，可选）
  - 匹配结束标签 `{/% switch }`

##### 自闭合语句块解析（无结束标签）

- [x] `parseSelfClosingStatement()` - 解析自闭合语句块
  - 格式：`{% 语句 %}` 或 `{% 语句1; 语句2; ... %}`
  - 识别语句类型（let/extends/import/其他）
  - 支持单条语句或多条语句（用换行或分号分隔）
  - 路由到具体的解析方法
- [x] `parseLetStatement()` - 解析 let 变量声明
  - 格式：`{% let 变量名 = 表达式 %}`
  - 解析变量名
  - 解析赋值表达式
  - **赋值约束检查**：如果赋值表达式是 IfExpression，检查其是否包含 else 分支，若缺少则抛出编译错误
- [x] `parseExtendsStatement()` - 解析 extends 模板扩展
  - 格式：`{% extends '模板路径' %}`
  - 解析模板文件路径（字符串字面量）
- [x] `parseImportStatement()` - 解析 import 类导入
  - 格式：`{% import 类全限定名 %}`
  - 解析 Java 类全限定名
- [x] `parseMultiLineStatements()` - 解析多条语句
  - 按换行符或分号分割语句
  - 依次解析每条语句
  - 每条语句可以是 let、extends、import 等

#### 5.5 语法解析器测试

- [x] 测试基本字面量解析
  - 数字字面量（int、long、BigInteger、double、BigDecimal）
  - 字符串字面量
  - 字符串模板字面量
  - 列表字面量（整数列表、字符串列表、混合类型、嵌套列表）
  - 哈希表字面量（标识符键、字符串键、数字键、嵌套哈希表、空哈希表）
  - 区间字面量
  - 正则表达式字面量
- [x] 测试变量访问
- [x] 测试成员访问链
  - 普通成员访问
  - 空安全访问
- [x] 测试方法调用
- [x] 测试各种运算符优先级
  - 算术运算
  - 比较运算
  - 逻辑运算
  - 三元运算符
  - Elvis 运算符
  - 正则匹配运算符
- [x] 测试复杂嵌套表达式
- [x] 测试语句块解析
  - if/else if/else 语句
  - for 循环语句
  - switch 语句
- [x] 测试嵌入式代码块解析
  - let 变量声明
  - extends 模板扩展
  - import 类导入
  - 多行代码块
- [x] 测试完整模板解析

---

### 第六阶段：编译器（第 17-19 天）✅ 已完成

#### 6.1 编译器接口

- [x] `Compiler` 接口
  - `compile(TemplateNode ast)` - 编译 AST 到 Template

#### 6.2 解释器实现

创建 `InterpreterTemplate` 类（直接解释执行）：

- [x] 实现 `Template` 接口
- [x] 持有 AST 根节点
- [x] `render(Context context)` 方法
  - 遍历 AST
  - 执行表达式
  - 拼接结果字符串

#### 6.3 访问者模式实现

创建 `Evaluator` 类（实现 Visitor）：

- [x] `visitTextNode()` - 处理文本节点
- [x] `visitExpressionNode()` - 处理表达式节点
- [x] `visitStringLiteral()` - 处理字符串字面量
- [x] `visitTemplateLiteral()` - 处理字符串模板字面量
  - 解析嵌入的表达式
  - 拼接字符串结果
- [x] `visitNumberLiteral()` - 处理数值字面量
  - 支持 int、long、BigInteger、double、BigDecimal
- [x] `visitBooleanLiteral()` - 处理布尔字面量
- [x] `visitNullLiteral()` - 处理 null
- [x] `visitListLiteral()` - 处理列表字面量
  - 求值每个元素表达式
  - 创建 ArrayList 对象
  - 支持嵌套列表
- [x] `visitHashLiteral()` - 处理哈希表字面量
  - 求值每个键和值表达式
  - 创建 HashMap 对象
  - 标识符键自动转换为字符串
  - 支持嵌套哈希表
- [x] `visitRangeLiteral()` - 处理区间字面量
  - 创建区间对象
  - 支持四种区间类型
- [x] `visitRegexLiteral()` - 处理正则表达式字面量
  - 编译正则表达式
  - 应用标志位
- [x] `visitIdentifier()` - 处理标识符
- [x] `visitMemberAccess()` - 处理成员访问
  - 普通成员访问
  - 空安全成员访问（`?.`）
- [x] `visitIndexAccess()` - 处理索引访问
- [x] `visitMethodCall()` - 处理方法调用
- [x] `visitBinaryExpression()` - 处理二元表达式
  - 算术运算
  - 比较运算
  - 逻辑运算
  - 正则匹配（`=~`、`!=~`）
  - 范围检查（`in`）
- [x] `visitUnaryExpression()` - 处理一元表达式
- [x] `visitTernaryExpression()` - 处理三元表达式
- [x] `visitElvisExpression()` - 处理 Elvis 表达式（`?:`）
  - 左侧为 null 时返回右侧默认值
- [x] `visitIfExpression()` - 处理 if 表达式（条件表达式）
  - 求值条件表达式
  - 根据条件选择执行对应的语句块
  - 依次执行语句块中的语句
  - 返回语句块中最后一条语句的值
  - 支持 else if 和 else 分支
- [x] `visitForExpression()` - 处理 for 循环语句
  - 求值集合表达式或区间
  - 为每个元素执行循环体
  - 管理循环变量作用域（仅限循环体内）
  - 支持单变量和索引+值变量两种形式
  - 依次执行循环体中的语句
  - 返回 null
- [x] `visitIfStatement()` - 处理 if 语句（条件语句块）
  - 条件判断
  - 分支选择（if/else if/else）
- [x] `visitForStatement()` - 处理 for 循环
  - 迭代集合或区间
  - 管理循环变量和索引
  - 循环体执行
- [x] `visitSwitchStatement()` - 处理 switch 语句
  - 表达式求值
  - case 匹配
  - default 分支
- [x] `visitLetStatement()` - 处理 let 变量声明
  - 求值赋值表达式
  - 将变量存入上下文
- [x] `visitExtendsStatement()` - 处理 extends 模板扩展
  - 加载模板文件
  - 编译并渲染子模板
  - 将结果嵌入当前位置
- [x] `visitImportStatement()` - 处理 import 类导入
  - 加载 Java 类
  - 将类注册到上下文（支持静态方法调用）

---

### 第七阶段：运行时执行（第 20-24 天）✅ 已完成（部分测试待完善）

#### 7.1 反射工具类

创建 `ReflectionUtils` 类：

- [x] `getProperty(Object obj, String propertyName)` - 获取属性值
  - 支持 getter 方法
  - 支持 public 字段
  - 支持 Map 的 get 方法
- [x] `invokeMethod(Object obj, String methodName, Object[] args)` - 调用方法
  - 支持方法重载匹配
  - 支持可变参数
  - 支持基本类型转换
- [x] `getIndexedValue(Object obj, Object index)` - 获取索引值
  - 支持数组
  - 支持 List
  - 支持 Map
- [x] 类型转换和匹配工具方法

#### 7.2 运算符执行

创建 `OperatorEvaluator` 类：

- [x] 算术运算
  - `add(Object a, Object b)` - 支持 BigInteger、BigDecimal
  - `subtract(Object a, Object b)` - 支持 BigInteger、BigDecimal
  - `multiply(Object a, Object b)` - 支持 BigInteger、BigDecimal
  - `divide(Object a, Object b)` - 支持 BigInteger、BigDecimal
  - `modulo(Object a, Object b)` - 支持 BigInteger、BigDecimal
- [x] 比较运算
  - `greaterThan(Object a, Object b)`
  - `lessThan(Object a, Object b)`
  - `greaterThanOrEqual(Object a, Object b)`
  - `lessThanOrEqual(Object a, Object b)`
  - `equal(Object a, Object b)`
  - `notEqual(Object a, Object b)`
- [x] 逻辑运算
  - `and(Object a, Object b)`
  - `or(Object a, Object b)`
  - `not(Object a)`
- [x] 正则运算
  - `regexMatch(String text, Pattern regex)` - 正则匹配
  - `regexNotMatch(String text, Pattern regex)` - 正则不匹配
- [x] 范围运算
  - `inCollection(Object value, Collection collection)` - 判断值是否在集合中（包括列表字面量）
  - `inRange(Object value, Range range)` - 判断值是否在区间内
  - `inMap(Object key, Map map)` - 判断键是否在 Map 中（包括哈希表字面量）
  - `inString(String substring, String string)` - 判断子字符串是否包含在字符串中
  - `notIn(Object value, Object container)` - 不包含判断（!in 运算符）
- [x] 类型转换
  - `toBoolean(Object value)`
  - `toNumber(Object value)`
  - `toString(Object value)`
  - `toBigInteger(Object value)`
  - `toBigDecimal(Object value)`

#### 7.3 区间对象

创建 `Range` 类：

- [x] 区间类型枚举
  - `OPEN` - 全开区间 (a, b)
  - `CLOSED` - 全闭区间 [a, b]
  - `LEFT_OPEN_RIGHT_CLOSED` - 前开后闭 (a, b]
  - `LEFT_CLOSED_RIGHT_OPEN` - 前闭后开 [a, b)
- [x] `contains(Object value)` - 判断值是否在区间内
- [x] `iterator()` - 支持区间迭代
- [x] `toList()` - 转换为列表（用于循环）

#### 7.4 正则表达式工具

正则表达式功能已集成到 Evaluator 和 OperatorEvaluator 中：

- [x] 正则表达式编译（在 Evaluator 中）
- [x] 正则匹配执行（在 OperatorEvaluator 中）
- [x] 标志位支持（g、i、m）
- [ ] 正则表达式缓存（待优化）

#### 7.5 上下文管理

增强 `Context` 类：

- [x] 支持作用域链（已在第一阶段实现）
  - 循环作用域（for 循环变量）
  - 语句块作用域
- [x] 支持变量覆盖
- [ ] 支持只读变量（待实现）
- [x] 支持循环变量
  - 循环索引
  - 循环值
- [x] 变量查找优化

#### 7.6 模板加载器

创建模板加载器，支持 extends 功能：

- [ ] `TemplateLoader` 接口（待实现）
  - `load(String path)` 方法 - 加载模板文件内容
- [ ] `FileTemplateLoader` 类（待实现）
  - 从文件系统加载模板
  - 支持相对路径和绝对路径
  - 支持路径解析
- [ ] `ClasspathTemplateLoader` 类（待实现）
  - 从 classpath 加载模板
- [ ] 模板缓存机制（待实现）
  - 缓存已加载的模板内容
  - 支持缓存失效策略

**注意**：模板加载器功能（extends 语句）目前代码已实现基本逻辑，但完整的加载器接口和实现类待完善。

---

### 第八阶段：集成与优化（第 25-27 天）

#### 7.1 TemplateEngine 完善

- [ ] 集成 Lexer、Parser、Compiler
- [ ] 实现 `compile(String template)` 方法
- [ ] 集成 TemplateLoader
  - 支持自定义模板加载器
  - 默认使用 FileTemplateLoader
- [ ] 添加配置选项
  - 花括号风格支持（单花括号、双花括号、美元符号、或组合）
  - 严格模式
  - 缓存开关
  - 模板基础路径
- [ ] 模板缓存机制
- [ ] 支持导入的 Java 类管理

#### 7.2 性能优化

- [ ] AST 节点对象池
- [ ] Token 对象池
- [ ] StringBuilder 复用
- [ ] 反射结果缓存
  - Method 缓存
  - Field 缓存
- [ ] 常量折叠优化

#### 7.3 错误处理增强

- [ ] 详细的错误信息
  - 行号和列号
  - 错误位置上下文
  - 建议修复方案
- [ ] 异常堆栈优化
- [ ] 调试模式

---

### 第九阶段：测试（第 28-32 天）

#### 8.1 单元测试

- [ ] Lexer 测试套件
  - 各种 Token 识别
  - 字符串模板解析
  - 区间字面量解析
  - 正则表达式解析
  - 语句块标记识别
  - 边界情况
  - 错误输入
- [ ] Parser 测试套件
  - 表达式解析
  - 运算符优先级
  - 空安全操作符
  - Elvis 运算符
  - 正则匹配运算符
  - 条件表达式解析（if 表达式，if-else，if-else if-else）
  - for 循环语句解析（单变量、索引+值变量）
  - 语句块解析（if/for/switch）
  - 语法错误处理
- [ ] Evaluator 测试套件
  - 各种节点执行
  - 运算符计算
  - 方法调用
  - 空安全访问
  - Elvis 表达式
  - 正则匹配
  - 列表字面量求值
  - 哈希表字面量求值（标识符键、字符串键、数字键、嵌套哈希表）
  - 条件表达式执行（if 表达式）
  - 条件表达式返回值测试
  - 条件表达式语句块执行
  - for 循环语句执行
  - for 循环语句作用域测试
  - for 循环语句遍历集合和区间
  - 条件语句执行（if 语句块）
  - 循环语句执行
  - Switch 语句执行
- [ ] Range 测试套件
  - 区间创建
  - 区间包含判断
  - 区间迭代
- [ ] RegexUtils 测试套件
  - 正则编译
  - 正则匹配
  - 标志位处理
- [ ] TemplateEngine 测试套件
  - 端到端测试
  - 性能测试

#### 8.2 集成测试

- [ ] 基本功能测试
  - 纯文本模板
  - 变量替换
  - 表达式计算
  - 单花括号、双花括号和美元符号表达式
- [ ] 字面量测试
  - 整数、长整数、大整数
  - 浮点数、科学计数法、高精度小数
  - 字符串和字符串模板
  - 列表字面量（整数列表、字符串列表、混合类型、嵌套列表）
  - 哈希表字面量（标识符键、字符串键、数字键、混合类型键、嵌套哈希表、空哈希表）
  - 区间字面量
  - 正则表达式字面量
- [ ] 运算符测试
  - 算术运算（支持大数运算）
  - 比较运算
  - 逻辑运算
  - 空安全操作符 `?.`
  - Elvis 运算符 `?:`
  - 正则匹配 `=~` 和 `!=~`
  - in 和 !in 运算符（集合、区间、Map、字符串）
- [ ] 条件表达式测试
  - 基本 if 表达式
  - if-else 表达式
  - if-else if-else 表达式
  - 条件表达式返回值
  - 条件表达式语句块中的多条语句
  - 条件表达式用于赋值
  - 条件表达式作为参数传递
  - 嵌套条件表达式
  - **赋值约束测试**：验证 if 表达式用于赋值时缺少 else 分支会抛出编译错误
  - **赋值约束测试**：验证 if 表达式用于赋值时包含 else 分支能正常工作
- [ ] for 循环语句测试
  - 基本 for 循环语句（单变量）
  - 带索引的 for 循环语句
  - 遍历集合（List、Set、数组）
  - 遍历区间字面量
  - 循环体中的多条语句
  - 循环变量作用域测试
  - 嵌套 for 循环语句
  - for 循环语句与 if 表达式结合
  - for 循环语句修改外部变量
  - for 循环语句返回值测试（应返回 null）
- [ ] 语句块测试
  - if/else if/else 条件语句块
  - for 循环（集合和区间）
  - switch 语句
  - 嵌套语句块
- [ ] 嵌入式代码块测试
  - let 变量声明和赋值
  - extends 模板扩展
  - import Java 类导入
  - 多行代码块
- [ ] 高级功能测试
  - 复杂表达式
  - 方法调用链
  - 空安全链式调用
  - 字符串模板嵌入表达式
- [ ] 边界测试
  - 空模板
  - 空上下文
  - null 值处理
  - 空安全操作
  - 大模板性能
  - 深度嵌套

#### 8.3 真实场景测试

- [ ] 邮件模板
  - 带条件的欢迎邮件
  - 订单确认邮件（循环显示商品）
- [ ] HTML 模板
  - 用户列表页面（for 循环）
  - 表单验证提示（if 条件）
  - 导航菜单（switch 状态）
- [ ] SQL 模板
  - 动态 WHERE 条件
  - 批量插入语句
- [ ] 日志模板
  - 带变量的日志消息
  - 条件日志级别
- [ ] 配置文件模板
  - 环境相关配置
  - 动态生成配置项
- [ ] 报表模板
  - 数据汇总表格
  - 条件格式化
  - 循环生成报表行

---

### 第十阶段：文档与示例（第 33-36 天）

#### 9.1 API 文档

- [ ] JavaDoc 完善
- [ ] 使用指南
- [ ] 最佳实践

#### 9.2 示例代码

- [ ] 基础示例
- [ ] 进阶示例
- [ ] 性能优化示例
- [ ] 错误处理示例

#### 9.3 README 完善

- [ ] 快速开始
- [ ] 功能特性
- [ ] 使用示例
- [ ] FAQ

---

## 技术难点与解决方案

### 难点 1：Source 类的缓冲区管理

**挑战：**
- 需要支持多种输入源（字符串、流、文件）
- 大文件不能全部加载到内存
- 需要支持字符预览和回溯
- 缓冲区需要自动清理已读内容
- 需要准确跟踪行号和列号

**解决方案：**
1. 使用接口抽象不同的输入源类型
2. 实现滑动窗口的环形缓冲区
3. 缓冲区大小根据实际使用情况动态调整
4. 保留足够的回溯空间（默认 1KB）
5. 自动识别和处理不同的换行符格式
6. 使用标记点机制支持 reset 操作
7. StringSource 直接操作字符串，避免额外缓冲开销

### 难点 2：TokenStream 的流式设计

**挑战：**
- 避免将所有 Token 都加载到内存中
- 需要支持 Token 预览和有限回溯
- Parser 需要能够按需获取 Token
- 大型模板文件的内存效率

**解决方案：**
1. 实现 TokenStream 类作为 Lexer 和 Parser 之间的桥梁
2. 内部维护小型 Token 缓冲区（默认 3 个 Token）
3. 支持 peek() 和 peek(offset) 预览功能
4. 按需从 Lexer 读取 Token，实现懒加载
5. Token 缓冲区采用循环队列实现，提高效率
6. 支持有限的回溯能力（回溯范围取决于缓冲区大小）
7. Parser 通过 TokenStream 间接访问 Lexer，解耦两者

### 难点 3：JavaScript 风格的动态类型表达式

**挑战：**
- Java 是静态类型语言，处理动态表达式复杂
- 需要支持 `user.name`、`user.getName()` 等多种形式
- 空安全操作符 `?.` 需要特殊处理

**解决方案：**
1. 使用反射 API 进行属性访问和方法调用
2. 建立类型推断系统
3. 缓存反射结果提升性能
4. 使用 MethodHandle 替代部分反射调用
5. 在 AST 节点中标记空安全操作，运行时进行 null 检查

### 难点 4：运算符优先级和结合性

**挑战：**
- 正确处理复杂表达式的运算顺序
- 支持括号改变优先级
- 新增运算符（`?.`、`?:`、`=~`、`!=~`、区间运算符）的优先级定义

**解决方案：**
1. 使用递归下降解析器
2. 按优先级分层解析函数
3. 参考 JavaScript 和 Kotlin 的运算符优先级表
4. Elvis 运算符优先级最低，空安全操作符作为后缀运算符处理

### 难点 5：方法重载匹配

**挑战：**
- Java 支持方法重载，需要根据参数类型匹配正确的方法
- 需要处理基本类型和包装类型的转换
- 支持可变参数方法

**解决方案：**
1. 实现类型距离算法
2. 选择最匹配的方法签名
3. 支持自动装箱/拆箱
4. 支持向上类型转换
5. 可变参数方法优先级最低

### 难点 6：大数运算支持

**挑战：**
- 需要支持 BigInteger 和 BigDecimal
- 混合类型运算时的类型提升
- 保持运算精度

**解决方案：**
1. 自动检测数值范围，超出时转换为大数类型
2. 实现类型提升规则：int → long → BigInteger → BigDecimal
3. 运算符重载支持大数类型
4. 使用 BigDecimal 进行高精度小数运算

### 难点 7：正则表达式集成

**挑战：**
- 正则表达式的解析和编译
- 标志位的处理（g、i、m）
- 性能优化（缓存编译结果）

**解决方案：**
1. 词法分析阶段识别正则表达式字面量
2. 使用 Java 的 Pattern 类进行编译
3. 缓存已编译的 Pattern 对象
4. 标志位转换为 Java Pattern 的 flags

### 难点 8：控制流语句实现

**挑战：**
- if/for/switch 语句的解析和执行
- 嵌套语句块的作用域管理
- 循环变量的生命周期

**解决方案：**
1. 使用递归下降解析器解析语句块
2. 实现作用域栈管理变量
3. 为每个语句块创建独立的 AST 节点
4. 循环变量在进入循环时创建，退出时销毁

### 难点 9：字符串模板字面量

**挑战：**
- 反引号字符串中嵌入表达式的解析
- 嵌套花括号的处理
- 表达式求值和字符串拼接

**解决方案：**
1. 词法分析器识别反引号，进入字符串模板模式
2. 在模板内部递归解析嵌入的表达式
3. 创建特殊的 TemplateLiteral 节点
4. 运行时依次求值表达式并拼接字符串

### 难点 10：列表字面量实现

**挑战：**
- 列表元素可以是任意类型的表达式
- 支持嵌套列表
- 需要在解析时区分索引访问和列表字面量

**解决方案：**
1. 在词法分析阶段识别方括号上下文
2. 创建专门的 ListLiteral 节点
3. 递归解析列表元素表达式
4. 运行时依次求值元素并构建 ArrayList
5. 支持嵌套列表的递归处理

### 难点 11：哈希表字面量实现

**挑战：**
- 花括号 `{}` 在模板中有多种用途：表达式嵌入、语句块标记、哈希表字面量
- 需要在词法和语法分析阶段正确区分上下文
- 哈希表的键可以是标识符、字符串或数字，需要特殊处理
- 支持嵌套哈希表的递归解析
- 空哈希表 `{}` 需要特殊识别

**解决方案：**
1. 在表达式上下文中，当遇到 `{` 且后面跟着标识符/字符串/数字 + `:` 时，识别为哈希表字面量
2. 空花括号 `{}` 在表达式上下文中识别为空哈希表
3. 创建专门的 HashLiteral 节点存储键值对列表
4. 标识符类型的键在运行时自动转换为字符串
5. 递归解析值表达式，支持嵌套哈希表
6. 运行时依次求值键和值表达式，构建 HashMap 对象
7. 支持使用点号访问标识符键：`map.key`
8. 支持使用方括号访问任意类型键：`map["key"]`、`map[0]`

### 难点 12：嵌入式代码块实现

**挑战：**
- let 语句需要支持变量声明和作用域管理
- extends 需要加载外部模板文件
- import 需要动态加载 Java 类
- 多行代码块的解析和执行顺序

**解决方案：**
1. 为每种嵌入式代码创建专门的 AST 节点
2. let 语句：将变量存入当前上下文，支持后续引用
3. extends 语句：实现模板加载器，支持文件路径解析和缓存
4. import 语句：使用 Class.forName() 加载类，注册到上下文供静态方法调用
5. 多行代码块：按行解析，依次执行，共享同一上下文

### 难点 13：条件表达式实现

**挑战：**
- 条件表达式与条件语句块的语法区分
- 语句块的解析和执行
- 语句块的返回值处理
- if 表达式与 if 语句的 AST 节点设计
- 条件表达式的优先级和结合性
- 语句块中多条语句的作用域管理
- 赋值约束：用于赋值的 if 表达式必须有 else 分支

**解决方案：**
1. 创建独立的 IfExpression 节点，区别于 IfStatement 节点
2. IfExpression 用于条件表达式，只能在自闭合语句块中使用
3. IfStatement 用于条件语句块，是标准语句块，用于控制模板输出
4. 语句块解析：识别花括号，解析内部的语句列表
5. 支持分号和换行符作为语句分隔符
6. 语句块返回值：执行所有语句，返回最后一条语句的求值结果
7. 条件表达式在表达式优先级中的位置：在 Elvis 表达式和三元运算符之间
8. 作用域管理：语句块中定义的变量在整个上下文中可见
9. 支持 else if 分支：解析多个条件和对应的语句块
10. 运行时求值：根据条件选择执行对应的语句块，返回该语句块的结果
11. **赋值约束检查**：在编译阶段，如果检测到 if 表达式用于赋值语句（= 右边），但缺少 else 分支，则抛出编译错误，提示必须包含 else 语句块以确保变量一定会被赋值

### 难点 14：for 循环语句实现

**挑战：**
- for 循环语句与 for 循环语句块的语法区分
- 循环变量的作用域管理（仅限循环体内）
- 支持单变量和索引+值变量两种形式
- 语句块的解析和执行
- for 表达式与 for 语句的 AST 节点设计
- 循环变量在循环结束后不可访问

**解决方案：**
1. 创建独立的 ForExpression 节点，区别于 ForStatement 节点
2. ForExpression 用于 for 循环语句，只能在自闭合语句块中使用
3. ForStatement 用于 for 循环语句块，是标准语句块，用于控制模板输出
4. 语法解析：识别 `for` 关键字、括号、`in` 关键字、循环体花括号
5. 支持两种循环变量形式：单变量 `for (item in list)` 和索引+值 `for (i, item in list)`
6. 循环变量作用域管理：创建临时作用域，循环结束后销毁
7. 循环体执行：为集合或区间的每个元素执行循环体语句
8. 返回值：for 循环语句执行完成后始终返回 null
9. 支持嵌套 for 循环语句
10. 循环体内可以访问外部变量并修改它们
11. 循环变量只在循环体内可见，循环结束后自动清理

### 难点 15：性能优化

**挑战：**
- 反射调用性能损耗大
- 频繁的对象创建影响性能
- 复杂模板的渲染性能
- 流式处理对性能的影响

**解决方案：**
1. 编译时优化（常量折叠、死代码消除）
2. 运行时缓存（反射结果、类型信息、正则表达式）
3. 对象池技术（Token、AST 节点）
4. 使用 MethodHandle 优化反射性能
5. StringBuilder 复用，减少内存分配
6. TokenStream 的小型缓冲区设计，平衡内存和性能

---

## 里程碑

- **M1（第 6 天）** ✅ 已完成：完成 Source 类、TokenStream 类和词法分析器，支持所有 Token 类型（包括列表字面量、!in运算符），实现流式 Token 输出
  - 完成时间：2025年11月9日
  - 测试数：85个（SourceTest: 22, TokenStreamTest: 8, LexerTest: 55）
  - 测试通过率：100%
- **M2（第 9 天）** ✅ 已完成：完成 AST 定义，支持所有节点类型（包括列表、哈希表、嵌入式代码块节点、IfExpression、ForExpression、三种表达式节点类型）
  - 完成时间：2025年11月18日
  - AST节点类：34个（包括所有字面量、表达式、语句块节点，以及三种表达式节点类型）
  - 测试数：97个（ASTNodeTest）
  - 测试通过率：100%
- **M3（第 10 天）** ✅ 已完成：完成 BNF 文法文档，明确所有语法规则和优先级
  - 完成时间：2025年11月10日
  - BNF 文法文档：BNF_GRAMMAR.md（包含完整的语法规则、优先级、歧义处理等）
- **M4（第 16 天）** ✅ 已完成：完成语法解析器（基于 TokenStream 的流式解析），支持表达式和语句块解析（包括哈希表字面量、嵌入式代码块）
  - 完成时间：2025年11月（具体日期待确认）
  - Parser 实现：Parser.java（857 行）
  - 测试覆盖：ParserTest.java（100+ 个测试用例）
- **M5（第 19 天）** ✅ 已完成：完成编译器，支持 AST 遍历和求值
  - 完成时间：2025年11月（具体日期待确认）
  - 编译器实现：Evaluator.java（32 个 visit 方法）、InterpreterCompiler.java、InterpreterTemplate.java
  - 测试覆盖：CompilerIntegrationTest.java（50+ 个测试用例）
- **M6（第 24 天）** ✅ 已完成（部分）：完成运行时执行，支持所有运算符和控制流
  - 完成时间：2025年11月（具体日期待确认）
  - 运行时实现：OperatorEvaluator.java、Range.java、ReflectionUtils.java
  - 测试状态：部分测试已完成，待完善
  - 待完成：模板加载器（TemplateLoader 接口和实现类）
- **M7（第 27 天）** ⏳ 待完成：完成集成和优化，性能达标
- **M8（第 32 天）** ⏳ 待完成：完成所有测试，质量达标
- **M9（第 36 天）** ⏳ 待完成：完成文档和示例，发布 1.0.0 版本

---

## 质量标准

- **代码覆盖率**：> 85%
- **单元测试**：所有核心类都有完整测试
- **性能基准**：
  - 简单模板渲染 < 1ms
  - 复杂模板渲染 < 5ms
  - 模板编译 < 10ms
- **代码规范**：遵循阿里巴巴 Java 代码规范
- **注释完整度**：所有 public 方法都有 JavaDoc

---

## 后续规划（2.0 版本）

- [ ] 支持自定义函数注册
  - 全局函数注册
  - 函数命名空间
  - 函数重载
- [ ] 支持模板继承和包含
  - `{% extends "base.tpl" %}`
  - `{% include "header.tpl" %}`
  - `{% block name %}...{% endblock %}`
- [ ] 支持 while 循环语句
  - `{% while condition %}...{% endwhile %}`
- [ ] 支持 break 和 continue
  - 循环控制语句
- [ ] 支持过滤器（Filter）
  - 管道语法 `{value | filter}`
  - 内置过滤器（upper、lower、trim、format 等）
  - 自定义过滤器注册
- [ ] 支持宏定义（Macro）
  - `{% macro name(params) %}...{% endmacro %}`
  - 宏调用 `{@name(args)}`
- [ ] 支持国际化（i18n）
  - `{_('message.key')}`
  - 多语言资源文件
- [ ] 支持 Lambda 表达式
  - `list.filter(x -> x > 10)`
  - `list.map(x -> x * 2)`
- [ ] 字节码生成优化（ASM）
  - 编译模板到字节码
  - 提升运行时性能
- [ ] GraalVM 原生编译支持
  - AOT 编译
  - 原生镜像生成
- [ ] 沙箱安全模式
  - 限制反射调用
  - 白名单机制
  - 防止恶意代码注入
- [ ] 模板调试器
  - 断点调试
  - 变量监视
  - 执行跟踪

