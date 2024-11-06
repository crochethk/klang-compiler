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
import cc.crochethk.compilerbau.p3.ast.ReturnStat;
import cc.crochethk.compilerbau.p3.ast.Var;
import cc.crochethk.compilerbau.p3.ast.BinOpExpr.BinaryOp;

public class PrettyPrinter implements Visitor<Writer> {
    Writer writer;
    private int indent_level = 0;

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
        write("(");
        var _ = binOpExpr.lhs.accept(this);

        if (binOpExpr.op != BinaryOp.pow)
            write(" ");
        write(binOpExpr.op.toLexeme());
        if (binOpExpr.op != BinaryOp.pow)
            write(" ");

        var _ = binOpExpr.rhs.accept(this);
        write(")");
        return writer;
    }

    @Override
    public Writer visit(FunDef funDef) {
        // Signature
        write(FunDef.KW_FUN_LEX);
        write(" ");
        write(funDef.name);
        write("(");
        if (funDef.params != null) {
            for (int i = 0; i < funDef.params.size(); i++) {
                var p = funDef.params.get(i);
                write(p.name());
                write(": ");
                write(p.type());
                if (i < funDef.params.size() - 1)
                    write(", ");
            }
        }
        write("): ");
        write(funDef.returnType);

        // Body
        write(" {");
        indent_level++;
        write_indent();
        var _ = funDef.statement.accept(this);
        indent_level--;
        write_indent();
        write("}");
        write_indent();
        write_indent();
        return writer;
    }

    @Override
    public Writer visit(Prog prog) {
        for (var def : prog.funDefs) {
            var _ = def.accept(this);
        }
        return writer;
    }

    @Override
    public Writer visit(Var var) {
        return write(var.name);
    }

    @Override
    public Writer visit(FunCall funCall) {
        write(funCall.name);
        write("(");
        if (funCall.args != null) {
            for (int i = 0; i < funCall.args.size(); i++) {
                var _ = funCall.args.get(i).accept(this);
                if (i < funCall.args.size() - 1)
                    write(", ");
            }
        }
        return write(")");
    }

    @Override
    public Writer visit(ReturnStat returnStat) {
        write("return ");
        return returnStat.expr.accept(this);
    }

    private void write_indent() {
        write("\n");
        write("  ".repeat(indent_level));
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
