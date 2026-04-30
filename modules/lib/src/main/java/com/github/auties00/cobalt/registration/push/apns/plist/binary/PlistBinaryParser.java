package com.github.auties00.cobalt.registration.push.apns.plist.binary;

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
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * Random-access parser for Apple's {@code bplist00} binary format.
 *
 * <p>The format places a 32-byte trailer at the end of the file
 * carrying the offset-table location, the number of objects, the
 * top-object index, and the byte widths used for offsets and
 * inter-object references. Each object is identified by a single
 * marker byte whose high nibble selects the type and whose low
 * nibble carries either the count or the {@code 1 << n} byte width
 * of the encoded scalar.
 *
 * <p>Strings are decoded into Java {@link String} (allocation is
 * unavoidable). {@code <data>} payloads are exposed as offset/length
 * slices over the source buffer. Nothing is copied.
 *
 * <p>Callers normally route through the
 * {@code Plist} facade. This class is the implementation.
 */
public final class PlistBinaryParser {
    /**
     * Length of the trailer that closes a binary plist.
     */
    private static final int TRAILER_SIZE = 32;

    /**
     * Magic header that identifies a binary plist.
     */
    static final byte[] MAGIC = {'b', 'p', 'l', 'i', 's', 't', '0', '0'};

    /**
     * Source bytes.
     */
    private final byte[] src;

    /**
     * Width in bytes of each offset-table entry.
     */
    private final int offsetSize;

    /**
     * Width in bytes of each inter-object reference.
     */
    private final int refSize;

    /**
     * Index of the top-level object.
     */
    private final int topObject;

    /**
     * Absolute offset of the offset table within {@link #src}.
     */
    private final int offsetTableOffset;

    /**
     * Constructs a parser. Reads and validates the trailer.
     *
     * @param src the source bytes
     * @throws IOException if the source is too short or the trailer
     *                     is malformed
     */
    private PlistBinaryParser(byte[] src) throws IOException {
        if (src.length < MAGIC.length + TRAILER_SIZE) {
            throw new IOException("binary plist too short: " + src.length);
        }
        this.src = src;
        var trailer = src.length - TRAILER_SIZE;
        this.offsetSize = src[trailer + 6] & 0xFF;
        this.refSize = src[trailer + 7] & 0xFF;
        if (offsetSize < 1 || offsetSize > 8 || refSize < 1 || refSize > 8) {
            throw new IOException("invalid trailer widths: offset=" + offsetSize + " ref=" + refSize);
        }
        var numObjects = readUnsignedBigEndian(trailer + 8, 8);
        this.topObject = (int) readUnsignedBigEndian(trailer + 16, 8);
        this.offsetTableOffset = (int) readUnsignedBigEndian(trailer + 24, 8);
        if (offsetTableOffset < 0 || offsetTableOffset > src.length
                || (long) offsetTableOffset + numObjects * offsetSize > src.length) {
            throw new IOException("offset table escapes source: at " + offsetTableOffset
                    + " for " + numObjects + " objects of " + offsetSize + " bytes");
        }
    }

    /**
     * Returns {@code true} if {@code data} carries the
     * {@code bplist00} magic.
     *
     * @param data the source bytes
     * @return {@code true} if binary plist
     */
    public static boolean isBinary(byte[] data) {
        if (data.length < MAGIC.length) {
            return false;
        }
        for (int i = 0; i < MAGIC.length; i++) {
            if (data[i] != MAGIC[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Parses {@code data} into a {@link PlistValue} tree.
     *
     * @param data the binary plist bytes
     * @return the parsed root value
     * @throws IOException if the source is malformed
     */
    public static PlistValue parse(byte[] data) throws IOException {
        var parser = new PlistBinaryParser(data);
        return parser.readObject(parser.topObject);
    }

    /**
     * Resolves the on-disk offset of object {@code index} via the
     * offset table, then decodes it.
     *
     * @param index the object index
     * @return the decoded value
     * @throws IOException if the marker is unknown
     */
    private PlistValue readObject(int index) throws IOException {
        var entry = offsetTableOffset + index * offsetSize;
        var offset = (int) readUnsignedBigEndian(entry, offsetSize);
        return readObjectAt(offset);
    }

    /**
     * Decodes the object at byte {@code offset}.
     *
     * @param offset the absolute offset within {@link #src}
     * @return the decoded value
     * @throws IOException if the marker is unknown or the encoded
     *                     payload escapes the source
     */
    private PlistValue readObjectAt(int offset) throws IOException {
        var marker = src[offset] & 0xFF;
        var type = marker >>> 4;
        var info = marker & 0x0F;
        return switch (type) {
            case 0x0 -> switch (info) {
                case 0x8 -> new PlistBooleanValue(false);
                case 0x9 -> new PlistBooleanValue(true);
                default -> throw new IOException("unsupported singleton 0x0/" + info);
            };
            case 0x1 -> readInteger(offset, info);
            case 0x2 -> readReal(offset, info);
            case 0x3 -> readDate(offset);
            case 0x4 -> readData(offset, info);
            case 0x5 -> readString(offset, info, StandardCharsets.US_ASCII, 1);
            case 0x6 -> readString(offset, info, StandardCharsets.UTF_16BE, 2);
            case 0x7 -> readString(offset, info, StandardCharsets.UTF_8, 1);
            case 0xA -> readArray(offset, info);
            case 0xD -> readDict(offset, info);
            default -> throw new IOException("unsupported plist marker 0x" + Integer.toHexString(marker));
        };
    }

    /**
     * Decodes a {@code 0x1n} integer marker. Widths of 1, 2, and 4
     * bytes are unsigned per Apple's spec; 8 bytes are signed.
     * 16-byte (uint128) integers are rejected.
     *
     * @param offset the marker offset
     * @param info   the low nibble. {@code 1 << info} is the byte
     *               count
     * @return the integer value
     * @throws IOException if the encoded width exceeds 8 bytes
     */
    private PlistIntegerValue readInteger(int offset, int info) throws IOException {
        var byteCount = 1 << info;
        if (byteCount > 8) {
            throw new IOException("16-byte plist integer not supported");
        }
        return new PlistIntegerValue(readUnsignedBigEndian(offset + 1, byteCount));
    }

    /**
     * Decodes a {@code 0x2n} floating-point marker.
     *
     * @param offset the marker offset
     * @param info   the low nibble. {@code 1 << info} is the byte
     *               count (must be 4 or 8)
     * @return the real value
     * @throws IOException if the encoded width is not 4 or 8
     */
    private PlistFloatingPointValue readReal(int offset, int info) throws IOException {
        var byteCount = 1 << info;
        return switch (byteCount) {
            case 4 -> new PlistFloatingPointValue(Float.intBitsToFloat((int) readUnsignedBigEndian(offset + 1, 4)));
            case 8 -> new PlistFloatingPointValue(Double.longBitsToDouble(readUnsignedBigEndian(offset + 1, 8)));
            default -> throw new IOException("unsupported real width: " + byteCount);
        };
    }

    /**
     * Decodes a {@code 0x33} date. IEEE-754 seconds since
     * 2001-01-01 UTC.
     *
     * @param offset the marker offset
     * @return the date value
     */
    private PlistDateValue readDate(int offset) {
        var seconds = Double.longBitsToDouble(readUnsignedBigEndian(offset + 1, 8));
        var whole = (long) Math.floor(seconds);
        var nanos = (long) ((seconds - whole) * 1_000_000_000L);
        return new PlistDateValue(Instant.ofEpochSecond(whole + 978_307_200L, nanos));
    }

    /**
     * Decodes a {@code 0x4n} data marker. The returned
     * {@link PlistDataValue} is a zero-copy slice over {@link #src}.
     *
     * @param offset the marker offset
     * @param info   the inline length, or {@code 0xF} when an
     *               extended length follows
     * @return the data value
     * @throws IOException if the slice escapes the source
     */
    private PlistDataValue readData(int offset, int info) throws IOException {
        var span = readLength(offset, info);
        return new PlistDataValue(src, span.dataOffset(), span.length());
    }

    /**
     * Decodes a {@code 0x5n}/{@code 0x6n}/{@code 0x7n} string
     * marker.
     *
     * @param offset       the marker offset
     * @param info         the inline length, or {@code 0xF}
     * @param charset      the string encoding
     * @param bytesPerUnit number of bytes per code unit (1 for
     *                     ASCII/UTF-8, 2 for UTF-16BE)
     * @return the string value
     * @throws IOException if the slice escapes the source
     */
    private PlistStringValue readString(int offset, int info, Charset charset, int bytesPerUnit) throws IOException {
        var span = readLength(offset, info);
        var byteLength = span.length() * bytesPerUnit;
        return new PlistStringValue(new String(src, span.dataOffset(), byteLength, charset));
    }

    /**
     * Decodes a {@code 0xAn} array marker.
     *
     * @param offset the marker offset
     * @param info   the inline count, or {@code 0xF}
     * @return the array value
     * @throws IOException if any element fails to decode
     */
    private PlistArrayValue readArray(int offset, int info) throws IOException {
        var span = readLength(offset, info);
        var count = span.length();
        var items = new ArrayList<PlistValue>(count);
        for (int i = 0; i < count; i++) {
            var ref = (int) readUnsignedBigEndian(span.dataOffset() + i * refSize, refSize);
            items.add(readObject(ref));
        }
        return new PlistArrayValue(items);
    }

    /**
     * Decodes a {@code 0xDn} dictionary marker. The on-disk layout
     * is N key references followed by N value references, both
     * contiguous.
     *
     * @param offset the marker offset
     * @param info   the inline count, or {@code 0xF}
     * @return the dictionary value
     * @throws IOException if a key is not a string
     */
    private PlistDictionaryValue readDict(int offset, int info) throws IOException {
        var span = readLength(offset, info);
        var count = span.length();
        var keysOffset = span.dataOffset();
        var valuesOffset = keysOffset + count * refSize;
        var entries = new LinkedHashMap<String, PlistValue>(Math.max(count * 2, 4));
        for (int i = 0; i < count; i++) {
            var keyRef = (int) readUnsignedBigEndian(keysOffset + i * refSize, refSize);
            var valueRef = (int) readUnsignedBigEndian(valuesOffset + i * refSize, refSize);
            var key = readObject(keyRef);
            if (!(key instanceof PlistStringValue s)) {
                throw new IOException("dictionary key is not a string: " + key);
            }
            entries.put(s.value(), readObject(valueRef));
        }
        return new PlistDictionaryValue(entries);
    }

    /**
     * Resolves the count and data start for a variable-length
     * marker. When {@code info < 0xF} the count is inline.
     * otherwise the next byte is itself an integer marker carrying
     * the actual count.
     *
     * @param markerOffset the marker offset
     * @param info         the low nibble of the marker
     * @return the {length, dataOffset} pair
     * @throws IOException if the extended-length marker is not an
     *                     integer
     */
    private LengthSpan readLength(int markerOffset, int info) throws IOException {
        if (info < 0xF) {
            return new LengthSpan(info, markerOffset + 1);
        }
        var nextMarker = src[markerOffset + 1] & 0xFF;
        if ((nextMarker >>> 4) != 0x1) {
            throw new IOException("expected integer marker for extended length, got 0x"
                    + Integer.toHexString(nextMarker));
        }
        var byteCount = 1 << (nextMarker & 0x0F);
        if (byteCount > 8) {
            throw new IOException("extended length exceeds 8 bytes");
        }
        var length = (int) readUnsignedBigEndian(markerOffset + 2, byteCount);
        return new LengthSpan(length, markerOffset + 2 + byteCount);
    }

    /**
     * Reads {@code byteCount} big-endian bytes as a {@code long}.
     * The natural overflow gives the correct two's-complement value
     * for 8-byte signed integers.
     *
     * @param offset    the start offset
     * @param byteCount the number of bytes from 1 to 8
     * @return the assembled value
     */
    private long readUnsignedBigEndian(int offset, int byteCount) {
        long value = 0;
        for (int i = 0; i < byteCount; i++) {
            value = (value << 8) | (src[offset + i] & 0xFFL);
        }
        return value;
    }

    /**
     * Pair of {length, dataOffset} returned by
     * {@link #readLength(int, int)}.
     *
     * @param length     the count or byte length
     * @param dataOffset the offset of the first payload byte
     */
    private record LengthSpan(int length, int dataOffset) {
    }
}
