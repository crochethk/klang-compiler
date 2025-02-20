package cc.crochethk.klang.visitor;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;
import java.util.stream.Stream;

import cc.crochethk.klang.ast.BinOpExpr.BinaryOp;
import cc.crochethk.klang.ast.MemberAccess.*;
import cc.crochethk.klang.ast.literal.*;
import cc.crochethk.klang.StringEscapeUtils;
import cc.crochethk.klang.ast.*;

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
    }

    @Override
    public void visit(F64Lit f64Lit) {
        scb.write(Double.toString(f64Lit.value));
    }

    @Override
    public void visit(BoolLit boolLit) {
        var lex = boolLit.value ? "true" : "false";
        scb.write(lex);
    }

    @Override
    public void visit(StringLit stringLit) {
        var escapedValue = StringEscapeUtils.unresolveEscapeSequences(stringLit.value);
        scb.write("\"", escapedValue, "\"");
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

    private void writeArgsList(SourceCodeBuilder scb, List<Expr> args) {
        for (int i = 0; i < args.size(); i++) {
            args.get(i).accept(this);
            if (i < args.size() - 1)
                scb.write(", ");
        }
    }

    @Override
    public void visit(MemberAccessChain maChain) {
        maChain.owner.accept(this);
        maChain.chain.accept(this);
    }

    @Override
    public void visit(MemberAccess memberAccess) {
        scb.write(".", memberAccess.targetName);
        if (memberAccess.next != null) {
            memberAccess.next.accept(this);
        }
    }

    @Override
    public void visit(MethodCall methodCall) {
        visit((MemberAccess) methodCall);
        scb.write("(");
        writeArgsList(scb, methodCall.args);
        scb.write(")");
    }

    @Override
    public void visit(FieldGet fieldGet) {
        visit((MemberAccess) fieldGet);
    }

    @Override
    public void visit(FieldSet fieldSet) {
        visit((MemberAccess) fieldSet);
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
    public void visit(TypeCast typeCast) {
        typeCast.expr.accept(this);
        scb.write(" as ");
        typeCast.targetType.accept(this);
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
        scb.write("let ", varDeclareStat.varName());
        varDeclareStat.declaredType.ifPresent(t -> {
            scb.write(": ");
            t.accept(this);
        });
        varDeclareStat.initializer.ifPresent(init -> {
            scb.write(" = ");
            init.expr.accept(this);
        });
        scb.write(";");
    }

    @Override
    public void visit(VarAssignStat varAssignStat) {
        scb.write(varAssignStat.targetVarName, " = ");
        varAssignStat.expr.accept(this);
        scb.write(";");
    }

    @Override
    public void visit(FieldAssignStat fieldAssignStat) {
        fieldAssignStat.maChain.accept(this);
        scb.write(" = ");
        fieldAssignStat.expr.accept(this);
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
    public void visit(DropStat dropStat) {
        scb.write("drop ");
        dropStat.refTypeVar.accept(this);
        scb.write(";");
    }

    @Override
    public void visit(VoidResultExprStat voidResultExprStat) {
        voidResultExprStat.expr.accept(this);
        scb.write(";");
    }

    @Override
    public void visit(TypeNode type) {
        scb.write(type.typeToken);
    }

    @Override
    public void visit(FunDef funDef) {
        scb.write("fn ");
        writeFunDefNoKeyword(funDef, funDef.params);
    }

    /** Writes funDef using the provided parameter list instead of the funDef's. */
    private void writeFunDefNoKeyword(FunDef funDef, List<Parameter> params) {
        // Signature
        scb.write(funDef.name, "(");
        for (var paramsIter = params.iterator(); paramsIter.hasNext();) {
            var p = paramsIter.next();
            scb.write(p.name(), ": ");
            p.type().accept(this);
            if (paramsIter.hasNext())
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
    }

    @Override
    public void visit(StructDef structDef) {
        scb.write("struct ", structDef.name, " {");
        if (!structDef.isEmpty()) {
            scb.increaseIndent();
            for (var f : structDef.fields) {
                scb.writeIndent();
                scb.write(f.name(), ": ");
                f.type().accept(this);
                scb.write(",");
            }

            var methsIter = structDef.methods.iterator();
            if (methsIter.hasNext())
                scb.writeIndented("---");
            for (; methsIter.hasNext();) {
                scb.writeIndent();
                methsIter.next().accept(this);
                if (methsIter.hasNext())
                    scb.writeIndent();
            }
            scb.decreaseIndent();
            scb.writeIndent();
        }
        scb.write("}");
    }

    @Override
    public void visit(MethDef methDef) {
        // Remove implicit 'self' parameter
        var params = methDef.def.params.stream().skip(1).toList();
        writeFunDefNoKeyword(methDef.def, params);
    }

    @Override
    public void visit(Prog prog) {
        var allDefs = Stream.concat(
                prog.structDefs.stream().map(def -> (Node) def),
                prog.funDefs.stream().map(def -> (Node) def) //
        );

        for (var defsIter = allDefs.iterator(); defsIter.hasNext();) {
            defsIter.next().accept(this);
            scb.writeIndent();
            if (defsIter.hasNext())
                scb.writeIndent();
        }

        try {
            writer.write(scb.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void visit(EmptyExpr emptyExpr) {
        return;
    }
}
