package cc.crochethk.klang.visitor.codegen.asm;

import java.util.HashMap;
import java.util.Map;

import utils.Utf8Helper;

/**
 * Base class for data sections providing common interface for generating data
 * section specific directives.
 */
public abstract class DataSection extends SectionBuilder {
    /** Maps each literal constant definition to its label. */
    private Map<String, String> literalsMap = new HashMap<>();

    /**
     * Returns the label. If the value was already defined, writing it is skipped
     * and the label is returned.
     */
    public String createLiteral(String value) {
        final var escapedStr = Utf8Helper.octalEscapeNonAscii(value);
        final var litDefinition = "\n\t.string\t\"" + escapedStr + "\"";
        var label = literalsMap.get(litDefinition);
        if (label == null) {
            label = nextLiteralLabel();
            literalsMap.put(litDefinition, label);
            write("\n", label, ":", litDefinition);
        }
        return label;
    }

    public String createLiteral(double value) {
        final long allBits = Double.doubleToRawLongBits(value);
        final int lowBits = (int) allBits;
        final int highBits = (int) (allBits >> 32);
        final var litDefinition = String.format(
                "\n\t.long\t%d".repeat(2), lowBits, highBits);

        var label = literalsMap.get(litDefinition);
        if (label == null) {
            label = nextLiteralLabel();
            literalsMap.put(litDefinition, label);
            write("\n", label, ":", litDefinition);
        }

        return label;
    }

    /** Counter variable for local variabke lables enumeration */
    private int _literalsCounter = 0;

    private String nextLiteralLabel() {
        var label = litLabelPrefix() + _literalsCounter;
        _literalsCounter += 1;
        return label;
    }

    protected abstract String litLabelPrefix();

    protected DataSection(String entryDirective) {
        super(entryDirective);
    }

    /** Represents the ".data" section */
    public static class WritableData extends DataSection {
        public WritableData() {
            super(".data");
        }

        @Override
        protected String litLabelPrefix() {
            return ".L";
        }
    }

    /** Represents the ".rodata" section */
    public static class ReadOnlyData extends DataSection {
        public ReadOnlyData() {
            super(".section\t.rodata");
        }

        @Override
        protected String litLabelPrefix() {
            return ".LC";
        }
    }

    /** Represents the ".bss" section */
    public static class UninitializedData extends DataSection {
        public UninitializedData() {
            super(".bss");
        }

        @Override
        public String createLiteral(double value) {
            throw new UnsupportedOperationException(
                    "Can't create a literal in .bss section");
        }

        @Override
        protected String litLabelPrefix() {
            return "";
        }
    }
}