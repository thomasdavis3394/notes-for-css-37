package org.dromara.easytl.runtime;

import org.dromara.easytl.exception.TemplateRuntimeException;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 反射工具类
 * <p>
 * 提供属性访问、方法调用和索引访问等反射操作
 * 包含反射结果缓存以提升性能
 * </p>
 *
 * @author EasyTL Team
 * @since 1.0.0
 */
public class ReflectionUtils {

    /**
     * Method 缓存：缓存查找的方法
     * Key: className + "::" + methodName + "::" + paramTypes
     * Value: Method 对象
     */
    private static final Map<String, Method> methodCache = new ConcurrentHashMap<>();

    /**
     * Getter 方法缓存：缓存查找的 getter 方法
     * Key: className + "::" + propertyName
     * Value: Method 对象
     */
    private static final Map<String, Method> getterCache = new ConcurrentHashMap<>();

    /**
     * Field 缓存：缓存查找的字段
     * Key: className + "::" + fieldName
     * Value: Field 对象
     */
    private static final Map<String, Field> fieldCache = new ConcurrentHashMap<>();

    /**
     * 获取对象的属性值
     * <p>
     * 支持：
     * - getter方法（getXxx、isXxx）
     * - public字段
     * - Map的get方法
     * </p>
     *
     * @param obj 对象
     * @param propertyName 属性名
     * @return 属性值
     */
    public static Object getProperty(Object obj, String propertyName) {
        if (obj == null) {
            throw new TemplateRuntimeException("Cannot get property '" + propertyName + "' of null");
        }

        // 新增：如果 obj 是 Class<?>，尝试获取静态属性
        if (obj instanceof Class) {
            Class<?> clazz = (Class<?>) obj;
            return getStaticProperty(clazz, propertyName);
        }

        // 如果是Map，直接使用get方法
        if (obj instanceof Map) {
            return ((Map<?, ?>) obj).get(propertyName);
        }

        Class<?> clazz = obj.getClass();

        // 尝试getter方法（使用缓存）
        Method getter = findGetterCached(clazz, propertyName);
        if (getter != null) {
            try {
                // Ensure the method is accessible, especially for Java 9+ modules
                if (!getter.isAccessible()) {
                    getter.setAccessible(true);
                }
                return getter.invoke(obj);
            } catch (Exception e) {
                throw new TemplateRuntimeException("Failed to invoke getter method '" + getter.getName() + "': " + e.getMessage(), e);
            }
        }

        // 尝试public字段（使用缓存）
        try {
            Field field = findFieldCached(clazz, propertyName);
            // Ensure the field is accessible, especially for Java 9+ modules
            if (!field.isAccessible()) {
                field.setAccessible(true);
            }
            return field.get(obj);
        } catch (NoSuchFieldException e) {
            throw new TemplateRuntimeException("Property '" + propertyName + "' not found in " + clazz.getName());
        } catch (Exception e) {
            throw new TemplateRuntimeException("Failed to access field '" + propertyName + "': " + e.getMessage(), e);
        }
    }

    /**
     * 获取类的静态属性值
     *
     * @param clazz 类
     * @param propertyName 属性名
     * @return 属性值
     */
    private static Object getStaticProperty(Class<?> clazz, String propertyName) {
        // 尝试静态 getter 方法
        Method getter = findStaticGetter(clazz, propertyName);
        if (getter != null) {
            try {
                if (!getter.isAccessible()) {
                    getter.setAccessible(true);
                }
                return getter.invoke(null);
            } catch (Exception e) {
                throw new TemplateRuntimeException(
                    "Failed to invoke static getter '" + getter.getName() + "': " + e.getMessage(), e);
            }
        }

        // 尝试静态字段
        Field field = findStaticField(clazz, propertyName);
        if (field != null) {
            try {
                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }
                return field.get(null);
            } catch (Exception e) {
                throw new TemplateRuntimeException(
                    "Failed to access static field '" + propertyName + "': " + e.getMessage(), e);
            }
        }

        throw new TemplateRuntimeException(
            "Static property '" + propertyName + "' not found in " + clazz.getName());
    }

    /**
     * 查找静态 getter 方法
     */
    private static Method findStaticGetter(Class<?> clazz, String propertyName) {
        // 尝试 getXxx 形式
        String getterName = "get" + capitalize(propertyName);
        try {
            Method method = clazz.getMethod(getterName);
            if (Modifier.isStatic(method.getModifiers())) {
                return method;
            }
        } catch (NoSuchMethodException e) {
            // 继续尝试其他形式
        }

        // 尝试 isXxx 形式
        String isGetterName = "is" + capitalize(propertyName);
        try {
            Method method = clazz.getMethod(isGetterName);
            if (Modifier.isStatic(method.getModifiers()) &&
                (method.getReturnType() == boolean.class || method.getReturnType() == Boolean.class)) {
                return method;
            }
        } catch (NoSuchMethodException e) {
            // 忽略
        }

        return null;
    }

    /**
     * 查找静态字段
     */
    private static Field findStaticField(Class<?> clazz, String fieldName) {
        try {
            Field field = clazz.getField(fieldName);
            if (Modifier.isStatic(field.getModifiers())) {
                return field;
            }
        } catch (NoSuchFieldException e) {
            // 尝试查找 declared field
            try {
                Field field = clazz.getDeclaredField(fieldName);
                if (Modifier.isStatic(field.getModifiers())) {
                    return field;
                }
            } catch (NoSuchFieldException ex) {
                // 忽略
            }
        }
        return null;
    }

    /**
     * 调用对象的方法
     * <p>
     * 支持方法重载匹配和可变参数
     * </p>
     *
     * @param obj 对象
     * @param methodName 方法名
     * @param args 参数数组
     * @return 方法返回值
     */
    public static Object invokeMethod(Object obj, String methodName, Object[] args) {
        if (obj == null) {
            throw new TemplateRuntimeException("Cannot invoke method '" + methodName + "' on null");
        }

        // 新增：如果 obj 是 Class<?>，尝试调用静态方法
        if (obj instanceof Class) {
            Class<?> clazz = (Class<?>) obj;
            return invokeStaticMethod(clazz, methodName, args);
        }

        // Special handling for Collection methods to avoid module access issues
        if (obj instanceof Collection && args.length == 0) {
            Collection<?> collection = (Collection<?>) obj;
            switch (methodName) {
                case "size":
                    return collection.size();
                case "isEmpty":
                    return collection.isEmpty();
                case "toString":
                    return collection.toString();
            }
        }

        // Special handling for List methods
        if (obj instanceof List) {
            List<?> list = (List<?>) obj;
            if ("get".equals(methodName) && args.length == 1 && args[0] instanceof Number) {
                return list.get(((Number) args[0]).intValue());
            }
        }

        // Special handling for Map methods
        if (obj instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) obj;
            if ("get".equals(methodName) && args.length == 1) {
                return map.get(args[0]);
            }
            if ("containsKey".equals(methodName) && args.length == 1) {
                return map.containsKey(args[0]);
            }
            if ("size".equals(methodName) && args.length == 0) {
                return map.size();
            }
        }

        Class<?> clazz = obj.getClass();
        Method method = findMethodCached(clazz, methodName, args);

        if (method == null) {
            throw new TemplateRuntimeException("Method '" + methodName + "' not found in " + clazz.getName());
        }

        // Adapt LambdaObject arguments to functional interfaces
        Object[] adaptedArgs = adaptLambdaArguments(method, args);

        try {
            // Try to invoke the method directly first
            return method.invoke(obj, adaptedArgs);
        } catch (IllegalAccessException e) {
            // If access is denied, try to make it accessible
            try {
                if (!method.isAccessible()) {
                    method.setAccessible(true);
                }
                return method.invoke(obj, adaptedArgs);
            } catch (Exception ex) {
                throw new TemplateRuntimeException("Failed to invoke method '" + methodName + "': " + ex.getMessage(), ex);
            }
        } catch (Exception e) {
            throw new TemplateRuntimeException("Failed to invoke method '" + methodName + "': " + e.getMessage(), e);
        }
    }

    /**
     * Adapt LambdaObject arguments to their corresponding functional interface types.
     *
     * @param method the method to be invoked
     * @param args   the arguments (may contain LambdaObjects)
     * @return adapted arguments with LambdaObjects converted to proxy instances
     */
    private static Object[] adaptLambdaArguments(Method method, Object[] args) {
        if (args == null || args.length == 0) {
            return args;
        }

        Class<?>[] paramTypes = method.getParameterTypes();
        Object[] adaptedArgs = args;

        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof LambdaObject && i < paramTypes.length) {
                Class<?> paramType = paramTypes[i];
                if (FunctionalInterfaceUtils.isFunctionalInterface(paramType)) {
                    LambdaObject lambda = (LambdaObject) args[i];
                    // Create adapted array if not already done
                    if (adaptedArgs == args) {
                        adaptedArgs = args.clone();
                    }
                    adaptedArgs[i] = LambdaAdapter.adapt(lambda, paramType);
                }
                // If not a functional interface, leave as is (will fail at invocation)
            }
        }

        return adaptedArgs;
    }

    /**
     * 调用静态方法
     *
     * @param clazz 类
     * @param methodName 方法名
     * @param args 参数数组
     * @return 方法返回值
     */
    private static Object invokeStaticMethod(Class<?> clazz, String methodName, Object[] args) {
        Method method = findStaticMethodCached(clazz, methodName, args);

        if (method == null) {
            throw new TemplateRuntimeException(
                "Static method '" + methodName + "' not found in " + clazz.getName());
        }

        Object[] adaptedArgs = adaptLambdaArguments(method, args);

        try {
            if (!method.isAccessible()) {
                method.setAccessible(true);
            }
            return method.invoke(null, adaptedArgs);
        } catch (Exception e) {
            throw new TemplateRuntimeException(
                "Failed to invoke static method '" + methodName + "': " + e.getMessage(), e);
        }
    }

    /**
     * 查找静态方法（带缓存）
     */
    private static Method findStaticMethodCached(Class<?> clazz, String methodName, Object[] args) {
        StringBuilder cacheKeyBuilder = new StringBuilder();
        cacheKeyBuilder.append("static:").append(clazz.getName()).append("::").append(methodName).append("::");
        for (int i = 0; i < args.length; i++) {
            if (i > 0) {
                cacheKeyBuilder.append(",");
            }
            if (args[i] != null) {
                cacheKeyBuilder.append(args[i].getClass().getName());
            } else {
                cacheKeyBuilder.append("null");
            }
        }
        String cacheKey = cacheKeyBuilder.toString();

        Method cached = methodCache.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        Method method = findStaticMethod(clazz, methodName, args);
        if (method != null) {
            methodCache.put(cacheKey, method);
        }
        return method;
    }

    /**
     * 查找静态方法
     */
    private static Method findStaticMethod(Class<?> clazz, String methodName, Object[] args) {
        Method[] methods = clazz.getMethods();
        Method bestMatch = null;
        int bestScore = Integer.MAX_VALUE;

        for (Method method : methods) {
            if (!method.getName().equals(methodName)) {
                continue;
            }

            if (!Modifier.isStatic(method.getModifiers())) {
                continue;
            }

            Class<?>[] paramTypes = method.getParameterTypes();

            if (paramTypes.length != args.length) {
                if (method.isVarArgs() && args.length >= paramTypes.length - 1) {
                    if (bestMatch == null) {
                        bestMatch = method;
                    }
                }
                continue;
            }

            int score = calculateMatchScore(paramTypes, args);
            if (score >= 0 && score < bestScore) {
                bestMatch = method;
                bestScore = score;
            }
        }

        return bestMatch;
    }

    /**
     * 获取索引值
     * <p>
     * 支持：
     * - 数组
     * - List
     * - Map
     * </p>
     *
     * @param obj 对象
     * @param index 索引
     * @return 索引对应的值
     */
    public static Object getIndexedValue(Object obj, Object index) {
        if (obj == null) {
            throw new TemplateRuntimeException("Cannot access index of null");
        }

        // 数组
        if (obj.getClass().isArray()) {
            int idx = toInt(index);
            int length = Array.getLength(obj);
            // 负数下标从末尾计数（-1 为最后一个元素）
            if (idx < 0) {
                idx += length;
            }
            try {
                return Array.get(obj, idx);
            } catch (ArrayIndexOutOfBoundsException e) {
                throw new TemplateRuntimeException("Array index " + toInt(index) + " out of bounds for array of length " + length);
            }
        }

        // List
        if (obj instanceof List) {
            int idx = toInt(index);
            int size = ((List<?>) obj).size();
            // 负数下标从末尾计数（-1 为最后一个元素）
            if (idx < 0) {
                idx += size;
            }
            try {
                return ((List<?>) obj).get(idx);
            } catch (IndexOutOfBoundsException e) {
                throw new TemplateRuntimeException("List index " + toInt(index) + " out of bounds for list of size " + size);
            }
        }

        // Map
        if (obj instanceof Map) {
            return ((Map<?, ?>) obj).get(index);
        }

        throw new TemplateRuntimeException("Cannot access index of " + obj.getClass().getName());
    }

    /**
     * 设置索引值（List、数组、Map）
     *
     * @param obj 对象
     * @param index 索引
     * @param value 要设置的值
     */
    public static void setIndexedValue(Object obj, Object index, Object value) {
        if (obj == null) {
            throw new TemplateRuntimeException("Cannot set index of null");
        }

        if (obj instanceof List) {
            List<?> list = (List<?>) obj;
            int idx = toIndexInt(index);
            // 负数下标从末尾计数（-1 为最后一个元素）
            if (idx < 0) {
                idx += list.size();
            }
            if (idx < 0 || idx >= list.size()) {
                throw new TemplateRuntimeException("Index out of bounds: " + toIndexInt(index));
            }
            @SuppressWarnings("unchecked")
            List<Object> mutableList = (List<Object>) list;
            mutableList.set(idx, value);
        } else if (obj.getClass().isArray()) {
            int idx = toIndexInt(index);
            // 负数下标从末尾计数（-1 为最后一个元素）
            if (idx < 0) {
                idx += Array.getLength(obj);
            }
            Array.set(obj, idx, value);
        } else if (obj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<Object, Object> map = (Map<Object, Object>) obj;
            map.put(index, value);
        } else {
            throw new TemplateRuntimeException("Cannot set index on " + obj.getClass().getName());
        }
    }

    private static int toIndexInt(Object value) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        throw new TemplateRuntimeException("Cannot convert " + value.getClass().getName() + " to int for index");
    }

    /**
     * 创建类的实例
     *
     * @param clazz 类
     * @param args 构造函数参数
     * @return 新创建的实例
     */
    public static Object createInstance(Class<?> clazz, Object[] args) {
        Constructor<?> constructor = findConstructor(clazz, args);

        if (constructor == null) {
            throw new TemplateRuntimeException(
                "No suitable constructor found in " + clazz.getName() + " for given arguments");
        }

        try {
            if (!constructor.isAccessible()) {
                constructor.setAccessible(true);
            }
            return constructor.newInstance(args);
        } catch (Exception e) {
            throw new TemplateRuntimeException(
                "Failed to create instance of " + clazz.getName() + ": " + e.getMessage(), e);
        }
    }

    /**
     * 查找构造函数
     */
    private static Constructor<?> findConstructor(Class<?> clazz, Object[] args) {
        Constructor<?>[] constructors = clazz.getConstructors();
        Constructor<?> bestMatch = null;
        int bestScore = Integer.MAX_VALUE;

        for (Constructor<?> constructor : constructors) {
            Class<?>[] paramTypes = constructor.getParameterTypes();

            if (paramTypes.length != args.length) {
                continue;
            }

            int score = calculateMatchScore(paramTypes, args);
            if (score >= 0 && score < bestScore) {
                bestMatch = constructor;
                bestScore = score;
            }
        }

        return bestMatch;
    }

    /**
     * 设置对象的属性值
     *
     * @param obj 对象
     * @param propertyName 属性名
     * @param value 属性值
     */
    public static void setProperty(Object obj, String propertyName, Object value) {
        if (obj == null) {
            throw new TemplateRuntimeException("Cannot set property '" + propertyName + "' of null");
        }

        // 尝试 setter 方法
        Method setter = findSetter(obj.getClass(), propertyName, value);
        if (setter != null) {
            try {
                if (!setter.isAccessible()) {
                    setter.setAccessible(true);
                }
                setter.invoke(obj, value);
                return;
            } catch (Exception e) {
                throw new TemplateRuntimeException(
                    "Failed to invoke setter '" + setter.getName() + "': " + e.getMessage(), e);
            }
        }

        // 尝试直接设置字段
        try {
            Field field = findFieldCached(obj.getClass(), propertyName);
            if (!field.isAccessible()) {
                field.setAccessible(true);
            }
            field.set(obj, value);
        } catch (Exception e) {
            throw new TemplateRuntimeException(
                "Failed to set property '" + propertyName + "' on " + obj.getClass().getName() + ": " + e.getMessage(), e);
        }
    }

    /**
     * 查找 setter 方法
     */
    private static Method findSetter(Class<?> clazz, String propertyName, Object value) {
        String setterName = "set" + capitalize(propertyName);

        // If value is null, try to find any setter with that name
        if (value == null) {
            for (Method method : clazz.getMethods()) {
                if (method.getName().equals(setterName) &&
                    method.getParameterCount() == 1) {
                    return method;
                }
            }
            return null;
        }

        // Try to find setter with matching parameter type
        Class<?> valueType = value.getClass();
        try {
            return clazz.getMethod(setterName, valueType);
        } catch (NoSuchMethodException e) {
            // Try with supertypes or primitive types
            for (Method method : clazz.getMethods()) {
                if (method.getName().equals(setterName) &&
                    method.getParameterCount() == 1) {
                    Class<?> paramType = method.getParameterTypes()[0];
                    // Check if parameter type matches or is assignable from value type
                    if (paramType.isAssignableFrom(valueType)) {
                        return method;
                    }
                    // Handle primitive type to wrapper type conversion
                    if (paramType.isPrimitive() && isWrapperForPrimitive(valueType, paramType)) {
                        return method;
                    }
                }
            }
        }

        return null;
    }

    /**
     * Check if a wrapper type corresponds to a primitive type
     */
    private static boolean isWrapperForPrimitive(Class<?> wrapperType, Class<?> primitiveType) {
        if (primitiveType == int.class) return wrapperType == Integer.class;
        if (primitiveType == long.class) return wrapperType == Long.class;
        if (primitiveType == double.class) return wrapperType == Double.class;
        if (primitiveType == float.class) return wrapperType == Float.class;
        if (primitiveType == boolean.class) return wrapperType == Boolean.class;
        if (primitiveType == byte.class) return wrapperType == Byte.class;
        if (primitiveType == short.class) return wrapperType == Short.class;
        if (primitiveType == char.class) return wrapperType == Character.class;
        return false;
    }

    /**
     * 查找getter方法（带缓存）
     *
     * @param clazz 类
     * @param propertyName 属性名
     * @return getter方法，如果不存在返回null
     */
    private static Method findGetterCached(Class<?> clazz, String propertyName) {
        String cacheKey = clazz.getName() + "::" + propertyName;
        Method cached = getterCache.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        Method getter = findGetter(clazz, propertyName);
        if (getter != null) {
            getterCache.put(cacheKey, getter);
        }
        return getter;
    }

    /**
     * 查找getter方法
     *
     * @param clazz 类
     * @param propertyName 属性名
     * @return getter方法，如果不存在返回null
     */
    private static Method findGetter(Class<?> clazz, String propertyName) {
        // 尝试getXxx形式
        String getterName = "get" + capitalize(propertyName);
        try {
            return clazz.getMethod(getterName);
        } catch (NoSuchMethodException e) {
            // 继续尝试其他形式
        }

        // 尝试isXxx形式（用于boolean属性）
        String isGetterName = "is" + capitalize(propertyName);
        try {
            Method method = clazz.getMethod(isGetterName);
            if (method.getReturnType() == boolean.class || method.getReturnType() == Boolean.class) {
                return method;
            }
        } catch (NoSuchMethodException e) {
            // 继续尝试其他形式
        }

        return null;
    }

    /**
     * 查找字段（带缓存）
     *
     * @param clazz 类
     * @param fieldName 字段名
     * @return 字段，如果不存在抛出 NoSuchFieldException
     * @throws NoSuchFieldException 如果字段不存在
     */
    private static Field findFieldCached(Class<?> clazz, String fieldName) throws NoSuchFieldException {
        String cacheKey = clazz.getName() + "::" + fieldName;
        Field cached = fieldCache.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        Field field = clazz.getField(fieldName);
        fieldCache.put(cacheKey, field);
        return field;
    }

    /**
     * 查找方法（带缓存）
     * <p>
     * 支持方法重载匹配
     * </p>
     *
     * @param clazz 类
     * @param methodName 方法名
     * @param args 参数数组
     * @return 方法，如果不存在返回null
     */
    private static Method findMethodCached(Class<?> clazz, String methodName, Object[] args) {
        // 构建缓存键：className::methodName::paramType1,paramType2,...
        StringBuilder cacheKeyBuilder = new StringBuilder();
        cacheKeyBuilder.append(clazz.getName()).append("::").append(methodName).append("::");
        for (int i = 0; i < args.length; i++) {
            if (i > 0) {
                cacheKeyBuilder.append(",");
            }
            if (args[i] != null) {
                cacheKeyBuilder.append(args[i].getClass().getName());
            } else {
                cacheKeyBuilder.append("null");
            }
        }
        String cacheKey = cacheKeyBuilder.toString();

        Method cached = methodCache.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        Method method = findMethod(clazz, methodName, args);
        if (method != null) {
            methodCache.put(cacheKey, method);
        }
        return method;
    }

    /**
     * 查找方法
     * <p>
     * 支持方法重载匹配
     * </p>
     *
     * @param clazz 类
     * @param methodName 方法名
     * @param args 参数数组
     * @return 方法，如果不存在返回null
     */
    private static Method findMethod(Class<?> clazz, String methodName, Object[] args) {
        Method[] methods = clazz.getMethods();
        Method bestMatch = null;
        int bestScore = Integer.MAX_VALUE;

        for (Method method : methods) {
            if (!method.getName().equals(methodName)) {
                continue;
            }

            Class<?>[] paramTypes = method.getParameterTypes();

            // 参数数量不匹配
            if (paramTypes.length != args.length) {
                // 检查是否是可变参数方法
                if (method.isVarArgs() && args.length >= paramTypes.length - 1) {
                    // 可变参数匹配，暂时跳过详细匹配
                    if (bestMatch == null) {
                        bestMatch = method;
                    }
                }
                continue;
            }

            // 计算类型匹配度
            int score = calculateMatchScore(paramTypes, args);
            if (score >= 0 && score < bestScore) {
                bestMatch = method;
                bestScore = score;
            }
        }

        return bestMatch;
    }

    /**
     * 计算参数匹配度
     * <p>
     * 返回值越小表示匹配度越高
     * </p>
     *
     * @param paramTypes 参数类型数组
     * @param args 参数值数组
     * @return 匹配度分数，如果不匹配返回-1
     */
    private static int calculateMatchScore(Class<?>[] paramTypes, Object[] args) {
        int score = 0;

        for (int i = 0; i < paramTypes.length; i++) {
            if (args[i] == null) {
                // null可以匹配任何引用类型
                if (paramTypes[i].isPrimitive()) {
                    return -1; // 不匹配
                }
                score += 1;
                continue;
            }

            Class<?> argType = args[i].getClass();

            // 精确匹配
            if (paramTypes[i].equals(argType)) {
                score += 0;
            }
            // 可赋值（向上转型）
            else if (paramTypes[i].isAssignableFrom(argType)) {
                score += 1;
            }
            // 基本类型转换 - 使用精细化评分
            else {
                int primitiveScore = getPrimitiveMatchScore(paramTypes[i], argType);
                if (primitiveScore > 0) {
                    score += primitiveScore;
                }
                // 不匹配
                else {
                    return -1;
                }
            }
        }

        return score;
    }

    /**
     * 获取基本类型转换的评分
     * <p>
     * 返回值越小表示匹配度越高：
     * - 2: 直接的 primitive-wrapper 对应 (如 int ↔ Integer)
     * - 3: 数字类型拓宽转换 (如 Integer → double)
     * - 0: 不匹配
     * </p>
     *
     * @param paramType 参数类型
     * @param argType 参数值类型
     * @return 匹配度分数，如果不匹配返回0
     */
    private static int getPrimitiveMatchScore(Class<?> paramType, Class<?> argType) {
        // 基本类型与包装类型的直接对应关系 - 最高优先级
        if (paramType == int.class && argType == Integer.class) return 2;
        if (paramType == long.class && argType == Long.class) return 2;
        if (paramType == double.class && argType == Double.class) return 2;
        if (paramType == float.class && argType == Float.class) return 2;
        if (paramType == boolean.class && argType == Boolean.class) return 2;
        if (paramType == char.class && argType == Character.class) return 2;
        if (paramType == byte.class && argType == Byte.class) return 2;
        if (paramType == short.class && argType == Short.class) return 2;

        // 反向匹配 - 包装类型接受基本类型
        if (paramType == Integer.class && argType == int.class) return 2;
        if (paramType == Long.class && argType == long.class) return 2;
        if (paramType == Double.class && argType == double.class) return 2;
        if (paramType == Float.class && argType == float.class) return 2;
        if (paramType == Boolean.class && argType == boolean.class) return 2;
        if (paramType == Character.class && argType == char.class) return 2;
        if (paramType == Byte.class && argType == byte.class) return 2;
        if (paramType == Short.class && argType == short.class) return 2;

        // 数字类型之间的拓宽转换 - 较低优先级
        // 例如: Integer → double, Integer → long 等
        if (isNumberType(paramType) && isNumberType(argType)) {
            return 3;
        }

        return 0; // 不匹配
    }

    /**
     * 判断类型是否是数字类型
     *
     * @param type 类型
     * @return 如果是数字类型返回true，否则返回false
     */
    private static boolean isNumberType(Class<?> type) {
        return type == int.class || type == Integer.class
                || type == long.class || type == Long.class
                || type == double.class || type == Double.class
                || type == float.class || type == Float.class
                || type == byte.class || type == Byte.class
                || type == short.class || type == Short.class;
    }

    /**
     * 将对象转换为整数
     *
     * @param value 对象
     * @return 整数值
     */
    private static int toInt(Object value) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value instanceof String) {
            return Integer.parseInt((String) value);
        }
        throw new TemplateRuntimeException("Cannot convert " + value + " to int");
    }

    /**
     * 将字符串首字母大写
     *
     * @param str 字符串
     * @return 首字母大写的字符串
     */
    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }
}
