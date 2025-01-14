package cc.crochethk.compilerbau.praktikum;

import static org.junit.jupiter.api.Assertions.*;

import java.util.function.Function;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.ParserRuleContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import cc.crochethk.compilerbau.praktikum.TreeBuilder.*;
import cc.crochethk.compilerbau.praktikum.antlr.*;
import cc.crochethk.compilerbau.praktikum.ast.literal.*;
import cc.crochethk.compilerbau.praktikum.testhelpers.NodeMocker;
import utils.SourcePos;

public class TreeBuilderTest extends NodeMocker {
    private TreeBuilder treeBuilder;

    @BeforeEach
    public void setUp() {
        treeBuilder = new TreeBuilderTestHelper();
    }

    private class TreeBuilderTestHelper extends TreeBuilder {
        @Override
        protected SourcePos getSourcePos(ParserRuleContext ctx) {
            // Mock source code position for built nodes.
            return srcPosMock;
        }
    }

    private <C extends ParserRuleContext> C parse(String s, Function<KlangParser, C> contextGetter) {
        var lexer = KlangCompiler.buildLexer(CharStreams.fromString(s));
        return contextGetter.apply(KlangCompiler.buildParser(lexer));
    }

    @Test
    void testExitNullLit() {
        var ctx = parse("null", p -> p.nullLit());
        treeBuilder.exitNullLit(ctx);
        assertEquals(NULL_LIT, ctx.result);
    }

    @Test
    void testLiteralExprEqualsDoesNotThrowWithNullValueField() {
        var nullLit = NULL_LIT;
        var nonNullNullLit = new NullLit(NULL_LIT.srcPos);
        nonNullNullLit.value = new Object();

        assertDoesNotThrow(() -> assertFalse(nullLit.equals(nonNullNullLit)));
        assertDoesNotThrow(() -> assertFalse(nonNullNullLit.equals(nullLit)));
        assertDoesNotThrow(() -> assertTrue(nullLit.equals(nullLit)));
        assertDoesNotThrow(() -> assertTrue(nonNullNullLit.equals(nonNullNullLit)));
    }

    @Nested
    class ExitStringTests {
        @Test
        void testResolveEscapeSequences_basic() {
            var tb = treeBuilder;
            assertEquals("This is a \"quote\"", tb.resolveEscapeSequences("This is a \\\"quote\\\""));
            assertEquals("\\path\\to\\file", tb.resolveEscapeSequences("\\\\path\\\\to\\\\file"));
            assertEquals("UnsupportedEscape", tb.resolveEscapeSequences("Unsupported\\xEscape"));
            assertEquals("escaped nl:\n, tab:\t, cr: \r",
                    tb.resolveEscapeSequences("escaped nl:\\n, tab:\\t, cr: \\r"));
        }

        @Test
        public void testResolveEscapeSequences_evilCombinations() {
            var tb = treeBuilder;
            assertEquals("2 backslashes: \\", tb.resolveEscapeSequences("2 backslashes: \\\\"));
            assertEquals("literal backslash, then literal quote: \\\"", tb.resolveEscapeSequences(
                    "literal backslash, then literal quote: \\\\\\\""));
            assertEquals("UnsupportedEscape", tb.resolveEscapeSequences("Unsupported\\xEscape"));
            assertEquals("escaped nl:\n, tab:\t, cr: \r",
                    tb.resolveEscapeSequences("escaped nl:\\n, tab:\\t, cr: \\r"));
            assertEquals("not a new line: \\n", tb.resolveEscapeSequences("not a new line: \\\\n"));
        }

        @Test
        public void buildWithMixedEscapeSequences() {
            var ctx = parse("\"esc. \\\\\\quote \\\" inside\\n String\"", p -> p.string());
            treeBuilder.exitString(ctx);
            assertEquals(stringLit("esc. \\uote \" inside\n String"), ctx.result);
        }

        @Test
        public void buildBasicStringLit() {
            var ctx = parse("\"hello world\"", p -> p.string());
            treeBuilder.exitString(ctx);
            assertEquals(stringLit("hello world"), ctx.result);
        }

        @Test
        public void buildEmptyStringLit() {
            var ctx = parse("\"\"", p -> p.string());
            treeBuilder.exitString(ctx);
            assertEquals(stringLit(""), ctx.result);
        }

        @Test
        public void buildMultiLineStringLit() {
            var ctx = parse("\"Hello\nWorld\"", p -> p.string());
            treeBuilder.exitString(ctx);
            assertEquals(stringLit("Hello\nWorld"), ctx.result);
        }

        @Test
        public void buildEmptyMultiLineStringLit() {
            var ctx = parse("\"\n\"", p -> p.string());
            treeBuilder.exitString(ctx);
            assertEquals(stringLit("\n"), ctx.result);
        }

        @Test
        public void buildUnescapedWhitespaceStringLit() {
            var ctx = parse("\" \r\t\n\"", p -> p.string());
            treeBuilder.exitString(ctx);
            assertEquals(stringLit(" \r\t\n"), ctx.result);
        }

    }

    @Nested
    class ExitBoolTests {
        @Test
        public void buildBoolLitTrue() {
            var ctx = parse("true", p -> p.bool());
            treeBuilder.exitBool(ctx);
            assertEquals(boolLit(true), ctx.result);
        }

        @Test
        public void buildBoolLitFalse() {
            var ctx = parse("false", p -> p.bool());
            treeBuilder.exitBool(ctx);
            assertEquals(boolLit(false), ctx.result);
        }
    }

    @Nested
    class ExitNumberTests {
        @Test
        public void buildI64Lit() {
            var ctx = parse("123", p -> p.number());
            treeBuilder.exitNumber(ctx);
            assertEquals(i64Lit(123, false), ctx.result);
        }

        @Test
        public void buildI64LitWithTypeAnnot() {
            var ctx = parse("123 as i64", p -> p.number());
            treeBuilder.exitNumber(ctx);
            assertEquals(i64Lit(123, true), ctx.result);
        }

        @Test
        public void buildF64Lit() {
            var ctx = parse("1.23", p -> p.number());
            treeBuilder.exitNumber(ctx);
            assertEquals(f64Lit(1.23d, false), ctx.result);
        }

        @Test
        public void buildF64LitWithTypeAnnot() {
            var ctx = parse("1.23 as f64", p -> p.number());
            treeBuilder.exitNumber(ctx);
            assertEquals(f64Lit(1.23d, true), ctx.result);
        }

        @Test
        public void buildF64LitWithTypeAnnotFromI64() {
            var ctx = parse("123 as f64", p -> p.number());
            treeBuilder.exitNumber(ctx);
            assertEquals(f64Lit(123.0d, true), ctx.result);
        }

        @Test
        public void castFloatLiteralToInteger_throwsIllegalLiteralTypeAnnotException() {
            var ctx = parse("123.456 as i64", p -> p.number());
            Exception exception = assertThrows(IllegalLiteralTypeAnnotException.class, () -> {
                treeBuilder.exitNumber(ctx);
            });
            assertTrue(exception.getMessage().contains("Illegal type suffix"));
        }
    }
}
