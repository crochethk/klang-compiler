package cc.crochethk.klang.ast.literal;

import java.util.Objects;

import cc.crochethk.klang.ast.Expr;
import utils.SourcePos;

/** The base class of all the literals */
public abstract class LiteralExpr<T> extends Expr {
    /** The value of the literal */
    public T value;

    public LiteralExpr(SourcePos srcPos, T value) {
        super(srcPos);
        this.value = value;
    }

    @Override
    public boolean isOrHasFunCall() {
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), value);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof LiteralExpr other) {
            return Objects.equals(this.value, other.value) && super.equals(other);
        }
        return false;
    }

    @Override
    public String toString() {
        return super.toString() + "(" + value + ")";
    }
}
