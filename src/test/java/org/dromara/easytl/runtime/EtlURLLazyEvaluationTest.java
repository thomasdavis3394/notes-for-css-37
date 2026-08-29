package org.dromara.easytl.runtime;

import org.dromara.easytl.Context;
import org.dromara.easytl.Eval;
import org.dromara.easytl.exception.TemplateRuntimeException;
import org.dromara.easytl.testutil.ErrorPointerAssert;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EtlURL 延迟计算测试
 */
public class EtlURLLazyEvaluationTest {

    @Test
    public void testLazyEvaluation_FullUrlChanges() {
        Context ctx = new Context();
        ctx.put("baseUrl", "https://api.example.com?a=111");

        EtlURL url = Eval.evalAsURL("{baseUrl}", ctx);
        // 初始值
        assertEquals("https", url.getScheme());
        assertNull(url.getPath());
        assertEquals("a=111", url.getQuery());
        assertEquals("api.example.com", url.getHost());
        assertEquals("https://api.example.com?a=111", url.toString());

        // 修改 Context 中的变量
        ctx.put("baseUrl", "http://localhost:8080/v1/users?email=test@example.com&password=test");

        // 属性值应该自动更新
        assertEquals("http", url.getScheme());
        assertEquals("localhost", url.getHost());
        assertEquals(8080, url.getPort());
        assertEquals("/v1/users", url.getPath());
        assertEquals("email=test@example.com&password=test", url.getQuery());
        assertEquals("test@example.com", url.getQuery("email"));
        assertEquals("http://localhost:8080/v1/users?email=test%40example.com&password=test", url.toString());
    }

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

    // ==================== 错误信息测试 ====================

    /**
     * 测试：访问未定义变量的属性应报错并显示位置
     *
     * 表达式: http://host/{undefinedVar.name}
     * 期望: 错误指向 {undefinedVar.name} 位置
     */
    @Test
    public void testError_UndefinedVariable_PropertyAccess_ShowsPosition() {
        Context ctx = new Context();
        // undefinedVar 未定义，访问其属性会抛出空指针错误

        EtlURL url = Eval.evalAsURL("http://host/{undefinedVar.name}", ctx);

        // 访问属性触发延迟计算
        TemplateRuntimeException ex = assertThrows(
            TemplateRuntimeException.class,
            () -> url.getHost()
        );

        ErrorPointerAssert.assertErrorPointer(ex,
            "http://host/{undefinedVar.name}",
            "                          ^^^^",
            "Cannot access member 'name' of null"
        );
    }

    /**
     * 测试：路径中访问未定义变量的属性应报错并显示位置
     *
     * 表达式: http://host/{basePath.name}/users
     * 期望: 错误指向 {basePath.name} 位置
     */
    @Test
    public void testError_UndefinedVariable_InPath_ShowsPosition() {
        Context ctx = new Context();

        EtlURL url = Eval.evalAsURL("http://host/{basePath.name}/users", ctx);

        TemplateRuntimeException ex = assertThrows(
            TemplateRuntimeException.class,
            () -> url.getPath()
        );

        ErrorPointerAssert.assertErrorPointer(ex,
            "http://host/{basePath.name}/users",
            "                      ^^^^",
            "Cannot access member 'name' of null"
        );
    }

    /**
     * 测试：多变量表达式中访问未定义变量属性应报错
     *
     * 表达式: {scheme.name}://{host.name}:{port.name}
     * 期望: 错误指向第一个未定义变量属性访问位置
     */
    @Test
    public void testError_UndefinedVariable_MultipleVariables_ShowsFirstError() {
        Context ctx = new Context();

        EtlURL url = Eval.evalAsURL("{scheme.name}://{host.name}:{port.name}", ctx);
        
        TemplateRuntimeException ex = assertThrows(
            TemplateRuntimeException.class,
            () -> url.getScheme()
        );

        assertTrue(ex.getStartLine() >= 1);
        assertTrue(ex.getMessage().contains("null") || ex.getMessage().contains("Cannot access"),
            "错误消息应提及 null 或访问错误: " + ex.getMessage());
    }

    // ==================== 空值访问错误测试 ====================

    /**
     * 测试：访问 null 对象属性应报错并显示位置
     *
     * 表达式: http://{user.name}/path
     * Context: user=null
     * 期望: 错误指向 {user.name} 整体
     */
    @Test
    public void testError_NullAccess_PropertyAccess_ShowsPosition() {
        Context ctx = new Context();
        ctx.put("user", null);

        EtlURL url = Eval.evalAsURL("http://{user.name}/path", ctx);

        TemplateRuntimeException ex = assertThrows(
            TemplateRuntimeException.class,
            () -> url.getHost()
        );

        ErrorPointerAssert.assertErrorPointer(ex,
            "http://{user.name}/path",
            "             ^^^^",
            "Cannot access member 'name' of null"
        );
    }

    /**
     * 测试：链式访问中中间对象为 null 应报错
     *
     * 表达式: http://{user.profile.city}/path
     * Context: user.profile=null
     * 期望: 错误指向失败位置
     */
    @Test
    public void testError_NullAccess_ChainedAccess_ShowsPosition() {
        Context ctx = new Context();
        ctx.put("user", new TestUserWithNullProfile());

        EtlURL url = Eval.evalAsURL("http://{user.profile.city}/path", ctx);

        TemplateRuntimeException ex = assertThrows(
            TemplateRuntimeException.class,
            () -> url.getHost()
        );

        ErrorPointerAssert.assertErrorPointer(ex,
            "http://{user.profile.city}/path",
            "                     ^^^^",
            "Cannot access member 'city' of null"
        );
    }

    /**
     * 测试：在 null 对象上调用方法应报错并显示位置
     *
     * 表达式: http://{obj.toString()}/path
     * Context: obj=null
     * 期望: 错误指向 {obj.toString()} 整体
     */
    @Test
    public void testError_NullAccess_MethodCallOnNull_ShowsPosition() {
        Context ctx = new Context();
        ctx.put("obj", null);

        EtlURL url = Eval.evalAsURL("http://{obj.toString()}/path", ctx);

        TemplateRuntimeException ex = assertThrows(
            TemplateRuntimeException.class,
            () -> url.getHost()
        );

        ErrorPointerAssert.assertErrorPointer(ex,
            "http://{obj.toString()}/path",
            "            ^^^^^^^^",
            "Cannot call method 'toString' on null"
        );
    }

    // ==================== 索引访问错误测试 ====================

    /**
     * 测试：数组索引越界应报错并显示位置
     *
     * 表达式: http://host/{items[10]}
     * Context: items=["a", "b"]
     * 期望: 错误指向索引访问位置
     */
    @Test
    public void testError_IndexAccess_ArrayOutOfBounds_ShowsPosition() {
        Context ctx = new Context();
        ctx.put("items", new String[]{"a", "b"});

        EtlURL url = Eval.evalAsURL("http://host/{items[10]}", ctx);

        TemplateRuntimeException ex = assertThrows(
            TemplateRuntimeException.class,
            () -> url.getPath()
        );
        
        ErrorPointerAssert.assertErrorPointer(ex,
            "http://host/{items[10]}",
            "                   ^^",
            "Array index 10 out of bounds"
        );
    }

    /**
     * 测试：在 null 数组上访问索引应报错
     *
     * 表达式: http://host/{arr[0]}
     * Context: arr=null
     * 期望: 错误指向 'arr'（对象）
     */
    @Test
    public void testError_IndexAccess_NullArray_ShowsPosition() {
        Context ctx = new Context();
        ctx.put("arr", null);

        EtlURL url = Eval.evalAsURL("http://host/{arr[0]}", ctx);

        TemplateRuntimeException ex = assertThrows(
            TemplateRuntimeException.class,
            () -> url.getPath()
        );

        // Pointer should point to 'arr' (object), not the index expression
        ErrorPointerAssert.assertErrorPointer(ex,
            "http://host/{arr[0]}",
            "             ^^^",
            "Cannot access index of null"
        );
    }

    // ==================== 方法调用错误测试 ====================

    /**
     * 测试：调用不存在的方法应报错并显示位置
     *
     * 表达式: http://{obj.nonExistentMethod()}/path
     * Context: obj=new Object()
     * 期望: 错误提及方法不存在
     */
    @Test
    public void testError_MethodCall_MethodNotExist_ShowsPosition() {
        Context ctx = new Context();
        ctx.put("obj", new Object());

        EtlURL url = Eval.evalAsURL("http://{obj.nonExistentMethod()}/path", ctx);

        TemplateRuntimeException ex = assertThrows(
            TemplateRuntimeException.class,
            () -> url.getHost()
        );

        ErrorPointerAssert.assertErrorPointer(ex,
            "http://{obj.nonExistentMethod()}/path",
            "            ^^^^^^^^^^^^^^^^^",
            "Method 'nonExistentMethod' not found"
        );
    }

    /**
     * 测试：方法参数数量错误应报错
     *
     * 表达式: http://{Math.max(1)}/path
     * 期望: Math.max 需要2个参数，只提供1个
     */
    @Test
    public void testError_MethodCall_WrongArgumentCount_ShowsPosition() {
        Context ctx = new Context();

        EtlURL url = Eval.evalAsURL("http://{Math.max(1)}/path", ctx);
        
        TemplateRuntimeException ex = assertThrows(
            TemplateRuntimeException.class,
            () -> url.getHost()
        );

        // Debug: print actual error message
        System.out.println("=== Error Message ===");
        System.out.println(ex.getMessage());
        System.out.println("=== End ===");

        assertTrue(ex.getStartLine() >= 1);
        assertTrue(ex.getMessage().contains("max") || ex.getMessage().contains("argument"),
            "错误消息应提及方法名或参数错误: " + ex.getMessage());
    }

    // ==================== 运算符错误测试 ====================

    /**
     * 测试：对 null 值进行运算应报错并显示位置
     *
     * 表达式: http://host/{-count}
     * Context: count=null
     * 期望: 错误指向运算表达式位置
     */
    @Test
    public void testError_Operator_NullOperand_ShowsPosition() {
        Context ctx = new Context();
        ctx.put("count", null);

        EtlURL url = Eval.evalAsURL("http://host/{-count}", ctx);
        
        TemplateRuntimeException ex = assertThrows(
            TemplateRuntimeException.class,
            () -> url.getPath()
        );

        // 验证位置信息存在
        assertTrue(ex.getStartLine() >= 1,
            "应有行号: " + ex.getMessage());
        // 验证错误消息提及 null 或运算错误
        assertTrue(ex.getMessage().contains("null") || ex.getMessage().contains("Cannot") || ex.getMessage().contains("negate"),
            "错误消息应提及 null 或运算错误: " + ex.getMessage());
    }

    // ==================== 复合场景测试 ====================

    /**
     * 测试：多行 URL 表达式中的错误应显示正确的行号
     *
     * 表达式:
     *   http://host/path1
     *   /{undefinedVar.name}/path2
     * 期望: 错误应在第2行
     */
    @Test
    public void testError_MultiLine_ShowsCorrectLine() {
        Context ctx = new Context();
        String multiLineUrl = "http://host/path1\n/{undefinedVar.name}/path2";

        EtlURL url = Eval.evalAsURL(multiLineUrl, ctx);

        TemplateRuntimeException ex = assertThrows(
            TemplateRuntimeException.class,
            () -> url.getPath()
        );

        // 对于多行表达式，错误在第2行，expectedSourceLine 应为第2行内容
        ErrorPointerAssert.assertErrorPointer(ex,
            "/{undefinedVar.name}/path2",
            "               ^^^^",
            "Cannot access member 'name' of null"
        );
    }

    /**
     * 测试：错误消息应包含可视化指针（Rust 风格）
     *
     * 表达式: http://{user.name}/path
     * Context: user=null
     * 期望: 错误消息包含 ^ 指针，且源码不包含 url(...) 包装
     */
    @Test
    public void testError_VisualPointer_ShowsInMessage() {
        Context ctx = new Context();
        ctx.put("user", null);

        EtlURL url = Eval.evalAsURL("http://{user.name}/path", ctx);

        TemplateRuntimeException ex = assertThrows(
            TemplateRuntimeException.class,
            () -> url.getHost()
        );

        // 验证错误消息格式
        String message = ex.getMessage();

        // 应包含错误类型
        assertTrue(message.contains("Runtime error") || message.contains("Cannot"),
            "应包含错误类型: " + message);

        // 验证源码不包含 url(...) 包装
        if (ex.getSourceText() != null) {
            assertEquals("http://{user.name}/path", ex.getSourceText(),
                "源码应为原始内容，不包含 url(...) 包装");
        }

        // 如果有源码，应包含可视化指针
        if (ex.getSourceText() != null) {
            assertTrue(message.contains("^") || message.contains(" |"),
                "有源码时应包含可视化指针: " + message);
        }
    }

    // ==================== 测试辅助类 ====================

    private static class TestUserWithNullProfile {
        public TestProfile getProfile() {
            return null;
        }
    }

    private static class TestProfile {
        public String getCity() {
            return "NYC";
        }
    }
}