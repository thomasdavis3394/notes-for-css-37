# URL 字面量特性设计文档

## 一、概述

为 EasyTL 模板引擎添加 URL 字面量语法支持，提供 URL 解析、动态构建、属性访问/修改、编码等功能。

### 1.1 功能目标

- 支持解析和构建 URL 对象
- URL 内部支持嵌入表达式和语句块
- 延迟计算：属性访问时动态求值
- 混合模式：支持属性修改与延迟计算共存
- 可配置的 URL 组件编码器
- Query 参数操作支持

### 1.2 设计决策

| 决策项 | 选择 | 理由 |
|--------|------|------|
| 延迟计算模式 | 动态重新计算 | 变量修改后属性自动更新 |
| URL 解析方式 | 自实现解析器 | 完全控制解析逻辑，支持相对 URL |
| 默认值处理 | 无默认值 | 更灵活，由用户自行处理 |
| 对象可变性 | 可修改属性 + 编码方法 | 提供便捷的 URL 操作能力 |
| 修改后计算策略 | 混合模式 | 未修改属性继续延迟计算，修改属性使用显式值 |

---

## 二、语法设计

### 2.1 基本语法

```
url(URL表达式)
```

### 2.2 表达式嵌入

URL 内部支持三种表达式形式：

```easytl
url(http://{host}/path)       // 单花括号
url(http://{{host}}/path)     // 双花括号
url(http://${host}/path)      // 美元花括号
```

### 2.3 语句块嵌入

URL 内部支持语句块：

```easytl
url(/path{% if debug %}?debug=true{/% if %})
url(/path{% for item in list %}/{item}{/% for %})
```

### 2.4 语法示例

```easytl
// 完整 URL
let u1 = url(https://user:pass@host:8080/path?q=1#ref)

// 相对 URL
let u2 = url(/path/only)
let u3 = url(?a=1&b=2)
let u4 = url(#section)

// 表达式嵌入
let host = "localhost"
let port = 8080
let u5 = url(http://{host}:{port}/api)

// 语句块嵌入
let debug = true
let u6 = url(/api{% if debug %}?debug=true{/% if %})

// 动态 URL
let baseUrl = "https://api.example.com"
let u7 = url({baseUrl}/v1/users)
```

---

## 三、架构设计

### 3.1 整体流程

```
url(http://{host}:{port}/path)
        ↓ Lexer
Token 流（URL_BEGIN, URL 内容片段..., URL_END）
        ↓ Parser
UrlLiteral AST 节点（子节点列表：TextNode + ExpressionNode + StatementBlock）
        ↓ Evaluator
EtlURL 运行时对象（延迟计算 + 混合修改模式）
        ↓ 属性访问/toString
动态计算 → URL 解析 → 返回结果
```

### 3.2 新增组件

| 组件 | 位置 | 职责 |
|------|------|------|
| `UrlLiteral` | `org.dromara.easytl.ast` | URL 字面量 AST 节点 |
| `EtlURL` | `org.dromara.easytl.runtime` | URL 运行时对象 |
| `EtlURLQuery` | `org.dromara.easytl.runtime` | Query 参数对象 |
| `UrlParser` | `org.dromara.easytl.runtime` | URL 字符串解析器 |
| `UrlEncoder` | `org.dromara.easytl.runtime` | 编码器接口 |
| `UrlEncoders` | `org.dromara.easytl.runtime` | 内置编码器集合 |
| `UrlSyntaxException` | `org.dromara.easytl.exception` | URL 语法错误 |
| `UrlEncodingException` | `org.dromara.easytl.exception` | URL 编码错误 |

---

## 四、Lexer 层设计

### 4.1 新增 Token 类型

```java
// TokenType.java
public static final int URL = 132;              // url 关键字
// 注意：使用现有的 LEFT_PAREN 和 RIGHT_PAREN 作为括号
```

### 4.2 词法分析流程

1. 在表达式模式下识别 `url` 关键字（新增 `URL` Token 类型）
2. 识别 `LEFT_PAREN` 后，进入 **URL 模式**
3. URL 模式下：
   - 识别 `{expr}`、`{{expr}}`、`${expr}` 表达式
   - 识别 `{% stmt %}...{/% stmt}` 语句块
   - 其他字符作为普通文本（`TEXT` Token）
4. 遇到匹配的 `RIGHT_PAREN` 时，退出 URL 模式

### 4.3 Token 示例

输入：
```
url(http://{host}/path{% if debug %}?debug=true{/% if %})
```

输出 Token 流：
```
[URL, LEFT_PAREN, TEXT("http://"), SINGLE_BRACE_EXPR_BEGIN, IDENTIFIER("host"),
 SINGLE_BRACE_EXPR_END, TEXT("/path"), STMT_BEGIN, IF, IDENTIFIER("debug"),
 STMT_END, TEXT("?debug=true"), STMT_CLOSE, IF, STMT_END, RIGHT_PAREN]
```

### 4.4 Lexer 关键字识别

在 `Lexer.java` 的关键字表中添加 `url`：

```java
// Lexer.java 关键字映射
keywords.put("url", TokenType.URL);

// parsePrimary() 中处理 URL
if (check(TokenType.URL)) {
    return parseUrlLiteral();
}
```

---

## 五、Parser 层设计

### 5.1 UrlLiteral AST 节点

```java
// UrlLiteral.java
package org.dromara.easytl.ast;

public class UrlLiteral extends ASTNode {

    /** 子节点列表：TextNode、ExpressionNode、StatementBlock */
    private final List<ASTNode> children;

    public UrlLiteral(List<ASTNode> children) {
        this.children = children;
    }

    public UrlLiteral(List<ASTNode> children, Token token) {
        super(positionFromToken(token));
        this.children = children;
    }

    public List<ASTNode> getChildren() {
        return children;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitUrlLiteral(this);
    }
}
```

### 5.2 解析流程

```java
private ASTNode parseUrlLiteral() {
    Token beginToken = advance();      // 消耗 URL 关键字
    expect(TokenType.LEFT_PAREN);       // 消耗 (

    List<ASTNode> children = new ArrayList<>();

    while (!check(TokenType.RIGHT_PAREN) && !isAtEnd()) {
        if (check(TokenType.SINGLE_BRACE_EXPR_BEGIN)) {
            children.add(parseSingleBraceExpression());
        } else if (check(TokenType.DOUBLE_BRACE_EXPR_BEGIN)) {
            children.add(parseDoubleBraceExpression());
        } else if (check(TokenType.DOLLAR_BRACE_EXPR_BEGIN)) {
            children.add(parseDollarBraceExpression());
        } else if (check(TokenType.STMT_BEGIN)) {
            children.add(parseStatementBlock());
        } else {
            children.add(parseUrlText());
        }
    }

    expect(TokenType.RIGHT_PAREN);
    return new UrlLiteral(children, beginToken);
}
```

### 5.3 ASTVisitor 接口扩展

```java
// ASTVisitor.java
public interface ASTVisitor<T> {
    // ... 现有方法

    T visitUrlLiteral(UrlLiteral node);
}
```

---

## 六、Runtime 层设计

### 6.1 EtlURL 类

```java
// EtlURL.java
package org.dromara.easytl.runtime;

public class EtlURL {

    // ========== 核心字段 ==========

    /** AST 子节点列表（用于延迟计算） */
    private final List<ASTNode> children;

    /** 执行上下文 */
    private final Context context;

    /** 模板加载器 */
    private final TemplateLoader templateLoader;

    /** 源代码文本（用于错误报告） */
    private final String sourceText;

    /** 显式修改的属性值（混合模式） */
    private String scheme;
    private String userInfo;
    private String host;
    private Integer port;
    private String path;
    private String query;
    private String ref;

    /** 标记哪些属性被显式修改过 */
    private final Set<UrlComponent> modifiedComponents = EnumSet.noneOf(UrlComponent.class);

    /** 缓存：解析后的组件 */
    private volatile UrlComponents cached;

    // ========== 构造函数 ==========

    public EtlURL(List<ASTNode> children, Context context,
                   TemplateLoader templateLoader, String sourceText) {
        this.children = children;
        this.context = context;
        this.templateLoader = templateLoader;
        this.sourceText = sourceText;
    }

    // ========== 属性访问 ==========

    public String getScheme() {
        if (modifiedComponents.contains(UrlComponent.SCHEME)) return scheme;
        return getOrComputeComponents().scheme;
    }

    public void setScheme(String value) {
        this.scheme = value;
        modifiedComponents.add(UrlComponent.SCHEME);
        this.cached = null;  // 清除缓存
    }

    public String getUserInfo() {
        if (modifiedComponents.contains(UrlComponent.USER_INFO)) return userInfo;
        return getOrComputeComponents().userInfo;
    }
    public void setUserInfo(String value) {
        this.userInfo = value;
        modifiedComponents.add(UrlComponent.USER_INFO);
        this.cached = null;
    }

    public String getHost() {
        if (modifiedComponents.contains(UrlComponent.HOST)) return host;
        return getOrComputeComponents().host;
    }
    public void setHost(String value) {
        this.host = value;
        modifiedComponents.add(UrlComponent.HOST);
        this.cached = null;
    }

    public Integer getPort() {
        if (modifiedComponents.contains(UrlComponent.PORT)) return port;
        return getOrComputeComponents().port;
    }
    public void setPort(Integer value) {
        this.port = value;
        modifiedComponents.add(UrlComponent.PORT);
        this.cached = null;
    }

    public String getPath() {
        if (modifiedComponents.contains(UrlComponent.PATH)) return path;
        return getOrComputeComponents().path;
    }
    public void setPath(String value) {
        this.path = value;
        modifiedComponents.add(UrlComponent.PATH);
        this.cached = null;
    }

    public String getQuery() {
        if (modifiedComponents.contains(UrlComponent.QUERY)) return query;
        return getOrComputeComponents().query;
    }
    public void setQuery(String value) {
        this.query = value;
        modifiedComponents.add(UrlComponent.QUERY);
        this.cached = null;
    }

    public String getRef() {
        if (modifiedComponents.contains(UrlComponent.REF)) return ref;
        return getOrComputeComponents().ref;
    }
    public void setRef(String value) {
        this.ref = value;
        modifiedComponents.add(UrlComponent.REF);
        this.cached = null;
    }

    // ========== 核心计算逻辑 ==========

    /**
     * 获取缓存的组件，如果不存在则计算并缓存
     * 注意：由于延迟计算需要每次重新求值表达式，缓存主要用于
     * 同一次 toString() 调用中多次访问属性时避免重复解析
     */
    private UrlComponents getOrComputeComponents() {
        UrlComponents result = cached;
        if (result == null) {
            result = computeComponents();
            cached = result;
        }
        return result;
    }

    private UrlComponents computeComponents() {
        String urlStr = buildUrlString();
        return UrlParser.parse(urlStr);
    }

    private String buildUrlString() {
        StringBuilder sb = new StringBuilder();
        Evaluator evaluator = new Evaluator(context);
        for (ASTNode child : children) {
            Object value = child.accept(evaluator);
            if (value != null) sb.append(value.toString());
        }
        return sb.toString();
    }

    // ========== Query 操作 ==========

    public String getQuery(String key) { ... }
    public List<String> getQueryValues(String key) { ... }
    public EtlURLQuery getQueryObject(String key) { ... }
    public List<EtlURLQuery> getQueryArray() { ... }
    public Map<String, String> getQueryMap() { ... }
    public Map<String, List<String>> getQueryMultiMap() { ... }

    public void addQuery(String key, String value) { ... }
    public void setQuery(String key, String value) { ... }
    public void removeQuery(String key) { ... }
    public void clearQuery() { ... }

    // ========== 编码/解码 ==========

    public String encode() {
        return toString();  // toString 已经包含编码
    }

    public String decode() {
        // 返回未编码的原始 URL 字符串
        StringBuilder sb = new StringBuilder();
        String s = getScheme();
        if (s != null) sb.append(s).append("://");
        String ui = getUserInfo();
        if (ui != null) sb.append(ui).append("@");
        sb.append(getHost());
        Integer p = getPort();
        if (p != null) sb.append(":").append(p);
        sb.append(getPath());
        String q = getQuery();
        if (q != null) sb.append("?").append(q);
        String r = getRef();
        if (r != null) sb.append("#").append(r);
        return sb.toString();
    }

    public String encodeQuery() {
        String q = getQuery();
        return q != null ? encodeQueryValue(q) : null;
    }

    public String decodeQuery() {
        return getQuery();  // getQuery() 返回已解码的值
    }

    // ========== 组件编码方法 ==========

    private String encodeScheme(String value) {
        return context.getUrlSchemeEncoder().encode(value);
    }

    private String encodeUserInfo(String value) {
        return context.getUrlUserInfoEncoder().encode(value);
    }

    private String encodeHost(String value) {
        return context.getUrlHostEncoder().encode(value);
    }

    private String encodePath(String value) {
        return context.getUrlPathEncoder().encode(value);
    }

    private String encodeQueryString(String query) {
        // 解析 query 参数，分别编码 key 和 value
        StringBuilder sb = new StringBuilder();
        String[] pairs = query.split("&");
        for (int i = 0; i < pairs.length; i++) {
            if (i > 0) sb.append("&");
            int eqPos = pairs[i].indexOf('=');
            if (eqPos >= 0) {
                String key = pairs[i].substring(0, eqPos);
                String value = pairs[i].substring(eqPos + 1);
                sb.append(context.getUrlQueryKeyEncoder().encode(key));
                sb.append("=");
                sb.append(context.getUrlQueryValueEncoder().encode(value));
            } else {
                sb.append(context.getUrlQueryKeyEncoder().encode(pairs[i]));
            }
        }
        return sb.toString();
    }

    private String encodeQueryValue(String value) {
        return context.getUrlQueryValueEncoder().encode(value);
    }

    private String encodeRef(String value) {
        return context.getUrlRefEncoder().encode(value);
    }

    // ========== toString ==========

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        String s = getScheme();
        if (s != null) sb.append(encodeScheme(s)).append("://");

        String ui = getUserInfo();
        if (ui != null) sb.append(encodeUserInfo(ui)).append("@");

        sb.append(encodeHost(getHost()));

        Integer p = getPort();
        if (p != null) sb.append(":").append(p);

        sb.append(encodePath(getPath()));

        String q = getQuery();
        if (q != null) sb.append("?").append(encodeQueryString(q));

        String r = getRef();
        if (r != null) sb.append("#").append(encodeRef(r));

        return sb.toString();
    }
}
```

### 6.2 EtlURLQuery 类

```java
// EtlURLQuery.java
package org.dromara.easytl.runtime;

public class EtlURLQuery {

    private final String key;
    private String value;        // 已解码的值
    private String rawValue;     // 原始值（未解码）

    public EtlURLQuery(String key, String value, String rawValue) {
        this.key = key;
        this.value = value;
        this.rawValue = rawValue;
    }

    public String getKey() { return key; }
    public String getValue() { return value; }
    public String getRawValue() { return rawValue; }

    public void setValue(String value) { this.value = value; }

    @Override
    public String toString() {
        return key + "=" + value;
    }
}
```

### 6.3 UrlComponent 枚举

```java
// UrlComponent.java
package org.dromara.easytl.runtime;

public enum UrlComponent {
    SCHEME,
    USER_INFO,
    HOST,
    PORT,
    PATH,
    QUERY,
    REF
}
```

---

## 七、URL 解析器设计

### 7.1 UrlComponents 类

```java
// UrlComponents.java
package org.dromara.easytl.runtime;

/**
 * URL 解析结果，包含 URL 的各个组件
 */
public class UrlComponents {

    public String scheme;
    public String userInfo;
    public String host;
    public Integer port;
    public String path;
    public String query;
    public String ref;

    public UrlComponents() {
        // 所有字段默认为 null
    }

    public String getScheme() { return scheme; }
    public String getUserInfo() { return userInfo; }
    public String getHost() { return host; }
    public Integer getPort() { return port; }
    public String getPath() { return path; }
    public String getQuery() { return query; }
    public String getRef() { return ref; }
}
```

### 7.2 UrlParser 类

```java
// UrlParser.java
package org.dromara.easytl.runtime;

public class UrlParser {

    /**
     * 解析 URL 字符串，返回各组件
     * 支持完整 URL、相对 URL、IPv6 地址
     */
    public static UrlComponents parse(String url) {
        UrlComponents components = new UrlComponents();
        int pos = 0;

        // 1. 解析 scheme (xxx://)
        int schemeEnd = url.indexOf("://");
        if (schemeEnd > 0) {
            components.scheme = url.substring(0, schemeEnd);
            pos = schemeEnd + 3;
        }

        // 2. 解析 authority (userInfo@host:port)
        pos = parseAuthority(url, pos, components);

        // 3. 解析 path
        pos = parsePath(url, pos, components);

        // 4. 解析 query (?xxx)
        pos = parseQuery(url, pos, components);

        // 5. 解析 ref (#xxx)
        parseRef(url, pos, components);

        return components;
    }

    private static int parseAuthority(String url, int start, UrlComponents c) {
        // 查找 authority 结束位置（path/query/ref 开始）
        int pathStart = findPathStart(url, start);
        String authority = pathStart > start
            ? url.substring(start, pathStart)
            : url.substring(start);

        if (authority.isEmpty()) return start;

        // 解析 userInfo@host:port
        int atPos = authority.indexOf('@');
        if (atPos >= 0) {
            c.userInfo = authority.substring(0, atPos);
            authority = authority.substring(atPos + 1);
        }

        // 解析 host:port（支持 IPv6 [xxx]）
        if (authority.startsWith("[")) {
            // IPv6 地址
            int bracketEnd = authority.indexOf(']');
            if (bracketEnd > 0) {
                c.host = authority.substring(1, bracketEnd);
                if (bracketEnd + 1 < authority.length()
                    && authority.charAt(bracketEnd + 1) == ':') {
                    c.port = parsePort(authority.substring(bracketEnd + 2));
                }
            }
        } else {
            // 普通主机名
            int colonPos = authority.lastIndexOf(':');
            if (colonPos > 0) {
                c.host = authority.substring(0, colonPos);
                c.port = parsePort(authority.substring(colonPos + 1));
            } else {
                c.host = authority;
            }
        }

        return pathStart;
    }

    private static int parsePath(String url, int start, UrlComponents c) {
        int queryStart = url.indexOf('?', start);
        int refStart = url.indexOf('#', start);
        int end = minPositive(queryStart, refStart, url.length());

        if (end > start) {
            c.path = url.substring(start, end);
        }
        return end;
    }

    private static int parseQuery(String url, int start, UrlComponents c) {
        if (start >= url.length() || url.charAt(start) != '?') return start;

        int refStart = url.indexOf('#', start);
        c.query = refStart > 0
            ? url.substring(start + 1, refStart)
            : url.substring(start + 1);

        return refStart > 0 ? refStart : url.length();
    }

    private static void parseRef(String url, int start, UrlComponents c) {
        if (start < url.length() && url.charAt(start) == '#') {
            c.ref = url.substring(start + 1);
        }
    }
}
```

### 7.3 支持的 URL 格式

| 格式 | scheme | userInfo | host | port | path | query | ref |
|------|--------|----------|------|------|------|-------|-----|
| `http://localhost/path` | http | null | localhost | null | /path | null | null |
| `https://user:pass@host:8080/p?q=1#r` | https | user:pass | host | 8080 | /p | q=1 | r |
| `/path/only` | null | null | null | null | /path/only | null | null |
| `?query=only` | null | null | null | null | null | query=only | null |
| `#ref-only` | null | null | null | null | null | null | ref-only |
| `http://[::1]:8080/p` | http | null | ::1 | 8080 | /p | null | null |
| `file:///Users/name/file.txt` | file | null | null | null | /Users/name/file.txt | null | null |
| `ws://host/path` | ws | null | host | null | /path | null | null |

---

## 八、编码器设计

### 8.1 UrlEncoder 接口

```java
// UrlEncoder.java
package org.dromara.easytl.runtime;

public interface UrlEncoder {
    String encode(String input);
    String decode(String input);
}
```

### 8.2 内置编码器

```java
// UrlEncoders.java
package org.dromara.easytl.runtime;

public final class UrlEncoders {

    /** 标准URL编码 (application/x-www-form-urlencoded) */
    public static final UrlEncoder URL = new UrlEncoder() {
        @Override
        public String encode(String input) {
            return URLEncoder.encode(input, "UTF-8");
        }
        @Override
        public String decode(String input) {
            return URLDecoder.decode(input, "UTF-8");
        }
    };

    /** 无编码（原样返回） */
    public static final UrlEncoder NOOP = new UrlEncoder() {
        @Override
        public String encode(String input) { return input; }
        @Override
        public String decode(String input) { return input; }
    };

    /** Base64 编码（使用 java.util.Base64） */
    public static final UrlEncoder BASE64 = new UrlEncoder() {
        @Override
        public String encode(String input) {
            return Base64.getEncoder().encodeToString(input.getBytes("UTF-8"));
        }
        @Override
        public String decode(String input) {
            return new String(Base64.getDecoder().decode(input), "UTF-8");
        }
    };

    /** URL安全 Base64 编码（使用 java.util.Base64） */
    public static final UrlEncoder BASE64_URL_SAFE = new UrlEncoder() {
        @Override
        public String encode(String input) {
            return Base64.getUrlEncoder().encodeToString(input.getBytes("UTF-8"));
        }
        @Override
        public String decode(String input) {
            return new String(Base64.getUrlDecoder().decode(input), "UTF-8");
        }
    };
}
```

### 8.3 Context 编码器配置

```java
// Context.java 新增方法

public class Context {

    // ========== URL 编码器配置 ==========

    public void setUrlSchemeEncoder(UrlEncoder encoder) {
        put("_urlSchemeEncoder", encoder);
    }

    public UrlEncoder getUrlSchemeEncoder() {
        Object encoder = get("_urlSchemeEncoder");
        return encoder != null ? (UrlEncoder) encoder : UrlEncoders.NOOP;
    }

    public void setUrlUserInfoEncoder(UrlEncoder encoder) {
        put("_urlUserInfoEncoder", encoder);
    }

    public UrlEncoder getUrlUserInfoEncoder() {
        Object encoder = get("_urlUserInfoEncoder");
        return encoder != null ? (UrlEncoder) encoder : UrlEncoders.URL;
    }

    public void setUrlHostEncoder(UrlEncoder encoder) {
        put("_urlHostEncoder", encoder);
    }

    public UrlEncoder getUrlHostEncoder() {
        Object encoder = get("_urlHostEncoder");
        return encoder != null ? (UrlEncoder) encoder : UrlEncoders.NOOP;
    }

    public void setUrlPortEncoder(UrlEncoder encoder) {
        put("_urlPortEncoder", encoder);
    }

    public UrlEncoder getUrlPortEncoder() {
        Object encoder = get("_urlPortEncoder");
        return encoder != null ? (UrlEncoder) encoder : UrlEncoders.NOOP;
    }

    public void setUrlPathEncoder(UrlEncoder encoder) {
        put("_urlPathEncoder", encoder);
    }

    public UrlEncoder getUrlPathEncoder() {
        Object encoder = get("_urlPathEncoder");
        return encoder != null ? (UrlEncoder) encoder : UrlEncoders.URL;
    }

    public void setUrlQueryKeyEncoder(UrlEncoder encoder) {
        put("_urlQueryKeyEncoder", encoder);
    }

    public UrlEncoder getUrlQueryKeyEncoder() {
        Object encoder = get("_urlQueryKeyEncoder");
        return encoder != null ? (UrlEncoder) encoder : UrlEncoders.URL;
    }

    public void setUrlQueryValueEncoder(UrlEncoder encoder) {
        put("_urlQueryValueEncoder", encoder);
    }

    public UrlEncoder getUrlQueryValueEncoder() {
        Object encoder = get("_urlQueryValueEncoder");
        return encoder != null ? (UrlEncoder) encoder : UrlEncoders.URL;
    }

    public void setUrlRefEncoder(UrlEncoder encoder) {
        put("_urlRefEncoder", encoder);
    }

    public UrlEncoder getUrlRefEncoder() {
        Object encoder = get("_urlRefEncoder");
        return encoder != null ? (UrlEncoder) encoder : UrlEncoders.URL;
    }
}
```

### 8.4 使用示例

```java
// Java 端配置
Context ctx = new Context();
ctx.setUrlQueryValueEncoder(UrlEncoders.BASE64);
ctx.setUrlUserInfoEncoder(UrlEncoders.NOOP);

// 模板中
let u = url(http://user@host/path?key=hello)
u.toString()  // http://user@host/path?key=aGVsbG8=
```

---

## 九、Evaluator 层设计

```java
// Evaluator.java 新增方法

@Override
public Object visitUrlLiteral(UrlLiteral node) {
    return new EtlURL(
        node.getChildren(),
        context,
        templateLoader,
        sourceText
    );
}
```

**注意**：创建 `EtlURL` 时不立即计算 URL 字符串，只保存 AST 节点和 Context 引用，实现延迟计算。

---

## 十、错误处理设计

### 10.1 异常类型

```java
// UrlSyntaxException.java
package org.dromara.easytl.exception;

public class UrlSyntaxException extends TemplateRuntimeException {

    private final String url;
    private final int position;

    public UrlSyntaxException(String message, String url, int position) {
        super(message);
        this.url = url;
        this.position = position;
    }

    public String getUrl() { return url; }
    public int getPosition() { return position; }
}
```

```java
// UrlEncodingException.java
package org.dromara.easytl.exception;

public class UrlEncodingException extends TemplateRuntimeException {

    private final String component;
    private final String value;

    public UrlEncodingException(String component, String value, Throwable cause) {
        super("Failed to encode " + component + ": " + value, cause);
        this.component = component;
        this.value = value;
    }

    public String getComponent() { return component; }
    public String getValue() { return value; }
}
```

### 10.2 错误场景

| 错误场景 | 异常类型 |
|----------|----------|
| 变量不存在 | `TemplateRuntimeException` |
| 表达式求值失败 | `TemplateRuntimeException` |
| 编码/解码失败 | `UrlEncodingException` |

---

## 十一、测试用例

### 11.1 Lexer 测试

```java
// 空URL
url() → 子节点为空

// 纯文本
url(http://localhost/path) → 单个TextNode

// 多个表达式
url({scheme}://{host}:{port}) → TextNode + Expr + TextNode + Expr + TextNode + Expr

// 三种表达式形式混合
url(http://{host}/path{{port}}?x=${val})

// 语句块嵌套
url({% if a %}{% if b %}x{/% if %}{/% if %})

// for 循环生成 path
url(/path{% for item in list %}/{item}{/% for %})
```

### 11.2 URL 解析测试

```java
// 基础解析
url(http://localhost/path) → scheme="http", host="localhost", path="/path"

// 完整URL
url(https://user:pass@host:8080/p?q=1#r) → 所有组件正确

// 相对URL
url(/path/only) → scheme=null, host=null, path="/path/only"

// 仅 query
url(?a=1&b=2) → query="a=1&b=2"

// 仅 ref
url(#section) → ref="section"

// IPv6
url(http://[::1]:8080/p) → host="::1", port=8080
url(http://[2001:db8::1]/path) → host="2001:db8::1"

// file 协议
url(file:///Users/name/file.txt) → scheme="file"

// WebSocket 协议
url(ws://host/path) → scheme="ws"
url(wss://host/path) → scheme="wss"

// 自定义协议
url(myapp://deep/link?id=123) → scheme="myapp"
```

### 11.3 延迟计算测试

```java
// 基础延迟计算
let base = "host1"
let u = url(http://{base}/p)
u.host → "host1"
base = "host2"
u.host → "host2"

// 多变量依赖
let a = "host1", b = "port1"
let u = url(http://{a}:{b}/p)
a = "host2", b = "port2"
u.host → "host2"
u.port → "port2"

// 链式属性访问
let config = { api: { host: "localhost" } }
let u = url(http://{config.api.host}/p)
config.api.host = "newhost"
u.host → "newhost"

// 表达式计算
let x = 1, y = 2
let u = url(http://host:{x + y}/p)
u.port → 3
x = 10
u.port → 12
```

### 11.4 混合模式测试

```java
// 修改后延迟计算
let base = "host1"
let u = url(http://{base}/p)
u.scheme = "https"
base = "host2"
u.host → "host2"（仍动态）
u.scheme → "https"（已固定）

// 修改多个属性
let u = url(http://localhost:80/p)
u.scheme = "https"
u.port = 443
u.toString() → "https://localhost:443/p"

// 修改为 null
let u = url(http://user@host/p)
u.userInfo = null
u.toString() → "http://host/p"
```

### 11.5 Query 操作测试

```java
let u = url(http://host/path?a=1&b=2&a=3)

// 单值获取
u.getQuery("a")      → "1"
u.getQuery("c")      → null

// 多值获取
u.getQueryValues("a") → ["1", "3"]

// 所有 query
u.getQueryArray()    → [EtlURLQuery(a,1), EtlURLQuery(b,2), EtlURLQuery(a,3)]
u.getQueryMap()      → {a: "1", b: "2"}

// 修改 query
u.setQuery("a", "100")
u.toString()         → "http://host/path?a=100&b=2"

u.addQuery("c", "4")
u.toString()         → "http://host/path?a=100&b=2&c=4"

u.removeQuery("b")
u.toString()         → "http://host/path?a=100&c=4"
```

### 11.6 编码测试

```java
// 默认编码
url(http://host/p?key=hello world).toString()
→ "http://host/p?key=hello+world"

// 自定义编码器
ctx.setUrlQueryValueEncoder(UrlEncoders.BASE64)
url(http://host/p?key=hello).toString()
→ "http://host/p?key=aGVsbG8="

// 不编码
ctx.setUrlQueryValueEncoder(UrlEncoders.NOOP)
url(http://host/p?key=hello world).toString()
→ "http://host/p?key=hello world"
```

---

## 十二、实现文件清单

| 文件 | 位置 | 说明 |
|------|------|------|
| `UrlLiteral.java` | `org.dromara.easytl.ast` | AST 节点 |
| `EtlURL.java` | `org.dromara.easytl.runtime` | URL 运行时对象 |
| `EtlURLQuery.java` | `org.dromara.easytl.runtime` | Query 参数对象 |
| `UrlComponent.java` | `org.dromara.easytl.runtime` | URL 组件枚举 |
| `UrlComponents.java` | `org.dromara.easytl.runtime` | URL 解析结果 |
| `UrlParser.java` | `org.dromara.easytl.runtime` | URL 解析器 |
| `UrlEncoder.java` | `org.dromara.easytl.runtime` | 编码器接口 |
| `UrlEncoders.java` | `org.dromara.easytl.runtime` | 内置编码器 |
| `UrlSyntaxException.java` | `org.dromara.easytl.exception` | URL 语法错误 |
| `UrlEncodingException.java` | `org.dromara.easytl.exception` | URL 编码错误 |

**修改文件**：

| 文件 | 修改内容 |
|------|----------|
| `TokenType.java` | 新增 `URL` 关键字 |
| `Lexer.java` | 新增 URL 模式词法分析、关键字识别 |
| `Parser.java` | 新增 `parseUrlLiteral()` 方法 |
| `ASTVisitor.java` | 新增 `visitUrlLiteral()` 方法 |
| `Evaluator.java` | 实现 `visitUrlLiteral()` |
| `Context.java` | 新增编码器配置方法 |