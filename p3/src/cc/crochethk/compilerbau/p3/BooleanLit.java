package cc.crochethk.compilerbau.p3;

public class BooleanLit extends Node {
    /// The value of the boolean literal
    boolean value;

    public BooleanLit(int line, int col, boolean value) {
        super(line, col);
        this.value = value;
    }

    public boolean getValue() {
        return value;
    }

    // Boilerplate code for the Visitor pattern
    @Override
    public <R> R accept(Visitor<R> visitor) throws Exception {
        return visitor.visit(this);
    }
}
