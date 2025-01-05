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
import lombok.Builder;
import utils.PathUtils;

public class KlangCompiler {
    private KlangCompilerConfig cfg;

    public KlangCompiler(KlangCompilerConfig config) {
        this.cfg = config;
    }

    @Builder
    static record KlangCompilerConfig(
            String outputDir,
            boolean visualizeParseTree,
            boolean buildAst,
            boolean prettyPrintAst,
            boolean typeCheck,
            boolean generateJbc,
            boolean generateAsm) {
        public static class KlangCompilerConfigBuilder {
            KlangCompilerConfigBuilder() {
                // Default compiler configuration
                outputDir = "./out";
                visualizeParseTree = false;
                buildAst = true;
                prettyPrintAst = false;
                typeCheck = true;
                generateJbc = true;
                generateAsm = true;
            }
        }
    }

    public void compile(Reader inputCode, String packageName, String className)
            throws IOException {
        var ast = buildAst(inputCode);
        var indent = " ".repeat(4);

        // PrettyPrint
        if (cfg.prettyPrintAst()) {
            var pp = new PrettyPrinter();
            ast.accept(new PrettyPrinter());
            System.out.println(pp.writer.toString());
        }

        // Type checking
        if (cfg.typeCheck()) {
            ast.accept(new TypeChecker());
        }

        // Java Byte Code generation
        System.out.println("Java Byte Code:");
        if (cfg.generateJbc()) {
            var codeGenerator = new GenJBC(cfg.outputDir(), packageName, className);
            printGeneratingFilesMessage(indent, codeGenerator.outFilePaths());

            runWithSuccessCheck(() -> ast.accept(codeGenerator), indent);

        } else {
            System.out.println(indent + "No JBC generated (disabled).");
        }
        System.out.println();

        // GNU Assembly generation
        System.out.println("GNU Assembly Code:");
        if (cfg.generateAsm()) {
            var codeGenerator = new GenAsm(cfg.outputDir(), packageName, className);
            printGeneratingFilesMessage(indent, codeGenerator.outFilePaths());
            ast.accept(codeGenerator);

            // Generate C helper files
            var cHelpersGen = new GenCHelpers(cfg.outputDir(), packageName, className);
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
        if (cfg.visualizeParseTree()) {
            showAstVisualization(parser, antlrTree);
        }

        if (cfg.buildAst()) {
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
        var config = KlangCompilerConfig.builder().build();
        var compiler = new KlangCompiler(config);
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
