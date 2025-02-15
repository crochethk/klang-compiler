package cc.crochethk.klang;

import static cc.crochethk.klang.StringEscapeUtils.resolveEscapeSequences;
import static cc.crochethk.klang.StringEscapeUtils.unresolveEscapeSequences;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class StringEscapeUtilsTest {
    @Nested
    class UnresolveEscapeSequencesTests {
        @Test
        void unresolveDoubleQuote() {
            assertEquals("\\\"quote\\\"", unresolveEscapeSequences("\"quote\""));
        }

        @Test
        void unresolveBackslash() {
            assertEquals("\\\\path\\\\to\\\\file",
                    unresolveEscapeSequences("\\path\\to\\file"));
        }

        @Test
        void unresolveNewlineTabCarriageReturn() {
            assertEquals("nl:\\n, tab:\\t, cr: \\r",
                    unresolveEscapeSequences("nl:\n, tab:\t, cr: \r"));
        }

        @Test
        void unresolveTwoBackslashes() {
            assertEquals("2 backslashes: \\\\",
                    unresolveEscapeSequences("2 backslashes: \\"));
        }

        @Test
        void unresolveBackslashThenQuote() {
            assertEquals("\\\\\\\"", unresolveEscapeSequences("\\\""));
        }

        @Test
        void unresolveEscapedNewlineIsNotNewlineCharacter() {
            assertEquals("\\\\n", unresolveEscapeSequences("\\n"));
        }
    }

    @Nested
    class ResolveEscapeSequencesTests {
        @Test
        void resolveDoubleQuote() {
            assertEquals("\"quote\"", resolveEscapeSequences("\\\"quote\\\""));
        }

        @Test
        void resolveBackslash() {
            assertEquals("\\path\\to\\file",
                    resolveEscapeSequences("\\\\path\\\\to\\\\file"));
        }

        @Test
        void resolveUnsupportedEscapeIgnoresTheChar() {
            assertEquals("UnsupportedEscape",
                    resolveEscapeSequences("Unsupported\\xEscape"));
        }

        @Test
        void resolveNewlineTabCarriageReturn() {
            assertEquals("nl:\n, tab:\t, cr: \r",
                    resolveEscapeSequences("nl:\\n, tab:\\t, cr: \\r"));
        }

        @Test
        public void resolveTwoBackslashes() {
            assertEquals("2 backslashes: \\",
                    resolveEscapeSequences("2 backslashes: \\\\"));
        }

        @Test
        void resolveBackslashThenQuote() {
            assertEquals("\\\"", resolveEscapeSequences("\\\\\\\""));
        }

        @Test
        void resolveEscapedNewlineIsNotNewlineCharacter() {
            assertEquals("\\n", resolveEscapeSequences("\\\\n"));
        }
    }
}
