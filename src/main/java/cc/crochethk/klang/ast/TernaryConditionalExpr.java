package cc.crochethk.klang.ast;

import java.util.Objects;

import cc.crochethk.klang.visitor.Visitor;
import utils.SourcePos;

public class TernaryConditionalExpr extends Expr {
    public Expr condition;
    public Expr then, otherwise;

    public TernaryConditionalExpr(SourcePos srcPos, Expr condition, Expr then, Expr otherwise) {
        super(srcPos);
        this.condition = condition;
        this.then = then;
        this.otherwise = otherwise;
    }

    @Override
    public boolean isOrHasFunCall() {
        return condition.isOrHasFunCall()
                || then.isOrHasFunCall()
                || otherwise.isOrHasFunCall();
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "(condition=" + condition
                + ", then=" + then + ", otherwise=" + otherwise + ")";
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), condition, then, otherwise);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof TernaryConditionalExpr other) {
            return Objects.equals(condition, other.condition)
                    && Objects.equals(otherwise, other.otherwise)
                    && Objects.equals(then, other.then)
                    && super.equals(other);
        }
        return false;
    }
}
