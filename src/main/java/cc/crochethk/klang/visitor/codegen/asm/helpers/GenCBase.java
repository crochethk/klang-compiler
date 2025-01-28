package cc.crochethk.klang.visitor.codegen.asm.helpers;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import cc.crochethk.klang.ast.*;
import cc.crochethk.klang.ast.MemberAccess.*;
import cc.crochethk.klang.ast.literal.*;
import cc.crochethk.klang.visitor.SourceCodeBuilder;
import cc.crochethk.klang.visitor.Type;
import cc.crochethk.klang.visitor.codegen.CodeGenVisitor;
import cc.crochethk.klang.visitor.codegen.GenAsm;

public abstract class GenCBase extends CodeGenVisitor {
    private static final String INDENT_SEQ = "  ";

    /** The builder used to construct this instance's source code */
    protected final SourceCodeBuilder scb;
    private final String fileExt;

    public GenCBase(String outputDir, String packageName, String className, String fileExt) {
        super(outputDir, packageName, className);
        scb = new SourceCodeBuilder(INDENT_SEQ, 0);
        this.fileExt = fileExt;
    }

    @Override
    public List<Path> outFilePaths() {
        return List.of(Path.of(filePath()));
    }

    protected String filePath() {
        return Path.of(outDir, fileName()).toString();
    }

    protected String fileName() {
        return fileNameNoExt() + fileExt;
    }

    protected String fileNameNoExt() {
        return packageName + "." + className;
    }

    protected void writeCFile() {
        try (var file = new FileWriter(filePath())) {
            file.write(scb.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // --------------------[ Builtins Generation ]------------------------------
    protected void writeConstructorSignature(StructDef st) {
        writeConstructorSignature(st.theType, st.fields);
    }

    protected void writeConstructorSignature(Type refType, List<Parameter> params) {
        scb.writeIndented(refType.cTypeName(), " ",
                GenAsm.getConstructorFullName(refType), "(", formatParams(params), ")");
    }
    // ---

    protected void writeDestructorSignature(StructDef st) {
        writeDestructorSignature(st.theType);
    }

    protected void writeDestructorSignature(Type refType) {
        scb.writeIndented("void ", GenAsm.getDestructorFullName(refType),
                "(", refType.cTypeName(), " this)");
    }
    // ---

    protected void writeToStringSignature(StructDef st) {
        writeToStringSignature(st.theType);
    }

    protected void writeToStringSignature(Type refType) {
        scb.writeIndented("char* ", GenAsm.getToStringFullName(refType),
                "(", refType.cTypeName(), " this)");
    }
    // ---

    protected void writeGetterSignature(StructDef st, Parameter field) {
        var refType = st.theType;
        scb.writeIndent();
        field.type().accept(this);
        scb.write(" ", GenAsm.getGetterFullName(refType, field.name()),
                "(", refType.cTypeName(), " this)");
    }

    protected void writeSetterSignature(StructDef st, Parameter field) {
        var refType = st.theType;
        scb.writeIndented("void ", GenAsm.getSetterFullName(refType, field.name()),
                "(", refType.cTypeName(), " this, ", field.type().theType.cTypeName(), " value", ")");
    }

    protected void writeCFunSignature(TypeNode returnType, String funName, List<Parameter> params) {
        scb.writeIndent();
        returnType.accept(this);
        scb.write(" ", funName, "(", formatParams(params), ");");
    }

    protected String formatParams(List<Parameter> params) {
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

    protected String formatTypeNode(TypeNode type) {
        return type.theType.cTypeName();
    }

    // --------------------[ Default Visitor Overrides ]------------------------
    @Override
    public void visit(I64Lit i64Lit) {
    }

    @Override
    public void visit(F64Lit f64Lit) {
    }

    @Override
    public void visit(BoolLit boolLit) {
    }

    @Override
    public void visit(StringLit stringLit) {
    }

    @Override
    public void visit(NullLit nullLit) {
    }

    @Override
    public void visit(Var var) {
    }

    @Override
    public void visit(FunCall funCall) {
    }

    @Override
    public void visit(MemberAccessChain memberAccessChain) {
    }

    @Override
    public void visit(MethodCall methodCall) {
    }

    @Override
    public void visit(FieldGet fieldGet) {
    }

    @Override
    public void visit(FieldSet fieldSet) {
    }

    @Override
    public void visit(ConstructorCall constructorCall) {
    }

    @Override
    public void visit(BinOpExpr binOpExpr) {
    }

    @Override
    public void visit(UnaryOpExpr unaryOpExpr) {
    }

    @Override
    public void visit(TernaryConditionalExpr ternaryConditionalExpr) {
    }

    @Override
    public void visit(VarDeclareStat varDeclareStat) {
    }

    @Override
    public void visit(VarAssignStat varAssignStat) {
    }

    @Override
    public void visit(FieldAssignStat fieldAssignStat) {
    }

    @Override
    public void visit(IfElseStat ifElseStat) {
    }

    @Override
    public void visit(LoopStat loopStat) {
    }

    @Override
    public void visit(StatementList statementList) {
    }

    @Override
    public void visit(ReturnStat returnStat) {
    }

    @Override
    public void visit(BreakStat breakStat) {
    }

    @Override
    public void visit(DropStat dropStat) {
    }

    @Override
    public void visit(TypeNode type) {
        scb.write(formatTypeNode(type));
    }

    @Override
    public void visit(FunDef funDef) {
    }

    @Override
    public void visit(StructDef structDef) {
    }

    @Override
    public void visit(MethDef methDef) {
    }

    @Override
    public void visit(Prog prog) {
    }

    @Override
    public void visit(EmptyNode emptyNode) {
    }
}
