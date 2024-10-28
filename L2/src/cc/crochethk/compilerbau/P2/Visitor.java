package cc.crochethk.compilerbau.P2;

public interface Visitor {
    default void reportError(Node node, String s) {
        System.err.println("(" + node.line + "," + node.column + "): " + s);
    }

    /*
    * Add one visit method per Node type of the tree
    * Concrete Visitors must then implement their behaviour for each of the types.
    * A visitor encapsulates all the algorithmic logic for a procedure on the tree.
    */
    // void visit(Visitable element);
    void visit(IntLit intLit);

    void visit(BinOpExpr binOpExpr);
}
