import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.util.Arrays;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import cc.crochethk.compilerbau.praktikum.GenJBC;
import cc.crochethk.compilerbau.praktikum.TypeChecker;
import cc.crochethk.compilerbau.praktikum.ast.Node;

public class L1Compiler {
    static String DEFAULT_OUTDIR = "out_jbc";
    static String DEFAULT_SOURCEFILE = "tests/CalcAverage.l1";

    public static void compile(Reader inputCode, String outputDir, String fullClassName) throws IOException {
        var ast = buildAST(inputCode);

        // Type checking
        ast.accept(new TypeChecker());

        // Code generation
        ast.accept(new GenJBC(outputDir, fullClassName));
    }

    /**
     * Builds the Abstract Syntax Tree from the given input source code and
     * returns its root node.
     */
    private static Node buildAST(Reader inputCode) throws IOException {
        var lexer = new L1Lexer(CharStreams.fromReader(inputCode));
        var parser = new L1Parser(new CommonTokenStream(lexer));
        var antlrTree = parser.start();
        ParseTreeWalker.DEFAULT.walk(new TreeBuilder(), antlrTree);
        return antlrTree.result;
    }

    /**
     * - we assume, the invoker is in the project root folder.
     *      - so fullClassName is inferred from the file's path
     *          (which therefore should be relative)
     * 
     * - Arguments:
     *      - args[0]: output directory for generated files 
     *      - args[1..]: paths to source files __relative to current dir__
     * 
     */
    public static void main(String[] args) throws IOException {
        args = switch (args.length) {
            case 0 -> new String[] { DEFAULT_OUTDIR, DEFAULT_SOURCEFILE };
            case 1 -> new String[] { args[0], DEFAULT_SOURCEFILE };
            default -> args;
        };

        var outputDir = args[0];
        var filePaths = Arrays.asList(args).stream().skip(1).map(sf -> Path.of(sf)).toList();
        for (var fp : filePaths) {
            var file = fp.toFile();
            var reader = new FileReader(file);
            String fullClassName = fp.toString().replace(File.separator, ".");
            compile(reader, outputDir, fullClassName);
        }
    }
}
