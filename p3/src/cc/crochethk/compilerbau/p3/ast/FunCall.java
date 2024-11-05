package cc.crochethk.compilerbau.p3.ast;

import java.util.Collections;
import java.util.List;

import cc.crochethk.compilerbau.p3.Visitor;

public class FunCall extends Node {
    public String name;
    public List<Node> args;

    public FunCall(int line, int column, String name, List<Node> args) {
        super(line, column);
        this.name = name;
        this.args = args != null ? args : Collections.emptyList();
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visit(this);
    }
}
