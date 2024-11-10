package cc.crochethk.compilerbau.praktikum.ast;

import cc.crochethk.compilerbau.praktikum.Visitor;

public class IfElseStat extends Node {
    public Node condition;
    public Node then, otherwise;

    public IfElseStat(int line, int column, Node condition, Node then, Node otherwise) {
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
