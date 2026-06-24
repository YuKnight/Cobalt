package com.github.auties00.cobalt.calls2.media.audio;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Builds the WhatsApp MLow redundancy payload: it prepends compact two-byte headers for the most recent
 * prior packets, then a single primary header byte, then the retained redundant payloads, and finally the
 * fresh primary payload, stopping when the cumulative size would reach the stream maximum transmission
 * unit.
 *
 * <p>MLow redundant audio carries one or more older payloads in the same packet as the current one so a
 * single loss can be recovered from the next packet's embedded copy. The layout is the redundant-audio
 * shape WhatsApp emits, not stock RFC 2198: all redundant block headers come first, then one primary
 * block header, then all redundant payloads in the same order, then the primary payload. Each redundant
 * header is two bytes carrying a continuation bit, a seven-bit timestamp offset (the redundant payload's
 * sample distance from the primary divided by ten), and an eight-bit payload length; the primary header
 * is a single zero byte. The packer walks the recent packets newest-first from the
 * {@link StreamPacketCache}, halting once adding another block would reach the MTU, the cache is
 * exhausted, or a candidate's timestamp offset or length overflows its header field.
 *
 * <p>Instances are not thread-safe; the send path drives one packer on its own thread.
 *
 * @implNote This implementation ports {@code mlow_red_pack_header} (fn4114,
 * tree/xplat/wa-voip/wacall/media/src/core/wa_stream_cache.cc lines 10-208) of the wa-voip WASM module
 * {@code ff-tScznZ8P}. The native code iterates the cached prior packets newest-to-older from
 * {@code wa_stream_cache_acquire_mlow_red_pkts_begin}, writing one two-byte redundant header per packet
 * (line 175: {@code local_2 = (timeOffset/10) | (length << 8) | 0x80}, emitted with {@code pj_memmove}
 * size 2). The timestamp offset is {@code stride * (main_payload_ext_seq - fec_pkt.ext_seq)} samples
 * (line 151), capped at {@code 0x4ff} (1279, line 153, logging "main_payload_ext_seq %u, fec_pkt.ext_seq
 * %u, time offset %u" at string offset 0x10fb1) and stored divided by ten in the seven-bit field; the
 * payload length is capped at {@code 0xff} (255, line 161, logging "size %u" at offset 0x124c2); the walk
 * stops when the running total reaches {@code stream_mtu} (line 168, logging "will exceed stream_mtu %u"
 * at offset 0x10d15) or a cached packet is null or empty (line 149, logging "stopping at index %d" at
 * offset 0x3a1b). After the redundant headers the native code reserves one zeroed primary header byte
 * (line 182: {@code uVar2 = uVar2 | 1} skips {@code local_683[2N]}, which {@code memory_fill} at line 59
 * left zero), then writes the redundant payloads (lines 187-199) and the primary payload last (lines
 * 201-203). The continuation bit {@code 0x80}, the seven-bit divided-by-ten timestamp offset, and the
 * eight-bit length are exact; the divide-by-ten and the all-headers-then-all-payloads framing are
 * recovered from fn4114, not assumed from RFC 2198. The sibling {@link StreamPacketCache.CachedPacket}
 * retains a packet's extended sequence but not its RTP timestamp, so a redundant block's timestamp offset
 * is computed as its sequence distance from the primary multiplied by the per-frame sample stride, which
 * equals the native {@code stride * (main_ext_seq - fec_ext_seq)} for the fixed-ptime audio stream.
 */
public final class MLowRedPacker {
    /**
     * The redundant-block header size, in bytes, of the WhatsApp MLow redundancy form.
     */
    private static final int REDUNDANT_HEADER_BYTES = 2;

    /**
     * The primary-block header size, in bytes: a single zero continuation byte.
     */
    private static final int PRIMARY_HEADER_BYTES = 1;

    /**
     * The {@code F} continuation bit set in every redundant-block header.
     *
     * <p>The primary header byte is zero, so its continuation bit is cleared.
     */
    private static final int F_BIT = 0x80;

    /**
     * Divisor applied to the sample-denominated timestamp offset before it is stored in the seven-bit
     * header field.
     *
     * <p>The native code stores {@code timeOffsetSamples / 10} (fn4114 line 175); with the
     * {@link #MAX_TIMESTAMP_OFFSET_SAMPLES} cap the quotient fits the seven-bit field.
     */
    private static final int TIMESTAMP_OFFSET_DIVISOR = 10;

    /**
     * The maximum redundant-block timestamp offset, in samples, before the redundancy walk halts.
     *
     * <p>The native cap is {@code 0x4ff} (fn4114 line 153): a candidate whose sample offset exceeds this
     * stops packing.
     */
    private static final int MAX_TIMESTAMP_OFFSET_SAMPLES = 0x4FF;

    /**
     * The maximum representable redundant-block length, the eight-bit field width of the header.
     */
    private static final int MAX_BLOCK_LENGTH = 0xFF;

    /**
     * The stream maximum transmission unit, in bytes, the assembled payload must not reach.
     */
    private final int streamMtu;

    /**
     * The maximum number of redundant blocks to prepend ahead of the primary.
     */
    private final int maxRedundancyLevel;

    /**
     * The per-frame sample stride the timestamp offset of a redundant block is computed from.
     *
     * <p>The cache retains a packet's extended sequence but not its RTP timestamp; for the fixed-ptime
     * audio stream each consecutive packet's timestamp advances by this stride, so the timestamp offset of
     * a redundant block is its sequence distance from the primary multiplied by this value.
     */
    private final int samplesPerFrame;

    /**
     * Constructs a RED packer for the given MTU, redundancy ceiling, and frame stride.
     *
     * @param streamMtu          the stream MTU in bytes the assembled payload must not reach; must be
     *                           positive
     * @param maxRedundancyLevel the maximum number of redundant blocks to prepend; must not be negative
     * @param samplesPerFrame    the per-frame sample stride used to derive a redundant block's timestamp
     *                           offset from its sequence distance; must be positive
     * @throws IllegalArgumentException if {@code streamMtu} is not positive, {@code maxRedundancyLevel}
     *                                  is negative, or {@code samplesPerFrame} is not positive
     */
    public MLowRedPacker(int streamMtu, int maxRedundancyLevel, int samplesPerFrame) {
        if (streamMtu <= 0) {
            throw new IllegalArgumentException("streamMtu must be positive: " + streamMtu);
        }
        if (maxRedundancyLevel < 0) {
            throw new IllegalArgumentException("maxRedundancyLevel must not be negative: " + maxRedundancyLevel);
        }
        if (samplesPerFrame <= 0) {
            throw new IllegalArgumentException("samplesPerFrame must be positive: " + samplesPerFrame);
        }
        this.streamMtu = streamMtu;
        this.maxRedundancyLevel = maxRedundancyLevel;
        this.samplesPerFrame = samplesPerFrame;
    }

    /**
     * Assembles a RED payload prepending recent prior packets ahead of the given primary payload.
     *
     * <p>Draws up to {@link #maxRedundancyLevel} recent packets older than the primary from the cache,
     * newest first, and selects each whose addition keeps the running total below {@link #streamMtu}. The
     * selected redundant headers are emitted first, then a single primary header byte, then the selected
     * redundant payloads, then the primary payload. A cached packet with an empty payload, a timestamp
     * offset beyond {@link #MAX_TIMESTAMP_OFFSET_SAMPLES}, or a length beyond {@link #MAX_BLOCK_LENGTH}
     * halts the redundancy walk. A primary that alone reaches the MTU still produces a valid
     * (redundancy-free) RED payload.
     *
     * @param cache           the stream packet cache holding the recent prior packets
     * @param primaryPayload  the fresh primary payload to send
     * @param primarySequence the primary packet's RTP extended sequence number
     * @return the assembled RED payload bytes
     * @throws NullPointerException if {@code cache} or {@code primaryPayload} is {@code null}
     */
    public byte[] pack(StreamPacketCache cache, byte[] primaryPayload, long primarySequence) {
        Objects.requireNonNull(cache, "cache cannot be null");
        Objects.requireNonNull(primaryPayload, "primaryPayload cannot be null");
        var selected = selectRedundantBlocks(cache, primaryPayload, primarySequence);
        var out = new ByteArrayOutputStream();
        for (var block : selected) {
            writeRedundantHeader(out, block.timestampOffsetSamples(), block.payload().length);
        }
        out.write(0);
        for (var block : selected) {
            out.writeBytes(block.payload());
        }
        out.writeBytes(primaryPayload);
        return out.toByteArray();
    }

    /**
     * Selects the redundant blocks to carry ahead of the primary, newest first.
     *
     * <p>Walks the recent packets older than the primary, accumulating the running payload size that the
     * headers-then-payloads layout produces, and stops at the first packet whose payload is empty, whose
     * timestamp offset or length overflows its header field, or whose addition would reach the MTU.
     *
     * @param cache           the stream packet cache holding the recent prior packets
     * @param primaryPayload  the fresh primary payload, whose size seeds the running total
     * @param primarySequence the primary packet's RTP extended sequence number
     * @return the selected redundant blocks in send order, newest first
     */
    private List<RedundantBlock> selectRedundantBlocks(StreamPacketCache cache, byte[] primaryPayload, long primarySequence) {
        var selected = new ArrayList<RedundantBlock>();
        if (maxRedundancyLevel == 0) {
            return selected;
        }
        var running = PRIMARY_HEADER_BYTES + primaryPayload.length;
        var recent = cache.acquireMlowRedRange(primarySequence, maxRedundancyLevel);
        for (var packet : recent) {
            var redundant = packet.payload();
            if (redundant.length == 0) {
                break;
            }
            var sequenceDelta = primarySequence - packet.extendedSequence();
            var offset = sequenceDelta * (long) samplesPerFrame;
            if (offset < 0 || offset > MAX_TIMESTAMP_OFFSET_SAMPLES || redundant.length > MAX_BLOCK_LENGTH) {
                break;
            }
            var blockSize = REDUNDANT_HEADER_BYTES + redundant.length;
            if (running + blockSize >= streamMtu) {
                break;
            }
            selected.add(new RedundantBlock((int) offset, redundant));
            running += blockSize;
        }
        return selected;
    }

    /**
     * Writes one MLow redundant-block header to the output.
     *
     * <p>The two-byte header is {@code [F | (tsOffset / 10), length]}: the continuation bit set with the
     * seven-bit divided-by-ten timestamp offset in the first byte, then the eight-bit block length.
     *
     * @param out                   the output to append the header to
     * @param timestampOffsetSamples the timestamp offset of the redundant payload, in samples, in
     *                               {@code 0..1279}
     * @param blockLength           the redundant payload length, in {@code 0..255}
     */
    private void writeRedundantHeader(ByteArrayOutputStream out, int timestampOffsetSamples, int blockLength) {
        out.write(F_BIT | ((timestampOffsetSamples / TIMESTAMP_OFFSET_DIVISOR) & 0x7F));
        out.write(blockLength & 0xFF);
    }

    /**
     * One selected redundant block: its sample-denominated timestamp offset from the primary and its
     * retained payload.
     *
     * @param timestampOffsetSamples the timestamp offset from the primary, in samples
     * @param payload                the retained redundant payload bytes
     */
    private record RedundantBlock(int timestampOffsetSamples, byte[] payload) {
    }
}
