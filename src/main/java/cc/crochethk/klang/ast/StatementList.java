package cc.crochethk.klang.ast;

import java.util.List;
import java.util.Objects;

import cc.crochethk.klang.visitor.Visitor;
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
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return super.toString() + "(statements=" + statements + ")";
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), statements);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof StatementList other) {
            return Objects.equals(statements, other.statements) && super.equals(other);
        }
        return false;
    }
}
