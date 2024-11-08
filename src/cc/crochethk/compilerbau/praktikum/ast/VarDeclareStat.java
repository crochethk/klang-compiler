package cc.crochethk.compilerbau.praktikum.ast;

import cc.crochethk.compilerbau.praktikum.Visitor;

public class VarDeclareStat extends Node {
    public String name;
    public String declaredType;

    public VarDeclareStat(int line, int column, String name, String declaredType) {
        super(line, column);
        this.name = name;
        this.declaredType = declaredType;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visit(this);
    }
}
