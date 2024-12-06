package cc.crochethk.compilerbau.praktikum;

/** Enum representing registers according to x86-64, Linux System V ABI */
public enum Register {
    rax, rcx, rdx, rbx, rsi, rdi, rsp, rbp, r8, r9, r10, r11, r12, r13, r14, r15;

    @Override
    public String toString() {
        return "%" + super.toString();
    }
}
