package cc.crochethk.compilerbau.praktikum.ast.literal;

import cc.crochethk.compilerbau.praktikum.Visitor;
import utils.SourcePos;

public class StringLit extends LiteralExpr<String> {
    public StringLit(SourcePos srcPos, String value) {
        super(srcPos, value);
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visit(this);
    }
}
