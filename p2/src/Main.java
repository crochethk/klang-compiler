import org.antlr.v4.gui.TreeViewer;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import javax.swing.*;

import java.io.StringReader;
import java.util.Arrays;

public class Main {
    static boolean SHOW_AST_VISUALIZATION = false;

    public static void main(String[] args) throws Exception {
        L1Lexer lexer;
        // some easy to swap sample expressions
        lexer = new L1Lexer(CharStreams.fromReader(new StringReader("(17+4)*2*1")));
        lexer = new L1Lexer(CharStreams.fromReader(new StringReader("(17+4**(2*3-4)+7)*2*1")));

        var parser = new L1Parser(new CommonTokenStream(lexer));
        var tree = parser.expr();
        ParseTreeWalker.DEFAULT.walk(new Eval(), tree);
        System.out.println(tree.result);

        if (SHOW_AST_VISUALIZATION) {
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
}