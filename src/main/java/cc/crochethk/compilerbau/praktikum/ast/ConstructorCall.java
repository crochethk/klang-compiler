package cc.crochethk.compilerbau.praktikum.ast;

import java.util.Collections;
import java.util.List;

import cc.crochethk.compilerbau.praktikum.visitor.Visitor;
import utils.SourcePos;

public class ConstructorCall extends Node {
    public String structName;
    public List<Node> args;

    public ConstructorCall(SourcePos srcPos, String structName, List<Node> args) {
        super(srcPos);
        this.structName = structName;
        this.args = args != null ? args : Collections.emptyList();
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName()
                + "(structName=" + structName + ", args=" + args + ")";
    }
}
