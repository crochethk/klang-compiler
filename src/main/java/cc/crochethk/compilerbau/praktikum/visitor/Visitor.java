package cc.crochethk.compilerbau.praktikum.visitor;

import cc.crochethk.compilerbau.praktikum.ast.*;
import cc.crochethk.compilerbau.praktikum.ast.literal.*;

public interface Visitor<R> {
    /*
    * Add one visit method per Node type of the tree
    * Concrete Visitors must then implement their behaviour for each of the types.
    * A visitor encapsulates all the algorithmic logic for a procedure on the tree.
    */
    // void visit(Visitable element);
    R visit(I64Lit i64Lit);

    R visit(F64Lit f64Lit);

    R visit(BoolLit boolLit);

    R visit(StringLit stringLit);

    R visit(NullLit nullLit);

    R visit(Var var);

    R visit(FunCall funCall);

    R visit(ConstructorCall constructorCall);

    R visit(BinOpExpr binOpExpr);

    R visit(UnaryOpExpr unaryOpExpr);

    R visit(TernaryConditionalExpr ternaryConditionalExpr);

    R visit(VarDeclareStat varDeclareStat);

    R visit(VarAssignStat varAssignStat);

    R visit(IfElseStat ifElseStat);

    R visit(LoopStat loopStat);

    R visit(StatementList statementList);

    R visit(ReturnStat returnStat);

    R visit(BreakStat breakStat);

    R visit(TypeNode type);

    R visit(FunDef funDef);

    R visit(StructDef structDef);

    R visit(Prog prog);

    R visit(EmptyNode emptyNode);
}
