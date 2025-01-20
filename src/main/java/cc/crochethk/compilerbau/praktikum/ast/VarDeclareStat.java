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
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName()
                + "(varName=" + varName + ", declaredType=" + declaredType + ")";
    }
}
