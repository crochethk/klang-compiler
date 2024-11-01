package cc.crochethk.compilerbau.p2;

public class BooleanLit extends Node {
    /// The value of the integer literal
    boolean value;

    public BooleanLit(int line, int col, boolean value) {
        super(line, col);
        this.value = value;
    }

    // Boilerplate code for the Visitor pattern
    @Override
    public <R> R accept(Visitor<R> visitor) throws Exception {
        return visitor.visit(this);
    }
}
