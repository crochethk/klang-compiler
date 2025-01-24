package cc.crochethk.klang.ast;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import cc.crochethk.klang.visitor.Visitor;
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
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName()
                + "(name=" + name + ", fields=" + fields + ")";
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), name, fields);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof StructDef other) {
            return Objects.equals(name, other.name)
                    && Objects.equals(fields, other.fields)
                    && super.equals(other);
        }
        return false;
    }
}
