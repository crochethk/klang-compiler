package cc.crochethk.compilerbau.praktikum.ast;

import cc.crochethk.compilerbau.praktikum.visitor.Visitor;
import utils.SourcePos;

public class TypeNode extends Node {
    public String typeToken;
    // public String packageToken;

    public TypeNode(SourcePos srcPos, String typeToken) {
        super(srcPos);
        this.typeToken = typeToken;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visit(this);
    }
}
