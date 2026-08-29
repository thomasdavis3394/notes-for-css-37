package org.dromara.easytl.runtime;

import org.dromara.easytl.ast.RangeLiteral;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * 区间对象
 * <p>
 * 表示一个数值区间，支持四种区间类型：
 * - OPEN: 全开区间 (a, b)
 * - CLOSED: 全闭区间 [a, b]
 * - LEFT_OPEN_RIGHT_CLOSED: 前开后闭 (a, b]
 * - LEFT_CLOSED_RIGHT_OPEN: 前闭后开 [a, b)
 * </p>
 *
 * @author EasyTL Team
 * @since 1.0.0
 */
public class Range implements Iterable<Integer> {

    private final Object start;
    private final Object end;
    private final RangeLiteral.RangeType type;

    /**
     * 构造区间对象
     *
     * @param start 起始值
     * @param end 结束值
     * @param type 区间类型
     */
    public Range(Object start, Object end, RangeLiteral.RangeType type) {
        this.start = start;
        this.end = end;
        this.type = type;
    }

    /**
     * 获取起始值
     *
     * @return 起始值
     */
    public Object getStart() {
        return start;
    }

    /**
     * 获取结束值
     *
     * @return 结束值
     */
    public Object getEnd() {
        return end;
    }

    /**
     * 获取区间类型
     *
     * @return 区间类型
     */
    public RangeLiteral.RangeType getType() {
        return type;
    }

    /**
     * 判断值是否在区间内
     *
     * @param value 要判断的值
     * @return 如果值在区间内返回true，否则返回false
     */
    public boolean contains(Object value) {
        if (value == null) {
            return false;
        }

        int compareStart = compareValues(value, start);
        int compareEnd = compareValues(value, end);

        switch (type) {
            case OPEN:
                return compareStart > 0 && compareEnd < 0;
            case CLOSED:
                return compareStart >= 0 && compareEnd <= 0;
            case LEFT_OPEN_RIGHT_CLOSED:
                return compareStart > 0 && compareEnd <= 0;
            case LEFT_CLOSED_RIGHT_OPEN:
                return compareStart >= 0 && compareEnd < 0;
            default:
                return false;
        }
    }

    /**
     * 转换为列表（用于循环迭代）
     *
     * @return 整数列表
     */
    public List<Integer> toList() {
        List<Integer> list = new ArrayList<>();

        int startInt = toInt(start);
        int endInt = toInt(end);

        // 根据区间类型调整起始和结束值
        switch (type) {
            case OPEN:
                startInt++;
                endInt--;
                break;
            case CLOSED:
                // 不需要调整
                break;
            case LEFT_OPEN_RIGHT_CLOSED:
                startInt++;
                break;
            case LEFT_CLOSED_RIGHT_OPEN:
                endInt--;
                break;
        }

        // 生成范围内的整数
        if (startInt <= endInt) {
            for (int i = startInt; i <= endInt; i++) {
                list.add(i);
            }
        }

        return list;
    }

    @Override
    public Iterator<Integer> iterator() {
        return new RangeIterator();
    }

    /**
     * 区间迭代器
     */
    private class RangeIterator implements Iterator<Integer> {
        private int current;
        private final int end;

        public RangeIterator() {
            int startInt = toInt(start);
            int endInt = toInt(Range.this.end);

            // 根据区间类型调整起始和结束值
            switch (type) {
                case OPEN:
                    this.current = startInt + 1;
                    this.end = endInt - 1;
                    break;
                case CLOSED:
                    this.current = startInt;
                    this.end = endInt;
                    break;
                case LEFT_OPEN_RIGHT_CLOSED:
                    this.current = startInt + 1;
                    this.end = endInt;
                    break;
                case LEFT_CLOSED_RIGHT_OPEN:
                    this.current = startInt;
                    this.end = endInt - 1;
                    break;
                default:
                    this.current = startInt;
                    this.end = endInt;
            }
        }

        @Override
        public boolean hasNext() {
            return current <= end;
        }

        @Override
        public Integer next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            return current++;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Cannot remove from range");
        }
    }

    /**
     * 比较两个值的大小
     *
     * @param a 值a
     * @param b 值b
     * @return 如果a < b返回负数，a == b返回0，a > b返回正数
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private int compareValues(Object a, Object b) {
        if (a instanceof Comparable && b instanceof Comparable) {
            try {
                return ((Comparable) a).compareTo(b);
            } catch (ClassCastException e) {
                // 类型不匹配，尝试转换为数字比较
                return Double.compare(toDouble(a), toDouble(b));
            }
        }
        return Double.compare(toDouble(a), toDouble(b));
    }

    /**
     * 将对象转换为整数
     *
     * @param value 对象
     * @return 整数值
     */
    private int toInt(Object value) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value instanceof String) {
            return Integer.parseInt((String) value);
        }
        throw new IllegalArgumentException("Cannot convert " + value + " to int");
    }

    /**
     * 将对象转换为浮点数
     *
     * @param value 对象
     * @return 浮点数值
     */
    private double toDouble(Object value) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        if (value instanceof String) {
            return Double.parseDouble((String) value);
        }
        throw new IllegalArgumentException("Cannot convert " + value + " to double");
    }

    @Override
    public String toString() {
        String startSymbol = (type == RangeLiteral.RangeType.OPEN || type == RangeLiteral.RangeType.LEFT_OPEN_RIGHT_CLOSED) ? "(" : "[";
        String endSymbol = (type == RangeLiteral.RangeType.OPEN || type == RangeLiteral.RangeType.LEFT_CLOSED_RIGHT_OPEN) ? ")" : "]";
        return startSymbol + start + ", " + end + endSymbol;
    }
}
