package cc.crochethk.compilerbau.P2;

public class IntLit extends Node {
    /// The value of the integer literal
    int n;

    public IntLit(int line, int column, int n) {
        super(line, column);
        this.n = n;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
