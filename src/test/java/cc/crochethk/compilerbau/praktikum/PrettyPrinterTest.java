package cc.crochethk.compilerbau.praktikum;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import cc.crochethk.compilerbau.praktikum.ast.*;
import cc.crochethk.compilerbau.praktikum.ast.literals.I64Lit;
import utils.SourcePos;

import org.junit.jupiter.api.Nested;

public class PrettyPrinterTest {
    @Nested
    class LoopStatTests {
        private static final SourcePos DUMMY_POS = new SourcePos(-1, -1);
        private PrettyPrinter pp = new PrettyPrinter();

        @Test
        void testEmptyBody() {
            assertEquals("loop {}", pp.visit(new LoopStat(DUMMY_POS,
                    new StatementList(DUMMY_POS, Collections.emptyList()))).toString());
        }

        @Test
        void testSingleStatment() {
            assertEquals("loop {\n  return 42;\n}", pp.visit(new LoopStat(DUMMY_POS,
                    new StatementList(DUMMY_POS, List.of(
                            new ReturnStat(DUMMY_POS, new I64Lit(DUMMY_POS, 42, false))))))
                    .toString());
        }

        @Test
        void testMultiStatment() {
            var assStat = new VarAssignStat(DUMMY_POS,
                    "counter",
                    new BinOpExpr(DUMMY_POS,
                            new Var(DUMMY_POS, "counter"),
                            BinOpExpr.BinaryOp.add,
                            new I64Lit(DUMMY_POS, 1, false)));
            var ifelse = new IfElseStat(DUMMY_POS,
                    new FunCall(DUMMY_POS, "maxReached", Collections.emptyList()),
                    new BreakStat(DUMMY_POS),
                    new EmptyNode(DUMMY_POS));
            assertEquals("loop {\n  counter = (counter + 1);\n  if maxReached() {\n    break;\n  } else {}\n}",
                    pp.visit(new LoopStat(DUMMY_POS, new StatementList(DUMMY_POS, List.of(
                            assStat, ifelse))))
                            .toString());
        }
    }
}
