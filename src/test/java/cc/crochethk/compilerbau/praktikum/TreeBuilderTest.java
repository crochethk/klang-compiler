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
