package cc.crochethk.compilerbau.p3.ast;

import cc.crochethk.compilerbau.p3.Visitor;

public class ReturnStat extends Node {
    public Node expr;

    public ReturnStat(int line, int column, Node expr) {
        super(line, column);
        this.expr = expr;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visit(this);
    }
}
