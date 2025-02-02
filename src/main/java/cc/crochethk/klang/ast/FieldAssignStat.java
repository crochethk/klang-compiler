package cc.crochethk.klang.ast;

import java.util.Objects;

import cc.crochethk.klang.visitor.Visitor;
import utils.SourcePos;

public class FieldAssignStat extends Node {
    public MemberAccessChain maChain;
    public Expr expr;

    public FieldAssignStat(SourcePos srcPos, MemberAccessChain memberAccessChain, Expr expr) {
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
        return super.toString() + "(maChain=" + maChain + ", expr=" + expr + ")";
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
