package cc.crochethk.compilerbau.praktikum.ast;

import cc.crochethk.compilerbau.praktikum.Visitor;
import cc.crochethk.compilerbau.praktikum.utils.SourcePos;

public class IntLit extends Node {
    /// The value of the integer literal
    public long value;

    public IntLit(SourcePos srcPos, int value) {
        this(srcPos.line(), srcPos.column(), value);
    }

    public IntLit(int line, int col, int value) {
        super(line, col);
        this.value = value;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visit(this);
    }
}
