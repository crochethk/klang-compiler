package cc.crochethk.compilerbau.praktikum.ast;

import java.util.Collections;
import java.util.List;

import cc.crochethk.compilerbau.praktikum.Visitor;
import utils.SourcePos;

public class Prog extends Node {
    public List<FunDef> funDefs;
    public FunCall entryPoint;

    public Prog(SourcePos srcPos, List<FunDef> funDefs) {
        List<Node> args = Collections.emptyList();
        FunCall entryPoint = new FunCall(new SourcePos(-1, -1), "___main___", args);
        this(srcPos, funDefs, entryPoint);
    }

    public Prog(SourcePos srcPos, List<FunDef> funDefs, FunCall entryPoint) {
        super(srcPos);
        this.funDefs = funDefs;
        this.entryPoint = entryPoint;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visit(this);
    }
}
