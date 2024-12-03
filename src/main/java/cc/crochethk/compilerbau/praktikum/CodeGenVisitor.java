package cc.crochethk.compilerbau.praktikum;

import utils.Result;

public abstract class CodeGenVisitor<T> implements Visitor<T> {
    /**
    * The status after code generation finished.
    * Might be null if visiting started in an inappropriate Node.
    */
    public Result<Void> exitStatus = null;

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
    public abstract String outFilePath();
}