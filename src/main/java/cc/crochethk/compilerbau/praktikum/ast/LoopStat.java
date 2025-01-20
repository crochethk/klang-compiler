package cc.crochethk.compilerbau.praktikum.ast;

import cc.crochethk.compilerbau.praktikum.visitor.Visitor;
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
}
