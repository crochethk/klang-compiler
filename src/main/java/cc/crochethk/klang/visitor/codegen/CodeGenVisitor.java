package cc.crochethk.klang.visitor.codegen;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import cc.crochethk.klang.visitor.Visitor;

public abstract class CodeGenVisitor implements Visitor {
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
        try {
            Files.createDirectories(Path.of(outputDir));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
    * Returns a list of paths of the generated files. Note that this does _not_ imply
    * whether the files actually exists or generating code succeeded.
    */
    public abstract List<Path> outFilePaths();
}