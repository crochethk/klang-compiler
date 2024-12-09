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
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visit(this);
    }
}