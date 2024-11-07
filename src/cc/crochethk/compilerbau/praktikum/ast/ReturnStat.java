package cc.crochethk.compilerbau.praktikum.ast;

import cc.crochethk.compilerbau.praktikum.Visitor;

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
