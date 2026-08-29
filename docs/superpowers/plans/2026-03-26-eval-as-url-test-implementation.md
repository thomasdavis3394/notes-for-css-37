# evalAsURL 测试用例增强实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 为 EvalTest.java 添加 35 个 evalAsURL 相关测试用例，覆盖所有 URL 模板表达式形式和 EtlURL 所有属性。

**Architecture:** 在现有 EvalTest.java 中添加三个测试分类块，使用 JUnit 5 测试框架，每个测试方法独立验证特定功能点。

**Tech Stack:** Java 8, JUnit 5, Maven

---

## 文件变更清单

| 文件 | 操作 | 说明 |
|------|------|------|
| `src/test/java/org/dromara/easytl/EvalTest.java` | 修改 | 添加 35 个测试方法 |

---

## Task 1: URL 字面量形式测试 - 基本形式

**Files:**
- Modify: `src/test/java/org/dromara/easytl/EvalTest.java`

- [ ] **Step 1: 添加基本 URL 测试（替换现有测试并扩展）**

在 `EvalTest.java` 中找到现有的 `testEvalAsURL_basicURL` 测试区域，将其扩展为更完整的测试块。

```java
    // ==================== URL Literal Tests ====================

    // --- Basic URL Forms ---

    @Test
    void testEvalAsURL_basicCompleteURL() {
        // Given: 一个基本的完整 URL
        // When: 使用 evalAsURL 解析
        EtlURL url = Eval.evalAsURL("http://example.com/path/to/resource");

        // Then: 所有组件正确解析
        assertNotNull(url);
        assertEquals("http", url.getScheme());
        assertEquals("example.com", url.getHost());
        assertEquals("/path/to/resource", url.getPath());
        assertNull(url.getUserInfo());
        assertNull(url.getPort());
        assertNull(url.getQuery());
        assertNull(url.getRef());
    }

    @Test
    void testEvalAsURL_fullURLWithAllComponents() {
        // Given: 一个包含所有组件的完整 URL
        EtlURL url = Eval.evalAsURL("https://user:pass@example.com:8080/api/users?page=1&size=10#results");

        // Then: 所有组件正确解析
        assertNotNull(url);
        assertEquals("https", url.getScheme());
        assertEquals("user:pass", url.getUserInfo());
        assertEquals("example.com", url.getHost());
        assertEquals(8080, url.getPort());
        assertEquals("/api/users", url.getPath());
        assertEquals("page=1&size=10", url.getQuery());
        assertEquals("results", url.getRef());
    }

    @Test
    void testEvalAsURL_relativePath() {
        // Given: 一个相对路径 URL
        EtlURL url = Eval.evalAsURL("/path/to/resource");

        // Then: 只有 path，其他组件为 null
        assertNotNull(url);
        assertNull(url.getScheme());
        assertNull(url.getHost());
        assertEquals("/path/to/resource", url.getPath());
    }

    @Test
    void testEvalAsURL_queryOnly() {
        // Given: 只有 query 部分的 URL
        EtlURL url = Eval.evalAsURL("?a=1&b=2&c=3");

        // Then: query 正确解析
        assertNotNull(url);
        assertEquals("a=1&b=2&c=3", url.getQuery());
    }

    @Test
    void testEvalAsURL_refOnly() {
        // Given: 只有 ref 部分的 URL
        EtlURL url = Eval.evalAsURL("#section");

        // Then: ref 正确解析
        assertNotNull(url);
        assertEquals("section", url.getRef());
    }
```

- [ ] **Step 2: 运行测试验证通过**

```bash
mvn test -Dtest=EvalTest#testEvalAsURL_basicCompleteURL,EvalTest#testEvalAsURL_fullURLWithAllComponents,EvalTest#testEvalAsURL_relativePath,EvalTest#testEvalAsURL_queryOnly,EvalTest#testEvalAsURL_refOnly -q
```

Expected: 5 tests passed

- [ ] **Step 3: 提交基本 URL 测试**

```bash
git add src/test/java/org/dromara/easytl/EvalTest.java
git commit -m "test: add basic URL form tests for evalAsURL"
```

---

## Task 2: URL 字面量形式测试 - 表达式嵌入

**Files:**
- Modify: `src/test/java/org/dromara/easytl/EvalTest.java`

- [ ] **Step 1: 添加表达式嵌入测试**

```java
    // --- Expression Embedding ---

    @Test
    void testEvalAsURL_singleBraceExpr() {
        // Given: Context 中有 host 变量
        Context ctx = new Context();
        ctx.put("host", "example.com");

        // When: 使用单花括号表达式
        EtlURL url = Eval.evalAsURL("http://{host}/api", ctx);

        // Then: 表达式正确求值
        assertNotNull(url);
        assertEquals("http", url.getScheme());
        assertEquals("example.com", url.getHost());
        assertEquals("/api", url.getPath());
    }

    @Test
    void testEvalAsURL_doubleBraceExpr() {
        // Given: Context 中有 host 变量
        Context ctx = new Context();
        ctx.put("host", "example.com");

        // When: 使用双花括号表达式
        EtlURL url = Eval.evalAsURL("http://{{host}}/api", ctx);

        // Then: 表达式正确求值
        assertNotNull(url);
        assertEquals("example.com", url.getHost());
    }

    @Test
    void testEvalAsURL_dollarBraceExpr() {
        // Given: Context 中有 host 变量
        Context ctx = new Context();
        ctx.put("host", "example.com");

        // When: 使用美元花括号表达式
        EtlURL url = Eval.evalAsURL("http://${host}/api", ctx);

        // Then: 表达式正确求值
        assertNotNull(url);
        assertEquals("example.com", url.getHost());
    }

    @Test
    void testEvalAsURL_multipleExpressions() {
        // Given: Context 中有多个变量
        Context ctx = new Context();
        ctx.put("scheme", "https");
        ctx.put("host", "api.example.com");
        ctx.put("port", 8080);
        ctx.put("path", "/v1/users");

        // When: 使用多个表达式
        EtlURL url = Eval.evalAsURL("{scheme}://{host}:{port}{path}", ctx);

        // Then: 所有变量正确替换
        assertNotNull(url);
        assertEquals("https", url.getScheme());
        assertEquals("api.example.com", url.getHost());
        assertEquals(8080, url.getPort());
        assertEquals("/v1/users", url.getPath());
    }
```

- [ ] **Step 2: 运行测试验证通过**

```bash
mvn test -Dtest=EvalTest#testEvalAsURL_singleBraceExpr,EvalTest#testEvalAsURL_doubleBraceExpr,EvalTest#testEvalAsURL_dollarBraceExpr,EvalTest#testEvalAsURL_multipleExpressions -q
```

Expected: 4 tests passed

- [ ] **Step 3: 提交表达式嵌入测试**

```bash
git add src/test/java/org/dromara/easytl/EvalTest.java
git commit -m "test: add expression embedding tests for evalAsURL"
```

---

## Task 3: URL 字面量形式测试 - 语句块嵌入

**Files:**
- Modify: `src/test/java/org/dromara/easytl/EvalTest.java`

- [ ] **Step 1: 添加语句块嵌入测试**

```java
    // --- Statement Block Embedding ---

    @Test
    void testEvalAsURL_ifStatementBlock() {
        // Given: Context 中有 debug 标志
        Context ctx = new Context();
        ctx.put("debug", true);

        // When: URL 中包含 if 语句块
        EtlURL url = Eval.evalAsURL("/api/users{% if debug %}?debug=true{/% if %}", ctx);

        // Then: 条件分支正确执行
        assertNotNull(url);
        assertEquals("/api/users", url.getPath());
        assertEquals("debug=true", url.getQuery());
    }

    @Test
    void testEvalAsURL_ifStatementBlock_falseCondition() {
        // Given: Context 中 debug 为 false
        Context ctx = new Context();
        ctx.put("debug", false);

        // When: URL 中包含 if 语句块
        EtlURL url = Eval.evalAsURL("/api/users{% if debug %}?debug=true{/% if %}", ctx);

        // Then: 条件分支不执行
        assertEquals("/api/users", url.getPath());
        assertNull(url.getQuery());
    }

    @Test
    void testEvalAsURL_forStatementBlock() {
        // Given: Context 中有列表变量
        Context ctx = new Context();
        ctx.put("tags", java.util.Arrays.asList("a", "b", "c"));

        // When: URL 中包含 for 语句块
        EtlURL url = Eval.evalAsURL("/search{% for tag in tags %}/{tag}{/% for %}", ctx);

        // Then: 循环正确生成路径
        assertNotNull(url);
        assertEquals("/search/a/b/c", url.getPath());
    }

    @Test
    void testEvalAsURL_dynamicURLVariable() {
        // Given: Context 中有完整的 baseUrl 变量
        Context ctx = new Context();
        ctx.put("baseUrl", "https://api.example.com");

        // When: 使用变量引用整个 URL 部分
        EtlURL url = Eval.evalAsURL("{baseUrl}/v1/users", ctx);

        // Then: 变量值正确使用
        assertNotNull(url);
        assertEquals("https", url.getScheme());
        assertEquals("api.example.com", url.getHost());
        assertEquals("/v1/users", url.getPath());
    }
```

- [ ] **Step 2: 运行测试验证通过**

```bash
mvn test -Dtest=EvalTest#testEvalAsURL_ifStatementBlock,EvalTest#testEvalAsURL_ifStatementBlock_falseCondition,EvalTest#testEvalAsURL_forStatementBlock,EvalTest#testEvalAsURL_dynamicURLVariable -q
```

Expected: 4 tests passed

- [ ] **Step 3: 提交语句块嵌入测试**

```bash
git add src/test/java/org/dromara/easytl/EvalTest.java
git commit -m "test: add statement block embedding tests for evalAsURL"
```

---

## Task 4: EtlURL 属性验证测试 - URL 组件 getter/setter

**Files:**
- Modify: `src/test/java/org/dromara/easytl/EvalTest.java`

- [ ] **Step 1: 添加 URL 组件 getter/setter 测试**

```java
    // ==================== EtlURL Property Tests ====================

    // --- URL Component Getter/Setter ---

    @Test
    void testEvalAsURL_schemeGetterSetter() {
        // Given: 一个基本 URL
        EtlURL url = Eval.evalAsURL("http://example.com/path");

        // When: 读取和修改 scheme
        assertEquals("http", url.getScheme());
        url.setScheme("https");

        // Then: scheme 已更新，toString 反映变化
        assertEquals("https", url.getScheme());
        assertEquals("https://example.com/path", url.toString());
    }

    @Test
    void testEvalAsURL_userInfoGetterSetter() {
        // Given: 一个包含 userInfo 的 URL
        EtlURL url = Eval.evalAsURL("http://user:pass@example.com/path");

        // When: 读取和修改 userInfo
        assertEquals("user:pass", url.getUserInfo());
        url.setUserInfo("newuser:newpass");

        // Then: userInfo 已更新
        assertEquals("newuser:newpass", url.getUserInfo());
        assertTrue(url.toString().contains("newuser:newpass@"));
    }

    @Test
    void testEvalAsURL_hostGetterSetter() {
        // Given: 一个基本 URL
        EtlURL url = Eval.evalAsURL("http://example.com/path");

        // When: 读取和修改 host
        assertEquals("example.com", url.getHost());
        url.setHost("newhost.com");

        // Then: host 已更新
        assertEquals("newhost.com", url.getHost());
        assertEquals("http://newhost.com/path", url.toString());
    }

    @Test
    void testEvalAsURL_portGetterSetter() {
        // Given: 一个包含 port 的 URL
        EtlURL url = Eval.evalAsURL("http://example.com:8080/path");

        // When: 读取和修改 port
        assertEquals(8080, url.getPort());
        url.setPort(443);

        // Then: port 已更新
        assertEquals(443, url.getPort());
        assertEquals("http://example.com:443/path", url.toString());
    }

    @Test
    void testEvalAsURL_pathGetterSetter() {
        // Given: 一个基本 URL
        EtlURL url = Eval.evalAsURL("http://example.com/old/path");

        // When: 读取和修改 path
        assertEquals("/old/path", url.getPath());
        url.setPath("/new/path");

        // Then: path 已更新
        assertEquals("/new/path", url.getPath());
        assertEquals("http://example.com/new/path", url.toString());
    }

    @Test
    void testEvalAsURL_queryGetterSetter() {
        // Given: 一个包含 query 的 URL
        EtlURL url = Eval.evalAsURL("http://example.com/path?a=1&b=2");

        // When: 读取和修改 query
        assertEquals("a=1&b=2", url.getQuery());
        url.setQuery("x=10&y=20");

        // Then: query 已更新
        assertEquals("x=10&y=20", url.getQuery());
        assertEquals("http://example.com/path?x=10&y=20", url.toString());
    }

    @Test
    void testEvalAsURL_refGetterSetter() {
        // Given: 一个包含 ref 的 URL
        EtlURL url = Eval.evalAsURL("http://example.com/path#section1");

        // When: 读取和修改 ref
        assertEquals("section1", url.getRef());
        url.setRef("section2");

        // Then: ref 已更新
        assertEquals("section2", url.getRef());
        assertEquals("http://example.com/path#section2", url.toString());
    }
```

- [ ] **Step 2: 运行测试验证通过**

```bash
mvn test -Dtest=EvalTest#testEvalAsURL_schemeGetterSetter,EvalTest#testEvalAsURL_userInfoGetterSetter,EvalTest#testEvalAsURL_hostGetterSetter,EvalTest#testEvalAsURL_portGetterSetter,EvalTest#testEvalAsURL_pathGetterSetter,EvalTest#testEvalAsURL_queryGetterSetter,EvalTest#testEvalAsURL_refGetterSetter -q
```

Expected: 7 tests passed

- [ ] **Step 3: 提交组件 getter/setter 测试**

```bash
git add src/test/java/org/dromara/easytl/EvalTest.java
git commit -m "test: add URL component getter/setter tests for evalAsURL"
```

---

## Task 5: EtlURL 属性验证测试 - Query 操作

**Files:**
- Modify: `src/test/java/org/dromara/easytl/EvalTest.java`

- [ ] **Step 1: 添加 Query 操作测试**

```java
    // --- Query Operations ---

    @Test
    void testEvalAsURL_getQuerySingleValue() {
        // Given: URL 中有多个 query 参数
        EtlURL url = Eval.evalAsURL("http://example.com/path?a=1&b=2&c=3");

        // When: 获取单个参数值
        // Then: 返回第一个匹配的值
        assertEquals("1", url.getQuery("a"));
        assertEquals("2", url.getQuery("b"));
        assertEquals("3", url.getQuery("c"));
        assertNull(url.getQuery("d"));
    }

    @Test
    void testEvalAsURL_getQueryMultipleValues() {
        // Given: URL 中有重复的 query 参数
        EtlURL url = Eval.evalAsURL("http://example.com/path?a=1&b=2&a=3");

        // When: 获取参数的所有值
        java.util.List<String> values = url.getQueryValues("a");

        // Then: 返回所有匹配的值
        assertEquals(2, values.size());
        assertTrue(values.contains("1"));
        assertTrue(values.contains("3"));
    }

    @Test
    void testEvalAsURL_addQuery() {
        // Given: 一个基本 URL
        EtlURL url = Eval.evalAsURL("http://example.com/path?a=1");

        // When: 添加新的 query 参数
        url.addQuery("b", "2");

        // Then: 参数已添加
        assertTrue(url.toString().contains("a=1"));
        assertTrue(url.toString().contains("b=2"));
    }

    @Test
    void testEvalAsURL_setQueryByKey() {
        // Given: URL 中有 query 参数
        EtlURL url = Eval.evalAsURL("http://example.com/path?a=1&b=2");

        // When: 设置已存在的参数
        url.setQuery("a", "100");

        // Then: 参数值已更新
        assertEquals("100", url.getQuery("a"));
        assertEquals("2", url.getQuery("b"));
    }

    @Test
    void testEvalAsURL_setQueryByKey_newKey() {
        // Given: URL 中没有某个参数
        EtlURL url = Eval.evalAsURL("http://example.com/path?a=1");

        // When: 设置不存在的参数
        url.setQuery("b", "2");

        // Then: 参数已添加
        assertEquals("1", url.getQuery("a"));
        assertEquals("2", url.getQuery("b"));
    }

    @Test
    void testEvalAsURL_removeQuery() {
        // Given: URL 中有多个 query 参数
        EtlURL url = Eval.evalAsURL("http://example.com/path?a=1&b=2&c=3");

        // When: 移除一个参数
        url.removeQuery("b");

        // Then: 参数已移除
        assertNull(url.getQuery("b"));
        assertEquals("1", url.getQuery("a"));
        assertEquals("3", url.getQuery("c"));
    }
```

- [ ] **Step 2: 运行测试验证通过**

```bash
mvn test -Dtest=EvalTest#testEvalAsURL_getQuerySingleValue,EvalTest#testEvalAsURL_getQueryMultipleValues,EvalTest#testEvalAsURL_addQuery,EvalTest#testEvalAsURL_setQueryByKey,EvalTest#testEvalAsURL_setQueryByKey_newKey,EvalTest#testEvalAsURL_removeQuery -q
```

Expected: 6 tests passed

- [ ] **Step 3: 提交 Query 操作测试**

```bash
git add src/test/java/org/dromara/easytl/EvalTest.java
git commit -m "test: add query operation tests for evalAsURL"
```

---

## Task 6: EtlURL 属性验证测试 - toString 和混合模式

**Files:**
- Modify: `src/test/java/org/dromara/easytl/EvalTest.java`

- [ ] **Step 1: 添加 toString 和混合模式测试**

```java
    // --- toString and Mixed Mode ---

    @Test
    void testEvalAsURL_toStringReconstruction() {
        // Given: 一个包含所有组件的 URL
        EtlURL url = Eval.evalAsURL("https://user:pass@example.com:8080/path?a=1#section");

        // When: 调用 toString
        String result = url.toString();

        // Then: URL 完整重建
        assertEquals("https://user:pass@example.com:8080/path?a=1#section", result);
    }

    @Test
    void testEvalAsURL_toStringReconstruction_minimal() {
        // Given: 一个最简 URL
        EtlURL url = Eval.evalAsURL("http://example.com/path");

        // When: 调用 toString
        String result = url.toString();

        // Then: URL 正确重建
        assertEquals("http://example.com/path", result);
    }

    @Test
    void testEvalAsURL_mixedMode() {
        // Given: Context 中有 host 变量
        Context ctx = new Context();
        ctx.put("host", "example.com");

        EtlURL url = Eval.evalAsURL("http://{host}/path", ctx);

        // When: 修改 scheme 但不修改 host
        url.setScheme("https");

        // Then: scheme 使用固定值，host 仍动态计算
        assertEquals("https", url.getScheme());
        assertEquals("example.com", url.getHost());
        assertEquals("https://example.com/path", url.toString());
    }

    @Test
    void testEvalAsURL_mixedMode_contextUpdate() {
        // Given: Context 中有 host 变量
        Context ctx = new Context();
        ctx.put("host", "example.com");

        EtlURL url = Eval.evalAsURL("http://{host}/path", ctx);
        assertEquals("example.com", url.getHost());

        // When: 更新 Context 中的变量
        ctx.put("host", "newhost.com");

        // Then: host 动态更新
        assertEquals("newhost.com", url.getHost());
    }

    @Test
    void testEvalAsURL_dynamicPropertyUpdate() {
        // Given: Context 中有多个变量
        Context ctx = new Context();
        ctx.put("scheme", "http");
        ctx.put("host", "example.com");

        EtlURL url = Eval.evalAsURL("{scheme}://{host}/path", ctx);

        // When: 更新 Context 变量
        ctx.put("scheme", "https");
        ctx.put("host", "newhost.com");

        // Then: 所有属性动态更新
        assertEquals("https", url.getScheme());
        assertEquals("newhost.com", url.getHost());
        assertEquals("https://newhost.com/path", url.toString());
    }
```

- [ ] **Step 2: 运行测试验证通过**

```bash
mvn test -Dtest=EvalTest#testEvalAsURL_toStringReconstruction,EvalTest#testEvalAsURL_toStringReconstruction_minimal,EvalTest#testEvalAsURL_mixedMode,EvalTest#testEvalAsURL_mixedMode_contextUpdate,EvalTest#testEvalAsURL_dynamicPropertyUpdate -q
```

Expected: 5 tests passed

- [ ] **Step 3: 提交 toString 和混合模式测试**

```bash
git add src/test/java/org/dromara/easytl/EvalTest.java
git commit -m "test: add toString and mixed mode tests for evalAsURL"
```

---

## Task 7: 边界情况测试 - 特殊地址格式

**Files:**
- Modify: `src/test/java/org/dromara/easytl/EvalTest.java`

- [ ] **Step 1: 添加特殊地址格式测试**

```java
    // ==================== Edge Case Tests ====================

    // --- Special Address Formats ---

    @Test
    void testEvalAsURL_ipv6Address() {
        // Given: IPv6 地址 URL
        EtlURL url = Eval.evalAsURL("http://[::1]:8080/path");

        // Then: IPv6 地址正确解析
        assertNotNull(url);
        assertEquals("http", url.getScheme());
        assertEquals("::1", url.getHost());
        assertEquals(8080, url.getPort());
        assertEquals("/path", url.getPath());
    }

    @Test
    void testEvalAsURL_ipv6Address_longForm() {
        // Given: IPv6 长格式地址
        EtlURL url = Eval.evalAsURL("http://[2001:db8::1]/path");

        // Then: IPv6 地址正确解析
        assertEquals("2001:db8::1", url.getHost());
        assertNull(url.getPort());
    }

    @Test
    void testEvalAsURL_fileProtocol() {
        // Given: file 协议 URL
        EtlURL url = Eval.evalAsURL("file:///Users/name/document.txt");

        // Then: file 协议正确解析
        assertNotNull(url);
        assertEquals("file", url.getScheme());
        assertEquals("/Users/name/document.txt", url.getPath());
    }

    @Test
    void testEvalAsURL_webSocketProtocol() {
        // Given: WebSocket 协议 URL
        EtlURL wsUrl = Eval.evalAsURL("ws://example.com/socket");
        EtlURL wssUrl = Eval.evalAsURL("wss://example.com/socket");

        // Then: WebSocket 协议正确解析
        assertEquals("ws", wsUrl.getScheme());
        assertEquals("wss", wssUrl.getScheme());
        assertEquals("/socket", wsUrl.getPath());
    }
```

- [ ] **Step 2: 运行测试验证通过**

```bash
mvn test -Dtest=EvalTest#testEvalAsURL_ipv6Address,EvalTest#testEvalAsURL_ipv6Address_longForm,EvalTest#testEvalAsURL_fileProtocol,EvalTest#testEvalAsURL_webSocketProtocol -q
```

Expected: 4 tests passed

- [ ] **Step 3: 提交特殊地址格式测试**

```bash
git add src/test/java/org/dromara/easytl/EvalTest.java
git commit -m "test: add special address format tests for evalAsURL"
```

---

## Task 8: 边界情况测试 - 编码行为

**Files:**
- Modify: `src/test/java/org/dromara/easytl/EvalTest.java`

- [ ] **Step 1: 添加编码行为测试**

```java
    // --- Encoding Behavior ---

    @Test
    void testEvalAsURL_chineseCharacterEncoding() {
        // Given: 包含中文字符的 URL
        EtlURL url = Eval.evalAsURL("http://example.com/search?q=中文");

        // When: 调用 toString
        String result = url.toString();

        // Then: 中文字符被正确编码
        assertTrue(result.contains("http://example.com/search?q="));
        assertTrue(result.contains("%"));
    }

    @Test
    void testEvalAsURL_spaceEncoding() {
        // Given: query 中包含空格
        EtlURL url = Eval.evalAsURL("http://example.com/search?q=hello world");

        // When: 调用 toString
        String result = url.toString();

        // Then: 空格被编码为 +
        assertTrue(result.contains("q=hello+world") || result.contains("q=hello%20world"));
    }

    @Test
    void testEvalAsURL_specialCharacters() {
        // Given: 包含特殊字符的 URL
        EtlURL url = Eval.evalAsURL("http://example.com/path?key=value&other=test");

        // Then: 特殊字符正确处理
        assertEquals("key=value&other=test", url.getQuery());
        assertEquals("value", url.getQuery("key"));
        assertEquals("test", url.getQuery("other"));
    }
```

- [ ] **Step 2: 运行测试验证通过**

```bash
mvn test -Dtest=EvalTest#testEvalAsURL_chineseCharacterEncoding,EvalTest#testEvalAsURL_spaceEncoding,EvalTest#testEvalAsURL_specialCharacters -q
```

Expected: 3 tests passed

- [ ] **Step 3: 提交编码行为测试**

```bash
git add src/test/java/org/dromara/easytl/EvalTest.java
git commit -m "test: add encoding behavior tests for evalAsURL"
```

---

## Task 9: 边界情况测试 - 错误处理

**Files:**
- Modify: `src/test/java/org/dromara/easytl/EvalTest.java`

- [ ] **Step 1: 添加错误处理测试**

```java
    // --- Error Handling ---

    @Test
    void testEvalAsURL_emptySource() {
        // Given: 空字符串
        // When: 调用 evalAsURL
        EtlURL result = Eval.evalAsURL("");

        // Then: 返回 null
        assertNull(result);
    }

    @Test
    void testEvalAsURL_whitespaceSource() {
        // Given: 纯空白字符串
        EtlURL result = Eval.evalAsURL("   ");

        // Then: 返回 null
        assertNull(result);
    }

    @Test
    void testEvalAsURL_invalidExpression() {
        // Given: 无效的表达式语法
        // When/Then: 抛出异常
        assertThrows(Exception.class, () -> {
            Eval.evalAsURL("http://example.com/${");
        });
    }

    @Test
    void testEvalAsURL_nullSource_throwsException() {
        // Given: null 源码
        // When/Then: 抛出 IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> {
            Eval.evalAsURL(null);
        });
    }
```

- [ ] **Step 2: 运行测试验证通过**

```bash
mvn test -Dtest=EvalTest#testEvalAsURL_emptySource,EvalTest#testEvalAsURL_whitespaceSource,EvalTest#testEvalAsURL_invalidExpression,EvalTest#testEvalAsURL_nullSource_throwsException -q
```

Expected: 4 tests passed

- [ ] **Step 3: 提交错误处理测试**

```bash
git add src/test/java/org/dromara/easytl/EvalTest.java
git commit -m "test: add error handling tests for evalAsURL"
```

---

## Task 10: 清理和验证

**Files:**
- Modify: `src/test/java/org/dromara/easytl/EvalTest.java`

- [ ] **Step 1: 删除被替换的旧测试方法**

删除以下已存在的测试方法（它们已被新测试覆盖）：
- `testEvalAsURL_basicURL`（被 `testEvalAsURL_basicCompleteURL` 替代）
- `testEvalAsURL_fullURL`（被 `testEvalAsURL_fullURLWithAllComponents` 替代）
- `testEvalAsURL_withPath`（被 `testEvalAsURL_basicCompleteURL` 覆盖）
- `testEvalAsURL_withQuery`（被 `testEvalAsURL_fullURLWithAllComponents` 覆盖）
- `testEvalAsURL_relativePath`（被新版本替代）
- `testEvalAsURL_nullSource`（被 `testEvalAsURL_nullSource_throwsException` 替代）
- `testEvalAsURL_syntaxError`（被 `testEvalAsURL_invalidExpression` 替代）
- `testEvalAsURL_withContext`（被 `testEvalAsURL_singleBraceExpr` 等替代）
- `testEvalAsURL_withContext_NullContext_ThrowsException`（保留，测试 null context）

- [ ] **Step 2: 运行所有 evalAsURL 相关测试**

```bash
mvn test -Dtest=EvalTest -q
```

Expected: All tests passed

- [ ] **Step 3: 运行完整测试套件**

```bash
mvn test -q
```

Expected: All tests passed

- [ ] **Step 4: 最终提交**

```bash
git add src/test/java/org/dromara/easytl/EvalTest.java
git commit -m "test: complete evalAsURL test coverage with 35 test cases"
```

---

## 测试用例清单总结

| 分类 | 数量 | Task |
|------|------|------|
| URL 字面量形式测试 - 基本形式 | 5 | Task 1 |
| URL 字面量形式测试 - 表达式嵌入 | 4 | Task 2 |
| URL 字面量形式测试 - 语句块嵌入 | 4 | Task 3 |
| EtlURL 属性验证 - 组件 getter/setter | 7 | Task 4 |
| EtlURL 属性验证 - Query 操作 | 6 | Task 5 |
| EtlURL 属性验证 - toString 和混合模式 | 5 | Task 6 |
| 边界情况 - 特殊地址格式 | 4 | Task 7 |
| 边界情况 - 编码行为 | 3 | Task 8 |
| 边界情况 - 错误处理 | 4 | Task 9 |
| **总计** | **42** | - |

注：部分原有测试将被删除或合并，最终新增测试约 35 个。