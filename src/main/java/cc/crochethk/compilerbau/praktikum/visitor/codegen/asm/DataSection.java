package cc.crochethk.compilerbau.praktikum.visitor.codegen.asm;

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
        public ReadOnlyData() {
            super(".section\t.rodata");
        }
    }

    /** Represents the ".bss" section */
    public static class UninitializedData extends DataSection {
        public UninitializedData() {
            super(".bss");
        }
    }

    // TODO add functionality to generate data section specific directives
}