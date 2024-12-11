package cc.crochethk.compilerbau.praktikum.visitor.codegen.asm;

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

    public void movq(OperandSpecifier source, OperandSpecifier destination) {
        writeIndented("movq", "\t", source.operandSpec(), ", ", destination.operandSpec());
    }

    public void pushq(OperandSpecifier source) {
        writeIndented("pushq", "\t", source.operandSpec());
    }

    /**
     * Move scalar double-precision floating-point value from soruce to destination.
     * @param source
     * @param destination The destination XmmRegister or MemAddress
     */
    public void movsd(OperandSpecifier source, OperandSpecifier destination) {
        writeIndented("movsd", "\t", source.operandSpec(), ", ", destination.operandSpec());
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

    /** Add source to destination */
    public void addq(OperandSpecifier src, OperandSpecifier dst) {
        writeIndented("addq", "\t", src.operandSpec(), ", ", dst.operandSpec());
    }

    /** Subtract source from destination */
    public void subq(OperandSpecifier src, OperandSpecifier dst) {
        writeIndented("subq", "\t", src.operandSpec(), ", ", dst.operandSpec());
    }

    /** Multiply destination by source */
    public void imulq(OperandSpecifier src, OperandSpecifier dst) {
        writeIndented("imulq", "\t", src.operandSpec(), ", ", dst.operandSpec());
    }
}