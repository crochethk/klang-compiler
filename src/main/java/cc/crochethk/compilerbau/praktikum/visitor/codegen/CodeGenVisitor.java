package cc.crochethk.compilerbau.praktikum.visitor.codegen;

import java.nio.file.Path;

import cc.crochethk.compilerbau.praktikum.visitor.Visitor;

public abstract class CodeGenVisitor<T> implements Visitor<T> {
    protected String outDir;
    protected String packageName;
    protected String className;

    /**
     * @param outputDir The path to the directory generated files will be written to.
     * @param packageName The package the generated code should be member of,
     *      for example {@code com.example}. Might be the empty String.
     * @param className The name of the generated class, for example '{@code MyClass}'.
     */
    protected CodeGenVisitor(String outputDir, String packageName, String className) {
        this.outDir = outputDir;
        this.packageName = packageName;
        this.className = className;
    }

    /**
    * Returns the path of the generated file. Note that this does _not_ imply
    * whether the file actually exists or generating code succeeded.
    */
    public abstract Path outFilePath();
}