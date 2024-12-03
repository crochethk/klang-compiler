import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.antlr.v4.gui.TreeViewer;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import cc.crochethk.compilerbau.praktikum.GenAsm;
import cc.crochethk.compilerbau.praktikum.GenJBC;
import cc.crochethk.compilerbau.praktikum.PrettyPrinter;
import cc.crochethk.compilerbau.praktikum.TypeChecker;
import cc.crochethk.compilerbau.praktikum.ast.Node;
import utils.Result;

public class L1Compiler {
    /** Compiler config that overrides defaults if present */
    static String DOTENV_FILE = "L1Compiler.env";

    // Default compiler config
    // These are overridden by the .env file (if present)
    static boolean VISUALIZE_PARSETREE = false;
    static boolean BUILD_AST = true;
    static boolean PRETTY_PRINT_AST = false;
    static boolean TYPECHECK = true;
    static boolean GENERATE_JBC = true;
    static boolean GENERATE_ASM = true;

    public static Result<Void> compile(Reader inputCode, String outputDir, String fullClassName) throws IOException {
        var compileStatus = Result.Ok;
        var ast = buildAST(inputCode);

        // PrettyPrint
        if (PRETTY_PRINT_AST) {
            var w = ast.accept(new PrettyPrinter());
            System.out.println(w.toString());
        }

        // Type checking
        if (TYPECHECK) {
            ast.accept(new TypeChecker());
        }

        // Java Byte Code generation
        if (GENERATE_JBC) {
            System.out.println("\n--- Generating Java Byte Code ---");
            var codeGenerator = new GenJBC(outputDir, fullClassName);
            ast.accept(codeGenerator);

            if (codeGenerator.exitStatus.isOk()) {
                System.out.println("Success: generated '" + Path.of(outputDir).toString()
                        + fullClassName + ".class'");
            } else {
                System.out.println("Failed: '" + fullClassName + "'");
            }
            compileStatus = compileStatus.isOk() ? codeGenerator.exitStatus : compileStatus;
        } else {
            System.out.println("No JBC generated (disabled).");
        }

        // GNU Assembly generation
        if (GENERATE_ASM) {
            System.out.println("\n--- Generating GNU Assembly Code ---");
            Files.createDirectories(Path.of(outputDir));
            var fileBaseName = fullClassName + ".s";
            var fullFileName = Path.of(outputDir, fileBaseName).toString();
            var codeGenerator = new GenAsm(fullFileName);
            ast.accept(codeGenerator);

            if (codeGenerator.exitStatus.isOk()) {
                System.out.println("Success: generated '" + fullFileName + "'");
            } else {
                System.out.println("Failed: '" + fullClassName + "'");
            }
            compileStatus = compileStatus.isOk() ? codeGenerator.exitStatus : compileStatus;
        } else {
            System.out.println("No assembly generated (disabled).");
        }

        return compileStatus;
    }

    /**
     * Builds the Abstract Syntax Tree from the given input source code and
     * returns its root node.
     */
    private static Node buildAST(Reader inputCode) throws IOException {
        var lexer = applyLexer(inputCode);
        var parser = new L1Parser(new CommonTokenStream(lexer));
        var antlrTree = parser.start();
        if (VISUALIZE_PARSETREE) {
            showAstVisualization(parser, antlrTree);
        }

        if (BUILD_AST) {
            var treeBuilder = new TreeBuilder();
            // var treeBuilder = new TestParseTreeListener();
            ParseTreeWalker.DEFAULT.walk(treeBuilder, antlrTree);
        }
        return antlrTree.result;
    }

    static Lexer applyLexer(Reader inputText) throws IOException {
        return new L1Lexer(CharStreams.fromReader(inputText));
    }

    /**
     * - we assume, the invoker is in the project root folder.
     *      - so fullClassName is inferred from the file's path
     *          (which therefore should be relative)
     * 
     * - either args must be specified as follows 
     *      or a L1Compiler.env must exist containing values for at least
     *          OUTDIR and SOURCEFILE
     * 
     * - Arguments:
     *      - args[0]: output directory for generated files 
     *      - args[1..]: paths to source files __relative to current dir__
     * 
     */
    public static void main(String[] args) throws IOException {
        var dotEnv = utils.Env.readEnvVarFile(DOTENV_FILE);
        VISUALIZE_PARSETREE = Boolean.parseBoolean(
                dotEnv.getProperty("VISUALIZE_PARSETREE", String.valueOf(VISUALIZE_PARSETREE)));
        BUILD_AST = Boolean.parseBoolean(
                dotEnv.getProperty("BUILD_AST", String.valueOf(BUILD_AST)));
        PRETTY_PRINT_AST = Boolean.parseBoolean(
                dotEnv.getProperty("PRETTY_PRINT_AST", String.valueOf(PRETTY_PRINT_AST)));
        TYPECHECK = Boolean.parseBoolean(
                dotEnv.getProperty("TYPECHECK", String.valueOf(TYPECHECK)));
        GENERATE_JBC = Boolean.parseBoolean(
                dotEnv.getProperty("GENERATE_JBC", String.valueOf(GENERATE_JBC)));
        GENERATE_ASM = Boolean.parseBoolean(
                dotEnv.getProperty("GENERATE_ASM", String.valueOf(GENERATE_ASM)));

        switch (args.length) {
            case 0 -> args = new String[] { dotEnv.getProperty("OUTDIR"), dotEnv.getProperty("SOURCEFILE") };
            case 1 -> {
                System.out.println("Error: Expected either 0 or at least 2 arguments");
                System.exit(1);
            }
            default -> {
            }
        }

        var outputDir = args[0];
        var filePaths = Arrays.asList(args).stream().skip(1).map(src -> Path.of(src)).toList();
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
            if (status.isOk()) {
                System.out.println("All compilation tasks finished successfully.");
            } else {
                System.out.println("Errors occured while processing compilation tasks.");
            }
        }
    }

    private static void showAstVisualization(Parser parser, RuleContext tree) {
        // UI AST Visualization
        JFrame frame = new JFrame("Antlr AST");
        JPanel panel = new JPanel();
        TreeViewer viewer = new TreeViewer(Arrays.asList(
                parser.getRuleNames()), tree);
        viewer.setScale(1.5); // Scale a little
        panel.add(viewer);
        frame.add(panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
