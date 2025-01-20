package cc.crochethk.compilerbau.praktikum.ast;

import cc.crochethk.compilerbau.praktikum.visitor.Visitor;
import utils.SourcePos;

public class IfElseStat extends Node {
    public Node condition;
    public StatementList then, otherwise;

    public IfElseStat(SourcePos srcPos, Node condition, StatementList then, StatementList otherwise) {
        super(srcPos);
        this.condition = condition;
        this.then = then;
        this.otherwise = otherwise;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "(condition=" + condition
                + ", then=" + then + ", otherwise=" + otherwise + ")";
    }
}
