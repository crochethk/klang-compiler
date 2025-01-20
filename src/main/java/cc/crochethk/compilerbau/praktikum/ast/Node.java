package cc.crochethk.compilerbau.praktikum.ast;

import cc.crochethk.compilerbau.praktikum.visitor.Type;
import utils.SourcePos;

public abstract class Node implements Visitable {
    public final SourcePos srcPos;

    /// The line where the node's token begins in the source file.
    public int line() {
        return srcPos.line();
    }

    /// The column where the node's token begins in the source file.
    public int column() {
        return srcPos.column();
    }

    /// The result type of the node.
    public Type theType = null;

    protected Node(SourcePos srcPos) {
        this.srcPos = srcPos;
    }

    public boolean isEmpty() {
        return false;
    }

    public boolean returnsControlFlow() {
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof Node other) {
            return srcPos.equals(other.srcPos);
        }
        return false;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }
}
