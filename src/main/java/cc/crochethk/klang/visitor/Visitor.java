package cc.crochethk.klang.visitor;

import cc.crochethk.klang.ast.*;
import cc.crochethk.klang.ast.MemberAccess.*;
import cc.crochethk.klang.ast.literal.*;

public interface Visitor {
    /*
    * Add one visit method per Node type of the tree
    * Concrete Visitors must then implement their behaviour for each of the types.
    * A visitor encapsulates all the algorithmic logic for a procedure on the tree.
    */
    // void visit(Visitable element);
    void visit(I64Lit i64Lit);

    void visit(F64Lit f64Lit);

    void visit(BoolLit boolLit);

    void visit(StringLit stringLit);

    void visit(NullLit nullLit);

    void visit(Var var);

    void visit(FunCall funCall);

    default void visit(MemberAccess memberAccess) {
    }

    void visit(MemberAccessChain memberAccessChain);

    void visit(MethodCall methodCall);

    void visit(FieldGet fieldGet);

    void visit(FieldSet fieldSet);

    void visit(ConstructorCall constructorCall);

    void visit(BinOpExpr binOpExpr);

    void visit(UnaryOpExpr unaryOpExpr);

    void visit(TernaryConditionalExpr ternaryConditionalExpr);

    void visit(VarDeclareStat varDeclareStat);

    void visit(VarAssignStat varAssignStat);

    void visit(FieldAssignStat fieldAssignStat);

    void visit(IfElseStat ifElseStat);

    void visit(LoopStat loopStat);

    void visit(StatementList statementList);

    void visit(ReturnStat returnStat);

    void visit(BreakStat breakStat);

    void visit(DropStat dropStat);

    void visit(TypeNode type);

    void visit(FunDef funDef);

    void visit(StructDef structDef);

    void visit(Prog prog);

    void visit(EmptyNode emptyNode);
}
