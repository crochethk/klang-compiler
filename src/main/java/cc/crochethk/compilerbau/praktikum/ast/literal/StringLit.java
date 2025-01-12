package cc.crochethk.compilerbau.praktikum.ast.literal;

import cc.crochethk.compilerbau.praktikum.visitor.Visitor;
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
