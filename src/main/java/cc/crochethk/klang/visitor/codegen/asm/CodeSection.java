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
        writeSrcDstInstruction("movq", src, dst);
    }

    public void pushq(OperandSpecifier source) {
        writeIndented("pushq", "\t", source.operandSpec());
    }

    public void call(String name) {
        writeIndented("call", "\t", name);
    }

    public void leave() {
        writeIndented("leave");
    }

    public void ret() {
        writeIndented("ret");
    }

    /** Calculate an effective address and put it into {@code dst} register */
    public void leaq(MemAddr src, Register dst) {
        writeSrcDstInstruction("leaq", src, dst);
    }

    /** Add {@code src} to {@code dst} */
    public void addq(OperandSpecifier src, OperandSpecifier dst) {
        writeSrcDstInstruction("addq", src, dst);
    }

    /** Subtract {@code src} from {@code dst} */
    public void subq(OperandSpecifier src, OperandSpecifier dst) {
        writeSrcDstInstruction("subq", src, dst);
    }

    /** Multiply {@code dst} by {@code src} */
    public void imulq(OperandSpecifier src, OperandSpecifier dst) {
        writeSrcDstInstruction("imulq", src, dst);
    }

    /** Divide {@code rax:rdx} by {@code src} */
    public void idivq(OperandSpecifier src) {
        writeIndented("idivq", "\t", src.operandSpec());
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
        writeSrcDstInstruction("movsd", src, dst);
    }

    /** Add {@code src} to {@code dst} */
    public void addsd(OperandSpecifier src, OperandSpecifier dst) {
        writeSrcDstInstruction("addsd", src, dst);
    }

    /** Subtract {@code src} from {@code dst} */
    public void subsd(OperandSpecifier src, OperandSpecifier dst) {
        writeSrcDstInstruction("subsd", src, dst);
    }

    /** Multiply {@code dst} by {@code src} */
    public void mulsd(OperandSpecifier src, OperandSpecifier dst) {
        writeSrcDstInstruction("mulsd", src, dst);
    }

    /** Divide {@code dst} by {@code src} */
    public void divsd(OperandSpecifier src, OperandSpecifier dst) {
        writeSrcDstInstruction("divsd", src, dst);
    }

    private void writeSrcDstInstruction(String instr, OperandSpecifier src, OperandSpecifier dst) {
        writeIndented(instr, "\t", src.operandSpec(), ", ", dst.operandSpec());
    }
}