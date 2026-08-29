# Context importClass 功能设计

## 概述

在 Context 中添加 `importClass` 方法，支持通过 Java API 和模板语法导入 Java 类，使模板能够访问类的静态方法、静态字段，以及创建类的实例。

## 功能需求

### 1. Context API 导入类

```java
Context ctx = new Context();
ctx.importClass(Math.class);              // 使用简单名称 "Math" 作为别名
ctx.importClass(ArrayList.class, "List"); // 使用指定别名 "List"
```

### 2. 模板语法导入类

```
{% import java.util.List %}              // 别名自动为 "List"
{% import java.util.List as ArrayList %} // 别名为 "ArrayList"
```

### 3. 使用导入的类

导入的类支持以下操作：

- **静态字段访问**: `{Math.PI}`, `{Integer.MAX_VALUE}`
- **静态方法调用**: `{Math.sqrt(16)}`, `{Integer.parseInt("123")}`
- **创建实例（三种语法）**:
  - 类名作为函数调用: `{ArrayList()}`, `{Person("Peter")}`
  - 无参构造 + 属性块: `Person {name: 'Peter', age: 18}`
  - 有参构造 + 属性块: `{Person("Peter") {age: 18, gender: 'M'}}`

### 4. 属性块设置规则

- **普通对象**: 属性块的键值对转化为 setter 方法调用
  - `{name: 'Peter'}` → `setName('Peter')`
- **Map 接口及其实现类**: 属性块的键值对作为 Map 的键值对
  - `{name: 'Peter', age: 18}` → `HashMap` 的两个键值对

## 设计细节

### 第一部分：Context API

在 `Context.java` 中添加两个方法：

```java
/**
 * 导入Java类到上下文，使用类的简单名称作为别名
 *
 * @param clazz 要导入的Java类
 */
public void importClass(Class<?> clazz) {
    String alias = clazz.getSimpleName();
    variables.put(alias, clazz);
}

/**
 * 导入Java类到上下文，使用指定别名
 *
 * @param clazz 要导入的Java类
 * @param alias 别名
 */
public void importClass(Class<?> clazz, String alias) {
    variables.put(alias, clazz);
}
```

存储方式：直接将 `Class<?>` 对象存入 `ConcurrentHashMap<String, Object> variables`。

### 第二部分：ImportStatement AST 和 Parser 语法

#### ImportStatement 改动

```java
public class ImportStatement extends ASTNode {

    private final String className;
    private final String alias;  // 新增：别名（可为null）

    public ImportStatement(String className, String alias) {
        this.className = className;
        this.alias = alias;
    }

    public ImportStatement(String className) {
        this(className, null);
    }

    public String getAlias() {
        return alias;
    }
}
```

#### Parser.parseImportStatement() 改动

```java
private ASTNode parseImportStatement() {
    expect(TokenType.IMPORT);

    StringBuilder className = new StringBuilder();
    className.append(expect(TokenType.IDENTIFIER).getValue());

    while (check(TokenType.DOT)) {
        advance();
        className.append(".");
        className.append(expect(TokenType.IDENTIFIER).getValue());
    }

    // 新增：解析可选的 as alias
    String alias = null;
    if (check(TokenType.AS)) {
        advance();
        alias = expect(TokenType.IDENTIFIER).getValue();
    }

    expect(TokenType.STMT_END);
    return new ImportStatement(className.toString(), alias);
}
```

### 第三部分：Evaluator 实现

```java
@Override
public Object visitImportStatement(ImportStatement node) {
    String className = node.getClassName();
    String alias = node.getAlias();

    try {
        Class<?> clazz = Class.forName(className);

        if (alias == null) {
            alias = clazz.getSimpleName();
        }

        context.put(alias, clazz);
        return null;
    } catch (ClassNotFoundException e) {
        throw new TemplateRuntimeException(
            "Cannot import class '" + className + "': class not found", e);
    }
}
```

### 第四部分：ReflectionUtils 扩展

#### 静态字段访问

```java
public static Object getProperty(Object obj, String propertyName) {
    if (obj == null) {
        throw new TemplateRuntimeException("Cannot get property '" + propertyName + "' of null");
    }

    // 新增：如果 obj 是 Class<?>，尝试获取静态字段
    if (obj instanceof Class) {
        Class<?> clazz = (Class<?>) obj;
        return getStaticProperty(clazz, propertyName);
    }

    // 原有逻辑...
}

private static Object getStaticProperty(Class<?> clazz, String propertyName) {
    // 1. 尝试静态 getter 方法
    // 2. 尝试静态字段
    // 3. 抛出异常
}
```

#### 静态方法调用

```java
public static Object invokeMethod(Object obj, String methodName, Object[] args) {
    if (obj == null) {
        throw new TemplateRuntimeException("Cannot invoke method '" + methodName + "' on null");
    }

    // 新增：如果 obj 是 Class<?>，尝试调用静态方法
    if (obj instanceof Class) {
        Class<?> clazz = (Class<?>) obj;
        return invokeStaticMethod(clazz, methodName, args);
    }

    // 原有逻辑...
}

private static Object invokeStaticMethod(Class<?> clazz, String methodName, Object[] args) {
    // 查找并调用静态方法
}
```

#### 构造实例

```java
public static Object createInstance(Class<?> clazz, Object[] args) {
    Constructor<?> constructor = findConstructorCached(clazz, args);

    if (constructor == null) {
        throw new TemplateRuntimeException(
            "No suitable constructor found in " + clazz.getName());
    }

    return constructor.newInstance(args);
}
```

#### 设置属性

```java
public static void setProperty(Object obj, String propertyName, Object value) {
    // 1. 尝试 setter 方法
    // 2. 尝试直接设置字段
}
```

### 第五部分：类构造函数调用语法

#### MethodCall 处理类构造

```java
@Override
public Object visitMethodCall(MethodCall node) {
    Object target = node.getTarget().accept(this);

    // 如果 target 是 Class<?>，调用构造函数
    if (target instanceof Class) {
        Object[] args = evaluateArguments(node.getArguments());
        return ReflectionUtils.createInstance((Class<?>) target, args);
    }

    // 原有逻辑：普通方法调用
    Object[] args = evaluateArguments(node.getArguments());
    return ReflectionUtils.invokeMethod(target, node.getMethodName(), args);
}
```

#### 新增 ObjectConstruction AST 节点

```java
public class ObjectConstruction extends ASTNode {

    private final ASTNode targetClass;
    private final HashLiteral propertiesBlock;

    public ObjectConstruction(ASTNode targetClass, HashLiteral propertiesBlock) {
        this.targetClass = targetClass;
        this.propertiesBlock = propertiesBlock;
    }

    public ASTNode getTargetClass() {
        return targetClass;
    }

    public HashLiteral getPropertiesBlock() {
        return propertiesBlock;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitObjectConstruction(this);
    }
}
```

#### Parser 解析 ObjectConstruction

在 `parsePostfix()` 中识别：

- `Identifier + LEFT_BRACE` → 无参构造 + 属性块
- `MethodCall + LEFT_BRACE` → 有参构造 + 属性块

#### Evaluator 处理 ObjectConstruction

```java
@Override
public Object visitObjectConstruction(ObjectConstruction node) {
    ASTNode targetNode = node.getTargetClass();

    // 获取或创建实例
    Object target;
    if (targetNode instanceof MethodCall) {
        target = visitMethodCall((MethodCall) targetNode);
    } else {
        Object classOrInstance = targetNode.accept(this);
        if (classOrInstance instanceof Class) {
            target = ReflectionUtils.createInstance((Class<?>) classOrInstance, new Object[0]);
        } else {
            target = classOrInstance;
        }
    }

    // 设置属性
    for (Map.Entry<ASTNode, ASTNode> entry : properties.getEntries()) {
        String propName = getPropertyName(entry.getKey());
        Object propValue = entry.getValue().accept(this);

        if (target instanceof Map) {
            ((Map) target).put(propName, propValue);
        } else {
            ReflectionUtils.setProperty(target, propName, propValue);
        }
    }

    return target;
}
```

### 第六部分：错误处理和边界情况

#### 错误类型

1. **类加载失败**: `ClassNotFoundException`
2. **静态属性不存在**: `TemplateRuntimeException`
3. **静态方法不存在**: `TemplateRuntimeException`
4. **构造函数不存在**: `TemplateRuntimeException`
5. **Setter 方法不存在**: `TemplateRuntimeException`

#### 边界情况

1. **导入类名冲突**: 后导入覆盖之前的，符合现有 Context 行为
2. **内部类导入**: 支持 `{% import outer.inner %}`，别名取简单名称
3. **数组类型**: 不支持导入数组类型
4. **原始类型**: 不支持导入原始类型
5. **Map 特殊处理**: Map 实现类的属性块作为键值对
6. **空属性块**: `{Person {}}` 创建实例但不设置属性

## 改动文件列表

| 文件 | 改动类型 | 说明 |
|------|----------|------|
| `Context.java` | 新增方法 | `importClass(Class<?>)` 和 `importClass(Class<?>, String)` |
| `ImportStatement.java` | 新增字段 | `alias` 字段和对应构造函数、getter |
| `Parser.java` | 修改方法 | `parseImportStatement()` 支持 `as alias`；`parsePostfix()` 支持 ObjectConstruction |
| `ASTVisitor.java` | 新增方法 | `visitObjectConstruction()` |
| `ObjectConstruction.java` | 新增文件 | 类构造 + 属性块的 AST 节点 |
| `Evaluator.java` | 实现/修改 | `visitImportStatement()`、`visitMethodCall()`、`visitObjectConstruction()` |
| `ReflectionUtils.java` | 新增方法 | `getStaticProperty()`、`invokeStaticMethod()`、`createInstance()`、`setProperty()` |

## 使用示例

### Java API 使用

```java
Context ctx = new Context();
ctx.importClass(Math.class);
ctx.importClass(ArrayList.class);
ctx.importClass(Person.class);

Template template = engine.compile("{Math.sqrt(16)}");
// 输出: 4.0

Template template2 = engine.compile("{ArrayList().add('item')}");
// 创建 ArrayList 并添加元素

Template template3 = engine.compile("{Person('Peter').getName()}");
// 创建 Person 实例并调用方法
```

### 模板语法使用

```
{% import java.lang.Math %}
{% import java.util.HashMap as Map %}
{% import com.example.Person %}

{Math.PI}
{Map {'name': 'Peter', 'age': 18}}
{Person('Peter') {age: 18}}
```

## 测试要点

1. **Context API 测试**
   - `importClass` 使用简单名称作为别名
   - `importClass` 使用指定别名
   - 类名冲突时的覆盖行为

2. **模板语法测试**
   - `{% import className %}` 无别名语法
   - `{% import className as alias %}` 有别名语法
   - 内部类导入
   - 类不存在时的错误处理

3. **静态访问测试**
   - 静态字段访问
   - 静态方法调用（无参、有参）
   - 静态属性不存在时的错误处理

4. **实例创建测试**
   - 无参构造函数
   - 有参构造函数
   - 构造函数不存在时的错误处理

5. **属性块测试**
   - 普通对象的 setter 调用
   - Map 的键值对设置
   - 空 Map 实现类
   - 属性不存在时的错误处理

6. **综合测试**
   - 链式调用 `{Person('Peter').setName('John').getName()}`
   - 嵌套对象创建