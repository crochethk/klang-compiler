package cc.crochethk.compilerbau.p3;

import cc.crochethk.compilerbau.p3.ast.BinOpExpr;
import cc.crochethk.compilerbau.p3.ast.BooleanLit;
import cc.crochethk.compilerbau.p3.ast.IntLit;
import cc.crochethk.compilerbau.p3.ast.Node;

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
    R visit(IntLit intLit) throws Exception;

    R visit(BooleanLit booleanLit) throws Exception;

    R visit(BinOpExpr binOpExpr) throws Exception;
}
