package cc.crochethk.compilerbau.praktikum.visitor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import cc.crochethk.compilerbau.praktikum.ast.*;
import cc.crochethk.compilerbau.praktikum.ast.literal.*;

/** TypeChecker Visitor
 * The main task of this Visitor is to semantically check typing of the visited AST.
 * While doing that each visited Node is annotated with its inferred type information
 * by assigning an appropriate Type object to {@code Node.theType}.
 */
public class TypeChecker implements Visitor {
    int errorsReported = 0;

    private void reportError(Node node, String s) {
        errorsReported++;
        System.err.println("(L" + node.line() + ":" + node.column() + ") Type error: " + s);
    }

    @Override
    public void visit(I64Lit i64Lit) {
        i64Lit.theType = Type.LONG_T;
    }

    @Override
    public void visit(F64Lit f64Lit) {
        f64Lit.theType = Type.DOUBLE_T;
    }

    @Override
    public void visit(BoolLit boolLit) {
        boolLit.theType = Type.BOOL_T;
    }

    @Override
    public void visit(StringLit stringLit) {
        stringLit.theType = Type.STRING_T;
    }

    @Override
    public void visit(NullLit nullLit) {
        nullLit.theType = Type.NULL_T;
    }

    @Override
    public void visit(Var var) {
        var varType = funDefVarTypes.get(var.name);
        if (varType == null) {
            reportError(var, "Use of undefined variable '" + var.name + "'");
        }
        var.theType = varType;
    }

    @Override
    public void visit(FunCall funCall) {
        funCall.args.forEach(arg -> arg.accept(this));

        var funDef = funDefs.get(funCall.name);
        if (funDef == null) {
            reportError(funCall, "Unknown function '" + funCall.name + "'");
            funCall.theType = Type.UNKNOWN_T;
        } else {
            //funDef found
            funCall.theType = funDef.returnType.theType;

            var paramCount = funDef.params.size();
            var argsCount = funCall.args.size();
            if (paramCount != argsCount) {
                reportError(funCall,
                        "Wrong number of arguments: expected " + paramCount
                                + " but " + argsCount + " provided");
            }

            var argsIter = funCall.args.iterator();
            for (var p : funDef.params) {
                if (!argsIter.hasNext()) {
                    break;
                }
                var arg = argsIter.next();

                if (!p.type().theType.isCompatible(arg.theType)) {
                    reportError(funCall, "Invalid argument type for parameter '" + p.name()
                            + "': expected '" + prettyTheTypeName(p.type()) + "' but found incompatible '"
                            + prettyTheTypeName(arg) + "'");
                }
            }
        }
    }

    private String prettyTheTypeName(Node n) {
        return n.theType.prettyTypeName();
    };

    @Override
    public void visit(ConstructorCall constCall) {
        // Basically same constraints apply as in funCall
        constCall.args.forEach(arg -> arg.accept(this));

        var structDef = structDefs.get(constCall.structName);
        if (structDef == null) {
            reportError(constCall, "Unknown constructor '" + constCall.structName + "'");
            constCall.theType = Type.UNKNOWN_T;
        } else {
            //structDef found
            constCall.theType = structDef.theType;

            var fieldsCount = structDef.fields.size();
            var argsCount = constCall.args.size();
            if (fieldsCount != argsCount) {
                reportError(constCall,
                        "Wrong number of arguments: expected " + fieldsCount
                                + " but " + argsCount + " provided");
            }

            var argsIter = constCall.args.iterator();
            for (var field : structDef.fields) {
                if (!argsIter.hasNext()) {
                    break;
                }
                var arg = argsIter.next();

                if (!field.type().theType.isCompatible(arg.theType)) {
                    reportError(constCall, "Invalid argument type for field '" + field.name()
                            + "': Expected '" + prettyTheTypeName(field.type())
                            + "' but found incompatible '" + prettyTheTypeName(arg) + "'");
                }
            }
        }
    }

    @Override
    public void visit(BinOpExpr binOpExpr) {
        // Compute type of the operands
        binOpExpr.lhs.accept(this);
        binOpExpr.rhs.accept(this);
        var lhsType = binOpExpr.lhs.theType;
        var rhsType = binOpExpr.rhs.theType;
        var op = binOpExpr.op;

        Type exprType;
        if (op.isBoolean()) {
            exprType = Type.BOOL_T;
            if (!lhsType.equals(exprType) || !rhsType.equals(exprType)) {
                reportBinOpExprErrorMsg(binOpExpr);
            }
        } else if (op.isArithmetic()) {
            exprType = lhsType;
            if (!lhsType.isNumeric() || !rhsType.isNumeric() || !rhsType.equals(exprType)) {
                reportBinOpExprErrorMsg(binOpExpr);
            }
        } else if (op.isEqualityComparison()) {
            exprType = Type.BOOL_T;
            var isComparable = lhsType.isReference() && rhsType.isReference()
                    || lhsType.equals(rhsType);
            if (!isComparable) {
                reportBinOpExprErrorMsg(binOpExpr, "Operand types must be equal or RefType");
            }
        } else if (op.isOrdinalComparison()) {
            exprType = Type.BOOL_T;
            var isComparable = lhsType.equals(rhsType) && lhsType.isNumeric();
            if (!isComparable) {
                reportBinOpExprErrorMsg(binOpExpr,
                        "Operand types must be of equal, primitive, numerical kind");
            }
        } else {
            throw new UnsupportedOperationException("Unknown binary operator: " + op);
        }

        binOpExpr.theType = Objects.requireNonNull(exprType, "Expected valid Type object but was null");
    }

    private void reportBinOpExprErrorMsg(BinOpExpr expr) {
        reportBinOpExprErrorMsg(expr, null);
    }

    private void reportBinOpExprErrorMsg(BinOpExpr expr, String hint) {
        reportError(expr, "Can't use binary '" + expr.op.toLexeme() + "' with operands "
                + prettyTheTypeName(expr.lhs) + ", " + prettyTheTypeName(expr.rhs)
                + (hint == null ? "" : " (" + hint + ")"));
    }

    @Override
    public void visit(UnaryOpExpr unaryOpExpr) {
        unaryOpExpr.operand.accept(this);
        var operandType = unaryOpExpr.operand.theType;
        unaryOpExpr.theType = operandType;
        var op = unaryOpExpr.op;

        if (op.isBoolean() && (operandType.isNumeric() || operandType.isReference())) {
            reportError(unaryOpExpr, "Boolean operator incompatible with '"
                    + operandType.prettyTypeName() + "' operand");
        }
        if (op.isArithmetic() && !operandType.isNumeric()) {
            reportError(unaryOpExpr, "Arithmetic operator '" + op
                    + "' incompatible with '" + operandType.prettyTypeName() + "' operand");
        }
    }

    @Override
    public void visit(TernaryConditionalExpr ternaryConditionalExpr) {
        ternaryConditionalExpr.condition.accept(this);
        ternaryConditionalExpr.then.accept(this);
        ternaryConditionalExpr.otherwise.accept(this);
        var condType = ternaryConditionalExpr.condition.theType;
        var thenType = ternaryConditionalExpr.then.theType;
        var otherwiseType = ternaryConditionalExpr.otherwise.theType;
        ternaryConditionalExpr.theType = thenType;

        if (!thenType.isCompatible(otherwiseType)) {
            reportError(ternaryConditionalExpr.then, "Conditional branches return incompatible types");
        }
        if (!condType.equals(Type.BOOL_T)) {
            reportError(ternaryConditionalExpr.condition, "Condition must return a boolean type");
        }
    }

    @Override
    public void visit(VarDeclareStat varDeclareStat) {
        /*
        TODO
        - when declaration has optional initializer: check whether types are compatible
        */

        varDeclareStat.declaredType.accept(this);
        varDeclareStat.theType = varDeclareStat.declaredType.theType;
        var previous = funDefVarTypes.put(varDeclareStat.varName, varDeclareStat.theType);
        if (previous != null) {
            reportError(varDeclareStat, "Attempt to redeclare variable '" + varDeclareStat.varName + "'");
        }
    }

    @Override
    public void visit(VarAssignStat varAssignStat) {
        var varType = funDefVarTypes.get(varAssignStat.targetVarName);
        varAssignStat.expr.accept(this);
        var exprType = varAssignStat.expr.theType;

        if (varType == null) {
            reportError(varAssignStat, "Assignment to undeclared variable '"
                    + varAssignStat.targetVarName + "'");
        } else if (!(varType.isCompatible(exprType))) {
            reportError(varAssignStat, "Attempt to assign value of type '"
                    + exprType.prettyTypeName() + "' to variable '" + varAssignStat.targetVarName
                    + "' of incompatible type '" + varType.prettyTypeName() + "'");
        }

        varAssignStat.theType = varType;
    }

    @Override
    public void visit(IfElseStat ifElseStat) {
        ifElseStat.condition.accept(this);
        ifElseStat.then.accept(this);
        var condType = ifElseStat.condition.theType;
        var thenType = ifElseStat.then.theType;
        ifElseStat.otherwise.accept(this);

        if (!condType.equals(Type.BOOL_T)) {
            reportError(ifElseStat.condition, "Condition must evaluate to boolean but is '"
                    + condType.prettyTypeName() + "'");
        }

        ifElseStat.theType = thenType;
    }

    /** Indicates that the visitor is currently in a loop context */
    private boolean isLoopContext = false;

    @Override
    public void visit(LoopStat loopStat) {
        var prevState = isLoopContext;

        isLoopContext = true;
        loopStat.body.accept(this);
        isLoopContext = false;
        loopStat.theType = Type.VOID_T;

        isLoopContext = prevState;
    }

    @Override
    public void visit(StatementList statementList) {
        statementList.statements.forEach(s -> s.accept(this));
        statementList.theType = statementList.isEmpty()
                ? Type.VOID_T
                : statementList.statements.getLast().theType;
    }

    @Override
    public void visit(ReturnStat returnStat) {
        var retType = currentFun.returnType.theType;
        currentFunReturnCount += 1;
        returnStat.theType = retType;

        returnStat.expr.accept(this);
        var exprType = returnStat.expr.theType;
        if (!retType.isCompatible(exprType)) {
            reportError(returnStat, "Expected return type '" + retType.prettyTypeName()
                    + "' but found incompatible '" + exprType.prettyTypeName() + "'");
        }
    }

    @Override
    public void visit(BreakStat breakStat) {
        if (!isLoopContext) {
            reportError(breakStat, "'break' only allowed inside loop body");
        }
        breakStat.theType = Type.VOID_T;
    }

    @Override
    public void visit(TypeNode type) {
        // TODO handle non-default packages
        type.theType = Type.of(type.typeToken, "" /*default package */);
        if (!type.isBuiltin && !structDefs.containsKey(type.typeToken)) {
            reportError(type, "Undefined type '" + type.typeToken + "'");
        }
    }

    /**
     * Mapping of a function's local variable names and their  associated declared/inferred types.
     * Here only references to "Node.theType" are to be added.
     */
    private Map<String, Type> funDefVarTypes = new HashMap<>();

    /** Reference to currently inspected function def */
    private FunDef currentFun = null;
    /** Return statements count for currentFun */
    private int currentFunReturnCount = 0;

    @Override
    public void visit(FunDef funDef) {
        // Hint: Signature nodes are already evaluated in Prog
        currentFun = funDef;
        currentFunReturnCount = 0;

        // Make sure the local variables lookup table is empty before proceeding
        funDefVarTypes.clear();

        funDef.params.forEach(p -> {
            var previous = funDefVarTypes.put(p.name(), p.type().theType);
            if (previous != null) {
                reportError(funDef, "Duplicate parameter name '" + p.name()
                        + "' in function '" + funDef.name + "'");
            }
        });

        // Check return type consistency in the according node(s)
        // using "currentFun" and the "currentFunReturnCount" counter
        funDef.body.accept(this);

        // Infer 'void' for no returns
        if (currentFunReturnCount == 0 && !funDef.returnType.theType.equals(Type.VOID_T)) {
            reportError(funDef, "Declared return type '" + prettyTheTypeName(funDef.returnType)
                    + "' but no return statement was found");
        }

        funDef.theType = funDef.returnType.theType;
    }

    @Override
    public void visit(StructDef structDef) {
        //TODO handle custom package
        structDef.theType = Type.of(structDef.name, "" /*default package */);
        Set<String> fnames = new HashSet<>();
        structDef.fields.forEach(f -> {
            if (!fnames.add(f.name())) {
                reportError(structDef, "Duplicate field '" + structDef.name + "." + f.name() + "'");
            }
            f.type().accept(this);
        });
    }

    private Map<String, FunDef> funDefs = new HashMap<>();
    private Map<String, StructDef> structDefs = new HashMap<>();

    @Override
    public void visit(Prog prog) {
        prog.structDefs.forEach(structDef -> {
            var previous = structDefs.put(structDef.name, structDef);
            if (previous != null) {
                reportError(structDef, "Struct type '" + structDef.name + "' defined multiple times");
            }
            structDef.accept(this);
        });

        // Before traversiing the tree, we need to make funDefs available for other "visit"s
        prog.funDefs.forEach(funDef -> {
            var previous = funDefs.put(funDef.name, funDef);
            if (previous != null) {
                reportError(funDef, "Function '" + funDef.name + "' defined multiple times");
            }

            // Enables call to fun whose body was not checked yet
            funDef.params.forEach(p -> p.type().accept(this));
            funDef.returnType.accept(this);
        });
        prog.funDefs.forEach(funDef -> funDef.accept(this));

        prog.theType = Type.VOID_T;

        // Check optional entrypoint call
        if (prog.entryPoint != null) {
            prog.entryPoint.accept(this);
            prog.theType = prog.entryPoint.theType;
        }
        if (errorsReported != 0) {
            throw new TypeCheckFailedException();
        }
    }

    @Override
    public void visit(EmptyNode emptyNode) {
        emptyNode.theType = Type.VOID_T;
    }

    public class TypeCheckFailedException extends RuntimeException {
        public TypeCheckFailedException() {
            super("TypeCheck resulted in " + errorsReported + " errors.");
        }
    }
}
