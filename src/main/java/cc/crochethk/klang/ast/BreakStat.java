package cc.crochethk.klang.ast;

import cc.crochethk.klang.visitor.Visitor;
import utils.SourcePos;

public class BreakStat extends Node {
    public BreakStat(SourcePos srcPos) {
        super(srcPos);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof BreakStat other) {
            return super.equals(other);
        }
        return false;
    }
}
