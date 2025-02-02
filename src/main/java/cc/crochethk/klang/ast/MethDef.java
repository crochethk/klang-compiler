package cc.crochethk.klang.ast;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import cc.crochethk.klang.visitor.Type;
import cc.crochethk.klang.visitor.Visitor;
import utils.SourcePos;

public class MethDef extends Node {
    /**
     * This definition will contain the method's owner as first parameter 'self'.
     */
    public final FunDef def;

    /**
     * @param ownerType The owner's type.
     * @param def Actual method defintion. The 'self' parameter is injected in this 
     * constructor to the beginning of the definition's parameter list.
     */
    public MethDef(SourcePos srcPos, Type ownerType, FunDef def) {
        super(srcPos);
        // Inject implicit first parameter 'self' to defintion
        var self = new Parameter("self", new TypeNode(srcPos, ownerType.klangName()));
        def.params = Stream.concat(Stream.of(self), def.params.stream()).toList();
        this.def = def;
    }

    public TypeNode owner() {
        return def.params.get(0).type();
    }

    public String name() {
        return def.name;
    }

    public List<Parameter> params() {
        return def.params;
    }

    public TypeNode returnType() {
        return def.returnType;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return super.toString() + "(def=" + def + ")";
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), def);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof MethDef other) {
            return Objects.equals(def, other.def) && super.equals(other);
        }
        return false;
    }
}
