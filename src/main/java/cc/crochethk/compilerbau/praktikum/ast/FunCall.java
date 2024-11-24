package cc.crochethk.compilerbau.praktikum.ast;

import java.util.Collections;
import java.util.List;

import cc.crochethk.compilerbau.praktikum.Visitor;
import utils.SourcePos;

public class FunCall extends Node {
    public String name;
    public List<Node> args;

    public FunCall(SourcePos srcPos, String name, List<Node> args) {
        super(srcPos);
        this.name = name;
        this.args = args != null ? args : Collections.emptyList();
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visit(this);
    }
}
