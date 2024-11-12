package cc.crochethk.compilerbau.praktikum.ast;

import java.util.Collections;
import java.util.List;

import cc.crochethk.compilerbau.praktikum.Visitor;

public class Prog extends Node {
    public List<FunDef> funDefs;
    public FunCall entryPoint;

    public Prog(int line, int column, List<FunDef> funDefs) {
        List<Node> args = Collections.emptyList();
        FunCall entryPoint = new FunCall(-1, -1, "___main___", args);
        this(line, column, funDefs, entryPoint);
    }

    public Prog(int line, int column, List<FunDef> funDefs, FunCall entryPoint) {
        super(line, column);
        this.funDefs = funDefs;
        this.entryPoint = entryPoint;
        // TODO
        // TODO the check should actually be done in the semantic anaylsis
        // TODO (where also TypeChecking takes place). for now it stays here
        // TODO since there is no according analyser yet and otherwise 
        // TODO it makes debugging way harder when entrypoint was forgotten for 
        // TODO some reason.
        // TODO
        var entryPointCount = funDefs.stream().filter(fd -> fd.name.equals(entryPoint.name)).count();
        if (entryPointCount > 1) {
            throw new Error("Entrypoint '" + entryPoint.name + "' was defined more than once");
        } else if (entryPointCount <= 0) {
            throw new Error("Entrypoint '" + entryPoint.name + "' not found");
        }
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visit(this);
    }
}
