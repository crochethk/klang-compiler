package cc.crochethk.klang.ast;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import cc.crochethk.klang.visitor.Visitor;
import utils.SourcePos;

public class FunCall extends Expr {
    public String name;
    public List<Expr> args;

    public FunCall(SourcePos srcPos, String name, List<Expr> args) {
        super(srcPos);
        this.name = name;
        this.args = args != null ? args : Collections.emptyList();
    }

    @Override
    public boolean isOrHasFunCall() {
        return true;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), name, args);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof FunCall other) {
            return Objects.equals(name, other.name)
                    && Objects.equals(args, other.args)
                    && super.equals(other);
        }
        return false;
    }

    @Override
    public String toString() {
        return super.toString() + "(name=" + name + ", args=" + args + ")";
    }
}
