package cc.crochethk.compilerbau.praktikum.ast;

import cc.crochethk.compilerbau.praktikum.Visitor;

public class TypeNode extends Node {
    public String tokenText;

    public TypeNode(int line, int column, String tokenText) {
        super(line, column);
        this.tokenText = tokenText;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visit(this);
    }
}
