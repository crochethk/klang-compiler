package cc.crochethk.klang.ast;

import java.util.Objects;

import cc.crochethk.klang.visitor.Visitor;
import utils.SourcePos;

/**
 * Node type representing a variable name as part of an expression.
 * For example <code>x</code> in <code>1+x+3</code>.
 */
public class Var extends Expr {
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
    public int hashCode() {
        return Objects.hash(super.hashCode(), name);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof Var other) {
            return Objects.equals(name, other.name) && super.equals(other);
        }
        return false;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "(name=" + name + ")";
    }
}
