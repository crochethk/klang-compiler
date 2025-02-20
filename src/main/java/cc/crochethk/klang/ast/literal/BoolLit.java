package cc.crochethk.klang.ast.literal;

import cc.crochethk.klang.visitor.Visitor;
import utils.SourcePos;

public class BoolLit extends LiteralExpr<Boolean> {
    public BoolLit(SourcePos srcPos, boolean value) {
        super(srcPos, value);
    }

    // Boilerplate code for the Visitor pattern
    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
