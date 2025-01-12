package cc.crochethk.compilerbau.praktikum.ast;

import cc.crochethk.compilerbau.praktikum.visitor.Visitor;

public interface Visitable {
    void accept(Visitor visitor);

    /* Add this boilerplate for each Visitable:
    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
    */
}