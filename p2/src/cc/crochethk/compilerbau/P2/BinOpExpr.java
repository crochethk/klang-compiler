package cc.crochethk.compilerbau.p2;

public class BinOpExpr extends Node {
    BinaryOp op;
    Node lhs, rhs;

    public enum BinaryOp {
        // Arithmetic
        add("+"), sub("-"), mult("*"), div("/"), mod("%"),
        // Boolean
        eq("=="), neq("!="), gt(">"), gteq(">="), lt("<"), lteq("<="),
        and("and"), or("or");

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
                case add, div, mod, mult, sub -> true;
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
    public <R> R accept(Visitor<R> visitor) throws Exception {
        return visitor.visit(this);
    }
}
