package cc.crochethk.klang.ast;

import cc.crochethk.klang.visitor.Visitor;

public interface Visitable {
    void accept(Visitor visitor);

    /* Add this boilerplate for each Visitable:
    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
    */
}