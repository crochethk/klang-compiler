package cc.crochethk.klang.ast;

import java.util.Objects;
import java.util.Optional;

import cc.crochethk.klang.visitor.Visitor;
import utils.SourcePos;

public class VarDeclareStat extends Node {
    private final String varName;
    public final Optional<TypeNode> declaredType;
    public final Optional<VarAssignStat> initializer;

    /**
     * @param declaredType A semi-optional type to declare the variable with. 
     *  May be {@code null}, if {@initExpr} is provided.
     * @param initExpr A semi-optional expression to initialize the declared
     *  variable with. May be {@code null}, if {@declaredType} is provided.
     */
    public VarDeclareStat(SourcePos srcPos, String varName, TypeNode declaredType, Expr initExpr) {
        super(srcPos);
        this.varName = varName;
        this.declaredType = Optional.ofNullable(declaredType);
        this.initializer = Optional.ofNullable(
                initExpr != null ? new VarAssignStat(srcPos, varName, initExpr) : null);
    }

    public String varName() {
        return varName;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + String.format(
                "%s(varName=%s, declaredType=%s, init=%s)",
                varName(), declaredType, initializer.map(init -> init.expr));
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), varName, declaredType, initializer);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof VarDeclareStat other) {
            return Objects.equals(varName, other.varName)
                    && Objects.equals(declaredType, other.declaredType)
                    && Objects.equals(initializer, other.initializer)
                    && super.equals(other);
        }
        return false;
    }
}
