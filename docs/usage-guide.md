# EasyTL 使用指南

## 目录

1. [快速开始](#快速开始)
2. [基本用法](#基本用法)
   - [1. 变量替换](#1-变量替换)
   - [2. 表达式计算](#2-表达式计算)
   - [3. 条件语句](#3-条件语句)
   - [4. 循环语句](#4-循环语句)
   - [5. 区间运算](#5-区间运算)
   - [6. Switch 语句](#6-switch-语句)
3. [高级用法](#高级用法)
   - [1. 模板继承 (extends)](#1-模板继承-extends)
   - [2. 模板导入 (import)](#2-模板导入-import)
   - [3. 空安全运算符](#3-空安全运算符)
   - [4. 变量声明 (let)](#4-变量声明-let)
   - [5. 正则表达式](#5-正则表达式)
   - [6. 配置选项](#6-配置选项)
   - [7. 模板加载器](#7-模板加载器)
   - [8. 参数访问](#8-参数访问)

---

## 快速开始

### 引入依赖

Maven 依赖：

```xml
<dependency>
    <groupId>org.dromara</groupId>
    <artifactId>easytl</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 第一个模板

```java
import org.dromara.easytl.TemplateEngine;
import org.dromara.easytl.Template;
import org.dromara.easytl.Context;

public class HelloWorld {
    public static void main(String[] args) {
        // 创建模板引擎
        TemplateEngine engine = new TemplateEngine();

        // 编译模板
        Template template = engine.compile("Hello, {name}!");

        // 创建上下文并渲染
        Context context = new Context();
        context.put("name", "World");

        // 输出结果
        String result = template.render(context);
        System.out.println(result);  // 输出: Hello, World!
    }
}
```

---

## 基本用法

### 1. 变量替换

```java
// 简单变量
Template template = engine.compile("姓名: {name}");
context.put("name", "张三");
template.render(context);  // 姓名: 张三

// 对象属性
context.put("user", new User("张三", 25));
template = engine.compile("姓名: {user.name}, 年龄: {user.age}");
template.render(context);  // 姓名: 张三, 年龄: 25

// Map 访问
Map<String, Object> data = new HashMap<>();
data.put("name", "李四");
context.put("data", data);
template = engine.compile("姓名: {data.name}");
template.render(context);  // 姓名: 李四
```

### 2. 表达式计算

```java
// 算术运算
engine.compile("{a + b}");           // 加法
engine.compile("{a - b}");           // 减法
engine.compile("{a * b}");           // 乘法
engine.compile("{a / b}");           // 除法
engine.compile("{a % b}");           // 取模

// 比较运算
engine.compile("{a > b}");           // 大于
engine.compile("{a < b}");           // 小于
engine.compile("{a >= b}");          // 大于等于
engine.compile("{a <= b}");          // 小于等于
engine.compile("{a == b}");          // 等于
engine.compile("{a != b}");          // 不等于

// 逻辑运算
engine.compile("{a && b}");          // 逻辑与
engine.compile("{a || b}");          // 逻辑或
engine.compile("{!a}");              // 逻辑非
```

### 3. 条件语句

```java
// if 语句块
engine.compile("{% if score >= 60 }及格{% else }不及格{/% if }");

// if-elseif-else
engine.compile("{% if score >= 90 }优秀{% else if score >= 60 %}良好{% else %}需要努力{/% if }");

// 三元表达式
engine.compile("{status ? '启用' : '禁用'}");
```

模板文件语法示例（.etl）：
```etl
{% if flag }
Yes
{% else }
No
{/% if }
```

### 4. 循环语句

```java
// for 循环 - 遍历列表
context.put("items", Arrays.asList("A", "B", "C"));
engine.compile("{% for item in items }{item}{/% for }");  // ABC

// for 循环 - 遍历 Map
Map<String, Object> map = new LinkedHashMap<>();
map.put("a", 1);
map.put("b", 2);
context.put("data", map);
engine.compile("{% for key, value in data }{key}={value}{/% for }");  // a=1b=2

// for 循环 - 数字范围
engine.compile("{% for i in 1..5 }{i}{/% for }");  // 12345

// for 循环 - 带索引
engine.compile("{% for i, item in items }[{i}]{item}{/% for }");
```

模板文件语法示例（.etl）：
```etl
{% for item in items }
- {item}
{/% for }
```

### 5. 区间运算

```java
// 区间包含判断
engine.compile("{3 in 1..5}");        // true (闭区间: 包括两端)
engine.compile("{6 in 1..5}");        // false
engine.compile("{3 in 1..<5}");       // false (左闭右开: 不包括右端)
engine.compile("{5 in 1..<5}");       // true

// 区间类型
// a..b    - 闭区间 [a, b]
// a..<b   - 左闭右开 [a, b)
// a>..b   - 左开右闭 (a, b]
// a>..<b  - 开区间 (a, b)
engine.compile("{3 in 1..5}");        // true  (闭区间)
engine.compile("{3 in 1..<5}");       // false (左闭右开)
```

### 6. Switch 语句

```java
// switch 语句块
engine.compile("{% switch status }{% case 1 }待处理{% case 2 }处理中{% case 3 }已完成{% default }未知{/% switch }");
```

模板文件语法示例：
```etl
{% switch status }
{% case 1 }Pending
{% case 2 }Processing
{% case 3 }Completed
{% default }Unknown
{/% switch }
```

---

## 高级用法

### 1. 模板继承 (extends)

基础模板 `base.etl`:
```etl
<html>
<head>
    <title>默认标题</title>
</head>
<body>
    Base Content
</body>
</html>
```

子模板 `page.etl`:
```etl
{% extends 'base.etl' %}
Custom Content
```

渲染:
```java
TemplateEngineConfig config = new TemplateEngineConfig();
config.setTemplateBasePath("/templates");
TemplateEngine engine = new TemplateEngine(config);

Template template = engine.compile("{% extends 'base.etl' %}Custom Content");
```

### 2. 模板导入 (import)

```java
// 导入 Java 类（用于静态方法调用）
engine.compile("{% import java.util.List %}Hello");
```

模板文件语法示例（导入 Java 类）：
```etl
{% import java.util.List %}
Using List: {List.class}
```

模板文件语法示例（导入模板文件）：
```etl
{% import 'common.etl' as common %}
Using imported: {common.header()}
```

### 3. 空安全运算符

```java
// Elvis 运算符
engine.compile("{name ?? '默认值'}");  // name 为 null 时使用默认值

// 空值检查
engine.compile("{user?.name}");      // user 为 null 时返回 null
```

### 4. 变量声明 (let)

```java
// 变量声明
engine.compile("{% let name = 'John' %}{{ name }}");

// 多个变量声明（使用分号分隔）
engine.compile("{% let a = 1; let b = 2; let c = a + b %}{{ c }}");

// 使用表达式赋值
engine.compile("{% let greeting = 'Hello, ' + name %}{{ greeting }}");
```

模板文件语法示例：
```etl
{% let name = 'John' %}
{% let age = 25 %}
Name: {name}, Age: {age}
```

### 5. 正则表达式

```java
// 正则匹配
engine.compile("{email =~ '\\w+@\\w+\\.\\w+'}");   // 返回 true 或 false

// 正则不匹配
engine.compile("{email !~ '\\w+@\\w+\\.\\w+'}");  // 返回 true 或 false
```

### 6. 配置选项

```java
TemplateEngineConfig config = new TemplateEngineConfig();

// 花括号语法
config.setBraceSyntax(TemplateEngineConfig.BraceSyntax.SINGLE);   // {expr}
config.setBraceSyntax(TemplateEngineConfig.BraceSyntax.DOUBLE);  // {{expr}}
config.setBraceSyntax(TemplateEngineConfig.BraceSyntax.BOTH);    // 两者都支持

// 严格模式
config.setStrictMode(true);  // 访问不存在的变量抛出异常

// 缓存配置
config.setCacheEnabled(true);   // 启用模板缓存
config.setMaxCacheSize(100);    // 最大缓存数量

// 字符编码
config.setCharset(StandardCharsets.UTF_8);

// 模板基础路径
config.setTemplateBasePath("/templates");

TemplateEngine engine = new TemplateEngine(config);
```

### 7. 模板加载器

```java
// 文件模板加载器
FileTemplateLoader loader = new FileTemplateLoader("/templates", StandardCharsets.UTF_8);
TemplateEngine engine = new TemplateEngine(config, loader);

// Classpath 模板加载器
ClasspathTemplateLoader loader = new ClasspathTemplateLoader(StandardCharsets.UTF_8);
TemplateEngine engine = new TemplateEngine(config, loader);
```

### 8. 参数访问

```java
// 通过索引访问参数
context.setArguments("Alice", "Bob");
engine.compile("参数0: {0}, 参数1: {1}");

// 默认参数名 $：等价 {$0}，引用第一个参数
engine.compile("你好，{$}！");

// 渲染时直接传入参数数组
template.render(context);
```
