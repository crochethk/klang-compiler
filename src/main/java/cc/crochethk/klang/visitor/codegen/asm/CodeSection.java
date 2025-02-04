package cc.crochethk.klang.visitor.codegen.asm;

import cc.crochethk.klang.visitor.codegen.asm.OperandSpecifier.MemAddr;
import cc.crochethk.klang.visitor.codegen.asm.OperandSpecifier.Register;
import cc.crochethk.klang.visitor.codegen.asm.OperandSpecifier.Register.ByteRegister;
import cc.crochethk.klang.visitor.codegen.asm.OperandSpecifier.XmmRegister;

/**
 * SectionBuilder representing the ".text" section of the assembly and providing
 * a convenient interface for generating instructions.
 * 
 * Arguments for most instructions must implement the OperandSpecifier interface,
 * providing a valid Register, Immediate or Memoryaddress string.
 */
public class CodeSection extends SectionBuilder {
    public CodeSection() {
        super(".text");
    }

    public void movq(OperandSpecifier src, OperandSpecifier dst) {
        writeInstruction("movq", src, dst);
    }

    public void pushq(OperandSpecifier source) {
        writeInstruction("pushq", source);
    }

    public void call(String name) {
        writeInstruction("call", name);
    }

    public void leave() {
        writeIndented("leave");
    }

    public void ret() {
        writeIndented("ret");
    }

    /** Calculate an effective address and put it into {@code dst} register */
    public void leaq(MemAddr src, Register dst) {
        writeInstruction("leaq", src, dst);
    }

    /** Add {@code src} to {@code dst} */
    public void addq(OperandSpecifier src, OperandSpecifier dst) {
        writeInstruction("addq", src, dst);
    }

    /** Subtract {@code src} from {@code dst} */
    public void subq(OperandSpecifier src, OperandSpecifier dst) {
        writeInstruction("subq", src, dst);
    }

    /** Multiply {@code dst} by {@code src} */
    public void imulq(OperandSpecifier src, OperandSpecifier dst) {
        writeInstruction("imulq", src, dst);
    }

    /** Divide {@code rax:rdx} by {@code src} */
    public void idivq(OperandSpecifier src) {
        writeInstruction("idivq", src);
    }

    /** Sign-extend {@code rax} into {@code rdx} */
    public void cqto() {
        writeIndented("cqto");
    }

    /**
     * Two's complement negate {@code dst}. Equivalent to {@code dst = 0 - dst}
     * @param dst A general-purpose register or a memory location.
     */
    public void negq(OperandSpecifier dst) {
        writeInstruction("negq", dst);
    }

    /** Bitwise AND similar to {@code dst = dst & src} */
    public void andq(OperandSpecifier src, OperandSpecifier dst) {
        writeInstruction("andq", src, dst);
    }

    /** Bitwise OR similar to {@code dst = dst | src} */
    public void orq(OperandSpecifier src, OperandSpecifier dst) {
        writeInstruction("orq", src, dst);
    }

    /** Bitwise XOR similar to {@code dst = dst ^ src} */
    public void xorq(OperandSpecifier src, OperandSpecifier dst) {
        writeInstruction("xorq", src, dst);
    }

    /** Set condition codes (e.g. ZF for jmp) according to {@code src1 - src2} */
    public void cmpq(OperandSpecifier src2, OperandSpecifier src1) {
        writeInstruction("cmpq", src2, src1);
    }

    public void jmp(String label) {
        writeInstruction("jmp", label);
    }

    public void je(String label) {
        writeInstruction("je", label);
    }

    // --------------------[ conditional byte set instruction ]-----------------
    /**
     * Set 1 if CCs indicate 'equal'/'zero'.
     * Use e.g. {@code cmpq} to set CCc accordingly beforhand.
     */
    public void sete(ByteRegister byteReg) {
        writeInstruction("sete", byteReg);
    }

    /**
     * Set 1 if CCs indicate 'notequal'/'nonzero'.
     * Use e.g. {@code cmpq} to set CCc accordingly beforhand.
     */
    public void setne(ByteRegister byteReg) {
        writeInstruction("setne", byteReg);
    }

    /**
     * Set 1 if CCs indicate 'greater' (signed).
     * Use e.g. {@code cmpq} to set CCc accordingly beforhand.
     */
    public void setg(ByteRegister byteReg) {
        writeInstruction("setg", byteReg);
    }

    /**
     * Set 1 if CCs indicate 'greater or equal' (signed).
     * Use e.g. {@code cmpq} to set CCc accordingly beforhand.
     */
    public void setge(ByteRegister byteReg) {
        writeInstruction("setge", byteReg);
    }

    /**
     * Set 1 if CCs indicate 'less' (signed).
     * Use e.g. {@code cmpq} to set CCc accordingly beforhand.
     */
    public void setl(ByteRegister byteReg) {
        writeInstruction("setl", byteReg);
    }

    /**
     * Set 1 if CCs indicate 'less or equal'.
     * Use e.g. {@code cmpq} to set CCc accordingly beforhand.
     */
    public void setle(ByteRegister byteReg) {
        writeInstruction("setle", byteReg);
    }

    // --------------------[ XMM based instructions ]---------------------------
    /**
     * Move scalar double-precision floating-point value from soruce to destination.
     * @param source
     * @param destination The destination XmmRegister or MemAddress
     */
    public void movsd(OperandSpecifier src, OperandSpecifier dst) {
        writeInstruction("movsd", src, dst);
    }

    /** Add {@code src} to {@code dst} */
    public void addsd(OperandSpecifier src, OperandSpecifier dst) {
        writeInstruction("addsd", src, dst);
    }

    /** Subtract {@code src} from {@code dst} */
    public void subsd(OperandSpecifier src, OperandSpecifier dst) {
        writeInstruction("subsd", src, dst);
    }

    /** Multiply {@code dst} by {@code src} */
    public void mulsd(OperandSpecifier src, OperandSpecifier dst) {
        writeInstruction("mulsd", src, dst);
    }

    /** Divide {@code dst} by {@code src} */
    public void divsd(OperandSpecifier src, OperandSpecifier dst) {
        writeInstruction("divsd", src, dst);
    }

    /**
     * Bitwise logical XOR of packed double precision floating-point values:
     * {@code dst = dst ^ src}
     */
    public void xorpd(OperandSpecifier src, XmmRegister dst) {
        writeInstruction("xorpd", src, dst);
    }

    // -------------------------------------------------------------------------

    private void writeInstruction(String instr, OperandSpecifier src, OperandSpecifier dst) {
        writeIndented(instr, "\t", src.operandSpec(), ", ", dst.operandSpec());
    }

    private void writeInstruction(String instr, OperandSpecifier singleOperand) {
        writeIndented(instr, "\t", singleOperand.operandSpec());
    }

    private void writeInstruction(String instr, String label) {
        writeIndented(instr, "\t", label);
    }
}