package cc.crochethk.compilerbau.praktikum.ast;

import cc.crochethk.compilerbau.praktikum.Visitor;
import utils.SourcePos;

public class TypeNode extends Node {
    public String typeToken;
    // public String packageToken;
    public boolean isPrimitive;

    public TypeNode(SourcePos srcPos, String typeToken, boolean isPrimitiveType) {
        super(srcPos);
        this.typeToken = typeToken;
        this.isPrimitive = isPrimitiveType;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visit(this);
    }
}
