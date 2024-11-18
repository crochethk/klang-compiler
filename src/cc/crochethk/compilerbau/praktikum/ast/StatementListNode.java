package cc.crochethk.compilerbau.praktikum.ast;

import cc.crochethk.compilerbau.praktikum.Visitor;
import cc.crochethk.compilerbau.praktikum.utils.SourcePos;

public class StatementListNode extends Node {
    public Node value;

    /**
     * The next listnode in the linked list. Instance of {@code EmptyNode} indicates
     * the end of the list, which can be checked with {@code next.isEmpty()}.
     */
    public Node next;

    public StatementListNode(SourcePos srcPos, Node value, Node next) {
        this(srcPos.line(), srcPos.column(), value, next);
    }

    public StatementListNode(int line, int column, Node value, Node next) {
        super(line, column);
        this.value = value;
        this.next = next;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visit(this);
    }
}
