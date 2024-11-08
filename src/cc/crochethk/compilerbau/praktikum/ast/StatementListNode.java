package cc.crochethk.compilerbau.praktikum.ast;

import cc.crochethk.compilerbau.praktikum.Visitor;

public class StatementListNode extends Node {
    public Node value;

    /**
     * The next listnode in the linked list. {@code null} if the current instance
     * is the last node in the list.
     */
    public Node next;

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
