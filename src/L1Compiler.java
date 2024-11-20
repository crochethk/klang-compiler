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
    static String DEFAULT_OUTDIR = "gen/L1CompilerJBC";
    static String DEFAULT_SOURCEFILE = "tests/CalcAverage.l1";

    public static GenJBC.Status compile(Reader inputCode, String outputDir, String fullClassName) throws IOException {
        var ast = buildAST(inputCode);

        // Type checking
        ast.accept(new TypeChecker());

        // Code generation
        var codeGenerator = new GenJBC(outputDir, fullClassName);
        ast.accept(codeGenerator);
        return codeGenerator.status;
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

            var fileNameNoExt = fp.getFileName().toString();
            var extIdx = fileNameNoExt.lastIndexOf('.');
            fileNameNoExt = fileNameNoExt.substring(0, extIdx > 0 ? extIdx : 0);
            var fpBase = fp.getParent();
            fpBase = fpBase != null ? fpBase : Path.of("");

            String fullClassName = fpBase
                    .resolve(fileNameNoExt)
                    .toString()
                    .replace(File.separator, ".");
            var status = compile(reader, outputDir, fullClassName);

            var msg = switch (status) {
                case GenJBC.Status.Success -> "Success: '"
                        + Path.of(outputDir, fpBase.toString(), fileNameNoExt).toString()
                        + ".class'";
                case GenJBC.Status.Failure -> "Failed: '" + fp.toString() + "'";
                case GenJBC.Status.Ready -> "Error: somehow did not even run?";
            };
            System.out.println(msg);
        }
    }
}
