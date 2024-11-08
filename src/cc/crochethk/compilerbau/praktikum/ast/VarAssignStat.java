package cc.crochethk.compilerbau.praktikum.ast;

import cc.crochethk.compilerbau.praktikum.Visitor;

public class VarAssignStat extends Node {
    public String targetVar;
    public Node expr;

    public VarAssignStat(int line, int column, String targetVar, Node expr) {
        super(line, column);
        this.targetVar = targetVar;
        this.expr = expr;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visit(this);
    }
}
