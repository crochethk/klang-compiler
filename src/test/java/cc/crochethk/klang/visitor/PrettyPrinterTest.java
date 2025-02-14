package cc.crochethk.klang.visitor;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;

import cc.crochethk.klang.ast.*;
import cc.crochethk.klang.ast.BinOpExpr.BinaryOp;
import cc.crochethk.klang.testhelpers.NodeMocker;

public class PrettyPrinterTest extends NodeMocker {
    private PrettyPrinter pp;

    @BeforeEach
    void setUp() {
        pp = new PrettyPrinter();
    }

    @Nested
    class LiteralExprTests {
        @Test
        void nullLit_isolated() {
            pp.visit(NULL_LIT);
            assertEquals("null", pp.scb.toString());
        }

        @Test
        void nullLit_inStatement() {
            var stat = varAssignStat("foo", NULL_LIT);
            pp.visit(stat);
            assertEquals("foo = null;", pp.scb.toString());
        }
    }

    @Nested
    class MemberAccessChainTests {
        @Test
        void varThenSingleFieldChain() {
            var maChain = memberAccessChain(var("var"), fieldGet("field"));
            pp.visit(maChain);
            assertEquals("var.field", pp.scb.toString());
        }

        @Test
        void varThenMultipleFieldsChain() {
            var maChain = memberAccessChain(var("var"), fieldGet("field1"),
                    fieldGet("field2"), fieldGet("field3"));
            pp.visit(maChain);
            assertEquals("var.field1.field2.field3", pp.scb.toString());
        }

        @Test
        void funCallThenSingleFieldChain() {
            var maChain = memberAccessChain(funCall("fun"), fieldGet("field"));
            pp.visit(maChain);
            assertEquals("fun().field", pp.scb.toString());
        }
    }

    @Nested
    class ConstructorCallTests {
        @Test
        void emptyStruct_emptyArgsList() {
            var constCall = constructorCall("EmptyStruct", List.of());
            pp.visit(constCall);
            assertEquals("EmptyStruct{}", pp.scb.toString());
        }

        @Test
        void emptyStruct_nullArgs_unlikely() {
            var constCall = constructorCall("EmptyStruct", null);
            pp.visit(constCall);
            assertEquals("EmptyStruct{}", pp.scb.toString());
        }

        @Test
        void emptyStruct_insideStatement() {
            var constCall = constructorCall("EmptyStruct", List.of());
            var stat = varAssignStat("empty", constCall);
            pp.visit(stat);
            assertEquals("empty = EmptyStruct{};", pp.scb.toString());
        }

        @Test
        void withMultipleArgs() {
            var constCall = constructorCall("MultiField", List.of(
                    i64Lit(-42),
                    stringLit("bar"),
                    f64Lit(4.2d)));
            pp.visit(constCall);
            assertEquals("MultiField{-42, \"bar\", 4.2}", pp.scb.toString());
        }

        @Test
        void withSingleArg_insideStatement() {
            var constCall = constructorCall("OneField", List.of(i64Lit(123)));
            var stat = varAssignStat("onefield", constCall);
            pp.visit(stat);
            assertEquals("onefield = OneField{123};", pp.scb.toString());
        }
    }

    @Nested
    class StructDefTests {
        @Test
        void emptyStruct() {
            pp.visit(structDef("FooBar", List.of()));
            assertEquals("struct FooBar {}", pp.scb.toString());
        }

        @Test
        void oneFieldStruct() {
            pp.visit(structDef("_Other", List.of(param("a_value", I64_TN))));
            assertEquals("struct _Other {\n  a_value: i64,\n}", pp.scb.toString());
        }

        @Test
        void multipleFieldsStruct() {
            pp.visit(structDef("structuredData", List.of(
                    param("one", F64_TN), param("two", STRING_TN),
                    param("thr33", I64_TN), param("four", "OtherStruct"))));
            assertEquals("""
                    struct structuredData {
                      one: f64,
                      two: string,
                      thr33: i64,
                      four: OtherStruct,
                    }""", pp.scb.toString());
        }
    }

    @Nested
    class FunDefTests {
        @Test
        void emptyParamsAndBody() {
            pp.visit(funDef("fun_1", List.of(), VOID_TN, List.of()));
            assertEquals("fn fun_1() {}", pp.scb.toString());
        }

        @Test
        void oneParamWithReturn() {
            pp.visit(funDef("fun_2", List.of(param("p1", I64_TN)), I64_TN,
                    List.of(returnStat(i64Lit(42)))));
            assertEquals("fn fun_2(p1: i64) -> i64 {\n  return 42;\n}", pp.scb.toString());
        }

        @Test
        void multipleParamsAndStatements() {
            pp.visit(funDef("fun_3", List.of(
                    param("p1", I64_TN),
                    param("p2", "CustomType"),
                    param("p3", STRING_TN)),
                    VOID_TN,
                    List.of(
                            varAssignStat("p1", binOpExpr(var("p1"), BinaryOp.add, i64Lit(1))),
                            returnStat(emptyNode()))));
            assertEquals("""
                    fn fun_3(p1: i64, p2: CustomType, p3: string) {
                      p1 = (p1 + 1);
                      return;
                    }""", pp.scb.toString());
        }
    }

    @Nested
    class FieldAssignStatTests {
        @Test
        void varThenSingleFieldAssignLit() {
            var stat = fieldAssignStat(var("var"), List.of(fieldSet("field")), i64Lit(1234));
            pp.visit(stat);
            assertEquals("var.field = 1234;", pp.scb.toString());
        }

        @Test
        void varThenMultipleFieldsAssignLit() {
            var stat = fieldAssignStat(var("var"), List.of(
                    fieldGet("field1"), fieldGet("field2"),
                    fieldSet("field3")), i64Lit(1234));
            pp.visit(stat);
            assertEquals("var.field1.field2.field3 = 1234;", pp.scb.toString());
        }

        @Test
        void funCallThenSingleFieldAssignOtherField() {
            var stat = fieldAssignStat(
                    funCall("fun"), List.of(fieldSet("field")),
                    memberAccessChain(var("foo"), fieldGet("bar")));
            pp.visit(stat);
            assertEquals("fun().field = foo.bar;", pp.scb.toString());
        }
    }

    @Nested
    class LoopStatTests {
        @Test
        void testEmptyBody() {
            pp.visit(loopStat(statementList()));
            assertEquals("loop {}", pp.scb.toString());
        }

        @Test
        void testSingleStatment() {
            pp.visit(loopStat(statementList(returnStat(i64Lit(42)))));
            assertEquals("loop {\n  return 42;\n}", pp.scb.toString());
        }

        @Test
        void testMultiStatment() {
            var assStat = varAssignStat("counter",
                    binOpExpr(var("counter"), BinOpExpr.BinaryOp.add, i64Lit(1)));
            var ifelse = ifElseStat(
                    funCall("maxReached", List.of()), statementList(breakStat()), statementList());

            pp.visit(loopStat(statementList(assStat, ifelse)));
            assertEquals("""
                    loop {
                      counter = (counter + 1);
                      if maxReached() {
                        break;
                      } else {}
                    }""", pp.scb.toString());
        }
    }

    @Nested
    class DropStatTests {
        @Test
        void testDropStatement() {
            var varName = "theStructInstanceReference";
            var dropStat = dropStat(varName);
            pp.visit(dropStat);
            assertEquals("drop " + varName + ";", pp.scb.toString());
        }
    }
}
