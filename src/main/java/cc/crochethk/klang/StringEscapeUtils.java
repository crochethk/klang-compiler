package cc.crochethk.klang;

import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class StringEscapeUtils {
    private final static Map<String, String> escapeSequencesMap = Map.of(
            "\\\"", "\"",
            "\\\\", "\\",
            "\\n", "\n",
            "\\t", "\t",
            "\\r", "\r");

    // Reverse mapping: resolved character -> escape sequence
    private static final Map<String, String> resolvedToEscaped = escapeSequencesMap.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));

    public static String resolveEscapeSequences(String s) {
        var escapedSeqs = escapeSequencesMap.keySet();
        // Regex matching the escaped special chars
        var regexStr = String.join("|",
                escapedSeqs.stream().map(escSeq -> Pattern.quote(escSeq)).toList());
        var regex = Pattern.compile(regexStr);

        var escChunks = regex.splitWithDelimiters(s, 0);
        for (int i = 0; i < escChunks.length; i++) {
            var resolvedSeq = escapeSequencesMap.get(escChunks[i]);
            if (resolvedSeq != null) {
                // replace escaped by actual char
                escChunks[i] = resolvedSeq;
            } else {
                // remove all chars with '\' prefix
                escChunks[i] = escChunks[i].replaceAll(Pattern.quote("\\") + ".", "");
            }
        }
        return String.join("", escChunks);
    }

    public static String unresolveEscapeSequences(String s) {
        var sb = new StringBuilder();

        for (char ch : s.toCharArray()) {
            var key = String.valueOf(ch);
            sb.append(resolvedToEscaped.getOrDefault(key, key));
        }

        return sb.toString();
    }
}