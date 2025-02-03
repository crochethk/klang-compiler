package cc.crochethk.klang.ast;

import utils.SourcePos;

/** The base class of all the expression nodes */
public abstract class Expr extends Node {
    public Expr(SourcePos srcPos) {
        super(srcPos);
    }
}
