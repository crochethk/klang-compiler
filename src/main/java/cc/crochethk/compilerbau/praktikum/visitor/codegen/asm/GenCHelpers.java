package cc.crochethk.compilerbau.praktikum.visitor.codegen.asm;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import cc.crochethk.compilerbau.praktikum.ast.*;
import cc.crochethk.compilerbau.praktikum.ast.literal.*;
import cc.crochethk.compilerbau.praktikum.visitor.SourceCodeBuilder;
import cc.crochethk.compilerbau.praktikum.visitor.codegen.CodeGenVisitor;

/**
 * Generates C files (.h, .c) containing all function signatures  of the programs
 * FunDef nodes for easier external usage, as well as auto-generated functions for
 * structs.
 */
public class GenCHelpers extends CodeGenVisitor<Void> {
    private static final String EXT_H = ".h";
    private static final String EXT_C = ".c";
    private SourceCodeBuilder header;
    private SourceCodeBuilder ccode;

    public GenCHelpers(String outputDir, String packageName, String className) throws IOException {
        super(outputDir, packageName, className);
        header = new SourceCodeBuilder("  ");
        ccode = new SourceCodeBuilder("  ");
    }

    @Override
    public List<Path> outFilePaths() {
        var ofpne = outFilePathNoExt();
        var h = Path.of(ofpne + EXT_H);
        var c = Path.of(ofpne + EXT_C);
        return List.of(h, c);
    }

    private String outFilePathNoExt() {
        return Path.of(outDir, fileNameNoExt()).toString();
    }

    private String fileNameNoExt() {
        return packageName + "." + className;
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
        header.write(type.theType.cTypeName());
        return null;
    }

    @Override
    public Void visit(FunDef funDef) {
        header.write("\n");
        // Write return type
        funDef.returnType.accept(this);
        header.write(" ");

        // Write function name
        header.write(funDef.name, "(");

        // Add parameters
        if (funDef.params.isEmpty()) {
            header.write("void");
        } else {
            for (int i = 0; i < funDef.params.size(); i++) {
                var p = funDef.params.get(i);
                // Write parameter type and name
                p.type().accept(this);
                header.write(" ", p.name());
                if (i < funDef.params.size() - 1)
                    header.write(", ");
            }
        }
        header.write(");");
        return null;
    }

    @Override
    public Void visit(StructDef structDef) {
        header.write("\nstruct ");
        header.write(structDef.name);
        header.write(" {");
        for (int i = 0; i < structDef.fields.size(); i++) {
            header.writeIndented("");
            var f = structDef.fields.get(i);
            // Write field type and name
            f.type().accept(this);
            header.write(" ", f.name());
            header.write(";");
        }
        if (!structDef.fields.isEmpty())
            header.write("\n");
        header.write("};\n");
        return null;
    }

    @Override
    public Void visit(Prog prog) {
        var guardName = fileNameNoExt().concat(EXT_H).replace('.', '_').toUpperCase();
        // H preamble
        header.write("// Auto-generated C header file");
        header.write("\n#ifndef ", guardName);
        header.write("\n#define ", guardName, "\n");
        header.write("\n#include <stdint.h>");
        header.write("\n#include <stdbool.h>", "\n");

        // C preamble
        ccode.write("#include \"", fileNameNoExt(), EXT_H, "\"", "\n");

        // Declare structs in header
        prog.structDefs.forEach(st -> header.write("\nstruct ", st.name, ";"));
        header.write("\n");

        // Declare and define funcitons
        prog.funDefs.forEach(f -> f.accept(this));
        // Declare and define structs
        prog.structDefs.forEach(st -> st.accept(this));

        header.write("\n#endif // ", guardName, "\n");

        // Dump source code to files
        try {
            var h = new FileWriter(outFilePathNoExt() + EXT_H);
            h.write(header.toString());
            h.close();
            var c = new FileWriter(outFilePathNoExt() + EXT_C);
            c.write(ccode.toString());
            c.close();
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
