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
import cc.crochethk.compilerbau.praktikum.visitor.codegen.asm.GenCHelpers;
import utils.PathUtils;

public class KlangCompiler {
    // Default compiler args
    static String DEFAULT_OUT_DIR = "./out";
    static boolean DEFAULT_VISUALIZE_PARSETREE = false;
    static boolean DEFAULT_BUILD_AST = true;
    static boolean DEFAULT_PRETTY_PRINT_AST = false;
    static boolean DEFAULT_TYPECHECK = true;
    static boolean DEFAULT_GENERATE_JBC = true;
    static boolean DEFAULT_GENERATE_ASM = true;

    private String outputDir;
    private boolean visualizeParseTree;
    private boolean buildAst;
    private boolean prettyPrintAst;
    private boolean typeCheck;
    private boolean generateJbc;
    private boolean generateAsm;

    public KlangCompiler(
            String outputDir,
            boolean visualizeParseTree,
            boolean buildAst,
            boolean prettyPrintAst,
            boolean typeCheck,
            boolean generateJbc,
            boolean generateAsm) {
        this.outputDir = outputDir;
        this.visualizeParseTree = visualizeParseTree;
        this.buildAst = buildAst;
        this.prettyPrintAst = prettyPrintAst;
        this.typeCheck = typeCheck;
        this.generateJbc = generateJbc;
        this.generateAsm = generateAsm;
    }

    public void compile(Reader inputCode, String packageName, String className)
            throws IOException {
        var ast = buildAst(inputCode);
        var indent = " ".repeat(4);

        // PrettyPrint
        if (prettyPrintAst) {
            var pp = new PrettyPrinter();
            ast.accept(new PrettyPrinter());
            System.out.println(pp.writer.toString());
        }

        // Type checking
        if (typeCheck) {
            ast.accept(new TypeChecker());
        }

        // Java Byte Code generation
        System.out.println("Java Byte Code:");
        if (generateJbc) {
            var codeGenerator = new GenJBC(outputDir, packageName, className);
            printGeneratingFilesMessage(indent, codeGenerator.outFilePaths());

            runWithSuccessCheck(() -> ast.accept(codeGenerator), indent);

        } else {
            System.out.println(indent + "No JBC generated (disabled).");
        }
        System.out.println();

        // GNU Assembly generation
        System.out.println("GNU Assembly Code:");
        if (generateAsm) {
            var codeGenerator = new GenAsm(outputDir, packageName, className);
            printGeneratingFilesMessage(indent, codeGenerator.outFilePaths());
            ast.accept(codeGenerator);

            // Generate C helper files
            var cHelpersGen = new GenCHelpers(outputDir, packageName, className);
            printGeneratingFilesMessage(indent, cHelpersGen.outFilePaths());
            ast.accept(cHelpersGen);
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
    private Node buildAst(Reader inputCode) throws IOException {
        var lexer = applyLexer(inputCode);
        var parser = new KlangParser(new CommonTokenStream(lexer));
        var antlrTree = parser.start();
        if (visualizeParseTree) {
            showAstVisualization(parser, antlrTree);
        }

        if (buildAst) {
            var treeBuilder = new TreeBuilder();
            // var treeBuilder = new TestParseTreeListener();
            ParseTreeWalker.DEFAULT.walk(treeBuilder, antlrTree);
        }
        return antlrTree.result;
    }

    private static Lexer applyLexer(Reader inputText) throws IOException {
        return new KlangLexer(CharStreams.fromReader(inputText));
    }

    public static void main(String[] args) {
        var outputDir = DEFAULT_OUT_DIR;
        var visualizeParseTree = DEFAULT_VISUALIZE_PARSETREE;
        var buildAst = DEFAULT_BUILD_AST;
        var prettyPrintAst = DEFAULT_PRETTY_PRINT_AST;
        var typeCheck = DEFAULT_TYPECHECK;
        var generateJbc = DEFAULT_GENERATE_JBC;
        var generateAsm = DEFAULT_GENERATE_ASM;

        var compiler = new KlangCompiler(
                outputDir,
                visualizeParseTree,
                buildAst,
                prettyPrintAst,
                typeCheck,
                generateJbc,
                generateAsm);
        var filePaths = Arrays.asList(args).stream().skip(1).map(src -> Path.of(src)).toList();
        for (var fp : filePaths) {
            var file = fp.toFile();

            var fpBase = PathUtils.getParentOrEmpty(fp);
            var packageName = fpBase.toString().replace(File.separator, ".");
            var className = PathUtils.getFileNameNoExt(fp);

            try (var reader = new FileReader(file)) {
                compiler.compile(reader, packageName, className);
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
