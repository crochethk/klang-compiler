package cc.crochethk.compilerbau.praktikum.ast;

import java.util.Collections;
import java.util.List;

import cc.crochethk.compilerbau.praktikum.visitor.Visitor;
import utils.SourcePos;

public class StructDef extends Node {
    public String name;
    public List<Parameter> fields;

    public StructDef(SourcePos srcPos, String name, List<Parameter> fields) {
        super(srcPos);
        this.name = name;
        this.fields = fields != null ? fields : Collections.emptyList();
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visit(this);
    }
}
