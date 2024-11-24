package cc.crochethk.compilerbau.praktikum.ast.literals;

import cc.crochethk.compilerbau.praktikum.Visitor;
import utils.SourcePos;

public class F64Lit extends NumberLiteral<Double> {
    public F64Lit(SourcePos srcPos, double value) {
        super(srcPos, value);
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visit(this);
    }
}
