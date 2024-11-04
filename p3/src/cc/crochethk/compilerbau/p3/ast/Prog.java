package cc.crochethk.compilerbau.p3.ast;

import java.util.List;

import cc.crochethk.compilerbau.p3.Visitor;

public class Prog extends Node {
    public List<FunDef> funDefs;

    public Prog(int line, int column, List<FunDef> funDefs) {
        super(line, column);
        this.funDefs = funDefs;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visit(this);
    }
}
