package cc.crochethk.compilerbau.p3;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

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
        prog.funDefs.forEach(def -> def.accept(this));
        if (!funDefs.containsKey(prog.entryPoint.name)) {
            throw new RuntimeException("Entrypoint '" + prog.entryPoint.name + "' not found");
        }
        return prog.entryPoint.accept(this);
    }

    @Override
    public InterpretResult visit(Var var) {
        return vars.get(var.name).peek();
    }

    private Map<String, Stack<InterpretResult>> vars = new HashMap<>();

    @Override
    public InterpretResult visit(FunCall funCall) {
        String funName = funCall.name;
        FunDef funDef = funDefs.get(funName);

        // Prepare parameter variables
        for (int i = 0; i < funDef.params.size(); i++) {
            // we trust the type checker that arg count and types are correct.
            var varName = funDef.params.get(i).name();
            var varValue = funCall.args.get(i).accept(this);
            if (vars.containsKey(varName)) {
                vars.get(varName).push(varValue);
            } else {
                var stack = new Stack<InterpretResult>();
                stack.push(varValue);
                vars.put(varName, stack);
            }
        }
        var result = funDef.body.accept(this);

        // remove pushed local variables again
        for (int i = 0; i < funDef.params.size(); i++) {
            var varName = funDef.params.get(i).name();
            vars.get(varName).pop();
        }

        return result;
    }

    @Override
    public InterpretResult visit(ReturnStat returnStat) {
        return returnStat.expr.accept(this);
    }

    @Override
    public InterpretResult visit(UnaryOpExpr unaryOpExpr) {
        var operand = unaryOpExpr.operand.accept(this);

        if (operand instanceof NumericalResult num) {
            return num.applyOperator(unaryOpExpr.op);
        } else if (operand instanceof BoolResult bool) {
            return bool.applyOperator(unaryOpExpr.op);
        } else {
            throw new UnsupportedOperationException(
                    "Unsupported InterpretResult type: " + operand.getClass());
        }
    }

    @Override
    public InterpretResult visit(TernaryConditionalExpr ternaryConditionalExpr) {
        if ((boolean) ternaryConditionalExpr.condition.accept(this).value()) {
            return ternaryConditionalExpr.then.accept(this);
        } else {
            return ternaryConditionalExpr.otherwise.accept(this);
        }
    }
}
