package cc.crochethk.compilerbau.p3;

import cc.crochethk.compilerbau.p3.ast.BinOpExpr;
import cc.crochethk.compilerbau.p3.ast.BooleanLit;
import cc.crochethk.compilerbau.p3.ast.IntLit;

public class Interpreter implements Visitor<Long> {

    @Override
    public Long visit(IntLit intLit) throws Exception {
        return intLit.n;
    }

    @Override
    public Long visit(BooleanLit booleanLit) throws Exception {
        return booleanLit.value ? 1L : 0L;
    }

    @Override
    public Long visit(BinOpExpr binOpExpr) throws Exception {
        var lhs = binOpExpr.lhs.accept(this);
        var rhs = binOpExpr.rhs.accept(this);

        return switch (binOpExpr.op) {
            case add -> lhs + rhs;
            case sub -> lhs - rhs;
            case mult -> lhs * rhs;
            case div -> lhs / rhs;
            case pow -> (long) Math.pow(lhs, rhs);
            case and -> lhs & rhs;
            case or -> lhs | rhs;

            // case mod -> lhs % rhs;
            // case eq -> lhs == rhs ? 1 : 0;
            // case neq -> lhs != rhs ? 1 : 0;
            // case gt -> lhs > rhs ? 1 : 0;
            // case gteq -> lhs >= rhs ? 1 : 0;
            // case lt -> lhs < rhs ? 1 : 0;
            // case lteq -> lhs <= rhs ? 1 : 0;

            default -> throw new UnsupportedOperationException("Unknown binary operator: " + binOpExpr.op);
        };
    }
}
