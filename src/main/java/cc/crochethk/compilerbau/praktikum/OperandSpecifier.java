package cc.crochethk.compilerbau.praktikum;

public interface OperandSpecifier {
    String operandSpec();

    /** Enum representing registers according to x86-64, Linux System V ABI */
    public enum Register implements OperandSpecifier {
        rax, rcx, rdx, rbx, rsi, rdi, rsp, rbp, r8, r9, r10, r11, r12, r13, r14, r15;

        @Override
        public String operandSpec() {
            return "%" + this;
        }
    }
}
