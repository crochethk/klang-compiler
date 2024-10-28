package cc.crochethk.compilerbau.P2;

import cc.crochethk.compilerbau.P2.BinOpExpr.BinaryOp;

public class TypeCheckVisitor implements Visitor {

    @Override
    public void reportError(Node node, String msg) {
        Visitor.super.reportError(node, "Type error: " + msg);
    }

    @Override
    public void visit(IntLit intLit) {
        intLit.theType = "long";
    }

    @Override
    public void visit(BinOpExpr binOpExpr) {
        // Compute type of the operands
        binOpExpr.lhs.accept(this);
        binOpExpr.rhs.accept(this);

        if (BinaryOp.boolOps.contains(binOpExpr.op)) {
            binOpExpr.theType = "boolean";
            if (!binOpExpr.lhs.theType.equals("boolean") || !binOpExpr.rhs.theType.equals("boolean")) {
                reportError(binOpExpr, binOpExpr.lhs.theType + " " + binOpExpr.op + " " + binOpExpr.rhs.theType);
            }
        } else if (BinaryOp.arithOps.contains(binOpExpr.op)) {
            binOpExpr.theType = "long";
            if (!binOpExpr.lhs.theType.equals("long") || !binOpExpr.rhs.theType.equals("long")) {
                reportError(binOpExpr, binOpExpr.lhs.theType + " " + binOpExpr.op + " " + binOpExpr.rhs.theType);
            }
        } else {
            throw new UnsupportedOperationException("Unknown binary operator: " + binOpExpr.op);
        }
    }
}
