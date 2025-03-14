package cc.crochethk.klang.ast;

import java.util.Objects;

import cc.crochethk.klang.visitor.Visitor;
import utils.SourcePos;

/**
 * Statement that wraps an expression which does not return a value.
 */
public class VoidResultExprStat extends Node {
    public Expr expr;

    public VoidResultExprStat(SourcePos srcPos, Expr voidExpr) {
        super(srcPos);
        this.expr = voidExpr;
    }

    @Override
    public boolean isEmpty() {
        return expr.isEmpty();
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return super.toString() + "(expr=" + expr + ")";
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), expr);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof VoidResultExprStat other) {
            return Objects.equals(expr, other.expr) && super.equals(other);
        }
        return false;
    }
}
