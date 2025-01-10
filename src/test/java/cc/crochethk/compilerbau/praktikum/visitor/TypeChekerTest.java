package cc.crochethk.compilerbau.praktikum.visitor;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;

import cc.crochethk.compilerbau.praktikum.ast.*;
import cc.crochethk.compilerbau.praktikum.ast.BinOpExpr.BinaryOp;
import cc.crochethk.compilerbau.praktikum.ast.literal.*;
import cc.crochethk.compilerbau.praktikum.visitor.TypeChecker.TypeCheckFailedException;
import utils.SourcePos;

public class TypeChekerTest {
    private static final SourcePos srcPosMock = new SourcePos(-1, -1);
    private TypeChecker tc;

    private Parameter param(String paramName, String typeName, boolean isBuiltin) {
        return new Parameter(paramName, new TypeNode(srcPosMock, typeName, isBuiltin));
    }

    @BeforeEach
    void setUp() {
        tc = new TypeChecker();
    }

    @Nested
    class FunCallTests {
        @Test
        void nullArgForNonRefParamShouldReportErr() {
            var fun = new FunDef(srcPosMock, "fun", List.of(
                    param("p1", "i64", true)),
                    new TypeNode(srcPosMock, "i64", true), new StatementList(srcPosMock,
                            List.of(new ReturnStat(srcPosMock, new I64Lit(srcPosMock, 42, false)))));
            registerDefinitions(List.of(fun), List.of());
            assertReportedErrors(0);

            var funCall = new FunCall(srcPosMock, "fun", List.of(new NullLit(srcPosMock)));
            tc.visit(funCall);
            assertReportedErrors(1);
        }

        @Test
        void nullArgForRefParamIsOk() {
            var customType = new StructDef(srcPosMock, "CustomType", List.of());
            var fun = new FunDef(srcPosMock, "fun",
                    List.of(param("p1", "CustomType", false)),
                    new TypeNode(srcPosMock, "void", true),
                    new StatementList(srcPosMock, List.of()));
            registerDefinitions(List.of(fun), List.of(customType));
            assertReportedErrors(0);

            var funCall = new FunCall(srcPosMock, "fun", List.of(new NullLit(srcPosMock)));
            tc.visit(funCall);
            assertReportedErrors(0);
        }
    }

    @Nested
    class ConstructorCallTests {
        @Test
        void emptyStruct() {
            var structDef = new StructDef(srcPosMock, "Empty", List.of());
            registerDefinitions(List.of(), List.of(structDef));
            assertReportedErrors(0);

            var constCall = new ConstructorCall(srcPosMock, "Empty", List.of());
            tc.visit(constCall);
            assertReportedErrors(0);
        }

        // ------------ adjusted copy paste from FunCallTests ------------------
        @Test
        void nullArgForNonRefParamShouldReportErr() {
            var def = new StructDef(srcPosMock, "Def", List.of(param("p1", "i64", true)));
            registerDefinitions(List.of(), List.of(def));
            assertReportedErrors(0);

            var constCall = new ConstructorCall(srcPosMock, "Def", List.of(new NullLit(srcPosMock)));
            tc.visit(constCall);
            assertReportedErrors(1);
        }

        @Test
        void nullArgForRefParamIsOk() {
            var customType = new StructDef(srcPosMock, "CustomType", List.of());
            var def = new StructDef(srcPosMock, "Def", List.of(param("p1", "CustomType", false)));
            registerDefinitions(List.of(), List.of(customType, def));
            assertReportedErrors(0);

            var constCall = new ConstructorCall(srcPosMock, "Def", List.of(new NullLit(srcPosMock)));
            tc.visit(constCall);
            assertReportedErrors(0);
        }
        // ---------------------------------------------------------------------
    }

    /** Add definitions to the tested type checker's state */
    private void registerDefinitions(List<FunDef> funDefs, List<StructDef> structDefs) {
        tc.visit(new Prog(srcPosMock, funDefs, null, structDefs));
    }

    private void assertReportedErrors(int expErrCount) {
        assertEquals(expErrCount, tc.errorsReported);
    }
}
