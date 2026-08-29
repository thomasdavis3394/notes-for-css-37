package org.dromara.easytl;

import org.dromara.easytl.exception.ArgumentIndexOutOfBoundsException;
import org.dromara.easytl.exception.TemplateRuntimeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Context 测试类
 * 
 * @author gongjun
 * @since 1.0.0
 */
class ContextTest {

    private Context context;

    @BeforeEach
    void setUp() {
        context = new Context();
    }

    @Test
    void testPutAndGet() {
        Math.abs(-1);
        context.put("name", "张三");
        context.put("age", 25);

        assertEquals("张三", context.get("name"));
        assertEquals(25, context.get("age"));
    }

    @Test
    void testGetNonExistent() {
        assertNull(context.get("nonExistent"));
    }

    @Test
    void testContains() {
        context.put("name", "张三");

        assertTrue(context.contains("name"));
        assertFalse(context.contains("age"));
    }

    @Test
    void testRemove() {
        context.put("name", "张三");
        
        final Object removed = context.remove("name");
        
        assertEquals("张三", removed);
        assertFalse(context.contains("name"));
        assertNull(context.get("name"));
    }

    @Test
    void testClear() {
        context.put("name", "张三");
        context.put("age", 25);

        context.clear();

        assertFalse(context.contains("name"));
        assertFalse(context.contains("age"));
    }

    @Test
    void testGetVariables() {
        context.put("name", "张三");
        context.put("age", 25);

        final Map<String, Object> variables = context.getVariables();

        assertEquals(2, variables.size());
        assertEquals("张三", variables.get("name"));
        assertEquals(25, variables.get("age"));

        // 确保返回的是副本
        variables.put("city", "北京");
        assertFalse(context.contains("city"));
    }

    @Test
    void testNestedContext() {
        // 父级上下文
        context.put("name", "张三");
        context.put("age", 25);

        // 创建子级上下文
        final Context childContext = context.createChildContext();
        childContext.put("city", "北京");

        // 子级上下文可以访问父级变量
        assertEquals("张三", childContext.get("name"));
        assertEquals(25, childContext.get("age"));
        assertEquals("北京", childContext.get("city"));

        // 父级上下文不能访问子级变量
        assertNull(context.get("city"));
    }

    @Test
    void testChildContextOverride() {
        // 父级上下文
        context.put("name", "张三");

        // 创建子级上下文并覆盖变量
        final Context childContext = context.createChildContext();
        childContext.put("name", "李四");

        // 子级上下文中的变量覆盖父级
        assertEquals("李四", childContext.get("name"));

        // 父级上下文中的变量不受影响
        assertEquals("张三", context.get("name"));
    }

    @Test
    void testGetParent() {
        assertNull(context.getParent());

        final Context childContext = context.createChildContext();
        assertEquals(context, childContext.getParent());
    }

    @Test
    void testMultiLevelNesting() {
        // 第一级
        context.put("level", 1);

        // 第二级
        final Context level2 = context.createChildContext();
        level2.put("level", 2);

        // 第三级
        final Context level3 = level2.createChildContext();
        level3.put("level", 3);

        // 验证每一级都能获取到自己的变量
        assertEquals(1, context.get("level"));
        assertEquals(2, level2.get("level"));
        assertEquals(3, level3.get("level"));

        // 验证父级关系
        assertEquals(context, level2.getParent());
        assertEquals(level2, level3.getParent());
    }

    @Test
    void testSetAndGetParameters() {
        // 测试设置和获取参数
        context.setArguments("arg0", "arg1", 123, true);

        assertEquals("arg0", context.getArgument(0));
        assertEquals("arg1", context.getArgument(1));
        assertEquals(123, context.getArgument(2));
        assertEquals(true, context.getArgument(3));
    }

    @Test
    void testGetParameterOutOfBounds() {
        context.setArguments("arg0", "arg1");

        // 测试索引超出范围
        assertNull(context.getArgument(5));
        assertNull(context.getArgument(-1));
    }

    @Test
    void testGetParameterWithNoParameters() {
        // 未设置参数时应返回 null
        assertNull(context.getArgument(0));
    }

    @Test
    void testGetParametersArray() {
        // 测试获取参数数组
        final Object[] params = new Object[]{"arg0", 123, true};
        context.setArguments(params);

        final Object[] result = context.getArguments();
        assertNotNull(result);
        assertArrayEquals(params, result);
    }

    @Test
    void testGetParametersWithNoParameters() {
        // 未设置参数时应返回 null
        assertNull(context.getArguments());
    }

    @Test
    void testParametersInheritance() {
        // 父级上下文设置参数
        context.setArguments("parent0", "parent1", 100);

        // 子级上下文
        final Context childContext = context.createChildContext();

        // 子级上下文可以访问父级参数
        assertEquals("parent0", childContext.getArgument(0));
        assertEquals("parent1", childContext.getArgument(1));
        assertEquals(100, childContext.getArgument(2));

        // 子级上下文可以获取父级参数数组
        assertArrayEquals(new Object[]{"parent0", "parent1", 100}, childContext.getArguments());
    }

    @Test
    void testParametersOverride() {
        // 父级上下文设置参数
        context.setArguments("parent0", "parent1");

        // 子级上下文覆盖参数
        final Context childContext = context.createChildContext();
        childContext.setArguments("child0", "child1", "child2");

        // 子级使用自己的参数
        assertEquals("child0", childContext.getArgument(0));
        assertEquals("child1", childContext.getArgument(1));
        assertEquals("child2", childContext.getArgument(2));

        // 父级参数不受影响
        assertEquals("parent0", context.getArgument(0));
        assertEquals("parent1", context.getArgument(1));
    }

    @Test
    void testRemoveNonExistent() {
        // 移除不存在的变量应返回 null
        assertNull(context.remove("nonExistent"));
    }

    @Test
    void testContainsInNestedContext() {
        // 父级上下文
        context.put("parentVar", "value");

        // 子级上下文
        final Context childContext = context.createChildContext();
        childContext.put("childVar", "value");

        // 子级上下文可以检测到父级变量
        assertTrue(childContext.contains("parentVar"));
        assertTrue(childContext.contains("childVar"));

        // 父级上下文不能检测到子级变量
        assertTrue(context.contains("parentVar"));
        assertFalse(context.contains("childVar"));
    }

    @Test
    void testGetInNestedContextWithNull() {
        // 父级上下文
        context.put("name", null);

        // 子级上下文
        final Context childContext = context.createChildContext();

        // 应该能够获取到父级的 null 值
        assertNull(childContext.get("name"));
        assertTrue(childContext.contains("name"));
    }

    @Test
    void testMultiLevelParametersInheritance() {
        // 第一级设置参数
        context.setArguments("level1-0", "level1-1");

        // 第二级
        final Context level2 = context.createChildContext();

        // 第三级
        final Context level3 = level2.createChildContext();
        level3.setArguments("level3-0", "level3-1", "level3-2");

        // 第二级应该继承第一级的参数
        assertEquals("level1-0", level2.getArgument(0));
        assertEquals("level1-1", level2.getArgument(1));

        // 第三级使用自己的参数
        assertEquals("level3-0", level3.getArgument(0));
        assertEquals("level3-1", level3.getArgument(1));
        assertEquals("level3-2", level3.getArgument(2));

        // 第一级参数不受影响
        assertEquals("level1-0", context.getArgument(0));
        assertEquals("level1-1", context.getArgument(1));
        assertNull(context.getArgument(2));
    }

    @Test
    void testEmptyParametersArray() {
        // 测试空参数数组
        context.setArguments();

        assertNotNull(context.getArguments());
        assertEquals(0, context.getArguments().length);
        assertNull(context.getArgument(0));
    }

    // ==================== 严格模式参数访问测试 ====================

    @Test
    void testGetArgumentStrictOutOfBounds() {
        context.setArguments("a", "b");

        ArgumentIndexOutOfBoundsException ex = assertThrows(ArgumentIndexOutOfBoundsException.class, () -> {
            context.getArgumentStrict(5);
        });
        assertTrue(ex.getMessage().contains("out of bounds"));
    }

    @Test
    void testGetArgumentStrictNoArguments() {
        // arguments not set

        ArgumentIndexOutOfBoundsException ex = assertThrows(ArgumentIndexOutOfBoundsException.class, () -> {
            context.getArgumentStrict(0);
        });
        assertTrue(ex.getMessage().contains("out of bounds"));
    }

    @Test
    void testGetArgumentStrictSuccess() {
        context.setArguments("a", "b", "c");

        assertEquals("a", context.getArgumentStrict(0));
        assertEquals("b", context.getArgumentStrict(1));
        assertEquals("c", context.getArgumentStrict(2));
    }

    @Test
    void testGetArgumentStrictWithNull() {
        // {0} 语法允许 null 值
        context.setArguments("a", null, "c");

        assertEquals("a", context.getArgumentStrict(0));
        assertNull(context.getArgumentStrict(1));  // null 值是允许的
        assertEquals("c", context.getArgumentStrict(2));
    }

    @Test
    void testGetArgumentVariableStrictOutOfBounds() {
        context.setArguments("a");

        ArgumentIndexOutOfBoundsException ex = assertThrows(ArgumentIndexOutOfBoundsException.class, () -> {
            context.getArgumentVariableStrict(5);
        });
        assertTrue(ex.getMessage().contains("out of bounds"));
    }

    @Test
    void testGetArgumentVariableStrictNull() {
        context.setArguments("a", null);

        ArgumentIndexOutOfBoundsException ex = assertThrows(ArgumentIndexOutOfBoundsException.class, () -> {
            context.getArgumentVariableStrict(1);
        });
        assertTrue(ex.getMessage().contains("is null"));
    }

    @Test
    void testGetArgumentVariableStrictSuccess() {
        context.setArguments("a", "b", "c");

        assertEquals("a", context.getArgumentVariableStrict(0));
        assertEquals("b", context.getArgumentVariableStrict(1));
        assertEquals("c", context.getArgumentVariableStrict(2));
    }

    @Test
    void testGetArgumentVariableStrictNoArguments() {
        ArgumentIndexOutOfBoundsException ex = assertThrows(ArgumentIndexOutOfBoundsException.class, () -> {
            context.getArgumentVariableStrict(0);
        });
        assertTrue(ex.getMessage().contains("out of bounds"));
    }

    // ==================== importClass 测试 ====================

    @Test
    void testImportClassWithSimpleName() {
        context.importClass(Math.class);

        assertTrue(context.contains("Math"));
        assertEquals(Math.class, context.get("Math"));
    }

    @Test
    void testImportClassWithAlias() {
        context.importClass(ArrayList.class, "List");

        assertTrue(context.contains("List"));
        assertEquals(ArrayList.class, context.get("List"));
    }

    @Test
    void testImportClassOverwrite() {
        context.importClass(Math.class);
        context.importClass(java.lang.System.class, "Math");

        assertEquals(java.lang.System.class, context.get("Math"));
    }
}

