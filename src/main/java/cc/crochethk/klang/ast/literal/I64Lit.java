package cc.crochethk.klang.ast.literal;

import cc.crochethk.klang.visitor.Visitor;
import utils.SourcePos;

public class I64Lit extends NumberLiteral<Long> {
    public I64Lit(SourcePos srcPos, long value) {
        super(srcPos, value);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
