package org.dromara.easytl;

import org.dromara.easytl.exception.ArgumentIndexOutOfBoundsException;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 默认参数名 $ 集成测试
 *
 * <p>语义规则（统一规则）：$ 作为标识符求值时：</p>
 * <ul>
 *   <li>可见作用域链中存在名为 $ 的变量（context.put("$", v)、{% let $ = ... %}、Lambda 参数名 $）→ 使用变量值（覆盖）</li>
 *   <li>否则 → 等价 $0，取最近参数作用域的第一个实参（默认），严格语义与 $0 完全一致：
 *       参数缺失/越界/值为 null 时抛 ArgumentIndexOutOfBoundsException</li>
 * </ul>
 *
 * @author EasyTL Test Team
 */
class DollarDefaultParamTest {

    // ==================== AC1: 三种包装形式下 {$} 等价 {$0} ====================

    @Test
    void testDollarInBrace() {
        Template template = EasyTL.compile("{$}");
        Context context = new Context();
        context.setArguments("World");
        assertEquals("World", template.render(context));
    }

    @Test
    void testDollarInDollarBrace() {
        Template template = EasyTL.compile("${$}");
        Context context = new Context();
        context.setArguments("World");
        assertEquals("World", template.render(context));
    }

    @Test
    void testDollarInDoubleBrace() {
        Template template = EasyTL.compile("{{$}}");
        Context context = new Context();
        context.setArguments("World");
        assertEquals("World", template.render(context));
    }

    // ==================== AC2: 无参数时与 {$0} 一致抛异常 ====================

    @Test
    void testDollarNoArgumentsThrows() {
        Template template = EasyTL.compile("{$}");
        Context context = new Context();

        ArgumentIndexOutOfBoundsException ex = assertThrows(ArgumentIndexOutOfBoundsException.class, () -> {
            template.render(context);
        });
        assertTrue(ex.getMessage().contains("out of bounds"));
    }

    @Test
    void testDollarNullFirstArgumentThrows() {
        // 与 {$0} 严格语义一致：第一参数为 null 时抛异常
        Template template = EasyTL.compile("{$}");
        Context context = new Context();
        context.setArguments((Object) null);

        ArgumentIndexOutOfBoundsException ex = assertThrows(ArgumentIndexOutOfBoundsException.class, () -> {
            template.render(context);
        });
        assertTrue(ex.getMessage().contains("is null"));
    }

    @Test
    void testDollarExceptionContainsPosition() {
        // 异常应携带位置信息（与 {$0} 一致）
        Template template = EasyTL.compile("{$}");
        Context context = new Context();

        ArgumentIndexOutOfBoundsException ex = assertThrows(ArgumentIndexOutOfBoundsException.class, () -> {
            template.render(context);
        });
        assertNotNull(ex.getPosition());
        assertFalse(ex.getPosition().isUnknown());
    }

    // ==================== AC3: ?? 运算符行为与 {$0 ?? 'default'} 一致 ====================

    @Test
    void testDollarElvisWithNullFirstArgument() {
        Template template = EasyTL.compile("{$ ?? 'default'}");
        Context context = new Context();
        context.setArguments((Object) null);
        assertEquals("default", template.render(context));
    }

    @Test
    void testDollarElvisWithValue() {
        Template template = EasyTL.compile("{$ ?? 'default'}");
        Context context = new Context();
        context.setArguments("actual");
        assertEquals("actual", template.render(context));
    }

    @Test
    void testDollarElvisNoArgumentsConsistentWithDollarZero() {
        // 无参数时 {$ ?? 'default'} 行为与 {$0 ?? 'default'} 完全一致
        Template dollarTemplate = EasyTL.compile("{$ ?? 'default'}");
        Template dollarZeroTemplate = EasyTL.compile("{$0 ?? 'default'}");
        assertEquals(dollarZeroTemplate.render(new Context()), dollarTemplate.render(new Context()));
    }

    // ==================== AC4: context.put("$", v) 覆盖优先于参数回退 ====================

    @Test
    void testDollarOverriddenByContextVariable() {
        Template template = EasyTL.compile("{$}");
        Context context = new Context();
        context.setArguments("Y");
        context.put("$", "X");
        assertEquals("X", template.render(context));
    }

    // ==================== AC5: {% let $ = ... %} 模板内覆盖生效 ====================

    @Test
    void testDollarOverriddenByLetStatement() {
        Template template = EasyTL.compile("{% let $ = 'X' %}{$}");
        Context context = new Context();
        context.setArguments("Y");
        assertEquals("X", template.render(context));
    }

    // ==================== AC6: Lambda 内 $ 默认引用第一个入参 ====================

    @Test
    void testDollarInLambdaDefaultsToFirstArgument() {
        Template template = EasyTL.compile("{% let f = (a, b) -> $ %}{f('first', 'second')}");
        Context context = new Context();
        assertEquals("first", template.render(context));
    }

    // ==================== AC7: Lambda 显式参数名 $ 覆盖绑定 ====================

    @Test
    void testDollarAsLambdaParameterName() {
        Template template = EasyTL.compile("{% let f = $ -> $ * 2 %}{f(21)}");
        Context context = new Context();
        assertEquals("42", template.render(context));
    }

    // ==================== AC8: 成员访问与 {$0.name} 一致 ====================

    @Test
    void testDollarMemberAccess() {
        Template template = EasyTL.compile("{$.name}");
        Context context = new Context();
        Map<String, Object> user = new HashMap<>();
        user.put("name", "Alice");
        context.setArguments(user);
        assertEquals("Alice", template.render(context));
    }

    @Test
    void testDollarMemberAccessConsistentWithDollarZero() {
        Map<String, Object> user = new HashMap<>();
        user.put("name", "Alice");

        Template dollarTemplate = EasyTL.compile("{$.name}");
        Template dollarZeroTemplate = EasyTL.compile("{$0.name}");

        Context dollarContext = new Context();
        dollarContext.setArguments(user);
        Context dollarZeroContext = new Context();
        dollarZeroContext.setArguments(user);

        assertEquals(dollarZeroTemplate.render(dollarZeroContext), dollarTemplate.render(dollarContext));
    }
}
