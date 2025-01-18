package cc.crochethk.compilerbau.praktikum.ast;

import java.util.Objects;

import cc.crochethk.compilerbau.praktikum.visitor.Visitor;
import utils.SourcePos;

public class FieldAssignStat extends Node {
    public MemberAccessChain maChain;
    public Node expr;

    public FieldAssignStat(SourcePos srcPos, MemberAccessChain memberAccessChain, Node expr) {
        super(srcPos);
        this.maChain = memberAccessChain;
        this.expr = expr;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName()
                + "(maChain=" + maChain + ", expr=" + expr + ")";
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), maChain, expr);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof FieldAssignStat other) {
            return Objects.equals(maChain, other.maChain)
                    && Objects.equals(expr, other.expr)
                    && super.equals(other);
        }
        return false;
    }
}
