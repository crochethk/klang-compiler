package cc.crochethk.compilerbau.praktikum.ast;

import cc.crochethk.compilerbau.praktikum.Visitor;

public class VarAssignStat extends Node {
    public String targetVarName;
    public Node expr;

    public VarAssignStat(int line, int column, String targetVarName, Node expr) {
        super(line, column);
        this.targetVarName = targetVarName;
        this.expr = expr;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visit(this);
    }
}
