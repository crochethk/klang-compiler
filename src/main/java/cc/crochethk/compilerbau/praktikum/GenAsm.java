package cc.crochethk.compilerbau.praktikum;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import cc.crochethk.compilerbau.praktikum.ast.*;
import cc.crochethk.compilerbau.praktikum.ast.literals.*;
import utils.Result;

public class GenAsm extends CodeGenVisitor<Writer> {
    private static final String FILE_EXT = ".s";

    @Override
    public String outFilePath() {
        return Path.of(outDir, packageName + "." + className + FILE_EXT).toString();
    }

    Writer writer;

    /** Function argument registers (x86_64, Linux System V ABI) */
    String[] rs = { "%rdi", "%rsi", "%rdx", "%rcx", "%r8", "%r9" };

    public GenAsm(String outputDir, String packageName, String className) throws IOException {
        super(outputDir, packageName, className);

        var filePath = outFilePath();
        var parentDir = Path.of(filePath).getParent();
        parentDir = parentDir != null ? parentDir : Path.of("");
        Files.createDirectories(parentDir);
        this.writer = new FileWriter(filePath);
    }

    /** Write multiple strings into new indented line */
    private Writer writeIndented(String... ss) {
        write("\n\t");
        for (var s : ss)
            write(s);
        return writer;
    }

    /** Write the string into new indented line */
    private Writer writeIndented(String s) {
        return write("\n\t", s);
    }

    private Writer write(String... ss) {
        for (var s : ss)
            write(s);
        return writer;
    }

    private Writer write(String s) {
        try {
            writer.write(s);
        } catch (IOException e) {
            exitStatus = Result.Err;
            throw new RuntimeException(e);
        }
        return writer;
    }

    @Override
    public Writer visit(I64Lit i64Lit) {
        return writeIndented("movq", "\t", "$" + i64Lit.value, ", ", "%rax");
    }

    @Override
    public Writer visit(F64Lit f64Lit) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Writer visit(BoolLit boolLit) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Writer visit(StringLit stringLit) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Writer visit(Var var) {
        return writeIndented(
                "movq", "\t", ctx.get(var.name).toString() + "(%rbp)", ", ", "%rax");
    }

    @Override
    public Writer visit(FunCall funCall) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Writer visit(BinOpExpr binOpExpr) {
        // TODO use actual operators
        // TODO hardcoded ADD for now!
        binOpExpr.lhs.accept(this);
        writeIndented("movq", "\t", "%rax", ", ", "%rdx");
        binOpExpr.rhs.accept(this);
        return writeIndented("addq", "\t", "%rdx", ", ", "%rax");
    }

    @Override
    public Writer visit(UnaryOpExpr unaryOpExpr) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Writer visit(TernaryConditionalExpr ternaryConditionalExpr) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Writer visit(VarDeclareStat varDeclareStat) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Writer visit(VarAssignStat varAssignStat) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Writer visit(IfElseStat ifElseStat) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Writer visit(LoopStat loopStat) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Writer visit(StatementList statementList) {
        statementList.statements.forEach(s -> s.accept(this));
        return writer;
    }

    @Override
    public Writer visit(ReturnStat returnStat) {
        returnStat.expr.accept(this);
        /* Epilogue */
        // Restore caller's context:
        // -> Copy %rbp to %rsp and then replace %rbp with the stored value
        // -> Can be reduced to "popq" part in certain cases.
        writeIndented("leave");
        return writeIndented("ret");
    }

    @Override
    public Writer visit(BreakStat breakStat) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Writer visit(TypeNode type) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Serves as lookup for basepointer offsets of variables stored on the stack
     * in the current context.
     */
    Map<String, Integer> ctx = null;

    @Override
    public Writer visit(FunDef funDef) {
        var oldCtx = ctx;
        ctx = new HashMap<>();

        writeIndented(".globl\t", funDef.name);
        writeIndented(".type\t", funDef.name, ", @function");
        write("\n", funDef.name, ":");

        /* Prologue */
        // Backup caller's and set callee's context
        writeIndented("pushq", "\t", "%rbp");
        writeIndented("movq", "\t", "%rsp, %rbp");
        var regParamsCount = Math.min(funDef.params.size(), rs.length);
        var baseoffset = 0;
        for (int i = 0; i < regParamsCount; i++) {
            baseoffset -= 8;
            ctx.put(funDef.params.get(i).name(), baseoffset);
            writeIndented("movq", "\t", rs[i], ", ", baseoffset + "(%rbp)");
        }

        // when >6 params: get offsets of caller-saved params
        baseoffset = 16; //skip rbp backup and return addr
        for (int i = regParamsCount; i < funDef.params.size(); i++) {
            ctx.put(funDef.params.get(i).name(), baseoffset);
            baseoffset += 8;
        }

        // Generate body instructions
        funDef.body.accept(this);

        // Append epilogue in case of implicit return
        if (funDef.body.isEmpty()
                || (!funDef.body.statements.getLast().returnsControlFlow()
                        && funDef.returnType.theType.equals(Type.VOID_T))) {
            writeIndented("leave");
            writeIndented("ret");
        }

        ctx = oldCtx;
        return writer;
    }

    @Override
    public Writer visit(Prog prog) {
        exitStatus = Result.Ok;

        prog.funDefs.forEach(f -> f.accept(this));
        writeIndented(".section\t.note.GNU-stack,\"\",@progbits");
        write("\n");
        try {
            writer.close();
        } catch (IOException e) {
            System.err.println(e);
            exitStatus = Result.Err;
        }
        return writer;
    }

    @Override
    public Writer visit(EmptyNode emptyNode) {
        return writer;
    }

}
