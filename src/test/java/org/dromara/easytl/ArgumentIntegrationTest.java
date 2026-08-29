package org.dromara.easytl;

import org.dromara.easytl.ast.ArgumentAccess;
import org.dromara.easytl.ast.ArgumentVariable;
import org.dromara.easytl.ast.Position;
import org.dromara.easytl.exception.ArgumentIndexOutOfBoundsException;
import org.dromara.easytl.exception.TemplateException;
import org.dromara.easytl.exception.TemplateRuntimeException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 参数访问集成测试
 *
 * @author EasyTL Test Team
 */
class ArgumentIntegrationTest {

    @Test
    void testArgumentAccessWithContext() {
        Context context = new Context();
        context.setArguments("arg0", "arg1", 123);

        // 验证参数访问
        assertEquals("arg0", context.getArgument(0));
        assertEquals("arg1", context.getArgument(1));
        assertEquals(123, context.getArgument(2));
    }

    @Test
    void testArgumentAccessNode() {
        ArgumentAccess arg0 = new ArgumentAccess(0);
        ArgumentAccess arg1 = new ArgumentAccess(1);
        ArgumentAccess arg2 = new ArgumentAccess(2);

        assertEquals(0, arg0.getIndex());
        assertEquals(1, arg1.getIndex());
        assertEquals(2, arg2.getIndex());
    }

    @Test
    void testArgumentAccessWithPosition() {
        Position pos = new Position(1, 5, 1, 8);
        ArgumentAccess arg = new ArgumentAccess(0, pos);

        assertEquals(0, arg.getIndex());
        assertEquals(pos, arg.getPosition());
    }

    @Test
    void testTemplateExceptionBasic() {
        TemplateException ex = new TemplateException("Test error");
        assertEquals("Test error", ex.getMessage());
    }

    @Test
    void testTemplateExceptionWithCause() {
        RuntimeException cause = new RuntimeException("Root cause");
        TemplateException ex = new TemplateException("Test error", cause);

        assertEquals("Test error", ex.getMessage());
        assertEquals(cause, ex.getCause());
    }

    @Test
    void testNestedContextWithArguments() {
        Context parent = new Context();
        parent.setArguments("parent0", "parent1");

        Context child = parent.createChildContext();

        // 子上下文应该继承父上下文的参数
        assertEquals("parent0", child.getArgument(0));
        assertEquals("parent1", child.getArgument(1));

        // 子上下文设置自己的参数
        child.setArguments("child0", "child1", "child2");

        // 子上下文使用自己的参数
        assertEquals("child0", child.getArgument(0));
        assertEquals("child1", child.getArgument(1));
        assertEquals("child2", child.getArgument(2));

        // 父上下文参数不变
        assertEquals("parent0", parent.getArgument(0));
        assertEquals("parent1", parent.getArgument(1));
    }

    @Test
    void testMultiLevelArgumentInheritance() {
        // 三层嵌套
        Context level1 = new Context();
        level1.setArguments("L1-0", "L1-1");

        Context level2 = level1.createChildContext();
        // level2 不设置参数，应该继承 level1

        Context level3 = level2.createChildContext();
        level3.setArguments("L3-0");

        // level2 应该继承 level1 的参数
        assertEquals("L1-0", level2.getArgument(0));
        assertEquals("L1-1", level2.getArgument(1));

        // level3 使用自己的参数
        assertEquals("L3-0", level3.getArgument(0));
        // level3 的参数数组只有一个元素，访问 index 1 会继承父级
        assertEquals("L1-1", level3.getArgument(1));
    }

    @Test
    void testEmptyArgumentsArray() {
        Context context = new Context();
        context.setArguments(); // 空参数数组

        assertNotNull(context.getArguments());
        assertEquals(0, context.getArguments().length);
        assertNull(context.getArgument(0));
        assertNull(context.getArgument(-1));
    }

    @Test
    void testContextClearDoesNotAffectArguments() {
        Context context = new Context();
        context.setArguments("arg0", "arg1");
        context.put("var", "value");

        context.clear(); // 清空变量

        // 参数不受影响
        assertEquals("arg0", context.getArgument(0));
        assertEquals("arg1", context.getArgument(1));

        // 变量被清空
        assertNull(context.get("var"));
    }

    // ==================== ArgumentVariable 测试 ====================

    @Test
    void testArgumentVariableNode() {
        ArgumentVariable arg0 = new ArgumentVariable(0);
        ArgumentVariable arg1 = new ArgumentVariable(1);

        assertEquals(0, arg0.getIndex());
        assertEquals(1, arg1.getIndex());
    }

    @Test
    void testArgumentVariableWithPosition() {
        Position pos = new Position(1, 5, 1, 8);
        ArgumentVariable arg = new ArgumentVariable(0, pos);

        assertEquals(0, arg.getIndex());
        assertEquals(pos, arg.getPosition());
    }

    // ==================== {$0} 语法渲染测试 ====================

    @Test
    void testArgumentVariableRender() {
        Template template = EasyTL.compile("{$0}");
        Context context = new Context();
        context.setArguments("hello");
        assertEquals("hello", template.render(context));
    }

    @Test
    void testArgumentVariableWithMultipleArgs() {
        Template template = EasyTL.compile("{$0}-{$1}-{$2}");
        Context context = new Context();
        context.setArguments("a", "b", "c");
        assertEquals("a-b-c", template.render(context));
    }

    @Test
    void testArgumentVariableInDollarBrace() {
        Template template = EasyTL.compile("${$0}");
        Context context = new Context();
        context.setArguments("hello");
        assertEquals("hello", template.render(context));
    }

    @Test
    void testArgumentVariableInDoubleBrace() {
        Template template = EasyTL.compile("{{$0}}");
        Context context = new Context();
        context.setArguments("hello");
        assertEquals("hello", template.render(context));
    }

    // ==================== 错误处理测试 ====================

    @Test
    void testArgumentAccessOutOfBounds() {
        // Access argument index out of bounds
        Template template = EasyTL.compile("{1}");
        Context context = new Context();
        context.setArguments("only_one");

        ArgumentIndexOutOfBoundsException ex = assertThrows(ArgumentIndexOutOfBoundsException.class, () -> {
            template.render(context);
        });
        assertTrue(ex.getMessage().contains("out of bounds"));
    }

    @Test
    void testArgumentAccessNoArguments() {
        Template template = EasyTL.compile("{0}");
        Context context = new Context();

        ArgumentIndexOutOfBoundsException ex = assertThrows(ArgumentIndexOutOfBoundsException.class, () -> {
            template.render(context);
        });
        assertTrue(ex.getMessage().contains("out of bounds"));
    }

    @Test
    void testArgumentVariableOutOfBounds() {
        Template template = EasyTL.compile("{$15}");
        Context context = new Context();
        context.setArguments("only_one");

        ArgumentIndexOutOfBoundsException ex = assertThrows(ArgumentIndexOutOfBoundsException.class, () -> {
            template.render(context);
        });
        assertTrue(ex.getMessage().contains("out of bounds"));
    }

    @Test
    void testArgumentVariableNull() {
        Template template = EasyTL.compile("{$0}");
        Context context = new Context();
        context.setArguments((Object) null);

        ArgumentIndexOutOfBoundsException ex = assertThrows(ArgumentIndexOutOfBoundsException.class, () -> {
            template.render(context);
        });
        assertTrue(ex.getMessage().contains("is null"));
    }

    // ==================== ?? 运算符测试 ====================

    @Test
    void testArgumentVariableWithElvisNotTriggered() {
        Template template = EasyTL.compile("{$0 ?? 'default'}");
        Context context = new Context();
        context.setArguments("actual");
        assertEquals("actual", template.render(context));
    }

    @Test
    void testArgumentVariableWithElvisTriggered() {
        Template template = EasyTL.compile("{$0 ?? 'default'}");
        Context context = new Context();
        context.setArguments((Object) null);
        assertEquals("default", template.render(context));
    }

    @Test
    void testArgumentVariableOutOfBoundsWithElvis() {
        // ?? cannot catch out-of-bounds error
        Template template = EasyTL.compile("{$5 ?? 'default'}");
        Context context = new Context();
        context.setArguments("only_one");

        ArgumentIndexOutOfBoundsException ex = assertThrows(ArgumentIndexOutOfBoundsException.class, () -> {
            template.render(context);
        });
        assertTrue(ex.getMessage().contains("out of bounds"));
    }

    // ==================== 混合语法测试 ====================

    @Test
    void testMixedArgumentAccessAndVariable() {
        Template template = EasyTL.compile("{0}-{$1}");
        Context context = new Context();
        context.setArguments("a", "b");
        assertEquals("a-b", template.render(context));
    }

    @Test
    void testArgumentAccessAllowsNull() {
        // {0} 语法允许 null 值（不像 {$0} 那样报错）
        // null 值会被渲染为空字符串
        Template template = EasyTL.compile("{0}");
        Context context = new Context();
        context.setArguments((Object) null);
        assertEquals("", template.render(context));
    }
}