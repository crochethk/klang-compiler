package cc.crochethk.klang.ast;

import java.util.Objects;

import cc.crochethk.klang.visitor.Type;
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
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((srcPos == null) ? 0 : srcPos.hashCode());
        result = prime * result + ((theType == null) ? 0 : theType.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof Node other) {
            return Objects.equals(srcPos, other.srcPos)
                    && Objects.equals(theType, other.theType);
        }
        return false;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }
}
