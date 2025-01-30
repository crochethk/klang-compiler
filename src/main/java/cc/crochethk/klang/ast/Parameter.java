package cc.crochethk.klang.ast;

import cc.crochethk.klang.visitor.Type.CheckedParam;

import java.util.List;

public record Parameter(String name, TypeNode type) {
    public CheckedParam toChecked() {
        return new CheckedParam(name(), type().theType);
    }

    public static List<CheckedParam> toChecked(List<Parameter> params) {
        return params.stream().map(f -> f.toChecked()).toList();
    }
}