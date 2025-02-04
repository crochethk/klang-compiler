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

        public enum Align {
            None(-1), _4(4), _8(8), _16(16);

            int bytes;

            Align(int bytes) {
                this.bytes = bytes;
            }
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
            return createLiteralData32(new int[] { lowBits, highBits }, Align._8);
        }

        /**
         * Create literal constant of arbitrary data represented by the 
         * provided 32-bit signed integer array and align it to
         * {@code alignment} bytes.
         * @return The label pointing to the beginning of the data.
         */
        public String createLiteralData32(int[] data, Align alignment) {
            var sb = new StringBuilder();
            for (int d : data) {
                sb.append("\n\t.long\t");
                sb.append(d);
            }
            var litDef = sb.toString();
            return getOrCreateLabel(litDef, alignment);
        }

        private String getOrCreateLabel(final String litDefinition) {
            return getOrCreateLabel(litDefinition, Align.None);
        }

        /**
         * @param litDefinition The literal data definition.
         * @param alignment An optional byte alignment value (e.g. 8 or 16).
         */
        private String getOrCreateLabel(final String litDefinition, Align alignment) {
            var alignDirective = alignment != Align.None
                    ? "\n\t.align\t" + alignment.bytes
                    : "";
            var labelKey = alignDirective + litDefinition;
            var label = literalsMap.get(labelKey);
            if (label == null) {
                label = labelMgr.getNext();
                literalsMap.put(labelKey, label);

                write(alignDirective);
                write("\n", label, ":", litDefinition);
            }
            return label;
        }
    }
}