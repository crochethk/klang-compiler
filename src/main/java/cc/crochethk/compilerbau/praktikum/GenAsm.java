package cc.crochethk.compilerbau.praktikum;

import static cc.crochethk.compilerbau.praktikum.Register.*;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import cc.crochethk.compilerbau.praktikum.ast.*;
import cc.crochethk.compilerbau.praktikum.ast.literals.*;
import cc.crochethk.compilerbau.praktikum.GenAsm.AsmCodeWriter;
import utils.Result;

public class GenAsm extends CodeGenVisitor<AsmCodeWriter> {
    private static final String FILE_EXT = ".s";

    AsmCodeWriter acw;

    /** Function argument registers */
    private final Register[] regs = { rdi, rsi, rdx, rcx, r8, r9 };

    public GenAsm(String outputDir, String packageName, String className) throws IOException {
        super(outputDir, packageName, className);

        var filePath = outFilePath();
        var parentDir = Path.of(filePath).getParent();
        parentDir = parentDir != null ? parentDir : Path.of("");
        Files.createDirectories(parentDir);
        this.acw = new AsmCodeWriter(new FileWriter(filePath));
    }

    @Override
    public String outFilePath() {
        return Path.of(outDir, packageName + "." + className + FILE_EXT).toString();
    }

    @Override
    public AsmCodeWriter visit(I64Lit i64Lit) {
        return acw.movq("$" + i64Lit.value, rax);
    }

    @Override
    public AsmCodeWriter visit(F64Lit f64Lit) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public AsmCodeWriter visit(BoolLit boolLit) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public AsmCodeWriter visit(StringLit stringLit) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public AsmCodeWriter visit(Var var) {
        return acw.movq(ctx.get(var.name).toString() + "(" + rbp + ")", rax);
    }

    @Override
    public AsmCodeWriter visit(FunCall funCall) {
        var regArgsCount = Math.min(funCall.args.size(), regs.length);
        var stackArgsOffset = Math.max(funCall.args.size() - regs.length, 0) * 8;
        for (int i = regArgsCount; i < funCall.args.size(); i++) {
            funCall.args.get(i).accept(this);
            acw.pushq(rax);
        }

        for (int i = 0; i < regArgsCount; i++) {
            funCall.args.get(i).accept(this);
            acw.movq(rax, regs[i]);
        }
        acw.call(funCall.name);
        //release stack args if necessary
        if (stackArgsOffset > 0) {
            acw.addq("$" + stackArgsOffset, rsp);
        }
        return acw;
    }

    @Override
    public AsmCodeWriter visit(BinOpExpr binOpExpr) {
        // TODO use actual operators
        // TODO hardcoded ADD for now!
        binOpExpr.lhs.accept(this);
        acw.movq(rax, rdx);
        binOpExpr.rhs.accept(this);
        return acw.addq(rdx, rax);
    }

    @Override
    public AsmCodeWriter visit(UnaryOpExpr unaryOpExpr) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public AsmCodeWriter visit(TernaryConditionalExpr ternaryConditionalExpr) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public AsmCodeWriter visit(VarDeclareStat varDeclareStat) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public AsmCodeWriter visit(VarAssignStat varAssignStat) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public AsmCodeWriter visit(IfElseStat ifElseStat) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public AsmCodeWriter visit(LoopStat loopStat) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public AsmCodeWriter visit(StatementList statementList) {
        statementList.statements.forEach(s -> s.accept(this));
        return acw;
    }

    @Override
    public AsmCodeWriter visit(ReturnStat returnStat) {
        returnStat.expr.accept(this);
        /* Epilogue */
        // Restore caller's context:
        // -> Copy %rbp to %rsp and then replace %rbp with the stored value
        // -> Can be reduced to "popq" part in certain cases.
        acw.leave();
        return acw.ret();
    }

    @Override
    public AsmCodeWriter visit(BreakStat breakStat) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public AsmCodeWriter visit(TypeNode type) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Serves as lookup for basepointer offsets of variables stored on the stack
     * in the current context.
     */
    Map<String, Integer> ctx = null;

    @Override
    public AsmCodeWriter visit(FunDef funDef) {
        var oldCtx = ctx;
        ctx = new HashMap<>();

        acw.writeIndented(".globl\t", funDef.name);
        acw.writeIndented(".type\t", funDef.name, ", @function");
        acw.write("\n", funDef.name, ":");

        /* Prologue */
        // Backup caller's and set callee's context
        acw.pushq(rbp);
        acw.movq(rsp, rbp);
        var regArgsCount = Math.min(funDef.params.size(), regs.length);
        var baseoffset = 0;
        for (int i = 0; i < regArgsCount; i++) {
            baseoffset -= 8;
            ctx.put(funDef.params.get(i).name(), baseoffset);
            acw.movq(regs[i], baseoffset + "(" + rbp + ")");
        }

        // when >6 params: get offsets of caller-saved params
        baseoffset = 16; //skip rbp backup and return addr
        for (int i = regArgsCount; i < funDef.params.size(); i++) {
            ctx.put(funDef.params.get(i).name(), baseoffset);
            baseoffset += 8;
        }

        /* Actual work */
        // Generate body instructions
        funDef.body.accept(this);

        // Append epilogue in case of implicit return
        if (funDef.body.isEmpty()
                || (!funDef.body.statements.getLast().returnsControlFlow()
                        && funDef.returnType.theType.equals(Type.VOID_T))) {
            acw.leave();
            acw.ret();
        }

        ctx = oldCtx;
        return acw;
    }

    @Override
    public AsmCodeWriter visit(Prog prog) {
        exitStatus = Result.Ok;

        prog.funDefs.forEach(f -> f.accept(this));
        acw.writeIndented(".section\t.note.GNU-stack,\"\",@progbits", "\n");
        try {
            acw.closeWriter();
        } catch (IOException e) {
            System.err.println(e);
            exitStatus = Result.Err;
        }
        return acw;
    }

    @Override
    public AsmCodeWriter visit(EmptyNode emptyNode) {
        return acw;
    }

    class AsmCodeWriter {
        private Writer writer;

        AsmCodeWriter(Writer writer) {
            this.writer = writer;
        }

        AsmCodeWriter movq(Object source, Object target) {
            return writeIndented("movq", "\t", source.toString(), ", ", target.toString());
        }

        AsmCodeWriter pushq(Object source) {
            return writeIndented("pushq", "\t", source.toString());
        }

        AsmCodeWriter call(Object name) {
            return writeIndented("call", "\t", name.toString());
        }

        AsmCodeWriter addq(Object lhs, Object rhs) {
            return writeIndented("addq", "\t", lhs.toString(), ", ", rhs.toString());
        }

        AsmCodeWriter leave() {
            return writeIndented("leave");
        }

        AsmCodeWriter ret() {
            return writeIndented("ret");
        }

        /** Write multiple strings into new indented line */
        AsmCodeWriter writeIndented(Object... ss) {
            write("\n\t");
            for (var s : ss)
                write(s);
            return this;
        }

        /** Write the string into new indented line */
        AsmCodeWriter writeIndented(Object s) {
            return write("\n\t", s);
        }

        AsmCodeWriter write(Object... ss) {
            for (var s : ss)
                write(s.toString());
            return this;
        }

        AsmCodeWriter write(Object s) {
            try {
                writer.write(s.toString());
            } catch (IOException e) {
                exitStatus = Result.Err;
                throw new RuntimeException(e);
            }
            return this;
        }

        void closeWriter() throws IOException {
            writer.close();
        }
    }
}
