package cc.crochethk.klang;

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
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import cc.crochethk.klang.antlr.*;
import cc.crochethk.klang.ast.Node;
import cc.crochethk.klang.visitor.PrettyPrinter;
import cc.crochethk.klang.visitor.TypeChecker;
import cc.crochethk.klang.visitor.codegen.GenAsm;
import cc.crochethk.klang.visitor.codegen.GenJBC;
import lombok.Builder;
import utils.CommandLineParser;
import utils.PathUtils;

public class KlangCompiler {
    private KlangCompilerConfig cfg;

    public KlangCompiler(KlangCompilerConfig config) {
        this.cfg = config;
    }

    @Builder
    static record KlangCompilerConfig(
            String outputDir,
            boolean showParseTree,
            boolean buildAst,
            boolean prettyPrintAst,
            boolean typeCheck,
            boolean generateJbc,
            boolean generateAsm) {
    }

    public Result compile(Reader inputCode, final String packageName, final String className)
            throws IOException {
        Result compileResult = Result.Ok;

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

            if (runWithSuccessCheck(() -> ast.accept(codeGenerator), indent).isErr()) {
                compileResult = Result.Err;
            }
        } else {
            System.out.println(indent + "No JBC generated (disabled).");
        }
        System.out.println();

        // GNU Assembly generation
        System.out.println("GNU Assembly Code:");
        if (cfg.generateAsm()) {
            // Prepare for Assembly
            var codeGenerator = new GenAsm(cfg.outputDir(), packageName, className);

            // Generate files
            var genResult = runWithSuccessCheck(() -> {
                printGeneratingFilesMessage(indent, codeGenerator.outFilePaths());
                ast.accept(codeGenerator);
            }, indent);

            if (genResult.isErr()) {
                compileResult = Result.Err;
            }
        } else {
            System.out.println(indent + "No assembly generated (disabled).");
        }
        return compileResult;
    }

    public enum Result {
        Ok, Err;

        public boolean isErr() {
            return switch (this) {
                case Err -> true;
                default -> false;
            };
        }
    };

    private static Result runWithSuccessCheck(Runnable callable, String msgIndent) {
        try {
            callable.run();
            System.out.println(msgIndent + "Success!");
            return Result.Ok;
        } catch (Exception e) {
            System.err.println(msgIndent + "ERROR: " + e.getMessage());
            return Result.Err;
        }
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
        var lexer = buildLexer(CharStreams.fromReader(inputCode));
        var parser = buildParser(lexer);
        var antlrTree = parser.start();
        if (cfg.showParseTree()) {
            showParseTreeVisualization(parser, antlrTree);
        }

        if (cfg.buildAst()) {
            var treeBuilder = new TreeBuilder();
            // var treeBuilder = new TestParseTreeListener();
            ParseTreeWalker.DEFAULT.walk(treeBuilder, antlrTree);
        }
        return antlrTree.result;
    }

    static Lexer buildLexer(CharStream input) {
        var lx = new KlangLexer(input);
        lx.addErrorListener(ThrowingErrorListener.INSTANCE);
        return lx;
    }

    static KlangParser buildParser(Lexer lx) {
        var parser = new KlangParser(new CommonTokenStream(lx));
        parser.addErrorListener(ThrowingErrorListener.INSTANCE);
        return parser;
    }

    public static void main(String[] args) {
        final String USAGE_INFO = """
                Usage: java --enable-preview -jar klangc.jar [OPTION | FLAG]... [--] FILE...
                Default: {...} --outdir ./out --build-ast --typecheck --jbc --asm FILE...

                    FILE...                     One or more source files to compile.
                Options:
                    --output    <dir>           Output directory for generated files. (default: './out')
                    --build-ast <true|false>    Transform parsetree to custom AST. (default: true)
                    --typecheck <true|false>    Perform type check. (default: true)
                Flags:
                    --jbc                       Generate Java Byte Code. (default)
                    --asm                       Generate GNU Assembly. (default)
                    --show-parsetree            Visualize the parsetree generated by antlr.
                    --pretty-print-ast          Convert the AST back to sourcecode and print it to the console.
                    --help                      Print this usage information.

                Note:
                It is assumed that the invoker's workind directory is the source root folder,
                since the package name is inferred from the file's relative path.
                Therefore a file './tests/somePackage/source.k' will be considered in the package 'tests.somePackage'.
                Also "../" is not allowed in the sourcefile path and might throw.""";

        // Names of options and flags
        final String optOutDir = "output";
        final String optBuildAst = "build-ast";
        final String optTypeCheck = "typecheck";
        final String flagJbc = "jbc";
        final String flagAsm = "asm";
        final String flagShowParseTree = "show-parsetree";
        final String flagPrettyPrintAst = "pretty-print-ast";
        final String flagHelp = "help";

        // Setup parser and parse args
        CommandLineParser parser = null;
        try {
            // --- Parser config ---
            parser = new CommandLineParser.ArgumentBuilder()
                    .flag(flagHelp)
                    .optionalArg(optOutDir, "./out")
                    .optionalArg(optBuildAst, "true")
                    .optionalArg(optTypeCheck, "true")
                    .flag(flagJbc)
                    .flag(flagAsm)
                    .withTrailingArgs()

                    .flag(flagShowParseTree)
                    .flag(flagPrettyPrintAst)
                    .parse(args);
        } catch (IllegalArgumentException e) {
            System.err.println("Error: " + e.getMessage());
            System.err.println(USAGE_INFO);
            System.exit(1);
        }

        if (parser.hasFlag(flagHelp)) {
            System.out.println(USAGE_INFO);
        }

        String outputDir = parser.getValue(optOutDir).get();
        List<String> files = parser.getTrailingArgs();
        System.out.println("Output directory: " + outputDir);
        System.out.println("Files to process: " + files);

        var targetFormatProvided = parser.hasFlag(flagJbc) || parser.hasFlag(flagAsm);

        var config = KlangCompilerConfig.builder()
                .outputDir(outputDir)

                .buildAst(parser.getValue(optBuildAst).get().equals("true"))
                .typeCheck(parser.getValue(optTypeCheck).get().equals("true"))

                // If not format specified generate both per default
                .generateJbc(targetFormatProvided ? parser.hasFlag(flagJbc) : true)
                .generateAsm(targetFormatProvided ? parser.hasFlag(flagAsm) : true)

                .showParseTree(parser.hasFlag(flagShowParseTree))
                .prettyPrintAst(parser.hasFlag(flagPrettyPrintAst))
                .build();

        var compiler = new KlangCompiler(config);
        var filePaths = files.stream().map(src -> Path.of(src)).toList();
        for (var fp : filePaths) {
            System.out.println(String.format("--------[ %s ]--------", fp.toString()));
            var file = fp.toFile();

            var fpBase = PathUtils.getParentOrEmpty(fp).normalize();
            var packageName = fpBase.toString().replace(File.separator, ".");
            var className = PathUtils.getFileNameNoExt(fp);

            try (var reader = new FileReader(file)) {
                if (compiler.compile(reader, packageName, className).isErr()) {
                    throw new RuntimeException();
                }
                System.out.println(">>> All tasks finished successfully.\n");
            } catch (Exception e) {
                System.out.println(">>> Errors occured while processing compilation tasks.");
                System.err.println(e.getMessage());
                System.exit(1);
            }
        }
    }

    private static void showParseTreeVisualization(Parser parser, RuleContext tree) {
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
