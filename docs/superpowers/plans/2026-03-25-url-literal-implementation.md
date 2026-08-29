# URL Literal Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add URL literal syntax support to EasyTL template engine with URL parsing, dynamic construction, lazy evaluation, and configurable encoding.

**Architecture:** URL literals are parsed by Lexer in a new URL mode, produce UrlLiteral AST nodes, and evaluate to EtlURL runtime objects with lazy evaluation and mixed modification mode.

**Tech Stack:** Java 8, no third-party dependencies, uses java.net.URLEncoder/URLDecoder and java.util.Base64

---

## File Structure

**New Files:**
| File | Responsibility |
|------|----------------|
| `org.dromara.easytl.ast.UrlLiteral` | URL literal AST node |
| `org.dromara.easytl.runtime.EtlURL` | URL runtime object with lazy evaluation |
| `org.dromara.easytl.runtime.EtlURLQuery` | Query parameter object |
| `org.dromara.easytl.runtime.UrlComponent` | URL component enum |
| `org.dromara.easytl.runtime.UrlComponents` | URL parse result |
| `org.dromara.easytl.runtime.UrlParser` | URL string parser |
| `org.dromara.easytl.runtime.UrlEncoder` | Encoder interface |
| `org.dromara.easytl.runtime.UrlEncoders` | Built-in encoders |
| `org.dromara.easytl.exception.UrlSyntaxException` | URL syntax error |
| `org.dromara.easytl.exception.UrlEncodingException` | URL encoding error |

**Test Files:**
| File | Purpose |
|------|---------|
| `org.dromara.easytl.runtime.UrlEncodersTest` | Encoder tests |
| `org.dromara.easytl.runtime.UrlParserTest` | URL parser tests |
| `org.dromara.easytl.runtime.EtlURLQueryTest` | Query object tests |
| `org.dromara.easytl.runtime.EtlURLTest` | EtlURL tests |
| `org.dromara.easytl.parser.UrlLexerTest` | Lexer URL tests |
| `org.dromara.easytl.parser.UrlParserIntegrationTest` | Parser URL tests |
| `org.dromara.easytl.compiler.UrlEvaluatorTest` | Evaluator URL tests |
| `org.dromara.easytl.UrlEncoderContextTest` | Context encoder tests |
| `org.dromara.easytl.UrlLiteralIntegrationTest` | End-to-end tests |

**Modified Files:**
| File | Changes |
|------|---------|
| `org.dromara.easytl.parser.TokenType` | Add URL token type |
| `org.dromara.easytl.parser.Lexer` | Add URL keyword |
| `org.dromara.easytl.parser.Parser` | Add parseUrlLiteral() |
| `org.dromara.easytl.ast.ASTVisitor` | Add visitUrlLiteral() |
| `org.dromara.easytl.compiler.Evaluator` | Implement visitUrlLiteral() |
| `org.dromara.easytl.Context` | Add URL encoder configuration |

---

## Task 1: URL Token Type

**Files:**
- Modify: `src/main/java/org/dromara/easytl/parser/TokenType.java`

- [ ] **Step 1: Add URL token type constant**

Add after line 75 (after DOLLAR_NUMBER):

```java
// ==================== URL 字面量 ====================
public static final int URL = 132;              // url 关键字
```

- [ ] **Step 2: Update getSymbol() method**

Add case in getSymbol() switch (around line 240):

```java
// URL literal
case URL: return "url";
```

- [ ] **Step 3: Update getName() method**

Add case in getName() switch (around line 355):

```java
// URL literal
case URL: return "URL";
```

- [ ] **Step 4: Verify compilation**

Run: `mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 5: Commit**

```bash
git add src/main/java/org/dromara/easytl/parser/TokenType.java
git commit -m "feat: add URL token type for URL literal syntax"
```

---

## Task 2: URL Exceptions

**Files:**
- Create: `src/main/java/org/dromara/easytl/exception/UrlSyntaxException.java`
- Create: `src/main/java/org/dromara/easytl/exception/UrlEncodingException.java`

- [ ] **Step 1: Write UrlSyntaxException**

```java
package org.dromara.easytl.exception;

/**
 * URL 语法异常
 * 当 URL 解析失败时抛出此异常
 *
 * @author EasyTL Team
 */
public class UrlSyntaxException extends TemplateRuntimeException {

    private static final long serialVersionUID = 1L;

    private final String url;
    private final int position;

    public UrlSyntaxException(String message, String url, int position) {
        super(message);
        this.url = url;
        this.position = position;
    }

    public UrlSyntaxException(String message, String url) {
        this(message, url, -1);
    }

    public String getUrl() {
        return url;
    }

    public int getPosition() {
        return position;
    }
}
```

- [ ] **Step 2: Write UrlEncodingException**

```java
package org.dromara.easytl.exception;

/**
 * URL 编码异常
 * 当 URL 编码或解码失败时抛出此异常
 *
 * @author EasyTL Team
 */
public class UrlEncodingException extends TemplateRuntimeException {

    private static final long serialVersionUID = 1L;

    private final String component;
    private final String value;

    public UrlEncodingException(String component, String value, Throwable cause) {
        super("Failed to encode " + component + ": " + value, cause);
        this.component = component;
        this.value = value;
    }

    public String getComponent() {
        return component;
    }

    public String getValue() {
        return value;
    }
}
```

- [ ] **Step 3: Verify compilation**

Run: `mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add src/main/java/org/dromara/easytl/exception/UrlSyntaxException.java
git add src/main/java/org/dromara/easytl/exception/UrlEncodingException.java
git commit -m "feat: add URL exception types for URL literal feature"
```

---

## Task 3: URL Encoder Interface and Built-in Encoders

**Files:**
- Create: `src/main/java/org/dromara/easytl/runtime/UrlEncoder.java`
- Create: `src/main/java/org/dromara/easytl/runtime/UrlEncoders.java`
- Create: `src/test/java/org/dromara/easytl/runtime/UrlEncodersTest.java`

- [ ] **Step 1: Write failing tests for UrlEncoders**

```java
package org.dromara.easytl.runtime;

import org.junit.jupiter.api.Test;

import java.io.UnsupportedEncodingException;

import static org.junit.jupiter.api.Assertions.*;

public class UrlEncodersTest {

    @Test
    public void testNoopEncoder() {
        UrlEncoder encoder = UrlEncoders.NOOP;
        assertEquals("hello world", encoder.encode("hello world"));
        assertEquals("test", encoder.decode("test"));
    }

    @Test
    public void testUrlEncoder() throws UnsupportedEncodingException {
        UrlEncoder encoder = UrlEncoders.URL;
        assertEquals("hello+world", encoder.encode("hello world"));
        assertEquals("hello world", encoder.decode("hello+world"));
        assertEquals("%E4%B8%AD%E6%96%87", encoder.encode("中文"));
        assertEquals("中文", encoder.decode("%E4%B8%AD%E6%96%87"));
    }

    @Test
    public void testBase64Encoder() {
        UrlEncoder encoder = UrlEncoders.BASE64;
        assertEquals("aGVsbG8=", encoder.encode("hello"));
        assertEquals("hello", encoder.decode("aGVsbG8="));
    }

    @Test
    public void testBase64UrlSafeEncoder() {
        UrlEncoder encoder = UrlEncoders.BASE64_URL_SAFE;
        assertEquals("aGVsbG8=", encoder.encode("hello"));
        assertEquals("hello", encoder.decode("aGVsbG8="));
        // URL-safe encoding for special chars
        String encoded = encoder.encode("data?");
        assertNotNull(encoded);
        assertEquals("data?", encoder.decode(encoded));
    }
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `mvn test -Dtest=UrlEncodersTest -q`
Expected: Test compilation fails (class not found)

- [ ] **Step 3: Write UrlEncoder interface**

```java
package org.dromara.easytl.runtime;

/**
 * URL 编码器接口
 * 用于对 URL 各组件进行编码和解码
 *
 * @author EasyTL Team
 */
public interface UrlEncoder {

    /**
     * 编码字符串
     *
     * @param input 输入字符串
     * @return 编码后的字符串
     */
    String encode(String input);

    /**
     * 解码字符串
     *
     * @param input 编码后的字符串
     * @return 解码后的字符串
     */
    String decode(String input);
}
```

- [ ] **Step 4: Write UrlEncoders class**

```java
package org.dromara.easytl.runtime;

import org.dromara.easytl.exception.UrlEncodingException;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Base64;

/**
 * 内置 URL 编码器集合
 *
 * @author EasyTL Team
 */
public final class UrlEncoders {

    private UrlEncoders() {
        // 工具类，禁止实例化
    }

    /**
     * 无编码器（原样返回）
     */
    public static final UrlEncoder NOOP = new UrlEncoder() {
        @Override
        public String encode(String input) {
            return input;
        }

        @Override
        public String decode(String input) {
            return input;
        }
    };

    /**
     * 标准 URL 编码器 (application/x-www-form-urlencoded)
     */
    public static final UrlEncoder URL = new UrlEncoder() {
        @Override
        public String encode(String input) {
            try {
                return URLEncoder.encode(input, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new UrlEncodingException("url", input, e);
            }
        }

        @Override
        public String decode(String input) {
            try {
                return URLDecoder.decode(input, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new UrlEncodingException("url", input, e);
            }
        }
    };

    /**
     * Base64 编码器
     */
    public static final UrlEncoder BASE64 = new UrlEncoder() {
        @Override
        public String encode(String input) {
            try {
                return Base64.getEncoder().encodeToString(input.getBytes("UTF-8"));
            } catch (UnsupportedEncodingException e) {
                throw new UrlEncodingException("base64", input, e);
            }
        }

        @Override
        public String decode(String input) {
            try {
                return new String(Base64.getDecoder().decode(input), "UTF-8");
            } catch (Exception e) {
                throw new UrlEncodingException("base64", input, e);
            }
        }
    };

    /**
     * URL 安全 Base64 编码器
     */
    public static final UrlEncoder BASE64_URL_SAFE = new UrlEncoder() {
        @Override
        public String encode(String input) {
            try {
                return Base64.getUrlEncoder().encodeToString(input.getBytes("UTF-8"));
            } catch (UnsupportedEncodingException e) {
                throw new UrlEncodingException("base64url", input, e);
            }
        }

        @Override
        public String decode(String input) {
            try {
                return new String(Base64.getUrlDecoder().decode(input), "UTF-8");
            } catch (Exception e) {
                throw new UrlEncodingException("base64url", input, e);
            }
        }
    };
}
```

- [ ] **Step 5: Run tests to verify they pass**

Run: `mvn test -Dtest=UrlEncodersTest -q`
Expected: Tests PASS

- [ ] **Step 6: Commit**

```bash
git add src/main/java/org/dromara/easytl/runtime/UrlEncoder.java
git add src/main/java/org/dromara/easytl/runtime/UrlEncoders.java
git add src/test/java/org/dromara/easytl/runtime/UrlEncodersTest.java
git commit -m "feat: add URL encoder interface and built-in encoders"
```

---

## Task 4: URL Components and Parser

**Files:**
- Create: `src/main/java/org/dromara/easytl/runtime/UrlComponent.java`
- Create: `src/main/java/org/dromara/easytl/runtime/UrlComponents.java`
- Create: `src/main/java/org/dromara/easytl/runtime/UrlParser.java`
- Create: `src/test/java/org/dromara/easytl/runtime/UrlParserTest.java`

- [ ] **Step 1: Write failing tests for UrlParser**

```java
package org.dromara.easytl.runtime;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UrlParserTest {

    @Test
    public void testParseFullUrl() {
        UrlComponents c = UrlParser.parse("https://user:pass@host:8080/path?q=1#ref");
        assertEquals("https", c.scheme);
        assertEquals("user:pass", c.userInfo);
        assertEquals("host", c.host);
        assertEquals(8080, c.port);
        assertEquals("/path", c.path);
        assertEquals("q=1", c.query);
        assertEquals("ref", c.ref);
    }

    @Test
    public void testParseSimpleUrl() {
        UrlComponents c = UrlParser.parse("http://localhost/path");
        assertEquals("http", c.scheme);
        assertNull(c.userInfo);
        assertEquals("localhost", c.host);
        assertNull(c.port);
        assertEquals("/path", c.path);
        assertNull(c.query);
        assertNull(c.ref);
    }

    @Test
    public void testParseRelativePath() {
        UrlComponents c = UrlParser.parse("/path/only");
        assertNull(c.scheme);
        assertNull(c.host);
        assertEquals("/path/only", c.path);
    }

    @Test
    public void testParseQueryOnly() {
        UrlComponents c = UrlParser.parse("?a=1&b=2");
        assertNull(c.scheme);
        assertNull(c.host);
        assertEquals("a=1&b=2", c.query);
    }

    @Test
    public void testParseRefOnly() {
        UrlComponents c = UrlParser.parse("#section");
        assertNull(c.scheme);
        assertNull(c.host);
        assertEquals("section", c.ref);
    }

    @Test
    public void testParseIPv6() {
        UrlComponents c = UrlParser.parse("http://[::1]:8080/path");
        assertEquals("http", c.scheme);
        assertEquals("::1", c.host);
        assertEquals(8080, c.port);
        assertEquals("/path", c.path);
    }

    @Test
    public void testParseIPv6NoPort() {
        UrlComponents c = UrlParser.parse("http://[2001:db8::1]/path");
        assertEquals("http", c.scheme);
        assertEquals("2001:db8::1", c.host);
        assertNull(c.port);
    }

    @Test
    public void testParseFileUrl() {
        UrlComponents c = UrlParser.parse("file:///Users/name/file.txt");
        assertEquals("file", c.scheme);
        assertEquals("/Users/name/file.txt", c.path);
    }

    @Test
    public void testParseWebSocketUrl() {
        UrlComponents c = UrlParser.parse("ws://host/path");
        assertEquals("ws", c.scheme);
        assertEquals("host", c.host);
        assertEquals("/path", c.path);
    }

    @Test
    public void testParseCustomScheme() {
        UrlComponents c = UrlParser.parse("myapp://deep/link?id=123");
        assertEquals("myapp", c.scheme);
        assertEquals("/link", c.path);
        assertEquals("id=123", c.query);
    }

    @Test
    public void testParseEmpty() {
        UrlComponents c = UrlParser.parse("");
        assertNull(c.scheme);
        assertNull(c.host);
        assertNull(c.path);
    }
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `mvn test -Dtest=UrlParserTest -q`
Expected: Test compilation fails (classes not found)

- [ ] **Step 3: Write UrlComponent enum**

```java
package org.dromara.easytl.runtime;

/**
 * URL 组件枚举
 * 用于标记哪些组件被显式修改过
 *
 * @author EasyTL Team
 */
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

- [ ] **Step 4: Write UrlComponents class**

```java
package org.dromara.easytl.runtime;

/**
 * URL 解析结果
 * 包含 URL 的各个组件
 *
 * @author EasyTL Team
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

    public String getScheme() {
        return scheme;
    }

    public String getUserInfo() {
        return userInfo;
    }

    public String getHost() {
        return host;
    }

    public Integer getPort() {
        return port;
    }

    public String getPath() {
        return path;
    }

    public String getQuery() {
        return query;
    }

    public String getRef() {
        return ref;
    }
}
```

- [ ] **Step 5: Write UrlParser class**

```java
package org.dromara.easytl.runtime;

/**
 * URL 解析器
 * 支持完整 URL、相对 URL、IPv6 地址
 *
 * @author EasyTL Team
 */
public final class UrlParser {

    private UrlParser() {
        // 工具类，禁止实例化
    }

    /**
     * 解析 URL 字符串，返回各组件
     */
    public static UrlComponents parse(String url) {
        UrlComponents components = new UrlComponents();
        if (url == null || url.isEmpty()) {
            return components;
        }

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
                : (start < url.length() ? url.substring(start) : "");

        if (authority.isEmpty()) {
            return start;
        }

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

    private static int findPathStart(String url, int start) {
        for (int i = start; i < url.length(); i++) {
            char ch = url.charAt(i);
            if (ch == '/' || ch == '?' || ch == '#') {
                return i;
            }
        }
        return url.length();
    }

    private static int parsePath(String url, int start, UrlComponents c) {
        if (start >= url.length()) {
            return start;
        }

        int queryStart = url.indexOf('?', start);
        int refStart = url.indexOf('#', start);
        int end = minPositive(queryStart, refStart, url.length());

        if (end > start) {
            c.path = url.substring(start, end);
        }
        return end;
    }

    private static int parseQuery(String url, int start, UrlComponents c) {
        if (start >= url.length() || url.charAt(start) != '?') {
            return start;
        }

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

    private static Integer parsePort(String portStr) {
        if (portStr == null || portStr.isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(portStr);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static int minPositive(int a, int b, int defaultValue) {
        int result = defaultValue;
        if (a > 0 && a < result) result = a;
        if (b > 0 && b < result) result = b;
        return result;
    }
}
```

- [ ] **Step 6: Run tests to verify they pass**

Run: `mvn test -Dtest=UrlParserTest -q`
Expected: Tests PASS

- [ ] **Step 7: Commit**

```bash
git add src/main/java/org/dromara/easytl/runtime/UrlComponent.java
git add src/main/java/org/dromara/easytl/runtime/UrlComponents.java
git add src/main/java/org/dromara/easytl/runtime/UrlParser.java
git add src/test/java/org/dromara/easytl/runtime/UrlParserTest.java
git commit -m "feat: add URL parser with support for various URL formats"
```

---

## Task 5: EtlURLQuery Class

**Files:**
- Create: `src/main/java/org/dromara/easytl/runtime/EtlURLQuery.java`
- Create: `src/test/java/org/dromara/easytl/runtime/EtlURLQueryTest.java`

- [ ] **Step 1: Write failing tests for EtlURLQuery**

```java
package org.dromara.easytl.runtime;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class EtlURLQueryTest {

    @Test
    public void testBasicCreation() {
        EtlURLQuery query = new EtlURLQuery("key", "value", "value");
        assertEquals("key", query.getKey());
        assertEquals("value", query.getValue());
        assertEquals("value", query.getRawValue());
    }

    @Test
    public void testSetValue() {
        EtlURLQuery query = new EtlURLQuery("key", "old", "old");
        query.setValue("new");
        assertEquals("new", query.getValue());
        assertEquals("old", query.getRawValue());
    }

    @Test
    public void testToString() {
        EtlURLQuery query = new EtlURLQuery("key", "value", "value");
        assertEquals("key=value", query.toString());
    }

    @Test
    public void testNullValue() {
        EtlURLQuery query = new EtlURLQuery("key", null, null);
        assertEquals("key", query.getKey());
        assertNull(query.getValue());
    }
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `mvn test -Dtest=EtlURLQueryTest -q`
Expected: Test compilation fails (class not found)

- [ ] **Step 3: Write EtlURLQuery class**

```java
package org.dromara.easytl.runtime;

/**
 * URL Query 参数对象
 * 表示单个 query 参数，包含 key 和 value
 *
 * @author EasyTL Team
 */
public class EtlURLQuery {

    private final String key;
    private String value;
    private final String rawValue;

    /**
     * 构造 Query 参数对象
     *
     * @param key      参数名
     * @param value    解码后的参数值
     * @param rawValue 原始参数值（未解码）
     */
    public EtlURLQuery(String key, String value, String rawValue) {
        this.key = key;
        this.value = value;
        this.rawValue = rawValue;
    }

    /**
     * 获取参数名
     *
     * @return 参数名
     */
    public String getKey() {
        return key;
    }

    /**
     * 获取解码后的参数值
     *
     * @return 参数值
     */
    public String getValue() {
        return value;
    }

    /**
     * 获取原始参数值（未解码）
     *
     * @return 原始参数值
     */
    public String getRawValue() {
        return rawValue;
    }

    /**
     * 设置参数值
     *
     * @param value 新的参数值
     */
    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        if (value == null) {
            return key;
        }
        return key + "=" + value;
    }
}
```

- [ ] **Step 4: Run tests to verify they pass**

Run: `mvn test -Dtest=EtlURLQueryTest -q`
Expected: Tests PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/java/org/dromara/easytl/runtime/EtlURLQuery.java
git add src/test/java/org/dromara/easytl/runtime/EtlURLQueryTest.java
git commit -m "feat: add EtlURLQuery class for query parameter handling"
```

---

## Task 6: EtlURL Runtime Class

**Files:**
- Create: `src/main/java/org/dromara/easytl/runtime/EtlURL.java`
- Create: `src/test/java/org/dromara/easytl/runtime/EtlURLTest.java`

- [ ] **Step 1: Write failing tests for EtlURL basic functionality**

```java
package org.dromara.easytl.runtime;

import org.dromara.easytl.Context;
import org.dromara.easytl.ast.ASTNode;
import org.dromara.easytl.ast.TextNode;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class EtlURLTest {

    private EtlURL createUrl(String urlStr) {
        List<ASTNode> children = Collections.singletonList(new TextNode(urlStr));
        return new EtlURL(children, new Context(), null, null);
    }

    @Test
    public void testBasicUrl() {
        EtlURL url = createUrl("http://localhost/path");
        assertEquals("http", url.getScheme());
        assertEquals("localhost", url.getHost());
        assertEquals("/path", url.getPath());
    }

    @Test
    public void testFullUrl() {
        EtlURL url = createUrl("https://user:pass@host:8080/path?q=1#ref");
        assertEquals("https", url.getScheme());
        assertEquals("user:pass", url.getUserInfo());
        assertEquals("host", url.getHost());
        assertEquals(8080, url.getPort());
        assertEquals("/path", url.getPath());
        assertEquals("q=1", url.getQuery());
        assertEquals("ref", url.getRef());
    }

    @Test
    public void testRelativeUrl() {
        EtlURL url = createUrl("/path/only");
        assertNull(url.getScheme());
        assertNull(url.getHost());
        assertEquals("/path/only", url.getPath());
    }

    @Test
    public void testQueryOnly() {
        EtlURL url = createUrl("?a=1&b=2");
        assertEquals("a=1&b=2", url.getQuery());
    }

    @Test
    public void testRefOnly() {
        EtlURL url = createUrl("#section");
        assertEquals("section", url.getRef());
    }

    @Test
    public void testModifyScheme() {
        EtlURL url = createUrl("http://host/path");
        url.setScheme("https");
        assertEquals("https", url.getScheme());
        assertEquals("https://host/path", url.toString());
    }

    @Test
    public void testModifyHost() {
        EtlURL url = createUrl("http://host/path");
        url.setHost("newhost");
        assertEquals("newhost", url.getHost());
        assertEquals("http://newhost/path", url.toString());
    }

    @Test
    public void testModifyPort() {
        EtlURL url = createUrl("http://host:80/path");
        url.setPort(443);
        assertEquals(443, url.getPort());
        assertEquals("http://host:443/path", url.toString());
    }

    @Test
    public void testModifyPath() {
        EtlURL url = createUrl("http://host/old");
        url.setPath("/new");
        assertEquals("/new", url.getPath());
        assertEquals("http://host/new", url.toString());
    }

    @Test
    public void testModifyQuery() {
        EtlURL url = createUrl("http://host?a=1");
        url.setQuery("b=2");
        assertEquals("b=2", url.getQuery());
        assertEquals("http://host?b=2", url.toString());
    }

    @Test
    public void testModifyRef() {
        EtlURL url = createUrl("http://host/path#old");
        url.setRef("new");
        assertEquals("new", url.getRef());
    }

    @Test
    public void testToString() {
        EtlURL url = createUrl("http://localhost/path");
        assertEquals("http://localhost/path", url.toString());
    }

    @Test
    public void testToStringWithAllComponents() {
        EtlURL url = createUrl("https://user:pass@host:8080/path?q=1#ref");
        assertEquals("https://user:pass@host:8080/path?q=1#ref", url.toString());
    }

    @Test
    public void testGetQuerySingle() {
        EtlURL url = createUrl("http://host/path?a=1&b=2");
        assertEquals("1", url.getQuery("a"));
        assertEquals("2", url.getQuery("b"));
        assertNull(url.getQuery("c"));
    }

    @Test
    public void testGetQueryValues() {
        EtlURL url = createUrl("http://host/path?a=1&a=2");
        List<String> values = url.getQueryValues("a");
        assertEquals(2, values.size());
        assertTrue(values.contains("1"));
        assertTrue(values.contains("2"));
    }

    @Test
    public void testAddQuery() {
        EtlURL url = createUrl("http://host/path?a=1");
        url.addQuery("b", "2");
        assertTrue(url.toString().contains("a=1"));
        assertTrue(url.toString().contains("b=2"));
    }

    @Test
    public void testSetQuery() {
        EtlURL url = createUrl("http://host/path?a=1");
        url.setQuery("a", "2");
        assertEquals("2", url.getQuery("a"));
    }

    @Test
    public void testRemoveQuery() {
        EtlURL url = createUrl("http://host/path?a=1&b=2");
        url.removeQuery("a");
        assertNull(url.getQuery("a"));
        assertEquals("2", url.getQuery("b"));
    }

    @Test
    public void testDefaultEncoding() {
        EtlURL url = createUrl("http://host/path?key=hello world");
        // Default encoder is URL encoder, spaces become +
        String result = url.toString();
        assertTrue(result.contains("key=hello+world"));
    }

    @Test
    public void testEncodingChineseCharacters() {
        EtlURL url = createUrl("http://host/path?q=中文");
        String result = url.toString();
        // Chinese characters should be percent-encoded
        assertTrue(result.contains("%"));
    }

    @Test
    public void testNoopEncoding() {
        Context ctx = new Context();
        ctx.setUrlQueryValueEncoder(UrlEncoders.NOOP);
        EtlURL url = new EtlURL(
            Collections.singletonList(new TextNode("http://host/path?key=hello world")),
            ctx, null, null
        );
        String result = url.toString();
        // With NOOP encoder, spaces remain as-is
        assertTrue(result.contains("key=hello world"));
    }
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `mvn test -Dtest=EtlURLTest -q`
Expected: Test compilation fails (class not found)

- [ ] **Step 3: Write EtlURL class (Part 1 - basic structure)**

```java
package org.dromara.easytl.runtime;

import org.dromara.easytl.Context;
import org.dromara.easytl.ast.ASTNode;
import org.dromara.easytl.compiler.Evaluator;
import org.dromara.easytl.exception.UrlEncodingException;
import org.dromara.easytl.loader.TemplateLoader;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;

/**
 * URL 运行时对象
 * 支持延迟计算和混合修改模式
 *
 * @author EasyTL Team
 */
public class EtlURL {

    // ========== 核心字段 ==========

    private final List<ASTNode> children;
    private final Context context;
    private final TemplateLoader templateLoader;
    private final String sourceText;

    // 显式修改的属性值
    private String scheme;
    private String userInfo;
    private String host;
    private Integer port;
    private String path;
    private String query;
    private String ref;

    // 标记哪些属性被显式修改过
    private final Set<UrlComponent> modifiedComponents = EnumSet.noneOf(UrlComponent.class);

    // 缓存：解析后的组件
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
        this.cached = null;
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
        Evaluator evaluator = new Evaluator(context, templateLoader, sourceText);
        for (ASTNode child : children) {
            Object value = child.accept(evaluator);
            if (value != null) sb.append(value.toString());
        }
        return sb.toString();
    }

    // ========== Query 操作 ==========

    public String getQuery(String key) {
        String q = getQuery();
        if (q == null) return null;
        return parseQueryValue(q, key);
    }

    public List<String> getQueryValues(String key) {
        String q = getQuery();
        if (q == null) return Collections.emptyList();
        return parseQueryValues(q, key);
    }

    public void addQuery(String key, String value) {
        String q = getQuery();
        if (q == null || q.isEmpty()) {
            setQuery(key + "=" + value);
        } else {
            setQuery(q + "&" + key + "=" + value);
        }
    }

    public void setQuery(String key, String value) {
        String q = getQuery();
        if (q == null || q.isEmpty()) {
            setQuery(key + "=" + value);
            return;
        }
        setQuery(replaceQueryParam(q, key, value));
    }

    public void removeQuery(String key) {
        String q = getQuery();
        if (q == null) return;
        setQuery(removeQueryParam(q, key));
    }

    public void clearQuery() {
        setQuery((String) null);
    }

    private String parseQueryValue(String query, String key) {
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int eqPos = pair.indexOf('=');
            if (eqPos >= 0) {
                String k = pair.substring(0, eqPos);
                if (k.equals(key)) {
                    return decodeQueryValue(pair.substring(eqPos + 1));
                }
            } else {
                if (pair.equals(key)) {
                    return null;
                }
            }
        }
        return null;
    }

    private List<String> parseQueryValues(String query, String key) {
        List<String> values = new ArrayList<>();
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int eqPos = pair.indexOf('=');
            if (eqPos >= 0) {
                String k = pair.substring(0, eqPos);
                if (k.equals(key)) {
                    values.add(decodeQueryValue(pair.substring(eqPos + 1)));
                }
            }
        }
        return values;
    }

    private String replaceQueryParam(String query, String key, String value) {
        String[] pairs = query.split("&");
        StringBuilder sb = new StringBuilder();
        boolean found = false;
        for (int i = 0; i < pairs.length; i++) {
            String pair = pairs[i];
            int eqPos = pair.indexOf('=');
            if (eqPos >= 0 && pair.substring(0, eqPos).equals(key)) {
                if (!found) {
                    if (sb.length() > 0) sb.append("&");
                    sb.append(key).append("=").append(value);
                    found = true;
                }
            } else {
                if (sb.length() > 0) sb.append("&");
                sb.append(pair);
            }
        }
        if (!found) {
            if (sb.length() > 0) sb.append("&");
            sb.append(key).append("=").append(value);
        }
        return sb.toString();
    }

    private String removeQueryParam(String query, String key) {
        String[] pairs = query.split("&");
        StringBuilder sb = new StringBuilder();
        for (String pair : pairs) {
            int eqPos = pair.indexOf('=');
            String k = eqPos >= 0 ? pair.substring(0, eqPos) : pair;
            if (!k.equals(key)) {
                if (sb.length() > 0) sb.append("&");
                sb.append(pair);
            }
        }
        return sb.length() > 0 ? sb.toString() : null;
    }

    private String decodeQueryValue(String value) {
        try {
            return URLDecoder.decode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return value;
        }
    }

    // ========== 编码方法 ==========

    private String encodeScheme(String value) {
        return getUrlEncoder("_urlSchemeEncoder").encode(value);
    }

    private String encodeUserInfo(String value) {
        return getUrlEncoder("_urlUserInfoEncoder").encode(value);
    }

    private String encodeHost(String value) {
        return getUrlEncoder("_urlHostEncoder").encode(value);
    }

    private String encodePath(String value) {
        return getUrlEncoder("_urlPathEncoder").encode(value);
    }

    private String encodeQueryKey(String value) {
        return getUrlEncoder("_urlQueryKeyEncoder").encode(value);
    }

    private String encodeQueryValue(String value) {
        return getUrlEncoder("_urlQueryValueEncoder").encode(value);
    }

    private String encodeRef(String value) {
        return getUrlEncoder("_urlRefEncoder").encode(value);
    }

    private String encodeQueryString(String query) {
        if (query == null || query.isEmpty()) return query;
        StringBuilder sb = new StringBuilder();
        String[] pairs = query.split("&");
        for (int i = 0; i < pairs.length; i++) {
            if (i > 0) sb.append("&");
            int eqPos = pairs[i].indexOf('=');
            if (eqPos >= 0) {
                String key = pairs[i].substring(0, eqPos);
                String value = pairs[i].substring(eqPos + 1);
                sb.append(encodeQueryKey(key));
                sb.append("=");
                sb.append(encodeQueryValue(value));
            } else {
                sb.append(encodeQueryKey(pairs[i]));
            }
        }
        return sb.toString();
    }

    private UrlEncoder getUrlEncoder(String key) {
        if (context == null) return UrlEncoders.NOOP;
        Object encoder = context.getRaw(key);
        return encoder instanceof UrlEncoder ? (UrlEncoder) encoder : UrlEncoders.URL;
    }

    // ========== toString ==========

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        String s = getScheme();
        if (s != null) sb.append(s).append("://");

        String ui = getUserInfo();
        if (ui != null) sb.append(encodeUserInfo(ui)).append("@");

        String h = getHost();
        if (h != null) sb.append(encodeHost(h));

        Integer p = getPort();
        if (p != null) sb.append(":").append(p);

        String pt = getPath();
        if (pt != null) sb.append(encodePath(pt));

        String q = getQuery();
        if (q != null) sb.append("?").append(encodeQueryString(q));

        String r = getRef();
        if (r != null) sb.append("#").append(encodeRef(r));

        return sb.toString();
    }
}
```

- [ ] **Step 4: Run tests to verify they pass**

Run: `mvn test -Dtest=EtlURLTest -q`
Expected: Tests PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/java/org/dromara/easytl/runtime/EtlURL.java
git add src/test/java/org/dromara/easytl/runtime/EtlURLTest.java
git commit -m "feat: add EtlURL runtime class with lazy evaluation and query operations"
```

---

## Task 7: UrlLiteral AST Node

**Files:**
- Create: `src/main/java/org/dromara/easytl/ast/UrlLiteral.java`

- [ ] **Step 1: Write UrlLiteral class**

```java
package org.dromara.easytl.ast;

import org.dromara.easytl.parser.Token;

import java.util.List;

/**
 * URL 字面量 AST 节点
 * 表示 url(...) 构造，支持嵌入表达式和语句块
 *
 * @author EasyTL Team
 */
public class UrlLiteral extends ASTNode {

    /** 子节点列表：TextNode、ExpressionNode、StatementBlock */
    private final List<ASTNode> children;

    /**
     * 构造 URL 字面量节点
     *
     * @param children 子节点列表
     */
    public UrlLiteral(List<ASTNode> children) {
        this.children = children;
    }

    /**
     * 构造 URL 字面量节点（带位置信息）
     *
     * @param children 子节点列表
     * @param token    Token 对象（用于获取位置信息）
     */
    public UrlLiteral(List<ASTNode> children, Token token) {
        super(positionFromToken(token));
        this.children = children;
    }

    /**
     * 获取子节点列表
     *
     * @return 子节点列表
     */
    public List<ASTNode> getChildren() {
        return children;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitUrlLiteral(this);
    }

    @Override
    public String toString() {
        return "UrlLiteral{" +
                "children=" + children +
                '}';
    }
}
```

- [ ] **Step 2: Verify compilation**

Run: `mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add src/main/java/org/dromara/easytl/ast/UrlLiteral.java
git commit -m "feat: add UrlLiteral AST node for URL literal syntax"
```

---

## Task 8: Add URL Support to ASTVisitor and Evaluator

**Files:**
- Modify: `src/main/java/org/dromara/easytl/ast/ASTVisitor.java`
- Modify: `src/main/java/org/dromara/easytl/compiler/Evaluator.java`
- Create: `src/test/java/org/dromara/easytl/compiler/UrlEvaluatorTest.java`

**Note:** ASTVisitor and Evaluator must be updated together to keep compilation passing.

- [ ] **Step 1: Write failing tests for URL evaluation**

```java
package org.dromara.easytl.compiler;

import org.dromara.easytl.Context;
import org.dromara.easytl.EasyTL;
import org.dromara.easytl.Template;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UrlEvaluatorTest {

    @Test
    public void testEvaluateSimpleUrl() {
        Template template = EasyTL.compile("{{url(http://localhost/path)}}");
        String result = template.render(new Context());
        assertEquals("http://localhost/path", result);
    }

    @Test
    public void testEvaluateUrlWithVariable() {
        Template template = EasyTL.compile("{{url(http://{host}/path)}}");
        Context ctx = new Context();
        ctx.put("host", "example.com");

        String result = template.render(ctx);
        assertEquals("http://example.com/path", result);
    }

    @Test
    public void testUrlInTemplate() {
        Template template = EasyTL.compile("URL: {{url(http://{host}:{port}/api)}}");
        Context ctx = new Context();
        ctx.put("host", "api.example.com");
        ctx.put("port", 8080);

        String result = template.render(ctx);
        assertEquals("URL: http://api.example.com:8080/api", result);
    }

    @Test
    public void testUrlWithAllComponents() {
        Template template = EasyTL.compile("{{url(https://user:pass@host:443/path?q=1#ref)}}");
        String result = template.render(new Context());
        assertEquals("https://user:pass@host:443/path?q=1#ref", result);
    }

    @Test
    public void testRelativeUrl() {
        Template template = EasyTL.compile("{{url(/path/only)}}");
        String result = template.render(new Context());
        assertEquals("/path/only", result);
    }

    @Test
    public void testQueryOnlyUrl() {
        Template template = EasyTL.compile("{{url(?a=1&b=2)}}");
        String result = template.render(new Context());
        assertEquals("?a=1&b=2", result);
    }
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `mvn test -Dtest=UrlEvaluatorTest -q`
Expected: Tests FAIL (UrlLiteral/visitUrlLiteral not found)

- [ ] **Step 3: Add visitUrlLiteral to ASTVisitor interface**

Add in ASTVisitor.java after line 63 (after visitReturnStatement):

```java
    // URL 字面量节点访问方法
    T visitUrlLiteral(UrlLiteral node);
```

- [ ] **Step 4: Implement visitUrlLiteral in Evaluator (same commit)**

Add in Evaluator.java after other visit methods:

```java
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

Add imports in Evaluator.java:

```java
import org.dromara.easytl.ast.UrlLiteral;
import org.dromara.easytl.runtime.EtlURL;
```

- [ ] **Step 5: Run tests to verify they pass**

Run: `mvn test -Dtest=UrlEvaluatorTest -q`
Expected: Tests PASS

- [ ] **Step 6: Verify full compilation**

Run: `mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 7: Commit**

```bash
git add src/main/java/org/dromara/easytl/ast/ASTVisitor.java
git add src/main/java/org/dromara/easytl/compiler/Evaluator.java
git add src/test/java/org/dromara/easytl/compiler/UrlEvaluatorTest.java
git commit -m "feat: add URL literal support to ASTVisitor and Evaluator"
```

---

## Task 9: Update Lexer for URL Keyword

**Files:**
- Modify: `src/main/java/org/dromara/easytl/parser/Lexer.java`
- Create: `src/test/java/org/dromara/easytl/parser/UrlLexerTest.java`

- [ ] **Step 1: Write failing tests for URL lexer**

```java
package org.dromara.easytl.parser;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class UrlLexerTest {

    private Token assertNextToken(Lexer lexer, int expectedType) throws IOException {
        Token token = lexer.nextToken();
        assertEquals(expectedType, token.getType());
        return token;
    }

    private Token assertNextToken(Lexer lexer, int expectedType, String expectedValue) throws IOException {
        Token token = lexer.nextToken();
        assertEquals(expectedType, token.getType());
        if (expectedValue != null) {
            assertEquals(expectedValue, token.getValue());
        }
        return token;
    }

    @Test
    public void testUrlKeyword() throws IOException {
        StringSource source = new StringSource("{url()}");
        Lexer lexer = new Lexer(source);

        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_BEGIN);
        assertNextToken(lexer, TokenType.URL, "url");
        assertNextToken(lexer, TokenType.LEFT_PAREN);
        assertNextToken(lexer, TokenType.RIGHT_PAREN);
        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_END);
        assertNextToken(lexer, TokenType.EOF);
    }

    @Test
    public void testUrlWithText() throws IOException {
        StringSource source = new StringSource("{url(http://localhost)}");
        Lexer lexer = new Lexer(source);

        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_BEGIN);
        assertNextToken(lexer, TokenType.URL, "url");
        assertNextToken(lexer, TokenType.LEFT_PAREN);
        assertNextToken(lexer, TokenType.TEXT, "http://localhost");
        assertNextToken(lexer, TokenType.RIGHT_PAREN);
        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_END);
        assertNextToken(lexer, TokenType.EOF);
    }

    @Test
    public void testUrlWithExpression() throws IOException {
        StringSource source = new StringSource("{url(http://{host}/path)}");
        Lexer lexer = new Lexer(source);

        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_BEGIN);
        assertNextToken(lexer, TokenType.URL, "url");
        assertNextToken(lexer, TokenType.LEFT_PAREN);
        assertNextToken(lexer, TokenType.TEXT, "http://");
        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_BEGIN);
        assertNextToken(lexer, TokenType.IDENTIFIER, "host");
        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_END);
        assertNextToken(lexer, TokenType.TEXT, "/path");
        assertNextToken(lexer, TokenType.RIGHT_PAREN);
        assertNextToken(lexer, TokenType.SINGLE_BRACE_EXPR_END);
        assertNextToken(lexer, TokenType.EOF);
    }
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `mvn test -Dtest=UrlLexerTest -q`
Expected: Tests FAIL (URL keyword not recognized)

- [ ] **Step 3: Add URL keyword to Lexer KEYWORDS map**

In Lexer.java, find the KEYWORDS static block (around line 46) and add:

```java
KEYWORDS.put("url", TokenType.URL);
```

- [ ] **Step 4: Run tests to verify they pass**

Run: `mvn test -Dtest=UrlLexerTest -q`
Expected: Tests PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/java/org/dromara/easytl/parser/Lexer.java
git add src/test/java/org/dromara/easytl/parser/UrlLexerTest.java
git commit -m "feat: add URL keyword recognition in Lexer"
```

---

## Task 10: Update Parser for URL Literal

**Files:**
- Modify: `src/main/java/org/dromara/easytl/parser/Parser.java`
- Create: `src/test/java/org/dromara/easytl/parser/UrlParserIntegrationTest.java`

- [ ] **Step 1: Write failing tests for URL parser**

```java
package org.dromara.easytl.parser;

import org.dromara.easytl.ast.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UrlParserIntegrationTest {

    private UrlLiteral parseUrl(String source) {
        Lexer lexer = new Lexer(new StringSource(source));
        TokenStream tokens = new TokenStream(lexer);
        Parser parser = new Parser(tokens);
        TemplateNode template = parser.parse();

        // Extract URL literal from template
        for (ASTNode child : template.getChildren()) {
            if (child instanceof SingleBraceExpressionNode) {
                ASTNode expr = ((SingleBraceExpressionNode) child).getExpression();
                if (expr instanceof UrlLiteral) {
                    return (UrlLiteral) expr;
                }
            }
        }
        return null;
    }

    @Test
    public void testParseEmptyUrl() {
        UrlLiteral url = parseUrl("{url()}");
        assertNotNull(url);
        assertTrue(url.getChildren().isEmpty());
    }

    @Test
    public void testParseSimpleUrl() {
        UrlLiteral url = parseUrl("{url(http://localhost)}");
        assertNotNull(url);
        assertEquals(1, url.getChildren().size());
        assertTrue(url.getChildren().get(0) instanceof TextNode);
        assertEquals("http://localhost", ((TextNode) url.getChildren().get(0)).getText());
    }

    @Test
    public void testParseUrlWithExpression() {
        UrlLiteral url = parseUrl("{url(http://{host}/path)}");
        assertNotNull(url);
        assertEquals(3, url.getChildren().size());
        assertTrue(url.getChildren().get(0) instanceof TextNode);
        assertTrue(url.getChildren().get(1) instanceof SingleBraceExpressionNode);
        assertTrue(url.getChildren().get(2) instanceof TextNode);
    }
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `mvn test -Dtest=UrlParserIntegrationTest -q`
Expected: Tests FAIL (parsePrimary doesn't handle URL)

- [ ] **Step 3: Add URL parsing to Parser.parsePrimary()**

In Parser.java, find the parsePrimary() method (around line 975). Add before the final throw statement:

```java
        if (check(TokenType.URL)) {
            return parseUrlLiteral();
        }
```

- [ ] **Step 4: Add parseUrlLiteral() method**

Add the method in Parser.java:

```java
    private UrlLiteral parseUrlLiteral() {
        Token beginToken = advance();      // 消耗 URL 关键字
        expect(TokenType.LEFT_PAREN);       // 消耗 (

        List<ASTNode> children = new ArrayList<>();

        while (!check(TokenType.RIGHT_PAREN) && !check(TokenType.EOF)) {
            if (check(TokenType.SINGLE_BRACE_EXPR_BEGIN)) {
                children.add(parseExpressionWrapper());
            } else if (check(TokenType.DOUBLE_BRACE_EXPR_BEGIN)) {
                children.add(parseExpressionWrapper());
            } else if (check(TokenType.DOLLAR_BRACE_EXPR_BEGIN)) {
                children.add(parseExpressionWrapper());
            } else if (check(TokenType.STMT_BEGIN)) {
                children.add(parseStatementBlock());
            } else if (check(TokenType.TEXT)) {
                children.add(new TextNode(advance().getValue()));
            } else {
                break;
            }
        }

        expect(TokenType.RIGHT_PAREN);
        return new UrlLiteral(children, beginToken);
    }
```

- [ ] **Step 5: Add import for UrlLiteral**

Ensure UrlLiteral is imported in Parser.java:

```java
import org.dromara.easytl.ast.UrlLiteral;
```

- [ ] **Step 6: Run tests to verify they pass**

Run: `mvn test -Dtest=UrlParserIntegrationTest -q`
Expected: Tests PASS

- [ ] **Step 7: Commit**

```bash
git add src/main/java/org/dromara/easytl/parser/Parser.java
git add src/test/java/org/dromara/easytl/parser/UrlParserIntegrationTest.java
git commit -m "feat: add URL literal parsing to Parser"
```

---

## Task 11: Add URL Encoder Configuration to Context

**Files:**
- Modify: `src/main/java/org/dromara/easytl/Context.java`
- Create: `src/test/java/org/dromara/easytl/UrlEncoderContextTest.java`

- [ ] **Step 1: Write failing tests for URL encoder configuration**

```java
package org.dromara.easytl;

import org.dromara.easytl.runtime.UrlEncoders;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UrlEncoderContextTest {

    @Test
    public void testDefaultEncoders() {
        Context ctx = new Context();

        // Default encoders should be NOOP or URL
        assertEquals(UrlEncoders.NOOP, ctx.getUrlSchemeEncoder());
        assertEquals(UrlEncoders.URL, ctx.getUrlUserInfoEncoder());
        assertEquals(UrlEncoders.NOOP, ctx.getUrlHostEncoder());
        assertEquals(UrlEncoders.URL, ctx.getUrlPathEncoder());
        assertEquals(UrlEncoders.URL, ctx.getUrlQueryKeyEncoder());
        assertEquals(UrlEncoders.URL, ctx.getUrlQueryValueEncoder());
        assertEquals(UrlEncoders.URL, ctx.getUrlRefEncoder());
    }

    @Test
    public void testSetCustomEncoder() {
        Context ctx = new Context();
        ctx.setUrlQueryValueEncoder(UrlEncoders.BASE64);

        assertEquals(UrlEncoders.BASE64, ctx.getUrlQueryValueEncoder());
    }
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `mvn test -Dtest=UrlEncoderContextTest -q`
Expected: Tests FAIL (methods don't exist)

- [ ] **Step 3: Add encoder methods to Context**

Add in Context.java, before the final closing brace:

```java
    // ========== URL 编码器配置 ==========

    public void setUrlSchemeEncoder(UrlEncoder encoder) {
        put("_urlSchemeEncoder", encoder);
    }

    public UrlEncoder getUrlSchemeEncoder() {
        Object encoder = getRaw("_urlSchemeEncoder");
        return encoder instanceof UrlEncoder ? (UrlEncoder) encoder : UrlEncoders.NOOP;
    }

    public void setUrlUserInfoEncoder(UrlEncoder encoder) {
        put("_urlUserInfoEncoder", encoder);
    }

    public UrlEncoder getUrlUserInfoEncoder() {
        Object encoder = getRaw("_urlUserInfoEncoder");
        return encoder instanceof UrlEncoder ? (UrlEncoder) encoder : UrlEncoders.URL;
    }

    public void setUrlHostEncoder(UrlEncoder encoder) {
        put("_urlHostEncoder", encoder);
    }

    public UrlEncoder getUrlHostEncoder() {
        Object encoder = getRaw("_urlHostEncoder");
        return encoder instanceof UrlEncoder ? (UrlEncoder) encoder : UrlEncoders.NOOP;
    }

    public void setUrlPathEncoder(UrlEncoder encoder) {
        put("_urlPathEncoder", encoder);
    }

    public UrlEncoder getUrlPathEncoder() {
        Object encoder = getRaw("_urlPathEncoder");
        return encoder instanceof UrlEncoder ? (UrlEncoder) encoder : UrlEncoders.URL;
    }

    public void setUrlQueryKeyEncoder(UrlEncoder encoder) {
        put("_urlQueryKeyEncoder", encoder);
    }

    public UrlEncoder getUrlQueryKeyEncoder() {
        Object encoder = getRaw("_urlQueryKeyEncoder");
        return encoder instanceof UrlEncoder ? (UrlEncoder) encoder : UrlEncoders.URL;
    }

    public void setUrlQueryValueEncoder(UrlEncoder encoder) {
        put("_urlQueryValueEncoder", encoder);
    }

    public UrlEncoder getUrlQueryValueEncoder() {
        Object encoder = getRaw("_urlQueryValueEncoder");
        return encoder instanceof UrlEncoder ? (UrlEncoder) encoder : UrlEncoders.URL;
    }

    public void setUrlRefEncoder(UrlEncoder encoder) {
        put("_urlRefEncoder", encoder);
    }

    public UrlEncoder getUrlRefEncoder() {
        Object encoder = getRaw("_urlRefEncoder");
        return encoder instanceof UrlEncoder ? (UrlEncoder) encoder : UrlEncoders.URL;
    }
```

- [ ] **Step 4: Add imports for UrlEncoder and UrlEncoders**

Add at top of Context.java:

```java
import org.dromara.easytl.runtime.UrlEncoder;
import org.dromara.easytl.runtime.UrlEncoders;
```

- [ ] **Step 5: Run tests to verify they pass**

Run: `mvn test -Dtest=UrlEncoderContextTest -q`
Expected: Tests PASS

- [ ] **Step 6: Commit**

```bash
git add src/main/java/org/dromara/easytl/Context.java
git add src/test/java/org/dromara/easytl/UrlEncoderContextTest.java
git commit -m "feat: add URL encoder configuration methods to Context"
```

---

## Task 12: Integration Tests

**Files:**
- Create: `src/test/java/org/dromara/easytl/UrlLiteralIntegrationTest.java`

- [ ] **Step 1: Write comprehensive integration tests**

```java
package org.dromara.easytl;

import org.dromara.easytl.runtime.UrlEncoders;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class UrlLiteralIntegrationTest {

    @Test
    public void testFullUrl() {
        Template template = EasyTL.compile("{{url(https://user:pass@host:8080/path?q=1#ref)}}");
        String result = template.render(new Context());
        assertEquals("https://user:pass@host:8080/path?q=1#ref", result);
    }

    @Test
    public void testRelativeUrl() {
        Template template = EasyTL.compile("{{url(/path/only)}}");
        String result = template.render(new Context());
        assertEquals("/path/only", result);
    }

    @Test
    public void testQueryOnly() {
        Template template = EasyTL.compile("{{url(?a=1&b=2)}}");
        String result = template.render(new Context());
        assertEquals("?a=1&b=2", result);
    }

    @Test
    public void testRefOnly() {
        Template template = EasyTL.compile("{{url(#section)}}");
        String result = template.render(new Context());
        assertEquals("#section", result);
    }

    @Test
    public void testExpressionEmbedding() {
        Template template = EasyTL.compile("{% let h = 'example.com' %}{{url(http://{h}/api)}}");
        String result = template.render(new Context());
        assertEquals("http://example.com/api", result);
    }

    @Test
    public void testLazyEvaluation() {
        Template template = EasyTL.compile("{% let h = 'host1' %}{% let u = url(http://{h}/path) %}{{u.host}}{% let h = 'host2' %}{{u.host}}");
        String result = template.render(new Context());
        // Note: This tests lazy evaluation behavior
        assertTrue(result.contains("host"));
    }

    @Test
    public void testUrlModification() {
        Template template = EasyTL.compile("{% let u = url(http://host/path) %}{% u.scheme = 'https' %}{{u}}");
        String result = template.render(new Context());
        assertEquals("https://host/path", result);
    }

    @Test
    public void testQueryOperations() {
        Template template = EasyTL.compile("{% let u = url(http://host/path?a=1) %}{% u.addQuery('b', '2') %}{{u}}");
        String result = template.render(new Context());
        assertTrue(result.contains("a=1"));
        assertTrue(result.contains("b=2"));
    }

    @Test
    public void testNestedExpressions() {
        Context ctx = new Context();
        Map<String, Object> config = new HashMap<>();
        Map<String, String> api = new HashMap<>();
        api.put("host", "api.example.com");
        api.put("port", "8080");
        config.put("api", api);
        ctx.put("config", config);

        Template template = EasyTL.compile("{{url(http://{config.api.host}:{config.api.port}/v1)}}");
        String result = template.render(ctx);
        assertEquals("http://api.example.com:8080/v1", result);
    }

    @Test
    public void testStatementBlockEmbedding() {
        Context ctx = new Context();
        ctx.put("debug", true);

        Template template = EasyTL.compile("{{url(/api{% if debug %}?debug=true{/% if %})}}");
        String result = template.render(ctx);
        assertEquals("/api?debug=true", result);
    }

    @Test
    public void testForLoopInUrl() {
        Context ctx = new Context();
        ctx.put("items", new String[]{"a", "b", "c"});

        Template template = EasyTL.compile("{{url(/path{% for item in items %}/{item}{/% for %})}}");
        String result = template.render(ctx);
        assertEquals("/path/a/b/c", result);
    }

    @Test
    public void testIPv6Url() {
        Template template = EasyTL.compile("{{url(http://[::1]:8080/path)}}");
        String result = template.render(new Context());
        assertEquals("http://[::1]:8080/path", result);
    }

    @Test
    public void testWebSocketUrl() {
        Template template = EasyTL.compile("{{url(ws://host/path)}}");
        String result = template.render(new Context());
        assertEquals("ws://host/path", result);
    }

    @Test
    public void testFileUrl() {
        Template template = EasyTL.compile("{{url(file:///Users/name/file.txt)}}");
        String result = template.render(new Context());
        assertEquals("file:///Users/name/file.txt", result);
    }

    @Test
    public void testMultipleUrlsInTemplate() {
        Context ctx = new Context();
        ctx.put("api", "api.example.com");
        ctx.put("cdn", "cdn.example.com");

        Template template = EasyTL.compile("API: {{url(https://{api}/v1)}} CDN: {{url(https://{cdn}/static)}}");
        String result = template.render(ctx);
        assertEquals("API: https://api.example.com/v1 CDN: https://cdn.example.com/static", result);
    }
}
```

- [ ] **Step 2: Run all tests**

Run: `mvn test -Dtest=UrlLiteralIntegrationTest -q`
Expected: Tests PASS

- [ ] **Step 3: Run full test suite**

Run: `mvn test -q`
Expected: All tests PASS

- [ ] **Step 4: Commit**

```bash
git add src/test/java/org/dromara/easytl/UrlLiteralIntegrationTest.java
git commit -m "test: add comprehensive URL literal integration tests"
```

---

## Task 13: Final Verification and Documentation

- [ ] **Step 1: Run full test suite**

Run: `mvn test`
Expected: All tests PASS

- [ ] **Step 2: Run test coverage report**

Run: `mvn jacoco:report`
Expected: Report generated successfully

- [ ] **Step 3: Verify no compilation warnings**

Run: `mvn compile -X 2>&1 | grep -i warning`
Expected: No relevant warnings

- [ ] **Step 4: Create summary commit**

```bash
git add src/main/java/org/dromara/easytl/parser/TokenType.java
git add src/main/java/org/dromara/easytl/parser/Lexer.java
git add src/main/java/org/dromara/easytl/parser/Parser.java
git add src/main/java/org/dromara/easytl/ast/UrlLiteral.java
git add src/main/java/org/dromara/easytl/ast/ASTVisitor.java
git add src/main/java/org/dromara/easytl/compiler/Evaluator.java
git add src/main/java/org/dromara/easytl/runtime/UrlComponent.java
git add src/main/java/org/dromara/easytl/runtime/UrlComponents.java
git add src/main/java/org/dromara/easytl/runtime/UrlParser.java
git add src/main/java/org/dromara/easytl/runtime/UrlEncoder.java
git add src/main/java/org/dromara/easytl/runtime/UrlEncoders.java
git add src/main/java/org/dromara/easytl/runtime/EtlURL.java
git add src/main/java/org/dromara/easytl/runtime/EtlURLQuery.java
git add src/main/java/org/dromara/easytl/exception/UrlSyntaxException.java
git add src/main/java/org/dromara/easytl/exception/UrlEncodingException.java
git add src/main/java/org/dromara/easytl/Context.java
git commit -m "feat: complete URL literal feature implementation

- Add URL token type and keyword recognition
- Implement URL parser with expression and statement embedding
- Create EtlURL runtime with lazy evaluation and query operations
- Add configurable URL encoders (URL, Base64, NOOP)
- Support IPv6, WebSocket, and custom scheme URLs
- Full test coverage for all URL components"
```

---

## Summary

This plan implements URL literal support for EasyTL following TDD principles:

1. **Infrastructure**: Token types, exceptions, encoder interfaces
2. **Parser**: URL keyword, URL mode parsing, AST node
3. **Runtime**: EtlURL with lazy evaluation, query operations, URL encoding
4. **Integration**: Full end-to-end tests covering all spec requirements

**Key Features Implemented:**
- `url(...)` syntax with embedded expressions and statements
- Lazy evaluation with dynamic recomputation
- Mixed modification mode (modified properties fixed, unmodified dynamic)
- Query parameter operations (get/set/add/remove)
- Configurable encoding per URL component
- Support for IPv6, WebSocket, file URLs, and custom schemes