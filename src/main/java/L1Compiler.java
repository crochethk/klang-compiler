import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
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

import cc.crochethk.compilerbau.praktikum.ast.Node;
import cc.crochethk.compilerbau.praktikum.visitor.PrettyPrinter;
import cc.crochethk.compilerbau.praktikum.visitor.TypeChecker;
import cc.crochethk.compilerbau.praktikum.visitor.codegen.GenAsm;
import cc.crochethk.compilerbau.praktikum.visitor.codegen.GenCHeader;
import cc.crochethk.compilerbau.praktikum.visitor.codegen.GenJBC;
import utils.PathUtils;

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

    public static int compile(Reader inputCode, String outputDir, String packageName, String className)
            throws IOException {
        var indent = " ".repeat(4);
        var ast = buildAST(inputCode);

        var errorCount = 0; // TODO <----------- count errors when catching exceptions of visitors

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
        System.out.println("Java Byte Code:");
        if (GENERATE_JBC) {
            var codeGenerator = new GenJBC(outputDir, packageName, className);
            System.out.println(indent + "Generating '" + codeGenerator.outFilePath() + "'...");
            ast.accept(codeGenerator);

            // if (codeGenerator.exitStatus.isOk()) {
            //     System.out.println(indent + "Success!");
            // } else {
            //     System.out.println(indent + "Failed!");
            // }
        } else {
            System.out.println(indent + "No JBC generated (disabled).");
        }
        System.out.println();

        // GNU Assembly generation
        System.out.println("GNU Assembly Code:");
        if (GENERATE_ASM) {
            var codeGenerator = new GenAsm(outputDir, packageName, className);
            System.out.println(indent + "Generating '" + codeGenerator.outFilePath() + "'...");
            ast.accept(codeGenerator);

            // if (codeGenerator.exitStatus.isOk()) {
            //     System.out.println(indent + "Success!");
            // } else {
            //     System.out.println(indent + "Failed!");
            // }

            // Generate header
            var headerGen = new GenCHeader(outputDir, packageName, className);
            System.out.println(indent + "Generating '" + headerGen.outFilePath() + "'...");
            ast.accept(headerGen);
            // if (headerGen.exitStatus.isOk()) {
            //     System.out.println(indent + "Success!");
            // } else {
            //     System.out.println(indent + "Failed!");
            // }
        } else {
            System.out.println(indent + "No assembly generated (disabled).");
        }
        System.out.println();

        return errorCount;
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
     * - we assume, the invoker is in the source root folder.
     *      - so fullClassName is inferred from the file's relative path
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

            var fpBase = PathUtils.getParentOrEmpty(fp);
            var packageName = fpBase.toString().replace(File.separator, ".");
            var className = PathUtils.getFileNameNoExt(fp);

            var errorCount = compile(reader, outputDir, packageName, className);
            if (errorCount != 0) {
                System.out.println("Errors occured while processing compilation tasks.");
            } else {
                System.out.println("All tasks finished successfully.");
            }
            reader.close();
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
