package cc.crochethk.klang.visitor.codegen.asm.helpers;

import java.util.HashMap;
import java.util.Map;

import cc.crochethk.klang.ast.*;
import cc.crochethk.klang.visitor.SourceCodeBuilder;
import cc.crochethk.klang.visitor.Type;

/**
 * Generates a C-File containing helper implementations for some of the 
 * functions declared in the respective, also generated header file,
 * based on the traversed AST.
 */
public class GenCImpls extends GenCBase {
    public GenCImpls(String outputDir, String packageName, String className) {
        super(outputDir, packageName, className, ".c");
    }

    @Override
    public void visit(StructDef structDef) {
        implConstructor(codeBuilder, structDef);
        implDestructor(codeBuilder, structDef);
        implToString(codeBuilder, structDef);
        structDef.fields.forEach(f -> {
            implGetter(codeBuilder, structDef, f);
            implSetter(codeBuilder, structDef, f);
        });
    }

    private Map<String, StructDef> structDefs = null;

    @Override
    public void visit(Prog prog) {
        // C preamble
        codeBuilder.write("#include <stdio.h>");
        codeBuilder.writeIndented("#include <stdlib.h>");
        codeBuilder.writeIndented("#include <string.h>");
        codeBuilder.writeIndented("#include \"", fileNameNoExt(), ".h", "\"", "\n");

        // Add struct definitions to lookup table
        structDefs = new HashMap<>();
        prog.structDefs.forEach(st -> structDefs.put(st.name, st));

        // Generate struct auto-method implementations
        prog.structDefs.forEach(st -> st.accept(this));

        // Dump source code to file
        writeCFile(codeBuilder);
    }

    // --------------------[ Builtins Generation ]------------------------------

    private void implConstructor(SourceCodeBuilder scb, StructDef st) {
        var thisPtrT = st.theType.cTypeName();
        // constructor
        writeConstructorSignature(scb, st);
        scb.write(" {");
        scb.increaseIndent();
        scb.writeIndented(thisPtrT, " this = (", thisPtrT, ")malloc(sizeof(*this));");
        st.fields.forEach(
                f -> scb.writeIndented("this->", f.name(), " = ", f.name(), ";"));
        scb.writeIndented("return this;");
        scb.decreaseIndent();
        scb.writeIndented("}\n");
    }

    private void implDestructor(SourceCodeBuilder scb, StructDef st) {
        writeDestructorSignature(scb, st);
        scb.write(" {");
        scb.increaseIndent();
        writeDestructorDefinition(scb, st.theType);
        scb.decreaseIndent();
        scb.writeIndented("}\n");
    }

    private void implToString(SourceCodeBuilder scb, StructDef st) {
        writeToStringSignature(scb, st);
        scb.write(" {");
        scb.increaseIndent();
        scb.writeIndented("char* r = strdup(\"", st.name, "(\");");
        if (!st.fields.isEmpty()) {
            scb.writeIndented("char* fStr;");
            scb.writeIndented("char* tmpRes;");
            for (var it = st.fields.iterator(); it.hasNext();) {
                var f = it.next();
                // Put current field's stringified value to "fStr"
                if (f.type().theType.isReference()) {
                    scb.writeIndented("fStr = ");
                    if (f.type().theType == Type.STRING_T) {
                        scb.write("strdup(this->", f.name(), ");");
                    } else {
                        scb.write(f.type().typeToken, "$to_string(this->", f.name(), ");");
                    }
                } else {
                    scb.writeIndented("fStr = malloc(22);");
                    scb.writeIndented("sprintf(fStr,\"%ld\", this->", f.name(), ");");
                }

                // "+3" for the ", " or ")" suffix after each field (I guess)
                scb.writeIndented("tmpRes = malloc(strlen(r)+strlen(fStr)+3);");
                scb.writeIndented("tmpRes[0] = '\\0';"); // set strlen to 0
                scb.writeIndented("tmpRes = strcat(tmpRes,r);"); // copy current result
                scb.writeIndented("tmpRes = strcat(tmpRes,fStr);"); // append field value
                scb.writeIndented("tmpRes = strcat(tmpRes,", (it.hasNext() ? "\", \"" : "\"\""), ");");
                scb.writeIndented("free(r);");
                scb.writeIndented("free(fStr);");
                scb.writeIndented("r = tmpRes;");
            }
        }
        scb.writeIndented("r = strcat(r, \")\");");
        scb.writeIndented("return r;");
        scb.decreaseIndent();
        scb.writeIndented("}\n");
    }

    private void writeDestructorDefinition(SourceCodeBuilder scb, Type type) {
        if (!type.isReference()) {
            return;
        }

        if (type == Type.STRING_T) {
            scb.writeIndented("free(this);");
        } else {
            // Drop RefType fields
            var stDef = structDefs.get(type.klangName());
            for (var f : stDef.fields) {
                var fType = f.type().theType;
                if (!fType.isReference()) {
                    continue;
                }
                writeDestructorCall(scb, "this->" + f.name(), fType);
            }
            scb.writeIndented("free(this);");
        }
    }

    /**
     * Write a C statement that frees memory of the object referenced by the given
     * named reftype variable.
     * @param refTypeCVarString a String representing a C pointer variable.
     *          E.g. "this->field", where "field" is a reftype field in a struct
     *          instance referenced by the pointer "this").
     */
    private void writeDestructorCall(SourceCodeBuilder scb, String refTypeCVarString, Type refType) {
        if (refType == Type.STRING_T) {
            scb.writeIndented("free(", refTypeCVarString, ");");
        } else {
            scb.writeIndented(GenCBase.getDestructorFullName(refType), "(", refTypeCVarString, ");");
        }
    }

    private void implGetter(SourceCodeBuilder scb, StructDef st, Parameter field) {
        writeGetterSignature(scb, st, field);
        scb.write(" {");
        scb.increaseIndent();
        scb.writeIndented("return this->", field.name(), ";");
        scb.decreaseIndent();
        scb.writeIndented("}\n");
    }

    private void implSetter(SourceCodeBuilder scb, StructDef st, Parameter field) {
        writeSetterSignature(scb, st, field);
        scb.write(" {");
        scb.increaseIndent();
        scb.writeIndented("this->", field.name(), " = value;");
        scb.decreaseIndent();
        scb.writeIndented("}\n");
    }
}
