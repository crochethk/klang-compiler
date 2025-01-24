package cc.crochethk.klang.visitor.codegen.asm.helpers;

import java.util.ArrayList;
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

        // "%s, %ld, ..."
        StringBuilder fmt = new StringBuilder();
        StringBuilder fmtArgs = new StringBuilder();

        fmt.append(st.name).append("(");

        var tmpVarNames = new ArrayList<String>();

        for (var it = st.fields.iterator(); it.hasNext();) {
            var field = it.next();
            var fType = field.type().theType;

            if (fType.isReference() && fType != Type.STRING_T) {
                var tmpVarName = "str_" + field.name();
                tmpVarNames.add(tmpVarName);

                // Declare a temp variable and assign the delegated "to_string" outcome
                scb.writeIndented("char* ", tmpVarName, " = ",
                        getToStringFullName(fType), "(this->", field.name(), ");");

                fmt.append("%s");
                fmtArgs.append(tmpVarName);
            } else {
                // field type has a matching C format specifier
                fmt.append(getTypeFormat(fType));
                fmtArgs.append("this->").append(field.name());
            }

            // if not last, add trailing comma
            if (it.hasNext()) {
                fmt.append(", ");
                fmtArgs.append(", ");
            }
        }
        fmt.append(")");
        var fmtArgsStr = fmtArgs.toString();

        scb.writeIndented("const char* fmt = \"", fmt.toString(), "\";");
        // Calculate required buffer size
        scb.writeIndented("int bufsize = 1 + snprintf(NULL, 0, fmt");
        if (!st.fields.isEmpty()) {
            scb.write(", ", fmtArgsStr);
        }
        scb.write(");");
        // Allocate buffer
        scb.writeIndented("char* buffer = (char*)malloc(bufsize);");
        // Format result into buffer
        scb.writeIndented("snprintf(buffer, bufsize, fmt");
        if (!st.fields.isEmpty()) {
            scb.write(", ", fmtArgsStr);
        }
        scb.write(");");
        // Release temporary strings
        tmpVarNames.forEach(varName -> scb.writeIndented("free(", varName, ");"));

        scb.writeIndented("return buffer;");
        scb.decreaseIndent();
        scb.writeIndented("}\n");
    }

    private static final Map<Type, String> TYPE_FORMATS = Map.of(
            Type.BOOL_T, "%d",
            Type.DOUBLE_T, "%f",
            Type.LONG_T, "%ld",
            Type.STRING_T, "\\\"%s\\\"");

    private String getTypeFormat(Type t) {
        return TYPE_FORMATS.getOrDefault(t, "%p");
    }

    private void writeDestructorDefinition(SourceCodeBuilder scb, Type type) {
        if (!type.isReference()) {
            return;
        }

        if (type == Type.STRING_T) {
            // scb.writeIndented("free(this);");
            // scb.writeIndented("string$drop$(this);");
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
            // scb.writeIndented("free(", refTypeCVarString, ");");
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
