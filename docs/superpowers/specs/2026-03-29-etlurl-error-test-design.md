# EtlURL 错误信息测试用例设计

## 概述

为 `EtlURLLazyEvaluationTest.java` 添加 URL 报错信息测试用例，确保所有错误消息包含正确的源码位置定位信息。

## 背景

EasyTL 模板引擎在运行时错误报告中采用 Rust 风格的错误格式，包含：
- 错误类型（如 "Runtime error"）
- 错误消息
- 源码位置（行号、列号）
- 可视化指针（`^` 标记）

现有 `EtlURLLazyEvaluationTest.java` 只测试了延迟计算的正确场景，缺少错误情况的测试覆盖。

## 错误类别覆盖

### 1. 未定义变量错误

**场景**：URL 中引用未定义的变量

| 测试用例 | URL 表达式 | 预期错误位置 |
|---------|-----------|-------------|
| 单个未定义变量 | `http://host/{undefinedVar}` | `{undefinedVar}` 起始列 |
| 嵌入路径中的未定义变量 | `http://host/{basePath}/users` | `{basePath}` 起始列 |
| 多变量中的第一个错误 | `{scheme}://{host}:{port}` | 第一个未定义变量位置 |

### 2. 空值访问错误

**场景**：访问 null 对象的属性或方法

| 测试用例 | URL 表达式 | Context | 预期错误位置 |
|---------|-----------|---------|-------------|
| 空对象属性访问 | `http://{user.name}/path` | user=null | `{user.name}` 整体 |
| 链式空访问（中间为空） | `http://{user.profile.city}` | user.profile=null | `.city` 或 `.profile.city` |
| 空对象方法调用 | `http://{obj.toString()}/path` | obj=null | `{obj.toString()}` 整体 |

### 3. 索引访问错误

**场景**：数组/列表索引操作失败

| 测试用例 | URL 表达式 | Context | 预期错误位置 |
|---------|-----------|---------|-------------|
| 数组越界 | `http://host/{items[10]}` | items=[a,b] | `[10]` 索引位置 |
| 空数组索引 | `http://host/{arr[0]}` | arr=null | `{arr[0]}` 整体 |
| 无效索引类型 | `http://host/{list[key]}` | list=[], key="invalid" | `[key]` 索引位置 |

### 4. 方法调用错误

**场景**：方法调用失败

| 测试用例 | URL 表达式 | Context | 预期错误 |
|---------|-----------|---------|---------|
| 不存在的方法 | `http://{obj.nonExistent()}` | obj=new Object() | Cannot call method 'nonExistent' |
| 参数数量错误 | `http://{Math.max(1)}` | - | Wrong number of arguments |

### 5. 运算符错误

**场景**：运算符操作失败

| 测试用例 | URL 表达式 | Context | 预期错误位置 |
|---------|-----------|---------|-------------|
| 空值运算 | `http://host/{-count}` | count=null | `{-count}` 整体 |
| 类型不匹配运算 | `http://host/{a + b}` | a="str", b=123 | `{a + b}` 整体 |

### 6. 循环/迭代错误

**场景**：for 循环中的错误传播

| 测试用例 | URL 表达式 | Context | 预期错误 |
|---------|-----------|---------|---------|
| 迭代 null | `{% for x in items}{x}{/% for }` | items=null | Cannot iterate over null |

## 测试结构设计

```java
public class EtlURLLazyEvaluationTest {

    // ========== 现有的延迟计算测试 ==========

    // ========== 新增：错误信息测试 ==========

    // ==================== 1. 未定义变量错误 ====================

    @Test
    public void testUndefinedVariable_SingleVariable_ShowsPosition() { ... }

    @Test
    public void testUndefinedVariable_InPath_ShowsPosition() { ... }

    // ==================== 2. 空值访问错误 ====================

    @Test
    public void testNullAccess_PropertyAccess_ShowsPosition() { ... }

    @Test
    public void testNullAccess_ChainedAccess_ShowsPosition() { ... }

    // ==================== 3. 索引访问错误 ====================

    @Test
    public void testIndexAccess_ArrayOutOfBounds_ShowsPosition() { ... }

    @Test
    public void testIndexAccess_NullArray_ShowsPosition() { ... }

    // ==================== 4. 方法调用错误 ====================

    @Test
    public void testMethodCall_NullObject_ShowsPosition() { ... }

    @Test
    public void testMethodCall_MethodNotExist_ShowsPosition() { ... }

    // ==================== 5. 运算符错误 ====================

    @Test
    public void testOperator_NullOperand_ShowsPosition() { ... }

    // ==================== 6. 复合错误场景 ====================

    @Test
    public void testMixedExpression_MultipleErrors_ShowsFirstError() { ... }

    @Test
    public void testErrorPosition_MultiLineUrl_ShowsCorrectLine() { ... }
}
```

## 验证标准

每个测试用例必须验证：

1. **异常类型正确**
   ```java
   TemplateRuntimeException ex = assertThrows(
       TemplateRuntimeException.class,
       () -> Eval.evalAsURL(expression, ctx)
   );
   ```

2. **位置信息存在**
   ```java
   // 行号有效（单行测试应为 1）
   assertTrue(ex.getStartLine() > 0);
   // 列号有效（指向错误起始位置）
   assertTrue(ex.getStartColumn() > 0);
   ```

3. **可视化指针存在**（当有源码时）
   ```java
   assertTrue(ex.getMessage().contains("^") ||
              ex.getSourceText() != null);
   ```

4. **错误消息描述准确**
   ```java
   assertTrue(ex.getMessage().contains("Cannot access") ||
              ex.getMessage().contains("Cannot call") ||
              ex.getMessage().contains("Cannot iterate"));
   ```

## 测试用例示例

```java
/**
 * 测试：未定义变量在 URL 中应报错并显示位置
 *
 * 表达式: http://host/{undefinedVar}
 * 期望: 错误指向 {undefinedVar} 位置（约第12列）
 */
@Test
public void testUndefinedVariable_SingleVariable_ShowsPosition() {
    Context ctx = new Context();
    // 不设置 undefinedVar，触发未定义变量错误

    TemplateRuntimeException ex = assertThrows(
        TemplateRuntimeException.class,
        () -> Eval.evalAsURL("http://host/{undefinedVar}", ctx)
    );

    // 验证错误消息包含位置信息
    assertTrue(ex.getStartLine() >= 1,
        "应有行号: " + ex.getMessage());
    assertTrue(ex.getStartColumn() >= 12,
        "列号应指向 {undefinedVar} 开始位置: " + ex.getStartColumn());
    assertTrue(ex.getMessage().contains("undefinedVar"),
        "错误消息应提及变量名: " + ex.getMessage());
}

/**
 * 测试：访问 null 对象属性应报错并显示位置
 *
 * 表达式: http://{user.name}/path
 * Context: user=null
 * 期望: 错误指向 {user.name} 整体
 */
@Test
public void testNullAccess_PropertyAccess_ShowsPosition() {
    Context ctx = new Context();
    ctx.put("user", null);

    TemplateRuntimeException ex = assertThrows(
        TemplateRuntimeException.class,
        () -> Eval.evalAsURL("http://{user.name}/path", ctx)
    );

    assertTrue(ex.getStartLine() >= 1);
    assertTrue(ex.getStartColumn() >= 8,
        "列号应指向 {user.name} 开始位置");
    assertTrue(ex.getMessage().contains("null"),
        "错误消息应提及 null: " + ex.getMessage());
}
```

## 实现要点

1. **使用 `Eval.evalAsURL()` 作为测试入口**
   - 此方法会自动包装源码为 `url(...)` 语法
   - 错误位置会自动调整（减去 `url(` 的前缀长度）

2. **利用现有异常类**
   - `TemplateRuntimeException`：通用运行时错误
   - `ArgumentIndexOutOfBoundsException`：参数索引错误

3. **遵循现有测试风格**
   - 参考 `RuntimeExceptionFormattingTest.java` 的断言模式
   - 使用 JUnit 5 的 `assertThrows`

## 预估测试数量

- 未定义变量错误：3 个测试
- 空值访问错误：3 个测试
- 索引访问错误：2 个测试
- 方法调用错误：2 个测试
- 运算符错误：1 个测试
- 复合场景：2 个测试

**总计：13 个测试用例**

## 相关文件

- `src/test/java/org/dromara/easytl/runtime/EtlURLLazyEvaluationTest.java` - 目标测试文件
- `src/main/java/org/dromara/easytl/Eval.java` - evalAsURL 方法
- `src/main/java/org/dromara/easytl/exception/TemplateRuntimeException.java` - 异常类
- `src/main/java/org/dromara/easytl/exception/ErrorMessageFormatter.java` - 错误格式化
- `src/test/java/org/dromara/easytl/exception/RuntimeExceptionFormattingTest.java` - 参考测试