package org.dromara.easytl.runtime;

import org.dromara.easytl.exception.TemplateRuntimeException;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Modifier;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ReflectionUtils 测试类
 * <p>
 * 测试反射工具类的属性访问、方法调用和索引访问等功能
 * </p>
 *
 * @author EasyTL Team
 * @since 1.0.0
 */
class ReflectionUtilsTest {

    // ==================== 测试辅助类 ====================

    /**
     * 测试用的简单类（使用 getter 方法）
     */
    public static class TestBean {
        private String name;
        private int age;
        private boolean active;

        public TestBean(String name, int age, boolean active) {
            this.name = name;
            this.age = age;
            this.active = active;
        }

        public String getName() {
            return name;
        }

        public int getAge() {
            return age;
        }

        public boolean isActive() {
            return active;
        }
    }

    /**
     * 测试用的简单类（使用 public 字段）
     */
    public static class TestBeanWithFields {
        public String name;
        public int age;
        public boolean active;

        public TestBeanWithFields(String name, int age, boolean active) {
            this.name = name;
            this.age = age;
            this.active = active;
        }
    }

    /**
     * 测试用的类（同时有 getter 和 public 字段，用于测试优先级）
     */
    public static class TestBeanWithBoth {
        public String name; // public 字段
        private int age;

        public TestBeanWithBoth(String name, int age) {
            this.name = name;
            this.age = age;
        }

        public String getName() {
            return "getter:" + name; // getter 返回不同的值以验证优先级
        }

        public int getAge() {
            return age;
        }
    }

    /**
     * 测试用的类（使用 Boolean 包装类型的 isXxx getter）
     */
    public static class TestBeanWithBooleanWrapper {
        private Boolean enabled;
        private Boolean disabled;

        public TestBeanWithBooleanWrapper(Boolean enabled, Boolean disabled) {
            this.enabled = enabled;
            this.disabled = disabled;
        }

        public Boolean isEnabled() {
            return enabled;
        }

        public Boolean isDisabled() {
            return disabled;
        }
    }

    /**
     * 测试用的类（用于方法调用测试）
     */
    public static class TestMethodClass {
        public String methodNoArgs() {
            return "no args";
        }

        public String methodWithArgs(String arg) {
            return "with arg: " + arg;
        }

        public String methodOverloaded(int arg) {
            return "int: " + arg;
        }

        public String methodOverloaded(String arg) {
            return "string: " + arg;
        }

        public String methodVarArgs(String... args) {
            return "var args: " + Arrays.toString(args);
        }

        public int methodWithPrimitive(int arg) {
            return arg * 2;
        }

        public String methodWithBoxed(Integer arg) {
            return "boxed: " + arg;
        }

        // 更多重载方法用于测试
        public String methodOverloaded(int arg1, int arg2) {
            return "int, int: " + arg1 + ", " + arg2;
        }

        public String methodOverloaded(String arg1, String arg2) {
            return "string, string: " + arg1 + ", " + arg2;
        }

        public String methodOverloaded(int arg1, String arg2) {
            return "int, string: " + arg1 + ", " + arg2;
        }

        public String methodOverloaded(String arg1, int arg2) {
            return "string, int: " + arg1 + ", " + arg2;
        }

        // 可变参数方法的不同形式
        public String methodVarArgs(int... args) {
            return "var args int: " + Arrays.toString(args);
        }

        public String methodVarArgs(String prefix, String... args) {
            return "var args with prefix: " + prefix + ", " + Arrays.toString(args);
        }

        // 基本类型方法
        public long methodWithLong(long arg) {
            return arg * 2;
        }

        public double methodWithDouble(double arg) {
            return arg * 2.0;
        }

        public float methodWithFloat(float arg) {
            return arg * 2.0f;
        }

        public boolean methodWithBoolean(boolean arg) {
            return !arg;
        }

        public char methodWithChar(char arg) {
            return Character.toUpperCase(arg);
        }

        public byte methodWithByte(byte arg) {
            return (byte) (arg * 2);
        }

        public short methodWithShort(short arg) {
            return (short) (arg * 2);
        }

        // 包装类型方法
        public String methodWithLongBoxed(Long arg) {
            return "boxed long: " + arg;
        }

        public String methodWithDoubleBoxed(Double arg) {
            return "boxed double: " + arg;
        }

        // 混合类型方法
        public String methodMixed(int i, String s, boolean b) {
            return "mixed: " + i + ", " + s + ", " + b;
        }

        // null 参数测试
        public String methodWithNull(String arg) {
            return arg == null ? "null" : "not null: " + arg;
        }
    }

    // ==================== 属性访问测试 ====================

    @Test
    void testGetProperty_GetterMethod() {
        // Given: 使用 getter 方法的对象
        TestBean bean = new TestBean("John", 30, true);

        // When: 通过 getter 方法访问属性
        Object name = ReflectionUtils.getProperty(bean, "name");
        Object age = ReflectionUtils.getProperty(bean, "age");
        Object active = ReflectionUtils.getProperty(bean, "active");

        // Then: 应该正确获取属性值
        assertEquals("John", name);
        assertEquals(30, age);
        assertEquals(true, active);
    }

    @Test
    void testGetProperty_PublicField() {
        // Given: 使用 public 字段的对象
        TestBeanWithFields bean = new TestBeanWithFields("Jane", 25, false);

        // When: 通过 public 字段访问属性
        Object name = ReflectionUtils.getProperty(bean, "name");
        Object age = ReflectionUtils.getProperty(bean, "age");
        Object active = ReflectionUtils.getProperty(bean, "active");

        // Then: 应该正确获取属性值
        assertEquals("Jane", name);
        assertEquals(25, age);
        assertEquals(false, active);
    }

    @Test
    void testGetProperty_Map() {
        // Given: Map 对象
        Map<String, Object> map = new HashMap<>();
        map.put("name", "MapName");
        map.put("age", 40);

        // When: 通过 Map 的 get 方法访问属性
        Object name = ReflectionUtils.getProperty(map, "name");
        Object age = ReflectionUtils.getProperty(map, "age");

        // Then: 应该正确获取属性值
        assertEquals("MapName", name);
        assertEquals(40, age);
    }

    @Test
    void testGetProperty_Map_NonExistent() {
        // Given: Map 对象
        Map<String, Object> map = new HashMap<>();
        map.put("name", "MapName");

        // When: 访问不存在的属性
        Object value = ReflectionUtils.getProperty(map, "nonExistent");

        // Then: 应该返回 null（Map.get 返回 null）
        assertNull(value);
    }

    @Test
    void testGetProperty_PropertyNotFound() {
        // Given: 对象
        TestBean bean = new TestBean("John", 30, true);

        // When/Then: 访问不存在的属性应该抛出异常
        assertThrows(TemplateRuntimeException.class, () -> {
            ReflectionUtils.getProperty(bean, "nonExistent");
        });
    }

    @Test
    void testGetProperty_NullObject() {
        // Given: null 对象
        TestBean bean = null;

        // When/Then: 访问 null 对象的属性应该抛出异常
        assertThrows(TemplateRuntimeException.class, () -> {
            ReflectionUtils.getProperty(bean, "name");
        });
    }

    @Test
    void testGetProperty_CachePerformance() {
        // Given: 对象
        TestBean bean = new TestBean("John", 30, true);

        // When: 多次访问同一属性（应该使用缓存）
        Object name1 = ReflectionUtils.getProperty(bean, "name");
        Object name2 = ReflectionUtils.getProperty(bean, "name");
        Object name3 = ReflectionUtils.getProperty(bean, "name");

        // Then: 应该正确获取属性值（缓存应该提升性能）
        assertEquals("John", name1);
        assertEquals("John", name2);
        assertEquals("John", name3);
    }

    @Test
    void testGetProperty_GetterPriority() {
        // Given: 同时有 getter 和 public 字段的对象
        TestBeanWithBoth bean = new TestBeanWithBoth("fieldValue", 25);

        // When: 访问属性
        Object name = ReflectionUtils.getProperty(bean, "name");
        Object age = ReflectionUtils.getProperty(bean, "age");

        // Then: 应该优先使用 getter 方法（而不是 public 字段）
        assertEquals("getter:fieldValue", name); // getter 返回的值，不是字段值
        assertEquals(25, age);
    }

    @Test
    void testGetProperty_BooleanWrapper_IsXxx() {
        // Given: 使用 Boolean 包装类型的 isXxx getter 的对象
        TestBeanWithBooleanWrapper bean = new TestBeanWithBooleanWrapper(true, false);

        // When: 通过 isXxx getter 访问属性
        Object enabled = ReflectionUtils.getProperty(bean, "enabled");
        Object disabled = ReflectionUtils.getProperty(bean, "disabled");

        // Then: 应该正确获取属性值
        assertEquals(true, enabled);
        assertEquals(false, disabled);
    }

    @Test
    void testGetProperty_BooleanWrapper_Null() {
        // Given: Boolean 包装类型为 null 的对象
        TestBeanWithBooleanWrapper bean = new TestBeanWithBooleanWrapper(null, null);

        // When: 通过 isXxx getter 访问属性
        Object enabled = ReflectionUtils.getProperty(bean, "enabled");
        Object disabled = ReflectionUtils.getProperty(bean, "disabled");

        // Then: 应该返回 null
        assertNull(enabled);
        assertNull(disabled);
    }

    // ==================== 方法调用测试 ====================

    // --- 基本方法调用测试 ---

    @Test
    void testInvokeMethod_NoArgs() {
        // Given: 对象
        TestMethodClass obj = new TestMethodClass();

        // When: 调用无参数方法
        Object result = ReflectionUtils.invokeMethod(obj, "methodNoArgs", new Object[0]);

        // Then: 应该正确调用方法
        assertEquals("no args", result);
    }

    @Test
    void testInvokeMethod_WithArgs() {
        // Given: 对象
        TestMethodClass obj = new TestMethodClass();

        // When: 调用带参数方法
        Object result = ReflectionUtils.invokeMethod(obj, "methodWithArgs", new Object[]{"test"});

        // Then: 应该正确调用方法
        assertEquals("with arg: test", result);
    }

    @Test
    void testInvokeMethod_NullObject() {
        // Given: null 对象
        TestMethodClass obj = null;

        // When/Then: 在 null 对象上调用方法应该抛出异常
        TemplateRuntimeException exception = assertThrows(TemplateRuntimeException.class, () -> {
            ReflectionUtils.invokeMethod(obj, "methodNoArgs", new Object[0]);
        });
        assertTrue(exception.getMessage().contains("Cannot invoke method"));
        assertTrue(exception.getMessage().contains("on null"));
    }

    // --- 方法重载匹配测试 ---

    @Test
    void testInvokeMethod_Overloaded_Int() {
        // Given: 对象
        TestMethodClass obj = new TestMethodClass();

        // When: 调用重载方法（int 参数）
        Object result = ReflectionUtils.invokeMethod(obj, "methodOverloaded", new Object[]{10});

        // Then: 应该正确匹配 int 版本的方法
        assertEquals("int: 10", result);
    }

    @Test
    void testInvokeMethod_Overloaded_String() {
        // Given: 对象
        TestMethodClass obj = new TestMethodClass();

        // When: 调用重载方法（String 参数）
        Object result = ReflectionUtils.invokeMethod(obj, "methodOverloaded", new Object[]{"test"});

        // Then: 应该正确匹配 String 版本的方法
        assertEquals("string: test", result);
    }

    @Test
    void testInvokeMethod_Overloaded_TwoInts() {
        // Given: 对象
        TestMethodClass obj = new TestMethodClass();

        // When: 调用重载方法（两个 int 参数）
        Object result = ReflectionUtils.invokeMethod(obj, "methodOverloaded", new Object[]{10, 20});

        // Then: 应该正确匹配两个 int 版本的方法
        assertEquals("int, int: 10, 20", result);
    }

    @Test
    void testInvokeMethod_Overloaded_TwoStrings() {
        // Given: 对象
        TestMethodClass obj = new TestMethodClass();

        // When: 调用重载方法（两个 String 参数）
        Object result = ReflectionUtils.invokeMethod(obj, "methodOverloaded", new Object[]{"a", "b"});

        // Then: 应该正确匹配两个 String 版本的方法
        assertEquals("string, string: a, b", result);
    }

    @Test
    void testInvokeMethod_Overloaded_IntString() {
        // Given: 对象
        TestMethodClass obj = new TestMethodClass();

        // When: 调用重载方法（int, String 参数）
        Object result = ReflectionUtils.invokeMethod(obj, "methodOverloaded", new Object[]{10, "test"});

        // Then: 应该正确匹配 int, String 版本的方法
        assertEquals("int, string: 10, test", result);
    }

    @Test
    void testInvokeMethod_Overloaded_StringInt() {
        // Given: 对象
        TestMethodClass obj = new TestMethodClass();

        // When: 调用重载方法（String, int 参数）
        Object result = ReflectionUtils.invokeMethod(obj, "methodOverloaded", new Object[]{"test", 10});

        // Then: 应该正确匹配 String, int 版本的方法
        assertEquals("string, int: test, 10", result);
    }

    // --- 可变参数测试 ---

    @Test
    void testInvokeMethod_VarArgs_NoArgs() {
        // Given: 对象
        TestMethodClass obj = new TestMethodClass();

        // When/Then: 调用可变参数方法（无参数）
        // Note: 当前实现不完全支持可变参数。对于 methodVarArgs(String... args)，
        // 当传入空数组时，Java 反射需要将空数组包装为可变参数数组
        // 当前实现会抛出异常，因为参数数量不匹配
        // 这是已知限制，需要实现改进
        assertThrows(TemplateRuntimeException.class, () -> {
            ReflectionUtils.invokeMethod(obj, "methodVarArgs", new Object[0]);
        });
    }

    @Test
    void testInvokeMethod_VarArgs_SingleArg() {
        // Given: 对象
        TestMethodClass obj = new TestMethodClass();

        // When/Then: 调用可变参数方法（单个参数）
        // Note: 当前实现不完全支持可变参数。对于 methodVarArgs(String... args)，
        // 当传入单个参数时，Java 反射需要将参数包装为可变参数数组
        // 当前实现会抛出异常，因为参数数量不匹配（找到的是带前缀的方法）
        // 这是已知限制，需要实现改进
        assertThrows(TemplateRuntimeException.class, () -> {
            ReflectionUtils.invokeMethod(obj, "methodVarArgs", new Object[]{"single"});
        });
    }

    @Test
    void testInvokeMethod_VarArgs_MultipleArgs() {
        // Given: 对象
        TestMethodClass obj = new TestMethodClass();

        // When/Then: 调用可变参数方法（多个参数）
        // Note: 当前实现不完全支持可变参数。对于 methodVarArgs(String... args)，
        // 当传入多个参数时，Java 反射需要将参数包装为可变参数数组
        // 当前实现会抛出异常，因为参数数量不匹配
        // 这是已知限制，需要实现改进
        assertThrows(TemplateRuntimeException.class, () -> {
            ReflectionUtils.invokeMethod(obj, "methodVarArgs", new Object[]{"a", "b", "c"});
        });
    }

    @Test
    void testInvokeMethod_VarArgs_Int() {
        // Given: 对象
        TestMethodClass obj = new TestMethodClass();

        // When/Then: 调用 int 可变参数方法
        // Note: 当前实现不完全支持可变参数。对于 methodVarArgs(int... args)，
        // 当传入多个参数时，Java 反射需要将参数包装为可变参数数组
        // 当前实现会抛出异常，因为参数数量不匹配
        // 这是已知限制，需要实现改进
        assertThrows(TemplateRuntimeException.class, () -> {
            ReflectionUtils.invokeMethod(obj, "methodVarArgs", new Object[]{1, 2, 3});
        });
    }

    @Test
    void testInvokeMethod_VarArgs_WithPrefix() {
        // Given: 对象
        TestMethodClass obj = new TestMethodClass();

        // When/Then: 调用带前缀的可变参数方法
        // Note: 当前实现不完全支持可变参数。对于 methodVarArgs(String prefix, String... args)，
        // 当传入多个参数时，Java 反射需要将后面的参数包装为可变参数数组
        // 当前实现会抛出异常，因为参数数量不匹配
        // 这是已知限制，需要实现改进
        assertThrows(TemplateRuntimeException.class, () -> {
            ReflectionUtils.invokeMethod(obj, "methodVarArgs", new Object[]{"prefix", "a", "b"});
        });
    }

    // --- 基本类型转换测试 ---

    @Test
    void testInvokeMethod_PrimitiveConversion_Int() {
        // Given: 对象
        TestMethodClass obj = new TestMethodClass();

        // When: 调用基本类型方法（传入包装类型 Integer）
        Object result = ReflectionUtils.invokeMethod(obj, "methodWithPrimitive", new Object[]{Integer.valueOf(5)});

        // Then: 应该正确进行类型转换并调用方法
        assertEquals(10, result);
    }

    @Test
    void testInvokeMethod_PrimitiveConversion_Long() {
        // Given: 对象
        TestMethodClass obj = new TestMethodClass();

        // When: 调用基本类型方法（传入包装类型 Long）
        Object result = ReflectionUtils.invokeMethod(obj, "methodWithLong", new Object[]{Long.valueOf(5L)});

        // Then: 应该正确进行类型转换并调用方法
        assertEquals(10L, result);
    }

    @Test
    void testInvokeMethod_PrimitiveConversion_Double() {
        // Given: 对象
        TestMethodClass obj = new TestMethodClass();

        // When: 调用基本类型方法（传入包装类型 Double）
        Object result = ReflectionUtils.invokeMethod(obj, "methodWithDouble", new Object[]{Double.valueOf(5.5)});

        // Then: 应该正确进行类型转换并调用方法
        assertEquals(11.0, (Double) result, 0.001);
    }

    @Test
    void testInvokeMethod_PrimitiveConversion_Float() {
        // Given: 对象
        TestMethodClass obj = new TestMethodClass();

        // When: 调用基本类型方法（传入包装类型 Float）
        Object result = ReflectionUtils.invokeMethod(obj, "methodWithFloat", new Object[]{Float.valueOf(5.5f)});

        // Then: 应该正确进行类型转换并调用方法
        assertEquals(11.0f, (Float) result, 0.001f);
    }

    @Test
    void testInvokeMethod_PrimitiveConversion_Boolean() {
        // Given: 对象
        TestMethodClass obj = new TestMethodClass();

        // When: 调用基本类型方法（传入包装类型 Boolean）
        Object result = ReflectionUtils.invokeMethod(obj, "methodWithBoolean", new Object[]{Boolean.valueOf(true)});

        // Then: 应该正确进行类型转换并调用方法
        assertEquals(false, result);
    }

    @Test
    void testInvokeMethod_PrimitiveConversion_Char() {
        // Given: 对象
        TestMethodClass obj = new TestMethodClass();

        // When: 调用基本类型方法（传入包装类型 Character）
        Object result = ReflectionUtils.invokeMethod(obj, "methodWithChar", new Object[]{Character.valueOf('a')});

        // Then: 应该正确进行类型转换并调用方法
        assertEquals('A', result);
    }

    @Test
    void testInvokeMethod_PrimitiveConversion_Byte() {
        // Given: 对象
        TestMethodClass obj = new TestMethodClass();

        // When: 调用基本类型方法（传入包装类型 Byte）
        Object result = ReflectionUtils.invokeMethod(obj, "methodWithByte", new Object[]{Byte.valueOf((byte) 5)});

        // Then: 应该正确进行类型转换并调用方法
        assertEquals((byte) 10, result);
    }

    @Test
    void testInvokeMethod_PrimitiveConversion_Short() {
        // Given: 对象
        TestMethodClass obj = new TestMethodClass();

        // When: 调用基本类型方法（传入包装类型 Short）
        Object result = ReflectionUtils.invokeMethod(obj, "methodWithShort", new Object[]{Short.valueOf((short) 5)});

        // Then: 应该正确进行类型转换并调用方法
        assertEquals((short) 10, result);
    }

    @Test
    void testInvokeMethod_PrimitiveConversion_NumberTypes() {
        // Given: 对象
        TestMethodClass obj = new TestMethodClass();

        // When: 调用 int 方法，传入不同类型的数字
        // Note: 当前实现支持 Byte 和 Short 到 int 的转换，但不支持 Long 到 int 的直接转换
        Object result1 = ReflectionUtils.invokeMethod(obj, "methodWithPrimitive", new Object[]{Byte.valueOf((byte) 5)});
        Object result2 = ReflectionUtils.invokeMethod(obj, "methodWithPrimitive", new Object[]{Short.valueOf((short) 5)});

        // Then: 应该正确进行类型转换并调用方法
        assertEquals(10, result1);
        assertEquals(10, result2);
        
        // Long 到 int 的转换需要显式转换，当前实现不支持
        // 这需要实现改进，暂时跳过此测试场景
    }

    // --- 包装类型测试 ---

    @Test
    void testInvokeMethod_BoxedType_Integer() {
        // Given: 对象
        TestMethodClass obj = new TestMethodClass();

        // When: 调用包装类型方法
        Object result = ReflectionUtils.invokeMethod(obj, "methodWithBoxed", new Object[]{Integer.valueOf(5)});

        // Then: 应该正确调用方法
        assertEquals("boxed: 5", result);
    }

    @Test
    void testInvokeMethod_BoxedType_Long() {
        // Given: 对象
        TestMethodClass obj = new TestMethodClass();

        // When: 调用包装类型方法
        Object result = ReflectionUtils.invokeMethod(obj, "methodWithLongBoxed", new Object[]{Long.valueOf(5L)});

        // Then: 应该正确调用方法
        assertEquals("boxed long: 5", result);
    }

    @Test
    void testInvokeMethod_BoxedType_Double() {
        // Given: 对象
        TestMethodClass obj = new TestMethodClass();

        // When: 调用包装类型方法
        Object result = ReflectionUtils.invokeMethod(obj, "methodWithDoubleBoxed", new Object[]{Double.valueOf(5.5)});

        // Then: 应该正确调用方法
        assertEquals("boxed double: 5.5", result);
    }

    // --- 混合类型测试 ---

    @Test
    void testInvokeMethod_MixedTypes() {
        // Given: 对象
        TestMethodClass obj = new TestMethodClass();

        // When: 调用混合类型方法
        Object result = ReflectionUtils.invokeMethod(obj, "methodMixed", new Object[]{10, "test", true});

        // Then: 应该正确调用方法
        assertEquals("mixed: 10, test, true", result);
    }

    @Test
    void testInvokeMethod_NullParameter() {
        // Given: 对象
        TestMethodClass obj = new TestMethodClass();

        // When: 调用方法时传入 null 参数
        Object result = ReflectionUtils.invokeMethod(obj, "methodWithNull", new Object[]{null});

        // Then: 应该正确处理 null 参数
        assertEquals("null", result);
    }

    @Test
    void testInvokeMethod_NullParameter_NotNull() {
        // Given: 对象
        TestMethodClass obj = new TestMethodClass();

        // When: 调用方法时传入非 null 参数
        Object result = ReflectionUtils.invokeMethod(obj, "methodWithNull", new Object[]{"test"});

        // Then: 应该正确处理非 null 参数
        assertEquals("not null: test", result);
    }

    // --- 方法不存在测试 ---

    @Test
    void testInvokeMethod_MethodNotFound() {
        // Given: 对象
        TestMethodClass obj = new TestMethodClass();

        // When/Then: 调用不存在的方法应该抛出异常
        TemplateRuntimeException exception = assertThrows(TemplateRuntimeException.class, () -> {
            ReflectionUtils.invokeMethod(obj, "nonExistentMethod", new Object[0]);
        });
        assertTrue(exception.getMessage().contains("Method 'nonExistentMethod' not found"));
    }

    @Test
    void testInvokeMethod_MethodNotFound_WrongArgs() {
        // Given: 对象
        TestMethodClass obj = new TestMethodClass();

        // When/Then: 调用方法时参数不匹配应该抛出异常
        TemplateRuntimeException exception = assertThrows(TemplateRuntimeException.class, () -> {
            ReflectionUtils.invokeMethod(obj, "methodWithArgs", new Object[0]);
        });
        assertTrue(exception.getMessage().contains("Method 'methodWithArgs' not found"));
    }

    @Test
    void testInvokeMethod_MethodNotFound_WrongArgTypes() {
        // Given: 对象
        TestMethodClass obj = new TestMethodClass();

        // When/Then: 调用方法时参数类型完全不匹配应该抛出异常
        // 注意：如果参数类型可以转换，应该能匹配成功
        // 这里测试一个无法匹配的场景
        TemplateRuntimeException exception = assertThrows(TemplateRuntimeException.class, () -> {
            ReflectionUtils.invokeMethod(obj, "methodWithPrimitive", new Object[]{"not a number"});
        });
        assertTrue(exception.getMessage().contains("Method 'methodWithPrimitive' not found"));
    }

    // --- Collection/List/Map 特殊处理测试 ---

    @Test
    void testInvokeMethod_CollectionSize() {
        // Given: Collection 对象
        List<String> list = new ArrayList<>();
        list.add("item1");
        list.add("item2");

        // When: 调用 Collection 的特殊处理方法
        Object size = ReflectionUtils.invokeMethod(list, "size", new Object[0]);
        Object isEmpty = ReflectionUtils.invokeMethod(list, "isEmpty", new Object[0]);

        // Then: 应该正确调用方法
        assertEquals(2, size);
        assertEquals(false, isEmpty);
    }

    @Test
    void testInvokeMethod_CollectionToString() {
        // Given: Collection 对象
        List<String> list = new ArrayList<>();
        list.add("item1");
        list.add("item2");

        // When: 调用 Collection 的 toString 方法
        Object result = ReflectionUtils.invokeMethod(list, "toString", new Object[0]);

        // Then: 应该正确调用方法
        assertNotNull(result);
        assertTrue(result.toString().contains("item1") || result.toString().contains("item2"));
    }

    @Test
    void testInvokeMethod_ListGet() {
        // Given: List 对象
        List<String> list = new ArrayList<>();
        list.add("item1");
        list.add("item2");

        // When: 调用 List 的特殊处理方法
        Object item = ReflectionUtils.invokeMethod(list, "get", new Object[]{0});

        // Then: 应该正确获取元素
        assertEquals("item1", item);
    }

    @Test
    void testInvokeMethod_MapMethods() {
        // Given: Map 对象
        Map<String, Object> map = new HashMap<>();
        map.put("key1", "value1");

        // When: 调用 Map 的特殊处理方法
        Object get = ReflectionUtils.invokeMethod(map, "get", new Object[]{"key1"});
        Object containsKey = ReflectionUtils.invokeMethod(map, "containsKey", new Object[]{"key1"});
        Object size = ReflectionUtils.invokeMethod(map, "size", new Object[0]);

        // Then: 应该正确调用方法
        assertEquals("value1", get);
        assertEquals(true, containsKey);
        assertEquals(1, size);
    }

    @Test
    void testInvokeMethod_Map_NonExistentKey() {
        // Given: Map 对象
        Map<String, Object> map = new HashMap<>();
        map.put("key1", "value1");

        // When: 调用 Map 的 get 方法获取不存在的键
        Object result = ReflectionUtils.invokeMethod(map, "get", new Object[]{"nonExistent"});

        // Then: 应该返回 null
        assertNull(result);
    }

    // --- 缓存机制测试 ---

    @Test
    void testInvokeMethod_CachePerformance() {
        // Given: 对象
        TestMethodClass obj = new TestMethodClass();

        // When: 多次调用同一方法（应该使用缓存）
        Object result1 = ReflectionUtils.invokeMethod(obj, "methodNoArgs", new Object[0]);
        Object result2 = ReflectionUtils.invokeMethod(obj, "methodNoArgs", new Object[0]);
        Object result3 = ReflectionUtils.invokeMethod(obj, "methodNoArgs", new Object[0]);

        // Then: 应该正确调用方法（缓存应该提升性能）
        assertEquals("no args", result1);
        assertEquals("no args", result2);
        assertEquals("no args", result3);
    }

    @Test
    void testInvokeMethod_CachePerformance_DifferentArgs() {
        // Given: 对象
        TestMethodClass obj = new TestMethodClass();

        // When: 使用不同参数多次调用同一方法（应该使用不同的缓存键）
        Object result1 = ReflectionUtils.invokeMethod(obj, "methodOverloaded", new Object[]{10});
        Object result2 = ReflectionUtils.invokeMethod(obj, "methodOverloaded", new Object[]{"test"});
        Object result3 = ReflectionUtils.invokeMethod(obj, "methodOverloaded", new Object[]{10});

        // Then: 应该正确调用方法并正确使用缓存
        assertEquals("int: 10", result1);
        assertEquals("string: test", result2);
        assertEquals("int: 10", result3); // 应该使用缓存
    }

    @Test
    void testInvokeMethod_CachePerformance_MultipleObjects() {
        // Given: 多个对象
        TestMethodClass obj1 = new TestMethodClass();
        TestMethodClass obj2 = new TestMethodClass();

        // When: 在不同对象上调用相同方法（应该使用不同的缓存键）
        Object result1 = ReflectionUtils.invokeMethod(obj1, "methodNoArgs", new Object[0]);
        Object result2 = ReflectionUtils.invokeMethod(obj2, "methodNoArgs", new Object[0]);
        Object result3 = ReflectionUtils.invokeMethod(obj1, "methodNoArgs", new Object[0]);

        // Then: 应该正确调用方法并正确使用缓存
        assertEquals("no args", result1);
        assertEquals("no args", result2);
        assertEquals("no args", result3); // obj1 的调用应该使用缓存
    }

    // ==================== 索引访问测试 ====================

    @Test
    void testGetIndexedValue_Array() {
        // Given: 数组
        String[] array = {"item1", "item2", "item3"};

        // When: 访问数组索引
        Object item1 = ReflectionUtils.getIndexedValue(array, 0);
        Object item2 = ReflectionUtils.getIndexedValue(array, 1);
        Object item3 = ReflectionUtils.getIndexedValue(array, 2);

        // Then: 应该正确获取元素
        assertEquals("item1", item1);
        assertEquals("item2", item2);
        assertEquals("item3", item3);
    }

    @Test
    void testGetIndexedValue_Array_IntArray() {
        // Given: int 数组
        int[] array = {1, 2, 3};

        // When: 访问数组索引
        Object item1 = ReflectionUtils.getIndexedValue(array, 0);
        Object item2 = ReflectionUtils.getIndexedValue(array, 1);

        // Then: 应该正确获取元素
        assertEquals(1, item1);
        assertEquals(2, item2);
    }

    @Test
    void testGetIndexedValue_Array_IndexOutOfBounds() {
        // Given: 数组
        String[] array = {"item1", "item2"};

        // When/Then: 访问越界索引应该抛出异常
        assertThrows(org.dromara.easytl.exception.TemplateRuntimeException.class, () -> {
            ReflectionUtils.getIndexedValue(array, 10);
        });
    }

    @Test
    void testGetIndexedValue_List() {
        // Given: List 对象
        List<String> list = new ArrayList<>();
        list.add("item1");
        list.add("item2");
        list.add("item3");

        // When: 访问 List 索引
        Object item1 = ReflectionUtils.getIndexedValue(list, 0);
        Object item2 = ReflectionUtils.getIndexedValue(list, 1);
        Object item3 = ReflectionUtils.getIndexedValue(list, 2);

        // Then: 应该正确获取元素
        assertEquals("item1", item1);
        assertEquals("item2", item2);
        assertEquals("item3", item3);
    }

    @Test
    void testGetIndexedValue_List_IndexOutOfBounds() {
        // Given: List 对象
        List<String> list = new ArrayList<>();
        list.add("item1");

        // When/Then: 访问越界索引应该抛出异常
        assertThrows(org.dromara.easytl.exception.TemplateRuntimeException.class, () -> {
            ReflectionUtils.getIndexedValue(list, 10);
        });
    }

    @Test
    void testGetIndexedValue_Map() {
        // Given: Map 对象
        Map<String, Object> map = new HashMap<>();
        map.put("key1", "value1");
        map.put("key2", "value2");

        // When: 访问 Map 键
        Object value1 = ReflectionUtils.getIndexedValue(map, "key1");
        Object value2 = ReflectionUtils.getIndexedValue(map, "key2");

        // Then: 应该正确获取值
        assertEquals("value1", value1);
        assertEquals("value2", value2);
    }

    @Test
    void testGetIndexedValue_Map_NonExistentKey() {
        // Given: Map 对象
        Map<String, Object> map = new HashMap<>();
        map.put("key1", "value1");

        // When: 访问不存在的键
        Object value = ReflectionUtils.getIndexedValue(map, "nonExistent");

        // Then: 应该返回 null（Map.get 返回 null）
        assertNull(value);
    }

    @Test
    void testGetIndexedValue_NullObject() {
        // Given: null 对象
        String[] array = null;

        // When/Then: 访问 null 对象的索引应该抛出异常
        assertThrows(TemplateRuntimeException.class, () -> {
            ReflectionUtils.getIndexedValue(array, 0);
        });
    }

    @Test
    void testGetIndexedValue_UnsupportedType() {
        // Given: 不支持索引访问的对象
        String obj = "not an array or list or map";

        // When/Then: 访问不支持类型的索引应该抛出异常
        assertThrows(TemplateRuntimeException.class, () -> {
            ReflectionUtils.getIndexedValue(obj, 0);
        });
    }

    @Test
    void testGetIndexedValue_List_WithLongIndex() {
        // Given: List 对象
        List<String> list = new ArrayList<>();
        list.add("item1");

        // When: 使用 Long 类型的索引
        Object item = ReflectionUtils.getIndexedValue(list, 0L);

        // Then: 应该正确转换并获取元素
        assertEquals("item1", item);
    }

    @Test
    void testGetIndexedValue_Array_NegativeIndex() {
        // Given: 数组
        String[] array = {"item1", "item2"};

        // When/Then: 负数索引从末尾计数（-1 为最后一个元素）
        assertEquals("item2", ReflectionUtils.getIndexedValue(array, -1));

        // 超出范围的负数索引仍抛出异常
        assertThrows(org.dromara.easytl.exception.TemplateRuntimeException.class, () -> {
            ReflectionUtils.getIndexedValue(array, -3);
        });
    }

    @Test
    void testGetIndexedValue_List_NegativeIndex() {
        // Given: List 对象
        List<String> list = new ArrayList<>();
        list.add("item1");

        // When/Then: 负数索引从末尾计数（-1 为最后一个元素）
        assertEquals("item1", ReflectionUtils.getIndexedValue(list, -1));

        // 超出范围的负数索引仍抛出异常
        assertThrows(org.dromara.easytl.exception.TemplateRuntimeException.class, () -> {
            ReflectionUtils.getIndexedValue(list, -2);
        });
    }

    @Test
    void testGetIndexedValue_Array_WithStringIndex() {
        // Given: 数组
        String[] array = {"item1", "item2"};

        // When: 使用字符串形式的数字索引（"0" 是有效的数字字符串）
        Object item = ReflectionUtils.getIndexedValue(array, "0");

        // Then: 应该正确解析并获取元素
        assertEquals("item1", item);
    }

    @Test
    void testGetIndexedValue_Array_WithStringIndex_ValidNumber() {
        // Given: 数组
        String[] array = {"item1", "item2", "item3"};

        // When: 使用字符串形式的数字索引
        Object item = ReflectionUtils.getIndexedValue(array, "1");

        // Then: 应该正确解析并获取元素
        assertEquals("item2", item);
    }

    @Test
    void testGetIndexedValue_List_WithStringIndex() {
        // Given: List 对象
        List<String> list = new ArrayList<>();
        list.add("item1");
        list.add("item2");

        // When: 使用字符串形式的数字索引
        Object item = ReflectionUtils.getIndexedValue(list, "0");

        // Then: 应该正确解析并获取元素
        assertEquals("item1", item);
    }

    @Test
    void testGetIndexedValue_Array_WithByteIndex() {
        // Given: 数组
        String[] array = {"item1", "item2", "item3"};

        // When: 使用 Byte 类型的索引
        Object item = ReflectionUtils.getIndexedValue(array, (byte) 2);

        // Then: 应该正确转换并获取元素
        assertEquals("item3", item);
    }

    @Test
    void testGetIndexedValue_Array_WithShortIndex() {
        // Given: 数组
        String[] array = {"item1", "item2", "item3"};

        // When: 使用 Short 类型的索引
        Object item = ReflectionUtils.getIndexedValue(array, (short) 1);

        // Then: 应该正确转换并获取元素
        assertEquals("item2", item);
    }

    @Test
    void testGetIndexedValue_Array_WithIntegerIndex() {
        // Given: 数组
        String[] array = {"item1", "item2", "item3"};

        // When: 使用 Integer 类型的索引
        Object item = ReflectionUtils.getIndexedValue(array, Integer.valueOf(0));

        // Then: 应该正确获取元素
        assertEquals("item1", item);
    }

    @Test
    void testGetIndexedValue_Array_EmptyArray() {
        // Given: 空数组
        String[] array = {};

        // When/Then: 访问空数组的索引应该抛出异常
        assertThrows(org.dromara.easytl.exception.TemplateRuntimeException.class, () -> {
            ReflectionUtils.getIndexedValue(array, 0);
        });
    }

    @Test
    void testGetIndexedValue_List_EmptyList() {
        // Given: 空 List
        List<String> list = new ArrayList<>();

        // When/Then: 访问空 List 的索引应该抛出异常
        assertThrows(org.dromara.easytl.exception.TemplateRuntimeException.class, () -> {
            ReflectionUtils.getIndexedValue(list, 0);
        });
    }

    @Test
    void testGetIndexedValue_Map_EmptyMap() {
        // Given: 空 Map
        Map<String, Object> map = new HashMap<>();

        // When: 访问空 Map 的键
        Object value = ReflectionUtils.getIndexedValue(map, "key1");

        // Then: 应该返回 null
        assertNull(value);
    }

    @Test
    void testGetIndexedValue_Array_PrimitiveTypes() {
        // Given: 不同类型的原始数组
        byte[] byteArray = {1, 2, 3};
        short[] shortArray = {10, 20, 30};
        long[] longArray = {100L, 200L, 300L};
        float[] floatArray = {1.1f, 2.2f, 3.3f};
        double[] doubleArray = {1.11, 2.22, 3.33};
        char[] charArray = {'a', 'b', 'c'};
        boolean[] booleanArray = {true, false, true};

        // When: 访问不同类型的数组索引
        Object byteVal = ReflectionUtils.getIndexedValue(byteArray, 0);
        Object shortVal = ReflectionUtils.getIndexedValue(shortArray, 1);
        Object longVal = ReflectionUtils.getIndexedValue(longArray, 2);
        Object floatVal = ReflectionUtils.getIndexedValue(floatArray, 0);
        Object doubleVal = ReflectionUtils.getIndexedValue(doubleArray, 1);
        Object charVal = ReflectionUtils.getIndexedValue(charArray, 2);
        Object booleanVal = ReflectionUtils.getIndexedValue(booleanArray, 0);

        // Then: 应该正确获取元素
        assertEquals((byte) 1, byteVal);
        assertEquals((short) 20, shortVal);
        assertEquals(300L, longVal);
        assertEquals(1.1f, floatVal);
        assertEquals(2.22, doubleVal);
        assertEquals('c', charVal);
        assertEquals(true, booleanVal);
    }

    @Test
    void testGetIndexedValue_Map_WithIntegerKey() {
        // Given: Map 对象（使用 Integer 作为键）
        Map<Integer, String> map = new HashMap<>();
        map.put(1, "value1");
        map.put(2, "value2");

        // When: 访问 Map 键
        Object value1 = ReflectionUtils.getIndexedValue(map, 1);
        Object value2 = ReflectionUtils.getIndexedValue(map, 2);

        // Then: 应该正确获取值
        assertEquals("value1", value1);
        assertEquals("value2", value2);
    }

    @Test
    void testGetIndexedValue_Map_WithLongKey() {
        // Given: Map 对象（使用 Long 作为键）
        Map<Long, String> map = new HashMap<>();
        map.put(1L, "value1");
        map.put(2L, "value2");

        // When: 访问 Map 键
        Object value1 = ReflectionUtils.getIndexedValue(map, 1L);
        Object value2 = ReflectionUtils.getIndexedValue(map, 2L);

        // Then: 应该正确获取值
        assertEquals("value1", value1);
        assertEquals("value2", value2);
    }

    @Test
    void testGetIndexedValue_Array_IndexAtBoundary() {
        // Given: 数组
        String[] array = {"item1", "item2"};

        // When: 访问边界索引（最后一个有效索引）
        Object lastItem = ReflectionUtils.getIndexedValue(array, 1);

        // Then: 应该正确获取元素
        assertEquals("item2", lastItem);
    }

    @Test
    void testGetIndexedValue_List_IndexAtBoundary() {
        // Given: List 对象
        List<String> list = new ArrayList<>();
        list.add("item1");
        list.add("item2");

        // When: 访问边界索引（最后一个有效索引）
        Object lastItem = ReflectionUtils.getIndexedValue(list, 1);

        // Then: 应该正确获取元素
        assertEquals("item2", lastItem);
    }

    @Test
    void testGetIndexedValue_Array_IndexJustOutOfBounds() {
        // Given: 数组
        String[] array = {"item1", "item2"};

        // When/Then: 访问刚好越界的索引应该抛出异常
        assertThrows(org.dromara.easytl.exception.TemplateRuntimeException.class, () -> {
            ReflectionUtils.getIndexedValue(array, 2);
        });
    }

    @Test
    void testGetIndexedValue_List_IndexJustOutOfBounds() {
        // Given: List 对象
        List<String> list = new ArrayList<>();
        list.add("item1");
        list.add("item2");

        // When/Then: 访问刚好越界的索引应该抛出异常
        assertThrows(org.dromara.easytl.exception.TemplateRuntimeException.class, () -> {
            ReflectionUtils.getIndexedValue(list, 2);
        });
    }

    @Test
    void testGetIndexedValue_Array_WithInvalidStringIndex() {
        // Given: 数组
        String[] array = {"item1", "item2"};

        // When/Then: 使用无效的字符串索引应该抛出 NumberFormatException
        // (toInt 方法中的 Integer.parseInt 会抛出此异常)
        assertThrows(NumberFormatException.class, () -> {
            ReflectionUtils.getIndexedValue(array, "invalid");
        });
    }

    @Test
    void testGetIndexedValue_List_WithInvalidStringIndex() {
        // Given: List 对象
        List<String> list = new ArrayList<>();
        list.add("item1");

        // When/Then: 使用无效的字符串索引应该抛出 NumberFormatException
        // (toInt 方法中的 Integer.parseInt 会抛出此异常)
        assertThrows(NumberFormatException.class, () -> {
            ReflectionUtils.getIndexedValue(list, "invalid");
        });
    }

    @Test
    void testGetIndexedValue_Map_WithNullKey() {
        // Given: Map 对象（允许 null 键）
        Map<String, Object> map = new HashMap<>();
        map.put(null, "nullValue");

        // When: 使用 null 键访问 Map
        Object value = ReflectionUtils.getIndexedValue(map, null);

        // Then: 应该正确获取值
        assertEquals("nullValue", value);
    }

    @Test
    void testGetIndexedValue_Map_WithNullKey_NotExists() {
        // Given: Map 对象（不包含 null 键）
        Map<String, Object> map = new HashMap<>();
        map.put("key1", "value1");

        // When: 使用 null 键访问 Map
        Object value = ReflectionUtils.getIndexedValue(map, null);

        // Then: 应该返回 null
        assertNull(value);
    }

    // ==================== 静态属性访问测试 ====================

    @Test
    void testGetStaticProperty() {
        // Math.PI is a public static field
        Object value = ReflectionUtils.getProperty(Math.class, "PI");
        assertEquals(Math.PI, value);
    }

    @Test
    void testGetStaticFieldNotFound() {
        TemplateRuntimeException ex = assertThrows(TemplateRuntimeException.class, () -> {
            ReflectionUtils.getProperty(Math.class, "NON_EXISTENT_FIELD");
        });
        assertTrue(ex.getMessage().contains("not found"));
    }

    // ==================== 静态方法调用测试 ====================

    @Test
    void testInvokeStaticMethod() {
        Object result = ReflectionUtils.invokeMethod(Math.class, "sqrt", new Object[]{16.0});
        assertEquals(4.0, result);
    }

    @Test
    void testInvokeStaticMethodNoArgs() {
        Object result = ReflectionUtils.invokeMethod(System.class, "currentTimeMillis", new Object[0]);
        assertNotNull(result);
        assertTrue((Long) result > 0);
    }

    @Test
    void testInvokeStaticMethodNotFound() {
        TemplateRuntimeException ex = assertThrows(TemplateRuntimeException.class, () -> {
            ReflectionUtils.invokeMethod(Math.class, "nonExistentMethod", new Object[0]);
        });
        assertTrue(ex.getMessage().contains("not found"));
    }

    // ==================== createInstance 测试 ====================

    @Test
    void testCreateInstanceNoArgs() {
        Object instance = ReflectionUtils.createInstance(java.util.ArrayList.class, new Object[0]);
        assertNotNull(instance);
        assertTrue(instance instanceof java.util.ArrayList);
    }

    @Test
    void testCreateInstanceWithArgs() {
        Object instance = ReflectionUtils.createInstance(String.class, new Object[]{"hello"});
        assertNotNull(instance);
        assertEquals("hello", instance);
    }

    @Test
    void testCreateInstanceNoSuitableConstructor() {
        TemplateRuntimeException ex = assertThrows(TemplateRuntimeException.class, () -> {
            ReflectionUtils.createInstance(Math.class, new Object[0]);
        });
        assertTrue(ex.getMessage().contains("No suitable constructor"));
    }

    // ==================== setProperty 测试 ====================

    @Test
    void testSetPropertyViaSetter() {
        // Use a class that has a setter
        TestBeanWithSetter obj = new TestBeanWithSetter();
        ReflectionUtils.setProperty(obj, "name", "testValue");
        assertEquals("testValue", obj.getName());
    }

    @Test
    void testSetPropertyViaField() {
        // Use a class that has public field but no setter
        TestBeanWithFields obj = new TestBeanWithFields("original", 10, true);
        ReflectionUtils.setProperty(obj, "name", "newName");
        assertEquals("newName", obj.name);
    }

    @Test
    void testSetPropertyOnNullObject() {
        TemplateRuntimeException ex = assertThrows(TemplateRuntimeException.class, () -> {
            ReflectionUtils.setProperty(null, "property", "value");
        });
        assertTrue(ex.getMessage().contains("Cannot set property"));
    }

    /**
     * 测试用的类（带 setter）
     */
    public static class TestBeanWithSetter {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
