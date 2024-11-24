package cc.crochethk.compilerbau.praktikum.ast;

import cc.crochethk.compilerbau.praktikum.Visitor;
import utils.SourcePos;

public class StatementListNode extends Node {
    public Node current;

    /**
     * The next listnode in the linked list. Instance of {@code EmptyNode} indicates
     * the end of the list, which can be checked with {@code next.isEmpty()}.
     */
    public Node next;

    /**
     * @param srcPos
     * @param current The current node in the list.
     * @param next The next node in the list. Provide "EmptyNode" to terminate list.
     */
    public StatementListNode(SourcePos srcPos, Node current, Node next) {
        super(srcPos);
        this.current = current;
        this.next = next;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visit(this);
    }
}
