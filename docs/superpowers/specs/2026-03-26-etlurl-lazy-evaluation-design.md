# EtlURL 延迟计算设计

## 概述

使 `EtlURL` 对象支持对 Context 的延迟计算，当 Context 中的变量变化时，EtlURL 的属性和渲染结果自动反映最新状态。

## 动机

当前 `EtlURL` 使用缓存机制，在首次访问属性时计算并缓存结果。如果后续 Context 中的变量发生变化，缓存的值不会更新，导致 `EtlURL` 无法反映 Context 的最新状态。

期望行为：

```java
Context ctx = new Context();
ctx.put("baseUrl", "https://api.example.com");

EtlURL url = Eval.evalAsURL("{baseUrl}/v1/users", ctx);
url.getHost(); // "api.example.com"

ctx.put("baseUrl", "http://localhost:8080");
url.getHost(); // 期望: "localhost" (自动反映变化)
              // 实际: "api.example.com" (使用缓存值)
```

## 设计

### 核心原则

1. **完全不缓存** - 每次访问属性都重新从 Context 计算，确保始终反映最新状态
2. **显式修改优先** - 通过 `setXxx()` 方法设置的值固定不变，不受 Context 变化影响
3. **向后兼容** - API 保持不变，只修改内部计算逻辑

### 改动点

#### EtlURL 类

**移除缓存字段：**

```java
// 删除
- private volatile UrlComponents cached;
```

**修改 getter 方法：**

```java
// 当前实现
public String getScheme() {
    if (modifiedComponents.contains(UrlComponent.SCHEME)) return scheme;
    return getOrComputeComponents().scheme;
}

// 新实现（移除缓存）
public String getScheme() {
    if (modifiedComponents.contains(UrlComponent.SCHEME)) return scheme;
    return computeComponents().scheme;
}
```

**修改 setter 方法：**

```java
// 当前实现
public void setScheme(String value) {
    this.scheme = value;
    modifiedComponents.add(UrlComponent.SCHEME);
    this.cached = null;  // 删除此行
}

// 新实现
public void setScheme(String value) {
    this.scheme = value;
    modifiedComponents.add(UrlComponent.SCHEME);
}
```

**移除缓存方法：**

```java
// 删除
- private UrlComponents getOrComputeComponents() { ... }

// 保留并简化
private UrlComponents computeComponents() {
    String urlStr = buildUrlString();
    return UrlParser.parse(urlStr);
}
```

### 行为示例

```java
Context ctx = new Context();
ctx.put("baseUrl", "https://api.example.com");

EtlURL url = Eval.evalAsURL("{baseUrl}/v1/users", ctx);

// 延迟计算：每次都从 Context 获取最新值
url.getHost();  // "api.example.com"

ctx.put("baseUrl", "http://localhost:8080");
url.getHost();  // "localhost" (自动反映变化)

// 显式修改：固定该属性
url.setHost("custom.api.com");
url.getHost();  // "custom.api.com" (不再受 Context 影响)

ctx.put("baseUrl", "https://another.com");
url.getHost();  // "custom.api.com" (仍然保持显式值)
```

### 性能考虑

- URL 表达式通常较简单（如 `{baseUrl}/v1/users`），计算开销很小
- 用户选择延迟计算特性，目的是获得动态绑定能力，而非性能优化
- 如有性能敏感场景，可考虑后续提供快照方法（如 `toSnapshot()` 返回静态副本）

### 线程安全性

移除缓存后，线程安全性反而更简单：

- `EtlURL` 的核心状态（`children`、`context`、`sourceText`）在构造时确定，不可变
- `modifiedComponents` 和显式设置的属性值使用 `final` 或 `ConcurrentHashMap` 保证线程安全
- `computeComponents()` 是无状态计算，每次调用独立执行，天然线程安全

相比之前使用 `volatile` 缓存的方案，新方案无需同步机制即可保证线程安全。

## 影响范围

- `org.dromara.easytl.runtime.EtlURL` - 移除缓存逻辑

## 测试计划

1. 测试延迟计算：修改 Context 变量后，属性值自动更新
2. 测试显式修改优先：调用 `setXxx()` 后，属性不受 Context 影响
3. 测试混合模式：部分属性显式修改，部分属性延迟计算
4. 测试 `toString()` 始终反映最新状态
5. 测试多线程安全性