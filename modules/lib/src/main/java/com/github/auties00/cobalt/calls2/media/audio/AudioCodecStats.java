package com.github.auties00.cobalt.calls2.media.audio;

/**
 * An immutable snapshot of one {@link AudioCodec} instance's lifetime counters: the FEC and PLC
 * recovery tallies, the rolling encode and decode timing, and the observed and target bitrates.
 *
 * <p>An {@link AudioCodec} accumulates these counters as it encodes and decodes and exposes them
 * through this record, which the call telemetry layer drains into the WAM call event and which the
 * codec also logs at close. {@link #fecFrames()} and {@link #plcFrames()} count the decode-side
 * recoveries (in-band FEC reconstructions and packet-loss-concealment fills); {@link #avgEncodeMicros()}
 * and {@link #avgDecodeMicros()} are the rolling per-call averages of the native encode and decode wall
 * time; {@link #avgTargetBitrate()} is the running average target bitrate the encoder ran at; and
 * {@link #observedBitrate()} is the bitrate derived from the running average encoded-frame size.
 *
 * @param totalEncodedFrames the cumulative count of frames passed to the encoder
 * @param totalDecodedFrames the cumulative count of frames passed to the decoder
 * @param fecFrames          the decode-side count of frames reconstructed from in-band FEC (LBRR)
 * @param plcFrames          the decode-side count of frames filled by packet-loss concealment
 * @param avgEncodeMicros    the rolling average native encode wall time per frame, in microseconds
 * @param avgDecodeMicros    the rolling average native decode wall time per frame, in microseconds
 * @param avgTargetBitrate   the running average encoder target bitrate, in bits per second
 * @param observedBitrate    the bitrate derived from the running average encoded-frame size, in bits
 *                           per second
 * @implNote This implementation surfaces the lifetime stats {@code opus_get_stats} (fn6274) and
 * {@code opus_codec_close} (fn6257) of the wa-voip WASM module {@code ff-tScznZ8P} snapshot and log:
 * {@code fec_cnt}, {@code plc_cnt}, the average encode/decode milliseconds-per-second, the average
 * target bitrate, and the {@code calc_bitrate_from_avg_size} (fn6267) observed bitrate. The native
 * stats also break the per-bandwidth frame counts (NB/MB/WB/SWB/FB) out; those are collapsed here since
 * Cobalt's telemetry consumes the aggregate frame counts and recovery tallies. Encode and decode timing
 * is held in microseconds for resolution rather than the native milliseconds-per-second ratio.
 */
public record AudioCodecStats(
        long totalEncodedFrames,
        long totalDecodedFrames,
        long fecFrames,
        long plcFrames,
        long avgEncodeMicros,
        long avgDecodeMicros,
        int avgTargetBitrate,
        int observedBitrate
) {
    /**
     * Returns an all-zero stats snapshot, the state of a codec that has processed no frames.
     *
     * @return the empty stats snapshot
     */
    public static AudioCodecStats empty() {
        return new AudioCodecStats(0, 0, 0, 0, 0, 0, 0, 0);
    }
}
