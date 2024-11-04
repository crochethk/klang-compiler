package cc.crochethk.compilerbau.p3;

import cc.crochethk.compilerbau.p3.ast.BinOpExpr;
import cc.crochethk.compilerbau.p3.ast.BooleanLit;
import cc.crochethk.compilerbau.p3.ast.IntLit;
import cc.crochethk.compilerbau.p3.ast.Node;

public class TypeCheckVisitor implements Visitor<Void> {

    @Override
    public void reportError(Node node, String msg) {
        Visitor.super.reportError(node, "Type error: " + msg);
    }

    @Override
    public Void visit(IntLit intLit) throws Exception {
        intLit.theType = "int";
        return null;
    }

    @Override
    public Void visit(BooleanLit booleanLit) throws Exception {
        booleanLit.theType = "boolean";
        return null;
    }

    @Override
    public Void visit(BinOpExpr binOpExpr) throws Exception {
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
}
