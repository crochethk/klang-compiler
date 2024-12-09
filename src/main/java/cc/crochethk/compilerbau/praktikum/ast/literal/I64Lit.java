package cc.crochethk.compilerbau.praktikum.ast.literal;

import cc.crochethk.compilerbau.praktikum.Visitor;
import utils.SourcePos;

public class I64Lit extends NumberLiteral<Long> {
    public I64Lit(SourcePos srcPos, long value, boolean hasTypeAnnotation) {
        super(srcPos, value, hasTypeAnnotation);
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visit(this);
    }
}
