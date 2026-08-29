---
name: null-pointer-property-access-error-position
description: Fix caret position for null pointer property access errors - point to property name only
type: project
---

# 设计文档：修复 null 指针属性访问错误的定位符号

## 问题分析

### 当前行为

模板 `Hello {user.name}!` 中，当 `user` 为 null 时，运行时错误显示：

```
  |
1 | Hello {user.name}!
  |        ^^^^^^^^ Runtime error: Cannot access member 'name' of null
```

定位符号 `^^^^^^^` 指向整个 `user.name` 表达式（列 8-16）。

### 期望行为

定位符号应只指向属性名 `name`：

```
  |
1 | Hello {user.name}!
  |             ^^^^ Runtime error: Cannot access member 'name' of null
```

定位符号 `^^^^` 只指向 `name`（列 13-17）。

### 根因分析

**Why:** 当前 MemberAccess 节点的 position 存储的是整个表达式（从对象起始到属性名结束）的范围。Evaluator 在抛出 null 指针错误时直接使用这个 position，导致 caret 指向整个表达式。

**How to fix:** 在 MemberAccess 中单独存储属性名的位置，null 指针错误时使用属性名位置而非整个表达式位置。

## Token 位置验证

通过 Lexer 输出确认 `name` Token 的位置：

```
Token{type=IDENTIFIER, value='name', line=1, startColumn=13, endColumn=17}
```

使用此位置（startColumn=13, endColumn=17）生成的错误输出符合预期。

**Note:** Token 的 `endColumn` 采用 exclusive 语义（指向最后一个字符之后的位置），ErrorMessageFormatter 已正确处理此语义。

## 实现方案

### 修改范围

仅应用于 null 指针属性访问错误，其他错误保持原有行为。

### 修改内容

#### 1. MemberAccess 类

新增字段存储属性名位置：

```java
/** 属性名位置（仅属性名部分，不包含对象和点号） */
private final Position memberNamePosition;

/**
 * 构造成员访问节点（包含属性名位置）
 *
 * @param object 对象表达式
 * @param memberName 成员名称
 * @param safeNavigation 是否为空安全访问
 * @param position 整个表达式的位置
 * @param memberNamePosition 属性名的位置
 */
public MemberAccess(ASTNode object, String memberName, boolean safeNavigation, 
                    Position position, Position memberNamePosition) {
    super(position);
    this.object = object;
    this.memberName = memberName;
    this.safeNavigation = safeNavigation;
    this.memberNamePosition = memberNamePosition;
}

/**
 * 获取属性名的位置
 *
 * @return 属性名位置，可能为 null 或 unknown
 */
public Position getMemberNamePosition() {
    return memberNamePosition;
}
```

#### 2. Parser 类

在解析成员访问时，保存属性名 Token 的位置：

```java
if (check(TokenType.DOT)) {
    advance();
    Token name = expect(TokenType.IDENTIFIER);
    // 整个表达式的位置
    Position fullPos = new Position(expr.getStartLine(), expr.getStartColumn(),
                                    name.getLine(), name.getEndColumn());
    // 属性名的位置（使用 Token 的原始位置）
    Position memberNamePos = new Position(name.getLine(), name.getStartColumn(),
                                          name.getLine(), name.getEndColumn());
    expr = new MemberAccess(expr, name.getValue(), false, fullPos, memberNamePos);
}
```

同样修改 `SAFE_NAV` 分支。

#### 3. Evaluator 类

null 指针错误使用属性名位置：

```java
@Override
public Object visitMemberAccess(MemberAccess node) {
    Object obj = node.getObject().accept(this);

    // 如果是空安全访问且对象为null，直接返回null
    if (node.isSafeNavigation() && obj == null) {
        return null;
    }

    if (obj == null) {
        // 使用属性名位置而非整个表达式位置
        Position errorPos = node.getMemberNamePosition();
        if (errorPos != null && !errorPos.isUnknown()) {
            throw new TemplateRuntimeException(
                "Cannot access member '" + node.getMemberName() + "' of null",
                errorPos, new StringSource(sourceText));
        }
        throw runtimeError("Cannot access member '" + node.getMemberName() + "' of null", node);
    }
    
    // ... 其他逻辑不变
}
```

## 测试验证

### 测试用例

1. **基本测试**：`Hello {user.name}!` - caret 指向 `name`
2. **链式访问**：`{user.address.city}` - 当 `address` 为 null 时，caret 指向 `city`
3. **安全访问对比**：`{user?.name}` - 无错误抛出
4. **属性不存在错误**：保持原有行为（caret 指向整个表达式）

### 测试代码位置

`src/test/java/org/dromara/easytl/exception/RuntimeExceptionFormattingTest.java`

新增测试方法验证 caret 位置。

## 影响范围

- **向后兼容**：是，新增字段不影响现有代码
- **其他错误类型**：不改变，仅 null 指针错误使用新位置
- **编译时错误**：不涉及，仅运行时错误