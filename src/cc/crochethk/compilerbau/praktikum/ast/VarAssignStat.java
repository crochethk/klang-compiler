package cc.crochethk.compilerbau.praktikum.ast;

import cc.crochethk.compilerbau.praktikum.Visitor;
import cc.crochethk.compilerbau.praktikum.utils.SourcePos;

public class VarAssignStat extends Node {
    public String targetVarName;
    public Node expr;

    public VarAssignStat(SourcePos srcPos, String targetVarName, Node expr) {
        this(srcPos.line(), srcPos.column(), targetVarName, expr);
    }

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
