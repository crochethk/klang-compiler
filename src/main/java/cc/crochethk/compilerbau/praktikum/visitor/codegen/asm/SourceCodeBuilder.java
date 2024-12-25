package cc.crochethk.compilerbau.praktikum.visitor.codegen.asm;

/**
 * Base class for writing string based source code.
 * The result can be retrieved using "toString()".
 */
public class SourceCodeBuilder {
    private StringBuilder buffer;
    private String indent;
    private boolean isEmpty;

    /**
     * @param indentSequence The string to use for indentations
     */
    public SourceCodeBuilder(String indentSequence) {
        this.buffer = new StringBuilder();
        this.indent = indentSequence;
        this.isEmpty = true;
    }

    /** Writes multiple strings into a new indented line */
    public void writeIndented(String... ss) {
        write("\n", indent);
        for (var s : ss)
            write(s);
    }

    /** Writes the string in a new indented line. */
    public void writeIndented(String s) {
        write("\n", indent, s);
    }

    /** Writes the given strings. */
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
