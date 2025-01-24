package cc.crochethk.klang.ast.literal;

import java.util.Objects;

import utils.SourcePos;

public abstract class NumberLiteral<T> extends LiteralExpr<T> {
    public boolean hasTypeAnnotation = false;

    public NumberLiteral(SourcePos srcPos, T value, boolean hasTypeAnnotation) {
        super(srcPos, value);
        this.hasTypeAnnotation = hasTypeAnnotation;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), hasTypeAnnotation);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof NumberLiteral other) {
            return this.hasTypeAnnotation == other.hasTypeAnnotation && super.equals(other);
        }
        return false;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "(" + value + ", " + hasTypeAnnotation + ")";
    }
}
