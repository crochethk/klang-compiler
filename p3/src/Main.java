import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.antlr.v4.gui.TreeViewer;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import cc.crochethk.compilerbau.p3.Interpreter;
import cc.crochethk.compilerbau.p3.PrettyPrinter;
import cc.crochethk.compilerbau.p3.TypeChecker;
import cc.crochethk.compilerbau.p3.ast.Node;

public class Main {
    static boolean SHOW_AST_VISUALIZATION = false;

    public static void main(String[] args) throws Exception {
        System.out.println(" ");
        String input_code;

        // some easy to swap sample expressions
        // input_code = "(true || false) && (1+2)"; // type error example
        // input_code = "(true || false) && true"; // good example
        // input_code = "(17+4)*2*1"; // good example
        // input_code = "17+4*2*1"; // good example
        // input_code = "(17+4**(2*3-4)+7)*2*1"; // pow example
        // input_code = "//hello\n// world\r\n(17+4**(2*3-4)+7)*2*1"; // linecomments

        input_code = """
                //Erster Test
                fn f(x:long):long{
                  return (17   +4)*2*1+x
                }

                fn g(y:long):long {return y+f(y+1)}

                """;

        // ----- Further work on the custom-build AST
        Node rootNode = buildNodeTree(input_code, SHOW_AST_VISUALIZATION);

        /**
         * Type checking
         */
        var typeChecker = new TypeChecker();
        rootNode.accept(typeChecker);

        /**
         * Pretty Print the built AST
         */
        var stringifiedNodeTree = rootNode.accept(new PrettyPrinter()).toString();
        System.out.println("--- Pretty Printer:");
        var format = "%-28s : %s\n";
        System.out.printf(format, "Original Code", "\n" + input_code);
        System.out.printf(format, "Prettyprinted AST", stringifiedNodeTree);

        // Validate semantics: Re-parse the stringified AST
        var rootNode2 = buildNodeTree(stringifiedNodeTree);
        var restringifiedNodeTree = rootNode2.accept(new PrettyPrinter()).toString();
        System.out.printf(format, "Reparsed, prettyprinted AST", restringifiedNodeTree);
        System.out.println();

        /**
         * Interpret the code
         */
        var result = rootNode.accept(new Interpreter());
        System.out.println("--- Interpreter:");
        System.out.println("Result: " + result);
        var result2 = rootNode2.accept(new Interpreter());
        System.out.println("Result reparsed: " + result2);
    }

    private static Node buildNodeTree(String input_code, boolean show_antlr_ast_visualization) throws IOException {
        var lexer = new L1Lexer(CharStreams.fromReader(new StringReader(input_code)));
        var parser = new L1Parser(new CommonTokenStream(lexer));
        var antlrTree = parser.start();
        ParseTreeWalker.DEFAULT.walk(new TreeBuilder(), antlrTree);

        if (show_antlr_ast_visualization) {
            showAstVisualization(parser, antlrTree);
        }
        return antlrTree.result;
    }

    private static Node buildNodeTree(String input_code) throws IOException {
        return buildNodeTree(input_code, false);
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