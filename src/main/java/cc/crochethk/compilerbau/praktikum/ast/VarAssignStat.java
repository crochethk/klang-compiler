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
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName()
                + "(targetVarName=" + targetVarName + ", expr=" + expr + ")";
    }
}
