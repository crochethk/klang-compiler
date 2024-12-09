package cc.crochethk.compilerbau.praktikum.ast;

import cc.crochethk.compilerbau.praktikum.visitor.Visitor;
import utils.SourcePos;

public class VarAssignStat extends Node {
    public String targetVarName;
    public Node expr;

    public VarAssignStat(SourcePos srcPos, String targetVarName, Node expr) {
        super(srcPos);
        this.targetVarName = targetVarName;
        this.expr = expr;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visit(this);
    }
}
