package cc.crochethk.compilerbau.P2;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class BinOpExpr extends Node {
    BinaryOp op;
    Node lhs, rhs;

    public enum BinaryOp {
        add, sub, mult, div, mod, // Arithmetic
        eq, neq, gt, gteq, lt, lteq, and, or; // Boolean

        static final List<BinaryOp> boolOps = Collections
                .unmodifiableList(Arrays.asList(eq, neq, gt, gteq, lt, lteq, and, or));

        static final List<BinaryOp> arithOps = Collections
                .unmodifiableList(Arrays.asList(add, mult, sub, div, mod));
    }

    public BinOpExpr(int line, int col, Node lhs, BinaryOp op, Node rhs) {
        super(line, col);
        this.lhs = lhs;
        this.op = op;
        this.rhs = rhs;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) throws Exception {
        return visitor.visit(this);
    }
}
