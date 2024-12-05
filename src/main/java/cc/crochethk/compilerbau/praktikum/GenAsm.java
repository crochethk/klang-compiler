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
    StackManager stack = null;

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
        return acw.movq(stack.get(var.name), rax);
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
        stack.store(varDeclareStat.varName, varDeclareStat.theType.byteSize());
        return acw;
    }

    @Override
    public AsmCodeWriter visit(VarAssignStat varAssignStat) {
        varAssignStat.expr.accept(this);
        acw.movq(rax, stack.get(varAssignStat.targetVarName));
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

    @Override
    public AsmCodeWriter visit(FunDef funDef) {
        var oldCtx = stack;
        stack = new StackManager(acw);

        acw.writeIndented(".globl\t", funDef.name);
        acw.writeIndented(".type\t", funDef.name, ", @function");
        acw.write("\n", funDef.name, ":");

        /* Prologue */
        // Backup caller's and set callee's context
        acw.pushq(rbp);
        acw.movq(rsp, rbp);

        // Adjust %rsp as necessary
        var regArgsCount = Math.min(funDef.params.size(), regs.length);
        stack.alloc(regArgsCount * 8); // Convention: 8 byte per fun arg

        for (var stat : funDef.body.statements) {
            if (stat instanceof VarDeclareStat decl) {
                stack.alloc(decl.theType.byteSize());
            }
        }
        stack.alignRspToStackSize();

        // Store register args on stack
        for (int i = 0; i < regArgsCount; i++) {
            var arg = funDef.params.get(i);
            // Convention: 8 byte per fun arg
            stack.store(arg.name(), 8, regs[i].toString());
        }

        // Get offsets for caller-saved args (when >6 params)
        var baseOffset = 16; //skip rbp backup and return addr
        for (int i = regArgsCount; i < funDef.params.size(); i++) {
            stack.put(funDef.params.get(i).name(), baseOffset);
            baseOffset += 8;
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

        stack = oldCtx;
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

    class StackManager {
        private AsmCodeWriter acw;

        /**
         * Lookup for basepointer offsets of variables stored on stack in the current context.
         */
        private Map<String, Integer> ctx;

        /** Current basepointer (%rbp) offset. It's the most recently stored element's offset. */
        private int baseOffset = 0;

        /** Unaligned byte size reserved in the current stack frame (e.g. by local vars). */
        private int allocSize = 0;

        public StackManager(AsmCodeWriter asmCodeWriter) {
            this.acw = asmCodeWriter;
            this.ctx = new HashMap<>();
        }

        /**
         * Decrements %rsp such that it is offset by the 16-byte-aligned stacksize that
         * was reserved using "alloc" method until this point.
         */
        public void alignRspToStackSize() {
            var size = getAlignedSize();
            if (size > 0)
                acw.subq("$" + size, rsp);
        }

        /** Returns allocSize aligned such that it's multiple of 16 Bytes. */
        private int getAlignedSize() {
            return (allocSize + 0xF) & ~0xF;
        }

        /**
         * Increases the number of bytes the manager will use to determine the 
         * %rsp address for the current stack frame.
         * This operation does not actually allocate resources. Rather its purpose
         * is to help computing the required stack size in the prologue of the current
         * function (i.e. before actually changing %rsp or storing elements on stack),
         * such that %rsp can be set accordingly in advance for potential "call" instructions
         * (without repeatedly doing "subq/addq").
         */
        public void alloc(int bytes) {
            allocSize += bytes;
        }

        /**
         * Writes the value of given size from source to stack and stores it using
         * varName for later lookup.
         * @param name The value's name for later referencing.
         * @param size How many bytes the value should occupy.
         * @param source The value's location (register, memoryaddress, constant)
         */
        public void store(String name, int size, String source) {
            baseOffset -= size;
            ctx.put(name, baseOffset);
            acw.movq(source, this.get(name));
        }

        /**
         * Stores an uninitialized element.
         * @see #store(String, int, String)
         */
        public void store(String name, int size) {
            baseOffset -= size;
            ctx.put(name, baseOffset);
        }

        /**
         * Adds stack element reference to this manager. The element's location
         * can later be retrieved using the "get" method.
         * @param name The name for later lookup.
         * @param baseOffset The offset relative to &rbp.
         * @see #get(String)
        */
        public void put(String name, int baseOffset) {
            ctx.put(name, baseOffset);
        }

        /** Returns the ready to use memory location of the element referenced by varName. */
        public String get(String varName) {
            return ctx.get(varName) + "(" + rbp + ")";
        }
    }

    class AsmCodeWriter {
        private Writer writer;

        AsmCodeWriter(Writer writer) {
            this.writer = writer;
        }

        AsmCodeWriter movq(Object source, Object destination) {
            return writeIndented("movq", "\t", source.toString(), ", ", destination.toString());
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

        AsmCodeWriter subq(Object lhs, Object rhs) {
            return writeIndented("subq", "\t", lhs.toString(), ", ", rhs.toString());
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
