package cc.crochethk.compilerbau.praktikum.ast;

import cc.crochethk.compilerbau.praktikum.visitor.Visitor;
import utils.SourcePos;

public class EmptyNode extends Node {
    public EmptyNode(SourcePos srcPos) {
        super(srcPos);
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof EmptyNode other) {
            return super.equals(other);
        }
        return false;
    }
}