package cc.crochethk.klang.visitor.codegen.asm;

import java.util.concurrent.atomic.AtomicInteger;

import cc.crochethk.klang.visitor.SourceCodeBuilder;

/**
 * Base class representing a section of the program (e.g. ".text").
 * 
 * The current result can be retrieved using "toString()".
 */
public abstract class SectionBuilder extends SourceCodeBuilder {
    /**
     * @param entryDirective For example {@code .data} or {@code .text}
     */
    public SectionBuilder(String entryDirective) {
        super("\t");
    }

    /** Represents the ".bss" section */
    public static class BssSection extends SectionBuilder {
        public BssSection() {
            super(".bss");
        }
    }

    protected class LocalLabelManager {
        private final String labelPrefix;
        private final AtomicInteger labelCounter = new AtomicInteger(0);

        /**
         * @param prefix The string to use as prefix in generated local labels.
         */
        public LocalLabelManager(String prefix) {
            this.labelPrefix = prefix;
        }

        public String getNext() {
            return labelPrefix + labelCounter.getAndIncrement();
        }
    }
}
