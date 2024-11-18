package cc.crochethk.compilerbau.praktikum.ast;

import cc.crochethk.compilerbau.praktikum.Visitor;

public class VarDeclareStat extends Node {
    public String varName;
    public TypeNode declaredType;

    public VarDeclareStat(int line, int column, String varName, TypeNode declaredType) {
        super(line, column);
        this.varName = varName;
        this.declaredType = declaredType;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visit(this);
    }
}
