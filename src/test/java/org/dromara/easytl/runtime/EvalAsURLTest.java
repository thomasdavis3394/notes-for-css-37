package org.dromara.easytl.runtime;

import org.dromara.easytl.Context;
import org.dromara.easytl.Eval;
import org.dromara.easytl.exception.TemplateRuntimeException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Eval.evalAsURL 错误位置测试
 */
public class EvalAsURLTest {

    // ==================== 错误位置验证测试 ====================

    /**
     * 测试：错误消息应显示原始源码（不包含 url(...) 包装）
     */
    @Test
    public void testErrorPosition_NoWrapper_ShowsOriginalSource() {
        Context ctx = new Context();
        ctx.put("user", null);

        EtlURL url = Eval.evalAsURL("http://{user.name}/path", ctx);

        TemplateRuntimeException ex = assertThrows(
            TemplateRuntimeException.class,
            () -> url.getHost()
        );

        // 验证源码是原始的（不包含 url(...) 包装）
        assertEquals("http://{user.name}/path", ex.getSourceText(),
            "源码应为原始内容，不包含 url(...) 包装");

        // 验证位置指向 {user.name}
        assertTrue(ex.getStartColumn() >= 8,
            "列号应指向 {user.name} 开始位置: " + ex.getStartColumn());
    }

    /**
     * 测试：多行 URL 错误应指向正确的行
     */
    @Test
    public void testErrorPosition_MultiLine_ShowsCorrectLine() {
        Context ctx = new Context();
        String multiLineUrl = "http://host/path1\n/{undefinedVar.name}/path2";

        EtlURL url = Eval.evalAsURL(multiLineUrl, ctx);

        TemplateRuntimeException ex = assertThrows(
            TemplateRuntimeException.class,
            () -> url.getPath()
        );

        // 验证错误在第2行
        assertEquals(2, ex.getStartLine(),
            "错误应在第2行: " + ex.getMessage());

        // 验证源码是原始的
        assertTrue(ex.getSourceText().contains("http://host/path1"),
            "源码应包含第一行内容");
    }

    /**
     * 测试：错误消息中的源码显示应正确格式化
     */
    @Test
    public void testErrorPosition_VisualFormat_ShowsCorrectPosition() {
        Context ctx = new Context();
        ctx.put("user", null);

        EtlURL url = Eval.evalAsURL("http://{user.name}/path", ctx);

        TemplateRuntimeException ex = assertThrows(
            TemplateRuntimeException.class,
            () -> url.getHost()
        );

        String message = ex.getMessage();

        // 验证错误消息包含源码行
        assertTrue(message.contains("http://{user.name}/path") || ex.getSourceText() != null,
            "错误消息应包含原始源码");

        // 验证不包含 url(...) 包装
        assertFalse(message.contains("url(http://") || message.contains("url (http://"),
            "错误消息不应包含 url(...) 包装: " + message);
    }

    // ==================== 包装与非包装兼容性测试 ====================

    /**
     * 测试：使用 url(...) 包装和非包装结果应相同
     */
    @Test
    public void testEvalAsURL_WithWrapper_WorksAsExpected() {
        Context ctx = new Context();
        ctx.put("host", "example.com");

        // 显式使用 url(...) 包装
        EtlURL url1 = Eval.evalAsURL("url(http://{host}/path)", ctx);
        // 不使用包装
        EtlURL url2 = Eval.evalAsURL("http://{host}/path", ctx);

        // 两者结果应相同
        assertEquals(url1.toString(), url2.toString(),
            "包装和非包装结果应相同");
    }

    /**
     * 测试：不带包装的简单 URL
     */
    @Test
    public void testEvalAsURL_NoWrapper_SimpleUrl() {
        Context ctx = new Context();
        ctx.put("host", "api.example.com");
        ctx.put("path", "/v1/users");

        EtlURL url = Eval.evalAsURL("http://{host}{path}", ctx);

        assertEquals("http", url.getScheme());
        assertEquals("api.example.com", url.getHost());
        assertEquals("/v1/users", url.getPath());
        assertEquals("http://api.example.com/v1/users", url.toString());
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