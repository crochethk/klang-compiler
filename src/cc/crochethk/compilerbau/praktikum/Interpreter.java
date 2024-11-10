package cc.crochethk.compilerbau.praktikum;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import cc.crochethk.compilerbau.praktikum.InterpretResult.BoolResult;
import cc.crochethk.compilerbau.praktikum.InterpretResult.NoResult;
import cc.crochethk.compilerbau.praktikum.InterpretResult.NumericalResult;
import cc.crochethk.compilerbau.praktikum.InterpretResult.NumericalResult.IntResult;
import cc.crochethk.compilerbau.praktikum.InterpretResult.VoidResult;
import cc.crochethk.compilerbau.praktikum.ast.*;

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
        return NoResult.instance();
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
        return returnStat.expr != null
                ? returnStat.expr.accept(this)
                : new VoidResult();
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

    @Override
    public InterpretResult visit(IfElseStat ifElseStat) {
        if ((boolean) ifElseStat.condition.accept(this).value()) {
            return ifElseStat.then.accept(this);
        } else if (ifElseStat.otherwise != null) {
            return ifElseStat.otherwise.accept(this);
        } else {
            return NoResult.instance();
        }
    }

    @Override
    public InterpretResult visit(VarAssignStat varAssignStat) {
        if (vars.containsKey(varAssignStat.targetVarName)) {
            var targetVarStack = vars.get(varAssignStat.targetVarName);
            targetVarStack.pop();
            targetVarStack.push(varAssignStat.expr.accept(this));
        } else {
            throw new IllegalArgumentException(
                    "Assignment target variable " + varAssignStat.targetVarName + " is not declared");
        }
        return NoResult.instance();
    }

    @Override
    public InterpretResult visit(VarDeclareStat varDeclareStat) {
        var varName = varDeclareStat.varName;
        var varValue = (InterpretResult) null;
        if (vars.containsKey(varName)) {
            // create unassigned entry
            vars.get(varName).push(null);
        } else {
            var stack = new Stack<InterpretResult>();
            stack.push(varValue);
            vars.put(varName, stack);
        }
        return NoResult.instance();
    }

    @Override
    public InterpretResult visit(StatementListNode statementListNode) {
        var currentResult = statementListNode.value.accept(this);

        if (currentResult.isNoResult()) {
            if (statementListNode.next != null) {
                return statementListNode.next.accept(this);
            } else {
                // current node was last of the list and yielded no result.
                return NoResult.instance();
            }
        } else {
            // stop evaluation and return concrete result.
            return currentResult;
        }
    }
}
