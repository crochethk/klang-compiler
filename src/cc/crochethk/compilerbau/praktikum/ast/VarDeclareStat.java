package cc.crochethk.compilerbau.praktikum.ast;

import cc.crochethk.compilerbau.praktikum.Visitor;
import cc.crochethk.compilerbau.praktikum.ast.types.Type;

public class VarDeclareStat extends Node {
    public String varName;
    public Type declaredType;

    public VarDeclareStat(int line, int column, String varName, Type declaredType) {
        super(line, column);
        this.varName = varName;
        this.declaredType = declaredType;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visit(this);
    }
}
