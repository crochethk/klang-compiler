package cc.crochethk.klang.ast;

import java.util.Objects;

import cc.crochethk.klang.visitor.Visitor;
import utils.SourcePos;

public class TypeNode extends Node {
    public String typeToken;
    // public String packageToken;

    public TypeNode(SourcePos srcPos, String typeToken) {
        super(srcPos);
        this.typeToken = typeToken;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return super.toString() + "(typeToken=" + typeToken + ")";
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), typeToken);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof TypeNode other) {
            return Objects.equals(typeToken, other.typeToken) && super.equals(other);
        }
        return false;
    }
}
