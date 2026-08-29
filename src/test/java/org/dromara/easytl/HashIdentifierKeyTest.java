package org.dromara.easytl;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 哈希表字面量标识符键测试
 *
 * 覆盖 README 3.4.7：键是标识符时自动转换为字符串（不作为变量求值），
 * 支持点访问、下标访问、混合键，以及字符串键/数字键回归和 in 运算。
 */
public class HashIdentifierKeyTest {

    private String render(String template) {
        return EasyTL.compile(template).render(new Context());
    }

    @Test
    public void testIdentifierKeyDotAccess() {
        assertEquals("zs", render("{% let u = {name: 'zs', age: 25} %}{u.name}"));
        assertEquals("25", render("{% let u = {name: 'zs', age: 25} %}{u.age}"));
    }

    @Test
    public void testIdentifierKeyIndexAccess() {
        assertEquals("zs", render("{% let u = {name: 'zs', age: 25} %}{u['name']}"));
    }

    @Test
    public void testIdentifierKeysDoNotCollapse() {
        // 修复前多个标识符键都会被求值为 null，坍缩为同一个 null 键
        assertEquals("zs-25", render("{% let u = {name: 'zs', age: 25} %}{u.name}-{u.age}"));
    }

    @Test
    public void testMixedKeys() {
        String template = "{% let m = {name: 'a', \"b\": 1, 0: 'c'} %}{m.name}-{m['b']}-{m[0]}";
        assertEquals("a-1-c", render(template));
    }

    @Test
    public void testStringKeyRegression() {
        assertEquals("zs", render("{% let u = {'name': 'zs'} %}{u.name}"));
        assertEquals("zs", render("{% let u = {'name': 'zs'} %}{u['name']}"));
    }

    @Test
    public void testNumberKeyRegression() {
        assertEquals("待处理", render("{% let s = {0: '待处理', 1: '进行中'} %}{s[0]}"));
        assertEquals("进行中", render("{% let s = {0: '待处理', 1: '进行中'} %}{s[1]}"));
    }

    @Test
    public void testIdentifierKeyInOperator() {
        assertEquals("true", render("{% let u = {name: 'zs', age: 25} %}{'name' in u}"));
        assertEquals("false", render("{% let u = {name: 'zs', age: 25} %}{'email' in u}"));
    }

    @Test
    public void testIdentifierKeyInOperatorConfig() {
        assertEquals("true", render("{% let config = {debug: true, timeout: 3000} %}{'debug' in config}"));
        assertEquals("false", render("{% let config = {debug: true, timeout: 3000} %}{'verbose' in config}"));
    }

    @Test
    public void testNestedHashWithIdentifierKeys() {
        String template = "{% let nested = {user: {name: '张三', age: 25}} %}{nested.user.name}-{nested.user.age}";
        assertEquals("张三-25", render(template));
    }
}
