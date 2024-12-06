package cc.crochethk.compilerbau.praktikum;

import static cc.crochethk.compilerbau.praktikum.OperandSpecifier.Register.*;
import static cc.crochethk.compilerbau.praktikum.OperandSpecifier.Const.$;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import cc.crochethk.compilerbau.praktikum.ast.*;
import cc.crochethk.compilerbau.praktikum.ast.BinOpExpr.BinaryOp;
import cc.crochethk.compilerbau.praktikum.ast.literals.*;
import cc.crochethk.compilerbau.praktikum.GenAsm.AsmCodeWriter;
import cc.crochethk.compilerbau.praktikum.OperandSpecifier.MemAddr;
import cc.crochethk.compilerbau.praktikum.OperandSpecifier.Register;
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
        return acw.movq($(i64Lit.value), resultDest);
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
        return acw.movq(stack.get(var.name), resultDest);
    }

    @Override
    public AsmCodeWriter visit(FunCall funCall) {
        stack.alignRspToStackSize();

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
            acw.addq($(stackArgsOffset), rsp);
        }
        return acw;
    }

    /**
     * Operand specifier defining where the results of instructions generated by
     * intermediate visit/accept methods (i.e. called inside other visits)
     * should be placed (and thus the caller can expect to find).
     */
    private OperandSpecifier resultDest = rax;

    @Override
    public AsmCodeWriter visit(BinOpExpr binOpExpr) {
        withResultDestBackup(() -> {
            resultDest = rax;
            binOpExpr.lhs.accept(this);
            resultDest = rdx;
            binOpExpr.rhs.accept(this);
            // now operands in rax (lhs), rdx (rhs)

            resultDest = rax;
            genOpInstruction(acw, binOpExpr.lhs.theType, binOpExpr.op);
        });
        return acw;
    }

    /**
     * Generates instruction(s) for the  given operand type and operation.
     * Integer operands are expected in following locations: 
     *      lhs -> currentDestinationSpec, rhs -> %rdx
     */
    private void genOpInstruction(AsmCodeWriter acw, Type operandType, BinaryOp op) {
        boolean error = false;
        switch (op) {
            // Arithmetic
            case add -> {
                if (operandType == Type.LONG_T) {
                    acw.addq(rdx, resultDest);
                    // TODO
                } else {
                    // TODO implement case "operandType == Type.DOUBLE_T"
                    error = true;
                }
            }
            case sub -> {
                if (operandType == Type.LONG_T) {
                    acw.subq(rdx, resultDest);
                } else {
                    // TODO implement case "operandType == Type.DOUBLE_T"
                    error = true;
                }
            }
            case mult -> {
                if (operandType == Type.LONG_T) {
                    // TODO
                } else {
                    // TODO implement case "operandType == Type.DOUBLE_T"
                    error = true;
                }
            }
            case div -> {
                if (operandType == Type.LONG_T) {
                    // TODO
                } else {
                    // TODO implement case "operandType == Type.DOUBLE_T"
                    error = true;
                }
            }
            // TODO
            // case mod -> {}
            // case pow -> {}

            // Comparison
            case eq, neq, gt, gteq, lt, lteq -> {
                if (operandType == Type.LONG_T) {
                    // TODO
                } else {
                    // TODO implement case "operandType == Type.DOUBLE_T"
                    error = true;
                }
            }

            // Boolean
            case and -> error = true;//TODO
            case or -> error = true; //TODO
            default -> error = true;
        }

        if (error) {
            throw new UnsupportedOperationException("Operation '"
                    + op + "' not supported for '" + operandType + ", " + operandType + "'");
        }
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
        withResultDestBackup(() -> {
            resultDest = rax;
            varAssignStat.expr.accept(this);
            acw.movq(resultDest, stack.get(varAssignStat.targetVarName));
        });
        return acw;
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

        // Store register args on stack
        for (int i = 0; i < regArgsCount; i++) {
            var arg = funDef.params.get(i);
            // Convention: 8 byte per fun arg
            stack.store(arg.name(), 8, regs[i]);
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

    /**
     * Runs given action while making sure the resultDest field is restored to the
     * value when the action was called.
     */
    private void withResultDestBackup(Runnable action) {
        OperandSpecifier backup = resultDest;
        action.run();
        resultDest = backup;
    }

    class StackManager {
        private AsmCodeWriter acw;

        /**
         * Lookup for basepointer offsets of variables stored on stack in the current context.
         */
        private Map<String, Integer> ctx;

        /** Basepointer (%rbp) offset used to determine where the next element will be stored */
        private int writeOffset = 0;

        /**
         * Unaligned byte size that will be allocated in the current stack frame (e.g. for local vars)
         * on next {@link #alignRspToStackSize()} call.
         */
        private int pendingAllocSize = 0;

        /** Currently allocated stack frame size including any alignment padding */
        private int alignedStackSize = 0;

        public StackManager(AsmCodeWriter asmCodeWriter) {
            this.acw = asmCodeWriter;
            this.ctx = new HashMap<>();
        }

        /**
         * Increases current frame's stacksize by decrementing %rsp by the
         * 16-byte-aligned {@link #pendingAllocSize}.
         */
        public void alignRspToStackSize() {
            var size = getAlignedSize();
            if (size > 0) {
                acw.subq($(size), rsp);
                alignedStackSize += size;
                writeOffset = alignedStackSize; // synchronize write head
            }
            pendingAllocSize = 0;
        }

        /** Returns allocSize aligned such that it's multiple of 16 Bytes. */
        private int getAlignedSize() {
            return (pendingAllocSize + 0xF) & ~0xF;
        }

        /**
         * Increases the number of bytes the manager will use to determine the 
         * %rsp address for the current stack frame.
         * This operation does not actually allocate resources. Rather its purpose
         * is to help computing the required stack size in the prologue of the current
         * function (i.e. before actually changing %rsp or storing elements on stack).
         * %rsp must be aligned before any "call" instructions using {@link #alignRspToStackSize()}.
         */
        public void alloc(int bytes) {
            pendingAllocSize += bytes;
        }

        /**
         * Writes the value of given size from source to stack and stores it using
         * varName for later lookup.
         * @param name The value's name for later referencing.
         * @param size How many bytes the value should occupy.
         * @param source The value's source location (register, memoryaddress, constant)
         */
        public void store(String name, int size, OperandSpecifier source) {
            writeOffset -= size;
            ctx.put(name, writeOffset);
            acw.movq(source, this.get(name));
        }

        /**
         * Stores an uninitialized element.
         * @see #store(String, int, OperandSpecifier)
         */
        public void store(String name, int size) {
            writeOffset -= size;
            ctx.put(name, writeOffset);
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

        /**
         * Returns the ready to use memory location as operand specifier for
         * the element referenced by varName.
         */
        public OperandSpecifier get(String varName) {
            return new MemAddr(ctx.get(varName), rbp);
        }
    }

    /**
     * Class providing interface for writing assembly instructions.
     * 
     * Arguments for most instructions must implement the OperandSpecifier interface,
     * providing a valid Register, Immediate or Memoryaddress string.
     */
    class AsmCodeWriter {
        private Writer writer;

        AsmCodeWriter(Writer writer) {
            this.writer = writer;
        }

        AsmCodeWriter movq(OperandSpecifier source, OperandSpecifier destination) {
            return writeIndented("movq", "\t", source.operandSpec(), ", ", destination.operandSpec());
        }

        AsmCodeWriter pushq(OperandSpecifier source) {
            return writeIndented("pushq", "\t", source.operandSpec());
        }

        AsmCodeWriter call(String name) {
            return writeIndented("call", "\t", name);
        }

        AsmCodeWriter leave() {
            return writeIndented("leave");
        }

        AsmCodeWriter ret() {
            return writeIndented("ret");
        }

        /** Add source to destination */
        AsmCodeWriter addq(OperandSpecifier src, OperandSpecifier dst) {
            return writeIndented("addq", "\t", src.operandSpec(), ", ", dst.operandSpec());
        }

        /** Subtract source from destination */
        AsmCodeWriter subq(OperandSpecifier src, OperandSpecifier dst) {
            return writeIndented("subq", "\t", src.operandSpec(), ", ", dst.operandSpec());
        }

        /** Multiply destination by source */
        AsmCodeWriter imulq(OperandSpecifier src, OperandSpecifier dst) {
            return writeIndented("imulq", "\t", src.operandSpec(), ", ", dst.operandSpec());
        }

        /** Write multiple strings into new indented line */
        AsmCodeWriter writeIndented(String... ss) {
            write("\n\t");
            for (var s : ss)
                write(s);
            return this;
        }

        /** Write the string into new indented line */
        AsmCodeWriter writeIndented(String s) {
            return write("\n\t", s);
        }

        AsmCodeWriter write(String... ss) {
            for (var s : ss)
                write(s);
            return this;
        }

        AsmCodeWriter write(String s) {
            try {
                writer.write(s);
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
