package cc.crochethk.compilerbau.praktikum.ast;

import java.util.Collections;
import java.util.List;

import cc.crochethk.compilerbau.praktikum.Visitor;
import cc.crochethk.compilerbau.praktikum.utils.SourcePos;

public class FunDef extends Node {
    public static final String KW_FUN_LEX = "fn";

    public record Parameter(String name, TypeNode type) {
    }

    public String name;
    public TypeNode returnType;
    public List<Parameter> params;
    public Node body;

    public FunDef(SourcePos srcPos, String name, List<Parameter> params,
            TypeNode returnType, Node body) {
        this(srcPos.line(), srcPos.column(), name, params, returnType, body);
    }

    public FunDef(int line, int column, String name, List<Parameter> params,
            TypeNode returnType, Node body) {
        super(line, column);
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
