package cc.crochethk.klang.testhelpers;

import java.util.List;

import cc.crochethk.klang.ast.*;
import cc.crochethk.klang.ast.BinOpExpr.BinaryOp;
import cc.crochethk.klang.ast.MemberAccess.*;
import cc.crochethk.klang.ast.UnaryOpExpr.UnaryOp;
import cc.crochethk.klang.ast.literal.*;
import cc.crochethk.klang.visitor.Type;
import utils.SourcePos;

public class NodeMocker {
    protected final SourcePos srcPosMock = new SourcePos(-1, -1);

    public final TypeNode STRING_TN = typeNode("string");
    public final TypeNode I64_TN = typeNode("i64");
    public final TypeNode BOOL_TN = typeNode("bool");
    public final TypeNode F64_TN = typeNode("f64");
    public final TypeNode VOID_TN = typeNode("void");

    public final NullLit NULL_LIT = new NullLit(srcPosMock);
    protected final StructDef EMPTY_STRUCT = new StructDef(srcPosMock, "Empty", List.of(), List.of());

    public Parameter param(String paramName, String typeName) {
        return param(paramName, typeNode(typeName));
    }

    public Parameter param(String paramName, TypeNode tn) {
        return new Parameter(paramName, tn);
    }

    public Prog prog(List<FunDef> funDefs, List<StructDef> structDefs) {
        return new Prog(srcPosMock, funDefs, null, structDefs);
    }

    public FunDef funDef(String name, List<Parameter> params, TypeNode returnType, List<Node> statements) {
        return new FunDef(srcPosMock, name, params, returnType,
                new StatementList(srcPosMock, statements));
    }

    /** FunDef mock with {@code void} return type */
    public FunDef funDef(String name, List<Parameter> params, List<Node> statements) {
        return funDef(name, params, VOID_TN, statements);
    }

    public StructDef structDef(String name, List<Parameter> fields) {
        return structDef(name, fields, List.of());
    }

    public StructDef structDef(String name, List<Parameter> fields, List<MethDef> methods) {
        return new StructDef(srcPosMock, name, fields, methods);
    }

    public MethDef methDef(Type ownerType, String methName, List<Parameter> params, TypeNode returnType,
            List<Node> statements) {
        return new MethDef(srcPosMock, ownerType, funDef(methName, params, returnType, statements));
    }

    /** MethDef mock with {@code void} return type */
    public MethDef methDef(Type ownerType, String methName, List<Parameter> params, List<Node> statements) {
        return methDef(ownerType, methName, params, VOID_TN, statements);
    }

    public TypeNode typeNode(String typeName) {
        return new TypeNode(srcPosMock, typeName);
    }

    public ReturnStat returnStat(Expr expr) {
        return new ReturnStat(srcPosMock, expr);
    }

    public ReturnStat returnStat() {
        return new ReturnStat(srcPosMock, emptyNode());
    }

    public I64Lit i64Lit(long value) {
        return new I64Lit(srcPosMock, value);
    }

    public F64Lit f64Lit(double value) {
        return new F64Lit(srcPosMock, value);
    }

    public StringLit stringLit(String value) {
        return new StringLit(srcPosMock, value);
    }

    public BoolLit boolLit(boolean value) {
        return new BoolLit(srcPosMock, value);
    }

    /** Var declaration w/o init. */
    public VarDeclareStat varDeclareStat(String varName, TypeNode declaredType) {
        return varDeclareStat(varName, declaredType, null);
    }

    /** Var declaration w/o type specifier. */
    public VarDeclareStat varDeclareStatInferType(String varName, Expr expr) {
        return varDeclareStat(varName, null, expr);
    }

    /** Var declaration w/ init. */
    public VarDeclareStat varDeclareStat(String varName, TypeNode declaredType, Expr expr) {
        return new VarDeclareStat(srcPosMock, varName, declaredType, expr);
    }

    public VarAssignStat varAssignStat(String targetVarName, Expr expr) {
        return new VarAssignStat(srcPosMock, targetVarName, expr);
    }

    public BinOpExpr binOpExpr(Expr lhs, BinaryOp op, Expr rhs) {
        return new BinOpExpr(srcPosMock, lhs, op, rhs);
    }

    public UnaryOpExpr unaryOpExpr(Expr operand, UnaryOp op) {
        return new UnaryOpExpr(srcPosMock, operand, op);
    }

    public TypeCast typeCast(Expr expr, TypeNode targetType) {
        return new TypeCast(srcPosMock, expr, targetType);
    }

    public Var var(String name) {
        return new Var(srcPosMock, name);
    }

    public FunCall funCall(String name, List<Expr> args) {
        return new FunCall(srcPosMock, name, args);
    }

    public MemberAccessChain memberAccessChain(Expr owner, List<MemberAccess> detachedMembers) {
        return new MemberAccessChain(srcPosMock, owner, MemberAccess.chain(detachedMembers));
    }

    public MemberAccessChain memberAccessChain(Expr owner, MemberAccess... detachedMembers) {
        var dms = List.of(detachedMembers);
        return memberAccessChain(owner, dms);
    }

    /** Shortcut for creating a detached FieldGet accessor */
    public FieldGet fieldGet(String fieldName) {
        return new FieldGet(srcPosMock, null, fieldName, null);
    }

    public FieldGet fieldGet(MemberAccess owner, String fieldName, MemberAccess next) {
        return new FieldGet(srcPosMock, owner, fieldName, next);
    }

    /** Shortcut for creating a detached FieldSet accessor */
    public FieldSet fieldSet(String fieldName) {
        return new FieldSet(srcPosMock, null, fieldName, null);
    }

    /** Shortcut for creating a detached FieldGet accessor */
    public MethodCall methodCall(String methodName, List<Expr> args) {
        return new MethodCall(srcPosMock, null, methodName, args, null);
    }

    /** Call without args */
    public FunCall funCall(String name) {
        return funCall(name, List.of());
    }

    public ConstructorCall constructorCall(String structName, List<Expr> args) {
        return new ConstructorCall(srcPosMock, structName, args);
    }

    /** Call without args */
    public ConstructorCall constructorCall(String structName) {
        return constructorCall(structName, List.of());
    }

    public TernaryConditionalExpr ternaryConditionalExpr(Expr condition, Expr then, Expr otherwise) {
        return new TernaryConditionalExpr(srcPosMock, condition, then, otherwise);
    }

    public StatementList statementList(Node... nodes) {
        return new StatementList(srcPosMock, List.of(nodes));
    }

    public IfElseStat ifElseStat(Expr condition, StatementList then, StatementList otherwise) {
        return new IfElseStat(srcPosMock, condition, then, otherwise);
    }

    public FieldAssignStat fieldAssignStat(Expr owner, List<MemberAccess> accessors, Expr expr) {
        return new FieldAssignStat(srcPosMock, memberAccessChain(owner, accessors), expr);
    }

    public EmptyExpr emptyNode() {
        return new EmptyExpr(srcPosMock);
    }

    public LoopStat loopStat(StatementList statementList) {
        return new LoopStat(srcPosMock, statementList);
    }

    public BreakStat breakStat() {
        return new BreakStat(srcPosMock);
    }

    public DropStat dropStat(String varName) {
        return dropStat(new Var(srcPosMock, varName));
    }

    public DropStat dropStat(Var var) {
        return new DropStat(srcPosMock, var);
    }
}
