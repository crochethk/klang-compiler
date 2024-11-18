package cc.crochethk.compilerbau.praktikum.ast;

import cc.crochethk.compilerbau.praktikum.Visitor;
import cc.crochethk.compilerbau.praktikum.utils.SourcePos;

public class TypeNode extends Node {
    public String typeToken;
    // public String packageToken;
    public boolean isPrimitive;

    public TypeNode(SourcePos srcPos, String typeToken, boolean isPrimitiveType) {
        this(srcPos.line(), srcPos.column(), typeToken, isPrimitiveType);
    }

    public TypeNode(int line, int column, String typeToken, boolean isPrimitiveType) {
        super(line, column);
        this.typeToken = typeToken;
        this.isPrimitive = isPrimitiveType;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visit(this);
    }
}
