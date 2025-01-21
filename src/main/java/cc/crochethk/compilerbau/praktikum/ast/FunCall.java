package cc.crochethk.compilerbau.praktikum.ast;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import cc.crochethk.compilerbau.praktikum.visitor.Visitor;
import utils.SourcePos;

public class FunCall extends Node {
    public String name;
    public List<Node> args;

    public FunCall(SourcePos srcPos, String name, List<Node> args) {
        super(srcPos);
        this.name = name;
        this.args = args != null ? args : Collections.emptyList();
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
        return this.getClass().getSimpleName()
                + "(name=" + name + ", args=" + args + ")";
    }
}
