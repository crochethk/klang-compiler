package cc.crochethk.compilerbau.praktikum.visitor.codegen.asm.helpers;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import cc.crochethk.compilerbau.praktikum.ast.*;
import cc.crochethk.compilerbau.praktikum.ast.literal.*;
import cc.crochethk.compilerbau.praktikum.visitor.SourceCodeBuilder;
import cc.crochethk.compilerbau.praktikum.visitor.Type;
import cc.crochethk.compilerbau.praktikum.visitor.codegen.CodeGenVisitor;

public abstract class GenCBase extends CodeGenVisitor {
    private static final String INDENT_SEQ = "  ";

    protected final SourceCodeBuilder codeBuilder;
    private final String fileExt;

    public GenCBase(String outputDir, String packageName, String className, String fileExt) {
        super(outputDir, packageName, className);
        codeBuilder = new SourceCodeBuilder(INDENT_SEQ, 0);
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

    protected void writeCFile(SourceCodeBuilder scb) {
        try (var file = new FileWriter(filePath())) {
            file.write(scb.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // --------------------[ Builtins Generation ]------------------------------
    protected void writeConstructorSignature(SourceCodeBuilder scb, StructDef st) {
        var refType = st.theType;
        List<Parameter> fields = st.fields;
        scb.writeIndented(refType.cTypeName(), " ",
                refType.klangName(), "$new$", "(", formatParams(fields), ")");
    }

    protected void writeDestructorSignature(SourceCodeBuilder scb, StructDef st) {
        var refType = st.theType;
        scb.writeIndented("void ",
                getDestructorFullName(refType), "(", refType.cTypeName(), " this)");
    }

    protected String getDestructorFullName(Type refType) {
        return refType.klangName() + "$drop$";
    }

    protected void writeToStringSignature(SourceCodeBuilder scb, StructDef st) {
        var refType = st.theType;
        scb.writeIndented("char* ",
                refType.klangName(), "$to_string(", refType.cTypeName(), " this)");
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

    // --------------------[ Empty Visitor Overrides ]--------------------------
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
    }

    @Override
    public void visit(FunDef funDef) {
    }

    @Override
    public void visit(StructDef structDef) {
    }

    @Override
    public void visit(Prog prog) {
    }

    @Override
    public void visit(EmptyNode emptyNode) {
    }
}
