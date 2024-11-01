import org.antlr.v4.runtime.ParserRuleContext;

import cc.crochethk.compilerbau.p2.BinOpExpr;
import cc.crochethk.compilerbau.p2.IntLit;

public class TreeBuilder extends L1BaseListener {
    @Override
    public void exitZahl(L1Parser.ZahlContext ctx) {
        var srcPos = getSourcePos(ctx);
        var node = new IntLit(
                srcPos.line(), srcPos.column(), Integer.parseInt(ctx.NUMBER().getText()));
        ctx.result = node;
    }

    @Override
    public void exitExpr(L1Parser.ExprContext ctx) {
        var srcPos = getSourcePos(ctx);
        if (ctx.zahl() != null) {
            ctx.result = ctx.zahl().result;
        } else if (ctx.MULT() != null) {
            var lhs = ctx.expr(0).result;
            var rhs = ctx.expr(1).result;
            var node = new BinOpExpr(
                    srcPos.line(), srcPos.column(), lhs, BinOpExpr.BinaryOp.mult, rhs);
            ctx.result = node;
        } else if (ctx.DIV() != null) {
            var lhs = ctx.expr(0).result;
            var rhs = ctx.expr(1).result;
            var node = new BinOpExpr(
                    srcPos.line(), srcPos.column(), lhs, BinOpExpr.BinaryOp.div, rhs);
            ctx.result = node;
        } else if (ctx.ADD() != null) {
            var lhs = ctx.expr(0).result;
            var rhs = ctx.expr(1).result;
            var node = new BinOpExpr(
                    srcPos.line(), srcPos.column(), lhs, BinOpExpr.BinaryOp.add, rhs);
            ctx.result = node;
        } else if (ctx.SUB() != null) {
            var lhs = ctx.expr(0).result;
            var rhs = ctx.expr(1).result;
            var node = new BinOpExpr(
                    srcPos.line(), srcPos.column(), lhs, BinOpExpr.BinaryOp.sub, rhs);
            ctx.result = node;
            // } else if (ctx.POW() != null) {
            //     var lhs = ctx.expr(0).result;
            //     var rhs = ctx.expr(1).result;
            //     var node = new BinOpExpr(
            //             srcPos.line(), srcPos.column(), lhs, BinOpExpr.BinaryOp.pow, rhs);
            //     ctx.result = node;
        } else {
            ctx.result = ctx.expr().get(0).result;
        }
    }

    @Override
    public void exitStart(L1Parser.StartContext ctx) {
        ctx.result = ctx.expr().result;
    }

    //
    // Helper methods
    //
    private record SourcePos(int line, int column) {
    }

    private SourcePos getSourcePos(ParserRuleContext ctx) {
        return new SourcePos(
                ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());
    }
}
