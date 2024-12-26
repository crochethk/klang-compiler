package cc.crochethk.compilerbau.praktikum.visitor;

/**
 * Base class for writing string based source code.
 * The indentation symbol can be customized and indentation level is managed.
 * Methods are provided to for writing one or more strings at once, with or
 * without indentation.
 * 
 * The result can be retrieved using "toString()".
 */
public class SourceCodeBuilder {
    private StringBuilder buffer;
    private String indentSequence;
    protected int indentLvl;
    private boolean isEmpty;

    public void increaseIndent() {
        indentLvl++;
    }

    public void decreaseIndent() {
        indentLvl = indentLvl > 0 ? indentLvl - 1 : 0;
    }

    /**
     * @param indentSequence The string to use for indentations
     */
    public SourceCodeBuilder(String indentSequence) {
        this(indentSequence, 1);
    }

    public SourceCodeBuilder(String indentSequence, int initialIndentLevel) {
        this.buffer = new StringBuilder();
        this.indentSequence = indentSequence;
        this.indentLvl = initialIndentLevel;
        this.isEmpty = true;
    }

    private String indent() {
        return indentSequence.repeat(indentLvl);
    }

    /** Writes multiple strings into a new indented line */
    public void writeIndented(String... ss) {
        write("\n", indent());
        for (var s : ss)
            write(s);
    }

    /** Writes the string in a new indented line. */
    public void writeIndented(String s) {
        write("\n", indent(), s);
    }

    /** Writes a new line with indentation only. */
    public void writeIndent() {
        write("\n", indent());
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
