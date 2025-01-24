package cc.crochethk.klang.ast;

import java.util.Objects;

import cc.crochethk.klang.visitor.Visitor;
import utils.SourcePos;

public class BinOpExpr extends Node {
    public BinaryOp op;
    public Node lhs;
    public Node rhs;

    public enum BinaryOp {
        // Arithmetic
        add("+"), sub("-"), mult("*"), div("/"), mod("%"), pow("**"),
        // Comparisson
        eq("=="), neq("!="), gt(">"), gteq(">="), lt("<"), lteq("<="),
        // Boolean
        and("&&"), or("||");

        String lexeme;

        BinaryOp(String lexeme) {
            this.lexeme = lexeme;
        }

        public String toLexeme() {
            return lexeme;
        }

        public boolean isBoolean() {
            return switch (this) {
                case and, or -> true;
                case eq, neq, gt, gteq, lt, lteq, add, sub, mult, div, mod, pow -> false;
            };
        }

        public boolean isComparison() {
            return isEqualityComparison() || isOrdinalComparison();
        }

        public boolean isOrdinalComparison() {
            return switch (this) {
                case gt, gteq, lt, lteq -> true;
                default -> false;
            };
        }

        public boolean isEqualityComparison() {
            return switch (this) {
                case eq, neq -> true;
                default -> false;
            };
        }

        public boolean isArithmetic() {
            return switch (this) {
                case add, sub, mult, div, mod, pow -> true;
                case eq, neq, gt, gteq, lt, lteq, and, or -> false;
            };
        }
    }

    public BinOpExpr(SourcePos srcPos, Node lhs, BinaryOp op, Node rhs) {
        super(srcPos);
        this.lhs = lhs;
        this.op = op;
        this.rhs = rhs;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName()
                + "(lhs=" + lhs + ", op=" + op + ", rhs=" + rhs + ")";
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), lhs, op, rhs);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof BinOpExpr other) {
            return Objects.equals(lhs, other.lhs)
                    && Objects.equals(op, other.op)
                    && Objects.equals(rhs, other.rhs)
                    && super.equals(other);
        }
        return false;
    }
}
