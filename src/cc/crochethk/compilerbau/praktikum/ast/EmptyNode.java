package cc.crochethk.compilerbau.praktikum.ast;

import cc.crochethk.compilerbau.praktikum.Visitor;
import cc.crochethk.compilerbau.praktikum.utils.SourcePos;

public class EmptyNode extends Node {
    public EmptyNode(SourcePos srcPos) {
        this(srcPos.line(), srcPos.column());
    }

    public EmptyNode(int line, int column) {
        super(line, column);
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visit(this);
    }
}