package cc.crochethk.compilerbau.praktikum;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import cc.crochethk.compilerbau.praktikum.ast.BinOpExpr.BinaryOp;
import cc.crochethk.compilerbau.praktikum.ast.*;
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
        write(Long.toString(i64Lit.value));
        // TODO this could go into NumberLiteral base class
        if (i64Lit.hasTypeAnnotation)
            write(" as i64");
        return writer;
    }

    @Override
    public Writer visit(F64Lit f64Lit) {
        write(Double.toString(f64Lit.value));
        if (f64Lit.hasTypeAnnotation)
            write(" as f64");
        return writer;
    }

    @Override
    public Writer visit(BoolLit boolLit) {
        var lex = boolLit.value ? BoolLit.TRUE_LEX : BoolLit.FALSE_LEX;
        return write(lex);
    }

    @Override
    public Writer visit(StringLit stringLit) {
        write("\"");
        write(stringLit.value);
        return write("\"");
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
        write("(");
        var op_lex = unaryOpExpr.op.toLexeme();
        switch (unaryOpExpr.op.side) {
            case left -> {
                write(op_lex);
                unaryOpExpr.operand.accept(this);
            }
            case right -> {
                unaryOpExpr.operand.accept(this);
                write(op_lex);
            }
        }
        return write(")");
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
        return writeSemi();
    }

    @Override
    public Writer visit(VarAssignStat varAssignStat) {
        write(varAssignStat.targetVarName);
        write(" = ");
        varAssignStat.expr.accept(this);
        return writeSemi();
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
        return write("}");
    }

    @Override
    public Writer visit(LoopStat loopStat) {
        write("loop");
        write(" {");
        if (!loopStat.body.isEmpty()) {
            indent_level++;
            write_indent();
            loopStat.body.accept(this);
            indent_level--;
            write_indent();
        }
        return write("}");
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
        return writeSemi();
    }

    @Override
    public Writer visit(BreakStat breakStat) {
        write("break");
        return writeSemi();
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
        write(")");
        if (!funDef.returnType.typeToken.equals("void")) {
            write(" -> ");
            funDef.returnType.accept(this);
        }

        // Body
        write(" {");
        if (!funDef.body.isEmpty()) {
            indent_level++;
            write_indent();
            funDef.body.accept(this);
            indent_level--;
            write_indent();
        }
        write("}");
        write_indent();
        return writer;
    }

    @Override
    public Writer visit(Prog prog) {
        for (int i = 0; i < prog.funDefs.size(); i++) {
            prog.funDefs.get(i).accept(this);
            if (i < prog.funDefs.size() - 1)
                write_indent();
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

    private Writer writeSemi() {
        return write(";");
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
