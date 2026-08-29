package org.dromara.easytl;

import org.dromara.easytl.exception.TemplateCompileException;
import org.dromara.easytl.exception.TemplateException;
import org.dromara.easytl.loader.ClasspathTemplateLoader;
import org.dromara.easytl.testutil.ErrorPointerAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests all compile-time errors via engine.compile(...) API.
 * Verifies error location information (line, column, pointer) accuracy.
 */
@DisplayName("Compile Error Tests")
class CompileErrorTest {

    private TemplateEngine engine;

    @BeforeEach
    void setUp() {
        engine = EasyTL.defaultEngine();
    }

    /**
     * Helper method: Verify engine.compile throws exception with correct error pointer.
     */
    private void assertCompileError(
        String template,
        String expectedSourceLine,
        String expectedPointerLine,
        String expectedErrorMessage
    ) {
        TemplateException ex = assertThrows(
            TemplateException.class,
            () -> engine.compile(template),
            "Should throw TemplateException for: " + template
        );
        ErrorPointerAssert.assertErrorPointer(ex,
            expectedSourceLine, expectedPointerLine, expectedErrorMessage);
    }

    @Nested
    @DisplayName("Syntax Errors")
    class SyntaxErrors {

        // ========== Expression Wrapper Errors ==========

        @Test
        @DisplayName("Unclosed single brace expression")
        void testUnclosedSingleBrace() {
            assertCompileError("{x",
                "{x",
                "  ^",
                "Unclosed expression"
            );
        }

        @Test
        @DisplayName("Unclosed double brace expression")
        void testUnclosedDoubleBrace() {
            assertCompileError("{{x",
                "{{x",
                "   ^",
                "Unclosed expression"
            );
        }

        @Test
        @DisplayName("Unclosed dollar brace expression")
        void testUnclosedDollarBrace() {
            assertCompileError("${x",
                "${x",
                "   ^",
                "Unclosed expression"
            );
        }

        // ========== If Statement Errors ==========

        @Test
        @DisplayName("If statement missing condition")
        void testIfMissingCondition() {
            assertCompileError("{% if } %}",
                "{% if } %}",
                "      ^",
                "Unexpected token"
            );
        }

        @Test
        @DisplayName("If statement missing right brace")
        void testIfMissingRightBrace() {
            assertCompileError("{% if true %}",
                "{% if true %}",
                "             ^",
                "Expected {/%"
            );
        }

        @Test
        @DisplayName("If statement missing end tag")
        void testIfMissingEndTag() {
            assertCompileError("{% if true %}content",
                "{% if true %}content",
                "                    ^",
                "Expected {/%"
            );
        }

        @Test
        @DisplayName("If statement wrong end tag")
        void testIfWrongEndTag() {
            assertCompileError("{% if true %}content{% endfor %}",
                "{% if true %}content{% endfor %}",
                "                                ^",
                "Expected {/%"
            );
        }

        @Test
        @DisplayName("Else if missing condition")
        void testElseIfMissingCondition() {
            assertCompileError("{% if false %}a{% else if %}b{% endif %}",
                "{% if false %}a{% else if %}b{% endif %}",
                "                          ^^",
                "Unexpected token"
            );
        }

        @Test
        @DisplayName("Else if missing right brace")
        void testElseIfMissingRightBrace() {
            assertCompileError("{% if false %}a{% else if true %}b{% endif %}",
                "{% if false %}a{% else if true %}b{% endif %}",
                "                                             ^",
                "Expected {/%"
            );
        }

        @Test
        @DisplayName("Else missing right brace")
        void testElseMissingRightBrace() {
            assertCompileError("{% if false %}a{% else %}b{% endif %}",
                "{% if false %}a{% else %}b{% endif %}",
                "                                     ^",
                "Expected {/%"
            );
        }

        // ========== For Statement Errors ==========

        @Test
        @DisplayName("For statement missing identifier")
        void testForMissingIdentifier() {
            assertCompileError("{% for in list %}",
                "{% for in list %}",
                "       ^^",
                "Expected identifier"
            );
        }

        @Test
        @DisplayName("For statement missing 'in' keyword")
        void testForMissingIn() {
            assertCompileError("{% for item list %}",
                "{% for item list %}",
                "            ^^^^",
                "Expected in"
            );
        }

        @Test
        @DisplayName("For statement missing collection")
        void testForMissingCollection() {
            assertCompileError("{% for item in %}",
                "{% for item in %}",
                "               ^^",
                "Unexpected token"
            );
        }

        @Test
        @DisplayName("For statement missing right brace")
        void testForMissingRightBrace() {
            assertCompileError("{% for item in list %}",
                "{% for item in list %}",
                "                      ^",
                "Expected {/%"
            );
        }

        @Test
        @DisplayName("For statement missing end tag")
        void testForMissingEndTag() {
            assertCompileError("{% for item in list %}content",
                "{% for item in list %}content",
                "                             ^",
                "Expected {/%"
            );
        }

        @Test
        @DisplayName("For statement wrong end tag")
        void testForWrongEndTag() {
            assertCompileError("{% for item in list %}content{% endif %}",
                "{% for item in list %}content{% endif %}",
                "                                        ^",
                "Expected {/%"
            );
        }

        @Test
        @DisplayName("For statement invalid comma after single identifier")
        void testForInvalidComma() {
            assertCompileError("{% for item, in list %}",
                "{% for item, in list %}",
                "             ^^",
                "Expected identifier"
            );
        }

        @Test
        @DisplayName("For statement missing second identifier after comma")
        void testForMissingSecondIdentifier() {
            assertCompileError("{% for index, in list %}",
                "{% for index, in list %}",
                "              ^^",
                "Expected identifier"
            );
        }

        // ========== Switch Statement Errors ==========

        @Test
        @DisplayName("Switch statement missing expression")
        void testSwitchMissingExpression() {
            assertCompileError("{% switch %}",
                "{% switch %}",
                "          ^^",
                "Unexpected token"
            );
        }

        @Test
        @DisplayName("Switch statement missing right brace")
        void testSwitchMissingRightBrace() {
            assertCompileError("{% switch x %}",
                "{% switch x %}",
                "              ^",
                "Expected {/%"
            );
        }

        @Test
        @DisplayName("Switch statement missing end tag")
        void testSwitchMissingEndTag() {
            assertCompileError("{% switch x %}content",
                "{% switch x %}content",
                "                     ^",
                "Expected {/%"
            );
        }

        @Test
        @DisplayName("Switch case missing expression")
        void testSwitchCaseMissingExpression() {
            assertCompileError("{% switch x %}{% case %}{% endswitch %}",
                "{% switch x %}{% case %}{% endswitch %}",
                "                      ^^",
                "Unexpected token"
            );
        }

        @Test
        @DisplayName("Switch case missing right brace")
        void testSwitchCaseMissingRightBrace() {
            assertCompileError("{% switch x %}{% case 1 %}content{% endswitch %}",
                "{% switch x %}{% case 1 %}content{% endswitch %}",
                "                        ^^",
                "Expected }"
            );
        }

        @Test
        @DisplayName("Switch default missing right brace")
        void testSwitchDefaultMissingRightBrace() {
            assertCompileError("{% switch x %}{% default %}content{% endswitch %}",
                "{% switch x %}{% default %}content{% endswitch %}",
                "                         ^^",
                "Expected }"
            );
        }

        // ========== Let Statement Errors ==========

        @Test
        @DisplayName("Let statement missing identifier")
        void testLetMissingIdentifier() {
            assertCompileError("{% let = 5 %}",
                "{% let = 5 %}",
                "       ^",
                "Expected identifier"
            );
        }

        @Test
        @DisplayName("Let statement missing assign operator")
        void testLetMissingAssign() {
            assertCompileError("{% let x 5 %}",
                "{% let x 5 %}",
                "         ^",
                "Expected ="
            );
        }

        @Test
        @DisplayName("Let statement missing value")
        void testLetMissingValue() {
            assertCompileError("{% let x = %}",
                "{% let x = %}",
                "           ^^",
                "Unexpected token"
            );
        }

        @Test
        @DisplayName("Let statement if expression without else")
        void testLetIfExprWithoutElse() {
            assertCompileError("{% let x = if (true) { 1 } %}",
                "{% let x = if (true) { 1 } %}",
                "           ^^^^^^^^^^^^^^^",
                "If expression in let assignment must include else branch"
            );
        }

        @Test
        @DisplayName("Let statement if expression with else if but no else")
        void testLetIfExprWithElseIfNoElse() {
            assertCompileError("{% let x = if (false) { 1 } else if (true) { 2 } %}",
                "{% let x = if (false) { 1 } else if (true) { 2 } %}",
                "           ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^",
                "If expression in let assignment must include else branch"
            );
        }

        // ========== Extends/Import Statement Errors ==========

        @Test
        @DisplayName("Extends statement missing string")
        void testExtendsMissingString() {
            assertCompileError("{% extends %}",
                "{% extends %}",
                "           ^^",
                "Expected string"
            );
        }

        @Test
        @DisplayName("Import statement missing identifier")
        void testImportMissingIdentifier() {
            assertCompileError("{% import %}",
                "{% import %}",
                "          ^^",
                "Expected identifier"
            );
        }

        @Test
        @DisplayName("Import statement incomplete dot notation")
        void testImportIncompleteDot() {
            assertCompileError("{% import java. %}",
                "{% import java. %}",
                "                ^^",
                "Expected identifier"
            );
        }

        // ========== Expression Errors ==========

        @Test
        @DisplayName("Binary expression missing right operand")
        void testBinaryMissingRightOperand() {
            assertCompileError("{x + }",
                "{x + }",
                "     ^",
                "Unexpected token"
            );
        }

        @Test
        @DisplayName("Ternary expression missing colon")
        void testTernaryMissingColon() {
            assertCompileError("{x ? y }",
                "{x ? y }",
                "       ^",
                "Expected :"
            );
        }

        @Test
        @DisplayName("Ternary expression missing true expression")
        void testTernaryMissingTrueExpr() {
            assertCompileError("{x ? : y}",
                "{x ? : y}",
                "     ^",
                "Unexpected token"
            );
        }

        @Test
        @DisplayName("Ternary expression missing false expression")
        void testTernaryMissingFalseExpr() {
            assertCompileError("{x ? y : }",
                "{x ? y : }",
                "         ^",
                "Unexpected token"
            );
        }

        @Test
        @DisplayName("Elvis expression missing right operand")
        void testElvisMissingRightOperand() {
            assertCompileError("{x ?? }",
                "{x ?? }",
                "      ^",
                "Unexpected token"
            );
        }

        @Test
        @DisplayName("Method call missing right parenthesis")
        void testMethodCallMissingRightParen() {
            assertCompileError("{func(}",
                "{func(}",
                "      ^",
                "Unexpected token"
            );
        }

        @Test
        @DisplayName("Method call missing comma between arguments")
        void testMethodCallMissingComma() {
            assertCompileError("{func(x y)}",
                "{func(x y)}",
                "        ^",
                "Expected )"
            );
        }

        @Test
        @DisplayName("Method call with invalid target")
        void testMethodCallInvalidTarget() {
            assertCompileError("{5()}",
                "{5()}",
                "  ^",
                "Invalid method call target"
            );
        }

        @Test
        @DisplayName("Member access missing identifier")
        void testMemberAccessMissingIdentifier() {
            assertCompileError("{obj.}",
                "{obj.}",
                "     ^",
                "Expected identifier"
            );
        }

        @Test
        @DisplayName("Safe navigation missing identifier")
        void testSafeNavMissingIdentifier() {
            assertCompileError("{obj?.}",
                "{obj?.}",
                "      ^",
                "Expected identifier"
            );
        }

        @Test
        @DisplayName("Index access missing right bracket")
        void testIndexAccessMissingRightBracket() {
            assertCompileError("{list[}",
                "{list[}",
                "      ^",
                "Unexpected token"
            );
        }

        @Test
        @DisplayName("Index access missing index expression")
        void testIndexAccessMissingIndex() {
            assertCompileError("{list[]}",
                "{list[]}",
                "      ^",
                "Unexpected token"
            );
        }

        @Test
        @DisplayName("Parenthesized expression missing right parenthesis")
        void testParenMissingRightParen() {
            assertCompileError("{(x}",
                "{(x}",
                "   ^",
                "Expected )"
            );
        }

        @Test
        @DisplayName("Empty parentheses")
        void testParenEmpty() {
            assertCompileError("{()}",
                "{()}",
                "  ^",
                "Unexpected token"
            );
        }

        @Test
        @DisplayName("Unexpected token in expression")
        void testUnexpectedTokenInExpression() {
            assertCompileError("{% %}",
                "{% %}",
                "   ^^",
                "Unexpected token"
            );
        }

        // ========== Literal Errors ==========

        @Test
        @DisplayName("List literal missing right bracket")
        void testListMissingRightBracket() {
            assertCompileError("{[1, 2}",
                "{[1, 2}",
                "      ^",
                "Expected ]"
            );
        }

        @Test
        @DisplayName("List literal missing comma between elements")
        void testListMissingComma() {
            assertCompileError("{[1 2]}",
                "{[1 2]}",
                "    ^",
                "Expected ]"
            );
        }

        @Test
        @DisplayName("Hash literal missing right brace")
        void testHashMissingRightBrace() {
            assertCompileError("{{key: value}",
                "{{key: value}",
                "             ^",
                "Unclosed expression"
            );
        }

        @Test
        @DisplayName("Hash literal missing colon")
        void testHashMissingColon() {
            assertCompileError("{{key value}}",
                "{{key value}}",
                "      ^^^^^",
                "Expected }}"
            );
        }

        @Test
        @DisplayName("Hash literal with invalid key")
        void testHashInvalidKey() {
            assertCompileError("{{true: value}}",
                "{{true: value}}",
                "  ^^^^",
                "Invalid hash key"
            );
        }

        @Test
        @DisplayName("Hash literal missing value")
        void testHashMissingValue() {
            assertCompileError("{{key: }}",
                "{{key: }}",
                "       ^",
                "Unexpected token"
            );
        }

        // ========== Structured Expression Errors ==========

        @Test
        @DisplayName("If expression missing left parenthesis")
        void testIfExprMissingLeftParen() {
            assertCompileError("{if true { 1 } else { 2 }}",
                "{if true { 1 } else { 2 }}",
                "    ^^^^",
                "Expected ("
            );
        }

        @Test
        @DisplayName("If expression missing right parenthesis")
        void testIfExprMissingRightParen() {
            assertCompileError("{if (true { 1 } else { 2 }}",
                "{if (true { 1 } else { 2 }}",
                "          ^",
                "Expected )"
            );
        }

        @Test
        @DisplayName("If expression missing left brace")
        void testIfExprMissingLeftBrace() {
            assertCompileError("{if (true) 1 else { 2 }}",
                "{if (true) 1 else { 2 }}",
                "           ^",
                "Expected {"
            );
        }

        @Test
        @DisplayName("If expression missing right brace")
        void testIfExprMissingRightBrace() {
            assertCompileError("{if (true) { 1 else { 2 }}",
                "{if (true) { 1 else { 2 }}",
                "               ^^^^",
                "Unexpected token"
            );
        }

        @Test
        @DisplayName("For expression missing left parenthesis")
        void testForExprMissingLeftParen() {
            assertCompileError("{for item in list { x } }",
                "{for item in list { x } }",
                "     ^^^^",
                "Expected ("
            );
        }

        @Test
        @DisplayName("For expression missing right parenthesis")
        void testForExprMissingRightParen() {
            assertCompileError("{for (item in list { x } }",
                "{for (item in list { x } }",
                "                       ^",
                "Expected :"
            );
        }

        @Test
        @DisplayName("For expression missing left brace")
        void testForExprMissingLeftBrace() {
            assertCompileError("{for (item in list) x }",
                "{for (item in list) x }",
                "                    ^",
                "Expected {"
            );
        }

        @Test
        @DisplayName("For expression missing right brace")
        void testForExprMissingRightBrace() {
            assertCompileError("{for (item in list) { x }",
                "{for (item in list) { x }",
                "                         ^",
                "Unclosed expression"
            );
        }

        @Test
        @DisplayName("Range expression missing right operand")
        void testRangeMissingRightOperand() {
            assertCompileError("{1..}",
                "{1..}",
                "    ^",
                "Unexpected token"
            );
        }
    }

    @Nested
    @DisplayName("Validator Errors")
    class ValidatorErrors {

        @Test
        @DisplayName("Duplicate export")
        void testDuplicateExport() {
            assertCompileError("{% export x %} {% export x %}",
                "{% export x %} {% export x %}",
                "                  ^^^^^^^^",
                "Variable 'x' is already exported"
            );
        }

        @Test
        @DisplayName("Duplicate export with let")
        void testDuplicateExportWithLet() {
            assertCompileError("{% export let x = 1 %}\n{% export let x = 2 %}",
                "{% export let x = 2 %}",
                "   ^^^^^^^^^^^^^^^^",
                "Variable 'x' is already exported"
            );
        }

        @Test
        @DisplayName("Import template alias conflicts with variable")
        void testImportTemplateAliasConflicts() {
            TemplateEngine engineWithLoader = new TemplateEngine(
                new TemplateEngine.TemplateEngineConfig(),
                new ClasspathTemplateLoader()
            );
            TemplateException ex = assertThrows(
                TemplateException.class,
                () -> engineWithLoader.compile("{% let foo = 1 %}\n{% import 'templates/empty.etl' as foo %}"),
                "Should throw TemplateException"
            );
            ErrorPointerAssert.assertErrorPointer(ex,
                "{% import 'templates/empty.etl' as foo %}",
                "   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^",
                "Alias 'foo' conflicts with an existing variable"
            );
        }

        @Test
        @DisplayName("Import template alias already used")
        void testImportTemplateAliasAlreadyUsed() {
            TemplateEngine engineWithLoader = new TemplateEngine(
                new TemplateEngine.TemplateEngineConfig(),
                new ClasspathTemplateLoader()
            );
            TemplateException ex = assertThrows(
                TemplateException.class,
                () -> engineWithLoader.compile("{% import 'templates/empty.etl' as lib %}\n{% import 'templates/empty.etl' as lib %}"),
                "Should throw TemplateException"
            );
            ErrorPointerAssert.assertErrorPointer(ex,
                "{% import 'templates/empty.etl' as lib %}",
                "   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^",
                "Alias 'lib' conflicts with an existing variable"
            );
        }

        @Test
        @DisplayName("Import template not found")
        void testImportTemplateNotFound() {
            TemplateEngine engineWithLoader = new TemplateEngine(
                new TemplateEngine.TemplateEngineConfig(),
                new ClasspathTemplateLoader()
            );
            TemplateException ex = assertThrows(
                TemplateException.class,
                () -> engineWithLoader.compile("{% import 'nonexistent.etl' as lib %}"),
                "Should throw TemplateException"
            );
            ErrorPointerAssert.assertErrorPointer(ex,
                "{% import 'nonexistent.etl' as lib %}",
                "   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^",
                "Cannot import template 'nonexistent.etl'"
            );
        }

        @Test
        @DisplayName("Import template parse failure")
        void testImportTemplateParseFailure() {
            TemplateEngine engineWithLoader = new TemplateEngine(
                new TemplateEngine.TemplateEngineConfig(),
                new ClasspathTemplateLoader()
            );
            TemplateException ex = assertThrows(
                TemplateException.class,
                () -> engineWithLoader.compile("{% import 'templates/broken.etl' as lib %}"),
                "Should throw TemplateException"
            );
            ErrorPointerAssert.assertErrorPointer(ex,
                "{% import 'templates/broken.etl' as lib %}",
                "   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^",
                "Failed to parse imported template 'templates/broken.etl'"
            );
        }

        @Test
        @DisplayName("Java import disabled")
        void testJavaImportDisabled() {
            TemplateEngine engineNoJavaImport = EasyTL.engine()
                .templateJavaClassImportEnabled(false)
                .build();
            TemplateException ex = assertThrows(
                TemplateException.class,
                () -> engineNoJavaImport.compile("{% import java.util.List %}"),
                "Should throw TemplateException"
            );
            ErrorPointerAssert.assertErrorPointer(ex,
                "{% import java.util.List %}",
                "   ^^^^^^^^^^^^^^^^^^^^^",
                "Template Java class import is disabled"
            );
        }
    }

    @Nested
    @DisplayName("Engine Errors")
    class EngineErrors {

        @Test
        @DisplayName("Null template string")
        void testNullTemplateString() {
            TemplateCompileException ex = assertThrows(
                TemplateCompileException.class,
                () -> engine.compile((String) null)
            );
            assertTrue(ex.getMessage().contains("Template string cannot be null"));
        }

        @Test
        @DisplayName("Null input stream")
        void testNullInputStream() {
            TemplateCompileException ex = assertThrows(
                TemplateCompileException.class,
                () -> engine.compile((java.io.InputStream) null)
            );
            assertTrue(ex.getMessage().contains("InputStream cannot be null"));
        }

        @Test
        @DisplayName("Null file")
        void testNullFile() {
            TemplateCompileException ex = assertThrows(
                TemplateCompileException.class,
                () -> engine.compile((File) null)
            );
            assertTrue(ex.getMessage().contains("File cannot be null"));
        }

        @Test
        @DisplayName("File not found")
        void testFileNotFound() {
            TemplateCompileException ex = assertThrows(
                TemplateCompileException.class,
                () -> engine.compile(new File("nonexistent-template-file.etl"))
            );
            assertTrue(ex.getMessage().contains("Template file does not exist"));
        }

        @Test
        @DisplayName("Path is directory")
        void testPathIsDirectory() {
            TemplateCompileException ex = assertThrows(
                TemplateCompileException.class,
                () -> engine.compile(new File("src/test/resources"))
            );
            assertTrue(ex.getMessage().contains("Path is not a file"));
        }
    }

    @Nested
    @DisplayName("Nested Errors")
    class NestedErrors {

        @Test
        @DisplayName("Nested if missing end")
        void testNestedIfMissingEnd() {
            assertCompileError("{% if true %}{% if false %}content{% endif %}",
                "{% if true %}{% if false %}content{% endif %}",
                "                                             ^",
                "Expected {/%"
            );
        }

        @Test
        @DisplayName("Nested for missing end")
        void testNestedForMissingEnd() {
            assertCompileError("{% for x in list %}{% for y in list %}content{% endfor %}",
                "{% for x in list %}{% for y in list %}content{% endfor %}",
                "                                                         ^",
                "Expected {/%"
            );
        }

        @Test
        @DisplayName("Mixed nested missing end")
        void testMixedNestedMissingEnd() {
            assertCompileError("{% if true %}{% for x in list %}content{% endfor %}",
                "{% if true %}{% for x in list %}content{% endfor %}",
                "                                                   ^",
                "Expected {/%"
            );
        }

        @Test
        @DisplayName("Multiline error line 3")
        void testMultilineErrorLine3() {
            assertCompileError("line1\nline2\n{x + }",
                "{x + }",
                "     ^",
                "Unexpected token"
            );
        }

        @Test
        @DisplayName("Multiline error location")
        void testMultilineErrorLocation() {
            assertCompileError("Hello\n{% if true %}\ncontent",
                "content",
                "       ^",
                "Expected {/%"
            );
        }
    }
}