package org.dromara.easytl;

import org.dromara.easytl.exception.TemplateCompileException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 正则表达式标志位测试
 *
 * 覆盖 README 3.14：/pattern/flags 支持 g（全局）、i（忽略大小写）、m（多行），
 * 以及 =~ / !=~ 运算符、组合标志位、无标志位回归与非法标志位报错。
 */
public class RegexFlagsTest {

    private String render(String template) {
        return EasyTL.compile(template).render(new Context());
    }

    @Test
    public void testIgnoreCaseFlag() {
        assertEquals("true", render("{'admin' =~ /^admin$/i}"));
        assertEquals("true", render("{'ADMIN' =~ /^admin$/i}"));
        assertEquals("false", render("{'ADMIN' =~ /^admin$/}"));
    }

    @Test
    public void testIgnoreCaseFlagWithNotMatch() {
        assertEquals("false", render("{'ADMIN' !=~ /^admin$/i}"));
        assertEquals("true", render("{'ADMIN' !=~ /^root$/i}"));
    }

    @Test
    public void testMultilineFlag() {
        // 多行模式下 ^ $ 匹配行边界
        assertEquals("true", render("{'a\nb' =~ /^b$/m}"));
        assertEquals("false", render("{'a\nb' =~ /^b$/}"));
    }

    @Test
    public void testGlobalFlag() {
        // g 标志可正常解析，匹配语义与现有 =~ 的 find 语义一致
        assertEquals("true", render("{'abc123' =~ /\\d+/g}"));
        assertEquals("false", render("{'abc' =~ /\\d+/g}"));
    }

    @Test
    public void testCombinedFlags() {
        // gi 组合：忽略大小写
        assertEquals("true", render("{'ADMIN' =~ /^admin$/gi}"));
        // im 组合：忽略大小写 + 多行行边界
        assertEquals("true", render("{'a\nB' =~ /^b$/im}"));
    }

    @Test
    public void testNoFlagsRegression() {
        assertEquals("true", render("{'abc123' =~ /\\d+/}"));
        assertEquals("true", render("{'abc' !=~ /^\\d+$/}"));
        assertEquals("true", render("{'admin' =~ /^admin$/}"));
    }

    @Test
    public void testPatternEndingWithColonLettersNoFlags() {
        // 模式自身以 :i 结尾且无标志位时，不应被误拆分为标志位
        assertEquals("true", render("{'a:i' =~ /^a:i$/}"));
        assertEquals("false", render("{'A:I' =~ /^a:i$/}"));
    }

    @Test
    public void testInvalidFlag() {
        TemplateCompileException ex = assertThrows(TemplateCompileException.class,
            () -> EasyTL.compile("{'abc' =~ /abc/x}"));
        assertTrue(ex.getMessage().contains("Invalid regex flag"),
            "错误信息应指明非法标志位，实际：" + ex.getMessage());
    }

    @Test
    public void testInvalidFlagAfterValidFlag() {
        assertThrows(TemplateCompileException.class,
            () -> EasyTL.compile("{'abc' =~ /abc/gx}"));
    }
}
