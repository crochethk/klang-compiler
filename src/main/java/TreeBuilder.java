import java.util.List;

import org.antlr.v4.runtime.ParserRuleContext;

import cc.crochethk.compilerbau.praktikum.ast.BinOpExpr;
import cc.crochethk.compilerbau.praktikum.ast.BinOpExpr.BinaryOp;
import cc.crochethk.compilerbau.praktikum.ast.EmptyNode;
import cc.crochethk.compilerbau.praktikum.ast.FunCall;
import cc.crochethk.compilerbau.praktikum.ast.FunDef;
import cc.crochethk.compilerbau.praktikum.ast.IfElseStat;
import cc.crochethk.compilerbau.praktikum.ast.FunDef.Parameter;
import cc.crochethk.compilerbau.praktikum.ast.Node;
import cc.crochethk.compilerbau.praktikum.ast.Prog;
import cc.crochethk.compilerbau.praktikum.ast.ReturnStat;
import cc.crochethk.compilerbau.praktikum.ast.StatementList;
import cc.crochethk.compilerbau.praktikum.ast.TernaryConditionalExpr;
import cc.crochethk.compilerbau.praktikum.ast.TypeNode;
import cc.crochethk.compilerbau.praktikum.ast.UnaryOpExpr;
import cc.crochethk.compilerbau.praktikum.ast.UnaryOpExpr.UnaryOp;
import cc.crochethk.compilerbau.praktikum.ast.Var;
import cc.crochethk.compilerbau.praktikum.ast.VarAssignStat;
import cc.crochethk.compilerbau.praktikum.ast.VarDeclareStat;
import cc.crochethk.compilerbau.praktikum.ast.literals.*;
import utils.SourcePos;

public class TreeBuilder extends L1BaseListener {
    @Override
    public void exitNumber(L1Parser.NumberContext ctx) {
        /**
         * There are two kinds of literal number expressions:
         * 1) bare ("123") and
         * 2) annotated ("123i64" or "123_i64")
         * 
         * In the former case, the literal type is inferred to a default type 
         * according to the number class (integer or floating point). Then parsing
         * to the interal value representation is done aaccordingly with said 
         * default type.
         * 
         * The latter case allows specifying the type along the literal. This
         * allows to convey which concrete literal type is meant, which will be
         * useful as soon as there is more than one bit-width and/or distinction
         * between signed and unsigned types in a given number class.
         * The type annotation is done by appending "as <targetType>" to the literal.
         * This gives flexibility for later type extension that otherwise would
         * lead to ambiguity (e.g. "i64" and "i32" would be indistinguishable
         * in their common number space).
         * Also "as" could later be utilized to implement a cast expr, that 
         * converts between primitive types.
         */
        NumberLiteralType targetType = inferNumberType(ctx);
        ctx.result = buildNumberLiteral(ctx, targetType);
    }

    private NumberLiteralType inferNumberType(L1Parser.NumberContext ctx) {
        var srcPos = getSourcePos(ctx);
        var typeAnnot = ctx.typeAnnot;
        if (typeAnnot != null) {
            if (typeAnnot.T_F64() != null) {
                // converts int literals also to float
                return NumberLiteralType.f64;
            } else if (typeAnnot.T_I64() != null && ctx.LIT_INTEGER() != null) {
                return NumberLiteralType.i64;
            } else {
                throw new IllegalLiteralTypeSuffixException(
                        srcPos, ctx.getText(), typeAnnot.getText());
            }
        } else if (ctx.LIT_INTEGER() != null) {
            return NumberLiteralType.i64; // default int type
        } else if (ctx.LIT_FLOAT() != null) {
            return NumberLiteralType.f64; // default float type
        }
        throw new UnhandledAlternativeException(srcPos, "number", ctx.getText());
    }

    private enum NumberLiteralType {
        i64, f64
    }

    Node buildNumberLiteral(L1Parser.NumberContext ctx, NumberLiteralType targetType) {
        var srcPos = getSourcePos(ctx);
        Node node = switch (targetType) {
            case i64 -> new I64Lit(srcPos, Long.parseLong(ctx.num.getText()));
            case f64 -> new F64Lit(srcPos, Double.parseDouble(ctx.num.getText()));
        };
        return node;
    }

    @Override
    public void exitBool(L1Parser.BoolContext ctx) {
        var srcPos = getSourcePos(ctx);
        boolean value = ctx.TRUE() != null;
        var node = new BoolLit(srcPos, value);
        ctx.result = node;
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
            throw new UnhandledAlternativeException(srcPos, "varOrFunCall", ctx.getText());
        }
    }

    @Override
    // On "EXIT": if a expr type matched, than this method is executed AFTER all 
    // components of the matched rule where already parsed. This means, we can safely
    // assume rule components have a result and we dont have to care about computing
    // it first.
    public void exitExpr(L1Parser.ExprContext ctx) {
        // arithmetic
        if (ctx.number() != null) {
            ctx.result = ctx.number().result;
        } else if (ctx.MULT() != null) {
            ctx.result = parseBinOpExpr(ctx, BinaryOp.mult);
        } else if (ctx.DIV() != null) {
            ctx.result = parseBinOpExpr(ctx, BinaryOp.div);
        } else if (ctx.ADD() != null) {
            ctx.result = parseBinOpExpr(ctx, BinaryOp.add);
        } else if (ctx.negationOp != null) { // must come before subtract
            ctx.result = parseUnaryOpExpr(ctx, UnaryOp.neg);
        } else if (ctx.SUB() != null) {
            ctx.result = parseBinOpExpr(ctx, BinaryOp.sub);
        } else if (ctx.POW() != null) {
            ctx.result = parseBinOpExpr(ctx, BinaryOp.pow);
        }

        // parentheses
        else if (ctx.exprInParens != null) {
            ctx.result = ctx.exprInParens.result;
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

        // comparison
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

        } else if (ctx.ternaryElseBranch() != null) {
            ctx.result = buildTernaryConditionalNode(ctx, ctx.expr(), ctx.ternaryElseBranch());
        } else {
            throw new UnhandledAlternativeException(getSourcePos(ctx), "expr", ctx.getText());
        }
    }

    public void exitTernaryElseBranch(L1Parser.TernaryElseBranchContext ctx) {
        if (ctx.ternaryElseBranch() != null) {
            ctx.result = buildTernaryConditionalNode(ctx, ctx.expr(), ctx.ternaryElseBranch());
        } else {
            ctx.result = ctx.expr(0).result;
        }
    }

    private Node buildTernaryConditionalNode(ParserRuleContext ctx, List<L1Parser.ExprContext> ifThenExprs,
            L1Parser.TernaryElseBranchContext ternaryElseBranch) {
        var srcPos = getSourcePos(ctx);
        var condition = ifThenExprs.get(0).result;
        var then = ifThenExprs.get(1).result;
        var otherwise = ternaryElseBranch.result;
        return new TernaryConditionalExpr(srcPos, condition, then, otherwise);
    }

    @Override
    public void exitVarDeclarationOrAssignment(L1Parser.VarDeclarationOrAssignmentContext ctx) {
        var srcPos = getSourcePos(ctx);
        if (ctx.KW_LET() != null && ctx.ASSIGN() != null) {
            ctx.result = new StatementList(srcPos, List.of(
                    buildVarDeclareNode(srcPos, ctx), buildVarAssignNode(srcPos, ctx)));
        } else if (ctx.KW_LET() != null) {
            ctx.result = buildVarDeclareNode(srcPos, ctx);
        } else if (ctx.ASSIGN() != null) {
            ctx.result = buildVarAssignNode(srcPos, ctx);
        } else {
            throw new UnhandledAlternativeException(
                    srcPos, "varDeclarationOrAssignment", ctx.getText());
        }
    }

    private Node buildVarDeclareNode(SourcePos srcPos, L1Parser.VarDeclarationOrAssignmentContext ctx) {
        return new VarDeclareStat(srcPos, ctx.varName.getText(), ctx.type().result);
    }

    private Node buildVarAssignNode(SourcePos srcPos, L1Parser.VarDeclarationOrAssignmentContext ctx) {
        return new VarAssignStat(srcPos, ctx.varName.getText(), ctx.expr().result);
    }

    @Override
    public void exitIfElse(L1Parser.IfElseContext ctx) {
        var srcPos = getSourcePos(ctx);
        // Create ifElse Node
        var condition = ctx.condition.result;
        var then = ctx.then.result;
        var otherwise = ctx.KW_ELSE() != null
                ? ctx.otherwise.result
                : new EmptyNode(srcPos);
        ctx.result = new IfElseStat(srcPos, condition, then, otherwise);
    }

    @Override
    public void exitBlock(L1Parser.BlockContext ctx) {
        ctx.result = ctx.statementList().result;
    }

    @Override
    public void exitStatementList(L1Parser.StatementListContext ctx) {
        var statements = ctx.statement().stream().map(s -> s.result).toList();
        ctx.result = new StatementList(getSourcePos(ctx), statements);
    }

    @Override
    public void exitBlockLikeStatement(L1Parser.BlockLikeStatementContext ctx) {
        var srcPos = getSourcePos(ctx);
        if (ctx.ifElse() != null) {
            ctx.result = ctx.ifElse().result;
        } else if (ctx.block() != null) {
            ctx.result = ctx.block().result;
        } else {
            throw new UnhandledAlternativeException(srcPos, "blockLikeStatement", ctx.getText());
        }
    }

    @Override
    public void exitStatement(L1Parser.StatementContext ctx) {
        var srcPos = getSourcePos(ctx);
        if (ctx.blockLikeStatement() != null) {
            ctx.result = ctx.blockLikeStatement().result;
        } else if (ctx.varDeclarationOrAssignment() != null) {
            ctx.result = ctx.varDeclarationOrAssignment().result;
        } else if (ctx.KW_RETURN() != null) {
            var expr = ctx.expr() != null
                    ? ctx.expr().result
                    : new EmptyNode(srcPos);
            ctx.result = new ReturnStat(srcPos, expr);
        } else {
            throw new UnhandledAlternativeException(srcPos, "statement", ctx.getText());
        }
    }

    @Override
    public void exitType(L1Parser.TypeContext ctx) {
        var srcPos = getSourcePos(ctx);
        if (ctx.primitiveType() != null) {
            var ttext = ctx.primitiveType().getText();
            ctx.result = new TypeNode(srcPos, ttext);
        } else {
            throw new UnhandledAlternativeException(srcPos, "type", ctx.getText());
        }
    }

    @Override
    public void exitDefinition(L1Parser.DefinitionContext ctx) {
        var srcPos = getSourcePos(ctx);
        var name = ctx.funName.getText();
        var returnType = ctx.type().result;
        List<Parameter> params = ctx.funParam().stream()
                .map(p -> new Parameter(p.name.getText(), p.type().result)).toList();
        Node body = ctx.funBody.result;
        ctx.result = new FunDef(srcPos, name, params, returnType, body);
    }

    @Override
    public void exitStart(L1Parser.StartContext ctx) {
        var srcPos = getSourcePos(ctx);
        List<FunDef> defs = ctx.definition().stream().map(d -> d.result).toList();
        ctx.result = new Prog(srcPos, defs);
    }

    //
    // Helper methods and structs
    //
    private SourcePos getSourcePos(ParserRuleContext ctx) {
        return new SourcePos(
                ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());
    }

    private Node parseBinOpExpr(L1Parser.ExprContext ctx, BinaryOp op) {
        var srcPos = getSourcePos(ctx);
        return new BinOpExpr(srcPos, ctx.lhs.result, op, ctx.rhs.result);
    }

    private Node parseUnaryOpExpr(L1Parser.ExprContext ctx, UnaryOp op) {
        var srcPos = getSourcePos(ctx);
        var operand = ctx.expr(0).result;
        return new UnaryOpExpr(srcPos, operand, op);
    }

    private class UnhandledAlternativeException extends UnsupportedOperationException {
        UnhandledAlternativeException(SourcePos srcPos, String alternativeName, String ctxText) {
            super("Unhandled `" + alternativeName + "` alternative '" + ctxText + "' at " + srcPos);
        }
    }

    private class IllegalLiteralTypeSuffixException extends UnsupportedOperationException {
        IllegalLiteralTypeSuffixException(SourcePos srcPos, String literalText, String suffixText) {
            super("Illegal type suffix '" + suffixText + "' in literal '"
                    + literalText + "'" + "at " + srcPos);
        }
    }
}
