package cc.crochethk.compilerbau.praktikum.ast;

import cc.crochethk.compilerbau.praktikum.Visitor;
import cc.crochethk.compilerbau.praktikum.utils.SourcePos;

public class TernaryConditionalExpr extends Node {
    public Node condition;
    public Node then, otherwise;

    public TernaryConditionalExpr(SourcePos srcPos, Node condition, Node then, Node otherwise) {
        this(srcPos.line(), srcPos.column(), condition, then, otherwise);
    }

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
