package org.dromara.easytl.runtime;

import org.dromara.easytl.ast.RangeLiteral;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Range 测试类
 * <p>
 * 测试区间对象的创建、迭代、包含判断等功能
 * </p>
 *
 * @author EasyTL Team
 * @since 1.0.0
 */
class RangeTest {

    // ==================== 整数区间创建测试 ====================

    @Test
    void testCreate_ClosedRange() {
        // Given: 起始值和结束值
        int start = 1;
        int end = 5;
        RangeLiteral.RangeType type = RangeLiteral.RangeType.CLOSED;

        // When: 创建全闭区间
        Range range = new Range(start, end, type);

        // Then: 应该正确创建区间对象
        assertNotNull(range);
        assertEquals(start, range.getStart());
        assertEquals(end, range.getEnd());
        assertEquals(type, range.getType());
    }

    @Test
    void testCreate_OpenRange() {
        // Given: 起始值和结束值
        int start = 1;
        int end = 5;
        RangeLiteral.RangeType type = RangeLiteral.RangeType.OPEN;

        // When: 创建全开区间
        Range range = new Range(start, end, type);

        // Then: 应该正确创建区间对象
        assertNotNull(range);
        assertEquals(start, range.getStart());
        assertEquals(end, range.getEnd());
        assertEquals(type, range.getType());
    }

    @Test
    void testCreate_LeftClosedRightOpenRange() {
        // Given: 起始值和结束值
        int start = 1;
        int end = 5;
        RangeLiteral.RangeType type = RangeLiteral.RangeType.LEFT_CLOSED_RIGHT_OPEN;

        // When: 创建前闭后开区间
        Range range = new Range(start, end, type);

        // Then: 应该正确创建区间对象
        assertNotNull(range);
        assertEquals(start, range.getStart());
        assertEquals(end, range.getEnd());
        assertEquals(type, range.getType());
    }

    @Test
    void testCreate_LeftOpenRightClosedRange() {
        // Given: 起始值和结束值
        int start = 1;
        int end = 5;
        RangeLiteral.RangeType type = RangeLiteral.RangeType.LEFT_OPEN_RIGHT_CLOSED;

        // When: 创建前开后闭区间
        Range range = new Range(start, end, type);

        // Then: 应该正确创建区间对象
        assertNotNull(range);
        assertEquals(start, range.getStart());
        assertEquals(end, range.getEnd());
        assertEquals(type, range.getType());
    }

    @Test
    void testCreate_SingleValueRange() {
        // Given: 起始值和结束值相同
        int start = 5;
        int end = 5;
        RangeLiteral.RangeType type = RangeLiteral.RangeType.CLOSED;

        // When: 创建单值区间
        Range range = new Range(start, end, type);

        // Then: 应该正确创建区间对象
        assertNotNull(range);
        assertEquals(start, range.getStart());
        assertEquals(end, range.getEnd());
    }

    @Test
    void testCreate_EmptyRange() {
        // Given: 起始值大于结束值
        int start = 5;
        int end = 1;
        RangeLiteral.RangeType type = RangeLiteral.RangeType.CLOSED;

        // When: 创建空区间
        Range range = new Range(start, end, type);

        // Then: 应该正确创建区间对象（虽然为空）
        assertNotNull(range);
        assertEquals(start, range.getStart());
        assertEquals(end, range.getEnd());
    }

    @Test
    void testCreate_IntegerRange_WithStringValues() {
        // Given: 使用字符串表示的整数创建区间
        String start = "1";
        String end = "5";
        RangeLiteral.RangeType type = RangeLiteral.RangeType.CLOSED;

        // When: 创建区间
        Range range = new Range(start, end, type);

        // Then: 应该正确创建区间对象
        assertNotNull(range);
        assertEquals(start, range.getStart());
        assertEquals(end, range.getEnd());
        assertEquals(type, range.getType());
    }

    @Test
    void testCreate_IntegerRange_WithNumberObjects() {
        // Given: 使用 Number 对象创建区间
        Integer start = Integer.valueOf(1);
        Integer end = Integer.valueOf(5);
        RangeLiteral.RangeType type = RangeLiteral.RangeType.CLOSED;

        // When: 创建区间
        Range range = new Range(start, end, type);

        // Then: 应该正确创建区间对象
        assertNotNull(range);
        assertEquals(start, range.getStart());
        assertEquals(end, range.getEnd());
        assertEquals(type, range.getType());
    }

    // ==================== 整数区间包含判断测试 ====================

    @Test
    void testContains_ClosedRange_Inside() {
        // Given: 全闭区间 [1, 5]
        Range range = new Range(1, 5, RangeLiteral.RangeType.CLOSED);

        // When: 判断值是否在区间内
        // Then: 边界值和中间值都应该在区间内
        assertTrue(range.contains(1));
        assertTrue(range.contains(3));
        assertTrue(range.contains(5));
    }

    @Test
    void testContains_ClosedRange_Outside() {
        // Given: 全闭区间 [1, 5]
        Range range = new Range(1, 5, RangeLiteral.RangeType.CLOSED);

        // When: 判断值是否在区间内
        // Then: 边界外的值不应该在区间内
        assertFalse(range.contains(0));
        assertFalse(range.contains(6));
    }

    @Test
    void testContains_OpenRange_Inside() {
        // Given: 全开区间 (1, 5)
        Range range = new Range(1, 5, RangeLiteral.RangeType.OPEN);

        // When: 判断值是否在区间内
        // Then: 中间值应该在区间内，边界值不应该在区间内
        assertFalse(range.contains(1));
        assertTrue(range.contains(3));
        assertFalse(range.contains(5));
    }

    @Test
    void testContains_OpenRange_Outside() {
        // Given: 全开区间 (1, 5)
        Range range = new Range(1, 5, RangeLiteral.RangeType.OPEN);

        // When: 判断值是否在区间内
        // Then: 边界外的值不应该在区间内
        assertFalse(range.contains(0));
        assertFalse(range.contains(6));
    }

    @Test
    void testContains_LeftClosedRightOpenRange() {
        // Given: 前闭后开区间 [1, 5)
        Range range = new Range(1, 5, RangeLiteral.RangeType.LEFT_CLOSED_RIGHT_OPEN);

        // When: 判断值是否在区间内
        // Then: 左边界值应该在，右边界值不应该在
        assertTrue(range.contains(1));
        assertTrue(range.contains(3));
        assertFalse(range.contains(5));
    }

    @Test
    void testContains_LeftOpenRightClosedRange() {
        // Given: 前开后闭区间 (1, 5]
        Range range = new Range(1, 5, RangeLiteral.RangeType.LEFT_OPEN_RIGHT_CLOSED);

        // When: 判断值是否在区间内
        // Then: 左边界值不应该在，右边界值应该在
        assertFalse(range.contains(1));
        assertTrue(range.contains(3));
        assertTrue(range.contains(5));
    }

    @Test
    void testContains_SingleValueRange() {
        // Given: 单值区间 [5, 5]
        Range range = new Range(5, 5, RangeLiteral.RangeType.CLOSED);

        // When: 判断值是否在区间内
        // Then: 只有该值应该在区间内
        assertTrue(range.contains(5));
        assertFalse(range.contains(4));
        assertFalse(range.contains(6));
    }

    @Test
    void testContains_EmptyRange() {
        // Given: 空区间 [5, 1]
        Range range = new Range(5, 1, RangeLiteral.RangeType.CLOSED);

        // When: 判断值是否在区间内
        // Then: 任何值都不应该在区间内
        assertFalse(range.contains(1));
        assertFalse(range.contains(3));
        assertFalse(range.contains(5));
    }

    @Test
    void testContains_NullValue() {
        // Given: 区间
        Range range = new Range(1, 5, RangeLiteral.RangeType.CLOSED);

        // When: 判断 null 是否在区间内
        // Then: null 不应该在区间内
        assertFalse(range.contains(null));
    }

    @Test
    void testContains_StringBasedRange() {
        // Given: 使用字符串创建的区间 [1, 5]
        Range range = new Range("1", "5", RangeLiteral.RangeType.CLOSED);

        // When: 判断值是否在区间内
        // Then: 应该正确判断（通过字符串转换）
        assertTrue(range.contains(1));
        assertTrue(range.contains(3));
        assertTrue(range.contains(5));
        assertFalse(range.contains(0));
        assertFalse(range.contains(6));
    }

    @Test
    void testContains_NumberObjectValues() {
        // Given: 区间 [1, 5]
        Range range = new Range(1, 5, RangeLiteral.RangeType.CLOSED);

        // When: 使用 Number 对象判断
        // Then: 应该正确判断
        assertTrue(range.contains(Integer.valueOf(1)));
        assertTrue(range.contains(Integer.valueOf(3)));
        assertTrue(range.contains(Integer.valueOf(5)));
        assertFalse(range.contains(Integer.valueOf(0)));
        assertFalse(range.contains(Integer.valueOf(6)));
    }

    // ==================== 整数区间迭代测试 ====================

    @Test
    void testIterator_ClosedRange() {
        // Given: 全闭区间 [1, 5]
        Range range = new Range(1, 5, RangeLiteral.RangeType.CLOSED);

        // When: 迭代区间
        List<Integer> values = new ArrayList<>();
        for (Integer value : range) {
            values.add(value);
        }

        // Then: 应该包含所有值 [1, 2, 3, 4, 5]
        assertEquals(5, values.size());
        assertEquals(1, values.get(0));
        assertEquals(2, values.get(1));
        assertEquals(3, values.get(2));
        assertEquals(4, values.get(3));
        assertEquals(5, values.get(4));
    }

    @Test
    void testIterator_OpenRange() {
        // Given: 全开区间 (1, 5)
        Range range = new Range(1, 5, RangeLiteral.RangeType.OPEN);

        // When: 迭代区间
        List<Integer> values = new ArrayList<>();
        for (Integer value : range) {
            values.add(value);
        }

        // Then: 应该包含所有值 [2, 3, 4]（不包含边界）
        assertEquals(3, values.size());
        assertEquals(2, values.get(0));
        assertEquals(3, values.get(1));
        assertEquals(4, values.get(2));
    }

    @Test
    void testIterator_LeftClosedRightOpenRange() {
        // Given: 前闭后开区间 [1, 5)
        Range range = new Range(1, 5, RangeLiteral.RangeType.LEFT_CLOSED_RIGHT_OPEN);

        // When: 迭代区间
        List<Integer> values = new ArrayList<>();
        for (Integer value : range) {
            values.add(value);
        }

        // Then: 应该包含所有值 [1, 2, 3, 4]（包含左边界，不包含右边界）
        assertEquals(4, values.size());
        assertEquals(1, values.get(0));
        assertEquals(2, values.get(1));
        assertEquals(3, values.get(2));
        assertEquals(4, values.get(3));
    }

    @Test
    void testIterator_LeftOpenRightClosedRange() {
        // Given: 前开后闭区间 (1, 5]
        Range range = new Range(1, 5, RangeLiteral.RangeType.LEFT_OPEN_RIGHT_CLOSED);

        // When: 迭代区间
        List<Integer> values = new ArrayList<>();
        for (Integer value : range) {
            values.add(value);
        }

        // Then: 应该包含所有值 [2, 3, 4, 5]（不包含左边界，包含右边界）
        assertEquals(4, values.size());
        assertEquals(2, values.get(0));
        assertEquals(3, values.get(1));
        assertEquals(4, values.get(2));
        assertEquals(5, values.get(3));
    }

    @Test
    void testIterator_SingleValueRange() {
        // Given: 单值区间 [5, 5]
        Range range = new Range(5, 5, RangeLiteral.RangeType.CLOSED);

        // When: 迭代区间
        List<Integer> values = new ArrayList<>();
        for (Integer value : range) {
            values.add(value);
        }

        // Then: 应该只包含一个值 [5]
        assertEquals(1, values.size());
        assertEquals(5, values.get(0));
    }

    @Test
    void testIterator_EmptyRange() {
        // Given: 空区间 [5, 1]
        Range range = new Range(5, 1, RangeLiteral.RangeType.CLOSED);

        // When: 迭代区间
        List<Integer> values = new ArrayList<>();
        for (Integer value : range) {
            values.add(value);
        }

        // Then: 应该为空
        assertTrue(values.isEmpty());
    }

    @Test
    void testIterator_HasNext() {
        // Given: 区间 [1, 3]
        Range range = new Range(1, 3, RangeLiteral.RangeType.CLOSED);

        // When: 使用迭代器
        Iterator<Integer> iterator = range.iterator();

        // Then: 应该正确判断是否有下一个元素
        assertTrue(iterator.hasNext());
        assertEquals(1, iterator.next());
        assertTrue(iterator.hasNext());
        assertEquals(2, iterator.next());
        assertTrue(iterator.hasNext());
        assertEquals(3, iterator.next());
        assertFalse(iterator.hasNext());
    }

    @Test
    void testIterator_NextBeyondRange() {
        // Given: 区间 [1, 2]
        Range range = new Range(1, 2, RangeLiteral.RangeType.CLOSED);

        // When: 迭代超出范围
        Iterator<Integer> iterator = range.iterator();
        iterator.next(); // 1
        iterator.next(); // 2

        // Then: 应该抛出异常
        assertThrows(java.util.NoSuchElementException.class, () -> {
            iterator.next();
        });
    }

    @Test
    void testIterator_Remove() {
        // Given: 区间
        Range range = new Range(1, 5, RangeLiteral.RangeType.CLOSED);

        // When: 尝试删除元素
        Iterator<Integer> iterator = range.iterator();

        // Then: 应该抛出异常（不支持删除）
        assertThrows(UnsupportedOperationException.class, () -> {
            iterator.remove();
        });
    }

    // ==================== 整数区间 toList 测试 ====================

    @Test
    void testToList_ClosedRange() {
        // Given: 全闭区间 [1, 5]
        Range range = new Range(1, 5, RangeLiteral.RangeType.CLOSED);

        // When: 转换为列表
        List<Integer> list = range.toList();

        // Then: 应该包含所有值 [1, 2, 3, 4, 5]
        assertEquals(5, list.size());
        assertEquals(1, list.get(0));
        assertEquals(5, list.get(4));
    }

    @Test
    void testToList_OpenRange() {
        // Given: 全开区间 (1, 5)
        Range range = new Range(1, 5, RangeLiteral.RangeType.OPEN);

        // When: 转换为列表
        List<Integer> list = range.toList();

        // Then: 应该包含所有值 [2, 3, 4]（不包含边界）
        assertEquals(3, list.size());
        assertEquals(2, list.get(0));
        assertEquals(4, list.get(2));
    }

    @Test
    void testToList_LeftClosedRightOpenRange() {
        // Given: 前闭后开区间 [1, 5)
        Range range = new Range(1, 5, RangeLiteral.RangeType.LEFT_CLOSED_RIGHT_OPEN);

        // When: 转换为列表
        List<Integer> list = range.toList();

        // Then: 应该包含所有值 [1, 2, 3, 4]（包含左边界，不包含右边界）
        assertEquals(4, list.size());
        assertEquals(1, list.get(0));
        assertEquals(4, list.get(3));
    }

    @Test
    void testToList_LeftOpenRightClosedRange() {
        // Given: 前开后闭区间 (1, 5]
        Range range = new Range(1, 5, RangeLiteral.RangeType.LEFT_OPEN_RIGHT_CLOSED);

        // When: 转换为列表
        List<Integer> list = range.toList();

        // Then: 应该包含所有值 [2, 3, 4, 5]（不包含左边界，包含右边界）
        assertEquals(4, list.size());
        assertEquals(2, list.get(0));
        assertEquals(5, list.get(3));
    }

    @Test
    void testToList_SingleValueRange() {
        // Given: 单值区间 [5, 5]
        Range range = new Range(5, 5, RangeLiteral.RangeType.CLOSED);

        // When: 转换为列表
        List<Integer> list = range.toList();

        // Then: 应该只包含一个值 [5]
        assertEquals(1, list.size());
        assertEquals(5, list.get(0));
    }

    @Test
    void testToList_EmptyRange() {
        // Given: 空区间 [5, 1]
        Range range = new Range(5, 1, RangeLiteral.RangeType.CLOSED);

        // When: 转换为列表
        List<Integer> list = range.toList();

        // Then: 应该为空
        assertTrue(list.isEmpty());
    }

    @Test
    void testToList_StringBasedRange() {
        // Given: 使用字符串创建的区间 [1, 5]
        Range range = new Range("1", "5", RangeLiteral.RangeType.CLOSED);

        // When: 转换为列表
        List<Integer> list = range.toList();

        // Then: 应该包含所有值 [1, 2, 3, 4, 5]
        assertEquals(5, list.size());
        assertEquals(1, list.get(0));
        assertEquals(5, list.get(4));
    }

    @Test
    void testIterator_StringBasedRange() {
        // Given: 使用字符串创建的区间 [1, 3]
        Range range = new Range("1", "3", RangeLiteral.RangeType.CLOSED);

        // When: 迭代区间
        List<Integer> values = new ArrayList<>();
        for (Integer value : range) {
            values.add(value);
        }

        // Then: 应该包含所有值 [1, 2, 3]
        assertEquals(3, values.size());
        assertEquals(1, values.get(0));
        assertEquals(2, values.get(1));
        assertEquals(3, values.get(2));
    }

    // ==================== 字符区间测试 ====================

    @Test
    void testCreate_CharacterRange_Closed() {
        // Given: 字符起始值和结束值（作为整数）
        int start = (int) 'a';
        int end = (int) 'e';
        RangeLiteral.RangeType type = RangeLiteral.RangeType.CLOSED;

        // When: 创建全闭字符区间
        Range range = new Range(start, end, type);

        // Then: 应该正确创建区间对象
        assertNotNull(range);
        assertEquals(start, range.getStart());
        assertEquals(end, range.getEnd());
        assertEquals(type, range.getType());
    }

    @Test
    void testCreate_CharacterRange_Open() {
        // Given: 字符起始值和结束值
        int start = (int) 'a';
        int end = (int) 'e';
        RangeLiteral.RangeType type = RangeLiteral.RangeType.OPEN;

        // When: 创建全开字符区间
        Range range = new Range(start, end, type);

        // Then: 应该正确创建区间对象
        assertNotNull(range);
        assertEquals(start, range.getStart());
        assertEquals(end, range.getEnd());
        assertEquals(type, range.getType());
    }

    @Test
    void testCreate_CharacterRange_LeftClosedRightOpen() {
        // Given: 字符起始值和结束值
        int start = (int) 'a';
        int end = (int) 'e';
        RangeLiteral.RangeType type = RangeLiteral.RangeType.LEFT_CLOSED_RIGHT_OPEN;

        // When: 创建前闭后开字符区间
        Range range = new Range(start, end, type);

        // Then: 应该正确创建区间对象
        assertNotNull(range);
        assertEquals(start, range.getStart());
        assertEquals(end, range.getEnd());
        assertEquals(type, range.getType());
    }

    @Test
    void testCreate_CharacterRange_LeftOpenRightClosed() {
        // Given: 字符起始值和结束值
        int start = (int) 'a';
        int end = (int) 'e';
        RangeLiteral.RangeType type = RangeLiteral.RangeType.LEFT_OPEN_RIGHT_CLOSED;

        // When: 创建前开后闭字符区间
        Range range = new Range(start, end, type);

        // Then: 应该正确创建区间对象
        assertNotNull(range);
        assertEquals(start, range.getStart());
        assertEquals(end, range.getEnd());
        assertEquals(type, range.getType());
    }

    @Test
    void testCreate_CharacterRange_SingleCharacter() {
        // Given: 单个字符区间
        int start = (int) 'a';
        int end = (int) 'a';
        RangeLiteral.RangeType type = RangeLiteral.RangeType.CLOSED;

        // When: 创建单字符区间
        Range range = new Range(start, end, type);

        // Then: 应该正确创建区间对象
        assertNotNull(range);
        assertEquals(start, range.getStart());
        assertEquals(end, range.getEnd());
    }

    @Test
    void testCreate_CharacterRange_Empty() {
        // Given: 起始字符大于结束字符
        int start = (int) 'e';
        int end = (int) 'a';
        RangeLiteral.RangeType type = RangeLiteral.RangeType.CLOSED;

        // When: 创建空字符区间
        Range range = new Range(start, end, type);

        // Then: 应该正确创建区间对象（虽然为空）
        assertNotNull(range);
        assertEquals(start, range.getStart());
        assertEquals(end, range.getEnd());
    }

    @Test
    void testContains_CharacterRange_Closed() {
        // Given: 全闭字符区间 [a, e]
        Range range = new Range((int) 'a', (int) 'e', RangeLiteral.RangeType.CLOSED);

        // When: 判断字符是否在区间内
        // Then: 边界值和中间值都应该在区间内
        assertTrue(range.contains((int) 'a'));
        assertTrue(range.contains((int) 'c'));
        assertTrue(range.contains((int) 'e'));
        assertFalse(range.contains((int) '`')); // 'a' 前一个字符
        assertFalse(range.contains((int) 'f')); // 'e' 后一个字符
    }

    @Test
    void testContains_CharacterRange_Open() {
        // Given: 全开字符区间 (a, e)
        Range range = new Range((int) 'a', (int) 'e', RangeLiteral.RangeType.OPEN);

        // When: 判断字符是否在区间内
        // Then: 中间值应该在区间内，边界值不应该在区间内
        assertFalse(range.contains((int) 'a'));
        assertTrue(range.contains((int) 'c'));
        assertFalse(range.contains((int) 'e'));
        assertFalse(range.contains((int) '`'));
        assertFalse(range.contains((int) 'f'));
    }

    @Test
    void testContains_CharacterRange_LeftClosedRightOpen() {
        // Given: 前闭后开字符区间 [a, e)
        Range range = new Range((int) 'a', (int) 'e', RangeLiteral.RangeType.LEFT_CLOSED_RIGHT_OPEN);

        // When: 判断字符是否在区间内
        // Then: 左边界值应该在，右边界值不应该在
        assertTrue(range.contains((int) 'a'));
        assertTrue(range.contains((int) 'c'));
        assertFalse(range.contains((int) 'e'));
        assertFalse(range.contains((int) '`'));
        assertFalse(range.contains((int) 'f'));
    }

    @Test
    void testContains_CharacterRange_LeftOpenRightClosed() {
        // Given: 前开后闭字符区间 (a, e]
        Range range = new Range((int) 'a', (int) 'e', RangeLiteral.RangeType.LEFT_OPEN_RIGHT_CLOSED);

        // When: 判断字符是否在区间内
        // Then: 左边界值不应该在，右边界值应该在
        assertFalse(range.contains((int) 'a'));
        assertTrue(range.contains((int) 'c'));
        assertTrue(range.contains((int) 'e'));
        assertFalse(range.contains((int) '`'));
        assertFalse(range.contains((int) 'f'));
    }

    @Test
    void testContains_CharacterRange_SingleCharacter() {
        // Given: 单字符区间 [a, a]
        Range range = new Range((int) 'a', (int) 'a', RangeLiteral.RangeType.CLOSED);

        // When: 判断字符是否在区间内
        // Then: 只有该字符应该在区间内
        assertTrue(range.contains((int) 'a'));
        assertFalse(range.contains((int) '`'));
        assertFalse(range.contains((int) 'b'));
    }

    @Test
    void testContains_CharacterRange_Empty() {
        // Given: 空字符区间 [e, a]
        Range range = new Range((int) 'e', (int) 'a', RangeLiteral.RangeType.CLOSED);

        // When: 判断字符是否在区间内
        // Then: 任何字符都不应该在区间内
        assertFalse(range.contains((int) 'a'));
        assertFalse(range.contains((int) 'c'));
        assertFalse(range.contains((int) 'e'));
    }

    @Test
    void testContains_CharacterRange_Null() {
        // Given: 字符区间
        Range range = new Range((int) 'a', (int) 'e', RangeLiteral.RangeType.CLOSED);

        // When: 判断 null 是否在区间内
        // Then: null 不应该在区间内
        assertFalse(range.contains(null));
    }

    @Test
    void testContains_CharacterRange_CharacterObjectThrowsException() {
        // Given: 字符区间 [a, e]
        Range range = new Range((int) 'a', (int) 'e', RangeLiteral.RangeType.CLOSED);

        // When/Then: Character 对象无法被转换为 double，应该抛出异常
        // Note: Range 类只支持 Number 和 String 类型，不支持 Character 对象
        assertThrows(IllegalArgumentException.class, () -> {
            range.contains('a');
        });
    }

    @Test
    void testIterator_CharacterRange_Closed() {
        // Given: 全闭字符区间 [a, e]
        Range range = new Range((int) 'a', (int) 'e', RangeLiteral.RangeType.CLOSED);

        // When: 迭代字符区间
        List<Integer> values = new ArrayList<>();
        for (Integer value : range) {
            values.add(value);
        }

        // Then: 应该包含所有字符的整数值 [97, 98, 99, 100, 101]
        assertEquals(5, values.size());
        assertEquals((int) 'a', values.get(0));
        assertEquals((int) 'b', values.get(1));
        assertEquals((int) 'c', values.get(2));
        assertEquals((int) 'd', values.get(3));
        assertEquals((int) 'e', values.get(4));
    }

    @Test
    void testIterator_CharacterRange_Open() {
        // Given: 全开字符区间 (a, e)
        Range range = new Range((int) 'a', (int) 'e', RangeLiteral.RangeType.OPEN);

        // When: 迭代字符区间
        List<Integer> values = new ArrayList<>();
        for (Integer value : range) {
            values.add(value);
        }

        // Then: 应该包含所有值 [98, 99, 100]（不包含边界）
        assertEquals(3, values.size());
        assertEquals((int) 'b', values.get(0));
        assertEquals((int) 'c', values.get(1));
        assertEquals((int) 'd', values.get(2));
    }

    @Test
    void testIterator_CharacterRange_LeftClosedRightOpen() {
        // Given: 前闭后开字符区间 [a, e)
        Range range = new Range((int) 'a', (int) 'e', RangeLiteral.RangeType.LEFT_CLOSED_RIGHT_OPEN);

        // When: 迭代字符区间
        List<Integer> values = new ArrayList<>();
        for (Integer value : range) {
            values.add(value);
        }

        // Then: 应该包含所有值 [97, 98, 99, 100]（包含左边界，不包含右边界）
        assertEquals(4, values.size());
        assertEquals((int) 'a', values.get(0));
        assertEquals((int) 'b', values.get(1));
        assertEquals((int) 'c', values.get(2));
        assertEquals((int) 'd', values.get(3));
    }

    @Test
    void testIterator_CharacterRange_LeftOpenRightClosed() {
        // Given: 前开后闭字符区间 (a, e]
        Range range = new Range((int) 'a', (int) 'e', RangeLiteral.RangeType.LEFT_OPEN_RIGHT_CLOSED);

        // When: 迭代字符区间
        List<Integer> values = new ArrayList<>();
        for (Integer value : range) {
            values.add(value);
        }

        // Then: 应该包含所有值 [98, 99, 100, 101]（不包含左边界，包含右边界）
        assertEquals(4, values.size());
        assertEquals((int) 'b', values.get(0));
        assertEquals((int) 'c', values.get(1));
        assertEquals((int) 'd', values.get(2));
        assertEquals((int) 'e', values.get(3));
    }

    @Test
    void testIterator_CharacterRange_SingleCharacter() {
        // Given: 单字符区间 [a, a]
        Range range = new Range((int) 'a', (int) 'a', RangeLiteral.RangeType.CLOSED);

        // When: 迭代字符区间
        List<Integer> values = new ArrayList<>();
        for (Integer value : range) {
            values.add(value);
        }

        // Then: 应该只包含一个值 [97]
        assertEquals(1, values.size());
        assertEquals((int) 'a', values.get(0));
    }

    @Test
    void testIterator_CharacterRange_Empty() {
        // Given: 空字符区间 [e, a]
        Range range = new Range((int) 'e', (int) 'a', RangeLiteral.RangeType.CLOSED);

        // When: 迭代字符区间
        List<Integer> values = new ArrayList<>();
        for (Integer value : range) {
            values.add(value);
        }

        // Then: 应该为空
        assertTrue(values.isEmpty());
    }

    @Test
    void testIterator_CharacterRange_HasNext() {
        // Given: 字符区间 [a, c]
        Range range = new Range((int) 'a', (int) 'c', RangeLiteral.RangeType.CLOSED);

        // When: 使用迭代器
        Iterator<Integer> iterator = range.iterator();

        // Then: 应该正确判断是否有下一个元素
        assertTrue(iterator.hasNext());
        assertEquals((int) 'a', iterator.next());
        assertTrue(iterator.hasNext());
        assertEquals((int) 'b', iterator.next());
        assertTrue(iterator.hasNext());
        assertEquals((int) 'c', iterator.next());
        assertFalse(iterator.hasNext());
    }

    @Test
    void testToList_CharacterRange_Closed() {
        // Given: 全闭字符区间 [a, e]
        Range range = new Range((int) 'a', (int) 'e', RangeLiteral.RangeType.CLOSED);

        // When: 转换为列表
        List<Integer> list = range.toList();

        // Then: 应该包含所有值 [97, 98, 99, 100, 101]
        assertEquals(5, list.size());
        assertEquals((int) 'a', list.get(0));
        assertEquals((int) 'e', list.get(4));
    }

    @Test
    void testToList_CharacterRange_Open() {
        // Given: 全开字符区间 (a, e)
        Range range = new Range((int) 'a', (int) 'e', RangeLiteral.RangeType.OPEN);

        // When: 转换为列表
        List<Integer> list = range.toList();

        // Then: 应该包含所有值 [98, 99, 100]（不包含边界）
        assertEquals(3, list.size());
        assertEquals((int) 'b', list.get(0));
        assertEquals((int) 'd', list.get(2));
    }

    @Test
    void testToList_CharacterRange_Empty() {
        // Given: 空字符区间 [e, a]
        Range range = new Range((int) 'e', (int) 'a', RangeLiteral.RangeType.CLOSED);

        // When: 转换为列表
        List<Integer> list = range.toList();

        // Then: 应该为空
        assertTrue(list.isEmpty());
    }

    @Test
    void testContains_CharacterRange_Uppercase() {
        // Given: 大写字符区间 [A, E]
        Range range = new Range((int) 'A', (int) 'E', RangeLiteral.RangeType.CLOSED);

        // When: 判断字符是否在区间内
        // Then: 应该正确判断
        assertTrue(range.contains((int) 'A'));
        assertTrue(range.contains((int) 'C'));
        assertTrue(range.contains((int) 'E'));
        assertFalse(range.contains((int) 'a')); // 小写字符
        assertFalse(range.contains((int) 'F'));
    }

    @Test
    void testContains_CharacterRange_Digits() {
        // Given: 数字字符区间 [0, 9]
        Range range = new Range((int) '0', (int) '9', RangeLiteral.RangeType.CLOSED);

        // When: 判断字符是否在区间内
        // Then: 应该正确判断
        assertTrue(range.contains((int) '0'));
        assertTrue(range.contains((int) '5'));
        assertTrue(range.contains((int) '9'));
        assertFalse(range.contains((int) '/'));
        assertFalse(range.contains((int) ':'));
    }

    @Test
    void testIterator_CharacterRange_WideRange() {
        // Given: 大范围字符区间 [A, Z]
        Range range = new Range((int) 'A', (int) 'Z', RangeLiteral.RangeType.CLOSED);

        // When: 迭代字符区间
        List<Integer> values = new ArrayList<>();
        for (Integer value : range) {
            values.add(value);
        }

        // Then: 应该包含所有 26 个大写字母
        assertEquals(26, values.size());
        assertEquals((int) 'A', values.get(0));
        assertEquals((int) 'Z', values.get(25));
    }

    @Test
    void testToString() {
        // Given: 区间
        Range closedRange = new Range(1, 5, RangeLiteral.RangeType.CLOSED);
        Range openRange = new Range(1, 5, RangeLiteral.RangeType.OPEN);
        Range leftClosedRightOpen = new Range(1, 5, RangeLiteral.RangeType.LEFT_CLOSED_RIGHT_OPEN);
        Range leftOpenRightClosed = new Range(1, 5, RangeLiteral.RangeType.LEFT_OPEN_RIGHT_CLOSED);

        // When: 转换为字符串
        // Then: 应该正确格式化
        assertEquals("[1, 5]", closedRange.toString());
        assertEquals("(1, 5)", openRange.toString());
        assertEquals("[1, 5)", leftClosedRightOpen.toString());
        assertEquals("(1, 5]", leftOpenRightClosed.toString());
    }
}
