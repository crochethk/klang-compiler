package cc.crochethk.compilerbau.praktikum.ast.literal;

import utils.SourcePos;

public abstract class NumberLiteral<T> extends LiteralExpr<T> {
    public boolean hasTypeAnnotation = false;

    public NumberLiteral(SourcePos srcPos, T value, boolean hasTypeAnnotation) {
        super(srcPos, value);
        this.hasTypeAnnotation = hasTypeAnnotation;
    }
}
