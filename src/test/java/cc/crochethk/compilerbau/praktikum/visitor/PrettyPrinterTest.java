package cc.crochethk.compilerbau.praktikum.visitor;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;

import cc.crochethk.compilerbau.praktikum.ast.*;
import cc.crochethk.compilerbau.praktikum.ast.literal.I64Lit;
import utils.SourcePos;

public class PrettyPrinterTest {
    private static final SourcePos srcPosMock = new SourcePos(-1, -1);
    private PrettyPrinter pp;

    @BeforeEach
    void setUp() {
        pp = new PrettyPrinter();
    }

    @Nested
    class StructDefTests {
        private Parameter param(String paramName, String typeName, boolean isBuiltin) {
            return new Parameter(paramName, new TypeNode(srcPosMock, typeName, isBuiltin));
        }

        @Test
        void emptyStruct() {
            pp.visit(new StructDef(srcPosMock, "FooBar", Collections.emptyList()));
            assertEquals("struct FooBar {}\n", pp.scb.toString());
        }

        @Test
        void oneFieldStruct() {
            pp.visit(new StructDef(srcPosMock, "_Other", List.of(param("a_value", "i64", true))));
            assertEquals("struct _Other {\n  a_value: i64,\n}\n", pp.scb.toString());
        }

        @Test
        void multipleFieldsStruct() {
            pp.visit(new StructDef(srcPosMock, "structuredData", List.of(
                    param("one", "f64", true), param("two", "string", true),
                    param("thr33", "i64", true), param("four", "OtherStruct", false))));
            assertEquals(
                    "struct structuredData {\n  one: f64,\n  two: string,\n  thr33: i64,\n  four: OtherStruct,\n}\n",
                    pp.scb.toString());
        }
    }

    @Nested
    class LoopStatTests {
        @Test
        void testEmptyBody() {
            pp.visit(new LoopStat(srcPosMock,
                    new StatementList(srcPosMock, Collections.emptyList())));
            assertEquals("loop {}", pp.scb.toString());
        }

        @Test
        void testSingleStatment() {
            pp.visit(new LoopStat(srcPosMock,
                    new StatementList(srcPosMock, List.of(
                            new ReturnStat(srcPosMock, new I64Lit(srcPosMock, 42, false))))));
            assertEquals("loop {\n  return 42;\n}",
                    pp.scb.toString());
        }

        @Test
        void testMultiStatment() {
            var assStat = new VarAssignStat(srcPosMock,
                    "counter", new BinOpExpr(srcPosMock, new Var(srcPosMock, "counter"),
                            BinOpExpr.BinaryOp.add, new I64Lit(srcPosMock, 1, false)));
            var ifelse = new IfElseStat(srcPosMock,
                    new FunCall(srcPosMock, "maxReached", Collections.emptyList()),
                    new BreakStat(srcPosMock), new EmptyNode(srcPosMock));

            pp.visit(new LoopStat(srcPosMock, new StatementList(srcPosMock, List.of(assStat, ifelse))));
            assertEquals("loop {\n  counter = (counter + 1);\n  if maxReached() {\n    break;\n  } else {}\n}",
                    pp.scb.toString());
        }
    }
}
