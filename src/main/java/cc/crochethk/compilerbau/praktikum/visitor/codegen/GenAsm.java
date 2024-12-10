package cc.crochethk.compilerbau.praktikum.visitor.codegen;

import static cc.crochethk.compilerbau.praktikum.visitor.codegen.OperandSpecifier.Const.$;
import static cc.crochethk.compilerbau.praktikum.visitor.codegen.OperandSpecifier.Register.*;
import static cc.crochethk.compilerbau.praktikum.visitor.codegen.OperandSpecifier.XmmRegister.*;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import cc.crochethk.compilerbau.praktikum.ast.*;
import cc.crochethk.compilerbau.praktikum.ast.BinOpExpr.BinaryOp;
import cc.crochethk.compilerbau.praktikum.ast.literal.*;
import cc.crochethk.compilerbau.praktikum.visitor.Type;
import cc.crochethk.compilerbau.praktikum.visitor.codegen.asm.CodeSection;
import cc.crochethk.compilerbau.praktikum.visitor.codegen.asm.DataSection;
import cc.crochethk.compilerbau.praktikum.visitor.codegen.OperandSpecifier;
import cc.crochethk.compilerbau.praktikum.visitor.codegen.OperandSpecifier.MemAddr;
import cc.crochethk.compilerbau.praktikum.visitor.codegen.OperandSpecifier.Register;
import utils.Result;

public class GenAsm extends CodeGenVisitor<Void> {
    private static final String FILE_EXT = ".s";

    /** Readonly data section (".rodata") */
    DataSection rodataSec = new DataSection.ReadOnlyData();
    /** Mutable data section (".data") */
    DataSection dataSec = new DataSection.WritableData();
    /** Programm code section (".text") */
    CodeSection code = new CodeSection();

    StackManager stack = null;

    /** Function argument registers */
    private final Register[] regs = { rdi, rsi, rdx, rcx, r8, r9 };

    public GenAsm(String outputDir, String packageName, String className) throws IOException {
        super(outputDir, packageName, className);

        var filePath = outFilePath();
        var parentDir = filePath.getParent();
        parentDir = parentDir != null ? parentDir : Path.of("");
        Files.createDirectories(parentDir);
    }

    @Override
    public Path outFilePath() {
        return Path.of(outDir, packageName + "." + className + FILE_EXT);
    }

    @Override
    public Void visit(I64Lit i64Lit) {
        code.movq($(i64Lit.value), rax);
        return null;
    }

    private int localConstantCounter = 0;

    @Override
    public Void visit(F64Lit f64Lit) {
        // Create constant definition
        long allBits = Double.doubleToRawLongBits(f64Lit.value);
        int lowBits = (int) allBits;
        int highBits = (int) (allBits >> 32);
        rodataSec.write("\n.LC" + localConstantCounter + ":");
        rodataSec.writeIndented(".long\t" + lowBits);
        rodataSec.writeIndented(".long\t" + highBits);

        code.movsd(new MemAddr(".LC" + localConstantCounter, rip), xmm0);
        code.movq(xmm0, rax);

        localConstantCounter += 1;
        return null;
    }

    @Override
    public Void visit(BoolLit boolLit) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visit(StringLit stringLit) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visit(Var var) {
        code.movq(stack.get(var.name), rax);
        return null;
    }

    @Override
    public Void visit(FunCall funCall) {
        stack.alignRspToStackSize();

        var regArgsCount = Math.min(funCall.args.size(), regs.length);
        var stackArgsOffset = Math.max(funCall.args.size() - regs.length, 0) * 8;
        for (int i = regArgsCount; i < funCall.args.size(); i++) {
            funCall.args.get(i).accept(this);
            code.pushq(rax);
        }

        for (int i = 0; i < regArgsCount; i++) {
            funCall.args.get(i).accept(this);
            code.movq(rax, regs[i]);
        }
        code.call(funCall.name);
        //release stack args if necessary
        if (stackArgsOffset > 0) {
            code.addq($(stackArgsOffset), rsp);
        }
        return null;
    }

    @Override
    public Void visit(BinOpExpr binOpExpr) {
        binOpExpr.rhs.accept(this);
        var lhsNotComplex = binOpExpr.lhs instanceof LiteralExpr || binOpExpr.lhs instanceof Var;
        // Put rdx on stack, if rdx might be overwritten when evaluating lhs
        OperandSpecifier rhsResLoc = lhsNotComplex ? rdx : stack.reserveSlot(8);
        code.movq(rax, rhsResLoc);

        binOpExpr.lhs.accept(this);
        // now operands in %rax (lhs), "rhsResLoc" (rhs)

        genOpInstruction(code, binOpExpr.lhs.theType, binOpExpr.op, rhsResLoc, rax);
        return null;
    }

    /**
     * Generates instruction(s) for the given operand type, operator and
     * source/destination operand specifiers.
     *
     * @param src The source. It's the operation's RHS.
     * @param dst The destination. It's the operation's LHS.
     */
    private void genOpInstruction(CodeSection code, Type operandType,
            BinaryOp op, OperandSpecifier src, OperandSpecifier dst) {
        boolean error = false;
        switch (op) {
            // Arithmetic
            case add -> {
                if (operandType == Type.LONG_T) {
                    code.addq(src, dst);
                } else {
                    // TODO implement case "operandType == Type.DOUBLE_T"
                    error = true;
                }
            }
            case sub -> {
                if (operandType == Type.LONG_T) {
                    code.subq(src, dst);
                } else {
                    // TODO implement case "operandType == Type.DOUBLE_T"
                    error = true;
                }
            }
            case mult -> {
                if (operandType == Type.LONG_T) {
                    code.imulq(src, dst);
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
    public Void visit(UnaryOpExpr unaryOpExpr) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visit(TernaryConditionalExpr ternaryConditionalExpr) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visit(VarDeclareStat varDeclareStat) {
        stack.store(varDeclareStat.varName, varDeclareStat.theType.byteSize());
        return null;
    }

    @Override
    public Void visit(VarAssignStat varAssignStat) {
        varAssignStat.expr.accept(this);
        code.movq(rax, stack.get(varAssignStat.targetVarName));
        return null;
    }

    @Override
    public Void visit(IfElseStat ifElseStat) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visit(LoopStat loopStat) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visit(StatementList statementList) {
        statementList.statements.forEach(s -> s.accept(this));
        return null;
    }

    @Override
    public Void visit(ReturnStat returnStat) {
        returnStat.expr.accept(this);
        /* Epilogue */
        // Restore caller's context:
        // -> Copy %rbp to %rsp and then replace %rbp with the stored value
        // -> Can be reduced to "popq" part in certain cases.
        code.leave();
        code.ret();
        return null;
    }

    @Override
    public Void visit(BreakStat breakStat) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visit(TypeNode type) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visit(FunDef funDef) {
        var oldCtx = stack;
        stack = new StackManager(code);

        code.writeIndented(".globl\t", funDef.name);
        code.writeIndented(".type\t", funDef.name, ", @function");
        code.write("\n", funDef.name, ":");

        /* Prologue */
        // Backup caller's and set callee's context
        code.pushq(rbp);
        code.movq(rsp, rbp);

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
            code.leave();
            code.ret();
        }

        stack = oldCtx;
        return null;
    }

    @Override
    public Void visit(Prog prog) {
        exitStatus = Result.Ok;

        prog.funDefs.forEach(f -> f.accept(this));

        var filePath = outFilePath();
        try (var w = new FileWriter(filePath.toFile())) {
            w.write("\t.file\t\"" + filePath.getFileName().toString() + "\"");
            if (!rodataSec.isEmpty()) {
                w.write(rodataSec.toString());
                w.write("\n");
            }
            if (!dataSec.isEmpty()) {
                w.write(dataSec.toString());
                w.write("\n");
            }

            w.write(code.toString());
            w.write("\n\t.section\t.note.GNU-stack,\"\",@progbits\n");
        } catch (IOException e) {
            System.err.println(e);
            exitStatus = Result.Err;
        }
        return null;
    }

    @Override
    public Void visit(EmptyNode emptyNode) {
        return null;
    }

    class StackManager {
        private CodeSection code;

        /** Lookup for basepointer offsets of variables stored on stack in the current context. */
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

        private int unalignedStackSize() {
            return alignedStackSize + pendingAllocSize;
        }

        public StackManager(CodeSection codeSection) {
            this.code = codeSection;
            this.ctx = new HashMap<>();
        }

        /**
         * Increases current frame's stacksize by decrementing %rsp by the
         * 16-byte-aligned {@link #pendingAllocSize}.
         * 
         * Call this method before "call" instructions to make sure %rsp is aligned
         * as required by the Linux System V ABI.
         */
        public void alignRspToStackSize() {
            var size = getAlignedSize();
            if (size > 0) {
                code.subq($(size), rsp);
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
            ctx.put(name, nextOffset(size));
            code.movq(source, this.get(name));
        }

        /**
         * Stores an uninitialized element.
         * @see #store(String, int, OperandSpecifier)
         */
        public void store(String name, int size) {
            ctx.put(name, nextOffset(size));
        }

        /**
         * Reserves stack slot of given size for an unnamed element and returns
         * the corresponding memory address.
         * @param size
         * @return MemAddr referencing the reserved stack slot.
         */
        public MemAddr reserveSlot(int size) {
            return new MemAddr(nextOffset(size), rbp);
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

        private int nextOffset(int size) {
            writeOffset -= size;
            // allocated if necessary
            if (Math.abs(writeOffset) > unalignedStackSize()) {
                alloc(size);
            }
            return writeOffset;
        }
    }
}
