package cc.crochethk.compilerbau.praktikum.ast.literal;

import cc.crochethk.compilerbau.praktikum.visitor.Visitor;
import utils.SourcePos;

public class I64Lit extends NumberLiteral<Long> {
    public I64Lit(SourcePos srcPos, long value, boolean hasTypeAnnotation) {
        super(srcPos, value, hasTypeAnnotation);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
