package cc.crochethk.compilerbau.praktikum.ast;

import java.util.Collections;
import java.util.List;

import cc.crochethk.compilerbau.praktikum.Visitor;

public class FunDef extends Node {
    public static final String KW_FUN_LEX = "fn";

    public record Parameter(String name, String type) {
    }

    public String name;
    public String returnType;
    public List<Parameter> params;
    public Node body;

    public FunDef(int line, int column, String name, List<Parameter> params,
            String returnType, Node body) {
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
