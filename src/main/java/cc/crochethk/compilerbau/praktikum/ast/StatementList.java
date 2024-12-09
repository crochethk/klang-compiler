package cc.crochethk.compilerbau.praktikum.ast;

import java.util.List;

import cc.crochethk.compilerbau.praktikum.visitor.Visitor;
import utils.SourcePos;

public class StatementList extends Node {
    public List<Node> statements;

    public StatementList(SourcePos srcPos, List<Node> statements) {
        super(srcPos);
        this.statements = statements;
    }

    @Override
    public boolean isEmpty() {
        return statements.isEmpty();
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visit(this);
    }
}
