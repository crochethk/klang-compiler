package cc.crochethk.klang.ast;

import java.util.Objects;

import cc.crochethk.klang.visitor.Visitor;
import utils.SourcePos;

public class TypeCast extends Expr {
    public final Expr expr;
    public final TypeNode targetType;

    public TypeCast(SourcePos srcPos, Expr expr, TypeNode targetType) {
        super(srcPos);
        this.expr = expr;
        this.targetType = targetType;
    }

    @Override
    public boolean isOrHasFunCall() {
        return expr.isOrHasFunCall();
    }

    @Override
    public boolean isOrHasMemberAccessChain() {
        return expr.isOrHasMemberAccessChain();
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return super.toString() + "(expr=" + expr + ", castType=" + targetType + ")";
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), expr, targetType);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof TypeCast other) {
            return Objects.equals(expr, other.expr)
                    && Objects.equals(targetType, other.targetType)
                    && super.equals(other);
        }
        return false;
    }
}
