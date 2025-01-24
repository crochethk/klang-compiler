package cc.crochethk.klang.ast;

import java.util.Objects;

import cc.crochethk.klang.visitor.Visitor;
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

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), typeToken, isBuiltin);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof TypeNode other) {
            return Objects.equals(typeToken, other.typeToken)
                    && Objects.equals(isBuiltin, other.isBuiltin)
                    && super.equals(other);
        }
        return false;
    }
}
