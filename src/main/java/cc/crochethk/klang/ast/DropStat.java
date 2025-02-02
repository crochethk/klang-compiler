package cc.crochethk.klang.ast;

import java.util.Objects;

import cc.crochethk.klang.visitor.Visitor;
import utils.SourcePos;

public class DropStat extends Node {
    public Var refTypeVar;

    public DropStat(SourcePos srcPos, Var namedInstance) {
        super(srcPos);
        this.refTypeVar = namedInstance;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), refTypeVar);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof DropStat other) {
            return Objects.equals(refTypeVar, other.refTypeVar) && super.equals(other);
        }
        return false;
    }

    @Override
    public String toString() {
        return super.toString() + "(refTypeVar=" + refTypeVar + ")";
    }
}
