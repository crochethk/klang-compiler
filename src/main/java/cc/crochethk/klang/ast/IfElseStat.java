package cc.crochethk.klang.ast;

import java.util.Objects;

import cc.crochethk.klang.visitor.Visitor;
import utils.SourcePos;

public class IfElseStat extends Node {
    public Node condition;
    public StatementList then, otherwise;

    public IfElseStat(SourcePos srcPos, Node condition, StatementList then, StatementList otherwise) {
        super(srcPos);
        this.condition = condition;
        this.then = then;
        this.otherwise = otherwise;
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
        return Objects.hash(super.hashCode(), condition, otherwise, then);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof IfElseStat other) {
            return Objects.equals(condition, other.condition)
                    && Objects.equals(then, other.then)
                    && Objects.equals(otherwise, other.otherwise)
                    && super.equals(other);
        }
        return false;
    }
}
