package cc.crochethk.compilerbau.praktikum.ast;

import cc.crochethk.compilerbau.praktikum.Visitor;
import utils.SourcePos;

/**
 * Node type representing a variable name as part of an expression.
 * For example <code>x</code> in <code>1+x+3</code>.
 */
public class Var extends Node {
    public String name;

    public Var(SourcePos srcPos, String name) {
        super(srcPos);
        this.name = name;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visit(this);
    }
}
