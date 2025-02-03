package cc.crochethk.klang.visitor;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;

import cc.crochethk.klang.ast.*;
import cc.crochethk.klang.ast.BinOpExpr.BinaryOp;
import cc.crochethk.klang.ast.UnaryOpExpr.UnaryOp;
import cc.crochethk.klang.visitor.TypeChecker.TypeCheckFailedException;
import cc.crochethk.klang.testhelpers.NodeMocker;

public class TypeCheckerTest extends NodeMocker {
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
            assertEquals(Type.LONG_T, funCall.theType);
            assertEquals(Type.ANY_T, funCall.args.get(0).theType);
        }

        @Test
        void nullArgForRefParamIsOk() {
            var customType = structDef("CustomType", List.of());
            var fun = funDef("fun", List.of(param("p1", "CustomType")), List.of());
            checkProgOf(List.of(fun), List.of(customType));
            assertReportedErrors(0);

            var funCall = funCall("fun", List.of(NULL_LIT));
            tc.visit(funCall);
            assertReportedErrors(0);
            assertEquals(Type.VOID_T, funCall.theType);
            assertEquals(Type.ANY_T, funCall.args.get(0).theType);
        }
    }

    @Nested
    class MemberAccessChainTests {
        private StructDef simpleStruct;
        private StructDef structWithStructField;
        private StructDef structWithMeths;

        @BeforeEach
        void setUp() {
            simpleStruct = structDef("SimpleStruct", List.of(
                    param("primField", I64_TN)));

            structWithStructField = structDef("StructWithStructField", List.of(
                    param("structField", typeNode(simpleStruct.name))));

            var sswmName = "StructWithMethsAndFields";
            var sswmType = Type.of(sswmName, "");
            var meth_get42 = methDef(
                    sswmType, "get42", List.of(), I64_TN,
                    List.of(returnStat(i64Lit(42))));
            var meth_addToField = methDef(sswmType, "addToField",
                    List.of(param("num", I64_TN)),
                    I64_TN,
                    List.of(returnStat(binOpExpr(
                            var("num"),
                            BinaryOp.add,
                            memberAccessChain(var("self"), fieldGet("primField")))) //
                    ));
            structWithMeths = structDef(sswmName,
                    List.of(param("primField", I64_TN),
                            param("structField", typeNode(simpleStruct.name))),
                    List.of(meth_get42, meth_addToField));
        }

        @Test
        void chainOfOneField() {
            var customType = simpleStruct;
            // "value=p1.primField"
            var maChain = memberAccessChain(var("p1"), fieldGet("primField"));
            var fun = funDef("fun",
                    List.of(param("p1", customType.name),
                            param("value", I64_TN)),
                    List.of(varAssignStat("value", maChain)));

            checkProgOf(List.of(fun), List.of(customType));
            assertReportedErrors(0);

            assertEquals(Type.LONG_T, maChain.theType);
            assertEquals(Type.of(simpleStruct.name, ""), maChain.owner.theType);
            assertEquals(Type.LONG_T, maChain.chain.theType);
            assertNull(maChain.chain.next);
        }

        @Test
        void chainOfMultipleFields() {
            var customType = structWithStructField;

            // "p1.structField.primField"
            var maChain = memberAccessChain(var("p1"), fieldGet("structField"), fieldGet("primField"));

            var fun = funDef("fun",
                    List.of(param("p1", customType.name),
                            param("value", I64_TN)),
                    List.of(varAssignStat("value", maChain)));

            checkProgOf(List.of(fun), List.of(customType, simpleStruct));
            assertReportedErrors(0);

            assertEquals(Type.LONG_T, maChain.theType);
            assertEquals(Type.of(customType.name, ""), maChain.owner.theType);
            assertEquals(Type.of(simpleStruct.name, ""), maChain.chain.theType);
            assertEquals(Type.LONG_T, maChain.chain.next.theType);
            assertNull(maChain.chain.next.next);
        }

        @Test
        void chainOfOneMethodCall_noArgs() {
            var customType = structWithMeths;
            var maChain = memberAccessChain(var("p1"), methodCall("get42", List.of()));

            var fun = funDef("fun",
                    List.of(param("p1", customType.name)), I64_TN,
                    List.of(returnStat(maChain)));

            checkProgOf(List.of(fun), List.of(customType, simpleStruct));
            assertReportedErrors(0);

            assertEquals(Type.LONG_T, maChain.theType);
            assertEquals(Type.of(customType.name, ""), maChain.owner.theType);
            assertEquals(Type.LONG_T, maChain.chain.theType);
            assertNull(maChain.chain.next);
        }

        @Test
        void chainOfOneMethodCall_oneArg() {
            var customType = structWithMeths;
            var maChain = memberAccessChain(var("p1"),
                    methodCall("addToField", List.of(i64Lit(7))));

            var fun = funDef("fun",
                    List.of(param("p1", customType.name)), I64_TN,
                    List.of(returnStat(maChain)));

            checkProgOf(List.of(fun), List.of(customType, simpleStruct));
            assertReportedErrors(0);

            assertEquals(Type.LONG_T, maChain.theType);
            assertEquals(Type.of(customType.name, ""), maChain.owner.theType);
            assertEquals(Type.LONG_T, maChain.chain.theType);
            assertNull(maChain.chain.next);
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
            assertEquals(Type.of(structDef.name, ""), constCall.theType);
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
            assertEquals(Type.of(def.name, ""), constCall.theType);
            assertEquals(Type.ANY_T, constCall.args.get(0).theType);
        }

        @Test
        void nullArgForRefParamIsOk() {
            var customType = structDef("CustomType", List.of());
            var def = structDef("Def", List.of(param("p1", "CustomType")));
            checkProgOf(List.of(), List.of(customType, def));
            assertReportedErrors(0);

            var constCall = constructorCall("Def", List.of(NULL_LIT));
            tc.visit(constCall);
            assertReportedErrors(0);
            assertEquals(Type.of(def.name, ""), constCall.theType);
            assertEquals(Type.ANY_T, constCall.args.get(0).theType);
        }
        // ---------------------------------------------------------------------
    }

    @Nested
    class BinOpExprTests {
        @Nested
        class ComparisonOperators {
            // ------ equality
            @Test
            void equalityWithMixedRefTypesIsOk() {
                var stDef = EMPTY_STRUCT;
                var structObj = constructorCall(stDef.name);
                var fun = funDef("fun", List.of(), List.of(varDeclareStat("var", BOOL_TN),
                        varAssignStat("var", binOpExpr(structObj, BinaryOp.eq, NULL_LIT)),
                        varAssignStat("var", binOpExpr(NULL_LIT, BinaryOp.eq, structObj)),
                        varAssignStat("var", binOpExpr(structObj, BinaryOp.eq, stringLit("some string"))),
                        varAssignStat("var", binOpExpr(structObj, BinaryOp.neq, NULL_LIT)),
                        varAssignStat("var", binOpExpr(NULL_LIT, BinaryOp.neq, structObj)),
                        varAssignStat("var", binOpExpr(structObj, BinaryOp.neq, stringLit("some string")))//
                ));
                checkProgOf(List.of(fun), List.of(stDef));
                assertReportedErrors(0);
            }

            @Test
            void equalityWithCompatiblePrimTypesIsOk() {
                var fun = funDef("fun", List.of(), List.of(varDeclareStat("var", BOOL_TN),
                        varAssignStat("var", binOpExpr(boolLit(true), BinaryOp.eq, boolLit(false))),
                        varAssignStat("var", binOpExpr(f64Lit(42), BinaryOp.eq, f64Lit(42))),
                        varAssignStat("var", binOpExpr(boolLit(true), BinaryOp.neq, boolLit(false))),
                        varAssignStat("var", binOpExpr(f64Lit(42), BinaryOp.neq, f64Lit(42)))//
                ));
                checkProgOf(List.of(fun), List.of());
                assertReportedErrors(0);
            }

            @Test
            void shouldReportErrOnEqualityWithIncompatiblePrimTypes() {
                var fun = funDef("fun", List.of(), List.of(varDeclareStat("var", BOOL_TN),
                        varAssignStat("var", binOpExpr(boolLit(true), BinaryOp.eq, i64Lit(1))),
                        varAssignStat("var", binOpExpr(i64Lit(0), BinaryOp.eq, f64Lit(0))),
                        varAssignStat("var", binOpExpr(boolLit(true), BinaryOp.neq, i64Lit(1))),
                        varAssignStat("var", binOpExpr(i64Lit(0), BinaryOp.neq, f64Lit(0)))//
                ));
                assertThrows(TypeCheckFailedException.class, () -> checkProgOf(List.of(fun), List.of()));
                assertReportedErrors(4);
            }

            @Test
            void shouldReportErrOnEqualityComparisonOfPrimAndRefType() {
                var fun = funDef("fun", List.of(), List.of(varDeclareStat("var", BOOL_TN),
                        varAssignStat("var", binOpExpr(boolLit(true), BinaryOp.eq, stringLit("foo bar"))),
                        varAssignStat("var", binOpExpr(NULL_LIT, BinaryOp.eq, i64Lit(0))),
                        varAssignStat("var", binOpExpr(boolLit(true), BinaryOp.neq, stringLit("foo bar"))),
                        varAssignStat("var", binOpExpr(NULL_LIT, BinaryOp.neq, i64Lit(0)))//
                ));
                assertThrows(TypeCheckFailedException.class, () -> checkProgOf(List.of(fun), List.of()));
                assertReportedErrors(4);
            }

            // ------ ordinal
            @Test
            void shouldReportErrOnOrdinalComparisonOfNonNumericalType() {
                var voidFun = funDef("voidFun", List.of(), List.of());
                var voidExpr = funCall("voidFun");
                var fun = funDef("fun", List.of(), List.of(varDeclareStat("var", BOOL_TN),
                        varAssignStat("var", binOpExpr(boolLit(true), BinaryOp.gt, boolLit(false))),
                        varAssignStat("var", binOpExpr(voidExpr, BinaryOp.gteq, boolLit(false))),
                        varAssignStat("var", binOpExpr(boolLit(false), BinaryOp.lt, voidExpr)),
                        varAssignStat("var", binOpExpr(voidExpr, BinaryOp.lteq, voidExpr))//
                ));
                assertThrows(TypeCheckFailedException.class, () -> checkProgOf(List.of(fun, voidFun), List.of()));
                assertReportedErrors(4);
            }

            @Test
            void shouldReportErrOnOrdinalComparisonOfIncompatibleNumericalTypes() {
                var fun = funDef("fun", List.of(), List.of(varDeclareStat("var", BOOL_TN),
                        varAssignStat("var", binOpExpr(f64Lit(1), BinaryOp.gt, i64Lit(2))),
                        varAssignStat("var", binOpExpr(i64Lit(1), BinaryOp.gteq, f64Lit(2))),
                        varAssignStat("var", binOpExpr(f64Lit(1), BinaryOp.lt, i64Lit(2))),
                        varAssignStat("var", binOpExpr(i64Lit(1), BinaryOp.lteq, f64Lit(2)))//
                ));
                assertThrows(TypeCheckFailedException.class, () -> checkProgOf(List.of(fun), List.of()));
                assertReportedErrors(4);
            }

            @Test
            void shouldReportErrOnOrdinalComparisonOfPrimAndRefTypes() {
                var fun = funDef("fun", List.of(), List.of(varDeclareStat("var", BOOL_TN),
                        varAssignStat("var", binOpExpr(i64Lit(1), BinaryOp.gt, stringLit("foo bar"))),
                        varAssignStat("var", binOpExpr(NULL_LIT, BinaryOp.gteq, f64Lit(0))),
                        varAssignStat("var", binOpExpr(boolLit(true), BinaryOp.lt, stringLit("foo bar"))),
                        varAssignStat("var", binOpExpr(NULL_LIT, BinaryOp.lteq, boolLit(false)))//
                ));
                assertThrows(TypeCheckFailedException.class, () -> checkProgOf(List.of(fun), List.of()));
                assertReportedErrors(4);
            }

            @Test
            void ordinalComparisonOnEqualNumericalTypesIsOk() {
                var fun = funDef("fun", List.of(), List.of(varDeclareStat("var", BOOL_TN),
                        varAssignStat("var", binOpExpr(i64Lit(1), BinaryOp.gt, i64Lit(2))),
                        varAssignStat("var", binOpExpr(f64Lit(3), BinaryOp.gteq, f64Lit(4))),
                        varAssignStat("var", binOpExpr(f64Lit(1), BinaryOp.lt, f64Lit(2))),
                        varAssignStat("var", binOpExpr(i64Lit(3), BinaryOp.lteq, i64Lit(4)))//
                ));
                checkProgOf(List.of(fun), List.of());
                assertReportedErrors(0);
            }
        }

        @Nested
        class ArithmeticOperators {
            @Test
            void addTwoI64IsOk() {
                testBinaryOpAssertI64Result(i64Lit(123), BinaryOp.add, i64Lit(456));
                assertReportedErrors(0);
            }

            @Test
            void subTwoI64IsOk() {
                testBinaryOpAssertI64Result(i64Lit(123), BinaryOp.sub, i64Lit(456));
                assertReportedErrors(0);
            }

            @Test
            void multTwoI64IsOk() {
                testBinaryOpAssertI64Result(i64Lit(123), BinaryOp.mult, i64Lit(456));
                assertReportedErrors(0);
            }

            @Test
            void divTwoI64IsOk() {
                testBinaryOpAssertI64Result(i64Lit(123), BinaryOp.div, i64Lit(456));
                assertReportedErrors(0);
            }

            @Test
            void modTwoI64IsOk() {
                testBinaryOpAssertI64Result(i64Lit(123), BinaryOp.mod, i64Lit(456));
                assertReportedErrors(0);
            }

            // -----------
            @Test
            void addTwoF64IsOk() {
                testBinaryOpAssertF64Result(f64Lit(1.23d), BinaryOp.add, f64Lit(4.56d));
                assertReportedErrors(0);
            }

            @Test
            void subTwoF64IsOk() {
                testBinaryOpAssertF64Result(f64Lit(1.23d), BinaryOp.sub, f64Lit(4.56d));
                assertReportedErrors(0);
            }

            @Test
            void multTwoF64IsOk() {
                testBinaryOpAssertF64Result(f64Lit(1.23d), BinaryOp.mult, f64Lit(4.56d));
                assertReportedErrors(0);
            }

            @Test
            void divTwoF64IsOk() {
                testBinaryOpAssertF64Result(f64Lit(1.23d), BinaryOp.div, f64Lit(4.56d));
                assertReportedErrors(0);
            }

            @Test
            void modTwoF64IsOk() {
                testBinaryOpAssertF64Result(f64Lit(1.23d), BinaryOp.mod, f64Lit(4.56d));
                assertReportedErrors(0);
            }
            //----------- with errors
            /*
            OK, when: lhs, rhs == numeric && lhs, rhs == same equal types
            NOT, ok:
                - different NUMERIC types
                - NUMERIC and not-numeric PRIMtype
                - Numeric and Reftype
             */

            @Test
            void shouldReportErrOnIncompatibleNumericOperands_1() {
                var fun = funDef("fun", List.of(), List.of(varDeclareStat("var", I64_TN),
                        varAssignStat("var", binOpExpr(i64Lit(1), BinaryOp.add, f64Lit(2.3))),
                        varAssignStat("var", binOpExpr(i64Lit(1), BinaryOp.sub, f64Lit(2.3))),
                        varAssignStat("var", binOpExpr(i64Lit(1), BinaryOp.mult, f64Lit(2.3))),
                        varAssignStat("var", binOpExpr(i64Lit(1), BinaryOp.div, f64Lit(2.3))),
                        varAssignStat("var", binOpExpr(i64Lit(1), BinaryOp.mod, f64Lit(2.3)))//
                ));
                assertThrows(TypeCheckFailedException.class, () -> checkProgOf(List.of(fun), List.of()));
                assertReportedErrors(5);
            }

            @Test
            void shouldReportErrOnIncompatibleNumericOperands_2() {
                var fun = funDef("fun", List.of(), List.of(varDeclareStat("var", F64_TN),
                        varAssignStat("var", binOpExpr(f64Lit(2.3), BinaryOp.add, i64Lit(1))),
                        varAssignStat("var", binOpExpr(f64Lit(2.3), BinaryOp.sub, i64Lit(1))),
                        varAssignStat("var", binOpExpr(f64Lit(2.3), BinaryOp.mult, i64Lit(1))),
                        varAssignStat("var", binOpExpr(f64Lit(2.3), BinaryOp.div, i64Lit(1))),
                        varAssignStat("var", binOpExpr(f64Lit(2.3), BinaryOp.mod, i64Lit(1)))//
                ));
                assertThrows(TypeCheckFailedException.class, () -> checkProgOf(List.of(fun), List.of()));
                assertReportedErrors(5);
            }

            @Test
            void shouldReportErrOnIncompatiblePrimTypes_1() {
                var voidFun = funDef("voidFun", List.of(), List.of());
                var voidExpr = funCall("voidFun");
                var fun = funDef("fun", List.of(), List.of(varDeclareStat("var", I64_TN), varDeclareStat("b", BOOL_TN),
                        varAssignStat("var", binOpExpr(i64Lit(1), BinaryOp.add, boolLit(true))),
                        varAssignStat("var", binOpExpr(voidExpr, BinaryOp.mult, i64Lit(1))),
                        varAssignStat("b", binOpExpr(boolLit(true), BinaryOp.mod, i64Lit(1)))//
                ));
                assertThrows(TypeCheckFailedException.class, () -> checkProgOf(List.of(fun, voidFun), List.of()));
                assertReportedErrors(4);
            }

            @Test
            void shouldReportErrOnPrimWithRefType() {
                var fun = funDef("fun", List.of(), List.of(varDeclareStat("i", I64_TN), varDeclareStat("s", STRING_TN),
                        varAssignStat("s", binOpExpr(stringLit(""), BinaryOp.sub, i64Lit(1))),
                        varAssignStat("i", binOpExpr(i64Lit(0), BinaryOp.mult, stringLit(""))),
                        varAssignStat("s", binOpExpr(stringLit(""), BinaryOp.div, boolLit(false))),
                        varAssignStat("s", binOpExpr(stringLit(""), BinaryOp.mod, stringLit("")))//
                ));
                assertThrows(TypeCheckFailedException.class, () -> checkProgOf(List.of(fun), List.of()));
                assertReportedErrors(4);
            }
        }

        @Nested
        class BooleanOperators {
            @Test
            void andTwoBoolIsOk() {
                testBinaryOpAssertBoolResult(boolLit(false), BinaryOp.and, boolLit(true));
                assertReportedErrors(0);
            }

            @Test
            void orTwoBoolIsOk() {
                testBinaryOpAssertBoolResult(boolLit(true), BinaryOp.or, boolLit(false));
                assertReportedErrors(0);
            }

            @Test
            void shouldReportErrOnNonBoolPrimType() {
                var fun = funDef("fun", List.of(), List.of(varDeclareStat("b", BOOL_TN),
                        // lhs non-bool prim
                        varAssignStat("b", binOpExpr(i64Lit(1), BinaryOp.or, boolLit(true))),
                        // rhs non-bool prim
                        varAssignStat("b", binOpExpr(boolLit(true), BinaryOp.or, i64Lit(1))),
                        // lhs, rhs non-bool prim
                        varAssignStat("b", binOpExpr(i64Lit(1), BinaryOp.and, f64Lit(2.3)))//
                ));
                assertThrows(TypeCheckFailedException.class, () -> checkProgOf(List.of(fun), List.of()));
                assertReportedErrors(3);
            }

            @Test
            void shouldReportErrOnNonBoolRefType() {
                var fun = funDef("fun", List.of(), List.of(varDeclareStat("b", BOOL_TN),
                        // lhs reftype
                        varAssignStat("b", binOpExpr(stringLit(""), BinaryOp.or, boolLit(true))),
                        // rhs reftype
                        varAssignStat("b", binOpExpr(boolLit(true), BinaryOp.or, NULL_LIT)),
                        // lhs, rhs reftype
                        varAssignStat("b", binOpExpr(NULL_LIT, BinaryOp.and, stringLit("")))//
                ));
                assertThrows(TypeCheckFailedException.class, () -> checkProgOf(List.of(fun), List.of()));
                assertReportedErrors(3);
            }
        }

        private void testBinaryOpAssertI64Result(Expr lhs, BinaryOp op, Expr rhs) {
            assertEquals(Type.LONG_T, _testBinaryOpWith(lhs, op, rhs, I64_TN).theType);
        }

        private void testBinaryOpAssertF64Result(Expr lhs, BinaryOp op, Expr rhs) {
            assertEquals(Type.DOUBLE_T, _testBinaryOpWith(lhs, op, rhs, F64_TN).theType);
        }

        private void testBinaryOpAssertBoolResult(Expr lhs, BinaryOp op, Expr rhs) {
            assertEquals(Type.BOOL_T, _testBinaryOpWith(lhs, op, rhs, BOOL_TN).theType);
        }

        private BinOpExpr _testBinaryOpWith(Expr lhs, BinaryOp op, Expr rhs, TypeNode expResultType) {
            var expr = binOpExpr(lhs, op, rhs);
            var fun = funDef("fun", List.of(), List.of(varDeclareStat("var", expResultType),
                    varAssignStat("var", expr)));
            checkProgOf(List.of(fun), List.of());
            return expr;
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
            checkProgOf(List.of(fun), List.of());
            assertReportedErrors(0);
            var callFun = funCall(fun.name, List.of(i64Lit(-1), i64Lit(2)));
            tc.visit(callFun);
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

    @Nested
    class ReturnStatTests {
        @Test
        void emptyReturnIsVoidType() {
            var stat = returnStat();
            var fun = funDef("fun", List.of(), List.of(stat));
            checkProgOf(List.of(fun), List.of());
            assertReportedErrors(0);
            assertEquals(Type.VOID_T, stat.theType);
        }

        @Test
        void nullWithReturnPrimitiveShouldReportErr_1() {
            var stat = returnStat(NULL_LIT);
            var fun = funDef("fun", List.of(), List.of(stat));
            assertThrows(TypeCheckFailedException.class, () -> checkProgOf(List.of(fun), List.of()));
            assertReportedErrors(1);
            assertEquals(Type.VOID_T, stat.theType);
        }

        @Test
        void nullWithReturnPrimitiveShouldReportErr_2() {
            var stat = returnStat(NULL_LIT);
            var fun = funDef("fun", List.of(), I64_TN, List.of(stat));
            assertThrows(TypeCheckFailedException.class, () -> checkProgOf(List.of(fun), List.of()));
            assertReportedErrors(1);
            assertEquals(Type.LONG_T, stat.theType);
        }

        @Test
        void nullOkWithReturnRefType_1() {
            var stat = returnStat(NULL_LIT);
            var fun = funDef("fun", List.of(), STRING_TN, List.of(stat));
            checkProgOf(List.of(fun), List.of());
            assertReportedErrors(0);
            assertEquals(Type.STRING_T, stat.theType);
        }

        @Test
        void nullOkWithReturnRefType_2() {
            var stat = returnStat(NULL_LIT);
            var struct = structDef("SomeStruct", List.of());
            var fun = funDef("fun", List.of(), typeNode(struct.name), List.of(stat));
            checkProgOf(List.of(fun), List.of(struct));
            assertReportedErrors(0);
            assertEquals(Type.of(struct.name, ""), stat.theType);
        }
    }

    @Nested
    class DropStatTests {
        @Test
        void nonRefTypeVarShouldReportErr_1() {
            var varName = "intVar";
            var fun = funDef("fun", List.of(param(varName, I64_TN)), List.of(dropStat(varName)));
            assertThrows(TypeCheckFailedException.class, () -> checkProgOf(List.of(fun), List.of()));
            assertReportedErrors(1);
        }

        @Test
        void dropStringVarIsOk() {
            var varName = "s";
            var fun = funDef("fun", List.of(param(varName, STRING_TN)), List.of(dropStat(varName)));
            checkProgOf(List.of(fun), List.of());
            assertReportedErrors(0);
        }

        @Test
        void dropCustomRefTypeVarIsOk() {
            var varName = "crtv";
            var struct = structDef("SomeStruct", List.of(param("foo", I64_TN)));
            var fun = funDef("fun", List.of(param(varName, typeNode(struct.name))),
                    List.of(dropStat(varName)));
            checkProgOf(List.of(fun), List.of(struct));
            assertReportedErrors(0);
        }
    }

    @Nested
    class MethDefTests {
        @Test
        void noExplicitArgsMeth() {
            var stName = "TheStruct";
            var theMeth = methDef(
                    Type.of(stName, ""), "noargsmeth", List.of(), I64_TN,
                    List.of(returnStat(memberAccessChain(var("self"), fieldGet("field")))));
            var theStruct = structDef(stName, List.of(param("field", I64_TN)), List.of(theMeth));
            assertDoesNotThrow(() -> checkProgOf(List.of(), List.of(theStruct)));
            assertReportedErrors(0);

            var selfParam = theMeth.params().get(0);
            assertEquals("self", selfParam.name());
            assertEquals(Type.of(stName, ""), selfParam.type().theType);

            assertEquals(Type.LONG_T, theMeth.def.theType);
            assertEquals(Type.LONG_T, theMeth.theType);
            assertEquals(Type.LONG_T, theMeth.returnType().theType);
        }

        @Test
        void someExplicitArgsMeth() {
            var stName = "TheStruct";
            var theMeth = methDef(
                    Type.of(stName, ""), "withSomeArgs",
                    List.of(param("foo", I64_TN), param("bar", BOOL_TN)), VOID_TN,
                    List.of(fieldAssignStat(var("self"), List.of(fieldSet("field")), var("foo"))));
            var theStruct = structDef(stName, List.of(param("field", I64_TN)), List.of(theMeth));
            assertDoesNotThrow(() -> checkProgOf(List.of(), List.of(theStruct)));
            assertReportedErrors(0);

            var selfParam = theMeth.params().get(0);
            assertEquals("self", selfParam.name());
            assertEquals(Type.of(stName, ""), selfParam.type().theType);

            assertEquals(Type.VOID_T, theMeth.def.theType);
            assertEquals(Type.VOID_T, theMeth.theType);
            assertEquals(Type.VOID_T, theMeth.returnType().theType);
        }
    }

    @Nested
    class ProgTests {
        @Test
        void structDefWithFieldOfStructDefinedLaterDoesNotFail() {
            var firstStruct = structDef("First", List.of(param("otherRef", "Other")));
            var otherStruct = structDef("Other", List.of(param("otherField", I64_TN)));
            assertDoesNotThrow(() -> checkProgOf(List.of(), List.of(firstStruct, otherStruct)));
            assertReportedErrors(0);
        }
    }

    /** Run TypeChecker on new Program consiting of given definitions */
    private void checkProgOf(List<FunDef> funDefs, List<StructDef> structDefs) {
        tc.visit(prog(funDefs, structDefs));
    }

    private void assertReportedErrors(int expErrCount) {
        assertEquals(expErrCount, tc.errorsReported);
    }
}
