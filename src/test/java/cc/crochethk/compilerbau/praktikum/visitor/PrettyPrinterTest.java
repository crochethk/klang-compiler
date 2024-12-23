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
        System.out.println("test");
    }

    @Nested
    class StructDefTests {
        private Parameter param(String paramName, String typeName) {
            return new Parameter(paramName, new TypeNode(srcPosMock, typeName));
        }

        @Test
        void emptyStruct() {
            assertEquals("struct FooBar {}\n", pp.visit(new StructDef(srcPosMock,
                    "FooBar", Collections.emptyList())).toString());
        }

        @Test
        void oneFieldStruct() {
            assertEquals("struct _Other {\n  a_value: i64,\n}\n", pp.visit(
                    new StructDef(srcPosMock, "_Other", List.of(param("a_value", "i64"))))
                    .toString());
        }

        @Test
        void multipleFieldsStruct() {
            assertEquals(
                    "struct structuredData {\n  one: f64,\n  two: string,\n  thr33: i64,\n  four: OtherStruct,\n}\n",
                    pp.visit(new StructDef(srcPosMock, "structuredData", List.of(
                            param("one", "f64"), param("two", "string"),
                            param("thr33", "i64"), param("four", "OtherStruct")))).toString());
        }
    }

    @Nested
    class LoopStatTests {
        @Test
        void testEmptyBody() {
            assertEquals("loop {}", pp.visit(new LoopStat(srcPosMock,
                    new StatementList(srcPosMock, Collections.emptyList()))).toString());
        }

        @Test
        void testSingleStatment() {
            assertEquals("loop {\n  return 42;\n}", pp.visit(new LoopStat(srcPosMock,
                    new StatementList(srcPosMock, List.of(
                            new ReturnStat(srcPosMock, new I64Lit(srcPosMock, 42, false))))))
                    .toString());
        }

        @Test
        void testMultiStatment() {
            var assStat = new VarAssignStat(srcPosMock,
                    "counter",
                    new BinOpExpr(srcPosMock,
                            new Var(srcPosMock, "counter"),
                            BinOpExpr.BinaryOp.add,
                            new I64Lit(srcPosMock, 1, false)));
            var ifelse = new IfElseStat(srcPosMock,
                    new FunCall(srcPosMock, "maxReached", Collections.emptyList()),
                    new BreakStat(srcPosMock),
                    new EmptyNode(srcPosMock));
            assertEquals("loop {\n  counter = (counter + 1);\n  if maxReached() {\n    break;\n  } else {}\n}",
                    pp.visit(new LoopStat(srcPosMock, new StatementList(srcPosMock, List.of(
                            assStat, ifelse))))
                            .toString());
        }
    }
}
