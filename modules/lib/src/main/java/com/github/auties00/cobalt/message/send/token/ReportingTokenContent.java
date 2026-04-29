package com.github.auties00.cobalt.message.send.token;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.reporting.ReportingConfig;
import com.github.auties00.cobalt.model.reporting.ReportingConfigSpec;
import com.github.auties00.cobalt.model.reporting.ReportingField;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Builds the deterministic franking content over which the reporting-token
 * HMAC is computed.
 *
 * <p>The reporting token authenticates a <em>sparse</em> copy of a
 * message's serialised protobuf, not the full payload: only the field
 * numbers whitelisted by {@link ReportingConfig} for the current sender
 * version are retained, recursively. Computing the HMAC over anything
 * other than this sparse copy makes the server-side verification fail
 * with a {@code reporting-token-validation-failure}, even when every
 * other field of the stanza is correct.
 *
 * <p>The whitelist is shipped inline in the WA Web bundle as a
 * base64-encoded protobuf
 * ({@link #REPORTING_TOKEN_CONFIG_BASE64}); this class decodes it lazily
 * via {@link ReportingConfigSpec#decode(byte[])} and caches the result.
 *
 * <p>The walker builds an intermediate tree of {@link KeptNode}
 * carriers, summing each node's emit size on the way up, then
 * materialises the output into a single pre-sized {@code byte[]}.
 *
 * @implNote WAWebReportingTokenConfig +
 * WAWebReportingTokenContent.ReportingTokenContentCalculator: the JS
 * module decodes {@code REPORTING_TOKEN_CONFIG_BASE64} against
 * {@code WAWebProtobufsReporting.pb.ConfigSpec}, prunes the rule tree per
 * {@code senderVersion}, walks the encoded message protobuf
 * varint-by-varint without deserialising it, and concatenates the raw
 * bytes of the kept fields after sorting by ascending field number.
 * Cobalt mirrors the algorithm but reuses
 * {@link ReportingConfigSpec#decode(byte[])} for the config decoding step
 * so the field rules round-trip through the standard protobuf model.
 */
@WhatsAppWebModule(moduleName = "WAWebReportingTokenConfig")
@WhatsAppWebModule(moduleName = "WAWebReportingTokenContent")
public final class ReportingTokenContent {
    /**
     * Wire type for VARINT-encoded fields (int32, int64, bool, enum).
     *
     * @implNote WAProtoConst.ENC.VARINT.
     */
    private static final int WIRE_VARINT = 0;

    /**
     * Wire type for fixed 64-bit fields (fixed64, sfixed64, double).
     *
     * @implNote WAProtoConst.ENC.BIT64.
     */
    private static final int WIRE_BIT64 = 1;

    /**
     * Wire type for length-delimited fields (string, bytes, sub-message).
     *
     * @implNote WAProtoConst.ENC.BINARY.
     */
    private static final int WIRE_LENGTH_DELIMITED = 2;

    /**
     * Wire type for fixed 32-bit fields (fixed32, sfixed32, float).
     *
     * @implNote WAProtoConst.ENC.BIT32.
     */
    private static final int WIRE_BIT32 = 5;

    /**
     * Mask for extracting the wire type bits from a tag varint.
     */
    private static final int WIRE_TYPE_MASK = 0x07;

    /**
     * Bit shift applied to recover the field number from a tag varint.
     */
    private static final int FIELD_NUMBER_SHIFT = 3;

    /**
     * Sorts kept nodes by ascending field number to match WA Web's
     * canonical emission order.
     */
    private static final Comparator<KeptNode> NODE_BY_FIELD_NUMBER =
            Comparator.comparingInt(KeptNode::fieldNumber);

    /**
     * The base64-encoded {@code Config} protobuf shipped inline in the WA
     * Web bundle. Decoded against {@link ReportingConfigSpec} on first use.
     *
     * @implNote WAWebReportingTokenConfig: {@code REPORTING_TOKEN_CONFIG_BASE64}.
     */
    @WhatsAppWebExport(moduleName = "WAWebReportingTokenConfig", exports = "REPORTING_TOKEN_CONFIG_BASE64",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static final String REPORTING_TOKEN_CONFIG_BASE64 =
            "CgQIARIACjQIAxIwKgQIAhIAKgQIAxIAKgQICBIAKgQICxIAKhAIERIMKgQIFRIAKgQIFhIAKgQIGRIA"
                    + "CioIBBImCAIqBggBEgIIAioGCBASAggCKhIIERIOCAIqBAgVEgAqBAgWEgAKOggFEjYIAioGCAMSAggC"
                    + "KgYIBBICCAIqBggFEgIIAioGCBASAggCKhIIERIOCAIqBAgVEgAqBAgWEgAKIggGEh4qBAgBEgAqEAgR"
                    + "EgwqBAgVEgAqBAgWEgAqBAgeEgAKLggHEioqBAgCEgAqBAgHEgAqBAgKEgAqEAgREgwqBAgVEgAqBAgW"
                    + "EgAqBAgUEgAKLggIEioqBAgCEgAqBAgHEgAqBAgJEgAqEAgREgwqBAgVEgAqBAgWEgAqBAgVEgAKNAgJ"
                    + "EjAqBAgCEgAqBAgGEgAqBAgHEgAqBAgNEgAqEAgREgwqBAgVEgAqBAgWEgAqBAgUEgAKKAgMEiQIAioG"
                    + "CAESAggCKgYIAhICCAIqCAgOEgQIAiABKgYIDxICCAIKKggSEiYIAioGCAYSAggCKgYIEBICCAIqEggR"
                    + "Eg4IAioECBUSACoECBYSAAouCBoSKioECAQSACoECAUSACoECAgSACoECA0SACoQCBESDCoECBUSACoE"
                    + "CBYSAApCCBwSPggCKgYIARICCAIqBggCEgIIAioGCAQSAggCKgYIBRICCAIqBggGEgIIAioSCAcSDggC"
                    + "KgQIFRIAKgQIFhIACgwIJRIIKgYIARICIAEKUggxEk4IAioGCAISAggCKhYIAxISCAIqBggBEgIIAioG"
                    + "CAISAggCKhIIBRIOCAIqBAgVEgAqBAgWEgAqFggIEhIIAioGCAESAggCKgYIAhICCAIKDAg1EggqBggB"
                    + "EgIgAQoOCDcSCggCKgYIARICIAEKDgg6EgoIAioGCAESAiABCg4IOxIKCAIqBggBEgIgAQpSCDwSTggC"
                    + "KgYIAhICCAIqFggDEhIIAioGCAESAggCKgYIAhICCAIqEggFEg4IAioECBUSACoECBYSACoWCAgSEggC"
                    + "KgYIARICCAIqBggCEgIIAgpSCEASTggCKgYIAhICCAIqFggDEhIIAioGCAESAggCKgYIAhICCAIqEggF"
                    + "Eg4IAioECBUSACoECBYSACoWCAgSEggCKgYIARICCAIqBggCEgIIAgo2CEISMggCKgQIAhIAKgQIBhIA"
                    + "KgQIBxIAKgQIDRIAKhAIERIMKgQIFRIAKgQIFhIAKgQIFBIACg4IShIKCAIqBggBEgIgAQoOCFcSCggC"
                    + "KgYIARICIAEKMghYEi4IAioGCAESAggCKg4IAhIKCAIqBggBEgIIAioSCAMSDggCKgQIFRIAKgQIFhIA"
                    + "Cg4IXBIKCAIqBggBEgIgAQoOCF0SCggCKgYIARICIAEKDgheEgoIAioGCAESAiAB";

    /**
     * Lazily-decoded singleton of the top-level {@link ReportingConfig}.
     */
    private static volatile ReportingConfig CONFIG;

    /**
     * Cache keyed by sender version: the decoded config does not vary
     * with the version, but pre-pruning is unnecessary because the
     * version filter is cheap and consulted at every node.
     */
    private static final Map<Integer, ReportingConfig> CONFIG_CACHE = new ConcurrentHashMap<>();

    /**
     * Private constructor preventing instantiation.
     *
     * @throws UnsupportedOperationException always
     */
    private ReportingTokenContent() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Returns the decoded reporting-token configuration for the given
     * sender version.
     *
     * <p>Equivalent to {@code WAWebReportingTokenConfig.getReportingTokenConfig(version)}.
     * The current implementation returns the same {@link ReportingConfig}
     * for every version because the version-pruning step is fused into
     * the byte walker; the parameter is preserved to keep the API
     * symmetric with WA Web and to support future version-specific
     * pruning if needed.
     *
     * @param senderVersion the {@code rt_sender_reporting_token_version}
     *                      currently in effect
     * @return the decoded {@link ReportingConfig}
     * @implNote WAWebReportingTokenConfig.getReportingTokenConfig.
     */
    @WhatsAppWebExport(moduleName = "WAWebReportingTokenConfig", exports = "getReportingTokenConfig",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public static ReportingConfig getConfig(int senderVersion) {
        return CONFIG_CACHE.computeIfAbsent(senderVersion, _ -> getOrDecodeConfig());
    }

    /**
     * Computes the deterministic franking content over which the
     * reporting-token HMAC must be applied.
     *
     * <p>Walks the protobuf wire format of {@code messageBytes}
     * field-by-field, keeps only the fields whitelisted by the config
     * for {@code senderVersion}, recurses on sub-messages, sorts the
     * retained fields by ascending field number, and concatenates the
     * raw bytes of each retained field. The output is bound to the
     * encoded layout of the source: changing the order or the wire
     * encoding of a kept field changes the HMAC, which is the desired
     * behaviour because the server replays the same algorithm on its
     * own copy of the message and rejects mismatches.
     *
     * @param messageBytes  the serialised {@code MessageContainer}
     *                      protobuf whose franking content is to be
     *                      computed
     * @param senderVersion the {@code rt_sender_reporting_token_version}
     *                      currently in effect
     * @return the franking content; possibly empty when no field of the
     *         message survives the filter
     * @throws NullPointerException     if {@code messageBytes} is
     *                                  {@code null}
     * @throws IllegalArgumentException if {@code messageBytes} is
     *                                  malformed
     * @implNote WAWebReportingTokenContent.ReportingTokenContentCalculator.getReportingTokenContent.
     */
    @WhatsAppWebExport(moduleName = "WAWebReportingTokenContent", exports = "ReportingTokenContentCalculator",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public static byte[] compute(byte[] messageBytes, int senderVersion) {
        if (messageBytes == null) {
            throw new NullPointerException("messageBytes cannot be null");
        }
        if (messageBytes.length == 0) {
            return messageBytes;
        }
        var config = getConfig(senderVersion);
        var topRules = config.field();
        var kept = new ArrayList<KeptNode>();
        var totalSize = extract(messageBytes, 0, messageBytes.length, topRules, topRules, senderVersion, kept);
        if (kept.isEmpty()) {
            return new byte[0];
        }
        kept.sort(NODE_BY_FIELD_NUMBER);
        var out = new byte[totalSize];
        var cursor = 0;
        for (var node : kept) {
            cursor = node.write(messageBytes, out, cursor);
        }
        return out;
    }

    /**
     * Recursively walks {@code src[start, end)}, appends every
     * whitelisted field as a {@link KeptNode} into {@code out}, and
     * returns the total number of bytes the appended nodes will emit.
     *
     * @param src           the raw protobuf bytes containing the
     *                      sub-region to walk
     * @param start         inclusive start offset of the sub-region
     * @param end           exclusive end offset of the sub-region
     * @param rules         the rule map applicable at this level
     * @param topRules      the top-level rule map, consulted when a kept
     *                      field carries {@code isMessage = true}
     * @param senderVersion the version that filters out rules whose
     *                      {@code minVersion}/{@code maxVersion} bracket
     *                      excludes it
     * @param out           accumulator for the kept nodes at this level
     * @return the total emit size of the nodes that were appended at
     *         this level
     * @throws IllegalArgumentException if {@code src} is malformed
     */
    private static int extract(byte[] src, int start, int end,
                               Map<Integer, ReportingField> rules,
                               Map<Integer, ReportingField> topRules,
                               int senderVersion, List<KeptNode> out) {
        var emitSize = 0;
        var cursor = start;
        while (cursor < end) {
            var tagStart = cursor;
            var tagRead = readVarInt(src, cursor, end);
            cursor = tagRead.cursor();
            var fieldNumber = (int) (tagRead.value() >>> FIELD_NUMBER_SHIFT);
            var wireType = (int) (tagRead.value() & WIRE_TYPE_MASK);
            var valueStart = cursor;
            cursor = skipValue(src, cursor, end, wireType);
            var rule = rules == null ? null : rules.get(fieldNumber);
            if (rule == null || !versionMatches(rule, senderVersion)) {
                continue;
            }
            var hasSubfields = !rule.subfield().isEmpty();
            var recurse = wireType == WIRE_LENGTH_DELIMITED && (rule.isMessage() || hasSubfields);
            if (recurse) {
                var lengthRead = readVarInt(src, valueStart, end);
                var innerStart = lengthRead.cursor();
                var innerEnd = innerStart + (int) lengthRead.value();
                if (innerEnd > end) {
                    throw new IllegalArgumentException("Reporting-token content: inner submessage exceeds parent bounds");
                }
                cursor = innerEnd;
                var innerKept = new ArrayList<KeptNode>();
                var innerRules = rule.isMessage() ? topRules : rule.subfield();
                var innerSize = extract(src, innerStart, innerEnd, innerRules, topRules, senderVersion, innerKept);
                if (innerKept.isEmpty()) {
                    continue;
                }
                innerKept.sort(NODE_BY_FIELD_NUMBER);
                var tagLength = valueStart - tagStart;
                var nodeSize = tagLength + getVarIntSize(innerSize) + innerSize;
                out.add(new KeptNode.Branch(fieldNumber, tagStart, tagLength, innerSize, innerKept));
                emitSize += nodeSize;
            } else {
                var fieldLength = cursor - tagStart;
                out.add(new KeptNode.Leaf(fieldNumber, tagStart, fieldLength));
                emitSize += fieldLength;
            }
        }
        return emitSize;
    }

    /**
     * Returns whether {@code rule} applies for {@code version}.
     *
     * <p>WA Web's {@code d(version, fieldNumber, configEntry)} returns
     * {@code null} when {@code version < minVersion} or
     * {@code version > maxVersion}, with {@code maxVersion} defaulting
     * to positive infinity. Cobalt mirrors that and additionally honours
     * {@code notReportableMinVersion} as the inclusive lower bound for
     * deprecation: at or above that version the rule no longer applies.
     *
     * @param rule    the rule from {@link ReportingField}
     * @param version the sender reporting-token version
     * @return {@code true} when the rule applies
     */
    private static boolean versionMatches(ReportingField rule, int version) {
        if (rule.minVersion().isPresent() && version < rule.minVersion().getAsInt()) {
            return false;
        }
        if (rule.maxVersion().isPresent() && version > rule.maxVersion().getAsInt()) {
            return false;
        }
        return rule.notReportableMinVersion().isEmpty() || version < rule.notReportableMinVersion().getAsInt();
    }

    /**
     * Decodes the bundled {@link #REPORTING_TOKEN_CONFIG_BASE64} into a
     * {@link ReportingConfig} the first time it is requested and returns
     * the cached instance thereafter.
     *
     * @return the singleton {@link ReportingConfig}
     */
    private static ReportingConfig getOrDecodeConfig() {
        var local = CONFIG;
        if (local != null) {
            return local;
        }
        synchronized (ReportingTokenContent.class) {
            if (CONFIG == null) {
                var bytes = Base64.getDecoder().decode(REPORTING_TOKEN_CONFIG_BASE64);
                CONFIG = ReportingConfigSpec.decode(bytes);
            }
            return CONFIG;
        }
    }

    /**
     * Advances {@code cursor} past one wire-format value of type
     * {@code wireType}.
     *
     * @param src      the source bytes
     * @param cursor   the position pointing at the first value byte
     * @param end      exclusive end of the sub-region
     * @param wireType the protobuf wire type
     * @return the position immediately after the value bytes
     * @throws IllegalArgumentException if the value is malformed or the
     *                                  wire type is unsupported
     */
    private static int skipValue(byte[] src, int cursor, int end, int wireType) {
        switch (wireType) {
            case WIRE_VARINT -> {
                return readVarInt(src, cursor, end).cursor();
            }
            case WIRE_BIT64 -> {
                if (cursor + 8 > end) {
                    throw new IllegalArgumentException("Reporting-token content: truncated 64-bit value");
                }
                return cursor + 8;
            }
            case WIRE_LENGTH_DELIMITED -> {
                var lengthRead = readVarInt(src, cursor, end);
                var afterLen = lengthRead.cursor();
                var length = (int) lengthRead.value();
                if (afterLen + length > end) {
                    throw new IllegalArgumentException("Reporting-token content: truncated length-delimited value");
                }
                return afterLen + length;
            }
            case WIRE_BIT32 -> {
                if (cursor + 4 > end) {
                    throw new IllegalArgumentException("Reporting-token content: truncated 32-bit value");
                }
                return cursor + 4;
            }
            default -> throw new IllegalArgumentException("Reporting-token content: unsupported wire type " + wireType);
        }
    }

    /**
     * Reads a single LEB128-encoded varint starting at {@code cursor}.
     *
     * @param src    the source bytes
     * @param cursor the position pointing at the first varint byte
     * @param end    exclusive end of the sub-region
     * @return the decoded value paired with the position immediately
     *         after the varint
     * @throws IllegalArgumentException if the varint is truncated or
     *                                  longer than 10 bytes (overflowing
     *                                  a 64-bit unsigned integer)
     */
    private static VarInt readVarInt(byte[] src, int cursor, int end) {
        var value = 0L;
        var shift = 0;
        var pos = cursor;
        while (pos < end) {
            var b = src[pos++];
            value |= ((long) (b & 0x7F)) << shift;
            if ((b & 0x80) == 0) {
                return new VarInt(value, pos);
            }
            shift += 7;
            if (shift > 63) {
                throw new IllegalArgumentException("Reporting-token content: varint overflow");
            }
        }
        throw new IllegalArgumentException("Reporting-token content: truncated varint");
    }

    /**
     * Returns the number of bytes a non-negative {@code value} occupies
     * when LEB128-encoded.
     *
     * <p>Constants are folded so the lookup is a chain of compares
     * rather than a loop. Negative values are treated as 10-byte sign
     * extensions to match the protobuf wire format, even though the
     * walker never produces negative inner-message lengths.
     *
     * @param value the integer to be encoded
     * @return the number of bytes the LEB128 encoding will occupy
     */
    private static int getVarIntSize(long value) {
        if (value < 0) {
            return 10;
        } else if (value < 1L << 7) {
            return 1;
        } else if (value < 1L << 14) {
            return 2;
        } else if (value < 1L << 21) {
            return 3;
        } else if (value < 1L << 28) {
            return 4;
        } else if (value < 1L << 35) {
            return 5;
        } else if (value < 1L << 42) {
            return 6;
        } else if (value < 1L << 49) {
            return 7;
        } else if (value < 1L << 56) {
            return 8;
        } else {
            return 9;
        }
    }

    /**
     * Writes {@code value} as a LEB128-encoded varint into {@code out}
     * starting at {@code cursor}.
     *
     * @param out    destination buffer (must be pre-sized)
     * @param cursor the offset at which to start writing
     * @param value  the unsigned integer to encode
     * @return the offset immediately after the written bytes
     */
    private static int writeVarInt(byte[] out, int cursor, long value) {
        var pos = cursor;
        var v = value;
        while ((v & ~0x7FL) != 0) {
            out[pos++] = (byte) ((v & 0x7F) | 0x80);
            v >>>= 7;
        }
        out[pos++] = (byte) (v & 0x7F);
        return pos;
    }

    /**
     * Carrier returned by {@link #readVarInt}: the decoded value paired
     * with the cursor advanced past the varint.
     *
     * @param value  the decoded value
     * @param cursor the position immediately after the varint
     */
    private record VarInt(long value, int cursor) {
    }

    /**
     * A field that survived the whitelist filter, plus the logic to
     * write it into the output buffer.
     */
    private sealed interface KeptNode {
        /**
         * Returns the protobuf field number used for sort order.
         *
         * @return the field number
         */
        int fieldNumber();

        /**
         * Writes this node's bytes into {@code out} starting at
         * {@code cursor} and returns the new cursor.
         *
         * @param src    the original source bytes (verbatim slices are
         *               copied straight from here)
         * @param out    destination buffer (must be pre-sized)
         * @param cursor the offset at which to start writing
         * @return the offset immediately after the written bytes
         */
        int write(byte[] src, byte[] out, int cursor);

        /**
         * A field that is kept verbatim: the writer copies the tag plus
         * the value bytes straight out of the source.
         *
         * @param fieldNumber the protobuf field number used for sort
         *                    order
         * @param sourceStart the offset into the source where the tag
         *                    bytes begin
         * @param length      the number of bytes spanning the tag plus
         *                    the value
         */
        record Leaf(int fieldNumber, int sourceStart, int length) implements KeptNode {
            @Override
            public int write(byte[] src, byte[] out, int cursor) {
                System.arraycopy(src, sourceStart, out, cursor, length);
                return cursor + length;
            }
        }

        /**
         * A length-delimited field whose body has been recursively
         * pruned. The original tag is reused verbatim, but the inner
         * length-prefix is freshly emitted because the pruned body's
         * size differs from the source body's size.
         *
         * @param fieldNumber the protobuf field number used for sort
         *                    order
         * @param tagStart    the offset into the source where the
         *                    original tag bytes begin
         * @param tagLength   the number of bytes spanning the original
         *                    tag
         * @param innerSize   the total byte size of {@code children}
         *                    when written
         * @param children    the kept inner nodes, sorted by field
         *                    number
         */
        record Branch(int fieldNumber, int tagStart, int tagLength,
                      int innerSize, List<KeptNode> children) implements KeptNode {
            @Override
            public int write(byte[] src, byte[] out, int cursor) {
                System.arraycopy(src, tagStart, out, cursor, tagLength);
                var pos = cursor + tagLength;
                pos = writeVarInt(out, pos, innerSize);
                for (var child : children) {
                    pos = child.write(src, out, pos);
                }
                return pos;
            }
        }
    }
}
