package cc.crochethk.klang.ast.literal;

import java.util.Objects;

import utils.SourcePos;

public abstract class NumberLiteral<T> extends LiteralExpr<T> {
    public NumberLiteral(SourcePos srcPos, T value) {
        super(srcPos, value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof NumberLiteral other) {
            return super.equals(other);
        }
        return false;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "(" + value + ")";
    }
}
