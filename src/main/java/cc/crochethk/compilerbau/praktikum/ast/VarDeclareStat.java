package cc.crochethk.compilerbau.praktikum.ast;

import cc.crochethk.compilerbau.praktikum.visitor.Visitor;
import utils.SourcePos;

public class VarDeclareStat extends Node {
    public String varName;
    public TypeNode declaredType;

    public VarDeclareStat(SourcePos srcPos, String varName, TypeNode declaredType) {
        super(srcPos);
        this.varName = varName;
        this.declaredType = declaredType;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visit(this);
    }
}
