package cc.crochethk.compilerbau.praktikum.ast;

import cc.crochethk.compilerbau.praktikum.Visitor;

public class BooleanLit extends Node {
    public static final String TRUE_LEX = "true";
    public static final String FALSE_LEX = "false";

    /// The value of the boolean literal
    public boolean value;

    public BooleanLit(int line, int col, boolean value) {
        super(line, col);
        this.value = value;
    }

    // Boilerplate code for the Visitor pattern
    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visit(this);
    }
}
