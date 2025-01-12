package cc.crochethk.compilerbau.praktikum.ast;

import cc.crochethk.compilerbau.praktikum.visitor.Visitor;
import utils.SourcePos;

public class BreakStat extends Node {
    public BreakStat(SourcePos srcPos) {
        super(srcPos);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
