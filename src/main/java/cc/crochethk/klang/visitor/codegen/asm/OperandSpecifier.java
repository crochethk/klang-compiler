package cc.crochethk.klang.visitor.codegen.asm;

public interface OperandSpecifier {
    String operandSpec();

    /** Enum representing general purpose registers according to x86-64, Linux System V ABI */
    public enum Register implements OperandSpecifier {
        /** Return value */
        rax,
        /** Argument 4 */
        rcx,
        /** Argument 3 */
        rdx,
        /** <b>Callee</b> saved */
        rbx,
        /** Argument 2 */
        rsi,
        /** Argument 1 */
        rdi,
        /** Stack pointer */
        rsp,
        /** Base Pointer, <b>Callee</b> saved */
        rbp,
        /** Argument 5 */
        r8,
        /** Argument 6 */
        r9,
        /** Scratch reg */
        r10,
        /** Scratch reg */
        r11,
        /** <b>Callee</b> saved */
        r12,
        /** <b>Callee</b> saved */
        r13,
        /** <b>Callee</b> saved */
        r14,
        /** <b>Callee</b> saved */
        r15,
        /** The instruction pointer */
        rip;

        @Override
        public String operandSpec() {
            return "%" + this;
        }
    }

    /**
     * Enum representing the SIMD registers used in SSE/AVX instruction sets
     * (among others for floating point operations)
     */
    public enum XmmRegister implements OperandSpecifier {
        xmm0, xmm1, xmm2, xmm3, xmm4, xmm5, xmm6, xmm7, xmm8, xmm9, xmm10, xmm11,
        xmm12, xmm13, xmm14, xmm15;

        @Override
        public String operandSpec() {
            return "%" + this;
        }
    }

    public record Const(long value) implements OperandSpecifier {
        /** Alias for new Const(value) */
        public static Const $(long value) {
            return new Const(value);
        }

        @Override
        public String operandSpec() {
            return "$" + value();
        }
    }

    sealed interface MemoryOperandSpecifier extends OperandSpecifier permits RawMemAddr, MemAddr {
    }

    public record RawMemAddr(int address) implements MemoryOperandSpecifier {
        @Override
        public String operandSpec() {
            return address() + "";
        }
    }

    /** Memory Address using the "<offset>(<base>, <index>, <scale>)" operand syntax */
    public final class MemAddr implements MemoryOperandSpecifier {
        private String offset;
        private Register base, index;
        private Scale s;

        /** Use the address inside the specified register */
        public MemAddr(Register base) {
            this("", base);
        }

        public MemAddr(int offset, Register base) {
            this(offset + "", base);
        }

        public MemAddr(String offset, Register base) {
            this(offset, base, null, null);
        }

        public MemAddr(int offset, Register base, Register index, Scale s) {
            this(offset + "", base, index, s);
        }

        public MemAddr(String offset, Register base, Register index, Scale s) {
            this.offset = offset + "";
            this.base = base;
            this.index = index;
            this.s = s;
        }

        public enum Scale {
            _1(1), _2(2), _4(4), _8(8);

            final int factor;

            private Scale(int f) {
                this.factor = f;
            }
        }

        @Override
        public String operandSpec() {
            // return offset + "(" + base.operandSpec() + "," + index.operandSpec() + "," + s.factor + ")";
            return offset + "(" + base.operandSpec()
                    + (index != null
                            ? "," + index.operandSpec() + "," + s.factor
                            : "")
                    + ")";
        }
    }
}
