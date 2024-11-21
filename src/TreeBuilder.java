import java.util.List;

import org.antlr.v4.runtime.ParserRuleContext;

import cc.crochethk.compilerbau.praktikum.ast.BinOpExpr;
import cc.crochethk.compilerbau.praktikum.ast.BinOpExpr.BinaryOp;
import cc.crochethk.compilerbau.praktikum.ast.BooleanLit;
import cc.crochethk.compilerbau.praktikum.ast.EmptyNode;
import cc.crochethk.compilerbau.praktikum.ast.FunCall;
import cc.crochethk.compilerbau.praktikum.ast.FunDef;
import cc.crochethk.compilerbau.praktikum.ast.IfElseStat;
import cc.crochethk.compilerbau.praktikum.ast.FunDef.Parameter;
import cc.crochethk.compilerbau.praktikum.ast.IntLit;
import cc.crochethk.compilerbau.praktikum.ast.Node;
import cc.crochethk.compilerbau.praktikum.ast.Prog;
import cc.crochethk.compilerbau.praktikum.ast.ReturnStat;
import cc.crochethk.compilerbau.praktikum.ast.StatementListNode;
import cc.crochethk.compilerbau.praktikum.ast.TernaryConditionalExpr;
import cc.crochethk.compilerbau.praktikum.ast.TypeNode;
import cc.crochethk.compilerbau.praktikum.ast.UnaryOpExpr;
import cc.crochethk.compilerbau.praktikum.ast.UnaryOpExpr.UnaryOp;
import utils.SourcePos;
import cc.crochethk.compilerbau.praktikum.ast.Var;
import cc.crochethk.compilerbau.praktikum.ast.VarAssignStat;
import cc.crochethk.compilerbau.praktikum.ast.VarDeclareStat;

public class TreeBuilder extends L1BaseListener {
    @Override
    public void exitZahl(L1Parser.ZahlContext ctx) {
        var srcPos = getSourcePos(ctx);
        var node = new IntLit(srcPos, Integer.parseInt(ctx.NUMBER().getText()));
        ctx.result = node;
    }

    @Override
    public void exitBool(L1Parser.BoolContext ctx) {
        var srcPos = getSourcePos(ctx);
        boolean value = ctx.BOOLEAN().getText().equals("true");
        var node = new BooleanLit(srcPos, value);
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

        } else if (ctx.TERNARY_QM() != null) {
            var srcPos = getSourcePos(ctx);
            var condition = ctx.expr(0).result;
            var then = ctx.expr(1).result;
            var otherwise = ctx.ternaryExpr().result;
            ctx.result = new TernaryConditionalExpr(srcPos,
                    condition, then, otherwise);
        } else {
            var srcPos = getSourcePos(ctx);
            throw new UnsupportedOperationException(
                    "Unhandled `expr` alternative '" + ctx.getText() + "' at " + srcPos);
        }
    }

    @Override
    public void exitTernaryExpr(L1Parser.TernaryExprContext ctx) {
        if (ctx.TERNARY_QM() != null) {
            var srcPos = getSourcePos(ctx);
            var condition = ctx.expr(0).result;
            var then = ctx.expr(1).result;
            var otherwise = ctx.ternaryExpr().result;
            ctx.result = new TernaryConditionalExpr(srcPos,
                    condition, then, otherwise);
        } else {
            ctx.result = ctx.expr(0).result;
        }
    }

    @Override
    public void exitIfElse(L1Parser.IfElseContext ctx) {
        var srcPos = getSourcePos(ctx);
        // Create ifElse Node
        var condition = ctx.expr().result;
        var then = ctx.ifElseBranchBlock(0).statement().result;
        var otherwise = ctx.KW_ELSE() != null
                ? ctx.ifElseBranchBlock(1).statement().result
                : new EmptyNode(srcPos);
        var ifElseStat = new IfElseStat(srcPos, condition, then, otherwise);
        // Check for followup statement
        Node result = ctx.statement() != null
                ? new StatementListNode(srcPos, ifElseStat, ctx.statement().result)
                : ifElseStat;
        ctx.result = result;
    }

    @Override
    public void exitBasicStatement(L1Parser.BasicStatementContext ctx) {
        var srcPos = getSourcePos(ctx);
        if (ctx.KW_LET() != null) {
            ctx.result = new VarDeclareStat(
                    srcPos, ctx.IDENT().getText(), ctx.type().result);
        } else if (ctx.ASSIGN() != null) {
            ctx.result = new VarAssignStat(
                    srcPos, ctx.IDENT().getText(), ctx.expr().result);
        } else if (ctx.KW_RETURN() != null) {
            var expr = ctx.expr() != null
                    ? ctx.expr().result
                    : new EmptyNode(srcPos);
            ctx.result = new ReturnStat(srcPos, expr);
        } else {
            throw new UnsupportedOperationException(
                    "Unhandled `basicStatement` alternative '" + ctx.getText() + "' at " + srcPos);
        }
    }

    @Override
    public void exitStatement(L1Parser.StatementContext ctx) {
        var srcPos = getSourcePos(ctx);
        if (ctx.basicStatement() != null) {
            var currentStatement = ctx.basicStatement().result;
            var next = ctx.statement() != null
                    ? ctx.statement().result
                    : new EmptyNode(srcPos);
            ctx.result = new StatementListNode(srcPos, currentStatement, next);
        } else if (ctx.ifElse() != null) {
            ctx.result = ctx.ifElse().result;
        } else if (ctx.emptyStatement() != null) {
            ctx.result = new EmptyNode(srcPos);
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
            ctx.result = new FunCall(srcPos, ctx.IDENT().getText(), args);
        } else if (ctx.IDENT() != null && ctx.LPAR() == null) {
            // variable access
            ctx.result = new Var(srcPos, ctx.IDENT().getText());
        } else {
            throw new UnsupportedOperationException(
                    "Unhandled `varOrFunCall` alternative '" + ctx.getText() + "' at " + srcPos);
        }
    }

    @Override
    public void exitType(L1Parser.TypeContext ctx) {
        var srcPos = getSourcePos(ctx);
        TypeNode result = null;
        if (ctx.primitiveType() != null) {
            var ttext = ctx.primitiveType().getText();
            result = new TypeNode(srcPos, ttext, true);
        } else {
            throw new UnsupportedOperationException(
                    "Recognized but unhandled 'type' token '" + ctx.getText() + "' at " + srcPos);
        }
        ctx.result = result;
    }

    @Override
    public void exitDefinition(L1Parser.DefinitionContext ctx) {
        var srcPos = getSourcePos(ctx);
        var name = ctx.IDENT().getText();
        var resturnType = ctx.type().result;
        List<Parameter> params = ctx.funParam().stream()
                .map(p -> new Parameter(p.IDENT().getText(), p.type().result)).toList();
        Node body = ctx.statement().result;
        ctx.result = new FunDef(
                srcPos, name, params, resturnType, body);
    }

    @Override
    public void exitStart(L1Parser.StartContext ctx) {
        var srcPos = getSourcePos(ctx);
        List<FunDef> defs = ctx.definition().stream().map(d -> d.result).toList();
        ctx.result = new Prog(srcPos, defs);
    }

    //
    // Helper methods
    //
    private SourcePos getSourcePos(ParserRuleContext ctx) {
        return new SourcePos(
                ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());
    }

    private Node parseBinOpExpr(L1Parser.ExprContext ctx, BinaryOp op) {
        var srcPos = getSourcePos(ctx);
        var lhs = ctx.expr(0).result;
        var rhs = ctx.expr(1).result;
        return new BinOpExpr(srcPos, lhs, op, rhs);
    }

    private Node parseUnaryOpExpr(L1Parser.ExprContext ctx, UnaryOp op) {
        var srcPos = getSourcePos(ctx);
        var operand = ctx.expr(0).result;
        return new UnaryOpExpr(srcPos, operand, op);
    }
}
