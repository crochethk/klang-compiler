package utils;

import java.nio.charset.StandardCharsets;

public class Utf8Helper {
    /**
     * Returns new string with all characters outside ASCII code range [32, 126]
     * are replaced by their respective utf-8 octal escape sequence.
     */
    public static String octalEscapeNonAscii(String input) {
        StringBuilder sb = new StringBuilder();

        byte[] utf8Bytes = input.getBytes(StandardCharsets.UTF_8);
        for (byte b : utf8Bytes) {
            // Convert to integer ignoring potential byte sign
            int uByte = b & 0xFF;

            if (uByte >= 32 && uByte <= 126) {
                sb.append((char) uByte);
            } else {
                // Non-ASCII or control character: append octal escape sequence
                sb.append("\\").append(String.format("%03o", uByte));
            }
        }
        return sb.toString();
    }
}
