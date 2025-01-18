package cc.crochethk.compilerbau.praktikum.ast;

import java.util.Objects;

import cc.crochethk.compilerbau.praktikum.visitor.Visitor;
import utils.SourcePos;

/** Simple wrapper Node for chained {@code MemberAccessor}s. */
public class MemberAccessChain extends Node {
    /** Initiator of the member access. Must not be {@code null}. */
    public final Node owner;
    /**
     * Accessor chain leading to the actual member to be accessed. There must be
     * at least one element in the chain, i.e. this field should never be 
     * {@code null}.
     */
    public final MemberAccess chain;

    public MemberAccessChain(SourcePos srcPos, Node owner, MemberAccess chain) {
        super(srcPos);
        this.owner = owner;
        this.chain = chain;
    }

    /** Gets the last accessor of this chain. */
    public MemberAccess getLast() {
        return chain.getLast();
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName()
                + "(owner=" + owner + ", chain=" + chain + ")";
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), owner, chain);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof MemberAccessChain other) {
            return Objects.equals(owner, other.owner)
                    && Objects.equals(chain, other.chain)
                    && super.equals(other);
        }
        return false;
    }
}
