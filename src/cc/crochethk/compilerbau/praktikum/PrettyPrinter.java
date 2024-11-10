package cc.crochethk.compilerbau.praktikum;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import cc.crochethk.compilerbau.praktikum.ast.BinOpExpr;
import cc.crochethk.compilerbau.praktikum.ast.BooleanLit;
import cc.crochethk.compilerbau.praktikum.ast.EmptyNode;
import cc.crochethk.compilerbau.praktikum.ast.FunCall;
import cc.crochethk.compilerbau.praktikum.ast.FunDef;
import cc.crochethk.compilerbau.praktikum.ast.IfElseStat;
import cc.crochethk.compilerbau.praktikum.ast.IntLit;
import cc.crochethk.compilerbau.praktikum.ast.Prog;
import cc.crochethk.compilerbau.praktikum.ast.ReturnStat;
import cc.crochethk.compilerbau.praktikum.ast.StatementListNode;
import cc.crochethk.compilerbau.praktikum.ast.TernaryConditionalExpr;
import cc.crochethk.compilerbau.praktikum.ast.UnaryOpExpr;
import cc.crochethk.compilerbau.praktikum.ast.Var;
import cc.crochethk.compilerbau.praktikum.ast.VarAssignStat;
import cc.crochethk.compilerbau.praktikum.ast.VarDeclareStat;
import cc.crochethk.compilerbau.praktikum.ast.BinOpExpr.BinaryOp;

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
        binOpExpr.lhs.accept(this);

        if (binOpExpr.op != BinaryOp.pow)
            write(" ");
        write(binOpExpr.op.toLexeme());
        if (binOpExpr.op != BinaryOp.pow)
            write(" ");

        binOpExpr.rhs.accept(this);
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
        for (int i = 0; i < funDef.params.size(); i++) {
            var p = funDef.params.get(i);
            write(p.name());
            write(": ");
            write(p.type());
            if (i < funDef.params.size() - 1)
                write(", ");
        }
        write("): ");
        write(funDef.returnType);

        // Body
        write(" {");
        indent_level++;
        write_indent();
        funDef.body.accept(this);
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
            def.accept(this);
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
        for (int i = 0; i < funCall.args.size(); i++) {
            funCall.args.get(i).accept(this);
            if (i < funCall.args.size() - 1)
                write(", ");
        }
        return write(")");
    }

    @Override
    public Writer visit(ReturnStat returnStat) {
        write("return");
        if (!returnStat.expr.isEmpty()) {
            write(" ");
            returnStat.expr.accept(this);
        }
        return writer;
    }

    @Override
    public Writer visit(UnaryOpExpr unaryOpExpr) {
        var op_lex = unaryOpExpr.op.toLexeme();
        return switch (unaryOpExpr.op.side) {
            case left -> {
                write(op_lex);
                yield unaryOpExpr.operand.accept(this);
            }
            case right -> {
                unaryOpExpr.operand.accept(this);
                yield write(op_lex);
            }
        };
    }

    @Override
    public Writer visit(TernaryConditionalExpr ternaryConditionalExpr) {
        ternaryConditionalExpr.condition.accept(this);
        write(" ? ");
        ternaryConditionalExpr.then.accept(this);
        write(" : ");
        return ternaryConditionalExpr.otherwise.accept(this);
    }

    @Override
    public Writer visit(VarAssignStat varAssignStat) {
        write(varAssignStat.targetVarName);
        write(" = ");
        return varAssignStat.expr.accept(this);
    }

    @Override
    public Writer visit(VarDeclareStat varDeclareStat) {
        write("let ");
        write(varDeclareStat.varName);
        write(": ");
        return write(varDeclareStat.declaredType);
    }

    @Override
    public Writer visit(StatementListNode statementListNode) {
        statementListNode.value.accept(this);
        write(";");
        if (!statementListNode.next.isEmpty()) {
            write_indent();
            statementListNode.next.accept(this);
        }
        return writer;
    }

    @Override
    public Writer visit(IfElseStat ifElseStat) {
        write("if ");
        ifElseStat.condition.accept(this);
        write(" {");
        indent_level++;
        write_indent();
        ifElseStat.then.accept(this);
        indent_level--;
        write_indent();
        write("}");

        if (!ifElseStat.otherwise.isEmpty()) {
            write(" else {");
            indent_level++;
            write_indent();
            ifElseStat.otherwise.accept(this);
            indent_level--;
            write_indent();
            write("}");
        }
        write_indent();
        return writer;
    }

    @Override
    public Writer visit(EmptyNode emptyNode) {
        return writer;
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
