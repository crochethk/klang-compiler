package cc.crochethk.compilerbau.praktikum.ast;

import cc.crochethk.compilerbau.praktikum.Visitor;
import utils.SourcePos;

public class IfElseStat extends Node {
    public Node condition;
    public Node then, otherwise;

    public IfElseStat(SourcePos srcPos, Node condition, Node then, Node otherwise) {
        super(srcPos);
        this.condition = condition;
        this.then = then;
        this.otherwise = otherwise;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visit(this);
    }
}
