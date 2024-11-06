package cc.crochethk.compilerbau.p3;

public interface InterpretResult {
    String valueAsString();

    public record IntResult(long value) implements InterpretResult {
        @Override
        public String valueAsString() {
            return value() + "";
        }
    }

    public record BoolResult(boolean value) implements InterpretResult {
        @Override
        public String valueAsString() {
            return value() + "";
        }
    }

    public record VoidResult() implements InterpretResult {
        @Override
        public String valueAsString() {
            return "";
        }
    }

    // public record StringResult(String value) implements InterpretResult {
    //     @Override
    //     public String valueAsString() {
    //         return value() + "";
    //     }
    // }
}
