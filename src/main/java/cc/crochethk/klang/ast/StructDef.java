package cc.crochethk.klang.ast;

import java.util.List;
import java.util.Objects;

import cc.crochethk.klang.visitor.Visitor;
import utils.SourcePos;

public class StructDef extends Node {
    public String name;
    public List<Parameter> fields;
    public List<MethDef> methods;
    // // public List<MethDef> autoMethods = new ArrayList<>();
    // // public List<FunDef> autoFuns = new ArrayList<>();

    public StructDef(SourcePos srcPos, String name, List<Parameter> fields, List<MethDef> methDefs) {
        super(srcPos);
        this.name = name;
        this.fields = fields != null ? fields : List.of();
        this.methods = methDefs != null ? methDefs : List.of();
    }

    @Override
    public boolean isEmpty() {
        return fields.isEmpty() && methods.isEmpty();
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return super.toString() + "(name=" + name + ", fields=" + fields
                + ", methods=" + methods + ")";
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), name, fields, methods);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof StructDef other) {
            return Objects.equals(name, other.name)
                    && Objects.equals(fields, other.fields)
                    && Objects.equals(methods, other.methods)
                    && super.equals(other);
        }
        return false;
    }
}
