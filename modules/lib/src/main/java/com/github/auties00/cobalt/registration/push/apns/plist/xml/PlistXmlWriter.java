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

import java.nio.charset.StandardCharsets;
import java.time.Instant;

/**
 * Two-pass exact-allocation writer for Apple's XML property-list
 * format.
 *
 * <p>Pass one walks the tree and computes the precise UTF-8 byte
 * count. Pass two fills a single {@code byte[]} of that exact size
 *. No {@code StringBuilder} growth, no intermediate {@code String}
 * conversion, no {@code CharsetEncoder} pass. All XML element tags
 * and entity replacements are pre-encoded as {@code byte[]}
 * constants.
 *
 * <p>Callers normally route through the
 * {@code Plist} facade. This class is the implementation.
 */
public final class PlistXmlWriter {
    /**
     * XML preamble. Matches the canonical Apple
     * {@code PropertyList-1.0.dtd} declaration so Apple's parsers
     * accept the bytes verbatim.
     */
    private static final byte[] PREAMBLE = """
            <?xml version="1.0" encoding="UTF-8"?>
            <!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
            <plist version="1.0">
            """.getBytes(StandardCharsets.US_ASCII);

    /**
     * Closing {@code </plist>} block.
     */
    private static final byte[] EPILOGUE = "\n</plist>\n".getBytes(StandardCharsets.US_ASCII);

    /**
     * Opening {@code <dict>} tag with trailing newline.
     */
    private static final byte[] DICT_OPEN = {'<', 'd', 'i', 'c', 't', '>', '\n'};
    /** Closing {@code </dict>} tag. */
    private static final byte[] DICT_CLOSE = {'<', '/', 'd', 'i', 'c', 't', '>'};
    /** Opening {@code <array>} tag with trailing newline. */
    private static final byte[] ARRAY_OPEN = {'<', 'a', 'r', 'r', 'a', 'y', '>', '\n'};
    /** Closing {@code </array>} tag. */
    private static final byte[] ARRAY_CLOSE = {'<', '/', 'a', 'r', 'r', 'a', 'y', '>'};
    /** Opening {@code <key>} tag. */
    private static final byte[] KEY_OPEN = {'<', 'k', 'e', 'y', '>'};
    /** Closing {@code </key>} tag with trailing newline. */
    private static final byte[] KEY_CLOSE = {'<', '/', 'k', 'e', 'y', '>', '\n'};
    /** Opening {@code <string>} tag. */
    private static final byte[] STRING_OPEN = {'<', 's', 't', 'r', 'i', 'n', 'g', '>'};
    /** Closing {@code </string>} tag. */
    private static final byte[] STRING_CLOSE = {'<', '/', 's', 't', 'r', 'i', 'n', 'g', '>'};
    /** Opening {@code <data>} tag. */
    private static final byte[] DATA_OPEN = {'<', 'd', 'a', 't', 'a', '>'};
    /** Closing {@code </data>} tag. */
    private static final byte[] DATA_CLOSE = {'<', '/', 'd', 'a', 't', 'a', '>'};
    /** Opening {@code <integer>} tag. */
    private static final byte[] INTEGER_OPEN = {'<', 'i', 'n', 't', 'e', 'g', 'e', 'r', '>'};
    /** Closing {@code </integer>} tag. */
    private static final byte[] INTEGER_CLOSE = {'<', '/', 'i', 'n', 't', 'e', 'g', 'e', 'r', '>'};
    /** Opening {@code <real>} tag. */
    private static final byte[] REAL_OPEN = {'<', 'r', 'e', 'a', 'l', '>'};
    /** Closing {@code </real>} tag. */
    private static final byte[] REAL_CLOSE = {'<', '/', 'r', 'e', 'a', 'l', '>'};
    /** Opening {@code <date>} tag. */
    private static final byte[] DATE_OPEN = {'<', 'd', 'a', 't', 'e', '>'};
    /** Closing {@code </date>} tag. */
    private static final byte[] DATE_CLOSE = {'<', '/', 'd', 'a', 't', 'e', '>'};
    /** Self-closing {@code <true/>} tag. */
    private static final byte[] TRUE_TAG = {'<', 't', 'r', 'u', 'e', '/', '>'};
    /** Self-closing {@code <false/>} tag. */
    private static final byte[] FALSE_TAG = {'<', 'f', 'a', 'l', 's', 'e', '/', '>'};
    /** Replacement bytes for the {@code &} character. */
    private static final byte[] AMP_ENTITY = {'&', 'a', 'm', 'p', ';'};
    /** Replacement bytes for the {@code <} character. */
    private static final byte[] LT_ENTITY = {'&', 'l', 't', ';'};
    /** Replacement bytes for the {@code >} character. */
    private static final byte[] GT_ENTITY = {'&', 'g', 't', ';'};
    /** {@code -9223372036854775808} pre-encoded. */
    private static final byte[] LONG_MIN_VALUE = "-9223372036854775808".getBytes(StandardCharsets.US_ASCII);
    /** Standard Base64 alphabet, indexed by 6-bit value. */
    private static final byte[] BASE64_ALPHABET =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".getBytes(StandardCharsets.US_ASCII);

    /**
     * Hidden constructor. The class is a stateless namespace.
     */
    private PlistXmlWriter() {
    }

    /**
     * Serializes {@code root} as an XML plist with the canonical
     * Apple preamble.
     *
     * @param root the root value
     * @return the UTF-8 encoded XML bytes
     */
    public static byte[] write(PlistValue root) {
        var size = PREAMBLE.length + sizeOf(root, 0) + EPILOGUE.length;
        var out = new byte[size];
        var pos = writeBytes(out, 0, PREAMBLE);
        pos = writeValue(out, pos, root, 0);
        writeBytes(out, pos, EPILOGUE);
        return out;
    }

    /**
     * Returns the exact UTF-8 byte count required to serialize
     * {@code value} at indent depth {@code indent}, excluding the
     * preamble and epilogue. Recurses into containers.
     *
     * @param value  the value to size
     * @param indent the indentation depth (in tabs)
     * @return the byte count
     */
    private static int sizeOf(PlistValue value, int indent) {
        return switch (value) {
            case PlistDictionaryValue d -> sizeOfDict(d, indent);
            case PlistArrayValue a -> sizeOfArray(a, indent);
            case PlistStringValue s -> indent + STRING_OPEN.length + escapedXmlByteLength(s.value()) + STRING_CLOSE.length;
            case PlistDataValue d -> indent + DATA_OPEN.length + base64Length(d.length()) + DATA_CLOSE.length;
            case PlistIntegerValue i -> indent + INTEGER_OPEN.length + decimalDigitCount(i.value()) + INTEGER_CLOSE.length;
            case PlistFloatingPointValue r -> indent + REAL_OPEN.length + Double.toString(r.value()).length() + REAL_CLOSE.length;
            case PlistBooleanValue b -> indent + (b.value() ? TRUE_TAG.length : FALSE_TAG.length);
            case PlistDateValue d -> indent + DATE_OPEN.length + d.value().toString().length() + DATE_CLOSE.length;
        };
    }

    /**
     * Returns the byte count for a dictionary node. Open tag,
     * indented {@code <key>} / value pairs separated by newlines,
     * close tag.
     *
     * @param dict   the dictionary
     * @param indent the indentation depth
     * @return the byte count
     */
    private static int sizeOfDict(PlistDictionaryValue dict, int indent) {
        var total = indent + DICT_OPEN.length;
        var childIndent = indent + 1;
        for (var entry : dict.entries().entrySet()) {
            total += childIndent + KEY_OPEN.length + escapedXmlByteLength(entry.getKey()) + KEY_CLOSE.length;
            total += sizeOf(entry.getValue(), childIndent) + 1;
        }
        total += indent + DICT_CLOSE.length;
        return total;
    }

    /**
     * Returns the byte count for an array node.
     *
     * @param array  the array
     * @param indent the indentation depth
     * @return the byte count
     */
    private static int sizeOfArray(PlistArrayValue array, int indent) {
        var total = indent + ARRAY_OPEN.length;
        var childIndent = indent + 1;
        for (var item : array.items()) {
            total += sizeOf(item, childIndent) + 1;
        }
        total += indent + ARRAY_CLOSE.length;
        return total;
    }

    /**
     * Recursive byte-emitter. Dispatches on the sealed
     * {@link PlistValue} hierarchy and writes directly into
     * {@code out} starting at {@code pos}.
     *
     * @param out    the destination buffer
     * @param pos    the current write position
     * @param value  the value to emit
     * @param indent the current indentation depth (in tabs)
     * @return the position after the value
     */
    private static int writeValue(byte[] out, int pos, PlistValue value, int indent) {
        return switch (value) {
            case PlistDictionaryValue d -> writeDict(out, pos, d, indent);
            case PlistArrayValue a -> writeArray(out, pos, a, indent);
            case PlistStringValue s -> {
                var p = writeIndent(out, pos, indent);
                p = writeBytes(out, p, STRING_OPEN);
                p = writeXmlEscaped(out, p, s.value());
                yield writeBytes(out, p, STRING_CLOSE);
            }
            case PlistDataValue d -> {
                var p = writeIndent(out, pos, indent);
                p = writeBytes(out, p, DATA_OPEN);
                p = writeBase64(out, p, d.source(), d.offset(), d.length());
                yield writeBytes(out, p, DATA_CLOSE);
            }
            case PlistIntegerValue i -> {
                var p = writeIndent(out, pos, indent);
                p = writeBytes(out, p, INTEGER_OPEN);
                p = writeLong(out, p, i.value());
                yield writeBytes(out, p, INTEGER_CLOSE);
            }
            case PlistFloatingPointValue r -> {
                var p = writeIndent(out, pos, indent);
                p = writeBytes(out, p, REAL_OPEN);
                p = writeAsciiString(out, p, Double.toString(r.value()));
                yield writeBytes(out, p, REAL_CLOSE);
            }
            case PlistBooleanValue b -> {
                var p = writeIndent(out, pos, indent);
                yield writeBytes(out, p, b.value() ? TRUE_TAG : FALSE_TAG);
            }
            case PlistDateValue d -> {
                var p = writeIndent(out, pos, indent);
                p = writeBytes(out, p, DATE_OPEN);
                p = writeAsciiString(out, p, d.value().toString());
                yield writeBytes(out, p, DATE_CLOSE);
            }
        };
    }

    /**
     * Emits a dictionary with one {@code <key>}/value pair per
     * line.
     *
     * @param out    the destination buffer
     * @param pos    the current write position
     * @param dict   the dictionary
     * @param indent the current indentation depth
     * @return the position after the dictionary
     */
    private static int writeDict(byte[] out, int pos, PlistDictionaryValue dict, int indent) {
        pos = writeIndent(out, pos, indent);
        pos = writeBytes(out, pos, DICT_OPEN);
        var childIndent = indent + 1;
        for (var entry : dict.entries().entrySet()) {
            pos = writeIndent(out, pos, childIndent);
            pos = writeBytes(out, pos, KEY_OPEN);
            pos = writeXmlEscaped(out, pos, entry.getKey());
            pos = writeBytes(out, pos, KEY_CLOSE);
            pos = writeValue(out, pos, entry.getValue(), childIndent);
            out[pos++] = '\n';
        }
        pos = writeIndent(out, pos, indent);
        return writeBytes(out, pos, DICT_CLOSE);
    }

    /**
     * Emits an array with one entry per line.
     *
     * @param out    the destination buffer
     * @param pos    the current write position
     * @param array  the array
     * @param indent the current indentation depth
     * @return the position after the array
     */
    private static int writeArray(byte[] out, int pos, PlistArrayValue array, int indent) {
        pos = writeIndent(out, pos, indent);
        pos = writeBytes(out, pos, ARRAY_OPEN);
        var childIndent = indent + 1;
        for (var item : array.items()) {
            pos = writeValue(out, pos, item, childIndent);
            out[pos++] = '\n';
        }
        pos = writeIndent(out, pos, indent);
        return writeBytes(out, pos, ARRAY_CLOSE);
    }

    /**
     * Copies {@code src} into {@code out} at {@code pos}.
     *
     * @param out the destination buffer
     * @param pos the current write position
     * @param src the bytes to copy
     * @return the position after the copy
     */
    private static int writeBytes(byte[] out, int pos, byte[] src) {
        System.arraycopy(src, 0, out, pos, src.length);
        return pos + src.length;
    }

    /**
     * Writes {@code count} tab characters at {@code pos}.
     *
     * @param out   the destination buffer
     * @param pos   the current write position
     * @param count the indentation depth
     * @return the position after the tabs
     */
    private static int writeIndent(byte[] out, int pos, int count) {
        for (int i = 0; i < count; i++) {
            out[pos++] = '\t';
        }
        return pos;
    }

    /**
     * Writes {@code value} as UTF-8 with XML entity escaping for
     * {@code &}, {@code <}, and {@code >}.
     *
     * @param out   the destination buffer
     * @param pos   the current write position
     * @param value the source string
     * @return the position after the encoded text
     */
    private static int writeXmlEscaped(byte[] out, int pos, String value) {
        for (int i = 0; i < value.length(); i++) {
            var c = value.charAt(i);
            switch (c) {
                case '&' -> pos = writeBytes(out, pos, AMP_ENTITY);
                case '<' -> pos = writeBytes(out, pos, LT_ENTITY);
                case '>' -> pos = writeBytes(out, pos, GT_ENTITY);
                default -> {
                    if (c < 0x80) {
                        out[pos++] = (byte) c;
                    } else if (c < 0x800) {
                        out[pos++] = (byte) (0xC0 | (c >> 6));
                        out[pos++] = (byte) (0x80 | (c & 0x3F));
                    } else if (Character.isHighSurrogate(c) && i + 1 < value.length()
                            && Character.isLowSurrogate(value.charAt(i + 1))) {
                        var cp = Character.toCodePoint(c, value.charAt(++i));
                        out[pos++] = (byte) (0xF0 | (cp >> 18));
                        out[pos++] = (byte) (0x80 | ((cp >> 12) & 0x3F));
                        out[pos++] = (byte) (0x80 | ((cp >> 6) & 0x3F));
                        out[pos++] = (byte) (0x80 | (cp & 0x3F));
                    } else {
                        out[pos++] = (byte) (0xE0 | (c >> 12));
                        out[pos++] = (byte) (0x80 | ((c >> 6) & 0x3F));
                        out[pos++] = (byte) (0x80 | (c & 0x3F));
                    }
                }
            }
        }
        return pos;
    }

    /**
     * Writes an already-ASCII string verbatim. Used for the small
     * outputs of {@link Double#toString(double)} and
     * {@link Instant#toString()}, neither of which can produce
     * non-ASCII characters.
     *
     * @param out the destination buffer
     * @param pos the current write position
     * @param s   the ASCII source string
     * @return the position after the bytes
     */
    private static int writeAsciiString(byte[] out, int pos, String s) {
        for (int i = 0; i < s.length(); i++) {
            out[pos++] = (byte) s.charAt(i);
        }
        return pos;
    }

    /**
     * Writes the decimal representation of {@code value}, including
     * a leading minus sign when negative. Avoids
     * {@link Long#toString(long)} so no intermediate {@link String}
     * is allocated.
     *
     * @param out   the destination buffer
     * @param pos   the current write position
     * @param value the integer value
     * @return the position after the digits
     */
    private static int writeLong(byte[] out, int pos, long value) {
        if (value == Long.MIN_VALUE) {
            return writeBytes(out, pos, LONG_MIN_VALUE);
        }
        if (value == 0) {
            out[pos++] = '0';
            return pos;
        }
        var negative = value < 0;
        if (negative) {
            value = -value;
            out[pos++] = '-';
        }
        var digitCount = 0;
        var probe = value;
        while (probe > 0) {
            digitCount++;
            probe /= 10;
        }
        var end = pos + digitCount;
        var writeAt = end - 1;
        while (value > 0) {
            out[writeAt--] = (byte) ('0' + (value % 10));
            value /= 10;
        }
        return end;
    }

    /**
     * Writes the Base64 encoding of {@code src[srcOffset .. SrcOffset
     * + srcLength)} directly into {@code out} starting at
     * {@code pos}, with no intermediate copy of the source slice.
     *
     * @param out       the destination buffer
     * @param pos       the current write position
     * @param src       the source buffer
     * @param srcOffset the start offset within {@code src}
     * @param srcLength the number of source bytes to encode
     * @return the position after the encoded bytes
     */
    private static int writeBase64(byte[] out, int pos, byte[] src, int srcOffset, int srcLength) {
        var srcEnd = srcOffset + srcLength;
        var i = srcOffset;
        while (i + 3 <= srcEnd) {
            var b = ((src[i] & 0xFF) << 16) | ((src[i + 1] & 0xFF) << 8) | (src[i + 2] & 0xFF);
            out[pos++] = BASE64_ALPHABET[(b >> 18) & 0x3F];
            out[pos++] = BASE64_ALPHABET[(b >> 12) & 0x3F];
            out[pos++] = BASE64_ALPHABET[(b >> 6) & 0x3F];
            out[pos++] = BASE64_ALPHABET[b & 0x3F];
            i += 3;
        }
        var remaining = srcEnd - i;
        if (remaining == 1) {
            var b = (src[i] & 0xFF) << 16;
            out[pos++] = BASE64_ALPHABET[(b >> 18) & 0x3F];
            out[pos++] = BASE64_ALPHABET[(b >> 12) & 0x3F];
            out[pos++] = '=';
            out[pos++] = '=';
        } else if (remaining == 2) {
            var b = ((src[i] & 0xFF) << 16) | ((src[i + 1] & 0xFF) << 8);
            out[pos++] = BASE64_ALPHABET[(b >> 18) & 0x3F];
            out[pos++] = BASE64_ALPHABET[(b >> 12) & 0x3F];
            out[pos++] = BASE64_ALPHABET[(b >> 6) & 0x3F];
            out[pos++] = '=';
        }
        return pos;
    }

    /**
     * Returns the UTF-8 byte count for {@code s} after XML entity
     * escaping is applied to {@code &}, {@code <}, and {@code >}.
     *
     * @param s the source string
     * @return the byte count
     */
    private static int escapedXmlByteLength(String s) {
        var len = 0;
        for (int i = 0; i < s.length(); i++) {
            var c = s.charAt(i);
            switch (c) {
                case '&' -> len += AMP_ENTITY.length;
                case '<' -> len += LT_ENTITY.length;
                case '>' -> len += GT_ENTITY.length;
                default -> {
                    if (c < 0x80) {
                        len++;
                    } else if (c < 0x800) {
                        len += 2;
                    } else if (Character.isHighSurrogate(c) && i + 1 < s.length()
                            && Character.isLowSurrogate(s.charAt(i + 1))) {
                        len += 4;
                        i++;
                    } else {
                        len += 3;
                    }
                }
            }
        }
        return len;
    }

    /**
     * Returns the length of the Base64 encoding (with padding) of a
     * source of {@code srcLength} bytes.
     *
     * @param srcLength the source length
     * @return the encoded length
     */
    private static int base64Length(int srcLength) {
        return ((srcLength + 2) / 3) * 4;
    }

    /**
     * Returns the number of characters {@code Long.toString(value)}
     * would produce (digits plus optional sign), without allocating
     * a {@link String}.
     *
     * @param value the integer value
     * @return the character count
     */
    private static int decimalDigitCount(long value) {
        if (value == Long.MIN_VALUE) {
            return LONG_MIN_VALUE.length;
        }
        var sign = value < 0 ? 1 : 0;
        var abs = value < 0 ? -value : value;
        var count = 1;
        while (abs >= 10) {
            count++;
            abs /= 10;
        }
        return count + sign;
    }
}
