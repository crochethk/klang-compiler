package cc.crochethk.compilerbau.praktikum.visitor.codegen.asm.helpers;

import cc.crochethk.compilerbau.praktikum.ast.*;

/** Generates a C-Header file based on the traversed AST. */
public class GenCHeaders extends GenCBase {
    public GenCHeaders(String outputDir, String packageName, String className) {
        super(outputDir, packageName, className, ".h");
    }

    @Override
    public void visit(TypeNode type) {
        codeBuilder.write(formatTypeNode(type));
    }

    @Override
    public void visit(FunDef funDef) {
        codeBuilder.writeIndent();
        // Write function signature
        funDef.returnType.accept(this);
        codeBuilder.write(" ", funDef.name, "(", formatParams(funDef.params), ");");
    }

    @Override
    public void visit(StructDef structDef) {
        // write actual struct definition
        codeBuilder.writeIndented("struct ", structDef.name, " {");
        codeBuilder.increaseIndent();
        for (var f : structDef.fields) {
            codeBuilder.writeIndent();
            // Write field type and name
            f.type().accept(this);
            codeBuilder.write(" ", f.name(), ";");
        }
        codeBuilder.decreaseIndent();
        if (!structDef.fields.isEmpty())
            codeBuilder.writeIndent();
        codeBuilder.write("};\n");
    }

    @Override
    public void visit(Prog prog) {
        var guardName = fileName().replaceAll("[\\. ]", "_").toUpperCase();
        // H preamble
        codeBuilder.write("// Auto-generated C header file");
        codeBuilder.writeIndented("#ifndef ", guardName);
        codeBuilder.writeIndented("#define ", guardName, "\n");
        codeBuilder.writeIndented("#include <stdint.h>");
        codeBuilder.writeIndented("#include <stdbool.h>", "\n");

        // Declare structs
        prog.structDefs.forEach(st -> codeBuilder.writeIndented("struct ", st.name, ";"));
        codeBuilder.write("\n");

        // Declare struct auto-methods
        // - Normal methods: <RefTypeName>$<methName>
        // - Internal methods: <RefTypeName>$<methName>$
        prog.structDefs.forEach(st -> {
            // constructor
            writeConstructorSignature(codeBuilder, st);
            codeBuilder.write(";");

            // destructor
            writeDestructorSignature(codeBuilder, st);
            codeBuilder.write(";");

            // to_string
            writeToStringSignature(codeBuilder, st);
            codeBuilder.write(";");

            codeBuilder.write("\n");
        });

        // Declare static funcitons
        prog.funDefs.forEach(f -> f.accept(this));
        codeBuilder.write("\n");

        // Generate struct definitions
        prog.structDefs.forEach(st -> st.accept(this));

        codeBuilder.writeIndented("#endif // ", guardName, "\n");

        // Dump source code to file
        writeCFile(codeBuilder);
    }
}
