# 参数下标引用功能 - Fluent API 实现

## 需求分析

用户希望在 EasyTL 的 fluent API 中添加 `parameters()` 方法，支持以下用法：

```java
// 用法 1: TemplateBuilder 直接链式调用
EasyTL.template("Args: {0}, {1}").parameters("a", "b").render();

// 用法 2: 通过 ContextBuilder 创建带参数的上下文
Context ctx = EasyTL.context().parameters("a", "b");
EasyTL.template("Args: {0}, {1}").context(ctx).render();
```

## 现状分析

### 已实现的功能

| 组件 | 状态 | 说明 |
|------|------|------|
| `Context.setParameters(Object...)` | ✅ 已存在 | 设置参数数组 |
| `Context.getParameter(int)` | ✅ 已存在 | 通过索引获取参数 |
| `Context.getParameters()` | ✅ 已存在 | 获取参数数组 |
| `ParameterAccess` AST 节点 | ✅ 已存在 | 参数访问表达式节点 |
| `Parser` | ✅ 已实现 | `{0}`, `{1}` 等表达式自动转换为 `ParameterAccess` |
| `Evaluator.visitParameterAccess()` | ✅ 已实现 | 执行时调用 `context.getParameter(index)` |

### 缺失的功能

| 组件 | 需要添加 |
|------|----------|
| `ContextBuilder` | `parameters(Object...)` 方法 |
| `TemplateBuilder` | `parameters(Object...)` 方法 |
| `TemplateBuilder` | `context(Context)` 方法需要同时复制参数 |
| `EasyTLTest` | 端到端测试用例 |

## 实现计划

### Step 1: 修改 ContextBuilder.java

添加 `parameters(Object...)` 方法：

```java
/**
 * 设置参数数组
 *
 * @param parameters 参数数组
 * @return this，支持链式调用
 */
public ContextBuilder parameters(Object... parameters) {
    context.setParameters(parameters);
    return this;
}
```

### Step 2: 修改 TemplateBuilder.java

1. 添加 `parameters(Object...)` 方法
2. 更新 `context(Context)` 方法以复制参数

```java
/**
 * 设置参数数组
 *
 * @param parameters 参数数组
 * @return this，支持链式调用
 */
public TemplateBuilder parameters(Object... parameters) {
    contextBuilder.parameters(parameters);
    return this;
}
```

对于 `context(Context)` 方法，需要同时复制参数：

```java
public TemplateBuilder context(Context context) {
    // 复制变量
    Map<String, Object> vars = context.getVariables();
    for (Map.Entry<String, Object> entry : vars.entrySet()) {
        contextBuilder.put(entry.getKey(), entry.getValue());
    }
    // 复制参数
    Object[] params = context.getParameters();
    if (params != null) {
        contextBuilder.parameters(params);
    }
    return this;
}
```

### Step 3: 添加测试用例到 EasyTLTest.java

添加以下测试：

1. `testTemplateWithParameters()` - TemplateBuilder 直接使用 parameters()
2. `testContextBuilderWithParameters()` - ContextBuilder 使用 parameters()
3. `testTemplateWithContextParameters()` - 组合使用 context() 和 parameters()
4. `testParametersWithDifferentTypes()` - 不同类型的参数
5. `testParametersOutOfBound()` - 访问超出范围的参数

## 验收标准

1. ✅ `EasyTL.template("Hello {0}").parameters("World").render()` 返回 `"Hello World"`
2. ✅ `EasyTL.context().parameters("a", "b")` 创建的 Context 可以通过 `getParameter()` 获取参数
3. ✅ 现有测试全部通过
4. ✅ 新增测试覆盖所有新增功能