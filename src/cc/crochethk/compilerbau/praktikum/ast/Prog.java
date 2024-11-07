package cc.crochethk.compilerbau.praktikum.ast;

import java.util.Collections;
import java.util.List;

import cc.crochethk.compilerbau.praktikum.Visitor;

public class Prog extends Node {
    public List<FunDef> funDefs;

    public FunCall entryPoint = new FunCall(-1, -1, "main", Collections.emptyList());

    public Prog(int line, int column, List<FunDef> funDefs) {
        super(line, column);
        this.funDefs = funDefs;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visit(this);
    }
}
