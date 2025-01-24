package cc.crochethk.klang.ast.literal;

import cc.crochethk.klang.visitor.Visitor;
import utils.SourcePos;

public class StringLit extends LiteralExpr<String> {
    public StringLit(SourcePos srcPos, String value) {
        super(srcPos, value);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
