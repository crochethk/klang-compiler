package cc.crochethk.klang.ast.literal;

import cc.crochethk.klang.visitor.Visitor;
import utils.SourcePos;

public class NullLit extends LiteralExpr<Object> {
    public NullLit(SourcePos srcPos) {
        super(srcPos, null);
    }

    // Boilerplate code for the Visitor pattern
    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
