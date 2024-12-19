package cc.crochethk.compilerbau.praktikum;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.antlr.v4.runtime.ParserRuleContext;

import cc.crochethk.compilerbau.praktikum.antlr.*;
import cc.crochethk.compilerbau.praktikum.ast.*;
import cc.crochethk.compilerbau.praktikum.ast.BinOpExpr.BinaryOp;
import cc.crochethk.compilerbau.praktikum.ast.UnaryOpExpr.UnaryOp;
import cc.crochethk.compilerbau.praktikum.ast.literal.*;
import utils.SourcePos;

public class TreeBuilder extends KlangBaseListener {
    @Override
    public void exitNumber(KlangParser.NumberContext ctx) {
        /**
         * There are two kinds of literal number expressions:
         * 1) bare ("123") and
         * 2) annotated ("123 as i64" )
         * 
         * In the former case, the literal type is inferred to a default type 
         * according to the number class (integer or floating point). Then parsing
         * to the internal value representation is done accordingly with said 
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
         */
        NumberLiteralType targetType = inferNumberType(ctx);
        ctx.result = buildNumberLiteral(ctx, targetType);
    }

    private NumberLiteralType inferNumberType(KlangParser.NumberContext ctx) {
        var srcPos = getSourcePos(ctx);
        var typeAnnot = ctx.typeAnnot;
        if (typeAnnot != null) {
            if (typeAnnot.T_F64() != null) {
                // converts int literals also to float
                return NumberLiteralType.f64;
            } else if (typeAnnot.T_I64() != null && ctx.LIT_INTEGER() != null) {
                return NumberLiteralType.i64;
            } else {
                throw new IllegalLiteralTypeAnnotException(
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

    Node buildNumberLiteral(KlangParser.NumberContext ctx, NumberLiteralType targetType) {
        var srcPos = getSourcePos(ctx);
        boolean hasTypeAnnot = ctx.typeAnnot != null;
        Node node = switch (targetType) {
            case i64 -> new I64Lit(srcPos, Long.parseLong(ctx.num.getText()), hasTypeAnnot);
            case f64 -> new F64Lit(srcPos, Double.parseDouble(ctx.num.getText()), hasTypeAnnot);
        };
        return node;
    }

    @Override
    public void exitBool(KlangParser.BoolContext ctx) {
        var srcPos = getSourcePos(ctx);
        boolean value = ctx.TRUE() != null;
        var node = new BoolLit(srcPos, value);
        ctx.result = node;
    }

    @Override
    public void exitString(KlangParser.StringContext ctx) {
        var ttext = ctx.LIT_STRING().getText();
        // remove enclosing '"'
        ttext = ttext.substring(1, ttext.length() - 1);
        ttext = resolveEscapeSequences(ttext);
        ctx.result = new StringLit(getSourcePos(ctx), ttext);
    }

    String resolveEscapeSequences(String s) {
        // other "\"-escaped chars will be simply removed
        List<String> rawSpecialChars = List.of(
                "\"", // literal "
                "\\" // literal \
        );
        var escapedSpecialChars = rawSpecialChars.stream().map(
                rech -> "\\" + rech).toList();

        var regexStr = String.join("|",
                escapedSpecialChars.stream().map(escCh -> Pattern.quote(escCh)).toList());
        var regex = Pattern.compile(regexStr);

        var chunks = regex.splitWithDelimiters(s, 0);
        for (int i = 0; i < chunks.length; i++) {
            var escCharIdx = escapedSpecialChars.indexOf(chunks[i]);
            if (escCharIdx > -1) {
                // replace escaped by actual char
                chunks[i] = rawSpecialChars.get(escCharIdx);
            } else {
                // remove all chars with '\' prefix
                chunks[i] = chunks[i].replaceAll("\\\\.", "");
            }
        }

        return String.join("", chunks);
    }

    @Override
    public void exitVarOrFunCall(KlangParser.VarOrFunCallContext ctx) {
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
    public void exitExpr(KlangParser.ExprContext ctx) {
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
        else if (ctx.EQEQ() != null) {
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
        } else if (ctx.string() != null) {
            ctx.result = ctx.string().result;
        } else {
            throw new UnhandledAlternativeException(getSourcePos(ctx), "expr", ctx.getText());
        }
    }

    public void exitTernaryElseBranch(KlangParser.TernaryElseBranchContext ctx) {
        if (ctx.ternaryElseBranch() != null) {
            ctx.result = buildTernaryConditionalNode(ctx, ctx.expr(), ctx.ternaryElseBranch());
        } else {
            ctx.result = ctx.expr(0).result;
        }
    }

    private Node buildTernaryConditionalNode(ParserRuleContext ctx, List<KlangParser.ExprContext> ifThenExprs,
            KlangParser.TernaryElseBranchContext ternaryElseBranch) {
        var srcPos = getSourcePos(ctx);
        var condition = ifThenExprs.get(0).result;
        var then = ifThenExprs.get(1).result;
        var otherwise = ternaryElseBranch.result;
        return new TernaryConditionalExpr(srcPos, condition, then, otherwise);
    }

    @Override
    public void exitVarDeclarationOrAssignment(KlangParser.VarDeclarationOrAssignmentContext ctx) {
        var srcPos = getSourcePos(ctx);
        if (ctx.KW_LET() != null && ctx.EQ() != null) {
            ctx.result = new StatementList(srcPos, List.of(
                    buildVarDeclareNode(srcPos, ctx), buildVarAssignNode(srcPos, ctx)));
        } else if (ctx.KW_LET() != null) {
            ctx.result = buildVarDeclareNode(srcPos, ctx);
        } else if (ctx.EQ() != null) {
            ctx.result = buildVarAssignNode(srcPos, ctx);
        } else {
            throw new UnhandledAlternativeException(
                    srcPos, "varDeclarationOrAssignment", ctx.getText());
        }
    }

    private Node buildVarDeclareNode(SourcePos srcPos, KlangParser.VarDeclarationOrAssignmentContext ctx) {
        return new VarDeclareStat(srcPos, ctx.varName.getText(), ctx.type().result);
    }

    private Node buildVarAssignNode(SourcePos srcPos, KlangParser.VarDeclarationOrAssignmentContext ctx) {
        return new VarAssignStat(srcPos, ctx.varName.getText(), ctx.expr().result);
    }

    @Override
    public void exitIfElse(KlangParser.IfElseContext ctx) {
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
    public void exitLoop(KlangParser.LoopContext ctx) {
        ctx.result = new LoopStat(getSourcePos(ctx), ctx.block().result);
    }

    @Override
    public void exitBlock(KlangParser.BlockContext ctx) {
        ctx.result = ctx.statementList().result;
    }

    @Override
    public void exitStatementList(KlangParser.StatementListContext ctx) {
        var statements = ctx.statement().stream().map(s -> s.result).toList();
        ctx.result = new StatementList(getSourcePos(ctx), statements);
    }

    @Override
    public void exitBlockLikeStatement(KlangParser.BlockLikeStatementContext ctx) {
        var srcPos = getSourcePos(ctx);
        if (ctx.ifElse() != null) {
            ctx.result = ctx.ifElse().result;
        } else if (ctx.block() != null) {
            ctx.result = ctx.block().result;
        } else if (ctx.loop() != null) {
            ctx.result = ctx.loop().result;
        } else {
            throw new UnhandledAlternativeException(srcPos, "blockLikeStatement", ctx.getText());
        }
    }

    @Override
    public void exitStatement(KlangParser.StatementContext ctx) {
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
        } else if (ctx.KW_BREAK() != null) {
            ctx.result = new BreakStat(srcPos);
        } else {
            throw new UnhandledAlternativeException(srcPos, "statement", ctx.getText());
        }
    }

    @Override
    public void exitType(KlangParser.TypeContext ctx) {
        var srcPos = getSourcePos(ctx);
        if (ctx.primitiveType() != null) {
            var ttext = ctx.primitiveType().getText();
            ctx.result = new TypeNode(srcPos, ttext);
        } else if (ctx.refType() != null) {
            var ttext = ctx.refType().getText();
            ctx.result = new TypeNode(srcPos, ttext);
        } else {
            throw new UnhandledAlternativeException(srcPos, "type", ctx.getText());
        }
    }

    @Override
    public void exitParam(KlangParser.ParamContext ctx) {
        ctx.result = new Parameter(ctx.name.getText(), ctx.type().result);
    }

    @Override
    public void exitFunctionDef(KlangParser.FunctionDefContext ctx) {
        var srcPos = getSourcePos(ctx);
        var name = ctx.name.getText();
        var returnType = ctx.type() != null ? ctx.type().result : new TypeNode(srcPos, "void");
        List<Parameter> params = ctx.param().stream().map(p -> p.result).toList();
        var body = ctx.funBody.result;
        ctx.result = new FunDef(srcPos, name, params, returnType, body);
    }

    @Override
    public void exitStructDef(KlangParser.StructDefContext ctx) {
        var srcPos = getSourcePos(ctx);
        var name = ctx.name.getText();
        List<Parameter> params = ctx.param().stream().map(p -> p.result).toList();
        ctx.result = new StructDef(srcPos, name, params);

    }

    @Override
    public void exitStart(KlangParser.StartContext ctx) {
        var srcPos = getSourcePos(ctx);

        List<StructDef> structs = new ArrayList<>();
        List<FunDef> funs = new ArrayList<>();
        boolean hasEntryPoint = false;

        for (var def : ctx.definition()) {
            if (def.functionDef() != null) {
                // watch for entry point
                if (!hasEntryPoint && def.functionDef().result.name.equals("___main___")) {
                    hasEntryPoint = true;
                }
                funs.add(def.functionDef().result);
            } else if (def.structDef() != null) {
                structs.add(def.structDef().result);
            }
        }

        var entryPoint = hasEntryPoint
                ? new FunCall(new SourcePos(-1, -1), "___main___", Collections.emptyList())
                : null;
        ctx.result = new Prog(srcPos, funs, entryPoint, structs);
    }

    //
    // Helper methods and structs
    //
    private SourcePos getSourcePos(ParserRuleContext ctx) {
        return new SourcePos(
                ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());
    }

    private Node parseBinOpExpr(KlangParser.ExprContext ctx, BinaryOp op) {
        var srcPos = getSourcePos(ctx);
        return new BinOpExpr(srcPos, ctx.lhs.result, op, ctx.rhs.result);
    }

    private Node parseUnaryOpExpr(KlangParser.ExprContext ctx, UnaryOp op) {
        var srcPos = getSourcePos(ctx);
        var operand = ctx.expr(0).result;
        return new UnaryOpExpr(srcPos, operand, op);
    }

    private class UnhandledAlternativeException extends UnsupportedOperationException {
        UnhandledAlternativeException(SourcePos srcPos, String alternativeName, String ctxText) {
            super("Unhandled `" + alternativeName + "` alternative '" + ctxText + "' at " + srcPos);
        }
    }

    class IllegalLiteralTypeAnnotException extends UnsupportedOperationException {
        IllegalLiteralTypeAnnotException(SourcePos srcPos, String literalText, String suffixText) {
            super("Illegal type suffix '" + suffixText + "' in literal '"
                    + literalText + "'" + "at " + srcPos);
        }
    }
}
