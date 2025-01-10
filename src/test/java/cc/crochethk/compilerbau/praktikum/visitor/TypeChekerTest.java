package cc.crochethk.compilerbau.praktikum.visitor;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;

import cc.crochethk.compilerbau.praktikum.ast.*;
import cc.crochethk.compilerbau.praktikum.ast.BinOpExpr.BinaryOp;
import cc.crochethk.compilerbau.praktikum.ast.UnaryOpExpr.UnaryOp;
import cc.crochethk.compilerbau.praktikum.visitor.TypeChecker.TypeCheckFailedException;
import utils.SourcePos;
import cc.crochethk.compilerbau.praktikum.testhelpers.NodeMocker;

public class TypeChekerTest extends NodeMocker {
    private TypeChecker tc;

    @BeforeEach
    void setUp() {
        tc = new TypeChecker();
    }

    @Nested
    class FunCallTests {
        @Test
        void nullArgForNonRefParamShouldReportErr() {
            var fun = funDef("fun", List.of(param("p1", I64_TN)), I64_TN,
                    List.of(returnStat(i64Lit(42))));
            checkProgOf(List.of(fun), List.of());
            assertReportedErrors(0);

            var funCall = funCall("fun", List.of(NULL_LIT));
            tc.visit(funCall);
            assertReportedErrors(1);
        }

        @Test
        void nullArgForRefParamIsOk() {
            var customType = structDef("CustomType", List.of());
            var fun = funDef("fun", List.of(param("p1", "CustomType", false)), List.of());
            checkProgOf(List.of(fun), List.of(customType));
            assertReportedErrors(0);

            var funCall = funCall("fun", List.of(NULL_LIT));
            tc.visit(funCall);
            assertReportedErrors(0);
        }
    }

    @Nested
    class ConstructorCallTests {
        @Test
        void emptyStruct() {
            var structDef = structDef("Empty", List.of());
            checkProgOf(List.of(), List.of(structDef));
            assertReportedErrors(0);

            var constCall = constructorCall("Empty");
            tc.visit(constCall);
            assertReportedErrors(0);
        }

        // ------------ adjusted copy paste from FunCallTests ------------------
        @Test
        void nullArgForNonRefParamShouldReportErr() {
            var def = structDef("Def", List.of(param("p1", I64_TN)));
            checkProgOf(List.of(), List.of(def));
            assertReportedErrors(0);

            var constCall = constructorCall("Def", List.of(NULL_LIT));
            tc.visit(constCall);
            assertReportedErrors(1);
        }

        @Test
        void nullArgForRefParamIsOk() {
            var customType = structDef("CustomType", List.of());
            var def = structDef("Def", List.of(param("p1", "CustomType", false)));
            checkProgOf(List.of(), List.of(customType, def));
            assertReportedErrors(0);

            var constCall = constructorCall("Def", List.of(NULL_LIT));
            tc.visit(constCall);
            assertReportedErrors(0);
        }
        // ---------------------------------------------------------------------
    }

    @Nested
    class BinOpExprTests {
        @Test
        void nullOperandShouldReportErr_1() {
            var lhs = i64Lit(123);
            var rhs = NULL_LIT;
            var fun = funDef("fun", List.of(), List.of(
                    varDeclareStat("var", I64_TN),
                    varAssignStat("var", binOpExpr(lhs, BinaryOp.add, rhs))));

            assertThrows(TypeCheckFailedException.class, () -> checkProgOf(List.of(fun), List.of()));
            assertReportedErrors(2);
        }

        @Test
        void nullOperandShouldReportErr_2() {
            var lhs = NULL_LIT;
            var rhs = i64Lit(123);
            var fun = funDef("fun", List.of(), List.of(
                    varDeclareStat("var", I64_TN),
                    varAssignStat("var", binOpExpr(lhs, BinaryOp.add, rhs))));

            assertThrows(TypeCheckFailedException.class, () -> checkProgOf(List.of(fun), List.of()));
            assertReportedErrors(3);
        }

        @Test
        void nullOperandShouldReportErr_3() {
            var lhs = NULL_LIT;
            var rhs = NULL_LIT;
            var fun = funDef("fun", List.of(), List.of(
                    varDeclareStat("var", I64_TN),
                    varAssignStat("var", binOpExpr(lhs, BinaryOp.add, rhs))));

            assertThrows(TypeCheckFailedException.class, () -> checkProgOf(List.of(fun), List.of()));
            assertReportedErrors(2);
        }
    }

    @Nested
    class UnaryOpExprTests {
        @Test
        void nullOperandShouldReportErr_arithmeticOp() {
            var operand = NULL_LIT;
            var fun = funDef("fun", List.of(), List.of(
                    varDeclareStat("var", I64_TN),
                    varAssignStat("var", unaryOpExpr(operand, UnaryOp.neg))));
            assertThrows(TypeCheckFailedException.class, () -> checkProgOf(List.of(fun), List.of()));
            assertReportedErrors(2);
        }

        @Test
        void nullOperandShouldReportErr_booleanOp() {
            var operand = NULL_LIT;
            var fun = funDef("fun", List.of(), List.of(
                    varDeclareStat("var", BOOL_TN),
                    varAssignStat("var", unaryOpExpr(operand, UnaryOp.not))));
            assertThrows(TypeCheckFailedException.class, () -> checkProgOf(List.of(fun), List.of()));
            assertReportedErrors(2);
        }
    }

    @Nested
    class TernaryConditionalExprTests {
        @Test
        void nullConditionShouldReportErr() {
            var cond = NULL_LIT;
            var fun = funDef("fun", List.of(param("strVar", STRING_TN)), List.of(
                    varAssignStat("strVar", ternaryConditionalExpr(
                            cond, stringLit("then expr"), stringLit("else expr")))));
            assertThrows(TypeCheckFailedException.class, () -> checkProgOf(List.of(fun), List.of()));
            assertReportedErrors(1);
        }

        @Test
        void nullOkInBranchWithRefType() {
            var cond = boolLit(true);
            var fun = funDef("fun", List.of(param("strVar", STRING_TN)), List.of(
                    varAssignStat("strVar",
                            ternaryConditionalExpr(cond, NULL_LIT, stringLit("else expr")))));
            checkProgOf(List.of(fun), List.of());
            assertReportedErrors(0);
        }
    }

    @Nested
    class VarAssignStatTests {
        @Test
        void nullOnNonRefTypeShouldReportErr() {
            var expr = NULL_LIT;
            var fun = funDef("fun", List.of(param("var", F64_TN)),
                    List.of(varAssignStat("var", expr)));
            assertThrows(TypeCheckFailedException.class, () -> checkProgOf(List.of(fun), List.of()));
            assertReportedErrors(1);
        }

        @Test
        void nullOkIfTargetIsRefType() {
            var expr = NULL_LIT;
            var fun = funDef("fun", List.of(param("strVar", STRING_TN)),
                    List.of(varAssignStat("strVar", expr)));
            checkProgOf(List.of(fun), List.of());
            assertReportedErrors(0);
        }
    }

    @Nested
    class IfElseStatTests {
        @Test
        void nullConditionShouldReportErr() {
            var cond = NULL_LIT;
            var fun = funDef("fun", List.of(), List.of(ifElseStat(cond, statementList(), statementList())));
            assertThrows(TypeCheckFailedException.class, () -> checkProgOf(List.of(fun), List.of()));
            assertReportedErrors(1);
        }

        @Test
        void maxIntUsingIfElse() {
            var fun = maxInt_IfElse_fd();
            var useMaxInt = funCall("maxInt", List.of(i64Lit(-1), i64Lit(2)));
            checkProgOf(List.of(fun), List.of(), useMaxInt);
            assertReportedErrors(0);
        }

        private FunDef maxInt_IfElse_fd() {
            var lhs = var("a");
            var rhs = var("b");
            var params = List.of(param(lhs.name, I64_TN), param(rhs.name, I64_TN));
            var cond = binOpExpr(lhs, BinaryOp.gteq, rhs);
            var then = statementList(returnStat(lhs));
            var otherwise = statementList(returnStat(rhs));
            IfElseStat ifelse = ifElseStat(cond, then, otherwise);
            return funDef("maxInt", params, I64_TN, List.of(ifelse));
        }
    }

    /** Run TypeChecker on new Program consiting of given definitions */
    private void checkProgOf(List<FunDef> funDefs, List<StructDef> structDefs) {
        tc.visit(new Prog(new SourcePos(0, 0), funDefs, null, structDefs));
    }

    /** Run TypeChecker on new Program consiting of given definitions and the entry point call */
    private void checkProgOf(List<FunDef> funDefs, List<StructDef> structDefs, FunCall entryPoint) {
        tc.visit(new Prog(new SourcePos(0, 0), funDefs, entryPoint, structDefs));
    }

    private void assertReportedErrors(int expErrCount) {
        assertEquals(expErrCount, tc.errorsReported);
    }
}
