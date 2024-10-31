package cc.crochethk.compilerbau.P2;

public class IntLit extends Node {
    /// The value of the integer literal
    int n;

    public IntLit(int line, int col, int n) {
        super(line, col);
        this.n = n;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) throws Exception {
        return visitor.visit(this);
    }
}
