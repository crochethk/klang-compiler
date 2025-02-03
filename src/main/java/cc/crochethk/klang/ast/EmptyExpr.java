package cc.crochethk.klang.ast;

import cc.crochethk.klang.visitor.Visitor;
import utils.SourcePos;

public class EmptyExpr extends Expr {
    public EmptyExpr(SourcePos srcPos) {
        super(srcPos);
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof EmptyExpr other) {
            return super.equals(other);
        }
        return false;
    }
}