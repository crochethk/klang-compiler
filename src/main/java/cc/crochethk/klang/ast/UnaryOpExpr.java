package cc.crochethk.klang.ast;

import java.util.Objects;

import cc.crochethk.klang.visitor.Visitor;
import utils.SourcePos;

public class UnaryOpExpr extends Expr {
    public UnaryOp op;
    public Expr operand;

    public enum UnaryOp {
        // Arithmetic
        neg("-", Side.left),
        // Boolean
        not("!", Side.left);

        /** Where an operator is, relative to the target operand. */
        public enum Side {
            left, right;
        }

        public final Side side;
        private final String lexeme;

        UnaryOp(String lexeme, Side side) {
            this.lexeme = lexeme;
            this.side = side;
        }

        public String toLexeme() {
            return lexeme;
        }

        public boolean isBoolean() {
            return !isArithmetic();
        }

        public boolean isArithmetic() {
            return switch (this) {
                case neg -> true;
                case not -> false;
            };
        }
    }

    public UnaryOpExpr(SourcePos srcPos, Expr operand, UnaryOp op) {
        super(srcPos);
        this.operand = operand;
        this.op = op;
    }

    @Override
    public boolean isOrHasMemberAccessChain() {
        return operand.isOrHasMemberAccessChain();
    }

    @Override
    public boolean isOrHasFunCall() {
        return operand.isOrHasFunCall();
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return super.toString() + "(op=" + op + ", operand=" + operand + ")";
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), op, operand);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof UnaryOpExpr other) {
            return Objects.equals(op, other.op)
                    && Objects.equals(operand, other.operand)
                    && super.equals(other);
        }
        return false;
    }
}
