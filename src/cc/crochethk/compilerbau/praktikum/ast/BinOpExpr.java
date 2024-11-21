package cc.crochethk.compilerbau.praktikum.ast;

import cc.crochethk.compilerbau.praktikum.Visitor;
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
            return switch (this) {
                case eq, neq, gt, gteq, lt, lteq -> true;
                case add, sub, mult, div, mod, pow, and, or -> false;
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
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visit(this);
    }
}
