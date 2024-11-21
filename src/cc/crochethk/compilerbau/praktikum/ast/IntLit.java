package cc.crochethk.compilerbau.praktikum.ast;

import cc.crochethk.compilerbau.praktikum.Visitor;
import utils.SourcePos;

public class IntLit extends Node {
    /// The value of the integer literal
    public long value;

    public IntLit(SourcePos srcPos, int value) {
        super(srcPos);
        this.value = value;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visit(this);
    }
}
