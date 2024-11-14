package cc.crochethk.compilerbau.praktikum.ast.types;

import cc.crochethk.compilerbau.praktikum.Visitor;
import cc.crochethk.compilerbau.praktikum.ast.Node;

public sealed abstract class Type extends Node permits I64T, BoolT, VoidT {
    public String typeName;

    public Type(int line, int column, String typeName) {
        super(line, column);
        this.typeName = typeName;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visit(this);
    }
}
