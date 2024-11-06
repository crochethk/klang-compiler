package cc.crochethk.compilerbau.p3;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cc.crochethk.compilerbau.p3.InterpretResult.BoolResult;
import cc.crochethk.compilerbau.p3.InterpretResult.NumericalResult;
import cc.crochethk.compilerbau.p3.InterpretResult.NumericalResult.IntResult;
import cc.crochethk.compilerbau.p3.InterpretResult.VoidResult;
import cc.crochethk.compilerbau.p3.ast.*;

public class Interpreter implements Visitor<InterpretResult> {
    @Override
    public InterpretResult visit(IntLit intLit) {
        return new IntResult(intLit.value);
    }

    @Override
    public InterpretResult visit(BooleanLit booleanLit) {
        return new BoolResult(booleanLit.value);
    }

    @Override
    public InterpretResult visit(BinOpExpr binOpExpr) {
        var lhs = binOpExpr.lhs.accept(this);
        var rhs = binOpExpr.rhs.accept(this);

        if (lhs instanceof NumericalResult left && rhs instanceof NumericalResult right) {
            return left.applyOperator(right, binOpExpr.op);
        } else if (lhs instanceof BoolResult left && rhs instanceof BoolResult right) {
            return left.applyOperator(right, binOpExpr.op);
        } else {
            throw new UnsupportedOperationException(
                    "Unsupported InterpretResult type: " + lhs.getClass());
        }
    }

    private Map<String, FunDef> funDefs = new HashMap<>();

    @Override
    public InterpretResult visit(FunDef funDef) {
        funDefs.put(funDef.name, funDef);
        return new VoidResult();
    }

    @Override
    public InterpretResult visit(Prog prog) {
        prog.funDefs.stream().map(def -> def.accept(this));
        if (!funDefs.containsKey(prog.entryPoint.name)) {
            throw new RuntimeException("Entrypoint '" + prog.entryPoint.name + "' not found");
        }

        return prog.entryPoint.accept(this);
    }

    private Map<String, Node> vars = new HashMap<>();

    @Override
    public InterpretResult visit(Var var) {
        return vars.get(var.name).accept(this);
    }

    @Override
    public InterpretResult visit(FunCall funCall) {
        //TODO

        List<Node> args = funCall.args;
        String funName = funCall.name;
        FunDef funDef = funDefs.get(funName);
        for (int i = 0; i < funDef.params.size(); i++) {
            // we trust the type checker that arg count and types are correct.
            var var_name = funDef.params.get(i).name();
            var var_val = funCall.args.get(i).accept(this);
            // TODO---------
        }

        return new VoidResult();
    }

    @Override
    public InterpretResult visit(ReturnStat returnStat) {
        return returnStat.expr.accept(this);
    }
}
