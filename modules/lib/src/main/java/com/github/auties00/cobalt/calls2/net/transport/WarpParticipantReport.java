package com.github.auties00.cobalt.calls2.net.transport;

/**
 * Holds the ten-byte WARP participant rate-control report block carried by the
 * {@link WarpAttributeFlag#PARTICIPANT_REPORT participant-report} attribute.
 *
 * <p>The report tells the SFU one device's current rate-control view: its round-trip time, the
 * bandwidth it can receive and send, its observed loss, an auxiliary metric, and a bitset of which
 * media it currently has active. The block is fixed at {@value #BYTE_LENGTH} bytes and is laid out, in
 * order, as {@code rtt_ms} ({@code u16}), {@code dl_bw_kbps} ({@code u16}), {@code packet_loss_q8}
 * ({@code u8}, the loss rate scaled by {@code 256} and clamped to {@code 255}), {@code aux}
 * ({@code u16}), {@code media_dir_flags} ({@code u8}), and {@code uplink_bw_kbps} ({@code u16}), all
 * multi-byte fields big-endian.
 *
 * <p>The packet-loss field is a Q8 fixed-point fraction: {@link #ofLossRate(int, int, int, int, double, int)}
 * converts a {@code [0,1]} loss rate to it by multiplying by {@code 256} and clamping to {@code 255},
 * while {@link #lossRate()} reverses the conversion. The {@link #mediaDirFlags() media-direction flags}
 * are an opaque bitset whose low bit is a base flag and whose higher bits mark individual enabled
 * streams or features.
 *
 * @implNote This implementation reproduces the block written by {@code warp_write_participant_report_data}
 *           (fn5152) and filled by {@code warp_mcs_prepare_warp_pr} (fn4043) from the wa-voip WASM
 *           module {@code ff-tScznZ8P} ({@code sfu/wa_warp_msg.cc},
 *           {@code sfu/wa_warp_media_control_session.cc}). The native filler computes
 *           {@code rtt_ms = rtt/1000}, {@code dl_bw_kbps = update_dl_bw/1000},
 *           {@code packet_loss_q8 = min(255, loss_rate*256)}, and {@code uplink_bw_kbps = uplink_bw/1000}
 *           (zero when one-side BWE is inactive); those derivations live in the rate-control layer, so
 *           this record carries the already-scaled wire values. The {@code aux} field is the native
 *           {@code DAT_1f74} auxiliary rate-control metric whose precise meaning is not recovered.
 *
 * @param rttMs         the round-trip time in milliseconds, an unsigned sixteen-bit value
 * @param dlBwKbps      the downlink bandwidth in kilobits per second, an unsigned sixteen-bit value
 * @param packetLossQ8  the packet-loss rate as a Q8 fraction, in the range {@code 0..255}
 * @param aux           the auxiliary rate-control metric, an unsigned sixteen-bit value
 * @param mediaDirFlags the active-media bitset, an unsigned eight-bit value
 * @param uplinkBwKbps  the uplink bandwidth in kilobits per second, an unsigned sixteen-bit value
 */
public record WarpParticipantReport(int rttMs,
                                    int dlBwKbps,
                                    int packetLossQ8,
                                    int aux,
                                    int mediaDirFlags,
                                    int uplinkBwKbps) {
    /**
     * The fixed length, in bytes, of a participant-report block on the wire.
     */
    public static final int BYTE_LENGTH = 10;

    /**
     * The maximum value of the Q8 packet-loss field, the clamp the engine applies to {@code rate*256}.
     */
    public static final int MAX_PACKET_LOSS_Q8 = 255;

    /**
     * The Q8 fixed-point scale, the factor a {@code [0,1]} loss rate is multiplied by.
     */
    private static final int Q8_SCALE = 256;

    /**
     * Canonicalizes the record components, validating each field fits its unsigned wire width.
     *
     * @throws IllegalArgumentException if {@code rttMs}, {@code dlBwKbps}, {@code aux}, or
     *                                  {@code uplinkBwKbps} is outside {@code 0..65535}, if
     *                                  {@code packetLossQ8} is outside {@code 0..255}, or if
     *                                  {@code mediaDirFlags} is outside {@code 0..255}
     */
    public WarpParticipantReport {
        requireU16(rttMs, "rttMs");
        requireU16(dlBwKbps, "dlBwKbps");
        requireRange(packetLossQ8, MAX_PACKET_LOSS_Q8, "packetLossQ8");
        requireU16(aux, "aux");
        requireRange(mediaDirFlags, 0xff, "mediaDirFlags");
        requireU16(uplinkBwKbps, "uplinkBwKbps");
    }

    /**
     * Creates a report from a {@code [0,1]} loss rate, converting it to the Q8 packet-loss field.
     *
     * <p>The loss rate is multiplied by {@value #Q8_SCALE}, rounded toward zero, and clamped to
     * {@value #MAX_PACKET_LOSS_Q8}, matching the engine's {@code min(255, loss_rate*256)}; the other
     * fields are taken as already-scaled wire values.
     *
     * @param rttMs         the round-trip time in milliseconds
     * @param dlBwKbps      the downlink bandwidth in kilobits per second
     * @param uplinkBwKbps  the uplink bandwidth in kilobits per second
     * @param aux           the auxiliary rate-control metric
     * @param lossRate      the packet-loss rate in the range {@code [0, 1]}; values outside are clamped
     * @param mediaDirFlags the active-media bitset
     * @return a participant report with {@code packetLossQ8} derived from {@code lossRate}
     */
    public static WarpParticipantReport ofLossRate(int rttMs,
                                                   int dlBwKbps,
                                                   int uplinkBwKbps,
                                                   int aux,
                                                   double lossRate,
                                                   int mediaDirFlags) {
        var scaled = (int) (Math.max(0.0, lossRate) * Q8_SCALE);
        var clamped = Math.min(MAX_PACKET_LOSS_Q8, scaled);
        return new WarpParticipantReport(rttMs, dlBwKbps, clamped, aux, mediaDirFlags, uplinkBwKbps);
    }

    /**
     * Returns the packet-loss rate as a fraction in {@code [0, 1]}, reversing the Q8 conversion.
     *
     * @return the loss rate, {@code packetLossQ8 / 256.0}
     */
    public double lossRate() {
        return packetLossQ8 / (double) Q8_SCALE;
    }

    /**
     * Writes this report's ten bytes, big-endian per field, into a buffer at the given offset.
     *
     * @param out    the destination buffer; must have at least {@value #BYTE_LENGTH} bytes free at
     *               {@code offset}
     * @param offset the index at which to write the first byte
     * @return the index immediately after the written block ({@code offset + }{@value #BYTE_LENGTH})
     * @throws NullPointerException           if {@code out} is {@code null}
     * @throws ArrayIndexOutOfBoundsException if the block does not fit at {@code offset}
     */
    public int writeTo(byte[] out, int offset) {
        var cursor = offset;
        cursor = putU16(out, cursor, rttMs);
        cursor = putU16(out, cursor, dlBwKbps);
        out[cursor++] = (byte) packetLossQ8;
        cursor = putU16(out, cursor, aux);
        out[cursor++] = (byte) mediaDirFlags;
        cursor = putU16(out, cursor, uplinkBwKbps);
        return cursor;
    }

    /**
     * Reads a ten-byte report block, big-endian per field, from a buffer at the given offset.
     *
     * @param in     the source buffer; must hold at least {@value #BYTE_LENGTH} bytes at {@code offset}
     * @param offset the index of the first byte of the block
     * @return the decoded participant report
     * @throws NullPointerException           if {@code in} is {@code null}
     * @throws ArrayIndexOutOfBoundsException if the block does not fit at {@code offset}
     */
    public static WarpParticipantReport readFrom(byte[] in, int offset) {
        var cursor = offset;
        var rttMs = readU16(in, cursor);
        cursor += 2;
        var dlBwKbps = readU16(in, cursor);
        cursor += 2;
        var packetLossQ8 = in[cursor++] & 0xff;
        var aux = readU16(in, cursor);
        cursor += 2;
        var mediaDirFlags = in[cursor++] & 0xff;
        var uplinkBwKbps = readU16(in, cursor);
        return new WarpParticipantReport(rttMs, dlBwKbps, packetLossQ8, aux, mediaDirFlags, uplinkBwKbps);
    }

    /**
     * Writes an unsigned sixteen-bit value big-endian into a buffer.
     *
     * @param out    the destination buffer
     * @param offset the index of the high byte
     * @param value  the value to write, masked to sixteen bits
     * @return the index immediately after the two written bytes
     */
    private static int putU16(byte[] out, int offset, int value) {
        out[offset] = (byte) (value >>> 8);
        out[offset + 1] = (byte) value;
        return offset + 2;
    }

    /**
     * Reads an unsigned sixteen-bit big-endian value from a buffer.
     *
     * @param in     the source buffer
     * @param offset the index of the high byte
     * @return the unsigned value in {@code 0..65535}
     */
    private static int readU16(byte[] in, int offset) {
        return ((in[offset] & 0xff) << 8) | (in[offset + 1] & 0xff);
    }

    /**
     * Validates that a value fits an unsigned sixteen-bit field.
     *
     * @param value the value to validate
     * @param name  the field name for the diagnostic
     * @throws IllegalArgumentException if {@code value} is outside {@code 0..65535}
     */
    private static void requireU16(int value, String name) {
        requireRange(value, 0xffff, name);
    }

    /**
     * Validates that a value lies in {@code 0..max}.
     *
     * @param value the value to validate
     * @param max   the inclusive upper bound
     * @param name  the field name for the diagnostic
     * @throws IllegalArgumentException if {@code value} is outside {@code 0..max}
     */
    private static void requireRange(int value, int max, String name) {
        if (value < 0 || value > max) {
            throw new IllegalArgumentException(name + " must be in [0, " + max + "], got " + value);
        }
    }
}
