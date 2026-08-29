# EasyTL 测试用例覆盖设计

## 1. 概述

本文档定义了 EasyTL 模板引擎测试用例的全面覆盖方案，旨在系统性地覆盖所有已实现的语法特性。

### 1.1 目标

- 覆盖 `BNF_GRAMMAR.md` 中定义的所有语法规则
- 覆盖 `README.md` 中描述的所有语法特性
- 覆盖代码中实际实现的所有语法功能
- 补充边界条件、类型转换、错误处理等边缘情况测试

### 1.2 范围

| 维度 | 描述 |
|------|------|
| 边界条件 | null 值、空集合、数值边界、索引越界 |
| 集成场景 | 复杂嵌套结构、特性组合、真实世界模式 |
| 语法完整性 | 每个 BNF 规则至少有一个测试用例 |

---

## 2. 测试结构

### 2.1 目录结构

```
src/test/java/org/dromara/easytl/
├── parser/
│   ├── LexerTest.java          [现有] 扩展：词法分析边界情况
│   ├── ParserTest.java         [现有] 扩展：语法分析边界情况
│   └── SyntaxErrorTest.java    [现有] 扩展：语法错误处理
├── runtime/
│   ├── OperatorEvaluatorTest.java  [现有] 扩展：类型转换测试
│   └── RangeTest.java              [现有] 扩展：区间边界情况
├── GrammarCoverageTest.java    [新增] BNF 语法系统性覆盖
├── EdgeCaseTest.java           [新增] 边界条件与错误处理
└── IntegrationScenarioTest.java [新增] 复杂集成场景
```

**图例说明:**
- `[现有]` 表示已存在的测试文件，将进行扩展
- `[新增]` 表示需要新建的测试文件

### 2.2 命名规范

- 正常路径: `test<Feature>_<Scenario>`
- 异常情况: `test<Feature>_<Scenario>_throwsException`
- 特定返回: `test<Feature>_<Scenario>_returns<Expected>`

示例:
- `testTernaryExpression_nestedInElvis_returnsValue`
- `testArgumentAccess_indexOutOfBounds_throwsException`

---

## 3. GrammarCoverageTest 设计

基于 `BNF_GRAMMAR.md` 和 README，系统性覆盖每个语法规则。

### 3.1 测试分类

#### 3.1.1 模板结构 (Section 3)

| 测试场景 | 测试方法 |
|----------|----------|
| 单花括号表达式 | `testSingleBraceExpression_basic` |
| 双花括号表达式 | `testDoubleBraceExpression_basic` |
| 美元花括号表达式 | `testDollarBraceExpression_basic` |
| 三种语法混合 | `testMixedExpressionWrappers` |
| 纯文本模板 | `testPlainTextOnly` |

#### 3.1.2 语句块 (Section 4)

**If 语句**

| 测试场景 | 测试方法 |
|----------|----------|
| 简单 if | `testIfStatement_simple` |
| if-else | `testIfStatement_withElse` |
| if-else if-else | `testIfStatement_withElseIfAndElse` |
| 多个 else if | `testIfStatement_multipleElseIf` |
| 嵌套 if | `testIfStatement_nested` |
| if 条件为表达式 | `testIfStatement_withExpressionCondition` |

**For 语句**

| 测试场景 | 测试方法 |
|----------|----------|
| 简单 for | `testForStatement_simple` |
| for 带索引 | `testForStatement_withIndex` |
| for 遍历区间 | `testForStatement_withRange` |
| for 遍历列表 | `testForStatement_withList` |
| 嵌套 for | `testForStatement_nested` |
| for 遍历 Map | `testForStatement_withMap` |

**Switch 语句**

| 测试场景 | 测试方法 |
|----------|----------|
| 简单 switch | `testSwitchStatement_simple` |
| switch 多 case | `testSwitchStatement_multipleCases` |
| switch 带 default | `testSwitchStatement_withDefault` |
| switch case 为表达式 | `testSwitchStatement_caseExpression` |
| switch 字符串匹配 | `testSwitchStatement_stringCase` |

**Let 语句**

| 测试场景 | 测试方法 |
|----------|----------|
| let 数字值 | `testLetStatement_numberValue` |
| let 字符串值 | `testLetStatement_stringValue` |
| let 表达式值 | `testLetStatement_expressionValue` |
| let 列表值 | `testLetStatement_listValue` |
| let map 值 | `testLetStatement_mapValue` |
| let lambda 值 | `testLetStatement_lambdaValue` |
| 多个 let | `testLetStatement_multiple` |

**Extends 语句**

| 测试场景 | 测试方法 |
|----------|----------|
| extends 基础模板 | `testExtendsStatement_basic` |
| extends 带变量传递 | `testExtendsStatement_withVariables` |
| extends 多层继承 | `testExtendsStatement_multiLevel` |

**Import 语句**

| 测试场景 | 测试方法 |
|----------|----------|
| import 无别名 | `testImportStatement_withoutAlias` |
| import 带别名 | `testImportStatement_withAlias` |
| import 调用宏 | `testImportStatement_callMacro` |

#### 3.1.3 结构化表达式 (Section 5.1)

| 测试场景 | 测试方法 |
|----------|----------|
| if 表达式 | `testIfExpression_basic` |
| if 表达式带 else | `testIfExpression_withElse` |
| if 表达式多 else if | `testIfExpression_multipleElseIf` |
| for 表达式 | `testForExpression_basic` |
| for 表达式带副作用 | `testForExpression_withSideEffect` |
| switch 表达式 | `testSwitchExpression_basic` |
| switch 表达式带 default | `testSwitchExpression_withDefault` |
| switch 表达式表达式 case | `testSwitchExpression_expressionCase` |
| 结构化表达式赋值 | `testStructuredExpression_inLet` |

#### 3.1.4 运算符优先级 (Section 5.2)

**优先级 1: Elvis `??`**

| 测试场景 | 测试方法 |
|----------|----------|
| 简单 elvis | `testElvisOperator_basic` |
| elvis 链 | `testElvisOperator_chain` |
| elvis 与 null | `testElvisOperator_withNull` |

**优先级 2: 三元 `? :`**

| 测试场景 | 测试方法 |
|----------|----------|
| 简单三元 | `testTernaryOperator_basic` |
| 嵌套三元 | `testTernaryOperator_nested` |
| 三元与表达式 | `testTernaryOperator_withExpressions` |

**优先级 3-4: 逻辑运算 `||`, `&&`**

| 测试场景 | 测试方法 |
|----------|----------|
| 逻辑或 | `testLogicalOr_basic` |
| 逻辑与 | `testLogicalAnd_basic` |
| 短路求值 | `testLogicalOperator_shortCircuit` |
| 混合逻辑 | `testLogicalOperator_mixed` |

**优先级 5: 相等性 `==`, `!=`, `=~`, `!=~`, `in`, `!in`**

| 测试场景 | 测试方法 |
|----------|----------|
| 相等比较 | `testEqualityOperator_equals` |
| 不等比较 | `testEqualityOperator_notEquals` |
| 正则匹配 | `testRegexMatchOperator_basic` |
| 正则不匹配 | `testRegexNotMatchOperator_basic` |
| in 运算符 | `testInOperator_list` |
| in 运算符字符串 | `testInOperator_string` |
| !in 运算符 | `testNotInOperator_basic` |

**优先级 6: 关系运算 `<`, `<=`, `>`, `>=`**

| 测试场景 | 测试方法 |
|----------|----------|
| 小于比较 | `testRelationalOperator_lessThan` |
| 小于等于 | `testRelationalOperator_lessThanOrEqual` |
| 大于比较 | `testRelationalOperator_greaterThan` |
| 大于等于 | `testRelationalOperator_greaterThanOrEqual` |
| 字符串比较 | `testRelationalOperator_stringComparison` |

**优先级 7: 区间 `..`, `..<`, `>..`, `>..<`**

| 测试场景 | 测试方法 |
|----------|----------|
| 闭区间 | `testRangeOperator_closed` |
| 左闭右开 | `testRangeOperator_leftClosedRightOpen` |
| 左开右闭 | `testRangeOperator_leftOpenRightClosed` |
| 开区间 | `testRangeOperator_open` |
| 负数区间 | `testRangeOperator_negativeNumbers` |

**优先级 8-9: 算术运算 `+`, `-`, `*`, `/`, `%`**

| 测试场景 | 测试方法 |
|----------|----------|
| 加法 | `testArithmeticOperator_addition` |
| 减法 | `testArithmeticOperator_subtraction` |
| 乘法 | `testArithmeticOperator_multiplication` |
| 除法 | `testArithmeticOperator_division` |
| 取模 | `testArithmeticOperator_modulo` |
| 负数运算 | `testArithmeticOperator_negativeNumbers` |

**优先级 10: 一元运算 `!`, `-`, `+`**

| 测试场景 | 测试方法 |
|----------|----------|
| 逻辑非 | `testUnaryOperator_not` |
| 负号 | `testUnaryOperator_negative` |
| 正号 | `testUnaryOperator_positive` |
| 双重否定 | `testUnaryOperator_doubleNegation` |

**优先级 11: 后缀运算 `.`, `?.`, `[]`, `()`**

| 测试场景 | 测试方法 |
|----------|----------|
| 成员访问 | `testPostfixOperator_memberAccess` |
| 安全导航 | `testPostfixOperator_safeNavigation` |
| 索引访问 | `testPostfixOperator_indexAccess` |
| 方法调用 | `testPostfixOperator_methodCall` |
| 链式调用 | `testPostfixOperator_chained` |
| 安全导航链 | `testPostfixOperator_safeNavigationChain` |

#### 3.1.5 参数访问 (Section 5.4)

| 测试场景 | 测试方法 |
|----------|----------|
| 简单参数访问 `{n}` | `testArgumentAccess_simpleIndex` |
| 参数变量引用 `{$n}` | `testArgumentVariable_dollarSyntax` |
| 三种语法混合 | `testArgumentAccess_mixedSyntaxes` |
| 参数与变量混合 | `testArgumentAccess_withVariables` |
| 参数越界 | `testArgumentAccess_outOfBounds_throwsException` |
| 参数 null 值 | `testArgumentAccess_withNullValue` |
| 参数变量 elvis | `testArgumentVariable_withElvis` |

#### 3.1.6 字面量 (Section 6)

**字符串字面量**

| 测试场景 | 测试方法 |
|----------|----------|
| 单引号字符串 | `testStringLiteral_singleQuote` |
| 双引号字符串 | `testStringLiteral_doubleQuote` |
| 转义字符 | `testStringLiteral_escapeSequences` |
| 空字符串 | `testStringLiteral_empty` |
| Unicode 字符 | `testStringLiteral_unicode` |

**模板字符串**

| 测试场景 | 测试方法 |
|----------|----------|
| 基本模板字符串 | `testTemplateString_basic` |
| 嵌套表达式 | `testTemplateString_withExpression` |
| 多行模板字符串 | `testTemplateString_multiline` |

**数字字面量**

| 测试场景 | 测试方法 |
|----------|----------|
| 整数 | `testNumberLiteral_integer` |
| 长整数 | `testNumberLiteral_long` |
| 浮点数 | `testNumberLiteral_float` |
| 科学计数法 | `testNumberLiteral_scientificNotation` |
| BigDecimal | `testNumberLiteral_bigDecimal` |
| 负数 | `testNumberLiteral_negative` |
| 零 | `testNumberLiteral_zero` |

**布尔与 null 字面量**

| 测试场景 | 测试方法 |
|----------|----------|
| true | `testBooleanLiteral_true` |
| false | `testBooleanLiteral_false` |
| null | `testNullLiteral_basic` |

**列表字面量**

| 测试场景 | 测试方法 |
|----------|----------|
| 空列表 | `testListLiteral_empty` |
| 数字列表 | `testListLiteral_numbers` |
| 字符串列表 | `testListLiteral_strings` |
| 混合类型列表 | `testListLiteral_mixedTypes` |
| 嵌套列表 | `testListLiteral_nested` |

**Map 字面量**

| 测试场景 | 测试方法 |
|----------|----------|
| 空 map | `testMapLiteral_empty` |
| 标识符键 | `testMapLiteral_identifierKeys` |
| 字符串键 | `testMapLiteral_stringKeys` |
| 数字键 | `testMapLiteral_numberKeys` |
| 嵌套 map | `testMapLiteral_nested` |
| 表达式值 | `testMapLiteral_expressionValues` |

**正则字面量**

| 测试场景 | 测试方法 |
|----------|----------|
| 简单正则 | `testRegexLiteral_basic` |
| 正则带标志 | `testRegexLiteral_withFlags` |
| 复杂正则 | `testRegexLiteral_complexPattern` |

#### 3.1.7 Lambda 表达式

Lambda 表达式是 EasyTL 的重要特性，需要专门覆盖。

**基础 Lambda 语法**

| 测试场景 | 测试方法 |
|----------|----------|
| 无参 Lambda | `testLambda_noParams` |
| 单参 Lambda 无括号 | `testLambda_singleParamNoParens` |
| 单参 Lambda 有括号 | `testLambda_singleParamWithParens` |
| 多参 Lambda | `testLambda_multiParams` |

**Lambda 代码块**

| 测试场景 | 测试方法 |
|----------|----------|
| 块体 Lambda | `testLambda_blockBody` |
| 块体 Lambda 带 return | `testLambda_blockBodyWithReturn` |
| 空块 Lambda | `testLambda_emptyBlock` |

**Lambda 闭包**

| 测试场景 | 测试方法 |
|----------|----------|
| Lambda 捕获外部变量 | `testLambda_captureVariable` |
| Lambda 捕获后变量修改 | `testLambda_captureByValue` |
| Lambda 与外部变量同名 | `testLambda_shadowing` |

**Lambda 组合使用**

| 测试场景 | 测试方法 |
|----------|----------|
| Lambda 作为方法参数 | `testLambda_asMethodArgument` |
| Lambda 存储在列表中 | `testLambda_inList` |
| Lambda 存储在 map 中 | `testLambda_inMap` |
| 嵌套 Lambda | `testLambda_nested` |
| Lambda 条件体 | `testLambda_conditionalBody` |

#### 3.1.8 动态变量 (TemplateVariable)

动态变量允许在渲染时动态获取值，参考 README.md Section 9。

| 测试场景 | 测试方法 |
|----------|----------|
| 动态变量基础用法 | `testDynamicVariable_basic` |
| 动态变量访问其他变量 | `testDynamicVariable_accessOtherVariables` |
| 动态变量多次访问 | `testDynamicVariable_multipleAccess` |
| 动态变量异常处理 | `testDynamicVariable_exceptionHandling` |
| 动态变量与 Context | `testDynamicVariable_withContext` |

### 3.2 预估测试数量

| 分类 | 数量 |
|------|------|
| 模板结构 | 5 |
| 语句块 (if/for/switch/let) | 24 |
| 语句块 (extends/import) | 6 |
| 结构化表达式 | 9 |
| 运算符 | 35 |
| 参数访问 | 7 |
| 字面量 | 22 |
| Lambda 表达式 | 15 |
| 动态变量 | 5 |
| **总计** | **~128** |

---

## 4. EdgeCaseTest 设计

### 4.1 边界条件

| 测试场景 | 测试方法 |
|----------|----------|
| null 在表达式中 | `testNullInExpression_basic` |
| null 比较 | `testNullComparison_equals` |
| null 算术 | `testNullArithmetic_throwsException` |
| 空列表操作 | `testEmptyList_operations` |
| 空 map 操作 | `testEmptyMap_operations` |
| 空字符串 | `testEmptyString_operations` |
| Integer 最大值 | `testIntegerMaxValue_arithmetic` |
| Long 最大值 | `testLongMaxValue_arithmetic` |
| 负数边界 | `testNegativeNumbers_boundary` |
| 零除 | `testDivisionByZero` |
| 列表首元素 | `testListIndex_firstElement` |
| 列表末元素 | `testListIndex_lastElement` |
| 列表越界 | `testListIndex_outOfBounds_throwsException` |
| 负索引 | `testListIndex_negative` |

### 4.2 类型转换

| 测试场景 | 测试方法 |
|----------|----------|
| int + long | `testTypeCoercion_intPlusLong_returnsLong` |
| int * double | `testTypeCoercion_intTimesDouble_returnsDouble` |
| BigDecimal 运算 | `testTypeCoercion_bigDecimalOperations` |
| 字符串 + 数字 | `testTypeCoercion_stringPlusNumber_returnsString` |
| 字符串 + 布尔 | `testTypeCoercion_stringPlusBoolean_returnsString` |
| 跨类型比较 | `testTypeCoercion_crossTypeComparison` |
| 数值字符串比较 | `testTypeCoercion_numericStringComparison` |

### 4.3 运算符优先级验证

| 测试场景 | 测试方法 |
|----------|----------|
| 乘法优先于加法 | `testPrecedence_multiplicationBeforeAddition` |
| 逻辑与优先于或 | `testPrecedence_andBeforeOr` |
| Elvis 右结合 | `testPrecedence_elvisRightAssociative` |
| 三元右结合 | `testPrecedence_ternaryRightAssociative` |
| 减法左结合 | `testPrecedence_subtractionLeftAssociative` |
| 复合表达式 | `testPrecedence_complexExpression` |

### 4.4 特殊字符处理

| 测试场景 | 测试方法 |
|----------|----------|
| 换行转义 | `testEscapeSequences_newline` |
| 制表转义 | `testEscapeSequences_tab` |
| 引号转义 | `testEscapeSequences_quotes` |
| 反斜杠转义 | `testEscapeSequences_backslash` |
| 正则斜杠转义 | `testRegexEscape_slash` |
| 正则字符类 | `testRegexEscape_characterClass` |

### 4.5 错误处理

所有异常测试应验证错误消息包含关键信息。

| 测试场景 | 测试方法 | 预期错误消息包含 |
|----------|----------|------------------|
| 未闭合花括号 | `testSyntaxError_unclosedBrace_throwsException` | "unclosed" 或 "expected" |
| 无效运算符 | `testSyntaxError_invalidOperator_throwsException` | "unexpected" 或 "invalid" |
| 未定义变量 | `testRuntimeError_undefinedVariable` | 变量名 |
| null 方法调用 | `testRuntimeError_nullMethodCall` | "null" 或 方法名 |
| 方法未找到 | `testRuntimeError_methodNotFound` | 方法名 |
| 无效操作数类型 | `testRuntimeError_invalidOperandTypes` | 类型名或运算符 |
| 参数索引越界 | `testRuntimeError_argumentIndexOutOfBounds` | "out of bounds" |
| Lambda 参数变量 null | `testRuntimeError_argumentVariableNull` | "null" 或 索引 |

**错误消息验证示例:**
```java
@Test
void testArgumentAccess_outOfBounds_throwsException() {
    Template template = engine.compile("Value: {5}");
    Context context = new Context();
    context.setArguments("only-one");

    ArgumentIndexOutOfBoundsException ex = assertThrows(
        ArgumentIndexOutOfBoundsException.class,
        () -> template.render(context)
    );
    assertTrue(ex.getMessage().contains("out of bounds"));
}
```

### 4.6 预估测试数量

| 分类 | 数量 |
|------|------|
| 边界条件 | 15 |
| 类型转换 | 12 |
| 运算符优先级 | 10 |
| 特殊字符 | 8 |
| 错误处理 | 20 |
| **总计** | **~65** |

---

## 5. IntegrationScenarioTest 设计

### 5.1 嵌套结构

| 测试场景 | 测试方法 |
|----------|----------|
| for 嵌套 for | `testNestedFor_doubleLoop` |
| for 内嵌 if | `testNestedFor_withIf` |
| if 内嵌 for | `testNestedIf_withFor` |
| if 嵌套 if | `testNestedIf_doubleIf` |
| switch 内嵌 for | `testNestedSwitch_withFor` |
| switch 内嵌 if | `testNestedSwitch_withIf` |
| 三层嵌套 | `testDeepNesting_threeLevels` |

### 5.2 特性组合

| 测试场景 | 测试方法 |
|----------|----------|
| let 与复杂表达式 | `testCombination_letWithComplexExpression` |
| 表达式与方法调用 | `testCombination_expressionWithMethodCall` |
| Lambda 在 for 中 | `testCombination_lambdaInFor` |
| Lambda 闭包 | `testCombination_lambdaWithClosure` |
| Lambda 作为参数 | `testCombination_lambdaAsArgument` |
| 参数与变量混合 | `testCombination_argumentWithVariables` |
| 区间在 for 中 | `testCombination_rangeInFor` |
| 区间比较 | `testCombination_rangeComparison` |

### 5.3 数据结构场景

| 测试场景 | 测试方法 |
|----------|----------|
| 嵌套 map | `testNestedMap_access` |
| map 值为列表 | `testMapWithListValues_access` |
| map 操作 | `testMapOperations_basic` |
| 列表嵌套 map | `testListOfMaps_iteration` |
| 列表过滤模式 | `testListFiltering_pattern` |
| 深层属性链 | `testDeepPropertyChain_access` |
| 安全导航链 | `testSafeNavigationChain_access` |
| 方法调用链 | `testMethodCallChain_access` |

### 5.4 实际应用模式

| 测试场景 | 测试方法 |
|----------|----------|
| 多条件显示 | `testConditionalRendering_multipleConditions` |
| 交替样式列表 | `testLoopRendering_alternatingStyles` |
| 计算值 | `testDynamicContent_calculatedValues` |
| 格式化输出 | `testDynamicContent_formattedOutput` |
| 回退值模式 | `testErrorRecovery_fallbackValues` |
| null 安全模式 | `testErrorRecovery_nullSafePattern` |

### 5.5 预估测试数量

| 分类 | 数量 |
|------|------|
| 嵌套结构 | 12 |
| 特性组合 | 15 |
| 数据结构场景 | 10 |
| 实际应用模式 | 12 |
| **总计** | **~55** |

---

## 6. 现有测试类扩展

### 6.1 LexerTest 扩展

| 测试方法 | 描述 |
|----------|------|
| `testNumberLiteral_maxInteger` | Integer.MAX_VALUE 词法分析 |
| `testNumberLiteral_maxLong` | Long.MAX_VALUE 词法分析 |
| `testNumberLiteral_negativeZero` | -0 词法分析 |
| `testStringLiteral_unicodeEscape` | Unicode 转义序列 |
| `testStringLiteral_escapedQuotes` | 嵌套引号转义 |
| `testRegexLiteral_complexPattern` | 复杂正则模式 |
| `testRegexLiteral_withAllFlags` | 正则所有标志组合 |
| `testMultipleExpressionsOnSameLine` | 同行多表达式 |
| `testExpressionAtLineStart` | 行首表达式 |
| `testExpressionAtLineEnd` | 行尾表达式 |
| `testExpressionWithOnlyWhitespace` | 仅空白表达式 |
| `testMixedExpressionSyntaxesInSameTemplate` | 三种语法混合 |

### 6.2 ParserTest 扩展

| 测试方法 | 描述 |
|----------|------|
| `testDeepNestedBinaryExpression` | 深层嵌套二元表达式 |
| `testMultipleTernaryInSameExpression` | 同表达式多三元 |
| `testElvisChain` | Elvis 链解析 |
| `testSafeNavigationChain` | 安全导航链解析 |
| `testMethodCallWithLambdaArgument` | Lambda 作为方法参数 |
| `testIfExpression_inLetStatement` | if 表达式在 let 中 |
| `testSwitchExpression_withExpressionCase` | switch 表达式 case |
| `testForExpression_withSideEffect` | for 表达式副作用 |
| `testLambdaExpression_noParams` | 无参 Lambda |
| `testLambdaExpression_singleParamNoParens` | 单参无括号 Lambda |
| `testLambdaExpression_multiParams` | 多参 Lambda |
| `testLambdaExpression_blockBody` | 块体 Lambda |
| `testLambdaExpression_nested` | 嵌套 Lambda |

### 6.3 TemplateEngineTest 扩展

| 测试方法 | 描述 |
|----------|------|
| `testRender_withLargeContext` | 大量上下文变量 |
| `testRender_withDeepNestedContext` | 深层嵌套上下文 |
| `testRender_unicodeContent` | Unicode 内容渲染 |
| `testRender_multilineTemplate` | 多行模板渲染 |
| `testArgumentAccess_withNullInArray` | 参数数组含 null |
| `testArgumentAccess_allThreeSyntaxes` | 三种参数访问语法 |
| `testArgumentVariable_withElvisOperator` | 参数变量与 Elvis |

### 6.4 OperatorEvaluatorTest 扩展

| 测试方法 | 描述 |
|----------|------|
| `testAddition_intAndLong_returnsLong` | int + long 返回 long |
| `testAddition_intAndDouble_returnsDouble` | int + double 返回 double |
| `testAddition_stringAndNumber_returnsString` | 字符串 + 数字返回字符串 |
| `testComparison_differentNumericTypes` | 不同数值类型比较 |
| `testElvis_withNull_returnsAlternative` | null ?? 返回替代值 |
| `testElvis_withNonNull_returnsValue` | 非null ?? 返回原值 |
| `testSafeNavigation_withNullTarget_returnsNull` | null?. 返回 null |
| `testRegexMatch_withMatch_returnsTrue` | 正则匹配返回 true |

### 6.5 预估测试数量

| 类 | 数量 |
|------|------|
| LexerTest | 12 |
| ParserTest | 13 |
| TemplateEngineTest | 7 |
| OperatorEvaluatorTest | 8 |
| **总计** | **~40** |

---

## 7. 执行策略

### 7.1 运行命令

```bash
# 运行所有测试
mvn test

# 运行新增测试类
mvn test -Dtest=GrammarCoverageTest
mvn test -Dtest=EdgeCaseTest
mvn test -Dtest=IntegrationScenarioTest

# 运行特定测试方法
mvn test -Dtest=GrammarCoverageTest#testIfStatement_withElseIfAndElse
```

### 7.2 测试优先级

| 优先级 | 类 | 原因 |
|--------|-----|------|
| P0 | GrammarCoverageTest | 核心语法正确性 |
| P0 | EdgeCaseTest | 稳定性与错误处理 |
| P1 | IntegrationScenarioTest | 真实场景可用性 |
| P1 | LexerTest 扩展 | 词法分析准确性 |
| P1 | ParserTest 扩展 | AST 正确性 |
| P2 | TemplateEngineTest 扩展 | 端到端验证 |
| P2 | OperatorEvaluatorTest 扩展 | 运行时行为 |

### 7.3 验收标准

1. **所有测试通过**: `mvn test` 返回成功
2. **语法规则覆盖**: 新测试覆盖所有 BNF 语法规则
3. **无重复测试**: 避免与现有测试重复
4. **命名一致**: 遵循 `test<Feature>_<Scenario>` 规范

---

## 8. 总结

### 8.1 测试数量统计

| 测试类 | 数量 |
|--------|------|
| GrammarCoverageTest (新增) | ~128 |
| EdgeCaseTest (新增) | ~65 |
| IntegrationScenarioTest (新增) | ~55 |
| 现有类扩展 | ~40 |
| **总计** | **~288** |

### 8.2 覆盖维度

- ✅ 边界条件 (null, 空值, 数值边界, 索引越界)
- ✅ 集成场景 (嵌套结构, 特性组合, 真实模式)
- ✅ 语法完整性 (每个 BNF 规则)
- ✅ 类型转换 (跨类型运算)
- ✅ 错误处理 (语法错误, 运行时错误)
- ✅ Lambda 表达式 (语法, 闭包, 组合使用)
- ✅ 动态变量 (TemplateVariable)
- ✅ 模板继承与导入 (extends, import)