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

import cc.crochethk.compilerbau.p2.InterpretVisitor;
import cc.crochethk.compilerbau.p2.Node;
import cc.crochethk.compilerbau.p2.TypeCheckVisitor;

public class Main {
    static boolean SHOW_AST_VISUALIZATION = false;

    public static void main(String[] args) throws Exception {
        L1Lexer lexer;
        // some easy to swap sample expressions
        lexer = new L1Lexer(CharStreams.fromReader(new StringReader("(true or false) and (1+2)"))); // type error example
        lexer = new L1Lexer(CharStreams.fromReader(new StringReader("(true or false) and true"))); // good example
        lexer = new L1Lexer(CharStreams.fromReader(new StringReader("(17+4)*2*1"))); // good example

        var parser = new L1Parser(new CommonTokenStream(lexer));
        var antlrTree = parser.start();
        ParseTreeWalker.DEFAULT.walk(new TreeBuilder(), antlrTree);

        if (SHOW_AST_VISUALIZATION) {
            showAstVisualization(parser, antlrTree);
        }

        // ----- Further work on the custom-build AST
        Node rootNode = antlrTree.result;

        // Type checking
        var typeChecker = new TypeCheckVisitor();
        rootNode.accept(typeChecker);

        // Interpret the code
        var interpreter = new InterpretVisitor();
        var result = rootNode.accept(interpreter);
        System.out.println("Result: " + result);

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