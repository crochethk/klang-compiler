package cc.crochethk.compilerbau.praktikum.ast;

import cc.crochethk.compilerbau.praktikum.Visitor;
import cc.crochethk.compilerbau.praktikum.utils.SourcePos;

public class UnaryOpExpr extends Node {
    public UnaryOp op;
    public Node operand;

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

    public UnaryOpExpr(SourcePos srcPos, Node operand, UnaryOp op) {
        this(srcPos.line(), srcPos.column(), operand, op);
    }

    public UnaryOpExpr(int line, int col, Node operand, UnaryOp op) {
        super(line, col);
        this.operand = operand;
        this.op = op;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visit(this);
    }
}
