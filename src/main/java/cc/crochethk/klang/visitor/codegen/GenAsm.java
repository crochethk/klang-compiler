package cc.crochethk.klang.visitor.codegen;

import static cc.crochethk.klang.visitor.BuiltinDefinitions.*;
import static cc.crochethk.klang.visitor.codegen.asm.OperandSpecifier.Const.$;
import static cc.crochethk.klang.visitor.codegen.asm.OperandSpecifier.Register.*;
import static cc.crochethk.klang.visitor.codegen.asm.OperandSpecifier.XmmRegister.*;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import cc.crochethk.klang.visitor.codegen.asm.OperandSpecifier.XmmRegister;
import cc.crochethk.klang.ast.*;
import cc.crochethk.klang.ast.BinOpExpr.BinaryOp;
import cc.crochethk.klang.ast.MemberAccess.*;
import cc.crochethk.klang.ast.literal.*;
import cc.crochethk.klang.visitor.Type;
import cc.crochethk.klang.visitor.codegen.asm.*;
import cc.crochethk.klang.visitor.codegen.asm.helpers.*;

public class GenAsm extends CodeGenVisitor {
    private static final String FILE_EXT = ".s";

    private GenCHeaders cHeaderGen;
    private GenCImpls cImplsGen;

    /** Readonly data section (".rodata") */
    DataSection rodataSec = new DataSection.ReadOnlyData();
    /** Mutable data section (".data") */
    DataSection dataSec = new DataSection.WritableData();
    /** Programm code section (".text") */
    CodeSection code = new CodeSection();

    StackManager stack = null;

    /** Function argument registers */
    private final Register[] regs = { rdi, rsi, rdx, rcx, r8, r9 };
    private final XmmRegister[] xmmRegs = { xmm0, xmm1, xmm2, xmm3, xmm4, xmm5, xmm6, xmm7 };

    public GenAsm(String outputDir, String packageName, String className) throws IOException {
        super(outputDir, packageName, className);
        cHeaderGen = new GenCHeaders(outputDir, packageName, className);
        cImplsGen = new GenCImpls(outputDir, packageName, className);
    }

    @Override
    public List<Path> outFilePaths() {
        var asmOut = Path.of(outDir, packageName + "." + className + FILE_EXT);
        var cOuts = Stream.concat(
                cHeaderGen.outFilePaths().stream(), cImplsGen.outFilePaths().stream());
        return Stream.concat(Stream.of(asmOut), cOuts).toList();
    }

    @Override
    public void visit(I64Lit i64Lit) {
        code.movq($(i64Lit.value), rax);
    }

    @Override
    public void visit(F64Lit f64Lit) {
        var label = rodataSec.createLiteral(f64Lit.value);
        // "movsd	.LC{n}(%rip), %xmm0"
        code.movsd(new MemAddr(label, rip), xmm0);
    }

    @Override
    public void visit(BoolLit boolLit) {
        code.movq(boolLit.value ? $(1) : $(0), rax);
    }

    @Override
    public void visit(StringLit stringLit) {
        var label = rodataSec.createLiteral(stringLit.value);
        // Calculate pointer to string literal at runtime
        code.leaq(new MemAddr(label, rip), rax);
    }

    @Override
    public void visit(NullLit nullLit) {
        code.movq($(0), rax);
    }

    @Override
    public void visit(Var var) {
        if (var.theType.isFloatType()) {
            code.movsd(stack.get(var.name), xmm0);
        } else {
            code.movq(stack.get(var.name), rax);
        }
    }

    @Override
    public void visit(FunCall funCall) {
        // Generate call to builtin function (if funCall is builtin)
        var argTypes = funCall.args.stream().map(arg -> arg.theType).toList();
        var autoFunSign = findBuiltinFun(funCall.name, argTypes);
        if (autoFunSign.isPresent()) {
            var autoFun = autoFunSign.get();
            // Generate builtinFuncall and execute?
            var srcPos = funCall.srcPos;
            var arg0 = new StringLit(srcPos, GenCBase.getTypeFormat(argTypes.get(0)));
            arg0.theType = Type.STRING_T;
            var args = Stream.concat(Stream.of(arg0), funCall.args.stream()).toList();
            var builtinFunCall = new FunCall(srcPos, /*autoFun.name()*/"printf@PLT", args);
            builtinFunCall.theType = autoFun.returnType();
            builtinFunCall.accept(this);
            return;
        }

        // Memory positions of argument results from FunCall args
        var preEvaluatedArgsIt = funCall.args.stream().map(arg -> {
            if (arg instanceof FunCall) {
                arg.accept(this);
                var resultAddr = stack.reserveSlot(arg.theType);
                if (arg.theType.isFloatType())
                    code.movsd(xmm0, resultAddr);
                else
                    code.movq(rax, resultAddr);
                return resultAddr;
            } else {
                return null;
            }
        }).toList().iterator();

        var xmmRegsIt = Arrays.stream(xmmRegs).iterator();
        var regularRegsIt = Arrays.stream(regs).iterator();

        Node theXmm0Arg = null;
        OperandSpecifier theXmm0SrcOpSpec = null;

        stack.alignRspToStackSize();
        var stackArgsOffset = 0;
        for (var argIter = funCall.args.iterator(); argIter.hasNext();) {
            var arg = argIter.next();
            var preEvaledArg = preEvaluatedArgsIt.next();

            if (arg.theType.isFloatType()) {
                if (theXmm0Arg == null) {
                    // - remember arg that should go into xmm0 for later
                    //  (since we might need xmm0 in the meantime)
                    // - also advance xmm reg iterator 
                    theXmm0Arg = arg;
                    theXmm0SrcOpSpec = preEvaledArg;
                    var _ = xmmRegsIt.next();
                    continue;
                }

                if (preEvaledArg != null) {
                    code.movsd(preEvaledArg, xmm0);
                } else {
                    arg.accept(this);
                }
                // result in xmm0

                if (xmmRegsIt.hasNext()) {
                    var xmm_i = xmmRegsIt.next();
                    // put arg result into next free xmm register
                    code.movsd(xmm0, xmm_i);
                } else {
                    // allocate 8 byte and pass arg on stack
                    code.subq($(8), rsp);
                    code.movsd(xmm0, new MemAddr(rsp));
                    stackArgsOffset += 8;
                }
            } else { // use general purpose registers
                if (preEvaledArg != null) {
                    code.movq(preEvaledArg, rax);
                } else {
                    arg.accept(this);
                }

                if (regularRegsIt.hasNext()) {
                    var reg_i = regularRegsIt.next();
                    code.movq(rax, reg_i);
                } else {
                    code.pushq(rax);
                    stackArgsOffset += 8;
                }
            }
        }

        // Finally eval first float arg (if exists)
        if (theXmm0Arg != null) {
            if (theXmm0SrcOpSpec != null) {
                code.movsd(theXmm0SrcOpSpec, xmm0);
            } else {
                theXmm0Arg.accept(this);
            }
        }
        code.call(funCall.name);

        // Release stack args if necessary
        if (stackArgsOffset > 0) {
            code.addq($(stackArgsOffset), rsp);
        }
    }

    /**
     * Context helper variable for MemberAccessChain and MemberAccess nodes evaluation
     */
    private Type maChainOwnerType = null;

    /**
     * Determines the ownerType of the given {@code MemberAccess} node. If its 
     * owner is null the current {@code MemberAccessChain} ownerType is 
     * returned.
     */
    private Type getMemberOwnerType(MemberAccess ma) {
        return ma.owner != null ? ma.owner.theType : maChainOwnerType;
    }

    @Override
    public void visit(MemberAccessChain maChain) {
        maChain.owner.accept(this); // -> init reference in rax

        var oldOwner = maChainOwnerType;
        maChainOwnerType = maChain.owner.theType;
        maChain.chain.accept(this);
        maChainOwnerType = oldOwner;
    }

    @Override
    public void visit(FieldGet fieldGet) {
        // Call autoimplemented getter matching the targets owner type and 'targetName' field
        var ownerType = getMemberOwnerType(fieldGet);
        var getterFunName = getGetterFullName(ownerType, fieldGet.targetName);

        // Assumption: "owner" reference already in rax (from previous MemberAccess nodes)
        // Passing EmptyNode as arg, makes FunCall load the mentioned pointer from rax.
        var thisArg = new EmptyNode(fieldGet.srcPos);
        var funCall = new FunCall(fieldGet.srcPos, getterFunName, List.of(thisArg));
        funCall.theType = fieldGet.theType;
        funCall.accept(this);
        if (fieldGet.next != null) {
            fieldGet.next.accept(this);
        }
    }

    @Override
    public void visit(MethodCall methodCall) {
        var ownerType = getMemberOwnerType(methodCall);
        var asmMethName = getAsmMethodName(ownerType, methodCall.targetName);

        // Assumption: "owner" reference already in rax (from previous MemberAccess nodes)
        // Passing EmptyNode as first arg, makes FunCall load the mentioned pointer from rax.
        var thisArg = new EmptyNode(methodCall.srcPos);
        var allArgs = Stream.concat(Stream.of(thisArg), methodCall.args.stream()).toList();
        var funCall = new FunCall(methodCall.srcPos, asmMethName, allArgs);
        funCall.theType = methodCall.theType;
        funCall.accept(this);
        if (methodCall.next != null) {
            methodCall.next.accept(this);
        }
    }

    @Override
    public void visit(FieldSet fieldSet) {
        /**
         * Simply ignore. Work is done in the statement depending on this node
         * (e.g. FieldAssignStat), since it has all information required.
         */
    }

    @Override
    public void visit(ConstructorCall constructorCall) {
        var fullConstName = getConstructorFullName(constructorCall.theType);
        var funCall = new FunCall(constructorCall.srcPos, fullConstName, constructorCall.args);
        funCall.theType = constructorCall.theType;
        funCall.accept(this);
    }

    @Override
    public void visit(BinOpExpr binOpExpr) {
        binOpExpr.rhs.accept(this);

        var lhsNotComplex = binOpExpr.lhs instanceof LiteralExpr || binOpExpr.lhs instanceof Var;

        OperandSpecifier rhsResLoc = null;
        OperandSpecifier dstOpSpec = null;

        // Put rhs result on stack, if the register might be overwritten when evaluating lhs
        if (binOpExpr.theType.isFloatType()) {
            dstOpSpec = xmm0;
            rhsResLoc = lhsNotComplex ? xmm1 : stack.reserveSlot(binOpExpr.rhs.theType);
            code.movsd(dstOpSpec, rhsResLoc);
        } else {
            dstOpSpec = rax;
            rhsResLoc = lhsNotComplex ? rdx : stack.reserveSlot(binOpExpr.rhs.theType);
            code.movq(dstOpSpec, rhsResLoc);
        }

        binOpExpr.lhs.accept(this);
        // now operands in %rax (lhs), "rhsResLoc" (rhs)
        // or with floats:  %xmm0 (lhs), "rhsResLoc" (rhs)

        genOpInstruction(code, binOpExpr.lhs.theType, binOpExpr.op, rhsResLoc, dstOpSpec);
    }

    /**
     * Generates instruction(s) for the given operand type, operator and
     * source/destination operand specifiers.
     * The operation can be described as: {@code dst:= dst op src}
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
                } else if (operandType == Type.DOUBLE_T) {
                    code.addsd(src, dst);
                } else {
                    error = true;
                }
            }
            case sub -> {
                if (operandType == Type.LONG_T) {
                    code.subq(src, dst);
                } else if (operandType == Type.DOUBLE_T) {
                    code.subsd(src, dst);
                } else {
                    error = true;
                }
            }
            case mult -> {
                if (operandType == Type.LONG_T) {
                    code.imulq(src, dst);
                } else if (operandType == Type.DOUBLE_T) {
                    code.mulsd(src, dst);
                } else {
                    error = true;
                }
            }
            case div -> {
                if (operandType == Type.LONG_T) {
                    // TODO
                } else if (operandType == Type.DOUBLE_T) {
                    code.divsd(src, dst);
                } else {
                    error = true;
                }
            }
            // case mod -> {}
            // case pow -> {}

            // Comparison
            case eq, neq, gt, gteq, lt, lteq -> {
                if (operandType == Type.LONG_T) {
                    // TODO
                } else if (operandType == Type.DOUBLE_T) {
                    // TODO
                } else {
                    // TODO implement case "operandType == Type.DOUBLE_T"
                    error = true;
                }
            }

            // Boolean
            //case and -> error = true;//TODO
            //case or -> error = true; //TODO
            default -> throw new UnsupportedOperationException("Operation '" + op
                    + "' not yet implemented for '" + operandType + ", " + operandType + "'");
        }

        if (error) {
            throw new UnsupportedOperationException("Operation '" + op
                    + "' not supported for '" + operandType + ", " + operandType + "'");
        }
    }

    @Override
    public void visit(UnaryOpExpr unaryOpExpr) {
        // TODO Auto-generated method stub
        return;
    }

    @Override
    public void visit(TernaryConditionalExpr ternaryConditionalExpr) {
        // TODO Auto-generated method stub
        return;
    }

    @Override
    public void visit(VarDeclareStat varDeclareStat) {
        stack.reserveSlot(varDeclareStat.varName(), varDeclareStat.theType);
        varDeclareStat.initializer.ifPresent(init -> init.accept(this));
    }

    @Override
    public void visit(VarAssignStat varAssignStat) {
        varAssignStat.expr.accept(this);
        if (varAssignStat.theType.isFloatType()) {
            code.movsd(xmm0, stack.get(varAssignStat.targetVarName));
        } else {
            code.movq(rax, stack.get(varAssignStat.targetVarName));
        }
    }

    @Override
    public void visit(FieldAssignStat faStat) {
        // traverse the member access chain --> at the end owner pointer is in rax
        faStat.maChain.accept(this);

        var field = faStat.maChain.chain.getLast();
        var fieldOwnerType = field.owner != null
                ? field.owner.theType
                : faStat.maChain.owner.theType;
        var setterFunName = getSetterFullName(fieldOwnerType, field.targetName);
        var thisArg = new EmptyNode(faStat.srcPos);
        var funCall = new FunCall(faStat.srcPos, setterFunName, List.of(thisArg, faStat.expr));
        funCall.theType = faStat.theType;
        funCall.accept(this);
    }

    @Override
    public void visit(IfElseStat ifElseStat) {
        // TODO Auto-generated method stub
        return;
    }

    @Override
    public void visit(LoopStat loopStat) {
        // TODO Auto-generated method stub
        return;
    }

    @Override
    public void visit(StatementList statementList) {
        statementList.statements.forEach(s -> s.accept(this));
    }

    @Override
    public void visit(ReturnStat returnStat) {
        returnStat.expr.accept(this);
        /* Epilogue */
        // Restore caller's context:
        // -> Copy %rbp to %rsp and then replace %rbp with the stored value
        // -> Can be reduced to "popq" part in certain cases.
        code.leave();
        code.ret();
    }

    @Override
    public void visit(BreakStat breakStat) {
        // TODO Auto-generated method stub
        return;
    }

    @Override
    public void visit(DropStat dropStat) {
        var thisArg = dropStat.refTypeVar;
        var funName = getDestructorFullName(thisArg.theType);
        var destructor = new FunCall(dropStat.srcPos, funName, List.of(thisArg));
        destructor.theType = dropStat.theType;
        destructor.accept(this);
    }

    @Override
    public void visit(TypeNode type) {
    }

    @Override
    public void visit(FunDef funDef) {
        genFunDefWithName(funDef.name, funDef);
    }

    /**
     * Generates the function defintion for the given {@code FunDef} using a
     * custom name instead of the one in {@code FunDef}.
     * @param newFunName Name to use instead of {@code funDef.name}
     * @param funDef
     */
    void genFunDefWithName(String newFunName, FunDef funDef) {
        var oldCtx = stack;
        stack = new StackManager(code);

        code.writeIndented(".globl\t", newFunName);
        code.writeIndented(".type\t", newFunName, ", @function");
        code.write("\n", newFunName, ":");

        /* Prologue */
        // Backup caller's and set callee's context
        code.pushq(rbp);
        code.movq(rsp, rbp);

        // Backup all register args to stack
        // and add args passed on stack to the stack manager.

        /** Stack Layout Overview
         * 
         *  +--------------------+
         *  | ...                |
         *  +--------------------+
         *  | first stack arg    | <- [16(%rbp)]
         *  +--------------------+   
         *  | second stack arg   | <- [24(%rbp)]
         *  +--------------------+
         *  | first stack arg    | <- [16(%rbp)]
         *  +--------------------+
         *  | saved frame pointer| <- [8(%rbp)]
         *  +--------------------+
         *  | __return address__ | <- [0(%rbp)]
         *  +--------------------+
         *  | %rdi / %xmm0 arg   | <- [-8(%rbp)]
         *  +--------------------+
         *  | ...                |
         *  +--------------------+
         */

        var xmmRegsIt = Arrays.stream(xmmRegs).iterator();
        var regularRegsIt = Arrays.stream(regs).iterator();

        var stackArgOffset = 16; //skip rbp backup and return addr
        for (var paramIt = funDef.params.iterator(); paramIt.hasNext();) {
            var p = paramIt.next();
            var ptype = p.type().theType;

            if (ptype.isFloatType() && xmmRegsIt.hasNext()) {
                var src = xmmRegsIt.next();
                stack.storeXmmSd(p.name(), src);
            } else if (regularRegsIt.hasNext()) {
                var src = regularRegsIt.next();
                stack.store(p.name(), ptype, src);
            } else {
                // Caller saved stack arg
                // - add its offset to manager, assuming 8 Bytes per arg
                stack.associate(p.name(), stackArgOffset);
                stackArgOffset += 8;
            }

        }

        // Reserve stack for local variables
        for (var stat : funDef.body.statements) {
            if (stat instanceof VarDeclareStat decl) {
                stack.alloc(decl.theType.byteSize());
            }
        }

        // // // Store register args on stack
        // // for (int i = 0; i < regArgsCount; i++) {
        // //     var arg = funDef.params.get(i);
        // //     // Convention: 8 byte per fun arg
        // //     stack.store(arg.name(), Type.ANY_T, regs[i]);
        // // }

        // // // Get offsets for caller-saved args (when >6 params)
        // // var baseOffset = 16; //skip rbp backup and return addr
        // // for (int i = regArgsCount; i < funDef.params.size(); i++) {
        // //     stack.associate(funDef.params.get(i).name(), baseOffset);
        // //     baseOffset += 8;
        // // }

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
    }

    @Override
    public void visit(StructDef structDef) {
        structDef.methods.forEach(meth -> meth.accept(this));
        // see also GenCHelpers
    }

    @Override
    public void visit(MethDef methDef) {
        genFunDefWithName(getAsmMethodName(methDef.owner().theType, methDef.name()), methDef.def);
    }

    @Override
    public void visit(Prog prog) {
        prog.structDefs.forEach(stDef -> stDef.accept(this));
        prog.funDefs.forEach(f -> {
            if (prog.entryPoint.isPresent() && f.name.equals(prog.entryPoint.get().name))
                genFunDefWithName("main", f);
            else
                f.accept(this);
        });

        var filePath = outFilePaths().get(0);
        try (var w = new FileWriter(filePath.toFile())) {
            w.write("\t.file\t\"" + filePath.getFileName().toString() + "\"");

            SectionBuilder[] sections = { rodataSec, dataSec, code };
            for (var s : sections) {
                if (!s.isEmpty()) {
                    w.write(s.toString());
                    w.write("\n");
                }
            }

            w.write("\n\t.section\t.note.GNU-stack,\"\",@progbits\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Create C helpers
        prog.accept(cHeaderGen);
        prog.accept(cImplsGen);
    }

    @Override
    public void visit(EmptyNode emptyNode) {
        // nop
    }

    /** The {@code ownerType} method's respective assembly function name. */
    public static String getAsmMethodName(Type ownerType, String methName) {
        return ownerType.klangName() + "$" + methName;
    }

    /** The {@code refType} constructor's assembly function name. */
    public static String getConstructorFullName(Type refType) {
        return refType.klangName() + "$new$";
    }

    /** The {@code refType} destructor's assembly function name. */
    public static String getDestructorFullName(Type refType) {
        return refType.klangName() + "$drop$";
    }

    /** The {@code refType} to_string's assembly function name. */
    public static String getToStringFullName(Type refType) {
        return refType.klangName() + "$to_string";
    }

    /** The {@code refType} field getter's assembly function name. */
    public static String getGetterFullName(Type refType, String fieldName) {
        return refType.klangName() + "$get_" + fieldName + "$";
    }

    /** The {@code refType} field setter's assembly function name. */
    public static String getSetterFullName(Type refType, String fieldName) {
        return refType.klangName() + "$set_" + fieldName + "$";
    }

    /**
     * Class responsible for writing variables to stack while keeping track 
     * of their locations.
     * 
     * @implNote Currently all variables will effectively use 8 Bytes since
     * only 64 Bit / 8 Byte variants of instructions are used even if the given
     * type would be shorter.
     * This is reasonable at this point, since
     * - only 64 Bit general purpose registers are implemented
     * - xmm related instructions also only use single scalar values up to 64 Bit.
     */
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
         * Call this method before "call" instructions to make sure %rsp is 
         * aligned as required by the Linux System V ABI.
         */
        public void alignRspToStackSize() {
            var size = getAlignedSize();
            if (size > 0) {
                code.subq($(size), rsp);
                alignedStackSize += size;
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
         * This operation does not actually allocate resources. Rather it's
         * useful for computing the required stack size in the prologue of a
         * function definition (i.e. before actually changing %rsp or storing
         * elements on stack).
         * %rsp must be aligned using {@link #alignRspToStackSize()} before any 
         * "call" instructions.
         */
        public void alloc(int bytes) {
            pendingAllocSize += bytes;
        }

        /**
         * Writes the value of given {@code type} from {@code source} to stack
         * and stores it using {@code name} for later lookup.
         * @param name The value's name for later referencing.
         * @param type The Type of the value to be stored.
         * @param source The value's source (register, memoryaddress, constant).
         * In case of {@link OperandSpecifier.MemoryOperandSpecifier} this will require
         * an intermediate instruction loading the value to %rax.
         * 
         * @apiNote For operations on floats using xmm registers you should
         * rather use {@link #storeXmmSd(String, XmmRegister)}.
         */
        public void store(String name, Type type, OperandSpecifier source) {
            ctx.put(name, nextOffset(type.byteSize()));
            if (source instanceof MemoryOperandSpecifier) {
                code.movq(source, rax);
                code.movq(rax, this.get(name));
            } else {
                code.movq(source, this.get(name));
            }
        }

        /**
         * Write a single scalar double precision float (8 byte) from the given xmm
         * register to stack, associating its address with the given {@code name}.
         * @param name The value's name for later referencing.
         * @param source The value's source xmm register.
         * @see #store(String, Type, OperandSpecifier)
         */
        public void storeXmmSd(String name, XmmRegister source) {
            ctx.put(name, nextOffset(8)); //movsd always writes 8 bytes
            code.movsd(source, this.get(name));
        }

        /**
         * Reserves memory on stack suitable to store an element of given
         * {@code type}. This only changes the StackManagers internal state
         * and does not write any actual instructions.
         * The reserved memory location can be retrieved 
         * using {@link #get(String)} and the specified {@code name}.
         * 
         * @param name A Name for the stored element for later reference.
         * @param type The Type of the element to be stored.
         */
        public void reserveSlot(String name, Type type) {
            ctx.put(name, nextOffset(type.byteSize()));
        }

        /**
         * Reserves stack slot for an unnamed element of given {@code type} and
         * returns the corresponding memory address.
         * @param type
         * @return MemAddr The %rbp based address referencing the reserved slot.
         */
        public MemAddr reserveSlot(Type type) {
            return new MemAddr(nextOffset(type.byteSize()), rbp);
        }

        /**
         * Adds stack element reference to this manager. The element's location
         * can later be retrieved using the "get" method.
         * @param name The name for later lookup.
         * @param baseOffset The offset relative to &rbp.
         * @see #get(String)
        */
        public void associate(String name, int baseOffset) {
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
