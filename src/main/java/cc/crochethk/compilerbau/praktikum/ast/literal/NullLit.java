package cc.crochethk.compilerbau.praktikum.ast.literal;

import cc.crochethk.compilerbau.praktikum.visitor.Visitor;
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
