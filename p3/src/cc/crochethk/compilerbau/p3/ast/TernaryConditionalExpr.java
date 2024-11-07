package cc.crochethk.compilerbau.p3.ast;

import cc.crochethk.compilerbau.p3.Visitor;

public class TernaryConditionalExpr extends Node {
    public Node condition;
    public Node then, otherwise;

    public TernaryConditionalExpr(int line, int column, Node condition, Node then, Node otherwise) {
        super(line, column);
        this.condition = condition;
        this.then = then;
        this.otherwise = otherwise;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visit(this);
    }
}
