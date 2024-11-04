package cc.crochethk.compilerbau.p3;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import cc.crochethk.compilerbau.p3.ast.BinOpExpr;
import cc.crochethk.compilerbau.p3.ast.BooleanLit;
import cc.crochethk.compilerbau.p3.ast.FunCall;
import cc.crochethk.compilerbau.p3.ast.FunDef;
import cc.crochethk.compilerbau.p3.ast.IntLit;
import cc.crochethk.compilerbau.p3.ast.Prog;
import cc.crochethk.compilerbau.p3.ast.Var;
import cc.crochethk.compilerbau.p3.ast.BinOpExpr.BinaryOp;

public class PrettyPrinter implements Visitor<Writer> {
    Writer writer;

    public PrettyPrinter(Writer out) {
        this.writer = out;
    }

    public PrettyPrinter() {
        this(new StringWriter());
    }

    @Override
    public Writer visit(IntLit intLit) {
        return write(Long.toString(intLit.value));
    }

    @Override
    public Writer visit(BooleanLit booleanLit) {
        var lex = booleanLit.value ? BooleanLit.TRUE_LEX : BooleanLit.FALSE_LEX;
        return write(lex);
    }

    @Override
    public Writer visit(BinOpExpr binOpExpr) {
        writer.append("(");
        var _ = binOpExpr.lhs.accept(this);

        if (binOpExpr.op != BinaryOp.pow)
            writer.append(" ");
        writer.append(binOpExpr.op.toLexeme());
        if (binOpExpr.op != BinaryOp.pow)
            writer.append(" ");

        var _ = binOpExpr.rhs.accept(this);
        writer.append(")");
        return writer;
    }

    @Override
    public Writer visit(FunDef funDef) {
        // TODO Auto-generated method stub
        return writer;
    }

    @Override
    public Writer visit(Prog prog) {
        // TODO Auto-generated method stub
        return writer;
    }

    @Override
    public Writer visit(Var var) {
        // TODO Auto-generated method stub
        return writer;
    }

    @Override
    public Writer visit(FunCall funCall) {
        // TODO Auto-generated method stub
        return writer;
    }

    private Writer write(String s) {
        try {
            writer.write(s);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return writer;
    }
}
