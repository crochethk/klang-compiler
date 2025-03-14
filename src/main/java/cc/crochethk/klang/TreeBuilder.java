package cc.crochethk.klang;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.ParserRuleContext;

import cc.crochethk.klang.antlr.*;
import cc.crochethk.klang.ast.*;
import cc.crochethk.klang.ast.BinOpExpr.BinaryOp;
import cc.crochethk.klang.ast.MemberAccess.*;
import cc.crochethk.klang.ast.UnaryOpExpr.UnaryOp;
import cc.crochethk.klang.ast.literal.*;
import cc.crochethk.klang.visitor.Type;
import utils.SourcePos;

public class TreeBuilder extends KlangBaseListener {
    static final String ENTRY_POINT_NAME = "___main___";

    @Override
    public void exitNumber(KlangParser.NumberContext ctx) {
        var srcPos = getSourcePos(ctx);
        if (ctx.LIT_INTEGER() != null) {
            // default int type
            ctx.result = new I64Lit(srcPos, Long.parseLong(ctx.num.getText()));
        } else if (ctx.LIT_FLOAT() != null) {
            // default float type
            ctx.result = new F64Lit(srcPos, Double.parseDouble(ctx.num.getText()));
        } else {
            throw new UnhandledAlternativeException(srcPos, "number", ctx.getText());
        }
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
        ttext = StringEscapeUtils.resolveEscapeSequences(ttext);
        ctx.result = new StringLit(getSourcePos(ctx), ttext);
    }

    @Override
    public void exitNullLit(KlangParser.NullLitContext ctx) {
        ctx.result = new NullLit(getSourcePos(ctx));
    }

    @Override
    public void exitVarOrFunCall(KlangParser.VarOrFunCallContext ctx) {
        var srcPos = getSourcePos(ctx);
        if (ctx.funCall() != null) {
            ctx.result = ctx.funCall().result;
        } else if (ctx.varName != null) {
            // variable access
            ctx.result = new Var(srcPos, ctx.varName.getText());
        } else {
            throw new UnhandledAlternativeException(srcPos, "varOrFunCall", ctx.getText());
        }
    }

    @Override
    public void exitFunCall(KlangParser.FunCallContext ctx) {
        var srcPos = getSourcePos(ctx);
        var args = ctx.args.stream().map(arg -> arg.result).toList();
        ctx.result = new FunCall(srcPos, ctx.name.getText(), args);
    }

    @Override
    public void exitConstructorCall(KlangParser.ConstructorCallContext ctx) {
        ctx.result = new ConstructorCall(
                getSourcePos(ctx),
                ctx.structName.getText(),
                ctx.args.stream().map(arg -> arg.result).toList());
    }

    @Override
    public void exitFieldOrMethCall(KlangParser.FieldOrMethCallContext ctx) {
        var srcPos = getSourcePos(ctx);
        if (ctx.fieldName != null) {
            ctx.result = new FieldGet(srcPos, null, ctx.fieldName.getText(), null);
        } else if (ctx.methCall != null) {
            var args = ctx.methCall.args.stream().map(arg -> arg.result).toList();
            ctx.result = new MethodCall(srcPos, null, ctx.methCall.name.getText(), args, null);
        } else {
            throw new UnhandledAlternativeException(srcPos, "fieldOrMethCall", ctx.getText());
        }
    }

    @Override
    public void exitMemberAccessor(KlangParser.MemberAccessorContext ctx) {
        var srcPos = getSourcePos(ctx);

        // Make last element a FieldSet if its a setter context
        var last = ctx.memberChain.getLast();
        if (isFieldSetCtx && last.fieldName != null) {
            var fieldGet = last.result;
            last.result = new FieldSet(
                    fieldGet.srcPos, fieldGet.owner, fieldGet.targetName, fieldGet.next);
        }

        var detachedMembers = ctx.memberChain.stream().map(
                memCtx -> memCtx.result).toList();
        var chain = MemberAccess.chain(detachedMembers);
        ctx.result = new MemberAccessChain(srcPos, ctx.owner.result, chain);
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
        } else if (ctx.MOD() != null) {
            ctx.result = parseBinOpExpr(ctx, BinaryOp.mod);
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
        } else if (ctx.nullLit() != null) {
            ctx.result = ctx.nullLit().result;
        } else if (ctx.constructorCall() != null) {
            ctx.result = ctx.constructorCall().result;
        } else if (ctx.memberAccessor() != null) {
            ctx.result = ctx.memberAccessor().result;
        } else if (ctx.KW_AS() != null) {
            ctx.result = new TypeCast(getSourcePos(ctx), ctx.expr(0).result, ctx.type().result);
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

    private Expr buildTernaryConditionalNode(ParserRuleContext ctx, List<KlangParser.ExprContext> ifThenExprs,
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
        if (ctx.KW_LET() != null) {
            ctx.result = buildVarDeclareNode(srcPos, ctx);
        } else if (ctx.EQ() != null) {
            ctx.result = new VarAssignStat(srcPos, ctx.varName.getText(), ctx.expr().result);
        } else if (ctx.structFieldAssignStat() != null) {
            ctx.result = ctx.structFieldAssignStat().result;
        } else {
            throw new UnhandledAlternativeException(
                    srcPos, "varDeclarationOrAssignment", ctx.getText());
        }
    }

    private Node buildVarDeclareNode(SourcePos srcPos, KlangParser.VarDeclarationOrAssignmentContext ctx) {
        var declaredType = ctx.type() != null ? ctx.type().result : null;
        var initializer = ctx.expr() != null ? ctx.expr().result : null;
        return new VarDeclareStat(srcPos, ctx.varName.getText(), declaredType, initializer);
    }

    @Override
    public void enterStructFieldAssignStat(KlangParser.StructFieldAssignStatContext ctx) {
        isFieldSetCtx = true;
    }

    /** Helps in creating MemberAccessChain that should be used as a setter. */
    private boolean isFieldSetCtx = false;

    @Override
    public void enterFieldSetExpr(KlangParser.FieldSetExprContext ctx) {
        // before parsing the expression we must reset the context,
        // such that memberaccessors in the expr are built correctly
        isFieldSetCtx = false;
    }

    @Override
    public void exitStructFieldAssignStat(KlangParser.StructFieldAssignStatContext ctx) {
        var srcPos = getSourcePos(ctx);
        ctx.result = new FieldAssignStat(srcPos, ctx.memberAccessor().result, ctx.fieldSetExpr().expr().result);
    }

    @Override
    public void exitIfElse(KlangParser.IfElseContext ctx) {
        var srcPos = getSourcePos(ctx);
        // Create ifElse Node
        var condition = ctx.condition.result;
        var then = ctx.then.result;
        var otherwise = ctx.KW_ELSE() != null
                ? ctx.otherwise.result
                : new StatementList(srcPos, List.of());
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
                    : new EmptyExpr(srcPos);
            ctx.result = new ReturnStat(srcPos, expr);
        } else if (ctx.KW_BREAK() != null) {
            ctx.result = new BreakStat(srcPos);
        } else if (ctx.KW_DROP() != null) {
            ctx.result = new DropStat(srcPos, new Var(srcPos, ctx.refTypeVarName.getText()));
        } else if (ctx.funCall() != null) {
            ctx.result = new VoidResultExprStat(srcPos, ctx.funCall().result);
        } else if (ctx.memberAccessor() != null) {
            ctx.result = new VoidResultExprStat(srcPos, ctx.memberAccessor().result);
        } else {
            throw new UnhandledAlternativeException(srcPos, "statement", ctx.getText());
        }
    }

    @Override
    public void exitType(KlangParser.TypeContext ctx) {
        var srcPos = getSourcePos(ctx);
        String ttext;
        if (ctx.builtinType() != null)
            ttext = ctx.builtinType().getText();
        else if (ctx.customType() != null)
            ttext = ctx.customType().getText();
        else
            throw new UnhandledAlternativeException(srcPos, "type", ctx.getText());
        ctx.result = new TypeNode(srcPos, ttext);
    }

    @Override
    public void exitParameter(KlangParser.ParameterContext ctx) {
        ctx.result = new Parameter(ctx.name.getText(), ctx.type().result);
    }

    private List<Parameter> buildParamsList(KlangParser.ParamsContext ctx) {
        return ctx != null
                ? ctx.list.stream().map(p -> p.result).toList()
                : List.of();
    }

    @Override
    public void exitFunctionDef(KlangParser.FunctionDefContext ctx) {
        var srcPos = getSourcePos(ctx);
        var name = ctx.name.getText();
        var returnType = ctx.type() != null
                ? ctx.type().result
                : new TypeNode(srcPos, "void");
        var params = buildParamsList(ctx.params());
        var body = ctx.funBody.result;
        ctx.result = new FunDef(srcPos, name, params, returnType, body);
    }

    @Override
    public void exitStructDef(KlangParser.StructDefContext ctx) {
        var srcPos = getSourcePos(ctx);
        var name = ctx.name.getText();
        var params = buildParamsList(ctx.params());
        var methDefs = ctx.methodDefs.stream().map(
                methDef -> new MethDef(srcPos, Type.of(name, ""), methDef.result)).toList();
        ctx.result = new StructDef(srcPos, name, params, methDefs);
    }

    @Override
    public void exitStart(KlangParser.StartContext ctx) {
        var srcPos = getSourcePos(ctx);

        List<StructDef> structs = new ArrayList<>();
        List<FunDef> funs = new ArrayList<>();
        FunCall entryPoint = null;

        for (var def : ctx.definition()) {
            if (def.functionDef() != null) {
                // watch for entry point
                if (entryPoint == null && def.functionDef().result.name.equals(ENTRY_POINT_NAME)) {
                    entryPoint = new FunCall(srcPos, ENTRY_POINT_NAME, List.of());
                }
                funs.add(def.functionDef().result);
            } else if (def.structDef() != null) {
                structs.add(def.structDef().result);
            }
        }

        ctx.result = new Prog(srcPos, funs, entryPoint, structs);
    }

    //
    // Helper methods and structs
    //

    /**
     * Constructs the source code position of the given context.
     * Can be overriden e.g. to mock the position in unit tests.
     */
    protected SourcePos getSourcePos(ParserRuleContext ctx) {
        return new SourcePos(
                ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());
    }

    private Expr parseBinOpExpr(KlangParser.ExprContext ctx, BinaryOp op) {
        var srcPos = getSourcePos(ctx);
        return new BinOpExpr(srcPos, ctx.lhs.result, op, ctx.rhs.result);
    }

    private Expr parseUnaryOpExpr(KlangParser.ExprContext ctx, UnaryOp op) {
        var srcPos = getSourcePos(ctx);
        var operand = ctx.expr(0).result;
        return new UnaryOpExpr(srcPos, operand, op);
    }

    private class UnhandledAlternativeException extends UnsupportedOperationException {
        UnhandledAlternativeException(SourcePos srcPos, String alternativeName, String ctxText) {
            super("Unhandled `" + alternativeName + "` alternative '" + ctxText + "' at " + srcPos);
        }
    }
}
