import static java.util.Map.entry;
import static org.junit.Assert.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.StringReader;
import java.util.Map;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.jupiter.api.Test;

public class EvalTest {
    @Test
    void testExitExpr() throws Exception {
        var samples = Map.ofEntries(
                entry("(17+4)*2*1", (long) 42),
                entry("(17+4**2*7)*2*1", (long) 258),
                entry("(17+4**2+7)*2*1", (long) 80),
                entry("(17+4**(2*3-4)+7)*2*1", (long) 80),
                entry("3**2", (long) 9),
                entry("(17+4**2)*2*1", (long) 66),
                entry("31/7", (long) 4)//
        );

        for (var entry : samples.entrySet()) {
            System.out.println("hello");
            var k = entry.getKey();
            var v = entry.getValue();
            assertEquals(eval_input_string(k), v);
        }

        assertNotEquals(eval_input_string("2*2+3"), 10);
        assertNotEquals(eval_input_string("3+2*2"), 10);
    }

    static long eval_input_string(String input) throws Exception {
        var lexer = new L1Lexer(CharStreams.fromReader(new StringReader(input)));
        var parser = new L1Parser(new CommonTokenStream(lexer));
        var tree = parser.expr();
        ParseTreeWalker.DEFAULT.walk(new Eval(), tree);
        return tree.result;
    }
}
