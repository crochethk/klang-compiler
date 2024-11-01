package cc.crochethk.compilerbau.p2;

import cc.crochethk.compilerbau.p2.BinOpExpr.BinaryOp;

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
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public Void visit(BinOpExpr binOpExpr) throws Exception {
        // Compute type of the operands
        binOpExpr.lhs.accept(this);
        binOpExpr.rhs.accept(this);

        if (BinaryOp.boolOps.contains(binOpExpr.op)) {
            binOpExpr.theType = "boolean";
            if (!binOpExpr.lhs.theType.equals("boolean") || !binOpExpr.rhs.theType.equals("boolean")) {
                reportError(binOpExpr, binOpExpr.lhs.theType + " " + binOpExpr.op + " " + binOpExpr.rhs.theType);
            }
        } else if (BinaryOp.arithOps.contains(binOpExpr.op)) {
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
