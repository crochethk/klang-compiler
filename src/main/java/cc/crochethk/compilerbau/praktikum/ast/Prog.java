package cc.crochethk.compilerbau.praktikum.ast;

import java.util.List;

import cc.crochethk.compilerbau.praktikum.visitor.Visitor;
import utils.SourcePos;

public class Prog extends Node {
    public List<FunDef> funDefs;
    public List<StructDef> structDefs;
    public FunCall entryPoint;

    public Prog(SourcePos srcPos, List<FunDef> funDefs, FunCall entryPoint, List<StructDef> structDefs) {
        super(srcPos);
        this.funDefs = funDefs;
        this.structDefs = structDefs;
        this.entryPoint = entryPoint;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
