import java.util.List;

import org.antlr.v4.runtime.ParserRuleContext;

import cc.crochethk.compilerbau.p3.ast.BinOpExpr;
import cc.crochethk.compilerbau.p3.ast.BinOpExpr.BinaryOp;
import cc.crochethk.compilerbau.p3.ast.BooleanLit;
import cc.crochethk.compilerbau.p3.ast.FunCall;
import cc.crochethk.compilerbau.p3.ast.FunDef;
import cc.crochethk.compilerbau.p3.ast.FunDef.Parameter;
import cc.crochethk.compilerbau.p3.ast.IntLit;
import cc.crochethk.compilerbau.p3.ast.Node;
import cc.crochethk.compilerbau.p3.ast.Prog;
import cc.crochethk.compilerbau.p3.ast.ReturnStat;
import cc.crochethk.compilerbau.p3.ast.UnaryOpExpr;
import cc.crochethk.compilerbau.p3.ast.UnaryOpExpr.UnaryOp;
import cc.crochethk.compilerbau.p3.ast.Var;

public class TreeBuilder extends L1BaseListener {
    @Override
    public void exitZahl(L1Parser.ZahlContext ctx) {
        var srcPos = getSourcePos(ctx);
        var node = new IntLit(
                srcPos.line(), srcPos.column(), Integer.parseInt(ctx.NUMBER().getText()));
        ctx.result = node;
    }

    @Override
    public void exitBool(L1Parser.BoolContext ctx) {
        var srcPos = getSourcePos(ctx);
        boolean value = ctx.BOOLEAN().getText().equals("true");
        var node = new BooleanLit(srcPos.line(), srcPos.column(), value);
        ctx.result = node;
    }

    @Override
    // On "EXIT": if a expr type matched, than this method is executed AFTER all 
    // components of the matched rule where already parsed. This means, we can safely
    // assume rule components have a result and we dont have to care about computing
    // it first.
    public void exitExpr(L1Parser.ExprContext ctx) {
        // integer
        if (ctx.zahl() != null) {
            ctx.result = ctx.zahl().result;
        } else if (ctx.MULT() != null) {
            ctx.result = parseBinOpExpr(ctx, BinaryOp.mult);
        } else if (ctx.DIV() != null) {
            ctx.result = parseBinOpExpr(ctx, BinaryOp.div);
        } else if (ctx.ADD() != null) {
            ctx.result = parseBinOpExpr(ctx, BinaryOp.add);
        } else if (ctx.SUB() != null) {
            ctx.result = parseBinOpExpr(ctx, BinaryOp.sub);
        } else if (ctx.POW() != null) {
            ctx.result = parseBinOpExpr(ctx, BinaryOp.pow);
        }
        // parentheses
        else if (ctx.LPAR() != null && ctx.RPAR() != null) {
            ctx.result = ctx.expr(0).result;
        }

        // boolean
        else if (ctx.bool() != null) {
            ctx.result = ctx.bool().result;
        } else if (ctx.AND() != null) {
            ctx.result = parseBinOpExpr(ctx, BinaryOp.and);
        } else if (ctx.OR() != null) {
            ctx.result = parseBinOpExpr(ctx, BinaryOp.or);
        } else if (ctx.NOT() != null) {
            ctx.result = parseUnaryOpExpr(ctx, UnaryOp.not);
        }

        // comparisson
        else if (ctx.EQ() != null) {
            ctx.result = parseBinOpExpr(ctx, BinaryOp.eq);
        } else if (ctx.NEQ() != null) {
            ctx.result = parseBinOpExpr(ctx, BinaryOp.neq);
        } else if (ctx.GT() != null) {
            ctx.result = parseBinOpExpr(ctx, BinaryOp.gt);
        } else if (ctx.GTEQ() != null) {
            ctx.result = parseBinOpExpr(ctx, BinaryOp.gteq);
        } else if (ctx.LT() != null) {
            ctx.result = parseBinOpExpr(ctx, BinaryOp.lt);
        } else if (ctx.LTEQ() != null) {
            ctx.result = parseBinOpExpr(ctx, BinaryOp.lteq);

        } else if (ctx.varOrFunCall() != null) {
            ctx.result = ctx.varOrFunCall().result;
        } else {
            var srcPos = getSourcePos(ctx);
            throw new UnsupportedOperationException(
                    "Unhandled `expr` alternative  '" + ctx.getText() + "' at " + srcPos);
        }
    }

    @Override
    public void exitStatement(L1Parser.StatementContext ctx) {
        var srcPos = getSourcePos(ctx);
        if (ctx.KW_RETURN() != null) {
            var expr = ctx.expr().result;
            ctx.result = new ReturnStat(srcPos.line(), srcPos.column(), expr);
        } else {
            throw new UnsupportedOperationException(
                    "Unhandled `statement` alternative '" + ctx.getText() + "' at " + srcPos);
        }
    }

    @Override
    public void exitVarOrFunCall(L1Parser.VarOrFunCallContext ctx) {
        var srcPos = getSourcePos(ctx);
        if (ctx.LPAR() != null) {
            // function call
            var args = ctx.expr() != null
                    ? ctx.expr().stream().map(expr -> expr.result).toList()
                    : null;
            ctx.result = new FunCall(srcPos.line(), srcPos.column(), ctx.IDENT().getText(), args);
        } else if (ctx.IDENT() != null && ctx.LPAR() == null) {
            // variable access
            ctx.result = new Var(srcPos.line(), srcPos.column(), ctx.IDENT().getText());
        } else {
            throw new UnsupportedOperationException(
                    "Unhandled `varOrFunCall` alternative '" + ctx.getText() + "' at " + srcPos);
        }
    }

    @Override
    public void exitDefinition(L1Parser.DefinitionContext ctx) {
        var srcPos = getSourcePos(ctx);
        var name = ctx.IDENT().getFirst().getText();
        var resturnType = ctx.IDENT().getLast().getText();
        List<Parameter> params = ctx.funParam().stream()
                .map(p -> new Parameter(p.IDENT(0).getText(), p.IDENT(1).getText())).toList();
        Node body = ctx.funBody().result;
        ctx.result = new FunDef(
                srcPos.line(), srcPos.column(), name, params, resturnType, body);
    }

    @Override
    public void exitFunBody(L1Parser.FunBodyContext ctx) {
        ctx.result = ctx.statement().result;
    }

    @Override
    public void exitStart(L1Parser.StartContext ctx) {
        var srcPos = getSourcePos(ctx);
        List<FunDef> defs = ctx.definition().stream().map(d -> d.result).toList();
        ctx.result = new Prog(srcPos.line(), srcPos.column(), defs);
    }

    //
    // Helper methods
    //
    private record SourcePos(int line, int column) {
        @Override
        public String toString() {
            return "L" + line + ":" + column;
        }
    }

    private SourcePos getSourcePos(ParserRuleContext ctx) {
        return new SourcePos(
                ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());
    }

    private Node parseBinOpExpr(L1Parser.ExprContext ctx, BinaryOp op) {
        var srcPos = getSourcePos(ctx);
        var lhs = ctx.expr(0).result;
        var rhs = ctx.expr(1).result;
        return new BinOpExpr(srcPos.line(), srcPos.column(), lhs, op, rhs);
    }

    private Node parseUnaryOpExpr(L1Parser.ExprContext ctx, UnaryOp op) {
        var srcPos = getSourcePos(ctx);
        var operand = ctx.expr(0).result;
        return new UnaryOpExpr(srcPos.line(), srcPos.column(), operand, op);
    }
}
