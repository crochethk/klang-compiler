package cc.crochethk.compilerbau.praktikum.ast;

import java.util.List;
import java.util.Objects;

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

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "(funDefs=" + funDefs
                + ", structDefs=" + structDefs + ", entryPoint=" + entryPoint + ")";
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), funDefs, structDefs, entryPoint);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof Prog other) {
            return Objects.equals(funDefs, other.funDefs)
                    && Objects.equals(structDefs, other.structDefs)
                    && Objects.equals(entryPoint, other.entryPoint)
                    && super.equals(other);
        }
        return false;
    }
}
