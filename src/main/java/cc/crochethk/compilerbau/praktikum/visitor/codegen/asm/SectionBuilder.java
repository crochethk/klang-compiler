package cc.crochethk.compilerbau.praktikum.visitor.codegen.asm;

/**
 * Base class representing a section of the program (e.g. ".text").
 * 
 * The current state can be retrieved using "toString()".
 */
public abstract class SectionBuilder {
    private StringBuilder buffer = new StringBuilder();
    private boolean isEmpty;

    public SectionBuilder(String entryDirective) {
        this.buffer = new StringBuilder();
        writeIndented(entryDirective);
        this.isEmpty = true;
    }

    /** Writes multiple instruction strings, each into a indented line */
    public void writeIndented(String... ss) {
        write("\n\t");
        for (var s : ss)
            write(s);
    }

    /** Writes the instruction string in a new indented line. */
    public void writeIndented(String s) {
        write("\n\t", s);
    }

    /** Writes the given instruction strings. */
    public void write(String... ss) {
        for (var s : ss)
            write(s);
    }

    public void write(String s) {
        buffer.append(s);
        isEmpty = false;
    }

    public boolean isEmpty() {
        return isEmpty;
    }

    @Override
    public String toString() {
        return buffer.toString();
    }
}
