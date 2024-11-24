package cc.crochethk.compilerbau.praktikum.ast.literals;

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
}
