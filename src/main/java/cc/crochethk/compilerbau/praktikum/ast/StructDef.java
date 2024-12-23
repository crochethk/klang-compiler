package cc.crochethk.compilerbau.praktikum.ast;

import java.util.Collections;
import java.util.List;

import cc.crochethk.compilerbau.praktikum.visitor.Visitor;
import utils.SourcePos;

public class StructDef extends Node {
    public String name;
    public List<Parameter> params;

    public StructDef(SourcePos srcPos, String name, List<Parameter> params) {
        super(srcPos);
        this.name = name;
        this.params = params != null ? params : Collections.emptyList();
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visit(this);
    }
}
