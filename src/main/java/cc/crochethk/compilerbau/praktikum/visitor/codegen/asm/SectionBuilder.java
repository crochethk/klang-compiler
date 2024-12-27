package cc.crochethk.compilerbau.praktikum.visitor.codegen.asm;

import cc.crochethk.compilerbau.praktikum.visitor.SourceCodeBuilder;

/**
 * Base class representing a section of the program (e.g. ".text").
 * 
 * The current result can be retrieved using "toString()".
 */
public abstract class SectionBuilder extends SourceCodeBuilder {
    public SectionBuilder(String entryDirective) {
        super("\t");
    }
}
