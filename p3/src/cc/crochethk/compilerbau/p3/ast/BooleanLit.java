package cc.crochethk.compilerbau.p3.ast;

import cc.crochethk.compilerbau.p3.Visitor;

public class BooleanLit extends Node {
    /// The value of the boolean literal
    public boolean value;

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
