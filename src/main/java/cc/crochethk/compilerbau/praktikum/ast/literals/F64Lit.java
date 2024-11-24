package cc.crochethk.compilerbau.praktikum.ast.literals;

import cc.crochethk.compilerbau.praktikum.Visitor;
import cc.crochethk.compilerbau.praktikum.ast.Node;
import utils.SourcePos;

public class F64Lit extends Node {
    /// The value of the literal
    public double value;

    public F64Lit(SourcePos srcPos, double value) {
        super(srcPos);
        this.value = value;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visit(this);
    }
}
