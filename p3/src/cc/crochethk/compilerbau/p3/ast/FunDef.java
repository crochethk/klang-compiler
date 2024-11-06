package cc.crochethk.compilerbau.p3.ast;

import java.util.List;

import cc.crochethk.compilerbau.p3.Visitor;

public class FunDef extends Node {
    public static final String KW_FUN_LEX = "fn";

    public record Parameter(String name, String type) {
    }

    public String name;
    public String returnType;
    public List<Parameter> params;
    public Node statement;

    public FunDef(int line, int column, String name, List<Parameter> params,
            String returnType, Node statement) {
        super(line, column);
        this.name = name;
        this.params = params;
        this.returnType = returnType;
        this.statement = statement;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visit(this);
    }
}
