package cc.crochethk.compilerbau.praktikum.ast.literal;

import cc.crochethk.compilerbau.praktikum.visitor.Visitor;
import utils.SourcePos;

public class F64Lit extends NumberLiteral<Double> {
    public F64Lit(SourcePos srcPos, double value, boolean hasTypeAnnotation) {
        super(srcPos, value, hasTypeAnnotation);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
