# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

EasyTL is a lightweight string template engine built on Java 8 with no third-party dependencies. It provides JavaScript-like expression syntax support for template rendering.

## Build Commands

```bash
# Compile
mvn compile

# Run all tests
mvn test

# Run a specific test class
mvn test -Dtest=LexerTest
mvn test -Dtest=ParserTest
mvn test -Dtest=TemplateEngineTest

# Run a specific test method
mvn test -Dtest=LexerTest#testNumberLiteral

# Package
mvn package

# Clean build
mvn clean install
```

## Architecture

The template engine follows a classic compiler pipeline architecture:

```
Source → Lexer → TokenStream → Parser → AST → Compiler → Template
```

### Core Pipeline

1. **Lexer** (`org.dromara.easytl.parser.Lexer`): Tokenizes template source with three modes:
   - TEXT mode: Plain text content
   - EXPRESSION mode: Inside `{expr}`, `{{expr}}`, or `${expr}`
   - STATEMENT mode: Inside `{% stmt %}`

2. **Parser** (`org.dromara.easytl.parser.Parser`): Recursive descent parser producing AST

3. **AST** (`org.dromara.easytl.ast.*`): Node types including expressions, statements, and literals

4. **Compiler** (`org.dromara.easytl.compiler.InterpreterCompiler`): Compiles AST to `Template` objects

5. **Evaluator** (`org.dromara.easytl.compiler.Evaluator`): Evaluates AST nodes at runtime

### Key Packages

| Package | Purpose |
|---------|---------|
| `org.dromara.easytl` | Entry points: `TemplateEngine`, `Template`, `Context` |
| `org.dromara.easytl.parser` | Lexer, Parser, Token, TokenStream, Source implementations |
| `org.dromara.easytl.ast` | AST nodes (expressions, statements, literals) |
| `org.dromara.easytl.compiler` | Compiler interface and interpreter implementation |
| `org.dromara.easytl.runtime` | Runtime utilities: `OperatorEvaluator`, `ReflectionUtils`, `Range` |
| `org.dromara.easytl.loader` | Template loaders: `FileTemplateLoader`, `ClasspathTemplateLoader` |
| `org.dromara.easytl.exception` | Exception hierarchy |

### Entry Points

- **TemplateEngine**: Main entry for compiling templates from String, File, or InputStream
- **Template**: Compiled template interface with `render(Context)` method
- **Context**: Variable scope with parent-child hierarchy for template rendering

## Template Syntax

Three expression syntaxes (functionally equivalent):
- `{expression}` - Single brace
- `{{expression}}` - Double brace
- `${expression}` - Dollar brace

Statement blocks use `{% ... %}` syntax:
- `{% let x = value %}` - Variable declaration
- `{% extends "base.etl" %}` - Template inheritance
- `{% import "lib.etl" as lib %}` - Template import

Control flow:
- `{% if condition }...{% else }...{/% if }`
- `{% for item in list }...{/% for }`
- `{% switch expr }...{/% switch }`

See `BNF_GRAMMAR.md` for complete grammar specification.

## Workflow System

This project uses a custom java-workflow skill for development workflow management. The workflow follows a hierarchical structure:

```
Requirement → Epic → Sprint → Story → Task → Subtask
```

Workflow documents are in `.cursor/project/workflow/`:
- `requirements/` - Requirement documents
- `epics/` - Epic (project-level) documents
- `current-sprint/` - Active sprint
- `backlogs/` - Planned but not started work
- `history-sprints/` - Completed sprints

### Agent Delegation

- **tech-leader agent**: Task breakdown, status management, progress review
- **java-programmer agent**: Concrete task/subtask execution (coding)

When working with workflow tasks:
1. Use tech-leader for task breakdown and status updates
2. Use java-programmer for actual code implementation
3. Never mark tasks complete until compilation passes AND all AC tests pass

## Code Conventions

- Java 8 compatibility (no modern Java features)
- No third-party dependencies
- Thread-safe `Template` objects (can be shared across threads)
- LRU cache for compiled templates (configurable via `TemplateEngineConfig`)