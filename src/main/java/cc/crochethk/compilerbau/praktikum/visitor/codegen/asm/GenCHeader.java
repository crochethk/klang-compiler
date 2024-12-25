package cc.crochethk.compilerbau.praktikum.visitor.codegen.asm;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

import cc.crochethk.compilerbau.praktikum.ast.*;
import cc.crochethk.compilerbau.praktikum.ast.literal.*;
import cc.crochethk.compilerbau.praktikum.visitor.codegen.CodeGenVisitor;

/** Generates a C header file containing all function signatures of the programs FunDef nodes. */
public class GenCHeader extends CodeGenVisitor<Void> {
    private static final String FILE_EXT = ".h";
    private SourceCodeBuilder scb;

    public GenCHeader(String outputDir, String packageName, String className) throws IOException {
        super(outputDir, packageName, className);
        scb = new SourceCodeBuilder("  ");
    }

    @Override
    public Path outFilePath() {
        return Path.of(outDir, packageName + "." + className + FILE_EXT);
    }

    @Override
    public Void visit(I64Lit i64Lit) {
        return null;
    }

    @Override
    public Void visit(F64Lit f64Lit) {
        return null;
    }

    @Override
    public Void visit(BoolLit boolLit) {
        return null;
    }

    @Override
    public Void visit(StringLit stringLit) {
        return null;
    }

    @Override
    public Void visit(Var var) {
        return null;
    }

    @Override
    public Void visit(FunCall funCall) {
        return null;
    }

    @Override
    public Void visit(BinOpExpr binOpExpr) {
        return null;
    }

    @Override
    public Void visit(UnaryOpExpr unaryOpExpr) {
        return null;
    }

    @Override
    public Void visit(TernaryConditionalExpr ternaryConditionalExpr) {
        return null;
    }

    @Override
    public Void visit(VarDeclareStat varDeclareStat) {
        return null;
    }

    @Override
    public Void visit(VarAssignStat varAssignStat) {
        return null;
    }

    @Override
    public Void visit(IfElseStat ifElseStat) {
        return null;
    }

    @Override
    public Void visit(LoopStat loopStat) {
        return null;
    }

    @Override
    public Void visit(StatementList statementList) {
        return null;
    }

    @Override
    public Void visit(ReturnStat returnStat) {
        return null;
    }

    @Override
    public Void visit(BreakStat breakStat) {
        return null;
    }

    @Override
    public Void visit(TypeNode type) {
        scb.write(type.theType.cTypeName());
        return null;
    }

    @Override
    public Void visit(FunDef funDef) {
        scb.write("\n");
        // Write return type
        funDef.returnType.accept(this);
        scb.write(" ");

        // Write function name
        scb.write(funDef.name, "(");

        // Add parameters
        if (funDef.params.isEmpty()) {
            scb.write("void");
        } else {
            for (int i = 0; i < funDef.params.size(); i++) {
                var p = funDef.params.get(i);
                // Write parameter type and name
                p.type().accept(this);
                scb.write(" ", p.name());
                if (i < funDef.params.size() - 1)
                    scb.write(", ");
            }
        }
        scb.write(");");
        return null;
    }

    @Override
    public Void visit(StructDef structDef) {
        //TODO define auto-constructors/destructors
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public Void visit(Prog prog) {
        var outputFileName = outFilePath().getFileName().toString();
        var guardName = outputFileName.replace('.', '_').toUpperCase();
        scb.write("// Auto-generated C header file");
        scb.write("\n#ifndef ", guardName);
        scb.write("\n#define ", guardName, "\n");
        scb.write("\n#include <stdint.h>");
        scb.write("\n#include <stdbool.h>", "\n");
        prog.funDefs.forEach(f -> f.accept(this));
        scb.write("\n#endif // ", guardName, "\n");

        try (var w = new FileWriter(outFilePath().toFile())) {
            w.write(scb.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public Void visit(EmptyNode emptyNode) {
        return null;
    }
}
