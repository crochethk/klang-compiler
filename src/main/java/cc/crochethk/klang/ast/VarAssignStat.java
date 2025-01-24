package cc.crochethk.klang.ast;

import java.util.Objects;

import cc.crochethk.klang.visitor.Visitor;
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

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), targetVarName, expr);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof VarAssignStat other) {
            return Objects.equals(targetVarName, other.targetVarName)
                    && Objects.equals(expr, other.expr)
                    && super.equals(other);
        }
        return false;
    }
}
