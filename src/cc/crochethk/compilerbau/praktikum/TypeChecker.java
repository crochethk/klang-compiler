package cc.crochethk.compilerbau.praktikum;

import java.util.ArrayList;
import java.util.List;

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
import cc.crochethk.compilerbau.praktikum.ast.UnaryOpExpr;
import cc.crochethk.compilerbau.praktikum.ast.Var;
import cc.crochethk.compilerbau.praktikum.ast.VarAssignStat;
import cc.crochethk.compilerbau.praktikum.ast.VarDeclareStat;
import cc.crochethk.compilerbau.praktikum.ast.types.*;

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
     * Here only the "Node.theType" object of the returning Node is to be added.
     */
    private List<Type> funDefPathsReturnTypes = new ArrayList<>();

    @Override
    public Void visit(FunDef funDef) {
        // Compute types of subnodes
        funDef.params.forEach(p -> p.type().accept(this));
        funDef.returnType.accept(this);

        // Make sure the list is empty before evaluation the function body
        funDefPathsReturnTypes.clear();

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
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visit(Var var) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visit(FunCall funCall) {
        // TODO Auto-generated method stub
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
    public Void visit(UnaryOpExpr unaryOpExpr) {
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
    public Void visit(VarAssignStat varAssignStat) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visit(VarDeclareStat varDeclareStat) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visit(StatementListNode statementListNode) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visit(IfElseStat ifElseStat) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visit(EmptyNode emptyNode) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visit(Type type) {
        // Self-reference. Type represents the type itself.
        type.theType = type;
        return null;
    }

    private Type createBoolT(int line, int column) {
        return new BoolT(line, column, "boolean");
    }

    private Type createI64T(int line, int column) {
        return new I64T(line, column, "int");
    }

    private Type createVoidT(int line, int column) {
        return new I64T(line, column, "void");
    }
}
