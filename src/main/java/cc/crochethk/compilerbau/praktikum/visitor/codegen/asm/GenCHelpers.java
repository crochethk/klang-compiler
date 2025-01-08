package cc.crochethk.compilerbau.praktikum.visitor.codegen.asm;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import cc.crochethk.compilerbau.praktikum.ast.*;
import cc.crochethk.compilerbau.praktikum.ast.literal.*;
import cc.crochethk.compilerbau.praktikum.visitor.SourceCodeBuilder;
import cc.crochethk.compilerbau.praktikum.visitor.Type;
import cc.crochethk.compilerbau.praktikum.visitor.codegen.CodeGenVisitor;

/**
 * Generates C files (.h, .c) containing all function signatures  of the programs
 * FunDef nodes for easier external usage, as well as auto-generated functions for
 * structs.
 */
public class GenCHelpers extends CodeGenVisitor<Void> {
    private static final String EXT_H = ".h";
    private static final String EXT_C = ".c";
    private static final String INDENT_SEQ = "  ";
    private SourceCodeBuilder header;
    private SourceCodeBuilder ccode;

    public GenCHelpers(String outputDir, String packageName, String className) throws IOException {
        super(outputDir, packageName, className);
        header = new SourceCodeBuilder(INDENT_SEQ);
        ccode = new SourceCodeBuilder(INDENT_SEQ);
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
        header.write(formatTypeNode(type));
        return null;
    }

    @Override
    public Void visit(FunDef funDef) {
        header.write("\n");
        // Write function signature
        funDef.returnType.accept(this);
        header.write(" ");
        header.write(funDef.name, "(", formatParams(funDef.params), ");");
        return null;
    }

    private String formatParams(List<Parameter> params) {
        StringBuilder sb = new StringBuilder();
        if (params.isEmpty()) {
            sb.append("void");
        } else {
            for (int i = 0; i < params.size(); i++) {
                var p = params.get(i);
                // Write parameter type and name
                sb.append(formatTypeNode(p.type()));
                sb.append(" ");
                sb.append(p.name());
                if (i < params.size() - 1)
                    sb.append(", ");
            }
        }
        return sb.toString();
    }

    private String formatTypeNode(TypeNode type) {
        return type.theType.cTypeName();
    }

    @Override
    public Void visit(StructDef structDef) {
        // write actual struct definition
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
        var guardName = fileNameNoExt().concat(EXT_H).replaceAll("[\\. ]", "_").toUpperCase();
        // H preamble
        header.write("// Auto-generated C header file");
        header.write("\n#ifndef ", guardName);
        header.write("\n#define ", guardName, "\n");
        header.write("\n#include <stdint.h>");
        header.write("\n#include <stdbool.h>", "\n");

        // C preamble
        ccode.write("\n#include <stdio.h>", "\n");
        ccode.write("\n#include <stdlib.h>", "\n");
        ccode.write("\n#include <string.h>", "\n");
        ccode.write("#include \"", fileNameNoExt(), EXT_H, "\"", "\n");

        // Declare structs
        prog.structDefs.forEach(st -> header.write("\nstruct ", st.name, ";"));
        header.write("\n");

        // Declare struct auto-methods
        // - Methodnames: <packageName>$$<ProgName>$<StructName>$<methName>
        prog.structDefs.forEach(st -> {
            // constructor
            writeConstructorSignature(header, st);
            header.write(";");

            // destructor
            writeDestructorSignature(header, st);
            header.write(";");

            // to_string
            writeToStringSignature(header, st);
            header.write(";");

            header.write("\n");
        });

        // Generate auto-methods definitions
        prog.structDefs.forEach(st -> {
            var thisPtrT = st.theType.cTypeName();
            // constructor
            writeConstructorSignature(ccode, st);
            ccode.write(" {");
            ccode.writeIndented(thisPtrT, " this = (", thisPtrT, ")malloc(sizeof(*this));");
            for (int i = 0; i < st.fields.size(); i++) {
                var f = st.fields.get(i);
                ccode.writeIndented("this->", f.name(), " = ", f.name(), ";");
            }
            ccode.writeIndented("return this;");
            ccode.write("\n}\n");

            // destructor
            writeDestructorSignature(ccode, st);
            ccode.write(" {");
            ccode.writeIndented("free(this);");
            ccode.write("\n}\n");

            // to_string
            writeToStringSignature(ccode, st);
            ccode.write(" {");
            ccode.writeIndented("char* r = strdup(\"", st.name, "(\");");
            if (!st.fields.isEmpty()) {
                ccode.writeIndented("char* fStr;");
                ccode.writeIndented("char* tmpRes;");
                for (var it = st.fields.iterator(); it.hasNext();) {
                    var f = it.next();
                    // Put current field's stringified value to "fStr"
                    if (f.type().theType.isReference()) {
                        ccode.writeIndented("fStr = ");
                        if (f.type().theType == Type.STRING_T) {
                            ccode.write("strdup(this->", f.name(), ");");
                        } else {
                            writeCFullClassName(ccode);
                            ccode.write("$", f.type().typeToken, "$to_string(this->", f.name(), ");");
                        }
                    } else {
                        ccode.writeIndented("fStr = malloc(22);");
                        ccode.writeIndented("sprintf(fStr,\"%ld\", this->", f.name(), ");");
                    }

                    // "+3" for the ", " or ")" suffix after each field (I guess)
                    ccode.writeIndented("tmpRes = malloc(strlen(r)+strlen(fStr)+3);");
                    ccode.writeIndented("tmpRes[0] = '\\0';"); // set strlen to 0
                    ccode.writeIndented("tmpRes = strcat(tmpRes,r);"); // copy current result
                    ccode.writeIndented("tmpRes = strcat(tmpRes,fStr);"); // append field value
                    ccode.writeIndented("tmpRes = strcat(tmpRes,", (it.hasNext() ? "\", \"" : "\"\""), ");");
                    ccode.writeIndented("free(r);");
                    ccode.writeIndented("free(fStr);");
                    ccode.writeIndented("r = tmpRes;");
                }
            }
            ccode.writeIndented("r = strcat(r, \")\");");
            ccode.writeIndented("return r;");

            ccode.write("\n}\n");
            ccode.write("\n");
        });

        // Declare static funcitons
        prog.funDefs.forEach(f -> f.accept(this));
        header.write("\n");

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

    private void writeCFullClassName(SourceCodeBuilder scb) {
        scb.write(packageName, "$$", className);
    }

    private void writeConstructorSignature(SourceCodeBuilder scb, StructDef st) {
        scb.write("\n", st.theType.cTypeName(), " ");
        writeCFullClassName(scb);
        scb.write("$", st.name, "$new(", formatParams(st.fields), ")");
    }

    private void writeDestructorSignature(SourceCodeBuilder scb, StructDef st) {
        scb.write("\nvoid ");
        writeCFullClassName(scb);
        scb.write("$", st.name, "$dispose(", st.theType.cTypeName(), " this)");
    }

    private void writeToStringSignature(SourceCodeBuilder scb, StructDef st) {
        scb.write("\nchar* ");
        writeCFullClassName(scb);
        scb.write("$", st.name, "$to_string(", st.theType.cTypeName(), " this)");
    }
}
