# Increment/Decrement & C-Style For Loop Design

Date: 2026-04-14

## Overview

Add C/C++ style increment/decrement operators, compound assignment operators, and C-style for loops to the EasyTL template engine. All features use full expression semantics (operators return values and can appear anywhere in expressions).

## Requirements

| Feature | Decision |
|---------|----------|
| Increment/decrement | Full expression semantics: `i++` returns old value, `++i` returns new value |
| Compound assignment | Full set: `+= -= *= /= %= &= \|= ^= <<= >>= >>>=` |
| C-style for loop | `{% for let i = 0; cond; update }...{/% for }`, loop-scoped variables |
| For update clause | Any expression allowed |

## Design Approach: Orthogonal Additions

Each new feature is an independent AST node and parser path. No existing AST nodes, parser methods, or evaluator logic are modified. All changes are additions.

---

## 1. Lexer

### New Token Types

| Token | Literal | Description |
|-------|---------|-------------|
| `INCREMENT` | `++` | Increment operator |
| `DECREMENT` | `--` | Decrement operator |
| `PLUS_ASSIGN` | `+=` | Add-assign |
| `MINUS_ASSIGN` | `-=` | Subtract-assign |
| `MULTIPLY_ASSIGN` | `*=` | Multiply-assign |
| `DIVIDE_ASSIGN` | `/=` | Divide-assign |
| `MODULO_ASSIGN` | `%=` | Modulo-assign |
| `BIT_AND_ASSIGN` | `&=` | Bitwise AND-assign |
| `BIT_OR_ASSIGN` | `\|=` | Bitwise OR-assign |
| `BIT_XOR_ASSIGN` | `^=` | Bitwise XOR-assign |
| `LEFT_SHIFT_ASSIGN` | `<<=` | Left shift-assign |
| `RIGHT_SHIFT_ASSIGN` | `>>=` | Right shift-assign |
| `UNSIGNED_RIGHT_SHIFT_ASSIGN` | `>>>=` | Unsigned right shift-assign |
| `SEMICOLON` | `;` | Separator for C-style for |

### Scanning Strategy

- `+` scan priority: `++` > `+=` > `+` (two-character lookahead before falling back to single character)
- `>>>=` requires three-character lookahead: match `>>>` then check fourth character for `=`
- `;` scanned as `SEMICOLON` token in statement mode

---

## 2. AST

### 2.1 IncrementExpression

```java
public class IncrementExpression extends Expression {
    private final Expression operand;   // Must be assignable target
    private final boolean isPrefix;      // true: ++i, false: i++
    private final boolean isIncrement;   // true: ++, false: --
}
```

### 2.2 CompoundAssignmentExpression

```java
public class CompoundAssignmentExpression extends Expression {
    private final Expression target;            // Assignable target
    private final CompoundOperator operator;    // +=, -=, *=, ...
    private final Expression value;             // Right-hand expression
}
```

`CompoundOperator` enum: `PLUS_ASSIGN, MINUS_ASSIGN, MULTIPLY_ASSIGN, DIVIDE_ASSIGN, MODULO_ASSIGN, BIT_AND_ASSIGN, BIT_OR_ASSIGN, BIT_XOR_ASSIGN, LEFT_SHIFT_ASSIGN, RIGHT_SHIFT_ASSIGN, UNSIGNED_RIGHT_SHIFT_ASSIGN`

### 2.3 ClassicForStatement

```java
public class ClassicForStatement extends Statement {
    private final LetStatement initialization;  // let i = 0 (nullable)
    private final Expression condition;         // i < n (nullable, means true)
    private final Expression update;            // i++ (nullable)
    private final TemplateBody body;            // Loop body
}
```

### Target Validation

Assignable targets (validated at parse time):
- `IdentifierExpression` (simple variable)
- `PropertyAccessExpression` (`obj.prop`)
- `IndexAccessExpression` (`arr[i]`)

Invalid targets throw `TemplateSyntaxException` at parse time.

---

## 3. Parser

### 3.1 Operator Precedence

Updated precedence table (new entries marked):

| Priority | Operators | Associativity | Notes |
|----------|-----------|---------------|-------|
| **0 (new)** | **`+= -= *= /= %= ...`** | **Right** | **Compound assignment, lowest** |
| 1 | `??` | Right | Existing |
| 2 | `?:` | Right | Existing |
| 3 | `\|\|` | Left | Existing |
| 4 | `&&` | Left | Existing |
| 5 | `== != =~ !=~ in !in` | Left | Existing |
| 6 | `< <= > >=` | Left | Existing |
| 7 | `.. ..< >.. >..<` | Left | Existing |
| 8 | `+ -` | Left | Existing |
| 9 | `* / %` | Left | Existing |
| 10 | `! - + ++ --` (prefix) | Right | **Extended**: added `++ --` |
| 11 | `. ?. [] () ++ --` (postfix) | Left | **Extended**: added `++ --` |

### 3.2 Prefix Increment/Decrement

Integrated into existing `parseUnary()`:

```
parseUnary():
    if (match(INCREMENT) or match(DECREMENT)):
        operand = parseUnary()
        validate assignable(operand)
        return new IncrementExpression(operand, isPrefix=true, ...)
    ...existing logic for !, -, +...
```

### 3.3 Postfix Increment/Decrement

Appended to `parsePostfix()`:

```
parsePostfix():
    expr = parseCallOrAccess()
    if (match(INCREMENT)):
        validate assignable(expr)
        return new IncrementExpression(expr, isPrefix=false, isIncrement=true)
    if (match(DECREMENT)):
        validate assignable(expr)
        return new IncrementExpression(expr, isPrefix=false, isIncrement=false)
    return expr
```

`a.b++` parses as `(a.b)++` as expected.

### 3.4 Compound Assignment

New method called before `parseElvis()`:

```
parseAssignmentExpression():
    expr = parseElvis()
    if (match compound assignment token):
        right = parseAssignmentExpression()   // Right-recursive for right associativity
        validate assignable(expr)
        return new CompoundAssignmentExpression(expr, operator, right)
    return expr
```

`a += b += c` parses as `a += (b += c)`.

### 3.5 C-Style For Loop

Fork in existing `parseForStatement()`:

```
parseForStatement():
    consume FOR, LEFT_PAREN
    if lookahead is LET:
        // C-style for
        init = parseLetStatement()
        consume SEMICOLON
        condition = parseExpression() or null  // empty means true
        consume SEMICOLON
        update = parseExpression() or null
        consume RIGHT_PAREN, RIGHT_BRACE
        body = parseTemplateBody()
        consumeStatementClose(FOR)
        return new ClassicForStatement(init, condition, update, body)
    else:
        // Existing for-in syntax (unchanged)
        ...existing for-in logic...
```

Lookahead on `LET` keyword distinguishes the two for syntaxes.

---

## 4. Evaluator

### 4.1 IncrementExpression

```
evaluate IncrementExpression:
    oldValue = read target value

    if (isIncrement):
        newValue = OperatorEvaluator.add(oldValue, 1)
    else:
        newValue = OperatorEvaluator.subtract(oldValue, 1)

    write newValue to target
    return isPrefix ? newValue : oldValue
```

Reuses existing `OperatorEvaluator.add`/`subtract` for consistent type promotion and precision.

### 4.2 CompoundAssignmentExpression

```
evaluate CompoundAssignmentExpression:
    targetValue = evaluate target (read)
    rightValue = evaluate value expression

    switch (operator):
        PLUS_ASSIGN:    newValue = add(targetValue, rightValue)
        MINUS_ASSIGN:   newValue = subtract(targetValue, rightValue)
        MULTIPLY_ASSIGN: newValue = multiply(targetValue, rightValue)
        DIVIDE_ASSIGN:  newValue = divide(targetValue, rightValue)
        MODULO_ASSIGN:  newValue = modulo(targetValue, rightValue)
        BIT_AND_ASSIGN: newValue = bitwiseAnd(targetValue, rightValue)
        BIT_OR_ASSIGN:  newValue = bitwiseOr(targetValue, rightValue)
        BIT_XOR_ASSIGN: newValue = bitwiseXor(targetValue, rightValue)
        LEFT_SHIFT_ASSIGN:   newValue = leftShift(targetValue, rightValue)
        RIGHT_SHIFT_ASSIGN:  newValue = rightShift(targetValue, rightValue)
        UNSIGNED_RIGHT_SHIFT_ASSIGN: newValue = unsignedRightShift(targetValue, rightValue)

    write newValue to target
    return newValue
```

Bitwise operation methods are new additions to `OperatorEvaluator`. Integer-only operands; non-integer types converted to `long` first.

### 4.3 ClassicForStatement

```
evaluate ClassicForStatement:
    pushChildContext()    // Loop-scoped variables

    if (initialization != null):
        evaluate initialization

    while (true):
        if (condition != null):
            if (!isTruthy(evaluate condition)) break

        evaluate body

        if (update != null):
            evaluate update

    popContext()
```

`break`/`continue` propagate through existing exception mechanism (consistent with for-in).

### 4.4 Target Read/Write

| Target Type | Read | Write |
|-------------|------|-------|
| `IdentifierExpression` | `context.get(name)` | `context.set(name, value)` |
| `PropertyAccessExpression` | `ReflectionUtils.getProperty(obj, prop)` | `ReflectionUtils.setProperty(obj, prop, value)` (new) |
| `IndexAccessExpression` | `obj.get(index)` / array access | `obj.set(index, value)` / array assignment |

`ReflectionUtils.setProperty()` is a new addition. Existing `ReflectionUtils` is read-only.

---

## 5. Error Handling

| Scenario | Handling | Exception |
|----------|----------|-----------|
| `++5` / `++"hello"` | Reject at parse time, target not assignable | `TemplateSyntaxException` |
| `i++` where `i` undefined | Runtime error | `TemplateRuntimeException` |
| `i += "hello"` (i is number) | Runtime: type promotion (string concatenation) | Normal behavior |
| `for let i = 0; i < 5` missing semicolon | Reject at parse time | `TemplateSyntaxException` |
| `i >>= "abc"` | Runtime error, bitwise requires integer | `TemplateRuntimeException` |
| `obj.readonly++` (read-only property) | Runtime error, property not writable | `TemplateRuntimeException` |

---

## 6. Testing

### Lexer Tests (LexerTest)

- Correct scanning of all new tokens (`++`, `+=`, `>>=`, `>>>=`, etc.)
- Boundary cases: `+++` -> `++` + `+`, `+++=` -> `++` + `+=`
- `;` scanning in statement mode

### Parser Tests (ParserTest)

- Prefix `++x`, `--x` AST structure
- Postfix `x++`, `x--` AST structure
- Compound assignment `x += 1 + 2` precedence (right side fully parsed)
- Nested targets: `a.b++`, `arr[i]++`, `++(a.b)` target validation
- Invalid targets: `++5`, `"hello"++` -> syntax error
- C-style for parsing (with/without condition, with/without update)
- Correct forking between for-in and C-style for

### Evaluator / Integration Tests (TemplateEngineTest)

- Increment/decrement return value semantics: prefix returns new, postfix returns old
- Compound assignment calculation correctness and return values
- C-style for basic execution, nested loops
- Loop scope verification (variable invisible after loop)
- `break` / `continue` in C-style for
- Coexistence with existing for-in (no regression)

### End-to-End Tests

**Increment/Decrement:**
```
context.set("i", 5); "{i++}" -> "5", i == 6 after
context.set("i", 5); "{++i}" -> "6", i == 6 after
context.set("i", 1); "{i++ * 2}" -> "2", i == 2 after
context.set("obj", {count: 0}); "{obj.count++}" -> "0", obj.count == 1 after
```

**Compound Assignment:**
```
context.set("total", 10); "{total += 5}" -> "15"
context.set("x", 3); "{x *= 2}" -> "6"
context.set("msg", "hello"); "{msg += ' world'}" -> "hello world"
```

Note: Compound assignment is an expression, not a declaration. It does not use `let`. It modifies an existing variable and returns the new value.

**C-Style For Loop:**
```
"{% for let i = 0; i < 5; i++ }{i}{/% for}" -> "01234"
"{% for let i = 0; i < 10; i += 2 }{i}{/% for}" -> "02468"
"{% for let i = 0; i < 3; i++ }{% for let j = 0; j < 3; j++ }({i},{j}){/% for}{/% for}"
    -> "(0,0)(0,1)(0,2)(1,0)(1,1)(1,2)(2,0)(2,1)(2,2)"
"{% for let i = 0; i < 1; i++ }inner{/% for}{i}" -> compile error, i undefined
"{% for let i = 0; ; i++ }{i}{% if i >= 3 }{break}{/% if}{/% for}" -> "0123"
"{% for item in [1,2,3] }{item}{/% for}" -> "123" (regression check)
```
