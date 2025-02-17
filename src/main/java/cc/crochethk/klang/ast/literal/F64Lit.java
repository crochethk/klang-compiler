package cc.crochethk.klang.ast.literal;

import cc.crochethk.klang.visitor.Visitor;
import utils.SourcePos;

public class F64Lit extends NumberLiteral<Double> {
    public F64Lit(SourcePos srcPos, double value) {
        super(srcPos, value);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
