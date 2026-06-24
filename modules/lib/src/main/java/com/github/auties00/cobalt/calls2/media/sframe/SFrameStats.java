package com.github.auties00.cobalt.calls2.media.sframe;


/**
 * An immutable snapshot of one SFrame stream's processing counters: the frames and bytes sealed or
 * opened and the error tallies the decode path accumulates.
 *
 * <p>A {@link SFrameSecureFrame} maintains running counters as it seals and opens frames and exposes
 * them through this record, which the call telemetry layer drains into the WAM call event. The
 * counters are cumulative for the lifetime of the stream: {@link #totalFrames()} and
 * {@link #totalBytes()} count every frame and plaintext byte processed, {@link #errorFrames()}
 * counts frames the transform failed to process, and the remaining fields break the decode-side
 * errors into the categories the native stats expose: duplicate (replayed) frames, frames whose key
 * id had no installed key, and frames rejected for an invalid parameter or malformed trailer.
 *
 * @param totalFrames        the cumulative count of frames sealed or opened
 * @param totalBytes         the cumulative count of plaintext bytes sealed or opened
 * @param errorFrames        the cumulative count of frames that failed to seal or open
 * @param duplicateFrames    the decode-side count of frames rejected as replays of a seen counter
 * @param missingKeyFrames   the decode-side count of frames whose key id had no installed key
 * @param invalidParamFrames the decode-side count of frames rejected for a malformed trailer or
 *                           invalid parameter
 * @implNote This implementation surfaces the fields {@code wa_sframe_get_stats} (fn6901) reads from
 * the {@code SecureFrameTransform}: {@code total_frames} (off 0x20), {@code total_bytes} (off 0x40),
 * and {@code error_frames} (off 0x48) of the wa-voip WASM module {@code ff-tScznZ8P}, plus the
 * decode-only categories ({@code dup_pkt_cnts}, {@code error_missing_key},
 * {@code invalid_param_error_cnt}) the native decode path tallies. fn6901 also walks a linked list of
 * per-frame-type byte buckets and folds them into a small set of aggregate byte sums (the all-types
 * total plus three per-type-group sums for frame types {@code 2}, {@code 6}, and {@code 7..10});
 * {@link #totalBytes()} carries that all-types total. The per-type-group byte sums are deliberately not
 * surfaced: the WAM {@code CallEvent} field-stats consumer reads only the SFrame packet COUNT fields
 * ({@code wa_sframe_*_rx_dup_pkts_cnt}, {@code *_rx_error_missing_key}, {@code *_rx_reject_pkts_cnt},
 * {@code *_tx_error_pkt_cnt}), which the count fields here cover, and no WAM field consumes a
 * per-frame-type SFrame byte breakdown, so adding the per-type byte sums would surface state no
 * consumer reads.
 */
public record SFrameStats(
        long totalFrames,
        long totalBytes,
        long errorFrames,
        long duplicateFrames,
        long missingKeyFrames,
        long invalidParamFrames
) {
    /**
     * Returns an all-zero stats snapshot, the state of a stream that has processed no frames.
     *
     * @return the empty stats snapshot
     */
    public static SFrameStats empty() {
        return new SFrameStats(0, 0, 0, 0, 0, 0);
    }
}
