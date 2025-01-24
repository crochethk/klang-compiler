package cc.crochethk.klang.ast;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import cc.crochethk.klang.visitor.Visitor;
import utils.SourcePos;

public class FunDef extends Node {
    public String name;
    public TypeNode returnType;
    public List<Parameter> params;
    public StatementList body;

    public FunDef(SourcePos srcPos, String name, List<Parameter> params,
            TypeNode returnType, StatementList body) {
        super(srcPos);
        this.name = name;
        this.params = params != null ? params : Collections.emptyList();
        this.returnType = returnType;
        this.body = body;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "(name=" + name + ", returnType="
                + returnType + ", params=" + params + ", body=" + body + ")";
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), name, returnType, params, body);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof FunDef other) {
            return Objects.equals(name, other.name)
                    && Objects.equals(returnType, other.returnType)
                    && Objects.equals(params, other.params)
                    && Objects.equals(body, other.body)
                    && super.equals(other);
        }
        return false;
    }
}
