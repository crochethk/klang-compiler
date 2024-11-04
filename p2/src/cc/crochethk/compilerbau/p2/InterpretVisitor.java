package cc.crochethk.compilerbau.p2;

public class InterpretVisitor implements Visitor<Integer> {

    @Override
    public Integer visit(IntLit intLit) throws Exception {
        return intLit.n;
    }

    @Override
    public Integer visit(BooleanLit booleanLit) throws Exception {
        return booleanLit.value ? 1 : 0;
    }

    @Override
    public Integer visit(BinOpExpr binOpExpr) throws Exception {
        int lhs = binOpExpr.lhs.accept(this);
        int rhs = binOpExpr.rhs.accept(this);

        return switch (binOpExpr.op) {
            case add -> lhs + rhs;
            case sub -> lhs - rhs;
            case mult -> lhs * rhs;
            case div -> lhs / rhs;
            case pow -> (int) Math.pow(lhs, rhs);
            case and -> lhs & rhs;
            case or -> lhs | rhs;

            default -> throw new UnsupportedOperationException("Unknown binary operator: " + binOpExpr.op);
        };
    }
}
