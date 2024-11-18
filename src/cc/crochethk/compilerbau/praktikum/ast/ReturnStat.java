package cc.crochethk.compilerbau.praktikum.ast;

import cc.crochethk.compilerbau.praktikum.Visitor;
import cc.crochethk.compilerbau.praktikum.utils.SourcePos;

public class ReturnStat extends Node {
    public Node expr;

    public ReturnStat(SourcePos srcPos, Node expr) {
        this(srcPos.line(), srcPos.column(), expr);
    }

    public ReturnStat(int line, int column, Node expr) {
        super(line, column);
        this.expr = expr;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visit(this);
    }
}
