package cc.crochethk.compilerbau.praktikum.ast.literal;

import cc.crochethk.compilerbau.praktikum.Visitor;
import utils.SourcePos;

public class F64Lit extends NumberLiteral<Double> {
    public F64Lit(SourcePos srcPos, double value, boolean hasTypeAnnotation) {
        super(srcPos, value, hasTypeAnnotation);
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visit(this);
    }
}
