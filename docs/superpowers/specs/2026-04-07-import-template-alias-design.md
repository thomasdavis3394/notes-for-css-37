# Import Template Statement Alias 功能设计

## 概述

在 EasyTL 模板引擎中引入模板导入功能，支持通过别名导入其他模板文件，并访问该模板导出的变量和函数。

## 功能需求

### 1. Import Template 语法

```
{% import 'path/to/template.etl' as alias %}
```

- 路径必须是字符串字面量（单引号或双引号）
- `as alias` 必须指定别名
- 路径相对于当前模板文件解析

### 2. Export 语法

两种形式：

```
{% export let add = (a, b) => a + b %}   // 声明并导出
```

```
{% let a = 100 %}
{% export a %}                            // 先声明后导出
```

- `export` 只能在模板顶层使用
- 每个变量只能 export 一次（重复 export 报错）
- export 语句不产生输出文本

### 3. 使用导入的模板

```etl
{% import 'utils.etl' as utils %}

// 调用导出的函数
{utils.add(1, 2)}

// 访问导出的变量
{utils.config}

// 渲染模板文本（ctx 可选）
{utils.render()}
{utils.render(otherCtx)}
```

### 4. 模板函数定义

模板中的函数通过 Lambda 表达式定义：

```
{% export let add = (a, b) => a + b %}
{% export let multiply = (x, y) => x * y %}
```

## 设计细节

### 第一部分：AST 节点

#### ImportTemplateStatement

```java
public class ImportTemplateStatement extends ASTNode {
    private final String templatePath;   // 模板文件路径
    private final String alias;          // 别名（必须指定）
}
```

#### ExportStatement

```java
public class ExportStatement extends ASTNode {
    private final String variableName;   // 要导出的变量名
    private final LetStatement letStatement;  // 可选：声明并导出时的 LetStatement

    // 如果 letStatement != null，表示 {% export let x = value %}
    // 如果 letStatement == null，表示 {% export x %}
}
```

#### ASTVisitor 新增方法

```java
T visitImportTemplateStatement(ImportTemplateStatement node);
T visitExportStatement(ExportStatement node);
```

### 第二部分：Runtime 对象

#### ImportedTemplateObject

```java
public class ImportedTemplateObject {
    private final TemplateNode templateAst;     // 编译后的模板 AST
    private final TemplateLoader templateLoader; // 用于路径解析
    private final Context definingContext;     // import 语句所在模板的上下文
    private final String sourceText;           // 源文本（用于错误报告）
    private final String templatePath;         // 模板路径（用于循环导入检测）

    private volatile Map<String, Object> exportedValues; // 缓存的 export 变量（懒加载）
    private volatile boolean executed;         // 是否已执行

    // 成员访问：xxx.add() 或 xxx.someVar
    public Object getMember(String name);

    // 方法调用：xxx.add(1, 2) 或 xxx.render(ctx)
    public Object invokeMethod(String name, Object[] args);

    // 渲染模板文本
    public String render(Context ctx);

    // 确保模板已执行（懒加载）
    private void ensureExecuted();
}
```

#### ExportRegistry

```java
public class ExportRegistry {
    private final Set<String> exportedNames = new HashSet<>();

    // 注册 export 变量，重复时报错
    public void register(String name);

    // 获取所有已导出的变量名
    public Set<String> getExportedNames();

    // 检查是否已导出
    public boolean isExported(String name);
}
```

### 第三部分：Parser 解析逻辑

#### 解析 ImportTemplateStatement

在 `parseImportStatement()` 中，识别 `import` 后检查下一个 token：

- 如果是 `STRING` → 解析为 ImportTemplateStatement（模板文件导入）
- 如果是 `IDENTIFIER` → 解析为 ImportStatement（Java 类导入，现有逻辑）

```java
private ASTNode parseImportStatement() {
    expect(TokenType.IMPORT);

    if (check(TokenType.STRING)) {
        // 模板文件导入
        String path = advance().getValue();
        expect(TokenType.AS);
        String alias = expect(TokenType.IDENTIFIER).getValue();
        expect(TokenType.STMT_END);
        return new ImportTemplateStatement(path, alias);
    } else {
        // Java 类导入（现有逻辑）
        StringBuilder className = new StringBuilder();
        className.append(expect(TokenType.IDENTIFIER).getValue());
        while (check(TokenType.DOT)) {
            advance();
            className.append(".");
            className.append(expect(TokenType.IDENTIFIER).getValue());
        }
        String alias = null;
        if (check(TokenType.AS)) {
            advance();
            alias = expect(TokenType.IDENTIFIER).getValue();
        }
        expect(TokenType.STMT_END);
        return new ImportStatement(className.toString(), alias);
    }
}
```

#### 解析 ExportStatement

```java
private ASTNode parseExportStatement() {
    expect(TokenType.EXPORT);

    if (check(TokenType.LET)) {
        // {% export let x = value %}
        LetStatement letStmt = parseLetStatement();
        return new ExportStatement(letStmt.getVariableName(), letStmt);
    } else {
        // {% export x %}
        String varName = expect(TokenType.IDENTIFIER).getValue();
        expect(TokenType.STMT_END);
        return new ExportStatement(varName, null);
    }
}
```

#### TokenType 新增

```java
EXPORT = ...;  // export 关键字
```

### 第四部分：编译阶段验证

#### ImportTemplateValidator

编译阶段验证 import template 语句：

```java
public class ImportTemplateValidator {
    /**
     * 验证所有 ImportTemplateStatement
     * - 模板文件是否存在
     * - 别名是否冲突
     * - 循环导入检测
     */
    public static void validate(
        TemplateNode ast,
        TemplateLoader loader,
        String currentTemplatePath,
        Set<String> definedAliases,
        Set<String> visitedTemplates
    );
}
```

验证内容：

1. **文件存在性**：通过 TemplateLoader 检查模板文件是否存在
2. **别名冲突**：检查 alias 是否与已定义变量或已导入别名冲突
3. **循环导入**：递归检查被导入模板的 import 语句，维护已访问模板集合

#### ExportValidator

验证 export 语句的正确性：

```java
public class ExportValidator {
    /**
     * 验证所有 ExportStatement
     * - 是否在顶层
     * - 是否重复 export
     */
    public static void validate(TemplateNode ast);
}
```

验证内容：

1. **顶层检查**：ExportStatement 只能是 TemplateNode 的直接子节点
2. **重复检查**：同一变量名不能 export 多次

#### 与现有验证器整合

在 `InterpreterCompiler.compile()` 中调用：

```java
// 现有验证
TemplateJavaImportValidator.validate(ast, templateJavaClassImportEnabled, sourceText);

// 新增验证
ImportTemplateValidator.validate(ast, templateLoader, currentTemplatePath, ...);
ExportValidator.validate(ast);
```

### 第五部分：Evaluator 运行时处理

#### visitImportTemplateStatement

```java
@Override
public Object visitImportTemplateStatement(ImportTemplateStatement node) {
    String templatePath = node.getTemplatePath();
    String alias = node.getAlias();

    if (templateLoader == null) {
        throw runtimeError("TemplateLoader is not configured. Cannot import template: " + templatePath, node);
    }

    // 加载并编译导入的模板
    String templateContent = templateLoader.load(templatePath);
    Lexer lexer = new Lexer(new StringSource(templateContent));
    TokenStream tokens = new TokenStream(lexer);
    Parser parser = new Parser(tokens);
    TemplateNode importedAst = parser.parse();

    // 创建 ImportedTemplateObject（懒加载，不立即执行）
    ImportedTemplateObject importedTemplate = new ImportedTemplateObject(
        importedAst,
        templateLoader,
        context,
        templateContent,
        templatePath
    );

    // 存入 Context
    context.put(alias, importedTemplate);

    return null;
}
```

#### visitExportStatement

```java
@Override
public Object visitExportStatement(ExportStatement node) {
    ExportRegistry registry = context.getExportRegistry();
    String varName = node.getVariableName();

    // 注册 export（重复会抛出异常）
    registry.register(varName);

    // 如果有 LetStatement，先执行 let
    if (node.getLetStatement() != null) {
        node.getLetStatement().accept(this);
    }

    return "";
}
```

#### MemberAccess 处理 ImportedTemplateObject

修改 `visitMemberAccess`，支持对 ImportedTemplateObject 的成员访问：

```java
@Override
public Object visitMemberAccess(MemberAccess node) {
    Object obj = node.getObject().accept(this);

    if (obj instanceof ImportedTemplateObject) {
        ImportedTemplateObject imported = (ImportedTemplateObject) obj;
        return imported.getMember(node.getMemberName());
    }

    // 现有逻辑...
}
```

#### MethodCall 处理 ImportedTemplateObject

修改 `visitMethodCall`，支持对 ImportedTemplateObject 的方法调用：

```java
@Override
public Object visitMethodCall(MethodCall node) {
    if (node.getObject() == null) {
        // 现有逻辑...
    }

    Object obj = node.getObject().accept(this);

    if (obj instanceof ImportedTemplateObject) {
        ImportedTemplateObject imported = (ImportedTemplateObject) obj;
        String methodName = node.getMethodName();
        Object[] args = evaluateArguments(node.getArguments());
        return imported.invokeMethod(methodName, args);
    }

    // 现有逻辑...
}
```

#### Context 新增 ExportRegistry

```java
public class Context {
    private ExportRegistry exportRegistry;

    public ExportRegistry getExportRegistry() {
        if (exportRegistry == null) {
            exportRegistry = new ExportRegistry();
        }
        return exportRegistry;
    }

    public void setExportRegistry(ExportRegistry registry) {
        this.exportRegistry = registry;
    }

    // 创建子上下文时共享 ExportRegistry
    public Context createChildContext() {
        Context child = new Context(this);
        child.exportRegistry = this.exportRegistry;
        return child;
    }
}
```

### 第六部分：ImportedTemplateObject 详细实现

#### getMember 方法

```java
public Object getMember(String name) {
    ensureExecuted();

    Object value = exportedValues.get(name);
    if (value == null && !exportedValues.containsKey(name)) {
        throw new TemplateRuntimeException(
            "Imported template '" + templatePath + "' does not export '" + name + "'");
    }
    return value;
}
```

#### invokeMethod 方法

```java
public Object invokeMethod(String name, Object[] args) {
    // 特殊方法：render
    if ("render".equals(name)) {
        Context ctx = args.length > 0 && args[0] instanceof Context
            ? (Context) args[0]
            : definingContext;
        return render(ctx);
    }

    // 普通 export 函数调用
    ensureExecuted();
    Object value = exportedValues.get(name);

    if (value == null && !exportedValues.containsKey(name)) {
        throw new TemplateRuntimeException(
            "Imported template '" + templatePath + "' does not export '" + name + "'");
    }

    if (value instanceof LambdaObject) {
        return ((LambdaObject) value).invoke(args);
    }

    // 不是 Lambda，尝试调用该对象的同名方法
    return ReflectionUtils.invokeMethod(value, name, args);
}
```

#### render 方法

```java
public String render(Context ctx) {
    Context renderContext = ctx.createChildContext();
    renderContext.setExportRegistry(new ExportRegistry());

    Evaluator evaluator = new Evaluator(renderContext, templateLoader, sourceText, true);
    Object result = templateAst.accept(evaluator);
    return result != null ? result.toString() : "";
}
```

#### ensureExecuted 方法（懒加载与缓存）

```java
private void ensureExecuted() {
    if (executed) {
        return;
    }

    synchronized (this) {
        if (executed) {
            return;
        }

        // 循环导入检测
        String templateKey = templatePath;
        if (ExecutionTracker.isExecuting(templateKey)) {
            // 循环导入，跳过，返回空缓存
            exportedValues = new HashMap<>();
            executed = true;
            return;
        }

        ExecutionTracker.beginExecution(templateKey);
        try {
            Context execContext = definingContext.createChildContext();
            execContext.setExportRegistry(new ExportRegistry());

            Evaluator evaluator = new Evaluator(execContext, templateLoader, sourceText, true);
            templateAst.accept(evaluator);

            exportedValues = new HashMap<>();
            for (String name : execContext.getExportRegistry().getExportedNames()) {
                exportedValues.put(name, execContext.get(name));
            }

            executed = true;
        } finally {
            ExecutionTracker.endExecution(templateKey);
        }
    }
}
```

#### ExecutionTracker（循环导入检测）

```java
public class ExecutionTracker {
    private static final ThreadLocal<Set<String>> executingTemplates = ThreadLocal.withInitial(HashSet::new);

    public static boolean isExecuting(String templateKey) {
        return executingTemplates.get().contains(templateKey);
    }

    public static void beginExecution(String templateKey) {
        executingTemplates.get().add(templateKey);
    }

    public static void endExecution(String templateKey) {
        executingTemplates.get().remove(templateKey);
    }
}
```

### 第七部分：错误处理与边界情况

#### 错误类型

| 错误场景 | 报错阶段 | 错误类型 |
|---------|---------|---------|
| 模板文件不存在 | 编译时 | TemplateCompileException |
| 别名与已存在变量冲突 | 编译时 | TemplateCompileException |
| 循环导入 | 运行时跳过 | 不报错，返回空缓存 |
| export 不在顶层 | 编译时 | TemplateCompileException |
| export 重复变量名 | 编译时 | TemplateCompileException |
| 访问未 export 的成员 | 运行时 | TemplateRuntimeException |
| TemplateLoader 未配置 | 运行时 | TemplateRuntimeException |

#### 边界情况处理

1. **空模板导入**：模板无 export 语句，ImportedTemplateObject.getMember() 对任何成员都报错
2. **模板执行异常**：ensureExecuted() 中模板执行出错时，异常向上抛出
3. **render 返回非字符串**：render() 方法将结果转字符串返回
4. **Lambda 参数类型验证**：调用 export 的 Lambda 时，由 LambdaObject.invoke() 处理类型验证
5. **并发安全**：ImportedTemplateObject 的 executed 和 exportedValues 使用 volatile + synchronized

### 第八部分：改动文件列表

| 文件 | 改动类型 | 说明 |
|------|----------|------|
| `TokenType.java` | 新增 | `EXPORT` 关键字 |
| `Lexer.java` | 新增关键字 | `EXPORT` 词法单元识别 |
| `ImportTemplateStatement.java` | 新增文件 | 导入模板 AST 节点 |
| `ExportStatement.java` | 新增文件 | 导出变量 AST 节点 |
| `ASTVisitor.java` | 新增方法 | `visitImportTemplateStatement`、`visitExportStatement` |
| `Parser.java` | 修改 | 解析 import template 和 export 语句 |
| `ImportedTemplateObject.java` | 新增文件 | 运行时模板对象 |
| `ExportRegistry.java` | 新增文件 | Export 注册表 |
| `ExecutionTracker.java` | 新增文件 | 循环导入检测辅助类 |
| `ImportTemplateValidator.java` | 新增文件 | 编译阶段验证 import template |
| `ExportValidator.java` | 新增文件 | 编译阶段验证 export |
| `Evaluator.java` | 修改 | 新增 visit 方法和 MemberAccess/MethodCall 处理 |
| `Context.java` | 修改 | 新增 ExportRegistry 字段 |
| `InterpreterCompiler.java` | 修改 | 调用新的验证器 |

## 使用示例

### 基本用法

utils.etl:
```etl
{% export let add = (a, b) => a + b %}
{% export let multiply = (x, y) => x * y %}
{% let config = {name: 'utils', version: '1.0'} %}
{% export config %}
```

main.etl:
```etl
{% import 'utils.etl' as utils %}

{utils.add(1, 2)}           // 输出: 3
{utils.multiply(3, 4)}      // 输出: 12
{utils.config.name}         // 输出: utils
{utils.render()}            // 渲染 utils.etl 的文本输出
```

### render 方法

```etl
{% import 'utils.etl' as utils %}

// 使用当前上下文渲染
{utils.render()}

// 使用新上下文渲染
{utils.render(new Context())}

// 传入自定义上下文
{% let myCtx = new Context() %}
{myCtx.put('key', 'value')}
{utils.render(myCtx)}
```

### 循环导入处理

a.etl:
```etl
{% import 'b.etl' as b %}
{% export let funcA = () => b.funcB() %}
```

b.etl:
```etl
{% import 'a.etl' as a %}
{% export let funcB = () => 'from B' %}
```

调用 `a.funcA()` 时：
1. 执行 a.etl，发现 import b.etl
2. 执行 b.etl，发现 import a.etl（循环）
3. b.etl 的 a 别名返回空缓存
4. b.etl 正常执行，export funcB
5. a.etl 的 b 别名获得 funcB
6. funcA 可以调用 b.funcB()

## 测试要点

1. **Import Template 语法测试**
   - 基本导入 `{% import 'path.etl' as alias %}`
   - 文件不存在时报错
   - 别名冲突时报错
   - TemplateLoader 未配置时报错

2. **Export 语法测试**
   - `{% export let x = value %}`
   - `{% let x = value %}{% export x %}`
   - export 不在顶层时报错
   - 重复 export 同一变量时报错

3. **成员访问测试**
   - 访问 export 的 Lambda 函数
   - 访问 export 的普通变量
   - 访问未 export 的变量时报错

4. **方法调用测试**
   - 调用 export 的 Lambda：`alias.func(args)`
   - 调用 render 方法：`alias.render()`、`alias.render(ctx)`
   - 调用未 export 的方法时报错

5. **懒加载与缓存测试**
   - 第一次访问时执行模板
   - 第二次访问使用缓存值
   - 多次调用 render 使用同一缓存

6. **循环导入测试**
   - 直接循环导入（a 导 b，b 导 a）
   - 间接循环导入（a 导 b，b 导 c，c 导 a）
   - 循环导入时正确跳过，功能正常

7. **并发安全测试**
   - 多线程同时访问 ImportedTemplateObject
   - 确保只执行一次模板