# EtlURL 延迟计算实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 使 EtlURL 支持对 Context 的延迟计算，当 Context 变量变化时自动反映最新状态。

**Architecture:** 移除 EtlURL 中的缓存机制，每次属性访问都重新从 Context 计算。显式修改的属性通过 `modifiedComponents` 集合标记，优先于延迟计算值。

**Tech Stack:** Java 8, JUnit 5

---

## 文件结构

| 文件 | 操作 | 说明 |
|------|------|------|
| `src/test/java/org/dromara/easytl/runtime/EtlURLLazyEvaluationTest.java` | 创建 | 延迟计算测试 |
| `src/main/java/org/dromara/easytl/runtime/EtlURL.java` | 修改 | 移除缓存逻辑 |

---

### Task 1: 编写延迟计算测试

**Files:**
- Create: `src/test/java/org/dromara/easytl/runtime/EtlURLLazyEvaluationTest.java`

- [ ] **Step 1: 编写延迟计算测试类**

```java
package org.dromara.easytl.runtime;

import org.dromara.easytl.Context;
import org.dromara.easytl.Eval;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EtlURL 延迟计算测试
 */
public class EtlURLLazyEvaluationTest {

    /**
     * 测试：修改 Context 变量后，属性值自动更新
     */
    @Test
    public void testLazyEvaluation_ReflectsContextChanges() {
        Context ctx = new Context();
        ctx.put("baseUrl", "https://api.example.com");

        EtlURL url = Eval.evalAsURL("{baseUrl}/v1/users", ctx);

        // 初始值
        assertEquals("https", url.getScheme());
        assertEquals("api.example.com", url.getHost());
        assertEquals("/v1/users", url.getPath());
        assertEquals("https://api.example.com/v1/users", url.toString());

        // 修改 Context 中的变量
        ctx.put("baseUrl", "http://localhost:8080");

        // 属性值应该自动更新
        assertEquals("http", url.getScheme());
        assertEquals("localhost", url.getHost());
        assertEquals(8080, url.getPort());
        assertEquals("/v1/users", url.getPath());
        assertEquals("http://localhost:8080/v1/users", url.toString());
    }

    /**
     * 测试：显式修改优先于延迟计算
     */
    @Test
    public void testExplicitModification_TakesPrecedenceOverLazyEvaluation() {
        Context ctx = new Context();
        ctx.put("baseUrl", "https://api.example.com");

        EtlURL url = Eval.evalAsURL("{baseUrl}/v1/users", ctx);

        // 初始值
        assertEquals("api.example.com", url.getHost());

        // 显式修改 host
        url.setHost("custom.api.com");
        assertEquals("custom.api.com", url.getHost());

        // 修改 Context 中的变量，显式修改应该保持不变
        ctx.put("baseUrl", "http://another.com");
        assertEquals("custom.api.com", url.getHost()); // 仍然是显式设置的值
    }

    /**
     * 测试：混合模式 - 部分属性显式修改，部分属性延迟计算
     */
    @Test
    public void testMixedMode_PartialExplicit_PartialLazy() {
        Context ctx = new Context();
        ctx.put("baseUrl", "https://api.example.com");

        EtlURL url = Eval.evalAsURL("{baseUrl}/v1/users", ctx);

        // 显式修改 port
        url.setPort(9000);

        // 修改 Context
        ctx.put("baseUrl", "http://localhost:8080");

        // port 保持显式设置的值
        assertEquals(9000, url.getPort());

        // host 从 Context 延迟计算
        assertEquals("localhost", url.getHost());

        // toString 应该反映两者的组合
        assertEquals("http://localhost:9000/v1/users", url.toString());
    }

    /**
     * 测试：多次访问 toString 始终反映最新状态
     */
    @Test
    public void testToString_AlwaysReflectsLatestContext() {
        Context ctx = new Context();
        ctx.put("host", "host1");

        EtlURL url = Eval.evalAsURL("http://{host}/path", ctx);

        assertEquals("http://host1/path", url.toString());

        ctx.put("host", "host2");
        assertEquals("http://host2/path", url.toString());

        ctx.put("host", "host3");
        assertEquals("http://host3/path", url.toString());
    }

    /**
     * 测试：延迟计算作用于所有 URL 组件
     */
    @Test
    public void testLazyEvaluation_AllComponents() {
        Context ctx = new Context();
        ctx.put("scheme", "https");
        ctx.put("userInfo", "user:pass");
        ctx.put("host", "api.example.com");
        ctx.put("port", "8080");
        ctx.put("path", "/v1/users");
        ctx.put("query", "id=123");
        ctx.put("ref", "section");

        EtlURL url = Eval.evalAsURL(
            "{scheme}://{userInfo}@{host}:{port}{path}?{query}#{ref}", ctx);

        // 初始值
        assertEquals("https", url.getScheme());
        assertEquals("user:pass", url.getUserInfo());
        assertEquals("api.example.com", url.getHost());
        assertEquals(8080, url.getPort());
        assertEquals("/v1/users", url.getPath());
        assertEquals("id=123", url.getQuery());
        assertEquals("section", url.getRef());

        // 修改所有变量
        ctx.put("scheme", "http");
        ctx.put("userInfo", "admin:secret");
        ctx.put("host", "localhost");
        ctx.put("port", "3000");
        ctx.put("path", "/api/test");
        ctx.put("query", "name=test");
        ctx.put("ref", "top");

        // 所有属性应该更新
        assertEquals("http", url.getScheme());
        assertEquals("admin:secret", url.getUserInfo());
        assertEquals("localhost", url.getHost());
        assertEquals(3000, url.getPort());
        assertEquals("/api/test", url.getPath());
        assertEquals("name=test", url.getQuery());
        assertEquals("top", url.getRef());
    }
}
```

- [ ] **Step 2: 运行测试验证失败**

Run: `mvn test -Dtest=EtlURLLazyEvaluationTest -q`

Expected: 测试失败，因为当前缓存机制阻止了延迟计算

---

### Task 2: 移除缓存机制

**Files:**
- Modify: `src/main/java/org/dromara/easytl/runtime/EtlURL.java`

- [ ] **Step 1: 移除缓存字段**

删除第 40 行：
```java
private volatile UrlComponents cached;
```

- [ ] **Step 2: 移除 getOrComputeComponents 方法，保留 computeComponents**

删除 `getOrComputeComponents` 方法（第 133-140 行），保留 `computeComponents` 方法。

- [ ] **Step 3: 修改所有 getter 方法，直接调用 computeComponents()**

将所有 `getOrComputeComponents()` 调用改为 `computeComponents()`：

```java
public String getScheme() {
    if (modifiedComponents.contains(UrlComponent.SCHEME)) return scheme;
    return computeComponents().scheme;
}

public String getUserInfo() {
    if (modifiedComponents.contains(UrlComponent.USER_INFO)) return userInfo;
    return computeComponents().userInfo;
}

public String getHost() {
    if (modifiedComponents.contains(UrlComponent.HOST)) return host;
    return computeComponents().host;
}

public Integer getPort() {
    if (modifiedComponents.contains(UrlComponent.PORT)) return port;
    return computeComponents().port;
}

public String getPath() {
    if (modifiedComponents.contains(UrlComponent.PATH)) return path;
    return computeComponents().path;
}

public String getQuery() {
    if (modifiedComponents.contains(UrlComponent.QUERY)) return query;
    return computeComponents().query;
}

public String getRef() {
    if (modifiedComponents.contains(UrlComponent.REF)) return ref;
    return computeComponents().ref;
}
```

- [ ] **Step 4: 移除所有 setter 方法中的缓存清除代码**

从所有 setter 方法中删除 `this.cached = null;`：

```java
public void setScheme(String value) {
    this.scheme = value;
    modifiedComponents.add(UrlComponent.SCHEME);
}

public void setUserInfo(String value) {
    this.userInfo = value;
    modifiedComponents.add(UrlComponent.USER_INFO);
}

public void setHost(String value) {
    this.host = value;
    modifiedComponents.add(UrlComponent.HOST);
}

public void setPort(Integer value) {
    this.port = value;
    modifiedComponents.add(UrlComponent.PORT);
}

public void setPath(String value) {
    this.path = value;
    modifiedComponents.add(UrlComponent.PATH);
}

public void setQuery(String value) {
    this.query = value;
    modifiedComponents.add(UrlComponent.QUERY);
}

public void setRef(String value) {
    this.ref = value;
    modifiedComponents.add(UrlComponent.REF);
}
```

- [ ] **Step 5: 运行测试验证通过**

Run: `mvn test -Dtest=EtlURLLazyEvaluationTest -q`

Expected: 所有测试通过

- [ ] **Step 6: 运行现有测试确保向后兼容**

Run: `mvn test -Dtest=EtlURLTest -q`

Expected: 所有现有测试仍然通过

---

### Task 3: 提交更改

- [ ] **Step 1: 提交实现**

```bash
git add src/main/java/org/dromara/easytl/runtime/EtlURL.java src/test/java/org/dromara/easytl/runtime/EtlURLLazyEvaluationTest.java
git commit -m "feat: add lazy evaluation support for EtlURL

- Remove caching mechanism from EtlURL
- Properties now reflect Context changes in real-time
- Explicit modifications via setXxx() take precedence over lazy evaluation
- Add comprehensive tests for lazy evaluation behavior"
```

---

## 验收标准

- [x] 修改 Context 变量后，EtlURL 属性值自动更新
- [x] 显式修改的属性不受 Context 变化影响
- [x] 混合模式正常工作
- [x] toString() 始终反映最新状态
- [x] 所有现有测试通过