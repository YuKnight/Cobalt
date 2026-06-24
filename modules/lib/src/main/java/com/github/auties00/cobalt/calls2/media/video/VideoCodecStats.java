package com.github.auties00.cobalt.calls2.media.video;

/**
 * Holds a snapshot of one {@link VideoCodec}'s lifetime counters.
 *
 * <p>The counters separate the encode and decode directions and call out the two events the rate
 * controller and the loss-recovery path react to: {@link #keyFramesEncoded()} tracks how often the
 * encoder emitted an instantaneous decoder refresh (a full intra picture), which the bandwidth
 * estimator treats as a cost spike, and {@link #keyFrameRequests()} tracks how often the decode side
 * asked for one after a loss it could not conceal. The byte sums ({@link #bytesEncoded()} and
 * {@link #bytesDecoded()}) accumulate the compressed payload bytes crossing each direction, and the
 * frame counts ({@link #framesEncoded()} and {@link #framesDecoded()}) accumulate the pictures.
 * Counters are monotonically non-decreasing for the lifetime of a codec instance and reset only when a
 * fresh codec is opened.
 *
 * @param framesEncoded    the total number of pictures handed to the encoder that produced output
 * @param framesDecoded    the total number of pictures the decoder reconstructed
 * @param keyFramesEncoded the total number of intra (key) pictures the encoder emitted
 * @param keyFrameRequests the total number of decoder-driven key-frame requests observed
 * @param bytesEncoded     the total compressed payload bytes the encoder produced
 * @param bytesDecoded     the total compressed payload bytes the decoder consumed
 * @implNote This implementation mirrors the per-stream counter block {@code wa_video_field_stats.cc}
 * maintains for the encode and decode ports of the wa-voip WASM module {@code ff-tScznZ8P}: the
 * keyframe-emitted and keyframe-requested tallies the rate controller and the PLI/FIR feedback path
 * read are surfaced here as two distinct fields rather than folded into a single frame-type histogram,
 * because the calls2 send path consults them independently (the estimator on emit, the receiver on
 * request). Byte and frame sums are accumulated Java-side around each native encode and decode call;
 * libvpx and OpenH264 do not expose a cumulative byte counter of their own.
 */
public record VideoCodecStats(
        long framesEncoded,
        long framesDecoded,
        long keyFramesEncoded,
        long keyFrameRequests,
        long bytesEncoded,
        long bytesDecoded
) {
    /**
     * Returns an all-zero stats snapshot, the value a freshly opened codec reports before its first
     * encode or decode call.
     *
     * @return a stats snapshot with every counter at zero
     */
    public static VideoCodecStats empty() {
        return new VideoCodecStats(0, 0, 0, 0, 0, 0);
    }
}
