package cc.crochethk.compilerbau.praktikum.ast;

import cc.crochethk.compilerbau.praktikum.visitor.Visitor;
import utils.SourcePos;

public class DropStat extends Node {
    public Var refTypeVar;

    public DropStat(SourcePos srcPos, Var namedInstance) {
        super(srcPos);
        this.refTypeVar = namedInstance;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof DropStat other) {
            return refTypeVar.equals(other.refTypeVar) && super.equals(other);
        }
        return false;
    }
}
