# evalAsURL 测试用例增强设计文档

## 一、概述

为 `EvalTest.java` 添加全面的 `evalAsURL` 测试用例，覆盖所有 URL 模板表达式形式并验证返回的 `EtlURL` 对象的所有属性。

### 1.1 目标

- 测试所有 URL 字面量形式（基本、表达式嵌入、语句块嵌入）
- 验证 EtlURL 所有属性的 getter/setter
- 测试 Query 操作方法
- 覆盖边界情况和错误处理

### 1.2 设计决策

| 决策项 | 选择 | 理由 |
|--------|------|------|
| 测试组织 | 分类嵌套测试 | 清晰的测试结构，便于维护 |
| 测试方法 | 使用 evalAsURL 直接测试 | 符合实际使用场景 |
| 属性验证 | 逐属性测试 + toString 验证 | 确保每个属性正确性 |

---

## 二、测试分类设计

### 2.1 测试架构

```
EvalTest.java
├── URL Literal Tests (URL 字面量测试)
│   ├── 基本形式测试
│   ├── 表达式嵌入测试 ({}, {{}}, ${})
│   ├── 语句块嵌入测试 ({% if %}, {% for %})
│   └── 特殊 URL 形式测试
│
├── EtlURL Property Tests (属性验证测试)
│   ├── URL 组件访问
│   ├── Query 操作
│   └── 属性修改与混合模式
│
└── Edge Case Tests (边界情况测试)
    ├── 特殊地址格式
    ├── 编码行为
    └── 错误处理
```

---

## 三、测试用例清单

### 3.1 URL 字面量形式测试（12 个）

| 编号 | 测试方法名 | 描述 | 验证内容 |
|------|------------|------|----------|
| 1 | `testEvalAsURL_basicCompleteURL` | 基本完整 URL | scheme, host, path |
| 2 | `testEvalAsURL_fullURLWithAllComponents` | 带 userInfo 和 port | scheme, userInfo, host, port, path, query, ref |
| 3 | `testEvalAsURL_relativePath` | 相对路径 `/path/to/resource` | path, scheme/host 为 null |
| 4 | `testEvalAsURL_queryOnly` | 仅 query `?a=1&b=2` | query 值 |
| 5 | `testEvalAsURL_refOnly` | 仅 ref `#section` | ref 值 |
| 6 | `testEvalAsURL_singleBraceExpr` | 单花括号 `{host}` | 表达式求值正确 |
| 7 | `testEvalAsURL_doubleBraceExpr` | 双花括号 `{{host}}` | 表达式求值正确 |
| 8 | `testEvalAsURL_dollarBraceExpr` | 美元花括号 `${host}` | 表达式求值正确 |
| 9 | `testEvalAsURL_multipleExpressions` | 多表达式混合 | 所有变量正确替换 |
| 10 | `testEvalAsURL_ifStatementBlock` | if 语句块嵌入 | 条件分支正确 |
| 11 | `testEvalAsURL_forStatementBlock` | for 语句块嵌入 | 循环生成正确 |
| 12 | `testEvalAsURL_dynamicURLVariable` | 动态 URL 变量引用 | 变量值正确使用 |

### 3.2 EtlURL 属性验证测试（15 个）

| 编号 | 测试方法名 | 描述 | 验证内容 |
|------|------------|------|----------|
| 13 | `testEvalAsURL_schemeGetterSetter` | scheme getter/setter | 读取和修改 |
| 14 | `testEvalAsURL_userInfoGetterSetter` | userInfo getter/setter | 读取和修改 |
| 15 | `testEvalAsURL_hostGetterSetter` | host getter/setter | 读取和修改 |
| 16 | `testEvalAsURL_portGetterSetter` | port getter/setter | 读取和修改 |
| 17 | `testEvalAsURL_pathGetterSetter` | path getter/setter | 读取和修改 |
| 18 | `testEvalAsURL_queryGetterSetter` | query getter/setter | 读取和修改 |
| 19 | `testEvalAsURL_refGetterSetter` | ref getter/setter | 读取和修改 |
| 20 | `testEvalAsURL_getQuerySingleValue` | `getQuery(key)` | 单值获取 |
| 21 | `testEvalAsURL_getQueryMultipleValues` | `getQueryValues(key)` | 多值获取 |
| 22 | `testEvalAsURL_addQuery` | `addQuery(key, value)` | 添加参数 |
| 23 | `testEvalAsURL_setQueryByKey` | `setQuery(key, value)` | 设置参数 |
| 24 | `testEvalAsURL_removeQuery` | `removeQuery(key)` | 移除参数 |
| 25 | `testEvalAsURL_toStringReconstruction` | `toString()` 完整重建 | URL 字符串正确 |
| 26 | `testEvalAsURL_mixedMode` | 混合模式 | 修改属性后其他属性仍动态计算 |
| 27 | `testEvalAsURL_dynamicPropertyUpdate` | Context 变量更新 | 属性随变量变化 |

### 3.3 边界情况测试（8 个）

| 编号 | 测试方法名 | 描述 | 验证内容 |
|------|------------|------|----------|
| 28 | `testEvalAsURL_ipv6Address` | IPv6 地址 `[::1]:8080` | host 和 port 正确解析 |
| 29 | `testEvalAsURL_fileProtocol` | file 协议 | scheme 和 path |
| 30 | `testEvalAsURL_webSocketProtocol` | WebSocket 协议 ws/wss | scheme 正确 |
| 31 | `testEvalAsURL_chineseCharacterEncoding` | 中文字符编码 | 正确编码中文字符 |
| 32 | `testEvalAsURL_spaceEncoding` | 空格编码 | 空格正确编码为 + |
| 33 | `testEvalAsURL_specialCharacters` | 特殊字符处理 | 特殊字符处理正确 |
| 34 | `testEvalAsURL_emptySource` | 空源码 | 返回 null |
| 35 | `testEvalAsURL_invalidExpression` | 无效表达式 | 抛出异常 |

---

## 四、测试实现规范

### 4.1 测试方法命名

- 使用 `testEvalAsURL_` 前缀
- 使用驼峰命名描述测试场景
- 例如：`testEvalAsURL_fullURLWithAllComponents`

### 4.2 断言规范

- 每个 URL 组件使用独立的 `assertEquals` 验证
- 使用 `assertNull` 验证空值
- 使用 `assertThrows` 验证异常

### 4.3 代码组织

- 测试按分类使用注释块分隔
- 每个测试方法包含清晰的测试意图注释
- 复杂测试使用 Given-When-Then 结构

---

## 五、预期工作量

| 分类 | 测试数量 | 预计代码行数 |
|------|----------|--------------|
| URL 字面量形式测试 | 12 | ~200 行 |
| EtlURL 属性验证测试 | 15 | ~250 行 |
| 边界情况测试 | 8 | ~150 行 |
| **总计** | **35** | **~600 行** |

---

## 六、实施计划

1. 添加 URL 字面量形式测试（12 个）
2. 添加 EtlURL 属性验证测试（15 个）
3. 添加边界情况测试（8 个）
4. 运行所有测试验证通过