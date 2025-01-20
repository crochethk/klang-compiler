package cc.crochethk.compilerbau.praktikum.ast;

import cc.crochethk.compilerbau.praktikum.visitor.Visitor;
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
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof Var other) {
            return name.equals(other.name) && super.equals(other);
        }
        return false;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "(name=" + name + ")";
    }
}
