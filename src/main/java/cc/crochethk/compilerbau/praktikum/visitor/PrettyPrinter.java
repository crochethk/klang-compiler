package cc.crochethk.compilerbau.praktikum.visitor;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;

import cc.crochethk.compilerbau.praktikum.ast.BinOpExpr.BinaryOp;
import cc.crochethk.compilerbau.praktikum.ast.literal.*;
import cc.crochethk.compilerbau.praktikum.ast.*;

public class PrettyPrinter implements Visitor {
    public Writer writer;
    SourceCodeBuilder scb;

    public PrettyPrinter(Writer out) {
        this.writer = out;
        scb = new SourceCodeBuilder("  ", 0);
    }

    public PrettyPrinter() {
        this(new StringWriter());
    }

    @Override
    public void visit(I64Lit i64Lit) {
        scb.write(Long.toString(i64Lit.value));
        if (i64Lit.hasTypeAnnotation)
            scb.write(" as i64");
    }

    @Override
    public void visit(F64Lit f64Lit) {
        scb.write(Double.toString(f64Lit.value));
        if (f64Lit.hasTypeAnnotation)
            scb.write(" as f64");
    }

    @Override
    public void visit(BoolLit boolLit) {
        var lex = boolLit.value ? BoolLit.TRUE_LEX : BoolLit.FALSE_LEX;
        scb.write(lex);
    }

    @Override
    public void visit(StringLit stringLit) {
        scb.write("\"", stringLit.value, "\"");
    }

    @Override
    public void visit(NullLit nullLit) {
        scb.write("null");
    }

    @Override
    public void visit(Var var) {
        scb.write(var.name);
    }

    @Override
    public void visit(FunCall funCall) {
        scb.write(funCall.name, "(");
        writeArgsList(scb, funCall.args);
        scb.write(")");
    }

    private void writeArgsList(SourceCodeBuilder scb, List<Node> args) {
        for (int i = 0; i < args.size(); i++) {
            args.get(i).accept(this);
            if (i < args.size() - 1)
                scb.write(", ");
        }
    }

    @Override
    public void visit(ConstructorCall constructorCall) {
        var cc = constructorCall;
        scb.write(cc.structName, "{");
        writeArgsList(scb, cc.args);
        scb.write("}");
    }

    @Override
    public void visit(BinOpExpr binOpExpr) {
        scb.write("(");
        binOpExpr.lhs.accept(this);

        if (binOpExpr.op != BinaryOp.pow)
            scb.write(" ");
        scb.write(binOpExpr.op.toLexeme());
        if (binOpExpr.op != BinaryOp.pow)
            scb.write(" ");

        binOpExpr.rhs.accept(this);
        scb.write(")");
    }

    @Override
    public void visit(UnaryOpExpr unaryOpExpr) {
        scb.write("(");
        var op_lex = unaryOpExpr.op.toLexeme();
        switch (unaryOpExpr.op.side) {
            case left -> {
                scb.write(op_lex);
                unaryOpExpr.operand.accept(this);
            }
            case right -> {
                unaryOpExpr.operand.accept(this);
                scb.write(op_lex);
            }
        }
        scb.write(")");
    }

    @Override
    public void visit(TernaryConditionalExpr ternaryConditionalExpr) {
        ternaryConditionalExpr.condition.accept(this);
        scb.write(" ? ");
        ternaryConditionalExpr.then.accept(this);
        scb.write(" : ");
        ternaryConditionalExpr.otherwise.accept(this);
    }

    @Override
    public void visit(VarDeclareStat varDeclareStat) {
        scb.write("let ", varDeclareStat.varName, ": ");
        varDeclareStat.declaredType.accept(this);
        scb.write(";");
    }

    @Override
    public void visit(VarAssignStat varAssignStat) {
        scb.write(varAssignStat.targetVarName, " = ");
        varAssignStat.expr.accept(this);
        scb.write(";");
    }

    @Override
    public void visit(IfElseStat ifElseStat) {
        scb.write("if ");
        ifElseStat.condition.accept(this);
        scb.write(" {");
        if (!ifElseStat.then.isEmpty()) {
            scb.increaseIndent();
            scb.writeIndent();
            ifElseStat.then.accept(this);
            scb.decreaseIndent();
            scb.writeIndent();
        }
        scb.write("} else {");

        if (!ifElseStat.otherwise.isEmpty()) {
            scb.increaseIndent();
            scb.writeIndent();
            ifElseStat.otherwise.accept(this);
            scb.decreaseIndent();
            scb.writeIndent();
        }
        scb.write("}");
    }

    @Override
    public void visit(LoopStat loopStat) {
        scb.write("loop {");
        if (!loopStat.body.isEmpty()) {
            scb.increaseIndent();
            scb.writeIndent();
            loopStat.body.accept(this);
            scb.decreaseIndent();
            scb.writeIndent();
        }
        scb.write("}");
    }

    @Override
    public void visit(StatementList statementList) {
        var statList = statementList.statements;
        for (int i = 0; i < statList.size(); i++) {
            var s = statList.get(i);
            s.accept(this);
            if (!s.isEmpty() && i < statList.size() - 1)
                scb.writeIndent();
        }
    }

    @Override
    public void visit(ReturnStat returnStat) {
        scb.write("return");
        if (!returnStat.isEmpty()) {
            scb.write(" ");
        }
        returnStat.expr.accept(this);
        scb.write(";");
    }

    @Override
    public void visit(BreakStat breakStat) {
        scb.write("break");
        scb.write(";");
    }

    @Override
    public void visit(TypeNode type) {
        scb.write(type.typeToken);
    }

    @Override
    public void visit(FunDef funDef) {
        // Signature
        scb.write("fn ", funDef.name, "(");
        for (var p : funDef.params) {
            scb.write(p.name(), ": ");
            p.type().accept(this);
            scb.write(", ");
        }
        scb.write(")");
        if (!funDef.returnType.typeToken.equals("void")) {
            scb.write(" -> ");
            funDef.returnType.accept(this);
        }

        // Body
        scb.write(" {");
        if (!funDef.body.isEmpty()) {
            scb.increaseIndent();
            scb.writeIndent();
            funDef.body.accept(this);
            scb.decreaseIndent();
            scb.writeIndent();
        }
        scb.write("}");
        scb.writeIndent();
    }

    @Override
    public void visit(StructDef structDef) {
        scb.write("struct ", structDef.name, " {");
        if (!structDef.fields.isEmpty()) {
            scb.increaseIndent();
            for (var p : structDef.fields) {
                scb.writeIndent();
                scb.write(p.name(), ": ");
                p.type().accept(this);
                scb.write(",");
            }
            scb.decreaseIndent();
            scb.writeIndent();
        }
        scb.write("}");
        scb.writeIndent();
    }

    @Override
    public void visit(Prog prog) {
        for (int i = 0; i < prog.structDefs.size(); i++) {
            prog.structDefs.get(i).accept(this);
            if (i < prog.structDefs.size() - 1)
                scb.writeIndent();
        }

        for (int i = 0; i < prog.funDefs.size(); i++) {
            prog.funDefs.get(i).accept(this);
            if (i < prog.funDefs.size() - 1)
                scb.writeIndent();
        }

        try {
            writer.write(scb.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void visit(EmptyNode emptyNode) {
        return;
    }
}
