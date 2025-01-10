package cc.crochethk.compilerbau.praktikum.visitor;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;

import cc.crochethk.compilerbau.praktikum.ast.*;
import cc.crochethk.compilerbau.praktikum.ast.literal.*;
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
            var prog = new Prog(srcPosMock, List.of(fun), null, List.of());
            tc.visit(prog);

            var funCall = new FunCall(srcPosMock, "fun", List.of(new NullLit(srcPosMock)));
            assertEquals(0, tc.errorsReported);
            tc.visit(funCall);
            assertEquals(1, tc.errorsReported);
        }

        @Test
        void nullArgForRefParamIsOk() {
            var structDefs = List.of(new StructDef(srcPosMock, "CustomType", List.of()));
            var fun = new FunDef(srcPosMock, "fun",
                    List.of(param("p1", "CustomType", false)),
                    new TypeNode(srcPosMock, "void", true),
                    new StatementList(srcPosMock, List.of()));
            var prog = new Prog(srcPosMock, List.of(fun), null, structDefs);
            tc.visit(prog);

            var funCall = new FunCall(srcPosMock, "fun", List.of(new NullLit(srcPosMock)));
            assertEquals(0, tc.errorsReported);
            tc.visit(funCall);
            assertEquals(0, tc.errorsReported);
        }
    }
}
