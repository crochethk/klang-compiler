package cc.crochethk.compilerbau.praktikum.visitor.codegen;

import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.classfile.AccessFlags;
import java.lang.classfile.ClassBuilder;
import java.lang.classfile.ClassFile;
import java.lang.classfile.CodeBuilder;
import java.lang.classfile.Label;
import java.lang.classfile.TypeKind;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.MethodTypeDesc;
import java.lang.reflect.AccessFlag;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cc.crochethk.compilerbau.praktikum.ast.*;
import cc.crochethk.compilerbau.praktikum.ast.UnaryOpExpr.UnaryOp;
import cc.crochethk.compilerbau.praktikum.ast.literal.*;
import cc.crochethk.compilerbau.praktikum.visitor.Type;
import cc.crochethk.compilerbau.praktikum.ast.BinOpExpr.BinaryOp;
import utils.PathUtils;
import utils.Result;

/**
 * Visitor that generates Java Byte Code by traversing the AST nodes.
 * The generated code is wrapped by a class defined by the specified fully
 * qualified name and then written into a '.class' file, ready to be executed
 * by the JVM.
 */
public class GenJBC extends CodeGenVisitor<Void> {
    private static final String FILE_EXT = ".class";

    @Override
    public Path outFilePath() {
        return Path.of(outDir, packageName.replace('.', '/'), className + FILE_EXT);
    }

    private ClassBuilder classBuilder = null;
    private CodeBuilder codeBuilder = null;

    private Map<String, FunDef> funDefs = new HashMap<>();

    /**
     * Manager for function-local variable offset lookup.
     * (for example for use in "Xload(slot)" instructions)
     */
    private VariableSlotManager varsManager = new VariableSlotManager();

    public GenJBC(String outputDir, String packageName, String className) {
        super(outputDir, packageName, className);
    }

    @Override
    public Void visit(I64Lit i64Lit) {
        codeBuilder.ldc(i64Lit.value);
        return null;
    }

    @Override
    public Void visit(F64Lit f64Lit) {
        codeBuilder.ldc(f64Lit.value);
        return null;
    }

    @Override
    public Void visit(BoolLit boolLit) {
        if (boolLit.value) {
            // push "true" representation
            codeBuilder.iconst_1();
        } else {
            codeBuilder.iconst_0();
        }
        return null;
    }

    @Override
    public Void visit(StringLit stringLit) {
        codeBuilder.ldc(stringLit.value);
        return null;
    }

    @Override
    public Void visit(Var var) {
        var slot = varsManager.getSlot(var.name);

        switch (var.theType.jvmTypeKind()) {
            case LongType -> codeBuilder.lload(slot);
            case DoubleType -> codeBuilder.dload(slot);
            case BooleanType -> codeBuilder.iload(slot);
            case ReferenceType -> codeBuilder.aload(slot);
            case VoidType -> throw new UnsupportedOperationException(
                    "Loading the type '" + var.theType.jvmName() + "' is not supported");
            default -> throw new AssertionError(
                    "Could not find a matching load instruction for var'" + var.name + "'");

        }
        return null;
    }

    @Override
    public Void visit(FunCall funCall) {
        // load arguments before calling function
        funCall.args.forEach(arg -> arg.accept(this));

        var argsClassDescs = funCall.args.stream().map(arg -> arg.theType.classDesc()).toList();
        var methDescriptor = MethodTypeDesc.of(funCall.theType.classDesc(), argsClassDescs);

        codeBuilder.invokestatic(ClassDesc.of(packageName, className), funCall.name, methDescriptor);
        return null;
    }

    @Override
    public Void visit(BinOpExpr binOpExpr) {
        // load both operands
        binOpExpr.lhs.accept(this);
        binOpExpr.rhs.accept(this);

        // now operands should be loaded onto operand stack

        genOpInstruction(codeBuilder, binOpExpr.lhs.theType, binOpExpr.op);
        return null;
    }

    private void genOpInstruction(CodeBuilder cb, Type operandType, BinaryOp op) {
        boolean error = false;
        switch (op) {
            /** Arithmetics */
            case add -> {
                switch (operandType.jvmTypeKind()) {
                    case LongType -> cb.ladd();
                    case DoubleType -> cb.dadd();
                    default -> error = true;
                }
            }
            case sub -> {
                switch (operandType.jvmTypeKind()) {
                    case LongType -> cb.lsub();
                    case DoubleType -> cb.dsub();
                    default -> error = true;
                }
            }
            case mult -> {
                switch (operandType.jvmTypeKind()) {
                    case LongType -> cb.lmul();
                    case DoubleType -> cb.dmul();
                    default -> error = true;
                }
            }
            case div -> {
                switch (operandType.jvmTypeKind()) {
                    case LongType -> cb.ldiv();
                    case DoubleType -> cb.ddiv();
                    default -> error = true;
                }
            }
            // TODO
            // case mod -> {}
            // case pow -> {}

            /** Comparisons
            * For Double comparisons "dcmpl" and "dcmpg" instructions are used
            * alike javac seems to do it: dcmpg for "<"/"<=" and dcmpl otherwise.
            */
            case eq, neq, gt, gteq, lt, lteq -> {
                error = genCmpInstruction(cb, operandType.jvmTypeKind(), op).isErr();
                genCmpInstructionEvaluation(cb, operandType.jvmTypeKind(), op);
            }
            /**
             * Boolean
             */
            case and -> cb.iand();
            case or -> cb.ior();
            default -> {
                throw new UnsupportedOperationException("Operation '" + op
                        + "' not yet implemented for '" + operandType + ", " + operandType + "'");
            }
        }

        if (error) {
            throw new UnsupportedOperationException("Operation '"
                    + op + "' not supported for '" + operandType + ", " + operandType + "'");
        }
    }

    /**
     * Generates the instruction for a comparison of two operands of the given TypeKind.
     * Afterwards the comparison result (-1|0|1) is ontop of the stack and ready for
     * further evaluation, e.g. by an "ifXX" instruction.
     */
    private Result<Void> genCmpInstruction(CodeBuilder cb, TypeKind jvmTypeKind, BinaryOp cmpOp) {
        if (!cmpOp.isComparison()) {
            throw new UnsupportedOperationException(
                    "Invalid operator provided: Only comparison operators are allowed");
        }
        switch (jvmTypeKind) {
            case LongType -> cb.lcmp();
            case DoubleType -> {
                switch (cmpOp) {
                    case lt, lteq -> cb.dcmpg();
                    default -> cb.dcmpl();
                }
            }
            default -> {
                return Result.Err;
            }
        }
        return Result.Ok;
    }

    /**
     * Generates instructions for one of the comparisons eq, neq, gt or gteq.
     * The method expects the result of a "cmp" instruction beeing present ontop
     * of the operand stack.
     */
    private void genCmpInstructionEvaluation(CodeBuilder cb, TypeKind jvmTypeKind, BinaryOp cmpOp) {
        Label falseBranch = cb.newLabel();
        switch (cmpOp) {
            case eq -> cb.ifne(falseBranch);
            case neq -> cb.ifeq(falseBranch);
            case gt -> cb.ifle(falseBranch);
            case gteq -> cb.iflt(falseBranch);
            case lt -> cb.ifge(falseBranch);
            case lteq -> cb.ifgt(falseBranch);
            default -> throw new UnsupportedOperationException(
                    "Invalid operator provided: Only comparison operators are allowed");
        }
        genComparisonBranches(cb, falseBranch);
    }

    /**
     * Generates instructions that typically follow an "ifXX" instruction.
     * I.e. a jump is performed to the specified label ("falseBranch") if the
     * comparison evaluated true. This effectively skips the branch right after
     * the "if" which would otherwise push "true" onto the stack.
     * 
     * @param falseBranch label of the branch that will push "false" onto the stack.
     */
    private void genComparisonBranches(CodeBuilder cb, Label falseBranch) {
        var afterFalseBranch = cb.newLabel();
        cb.iconst_1(); // true
        cb.goto_(afterFalseBranch);
        cb.labelBinding(falseBranch);
        cb.iconst_0(); // false
        cb.labelBinding(afterFalseBranch);
    }

    @Override
    public Void visit(UnaryOpExpr unaryOpExpr) {
        // load operand
        unaryOpExpr.operand.accept(this);
        genOpInstruction(codeBuilder, unaryOpExpr.operand.theType, unaryOpExpr.op);
        return null;
    }

    private void genOpInstruction(CodeBuilder cb, Type operandType, UnaryOp op) {
        boolean error = false;
        switch (op) {
            /** Arithmetics */
            case neg -> {
                switch (operandType.jvmTypeKind()) {
                    case LongType -> cb.lneg();
                    case DoubleType -> cb.dneg();
                    default -> error = true;
                }
            }

            /** Boolean */
            case not -> {
                genCmpInstructionEvaluation(cb, operandType.jvmTypeKind(), BinaryOp.eq);
            }
            default -> {
                throw new UnsupportedOperationException("Operation '" + op
                        + "' not implemented for '" + operandType + "'");
            }
        }

        if (error) {
            throw new UnsupportedOperationException("Operation '"
                    + op + "' not supported for '" + operandType + "'");
        }
    }

    @Override
    public Void visit(TernaryConditionalExpr ternaryConditionalExpr) {
        ternaryConditionalExpr.condition.accept(this);
        var falseBranch = codeBuilder.newLabel();
        var afterFalseBranch = codeBuilder.newLabel();

        codeBuilder.ifeq(falseBranch);
        ternaryConditionalExpr.then.accept(this);
        codeBuilder.goto_(afterFalseBranch);
        codeBuilder.labelBinding(falseBranch);
        ternaryConditionalExpr.otherwise.accept(this);
        codeBuilder.labelBinding(afterFalseBranch);
        return null;
    }

    @Override
    public Void visit(VarDeclareStat varDeclareStat) {
        // we are fine with redefintion
        varsManager.reserveSlot(varDeclareStat.varName, varDeclareStat.declaredType.theType);
        return null;
    }

    @Override
    public Void visit(VarAssignStat varAssignStat) {
        varAssignStat.expr.accept(this);
        var slot = varsManager.getSlot(varAssignStat.targetVarName);
        codeBuilder.storeLocal(varAssignStat.theType.jvmTypeKind(), slot);
        return null;
    }

    @Override
    public Void visit(IfElseStat ifElseStat) {
        ifElseStat.condition.accept(this);
        codeBuilder.ifThenElse(
                thenbcb -> {
                    var cbBak = codeBuilder;
                    codeBuilder = thenbcb;
                    ifElseStat.then.accept(this);
                    codeBuilder = cbBak;
                }, elsebcb -> {
                    var cbBak = codeBuilder;
                    codeBuilder = elsebcb;
                    ifElseStat.otherwise.accept(this);
                    codeBuilder = cbBak;
                });
        return null;
    }

    private Label currentLoopEnd = null;

    @Override
    public Void visit(LoopStat loopStat) {
        // backup current label (in case other scope is using it)
        var previousEnd = currentLoopEnd;
        currentLoopEnd = codeBuilder.newLabel();

        var start = codeBuilder.newBoundLabel();
        loopStat.body.accept(this);
        codeBuilder.goto_(start);
        codeBuilder.labelBinding(currentLoopEnd);

        //restore previous label
        currentLoopEnd = previousEnd;
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
        codeBuilder.return_(returnStat.theType.jvmTypeKind());
        return null;
    }

    @Override
    public Void visit(BreakStat breakStat) {
        codeBuilder.goto_(currentLoopEnd);
        return null;
    }

    @Override
    public Void visit(TypeNode type) {
        // -> skip
        return null;
    }

    @Override
    public Void visit(FunDef funDef) {
        // Add to function lookup table for later use
        funDefs.put(funDef.name, funDef);

        // Purge vars stored by previous funDef
        varsManager.reset();

        // Map parameters to JVM types and compute their initial stack indices
        List<ClassDesc> paramsClassDescs = new ArrayList<>(funDef.params.size());
        for (var p : funDef.params) {
            var type = p.type().theType;
            paramsClassDescs.add(type.classDesc());
            varsManager.reserveSlot(p.name(), type);
        }

        // Generate actual method defintion
        var methDescriptor = MethodTypeDesc.of(
                funDef.returnType.theType.classDesc(),
                paramsClassDescs);

        var methFlags = AccessFlags.ofMethod(AccessFlag.STATIC, AccessFlag.PUBLIC).flagsMask();
        classBuilder.withMethod(
                funDef.name,
                methDescriptor,
                methFlags,
                mb -> mb.withCode(cdb -> {
                    this.codeBuilder = cdb;
                    funDef.body.accept(this);

                    if (funDef.body.isEmpty()
                            || (!funDef.body.statements.getLast().returnsControlFlow()
                                    && funDef.returnType.theType.equals(Type.VOID_T))) {
                        cdb.return_();
                    }
                }));
        return null;
    }

    @Override
    public Void visit(Prog prog) {
        exitStatus = Result.Ok;
        var classDesc = ClassDesc.of(packageName, className);
        var bytes = ClassFile.of().build(classDesc, cb -> {
            this.classBuilder = cb;
            prog.funDefs.forEach(def -> def.accept(this));

            // Generate executable if an entrypoint is specified
            if (prog.entryPoint != null) {
                genMainMethod(cb, prog.entryPoint);
            }
        });

        // Write generated bytes to file(s)
        try {
            var filePath = outFilePath();
            Files.createDirectories(PathUtils.getParentOrEmpty(filePath));

            var file = new FileOutputStream(filePath.toFile());
            file.write(bytes);
            file.close();
        } catch (IOException e) {
            System.err.println(e);
            exitStatus = Result.Err;
        }

        return null;
    }

    private void genMainMethod(ClassBuilder cb, FunCall entryPointCall) {
        var methDescriptor = MethodTypeDesc.of(ConstantDescs.CD_void, ConstantDescs.CD_String.arrayType());
        var methFlags = AccessFlags.ofMethod(AccessFlag.STATIC, AccessFlag.PUBLIC).flagsMask();
        cb.withMethod("main", methDescriptor, methFlags, mb -> {
            mb.withCode(cdb -> {
                this.codeBuilder = cdb;

                /** Generate code that:
                 *  - calls the entryPoint function
                 *  - passes the return value to "System.out.println" for outputs to stdout
                 */
                var CD_PrintStream = ClassDesc.of(java.io.PrintStream.class.getName());

                // Push "System.out" reference onto operand stack
                codeBuilder.getstatic(
                        ClassDesc.of(java.lang.System.class.getName()), "out",
                        CD_PrintStream);

                // Execute entrypoint function
                // Push the result onto operand stack
                entryPointCall.accept(this);

                // Only print if returntype is not void
                if (!entryPointCall.theType.equals(Type.VOID_T)) {
                    codeBuilder.invokevirtual(
                            CD_PrintStream, "println",
                            MethodTypeDesc.of(
                                    // Return type of println
                                    ConstantDescs.CD_void,
                                    entryPointCall.theType.classDesc()));
                }

                codeBuilder.return_();
            });
        });
    }

    @Override
    public Void visit(EmptyNode emptyNode) {
        // -> skip
        return null;
    }

    private class VariableSlotManager {
        private Map<String, Integer> varSlots = new HashMap<>();
        private int nextSlot = 0;

        /** Resets this manager to a state similar to a new instance */
        void reset() {
            varSlots.clear();
            nextSlot = 0;
        }

        /**
         * Reserves slots for variable of varName suitable to store the given Type
         * and returns starting slot index.
         */
        Integer reserveSlot(String varName, Type t) {
            int slot = nextSlot;
            varSlots.put(varName, slot);
            nextSlot += t.jvmSize();
            return slot;
        }

        /** Returns the slot index associated with the given varName. */
        Integer getSlot(String varName) {
            return varSlots.get(varName);
        }
    }
}
