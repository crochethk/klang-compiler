import org.antlr.v4.runtime.ParserRuleContext;

import cc.crochethk.compilerbau.praktikum.antlr.*;

import utils.SourcePos;

@SuppressWarnings("unused")
public class TestParseTreeListener extends L1BaseListener {
    @Override
    public void exitIfElse(L1Parser.IfElseContext ctx) {
        var cond = ctx.condition;
        var then = ctx.then;
        var otherwise = ctx.otherwise;
    }

    //
    // Helper methods
    //
    private SourcePos getSourcePos(ParserRuleContext ctx) {
        return new SourcePos(
                ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());
    }

}
