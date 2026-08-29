package org.dromara.easytl.compiler;

import org.dromara.easytl.EasyTL;
import org.dromara.easytl.TemplateEngine;
import org.dromara.easytl.exception.TemplateCompileException;
import org.dromara.easytl.loader.ClasspathTemplateLoader;
import org.dromara.easytl.testutil.ErrorPointerAssert;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for compile-time validator error location accuracy
 */
class CompilerValidatorLocationTest {

    private final TemplateEngine engine = EasyTL.defaultEngine();

    // Engine with ClasspathTemplateLoader for ImportTemplateValidator tests
    private final TemplateEngine engineWithLoader = new TemplateEngine(
        new TemplateEngine.TemplateEngineConfig(),
        new ClasspathTemplateLoader()
    );

    @Test
    void duplicateExport_simple() {
        String template = "{% export x %} {% export x %}";

        TemplateCompileException ex = assertThrows(
            TemplateCompileException.class,
            () -> engine.compile(template)
        );

        // The pointer starts at column 19 and points to "export x" (8 characters)
        // Column 19 = 18 spaces + 8 carets = startColumn 19 (1-indexed)
        ErrorPointerAssert.assertErrorPointer(ex,
            "{% export x %} {% export x %}",
            "                  ^^^^^^^^",
            "Variable 'x' is already exported"
        );
    }

    @Test
    void duplicateExport_withLet() {
        String template = "{% export let x = 1 %}\n{% export let x = 2 %}";

        TemplateCompileException ex = assertThrows(
            TemplateCompileException.class,
            () -> engine.compile(template)
        );

        // Position: startColumn=4, length=17 (export let x = 2, excluding %})
        ErrorPointerAssert.assertErrorPointer(ex,
            "{% export let x = 2 %}",
            "   ^^^^^^^^^^^^^^^^",
            "Variable 'x' is already exported"
        );
    }

    @Test
    void duplicateExport_multiline() {
        String template = "Hello {% export name %}\nWorld {% export name %}";

        TemplateCompileException ex = assertThrows(
            TemplateCompileException.class,
            () -> engine.compile(template)
        );

        // Position: startColumn=10, endColumn=21, length=11
        ErrorPointerAssert.assertErrorPointer(ex,
            "World {% export name %}",
            "         ^^^^^^^^^^^",
            "Variable 'name' is already exported"
        );
    }

    // ==================== TemplateJavaImportValidator Tests ====================

    @Test
    void javaImport_disabled() {
        String template = "{% import java.util.List %}";

        // Create engine with Java import disabled
        TemplateEngine engineNoJavaImport = EasyTL.engine()
            .templateJavaClassImportEnabled(false)
            .build();

        TemplateCompileException ex = assertThrows(
            TemplateCompileException.class,
            () -> engineNoJavaImport.compile(template)
        );

        // keywordPosition: "import java.util.List" (columns 4-23, length 20)
        ErrorPointerAssert.assertErrorPointer(ex,
            "{% import java.util.List %}",
            "   ^^^^^^^^^^^^^^^^^^^^^",
            "Template Java class import is disabled"
        );
    }

    // ==================== ImportTemplateValidator Tests ====================

    @Test
    void importTemplate_aliasConflictsWithVariable() {
        String template = "{% let foo = 1 %}\n{% import 'templates/empty.etl' as foo %}";

        TemplateCompileException ex = assertThrows(
            TemplateCompileException.class,
            () -> engineWithLoader.compile(template)
        );

        // keywordPosition points to the entire import statement on line 2
        ErrorPointerAssert.assertErrorPointer(ex,
            "{% import 'templates/empty.etl' as foo %}",
            "   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^",
            "Alias 'foo' conflicts with an existing variable"
        );
    }

    @Test
    void importTemplate_aliasAlreadyUsed() {
        String template = "{% import 'templates/empty.etl' as lib %}\n{% import 'templates/empty.etl' as lib %}";

        TemplateCompileException ex = assertThrows(
            TemplateCompileException.class,
            () -> engineWithLoader.compile(template)
        );

        // keywordPosition points to the entire import statement on line 2
        // Note: After first import succeeds, 'lib' is added to localDefinedVariables,
        // so second import triggers "conflicts with an existing variable" error
        ErrorPointerAssert.assertErrorPointer(ex,
            "{% import 'templates/empty.etl' as lib %}",
            "   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^",
            "Alias 'lib' conflicts with an existing variable"
        );
    }

    @Test
    void importTemplate_templateNotFound() {
        String template = "{% import 'nonexistent.etl' as lib %}";

        TemplateCompileException ex = assertThrows(
            TemplateCompileException.class,
            () -> engineWithLoader.compile(template)
        );

        // keywordPosition points to the entire import statement
        ErrorPointerAssert.assertErrorPointer(ex,
            "{% import 'nonexistent.etl' as lib %}",
            "   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^",
            "Cannot import template 'nonexistent.etl'"
        );
    }

    @Test
    void importTemplate_parseFailure() {
        String template = "{% import 'templates/broken.etl' as lib %}";

        TemplateCompileException ex = assertThrows(
            TemplateCompileException.class,
            () -> engineWithLoader.compile(template)
        );

        // keywordPosition points to the entire import statement
        // Note: Error message includes "Failed to parse imported template" prefix
        ErrorPointerAssert.assertErrorPointer(ex,
            "{% import 'templates/broken.etl' as lib %}",
            "   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^",
            "Failed to parse imported template 'templates/broken.etl'"
        );
    }
}