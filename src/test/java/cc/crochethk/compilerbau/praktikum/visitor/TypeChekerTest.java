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
            registerDefinitions(List.of(fun), List.of());
            assertReportedErrors(0);

            var funCall = funCall("fun", List.of(NULL_LIT));
            tc.visit(funCall);
            assertReportedErrors(1);
        }

        @Test
        void nullArgForRefParamIsOk() {
            var customType = structDef("CustomType", List.of());
            var fun = funDef("fun", List.of(param("p1", "CustomType", false)), List.of());
            registerDefinitions(List.of(fun), List.of(customType));
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
            registerDefinitions(List.of(), List.of(structDef));
            assertReportedErrors(0);

            var constCall = constructorCall("Empty");
            tc.visit(constCall);
            assertReportedErrors(0);
        }

        // ------------ adjusted copy paste from FunCallTests ------------------
        @Test
        void nullArgForNonRefParamShouldReportErr() {
            var def = structDef("Def", List.of(param("p1", I64_TN)));
            registerDefinitions(List.of(), List.of(def));
            assertReportedErrors(0);

            var constCall = constructorCall("Def", List.of(NULL_LIT));
            tc.visit(constCall);
            assertReportedErrors(1);
        }

        @Test
        void nullArgForRefParamIsOk() {
            var customType = structDef("CustomType", List.of());
            var def = structDef("Def", List.of(param("p1", "CustomType", false)));
            registerDefinitions(List.of(), List.of(customType, def));
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

            assertThrows(TypeCheckFailedException.class, () -> registerDefinitions(List.of(fun), List.of()));
            assertReportedErrors(2);
        }

        @Test
        void nullOperandShouldReportErr_2() {
            var lhs = NULL_LIT;
            var rhs = i64Lit(123);
            var fun = funDef("fun", List.of(), List.of(
                    varDeclareStat("var", I64_TN),
                    varAssignStat("var", binOpExpr(lhs, BinaryOp.add, rhs))));

            assertThrows(TypeCheckFailedException.class, () -> registerDefinitions(List.of(fun), List.of()));
            assertReportedErrors(3);
        }

        @Test
        void nullOperandShouldReportErr_3() {
            var lhs = NULL_LIT;
            var rhs = NULL_LIT;
            var fun = funDef("fun", List.of(), List.of(
                    varDeclareStat("var", I64_TN),
                    varAssignStat("var", binOpExpr(lhs, BinaryOp.add, rhs))));

            assertThrows(TypeCheckFailedException.class, () -> registerDefinitions(List.of(fun), List.of()));
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
            assertThrows(TypeCheckFailedException.class, () -> registerDefinitions(List.of(fun), List.of()));
            assertReportedErrors(2);
        }

        @Test
        void nullOperandShouldReportErr_booleanOp() {
            var operand = NULL_LIT;
            var fun = funDef("fun", List.of(), List.of(
                    varDeclareStat("var", BOOL_TN),
                    varAssignStat("var", unaryOpExpr(operand, UnaryOp.not))));
            assertThrows(TypeCheckFailedException.class, () -> registerDefinitions(List.of(fun), List.of()));
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
            assertThrows(TypeCheckFailedException.class, () -> registerDefinitions(List.of(fun), List.of()));
            assertReportedErrors(1);
        }

        @Test
        void nullOkInBranchWithRefType() {
            var cond = boolLit(true);
            var fun = funDef("fun", List.of(param("strVar", STRING_TN)), List.of(
                    varAssignStat("strVar",
                            ternaryConditionalExpr(cond, NULL_LIT, stringLit("else expr")))));
            registerDefinitions(List.of(fun), List.of());
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
            assertThrows(TypeCheckFailedException.class, () -> registerDefinitions(List.of(fun), List.of()));
            assertReportedErrors(1);
        }

        @Test
        void nullOkIfTargetIsRefType() {
            var expr = NULL_LIT;
            var fun = funDef("fun", List.of(param("strVar", STRING_TN)),
                    List.of(varAssignStat("strVar", expr)));
            registerDefinitions(List.of(fun), List.of());
            assertReportedErrors(0);
        }
    }

    /** Add definitions to the tested type checker's state */
    private void registerDefinitions(List<FunDef> funDefs, List<StructDef> structDefs) {
        tc.visit(new Prog(new SourcePos(0, 0), funDefs, null, structDefs));
    }

    private void assertReportedErrors(int expErrCount) {
        assertEquals(expErrCount, tc.errorsReported);
    }
}
