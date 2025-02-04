package cc.crochethk.klang.visitor.codegen.asm;

import cc.crochethk.klang.visitor.codegen.asm.OperandSpecifier.MemAddr;
import cc.crochethk.klang.visitor.codegen.asm.OperandSpecifier.Register;

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