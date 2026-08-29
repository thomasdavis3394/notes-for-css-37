package org.dromara.easytl;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 字符串模板字面量（反引号）测试
 *
 * 覆盖 README 3.4.4：反引号包裹的字符串模板内使用 {表达式} 插值，
 * 支持任意表达式（运算、方法调用、三元）、{% %} 语句块、多行模板，
 * 以及无嵌入表达式的回归场景。
 */
public class TemplateLiteralTest {

    private String render(String template, Context context) {
        return EasyTL.compile(template).render(context);
    }

    @Test
    public void testBasicInterpolation() {
        Context context = new Context();
        context.put("name", "World");
        assertEquals("Hello World!", render("{`Hello {name}!`}", context));
    }

    @Test
    public void testMultipleInterpolations() {
        Context context = new Context();
        context.put("name", "张三");
        context.put("city", "北京");
        assertEquals("您好，张三，欢迎来到 北京", render("{`您好，{name}，欢迎来到 {city}`}", context));
    }

    @Test
    public void testArithmeticInterpolation() {
        Context context = new Context();
        context.put("price", 5);
        context.put("quantity", 3);
        assertEquals("总价：15元", render("{`总价：{price * quantity}元`}", context));
    }

    @Test
    public void testMethodCallInterpolation() {
        Context context = new Context();
        context.put("name", "World");
        assertEquals("长度=5", render("{`长度={name.length()}`}", context));
    }

    @Test
    public void testTernaryInterpolation() {
        Context context = new Context();
        context.put("status", 1);
        assertEquals("订单状态：已完成", render("{`订单状态：{status == 1 ? '已完成' : '进行中'}`}", context));
    }

    @Test
    public void testStringWithBracesInInterpolation() {
        // 嵌入表达式内的字符串字面量包含花括号，不应干扰表达式边界的识别
        Context context = new Context();
        context.put("name", "World");
        assertEquals("say }hi", render("{`say {name == 'World' ? '}hi' : 'no'}`}", context));
    }

    @Test
    public void testInterpolationInLetStatement() {
        Context context = new Context();
        context.put("name", "World");
        assertEquals("Hi World", render("{% let m = `Hi {name}` %}{m}", context));
    }

    @Test
    public void testMultilineTemplate() {
        Context context = new Context();
        context.put("name", "World");
        String template = "{`第一行：{name}\n    第二行：缩进`}";
        assertEquals("第一行：World\n    第二行：缩进", render(template, context));
    }

    @Test
    public void testNoInterpolationRegression() {
        // 无嵌入表达式的反引号字符串行为不变
        assertEquals("Hello World", render("{`Hello World`}", new Context()));
    }

    @Test
    public void testEmptyTemplate() {
        assertEquals("", render("{``}", new Context()));
    }

    @Test
    public void testNullInterpolation() {
        // 未定义变量插值结果为空字符串（Evaluator.visitTemplateLiteral 对 null 输出 ""）
        assertEquals("Hello !", render("{`Hello {name}!`}", new Context()));
    }
}
