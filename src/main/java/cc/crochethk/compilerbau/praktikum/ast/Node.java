package cc.crochethk.compilerbau.praktikum.ast;

import cc.crochethk.compilerbau.praktikum.Visitable;
import utils.SourcePos;
import cc.crochethk.compilerbau.praktikum.Type;

public abstract class Node implements Visitable {
    /// The line where the node's token begins in the source file.
    public int line;
    /// The column where the node's token begins in the source file.
    public int column;

    /// The result type of the node.
    public Type theType = null;

    protected Node(SourcePos srcPos) {
        this.line = srcPos.line();
        this.column = srcPos.column();
    }

    public boolean isEmpty() {
        return false;
    }

    public boolean returnsControlFlow() {
        return false;
    }
}
