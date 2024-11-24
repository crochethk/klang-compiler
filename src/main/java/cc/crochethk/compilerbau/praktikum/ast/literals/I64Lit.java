package cc.crochethk.compilerbau.praktikum.ast.literals;

import cc.crochethk.compilerbau.praktikum.Visitor;
import cc.crochethk.compilerbau.praktikum.ast.Node;
import utils.SourcePos;

public class I64Lit extends Node {
    /// The value of the integer literal
    public long value;

    public I64Lit(SourcePos srcPos, long value) {
        super(srcPos);
        this.value = value;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visit(this);
    }
}
