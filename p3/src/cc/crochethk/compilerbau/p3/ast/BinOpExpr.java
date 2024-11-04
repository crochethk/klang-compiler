package cc.crochethk.compilerbau.p3.ast;

import cc.crochethk.compilerbau.p3.Visitor;

public class BinOpExpr extends Node {
    public BinaryOp op;
    public Node lhs;
    public Node rhs;

    public enum BinaryOp {
        // Arithmetic
        add("+"), sub("-"), mult("*"), div("/"), mod("%"), pow("**"),
        // Boolean
        eq("=="), neq("!="), gt(">"), gteq(">="), lt("<"), lteq("<="),
        and("&&"), or("||");

        String lexeme;

        BinaryOp(String lexeme) {
            this.lexeme = lexeme;
        }

        public String toLexeme() {
            return lexeme;
        }

        public boolean isBoolean() {
            return !isArithmetic();
        }

        public boolean isArithmetic() {
            return switch (this) {
                case add, sub, mult, div, mod, pow -> true;
                case eq, neq, gt, gteq, lt, lteq, and, or -> false;
            };
        }
    }

    public BinOpExpr(int line, int col, Node lhs, BinaryOp op, Node rhs) {
        super(line, col);
        this.lhs = lhs;
        this.op = op;
        this.rhs = rhs;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visit(this);
    }
}
