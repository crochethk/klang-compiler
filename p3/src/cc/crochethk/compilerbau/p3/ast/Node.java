package cc.crochethk.compilerbau.p3.ast;

import cc.crochethk.compilerbau.p3.Visitable;

public abstract class Node implements Visitable {
    /// The line where the node's token begins in the source file.
    public int line;
    /// The column where the node's token begins in the source file.
    public int column;

    /// The result type of the node.
    public String theType = null;

    Node(int line, int column) {
        this.line = line;
        this.column = column;
    }
}
