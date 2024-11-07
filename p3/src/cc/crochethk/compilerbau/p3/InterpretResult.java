package cc.crochethk.compilerbau.p3;

import cc.crochethk.compilerbau.p3.ast.BinOpExpr.BinaryOp;
import cc.crochethk.compilerbau.p3.ast.UnaryOpExpr.UnaryOp;

public interface InterpretResult {
    Object value();

    default String valueAsString() {
        return value() + "";
    }

    public interface NumericalResult extends InterpretResult {
        InterpretResult applyOperator(NumericalResult rhs, BinaryOp op);

        InterpretResult applyOperator(UnaryOp op);

        public record IntResult(Long value) implements NumericalResult {
            @Override
            public InterpretResult applyOperator(NumericalResult rhs, BinaryOp op) {
                if (rhs instanceof IntResult other) {
                    return switch (op) {
                        case add -> new IntResult(this.value() + other.value());
                        case sub -> new IntResult(this.value() - other.value());
                        case mult -> new IntResult(this.value() * other.value());
                        case div -> new IntResult(this.value() / other.value());
                        case pow -> new IntResult((long) Math.pow(this.value(), other.value()));
                        // case mod -> new IntResult(this.value() % other.value());

                        // comparissons
                        case eq -> new BoolResult(this.value().equals(other.value()));
                        case neq -> new BoolResult(!(this.value().equals(other.value())));
                        case gt -> new BoolResult(this.value() > other.value());
                        case gteq -> new BoolResult(this.value() >= other.value());
                        case lt -> new BoolResult(this.value() < other.value());
                        case lteq -> new BoolResult(this.value() <= other.value());
                        default -> throw new UnsupportedOperationException(
                                "Unsupported binary operator: " + op + "(" + op.toLexeme() + ")");
                    };
                } else {
                    throw new UnsupportedOperationException("Righthandside has unsupported type.");
                }
            }

            @Override
            public InterpretResult applyOperator(UnaryOp op) {
                var result = switch (op) {
                    case neg -> -this.value();
                    default -> throw new UnsupportedOperationException(
                            "Unsupported unary operator: " + op + "(" + op.toLexeme() + ")");
                };
                return new IntResult(result);
            }
        }
    }

    public record BoolResult(Boolean value) implements InterpretResult {
        public InterpretResult applyOperator(BoolResult rhs, BinaryOp op) {
            if (rhs instanceof BoolResult other) {
                var result = switch (op) {
                    case and, eq, neq -> this.value() && other.value();
                    case or -> this.value() || other.value();
                    default -> throw new UnsupportedOperationException(
                            "Unsupported binary operator: " + op + "(" + op.toLexeme() + ")");
                };
                return new BoolResult(result);
            } else {
                throw new UnsupportedOperationException("Righthandside has unsupported type.");
            }
        }

        public InterpretResult applyOperator(UnaryOp op) {
            var result = switch (op) {
                case not -> !this.value();
                default -> throw new UnsupportedOperationException(
                        "Unsupported unary operator: " + op + "(" + op.toLexeme() + ")");
            };
            return new BoolResult(result);
        }
    }

    public record VoidResult() implements InterpretResult {
        @Override
        public String valueAsString() {
            return "";
        }

        @Override
        public Object value() {
            return null;
        }
    }

    // public record StringResult(String value) implements InterpretResult {
    //     @Override
    //     public String valueAsString() {
    //         return value() + "";
    //     }
    // }
}
