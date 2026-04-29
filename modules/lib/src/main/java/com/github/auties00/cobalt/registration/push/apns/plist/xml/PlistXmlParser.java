package com.github.auties00.cobalt.registration.push.apns.plist.xml;

import com.github.auties00.cobalt.registration.push.apns.plist.value.PlistArrayValue;
import com.github.auties00.cobalt.registration.push.apns.plist.value.PlistBooleanValue;
import com.github.auties00.cobalt.registration.push.apns.plist.value.PlistDataValue;
import com.github.auties00.cobalt.registration.push.apns.plist.value.PlistDateValue;
import com.github.auties00.cobalt.registration.push.apns.plist.value.PlistDictionaryValue;
import com.github.auties00.cobalt.registration.push.apns.plist.value.PlistFloatingPointValue;
import com.github.auties00.cobalt.registration.push.apns.plist.value.PlistIntegerValue;
import com.github.auties00.cobalt.registration.push.apns.plist.value.PlistStringValue;
import com.github.auties00.cobalt.registration.push.apns.plist.value.PlistValue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;

/**
 * Recursive-descent parser for Apple's XML property-list format,
 * tailored to the small subset Cobalt's APNS code exchanges with
 * Apple's activation and bag endpoints — no streaming, no
 * namespaces, no DTD validation.
 *
 * <p>The parser is single-pass over the source {@code byte[]} (no
 * {@code Reader} wrappers) and tolerates the {@code <?xml?>}
 * prolog, a single {@code <!DOCTYPE>}, and {@code <!-- ... -->}
 * comments interleaved between elements; everything else is
 * rejected with an {@link IOException}.
 *
 * <p>Callers normally route through the
 * {@code Plist} facade; this class is the implementation.
 */
public final class PlistXmlParser {
    /**
     * Source bytes.
     */
    private final byte[] src;

    /**
     * Current read position.
     */
    private int pos;

    /**
     * Constructs a parser bound to {@code src}.
     *
     * @param src the source bytes
     */
    private PlistXmlParser(byte[] src) {
        this.src = src;
    }

    /**
     * Parses {@code data} into a {@link PlistValue} tree.
     *
     * @param data the XML plist bytes
     * @return the parsed root value
     * @throws IOException if the source is malformed
     */
    public static PlistValue parse(byte[] data) throws IOException {
        return new PlistXmlParser(data).parseRoot();
    }

    /**
     * Drives the parse: skips the prolog, opens the
     * {@code <plist>} root, parses one child value.
     *
     * @return the root value
     * @throws IOException if the source is malformed
     */
    private PlistValue parseRoot() throws IOException {
        skipMisc();
        if (matchAt("<plist")) {
            pos = indexOf('>') + 1;
            skipMisc();
        }
        return parseValue();
    }

    /**
     * Reads exactly one value element starting at the current
     * position.
     *
     * @return the parsed value
     * @throws IOException if the element is unknown or malformed
     */
    private PlistValue parseValue() throws IOException {
        skipMisc();
        if (pos >= src.length || src[pos] != '<') {
            throw new IOException("expected '<' at " + pos);
        }
        pos++;
        var tagStart = pos;
        while (pos < src.length) {
            var c = src[pos];
            if (c == '>' || c == '/' || c == ' ' || c == '\t' || c == '\n' || c == '\r') {
                break;
            }
            pos++;
        }
        var tagName = new String(src, tagStart, pos - tagStart, StandardCharsets.US_ASCII);
        var selfClose = false;
        while (pos < src.length && src[pos] != '>') {
            if (src[pos] == '/') {
                selfClose = true;
            }
            pos++;
        }
        if (pos >= src.length) {
            throw new IOException("unterminated tag <" + tagName);
        }
        pos++;
        return switch (tagName) {
            case "true" -> {
                if (!selfClose) {
                    consumeClose("true");
                }
                yield new PlistBooleanValue(true);
            }
            case "false" -> {
                if (!selfClose) {
                    consumeClose("false");
                }
                yield new PlistBooleanValue(false);
            }
            case "string" -> selfClose
                    ? new PlistStringValue("")
                    : new PlistStringValue(readTextAndClose("string"));
            case "integer" -> new PlistIntegerValue(Long.parseLong(readTextAndClose("integer").trim()));
            case "real" -> new PlistFloatingPointValue(Double.parseDouble(readTextAndClose("real").trim()));
            case "data" -> {
                if (selfClose) {
                    yield new PlistDataValue(new byte[0], 0, 0);
                }
                var raw = readTextAndClose("data");
                var decoded = Base64.getMimeDecoder().decode(raw);
                yield new PlistDataValue(decoded, 0, decoded.length);
            }
            case "date" -> new PlistDateValue(Instant.parse(readTextAndClose("date").trim()));
            case "dict" -> selfClose ? new PlistDictionaryValue(new LinkedHashMap<>()) : parseDict();
            case "array" -> selfClose ? new PlistArrayValue(new ArrayList<>()) : parseArray();
            default -> throw new IOException("unknown plist element <" + tagName + ">");
        };
    }

    /**
     * Reads alternating {@code <key>} / value pairs until
     * {@code </dict>} is reached.
     *
     * @return the parsed dictionary
     * @throws IOException if the contents are malformed
     */
    private PlistDictionaryValue parseDict() throws IOException {
        var entries = new LinkedHashMap<String, PlistValue>();
        while (true) {
            skipMisc();
            if (matchAt("</dict>")) {
                pos += "</dict>".length();
                return new PlistDictionaryValue(entries);
            }
            if (!matchAt("<key>")) {
                throw new IOException("expected <key> at " + pos);
            }
            pos += "<key>".length();
            var keyStart = pos;
            while (pos < src.length && src[pos] != '<') {
                pos++;
            }
            var key = decodeEntities(new String(src, keyStart, pos - keyStart, StandardCharsets.UTF_8));
            if (!matchAt("</key>")) {
                throw new IOException("expected </key> at " + pos);
            }
            pos += "</key>".length();
            entries.put(key, parseValue());
        }
    }

    /**
     * Reads values until {@code </array>} is reached.
     *
     * @return the parsed array
     * @throws IOException if the contents are malformed
     */
    private PlistArrayValue parseArray() throws IOException {
        var items = new ArrayList<PlistValue>();
        while (true) {
            skipMisc();
            if (matchAt("</array>")) {
                pos += "</array>".length();
                return new PlistArrayValue(items);
            }
            items.add(parseValue());
        }
    }

    /**
     * Reads the text content up to the next {@code <}, then the
     * matching closing tag.
     *
     * @param tag the element name (used to validate the close)
     * @return the entity-decoded text content
     * @throws IOException if the close tag is missing
     */
    private String readTextAndClose(String tag) throws IOException {
        var start = pos;
        while (pos < src.length && src[pos] != '<') {
            pos++;
        }
        var raw = new String(src, start, pos - start, StandardCharsets.UTF_8);
        consumeClose(tag);
        return decodeEntities(raw);
    }

    /**
     * Consumes the closing tag {@code </tag>} at the current
     * position.
     *
     * @param tag the element name
     * @throws IOException if the expected tag is not present
     */
    private void consumeClose(String tag) throws IOException {
        var close = "</" + tag + ">";
        if (!matchAt(close)) {
            throw new IOException("expected " + close + " at " + pos);
        }
        pos += close.length();
    }

    /**
     * Skips whitespace, XML/processing-instruction prologs,
     * {@code <!DOCTYPE>} declarations, and comments — in any
     * order, repeatedly.
     *
     * @throws IOException if a comment or doctype is unterminated
     */
    private void skipMisc() throws IOException {
        while (pos < src.length) {
            var c = src[pos];
            if (c == ' ' || c == '\t' || c == '\n' || c == '\r') {
                pos++;
                continue;
            }
            if (matchAt("<?")) {
                var end = indexOfPair("?>");
                if (end < 0) {
                    throw new IOException("unterminated <?...?>");
                }
                pos = end + 2;
                continue;
            }
            if (matchAt("<!--")) {
                var end = indexOfPair("-->");
                if (end < 0) {
                    throw new IOException("unterminated <!-- -->");
                }
                pos = end + 3;
                continue;
            }
            if (matchAt("<!DOCTYPE") || matchAt("<!doctype")) {
                pos = indexOf('>') + 1;
                continue;
            }
            break;
        }
    }

    /**
     * Returns {@code true} if {@code expected} appears literally at
     * the current read position.
     *
     * @param expected the literal to test
     * @return whether the literal is at {@link #pos}
     */
    private boolean matchAt(String expected) {
        if (pos + expected.length() > src.length) {
            return false;
        }
        for (int i = 0; i < expected.length(); i++) {
            if (src[pos + i] != (byte) expected.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the next position of {@code target} starting from
     * {@link #pos}, or {@code -1} if the source ends first.
     *
     * @param target the byte to find
     * @return the index, or {@code -1}
     */
    private int indexOf(char target) {
        for (int i = pos; i < src.length; i++) {
            if (src[i] == target) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Returns the next position at which the two-character
     * sequence {@code pair} starts, or {@code -1} on EOF.
     *
     * @param pair the two-or-more character sequence
     * @return the index, or {@code -1}
     */
    private int indexOfPair(String pair) {
        outer:
        for (int i = pos; i + pair.length() <= src.length; i++) {
            for (int j = 0; j < pair.length(); j++) {
                if (src[i + j] != (byte) pair.charAt(j)) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }

    /**
     * Decodes the XML entities Cobalt may encounter in plist
     * payloads ({@code &amp;}, {@code &lt;}, {@code &gt;},
     * {@code &quot;}, {@code &apos;}, plus numeric
     * {@code &#NN;}). Untranslated entities are passed through
     * verbatim.
     *
     * @param raw the raw text
     * @return the decoded text
     */
    private static String decodeEntities(String raw) {
        if (raw.indexOf('&') < 0) {
            return raw;
        }
        var sb = new StringBuilder(raw.length());
        var i = 0;
        while (i < raw.length()) {
            var c = raw.charAt(i);
            if (c != '&') {
                sb.append(c);
                i++;
                continue;
            }
            var semi = raw.indexOf(';', i);
            if (semi < 0) {
                sb.append(raw, i, raw.length());
                break;
            }
            var name = raw.substring(i + 1, semi);
            switch (name) {
                case "amp" -> sb.append('&');
                case "lt" -> sb.append('<');
                case "gt" -> sb.append('>');
                case "quot" -> sb.append('"');
                case "apos" -> sb.append('\'');
                default -> {
                    if (name.startsWith("#x") || name.startsWith("#X")) {
                        sb.appendCodePoint(Integer.parseInt(name, 2, name.length(), 16));
                    } else if (name.startsWith("#")) {
                        sb.appendCodePoint(Integer.parseInt(name, 1, name.length(), 10));
                    } else {
                        sb.append('&').append(name).append(';');
                    }
                }
            }
            i = semi + 1;
        }
        return sb.toString();
    }
}
