package cc.crochethk.compilerbau.praktikum;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import cc.crochethk.compilerbau.praktikum.ast.*;
import cc.crochethk.compilerbau.praktikum.ast.literals.*;

/** TypeChecker Visitor
 * The main task of this Visitor is to semantically check typing of the visited AST.
 * While doing that each visited Node is annotated with its inferred type information
 * by assigning an appropriate Type object to {@code Node.theType}.
 */
public class TypeChecker implements Visitor<Type> {
    @Override
    public void reportError(Node node, String msg) {
        Visitor.super.reportError(node, "Type error: " + msg);
    }

    @Override
    public Type visit(I64Lit i64Lit) {
        i64Lit.theType = Type.LONG_T;
        return i64Lit.theType;
    }

    @Override
    public Type visit(F64Lit f64Lit) {
        f64Lit.theType = Type.DOUBLE_T;
        return f64Lit.theType;
    }

    @Override
    public Type visit(BoolLit boolLit) {
        boolLit.theType = Type.BOOL_T;
        return boolLit.theType;
    }

    @Override
    public Type visit(StringLit stringLit) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Type visit(Var var) {
        var varType = funDefVarTypes.get(var.name);
        if (varType == null) {
            reportError(var, "Use of undefined variable '" + var.name + "'");
        }
        var.theType = varType;
        return varType;
    }

    @Override
    public Type visit(FunCall funCall) {
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
                if (!p.type().theType.equals(arg.theType)) {
                    reportError(funCall, "Invalid argument type '" + arg.theType + "'");
                }
            }
        }

        return funCall.theType;
    }

    @Override
    public Type visit(BinOpExpr binOpExpr) {
        // Compute type of the operands
        var lhsType = binOpExpr.lhs.accept(this);
        var rhsType = binOpExpr.rhs.accept(this);

        Type exprType;
        if (binOpExpr.op.isBoolean()) {
            exprType = Type.BOOL_T;
            if (!lhsType.equals(exprType) || !rhsType.equals(exprType)) {
                reportError(binOpExpr, lhsType + " " + binOpExpr.op + " " + rhsType);
            }
        } else if (binOpExpr.op.isArithmetic()) {
            exprType = binOpExpr.lhs.theType;
            if (!lhsType.equals(exprType) || !rhsType.equals(exprType)) {
                reportError(binOpExpr, lhsType + " " + binOpExpr.op + " " + rhsType);
            }
        } else if (binOpExpr.op.isComparison()) {
            exprType = Type.BOOL_T;
            if (!(lhsType.equals(rhsType) && lhsType.isNumeric() && rhsType.isNumeric())) {
                reportError(binOpExpr, lhsType + " " + binOpExpr.op + " " + rhsType);
            }
        } else {
            throw new UnsupportedOperationException("Unknown binary operator: " + binOpExpr.op);
        }

        binOpExpr.theType = Objects.requireNonNull(exprType, "Expected valid Type object but was null");
        return binOpExpr.theType;
    }

    @Override
    public Type visit(UnaryOpExpr unaryOpExpr) {
        var operandType = unaryOpExpr.operand.accept(this);
        unaryOpExpr.theType = operandType;
        var op = unaryOpExpr.op;

        if (op.isBoolean() && operandType.isNumeric()) {
            reportError(unaryOpExpr, "Boolean operator incompatible with '"
                    + operandType + "' operand");
        }
        if (op.isArithmetic() && !operandType.isNumeric()) {
            reportError(unaryOpExpr, "Arithmetic operator '" + op
                    + "' incompatible with '" + operandType + "' operand");
        }
        return unaryOpExpr.theType;
    }

    @Override
    public Type visit(TernaryConditionalExpr ternaryConditionalExpr) {
        var condType = ternaryConditionalExpr.condition.accept(this);
        var thenType = ternaryConditionalExpr.then.accept(this);
        var otherwiseType = ternaryConditionalExpr.otherwise.accept(this);
        ternaryConditionalExpr.theType = thenType;

        if (!thenType.equals(otherwiseType)) {
            reportError(ternaryConditionalExpr.then, "Conditional branches return incompatible types");
        }
        if (!condType.equals(Type.BOOL_T)) {
            reportError(ternaryConditionalExpr.condition, "Condition must return a boolean type");
        }
        return ternaryConditionalExpr.theType;
    }

    @Override
    public Type visit(VarDeclareStat varDeclareStat) {
        /*
        TODO TODO TODO TODO TODO 
        - consider checking, whether the declared type is actually defined
            - should only be relevant for custom types, since primitves are
            already implicitly checked upon building the AST...
            - probably should be delegated to "visit(Type)" instead
        
        - we will allow redeclaration of variables (so no check if already declared)
        
        - when declaration has optional initializer: check whether types match
        */

        varDeclareStat.theType = varDeclareStat.declaredType.accept(this);
        funDefVarTypes.put(varDeclareStat.varName, varDeclareStat.theType);
        return varDeclareStat.theType;
    }

    @Override
    public Type visit(VarAssignStat varAssignStat) {
        var varType = funDefVarTypes.get(varAssignStat.targetVarName);
        var exprType = varAssignStat.expr.accept(this);

        if (varType == null) {
            reportError(varAssignStat,
                    "Assignment to undeclared variable '" + varAssignStat.targetVarName + "'");
        } else if (!(varType.equals(exprType))) {
            reportError(varAssignStat, "Attempt to assign value of type '"
                    + exprType + "' to variable '" + varAssignStat.targetVarName
                    + "' of incompatible type '" + varType + "'");
        }

        varAssignStat.theType = varType;
        return varAssignStat.theType;
    }

    @Override
    public Type visit(IfElseStat ifElseStat) {
        var condType = ifElseStat.condition.accept(this);
        var thenType = ifElseStat.then.accept(this);
        ifElseStat.otherwise.accept(this);

        if (!condType.equals(Type.BOOL_T)) {
            reportError(ifElseStat.condition,
                    "Condition must evaluate to boolean but is '" + condType + "'");
        }

        ifElseStat.theType = thenType;
        return ifElseStat.theType;
    }

    /** Indicates that the visitor is currently in a loop context */
    private boolean isLoopContext = false;

    @Override
    public Type visit(LoopStat loopStat) {
        var prevState = isLoopContext;

        isLoopContext = true;
        loopStat.body.accept(this);
        isLoopContext = false;
        loopStat.theType = Type.VOID_T;

        isLoopContext = prevState;
        return loopStat.theType;
    }

    @Override
    public Type visit(StatementList statementList) {
        statementList.statements.forEach(s -> s.accept(this));
        statementList.theType = statementList.isEmpty()
                ? Type.VOID_T
                : statementList.statements.getLast().theType;
        return statementList.theType;
    }

    @Override
    public Type visit(ReturnStat returnStat) {
        var retType = currentFun.returnType.theType;
        currentFunReturnCount += 1;
        returnStat.theType = retType;

        var exprType = returnStat.expr.accept(this);
        if (!retType.equals(exprType)) {
            reportError(returnStat, "Expected return type '" + retType
                    + "' but found incompatible '" + exprType + "'");
        }
        return returnStat.theType;
    }

    @Override
    public Type visit(BreakStat breakStat) {
        if (!isLoopContext) {
            reportError(breakStat, "'break' only allowed inside loop body");
        }
        breakStat.theType = Type.VOID_T;
        return breakStat.theType;
    }

    @Override
    public Type visit(TypeNode type) {
        type.theType = Type.of(type.typeToken, "" /*default package */);
        return type.theType;
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
    public Type visit(FunDef funDef) {
        // Hint: Signature nodes are already evaluated in Prog
        currentFun = funDef;
        currentFunReturnCount = 0;

        // Make sure the local variables lookup table is empty before proceeding
        funDefVarTypes.clear();

        funDef.params.forEach(p -> funDefVarTypes.put(p.name(), p.type().theType));

        // Check return type consistency in the according node(s)
        // using "currentFun" and the "currentFunReturnCount" counter
        funDef.body.accept(this);

        // Infer 'void' for no returns
        if (currentFunReturnCount == 0 && !funDef.returnType.theType.equals(Type.VOID_T)) {
            reportError(funDef, "Declared return type '" + funDef.returnType.theType
                    + "' but no return statement was found");
        }

        /*
        TODO TODO TODO TODO 
        Probably a "FunctionT" Type should be introduced as functiondef type, maybe
        something like "java.lang.constant.MethodTypeDesc" (see GenJBC).
        */
        // TODO change to something more meaningful
        funDef.theType = funDef.returnType.theType;
        return funDef.theType;
    }

    private Map<String, FunDef> funDefs = new HashMap<>();

    @Override
    public Type visit(Prog prog) {
        // Before traversiing the tree, we need to make funDefs available for other "visit"s
        prog.funDefs.forEach(funDef -> {
            var previous = funDefs.put(funDef.name, funDef);
            if (previous != null) {
                reportError(funDef, "Function '" + funDef.name + "' defined multiple times");
            }

            // Enables call to fun whose definition was not evaluated yet
            // alternativels we could do ' Type.of(returnType,"")' where applicable 
            // instead of relying on theType...
            funDef.params.forEach(p -> p.type().accept(this));
            funDef.returnType.accept(this);
        });
        prog.funDefs.forEach(funDef -> funDef.accept(this));

        prog.theType = Type.VOID_T;

        // Check optional entrypoint call
        if (prog.entryPoint != null) {
            prog.entryPoint.accept(this);
            prog.theType = prog.entryPoint.theType;

            if (!funDefs.containsKey(prog.entryPoint.name)) {
                reportError(prog, "Entrypoint function '" + prog.entryPoint.name
                        + "' specified but no definition with matching name exists");
            }
        }
        return null;
    }

    @Override
    public Type visit(EmptyNode emptyNode) {
        emptyNode.theType = Type.VOID_T;
        return emptyNode.theType;
    }
}
