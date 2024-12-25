package cc.crochethk.compilerbau.praktikum;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.antlr.v4.gui.TreeViewer;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import cc.crochethk.compilerbau.praktikum.antlr.*;
import cc.crochethk.compilerbau.praktikum.ast.Node;
import cc.crochethk.compilerbau.praktikum.visitor.PrettyPrinter;
import cc.crochethk.compilerbau.praktikum.visitor.TypeChecker;
import cc.crochethk.compilerbau.praktikum.visitor.codegen.GenAsm;
import cc.crochethk.compilerbau.praktikum.visitor.codegen.GenJBC;
import cc.crochethk.compilerbau.praktikum.visitor.codegen.asm.GenCHeader;
import utils.PathUtils;

public class KlangCompiler {
    /** Compiler config that overrides defaults if present */
    static String DOTENV_FILE = "klangc.env";

    // Default compiler config
    // These are overridden by the .env file (if present)
    static boolean VISUALIZE_PARSETREE = false;
    static boolean BUILD_AST = true;
    static boolean PRETTY_PRINT_AST = false;
    static boolean TYPECHECK = true;
    static boolean GENERATE_JBC = true;
    static boolean GENERATE_ASM = true;

    public static void compile(Reader inputCode, String outputDir, String packageName, String className)
            throws IOException {
        var ast = buildAST(inputCode);
        var indent = " ".repeat(4);

        // PrettyPrint
        if (PRETTY_PRINT_AST) {
            var pp = new PrettyPrinter();
            ast.accept(new PrettyPrinter());
            System.out.println(pp.writer.toString());
        }

        // Type checking
        if (TYPECHECK) {
            ast.accept(new TypeChecker());
        }

        // Java Byte Code generation
        System.out.println("Java Byte Code:");
        if (GENERATE_JBC) {
            var codeGenerator = new GenJBC(outputDir, packageName, className);
            printGeneratingFilesMessage(indent, codeGenerator.outFilePaths());

            runWithSuccessCheck(() -> ast.accept(codeGenerator), indent);

        } else {
            System.out.println(indent + "No JBC generated (disabled).");
        }
        System.out.println();

        // GNU Assembly generation
        System.out.println("GNU Assembly Code:");
        if (GENERATE_ASM) {
            var codeGenerator = new GenAsm(outputDir, packageName, className);
            printGeneratingFilesMessage(indent, codeGenerator.outFilePaths());
            ast.accept(codeGenerator);

            // Generate C helper files
            var headerGen = new GenCHeader(outputDir, packageName, className);
            printGeneratingFilesMessage(indent, headerGen.outFilePaths());
            ast.accept(headerGen);
        } else {
            System.out.println(indent + "No assembly generated (disabled).");
        }
        System.out.println();
    }

    private static void printGeneratingFilesMessage(String indent, List<Path> paths) {
        StringBuilder sb = new StringBuilder();
        sb.append(indent + "Generating...");
        for (var p : paths) {
            sb.append("\n" + indent.repeat(2) + "'" + p + "'");
        }
        System.out.println(sb.toString());
    }

    /**
     * Builds the Abstract Syntax Tree from the given input source code and
     * returns its root node.
     */
    private static Node buildAST(Reader inputCode) throws IOException {
        var lexer = applyLexer(inputCode);
        var parser = new KlangParser(new CommonTokenStream(lexer));
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
        return new KlangLexer(CharStreams.fromReader(inputText));
    }

    /**
     * - we assume, the invoker is in the source root folder.
     *      - so fullClassName is inferred from the file's relative path
     * 
     * - either args must be specified as follows 
     *      or a klangc.env must exist containing values for at least
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

            try {
                compile(reader, outputDir, packageName, className);
                reader.close();
                System.out.println("All tasks finished successfully.");
            } catch (Exception e) {
                System.out.println("Errors occured while processing compilation tasks.");
                System.err.println(e.getMessage());
                System.exit(1);
            }
        }
    }

    private static void runWithSuccessCheck(Runnable callable, String msgIndent) {
        try {
            callable.run();
            System.out.println(msgIndent + "Success!");
        } catch (RuntimeException e) {
            System.out.println(msgIndent + "Failed!");
            throw e;
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
