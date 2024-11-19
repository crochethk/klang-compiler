package cc.crochethk.compilerbau.praktikum;

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
import java.util.function.Consumer;
import java.util.function.Function;

import cc.crochethk.compilerbau.praktikum.ast.BinOpExpr;
import cc.crochethk.compilerbau.praktikum.ast.BooleanLit;
import cc.crochethk.compilerbau.praktikum.ast.EmptyNode;
import cc.crochethk.compilerbau.praktikum.ast.FunCall;
import cc.crochethk.compilerbau.praktikum.ast.FunDef;
import cc.crochethk.compilerbau.praktikum.ast.IfElseStat;
import cc.crochethk.compilerbau.praktikum.ast.IntLit;
import cc.crochethk.compilerbau.praktikum.ast.Prog;
import cc.crochethk.compilerbau.praktikum.ast.ReturnStat;
import cc.crochethk.compilerbau.praktikum.ast.StatementListNode;
import cc.crochethk.compilerbau.praktikum.ast.TernaryConditionalExpr;
import cc.crochethk.compilerbau.praktikum.ast.TypeNode;
import cc.crochethk.compilerbau.praktikum.ast.UnaryOpExpr;
import cc.crochethk.compilerbau.praktikum.ast.Var;
import cc.crochethk.compilerbau.praktikum.ast.VarAssignStat;
import cc.crochethk.compilerbau.praktikum.ast.VarDeclareStat;
import cc.crochethk.compilerbau.praktikum.ast.BinOpExpr.BinaryOp;

import static cc.crochethk.compilerbau.praktikum.Type.*;

/**
 * Visitor that generates Java Byte Code by traversing the AST nodes.
 * The generated code is wrapped by a class defined by the specified fully
 * qualified name and then written into a '.class' file, ready to be executed
 * by the JVM.
 */
public class GenJBC implements Visitor<Void> {
    private String outDir;
    private String packageName;
    private String className;

    private ClassBuilder classBuilder = null;
    private CodeBuilder codeBuilder = null;

    private Map<String, FunDef> funDefs = new HashMap<>();

    /** 
     * Temporary offset store for local variables
     *  while the function's code is built
     */
    private Map<String, Integer> vars = new HashMap<>();

    /**
     * @param outputDir The path to the directory generated files will be written to.
     * @param fullClassName
     * The fully qualified class name of pattern {@code [<packageName>.]<className>},
     * for example {@code com.example.MyClass}.
     * The packagename prefix is optional.
     * This also will be the generated file's path relative to '{@code outputDir}'.
     */
    public GenJBC(String outputDir, String fullClassName) {
        this.outDir = outputDir;
        var lastDelimIdx = fullClassName.lastIndexOf('.');
        this.className = fullClassName.substring(lastDelimIdx + 1);
        this.packageName = lastDelimIdx >= 0
                ? fullClassName.substring(0, lastDelimIdx)
                : "";
    }

    @Override
    public Void visit(Prog prog) {
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
            var packageDir = Path.of(outDir).resolve(packageName.replace('.', '/'));
            Files.createDirectories(packageDir);
            var file = new FileOutputStream(packageDir.resolve(className + ".class").toFile());
            file.write(bytes);
            file.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
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

                codeBuilder.invokevirtual(
                        CD_PrintStream, "println",
                        MethodTypeDesc.of(
                                // Return type of println
                                ConstantDescs.CD_void,
                                entryPointCall.theType.classDesc()));

                codeBuilder.return_();
            });
        });
    }

    @Override
    public Void visit(FunDef funDef) {
        // Add to function lookup table for later use
        funDefs.put(funDef.name, funDef);

        // Purge vars stored by previous funDef
        vars.clear();

        // Map parameters to JVM types and compute their initial stack indices
        int pStoreIdx = 0;
        List<ClassDesc> paramsClassDescs = new ArrayList<>(funDef.params.size());
        for (var p : funDef.params) {
            var type = p.type().theType;
            paramsClassDescs.add(type.classDesc());

            vars.put(p.name(), pStoreIdx);
            pStoreIdx += type.jvmSize();
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
                }));
        return null;
    }

    @Override
    public Void visit(StatementListNode statementListNode) {
        statementListNode.value.accept(this);
        statementListNode.next.accept(this);
        return null;
    }

    @Override
    public Void visit(ReturnStat returnStat) {
        returnStat.expr.accept(this);
        codeBuilder.return_(returnStat.theType.jvmTypeKind());
        return null;
    }

    @Override
    public Void visit(IntLit intLit) {
        codeBuilder.ldc(intLit.value);
        return null;
    }

    @Override
    public Void visit(BooleanLit booleanLit) {
        if (booleanLit.value) {
            // push "true" representation
            codeBuilder.iconst_1();
        } else {
            codeBuilder.iconst_0();
        }
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
        op_switch: switch (op) {
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
            * alike javac seems to do it, i.e. dcmpg for "<"/"<=" and dcmpl otherwise.
            */
            case eq -> {
                Label falseBranch = cb.newLabel();
                Label afterFalseBranch = cb.newLabel();

                // Compares operands pushing result (-1|0|1) to stack
                switch (operandType.jvmTypeKind()) {
                    case LongType -> cb.lcmp();
                    case DoubleType -> cb.dcmpl();
                    default -> {
                        error = true;
                        break op_switch;
                    }
                }
                cb.ifne(falseBranch);
                cb.iconst_1(); // true
                cb.goto_(afterFalseBranch);
                cb.labelBinding(falseBranch);
                cb.iconst_0(); // false
                cb.labelBinding(afterFalseBranch);
            }
            //TODO
            // case neq -> {
            //     switch (operandType.jvmTypeKind()) {
            //         case LongType -> cb.l();
            //         case DoubleType -> cb.d();
            //         default -> error = true;
            //     }
            // }

            // case gt -> {
            //     switch (operandType.jvmTypeKind()) {
            //         case LongType -> cb.l();
            //         case DoubleType -> cb.d();
            //         default -> error = true;
            //     }
            // }
            // case gteq -> {
            //     switch (operandType.jvmTypeKind()) {
            //         case LongType -> cb.l();
            //         case DoubleType -> cb.d();
            //         default -> error = true;
            //     }
            // }
            // case lt -> {
            //     switch (operandType.jvmTypeKind()) {
            //         case LongType -> cb.l();
            //         case DoubleType -> cb.d();
            //         default -> error = true;
            //     }
            // }
            // case lteq -> {
            //     switch (operandType.jvmTypeKind()) {
            //         case LongType -> cb.l();
            //         case DoubleType -> cb.d();
            //         default -> error = true;
            //     }
            // }

            // case and -> {}
            // case or -> {}
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

    @Override
    public Void visit(FunCall funCall) {
        // load arguments before calling function
        funCall.args.forEach(arg -> arg.accept(this));

        var funDef = funDefs.get(funCall.name);

        // TODO ----- TEMORARY SOLUTION until Node.theType is correctly set by TypeChecker -------
        List<ClassDesc> argsClassDescs = new ArrayList<>(funCall.args.size());
        for (var p : funDef.params) { // here we should actually work with the given arg-types
            var jvmT = mapToJvmType(p.type());
            argsClassDescs.add(jvmT.classDesc);
        }
        var methDescriptor = MethodTypeDesc.of(
                mapToJvmType(funDef.returnType).classDesc,
                argsClassDescs);
        codeBuilder.invokestatic(ClassDesc.of(packageName, className), funCall.name, methDescriptor);
        return null;
    }

    // TODO ----- TEMORARY SOLUTION -------
    // TODO this is a temporary solution, until TypeChecker correctly sets "Node.theType"
    // TODO in all Node types.
    Map<String, JvmType> varTypes = new HashMap<>();
    // TODO -------------------------------

    @Override
    public Void visit(Var var) { // TODO dont use the temporary "varTypes" map
        var varIdx = vars.get(var.name);
        var classDesc = varTypes.get(var.name).classDesc;
        if (classDesc.equals(ConstantDescs.CD_long)) {
            codeBuilder.lload(varIdx);
        } else if (classDesc.equals(ConstantDescs.CD_boolean)) {
            codeBuilder.iload(varIdx);
        } else {
            throw new AssertionError("var type '" + classDesc + "' did not match any expected ClassDesc");
        }

        return null;
    }

    @Override
    public Void visit(UnaryOpExpr unaryOpExpr) {
        // load operand
        unaryOpExpr.operand.accept(this);

        switch (unaryOpExpr.op) {
            case neg -> codeBuilder.lneg();
            case not -> {
                // TODO no idea whether this actually will work
                var _setFalse = codeBuilder.newLabel();
                var setFalse_ = codeBuilder.newLabel();
                codeBuilder.ifne(_setFalse); // if not is 0 -> jump to "_t"
                codeBuilder.iconst_1(); // set true
                codeBuilder.goto_(setFalse_);
                codeBuilder.labelBinding(_setFalse);
                codeBuilder.iconst_0(); // set false
                codeBuilder.labelBinding(setFalse_);
            }
            default -> {
                throw new UnsupportedOperationException("Unary operation '" + unaryOpExpr.op + "' not supported.");
            }
        }
        return null;
    }

    @Override
    public Void visit(TernaryConditionalExpr ternaryConditionalExpr) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visit(VarDeclareStat varDeclareStat) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visit(VarAssignStat varAssignStat) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visit(EmptyNode emptyNode) {
        // shouldn't need to generate anything, I guess...
        return null;
    }

    @Override
    public Void visit(IfElseStat ifElseStat) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visit(TypeNode type) {
        // TODO Auto-generated method stub
        return null;
    }

    // @Override
    // public Void visit(Type type) {
    //     // TODO Auto-generated method stub
    //     return null;
    // }

    // private JvmType mapToJvmType(Type t) {
    //     var jvmT = switch (t) {
    //         case BoolT _ -> {
    //             yield new JvmType(ConstantDescs.CD_boolean, 1);
    //         }
    //         case I64T _ -> {
    //             yield new JvmType(ConstantDescs.CD_long, 2);
    //         }
    //         case VoidT _ -> {
    //             yield new JvmType(ConstantDescs.CD_void, 0);
    //         }
    //     };

    //     return jvmT;
    // }

    // private class JvmType {
    //     /**
    //      * The JVM ClassDesc corresponding to this type.
    //      */
    //     final ClassDesc classDesc;
    //     /**
    //      * Amount of JVM stack slots (each 32 Bit) occupied by a value
    //      * represented by this type
    //      */
    //     final int slotSize;

    //     JvmType(ClassDesc classDesc, int slotSize) {
    //         this.classDesc = classDesc;
    //         this.slotSize = slotSize;
    //     }
    // }
}
