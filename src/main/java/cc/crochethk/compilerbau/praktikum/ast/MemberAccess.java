package cc.crochethk.compilerbau.praktikum.ast;

import java.util.List;
import java.util.Objects;

import cc.crochethk.compilerbau.praktikum.visitor.Visitor;
import utils.SourcePos;

public sealed abstract class MemberAccess extends Node
        permits MemberAccess.FieldGet, MemberAccess.MethodCall, MemberAccess.FieldSet {
    /** Is null if {@code this} is the first element in a chain of accessors. */
    public MemberAccess owner;
    /** Name of the member this {@code MemberAccess} instance refers to. */
    public final String targetName;
    /** Is null if {@code this} is the last element in a chain of accessors. */
    public MemberAccess next;

    /** Traverses the accessor chain using 'next' and returns the last accessor. */
    public MemberAccess getLast() {
        return next == null ? this : next.getLast();
    }

    /**
     * @param srcPos
     * @param owner An expression that evaluates to instance with membername "targetName"
     * @param targetMemberName The name of the member beeing accessed (e.g. field or method name)
     * @param next The next MemberAccess Node in the chain (representing the target)
     */
    public MemberAccess(SourcePos srcPos, MemberAccess owner, String targetMemberName, MemberAccess next) {
        super(srcPos);
        this.owner = owner;
        this.targetName = targetMemberName;
        this.next = next;
    }

    /**
     * Node type representing the getter of a struct field.
     */
    public static final class FieldGet extends MemberAccess {
        /**
         * @param srcPos
         * @param owner An expression that evaluates to a struct instance with a 
         *          field named "fieldName".
         * @param fieldName The name of the field beeing accessed.
         * @see MemberAccess#MemberAccess(SourcePos, Node, String)
         */
        public FieldGet(SourcePos srcPos, MemberAccess owner, String fieldName, MemberAccess next) {
            super(srcPos, owner, fieldName, next);
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visit(this);
        }

        @Override
        public String toString() {
            return this.getClass().getSimpleName() + "(" + super.toString() + ")";
        }
    }

    /**
     * Node type representing the setter of a struct field.
     */
    public static final class FieldSet extends MemberAccess {
        public FieldSet(SourcePos srcPos, MemberAccess owner, String fieldName, MemberAccess next) {
            super(srcPos, owner, fieldName, next);
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visit(this);
        }

        @Override
        public String toString() {
            return this.getClass().getSimpleName() + "(" + super.toString() + ")";
        }
    }

    /**
     *  Node type representing the method call of a RefType instance.
     */
    public static final class MethodCall extends MemberAccess {
        public List<Node> args;

        /** @see MemberAccess#MemberAccess(SourcePos, Node, String) */
        public MethodCall(SourcePos srcPos, MemberAccess owner, String methodName, List<Node> args, MemberAccess next) {
            super(srcPos, owner, methodName, next);
            this.args = args;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visit(this);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), args);
        }

        @Override
        public String toString() {
            return this.getClass().getSimpleName()
                    + "(args=" + args.toString() + ", " + super.toString() + ")";
        }
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(super.hashCode(), targetName);

        // Hash "owner" chain (going left)
        MemberAccess currentOwner = this.owner;
        while (currentOwner != null) {
            result = Objects.hash(result, currentOwner.targetName);
            currentOwner = currentOwner.owner;
        }

        // Hash "next" chain (going right)
        MemberAccess currentNext = this.next;
        while (currentNext != null) {
            result = Objects.hash(result, currentNext.targetName);
            currentNext = currentNext.next;
        }
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass() || !super.equals(obj))
            return false;

        MemberAccess other = (MemberAccess) obj;
        if (!Objects.equals(targetName, other.targetName))
            return false;

        // Compare owner chain
        // - Go "left", comparing targetNames, until one or both owners are the left-most
        MemberAccess thisOwner = this.owner;
        MemberAccess otherOwner = other.owner;
        while (thisOwner != null && otherOwner != null) {
            // are not MemberAccess instances
            if (!Objects.equals(thisOwner.targetName, otherOwner.targetName))
                return false;
            thisOwner = thisOwner.owner;
            otherOwner = otherOwner.owner;
        }
        // One or both are now "left-most" owners (i.e. one or both are null)
        if (thisOwner != null || otherOwner != null)
            return false;

        // Compare next chain
        // - Go "right", comparing targetNames, until one or both reach boundary
        MemberAccess thisNext = this.next;
        MemberAccess otherNext = other.next;
        while (thisNext != null && otherNext != null) {
            if (!Objects.equals(thisNext.targetName, otherNext.targetName))
                return false;
            thisNext = thisNext.next;
            otherNext = otherNext.next;
        }
        return thisNext == null && otherNext == null;
    }

    @Override
    public String toString() {
        String ownerStr = owner != null ? owner.targetName : String.valueOf(owner);
        return "owner=" + ownerStr + ", target=" + targetName + ", next=" + next;
    }

    /**
     * <p>
     * Creates a doubly linked chain using the given list of (possibly detached)
     * MemberAccess nodes. 'Detached' means their {@code owner} and {@code next}
     * fields may be {@code null}. In any case this method will modify those
     * fields.
     * </p><p>
     * The resulting chain's first element will have {@code owner==null} and the
     * last element {@code next==null} respectively.
     * </p> 
     * @param ms A list of MemberAccess nodes (can be detached).
     * @return Returns the first of the now doubly linked MemberAccess nodes,
     *      or {@code null} if the provided list was empty.
     */
    public static MemberAccess chain(List<MemberAccess> ms) {
        if (ms.isEmpty())
            return null;
        /*
            owner
            a[null, STRING, null]
            b[null, STRING, null]
            c[null, STRING, null]
            ======== ↓↓↓ ========
            a[null, STRING, b]
            b[a, STRING, c]
            c[b, STRING, null]
        */
        MemberAccess next = null;
        for (var m : ms.reversed()) {
            if (next != null)
                next.owner = m;
            m.next = next;
            next = m;
        }
        return next;
    }
}
