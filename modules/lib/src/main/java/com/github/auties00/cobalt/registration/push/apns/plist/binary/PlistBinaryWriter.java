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

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;

/**
 * Multi-pass exact-allocation writer for Apple's {@code bplist00}
 * binary format.
 *
 * <p>The writer first walks the tree to enumerate every object
 * (synthesizing a {@link PlistStringValue} per dictionary key, since
 * keys live in the object table just like values). It then sizes
 * each object given the now-known reference width, computes per-
 * object offsets, sums them to locate the offset table, derives the
 * offset width, and finally allocates a {@code byte[]} of the exact
 * total size and fills it in place — header, object table, offset
 * table, trailer.
 *
 * <p>No object deduplication is performed: equal-but-distinct
 * values get separate slots. Apple's parsers accept this.
 *
 * <p>Callers normally route through the
 * {@code Plist} facade; this class is the implementation.
 */
public final class PlistBinaryWriter {
    /**
     * Length of the trailer that closes a binary plist.
     */
    private static final int TRAILER_SIZE = 32;

    /**
     * Hidden constructor — the class is a stateless namespace.
     */
    private PlistBinaryWriter() {
    }

    /**
     * Serializes {@code root} as a {@code bplist00} binary plist.
     *
     * @param root the root value
     * @return the binary plist bytes
     */
    public static byte[] write(PlistValue root) {
        var ctx = new Context();
        ctx.collect(root);
        var numObjects = ctx.objects.size();
        var refSize = byteCountFor(numObjects - 1);

        var offsets = new int[numObjects];
        var objectTableSize = 0;
        for (int i = 0; i < numObjects; i++) {
            offsets[i] = PlistBinaryParser.MAGIC.length + objectTableSize;
            objectTableSize += sizeOf(ctx.objects.get(i), refSize);
        }

        var offsetTableOffset = PlistBinaryParser.MAGIC.length + objectTableSize;
        var offsetSize = byteCountFor(offsetTableOffset);
        var trailerOffset = offsetTableOffset + numObjects * offsetSize;
        var totalSize = trailerOffset + TRAILER_SIZE;

        var out = new byte[totalSize];
        System.arraycopy(PlistBinaryParser.MAGIC, 0, out, 0, PlistBinaryParser.MAGIC.length);

        var pos = PlistBinaryParser.MAGIC.length;
        for (int i = 0; i < numObjects; i++) {
            pos = encodeObject(out, pos, ctx.objects.get(i), refSize, ctx);
        }

        for (int i = 0; i < numObjects; i++) {
            writeBigEndian(out, offsetTableOffset + i * offsetSize, offsets[i], offsetSize);
        }

        out[trailerOffset + 6] = (byte) offsetSize;
        out[trailerOffset + 7] = (byte) refSize;
        writeBigEndian(out, trailerOffset + 8, numObjects, 8);
        writeBigEndian(out, trailerOffset + 16, 0L, 8);
        writeBigEndian(out, trailerOffset + 24, offsetTableOffset, 8);

        return out;
    }

    /**
     * Returns the encoded byte length of {@code v} given the
     * already-decided {@code refSize}. For containers this is the
     * marker (plus an extended-length suffix when the count is
     * {@code >= 15}) plus {@code refSize} bytes per child reference.
     *
     * @param v       the value
     * @param refSize the inter-object reference width
     * @return the byte count
     */
    private static int sizeOf(PlistValue v, int refSize) {
        return switch (v) {
            case PlistBooleanValue b -> 1;
            case PlistIntegerValue i -> 1 + integerWidth(i.value());
            case PlistFloatingPointValue r -> 1 + 8;
            case PlistDateValue d -> 1 + 8;
            case PlistDataValue d -> 1 + extendedLengthBytes(d.length()) + d.length();
            case PlistStringValue s -> {
                var charCount = s.value().length();
                var payloadBytes = isAscii(s.value()) ? charCount : charCount * 2;
                yield 1 + extendedLengthBytes(charCount) + payloadBytes;
            }
            case PlistArrayValue a -> 1 + extendedLengthBytes(a.items().size()) + a.items().size() * refSize;
            case PlistDictionaryValue d -> 1 + extendedLengthBytes(d.entries().size()) + 2 * d.entries().size() * refSize;
        };
    }

    /**
     * Encodes one object at {@code pos}.
     *
     * @param out     the destination buffer
     * @param pos     the current write position
     * @param v       the object to encode
     * @param refSize the inter-object reference width
     * @param ctx     the collection context (for resolving child
     *                references)
     * @return the position after the object
     */
    private static int encodeObject(byte[] out, int pos, PlistValue v, int refSize, Context ctx) {
        return switch (v) {
            case PlistBooleanValue b -> {
                out[pos++] = (byte) (b.value() ? 0x09 : 0x08);
                yield pos;
            }
            case PlistIntegerValue i -> {
                var width = integerWidth(i.value());
                out[pos++] = (byte) (0x10 | Integer.numberOfTrailingZeros(width));
                writeBigEndian(out, pos, i.value(), width);
                yield pos + width;
            }
            case PlistFloatingPointValue r -> {
                out[pos++] = 0x23;
                writeBigEndian(out, pos, Double.doubleToLongBits(r.value()), 8);
                yield pos + 8;
            }
            case PlistDateValue d -> {
                out[pos++] = 0x33;
                var seconds = (d.value().getEpochSecond() - 978_307_200L) + d.value().getNano() / 1e9;
                writeBigEndian(out, pos, Double.doubleToLongBits(seconds), 8);
                yield pos + 8;
            }
            case PlistDataValue d -> {
                pos = writeMarkerWithLength(out, pos, 0x40, d.length());
                System.arraycopy(d.source(), d.offset(), out, pos, d.length());
                yield pos + d.length();
            }
            case PlistStringValue s -> {
                var value = s.value();
                var charCount = value.length();
                if (isAscii(value)) {
                    pos = writeMarkerWithLength(out, pos, 0x50, charCount);
                    for (int i = 0; i < charCount; i++) {
                        out[pos++] = (byte) value.charAt(i);
                    }
                } else {
                    pos = writeMarkerWithLength(out, pos, 0x60, charCount);
                    for (int i = 0; i < charCount; i++) {
                        var c = value.charAt(i);
                        out[pos++] = (byte) (c >> 8);
                        out[pos++] = (byte) c;
                    }
                }
                yield pos;
            }
            case PlistArrayValue a -> {
                pos = writeMarkerWithLength(out, pos, 0xA0, a.items().size());
                for (var item : a.items()) {
                    writeBigEndian(out, pos, ctx.indices.get(item), refSize);
                    pos += refSize;
                }
                yield pos;
            }
            case PlistDictionaryValue d -> {
                pos = writeMarkerWithLength(out, pos, 0xD0, d.entries().size());
                var keys = ctx.dictKeys.get(d);
                for (var key : keys) {
                    writeBigEndian(out, pos, ctx.indices.get(key), refSize);
                    pos += refSize;
                }
                for (var entry : d.entries().entrySet()) {
                    writeBigEndian(out, pos, ctx.indices.get(entry.getValue()), refSize);
                    pos += refSize;
                }
                yield pos;
            }
        };
    }

    /**
     * Writes a container/data/string marker — combining the high
     * nibble of the marker base ({@code 0x4n}, {@code 0x5n}, …)
     * with the inline count when {@code count < 15}, or with the
     * extension nibble {@code 0xF} followed by an integer marker
     * carrying the actual count otherwise.
     *
     * @param out        the destination buffer
     * @param pos        the current write position
     * @param markerBase the high nibble (e.g.
     *                   {@code 0x40} for data)
     * @param count      the count to encode
     * @return the position after the marker
     */
    private static int writeMarkerWithLength(byte[] out, int pos, int markerBase, int count) {
        if (count < 0xF) {
            out[pos++] = (byte) (markerBase | count);
            return pos;
        }
        out[pos++] = (byte) (markerBase | 0x0F);
        var width = integerWidth(count);
        out[pos++] = (byte) (0x10 | Integer.numberOfTrailingZeros(width));
        writeBigEndian(out, pos, count, width);
        return pos + width;
    }

    /**
     * Returns the smallest power-of-two byte width that holds
     * {@code value}. Negative values always require 8 bytes (signed
     * two's complement); non-negative values use 1, 2, 4, or 8
     * unsigned per Apple's spec.
     *
     * @param value the integer value
     * @return the width in bytes (1, 2, 4, or 8)
     */
    private static int integerWidth(long value) {
        if (value < 0) {
            return 8;
        }
        if (value <= 0xFFL) {
            return 1;
        }
        if (value <= 0xFFFFL) {
            return 2;
        }
        if (value <= 0xFFFF_FFFFL) {
            return 4;
        }
        return 8;
    }

    /**
     * Returns the size of the extended-length suffix for a count
     * that does not fit in the marker's low nibble — one byte for
     * the integer marker plus the integer's own bytes. Returns
     * {@code 0} when the count is inline.
     *
     * @param count the count
     * @return the suffix size in bytes
     */
    private static int extendedLengthBytes(int count) {
        if (count < 0xF) {
            return 0;
        }
        return 1 + integerWidth(count);
    }

    /**
     * Returns the smallest power-of-two byte width sufficient to
     * encode {@code maxValue} unsigned. Used to pick {@code refSize}
     * and {@code offsetSize} for the trailer.
     *
     * @param maxValue the largest value to be stored
     * @return the width in bytes (1, 2, 4, or 8)
     */
    private static int byteCountFor(long maxValue) {
        if (maxValue < 0) {
            return 8;
        }
        if (maxValue <= 0xFFL) {
            return 1;
        }
        if (maxValue <= 0xFFFFL) {
            return 2;
        }
        if (maxValue <= 0xFFFF_FFFFL) {
            return 4;
        }
        return 8;
    }

    /**
     * Returns {@code true} if every character in {@code s} is in
     * the ASCII range.
     *
     * @param s the string
     * @return whether the string is ASCII-only
     */
    private static boolean isAscii(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) >= 0x80) {
                return false;
            }
        }
        return true;
    }

    /**
     * Writes {@code value} as {@code byteCount} big-endian bytes at
     * {@code pos}.
     *
     * @param out       the destination buffer
     * @param pos       the start offset
     * @param value     the value to write
     * @param byteCount the byte width (1–8)
     */
    private static void writeBigEndian(byte[] out, int pos, long value, int byteCount) {
        for (int i = byteCount - 1; i >= 0; i--) {
            out[pos + i] = (byte) (value & 0xFF);
            value >>>= 8;
        }
    }

    /**
     * Tree-collection state shared between the sizing and encoding
     * passes — every object's index in the object table, and for
     * each dictionary the synthesized key strings (since dict keys
     * are themselves objects in the table).
     */
    private static final class Context {
        /**
         * Object table in DFS order; root is at index 0.
         */
        private final List<PlistValue> objects = new ArrayList<>();

        /**
         * Index of each collected object. Identity-keyed so equal
         * but distinct values do not get deduplicated.
         */
        private final IdentityHashMap<PlistValue, Integer> indices = new IdentityHashMap<>();

        /**
         * For each dictionary, the synthesized {@link PlistStringValue}
         * objects for its keys, in entry order.
         */
        private final IdentityHashMap<PlistDictionaryValue, List<PlistStringValue>> dictKeys = new IdentityHashMap<>();

        /**
         * Recursively assigns an object-table index to {@code v} and
         * its descendants.
         *
         * @param v the value
         * @return the assigned index
         */
        int collect(PlistValue v) {
            var existing = indices.get(v);
            if (existing != null) {
                return existing;
            }
            var idx = objects.size();
            objects.add(v);
            indices.put(v, idx);
            switch (v) {
                case PlistArrayValue arr -> {
                    for (var item : arr.items()) {
                        collect(item);
                    }
                }
                case PlistDictionaryValue dict -> {
                    var keys = new ArrayList<PlistStringValue>(dict.entries().size());
                    for (var entry : dict.entries().entrySet()) {
                        var keyObject = new PlistStringValue(entry.getKey());
                        keys.add(keyObject);
                        collect(keyObject);
                    }
                    dictKeys.put(dict, keys);
                    for (var entry : dict.entries().entrySet()) {
                        collect(entry.getValue());
                    }
                }
                default -> {
                }
            }
            return idx;
        }
    }
}
