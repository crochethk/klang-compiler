import org.antlr.v4.runtime.ParserRuleContext;

import cc.crochethk.compilerbau.p2.BinOpExpr;
import cc.crochethk.compilerbau.p2.BooleanLit;
import cc.crochethk.compilerbau.p2.BinOpExpr.BinaryOp;
import cc.crochethk.compilerbau.p2.IntLit;
import cc.crochethk.compilerbau.p2.Node;

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
        } else {
            var srcPos = getSourcePos(ctx);
            throw new UnsupportedOperationException("Unknown rule '" + ctx.getText() + "' at " + srcPos);
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
        var node = new BinOpExpr(
                srcPos.line(), srcPos.column(), lhs, op, rhs);
        return node;
    }
}
