package cc.crochethk.compilerbau.praktikum;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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
        intLit.theType = createI64T(intLit.line, intLit.column);
        return null;
    }

    @Override
    public Void visit(BooleanLit booleanLit) {
        booleanLit.theType = createBoolT(booleanLit.line, booleanLit.column);
        return null;
    }

    @Override
    public Void visit(BinOpExpr binOpExpr) {
        // Compute type of the operands
        binOpExpr.lhs.accept(this);
        binOpExpr.rhs.accept(this);

        var lhsType = binOpExpr.lhs.theType;
        var rhsType = binOpExpr.rhs.theType;
        var exprLine = binOpExpr.line;
        var exprCol = binOpExpr.column;

        Type exprType;
        if (binOpExpr.op.isBoolean()) {
            exprType = createBoolT(exprLine, exprCol);
            if (!lhsType.equals(exprType) || !rhsType.equals(exprType)) {
                reportError(binOpExpr, lhsType + " " + binOpExpr.op + " " + rhsType);
            }
        } else if (binOpExpr.op.isArithmetic()) {
            exprType = createI64T(exprLine, exprCol);
            if (!lhsType.equals(exprType) || !rhsType.equals(exprType)) {
                reportError(binOpExpr, lhsType + " " + binOpExpr.op + " " + rhsType);
            }
        } else {
            throw new UnsupportedOperationException("Unknown binary operator: " + binOpExpr.op);
        }

        binOpExpr.theType = Objects.requireNonNull(exprType, "Expected valid Type object but was null");
        return null;
    }

    /**
     * List used to keep track of return types of a function's body.
     * Here only references to "Node.theType" are to be added.
     */
    private List<Type> funDefPathsReturnTypes = new ArrayList<>();

    /**
     * Mapping of a function's local variable names and their  associated declared/inferred types.
     * Here only references to "Node.theType" are to be added.
     */
    private Map<String, Type> funDefVarTypes = new HashMap<>();

    private Map<String, FunDef> funDefs = new HashMap<>();

    @Override
    public Void visit(FunDef funDef) {
        funDefs.put(funDef.name, funDef);
        // Compute types of subnodes
        funDef.params.forEach(p -> p.type().accept(this));
        funDef.returnType.accept(this);

        // Make sure the lookup collections are empty before evaluating the funDef
        funDefPathsReturnTypes.clear();
        funDefVarTypes.clear();

        funDef.params.forEach(p -> funDefVarTypes.put(p.name(), p.type().theType));

        /*
        Each possible body-node-type must consider adding its result type to funDefPathsReturnTypes.
        Though actually only returnStat should be relevant, since all other do not immediately
        return a value to the caller.
        
        This leads to three cases, regarding the nested nodes inside "funDef.body":
        1. all "returnStat" have the same types ("theType" field)
        2. "returnStat" types are unequal
        3. there are no "returnStat"
            [x] -> infer Void in this case
        */
        funDef.body.accept(this);
        /*
        Check the aggregated "funDefPathsReturnTypes":
            [x] - are they consistent with "funDef.returnType.theType"?
            [x] - are all basically the same? (see above)
        */

        // Infer 'void' for no returns
        if (funDefPathsReturnTypes.isEmpty()) {
            funDefPathsReturnTypes.add(createVoidT(funDef.line, funDef.column));
        }
        var typeSample = funDefPathsReturnTypes.getFirst();

        if (!(typeSample.equals(funDef.returnType.theType))) {
            reportError(funDef, "Declared type '" + funDef.returnType.theType
                    + "' but returns incompatible '" + typeSample + "')");
        }
        if (!(funDefPathsReturnTypes.stream().allMatch(t -> typeSample.equals(t)))) {
            reportError(funDef, "Not all code paths return the same type");
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

    @Override
    public Void visit(Prog prog) {
        prog.entryPoint.accept(this);
        prog.funDefs.forEach(def -> def.accept(this));

        prog.theType = prog.entryPoint.theType;
        return null;
    }

    @Override
    public Void visit(ReturnStat returnStat) {
        returnStat.expr.accept(this);
        returnStat.theType = returnStat.expr.theType;
        funDefPathsReturnTypes.add(returnStat.theType);
        return null;
    }

    @Override
    public Void visit(VarAssignStat varAssignStat) {
        /*
        [x] 0. Make sure local variables are somehow tracked in the visitor (e.g. Map<String, Type>)
        [x] 1. lookup the type of "targetVar" in the local variables stack (or whatever) using "var.name"
            2. if lookup...
            [x] 2.1 ...succeeded: "targetVar" is declared
                    -> Check compatibility of "expr.theType" and "targetVarType"
                [x]     - fail: reportError "Assignment of incompatible type"
                [x]     - success: Assign "varAssignStat.theType = targetVar.theType" (or  ""...= expr.theType")
            [x] 2.2 ...failed: report Error "Assignment to undeclared variable"
        */

        varAssignStat.expr.accept(this);

        var varType = funDefVarTypes.get(varAssignStat.targetVarName);
        var exprType = varAssignStat.expr.theType;

        if (varType == null) {
            reportError(varAssignStat, "Assignment to undeclared variable '" + varAssignStat.targetVarName + "'");
        } else if (!(varType.equals(exprType))) {
            reportError(varAssignStat, "Attempt to assign value of type '"
                    + exprType + "' to variable '" + varAssignStat.targetVarName
                    + "' of incompatible type '" + varType + "')");
        }

        varAssignStat.theType = exprType;
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
        // statementListNode.next.accept(this);
        /*
        TODO TODO TODO TODO TODO
        - to consider:
            - check that all code paths (one or more return statements) produce a similar type
            - multiple return statements in the list
                - "how to track and check this?"
                    - create new list of types on funDef visit
                    - collect each returnStat type into this list
                    - in fundef: do type ckecks with this list after evaluation the funDef.body
                    - empty list when done, so next funDef can start with fresh context
                - checking if all return compatible type
            - I believe interpreter already had a solution to the multi-return problem
        */
        return null;
    }

    @Override
    public Void visit(IfElseStat ifElseStat) {
        ifElseStat.condition.accept(this);
        ifElseStat.then.accept(this);
        ifElseStat.otherwise.accept(this);

        /*
        TODO TODO TODO TODO TODO 
        - see ternaryNode and interpreter visitor impl about result typing?
            - maybe something along the lines:
                - check "then.theType == otherwise.theType"
                - check whether "condition.theType == BoolT"
                - set: ifElseStat.theType = then.theType
        */
        return null;
    }

    @Override
    public Void visit(Var var) {
        /*
        [x] 0. Make sure local variables are somehow tracked in the visitor (e.g. Map<String, Type>)
        [x] 1. lookup the type of "var" in the local variables stack (or whatever) using "var.name"
            2. if lookup...
            [x] 2.1 ...succeeded: "var" is declared -> set "var.theType = varType"
            [x] 2.2 ...failed: report Error "Use of undefined variable '" + var.name + "'"
        */
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
        //TODO maybe a check should be done, whether the operator is applicable to the operand type
        unaryOpExpr.operand.accept(this);
        unaryOpExpr.theType = unaryOpExpr.operand.theType;
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
            reportError(ternaryConditionalExpr, "Conditional branches return different types");
        }
        if (!(condType instanceof BoolT)) {
            reportError(ternaryConditionalExpr, "Condition must return a boolean type");
        }
        return null;
    }

    @Override
    public Void visit(EmptyNode emptyNode) {
        emptyNode.theType = createVoidT(emptyNode.line, emptyNode.column);
        return null;
    }

    @Override
    public Void visit(TypeNode type) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }
}
