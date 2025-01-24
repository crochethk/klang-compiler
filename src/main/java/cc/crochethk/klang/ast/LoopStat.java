package cc.crochethk.klang.ast;

import java.util.Objects;

import cc.crochethk.klang.visitor.Visitor;
import utils.SourcePos;

public class LoopStat extends Node {
    public StatementList body;

    public LoopStat(SourcePos srcPos, StatementList body) {
        super(srcPos);
        this.body = body;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName()
                + "(body=" + body + ")";
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), body);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof LoopStat other) {
            return Objects.equals(body, other.body) && super.equals(other);
        }
        return false;
    }
}
