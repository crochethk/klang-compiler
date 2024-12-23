package cc.crochethk.compilerbau.praktikum.visitor;

import cc.crochethk.compilerbau.praktikum.ast.BinOpExpr;
import cc.crochethk.compilerbau.praktikum.ast.BreakStat;
import cc.crochethk.compilerbau.praktikum.ast.EmptyNode;
import cc.crochethk.compilerbau.praktikum.ast.FunCall;
import cc.crochethk.compilerbau.praktikum.ast.FunDef;
import cc.crochethk.compilerbau.praktikum.ast.IfElseStat;
import cc.crochethk.compilerbau.praktikum.ast.LoopStat;
import cc.crochethk.compilerbau.praktikum.ast.Prog;
import cc.crochethk.compilerbau.praktikum.ast.ReturnStat;
import cc.crochethk.compilerbau.praktikum.ast.StatementList;
import cc.crochethk.compilerbau.praktikum.ast.StructDef;
import cc.crochethk.compilerbau.praktikum.ast.TernaryConditionalExpr;
import cc.crochethk.compilerbau.praktikum.ast.TypeNode;
import cc.crochethk.compilerbau.praktikum.ast.UnaryOpExpr;
import cc.crochethk.compilerbau.praktikum.ast.Var;
import cc.crochethk.compilerbau.praktikum.ast.VarAssignStat;
import cc.crochethk.compilerbau.praktikum.ast.VarDeclareStat;
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

    R visit(Var var);

    R visit(FunCall funCall);

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
