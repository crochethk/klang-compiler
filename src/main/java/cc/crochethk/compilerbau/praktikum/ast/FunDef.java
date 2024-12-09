package cc.crochethk.compilerbau.praktikum.ast;

import java.util.Collections;
import java.util.List;

import cc.crochethk.compilerbau.praktikum.visitor.Visitor;
import utils.SourcePos;

public class FunDef extends Node {
    public static final String KW_FUN_LEX = "fn";

    public record Parameter(String name, TypeNode type) {
    }

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
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visit(this);
    }
}
