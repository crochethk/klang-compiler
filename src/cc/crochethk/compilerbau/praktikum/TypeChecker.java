package cc.crochethk.compilerbau.praktikum;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import cc.crochethk.compilerbau.praktikum.ast.BinOpExpr;
import cc.crochethk.compilerbau.praktikum.ast.BooleanLit;
import cc.crochethk.compilerbau.praktikum.ast.EmptyNode;
import cc.crochethk.compilerbau.praktikum.ast.FunCall;
import cc.crochethk.compilerbau.praktikum.ast.FunDef;
import cc.crochethk.compilerbau.praktikum.ast.IfElseStat;
import cc.crochethk.compilerbau.praktikum.ast.IntLit;
import cc.crochethk.compilerbau.praktikum.ast.Node;
import cc.crochethk.compilerbau.praktikum.ast.Prog;
import cc.crochethk.compilerbau.praktikum.ast.ReturnStat;
import cc.crochethk.compilerbau.praktikum.ast.StatementListNode;
import cc.crochethk.compilerbau.praktikum.ast.TernaryConditionalExpr;
import cc.crochethk.compilerbau.praktikum.ast.TypeNode;
import cc.crochethk.compilerbau.praktikum.ast.UnaryOpExpr;
import cc.crochethk.compilerbau.praktikum.ast.Var;
import cc.crochethk.compilerbau.praktikum.ast.VarAssignStat;
import cc.crochethk.compilerbau.praktikum.ast.VarDeclareStat;

/** TypeChecker Visitor
 * The main task of this Visitor is to semantically check typing of the visited AST.
 * While doing that each visited Node is annotated with its inferred type information
 * by assigning an appropriate Type object to {@code Node.theType}.
 */
public class TypeChecker implements Visitor<Void> {

    @Override
    public void reportError(Node node, String msg) {
        Visitor.super.reportError(node, "Type error: " + msg);
    }

    @Override
    public Void visit(IntLit intLit) {
        intLit.theType = Type.LONG_T;
        return null;
    }

    @Override
    public Void visit(BooleanLit booleanLit) {
        booleanLit.theType = Type.BOOLEAN_T;
        return null;
    }

    @Override
    public Void visit(BinOpExpr binOpExpr) {
        // Compute type of the operands
        binOpExpr.lhs.accept(this);
        binOpExpr.rhs.accept(this);
        var lhsType = binOpExpr.lhs.theType;
        var rhsType = binOpExpr.rhs.theType;

        Type exprType;
        if (binOpExpr.op.isBoolean()) {
            exprType = Type.BOOLEAN_T;
            if (!lhsType.equals(exprType) || !rhsType.equals(exprType)) {
                reportError(binOpExpr, lhsType + " " + binOpExpr.op + " " + rhsType);
            }
        } else if (binOpExpr.op.isArithmetic()) {
            exprType = Type.LONG_T;
            if (!lhsType.equals(exprType) || !rhsType.equals(exprType)) {
                reportError(binOpExpr, lhsType + " " + binOpExpr.op + " " + rhsType);
            }
        } else if (binOpExpr.op.isComparison()) {
            exprType = Type.BOOLEAN_T;
            if (!(lhsType.equals(rhsType) && lhsType.isNumeric() && rhsType.isNumeric())) {
                reportError(binOpExpr, lhsType + " " + binOpExpr.op + " " + rhsType);
            }
        } else {
            throw new UnsupportedOperationException("Unknown binary operator: " + binOpExpr.op);
        }

        binOpExpr.theType = Objects.requireNonNull(exprType, "Expected valid Type object but was null");
        return null;
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
    public Void visit(FunDef funDef) {
        currentFun = funDef;
        currentFunReturnCount = 0;

        // Compute types of signature nodes
        funDef.params.forEach(p -> p.type().accept(this));
        funDef.returnType.accept(this);

        // Make sure the local variables lookup table is empty before proceeding
        funDefVarTypes.clear();

        funDef.params.forEach(p -> funDefVarTypes.put(p.name(), p.type().theType));

        // Check return type consistency in the according node(s)
        // using "currentFun" and the "currentFunReturnCount" counter
        funDef.body.accept(this);

        // Infer 'void' for no returns
        if (currentFunReturnCount == 0 && funDef.returnType.theType != Type.VOID_T) {
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
        return null;
    }

    private Map<String, FunDef> funDefs = new HashMap<>();

    @Override
    public Void visit(Prog prog) {
        // Before walk down the tree, we need to make funDefs available for other "visit"s
        prog.funDefs.forEach(funDef -> funDefs.put(funDef.name, funDef));
        prog.funDefs.forEach(def -> def.accept(this));

        // Check entrypoint call
        prog.entryPoint.accept(this);

        // prog.theType does not matter
        return null;
    }

    @Override
    public Void visit(ReturnStat returnStat) {
        var retType = currentFun.returnType.theType;
        currentFunReturnCount += 1;
        returnStat.theType = retType;

        returnStat.expr.accept(this);
        var exprType = returnStat.expr.theType;

        if (!retType.equals(exprType)) {
            reportError(returnStat, "Expected return type '" + retType
                    + "' but found incompatible '" + exprType + "'");
        }
        return null;
    }

    @Override
    public Void visit(VarAssignStat varAssignStat) {
        varAssignStat.expr.accept(this);

        var varType = funDefVarTypes.get(varAssignStat.targetVarName);
        var exprType = varAssignStat.expr.theType;

        if (varType == null) {
            reportError(varAssignStat, "Assignment to undeclared variable '" + varAssignStat.targetVarName + "'");
        } else if (!(varType.equals(exprType))) {
            reportError(varAssignStat, "Attempt to assign value of type '"
                    + exprType + "' to variable '" + varAssignStat.targetVarName
                    + "' of incompatible type '" + varType + "'");
        }

        varAssignStat.theType = varType;
        return null;
    }

    @Override
    public Void visit(VarDeclareStat varDeclareStat) {
        /*
        TODO TODO TODO TODO TODO 
        - consider checking, whether the declared type is actually defined
            - should only be relevant for custom types, since primitves are
            already implicitly checked upon building the AST...
            - probably should be delegated to "visit(Type)" instead
        
        - we will allow redeclaration of variables (so no check if already declared)
        
        - when declaration has optional initializer: check whether types match
        */

        varDeclareStat.declaredType.accept(this);
        varDeclareStat.theType = varDeclareStat.declaredType.theType;
        funDefVarTypes.put(varDeclareStat.varName, varDeclareStat.theType);
        return null;
    }

    @Override
    public Void visit(StatementListNode statementListNode) {
        statementListNode.value.accept(this);
        statementListNode.next.accept(this);
        statementListNode.theType = Type.VOID_T;
        return null;
    }

    @Override
    public Void visit(IfElseStat ifElseStat) {
        ifElseStat.condition.accept(this);
        ifElseStat.then.accept(this);
        ifElseStat.otherwise.accept(this);
        var condType = ifElseStat.condition.theType;

        if (!condType.equals(Type.BOOLEAN_T)) {
            reportError(ifElseStat.condition,
                    "Condition must evaluate to boolean but is '" + condType + "'");
        }

        ifElseStat.theType = ifElseStat.then.theType;
        return null;
    }

    @Override
    public Void visit(Var var) {
        var varType = funDefVarTypes.get(var.name);
        if (varType == null) {
            reportError(var, "Use of undefined variable '" + var.name + "'");
        }

        var.theType = varType;
        return null;
    }

    @Override
    public Void visit(FunCall funCall) {
        funCall.args.forEach(arg -> arg.accept(this));
        /*
        TODO TODO TODO TODO TODO 
        - check if FunCall args types are consistent with the matching FunDef
        - set "funCall.theType = funDef.returnType.theType"
        */

        return null;
    }

    @Override
    public Void visit(UnaryOpExpr unaryOpExpr) {
        unaryOpExpr.operand.accept(this);
        var operandType = unaryOpExpr.operand.theType;
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
        return null;
    }

    @Override
    public Void visit(TernaryConditionalExpr ternaryConditionalExpr) {
        ternaryConditionalExpr.condition.accept(this);
        ternaryConditionalExpr.then.accept(this);
        ternaryConditionalExpr.otherwise.accept(this);
        var condType = ternaryConditionalExpr.condition.theType;
        var thenType = ternaryConditionalExpr.then.theType;
        var otherwiseType = ternaryConditionalExpr.otherwise.theType;
        ternaryConditionalExpr.theType = thenType;

        if (!thenType.equals(otherwiseType)) {
            reportError(ternaryConditionalExpr.then, "Conditional branches return incompatible types");
        }
        if (!condType.equals(Type.BOOLEAN_T)) {
            reportError(ternaryConditionalExpr.condition, "Condition must return a boolean type");
        }
        return null;
    }

    @Override
    public Void visit(EmptyNode emptyNode) {
        emptyNode.theType = Type.VOID_T;
        return null;
    }

    @Override
    public Void visit(TypeNode type) {
        type.theType = Type.of(type.typeToken, "" /*default package */);
        return null;
    }
}
