package cc.crochethk.klang.ast;

import java.util.Objects;

import cc.crochethk.klang.visitor.Visitor;
import utils.SourcePos;

public class VarDeclareStat extends Node {
    public String varName;
    public TypeNode declaredType;

    public VarDeclareStat(SourcePos srcPos, String varName, TypeNode declaredType) {
        super(srcPos);
        this.varName = varName;
        this.declaredType = declaredType;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName()
                + "(varName=" + varName + ", declaredType=" + declaredType + ")";
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), varName, declaredType);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof VarDeclareStat other) {
            return Objects.equals(varName, other.varName)
                    && Objects.equals(declaredType, other.declaredType)
                    && super.equals(other);
        }
        return false;
    }
}
