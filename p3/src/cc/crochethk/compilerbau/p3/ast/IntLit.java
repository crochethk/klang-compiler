package cc.crochethk.compilerbau.p3.ast;

import cc.crochethk.compilerbau.p3.Visitor;

public class IntLit extends Node {
    /// The value of the integer literal
    public long n;

    public IntLit(int line, int col, int n) {
        super(line, col);
        this.n = n;
    }

    public long getValue() {
        return n;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) throws Exception {
        return visitor.visit(this);
    }
}
