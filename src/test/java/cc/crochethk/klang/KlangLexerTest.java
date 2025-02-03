package cc.crochethk.klang;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Token;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import cc.crochethk.klang.antlr.KlangLexer;

public class KlangLexerTest {
    @Nested
    class SingleToken {
        private static final List<Integer> ONLY_EOF_TOKEN = List.of(KlangLexer.EOF);

        @Test
        public void testLIT_INTEGER() {
            assertEquals(List.of(KlangLexer.LIT_INTEGER, KlangLexer.EOF), getTokenTypesFromText("123"));
        }

        @Test
        public void testLIT_INTEGER_neg() {
            assertEquals(List.of(KlangLexer.LIT_INTEGER, KlangLexer.EOF), getTokenTypesFromText("-123"));
        }

        @Test
        public void testLIT_FLOAT() {
            assertEquals(List.of(KlangLexer.LIT_FLOAT, KlangLexer.EOF), getTokenTypesFromText("123.456"));
        }

        @Test
        public void testLIT_FLOAT_neg() {
            assertEquals(List.of(KlangLexer.LIT_FLOAT, KlangLexer.EOF), getTokenTypesFromText("-123.456"));
        }

        @Test
        public void simpleLIT_STRING() {
            assertEquals(List.of(KlangLexer.LIT_STRING, KlangLexer.EOF), getTokenTypesFromText("\"abc\""));
            assertEquals("\"abc\"", getTokensFromText("\"abc\"").get(0).getText());
        }

        @Test
        public void emptyLIT_STRING() {
            assertEquals(List.of(KlangLexer.LIT_STRING, KlangLexer.EOF), getTokenTypesFromText("\"\""));
            assertEquals("\"\"", getTokensFromText("\"\"").get(0).getText());
        }

        @Test
        public void testDQUOTE() {
            assertEquals(List.of(KlangLexer.DQUOTE, KlangLexer.EOF), getTokenTypesFromText("\""));
        }

        @Test
        public void testTRIDASH() {
            assertEquals(List.of(KlangLexer.TRIDASH, KlangLexer.EOF), getTokenTypesFromText("---"));
        }

        @Test
        public void testTRUE() {
            assertEquals(List.of(KlangLexer.TRUE, KlangLexer.EOF), getTokenTypesFromText("true"));
        }

        @Test
        public void testFALSE() {
            assertEquals(List.of(KlangLexer.FALSE, KlangLexer.EOF), getTokenTypesFromText("false"));
        }

        @Test
        public void testADD() {
            assertEquals(List.of(KlangLexer.ADD, KlangLexer.EOF), getTokenTypesFromText("+"));
        }

        @Test
        public void testSUB() {
            assertEquals(List.of(KlangLexer.SUB, KlangLexer.EOF), getTokenTypesFromText("-"));
        }

        @Test
        public void testPOW() {
            assertEquals(List.of(KlangLexer.POW, KlangLexer.EOF), getTokenTypesFromText("**"));
        }

        @Test
        public void testMULT() {
            assertEquals(List.of(KlangLexer.MULT, KlangLexer.EOF), getTokenTypesFromText("*"));
        }

        @Test
        public void testDIV() {
            assertEquals(List.of(KlangLexer.DIV, KlangLexer.EOF), getTokenTypesFromText("/"));
        }

        @Test
        public void testMOD() {
            assertEquals(List.of(KlangLexer.MOD, KlangLexer.EOF), getTokenTypesFromText("%"));
        }

        @Test
        public void testAND() {
            assertEquals(List.of(KlangLexer.AND, KlangLexer.EOF), getTokenTypesFromText("&&"));
        }

        @Test
        public void testOR() {
            assertEquals(List.of(KlangLexer.OR, KlangLexer.EOF), getTokenTypesFromText("||"));
        }

        @Test
        public void testNOT() {
            assertEquals(List.of(KlangLexer.NOT, KlangLexer.EOF), getTokenTypesFromText("!"));
        }

        @Test
        public void testEQEQ() {
            assertEquals(List.of(KlangLexer.EQEQ, KlangLexer.EOF), getTokenTypesFromText("=="));
        }

        @Test
        public void testNEQ() {
            assertEquals(List.of(KlangLexer.NEQ, KlangLexer.EOF), getTokenTypesFromText("!="));
        }

        @Test
        public void testGT() {
            assertEquals(List.of(KlangLexer.GT, KlangLexer.EOF), getTokenTypesFromText(">"));
        }

        @Test
        public void testGTEQ() {
            assertEquals(List.of(KlangLexer.GTEQ, KlangLexer.EOF), getTokenTypesFromText(">="));
        }

        @Test
        public void testLT() {
            assertEquals(List.of(KlangLexer.LT, KlangLexer.EOF), getTokenTypesFromText("<"));
        }

        @Test
        public void testLTEQ() {
            assertEquals(List.of(KlangLexer.LTEQ, KlangLexer.EOF), getTokenTypesFromText("<="));
        }

        @Test
        public void testRARROW() {
            assertEquals(List.of(KlangLexer.RARROW, KlangLexer.EOF), getTokenTypesFromText("->"));
        }

        @Test
        public void testEQ() {
            assertEquals(List.of(KlangLexer.EQ, KlangLexer.EOF), getTokenTypesFromText("="));
        }

        @Test
        public void testLPAR() {
            assertEquals(List.of(KlangLexer.LPAR, KlangLexer.EOF), getTokenTypesFromText("("));
        }

        @Test
        public void testRPAR() {
            assertEquals(List.of(KlangLexer.RPAR, KlangLexer.EOF), getTokenTypesFromText(")"));
        }

        @Test
        public void testLBRACE() {
            assertEquals(List.of(KlangLexer.LBRACE, KlangLexer.EOF), getTokenTypesFromText("{"));
        }

        @Test
        public void testRBRACE() {
            assertEquals(List.of(KlangLexer.RBRACE, KlangLexer.EOF), getTokenTypesFromText("}"));
        }

        @Test
        public void testCOLON() {
            assertEquals(List.of(KlangLexer.COLON, KlangLexer.EOF), getTokenTypesFromText(":"));
        }

        @Test
        public void testCOMMA() {
            assertEquals(List.of(KlangLexer.COMMA, KlangLexer.EOF), getTokenTypesFromText(","));
        }

        @Test
        public void testSEMI() {
            assertEquals(List.of(KlangLexer.SEMI, KlangLexer.EOF), getTokenTypesFromText(";"));
        }

        @Test
        public void testQM() {
            assertEquals(List.of(KlangLexer.QM, KlangLexer.EOF), getTokenTypesFromText("?"));
        }

        @Test
        public void testDOT() {
            assertEquals(List.of(KlangLexer.DOT, KlangLexer.EOF), getTokenTypesFromText("."));
        }

        @Test
        public void testKW_FUN() {
            assertEquals(List.of(KlangLexer.KW_FUN, KlangLexer.EOF), getTokenTypesFromText("fn"));
        }

        @Test
        public void testKW_RETURN() {
            assertEquals(List.of(KlangLexer.KW_RETURN, KlangLexer.EOF), getTokenTypesFromText("return"));
        }

        @Test
        public void testKW_IF() {
            assertEquals(List.of(KlangLexer.KW_IF, KlangLexer.EOF), getTokenTypesFromText("if"));
        }

        @Test
        public void testKW_ELSE() {
            assertEquals(List.of(KlangLexer.KW_ELSE, KlangLexer.EOF), getTokenTypesFromText("else"));
        }

        @Test
        public void testKW_LET() {
            assertEquals(List.of(KlangLexer.KW_LET, KlangLexer.EOF), getTokenTypesFromText("let"));
        }

        @Test
        public void testKW_DROP() {
            assertEquals(List.of(KlangLexer.KW_DROP, KlangLexer.EOF), getTokenTypesFromText("drop"));
        }

        @Test
        public void testKW_AS() {
            assertEquals(List.of(KlangLexer.KW_AS, KlangLexer.EOF), getTokenTypesFromText("as"));
        }

        @Test
        public void testKW_LOOP() {
            assertEquals(List.of(KlangLexer.KW_LOOP, KlangLexer.EOF), getTokenTypesFromText("loop"));
        }

        @Test
        public void testKW_BREAK() {
            assertEquals(List.of(KlangLexer.KW_BREAK, KlangLexer.EOF), getTokenTypesFromText("break"));
        }

        @Test
        public void testKW_STRUCT() {
            assertEquals(List.of(KlangLexer.KW_STRUCT, KlangLexer.EOF), getTokenTypesFromText("struct"));
        }

        @Test
        public void testKW_NULL() {
            assertEquals(List.of(KlangLexer.KW_NULL, KlangLexer.EOF), getTokenTypesFromText("null"));
        }

        @Test
        public void testT_BOOL() {
            assertEquals(List.of(KlangLexer.T_BOOL, KlangLexer.EOF), getTokenTypesFromText("bool"));
        }

        @Test
        public void testT_VOID() {
            assertEquals(List.of(KlangLexer.T_VOID, KlangLexer.EOF), getTokenTypesFromText("void"));
        }

        @Test
        public void testT_I64() {
            assertEquals(List.of(KlangLexer.T_I64, KlangLexer.EOF), getTokenTypesFromText("i64"));
        }

        @Test
        public void testT_F64() {
            assertEquals(List.of(KlangLexer.T_F64, KlangLexer.EOF), getTokenTypesFromText("f64"));
        }

        @Test
        public void testT_STRING() {
            assertEquals(List.of(KlangLexer.T_STRING, KlangLexer.EOF), getTokenTypesFromText("string"));
        }

        @Test
        public void testIDENT() {
            assertEquals(List.of(KlangLexer.IDENT, KlangLexer.EOF), getTokenTypesFromText("identifier"));
            assertEquals(List.of(KlangLexer.IDENT, KlangLexer.EOF), getTokenTypesFromText(
                    "_ABCDEFGHIJKLMNOPQRSTUVWXYZ_abcdefghijklmnopqrstuvwxyz_0123456789_"));
        }

        @Test
        public void testLINE_COMMENT() {
            assertEquals(ONLY_EOF_TOKEN, getTokenTypesFromText("//this is not a type token: f64\n"));
        }

        @Test
        public void testBLOCK_COMMENT() {
            assertEquals(ONLY_EOF_TOKEN, getTokenTypesFromText("/* first ln followed by emtpy\n\n last line*/"));
            assertEquals(ONLY_EOF_TOKEN, getTokenTypesFromText("/*simple *** inline comment*/"));
        }

        @Test
        public void testWHITESPACE() {
            assertEquals(ONLY_EOF_TOKEN, getTokenTypesFromText(" "));
            assertEquals(ONLY_EOF_TOKEN, getTokenTypesFromText("\t"));
            assertEquals(ONLY_EOF_TOKEN, getTokenTypesFromText("\n"));
            assertEquals(ONLY_EOF_TOKEN, getTokenTypesFromText("\r"));
            assertEquals(ONLY_EOF_TOKEN, getTokenTypesFromText("  "));
            assertEquals(ONLY_EOF_TOKEN, getTokenTypesFromText("\t\t"));
            assertEquals(ONLY_EOF_TOKEN, getTokenTypesFromText("\n\n"));
            assertEquals(ONLY_EOF_TOKEN, getTokenTypesFromText("\r\r"));
        }
    }

    @Nested
    class PotentialAmbiguities {
        @Test
        void testCommentSyntaxInsideStringLiteral_1() {
            var litStrText = "\"/*this is NOT empty!*/\"";
            var tokens = getTokensFromText(litStrText);
            assertEquals(List.of(KlangLexer.LIT_STRING, KlangLexer.EOF), getTokenTypes(tokens));
            assertEquals(litStrText, tokens.get(0).getText());
        }

        @Test
        void testCommentSyntaxInsideStringLiteral_2() {
            var litStrText = "\"//this is NOT empty!\"";
            var tokens = getTokensFromText(litStrText);
            assertEquals(List.of(KlangLexer.LIT_STRING, KlangLexer.EOF), getTokenTypes(tokens));
            assertEquals(litStrText, tokens.get(0).getText());
        }

        @Test
        void testStatementInStringLiteral() {
            var litStrText = "\"let foo : bool = false;\"";
            var expression = "let s = " + litStrText;
            var tokens = getTokensFromText(expression);
            assertEquals(List.of(
                    KlangLexer.KW_LET, KlangLexer.IDENT, KlangLexer.EQ,
                    KlangLexer.LIT_STRING, KlangLexer.EOF),
                    getTokenTypes(tokens));
            assertEquals(litStrText, tokens.get(3).getText());
        }

        @Test
        void testAsterisks() {
            assertEquals(List.of(KlangLexer.MULT, KlangLexer.EOF),
                    getTokenTypesFromText("*/** */"));
            assertEquals(List.of(KlangLexer.POW, KlangLexer.EOF),
                    getTokenTypesFromText("**/** */"));
            assertEquals(List.of(KlangLexer.MULT, KlangLexer.EOF),
                    getTokenTypesFromText("/** */*"));
            assertEquals(List.of(KlangLexer.POW, KlangLexer.EOF),
                    getTokenTypesFromText("/** */**"));

            assertEquals(List.of(KlangLexer.MULT, KlangLexer.MULT, KlangLexer.EOF),
                    getTokenTypesFromText("* *"));
            assertEquals(List.of(KlangLexer.POW, KlangLexer.MULT, KlangLexer.EOF),
                    getTokenTypesFromText("***"));
            assertEquals(List.of(KlangLexer.POW, KlangLexer.POW, KlangLexer.MULT, KlangLexer.EOF),
                    getTokenTypesFromText("*****"));
            assertEquals(List.of(KlangLexer.MULT, KlangLexer.POW, KlangLexer.POW, KlangLexer.EOF),
                    getTokenTypesFromText("* ****"));
        }

        @Test
        void testLetPrefixIsNotKeyword() {
            assertEquals(List.of(KlangLexer.KW_LET, KlangLexer.IDENT, KlangLexer.EOF),
                    getTokenTypesFromText("let letfoo"));
        }

        @Test
        void testAsSuffixIsNotKeyword() {
            assertEquals(List.of(KlangLexer.KW_AS, KlangLexer.IDENT, KlangLexer.EOF),
                    getTokenTypesFromText("as gas"));
        }

        @Test
        void testBuiltinTypePartOfIdent() {
            assertEquals(List.of(KlangLexer.IDENT, KlangLexer.COLON, KlangLexer.T_F64, KlangLexer.EOF),
                    getTokenTypesFromText("i64_f64_var : f64"));
        }

        @Test
        void testOperators_1() {
            assertEquals(List.of(KlangLexer.NOT, KlangLexer.NEQ, KlangLexer.EOF),
                    getTokenTypesFromText("!!="));
        }

        @Test
        void testOperators_2() {
            assertEquals(List.of(KlangLexer.NEQ, KlangLexer.EQEQ, KlangLexer.EOF),
                    getTokenTypesFromText("!==="));
        }

        @Test
        void testOperators_5() {
            assertEquals(List.of(KlangLexer.RARROW, KlangLexer.EQ, KlangLexer.EOF),
                    getTokenTypesFromText("->="));
        }

        @Test
        void testOperators_6() {
            assertEquals(List.of(KlangLexer.RARROW, KlangLexer.GT, KlangLexer.EOF),
                    getTokenTypesFromText("->>"));
        }

        @Test
        void testBuiltinTypeTokens() {
            assertEquals(List.of(KlangLexer.KW_FUN, KlangLexer.T_I64,
                    KlangLexer.LPAR, KlangLexer.RPAR, KlangLexer.IDENT, KlangLexer.LBRACE,
                    KlangLexer.T_STRING, KlangLexer.COLON, KlangLexer.T_F64, KlangLexer.RBRACE, KlangLexer.EOF),
                    getTokenTypesFromText("fn i64()foo{string:f64}"));

        }

        @Test
        void testMinusNegativeInt() {
            assertEquals(List.of(KlangLexer.SUB, KlangLexer.LIT_INTEGER, KlangLexer.EOF),
                    getTokenTypesFromText("--42"));

        }
    }

    @Nested
    class StringTests {
        private void assertStringTokenTextAndTokenTypes(String input, String expStrTokText, int expPosition,
                List<Integer> expTokenTypes) {
            var tokens = getTokensFromText(input);
            assertEquals(expTokenTypes, getTokenTypes(tokens));
            if (expPosition > -1)
                assertEquals(expStrTokText, tokens.get(expPosition).getText());
        }

        @Test
        void mostCommonAnsiEscapesArePreserved() {
            var input = "\"\n\t\r\"";
            var expStrText = input;
            assertStringTokenTextAndTokenTypes(input, expStrText, 0, List.of(KlangLexer.LIT_STRING, KlangLexer.EOF));
        }

        @Test
        void stringInbetweenOtherTokens() {
            assertStringTokenTextAndTokenTypes("i64\"foo\"baz", "\"foo\"", 1,
                    List.of(KlangLexer.T_I64, KlangLexer.LIT_STRING, KlangLexer.IDENT, KlangLexer.EOF));
        }

        @Test
        void backslashEscapedQuoteInString() {
            assertStringTokenTextAndTokenTypes("\"\\\"\"", "\"\\\"\"", 0,
                    List.of(KlangLexer.LIT_STRING, KlangLexer.EOF));
        }

        @Test
        void backslashEscapedBackslashInString() {
            // equivalent to literal string '\'
            assertStringTokenTextAndTokenTypes("\"\\\\\"", "\"\\\\\"",
                    0, List.of(KlangLexer.LIT_STRING, KlangLexer.EOF));
            // equivalent to literal string '\\'
            assertStringTokenTextAndTokenTypes("\"\\\\\\\\\"", "\"\\\\\\\\\"", 0,
                    List.of(KlangLexer.LIT_STRING, KlangLexer.EOF));
        }

        @Test
        void noStringWhenClosingQuoteIsEscaped() {
            assertEquals(List.of(
                    KlangLexer.DQUOTE, KlangLexer.IDENT, KlangLexer.ANY, KlangLexer.DQUOTE, KlangLexer.EOF),
                    getTokenTypesFromText("\"abc\\\""));
        }

        @Test
        void literalBackslashThenLiteralQuoteInString() {
            assertStringTokenTextAndTokenTypes("\"\\\\\\\"\"", "\"\\\\\\\"\"", 0,
                    List.of(KlangLexer.LIT_STRING, KlangLexer.EOF));
        }
    }

    @Test
    void testFunDef() {
        var tokens = getTokenTypesFromText("fn f(a:A,)-> f64{return 42 as f64;}");
        List<Integer> expTokens = List.of(
                KlangLexer.KW_FUN, KlangLexer.IDENT, KlangLexer.LPAR,
                KlangLexer.IDENT, KlangLexer.COLON, KlangLexer.IDENT, KlangLexer.COMMA,
                KlangLexer.RPAR, KlangLexer.RARROW, KlangLexer.T_F64,
                KlangLexer.LBRACE, KlangLexer.KW_RETURN, KlangLexer.LIT_INTEGER,
                KlangLexer.KW_AS, KlangLexer.T_F64, KlangLexer.SEMI, KlangLexer.RBRACE,
                KlangLexer.EOF);
        assertEquals(expTokens, tokens);
    }

    // @Test
    // void testBuiltinTypes() {
    //     var tokens = getTokenTypesFromText("fn bool() -> void{}");
    //     List<Integer> expTokens = List.of(
    //             KlangLexer.KW_FUN, KlangLexer.IDENT, KlangLexer.LPAR, KlangLexer.RPAR,
    //             KlangLexer.RARROW, KlangLexer.T_VOID,
    //             KlangLexer.LBRACE, KlangLexer.RBRACE,
    //             KlangLexer.EOF);
    //     assertEquals(expTokens, tokens);
    // }

    @Test
    void testBooleanExpr() {
        var tokens = getTokenTypesFromText("/*modulo*/!(number-(number/2)*-2==0)");
        List<Integer> expTokens = List.of(
                KlangLexer.NOT, KlangLexer.LPAR, KlangLexer.IDENT, KlangLexer.SUB,
                KlangLexer.LPAR, KlangLexer.IDENT, KlangLexer.DIV, KlangLexer.LIT_INTEGER,
                KlangLexer.RPAR, KlangLexer.MULT, KlangLexer.LIT_INTEGER,
                KlangLexer.EQEQ, KlangLexer.LIT_INTEGER, KlangLexer.RPAR,
                KlangLexer.EOF);
        assertEquals(expTokens, tokens);
    }

    @Test
    void testTernaryExpr() {
        var tokens = getTokenTypesFromText("let a:TheType = cond?true:false;");
        List<Integer> expTokens = List.of(
                KlangLexer.KW_LET, KlangLexer.IDENT, KlangLexer.COLON, KlangLexer.IDENT,
                KlangLexer.EQ, KlangLexer.IDENT, KlangLexer.QM,
                KlangLexer.TRUE, KlangLexer.COLON, KlangLexer.FALSE, KlangLexer.SEMI,
                KlangLexer.EOF);
        assertEquals(expTokens, tokens);
    }

    @Test
    void testIfElse() {
        var tokens = getTokenTypesFromText("if a>=b{return a;}else{return b;}");
        List<Integer> expTokens = List.of(
                KlangLexer.KW_IF, KlangLexer.IDENT, KlangLexer.GTEQ, KlangLexer.IDENT,
                KlangLexer.LBRACE, KlangLexer.KW_RETURN, KlangLexer.IDENT, KlangLexer.SEMI,
                KlangLexer.RBRACE,
                KlangLexer.KW_ELSE, KlangLexer.LBRACE, KlangLexer.KW_RETURN, KlangLexer.IDENT,
                KlangLexer.SEMI, KlangLexer.RBRACE,
                KlangLexer.EOF);
        assertEquals(expTokens, tokens);
    }

    @Test
    void testStruct1() {
        var tokens = getTokenTypesFromText("struct MyStruct\n{\nfield1:i64,\nfield2:Foo\n}");
        List<Integer> expTokens = List.of(
                KlangLexer.KW_STRUCT, KlangLexer.IDENT, KlangLexer.LBRACE,
                KlangLexer.IDENT, KlangLexer.COLON, KlangLexer.T_I64, KlangLexer.COMMA,
                KlangLexer.IDENT, KlangLexer.COLON, KlangLexer.IDENT, KlangLexer.RBRACE,
                KlangLexer.EOF);
        assertEquals(expTokens, tokens);
        return;
    }

    @Test
    void testStruct2() {
        var tokens = getTokenTypesFromText("myStruct.structField.field1=null;");
        List<Integer> expTokens = List.of(
                KlangLexer.IDENT, KlangLexer.DOT, KlangLexer.IDENT, KlangLexer.DOT,
                KlangLexer.IDENT, KlangLexer.EQ, KlangLexer.KW_NULL, KlangLexer.SEMI,
                KlangLexer.EOF);
        assertEquals(expTokens, tokens);
        return;
    }

    //
    // Helper stuff
    //
    private ANTLRErrorListener errorListener = new LexerErrorListener();

    private List<Integer> getTokenTypesFromText(String txt) {
        return getTokenTypes(getTokensFromText(txt));
    }

    private List<Integer> getTokenTypes(List<Token> tokens) {
        return tokens.stream().map(t -> t.getType()).toList();
    }

    private List<Token> getTokensFromText(String txt) {
        var strReader = new StringReader(txt);
        Lexer lexer;
        try {
            lexer = new KlangLexer(CharStreams.fromReader(strReader));
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
