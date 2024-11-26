import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Token;
import org.junit.jupiter.api.Test;

public class L1LexerTest {
    private ANTLRErrorListener errorListener = new LexerErrorListener();

    private List<Integer> getTokenTypesFromText(String txt) {
        return getTokensFromText(txt).stream()
                .map(t -> t.getType()).toList();
    }

    private List<Token> getTokensFromText(String txt) {
        var strReader = new StringReader(txt);
        Lexer lexer;
        try {
            lexer = L1Compiler.applyLexer(strReader);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        lexer.addErrorListener(errorListener); // controls recongintion error behaviour
        var tokenStream = new CommonTokenStream(lexer);
        tokenStream.fill();
        return tokenStream.getTokens();
    }

    class LexerErrorListener extends BaseErrorListener {
    }
}
