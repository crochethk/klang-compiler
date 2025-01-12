package cc.crochethk.compilerbau.praktikum.ast;

import cc.crochethk.compilerbau.praktikum.visitor.Visitor;
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
}
