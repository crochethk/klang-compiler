package cc.crochethk.compilerbau.praktikum;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import cc.crochethk.compilerbau.praktikum.ast.BinOpExpr;
import cc.crochethk.compilerbau.praktikum.ast.EmptyNode;
import cc.crochethk.compilerbau.praktikum.ast.FunCall;
import cc.crochethk.compilerbau.praktikum.ast.FunDef;
import cc.crochethk.compilerbau.praktikum.ast.IfElseStat;
import cc.crochethk.compilerbau.praktikum.ast.Prog;
import cc.crochethk.compilerbau.praktikum.ast.ReturnStat;
import cc.crochethk.compilerbau.praktikum.ast.StatementList;
import cc.crochethk.compilerbau.praktikum.ast.StatementListNode;
import cc.crochethk.compilerbau.praktikum.ast.TernaryConditionalExpr;
import cc.crochethk.compilerbau.praktikum.ast.TypeNode;
import cc.crochethk.compilerbau.praktikum.ast.UnaryOpExpr;
import cc.crochethk.compilerbau.praktikum.ast.Var;
import cc.crochethk.compilerbau.praktikum.ast.VarAssignStat;
import cc.crochethk.compilerbau.praktikum.ast.VarDeclareStat;
import cc.crochethk.compilerbau.praktikum.ast.BinOpExpr.BinaryOp;
import cc.crochethk.compilerbau.praktikum.ast.literals.*;

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
    public Writer visit(I64Lit i64Lit) {
        return write(Long.toString(i64Lit.value));
    }

    @Override
    public Writer visit(F64Lit f64Lit) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public Writer visit(BoolLit boolLit) {
        var lex = boolLit.value ? BoolLit.TRUE_LEX : BoolLit.FALSE_LEX;
        return write(lex);
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
    public Writer visit(VarDeclareStat varDeclareStat) {
        write("let ");
        write(varDeclareStat.varName);
        write(": ");
        varDeclareStat.declaredType.accept(this);
        return write(";");
    }

    @Override
    public Writer visit(VarAssignStat varAssignStat) {
        write(varAssignStat.targetVarName);
        write(" = ");
        varAssignStat.expr.accept(this);
        return write(";");
    }

    @Override
    public Writer visit(IfElseStat ifElseStat) {
        write("if ");
        ifElseStat.condition.accept(this);
        write(" {");
        if (!ifElseStat.then.isEmpty()) {
            indent_level++;
            write_indent();
            ifElseStat.then.accept(this);
            indent_level--;
            write_indent();
        }
        write("}");
        write(" else {");

        if (!ifElseStat.otherwise.isEmpty()) {
            indent_level++;
            write_indent();
            ifElseStat.otherwise.accept(this);
            indent_level--;
            write_indent();
        }
        write("}");
        return writer;
    }

    @Override
    public Writer visit(StatementListNode statementListNode) {
        // TODO finally remove this from interface 
        throw new UnsupportedOperationException("statementListNode SHOULD NOT BE IN USE ANYMORE...");
    }

    @Override
    public Writer visit(StatementList statementList) {
        var statList = statementList.statements;
        for (int i = 0; i < statList.size(); i++) {
            var s = statList.get(i);
            s.accept(this);
            if (!s.isEmpty() && i < statList.size() - 1)
                write_indent();
        }
        return writer;
    }

    @Override
    public Writer visit(ReturnStat returnStat) {
        write("return");
        if (!returnStat.isEmpty()) {
            write(" ");
        }
        returnStat.expr.accept(this);
        return write(";");
    }

    @Override
    public Writer visit(TypeNode type) {
        return write(type.typeToken);
    }

    @Override
    public Writer visit(FunDef funDef) {
        // Signature
        write(FunDef.KW_FUN_LEX);
        write(" ");
        write(funDef.name);
        write("(");

        for (var p : funDef.params) {
            write(p.name());
            write(": ");
            p.type().accept(this);
            write(", ");
        }
        write("): ");
        funDef.returnType.accept(this);

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
