package cc.crochethk.compilerbau.P2;

public abstract class Node implements Visitable {
    /// The line where the node's token begins in the source file.
    int line;
    /// The column where the node's token begins in the source file.
    int column;

    /// The result type of the node.
    String theType = null;

    Node(int line, int column) {
        this.line = line;
        this.column = column;
    }
}
