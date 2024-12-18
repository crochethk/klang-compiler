package cc.crochethk.compilerbau.praktikum;

import static org.junit.jupiter.api.Assertions.*;

import java.util.function.Function;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import cc.crochethk.compilerbau.praktikum.TreeBuilder.*;
import cc.crochethk.compilerbau.praktikum.antlr.*;
import cc.crochethk.compilerbau.praktikum.ast.*;
import cc.crochethk.compilerbau.praktikum.ast.literal.*;
import utils.SourcePos;

public class TreeBuilderTest {
    private TreeBuilder treeBuilder;

    @BeforeEach
    public void setUp() {
        treeBuilder = new TreeBuilder();
    }

    private <C extends ParserRuleContext> C parse(String s, Function<L1Parser, C> contextGetter) {
        var lexer = new L1Lexer(CharStreams.fromString(s));
        return contextGetter.apply(new L1Parser(new CommonTokenStream(lexer)));
    }

    private SourcePos srcPos(Node src) {
        return new SourcePos(src.line, src.column);
    }

    @Nested
    class ExitStringTests {
        @Test
        public void buildRegularStringLit() {
            var ctx = parse("\"hello world\"", p -> p.string());
            treeBuilder.exitString(ctx);
            assertEquals(new StringLit(srcPos(ctx.result), "hello world"), ctx.result);
        }

        @Test
        public void buildEmptyStringLit() {
            var ctx = parse("\"\"", p -> p.string());
            treeBuilder.exitString(ctx);
            assertEquals(new StringLit(srcPos(ctx.result), ""), ctx.result);
        }

        @Test
        public void buildWhitespaceStringLit() {
            var ctx = parse("\" \n\"", p -> p.string());
            treeBuilder.exitString(ctx);
            assertEquals(new StringLit(srcPos(ctx.result), " \n"), ctx.result);
        }

        @Test
        public void escapedQuotationMark() {
            var ctx = parse("\"escaped quote \\\" inside string\"", p -> p.string());
            treeBuilder.exitString(ctx);
            assertEquals(new StringLit(srcPos(ctx.result), "escaped quote \" inside string"), ctx.result);
        }

        @Test
        public void escapedBackslash() {
            var ctx = parse("\"escaped \\\\Backslash\"", p -> p.string());
            treeBuilder.exitString(ctx);
            assertEquals(new StringLit(srcPos(ctx.result), "escaped \\Backslash"), ctx.result);
        }

        @Test
        public void escapedBackslashesInSequence() {
            var ctx = parse("\"escaped \\\\\\\\Backslashes\"", p -> p.string());
            treeBuilder.exitString(ctx);
            assertEquals(new StringLit(srcPos(ctx.result), "escaped \\\\Backslashes"), ctx.result);
        }

        @Test
        public void escapedBackslashThenEscapedRegularCharacter() {
            var ctx = parse("\"backslash removes 'B': \\\\\\Backslash\"", p -> p.string());
            treeBuilder.exitString(ctx);
            assertEquals(new StringLit(srcPos(ctx.result), "backslash removes 'B': \\ackslash"), ctx.result);
        }

        @Test
        public void escapedBackslashThenEscapedQuote() {
            var ctx = parse("\"literal backslash, then literal quote: \\\\\\\"\"", p -> p.string());
            treeBuilder.exitString(ctx);
            assertEquals(new StringLit(srcPos(ctx.result), "literal backslash, then literal quote: \\\""), ctx.result);
        }

        @Test
        public void mixedEscapeSequences() {
            var ctx = parse("\"escaped \\\\\\quote \\\" inside\\ String\"", p -> p.string());
            treeBuilder.exitString(ctx);
            assertEquals(new StringLit(srcPos(ctx.result), "escaped \\uote \" insideString"), ctx.result);
        }
    }

    @Nested
    class ExitBoolTests {
        @Test
        public void buildBoolLitTrue() {
            var ctx = parse("true", p -> p.bool());
            treeBuilder.exitBool(ctx);
            assertEquals(new BoolLit(srcPos(ctx.result), true), ctx.result);
        }

        @Test
        public void buildBoolLitFalse() {
            var ctx = parse("false", p -> p.bool());
            treeBuilder.exitBool(ctx);
            assertEquals(new BoolLit(srcPos(ctx.result), false), ctx.result);
        }
    }

    @Nested
    class ExitNumberTests {
        @Test
        public void buildI64Lit() {
            var ctx = parse("123", p -> p.number());
            treeBuilder.exitNumber(ctx);
            assertEquals(new I64Lit(srcPos(ctx.result), 123, false), ctx.result);
        }

        @Test
        public void buildI64LitWithTypeAnnot() {
            var ctx = parse("123 as i64", p -> p.number());
            treeBuilder.exitNumber(ctx);
            assertEquals(new I64Lit(srcPos(ctx.result), 123, true), ctx.result);
        }

        @Test
        public void buildF64Lit() {
            var ctx = parse("1.23", p -> p.number());
            treeBuilder.exitNumber(ctx);
            assertEquals(new F64Lit(srcPos(ctx.result), 1.23d, false), ctx.result);
        }

        @Test
        public void buildF64LitWithTypeAnnot() {
            var ctx = parse("1.23 as f64", p -> p.number());
            treeBuilder.exitNumber(ctx);
            assertEquals(new F64Lit(srcPos(ctx.result), 1.23d, true), ctx.result);
        }

        @Test
        public void buildF64LitWithTypeAnnotFromI64() {
            var ctx = parse("123 as f64", p -> p.number());
            treeBuilder.exitNumber(ctx);
            assertEquals(new F64Lit(srcPos(ctx.result), 123.0d, true), ctx.result);
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
