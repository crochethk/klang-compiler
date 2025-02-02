package cc.crochethk.klang.ast;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import cc.crochethk.klang.visitor.Visitor;
import utils.SourcePos;

public class ConstructorCall extends Expr {
    public String structName;
    public List<Expr> args;

    public ConstructorCall(SourcePos srcPos, String structName, List<Expr> args) {
        super(srcPos);
        this.structName = structName;
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
    public String toString() {
        return super.toString()+ "(structName=" + structName + ", args=" + args + ")";
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), structName, args);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof ConstructorCall other) {
            return Objects.equals(structName, other.structName)
                    && Objects.equals(args, other.args)
                    && super.equals(other);
        }
        return false;
    }
}
