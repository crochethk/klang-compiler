import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Token;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class L1LexerTest {
    @Nested
    class SingleToken {
        @Test
        public void testLIT_INTEGER() {
            assertEquals(List.of(L1Lexer.LIT_INTEGER, L1Lexer.EOF), getTokenTypesFromText("123"));
        }

        @Test
        public void testLIT_FLOAT() {
            assertEquals(List.of(L1Lexer.LIT_FLOAT, L1Lexer.EOF), getTokenTypesFromText("123.456"));
        }

        @Test
        public void testTRUE() {
            assertEquals(List.of(L1Lexer.TRUE, L1Lexer.EOF), getTokenTypesFromText("true"));
        }

        @Test
        public void testFALSE() {
            assertEquals(List.of(L1Lexer.FALSE, L1Lexer.EOF), getTokenTypesFromText("false"));
        }

        @Test
        public void testINCREMENT() {
            assertEquals(List.of(L1Lexer.INCREMENT, L1Lexer.EOF), getTokenTypesFromText("++"));
        }

        @Test
        public void testADD() {
            assertEquals(List.of(L1Lexer.ADD, L1Lexer.EOF), getTokenTypesFromText("+"));
        }

        @Test
        public void testSUB() {
            assertEquals(List.of(L1Lexer.SUB, L1Lexer.EOF), getTokenTypesFromText("-"));
        }

        @Test
        public void testDECREMENT() {
            assertEquals(List.of(L1Lexer.DECREMENT, L1Lexer.EOF), getTokenTypesFromText("--"));
        }

        @Test
        public void testPOW() {
            assertEquals(List.of(L1Lexer.POW, L1Lexer.EOF), getTokenTypesFromText("**"));
        }

        @Test
        public void testMULT() {
            assertEquals(List.of(L1Lexer.MULT, L1Lexer.EOF), getTokenTypesFromText("*"));
        }

        @Test
        public void testDIV() {
            assertEquals(List.of(L1Lexer.DIV, L1Lexer.EOF), getTokenTypesFromText("/"));
        }

        @Test
        public void testAND() {
            assertEquals(List.of(L1Lexer.AND, L1Lexer.EOF), getTokenTypesFromText("&&"));
        }

        @Test
        public void testOR() {
            assertEquals(List.of(L1Lexer.OR, L1Lexer.EOF), getTokenTypesFromText("||"));
        }

        @Test
        public void testNOT() {
            assertEquals(List.of(L1Lexer.NOT, L1Lexer.EOF), getTokenTypesFromText("!"));
        }

        @Test
        public void testEQEQ() {
            assertEquals(List.of(L1Lexer.EQEQ, L1Lexer.EOF), getTokenTypesFromText("=="));
        }

        @Test
        public void testNEQ() {
            assertEquals(List.of(L1Lexer.NEQ, L1Lexer.EOF), getTokenTypesFromText("!="));
        }

        @Test
        public void testGT() {
            assertEquals(List.of(L1Lexer.GT, L1Lexer.EOF), getTokenTypesFromText(">"));
        }

        @Test
        public void testGTEQ() {
            assertEquals(List.of(L1Lexer.GTEQ, L1Lexer.EOF), getTokenTypesFromText(">="));
        }

        @Test
        public void testLT() {
            assertEquals(List.of(L1Lexer.LT, L1Lexer.EOF), getTokenTypesFromText("<"));
        }

        @Test
        public void testLTEQ() {
            assertEquals(List.of(L1Lexer.LTEQ, L1Lexer.EOF), getTokenTypesFromText("<="));
        }

        @Test
        public void testRARROW() {
            assertEquals(List.of(L1Lexer.RARROW, L1Lexer.EOF), getTokenTypesFromText("->"));
        }

        @Test
        public void testEQ() {
            assertEquals(List.of(L1Lexer.EQ, L1Lexer.EOF), getTokenTypesFromText("="));
        }

        @Test
        public void testLPAR() {
            assertEquals(List.of(L1Lexer.LPAR, L1Lexer.EOF), getTokenTypesFromText("("));
        }

        @Test
        public void testRPAR() {
            assertEquals(List.of(L1Lexer.RPAR, L1Lexer.EOF), getTokenTypesFromText(")"));
        }

        @Test
        public void testLBRACE() {
            assertEquals(List.of(L1Lexer.LBRACE, L1Lexer.EOF), getTokenTypesFromText("{"));
        }

        @Test
        public void testRBRACE() {
            assertEquals(List.of(L1Lexer.RBRACE, L1Lexer.EOF), getTokenTypesFromText("}"));
        }

        @Test
        public void testCOLON() {
            assertEquals(List.of(L1Lexer.COLON, L1Lexer.EOF), getTokenTypesFromText(":"));
        }

        @Test
        public void testCOMMA() {
            assertEquals(List.of(L1Lexer.COMMA, L1Lexer.EOF), getTokenTypesFromText(","));
        }

        @Test
        public void testSEMI() {
            assertEquals(List.of(L1Lexer.SEMI, L1Lexer.EOF), getTokenTypesFromText(";"));
        }

        @Test
        public void testQM() {
            assertEquals(List.of(L1Lexer.QM, L1Lexer.EOF), getTokenTypesFromText("?"));
        }

        @Test
        public void testKW_FUN() {
            assertEquals(List.of(L1Lexer.KW_FUN, L1Lexer.EOF), getTokenTypesFromText("fn"));
        }

        @Test
        public void testKW_RETURN() {
            assertEquals(List.of(L1Lexer.KW_RETURN, L1Lexer.EOF), getTokenTypesFromText("return"));
        }

        @Test
        public void testKW_IF() {
            assertEquals(List.of(L1Lexer.KW_IF, L1Lexer.EOF), getTokenTypesFromText("if"));
        }

        @Test
        public void testKW_ELSE() {
            assertEquals(List.of(L1Lexer.KW_ELSE, L1Lexer.EOF), getTokenTypesFromText("else"));
        }

        @Test
        public void testKW_LET() {
            assertEquals(List.of(L1Lexer.KW_LET, L1Lexer.EOF), getTokenTypesFromText("let"));
        }

        @Test
        public void testKW_AS() {
            assertEquals(List.of(L1Lexer.KW_AS, L1Lexer.EOF), getTokenTypesFromText("as"));
        }

        @Test
        public void testT_BOOL() {
            assertEquals(List.of(L1Lexer.T_BOOL, L1Lexer.EOF), getTokenTypesFromText("bool"));
        }

        @Test
        public void testT_VOID() {
            assertEquals(List.of(L1Lexer.T_VOID, L1Lexer.EOF), getTokenTypesFromText("void"));
        }

        @Test
        public void testT_I64() {
            assertEquals(List.of(L1Lexer.T_I64, L1Lexer.EOF), getTokenTypesFromText("i64"));
        }

        @Test
        public void testT_F64() {
            assertEquals(List.of(L1Lexer.T_F64, L1Lexer.EOF), getTokenTypesFromText("f64"));
        }

        @Test
        public void testIDENT() {
            assertEquals(List.of(L1Lexer.IDENT, L1Lexer.EOF), getTokenTypesFromText("identifier"));
            assertEquals(List.of(L1Lexer.IDENT, L1Lexer.EOF), getTokenTypesFromText(
                    "_ABCDEFGHIJKLMNOPQRSTUVWXYZ_abcdefghijklmnopqrstuvwxyz_0123456789_"));
        }

        @Test
        public void testLINE_COMMENT() {
            assertEquals(List.of(L1Lexer.EOF), getTokenTypesFromText("//this is not a type token: f64\n"));
        }

        @Test
        public void testBLOCK_COMMENT() {
            assertEquals(List.of(L1Lexer.EOF), getTokenTypesFromText("/* first ln followed by emtpy\n\n last line*/"));
            assertEquals(List.of(L1Lexer.EOF), getTokenTypesFromText("/*simple *** inline comment*/"));
        }

        @Test
        public void testWHITESPACE() {
            assertEquals(List.of(L1Lexer.EOF), getTokenTypesFromText(" "));
            assertEquals(List.of(L1Lexer.EOF), getTokenTypesFromText("\t"));
            assertEquals(List.of(L1Lexer.EOF), getTokenTypesFromText("\n"));
            assertEquals(List.of(L1Lexer.EOF), getTokenTypesFromText("\r"));
            assertEquals(List.of(L1Lexer.EOF), getTokenTypesFromText("  "));
            assertEquals(List.of(L1Lexer.EOF), getTokenTypesFromText("\t\t"));
            assertEquals(List.of(L1Lexer.EOF), getTokenTypesFromText("\n\n"));
            assertEquals(List.of(L1Lexer.EOF), getTokenTypesFromText("\r\r"));
        }
    }

    @Nested
    class PotentialAmbiguities {
        @Test
        void testAsterisks() {
            assertEquals(List.of(L1Lexer.MULT, L1Lexer.EOF),
                    getTokenTypesFromText("*/** */"));
            assertEquals(List.of(L1Lexer.POW, L1Lexer.EOF),
                    getTokenTypesFromText("**/** */"));
            assertEquals(List.of(L1Lexer.MULT, L1Lexer.EOF),
                    getTokenTypesFromText("/** */*"));
            assertEquals(List.of(L1Lexer.POW, L1Lexer.EOF),
                    getTokenTypesFromText("/** */**"));

            assertEquals(List.of(L1Lexer.MULT, L1Lexer.MULT, L1Lexer.EOF),
                    getTokenTypesFromText("* *"));
            assertEquals(List.of(L1Lexer.POW, L1Lexer.MULT, L1Lexer.EOF),
                    getTokenTypesFromText("***"));
            assertEquals(List.of(L1Lexer.POW, L1Lexer.POW, L1Lexer.MULT, L1Lexer.EOF),
                    getTokenTypesFromText("*****"));
            assertEquals(List.of(L1Lexer.MULT, L1Lexer.POW, L1Lexer.POW, L1Lexer.EOF),
                    getTokenTypesFromText("* ****"));
        }

        @Test
        void testLetPrefixIsNotKeyword() {
            assertEquals(List.of(L1Lexer.KW_LET, L1Lexer.IDENT, L1Lexer.EOF),
                    getTokenTypesFromText("let letfoo"));
        }

        @Test
        void testAsSuffixIsNotKeyword() {
            assertEquals(List.of(L1Lexer.KW_AS, L1Lexer.IDENT, L1Lexer.EOF),
                    getTokenTypesFromText("as gas"));
        }

        @Test
        void testBuiltinTypePartOfIdent() {
            assertEquals(List.of(L1Lexer.IDENT, L1Lexer.COLON, L1Lexer.T_F64, L1Lexer.EOF),
                    getTokenTypesFromText("i64_f64_var : f64"));
        }

        @Test
        void testOperators_1() {
            assertEquals(List.of(L1Lexer.NOT, L1Lexer.NEQ, L1Lexer.EOF),
                    getTokenTypesFromText("!!="));
        }

        @Test
        void testOperators_2() {
            assertEquals(List.of(L1Lexer.NEQ, L1Lexer.EQEQ, L1Lexer.EOF),
                    getTokenTypesFromText("!==="));
        }

        @Test
        void testOperators_3() {
            assertEquals(List.of(L1Lexer.DECREMENT, L1Lexer.SUB, L1Lexer.EOF),
                    getTokenTypesFromText("---"));
        }

        @Test
        void testOperators_4() {
            assertEquals(List.of(L1Lexer.INCREMENT, L1Lexer.ADD, L1Lexer.EOF),
                    getTokenTypesFromText("+++"));
        }

        @Test
        void testOperators_5() {
            assertEquals(List.of(L1Lexer.RARROW, L1Lexer.EQ, L1Lexer.EOF),
                    getTokenTypesFromText("->="));
        }

        @Test
        void testOperators_6() {
            assertEquals(List.of(L1Lexer.RARROW, L1Lexer.GT, L1Lexer.EOF),
                    getTokenTypesFromText("->>"));
        }

    }

    /*
    @Test
    void testXXX() {
        var tokens = getTokenTypesFromText("");
        List<Integer> expTokens = List.of(
                L1Lexer.IDENT,
                L1Lexer.EOF);
        assertEquals(expTokens, tokens);
    }
    */

    @Test
    void testFunDef() {
        var tokens = getTokenTypesFromText("fn f(a:A,)-> f64{return 42 as f64;}");
        List<Integer> expTokens = List.of(
                L1Lexer.KW_FUN, L1Lexer.IDENT, L1Lexer.LPAR,
                L1Lexer.IDENT, L1Lexer.COLON, L1Lexer.IDENT, L1Lexer.COMMA,
                L1Lexer.RPAR, L1Lexer.RARROW, L1Lexer.T_F64,
                L1Lexer.LBRACE, L1Lexer.KW_RETURN, L1Lexer.LIT_INTEGER,
                L1Lexer.KW_AS, L1Lexer.T_F64, L1Lexer.SEMI, L1Lexer.RBRACE,
                L1Lexer.EOF);
        assertEquals(expTokens, tokens);
    }

    // @Test
    // void testBuiltinTypes() {
    //     var tokens = getTokenTypesFromText("fn bool() -> void{}");
    //     List<Integer> expTokens = List.of(
    //             L1Lexer.KW_FUN, L1Lexer.IDENT, L1Lexer.LPAR, L1Lexer.RPAR,
    //             L1Lexer.RARROW, L1Lexer.T_VOID,
    //             L1Lexer.LBRACE, L1Lexer.RBRACE,
    //             L1Lexer.EOF);
    //     assertEquals(expTokens, tokens);
    // }

    @Test
    void testBooleanExpr() {
        var tokens = getTokenTypesFromText("/*modulo*/!(number-(number/2)*2==0)");
        List<Integer> expTokens = List.of(
                L1Lexer.NOT, L1Lexer.LPAR, L1Lexer.IDENT, L1Lexer.SUB,
                L1Lexer.LPAR, L1Lexer.IDENT, L1Lexer.DIV, L1Lexer.LIT_INTEGER,
                L1Lexer.RPAR, L1Lexer.MULT, L1Lexer.LIT_INTEGER,
                L1Lexer.EQEQ, L1Lexer.LIT_INTEGER, L1Lexer.RPAR,
                L1Lexer.EOF);
        assertEquals(expTokens, tokens);
    }

    @Test
    void testTernaryExpr() {
        var tokens = getTokenTypesFromText("let a:TheType = cond?true:false;");
        List<Integer> expTokens = List.of(
                L1Lexer.KW_LET, L1Lexer.IDENT, L1Lexer.COLON, L1Lexer.IDENT,
                L1Lexer.EQ, L1Lexer.IDENT, L1Lexer.QM,
                L1Lexer.TRUE, L1Lexer.COLON, L1Lexer.FALSE, L1Lexer.SEMI,
                L1Lexer.EOF);
        assertEquals(expTokens, tokens);
    }

    @Test
    void testIfElse() {
        var tokens = getTokenTypesFromText("if a>=b{return a;}else{return b;}");
        List<Integer> expTokens = List.of(
                L1Lexer.KW_IF, L1Lexer.IDENT, L1Lexer.GTEQ, L1Lexer.IDENT,
                L1Lexer.LBRACE, L1Lexer.KW_RETURN, L1Lexer.IDENT, L1Lexer.SEMI,
                L1Lexer.RBRACE,
                L1Lexer.KW_ELSE, L1Lexer.LBRACE, L1Lexer.KW_RETURN, L1Lexer.IDENT,
                L1Lexer.SEMI, L1Lexer.RBRACE,
                L1Lexer.EOF);
        assertEquals(expTokens, tokens);
    }

    //
    // Helper stuff
    //
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
