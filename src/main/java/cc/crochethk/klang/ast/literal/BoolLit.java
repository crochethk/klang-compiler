package cc.crochethk.klang.ast.literal;

import cc.crochethk.klang.visitor.Visitor;
import utils.SourcePos;

public class BoolLit extends LiteralExpr<Boolean> {
    // TODO hardcode these in prettyprinter, since it is and will only be used there
    public static final String TRUE_LEX = "true";
    public static final String FALSE_LEX = "false";

    public BoolLit(SourcePos srcPos, boolean value) {
        super(srcPos, value);
    }

    // Boilerplate code for the Visitor pattern
    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
