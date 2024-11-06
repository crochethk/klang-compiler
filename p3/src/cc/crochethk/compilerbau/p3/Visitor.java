package cc.crochethk.compilerbau.p3;

import cc.crochethk.compilerbau.p3.ast.BinOpExpr;
import cc.crochethk.compilerbau.p3.ast.BooleanLit;
import cc.crochethk.compilerbau.p3.ast.FunCall;
import cc.crochethk.compilerbau.p3.ast.FunDef;
import cc.crochethk.compilerbau.p3.ast.IntLit;
import cc.crochethk.compilerbau.p3.ast.Node;
import cc.crochethk.compilerbau.p3.ast.Prog;
import cc.crochethk.compilerbau.p3.ast.ReturnStat;
import cc.crochethk.compilerbau.p3.ast.UnaryOpExpr;
import cc.crochethk.compilerbau.p3.ast.Var;

public interface Visitor<R> {
    default void reportError(Node node, String s) {
        System.err.println("(L" + node.line + ":" + node.column + ") " + s);
    }

    /*
    * Add one visit method per Node type of the tree
    * Concrete Visitors must then implement their behaviour for each of the types.
    * A visitor encapsulates all the algorithmic logic for a procedure on the tree.
    */
    // void visit(Visitable element);
    R visit(IntLit intLit);

    R visit(BooleanLit booleanLit);

    R visit(BinOpExpr binOpExpr);

    R visit(FunDef funDef);

    R visit(Prog prog);

    R visit(Var var);

    R visit(FunCall funCall);

    R visit(ReturnStat returnStat);

    R visit(UnaryOpExpr unaryOpExpr);
}
