package cc.crochethk.compilerbau.praktikum.ast.literal;

import cc.crochethk.compilerbau.praktikum.ast.Node;
import utils.SourcePos;

/** The base class of all the literals */
public abstract class LiteralExpr<T> extends Node {
    /** The value of the literal */
    public T value;

    public LiteralExpr(SourcePos srcPos, T value) {
        super(srcPos);
        this.value = value;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof LiteralExpr other) {
            return this.value.equals(other.value);
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "(" + value + ")";
    }
}
