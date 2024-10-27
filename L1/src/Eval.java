public class Eval extends L1BaseListener {
    @Override
    public void exitZahl(L1Parser.ZahlContext ctx) {
        ctx.result = Long.parseLong(ctx.NUMBER().getText());
    }

    @Override
    public void exitExpr(L1Parser.ExprContext ctx) {
        if (ctx.zahl() != null) {
            ctx.result = ctx.zahl().result;
        } else if (ctx.MULT() != null) {
            ctx.result = ctx.expr().get(0).result * ctx.expr().get(1).result;
        } else if (ctx.DIV() != null) {
            ctx.result = ctx.expr().get(0).result / ctx.expr().get(1).result;
        } else if (ctx.ADD() != null) {
            ctx.result = ctx.expr().get(0).result + ctx.expr().get(1).result;
        } else if (ctx.SUB() != null) {
            ctx.result = ctx.expr().get(0).result - ctx.expr().get(1).result;
        } else if (ctx.POW() != null) {
            var lhs = ctx.expr(0).result;
            var rhs = ctx.expr(1).result;
            ctx.result = (long) Math.pow(lhs, rhs);
            // ctx.result = (long) Math.pow(ctx.expr(0).result, ctx.expr(1).result);
        } else {
            ctx.result = ctx.expr().get(0).result;
        }
    }

    @Override
    public void exitStart(L1Parser.StartContext ctx) {
        ctx.result = ctx.expr().result;
    }
}
