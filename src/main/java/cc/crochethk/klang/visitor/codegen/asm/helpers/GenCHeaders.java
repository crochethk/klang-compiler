package cc.crochethk.klang.visitor.codegen.asm.helpers;

import java.util.List;

import cc.crochethk.klang.ast.*;
import cc.crochethk.klang.visitor.Type;
import cc.crochethk.klang.visitor.codegen.GenAsm;
import utils.SourcePos;

/** Generates a C-Header file based on the traversed AST. */
public class GenCHeaders extends GenCBase {
    public GenCHeaders(String outputDir, String packageName, String className) {
        super(outputDir, packageName, className, ".h");
    }

    @Override
    public void visit(FunDef funDef) {
        writeCFunSignature(funDef.returnType, funDef.name, funDef.params);
    }

    @Override
    public void visit(StructDef structDef) {
        // write actual struct definition
        scb.writeIndented("struct ", structDef.name, " {");
        scb.increaseIndent();
        for (var f : structDef.fields) {
            scb.writeIndent();
            // Write field type and name
            f.type().accept(this);
            scb.write(" ", f.name(), ";");
        }
        // add dummy field if empty
        if (structDef.fields.isEmpty())
            scb.writeIndented("char _$dummy$_;");
        scb.decreaseIndent();
        scb.writeIndented("};\n");

        // Declare struct methods
        structDef.methods.forEach(meth -> meth.accept(this));
    }

    @Override
    public void visit(MethDef methDef) {
        var ownerType = methDef.owner().theType;
        var methName = GenAsm.getAsmMethodName(ownerType, methDef.name());
        writeCFunSignature(methDef.returnType(), methName, methDef.params());
    }

    @Override
    public void visit(Prog prog) {
        var guardName = fileName().replaceAll("[\\. ]", "_").toUpperCase();
        // H preamble
        scb.write("// Auto-generated C header file");
        scb.writeIndented("#ifndef ", guardName);
        scb.writeIndented("#define ", guardName, "\n");

        scb.writeIndented("#include <stdbool.h>");
        scb.writeIndented("#include <stdint.h>");
        scb.writeIndented("#include <stdio.h>");
        scb.writeIndented("#include <stdlib.h>");
        scb.writeIndented("#include <string.h>", "\n");

        // Declare structs
        scb.writeIndented("// ----------[ Struct declarations ]----------\n");
        prog.structDefs.forEach(st -> scb.writeIndented("struct ", st.name, ";"));
        scb.write("\n");

        // Declare static functions
        scb.writeIndented("// ----------[ Function signatures ]----------\n");
        prog.funDefs.forEach(f -> f.accept(this));
        scb.write("\n");

        // Generate struct definitions
        scb.writeIndented(
                "// ----------[ Struct definitions and Method declarations ]----------\n");
        prog.structDefs.forEach(st -> st.accept(this));

        // Declare struct auto-methods
        // - Normal methods: <RefTypeName>$<methName>
        // - Internal methods: <RefTypeName>$<methName>$
        scb.writeIndented("// ----------[ Auto-Method Signatures ]----------\n");
        prog.structDefs.forEach(st -> {
            // constructor
            writeConstructorSignature(st);
            scb.write(";");

            // destructor
            writeDestructorSignature(st);
            scb.write(";");

            // to_string
            writeToStringSignature(st);
            scb.write(";");

            // field getters and setters
            st.fields.forEach(f -> {
                writeGetterSignature(st, f);
                scb.write(";");
                writeSetterSignature(st, f);
                scb.write(";");
            });

            scb.write("\n");
        });

        scb.writeIndented("// ----------[ klang 'stdlib' ]----------\n");
        writeStringHelpersSignatures();

        scb.writeIndented("\n#endif // ", guardName, "\n");
        // Dump source code to file
        writeCFile();
    }

    private void writeStringHelpersSignatures() {
        // Declare string helper functions
        var strTypeNode = new TypeNode(new SourcePos(-1, -1), Type.STRING_T.klangName());
        strTypeNode.theType = Type.STRING_T;

        writeConstructorSignature(Type.STRING_T, List.of(
                new Parameter("str", strTypeNode)));
        scb.write(";");

        writeDestructorSignature(Type.STRING_T);
        scb.write(";");

        writeToStringSignature(Type.STRING_T);
        scb.write(";");

        scb.writeIndented("size_t ", Type.STRING_T.klangName(), "$len(const ",
                Type.STRING_T.cTypeName(), " this)");
        scb.write(";");

        scb.writeIndented("char* ", Type.STRING_T.klangName(), "$concat(",
                "const ", Type.STRING_T.cTypeName(), " this, ",
                "const ", Type.STRING_T.cTypeName(), " other)");
        scb.write(";");
    }
}
