package cc.crochethk.klang;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.function.Function;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.InputMismatchException;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import cc.crochethk.klang.antlr.*;
import cc.crochethk.klang.ast.BinOpExpr.BinaryOp;
import cc.crochethk.klang.ast.MemberAccess.*;
import cc.crochethk.klang.ast.UnaryOpExpr.UnaryOp;
import cc.crochethk.klang.ast.literal.*;
import cc.crochethk.klang.testhelpers.NodeMocker;
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

    private <C extends ParserRuleContext> C parseAndWalk(String s, Function<KlangParser, C> contextGetter) {
        var lexer = KlangCompiler.buildLexer(CharStreams.fromString(s));
        var ctx = contextGetter.apply(KlangCompiler.buildParser(lexer));
        ParseTreeWalker.DEFAULT.walk(treeBuilder, ctx);
        return ctx;
    }

    @Test
    void testExitNullLit() {
        var ctx = parseAndWalk("null", p -> p.nullLit());
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
        public void buildWithMixedEscapeSequences() {
            var ctx = parseAndWalk("\"esc. \\\\\\quote \\\" inside\\n String\"", p -> p.string());
            assertEquals(stringLit("esc. \\uote \" inside\n String"), ctx.result);
        }

        @Test
        public void buildBasicStringLit() {
            var ctx = parseAndWalk("\"hello world\"", p -> p.string());
            assertEquals(stringLit("hello world"), ctx.result);
        }

        @Test
        public void buildEmptyStringLit() {
            var ctx = parseAndWalk("\"\"", p -> p.string());
            assertEquals(stringLit(""), ctx.result);
        }

        @Test
        public void buildMultiLineStringLit() {
            var ctx = parseAndWalk("\"Hello\nWorld\"", p -> p.string());
            assertEquals(stringLit("Hello\nWorld"), ctx.result);
        }

        @Test
        public void buildEmptyMultiLineStringLit() {
            var ctx = parseAndWalk("\"\n\"", p -> p.string());
            assertEquals(stringLit("\n"), ctx.result);
        }

        @Test
        public void buildUnescapedWhitespaceStringLit() {
            var ctx = parseAndWalk("\" \r\t\n\"", p -> p.string());
            assertEquals(stringLit(" \r\t\n"), ctx.result);
        }

    }

    @Nested
    class ExitBoolTests {
        @Test
        public void buildBoolLitTrue() {
            var ctx = parseAndWalk("true", p -> p.bool());
            assertEquals(boolLit(true), ctx.result);
        }

        @Test
        public void buildBoolLitFalse() {
            var ctx = parseAndWalk("false", p -> p.bool());
            assertEquals(boolLit(false), ctx.result);
        }
    }

    @Nested
    class ExitNumberTests {
        @Test
        public void buildI64Lit() {
            var ctx = parseAndWalk("123", p -> p.number());
            assertEquals(i64Lit(123), ctx.result);
        }

        @Test
        public void buildF64Lit() {
            var ctx = parseAndWalk("1.23", p -> p.number());
            assertEquals(f64Lit(1.23d), ctx.result);
        }
    }

    @Nested
    class ExitExprTests {
        @Test
        void notOperatorHasHighPrecedence_1() {
            var ctx = parseAndWalk("!a && b", p -> p.expr());
            assertEquals(binOpExpr(unaryOpExpr(var("a"), UnaryOp.not),
                    BinaryOp.and, var("b")), ctx.result);
        }

        @Test
        void notOperatorHasHighPrecedence_2() {
            var ctx = parseAndWalk("!a + b", p -> p.expr());
            assertEquals(binOpExpr(unaryOpExpr(var("a"), UnaryOp.not),
                    BinaryOp.add, var("b")), ctx.result);
        }
    }

    @Nested
    class ExitVarOrFunCallTests {
        @Test
        void buildFunCallNoArgs() {
            var ctx = parseAndWalk("foo()", p -> p.varOrFunCall());
            assertEquals(funCall("foo"), ctx.result);
        }

        @Test
        void buildFunCallWithArgs() {
            var ctx = parseAndWalk("foo(bar, 123, baz())", p -> p.varOrFunCall());
            assertEquals(funCall("foo", List.of(
                    var("bar"), i64Lit(123), funCall("baz"))), ctx.result);
        }

        @Test
        void buildVar() {
            var ctx = parseAndWalk("foo", p -> p.varOrFunCall());
            assertEquals(var("foo"), ctx.result);
        }
    }

    @Nested
    class ExitStatementTests {
        // TODO Test other alternatives...

        @Test
        public void buildDropStat() {
            var ctx = parseAndWalk("drop someVarName;", p -> p.statement());
            assertEquals(dropStat("someVarName"), ctx.result);
        }

        /** This is actually more of a parser test... */
        @Test
        void buildDropWithoutNamedVarShouldThrow() {
            var _ = assertThrows(InputMismatchException.class, () -> {
                System.out.println("---Test: buildDropWithoutNamedVarShouldThrow");
                parseAndWalk("drop getSomeRef();", p -> p.statement());
            });
        }
    }

    @Nested
    class FieldAssignStatTests {
        @Test
        public void lastMemberAccessIsSetter() {
            var ctx = parseAndWalk("some.field=value;", p -> p.structFieldAssignStat());
            assertEquals(fieldAssignStat(var("some"), List.of(fieldSet("field")), var("value")), ctx.result);
            assertTrue(ctx.result.maChain.getLast() instanceof FieldSet);
            assertFalse(ctx.result.maChain.getLast() instanceof FieldGet);
        }

        @Test
        public void multipleChainedFields() {
            var ctx = parseAndWalk("some.field1.field2.field3=value;", p -> p.structFieldAssignStat());
            assertEquals(fieldAssignStat(
                    var("some"), List.of(fieldGet("field1"), fieldGet("field2"), fieldSet("field3")),
                    var("value")), ctx.result);
            assertTrue(ctx.result.maChain.getLast() instanceof FieldSet);
            assertFalse(ctx.result.maChain.getLast() instanceof FieldGet);
        }

        @Test
        public void assignedExprHasFieldGetAndIsNotSetter() {
            var ctx = parseAndWalk("some.field=some.field+1;", p -> p.structFieldAssignStat());
            var lhs = memberAccessChain(var("some"), fieldGet("field"));
            var rhs = i64Lit(1);
            assertEquals(fieldAssignStat(var("some"), List.of(fieldSet("field")),
                    binOpExpr(lhs, BinaryOp.add, rhs)),
                    ctx.result);
            assertTrue(ctx.result.maChain.getLast() instanceof FieldSet);
            assertFalse(ctx.result.maChain.getLast() instanceof FieldGet);
        }
    }

    @Nested
    class ExitMemberAccessorTests {
        @Test
        public void varThenSingleFieldChain() {
            var ctx = parseAndWalk("var.field", p -> p.memberAccessor());
            assertEquals(memberAccessChain(var("var"), fieldGet("field")), ctx.result);
        }

        @Test
        public void varThenMultipleFieldsChain() {
            var ctx = parseAndWalk("var.field1.field2.field3", p -> p.memberAccessor());
            assertEquals(memberAccessChain(
                    var("var"), fieldGet("field1"), fieldGet("field2"), fieldGet("field3")),
                    ctx.result);
        }

        @Test
        public void funCallThenSingleFieldChain() {
            var ctx = parseAndWalk("fun().field", p -> p.memberAccessor());
            assertNotEquals(memberAccessChain(funCall("var"), fieldGet("field")), ctx.result);
            assertEquals(memberAccessChain(funCall("fun"), fieldGet("field")), ctx.result);
        }
    }
}
