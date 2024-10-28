package cc.crochethk.compilerbau.P2;

public abstract class Node implements Visitable {
    int line, column;

    /// The result type of the node
    String theType = null;

    Node(int line, int column) {
        this.line = line;
        this.column = column;
    }
}
