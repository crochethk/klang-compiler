package cc.crochethk.compilerbau.praktikum;

/** Enum representing registers according to x86-64, Linux System V ABI */
public enum Register {
    rax("%rax"),
    rcx("%rcx"),
    rdx("%rdx"),
    rbx("%rbx"),
    rsi("%rsi"),
    rdi("%rdi"),
    rsp("%rsp"),
    rbp("%rbp"),
    r8("%r8"),
    r9("%r9"),
    r10("%r10"),
    r11("%r11"),
    r12("%r12"),
    r13("%r13"),
    r14("%r14"),
    r15("%r15");

    String name;

    Register(String regName) {
        name = regName;
    }

    @Override
    public String toString() {
        return name;
    }
}
