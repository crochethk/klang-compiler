package cc.crochethk.compilerbau.praktikum.ast;

import cc.crochethk.compilerbau.praktikum.visitor.Visitor;
import utils.SourcePos;

public class TypeNode extends Node {
    public String typeToken;
    // public String packageToken;
    public boolean isBuiltin;

    public TypeNode(SourcePos srcPos, String typeToken, boolean isBuiltinType) {
        super(srcPos);
        this.typeToken = typeToken;
        this.isBuiltin = isBuiltinType;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName()
                + "(typeToken=" + typeToken + ", isBuiltin=" + isBuiltin + ")";
    }
}
