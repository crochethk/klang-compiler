package cc.crochethk.klang.ast;

import java.util.Objects;

import cc.crochethk.klang.visitor.Visitor;
import utils.SourcePos;

public class ReturnStat extends Node {
    public Node expr;

    public ReturnStat(SourcePos srcPos, Node expr) {
        super(srcPos);
        this.expr = expr;
    }

    @Override
    public boolean isEmpty() {
        return expr.isEmpty();
    }

    @Override
    public boolean returnsControlFlow() {
        return true;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "(expr=" + expr + ")";
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), expr);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof ReturnStat other) {
            return Objects.equals(expr, other.expr) && super.equals(other);
        }
        return false;
    }
}
