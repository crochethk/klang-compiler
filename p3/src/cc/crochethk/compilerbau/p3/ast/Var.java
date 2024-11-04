package cc.crochethk.compilerbau.p3.ast;

import cc.crochethk.compilerbau.p3.Visitor;

public class Var extends Node {
    public String name;

    public Var(int line, int column, String name) {
        super(line, column);
        this.name = name;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) throws Exception {
        return visitor.visit(this);
    }
}
