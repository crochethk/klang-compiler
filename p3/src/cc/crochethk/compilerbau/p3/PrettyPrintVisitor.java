package cc.crochethk.compilerbau.p3;

import cc.crochethk.compilerbau.p3.ast.BinOpExpr;
import cc.crochethk.compilerbau.p3.ast.BooleanLit;
import cc.crochethk.compilerbau.p3.ast.IntLit;
import cc.crochethk.compilerbau.p3.ast.BinOpExpr.BinaryOp;

public class PrettyPrintVisitor implements Visitor<StringBuilder> {
    StringBuilder strbuf;

    public PrettyPrintVisitor(StringBuilder strbuf) {
        this.strbuf = strbuf;
    }

    public PrettyPrintVisitor() {
        this(new StringBuilder());
    }

    @Override
    public StringBuilder visit(IntLit intLit) throws Exception {
        strbuf.append(intLit.getValue());
        return strbuf;
    }

    @Override
    public StringBuilder visit(BooleanLit booleanLit) throws Exception {
        var lex = booleanLit.getValue() ? "true" : "false";
        strbuf.append(lex);
        return strbuf;
    }

    @Override
    public StringBuilder visit(BinOpExpr binOpExpr) throws Exception {
        strbuf.append("(");
        var _ = binOpExpr.lhs.accept(this);

        if (binOpExpr.op != BinaryOp.pow)
            strbuf.append(" ");
        strbuf.append(binOpExpr.op.toLexeme());
        if (binOpExpr.op != BinaryOp.pow)
            strbuf.append(" ");

        var _ = binOpExpr.rhs.accept(this);
        strbuf.append(")");
        return strbuf;
    }
}
