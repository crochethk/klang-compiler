package cc.crochethk.compilerbau.praktikum;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

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

    private Writer writeIndented(String s) {
        write("\n\t");
        return write(s);
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
        return writeIndented("movq\t$" + i64Lit.value + ", %rax");
        // TODO Auto-generated method stub
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
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Writer visit(FunCall funCall) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Writer visit(BinOpExpr binOpExpr) {
        // TODO Auto-generated method stub
        return null;
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
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Writer visit(ReturnStat returnStat) {
        // TODO Auto-generated method stub
        return null;
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

    @Override
    public Writer visit(FunDef funDef) {
        writeIndented(".globl\t" + funDef.name);
        writeIndented(".type\t" + funDef.name + ", @function");
        write("\n" + funDef.name + ":");

        // Store caller's context
        writeIndented("pushq\t%rbp");
        // set callee's context
        writeIndented("movq\t%rsp, %rbp");

        // Restore caller's context:
        // -> Copy %rbp to %rsp and then replace %rbp with the stored value
        // -> Can be reduced to "popq" part in certain cases.
        writeIndented("leave");
        return writeIndented("ret");
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
        // TODO Auto-generated method stub
        return null;
    }

}
