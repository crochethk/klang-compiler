package cc.crochethk.compilerbau.praktikum.visitor;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;

import cc.crochethk.compilerbau.praktikum.ast.BinOpExpr.BinaryOp;
import cc.crochethk.compilerbau.praktikum.ast.literal.*;
import cc.crochethk.compilerbau.praktikum.ast.*;

public class PrettyPrinter implements Visitor<Void> {
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
    public Void visit(I64Lit i64Lit) {
        scb.write(Long.toString(i64Lit.value));
        if (i64Lit.hasTypeAnnotation)
            scb.write(" as i64");
        return null;
    }

    @Override
    public Void visit(F64Lit f64Lit) {
        scb.write(Double.toString(f64Lit.value));
        if (f64Lit.hasTypeAnnotation)
            scb.write(" as f64");
        return null;
    }

    @Override
    public Void visit(BoolLit boolLit) {
        var lex = boolLit.value ? BoolLit.TRUE_LEX : BoolLit.FALSE_LEX;
        scb.write(lex);
        return null;
    }

    @Override
    public Void visit(StringLit stringLit) {
        scb.write("\"", stringLit.value, "\"");
        return null;
    }

    @Override
    public Void visit(NullLit nullLit) {
        scb.write("null");
        return null;
    }

    @Override
    public Void visit(Var var) {
        scb.write(var.name);
        return null;
    }

    @Override
    public Void visit(FunCall funCall) {
        scb.write(funCall.name, "(");
        writeArgsList(scb, funCall.args);
        scb.write(")");
        return null;
    }

    private void writeArgsList(SourceCodeBuilder scb, List<Node> args) {
        for (int i = 0; i < args.size(); i++) {
            args.get(i).accept(this);
            if (i < args.size() - 1)
                scb.write(", ");
        }
    }

    @Override
    public Void visit(ConstructorCall constructorCall) {
        var cc = constructorCall;
        scb.write(cc.structName, "{");
        writeArgsList(scb, cc.args);
        scb.write("}");
        return null;
    }

    @Override
    public Void visit(BinOpExpr binOpExpr) {
        scb.write("(");
        binOpExpr.lhs.accept(this);

        if (binOpExpr.op != BinaryOp.pow)
            scb.write(" ");
        scb.write(binOpExpr.op.toLexeme());
        if (binOpExpr.op != BinaryOp.pow)
            scb.write(" ");

        binOpExpr.rhs.accept(this);
        scb.write(")");
        return null;
    }

    @Override
    public Void visit(UnaryOpExpr unaryOpExpr) {
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
        return null;
    }

    @Override
    public Void visit(TernaryConditionalExpr ternaryConditionalExpr) {
        ternaryConditionalExpr.condition.accept(this);
        scb.write(" ? ");
        ternaryConditionalExpr.then.accept(this);
        scb.write(" : ");
        return ternaryConditionalExpr.otherwise.accept(this);
    }

    @Override
    public Void visit(VarDeclareStat varDeclareStat) {
        scb.write("let ", varDeclareStat.varName, ": ");
        varDeclareStat.declaredType.accept(this);
        scb.write(";");
        return null;
    }

    @Override
    public Void visit(VarAssignStat varAssignStat) {
        scb.write(varAssignStat.targetVarName, " = ");
        varAssignStat.expr.accept(this);
        scb.write(";");
        return null;
    }

    @Override
    public Void visit(IfElseStat ifElseStat) {
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
        return null;
    }

    @Override
    public Void visit(LoopStat loopStat) {
        scb.write("loop {");
        if (!loopStat.body.isEmpty()) {
            scb.increaseIndent();
            scb.writeIndent();
            loopStat.body.accept(this);
            scb.decreaseIndent();
            scb.writeIndent();
        }
        scb.write("}");
        return null;
    }

    @Override
    public Void visit(StatementList statementList) {
        var statList = statementList.statements;
        for (int i = 0; i < statList.size(); i++) {
            var s = statList.get(i);
            s.accept(this);
            if (!s.isEmpty() && i < statList.size() - 1)
                scb.writeIndent();
        }
        return null;
    }

    @Override
    public Void visit(ReturnStat returnStat) {
        scb.write("return");
        if (!returnStat.isEmpty()) {
            scb.write(" ");
        }
        returnStat.expr.accept(this);
        scb.write(";");
        return null;
    }

    @Override
    public Void visit(BreakStat breakStat) {
        scb.write("break");
        scb.write(";");
        return null;
    }

    @Override
    public Void visit(TypeNode type) {
        scb.write(type.typeToken);
        return null;
    }

    @Override
    public Void visit(FunDef funDef) {
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
        return null;
    }

    @Override
    public Void visit(StructDef structDef) {
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
        return null;
    }

    @Override
    public Void visit(Prog prog) {
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
        return null;
    }

    @Override
    public Void visit(EmptyNode emptyNode) {
        return null;
    }
}
