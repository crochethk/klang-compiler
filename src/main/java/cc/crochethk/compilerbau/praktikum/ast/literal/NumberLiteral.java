package cc.crochethk.compilerbau.praktikum.ast.literal;

import utils.SourcePos;

public abstract class NumberLiteral<T> extends LiteralExpr<T> {
    public boolean hasTypeAnnotation = false;

    public NumberLiteral(SourcePos srcPos, T value, boolean hasTypeAnnotation) {
        super(srcPos, value);
        this.hasTypeAnnotation = hasTypeAnnotation;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof NumberLiteral other) {
            return this.hasTypeAnnotation == other.hasTypeAnnotation && super.equals(other);
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "(" + value + ", " + hasTypeAnnotation + ")";
    }
}
