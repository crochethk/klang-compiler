package cc.crochethk.compilerbau.p3;

import cc.crochethk.compilerbau.p3.ast.BinOpExpr;
import cc.crochethk.compilerbau.p3.ast.BooleanLit;
import cc.crochethk.compilerbau.p3.ast.FunCall;
import cc.crochethk.compilerbau.p3.ast.FunDef;
import cc.crochethk.compilerbau.p3.ast.IntLit;
import cc.crochethk.compilerbau.p3.ast.Node;
import cc.crochethk.compilerbau.p3.ast.Prog;
import cc.crochethk.compilerbau.p3.ast.ReturnStat;
import cc.crochethk.compilerbau.p3.ast.TernaryConditionalExpr;
import cc.crochethk.compilerbau.p3.ast.UnaryOpExpr;
import cc.crochethk.compilerbau.p3.ast.Var;

public class TypeChecker implements Visitor<Void> {

    @Override
    public void reportError(Node node, String msg) {
        Visitor.super.reportError(node, "Type error: " + msg);
    }

    @Override
    public Void visit(IntLit intLit) {
        intLit.theType = "int";
        return null;
    }

    @Override
    public Void visit(BooleanLit booleanLit) {
        booleanLit.theType = "boolean";
        return null;
    }

    @Override
    public Void visit(BinOpExpr binOpExpr) {
        // Compute type of the operands
        binOpExpr.lhs.accept(this);
        binOpExpr.rhs.accept(this);

        if (binOpExpr.op.isBoolean()) {
            binOpExpr.theType = "boolean";
            if (!binOpExpr.lhs.theType.equals("boolean") || !binOpExpr.rhs.theType.equals("boolean")) {
                reportError(binOpExpr, binOpExpr.lhs.theType + " " + binOpExpr.op + " " + binOpExpr.rhs.theType);
            }
        } else if (binOpExpr.op.isArithmetic()) {
            binOpExpr.theType = "int";
            if (!binOpExpr.lhs.theType.equals("int") || !binOpExpr.rhs.theType.equals("int")) {
                reportError(binOpExpr, binOpExpr.lhs.theType + " " + binOpExpr.op + " " + binOpExpr.rhs.theType);
            }
        } else {
            throw new UnsupportedOperationException("Unknown binary operator: " + binOpExpr.op);
        }
        return null;
    }

    @Override
    public Void visit(FunDef funDef) {
        // TODO Auto-generated method stub
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
        // TODO Auto-generated method stub
        return null;
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
}
