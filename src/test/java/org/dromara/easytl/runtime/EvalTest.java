package org.dromara.easytl.runtime;

import org.dromara.easytl.Context;
import org.dromara.easytl.Eval;
import org.dromara.easytl.exception.TypeConversionException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Eval 测试类
 * <p>
 * 测试 Eval 工具类的基本功能
 * </p>
 *
 * @author EasyTL Team
 * @since 1.0.0
 */
class EvalTest {

    // ==================== Basic Expression Tests ====================

    @Test
    void testIntegerArithmetic() {
        Object result = Eval.eval("1 + 2");
        assertEquals(3, result);
    }

    @Test
    void testIntegerArithmetic_complex() {
        Object result = Eval.eval("10 - 3 * 2");
        assertEquals(4, result);
    }

    @Test
    void testFloatArithmetic() {
        Object result = Eval.eval("1.5 + 2.5");
        assertEquals(4.0, result);
    }

    @Test
    void testStringLiteral() {
        Object result = Eval.eval("'hello' + ' world'");
        assertEquals("hello world", result);
    }

    @Test
    void testStringLiteral_doubleQuotes() {
        Object result = Eval.eval("\"hello\" + \" world\"");
        assertEquals("hello world", result);
    }

    @Test
    void testBooleanExpression() {
        Object result = Eval.eval("true && false");
        assertEquals(false, result);
    }

    @Test
    void testBooleanExpression_or() {
        Object result = Eval.eval("true || false");
        assertEquals(true, result);
    }

    @Test
    void testBooleanExpression_comparison() {
        Object result = Eval.eval("5 > 3");
        assertEquals(true, result);
    }

    // ==================== No Type Parameter Tests ====================

    @Test
    void testNoTypeParameter_returnsInteger() {
        Object result = Eval.eval("1 + 2");
        assertTrue(result instanceof Integer);
        assertEquals(3, result);
    }

    @Test
    void testNoTypeParameter_returnsString() {
        Object result = Eval.eval("'test'");
        assertTrue(result instanceof String);
        assertEquals("test", result);
    }

    @Test
    void testNoTypeParameter_returnsBoolean() {
        Object result = Eval.eval("true");
        assertTrue(result instanceof Boolean);
        assertEquals(true, result);
    }

    @Test
    void testNoTypeParameter_returnsList() {
        Object result = Eval.eval("[1, 2, 3]");
        assertTrue(result instanceof List);
        List<?> list = (List<?>) result;
        assertEquals(3, list.size());
        assertEquals(1, list.get(0));
        assertEquals(2, list.get(1));
        assertEquals(3, list.get(2));
    }

    @Test
    void testNoTypeParameter_returnsMap() {
        // Hash literals require string keys with quotes
        Object result = Eval.eval("{\"a\": 1, \"b\": 2}");
        assertTrue(result instanceof Map);
        Map<?, ?> map = (Map<?, ?>) result;
        assertEquals(1, map.get("a"));
        assertEquals(2, map.get("b"));
    }

    // ==================== Null Handling Tests ====================

    @Test
    void testNullSource_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> {
            Eval.eval(null);
        });
    }

    @Test
    void testEmptySource_returnsNull() {
        Object result = Eval.eval("");
        assertNull(result);
    }

    @Test
    void testWhitespaceSource_returnsNull() {
        Object result = Eval.eval("   ");
        assertNull(result);
    }

    @Test
    void testWhitespaceSource_tabs_returnsNull() {
        Object result = Eval.eval("\t\n  ");
        assertNull(result);
    }

    // ==================== Type Conversion Tests ====================

    @Test
    void testIntegerToString() {
        String result = Eval.eval("42", String.class);
        assertEquals("42", result);
    }

    @Test
    void testIntegerToLong() {
        Long result = Eval.eval("42", Long.class);
        assertEquals(Long.valueOf(42L), result);
    }

    @Test
    void testIntegerToDouble() {
        Double result = Eval.eval("42", Double.class);
        assertEquals(Double.valueOf(42.0), result);
    }

    @Test
    void testIntegerToBigInteger() {
        BigInteger result = Eval.eval("42", BigInteger.class);
        assertEquals(BigInteger.valueOf(42L), result);
    }

    @Test
    void testIntegerToBigDecimal() {
        BigDecimal result = Eval.eval("42", BigDecimal.class);
        assertEquals(BigDecimal.valueOf(42L), result);
    }

    @Test
    void testStringToInteger_throws() {
        assertThrows(TypeConversionException.class, () -> {
            Eval.eval("'not a number'", Integer.class);
        });
    }

    @Test
    void testDoubleToInteger_truncates() {
        Integer result = Eval.eval("3.9", Integer.class);
        assertEquals(Integer.valueOf(3), result);
    }

    // ==================== Convenience Method Tests ====================

    @Test
    void testEvalAsInteger() {
        Integer result = Eval.evalAsInteger("1 + 2");
        assertEquals(Integer.valueOf(3), result);
    }

    @Test
    void testEvalAsLong() {
        Long result = Eval.evalAsLong("100 + 200");
        assertEquals(Long.valueOf(300L), result);
    }

    @Test
    void testEvalAsFloat() {
        Float result = Eval.evalAsFloat("1.5 + 2.5");
        assertEquals(Float.valueOf(4.0f), result, 0.001f);
    }

    @Test
    void testEvalAsDouble() {
        Double result = Eval.evalAsDouble("1.5 + 2.5");
        assertEquals(Double.valueOf(4.0), result);
    }

    @Test
    void testEvalAsBigInteger() {
        BigInteger result = Eval.evalAsBigInteger("123456789");
        assertEquals(new BigInteger("123456789"), result);
    }

    @Test
    void testEvalAsBigDecimal() {
        BigDecimal result = Eval.evalAsBigDecimal("123.456");
        assertEquals(new BigDecimal("123.456"), result);
    }

    @Test
    void testEvalAsBoolean_true() {
        Boolean result = Eval.evalAsBoolean("5 > 3");
        assertTrue(result);
    }

    @Test
    void testEvalAsBoolean_false() {
        Boolean result = Eval.evalAsBoolean("5 < 3");
        assertFalse(result);
    }

    @Test
    void testEvalAsString() {
        String result = Eval.evalAsString("'hello' + ' world'");
        assertEquals("hello world", result);
    }

    @Test
    void testEvalAsList() {
        List<?> result = Eval.evalAsList("[1, 2, 3]");
        assertEquals(3, result.size());
        assertEquals(1, result.get(0));
        assertEquals(2, result.get(1));
        assertEquals(3, result.get(2));
    }

    @Test
    void testEvalAsMap() {
        // Hash literals require string keys with quotes
        Map<?, ?> result = Eval.evalAsMap("{\"a\": 1, \"b\": 2}");
        assertEquals(2, result.size());
        assertEquals(1, result.get("a"));
        assertEquals(2, result.get("b"));
    }

    // ==================== Variables Tests ====================

    @Test
    void testLetStatement() {
        Object result = Eval.eval("let x = 10; x + 5");
        assertEquals(15, result);
    }

    @Test
    void testMultipleLetStatements() {
        Object result = Eval.eval("let a = 1; let b = 2; a + b");
        assertEquals(3, result);
    }

    @Test
    void testLastExpressionValue() {
        Object result = Eval.eval("1 + 2; 3 + 4");
        assertEquals(7, result);
    }

    @Test
    void testLetStatementWithExpression() {
        Object result = Eval.eval("let x = 1 + 2; x * 3");
        assertEquals(9, result);
    }

    // ==================== Error Tests ====================

    @Test
    void testUndefinedVariable_returnsNull() {
        // Undefined variables return null in EasyTL
        Object result = Eval.eval("undefinedVariable");
        assertNull(result);
    }

    @Test
    void testSyntaxError_throws() {
        assertThrows(Exception.class, () -> {
            Eval.eval("1 + ");
        });
    }

    @Test
    void testDivisionByZero() {
        // Division by zero in EasyTL should return Infinity for doubles
        Object result = Eval.eval("1.0 / 0");
        assertEquals(Double.POSITIVE_INFINITY, result);
    }

    // ==================== Complex Expression Tests ====================

    @Test
    void testTernaryExpression() {
        Object result = Eval.eval("5 > 3 ? 'yes' : 'no'");
        assertEquals("yes", result);
    }

    @Test
    void testElvisExpression() {
        Object result = Eval.eval("null ?? 'default'");
        assertEquals("default", result);
    }

    @Test
    void testListAccess() {
        Object result = Eval.eval("[1, 2, 3][1]");
        assertEquals(2, result);
    }

    @Test
    void testMapAccess() {
        // Hash literals require string keys with quotes
        Object result = Eval.eval("{\"a\": 1, \"b\": 2}[\"a\"]");
        assertEquals(1, result);
    }

    @Test
    void testRange() {
        Object result = Eval.eval("1..5");
        assertTrue(result instanceof org.dromara.easytl.runtime.Range);
        List<?> list = ((org.dromara.easytl.runtime.Range) result).toList();
        assertEquals(5, list.size());
    }

    // ==================== Null Return Tests ====================

    @Test
    void testEvalNullLiteral() {
        Object result = Eval.eval("null");
        assertNull(result);
    }

    @Test
    void testEvalNullLiteral_typed() {
        String result = Eval.eval("null", String.class);
        assertNull(result);
    }

    // ==================== Convenience Methods Null Handling ====================

    @Test
    void testEvalAsInteger_nullSource() {
        assertThrows(IllegalArgumentException.class, () -> {
            Eval.evalAsInteger(null);
        });
    }

    @Test
    void testEvalAsString_emptySource() {
        String result = Eval.evalAsString("");
        assertNull(result);
    }

    // ==================== Type Conversion with Null Tests ====================

    @Test
    void testNullToInteger_returnsNull() {
        // null to Integer returns null (not primitive int)
        Integer result = Eval.eval("null", Integer.class);
        assertNull(result);
    }

    @Test
    void testNullToPrimitiveInt_throws() {
        // null to primitive int should throw
        assertThrows(TypeConversionException.class, () -> {
            Eval.eval("null", int.class);
        });
    }

    @Test
    void testNullToString_returnsNull() {
        String result = Eval.eval("null", String.class);
        assertNull(result);
    }

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

    // --- Statement Block Embedding ---

    @Test
    void testEvalAsURL_ifStatementBlock() {
        // Given: Context 中有 debug 标志
        Context ctx = new Context();
        ctx.put("debug", true);

        // When: URL 中包含 if 语句块
        EtlURL url = Eval.evalAsURL("/api/users{% if debug }?debug=true{/% if }", ctx);

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
        EtlURL url = Eval.evalAsURL("/api/users{% if debug }?debug=true{/% if }", ctx);

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
        EtlURL url = Eval.evalAsURL("/search{% for tag in tags }/{tag}{/% for }", ctx);

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
    void testEvalAsURL_dynamicPropertyUpdate() {
        // Given: Context 中有多个变量
        Context ctx = new Context();
        ctx.put("scheme", "http");
        ctx.put("host", "example.com");

        EtlURL url = Eval.evalAsURL("{scheme}://{host}/path", ctx);

        // Then: 属性从 Context 正确获取
        assertEquals("http", url.getScheme());
        assertEquals("example.com", url.getHost());
        assertEquals("http://example.com/path", url.toString());
    }

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

    // ==================== Control Flow Tests ====================

    @Test
    void testEval_IfExpression() {
        Object result = Eval.eval("if (true) { 10 } else { 20 }");
        assertEquals(10, result);
    }

    @Test
    void testEval_IfExpression_FalseCondition() {
        Object result = Eval.eval("if (false) { 10 } else { 20 }");
        assertEquals(20, result);
    }

    @Test
    void testEval_ForExpression() {
        // For expression executes side effects and returns null
        // This test verifies the for expression syntax is valid and executes
        Object result = Eval.eval("for (i in [1,2,3]) { i * 2 }");
        // For expression returns null in EasyTL
        assertNull(result);
    }

    // ==================== Return Statement Tests ====================

    @Test
    void testEval_ReturnInFor() {
        // Return inside a lambda block in for
        Object result = Eval.eval("let f = (x) -> { for (i in x) { if (i == 2) { return i } } return null }; f([1,2,3])");
        assertEquals(2, result);
    }

    @Test
    void testEval_ReturnInIf() {
        // Return inside a lambda block with if
        Object result = Eval.eval("let f = () -> { if (true) { return 5 } return 10 }; f()");
        assertEquals(5, result);
    }

    @Test
    void testEval_ReturnValue() {
        Object result = Eval.eval("return 42");
        assertEquals(42, result);
    }

    // ==================== Lambda Expression Tests ====================

    @Test
    void testEval_LambdaCall() {
        // Lambda must be assigned to a variable before calling
        Object result = Eval.eval("let f = (x) -> x + 1; f(2)");
        assertEquals(3, result);
    }

    @Test
    void testEval_LambdaWithMultipleParams() {
        Object result = Eval.eval("let f = (x, y) -> x * y; f(2, 3)");
        assertEquals(6, result);
    }

    // ==================== Additional Complex Expression Tests ====================

    @Test
    void testEval_Ternary() {
        Object result = Eval.eval("true ? 1 : 2");
        assertEquals(1, result);
    }

    @Test
    void testEval_Elvis() {
        Object result = Eval.eval("null ?? 5");
        assertEquals(5, result);
    }

    @Test
    void testEval_ListLiteral() {
        Object result = Eval.eval("[1, 2, 3]");
        assertTrue(result instanceof List);
        List<?> list = (List<?>) result;
        assertEquals(3, list.size());
        assertEquals(1, list.get(0));
        assertEquals(2, list.get(1));
        assertEquals(3, list.get(2));
    }

    @Test
    void testEval_MapLiteral() {
        Object result = Eval.eval("{'a': 1, 'b': 2}");
        assertTrue(result instanceof Map);
        Map<?, ?> map = (Map<?, ?>) result;
        assertEquals(1, map.get("a"));
        assertEquals(2, map.get("b"));
    }

    // ==================== Additional Convenience Method Tests ====================

    @Test
    void testEvalAsBigDecimal_expression() {
        BigDecimal result = Eval.evalAsBigDecimal("1.5 + 2.5");
        assertEquals(new BigDecimal("4.0"), result);
    }

    @Test
    void testEvalAsBigInteger_largeResult() {
        // Use a large number literal that exceeds int range
        BigInteger result = Eval.evalAsBigInteger("1000000000000");
        assertEquals(new BigInteger("1000000000000"), result);
    }

    // ==================== Context Parameter Tests ====================

    @Test
    void testEvalWithContext_IntegerVariable() {
        Context ctx = new Context();
        ctx.put("age", 25);
        Integer result = Eval.eval("age + 1", ctx, Integer.class);
        assertEquals(Integer.valueOf(26), result);
    }

    @Test
    void testEvalWithContext_StringVariable() {
        Context ctx = new Context();
        ctx.put("name", "张三");
        String result = Eval.eval("name", ctx, String.class);
        assertEquals("张三", result);
    }

    @Test
    void testEvalWithContext_MultipleVariables() {
        Context ctx = new Context();
        ctx.put("a", 10);
        ctx.put("b", 20);
        Integer result = Eval.eval("a + b", ctx, Integer.class);
        assertEquals(Integer.valueOf(30), result);
    }

    @Test
    void testEvalWithContext_NullContext_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            Eval.eval("1 + 1", (Context) null);
        });
    }

    @Test
    void testEvalWithContext_NullContextWithType_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            Eval.eval("1 + 1", (Context) null, Integer.class);
        });
    }

    @Test
    void testEvalWithContext_EmptySource_ReturnsNull() {
        Context ctx = new Context();
        Object result = Eval.eval("", ctx);
        assertNull(result);
    }

    @Test
    void testEvalAsIntegerWithContext() {
        Context ctx = new Context();
        ctx.put("x", 5);
        Integer result = Eval.evalAsInteger("x * 2", ctx);
        assertEquals(Integer.valueOf(10), result);
    }

    @Test
    void testEvalAsLongWithContext() {
        Context ctx = new Context();
        ctx.put("value", 100L);
        Long result = Eval.evalAsLong("value + 50", ctx);
        assertEquals(Long.valueOf(150L), result);
    }

    @Test
    void testEvalAsFloatWithContext() {
        Context ctx = new Context();
        ctx.put("price", 1.5f);
        Float result = Eval.evalAsFloat("price * 2", ctx);
        assertEquals(Float.valueOf(3.0f), result, 0.001f);
    }

    @Test
    void testEvalAsDoubleWithContext() {
        Context ctx = new Context();
        ctx.put("rate", 1.5);
        Double result = Eval.evalAsDouble("rate * 2", ctx);
        assertEquals(Double.valueOf(3.0), result);
    }

    @Test
    void testEvalAsBigIntegerWithContext() {
        Context ctx = new Context();
        ctx.put("big", BigInteger.valueOf(1000));
        BigInteger result = Eval.evalAsBigInteger("big + 1", ctx);
        assertEquals(BigInteger.valueOf(1001), result);
    }

    @Test
    void testEvalAsBigDecimalWithContext() {
        Context ctx = new Context();
        ctx.put("amount", new BigDecimal("99.99"));
        BigDecimal result = Eval.evalAsBigDecimal("amount * 2", ctx);
        assertEquals(new BigDecimal("199.98"), result);
    }

    @Test
    void testEvalAsBooleanWithContext() {
        Context ctx = new Context();
        ctx.put("age", 25);
        Boolean result = Eval.evalAsBoolean("age >= 18", ctx);
        assertTrue(result);
    }

    @Test
    void testEvalAsStringWithContext() {
        Context ctx = new Context();
        ctx.put("name", "World");
        String result = Eval.evalAsString("'Hello, ' + name", ctx);
        assertEquals("Hello, World", result);
    }

    @Test
    void testEvalAsListWithContext() {
        Context ctx = new Context();
        ctx.put("items", java.util.Arrays.asList(1, 2, 3));
        List<?> result = Eval.evalAsList("items", ctx);
        assertEquals(3, result.size());
        assertEquals(1, result.get(0));
    }

    @Test
    void testEvalAsMapWithContext() {
        Context ctx = new Context();
        Map<String, Integer> data = new java.util.HashMap<>();
        data.put("a", 1);
        data.put("b", 2);
        ctx.put("data", data);
        Map<?, ?> result = Eval.evalAsMap("data", ctx);
        assertEquals(1, result.get("a"));
        assertEquals(2, result.get("b"));
    }

    @Test
    void testEvalAsURLWithContext() {
        Context ctx = new Context();
        ctx.put("host", "example.com");
        EtlURL url = Eval.evalAsURL("http://${host}/path", ctx);
        assertEquals("http://example.com/path", url.toString());
    }

    @Test
    void testEvalAsURLWithContext_NullContext_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            Eval.evalAsURL("http://example.com", (Context) null);
        });
    }
}