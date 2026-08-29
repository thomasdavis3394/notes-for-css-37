# Import Template Statement Alias 实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现模板导入功能，支持通过别名导入其他模板文件并访问导出的变量和函数。

**Architecture:** 新增 EXPORT 关键字、ImportTemplateStatement 和 ExportStatement AST 节点、ImportedTemplateObject 运行时对象（懒加载+缓存）、ExportRegistry 注册表。Parser 区分 Java 类导入和模板文件导入，Evaluator 支持成员访问和方法调用。

**Tech Stack:** Java 8, 无第三方依赖

---

## 文件结构

**新增文件：**
- `src/main/java/org/dromara/easytl/ast/ImportTemplateStatement.java` - 模板导入 AST 节点
- `src/main/java/org/dromara/easytl/ast/ExportStatement.java` - 导出变量 AST 节点
- `src/main/java/org/dromara/easytl/runtime/ImportedTemplateObject.java` - 运行时模板对象
- `src/main/java/org/dromara/easytl/runtime/ExportRegistry.java` - Export 注册表
- `src/main/java/org/dromara/easytl/runtime/ExecutionTracker.java` - 循环导入检测
- `src/main/java/org/dromara/easytl/compiler/ImportTemplateValidator.java` - 编译验证
- `src/main/java/org/dromara/easytl/compiler/ExportValidator.java` - 编译验证
- `src/test/java/org/dromara/easytl/ImportTemplateIntegrationTest.java` - 集成测试

**修改文件：**
- `src/main/java/org/dromara/easytl/parser/TokenType.java` - 新增 EXPORT 常量
- `src/main/java/org/dromara/easytl/parser/Lexer.java` - 新增 export 关键字
- `src/main/java/org/dromara/easytl/parser/Parser.java` - 解析新语法
- `src/main/java/org/dromara/easytl/ast/ASTVisitor.java` - 新增 visit 方法
- `src/main/java/org/dromara/easytl/compiler/Evaluator.java` - 处理新节点
- `src/main/java/org/dromara/easytl/Context.java` - 新增 ExportRegistry
- `src/main/java/org/dromara/easytl/compiler/TemplateJavaImportValidator.java` - 新增 visit 方法

---

## Task 1: TokenType 和 Lexer - 新增 EXPORT 关键字

**Files:**
- Modify: `src/main/java/org/dromara/easytl/parser/TokenType.java`
- Modify: `src/main/java/org/dromara/easytl/parser/Lexer.java`
- Test: `src/test/java/org/dromara/easytl/parser/LexerTest.java`

- [ ] **Step 1: 在 TokenType 中新增 EXPORT 常量**

```java
// 在关键字区域添加（约第 93 行，RETURN 之后）
public static final int EXPORT = 103;           // export（导出变量）
```

- [ ] **Step 2: 在 TokenType.getSymbol() 中添加 EXPORT 映射**

```java
// 在 case RETURN: return "return"; 之后添加
case EXPORT: return "export";
```

- [ ] **Step 3: 在 TokenType.getName() 中添加 EXPORT 映射**

```java
// 在 case RETURN: return "RETURN"; 之后添加
case EXPORT: return "EXPORT";
```

- [ ] **Step 4: 在 Lexer KEYWORDS 静态块中添加 export 关键字**

```java
// 在 KEYWORDS.put("as", TokenType.AS); 之后添加
KEYWORDS.put("export", TokenType.EXPORT);
```

- [ ] **Step 5: 编译验证**

Run: `mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 6: 提交**

```bash
git add src/main/java/org/dromara/easytl/parser/TokenType.java src/main/java/org/dromara/easytl/parser/Lexer.java
git commit -m "feat: add EXPORT keyword to TokenType and Lexer"
```

---

## Task 2: AST 节点 - ImportTemplateStatement 和 ExportStatement

**Files:**
- Create: `src/main/java/org/dromara/easytl/ast/ImportTemplateStatement.java`
- Create: `src/main/java/org/dromara/easytl/ast/ExportStatement.java`
- Modify: `src/main/java/org/dromara/easytl/ast/ASTVisitor.java`
- Test: `src/test/java/org/dromara/easytl/ast/ASTNodeTest.java`

- [ ] **Step 1: 创建 ImportTemplateStatement 类**

```java
package org.dromara.easytl.ast;

import org.dromara.easytl.parser.Token;

/**
 * 模板导入语句节点
 * 表示导入其他模板文件
 * 格式：{% import 'path/to/template.etl' as alias %}
 *
 * @author EasyTL Team
 */
public class ImportTemplateStatement extends ASTNode {

    /** 模板文件路径 */
    private final String templatePath;

    /** 别名（必须指定） */
    private final String alias;

    /**
     * 构造模板导入语句节点
     *
     * @param templatePath 模板文件路径
     * @param alias 别名
     */
    public ImportTemplateStatement(String templatePath, String alias) {
        this.templatePath = templatePath;
        this.alias = alias;
    }

    /**
     * 构造模板导入语句节点（从 Token 获取位置信息）
     *
     * @param templatePath 模板文件路径
     * @param alias 别名
     * @param token Token 对象（用于获取位置信息）
     */
    public ImportTemplateStatement(String templatePath, String alias, Token token) {
        super(positionFromToken(token));
        this.templatePath = templatePath;
        this.alias = alias;
    }

    /**
     * 获取模板文件路径
     *
     * @return 模板文件路径
     */
    public String getTemplatePath() {
        return templatePath;
    }

    /**
     * 获取别名
     *
     * @return 别名
     */
    public String getAlias() {
        return alias;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitImportTemplateStatement(this);
    }

    @Override
    public String toString() {
        return "ImportTemplateStatement{templatePath='" + templatePath + "', alias='" + alias + "'}";
    }
}
```

- [ ] **Step 2: 创建 ExportStatement 类**

```java
package org.dromara.easytl.ast;

import org.dromara.easytl.parser.Token;

/**
 * 导出变量语句节点
 * 表示导出变量供其他模板使用
 * 格式：{% export let x = value %} 或 {% export x %}
 *
 * @author EasyTL Team
 */
public class ExportStatement extends ASTNode {

    /** 要导出的变量名 */
    private final String variableName;

    /** 声明并导出时的 LetStatement（可为 null） */
    private final LetStatement letStatement;

    /**
     * 构造导出语句节点
     *
     * @param variableName 要导出的变量名
     * @param letStatement 声明并导出时的 LetStatement（可为 null）
     */
    public ExportStatement(String variableName, LetStatement letStatement) {
        this.variableName = variableName;
        this.letStatement = letStatement;
    }

    /**
     * 构造导出语句节点（从 Token 获取位置信息）
     *
     * @param variableName 要导出的变量名
     * @param letStatement 声明并导出时的 LetStatement（可为 null）
     * @param token Token 对象（用于获取位置信息）
     */
    public ExportStatement(String variableName, LetStatement letStatement, Token token) {
        super(positionFromToken(token));
        this.variableName = variableName;
        this.letStatement = letStatement;
    }

    /**
     * 获取要导出的变量名
     *
     * @return 变量名
     */
    public String getVariableName() {
        return variableName;
    }

    /**
     * 获取声明并导出时的 LetStatement
     *
     * @return LetStatement，如果只是导出已声明变量则返回 null
     */
    public LetStatement getLetStatement() {
        return letStatement;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitExportStatement(this);
    }

    @Override
    public String toString() {
        return "ExportStatement{variableName='" + variableName + "', letStatement=" + letStatement + "}";
    }
}
```

- [ ] **Step 3: 在 ASTVisitor 接口中新增方法**

```java
// 在 visitImportStatement(ImportStatement node); 之后添加

// 模板导入节点访问方法
T visitImportTemplateStatement(ImportTemplateStatement node);

// 导出变量节点访问方法
T visitExportStatement(ExportStatement node);
```

- [ ] **Step 4: 编译验证**

Run: `mvn compile -q`
Expected: BUILD SUCCESS（会有未实现方法的警告，后续任务处理）

- [ ] **Step 5: 提交**

```bash
git add src/main/java/org/dromara/easytl/ast/ImportTemplateStatement.java src/main/java/org/dromara/easytl/ast/ExportStatement.java src/main/java/org/dromara/easytl/ast/ASTVisitor.java
git commit -m "feat: add ImportTemplateStatement and ExportStatement AST nodes"
```

---

## Task 3: Runtime - ExportRegistry 和 ExecutionTracker

**Files:**
- Create: `src/main/java/org/dromara/easytl/runtime/ExportRegistry.java`
- Create: `src/main/java/org/dromara/easytl/runtime/ExecutionTracker.java`
- Test: `src/test/java/org/dromara/easytl/runtime/ExportRegistryTest.java`

- [ ] **Step 1: 创建 ExportRegistry 类**

```java
package org.dromara.easytl.runtime;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Export 变量注册表
 * 用于跟踪模板中已导出的变量名
 *
 * @author EasyTL Team
 */
public class ExportRegistry {

    private final Set<String> exportedNames = new HashSet<>();

    /**
     * 注册导出变量
     *
     * @param name 变量名
     * @throws IllegalStateException 如果变量已被导出
     */
    public void register(String name) {
        if (exportedNames.contains(name)) {
            throw new IllegalStateException("Variable '" + name + "' is already exported");
        }
        exportedNames.add(name);
    }

    /**
     * 获取所有已导出的变量名
     *
     * @return 不可变的变量名集合
     */
    public Set<String> getExportedNames() {
        return Collections.unmodifiableSet(exportedNames);
    }

    /**
     * 检查变量是否已导出
     *
     * @param name 变量名
     * @return 如果已导出返回 true
     */
    public boolean isExported(String name) {
        return exportedNames.contains(name);
    }

    /**
     * 清空注册表
     */
    public void clear() {
        exportedNames.clear();
    }
}
```

- [ ] **Step 2: 创建 ExecutionTracker 类**

```java
package org.dromara.easytl.runtime;

import java.util.HashSet;
import java.util.Set;

/**
 * 模板执行追踪器
 * 用于检测循环导入
 *
 * @author EasyTL Team
 */
public class ExecutionTracker {

    private static final ThreadLocal<Set<String>> executingTemplates = ThreadLocal.withInitial(HashSet::new);

    /**
     * 检查模板是否正在执行
     *
     * @param templateKey 模板唯一标识（通常是路径）
     * @return 如果正在执行返回 true
     */
    public static boolean isExecuting(String templateKey) {
        return executingTemplates.get().contains(templateKey);
    }

    /**
     * 开始执行模板
     *
     * @param templateKey 模板唯一标识
     */
    public static void beginExecution(String templateKey) {
        executingTemplates.get().add(templateKey);
    }

    /**
     * 结束执行模板
     *
     * @param templateKey 模板唯一标识
     */
    public static void endExecution(String templateKey) {
        executingTemplates.get().remove(templateKey);
    }

    /**
     * 清空当前线程的执行追踪
     */
    public static void clear() {
        executingTemplates.get().clear();
    }

    private ExecutionTracker() {
        // 工具类，禁止实例化
    }
}
```

- [ ] **Step 3: 编译验证**

Run: `mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 4: 提交**

```bash
git add src/main/java/org/dromara/easytl/runtime/ExportRegistry.java src/main/java/org/dromara/easytl/runtime/ExecutionTracker.java
git commit -m "feat: add ExportRegistry and ExecutionTracker for import template support"
```

---

## Task 4: Runtime - ImportedTemplateObject

**Files:**
- Create: `src/main/java/org/dromara/easytl/runtime/ImportedTemplateObject.java`
- Test: `src/test/java/org/dromara/easytl/runtime/ImportedTemplateObjectTest.java`

- [ ] **Step 1: 创建 ImportedTemplateObject 类**

```java
package org.dromara.easytl.runtime;

import org.dromara.easytl.Context;
import org.dromara.easytl.ast.TemplateNode;
import org.dromara.easytl.compiler.Evaluator;
import org.dromara.easytl.exception.TemplateRuntimeException;
import org.dromara.easytl.loader.TemplateLoader;

import java.util.HashMap;
import java.util.Map;

/**
 * 导入的模板对象
 * 封装被导入模板的 AST，提供懒加载、缓存、成员访问和渲染功能
 *
 * @author EasyTL Team
 */
public class ImportedTemplateObject {

    private final TemplateNode templateAst;
    private final TemplateLoader templateLoader;
    private final Context definingContext;
    private final String sourceText;
    private final String templatePath;

    private volatile Map<String, Object> exportedValues;
    private volatile boolean executed;

    /**
     * 构造导入的模板对象
     *
     * @param templateAst    编译后的模板 AST
     * @param templateLoader 模板加载器
     * @param definingContext import 语句所在模板的上下文
     * @param sourceText     源文本（用于错误报告）
     * @param templatePath   模板路径（用于循环导入检测）
     */
    public ImportedTemplateObject(TemplateNode templateAst,
                                  TemplateLoader templateLoader,
                                  Context definingContext,
                                  String sourceText,
                                  String templatePath) {
        this.templateAst = templateAst;
        this.templateLoader = templateLoader;
        this.definingContext = definingContext;
        this.sourceText = sourceText;
        this.templatePath = templatePath;
        this.exportedValues = null;
        this.executed = false;
    }

    /**
     * 获取导出的成员
     *
     * @param name 成员名
     * @return 成员值
     * @throws TemplateRuntimeException 如果成员不存在
     */
    public Object getMember(String name) {
        ensureExecuted();

        Object value = exportedValues.get(name);
        if (value == null && !exportedValues.containsKey(name)) {
            throw new TemplateRuntimeException(
                "Imported template '" + templatePath + "' does not export '" + name + "'");
        }
        return value;
    }

    /**
     * 调用导出的方法
     *
     * @param name 方法名
     * @param args 参数
     * @return 方法返回值
     */
    public Object invokeMethod(String name, Object[] args) {
        // 特殊方法：render
        if ("render".equals(name)) {
            Context ctx = args.length > 0 && args[0] instanceof Context
                ? (Context) args[0]
                : definingContext;
            return render(ctx);
        }

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

    /**
     * 渲染模板文本
     *
     * @param ctx 上下文（可为 null，使用 definingContext）
     * @return 渲染结果
     */
    public String render(Context ctx) {
        Context renderContext = (ctx != null ? ctx : definingContext).createChildContext();
        renderContext.setExportRegistry(new ExportRegistry());

        Evaluator evaluator = new Evaluator(renderContext, templateLoader, sourceText, true);
        Object result = templateAst.accept(evaluator);
        return result != null ? result.toString() : "";
    }

    /**
     * 确保模板已执行（懒加载）
     */
    private void ensureExecuted() {
        if (executed) {
            return;
        }

        synchronized (this) {
            if (executed) {
                return;
            }

            // 循环导入检测
            if (ExecutionTracker.isExecuting(templatePath)) {
                // 循环导入，跳过，返回空缓存
                exportedValues = new HashMap<>();
                executed = true;
                return;
            }

            ExecutionTracker.beginExecution(templatePath);
            try {
                Context execContext = definingContext.createChildContext();
                ExportRegistry registry = new ExportRegistry();
                execContext.setExportRegistry(registry);

                Evaluator evaluator = new Evaluator(execContext, templateLoader, sourceText, true);
                templateAst.accept(evaluator);

                exportedValues = new HashMap<>();
                for (String name : registry.getExportedNames()) {
                    exportedValues.put(name, execContext.get(name));
                }

                executed = true;
            } finally {
                ExecutionTracker.endExecution(templatePath);
            }
        }
    }

    /**
     * 获取模板路径
     *
     * @return 模板路径
     */
    public String getTemplatePath() {
        return templatePath;
    }

    /**
     * 检查是否已执行
     *
     * @return 如果已执行返回 true
     */
    public boolean isExecuted() {
        return executed;
    }
}
```

- [ ] **Step 2: 编译验证**

Run: `mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: 提交**

```bash
git add src/main/java/org/dromara/easytl/runtime/ImportedTemplateObject.java
git commit -m "feat: add ImportedTemplateObject for lazy-loaded imported template support"
```

---

## Task 5: Context - 新增 ExportRegistry 支持

**Files:**
- Modify: `src/main/java/org/dromara/easytl/Context.java`

- [ ] **Step 1: 在 Context 类中添加 ExportRegistry 字段和方法**

在 Context 类中添加以下代码：

```java
// 在类的字段区域添加（约第 47 行，arguments 字段之后）

/**
 * Export 注册表（用于导出变量跟踪）
 */
private ExportRegistry exportRegistry;

// 在 getArguments() 方法之后添加

/**
 * 获取 Export 注册表
 *
 * @return Export 注册表，如果未设置则创建新的
 */
public ExportRegistry getExportRegistry() {
    if (exportRegistry == null) {
        exportRegistry = new ExportRegistry();
    }
    return exportRegistry;
}

/**
 * 设置 Export 注册表
 *
 * @param registry Export 注册表
 */
public void setExportRegistry(ExportRegistry registry) {
    this.exportRegistry = registry;
}
```

- [ ] **Step 2: 修改 createChildContext 方法共享 ExportRegistry**

找到 `createChildContext()` 方法，修改为：

```java
/**
 * 创建子作用域
 *
 * @return 新的子作用域
 */
public Context createChildContext() {
    Context child = new Context(this);
    child.exportRegistry = this.exportRegistry;  // 共享 ExportRegistry
    return child;
}
```

- [ ] **Step 3: 添加 import 语句**

在文件顶部添加：

```java
import org.dromara.easytl.runtime.ExportRegistry;
```

- [ ] **Step 4: 编译验证**

Run: `mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 5: 提交**

```bash
git add src/main/java/org/dromara/easytl/Context.java
git commit -m "feat: add ExportRegistry support to Context"
```

---

## Task 6: Parser - 解析 ImportTemplateStatement 和 ExportStatement

**Files:**
- Modify: `src/main/java/org/dromara/easytl/parser/Parser.java`
- Test: `src/test/java/org/dromara/easytl/parser/ParserTest.java`

- [ ] **Step 1: 修改 parseStatementBlock 添加 EXPORT 分支**

找到 `parseStatementBlock()` 方法中的关键字分发部分，在 `check(TokenType.IMPORT)` 之后添加：

```java
} else if (check(TokenType.EXPORT)) {
    return parseExportStatement();
```

- [ ] **Step 2: 修改 parseImportStatement 区分模板导入和 Java 类导入**

将现有的 `parseImportStatement()` 方法替换为：

```java
private ASTNode parseImportStatement() {
    Token importToken = current();
    expect(TokenType.IMPORT);

    if (check(TokenType.STRING)) {
        // 模板文件导入：{% import 'path/to/template.etl' as alias %}
        String path = advance().getValue();
        expect(TokenType.AS);
        String alias = expect(TokenType.IDENTIFIER).getValue();
        expect(TokenType.STMT_END);
        return new ImportTemplateStatement(path, alias, importToken);
    } else {
        // Java 类导入：{% import java.util.List %} 或 {% import java.util.List as ArrayList %}
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
        return new ImportStatement(className.toString(), alias, importToken);
    }
}
```

- [ ] **Step 3: 添加 parseExportStatement 方法**

在 `parseImportStatement()` 方法之后添加：

```java
private ASTNode parseExportStatement() {
    Token exportToken = current();
    expect(TokenType.EXPORT);

    if (check(TokenType.LET)) {
        // {% export let x = value %}
        Token letToken = current();
        advance(); // 消耗 LET
        Token name = expect(TokenType.IDENTIFIER);
        expect(TokenType.ASSIGN);
        ASTNode value = parseExpression();
        expect(TokenType.STMT_END);

        LetStatement letStmt = new LetStatement(name.getValue(), value, letToken);
        return new ExportStatement(name.getValue(), letStmt, exportToken);
    } else {
        // {% export x %}
        String varName = expect(TokenType.IDENTIFIER).getValue();
        expect(TokenType.STMT_END);
        return new ExportStatement(varName, null, exportToken);
    }
}
```

- [ ] **Step 4: 添加 import 语句**

在文件顶部添加：

```java
import org.dromara.easytl.ast.ExportStatement;
import org.dromara.easytl.ast.ImportTemplateStatement;
```

- [ ] **Step 5: 编译验证**

Run: `mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 6: 提交**

```bash
git add src/main/java/org/dromara/easytl/parser/Parser.java
git commit -m "feat: add parser support for ImportTemplateStatement and ExportStatement"
```

---

## Task 7: Validators - ExportValidator

**Files:**
- Create: `src/main/java/org/dromara/easytl/compiler/ExportValidator.java`
- Modify: `src/main/java/org/dromara/easytl/compiler/TemplateJavaImportValidator.java`

- [ ] **Step 1: 创建 ExportValidator 类**

```java
package org.dromara.easytl.compiler;

import org.dromara.easytl.ast.*;
import org.dromara.easytl.exception.TemplateCompileException;
import org.dromara.easytl.parser.StringSource;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 验证 export 语句的正确性
 * - 是否在顶层
 * - 是否重复 export
 */
public final class ExportValidator {

    private ExportValidator() {
    }

    /**
     * 验证 AST 中的所有 ExportStatement
     *
     * @param root       AST 根节点
     * @param sourceText 源代码文本（用于错误报告）
     */
    public static void validate(TemplateNode root, String sourceText) {
        if (root == null) {
            return;
        }

        Set<String> exportedNames = new HashSet<>();
        List<ASTNode> children = root.getChildren();

        for (ASTNode child : children) {
            if (child instanceof ExportStatement) {
                ExportStatement export = (ExportStatement) child;
                String varName = export.getVariableName();

                if (exportedNames.contains(varName)) {
                    String msg = "Variable '" + varName + "' is already exported";
                    if (export.getPosition() != null && !export.getPosition().isUnknown()) {
                        throw new TemplateCompileException(msg, export.getPosition(), new StringSource(sourceText));
                    }
                    throw new TemplateCompileException(msg);
                }
                exportedNames.add(varName);
            }
        }
    }
}
```

- [ ] **Step 2: 在 TemplateJavaImportValidator 中添加新方法**

在 TemplateJavaImportValidator 类中添加：

```java
@Override
public Void visitImportTemplateStatement(ImportTemplateStatement node) {
    // 模板导入在 ImportTemplateValidator 中验证
    return null;
}

@Override
public Void visitExportStatement(ExportStatement node) {
    // export 在 ExportValidator 中验证
    if (node.getLetStatement() != null) {
        node.getLetStatement().accept(this);
    }
    return null;
}
```

- [ ] **Step 3: 添加 import 语句**

在 TemplateJavaImportValidator.java 顶部添加：

```java
import org.dromara.easytl.ast.ExportStatement;
import org.dromara.easytl.ast.ImportTemplateStatement;
```

- [ ] **Step 4: 编译验证**

Run: `mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 5: 提交**

```bash
git add src/main/java/org/dromara/easytl/compiler/ExportValidator.java src/main/java/org/dromara/easytl/compiler/TemplateJavaImportValidator.java
git commit -m "feat: add ExportValidator for compile-time export validation"
```

---

## Task 8: Validators - ImportTemplateValidator

**Files:**
- Create: `src/main/java/org/dromara/easytl/compiler/ImportTemplateValidator.java`

- [ ] **Step 1: 创建 ImportTemplateValidator 类**

```java
package org.dromara.easytl.compiler;

import org.dromara.easytl.ast.*;
import org.dromara.easytl.exception.TemplateCompileException;
import org.dromara.easytl.loader.TemplateLoader;
import org.dromara.easytl.parser.Lexer;
import org.dromara.easytl.parser.Parser;
import org.dromara.easytl.parser.StringSource;
import org.dromara.easytl.parser.TokenStream;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 验证 import template 语句
 * - 模板文件是否存在
 * - 别名是否冲突
 * - 循环导入检测
 */
public final class ImportTemplateValidator {

    private ImportTemplateValidator() {
    }

    /**
     * 验证 AST 中的所有 ImportTemplateStatement
     *
     * @param root               AST 根节点
     * @param loader             模板加载器
     * @param currentTemplatePath 当前模板路径（用于相对路径解析和循环导入检测）
     * @param definedVariables   已定义的变量名集合
     * @param visitedTemplates   已访问的模板路径集合（用于循环导入检测）
     * @param sourceText         源代码文本（用于错误报告）
     */
    public static void validate(TemplateNode root,
                                TemplateLoader loader,
                                String currentTemplatePath,
                                Set<String> definedVariables,
                                Set<String> visitedTemplates,
                                String sourceText) {
        if (root == null || loader == null) {
            return;
        }

        Set<String> importedAliases = new HashSet<>();
        collectImports(root, loader, currentTemplatePath, definedVariables, importedAliases,
                       visitedTemplates != null ? visitedTemplates : new HashSet<>(), sourceText);
    }

    private static void collectImports(TemplateNode root,
                                       TemplateLoader loader,
                                       String currentTemplatePath,
                                       Set<String> definedVariables,
                                       Set<String> importedAliases,
                                       Set<String> visitedTemplates,
                                       String sourceText) {
        // 检测循环导入
        if (currentTemplatePath != null && visitedTemplates.contains(currentTemplatePath)) {
            return; // 已经在访问路径中，跳过
        }
        if (currentTemplatePath != null) {
            visitedTemplates.add(currentTemplatePath);
        }

        List<ASTNode> children = root.getChildren();
        Set<String> localDefinedVariables = new HashSet<>();
        if (definedVariables != null) {
            localDefinedVariables.addAll(definedVariables);
        }

        for (ASTNode child : children) {
            if (child instanceof ImportTemplateStatement) {
                ImportTemplateStatement importStmt = (ImportTemplateStatement) child;
                String alias = importStmt.getAlias();
                String templatePath = importStmt.getTemplatePath();

                // 检查别名冲突
                if (localDefinedVariables.contains(alias)) {
                    String msg = "Alias '" + alias + "' conflicts with an existing variable";
                    if (importStmt.getPosition() != null && !importStmt.getPosition().isUnknown()) {
                        throw new TemplateCompileException(msg, importStmt.getPosition(), new StringSource(sourceText));
                    }
                    throw new TemplateCompileException(msg);
                }

                if (importedAliases.contains(alias)) {
                    String msg = "Alias '" + alias + "' is already used for another import";
                    if (importStmt.getPosition() != null && !importStmt.getPosition().isUnknown()) {
                        throw new TemplateCompileException(msg, importStmt.getPosition(), new StringSource(sourceText));
                    }
                    throw new TemplateCompileException(msg);
                }

                // 检查模板文件是否存在
                try {
                    String content = loader.load(templatePath);
                    importedAliases.add(alias);
                    localDefinedVariables.add(alias);

                    // 递归检查被导入模板中的 import 语句
                    Lexer lexer = new Lexer(new StringSource(content));
                    TokenStream tokens = new TokenStream(lexer);
                    Parser parser = new Parser(tokens);
                    TemplateNode importedAst = parser.parse();
                    collectImports(importedAst, loader, templatePath, null, new HashSet<>(),
                                   new HashSet<>(visitedTemplates), content);

                } catch (IOException e) {
                    String msg = "Cannot import template '" + templatePath + "': " + e.getMessage();
                    if (importStmt.getPosition() != null && !importStmt.getPosition().isUnknown()) {
                        throw new TemplateCompileException(msg, importStmt.getPosition(), new StringSource(sourceText));
                    }
                    throw new TemplateCompileException(msg);
                } catch (TemplateCompileException e) {
                    throw e;
                } catch (Exception e) {
                    String msg = "Failed to parse imported template '" + templatePath + "': " + e.getMessage();
                    if (importStmt.getPosition() != null && !importStmt.getPosition().isUnknown()) {
                        throw new TemplateCompileException(msg, importStmt.getPosition(), new StringSource(sourceText));
                    }
                    throw new TemplateCompileException(msg);
                }

            } else if (child instanceof LetStatement) {
                LetStatement letStmt = (LetStatement) child;
                localDefinedVariables.add(letStmt.getVariableName());
            }
        }
    }
}
```

- [ ] **Step 2: 编译验证**

Run: `mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: 提交**

```bash
git add src/main/java/org/dromara/easytl/compiler/ImportTemplateValidator.java
git commit -m "feat: add ImportTemplateValidator for compile-time import validation"
```

---

## Task 9: Evaluator - 处理新 AST 节点

**Files:**
- Modify: `src/main/java/org/dromara/easytl/compiler/Evaluator.java`

- [ ] **Step 1: 添加 import 语句**

在 Evaluator.java 顶部添加：

```java
import org.dromara.easytl.ast.ExportStatement;
import org.dromara.easytl.ast.ImportTemplateStatement;
import org.dromara.easytl.runtime.ExecutionTracker;
import org.dromara.easytl.runtime.ImportedTemplateObject;
import org.dromara.easytl.runtime.ExportRegistry;
```

- [ ] **Step 2: 添加 visitImportTemplateStatement 方法**

在 `visitImportStatement` 方法之后添加：

```java
@Override
public Object visitImportTemplateStatement(ImportTemplateStatement node) {
    String templatePath = node.getTemplatePath();
    String alias = node.getAlias();

    if (templateLoader == null) {
        throw runtimeError("TemplateLoader is not configured. Cannot import template: " + templatePath, node);
    }

    try {
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
    } catch (TemplateRuntimeException e) {
        throw enrichRuntimeException(e, node);
    } catch (Exception e) {
        throw runtimeError("Failed to import template '" + templatePath + "': " + e.getMessage(), node);
    }
}
```

- [ ] **Step 3: 添加 visitExportStatement 方法**

在 `visitImportTemplateStatement` 方法之后添加：

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

- [ ] **Step 4: 修改 visitMemberAccess 支持 ImportedTemplateObject**

找到 `visitMemberAccess` 方法，在检查 `obj == null` 之前添加：

```java
@Override
public Object visitMemberAccess(MemberAccess node) {
    Object obj = node.getObject().accept(this);

    // 支持对 ImportedTemplateObject 的成员访问
    if (obj instanceof ImportedTemplateObject) {
        ImportedTemplateObject imported = (ImportedTemplateObject) obj;
        return imported.getMember(node.getMemberName());
    }

    // 如果是空安全访问且对象为 null，直接返回 null
    if (node.isSafeNavigation() && obj == null) {
        return null;
    }
    // ... 后续现有逻辑不变
```

- [ ] **Step 5: 修改 visitMethodCall 支持 ImportedTemplateObject**

找到 `visitMethodCall` 方法，在处理 `node.getObject() == null` 之后添加：

```java
@Override
public Object visitMethodCall(MethodCall node) {
    // Handle the case where object is null (function-style call like fn())
    if (node.getObject() == null) {
        // ... 现有逻辑不变 ...
    }

    Object obj = node.getObject().accept(this);

    // 支持对 ImportedTemplateObject 的方法调用
    if (obj instanceof ImportedTemplateObject) {
        ImportedTemplateObject imported = (ImportedTemplateObject) obj;
        String methodName = node.getMethodName();
        List<ASTNode> argNodes = node.getArguments();
        Object[] args = new Object[argNodes.size()];
        for (int i = 0; i < argNodes.size(); i++) {
            args[i] = argNodes.get(i).accept(this);
        }
        return imported.invokeMethod(methodName, args);
    }

    if (obj == null) {
        // ... 后续现有逻辑不变 ...
```

- [ ] **Step 6: 编译验证**

Run: `mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 7: 提交**

```bash
git add src/main/java/org/dromara/easytl/compiler/Evaluator.java
git commit -m "feat: add Evaluator support for ImportTemplateStatement and ExportStatement"
```

---

## Task 10: 编译器集成 - InterpreterTemplate 验证

**Files:**
- Modify: `src/main/java/org/dromara/easytl/compiler/InterpreterTemplate.java`

- [ ] **Step 1: 在 InterpreterTemplate 构造函数中添加验证调用**

修改 InterpreterTemplate 构造函数：

```java
public InterpreterTemplate(TemplateNode ast, TemplateLoader templateLoader, String sourceText,
        boolean templateJavaClassImportEnabled) {
    if (ast == null) {
        throw new IllegalArgumentException("AST cannot be null");
    }
    this.ast = ast;
    this.templateLoader = templateLoader;
    this.sourceText = sourceText;
    this.templateJavaClassImportEnabled = templateJavaClassImportEnabled;

    // 运行验证
    TemplateJavaImportValidator.validate(ast, templateJavaClassImportEnabled, sourceText);
    ExportValidator.validate(ast, sourceText);
    // ImportTemplateValidator 需要在有 TemplateLoader 时才验证
    if (templateLoader != null && sourceText != null) {
        ImportTemplateValidator.validate(ast, templateLoader, null, new HashSet<>(), new HashSet<>(), sourceText);
    }
}
```

- [ ] **Step 2: 编译验证**

Run: `mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: 提交**

```bash
git add src/main/java/org/dromara/easytl/compiler/InterpreterTemplate.java
git commit -m "feat: integrate validators into InterpreterTemplate"
```

---

## Task 11: 测试 - 单元测试

**Files:**
- Create: `src/test/java/org/dromara/easytl/runtime/ExportRegistryTest.java`
- Create: `src/test/java/org/dromara/easytl/runtime/ImportedTemplateObjectTest.java`
- Create: `src/test/java/org/dromara/easytl/ast/ImportTemplateStatementTest.java`
- Create: `src/test/java/org/dromara/easytl/ast/ExportStatementTest.java`

- [ ] **Step 1: 创建 ExportRegistryTest**

```java
package org.dromara.easytl.runtime;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExportRegistryTest {

    @Test
    void testRegisterAndGetNames() {
        ExportRegistry registry = new ExportRegistry();
        registry.register("add");
        registry.register("multiply");

        assertTrue(registry.isExported("add"));
        assertTrue(registry.isExported("multiply"));
        assertFalse(registry.isExported("unknown"));
        assertEquals(2, registry.getExportedNames().size());
    }

    @Test
    void testDuplicateRegisterThrowsException() {
        ExportRegistry registry = new ExportRegistry();
        registry.register("add");

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            registry.register("add");
        });
        assertTrue(ex.getMessage().contains("already exported"));
    }

    @Test
    void testClear() {
        ExportRegistry registry = new ExportRegistry();
        registry.register("add");
        registry.clear();
        assertFalse(registry.isExported("add"));
    }
}
```

- [ ] **Step 2: 运行测试验证**

Run: `mvn test -Dtest=ExportRegistryTest -q`
Expected: Tests pass

- [ ] **Step 3: 创建 AST 节点测试**

```java
package org.dromara.easytl.ast;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ImportTemplateStatementTest {

    @Test
    void testConstruction() {
        ImportTemplateStatement stmt = new ImportTemplateStatement("utils.etl", "utils");
        assertEquals("utils.etl", stmt.getTemplatePath());
        assertEquals("utils", stmt.getAlias());
    }

    @Test
    void testAccept() {
        ImportTemplateStatement stmt = new ImportTemplateStatement("utils.etl", "utils");
        String result = stmt.accept(new TestVisitor());
        assertEquals("ImportTemplateStatement", result);
    }

    private static class TestVisitor implements ASTVisitor<String> {
        @Override public String visitImportTemplateStatement(ImportTemplateStatement node) { return "ImportTemplateStatement"; }
        @Override public String visitExportStatement(ExportStatement node) { return "ExportStatement"; }
        // ... 其他方法返回 null
        @Override public String visitStringLiteral(StringLiteral node) { return null; }
        @Override public String visitTemplateLiteral(TemplateLiteral node) { return null; }
        @Override public String visitNumberLiteral(NumberLiteral node) { return null; }
        @Override public String visitBooleanLiteral(BooleanLiteral node) { return null; }
        @Override public String visitNullLiteral(NullLiteral node) { return null; }
        @Override public String visitListLiteral(ListLiteral node) { return null; }
        @Override public String visitHashLiteral(HashLiteral node) { return null; }
        @Override public String visitRangeLiteral(RangeLiteral node) { return null; }
        @Override public String visitRegexLiteral(RegexLiteral node) { return null; }
        @Override public String visitIdentifier(Identifier node) { return null; }
        @Override public String visitMemberAccess(MemberAccess node) { return null; }
        @Override public String visitIndexAccess(IndexAccess node) { return null; }
        @Override public String visitArgumentAccess(ArgumentAccess node) { return null; }
        @Override public String visitArgumentVariable(ArgumentVariable node) { return null; }
        @Override public String visitMethodCall(MethodCall node) { return null; }
        @Override public String visitBinaryExpression(BinaryExpression node) { return null; }
        @Override public String visitUnaryExpression(UnaryExpression node) { return null; }
        @Override public String visitTernaryExpression(TernaryExpression node) { return null; }
        @Override public String visitElvisExpression(ElvisExpression node) { return null; }
        @Override public String visitIfExpression(IfExpression node) { return null; }
        @Override public String visitForExpression(ForExpression node) { return null; }
        @Override public String visitTemplateNode(TemplateNode node) { return null; }
        @Override public String visitTextNode(TextNode node) { return null; }
        @Override public String visitSingleBraceExpressionNode(SingleBraceExpressionNode node) { return null; }
        @Override public String visitDoubleBraceExpressionNode(DoubleBraceExpressionNode node) { return null; }
        @Override public String visitDollarBraceExpressionNode(DollarBraceExpressionNode node) { return null; }
        @Override public String visitIfStatement(IfStatement node) { return null; }
        @Override public String visitForStatement(ForStatement node) { return null; }
        @Override public String visitSwitchStatement(SwitchStatement node) { return null; }
        @Override public String visitCaseClause(CaseClause node) { return null; }
        @Override public String visitLetStatement(LetStatement node) { return null; }
        @Override public String visitExtendsStatement(ExtendsStatement node) { return null; }
        @Override public String visitImportStatement(ImportStatement node) { return null; }
        @Override public String visitObjectConstruction(ObjectConstruction node) { return null; }
        @Override public String visitLambdaExpression(LambdaExpression node) { return null; }
        @Override public String visitReturnStatement(ReturnStatement node) { return null; }
        @Override public String visitUrlLiteral(UrlLiteral node) { return null; }
    }
}
```

- [ ] **Step 4: 运行测试验证**

Run: `mvn test -Dtest=ImportTemplateStatementTest -q`
Expected: Tests pass

- [ ] **Step 5: 提交**

```bash
git add src/test/java/org/dromara/easytl/runtime/ExportRegistryTest.java src/test/java/org/dromara/easytl/ast/ImportTemplateStatementTest.java
git commit -m "test: add unit tests for ExportRegistry and ImportTemplateStatement"
```

---

## Task 12: 测试 - 集成测试

**Files:**
- Create: `src/test/java/org/dromara/easytl/ImportTemplateIntegrationTest.java`
- Create: `src/test/resources/templates/import/utils.etl`
- Create: `src/test/resources/templates/import/main.etl`

- [ ] **Step 1: 创建测试模板文件**

`src/test/resources/templates/import/utils.etl`:
```etl
{% export let add = (a, b) => a + b %}
{% export let multiply = (x, y) => x * y %}
{% let config = {name: 'utils', version: '1.0'} %}
{% export config %}
```

- [ ] **Step 2: 创建集成测试类**

```java
package org.dromara.easytl;

import org.dromara.easytl.loader.FileTemplateLoader;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class ImportTemplateIntegrationTest {

    private static FileTemplateLoader loader;

    @BeforeAll
    static void setup() {
        loader = new FileTemplateLoader(new File("src/test/resources/templates/import"));
    }

    @Test
    void testBasicImportAndFunctionCall() {
        Context ctx = new Context();
        Template template = TemplateEngine.compile("{% import 'utils.etl' as utils %}{utils.add(1, 2)}", loader);
        String result = template.render(ctx);
        assertEquals("3", result);
    }

    @Test
    void testMultipleFunctionCalls() {
        Context ctx = new Context();
        Template template = TemplateEngine.compile(
            "{% import 'utils.etl' as utils %}{utils.add(1, 2)} {utils.multiply(3, 4)}", loader);
        String result = template.render(ctx);
        assertEquals("3 12", result);
    }

    @Test
    void testAccessExportedVariable() {
        Context ctx = new Context();
        Template template = TemplateEngine.compile(
            "{% import 'utils.etl' as utils %}{utils.config.name}", loader);
        String result = template.render(ctx);
        assertEquals("utils", result);
    }

    @Test
    void testRenderMethod() {
        Context ctx = new Context();
        Template template = TemplateEngine.compile(
            "{% import 'utils.etl' as utils %}{utils.render()}", loader);
        String result = template.render(ctx);
        // utils.etl 的 export 语句不产生文本输出
        assertEquals("", result.trim());
    }

    @Test
    void testExportLetSyntax() {
        String templateSource = "{% export let greet = (name) => 'Hello, ' + name %}{greet('World')}";
        Template template = TemplateEngine.compile(templateSource);
        String result = template.render(new Context());
        assertEquals("Hello, World", result);
    }

    @Test
    void testExportExistingVariableSyntax() {
        String templateSource = "{% let x = 100 %}{% export x %}{x}";
        Template template = TemplateEngine.compile(templateSource);
        String result = template.render(new Context());
        assertEquals("100", result);
    }

    @Test
    void testDuplicateExportThrowsException() {
        String templateSource = "{% let x = 1 %}{% export x %}{% export x %}";
        assertThrows(Exception.class, () -> {
            TemplateEngine.compile(templateSource);
        });
    }

    @Test
    void testAccessNonExportedMemberThrowsException() {
        Context ctx = new Context();
        Template template = TemplateEngine.compile(
            "{% import 'utils.etl' as utils %}{utils.nonExistent}", loader);
        assertThrows(Exception.class, () -> {
            template.render(ctx);
        });
    }
}
```

- [ ] **Step 3: 运行测试验证**

Run: `mvn test -Dtest=ImportTemplateIntegrationTest -q`
Expected: Tests pass (可能需要调整实现细节)

- [ ] **Step 4: 提交**

```bash
git add src/test/java/org/dromara/easytl/ImportTemplateIntegrationTest.java src/test/resources/templates/import/
git commit -m "test: add integration tests for import template feature"
```

---

## Task 13: 测试 - 循环导入测试

**Files:**
- Create: `src/test/resources/templates/import/circular_a.etl`
- Create: `src/test/resources/templates/import/circular_b.etl`
- Modify: `src/test/java/org/dromara/easytl/ImportTemplateIntegrationTest.java`

- [ ] **Step 1: 创建循环导入测试模板**

`src/test/resources/templates/import/circular_a.etl`:
```etl
{% import 'circular_b.etl' as b %}
{% export let funcA = () => b.funcB() %}
```

`src/test/resources/templates/import/circular_b.etl`:
```etl
{% import 'circular_a.etl' as a %}
{% export let funcB = () => 'from B' %}
```

- [ ] **Step 2: 添加循环导入测试方法**

```java
@Test
void testCircularImport() {
    Context ctx = new Context();
    Template template = TemplateEngine.compile(
        "{% import 'circular_a.etl' as a %}{a.funcA()}", loader);
    String result = template.render(ctx);
    assertEquals("from B", result);
}
```

- [ ] **Step 3: 运行测试验证**

Run: `mvn test -Dtest=ImportTemplateIntegrationTest#testCircularImport -q`
Expected: Test passes

- [ ] **Step 4: 提交**

```bash
git add src/test/resources/templates/import/circular_a.etl src/test/resources/templates/import/circular_b.etl src/test/java/org/dromara/easytl/ImportTemplateIntegrationTest.java
git commit -m "test: add circular import test"
```

---

## Task 14: 完整测试与最终提交

**Files:**
- All modified files

- [ ] **Step 1: 运行所有测试**

Run: `mvn test -q`
Expected: All tests pass

- [ ] **Step 2: 运行完整构建**

Run: `mvn clean install -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: 检查代码风格和文档**

确保所有新增类有 Javadoc 注释，方法有文档说明。

- [ ] **Step 4: 最终提交（如果有遗漏）**

```bash
git status
# 检查是否有未提交的更改
git add -A
git commit -m "chore: finalize import template alias implementation"
```

---

## 实现检查清单

| 功能点 | 测试方法 |
|--------|----------|
| EXPORT 关键字词法分析 | Lexer 识别 export 标识符 |
| ImportTemplateStatement 解析 | Parser 解析 `{% import 'path.etl' as alias %}` |
| ExportStatement 解析 | Parser 解析两种 export 语法 |
| 别名冲突检测 | 编译时报错 |
| 模板文件不存在 | 编译时报错 |
| 循环导入 | 运行时跳过，功能正常 |
| 访问 export 的 Lambda | `alias.func(args)` |
| 访问 export 的变量 | `alias.varName` |
| render 方法 | `alias.render()`, `alias.render(ctx)` |
| 懒加载与缓存 | 第二次访问使用缓存 |
| export 重复变量 | 编译时报错 |
| export 不在顶层 | 编译时报错（通过 ExportValidator） |