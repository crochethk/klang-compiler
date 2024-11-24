package cc.crochethk.compilerbau.praktikum.ast.literals;

import utils.SourcePos;

public abstract class NumberLiteral<T> extends LiteralExpr<T> {
    public NumberLiteral(SourcePos srcPos, T value) {
        super(srcPos, value);
    }
}
