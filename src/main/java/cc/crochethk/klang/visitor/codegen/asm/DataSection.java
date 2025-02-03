package cc.crochethk.klang.visitor.codegen.asm;

import java.util.HashMap;
import java.util.Map;

import utils.Utf8Helper;

/**
 * Base class for data sections providing common interface for generating data
 * section specific directives.
 */
public abstract class DataSection extends SectionBuilder {
    protected DataSection(String entryDirective) {
        super(entryDirective);
    }

    /** Represents the ".data" section */
    public static class WritableData extends DataSection {
        public WritableData() {
            super(".data");
        }
    }

    /** Represents the ".rodata" section */
    public static class ReadOnlyData extends DataSection {
        /** Generates labels for local literal constants. */
        private LocalLabelManager labelMgr;

        /** Maps each literal constant definition to its label. */
        private Map<String, String> literalsMap = new HashMap<>();

        public ReadOnlyData() {
            super(".section\t.rodata");
            labelMgr = new LocalLabelManager(".LC");
        }

        /**
        * Returns the label. If the value is already defined, writing it is 
        * skipped and the existing label is returned.
        */
        public String createLiteral(String value) {
            final var escapedStr = Utf8Helper.octalEscapeNonAscii(value);
            final var litDefinition = "\n\t.string\t\"" + escapedStr + "\"";
            return getOrCreateLabel(litDefinition);
        }

        public String createLiteral(double value) {
            final long allBits = Double.doubleToRawLongBits(value);
            final int lowBits = (int) allBits;
            final int highBits = (int) (allBits >> 32);
            final var litDefinition = String.format(
                    "\n\t.long\t%d".repeat(2), lowBits, highBits);
            return getOrCreateLabel(litDefinition);
        }

        private String getOrCreateLabel(final String litDefinition) {
            var label = literalsMap.get(litDefinition);
            if (label == null) {
                label = labelMgr.getNext();
                literalsMap.put(litDefinition, label);
                write("\n", label, ":", litDefinition);
            }
            return label;
        }
    }
}