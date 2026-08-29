# EasyTL BNF 文法定义

本文档详细定义了 EasyTL 模板引擎的巴科斯-瑙尔范式（BNF）文法规则。

## 1. 符号说明

- `<...>` : 非终结符
- `'...'` : 终结符（字面量）
- `Token` : 终结符（Token 类型）
- `|` : 选择（OR）
- `*` : 零次或多次重复
- `+` : 一次或多次重复
- `?` : 可选（零次或一次）
- `(...)` : 分组

## 2. 词法符号 (Tokens)

参考 `org.dromara.easytl.parser.TokenType` 定义。

## 3. 模板结构 (Template Structure)

模板由一系列的模板部分组成，可以是普通文本、表达式或语句块。

```bnf
<template> ::= <template_part>* EOF

<template_part> ::= TEXT
                  | <expression_wrapper>
                  | <statement_block>

<expression_wrapper> ::= <single_brace_expr>
                       | <double_brace_expr>
                       | <dollar_brace_expr>

<single_brace_expr> ::= SINGLE_BRACE_EXPR_BEGIN <expression> SINGLE_BRACE_EXPR_END

<double_brace_expr> ::= DOUBLE_BRACE_EXPR_BEGIN <expression> DOUBLE_BRACE_EXPR_END

<dollar_brace_expr> ::= DOLLAR_BRACE_EXPR_BEGIN <expression> DOLLAR_BRACE_EXPR_END
```

## 4. 语句块 (Statement Blocks)

语句块分为标准语句块（有结束标签）和自闭合语句块（无结束标签）。

```bnf
<statement_block> ::= <standard_statement_block>
                    | <self_closing_statement_block>

<standard_statement_block> ::= <if_statement>
                             | <for_statement>
                             | <switch_statement>

<self_closing_statement_block> ::= STMT_BEGIN <script_content> STMT_END
```

### 4.1 脚本内容 (Script Content)

自闭合语句块中可以包含一条或多条语句。

```bnf
<script_content> ::= <script_statement> ( ( SEMICOLON | NEWLINE ) <script_statement> )*

<script_statement> ::= <let_statement>
                     | <extends_statement>
                     | <import_statement>
                     | <expression>            /* 允许普通表达式作为语句（如方法调用） */
                     | <structured_expression> /* 允许结构化表达式作为语句 */
                     | /* 空语句 */
```

### 4.2 具体语句定义

#### Let 语句 (变量声明)
```bnf
/* 变量声明语句，赋值右侧允许普通表达式或结构化表达式 */
<let_statement> ::= LET IDENTIFIER ASSIGN ( <expression> | <structured_expression> )
```

#### Extends 语句 (模板继承)
```bnf
<extends_statement> ::= EXTENDS STRING
```

#### Import 语句 (模板导入)
```bnf
/* 导入其他模板文件并指定别名 */
<import_statement> ::= IMPORT STRING AS IDENTIFIER
```

### 4.3 标准语句块定义

#### If 语句块
```bnf
<if_statement> ::= STMT_BEGIN IF <expression> RIGHT_BRACE
                   <template>
                   <else_if_clause>*
                   <else_clause>?
                   STMT_CLOSE IF RIGHT_BRACE

<else_if_clause> ::= STMT_BEGIN ELSE IF <expression> RIGHT_BRACE <template>

<else_clause> ::= STMT_BEGIN ELSE RIGHT_BRACE <template>
```

#### For 语句块
```bnf
<for_statement> ::= STMT_BEGIN FOR LEFT_PAREN <for_control> RIGHT_PAREN RIGHT_BRACE
                    <template>
                    STMT_CLOSE FOR RIGHT_BRACE

<for_control> ::= IDENTIFIER ( COMMA IDENTIFIER )? IN <expression>
```

#### Switch 语句块
```bnf
<switch_statement> ::= STMT_BEGIN SWITCH <expression> RIGHT_BRACE
                       <case_clause>*
                       <default_clause>?
                       STMT_CLOSE SWITCH RIGHT_BRACE

<case_clause> ::= STMT_BEGIN CASE <expression> RIGHT_BRACE <template>

<default_clause> ::= STMT_BEGIN DEFAULT RIGHT_BRACE <template>
```

## 5. 表达式 (Expressions)

表达式分为普通表达式和结构化表达式。

### 5.1 结构化表达式 (Structured Expressions)

结构化表达式包含流程控制逻辑，只能用于赋值语句右侧或作为独立语句，**不能**嵌入到普通表达式运算中（如 `1 + if(...)` 是非法的）。

```bnf
<structured_expression> ::= <if_expression>
                          | <for_expression>
                          | <switch_expression>

/* If 表达式 (条件表达式) */
<if_expression> ::= IF LEFT_PAREN <expression> RIGHT_PAREN <block_statement>
                    ( ELSE IF LEFT_PAREN <expression> RIGHT_PAREN <block_statement> )*
                    ( ELSE <block_statement> )?

/* For 循环表达式 (副作用循环) */
<for_expression> ::= FOR LEFT_PAREN <for_control> RIGHT_PAREN <block_statement>

/* Switch 表达式 */
<switch_expression> ::= SWITCH LEFT_PAREN <expression> RIGHT_PAREN LEFT_BRACE
                        ( CASE <expression> <block_statement> )*
                        ( DEFAULT <block_statement> )?
                        RIGHT_BRACE

/* 代码块 (用于结构化表达式) */
<block_statement> ::= LEFT_BRACE <script_content> RIGHT_BRACE
```

### 5.2 普通表达式优先级表

| 优先级 | 运算符/结构 | 结合性 |
| :--- | :--- | :--- |
| 1 | `??` (Elvis), `expression` | 右结合 |
| 2 | `? :` (Ternary) | 右结合 |
| 3 | `||` | 左结合 |
| 4 | `&&` | 左结合 |
| 5 | `==`, `!=`, `=~`, `!=~`, `in`, `!in`, `subsetof`, `anyof`, `noneof` | 左结合 |
| 6 | `<`, `<=`, `>`, `>=` | 左结合 |
| 7 | `..`, `..<`, `>..`, `>..<` | 左结合 |
| 8 | `+`, `-` | 左结合 |
| 9 | `*`, `/`, `%` | 左结合 |
| 10 | `!`, `-` (Unary), `+` (Unary), `(type)` (Cast) | 右结合 |
| 11 | `.` (Member), `?.` (Safe), `[]` (Index), `()` (Call) | 左结合 |
| 12 | Primary (Literal, Identifier, Parens) | - |

### 5.3 普通表达式规则

```bnf
<expression> ::= <elvis_expression>

<elvis_expression> ::= <ternary_expression> ( ELVIS <ternary_expression> )*

<ternary_expression> ::= <logical_or_expression> ( QUESTION <expression> COLON <ternary_expression> )?

<logical_or_expression> ::= <logical_and_expression> ( OR <logical_and_expression> )*

<logical_and_expression> ::= <equality_expression> ( AND <equality_expression> )*

<equality_expression> ::= <relational_expression> 
                          ( ( EQ | NEQ | REGEX_MATCH | REGEX_NOT_MATCH | IN | NOT_IN
                            | SUBSETOF | ANYOF | NONEOF ) <relational_expression> )*

<relational_expression> ::= <range_expression>
                            ( ( LT | LTE | GT | GTE ) <range_expression> )*

<range_expression> ::= <additive_expression> 
                       ( ( RANGE_OPEN | RANGE_CLOSED | RANGE_LEFT_OPEN_RIGHT_CLOSED | RANGE_LEFT_CLOSED_RIGHT_OPEN ) <additive_expression> )?

<additive_expression> ::= <multiplicative_expression> ( ( PLUS | MINUS ) <multiplicative_expression> )*

<multiplicative_expression> ::= <unary_expression> ( ( MULTIPLY | DIVIDE | MODULO ) <unary_expression> )*

<unary_expression> ::= ( NOT | MINUS | PLUS ) <unary_expression>
                     | <cast_expression>
                     | <postfix_expression>

<cast_expression> ::= LEFT_PAREN IDENTIFIER RIGHT_PAREN <unary_expression>

<postfix_expression> ::= <primary_expression> <postfix_op>*

<postfix_op> ::= DOT IDENTIFIER
               | DOT MULTIPLY                    /* JSONPath 点通配 .*（与 [*] 等价） */
               | SAFE_NAV IDENTIFIER
               | LEFT_BRACKET <bracket_selector> ( COMMA <bracket_selector> )* RIGHT_BRACKET
               /* 单个选择器 → 下标/通配/过滤/切片访问；含逗号 → JSONPath union 多选（结果合并） */
               | RANGE_CLOSED IDENTIFIER
               /* JSONPath 递归下降 ..name：仅当左值为路径类节点时成立（见 7.6 歧义消解） */
               | RANGE_CLOSED MULTIPLY
               /* JSONPath 递归通配 ..*：收集全部后代值（左值为路径类节点或标识符） */
               | RANGE_CLOSED LEFT_BRACKET <bracket_selector> ( COMMA <bracket_selector> )* RIGHT_BRACKET
               /* JSONPath 递归下降括号形式 ..['name'] / ..[0] / ..[1:3] / ..[?(...)] / ..[*] */
               | LEFT_PAREN <argument_list>? RIGHT_PAREN

<bracket_selector> ::= MULTIPLY
                      /* 通配 *：List/数组全部元素、Map 全部 value、Bean 全部属性值 */
                      | QUESTION LEFT_PAREN <expression> RIGHT_PAREN
                      /* 过滤 ?(expr)：谓词内可用 @ 引用当前元素；List/数组逐元素、Map 按 value */
                      | <expression>
                      /* 下标/键：List/数组数字下标（支持负数，-1 为末元素）、Map 任意键 */
                      | <expression>? COLON <expression>? ( COLON <expression>? )?
                      /* 切片 [start:end(:step)]：start 含、end 不含，均可省略，支持负数；
                         step 省略默认 1，负数反向取值，0 结果为空 */

<argument_list> ::= <expression> ( COMMA <expression> )*

### 5.5 类型转换表达式 (Cast Expression)

类型转换表达式将值转换为指定类型。

**语法：**
```bnf
<cast_expression> ::= LEFT_PAREN IDENTIFIER RIGHT_PAREN <unary_expression>
```

**示例：**
```
{(int) 3.14}        // 结果: 3
{(long) "100"}      // 结果: 100L
{(String) 123}      // 结果: "123"
{(int)(double) "3.99"}  // 链式转换，结果: 3
```

**支持的类型：**
- 基本类型：`int`, `long`, `double`, `float`, `short`, `byte`, `char`, `boolean`
- 包装类型：`Integer`, `Long`, `Double`, `Float`, `Short`, `Byte`, `Character`, `Boolean`
- 特殊类型：`String`, `BigDecimal`, `BigInteger`
- 导入类型：通过 `{% import %}` 导入的类别名

**null 值处理：**
- 对 `null` 进行任何类型转换，结果都是 `null`

**优先级：**
- 类型转换与一元运算符 `!`, `-`, `+` 同级
- `(int) -x` 解析为 `(int)(-x)` — 先取负，再转换
- `(int) x + 1` 解析为 `((int) x) + 1` — 先转换，再加

**歧义消解：**
当遇到 `(identifier)` 时：
1. 如果 `identifier` 后紧跟 `)` 且 `)` 后是表达式开始，则识别为类型转换
2. 否则识别为括号表达式

<primary_expression> ::= <literal>
                       | <argument_access>
                       | <argument_variable>
                       | IDENTIFIER
                       | AT                        /* JSONPath 当前节点 @（仅过滤表达式 [?(...)] 内有效） */
                       | LEFT_PAREN <expression> RIGHT_PAREN

<argument_access> ::= NUMBER
/* 参数访问：在表达式内部，纯数字字面量表示访问外部传入的位置参数
 * 例如：{0} 访问第一个参数，${1} 访问第二个参数，{{2}} 访问第三个参数
 * 注意：这是在 primary_expression 层面的特殊处理，仅当数字独立出现时才视为参数访问
 * 注意：索引超出范围或参数数组不存在时抛出异常
 */

<argument_variable> ::= DOLLAR_NUMBER
/* 参数变量引用：$后跟数字，表示变量式参数引用
 * 例如：{$0} 访问第一个参数，${$1} 访问第二个参数，{{$2}} 访问第三个参数
 * 与 argument_access 不同，支持与其他运算符组合使用（如 ??）
 * 注意：索引超出范围或值为 null 时抛出异常
 */

/* 默认参数名 $：$ 后不跟数字时按 IDENTIFIER "$" 处理（词法层无特殊规则）
 * 语义层对名称 "$" 特判：
 *   1. 可见作用域链中存在名为 $ 的变量（context.put("$", v)、let $ = ...、Lambda 参数名 $）→ 使用变量值（覆盖）
 *   2. 否则等价 $0，取最近参数作用域的第一个实参，严格语义与 $0 一致（缺失/越界/值为 null 时抛出异常）
 */
```

### 5.4 参数访问 (Argument Access)

参数访问允许通过索引获取外部传入的位置参数：

#### 简单参数访问 `{n}`

- 语法：在表达式内使用纯数字，如 `{0}`、`${1}`、`{{2}}`
- 索引从 0 开始，`{0}` 表示第一个参数
- 索引超出范围或参数数组不存在时抛出 `TemplateRuntimeException`
- 参数值为 `null` 时允许渲染，输出空字符串

#### 参数变量引用 `{$n}`

- 语法：`$` 后跟数字，如 `{$0}`、`${$1}`、`{{$2}}`
- 索引从 0 开始，`{$0}` 表示第一个参数
- 索引超出范围或参数数组不存在时抛出 `TemplateRuntimeException`
- 参数值为 `null` 时抛出 `TemplateRuntimeException`
- 支持 `??` 运算符处理 `null` 值

#### 默认参数名 `{$}`

- 语法：单独的 `$`，如 `{$}`、`${$}`、`{{$}}`
- 词法上 `$` 后不跟数字时按 `IDENTIFIER "$"` 处理，语义层对该名称特判
- 默认等价 `{$0}`：取最近参数作用域的第一个实参，严格语义与 `{$0}` 一致（缺失/越界/值为 null 时抛出 `TemplateRuntimeException`）
- 覆盖规则：可见作用域中存在名为 `$` 的变量（`context.put("$", v)`、`{% let $ = ... %}`、Lambda 参数名 `$`）时优先使用变量值，不再回退到第一个参数
- Lambda 语义：Lambda 调用时将自身入参设为参数作用域，Lambda 体内 `$`（以及 `$0`、`{0}`）引用 Lambda 自身入参而非模板参数

**示例：**
```
简单参数访问：
姓名：{0}，年龄：{1}

参数变量引用：
姓名：{$0}，年龄：{$1}

默认参数名（等价 {$0}）：
姓名：{$}

覆盖默认参数名：
{% let $ = '未知' %}{$}

Lambda 中的默认参数名：
{% let first = (a, b) -> $ %}{first('x', 'y')}

使用 ?? 处理 null：
{$0 ?? '未知'}

混合使用：
{0}-{$1}-{2}
```

**Java 调用：**
```java
Context context = new Context();
context.setArguments("张三", 25);
// 模板 "姓名：{0}，年龄：{1}" 渲染结果："姓名：张三，年龄：25"

// 使用 ?? 运算符处理 null
Context context2 = new Context();
context2.setArguments((Object) null);
// 模板 "{$0 ?? '未知'}" 渲染结果："未知"
```

## 6. 字面量 (Literals)

```bnf
<literal> ::= <string_literal>
            | <template_string_literal>
            | <number_literal>
            | <boolean_literal>
            | <null_literal>
            | <list_literal>
            | <map_literal>
            | <regex_literal>

<string_literal> ::= STRING

<template_string_literal> ::= BACKTICK ( TEXT_PART | <single_brace_expr> )* BACKTICK

<number_literal> ::= NUMBER | LONG | BIG_INTEGER | BIG_DECIMAL

<boolean_literal> ::= TRUE | FALSE

<null_literal> ::= NULL

<list_literal> ::= LEFT_BRACKET ( <expression> ( COMMA <expression> )* )? RIGHT_BRACKET

<map_literal> ::= LEFT_BRACE ( <map_entry> ( COMMA <map_entry> )* )? RIGHT_BRACE

<map_entry> ::= <map_key> COLON <expression>

<map_key> ::= IDENTIFIER
            | STRING
            | NUMBER

<regex_literal> ::= REGEX
```

## 7. 歧义处理 (Ambiguity Handling)

### 7.1 花括号 `{}` 歧义

花括号 `{}` 在 EasyTL 中有多种用途：
1. **单花括号表达式**：`{expr}`
2. **哈希表字面量**：`{key: value}`
3. **代码块**：`if (...) { ... }`
4. **语句块标记**：`{% ... %}`

**处理规则：**
- 在 Lexer 层面，根据上下文切换状态。
- 在 Parser 层面：
  - 当解析表达式时，如果遇到 `LEFT_BRACE`：
    - 如果后续跟随 `key :` (标识符/字符串/数字 + 冒号)，则解析为 **哈希表字面量**。
    - 否则，如果是在 `structured_expression` 的解析上下文中，解析为 **代码块**。
    - 注意：单花括号表达式在 Lexer 层面已被识别为 `SINGLE_BRACE_EXPR_BEGIN`，不使用 `LEFT_BRACE`。
  - `{%` 在 Lexer 层面识别为 `STMT_BEGIN`。

### 7.2 关键字作为标识符

关键字（如 `if`, `for`, `in` 等）在特定上下文中可以作为普通标识符使用（如对象属性名）。

**处理规则：**
- 在解析成员访问（`obj.key`）或哈希表键时，允许使用关键字作为 `IDENTIFIER`。
- 在表达式开始处，关键字优先被识别为语法结构（如 `if` 表达式）。

### 7.3 减号 `-` 歧义

减号 `-` 可以是减法运算符，也可以是负号（一元运算符）。

**处理规则：**
- 取决于位置：
  - 如果出现在操作数之后（如 `a - b`），是减法。
  - 如果出现在表达式开头或操作符之后（如 `-a`, `a * -b`），是负号。
- 在 BNF 中通过分层结构体现（`additive_expression` vs `unary_expression`）。

### 7.4 字符串模板中的花括号

字符串模板 `` `...` `` 中可以嵌入 `{expr}`。

**处理规则：**
- Lexer 在遇到反引号后进入 `TEMPLATE_STRING` 模式。
- 在该模式下，遇到 `{` 进入表达式模式，遇到 `}` 退出表达式模式回到字符串模板模式。
- Lexer 输出一系列 Token，Parser将其组装为 `TemplateLiteral` 节点。

### 7.5 赋值约束

赋值表达式 `let x = if (...) ...` 必须保证有返回值。

**处理规则：**
- 在解析 `let` 语句的赋值部分时，如果右侧是 `if_expression` 或 `switch_expression`，Parser 必须检查其是否包含 `else` 或 `default` 分支。如果不包含，抛出编译错误。

### 7.6 `..` 歧义（递归下降 vs 区间字面量）

`..`（RANGE_CLOSED）既表示区间字面量 `a..b`，也表示 JSONPath 递归下降 `..name` / `..*` / `..[...]`，存在二义。

**处理规则：**
- 在 `parsePostfix` 中：
  - `..` 后紧跟 `IDENTIFIER`（即成员名），当且仅当左值是路径类节点（`MemberAccess`、`IndexAccess`、`MethodCall` 或 JSONPath 节点（`CurrentNode`、`WildcardAccess`、`SliceAccess`、`FilterAccess`、`RecursiveDescentAccess`、`UnionAccess`））时，解析为 **递归下降 `..name`**；
  - `..` 后紧跟 `*` 或 `[` 且左值是路径类节点或 `IDENTIFIER` 时，解析为 **递归下降 `..*` / `..[...]`**（这两种形态本就不构成合法区间，无歧义；标识符左值使 `store..*` 可用）；
  - 否则回落到 `parseRange`，按 **区间字面量** 解析。
- 因此 `{a..b}`、`{1..10}`、`{% for i in 1..10 %}`（左值为标识符或字面量，且后跟标识符/数字）保持区间语义不变。

**已知边界：**
- `user.min..user.max`（`..` 左值是路径类节点且后跟标识符）会被解析为递归下降（`..user` 再 `.max`），而非区间。如需以路径表达式为端点的区间，先用 `let` 绑定端点：

```
{% let min = user.min %}
{% let max = user.max %}
{min..max}
```
